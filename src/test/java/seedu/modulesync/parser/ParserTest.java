package seedu.modulesync.parser;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.modulesync.command.AddModuleCommand;
import seedu.modulesync.command.AddTodoCommand;
import seedu.modulesync.command.ArchiveModuleCommand;
import seedu.modulesync.command.ArchiveSemesterCommand;
import seedu.modulesync.command.DeleteModuleCommand;
import seedu.modulesync.command.GradeCommand;
import seedu.modulesync.command.ListModulesCommand;
import seedu.modulesync.command.ListNotDoneCommand;
import seedu.modulesync.command.MarkCommand;
import seedu.modulesync.command.NewSemesterCommand;
import seedu.modulesync.command.SemesterStatsCommand;
import seedu.modulesync.command.SetCreditsCommand;
import seedu.modulesync.command.UnarchiveModuleCommand;
import seedu.modulesync.command.UnarchiveSemesterCommand;
import seedu.modulesync.command.UnmarkCommand;
import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.semester.Semester;
import seedu.modulesync.semester.SemesterBook;
import seedu.modulesync.storage.SemesterStorage;

class ParserTest {

    @Test
    void parse_addCommand_returnsAddTodo() throws ModuleSyncException {
        Parser parser = new Parser();
        assertTrue(parser.parse("add /mod CS2113 /task Week8") instanceof AddTodoCommand);
        assertTrue(parser.parse("add /task Week8 /mod CS2113") instanceof AddTodoCommand);
    }

    @Test
    void parse_missingFields_throws() {
        Parser parser = new Parser();
        assertThrows(ModuleSyncException.class, () -> parser.parse("add /task OnlyTask"));
        assertThrows(ModuleSyncException.class, () -> parser.parse("add"));
    }

    @Test
    void parse_markAndUnmark_returnsCorrectCommands() throws ModuleSyncException {
        Parser parser = new Parser();
        assertTrue(parser.parse("mark 1") instanceof MarkCommand);
        assertTrue(parser.parse("unmark 2") instanceof UnmarkCommand);
    }

    @Test
    void parse_markAndUnmarkInvalidInput_throws() {
        Parser parser = new Parser();
        assertThrows(ModuleSyncException.class, () -> parser.parse("mark"));
        assertThrows(ModuleSyncException.class, () -> parser.parse("unmark"));
        assertThrows(ModuleSyncException.class, () -> parser.parse("mark abc"));
    }

    @Test
    void parse_listNotDoneWithModule_returnsListNotDoneCommand() throws ModuleSyncException {
        Parser parser = new Parser();
        assertTrue(parser.parse("list /notdone /mod CS2113") instanceof ListNotDoneCommand);
        assertTrue(parser.parse("list /mod CS2113 /notdone") instanceof ListNotDoneCommand);
    }

    @Test
    void parse_listNotDoneWithoutModule_throws() {
        Parser parser = new Parser();
        assertThrows(ModuleSyncException.class, () -> parser.parse("list /notdone"));
    }

    @Test
    void parse_listNotDoneTypo_throws() {
        Parser parser = new Parser();
        assertThrows(ModuleSyncException.class, () -> parser.parse("list /notdonee /mod CS2113"));
    }

    @Test
    void parse_moduleListAndSemesterStats_returnsCorrectCommands(@TempDir Path tempDir) throws ModuleSyncException {
        Parser parser = new Parser();
        assertTrue(parser.parse("module list") instanceof ListModulesCommand);

        SemesterBook semesterBook = new SemesterBook();
        semesterBook.addSemester(new Semester("AY2526-S2", false));
        semesterBook.setCurrentSemester("AY2526-S2");
        SemesterStorage storage = new SemesterStorage(tempDir.resolve("semesters.txt"));
        Parser semesterParser = new Parser(semesterBook, storage);
        assertTrue(semesterParser.parse("semester stats") instanceof SemesterStatsCommand);
    }

    @Test
    void parse_moduleArchive_returnsArchiveModuleCommand() throws ModuleSyncException {
        Parser parser = new Parser();
        assertTrue(parser.parse("module archive /mod CS2113") instanceof ArchiveModuleCommand);
        assertTrue(parser.parse("module archive /mod CS3243") instanceof ArchiveModuleCommand);
    }

    @Test
    void parse_moduleUnarchive_returnsUnarchiveModuleCommand() throws ModuleSyncException {
        Parser parser = new Parser();
        assertTrue(parser.parse("module unarchive /mod CS2113") instanceof UnarchiveModuleCommand);
        assertTrue(parser.parse("module unarchive /mod CS3243") instanceof UnarchiveModuleCommand);
    }

    @Test
    void parse_moduleArchiveInvalidFormat_throws() {
        Parser parser = new Parser();
        assertThrows(ModuleSyncException.class, () -> parser.parse("module archive"));
        assertThrows(ModuleSyncException.class, () -> parser.parse("module archive /mod"));
        assertThrows(ModuleSyncException.class, () -> parser.parse("module unarchive"));
    }

    @Test
    void parse_semesterNew_returnsNewSemesterCommand(@TempDir Path tempDir) throws ModuleSyncException {
        SemesterBook semesterBook = new SemesterBook();
        SemesterStorage storage = new SemesterStorage(tempDir.resolve("semesters.txt"));
        Parser parser = new Parser(semesterBook, storage);
        
        assertTrue(parser.parse("semester new AY2526-S2") instanceof NewSemesterCommand);
        assertTrue(parser.parse("semester new AY2627-S1") instanceof NewSemesterCommand);
    }

    @Test
    void parse_semesterArchiveAndUnarchive_returnsCorrectCommands(@TempDir Path tempDir) throws ModuleSyncException {
        SemesterBook semesterBook = new SemesterBook();
        semesterBook.addSemester(new Semester("AY2526-S2", false));
        semesterBook.setCurrentSemester("AY2526-S2");

        SemesterStorage storage = new SemesterStorage(tempDir);
        Parser parser = new Parser(semesterBook, storage);

        assertTrue(parser.parse("semester archive") instanceof ArchiveSemesterCommand);
        assertTrue(parser.parse("semester unarchive") instanceof UnarchiveSemesterCommand);
    }

    @Test
    void parse_semesterNewInvalidFormat_throws(@TempDir Path tempDir) {
        SemesterBook semesterBook = new SemesterBook();
        SemesterStorage storage = new SemesterStorage(tempDir.resolve("semesters.txt"));
        Parser parser = new Parser(semesterBook, storage);
        
        assertThrows(ModuleSyncException.class, () -> parser.parse("semester new"));
        assertThrows(ModuleSyncException.class, () -> parser.parse("semester new "));
    }

    @Test
    void parse_gradeCommand_returnsGradeCommand() throws ModuleSyncException {
        Parser parser = new Parser();
        assertTrue(parser.parse("grade /mod CS2113 /grade A+") instanceof GradeCommand);
        assertTrue(parser.parse("grade /grade B /mod CS3243") instanceof GradeCommand);
        assertTrue(parser.parse("grade /mod CS2113 /grade CS") instanceof GradeCommand);
        assertTrue(parser.parse("grade /mod CS2113 /grade CU") instanceof GradeCommand);
        assertTrue(parser.parse("grade /mod CS2113 /grade S") instanceof GradeCommand);
        assertTrue(parser.parse("grade /mod CS2113 /grade U") instanceof GradeCommand);
    }

    @Test
    void parse_gradeInvalidFormat_throws() {
        Parser parser = new Parser();
        assertThrows(ModuleSyncException.class, () -> parser.parse("grade /mod CS2113"));
        assertThrows(ModuleSyncException.class, () -> parser.parse("grade /grade A+"));
        assertThrows(ModuleSyncException.class, () -> parser.parse("grade"));
        assertThrows(ModuleSyncException.class, () -> parser.parse("grade /mod"));
        assertThrows(ModuleSyncException.class, () -> parser.parse("grade /grade"));
    }
    @Test
    void parse_moduleAddAndModuleDelete_returnsCorrectCommands() throws ModuleSyncException {
        Parser parser = new Parser();
        assertTrue(parser.parse("add /mod CS1010S") instanceof AddModuleCommand);
        assertTrue(parser.parse("delete module /mod CS1010S") instanceof DeleteModuleCommand);
    }

    @Test
    void parse_moduleAddAndModuleDeleteInvalidFormat_throws() {
        Parser parser = new Parser();
        // missing task/flags throws usage correctly, but bad module code throws exception
        assertThrows(ModuleSyncException.class, () -> parser.parse("add /mod !@#$"));
        
        assertThrows(ModuleSyncException.class, () -> parser.parse("delete module /mod"));
        assertThrows(ModuleSyncException.class, () -> parser.parse("delete module /mod !@#$"));
    }

    @Test
    void parse_setCredits_returnsSetCreditsCommand() throws ModuleSyncException {
        Parser parser = new Parser();
        assertTrue(parser.parse("setcredits /mod CS1010S /mc 4") instanceof SetCreditsCommand);
        assertTrue(parser.parse("setcredits /mod CS1010S /mc 0") instanceof SetCreditsCommand);
        assertTrue(parser.parse("setcredits /mod CS1010S /mc 40") instanceof SetCreditsCommand);
    }

    @Test
    void parse_setCreditsBoundsAndFormat_throws() {
        Parser parser = new Parser();
        
        // Exceeds upper bounds
        assertThrows(ModuleSyncException.class, () -> parser.parse("setcredits /mod CS1010S /mc 41"));
        
        // Exceeds lower bounds
        assertThrows(ModuleSyncException.class, () -> parser.parse("setcredits /mod CS1010S /mc -1"));
        
        // Invalid string representation
        assertThrows(ModuleSyncException.class, () -> parser.parse("setcredits /mod CS1010S /mc four"));
        
        // Missing arguments entirely
        assertThrows(ModuleSyncException.class, () -> parser.parse("setcredits /mod CS1010S"));
        assertThrows(ModuleSyncException.class, () -> parser.parse("setcredits /mc 4"));
    }
}

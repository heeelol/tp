package seedu.modulesync.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import seedu.modulesync.command.AddTodoCommand;
import seedu.modulesync.command.ListNotDoneCommand;
import seedu.modulesync.command.ListModulesCommand;
import seedu.modulesync.command.MarkCommand;
import seedu.modulesync.command.SemesterStatsCommand;
import seedu.modulesync.command.UnmarkCommand;
import seedu.modulesync.exception.ModuleSyncException;

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
        assertThrows(ModuleSyncException.class, () -> parser.parse("add /mod CS2113"));
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
    void parse_modulesAndSemesterStats_returnsCorrectCommands() throws ModuleSyncException {
        Parser parser = new Parser();
        assertTrue(parser.parse("modules") instanceof ListModulesCommand);
        assertTrue(parser.parse("semesterstats") instanceof SemesterStatsCommand);
    }
}

package seedu.modulesync.others;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.modulesync.ModuleSync;
import seedu.modulesync.command.SwitchSemesterCommand;
import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.Module;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.parser.Parser;
import seedu.modulesync.semester.Semester;
import seedu.modulesync.semester.SemesterBook;
import seedu.modulesync.storage.SemesterStorage;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

class SemesterReadOnlyViewFeatureTest {

    static class StorageStub extends Storage {
        private boolean isSaved;

        StorageStub(Path path) {
            super(path);
        }

        @Override
        public void save(ModuleBook moduleBook) {
            isSaved = true;
        }

        boolean isSaved() {
            return isSaved;
        }
    }

    @Test
    void parse_semesterSwitch_returnsSwitchSemesterCommand(@TempDir Path tempDir) throws ModuleSyncException {
        SemesterBook semesterBook = new SemesterBook();
        SemesterStorage semesterStorage = new SemesterStorage(tempDir);
        Parser parser = new Parser(semesterBook, semesterStorage);

        assertTrue(parser.parse("semester switch AY2525-S1") instanceof SwitchSemesterCommand);
    }

    @Test
    void execute_switchToArchivedSemester_updatesPointerAndShowsReadOnlyMessage(@TempDir Path tempDir)
            throws Exception {
        SemesterBook semesterBook = createSemesterBookForReadOnlyView();
        SemesterStorage semesterStorage = new SemesterStorage(tempDir);
        Parser parser = new Parser(semesterBook, semesterStorage);
        Ui ui = new Ui(new Scanner(new ByteArrayInputStream(new byte[0])));
        StorageStub storageStub = new StorageStub(tempDir.resolve("unused.txt"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            parser.parse("semester switch AY2525-S1")
                    .execute(semesterBook.getCurrentModuleBook(), storageStub, ui);
        } finally {
            System.setOut(originalOut);
        }

        String actual = output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        String pointerFileContents = Files.readString(tempDir.resolve("current.txt"), StandardCharsets.UTF_8)
                .replace("\r\n", "\n");

        assertEquals("Now viewing AY2525-S1 [read-only]. Use 'semester switch AY2525-S2' to return.\n", actual);
        assertEquals("AY2525-S1\n", pointerFileContents);
        assertEquals("AY2525-S1", semesterBook.getCurrentSemesterName());
        assertFalse(storageStub.isSaved());
    }

    @Test
    void run_switchToArchivedSemester_allowsReferenceAndBlocksMutation(@TempDir Path tempDir) throws Exception {
        SemesterBook semesterBook = createSemesterBookForReadOnlyView();
        SemesterStorage semesterStorage = new SemesterStorage(tempDir);
        Parser parser = new Parser(semesterBook, semesterStorage);
        String commands = "semester switch AY2525-S1\nlist\ngrades list\nadd /mod CS2113 /task New task\nbye\n";
        Ui ui = new Ui(new Scanner(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8))));
        ModuleSync moduleSync = createModuleSync(semesterBook, semesterStorage, parser, ui);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            moduleSync.run();
        } finally {
            System.setOut(originalOut);
        }

        String actual = output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");

        assertTrue(actual.contains("Now viewing AY2525-S1 [read-only]. Use 'semester switch AY2525-S2' to return.\n"));
        assertTrue(actual.contains("Here are the tasks:\n1.[CS2113] [T][ ] Past checkpoint [Priority: 0]\n"));
        assertFalse(actual.contains("Current semester task"));
        assertTrue(actual.contains("AY2525-S1 Results (Archived)\n"));
        assertTrue(actual.contains("Error: Semester 'AY2525-S1' is archived and read-only."));
        assertFalse(actual.contains("Added task under CS2113:"));
    }

    @Test
    void run_archiveCurrentSemester_blocksMutation(@TempDir Path tempDir) throws Exception {
        SemesterBook semesterBook = new SemesterBook();
        Semester activeSemester = new Semester("AY2526-S2", false);
        activeSemester.getModuleBook().getOrCreate("CS2113").addTodo("Before archive");
        semesterBook.addSemester(activeSemester);
        semesterBook.setCurrentSemester("AY2526-S2");

        SemesterStorage semesterStorage = new SemesterStorage(tempDir);
        Parser parser = new Parser(semesterBook, semesterStorage);
        String commands = "semester archive\nadd /mod CS2113 /task After archive\nbye\n";
        Ui ui = new Ui(new Scanner(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8))));
        ModuleSync moduleSync = createModuleSync(semesterBook, semesterStorage, parser, ui);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            moduleSync.run();
        } finally {
            System.setOut(originalOut);
        }

        String actual = output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");

        assertTrue(actual.contains("Semester 'AY2526-S2' has been archived."));
        assertTrue(actual.contains("Error: Semester 'AY2526-S2' is archived and read-only."));
        assertFalse(actual.contains("Added task under CS2113:"));
    }

    /**
     * Creates a semester book with one archived semester and one active semester.
     *
     * @return a semester book with one archived semester and one active semester
     * @throws ModuleSyncException if the current semester cannot be set
     */
    private SemesterBook createSemesterBookForReadOnlyView() throws ModuleSyncException {
        SemesterBook semesterBook = new SemesterBook();
        Semester archivedSemester = new Semester("AY2525-S1", true);
        Semester currentSemester = new Semester("AY2525-S2", false);

        archivedSemester.getModuleBook().getOrCreate("CS2113").addTodo("Past checkpoint");
        assignGrade(archivedSemester, "CS2113", "A", 4);
        currentSemester.getModuleBook().getOrCreate("CS2100").addTodo("Current semester task");

        semesterBook.addSemester(archivedSemester);
        semesterBook.addSemester(currentSemester);
        semesterBook.setCurrentSemester("AY2525-S2");
        return semesterBook;
    }

    /**
     * Assigns a recorded grade to the given module inside the given semester.
     *
     * @param semester the semester containing the module
     * @param moduleCode the module code to update
     * @param grade the recorded grade
     * @param credits the module credits
     */
    private void assignGrade(Semester semester, String moduleCode, String grade, int credits) {
        Module module = semester.getModuleBook().getOrCreate(moduleCode);
        module.setGrade(grade);
        module.setCredits(credits);
    }

    /**
     * Creates a ModuleSync instance from its package-private constructor.
     *
     * @param semesterBook the semester book to inject
     * @param semesterStorage the semester storage to inject
     * @param parser the parser to inject
     * @param ui the UI to inject
     * @return the constructed ModuleSync instance
     * @throws Exception if reflection fails
     */
    private ModuleSync createModuleSync(SemesterBook semesterBook, SemesterStorage semesterStorage,
                                        Parser parser, Ui ui) throws Exception {
        Constructor<ModuleSync> constructor = ModuleSync.class.getDeclaredConstructor(
                SemesterBook.class,
                SemesterStorage.class,
                Parser.class,
                Ui.class);
        constructor.setAccessible(true);
        return constructor.newInstance(semesterBook, semesterStorage, parser, ui);
    }
}

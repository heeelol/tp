package seedu.modulesync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.parser.Parser;
import seedu.modulesync.semester.Semester;
import seedu.modulesync.semester.SemesterBook;
import seedu.modulesync.storage.SemesterStorage;
import seedu.modulesync.ui.Ui;

class ModuleSyncStartupWarningTest {

    @Test
    void run_withOverdueTasks_printsStartupWarningUsingGlobalTaskNumbers(@TempDir Path tempDir)
            throws ModuleSyncException {
        SemesterBook semesterBook = new SemesterBook();
        Semester semester = new Semester("default", false);
        semester.getModuleBook().getOrCreate("CS9999")
                .addTodo("Reference task");
        semester.getModuleBook().getOrCreate("CS2113")
                .addDeadline("Project checkpoint", LocalDateTime.now().minusHours(5));
        semester.getModuleBook().getOrCreate("CS2100")
                .addDeadline("Quiz", LocalDateTime.now().minusDays(2));
        semesterBook.addSemester(semester);
        semesterBook.setCurrentSemester("default");

        String actual = runApplication(tempDir, semesterBook);
        String expectedPrefix = "Welcome to ModuleSync\n"
                + "What would you like to do?\n"
                + "Active semester: default\n"
                + "Overdue warning: 2 task(s) have passed their deadlines.\n";

        assertEquals(expectedPrefix, actual.substring(0, expectedPrefix.length()));
        int projectIndex = actual.indexOf("2.[CS2113] [D][ ] Project checkpoint (was due: ");
        int quizIndex = actual.indexOf("3.[CS2100] [D][ ] Quiz (was due: ");
        assertTrue(projectIndex >= 0);
        assertTrue(quizIndex >= 0);
        assertTrue(projectIndex < quizIndex);
    }

    @Test
    void run_withoutOverdueTasks_doesNotPrintStartupWarning(@TempDir Path tempDir) throws ModuleSyncException {
        SemesterBook semesterBook = new SemesterBook();
        Semester semester = new Semester("default", false);
        semester.getModuleBook().getOrCreate("CS2113")
                .addDeadline("Project checkpoint", LocalDateTime.now().plusDays(2));
        semesterBook.addSemester(semester);
        semesterBook.setCurrentSemester("default");

        String actual = runApplication(tempDir, semesterBook);
        assertFalse(actual.contains("Overdue warning:"));
    }

    @Test
    void run_withCompletedOverdueTask_doesNotPrintStartupWarning(@TempDir Path tempDir) throws ModuleSyncException {
        SemesterBook semesterBook = new SemesterBook();
        Semester semester = new Semester("default", false);
        semester.getModuleBook().getOrCreate("CS2113")
                .addDeadline("Project checkpoint", LocalDateTime.now().minusDays(1))
                .markDone();
        semesterBook.addSemester(semester);
        semesterBook.setCurrentSemester("default");

        String actual = runApplication(tempDir, semesterBook);
        assertFalse(actual.contains("Overdue warning:"));
        assertFalse(actual.contains("Project checkpoint"));
    }

    @Test
    void run_withoutActiveSemester_doesNotPrintStartupWarning(@TempDir Path tempDir) throws ModuleSyncException {
        SemesterBook semesterBook = new SemesterBook();
        semesterBook.addSemester(new Semester("default", false));
        SemesterStorage semesterStorage = new SemesterStorage(tempDir);
        Parser parser = new Parser(semesterBook, semesterStorage);
        Ui ui = new Ui(new Scanner(new ByteArrayInputStream(new byte[0])));
        ModuleSync moduleSync = new ModuleSync(semesterBook, semesterStorage, parser, ui);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            ui.showCurrentSemester(semesterBook);
            moduleSync.showStartupWarnings();
        } finally {
            System.setOut(originalOut);
        }

        String actual = output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        assertTrue(actual.contains("No active semester. Use 'semester switch SEMESTER_NAME' to begin.\n"));
        assertFalse(actual.contains("Overdue warning:"));
    }

    /**
     * Runs the application once with an injected semester book and returns the printed output.
     *
     * @param tempDir the temporary storage directory
     * @param semesterBook the semester book used for the run
     * @return the printed console output
     * @throws ModuleSyncException if the test setup is invalid
     */
    private String runApplication(Path tempDir, SemesterBook semesterBook) throws ModuleSyncException {
        SemesterStorage semesterStorage = new SemesterStorage(tempDir);
        Parser parser = new Parser(semesterBook, semesterStorage);
        Ui ui = new Ui(new Scanner(new ByteArrayInputStream("bye\n".getBytes(StandardCharsets.UTF_8))));
        ModuleSync moduleSync = new ModuleSync(semesterBook, semesterStorage, parser, ui);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            moduleSync.run();
        } finally {
            System.setOut(originalOut);
        }

        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}

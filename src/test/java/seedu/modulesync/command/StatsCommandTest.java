package seedu.modulesync.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.task.Task;
import seedu.modulesync.ui.Ui;

class StatsCommandTest {

    /** Captures the arguments passed to showModuleStats for assertion. */
    static class CapturingUi extends Ui {
        int total;
        int completedOnTime;
        int completedLate;
        int active;
        double avgDays;

        CapturingUi() {
            super(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));
        }

        @Override
        public void showModuleStats(String moduleCode, int total, int completedOnTime,
                                    int completedLate, int active, double avgDaysBeforeDeadline) {
            this.total = total;
            this.completedOnTime = completedOnTime;
            this.completedLate = completedLate;
            this.active = active;
            this.avgDays = avgDaysBeforeDeadline;
        }
    }

    static class NoOpStorage extends Storage {
        NoOpStorage(Path path) {
            super(path);
        }
    }

    @Test
    void execute_allTodosNotDone_allActive(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        moduleBook.getOrCreate("CS2113").addTodo("Task A");
        moduleBook.getOrCreate("CS2113").addTodo("Task B");

        CapturingUi ui = new CapturingUi();
        new StatsCommand("CS2113").execute(moduleBook, new NoOpStorage(tempDir.resolve("x.txt")), ui);

        assertEquals(2, ui.total);
        assertEquals(0, ui.completedOnTime);
        assertEquals(0, ui.completedLate);
        assertEquals(2, ui.active);
    }

    @Test
    void execute_todosMarkedDone_countedAsOnTime(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        Task t = moduleBook.getOrCreate("CS2113").addTodo("Task A");
        t.markDone();

        CapturingUi ui = new CapturingUi();
        new StatsCommand("CS2113").execute(moduleBook, new NoOpStorage(tempDir.resolve("x.txt")), ui);

        assertEquals(1, ui.total);
        assertEquals(1, ui.completedOnTime);
        assertEquals(0, ui.completedLate);
        assertEquals(0, ui.active);
    }

    @Test
    void execute_deadlineDoneBeforeDue_countedOnTime(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        LocalDateTime farFuture = LocalDateTime.now().plusDays(30);
        Task t = moduleBook.getOrCreate("CS2113").addDeadline("Submit report", farFuture);
        t.markDone(); // completedAt = now, deadline = +30d → on time

        CapturingUi ui = new CapturingUi();
        new StatsCommand("CS2113").execute(moduleBook, new NoOpStorage(tempDir.resolve("x.txt")), ui);

        assertEquals(1, ui.completedOnTime);
        assertEquals(0, ui.completedLate);
    }

    @Test
    void execute_deadlineDoneAfterDue_countedLate(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        LocalDateTime pastDeadline = LocalDateTime.now().minusDays(5);
        Task t = moduleBook.getOrCreate("CS2113").addDeadline("Late submission", pastDeadline);
        t.markDone(); // completedAt = now, deadline = -5d → late

        CapturingUi ui = new CapturingUi();
        new StatsCommand("CS2113").execute(moduleBook, new NoOpStorage(tempDir.resolve("x.txt")), ui);

        assertEquals(0, ui.completedOnTime);
        assertEquals(1, ui.completedLate);
    }

    @Test
    void execute_todoOnlyModule_avgDaysIsNaN(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        Task t = moduleBook.getOrCreate("CS2113").addTodo("No deadline task");
        t.markDone();

        CapturingUi ui = new CapturingUi();
        new StatsCommand("CS2113").execute(moduleBook, new NoOpStorage(tempDir.resolve("x.txt")), ui);

        assertTrue(Double.isNaN(ui.avgDays));
    }

    @Test
    void execute_emptyModule_allZeroStats(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        moduleBook.getOrCreate("CS2113"); // empty module

        CapturingUi ui = new CapturingUi();
        new StatsCommand("CS2113").execute(moduleBook, new NoOpStorage(tempDir.resolve("x.txt")), ui);

        assertEquals(0, ui.total);
        assertEquals(0, ui.completedOnTime);
        assertEquals(0, ui.completedLate);
        assertEquals(0, ui.active);
    }

    @Test
    void execute_nonExistentModule_throwsModuleSyncException(@TempDir Path tempDir) {
        ModuleBook moduleBook = new ModuleBook();
        CapturingUi ui = new CapturingUi();

        assertThrows(ModuleSyncException.class,
                () -> new StatsCommand("CS9999").execute(moduleBook, new NoOpStorage(tempDir.resolve("x.txt")), ui));
    }
}

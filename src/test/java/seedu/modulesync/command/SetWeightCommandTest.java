package seedu.modulesync.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.task.Task;
import seedu.modulesync.ui.Ui;

class SetWeightCommandTest {

    static class TestStorage extends Storage {
        boolean saved;

        TestStorage(Path path) {
            super(path);
        }

        @Override
        public void save(ModuleBook moduleBook) {
            saved = true;
        }
    }

    static class TestUi extends Ui {
        Task lastTask;
        int lastTaskNumber;
        Integer lastPrevious;

        TestUi() {
            super(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));
        }

        @Override
        public void showWeightSet(Task task, int taskNumber, Integer previous) {
            this.lastTask = task;
            this.lastTaskNumber = taskNumber;
            this.lastPrevious = previous;
        }
    }

    private ModuleBook bookWithOneTodo(String moduleCode, String description) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        moduleBook.getOrCreate(moduleCode).addTodo(description);
        return moduleBook;
    }

    @Test
    void execute_setsWeightageOnTask(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = bookWithOneTodo("CS2113", "Assignment 1");
        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        TestUi ui = new TestUi();

        new SetWeightCommand(1, 30).execute(moduleBook, storage, ui);

        Task task = moduleBook.getTaskByDisplayIndex(1);
        assertTrue(task.hasWeightage());
        assertEquals(30, task.getWeightage());
        assertTrue(storage.saved);
    }

    @Test
    void execute_updatesExistingWeightage(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = bookWithOneTodo("CS2113", "Assignment 1");
        moduleBook.getTaskByDisplayIndex(1).setWeightage(20);
        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        TestUi ui = new TestUi();

        new SetWeightCommand(1, 50).execute(moduleBook, storage, ui);

        assertEquals(50, moduleBook.getTaskByDisplayIndex(1).getWeightage());
        assertEquals(20, ui.lastPrevious);
    }

    @Test
    void execute_previousIsNullWhenNoWeightBefore(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = bookWithOneTodo("CS2113", "Lab 1");
        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        TestUi ui = new TestUi();

        new SetWeightCommand(1, 10).execute(moduleBook, storage, ui);

        assertNull(ui.lastPrevious);
    }

    @Test
    void execute_invalidIndex_throwsModuleSyncException(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = bookWithOneTodo("CS2113", "Assignment 1");
        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        TestUi ui = new TestUi();

        assertThrows(ModuleSyncException.class,
                () -> new SetWeightCommand(99, 10).execute(moduleBook, storage, ui));
    }

    @Test
    void execute_boundaryWeightage_zeroAccepted(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = bookWithOneTodo("CS2113", "Optional task");
        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        TestUi ui = new TestUi();

        new SetWeightCommand(1, 0).execute(moduleBook, storage, ui);

        assertEquals(0, moduleBook.getTaskByDisplayIndex(1).getWeightage());
    }

    @Test
    void execute_boundaryWeightage_hundredAccepted(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = bookWithOneTodo("CS2113", "Final exam");
        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        TestUi ui = new TestUi();

        new SetWeightCommand(1, 100).execute(moduleBook, storage, ui);

        assertEquals(100, moduleBook.getTaskByDisplayIndex(1).getWeightage());
    }
}

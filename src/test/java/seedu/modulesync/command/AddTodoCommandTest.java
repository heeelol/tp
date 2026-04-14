package seedu.modulesync.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

class AddTodoCommandTest {

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
        TestUi() {
            super(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));
        }

        @Override
        public void showTaskAdded(seedu.modulesync.module.Module module, seedu.modulesync.task.Task task,
                                  int totalTasks) {
            // no-op to keep test output clean
        }
    }

    @Test
    void execute_addsTaskAndSaves(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        TestUi ui = new TestUi();

        AddTodoCommand command = new AddTodoCommand("CS2113", "Week8");
        command.execute(moduleBook, storage, ui);

        assertEquals(1, moduleBook.countTotalTasks());
        assertTrue(storage.saved);
        var tasks = moduleBook.getModules().iterator().next().getTasks().asUnmodifiableList();
        assertEquals("Week8", tasks.get(0).getDescription());
        assertNull(tasks.get(0).getWeightage());
    }

    @Test
    void execute_addsTaskWithWeightageAndSaves(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        TestUi ui = new TestUi();

        AddTodoCommand command = new AddTodoCommand("CS2113", "Final Project", 40);
        command.execute(moduleBook, storage, ui);

        var tasks = moduleBook.getModules().iterator().next().getTasks().asUnmodifiableList();
        assertEquals(40, tasks.get(0).getWeightage());
        assertTrue(storage.saved);
    }

    @Test
    void execute_addsTaskWithBoundaryWeightages(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        TestUi ui = new TestUi();

        new AddTodoCommand("CS2113", "Zero weight task", 0).execute(moduleBook, storage, ui);
        new AddTodoCommand("CS2113", "Full weight task", 100).execute(moduleBook, storage, ui);

        var tasks = moduleBook.getModules().iterator().next().getTasks().asUnmodifiableList();
        assertEquals(0, tasks.get(0).getWeightage());
        assertEquals(100, tasks.get(1).getWeightage());
    }

    @Test
    void isMutating_returnsTrue() {
        assertTrue(new AddTodoCommand("CS2113", "Task").isMutating());
    }
}

package seedu.duke.command;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.duke.exception.ModuleSyncException;
import seedu.duke.module.ModuleBook;
import seedu.duke.storage.Storage;
import seedu.duke.ui.Ui;

class MarkCommandTest {

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
        public void showTaskMarked(seedu.duke.task.Task task, int taskNumber) {
            // no-op to keep test output clean
        }
    }

    @Test
    void execute_marksTaskAndSaves(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        moduleBook.getOrCreate("CS2113").addTodo("Week8");
        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        TestUi ui = new TestUi();

        MarkCommand command = new MarkCommand(1);
        command.execute(moduleBook, storage, ui);

        var task = moduleBook.getOrCreate("CS2113").getTasks().asUnmodifiableList().get(0);
        assertTrue(task.isDone());
        assertTrue(storage.saved);
    }
}

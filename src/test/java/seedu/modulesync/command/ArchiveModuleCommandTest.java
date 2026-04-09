package seedu.modulesync.command;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

class ArchiveModuleCommandTest {

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
        String message;

        TestUi() {
            super(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));
        }

        @Override
        public void showMessage(String message) {
            this.message = message;
        }
    }

    @Test
    void execute_archivesModule(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        // Create a module with some tasks
        moduleBook.getOrCreate("CS2113").addTodo("Test task");

        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        TestUi ui = new TestUi();

        ArchiveModuleCommand command = new ArchiveModuleCommand("CS2113");
        command.execute(moduleBook, storage, ui);

        assertTrue(moduleBook.getModule("CS2113").isArchived());
        assertTrue(storage.saved);
        assertTrue(ui.message.contains("archived"));
    }

    @Test
    void execute_archiveAlreadyArchivedModule_throws(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        var module = moduleBook.getOrCreate("CS2113");
        module.setArchived(true);

        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        TestUi ui = new TestUi();

        ArchiveModuleCommand command = new ArchiveModuleCommand("CS2113");
        assertThrows(ModuleSyncException.class, () -> command.execute(moduleBook, storage, ui));
    }

    @Test
    void execute_archiveNonexistentModule_throws(@TempDir Path tempDir) {
        ModuleBook moduleBook = new ModuleBook();
        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        TestUi ui = new TestUi();

        ArchiveModuleCommand command = new ArchiveModuleCommand("NONEXISTENT");
        assertThrows(ModuleSyncException.class, () -> command.execute(moduleBook, storage, ui));
    }
}

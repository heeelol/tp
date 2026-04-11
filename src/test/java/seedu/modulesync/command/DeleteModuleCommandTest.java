package seedu.modulesync.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

class DeleteModuleCommandTest {

    @Test
    void execute_existingModule_moduleDeletedSuccessfully(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        Storage storage = new Storage(tempDir.resolve("data.txt"));
        Ui ui = new Ui(new java.util.Scanner(new java.io.ByteArrayInputStream(new byte[0])));
        
        // Manually place the module inside the book first
        moduleBook.getOrCreate("CS1010S");
        assertEquals(1, moduleBook.getModules().size());

        DeleteModuleCommand delCmd = new DeleteModuleCommand("CS1010S");
        delCmd.execute(moduleBook, storage, ui);
        
        assertNull(moduleBook.getModule("CS1010S"));
        assertEquals(0, moduleBook.getModules().size());
    }

    @Test
    void execute_nonExistingModule_throwsException(@TempDir Path tempDir) {
        ModuleBook moduleBook = new ModuleBook();
        Storage storage = new Storage(tempDir.resolve("data.txt"));
        Ui ui = new Ui(new java.util.Scanner(new java.io.ByteArrayInputStream(new byte[0])));
        
        DeleteModuleCommand delCmd = new DeleteModuleCommand("CS2113");
        
        ModuleSyncException exception = assertThrows(ModuleSyncException.class, () -> 
                delCmd.execute(moduleBook, storage, ui));
        
        assertEquals("Module CS2113 does not exist.", exception.getMessage());
    }
}

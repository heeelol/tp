package seedu.modulesync.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

class AddModuleCommandTest {

    @Test
    void execute_validModule_moduleCreatedSuccessfully(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        Storage storage = new Storage(tempDir.resolve("data.txt"));
        Ui ui = new Ui(new java.util.Scanner(new java.io.ByteArrayInputStream(new byte[0])));
        
        AddModuleCommand addCmd = new AddModuleCommand("CS1010S");
        addCmd.execute(moduleBook, storage, ui);
        
        assertNotNull(moduleBook.getModule("CS1010S"));
        assertEquals(1, moduleBook.getModules().size());
    }

    @Test
    void execute_duplicateModule_noErrorAndMaintainsSingleInstance(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        Storage storage = new Storage(tempDir.resolve("data.txt"));
        Ui ui = new Ui(new java.util.Scanner(new java.io.ByteArrayInputStream(new byte[0])));
        
        AddModuleCommand addCmd = new AddModuleCommand("CS1010S");
        addCmd.execute(moduleBook, storage, ui);
        
        // Add exact same module
        AddModuleCommand addCmdDuplicate = new AddModuleCommand("CS1010S");
        addCmdDuplicate.execute(moduleBook, storage, ui);
        
        assertNotNull(moduleBook.getModule("CS1010S"));
        assertEquals(1, moduleBook.getModules().size());
    }
}

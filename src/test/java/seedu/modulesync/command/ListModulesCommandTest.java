package seedu.modulesync.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

class ListModulesCommandTest {

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

    @Test
    void execute_printsModuleListWithoutSaving(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        moduleBook.getOrCreate("CS2113").addTodo("Week8");
        moduleBook.getOrCreate("CS2100").addTodo("Tutorial");

        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        Ui ui = new Ui(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            new ListModulesCommand().execute(moduleBook, storage, ui);
        } finally {
            System.setOut(originalOut);
        }

        String actual = output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        String expected = "Here are your registered modules:\n"
                + "1. CS2113 (1 task(s))\n"
                + "2. CS2100 (1 task(s))\n";

        assertEquals(expected, actual);
        assertFalse(storage.saved);
    }
}

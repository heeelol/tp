package seedu.modulesync.command;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

class HelpCommandTest {

    static class NoOpStorage extends Storage {
        NoOpStorage(Path path) {
            super(path);
        }

        @Override
        public void save(ModuleBook moduleBook) {
            // no-op
        }
    }

    @Test
    void isMutating_returnsFalse() {
        assertFalse(new HelpCommand().isMutating());
    }

    @Test
    void execute_doesNotThrow(@TempDir Path tempDir) {
        ModuleBook moduleBook = new ModuleBook();
        NoOpStorage storage = new NoOpStorage(tempDir.resolve("modules.txt"));
        Ui ui = new Ui(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));

        assertDoesNotThrow(() -> new HelpCommand().execute(moduleBook, storage, ui));
    }

    @Test
    void execute_outputContainsKeyCommands(@TempDir Path tempDir) {
        ModuleBook moduleBook = new ModuleBook();
        NoOpStorage storage = new NoOpStorage(tempDir.resolve("modules.txt"));
        Ui ui = new Ui(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            new HelpCommand().execute(moduleBook, storage, ui);
        } finally {
            System.setOut(originalOut);
        }

        String actual = output.toString(StandardCharsets.UTF_8);

        // Verify key command categories and representative commands are present
        assertTrue(actual.contains("add /mod"), "Should contain add /mod command");
        assertTrue(actual.contains("delete"), "Should contain delete command");
        assertTrue(actual.contains("mark"), "Should contain mark command");
        assertTrue(actual.contains("setweight"), "Should contain setweight command");
        assertTrue(actual.contains("list /top"), "Should contain list /top command");
        assertTrue(actual.contains("semester new"), "Should contain semester new command");
        assertTrue(actual.contains("semester archive"), "Should contain semester archive command");
        assertTrue(actual.contains("grade /mod"), "Should contain grade command");
        assertTrue(actual.contains("cap"), "Should contain cap command");
        assertTrue(actual.contains("help"), "Should contain help command");
        assertTrue(actual.contains("bye"), "Should contain bye command");
    }

    @Test
    void execute_outputContainsPercentNote(@TempDir Path tempDir) {
        ModuleBook moduleBook = new ModuleBook();
        NoOpStorage storage = new NoOpStorage(tempDir.resolve("modules.txt"));
        Ui ui = new Ui(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            new HelpCommand().execute(moduleBook, storage, ui);
        } finally {
            System.setOut(originalOut);
        }

        String actual = output.toString(StandardCharsets.UTF_8);
        // Weightage commands should note that no % symbol is needed
        assertTrue(actual.contains("no % symbol"), "Help text should clarify that % symbol is not needed");
    }
}

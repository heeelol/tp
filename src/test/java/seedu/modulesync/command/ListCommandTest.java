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

class ListCommandTest {

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
    void execute_printsTaskListWithoutSaving(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        Ui ui = new Ui(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));

        moduleBook.getOrCreate("CS2113").addTodo("Week8");
        moduleBook.getOrCreate("CS2100").addTodo("Tutorial").markDone();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            ListCommand command = new ListCommand();
            command.execute(moduleBook, storage, ui);
        } finally {
            System.setOut(originalOut);
        }

        String actual = output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        String expected = "Here are the tasks:\n"
                + "1.[CS2113] [T][ ] Week8 [Priority: 0]\n"
                + "2.[CS2100] [T][X] Tutorial [Priority: 0]\n";

        assertEquals(expected, actual);
        assertFalse(storage.saved);
    }

    @Test
    void execute_notDoneForModule_printsFilteredListWithoutSaving(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        Ui ui = new Ui(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));

        moduleBook.getOrCreate("CS2113").addTodo("Week8");
        moduleBook.getOrCreate("CS2113").addTodo("Week9").markDone();
        moduleBook.getOrCreate("CS2100").addTodo("Tutorial");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            ListNotDoneCommand command = new ListNotDoneCommand("CS2113");
            command.execute(moduleBook, storage, ui);
        } finally {
            System.setOut(originalOut);
        }

        String actual = output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        String expected = "Here are the not done tasks for CS2113:\n"
                + "1.[CS2113] [T][ ] Week8 [Priority: 0]\n";

        assertEquals(expected, actual);
        assertFalse(storage.saved);
    }

    @Test
    void execute_withModuleCode_printsOnlyThatModuleTasksWithoutSaving(@TempDir Path tempDir)
            throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        Ui ui = new Ui(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));

        moduleBook.getOrCreate("CS2100").addTodo("Tutorial").markDone();
        moduleBook.getOrCreate("CS2113").addTodo("Week10");
        moduleBook.getOrCreate("CS2113").addTodo("Quiz");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            ListCommand command = new ListCommand("CS2113");
            command.execute(moduleBook, storage, ui);
        } finally {
            System.setOut(originalOut);
        }

        String actual = output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        String expected = "Here are the tasks for CS2113:\n"
                + "2.[CS2113] [T][ ] Week10 [Priority: 0]\n"
                + "3.[CS2113] [T][ ] Quiz [Priority: 0]\n";

        assertEquals(expected, actual);
        assertFalse(storage.saved);
    }

    @Test
    void execute_withUnknownModuleCode_printsNoSuchModuleMessageWithoutSaving(@TempDir Path tempDir)
            throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        Ui ui = new Ui(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));

        moduleBook.getOrCreate("CS2113").addTodo("Week9");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            ListCommand command = new ListCommand("CS2100");
            command.execute(moduleBook, storage, ui);
        } finally {
            System.setOut(originalOut);
        }

        String actual = output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        String expected = "No such module: CS2100.\n";

        assertEquals(expected, actual);
        assertFalse(storage.saved);
    }
}

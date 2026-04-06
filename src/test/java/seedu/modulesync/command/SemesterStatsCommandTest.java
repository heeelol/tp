package seedu.modulesync.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.task.Task;
import seedu.modulesync.ui.Ui;

class SemesterStatsCommandTest {

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
    void execute_printsSemesterStatsWithoutSaving(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();

        Task t1 = moduleBook.getOrCreate("CS2113").addTodo("Week8", 10);
        Task t2 = moduleBook.getOrCreate("CS2113").addDeadline("Project", LocalDateTime.of(2026, 4, 30, 23, 59), 20);
        t2.markDone();
        moduleBook.getOrCreate("CS2100").addTodo("Tutorial").markDone();

        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        Ui ui = new Ui(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            new SemesterStatsCommand().execute(moduleBook, storage, ui);
        } finally {
            System.setOut(originalOut);
        }

        String actual = output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        String expected = "Semester statistics:\n"
                + "Modules: 2\n"
                + "Tasks: 3 total | 2 done | 1 not done | Completion: 66%\n"
                + "Types: 2 todo(s) | 1 deadline(s)\n"
                + "Weightage: 20/30 completed (66%) across 2 weighted task(s)\n"
                + "Work distribution by module:\n"
                + "1. CS2113: 2 task(s) | done 1 | weightage 20/30\n"
                + "2. CS2100: 1 task(s) | done 1 | weightage n/a\n";

        assertEquals(expected, actual);
        assertFalse(storage.saved);
    }
}

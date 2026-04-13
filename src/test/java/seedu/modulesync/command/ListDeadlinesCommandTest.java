package seedu.modulesync.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import seedu.modulesync.ui.Ui;

class ListDeadlinesCommandTest {

    static class TestStorage extends Storage {
        private boolean saved;

        TestStorage(Path path) {
            super(path);
        }

        @Override
        public void save(ModuleBook moduleBook) {
            saved = true;
        }

        boolean isSaved() {
            return saved;
        }
    }

    @Test
    void execute_mixedUpcomingAndOverdue_upcomingShownBeforeOldOverdue(@TempDir Path tempDir)
            throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        Ui ui = new Ui(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));

        moduleBook.getOrCreate("CS2113").addDeadline("Ancient overdue", LocalDateTime.now().minusYears(2));
        moduleBook.getOrCreate("CS2113").addDeadline("Also old", LocalDateTime.now().minusDays(20));
        moduleBook.getOrCreate("CS2113").addDeadline("Due tomorrow", LocalDateTime.now().plusDays(1));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            new ListDeadlinesCommand().execute(moduleBook, storage, ui);
        } finally {
            System.setOut(originalOut);
        }

        String actual = output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");

        int tomorrowIndex = actual.indexOf("Due tomorrow");
        int oldIndex = actual.indexOf("Also old");
        int ancientIndex = actual.indexOf("Ancient overdue");

        assertTrue(actual.contains("Here are your deadlines (upcoming, due today, then overdue):"));
        assertTrue(tomorrowIndex >= 0);
        assertTrue(oldIndex >= 0);
        assertTrue(ancientIndex >= 0);
        assertTrue(tomorrowIndex < oldIndex);
        assertTrue(oldIndex < ancientIndex);
        assertFalse(storage.isSaved());
    }

    @Test
    void execute_onlyOverdue_deadlinesSortedMostRecentlyOverdueFirst(@TempDir Path tempDir)
            throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        TestStorage storage = new TestStorage(tempDir.resolve("modules.txt"));
        Ui ui = new Ui(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));

        moduleBook.getOrCreate("CS2113").addDeadline("Older overdue", LocalDateTime.now().minusDays(10));
        moduleBook.getOrCreate("CS2113").addDeadline("Recent overdue", LocalDateTime.now().minusDays(1));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            new ListDeadlinesCommand().execute(moduleBook, storage, ui);
        } finally {
            System.setOut(originalOut);
        }

        String actual = output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        int recentIndex = actual.indexOf("Recent overdue");
        int olderIndex = actual.indexOf("Older overdue");

        assertTrue(recentIndex >= 0);
        assertTrue(olderIndex >= 0);
        assertTrue(recentIndex < olderIndex);
        assertFalse(storage.isSaved());
    }
}

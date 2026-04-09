package seedu.modulesync.others;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.modulesync.command.ListTopCommand;
import seedu.modulesync.command.ListCommand;
import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

class PriorityScoreListFeatureTest {
    private static final String PRIORITY_PATTERN_TEMPLATE = "%s.*\\[Priority: (\\d+)\\]";

    static class StorageStub extends Storage {
        private boolean isSaved;

        StorageStub(Path path) {
            super(path);
        }

        @Override
        public void save(ModuleBook moduleBook) {
            isSaved = true;
        }

        boolean isSaved() {
            return isSaved;
        }
    }

    @Test
    void execute_weightedAndUnweightedTodoTasks_printsPriorityScoresInTaskList(@TempDir Path tempDir)
            throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        moduleBook.getOrCreate("CS2113").addTodo("Weighted reading", 40);
        moduleBook.getOrCreate("CS2100").addTodo("Unweighted practice");

        StorageStub storageStub = new StorageStub(tempDir.resolve("modules.txt"));
        Ui ui = new Ui(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            new ListCommand().execute(moduleBook, storageStub, ui);
        } finally {
            System.setOut(originalOut);
        }

        String actual = output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        String expected = "Here are the tasks:\n"
                + "1.[CS2113] [T][ ] Weighted reading [40%] [Priority: 40]\n"
                + "2.[CS2100] [T][ ] Unweighted practice [Priority: 0]\n";

        assertEquals(expected, actual);
        assertFalse(storageStub.isSaved());
    }

    @Test
    void execute_tasksWithDifferentWeightsAndDeadlines_printsDifferentPriorityScores(@TempDir Path tempDir)
            throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        moduleBook.getOrCreate("CS2113").addDeadline("High weight",
                LocalDateTime.now().plusDays(5), 80);
        moduleBook.getOrCreate("CS2100").addDeadline("Low weight same deadline",
                LocalDateTime.now().plusDays(5), 20);
        moduleBook.getOrCreate("CS2040S").addDeadline("Earlier deadline same weight",
                LocalDateTime.now().plusDays(1), 20);

        StorageStub storageStub = new StorageStub(tempDir.resolve("modules.txt"));
        Ui ui = new Ui(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            new ListCommand().execute(moduleBook, storageStub, ui);
        } finally {
            System.setOut(originalOut);
        }

        String actual = output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        int highWeightScore = extractPriorityScore(actual, "High weight");
        int lowWeightScore = extractPriorityScore(actual, "Low weight same deadline");
        int earlierDeadlineScore = extractPriorityScore(actual, "Earlier deadline same weight");

        assertTrue(highWeightScore > lowWeightScore);
        assertTrue(earlierDeadlineScore > lowWeightScore);
        assertTrue(actual.contains("[Priority: "));
        assertFalse(storageStub.isSaved());
    }

    @Test
    void execute_topUrgentTasks_sortsByPrintedPriorityScore(@TempDir Path tempDir) throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        moduleBook.getOrCreate("CS2113").addDeadline("Far high weight",
                LocalDateTime.now().plusDays(30), 40);
        moduleBook.getOrCreate("CS2100").addDeadline("Sooner lower weight",
                LocalDateTime.now().plusDays(1), 20);

        StorageStub storageStub = new StorageStub(tempDir.resolve("modules.txt"));
        Ui ui = new Ui(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            new ListTopCommand(2).execute(moduleBook, storageStub, ui);
        } finally {
            System.setOut(originalOut);
        }

        String actual = output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        int soonerTaskIndex = actual.indexOf("2.[CS2100] [D][ ] Sooner lower weight");
        int farTaskIndex = actual.indexOf("1.[CS2113] [D][ ] Far high weight");

        assertTrue(soonerTaskIndex >= 0);
        assertTrue(farTaskIndex >= 0);
        assertTrue(soonerTaskIndex < farTaskIndex);
        assertTrue(extractPriorityScore(actual, "Sooner lower weight")
                > extractPriorityScore(actual, "Far high weight"));
        assertFalse(storageStub.isSaved());
    }

    /**
     * Extracts the printed priority score for the task with the given description.
     *
     * @param output the rendered list output
     * @param description the task description to search for
     * @return the parsed priority score
     */
    private int extractPriorityScore(String output, String description) {
        String patternText = String.format(PRIORITY_PATTERN_TEMPLATE, Pattern.quote(description));
        Pattern pattern = Pattern.compile(patternText);
        Matcher matcher = pattern.matcher(output);
        assertTrue(matcher.find());
        return Integer.parseInt(matcher.group(1));
    }
}

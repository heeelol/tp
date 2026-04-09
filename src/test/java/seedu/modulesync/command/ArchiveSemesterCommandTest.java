package seedu.modulesync.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.semester.Semester;
import seedu.modulesync.semester.SemesterBook;
import seedu.modulesync.storage.SemesterStorage;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

class ArchiveSemesterCommandTest {

    @Test
    void execute_archivesCurrentSemester_persistsHeader(@TempDir Path tempDir) throws Exception {
        SemesterBook semesterBook = new SemesterBook();
        ModuleBook moduleBook = new ModuleBook();
        moduleBook.getOrCreate("CS2113").addTodo("Week 12");
        semesterBook.addSemester(new Semester("AY2526-S2", moduleBook, false));
        semesterBook.setCurrentSemester("AY2526-S2");

        SemesterStorage semesterStorage = new SemesterStorage(tempDir);
        Ui ui = new Ui(new Scanner(new ByteArrayInputStream(new byte[0])));
        Storage unusedStorage = new Storage(tempDir.resolve("unused.txt"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            new ArchiveSemesterCommand(semesterBook, semesterStorage)
                    .execute(semesterBook.getCurrentModuleBook(), unusedStorage, ui);
        } finally {
            System.setOut(originalOut);
        }

        assertTrue(semesterBook.getCurrentSemester().isArchived());

        String contents = Files.readString(tempDir.resolve("AY2526-S2.txt"), StandardCharsets.UTF_8)
                .replace("\r\n", "\n");
        assertTrue(contents.startsWith("#archived\n"));

        String actualOutput = output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        assertTrue(actualOutput.contains("Semester 'AY2526-S2' has been archived."));
        assertEquals("AY2526-S2\n",
                Files.readString(tempDir.resolve("current.txt"), StandardCharsets.UTF_8).replace("\r\n", "\n"));
    }
}

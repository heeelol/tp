package seedu.modulesync.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.semester.Semester;
import seedu.modulesync.semester.SemesterBook;
import seedu.modulesync.storage.SemesterStorage;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

class UnarchiveSemesterCommandTest {

    @Test
    void execute_unarchivesCurrentSemester_removesHeader(@TempDir Path tempDir) throws Exception {
        SemesterBook semesterBook = new SemesterBook();
        ModuleBook moduleBook = new ModuleBook();
        moduleBook.getOrCreate("CS2113").addTodo("Week 12");
        semesterBook.addSemester(new Semester("AY2526-S2", moduleBook, true));
        semesterBook.setCurrentSemester("AY2526-S2");

        SemesterStorage semesterStorage = new SemesterStorage(tempDir);
        semesterStorage.save(semesterBook);

        Ui ui = new Ui(new Scanner(new ByteArrayInputStream(new byte[0])));
        Storage unusedStorage = new Storage(tempDir.resolve("unused.txt"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            new UnarchiveSemesterCommand(semesterBook, semesterStorage)
                    .execute(semesterBook.getCurrentModuleBook(), unusedStorage, ui);
        } finally {
            System.setOut(originalOut);
        }

        assertFalse(semesterBook.getCurrentSemester().isArchived());

        String contents = Files.readString(tempDir.resolve("AY2526-S2.txt"), StandardCharsets.UTF_8)
                .replace("\r\n", "\n");
        assertFalse(contents.startsWith("#archived\n"));
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("has been unarchived"));
    }
}

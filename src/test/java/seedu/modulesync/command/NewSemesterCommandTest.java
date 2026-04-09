package seedu.modulesync.command;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.semester.SemesterBook;
import seedu.modulesync.storage.SemesterStorage;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

class NewSemesterCommandTest {

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

    static class TestSemesterStorage extends SemesterStorage {
        boolean saved;

        TestSemesterStorage(Path path) {
            super(path);
        }

        @Override
        public void save(SemesterBook semesterBook) {
            saved = true;
        }
    }

    static class TestUi extends Ui {
        String message;

        TestUi() {
            super(new java.util.Scanner(new ByteArrayInputStream(new byte[0])));
        }

        @Override
        public void showMessage(String message) {
            this.message = message;
        }
    }

    @Test
    void execute_createsNewSemester(@TempDir Path tempDir) throws ModuleSyncException {
        SemesterBook semesterBook = new SemesterBook();
        TestSemesterStorage storage = new TestSemesterStorage(tempDir.resolve("semesters.txt"));
        TestUi ui = new TestUi();

        // Set an initial semester
        semesterBook.switchOrCreate("AY2526-S1");

        NewSemesterCommand command = new NewSemesterCommand(semesterBook, storage, "AY2526-S2");
        ModuleBook activeModuleBook = semesterBook.getCurrentModuleBook();
        command.execute(activeModuleBook, null, ui);

        assertTrue(semesterBook.getCurrentSemester().getName().equals("AY2526-S2"));
        assertTrue(storage.saved);
        assertTrue(ui.message.contains("AY2526-S2"));
    }

    @Test
    void execute_switchesToExistingSemester(@TempDir Path tempDir) throws ModuleSyncException {
        SemesterBook semesterBook = new SemesterBook();
        TestSemesterStorage storage = new TestSemesterStorage(tempDir.resolve("semesters.txt"));
        TestUi ui = new TestUi();

        // Create two semesters
        semesterBook.switchOrCreate("AY2526-S1");
        semesterBook.switchOrCreate("AY2526-S2");

        // Switch back to S1
        NewSemesterCommand command = new NewSemesterCommand(semesterBook, storage, "AY2526-S1");
        ModuleBook activeModuleBook = semesterBook.getCurrentModuleBook();
        command.execute(activeModuleBook, null, ui);

        assertTrue(semesterBook.getCurrentSemester().getName().equals("AY2526-S1"));
        assertTrue(storage.saved);
    }
}

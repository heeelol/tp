package seedu.modulesync.command;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.semester.Semester;
import seedu.modulesync.semester.SemesterBook;
import seedu.modulesync.storage.SemesterStorage;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

/**
 * Unarchives the current semester, making it editable.
 *
 * <p>Usage: {@code semester unarchive}
 */
public class UnarchiveSemesterCommand extends SemesterCommand {

    /**
     * Constructs an UnarchiveSemesterCommand.
     *
     * @param semesterBook the application's semester book
     * @param semesterStorage the semester storage for persistence
     */
    public UnarchiveSemesterCommand(SemesterBook semesterBook, SemesterStorage semesterStorage) {
        super(semesterBook, semesterStorage);
    }

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        assert ui != null : "Ui must not be null";

        Semester current = semesterBook.getCurrentSemester();
        semesterBook.unarchiveCurrentSemester();
        semesterStorage.save(semesterBook);
        ui.showSemesterUnarchived(current.getName());
    }
}

package seedu.modulesync.command;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.semester.Semester;
import seedu.modulesync.semester.SemesterBook;
import seedu.modulesync.storage.SemesterStorage;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

/**
 * Archives the current semester, making it read-only.
 *
 * <p>Usage: {@code semester archive}
 */
public class ArchiveSemesterCommand extends SemesterCommand {

    /**
     * Constructs an ArchiveSemesterCommand.
     *
     * @param semesterBook the application's semester book
     * @param semesterStorage the semester storage for persistence
     */
    public ArchiveSemesterCommand(SemesterBook semesterBook, SemesterStorage semesterStorage) {
        super(semesterBook, semesterStorage);
    }

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        assert ui != null : "Ui must not be null";

        Semester current = semesterBook.getCurrentSemester();
        semesterBook.archiveCurrentSemester();
        semesterStorage.save(semesterBook);
        ui.showSemesterArchived(current.getName());
    }
}

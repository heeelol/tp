package seedu.modulesync.command;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.semester.SemesterBook;
import seedu.modulesync.storage.SemesterStorage;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

/**
 * Command to list all tracked semesters.
 */
public class ListSemesterCommand extends SemesterCommand {

    public ListSemesterCommand(SemesterBook semesterBook, SemesterStorage semesterStorage) {
        super(semesterBook, semesterStorage);
    }

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        ui.showSemesterList(semesterBook);
    }
}

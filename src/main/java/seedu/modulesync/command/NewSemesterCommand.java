package seedu.modulesync.command;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.semester.SemesterBook;
import seedu.modulesync.storage.SemesterStorage;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

/**
 * Command to create and switch to a new semester.
 *
 * <p>Usage: {@code semester new SEMESTER_NAME}
 *
 * <p>If the semester already exists, switches to it without creating a duplicate.
 * If the semester is new, creates it as the active (non-archived) semester and switches to it.
 */
public class NewSemesterCommand extends SemesterCommand {

    private final String semesterName;

    /**
     * Constructs a NewSemesterCommand with the specified semester name.
     *
     * @param semesterBook    the application's semester book
     * @param semesterStorage the semester-level storage
     * @param semesterName    the name of the semester to create (e.g. "AY2526-S2")
     */
    public NewSemesterCommand(SemesterBook semesterBook, SemesterStorage semesterStorage, String semesterName) {
        super(semesterBook, semesterStorage);
        assert semesterName != null && !semesterName.trim().isEmpty() : "Semester name must not be null or empty";
        this.semesterName = semesterName.trim();
    }

    @Override
    public void execute(ModuleBook activeModuleBook, Storage activeStorage, Ui ui) throws ModuleSyncException {
        boolean created = semesterBook.switchOrCreate(semesterName);
        semesterStorage.save(semesterBook);

        if (created) {
            ui.showMessage("Created new semester: " + semesterName);
        }
        ui.showMessage("Switched to semester: " + semesterName);
    }
}

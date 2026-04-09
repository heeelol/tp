package seedu.modulesync.command;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.semester.SemesterBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

/**
 * Command that shows overall statistics for a specific semester.
 *
 * <p>Triggered by {@code semester stats SEMESTER_NAME}.
 */
public class SemesterStatsCommand extends Command {

    private final SemesterBook semesterBook;
    private final String semesterName;

    /**
     * Constructs a semester statistics command for the given semester.
     *
     * @param semesterBook the semester book to query
     * @param semesterName the semester name as entered by the user
     */
    public SemesterStatsCommand(SemesterBook semesterBook, String semesterName) {
        assert semesterBook != null : "SemesterBook must not be null";
        assert semesterName != null && !semesterName.trim().isEmpty() : "Semester name must not be null or blank";
        this.semesterBook = semesterBook;
        this.semesterName = semesterName.trim();
    }

    @Override
    public boolean isMutating() {
        return false;
    }

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        assert moduleBook != null : "ModuleBook must not be null";
        assert ui != null : "Ui must not be null";

        ModuleBook target = semesterBook.getSemester(semesterName).getModuleBook();
        ui.showSemesterStatistics(target);
    }
}

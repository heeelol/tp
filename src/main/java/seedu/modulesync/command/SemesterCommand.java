package seedu.modulesync.command;

import seedu.modulesync.semester.SemesterBook;
import seedu.modulesync.storage.SemesterStorage;

//@@author Huang-Hau-Shuan
/**
 * Base class for commands that operate on the semester lifecycle rather than on individual tasks.
 *
 * <p>Semester-level commands need access to the entire {@link SemesterBook} and
 * {@link SemesterStorage} to switch, archive, or list semesters. Extend this class for
 * those commands and inject the shared context via the constructor.
 *
 * <p>Semester lifecycle commands are not blocked by the read-only guard in
 * {@code ModuleSync.run()} because they operate at the semester level, not on tasks.
 */
public abstract class SemesterCommand extends Command {

    /** The semester book shared across the application session. */
    protected final SemesterBook semesterBook;

    /** The semester-level storage for persisting the entire semester book. */
    protected final SemesterStorage semesterStorage;

    /**
     * Constructs a SemesterCommand with the shared semester context.
     *
     * @param semesterBook    the application's semester book
     * @param semesterStorage the semester-level storage
     */
    protected SemesterCommand(SemesterBook semesterBook, SemesterStorage semesterStorage) {
        assert semesterBook != null : "SemesterBook must not be null";
        assert semesterStorage != null : "SemesterStorage must not be null";
        this.semesterBook = semesterBook;
        this.semesterStorage = semesterStorage;
    }

    /**
     * Semester lifecycle commands are not blocked by the read-only guard.
     *
     * @return {@code false} — semester commands bypass the task-mutation guard
     */
    @Override
    public boolean isMutating() {
        return false;
    }
}

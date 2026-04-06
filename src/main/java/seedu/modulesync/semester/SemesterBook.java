package seedu.modulesync.semester;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;

//@@author Huang-Hau-Shuan
/**
 * The central registry of all semesters in the application.
 *
 * <p>Hierarchy:  SemesterBook → Semester (active/archived) → ModuleBook → Module → TaskList → Task
 *
 * <p>At any given time, exactly one semester is the <em>current</em> semester.  All existing
 * task commands (add, delete, mark, …) operate on {@link #getCurrentModuleBook()}, which is
 * simply the {@link ModuleBook} of the current semester.  This means those commands require
 * <em>zero changes</em>; the wiring is done entirely in {@code ModuleSync.run()}.
 *
 * <p>Teammates implementing semester-level commands (archive, switch, list semesters) should
 * inject a {@code SemesterBook} into their command via the constructor, following the same
 * pattern used by {@code SetWeightCommand}.
 *
 * <h2>Read-only enforcement</h2>
 * Before any mutating command executes, {@code ModuleSync.run()} (or the command itself) should
 * call {@link #isCurrentSemesterReadOnly()} and throw a {@link ModuleSyncException} if {@code true}.
 */
public class SemesterBook {

    private static final Logger LOGGER = Logger.getLogger(SemesterBook.class.getName());

    /** Ordered map: semester name → Semester object. Insertion order = chronological order added. */
    private final Map<String, Semester> semesters = new LinkedHashMap<>();

    /** Name of the semester that is currently active (the user is working in). */
    private String currentSemesterName;

    /**
     * Constructs an empty SemesterBook with no semesters registered.
     * Callers must call {@link #addSemester(Semester)} and {@link #setCurrentSemester(String)}
     * before using the book (typically done by {@code SemesterStorage.load()}).
     */
    public SemesterBook() {
        // Intentionally empty — SemesterStorage populates this.
    }

    // -------------------------------------------------------------------------
    // Semester registration (used by SemesterStorage during load)
    // -------------------------------------------------------------------------

    /**
     * Registers a semester in this book.
     * If a semester with the same name already exists it is silently replaced
     * (safe for re-load scenarios).
     *
     * @param semester the semester to add; must not be null
     */
    public void addSemester(Semester semester) {
        assert semester != null : "Semester must not be null";
        semesters.put(semester.getName(), semester);
        LOGGER.fine(() -> "Registered semester: " + semester.getName());
    }

    // -------------------------------------------------------------------------
    // Active-semester management
    // -------------------------------------------------------------------------

    /**
     * Sets the current (active) semester by name.
     * The semester must already be registered via {@link #addSemester(Semester)}.
     *
     * @param name the semester name to make current
     * @throws ModuleSyncException if no semester with that name exists
     */
    public void setCurrentSemester(String name) throws ModuleSyncException {
        assert name != null && !name.trim().isEmpty() : "Semester name must not be null or blank";
        if (!semesters.containsKey(name)) {
            throw new ModuleSyncException("No such semester: " + name);
        }
        currentSemesterName = name;
        LOGGER.fine(() -> "Current semester set to: " + name);
    }

    /**
     * Returns the currently active {@link Semester}.
     *
     * @return the current semester
     * @throws ModuleSyncException if no current semester has been set
     */
    public Semester getCurrentSemester() throws ModuleSyncException {
        if (currentSemesterName == null) {
            throw new ModuleSyncException("No active semester. Use 'semester switch SEMESTER_NAME' to begin.");
        }
        Semester semester = semesters.get(currentSemesterName);
        assert semester != null : "currentSemesterName points to a non-existent semester";
        return semester;
    }

    /**
     * Convenience method — returns the {@link ModuleBook} of the current semester.
     * This is what all existing task commands operate on.
     *
     * @return the active semester's module book
     * @throws ModuleSyncException if no current semester has been set
     */
    public ModuleBook getCurrentModuleBook() throws ModuleSyncException {
        return getCurrentSemester().getModuleBook();
    }

    /**
     * Returns whether the current semester is read-only (archived).
     *
     * <p>Commands that mutate tasks should guard with:
     * <pre>
     *   if (semesterBook.isCurrentSemesterReadOnly()) {
     *       throw new ModuleSyncException("This semester is archived and cannot be modified.");
     *   }
     * </pre>
     *
     * @return {@code true} if the current semester is archived; {@code false} otherwise
     * @throws ModuleSyncException if no current semester has been set
     */
    public boolean isCurrentSemesterReadOnly() throws ModuleSyncException {
        return getCurrentSemester().isReadOnly();
    }

    // -------------------------------------------------------------------------
    // Semester creation / switch (used by teammate's SemesterSwitchCommand)
    // -------------------------------------------------------------------------

    /**
     * Switches to the semester with the given name, creating it (as active) if it does not exist.
     *
     * <p>This is the single method a teammate's {@code SemesterSwitchCommand} should call.
     * It encapsulates both the "create new semester" and "switch to existing" paths.
     *
     * @param name the semester label to switch to (e.g. "AY2627-S1")
     * @return {@code true} if a new semester was created, {@code false} if an existing one was switched to
     * @throws ModuleSyncException if the name is blank
     */
    public boolean switchOrCreate(String name) throws ModuleSyncException {
        if (name == null || name.trim().isEmpty()) {
            throw new ModuleSyncException("Semester name must not be blank.");
        }
        boolean created = false;
        if (!semesters.containsKey(name)) {
            addSemester(new Semester(name, false));
            created = true;
            LOGGER.info(() -> "Created new semester: " + name);
        }
        setCurrentSemester(name);
        return created;
    }

    // -------------------------------------------------------------------------
    // Archive / unarchive (used by teammate's SemesterArchiveCommand)
    // -------------------------------------------------------------------------

    /**
     * Archives the current semester, making it read-only.
     * The user remains in the archived semester; they must call
     * {@link #switchOrCreate(String)} to move to a different semester.
     *
     * @throws ModuleSyncException if no current semester is set
     */
    public void archiveCurrentSemester() throws ModuleSyncException {
        Semester current = getCurrentSemester();
        current.setArchived(true);
        LOGGER.info(() -> "Archived semester: " + current.getName());
    }

    /**
     * Unarchives (restores) the current semester, making it editable again.
     *
     * @throws ModuleSyncException if no current semester is set
     */
    public void unarchiveCurrentSemester() throws ModuleSyncException {
        Semester current = getCurrentSemester();
        current.setArchived(false);
        LOGGER.info(() -> "Unarchived semester: " + current.getName());
    }

    /**
     * Archives a specific semester by name.
     *
     * @param name the semester to archive
     * @throws ModuleSyncException if no semester with that name exists
     */
    public void archiveSemester(String name) throws ModuleSyncException {
        Semester semester = getSemester(name);
        semester.setArchived(true);
        LOGGER.info(() -> "Archived semester: " + name);
    }

    // -------------------------------------------------------------------------
    // Queries (used by teammate's SemesterListCommand, etc.)
    // -------------------------------------------------------------------------

    /**
     * Returns a semester by name.
     *
     * @param name the semester label
     * @return the matching {@link Semester}
     * @throws ModuleSyncException if no semester with that name is registered
     */
    public Semester getSemester(String name) throws ModuleSyncException {
        Semester semester = semesters.get(name);
        if (semester == null) {
            throw new ModuleSyncException("No such semester: " + name);
        }
        return semester;
    }

    /**
     * Returns an unmodifiable view of all registered semesters, in insertion order.
     *
     * @return all semesters
     */
    public Collection<Semester> getAllSemesters() {
        return Collections.unmodifiableCollection(semesters.values());
    }

    /**
     * Returns whether any semester has been registered.
     *
     * @return {@code true} if at least one semester exists
     */
    public boolean hasSemesters() {
        return !semesters.isEmpty();
    }

    /**
     * Returns the name of the current semester, or {@code null} if none has been set.
     * Prefer {@link #getCurrentSemester()} where possible; this is provided for
     * {@code SemesterStorage} to write the {@code current.txt} pointer.
     *
     * @return the current semester name, or null
     */
    public String getCurrentSemesterName() {
        return currentSemesterName;
    }
}

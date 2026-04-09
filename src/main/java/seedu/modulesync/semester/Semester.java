package seedu.modulesync.semester;

import seedu.modulesync.module.ModuleBook;

/**
 * Represents a single academic semester (e.g. "AY2526-S2").
 *
 * <p>Each semester owns exactly one {@link ModuleBook} containing that semester's
 * modules and tasks. A semester may be <em>active</em> (the one the user is currently
 * working in) or <em>archived</em> (read-only, from a past semester).
 *
 * <p>The archived flag is intentionally mutable so that a teammate's archive / unarchive
 * command can call {@link #setArchived(boolean)} without needing to reconstruct the object.
 */
public class Semester {

    private final String name;
    private final ModuleBook moduleBook;
    private boolean archived;

    /**
     * Constructs a Semester with an empty {@link ModuleBook}.
     *
     * @param name     the semester label chosen by the student (e.g. "AY2526-S2")
     * @param archived whether this semester starts in the archived (read-only) state
     */
    public Semester(String name, boolean archived) {
        assert name != null && !name.trim().isEmpty() : "Semester name must not be null or blank";
        this.name = name.trim();
        this.moduleBook = new ModuleBook();
        this.archived = archived;
    }

    /**
     * Constructs a Semester with a pre-populated {@link ModuleBook} (used when loading from disk).
     *
     * @param name       the semester label
     * @param moduleBook the already-loaded module book
     * @param archived   whether this semester is archived
     */
    public Semester(String name, ModuleBook moduleBook, boolean archived) {
        assert name != null && !name.trim().isEmpty() : "Semester name must not be null or blank";
        assert moduleBook != null : "ModuleBook must not be null";
        this.name = name.trim();
        this.moduleBook = moduleBook;
        this.archived = archived;
    }

    /**
     * Returns the semester label (e.g. "AY2526-S2").
     *
     * @return the semester name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the {@link ModuleBook} that belongs to this semester.
     *
     * @return this semester's module book
     */
    public ModuleBook getModuleBook() {
        return moduleBook;
    }

    /**
     * Returns whether this semester has been archived (i.e. is read-only).
     *
     * @return {@code true} if archived, {@code false} if active / editable
     */
    public boolean isArchived() {
        return archived;
    }

    /**
     * Sets the archived status of this semester.
     * Called by a teammate's archive / unarchive command.
     *
     * @param archived {@code true} to make this semester read-only, {@code false} to re-activate it
     */
    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    /**
     * Returns whether this semester is read-only.
     * A semester is read-only when it has been archived.
     *
     * @return {@code true} if the user should not be allowed to modify tasks in this semester
     */
    public boolean isReadOnly() {
        return archived;
    }

    @Override
    public String toString() {
        return name + (archived ? " [archived]" : " [active]");
    }
}

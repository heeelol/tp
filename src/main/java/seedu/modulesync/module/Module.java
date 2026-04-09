package seedu.modulesync.module;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.task.Task;
import seedu.modulesync.task.TaskList;

//@@author Huang-Hau-Shuan
/**
 * Represents a single academic module (e.g. "CS2113") within a semester.
 *
 * <p>Each module owns a {@link TaskList} containing all tasks for that module.
 * It also stores optional grade and credit-unit metadata so that teammates can
 * implement grade-entry and GPA commands on top of this containee structure.
 *
 * <p><b>Grade / Credits lifecycle:</b>
 * <ul>
 *   <li>{@code grade} is {@code null} until the student enters it at end-of-semester.</li>
 *   <li>{@code credits} defaults to {@code 0} until explicitly set.</li>
 *   <li>Use {@link #hasGrade()} before calling {@link #getGrade()} to avoid null checks.</li>
 * </ul>
 */
public class Module {

    private static final int DEFAULT_CREDITS = 0;

    private final String code;
    private final TaskList taskList = new TaskList();

    /** The letter grade awarded for this module (e.g. "A+", "B"). Null until entered. */
    private String grade;

    /** The number of Modular Credits (MCs) for this module. Defaults to 0 until set. */
    private int credits;

    /** Whether this module has been archived. Archived modules are read-only. */
    private boolean archived;

    /**
     * Constructs a Module with no grade and zero credits.
     *
     * @param code the module code (case-insensitive; stored as upper-case)
     */
    public Module(String code) {
        assert code != null && !code.trim().isEmpty() : "Module code must not be null or empty";
        this.code = code.toUpperCase();
        this.grade = null;
        this.credits = DEFAULT_CREDITS;
        this.archived = false;
    }

    /**
     * Returns the module code (always upper-case).
     *
     * @return the module code
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the task list for this module.
     *
     * @return the {@link TaskList} owned by this module
     */
    public TaskList getTasks() {
        return taskList;
    }

    // -------------------------------------------------------------------------
    // Grade / Credits — for teammate's grade command to call
    // -------------------------------------------------------------------------

    /**
     * Returns the letter grade assigned to this module, or {@code null} if not yet entered.
     *
     * @return the grade string (e.g. "A+"), or null
     */
    public String getGrade() {
        return grade;
    }

    /**
     * Sets the letter grade for this module.
     * Typically called by a teammate's {@code grade /mod <CODE> /grade <LETTER>} command.
     *
     * @param grade the letter grade to assign (e.g. "A+", "B"); must not be blank
     */
    public void setGrade(String grade) {
        assert grade != null && !grade.trim().isEmpty() : "Grade must not be null or blank";
        this.grade = grade.trim().toUpperCase();
    }

    /**
     * Returns whether a grade has been assigned to this module.
     *
     * @return {@code true} if a grade is present, {@code false} otherwise
     */
    public boolean hasGrade() {
        return grade != null;
    }

    /**
     * Returns the number of Modular Credits (MCs) assigned to this module.
     * Defaults to {@code 0} until explicitly set.
     *
     * @return the credit units
     */
    public int getCredits() {
        return credits;
    }

    /**
     * Sets the number of Modular Credits (MCs) for this module.
     * Typically called by a teammate's {@code grade /mod <CODE> /credits <N>} command.
     *
     * @param credits the credit units; must be non-negative
     */
    public void setCredits(int credits) {
        assert credits >= 0 : "Credits must be non-negative";
        this.credits = credits;
    }

    public Task addTodo(String description) throws ModuleSyncException {
        assert taskList != null : "Task list must be initialized";
        return taskList.addTodo(code, description);
    }

    /**
     * Adds a to-do task with an optional weightage to this module.
     *
     * @param description the task description
     * @param weightage   the optional weightage (0–100), or null if unweighted
     * @return the newly created task
     * @throws ModuleSyncException if the description is blank
     */
    public Task addTodo(String description, Integer weightage) throws ModuleSyncException {
        assert taskList != null : "Task list must be initialized";
        return taskList.addTodo(code, description, weightage);
    }

    public Task addDeadline(String description, java.time.LocalDateTime by) throws ModuleSyncException {
        assert taskList != null : "Task list must be initialized";
        return taskList.addDeadline(code, description, by);
    }

    /**
     * Adds a deadline task with an optional weightage to this module.
     *
     * @param description the task description
     * @param by          the deadline date and time
     * @param weightage   the optional weightage (0–100), or null if unweighted
     * @return the newly created task
     * @throws ModuleSyncException if the description is blank
     */
    public Task addDeadline(String description, java.time.LocalDateTime by,
                           Integer weightage) throws ModuleSyncException {
        assert taskList != null : "Task list must be initialized";
        return taskList.addDeadline(code, description, by, weightage);
    }

    // -------------------------------------------------------------------------
    // Archive — for teammate's archive command to call
    // -------------------------------------------------------------------------

    /**
     * Returns whether this module has been archived.
     *
     * @return {@code true} if archived, {@code false} if active
     */
    public boolean isArchived() {
        return archived;
    }

    /**
     * Sets the archived status of this module.
     * Typically called by a teammate's {@code module archive /mod <CODE>} command.
     *
     * @param archived {@code true} to archive this module, {@code false} to unarchive
     */
    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    /**
     * Returns whether this module is read-only.
     * A module is read-only when it has been archived.
     *
     * @return {@code true} if the user should not be allowed to modify tasks in this module
     */
    public boolean isReadOnly() {
        return archived;
    }
}

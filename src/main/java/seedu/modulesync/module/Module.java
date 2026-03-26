package seedu.modulesync.module;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.task.Task;
import seedu.modulesync.task.TaskList;

public class Module {
    private final String code;
    private final TaskList taskList = new TaskList();

    public Module(String code) {
        assert code != null && !code.trim().isEmpty() : "Module code must not be null or empty";
        this.code = code.toUpperCase();
    }

    public String getCode() {
        return code;
    }

    public TaskList getTasks() {
        return taskList;
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
}

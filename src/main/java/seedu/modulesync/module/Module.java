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

    public Task addDeadline(String description, java.time.LocalDateTime by) throws ModuleSyncException {
        assert taskList != null : "Task list must be initialized";
        return taskList.addDeadline(code, description, by);
    }
}

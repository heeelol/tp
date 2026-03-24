package seedu.modulesync.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import seedu.modulesync.exception.ModuleSyncException;

public class TaskList {
    private final List<Task> tasks = new ArrayList<>();

    public Task addTodo(String moduleCode, String description) throws ModuleSyncException {
        assert moduleCode != null && !moduleCode.trim().isEmpty() : "Module code must be provided for todo";
        int initialSize = tasks.size();
        if (description == null || description.trim().isEmpty()) {
            throw new ModuleSyncException("Task description cannot be empty.");
        }
        Task todo = new Todo(moduleCode, description.trim());
        tasks.add(todo);
        assert tasks.size() == initialSize + 1 : "Task list size should increase after adding todo";
        return todo;
    }

    public Task addDeadline(String moduleCode, String description, 
                            java.time.LocalDateTime by) throws ModuleSyncException {
        assert moduleCode != null && !moduleCode.trim().isEmpty() : "Module code must be provided for deadline";
        assert by != null : "Deadline datetime must be provided";
        int initialSize = tasks.size();
        if (description == null || description.trim().isEmpty()) {
            throw new ModuleSyncException("Task description cannot be empty.");
        }
        Task deadline = new Deadline(moduleCode, description.trim(), by);
        tasks.add(deadline);
        assert tasks.size() == initialSize + 1 : "Task list size should increase after adding deadline";
        return deadline;
    }

    public Task add(Task task) {
        tasks.add(task);
        return task;
    }

    public int size() {
        return tasks.size();
    }

    public List<Task> asUnmodifiableList() {
        return Collections.unmodifiableList(tasks);
    }

    public Task removeTask(int index) throws ModuleSyncException {
        if (index < 0 || index >= tasks.size()) {
            throw new ModuleSyncException("Task index out of bounds.");
        }
        return tasks.remove(index);
    }
}

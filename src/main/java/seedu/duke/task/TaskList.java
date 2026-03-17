package seedu.duke.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import seedu.duke.exception.ModuleSyncException;

public class TaskList {
    private final List<Task> tasks = new ArrayList<>();

    public Task addTodo(String moduleCode, String description) throws ModuleSyncException {
        if (description == null || description.trim().isEmpty()) {
            throw new ModuleSyncException("Task description cannot be empty.");
        }
        Task todo = new Todo(moduleCode, description.trim());
        tasks.add(todo);
        return todo;
    }

    public Task addDeadline(String moduleCode, String description, 
                            java.time.LocalDateTime by) throws ModuleSyncException {
        if (description == null || description.trim().isEmpty()) {
            throw new ModuleSyncException("Task description cannot be empty.");
        }
        Task deadline = new Deadline(moduleCode, description.trim(), by);
        tasks.add(deadline);
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


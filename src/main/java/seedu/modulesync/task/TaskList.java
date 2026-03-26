package seedu.modulesync.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import seedu.modulesync.exception.ModuleSyncException;

public class TaskList {
    private final List<Task> tasks = new ArrayList<>();

    public Task addTodo(String moduleCode, String description) throws ModuleSyncException {
        return addTodo(moduleCode, description, null);
    }

    /**
     * Adds a to-do task with an optional weightage to the list.
     *
     * @param moduleCode  the module this task belongs to
     * @param description the task description
     * @param weightage   the optional weightage (0–100), or null if unweighted
     * @return the newly created {@link Todo}
     * @throws ModuleSyncException if the description is blank
     */
    public Task addTodo(String moduleCode, String description, Integer weightage) throws ModuleSyncException {
        assert moduleCode != null && !moduleCode.trim().isEmpty() : "Module code must be provided for todo";
        int initialSize = tasks.size();
        if (description == null || description.trim().isEmpty()) {
            throw new ModuleSyncException("Task description cannot be empty.");
        }
        Todo todo = new Todo(moduleCode, description.trim());
        if (weightage != null) {
            assert weightage >= 0 && weightage <= 100 : "Weightage must be between 0 and 100";
            todo.setWeightage(weightage);
        }
        tasks.add(todo);
        assert tasks.size() == initialSize + 1 : "Task list size should increase after adding todo";
        return todo;
    }

    public Task addDeadline(String moduleCode, String description, 
                            java.time.LocalDateTime by) throws ModuleSyncException {
        return addDeadline(moduleCode, description, by, null);
    }

    /**
     * Adds a deadline task with an optional weightage to the list.
     *
     * @param moduleCode  the module this task belongs to
     * @param description the task description
     * @param by          the deadline date and time
     * @param weightage   the optional weightage (0–100), or null if unweighted
     * @return the newly created {@link Deadline}
     * @throws ModuleSyncException if the description is blank
     */
    public Task addDeadline(String moduleCode, String description,
                            java.time.LocalDateTime by, Integer weightage) throws ModuleSyncException {
        assert moduleCode != null && !moduleCode.trim().isEmpty() : "Module code must be provided for deadline";
        assert by != null : "Deadline datetime must be provided";
        int initialSize = tasks.size();
        if (description == null || description.trim().isEmpty()) {
            throw new ModuleSyncException("Task description cannot be empty.");
        }
        Deadline deadline = new Deadline(moduleCode, description.trim(), by);
        if (weightage != null) {
            assert weightage >= 0 && weightage <= 100 : "Weightage must be between 0 and 100";
            deadline.setWeightage(weightage);
        }
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

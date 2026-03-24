package seedu.duke.module;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import seedu.duke.exception.ModuleSyncException;
import seedu.duke.task.Task;

/**
 * Represents the collection of all modules and their associated tasks.
 * Acts as the central data store for the ModuleSync application.
 */
public class ModuleBook {
    private final Map<String, Module> modules = new LinkedHashMap<>();

    /**
     * Retrieves the module with the given code, creating it if it does not exist.
     *
     * @param code the module code (case-insensitive)
     * @return the existing or newly created {@link Module}
     */
    public Module getOrCreate(String code) {
        return modules.computeIfAbsent(code.toUpperCase(), Module::new);
    }

    /**
     * Returns all modules currently tracked.
     *
     * @return a collection of all {@link Module} objects
     */
    public Collection<Module> getModules() {
        return modules.values();
    }

    /**
     * Counts the total number of tasks across all modules.
     *
     * @return the total task count
     */
    public int countTotalTasks() {
        var taskCounts = modules.values().stream().mapToInt(module -> module.getTasks().size());
        return taskCounts.sum();
    }

    /**
     * Retrieves a task by its 1-based display index across all modules.
     *
     * @param displayIndex the 1-based index of the task
     * @return the {@link Task} at the given index
     * @throws ModuleSyncException if the index is invalid or out of range
     */
    public Task getTaskByDisplayIndex(int displayIndex) throws ModuleSyncException {
        if (displayIndex <= 0) {
            throw new ModuleSyncException("Task number must be a positive integer.");
        }

        int currentIndex = 1;
        for (Module module : modules.values()) {
            for (Task task : module.getTasks().asUnmodifiableList()) {
                if (currentIndex == displayIndex) {
                    return task;
                }
                currentIndex++;
            }
        }
        throw new ModuleSyncException("Task number does not exist: " + displayIndex);
    }

    /**
     * Removes and returns a task by its 1-based display index across all modules.
     *
     * @param displayIndex the 1-based index of the task to remove
     * @return the removed {@link Task}
     * @throws ModuleSyncException if the index is invalid or out of range
     */
    public Task removeTaskByDisplayIndex(int displayIndex) throws ModuleSyncException {
        if (displayIndex <= 0) {
            throw new ModuleSyncException("Task number must be a positive integer.");
        }

        int currentIndex = 1;
        for (Module module : modules.values()) {
            int moduleTaskCount = module.getTasks().size();
            if (displayIndex >= currentIndex && displayIndex < currentIndex + moduleTaskCount) {
                int indexInModule = displayIndex - currentIndex;
                return module.getTasks().removeTask(indexInModule);
            }
            currentIndex += moduleTaskCount;
        }

        throw new ModuleSyncException("Task number does not exist: " + displayIndex);
    }
}


package seedu.modulesync.module;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.task.Task;

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
     * Returns the module with the given module code.
     * The lookup is case-insensitive. If the given code is null,
     * this method returns null.
     *
     * @param code the module code to look up
     * @return the matching module, or null if the code is null
     *         or no such module exists
     */
    public Module getModule(String code) {
        if (code == null) {
            return null;
        }
        return modules.get(code.toUpperCase());
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
        Iterator<Map.Entry<String, Module>> entryIterator = modules.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, Module> entry = entryIterator.next();
            Module module = entry.getValue();
            int moduleTaskCount = module.getTasks().size();
            if (displayIndex >= currentIndex && displayIndex < currentIndex + moduleTaskCount) {
                int indexInModule = displayIndex - currentIndex;
                Task removedTask = module.getTasks().removeTask(indexInModule);
                if (module.getTasks().size() == 0 && shouldAutoRemoveEmptyModule(module)) {
                    entryIterator.remove();
                }
                return removedTask;
            }
            currentIndex += moduleTaskCount;
        }

        throw new ModuleSyncException("Task number does not exist: " + displayIndex);
    }

    /**
     * Returns true when an empty module can be safely auto-removed as housekeeping.
     * Modules with user-visible metadata are preserved even when they have no tasks.
     */
    private boolean shouldAutoRemoveEmptyModule(Module module) {
        return !module.hasGrade() && module.getCredits() == 0 && !module.isArchived();
    }

    /**
     * Updates the deadline of a task by its 1-based display index across all modules.
     * Replaces the task with a new Deadline task, preserving its module, description, completion status and weightage.
     *
     * @param displayIndex the 1-based index of the task to update
     * @param by the new deadline date and time
     * @return the updated {@link Task}
     * @throws ModuleSyncException if the index is invalid or out of range
     */
    public Task updateTaskDeadlineByDisplayIndex(int displayIndex, java.time.LocalDateTime by) 
            throws ModuleSyncException {
        if (displayIndex <= 0) {
            throw new ModuleSyncException("Task number must be a positive integer.");
        }

        int currentIndex = 1;
        for (Module module : modules.values()) {
            int moduleTaskCount = module.getTasks().size();
            if (displayIndex >= currentIndex && displayIndex < currentIndex + moduleTaskCount) {
                int indexInModule = displayIndex - currentIndex;
                Task oldTask = module.getTasks().asUnmodifiableList().get(indexInModule);
                seedu.modulesync.task.Deadline newDeadline = new seedu.modulesync.task.Deadline(
                        oldTask.getModuleCode(), oldTask.getDescription(), oldTask.isDone(), by);
                if (oldTask.hasWeightage()) {
                    newDeadline.setWeightage(oldTask.getWeightage());
                }
                module.getTasks().setTask(indexInModule, newDeadline);
                return newDeadline;
            }
            currentIndex += moduleTaskCount;
        }

        throw new ModuleSyncException("Task number does not exist: " + displayIndex);
    }

    /**
     * Removes a module by its code.
     *
     * @param code the module code to remove (case-insensitive)
     * @return the removed module, or null if it did not exist
     */
    public Module deleteModule(String code) {
        if (code == null) {
            return null;
        }
        return modules.remove(code.toUpperCase());
    }
}


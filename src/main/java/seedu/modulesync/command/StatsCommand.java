package seedu.modulesync.command;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.Module;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.task.Deadline;
import seedu.modulesync.task.Task;
import seedu.modulesync.ui.Ui;

/**
 * Command that displays task completion statistics for a given module.
 *
 * <p>Usage: {@code stats /mod MODULE_CODE}
 *
 * <p>Reports:
 * <ul>
 *   <li>Total tasks created</li>
 *   <li>Completed on time (count and %)</li>
 *   <li>Completed late (count and %)</li>
 *   <li>Currently active / not done (count and %)</li>
 *   <li>Average completion time (days before the deadline, for deadline tasks only)</li>
 * </ul>
 */
public class StatsCommand extends Command {

    private static final Logger LOGGER = Logger.getLogger(StatsCommand.class.getName());

    private final String moduleCode;

    /**
     * Constructs a StatsCommand for the specified module.
     *
     * @param moduleCode the module code to compute statistics for
     */
    public StatsCommand(String moduleCode) {
        assert moduleCode != null && !moduleCode.trim().isEmpty() : "Module code must not be null or blank";
        this.moduleCode = moduleCode.toUpperCase();
    }

    @Override
    public boolean isMutating() {
        return false;
    }

    /**
     * Executes the stats command by computing and displaying statistics for the module.
     *
     * @param moduleBook the module book containing all tasks
     * @param storage    the storage (unused but required by contract)
     * @param ui         the UI to display the statistics
     * @throws ModuleSyncException if the module does not exist
     */
    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        assert moduleBook != null : "ModuleBook must not be null";
        assert ui != null : "Ui must not be null";

        Module module = moduleBook.getModule(moduleCode);
        if (module == null) {
            throw new ModuleSyncException("No such module: " + moduleCode);
        }

        List<Task> tasks = module.getTasks().asUnmodifiableList();
        ModuleStats stats = computeStats(tasks);

        LOGGER.fine(() -> "Stats computed for " + moduleCode + ": total=" + stats.totalTasks);
        ui.showModuleStats(moduleCode, stats.totalTasks, stats.completedOnTime,
                stats.completedLate, stats.active, stats.avgDaysBeforeDeadline);
    }

    /**
     * Computes statistics from the given task list.
     *
     * @param tasks the list of tasks for the module
     * @return a {@link ModuleStats} record containing all computed values
     */
    private ModuleStats computeStats(List<Task> tasks) {
        int total = tasks.size();
        int completedOnTime = 0;
        int completedLate = 0;
        int active = 0;
        double totalDaysBeforeDeadline = 0.0;
        int countedForAvg = 0;

        for (Task task : tasks) {
            if (!task.isDone()) {
                active++;
                continue;
            }
            // Task is done — determine if on time or late
            LocalDateTime completedAt = task.getCompletedAt();
            if (task instanceof Deadline deadlineTask) {
                LocalDateTime dueDate = deadlineTask.getBy();
                if (completedAt != null) {
                    long daysBeforeDue = ChronoUnit.DAYS.between(completedAt, dueDate);
                    if (daysBeforeDue >= 0) {
                        completedOnTime++;
                    } else {
                        completedLate++;
                    }
                    totalDaysBeforeDeadline += daysBeforeDue;
                    countedForAvg++;
                } else {
                    // No timestamp: treat as on time (legacy data loaded before tracking)
                    completedOnTime++;
                }
            } else {
                // Todo tasks have no deadline — count as on time
                completedOnTime++;
            }
        }

        double avgDays = countedForAvg > 0 ? totalDaysBeforeDeadline / countedForAvg : Double.NaN;
        return new ModuleStats(total, completedOnTime, completedLate, active, avgDays);
    }

    /**
     * Value object holding the computed statistics for a module.
     */
    private static class ModuleStats {
        final int totalTasks;
        final int completedOnTime;
        final int completedLate;
        final int active;
        /** Average days before deadline for completed deadline tasks. {@link Double#NaN} if none. */
        final double avgDaysBeforeDeadline;

        ModuleStats(int totalTasks, int completedOnTime, int completedLate, int active,
                    double avgDaysBeforeDeadline) {
            this.totalTasks = totalTasks;
            this.completedOnTime = completedOnTime;
            this.completedLate = completedLate;
            this.active = active;
            this.avgDaysBeforeDeadline = avgDaysBeforeDeadline;
        }
    }
}

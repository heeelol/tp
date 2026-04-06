package seedu.modulesync.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import seedu.modulesync.module.Module;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.task.Deadline;
import seedu.modulesync.task.Task;

public class Ui {
    private final Scanner scanner;

    public Ui(Scanner scanner) {
        this.scanner = scanner;
    }

    public void showWelcome() {
        System.out.println("Welcome to ModuleSync");
        System.out.println("What would you like to do?");
    }

    public void showFarewell() {
        System.out.println("Bye. Hope to see you again!");
    }

    public String readCommand() {
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        }
        return "bye";
    }

    public void showError(String message) {
        System.out.println("Error: " + message);
    }

    public void showTaskAdded(Module module, Task task, int totalTasks) {
        System.out.println("Added task under " + module.getCode() + ":");
        System.out.println("  " + task.formatForList(module.getTasks().size()));
        System.out.println("Now tracking " + totalTasks + " task(s) across modules.");
    }

    public void showTaskList(ModuleBook moduleBook) {
        if (moduleBook.countTotalTasks() == 0) {
            System.out.println("No tasks found.");
            return;
        }

        System.out.println("Here are the tasks:");
        int taskNumber = 1;
        for (Module module : moduleBook.getModules()) {
            for (Task task : module.getTasks().asUnmodifiableList()) {
                System.out.println(task.formatForList(taskNumber));
                taskNumber++;
            }
        }
    }

    public void showTaskList(ModuleBook moduleBook, String moduleCode) {
        Module module = moduleBook.getModule(moduleCode);

        if (module == null) {
            System.out.println("No such module: " + moduleCode.toUpperCase() + ".");
            return;
        }

        if (module.getTasks().size() == 0) {
            System.out.println("No tasks found for module " + module.getCode() + ".");
            return;
        }

        System.out.println("Here are the tasks for " + module.getCode() + ":");

        int globalTaskNumber = 1;
        for (Module currentModule : moduleBook.getModules()) {
            for (Task task : currentModule.getTasks().asUnmodifiableList()) {
                if (currentModule.getCode().equals(module.getCode())) {
                    System.out.println(task.formatForList(globalTaskNumber));
                }
                globalTaskNumber++;
            }
        }
    }

    public void showNotDoneTaskList(ModuleBook moduleBook, String moduleCode) {
        assert moduleBook != null : "ModuleBook must not be null when listing not-done tasks";
        assert moduleCode != null && !moduleCode.isBlank() : "Module code must not be null/blank when listing not-done";

        if (moduleBook.countTotalTasks() == 0) {
            System.out.println("No tasks found.");
            return;
        }

        String targetModuleCode = moduleCode.toUpperCase();
        int globalTaskNumber = 1;
        boolean foundNotDoneTask = false;

        for (Module module : moduleBook.getModules()) {
            boolean isTargetModule = module.getCode().equals(targetModuleCode);
            for (Task task : module.getTasks().asUnmodifiableList()) {
                if (isTargetModule && !task.isDone()) {
                    if (!foundNotDoneTask) {
                        System.out.println("Here are the not done tasks for " + targetModuleCode + ":");
                        foundNotDoneTask = true;
                    }
                    System.out.println(task.formatForList(globalTaskNumber));
                }
                globalTaskNumber++;
            }
        }

        if (!foundNotDoneTask) {
            System.out.println("No not done tasks found for " + targetModuleCode + ".");
        }
    }

    /**
     * Displays all upcoming deadlines in chronological order.
     * Helps the user plan their week by showing deadlines organized by proximity.
     *
     * @param moduleBook the module book containing all modules and tasks
     */
    public void showDeadlineList(ModuleBook moduleBook) {
        assert moduleBook != null : "ModuleBook must not be null when calling showDeadlineList";
        
        // Collect all deadlines
        List<DeadlineEntry> deadlines = new ArrayList<>();
        int globalTaskNumber = 1;
        
        for (Module module : moduleBook.getModules()) {
            assert module != null : "Module retrieved from ModuleBook must not be null";
            for (Task task : module.getTasks().asUnmodifiableList()) {
                assert task != null : "Task retrieved from TaskList must not be null";
                if (task instanceof Deadline) {
                    Deadline deadline = (Deadline) task;
                    assert deadline.getBy() != null : "Deadline date must not be null";
                    deadlines.add(new DeadlineEntry(deadline, globalTaskNumber, module.getCode()));
                }
                globalTaskNumber++;
            }
        }

        if (deadlines.isEmpty()) {
            System.out.println("No deadlines found. Great job staying on top of your work!");
            return;
        }

        // Sort by deadline (earliest first)
        deadlines.sort((a, b) -> a.deadline.getBy().compareTo(b.deadline.getBy()));
        
        // Verify sorting: each deadline should be <= the next deadline
        for (int i = 0; i < deadlines.size() - 1; i++) {
            assert deadlines.get(i).deadline.getBy().compareTo(deadlines.get(i + 1).deadline.getBy()) <= 0
                    : "Deadlines must be sorted in ascending order by due date";
        }

        System.out.println("Here are the upcoming deadlines:");
        for (DeadlineEntry entry : deadlines) {
            System.out.println(entry.deadline.formatForList(entry.taskNumber));
        }
    }

    /**
     * Displays the top X most urgent tasks, sorted by weightage (descending) and then by deadline (ascending).
     * 
     * @param moduleBook the collection of modules and tasks
     * @param topCount   the number of top urgent tasks to display
     */
    public void showTopUrgentTasks(ModuleBook moduleBook, int topCount) {
        if (moduleBook.countTotalTasks() == 0) {
            System.out.println("No tasks found.");
            return;
        }

        List<UrgentTaskEntry> urgentTasks = new ArrayList<>();
        int globalTaskNumber = 1;

        // Collect all tasks with their metadata
        for (Module module : moduleBook.getModules()) {
            assert module != null : "Module retrieved from ModuleBook must not be null";
            for (Task task : module.getTasks().asUnmodifiableList()) {
                assert task != null : "Task retrieved from TaskList must not be null";
                int weightage = task.hasWeightage() ? task.getWeightage() : 0;
                long daysLeft = Long.MAX_VALUE;
                if (task instanceof Deadline) {
                    daysLeft = ((Deadline) task).getDaysLeft();
                }
                urgentTasks.add(new UrgentTaskEntry(task, globalTaskNumber, weightage, daysLeft));
                globalTaskNumber++;
            }
        }

        if (urgentTasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }

        // Sort by weightage (descending, highest first), then by days left (ascending, nearest first)
        urgentTasks.sort((a, b) -> {
            // Primary: Compare weightage (descending)
            int weightComparison = Integer.compare(b.weightage, a.weightage);
            if (weightComparison != 0) {
                return weightComparison;
            }
            // Secondary: Compare deadline proximity (ascending)
            return Long.compare(a.daysLeft, b.daysLeft);
        });

        // Verify sorting
        for (int i = 0; i < urgentTasks.size() - 1; i++) {
            int currentWeight = urgentTasks.get(i).weightage;
            int nextWeight = urgentTasks.get(i + 1).weightage;
            assert currentWeight >= nextWeight : "Weightage must be sorted in descending order";
            if (currentWeight == nextWeight) {
                assert urgentTasks.get(i).daysLeft <= urgentTasks.get(i + 1).daysLeft 
                        : "Tasks with same weightage must be sorted by deadline (nearest first)";
            }
        }

        // Display top X tasks
        int displayCount = Math.min(topCount, urgentTasks.size());
        System.out.println("Here are your top " + displayCount + " most urgent task(s):");
        for (int i = 0; i < displayCount; i++) {
            UrgentTaskEntry entry = urgentTasks.get(i);
            System.out.println(entry.task.formatForList(entry.taskNumber));
        }
    }

    /**
     * Helper class to track task information for urgency sorting.
     */
    private static class UrgentTaskEntry {
        final Task task;
        final int taskNumber;
        final int weightage;
        final long daysLeft;

        UrgentTaskEntry(Task task, int taskNumber, int weightage, long daysLeft) {
            this.task = task;
            this.taskNumber = taskNumber;
            this.weightage = weightage;
            this.daysLeft = daysLeft;
        }
    }

    /**
     * Helper class to track deadline information for sorting and display.
     */
    private static class DeadlineEntry {
        final Deadline deadline;
        final int taskNumber;
        final String moduleCode;

        DeadlineEntry(Deadline deadline, int taskNumber, String moduleCode) {
            this.deadline = deadline;
            this.taskNumber = taskNumber;
            this.moduleCode = moduleCode;
        }
    }

    public void showTaskMarked(Task task, int taskNumber) {
        System.out.println("Nice! I've marked this task as done:");
        System.out.println("  " + task.formatForList(taskNumber));
    }

    public void showTaskUnmarked(Task task, int taskNumber) {
        System.out.println("OK, I've marked this task as not done yet:");
        System.out.println("  " + task.formatForList(taskNumber));
    }

    /**
     * Displays a confirmation message after a task's weightage has been set or updated.
     *
     * @param task       the task whose weightage was changed
     * @param taskNumber the global display index of the task
     * @param previous   the previous weightage value, or null if none was set before
     */
    public void showWeightSet(Task task, int taskNumber, Integer previous) {
        assert task != null : "Task must not be null";
        assert taskNumber > 0 : "Task number must be positive";
        if (previous != null) {
            System.out.println("Updated weightage for task " + taskNumber
                    + " (was " + previous + "%, now " + task.getWeightage() + "%):");
        } else {
            System.out.println("Set weightage for task " + taskNumber + ":");
        }
        System.out.println("  " + task.formatForList(taskNumber));
    }

    public void showTaskDeleted(Task task, int totalTasks) {
        assert task != null : "Deleted task must not be null";
        assert totalTasks >= 0 : "Total task count must not be negative";
        System.out.println("Noted. I've removed this task:");
        System.out.println("  [" + task.getModuleCode() + "] " + task.formatForList(0));
        System.out.println("Now you have " + totalTasks + " task(s) in the list.");
    }

    public void showTaskDeadlineUpdated(Task task, int taskNumber) {
        assert task != null : "Updated task must not be null";
        assert taskNumber > 0 : "Task number must be positive";
        System.out.println("Got it. I've updated the deadline for this task:");
        System.out.println("  " + task.formatForList(taskNumber));
    }
}

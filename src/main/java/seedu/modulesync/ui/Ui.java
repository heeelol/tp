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

    public void showTaskDeleted(Task task, int totalTasks) {
        System.out.println("Noted. I've removed this task:");
        System.out.println("  [" + task.getModuleCode() + "] " + task.formatForList(0));
        System.out.println("Now you have " + totalTasks + " task(s) in the list.");
    }
}

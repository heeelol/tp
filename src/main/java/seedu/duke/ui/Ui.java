package seedu.duke.ui;

import java.util.Scanner;

import seedu.duke.module.Module;
import seedu.duke.module.ModuleBook;
import seedu.duke.task.Task;

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
        if (moduleBook.totalTaskCount() == 0) {
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

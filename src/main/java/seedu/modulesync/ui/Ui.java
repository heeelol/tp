package seedu.modulesync.ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import seedu.modulesync.module.Module;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.semester.Semester;
import seedu.modulesync.semester.SemesterBook;
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

    /**
     * Displays overdue warnings for incomplete tasks whose deadlines have already passed.
     *
     * @param moduleBook the active semester's module book
     */
    public void showStartupOverdueWarning(ModuleBook moduleBook) {
        assert moduleBook != null : "ModuleBook must not be null when showing startup overdue warnings";

        List<DeadlineEntry> overdueDeadlines = collectOverdueDeadlines(moduleBook);
        if (overdueDeadlines.isEmpty()) {
            return;
        }

        System.out.println("Overdue warning: " + overdueDeadlines.size()
                + " task(s) have passed their deadlines.");
        for (DeadlineEntry overdueDeadline : overdueDeadlines) {
            System.out.println(formatOverdueDeadline(overdueDeadline));
        }
    }

    /**
     * Displays same-day deadline conflicts to help the user spot crunch periods.
     *
     * @param moduleBook the active semester's module book
     */
    public void showDeadlineConflicts(ModuleBook moduleBook) {
        assert moduleBook != null : "ModuleBook must not be null when showing deadline conflicts";

        Map<LocalDate, List<DeadlineEntry>> conflictsByDate = collectDeadlineConflicts(moduleBook);
        if (conflictsByDate.isEmpty()) {
            System.out.println("No same-day deadline conflicts found.");
            return;
        }

        System.out.println("Here are your same-day deadline conflicts:");
        for (Map.Entry<LocalDate, List<DeadlineEntry>> conflictEntry : conflictsByDate.entrySet()) {
            LocalDate date = conflictEntry.getKey();
            List<DeadlineEntry> deadlines = conflictEntry.getValue();
            System.out.println(date + " (" + deadlines.size() + " deadlines)");
            for (DeadlineEntry deadlineEntry : deadlines) {
                System.out.println("  " + formatConflictDeadline(deadlineEntry));
            }
        }
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
     * Collects overdue deadlines from the active semester.
     *
     * @param moduleBook the module book to scan
     * @return the overdue deadline entries sorted by due date
     */
    private List<DeadlineEntry> collectOverdueDeadlines(ModuleBook moduleBook) {
        List<DeadlineEntry> overdueDeadlines = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        int globalTaskNumber = 1;

        for (Module module : moduleBook.getModules()) {
            for (Task task : module.getTasks().asUnmodifiableList()) {
                if (task instanceof Deadline) {
                    Deadline deadline = (Deadline) task;
                    if (!task.isDone() && deadline.getBy().isBefore(now)) {
                        overdueDeadlines.add(new DeadlineEntry(deadline, globalTaskNumber, module.getCode()));
                    }
                }
                globalTaskNumber++;
            }
        }
        return overdueDeadlines;
    }

    /**
     * Collects incomplete deadlines that share the same calendar day.
     *
     * @param moduleBook the module book to scan
     * @return a map of conflict dates to their deadline entries
     */
    private Map<LocalDate, List<DeadlineEntry>> collectDeadlineConflicts(ModuleBook moduleBook) {
        Map<LocalDate, List<DeadlineEntry>> deadlinesByDate = new TreeMap<>();
        int globalTaskNumber = 1;

        for (Module module : moduleBook.getModules()) {
            for (Task task : module.getTasks().asUnmodifiableList()) {
                if (task instanceof Deadline && !task.isDone()) {
                    Deadline deadline = (Deadline) task;
                    LocalDate dueDate = deadline.getBy().toLocalDate();
                    deadlinesByDate.computeIfAbsent(dueDate, ignored -> new ArrayList<>())
                            .add(new DeadlineEntry(deadline, globalTaskNumber, module.getCode()));
                }
                globalTaskNumber++;
            }
        }

        Map<LocalDate, List<DeadlineEntry>> conflictsByDate = new LinkedHashMap<>();
        for (Map.Entry<LocalDate, List<DeadlineEntry>> deadlineEntry : deadlinesByDate.entrySet()) {
            List<DeadlineEntry> deadlines = deadlineEntry.getValue();
            if (deadlines.size() > 1) {
                deadlines.sort((first, second) -> first.deadline.getBy().compareTo(second.deadline.getBy()));
                conflictsByDate.put(deadlineEntry.getKey(), deadlines);
            }
        }
        return conflictsByDate;
    }

    /**
     * Formats a single overdue deadline line for the startup warning.
     *
     * @param deadlineEntry the deadline entry to format
     * @return the formatted overdue warning line
     */
    private String formatOverdueDeadline(DeadlineEntry deadlineEntry) {
        assert deadlineEntry != null : "Deadline entry must not be null";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd yyyy, HH:mm");
        String dueDate = deadlineEntry.deadline.getBy().format(formatter);
        return deadlineEntry.taskNumber + ".[" + deadlineEntry.deadline.getModuleCode() + "] "
                + "[D][" + deadlineEntry.deadline.getStatusIcon() + "] "
                + deadlineEntry.deadline.getDescription() + " (was due: " + dueDate + ")";
    }

    /**
     * Formats a single deadline entry inside a same-day conflict group.
     *
     * @param deadlineEntry the conflict deadline entry to format
     * @return the formatted conflict line
     */
    private String formatConflictDeadline(DeadlineEntry deadlineEntry) {
        assert deadlineEntry != null : "Deadline entry must not be null";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String dueTime = deadlineEntry.deadline.getBy().format(formatter);
        return deadlineEntry.taskNumber + ".[" + deadlineEntry.deadline.getModuleCode() + "] "
                + "[D][" + deadlineEntry.deadline.getStatusIcon() + "] "
                + deadlineEntry.deadline.getDescription() + " (due: " + dueTime + ")";
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

    public void showNoTasksToMark(String moduleCode) {
        System.out.println("There are no tasks to mark in module " + moduleCode.toUpperCase() + ".");
    }

    public void showAllTasksMarked(String moduleCode, int taskCount) {
        System.out.println("Nice! I've marked all " + taskCount + " task(s) in " 
                + moduleCode.toUpperCase() + " as done.");
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

    /**
     * Displays task completion statistics for a module.
     *
     * @param moduleCode          the module code
     * @param total               total number of tasks
     * @param completedOnTime     count of tasks completed on time
     * @param completedLate       count of tasks completed late
     * @param active              count of currently active (not done) tasks
     * @param avgDaysBeforeDeadline average days before deadline at completion time (NaN if unavailable)
     */
    //@@author Huang-Hau-Shuan
    public void showModuleStats(String moduleCode, int total, int completedOnTime,
                                int completedLate, int active, double avgDaysBeforeDeadline) {
        assert moduleCode != null && !moduleCode.isBlank() : "Module code must not be blank for stats display";
        assert total >= 0 : "Total task count must not be negative";

        System.out.println("===== Stats for " + moduleCode + " =====");
        System.out.println("Total tasks created   : " + total);

        if (total == 0) {
            System.out.println("No tasks to compute statistics for.");
            return;
        }

        System.out.println(formatStatLine("Completed on time", completedOnTime, total));
        System.out.println(formatStatLine("Completed late   ", completedLate, total));
        System.out.println(formatStatLine("Currently active ", active, total));

        if (!Double.isNaN(avgDaysBeforeDeadline)) {
            System.out.printf("Avg completion time  : %.1f day(s) before deadline%n", avgDaysBeforeDeadline);
        } else {
            System.out.println("Avg completion time  : N/A (no completed deadline tasks)");
        }
    }

    /**
     * Displays a list of all registered modules currently tracked.
     */
    public void showModuleList(ModuleBook moduleBook) {
        assert moduleBook != null : "ModuleBook must not be null";

        if (moduleBook.getModules().isEmpty()) {
            System.out.println("No modules registered.");
            return;
        }

        System.out.println("Here are your registered modules:");
        int index = 1;
        for (Module module : moduleBook.getModules()) {
            System.out.println(index + ". " + module.getCode() + " (" + module.getTasks().size() + " task(s))");
            index++;
        }
    }

    /**
     * Formats a stat line with count and percentage.
     *
     * @param label the label for the stat
     * @param count the count for this category
     * @param total the total number of tasks
     * @return the formatted line
     */
    private String formatStatLine(String label, int count, int total) {
        double percent = total > 0 ? (count * 100.0 / total) : 0.0;
        return String.format("%s : %d (%.1f%%)", label, count, percent);
    }

    /**
     * Displays statistics across all modules, intended as semester-wide overview.
     */
    public void showSemesterStatistics(ModuleBook moduleBook) {
        assert moduleBook != null : "ModuleBook must not be null";

        int moduleCount = moduleBook.getModules().size();
        int totalTasks = 0;
        int doneTasks = 0;
        int todoCount = 0;
        int deadlineCount = 0;

        int weightedTaskCount = 0;
        int totalWeightage = 0;
        int doneWeightage = 0;

        for (Module module : moduleBook.getModules()) {
            for (Task task : module.getTasks().asUnmodifiableList()) {
                totalTasks++;
                if (task.isDone()) {
                    doneTasks++;
                }
                if (task instanceof Deadline) {
                    deadlineCount++;
                } else {
                    todoCount++;
                }
                if (task.hasWeightage()) {
                    weightedTaskCount++;
                    totalWeightage += task.getWeightage();
                    if (task.isDone()) {
                        doneWeightage += task.getWeightage();
                    }
                }
            }
        }

        if (moduleCount == 0) {
            System.out.println("No modules registered. Add tasks first to see semester statistics.");
            return;
        }

        System.out.println("Semester statistics:");
        System.out.println("Modules: " + moduleCount);

        int notDoneTasks = totalTasks - doneTasks;
        int completionPercent = totalTasks == 0 ? 0 : (doneTasks * 100) / totalTasks;
        System.out.println("Tasks: " + totalTasks + " total | " + doneTasks + " done | " + notDoneTasks
                + " not done | Completion: " + completionPercent + "%");
        System.out.println("Types: " + todoCount + " todo(s) | " + deadlineCount + " deadline(s)");

        if (totalWeightage > 0) {
            int weightedCompletion = (doneWeightage * 100) / totalWeightage;
            System.out.println("Weightage: " + doneWeightage + "/" + totalWeightage + " completed ("
                    + weightedCompletion + "%) across " + weightedTaskCount + " weighted task(s)");
        } else {
            System.out.println("Weightage: n/a (no weightage set)");
        }

        System.out.println("Work distribution by module:");
        int index = 1;
        for (Module module : moduleBook.getModules()) {
            int moduleTotal = module.getTasks().size();
            int moduleDone = 0;
            int moduleWeightTotal = 0;
            int moduleWeightDone = 0;

            for (Task task : module.getTasks().asUnmodifiableList()) {
                if (task.isDone()) {
                    moduleDone++;
                }
                if (task.hasWeightage()) {
                    moduleWeightTotal += task.getWeightage();
                    if (task.isDone()) {
                        moduleWeightDone += task.getWeightage();
                    }
                }
            }

            String weightSummary = moduleWeightTotal > 0
                    ? ("weightage " + moduleWeightDone + "/" + moduleWeightTotal)
                    : "weightage n/a";

            System.out.println(index + ". " + module.getCode() + ": " + moduleTotal + " task(s) | done "
                    + moduleDone + " | " + weightSummary);
            index++;
        }
    }

    // -------------------------------------------------------------------------
    // Semester-aware UI methods (for teammates' semester commands)
    // -------------------------------------------------------------------------

    /**
     * Displays the currently active semester on startup or on request.
     *
     * @param semesterBook the semester book containing the active semester
     */
    //@@author Huang-Hau-Shuan
    public void showCurrentSemester(SemesterBook semesterBook) {
        String name = semesterBook.getCurrentSemesterName();
        if (name == null) {
            System.out.println("No active semester. Use 'semester switch SEMESTER_NAME' to begin.");
            return;
        }
        try {
            boolean readOnly = semesterBook.isCurrentSemesterReadOnly();
            System.out.println("Active semester: " + name + (readOnly ? " [archived — read-only]" : ""));
        } catch (seedu.modulesync.exception.ModuleSyncException e) {
            System.out.println("Active semester: " + name);
        }
    }

    /**
     * Displays a rejection message when a mutating command is attempted in a read-only semester.
     *
     * @param semesterName the name of the currently archived semester
     */
    public void showReadOnlySemesterError(String semesterName) {
        assert semesterName != null : "Semester name must not be null for read-only error";
        System.out.println("Error: Semester '" + semesterName
                + "' is archived and read-only. Use 'semester switch SEMESTER_NAME' to switch "
                + "to an active semester, or unarchive this one first.");
    }

    /**
     * Displays a confirmation after switching to (or creating) a semester.
     *
     * @param semesterName the semester switched to
     * @param created      {@code true} if a new semester was created; {@code false} if existing
     */
    public void showSemesterSwitched(String semesterName, boolean created) {
        assert semesterName != null && !semesterName.isBlank() : "Semester name must not be blank";
        if (created) {
            System.out.println("Created and switched to new semester: " + semesterName);
        } else {
            System.out.println("Switched to semester: " + semesterName);
        }
    }

    /**
     * Displays a confirmation after archiving the current semester.
     *
     * @param semesterName the name of the archived semester
     */
    public void showSemesterArchived(String semesterName) {
        assert semesterName != null : "Semester name must not be null";
        System.out.println("Semester '" + semesterName + "' has been archived. It is now read-only.");
        System.out.println("Use 'semester switch SEMESTER_NAME' to move to another semester.");
    }

    /**
     * Displays a confirmation after unarchiving a semester.
     *
     * @param semesterName the name of the unarchived semester
     */
    public void showSemesterUnarchived(String semesterName) {
        assert semesterName != null : "Semester name must not be null";
        System.out.println("Semester '" + semesterName + "' has been unarchived and is now editable.");
    }

    /**
     * Displays all semesters registered in the semester book,
     * indicating which is active and which are archived.
     *
     * @param semesterBook the semester book to display
     */
    public void showSemesterList(SemesterBook semesterBook) {
        assert semesterBook != null : "SemesterBook must not be null";
        if (!semesterBook.hasSemesters()) {
            System.out.println("No semesters are currently tracked.");
            return;
        }
        String currentName = semesterBook.getCurrentSemesterName();
        System.out.println("Your semesters:");
        int index = 1;
        for (Semester semester : semesterBook.getAllSemesters()) {
            String marker = semester.getName().equals(currentName) ? " <- current" : "";
            String status = semester.isArchived() ? "[Archived]" : "[Active]";
            System.out.println(index + ". " + semester.getName() + " " + status + marker);
            index++;
        }
    }
}

package seedu.modulesync.ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import seedu.modulesync.grade.GradeHistorySummary;
import seedu.modulesync.grade.SemesterGradeSummary;
import seedu.modulesync.module.Module;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.semester.Semester;
import seedu.modulesync.semester.SemesterBook;
import seedu.modulesync.task.Deadline;
import seedu.modulesync.task.Task;

public class Ui {
    private static final int DEADLINE_BUCKET_UPCOMING = 0;
    private static final int DEADLINE_BUCKET_DUE_TODAY = 1;
    private static final int DEADLINE_BUCKET_OVERDUE = 2;
    private static final String NO_GRADES_FOUND_MESSAGE =
            "No recorded grades found. A grade summary cannot be generated yet.";
    private static final int MODULE_COLUMN_WIDTH = 8;
    private static final int CREDITS_COLUMN_WIDTH = 8;
    private static final int GRADE_COLUMN_WIDTH = 7;

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

    /**
     * Displays a summary of all available commands.
     */
    public void showHelp() {
        System.out.println("Here are the available commands:");
        System.out.println();
        System.out.println("--- Task Management ---");
        System.out.println("  add /mod MOD /task DESCRIPTION [/due YYYY-MM-DD[-HHmm]] [/w PERCENT]");
        System.out.println("      Add a task (todo or deadline) under a module.");
        System.out.println("  delete TASK_NUMBER");
        System.out.println("      Delete a task by its list index.");
        System.out.println("  mark TASK_NUMBER");
        System.out.println("      Mark a task as done.");
        System.out.println("  mark /mod MOD /all");
        System.out.println("      Mark all tasks in a module as done.");
        System.out.println("  unmark TASK_NUMBER");
        System.out.println("      Mark a task as not done.");
        System.out.println("  setweight TASK_NUMBER PERCENT");
        System.out.println("      Set the weightage of a task (whole number 0-100, no % symbol).");
        System.out.println("  editweight TASK_NUMBER /w PERCENT");
        System.out.println("      Edit the weightage of a task (whole number 0-100, no % symbol).");
        System.out.println("  setdeadline TASK_NUMBER /by YYYY-MM-DD[-HHmm]");
        System.out.println("      Set or update the deadline of a task.");
        System.out.println("  editdeadline TASK_NUMBER /by YYYY-MM-DD[-HHmm]");
        System.out.println("      Edit the deadline of a task.");
        System.out.println();
        System.out.println("--- Listing ---");
        System.out.println("  list");
        System.out.println("      List all tasks.");
        System.out.println("  list /mod MOD");
        System.out.println("      List tasks for a specific module.");
        System.out.println("  list /notdone /mod MOD");
        System.out.println("      List incomplete tasks for a module.");
        System.out.println("  list /deadlines");
        System.out.println("      List all deadlines grouped by urgency.");
        System.out.println("  list /top N");
        System.out.println("      List the top N tasks by priority score (urgency + weightage).");
        System.out.println();
        System.out.println("--- Modules ---");
        System.out.println("  add /mod MOD");
        System.out.println("      Register a module without adding a task.");
        System.out.println("  module delete /mod MOD");
        System.out.println("      Delete a module and all its tasks.");
        System.out.println("  module archive /mod MOD");
        System.out.println("      Archive a module (hides it from list views).");
        System.out.println("  module unarchive /mod MOD");
        System.out.println("      Restore an archived module.");
        System.out.println("  module list");
        System.out.println("      List all modules in the current semester.");
        System.out.println("  setcredits /mod MOD /mc CREDITS");
        System.out.println("      Set the MCs (modular credits) for a module.");
        System.out.println();
        System.out.println("--- Grades ---");
        System.out.println("  grade /mod MOD /grade GRADE");
        System.out.println("      Record a grade for a module (e.g. A+, B, CS).");
        System.out.println("  grades list");
        System.out.println("      Show grade history across all semesters.");
        System.out.println("  cap");
        System.out.println("      Display your current and cumulative CAP.");
        System.out.println();
        System.out.println("--- Stats ---");
        System.out.println("  stats /mod MOD");
        System.out.println("      Show task completion statistics for a module.");
        System.out.println("  semester stats");
        System.out.println("      Show statistics for the current semester.");
        System.out.println();
        System.out.println("--- Checks ---");
        System.out.println("  check /conflicts");
        System.out.println("      Show same-day deadline conflicts.");
        System.out.println("  check /urgent");
        System.out.println("      Show incomplete tasks due within the next 48 hours.");
        System.out.println();
        System.out.println("--- Semester Management ---");
        System.out.println("  semester new NAME");
        System.out.println("      Create and switch to a new semester.");
        System.out.println("  semester switch NAME");
        System.out.println("      Switch to an existing semester.");
        System.out.println("  semester list");
        System.out.println("      List all semesters.");
        System.out.println("  semester archive");
        System.out.println("      Archive the current semester (makes it read-only).");
        System.out.println("  semester unarchive");
        System.out.println("      Unarchive the current semester.");
        System.out.println();
        System.out.println("--- General ---");
        System.out.println("  help");
        System.out.println("      Show this help message.");
        System.out.println("  bye");
        System.out.println("      Exit the application.");
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

    public void showMessage(String message) {
        System.out.println(message);
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
            // Skip archived modules in the main task list to keep workspace clean
            if (module.isArchived()) {
                continue;
            }
            for (Task task : module.getTasks().asUnmodifiableList()) {
                showTaskWithPriority(task, taskNumber);
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
                    showTaskWithPriority(task, globalTaskNumber);
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
                    showTaskWithPriority(task, globalTaskNumber);
                }
                globalTaskNumber++;
            }
        }

        if (!foundNotDoneTask) {
            System.out.println("No not done tasks found for " + targetModuleCode + ".");
        }
    }

    /**
     * Displays deadlines grouped for actionability.
     * Ordering is: upcoming (future), due today, then overdue.
     * Within each bucket, entries are sorted by due date and then task index.
     *
     * @param moduleBook the module book containing all modules and tasks
     */
    public void showDeadlineList(ModuleBook moduleBook) {
        assert moduleBook != null : "ModuleBook must not be null when calling showDeadlineList";
        
        // Collect all deadlines
        List<DeadlineEntry> deadlines = new ArrayList<>();
        int globalTaskNumber = 1;
        
        for (Module module : moduleBook.getModules()) {
            // Skip archived modules to keep workspace clean
            if (module.isArchived()) {
                continue;
            }
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

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        List<DeadlineEntry> upcomingDeadlines = new ArrayList<>();
        List<DeadlineEntry> todayDeadlines = new ArrayList<>();
        List<DeadlineEntry> overdueDeadlines = new ArrayList<>();

        for (DeadlineEntry entry : deadlines) {
            int bucket = classifyDeadlineBucket(entry.deadline, now, today);
            if (bucket == DEADLINE_BUCKET_UPCOMING) {
                upcomingDeadlines.add(entry);
            } else if (bucket == DEADLINE_BUCKET_DUE_TODAY) {
                todayDeadlines.add(entry);
            } else {
                overdueDeadlines.add(entry);
            }
        }

        Comparator<DeadlineEntry> ascendingByDueThenTaskNumber =
                Comparator.comparing((DeadlineEntry e) -> e.deadline.getBy())
                        .thenComparingInt(e -> e.taskNumber);
        Comparator<DeadlineEntry> overdueByMostRecentThenTaskNumber =
                (first, second) -> {
                    int byComparison = second.deadline.getBy().compareTo(first.deadline.getBy());
                    if (byComparison != 0) {
                        return byComparison;
                    }
                    return Integer.compare(first.taskNumber, second.taskNumber);
                };

        upcomingDeadlines.sort(ascendingByDueThenTaskNumber);
        todayDeadlines.sort(ascendingByDueThenTaskNumber);
        overdueDeadlines.sort(overdueByMostRecentThenTaskNumber);

        deadlines.clear();
        deadlines.addAll(upcomingDeadlines);
        deadlines.addAll(todayDeadlines);
        deadlines.addAll(overdueDeadlines);

        // Verify grouping and ordering contract for long-term maintenance safety.
        for (int i = 0; i < deadlines.size() - 1; i++) {
            DeadlineEntry current = deadlines.get(i);
            DeadlineEntry next = deadlines.get(i + 1);

            int currentBucket = classifyDeadlineBucket(current.deadline, now, today);
            int nextBucket = classifyDeadlineBucket(next.deadline, now, today);
            assert currentBucket <= nextBucket : "Deadline buckets must be ordered as upcoming, today, overdue";

            if (currentBucket == nextBucket) {
                int comparison = currentBucket == DEADLINE_BUCKET_OVERDUE
                        ? overdueByMostRecentThenTaskNumber.compare(current, next)
                        : ascendingByDueThenTaskNumber.compare(current, next);
                assert comparison <= 0 : "Deadlines within each bucket must be consistently ordered";
            }
        }

        System.out.println("Here are your deadlines (upcoming, due today, then overdue):");
        for (DeadlineEntry entry : deadlines) {
            showTaskWithPriority(entry.deadline, entry.taskNumber);
        }
    }

    /**
     * Classifies a deadline into display buckets.
     */
    private int classifyDeadlineBucket(Deadline deadline, LocalDateTime now, LocalDate today) {
        LocalDateTime dueDateTime = deadline.getBy();
        if (dueDateTime.isBefore(now)) {
            return DEADLINE_BUCKET_OVERDUE;
        }
        if (dueDateTime.toLocalDate().isEqual(today)) {
            return DEADLINE_BUCKET_DUE_TODAY;
        }
        return DEADLINE_BUCKET_UPCOMING;
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
     * Displays the top X most urgent tasks, sorted by priority score and then by deadline proximity.
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
                int priorityScore = task.calculatePriorityScore();
                LocalDateTime dueDate = extractDueDate(task);
                urgentTasks.add(new UrgentTaskEntry(task, globalTaskNumber, priorityScore, dueDate));
                globalTaskNumber++;
            }
        }

        if (urgentTasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }

        // Sort by priority score (descending, highest first), then by due date (ascending, nearest first)
        urgentTasks.sort((a, b) -> {
            int priorityComparison = Integer.compare(b.priorityScore, a.priorityScore);
            if (priorityComparison != 0) {
                return priorityComparison;
            }
            int dueDateComparison = compareDueDates(a.dueDate, b.dueDate);
            if (dueDateComparison != 0) {
                return dueDateComparison;
            }
            return Integer.compare(a.taskNumber, b.taskNumber);
        });

        // Verify sorting
        for (int i = 0; i < urgentTasks.size() - 1; i++) {
            UrgentTaskEntry currentEntry = urgentTasks.get(i);
            UrgentTaskEntry nextEntry = urgentTasks.get(i + 1);
            assert currentEntry.priorityScore >= nextEntry.priorityScore
                    : "Priority score must be sorted in descending order";
            if (currentEntry.priorityScore == nextEntry.priorityScore) {
                assert compareDueDates(currentEntry.dueDate, nextEntry.dueDate) <= 0
                        : "Tasks with the same priority score must be sorted by due date";
            }
        }

        // Display top X tasks
        int displayCount = Math.min(topCount, urgentTasks.size());
        System.out.println("Here are your top " + displayCount + " most urgent task(s):");
        for (int i = 0; i < displayCount; i++) {
            UrgentTaskEntry entry = urgentTasks.get(i);
            showTaskWithPriority(entry.task, entry.taskNumber);
        }
    }

    /**
     * Extracts a task's due date for urgency tie-breaking.
     *
     * @param task the task to inspect
     * @return the due date if the task is a deadline, or {@code null} otherwise
     */
    private LocalDateTime extractDueDate(Task task) {
        assert task != null : "Task must not be null when extracting due date";
        if (task instanceof Deadline) {
            Deadline deadline = (Deadline) task;
            return deadline.getBy();
        }
        return null;
    }

    /**
     * Compares two due dates for urgency tie-breaking.
     *
     * @param firstDueDate the first due date
     * @param secondDueDate the second due date
     * @return a negative value if the first due date is earlier, a positive value if later, or zero if tied
     */
    private int compareDueDates(LocalDateTime firstDueDate, LocalDateTime secondDueDate) {
        if (firstDueDate == null && secondDueDate == null) {
            return 0;
        }
        if (firstDueDate == null) {
            return 1;
        }
        if (secondDueDate == null) {
            return -1;
        }
        return firstDueDate.compareTo(secondDueDate);
    }

    /**
     * Prints a task line together with its calculated priority score.
     *
     * @param task the task to print
     * @param taskNumber the global display index of the task
     */
    private void showTaskWithPriority(Task task, int taskNumber) {
        assert task != null : "Task must not be null when showing a list entry";
        assert taskNumber > 0 : "Task number must be positive when showing a list entry";
        System.out.println(task.formatForListWithPriority(taskNumber));
    }

    /**
     * Helper class to track task information for urgency sorting.
     */
    private static class UrgentTaskEntry {
        final Task task;
        final int taskNumber;
        final int priorityScore;
        final LocalDateTime dueDate;

        UrgentTaskEntry(Task task, int taskNumber, int priorityScore, LocalDateTime dueDate) {
            this.task = task;
            this.taskNumber = taskNumber;
            this.priorityScore = priorityScore;
            this.dueDate = dueDate;
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
        System.out.println("  " + task.formatForDisplay());
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
            String status = module.isArchived() ? " [archived]" : "";
            String grade = module.hasGrade() ? " [Grade: " + module.getGrade() + "]" : "";
            String moduleInfo = index + ". " + module.getCode() + " (" 
                    + module.getTasks().size() + " task(s))" + grade + status;
            System.out.println(moduleInfo);
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
        if (moduleCount == 0) {
            System.out.println("No modules registered. Add tasks first to see semester statistics.");
            return;
        }

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
            ModuleStatLine line = computeModuleStatLine(module);
            System.out.println(index + ". " + module.getCode() + ": " + line.total + " task(s) | done "
                    + line.done + " | " + line.weightSummary);
            index++;
        }
    }

    /**
     * Computes the per-module statistics needed for the work-distribution line.
     *
     * @param module the module to inspect
     * @return a {@link ModuleStatLine} with task counts and a formatted weight summary
     */
    private ModuleStatLine computeModuleStatLine(Module module) {
        assert module != null : "Module must not be null when computing stat line";
        int total = module.getTasks().size();
        int done = 0;
        int weightTotal = 0;
        int weightDone = 0;

        for (Task task : module.getTasks().asUnmodifiableList()) {
            if (task.isDone()) {
                done++;
            }
            if (task.hasWeightage()) {
                weightTotal += task.getWeightage();
                if (task.isDone()) {
                    weightDone += task.getWeightage();
                }
            }
        }

        String weightSummary = weightTotal > 0
                ? ("weightage " + weightDone + "/" + weightTotal)
                : "weightage n/a";
        return new ModuleStatLine(total, done, weightSummary);
    }

    /**
     * Value object holding the computed per-module statistics for the work-distribution display.
     */
    private static class ModuleStatLine {
        final int total;
        final int done;
        final String weightSummary;

        ModuleStatLine(int total, int done, String weightSummary) {
            this.total = total;
            this.done = done;
            this.weightSummary = weightSummary;
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
     * Displays the semester currently being viewed after a switch.
     *
     * @param semesterName the semester now being viewed
     * @param isReadOnly whether the viewed semester is read-only
     * @param previousSemesterName the previously active semester name, if any
     */
    public void showSemesterViewChanged(String semesterName, boolean isReadOnly, String previousSemesterName) {
        assert semesterName != null && !semesterName.isBlank() : "Semester name must not be blank";

        if (!isReadOnly) {
            System.out.println("Now viewing " + semesterName + ".");
            return;
        }

        String message = "Now viewing " + semesterName + " [read-only].";
        if (previousSemesterName != null
                && !previousSemesterName.isBlank()
                && !previousSemesterName.equals(semesterName)) {
            message += " Use 'semester switch " + previousSemesterName + "' to return.";
        }
        System.out.println(message);
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

    public void showCreditsSet(String moduleCode, int credits) {
        System.out.println("Credits for module " + moduleCode.toUpperCase() + " set to " + credits + ".");
    }

    public void showHighSemesterCreditsWarning(int totalCredits) {
        System.out.println("Warning: You now have " + totalCredits + " total modular credits for this semester. "
                + "Please check if this is correct.");
    }

    public void showModuleAdded(String moduleCode) {
        System.out.println("Module " + moduleCode.toUpperCase() + " has been successfully added to your list!");
    }

    public void showModuleDeleted(String moduleCode) {
        System.out.println("Module " + moduleCode.toUpperCase() + " has been cleanly removed from your list!");
    }

    /**
     * Displays the calculated Semester and Cumulative CAP.
     *
     * @param semPoints  total grade points for the current semester
     * @param semCredits total graded credits for the current semester
     * @param cumPoints  total grade points overall
     * @param cumCredits total graded credits overall
     */
    public void showCap(double semPoints, int semCredits, double cumPoints, int cumCredits) {
        if (cumCredits == 0) {
            System.out.println("No graded modules found. A CAP cannot be calculated yet.");
            return;
        }

        System.out.println("Here is your CAP summary:");
        
        if (semCredits > 0) {
            System.out.printf("Current Semester CAP: %.2f%n", semPoints / semCredits);
        } else {
            System.out.println("Current Semester CAP: N/A (no graded modules this semester)");
        }

        System.out.printf("Cumulative CAP: %.2f%n", cumPoints / cumCredits);
    }

    /**
     * Displays the student's grades and cumulative academic progress across semesters.
     *
     * @param gradeHistorySummary the student's grade history summary
     */
    public void showGradeSummary(GradeHistorySummary gradeHistorySummary) {
        assert gradeHistorySummary != null : "Grade history summary must not be null";

        if (!gradeHistorySummary.hasSemesterGradeSummaries()) {
            System.out.println(NO_GRADES_FOUND_MESSAGE);
            return;
        }

        boolean isFirstSemester = true;
        for (SemesterGradeSummary semesterGradeSummary : gradeHistorySummary.getSemesterGradeSummaries()) {
            if (!isFirstSemester) {
                System.out.println();
            }

            System.out.println(formatGradeSummaryTitle(semesterGradeSummary));
            System.out.println(formatGradeHeaderRow());
            for (SemesterGradeSummary.ModuleGradeEntry moduleGradeEntry
                    : semesterGradeSummary.getModuleGradeEntries()) {
                System.out.println(formatGradeEntryRow(moduleGradeEntry));
            }
            System.out.println();
            System.out.println(formatSemesterCapLine(semesterGradeSummary));
            System.out.println(formatCumulativeCapLine(semesterGradeSummary));
            isFirstSemester = false;
        }
    }

    /**
     * Formats the title line for one semester's grade summary.
     *
     * @param semesterGradeSummary the semester grade summary
     * @return the title line for one semester's grade summary
     */
    private String formatGradeSummaryTitle(SemesterGradeSummary semesterGradeSummary) {
        assert semesterGradeSummary != null : "Semester grade summary must not be null when formatting title";

        String title = semesterGradeSummary.getSemesterName() + " Results";
        if (semesterGradeSummary.isArchivedSemester()) {
            return title + " (Archived)";
        }
        if (semesterGradeSummary.isCurrentSemester()) {
            return title + " (Current)";
        }
        return title;
    }

    /**
     * Formats the table header row for one semester's module grades.
     *
     * @return the table header row for one semester's module grades
     */
    private String formatGradeHeaderRow() {
        return String.format(Locale.US, "%-" + MODULE_COLUMN_WIDTH + "s %-" + CREDITS_COLUMN_WIDTH
                        + "s %-" + GRADE_COLUMN_WIDTH + "s %s",
                "Module", "Credits", "Grade", "Points");
    }

    /**
     * Formats one module-grade entry for one semester's table.
     *
     * @param moduleGradeEntry the module-grade entry to format
     * @return the formatted module-grade entry row
     */
    private String formatGradeEntryRow(SemesterGradeSummary.ModuleGradeEntry moduleGradeEntry) {
        assert moduleGradeEntry != null : "Module grade entry must not be null when formatting a grade row";

        return String.format(Locale.US, "%-" + MODULE_COLUMN_WIDTH + "s %-" + CREDITS_COLUMN_WIDTH
                        + "d %-" + GRADE_COLUMN_WIDTH + "s %s",
                moduleGradeEntry.getModuleCode(),
                moduleGradeEntry.getCredits(),
                moduleGradeEntry.getGrade(),
                formatGradePoint(moduleGradeEntry));
    }

    /**
     * Formats the semester CAP line for one semester summary.
     *
     * @param semesterGradeSummary the semester grade summary
     * @return the semester CAP line for the semester summary
     */
    private String formatSemesterCapLine(SemesterGradeSummary semesterGradeSummary) {
        assert semesterGradeSummary != null : "Semester grade summary must not be null when formatting semester CAP";

        if (!semesterGradeSummary.hasSemesterCap()) {
            return "Semester CAP: N/A (no CAP-bearing modules)";
        }
        return "Semester CAP: " + formatCapValue(semesterGradeSummary.getSemesterCap())
                + " (" + semesterGradeSummary.getSemesterCredits() + " MCs)";
    }

    /**
     * Formats the cumulative CAP line for one semester summary.
     *
     * @param semesterGradeSummary the semester grade summary
     * @return the cumulative CAP line for the semester summary
     */
    private String formatCumulativeCapLine(SemesterGradeSummary semesterGradeSummary) {
        assert semesterGradeSummary != null
                : "Semester grade summary must not be null when formatting cumulative CAP";

        if (!semesterGradeSummary.hasCumulativeCap()) {
            if (semesterGradeSummary.isArchivedSemester()) {
                return "Cumulative CAP at archive: N/A (no CAP-bearing modules)";
            }
            return "Cumulative CAP: N/A (no CAP-bearing modules)";
        }
        if (semesterGradeSummary.isArchivedSemester()) {
            return "Cumulative CAP at archive: " + formatCapValue(semesterGradeSummary.getCumulativeCap())
                    + " (" + semesterGradeSummary.getCumulativeCredits() + " MCs)";
        }
        return "Cumulative CAP: " + formatCapValue(semesterGradeSummary.getCumulativeCap())
                + " (" + semesterGradeSummary.getCumulativeCredits() + " MCs across "
                + semesterGradeSummary.getCumulativeRecordedSemesterCount() + " "
                + formatSemesterLabel(semesterGradeSummary.getCumulativeRecordedSemesterCount()) + ")";
    }

    /**
     * Formats the display value for one module's grade point.
     *
     * @param moduleGradeEntry the module-grade entry to inspect
     * @return the display value for one module's grade point
     */
    private String formatGradePoint(SemesterGradeSummary.ModuleGradeEntry moduleGradeEntry) {
        assert moduleGradeEntry != null : "Module grade entry must not be null when formatting grade points";

        if (!moduleGradeEntry.hasGradePoint()) {
            return "N/A";
        }
        return String.format(Locale.US, "%.1f", moduleGradeEntry.getGradePoint());
    }

    /**
     * Formats a CAP value using two decimal places.
     *
     * @param capValue the CAP value to format
     * @return the CAP value formatted using two decimal places
     */
    private String formatCapValue(double capValue) {
        return String.format(Locale.US, "%.2f", capValue);
    }

    /**
     * Formats the singular or plural semester label used in the cumulative line.
     *
     * @param semesterCount the number of semesters to describe
     * @return the singular or plural semester label
     */
    private String formatSemesterLabel(int semesterCount) {
        if (semesterCount == 1) {
            return "semester";
        }
        return "semesters";
    }
}

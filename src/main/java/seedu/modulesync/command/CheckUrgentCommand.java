package seedu.modulesync.command;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.Module;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.task.Deadline;
import seedu.modulesync.task.Task;
import seedu.modulesync.ui.Ui;

/**
 * Represents a command to check for urgent tasks due within the next 48 hours.
 */
public class CheckUrgentCommand extends Command {

    public static final String COMMAND_WORD = "check /urgent";
    public static final String ALT_COMMAND_WORD = "/urgent";

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.plusHours(48);

        List<Deadline> urgentTasks = new ArrayList<>();

        for (Module module : moduleBook.getModules()) {
            for (Task task : module.getTasks().asUnmodifiableList()) {
                if (!task.isDone() && task instanceof Deadline) {
                    Deadline deadline = (Deadline) task;
                    LocalDateTime due = deadline.getBy();
                    if (!due.isBefore(now) && !due.isAfter(limit)) {
                        urgentTasks.add(deadline);
                    }
                }
            }
        }

        urgentTasks.sort(Comparator.comparing(Deadline::getBy));

        System.out.println("⚠️ URGENT: " + urgentTasks.size() + " tasks due in next 48 hours");
        int count = 1;
        for (Deadline task : urgentTasks) {
            String formattedDate = task.getBy().format(DateTimeFormatter.ofPattern("MMM dd yyyy, HH:mm"));
            long hoursLeft = ChronoUnit.HOURS.between(now, task.getBy());
            int weight = task.hasWeightage() ? task.getWeightage() : 0;
            
            System.out.println(count + ". " + task.getDescription() + "/" + task.getModuleCode());
            System.out.println("   └── Due: " + formattedDate + " (in " + hoursLeft + " hours)");
            System.out.println("   └── Weight: " + weight + "% | Status: Incomplete");
            count++;
        }
    }
}

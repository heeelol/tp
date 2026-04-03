package seedu.modulesync.command;

import java.util.logging.Logger;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.task.Task;
import seedu.modulesync.ui.Ui;

//@@author Huang-Hau-Shuan
/**
 * Command that assigns or updates the weightage of an existing task,
 * identified by its global display index shown in {@code list}.
 */
public class SetWeightCommand extends Command {
    private static final Logger LOGGER = Logger.getLogger(SetWeightCommand.class.getName());

    private final int taskNumber;
    private final int weightage;

    /**
     * Constructs a SetWeightCommand.
     *
     * @param taskNumber the 1-based global display index of the task to update
     * @param weightage  the weightage to assign (0–100)
     */
    public SetWeightCommand(int taskNumber, int weightage) {
        assert taskNumber > 0 : "Task number must be strictly positive";
        assert weightage >= 0 && weightage <= 100 : "Weightage must be between 0 and 100";
        this.taskNumber = taskNumber;
        this.weightage = weightage;
    }

    /**
     * Executes the set-weight command by updating the weightage of the specified task,
     * persisting the change, and displaying a confirmation message.
     *
     * @param moduleBook the module book containing all tasks
     * @param storage    the storage to save to
     * @param ui         the UI to display feedback
     * @throws ModuleSyncException if the task number is invalid
     */
    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        assert moduleBook != null : "ModuleBook must not be null";
        assert storage != null : "Storage must not be null";
        assert ui != null : "Ui must not be null";

        Task task = moduleBook.getTaskByDisplayIndex(taskNumber);
        assert task != null : "getTaskByDisplayIndex must return a non-null task for a valid index";

        Integer previous = task.getWeightage();
        LOGGER.fine(() -> "Setting weightage for task " + taskNumber + " to " + weightage + "%"
                + (previous != null ? " (was " + previous + "%)" : ""));

        task.setWeightage(weightage);
        storage.save(moduleBook);

        LOGGER.fine(() -> "Weightage updated and saved for task " + taskNumber);
        ui.showWeightSet(task, taskNumber, previous);
    }
}

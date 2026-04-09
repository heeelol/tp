package seedu.modulesync.command;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.task.Task;
import seedu.modulesync.ui.Ui;

/**
 * Command to edit the weightage of an existing task.
 */
public class EditWeightCommand extends Command {
    private final int taskNumber;
    private final int weightage;

    public EditWeightCommand(int taskNumber, int weightage) {
        assert taskNumber > 0 : "Task number must be strictly positive";
        assert weightage >= 0 && weightage <= 100 : "Weightage must be between 0 and 100";
        this.taskNumber = taskNumber;
        this.weightage = weightage;
    }

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        Task task = moduleBook.getTaskByDisplayIndex(taskNumber);
        
        Integer previous = task.getWeightage();
        task.setWeightage(weightage);
        ui.showWeightSet(task, taskNumber, previous);
        storage.save(moduleBook);
    }
}

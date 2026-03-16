package seedu.duke.command;

import seedu.duke.exception.ModuleSyncException;
import seedu.duke.module.ModuleBook;
import seedu.duke.storage.Storage;
import seedu.duke.task.Task;
import seedu.duke.ui.Ui;

public class UnmarkCommand extends Command {
    private final int taskNumber;

    public UnmarkCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        Task task = moduleBook.getTaskByDisplayIndex(taskNumber);
        task.markUndone();
        storage.save(moduleBook);
        ui.showTaskUnmarked(task, taskNumber);
    }
}

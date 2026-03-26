package seedu.modulesync.command;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.task.Task;
import seedu.modulesync.ui.Ui;

public class MarkCommand extends Command {
    private final int taskNumber;

    public MarkCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        Task task = moduleBook.getTaskByDisplayIndex(taskNumber);
        task.markDone();
        storage.save(moduleBook);
        ui.showTaskMarked(task, taskNumber);
    }
}

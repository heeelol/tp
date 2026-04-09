package seedu.modulesync.command;

import java.time.LocalDateTime;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.task.Task;
import seedu.modulesync.ui.Ui;

/**
 * Command to edit the deadline of an existing task.
 */
public class EditDeadlineCommand extends Command {
    private final int taskNumber;
    private final LocalDateTime by;

    public EditDeadlineCommand(int taskNumber, LocalDateTime by) {
        assert taskNumber > 0 : "Task number must be strictly positive";
        assert by != null : "Deadline date must not be null";
        this.taskNumber = taskNumber;
        this.by = by;
    }

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        Task updatedTask = moduleBook.updateTaskDeadlineByDisplayIndex(taskNumber, by);
        ui.showTaskDeadlineUpdated(updatedTask, taskNumber);
        storage.save(moduleBook);
    }
}

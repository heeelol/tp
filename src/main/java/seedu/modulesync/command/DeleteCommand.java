package seedu.modulesync.command;

import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.task.Task;
import seedu.modulesync.ui.Ui;

public class DeleteCommand extends Command {
    private static final Logger logger = Logger.getLogger(DeleteCommand.class.getName());
    private final int displayIndex;

    public DeleteCommand(int displayIndex) {
        this.displayIndex = displayIndex;
    }

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        assert moduleBook != null : "ModuleBook must not be null";
        assert storage != null : "Storage must not be null";
        assert ui != null : "Ui must not be null";
        assert displayIndex > 0 : "Display index must be strictly positive";

        try {
            int totalBefore = moduleBook.countTotalTasks();
            assert displayIndex <= totalBefore : "Display index out of bounds";
            Task deletedTask = moduleBook.removeTaskByDisplayIndex(displayIndex);
            assert deletedTask != null : "removeTaskByDisplayIndex should return a task when deletion succeeds";

            storage.save(moduleBook);

            int totalAfter = moduleBook.countTotalTasks();
            assert totalAfter == totalBefore - 1 : "Total task count should decrease by exactly 1 after deletion";
            ui.showTaskDeleted(deletedTask, totalAfter);
        } catch (ModuleSyncException e) {
            logger.log(Level.WARNING, "Invalid task index: " + displayIndex, e);
            throw e;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception caught during task deletion", e);
            throw new ModuleSyncException("Error during deletion: " + e.getMessage());
        }
    }
}

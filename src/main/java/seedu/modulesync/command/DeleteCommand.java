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
        assert displayIndex > 0 : "Display index must be strictly positive";
        assert displayIndex <= moduleBook.countTotalTasks() : "Display index out of bounds";

        try {
            Task deletedTask = moduleBook.removeTaskByDisplayIndex(displayIndex);
            storage.save(moduleBook);
            ui.showTaskDeleted(deletedTask, moduleBook.countTotalTasks());
        } catch (ModuleSyncException e) {
            logger.log(Level.WARNING, "Invalid task index: " + displayIndex, e);
            throw e;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception caught during task deletion", e);
            throw new ModuleSyncException("Error during deletion: " + e.getMessage());
        }
    }
}

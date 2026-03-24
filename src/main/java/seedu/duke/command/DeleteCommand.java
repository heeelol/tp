package seedu.duke.command;

import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.duke.exception.ModuleSyncException;
import seedu.duke.module.ModuleBook;
import seedu.duke.storage.Storage;
import seedu.duke.task.Task;
import seedu.duke.ui.Ui;

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

package seedu.modulesync.command;

import java.util.logging.Logger;
import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.Module;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.task.Task;
import seedu.modulesync.ui.Ui;

/**
 * Command that adds a new to-do task to a specified module.
 */
public class AddTodoCommand extends Command {
    private static final Logger LOGGER = Logger.getLogger(AddTodoCommand.class.getName());
    private final String moduleCode;
    private final String description;

    /**
     * Constructs an AddTodoCommand with the given module code and task description.
     *
     * @param moduleCode  the target module code
     * @param description the to-do task description
     */
    public AddTodoCommand(String moduleCode, String description) {
        assert moduleCode != null && !moduleCode.trim().isEmpty() : "Module code must be provided";
        assert description != null && !description.trim().isEmpty() : "Task description must be provided";
        this.moduleCode = moduleCode;
        this.description = description;
    }

    /**
     * Executes the add-todo command by adding the task to the module book,
     * persisting the change, and displaying a confirmation message.
     *
     * @param moduleBook the module book to update
     * @param storage    the storage to save to
     * @param ui         the UI to display feedback
     * @throws ModuleSyncException if the task cannot be added or saved
     */
    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        LOGGER.fine(() -> "Adding todo to module " + moduleCode);
        Module module = moduleBook.getOrCreate(moduleCode);
        Task task = module.addTodo(description);
        storage.save(moduleBook);
        LOGGER.fine(() -> "Todo added and saved for module " + moduleCode);
        ui.showTaskAdded(module, task, moduleBook.countTotalTasks());
    }
}



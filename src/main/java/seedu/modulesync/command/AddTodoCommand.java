package seedu.modulesync.command;

import java.util.logging.Logger;
import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.Module;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.task.Task;
import seedu.modulesync.ui.Ui;

//@@author Huang-Hau-Shuan
/**
 * Command that adds a new to-do task to a specified module, with an optional weightage.
 */
public class AddTodoCommand extends Command {
    private static final Logger LOGGER = Logger.getLogger(AddTodoCommand.class.getName());
    private final String moduleCode;
    private final String description;
    /** Optional weightage (0–100). Null means the task is unweighted. */
    private final Integer weightage;

    /**
     * Constructs an AddTodoCommand with no weightage.
     *
     * @param moduleCode  the target module code
     * @param description the to-do task description
     */
    public AddTodoCommand(String moduleCode, String description) {
        this(moduleCode, description, null);
    }

    /**
     * Constructs an AddTodoCommand with an optional weightage.
     *
     * @param moduleCode  the target module code
     * @param description the to-do task description
     * @param weightage   the task weightage (0–100), or null if not applicable
     */
    public AddTodoCommand(String moduleCode, String description, Integer weightage) {
        assert moduleCode != null && !moduleCode.trim().isEmpty() : "Module code must be provided";
        assert description != null && !description.trim().isEmpty() : "Task description must be provided";
        assert weightage == null || (weightage >= 0 && weightage <= 100) : "Weightage must be 0–100 or null";
        this.moduleCode = moduleCode;
        this.description = description;
        this.weightage = weightage;
    }

    /**
     * Executes the add-todo command by adding the task (with optional weightage) to the module,
     * persisting the change, and displaying a confirmation message.
     *
     * @param moduleBook the module book to update
     * @param storage    the storage to save to
     * @param ui         the UI to display feedback
     * @throws ModuleSyncException if the task cannot be added or saved
     */
    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        LOGGER.fine(() -> "Adding todo to module " + moduleCode
                + (weightage != null ? " with weightage " + weightage + "%" : " (no weightage)"));
        Module module = moduleBook.getOrCreate(moduleCode);
        Task task = module.addTodo(description, weightage);
        storage.save(moduleBook);
        LOGGER.fine(() -> "Todo added and saved for module " + moduleCode);
        ui.showTaskAdded(module, task, moduleBook.countTotalTasks());
    }
}



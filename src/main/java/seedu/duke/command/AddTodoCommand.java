package seedu.duke.command;

import java.util.logging.Logger;
import seedu.duke.exception.ModuleSyncException;
import seedu.duke.module.Module;
import seedu.duke.module.ModuleBook;
import seedu.duke.storage.Storage;
import seedu.duke.task.Task;
import seedu.duke.ui.Ui;

public class AddTodoCommand extends Command {
    private static final Logger LOGGER = Logger.getLogger(AddTodoCommand.class.getName());
    private final String moduleCode;
    private final String description;

    public AddTodoCommand(String moduleCode, String description) {
        assert moduleCode != null && !moduleCode.trim().isEmpty() : "Module code must be provided";
        assert description != null && !description.trim().isEmpty() : "Task description must be provided";
        this.moduleCode = moduleCode;
        this.description = description;
    }

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        LOGGER.fine(() -> "Adding todo to module " + moduleCode);
        Module module = moduleBook.getOrCreate(moduleCode);
        Task task = module.addTodo(description);
        storage.save(moduleBook);
        LOGGER.fine(() -> "Todo added and saved for module " + moduleCode);
        ui.showTaskAdded(module, task, moduleBook.totalTaskCount());
    }
}

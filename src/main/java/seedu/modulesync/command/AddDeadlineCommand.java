package seedu.modulesync.command;

import java.time.LocalDateTime;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.Module;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.task.Task;
import seedu.modulesync.ui.Ui;

public class AddDeadlineCommand extends Command {
    private final String moduleCode;
    private final String description;
    private final LocalDateTime by;

    public AddDeadlineCommand(String moduleCode, String description, LocalDateTime by) {
        this.moduleCode = moduleCode;
        this.description = description;
        this.by = by;
    }

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        Module module = moduleBook.getOrCreate(moduleCode);
        Task task = module.addDeadline(description, by);
        storage.save(moduleBook);
        ui.showTaskAdded(module, task, moduleBook.countTotalTasks());
    }
}

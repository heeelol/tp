package seedu.modulesync.command;

import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

public class ListNotDoneCommand extends Command {
    private final String moduleCode;

    public ListNotDoneCommand(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) {
        ui.showNotDoneTaskList(moduleBook, moduleCode);
    }
}

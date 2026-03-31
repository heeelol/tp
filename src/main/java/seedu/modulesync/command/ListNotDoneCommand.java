package seedu.modulesync.command;

import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

public class ListNotDoneCommand extends Command {
    private final String moduleCode;

    public ListNotDoneCommand(String moduleCode) {
        assert moduleCode != null && !moduleCode.isBlank() : "Module code must not be null/blank";
        this.moduleCode = moduleCode;
    }

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) {
        assert moduleBook != null : "ModuleBook must not be null";
        assert ui != null : "Ui must not be null";
        assert moduleCode != null && !moduleCode.isBlank() : "Module code must not be null/blank";
        ui.showNotDoneTaskList(moduleBook, moduleCode);
    }
}

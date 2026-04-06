package seedu.modulesync.command;

import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

public class ListCommand extends Command {
    private final String moduleCode;

    public ListCommand() {
        this.moduleCode = null;
    }

    public ListCommand(String moduleCode) {
        if (moduleCode == null) {
            this.moduleCode = null;
        } else {
            this.moduleCode = moduleCode.toUpperCase();
        }
    }

    @Override
    public boolean isMutating() {
        return false;
    }

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) {
        if (moduleCode == null) {
            ui.showTaskList(moduleBook);
        } else {
            ui.showTaskList(moduleBook, moduleCode);
        }
    }
}

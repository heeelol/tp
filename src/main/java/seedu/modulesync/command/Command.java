package seedu.modulesync.command;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

public abstract class Command {
    public abstract void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException;

    public boolean isExit() {
        return false;
    }
}

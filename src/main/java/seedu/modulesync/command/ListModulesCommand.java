package seedu.modulesync.command;

import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

/**
 * Command that lists all registered modules.
 */
public class ListModulesCommand extends Command {

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) {
        assert moduleBook != null : "ModuleBook must not be null";
        assert ui != null : "Ui must not be null";
        ui.showModuleList(moduleBook);
    }
}

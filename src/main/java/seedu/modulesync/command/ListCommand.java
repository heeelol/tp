package seedu.modulesync.command;

import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

public class ListCommand extends Command {
    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) {
        ui.showTaskList(moduleBook);
    }
}

package seedu.modulesync.command;

import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

/**
 * Command that lists only upcoming deadlines to help plan the week.
 */
public class ListDeadlinesCommand extends Command {
    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) {
        assert moduleBook != null : "ModuleBook must not be null when executing ListDeadlinesCommand";
        assert storage != null : "Storage must not be null when executing ListDeadlinesCommand";
        assert ui != null : "Ui must not be null when executing ListDeadlinesCommand";
        ui.showDeadlineList(moduleBook);
    }
}

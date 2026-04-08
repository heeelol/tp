package seedu.modulesync.command;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

/**
 * Represents a command to check for deadline conflicts on the same day.
 */
public class CheckConflictsCommand extends Command {

    public static final String COMMAND_WORD = "check /conflicts";
    public static final String ALT_COMMAND_WORD = "/conflicts";

    /**
     * Returns whether this command mutates application state.
     *
     * @return {@code false} because this command only reads deadlines
     */
    @Override
    public boolean isMutating() {
        return false;
    }

    /**
     * Executes the deadline conflict check and displays same-day crunch periods.
     *
     * @param moduleBook the active semester module book
     * @param storage the active semester storage
     * @param ui the UI used for output
     * @throws ModuleSyncException if the UI cannot render the result
     */
    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        ui.showDeadlineConflicts(moduleBook);
    }
}

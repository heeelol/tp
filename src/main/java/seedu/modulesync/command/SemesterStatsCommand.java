package seedu.modulesync.command;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

/**
 * Command that shows overall statistics for the current semester.
 *
 * <p>Triggered by {@code semester stats}.
 */
public class SemesterStatsCommand extends Command {

    /**
     * Constructs a semester statistics command.
     */
    public SemesterStatsCommand() {
    }

    @Override
    public boolean isMutating() {
        return false;
    }

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        assert moduleBook != null : "ModuleBook must not be null";
        assert ui != null : "Ui must not be null";

        ui.showSemesterStatistics(moduleBook);
    }
}

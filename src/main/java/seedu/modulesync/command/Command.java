package seedu.modulesync.command;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

/**
 * Base class for all executable commands.
 *
 * <p>Subclasses implement {@link #execute(ModuleBook, Storage, Ui)} to carry out their action.
 * Commands that only read data (list, stats, etc.) should override {@link #isMutating()} to
 * return {@code false}; this allows {@code ModuleSync.run()} to enforce the read-only constraint
 * on archived semesters without requiring each mutating command to duplicate the guard.
 */
public abstract class Command {

    /**
     * Executes this command against the given model and I/O dependencies.
     *
     * @param moduleBook the active semester's module book
     * @param storage    the storage for the active semester file
     * @param ui         the user interface
     * @throws ModuleSyncException if the command cannot be completed
     */
    public abstract void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException;

    /**
     * Returns whether this command modifies the module book or storage.
     *
     * <p>Defaults to {@code true}.  Read-only commands (list, stats, exit, …) should override
     * this to return {@code false} so that {@code ModuleSync.run()} can reject mutating commands
     * when the current semester is archived.
     *
     * @return {@code true} if this command writes data, {@code false} if it is read-only
     */
    public boolean isMutating() {
        return true;
    }

    /**
     * Returns whether this command signals the application to exit.
     *
     * @return {@code true} only for {@link ExitCommand}
     */
    public boolean isExit() {
        return false;
    }
}

package seedu.modulesync.command;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.Module;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

/**
 * Command to archive a module so it no longer appears in the active task list.
 * The module's data is retained but marked as read-only.
 *
 * <p>Usage: {@code module archive /mod MODULECODE}
 */
public class ArchiveModuleCommand extends Command {

    private final String moduleCode;

    /**
     * Constructs an ArchiveModuleCommand with the specified module code.
     *
     * @param moduleCode the module code to archive (case-insensitive)
     */
    public ArchiveModuleCommand(String moduleCode) {
        assert moduleCode != null && !moduleCode.trim().isEmpty() : "Module code must not be null or empty";
        this.moduleCode = moduleCode.trim().toUpperCase();
    }

    @Override
    public boolean isMutating() {
        return true;
    }

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        assert moduleBook != null : "ModuleBook must not be null";
        assert ui != null : "Ui must not be null";
        assert storage != null : "Storage must not be null";

        Module module = moduleBook.getModule(moduleCode);
        if (module == null) {
            throw new ModuleSyncException("Module " + moduleCode + " not found.");
        }

        if (module.isArchived()) {
            throw new ModuleSyncException("Module " + moduleCode + " is already archived.");
        }

        module.setArchived(true);
        storage.save(moduleBook);
        ui.showMessage("Module " + moduleCode + " has been archived.");
    }
}

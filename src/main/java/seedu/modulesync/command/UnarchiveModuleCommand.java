package seedu.modulesync.command;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.Module;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

/**
 * Command to unarchive a module, making it active and editable again.
 *
 * <p>Usage: {@code module unarchive /mod MODULECODE}
 */
public class UnarchiveModuleCommand extends Command {

    private final String moduleCode;

    /**
     * Constructs an UnarchiveModuleCommand with the specified module code.
     *
     * @param moduleCode the module code to unarchive (case-insensitive)
     */
    public UnarchiveModuleCommand(String moduleCode) {
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

        if (!module.isArchived()) {
            throw new ModuleSyncException("Module " + moduleCode + " is not archived.");
        }

        module.setArchived(false);
        storage.save(moduleBook);
        ui.showMessage("Module " + moduleCode + " has been unarchived.");
    }
}

package seedu.modulesync.command;

import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

/**
 * Lists the top X most urgent tasks based on weightage (primary) and deadline (secondary).
 */
public class ListTopCommand extends Command {
    private final int topCount;

    /**
     * Creates a ListTopCommand to show the top X urgent tasks.
     *
     * @param topCount the number of tasks to display
     */
    public ListTopCommand(int topCount) {
        this.topCount = topCount;
    }

    @Override
    public boolean isMutating() {
        return false;
    }

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) {
        ui.showTopUrgentTasks(moduleBook, topCount);
    }
}

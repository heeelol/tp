package seedu.modulesync;

import java.util.Scanner;
import java.util.logging.Logger;

import seedu.modulesync.command.Command;
import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.parser.Parser;
import seedu.modulesync.semester.SemesterBook;
import seedu.modulesync.storage.SemesterStorage;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

/**
 * Main application class for ModuleSync.
 *
 * <p>Bootstraps the application by loading the full {@link SemesterBook} from disk via
 * {@link SemesterStorage}, then enters the command loop.  All existing task commands
 * continue to work unchanged — they receive the <em>current semester's</em>
 * {@link ModuleBook} and a per-semester {@link Storage} instance exactly as before.
 *
 * <p>Teammates implementing semester-level commands (archive, switch, list) should
 * inject {@link SemesterBook} and {@link SemesterStorage} via their command constructors.
 */
public class ModuleSync {

    private static final Logger LOGGER = Logger.getLogger(ModuleSync.class.getName());

    private final SemesterBook semesterBook;
    private final SemesterStorage semesterStorage;
    private final Parser parser;
    private final Ui ui;

    /**
     * Constructs and initialises the ModuleSync application.
     * Loads the full semester book from the {@code data/} directory.
     *
     * @throws ModuleSyncException if the storage layer cannot be initialised
     */
    public ModuleSync() throws ModuleSyncException {
        this.semesterStorage = SemesterStorage.ofDefault();
        this.semesterBook = semesterStorage.load();
        this.parser = new Parser(semesterBook, semesterStorage);
        this.ui = new Ui(new Scanner(System.in));
    }

    /**
     * Constructs a ModuleSync instance with injected dependencies.
     *
     * @param semesterBook the semester book used by the application
     * @param semesterStorage the semester storage used by the application
     * @param parser the parser used to create commands
     * @param ui the UI used for input and output
     */
    ModuleSync(SemesterBook semesterBook, SemesterStorage semesterStorage, Parser parser, Ui ui) {
        this.semesterBook = semesterBook;
        this.semesterStorage = semesterStorage;
        this.parser = parser;
        this.ui = ui;
    }

    /**
     * Runs the main command loop until the user exits.
     *
     * <p>On each iteration:
     * <ol>
     *   <li>The command is parsed first so semester commands (e.g. switch) run before the
     *       active-semester context is read.</li>
     *   <li>If the current semester is archived, any command whose {@link Command#isMutating()}
     *       returns {@code true} is rejected centrally here — no per-command guard needed.</li>
     *   <li>The active semester's {@link ModuleBook} and a per-semester {@link Storage} are
     *       passed to the command so existing task commands write to the correct file.</li>
     * </ol>
     */
    public void run() {
        ui.showWelcome();
        ui.showCurrentSemester(semesterBook);
        try {
            showStartupWarnings();
        } catch (ModuleSyncException e) {
            ui.showError(e.getMessage());
        }
        boolean exit = false;
        while (!exit) {
            String fullCommand = ui.readCommand();
            try {
                Command command = parser.parse(fullCommand);

                // Enforce read-only: reject mutating commands in an archived semester
                if (command.isMutating() && semesterBook.isCurrentSemesterReadOnly()) {
                    ui.showReadOnlySemesterError(semesterBook.getCurrentSemesterName());
                    continue;
                }

                ModuleBook activeModuleBook = semesterBook.getCurrentModuleBook();
                Storage activeSemesterStorage = buildActiveSemesterStorage();

                command.execute(activeModuleBook, activeSemesterStorage, ui);
                exit = command.isExit();
            } catch (ModuleSyncException e) {
                ui.showError(e.getMessage());
            }
        }
    }

    /**
     * Displays warnings that should appear immediately when the CLI opens.
     */
    void showStartupWarnings() throws ModuleSyncException {
        if (semesterBook.getCurrentSemesterName() == null) {
            LOGGER.fine("Skipping startup warnings because no active semester is set.");
            return;
        }
        ModuleBook activeModuleBook = semesterBook.getCurrentModuleBook();
        ui.showStartupOverdueWarning(activeModuleBook);
    }

    /**
     * Builds a {@link Storage} instance pointing at the active semester's task file.
     * This is passed to every command so that {@code storage.save(moduleBook)} writes
     * to the correct per-semester file.
     *
     * @return a {@link Storage} for the current semester file
     * @throws ModuleSyncException if no active semester is set
     */
    private Storage buildActiveSemesterStorage() throws ModuleSyncException {
        String name = semesterBook.getCurrentSemesterName();
        if (name == null) {
            throw new ModuleSyncException("No active semester.");
        }
        return new Storage(semesterStorage.getDataDir().resolve(name + ".txt"));
    }

    /**
     * Main entry-point for the ModuleSync application.
     */
    public static void main(String[] args) {
        try {
            new ModuleSync().run();
        } catch (ModuleSyncException e) {
            System.out.println("Failed to start: " + e.getMessage());
        }
    }
}






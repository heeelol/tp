package seedu.modulesync.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.semester.Semester;
import seedu.modulesync.semester.SemesterBook;

/**
 * Handles the multi-semester file layout on disk.
 *
 * <h2>File layout</h2>
 * <pre>
 * data/
 *   current.txt          ← single line: name of the active semester (e.g. "AY2526-S2")
 *   AY2526-S2.txt        ← active semester's task data
 *   AY2526-S1.txt        ← archived semester's task data
 *   AY2425-S2.txt        ← archived semester's task data
 * </pre>
 *
 * <h2>Semester file name convention</h2>
 * Each semester is stored as {@code <name>.txt} inside the data directory.
 * Archived semesters are identified by a header line {@code #archived} at the top of the file;
 * active semester files have no such header (or may have {@code #active} for clarity).
 *
 * <h2>Backward compatibility</h2>
 * If {@code current.txt} does not exist but a legacy {@code modules.txt} does, the legacy file
 * is treated as the single active semester named {@code "default"} and migrated automatically.
 *
 * <h2>Relationship with existing {@link Storage}</h2>
 * {@code SemesterStorage} <em>delegates</em> per-semester load/save work to {@link Storage}.
 * It does not duplicate that logic; it only adds the semester-level envelope (directory scan,
 * {@code current.txt} pointer, archive header).
 */
public class SemesterStorage {

    private static final Logger LOGGER = Logger.getLogger(SemesterStorage.class.getName());

    private static final String CURRENT_FILE = "current.txt";
    private static final String LEGACY_FILE = "modules.txt";
    private static final String DEFAULT_SEMESTER_NAME = "default";
    private static final String ARCHIVE_HEADER = "#archived";
    private static final String SEMESTER_FILE_SUFFIX = ".txt";

    private final Path dataDir;

    /**
     * Constructs a SemesterStorage backed by the given data directory.
     *
     * @param dataDir path to the {@code data/} directory
     */
    public SemesterStorage(Path dataDir) {
        assert dataDir != null : "Data directory path must not be null";
        this.dataDir = dataDir;
    }

    // -------------------------------------------------------------------------
    // Load
    // -------------------------------------------------------------------------

    /**
     * Loads the full {@link SemesterBook} from disk.
     *
     * <p>Steps:
     * <ol>
     *   <li>Read {@code current.txt} to find the active semester name.</li>
     *   <li>Scan the data directory for {@code *.txt} files (excluding {@code current.txt}
     *       and the legacy {@code modules.txt}).</li>
     *   <li>For each semester file, determine archived status from the {@code #archived} header,
     *       then delegate task loading to {@link Storage}.</li>
     *   <li>Fall back to the legacy {@code modules.txt} if no semester files exist.</li>
     * </ol>
     *
     * @return a fully populated {@link SemesterBook}
     * @throws ModuleSyncException if a critical I/O error occurs
     */
    public SemesterBook load() throws ModuleSyncException {
        ensureDataDirectory();
        SemesterBook semesterBook = new SemesterBook();

        Path currentFile = dataDir.resolve(CURRENT_FILE);

        if (!Files.exists(currentFile)) {
            return loadLegacyOrEmpty(semesterBook);
        }

        String activeName = readCurrentPointer(currentFile);
        loadAllSemesterFiles(semesterBook, activeName);

        // If the active semester file was missing, create a blank one
        ensureActiveSemesterExists(semesterBook, activeName);

        return semesterBook;
    }

    /**
     * Reads the {@code current.txt} pointer file and returns the active semester name.
     *
     * @param currentFile path to the pointer file
     * @return the trimmed semester name
     * @throws ModuleSyncException if the file cannot be read or is empty
     */
    private String readCurrentPointer(Path currentFile) throws ModuleSyncException {
        try {
            List<String> lines = Files.readAllLines(currentFile, StandardCharsets.UTF_8);
            if (lines.isEmpty() || lines.get(0).trim().isEmpty()) {
                throw new ModuleSyncException("current.txt is empty. Please specify a semester name.");
            }
            return lines.get(0).trim();
        } catch (IOException e) {
            throw new ModuleSyncException("Failed to read current.txt: " + e.getMessage());
        }
    }

    /**
     * Scans the data directory and loads every {@code *.txt} file that is not
     * {@code current.txt} or the legacy {@code modules.txt} as a semester.
     *
     * @param semesterBook the book to populate
     * @param activeName   the name of the active semester (read from {@code current.txt})
     * @throws ModuleSyncException on I/O error
     */
    private void loadAllSemesterFiles(SemesterBook semesterBook, String activeName)
            throws ModuleSyncException {
        try (java.util.stream.Stream<Path> stream = Files.list(dataDir)) {
            List<Path> semesterFiles = stream
                .filter(p -> p.getFileName().toString().endsWith(SEMESTER_FILE_SUFFIX))
                .filter(p -> !p.getFileName().toString().equals(CURRENT_FILE))
                .filter(p -> !p.getFileName().toString().equals(LEGACY_FILE))
                .collect(java.util.stream.Collectors.toList());

            for (Path p : semesterFiles) {
                try {
                    loadOneSemesterFile(semesterBook, p);
                } catch (ModuleSyncException e) {
                    LOGGER.warning("Skipping corrupted semester file " + p + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new ModuleSyncException("Failed to scan data directory: " + e.getMessage());
        }

        // Wire the active semester pointer
        if (semesterBook.hasSemesters()) {
            try {
                semesterBook.setCurrentSemester(activeName);
            } catch (ModuleSyncException e) {
                LOGGER.warning("Active semester '" + activeName + "' not found among loaded files.");
            }
        }
    }

    /**
     * Loads a single semester file into the given {@link SemesterBook}.
     *
     * @param semesterBook the book to register the semester into
     * @param filePath     path to the semester {@code .txt} file
     * @throws ModuleSyncException if the file cannot be read
     */
    private void loadOneSemesterFile(SemesterBook semesterBook, Path filePath) throws ModuleSyncException {
        String fileName = filePath.getFileName().toString();
        String semesterName = fileName.substring(0, fileName.length() - SEMESTER_FILE_SUFFIX.length());

        boolean archived = detectArchivedHeader(filePath);

        // Delegate task loading to the existing Storage class, skipping header lines
        Storage storage = new Storage(filePath);
        ModuleBook moduleBook = storage.loadSkippingHeaders();

        Semester semester = new Semester(semesterName, moduleBook, archived);
        semesterBook.addSemester(semester);
        LOGGER.fine(() -> "Loaded semester: " + semesterName + (archived ? " [archived]" : ""));
    }

    /**
     * Returns {@code true} if the file's first non-empty line is {@code #archived}.
     *
     * @param filePath the semester file to inspect
     * @return {@code true} if archived
     */
    private boolean detectArchivedHeader(Path filePath) {
        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    return ARCHIVE_HEADER.equals(line.trim());
                }
            }
        } catch (IOException e) {
            LOGGER.warning("Could not read archive header from " + filePath);
        }
        return false;
    }

    /**
     * Ensures the active semester exists in the book; creates a blank one if missing.
     *
     * @param semesterBook the book to check
     * @param activeName   the required active semester name
     * @throws ModuleSyncException on propagation
     */
    private void ensureActiveSemesterExists(SemesterBook semesterBook,
                                            String activeName) throws ModuleSyncException {
        try {
            semesterBook.getCurrentSemester();
        } catch (ModuleSyncException e) {
            LOGGER.info("Active semester '" + activeName + "' missing — creating blank.");
            semesterBook.addSemester(new Semester(activeName, false));
            semesterBook.setCurrentSemester(activeName);
        }
    }

    /**
     * Handles the first-run / legacy scenario.
     * If {@code modules.txt} exists it is loaded as a semester named {@code "default"};
     * otherwise an empty semester named {@code "default"} is created.
     *
     * @param semesterBook the book to populate
     * @return the populated (possibly empty) book
     * @throws ModuleSyncException on I/O error
     */
    private SemesterBook loadLegacyOrEmpty(SemesterBook semesterBook) throws ModuleSyncException {
        Path legacyFile = dataDir.resolve(LEGACY_FILE);
        ModuleBook moduleBook;

        if (Files.exists(legacyFile)) {
            LOGGER.info("No current.txt found; migrating legacy modules.txt to default semester.");
            Storage legacyStorage = new Storage(legacyFile);
            moduleBook = legacyStorage.load();
        } else {
            moduleBook = new ModuleBook();
        }

        Semester defaultSemester = new Semester(DEFAULT_SEMESTER_NAME, moduleBook, false);
        semesterBook.addSemester(defaultSemester);
        try {
            semesterBook.setCurrentSemester(DEFAULT_SEMESTER_NAME);
        } catch (ModuleSyncException e) {
            assert false : "Default semester was just added; setCurrentSemester must succeed";
        }

        // Persist the new layout so current.txt is created
        save(semesterBook);
        return semesterBook;
    }

    // -------------------------------------------------------------------------
    // Save
    // -------------------------------------------------------------------------

    /**
     * Persists the entire {@link SemesterBook} to disk.
     *
     * <p>For each semester, a {@code <name>.txt} file is written inside the data directory.
     * Archived semester files are prefixed with a {@code #archived} header line.
     * The {@code current.txt} pointer is updated to reflect the active semester.
     *
     * @param semesterBook the book to persist
     * @throws ModuleSyncException if writing fails
     */
    public void save(SemesterBook semesterBook) throws ModuleSyncException {
        assert semesterBook != null : "SemesterBook must not be null when saving";
        ensureDataDirectory();

        for (Semester semester : semesterBook.getAllSemesters()) {
            saveSemester(semester);
        }

        saveCurrentPointer(semesterBook.getCurrentSemesterName());
        LOGGER.fine("SemesterBook saved successfully.");
    }

    /**
     * Saves a single semester's tasks to its {@code <name>.txt} file.
     * Archived semesters are prefixed with {@code #archived}.
     *
     * @param semester the semester to save
     * @throws ModuleSyncException if writing fails
     */
    public void saveSemester(Semester semester) throws ModuleSyncException {
        assert semester != null : "Semester must not be null when saving";
        Path semesterFile = dataDir.resolve(semester.getName() + SEMESTER_FILE_SUFFIX);
        Storage storage = new Storage(semesterFile);
        storage.saveWithHeader(semester.getModuleBook(), semester.isArchived() ? ARCHIVE_HEADER : null);
        LOGGER.fine(() -> "Saved semester file: " + semesterFile.getFileName());
    }

    /**
     * Writes the {@code current.txt} pointer file.
     *
     * @param activeSemesterName the name to write; may be null (writes an empty file)
     * @throws ModuleSyncException if writing fails
     */
    private void saveCurrentPointer(String activeSemesterName) throws ModuleSyncException {
        Path currentFile = dataDir.resolve(CURRENT_FILE);
        String content = activeSemesterName != null ? activeSemesterName : "";
        try {
            Files.write(currentFile, (content + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ModuleSyncException("Failed to write current.txt: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Ensures the data directory exists.
     *
     * @throws ModuleSyncException if the directory cannot be created
     */
    private void ensureDataDirectory() throws ModuleSyncException {
        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
        } catch (IOException e) {
            throw new ModuleSyncException("Unable to create data directory: " + e.getMessage());
        }
    }

    /**
     * Returns the data directory path used by this storage.
     *
     * @return the data directory
     */
    public Path getDataDir() {
        return dataDir;
    }

    /**
     * Convenience factory — creates a {@link SemesterStorage} pointed at {@code data/}.
     *
     * @return a new {@link SemesterStorage} for the default data directory
     */
    public static SemesterStorage ofDefault() {
        return new SemesterStorage(Paths.get("data"));
    }
}

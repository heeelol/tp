package seedu.modulesync.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.Module;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.task.Deadline;
import seedu.modulesync.task.Task;
import seedu.modulesync.task.Todo;

/**
 * Handles reading and writing of task data (and module metadata) to a persistent file.
 *
 * <h2>File format</h2>
 * <pre>
 * [optional semester header, e.g. #archived]
 * #MOD | CS2113 | grade:A+ | credits:4
 * CS2113 | T | 0 | Homework
 * CS2113 | D | 1 | Final Project | 2026-05-01 23:59 | 30
 * #MOD | CS1010 | grade:- | credits:4
 * CS1010 | T | 0 | Lab 1
 * </pre>
 *
 * <p>The {@code #MOD} lines are written before each module's tasks and restore the
 * module's optional {@code grade} and {@code credits} fields. A grade value of {@code "-"}
 * means no grade has been assigned yet (null).
 *
 * <p>Lines starting with {@code #} that are not {@code #MOD} lines are treated as
 * semester-level headers and are skipped by the task-loading logic.
 */
public class Storage {

    private static final Logger LOGGER = Logger.getLogger(Storage.class.getName());

    private static final String FIELD_SEPARATOR_REGEX = "\\s*\\|\\s*";
    private static final String FIELD_SEPARATOR = " | ";
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";
    private static final int DATE_ONLY_LENGTH = 10;
    private static final int MIN_TASK_FIELDS = 4;
    private static final int MIN_DEADLINE_FIELDS = 5;
    private static final int TODO_FIELDS_WITHOUT_WEIGHTAGE = 4;
    private static final int TODO_FIELDS_WITH_WEIGHTAGE = 5;
    private static final int DEADLINE_FIELDS_WITHOUT_WEIGHTAGE = 5;
    private static final int DEADLINE_FIELDS_WITH_WEIGHTAGE = 6;
    private static final int FIELD_MODULE = 0;
    private static final int FIELD_TYPE = 1;
    private static final int FIELD_DONE = 2;
    private static final int FIELD_DESC = 3;
    private static final int FIELD_DUE = 4;

    /** Prefix used for per-module metadata lines in the storage file. */
    private static final String MODULE_META_PREFIX = "#MOD";
    /** Field count in a valid #MOD line: prefix, code, grade, credits. */
    private static final int MODULE_META_FIELD_COUNT = 4;
    /** Index of the module code field within a #MOD line. */
    private static final int MOD_META_FIELD_CODE = 1;
    /** Index of the grade field within a #MOD line. */
    private static final int MOD_META_FIELD_GRADE = 2;
    /** Index of the credits field within a #MOD line. */
    private static final int MOD_META_FIELD_CREDITS = 3;
    /** Sentinel written to file when a module has no grade assigned. */
    private static final String NO_GRADE_SENTINEL = "-";

    private final Path filePath;

    /**
     * Constructs a Storage instance backed by the given file path.
     *
     * @param filePath the path to the storage file
     */
    public Storage(Path filePath) {
        this.filePath = filePath;
    }

    // -------------------------------------------------------------------------
    // Public load methods
    // -------------------------------------------------------------------------

    /**
     * Loads all tasks and module metadata from the storage file, skipping any top-level
     * {@code #archived} / {@code #active} header lines managed by {@link SemesterStorage}.
     * Per-module {@code #MOD} lines are parsed to restore grade and credits.
     *
     * @return a populated {@link ModuleBook}, or an empty one if the file does not exist
     * @throws ModuleSyncException if the file cannot be read
     */
    public ModuleBook loadSkippingHeaders() throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        if (!Files.exists(filePath)) {
            return moduleBook;
        }
        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                if (trimmed.startsWith(MODULE_META_PREFIX)) {
                    decodeModuleMeta(trimmed, moduleBook);
                    continue;
                }
                if (trimmed.startsWith("#")) {
                    // Top-level semester header (e.g. #archived) — skip
                    continue;
                }
                Task task = decodeTask(trimmed);
                Module module = moduleBook.getOrCreate(task.getModuleCode());
                module.getTasks().add(task);
            }
            return moduleBook;
        } catch (IOException e) {
            throw new ModuleSyncException("Failed to read storage file: " + e.getMessage());
        }
    }

    /**
     * Loads all tasks and module metadata from the storage file and returns a populated
     * {@link ModuleBook}.
     *
     * @return a {@link ModuleBook} loaded from disk, or an empty one if the file does not exist
     * @throws ModuleSyncException if the file cannot be read
     */
    public ModuleBook load() throws ModuleSyncException {
        ModuleBook moduleBook = new ModuleBook();
        if (!Files.exists(filePath)) {
            ensureParentDirectory();
            return moduleBook;
        }
        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                if (trimmed.startsWith(MODULE_META_PREFIX)) {
                    decodeModuleMeta(trimmed, moduleBook);
                    continue;
                }
                if (trimmed.startsWith("#")) {
                    continue;
                }
                Task task = decodeTask(trimmed);
                Module module = moduleBook.getOrCreate(task.getModuleCode());
                module.getTasks().add(task);
            }
            return moduleBook;
        } catch (IOException e) {
            throw new ModuleSyncException("Failed to read storage file: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Public save methods
    // -------------------------------------------------------------------------

    /**
     * Saves all tasks and module metadata in the given {@link ModuleBook} to the storage file,
     * optionally writing a semester-level header line (e.g. {@code #archived}) as the first line.
     *
     * <p>Each module is preceded by a {@code #MOD} metadata line encoding its grade and credits.
     *
     * @param moduleBook the module book to persist
     * @param header     an optional semester header (e.g. {@code "#archived"}), or {@code null}
     * @throws ModuleSyncException if the file cannot be written
     */
    public void saveWithHeader(ModuleBook moduleBook, String header) throws ModuleSyncException {
        ensureParentDirectory();
        List<String> lines = new ArrayList<>();
        if (header != null && !header.isEmpty()) {
            lines.add(header);
        }
        appendModuleLines(moduleBook, lines);
        writeLines(lines);
    }

    /**
     * Saves all tasks and module metadata in the given {@link ModuleBook} to the storage file.
     *
     * @param moduleBook the module book to persist
     * @throws ModuleSyncException if the file cannot be written
     */
    public void save(ModuleBook moduleBook) throws ModuleSyncException {
        ensureParentDirectory();
        List<String> lines = new ArrayList<>();
        appendModuleLines(moduleBook, lines);
        writeLines(lines);
    }

    // -------------------------------------------------------------------------
    // Module metadata encode / decode
    // -------------------------------------------------------------------------

    /**
     * Encodes a module's metadata (grade and credits) as a {@code #MOD} storage line.
     *
     * <p>Format: {@code #MOD | <code> | grade:<grade_or_dash> | credits:<credits>}
     *
     * @param module the module to encode
     * @return the encoded metadata line
     */
    private String encodeModuleMeta(Module module) {
        String gradeValue = module.hasGrade() ? module.getGrade() : NO_GRADE_SENTINEL;
        return MODULE_META_PREFIX
                + FIELD_SEPARATOR + module.getCode()
                + FIELD_SEPARATOR + "grade:" + gradeValue
                + FIELD_SEPARATOR + "credits:" + module.getCredits();
    }

    /**
     * Parses a {@code #MOD} metadata line and applies the grade and credits to the matching
     * module in the given {@link ModuleBook}, creating the module if necessary.
     *
     * @param line       the raw {@code #MOD} line from the file
     * @param moduleBook the book to apply the metadata to
     */
    private void decodeModuleMeta(String line, ModuleBook moduleBook) {
        String[] parts = line.split(FIELD_SEPARATOR_REGEX);
        if (parts.length < MODULE_META_FIELD_COUNT) {
            LOGGER.warning("Skipping malformed #MOD line: " + line);
            return;
        }
        String code = parts[MOD_META_FIELD_CODE].trim();
        Module module = moduleBook.getOrCreate(code);

        applyGradeFromMeta(parts[MOD_META_FIELD_GRADE].trim(), module, line);
        applyCreditsFromMeta(parts[MOD_META_FIELD_CREDITS].trim(), module, line);
    }

    /**
     * Applies the grade value from a parsed {@code #MOD} field to the given module.
     * A value of {@code "-"} (the sentinel) means no grade and is ignored.
     *
     * @param rawGradeField the raw "grade:..." string from the #MOD line
     * @param module        the module to update
     * @param rawLine       the full line (for warning messages)
     */
    private void applyGradeFromMeta(String rawGradeField, Module module, String rawLine) {
        if (!rawGradeField.startsWith("grade:")) {
            LOGGER.warning("Unexpected grade field format in #MOD line: " + rawLine);
            return;
        }
        String gradeValue = rawGradeField.substring("grade:".length()).trim();
        if (!NO_GRADE_SENTINEL.equals(gradeValue) && !gradeValue.isEmpty()) {
            module.setGrade(gradeValue);
        }
    }

    /**
     * Applies the credits value from a parsed {@code #MOD} field to the given module.
     *
     * @param rawCreditsField the raw "credits:..." string from the #MOD line
     * @param module          the module to update
     * @param rawLine         the full line (for warning messages)
     */
    private void applyCreditsFromMeta(String rawCreditsField, Module module, String rawLine) {
        if (!rawCreditsField.startsWith("credits:")) {
            LOGGER.warning("Unexpected credits field format in #MOD line: " + rawLine);
            return;
        }
        String creditsValue = rawCreditsField.substring("credits:".length()).trim();
        try {
            int credits = Integer.parseInt(creditsValue);
            if (credits >= 0) {
                module.setCredits(credits);
            } else {
                LOGGER.warning("Negative credits value in #MOD line, skipping: " + rawLine);
            }
        } catch (NumberFormatException e) {
            LOGGER.warning("Non-integer credits value in #MOD line, skipping: " + rawLine);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Appends a {@code #MOD} metadata line followed by each task line for every module in the book.
     *
     * @param moduleBook the book to iterate
     * @param lines      the list to append lines to
     */
    private void appendModuleLines(ModuleBook moduleBook, List<String> lines) {
        for (Module module : moduleBook.getModules()) {
            lines.add(encodeModuleMeta(module));
            for (Task task : module.getTasks().asUnmodifiableList()) {
                lines.add(task.encode());
            }
        }
    }

    /**
     * Writes the given lines to the backing file.
     *
     * @param lines the lines to write
     * @throws ModuleSyncException if the file cannot be written
     */
    private void writeLines(List<String> lines) throws ModuleSyncException {
        try {
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ModuleSyncException("Failed to save tasks: " + e.getMessage());
        }
    }

    /**
     * Decodes a single task line from the storage file into a {@link Task}.
     *
     * @param line the raw encoded line (must not start with {@code #})
     * @return the decoded {@link Task}
     * @throws ModuleSyncException if the line is malformed or the task type is unsupported
     */
    private Task decodeTask(String line) throws ModuleSyncException {
        String[] parts = line.split(FIELD_SEPARATOR_REGEX);
        if (parts.length < MIN_TASK_FIELDS) {
            throw new ModuleSyncException("Corrupted task entry: " + line);
        }
        String moduleCode = parts[FIELD_MODULE];
        String type = parts[FIELD_TYPE];
        boolean isDone = parseDone(parts[FIELD_DONE]);
        String description = parts[FIELD_DESC];

        Task task = decodeTaskByType(type, parts, moduleCode, description, isDone, line);
        restoreCompletedAt(task, parts);

        assert task != null : "Decoded task must not be null";
        return task;
    }

    /**
     * Dispatches to the correct decode helper based on task type.
     *
     * @param type        the type string ("T" or "D")
     * @param parts       the split fields
     * @param moduleCode  the module code
     * @param description the task description
     * @param isDone      the done flag
     * @param rawLine     the original line (for error messages)
     * @return the decoded task
     * @throws ModuleSyncException if the type is unsupported or fields are malformed
     */
    private Task decodeTaskByType(String type, String[] parts, String moduleCode,
                                   String description, boolean isDone, String rawLine)
            throws ModuleSyncException {
        switch (type) {
        case "T":
            return decodeTodoTask(parts, moduleCode, description, isDone, rawLine);
        case "D":
            return decodeDeadlineTask(parts, moduleCode, description, isDone, rawLine);
        default:
            throw new ModuleSyncException("Unsupported task type: " + type);
        }
    }

    /**
     * Restores the {@code completedAt} timestamp on a task if a {@code "completed:..."} field
     * is present among the encoded parts.
     *
     * @param task  the task to update
     * @param parts the split storage fields
     */
    private void restoreCompletedAt(Task task, String[] parts) {
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.startsWith("completed:")) {
                continue;
            }
            String dateStr = trimmed.substring("completed:".length()).trim();
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
                task.setCompletedAt(LocalDateTime.parse(dateStr, formatter));
            } catch (DateTimeParseException ignored) {
                LOGGER.warning("Skipping corrupted completedAt value: " + dateStr);
            }
            break;
        }
    }

    /**
     * Decodes a Todo task from the split storage fields.
     *
     * @param parts       the split fields
     * @param moduleCode  the module code
     * @param description the task description
     * @param isDone      the done flag
     * @param rawLine     the original line (for error messages)
     * @return a new {@link Todo} task
     * @throws ModuleSyncException if the fields are insufficient
     */
    private Task decodeTodoTask(String[] parts, String moduleCode,
                                String description, boolean isDone, String rawLine) throws ModuleSyncException {
        if (parts.length < TODO_FIELDS_WITHOUT_WEIGHTAGE) {
            throw new ModuleSyncException("Corrupted task entry: " + rawLine);
        }
        Todo todo = new Todo(moduleCode, description, isDone);
        if (parts.length >= TODO_FIELDS_WITH_WEIGHTAGE) {
            String candidate = parts[TODO_FIELDS_WITHOUT_WEIGHTAGE].trim();
            if (!candidate.startsWith("completed:")) {
                int weightage = parseWeightage(candidate, rawLine);
                todo.setWeightage(weightage);
            }
        }
        return todo;
    }

    /**
     * Decodes a Deadline task from the split storage fields.
     *
     * @param parts       the split fields
     * @param moduleCode  the module code
     * @param description the task description
     * @param isDone      the done flag
     * @param rawLine     the original line (for error messages)
     * @return a new {@link Deadline} task
     * @throws ModuleSyncException if the deadline date is missing or cannot be parsed
     */
    private Task decodeDeadlineTask(String[] parts, String moduleCode,
                                    String description, boolean isDone, String rawLine) throws ModuleSyncException {
        if (parts.length < MIN_DEADLINE_FIELDS) {
            throw new ModuleSyncException("Corrupted deadline entry: " + rawLine);
        }
        try {
            LocalDateTime byDate = parseDueDate(parts[FIELD_DUE]);
            Deadline deadline = new Deadline(moduleCode, description, isDone, byDate);
            if (parts.length >= DEADLINE_FIELDS_WITH_WEIGHTAGE) {
                String candidate = parts[DEADLINE_FIELDS_WITHOUT_WEIGHTAGE].trim();
                if (!candidate.startsWith("completed:")) {
                    int weightage = parseWeightage(candidate, rawLine);
                    deadline.setWeightage(weightage);
                }
            }
            return deadline;
        } catch (DateTimeParseException e) {
            throw new ModuleSyncException("Corrupted deadline date in entry: " + rawLine);
        }
    }

    /**
     * Parses a weightage integer from a raw storage field string.
     *
     * @param raw     the raw field string
     * @param rawLine the full line (for error messages)
     * @return the weightage integer (0–100)
     * @throws ModuleSyncException if the value is not a valid integer in range
     */
    private int parseWeightage(String raw, String rawLine) throws ModuleSyncException {
        try {
            int value = Integer.parseInt(raw.trim());
            if (value < 0 || value > 100) {
                throw new ModuleSyncException("Corrupted weightage in entry: " + rawLine);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ModuleSyncException("Corrupted weightage in entry: " + rawLine);
        }
    }

    /**
     * Parses a stored due date string into a {@link LocalDateTime}.
     * Accepts date-only (yyyy-MM-dd) or full datetime (yyyy-MM-dd HH:mm) formats.
     *
     * @param raw the raw due date string
     * @return the parsed {@link LocalDateTime}
     */
    private LocalDateTime parseDueDate(String raw) {
        if (raw.length() <= DATE_ONLY_LENGTH) {
            LocalDate datePart = LocalDate.parse(raw);
            return datePart.atTime(23, 59);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
        return LocalDateTime.parse(raw, formatter);
    }

    /**
     * Parses the done-flag field ("1" or "0") from a stored task entry.
     *
     * @param raw the raw done-flag string
     * @return {@code true} if done, {@code false} if not
     * @throws ModuleSyncException if the value is neither "1" nor "0"
     */
    private boolean parseDone(String raw) throws ModuleSyncException {
        if ("1".equals(raw)) {
            return true;
        }
        if ("0".equals(raw)) {
            return false;
        }
        throw new ModuleSyncException("Invalid done flag: " + raw);
    }

    /**
     * Ensures the parent directory of the storage file exists, creating it if necessary.
     *
     * @throws ModuleSyncException if the directory cannot be created
     */
    private void ensureParentDirectory() throws ModuleSyncException {
        try {
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new ModuleSyncException("Unable to create storage directory: " + e.getMessage());
        }
    }
}

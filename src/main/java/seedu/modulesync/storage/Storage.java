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

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.Module;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.task.Deadline;
import seedu.modulesync.task.Task;
import seedu.modulesync.task.Todo;

/**
 * Handles reading and writing of task data to a persistent file.
 */
public class Storage {

    private static final String FIELD_SEPARATOR_REGEX = "\\s*\\|\\s*";
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

    private final Path filePath;

    /**
     * Constructs a Storage instance backed by the given file path.
     *
     * @param filePath the path to the storage file
     */
    public Storage(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads all tasks from the storage file and returns a populated {@link ModuleBook}.
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
                if (line.trim().isEmpty()) {
                    continue;
                }
                Task task = decodeTask(line);
                Module module = moduleBook.getOrCreate(task.getModuleCode());
                module.getTasks().add(task);
            }
            return moduleBook;
        } catch (IOException e) {
            throw new ModuleSyncException("Failed to read storage file: " + e.getMessage());
        }
    }

    /**
     * Saves all tasks in the given {@link ModuleBook} to the storage file.
     *
     * @param moduleBook the module book to persist
     * @throws ModuleSyncException if the file cannot be written
     */
    public void save(ModuleBook moduleBook) throws ModuleSyncException {
        ensureParentDirectory();
        List<String> lines = new ArrayList<>();
        for (Module module : moduleBook.getModules()) {
            for (Task task : module.getTasks().asUnmodifiableList()) {
                lines.add(task.encode());
            }
        }
        try {
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ModuleSyncException("Failed to save tasks: " + e.getMessage());
        }
    }

    /**
     * Decodes a single line from the storage file into a {@link Task}.
     *
     * @param line the raw encoded line
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

        Task task;
        switch (type) {
        case "T":
            task = decodeTodoTask(parts, moduleCode, description, isDone, line);
            break;
        case "D":
            task = decodeDeadlineTask(parts, moduleCode, description, isDone, line);
            break;
        default:
            throw new ModuleSyncException("Unsupported task type: " + type);
        }
        assert task != null : "Decoded task must not be null";
        return task;
    }

    private Task decodeTodoTask(String[] parts, String moduleCode,
                                String description, boolean isDone, String rawLine) throws ModuleSyncException {
        if (parts.length != TODO_FIELDS_WITHOUT_WEIGHTAGE && parts.length != TODO_FIELDS_WITH_WEIGHTAGE) {
            throw new ModuleSyncException("Corrupted task entry: " + rawLine);
        }
        Todo todo = new Todo(moduleCode, description, isDone);
        if (parts.length == TODO_FIELDS_WITH_WEIGHTAGE) {
            int weightage = parseWeightage(parts[TODO_FIELDS_WITHOUT_WEIGHTAGE], rawLine);
            todo.setWeightage(weightage);
        }
        return todo;
    }

    /**
     * Decodes a deadline task from the split storage fields.
     *
     * @param parts       the split fields from the encoded line
     * @param moduleCode  the module code
     * @param description the task description
     * @param isDone      whether the task is marked done
     * @param rawLine     the original encoded line (for error messages)
     * @return a new {@link Deadline} task
     * @throws ModuleSyncException if the deadline date is missing or cannot be parsed
     */
    private Task decodeDeadlineTask(String[] parts, String moduleCode,
                                    String description, boolean isDone, String rawLine) throws ModuleSyncException {
        if (parts.length < MIN_DEADLINE_FIELDS) {
            throw new ModuleSyncException("Corrupted deadline entry: " + rawLine);
        }
        if (parts.length != DEADLINE_FIELDS_WITHOUT_WEIGHTAGE && parts.length != DEADLINE_FIELDS_WITH_WEIGHTAGE) {
            throw new ModuleSyncException("Corrupted deadline entry: " + rawLine);
        }
        try {
            LocalDateTime byDate = parseDueDate(parts[FIELD_DUE]);
            Deadline deadline = new Deadline(moduleCode, description, isDone, byDate);
            if (parts.length == DEADLINE_FIELDS_WITH_WEIGHTAGE) {
                int weightage = parseWeightage(parts[DEADLINE_FIELDS_WITHOUT_WEIGHTAGE], rawLine);
                deadline.setWeightage(weightage);
            }
            return deadline;
        } catch (DateTimeParseException e) {
            throw new ModuleSyncException("Corrupted deadline date in entry: " + rawLine);
        }
    }

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








package seedu.modulesync.parser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import seedu.modulesync.command.AddDeadlineCommand;
import seedu.modulesync.command.AddTodoCommand;
import seedu.modulesync.command.CheckConflictsCommand;
import seedu.modulesync.command.CheckUrgentCommand;
import seedu.modulesync.command.Command;
import seedu.modulesync.command.DeleteCommand;
import seedu.modulesync.command.ExitCommand;
import seedu.modulesync.command.ListCommand;
import seedu.modulesync.command.ListDeadlinesCommand;
import seedu.modulesync.command.ListModulesCommand;
import seedu.modulesync.command.ListNotDoneCommand;
import seedu.modulesync.command.ListSemesterCommand;
import seedu.modulesync.command.ListTopCommand;
import seedu.modulesync.command.MarkCommand;
import seedu.modulesync.command.SemesterStatsCommand;
import seedu.modulesync.command.SetDeadlineCommand;
import seedu.modulesync.command.SetWeightCommand;
import seedu.modulesync.command.StatsCommand;
import seedu.modulesync.command.UnmarkCommand;
import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.semester.SemesterBook;
import seedu.modulesync.storage.SemesterStorage;

/**
 * Parses raw user input strings into executable {@link Command} objects.
 */
public class Parser {

    private static final String CMD_BYE = "bye";
    private static final String CMD_CHECK_CONFLICTS = "check /conflicts";
    private static final String CMD_ALT_CHECK_CONFLICTS = "/conflicts";
    private static final String CMD_CHECK_URGENT = "check /urgent";
    private static final String CMD_ALT_CHECK_URGENT = "/urgent";
    private static final String CMD_ADD = "add";
    private static final String CMD_LIST = "list";
    private static final String CMD_MARK = "mark";
    private static final String CMD_UNMARK = "unmark";
    private static final String CMD_DELETE = "delete";
    private static final String CMD_SETWEIGHT = "setweight";
    private static final String CMD_SETDEADLINE = "setdeadline";
    private static final String CMD_STATS = "stats";
    private static final String CMD_MODULES = "modules";
    private static final String CMD_SEMESTER_STATS = "semesterstats";
    private static final String CMD_SEMESTER = "semester";

    private static final String PREFIX_DEADLINES = "/deadlines";
    private static final String PREFIX_NOT_DONE = "/notdone";
    private static final String PREFIX_LIST_MOD = "/mod";
    private static final String PREFIX_TOP = "/top";
    private static final String PREFIX_MOD = "mod ";
    private static final String PREFIX_TASK = "task ";
    private static final String PREFIX_DUE = "due ";
    private static final String PREFIX_WEIGHTAGE = "w ";

    private static final int CMD_ADD_LENGTH = 3;
    private static final int CMD_MARK_LENGTH = 4;
    private static final int CMD_UNMARK_LENGTH = 6;
    private static final int CMD_DELETE_LENGTH = 6;
    private static final int CMD_SETWEIGHT_LENGTH = 9;
    private static final int CMD_SETDEADLINE_LENGTH = 11;
    private static final int CMD_STATS_LENGTH = 5;

    private static final int PREFIX_MOD_LENGTH = 4;
    private static final int PREFIX_TASK_LENGTH = 5;
    private static final int PREFIX_DUE_LENGTH = 4;
    private static final int PREFIX_WEIGHTAGE_LENGTH = 2;

    private static final int MIN_WEIGHTAGE = 0;
    private static final int MAX_WEIGHTAGE = 100;

    private static final int DATE_ONLY_LENGTH = 10;
    private static final int DATETIME_WITH_DASH_LENGTH = 15;
    private static final int DATETIME_DASH_POSITION = 10;

    private static final String DATETIME_FORMAT = "yyyy-MM-dd HHmm";
    private static final String ADD_USAGE = "Usage: add /mod MOD /task DESCRIPTION [/due YYYY-MM-DD]";
    private static final String UNKNOWN_COMMAND_MSG = "Unknown command. Try: add /mod MOD /task TASK";

    /** Held so semester-level commands can be constructed with the shared context. */
    private final SemesterBook semesterBook;
    private final SemesterStorage semesterStorage;

    /**
     * Constructs a Parser with semester context for constructing semester-level commands.
     *
     * @param semesterBook    the application's semester book
     * @param semesterStorage the semester-level storage
     */
    public Parser(SemesterBook semesterBook, SemesterStorage semesterStorage) {
        this.semesterBook = semesterBook;
        this.semesterStorage = semesterStorage;
    }

    /**
     * No-arg constructor for use in tests.
     * Semester-level commands are not available when constructed this way.
     */
    public Parser() {
        this.semesterBook = null;
        this.semesterStorage = null;
    }

    /**
     * Parses the given raw input string and returns the corresponding {@link Command}.
     *
     * @param input the raw user input
     * @return the parsed {@link Command}
     * @throws ModuleSyncException if the input is empty or does not match any known command
     */
    public Command parse(String input) throws ModuleSyncException {
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            throw new ModuleSyncException("Command cannot be empty.");
        }
        if (trimmed.equalsIgnoreCase(CMD_BYE)) {
            return new ExitCommand();
        }
        if (trimmed.equalsIgnoreCase(CMD_CHECK_CONFLICTS) || trimmed.equalsIgnoreCase(CMD_ALT_CHECK_CONFLICTS)) {
            return new CheckConflictsCommand();
        }
        if (trimmed.equalsIgnoreCase(CMD_CHECK_URGENT) || trimmed.equalsIgnoreCase(CMD_ALT_CHECK_URGENT)) {
            return new CheckUrgentCommand();
        }
        if (trimmed.equalsIgnoreCase(CMD_MODULES)) {
            return new ListModulesCommand();
        }
        if (trimmed.equalsIgnoreCase(CMD_SEMESTER_STATS)) {
            return new SemesterStatsCommand();
        }
        if (trimmed.toLowerCase().startsWith(CMD_SEMESTER)) {
            return parseSemester(trimmed);
        }
        if (trimmed.toLowerCase().startsWith(CMD_ADD)) {
            return parseAdd(trimmed);
        }
        if (trimmed.toLowerCase().startsWith(CMD_LIST)) {
            return parseList(trimmed);
        }
        if (trimmed.toLowerCase().startsWith(CMD_MARK)) {
            return parseMark(trimmed);
        }
        if (trimmed.toLowerCase().startsWith(CMD_UNMARK)) {
            return parseUnmark(trimmed);
        }
        if (trimmed.toLowerCase().startsWith(CMD_DELETE)) {
            return parseDelete(trimmed);
        }
        if (trimmed.toLowerCase().startsWith(CMD_SETWEIGHT)) {
            return parseSetWeight(trimmed);
        }
        if (trimmed.toLowerCase().startsWith(CMD_SETDEADLINE)) {
            return parseSetDeadline(trimmed);
        }
        if (trimmed.toLowerCase().startsWith(CMD_STATS)) {
            return parseStats(trimmed);
        }
        throw new ModuleSyncException(UNKNOWN_COMMAND_MSG);
    }

    /**
     * Parses an "add" command and returns either an {@link AddTodoCommand} or
     * {@link AddDeadlineCommand} depending on whether a due date is provided.
     *
     * @param input the full add command string
     * @return the appropriate add command
     * @throws ModuleSyncException if required fields are missing or the date format is invalid
     */
    private Command parseAdd(String input) throws ModuleSyncException {
        String remainder = extractRemainder(input, CMD_ADD_LENGTH);
        if (remainder.isEmpty()) {
            throw new ModuleSyncException(ADD_USAGE);
        }

        String[] tokens = remainder.split("/");
        String module = extractFieldFromTokens(tokens, PREFIX_MOD, PREFIX_MOD_LENGTH);
        String task = extractFieldFromTokens(tokens, PREFIX_TASK, PREFIX_TASK_LENGTH);
        String due = extractFieldFromTokens(tokens, PREFIX_DUE, PREFIX_DUE_LENGTH);
        String weightageRaw = extractFieldFromTokens(tokens, PREFIX_WEIGHTAGE, PREFIX_WEIGHTAGE_LENGTH);

        if (module == null || module.isEmpty() || task == null || task.isEmpty()) {
            throw new ModuleSyncException(ADD_USAGE);
        }
        assert module != null && !module.trim().isEmpty() : "Module code should be parsed for add command";
        assert task != null && !task.trim().isEmpty() : "Task description should be parsed for add command";

        Integer weightage = parseWeightage(weightageRaw);

        if (due != null && !due.isEmpty()) {
            return buildAddDeadlineCommand(module, task, due, weightage);
        }

        return new AddTodoCommand(module, task, weightage);
    }

    /**
     * Parses an optional weightage string into an Integer.
     * Returns null if no weightage token was provided.
     *
     * @param raw the raw weightage string, or null if the /w flag was absent
     * @return the parsed weightage (0–100), or null
     * @throws ModuleSyncException if the value is not a valid integer or out of range
     */
    private Integer parseWeightage(String raw) throws ModuleSyncException {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        try {
            int value = Integer.parseInt(raw);
            if (value < MIN_WEIGHTAGE || value > MAX_WEIGHTAGE) {
                throw new ModuleSyncException("Weightage must be between " + MIN_WEIGHTAGE
                        + " and " + MAX_WEIGHTAGE + ".");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ModuleSyncException("Weightage must be a whole number between "
                    + MIN_WEIGHTAGE + " and " + MAX_WEIGHTAGE + ".");
        }
    }

    /**
     * Builds an {@link AddDeadlineCommand} by parsing the due date string.
     *
     * @param module the module code
     * @param task   the task description
     * @param due    the raw due date string
     * @param weightage the optional weightage (0–100), or null
     * @return a new {@link AddDeadlineCommand}
     * @throws ModuleSyncException if the due date string cannot be parsed
     */
    private Command buildAddDeadlineCommand(String module, String task, String due,
                                           Integer weightage) throws ModuleSyncException {
        try {
            LocalDateTime byDate = parseDateTime(due);
            assert byDate != null : "Parsed deadline must not be null";
            assert module != null && task != null : "Add deadline command requires parsed module and task";
            return new AddDeadlineCommand(module, task, byDate, weightage);
        } catch (DateTimeParseException e) {
            throw new ModuleSyncException("Invalid date format. Use yyyy-MM-dd or yyyy-MM-dd-HHmm");
        }
    }

    /**
     * Parses a due date string into a {@link LocalDateTime}.
     * Supports date-only format (yyyy-MM-dd) and datetime format (yyyy-MM-dd-HHmm).
     *
     * @param due the raw due string
     * @return the parsed {@link LocalDateTime}
     */
    private LocalDateTime parseDateTime(String due) {
        if (due.length() <= DATE_ONLY_LENGTH) {
            LocalDate date = LocalDate.parse(due);
            return date.atTime(23, 59);
        }
        String normalized = normalizeDateTimeString(due);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
        return LocalDateTime.parse(normalized, formatter);
    }

    /**
     * Normalises a datetime string by replacing a dash separator with a space.
     *
     * @param due the raw datetime string
     * @return the normalised datetime string
     */
    private String normalizeDateTimeString(String due) {
        if (due.length() == DATETIME_WITH_DASH_LENGTH && due.charAt(DATETIME_DASH_POSITION) == '-') {
            return due.substring(0, DATETIME_DASH_POSITION) + " " + due.substring(DATETIME_DASH_POSITION + 1);
        }
        return due;
    }

    /**
     * Extracts a named field value from the parsed slash-delimited tokens.
     *
     * @param tokens        array of tokens split by "/"
     * @param prefix        the prefix to match (e.g. "mod ", "task ")
     * @param prefixLength  the character length of the prefix
     * @return the trimmed field value, or {@code null} if not found
     */
    private String extractFieldFromTokens(String[] tokens, String prefix, int prefixLength) {
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.toLowerCase().startsWith(prefix)) {
                return trimmed.substring(prefixLength).trim();
            }
        }
        return null;
    }

    /**
     * Extracts the remainder of a command string after the command keyword.
     *
     * @param input         the full command string
     * @param commandLength the character length of the command keyword
     * @return the trimmed remainder, or an empty string if none
     */
    private String extractRemainder(String input, int commandLength) {
        return input.length() > commandLength ? input.substring(commandLength).trim() : "";
    }

    /**
     * Parses a "mark" command.
     *
     * @param input the full mark command string
     * @return a {@link MarkCommand} with the specified task number
     * @throws ModuleSyncException if the task number is missing or invalid
     */
    private Command parseMark(String input) throws ModuleSyncException {
        String remainder = extractRemainder(input, CMD_MARK_LENGTH);

        if (remainder.toLowerCase().startsWith(PREFIX_LIST_MOD) || remainder.toLowerCase().contains("/all")) {
            String[] tokens = remainder.split("\\s+");
            if (tokens.length != 3 || !tokens[0].equalsIgnoreCase(PREFIX_LIST_MOD) 
                    || !tokens[2].equalsIgnoreCase("/all")) {
                throw new ModuleSyncException("Usage: mark /mod MODULE_CODE /all");
            }
            String moduleCode = tokens[1];
            if (moduleCode.startsWith("/")) {
                throw new ModuleSyncException("Usage: mark /mod MODULE_CODE /all");
            }
            return new MarkCommand(moduleCode);
        }

        int taskNumber = parseTaskNumber(remainder, CMD_MARK);
        return new MarkCommand(taskNumber);
    }

    /**
     * Parses an "unmark" command.
     *
     * @param input the full unmark command string
     * @return an {@link UnmarkCommand} with the specified task number
     * @throws ModuleSyncException if the task number is missing or invalid
     */
    private Command parseUnmark(String input) throws ModuleSyncException {
        String remainder = extractRemainder(input, CMD_UNMARK_LENGTH);
        int taskNumber = parseTaskNumber(remainder, CMD_UNMARK);
        return new UnmarkCommand(taskNumber);
    }

    /**
     * Parses a "delete" command.
     *
     * @param input the full delete command string
     * @return a {@link DeleteCommand} with the specified task number
     * @throws ModuleSyncException if the task number is missing or invalid
     */
    private Command parseDelete(String input) throws ModuleSyncException {
        String remainder = extractRemainder(input, CMD_DELETE_LENGTH);
        int taskNumber = parseTaskNumber(remainder, CMD_DELETE);
        assert taskNumber > 0 : "Parsed task number must be strictly positive";
        return new DeleteCommand(taskNumber);
    }

    /**
     * Parses a "setweight" command.
     * Format: {@code setweight TASK_NUMBER PERCENT}
     *
     * @param input the full setweight command string
     * @return a {@link SetWeightCommand} with the specified task number and weightage
     * @throws ModuleSyncException if the arguments are missing, non-integer, or out of range
     */
    private Command parseSetWeight(String input) throws ModuleSyncException {
        String remainder = extractRemainder(input, CMD_SETWEIGHT_LENGTH);
        if (remainder.isEmpty()) {
            throw new ModuleSyncException("Usage: setweight TASK_NUMBER PERCENT");
        }
        String[] parts = remainder.split("\\s+");
        if (parts.length != 2) {
            throw new ModuleSyncException("Usage: setweight TASK_NUMBER PERCENT");
        }
        int taskNumber = parseTaskNumber(parts[0], CMD_SETWEIGHT);
        Integer weightage = parseWeightage(parts[1]);
        if (weightage == null) {
            throw new ModuleSyncException("Usage: setweight TASK_NUMBER PERCENT");
        }
        assert taskNumber > 0 : "Parsed task number must be strictly positive";
        assert weightage >= 0 && weightage <= 100 : "Parsed weightage must be 0–100";
        return new SetWeightCommand(taskNumber, weightage);
    }

    /**
     * Parses a "setdeadline" command.
     * Format: {@code setdeadline TASK_NUMBER /by YYYY-MM-DD[-HHmm]}
     *
     * @param input the full setdeadline command string
     * @return a {@link SetDeadlineCommand} with the specified task number and deadline
     * @throws ModuleSyncException if the arguments are missing or invalid
     */
    private Command parseSetDeadline(String input) throws ModuleSyncException {
        String remainder = extractRemainder(input, CMD_SETDEADLINE_LENGTH);
        if (remainder.isEmpty()) {
            throw new ModuleSyncException("Usage: setdeadline TASK_NUMBER /by YYYY-MM-DD[-HHmm]");
        }
        String[] tokens = remainder.split("/by");
        if (tokens.length < 2) {
            throw new ModuleSyncException("Usage: setdeadline TASK_NUMBER /by YYYY-MM-DD[-HHmm]");
        }
        int taskNumber = parseTaskNumber(tokens[0].trim(), CMD_SETDEADLINE);
        String due = tokens[1].trim();
        if (due.isEmpty()) {
            throw new ModuleSyncException("Usage: setdeadline TASK_NUMBER /by YYYY-MM-DD[-HHmm]");
        }
        try {
            LocalDateTime byDate = parseDateTime(due);
            return new SetDeadlineCommand(taskNumber, byDate);
        } catch (DateTimeParseException e) {
            throw new ModuleSyncException("Invalid date format. Use yyyy-MM-dd or yyyy-MM-dd-HHmm");
        }
    }

    /**
     * Parses a "list" command, checking for optional filters like /deadlines.
     *
     * @param input the full list command string
     * @return a {@link ListCommand} or {@link ListDeadlinesCommand} depending on filters
     * @throws ModuleSyncException if an unknown filter is provided
     */
    private Command parseList(String input) throws ModuleSyncException {
        String remainder = extractRemainder(input, CMD_LIST.length());

        if (remainder.isEmpty()) {
            return new ListCommand();
        }

        String[] tokens = remainder.split("\\s+");

        if (tokens.length == 1 && tokens[0].equalsIgnoreCase(PREFIX_DEADLINES)) {
            return new ListDeadlinesCommand();
        }

        if (tokens.length == 2 && tokens[0].equalsIgnoreCase(PREFIX_TOP)) {
            try {
                int topCount = Integer.parseInt(tokens[1]);
                if (topCount <= 0) {
                    throw new ModuleSyncException("Top count must be a positive integer.");
                }
                return new ListTopCommand(topCount);
            } catch (NumberFormatException e) {
                throw new ModuleSyncException("Usage: list /top NUMBER (NUMBER must be a positive integer)");
            }
        }

        if (tokens.length == 2 && tokens[0].equalsIgnoreCase(PREFIX_LIST_MOD)) {
            if (tokens[1].startsWith("/")) {
                throw new ModuleSyncException("Usage: list /mod MODULE_CODE");
            }
            return new ListCommand(tokens[1]);
        }

        if (tokens.length == 3) {
            boolean hasNotDone = false;
            int modFlagIndex = -1;

            for (int i = 0; i < tokens.length; i++) {
                if (tokens[i].equalsIgnoreCase(PREFIX_NOT_DONE)) {
                    hasNotDone = true;
                }
                if (tokens[i].equalsIgnoreCase(PREFIX_LIST_MOD)) {
                    modFlagIndex = i;
                }
            }

            if (hasNotDone && modFlagIndex >= 0 && modFlagIndex + 1 < tokens.length
                    && !tokens[modFlagIndex + 1].startsWith("/")) {
                String moduleCode = tokens[modFlagIndex + 1];
                assert moduleCode != null && !moduleCode.isBlank() : "Module code token must not be blank";
                return new ListNotDoneCommand(moduleCode);
            }
        }

        for (String token : tokens) {
            if (token.equalsIgnoreCase(PREFIX_DEADLINES) && tokens.length > 1) {
                throw new ModuleSyncException("Usage: list /deadlines");
            }
        }

        if (containsToken(tokens, PREFIX_LIST_MOD)) {
            throw new ModuleSyncException("Usage: list /mod MODULE_CODE");
        }

        if (containsToken(tokens, PREFIX_NOT_DONE)) {
            throw new ModuleSyncException("Usage: list /notdone /mod MOD");
        }

        throw new ModuleSyncException(
                "Unknown list filter. Try: list, list /mod CODE, list /deadlines, "
                + "list /top NUMBER or list /notdone /mod MOD");
    }

    private boolean containsToken(String[] tokens, String target) {
        for (String token : tokens) {
            if (token.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses and validates a task number string.
     *
     * @param rawTaskNumber the raw string that should be a positive integer
     * @param commandWord   the command name (used in error messages)
     * @return the parsed task number as an int
     * @throws ModuleSyncException if the string is empty or not a valid integer
     */
    private int parseTaskNumber(String rawTaskNumber, String commandWord) throws ModuleSyncException {
        if (rawTaskNumber.isEmpty()) {
            throw new ModuleSyncException("Usage: " + commandWord + " TASK_NUMBER");
        }
        try {
            int value = Integer.parseInt(rawTaskNumber);
            if (value <= 0) {
                throw new ModuleSyncException("Task number must be a positive integer.");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ModuleSyncException("Task number must be a positive integer.");
        }
    }

    /**
     * Parses a "stats" command.
     * Format: {@code stats /mod MODULE_CODE}
     *
     * @param input the full stats command string
     * @return a {@link StatsCommand} for the specified module
     * @throws ModuleSyncException if the module code is missing or malformed
     */
    private Command parseStats(String input) throws ModuleSyncException {
        String remainder = extractRemainder(input, CMD_STATS_LENGTH);
        if (remainder.isEmpty()) {
            throw new ModuleSyncException("Usage: stats /mod MODULE_CODE");
        }
        String[] tokens = remainder.split("\\s+");
        if (tokens.length != 2 || !tokens[0].equalsIgnoreCase(PREFIX_LIST_MOD)) {
            throw new ModuleSyncException("Usage: stats /mod MODULE_CODE");
        }
        String moduleCode = tokens[1];
        if (moduleCode.startsWith("/")) {
            throw new ModuleSyncException("Usage: stats /mod MODULE_CODE");
        }
        assert moduleCode != null && !moduleCode.isBlank() : "Module code must not be blank for stats command";
        return new StatsCommand(moduleCode);
    }

    /**
     * Parses a "semester" command, looking for sub-commands like "list".
     *
     * @param input the full semester command string
     * @return a corresponding {@link seedu.modulesync.command.SemesterCommand}
     * @throws ModuleSyncException if the command is unknown or arguments are invalid
     */
    private Command parseSemester(String input) throws ModuleSyncException {
        if (semesterBook == null || semesterStorage == null) {
            throw new ModuleSyncException("Semester commands are not available in this context.");
        }
        
        String remainder = extractRemainder(input, CMD_SEMESTER.length());
        
        if (remainder.equalsIgnoreCase(CMD_LIST)) {
            return new ListSemesterCommand(semesterBook, semesterStorage);
        }
        
        throw new ModuleSyncException("Unknown semester command. Try: semester list");
    }
}


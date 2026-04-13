package seedu.modulesync.parser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import seedu.modulesync.command.AddDeadlineCommand;
import seedu.modulesync.command.AddModuleCommand;
import seedu.modulesync.command.AddTodoCommand;
import seedu.modulesync.command.ArchiveModuleCommand;
import seedu.modulesync.command.ArchiveSemesterCommand;
import seedu.modulesync.command.CapCommand;
import seedu.modulesync.command.CheckConflictsCommand;
import seedu.modulesync.command.CheckUrgentCommand;
import seedu.modulesync.command.Command;
import seedu.modulesync.command.DeleteCommand;
import seedu.modulesync.command.DeleteModuleCommand;
import seedu.modulesync.command.EditDeadlineCommand;
import seedu.modulesync.command.EditWeightCommand;
import seedu.modulesync.command.ExitCommand;
import seedu.modulesync.command.GradeCommand;
import seedu.modulesync.command.HelpCommand;
import seedu.modulesync.command.ListCommand;
import seedu.modulesync.command.ListDeadlinesCommand;
import seedu.modulesync.command.ListGradesCommand;
import seedu.modulesync.command.ListModulesCommand;
import seedu.modulesync.command.ListNotDoneCommand;
import seedu.modulesync.command.ListSemesterCommand;
import seedu.modulesync.command.ListTopCommand;
import seedu.modulesync.command.MarkCommand;
import seedu.modulesync.command.NewSemesterCommand;
import seedu.modulesync.command.SemesterStatsCommand;
import seedu.modulesync.command.SetCreditsCommand;
import seedu.modulesync.command.SetDeadlineCommand;
import seedu.modulesync.command.SetWeightCommand;
import seedu.modulesync.command.StatsCommand;
import seedu.modulesync.command.SwitchSemesterCommand;
import seedu.modulesync.command.UnarchiveModuleCommand;
import seedu.modulesync.command.UnarchiveSemesterCommand;
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
    private static final String CMD_EDITWEIGHT = "editweight";
    private static final String CMD_SETCREDITS = "setcredits";
    private static final String CMD_SETDEADLINE = "setdeadline";
    private static final String CMD_EDITDEADLINE = "editdeadline";
    private static final String CMD_STATS = "stats";
    private static final String CMD_GRADES = "grades";
    private static final String CMD_GRADE = "grade";
    private static final String CMD_MODULE = "module";
    private static final String CMD_SEMESTER = "semester";
    private static final String CMD_SWITCH = "switch";
    private static final String CMD_CAP = "cap";
    private static final String CMD_HELP = "help";

    private static final String PREFIX_DEADLINES = "/deadlines";
    private static final String PREFIX_NOT_DONE = "/notdone";
    private static final String PREFIX_LIST_MOD = "/mod";
    private static final String PREFIX_TOP = "/top";
    private static final String PREFIX_MOD = "mod ";
    private static final String PREFIX_TASK = "task ";
    private static final String PREFIX_DUE = "due ";
    private static final String PREFIX_GRADE = "grade ";
    private static final String PREFIX_WEIGHTAGE = "w ";
    private static final String PREFIX_ARCHIVE = "archive";
    private static final String PREFIX_UNARCHIVE = "unarchive";

    private static final int CMD_ADD_LENGTH = 3;
    private static final int CMD_MARK_LENGTH = 4;
    private static final int CMD_UNMARK_LENGTH = 6;
    private static final int CMD_DELETE_LENGTH = 6;
    private static final int CMD_SETWEIGHT_LENGTH = 9;
    private static final int CMD_EDITWEIGHT_LENGTH = 10;
    private static final int CMD_SETCREDITS_LENGTH = 10;
    private static final int CMD_SETDEADLINE_LENGTH = 11;
    private static final int CMD_EDITDEADLINE_LENGTH = 12;
    private static final int CMD_STATS_LENGTH = 5;
    private static final int CMD_MODULE_LENGTH = 6;
    private static final int CMD_GRADE_LENGTH = 5;

    private static final int PREFIX_MOD_LENGTH = 4;
    private static final int PREFIX_TASK_LENGTH = 5;
    private static final int PREFIX_DUE_LENGTH = 4;
    private static final int PREFIX_GRADE_LENGTH = 6;
    private static final int PREFIX_WEIGHTAGE_LENGTH = 2;

    private static final int MIN_WEIGHTAGE = 0;
    private static final int MAX_WEIGHTAGE = 100;

    private static final int DATE_ONLY_LENGTH = 10;
    private static final int DATETIME_WITH_DASH_LENGTH = 15;
    private static final int DATETIME_DASH_POSITION = 10;

    private static final String DATETIME_FORMAT = "yyyy-MM-dd HHmm";
    private static final String ADD_USAGE = "Usage: add /mod MOD /task DESCRIPTION [/due YYYY-MM-DD]";
    private static final String UNKNOWN_COMMAND_MSG = "Unknown command. Try: add /mod MOD /task TASK";

    /**
     * Held so semester-level commands can be constructed with the shared context.
     */
    private final SemesterBook semesterBook;
    private final SemesterStorage semesterStorage;

    /**
     * Constructs a Parser with semester context for constructing semester-level
     * commands.
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
     * Parses the given raw input string and returns the corresponding
     * {@link Command}.
     *
     * @param input the raw user input
     * @return the parsed {@link Command}
     * @throws ModuleSyncException if the input is empty or does not match any known
     *                             command
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
        if (trimmed.equalsIgnoreCase(CMD_CAP)) {
            if (semesterBook == null || semesterStorage == null) {
                throw new ModuleSyncException("CAP commands are not available in this context.");
            }
            return new CapCommand(semesterBook, semesterStorage);
        }
        if (trimmed.toLowerCase().startsWith(CMD_GRADES)) {
            return parseGrades(trimmed);
        }
        if (trimmed.toLowerCase().startsWith(CMD_SEMESTER)) {
            return parseSemester(trimmed);
        }
        if (trimmed.toLowerCase().startsWith(CMD_MODULE)) {
            return parseModule(trimmed);
        }
        if (trimmed.toLowerCase().startsWith(CMD_ADD)) {
            return parseAdd(trimmed);
        }
        if (trimmed.toLowerCase().startsWith(CMD_GRADE)) {
            return parseGrade(trimmed);
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
        if (trimmed.toLowerCase().startsWith(CMD_EDITWEIGHT)) {
            return parseEditWeight(trimmed);
        }
        if (trimmed.toLowerCase().startsWith(CMD_SETCREDITS)) {
            return parseSetCredits(trimmed);
        }
        if (trimmed.toLowerCase().startsWith(CMD_SETDEADLINE)) {
            return parseSetDeadline(trimmed);
        }
        if (trimmed.toLowerCase().startsWith(CMD_EDITDEADLINE)) {
            return parseEditDeadline(trimmed);
        }
        if (trimmed.toLowerCase().startsWith(CMD_STATS)) {
            return parseStats(trimmed);
        }
        if (trimmed.equalsIgnoreCase(CMD_HELP)) {
            return new HelpCommand();
        }
        throw new ModuleSyncException(UNKNOWN_COMMAND_MSG);
    }

    /**
     * Parses a "grades" command and returns the supported grades sub-command.
     *
     * @param input the full grades command string
     * @return the supported grades sub-command
     * @throws ModuleSyncException if the grades command is unavailable or malformed
     */
    private Command parseGrades(String input) throws ModuleSyncException {
        if (semesterBook == null || semesterStorage == null) {
            throw new ModuleSyncException("Grades commands are not available in this context.");
        }

        String remainder = extractRemainder(input, CMD_GRADES.length());
        if (remainder.equalsIgnoreCase(CMD_LIST)) {
            return new ListGradesCommand(semesterBook, semesterStorage);
        }

        throw new ModuleSyncException("Unknown grades command. Try: grades list");
    }

    /**
     * Parses an "add" command and returns either an {@link AddTodoCommand} or
     * {@link AddDeadlineCommand} depending on whether a due date is provided.
     *
     * @param input the full add command string
     * @return the appropriate add command
     * @throws ModuleSyncException if required fields are missing or the date format
     *                             is invalid
     */
    private Command parseAdd(String input) throws ModuleSyncException {
        String remainder = extractRemainder(input, CMD_ADD_LENGTH);
        if (remainder.isEmpty()) {
            throw new ModuleSyncException(ADD_USAGE);
        }

        // Validate that all flags are recognized
        validateAllFlagsRecognized(remainder, new String[]{"mod", "task", "due", "w"});

        // Find critical flag positions: /mod, /task, /due, /w
        int modPos = findFlagPosition(remainder, "mod");
        int taskPos = findFlagPosition(remainder, "task");
        int duePos = findFlagPosition(remainder, "due");
        int weightPos = findFlagPosition(remainder, "w");

        // /mod is required
        if (modPos == -1) {
            throw new ModuleSyncException(ADD_USAGE);
        }

        // If /task exists, it must come after /mod
        if (taskPos != -1 && modPos > taskPos) {
            throw new ModuleSyncException(
                "Invalid input! /mod must appear before /task.");
        }

        // Extract /mod MODULE_CODE
        String module = extractFlagValue(remainder, modPos, "mod");
        if (module == null || module.isEmpty()) {
            throw new ModuleSyncException(ADD_USAGE);
        }
        validateModuleCode(module);

        // If no /task is provided, just add the module
        if (taskPos == -1) {
            return new AddModuleCommand(module);
        }

        // Extract /task DESCRIPTION (up to next flag or end of string)
        int taskEndPos = remainder.length();
        if (duePos != -1 && duePos > taskPos) {
            taskEndPos = Math.min(taskEndPos, duePos);
        }
        if (weightPos != -1 && weightPos > taskPos) {
            taskEndPos = Math.min(taskEndPos, weightPos);
        }

        int taskContentStart = taskPos + PREFIX_TASK_LENGTH + 1;
        // Clamp taskContentStart to valid range and ensure it doesn't exceed taskEndPos
        if (taskContentStart > remainder.length()) {
            taskContentStart = remainder.length();
        }
        if (taskContentStart > taskEndPos) {
            taskContentStart = taskEndPos;
        }
        String task = remainder.substring(taskContentStart, taskEndPos).trim();

        // Check for duplicate /due flags
        if (countFlagOccurrences(remainder, "due") > 1) {
            throw new ModuleSyncException(
                "Invalid input! The deadline flag (/due) can only be provided once.");
        }

        // Check for duplicate /w flags
        if (countFlagOccurrences(remainder, "w") > 1) {
            throw new ModuleSyncException(
                "Invalid input! The weightage flag (/w) can only be provided once.");
        }

        // Extract optional /due and /w from the remainder
        String due = null;
        String weightageRaw = null;

        if (duePos != -1) {
            due = extractFlagValue(remainder, duePos, "due");
            if (due == null || due.isEmpty()) {
                throw new ModuleSyncException(
                    "Invalid input! /due requires a date value. "
                    + "Format: /due YYYY-MM-DD or /due YYYY-MM-DD-HHmm");
            }
        }

        if (weightPos != -1) {
            weightageRaw = extractFlagValue(remainder, weightPos, "w");
            if (weightageRaw == null || weightageRaw.isEmpty()) {
                throw new ModuleSyncException(
                    "Invalid input! /w requires an integer from 0 to 100.");
            }
        }

        // If task is empty after /task, still treat as module add
        if (task.isEmpty()) {
            return new AddModuleCommand(module);
        }

        assert module != null && !module.trim().isEmpty()
            : "Module code should be parsed for add command";
        assert task != null && !task.trim().isEmpty()
            : "Task description should be parsed for add command";

        Integer weightage = parseWeightage(weightageRaw);

        if (due != null && !due.isEmpty()) {
            return buildAddDeadlineCommand(module, task, due, weightage);
        }

        return new AddTodoCommand(module, task, weightage);
    }

    /**
     * Finds the position of a flag in the input string (case-insensitive).
     * 
     * @param input the input string to search
     * @param flagName the flag name without the slash (e.g., "mod", "task", "due", "w")
     * @return the position of the "/" character that precedes the flag, or -1 if not found
     */
    private int findFlagPosition(String input, String flagName) {
        int index = 0;
        int flagLength = flagName.length();
        while (index < input.length()) {
            if (input.charAt(index) == '/') {
                int endPos = Math.min(index + flagLength + 1, input.length());
                if (endPos <= input.length()) {
                    String potentialFlag = input.substring(index + 1, endPos);
                    // Check if it matches the flag and is followed by space (or end of string)
                    if (potentialFlag.equalsIgnoreCase(flagName + " ") ||
                        (potentialFlag.equalsIgnoreCase(flagName) && 
                         (endPos == input.length() || input.charAt(endPos) == ' '))) {
                        return index;
                    }
                }
            }
            index++;
        }
        return -1;
    }

    /**
     * Validates that all flags in the input string are recognized.
     * Throws an exception if any unrecognized flag is found.
     * 
     * @param input the input string to validate
     * @param recognizedFlags array of recognized flag names without the slash
     * @throws ModuleSyncException if an unrecognized flag is found
     */
    private void validateAllFlagsRecognized(String input, String[] recognizedFlags) 
            throws ModuleSyncException {
        int index = 0;
        while (index < input.length()) {
            if (input.charAt(index) == '/') {
                // Extract the flag name (everything between / and the next space or end)
                int endPos = index + 1;
                while (endPos < input.length() && input.charAt(endPos) != ' ') {
                    endPos++;
                }
                String flagName = input.substring(index + 1, endPos).toLowerCase();
                
                // Check if this flag is recognized
                boolean isRecognized = false;
                for (String recognized : recognizedFlags) {
                    if (flagName.equalsIgnoreCase(recognized)) {
                        isRecognized = true;
                        break;
                    }
                }
                
                if (!isRecognized) {
                    throw new ModuleSyncException(
                        "Invalid flag '/" + flagName + "'! Recognized flags are: /mod, /task, /due, /w");
                }
            }
            index++;
        }
    }

    /**
     * Extracts the value for a flag at a given position.
     * For example, if input is "/mod CS2113 /task", this extracts "CS2113" for "/mod".
     * 
     * @param input the input string
     * @param flagPos the position of the "/" character
     * @param flagName the flag name without the slash
     * @return the value after the flag, or empty string if none
     */
    private String extractFlagValue(String input, int flagPos, String flagName) {
        // Start position is after "/FLAGNAME "
        int valueStartPos = flagPos + 1 + flagName.length();
        // Skip the space after the flag name if present
        if (valueStartPos < input.length() && input.charAt(valueStartPos) == ' ') {
            valueStartPos++;
        }

        // Find the end of the value (next flag or end of string)
        int valueEndPos = valueStartPos;
        while (valueEndPos < input.length() && input.charAt(valueEndPos) != '/') {
            valueEndPos++;
        }

        // Extract and trim the value
        if (valueStartPos < input.length()) {
            return input.substring(valueStartPos, valueEndPos).trim();
        }
        return "";
    }

    /**
     * Counts how many times a flag appears in the input string (case-insensitive).
     * 
     * @param input the input string to search
     * @param flagName the flag name without the slash
     * @return the number of times the flag appears
     */
    private int countFlagOccurrences(String input, String flagName) {
        int count = 0;
        int index = 0;
        int flagLength = flagName.length();
        while (index < input.length()) {
            if (input.charAt(index) == '/') {
                int endPos = Math.min(index + flagLength + 1, input.length());
                if (endPos <= input.length()) {
                    String potentialFlag = input.substring(index + 1, endPos);
                    if (potentialFlag.equalsIgnoreCase(flagName + " ") ||
                        (potentialFlag.equalsIgnoreCase(flagName) && 
                         (endPos == input.length() || input.charAt(endPos) == ' '))) {
                        count++;
                    }
                }
            }
            index++;
        }
        return count;
    }

    /**
     * Finds the position of the /task flag in the input string.
     * The search is case-insensitive to support /TASK, /Task, /task, etc.
     * 
     * @param input the input string to search
     * @return the position of the "/" character that precedes "task ", 
     *         or -1 if the /task flag is not found
     */
    private int findTaskFlagPosition(String input) {
        return findFlagPosition(input, "task");
    }

    /**
     * Checks if a flag is present in the token array.
     * A flag is considered present if any token starts with the flag prefix.
     * 
     * @param tokens the token array to search
     * @param flagPrefix the flag prefix to look for (e.g., "due ", "w ")
     * @return true if the flag is present in tokens, false otherwise
     */
    private boolean hasFlagInTokens(String[] tokens, String flagPrefix) {
        for (String token : tokens) {
            String trimmed = token.trim().toLowerCase();
            if (trimmed.startsWith(flagPrefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses a "grade" command and returns the corresponding GradeCommand.
     *
     * @param input the full grade command string
     * @return a {@link GradeCommand} with the parsed module code and grade
     * @throws ModuleSyncException if the module code or grade is missing or
     *                             malformed
     */
    private Command parseGrade(String input) throws ModuleSyncException {
        String remainder = extractRemainder(input, CMD_GRADE_LENGTH);
        if (remainder.isEmpty()) {
            throw new ModuleSyncException("Usage: grade /mod MODULECODE /grade GRADEVALUE");
        }

        String[] tokens = remainder.split("/(?i)(?=(mod |task |due |w |grade ))");
        String module = extractFieldFromTokens(tokens, PREFIX_MOD, PREFIX_MOD_LENGTH);
        String grade = extractFieldFromTokens(tokens, PREFIX_GRADE, PREFIX_GRADE_LENGTH);

        if (module == null || module.isEmpty() || grade == null || grade.isEmpty()) {
            throw new ModuleSyncException("Usage: grade /mod MODULECODE /grade GRADEVALUE");
        }
        validateModuleCode(module);

        java.util.List<String> validGrades = java.util.List.of(
                "A+", "A", "A-", "B+", "B", "B-", "C+", "C", "D+", "D", "F", "CS", "CU", "S", "U"
        );
        if (!validGrades.contains(grade.trim().toUpperCase())) {
            throw new ModuleSyncException(
                    "Invalid grade! Accepted values are: A+, A, A-, B+, B, B-, C+, C, D+, D, F, CS, CU, S, U");
        }

        assert module != null && !module.trim().isEmpty() : "Module code should be parsed for grade command";
        assert grade != null && !grade.trim().isEmpty() : "Grade should be parsed for grade command";

        return new GradeCommand(module, grade);
    }

    /**
     * Parses an optional weightage string into an Integer.
     * Returns null if no weightage token was provided.
     *
     * @param raw the raw weightage string, or null if the /w flag was absent
     * @return the parsed weightage (0–100), or null
     * @throws ModuleSyncException if the value is not a valid integer or out of
     *                             range
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
     * Builds an {@link AddDeadlineCommand} by parsing the due date string and validating it.
     *
     * @param module    the module code
     * @param task      the task description
     * @param due       the raw due date string
     * @param weightage the optional weightage (0–100), or null
     * @return a new {@link AddDeadlineCommand}
     * @throws ModuleSyncException if the due date string cannot be parsed or is in the past
     */
    private Command buildAddDeadlineCommand(String module, String task, String due,
            Integer weightage) throws ModuleSyncException {
        try {
            LocalDateTime byDate = parseDateTime(due);
            assert byDate != null : "Parsed deadline must not be null";
            assert module != null && task != null : "Add deadline command requires parsed module and task";
            
            // Validate that the deadline is not in the past
            LocalDateTime now = LocalDateTime.now();
            if (byDate.isBefore(now)) {
                throw new ModuleSyncException("Error: Deadline cannot be in the past. Please enter a future date.");
            }
            
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
     * @throws ModuleSyncException if the time is 2400 or greater
     */
    private LocalDateTime parseDateTime(String due) throws ModuleSyncException {
        if (due.length() > DATE_ONLY_LENGTH) {
            String timeString = due.substring(due.length() - 4);
            try {
                int time = Integer.parseInt(timeString);
                if (time >= 2400) {
                    throw new ModuleSyncException("Invalid time format! Please use 2359 for the end of the day, "
                            + "or 0000 for the start of the next day.");
                }
            } catch (NumberFormatException ignored) {
                // Let DateTimeParseException handle non-numeric inputs
            }
        }

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
     * @param tokens       array of tokens split by "/"
     * @param prefix       the prefix to match (e.g. "mod ", "task ")
     * @param prefixLength the character length of the prefix
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

    private void validateModuleCode(String moduleCode) throws ModuleSyncException {
        if (moduleCode == null || !moduleCode.matches("^[a-zA-Z0-9]+$")) {
            throw new ModuleSyncException(
                    "Invalid module code! Module codes must be a single alphanumeric word with no special characters.");
        }
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
            validateModuleCode(moduleCode);
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
        
        if (remainder.toLowerCase().startsWith("module /mod")) {
            String moduleCode = remainder.substring("module /mod".length()).trim();
            if (moduleCode.isEmpty()) {
                throw new ModuleSyncException("Usage: delete module /mod MODULE_CODE");
            }
            validateModuleCode(moduleCode);
            return new DeleteModuleCommand(moduleCode);
        }

        int taskNumber = parseTaskNumber(remainder, CMD_DELETE);
        assert taskNumber > 0 : "Parsed task number must be strictly positive";
        return new DeleteCommand(taskNumber);
    }

    /**
     * Parses a "setweight" command.
     * Format: {@code setweight TASK_NUMBER PERCENT}
     *
     * @param input the full setweight command string
     * @return a {@link SetWeightCommand} with the specified task number and
     *         weightage
     * @throws ModuleSyncException if the arguments are missing, non-integer, or out
     *                             of range
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
     * Parses a "setcredits" command.
     * Format: {@code setcredits /mod MODULECODE /mc CREDITS}
     *
     * @param input the full setcredits command string
     * @return a {@link SetCreditsCommand} with the parsed module code and credits
     * @throws ModuleSyncException if the arguments are missing or malformed
     */
    private Command parseSetCredits(String input) throws ModuleSyncException {
        String remainder = extractRemainder(input, CMD_SETCREDITS_LENGTH);
        if (remainder.isEmpty()) {
            throw new ModuleSyncException("Usage: setcredits /mod MODULECODE /mc CREDITS");
        }

        String[] tokens = remainder.split("/(?i)(?=(mod |mc ))");
        String moduleCode = extractFieldFromTokens(tokens, PREFIX_MOD, PREFIX_MOD_LENGTH);
        String mcStr = extractFieldFromTokens(tokens, "mc ", 3);

        if (moduleCode == null || moduleCode.isEmpty() || mcStr == null || mcStr.isEmpty()) {
            throw new ModuleSyncException("Usage: setcredits /mod MODULECODE /mc CREDITS");
        }
        validateModuleCode(moduleCode);

        int credits;
        try {
            credits = Integer.parseInt(mcStr);
            if (credits < 0 || credits > 40) {
                throw new ModuleSyncException("Credits must be between 0 and 40.");
            }
        } catch (NumberFormatException e) {
            throw new ModuleSyncException("Credits must be a valid integer between 0 and 40.");
        }

        return new SetCreditsCommand(moduleCode, credits);
    }

    /**
     * Parses an "editweight" command.
     * Format: {@code editweight TASK_NUMBER /w PERCENT}
     *
     * @param input the full editweight command string
     * @return an {@link EditWeightCommand} with the specified task number and
     *         weightage
     * @throws ModuleSyncException if the arguments are missing, non-integer, or out
     *                             of range
     */
    private Command parseEditWeight(String input) throws ModuleSyncException {
        String remainder = extractRemainder(input, CMD_EDITWEIGHT_LENGTH);
        if (remainder.isEmpty()) {
            throw new ModuleSyncException("Usage: editweight TASK_NUMBER /w PERCENT");
        }
        String[] tokens = remainder.split("/w");
        if (tokens.length < 2) {
            throw new ModuleSyncException("Usage: editweight TASK_NUMBER /w PERCENT");
        }
        if (tokens.length > 2) {
            throw new ModuleSyncException("Invalid input! The weightage flag (/w) can only be provided once.");
        }
        int taskNumber = parseTaskNumber(tokens[0].trim(), CMD_EDITWEIGHT);
        String weightageRaw = tokens[1].trim();
        if (weightageRaw.isEmpty()) {
            throw new ModuleSyncException("Usage: editweight TASK_NUMBER /w PERCENT");
        }
        Integer weightage = parseWeightage(weightageRaw);
        if (weightage == null) {
            throw new ModuleSyncException("Usage: editweight TASK_NUMBER /w PERCENT");
        }
        return new EditWeightCommand(taskNumber, weightage);
    }

    /**
     * Parses a "setdeadline" command.
     * Format: {@code setdeadline TASK_NUMBER /by YYYY-MM-DD[-HHmm]}
     *
     * @param input the full setdeadline command string
     * @return a {@link SetDeadlineCommand} with the specified task number and
     *         deadline
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
     * Parses an "editdeadline" command.
     * Format: {@code editdeadline TASK_NUMBER /by YYYY-MM-DD[-HHmm]}
     *
     * @param input the full editdeadline command string
     * @return an {@link EditDeadlineCommand} with the specified task number and
     *         deadline
     * @throws ModuleSyncException if the arguments are missing or invalid
     */
    private Command parseEditDeadline(String input) throws ModuleSyncException {
        String remainder = extractRemainder(input, CMD_EDITDEADLINE_LENGTH);
        if (remainder.isEmpty()) {
            throw new ModuleSyncException("Usage: editdeadline TASK_NUMBER /by YYYY-MM-DD[-HHmm]");
        }
        String[] tokens = remainder.split("/by");
        if (tokens.length < 2) {
            throw new ModuleSyncException("Usage: editdeadline TASK_NUMBER /by YYYY-MM-DD[-HHmm]");
        }
        int taskNumber = parseTaskNumber(tokens[0].trim(), CMD_EDITDEADLINE);
        String due = tokens[1].trim();
        if (due.isEmpty()) {
            throw new ModuleSyncException("Usage: editdeadline TASK_NUMBER /by YYYY-MM-DD[-HHmm]");
        }
        try {
            LocalDateTime byDate = parseDateTime(due);
            return new EditDeadlineCommand(taskNumber, byDate);
        } catch (DateTimeParseException e) {
            throw new ModuleSyncException("Invalid date format. Use yyyy-MM-dd or yyyy-MM-dd-HHmm");
        }
    }

    /**
     * Parses a "list" command, checking for optional filters like /deadlines.
     *
     * @param input the full list command string
     * @return a {@link ListCommand} or {@link ListDeadlinesCommand} depending on
     *         filters
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
            validateModuleCode(tokens[1]);
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
                validateModuleCode(moduleCode);
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
        if (tokens.length != 2) {
            throw new ModuleSyncException("Usage: stats /mod MODULE_CODE");
        }

        String scope = tokens[0];
        String value = tokens[1];
        if (value.startsWith("/")) {
            throw new ModuleSyncException("Usage: stats /mod MODULE_CODE");
        }

        if (scope.equalsIgnoreCase(PREFIX_LIST_MOD)) {
            assert !value.isBlank() : "Module code must not be blank for stats command";
            validateModuleCode(value);
            return new StatsCommand(value);
        }

        throw new ModuleSyncException("Usage: stats /mod MODULE_CODE");
    }

    /**
     * Parses a "semester" command, looking for sub-commands like "list".
     *
     * @param input the full semester command string
     * @return a corresponding {@link seedu.modulesync.command.SemesterCommand}
     * @throws ModuleSyncException if the command is unknown or arguments are
     *                             invalid
     */
    private Command parseSemester(String input) throws ModuleSyncException {
        if (semesterBook == null || semesterStorage == null) {
            throw new ModuleSyncException("Semester commands are not available in this context.");
        }

        String remainder = extractRemainder(input, CMD_SEMESTER.length());

        if (remainder.isEmpty()) {
            throw new ModuleSyncException("Usage: semester new SEMESTER_NAME or semester list");
        }

        String[] parts = remainder.split("\\s+", 2);
        String subcommand = parts[0].toLowerCase();

        if (subcommand.equals(CMD_LIST)) {
            return new ListSemesterCommand(semesterBook, semesterStorage);
        } else if (subcommand.equals("new")) {
            if (parts.length < 2 || parts[1].trim().isEmpty()) {
                throw new ModuleSyncException("Usage: semester new SEMESTER_NAME");
            }
            String semesterName = parts[1].trim();
            return new NewSemesterCommand(semesterBook, semesterStorage, semesterName);
        }
        if (subcommand.equals("archive")) {
            if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                throw new ModuleSyncException("Usage: semester archive");
            }
            return new ArchiveSemesterCommand(semesterBook, semesterStorage);
        }
        if (subcommand.equals("unarchive")) {
            if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                throw new ModuleSyncException("Usage: semester unarchive");
            }
            return new UnarchiveSemesterCommand(semesterBook, semesterStorage);
        }
        if (subcommand.equals("stats")) {
            if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                throw new ModuleSyncException("Usage: semester stats");
            }
            return new SemesterStatsCommand();
        }
        if (remainder.toLowerCase().startsWith(CMD_SWITCH)) {
            return parseSemesterSwitch(remainder);
        }

        throw new ModuleSyncException("Unknown semester command. Try: semester list or semester switch NAME");
    }

    /**
     * Parses a "semester switch" command.
     *
     * @param remainder the portion after the word "semester"
     * @return a command that switches to the requested semester
     * @throws ModuleSyncException if the semester name is missing
     */
    private Command parseSemesterSwitch(String remainder) throws ModuleSyncException {
        String semesterName = extractRemainder(remainder, CMD_SWITCH.length());
        if (semesterName.isEmpty()) {
            throw new ModuleSyncException("Usage: semester switch SEMESTER_NAME");
        }
        return new SwitchSemesterCommand(semesterBook, semesterStorage, semesterName);
    }

    /**
     * Parses a "module" command and returns the appropriate module-level command.
     * Supports:
     * - {@code module list}
    * - {@code module delete /mod MODULECODE}
     * - {@code module archive /mod MODULECODE}
     * - {@code module unarchive /mod MODULECODE}
     *
     * @param input the full module command string
     * @return the appropriate module command
     * @throws ModuleSyncException if the command format is invalid or unknown
     */
    private Command parseModule(String input) throws ModuleSyncException {
        String remainder = extractRemainder(input, CMD_MODULE_LENGTH);

        if (remainder.isEmpty()) {
            throw new ModuleSyncException(
                "Usage: module list | module delete /mod MODULECODE | module archive /mod MODULECODE | "
                            + "module unarchive /mod MODULECODE");
        }

        String[] parts = remainder.split("\\s+", 2);
        String subcommand = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        if (subcommand.equals(CMD_LIST)) {
            if (!args.isBlank()) {
                throw new ModuleSyncException("Usage: module list");
            }
            return new ListModulesCommand();
        }
        if (subcommand.equals(CMD_DELETE)) {
            return parseModuleDelete(args);
        }
        if (subcommand.equals(PREFIX_ARCHIVE)) {
            return parseModuleArchive(args);
        } else if (subcommand.equals(PREFIX_UNARCHIVE)) {
            return parseModuleUnarchive(args);
        }

        throw new ModuleSyncException(
                "Unknown module command. Try: module list, module delete /mod MODULECODE, "
                        + "module archive /mod MODULECODE, or module unarchive /mod MODULECODE");
    }

    /**
     * Parses the module delete subcommand.
     *
     * @param args the arguments after the "delete" keyword
     * @return a {@link DeleteModuleCommand}
     * @throws ModuleSyncException if the module code is missing or malformed
     */
    private Command parseModuleDelete(String args) throws ModuleSyncException {
        if (args.isEmpty()) {
            throw new ModuleSyncException("Usage: module delete /mod MODULECODE");
        }

        String[] tokens = args.split("/");
        String moduleCode = null;
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.toLowerCase().startsWith("mod")) {
                if (trimmed.length() > 3) {
                    moduleCode = trimmed.substring(3).trim();
                }
                break;
            }
        }

        if (moduleCode == null || moduleCode.isEmpty()) {
            throw new ModuleSyncException("Usage: module delete /mod MODULECODE");
        }
        validateModuleCode(moduleCode);

        return new DeleteModuleCommand(moduleCode);
    }

    /**
     * Parses the module archive subcommand.
     *
     * @param args the arguments after the "archive" keyword
     * @return an {@link ArchiveModuleCommand}
     * @throws ModuleSyncException if the module code is missing or malformed
     */
    private Command parseModuleArchive(String args) throws ModuleSyncException {
        if (args.isEmpty()) {
            throw new ModuleSyncException("Usage: module archive /mod MODULECODE");
        }

        String[] tokens = args.split("/");
        String moduleCode = null;
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.toLowerCase().startsWith("mod")) {
                // Extract everything after "mod "
                if (trimmed.length() > 3) {
                    moduleCode = trimmed.substring(3).trim();
                }
                break;
            }
        }

        if (moduleCode == null || moduleCode.isEmpty()) {
            throw new ModuleSyncException("Usage: module archive /mod MODULECODE");
        }
        validateModuleCode(moduleCode);

        return new ArchiveModuleCommand(moduleCode);
    }

    /**
     * Parses the module unarchive subcommand.
     *
     * @param args the arguments after the "unarchive" keyword
     * @return an {@link UnarchiveModuleCommand}
     * @throws ModuleSyncException if the module code is missing or malformed
     */
    private Command parseModuleUnarchive(String args) throws ModuleSyncException {
        if (args.isEmpty()) {
            throw new ModuleSyncException("Usage: module unarchive /mod MODULECODE");
        }

        String[] tokens = args.split("/");
        String moduleCode = null;
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.toLowerCase().startsWith("mod")) {
                // Extract everything after "mod "
                if (trimmed.length() > 3) {
                    moduleCode = trimmed.substring(3).trim();
                }
                break;
            }
        }

        if (moduleCode == null || moduleCode.isEmpty()) {
            throw new ModuleSyncException("Usage: module unarchive /mod MODULECODE");
        }
        validateModuleCode(moduleCode);

        return new UnarchiveModuleCommand(moduleCode);
    }
}

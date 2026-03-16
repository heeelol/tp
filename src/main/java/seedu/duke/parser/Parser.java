package seedu.duke.parser;

import seedu.duke.command.AddTodoCommand;
import seedu.duke.command.Command;
import seedu.duke.command.ExitCommand;
import seedu.duke.command.ListCommand;
import seedu.duke.command.MarkCommand;
import seedu.duke.command.UnmarkCommand;
import seedu.duke.exception.ModuleSyncException;

public class Parser {
    public Command parse(String input) throws ModuleSyncException {
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            throw new ModuleSyncException("Command cannot be empty.");
        }
        if (trimmed.equalsIgnoreCase("bye")) {
            return new ExitCommand();
        }
        if (trimmed.toLowerCase().startsWith("add")) {
            return parseAdd(trimmed);
        }
        if (trimmed.equalsIgnoreCase("list")) {
            return new ListCommand();
        }
        if (trimmed.toLowerCase().startsWith("mark")) {
            return parseMark(trimmed);
        }
        if (trimmed.toLowerCase().startsWith("unmark")) {
            return parseUnmark(trimmed);
        }
        throw new ModuleSyncException("Unknown command. Try: add /mod MOD /task TASK");
    }

    private Command parseAdd(String input) throws ModuleSyncException {
        String remainder = input.length() > 3 ? input.substring(3).trim() : "";
        if (remainder.isEmpty()) {
            throw new ModuleSyncException("Usage: add /mod MOD /task DESCRIPTION");
        }

        String[] tokens = remainder.split("/");
        String module = null;
        String task = null;
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String lower = trimmed.toLowerCase();
            if (lower.startsWith("mod ")) {
                module = trimmed.substring(4).trim();
            } else if (lower.startsWith("task ")) {
                task = trimmed.substring(5).trim();
            }
        }

        if (module == null || module.isEmpty() || task == null || task.isEmpty()) {
            throw new ModuleSyncException("Usage: add /mod MOD /task DESCRIPTION");
        }
        return new AddTodoCommand(module, task);
    }

    private Command parseMark(String input) throws ModuleSyncException {
        String remainder = input.length() > 4 ? input.substring(4).trim() : "";
        int taskNumber = parseTaskNumber(remainder, "mark");
        return new MarkCommand(taskNumber);
    }

    private Command parseUnmark(String input) throws ModuleSyncException {
        String remainder = input.length() > 6 ? input.substring(6).trim() : "";
        int taskNumber = parseTaskNumber(remainder, "unmark");
        return new UnmarkCommand(taskNumber);
    }

    private int parseTaskNumber(String rawTaskNumber, String commandWord) throws ModuleSyncException {
        if (rawTaskNumber.isEmpty()) {
            throw new ModuleSyncException("Usage: " + commandWord + " TASK_NUMBER");
        }
        try {
            return Integer.parseInt(rawTaskNumber);
        } catch (NumberFormatException e) {
            throw new ModuleSyncException("Task number must be a positive integer.");
        }
    }
}

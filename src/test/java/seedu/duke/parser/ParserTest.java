package seedu.duke.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import seedu.duke.command.AddTodoCommand;
import seedu.duke.command.MarkCommand;
import seedu.duke.command.UnmarkCommand;
import seedu.duke.exception.ModuleSyncException;

class ParserTest {

    @Test
    void parse_addCommand_returnsAddTodo() throws ModuleSyncException {
        Parser parser = new Parser();
        assertTrue(parser.parse("add /mod CS2113 /task Week8") instanceof AddTodoCommand);
        assertTrue(parser.parse("add /task Week8 /mod CS2113") instanceof AddTodoCommand);
    }

    @Test
    void parse_missingFields_throws() {
        Parser parser = new Parser();
        assertThrows(ModuleSyncException.class, () -> parser.parse("add /mod CS2113"));
        assertThrows(ModuleSyncException.class, () -> parser.parse("add /task OnlyTask"));
        assertThrows(ModuleSyncException.class, () -> parser.parse("add"));
    }

    @Test
    void parse_markAndUnmark_returnsCorrectCommands() throws ModuleSyncException {
        Parser parser = new Parser();
        assertTrue(parser.parse("mark 1") instanceof MarkCommand);
        assertTrue(parser.parse("unmark 2") instanceof UnmarkCommand);
    }

    @Test
    void parse_markAndUnmarkInvalidInput_throws() {
        Parser parser = new Parser();
        assertThrows(ModuleSyncException.class, () -> parser.parse("mark"));
        assertThrows(ModuleSyncException.class, () -> parser.parse("unmark"));
        assertThrows(ModuleSyncException.class, () -> parser.parse("mark abc"));
    }
}

package seedu.modulesync.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TodoTest {

    @Test
    void getTypeCode_returnsT() {
        Todo todo = new Todo("CS2113", "Week8");
        assertEquals('T', todo.getTypeCode());
    }

    @Test
    void constructor_setsDoneFlag() {
        Todo todo = new Todo("CS2113", "Week8", true);
        assertTrue(todo.isDone());
        todo.markUndone();
        assertFalse(todo.isDone());
    }
}

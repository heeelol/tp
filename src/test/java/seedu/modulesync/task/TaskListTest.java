package seedu.modulesync.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.modulesync.exception.ModuleSyncException;

class TaskListTest {

    @Test
    void addTodo_validDescription_addsTask() throws ModuleSyncException {
        TaskList list = new TaskList();
        Task task = list.addTodo("CS2113", " Week8 ");

        assertEquals(1, list.size());
        assertEquals("CS2113", task.getModuleCode());
        assertEquals("Week8", task.getDescription());
    }

    @Test
    void addTodo_emptyDescription_throws() {
        TaskList list = new TaskList();
        assertThrows(ModuleSyncException.class, () -> list.addTodo("CS2113", " "));
    }
}

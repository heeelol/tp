package seedu.modulesync.module;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.task.Task;

class ModuleBookTest {

    private ModuleBook moduleBook;

    @BeforeEach
    void setUp() {
        moduleBook = new ModuleBook();
    }

    // -----------------------------------------------------------------
    // Implicit module creation via add
    // -----------------------------------------------------------------

    @Test
    void addTask_newModule_createsModuleAndAddsTask() throws ModuleSyncException {
        Module module = moduleBook.getOrCreate("CS2113");
        Task task = module.addTodo("Week 10 Quiz");

        assertEquals(1, moduleBook.getModules().size(),
                "ModuleBook should contain exactly one module");
        assertNotNull(moduleBook.getModule("CS2113"),
                "CS2113 module should exist after adding a task");
        assertEquals(1, moduleBook.countTotalTasks(),
                "Total task count should be 1");
        assertEquals("Week 10 Quiz", task.getDescription());
    }

    @Test
    void addTask_existingModule_doesNotDuplicate() throws ModuleSyncException {
        moduleBook.getOrCreate("CS2113").addTodo("Task A");
        moduleBook.getOrCreate("CS2113").addTodo("Task B");

        assertEquals(1, moduleBook.getModules().size(),
                "Should still be one module");
        assertEquals(2, moduleBook.countTotalTasks(),
                "Both tasks should exist under the same module");
    }

    @Test
    void getOrCreate_caseInsensitive_returnsSameModule() {
        Module lower = moduleBook.getOrCreate("cs2113");
        Module upper = moduleBook.getOrCreate("CS2113");

        assertEquals(lower, upper,
                "getOrCreate should be case-insensitive");
        assertEquals(1, moduleBook.getModules().size());
    }

    @Test
    void getModule_nonexistent_returnsNull() {
        assertNull(moduleBook.getModule("FAKE1234"));
    }

    // -----------------------------------------------------------------
    // Task deletion
    // -----------------------------------------------------------------

    @Test
    void removeTask_validIndex_decreasesCount() throws ModuleSyncException {
        moduleBook.getOrCreate("CS2113").addTodo("Task 1");
        moduleBook.getOrCreate("CS2113").addTodo("Task 2");
        moduleBook.getOrCreate("MA1521").addTodo("Task 3");

        assertEquals(3, moduleBook.countTotalTasks());

        Task removed = moduleBook.removeTaskByDisplayIndex(2);
        assertEquals("Task 2", removed.getDescription());
        assertEquals(2, moduleBook.countTotalTasks(),
                "Count should decrease by 1 after deletion");
    }

    @Test
    void removeTask_invalidIndex_throwsException() {
        assertThrows(ModuleSyncException.class,
                () -> moduleBook.removeTaskByDisplayIndex(1),
                "Should throw when no tasks exist");
    }

    @Test
    void removeTask_negativeIndex_throwsException() {
        assertThrows(ModuleSyncException.class,
                () -> moduleBook.removeTaskByDisplayIndex(-1));
    }

    @Test
    void removeTask_zeroIndex_throwsException() {
        assertThrows(ModuleSyncException.class,
                () -> moduleBook.removeTaskByDisplayIndex(0));
    }

    @Test
    void removeTask_indexBeyondTotal_throwsException() throws ModuleSyncException {
        moduleBook.getOrCreate("CS2113").addTodo("Only task");

        assertThrows(ModuleSyncException.class,
                () -> moduleBook.removeTaskByDisplayIndex(5));
    }

    @Test
    void removeTask_firstOfMany_remainingShiftDown() throws ModuleSyncException {
        moduleBook.getOrCreate("CS2113").addTodo("A");
        moduleBook.getOrCreate("CS2113").addTodo("B");
        moduleBook.getOrCreate("CS2113").addTodo("C");

        moduleBook.removeTaskByDisplayIndex(1);

        Task newFirst = moduleBook.getTaskByDisplayIndex(1);
        assertEquals("B", newFirst.getDescription(),
                "After removing 'A', 'B' should become index 1");

        Task newSecond = moduleBook.getTaskByDisplayIndex(2);
        assertEquals("C", newSecond.getDescription(),
                "After removing 'A', 'C' should become index 2");
    }

    // -----------------------------------------------------------------
    // Grade & credits on Module
    // -----------------------------------------------------------------

    @Test
    void moduleGradeAndCredits_setAndGet() {
        Module mod = moduleBook.getOrCreate("CS2113");
        assertEquals(false, mod.hasGrade());
        assertEquals(0, mod.getCredits());

        mod.setGrade("A+");
        mod.setCredits(4);

        assertEquals(true, mod.hasGrade());
        assertEquals("A+", mod.getGrade());
        assertEquals(4, mod.getCredits());
    }
}

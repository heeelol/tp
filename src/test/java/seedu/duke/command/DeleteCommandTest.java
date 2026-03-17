package seedu.duke.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import seedu.duke.exception.ModuleSyncException;
import seedu.duke.module.ModuleBook;
import seedu.duke.storage.Storage;
import seedu.duke.task.Task;
import seedu.duke.ui.Ui;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DeleteCommandTest {
    private ModuleBook moduleBook;
    private Storage stubStorage;
    private Ui stubUi;
    
    @BeforeEach
    void setUp() throws ModuleSyncException {
        moduleBook = new ModuleBook();
        moduleBook.getOrCreate("CS2113").addTodo("Test Task 1");
        moduleBook.getOrCreate("CS2113").addTodo("Test Task 2");
        
        stubStorage = new Storage(Path.of("dummy")) {
            @Override
            public void save(ModuleBook book) {
                // do nothing for testing
            }
        };
        
        stubUi = new Ui(new java.util.Scanner(System.in)) {
            @Override
            public void showTaskDeleted(Task task, int count) {
                // do nothing
            }
        };
    }

    @Test
    void execute_validIndex_deletesTask() throws ModuleSyncException {
        assertEquals(2, moduleBook.totalTaskCount());
        DeleteCommand command = new DeleteCommand(1);
        command.execute(moduleBook, stubStorage, stubUi);
        assertEquals(1, moduleBook.totalTaskCount());
        
        Task taskRemaining = moduleBook.getTaskByDisplayIndex(1);
        assertEquals("Test Task 2", taskRemaining.getDescription());
    }

    @Test
    void execute_invalidIndex_throwsException() {
        DeleteCommand command = new DeleteCommand(5);
        assertThrows(ModuleSyncException.class, () -> command.execute(moduleBook, stubStorage, stubUi));
    }
}

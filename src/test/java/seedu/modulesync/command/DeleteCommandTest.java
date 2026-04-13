package seedu.modulesync.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.task.Task;
import seedu.modulesync.ui.Ui;

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
        assertEquals(2, moduleBook.countTotalTasks());
        DeleteCommand command = new DeleteCommand(1);
        command.execute(moduleBook, stubStorage, stubUi);
        assertEquals(1, moduleBook.countTotalTasks());

        Task taskRemaining = moduleBook.getTaskByDisplayIndex(1);
        assertEquals("Test Task 2", taskRemaining.getDescription());
    }

    @Test
    void execute_invalidIndex_throwsException() {
        DeleteCommand command = new DeleteCommand(5);
        assertThrows(ModuleSyncException.class, () -> command.execute(moduleBook, stubStorage, stubUi));
    }

    @Test
    void execute_deletingLastTaskInMetadataFreeModule_autoRemovesModule() throws ModuleSyncException {
        ModuleBook localBook = new ModuleBook();
        localBook.getOrCreate("CS1231").addTodo("Only Task");

        DeleteCommand command = new DeleteCommand(1);
        command.execute(localBook, stubStorage, stubUi);

        assertEquals(0, localBook.countTotalTasks());
        assertEquals(0, localBook.getModules().size());
    }

    @Test
    void execute_deletingLastTaskInGradedModule_keepsModule() throws ModuleSyncException {
        ModuleBook localBook = new ModuleBook();
        localBook.getOrCreate("CS2100").addTodo("Only Task");
        localBook.getOrCreate("CS2100").setGrade("A");

        DeleteCommand command = new DeleteCommand(1);
        command.execute(localBook, stubStorage, stubUi);

        assertEquals(0, localBook.countTotalTasks());
        assertEquals(1, localBook.getModules().size());
        assertEquals("A", localBook.getModule("CS2100").getGrade());
    }
}

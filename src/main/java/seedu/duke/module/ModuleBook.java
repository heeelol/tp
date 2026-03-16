package seedu.duke.module;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import seedu.duke.exception.ModuleSyncException;
import seedu.duke.task.Task;

public class ModuleBook {
    private final Map<String, Module> modules = new LinkedHashMap<>();

    public Module getOrCreate(String code) {
        return modules.computeIfAbsent(code.toUpperCase(), Module::new);
    }

    public Collection<Module> getModules() {
        return modules.values();
    }

    public int totalTaskCount() {
        return modules.values().stream()
                .mapToInt(module -> module.getTasks().size())
                .sum();
    }

    public Task getTaskByDisplayIndex(int displayIndex) throws ModuleSyncException {
        if (displayIndex <= 0) {
            throw new ModuleSyncException("Task number must be a positive integer.");
        }

        int currentIndex = 1;
        for (Module module : modules.values()) {
            for (Task task : module.getTasks().asUnmodifiableList()) {
                if (currentIndex == displayIndex) {
                    return task;
                }
                currentIndex++;
            }
        }

        throw new ModuleSyncException("Task number does not exist: " + displayIndex);
    }
}

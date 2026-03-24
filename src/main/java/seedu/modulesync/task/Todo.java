package seedu.modulesync.task;

public class Todo extends Task {
    public Todo(String moduleCode, String description) {
        super(moduleCode, description, false);
    }

    public Todo(String moduleCode, String description, boolean isDone) {
        super(moduleCode, description, isDone);
    }

    @Override
    protected char getTypeCode() {
        return 'T';
    }
}

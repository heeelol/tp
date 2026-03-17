package seedu.duke.task;

public abstract class Task {
    private final String moduleCode;
    private final String description;
    private boolean isDone;

    protected Task(String moduleCode, String description, boolean isDone) {
        this.moduleCode = moduleCode;
        this.description = description;
        this.isDone = isDone;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDone() {
        return isDone;
    }

    public void markDone() {
        isDone = true;
    }

    public void markUndone() {
        isDone = false;
    }

    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    public String encode() {
        String extra = encodeExtra();
        if (extra.isEmpty()) {
            return String.join(" | ", moduleCode, String.valueOf(getTypeCode()), isDone ? "1" : "0", description);
        }
        return String.join(" | ", moduleCode, String.valueOf(getTypeCode()), isDone ? "1" : "0", description, extra);
    }

    protected String encodeExtra() {
        return "";
    }

    protected abstract char getTypeCode();

    public String formatForList(int index) {
        return index + ".[" + getModuleCode() + "] "
                + "[" + getTypeCode() + "][" + getStatusIcon() + "] "
                + description;
    }
}


package seedu.modulesync.task;

public abstract class Task {
    private final String moduleCode;
    private final String description;
    private boolean isDone;
    /** Optional weightage (0–100). Null means no weightage has been assigned. */
    private Integer weightage;

    protected Task(String moduleCode, String description, boolean isDone) {
        assert moduleCode != null && !moduleCode.trim().isEmpty() : "Module code must not be null or empty";
        assert description != null && !description.trim().isEmpty() : "Task description must not be null or empty";
        this.moduleCode = moduleCode;
        this.description = description;
        this.isDone = isDone;
        this.weightage = null;
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

    /**
     * Returns whether this task has a weightage assigned.
     *
     * @return true if a weightage value is present, false otherwise
     */
    public boolean hasWeightage() {
        return weightage != null;
    }

    /**
     * Returns the weightage of this task, or null if none is assigned.
     *
     * @return the weightage integer, or null
     */
    public Integer getWeightage() {
        return weightage;
    }

    /**
     * Sets the weightage of this task.
     *
     * @param weightage a value between 0 and 100 inclusive
     */
    public void setWeightage(int weightage) {
        assert weightage >= 0 && weightage <= 100 : "Weightage must be between 0 and 100";
        this.weightage = weightage;
    }

    public String encode() {
        java.util.List<String> fields = new java.util.ArrayList<>();
        fields.add(moduleCode);
        fields.add(String.valueOf(getTypeCode()));
        fields.add(isDone ? "1" : "0");
        fields.add(description);

        String extra = encodeExtra();
        if (!extra.isEmpty()) {
            fields.add(extra);
        }

        if (hasWeightage()) {
            fields.add(String.valueOf(weightage));
        }

        return String.join(" | ", fields);
    }

    protected String encodeExtra() {
        return "";
    }

    protected abstract char getTypeCode();

    public String formatForList(int index) {
        String base = index + ".[" + getModuleCode() + "] "
                + "[" + getTypeCode() + "][" + getStatusIcon() + "] "
                + description;
        if (hasWeightage()) {
            return base + " [" + weightage + "%]";
        }
        return base;
    }
}

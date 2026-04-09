package seedu.modulesync.task;

import java.time.LocalDateTime;

public abstract class Task {
    private static final int DEFAULT_PRIORITY_SCORE = 0;
    private static final String PRIORITY_PREFIX = " [Priority: ";
    private static final String PRIORITY_SUFFIX = "]";
    private static final String COMPLETED_PREFIX = "completed:";

    private final String moduleCode;
    private final String description;
    private boolean isDone;
    /** Optional weightage (0–100). Null means no weightage has been assigned. */
    private Integer weightage;
    /** Timestamp when the task was marked done. Null if not yet completed. */
    private LocalDateTime completedAt;

    protected Task(String moduleCode, String description, boolean isDone) {
        assert moduleCode != null && !moduleCode.trim().isEmpty() : "Module code must not be null or empty";
        assert description != null && !description.trim().isEmpty() : "Task description must not be null or empty";
        this.moduleCode = moduleCode;
        this.description = description;
        this.isDone = isDone;
        this.weightage = null;
        this.completedAt = null;
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

    /**
     * Marks this task as done and records the current timestamp as the completion time.
     */
    public void markDone() {
        isDone = true;
        completedAt = LocalDateTime.now();
    }

    /**
     * Marks this task as not done and clears the recorded completion timestamp.
     */
    public void markUndone() {
        isDone = false;
        completedAt = null;
    }

    /**
     * Returns the timestamp when this task was marked as done, or null if not yet completed.
     *
     * @return the completion timestamp, or null
     */
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    /**
     * Sets the completion timestamp directly (used when loading from storage).
     *
     * @param completedAt the completion timestamp, or null
     */
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
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

        if (completedAt != null) {
            fields.add(COMPLETED_PREFIX
                    + completedAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
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

    /**
     * Calculates the priority score for this task.
     *
     * @return the calculated priority score
     */
    public int calculatePriorityScore() {
        if (!hasWeightage()) {
            return DEFAULT_PRIORITY_SCORE;
        }
        return getWeightage();
    }

    /**
     * Formats this task for list output together with its priority score.
     *
     * @param index the global display index of the task
     * @return the formatted task line with its priority score
     */
    public String formatForListWithPriority(int index) {
        String formattedTask = formatForList(index);
        int priorityScore = calculatePriorityScore();
        return formattedTask + PRIORITY_PREFIX + priorityScore + PRIORITY_SUFFIX;
    }
}

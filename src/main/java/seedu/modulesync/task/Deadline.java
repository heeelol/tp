package seedu.modulesync.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Deadline extends Task {
    private static final int HOURS_PER_DAY = 24;
    private static final int PRIORITY_WINDOW_DAYS = 30;
    private static final int PRIORITY_WINDOW_HOURS = PRIORITY_WINDOW_DAYS * HOURS_PER_DAY;
    private static final int MINIMUM_DEADLINE_URGENCY_SCORE = 1;
    private static final int MAX_DEADLINE_URGENCY_SCORE = PRIORITY_WINDOW_DAYS
            + MINIMUM_DEADLINE_URGENCY_SCORE;

    private final LocalDateTime by;

    public Deadline(String moduleCode, String description, LocalDateTime by) {
        super(moduleCode, description, false);
        assert description != null && !description.trim().isEmpty() : "Description cannot be null or empty";
        assert by != null : "Deadline date cannot be null";
        this.by = by;
    }

    public Deadline(String moduleCode, String description, boolean isDone, LocalDateTime by) {
        super(moduleCode, description, isDone);
        assert description != null && !description.trim().isEmpty() : "Description cannot be null or empty";
        assert by != null : "Deadline date cannot be null";
        this.by = by;
    }

    public LocalDateTime getBy() {
        return by;
    }

    public long getDaysLeft() {
        return ChronoUnit.DAYS.between(LocalDateTime.now(), by);
    }

    @Override
    protected char getTypeCode() {
        return 'D';
    }

    @Override
    protected String encodeExtra() {
        return by.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    @Override
    public String formatForList(int index) {
        String base = super.formatForList(index);
        String formattedDate = by.format(DateTimeFormatter.ofPattern("MMM dd yyyy, HH:mm"));
        return base + " (by: " + formattedDate + ", " + getDaysLeft() + " days left)";
    }

    /**
     * Calculates the priority score for this deadline using both weightage and due-date urgency.
     *
     * @return the calculated priority score
     */
    @Override
    public int calculatePriorityScore() {
        int weightPriorityScore = super.calculatePriorityScore();
        int deadlineUrgencyScore = calculateDeadlineUrgencyScore();
        return weightPriorityScore + deadlineUrgencyScore;
    }

    /**
     * Calculates the urgency contribution from the deadline timing.
     *
     * @return the urgency score derived from the deadline
     */
    private int calculateDeadlineUrgencyScore() {
        long hoursUntilDeadline = ChronoUnit.HOURS.between(LocalDateTime.now(), by);
        if (hoursUntilDeadline <= 0) {
            return MAX_DEADLINE_URGENCY_SCORE;
        }

        long boundedHoursUntilDeadline = Math.min(hoursUntilDeadline, PRIORITY_WINDOW_HOURS);
        long elapsedPriorityHours = PRIORITY_WINDOW_HOURS - boundedHoursUntilDeadline;
        long elapsedPriorityDays = elapsedPriorityHours / HOURS_PER_DAY;
        return (int) elapsedPriorityDays + MINIMUM_DEADLINE_URGENCY_SCORE;
    }
}


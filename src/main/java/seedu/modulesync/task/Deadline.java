package seedu.modulesync.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Deadline extends Task {
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
}


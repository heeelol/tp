package seedu.modulesync.grade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the student's grade history across multiple semesters.
 */
public class GradeHistorySummary {

    private final List<SemesterGradeSummary> semesterGradeSummaries;

    /**
     * Constructs a grade history summary.
     *
     * @param semesterGradeSummaries the semester summaries to expose in chronological order
     */
    public GradeHistorySummary(List<SemesterGradeSummary> semesterGradeSummaries) {
        assert semesterGradeSummaries != null : "Semester grade summaries must not be null";
        this.semesterGradeSummaries = new ArrayList<>(semesterGradeSummaries);
    }

    /**
     * Returns the semester summaries in chronological order.
     *
     * @return the semester summaries in chronological order
     */
    public List<SemesterGradeSummary> getSemesterGradeSummaries() {
        return Collections.unmodifiableList(semesterGradeSummaries);
    }

    /**
     * Returns whether the student has any semester summaries to display.
     *
     * @return {@code true} if the student has any semester summaries to display
     */
    public boolean hasSemesterGradeSummaries() {
        return !semesterGradeSummaries.isEmpty();
    }
}

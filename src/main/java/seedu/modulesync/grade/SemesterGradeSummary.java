package seedu.modulesync.grade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents one semester's grades together with cumulative progress up to that semester.
 */
public class SemesterGradeSummary {

    private final String semesterName;
    private final boolean currentSemester;
    private final boolean archivedSemester;
    private final List<ModuleGradeEntry> moduleGradeEntries;
    private final double semesterGradePoints;
    private final int semesterCredits;
    private final double cumulativeGradePoints;
    private final int cumulativeCredits;
    private final int cumulativeRecordedSemesterCount;

    /**
     * Constructs a semester grade summary.
     *
     * @param semesterName the semester name
     * @param currentSemester whether the semester is the current active semester
     * @param archivedSemester whether the semester is archived
     * @param moduleGradeEntries the graded modules recorded in the semester
     * @param semesterGradePoints the semester CAP grade points
     * @param semesterCredits the semester CAP credits
     * @param cumulativeGradePoints the cumulative CAP grade points up to this semester
     * @param cumulativeCredits the cumulative CAP credits up to this semester
     * @param cumulativeRecordedSemesterCount the number of semesters with recorded grades up to this semester
     */
    public SemesterGradeSummary(String semesterName, boolean currentSemester, boolean archivedSemester,
                                List<ModuleGradeEntry> moduleGradeEntries,
                                double semesterGradePoints, int semesterCredits,
                                double cumulativeGradePoints, int cumulativeCredits,
                                int cumulativeRecordedSemesterCount) {
        assert semesterName != null && !semesterName.isBlank() : "Semester name must not be blank";
        assert moduleGradeEntries != null : "Module grade entries must not be null";
        this.semesterName = semesterName;
        this.currentSemester = currentSemester;
        this.archivedSemester = archivedSemester;
        this.moduleGradeEntries = new ArrayList<>(moduleGradeEntries);
        this.semesterGradePoints = semesterGradePoints;
        this.semesterCredits = semesterCredits;
        this.cumulativeGradePoints = cumulativeGradePoints;
        this.cumulativeCredits = cumulativeCredits;
        this.cumulativeRecordedSemesterCount = cumulativeRecordedSemesterCount;
    }

    /**
     * Returns the selected semester name.
     *
     * @return the selected semester name
     */
    public String getSemesterName() {
        return semesterName;
    }

    /**
     * Returns whether the selected semester is the active semester.
     *
     * @return {@code true} if the selected semester is active
     */
    public boolean isCurrentSemester() {
        return currentSemester;
    }

    /**
     * Returns whether the selected semester is archived.
     *
     * @return {@code true} if the selected semester is archived
     */
    public boolean isArchivedSemester() {
        return archivedSemester;
    }

    /**
     * Returns the graded modules recorded in the selected semester.
     *
     * @return the graded modules recorded in the selected semester
     */
    public List<ModuleGradeEntry> getModuleGradeEntries() {
        return Collections.unmodifiableList(moduleGradeEntries);
    }

    /**
     * Returns whether the selected semester has any recorded grades.
     *
     * @return {@code true} if the selected semester has recorded grades
     */
    public boolean hasRecordedGradesInSemester() {
        return !moduleGradeEntries.isEmpty();
    }

    /**
     * Returns whether the semester has CAP-bearing modules.
     *
     * @return {@code true} if the semester CAP can be calculated
     */
    public boolean hasSemesterCap() {
        return semesterCredits > 0;
    }

    /**
     * Returns the semester CAP.
     *
     * @return the semester CAP
     */
    public double getSemesterCap() {
        assert hasSemesterCap() : "Semester CAP is only available when semester credits are positive";
        return semesterGradePoints / semesterCredits;
    }

    /**
     * Returns the semester CAP credits.
     *
     * @return the semester CAP credits
     */
    public int getSemesterCredits() {
        return semesterCredits;
    }

    /**
     * Returns whether the cumulative CAP can be calculated.
     *
     * @return {@code true} if cumulative CAP credits exist
     */
    public boolean hasCumulativeCap() {
        return cumulativeCredits > 0;
    }

    /**
     * Returns the cumulative CAP up to this semester.
     *
     * @return the cumulative CAP up to this semester
     */
    public double getCumulativeCap() {
        assert hasCumulativeCap() : "Cumulative CAP is only available when cumulative credits are positive";
        return cumulativeGradePoints / cumulativeCredits;
    }

    /**
     * Returns the cumulative CAP credits up to this semester.
     *
     * @return the cumulative CAP credits up to this semester
     */
    public int getCumulativeCredits() {
        return cumulativeCredits;
    }

    /**
     * Returns the number of semesters with recorded grades up to this semester.
     *
     * @return the number of semesters with recorded grades up to this semester
     */
    public int getCumulativeRecordedSemesterCount() {
        return cumulativeRecordedSemesterCount;
    }

    /**
     * Represents one recorded module grade in the selected semester.
     */
    public static class ModuleGradeEntry {

        private final String moduleCode;
        private final int credits;
        private final String grade;
        private final Double gradePoint;

        /**
         * Constructs one module-grade entry.
         *
         * @param moduleCode the module code
         * @param credits the module credits
         * @param grade the recorded grade
         * @param gradePoint the CAP grade-point value, or {@code null} if the grade does not affect CAP
         */
        public ModuleGradeEntry(String moduleCode, int credits, String grade, Double gradePoint) {
            assert moduleCode != null && !moduleCode.isBlank() : "Module code must not be blank";
            assert credits >= 0 : "Credits must not be negative";
            assert grade != null && !grade.isBlank() : "Grade must not be blank";
            this.moduleCode = moduleCode;
            this.credits = credits;
            this.grade = grade;
            this.gradePoint = gradePoint;
        }

        /**
         * Returns the module code.
         *
         * @return the module code
         */
        public String getModuleCode() {
            return moduleCode;
        }

        /**
         * Returns the module credits.
         *
         * @return the module credits
         */
        public int getCredits() {
            return credits;
        }

        /**
         * Returns the recorded grade.
         *
         * @return the recorded grade
         */
        public String getGrade() {
            return grade;
        }

        /**
         * Returns whether this module grade has a CAP grade-point value.
         *
         * @return {@code true} if this module grade has a CAP grade-point value
         */
        public boolean hasGradePoint() {
            return gradePoint != null;
        }

        /**
         * Returns the CAP grade-point value for this module grade.
         *
         * @return the CAP grade-point value
         */
        public double getGradePoint() {
            assert hasGradePoint() : "Grade point is only available when this grade affects CAP";
            return gradePoint;
        }
    }
}

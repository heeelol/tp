package seedu.modulesync.grade;

import java.util.ArrayList;
import java.util.List;

import seedu.modulesync.module.Module;
import seedu.modulesync.semester.Semester;
import seedu.modulesync.semester.SemesterBook;

/**
 * Calculates the student's cross-semester grade history and cumulative CAP progress.
 */
public class GradeSummaryCalculator {

    private final GradePointScale gradePointScale;

    /**
     * Constructs a calculator backed by the default grade-point scale.
     */
    public GradeSummaryCalculator() {
        this.gradePointScale = new GradePointScale();
    }

    /**
     * Calculates the student's grade history across all semesters with recorded grades.
     *
     * @param semesterBook the semester book to inspect
     * @return the calculated grade history summary
     */
    public GradeHistorySummary calculateSummary(SemesterBook semesterBook) {
        assert semesterBook != null : "SemesterBook must not be null when calculating grade summary";

        List<SemesterGradeSummary> semesterGradeSummaries = new ArrayList<>();
        CumulativeSnapshot cumulativeSnapshot = new CumulativeSnapshot();
        String currentSemesterName = semesterBook.getCurrentSemesterName();

        for (Semester semester : semesterBook.getAllSemesters()) {
            List<SemesterGradeSummary.ModuleGradeEntry> moduleGradeEntries = collectModuleGradeEntries(semester);
            if (moduleGradeEntries.isEmpty()) {
                continue;
            }

            double semesterGradePoints = calculateGradePoints(moduleGradeEntries);
            int semesterCredits = calculateCredits(moduleGradeEntries);
            cumulativeSnapshot.gradePoints += semesterGradePoints;
            cumulativeSnapshot.credits += semesterCredits;
            cumulativeSnapshot.recordedSemesterCount++;

            SemesterGradeSummary semesterGradeSummary = new SemesterGradeSummary(
                    semester.getName(),
                    semester.getName().equals(currentSemesterName),
                    semester.isArchived(),
                    moduleGradeEntries,
                    semesterGradePoints,
                    semesterCredits,
                    cumulativeSnapshot.gradePoints,
                    cumulativeSnapshot.credits,
                    cumulativeSnapshot.recordedSemesterCount);
            semesterGradeSummaries.add(semesterGradeSummary);
        }

        return new GradeHistorySummary(semesterGradeSummaries);
    }

    /**
     * Collects the recorded module grades from the given semester.
     *
     * @param semester the semester to inspect
     * @return the recorded module grades from the given semester
     */
    private List<SemesterGradeSummary.ModuleGradeEntry> collectModuleGradeEntries(Semester semester) {
        List<SemesterGradeSummary.ModuleGradeEntry> moduleGradeEntries = new ArrayList<>();
        for (Module module : semester.getModuleBook().getModules()) {
            if (!module.hasGrade()) {
                continue;
            }

            Double gradePoint = gradePointScale.findGradePoint(module.getGrade());
            SemesterGradeSummary.ModuleGradeEntry moduleGradeEntry =
                    new SemesterGradeSummary.ModuleGradeEntry(
                            module.getCode(),
                            module.getCredits(),
                            module.getGrade(),
                            gradePoint);
            moduleGradeEntries.add(moduleGradeEntry);
        }
        return moduleGradeEntries;
    }

    /**
     * Calculates the CAP grade points for the given module-grade entries.
     *
     * @param moduleGradeEntries the module-grade entries to inspect
     * @return the CAP grade points for the given module-grade entries
     */
    private double calculateGradePoints(List<SemesterGradeSummary.ModuleGradeEntry> moduleGradeEntries) {
        double totalGradePoints = 0;
        for (SemesterGradeSummary.ModuleGradeEntry moduleGradeEntry : moduleGradeEntries) {
            if (!moduleGradeEntry.hasGradePoint()) {
                continue;
            }
            totalGradePoints += moduleGradeEntry.getGradePoint() * moduleGradeEntry.getCredits();
        }
        return totalGradePoints;
    }

    /**
     * Calculates the CAP credits for the given module-grade entries.
     *
     * @param moduleGradeEntries the module-grade entries to inspect
     * @return the CAP credits for the given module-grade entries
     */
    private int calculateCredits(List<SemesterGradeSummary.ModuleGradeEntry> moduleGradeEntries) {
        int totalCredits = 0;
        for (SemesterGradeSummary.ModuleGradeEntry moduleGradeEntry : moduleGradeEntries) {
            if (!moduleGradeEntry.hasGradePoint()) {
                continue;
            }
            totalCredits += moduleGradeEntry.getCredits();
        }
        return totalCredits;
    }

    /**
     * Holds cumulative grade progress while iterating across semesters.
     */
    private static class CumulativeSnapshot {
        private double gradePoints;
        private int credits;
        private int recordedSemesterCount;
    }
}

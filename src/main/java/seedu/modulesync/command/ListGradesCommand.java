package seedu.modulesync.command;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.grade.GradeHistorySummary;
import seedu.modulesync.grade.GradeSummaryCalculator;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.storage.SemesterStorage;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

/**
 * Command to display the student's grades and cumulative academic progress across semesters.
 */
public class ListGradesCommand extends SemesterCommand {

    private final GradeSummaryCalculator gradeSummaryCalculator;

    public ListGradesCommand(seedu.modulesync.semester.SemesterBook semesterBook,
                             SemesterStorage semesterStorage) {
        super(semesterBook, semesterStorage);
        this.gradeSummaryCalculator = new GradeSummaryCalculator();
    }

    @Override
    public void execute(ModuleBook moduleBook, Storage storage, Ui ui) throws ModuleSyncException {
        GradeHistorySummary gradeHistorySummary = gradeSummaryCalculator.calculateSummary(semesterBook);
        ui.showGradeSummary(gradeHistorySummary);
    }
}

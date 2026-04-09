package seedu.modulesync.command;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.grade.GradePointScale;
import seedu.modulesync.module.Module;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.semester.Semester;
import seedu.modulesync.semester.SemesterBook;
import seedu.modulesync.storage.SemesterStorage;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

/**
 * Command to compute and display the user's Semester and Cumulative CAP.
 */
public class CapCommand extends SemesterCommand {

    private final GradePointScale gradePointScale;

    public CapCommand(SemesterBook semesterBook, SemesterStorage semesterStorage) {
        super(semesterBook, semesterStorage);
        this.gradePointScale = new GradePointScale();
    }

    @Override
    public void execute(ModuleBook activeModuleBook, Storage activeStorage, Ui ui) throws ModuleSyncException {
        double semGradePoints = 0;
        int semCredits = 0;
        double cumGradePoints = 0;
        int cumCredits = 0;

        String currentSemName = semesterBook.getCurrentSemesterName();

        for (Semester semester : semesterBook.getAllSemesters()) {
            boolean isCurrent = semester.getName().equals(currentSemName);
            for (Module module : semester.getModuleBook().getModules()) {
                if (!module.hasGrade()) {
                    continue;
                }

                Double gradePoint = gradePointScale.findGradePoint(module.getGrade());
                if (gradePoint == null) {
                    continue;
                }

                int credits = module.getCredits();
                cumGradePoints += gradePoint * credits;
                cumCredits += credits;
                if (isCurrent) {
                    semGradePoints += gradePoint * credits;
                    semCredits += credits;
                }
            }
        }

        ui.showCap(semGradePoints, semCredits, cumGradePoints, cumCredits);
    }
}

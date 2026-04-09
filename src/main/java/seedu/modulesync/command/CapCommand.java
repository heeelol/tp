package seedu.modulesync.command;

import seedu.modulesync.exception.ModuleSyncException;
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

    public CapCommand(SemesterBook semesterBook, SemesterStorage semesterStorage) {
        super(semesterBook, semesterStorage);
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
                if (module.hasGrade() && !isCsCu(module.getGrade())) {
                    double point = getGradePoint(module.getGrade());
                    if (point >= 0) {
                        int credits = module.getCredits();
                        cumGradePoints += point * credits;
                        cumCredits += credits;
                        if (isCurrent) {
                            semGradePoints += point * credits;
                            semCredits += credits;
                        }
                    }
                }
            }
        }

        ui.showCap(semGradePoints, semCredits, cumGradePoints, cumCredits);
    }

    private boolean isCsCu(String grade) {
        String g = grade.toUpperCase();
        return g.equals("CS") || g.equals("CU") || g.equals("S") || g.equals("U");
    }

    private double getGradePoint(String grade) {
        switch (grade.toUpperCase()) {
        case "A+": case "A": return 5.0;
        case "A-": return 4.5;
        case "B+": return 4.0;
        case "B": return 3.5;
        case "B-": return 3.0;
        case "C+": return 2.5;
        case "C": return 2.0;
        case "D+": return 1.5;
        case "D": return 1.0;
        case "F": return 0.0;
        default: return -1.0;
        }
    }
}

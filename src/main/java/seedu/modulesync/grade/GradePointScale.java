package seedu.modulesync.grade;

/**
 * Maps recorded letter grades to their CAP grade-point values.
 */
public class GradePointScale {

    private static final String GRADE_A_PLUS = "A+";
    private static final String GRADE_A = "A";
    private static final String GRADE_A_MINUS = "A-";
    private static final String GRADE_B_PLUS = "B+";
    private static final String GRADE_B = "B";
    private static final String GRADE_B_MINUS = "B-";
    private static final String GRADE_C_PLUS = "C+";
    private static final String GRADE_C = "C";
    private static final String GRADE_D_PLUS = "D+";
    private static final String GRADE_D = "D";
    private static final String GRADE_F = "F";
    private static final String GRADE_CS = "CS";
    private static final String GRADE_CU = "CU";
    private static final String GRADE_S = "S";
    private static final String GRADE_U = "U";

    /**
     * Finds the CAP grade-point value for the given grade.
     *
     * @param grade the recorded letter grade
     * @return the CAP grade-point value, or {@code null} if the grade does not affect CAP
     */
    public Double findGradePoint(String grade) {
        assert grade != null && !grade.isBlank() : "Grade must not be blank when finding grade points";

        String normalizedGrade = grade.toUpperCase();
        if (normalizedGrade.equals(GRADE_A_PLUS)) {
            return 5.0;
        }
        if (normalizedGrade.equals(GRADE_A)) {
            return 5.0;
        }
        if (normalizedGrade.equals(GRADE_A_MINUS)) {
            return 4.5;
        }
        if (normalizedGrade.equals(GRADE_B_PLUS)) {
            return 4.0;
        }
        if (normalizedGrade.equals(GRADE_B)) {
            return 3.5;
        }
        if (normalizedGrade.equals(GRADE_B_MINUS)) {
            return 3.0;
        }
        if (normalizedGrade.equals(GRADE_C_PLUS)) {
            return 2.5;
        }
        if (normalizedGrade.equals(GRADE_C)) {
            return 2.0;
        }
        if (normalizedGrade.equals(GRADE_D_PLUS)) {
            return 1.5;
        }
        if (normalizedGrade.equals(GRADE_D)) {
            return 1.0;
        }
        if (normalizedGrade.equals(GRADE_F)) {
            return 0.0;
        }
        if (normalizedGrade.equals(GRADE_CS)) {
            return null;
        }
        if (normalizedGrade.equals(GRADE_CU)) {
            return null;
        }
        if (normalizedGrade.equals(GRADE_S)) {
            return null;
        }
        if (normalizedGrade.equals(GRADE_U)) {
            return null;
        }
        return null;
    }
}

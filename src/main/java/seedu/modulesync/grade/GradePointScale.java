package seedu.modulesync.grade;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps recorded letter grades to their CAP grade-point values.
 *
 * <p>Grades that do not affect CAP (CS, CU, S, U) return {@code null}.
 * Any unrecognised grade string also returns {@code null}.
 */
public class GradePointScale {

    /**
     * Lookup table of letter grade → CAP grade-point value.
     * Grades that do not affect CAP (CS, CU, S, U) are intentionally absent;
     * {@code getOrDefault} returns {@code null} for them automatically.
     */
    private static final Map<String, Double> GRADE_POINT_MAP = new LinkedHashMap<>();

    static {
        GRADE_POINT_MAP.put("A+", 5.0);
        GRADE_POINT_MAP.put("A",  5.0);
        GRADE_POINT_MAP.put("A-", 4.5);
        GRADE_POINT_MAP.put("B+", 4.0);
        GRADE_POINT_MAP.put("B",  3.5);
        GRADE_POINT_MAP.put("B-", 3.0);
        GRADE_POINT_MAP.put("C+", 2.5);
        GRADE_POINT_MAP.put("C",  2.0);
        GRADE_POINT_MAP.put("D+", 1.5);
        GRADE_POINT_MAP.put("D",  1.0);
        GRADE_POINT_MAP.put("F",  0.0);
    }

    /**
     * Returns the CAP grade-point value for the given letter grade.
     *
     * @param grade the recorded letter grade (case-insensitive)
     * @return the CAP grade-point value, or {@code null} if the grade does not affect CAP
     *         or is not recognised
     */
    public Double findGradePoint(String grade) {
        assert grade != null && !grade.isBlank() : "Grade must not be blank when finding grade points";
        return GRADE_POINT_MAP.getOrDefault(grade.toUpperCase(), null);
    }
}

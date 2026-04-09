package seedu.modulesync.semester;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.modulesync.exception.ModuleSyncException;

class SemesterBookTest {

    private SemesterBook semesterBook;

    @BeforeEach
    void setUp() {
        semesterBook = new SemesterBook();
    }

    // -------------------------------------------------------------------------
    // switchOrCreate
    // -------------------------------------------------------------------------

    @Test
    void switchOrCreate_newName_returnsTrueAndSemesterExists() throws ModuleSyncException {
        boolean created = semesterBook.switchOrCreate("AY2526-S2");

        assertTrue(created);
        assertNotNull(semesterBook.getCurrentSemester());
        assertEquals("AY2526-S2", semesterBook.getCurrentSemester().getName());
    }

    @Test
    void switchOrCreate_existingName_returnsFalse() throws ModuleSyncException {
        semesterBook.switchOrCreate("AY2526-S2");
        boolean created = semesterBook.switchOrCreate("AY2526-S2");

        assertFalse(created);
        assertEquals("AY2526-S2", semesterBook.getCurrentSemester().getName());
    }

    @Test
    void switchOrCreate_switchesBetweenTwoSemesters() throws ModuleSyncException {
        semesterBook.switchOrCreate("AY2526-S1");
        semesterBook.switchOrCreate("AY2526-S2");

        assertEquals("AY2526-S2", semesterBook.getCurrentSemester().getName());

        semesterBook.switchOrCreate("AY2526-S1");
        assertEquals("AY2526-S1", semesterBook.getCurrentSemester().getName());
    }

    @Test
    void switchOrCreate_blankName_throwsModuleSyncException() {
        assertThrows(ModuleSyncException.class, () -> semesterBook.switchOrCreate("   "));
    }

    // -------------------------------------------------------------------------
    // getCurrentSemester — no semester set
    // -------------------------------------------------------------------------

    @Test
    void getCurrentSemester_noSemesterSet_throwsModuleSyncException() {
        assertThrows(ModuleSyncException.class, () -> semesterBook.getCurrentSemester());
    }

    // -------------------------------------------------------------------------
    // isCurrentSemesterReadOnly
    // -------------------------------------------------------------------------

    @Test
    void isCurrentSemesterReadOnly_activeSemester_returnsFalse() throws ModuleSyncException {
        semesterBook.switchOrCreate("AY2526-S2");

        assertFalse(semesterBook.isCurrentSemesterReadOnly());
    }

    @Test
    void isCurrentSemesterReadOnly_archivedSemester_returnsTrue() throws ModuleSyncException {
        semesterBook.switchOrCreate("AY2526-S2");
        semesterBook.archiveCurrentSemester();

        assertTrue(semesterBook.isCurrentSemesterReadOnly());
    }

    // -------------------------------------------------------------------------
    // archiveCurrentSemester / unarchiveCurrentSemester
    // -------------------------------------------------------------------------

    @Test
    void archiveCurrentSemester_makesItReadOnly() throws ModuleSyncException {
        semesterBook.switchOrCreate("AY2526-S2");
        semesterBook.archiveCurrentSemester();

        assertTrue(semesterBook.getCurrentSemester().isArchived());
    }

    @Test
    void unarchiveCurrentSemester_makesItEditable() throws ModuleSyncException {
        semesterBook.switchOrCreate("AY2526-S2");
        semesterBook.archiveCurrentSemester();
        semesterBook.unarchiveCurrentSemester();

        assertFalse(semesterBook.getCurrentSemester().isArchived());
        assertFalse(semesterBook.isCurrentSemesterReadOnly());
    }

    // -------------------------------------------------------------------------
    // getAllSemesters / hasSemesters
    // -------------------------------------------------------------------------

    @Test
    void hasSemesters_emptySemesterBook_returnsFalse() {
        assertFalse(semesterBook.hasSemesters());
    }

    @Test
    void hasSemesters_afterAddingSemester_returnsTrue() throws ModuleSyncException {
        semesterBook.switchOrCreate("AY2526-S2");

        assertTrue(semesterBook.hasSemesters());
    }

    @Test
    void getAllSemesters_returnsAllRegisteredSemesters() throws ModuleSyncException {
        semesterBook.switchOrCreate("AY2526-S1");
        semesterBook.switchOrCreate("AY2526-S2");

        assertEquals(2, semesterBook.getAllSemesters().size());
    }
}

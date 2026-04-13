package seedu.modulesync.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.modulesync.exception.ModuleSyncException;
import seedu.modulesync.module.Module;
import seedu.modulesync.module.ModuleBook;
import seedu.modulesync.semester.Semester;
import seedu.modulesync.semester.SemesterBook;
import seedu.modulesync.storage.SemesterStorage;
import seedu.modulesync.storage.Storage;
import seedu.modulesync.ui.Ui;

class CapCommandTest {

    @TempDir
    Path tempDir;

    private SemesterBook semesterBook;
    private SemesterStorage stubSemesterStorage;
    private Storage stubStorage;
    private Ui stubUi;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        semesterBook = new SemesterBook();
        stubSemesterStorage = new SemesterStorage(tempDir);

        stubStorage = new Storage(tempDir.resolve("dummy.txt")) {
            @Override
            public void save(ModuleBook book) {
                // no-op
            }
        };

        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        stubUi = new Ui(
                new java.util.Scanner(
                        new java.io.ByteArrayInputStream(new byte[0])));
    }

    @Test
    void execute_twoGradedModules_calculatesCorrectCap() throws ModuleSyncException {
        Semester sem = new Semester("AY2526-S2", false);
        Module cs2113 = sem.getModuleBook().getOrCreate("CS2113");
        cs2113.setGrade("A+");
        cs2113.setCredits(4);
        Module ma1521 = sem.getModuleBook().getOrCreate("MA1521");
        ma1521.setGrade("B+");
        ma1521.setCredits(4);

        semesterBook.addSemester(sem);
        semesterBook.setCurrentSemester("AY2526-S2");

        CapCommand cmd = new CapCommand(semesterBook, stubSemesterStorage);
        cmd.execute(sem.getModuleBook(), stubStorage, stubUi);

        String output = outputStream.toString();
        // (5.0*4 + 4.0*4) / (4+4) = 36/8 = 4.50
        assertEquals(true, output.contains("4.50"),
                "Expected CAP of 4.50 but got: " + output);
    }

    @Test
    void execute_csCuGradeExcluded_doesNotAffectCap() throws ModuleSyncException {
        Semester sem = new Semester("AY2526-S2", false);
        Module cs2113 = sem.getModuleBook().getOrCreate("CS2113");
        cs2113.setGrade("A");
        cs2113.setCredits(4);
        Module csMod = sem.getModuleBook().getOrCreate("GEA1000");
        csMod.setGrade("CS");
        csMod.setCredits(4);

        semesterBook.addSemester(sem);
        semesterBook.setCurrentSemester("AY2526-S2");

        CapCommand cmd = new CapCommand(semesterBook, stubSemesterStorage);
        cmd.execute(sem.getModuleBook(), stubStorage, stubUi);

        String output = outputStream.toString();
        // Only CS2113 counts: 5.0*4 / 4 = 5.00
        assertEquals(true, output.contains("5.00"),
                "CS/CU modules should be excluded. Got: " + output);
        assertEquals(false, output.contains("cannot be calculated"),
                "Should not show 'cannot be calculated' message");
    }

    @Test
    void execute_zeroCreditsGradedModule_handlesGracefully() throws ModuleSyncException {
        Semester sem = new Semester("AY2526-S2", false);
        Module cs2113 = sem.getModuleBook().getOrCreate("CS2113");
        cs2113.setGrade("A");
        cs2113.setCredits(0);

        semesterBook.addSemester(sem);
        semesterBook.setCurrentSemester("AY2526-S2");

        CapCommand cmd = new CapCommand(semesterBook, stubSemesterStorage);
        cmd.execute(sem.getModuleBook(), stubStorage, stubUi);

        String output = outputStream.toString();
        // 0 credits module contributes 0 to both numerator and denominator
        // cumCredits stays 0, so the "cannot be calculated" path is triggered
        assertEquals(true, output.contains("cannot be calculated"),
                "0-credit module should not allow CAP calculation. Got: "
                        + output);
    }

    @Test
    void execute_noGradedModules_showsCannotCalculate() throws ModuleSyncException {
        Semester sem = new Semester("AY2526-S2", false);
        sem.getModuleBook().getOrCreate("CS2113");

        semesterBook.addSemester(sem);
        semesterBook.setCurrentSemester("AY2526-S2");

        CapCommand cmd = new CapCommand(semesterBook, stubSemesterStorage);
        cmd.execute(sem.getModuleBook(), stubStorage, stubUi);

        String output = outputStream.toString();
        assertEquals(true, output.contains("cannot be calculated"),
                "Expected 'cannot be calculated' message. Got: " + output);
    }

    @Test
    void execute_multiSemester_cumulativeIncludesAll() throws ModuleSyncException {
        Semester sem1 = new Semester("AY2526-S1", false);
        Module mod1 = sem1.getModuleBook().getOrCreate("CS1010");
        mod1.setGrade("A");
        mod1.setCredits(4);

        Semester sem2 = new Semester("AY2526-S2", false);
        Module mod2 = sem2.getModuleBook().getOrCreate("CS2113");
        mod2.setGrade("B");
        mod2.setCredits(4);

        semesterBook.addSemester(sem1);
        semesterBook.addSemester(sem2);
        semesterBook.setCurrentSemester("AY2526-S2");

        CapCommand cmd = new CapCommand(semesterBook, stubSemesterStorage);
        cmd.execute(sem2.getModuleBook(), stubStorage, stubUi);

        String output = outputStream.toString();
        // Semester CAP: B = 3.5 -> 3.5*4/4 = 3.50
        // Cumulative: (5.0*4 + 3.5*4) / 8 = 34/8 = 4.25
        assertEquals(true, output.contains("4.25"),
                "Cumulative should be 4.25. Got: " + output);
        assertEquals(true, output.contains("3.50"),
                "Semester CAP should be 3.50. Got: " + output);
    }
}

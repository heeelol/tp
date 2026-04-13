# Yap Jia Wei - Project Portfolio Page

## Project: ModuleSync

ModuleSync is a desktop CLI-based task manager designed to help university students achieve a balanced academic life through structured organisation of their module-related tasks. The user interacts with it using a Command Line Interface (CLI), and all data is stored locally in a human-editable text file. It is written in Java 17, and has about 4 kLoC.

Given below are my contributions to the project.

## Core Features Implemented

### Feature 1: Mark and Unmark Commands
**What it does:** Toggle task completion status. `mark INDEX` marks a task done; `unmark INDEX` reverts it. All task data (deadlines, weightage, module) is preserved.

**Why it matters:** Core feature—users need to track progress without losing task information. Unlike deletion, marking keeps metadata for later reference.

**Technical approach:** 
- Bidirectional state machine with automatic rollback if save fails
- State changes propagate through TaskList → ModuleBook → SemesterBook safely
- Prevents double-marking, handles invalid indices, recovers from corrupted files

### Feature 2: List Upcoming Deadlines
**What it does:** `list /deadlines` shows all tasks with due dates across all modules in current semester, sorted by date.

**Why it matters:** Helps students see all upcoming work at once instead of checking each module separately. Reduces confusion about what's due when.

**Technical approach:**
- Searches all modules efficiently in O(n) time
- Parses various date formats (Week X, DD/MM/YYYY, relative dates)
- Extensible design supports other filters (`/mod`, `/notdone`, `/top`) without duplication

### Feature 3: List Top N Tasks
**What it does:** `list /top X` shows the X most urgent tasks ranked by a priority score combining deadline and importance.

**Why it matters:** Helps overloaded students focus on the most critical work first instead of manually sorting priorities.

**Technical approach:**
- Priority score combines deadline urgency and task weightage
- Configurable urgency threshold (default 48 hours before deadline via `config /urgent-hours`)
- Uses efficient ranking algorithm for large task lists

### Feature 4: Module Archival
**What it does:** `module archive /mod [CODE]` hides a completed module from the main list but keeps its data for GPA calculations.

**Why it matters:** Keeps workspace clean after finishing a module while preserving grade history for CAP/transcript.

**Technical approach:**
- Archived modules load as needed (lazy-loading) to keep startup fast
- Hidden from `list` but still used in `grades list` and statistics
- Coordinates with semester archival (e.g., ending a semester auto-archives all modules)

### Feature 5: Semester Management System
**What it does:** `semester new AY2526-S2` creates and switches to a new semester. Organizes data by semester: SemesterBook → Semester → ModuleBook → Module → TaskList → Task

**Why it matters:** University spans multiple years/semesters. Separating data by semester allows CAP tracking, archive old semesters, and switch between them easily.

**Technical approach:**
- 5-level hierarchy keeps concerns separate
- Each semester stored in `data/[SEMESTER_ID].txt`
- `current.txt` tracks active semester; commands operate on active semester by default
- No data loss when switching semesters

### Feature 6: Grade Tracking & CAP Computation
**What it does:** `grade /mod [CODE] /grade [LETTER] /credits [NUM]` logs grades. `grades list` shows semester CAP, cumulative CAP across all semesters, and archived semester snapshots.

**Why it matters:** Completes the student workflow: track tasks → mark done → log grades → monitor GPA. Students need CAP to plan course loads and check academic standing.

**Technical approach:**
- Grade-to-points mapping (A+=5.0, A=5.0, A-=4.5, etc.) with SU handling
- CAP formula: `(grade_points × credits) / total_credits` summed per semester
- Archived semesters hold fixed CAP snapshots; cumulative CAP builds from archive + current
- `semester archive` captures final CAP for historical record

## Code Contributed
[RepoSense link](https://nus-cs2113-ay2526-s2.github.io/tp-dashboard/?search=heeelol&breakdown=true&sort=groupTitle%20dsc&sortWithin=title&since=2026-02-20T00%3A00%3A00&timeframe=commit&mergegroup=&groupSelect=groupByRepos&checkedFileTypes=docs~functional-code~test-code~other&filteredFileName=)

## Project Management & Leadership

- **QA Lead:** Set up testing infrastructure and automated test execution to catch bugs early
- **Process Owner:** Managed GitHub workflow, PR reviews, and team coding standards
- **Documentation:** Documented project decisions and architecture choices for team understanding

## Enhancements to Existing Features

- **List Command:** Refactored to support multiple filters (`list`, `list /mod`, `list /deadlines`, `list /notdone`, `list /top`) without code duplication
- **Task Model:** Extended to support optional fields (deadline, weightage, module, state) while keeping old data compatible
- **Cross-Semester Queries:** All commands properly search across multiple modules and semesters for features like GPA calculation
- **Data Persistence:** Enhanced to guarantee consistency during complex operations (semester archival, bulk marking)

## Documentation

### User Guide Contributions
- Mark/Unmark: Command syntax and examples for toggling task status
- List /deadlines: How to view all upcoming deadlines across modules
- List /top: How priority scoring works and viewing top urgent tasks
- Module Archival: Completing modules and archiving them
- Semester Management: Creating/switching semesters, understanding data isolation
- Grade Tracking: Logging grades, computing CAP, viewing semester/cumulative GPA
- FAQ: Task recovery, semester transitions, grade corrections, archival effects

### Developer Guide Contributions
- **SemesterBook Architecture:** Explains 5-level data hierarchy and why it's structured this way
- **Task State Machine:** How mark/unmark work with automatic rollback on failure
- **Priority Scoring:** Algorithm combining deadline and weightage into priority rank
- **Cross-Module Filtering:** Pipeline pattern for searching across all modules efficiently
- **Grade & CAP System:** Grade-point mapping, per-semester and cumulative CAP, archived semester snapshots
- **Sequence Diagrams:** Mark/Unmark flow, List /deadlines cross-module search, Semester archival with CAP capture, Grade entry with CAP update
- **Class Diagrams:** Task state transitions, SemesterBook hierarchy, Grade storage schema

## Team Contributions

### Code Review & Architecture Leadership
- **PR Reviews:** Reviewed pull requests for architecture consistency, test coverage (>80% target), and code quality
- **Architecture Mentoring:** Led discussions on SemesterBook design; helped team understand tradeoffs between hierarchical vs. flat data models
- **Complex Feature Integration:** Verified cross-module features (List /deadlines, Grade CAP) work consistently with team members' implementations

### Project Infrastructure & QA
- **Test Harness:** Set up `runtest.bat` for automated unit and integration testing (<2 second execution)
- **CI/CD:** Configured GitHub Actions to run tests and checkstyle checks on every commit
- **Testing Standards:** Documented testing conventions and best practices for the team

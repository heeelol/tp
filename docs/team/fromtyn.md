# Ni Tianyi - Project Portfolio Page

## Project: ModuleSync

ModuleSync is a desktop CLI-based task manager designed to help university students manage
module-related tasks across multiple semesters. The user interacts with it using a Command Line
Interface (CLI), and all data is stored locally in human-editable text files. It is written in
Java 17.

Given below are my contributions to the project.

## New Features

### Feature 1: Task Listing and Module Filter (`list`, `list /mod MODULE_CODE`)
**What it does:** Implemented the core task listing flow and extended it with module-based filtering
so users can view either all tasks or only the tasks belonging to one module.

**Justification:** Listing is one of the most frequently used actions in a CLI task tracker. The
module filter reduces clutter when a student wants to focus on a single module without scanning the
full semester task list.

**Highlights:** The module-filtered view preserves the same global task numbering used by commands
such as `mark`, `delete`, and `setweight`, so users can continue acting on tasks without learning a
separate indexing system.

### Feature 2: Startup Overdue Warning
**What it does:** Added an automatic overdue warning when the CLI starts. The app scans the active
semester for incomplete deadline tasks that have already passed their due time and prints them
before the command loop begins.

**Justification:** Students should not have to run a separate command just to discover overdue
work. A startup warning makes missed deadlines visible immediately.

**Highlights:** The warning keeps the original global task numbers, ignores completed overdue tasks,
and stays silent when there is nothing overdue.

### Feature 3: Same-Day Deadline Conflict Detection (`check /conflicts`)
**What it does:** Added a command that groups unfinished deadline tasks by date and shows only the
days with more than one deadline.

**Justification:** Same-day deadlines often create crunch periods. Surfacing them explicitly helps
students plan their study sessions earlier instead of discovering collisions too late.

**Highlights:** The output keeps fixed global task numbers while sorting deadlines within the same
day by earlier due time first, which makes the list easier to act on.

### Feature 4: Priority Score and Urgent Ranking (`list /top NUMBER`)
**What it does:** Added calculated priority-score support for task ranking and integrated it into
urgent task listing so users can quickly see which tasks deserve immediate attention.

**Justification:** Students often have to balance deadline urgency against academic weightage. A
priority score gives them a consistent way to compare tasks instead of relying on guesswork.

**Highlights:** The urgent-task ordering is driven by the same priority score shown to the user,
with due date and global task number used as tie-breakers for stable output.

### Feature 5: Cross-Semester Grade History and Cumulative Progress (`grades list`)
**What it does:** Added a grade summary command that shows recorded grades across multiple
semesters, together with semester CAP and cumulative CAP progress.

**Justification:** Students need more than a single-semester snapshot to understand their academic
journey. A cross-semester view makes it easier to review long-term progress.

**Highlights:** The final implementation prints semesters with recorded grades in chronological
order, labels archived and current semesters clearly, and handles non-CAP grades such as `S` by
displaying them without counting them toward CAP.

### Feature 6: Read-Only View for Old Semesters (`semester switch SEMESTER_NAME`)
**What it does:** Added support for switching to an existing semester and viewing archived semesters
in read-only mode.

**Justification:** Students often need to revisit past tasks and grades, but they should not be
able to modify finished semester data by accident.

**Highlights:** Switching updates the stored current-semester pointer, displays a clear read-only
message for archived semesters, and works with the application's central mutation guard so view
commands remain available while mutating commands are rejected.

## Code Contributed
[RepoSense link](https://nus-cs2113-ay2526-s2.github.io/tp-dashboard/?search=fromtyn&breakdown=true)

## Testing

- Added and updated tests for the core listing flow, including module-filtered listing in
  `ListCommandTest`.
- Added `ModuleSyncStartupWarningTest` to cover startup overdue warning behavior.
- Added `DeadlineConflictFeatureTest` for same-day deadline grouping and no-conflict behavior.
- Added `PriorityScoreListFeatureTest` for priority-score output and urgent-task ordering.
- Added `GradesListFeatureTest` for cross-semester grade history and cumulative progress.
- Added `SemesterReadOnlyViewFeatureTest` for archived-semester switching and read-only behavior.

## Documentation

### User Guide
- Updated the User Guide to add command documentation and examples for the features I implemented.
- Revised existing User Guide content so the command formats, explanations, and command summary
  stayed consistent with the final behavior of the application.

### Team
- Provide feedback on other people’s code and offer suggestions on the team’s direction.

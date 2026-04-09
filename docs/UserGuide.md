# User Guide

## Introduction

ModuleSync is a CLI app for tracking module-related tasks (e.g., assignments, quizzes, readings) grouped by module code.
It helps you capture tasks quickly, view only what matters (e.g., unfinished tasks for a module), and keep your list accurate.

## Quick Start

1. Ensure that you have Java 17 or above installed.
1. Download the latest version of `ModuleSync` from [here](http://link.to/modulesync).
1. Run the app.
	- If you have a JAR file: `java -jar modulesync.jar`
	- If you are running from source: `./gradlew run`
1. Type a command and press Enter.

## Features 

Notes about command formats:

* Words in `UPPER_CASE` are parameters to be supplied by you.
* Words in square brackets `[OPTIONAL]` are optional parameters.
* Module codes are case-insensitive (e.g., `cs2113` is treated as `CS2113`).
* Task indices are **1-based** and refer to the numbering shown by `list`.

### Adding a task: `add`
Adds a task under a module. If `/due` is provided, the task is recorded as a deadline.

Format: `add /mod MODULE_CODE /task DESCRIPTION [/due YYYY-MM-DD[-HHmm]] [/w PERCENT]`

* `/due` is optional.
	* `YYYY-MM-DD` is interpreted as `23:59` on that date.
	* `YYYY-MM-DD-HHmm` (e.g., `2026-04-01-2359`) specifies an exact time.
* `/w` is optional and must be an integer from `0` to `100` (representing percentage weightage).
* Both parameters can be added in any order.

Examples:

* `add /mod CS2113 /task Quiz 2`
* `add /mod CS2113 /task Final Project /w 25`
* `add /mod CS2113 /task Submit iP /due 2026-04-01`
* `add /mod CS2113 /task Submit iP /due 2026-04-01-2359 /w 10`

### Listing tasks: `list`
Shows tasks in the list.

Formats:

* `list` — lists all tasks across all modules.
* `list /mod MODULE_CODE` — lists tasks for a specific module.
* `list /deadlines` — lists only deadline tasks (sorted chronologically).

Examples:

* `list`
* `list /mod CS2113`
* `list /deadlines`

### Listing registered modules: `modules`
Shows a list of all modules you are currently tracking.

Format: `modules`

* A module is considered “registered” once you have at least one task under it.
* The output includes the number of tasks currently tracked for each module.

Example:

* `modules`

### Viewing semester statistics: `semesterstats`
Shows an overall summary across all tracked modules (treated as the current semester).

Format: `semesterstats`

The summary includes:
* number of modules tracked
* number of tasks done / not done, and completion percentage
* breakdown of todo vs deadline tasks
* weightage completion (if any tasks have weightage)
* a per-module workload summary

Example:

* `semesterstats`

### Listing not-done tasks for a module: `list /notdone`
Shows only tasks that are not marked done for a specific module.

Format: `list /notdone /mod MODULE_CODE`

* The output uses the **same global indices as `list`**, so you can use those indices for `mark`, `unmark`, or `delete`.
* `list /mod MODULE_CODE /notdone` is also accepted.

Examples:

* `list /notdone /mod CS2113`
* `list /mod CS2113 /notdone`

### Marking a task as done: `mark`
Marks a task as completed.

Format: `mark TASK_NUMBER`

* Task number refers to the index shown in the `list` output (1-based).

Examples:

* `mark 1`
* `mark 5`

### Unmarking a task as not done: `unmark`
Marks a task as incomplete (undoes a previous `mark`).

Format: `unmark TASK_NUMBER`

* Task number refers to the index shown in the `list` output (1-based).

Examples:

* `unmark 1`
* `unmark 3`

### Deleting a task: `delete`
Deletes a task using its **display index** from `list`.

Format: `delete TASK_NUMBER`

* Task number refers to the index shown in the `list` output (1-based).

Example:

* `delete 3`

### Assigning weightage to an existing task: `setweight`
Assigns or updates the percentage weightage of a task that was added without one (or to change an existing weightage).

Format: `setweight TASK_NUMBER PERCENT`

* `TASK_NUMBER` is the **global display index** shown by `list` or `list /mod MODULE_CODE` (1-based).
* `PERCENT` must be an integer from `0` to `100`.
* If the task already has a weightage, it will be overwritten and you will be told the previous value.

Examples:

* `setweight 3 25` — sets the weightage of task 3 to 25%
* `setweight 7 0` — sets the weightage of task 7 to 0%

> **Tip:** Run `list /mod MODULE_CODE` first to see the global task numbers for tasks in that module,
> then use `setweight` with the number you see.

### Archiving a module: `module archive`
Archives a module to keep your active workspace clean while retaining its data and history. Archived modules are read-only and do not appear in the main task list, but their data remains in your records.

Format: `module archive /mod MODULE_CODE`

* The module must exist and not already be archived.
* Archived modules will appear with an `[archived]` tag in the `modules` list.
* Archived modules cannot have new tasks added to them; you must unarchive first.

Examples:

* `module archive /mod CS2113` — archives the CS2113 module

### Unarchiving a module: `module unarchive`
Unarchives a previously archived module, allowing you to add and modify tasks again.

Format: `module unarchive /mod MODULE_CODE`

* The module must exist and already be archived.

Examples:

* `module unarchive /mod CS2113` — unarchives the CS2113 module

### Initializing a new semester: `semester new`
Creates a new semester and switches to it, allowing you to start a fresh tracking cycle with a clean module list.

Format: `semester new SEMESTER_NAME`

* `SEMESTER_NAME` is a unique identifier for the semester (e.g., `AY2526-S2`, `Spring2026`).
* If the semester already exists, you will be switched to it instead of creating a new one.
* All semesters are saved automatically in your data folder for future reference.

Examples:

* `semester new AY2526-S2` — creates and switches to the new semester AY2526-S2
* `semester new AY2627-S1` — creates and switches to the new semester AY2627-S1

### Recording a grade: `grade`
Records your final grade or S/U (Satisfactory/Unsatisfactory) status for a module, creating a permanent academic record.

Format: `grade /mod MODULE_CODE /grade GRADE_VALUE`

* `GRADE_VALUE` can be any letter grade (e.g., `A+`, `A`, `B+`, `B`, `C`, etc.) or `S` (Satisfactory) / `U` (Unsatisfactory).
* Grades are stored in uppercase regardless of how you enter them.
* You can update a grade by entering the command again with a new value.
* Grades appear when you use the `modules` command to view your module list.

Examples:

* `grade /mod CS2113 /grade A+` — records an A+ for CS2113
* `grade /mod CS2040 /grade S` — records a Satisfactory grade for CS2040
* `grade /mod MA1521 /grade U` — records an Unsatisfactory grade for MA1521

### Exiting the application: `bye`
Closes the application.

Format: `bye`

Example:

* `bye`

## FAQ

**Q**: How do I transfer my data to another computer? 

**A**: Your task data is stored in a file called `modules.txt` in the `data/` folder. You can copy this file to the `data/` folder of ModuleSync on another computer to transfer all your tasks.

**Q**: Can I edit the `modules.txt` file directly?

**A**: Yes, you can manually edit the `modules.txt` file as it is a plain text file. However, be careful with the format to avoid corruption. Make sure to follow the same format as the existing entries.

Each task is stored as a ` | `-separated line:

* Todo (unweighted): `MODULE | T | DONE_FLAG | DESCRIPTION`
* Todo (weighted): `MODULE | T | DONE_FLAG | DESCRIPTION | WEIGHTAGE`
* Deadline (unweighted): `MODULE | D | DONE_FLAG | DESCRIPTION | DUE_DATETIME`
* Deadline (weighted): `MODULE | D | DONE_FLAG | DESCRIPTION | DUE_DATETIME | WEIGHTAGE`

Where:
* `DONE_FLAG` is `1` (done) or `0` (not done)
* `DUE_DATETIME` is `yyyy-MM-dd HH:mm`
* `WEIGHTAGE` is an integer `0` to `100`

**Q**: What happens when I mark a task as done?

**A**: Marking a task sets its status to done. Once marked, it will not appear in `list /notdone`. You can change it back later using the `unmark` command.

**Q**: Will my data be lost if the application crashes?

**A**: As long as your last successful command changed the task list, the updated data is saved to `modules.txt` immediately after that command (for example, `add`, `delete`, `mark`, and `unmark`). Commands that do not modify data, such as `list`, do not trigger a save. If a command fails, no data is saved for that command.

## Command Summary

| Action | Format |
|--------|--------|
| Add task | `add /mod MODULE_CODE /task DESCRIPTION [/due YYYY-MM-DD[-HHmm]] [/w PERCENT]` |
| List all tasks | `list` |
| List tasks by module | `list /mod MODULE_CODE` |
| List upcoming deadlines | `list /deadlines` |
| List not-done tasks by module | `list /notdone /mod MODULE_CODE` |
| List registered modules | `modules` |
| View semester statistics | `semesterstats` |
| Mark task as done | `mark TASK_NUMBER` |
| Unmark task as not done | `unmark TASK_NUMBER` |
| Delete task | `delete TASK_NUMBER` |
| Set task weightage | `setweight TASK_NUMBER PERCENT` |
| Archive module | `module archive /mod MODULE_CODE` |
| Unarchive module | `module unarchive /mod MODULE_CODE` |
| Initialize new semester | `semester new SEMESTER_NAME` |
| Record grade for module | `grade /mod MODULE_CODE /grade GRADE_VALUE` |
| Exit application | `bye` |

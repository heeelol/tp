# User Guide

## Introduction

ModuleSync is a desktop CLI app for students who manage coursework across multiple modules and semesters.
It is intended for students who are comfortable typing short commands and want a faster way to review tasks than
clicking through a GUI.

ModuleSync helps you:

* record tasks under module codes
* spot overdue work, urgent deadlines, and same-day crunch periods
* review grades and cumulative academic progress
* switch to old semesters in read-only mode when you need to reference past work safely

This guide focuses on the commands and examples you need in order to use the product confidently.

## Quick Start

1. Ensure that you have Java 17 or above installed.
1. Open a terminal in the project folder.
1. Run the app.
   * If you have a JAR file: `java -jar modulesync.jar`
   * If you are running from source: `./gradlew run`
1. On first launch, ModuleSync creates the `data/` folder if it does not exist yet.
1. Type a command and press Enter.

## Features

Notes about command formats:

* Words in `UPPER_CASE` are parameters to be supplied by you.
* Words in square brackets `[OPTIONAL]` are optional parameters.
* Module codes are case-insensitive. For example, `cs2113` is treated as `CS2113`.
* Task indices are **1-based** and refer to the numbering shown by `list`.
* Dates use `yyyy-MM-dd` or `yyyy-MM-dd-HHmm`.
* Most commands operate on the **active semester** shown when the CLI starts.

### Automatic overdue warning on startup

When ModuleSync opens, it checks the active semester for incomplete deadline tasks whose deadlines have already
passed. This warning appears before the command loop starts so you can react immediately.

Example output:

```text
Welcome to ModuleSync
What would you like to do?
Active semester: AY2525-S2
Overdue warning: 1 task(s) have passed their deadlines.
2.[CS2113] [D][ ] Project checkpoint (was due: Apr 08 2026, 09:00)
```

### Adding a task: `add`

Adds a task under a module. If `/due` is provided, the task is recorded as a deadline.

Format: `add /mod MODULE_CODE /task DESCRIPTION [/due YYYY-MM-DD[-HHmm]] [/w PERCENT]`

* `/due` is optional.
  * `YYYY-MM-DD` is interpreted as `23:59` on that date.
  * `YYYY-MM-DD-HHmm` specifies an exact time.
* `/w` is optional and must be an integer from `0` to `100`.
* `/due` and `/w` can appear in any order.

Examples:

* `add /mod CS2113 /task Quiz 2`
* `add /mod CS2113 /task Final Project /w 25`
* `add /mod CS2113 /task Submit iP /due 2026-04-01`
* `add /mod CS2113 /task Submit iP /due 2026-04-01-2359 /w 10`

### Listing tasks: `list`

Shows tasks in the active semester.

Formats:

* `list` lists all non-archived modules in the active semester.
* `list /mod MODULE_CODE` lists tasks for a specific module.
* `list /deadlines` lists only deadline tasks, sorted chronologically.
* `list /top NUMBER` lists the highest-priority tasks first.

Examples:

* `list`
* `list /mod CS2113`
* `list /deadlines`
* `list /top 5`

### Listing registered modules: `modules`

Shows all modules currently tracked in the active semester.

Format: `modules`

The output includes:

* module code
* number of tasks currently tracked
* recorded grade, if one exists
* archived status, if the module has been archived

Example:

* `modules`

### Viewing semester statistics: `semesterstats`

Shows an overall summary across all tracked modules in the active semester.

Format: `semesterstats`

The summary includes:

* number of modules tracked
* number of tasks done and not done, and completion percentage
* breakdown of todo and deadline tasks
* weightage completion, if any tasks have weightage
* per-module workload summary

Example:

* `semesterstats`

### Listing not-done tasks for a module: `list /notdone`

Shows only unfinished tasks for a specific module.

Format: `list /notdone /mod MODULE_CODE`

* `list /mod MODULE_CODE /notdone` is also accepted.
* The output uses the **same global indices as `list`**, so you can use those indices for `mark`, `unmark`,
  `delete`, `setweight`, or `setdeadline`.

Examples:

* `list /notdone /mod CS2113`
* `list /mod CS2113 /notdone`

### Marking a task or module as done: `mark`

Marks a specific task as completed, or marks all tasks within the specified module as done.

Formats:

* `mark TASK_NUMBER`
* `mark /mod MODULE_CODE /all`

Examples:

* `mark 1`
* `mark 5`
* `mark /mod CS2113 /all` (Marks all tasks under CS2113 as complete)

### Unmarking a task as not done: `unmark`

Marks a task as incomplete.

Format: `unmark TASK_NUMBER`

Examples:

* `unmark 1`
* `unmark 3`

### Deleting a task: `delete`

Deletes a task using its display index from `list`.

Format: `delete TASK_NUMBER`

Example:

* `delete 3`

### Assigning or editing weightage: `setweight` / `editweight`

Assigns or updates the percentage grading weight of an existing task.

Formats:

* `setweight TASK_NUMBER PERCENT`
* `editweight TASK_NUMBER /w PERCENT`

* `TASK_NUMBER` is the global display index shown by `list`.
* `PERCENT` must be an integer from `0` to `100`.

Examples:

* `setweight 3 25`
* `setweight 7 0`
* `editweight 3 /w 25`

### Updating or adding a deadline: `setdeadline` / `editdeadline`

Adds a deadline to an existing task, or updates the due date of an existing task. Both commands perform the same action without needing to recreate the task.

Formats:

* `setdeadline TASK_NUMBER /by YYYY-MM-DD[-HHmm]`
* `editdeadline TASK_NUMBER /by YYYY-MM-DD[-HHmm]`

* `TASK_NUMBER` is the global display index shown by `list`.
* `YYYY-MM-DD` is interpreted as `23:59` on that date.

Examples:

* `setdeadline 2 /by 2026-04-20`
* `editdeadline 5 /by 2026-04-20-1800`

### Checking same-day deadline conflicts: `check /conflicts`

Shows dates where more than one unfinished deadline falls on the same calendar day.
Use this to spot crunch periods before they pile up.

Formats:

* `check /conflicts`
* `/conflicts`

Example output:

```text
Here are your same-day deadline conflicts:
2026-04-15 (2 deadlines)
  4.[CS2113] [D][ ] Project checkpoint (due: 09:00)
  2.[CS2100] [D][ ] Quiz (due: 18:00)
```

### Checking urgent deadlines: `check /urgent`

Filters for incomplete tasks due within the next 48 hours and sorts them by urgency (closest first).

Formats:

* `check /urgent`
* `/urgent`

Example output:

```text
Urgent tasks (due within 48 hours):
  1.[CS2113] [D][ ] Final Project (due: 12 hours)
```

### Recording a module grade: `grade`

Records a final grade for a module in the active semester.

Format: `grade /mod MODULE_CODE /grade GRADE_VALUE`

Examples:

* `grade /mod CS2113 /grade A-`
* `grade /mod MA1521 /grade S`

### Viewing CAP summary: `cap`

Calculates the current semester CAP and cumulative CAP on a standard 5.0 scale (ignoring CS/CU or ungraded modules).

Format: `cap`

Example output:

```text
Semester CAP: 5.00
Cumulative CAP: 4.80
```

### Viewing grade history: `grades list`

Shows semesters with recorded grades in chronological order, together with semester CAP and cumulative progress.

Format: `grades list`

Example output:

```text
AY2525-S1 Results (Archived)
Module   Credits  Grade   Points
CS1010   4        A       5.0

Semester CAP: 5.00 (4 MCs)
Cumulative CAP at archive: 5.00 (4 MCs)
```

### Listing tracked semesters: `semester list`

Prints a numbered list of all tracked semesters and indicates if they are Active or Archived.

Format: `semester list`

Example output:

```text
1. AY2525-S1 (Archived)
2. AY2525-S2 (Active)
```

### Creating a new semester: `semester new`

Creates a new semester if it does not exist yet, then switches to it.
If it already exists, ModuleSync switches to the existing semester instead.

Format: `semester new SEMESTER_NAME`

Example:

* `semester new AY2627-S1`

### Switching to another semester: `semester switch`

Switches the CLI view to an existing semester.

Format: `semester switch SEMESTER_NAME`

If the target semester is archived, ModuleSync opens it in **read-only** mode. You can still use view commands such
as `list`, `cap`, and `grades list`, but mutating commands such as `add`, `delete`, `mark`, and `setweight` will be
rejected.

Example output:

```text
Now viewing AY2525-S1 [read-only]. Use 'semester switch AY2525-S2' to return.
```

### Archiving a module: `module archive`

Archives a module in the active semester so it no longer appears in the main `list` and `list /deadlines` views.

Format: `module archive /mod MODULE_CODE`

Example:

* `module archive /mod CS1231S`

Archived modules still appear in `modules`, and you can restore them later with `module unarchive`.

### Restoring an archived module: `module unarchive`

Restores a module that was hidden with `module archive`.

Format: `module unarchive /mod MODULE_CODE`

Example:

* `module unarchive /mod CS1231S`

### Exiting the application: `bye`

Closes the application.

Format: `bye`

## FAQ

**Q**: How do I transfer my data to another computer?

**A**: Copy the entire `data/` folder. ModuleSync stores the active semester pointer in `data/current.txt`
and stores each semester in its own `data/SEMESTER_NAME.txt` file.

**Q**: Can I edit the data files directly?

**A**: Yes, but be careful. Semester files are plain-text UTF-8 files. A semester file may start with `#archived`,
and each module is introduced by a metadata line such as `#MOD | CS2113 | grade:A+ | credits:4`.

Each task is stored as a ` | `-separated line:

* Todo (unweighted): `MODULE | T | DONE_FLAG | DESCRIPTION`
* Todo (weighted): `MODULE | T | DONE_FLAG | DESCRIPTION | WEIGHTAGE`
* Deadline (unweighted): `MODULE | D | DONE_FLAG | DESCRIPTION | DUE_DATETIME`
* Deadline (weighted): `MODULE | D | DONE_FLAG | DESCRIPTION | DUE_DATETIME | WEIGHTAGE`

Where:

* `DONE_FLAG` is `1` for done or `0` for not done
* `DUE_DATETIME` is `yyyy-MM-dd HH:mm`
* `WEIGHTAGE` is an integer from `0` to `100`

**Q**: Why was my command rejected after I switched semesters?

**A**: You likely switched to an archived semester. Archived semesters are read-only so that you can reference old
tasks and grades without accidentally editing finished data.

**Q**: Why does `grades list` or `cap` show `N/A` or zero MCs for some modules?

**A**: CAP-related commands only count CAP-bearing grades together with the credits stored for that module.
Grades such as `S`, `U`, `CS`, and `CU` do not affect CAP.

**Q**: Will my data be lost if the application crashes?

**A**: Successful mutating commands are saved immediately to the relevant semester file. Commands such as
`semester new` and `semester switch` also update the active-semester pointer. If a command fails, nothing is saved
for that command.

## Command Summary

| Action | Format |
|--------|--------|
| Add task | `add /mod MODULE_CODE /task DESCRIPTION [/due YYYY-MM-DD[-HHmm]] [/w PERCENT]` |
| List all tasks | `list` |
| List tasks by module | `list /mod MODULE_CODE` |
| List upcoming deadlines | `list /deadlines` |
| List top urgent tasks | `list /top NUMBER` |
| List not-done tasks by module | `list /notdone /mod MODULE_CODE` |
| List registered modules | `modules` |
| View semester statistics | `semesterstats` |
| Mark task as done | `mark TASK_NUMBER` |
| Mark all tasks in a module as done | `mark /mod MODULE_CODE /all` |
| Unmark task as not done | `unmark TASK_NUMBER` |
| Delete task | `delete TASK_NUMBER` |
| Set task weightage | `setweight TASK_NUMBER PERCENT` |
| Edit task weightage | `editweight TASK_NUMBER /w PERCENT` |
| Set or update task deadline | `setdeadline TASK_NUMBER /by YYYY-MM-DD[-HHmm]` |
| Edit an existing deadline | `editdeadline TASK_NUMBER /by YYYY-MM-DD[-HHmm]` |
| Check same-day deadline conflicts | `check /conflicts` or `/conflicts` |
| Check urgent deadlines | `check /urgent` or `/urgent` |
| Record a module grade | `grade /mod MODULE_CODE /grade GRADE_VALUE` |
| View CAP summary | `cap` |
| View grade history | `grades list` |
| List tracked semesters | `semester list` |
| Create and switch to a semester | `semester new SEMESTER_NAME` |
| Switch to another semester | `semester switch SEMESTER_NAME` |
| Archive a module | `module archive /mod MODULE_CODE` |
| Restore an archived module | `module unarchive /mod MODULE_CODE` |
| Exit application | `bye` |

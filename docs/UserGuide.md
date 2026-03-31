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
* Module codes are case-insensitive (e.g., `cs2113` is treated as `CS2113`).
* Task indices are **1-based** and refer to the numbering shown by `list`.

### Adding a task: `add`
Adds a task under a module. If `/due` is provided, the task is recorded as a deadline.

Format: `add /mod MODULE_CODE /task DESCRIPTION [/due YYYY-MM-DD[-HHmm]] [/w PERCENT]`

* `/due` is optional.
	* `YYYY-MM-DD` is interpreted as `23:59` on that date.
	* `YYYY-MM-DD-HHmm` (e.g., `2026-04-01-2359`) specifies an exact time.
* `/w` is optional and must be an integer from `0` to `100`.

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

### Listing not-done tasks for a module: `list /notdone`
Shows only tasks that are not marked done for a specific module.

Format: `list /notdone /mod MODULE_CODE`

* The output uses the **same global indices as `list`**, so you can use those indices for `mark`, `unmark`, or `delete`.
* `list /mod MODULE_CODE /notdone` is also accepted.

Examples:

* `list /notdone /mod CS2113`
* `list /mod CS2113 /notdone`

### Deleting a task: `delete`
Deletes a task using its **display index** from `list`.

Format: `delete TASK_NUMBER`

Example:

* `delete 3`

## FAQ

**Q**: How do I transfer my data to another computer? 

**A**: {your answer here}

## Command Summary

* Add task `add /mod MODULE_CODE /task DESCRIPTION [/due YYYY-MM-DD[-HHmm]] [/w PERCENT]`
* List all tasks `list`
* List tasks by module `list /mod MODULE_CODE`
* List upcoming deadlines `list /deadlines`
* List not-done tasks by module `list /notdone /mod MODULE_CODE`
* Delete task `delete TASK_NUMBER`

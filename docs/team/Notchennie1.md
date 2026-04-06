# Notchennie1 - Project Portfolio Page

## Project: ModuleSync

ModuleSync is a desktop CLI-based task manager designed to help university students achieve a balanced academic life through structured organisation of their module-related tasks. The user interacts with it using a Command Line Interface (CLI), and all data is stored locally in a human-editable text file. It is written in Java 17, and has about 4 kLoC.

Given below are my contributions to the project.

## New Features

### Feature 4: Delete Task (`delete TASK_NUMBER`)
**What it does:** Allows users to delete a task by its global display index, helping them remove obsolete or incorrectly entered items.

**Justification:** Task lists evolve frequently; deletion is necessary to keep the tracker accurate and uncluttered.

**Highlights:**
- Deletion updates the in-memory state safely and persists the updated state to storage.
- Includes unit testing coverage for valid deletion and invalid index handling.

### Feature 3: List Not-Done Tasks by Module (`list /notdone /mod MODULE_CODE`)
**What it does:** Supports listing only the tasks that are not marked done for a specific module, separated from the main list view.

**Justification:** When a module accumulates many completed tasks, users may want to focus only on remaining work without being distracted by completed items.

**Highlights:**
- Ensured filtering so only incomplete tasks appear in the result.
- Kept indexing consistent with the application’s existing listing conventions.
- Reused the existing command/UI flow by delegating display to the UI.

### Feature 1: List Registered Modules (`modules`)
**What it does:** Adds a `modules` command that shows all modules currently being tracked, together with the number of tasks under each module.

**Justification:** As the number of tracked modules grows, users need a quick way to confirm which modules they are currently managing without scanning the entire task list.

**Highlights:**
- Implemented using the existing Command pattern (Parser → Command → UI) as a view-only command.
- Designed output to be concise while still informative (module code + task count).
- Added parser coverage to ensure the new keyword is recognised reliably.

### Feature 2: Semester Statistics (`semesterstats`)
**What it does:** Adds a `semesterstats` command that aggregates a semester-wide summary across all tracked modules (treated as the current semester). The output includes overall completion statistics, a breakdown by task type, optional weightage completion (if weightage exists), and a per-module distribution summary.

**Justification:** Students often want a high-level overview of workload and progress across all modules, not just within a single module. A single summary command helps them evaluate how balanced their workload is and how much work is left.

**Highlights:**
- Implemented statistics computation as an on-demand aggregation over in-memory tasks, keeping the feature view-only.
- Ensured weighted tasks remain meaningful across restarts by updating the storage encoding/decoding to persist optional task weightage while maintaining backward compatibility.
- Added tests to validate the printed summary format and to ensure the command does not trigger a save.

## Code Contributed
[RepoSense link](https://nus-cs2113-ay2526-s2.github.io/tp-dashboard/?search=Notchennie1&breakdown=true)

## Enhancements to Existing Features

- Updated the storage format to persist optional task weightage as a trailing field, while remaining backward compatible with previously saved data.
- Improved overall UX consistency by ensuring view-only commands do not perform unnecessary saves.

## Documentation

### User Guide
- Added documentation for `modules` and `semesterstats`.
- Updated the command summary table to include the new commands.
- Documented the `modules.txt` storage format (including optional weightage fields) to support safe manual edits.

### Developer Guide
- Added implementation write-ups for `modules` and `semesterstats` under my author section.
- Added UML class and sequence diagram sources (PlantUML) for both features.

## Testing

- Added JUnit tests to cover parsing and execution output for the new `modules` and `semesterstats` commands.
- Ensured tests verify these commands are view-only (no storage save calls).
- Maintained/extended deletion testing to verify task counts and index behavior after deletion.

## Community

- Reviewed teammates’ work and kept documentation/feature descriptions consistent with the project’s existing conventions.

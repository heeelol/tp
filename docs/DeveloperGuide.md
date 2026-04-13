# Developer Guide

## Acknowledgements

* [CS2113 project template (AddressBook-Level 3)](https://github.com/se-edu/addressbook-level3) — project structure and Gradle build configuration
* [JUnit 5](https://junit.org/junit5/) — unit and integration testing framework
* [PlantUML](https://plantuml.com/) — UML diagram generation

---

## Design

### Architecture

ModuleSync is structured as a layered architecture with five major components. Each layer depends only on layers below it — no inner component imports from `Ui` or `Parser`.

<img src="images/ArchitectureDiagram.png" alt="Architecture overview diagram" />

> Generated from [`docs/diagrams/ArchitectureDiagram.puml`](diagrams/ArchitectureDiagram.puml)

### Component Responsibilities

**`Ui`** — The sole point of contact with the user. Reads `stdin` and writes `stdout`. Contains no business logic and knows nothing about the data model or file format.

**`Parser`** — Converts a raw input string into the correct concrete `Command` subclass. All syntax validation (flag presence, integer range checks, blank-string guards) happens here. Returns a fully constructed command or throws `ModuleSyncException` with a user-readable message.

**`Command` (abstract)** — Encapsulates a single user intent. Every subclass holds the parameters extracted by `Parser` and implements `execute(ModuleBook, Storage, Ui)`. Commands that do not modify any data override `isMutating()` to return `false`, allowing `ModuleSync` to enforce the read-only guard centrally without per-command duplication. A second abstract subclass, `SemesterCommand`, extends `Command` for commands that operate on semester lifecycle (switch, archive, list semesters) and are never blocked by the read-only guard.

**Data Model** — Pure in-memory state with no I/O. The full ownership hierarchy is:
`SemesterBook` → `Semester` → `ModuleBook` → `Module` → `TaskList` → `Task` (`Todo` | `Deadline`).

**`Storage` / `SemesterStorage`** — All file I/O is isolated here. `Storage` handles one per-semester task file. `SemesterStorage` manages the multi-semester directory layout and the `current.txt` pointer that records which semester is active. Neither class knows about commands.

### Data Model

The following class diagram shows the full ownership hierarchy of the application's data model and the key fields on each class:

<img src="images/DataModelClassDiagram.png" alt="Full data model class diagram" />

> Generated from [`docs/diagrams/DataModelClassDiagram.puml`](diagrams/DataModelClassDiagram.puml)

Note that `Task` is abstract, with `Todo` and `Deadline` as the only concrete subclasses. The `weightage: Integer` field on `Task` is nullable — `null` means no weightage has been assigned, and any value from `0` to `100` is a valid percentage weight.

### Command Architecture

The following class diagram shows how all commands fit into the inheritance hierarchy and how `ModuleSync` and `Parser` relate to the abstract `Command` class:

<img src="images/CommandArchitectureClassDiagram.png" alt="Command architecture class diagram" />

> Generated from [`docs/diagrams/CommandArchitectureClassDiagram.puml`](diagrams/CommandArchitectureClassDiagram.puml)

The key design decision is that `Parser` creates and `ModuleSync` dispatches to the **abstract** `Command` type — neither depends on any concrete subclass. A developer adding a new feature therefore only needs to:
1. Create a class extending `Command` (or `SemesterCommand` for semester-level features).
2. Implement `execute()`.
3. Register the new keyword in `Parser`.

Nothing else in the codebase needs to change.

### Request Lifecycle

A typical mutating command (e.g. `add /mod CS2113 /task Final Project /w 25`) flows through the application as follows:

1. `Ui` reads the raw input string from `stdin`.
2. `Parser` parses the string and constructs the appropriate `Command` subclass.
3. `ModuleSync` calls `command.isMutating()`. If `true`, it also calls `semesterBook.isCurrentSemesterReadOnly()`. If the semester is archived, the command is rejected here — no per-command guard is needed.
4. `ModuleSync` retrieves the active semester's `ModuleBook` and a per-semester `Storage` instance, then calls `command.execute(activeModuleBook, activeSemesterStorage, ui)`.
5. The command mutates the data model, calls `storage.save(moduleBook)`, and calls the appropriate `ui.show...()` method.

Read-only commands (e.g. `stats /mod CS2113`, `list`) skip step 3 entirely and never call `storage.save()`.

---

## Implementation

### Assigning and Managing Task Weightage

#### Overview

This feature allows students to associate a percentage-based weight with any task, reflecting its contribution to the overall module grade. Weightage is entirely optional — tasks without it are fully functional and display normally.

Two commands implement this feature:
- `add /mod CODE /task DESCRIPTION [/w PERCENT]` — creates a task with an optional weightage at creation time.
- `setweight TASK_NUMBER PERCENT` — assigns or updates the weightage of an existing task.

Both commands follow the exact same Command Pattern described in the Architecture section: `Parser` validates input and constructs the command; `ModuleSync` dispatches it; the command mutates the model, saves, and notifies the Ui.

#### Where Weightage Lives in the Data Model

`weightage` is a field on the abstract `Task` class, typed as `Integer` (nullable). This decision has three consequences a future developer must understand:

- **`null` means unweighted.** There is no separate `boolean isWeighted` flag. `hasWeightage()` is a convenience wrapper for `weightage != null`.
- **Each task carries its own weight independently.** There is no per-module weight table. This supports heterogeneous weightages (e.g. a 30% exam and a 10% quiz under the same module) without any coupling between tasks.
- **The sum across a module is not enforced.** A future `v2.1` feature that validates the 100% cap would need to add that logic to `Module` or `Parser` — the `Task` class itself has no such constraint.

The following object diagram shows the heap state immediately after `add /mod CS2113 /task Final Project /w 25` executes. It uses the full path from `SemesterBook` down to the newly created `Todo` to illustrate how the ownership chain from the architecture section maps to a concrete runtime state:

<img src="images/WeightedTaskObjectDiagram.png" alt="Object diagram: heap state after adding a weighted task" />

> Generated from [`docs/diagrams/WeightedTaskObjectDiagram.puml`](diagrams/WeightedTaskObjectDiagram.puml)

#### Execution Flow

The following sequence diagram illustrates the interactions when the user executes `add /mod CS2113 /task Final Project /w 25`:

<img src="images/AddWeightageSequenceDiagram.png" alt="Sequence diagram for the add weighted task command" />

> Generated from [`docs/diagrams/AddWeightageSequenceDiagram.puml`](diagrams/AddWeightageSequenceDiagram.puml)

The `setweight` command follows the identical architectural path — `Parser` → `SetWeightCommand` → `ModuleBook.getTaskByDisplayIndex()` → `task.setWeightage()` → `Storage.save()` → `Ui.showWeightSet()`. The only structural difference is that it retrieves an existing `Task` by display index rather than creating a new one. A separate sequence diagram for `setweight` would be a verbatim repeat and is therefore omitted.

#### Validation and Error Handling

All validation for weightage input is performed in `Parser` before any command object is constructed. This keeps every command constructor free of user-facing validation logic — if an `AddTodoCommand` or `SetWeightCommand` is ever instantiated, its parameters are already guaranteed valid. The `assert` statements in the constructors document these guarantees for future developers.

The following activity diagram maps every decision point in the validation flow. It covers both `add /w` and `setweight` since they share the same validation rules:

<img src="images/WeightageValidationActivityDiagram.png" alt="Activity diagram for weightage input validation" />

> Generated from [`docs/diagrams/WeightageValidationActivityDiagram.puml`](diagrams/WeightageValidationActivityDiagram.puml)

#### Design Considerations

**Aspect: Storing weightage as `Integer` (nullable) vs `int` + boolean flag**

* **Alternative 1 (current): `Integer weightage` — null means unweighted.**
    * Pros: Single field, single null-check. No risk of a two-field inconsistency.
    * Cons: Callers must handle `null`; less immediately obvious than a primitive to developers unfamiliar with the convention.

* **Alternative 2: `int weightage` + `boolean isWeighted`.**
    * Pros: Explicit intent; no null handling.
    * Cons: Two fields that must always be kept consistent. `isWeighted = true, weightage = 0` is technically valid but semantically ambiguous.

We chose `Integer` to keep the model lean and consistent with Java idiom for optional numeric values.

**Aspect: Where to validate the 0–100 range**

* **Alternative 1 (current): Validate in `Parser` at parse time.**
    * Pros: Invalid input is rejected before any object is constructed. Command constructors can use `assert` rather than defensive exception-throwing.
    * Cons: Validation rules in `Parser` must be kept in sync with the constraints documented on `Task.setWeightage()`.

* **Alternative 2: Validate inside `Task.setWeightage()`.**
    * Pros: Constraint is co-located with the field it guards.
    * Cons: `setWeightage` would need to throw a checked exception, which would propagate through the storage loading path — where a silent warning-log-and-skip is more appropriate than a hard failure.

**Aspect: Weightage field on `Task` vs. weightage map on `Module`**

* **Alternative 1 (current): Field on `Task`.**
    * Pros: Each task is self-contained. No coupling between tasks within a module. Trivially serialised per-task in the storage file.
    * Cons: The 100% cap constraint (if ever added) must be enforced at a higher level.

* **Alternative 2: `Map<Task, Integer>` on `Module`.**
    * Pros: Centralises weight management; a 100% cap is easy to enforce.
    * Cons: Complicates task removal (map entry must be cleaned up). Couples `Module` to task identity in a fragile way that breaks if tasks are ever replaced rather than mutated in-place.

---

### [Feature] List Upcoming Deadlines (`list /deadlines`)

#### Implementation

The List Deadlines feature provides a specialized view that displays only tasks with deadlines,
grouped by actionability so users can decide what to work on next. The output order is:
upcoming (future), then due today, then overdue. Within each group, deadlines are sorted
consistently to keep the view predictable.

The feature implements the following operations:

* `Parser#parseList(String)` — Parses the `list` command and checks for optional filters. When
  `/deadlines` is detected, it returns a `ListDeadlinesCommand` instead of the regular `ListCommand`.
* `ListDeadlinesCommand#execute(ModuleBook, Storage, Ui)` — Executes the deadline listing by
  calling `Ui#showDeadlineList()`.
* `Ui#showDeadlineList(ModuleBook)` — Collects all `Deadline` objects from all modules, groups them
  into upcoming, due-today, and overdue buckets, then sorts and displays them in a user-friendly format.

Given below is the workflow for the List Deadlines feature:

**Step 1.** The user inputs `list /deadlines`.

**Step 2.** `ModuleSync#run()` calls `Ui#readCommand()`, which reads the raw input string from stdin.

**Step 3.** `ModuleSync#run()` passes the raw string to `Parser#parse(...)`. The parser detects the
`list` keyword and delegates to `Parser#parseList()`.

**Step 4.** `Parser#parseList()` checks if the input contains `/deadlines`. If found, it instantiates
a `ListDeadlinesCommand` and returns it; otherwise, it returns the regular `ListCommand`.

**Step 5.** `ModuleSync#run()` calls `ListDeadlinesCommand#execute(moduleBook, storage, ui)`.

**Step 6.** `execute()` delegates to `Ui#showDeadlineList(moduleBook)`.

**Step 7.** `showDeadlineList()` iterates through all modules in the `ModuleBook` and collects all
`Deadline` objects. For each deadline, it records the task number and module code.

**Step 8.** The collected deadlines are bucketed by urgency in this order:
upcoming (future), due today, then overdue.
Each bucket is then sorted deterministically before concatenating the final list.

**Step 9.** Finally, the sorted deadlines are displayed to the user, showing module code, status,
description, due date/time, and days remaining.

#### Design Considerations

**Aspect: Filtering vs. separate command**

* **Alternative 1 (Current choice): Use optional filter syntax `list /deadlines`.**
    * Pros: Consistent with existing command structure. Can extend with more filters in future
      (e.g., `list /todos`). Reduces command namespace pollution.
    * Cons: Slightly more parsing logic in `Parser#parseList()`.

* **Alternative 2: Create a separate command `deadlines` or `view-deadlines`.**
    * Pros: Simpler parsing; no need to check for filters.
    * Cons: Increases command count; less extensible for future filters.

We chose the filter approach for consistency and extensibility.

**Aspect: Sorting order for deadlines**

* **Alternative 1 (Current choice): Group by urgency (upcoming, due today, overdue).**
  * Pros: Keeps actionable deadlines at the top and prevents stale overdue tasks from burying near-term work.
  * Cons: Slightly more sorting logic than a single chronological comparator.

* **Alternative 2: Sort all deadlines by due date in ascending order.**
  * Pros: Minimal implementation complexity.
  * Cons: Very old overdue tasks can dominate the top of the list and reduce planning usefulness.

We chose urgency grouping because it better matches how users prioritize work in semester planning.


### [Feature] List Not Done Tasks by Module (`list /notdone /mod MOD`)

#### Implementation

The List Not Done Tasks by Module feature provides a focused view of only the unfinished tasks for a specific module.
This reduces noise when a module has many completed tasks and makes it faster to find remaining work.

This feature extends the existing `list` command using a filter syntax.
It is implemented using the following operations:

* `Parser#parseList(String)` — Detects the presence of the `/notdone` and `/mod` flags and creates a `ListNotDoneCommand`.
* `ListNotDoneCommand#execute(ModuleBook, Storage, Ui)` — Delegates the display logic to the UI layer.
* `Ui#showNotDoneTaskList(ModuleBook, String)` — Filters tasks for the target module and prints only tasks that are not done.

An important design decision is that the not-done list keeps the same **global display indices** used by the main `list` command.
This ensures the user can follow up with commands such as `delete`, `mark`, and `unmark` using the index they see.

##### Parsing rules

The parser requires the not-done filter to be used together with a module code:

* Accepted forms: `list /notdone /mod CS2113` or `list /mod CS2113 /notdone`
* Rejected forms: `list /notdone` (missing module), `list /notdone /mod` (missing module code)

At the time of writing, the parser accepts this filter only when the `list` remainder has exactly three
whitespace-delimited tokens.

#### Sequence Diagram

The following sequence diagram illustrates the interactions when the user executes `list /notdone /mod CS2113`:

<img src="images/ListNotDoneSequenceDiagram.png" alt="Sequence diagram for list /notdone /mod" />

> **Note:** The diagram above must be generated from
> [`docs/diagrams/ListNotDoneSequenceDiagram.puml`](diagrams/ListNotDoneSequenceDiagram.puml)
> and saved as `docs/images/ListNotDoneSequenceDiagram.png`.

#### Class Diagram

The following class diagram shows the main classes involved in listing not-done tasks and how they collaborate:

<img src="images/ListNotDoneClassDiagram.png" alt="Class diagram for list /notdone /mod" />

> **Note:** The diagram above must be generated from
> [`docs/diagrams/ListNotDoneClassDiagram.puml`](diagrams/ListNotDoneClassDiagram.puml)
> and saved as `docs/images/ListNotDoneClassDiagram.png`.

#### Design Considerations

**Aspect: Where to implement the not-done filtering**

* **Alternative 1 (Current choice): Filter in `Ui#showNotDoneTaskList(...)`.**
  * Pros: Minimal changes to model classes; view-only feature that does not affect persistence.
  * Cons: UI becomes responsible for traversal/filtering logic; less reusable for other commands.

* Alternative 2: Filter in `ModuleBook`/`TaskList` and return a structured result.
  * Pros: Easier to reuse in future commands (e.g., `delete /notdone ...`).
  * Cons: Requires deciding how to preserve global indices, or introducing a new indexing scheme.

**Aspect: Indexing strategy for the not-done view**

* **Alternative 1 (Current choice): Preserve the global indices from `list`.**
  * Pros: Allows direct follow-up using `delete`, `mark`, and `unmark`.
  * Cons: Indices can appear sparse in a filtered view.

* Alternative 2: Renumber not-done tasks starting from 1.
  * Pros: The filtered list looks compact.
  * Cons: Would require separate commands or extra identifiers to refer to the original task.


### [Feature] Delete Task (`delete TASK_NUMBER`)

#### Implementation

The Delete Task feature removes a task from the application using the **1-based display index** shown by `list`.
After deletion, the change is persisted to disk and the UI confirms the removal.

The feature is implemented using the following operations:

* `Parser#parseDelete(String)` — Parses the user-supplied index and creates a `DeleteCommand`.
* `DeleteCommand#execute(ModuleBook, Storage, Ui)` — Removes the task, saves data, and prints a confirmation.
* `ModuleBook#removeTaskByDisplayIndex(int)` — Locates the task in global order across modules and removes it.
* `Storage#save(ModuleBook)` — Persists the updated `ModuleBook`.

Internally, the removal uses a global scan to convert a display index to a per-module index.
This keeps the CLI simple: the user only needs the number shown by `list`.

#### Sequence Diagram

The following sequence diagram illustrates the interactions when the user executes `delete 3`:

<img src="images/DeleteTaskSequenceDiagram.png" alt="Sequence diagram for the delete command" />

> **Note:** The diagram above must be generated from
> [`docs/diagrams/DeleteTaskSequenceDiagram.puml`](diagrams/DeleteTaskSequenceDiagram.puml)
> and saved as `docs/images/DeleteTaskSequenceDiagram.png`.

#### Class Diagram

The following class diagram shows the main classes involved in deletion and how they collaborate:

<img src="images/DeleteTaskClassDiagram.png" alt="Class diagram for the delete command" />

> **Note:** The diagram above must be generated from
> [`docs/diagrams/DeleteTaskClassDiagram.puml`](diagrams/DeleteTaskClassDiagram.puml)
> and saved as `docs/images/DeleteTaskClassDiagram.png`.

#### Design Considerations

**Aspect: Which index should `delete` use**

* **Alternative 1 (Current choice): Use the global display index shown by `list`.**
  * Pros: Easy to learn and consistent with `mark`/`unmark`.
  * Cons: Index depends on the current global ordering across modules.

* Alternative 2: Use a module-scoped index, e.g., `delete /mod CS2113 2`.
  * Pros: Indices remain stable within a module.
  * Cons: More complex CLI; user must provide both module and index.

**Aspect: Where to validate indices**

* **Alternative 1 (Current choice): Parse integer in `Parser`, validate existence in `ModuleBook`.**
  * Pros: Separation of concerns; model owns "does this task exist?".
  * Cons: Error messages may originate from different layers.

* Alternative 2: Validate everything inside `DeleteCommand#execute()`.
  * Pros: Delete-related logic concentrated in one place.
  * Cons: Commands start duplicating model checks.


### [Feature] List Registered Modules (`module list`)

#### Implementation

The List Registered Modules feature provides a quick overview of which modules the user is currently tracking.
In ModuleSync, modules are represented implicitly: a module exists once at least one task has been added under it.

This feature is implemented using the following operations:

* `Parser#parse(String)` — Recognises `module list` and creates a `ListModulesCommand`.
* `ListModulesCommand#execute(ModuleBook, Storage, Ui)` — Delegates display logic to the UI.
* `Ui#showModuleList(ModuleBook)` — Iterates the `ModuleBook` and prints each module code with its task count.

This command is view-only and does not modify any stored data.

#### Sequence Diagram

The following sequence diagram illustrates the interactions when the user executes `module list`:

<img src="images/ListModulesSequenceDiagram.png" alt="Sequence diagram for the module list command" />

> **Note:** The diagram above must be generated from
> [`docs/diagrams/ListModulesSequenceDiagram.puml`](diagrams/ListModulesSequenceDiagram.puml)
> and saved as `docs/images/ListModulesSequenceDiagram.png`.

#### Class Diagram

The following class diagram shows the main classes involved in listing modules and how they collaborate:

<img src="images/ListModulesClassDiagram.png" alt="Class diagram for the module list command" />

> **Note:** The diagram above must be generated from
> [`docs/diagrams/ListModulesClassDiagram.puml`](diagrams/ListModulesClassDiagram.puml)
> and saved as `docs/images/ListModulesClassDiagram.png`.

#### Design Considerations

**Aspect: What is considered a "registered module"**

* **Alternative 1 (Current choice): Modules are created lazily when tasks are added.**
  * Pros: No separate "register module" workflow; data model stays simple.
  * Cons: A module cannot exist without at least one task.

* Alternative 2: Introduce explicit module registration (e.g., `addmodule CS2113`).
  * Pros: Allows empty modules.
  * Cons: Adds a new command and persistence requirements for modules without tasks.


### [Feature] Semester Statistics (`semester stats`)

#### Implementation

The Semester Statistics feature provides a semester-wide summary across all tracked modules so that the user can
evaluate overall progress and workload distribution.

The current implementation treats the set of modules currently stored in the `ModuleBook` as the current semester.
Statistics are computed on-demand from in-memory data.

This feature is implemented using the following operations:

* `Parser#parse(String)` — Recognises `semester stats` and creates a `SemesterStatsCommand`.
* `SemesterStatsCommand#execute(ModuleBook, Storage, Ui)` — Delegates the computation and display to the UI.
* `Ui#showSemesterStatistics(ModuleBook)` — Aggregates per-task and per-module counts:
  total tasks, done tasks, task type counts (todo vs deadline), and optional weightage-based completion.

This command is view-only and does not modify any stored data.

#### Sequence Diagram

The following sequence diagram illustrates the interactions when the user executes `semester stats`:

<img src="images/SemesterStatsSequenceDiagram.png" alt="Sequence diagram for the semester stats command" />

> **Note:** The diagram above must be generated from
> [`docs/diagrams/SemesterStatsSequenceDiagram.puml`](diagrams/SemesterStatsSequenceDiagram.puml)
> and saved as `docs/images/SemesterStatsSequenceDiagram.png`.

#### Class Diagram

The following class diagram shows the main classes involved in computing semester statistics:

<img src="images/SemesterStatsClassDiagram.png" alt="Class diagram for the semester stats command" />

> **Note:** The diagram above must be generated from
> [`docs/diagrams/SemesterStatsClassDiagram.puml`](diagrams/SemesterStatsClassDiagram.puml)
> and saved as `docs/images/SemesterStatsClassDiagram.png`.

#### Design Considerations

**Aspect: Where to compute statistics**

* **Alternative 1 (Current choice): Compute in `Ui#showSemesterStatistics(...)`.**
  * Pros: Feature remains view-only; minimal model changes.
  * Cons: Aggregation logic lives in UI and is less reusable for future commands.

* Alternative 2: Compute in a dedicated statistics model/service (e.g., `SemesterStats` class).
  * Pros: Cleaner separation and easier to test/extend.
  * Cons: Adds extra classes/indirection for a small feature.

**Aspect: Weightage-based completion**

* **Alternative 1 (Current choice): Treat weightage as optional and compute completion only when present.**
  * Pros: Works for both weighted and unweighted tasks.
  * Cons: The weightage completion metric may be absent until the user assigns weightage.

//@@codefuul

### [Feature] CAP Calculator (`cap`)

#### Implementation

The CAP Calculator feature iterates through the `ModuleBook` to gather grades for all tracked modules. It maps the assigned letter grades (e.g., A+, B, C) to the standard 5.0 scale. To do this accurately, the `CapCommand` includes specific logic to filter out and ignore modules that are marked as CS (Completed Satisfactory) or CU (Completed Unsatisfactory), as well as any modules that are currently ungraded. It calculates both the semester CAP based on the current modules and the cumulative CAP.

#### Sequence Diagram

The following sequence diagram illustrates the interactions when the user executes `cap`:

<img src="images/CapSequenceDiagram.png" alt="Sequence diagram for the cap command" />

> **Note:** The diagram above must be generated from
> [`docs/diagrams/CapSequenceDiagram.puml`](diagrams/CapSequenceDiagram.puml)
> and saved as `docs/images/CapSequenceDiagram.png`.

#### Design Considerations

**Aspect: How to compute the CAP value**

* **Alternative 1 (Current choice): Calculate CAP on-the-fly during command execution.**
  * Pros: No storage overhead, and the calculated CAP is always strictly accurate based on the current state.
  * Cons: Slightly more computation needed at runtime when the command is called.

* **Alternative 2: Cache the CAP in a variable inside the `ModuleBook`.**
  * Pros: Faster retrieval for subsequent calls.
  * Cons: Requires complex state synchronization whenever a grade is added, modified, or removed, which could lead to bugs if the state goes out of sync.


### [Feature] Urgent Tasks Filter (`check /urgent`)

#### Implementation

The Urgent Tasks Filter iterates through the task list to identify tasks that need immediate attention. The `CheckUrgentCommand` utilizes Java's `LocalDateTime` to assess deadlines. It first filters out any completed tasks so that only pending work is evaluated. It then identifies incomplete tasks whose deadlines fall strictly within the next 48 hours relative to the current system time. Finally, these filtered tasks undergo a dynamic sort by urgency, ensuring that the tasks due the soonest are displayed first.

#### Sequence Diagram

The following sequence diagram illustrates the interactions when the user executes `check /urgent`:

<img src="images/CheckUrgentSequenceDiagram.png" alt="Sequence diagram for the check /urgent command" />

> **Note:** The diagram above must be generated from
> [`docs/diagrams/CheckUrgentSequenceDiagram.puml`](diagrams/CheckUrgentSequenceDiagram.puml)
> and saved as `docs/images/CheckUrgentSequenceDiagram.png`.

#### Design Considerations

**Aspect: When to evaluate the 48-hour urgency window**

* **Alternative 1 (Current choice): Evaluate the 48-hour window dynamically at execution time.**
  * Pros: Simple application state, reliable, and uses up-to-date system time exactly when requested.
  * Cons: Has to iterate over the task list at execution time.

* **Alternative 2: Run a background thread that constantly tags tasks as 'urgent'.**
  * Pros: The UI could immediately highlight urgent tasks without a dedicated command execution.
  * Cons: Significant overhead and complexity introduced by multi-threading. It is overkill for a simple CLI task tracker.


### [Feature] System Logging & Defensive Assertions

#### Implementation

The application integrates `java.util.logging.Logger` combined with a `FileHandler` to silently write execution flows and caught exceptions to a background `duke.log` file. This ensures that debugging details are captured without polluting the CLI UI directly.

Furthermore, Java `assert` statements have been added in critical areas, such as the `DeleteCommand`, to enforce internal invariants and assumptions before executing destructive actions. This defensive programming approach prevents unintended corruption of the `ModuleBook` or application state.

#### Sequence Diagram

The following sequence diagram illustrates the interactions involved when the system invokes defensive assertions and logging:

<img src="images/LoggingSequenceDiagram.png" alt="Sequence diagram for system logging and defensive assertions" />

> **Note:** The diagram above must be generated from
> [`docs/diagrams/LoggingSequenceDiagram.puml`](diagrams/LoggingSequenceDiagram.puml)
> and saved as `docs/images/LoggingSequenceDiagram.png`.

#### Design Considerations

**Aspect: Destination for system logs**

* **Alternative 1 (Current choice): Log to a background file (`duke.log`).**
  * Pros: Maintains a clean, distraction-free user experience in the terminal while preserving diagnostic data for troubleshooting.
  * Cons: Requires developers to check an external file to view logs.

* **Alternative 2: Log directly to the console/UI.**
  * Pros: Easier to implement, immediately visible during development.
  * Cons: Ruins the clean user experience by cluttering the terminal output with internal operational details.

### [Feature] Explicit Module Lifecycles (`add /mod` & `delete module /mod`)

#### Implementation

To safely facilitate the configuration of workload modules prior to assigning any explicit task deadlines or grades, the `ModuleBook` array is bound natively to `add /mod [MOD]` to implicitly drop an orphaned module string footprint into the tracker. It safely checks the existing arrays and uses `getOrCreate` to guarantee zero duplication conflicts. When `delete module /mod [MOD]` is called, the `deleteModule(String)` utility natively queries the linked Hash Map and severs the memory mapping entirely.

#### Design Considerations

**Aspect: How to parse the add command gracefully without task constraints?**

* **Alternative 1 (Current choice): Dynamically detect task bounds through Parser constraints.**
  * Pros: Reuses the existing `CMD_ADD` array loop syntax perfectly without creating overlapping duplicate commands. 
  * Cons: Adds a layer of complexity inside `parseAdd()`.

* **Alternative 2: Add a separate `moduleadd` command.**
  * Pros: Easier to parse.
  * Cons: Inconvenient to the end user who prefers standard UI verbs.

### [Feature] CAP Calculation Bounds Engine (`setcredits`)

#### Implementation

The `setcredits` bounding limit enforces absolute `0` to `40` Credit (MC) limits on a single module string, matching standard University models. Upon entering `0`, the logic natively circumvents `CapCommand` computational limits, officially recognizing it as a neutral module and ignoring it during CAP multiplication arrays. Finally, if the tracker detects `> 32` combined credits during a semester scope tracking execution, it natively fires a `Ui` warning flag through `showHighSemesterCreditsWarning`.

#### Design Considerations

**Aspect: How to calculate 0-Credit Modules?**

* **Alternative 1 (Current choice): Nullify multiplication outputs during `CapCommand` scans without crashing the tracker.**
  * Pros: Accurately renders non-credit classes explicitly.
  * Cons: Adds bounding filters internally.

---

## Product scope

### Target user profile

ModuleSync targets **NUS undergraduate students** who are simultaneously managing coursework across four to six modules, an internship search, and co-curricular commitments. More specifically, the target user:

* is **comfortable using a terminal** and prefers typed commands over graphical interfaces
* types faster than they click, and finds GUI task managers slow and distracting
* organises work **by module code** (CS2113, MA1521, etc.) as their primary mental model
* needs to track both simple to-do tasks and deadline tasks with exact due dates and times
* wants to record **percentage weightages** against assessments so priority is reflected in their task list
* wants to review **grades and CAP progress** across semesters without switching to a separate tool
* may need to **reference past semesters** without accidentally editing closed academic records

### Value proposition

ModuleSync solves the problem of context-switching overhead for students who currently spread task tracking across multiple tools — a notes app for todos, a calendar for deadlines, a spreadsheet for grades. ModuleSync consolidates all three into one keyboard-driven CLI:

* **One-shot task creation** — a single `add` command records a task under its module, optionally with a deadline and a weightage percentage. No multi-step wizard, no mouse required.
* **Module-centric organisation** — tasks are grouped by module code, mirroring how a student's semester actually works rather than imposing an arbitrary folder structure.
* **Deadline intelligence** — automatic overdue warnings on startup, same-day conflict detection (`check /conflicts`), and 48-hour urgency filtering (`check /urgent`) surface crunch periods before they arrive.
* **Grade and CAP tracking** — recording a grade against a module immediately feeds into semester and cumulative CAP calculations. No separate spreadsheet needed.
* **Multi-semester history** — past semesters are archived in read-only mode, letting students safely reference old tasks and grades without the risk of accidental edits.
* **Human-readable storage** — data files are plain UTF-8 text. A student can read, back up, or migrate their data without any proprietary tools.

---

## User Stories

| Version | As a … | I want to … | So that I can … |
|---------|--------|-------------|-----------------|
| v1.0 | new user | see a list of all available commands | learn how to use the application without reading external documentation |
| v1.0 | student | add a task under a module code | organise my work by module the way I already think about it |
| v1.0 | student | mark a task as done | keep track of what I have finished |
| v1.0 | student | unmark a task | correct a mistake or reopen work I thought was complete |
| v1.0 | student | delete a task | remove tasks that are no longer relevant |
| v1.0 | student | list all my tasks | get an overview of everything I still need to do |
| v2.0 | student | add a deadline to a task with an exact date and time | know precisely when each assessment is due |
| v2.0 | student | assign a weightage percentage to a task | see at a glance which assessments matter most to my grade |
| v2.0 | student | update the weightage of an existing task | correct a percentage I entered wrongly without deleting and recreating the task |
| v2.0 | student | update the deadline of an existing task | correct a date I entered wrongly without deleting and recreating the task |
| v2.0 | student | see only unfinished tasks for a specific module | focus on remaining work without wading through completed items |
| v2.0 | student | view all deadlines sorted by due date | plan the next few weeks at a glance |
| v2.0 | student | view my highest-priority tasks by priority score | focus on what is most urgent and most heavily weighted |
| v2.0 | student | be warned about overdue tasks when I open the app | react immediately to anything I have missed |
| v2.0 | student | check which days have multiple deadlines falling at the same time | plan ahead and avoid last-minute crunch |
| v2.0 | student | check tasks due within the next 48 hours | focus on what is most immediately urgent |
| v2.0 | student | view per-module completion statistics including on-time and late rates | reflect on my work habits for a specific module |
| v2.0 | student | view a semester-wide task summary | understand my overall workload and progress at a glance |
| v2.0 | student | record a final grade for a module | keep all academic records in one place |
| v2.0 | student | view my current semester CAP and cumulative CAP | track my academic standing without opening a separate spreadsheet |
| v2.0 | student | view my full grade history across all semesters | understand how my performance has evolved over time |
| v2.0 | student | archive a module so it disappears from my main task list | reduce clutter once a module is fully completed |
| v2.0 | student | restore an archived module | access its tasks again if I archived it by mistake |
| v2.0 | student | create a new semester and switch to it | start a fresh task list without losing previous data |
| v2.0 | student | switch to a past semester in read-only mode | safely reference old tasks and grades without risking accidental edits |
| v2.0 | student | archive my current semester | transition to a new term while finalizing my academic records |
| v2.0 | student | see which semester I am currently working in on startup | immediately know whether I am in the right context |

## Non-Functional Requirements

1. **Java version** — ModuleSync requires Java 17 or above. It must run on any operating system that provides a compatible JVM (Windows, macOS, Linux).
2. **No network dependency** — ModuleSync is a fully offline, single-user application. It must function without any internet connection.
3. **Response time** — All commands must respond within one second on a machine with at least 4 GB of RAM and a modern dual-core processor, for a dataset of up to 500 tasks across 10 semesters.
4. **Data durability** — Every mutating command saves immediately to the relevant semester file before returning control to the user. A crash after a successful save must not cause data loss for that command.
5. **Human-readable storage** — Data files must be plain UTF-8 text that a user can open and understand in any text editor without special tooling.
6. **Single-user** — ModuleSync is designed for use by one person on one machine. Concurrent access to the same data directory by multiple processes is not supported and not required.
7. **No GUI dependency** — The application must operate entirely through a terminal. No graphical display system (e.g. a desktop environment) is required.
8. **Portability** — Transferring the `data/` folder to another machine running the same Java version must fully restore all semesters, tasks, and grades without any conversion step.

---

## Glossary

| Term | Definition |
|------|-----------|
| **Module** | An academic course identified by a module code (e.g. `CS2113`). In ModuleSync, a module is created automatically the first time a task is added under its code. |
| **Task** | A unit of work belonging to a module. A task is either a **Todo** (no due date) or a **Deadline** (has a due date and time). |
| **Weightage** | An integer from `0` to `100` representing a task's percentage contribution to the module's overall grade. Weightage is optional — tasks without it are still fully functional. |
| **Semester** | A named academic period (e.g. `AY2526-S2`) that groups a set of modules and their tasks. Each semester is stored in its own file under `data/`. |
| **Active semester** | The semester the user is currently working in. All mutating commands (`add`, `delete`, `mark`, etc.) operate on the active semester's `ModuleBook`. |
| **Archived semester** | A semester that has been closed and marked read-only (via `semester archive`). View commands (`list`, `cap`, `grades list`, `semester stats`) still work; mutating commands are rejected. It can be made editable again via `semester unarchive`. |
| **Archived module** | A module within the active semester that has been hidden from the main `list` and `list /deadlines` views. It remains visible in `module list` and can be restored with `module unarchive`. |
| **Display index** | The 1-based integer shown next to each task by the `list` command. Used as the identifier for `mark`, `unmark`, `delete`, `setweight`, and `setdeadline`. |
| **CAP** | Cumulative Average Point — NUS's GPA metric on a 5.0 scale. Only CAP-bearing grades (`A+`, `A`, `A-`, `B+`, etc.) contribute. Grades such as `S`, `U`, `CS`, and `CU` are excluded. |
| **MCs** | Modular Credits — the credit-unit weight of a module. Used as the denominator when computing weighted CAP averages. |
| **Priority score** | A numeric value computed from a task's weightage (and deadline proximity for `Deadline` tasks) used to rank tasks in `list /top`. |
| **`ModuleBook`** | The in-memory data structure that holds all `Module` objects for one semester. Each `Semester` owns exactly one `ModuleBook`. |
| **`SemesterBook`** | The in-memory registry of all `Semester` objects. Maintains the pointer to the currently active semester. |
| **`Command` (abstract)** | The base class for all executable user actions. `Parser` creates a concrete subclass; `ModuleSync` calls its `execute()` method. |
| **`SemesterCommand`** | A subclass of `Command` for semester-lifecycle operations (switch, archive, list). These bypass the read-only guard because they operate at the semester level, not on tasks. |
| **Read-only guard** | The check in `ModuleSync.run()` that rejects any command where `isMutating()` returns `true` if the current semester is archived. |

---

## Instructions for manual testing

> **Note:** These instructions assume you are running the application from the project root using `./gradlew run` or `java -jar build/libs/modulesync.jar`. All commands are typed at the `>` prompt.

### 1. Launch and first-run setup

1. Delete the `data/` folder if it exists, to start from a clean state.
2. Launch the application. You should see the welcome message and a prompt indicating no active semester.
3. Create and switch to a new semester:
   ```
   semester new AY2526-S2
   ```
   Expected: `Created and switched to new semester: AY2526-S2`

---

### 2. Adding tasks

```
add /mod CS2113 /task Week 10 Quiz
add /mod CS2113 /task Final Project /w 30
add /mod CS2113 /task Submit iP /due 2026-04-15 /w 10
add /mod CS2113 /task Project checkpoint /due 2026-04-15-0900
add /mod MA1521 /task Problem Set 4 /w 20
add /mod MA1521 /task Final Exam /due 2026-05-01-1300 /w 40
```

Expected after each `add`: confirmation showing the task description, module code, and weightage (if provided).

---

### 3. Listing tasks

```
list
list /mod CS2113
list /deadlines
list /top 3
list /notdone /mod CS2113
module list
```

Expected:
- `list` shows all tasks numbered from 1 with module codes, type (`T`/`D`), done status, and weightage where set.
- `list /deadlines` shows only deadline tasks with upcoming/due-today entries first,
  and overdue entries grouped at the end.
- `list /top 3` shows the three tasks with the highest priority scores.
- `list /notdone /mod CS2113` shows only incomplete CS2113 tasks, using **the same global indices as `list`**.
- `module list` shows `CS2113 (4 task(s))` and `MA1521 (2 task(s))`.

---

### 4. Marking, unmarking, and deleting

```
mark 1
list
unmark 1
list
delete 6
list
```

Expected:
- After `mark 1`: task 1 shows `[X]`.
- After `unmark 1`: task 1 shows `[ ]` again.
- After `delete 6`: task count decreases by one; subsequent tasks renumber.

---

### 5. Weightage and deadline updates

```
setweight 1 15
list /mod CS2113
editweight 1 /w 20
setdeadline 1 /by 2026-04-20
editdeadline 1 /by 2026-04-20-2359
list /mod CS2113
```

Expected: task 1 shows the updated weightage and deadline after each command.

---

### 6. Conflict and urgency checks

Set the system clock or use already-close deadlines and run:

```
check /conflicts
check /urgent
```

Expected:
- `check /conflicts` lists any calendar days where two or more deadline tasks fall on the same date (e.g. April 15 has two tasks in the sample above).
- `check /urgent` lists incomplete deadline tasks due within 48 hours of the current time.

---

### 7. Per-module and semester statistics

```
stats /mod CS2113
semester stats
```

Expected:
- `stats /mod CS2113` shows total tasks, on-time/late/active counts and percentages, and average days before deadline. Average completion time shows `N/A` until at least one deadline task has been marked done.
- `semester stats` shows module count, overall task counts and completion percentage, type breakdown (todo vs deadline), and weightage-based completion summary if any tasks have weightage set.

---

### 8. Grades and CAP

```
grade /mod CS2113 /grade A+
grade /mod MA1521 /grade B+
cap
grades list
```

Expected:
- `cap` shows a semester CAP calculated from the two grades and their credits (default 0 MCs until set — CAP will show N/A until credits are non-zero; set credits via the grade command if implemented, or note this limitation).
- `grades list` shows both modules tabulated with their grade points.

---

### 9. Module archiving

```
module archive /mod MA1521
list
module list
module unarchive /mod MA1521
list
```

Expected:
- After archiving: `list` no longer shows MA1521 tasks. `module list` still shows it marked `[archived]`.
- After unarchiving: MA1521 tasks reappear in `list`.

---

### 10. Multi-semester workflow

```
semester new AY2527-S1
add /mod CS3230 /task Assignment 1
list
semester switch AY2526-S2
list
```

Expected:
- After switching to `AY2527-S1`: only CS3230 tasks are shown.
- After switching back to `AY2526-S2`: only the original semester's tasks are shown.

---

### 11. Read-only guard (archived semester)

While in `AY2526-S2`, archive it and immediately attempt a mutation:

```
semester archive
add /mod CS2113 /task Should Fail
```

Expected: the `add` command is rejected with a message indicating the semester is archived and read-only.

To restore the semester for further testing:

```
semester unarchive
```

---

### 12. Persistence across restarts

1. Add at least one task, mark it done, and set its weightage.
2. Exit with `bye`.
3. Relaunch the application.

Expected: all tasks, their done status, and their weightage are restored exactly as they were before exit.

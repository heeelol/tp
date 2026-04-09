# Developer Guide

## Acknowledgements

* [CS2113 project template (AddressBook-Level 3)](https://github.com/se-edu/addressbook-level3) — project structure and Gradle build configuration
* [JUnit 5](https://junit.org/junit5/) — unit and integration testing framework
* [PlantUML](https://plantuml.com/) — UML diagram generation

---

## Design

### Architecture

ModuleSync is structured as a layered architecture with five major components. Each layer depends only on layers below it — no inner component imports from `Ui` or `Parser`.

```
         ┌─────────────────────────────────────────┐
         │               User (CLI)                │
         └──────────────────┬──────────────────────┘
                            │ stdin / stdout
         ┌──────────────────▼──────────────────────┐
         │                  Ui                     │
         │  Reads all input. Prints all output.    │
         └──────────────────┬──────────────────────┘
                            │ raw String
         ┌──────────────────▼──────────────────────┐
         │                Parser                   │
         │  Converts raw strings into Commands.    │
         └──────────────────┬──────────────────────┘
                            │ Command object
         ┌──────────────────▼──────────────────────┐
         │               ModuleSync                │
         │  Main run loop. Dispatches commands.    │
         │  Enforces read-only guard.              │
         └──────┬───────────┬──────────────┬───────┘
                │           │              │
   ┌────────────▼──┐  ┌─────▼──────┐  ┌───▼────────────┐
   │  Data Model   │  │  Storage   │  │  SemesterBook  │
   │  SemesterBook │  │  Storage   │  │  Semester      │
   │  Semester     │  │  Semester  │  │                │
   │  ModuleBook   │  │  Storage   │  │                │
   │  Module       │  │            │  │                │
   │  TaskList     │  │            │  │                │
   │  Task         │  │            │  │                │
   └───────────────┘  └────────────┘  └────────────────┘
```

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
sorted chronologically from earliest to latest. This helps users plan their week by seeing
upcoming deadlines at a glance. The feature filters out to-do tasks without deadlines and
presents the filtered list in priority order.

The feature implements the following operations:

* `Parser#parseList(String)` — Parses the `list` command and checks for optional filters. When
  `/deadlines` is detected, it returns a `ListDeadlinesCommand` instead of the regular `ListCommand`.
* `ListDeadlinesCommand#execute(ModuleBook, Storage, Ui)` — Executes the deadline listing by
  calling `Ui#showDeadlineList()`.
* `Ui#showDeadlineList(ModuleBook)` — Collects all `Deadline` objects from all modules, sorts them
  by due date in ascending order (earliest first), and displays them in a user-friendly format.

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

**Step 8.** The collected deadlines are sorted by their `LocalDateTime by` field in ascending order
(earliest deadline first).

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

* **Alternative 1 (Current choice): Sort by due date in ascending order (earliest first).**
    * Pros: Users see the most urgent deadlines first, aiding prioritization.
    * Cons: Does not highlight deadlines by urgency category (e.g., overdue vs. due soon vs. due later).

* **Alternative 2: Sort by days remaining with urgency grouping (overdue, due this week, etc.).**
    * Pros: Provides visual urgency categorization.
    * Cons: Adds complexity to sorting logic and UI formatting.

We chose ascending date order for simplicity and intuitive urgency ranking.


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


### [Feature] List Registered Modules (`modules`)

#### Implementation

The List Registered Modules feature provides a quick overview of which modules the user is currently tracking.
In ModuleSync, modules are represented implicitly: a module exists once at least one task has been added under it.

This feature is implemented using the following operations:

* `Parser#parse(String)` — Recognises the `modules` keyword and creates a `ListModulesCommand`.
* `ListModulesCommand#execute(ModuleBook, Storage, Ui)` — Delegates display logic to the UI.
* `Ui#showModuleList(ModuleBook)` — Iterates the `ModuleBook` and prints each module code with its task count.

This command is view-only and does not modify any stored data.

#### Sequence Diagram

The following sequence diagram illustrates the interactions when the user executes `modules`:

<img src="images/ListModulesSequenceDiagram.png" alt="Sequence diagram for the modules command" />

> **Note:** The diagram above must be generated from
> [`docs/diagrams/ListModulesSequenceDiagram.puml`](diagrams/ListModulesSequenceDiagram.puml)
> and saved as `docs/images/ListModulesSequenceDiagram.png`.

#### Class Diagram

The following class diagram shows the main classes involved in listing modules and how they collaborate:

<img src="images/ListModulesClassDiagram.png" alt="Class diagram for the modules command" />

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


### [Feature] Semester Statistics (`semesterstats`)

#### Implementation

The Semester Statistics feature provides a semester-wide summary across all tracked modules so that the user can
evaluate overall progress and workload distribution.

The current implementation treats the set of modules currently stored in the `ModuleBook` as the current semester.
Statistics are computed on-demand from in-memory data.

This feature is implemented using the following operations:

* `Parser#parse(String)` — Recognises the `semesterstats` keyword and creates a `SemesterStatsCommand`.
* `SemesterStatsCommand#execute(ModuleBook, Storage, Ui)` — Delegates the computation and display to the UI.
* `Ui#showSemesterStatistics(ModuleBook)` — Aggregates per-task and per-module counts:
  total tasks, done tasks, task type counts (todo vs deadline), and optional weightage-based completion.

This command is view-only and does not modify any stored data.

#### Sequence Diagram

The following sequence diagram illustrates the interactions when the user executes `semesterstats`:

<img src="images/SemesterStatsSequenceDiagram.png" alt="Sequence diagram for the semesterstats command" />

> **Note:** The diagram above must be generated from
> [`docs/diagrams/SemesterStatsSequenceDiagram.puml`](diagrams/SemesterStatsSequenceDiagram.puml)
> and saved as `docs/images/SemesterStatsSequenceDiagram.png`.

#### Class Diagram

The following class diagram shows the main classes involved in computing semester statistics:

<img src="images/SemesterStatsClassDiagram.png" alt="Class diagram for the semesterstats command" />

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

---

## Product scope
### Target user profile

{Describe the target user profile}

### Value proposition

{Describe the value proposition: what problem does it solve?}

## User Stories

|Version| As a ... | I want to ... | So that I can ...|
|--------|----------|---------------|------------------|
|v1.0|new user|see usage instructions|refer to them when I forget how to use the application|
|v2.0|user|find a to-do item by name|locate a to-do without having to go through the entire list|

## Non-Functional Requirements

{Give non-functional requirements}

## Glossary

* *glossary item* - Definition

## Instructions for manual testing

{Give instructions on how to do a manual product testing e.g., how to load sample data to be used for testing}

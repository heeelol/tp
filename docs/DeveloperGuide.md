# Developer Guide

## Acknowledgements

{list here sources of all reused/adapted ideas, code, documentation, and third-party libraries -- include links to the original source as well}

## Design & implementation

//@@author Huang-Hau-Shuan
### [Feature] Task Weightage (`add ... [/w PERCENT]`)

#### Implementation

The Task Weightage feature allows students to assign a percentage value to a task, helping them
prioritize assignments based on their contribution to the overall module grade. Since not all tasks
have weightage, this feature supports both weighted and unweighted task creation in the existing
`add` command flow and the core `Task` model.

The feature implements the following operations:

* `Parser#parse(String)` — Parses the user input to identify the `add` command and extracts the
  optional `/w` argument when present, converting it to a validated integer.
* `AddTodoCommand#execute(ModuleBook, Storage, Ui)` — Executes the creation of a new task. It
  creates a `Todo` with optional weightage and adds it to the target module's `TaskList`.
* `Task#setWeightage(int)` and `Task#getWeightage()` — Methods inside the core `Task` model to
  store and retrieve the weightage value when it is provided.

Given below are example usage scenarios and how the task weightage mechanism behaves.

**Step 1.** The user inputs either:
- `add /mod CS2113 /task Final Project /w 25` (weightage provided), or
- `add /mod CS2113 /task Final Project` (weightage omitted).

**Step 2.** `ModuleSync#run()` calls `Ui#readCommand()`, which reads the raw input string from stdin
and returns it.

**Step 3.** `ModuleSync#run()` passes the raw string to `Parser#parse(...)`. The parser extracts the
module code and task description. If `/w` is present, it parses and validates the integer range
(`0` to `100`) via the `parseWeightage` helper. If `/w` is absent, weightage is recorded as `null`.

**Step 4.** The `Parser` instantiates a new `AddTodoCommand` with the module code, description, and
optional weightage, then returns it to `ModuleSync#run()`.

**Step 5.** `ModuleSync#run()` calls `AddTodoCommand#execute(moduleBook, storage, ui)`.

**Step 6.** Inside `execute()`, the command retrieves the target `Module` from the `ModuleBook` by
calling `ModuleBook#getOrCreate("CS2113")`.

**Step 7.** The command calls `Module#addTodo(description, weightage)`, which delegates to
`TaskList#addTodo(moduleCode, description, weightage)`. The `TaskList` creates the `Todo` object
and calls `Todo#setWeightage(int)` only when a weightage value is present.

**Step 8.** `Storage#save(ModuleBook)` is called to persist the change to disk.

**Step 9.** Finally, `Ui#showTaskAdded(module, task, totalCount)` is called to display the newly
added task. Weightage is shown only when provided.

#### Sequence Diagram

The following sequence diagram illustrates the interactions between components when the user
executes `add /mod CS2113 /task Final Project [/w 25]`:

<img src="images/AddWeightageSequenceDiagram.png" alt="Sequence diagram for the add weightage command" />

> **Note:** The diagram above must be generated from
> [`docs/diagrams/AddWeightageSequenceDiagram.puml`](diagrams/AddWeightageSequenceDiagram.puml)
> and saved as `docs/images/AddWeightageSequenceDiagram.png`.

#### Design Considerations

**Aspect: How to store the weightage value**

* **Alternative 1 (Current choice): Store weightage as an `int` (0-100).**
    * Pros: Simple to parse, validate, and store. Eliminates floating-point precision issues when
      summing weightages across a module.
    * Cons: Does not support fractional weightages (e.g., `12.5%`), which some university modules
      use for smaller assessments.

* **Alternative 2: Store weightage as a `double`.**
    * Pros: Supports precise fractional weightages.
    * Cons: Adds complexity in input parsing and UI formatting to avoid displaying unwanted trailing
      zeros. Rounding errors may arise when summing many fractional values.

We chose `int` for v2.0 to maintain a streamlined, predictable CLI experience while keeping
validation straightforward.

**Aspect: Where to enforce the 0-100 validation**

* **Alternative 1 (Current choice): Validate in `Parser` when `/w` is present.**
    * Pros: Fails fast at the logic boundary; command objects are created with either a valid
      percentage or no percentage.
    * Cons: Validation rules must remain synchronized with command/model expectations.

* **Alternative 2: Validate inside `AddTodoCommand#execute()`.**
    * Pros: Validation lives close to where the value is used.
    * Cons: The `Command` layer would then need to handle user-facing error messages, blurring the
      separation of concerns between parsing and execution.

//@@author heeelol
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


//@@author Notchennie1
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


//@@author Notchennie1
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
  * Pros: Separation of concerns; model owns “does this task exist?”.
  * Cons: Error messages may originate from different layers.

* Alternative 2: Validate everything inside `DeleteCommand#execute()`.
  * Pros: Delete-related logic concentrated in one place.
  * Cons: Commands start duplicating model checks.


//@@author Notchennie1
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

**Aspect: What is considered a “registered module”**

* **Alternative 1 (Current choice): Modules are created lazily when tasks are added.**
  * Pros: No separate “register module” workflow; data model stays simple.
  * Cons: A module cannot exist without at least one task.

* Alternative 2: Introduce explicit module registration (e.g., `addmodule CS2113`).
  * Pros: Allows empty modules.
  * Cons: Adds a new command and persistence requirements for modules without tasks.


//@@author Notchennie1
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

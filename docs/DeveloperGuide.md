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

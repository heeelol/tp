# Huang Hau Shuan - Project Portfolio Page

## Project: ModuleSync

ModuleSync is a desktop CLI-based task manager designed to help university students achieve a
balanced academic life through structured organisation of their module-related tasks. The user
interacts with it using a Command Line Interface (CLI), and all data is stored locally in a
human-editable text file. It is written in Java 17, and has about 4 kLoC.

Given below are my contributions to the project.

## New Features

### Feature 1: Core OOP Architecture (Parser, Storage, Task, UI, Command, Exception, Module)
**What it does:** Established the foundational class structure of the entire application using
Object-Oriented Programming principles. This includes the abstract `Task` model, the `Command`
abstraction, the `Parser`, `Storage`, `Ui`, `ModuleBook`, `Module`, `TaskList`, and
`ModuleSyncException` classes.

**Justification:** Without a clean, well-designed base architecture, the team would have no
consistent framework to build features on. By defining clear responsibilities for each class (e.g.,
`Parser` only parses, `Command` only executes, `Storage` only persists), the codebase remains
maintainable and extensible as the project grows.

**Highlights:** The `Command` abstraction allows every feature to be implemented as a self-contained
class with a single `execute(ModuleBook, Storage, Ui)` method, making it easy for teammates to add
new commands without touching unrelated code. The `Task` abstract class uses an optional
`weightage` field (`Integer`, nullable) so that weighted and unweighted tasks coexist naturally
within the same model.

### Feature 2: Add Task (`add /mod MOD /task DESCRIPTION [/due DATE] [/w PERCENT]`)
**What it does:** Allows users to add a new task to a module. Supports both plain to-do tasks and
deadline tasks (via the optional `/due` flag), and optionally accepts a weightage percentage via
the `/w` flag.

**Justification:** Adding tasks is the core action of any task manager. Making the `/due` and `/w`
flags optional means the command is simple for everyday use but powerful enough to capture full
task metadata when the user needs it.

**Highlights:** The parser uses slash-delimited token extraction (via a reusable
`extractFieldFromTokens` helper) to cleanly separate the module code, task description, due date,
and weightage without relying on positional arguments. The weightage is validated against a 0–100
range using a dedicated `parseWeightage` helper and named constants (`MIN_WEIGHTAGE`,
`MAX_WEIGHTAGE`) to avoid magic numbers throughout the codebase.

### Feature 3: Task Weightage — inline during `add` (`/w PERCENT`)
**What it does:** Extends the `add` command to optionally accept a `/w PERCENT` argument, assigning
a percentage weightage to a task at creation time. The weightage is displayed alongside the task
in all list views (e.g., `[25%]`).

**Justification:** Not all tasks carry equal academic weight. Allowing users to annotate tasks with
their contribution to the module grade helps them prioritise high-impact work at a glance, without
requiring a separate follow-up command.

**Highlights:** Weightage is stored as a nullable `Integer` in the `Task` model, so the absence of
`/w` is naturally represented as `null` rather than a sentinel value. The `formatForList` method
on `Task` appends the `[NN%]` label only when a weightage is present, keeping the output clean for
unweighted tasks.

### Feature 4: Set Weightage After Creation (`setweight TASK_NUMBER PERCENT`)
**What it does:** Allows users to assign or update the weightage of an existing task using its
global display index (the same number shown by `list` and `list /mod`). If a weightage was already
set, the command overwrites it and informs the user of the previous value.

**Justification:** Users may not know the exact weightage of a task when they first add it, or they
may discover a weightage later (e.g., after the module coordinator publishes the grading breakdown).
This command provides a focused, non-destructive way to update that information without deleting
and re-adding the task.

**Highlights:** The implementation reuses `ModuleBook#getTaskByDisplayIndex(int)` — the same global
lookup method used by `mark`, `unmark`, and `delete` — keeping the indexing convention consistent
across all commands. The UI message distinguishes between setting a fresh weightage and overwriting
an existing one, providing clear feedback in both cases.

## Code Contributed
[RepoSense link](https://nus-cs2113-ay2526-s2.github.io/tp-dashboard/?search=Huang-Hau-Shuan&breakdown=true&sort=groupTitle%20dsc&sortWithin=title&since=2026-02-20T00%3A00%3A00&timeframe=commit&mergegroup=&groupSelect=groupByRepos&checkedFileTypes=docs~functional-code~test-code~other)

## Project Management

- **Architecture Lead:** Designed and implemented the core OOP structure that the entire team builds on
- **Coding Standards:** Enforced naming conventions, Javadoc comments, no magic numbers, and SLAP
  refactoring across the codebase

## Enhancements to Existing Features

- Refactored `Parser` to use named constants for all command keywords, prefix strings, and length
  values, eliminating magic numbers throughout the parsing logic
- Added `java.util.logging` integration to `AddTodoCommand` and `SetWeightCommand`, replacing
  diagnostic `System.out.println` calls with proper `Logger.fine()` entries
- Added Java `assert` statements across `Task`, `TaskList`, `Module`, `ModuleBook`, `AddTodoCommand`,
  and `SetWeightCommand` to document and enforce internal invariants

## Documentation

### User Guide
- Added documentation for the `add` command (both to-do and deadline variants, with optional `/w`)
- Added documentation for the `setweight` command including a usage tip on the intended workflow
  (`list /mod` → see index → `setweight`)
- Updated the Command Summary table to include all `add` and `setweight` variants

### Developer Guide
- Wrote the **Task Weightage** design and implementation section, including a step-by-step scenario,
  sequence diagram (`AddWeightageSequenceDiagram.puml`), and design considerations (int vs double)
- Documented the `setweight` post-creation flow as a follow-on to the Task Weightage feature

## Community

- **PRs Reviewed:** Provided feedback on teammates' feature implementations and Javadoc coverage
- **Architecture Guidance:** Assisted teammates in understanding the `Command`/`Parser`/`ModuleBook`
  interaction model to keep feature implementations consistent

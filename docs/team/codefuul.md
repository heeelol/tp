# Leonard - Project Portfolio Page

## Project: ModuleSync

ModuleSync is a desktop CLI-based task manager designed to help university students achieve a
balanced academic life through structured organisation of their module-related tasks. The user
interacts with it using a Command Line Interface (CLI), and all data is stored locally in a
human-editable text file. It is written in Java 17, and has about 4 kLoC.

Given below are my contributions to the project.

## New Features

### Feature 1: Check Urgent Tasks (`check /urgent`)
**What it does:** Iterates through the task list, filters for incomplete tasks that are due within the next 48 hours, and sorts them dynamically by urgency.

**Justification:** Students often have multiple impending deadlines. This feature provides a quick, focused view of tasks that require immediate attention, helping users triage their workload effectively.

**Highlights:** The dynamic sorting mechanism ensures that the most time-critical tasks always appear at the top of the list, regardless of the order they were added or their associated modules.

### Feature 2: Update Task Deadline (`setdeadline [index] /by [date]`)
**What it does:** Allows users to update the deadline of an existing task on the fly by specifying its index and the new date.

**Justification:** Deadlines frequently change in university courses. This command provides a seamless way to modify a task's due date without the friction and inefficiency of having to delete and recreate the task entirely.

**Highlights:** The implementation flawlessly locates the task and updates its metadata while maintaining the task's existing state, such as its completion status and description.

### Feature 3: Delete Task (`delete`)
**What it does:** Removes a specific task from the user's task manager.

**Justification:** As tasks become obsolete or are entered by mistake, users need a straightforward way to remove them from their records to keep their active task list uncluttered.

**Highlights:** I implemented the core logic to safely identify and remove the task across the application state, complete with rigorous unit testing to ensure the deletion process is robust and handles boundary cases gracefully.

### Feature 4: Explicit Module Tracking (`add /mod [MOD]` & `delete module /mod [MOD]`)
**What it does:** Grants users explicit CRUD capabilities to instantly instantiate blank modules or completely drop them from memory without attaching tasks or grades.

**Justification:** Allows safely generating bare modules to attach workload credits to early in the semester, and facilitates cleanly severing an entire module's local storage footprint dynamically if a user drops a class mid-semester.

**Highlights:** Integrated an absolute separation of bounds inside the `Parser` tree to intercept these paths dynamically, complete with heavily automated execution-mechanic tests via JUnit mapping.

### Feature 5: Stabilized Credits Engine (`setcredits bounds`)
**What it does:** Upgraded `setcredits` bounding systems to officially recognize `0` credits for non-credit bearing modules (without crashing CAP formulas), blocked workloads `> 40`, and triggered aggregated warning flags if a student hits `> 32` modules across their semester workload.

**Justification:** Perfectly matches real university registration constraints while acting as an invisible safety bumper protecting students from catastrophic mathematical typos in their workload planning.

## Code Contributed
[RepoSense link](#) 

## Project Management

- [Add any project management contributions here]

## Enhancements to Existing Features

- **Defensive Programming:** Added defensive `assert` statements to `DeleteCommand` to enforce internal invariants and ensure the system state remains consistent. I also enabled these assertions in the Gradle build process to verify them during development and testing.
- **Logging Integration:** Integrated Java's `java.util.logging.Logger` with a `FileHandler` to track system execution and errors. This silently records execution details in a background `modulesync.log` file without polluting the user's active CLI interface.

## Documentation

### User Guide
- Added comprehensive documentation for the `check /urgent` command, detailing its format, expected output, and providing an illustrative example of the dynamic sorting.
- Added documentation for the `setdeadline` command, explaining its syntax and usage scenarios for modifying existing tasks without deletion.
- Updated the command summary table to include both `check /urgent` and `setdeadline`.

### Developer Guide
- Authored the implementation details for the `check /urgent` and `setdeadline` commands, including the sequence of interactions between the `Parser`, `Command`, and `TaskList` components.
- Documented the system-wide logging strategy using `java.util.logging.Logger` and the usage of the `modulesync.log` file for debugging.
- Drafted the API usage documentation regarding Explicit Module Creation and Bounding Credit Lifecycles inside the Developer Guide implementation blocks.

### About Us
- Updated the **About Us** page to include my developer profile, illustrating my role and linking to my GitHub profile for better team transparency.

## Community

- **PRs Reviewed:** Actively reviewed pull requests from teammates, ensuring adherence to our 120-character line limit and providing constructive feedback on code quality.
- **Bug Triaging:** Helped identify and resolve edge cases across team members' features, particularly related to index parsing and array bounds.

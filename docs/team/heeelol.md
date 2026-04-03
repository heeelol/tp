# Yap Jia Wei - Project Portfolio Page

## Project: ModuleSync

ModuleSync is a desktop CLI-based task manager designed to help university students achieve a balanced academic life through structured organisation of their module-related tasks. The user interacts with it using a Command Line Interface (CLI), and all data is stored locally in a human-editable text file. It is written in Java 17, and has about 4 kLoC.

Given below are my contributions to the project.

## New Features

### Feature 1: Mark and Unmark Commands
**What it does:** Allows users to toggle the completion status of tasks. The `mark` command marks a task as done, while the `unmark` command marks a task as incomplete, enabling users to update their progress dynamically.

**Justification:** This feature is essential for a task manager as it allows students to track their progress on assignments and deadlines. Users can mark tasks as complete without deleting them, providing a clear visual representation of their workload.

**Highlights:** The implementation required careful management of task indices and state persistence to ensure data integrity across sessions. The feature integrates seamlessly with the data storage system, automatically saving changes to the `modules.txt` file.

### Feature 2: List Upcoming Deadlines
**What it does:** Adds a `list /deadlines` command that displays only tasks with associated deadlines, sorted chronologically by due date. This provides users with a focused view of their upcoming submissions.

**Justification:** With multiple modules and diverse deadlines, students need a quick way to see what's coming up without being overwhelmed by the full task list. This feature directly addresses the "priority paralysis" pain point by presenting urgent items first.

**Highlights:** The implementation involved designing an efficient filtering and sorting mechanism that works across all modules. The feature complements existing listing commands (`list`, `list /mod`, `list /notdone`) by providing another dimension for task organization.

## Code Contributed
[RepoSense link](https://nus-cs2113-ay2526-s2.github.io/tp-dashboard/?search=heeelol&breakdown=true&sort=groupTitle%20dsc&sortWithin=title&since=2026-02-20T00%3A00%3A00&timeframe=commit&mergegroup=&groupSelect=groupByRepos&checkedFileTypes=docs~functional-code~test-code~other&filteredFileName=)

## Project Management

- **Testing Lead:** Set up testing infrastructure including test scripts and automated test execution
- **Issue & Milestone Management:** Coordinated setup of GitHub issues and milestones to track project progress

## Enhancements to Existing Features

- Improved task state management to support the mark/unmark workflow
- Enhanced the list command with better filtering and sorting capabilities for deadline-based views

## Documentation

### User Guide
- Added documentation for the `mark` and `unmark` commands
- Added documentation for the `list /deadlines` command variant
- Completed the FAQ section to address common user questions about data management and task status

### Developer Guide
- Documented the implementation approach for task state management
- Added sequence diagrams illustrating the mark/unmark workflow

## Community

- **PRs Reviewed:** [#4](link), [#34](link) - provided feedback on feature implementations and code quality
- **Project Setup:** Configured GitHub issues and milestones to facilitate team coordination and iteration planning

## Tools & Infrastructure

- Set up automated testing infrastructure to support continuous validation of features
- Configured test execution pipelines for reliable testing during development

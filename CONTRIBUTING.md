# Contributing to opsi-configed

Thank you for your interest in contributing to the Configed project! We
appreciate your contributions to help improve the project. To ensure a
smooth collaboration, please follow these guidelines when contributing.

## Commit Message Convention

We follow a prefix-based commit message convention to provide clarity
and consistency in our commit messages. The convention is as follows:

```bash
[Prefix]: Short Description
```

### Prefixes

Prefixes are used to categorize commits based on the type of change they
represent. The following prefixes are used in our project:

* **new**: A new feature implementation
* **chg**: Implementation changes
* **dep**: Changes related to deprecated code or functionality
* **rem**: Removal or deletion of code or files
* **fix**: A bug fix
* **sec**: Security-related changes or fixes

## Commit Message Guidelines

Please adhere to the following guidelines when crafting your commit messages:

* Separate the subject line from the body with a blank line.
* Limit the subject line to 50 characters or less and the body to 72 characters
  per line.
* Capitalize the subject line and use imperative mood (e.g., "Add", "Fix").
* Do not end the subject line with a period.
* Provide explanatory text in the body to explain what and why, rather than how.
* Include relevant issues by referencing their numbers in the commit message.
* Use the following rules for commit messages:
  1. Follow an atomic commit approach, addressing a single logical change in
    each commit.
  2. Include the original problem description when fixing an issue.
  3. Avoid assuming the code is self-explanatory and provide sufficient context.

### Example Commit Message

```
[new] Implement login functionality

This commit adds a new login feature that allows users to authenticate
and access protected resources. It includes the necessary API endpoints,
database schema modifications, and user interface changes.

Fixes #123
```

## Breaking Change

In our project, we follow a specific convention for a committing a breaking
changes to our codebase. This convention helps us maintain a clear and
standardized commit history, making it easier for developers to understand
the nature and impact of changes.

A breaking commit refers to a commit that introduces changes that could
potentially break existing functionality or cause compatibility issues.
These changes are typically significant and may require careful consideration
by other developers who depend on the codebase.

When creating a breaking commit in our project, we include an exclamation
mark (!) symbol at the end of the prefix but before the short description.
This convention is widely adopted within our team and serves as a visual
indicator to quickly identify commits that may introduce compatibilty issues
or break existing functionality.

For instance, if we make a breaking change that modifies a critical function
in our software library, we would write a breaking commit message like
"[chg]! modify function x to improve performance" By adding the exclamation
mark after the "chg" prefix, it becomes immediately apparent that this commit
contains changes with the potential to break existing functionality.

It's important to note that the convention of adding the exclamation mark (!)
symbol after the breaking prefix is specifc to our project. While it may not
be a strict requirement imposed by version control systems or tools like Git,
we have found it to be a valuable practice that improves code comprehension
within our team.

## Coding Style and Code Analysis

* All code changes must adhere to the project's [coding style guidelines](CODING_STYLE.md).
* We use SonarLint tool to identify and resolve bugs, code smells, and other
  issues.
* Ensure that every commit has no SonarLint warnings or issues.

## Merge Requests and Issues

In our project, we use merge requests and issues to facilitate collaboration
and track progress. Understanding when to use each can help streamline the
development process.

### Merge Requests

Merge requests are used when you have completed a task or made changes to the
code that need to be reviewed and merged into the `main` branch. They are
typically used for the following scenarios:

* Implementing new features
* Fixing bugs
* Refactoring code
* Making significant changes to the project

When creating a merge request, make sure to provide a clear and concise
description of the changes made, along with any relevant information or
context. This helps reviewers understand the purpose and impact of the changes
and enables a smooth review process.

### Issues

Issues are used to track and discuss specific tasks, enhancements, or bugs
in the project. They are a way to document and organize work that needs to
be done. Issues can be used for the following purposes:

* Reporting bugs or unexpected behavior
* Discussing and planning new features or enhancements
* Tracking tasks and assignments

When creating an issue, provide a descriptive title and a detailed description
of the problem or task. Include any relevant information, such as steps to
reproduce a bug or specifications for a new feature. This helps in providing
context and ensures that everyone involved understands the purpose and scope
of the issue.

It's a good practice to reference the relevant issues in your merge requests
by mentioning the issue number or using the "Closes" keyword. This helps in
establishing the link between the work being done and the associated issue,
making it easier to track the progress and close the issue when the changes
are merged.

Remember, clear and informative merge requests and well-documented issues
contribute to effective collaboration and ensure that the development process
runs smoothly.

## Branch Strategy

### Branch Types

* **`main` branch**: The `main` branch serves as the main integration
  branch for ongoing development work. It is where individual feature
  branches or bug fix branches are merged in preparation for the next
  release. The `main` branch may contain features and bug fixes that
  are still being tested and refined.

When starting work on a new feature or bug fix, create a new branch from
the `main` branch. Once the changes are completed and tested, they can
be merged back into the `main` branch.

### Creating Branches

When working on a new feature, bug fix, or any other code change, follow
this branch naming convention:

```
<type>/<short-description>
```

* `<type>`: Use one of the following prefixes to indicate the type of the branch:
    * `feat`: Feature implementation
    * `fix`: Bug fix
    * `ref`: Code refactoring
    * `docs`: Documentation changes
    * `style`: Code style changes
    * `perf`: Performance improvements
    * `ci`: Continuous Integration configuration
    * `sec`: Security-related changes or fixes
* `<short-description>`: A brief description of the changes being made.

Example: `feat/user-authentication`, `fix/file-upload-bug`

## Workflow

1. Create a new branch for the issue or task you are working on, based on
   the `main` branch.
2. Make the necessary changes, additions, or fixes in your branch.
3. Commit your changes following the commit message guidelines, including
   relevant issues.
4. Push your branch to the remote repository.
5. Create a merge request to propose your changes for review.
6. Discuss and iterate on your code changes through comments on the
   merge request.
7. Once the changes are approved, they will be merged into the `main` branch.

## Discussion and Communication

All discussions and communication related to the project should be conducted
in English. This helps to ensure clear and effective communication among
contributors from different backgrounds.

We appreciate your contribution to Configed and adherence to these guidelines!
By following these guidelines and the established workflow, we can maintain
a collaborative and efficient development process.

Please reach out to us if you have any questions or need further assistance.

Thank you,

Configed Team

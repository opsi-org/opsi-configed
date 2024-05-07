# opsi-configed Coding Style

## Indentation and Formatting

* Use 4 spaces for indentation.
* Use braces for block statements, even if the block only contains a single
  statement.
* Place a space before opening parentheses in control statements (`if`,
  `for`, `while`, etc.).
* Use a newline after opening and before closing braces for classes, methods,
  and control statements.
* Limit the line length to 120 characters for better readability.

## Braces

* Use the "Egyptian brackets" style for braces:
  * Place the opening brace on the same line as the declaration or control
    statement.
  * Place the closing brace on a new line, aligned with the indentation
    level of the corresponding opening statement.

### Example:

```java
if (condition) {
    // Statements
} else {
    // Statements
}
```

## Naming Conventions

### Class and Interface Names

* Use PascalCase (CamelCase with initial uppercase) for class and interface
  names.
* Choose meaningful and descriptive names that accurately represent the
  purpose or behavior of the class or interface.
* Abstract class should contain `Abstract` in their names (`AbstractTree`).

### Method Names

* Use camelCase for method names (initial lowercase).
* Choose meaningful and descriptive names that accurately represent the
  action or behavior performed by the method.
* Use verbs or verb phrases to indicate actions (e.g., `calculateTotal()`,
  `getUserByID()`).

### Variable Names

* Use camelCase for variable names (initial lowercase).
* Choose meaningful and descriptive names that accurately represent the
  purpose or content of the variable.
* Avoid single-character names except for loop counters (`i`, `j`, `k`).
* Use nouns or noun phrases for variables representing objects or data.

### Constant Names

* Use uppercase with underscores for constant names.
* Separate words in a constant name with underscores (`CONSTANT_NAME`).
* Choose descriptive names that clearly indicate the purpose or value of
  the constant.

### Abbreviations

* For abbreviations use uppercase (e.g., `IO`, `URL`, `ID`).

## Code Documentation

* Use Javadoc comments to document classes, interfaces, methods, and variables.
* Provide meaningful descriptions and explanations for the documented elements.
* Include information about the purpose, behavior, parameters, return values,
  and exceptions (if applicable) in the Javadoc comments.

## Code Quality and SonarLint

* Follow code quality best practices and adhere to the principles of clean code.
* Utilize SonarLint tool to identify and resolve code bugs, code smells, and
  potential vulnerabilities. Configed has its own SonarLint server, which
  is used by us and is specifically configured for the project. The Configed
  server should be used to adhere to our configured rules.
* Ensure that every commit is free of SonarLint warnings and errors.

By adhering to these coding style conventions and best practices, you can
maintain clean, readable, and consistent Java code for opsi-configed project.


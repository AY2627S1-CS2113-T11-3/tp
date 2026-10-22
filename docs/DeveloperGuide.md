# Developer Guide

> Adapted from the [Address Book Level 3 Developer Guide](https://se-education.org/addressbook-level3/DeveloperGuide.html).

## Table of Contents

- [Acknowledgements](#acknowledgements)
- [Setting up and getting started](#setting-up-and-getting-started)
- [Design](#design)
  - [Architecture](#architecture)
  - [UI component](#ui-component)
  - [Logic component](#logic-component)
  - [Model component](#model-component)
  - [Storage component](#storage-component)
  - [Common classes](#common-classes)
- [Implementation](#implementation)
  - [Inventory management](#inventory-management)
  - [Session scheduling](#session-scheduling)
  - [Session preparation](#session-preparation)
  - [Contact management](#contact-management)
  - [Loan tracking](#loan-tracking)
  - [Validation and error handling](#validation-and-error-handling)
- [Documentation, logging, testing, and DevOps](#documentation-logging-testing-and-devops)
- [Appendix: Requirements](#appendix-requirements)
- [Appendix: Instructions for manual testing](#appendix-instructions-for-manual-testing)

## Acknowledgements

- [Address Book Level 3](https://se-education.org/addressbook-level3/) — used as the structure and documentation style reference.
- [Gradle](https://gradle.org/) — used for build automation, dependency management, and test execution.
- [JUnit 5](https://junit.org/junit5/) — used for automated unit and integration tests.
- [Java logging API](https://docs.oracle.com/en/java/javase/25/docs/api/java.logging/module-summary.html) — used for diagnostic logging.

## Setting up and getting started

### Prerequisites

- Java Development Kit (JDK) 25.
- A terminal and a Java-compatible IDE such as IntelliJ IDEA.
- Internet access for the first Gradle build, when dependencies are downloaded.

The project is intended to run on Windows, macOS, and Linux. The exact JDK version should be used because the project
is configured for Java 25.

### Importing and building

1. Clone or download the repository.
2. Open the repository root, the directory containing `build.gradle`, in the IDE.
3. Import the project as a Gradle project.
4. Verify the JDK used by Gradle and the IDE is Java 25.
5. Build the application:

   ```text
   Windows: .\gradlew.bat check shadowJar
   macOS/Linux: ./gradlew check shadowJar
   ```

### Running Bob

After building, run the generated JAR:

```text
java -jar build/libs/duke.jar
```

Bob prints a banner, asks for the user's name, and then accepts one command per line. Enter `Bye` to exit.

### Running tests

- Run the complete automated test and Checkstyle suite with `check`.
- Run the text UI tests with `text-ui-test\runtest.bat` on Windows or `sh text-ui-test/runtest.sh` on macOS/Linux.
- The feature-specific manual test cases are recorded in [`test/ui-test-plan.md`](../test/ui-test-plan.md).

## Design

Bob is a command-line application with a simple component structure:

```text
User
  |
  v
Bob / Ui  <-->  Parser  <-->  Model managers
                              |-- Inventory
                              |-- SessionManager
                              |-- ContactManager
                              `-- LoanManager
                                      |
                                      `-- Inventory
```

`Bob` starts the application and wires the components together. `Ui` contains shared display text, `Parser` validates
and dispatches commands, and the model classes store the current application state in memory.

### Architecture

The application uses a lightweight layered architecture:

- **UI** — reads command-line input and displays output.
- **Logic** — parses commands, validates their formats, and dispatches operations.
- **Model** — stores inventory, sessions, preparations, contacts, and loans.
- **Storage** — not implemented; all data is intentionally in memory for the current version.
- **Common classes** — shared domain objects, exceptions, and display constants.

`Bob.main` creates one `Parser`, one instance of each model manager, and a shared `Inventory` for the duration of the
process. The parser receives these objects when handling each command. This keeps the managers independent while allowing
loans and preparations to query or update inventory.

### UI component

The UI is implemented by [`Bob`](../src/main/java/seedu/bob/Bob.java) and [`Ui`](../src/main/java/seedu/bob/Ui.java).

- `Bob` displays the banner, reads input using `Scanner`, and prints command results.
- `Ui` stores the divider and the complete `help` message.
- The UI does not contain business rules; it delegates command handling to `Parser`.
- The application exits when the user enters `Bye` in any letter case.

### Logic component

The logic component is implemented by [`Parser`](../src/main/java/seedu/bob/Parser.java).

For every input line, `Parser`:

1. separates the command word from its arguments;
2. selects the command handler;
3. checks prefixes, argument order, and value formats;
4. constructs domain objects where necessary; and
5. invokes the relevant model manager.

Commands use fixed prefix orders, such as `n/NAME c/CATEGORY q/QUANTITY`. A shared prefix matcher separates values from
later arguments. Consequently, a value containing a standalone token such as `a/b` is currently interpreted as another
prefix and is rejected.

Expected command errors are converted into user-facing messages. Unexpected runtime failures are logged and replaced by a
generic safe error message so that one malformed command does not terminate the application.

### Model component

The model is composed of domain objects and managers:

- [`Inventory`](../src/main/java/seedu/bob/Inventory.java) stores categories and `InventoryItem` objects.
- [`SessionManager`](../src/main/java/seedu/bob/SessionManager.java) stores sessions in chronological order.
- [`Session`](../src/main/java/seedu/bob/Session.java) validates dates, times, and scheduling conflicts.
- [`Preparation`](../src/main/java/seedu/bob/Preparation.java) stores per-session requirements through `PreparationItem`.
- [`ContactManager`](../src/main/java/seedu/bob/ContactManager.java) stores contacts.
- [`LoanManager`](../src/main/java/seedu/bob/LoanManager.java) stores active loans and shares the inventory with loans.
- [`Loan`](../src/main/java/seedu/bob/Loan.java) stores an independent item snapshot, quantity, borrower, and due date.

Managers expose operations rather than their mutable internal collections. Where a collection is returned for querying,
the implementation returns an immutable snapshot. Domain classes maintain invariants such as positive quantities,
nonblank names, and conflict-free sessions.

### Storage component

There is no storage component in the current version. Inventory, sessions, preparations, contacts, and loans are created
when Bob starts and are discarded when the process exits. Persistence and data migration are future work.

### Common classes

The `seedu.bob.exception` package contains checked domain exceptions for invalid commands, indices, quantities, dates,
stock, and session conflicts. `Ui.DIVIDER` and `Ui.HELP_MESSAGE` are shared display constants. `InventoryItem` and
`PreparationItem` are small value objects used by their respective managers.

## Implementation

### Inventory management

`Inventory` stores items in insertion-ordered categories. Item names are unique within a category, ignoring case, while
the same name may occur in different categories. `add-i` creates a category when needed, and `delete-i` removes a
positive quantity. Removing all units removes the item and then removes an empty category.

Inventory operations validate all inputs before changing state. Loan deductions use the same private quantity-deduction
path as `delete-i`, ensuring depleted items and categories are handled consistently. Inventory listing uses one-based item
indices within each category.

### Session scheduling

`Session` parses dates in `d MMMM yyyy` format and times in `HHmm` format. Dates cannot be in the past, times must be
valid, and the end time must be later than the start time. `SessionManager` rejects overlapping sessions at the same
location, ignoring location capitalization, but permits overlapping sessions at different locations.

Sessions are sorted by date, start time, and name. Because sorting can change positions, users should run `list-s` before
using a session index. Deleting a session also discards its preparation.

### Session preparation

Each `Session` owns a `Preparation`. `add-p` converts a per-attendee quantity into a total using the session headcount.
Repeated name/category pairs are accumulated case-insensitively. `delete-p` removes a total quantity from the displayed
preparation row.

Preparation does not reserve or deduct inventory. At display time, it queries the live inventory and marks each row as
insufficient when available stock is below the required total. This means a loan or inventory deletion immediately affects
preparation availability.

### Contact management

`ContactManager` owns an insertion-ordered list of contacts. A contact requires a nonblank name and a digits-only phone
number; email and role are optional. Email validation checks for `@` and rejects whitespace. Duplicate contacts are
allowed because different suppliers or service providers may share a name or phone number.

`add-c`, `list-c`, and `delete-c` follow the same manager-and-parser structure as sessions. Contact indices are one-based
and are renumbered after deletion.

### Loan tracking

`LoanManager` is created with the shared `Inventory`. `add-l` validates the complete command before calling
`Inventory.lendItem`, so invalid input or insufficient stock leaves both inventory and loans unchanged. Lending all
available units removes the depleted inventory entry.

Each `Loan` contains an item snapshot and an immutable borrower `Contact`. Borrowers are not added to `ContactManager`,
and each successful command creates a separate loan record. Due dates use strict English `d MMMM yyyy` parsing. Past due
dates are accepted as backlog entries and produce a warning; a loan due today is not overdue.

`list-l` displays active loans in insertion order and calculates overdue status using an injected `Clock`. The default
clock uses the computer's timezone, while tests can inject a fixed or adjustable clock. `list-l overdue` filters the
display but keeps the original active-list indices so that `return` and `delete-l` always refer to the full list.

`return` and `delete-l` share a restoration path. The path restores stock first, then removes the loan. Existing matching
items and categories are updated case-insensitively; missing entries are recreated. Integer overflow or an invalid index
therefore leaves the loan and inventory unchanged.

### Validation and error handling

Validation is performed at the parser and domain boundaries:

- parser validation checks command names, prefix order, missing values, and integer syntax;
- domain constructors validate names, dates, times, quantities, and phone or email formats;
- managers validate indices, conflicts, stock levels, and state transitions; and
- assertions document internal invariants such as positive stored quantities and conflict-free schedules.

The parser catches expected `CommandException` subclasses and prints their messages. It logs unexpected runtime failures
with `java.util.logging` and prints a generic recovery message.

Loan validation uses the existing checked exception hierarchy. Invalid loan dates and malformed loan indices retain
their original parsing causes for debugging while preserving the existing user-facing messages. The loan stock helpers
also validate item names, categories, and positive quantities before modifying inventory, even when called directly.

Loan assertions check valid stored records and the exact inventory quantity and active-list size changes for successful
operations. Stock restoration is checked before removing a loan. Assertions identify internal programming errors;
expected invalid commands are always handled by exceptions. Assertion failures are not caught as recoverable user errors.
Use `java -ea -jar build/libs/duke.jar` for debugging; Gradle tests enable assertions, while normal launch is unchanged.

## Documentation, logging, testing, and DevOps

- [User Guide](UserGuide.md) — user-facing command formats, examples, and limitations.
- [UI test plan](../test/ui-test-plan.md) — command-by-command manual and console test cases.
- [README](../README.md) — setup, Gradle, testing, and Checkstyle instructions.

The project uses Gradle and the Gradle wrapper for reproducible builds. Java code follows the SE-EDU Java coding standard,
and `check` runs compilation, automated tests, and Checkstyle. Changes should be developed on a feature branch and
verified with `check` before review. The repository currently has no persistence or release automation beyond the Gradle
build and the project's CI configuration.

Logging is intended for diagnostics rather than user interaction. `Bob`, `Parser`, `Inventory`, `SessionManager`, and
`LoanManager` log lifecycle events, rejected operations, and unexpected failures at suitable levels.

Loan lifecycle, overdue-list queries, stock changes, and expected rejections use `FINE`, matching inventory diagnostics.
Messages include item/category, quantities, due dates, indices, or active counts as appropriate. They omit borrower
names, phone numbers, and raw commands. Expected loan command errors retain their exception causes in debug records;
unexpected runtime failures are logged once at the existing parser `SEVERE` boundary. Default logging is unchanged.

To inspect loan diagnostics, save this configuration as `build/loan-debug-logging.properties` after building:

```properties
handlers=java.util.logging.ConsoleHandler
.level=INFO
java.util.logging.ConsoleHandler.level=FINE
seedu.bob.LoanManager.level=FINE
seedu.bob.Inventory.level=FINE
seedu.bob.Parser.level=FINE
```

Then launch a debug session with:

```text
java -ea -Djava.util.logging.config.file=build/loan-debug-logging.properties -jar build/libs/duke.jar
```

Normal launches need no logging configuration. Logging tests capture records directly and restore shared logger settings
after each test. The UI regression plan is checked both normally and with assertions enabled.

## Appendix: Requirements

### Product scope

#### Target users

Bob targets lab technicians or teaching assistants who:

- manage reusable equipment and consumable supplies;
- schedule laboratory sessions at different locations and times; and
- need a quick command-line workflow without a database or graphical interface.

#### Value proposition

Bob combines inventory tracking, session scheduling, preparation checks, supplier contacts, and student loan tracking in
one lightweight command-line application. It highlights preparation shortages and updates availability when stock is
loaned or returned.

### User stories

| Priority | As a... | I want to... | So that I can... |
|---|---|---|---|
| High | lab technician | add, remove, and list inventory | know what equipment is available |
| High | lab technician | schedule and delete lab sessions | avoid conflicting bookings |
| High | lab technician | prepare materials for a session | identify shortages before the session |
| High | lab technician | record and return loans | track borrowed resources and restore stock |
| Medium | lab technician | save supplier and repair contacts | find relevant people quickly |
| Medium | lab technician | filter overdue loans | follow up on late returns |
| Low | project maintainer | persist data between launches | retain records after restarting Bob |

### Use cases

#### Use case: Prepare a lab session

**Actor:** Lab technician

**Preconditions:** Bob is running and a session has been added.

**Main success scenario:**

1. User adds inventory items.
2. User creates a session with a headcount.
3. User adds required items with `add-p`.
4. System calculates total requirements and compares them with current inventory.
5. User views `list-p` and obtains the preparation checklist.

Use case ends.

**Extensions:**

- **4a.** Required stock exceeds available stock.
  - **4a1.** System marks the item insufficient and displays the shortfall.
  - Use case resumes at step 5.

#### Use case: Record and return a loan

**Actor:** Lab technician

**Preconditions:** The requested item exists in inventory with enough available quantity.

**Main success scenario:**

1. User enters `add-l` with item, quantity, borrower, phone, and due date.
2. System deducts the quantity and creates an active loan.
3. User enters `return INDEX` when the item is returned.
4. System restores the quantity and removes the active loan.

Use case ends.

**Extensions:**

- **1a.** The item is missing or stock is insufficient.
  - **1a1.** System rejects the command without changing state.
- **3a.** Restoring stock would overflow the integer range.
  - **3a1.** System rejects the return and keeps the loan active.

### Non-functional requirements

1. The application should run with JDK 25 on Windows, macOS, and Linux.
2. Normal commands should complete interactively without noticeable delay for the expected small, in-memory data set.
3. Invalid user input should produce a clear error message and should not partially modify application state.
4. Collections exposed for querying should not allow callers to mutate manager state accidentally.
5. The code should remain maintainable through JavaDoc, Checkstyle, unit tests, and feature-level UI tests.
6. The current version does not promise persistence, multi-user access, encryption, or recovery after process termination.

### Glossary

- **Inventory item** — A named resource stored under a category with a positive quantity.
- **Session** — A scheduled lab event with a date, location, time range, and headcount.
- **Preparation** — The total material requirement for one session.
- **Loan** — An active record of inventory units lent to a borrower until returned or corrected.
- **Backlog loan** — A loan added with a due date before today.
- **Overdue** — A loan whose due date is strictly before the current local date.
- **Prefix** — The one-letter marker such as `n/` or `q/` that introduces a command argument.

## Appendix: Instructions for manual testing

These instructions supplement the detailed cases in [`test/ui-test-plan.md`](../test/ui-test-plan.md). Start each scenario
with a fresh Bob process because application data is not persisted.

### Launch and shutdown

1. Build with `gradlew.bat check shadowJar` on Windows or `./gradlew check shadowJar` on macOS/Linux.
2. Launch with `java -jar build/libs/duke.jar`.
3. Verify that Bob prints the banner and asks for a name.
4. Enter a name and verify that the welcome message and divider appear.
5. Enter `help` and verify that all command groups are displayed.
6. Enter `Bye`, `bye`, or `BYE` and verify that the application exits cleanly.

### Inventory and preparation

1. Add an item with `add-i` and confirm it with `list-i`.
2. Add a session with a future date using `add-s`, then add a requirement using `add-p`.
3. Use `list-p` to verify required quantity, available quantity, and shortage markers.
4. Use `delete-p` and verify that only the preparation changes, not inventory.
5. Test invalid quantities, indices, missing prefixes, duplicate inventory items, and conflicting sessions.

### Contacts and loans

1. Add contacts with required fields and with optional email or role fields.
2. Use `list-c`, then delete a contact using its displayed index.
3. Add enough inventory for a loan and execute `add-l`.
4. Verify that `list-i` shows reduced stock and `list-l` shows the active loan.
5. Add a past-due loan and verify the warning and `[OVERDUE]` marker.
6. Use `list-l overdue` and verify that displayed indices still refer to the full active-loan list.
7. Execute `return INDEX` and verify that stock is restored and the loan disappears.
8. Test insufficient stock, invalid dates, invalid borrower phone numbers, invalid indices, and restoration overflow.

### Automated and UI verification

1. Run `gradlew.bat check` or `./gradlew check` and confirm that compilation, tests, and Checkstyle pass.
2. Run the platform-specific text UI test script.
3. For each case in `test/ui-test-plan.md`, compare actual output with the expected output exactly, including dividers,
   spacing, capitalization, warnings, and blank lines.

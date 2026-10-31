# UI Test Plan

This file is the source of truth for repeatable console UI test cases run with the `test-ui` skill.

The course-style I/O redirection test in `text-ui-test/input.txt` and `text-ui-test/EXPECTED.txt`
reuses preparation, loan, lookup, broken-item, request-fulfillment, and help scenarios.
It substitutes the fixed future date `31 December 2099` for `{{TOMORROW}}`.
Its shell/batch script compares the full session after execution. To execute every active case below
command by command on Windows, build first, then run `powershell -File scripts/run-ui-tests.ps1`.
The runner stops at the first mismatch and writes `build/ui-plan/transcript.txt`.
Use `-CasePrefix UI-REQUEST-` to run just the request cases, for example.

For a demonstration of the current implementation, run `UI-PERSIST-FINAL-001` and `UI-PERSIST-FINAL-002`
with `-CasePrefix UI-PERSIST-FINAL-`. These cases create records for all six features, exit, restart using
the same isolated storage directory, and return the saved loan. The latest demonstration transcript is
[`ui-current-implementation-session.md`](ui-current-implementation-session.md). Expected outputs below include the stock deducted when fulfilling a request.

## Test environment

- Java: 25
- Build command: `gradlew.bat shadowJar` on Windows or `./gradlew shadowJar` on macOS/Linux
- Run command: `java -jar build/libs/bob.jar`
- Comparison: exact after newline normalization, ANSI-sequence removal, and ignoring one final newline
- Isolation: each test case starts a fresh application process in a new directory under `build/ui-tests-*`;
  only cases with the same explicit `Storage group` reuse that run's files
- Command response terminator: each ordinary command emits exactly one additional blank line after its expected output; EOF and `Bye` exit without a response terminator
- Diagnostics: stderr is captured separately from exact stdout comparisons; normal INFO/WARNING logs are allowed;
  exits must match the recorded `Exit code` (default 0), and SEVERE failures always fail the case
- Timeout: 10 seconds per output line or process exit; missing output and unexpected extra output fail the case
- Whitespace notation: each `␠` in the startup fixture or an input block represents one literal space and is decoded before comparison or sending input
- Startup prompt:

```text
  ____        _␠␠␠␠␠
 |  _ \      | |␠␠␠␠
 | |_) | ___ | |__␠␠
 |  _ < / _ \| '_ \␠
 | |_) | (_) | |_) |
 |____/ \___/|_.__/␠

What is your name?
```

- Test-user input: `Alex`
- Expected greeting:

```text
Hello Alex, welcome to Bob!
--------------------------------------------------------------------
```

## Phase 0 baseline contract

Expected outputs were reconciled before execution against the existing documented contact behavior and
Java unit-test contracts. Session ordering is chronological (date, start time, then name ignoring case),
as required by `SessionManagerTest` and `SessionTest`; validation messages follow `SessionTest`.
The reverse-order and tie-break cases below exercise that contract explicitly. This preserves scheduling
behavior while correcting stale documentation and UI fixtures, independently of actual test output.
Contact cases cover optional fields, validation, duplicates, deletion, renumbering, and feature isolation.
Loan commands, persistence, and return history are outside phase 0.

## Phase 1 loan contract

`add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE` requires every field.
`list-l` shows active loans in insertion order with independent one-based indices. Each accepted command
creates a separate loan and deducts available stock once. Item/category matching ignores case; loan rows
retain the spelling entered by the user. Borrower details belong to the loan, not supplier contacts.
All input and stock checks happen before mutation. Due dates use strict full English month names and may
be in the past. Deletion, returns, overdue warnings/markers/filtering, and persistence remain later phases.
Expected outputs below were recorded before implementing the feature.

## Phase 2 loan correction contract

`delete-l INDEX` removes an incorrect active loan and restores its full quantity to inventory, with a
confirmation explicitly describing an incorrect-record deletion. It is separate from the future `return`
command. Indices come from the latest `list-l` and remaining loans are renumbered after deletion.
Restoration matches item/category ignoring case, merges into existing stock, or recreates a missing
item/category at the end of its insertion order using the loan spelling when no stored spelling exists.
Invalid indices and restoration overflow leave both loans and inventory unchanged. There is no partial
correction. The cases below and updated help/smoke fixtures were recorded before implementation.

## Phase 3 full return contract

`return INDEX` records the complete return of one active loan. It restores all units, removes the active
record, and prints a return-specific confirmation distinct from `delete-l`. It does not retain history.
The index comes from the latest `list-l`; remaining entries are renumbered. No quantity or additional
argument is accepted. Invalid indices and restoration overflow preserve both inventory and loans.
Returns and corrections share the same checked stock-restoration path. Overdue behavior and persistence
remain later work. These cases and the help/smoke output updates were recorded before implementation.

## Phase 4 overdue contract

A loan is overdue only when its due date is before today's local calendar date; due today is not overdue.
Accepted past-date additions print `Warning: this loan is already overdue.` after the success confirmation.
`list-l` appends `[OVERDUE]` to overdue rows. `list-l overdue` shows only overdue records with the same
indices as the full active list, and uses `No overdue loans.` when none match. Status is recomputed from
the current date on each display, without changing stock or loans. Returns/corrections still use the
full active-list index; remaining indices shift normally after removal. Earlier loan/list/date fixtures
are updated for this approved behavior before implementation, and unrelated cases are preserved.
Clock-controlled unit tests cover midnight rollover, calendar boundaries, and timezone behavior.

## Defensive programming: phase 1 exception contract

Loan command syntax and stdout remain unchanged. Invalid dates and loan indices that are not integers retain
their parsing causes in checked exceptions without exposing stack traces to the user. Loan inventory
helpers reject invalid names, categories, and quantities before mutation, including direct domain calls.
UI-LOAN-DEFENSIVE-001 records recovery after rejected commands; the existing overflow, date, index,
borrower, stock, and feature-isolation cases remain mandatory regression coverage after every phase.
Unit tests verify exception causes and unchanged state, which stdout comparisons cannot inspect.
Expected outputs are recorded before implementation and before running the affected cases.

## Defensive programming: phase 2 assertion contract

Assertions check loan invariants and exact stock/list-size changes during additions, corrections, and
returns. Private deduction helpers assert their already-validated preconditions. Expected user errors
still use checked exceptions with assertions disabled. Duplicate loans and past due dates remain valid.
After this phase, run the full plan normally and again with `java -ea -jar build/libs/bob.jar`.
Both runs must produce the same expected stdout. Unit tests use faulty
inventory test doubles to prove missing deduction/restoration is detected before the loan list changes.
The normal launch scripts remain unchanged; debug launch is `java -ea -jar build/libs/bob.jar`.

## Defensive programming: phase 3 logging contract

Loan lifecycle, overdue-list queries, stock changes, and expected rejections use `java.util.logging`
at FINE level, following existing inventory diagnostics. Default logger settings and normal stdout
remain unchanged. Diagnostic messages omit borrower names, phone numbers, and raw command input.
Expected loan command failures retain their checked exception and parsing cause in the debug log.
Unexpected runtime failures remain logged once at the parser's existing SEVERE boundary.
Run all cases normally and with assertions enabled after this phase. Logging unit tests capture
records directly to verify levels, causes, operation details, and exclusion of borrower details.

Verification on 22 October 2026 (Java 25): phase 1 passed 134 unit tests and the full normal UI run;
phase 2 passed 136 unit tests and full normal/debug UI runs; phase 3 passed 144 unit tests and full
normal/debug UI runs. Each UI run passed 58 cases, 481 steps, and 480 commands (one step is EOF).
All three Gradle `check shadowJar` builds passed both Checkstyle checks. Per-step results and full
chronological transcripts are retained in `test/ui-test-session.md` as local session records.

## Loan review fixes: phase 1 borrower snapshots

Each loan captures its borrower name and phone as immutable values when constructed. Contact validation
is reused, but the source contact is not retained or exposed. Existing command syntax, confirmations,
phone spelling (including leading zeros), borrower isolation, and list output remain unchanged.
UI-LOAN-ADD-001, UI-LOAN-ADD-006, and the full existing plan retain their recorded expected outputs.
JUnit tests use a mutable Contact test double to verify source edits cannot change an earlier loan.
After committing this phase, execute the complete UI regression plan with assertions enabled before
starting the next phase. Expected outputs are preserved before implementation and execution.

## Loan review fixes: phase 2 operation results and confirmations

Adding a loan returns its recorded loan and overdue status at addition; corrections and returns return
the removed loan. These manager mutations produce no stdout. Parser passes successful results to Ui,
which retains the existing add, incorrect-record deletion, full-return, and backlog-warning messages.
The manager still owns validation, stock transitions, assertions, logging, and the injected clock.
All existing add/delete/return and overdue-warning expected outputs remain unchanged. Unit tests verify
silent manager mutations and independently verify Ui confirmations. Commit this phase, then run the
complete existing UI plan with assertions enabled before changing list rendering in phase 3.

## Loan review fixes: phase 3 listing queries and rendering

LoanManager returns immutable listing rows containing the loan, its full active-list index, and its
overdue status evaluated against one local date per query. Queries neither print nor mutate state.
Parser passes the selected rows to Ui; Ui owns headings, dividers, original indices, markers, and empty
messages. The overdue view preserves full-list indices rather than renumbering filtered results.
UI-LOAN-LIST-001 and UI-LOAN-OVERDUE-001 through UI-LOAN-OVERDUE-004 keep their exact recorded outputs,
as do all earlier cases. Unit tests cover query immutability, clock read count, rollover/time zones,
and independent list rendering. Commit this phase, then run the full plan with assertions enabled.

## Test cases

### UI-LIFECYCLE-001: Exit cleanly at end of input

**Aim:** Verify that closing standard input after startup exits the application without an exception.

**Preconditions:** Fresh application at the command prompt after accepting the test-user name.

#### Step 1

**Input**

```text
Close standard input (EOF)
```

**Expected output (no output)**

```text
```

### UI-ADD-001: Reject malformed add commands

**Aim:** Verify that malformed `add-i` prefixes and empty required values produce precise errors.

**Preconditions:** Empty inventory.

#### Step 1

**Input**

```text
add n/Wire c/Components
```

**Expected output**

```text
Invalid command
```

#### Step 2

**Input**

```text
add-i c/Components q/10
```

**Expected output**

```text
Invalid format. Use: add-i n/NAME c/CATEGORY [q/QUANTITY]
```

#### Step 3

**Input**

```text
add-i n/Wire q/10 c/Components
```

**Expected output**

```text
Invalid format. Use: add-i n/NAME c/CATEGORY [q/QUANTITY]
```

#### Step 4

**Input**

```text
add-i n/Wire n/Cable c/Components q/10
```

**Expected output**

```text
Invalid format. Use: add-i n/NAME c/CATEGORY [q/QUANTITY]
```

#### Step 5

**Input**

```text
add-i n/ c/Components q/10
```

**Expected output**

```text
Item name cannot be blank.
```

#### Step 6

**Input**

```text
add-i n/Wire c/ q/10
```

**Expected output**

```text
Category cannot be blank.
```

#### Step 7

**Input**

```text
add-i n/Wire c/Components x/extra
```

**Expected output**

```text
Invalid format. Use: add-i n/NAME c/CATEGORY [q/QUANTITY]
```

### UI-ADD-002: Reject invalid add quantities

**Aim:** Verify that quantities must be positive integers within the Java integer range.

**Preconditions:** Empty inventory.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/abc
```

**Expected output**

```text
Quantity must be a positive integer.
```

#### Step 2

**Input**

```text
add-i n/Wire c/Components q/
```

**Expected output**

```text
Quantity must be a positive integer.
```

#### Step 3

**Input**

```text
add-i n/Wire c/Components q/0
```

**Expected output**

```text
Quantity must be greater than zero.
```

#### Step 4

**Input**

```text
add-i n/Wire c/Components q/-5
```

**Expected output**

```text
Quantity must be greater than zero.
```

#### Step 5

**Input**

```text
add-i n/Wire c/Components q/2147483648
```

**Expected output**

```text
Quantity must be a positive integer.
```

### UI-ADD-003: Add valid items and reject a duplicate

**Aim:** Verify the default quantity, explicit quantity, multi-word name, and duplicate-item rule.

**Preconditions:** Empty inventory.

#### Step 1

**Input**

```text
add-i n/Oscilloscope c/Equipment
```

**Expected output**

```text
Successfully added: 1x Oscilloscope to the inventory
```

#### Step 2

**Input**

```text
add-i n/10k Resistor c/Components q/500
```

**Expected output**

```text
Successfully added: 500x 10k Resistor to the inventory
```

#### Step 3

**Input**

```text
add-i n/oscilloscope c/equipment q/2
```

**Expected output**

```text
Item "oscilloscope" already exists in category "Equipment".
```

### UI-DELETE-001: Reject malformed delete commands

**Aim:** Verify that `delete-i` requires one line with exactly one category, index, and quantity prefix.

**Preconditions:** Empty inventory.

#### Step 1

**Input**

```text
delete-i
```

**Expected output**

```text
Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY
```

#### Step 2

**Input**

```text
delete-i c/Components q/1
```

**Expected output**

```text
Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY
```

#### Step 3

**Input**

```text
delete-i i/1 c/Components q/1
```

**Expected output**

```text
Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY
```

#### Step 4

**Input**

```text
delete-i c/Components i/1 i/2 q/1
```

**Expected output**

```text
Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY
```

#### Step 5

**Input**

```text
delete-i c/ i/1 q/1
```

**Expected output**

```text
Category cannot be blank.
```

#### Step 6

**Input**

```text
delete-i c/Components i/1 q/1 x/extra
```

**Expected output**

```text
Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY
```

### UI-DELETE-002: Reject invalid delete numbers

**Aim:** Verify that item indices and quantities must be positive integers within the Java integer range.

**Preconditions:** Empty inventory.

#### Step 1

**Input**

```text
delete-i c/Components i/abc q/1
```

**Expected output**

```text
Item index must be a positive integer.
```

#### Step 2

**Input**

```text
delete-i c/Components i/0 q/1
```

**Expected output**

```text
Item index must be a positive integer.
```

#### Step 3

**Input**

```text
delete-i c/Components i/2147483648 q/1
```

**Expected output**

```text
Item index must be a positive integer.
```

#### Step 4

**Input**

```text
delete-i c/Components i/1 q/0
```

**Expected output**

```text
Quantity must be greater than zero.
```

#### Step 5

**Input**

```text
delete-i c/Components i/-1 q/1
```

**Expected output**

```text
Item index must be a positive integer.
```

#### Step 6

**Input**

```text
delete-i c/Components i/ q/1
```

**Expected output**

```text
Item index must be a positive integer.
```

#### Step 7

**Input**

```text
delete-i c/Components i/1 q/abc
```

**Expected output**

```text
Quantity must be a positive integer.
```

#### Step 8

**Input**

```text
delete-i c/Components i/1 q/
```

**Expected output**

```text
Quantity must be a positive integer.
```

#### Step 9

**Input**

```text
delete-i c/Components i/1 q/-5
```

**Expected output**

```text
Quantity must be greater than zero.
```

### UI-DELETE-003: Handle inventory deletion rules

**Aim:** Verify missing categories, invalid indices, insufficient stock, partial removal, exact removal, and category cleanup.

**Preconditions:** Empty inventory.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 2

**Input**

```text
delete-i c/Equipment i/1 q/1
```

**Expected output**

```text
Category "Equipment" does not exist.
```

#### Step 3

**Input**

```text
delete-i c/components i/2 q/1
```

**Expected output**

```text
Item index 2 is invalid. Category "Components" contains 1 item(s).
```

#### Step 4

**Input**

```text
delete-i c/components i/1 q/11
```

**Expected output**

```text
Cannot remove 11 Wire. Only 10 are available.
```

#### Step 5

**Input**

```text
delete-i c/components i/1 q/4
```

**Expected output**

```text
Successfully removed: 4x Wire from the inventory
```

#### Step 6

**Input**

```text
delete-i c/Components i/1 q/6
```

**Expected output**

```text
Successfully removed: 6x Wire from the inventory
```

#### Step 7

**Input**

```text
delete-i c/Components i/1 q/1
```

**Expected output**

```text
Category "Components" does not exist.
```

### UI-LIST-001: Validate list syntax and empty output

**Aim:** Verify that `list-i` rejects arguments and clearly reports an empty inventory.

**Preconditions:** Empty inventory.

#### Step 1

**Input**

```text
list-i extra
```

**Expected output**

```text
Invalid format. Use: list-i
```

#### Step 2

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
No items in inventory.
--------------------------------------------------------------------
```

### UI-LIST-002: List items deterministically

**Aim:** Verify that categories and their items are listed in insertion order with one-based indices.

**Preconditions:** Empty inventory.

#### Step 1

**Input**

```text
add-i n/Oscilloscope c/Equipment
```

**Expected output**

```text
Successfully added: 1x Oscilloscope to the inventory
```

#### Step 2

**Input**

```text
add-i n/Wire c/Components q/20
```

**Expected output**

```text
Successfully added: 20x Wire to the inventory
```

#### Step 3

**Input**

```text
add-i n/Red LED c/components q/30
```

**Expected output**

```text
Successfully added: 30x Red LED to the inventory
```

#### Step 4

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Equipment
1: Oscilloscope (Qty: 1)
Components
1: Wire (Qty: 20)
2: Red LED (Qty: 30)
--------------------------------------------------------------------
```

### UI-SESSION-ADD-001: Reject malformed session commands

**Aim:** Verify that `add-s` requires exactly one nonblank value for every prefix in the documented order.

**Preconditions:** Empty schedule.

#### Step 1

**Input**

```text
add n/Test Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/20
```

**Expected output**

```text
Invalid command
```

#### Step 2

**Input**

```text
add-s
```

**Expected output**

```text
Invalid format. Use: add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT
```

#### Step 3

**Input**

```text
add-s n/Test Lab d/{{TOMORROW}} s/0900 e/1000 p/20
```

**Expected output**

```text
Invalid format. Use: add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT
```

#### Step 4

**Input**

```text
add-s d/{{TOMORROW}} n/Test Lab l/Room-A s/0900 e/1000 p/20
```

**Expected output**

```text
Invalid format. Use: add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT
```

#### Step 5

**Input**

```text
add-s n/Test Lab n/Other Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/20
```

**Expected output**

```text
Invalid format. Use: add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT
```

#### Step 6

**Input**

```text
add-s n/ d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/20
```

**Expected output**

```text
Session name cannot be blank.
```

#### Step 7

**Input**

```text
add-s n/Test Lab d/{{TOMORROW}} l/ s/0900 e/1000 p/20
```

**Expected output**

```text
Session location cannot be blank.
```

#### Step 8

**Input**

```text
add-s n/Test Lab d/ l/Room-A s/0900 e/1000 p/20
```

**Expected output**

```text
Date must be in the format 'd MMMM yyyy' (e.g., 22 November 2027). Ensure the month is capitalized.
```

#### Step 9

**Input**

```text
add-s n/Test Lab d/{{TOMORROW}} l/Room-A s/ e/1000 p/20
```

**Expected output**

```text
Start time must be in 'HHmm' format (e.g., 0900 for 9:00 AM).
```

#### Step 10

**Input**

```text
add-s n/Test Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/20 x/extra
```

**Expected output**

```text
Invalid format. Use: add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT
```

### UI-SESSION-ADD-002: Validate session dates and times

**Aim:** Verify strict calendar dates, future-date rules, time formats, and same-day time ordering.

**Preconditions:** Empty schedule.

#### Step 1

**Input**

```text
add-s n/Test Lab d/31 February 2099 l/Room-A s/0900 e/1000 p/20
```

**Expected output**

```text
Date must be in the format 'd MMMM yyyy' (e.g., 22 November 2027). Ensure the month is capitalized.
```

#### Step 2

**Input**

```text
add-s n/Test Lab d/2099-01-01 l/Room-A s/0900 e/1000 p/20
```

**Expected output**

```text
Date must be in the format 'd MMMM yyyy' (e.g., 22 November 2027). Ensure the month is capitalized.
```

#### Step 3

**Input**

```text
add-s n/Test Lab d/{{YESTERDAY}} l/Room-A s/0900 e/1000 p/20
```

**Expected output**

```text
Session date cannot be before today.
```

#### Step 4

**Input**

```text
add-s n/Test Lab d/{{TOMORROW}} l/Room-A s/900 e/1000 p/20
```

**Expected output**

```text
Start time must be in 'HHmm' format (e.g., 0900 for 9:00 AM).
```

#### Step 5

**Input**

```text
add-s n/Test Lab d/{{TOMORROW}} l/Room-A s/0900 e/2400 p/20
```

**Expected output**

```text
End time must be in 'HHmm' format (e.g., 1200 for 12:00 PM).
```

#### Step 6

**Input**

```text
add-s n/Test Lab d/{{TOMORROW}} l/Room-A s/0900 e/0900 p/20
```

**Expected output**

```text
Start time must be earlier than end time.
```

#### Step 7

**Input**

```text
add-s n/Test Lab d/{{TOMORROW}} l/Room-A s/1000 e/0900 p/20
```

**Expected output**

```text
Start time must be earlier than end time.
```

#### Step 8

**Input**

```text
add-s n/Today Lab d/{{TODAY}} l/Room-A s/0900 e/1000 p/20
```

**Expected output**

```text
Successfully added: Today Lab on {{TODAY}} at Room-A from 0900 to 1000 for 20 attendees
```

### UI-SESSION-ADD-003: Validate session headcounts

**Aim:** Verify that headcounts are positive integers within the Java integer range.

**Preconditions:** Empty schedule.

#### Step 1

**Input**

```text
add-s n/Test Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/
```

**Expected output**

```text
Headcount must be a positive integer.
```

#### Step 2

**Input**

```text
add-s n/Test Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/abc
```

**Expected output**

```text
Headcount must be a positive integer.
```

#### Step 3

**Input**

```text
add-s n/Test Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/0
```

**Expected output**

```text
Headcount must be a positive integer.
```

#### Step 4

**Input**

```text
add-s n/Test Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/-1
```

**Expected output**

```text
Headcount must be a positive integer.
```

#### Step 5

**Input**

```text
add-s n/Test Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/2147483648
```

**Expected output**

```text
Headcount must be a positive integer.
```

### UI-SESSION-ADD-004: Enforce session conflicts

**Aim:** Verify overlap rejection by date and location while allowing adjacent sessions and simultaneous sessions elsewhere.

**Preconditions:** Empty schedule.

#### Step 1

**Input**

```text
add-s n/CG2111A Lab [03] d/{{TOMORROW}} l/E4A-04-08 s/0900 e/1200 p/50
```

**Expected output**

```text
Successfully added: CG2111A Lab [03] on {{TOMORROW}} at E4A-04-08 from 0900 to 1200 for 50 attendees
```

#### Step 2

**Input**

```text
add-s n/Overlapping Lab d/{{TOMORROW}} l/e4a-04-08 s/1100 e/1300 p/25
```

**Expected output**

```text
Session conflicts with "CG2111A Lab [03]" at E4A-04-08 on {{TOMORROW}} from 0900 to 1200.
```

#### Step 3

**Input**

```text
add-s n/Adjacent Lab d/{{TOMORROW}} l/E4A-04-08 s/1200 e/1400 p/25
```

**Expected output**

```text
Successfully added: Adjacent Lab on {{TOMORROW}} at E4A-04-08 from 1200 to 1400 for 25 attendees
```

#### Step 4

**Input**

```text
add-s n/Other Room Lab d/{{TOMORROW}} l/E4A-04-09 s/1000 e/1100 p/25
```

**Expected output**

```text
Successfully added: Other Room Lab on {{TOMORROW}} at E4A-04-09 from 1000 to 1100 for 25 attendees
```

#### Step 5

**Input**

```text
add-s n/Different Date Lab d/1 January 2099 l/E4A-04-08 s/1000 e/1100 p/25
```

**Expected output**

```text
Successfully added: Different Date Lab on 1 January 2099 at E4A-04-08 from 1000 to 1100 for 25 attendees
```

### UI-SESSION-DELETE-001: Reject invalid session deletions

**Aim:** Verify strict single-index syntax and distinguish malformed indices from out-of-range indices.

**Preconditions:** Empty schedule.

#### Step 1

**Input**

```text
delete-s
```

**Expected output**

```text
Invalid format. Use: delete-s INDEX
```

#### Step 2

**Input**

```text
delete-s 1 extra
```

**Expected output**

```text
Invalid format. Use: delete-s INDEX
```

#### Step 3

**Input**

```text
delete-s abc
```

**Expected output**

```text
Session index must be a positive integer.
```

#### Step 4

**Input**

```text
delete-s 0
```

**Expected output**

```text
Session index must be a positive integer.
```

#### Step 5

**Input**

```text
delete-s -1
```

**Expected output**

```text
Session index must be a positive integer.
```

#### Step 6

**Input**

```text
delete-s 2147483648
```

**Expected output**

```text
Session index must be a positive integer.
```

#### Step 7

**Input**

```text
delete-s 1
```

**Expected output**

```text
Session index 1 is invalid. There are 0 scheduled session(s).
```

### UI-SESSION-DELETE-002: Delete sessions and shift indices

**Aim:** Verify successful deletion and one-based index shifting after an earlier session is removed.

**Preconditions:** Empty schedule.

#### Step 1

**Input**

```text
add-s n/First Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/20
```

**Expected output**

```text
Successfully added: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 20 attendees
```

#### Step 2

**Input**

```text
add-s n/Second Lab d/{{TOMORROW}} l/Room-A s/1000 e/1100 p/25
```

**Expected output**

```text
Successfully added: Second Lab on {{TOMORROW}} at Room-A from 1000 to 1100 for 25 attendees
```

#### Step 3

**Input**

```text
delete-s 1
```

**Expected output**

```text
Successfully removed session: First Lab
```

#### Step 4

**Input**

```text
delete-s 1
```

**Expected output**

```text
Successfully removed session: Second Lab
```

#### Step 5

**Input**

```text
delete-s 1
```

**Expected output**

```text
Session index 1 is invalid. There are 0 scheduled session(s).
```

### UI-SESSION-LIST-001: Reject arguments and list an empty schedule

**Aim:** Verify that `list-s` accepts no arguments and clearly reports an empty schedule.

**Preconditions:** Empty schedule.

#### Step 1

**Input**

```text
list-s extra
```

**Expected output**

```text
Invalid format. Use: list-s
```

#### Step 2

**Input**

```text
list-s
```

**Expected output**

```text
--------------------------------------------------------------------
Sessions
--------------------------------------------------------------------
No sessions scheduled.
--------------------------------------------------------------------
```

### UI-SESSION-LIST-002: List sessions in full format and chronological order

**Aim:** Verify that sessions added in reverse time order are displayed chronologically in the full display format.

**Preconditions:** Empty schedule.

#### Step 1

**Input**

```text
add-s n/Second Lab d/{{TOMORROW}} l/Room-B s/0930 e/1030 p/25
```

**Expected output**

```text
Successfully added: Second Lab on {{TOMORROW}} at Room-B from 0930 to 1030 for 25 attendees
```

#### Step 2

**Input**

```text
add-s n/First Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/20
```

**Expected output**

```text
Successfully added: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 20 attendees
```

#### Step 3

**Input**

```text
list-s
```

**Expected output**

```text
--------------------------------------------------------------------
Sessions
--------------------------------------------------------------------
1. First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 20 attendees
2. Second Lab on {{TOMORROW}} at Room-B from 0930 to 1030 for 25 attendees
--------------------------------------------------------------------
```

### UI-PREP-001: Calculate requirements and refresh stock availability

**Aim:** Verify default quantities, headcount multiplication, shortages, and that preparation does not consume stock.

**Preconditions:** Fresh application with empty inventory and schedule.

#### Step 1

**Input**

```text
add-s n/First Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/10
```

**Expected output**

```text
Successfully added: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
```

#### Step 2

**Input**

```text
list-p s/1
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
No items in preparation.
--------------------------------------------------------------------
```

#### Step 3

**Input**

```text
add-i n/Wire c/Components q/20
```

**Expected output**

```text
Successfully added: 20x Wire to the inventory
```

#### Step 4

**Input**

```text
add-i n/Red LED c/Components q/3
```

**Expected output**

```text
Successfully added: 3x Red LED to the inventory
```

#### Step 5

**Input**

```text
add-p s/1 i/Wire c/Components q/2
```

**Expected output**

```text
Successfully added: 20x Wire [Components] to preparation
--------------------------------------------------------------------
Lab preparation: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 20, Available: 20)
--------------------------------------------------------------------
```

#### Step 6

**Input**

```text
add-p s/1 i/Red LED c/Components
```

**Expected output**

```text
Successfully added: 10x Red LED [Components] to preparation
--------------------------------------------------------------------
Lab preparation: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 20, Available: 20)
2. Red LED [Components] (Required: 10, Available: 3) !! Insufficient items (Shortfall: 7)
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
add-p s/1 i/Battery c/Components
```

**Expected output**

```text
Successfully added: 10x Battery [Components] to preparation
--------------------------------------------------------------------
Lab preparation: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 20, Available: 20)
2. Red LED [Components] (Required: 10, Available: 3) !! Insufficient items (Shortfall: 7)
3. Battery [Components] (Required: 10, Available: 0) !! Insufficient items (Shortfall: 10)
--------------------------------------------------------------------
```

#### Step 8

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 20)
2: Red LED (Qty: 3)
--------------------------------------------------------------------
```

#### Step 9

**Input**

```text
delete-p s/1 i/3 q/10
```

**Expected output**

```text
Successfully removed: 10x Battery [Components] from preparation
--------------------------------------------------------------------
Lab preparation: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 20, Available: 20)
2. Red LED [Components] (Required: 10, Available: 3) !! Insufficient items (Shortfall: 7)
--------------------------------------------------------------------
```

#### Step 10

**Input**

```text
delete-i c/Components i/1 q/10
```

**Expected output**

```text
Successfully removed: 10x Wire from the inventory
```

#### Step 11

**Input**

```text
list-p s/1
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 20, Available: 10) !! Insufficient items (Shortfall: 10)
2. Red LED [Components] (Required: 10, Available: 3) !! Insufficient items (Shortfall: 7)
--------------------------------------------------------------------
```

### UI-PREP-002: Accumulate and remove preparation quantities

**Aim:** Verify case-insensitive names, partial deletion, complete deletion, and unchanged state after excessive removal.

**Preconditions:** Fresh application with empty inventory and schedule.

#### Step 1

**Input**

```text
add-s n/First Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/10
```

**Expected output**

```text
Successfully added: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
```

#### Step 2

**Input**

```text
add-i n/Wire c/Components q/30
```

**Expected output**

```text
Successfully added: 30x Wire to the inventory
```

#### Step 3

**Input**

```text
add-p s/1 i/Wire c/Components q/2
```

**Expected output**

```text
Successfully added: 20x Wire [Components] to preparation
--------------------------------------------------------------------
Lab preparation: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 20, Available: 30)
--------------------------------------------------------------------
```

#### Step 4

**Input**

```text
add-p s/1 i/wIRE c/Components
```

**Expected output**

```text
Successfully added: 10x Wire [Components] to preparation
--------------------------------------------------------------------
Lab preparation: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 30, Available: 30)
--------------------------------------------------------------------
```

#### Step 5

**Input**

```text
delete-p s/1 i/1 q/10
```

**Expected output**

```text
Successfully removed: 10x Wire [Components] from preparation
--------------------------------------------------------------------
Lab preparation: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 20, Available: 30)
--------------------------------------------------------------------
```

#### Step 6

**Input**

```text
delete-p s/1 i/1 q/21
```

**Expected output**

```text
Cannot remove 21x Wire [Components] from preparation; only 20 required.
```

#### Step 7

**Input**

```text
list-p s/1
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 20, Available: 30)
--------------------------------------------------------------------
```

#### Step 8

**Input**

```text
delete-p s/1 i/1 q/20
```

**Expected output**

```text
Successfully removed: 20x Wire [Components] from preparation
--------------------------------------------------------------------
Lab preparation: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
No items in preparation.
--------------------------------------------------------------------
```

#### Step 9

**Input**

```text
delete-p s/1 i/1 q/1
```

**Expected output**

```text
Preparation item index 1 is invalid. There are 0 item(s) in this preparation.
```

### UI-PREP-003: Reject invalid preparation arguments

**Aim:** Verify malformed commands, invalid indices and quantities, and overflow do not change preparation.

**Preconditions:** Fresh application with empty inventory and schedule.

#### Step 1

**Input**

```text
list-p s/1
```

**Expected output**

```text
Session index 1 is invalid. There are 0 scheduled session(s).
```

#### Step 2

**Input**

```text
add-s n/First Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/10
```

**Expected output**

```text
Successfully added: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
```

#### Step 3

**Input**

```text
add-p
```

**Expected output**

```text
Invalid format. Use: add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]
```

#### Step 4

**Input**

```text
add-p i/Wire s/1 c/Components
```

**Expected output**

```text
Invalid format. Use: add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]
```

#### Step 5

**Input**

```text
add-p s/1 i/Wire i/Battery c/Components
```

**Expected output**

```text
Invalid format. Use: add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]
```

#### Step 6

**Input**

```text
add-p s/1 i/ c/Components
```

**Expected output**

```text
Item name cannot be blank.
```

#### Step 7

**Input**

```text
add-p s/0 i/Wire c/Components
```

**Expected output**

```text
Session index must be a positive integer.
```

#### Step 8

**Input**

```text
add-p s/2 i/Wire c/Components
```

**Expected output**

```text
Session index 2 is invalid. There are 1 scheduled session(s).
```

#### Step 9

**Input**

```text
add-p s/1 i/Wire c/Components q/0
```

**Expected output**

```text
Quantity must be greater than zero.
```

#### Step 10

**Input**

```text
add-p s/1 i/Wire c/Components q/1.5
```

**Expected output**

```text
Quantity must be a positive integer.
```

#### Step 11

**Input**

```text
add-p s/1 i/Wire c/Components q/2147483648
```

**Expected output**

```text
Quantity must be a positive integer.
```

#### Step 12

**Input**

```text
add-p s/1 i/Wire c/Components q/2147483647
```

**Expected output**

```text
Required quantity must not exceed 2147483647.
```

#### Step 13

**Input**

```text
list-p s/1
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
No items in preparation.
--------------------------------------------------------------------
```

#### Step 14

**Input**

```text
list-p
```

**Expected output**

```text
Invalid format. Use: list-p s/SESSION_INDEX
```

#### Step 15

**Input**

```text
list-p s/1 q/1
```

**Expected output**

```text
Invalid format. Use: list-p s/SESSION_INDEX
```

#### Step 16

**Input**

```text
delete-p s/1 i/1
```

**Expected output**

```text
Invalid format. Use: delete-p s/SESSION_INDEX i/ITEM_INDEX q/TOTAL_QUANTITY
```

#### Step 17

**Input**

```text
delete-p s/1 i/0 q/1
```

**Expected output**

```text
Item index must be a positive integer.
```

#### Step 18

**Input**

```text
delete-p s/1 i/1 q/-1
```

**Expected output**

```text
Quantity must be greater than zero.
```

### UI-PREP-004: Keep preparation attached to its session

**Aim:** Verify independent sessions retain their preparation when preceding sessions are deleted.

**Preconditions:** Fresh application with empty inventory and schedule.

#### Step 1

**Input**

```text
add-s n/First Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/10
```

**Expected output**

```text
Successfully added: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
```

#### Step 2

**Input**

```text
add-s n/Second Lab d/{{TOMORROW}} l/Room-B s/0900 e/1000 p/5
```

**Expected output**

```text
Successfully added: Second Lab on {{TOMORROW}} at Room-B from 0900 to 1000 for 5 attendees
```

#### Step 3

**Input**

```text
add-p s/1 i/Wire c/Components
```

**Expected output**

```text
Successfully added: 10x Wire [Components] to preparation
--------------------------------------------------------------------
Lab preparation: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 10, Available: 0) !! Insufficient items (Shortfall: 10)
--------------------------------------------------------------------
```

#### Step 4

**Input**

```text
list-p s/2
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: Second Lab on {{TOMORROW}} at Room-B from 0900 to 1000 for 5 attendees
--------------------------------------------------------------------
No items in preparation.
--------------------------------------------------------------------
```

#### Step 5

**Input**

```text
delete-s 1
```

**Expected output**

```text
Successfully removed session: First Lab
```

#### Step 6

**Input**

```text
list-p s/1
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: Second Lab on {{TOMORROW}} at Room-B from 0900 to 1000 for 5 attendees
--------------------------------------------------------------------
No items in preparation.
--------------------------------------------------------------------
```

### UI-PREP-005: Distinguish categories and match case-insensitively

**Aim:** Ensure stock in a different category cannot hide a shortage, category is required, and removal uses displayed positions.

**Preconditions:** Fresh application with empty inventory and schedule.

#### Step 1

**Input**

```text
add-s n/First Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/10
```

**Expected output**

```text
Successfully added: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
```

#### Step 2

**Input**

```text
add-i n/Wire c/Components q/3
```

**Expected output**

```text
Successfully added: 3x Wire to the inventory
```

#### Step 3

**Input**

```text
add-i n/Wire c/Spares q/100
```

**Expected output**

```text
Successfully added: 100x Wire to the inventory
```

#### Step 4

**Input**

```text
add-p s/1 i/Wire
```

**Expected output**

```text
Invalid format. Use: add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]
```

#### Step 5

**Input**

```text
add-p s/1 i/Wire c/
```

**Expected output**

```text
Category cannot be blank.
```

#### Step 6

**Input**

```text
add-p s/1 i/Wire c/Components
```

**Expected output**

```text
Successfully added: 10x Wire [Components] to preparation
--------------------------------------------------------------------
Lab preparation: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 10, Available: 3) !! Insufficient items (Shortfall: 7)
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
add-p s/1 i/wire c/spares q/2
```

**Expected output**

```text
Successfully added: 20x wire [spares] to preparation
--------------------------------------------------------------------
Lab preparation: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 10, Available: 3) !! Insufficient items (Shortfall: 7)
2. wire [spares] (Required: 20, Available: 100)
--------------------------------------------------------------------
```

#### Step 8

**Input**

```text
list-p s/1
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 10, Available: 3) !! Insufficient items (Shortfall: 7)
2. wire [spares] (Required: 20, Available: 100)
--------------------------------------------------------------------
```

#### Step 9

**Input**

```text
delete-p s/1 i/1 q/10
```

**Expected output**

```text
Successfully removed: 10x Wire [Components] from preparation
--------------------------------------------------------------------
Lab preparation: First Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. wire [spares] (Required: 20, Available: 100)
--------------------------------------------------------------------
```

### UI-HELP-001: List all commands

**Aim:** Verify that help prints every command and its format, grouped by feature.

**Preconditions:** None.

#### Step 1

**Input**

```text
help
```

**Expected output**

```text
--------------------------------------------------------------------
Available commands
--------------------------------------------------------------------
Inventory
  add-i n/NAME c/CATEGORY [q/QUANTITY]
  delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY
  list-i
  find-i KEYWORD
Lab sessions
  add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT
  delete-s INDEX
  list-s
Lab preparation
  add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]
  delete-p s/SESSION_INDEX i/ITEM_INDEX q/TOTAL_QUANTITY
  list-p s/SESSION_INDEX
Student requests
  add-r n/STUDENT_NAME i/ITEM q/QUANTITY d/DEADLINE
  delete-r INDEX
  list-r
  mark-r INDEX STATUS (prepared or fulfilled)
Contacts
  add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]
  delete-c INDEX
  list-c
Loans
  add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE
  list-l [overdue|history|returned]
  delete-l INDEX
  return INDEX
Broken items
  add-b n/NAME c/CATEGORY q/QUANTITY f/FAULT d/DATE_REPORTED [m/CONTACT_INDEX]
  list-b
  delete-b INDEX
    Deletion restores all units: use only for full repairs or incorrect reports.
Other
  help
  Bye
--------------------------------------------------------------------
```

#### Step 2

**Input**

```text
help
```

**Expected output**

```text
--------------------------------------------------------------------
Available commands
--------------------------------------------------------------------
Inventory
  add-i n/NAME c/CATEGORY [q/QUANTITY]
  delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY
  list-i
  find-i KEYWORD
Lab sessions
  add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT
  delete-s INDEX
  list-s
Lab preparation
  add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]
  delete-p s/SESSION_INDEX i/ITEM_INDEX q/TOTAL_QUANTITY
  list-p s/SESSION_INDEX
Student requests
  add-r n/STUDENT_NAME i/ITEM q/QUANTITY d/DEADLINE
  delete-r INDEX
  list-r
  mark-r INDEX STATUS (prepared or fulfilled)
Contacts
  add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]
  delete-c INDEX
  list-c
Loans
  add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE
  list-l [overdue|history|returned]
  delete-l INDEX
  return INDEX
Broken items
  add-b n/NAME c/CATEGORY q/QUANTITY f/FAULT d/DATE_REPORTED [m/CONTACT_INDEX]
  list-b
  delete-b INDEX
    Deletion restores all units: use only for full repairs or incorrect reports.
Other
  help
  Bye
--------------------------------------------------------------------
```


### UI-HELP-002: Reject help arguments and wrong command words

**Aim:** Verify that help accepts no arguments and is case-sensitive.

**Preconditions:** None.

#### Step 1

**Input**

```text
help me
```

**Expected output**

```text
Invalid format. Use: help
```

#### Step 2

**Input**

```text
HELP
```

**Expected output**

```text
Invalid command
```

#### Step 3

**Input**

```text
helps
```

**Expected output**

```text
Invalid command
```


### UI-LIFECYCLE-002: Exit using Bye

**Aim:** Verify that the explicit exit command produces no stdout and exits successfully.

**Preconditions:** Fresh application with empty inventory, schedule, and contacts.

#### Step 1

**Input**

```text
Bye
```

**Expected output (no output)**

```text
```

### UI-LIFECYCLE-003: Exit command ignores case and surrounding spaces

**Aim:** Verify that the exit command accepts capitalization and surrounding whitespace.

**Preconditions:** Fresh application with empty inventory, schedule, and contacts.

#### Step 1

**Input**

```text
␠␠BYE␠␠
```

**Expected output (no output)**

```text
```

### UI-CONTACT-ADD-001: Add contacts with every optional-field combination and duplicates

**Aim:** Verify required-only, email-only, role-only, and full contacts, leading-zero phones, and independent duplicate entries.

**Preconditions:** Fresh application with empty inventory, schedule, and contacts.

#### Step 1

**Input**

```text
add-c n/NUS Infrastructure t/65161234 r/Air conditioning and lighting repairs
```

**Expected output**

```text
Successfully added: NUS Infrastructure (65161234) to contacts
```

#### Step 2

**Input**

```text
add-c n/Component Supplier t/91234567 e/supplier@example.com r/Resistors, LEDs, wires
```

**Expected output**

```text
Successfully added: Component Supplier (91234567) to contacts
```

#### Step 3

**Input**

```text
add-c n/Email Only t/12345678 e/a@b.com
```

**Expected output**

```text
Successfully added: Email Only (12345678) to contacts
```

#### Step 4

**Input**

```text
add-c n/Bare t/00123456
```

**Expected output**

```text
Successfully added: Bare (00123456) to contacts
```

#### Step 5

**Input**

```text
add-c n/Bare t/00123456
```

**Expected output**

```text
Successfully added: Bare (00123456) to contacts
```

#### Step 6

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
1. NUS Infrastructure (65161234) - Air conditioning and lighting repairs
2. Component Supplier (91234567, supplier@example.com) - Resistors, LEDs, wires
3. Email Only (12345678, a@b.com)
4. Bare (00123456)
5. Bare (00123456)
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
delete-c 4
```

**Expected output**

```text
Successfully removed contact: Bare
```

#### Step 8

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
1. NUS Infrastructure (65161234) - Air conditioning and lighting repairs
2. Component Supplier (91234567, supplier@example.com) - Resistors, LEDs, wires
3. Email Only (12345678, a@b.com)
4. Bare (00123456)
--------------------------------------------------------------------
```

### UI-CONTACT-ADD-002: Reject malformed contact commands without adding entries

**Aim:** Verify missing, reordered, repeated, unknown, and embedded prefixes, and case-sensitive command words.

**Preconditions:** Fresh application with empty inventory, schedule, and contacts.

#### Step 1

**Input**

```text
add-c
```

**Expected output**

```text
Invalid format. Use: add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]
```

#### Step 2

**Input**

```text
add-c n/Alex
```

**Expected output**

```text
Invalid format. Use: add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]
```

#### Step 3

**Input**

```text
add-c t/91234567
```

**Expected output**

```text
Invalid format. Use: add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]
```

#### Step 4

**Input**

```text
add-c t/91234567 n/Alex
```

**Expected output**

```text
Invalid format. Use: add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]
```

#### Step 5

**Input**

```text
add-c n/Alex t/91234567 r/Role e/a@b.com
```

**Expected output**

```text
Invalid format. Use: add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]
```

#### Step 6

**Input**

```text
add-c n/Alex t/91234567 x/1
```

**Expected output**

```text
Invalid format. Use: add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]
```

#### Step 7

**Input**

```text
add-c n/Alex t/91234567 t/91234567
```

**Expected output**

```text
Invalid format. Use: add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]
```

#### Step 8

**Input**

```text
add-c extra n/Alex t/91234567
```

**Expected output**

```text
Invalid format. Use: add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]
```

#### Step 9

**Input**

```text
add-c n/Alex t/91234567 r/Repairs a/b
```

**Expected output**

```text
Invalid format. Use: add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]
```

#### Step 10

**Input**

```text
ADD-C n/Alex t/91234567
```

**Expected output**

```text
Invalid command
```

#### Step 11

**Input**

```text
add-contact n/Alex t/91234567
```

**Expected output**

```text
Invalid command
```

#### Step 12

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
No contacts saved.
--------------------------------------------------------------------
```

### UI-CONTACT-ADD-003: Validate contact values without changing state

**Aim:** Verify nonblank values, digit-only phones, email validation, and unchanged state after rejected additions.

**Preconditions:** Fresh application with empty inventory, schedule, and contacts.

#### Step 1

**Input**

```text
add-c n/ t/91234567
```

**Expected output**

```text
Contact name cannot be blank.
```

#### Step 2

**Input**

```text
add-c n/Alex t/
```

**Expected output**

```text
Phone cannot be blank.
```

#### Step 3

**Input**

```text
add-c n/Alex t/abc
```

**Expected output**

```text
Phone must contain digits only.
```

#### Step 4

**Input**

```text
add-c n/Alex t/9123 4567
```

**Expected output**

```text
Phone must contain digits only.
```

#### Step 5

**Input**

```text
add-c n/Alex t/+6591234567
```

**Expected output**

```text
Phone must contain digits only.
```

#### Step 6

**Input**

```text
add-c n/Alex t/9123-4567
```

**Expected output**

```text
Phone must contain digits only.
```

#### Step 7

**Input**

```text
add-c n/Alex t/91234567 e/
```

**Expected output**

```text
Email cannot be blank.
```

#### Step 8

**Input**

```text
add-c n/Alex t/91234567 e/nope
```

**Expected output**

```text
Email must be a valid address, e.g. name@example.com.
```

#### Step 9

**Input**

```text
add-c n/Alex t/91234567 e/a@b .com
```

**Expected output**

```text
Email must be a valid address, e.g. name@example.com.
```

#### Step 10

**Input**

```text
add-c n/Alex t/91234567 r/
```

**Expected output**

```text
Role cannot be blank.
```

#### Step 11

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
No contacts saved.
--------------------------------------------------------------------
```

### UI-CONTACT-LIST-001: List empty and populated contacts in insertion order

**Aim:** Verify list syntax, empty output, and deterministic display independent of alphabetical order.

**Preconditions:** Fresh application with empty inventory, schedule, and contacts.

#### Step 1

**Input**

```text
list-c extra
```

**Expected output**

```text
Invalid format. Use: list-c
```

#### Step 2

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
No contacts saved.
--------------------------------------------------------------------
```

#### Step 3

**Input**

```text
add-c n/Zeta Supplier t/11111111
```

**Expected output**

```text
Successfully added: Zeta Supplier (11111111) to contacts
```

#### Step 4

**Input**

```text
add-c n/Alpha Technician t/22222222 r/Repairs
```

**Expected output**

```text
Successfully added: Alpha Technician (22222222) to contacts
```

#### Step 5

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
1. Zeta Supplier (11111111)
2. Alpha Technician (22222222) - Repairs
--------------------------------------------------------------------
```

### UI-CONTACT-DELETE-001: Reject invalid contact deletions without changing state

**Aim:** Verify single-index syntax, numeric boundaries, empty and populated list bounds, and unchanged contacts.

**Preconditions:** Fresh application with empty inventory, schedule, and contacts.

#### Step 1

**Input**

```text
delete-c 1
```

**Expected output**

```text
Contact index 1 is invalid. There are 0 contact(s).
```

#### Step 2

**Input**

```text
add-c n/First t/11111111
```

**Expected output**

```text
Successfully added: First (11111111) to contacts
```

#### Step 3

**Input**

```text
delete-c
```

**Expected output**

```text
Invalid format. Use: delete-c INDEX
```

#### Step 4

**Input**

```text
delete-c 1 2
```

**Expected output**

```text
Invalid format. Use: delete-c INDEX
```

#### Step 5

**Input**

```text
delete-c abc
```

**Expected output**

```text
Contact index must be a positive integer.
```

#### Step 6

**Input**

```text
delete-c 0
```

**Expected output**

```text
Contact index must be a positive integer.
```

#### Step 7

**Input**

```text
delete-c -1
```

**Expected output**

```text
Contact index must be a positive integer.
```

#### Step 8

**Input**

```text
delete-c 1.5
```

**Expected output**

```text
Contact index must be a positive integer.
```

#### Step 9

**Input**

```text
delete-c 2147483648
```

**Expected output**

```text
Contact index must be a positive integer.
```

#### Step 10

**Input**

```text
delete-c 2
```

**Expected output**

```text
Contact index 2 is invalid. There are 1 contact(s).
```

#### Step 11

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
1. First (11111111)
--------------------------------------------------------------------
```

### UI-CONTACT-DELETE-002: Delete contacts and renumber the remaining entries

**Aim:** Verify middle, last, and final-entry deletion without gaps or stale indices.

**Preconditions:** Fresh application with empty inventory, schedule, and contacts.

#### Step 1

**Input**

```text
add-c n/First t/11111111
```

**Expected output**

```text
Successfully added: First (11111111) to contacts
```

#### Step 2

**Input**

```text
add-c n/Second t/22222222
```

**Expected output**

```text
Successfully added: Second (22222222) to contacts
```

#### Step 3

**Input**

```text
add-c n/Third t/33333333
```

**Expected output**

```text
Successfully added: Third (33333333) to contacts
```

#### Step 4

**Input**

```text
add-c n/Fourth t/44444444
```

**Expected output**

```text
Successfully added: Fourth (44444444) to contacts
```

#### Step 5

**Input**

```text
delete-c 2
```

**Expected output**

```text
Successfully removed contact: Second
```

#### Step 6

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
1. First (11111111)
2. Third (33333333)
3. Fourth (44444444)
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
delete-c 3
```

**Expected output**

```text
Successfully removed contact: Fourth
```

#### Step 8

**Input**

```text
delete-c 1
```

**Expected output**

```text
Successfully removed contact: First
```

#### Step 9

**Input**

```text
delete-c 1
```

**Expected output**

```text
Successfully removed contact: Third
```

#### Step 10

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
No contacts saved.
--------------------------------------------------------------------
```

#### Step 11

**Input**

```text
delete-c 1
```

**Expected output**

```text
Contact index 1 is invalid. There are 0 contact(s).
```

### UI-CONTACT-ISOLATION-001: Contact changes preserve inventory and preparation

**Aim:** Verify supplier and technician contact operations cannot alter inventory, sessions, or preparation.

**Preconditions:** Fresh application with empty inventory, schedule, and contacts.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/20
```

**Expected output**

```text
Successfully added: 20x Wire to the inventory
```

#### Step 2

**Input**

```text
add-s n/Baseline Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/10
```

**Expected output**

```text
Successfully added: Baseline Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
```

#### Step 3

**Input**

```text
add-p s/1 i/Wire c/Components
```

**Expected output**

```text
Successfully added: 10x Wire [Components] to preparation
--------------------------------------------------------------------
Lab preparation: Baseline Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 10, Available: 20)
--------------------------------------------------------------------
```

#### Step 4

**Input**

```text
add-c n/Technician t/91234567 r/Equipment repairs
```

**Expected output**

```text
Successfully added: Technician (91234567) to contacts
```

#### Step 5

**Input**

```text
delete-c 1
```

**Expected output**

```text
Successfully removed contact: Technician
```

#### Step 6

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
No contacts saved.
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 20)
--------------------------------------------------------------------
```

#### Step 8

**Input**

```text
list-s
```

**Expected output**

```text
--------------------------------------------------------------------
Sessions
--------------------------------------------------------------------
1. Baseline Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
```

#### Step 9

**Input**

```text
list-p s/1
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: Baseline Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 10, Available: 20)
--------------------------------------------------------------------
```

### UI-SESSION-LIST-003: Sort sessions by date, time, and name

**Aim:** Verify the chronological ordering contract, including alphabetical ties, using out-of-order additions.

**Preconditions:** Fresh application with empty inventory, schedule, and contacts.

#### Step 1

**Input**

```text
add-s n/Tomorrow Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/10
```

**Expected output**

```text
Successfully added: Tomorrow Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
```

#### Step 2

**Input**

```text
add-s n/Zulu Lab d/{{TODAY}} l/Room-A s/1000 e/1100 p/10
```

**Expected output**

```text
Successfully added: Zulu Lab on {{TODAY}} at Room-A from 1000 to 1100 for 10 attendees
```

#### Step 3

**Input**

```text
add-s n/alpha Lab d/{{TODAY}} l/Room-B s/1000 e/1100 p/10
```

**Expected output**

```text
Successfully added: alpha Lab on {{TODAY}} at Room-B from 1000 to 1100 for 10 attendees
```

#### Step 4

**Input**

```text
add-s n/Earlier Lab d/{{TODAY}} l/Room-A s/0900 e/1000 p/10
```

**Expected output**

```text
Successfully added: Earlier Lab on {{TODAY}} at Room-A from 0900 to 1000 for 10 attendees
```

#### Step 5

**Input**

```text
list-s
```

**Expected output**

```text
--------------------------------------------------------------------
Sessions
--------------------------------------------------------------------
1. Earlier Lab on {{TODAY}} at Room-A from 0900 to 1000 for 10 attendees
2. alpha Lab on {{TODAY}} at Room-B from 1000 to 1100 for 10 attendees
3. Zulu Lab on {{TODAY}} at Room-A from 1000 to 1100 for 10 attendees
4. Tomorrow Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
```

### UI-PREP-006: Keep preparation attached when an earlier session is inserted

**Aim:** Verify session sorting changes indices without moving preparation between sessions.

**Preconditions:** Fresh application with empty inventory, schedule, and contacts.

#### Step 1

**Input**

```text
add-s n/Later Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/10
```

**Expected output**

```text
Successfully added: Later Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
```

#### Step 2

**Input**

```text
add-p s/1 i/Wire c/Components q/2
```

**Expected output**

```text
Successfully added: 20x Wire [Components] to preparation
--------------------------------------------------------------------
Lab preparation: Later Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 20, Available: 0) !! Insufficient items (Shortfall: 20)
--------------------------------------------------------------------
```

#### Step 3

**Input**

```text
add-s n/Earlier Lab d/{{TODAY}} l/Room-A s/0900 e/1000 p/10
```

**Expected output**

```text
Successfully added: Earlier Lab on {{TODAY}} at Room-A from 0900 to 1000 for 10 attendees
```

#### Step 4

**Input**

```text
list-p s/1
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: Earlier Lab on {{TODAY}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
No items in preparation.
--------------------------------------------------------------------
```

#### Step 5

**Input**

```text
list-p s/2
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: Later Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 20, Available: 0) !! Insufficient items (Shortfall: 20)
--------------------------------------------------------------------
```

#### Step 6

**Input**

```text
delete-s 1
```

**Expected output**

```text
Successfully removed session: Earlier Lab
```

#### Step 7

**Input**

```text
list-p s/1
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: Later Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 20, Available: 0) !! Insufficient items (Shortfall: 20)
--------------------------------------------------------------------
```

### UI-LOAN-LIST-001: Validate loan listing and show an empty list

**Aim:** Verify empty active/overdue output, reject unsupported arguments, and retain case-sensitive commands.

**Preconditions:** Fresh application with empty inventory, loans, sessions, and supplier contacts.

#### Step 1

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
No active loans.
--------------------------------------------------------------------
```

#### Step 2

**Input**

```text
list-l extra
```

**Expected output**

```text
Invalid format. Use: list-l [overdue|history|returned]
```

#### Step 3

**Input**

```text
list-l overdue
```

**Expected output**

```text
--------------------------------------------------------------------
Overdue loans
--------------------------------------------------------------------
No overdue loans.
--------------------------------------------------------------------
```

#### Step 4

**Input**

```text
LIST-L
```

**Expected output**

```text
Invalid command
```

### UI-LOAN-ADD-001: Add independent loans and deduct available stock

**Aim:** Verify complete details, separate repeated loans, multiple borrowers/due dates, and live available inventory.

**Preconditions:** Fresh application with empty inventory, loans, sessions, and supplier contacts.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 2

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex Tan t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Successfully added loan: 2x Wire [Components] to Alex Tan (91234567), due {{TOMORROW}}
```

#### Step 3

**Input**

```text
add-l n/Wire c/Components q/1 b/Alex Tan t/91234567 d/{{TODAY}}
```

**Expected output**

```text
Successfully added loan: 1x Wire [Components] to Alex Tan (91234567), due {{TODAY}}
```

#### Step 4

**Input**

```text
add-l n/Wire c/Components q/3 b/Sam Lee t/00123456 d/1 January 2099
```

**Expected output**

```text
Successfully added loan: 3x Wire [Components] to Sam Lee (00123456), due 1 January 2099
```

#### Step 5

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 2x Wire [Components] to Alex Tan (91234567), due {{TOMORROW}}
2. 1x Wire [Components] to Alex Tan (91234567), due {{TODAY}}
3. 3x Wire [Components] to Sam Lee (00123456), due 1 January 2099
--------------------------------------------------------------------
```

#### Step 6

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 4)
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
No contacts saved.
--------------------------------------------------------------------
```

### UI-LOAN-ADD-002: Reject malformed loan commands without changing state

**Aim:** Verify required fixed-order prefixes, missing, repeated, embedded and unexpected arguments, and case sensitivity.

**Preconditions:** Fresh application with empty inventory, loans, sessions, and supplier contacts.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 2

**Input**

```text
add-l
```

**Expected output**

```text
Invalid format. Use: add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE
```

#### Step 3

**Input**

```text
add-l n/Wire c/Components b/Alex t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Invalid format. Use: add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE
```

#### Step 4

**Input**

```text
add-l c/Components n/Wire q/1 b/Alex t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Invalid format. Use: add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE
```

#### Step 5

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex Tan t/91234567 d/{{TOMORROW}} q/1
```

**Expected output**

```text
Invalid format. Use: add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE
```

#### Step 6

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex Tan t/91234567 d/{{TOMORROW}} x/extra
```

**Expected output**

```text
Invalid format. Use: add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE
```

#### Step 7

**Input**

```text
add-l extra n/Wire c/Components q/1 b/Alex t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Invalid format. Use: add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE
```

#### Step 8

**Input**

```text
add-l n/Wire c/Components q/1 b/Alex t/91234567
```

**Expected output**

```text
Invalid format. Use: add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE
```

#### Step 9

**Input**

```text
add-l n/Wire c/Components q/1 b/Alex a/b t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Invalid format. Use: add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE
```

#### Step 10

**Input**

```text
ADD-L n/Wire c/Components q/2 b/Alex Tan t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Invalid command
```

#### Step 11

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 10)
--------------------------------------------------------------------
```

#### Step 12

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
No active loans.
--------------------------------------------------------------------
```

### UI-LOAN-ADD-003: Reject invalid quantities and borrower details

**Aim:** Verify all invalid loan fields leave stock, active loans, and supplier contacts unchanged.

**Preconditions:** Fresh application with empty inventory, loans, sessions, and supplier contacts.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 2

**Input**

```text
add-l n/ c/Components q/2 b/Alex Tan t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Item name cannot be blank.
```

#### Step 3

**Input**

```text
add-l n/Wire c/ q/2 b/Alex Tan t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Category cannot be blank.
```

#### Step 4

**Input**

```text
add-l n/Wire c/Components q/ b/Alex Tan t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Quantity must be a positive integer.
```

#### Step 5

**Input**

```text
add-l n/Wire c/Components q/0 b/Alex Tan t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Quantity must be greater than zero.
```

#### Step 6

**Input**

```text
add-l n/Wire c/Components q/-1 b/Alex Tan t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Quantity must be greater than zero.
```

#### Step 7

**Input**

```text
add-l n/Wire c/Components q/abc b/Alex Tan t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Quantity must be a positive integer.
```

#### Step 8

**Input**

```text
add-l n/Wire c/Components q/1.5 b/Alex Tan t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Quantity must be a positive integer.
```

#### Step 9

**Input**

```text
add-l n/Wire c/Components q/2147483648 b/Alex Tan t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Quantity must be a positive integer.
```

#### Step 10

**Input**

```text
add-l n/Wire c/Components q/2 b/ t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Contact name cannot be blank.
```

#### Step 11

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex Tan t/ d/{{TOMORROW}}
```

**Expected output**

```text
Phone cannot be blank.
```

#### Step 12

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex Tan t/+6591234567 d/{{TOMORROW}}
```

**Expected output**

```text
Phone must contain digits only.
```

#### Step 13

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex Tan t/9123 4567 d/{{TOMORROW}}
```

**Expected output**

```text
Phone must contain digits only.
```

#### Step 14

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex Tan t/abc d/{{TOMORROW}}
```

**Expected output**

```text
Phone must contain digits only.
```

#### Step 15

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 10)
--------------------------------------------------------------------
```

#### Step 16

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
No active loans.
--------------------------------------------------------------------
```

#### Step 17

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
No contacts saved.
--------------------------------------------------------------------
```

### UI-LOAN-ADD-004: Validate dates and accept backlog loans

**Aim:** Verify strict dates and backlog acceptance, including the phase 4 warning and marker for past due dates.

**Preconditions:** Fresh application with empty inventory, loans, sessions, and supplier contacts.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 2

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex Tan t/91234567 d/
```

**Expected output**

```text
Due date must be a valid date in d MMMM yyyy format (e.g., 30 October 2099).
```

#### Step 3

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex Tan t/91234567 d/31 February 2099
```

**Expected output**

```text
Due date must be a valid date in d MMMM yyyy format (e.g., 30 October 2099).
```

#### Step 4

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex Tan t/91234567 d/2099-10-30
```

**Expected output**

```text
Due date must be a valid date in d MMMM yyyy format (e.g., 30 October 2099).
```

#### Step 5

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex Tan t/91234567 d/30 october 2099
```

**Expected output**

```text
Due date must be a valid date in d MMMM yyyy format (e.g., 30 October 2099).
```

#### Step 6

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex Tan t/91234567 d/30 Oct 2099
```

**Expected output**

```text
Due date must be a valid date in d MMMM yyyy format (e.g., 30 October 2099).
```

#### Step 7

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex Tan t/91234567 d/29 February 2099
```

**Expected output**

```text
Due date must be a valid date in d MMMM yyyy format (e.g., 30 October 2099).
```

#### Step 8

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex Tan t/91234567 d/{{YESTERDAY}}
```

**Expected output**

```text
Successfully added loan: 2x Wire [Components] to Alex Tan (91234567), due {{YESTERDAY}}
Warning: this loan is already overdue.
```

#### Step 9

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 2x Wire [Components] to Alex Tan (91234567), due {{YESTERDAY}} [OVERDUE]
--------------------------------------------------------------------
```

#### Step 10

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 8)
--------------------------------------------------------------------
```

### UI-LOAN-ADD-005: Reject missing items and insufficient stock

**Aim:** Verify exact name/category selection, case-insensitive matching, full depletion, and independent loan quantities.

**Preconditions:** Fresh application with empty inventory, loans, sessions, and supplier contacts.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 2

**Input**

```text
add-i n/Wire c/Spares q/3
```

**Expected output**

```text
Successfully added: 3x Wire to the inventory
```

#### Step 3

**Input**

```text
add-l n/Wire c/Unknown q/2 b/Alex Tan t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Category "Unknown" does not exist.
```

#### Step 4

**Input**

```text
add-l n/Battery c/Components q/2 b/Alex Tan t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Item "Battery" does not exist in category "Components".
```

#### Step 5

**Input**

```text
add-l n/Wire c/Components q/11 b/Alex Tan t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Cannot loan 11 Wire. Only 10 are available.
```

#### Step 6

**Input**

```text
add-l n/Wire c/Spares q/4 b/Alex Tan t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Cannot loan 4 Wire. Only 3 are available.
```

#### Step 7

**Input**

```text
add-l n/wire c/COMPONENTS q/10 b/Alex Tan t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Successfully added loan: 10x wire [COMPONENTS] to Alex Tan (91234567), due {{TOMORROW}}
```

#### Step 8

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Spares
1: Wire (Qty: 3)
--------------------------------------------------------------------
```

#### Step 9

**Input**

```text
add-i n/Wire c/Components q/1
```

**Expected output**

```text
Successfully added: 1x Wire to the inventory
```

#### Step 10

**Input**

```text
delete-i c/Components i/1 q/1
```

**Expected output**

```text
Successfully removed: 1x Wire from the inventory
```

#### Step 11

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 10x wire [COMPONENTS] to Alex Tan (91234567), due {{TOMORROW}}
--------------------------------------------------------------------
```

### UI-LOAN-ADD-006: Refresh preparation availability and isolate borrower contacts

**Aim:** Verify lending affects preparation availability, supplier contact deletion does not affect borrowers, and listing never deducts twice.

**Preconditions:** Fresh application with empty inventory, loans, sessions, and supplier contacts.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 2

**Input**

```text
add-s n/Loan Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/10
```

**Expected output**

```text
Successfully added: Loan Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
```

#### Step 3

**Input**

```text
add-p s/1 i/Wire c/Components
```

**Expected output**

```text
Successfully added: 10x Wire [Components] to preparation
--------------------------------------------------------------------
Lab preparation: Loan Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 10, Available: 10)
--------------------------------------------------------------------
```

#### Step 4

**Input**

```text
add-c n/Alex Tan t/91234567 r/Supplier
```

**Expected output**

```text
Successfully added: Alex Tan (91234567) to contacts
```

#### Step 5

**Input**

```text
add-l n/Wire c/Components q/3 b/Alex Tan t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Successfully added loan: 3x Wire [Components] to Alex Tan (91234567), due {{TOMORROW}}
```

#### Step 6

**Input**

```text
list-p s/1
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: Loan Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 10, Available: 7) !! Insufficient items (Shortfall: 3)
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
delete-c 1
```

**Expected output**

```text
Successfully removed contact: Alex Tan
```

#### Step 8

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 3x Wire [Components] to Alex Tan (91234567), due {{TOMORROW}}
--------------------------------------------------------------------
```

#### Step 9

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 3x Wire [Components] to Alex Tan (91234567), due {{TOMORROW}}
--------------------------------------------------------------------
```

#### Step 10

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
No contacts saved.
--------------------------------------------------------------------
```

#### Step 11

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 7)
--------------------------------------------------------------------
```

#### Step 12

**Input**

```text
list-s
```

**Expected output**

```text
--------------------------------------------------------------------
Sessions
--------------------------------------------------------------------
1. Loan Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
```

### UI-LOAN-DELETE-001: Reject malformed and invalid loan deletions

**Aim:** Verify plain positive index syntax and bounds; failed commands preserve stock and active loans.

**Preconditions:** Fresh application with empty inventory, loans, supplier contacts, and sessions.

#### Step 1

**Input**

```text
delete-l 1
```

**Expected output**

```text
Loan index 1 is invalid. There are 0 active loan(s).
```

#### Step 2

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 3

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex t/91234567 d/30 October 2099
```

**Expected output**

```text
Successfully added loan: 2x Wire [Components] to Alex (91234567), due 30 October 2099
```

#### Step 4

**Input**

```text
delete-l
```

**Expected output**

```text
Invalid format. Use: delete-l INDEX
```

#### Step 5

**Input**

```text
delete-l 1 extra
```

**Expected output**

```text
Invalid format. Use: delete-l INDEX
```

#### Step 6

**Input**

```text
delete-l 1 q/1
```

**Expected output**

```text
Invalid format. Use: delete-l INDEX
```

#### Step 7

**Input**

```text
delete-l abc
```

**Expected output**

```text
Loan index must be a positive integer.
```

#### Step 8

**Input**

```text
delete-l 0
```

**Expected output**

```text
Loan index must be a positive integer.
```

#### Step 9

**Input**

```text
delete-l -1
```

**Expected output**

```text
Loan index must be a positive integer.
```

#### Step 10

**Input**

```text
delete-l 1.5
```

**Expected output**

```text
Loan index must be a positive integer.
```

#### Step 11

**Input**

```text
delete-l 2147483648
```

**Expected output**

```text
Loan index must be a positive integer.
```

#### Step 12

**Input**

```text
delete-l i/1
```

**Expected output**

```text
Loan index must be a positive integer.
```

#### Step 13

**Input**

```text
delete-l 2
```

**Expected output**

```text
Loan index 2 is invalid. There are 1 active loan(s).
```

#### Step 14

**Input**

```text
DELETE-L 1
```

**Expected output**

```text
Invalid command
```

#### Step 15

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 8)
--------------------------------------------------------------------
```

#### Step 16

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 2x Wire [Components] to Alex (91234567), due 30 October 2099
--------------------------------------------------------------------
```

### UI-LOAN-DELETE-002: Delete independent records and renumber remaining loans

**Aim:** Verify middle/last/final deletion restores only the selected quantity and cannot restore it twice.

**Preconditions:** Fresh application with empty inventory, loans, supplier contacts, and sessions.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 2

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex t/91234567 d/30 October 2099
```

**Expected output**

```text
Successfully added loan: 2x Wire [Components] to Alex (91234567), due 30 October 2099
```

#### Step 3

**Input**

```text
add-l n/Wire c/Components q/3 b/Alex t/91234567 d/31 October 2099
```

**Expected output**

```text
Successfully added loan: 3x Wire [Components] to Alex (91234567), due 31 October 2099
```

#### Step 4

**Input**

```text
add-l n/Wire c/Components q/1 b/Sam t/91234567 d/30 October 2099
```

**Expected output**

```text
Successfully added loan: 1x Wire [Components] to Sam (91234567), due 30 October 2099
```

#### Step 5

**Input**

```text
delete-l 2
```

**Expected output**

```text
Successfully deleted incorrect loan: 3x Wire [Components] to Alex (91234567), due 31 October 2099. Inventory restored.
```

#### Step 6

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 7)
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 2x Wire [Components] to Alex (91234567), due 30 October 2099
2. 1x Wire [Components] to Sam (91234567), due 30 October 2099
--------------------------------------------------------------------
```

#### Step 8

**Input**

```text
delete-l 2
```

**Expected output**

```text
Successfully deleted incorrect loan: 1x Wire [Components] to Sam (91234567), due 30 October 2099. Inventory restored.
```

#### Step 9

**Input**

```text
delete-l 1
```

**Expected output**

```text
Successfully deleted incorrect loan: 2x Wire [Components] to Alex (91234567), due 30 October 2099. Inventory restored.
```

#### Step 10

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
No active loans.
--------------------------------------------------------------------
```

#### Step 11

**Input**

```text
delete-l 1
```

**Expected output**

```text
Loan index 1 is invalid. There are 0 active loan(s).
```

#### Step 12

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 10)
--------------------------------------------------------------------
```

### UI-LOAN-DELETE-003: Recreate depleted categories and merge matching replenished stock

**Aim:** Verify category recreation and case-insensitive merging into an item re-added after lending all stock.

**Preconditions:** Fresh application with empty inventory, loans, supplier contacts, and sessions.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/2
```

**Expected output**

```text
Successfully added: 2x Wire to the inventory
```

#### Step 2

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex t/91234567 d/30 October 2099
```

**Expected output**

```text
Successfully added loan: 2x Wire [Components] to Alex (91234567), due 30 October 2099
```

#### Step 3

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
No items in inventory.
--------------------------------------------------------------------
```

#### Step 4

**Input**

```text
delete-l 1
```

**Expected output**

```text
Successfully deleted incorrect loan: 2x Wire [Components] to Alex (91234567), due 30 October 2099. Inventory restored.
```

#### Step 5

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 2)
--------------------------------------------------------------------
```

#### Step 6

**Input**

```text
add-l n/wire c/COMPONENTS q/2 b/Alex t/91234567 d/30 October 2099
```

**Expected output**

```text
Successfully added loan: 2x wire [COMPONENTS] to Alex (91234567), due 30 October 2099
```

#### Step 7

**Input**

```text
add-i n/Wire c/Components q/5
```

**Expected output**

```text
Successfully added: 5x Wire to the inventory
```

#### Step 8

**Input**

```text
delete-l 1
```

**Expected output**

```text
Successfully deleted incorrect loan: 2x wire [COMPONENTS] to Alex (91234567), due 30 October 2099. Inventory restored.
```

#### Step 9

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 7)
--------------------------------------------------------------------
```

#### Step 10

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
No active loans.
--------------------------------------------------------------------
```

### UI-LOAN-DELETE-004: Restore a missing item within an existing category

**Aim:** Verify restoration appends the missing item, preserves surviving items/categories, and uses existing category spelling.

**Preconditions:** Fresh application with empty inventory, loans, supplier contacts, and sessions.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/2
```

**Expected output**

```text
Successfully added: 2x Wire to the inventory
```

#### Step 2

**Input**

```text
add-i n/Battery c/Components q/1
```

**Expected output**

```text
Successfully added: 1x Battery to the inventory
```

#### Step 3

**Input**

```text
add-i n/Wire c/Spares q/5
```

**Expected output**

```text
Successfully added: 5x Wire to the inventory
```

#### Step 4

**Input**

```text
add-l n/wire c/COMPONENTS q/2 b/Alex t/91234567 d/30 October 2099
```

**Expected output**

```text
Successfully added loan: 2x wire [COMPONENTS] to Alex (91234567), due 30 October 2099
```

#### Step 5

**Input**

```text
delete-l 1
```

**Expected output**

```text
Successfully deleted incorrect loan: 2x wire [COMPONENTS] to Alex (91234567), due 30 October 2099. Inventory restored.
```

#### Step 6

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Battery (Qty: 1)
2: wire (Qty: 2)
Spares
1: Wire (Qty: 5)
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
No active loans.
--------------------------------------------------------------------
```

### UI-LOAN-DELETE-005: Refresh preparation and preserve supplier contacts after correction

**Aim:** Verify stock restoration removes the preparation shortage without changing contacts or sessions.

**Preconditions:** Fresh application with empty inventory, loans, supplier contacts, and sessions.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 2

**Input**

```text
add-s n/Correction Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/10
```

**Expected output**

```text
Successfully added: Correction Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
```

#### Step 3

**Input**

```text
add-p s/1 i/Wire c/Components
```

**Expected output**

```text
Successfully added: 10x Wire [Components] to preparation
--------------------------------------------------------------------
Lab preparation: Correction Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 10, Available: 10)
--------------------------------------------------------------------
```

#### Step 4

**Input**

```text
add-c n/Alex t/91234567 r/Supplier
```

**Expected output**

```text
Successfully added: Alex (91234567) to contacts
```

#### Step 5

**Input**

```text
add-l n/Wire c/Components q/3 b/Alex t/91234567 d/30 October 2099
```

**Expected output**

```text
Successfully added loan: 3x Wire [Components] to Alex (91234567), due 30 October 2099
```

#### Step 6

**Input**

```text
list-p s/1
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: Correction Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 10, Available: 7) !! Insufficient items (Shortfall: 3)
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
delete-l 1
```

**Expected output**

```text
Successfully deleted incorrect loan: 3x Wire [Components] to Alex (91234567), due 30 October 2099. Inventory restored.
```

#### Step 8

**Input**

```text
list-p s/1
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: Correction Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 10, Available: 10)
--------------------------------------------------------------------
```

#### Step 9

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
1. Alex (91234567) - Supplier
--------------------------------------------------------------------
```

#### Step 10

**Input**

```text
list-s
```

**Expected output**

```text
--------------------------------------------------------------------
Sessions
--------------------------------------------------------------------
1. Correction Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
```

#### Step 11

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
No active loans.
--------------------------------------------------------------------
```

### UI-LOAN-DELETE-006: Reject restoration overflow without deleting the loan

**Aim:** Verify inventory and loans stay unchanged when restoration exceeds the integer limit, then allow retry after stock is reduced.

**Preconditions:** Fresh application with empty inventory, loans, supplier contacts, and sessions.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/2
```

**Expected output**

```text
Successfully added: 2x Wire to the inventory
```

#### Step 2

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex t/91234567 d/30 October 2099
```

**Expected output**

```text
Successfully added loan: 2x Wire [Components] to Alex (91234567), due 30 October 2099
```

#### Step 3

**Input**

```text
add-i n/Wire c/Components q/2147483647
```

**Expected output**

```text
Successfully added: 2147483647x Wire to the inventory
```

#### Step 4

**Input**

```text
delete-l 1
```

**Expected output**

```text
Cannot restore 2 Wire. Inventory quantity must not exceed 2147483647.
```

#### Step 5

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 2x Wire [Components] to Alex (91234567), due 30 October 2099
--------------------------------------------------------------------
```

#### Step 6

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 2147483647)
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
delete-i c/Components i/1 q/2
```

**Expected output**

```text
Successfully removed: 2x Wire from the inventory
```

#### Step 8

**Input**

```text
delete-l 1
```

**Expected output**

```text
Successfully deleted incorrect loan: 2x Wire [Components] to Alex (91234567), due 30 October 2099. Inventory restored.
```

#### Step 9

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 2147483647)
--------------------------------------------------------------------
```

#### Step 10

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
No active loans.
--------------------------------------------------------------------
```

### UI-LOAN-RETURN-001: Reject invalid and partial return commands

**Aim:** Verify one positive plain index is required, partial/batch returns are rejected, and invalid requests preserve all state.

**Preconditions:** Fresh application with empty inventory, loans, supplier contacts, and sessions.

#### Step 1

**Input**

```text
return 1
```

**Expected output**

```text
Loan index 1 is invalid. There are 0 active loan(s).
```

#### Step 2

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 3

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex t/91234567 d/30 October 2099
```

**Expected output**

```text
Successfully added loan: 2x Wire [Components] to Alex (91234567), due 30 October 2099
```

#### Step 4

**Input**

```text
return
```

**Expected output**

```text
Invalid format. Use: return INDEX
```

#### Step 5

**Input**

```text
return 1 extra
```

**Expected output**

```text
Invalid format. Use: return INDEX
```

#### Step 6

**Input**

```text
return 1 q/1
```

**Expected output**

```text
Invalid format. Use: return INDEX
```

#### Step 7

**Input**

```text
return 1 2
```

**Expected output**

```text
Invalid format. Use: return INDEX
```

#### Step 8

**Input**

```text
return 0
```

**Expected output**

```text
Loan index must be a positive integer.
```

#### Step 9

**Input**

```text
return -1
```

**Expected output**

```text
Loan index must be a positive integer.
```

#### Step 10

**Input**

```text
return abc
```

**Expected output**

```text
Loan index must be a positive integer.
```

#### Step 11

**Input**

```text
return 1.5
```

**Expected output**

```text
Loan index must be a positive integer.
```

#### Step 12

**Input**

```text
return 2147483648
```

**Expected output**

```text
Loan index must be a positive integer.
```

#### Step 13

**Input**

```text
return i/1
```

**Expected output**

```text
Loan index must be a positive integer.
```

#### Step 14

**Input**

```text
return 2
```

**Expected output**

```text
Loan index 2 is invalid. There are 1 active loan(s).
```

#### Step 15

**Input**

```text
RETURN 1
```

**Expected output**

```text
Invalid command
```

#### Step 16

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 2x Wire [Components] to Alex (91234567), due 30 October 2099
--------------------------------------------------------------------
```

#### Step 17

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 8)
--------------------------------------------------------------------
```

### UI-LOAN-RETURN-002: Return complete loans and mix returns with corrections

**Aim:** Verify independent records, shifted indices, distinct confirmations, and prevention of double restoration.

**Preconditions:** Fresh application with empty inventory, loans, supplier contacts, and sessions.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 2

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex t/91234567 d/30 October 2099
```

**Expected output**

```text
Successfully added loan: 2x Wire [Components] to Alex (91234567), due 30 October 2099
```

#### Step 3

**Input**

```text
add-l n/Wire c/Components q/3 b/Sam t/91234567 d/31 October 2099
```

**Expected output**

```text
Successfully added loan: 3x Wire [Components] to Sam (91234567), due 31 October 2099
```

#### Step 4

**Input**

```text
add-l n/Wire c/Components q/1 b/Alex t/91234567 d/30 October 2099
```

**Expected output**

```text
Successfully added loan: 1x Wire [Components] to Alex (91234567), due 30 October 2099
```

#### Step 5

**Input**

```text
return 2
```

**Expected output**

```text
Successfully returned loan: 3x Wire [Components] to Sam (91234567), due 31 October 2099. Inventory restored.
```

#### Step 6

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 2x Wire [Components] to Alex (91234567), due 30 October 2099
2. 1x Wire [Components] to Alex (91234567), due 30 October 2099
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 7)
--------------------------------------------------------------------
```

#### Step 8

**Input**

```text
delete-l 1
```

**Expected output**

```text
Successfully deleted incorrect loan: 2x Wire [Components] to Alex (91234567), due 30 October 2099. Inventory restored.
```

#### Step 9

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 1x Wire [Components] to Alex (91234567), due 30 October 2099
--------------------------------------------------------------------
```

#### Step 10

**Input**

```text
return 1
```

**Expected output**

```text
Successfully returned loan: 1x Wire [Components] to Alex (91234567), due 30 October 2099. Inventory restored.
```

#### Step 11

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
No active loans.
--------------------------------------------------------------------
```

#### Step 12

**Input**

```text
return 1
```

**Expected output**

```text
Loan index 1 is invalid. There are 0 active loan(s).
```

#### Step 13

**Input**

```text
delete-l 1
```

**Expected output**

```text
Loan index 1 is invalid. There are 0 active loan(s).
```

#### Step 14

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 10)
--------------------------------------------------------------------
```

### UI-LOAN-RETURN-003: Restore depleted and replenished inventory on return

**Aim:** Verify whole-loan returns recreate missing categories and merge case-insensitively without changing other categories.

**Preconditions:** Fresh application with empty inventory, loans, supplier contacts, and sessions.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/2
```

**Expected output**

```text
Successfully added: 2x Wire to the inventory
```

#### Step 2

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex t/91234567 d/30 October 2099
```

**Expected output**

```text
Successfully added loan: 2x Wire [Components] to Alex (91234567), due 30 October 2099
```

#### Step 3

**Input**

```text
return 1
```

**Expected output**

```text
Successfully returned loan: 2x Wire [Components] to Alex (91234567), due 30 October 2099. Inventory restored.
```

#### Step 4

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 2)
--------------------------------------------------------------------
```

#### Step 5

**Input**

```text
add-l n/wire c/COMPONENTS q/2 b/Alex t/91234567 d/30 October 2099
```

**Expected output**

```text
Successfully added loan: 2x wire [COMPONENTS] to Alex (91234567), due 30 October 2099
```

#### Step 6

**Input**

```text
add-i n/Wire c/Components q/5
```

**Expected output**

```text
Successfully added: 5x Wire to the inventory
```

#### Step 7

**Input**

```text
add-i n/Wire c/Spares q/4
```

**Expected output**

```text
Successfully added: 4x Wire to the inventory
```

#### Step 8

**Input**

```text
return 1
```

**Expected output**

```text
Successfully returned loan: 2x wire [COMPONENTS] to Alex (91234567), due 30 October 2099. Inventory restored.
```

#### Step 9

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 7)
Spares
1: Wire (Qty: 4)
--------------------------------------------------------------------
```

#### Step 10

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
No active loans.
--------------------------------------------------------------------
```

### UI-LOAN-RETURN-004: Refresh preparation after a full return and preserve contacts

**Aim:** Verify preparation shortages disappear after a return and supplier contacts, sessions, and repeated listing remain consistent.

**Preconditions:** Fresh application with empty inventory, loans, supplier contacts, and sessions.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 2

**Input**

```text
add-s n/Return Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/10
```

**Expected output**

```text
Successfully added: Return Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
```

#### Step 3

**Input**

```text
add-p s/1 i/Wire c/Components
```

**Expected output**

```text
Successfully added: 10x Wire [Components] to preparation
--------------------------------------------------------------------
Lab preparation: Return Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 10, Available: 10)
--------------------------------------------------------------------
```

#### Step 4

**Input**

```text
add-c n/Alex t/91234567 r/Supplier
```

**Expected output**

```text
Successfully added: Alex (91234567) to contacts
```

#### Step 5

**Input**

```text
add-l n/Wire c/Components q/3 b/Alex t/91234567 d/30 October 2099
```

**Expected output**

```text
Successfully added loan: 3x Wire [Components] to Alex (91234567), due 30 October 2099
```

#### Step 6

**Input**

```text
list-p s/1
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: Return Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 10, Available: 7) !! Insufficient items (Shortfall: 3)
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
return 1
```

**Expected output**

```text
Successfully returned loan: 3x Wire [Components] to Alex (91234567), due 30 October 2099. Inventory restored.
```

#### Step 8

**Input**

```text
list-p s/1
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: Return Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 10, Available: 10)
--------------------------------------------------------------------
```

#### Step 9

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
No active loans.
--------------------------------------------------------------------
```

#### Step 10

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
No active loans.
--------------------------------------------------------------------
```

#### Step 11

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
1. Alex (91234567) - Supplier
--------------------------------------------------------------------
```

#### Step 12

**Input**

```text
list-s
```

**Expected output**

```text
--------------------------------------------------------------------
Sessions
--------------------------------------------------------------------
1. Return Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
```

#### Step 13

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 10)
--------------------------------------------------------------------
```

### UI-LOAN-RETURN-005: Preserve active loans when return restoration would overflow

**Aim:** Verify a failed return changes neither stock nor loans and can be retried after inventory is reduced.

**Preconditions:** Fresh application with empty inventory, loans, supplier contacts, and sessions.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/2
```

**Expected output**

```text
Successfully added: 2x Wire to the inventory
```

#### Step 2

**Input**

```text
add-l n/Wire c/Components q/2 b/Alex t/91234567 d/30 October 2099
```

**Expected output**

```text
Successfully added loan: 2x Wire [Components] to Alex (91234567), due 30 October 2099
```

#### Step 3

**Input**

```text
add-i n/Wire c/Components q/2147483647
```

**Expected output**

```text
Successfully added: 2147483647x Wire to the inventory
```

#### Step 4

**Input**

```text
return 1
```

**Expected output**

```text
Cannot restore 2 Wire. Inventory quantity must not exceed 2147483647.
```

#### Step 5

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 2x Wire [Components] to Alex (91234567), due 30 October 2099
--------------------------------------------------------------------
```

#### Step 6

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 2147483647)
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
delete-i c/Components i/1 q/2
```

**Expected output**

```text
Successfully removed: 2x Wire from the inventory
```

#### Step 8

**Input**

```text
return 1
```

**Expected output**

```text
Successfully returned loan: 2x Wire [Components] to Alex (91234567), due 30 October 2099. Inventory restored.
```

#### Step 9

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
No active loans.
--------------------------------------------------------------------
```

#### Step 10

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 2147483647)
--------------------------------------------------------------------
```

### UI-LOAN-OVERDUE-001: Detect overdue boundaries and retain full-list indices

**Aim:** Verify yesterday is overdue while today/tomorrow are not, past-date warning is immediate, and filtered rows keep active-list indices.

**Preconditions:** Fresh application with empty inventory, loans, supplier contacts, and sessions. Date placeholders are resolved once at session start.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 2

**Input**

```text
add-l n/Wire c/Components q/1 b/Today t/91234567 d/{{TODAY}}
```

**Expected output**

```text
Successfully added loan: 1x Wire [Components] to Today (91234567), due {{TODAY}}
```

#### Step 3

**Input**

```text
add-l n/Wire c/Components q/1 b/Yesterday t/91234567 d/{{YESTERDAY}}
```

**Expected output**

```text
Successfully added loan: 1x Wire [Components] to Yesterday (91234567), due {{YESTERDAY}}
Warning: this loan is already overdue.
```

#### Step 4

**Input**

```text
add-l n/Wire c/Components q/1 b/Tomorrow t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Successfully added loan: 1x Wire [Components] to Tomorrow (91234567), due {{TOMORROW}}
```

#### Step 5

**Input**

```text
add-l n/Wire c/Components q/1 b/Older backlog t/91234567 d/{{YESTERDAY}}
```

**Expected output**

```text
Successfully added loan: 1x Wire [Components] to Older backlog (91234567), due {{YESTERDAY}}
Warning: this loan is already overdue.
```

#### Step 6

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 1x Wire [Components] to Today (91234567), due {{TODAY}}
2. 1x Wire [Components] to Yesterday (91234567), due {{YESTERDAY}} [OVERDUE]
3. 1x Wire [Components] to Tomorrow (91234567), due {{TOMORROW}}
4. 1x Wire [Components] to Older backlog (91234567), due {{YESTERDAY}} [OVERDUE]
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
list-l overdue
```

**Expected output**

```text
--------------------------------------------------------------------
Overdue loans
--------------------------------------------------------------------
2. 1x Wire [Components] to Yesterday (91234567), due {{YESTERDAY}} [OVERDUE]
4. 1x Wire [Components] to Older backlog (91234567), due {{YESTERDAY}} [OVERDUE]
--------------------------------------------------------------------
```

#### Step 8

**Input**

```text
list-l overdue
```

**Expected output**

```text
--------------------------------------------------------------------
Overdue loans
--------------------------------------------------------------------
2. 1x Wire [Components] to Yesterday (91234567), due {{YESTERDAY}} [OVERDUE]
4. 1x Wire [Components] to Older backlog (91234567), due {{YESTERDAY}} [OVERDUE]
--------------------------------------------------------------------
```

#### Step 9

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 6)
--------------------------------------------------------------------
```

#### Step 10

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
No contacts saved.
--------------------------------------------------------------------
```

### UI-LOAN-OVERDUE-002: Return and correct overdue loans using displayed active indices

**Aim:** Verify filtered indices select the correct full-list loan, renumbering is reflected after changes, and removed loans disappear.

**Preconditions:** Fresh application with empty inventory, loans, supplier contacts, and sessions. Date placeholders are resolved once at session start.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 2

**Input**

```text
add-l n/Wire c/Components q/1 b/Current t/91234567 d/{{TODAY}}
```

**Expected output**

```text
Successfully added loan: 1x Wire [Components] to Current (91234567), due {{TODAY}}
```

#### Step 3

**Input**

```text
add-l n/Wire c/Components q/1 b/First overdue t/91234567 d/{{YESTERDAY}}
```

**Expected output**

```text
Successfully added loan: 1x Wire [Components] to First overdue (91234567), due {{YESTERDAY}}
Warning: this loan is already overdue.
```

#### Step 4

**Input**

```text
add-l n/Wire c/Components q/1 b/Second overdue t/91234567 d/{{YESTERDAY}}
```

**Expected output**

```text
Successfully added loan: 1x Wire [Components] to Second overdue (91234567), due {{YESTERDAY}}
Warning: this loan is already overdue.
```

#### Step 5

**Input**

```text
list-l overdue
```

**Expected output**

```text
--------------------------------------------------------------------
Overdue loans
--------------------------------------------------------------------
2. 1x Wire [Components] to First overdue (91234567), due {{YESTERDAY}} [OVERDUE]
3. 1x Wire [Components] to Second overdue (91234567), due {{YESTERDAY}} [OVERDUE]
--------------------------------------------------------------------
```

#### Step 6

**Input**

```text
return 2
```

**Expected output**

```text
Successfully returned loan: 1x Wire [Components] to First overdue (91234567), due {{YESTERDAY}}. Inventory restored.
```

#### Step 7

**Input**

```text
list-l overdue
```

**Expected output**

```text
--------------------------------------------------------------------
Overdue loans
--------------------------------------------------------------------
2. 1x Wire [Components] to Second overdue (91234567), due {{YESTERDAY}} [OVERDUE]
--------------------------------------------------------------------
```

#### Step 8

**Input**

```text
delete-l 2
```

**Expected output**

```text
Successfully deleted incorrect loan: 1x Wire [Components] to Second overdue (91234567), due {{YESTERDAY}}. Inventory restored.
```

#### Step 9

**Input**

```text
list-l overdue
```

**Expected output**

```text
--------------------------------------------------------------------
Overdue loans
--------------------------------------------------------------------
No overdue loans.
--------------------------------------------------------------------
```

#### Step 10

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 1x Wire [Components] to Current (91234567), due {{TODAY}}
--------------------------------------------------------------------
```

#### Step 11

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 9)
--------------------------------------------------------------------
```

### UI-LOAN-OVERDUE-003: Show no overdue loans for empty and current-only lists

**Aim:** Verify due-today and future loans do not trigger warnings/markers, and malformed filters are rejected without state changes.

**Preconditions:** Fresh application with empty inventory, loans, supplier contacts, and sessions. Date placeholders are resolved once at session start.

#### Step 1

**Input**

```text
list-l overdue
```

**Expected output**

```text
--------------------------------------------------------------------
Overdue loans
--------------------------------------------------------------------
No overdue loans.
--------------------------------------------------------------------
```

#### Step 2

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 3

**Input**

```text
add-l n/Wire c/Components q/1 b/Today t/91234567 d/{{TODAY}}
```

**Expected output**

```text
Successfully added loan: 1x Wire [Components] to Today (91234567), due {{TODAY}}
```

#### Step 4

**Input**

```text
add-l n/Wire c/Components q/1 b/Future t/91234567 d/{{TOMORROW}}
```

**Expected output**

```text
Successfully added loan: 1x Wire [Components] to Future (91234567), due {{TOMORROW}}
```

#### Step 5

**Input**

```text
list-l overdue
```

**Expected output**

```text
--------------------------------------------------------------------
Overdue loans
--------------------------------------------------------------------
No overdue loans.
--------------------------------------------------------------------
```

#### Step 6

**Input**

```text
list-l OVERDUE
```

**Expected output**

```text
Invalid format. Use: list-l [overdue|history|returned]
```

#### Step 7

**Input**

```text
list-l overdue extra
```

**Expected output**

```text
Invalid format. Use: list-l [overdue|history|returned]
```

#### Step 8

**Input**

```text
list-l overdue overdue
```

**Expected output**

```text
Invalid format. Use: list-l [overdue|history|returned]
```

#### Step 9

**Input**

```text
list-l d/{{TODAY}}
```

**Expected output**

```text
Invalid format. Use: list-l [overdue|history|returned]
```

#### Step 10

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 1x Wire [Components] to Today (91234567), due {{TODAY}}
2. 1x Wire [Components] to Future (91234567), due {{TOMORROW}}
--------------------------------------------------------------------
```

#### Step 11

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 8)
--------------------------------------------------------------------
```

### UI-LOAN-OVERDUE-004: Reject invalid backlog additions without false warnings

**Aim:** Verify invalid dates and insufficient stock do not create loans, emit overdue warnings, or deduct inventory.

**Preconditions:** Fresh application with empty inventory, loans, supplier contacts, and sessions. Date placeholders are resolved once at session start.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 2

**Input**

```text
add-l n/Wire c/Components q/11 b/Alex t/91234567 d/{{YESTERDAY}}
```

**Expected output**

```text
Cannot loan 11 Wire. Only 10 are available.
```

#### Step 3

**Input**

```text
add-l n/Wire c/Components q/1 b/Alex t/91234567 d/31 February 2099
```

**Expected output**

```text
Due date must be a valid date in d MMMM yyyy format (e.g., 30 October 2099).
```

#### Step 4

**Input**

```text
list-l overdue
```

**Expected output**

```text
--------------------------------------------------------------------
Overdue loans
--------------------------------------------------------------------
No overdue loans.
--------------------------------------------------------------------
```

#### Step 5

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
No active loans.
--------------------------------------------------------------------
```

#### Step 6

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 10)
--------------------------------------------------------------------
```

### UI-LOAN-DEFENSIVE-001: Recover after rejected loans and prevent repeated restoration

**Aim:** Preserve stock and active records after invalid dates, insufficient stock, invalid indices, and a repeated return; continue accepting valid commands.

**Preconditions:** Fresh application with empty inventory and loans. Expected stdout remains identical with assertions enabled or disabled.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 2

**Input**

```text
add-l n/Wire c/Components q/1 b/Alex t/91234567 d/31 February 2099
```

**Expected output**

```text
Due date must be a valid date in d MMMM yyyy format (e.g., 30 October 2099).
```

#### Step 3

**Input**

```text
add-l n/Wire c/Components q/11 b/Alex t/91234567 d/30 October 2099
```

**Expected output**

```text
Cannot loan 11 Wire. Only 10 are available.
```

#### Step 4

**Input**

```text
return 999999999999999999999
```

**Expected output**

```text
Loan index must be a positive integer.
```

#### Step 5

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
No active loans.
--------------------------------------------------------------------
```

#### Step 6

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 10)
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
add-l n/Wire c/Components q/3 b/Alex t/91234567 d/30 October 2099
```

**Expected output**

```text
Successfully added loan: 3x Wire [Components] to Alex (91234567), due 30 October 2099
```

#### Step 8

**Input**

```text
delete-l 2
```

**Expected output**

```text
Loan index 2 is invalid. There are 1 active loan(s).
```

#### Step 9

**Input**

```text
return 1
```

**Expected output**

```text
Successfully returned loan: 3x Wire [Components] to Alex (91234567), due 30 October 2099. Inventory restored.
```

#### Step 10

**Input**

```text
return 1
```

**Expected output**

```text
Loan index 1 is invalid. There are 0 active loan(s).
```

#### Step 11

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
No active loans.
--------------------------------------------------------------------
```

#### Step 12

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 10)
--------------------------------------------------------------------
```

### UI-BROKEN-REGRESSION-001: Report, reject excessive damage, and restore stock

**Aim:** Verify broken-item commands through the packaged application, including unchanged stock and reports after a rejected addition.

**Preconditions:** Start a fresh Bob process, enter `Alex`, and begin with empty inventory, loans, and broken reports. The fixed report date is in the past. In the shared console fixture, the earlier steps already provide 10 Wire and no active loans or reports, so its setup command is omitted.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 2

**Input**

```text
add-b n/Wire c/Components q/3 f/Frayed insulation d/1 January 2000
```

**Expected output**

```text
Successfully recorded broken item: 3x Wire [Components], fault: Frayed insulation, reported 1 January 2000. Available inventory reduced.
```

#### Step 3

**Input**

```text
list-b
```

**Expected output**

```text
--------------------------------------------------------------------
Broken items
--------------------------------------------------------------------
1. 3x Wire [Components], fault: Frayed insulation, reported 1 January 2000
   Contact: Not assigned
--------------------------------------------------------------------
```

#### Step 4

**Input**

```text
find-i wire
```

**Expected output**

```text
--------------------------------------------------------------------
Search results for "wire"
--------------------------------------------------------------------
1. Wire [Components] (Available: 7, On loan: 0) - Available
--------------------------------------------------------------------
```

#### Step 5

**Input**

```text
add-b n/Wire c/Components q/8 f/Cracked casing d/1 January 2000
```

**Expected output**

```text
Cannot report 8 Wire as broken. Only 7 are available.
```

#### Step 6

**Input**

```text
list-b
```

**Expected output**

```text
--------------------------------------------------------------------
Broken items
--------------------------------------------------------------------
1. 3x Wire [Components], fault: Frayed insulation, reported 1 January 2000
   Contact: Not assigned
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
find-i wire
```

**Expected output**

```text
--------------------------------------------------------------------
Search results for "wire"
--------------------------------------------------------------------
1. Wire [Components] (Available: 7, On loan: 0) - Available
--------------------------------------------------------------------
```

#### Step 8

**Input**

```text
delete-b 1
```

**Expected output**

```text
Successfully deleted broken-item report: 3x Wire [Components], fault: Frayed insulation, reported 1 January 2000. All units restored to inventory.
```

#### Step 9

**Input**

```text
list-b
```

**Expected output**

```text
--------------------------------------------------------------------
Broken items
--------------------------------------------------------------------
No broken items recorded.
--------------------------------------------------------------------
```

#### Step 10

**Input**

```text
find-i wire
```

**Expected output**

```text
--------------------------------------------------------------------
Search results for "wire"
--------------------------------------------------------------------
1. Wire [Components] (Available: 10, On loan: 0) - Available
--------------------------------------------------------------------
```

### UI-REQUEST-FULFILLMENT-001: Fulfill once and preserve stock on rejection

**Aim:** Verify insufficient stock, multi-category fulfillment, final status, shortage display, and deletion without restoration.

**Preconditions:** Fresh application with empty inventory and requests. The shared console fixture already has 10 Wire in Components, so it omits Step 1. Run before 31 December 2099.

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/10
```

**Expected output**

```text
Successfully added: 10x Wire to the inventory
```

#### Step 2

**Input**

```text
add-r n/Alice i/Wire q/12 d/31 December 2099
```

**Expected output**

```text
Successfully added request: Alice - 12x Wire by 31 December 2099
Request list updated. Run list-r to see the latest order.
```

#### Step 3

**Input**

```text
add-r n/Bob i/Wire q/1 d/31 December 2099
```

**Expected output**

```text
Successfully added request: Bob - 1x Wire by 31 December 2099
Request list updated. Run list-r to see the latest order.
```

#### Step 4

**Input**

```text
mark-r 1 fulfilled
```

**Expected output**

```text
Cannot remove 12 Wire. Only 10 are available.
```

#### Step 5

**Input**

```text
find-i wire
```

**Expected output**

```text
--------------------------------------------------------------------
Search results for "wire"
--------------------------------------------------------------------
1. Wire [Components] (Available: 10, On loan: 0) - Available
--------------------------------------------------------------------
```

#### Step 6

**Input**

```text
add-i n/Wire c/Spares q/2
```

**Expected output**

```text
Successfully added: 2x Wire to the inventory
```

#### Step 7

**Input**

```text
mark-r 1 fulfilled
```

**Expected output**

```text
Successfully marked request 1 as fulfilled: Alice - 12x Wire by 31 December 2099
```

#### Step 8

**Input**

```text
mark-r 1 fulfilled
```

**Expected output**

```text
Request 1 is already fulfilled and cannot be changed: Alice - 12x Wire by 31 December 2099
```

#### Step 9

**Input**

```text
mark-r 1 prepared
```

**Expected output**

```text
Request 1 is already fulfilled and cannot be changed: Alice - 12x Wire by 31 December 2099
```

#### Step 10

**Input**

```text
mark-r 2 prepared
```

**Expected output**

```text
Successfully marked request 2 as prepared: Bob - 1x Wire by 31 December 2099
```

#### Step 11

**Input**

```text
list-r
```

**Expected output**

```text
--------------------------------------------------------------------
Student requests
--------------------------------------------------------------------
1. [Fulfilled] Alice - 12x Wire by 31 December 2099 (Available: 0)
2. [Prepared] Bob - 1x Wire by 31 December 2099 (Available: 0) !! Insufficient stock (Shortfall: 1)
--------------------------------------------------------------------
```

#### Step 12

**Input**

```text
delete-r 1
```

**Expected output**

```text
Successfully removed request: Alice - 12x Wire by 31 December 2099
Request list updated. Run list-r to see the latest order.
```

#### Step 13

**Input**

```text
find-i wire
```

**Expected output**

```text
--------------------------------------------------------------------
Search results for "wire"
--------------------------------------------------------------------
No items match "wire".
--------------------------------------------------------------------
```

<!--
### UI-001: Short title

**Aim:** Describe the behavior being verified.

**Preconditions:** None.

#### Step 1

**Input**

```text
command
```

**Expected output**

```text
exact output
```
-->

## Persistence phases 3-6

Persistence cases use an isolated working directory. Expected output is specified before implementing each
phase. Phase 3 exercises the storage boundary through unit tests and verifies the unchanged packaged console
workflow; Phase 4 verifies fixture loading, Phase 5 verifies saving and restarts, and Phase 6 runs the complete
plan plus a new-data workflow normally and with assertions enabled.

The runner recognizes these recorded setup fields:

- `Storage fixture`: `populated`, `recovery` (corrupt main plus populated backup), `backup-only`, `corrupt`,
  `newer` (version 2 main plus valid empty backup), or `blocked-backup` (empty main plus nonempty backup directory).
- `Storage group`: cases in the same group share one isolated directory and must run in plan order.
- `Startup failure`: its single `Start Bob` step compares output and the expected process exit without a greeting.
- `Startup warning`: output compared after the normal greeting and before commands.
- `Exit code`: expected status, default 0; `Exit input` identifies a prompt choice that terminates the process.
- `Disk unchanged`: compare saved-file fingerprints before startup and after normal exit.
- `Repair storage before step`: remove only the known backup blocker fixture before that step to test retry.

`{{STORAGE_PATH}}` resolves to the isolated main file's absolute path. Date placeholders use the once-resolved
computer-local date recorded at session start. Fixtures are set up before startup; recovery cases can repair
their main save on exit. Every failure terminates its process and stops later steps and cases.

### UI-PERSIST-LOAD-001: Restore all six feature areas

**Aim:** Restore all six feature areas using a fresh packaged application process.

**Preconditions:** Isolated storage initialized from the recorded fixture before startup.

**Storage fixture:** `populated`

**Disk unchanged:** `true`

#### Step 1

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Glassware
1: Beaker (Qty: 7)
Safety
1: Gloves (Qty: 20)
--------------------------------------------------------------------
```

#### Step 2

**Input**

```text
list-s
```

**Expected output**

```text
--------------------------------------------------------------------
Sessions
--------------------------------------------------------------------
1. Completed chemistry lab on 15 January 2020 at Lab 1 from 0900 to 1100 for 10 attendees
2. Introduction to chemistry on 5 November 2099 at Lab 1 from 0900 to 1100 for 10 attendees
--------------------------------------------------------------------
```

#### Step 3

**Input**

```text
list-p s/2
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: Introduction to chemistry on 5 November 2099 at Lab 1 from 0900 to 1100 for 10 attendees
--------------------------------------------------------------------
1. Beaker [Glassware] (Required: 13, Available: 7) !! Insufficient items (Shortfall: 6)
2. Gloves [Safety] (Required: 20, Available: 20)
3. Test tube [Glassware] (Required: 10, Available: 0) !! Insufficient items (Shortfall: 10)
--------------------------------------------------------------------
```

#### Step 4

**Input**

```text
list-r
```

**Expected output**

```text
--------------------------------------------------------------------
Student requests
--------------------------------------------------------------------
1. [Fulfilled] Alex Tan - 2x Gloves by 15 January 2020 (Available: 20)
2. [Pending] Jamie Lim - 8x Beaker by 4 November 2099 (Available: 7) !! Insufficient stock (Shortfall: 1)
3. [Prepared] Morgan Lee - 4x Gloves by 5 November 2099 (Available: 20)
--------------------------------------------------------------------
```

#### Step 5

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
1. Example Lab Supplies (01234567, orders@example.com) - Supplier
2. Example Maintenance (07654321)
--------------------------------------------------------------------
```

#### Step 6

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 3x Beaker [Glassware] to Taylor Wong (02345678), due 16 January 2020 [OVERDUE]
2. 1x Microscope [Equipment] to Casey Ng (03456789), due 6 November 2099
3. 1x Microscope [Equipment] to Casey Ng (03456789), due 6 November 2099
--------------------------------------------------------------------
```

### UI-PERSIST-LOAD-002: Recover a valid backup

**Aim:** Recover a valid backup using a fresh packaged application process.

**Preconditions:** Isolated storage initialized from the recorded fixture before startup.

**Storage fixture:** `recovery`

**Startup warning**

```text
Recovered data from backup. It may not include the latest changes.
```

#### Step 1

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Glassware
1: Beaker (Qty: 7)
Safety
1: Gloves (Qty: 20)
--------------------------------------------------------------------
```

### UI-PERSIST-LOAD-003: Reject corrupt data without overwriting it

**Aim:** Reject corrupt data without overwriting it using a fresh packaged application process.

**Preconditions:** Isolated storage initialized from the recorded fixture before startup.

**Storage fixture:** `corrupt`

**Disk unchanged:** `true`

**Startup failure:** `true`

**Exit code:** `1`

#### Step 1

**Input**

```text
Start Bob
```

**Expected output**

```text
Cannot load data from {{STORAGE_PATH}}: The saved data is invalid.
Bob has stopped; saved files were not changed.
```

### UI-PERSIST-LOAD-004: Protect a newer-format file despite a valid backup

**Aim:** Protect a newer-format file despite a valid backup using a fresh packaged application process.

**Preconditions:** Isolated storage initialized from the recorded fixture before startup.

**Storage fixture:** `newer`

**Disk unchanged:** `true`

**Startup failure:** `true`

**Exit code:** `1`

#### Step 1

**Input**

```text
Start Bob
```

**Expected output**

```text
Cannot load data from {{STORAGE_PATH}}: Unsupported data version: 3.
Bob has stopped; saved files were not changed.
```

### UI-PERSIST-LOAD-005: Recover when only a backup remains

**Aim:** Recover when only a backup remains using a fresh packaged application process.

**Preconditions:** Isolated storage initialized from the recorded fixture before startup.

**Storage fixture:** `backup-only`

**Startup warning**

```text
Recovered data from backup. It may not include the latest changes.
```

#### Step 1

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
1. Example Lab Supplies (01234567, orders@example.com) - Supplier
2. Example Maintenance (07654321)
--------------------------------------------------------------------
```

### UI-PERSIST-SAVE-001: Save changes to every feature before exit

**Aim:** Save changes to every feature before exit with exact console comparisons and isolated storage.

**Preconditions:** Cases sharing the recorded storage group run in order with a fresh process each time.

**Storage fixture:** `populated`

**Storage group:** `restart`

#### Step 1

**Input**

```text
delete-i c/Glassware i/1 q/2
```

**Expected output**

```text
Successfully removed: 2x Beaker from the inventory
```

#### Step 2

**Input**

```text
delete-s 1
```

**Expected output**

```text
Successfully removed session: Completed chemistry lab
```

#### Step 3

**Input**

```text
delete-p s/1 i/1 q/1
```

**Expected output**

```text
Successfully removed: 1x Beaker [Glassware] from preparation
--------------------------------------------------------------------
Lab preparation: Introduction to chemistry on 5 November 2099 at Lab 1 from 0900 to 1100 for 10 attendees
--------------------------------------------------------------------
1. Beaker [Glassware] (Required: 12, Available: 5) !! Insufficient items (Shortfall: 7)
2. Gloves [Safety] (Required: 20, Available: 20)
3. Test tube [Glassware] (Required: 10, Available: 0) !! Insufficient items (Shortfall: 10)
--------------------------------------------------------------------
```

#### Step 4

**Input**

```text
mark-r 2 prepared
```

**Expected output**

```text
Successfully marked request 2 as prepared: Jamie Lim - 8x Beaker by 4 November 2099
```

#### Step 5

**Input**

```text
delete-c 2
```

**Expected output**

```text
Successfully removed contact: Example Maintenance
```

#### Step 6

**Input**

```text
return 1
```

**Expected output**

```text
Successfully returned loan: 3x Beaker [Glassware] to Taylor Wong (02345678), due 16 January 2020. Inventory restored.
```

#### Step 7

**Input**

```text
Bye
```

**Expected output (no output)**

```text

```

### UI-PERSIST-SAVE-002: Restart with every changed value preserved

**Aim:** Restart with every changed value preserved with exact console comparisons and isolated storage.

**Preconditions:** Cases sharing the recorded storage group run in order with a fresh process each time.

**Storage group:** `restart`

#### Step 1

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Glassware
1: Beaker (Qty: 8)
Safety
1: Gloves (Qty: 20)
--------------------------------------------------------------------
```

#### Step 2

**Input**

```text
list-s
```

**Expected output**

```text
--------------------------------------------------------------------
Sessions
--------------------------------------------------------------------
1. Introduction to chemistry on 5 November 2099 at Lab 1 from 0900 to 1100 for 10 attendees
--------------------------------------------------------------------
```

#### Step 3

**Input**

```text
list-p s/1
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: Introduction to chemistry on 5 November 2099 at Lab 1 from 0900 to 1100 for 10 attendees
--------------------------------------------------------------------
1. Beaker [Glassware] (Required: 12, Available: 8) !! Insufficient items (Shortfall: 4)
2. Gloves [Safety] (Required: 20, Available: 20)
3. Test tube [Glassware] (Required: 10, Available: 0) !! Insufficient items (Shortfall: 10)
--------------------------------------------------------------------
```

#### Step 4

**Input**

```text
list-r
```

**Expected output**

```text
--------------------------------------------------------------------
Student requests
--------------------------------------------------------------------
1. [Fulfilled] Alex Tan - 2x Gloves by 15 January 2020 (Available: 20)
2. [Prepared] Jamie Lim - 8x Beaker by 4 November 2099 (Available: 8)
3. [Prepared] Morgan Lee - 4x Gloves by 5 November 2099 (Available: 20)
--------------------------------------------------------------------
```

#### Step 5

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
1. Example Lab Supplies (01234567, orders@example.com) - Supplier
--------------------------------------------------------------------
```

#### Step 6

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 1x Microscope [Equipment] to Casey Ng (03456789), due 6 November 2099
2. 1x Microscope [Equipment] to Casey Ng (03456789), due 6 November 2099
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
return 1
```

**Expected output**

```text
Successfully returned loan: 1x Microscope [Equipment] to Casey Ng (03456789), due 6 November 2099. Inventory restored.
```

#### Step 8

**Input**

```text
Close standard input (EOF)
```

**Expected output (no output)**

```text

```

### UI-PERSIST-SAVE-003: Restart after EOF and correct the remaining loan

**Aim:** Restart after EOF and correct the remaining loan with exact console comparisons and isolated storage.

**Preconditions:** Cases sharing the recorded storage group run in order with a fresh process each time.

**Storage group:** `restart`

#### Step 1

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Glassware
1: Beaker (Qty: 8)
Safety
1: Gloves (Qty: 20)
Equipment
1: Microscope (Qty: 1)
--------------------------------------------------------------------
```

#### Step 2

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 1x Microscope [Equipment] to Casey Ng (03456789), due 6 November 2099
--------------------------------------------------------------------
```

#### Step 3

**Input**

```text
delete-l 1
```

**Expected output**

```text
Successfully deleted incorrect loan: 1x Microscope [Equipment] to Casey Ng (03456789), due 6 November 2099. Inventory restored.
```

#### Step 4

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Glassware
1: Beaker (Qty: 8)
Safety
1: Gloves (Qty: 20)
Equipment
1: Microscope (Qty: 2)
--------------------------------------------------------------------
```

### UI-PERSIST-SAVE-READONLY-001: Read-only rejected and unchanged commands do not write

**Aim:** Read-only rejected and unchanged commands do not write with exact console comparisons and isolated storage.

**Preconditions:** Fresh process with the recorded storage fixture.

**Storage fixture:** `populated`

**Disk unchanged:** `true`

#### Step 1

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Glassware
1: Beaker (Qty: 7)
Safety
1: Gloves (Qty: 20)
--------------------------------------------------------------------
```

#### Step 2

**Input**

```text
add-i n/Beaker c/Glassware q/1
```

**Expected output**

```text
Item "Beaker" already exists in category "Glassware".
```

#### Step 3

**Input**

```text
mark-r 3 prepared
```

**Expected output**

```text
Successfully marked request 3 as prepared: Morgan Lee - 4x Gloves by 5 November 2099
```

#### Step 4

**Input**

```text
Bye
```

**Expected output (no output)**

```text

```

### UI-PERSIST-SAVE-FAIL-001: Keep unsaved data visible and allow cancel or explicit exit

**Aim:** Keep unsaved data visible and allow cancel or explicit exit with exact console comparisons and isolated storage.

**Preconditions:** Fresh process with the recorded storage fixture.

**Storage fixture:** `blocked-backup`

**Disk unchanged:** `true`

**Exit input:** `exit`

**Exit code:** `1`

#### Step 1

**Input**

```text
add-i n/Beaker c/Glassware q/10
```

**Expected output**

```text
Successfully added: 10x Beaker to the inventory
Warning: changes are only in memory. Could not save to {{STORAGE_PATH}}: The snapshot could not be saved.
```

#### Step 2

**Input**

```text
Bye
```

**Expected output**

```text
Warning: changes are only in memory. Could not save to {{STORAGE_PATH}}: The snapshot could not be saved.
Unsaved changes remain. Enter retry, cancel, or exit:
```

#### Step 3

**Input**

```text
cancel
```

**Expected output**

```text
Exit canceled. Unsaved changes remain in memory.
```

#### Step 4

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Glassware
1: Beaker (Qty: 10)
--------------------------------------------------------------------
```

#### Step 5

**Input**

```text
Bye
```

**Expected output**

```text
Warning: changes are only in memory. Could not save to {{STORAGE_PATH}}: The snapshot could not be saved.
Unsaved changes remain. Enter retry, cancel, or exit:
```

#### Step 6

**Input**

```text
exit
```

**Expected output**

```text
Exiting with unsaved changes.
```

### UI-PERSIST-SAVE-FAIL-002: Retry saving after storage becomes writable

**Aim:** Retry saving after storage becomes writable with exact console comparisons and isolated storage.

**Preconditions:** Cases sharing the recorded storage group run in order with a fresh process each time.

**Storage fixture:** `blocked-backup`

**Storage group:** `retry`

**Exit input:** `retry`

**Repair storage before step:** `3`

#### Step 1

**Input**

```text
add-i n/Beaker c/Glassware q/10
```

**Expected output**

```text
Successfully added: 10x Beaker to the inventory
Warning: changes are only in memory. Could not save to {{STORAGE_PATH}}: The snapshot could not be saved.
```

#### Step 2

**Input**

```text
Bye
```

**Expected output**

```text
Warning: changes are only in memory. Could not save to {{STORAGE_PATH}}: The snapshot could not be saved.
Unsaved changes remain. Enter retry, cancel, or exit:
```

#### Step 3

**Input**

```text
retry
```

**Expected output (no output)**

```text

```

### UI-PERSIST-SAVE-FAIL-003: Reload data committed by exit retry

**Aim:** Reload data committed by exit retry with exact console comparisons and isolated storage.

**Preconditions:** Cases sharing the recorded storage group run in order with a fresh process each time.

**Storage group:** `retry`

**Disk unchanged:** `true`

#### Step 1

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Glassware
1: Beaker (Qty: 10)
--------------------------------------------------------------------
```

### UI-PERSIST-SAVE-FAIL-004: Failed save at EOF exits without waiting for input

**Aim:** Failed save at EOF exits without waiting for input with exact console comparisons and isolated storage.

**Preconditions:** Fresh process with the recorded storage fixture.

**Storage fixture:** `blocked-backup`

**Disk unchanged:** `true`

**Exit code:** `1`

#### Step 1

**Input**

```text
add-i n/Beaker c/Glassware q/10
```

**Expected output**

```text
Successfully added: 10x Beaker to the inventory
Warning: changes are only in memory. Could not save to {{STORAGE_PATH}}: The snapshot could not be saved.
```

#### Step 2

**Input**

```text
Close standard input (EOF)
```

**Expected output**

```text
Warning: changes are only in memory. Could not save to {{STORAGE_PATH}}: The snapshot could not be saved.
Input ended with unsaved changes. The last saved data was kept.
```

### UI-PERSIST-FINAL-001: Create a new persisted lab workflow

**Aim:** Create a new persisted lab workflow using all six features and a fresh Java process per case.

**Preconditions:** Run FINAL-001 then FINAL-002 using their shared storage group. Start without saved files.

**Storage group:** `release-workflow`

#### Step 1

**Input**

```text
add-i n/Wire c/Components q/15
```

**Expected output**

```text
Successfully added: 15x Wire to the inventory
```

#### Step 2

**Input**

```text
add-s n/Practice Lab d/{{TOMORROW}} l/Room-A s/0900 e/1000 p/10
```

**Expected output**

```text
Successfully added: Practice Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
```

#### Step 3

**Input**

```text
add-p s/1 i/Wire c/Components q/2
```

**Expected output**

```text
Successfully added: 20x Wire [Components] to preparation
--------------------------------------------------------------------
Lab preparation: Practice Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 20, Available: 15) !! Insufficient items (Shortfall: 5)
--------------------------------------------------------------------
```

#### Step 4

**Input**

```text
add-r n/Jamie i/Wire q/5 d/{{TOMORROW}}
```

**Expected output**

```text
Successfully added request: Jamie - 5x Wire by {{TOMORROW}}
Request list updated. Run list-r to see the latest order.
```

#### Step 5

**Input**

```text
mark-r 1 fulfilled
```

**Expected output**

```text
Successfully marked request 1 as fulfilled: Jamie - 5x Wire by {{TOMORROW}}
```

#### Step 6

**Input**

```text
add-c n/Supplier t/01234567 e/orders@example.com r/Supplies
```

**Expected output**

```text
Successfully added: Supplier (01234567) to contacts
```

#### Step 7

**Input**

```text
add-l n/Wire c/Components q/5 b/Taylor t/02345678 d/{{TOMORROW}}
```

**Expected output**

```text
Successfully added loan: 5x Wire [Components] to Taylor (02345678), due {{TOMORROW}}
```

#### Step 8

**Input**

```text
Bye
```

**Expected output (no output)**

```text

```

### UI-PERSIST-FINAL-002: Restart the new workflow and return its loan

**Aim:** Restart the new workflow and return its loan using all six features and a fresh Java process per case.

**Preconditions:** Run FINAL-001 then FINAL-002 using their shared storage group. Start without saved files.

**Storage group:** `release-workflow`

#### Step 1

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 5)
--------------------------------------------------------------------
```

#### Step 2

**Input**

```text
list-s
```

**Expected output**

```text
--------------------------------------------------------------------
Sessions
--------------------------------------------------------------------
1. Practice Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
```

#### Step 3

**Input**

```text
list-p s/1
```

**Expected output**

```text
--------------------------------------------------------------------
Lab preparation: Practice Lab on {{TOMORROW}} at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 20, Available: 5) !! Insufficient items (Shortfall: 15)
--------------------------------------------------------------------
```

#### Step 4

**Input**

```text
list-r
```

**Expected output**

```text
--------------------------------------------------------------------
Student requests
--------------------------------------------------------------------
1. [Fulfilled] Jamie - 5x Wire by {{TOMORROW}} (Available: 5)
--------------------------------------------------------------------
```

#### Step 5

**Input**

```text
list-c
```

**Expected output**

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
1. Supplier (01234567, orders@example.com) - Supplies
--------------------------------------------------------------------
```

#### Step 6

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 5x Wire [Components] to Taylor (02345678), due {{TOMORROW}}
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
return 1
```

**Expected output**

```text
Successfully returned loan: 5x Wire [Components] to Taylor (02345678), due {{TOMORROW}}. Inventory restored.
```

#### Step 8

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 10)
--------------------------------------------------------------------
```

#### Step 9

**Input**

```text
Close standard input (EOF)
```

**Expected output (no output)**

```text

```

### UI-PERSIST-STORAGE-001: Console regression after adding storage

**Aim:** Verify inventory and loan commands retain their exact output after adding the JSON storage component.

**Preconditions:** Fresh process and an empty isolated working directory; no saved data.

#### Step 1

**Input**

```text
add-i n/Beaker c/Glassware q/10
```

**Expected output**

```text
Successfully added: 10x Beaker to the inventory
```

#### Step 2

**Input**

```text
add-l n/Beaker c/Glassware q/3 b/Taylor t/02345678 d/5 November 2099
```

**Expected output**

```text
Successfully added loan: 3x Beaker [Glassware] to Taylor (02345678), due 5 November 2099
```

#### Step 3

**Input**

```text
return 1
```

**Expected output**

```text
Successfully returned loan: 3x Beaker [Glassware] to Taylor (02345678), due 5 November 2099. Inventory restored.
```

#### Step 4

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Glassware
1: Beaker (Qty: 10)
--------------------------------------------------------------------
```


## Persistence and upstream merge regression contract

Broken-item reports and their contact links persist without repeating stock deductions. Request fulfillment
deducts stock once and remains final after restarting. These expectations follow the upstream feature contracts.
The three cases below run in order using one isolated storage group.

### UI-PERSIST-MERGE-001: Save broken reports and fulfilled requests together

**Aim:** Save broken reports and fulfilled requests together.

**Preconditions:** Run MERGE-001, MERGE-002, and MERGE-003 in order.

**Storage group:** `upstream-merge`

#### Step 1

**Input**

```text
add-i n/Meter c/Equipment q/10
```

**Expected output**

```text
Successfully added: 10x Meter to the inventory
```

#### Step 2

**Input**

```text
add-c n/Maintenance t/01234567
```

**Expected output**

```text
Successfully added: Maintenance (01234567) to contacts
```

#### Step 3

**Input**

```text
add-b n/Meter c/Equipment q/2 f/Cracked screen d/1 January 2020 m/1
```

**Expected output**

```text
Successfully recorded broken item: 2x Meter [Equipment], fault: Cracked screen, reported 1 January 2020. Available inventory reduced.
```

#### Step 4

**Input**

```text
add-r n/Jamie i/Meter q/3 d/{{TOMORROW}}
```

**Expected output**

```text
Successfully added request: Jamie - 3x Meter by {{TOMORROW}}
Request list updated. Run list-r to see the latest order.
```

#### Step 5

**Input**

```text
mark-r 1 fulfilled
```

**Expected output**

```text
Successfully marked request 1 as fulfilled: Jamie - 3x Meter by {{TOMORROW}}
```

#### Step 6

**Input**

```text
Bye
```

**Expected output (no output)**

```text

```

### UI-PERSIST-MERGE-002: Restore stock deductions and remove the selected maintenance contact

**Aim:** Restore stock deductions and remove the selected maintenance contact.

**Preconditions:** Run MERGE-001, MERGE-002, and MERGE-003 in order.

**Storage group:** `upstream-merge`

#### Step 1

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Equipment
1: Meter (Qty: 5)
--------------------------------------------------------------------
```

#### Step 2

**Input**

```text
list-b
```

**Expected output**

```text
--------------------------------------------------------------------
Broken items
--------------------------------------------------------------------
1. 2x Meter [Equipment], fault: Cracked screen, reported 1 January 2020
   Contact: Maintenance (01234567)
--------------------------------------------------------------------
```

#### Step 3

**Input**

```text
list-r
```

**Expected output**

```text
--------------------------------------------------------------------
Student requests
--------------------------------------------------------------------
1. [Fulfilled] Jamie - 3x Meter by {{TOMORROW}} (Available: 5)
--------------------------------------------------------------------
```

#### Step 4

**Input**

```text
delete-c 1
```

**Expected output**

```text
Successfully removed contact: Maintenance
```

#### Step 5

**Input**

```text
add-c n/Maintenance t/01234567
```

**Expected output**

```text
Successfully added: Maintenance (01234567) to contacts
```

#### Step 6

**Input**

```text
Bye
```

**Expected output (no output)**

```text

```

### UI-PERSIST-MERGE-003: Keep deleted contacts unavailable and restore broken units once

**Aim:** Keep deleted contacts unavailable and restore broken units once.

**Preconditions:** Run MERGE-001, MERGE-002, and MERGE-003 in order.

**Storage group:** `upstream-merge`

#### Step 1

**Input**

```text
list-b
```

**Expected output**

```text
--------------------------------------------------------------------
Broken items
--------------------------------------------------------------------
1. 2x Meter [Equipment], fault: Cracked screen, reported 1 January 2020
   Contact: Unavailable (contact deleted)
--------------------------------------------------------------------
```

#### Step 2

**Input**

```text
delete-b 1
```

**Expected output**

```text
Successfully deleted broken-item report: 2x Meter [Equipment], fault: Cracked screen, reported 1 January 2020. All units restored to inventory.
```

#### Step 3

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Equipment
1: Meter (Qty: 7)
--------------------------------------------------------------------
```

#### Step 4

**Input**

```text
Bye
```

**Expected output (no output)**

```text

```


## Loan history contract

Loan IDs remain permanent while action commands continue to use the latest active-list indices.
Storage retains full ISO offset timestamps; history displays their recorded local calendar dates, with Unknown
for imported recording dates and - for an active record's absent closure date. Returned and voided records
are terminal. Full returns and corrections retain distinct statuses and restore stock once.
The new list usage/help text is list-l [overdue|history|returned]. History queries never write saved files.

### UI-HISTORY-MODEL-001: Keep active indices correct when closing distinct duplicate loans

**Aim:** Keep active indices correct when closing distinct duplicate loans.

**Preconditions:** Fresh process; explicit storage groups share files in listed order.

#### Step 1

**Input**

```text
add-i n/Meter c/Equipment q/5
```

**Expected output**

```text
Successfully added: 5x Meter to the inventory
```

#### Step 2

**Input**

```text
add-l n/Meter c/Equipment q/2 b/Jamie t/01234567 d/31 December 2099
```

**Expected output**

```text
Successfully added loan: 2x Meter [Equipment] to Jamie (01234567), due 31 December 2099
```

#### Step 3

**Input**

```text
add-l n/Meter c/Equipment q/1 b/Jamie t/01234567 d/31 December 2099
```

**Expected output**

```text
Successfully added loan: 1x Meter [Equipment] to Jamie (01234567), due 31 December 2099
```

#### Step 4

**Input**

```text
return 1
```

**Expected output**

```text
Successfully returned loan: 2x Meter [Equipment] to Jamie (01234567), due 31 December 2099. Inventory restored.
```

#### Step 5

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 1x Meter [Equipment] to Jamie (01234567), due 31 December 2099
--------------------------------------------------------------------
```

#### Step 6

**Input**

```text
delete-l 1
```

**Expected output**

```text
Successfully deleted incorrect loan: 1x Meter [Equipment] to Jamie (01234567), due 31 December 2099. Inventory restored.
```

#### Step 7

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
No active loans.
--------------------------------------------------------------------
```

#### Step 8

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Equipment
1: Meter (Qty: 5)
--------------------------------------------------------------------
```

#### Step 9

**Input**

```text
Bye
```

**Expected output (no output)**

```text

```

### UI-HISTORY-STORAGE-001: Restore a version-2 ledger and return only its active loan

**Aim:** Restore a version-2 ledger and return only its active loan.

**Preconditions:** Fresh process; explicit storage groups share files in listed order.

**Storage fixture:** `history`

**Storage group:** `loan-history-restart`

#### Step 1

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Equipment
1: Meter (Qty: 7)
--------------------------------------------------------------------
```

#### Step 2

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 2x Meter [Equipment] to Jamie (01234567), due 31 December 2099
--------------------------------------------------------------------
```

#### Step 3

**Input**

```text
return 1
```

**Expected output**

```text
Successfully returned loan: 2x Meter [Equipment] to Jamie (01234567), due 31 December 2099. Inventory restored.
```

#### Step 4

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Equipment
1: Meter (Qty: 9)
--------------------------------------------------------------------
```

#### Step 5

**Input**

```text
Bye
```

**Expected output (no output)**

```text

```

### UI-HISTORY-STORAGE-002: Keep closed loans closed after restarting

**Aim:** Keep closed loans closed after restarting.

**Preconditions:** Fresh process; explicit storage groups share files in listed order.

**Storage group:** `loan-history-restart`

**Disk unchanged:** `true`

#### Step 1

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
No active loans.
--------------------------------------------------------------------
```

#### Step 2

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Equipment
1: Meter (Qty: 9)
--------------------------------------------------------------------
```

#### Step 3

**Input**

```text
return 1
```

**Expected output**

```text
Loan index 1 is invalid. There are 0 active loan(s).
```

#### Step 4

**Input**

```text
Bye
```

**Expected output (no output)**

```text

```

### UI-HISTORY-VIEW-001: List a mixed ledger and returned-only view without writing

**Aim:** List a mixed ledger and returned-only view without writing.

**Preconditions:** Fresh process; explicit storage groups share files in listed order.

**Storage fixture:** `history`

**Disk unchanged:** `true`

#### Step 1

**Input**

```text
list-l history
```

**Expected output**

```text
--------------------------------------------------------------------
Loan history
--------------------------------------------------------------------
Loan ID 1 [RETURNED] 2x Meter [Equipment] to Jamie (01234567), due 31 December 2099
   Recorded: 1 January 2020 | Closed: 2 January 2020
Loan ID 2 [VOIDED] 1x Meter [Equipment] to Jamie (01234567), due 31 December 2099
   Recorded: 1 January 2020 | Closed: 3 January 2020
Loan ID 3 [ACTIVE] 2x Meter [Equipment] to Jamie (01234567), due 31 December 2099
   Recorded: Unknown | Closed: -
Loan IDs are permanent. Use list-l for active indices.
--------------------------------------------------------------------
```

#### Step 2

**Input**

```text
list-l returned
```

**Expected output**

```text
--------------------------------------------------------------------
Returned loans
--------------------------------------------------------------------
Loan ID 1 [RETURNED] 2x Meter [Equipment] to Jamie (01234567), due 31 December 2099
   Recorded: 1 January 2020 | Closed: 2 January 2020
Loan IDs are permanent. Use list-l for active indices.
--------------------------------------------------------------------
```

#### Step 3

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 2x Meter [Equipment] to Jamie (01234567), due 31 December 2099
--------------------------------------------------------------------
```

#### Step 4

**Input**

```text
Bye
```

**Expected output (no output)**

```text

```

### UI-HISTORY-VIEW-002: Handle empty history and strict filter syntax

**Aim:** Handle empty history and strict filter syntax.

**Preconditions:** Fresh process; explicit storage groups share files in listed order.

**Disk unchanged:** `true`

#### Step 1

**Input**

```text
list-l history
```

**Expected output**

```text
--------------------------------------------------------------------
Loan history
--------------------------------------------------------------------
No loan records.
--------------------------------------------------------------------
```

#### Step 2

**Input**

```text
list-l returned
```

**Expected output**

```text
--------------------------------------------------------------------
Returned loans
--------------------------------------------------------------------
No returned loans.
--------------------------------------------------------------------
```

#### Step 3

**Input**

```text
list-l HISTORY
```

**Expected output**

```text
Invalid format. Use: list-l [overdue|history|returned]
```

#### Step 4

**Input**

```text
list-l history extra
```

**Expected output**

```text
Invalid format. Use: list-l [overdue|history|returned]
```

#### Step 5

**Input**

```text
Bye
```

**Expected output (no output)**

```text

```

### UI-HISTORY-VIEW-003: Retain new returned and voided loans with stable IDs

**Aim:** Retain new returned and voided loans with stable IDs.

**Preconditions:** Fresh process; explicit storage groups share files in listed order.

**Storage group:** `loan-history-workflow`

#### Step 1

**Input**

```text
add-i n/Meter c/Equipment q/5
```

**Expected output**

```text
Successfully added: 5x Meter to the inventory
```

#### Step 2

**Input**

```text
add-l n/Meter c/Equipment q/2 b/Jamie t/01234567 d/31 December 2099
```

**Expected output**

```text
Successfully added loan: 2x Meter [Equipment] to Jamie (01234567), due 31 December 2099
```

#### Step 3

**Input**

```text
add-l n/Meter c/Equipment q/1 b/Jamie t/01234567 d/31 December 2099
```

**Expected output**

```text
Successfully added loan: 1x Meter [Equipment] to Jamie (01234567), due 31 December 2099
```

#### Step 4

**Input**

```text
return 1
```

**Expected output**

```text
Successfully returned loan: 2x Meter [Equipment] to Jamie (01234567), due 31 December 2099. Inventory restored.
```

#### Step 5

**Input**

```text
delete-l 1
```

**Expected output**

```text
Successfully deleted incorrect loan: 1x Meter [Equipment] to Jamie (01234567), due 31 December 2099. Inventory restored.
```

#### Step 6

**Input**

```text
list-l history
```

**Expected output**

```text
--------------------------------------------------------------------
Loan history
--------------------------------------------------------------------
Loan ID 1 [RETURNED] 2x Meter [Equipment] to Jamie (01234567), due 31 December 2099
   Recorded: {{TODAY}} | Closed: {{TODAY}}
Loan ID 2 [VOIDED] 1x Meter [Equipment] to Jamie (01234567), due 31 December 2099
   Recorded: {{TODAY}} | Closed: {{TODAY}}
Loan IDs are permanent. Use list-l for active indices.
--------------------------------------------------------------------
```

#### Step 7

**Input**

```text
Bye
```

**Expected output (no output)**

```text

```

### UI-HISTORY-VIEW-004: Retain history and next ID across restarts

**Aim:** Retain history and next ID across restarts.

**Preconditions:** Fresh process; explicit storage groups share files in listed order.

**Storage group:** `loan-history-workflow`

#### Step 1

**Input**

```text
list-l history
```

**Expected output**

```text
--------------------------------------------------------------------
Loan history
--------------------------------------------------------------------
Loan ID 1 [RETURNED] 2x Meter [Equipment] to Jamie (01234567), due 31 December 2099
   Recorded: {{TODAY}} | Closed: {{TODAY}}
Loan ID 2 [VOIDED] 1x Meter [Equipment] to Jamie (01234567), due 31 December 2099
   Recorded: {{TODAY}} | Closed: {{TODAY}}
Loan IDs are permanent. Use list-l for active indices.
--------------------------------------------------------------------
```

#### Step 2

**Input**

```text
add-l n/Meter c/Equipment q/1 b/Jamie t/01234567 d/31 December 2099
```

**Expected output**

```text
Successfully added loan: 1x Meter [Equipment] to Jamie (01234567), due 31 December 2099
```

#### Step 3

**Input**

```text
list-l history
```

**Expected output**

```text
--------------------------------------------------------------------
Loan history
--------------------------------------------------------------------
Loan ID 1 [RETURNED] 2x Meter [Equipment] to Jamie (01234567), due 31 December 2099
   Recorded: {{TODAY}} | Closed: {{TODAY}}
Loan ID 2 [VOIDED] 1x Meter [Equipment] to Jamie (01234567), due 31 December 2099
   Recorded: {{TODAY}} | Closed: {{TODAY}}
Loan ID 3 [ACTIVE] 1x Meter [Equipment] to Jamie (01234567), due 31 December 2099
   Recorded: {{TODAY}} | Closed: -
Loan IDs are permanent. Use list-l for active indices.
--------------------------------------------------------------------
```

#### Step 4

**Input**

```text
list-l
```

**Expected output**

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 1x Meter [Equipment] to Jamie (01234567), due 31 December 2099
--------------------------------------------------------------------
```

#### Step 5

**Input**

```text
list-i
```

**Expected output**

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Equipment
1: Meter (Qty: 4)
--------------------------------------------------------------------
```

#### Step 6

**Input**

```text
Bye
```

**Expected output (no output)**

```text

```

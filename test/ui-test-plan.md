# UI Test Plan

This file is the source of truth for repeatable console UI test cases run with the `test-ui` skill.

The course-style I/O redirection test in `text-ui-test/input.txt` and `text-ui-test/EXPECTED.txt`
reuses UI-PREP-001, substituting the fixed future date `31 December 2099` for `{{TOMORROW}}`.
Its shell/batch script compares the full session after execution. The remaining cases below
are retained for manual command-by-command testing.

## Test environment

- Java: 25
- Build command: `gradlew.bat shadowJar` on Windows or `./gradlew shadowJar` on macOS/Linux
- Run command: `java -jar build/libs/duke.jar`
- Comparison: exact after newline normalization, ANSI-sequence removal, and ignoring one final newline
- Isolation: each test case starts a fresh application process
- Command response terminator: each ordinary command emits exactly one additional blank line after its expected output; EOF and `Bye` exit without a response terminator
- Diagnostics: stderr is captured separately from exact stdout comparisons; normal INFO/WARNING logs are allowed, while nonzero exits and SEVERE failures fail the case
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
Quantity must be a positive integer.
```

#### Step 4

**Input**

```text
add-i n/Wire c/Components q/-5
```

**Expected output**

```text
Quantity must be a positive integer.
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
Quantity must be a positive integer.
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
Quantity must be a positive integer.
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
Date must be in the format 'd MMMM yyyy' (e.g., 16 September 2026). Ensure the month is capitalized.
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
Date must be in the format 'd MMMM yyyy' (e.g., 16 September 2026). Ensure the month is capitalized.
```

#### Step 2

**Input**

```text
add-s n/Test Lab d/2099-01-01 l/Room-A s/0900 e/1000 p/20
```

**Expected output**

```text
Date must be in the format 'd MMMM yyyy' (e.g., 16 September 2026). Ensure the month is capitalized.
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
Quantity must be a positive integer.
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
Quantity must be a positive integer.
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
Lab sessions
  add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT
  delete-s INDEX
  list-s
Lab preparation
  add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]
  delete-p s/SESSION_INDEX i/ITEM_INDEX q/TOTAL_QUANTITY
  list-p s/SESSION_INDEX
Contacts
  add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]
  delete-c INDEX
  list-c
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
Lab sessions
  add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT
  delete-s INDEX
  list-s
Lab preparation
  add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]
  delete-p s/SESSION_INDEX i/ITEM_INDEX q/TOTAL_QUANTITY
  list-p s/SESSION_INDEX
Contacts
  add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]
  delete-c INDEX
  list-c
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
add-c n/NUS Infrastructure t/65161234 r/Aircon and lighting repairs
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
1. NUS Infrastructure (65161234) - Aircon and lighting repairs
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
1. NUS Infrastructure (65161234) - Aircon and lighting repairs
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

### UI-SESSION-LIST-003: Sort sessions by date then time then name

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

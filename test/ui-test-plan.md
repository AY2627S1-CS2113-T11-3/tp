# UI Test Plan

This file is the source of truth for repeatable console UI test cases run with the `test-ui` skill.

## Test environment

- Java: 25
- Build command: `gradlew.bat shadowJar` on Windows or `./gradlew shadowJar` on macOS/Linux
- Run command: `java -jar build/libs/duke.jar`
- Comparison: exact after newline normalization, ANSI-sequence removal, and ignoring one final newline
- Isolation: each test case starts a fresh application process
- Command response terminator: after every expected-output block, the application must emit exactly one additional blank line; this blank line is part of the expected response
- Startup whitespace notation: each `␠` in the startup fixture represents one literal trailing space and is decoded before comparison
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

## Test cases

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
Date must be a valid date in d MMMM uuuu format.
```

#### Step 9

**Input**

```text
add-s n/Test Lab d/{{TOMORROW}} l/Room-A s/ e/1000 p/20
```

**Expected output**

```text
Start time must be a valid time in HHmm format.
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
Date must be a valid date in d MMMM uuuu format.
```

#### Step 2

**Input**

```text
add-s n/Test Lab d/2099-01-01 l/Room-A s/0900 e/1000 p/20
```

**Expected output**

```text
Date must be a valid date in d MMMM uuuu format.
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
Start time must be a valid time in HHmm format.
```

#### Step 5

**Input**

```text
add-s n/Test Lab d/{{TOMORROW}} l/Room-A s/0900 e/2400 p/20
```

**Expected output**

```text
End time must be a valid time in HHmm format.
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

### UI-SESSION-LIST-002: List sessions in full format and insertion order

**Aim:** Verify that scheduled sessions retain their insertion order and use the approved full display format.

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
add-s n/Second Lab d/{{TOMORROW}} l/Room-B s/0930 e/1030 p/25
```

**Expected output**

```text
Successfully added: Second Lab on {{TOMORROW}} at Room-B from 0930 to 1030 for 25 attendees
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

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

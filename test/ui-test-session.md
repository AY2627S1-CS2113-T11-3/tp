# UI Test Session

Latest result: **PASS** - 8 test cases and 43 feature commands.

## Environment

```text
[java] 25.0.4
[build] gradlew.bat shadowJar
[build] BUILD SUCCESSFUL
```

Each case started a fresh process. The following startup output matched exactly before every case; `␠` represents a literal trailing space.

```text
[app]   ____        _␠␠␠␠␠
[app]  |  _ \      | |␠␠␠␠
[app]  | |_) | ___ | |__␠␠
[app]  |  _ < / _ \| '_ \␠
[app]  | |_) | (_) | |_) |
[app]  |____/ \___/|_.__/␠
[app]
[app] What is your name?
[input] Alex
[app] Hello Alex, welcome to Bob!
[app] --------------------------------------------------------------------
```

`[app] <blank line>` below records the exact response terminator emitted after every feature command.

## UI-ADD-001

```text
[startup] Matched common startup exactly
[input] add n/Wire c/Components
[app] Invalid command
[app] <blank line>
[input] add-i c/Components q/10
[app] Invalid format. Use: add-i n/NAME c/CATEGORY [q/QUANTITY]
[app] <blank line>
[input] add-i n/Wire q/10 c/Components
[app] Invalid format. Use: add-i n/NAME c/CATEGORY [q/QUANTITY]
[app] <blank line>
[input] add-i n/Wire n/Cable c/Components q/10
[app] Invalid format. Use: add-i n/NAME c/CATEGORY [q/QUANTITY]
[app] <blank line>
[input] add-i n/ c/Components q/10
[app] Item name cannot be blank.
[app] <blank line>
[input] add-i n/Wire c/ q/10
[app] Category cannot be blank.
[app] <blank line>
[input] add-i n/Wire c/Components x/extra
[app] Invalid format. Use: add-i n/NAME c/CATEGORY [q/QUANTITY]
[app] <blank line>
[cleanup] Bye
[process] Exited with code 0
```

## UI-ADD-002

```text
[startup] Matched common startup exactly
[input] add-i n/Wire c/Components q/abc
[app] Quantity must be a positive integer.
[app] <blank line>
[input] add-i n/Wire c/Components q/
[app] Quantity must be a positive integer.
[app] <blank line>
[input] add-i n/Wire c/Components q/0
[app] Quantity must be a positive integer.
[app] <blank line>
[input] add-i n/Wire c/Components q/-5
[app] Quantity must be a positive integer.
[app] <blank line>
[input] add-i n/Wire c/Components q/2147483648
[app] Quantity must be a positive integer.
[app] <blank line>
[cleanup] Bye
[process] Exited with code 0
```

## UI-ADD-003

```text
[startup] Matched common startup exactly
[input] add-i n/Oscilloscope c/Equipment
[app] Successfully added: 1x Oscilloscope to the inventory
[app] <blank line>
[input] add-i n/10k Resistor c/Components q/500
[app] Successfully added: 500x 10k Resistor to the inventory
[app] <blank line>
[input] add-i n/oscilloscope c/equipment q/2
[app] Item "oscilloscope" already exists in category "Equipment".
[app] <blank line>
[cleanup] Bye
[process] Exited with code 0
```

## UI-DELETE-001

```text
[startup] Matched common startup exactly
[input] delete-i
[app] Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY
[app] <blank line>
[input] delete-i c/Components q/1
[app] Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY
[app] <blank line>
[input] delete-i i/1 c/Components q/1
[app] Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY
[app] <blank line>
[input] delete-i c/Components i/1 i/2 q/1
[app] Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY
[app] <blank line>
[input] delete-i c/ i/1 q/1
[app] Category cannot be blank.
[app] <blank line>
[input] delete-i c/Components i/1 q/1 x/extra
[app] Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY
[app] <blank line>
[cleanup] Bye
[process] Exited with code 0
```

## UI-DELETE-002

```text
[startup] Matched common startup exactly
[input] delete-i c/Components i/abc q/1
[app] Item index must be a positive integer.
[app] <blank line>
[input] delete-i c/Components i/0 q/1
[app] Item index must be a positive integer.
[app] <blank line>
[input] delete-i c/Components i/2147483648 q/1
[app] Item index must be a positive integer.
[app] <blank line>
[input] delete-i c/Components i/1 q/0
[app] Quantity must be a positive integer.
[app] <blank line>
[input] delete-i c/Components i/-1 q/1
[app] Item index must be a positive integer.
[app] <blank line>
[input] delete-i c/Components i/ q/1
[app] Item index must be a positive integer.
[app] <blank line>
[input] delete-i c/Components i/1 q/abc
[app] Quantity must be a positive integer.
[app] <blank line>
[input] delete-i c/Components i/1 q/
[app] Quantity must be a positive integer.
[app] <blank line>
[input] delete-i c/Components i/1 q/-5
[app] Quantity must be a positive integer.
[app] <blank line>
[cleanup] Bye
[process] Exited with code 0
```

## UI-DELETE-003

```text
[startup] Matched common startup exactly
[input] add-i n/Wire c/Components q/10
[app] Successfully added: 10x Wire to the inventory
[app] <blank line>
[input] delete-i c/Equipment i/1 q/1
[app] Category "Equipment" does not exist.
[app] <blank line>
[input] delete-i c/components i/2 q/1
[app] Item index 2 is invalid. Category "Components" contains 1 item(s).
[app] <blank line>
[input] delete-i c/components i/1 q/11
[app] Cannot remove 11 Wire. Only 10 are available.
[app] <blank line>
[input] delete-i c/components i/1 q/4
[app] Successfully removed: 4x Wire from the inventory
[app] <blank line>
[input] delete-i c/Components i/1 q/6
[app] Successfully removed: 6x Wire from the inventory
[app] <blank line>
[input] delete-i c/Components i/1 q/1
[app] Category "Components" does not exist.
[app] <blank line>
[cleanup] Bye
[process] Exited with code 0
```

## UI-LIST-001

```text
[startup] Matched common startup exactly
[input] list-i extra
[app] Invalid format. Use: list-i
[app] <blank line>
[input] list-i
[app] --------------------------------------------------------------------
[app] Inventory
[app] --------------------------------------------------------------------
[app] No items in inventory.
[app] --------------------------------------------------------------------
[app] <blank line>
[cleanup] Bye
[process] Exited with code 0
```

## UI-LIST-002

```text
[startup] Matched common startup exactly
[input] add-i n/Oscilloscope c/Equipment
[app] Successfully added: 1x Oscilloscope to the inventory
[app] <blank line>
[input] add-i n/Wire c/Components q/20
[app] Successfully added: 20x Wire to the inventory
[app] <blank line>
[input] add-i n/Red LED c/components q/30
[app] Successfully added: 30x Red LED to the inventory
[app] <blank line>
[input] list-i
[app] --------------------------------------------------------------------
[app] Inventory
[app] --------------------------------------------------------------------
[app] Equipment
[app] 1: Oscilloscope (Qty: 1)
[app] Components
[app] 1: Wire (Qty: 20)
[app] 2: Red LED (Qty: 30)
[app] --------------------------------------------------------------------
[app] <blank line>
[cleanup] Bye
[process] Exited with code 0
```

## Results

- UI-ADD-001: 7/7 steps passed.
- UI-ADD-002: 5/5 steps passed.
- UI-ADD-003: 3/3 steps passed.
- UI-DELETE-001: 6/6 steps passed.
- UI-DELETE-002: 9/9 steps passed.
- UI-DELETE-003: 7/7 steps passed.
- UI-LIST-001: 2/2 steps passed.
- UI-LIST-002: 4/4 steps passed.

All 8 test cases and 43 feature-command steps passed.

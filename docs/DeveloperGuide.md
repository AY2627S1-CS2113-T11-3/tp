# Developer Guide

## Acknowledgements

{list here sources of all reused/adapted ideas, code, documentation, and third-party libraries -- include links to the original source as well}

## Design & implementation

{Describe the design and implementation of the product. Use UML diagrams and short code snippets where applicable.}

### Contact management

`add-c`, `list-c`, and `delete-c` follow the same structure as sessions: `Contact` is the validated data class,
`ContactManager` owns the list and prints confirmations, and `Parser` dispatches the commands. `delete-c` takes a
single index on the command line, like `delete-s`.

The user guide did not specify the rules below. They are design decisions made when implementing the feature and
can be changed in `Contact` (validation) or `Parser` (`ADD_CONTACT_PREFIXES`):

| Decision | Rule | Rationale |
| --- | --- | --- |
| Phone format | Digits only, no length check | Accepts local and international numbers. Rejecting separators keeps one canonical form. A length check would wrongly reject some valid numbers. |
| Email format | Contains `@`, no whitespace | Catches obvious typos without a full email grammar. |
| Duplicate contacts | Allowed | Two suppliers can share a name or switchboard number. Unlike `add-i`, a contact has no natural unique key. |
| Optional fields | `e/` before `r/`; a supplied value must not be blank | Matches the fixed prefix order used by every other command. |
| Empty list | `list-c` shows `No contacts saved.` | Matches `list-s`, which shows `No sessions scheduled.` |

Known limitation: prefixes are detected by a shared pattern (a single letter followed by `/` at the start of a
word), so a value containing a word such as `a/b` cannot be entered. This affects every command, not only contacts.

## Item Management

The item management system is facilitated by `Inventory` and `InventoryItem`.

It organizes items under various categories (e.g Components/Equipments) and stores it internally as a `LinkedHashMap<String, ArrayList<InventoryItem>>`.

By storing the data in a LinkedHashMap, with item categories as keys and the items in each category as the corresponding ArrayList values. It is easier for the program to display the items for each category by iterating over the items in its ArrayList.

![Inventory categories](images/item-management.png)

`Inventory#addItem(InventoryItem item)` - Adds an item into its corresponding category based on the given parameters, and it creates the category if it does not exist in the inventory.

`Inventory#findCategory(String requestedCategory)` - Finds the stored display name for a category without requiring matching capitalization.

`Inventory#hasValidState()` - Returns whether all stored categories and items satisfy inventory requirements.

`Inventory#removeItem(String category, int userIndex, int quantity)` - Removes the stated quantity of the item from the inventory. If the item's quantity drops to 0, the item is removed from the inventory entirely.

`Inventory#getAvailableQuantity(String name, String category)` - Returns current stock matching a preparation item's name and category. Matching is case-insensitive and does not reserve or remove stock.

`Inventory#listItems()` - Displays all items inside the inventory based on its category.

### Inventory Item

`InventoryItem#InventoryItem(String name, String category, int quantity)` - Creates an inventory item with the supplied name, category, and quantity.

`InventoryItem#IgetName()` - Returns the name of the item

`InventoryItem#IgetCategory()` - Returns the category of the item

`InventoryItem#IgetQuantity()` - Returns the quantity of the item

`InventoryItem#IremoveQuantity(int quantityToRemove)` - Removes the stated quantity of the item from the inventory.

Step 1. The user launches the application for the first time. The inventory will be initialized with the initial inventory state,

> **Note:** Executing list-i on an empty inventory prints "No items in inventory."

Step 2. The user executes `“add-i n/Resistor c/Components q/100”` to add a new item. Since Components does not exist as a key in the map yet, a new ArrayList is created under the key Components, and the new InventoryItem is inserted into it.
**Note**: If the command is malformed, the program prints: Invalid format. Use: add-i n/NAME c/CATEGORY [q/QUANTITY]. If q/ is omitted, the quantity defaults to 1.

Step 3. The user executes `“add-i n/Capacitor c/Components q/50”`. Since Components now already exists as a key, the new item is simply appended to the existing ArrayList under that key.

> **Note**: Duplicate names are rejected case-insensitively, adding n/resistor fails with Item "resistor" already exists in category "Components".

Step 4. The user executes `“list-i”` to view the inventory. `Inventory#listItems()` iterates over each category key in insertion order and prints the items inside each category's ArrayList.

> **Note**: list-i accepts no arguments; any extra input is rejected with Invalid format. Use: list-i.

Step 5. The user executes `“delete-i c/Components i/1 q/20”` to use up some stock. `Inventory#removeItem()` locates the Resistor and calls `InventoryItem#removeQuantity(20)`, leaving it at 80 units.

Step 6. The user executes `“delete-i c/Components i/2 q/50”` to remove all remaining stock of the Capacitor. The quantity drops to 0, so the Capacitor is removed from the ArrayList entirely. If the category's list is now empty, the category key itself is removed from the LinkedHashMap.

> **Note:** A failed command does not modify the inventory at all, the state only changes after all checks pass, so stock can never go negative.

## Lab Session Scheduling

Lab session scheduling supports adding, listing, and deleting sessions through the `add-s`, `list-s`, and `delete-s` commands. Responsibilities are divided between three classes:

- **Parser** validates command syntax, extracts arguments, and dispatches scheduling operations.
- **Session** validates individual session details and defines ordering and conflict detection.
- **SessionManager** maintains the schedule, rejects conflicting additions, and retrieves or removes sessions using displayed indices.

`SessionManager` stores sessions in an `ArrayList<Session>`, sorted after each successful addition. Displayed indices are one-based list positions and can change after additions or deletions. Each `Session` owns a `Preparation` object, so preparation remains associated with the same session when its position changes.

![Lab session scheduling class diagram](images/lab-session-scheduling-dg.png)

*Figure 1: Main scheduling classes and relationships. Selected members are shown; constructors, routine getters, and preparation internals are omitted.*

### Key operations

- `SessionManager#addSession(Session session)` rejects conflicts before insertion, then adds the session and sorts the schedule.
- `Session#conflictsWith(Session other)` checks for overlapping times on the same date at the same location.
- `Session#compareTo(Session other)` orders sessions by date, then start time, then name ignoring capitalization.
- `SessionManager#getSession(int userIndex)` validates a displayed index and retrieves its session.
- `SessionManager#deleteSession(int userIndex)` validates and retrieves the session through `getSession()` before removing it.
- `SessionManager#listSessions()` displays numbered sessions in stored order, or an empty-schedule message.

### Example walkthrough

Assume that 5 November 2099 is a future date.

**Step 1: Initialize an empty schedule**

At startup, `Bob` creates a `SessionManager` containing an empty `ArrayList<Session>`. Calling `list-s` displays the empty-schedule message.

**Step 2: Add an afternoon session**

The user enters:

`add-s n/Embedded Systems Lab d/5 November 2099 l/Room-A s/1300 e/1500 p/18`

`Parser` checks the prefixes, extracts values, converts the headcount, and constructs a validated `Session`. The manager finds no existing sessions, inserts the candidate, sorts the list, and prints confirmation. Embedded Systems Lab receives index 1.

**Step 3: Add an earlier session and reorder the schedule**

The user enters:

`add-s n/Digital Design Lab d/5 November 2099 l/Room-A s/0900 e/1100 p/24`

The sessions share a date and location, but their times do not overlap. The manager accepts the addition and calls `Collections.sort()`, which uses `Session#compareTo()`.

| Displayed index | Session | Time |
| --- | --- | --- |
| 1 | Digital Design Lab | 0900–1100 |
| 2 | Embedded Systems Lab | 1300–1500 |

Embedded Systems Lab moves to index 2; its session and preparation objects remain unchanged.

**Step 4: Reject a conflicting session**

The user enters:

`add-s n/Circuit Lab d/5 November 2099 l/Room-A s/1000 e/1200 p/12`

Construction succeeds, but `Session#conflictsWith()` detects a conflict with Digital Design Lab because:

- Their dates match.
- Their locations match, ignoring capitalization.
- Each session starts before the other ends.

The manager throws `SessionConflictException` before insertion. `Parser` displays the error, and the schedule remains unchanged. These time comparisons are strict, so sessions meeting exactly at an endpoint are allowed.

**Step 5: Associate preparation with a session**

The user enters:

`add-p s/2 i/Breadboard c/Equipments q/2`

`Parser` retrieves Embedded Systems Lab through `getSession(2)` and accesses its `Preparation` object. With 18 attendees, the requirement is 36 breadboards. This records a requirement without reserving inventory. Preparation calculates required quantities, while Parser compares these requirements with inventory availability.

**Step 6: Delete another session without losing preparation**

The user enters:

`delete-s 1`

`deleteSession()` retrieves Digital Design Lab through `getSession(1)`, converts the displayed index to a zero-based position, and removes it.

Embedded Systems Lab becomes index 1 while retaining its requirement of 36 breadboards. Calling `list-p s/1` accesses that same preparation. Calling `delete-s 2` now fails because only one session remains.

### Interactions during session addition

The sequence diagram shows successful addition. Construction precedes conflict checking; every comparison returns `false` before insertion and sorting. Descriptive labels summarize routine implementation details.

![Successful session addition sequence diagram](images/add-session-sequence.png)

*Figure 2: Successful session addition. The loop checks each stored session and is skipped when the schedule is empty.*

If parsing or construction fails, the manager is not asked to add the session. If a conflict is detected, addition stops before insertion, sorting, and success confirmation.

### Design considerations

**Aspect 1: Representing and ordering the schedule**

**Current approach:** Store sessions in an `ArrayList` and sort after each successful addition.

Stored and displayed positions remain consistent, simplifying retrieval and deletion. The tradeoff is scanning existing sessions for conflicts and sorting after insertion, which is sufficient for a small schedule.

**Alternative:** Preserve insertion order and sort a separate copy for display.

This avoids sorting the stored list, but displayed indices would require mapping back to the original collection. Keeping the stored list sorted avoids this additional mapping.

**Aspect 2: Associating preparation with sessions**

**Current approach:** Each `Session` owns its `Preparation` object.

Sorting moves session references without changing their associated preparation. Deleting another session therefore cannot transfer requirements to the wrong session. This intentionally couples a session with its preparation.

**Alternative:** Maintain preparation separately using session indices.

Both collections would require synchronization after sorting or deletion. Storing preparation inside the session avoids this dependency on changing positions.

### Validation, assertions, and logging

| Component | Responsibility |
| --- | --- |
| `Parser` | Command structure, required prefix order, numeric conversion, and positive numeric arguments. |
| `Session` | Nonblank name and location, positive headcount, valid date and time values, date not before today, and start time before end time. |
| `SessionManager` | Rejecting null additions, checking scheduling conflicts, and validating indices against the current schedule. |

Expected command errors are reported through exceptions before the relevant insertion or deletion occurs.

When Java assertions are enabled, `hasValidState()` checks for nonnull sessions and pairwise absence of conflicts after additions and deletions. It does not verify sorting. Assertions supplement ordinary validation; user-input checks do not depend on them.

`SessionManager` uses `java.util.logging.Logger` to record attempted and successful additions and deletions at `INFO` level, and conflicts or invalid indices at `WARNING` level.

### Current limitations

- Sessions cannot span midnight. Date checks occur during construction; sessions are not automatically removed afterward, and today’s start time is not compared with the current clock time.
- There is no command to edit a session. Deleting and recreating one produces a new session with empty preparation.
- The schedule exists only in memory and is not preserved after the application exits.

## Product scope
### Target user profile

{Describe the target user profile}

### Value proposition

{Describe the value proposition: what problem does it solve?}

## User Stories

|Version| As a ... | I want to ... | So that I can ...|
|--------|----------|---------------|------------------|
|v1.0|new user|see usage instructions|refer to them when I forget how to use the application|
|v2.0|user|find a to-do item by name|locate a to-do without having to go through the entire list|

## Non-Functional Requirements

{Give non-functional requirements}

## Glossary

* *glossary item* - Definition

## Instructions for manual testing

{Give instructions on how to do a manual product testing e.g., how to load sample data to be used for testing}

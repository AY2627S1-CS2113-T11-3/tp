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

## Student request tracking

`add-r`, `delete-r`, `list-r`, and `mark-r` follow the same structure as the other features: `Request` validates and
formats one request, `RequestManager` owns the deadline-sorted list and prints confirmations, and `Parser` dispatches
the commands. `Request` is the counterpart of `Contact`, while `RequestManager` mirrors `ContactManager` and
`SessionManager`.

A request records the student's name, item name, quantity, deadline, and status. Unlike a preparation item, a request
does not name a category. Availability is therefore calculated by
`Inventory#getTotalAvailableQuantity(String name)`, which adds the stock of that item name across all categories
without reserving or deducting anything. `RequestManager#listRequests(Inventory)` calls this method whenever the list
is displayed, so later inventory changes are reflected immediately.

`add-r` builds a `Request`, adds it to the manager, and sorts the list by deadline. `delete-r INDEX` and
`mark-r INDEX STATUS` use the same one-based positions shown by `list-r`. Because the list is sorted, deleting or
marking always acts on the row the user sees. `mark-r` accepts `prepared` or `fulfilled` in any capitalization and
replaces the current status; a new request starts as `pending`.

For example:

```text
add-r n/Alice i/Wire q/20 d/8 October 2099
list-r
mark-r 1 prepared
```

`add-r` prints a confirmation, `list-r` shows the pending request with current availability, and `mark-r` changes the
status shown by the next `list-r`. None of these commands changes inventory stock.

| Decision | Rule | Rationale |
| --- | --- | --- |
| Deadline format | `d MMMM yyyy`, English month names with an initial capital; today or later | Reuses the session date format and keeps deadlines unambiguous. |
| Deadline ordering | Earliest deadline first; ties sorted by student name, then item name | Gives a stable list order, so indices do not change between identical listings. |
| Item category | Not recorded | The requirement lists only student, item, quantity, and deadline. Availability sums stock across categories with the same item name. |
| Duplicate requests | Allowed | The same student may request the same item again for a different deadline or quantity. |
| Status changes | `mark-r` sets `prepared` or `fulfilled`; either can replace the other | A simple status field is enough for tracking, and allows corrections without extra commands. |
| Inventory effect | None; availability is read-only | A request is a tracking record, not a stock reservation. `delete-r` and `mark-r` never change inventory. |

The user guide documents the command syntax and the exact output. `help` lists the new commands under
**Student requests**.

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

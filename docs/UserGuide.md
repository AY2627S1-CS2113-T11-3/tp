# Bob User Guide

Bob is a command-line application for lab technicians to manage inventory, schedule lab sessions, prepare materials
for each session, track student requests, keep contacts, track resources loaned to students, and record broken items.
Preparation uses the
session's headcount to calculate required quantities and compare them with the inventory. Requests are sorted by
deadline and show whether the requested quantity is currently in inventory.

**Data is kept only while Bob is running.** Exiting or restarting clears all inventory, sessions, preparations,
contacts, requests, loans, and broken-item reports.

## Contents

- [Quick start](#quick-start)
- [Command format](#command-format)
- [Viewing help](#viewing-help)
- [Inventory management](#inventory-management)
- [Lab session scheduling](#lab-session-scheduling)
- [Lab session preparation](#lab-session-preparation)
- [Student request tracking](#student-request-tracking)
- [Contact management](#contact-management)
- [Loan tracking](#loan-tracking)
- [Broken-item tracking](#broken-item-tracking)
- [Exiting Bob](#exiting-bob)
- [Errors and troubleshooting](#errors-and-troubleshooting)
- [Current limitations](#current-limitations)
- [FAQ](#faq)
- [Command summary](#command-summary)

## Quick start

1. Install a Java 25 JDK and open a terminal in the project root, the folder containing `build.gradle`.
2. Run `java --version` and check that it reports Java 25. On macOS with SDKMAN and the project's JDK installed,
   select it with `sdk use java 25.0.3.fx-zulu`.
3. Build and launch Bob using the commands for your platform below. The first build needs an internet connection
   to download build dependencies.

   **macOS / Linux**

   ```bash
   ./gradlew shadowJar
   java -jar build/libs/bob.jar
   ```

   **Windows PowerShell**

   ```powershell
   .\gradlew.bat shadowJar
   java -jar build/libs/bob.jar
   ```

   The application is named Bob, and the build produces a file named `bob.jar`.

4. Bob displays a banner and asks `What is your name?`. Enter your name, for example `Alex`, and press Enter.
   Bob responds:

   ```text
   Hello Alex, welcome to Bob!
   --------------------------------------------------------------------
   ```

5. Enter one command at a time. You can try this sequence in a fresh application:

   ```text
   add-i n/Wire c/Components q/15
   add-s n/Practice Lab d/8 October 2099 l/Room-A s/0900 e/1000 p/10
   add-p s/1 i/Wire c/Components q/2
   list-p s/1
   ```

   The example date is deliberately in the future. Replace it with your session date, which must be today or later.
   Ten attendees needing two wires each require 20 wires. With only 15 in inventory, Bob reports a shortfall of 5.

6. Enter `Bye` to exit. To start again, run `java -jar build/libs/bob.jar`. Rebuild with `shadowJar` after changing
   the source code. Each launch starts with empty lists.

## Command format

- Enter each command on one line and press Enter. There are no follow-up selection prompts after a command.
- UPPER_CASE words in the formats below are placeholders. Replace them with your own values.
- Square brackets around a parameter mean it is optional. Do not type the brackets.
- Commands and prefixes are case-sensitive: use `add-i`, not `ADD-I`, and `n/`, not `N/`.
  The exit command is an exception: `Bye`, `bye`, and `BYE` all work.
- Supply prefixes in the exact order shown. Missing, repeated, reordered, or unexpected prefixes are rejected.
- Names, categories, and locations can contain spaces. For example, use `i/Red LED c/Lab Components` without quotes.
  Quotes are not treated as an escape mechanism. Avoid prefix-like tokens such as `q/` inside a value because Bob
  interprets them as arguments.
- Item names and categories are matched without regard to capitalization. Location matching for session conflicts
  also ignores capitalization. Month names in dates must use the capitalization shown below.
- Indices are positive, one-based positions in the displayed lists. Inventory item indices restart at 1 in each
  category; preparation item indices belong to the selected session. Request indices follow the deadline-sorted
  `list-r` display. Use the latest list after deleting an entry.
- Quantities, indices, and headcounts must be positive integers no greater than 2,147,483,647. Do not include commas
  or decimal points when entering numbers. Calculated preparation totals must also fit within this limit.
- `q/` has different meanings: inventory units for inventory commands, units **per attendee** for `add-p`,
  **total units** to remove for `delete-p`, requested units for `add-r`, loaned units for `add-l`,
  and broken units for `add-b`.

The examples below show command responses without the extra blank line Bob prints after each command.

## Viewing help

### List all commands: `help`

Displays every command with its format, grouped by feature. No arguments are accepted.

**Format:** `help`

```text
help
```

Expected output:

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
  list-l [overdue]
  delete-l INDEX
  return INDEX
Broken items
  add-b n/NAME c/CATEGORY q/QUANTITY f/FAULT d/DATE_REPORTED [m/CONTACT_INDEX]
  list-b
  delete-b INDEX
    Deletion restores all units: use only for full repairs or incorrect reports.
    Date reported: d MMMM yyyy, today or earlier. Optional m/ selects a contact from list-c.
Other
  help
  Bye
--------------------------------------------------------------------
```

See the sections below for what each command does and example outputs.

## Inventory management

Inventory shows usable, available quantities. Loaned units and units in broken-item reports are excluded.
Reporting broken items with `add-b` deducts stock; deleting a repaired or incorrect report with `delete-b`
restores its full quantity. Do not also remove those units with `delete-i`.

### Add an item: `add-i`

Adds a new item to a category. A category is created automatically when it does not already exist.

**Format:** `add-i n/NAME c/CATEGORY [q/QUANTITY]`

- Name and category cannot be blank.
- Quantity defaults to 1 when `q/` is omitted.
- An item with the same name cannot be added twice within the same category, even with different capitalization.
  This command does not increase the stock of an existing item.
- The same item name may exist in different categories. Such entries are separate inventory items.

Example in an empty inventory:

```text
add-i n/Wire c/Components q/15
```

Expected output:

```text
Successfully added: 15x Wire to the inventory
```

An example using the default quantity:

```text
add-i n/Oscilloscope c/Equipment
```

Expected output:

```text
Successfully added: 1x Oscilloscope to the inventory
```

### List inventory: `list-i`

Displays every category and its items, including available quantities. Categories and items are shown in insertion
order. No arguments are accepted.

**Format:** `list-i`

After the two additions above:

```text
list-i
```

Expected output:

```text
--------------------------------------------------------------------
Inventory
--------------------------------------------------------------------
Components
1: Wire (Qty: 15)
Equipment
1: Oscilloscope (Qty: 1)
--------------------------------------------------------------------
```

An empty inventory displays `No items in inventory.` between the inventory heading and the closing divider.

### Remove inventory units: `delete-i`

Removes a quantity from an item identified by its category and its position within that category.

**Format:** `delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY`

- All three arguments are required. Use `list-i` to find the category and item index.
- Removing less than the available quantity reduces the stock.
- Removing the entire quantity deletes the item. If the category becomes empty, it is deleted too.
- Removing more than the available quantity is rejected without changing inventory.
- Remaining items in the category are renumbered after an item is deleted.

With 15 Wire in Components, enter:

```text
delete-i c/Components i/1 q/5
```

Expected output:

```text
Successfully removed: 5x Wire from the inventory
```

The stock is now 10 Wire. Run `list-i` to see the updated inventory; `delete-i` prints only a confirmation.

## Lab session scheduling

Use these commands to add, view, and delete lab sessions.

After starting Bob and entering your name, type a complete command on one line and press Enter.
Bob processes the command immediately; it does not prompt separately for the session details or index.

The examples below show command responses, omitting diagnostic log messages.

### Add a session: `add-s`

Creates a lab session with its name, date, location, time range, and headcount.

**Format:** `add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT`

Include every prefix exactly once, in the order shown, with a value after each prefix.

| Argument | Accepted value |
| --- | --- |
| `n/NAME` | A nonblank session name. Spaces are allowed. |
| `d/DATE` | A valid date in `d MMMM yyyy` format, such as `8 October 2099`. Use a full English month name with an initial capital, such as `October`. The date must be today or later. |
| `l/LOCATION` | A nonblank location. Spaces are allowed. |
| `s/STARTTIME` | A four-digit, 24-hour time in `HHmm` format, such as `0900`. |
| `e/ENDTIME` | A four-digit, 24-hour time in `HHmm` format, such as `1200`. It must be later than the start time. |
| `p/HEADCOUNT` | A whole number from `1` to `2147483647`, representing the number of attendees. |

The following scheduling rules apply:

- Times must be between `0000` and `2359`. Sessions cannot span midnight.
- Sessions cannot overlap on the same date at the same location. Location comparisons ignore
  capitalization, so `Room-A` and `room-a` are treated as the same location.
- A session may start exactly when another session ends.
- Overlapping sessions at different locations are allowed.
- Session names do not have to be unique.

For a session dated today, Bob does not check whether its start time has already passed.

**Example:** Starting with an empty schedule, enter:

```text
add-s n/Practice Lab d/8 October 2099 l/Room-A s/0900 e/1000 p/10
```

Expected response:

```text
Successfully added: Practice Lab on 8 October 2099 at Room-A from 0900 to 1000 for 10 attendees
```

The new session is placed in chronological order. Adding a session can change the indices of
existing sessions. Use `list-s` to check the current indices before using a command that requires one.

**Example of a conflict:** With Practice Lab still scheduled, enter:

```text
add-s n/Circuit Lab d/8 October 2099 l/Room-A s/0930 e/1030 p/15
```

Expected response:

```text
Session conflicts with "Practice Lab" at Room-A on 8 October 2099 from 0900 to 1000.
```

Circuit Lab is not added. Change its date, location, or time range to avoid the conflict, then
submit the complete command again.

### List sessions: `list-s`

Displays all scheduled sessions with their indices, names, dates, locations, time ranges, and headcounts.
Sessions are sorted by date, then start time, then name alphabetically (ignoring case). No arguments are accepted.
Adding an earlier session can change existing indices; use `list-s` before selecting a session.

**Format:** `list-s`

This command takes no arguments.

Sessions are sorted by date, with earlier dates first. Sessions on the same date are sorted
by start time. If both the date and start time match, they are sorted alphabetically by name,
ignoring capitalization.

**Example:** With only Practice Lab scheduled, enter:

```text
list-s
```

Expected response:

```text
--------------------------------------------------------------------
Sessions
--------------------------------------------------------------------
1. Practice Lab on 8 October 2099 at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
```

If there are no scheduled sessions, the response is:

```text
--------------------------------------------------------------------
Sessions
--------------------------------------------------------------------
No sessions scheduled.
--------------------------------------------------------------------
```

Indices begin at `1` and represent positions in the current list. They are not permanent
session identifiers and may change after a session is added or deleted.

### Delete a session: `delete-s`

Deletes the selected session and its preparation list. Inventory quantities are unaffected.

**Format:** `delete-s INDEX`

Replace `INDEX` with the session's current index from `list-s`. Enter the index as a plain
positive whole number, without the `s/` prefix.

Deletion happens immediately, without a confirmation prompt. Check the current list before
submitting the command.

**Example:** With Practice Lab at index `1`, enter:

```text
delete-s 1
```

Expected response:

```text
Successfully removed session: Practice Lab
```

Remaining sessions are renumbered. Their preparation lists stay attached to the correct sessions.
Run `list-s` again before using another command that requires a session index.

**Example of an invalid index:** If only one session is scheduled, entering `delete-s 2` produces:

```text
Session index 2 is invalid. There are 1 scheduled session(s).
```

No session is deleted. Use `list-s` to find a valid index.

Entering `delete-s` without an index produces:

```text
Invalid format. Use: delete-s INDEX
```

## Lab session preparation

**Create a session with `add-s` before adding preparation.** Bob needs that session's headcount to calculate the
required quantities. Use `list-s` to find its index.

A preparation is a checklist for one session. It compares required totals against current inventory; it does not
reserve or deduct stock. Items do not have to exist in inventory before they can be included in preparation.

The examples in this section form a separate sequence. Start with a fresh application, enter your name, and run:

```text
add-i n/Wire c/Components q/15
add-s n/Practice Lab d/8 October 2099 l/Room-A s/0900 e/1000 p/10
```

### Add a requirement: `add-p`

**Format:** `add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]`

- Session index, item name, and category are required. Name and category cannot be blank.
- Quantity defaults to 1 per attendee. The quantity added to preparation is:
  **quantity per attendee × session headcount**.
- Adding the same item name/category pair again increases the existing required total. Matching ignores case and
  preserves the spelling from the first addition.
- The same item name in different categories is treated as separate requirements. Stock from another category
  does not contribute to availability.
- A missing item or category has zero available stock and is flagged as insufficient.
- An addition that would make the required total exceed 2,147,483,647 is rejected without changing preparation.

```text
add-p s/1 i/Wire c/Components q/2
```

Expected output:

```text
Successfully added: 20x Wire [Components] to preparation
--------------------------------------------------------------------
Lab preparation: Practice Lab on 8 October 2099 at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 20, Available: 15) !! Insufficient items (Shortfall: 5)
--------------------------------------------------------------------
```

Ten attendees need 20 Wire in total. Only 15 are available, so the shortfall is 5. Adding a requirement does not add
stock to inventory or consume any existing stock.

### List preparation: `list-p`

Displays a session's required items, their categories, total quantities, current availability, and any shortages.
Items are shown in insertion order with one-based indices.

**Format:** `list-p s/SESSION_INDEX`

```text
list-p s/1
```

After the addition above, the expected output is:

```text
--------------------------------------------------------------------
Lab preparation: Practice Lab on 8 October 2099 at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 20, Available: 15) !! Insufficient items (Shortfall: 5)
--------------------------------------------------------------------
```

Inventory is checked again each time preparation is displayed, including after successful `add-p` and `delete-p`
commands. An empty preparation displays `No items in preparation.` beneath the session heading.

### Remove a requirement: `delete-p`

Removes a total number of units from a preparation item.

**Format:** `delete-p s/SESSION_INDEX i/ITEM_INDEX q/TOTAL_QUANTITY`

- All arguments are required. Use the item index displayed by `list-p` for this session, not an inventory index.
- Here, `q/` is the **total units to remove**, not units per attendee.
- Partial removal reduces the requirement. Removing the entire required quantity deletes the row and renumbers
  later rows.
- Removing more than the required quantity is rejected without changing preparation.
- Inventory remains unchanged. The updated preparation is displayed after the confirmation.

Continuing the preparation example:

```text
delete-p s/1 i/1 q/5
```

Expected output:

```text
Successfully removed: 5x Wire [Components] from preparation
--------------------------------------------------------------------
Lab preparation: Practice Lab on 8 October 2099 at Room-A from 0900 to 1000 for 10 attendees
--------------------------------------------------------------------
1. Wire [Components] (Required: 15, Available: 15)
--------------------------------------------------------------------
```

## Student request tracking

Track items that students ask to borrow or collect. Requests are kept in a separate list, do not reserve stock, and
are displayed in deadline order.

### Add a request: `add-r`

Creates a request with the student's name, item, quantity, and deadline.

**Format:** `add-r n/STUDENT_NAME i/ITEM q/QUANTITY d/DEADLINE`

- All four arguments are required, in the order shown.
- `STUDENT_NAME` and `ITEM` must not be blank. Names can contain spaces.
- `QUANTITY` is a positive whole number.
- `DEADLINE` uses the same `d MMMM yyyy` format as sessions, for example `8 October 2099`. Use a full English month
  name with an initial capital. The deadline must be today or later.
- A new request starts with status `Pending`.
- Duplicate requests are allowed.
- The request does not name an inventory category. Availability combines stock with the same item name from all
  categories.

Example in an empty request list, with 15 Wire in inventory:

```text
add-r n/Alice i/Wire q/20 d/8 October 2099
```

Expected output:

```text
Successfully added request: Alice - 20x Wire by 8 October 2099
Request list updated. Run list-r to see the latest order.
```

The request is stored in deadline order, so the list may show it before or after requests added earlier.

### List requests: `list-r`

Displays all requests sorted by earliest deadline, with their status and current inventory availability. No arguments
are accepted.

**Format:** `list-r`

Rows show `Available: N`, where `N` is the total stock for the item name across all categories. If `N` is less than
the requested quantity, Bob also shows the shortfall.

```text
list-r
```

Expected output after the request above:

```text
--------------------------------------------------------------------
Student requests
--------------------------------------------------------------------
1. [Pending] Alice - 20x Wire by 8 October 2099 (Available: 15) !! Insufficient stock (Shortfall: 5)
--------------------------------------------------------------------
```

An empty request list displays `No requests recorded.` between the heading and the closing divider.

Availability is checked again each time `list-r` runs. Removing inventory after adding a request can increase the
shortfall; adding inventory can remove it. Requests never reserve or consume stock.

### Delete a request: `delete-r`

Deletes a request from the list.

**Format:** `delete-r INDEX`

Use the index from `list-r`. Like `delete-s` and `delete-c`, this command takes a plain number, without a prefix.

```text
delete-r 1
```

Expected output:

```text
Successfully removed request: Alice - 20x Wire by 8 October 2099
Request list updated. Run list-r to see the latest order.
```

Remaining requests are renumbered in deadline order. Run `list-r` to check the updated indices before the next
command.

### Mark a request as prepared or fulfilled: `mark-r`

Changes the status shown for a request.

**Format:** `mark-r INDEX STATUS`

- Use the index from `list-r`.
- `STATUS` must be `prepared` or `fulfilled`. The status is not case-sensitive, so `Prepared` and `FULFILLED` also
  work.
- Marking replaces the current status. A fulfilled request can be marked as prepared again if it was marked by
  mistake.
- Marking a request does not change inventory stock or create a loan. If the handover is a loan,
  record it separately with `add-l`, which deducts stock. Use `delete-i` only for an issue or consumption
  that has not already reduced inventory. Never deduct the same units twice.

Continuing the example above:

```text
mark-r 1 prepared
```

Expected output:

```text
Successfully marked request 1 as prepared: Alice - 20x Wire by 8 October 2099
```

The next `list-r` shows `[Prepared]` in place of `[Pending]`:

```text
--------------------------------------------------------------------
Student requests
--------------------------------------------------------------------
1. [Prepared] Alice - 20x Wire by 8 October 2099 (Available: 15) !! Insufficient stock (Shortfall: 5)
--------------------------------------------------------------------
```

To complete the request, run:

```text
mark-r 1 fulfilled
```

Expected output:

```text
Successfully marked request 1 as fulfilled: Alice - 20x Wire by 8 October 2099
```

## Contact management

Keep a list of people to call for repairs and procurement, such as facilities staff or component suppliers.

### Add a contact: `add-c`

Adds a contact with a name and phone number, and optionally an email and a role describing what they handle.

**Format:** `add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]`

- `n/` and `t/` are required. `e/` and `r/` are optional, but must appear in this order if used.
- Use `r/` to note why you would contact this person, for example what they repair or supply.
- `PHONE` must contain digits only, with no length limit. Spaces, `+`, and `-` are not accepted, so write
  `6561234567` rather than `+65 6123 4567`.
- `EMAIL` is only checked loosely: it must contain an `@` and no spaces. It is not checked against real addresses.
- A value you include must not be blank. Leave the whole prefix out instead.
- Duplicates are allowed. Adding the same name or number twice creates two separate contacts.
- A word in a value that is a single letter followed by `/` (for example `a/b`) is read as a new prefix, so the
  command is rejected as invalid format. Words such as `and/or` are fine.

```text
add-c n/NUS Infrastructure t/65161234 r/Aircon and lighting repairs
```

Expected output:

```text
Successfully added: NUS Infrastructure (65161234) to contacts
```

```text
add-c n/Component Supplier t/91234567 e/supplier@example.com r/Resistors, LEDs, wires
```

Expected output:

```text
Successfully added: Component Supplier (91234567) to contacts
```

### List contacts: `list-c`

Displays all contacts with their indices. No arguments are accepted.

**Format:** `list-c`

With the two contacts above:

```text
list-c
```

Expected output:

```text
--------------------------------------------------------------------
Contacts
--------------------------------------------------------------------
1. NUS Infrastructure (65161234) - Aircon and lighting repairs
2. Component Supplier (91234567, supplier@example.com) - Resistors, LEDs, wires
--------------------------------------------------------------------
```

An empty contact list displays `No contacts saved.` between the contacts heading and the closing divider.

### Delete a contact: `delete-c`

Deletes a contact from the list.

**Format:** `delete-c INDEX`

Use the index from `list-c`. Like `delete-s`, this command takes a plain number, without a prefix.

```text
delete-c 1
```

Expected output:

```text
Successfully removed contact: NUS Infrastructure
```

Remaining contacts are renumbered. Run `list-c` to check the updated indices before the next command.

If a broken-item report uses the deleted contact, its contact becomes unavailable. The report and inventory
remain unchanged. Other reports keep their selected contacts even when contact indices change.

## Loan tracking

Record student loans separately from supplier and technician contacts. Each loan contains one inventory
item, its quantity, borrower name and phone number, and explicit due date. Borrowers are entered directly;
adding or deleting a supplier contact does not change a loan's borrower details.

### Add a loan: `add-l`

**Format:** `add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE`

- All six arguments are required, in the displayed order. Quantity is the total number of units lent.
- Item name and category must already exist in inventory. Matching ignores capitalization, while the loan
  retains the spelling entered in the command.
- Quantity must be a positive integer and cannot exceed available inventory. Successful additions deduct
  stock immediately; preparation availability and shortage checks reflect that deduction.
- Lending all available units removes the depleted inventory entry and its category if empty. The loan
  retains its item details and quantity independently.
- Borrower name cannot be blank. Phone numbers use the existing contact rule: digits only, with leading
  zeros preserved. No entry is added to `list-c`.
- Due dates use a full, capitalized English month in `d MMMM yyyy` format, for example `30 October 2099`.
  Today and past dates are accepted so existing loans can be entered as a backlog. A past due date
  produces an immediate warning after the success confirmation; it does not reject the loan.
- Every successful command creates a separate record, including repeated loans to the same borrower.
- Rejected commands leave inventory, loans, and supplier contacts unchanged.

In a fresh application, enter:

```text
add-i n/Multimeter c/Equipment q/5
add-l n/Multimeter c/Equipment q/2 b/Alex Tan t/91234567 d/30 October 2099
```

The loan command prints:

```text
Successfully added loan: 2x Multimeter [Equipment] to Alex Tan (91234567), due 30 October 2099
```

There are now 3 Multimeters available in inventory.

### List active loans: `list-l`

**Format:** `list-l`

Displays loans in insertion order with one-based indices. Overdue loans have an `[OVERDUE]` marker.
Listing loans does not change inventory. A loan due today is not overdue.
After the example above:

```text
--------------------------------------------------------------------
Loans
--------------------------------------------------------------------
1. 2x Multimeter [Equipment] to Alex Tan (91234567), due 30 October 2099
--------------------------------------------------------------------
```

An empty list displays `No active loans.` between the heading and closing divider.

### Delete an incorrect loan: `delete-l`

**Format:** `delete-l INDEX`

Removes an incorrect loan record and restores its entire quantity to available inventory. Use the index
from the latest `list-l`. Remaining loans are renumbered, so list again before selecting another record.
This command corrects an entry. Use `return` when the borrower has actually returned the items.
Partial deletion is not supported.

Continuing the Multimeter example, enter:

```text
delete-l 1
```

Expected output:

```text
Successfully deleted incorrect loan: 2x Multimeter [Equipment] to Alex Tan (91234567), due 30 October 2099. Inventory restored.
```

Inventory is back to 5 Multimeters. Restoration matches item/category without regard to capitalization,
increases existing stock, or recreates a missing item/category. Existing spelling is preserved; a recreated
entry uses the loan's spelling and is appended to its category or the inventory category list.
Preparation availability reflects the restored stock when next displayed. Supplier contacts and other
loans are unaffected. An invalid index leaves everything unchanged.

If re-added stock makes restoration exceed 2,147,483,647 units, Bob rejects the deletion and retains both
the loan and stock unchanged. Check the inventory quantities before retrying the correction.

### Record a full return: `return`

**Format:** `return INDEX`

Records the return of every item in one loan, restores its full quantity to inventory, and removes it
from the active loan list. Use the index from the latest `list-l`. Remaining loans are renumbered, so
list again before choosing another loan. The confirmation identifies a return rather than a correction.

With the Multimeter loan from the add example still active, enter:

```text
return 1
```

Expected output:

```text
Successfully returned loan: 2x Multimeter [Equipment] to Alex Tan (91234567), due 30 October 2099. Inventory restored.
```

Inventory is back to 5 Multimeters, and `list-l` shows no active loans if this was the only loan.
Preparation availability reflects the restored stock when next displayed. Returning the loan does not
change supplier contacts, sessions, or other loans.

- Returns must cover the whole loan. Do not supply a quantity or additional loan index.
- Restoration merges matching stock without regard to capitalization, or recreates missing items/categories,
  following the same inventory rules as correcting a loan.
- Invalid indices or restoration exceeding 2,147,483,647 leave the loan and stock unchanged.
- A removed loan cannot be returned or deleted again. Positions can refer to different loans after renumbering;
  always check `list-l` before the next operation.
- Returned records are removed immediately; return history is not retained in this version.

### Track overdue loans: `list-l overdue`

**Format:** `list-l overdue`

Shows only active loans whose due date is before today's date on the computer running Bob. A loan becomes
overdue on the day after its due date. Status is checked again each time you list loans, so a running
application reflects the new date after midnight without restarting. `list-l` also marks overdue rows.

The overdue-only list keeps each loan's index from the full active list. For example, if only loan 2 is
overdue, it is still displayed as `2.`. Use that index for `return` or `delete-l`. After removal, indices
shift in both lists; list again before the next operation.

With an overdue Multimeter loan at position 2 and other current loans, the output looks like:

```text
--------------------------------------------------------------------
Overdue loans
--------------------------------------------------------------------
2. 2x Multimeter [Equipment] to Alex Tan (91234567), due 1 January 2000 [OVERDUE]
--------------------------------------------------------------------
```

When no loans are overdue, Bob displays `No overdue loans.`. The filter is case-sensitive: enter
`overdue` in lowercase. Extra or unsupported arguments are rejected. Filtering never changes stock,
loan order, borrower details, or preparation requirements.

Adding an existing loan with a past due date succeeds and prints this additional line:

```text
Warning: this loan is already overdue.
```

Loans due today or in the future do not produce this warning. Invalid or insufficient-stock additions
are rejected without adding a loan or printing an overdue warning. Returned and corrected records
are removed from both active and overdue lists.

All loans disappear when Bob exits; persistence is planned for V2.0b. Return history and partial returns
remain unsupported.

## Broken-item tracking

Record damaged inventory units, their faults, the date reported, and an optional maintenance contact.
Inventory represents usable, available stock: adding a broken-item report reduces that stock immediately.
Requests and session preparation reflect the reduced availability the next time their lists are displayed.

**Do not remove the same units with `delete-i` before reporting them.** `add-b` already deducts them.
Units on an active loan are also excluded from available stock. If a loan is returned with damaged units,
record its full return with `return INDEX`, then report the damaged quantity with `add-b`.
Partial loan returns are not supported.

The examples below form a separate sequence. In a fresh application, enter your name, then run:

```text
add-i n/Meter c/Equipment q/10
add-c n/Lab Maintenance t/61234567
```

### Report broken items: `add-b`

**Format:** `add-b n/NAME c/CATEGORY q/QUANTITY f/FAULT d/DATE_REPORTED [m/CONTACT_INDEX]`

Supply the prefixes in the order shown. Only `m/` is optional.

| Argument | Accepted value |
| --- | --- |
| `n/NAME` | A nonblank name matching an existing inventory item. Matching ignores capitalization. |
| `c/CATEGORY` | The item's inventory category. Matching ignores capitalization. Stock in other categories is not used. |
| `q/QUANTITY` | A whole number from `1` to `2147483647`, no greater than the currently available quantity. There is no default quantity. |
| `f/FAULT` | A nonblank fault description. Spaces are allowed. Avoid prefix-like tokens such as `q/` inside the description. |
| `d/DATE_REPORTED` | A real date in `d MMMM yyyy` format, with a capitalized full English month name. The date must be today or earlier, using the computer's local date. |
| `m/CONTACT_INDEX` | Optional positive index from the latest `list-c`. Omit the entire prefix if no contact is assigned. |

Contacts must already exist before they can be selected. Bob does not choose a contact automatically from an
item's name, category, or the contact's role. Entering an invalid contact index rejects the whole command.

Example:

```text
add-b n/Meter c/Equipment q/2 f/Cracked screen d/1 January 2020 m/1
```

The historical date demonstrates recording an earlier fault. Replace it with the actual reported date.

Expected output:

```text
Successfully recorded broken item: 2x Meter [Equipment], fault: Cracked screen, reported 1 January 2020. Available inventory reduced.
```

There are now 8 usable Meters in Equipment. Each successful addition creates a separate report, even if its
details match another report. Avoid submitting the same report twice: each addition deducts its quantity again.
If all available units are reported, the inventory item disappears from `list-i`; the broken report remains.
An invalid report or insufficient stock leaves both inventory and the report list unchanged.

### List broken items: `list-b`

**Format:** `list-b`

Displays reports in the order they were added, with one-based indices. No arguments are accepted.
Listing does not change stock, report order, or contacts.

Continuing the example:

```text
list-b
```

Expected output:

```text
--------------------------------------------------------------------
Broken items
--------------------------------------------------------------------
1. 2x Meter [Equipment], fault: Cracked screen, reported 1 January 2020
   Contact: Lab Maintenance (61234567)
--------------------------------------------------------------------
```

- Without an assigned contact, the contact line reads `Contact: Not assigned`.
- If the selected contact is later deleted, it reads `Contact: Unavailable (contact deleted)`.
  The report remains, and deleting or renumbering other contacts cannot redirect it to another person.
  Adding a new contact with identical details does not reconnect the report automatically.
- An empty list displays `No broken items recorded.` between the heading and closing divider.

### Remove a report and restore stock: `delete-b`

**Format:** `delete-b INDEX`

**This command restores every unit in the selected report to usable inventory.** Use it only when all those
units have been repaired, or when the report was incorrect. Do not use it to record disposal or a partial repair.
It takes effect immediately, without a confirmation prompt.

Use the current report index from `list-b`, not an inventory or contact index. Continuing the example, after
both Meters have been repaired:

```text
delete-b 1
```

Expected output:

```text
Successfully deleted broken-item report: 2x Meter [Equipment], fault: Cracked screen, reported 1 January 2020. All units restored to inventory.
```

Available stock returns to 10 Meters. The report is removed, and no repair history is retained.
Remaining reports are renumbered; run `list-b` again before selecting another report.

Restoration adds to matching inventory without regard to capitalization, or recreates a missing item/category.
A deleted maintenance contact does not prevent restoration. If the index is invalid or restoration would make
the available quantity exceed `2147483647`, Bob rejects the command and leaves the report and stock unchanged.

## Exiting Bob

Enter `Bye` on its own line. The spelling is case-insensitive, so `bye` and `BYE` also work.
Bob exits without a farewell message or a save prompt. All data entered during the run is discarded.

## Errors and troubleshooting

Bob displays an error and continues accepting commands when input is invalid. A rejected command does not change
inventory, sessions, preparation, contacts, requests, loans, or broken-item reports.
Format errors show the required command syntax.

These are examples of the current messages:

| Situation | Example message | What to do |
| --- | --- | --- |
| Unknown command, such as `todo` | `Invalid command` | Use `help` or the summary below to find the right command. |
| `help` with extra text | `Invalid format. Use: help` | Enter `help` on its own. |
| Missing required `add-p` arguments | `Invalid format. Use: add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]` | Supply the session, item, and category in that order. |
| Blank item name or category | `Item name cannot be blank.` or `Category cannot be blank.` | Enter a nonblank value after the prefix. |
| Zero or negative quantity | `Quantity must be greater than zero.` | Enter a whole number from 1 to 2147483647. |
| Non-integer or out-of-range quantity | `Quantity must be a positive integer.` | Enter a whole number from 1 to 2147483647. |
| Invalid headcount | `Headcount must be a positive integer.` | Enter a positive whole number after `p/`. |
| Invalid session date or lowercase month | `Date must be in the format 'd MMMM yyyy' (e.g., 22 November 2027). Ensure the month is capitalized.` | Use a real date such as `8 October 2099`, with a capitalized full month name. |
| Past session date | `Session date cannot be before today.` | Use today's date or a future date. |
| Invalid start or end time | `Start time must be in 'HHmm' format (e.g., 0900 for 9:00 AM).` or `End time must be in 'HHmm' format (e.g., 1200 for 12:00 PM).` | Use four digits, such as `0900`, with no colon. |
| End time at or before start time | `Start time must be earlier than end time.` | Choose a later end time on the same day. |
| Overlapping session at the same location | `Session conflicts with "Practice Lab" at Room-A on 8 October 2099 from 0900 to 1000.` | Change the date, location, or time range. |
| Missing contact name or phone | `Invalid format. Use: add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]` | Supply at least the name and phone number, in that order. |
| Invalid phone or email | `Phone must contain digits only.` or `Email must be a valid address, e.g. name@example.com.` | Correct the value after the prefix. |
| Unknown loan index | `Loan index 3 is invalid. There are 2 active loan(s).` | Use the latest `list-l` to check the index. |
| Stock restoration exceeds the limit | `Cannot restore 2 Wire. Inventory quantity must not exceed 2147483647.` | Check the available stock and correct any inaccurate quantities before retrying; the loan or broken-item report remains unchanged. |
| Unknown contact index | `Contact index 3 is invalid. There are 2 contact(s).` | Use `list-c` to check the index. |
| Duplicate inventory item | `Item "Wire" already exists in category "Components".` | Do not use `add-i` to top up an existing item. |
| Unknown inventory category | `Category "Unknown" does not exist.` | Check the category using `list-i`. |
| Removal exceeds stock | `Cannot remove 20 Wire. Only 15 are available.` | Remove no more than the available quantity. |
| No session exists for preparation | `Session index 1 is invalid. There are 0 scheduled session(s).` | Create a session with `add-s` first. |
| Unknown preparation item index | `Preparation item index 1 is invalid. There are 0 item(s) in this preparation.` | Use `list-p` to check the selected session's preparation. |
| Removal exceeds a preparation requirement | `Cannot remove 21x Wire [Components] from preparation; only 20 required.` | Remove no more than the required total. |
| Calculated preparation total is too large | `Required quantity must not exceed 2147483647.` | Reduce the per-person quantity or existing requirement. |
| Missing `add-r` arguments | `Invalid format. Use: add-r n/STUDENT_NAME i/ITEM q/QUANTITY d/DEADLINE` | Supply the student, item, quantity, and deadline in that order. |
| Blank student name or request item | `Student name cannot be blank.` or `Item name cannot be blank.` | Enter a nonblank value after the prefix. |
| Invalid or past request deadline | `Deadline must be a valid date in d MMMM yyyy format.` or `Request deadline cannot be before today.` | Use a real date such as `8 October 2099`, with a capitalized full month name, and do not use a date before today. |
| Unknown request index | `Request index 3 is invalid. There are 2 recorded request(s).` | Use `list-r` to check the index. |
| Invalid request status | `Status must be "prepared" or "fulfilled".` | Use `prepared` or `fulfilled` after the index. |
| Missing or reordered broken-item arguments | `Invalid format. Use: add-b n/NAME c/CATEGORY q/QUANTITY f/FAULT d/DATE_REPORTED [m/CONTACT_INDEX]` | Supply all required prefixes in that order; omit `m/` entirely if no contact is assigned. |
| Blank fault description | `Fault description cannot be blank.` | Enter the fault after `f/`. |
| Invalid reported date | `Date reported must be a valid date in d MMMM yyyy format (e.g., 23 October 2026).` | Use a real date and a capitalized full English month name. |
| Future reported date | `Date reported cannot be in the future.` | Enter the actual reported date, today or earlier. |
| Broken quantity exceeds available stock | `Cannot report 11 Meter as broken. Only 10 are available.` | Check `list-i`; do not include units still on loan or already reported as broken. |
| Unknown broken-item index | `Invalid broken-item index: 2. There are 1 broken-item reports. Use list-b to check positions.` | Run `list-b` and use its current index. |

If the JAR cannot be found, run `shadowJar` from the project root and use the exact filename
`build/libs/bob.jar`. If you changed the code but still see old behavior, rebuild and restart Bob.

## Current limitations

- There is no saving, loading, export, or recovery after exit. All data is held in memory.
- `todo` and `done` are not supported commands. Use `help` for a list of commands and `Bye` to exit.
- Inventory items and sessions cannot be edited in place. There is no command to top up an existing inventory item.
- Preparation does not reserve inventory or combine requirements across sessions. Two sessions can each appear to
  have sufficient stock even if there is not enough to supply both at once.
- Requests do not reserve inventory either. Two requests can each show enough stock for the same units, and marking a
  request as fulfilled does not deduct anything from inventory. Record a loan separately with `add-l`;
  use `delete-i` for other issues or consumption only if those units have not already been deducted.
- Requests are not removed automatically when their deadline passes. Delete them with `delete-r` when they are no
  longer needed.
- Requests cannot be edited in place. Delete the old request and add the corrected one.
- Session dates are checked against today's date on the computer running Bob. For a session dated today, its start
  time is not checked against the current clock time. Sessions do not disappear automatically after their dates pass.
- Broken-item reports cannot be edited in place or partially repaired. There is no disposal command, repair history,
  or command to reassign a report's contact. `delete-b` always restores the entire reported quantity.
- Commands cannot be chained on one line. Each add or delete command affects one entry.

## FAQ

**Do I need a session before preparing materials?**

Yes. Run `add-s`, then `list-s`, and use that session's index in `add-p`. The session provides the headcount.

**Does an item need to exist in inventory first?**

For session preparation or student requests, no: missing stock is shown as unavailable. For loans and
broken-item reports, yes: the item must exist in the selected category with enough available units.

**Why can I repeat `add-p` but not `add-i` for the same item?**

`add-p` accumulates required quantities for a session. `add-i` creates a new inventory entry and rejects duplicates
within the same category.

**Why does `october` fail while `October` works?**

Dates require full English month names with the usual capitalization, for example `8 October 2099`.

**Will deleting preparation return items to inventory?**

No inventory adjustment is needed: preparation never removes stock. `delete-p` changes only the checklist.

**Does marking a request as fulfilled remove the item from inventory?**

No. Request tracking only records that the item was prepared or handed over. If you lend the units,
record the loan with `add-l`; this already deducts stock. Use `delete-i` for other issues or consumption
only when those units have not already been deducted. Do not also use `delete-i` for units recorded
with `add-l` or `add-b`. `list-r` rechecks available stock each time it is displayed.

**Can I change a request's student, item, quantity, or deadline?**

Not in place. Delete the request with `delete-r` and add a corrected one with `add-r`.

**Why did deleting a broken-item report increase my inventory?**

`delete-b` means that all reported units are usable again, either after a full repair or correction of an
incorrect report. It restores the entire quantity. Leave genuinely broken units recorded until repaired;
disposal and partial repairs are not supported.

**Can I keep my data for the next launch or transfer it to another computer?**

Not in the current version. Bob has no save or import function; a new launch starts with empty lists.

## Command summary

| Action | Command format |
| --- | --- |
| View all commands | `help` |
| Add an inventory item | `add-i n/NAME c/CATEGORY [q/QUANTITY]` |
| Remove inventory units | `delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY` |
| List inventory | `list-i` |
| Add a session | `add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT` |
| Delete a session and its preparation | `delete-s INDEX` |
| List sessions | `list-s` |
| Add a preparation requirement | `add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]` |
| Remove preparation units | `delete-p s/SESSION_INDEX i/ITEM_INDEX q/TOTAL_QUANTITY` |
| List a session's preparation | `list-p s/SESSION_INDEX` |
| Add a student request | `add-r n/STUDENT_NAME i/ITEM q/QUANTITY d/DEADLINE` |
| Delete a student request | `delete-r INDEX` |
| List student requests by deadline | `list-r` |
| Mark a request as prepared or fulfilled | `mark-r INDEX STATUS` |
| Add a contact | `add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]` |
| Delete a contact | `delete-c INDEX` |
| List contacts | `list-c` |
| Add a student loan | `add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE` |
| List active loans with overdue markers | `list-l` |
| List overdue loans only | `list-l overdue` |
| Delete an incorrect loan and restore stock | `delete-l INDEX` |
| Record a full return and restore stock | `return INDEX` |
| Report broken inventory units | `add-b n/NAME c/CATEGORY q/QUANTITY f/FAULT d/DATE_REPORTED [m/CONTACT_INDEX]` |
| List broken-item reports and contacts | `list-b` |
| Remove a repaired or incorrect report and restore stock | `delete-b INDEX` |
| Exit and discard data | `Bye` |

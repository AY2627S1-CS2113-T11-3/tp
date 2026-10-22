# Bob User Guide

Bob is a command-line application for lab technicians to manage inventory, schedule lab sessions, and prepare
materials for each session. Preparation uses the session's headcount to calculate required quantities and compare
them with the inventory.

**Data is kept only while Bob is running.** Exiting or restarting clears all inventory, sessions, preparations, and contacts.

## Contents

- [Quick start](#quick-start)
- [Command format](#command-format)
- [Viewing help](#viewing-help)
- [Inventory management](#inventory-management)
- [Lab session scheduling](#lab-session-scheduling)
- [Lab session preparation](#lab-session-preparation)
- [Contact management](#contact-management)
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
   java -jar build/libs/duke.jar
   ```

   **Windows PowerShell**

   ```powershell
   .\gradlew.bat shadowJar
   java -jar build/libs/duke.jar
   ```

   The application is named Bob, but the current build produces a file named `duke.jar`.

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

6. Enter `Bye` to exit. To start again, run `java -jar build/libs/duke.jar`. Rebuild with `shadowJar` after changing
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
  category; preparation item indices belong to the selected session. Use the latest list after deleting an entry.
- Quantities, indices, and headcounts must be positive integers no greater than 2,147,483,647. Do not include commas
  or decimal points when entering numbers. Calculated preparation totals must also fit within this limit.
- `q/` has different meanings: inventory units for inventory commands, units **per attendee** for `add-p`, and
  **total units** to remove for `delete-p`.

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
Contacts
  add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]
  delete-c INDEX
  list-c
Other
  help
  Bye
--------------------------------------------------------------------
```

See the sections below for what each command does and example outputs.

## Inventory management

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

Displays all scheduled sessions with their indices, names, dates, locations, time ranges,
and headcounts.

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

## Exiting Bob

Enter `Bye` on its own line. The spelling is case-insensitive, so `bye` and `BYE` also work.
Bob exits without a farewell message or a save prompt. All data entered during the run is discarded.

## Errors and troubleshooting

Bob displays an error and continues accepting commands when input is invalid. A rejected command does not change
inventory, sessions, or preparation. Format errors show the required command syntax.

These are examples of the current messages:

| Situation | Example message | What to do |
| --- | --- | --- |
| Unknown command, such as `todo` | `Invalid command` | Use `help` or the summary below to find the right command. |
| `help` with extra text | `Invalid format. Use: help` | Enter `help` on its own. |
| Missing required `add-p` arguments | `Invalid format. Use: add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]` | Supply the session, item, and category in that order. |
| Blank item name or category | `Item name cannot be blank.` or `Category cannot be blank.` | Enter a nonblank value after the prefix. |
| Zero, negative, non-integer, or out-of-range quantity | `Quantity must be a positive integer.` | Enter a whole number from 1 to 2147483647. |
| Invalid headcount | `Headcount must be a positive integer.` | Enter a positive whole number after `p/`. |
| Invalid date or lowercase month | `Date must be in the format 'd MMMM yyyy' (e.g., 22 November 2027). Ensure the month is capitalized.` | Use a real date such as `8 October 2099`, with a capitalized full English month name. |
| Past session date | `Session date cannot be before today.` | Use today's date or a future date. |
| Invalid start or end time | `Start time must be in 'HHmm' format (e.g., 0900 for 9:00 AM).` or `End time must be in 'HHmm' format (e.g., 1200 for 12:00 PM).` | Enter four digits between `0000` and `2359`, such as `0900`, with no colon. |
| End time at or before start time | `Start time must be earlier than end time.` | Choose a later end time on the same day. |
| Overlapping session at the same location | `Session conflicts with "Practice Lab" at Room-A on 8 October 2099 from 0900 to 1000.` | Change the date, location, or time range. |
| Missing contact name or phone | `Invalid format. Use: add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]` | Supply at least the name and phone number, in that order. |
| Invalid phone or email | `Phone must contain digits only.` or `Email must be a valid address, e.g. name@example.com.` | Correct the value after the prefix. |
| Unknown contact index | `Contact index 3 is invalid. There are 2 contact(s).` | Use `list-c` to check the index. |
| Duplicate inventory item | `Item "Wire" already exists in category "Components".` | Do not use `add-i` to top up an existing item. |
| Unknown inventory category | `Category "Unknown" does not exist.` | Check the category using `list-i`. |
| Removal exceeds stock | `Cannot remove 20 Wire. Only 15 are available.` | Remove no more than the available quantity. |
| No session exists for preparation | `Session index 1 is invalid. There are 0 scheduled session(s).` | Create a session with `add-s` first. |
| Unknown preparation item index | `Preparation item index 1 is invalid. There are 0 item(s) in this preparation.` | Use `list-p` to check the selected session's preparation. |
| Removal exceeds a preparation requirement | `Cannot remove 21x Wire [Components] from preparation; only 20 required.` | Remove no more than the required total. |
| Calculated preparation total is too large | `Required quantity must not exceed 2147483647.` | Reduce the per-person quantity or existing requirement. |

If the JAR cannot be found, run `shadowJar` from the project root and use the exact filename
`build/libs/duke.jar`. If you changed the code but still see old behavior, rebuild and restart Bob.

## Current limitations

- There is no saving, loading, export, or recovery after exit. All data is held in memory.
- `todo` and `done` are not supported commands. Use `help` for a list of commands and `Bye` to exit.
- Inventory items and sessions cannot be edited in place. There is no command to top up an existing inventory item.
- Preparation does not reserve inventory or combine requirements across sessions. Two sessions can each appear to
  have sufficient stock even if there is not enough to supply both at once.
- Session dates are checked against today's date on the computer running Bob. For a session dated today, its start
  time is not checked against the current clock time. Sessions do not disappear automatically after their dates pass.
- Commands cannot be chained on one line. Each add or delete command affects one item or session.

## FAQ

**Do I need a session before preparing materials?**

Yes. Run `add-s`, then `list-s`, and use that session's index in `add-p`. The session provides the headcount.

**Does an item need to exist in inventory first?**

No. A requirement for a missing name/category pair is accepted with availability zero and a shortage warning.

**Why can I repeat `add-p` but not `add-i` for the same item?**

`add-p` accumulates required quantities for a session. `add-i` creates a new inventory entry and rejects duplicates
within the same category.

**Why does `october` fail while `October` works?**

Dates require full English month names with the usual capitalization, for example `8 October 2099`.

**Will deleting preparation return items to inventory?**

No inventory adjustment is needed: preparation never removes stock. `delete-p` changes only the checklist.

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
| Add a contact | `add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]` |
| Delete a contact | `delete-c INDEX` |
| List contacts | `list-c` |
| Exit and discard data | `Bye` |

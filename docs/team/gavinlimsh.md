# Gavin Lim — Project Portfolio Page

## Overview

Bob is a Java command-line application that helps university lab technicians manage inventory,
schedule sessions, prepare materials, maintain contacts, and track student requests, equipment
loans, and broken items.

My main contributions were lab session scheduling and broken-item tracking. I also improved
validation, regression testing, documentation, and integration between features.

**GitHub:** [gavinlimsh](https://github.com/gavinlimsh)  
**Code contributions:** [RepoSense](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=gavinlimsh)

## Summary of Contributions

### Lab session scheduling

Implemented the initial `add-s`, `list-s`, and `delete-s` functionality, then improved scheduling
behavior and tests collaboratively with the team.

- **Purpose:** Help technicians maintain a chronological schedule and avoid overlapping sessions
  at the same location.
- **Implementation:** Contributed session creation, listing, retrieval, and deletion; improved
  ordering and validation messages, and added assertions and logging.
- **Technical challenge:** Sorting and deletion change displayed indices. Tests verify that
  preparation remains attached to the correct session after renumbering, while conflict checks
  distinguish overlaps from adjacent sessions and sessions at different locations.
- **Testing:** Expanded coverage for invalid names, locations, dates, times, headcounts, and indices.
  Later improvements included null-input validation and deterministic date-boundary tests using
  an injected clock.

Evidence: [initial implementation](https://github.com/AY2627S1-CS2113-T11-3/tp/commit/c635ca5e2de05a4be90a63d715c71e54f0e5befa),
[PR #46](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/46),
[PR #66](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/66).

### Broken-item tracking

Implemented `add-b`, `list-b`, and `delete-b` with item name, category, quantity, fault description,
reported date, and an optional maintenance contact.

- **Purpose:** Separate damaged units from usable stock and retain the details needed to arrange repairs.
- **Stock consistency:** Reporting damage deducts available stock; removing a fully repaired or
  incorrect report restores its entire quantity. Validation failures and restoration overflow
  preserve the affected stock and reports.
- **Technical challenge:** Reports must remain meaningful after stock entries disappear or contacts
  are renumbered. Reports retain their own item details and selected contact identity. Deleting a
  contact makes the link unavailable; an identical replacement does not silently reconnect it.
- **Integration and testing:** Connected parsing, help, console output, inventory, and contacts.
  Added model, manager, and parser tests for date boundaries, malformed commands, insufficient
  stock, overflow, deleted contacts, and interactions with loans.

Evidence: [PR #52](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/52).

### Integration and quality assurance

- Fixed parser compilation and help-output inconsistencies, including lookup-related merge
  errors in [PR #58](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/58).
- Aligned lookup with inventory's case-insensitive matching, including Unicode variants, and added
  regression tests in [PR #66](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/66).
- Strengthened fulfilled-request protection against resets through retained references and
  repeated stock deduction. Added regression tests for insufficient stock and completed-request
  displays in [PR #73](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/73).
- Added the original command-by-command UI-plan runner and expanded console fixtures. Recorded
  validation of 60 UI cases and 504 steps at that revision. Teammates subsequently extended the
  runner for persistence and loan history.

### Documentation

- Expanded the User Guide's [scheduling](../UserGuide.md#lab-session-scheduling) and
  [broken-item](../UserGuide.md#broken-item-tracking) sections with syntax, examples, validation,
  changing-index guidance, stock effects, and recovery instructions.
- Contributed Developer Guide explanations for [scheduling](../DeveloperGuide.md#42-session-scheduling)
  and [broken-item tracking](../DeveloperGuide.md#47-broken-item-tracking), including walkthroughs,
  design alternatives, and class and sequence diagrams.
- Added editable PlantUML sources for the broken-item diagrams in
  [PR #60](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/60).
  The team subsequently refined the documentation and UML notation in
  [PR #78](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/78), addressing
  [DG feedback #75](https://github.com/AY2627S1-CS2113-T11-3/tp/issues/75).
  The extracts below use those updated diagrams.

### Team support and reviews

Reviewed teammates' work and supported integration across features. In the request-tracking review,
I highlighted the risk of users acting on outdated list positions and identified incorrect
RepoSense annotation placement.

Evidence: [index-change feedback](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/45#discussion_r4347338625),
[annotation feedback](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/45#discussion_r4347284658).

<div style="break-before: page; page-break-before: always;"></div>

## Contributions to the User Guide - Selected Extracts

The following extracts demonstrate my documentation of broken-item workflows and stock effects.
The selection is shortened for this portfolio; the complete instructions are in the
[User Guide](../UserGuide.md#broken-item-tracking).

### Reporting broken items

Record damaged inventory units, their faults, the date reported, and an optional maintenance contact.
Inventory represents usable, available stock: adding a broken-item report reduces that stock immediately.

**Do not remove the same units with `delete-i` before reporting them.** `add-b` already deducts them.
Units on an active loan are also excluded from available stock. If a loan is returned with damaged
units, record its full return with `return INDEX`, then report the damaged quantity with `add-b`.
Partial loan returns are not supported.

**Format:**

```text
add-b n/NAME c/CATEGORY q/QUANTITY f/FAULT d/DATE_REPORTED [m/CONTACT_INDEX]
```

Supply the prefixes in the order shown. Only `m/` is optional. The quantity must be positive and
must not exceed available stock in the selected category. The reported date must be today or earlier.

Start this example from a new, empty working directory, enter your name, then run:

```text
add-i n/Meter c/Equipment q/10
add-c n/Lab Maintenance t/61234567
add-b n/Meter c/Equipment q/2 f/Cracked screen d/1 January 2020 m/1
```

The historical date demonstrates recording an earlier fault. Replace it with the actual reported date.

There are now 8 usable Meters in Equipment. Each successful addition creates a separate report,
even if its details match another report. Avoid submitting the same report twice: each addition
deducts its quantity again. An invalid report or insufficient stock leaves both inventory and
the report list unchanged.

### Viewing reports and maintenance contacts

**Format:** `list-b`

Displays reports in insertion order with one-based indices. Listing does not change stock or contacts.

- Without an assigned contact, the contact line reads `Contact: Not assigned`.
- If the selected contact is deleted, it reads `Contact: Unavailable (contact deleted)`.
- Renumbering other contacts cannot redirect the report to another person.
- Adding a contact with identical details does not reconnect the report automatically.

### Removing a report and restoring stock

**Format:** `delete-b INDEX`

**This command restores every unit in the selected report to usable inventory.** Use it only when
all those units have been repaired, or when the report was incorrect. Do not use it to record
disposal or a partial repair. It takes effect immediately, without a confirmation prompt.

Use the current report index from `list-b`, not an inventory or contact index. In the example,
`delete-b 1` restores the two Meters, returning available stock to 10.

<div style="break-before: page; page-break-before: always;"></div>

## Contributions to the Developer Guide - Selected Extracts

These selections demonstrate my explanations of scheduling and broken-item design. I contributed
the original feature documentation and diagrams; the versions linked and displayed here include
subsequent team refinements. Persistence and loan-history implementation were contributed by teammates.

### Scheduling: keeping preparation attached to its session

Source: [Session scheduling](../DeveloperGuide.md#42-session-scheduling).

**Current approach:** Each `Session` owns its `Preparation` object.

Sorting moves session references without changing their associated preparation. Deleting another
session therefore cannot transfer requirements to the wrong session. This intentionally couples
a session with its preparation.

**Alternative:** Maintain preparation separately using session indices.

Both collections would require synchronization after sorting or deletion. Storing preparation
inside the session avoids this dependency on changing positions.

The schedule is stored in an `ArrayList` and sorted after successful additions. Stored and displayed
positions remain consistent, simplifying retrieval and deletion. The tradeoff is scanning existing
sessions for conflicts and sorting after insertion, which is sufficient for a small schedule.

### Broken-item tracking: responsibilities and relationships

Source: [Broken-item tracking](../DeveloperGuide.md#47-broken-item-tracking).

`BrokenItem` validates and stores report details. `BrokenItemManager` coordinates reports and stock
using the application's shared `Inventory` and `ContactManager`. `Inventory` validates and performs
quantity changes, while `Ui` displays the returned results.

![Broken-item classes and shared dependencies](../images/broken-item-class.png)

*Figure 1: Selected broken-item relationships. The manager stores reports in insertion order and
shares inventory and contacts with the application. A report may retain a reference to a contact
that has since been deleted. This is the current team-refined version of the diagram I introduced.*

### Stock consistency and operation ordering

Assume five Multimeters in Equipment and no existing reports. Reporting two broken units leaves
three usable units and one report for two units. `list-b` changes neither quantity. After both
units are repaired, `delete-b 1` restores usable stock to five and removes the report.

Reports for the same item remain separate, so deleting one restores only that report's quantity.

![Successful broken-item addition](../images/broken-item-add-sequence.png)

*Figure 2: Successful `add-b` after argument parsing and optional contact resolution. Report
construction validates details before stock changes; the manager appends the report only after
stock deduction succeeds. Routine getters, assertions, and logging are omitted. This is the
current team-refined version of the sequence diagram I introduced.*

For deletion, the order is deliberately reversed: restore stock first, then remove the report.
If restoration would exceed `Integer.MAX_VALUE`, an exception leaves both stock and the report
unchanged. Missing stock entries or categories are recreated on successful restoration.

These guarantees cover expected validation failures in the single-threaded application; they
are not database transactions or recovery from process failure.

Preparation and request availability read the shared inventory, so they reflect deductions and
restorations without updating a second stock total. Loans also use this inventory. Damaged units
returned from a loan must first be returned through the loan workflow, then reported with `add-b`;
the two commands are not atomic.

### Design choices and contact identity

**Separate reports rather than a broken flag on inventory items.** A report preserves its quantity,
fault, reported date, and optional contact even if deduction removes the last usable inventory
entry. A single flag would not represent multiple faults or batches with different contacts.

**Store a contact reference rather than its displayed index.** Membership uses identity, so
renumbering contacts cannot redirect a report, and adding identical contact details does not
revive a deleted link. Listing distinguishes `Not assigned` from `Unavailable (contact deleted)`;
a deleted contact does not prevent stock restoration.

Reports cannot be edited, partially repaired, disposed of, or reassigned to another contact.
Deletion restores all units and keeps no repair history. These limitations are stated explicitly
so users do not mistake report deletion for a general-purpose history-management operation.

### Verification

`BrokenItemTest` covers field validation and fixed-clock date boundaries.
`BrokenItemManagerTest` covers exact stock changes, invalid indices, missing stock, restoration
overflow, contact deletion, snapshots, and interaction with loans.
`ParserBrokenItemTest` covers command syntax, unavailable dependencies, and stock visibility across
features. Help tests and the console regression fixture check the advertised commands.

Diagram sources:
[class diagram](https://github.com/AY2627S1-CS2113-T11-3/tp/blob/master/docs/diagrams/broken-item-class.puml)
and
[addition sequence](https://github.com/AY2627S1-CS2113-T11-3/tp/blob/master/docs/diagrams/broken-item-add-sequence.puml).
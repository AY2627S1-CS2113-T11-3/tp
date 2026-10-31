# Bob persistence contract (version 2; version-1 compatibility)

Status: Implemented, including permanent loan history, version-1 migration, strict JSON storage, backup recovery,
automatic saving, and restart/error-handling verification.

## Scope and location

Persist inventory, sessions and their preparation lists, student requests, contacts, complete loan history, and broken-item reports as one
complete snapshot. The default file is `data/bob.json`, resolved against the process working directory, rather
than the JAR location. Starting Bob from a different directory selects a different data directory. Storage must
accept an explicit path for tests so tests never access a user's records.

Use UTF-8 JSON with readable indentation. A JSON library such as Gson will handle escaping and parsing;
do not construct JSON by joining strings. The implementation uses Gson for this conversion.
Configure the writer to include explicit `null` contact fields and the reader to enforce the validation rules below.
Use dedicated snapshot data classes instead of serializing managers or domain objects directly.

Companion files are `data/bob.json.bak` (the previous valid snapshot) and temporary sibling files used during
replacement. The default runtime directory is excluded from Git. Examples under
`docs/examples/` are fictional, tracked documentation, not live storage files.

The application supports one running Bob process per data directory. Version-1 snapshots migrate in memory.
Concurrent writers, cloud synchronization, encryption, import/export commands, and migration from other versions
are outside this scope.

## JSON structure

The root is an object with the fields below. Field names are case-sensitive. JSON object field order is
irrelevant; array order is significant. A valid empty snapshot is shown in
[`bob-v2-empty.json`](examples/bob-v2-empty.json). A populated loan-ledger example is
[`bob-v2.json`](examples/bob-v2.json). The original full-feature [version-1 example](examples/bob-v1.json) and
[empty version-1 example](examples/bob-v1-empty.json) remain compatibility fixtures.

| Root field | Type | Meaning |
| --- | --- | --- |
| `schemaVersion` | Integer | Writers use `2`; readers also accept version `1` through the conversion below. |
| `inventory` | Array | Ordered category objects containing ordered available-stock items. |
| `sessions` | Array | Sessions, each containing its own preparation array. |
| `requests` | Array | Student requests with their current statuses. |
| `contacts` | Array | Contacts in insertion order. |
| `loanRecords` | Array | All active, returned, and voided loans in creation order; duplicate details remain separate. |
| `nextLoanId` | Integer | Positive signed 64-bit next ID, greater than every stored loan ID. |
| `brokenItems` | Array | Broken reports in insertion order. Required in version 2; optional only for earlier version-1 files. |

### Inventory

Each category object contains `category` (string) and `items` (nonempty array).
Each item contains `name` (string) and `quantity` (positive integer).

Category names are unique ignoring case. Item names are unique ignoring case within their category; the same
item name may appear in different categories. Preserve category and item spelling and order. Depleted items
and empty categories are absent. Quantities represent stock currently available, after loan, fulfilled-request, and broken-item deductions.

### Sessions and preparations

Each session contains:

| Field | Type | Rule |
| --- | --- | --- |
| `name` | String | Nonblank session name. |
| `date` | String | Valid calendar date in `yyyy-MM-dd` format, for example `2099-11-05`. |
| `location` | String | Nonblank venue. |
| `startTime` | String | Valid 24-hour time in `HH:mm` format, for example `09:00`. |
| `endTime` | String | Same time format; strictly later than `startTime` on the same day. |
| `headcount` | Integer | Positive number of attendees. |
| `preparation` | Array | Ordered requirement objects; empty if nothing is required. |

Each preparation object contains `name`, `category` (nonblank strings), and `quantity` (positive integer).
This quantity is the current total requirement, not the quantity per attendee. It may no longer be a multiple
of headcount because `delete-p` removes total units. Name/category pairs are unique ignoring case within
each session. Preparation items may be missing from inventory or exceed available stock.

Store preparation inside the owning session. Do not save session indices as links: sorting can change them.
On restoration, sort sessions by the existing date, start time, and name rules. Reject sessions that overlap
at the same date and location, using the existing case-insensitive location matching. Adjacent sessions are valid.

### Student requests

Each request contains `studentName`, `itemName` (nonblank strings), `quantity` (positive integer), `deadline`
(calendar date in `yyyy-MM-dd` format), and `status` (exactly `pending`, `prepared`, or `fulfilled`).
Restore the saved status and sort requests using the existing deadline, student-name, and item-name rules.
Separate duplicate requests are allowed. Inventory matches are not required. Requests do not specify a category;
availability continues to be calculated across categories. Restoring a fulfilled request does not deduct stock
again; its saved available inventory already includes the deduction.

### Contacts

Each contact contains `name` (nonblank string), `phone` (nonempty digits-only string), `email`, and `role`.
`email` and `role` are required fields whose values may be `null` to represent absence. A supplied email must
satisfy the existing non-whitespace text on both sides of `@` rule; a supplied role must be nonblank.
Keep phone numbers as strings to preserve leading zeros. Separate duplicate contacts are allowed.

### Loan history

Each `loanRecords` entry has exactly these fields:

| Field | Type | Rule |
| --- | --- | --- |
| `loanId` | Integer | Positive signed 64-bit value, unique within the snapshot and less than `nextLoanId`. |
| `loan` | Object | Captured item and borrower details described below. |
| `status` | String | Exactly `ACTIVE`, `RETURNED`, or `VOIDED`. |
| `recordedAt` | String or null | ISO offset timestamp when Bob recorded the entry; null represents an unknown imported recording time. |
| `closedAt` | String or null | ISO offset timestamp when a return or correction was recorded. Null exactly when status is `ACTIVE`. |

The `loan` object contains `name`, `category`, `borrowerName` (nonblank strings), `quantity` (positive integer),
`borrowerPhone` (nonempty digits-only string), and `dueDate` (ISO calendar date). These immutable details are
independent of contact-list membership and available stock. Returned or voided records retain the original values.

A timestamp example is `2026-10-31T09:30:00+08:00`. Full timestamps are stored even though history listings show
calendar dates in their recorded offsets. Recording time is not necessarily the physical borrowing date; backlog
entries remain supported. Never replace an unknown imported timestamp with migration time.

IDs are allocated only after a successful stock deduction and are not recycled when records close.
Backup recovery restores the saved ledger and counter together; newer lost IDs are not reserved. The counter may equal
`9223372036854775807`; in that exhausted state, additions are rejected before inventory changes.
Returned and voided states are terminal. Restoring any record never deducts or restores stock; available inventory
already includes the effects of lending, returns, and corrections. Partial returns are not supported.

### Reading version-1 snapshots

Version 1 uses the original `loans` array with the captured detail fields above and no history metadata or counter.
Validate its exact original shape first, then convert each entry to an `ACTIVE` record in the same order with IDs
starting at 1, null timestamps, and `nextLoanId` equal to the number of entries plus 1. Retain all other feature data.
An absent version-1 `brokenItems` array defaults to empty. Version 2 does not allow this omission.

Normal loading, read-only commands, and ordinary exit do not rewrite a valid version-1 main file. The next
successful data-changing command writes version 2 and preserves the original version-1 text as the backup.
Backup recovery retains its existing pending-repair behavior, including repair on a successful exit save.
Previously returned or deleted records are not reconstructable from active-only snapshots or their backups.
Older Bob builds that support version 1 only reject a version-2 main file as unsupported.

### Broken-item reports

Each report contains `name`, `category`, `faultDescription` (nonblank strings), `quantity` (positive integer),
`reportedDate` (ISO calendar date, today or earlier), `contactIndex`, and `deletedContact`.
The last two fields are required but nullable. A nonnull `contactIndex` is a valid one-based position in the
saved contacts array. A nonnull `deletedContact` uses the contact object shape above and restores an unavailable
contact independently of registered contacts. Both cannot be nonnull; both null means unassigned.
This distinguishes identical contacts and prevents a replacement contact reconnecting a deleted link.

Restore reports without replaying `add-b`: saved inventory already excludes their units. Deleting a restored
report restores its quantity once. Original version-1 files without `brokenItems` remain readable; new saves
include this array. Use this updated Bob build to read files containing broken-item reports.

## Validation and restoration

- Require every documented field for its schema, including nullable fields. Empty lists are `[]`,
  never `null`. Reject unknown fields, duplicate JSON object keys, wrong types, and malformed JSON.
- Accept integer schema versions `1` and `2` only. Reject unsupported versions instead of guessing their meaning.
- Loan IDs and their counter are exact signed 64-bit integers; reject strings, fractions, exponents, and overflow.
  Validate positive, unique IDs, the next-ID bound, supported statuses, and closure-field consistency.
- All quantities and headcounts must be integers from `1` through `2147483647`. Reject fractions, numeric
  strings, and out-of-range values. Phone numbers must remain strings.
- Validate nonblank names, valid dates/times, uniqueness, and session conflicts before installing any state.
  Preserve saved display text; use existing case-insensitive matching rules for comparisons.
- ISO date/time formats are storage formats only. Console commands and displayed dates keep their current formats.
- Accept past session dates, request deadlines, and loan due dates during restoration. New session/request
  commands must continue to reject past dates. Passing time must not invalidate a previously valid snapshot.
- Restore inventory and all loan records independently. Never replay lending, return, or correction commands
  while loading. Reconnect the loan manager to the single restored inventory without any stock changes.
- Restore preparation totals directly, without multiplying them by headcount. Do not replay console commands
  or print addition confirmations during loading.
- Recalculate stock availability, shortages, displayed indices, and overdue flags when queried. These are not
  saved fields. Do not save greetings, the user's startup name, clocks, loggers, or UI formatting.
- Validate the complete snapshot in temporary state before exposing it to commands. Reject the whole snapshot
  on an invalid record; do not silently skip records or partially replace running state.

## Storage operations

The storage boundary has two operations, independent of console output:

| Operation | Contract |
| --- | --- |
| Load | Return a complete validated snapshot plus its source (main file, backup, or new empty state), or a structured failure. |
| Save | Accept a complete snapshot and report success only after main-file replacement completes, or report a structured failure. |

Distinguish invalid content, unsupported versions, and file-access/write failures so Bob can explain the problem
and include the resolved storage path. Storage does not print UI messages or replay business operations.

### Startup and recovery

| Condition | Required behavior |
| --- | --- |
| Main and backup both absent | Start with empty state; create the directory and file on the first successful change. |
| Main valid | Load it; an invalid backup does not prevent startup. |
| Main absent, malformed, invalid, or unreadable; backup valid | Load the backup and clearly report recovery, including that it may predate the last change. Preserve the original files until a successful save. |
| Existing main declares an unsupported schema version | Stop startup and preserve both files; do not fall back to an older backup and risk replacing newer-format data. |
| No usable snapshot, but either file exists or cannot be checked | Explain the failure and stop startup without writing empty state. |

A recovered backup is considered pending for saving to the main path. Do not overwrite a valid backup with a
corrupt main file. Temporary files left by an interruption are never promoted automatically; only the main and
backup files participate in recovery.

### Saving

1. Validate and serialize the complete snapshot into a temporary sibling file; finish writing and close it.
2. If the current main file is validated supported data, preserve it as the backup using a temporary file and
   replacement. If that backup update fails, abort the save and keep the main file intact. If the main file is
   corrupt after recovery, leave the valid backup intact instead of copying the corrupt main file over it.
3. Replace the main file with the completed temporary file using an atomic move. If atomic replacement is
   unsupported or fails, report a save failure and retain the existing main file; do not fall back to writing
   over the main file in place.
4. Report success and clear the pending-change flag only after replacement succeeds. Clean up temporary files
   where possible without deleting the main or backup files.

One file replacement commits inventory, all loan records, and the next-ID counter together. This protects against partially written snapshots;
it does not promise survival of every hardware or power failure. The backup contains the previous valid main
snapshot, so recovery can lose the most recent change.

### Automatic saving and exit

- Save synchronously after every command that successfully changes state, including request status changes,
  loan corrections, and returns. Determine this from an execution result, not just the command word.
- Do not write for help/list commands, rejected commands, or successful operations that make no change.
- If saving fails, retain the change in memory, mark state as pending, and show an explicit unsaved-change
  warning. A command confirmation must not imply that persistence succeeded. Retry with the complete latest
  state after the next successful change, or on exit.
- On `Bye` or end of input, retry pending state. With no pending state, exit without rewriting the snapshot.
- If a `Bye` save fails, offer retry, cancel exit, or explicit exit with unsaved changes. If input has ended,
  report the failure and exit with nonzero status; do not wait for an answer that cannot arrive.
- An explicit unsaved exit also returns a nonzero process exit code. Failed exit saves offer `retry`, `cancel`,
  and `exit`, ignoring capitalization. A canceled exit leaves the pending state available to later commands.
- Completed saves survive restarting Bob from the same working directory. Unsaved changes can be lost if the
  process is terminated. Do not rely on a shutdown hook as the normal saving mechanism.

## Acceptance criteria

1. Exporting and restoring a state preserves every stored value and the documented order, including optional
   fields, leading-zero phone numbers, duplicate loans, and all three request statuses.
2. Old sessions/requests still load, while new past-dated session/request commands remain rejected.
3. Loan restoration does not change inventory; returned/voided records remain closed. A later full return of an
   active record restores exactly its quantity, retains its history, and preserves its ID,
   including when its inventory item was absent at startup.
4. Preparation totals survive restoration unchanged, even when not divisible by headcount, and remain attached
   to their session after sorting or deletion of another session.
5. Missing files, invalid records, unsupported versions, interrupted writes, and backup recovery follow the
   policies above without silently overwriting valid data or loading partial state.
6. Storage failures leave pending changes visible and retryable. Read-only/rejected commands never trigger writes.
7. Restart-based tests cover every feature using temporary directories. Java changes pass the Gradle wrapper's
   `check` task with Java 25, and user/developer guides are updated when persistence becomes available.

The original version-1 populated example deliberately includes a past session and request, a preparation total of 13 for 10
attendees, an inventory quantity of 7 with a separate loan of 3, and a loan whose item is absent from available
inventory. These cases guard against rejecting historical data, multiplying totals, or deducting stock twice.

## Loan history verification

`LoanHistoryTest` covers immutable lifecycle transitions, active indices, rejected additions, ID reuse prevention,
and restoration overflow. `LoanHistoryStorageTest` covers strict ledger validation, exact large IDs, legacy
migration without writes, history round trips, exhausted counters, and backup recovery. `BobPersistenceTest`
verifies that a failed return save and exit retry retain the same identity and timestamps without another stock
restoration. `UI-HISTORY-*` cases exercise exact console output and fresh-process restart behavior.

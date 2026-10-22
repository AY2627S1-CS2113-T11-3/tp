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


### Loan tracking (phases 1 through 4)

`Bob` creates one `LoanManager` bound to its `Inventory` and passes it to `Parser.handleCommand`.
`add-l` uses the existing fixed-order prefix extraction and positive quantity parsing. `list-l` accepts
either no argument or the single lowercase filter `overdue`. `delete-l` and `return` accept one plain positive index, like `delete-c`.
`Ui` documents the commands in help.

`Loan` uses composition: a private `InventoryItem` snapshot holds item details and loan quantity, and an
immutable `Contact` holds the borrower. The item object is never exposed or shared with live stock.
Borrowers are never registered in `ContactManager`. Due dates are parsed strictly as English
`d MMMM uuuu` dates, with past dates permitted. Every loan is independent; no duplicate merging occurs.

Validation finishes before `LoanManager.addLoan` calls `Inventory.lendItem`. The inventory locates an
item by case-insensitive name/category, checks available stock, then deducts it without an inventory
confirmation. Lending and `delete-i` share the private deduction method, including removal of depleted
items and empty categories. Preparation continues to query live available stock through the existing
`getAvailableQuantity` method. Missing stock or invalid input leaves all feature state unchanged.

The manager stores loans in insertion order and exposes an immutable list snapshot for queries.
`LoanManager.deleteLoan` and `LoanManager.returnLoan` share the private
`removeLoanAndRestoreStock` method, which validates the index, restores stock through
`Inventory.restoreStock`, then removes the active record. Their public methods print separate
correction and return confirmations. No return history or partial return state is stored. Restoration matches
case-insensitively, merges stock with checked integer bounds, or recreates missing items/categories.
The loan is removed only after restoration succeeds, so overflow and invalid indices preserve state
for both corrections and returns.
Existing inventory objects are increased through `InventoryItem.addQuantity`, keeping the positive-stock
invariant. Remaining loan positions shift naturally with the list; no stored cross-feature index is used.

`Loan.isOverdue(LocalDate)` uses a strict before-date comparison, so a due-today loan is not overdue.
`LoanManager` uses an injected `Clock`, defaulting to the computer's timezone, and resolves one local
date per addition/display. Overdue status is calculated at display time rather than stored, so midnight
rollover cannot leave a cached flag stale. Successful past-date additions print a warning; rejected
additions do not. `listLoans` marks overdue rows, while `listOverdueLoans` filters them without changing
the original active-list indices. The filtered view is never used to resolve return/deletion indices.

`LoanOverdueTest` uses an adjustable clock to cover midnight rollover, timezone views, calendar boundaries,
and state preservation without sleeping or changing system time. `ParserLoanTest` fixes its loan clock
for reproducible overdue fixtures. Earlier UI fixtures are updated for the new approved backlog warning,
filter syntax, and marker behavior. History, partial returns, and persistence remain future work.

`LoanTest`, `LoanManagerTest`, and `ParserLoanTest` cover validation, separate records, exact output,
stock invariants, and immutable snapshots. `UI-LOAN-*` in `test/ui-test-plan.md` records console checks,
including preparation availability and supplier-contact isolation. Run the cumulative plan after each phase.

### Loan defensive programming

Loan validation uses the existing checked exception hierarchy. Invalid loan dates and malformed loan indices retain
their original parsing causes for debugging while preserving the existing user-facing messages. The loan stock helpers
also validate item names, categories, and positive quantities before modifying inventory, even when called directly.

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

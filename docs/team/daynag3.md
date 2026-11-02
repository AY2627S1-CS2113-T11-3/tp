# Dayna Gan - Project Portfolio Page

## Overview

Bob is a command-line application that helps lab technicians manage inventory, schedule lab sessions, prepare materials, track loans, and manage student requests. My main contributions are loan tracking and retained loan history, persistent record storage, shared error handling, UI regression testing, and documentation.

GitHub: [DaynaG3](https://github.com/DaynaG3)

### Summary of Contributions

- **Code contributed:** [GitHub repository](https://github.com/DaynaG3/tp) and [tP Code Dashboard](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=&sort=groupTitle&sortWithin=title&timeframe=commit&mergegroup=&groupSelect=groupByRepos&breakdown=true&checkedFileTypes=docs~functional-code~test-code~other&since=2026-09-18T00%3A00%3A00&filteredFileName=&tabOpen=true&tabType=authorship&tabAuthor=DaynaG3&tabRepo=AY2627S1-CS2113-T11-3%2Ftp%5Bmaster%5D&authorshipIsMergeGroup=false&authorshipFileTypes=docs~functional-code~test-code~other&authorshipIsBinaryFileTypeChecked=false&authorshipIsIgnoredFilesChecked=false).

- **Loan tracking:** Implemented the loan-tracking feature, including adding and listing loan records, deleting loan records, returning loaned items, and tracking overdue loans. The implementation introduced the `Loan` and `LoanManager` components, integrated loan commands with the parser and UI, and restored inventory quantities when items are returned or deleted.
  - [Add and list loan records](https://github.com/DaynaG3/tp/commit/33921dc)
  - [Delete loan records](https://github.com/DaynaG3/tp/commit/259a066)
  - [Return loaned items](https://github.com/DaynaG3/tp/commit/7684b95)
  - [Track overdue loans](https://github.com/DaynaG3/tp/commit/6f7abad)
  - [Document inventory restoration after returns](https://github.com/DaynaG3/tp/commit/a9a7163)

- **Persistent record storage:** Implemented automatic saving and startup restoration for inventory, sessions and preparations, contacts, student requests, loans, and broken-item reports. Added dedicated snapshot models, strict JSON validation, atomic file replacement, backup recovery, and failed-save retry and exit handling. Restoration preserves historical dates and existing stock quantities without applying deductions again.
  - [Implement persistence and recovery](https://github.com/DaynaG3/tp/commit/982bf7a)
  - [Integrate storage with merged broken-item and request features](https://github.com/DaynaG3/tp/commit/5d7a7c7)

- **Retained loan history:** Extended loan tracking to retain active, returned, and voided records with permanent IDs and recording and closure timestamps. Added `list-l history` and `list-l returned`, while preserving active-list indices for actions and restoring stock only once. Implemented schema-version-2 storage and in-memory migration of version-1 active loans without rewriting files during loading.
  - [Implement durable loan history, migration, and regression tests](https://github.com/DaynaG3/tp/commit/660e511)

- **Error handling and defensive programming:** Designed and implemented the initial inventory and lab-scheduling error-handling framework on the [`feat/error-handling`](https://github.com/DaynaG3/tp/tree/feat/error-handling) branch. This included validating command formats and inputs in the parser, rejecting invalid inventory categories, duplicate items, insufficient stock, invalid item indices, invalid quantities, invalid session dates/times/headcounts, invalid session indices, and conflicting sessions. I introduced the corresponding exception classes and integrated them across the inventory, session, parser, and UI layers so that invalid commands fail safely with user-facing messages.
  - [Add inventory command validation and exception classes](https://github.com/DaynaG3/tp/commit/781253e)
  - [Validate lab-scheduling commands and session conflicts](https://github.com/DaynaG3/tp/commit/6b3bc2d)
  - [Merged as PR #11: Add inventory and scheduling error handling](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/11)
  - [Merged as PR #22: Add error-handling JUnit tests](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/22)
  - [Strengthen loan exception handling](https://github.com/DaynaG3/tp/commit/3b9f369)
  - [Assert loan state and stock invariants](https://github.com/DaynaG3/tp/commit/7b31ed8)

- **Testing and verification:** Added and maintained unit tests for loan management, loan models, parser behavior, overdue-loan evaluation, loan UI behavior, exception cases, invariants, and debug logging. Extended automated tests to cover snapshot round trips, strict validation, startup and backup recovery, failed-save retries, schema migration, stable loan IDs, lifecycle transitions, and stock consistency. Created and expanded the command-by-command UI regression workflow, including isolated storage fixtures and restart checks. Verified the loan-history implementation against the full project suite: 370 unit tests and Checkstyle passed on Java 25, and 86 UI cases covering 620 commands passed in both normal and assertions-enabled modes.
  - [Loan and parser tests](https://github.com/DaynaG3/tp/commit/33921dc)
  - [Overdue-loan tests](https://github.com/DaynaG3/tp/commit/6f7abad)
  - [Loan validation tests](https://github.com/DaynaG3/tp/commit/3b9f369)
  - [UI regression records](https://github.com/DaynaG3/tp/commit/5a2844e)
  - [Create the UI testing workflow](https://github.com/DaynaG3/tp/commit/4e1fd77)
  - [Expand inventory UI coverage](https://github.com/DaynaG3/tp/commit/2588eda)
  - [Automate scheduling regression checks](https://github.com/DaynaG3/tp/commit/3b7db89)
  - [Establish the cross-feature regression baseline](https://github.com/DaynaG3/tp/commit/f9a6fb9)
  - [Add persistence fixtures, recovery checks, and transcript support](https://github.com/DaynaG3/tp/commit/448c8bc)
  - [Reconcile UI cases with merged stock behavior](https://github.com/DaynaG3/tp/commit/ef757f5)
  - For the subsequent User Guide audit, added 9 reusable cases covering 69 commands, with expected responses
    recorded before execution. Ran these against an isolated upstream build alongside the 86 existing regression
    cases: all 95 cases, 695 steps, and 689 commands passed. The same upstream build passed all 370 unit tests
    with zero Checkstyle violations on Java 25. Existing test cases were preserved, and no application code changed.
  - [User Guide example regression cases](../../test/ui-test-plan.md#user-guide-audit-against-upstream-cf84af2)

- **Enhancements implemented:** Separated loan queries and confirmations from console rendering, captured borrower values independently of mutable contacts, and added invariant assertions and diagnostic logging. Used immutable loan-history records and complete snapshots to keep inventory and loan lifecycle changes consistent across restarts. Added RepoSense author boundaries around storage contributions while preserving teammates' attribution.
  - [Capture borrower details independently](https://github.com/DaynaG3/tp/commit/a999fe1)
  - [Render loan confirmations through the UI](https://github.com/DaynaG3/tp/commit/393cf34)
  - [Separate loan queries from rendering](https://github.com/DaynaG3/tp/commit/f3ccc8a)
  - [Add loan diagnostic logging](https://github.com/DaynaG3/tp/commit/5c1c6c8)
  - [Attribute storage implementation and tests](https://github.com/DaynaG3/tp/commit/e52a05d)

- **Contributions to the User Guide:** Documented loan addition, listing, correction, full returns, overdue filtering, and the history and returned-only views, with examples, index rules, permanent IDs, dates, and limitations. Added the saving-and-restoring section covering the working-directory data location, automatic saves, backups, recovery, unsupported formats, failed-save messages, and exit choices. Corrected session ordering and validation examples, updated application JAR references, and reconciled persistence instructions with the merged stock-changing features.
  - [Loan command documentation](https://github.com/DaynaG3/tp/commit/33921dc)
  - [Return and delete documentation](https://github.com/DaynaG3/tp/commit/7684b95)
  - [Overdue-loan documentation](https://github.com/DaynaG3/tp/commit/6f7abad)
  - [Correct session ordering and validation examples](https://github.com/DaynaG3/tp/commit/f9a6fb9)
  - [Document saving, recovery, and failed-save exit choices](https://github.com/DaynaG3/tp/commit/1b15cae)
  - [Align persistence instructions with merged features](https://github.com/DaynaG3/tp/commit/96dde32)
  - [Document loan history and returned-only commands](https://github.com/DaynaG3/tp/commit/d288e49)
  - Audited all 17 sections against upstream `cf84af2`, checking command formats, help output, and internal links.
  - Updated feature instructions, examples, FAQs, and limitations to match lookup, broken-item tracking,
    loan history, persistence, migration, and request fulfillment.
  - Clarified example setup, validation, indices, and stock changes; added a dedicated `list-l returned` section.
  - [Updated User Guide](../UserGuide.md)
  - [Upstream revision reviewed](https://github.com/AY2627S1-CS2113-T11-3/tp/commit/cf84af215d8ab42ead8c2e23029764162ef1f44d)

- **Contributions to the Developer Guide:** Reorganized the guide using the Address Book Level 3 structure, adding the table of contents, acknowledgements, setup and build instructions, component descriptions, requirements, use cases, glossary, and manual testing guidance. Restored and expanded the scheduling design explanation using the existing scheduling diagrams. Documented loan responsibilities, borrower snapshots, UI separation, validation, invariants, logging, inventory restoration, and design alternatives. Added storage architecture, snapshot boundaries, automatic saving, startup recovery, and integration behavior. Documented retained loan history, lifecycle and ID rules, timestamps, migration, stock consistency, limitations, alternatives, and phased verification. Created four loan UML diagrams and four storage diagrams with editable PlantUML sources and rendered PNGs, then updated the affected diagrams for loan history. Corrected the JUnit reference to match version 5.14.4.
  - [Loan-tracking design and UML diagrams](https://github.com/DaynaG3/tp/commit/a2608cd)
  - [Loan exception handling](https://github.com/DaynaG3/tp/commit/3b9f369)
  - [Loan invariants and logging](https://github.com/DaynaG3/tp/commit/7b31ed8)
  - [Inventory restoration after returns](https://github.com/DaynaG3/tp/commit/a9a7163)
  - [Restructure the guide and expand project-wide documentation](https://github.com/DaynaG3/tp/commit/064a282)
  - [Restore and explain the scheduling design](https://github.com/DaynaG3/tp/commit/7abb7ac)
  - [Document storage architecture, classes, saving, and recovery with UML](https://github.com/DaynaG3/tp/commit/a04c48a)
  - [Explain loan-history design, alternatives, and updated UML flows](https://github.com/DaynaG3/tp/commit/3a4bf69)
  - [Record implementation phases and verification criteria](https://github.com/DaynaG3/tp/commit/df5003f)
  - [Correct the version-specific JUnit documentation link](https://github.com/DaynaG3/tp/commit/48c7e6f)

- **Storage format and examples:** Wrote [StorageFormat.md](../StorageFormat.md), starting with the version-1 persistence contract and extending it to version 2. Defined JSON fields, types, ordering, validation, stock semantics, backup and recovery rules, compatibility, and migration. Added populated and empty version-1 and version-2 example files under [docs/examples](../examples), and documented unknown migrated timestamps and ID-counter behavior after backup recovery.
  - [Define the persistence contract and version-1 examples](https://github.com/DaynaG3/tp/commit/0a98672)
  - [Update the contract for implemented persistence](https://github.com/DaynaG3/tp/commit/80bed4c)
  - [Document merged-feature storage behavior](https://github.com/DaynaG3/tp/commit/96dde32)
  - [Add version-2 example snapshots](https://github.com/DaynaG3/tp/commit/660e511)
  - [Document the version-2 ledger and compatibility rules](https://github.com/DaynaG3/tp/commit/d288e49)

- **Testing documentation and READMEs:** Created and maintained [test/ui-test-plan.md](../../test/ui-test-plan.md) with exact inputs and expected outputs for inventory, scheduling, contacts, loans, persistence, recovery, and loan history. Documented the testing workflow in the `test-ui` skill, maintained regression records and date expectations, and updated [text-ui-test/README.md](../../text-ui-test/README.md) with runner instructions, case-prefix selection, storage groups, and phased verification. Updated the [root README](../../README.md) with persistence and recovery links and [docs/README.md](../README.md) with the retained-history feature summary. Standardized guide and test references to `bob.jar`.
  - [Create the test plan and documented UI testing workflow](https://github.com/DaynaG3/tp/commit/4e1fd77)
  - [Maintain UI regression records and instructions](https://github.com/DaynaG3/tp/commit/5a2844e)
  - [Correct stale date expectations](https://github.com/DaynaG3/tp/commit/353d14b)
  - [Standardize application JAR references](https://github.com/DaynaG3/tp/commit/420f08e)
  - [Document persistence UI cases and runner usage](https://github.com/DaynaG3/tp/commit/80bed4c)
  - [Update README feature and storage summaries](https://github.com/DaynaG3/tp/commit/d288e49)
  - [Document the loan-history verification sequence](https://github.com/DaynaG3/tp/commit/c816af3)
  - Extended the UI test plan with the `UI-UG-*` audit cases for quick start, inventory and lookup, scheduling,
    preparation, request fulfillment, contacts, loans and history, overdue filtering, broken-item repairs, and
    the complete help output. Recorded the upstream revision and empty-storage preconditions so the examples
    can be repeated without using a technician's saved data.

- **Team documentation:** Updated my entry in [About Us](../AboutUs.md), created this project portfolio page, and maintained it with implementation, testing, and documentation contributions supported by commit references.
  - [Update About Us](https://github.com/DaynaG3/tp/commit/fc22d1b)
  - [Create the project portfolio page](https://github.com/DaynaG3/tp/commit/59e2527)

- **Contributions to team-based tasks:** Created and assigned feature and documentation tasks to coordinate
  the team's backlog, define product scope, and clarify ownership.
  - Created core feature tasks for [help (#12)](https://github.com/AY2627S1-CS2113-T11-3/tp/issues/12),
    [lab scheduling (#13)](https://github.com/AY2627S1-CS2113-T11-3/tp/issues/13),
    [inventory (#14)](https://github.com/AY2627S1-CS2113-T11-3/tp/issues/14), and
    [lab preparation (#18)](https://github.com/AY2627S1-CS2113-T11-3/tp/issues/18).
  - Captured the [equipment-addition user story (#15)](https://github.com/AY2627S1-CS2113-T11-3/tp/issues/15)
    and defined the [record-persistence task (#51)](https://github.com/AY2627S1-CS2113-T11-3/tp/issues/51).
  - Coordinated documentation requirements through tasks for
    [Developer Guide class diagrams (#49)](https://github.com/AY2627S1-CS2113-T11-3/tp/issues/49) and
    [individual portfolio pages (#50)](https://github.com/AY2627S1-CS2113-T11-3/tp/issues/50).

- **Review contributions:** Reviewed teammates' feature and design documentation changes and helped integrate
  completed work.
  - Submitted review comments on [PR #43: Item-management Developer Guide](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/43).
  - Submitted review comments on [PR #45: Request tracking](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/45).
  - Approved and merged [PR #48: Lab-scheduling Developer Guide](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/48).

- **Contributions beyond the project team:** Not applicable.

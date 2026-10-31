# Wei Xiong - Project Portfolio Page

## Overview

Bob is a command-line application that helps lab technicians manage inventory, schedule lab sessions, prepare
materials, track loans, and manage student requests. My main contributions are inventory management and student
request tracking.

GitHub: [Lokwx](https://github.com/Lokwx)

### Summary of Contributions

- **Code contributed:** [GitHub repository](https://github.com/Lokwx/tp) and
  [tP Code Dashboard](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=Lokwx&sort=groupTitle&sortWithin=title&timeframe=commit&mergegroup=&groupSelect=groupByRepos&breakdown=true&checkedFileTypes=docs~functional-code~test-code~other).

- **Inventory management:** Implemented the original inventory feature, including the `Inventory` and
  `InventoryItem` components and the `add-i`, `delete-i`, and `list-i` command flows. Items are grouped by category;
  technicians can register stock, remove a quantity, and view the remaining units. Removing all units removes the
  item and its category when empty. This gives the other features a common source of available stock.
  - [Implement inventory addition, removal, and listing](https://github.com/Lokwx/tp/commit/dedb0b0)
  - [Improve inventory removal output](https://github.com/Lokwx/tp/commit/e7be2de)
  - [Add inventory diagnostic logging](https://github.com/Lokwx/tp/commit/02945cd)
  - The shared inventory validation and exception framework was contributed by Dayna; my inventory contribution
    covers the original stock-management behavior and the additions linked here.

- **Student request tracking:** Implemented `Request` and `RequestManager` and integrated `add-r`, `delete-r`,
  `list-r`, and `mark-r` with the parser, application startup, and help text. A request records a student, item,
  quantity, deadline, and status. Requests are sorted by deadline, with student and item names breaking ties.
  Listing compares each request with live stock across categories and displays shortages without reserving units.
  This helps technicians identify upcoming requirements and arrange missing stock.
  - [Introduce the request model](https://github.com/Lokwx/tp/commit/601b009)
  - [Introduce the request manager](https://github.com/Lokwx/tp/commit/3cc1f74)
  - [Integrate request commands](https://github.com/Lokwx/tp/commit/7a93962)
  - [Add read-only stock totals for requests](https://github.com/Lokwx/tp/commit/64e1689)

- **Enhancements implemented:** Added request-specific deadline, index, and status handling, assertions, and
  diagnostics. Injected a clock into deadline validation so tests can check today's boundary consistently.
  Hardened request construction and quantity parsing, and used overflow-safe aggregation for availability.
  Added a reminder to refresh the request list after additions and deletions because sorted indices can change.
  - [Make deadline checks testable with an injected clock](https://github.com/Lokwx/tp/commit/60ce111)
  - [Harden request validation and availability totals](https://github.com/Lokwx/tp/commit/a736545)
  - [Remind users to refresh request indices](https://github.com/Lokwx/tp/commit/dc932c7)

- **Testing and verification:** Wrote the initial inventory model and manager tests and added inventory logging
  tests. Added request model, manager, parser, and logging tests covering valid commands, rejected inputs,
  deadline ordering, status changes, and availability after stock changes. These tests help detect regressions
  in the inventory and request workflows.
  - [Initial inventory unit tests](https://github.com/Lokwx/tp/commit/7951f7f)
  - [Inventory logging tests](https://github.com/Lokwx/tp/commit/5b2651c)
  - [Request model tests](https://github.com/Lokwx/tp/commit/55a6069)
  - [Request manager tests](https://github.com/Lokwx/tp/commit/9f650f6)
  - [Request parser tests](https://github.com/Lokwx/tp/commit/d2c277b)
  - [Request logging tests](https://github.com/Lokwx/tp/commit/0516ba6)

- **Contributions to the User Guide:** Documented student request commands, confirmations, deadlines, status
  changes, and read-only availability. Expanded the existing inventory instructions with duplicate-addition
  examples, input corrections, category-specific indices, and complete-removal behavior. Added request examples
  explaining rejected additions and stock totals across categories.
  - [Student request documentation](https://github.com/Lokwx/tp/commit/ee5803f)
  - [Inventory management section](../UserGuide.md#inventory-management)
  - [Student request tracking section](../UserGuide.md#student-request-tracking)

- **Contributions to the Developer Guide:** Documented inventory addition responsibilities, storage, validation,
  design alternatives, and limitations, with class and sequence diagrams and editable PlantUML sources.
  Expanded the request design with the addition flow, deadline sorting, clock-based validation, a sequence
  diagram, live availability, and test references.
  - [Earlier inventory design documentation](https://github.com/Lokwx/tp/commit/9c8ffef)
  - [Initial student request design documentation](https://github.com/Lokwx/tp/commit/23b82a8)
  - [Inventory management design](../DeveloperGuide.md#inventory-management)
  - [Student request tracking design](../DeveloperGuide.md#student-request-tracking)

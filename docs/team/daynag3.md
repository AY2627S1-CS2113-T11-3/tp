# Dayna Gan - Project Portfolio Page

## Overview

Bob is a command-line application that helps lab technicians manage inventory, schedule lab sessions, prepare materials, track loans, and manage student requests.

GitHub: [DaynaG3](https://github.com/DaynaG3)

### Summary of Contributions

- **Code contributed:** [GitHub repository](https://github.com/DaynaG3/tp) and [tP Code Dashboard](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=&sort=groupTitle&sortWithin=title&timeframe=commit&mergegroup=&groupSelect=groupByRepos&breakdown=true&checkedFileTypes=docs~functional-code~test-code~other&since=2026-09-18T00%3A00%3A00&filteredFileName=&tabOpen=true&tabType=authorship&tabAuthor=DaynaG3&tabRepo=AY2627S1-CS2113-T11-3%2Ftp%5Bmaster%5D&authorshipIsMergeGroup=false&authorshipFileTypes=docs~functional-code~test-code~other&authorshipIsBinaryFileTypeChecked=false&authorshipIsIgnoredFilesChecked=false).

- **Loan tracking:** Implemented the loan-tracking feature, including adding and listing loan records, deleting loan records, returning loaned items, and tracking overdue loans. The implementation introduced the `Loan` and `LoanManager` components, integrated loan commands with the parser and UI, and restored inventory quantities when items are returned or deleted.
  - [Add and list loan records](https://github.com/DaynaG3/tp/commit/33921dc)
  - [Delete loan records](https://github.com/DaynaG3/tp/commit/259a066)
  - [Return loaned items](https://github.com/DaynaG3/tp/commit/7684b95)
  - [Track overdue loans](https://github.com/DaynaG3/tp/commit/6f7abad)
  - [Document inventory restoration after returns](https://github.com/DaynaG3/tp/commit/a9a7163)

- **Error handling and defensive programming:** Designed and implemented the initial inventory and lab-scheduling error-handling framework on the [`feat/error-handling`](https://github.com/DaynaG3/tp/tree/feat/error-handling) branch. This included validating command formats and inputs in the parser, rejecting invalid inventory categories, duplicate items, insufficient stock, invalid item indices, invalid quantities, invalid session dates/times/headcounts, invalid session indices, and conflicting sessions. I introduced the corresponding exception classes and integrated them across the inventory, session, parser, and UI layers so that invalid commands fail safely with user-facing messages.
  - [Add inventory command validation and exception classes](https://github.com/DaynaG3/tp/commit/781253e)
  - [Validate lab-scheduling commands and session conflicts](https://github.com/DaynaG3/tp/commit/6b3bc2d)
  - [Merged as PR #11: Add inventory and scheduling error handling](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/11)
  - [Merged as PR #22: Add error-handling JUnit tests](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/22)
  - [Strengthen loan exception handling](https://github.com/DaynaG3/tp/commit/3b9f369)
  - [Assert loan state and stock invariants](https://github.com/DaynaG3/tp/commit/7b31ed8)

- **Testing and verification:** Added and maintained unit tests for loan management, loan models, parser behavior, overdue-loan evaluation, loan UI behavior, exception cases, invariants, and debug logging. Also updated UI regression test plans and records for loan-related behavior.
  - [Loan and parser tests](https://github.com/DaynaG3/tp/commit/33921dc)
  - [Overdue-loan tests](https://github.com/DaynaG3/tp/commit/6f7abad)
  - [Loan validation tests](https://github.com/DaynaG3/tp/commit/3b9f369)
  - [UI regression records](https://github.com/DaynaG3/tp/commit/5a2844e)

- **Enhancements implemented:** [Placeholder: describe the most technically difficult parts of loan tracking and error handling, the design decisions made, and the estimated effort involved.]

- **Contributions to the User Guide:** Documented the loan commands and their expected behavior, including adding/listing loans, deleting loans, returning items, and tracking overdue loans. Also updated references to the application JAR where required.
  - [Loan command documentation](https://github.com/DaynaG3/tp/commit/33921dc)
  - [Return and delete documentation](https://github.com/DaynaG3/tp/commit/7684b95)
  - [Overdue-loan documentation](https://github.com/DaynaG3/tp/commit/6f7abad)

- **Contributions to the Developer Guide:** Wrote and updated the loan-tracking design documentation, including the loan architecture, implementation details, exception handling, invariants, logging behavior, inventory restoration, and UML diagrams.
  - [Loan-tracking design and UML diagrams](https://github.com/DaynaG3/tp/commit/a2608cd)
  - [Loan exception handling](https://github.com/DaynaG3/tp/commit/3b9f369)
  - [Loan invariants and logging](https://github.com/DaynaG3/tp/commit/7b31ed8)
  - [Inventory restoration after returns](https://github.com/DaynaG3/tp/commit/a9a7163)

- **Contributions to team-based tasks:** Assigned issues to teammates, reviewed teammates' work, and merged teammates' pull requests. [Placeholder: add issue numbers, PR numbers, and brief descriptions of the coordination and review work.]

- **Review and mentoring contributions:** [Placeholder: add links to pull requests reviewed and examples of helping teammates with implementation, debugging, testing, or integration.]

- **Contributions beyond the project team:** Not applicable.

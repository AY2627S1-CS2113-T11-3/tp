# Tan Yu Jun - Project Portfolio Page

## Overview

Bob is a command-line assistant that helps lab technicians manage inventory, schedule lab sessions, prepare
materials, track student requests and loans, and keep contacts. It is written in Java and is optimized for users
who type quickly.

I built the features that answer "Do we have enough for this lab, and where is everything?": session preparation
and quick item lookup. I also set up automated console regression testing for the team.

## Summary of Contributions

### Code contributed

[RepoSense link](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=j-unn-y&sort=groupTitle&sortWithin=title&timeframe=commit&mergegroup=&groupSelect=groupByRepos&breakdown=true&checkedFileTypes=docs~functional-code~test-code~other)

### Enhancements implemented

- **New feature: Lab session preparation (`add-p`, `delete-p`, `list-p`)**
  ([#16](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/16))
  - What it does: Converts a per-attendee quantity into a total for the session's headcount, merges repeated items,
    and compares each requirement with live stock, showing the shortfall for any item that is short.
  - Justification: Technicians can see before a lab whether they need to order, borrow, or collect items, instead of
    working out totals by hand.
  - Highlights: Each `Session` owns its `Preparation`, so requirements stay with the right session when the schedule
    is re-sorted or a session is deleted. Stock is read each time the list is shown, never reserved, so loans and
    inventory changes are reflected immediately without keeping two copies of stock in sync. Totals use
    overflow-checked arithmetic, and invalid changes are rejected before anything is modified.
- **New feature: Quick item lookup (`find-i KEYWORD`)**
  ([#40](https://github.com/AY2627S1-CS2113-T11-3/tp/issues/40))
  - What it does: Finds items whose name or category contains a keyword, ignoring case, and shows units in stock,
    units on loan, and whether the item can be handed out now.
  - Justification: An item disappears from `list-i` once every unit is lent out. Lookup still finds it and shows it is
    all on loan, so technicians do not wrongly conclude the lab has none.
  - Highlights: Combines inventory and active loans through read-only snapshots, so a search cannot change stock.
    Search logic is kept in `ItemFinder`, separate from parsing and display, so it is tested directly
    (16 tests).
- **Enhancement to existing features:** Made the session date error message show the expected `yyyy` year format.

### Contributions to the User Guide

- Wrote the Lab session preparation, Inventory management, and Find an item (`find-i`) sections.
- Wrote most of Quick start and Command format, and contributed to Lab session scheduling, Errors and
  troubleshooting, FAQ, Current limitations, and the Command summary.

### Contributions to the Developer Guide

- Wrote the Session preparation implementation section
  ([#53](https://github.com/AY2627S1-CS2113-T11-3/tp/issues/53)): an overview of each class's responsibilities,
  a worked example, design alternatives, validation and testing, and limitations.
- UML diagrams: preparation class diagram and `add-p` sequence diagram, both in PlantUML.

### Contributions to team-based tasks

- Automated the console regression test in CI so it runs on Windows, macOS, and Linux for every PR
  ([#29](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/29)).
- Wrote the manual console test plans used to check features before release (`test/ui-test-plan.md` and session
  test plans).
- Recorded user stories and tasks in the issue tracker
  ([#27](https://github.com/AY2627S1-CS2113-T11-3/tp/issues/27),
  [#28](https://github.com/AY2627S1-CS2113-T11-3/tp/issues/28),
  [#53](https://github.com/AY2627S1-CS2113-T11-3/tp/issues/53),
  [#54](https://github.com/AY2627S1-CS2113-T11-3/tp/issues/54)).
- Added RepoSense author tags so code authorship is attributed accurately
  ([#54](https://github.com/AY2627S1-CS2113-T11-3/tp/issues/54)).

### Review and mentoring contributions

- Reviewed PRs: [#25](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/25),
  [#30](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/30), and
  [#52](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/52).

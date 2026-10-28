# Gavin Lim - Project Portfolio Page

## Overview

Bob is a command-line application that helps lab technicians manage inventory, schedule lab sessions,
prepare materials, maintain contacts, and track student requests, loans, and broken items.

GitHub: [gavinlimsh](https://github.com/gavinlimsh)

My main contributions are lab session scheduling and broken-item tracking: helping technicians avoid overlapping
sessions and distinguish damaged equipment from stock that is ready to use.

[View my code contributions on RepoSense](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=gavinlimsh)

## Summary of Contributions

### Lab session scheduling

This feature gives technicians a chronological schedule and checks for overlapping sessions at the same location.
The validation and ordering tests help keep the displayed session positions consistent with later commands.

- Implemented the initial session scheduling feature, including adding, listing, and deleting sessions through
  `add-s`, `list-s`, and `delete-s`.
- Improved chronological ordering and validation messages, and added assertions and logging to help diagnose
  scheduling problems. Subsequent improvements were made collaboratively with the team.
- Expanded tests for invalid names, locations, dates, times, headcounts, and indices; overlapping and adjacent
  sessions; chronological ordering; and preparation remaining attached to the correct session after renumbering.
- Relevant files: [Session](../../src/main/java/seedu/bob/Session.java),
  [SessionManager](../../src/main/java/seedu/bob/SessionManager.java),
  [SessionTest](../../src/test/java/seedu/bob/SessionTest.java), and
  [SessionManagerTest](../../src/test/java/seedu/bob/SessionManagerTest.java).

### Broken-item tracking

Damaged equipment should not count as usable stock. This feature connects fault reports to inventory changes,
so technicians can assess availability and identify a maintenance contact without maintaining a separate list.
Implemented in [PR #52](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/52).

- Implemented `add-b`, `list-b`, and `delete-b` to record an item's name, category, quantity, fault description,
  reported date, and optional maintenance contact.
- Integrated reports with shared inventory: reporting broken units deducts available stock, while deleting a
  fully repaired or incorrectly recorded report restores its entire quantity. Invalid operations and restoration
  overflow retain the existing stock and reports.
- Kept contact links attached to the selected contact when list positions change. Reports show that a contact
  is unavailable after deletion, rather than silently linking to a replacement contact.
- Connected the feature to command parsing, application startup, help text, and console output. Existing
  preparation and request displays reflect stock changes through the shared inventory.
- Added [BrokenItemTest](../../src/test/java/seedu/bob/BrokenItemTest.java),
  [BrokenItemManagerTest](../../src/test/java/seedu/bob/BrokenItemManagerTest.java), and
  [ParserBrokenItemTest](../../src/test/java/seedu/bob/ParserBrokenItemTest.java), covering validation, stock
  restoration, overflow, contact deletion, command errors, and interactions with other features.

### Documentation

- Expanded the [User Guide's scheduling section](../UserGuide.md#lab-session-scheduling) with command formats,
  validation rules, examples, conflict handling, and guidance about changing list indices.
- Added [broken-item instructions](../UserGuide.md#broken-item-tracking), including stock changes, maintenance
  contacts, damaged loan returns, error recovery, and the limitations of full-report deletion.
- Expanded the [Developer Guide's scheduling section](../DeveloperGuide.md#session-scheduling) with a walkthrough,
  design considerations, and class and sequence diagrams.
- Documented [broken-item tracking](../DeveloperGuide.md#broken-item-tracking), including shared-stock updates,
  contact identity, validation, design alternatives, and class and sequence diagrams.

### Integration and maintenance

- Restored parser compilation after contact-command changes and updated help-output tests.
- Updated console expected output when broken-item commands were added to help, keeping the regression fixture
  consistent with the advertised commands.
- Fixed the conflict-resolution errors in [PR #58](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/58): restored
  the lookup method boundary and broken-item help expectations. Verified the fix with Java 25, automated tests,
  Checkstyle, and the console regression test before pushing; all three platform checks subsequently passed.

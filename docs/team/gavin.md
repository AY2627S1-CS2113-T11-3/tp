# Gavin Lim - Project Portfolio Page

## Overview

Bob is a command-line application that helps lab technicians manage inventory, schedule lab sessions,
prepare materials, maintain contacts, and track student requests, loans, and broken items.

GitHub: [gavinlimsh](https://github.com/gavinlimsh)

## Summary of Contributions

### Lab session scheduling

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

This is the current milestone's development work. Its Developer Guide section remains to be written.

### Documentation

- Expanded the [User Guide's scheduling section](../UserGuide.md#lab-session-scheduling) with command formats,
  validation rules, examples, conflict handling, and guidance about changing list indices.
- Added [broken-item instructions](../UserGuide.md#broken-item-tracking), including stock changes, maintenance
  contacts, damaged loan returns, error recovery, and the limitations of full-report deletion.
- Expanded the [Developer Guide's scheduling section](../DeveloperGuide.md#session-scheduling) with a walkthrough,
  design considerations, and class and sequence diagrams.

### Integration and maintenance

- Restored parser compilation after contact-command changes and updated help-output tests.
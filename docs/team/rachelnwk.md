# Rachel Li - Project Portfolio Page

## Overview

Bob is a command-line application that helps lab technicians manage inventory, schedule lab sessions, prepare
materials, track loans, and manage student requests. My main contributions are contact management and the `help`
command.

GitHub: [rachelnwk](https://github.com/rachelnwk)

### Summary of Contributions

- **Code contributed:** [GitHub repository](https://github.com/rachelnwk/tp) and
  [tP Code Dashboard](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=rachelnwk&sort=groupTitle&sortWithin=title&timeframe=commit&mergegroup=&groupSelect=groupByRepos&breakdown=true&checkedFileTypes=docs~functional-code~test-code~other).

- **Contact management:** Implemented `Contact` and `ContactManager` and integrated the `add-c`, `delete-c`, and
  `list-c` commands with the parser and application startup
  ([#31](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/31)). A contact records a name and phone number, with an
  optional email and role, so technicians can keep suppliers, lecturers, and colleagues in one place. Contacts are
  listed with stable numbering, and deleting one renumbers the rest so the displayed indices stay consecutive.
  Duplicate contacts are allowed because different people can share a name.
  - [Add contacts](https://github.com/rachelnwk/tp/commit/38ec143)
  - [List contacts](https://github.com/rachelnwk/tp/commit/01abb08)
  - [Delete contacts](https://github.com/rachelnwk/tp/commit/c770736)

- **Help command:** Implemented `help`, which lists every command grouped by feature (inventory, lab sessions, lab
  preparation, contacts, and other) so that users need not consult the user guide to recall a command format
  ([#33](https://github.com/AY2627S1-CS2113-T11-3/tp/pull/33)). The text is kept in `Ui` beside the shared
  divider, and extra arguments such as `help me` are rejected with a usage message, like every other command.
  - [Add the help command](https://github.com/rachelnwk/tp/commit/6ce758c)

- **Enhancements implemented:** Validated contact input so that bad data is rejected before anything is stored:
  phone numbers must be digits only, emails must contain `@` and no spaces, and supplied optional values must not be
  blank. An empty list shows `No contacts saved.` instead of a blank screen, and an invalid `delete-c` index is
  reported with a dedicated exception that follows the existing `CommandException` pattern. Added assertions and
  diagnostic logging in line with the other features.

- **Testing and verification:** Wrote 28 JUnit tests alongside the features. Contact model tests cover valid
  and invalid fields; manager tests cover adding, renumbering after deletion, and out-of-range indices; parser tests
  cover each command end to end, including missing or repeated prefixes, duplicate contacts, and renumbering after a
  deletion; help tests compare the full output against the text in the user guide. Extended the console regression
  test (`text-ui-test`) and the manual test plan (`test/ui-test-plan.md`) with contact and help cases, and ran the
  real application to confirm the behavior.
  - [Extra list tests, including renumbering and duplicates](https://github.com/rachelnwk/tp/commit/0402c49)

- **Contributions to the User Guide:** Wrote the Contact management section (`add-c`, `list-c`, `delete-c`, with
  examples and expected output) and the Viewing help section. Added the matching rows to the command summary and
  updated the error table and limitations, which no longer described `help` as unsupported.
  - [Contact management section](../UserGuide.md#contact-management)
  - [Viewing help section](../UserGuide.md#viewing-help)

- **Contributions to the Developer Guide:** Documented the contact design, including the validation rules, the
  decision to allow duplicates, the single-line `delete-c INDEX` format that matches `delete-s`, and known
  limitations such as a role that begins with a letter and a slash being read as a command prefix.
  - [Contact management design](../DeveloperGuide.md#contact-management)

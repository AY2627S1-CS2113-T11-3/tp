---
name: seedu-java-coding-standard
description: Apply the SE-EDU basic and intermediate Java coding standard when writing, changing, or reviewing Java code in this project, including tests.
---

# SE-EDU Java coding standard

Read and follow the [basic and intermediate rules](https://se-education.org/guides/conventions/java/intermediate.html). Use the linked Google Java guide for uncovered topics.

## Review checklist

- Use lowercase packages, PascalCase type nouns, camelCase method verbs and variables, and uppercase underscore-separated constants. Use descriptive English names, boolean predicates, plural collections, and normally capitalized acronyms. Test names may use `feature_scenario_expectedBehavior`.
- Indent with four spaces; continuations add eight. Aim below 110 columns; never exceed 120. Wrap after commas or before operators. Keep method names attached to `(`.
- Use K&R braces, including single-statement branches and loops. Join `else`, `catch`, `finally`, and do-while endings to closing braces. Indent switch labels four spaces; mark intentional fallthrough.
- Space operators and comma-separated arguments; separate logical groups with blank lines.
- Use packages and consistent explicit imports. Remove unused imports. Attach array brackets to types. Initialize variables in the smallest practical scope. Encapsulate nonconstant fields except behaviorless data classes.
- Write English comments with American spelling. Document classes and public methods with Javadoc; apply the guide's getter/setter, override, and test exceptions. Explain behavior, parameters, returns, and exceptions where useful.

## Project workflow

Inspect existing changes before editing. Preserve behavior during style cleanup. Follow AGENTS.md's additional documentation requirements, including comments on test classes and nontrivial private members.

Check config/checkstyle/checkstyle.xml against the guide if they conflict; do not suppress violations to make a build pass. Automated checks do not cover every naming or documentation rule, so also review the changed code manually.

Verify Java 25 using the Gradle wrapper's --version output. Run ./gradlew check in Git Bash or .\gradlew.bat check in PowerShell. This runs both tests and Checkstyle; test alone is insufficient. Report failures or inability to run checks honestly.

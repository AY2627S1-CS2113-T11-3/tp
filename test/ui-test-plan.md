# UI Test Plan

This file is the source of truth for repeatable console UI test cases run with the `test-ui` skill.

## Test environment

- Java: 25
- Build command: `gradlew.bat shadowJar` on Windows or `./gradlew shadowJar` on macOS/Linux
- Run command: `java -jar build/libs/duke.jar`
- Comparison: exact after newline normalization, ANSI-sequence removal, and ignoring one final newline
- Isolation: each test case starts a fresh application process

## Test cases

No test cases have been recorded yet. Replace this line with cases supplied for a test session. Each case must specify its aim, preconditions, and ordered input/expected-output steps.

<!--
### UI-001: Short title

**Aim:** Describe the behavior being verified.

**Preconditions:** None.

#### Step 1

**Input**

```text
command
```

**Expected output**

```text
exact output
```
-->

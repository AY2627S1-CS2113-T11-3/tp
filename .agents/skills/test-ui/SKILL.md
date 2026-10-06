---
name: test-ui
description: Run command-by-command text UI tests for this project's console application, record cases in test/ui-test-plan.md, compare actual output exactly, and stop at the first mismatch. Use when the user supplies console inputs with expected outputs or asks to execute the recorded UI test plan.
---

# Test UI

Test the console application interactively and leave both a reusable plan and a readable session record.

## Test case format

Treat each supplied command and expected output as one ordered step. Each test case must contain:

- a stable ID and short title;
- an aim;
- any setup or preconditions that affect the result;
- an ordered list of input/expected-output pairs.

Require one expected output for every command. Use an empty fenced block, labelled as no output, when a command should produce nothing. If an input or expected output is ambiguous, resolve that ambiguity before running the affected case.

Before testing, create or update `test/ui-test-plan.md`. Preserve unrelated existing cases. Record all supplied cases there using this shape:

````markdown
### UI-001: Short title

**Aim:** What behavior this verifies.

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
````

Use additional numbered steps for additional commands. Never replace a user's expected value with the program's actual output merely to make a test pass.

## Run the tests

1. Confirm that `java --version` reports Java 25. Stop and report the version mismatch if it does not.
2. Build the executable with `gradlew.bat shadowJar` on Windows or `./gradlew shadowJar` on macOS/Linux. Stop if the build fails.
3. Start a fresh `java -jar build/libs/duke.jar` process for each test case so cases do not share in-memory state. Honor any recorded file or other external-state preconditions.
4. Capture startup output. Then send one command at a time, wait until the application's response settles, and capture the response produced by that command before sending the next command.
5. Compare the captured response with that step's expected output. Normalize `CRLF` and `CR` to `LF`, strip ANSI terminal control sequences, and ignore one final newline on either side. Otherwise compare exactly, including blank lines and spaces. Exclude terminal input echo and test-harness messages from the response being compared.
6. If the comparison passes, continue to the next step. If it fails, terminate the process immediately and do not run any remaining steps or test cases.
7. After the final passing step, close the application normally. For this project, send `Bye` as a clearly labelled harness-cleanup input if the listed commands did not already exit the program; do not compare cleanup output.

Do not edit application code or expected outputs during a test run. A build error, crash, timeout, unexpected early exit, or missing output is a failed test.

## Report the session

Always show a chronological console transcript after execution, including startup output, every user/test input, application output, cleanup input, and process exit. Use clear prefixes so terminal echo is not confused with application output, for example:

```text
[app] What is your name?
[input] Alex
[app] Hello Alex, welcome to Bob!
```

Finish with a compact result for each executed test and step. On the first failure, identify the test and command, then show separate fenced blocks for the exact expected and actual outputs. State that later steps and cases were not run. On full success, state how many test cases and commands passed and link to `test/ui-test-plan.md`.

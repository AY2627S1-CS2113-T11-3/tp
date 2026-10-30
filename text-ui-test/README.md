# Text UI tests

Use Java 25. From the repository root on macOS/Linux:

```sh
./gradlew check shadowJar
sh text-ui-test/runtest.sh
```

On Windows:

```bat
gradlew.bat check shadowJar
text-ui-test\runtest.bat
```

`input.txt` contains the name, commands, and `Bye` input. `EXPECTED.txt` contains
the expected console output. The script feeds the input into Bob, saves output
to `build/text-ui-test/ACTUAL.txt`, and compares the files. Differences or an
application error make the test fail. Java diagnostic logs remain on stderr.

The first 11 commands follow case UI-PREP-001 in `test/ui-test-plan.md`: create a
session, add stock, calculate preparation quantities, report shortages, remove
a preparation item, and refresh shortages after stock changes. The next four commands
record a loan, list it, refresh preparation shortages, and verify borrower isolation from
supplier contacts, following UI-LOAN-ADD-006. Three further commands delete the incorrect
loan, list the remaining loans, and refresh preparation availability, following UI-LOAN-DELETE-005.
Four more commands add another loan, record its full return, list loans, and refresh preparation,
following UI-LOAN-RETURN-004. Five further commands add a historical backlog loan, show
its marker in both list views, return it, and verify the overdue list is empty, covering
UI-LOAN-OVERDUE-001 and UI-LOAN-OVERDUE-002. Lookup commands then check matching and invalid input.
Nine further commands cover UI-BROKEN-REGRESSION-001: report three broken Wire,
check the report and remaining stock, reject an excessive report without changing either,
and delete the original report to restore all ten usable Wire. The fixed report date,
1 January 2000, remains valid on later runs. Twelve further commands cover
UI-REQUEST-FULFILLMENT-001: reject insufficient stock, fulfill across two categories,
reject repeated fulfillment and reversal, retain shortage warnings only for outstanding requests,
and delete a fulfilled request without restoring stock. The final
command is `help`, following case UI-HELP-001. The fixed future
date, 31 December 2099, avoids a date-substitution helper. Update both files if
that date is no longer valid.

To run the complete Markdown plan command by command with Java assertions enabled on Windows:

```powershell
powershell -File scripts/run-ui-tests.ps1
```

The runner starts a fresh process per case, checks startup and every response, and stops on the
first mismatch, timeout, abnormal exit, or SEVERE diagnostic. It writes a chronological transcript to
`build/ui-plan/transcript.txt`. Build the JAR first; this runner does not rebuild it.

When adding functionality, extend `input.txt` and `EXPECTED.txt` together with
the relevant test-plan case. Write expected output from the intended behavior;
do not replace it with actual output just to make a failing test pass.

CI builds the app and runs this comparison on each push and pull request.

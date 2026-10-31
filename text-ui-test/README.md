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

Each run uses a new working directory under `build/text-ui-test` so automatic saving cannot load or overwrite
real records, or leak state into the next test run. These temporary directories are retained for inspection.

On Windows, the command-by-command runner in `scripts/run-ui-tests.ps1` reads the full plan, starts a fresh
process per case, compares startup/greeting and each response exactly, and stops at the first mismatch:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/run-ui-tests.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/run-ui-tests.ps1 -EnableAssertions
```

The execution-policy option applies only to that test process. Use `-CasePrefix UI-PERSIST-` for the persistence
cases. Cases share storage only when their recorded `Storage group` matches; run grouped cases together and in
plan order. Fixtures and writes are confined to a fresh directory under `build/ui-tests-*`. The persistence phase
transcripts are saved in `test/ui-persistence-phase3-session.md` through `test/ui-persistence-phase6-session.md`.

The first 11 commands follow case UI-PREP-001 in `test/ui-test-plan.md`: create a
session, add stock, calculate preparation quantities, report shortages, remove
a preparation item, and refresh shortages after stock changes. The next four commands
record a loan, list it, refresh preparation shortages, and verify borrower isolation from
supplier contacts, following UI-LOAN-ADD-006. Three further commands delete the incorrect
loan, list the remaining loans, and refresh preparation availability, following UI-LOAN-DELETE-005.
Four more commands add another loan, record its full return, list loans, and refresh preparation,
following UI-LOAN-RETURN-004. Five further commands add a historical backlog loan, show
its marker in both list views, return it, and verify the overdue list is empty, covering
UI-LOAN-OVERDUE-001 and UI-LOAN-OVERDUE-002. The final
command is `help`, following case UI-HELP-001. The fixed future
date, 31 December 2099, avoids a date-substitution helper. Update both files if
that date is no longer valid. The other cases remain in the manual test plan.

When adding functionality, extend `input.txt` and `EXPECTED.txt` together with
the relevant test-plan case. Write expected output from the intended behavior;
do not replace it with actual output just to make a failing test pass.

CI builds the app and runs this comparison on each push and pull request.

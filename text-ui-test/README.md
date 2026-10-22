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
loan, list the remaining loans, and refresh preparation availability, following UI-LOAN-DELETE-005. The final
command is `help`, following case UI-HELP-001. The fixed future
date, 31 December 2099, avoids a date-substitution helper. Update both files if
that date is no longer valid. The other cases remain in the manual test plan.

When adding functionality, extend `input.txt` and `EXPECTED.txt` together with
the relevant test-plan case. Write expected output from the intended behavior;
do not replace it with actual output just to make a failing test pass.

CI builds the app and runs this comparison on each push and pull request.

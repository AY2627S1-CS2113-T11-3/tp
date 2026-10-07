# Date-format message regression session

Java 25.0.3; command-by-command exact comparison against `test/ui-test-plan.md`.
Fresh process for each case; stop on the first mismatch.
Trailing spaces in application output are shown as `␠`.

```text
[java] openjdk 25.0.3 2026-04-21 LTS
[date] TODAY=7 October 2026
[date] YESTERDAY=6 October 2026
[date] TOMORROW=8 October 2026
[case] UI-SESSION-ADD-001: Reject malformed session commands
[app]   ____        _␠␠␠␠␠
[app]  |  _ \      | |␠␠␠␠
[app]  | |_) | ___ | |__␠␠
[app]  |  _ < / _ \| '_ \␠
[app]  | |_) | (_) | |_) |
[app]  |____/ \___/|_.__/␠
[app]
[app] What is your name?
[input] Alex
[app] Hello Alex, welcome to Bob!
[app] --------------------------------------------------------------------
[input] add n/Test Lab d/8 October 2026 l/Room-A s/0900 e/1000 p/20
[app] Invalid command
[app]
[PASS] UI-SESSION-ADD-001 step 1
[input] add-s
[app] Invalid format. Use: add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT
[app]
[PASS] UI-SESSION-ADD-001 step 2
[input] add-s n/Test Lab d/8 October 2026 s/0900 e/1000 p/20
[app] Invalid format. Use: add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT
[app]
[PASS] UI-SESSION-ADD-001 step 3
[input] add-s d/8 October 2026 n/Test Lab l/Room-A s/0900 e/1000 p/20
[app] Invalid format. Use: add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT
[app]
[PASS] UI-SESSION-ADD-001 step 4
[input] add-s n/Test Lab n/Other Lab d/8 October 2026 l/Room-A s/0900 e/1000 p/20
[app] Invalid format. Use: add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT
[app]
[PASS] UI-SESSION-ADD-001 step 5
[input] add-s n/ d/8 October 2026 l/Room-A s/0900 e/1000 p/20
[app] Session name cannot be blank.
[app]
[PASS] UI-SESSION-ADD-001 step 6
[input] add-s n/Test Lab d/8 October 2026 l/ s/0900 e/1000 p/20
[app] Session location cannot be blank.
[app]
[PASS] UI-SESSION-ADD-001 step 7
[input] add-s n/Test Lab d/ l/Room-A s/0900 e/1000 p/20
[app] Date must be a valid date in d MMMM yyyy format.
[app]
[PASS] UI-SESSION-ADD-001 step 8
[input] add-s n/Test Lab d/8 October 2026 l/Room-A s/ e/1000 p/20
[app] Start time must be a valid time in HHmm format.
[app]
[PASS] UI-SESSION-ADD-001 step 9
[input] add-s n/Test Lab d/8 October 2026 l/Room-A s/0900 e/1000 p/20 x/extra
[app] Invalid format. Use: add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT
[app]
[PASS] UI-SESSION-ADD-001 step 10
[cleanup] Bye
[process] Exited with code 0
[case] UI-SESSION-ADD-002: Validate session dates and times
[app]   ____        _␠␠␠␠␠
[app]  |  _ \      | |␠␠␠␠
[app]  | |_) | ___ | |__␠␠
[app]  |  _ < / _ \| '_ \␠
[app]  | |_) | (_) | |_) |
[app]  |____/ \___/|_.__/␠
[app]
[app] What is your name?
[input] Alex
[app] Hello Alex, welcome to Bob!
[app] --------------------------------------------------------------------
[input] add-s n/Test Lab d/31 February 2099 l/Room-A s/0900 e/1000 p/20
[app] Date must be a valid date in d MMMM yyyy format.
[app]
[PASS] UI-SESSION-ADD-002 step 1
[input] add-s n/Test Lab d/2099-01-01 l/Room-A s/0900 e/1000 p/20
[app] Date must be a valid date in d MMMM yyyy format.
[app]
[PASS] UI-SESSION-ADD-002 step 2
[input] add-s n/Test Lab d/6 October 2026 l/Room-A s/0900 e/1000 p/20
[app] Session date cannot be before today.
[app]
[PASS] UI-SESSION-ADD-002 step 3
[input] add-s n/Test Lab d/8 October 2026 l/Room-A s/900 e/1000 p/20
[app] Start time must be a valid time in HHmm format.
[app]
[PASS] UI-SESSION-ADD-002 step 4
[input] add-s n/Test Lab d/8 October 2026 l/Room-A s/0900 e/2400 p/20
[app] End time must be a valid time in HHmm format.
[app]
[PASS] UI-SESSION-ADD-002 step 5
[input] add-s n/Test Lab d/8 October 2026 l/Room-A s/0900 e/0900 p/20
[app] Start time must be earlier than end time.
[app]
[PASS] UI-SESSION-ADD-002 step 6
[input] add-s n/Test Lab d/8 October 2026 l/Room-A s/1000 e/0900 p/20
[app] Start time must be earlier than end time.
[app]
[PASS] UI-SESSION-ADD-002 step 7
[input] add-s n/Today Lab d/7 October 2026 l/Room-A s/0900 e/1000 p/20
[app] Successfully added: Today Lab on 7 October 2026 at Room-A from 0900 to 1000 for 20 attendees
[app]
[PASS] UI-SESSION-ADD-002 step 8
[cleanup] Bye
[process] Exited with code 0
[result] PASS: 2 test cases and 18 commands
```

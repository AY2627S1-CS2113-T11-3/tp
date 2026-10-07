# Console regression session after Feature 4

Java 25.0.3; command-by-command exact comparison against `test/ui-test-plan.md`.
Fresh process for each case; stop on the first mismatch.
Trailing spaces in application output are shown as `␠`.

```text
[java] openjdk 25.0.3 2026-04-21 LTS
[date] TODAY=7 October 2026
[date] YESTERDAY=6 October 2026
[date] TOMORROW=8 October 2026
[case] UI-ADD-001: Reject malformed add commands
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
[input] add n/Wire c/Components
[app] Invalid command
[app]
[PASS] UI-ADD-001 step 1
[input] add-i c/Components q/10
[app] Invalid format. Use: add-i n/NAME c/CATEGORY [q/QUANTITY]
[app]
[PASS] UI-ADD-001 step 2
[input] add-i n/Wire q/10 c/Components
[app] Invalid format. Use: add-i n/NAME c/CATEGORY [q/QUANTITY]
[app]
[PASS] UI-ADD-001 step 3
[input] add-i n/Wire n/Cable c/Components q/10
[app] Invalid format. Use: add-i n/NAME c/CATEGORY [q/QUANTITY]
[app]
[PASS] UI-ADD-001 step 4
[input] add-i n/ c/Components q/10
[app] Item name cannot be blank.
[app]
[PASS] UI-ADD-001 step 5
[input] add-i n/Wire c/ q/10
[app] Category cannot be blank.
[app]
[PASS] UI-ADD-001 step 6
[input] add-i n/Wire c/Components x/extra
[app] Invalid format. Use: add-i n/NAME c/CATEGORY [q/QUANTITY]
[app]
[PASS] UI-ADD-001 step 7
[cleanup] Bye
[process] Exited with code 0
[case] UI-ADD-002: Reject invalid add quantities
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
[input] add-i n/Wire c/Components q/abc
[app] Quantity must be a positive integer.
[app]
[PASS] UI-ADD-002 step 1
[input] add-i n/Wire c/Components q/
[app] Quantity must be a positive integer.
[app]
[PASS] UI-ADD-002 step 2
[input] add-i n/Wire c/Components q/0
[app] Quantity must be a positive integer.
[app]
[PASS] UI-ADD-002 step 3
[input] add-i n/Wire c/Components q/-5
[app] Quantity must be a positive integer.
[app]
[PASS] UI-ADD-002 step 4
[input] add-i n/Wire c/Components q/2147483648
[app] Quantity must be a positive integer.
[app]
[PASS] UI-ADD-002 step 5
[cleanup] Bye
[process] Exited with code 0
[case] UI-ADD-003: Add valid items and reject a duplicate
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
[input] add-i n/Oscilloscope c/Equipment
[app] Successfully added: 1x Oscilloscope to the inventory
[app]
[PASS] UI-ADD-003 step 1
[input] add-i n/10k Resistor c/Components q/500
[app] Successfully added: 500x 10k Resistor to the inventory
[app]
[PASS] UI-ADD-003 step 2
[input] add-i n/oscilloscope c/equipment q/2
[app] Item "oscilloscope" already exists in category "Equipment".
[app]
[PASS] UI-ADD-003 step 3
[cleanup] Bye
[process] Exited with code 0
[case] UI-DELETE-001: Reject malformed delete commands
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
[input] delete-i
[app] Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY
[app]
[PASS] UI-DELETE-001 step 1
[input] delete-i c/Components q/1
[app] Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY
[app]
[PASS] UI-DELETE-001 step 2
[input] delete-i i/1 c/Components q/1
[app] Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY
[app]
[PASS] UI-DELETE-001 step 3
[input] delete-i c/Components i/1 i/2 q/1
[app] Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY
[app]
[PASS] UI-DELETE-001 step 4
[input] delete-i c/ i/1 q/1
[app] Category cannot be blank.
[app]
[PASS] UI-DELETE-001 step 5
[input] delete-i c/Components i/1 q/1 x/extra
[app] Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY
[app]
[PASS] UI-DELETE-001 step 6
[cleanup] Bye
[process] Exited with code 0
[case] UI-DELETE-002: Reject invalid delete numbers
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
[input] delete-i c/Components i/abc q/1
[app] Item index must be a positive integer.
[app]
[PASS] UI-DELETE-002 step 1
[input] delete-i c/Components i/0 q/1
[app] Item index must be a positive integer.
[app]
[PASS] UI-DELETE-002 step 2
[input] delete-i c/Components i/2147483648 q/1
[app] Item index must be a positive integer.
[app]
[PASS] UI-DELETE-002 step 3
[input] delete-i c/Components i/1 q/0
[app] Quantity must be a positive integer.
[app]
[PASS] UI-DELETE-002 step 4
[input] delete-i c/Components i/-1 q/1
[app] Item index must be a positive integer.
[app]
[PASS] UI-DELETE-002 step 5
[input] delete-i c/Components i/ q/1
[app] Item index must be a positive integer.
[app]
[PASS] UI-DELETE-002 step 6
[input] delete-i c/Components i/1 q/abc
[app] Quantity must be a positive integer.
[app]
[PASS] UI-DELETE-002 step 7
[input] delete-i c/Components i/1 q/
[app] Quantity must be a positive integer.
[app]
[PASS] UI-DELETE-002 step 8
[input] delete-i c/Components i/1 q/-5
[app] Quantity must be a positive integer.
[app]
[PASS] UI-DELETE-002 step 9
[cleanup] Bye
[process] Exited with code 0
[case] UI-DELETE-003: Handle inventory deletion rules
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
[input] add-i n/Wire c/Components q/10
[app] Successfully added: 10x Wire to the inventory
[app]
[PASS] UI-DELETE-003 step 1
[input] delete-i c/Equipment i/1 q/1
[app] Category "Equipment" does not exist.
[app]
[PASS] UI-DELETE-003 step 2
[input] delete-i c/components i/2 q/1
[app] Item index 2 is invalid. Category "Components" contains 1 item(s).
[app]
[PASS] UI-DELETE-003 step 3
[input] delete-i c/components i/1 q/11
[app] Cannot remove 11 Wire. Only 10 are available.
[app]
[PASS] UI-DELETE-003 step 4
[input] delete-i c/components i/1 q/4
[app] Successfully removed: 4x Wire from the inventory
[app]
[PASS] UI-DELETE-003 step 5
[input] delete-i c/Components i/1 q/6
[app] Successfully removed: 6x Wire from the inventory
[app]
[PASS] UI-DELETE-003 step 6
[input] delete-i c/Components i/1 q/1
[app] Category "Components" does not exist.
[app]
[PASS] UI-DELETE-003 step 7
[cleanup] Bye
[process] Exited with code 0
[case] UI-LIST-001: Validate list syntax and empty output
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
[input] list-i extra
[app] Invalid format. Use: list-i
[app]
[PASS] UI-LIST-001 step 1
[input] list-i
[app] --------------------------------------------------------------------
[app] Inventory
[app] --------------------------------------------------------------------
[app] No items in inventory.
[app] --------------------------------------------------------------------
[app]
[PASS] UI-LIST-001 step 2
[cleanup] Bye
[process] Exited with code 0
[case] UI-LIST-002: List items deterministically
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
[input] add-i n/Oscilloscope c/Equipment
[app] Successfully added: 1x Oscilloscope to the inventory
[app]
[PASS] UI-LIST-002 step 1
[input] add-i n/Wire c/Components q/20
[app] Successfully added: 20x Wire to the inventory
[app]
[PASS] UI-LIST-002 step 2
[input] add-i n/Red LED c/components q/30
[app] Successfully added: 30x Red LED to the inventory
[app]
[PASS] UI-LIST-002 step 3
[input] list-i
[app] --------------------------------------------------------------------
[app] Inventory
[app] --------------------------------------------------------------------
[app] Equipment
[app] 1: Oscilloscope (Qty: 1)
[app] Components
[app] 1: Wire (Qty: 20)
[app] 2: Red LED (Qty: 30)
[app] --------------------------------------------------------------------
[app]
[PASS] UI-LIST-002 step 4
[cleanup] Bye
[process] Exited with code 0
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
[app] Date must be a valid date in d MMMM uuuu format.
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
[app] Date must be a valid date in d MMMM uuuu format.
[app]
[PASS] UI-SESSION-ADD-002 step 1
[input] add-s n/Test Lab d/2099-01-01 l/Room-A s/0900 e/1000 p/20
[app] Date must be a valid date in d MMMM uuuu format.
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
[case] UI-SESSION-ADD-003: Validate session headcounts
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
[input] add-s n/Test Lab d/8 October 2026 l/Room-A s/0900 e/1000 p/
[app] Headcount must be a positive integer.
[app]
[PASS] UI-SESSION-ADD-003 step 1
[input] add-s n/Test Lab d/8 October 2026 l/Room-A s/0900 e/1000 p/abc
[app] Headcount must be a positive integer.
[app]
[PASS] UI-SESSION-ADD-003 step 2
[input] add-s n/Test Lab d/8 October 2026 l/Room-A s/0900 e/1000 p/0
[app] Headcount must be a positive integer.
[app]
[PASS] UI-SESSION-ADD-003 step 3
[input] add-s n/Test Lab d/8 October 2026 l/Room-A s/0900 e/1000 p/-1
[app] Headcount must be a positive integer.
[app]
[PASS] UI-SESSION-ADD-003 step 4
[input] add-s n/Test Lab d/8 October 2026 l/Room-A s/0900 e/1000 p/2147483648
[app] Headcount must be a positive integer.
[app]
[PASS] UI-SESSION-ADD-003 step 5
[cleanup] Bye
[process] Exited with code 0
[case] UI-SESSION-ADD-004: Enforce session conflicts
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
[input] add-s n/CG2111A Lab [03] d/8 October 2026 l/E4A-04-08 s/0900 e/1200 p/50
[app] Successfully added: CG2111A Lab [03] on 8 October 2026 at E4A-04-08 from 0900 to 1200 for 50 attendees
[app]
[PASS] UI-SESSION-ADD-004 step 1
[input] add-s n/Overlapping Lab d/8 October 2026 l/e4a-04-08 s/1100 e/1300 p/25
[app] Session conflicts with "CG2111A Lab [03]" at E4A-04-08 on 8 October 2026 from 0900 to 1200.
[app]
[PASS] UI-SESSION-ADD-004 step 2
[input] add-s n/Adjacent Lab d/8 October 2026 l/E4A-04-08 s/1200 e/1400 p/25
[app] Successfully added: Adjacent Lab on 8 October 2026 at E4A-04-08 from 1200 to 1400 for 25 attendees
[app]
[PASS] UI-SESSION-ADD-004 step 3
[input] add-s n/Other Room Lab d/8 October 2026 l/E4A-04-09 s/1000 e/1100 p/25
[app] Successfully added: Other Room Lab on 8 October 2026 at E4A-04-09 from 1000 to 1100 for 25 attendees
[app]
[PASS] UI-SESSION-ADD-004 step 4
[input] add-s n/Different Date Lab d/1 January 2099 l/E4A-04-08 s/1000 e/1100 p/25
[app] Successfully added: Different Date Lab on 1 January 2099 at E4A-04-08 from 1000 to 1100 for 25 attendees
[app]
[PASS] UI-SESSION-ADD-004 step 5
[cleanup] Bye
[process] Exited with code 0
[case] UI-SESSION-DELETE-001: Reject invalid session deletions
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
[input] delete-s
[app] Invalid format. Use: delete-s INDEX
[app]
[PASS] UI-SESSION-DELETE-001 step 1
[input] delete-s 1 extra
[app] Invalid format. Use: delete-s INDEX
[app]
[PASS] UI-SESSION-DELETE-001 step 2
[input] delete-s abc
[app] Session index must be a positive integer.
[app]
[PASS] UI-SESSION-DELETE-001 step 3
[input] delete-s 0
[app] Session index must be a positive integer.
[app]
[PASS] UI-SESSION-DELETE-001 step 4
[input] delete-s -1
[app] Session index must be a positive integer.
[app]
[PASS] UI-SESSION-DELETE-001 step 5
[input] delete-s 2147483648
[app] Session index must be a positive integer.
[app]
[PASS] UI-SESSION-DELETE-001 step 6
[input] delete-s 1
[app] Session index 1 is invalid. There are 0 scheduled session(s).
[app]
[PASS] UI-SESSION-DELETE-001 step 7
[cleanup] Bye
[process] Exited with code 0
[case] UI-SESSION-DELETE-002: Delete sessions and shift indices
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
[input] add-s n/First Lab d/8 October 2026 l/Room-A s/0900 e/1000 p/20
[app] Successfully added: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 20 attendees
[app]
[PASS] UI-SESSION-DELETE-002 step 1
[input] add-s n/Second Lab d/8 October 2026 l/Room-A s/1000 e/1100 p/25
[app] Successfully added: Second Lab on 8 October 2026 at Room-A from 1000 to 1100 for 25 attendees
[app]
[PASS] UI-SESSION-DELETE-002 step 2
[input] delete-s 1
[app] Successfully removed session: First Lab
[app]
[PASS] UI-SESSION-DELETE-002 step 3
[input] delete-s 1
[app] Successfully removed session: Second Lab
[app]
[PASS] UI-SESSION-DELETE-002 step 4
[input] delete-s 1
[app] Session index 1 is invalid. There are 0 scheduled session(s).
[app]
[PASS] UI-SESSION-DELETE-002 step 5
[cleanup] Bye
[process] Exited with code 0
[case] UI-SESSION-LIST-001: Reject arguments and list an empty schedule
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
[input] list-s extra
[app] Invalid format. Use: list-s
[app]
[PASS] UI-SESSION-LIST-001 step 1
[input] list-s
[app] --------------------------------------------------------------------
[app] Sessions
[app] --------------------------------------------------------------------
[app] No sessions scheduled.
[app] --------------------------------------------------------------------
[app]
[PASS] UI-SESSION-LIST-001 step 2
[cleanup] Bye
[process] Exited with code 0
[case] UI-SESSION-LIST-002: List sessions in full format and insertion order
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
[input] add-s n/First Lab d/8 October 2026 l/Room-A s/0900 e/1000 p/20
[app] Successfully added: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 20 attendees
[app]
[PASS] UI-SESSION-LIST-002 step 1
[input] add-s n/Second Lab d/8 October 2026 l/Room-B s/0930 e/1030 p/25
[app] Successfully added: Second Lab on 8 October 2026 at Room-B from 0930 to 1030 for 25 attendees
[app]
[PASS] UI-SESSION-LIST-002 step 2
[input] list-s
[app] --------------------------------------------------------------------
[app] Sessions
[app] --------------------------------------------------------------------
[app] 1. First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 20 attendees
[app] 2. Second Lab on 8 October 2026 at Room-B from 0930 to 1030 for 25 attendees
[app] --------------------------------------------------------------------
[app]
[PASS] UI-SESSION-LIST-002 step 3
[cleanup] Bye
[process] Exited with code 0
[case] UI-PREP-001: Calculate requirements and refresh stock availability
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
[input] add-s n/First Lab d/8 October 2026 l/Room-A s/0900 e/1000 p/10
[app] Successfully added: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app]
[PASS] UI-PREP-001 step 1
[input] list-p s/1
[app] --------------------------------------------------------------------
[app] Lab preparation: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app] --------------------------------------------------------------------
[app] No items in preparation.
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-001 step 2
[input] add-i n/Wire c/Components q/20
[app] Successfully added: 20x Wire to the inventory
[app]
[PASS] UI-PREP-001 step 3
[input] add-i n/Red LED c/Components q/3
[app] Successfully added: 3x Red LED to the inventory
[app]
[PASS] UI-PREP-001 step 4
[input] add-p s/1 i/Wire c/Components q/2
[app] Successfully added: 20x Wire [Components] to preparation
[app] --------------------------------------------------------------------
[app] Lab preparation: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app] --------------------------------------------------------------------
[app] 1. Wire [Components] (Required: 20, Available: 20)
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-001 step 5
[input] add-p s/1 i/Red LED c/Components
[app] Successfully added: 10x Red LED [Components] to preparation
[app] --------------------------------------------------------------------
[app] Lab preparation: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app] --------------------------------------------------------------------
[app] 1. Wire [Components] (Required: 20, Available: 20)
[app] 2. Red LED [Components] (Required: 10, Available: 3) !! Insufficient items (Shortfall: 7)
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-001 step 6
[input] add-p s/1 i/Battery c/Components
[app] Successfully added: 10x Battery [Components] to preparation
[app] --------------------------------------------------------------------
[app] Lab preparation: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app] --------------------------------------------------------------------
[app] 1. Wire [Components] (Required: 20, Available: 20)
[app] 2. Red LED [Components] (Required: 10, Available: 3) !! Insufficient items (Shortfall: 7)
[app] 3. Battery [Components] (Required: 10, Available: 0) !! Insufficient items (Shortfall: 10)
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-001 step 7
[input] list-i
[app] --------------------------------------------------------------------
[app] Inventory
[app] --------------------------------------------------------------------
[app] Components
[app] 1: Wire (Qty: 20)
[app] 2: Red LED (Qty: 3)
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-001 step 8
[input] delete-p s/1 i/3 q/10
[app] Successfully removed: 10x Battery [Components] from preparation
[app] --------------------------------------------------------------------
[app] Lab preparation: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app] --------------------------------------------------------------------
[app] 1. Wire [Components] (Required: 20, Available: 20)
[app] 2. Red LED [Components] (Required: 10, Available: 3) !! Insufficient items (Shortfall: 7)
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-001 step 9
[input] delete-i c/Components i/1 q/10
[app] Successfully removed: 10x Wire from the inventory
[app]
[PASS] UI-PREP-001 step 10
[input] list-p s/1
[app] --------------------------------------------------------------------
[app] Lab preparation: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app] --------------------------------------------------------------------
[app] 1. Wire [Components] (Required: 20, Available: 10) !! Insufficient items (Shortfall: 10)
[app] 2. Red LED [Components] (Required: 10, Available: 3) !! Insufficient items (Shortfall: 7)
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-001 step 11
[cleanup] Bye
[process] Exited with code 0
[case] UI-PREP-002: Accumulate and remove preparation quantities
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
[input] add-s n/First Lab d/8 October 2026 l/Room-A s/0900 e/1000 p/10
[app] Successfully added: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app]
[PASS] UI-PREP-002 step 1
[input] add-i n/Wire c/Components q/30
[app] Successfully added: 30x Wire to the inventory
[app]
[PASS] UI-PREP-002 step 2
[input] add-p s/1 i/Wire c/Components q/2
[app] Successfully added: 20x Wire [Components] to preparation
[app] --------------------------------------------------------------------
[app] Lab preparation: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app] --------------------------------------------------------------------
[app] 1. Wire [Components] (Required: 20, Available: 30)
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-002 step 3
[input] add-p s/1 i/wIRE c/Components
[app] Successfully added: 10x Wire [Components] to preparation
[app] --------------------------------------------------------------------
[app] Lab preparation: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app] --------------------------------------------------------------------
[app] 1. Wire [Components] (Required: 30, Available: 30)
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-002 step 4
[input] delete-p s/1 i/1 q/10
[app] Successfully removed: 10x Wire [Components] from preparation
[app] --------------------------------------------------------------------
[app] Lab preparation: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app] --------------------------------------------------------------------
[app] 1. Wire [Components] (Required: 20, Available: 30)
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-002 step 5
[input] delete-p s/1 i/1 q/21
[app] Cannot remove 21x Wire [Components] from preparation; only 20 required.
[app]
[PASS] UI-PREP-002 step 6
[input] list-p s/1
[app] --------------------------------------------------------------------
[app] Lab preparation: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app] --------------------------------------------------------------------
[app] 1. Wire [Components] (Required: 20, Available: 30)
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-002 step 7
[input] delete-p s/1 i/1 q/20
[app] Successfully removed: 20x Wire [Components] from preparation
[app] --------------------------------------------------------------------
[app] Lab preparation: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app] --------------------------------------------------------------------
[app] No items in preparation.
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-002 step 8
[input] delete-p s/1 i/1 q/1
[app] Preparation item index 1 is invalid. There are 0 item(s) in this preparation.
[app]
[PASS] UI-PREP-002 step 9
[cleanup] Bye
[process] Exited with code 0
[case] UI-PREP-003: Reject invalid preparation arguments
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
[input] list-p s/1
[app] Session index 1 is invalid. There are 0 scheduled session(s).
[app]
[PASS] UI-PREP-003 step 1
[input] add-s n/First Lab d/8 October 2026 l/Room-A s/0900 e/1000 p/10
[app] Successfully added: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app]
[PASS] UI-PREP-003 step 2
[input] add-p
[app] Invalid format. Use: add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]
[app]
[PASS] UI-PREP-003 step 3
[input] add-p i/Wire s/1 c/Components
[app] Invalid format. Use: add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]
[app]
[PASS] UI-PREP-003 step 4
[input] add-p s/1 i/Wire i/Battery c/Components
[app] Invalid format. Use: add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]
[app]
[PASS] UI-PREP-003 step 5
[input] add-p s/1 i/ c/Components
[app] Item name cannot be blank.
[app]
[PASS] UI-PREP-003 step 6
[input] add-p s/0 i/Wire c/Components
[app] Session index must be a positive integer.
[app]
[PASS] UI-PREP-003 step 7
[input] add-p s/2 i/Wire c/Components
[app] Session index 2 is invalid. There are 1 scheduled session(s).
[app]
[PASS] UI-PREP-003 step 8
[input] add-p s/1 i/Wire c/Components q/0
[app] Quantity must be a positive integer.
[app]
[PASS] UI-PREP-003 step 9
[input] add-p s/1 i/Wire c/Components q/1.5
[app] Quantity must be a positive integer.
[app]
[PASS] UI-PREP-003 step 10
[input] add-p s/1 i/Wire c/Components q/2147483648
[app] Quantity must be a positive integer.
[app]
[PASS] UI-PREP-003 step 11
[input] add-p s/1 i/Wire c/Components q/2147483647
[app] Required quantity must not exceed 2147483647.
[app]
[PASS] UI-PREP-003 step 12
[input] list-p s/1
[app] --------------------------------------------------------------------
[app] Lab preparation: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app] --------------------------------------------------------------------
[app] No items in preparation.
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-003 step 13
[input] list-p
[app] Invalid format. Use: list-p s/SESSION_INDEX
[app]
[PASS] UI-PREP-003 step 14
[input] list-p s/1 q/1
[app] Invalid format. Use: list-p s/SESSION_INDEX
[app]
[PASS] UI-PREP-003 step 15
[input] delete-p s/1 i/1
[app] Invalid format. Use: delete-p s/SESSION_INDEX i/ITEM_INDEX q/TOTAL_QUANTITY
[app]
[PASS] UI-PREP-003 step 16
[input] delete-p s/1 i/0 q/1
[app] Item index must be a positive integer.
[app]
[PASS] UI-PREP-003 step 17
[input] delete-p s/1 i/1 q/-1
[app] Quantity must be a positive integer.
[app]
[PASS] UI-PREP-003 step 18
[cleanup] Bye
[process] Exited with code 0
[case] UI-PREP-004: Keep preparation attached to its session
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
[input] add-s n/First Lab d/8 October 2026 l/Room-A s/0900 e/1000 p/10
[app] Successfully added: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app]
[PASS] UI-PREP-004 step 1
[input] add-s n/Second Lab d/8 October 2026 l/Room-B s/0900 e/1000 p/5
[app] Successfully added: Second Lab on 8 October 2026 at Room-B from 0900 to 1000 for 5 attendees
[app]
[PASS] UI-PREP-004 step 2
[input] add-p s/1 i/Wire c/Components
[app] Successfully added: 10x Wire [Components] to preparation
[app] --------------------------------------------------------------------
[app] Lab preparation: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app] --------------------------------------------------------------------
[app] 1. Wire [Components] (Required: 10, Available: 0) !! Insufficient items (Shortfall: 10)
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-004 step 3
[input] list-p s/2
[app] --------------------------------------------------------------------
[app] Lab preparation: Second Lab on 8 October 2026 at Room-B from 0900 to 1000 for 5 attendees
[app] --------------------------------------------------------------------
[app] No items in preparation.
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-004 step 4
[input] delete-s 1
[app] Successfully removed session: First Lab
[app]
[PASS] UI-PREP-004 step 5
[input] list-p s/1
[app] --------------------------------------------------------------------
[app] Lab preparation: Second Lab on 8 October 2026 at Room-B from 0900 to 1000 for 5 attendees
[app] --------------------------------------------------------------------
[app] No items in preparation.
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-004 step 6
[cleanup] Bye
[process] Exited with code 0
[case] UI-PREP-005: Distinguish categories and match case-insensitively
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
[input] add-s n/First Lab d/8 October 2026 l/Room-A s/0900 e/1000 p/10
[app] Successfully added: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app]
[PASS] UI-PREP-005 step 1
[input] add-i n/Wire c/Components q/3
[app] Successfully added: 3x Wire to the inventory
[app]
[PASS] UI-PREP-005 step 2
[input] add-i n/Wire c/Spares q/100
[app] Successfully added: 100x Wire to the inventory
[app]
[PASS] UI-PREP-005 step 3
[input] add-p s/1 i/Wire
[app] Invalid format. Use: add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]
[app]
[PASS] UI-PREP-005 step 4
[input] add-p s/1 i/Wire c/
[app] Category cannot be blank.
[app]
[PASS] UI-PREP-005 step 5
[input] add-p s/1 i/Wire c/Components
[app] Successfully added: 10x Wire [Components] to preparation
[app] --------------------------------------------------------------------
[app] Lab preparation: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app] --------------------------------------------------------------------
[app] 1. Wire [Components] (Required: 10, Available: 3) !! Insufficient items (Shortfall: 7)
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-005 step 6
[input] add-p s/1 i/wire c/spares q/2
[app] Successfully added: 20x wire [spares] to preparation
[app] --------------------------------------------------------------------
[app] Lab preparation: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app] --------------------------------------------------------------------
[app] 1. Wire [Components] (Required: 10, Available: 3) !! Insufficient items (Shortfall: 7)
[app] 2. wire [spares] (Required: 20, Available: 100)
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-005 step 7
[input] list-p s/1
[app] --------------------------------------------------------------------
[app] Lab preparation: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app] --------------------------------------------------------------------
[app] 1. Wire [Components] (Required: 10, Available: 3) !! Insufficient items (Shortfall: 7)
[app] 2. wire [spares] (Required: 20, Available: 100)
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-005 step 8
[input] delete-p s/1 i/1 q/10
[app] Successfully removed: 10x Wire [Components] from preparation
[app] --------------------------------------------------------------------
[app] Lab preparation: First Lab on 8 October 2026 at Room-A from 0900 to 1000 for 10 attendees
[app] --------------------------------------------------------------------
[app] 1. wire [spares] (Required: 20, Available: 100)
[app] --------------------------------------------------------------------
[app]
[PASS] UI-PREP-005 step 9
[cleanup] Bye
[process] Exited with code 0
[result] PASS: 21 test cases and 141 commands
```

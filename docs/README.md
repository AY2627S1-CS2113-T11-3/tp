# Bob

**Bob** is a command-line application that helps university lab technicians manage inventory, schedule lab sessions, prepare session materials, maintain supplier and maintenance contacts, and track student requests, equipment loans, and broken items. It is written in Java and optimized for users who type fast, single-line commands.

## Key Features

* **Inventory Management & Quick Lookup (`add-i`, `list-i`, `find-i`, `delete-i`):** Track usable equipment by category and search across both in-stock and loaned items.
* **Lab Session Scheduling (`add-s`, `list-s`, `delete-s`):** Maintain a chronological lab schedule with automatic location and time-overlap conflict detection.
* **Lab Session Preparation (`add-p`, `list-p`, `delete-p`):** Calculate total required quantities from per-attendee requirements and flag stock shortfalls against live inventory.
* **Student Request Tracking (`add-r`, `list-r`, `mark-r`, `delete-r`):** Organize student equipment requests by deadline and monitor fulfillment status (`PENDING`, `PREPARED`, `fulfilled`).
* **Contact Management (`add-c`, `list-c`, `delete-c`):** Store supplier and maintenance contact details with phone, optional email, and role information.
* **Loan Tracking (`add-l`, `list-l`, `list-l overdue`, `return`, `delete-l`):** Record equipment loans, highlight overdue items, and restore stock upon full return or correction.
* **Broken-Item Tracking (`add-b`, `list-b`, `delete-b`):** Deduct damaged equipment from usable inventory, link reports to maintenance contacts, and restore stock once items are repaired.

## Documentation & Useful Links

* [User Guide](UserGuide.md)
* [Developer Guide](DeveloperGuide.md)
* [About Us](AboutUs.md)
* [GitHub Repository](https://github.com/AY2627S1-CS2113-T11-3/tp)

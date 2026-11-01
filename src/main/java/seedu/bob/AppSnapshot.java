//@@author DaynaG3
package seedu.bob;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable version-2 storage data, independent of managers and their mutable domain objects.
 * List constructors make defensive copies; domain validation takes place during restoration.
 */
public record AppSnapshot(int schemaVersion, List<InventoryCategory> inventory, List<SessionEntry> sessions,
        List<RequestEntry> requests, List<ContactEntry> contacts, List<LoanRecordEntry> loanRecords,
        List<BrokenItemEntry> brokenItems, long nextLoanId) {

    public static final int CURRENT_VERSION = 2;

    /** Builds a current snapshot from active-loan values with unknown recording timestamps. */
    public AppSnapshot(int schemaVersion, List<InventoryCategory> inventory, List<SessionEntry> sessions,
            List<RequestEntry> requests, List<ContactEntry> contacts, List<LoanEntry> loans) {
        this(schemaVersion, inventory, sessions, requests, contacts, loans, List.of());
    }

    /** Builds a snapshot from active-loan values while preserving broken-item reports. */
    public AppSnapshot(int schemaVersion, List<InventoryCategory> inventory, List<SessionEntry> sessions,
            List<RequestEntry> requests, List<ContactEntry> contacts, List<LoanEntry> loans,
            List<BrokenItemEntry> brokenItems) {
        this(schemaVersion, inventory, sessions, requests, contacts, importActiveLoans(loans), brokenItems,
                (long) loans.size() + 1);
    }

    /** Copies every root collection, rejecting null collections and entries. */
    public AppSnapshot {
        inventory = List.copyOf(inventory);
        sessions = List.copyOf(sessions);
        requests = List.copyOf(requests);
        contacts = List.copyOf(contacts);
        loanRecords = List.copyOf(loanRecords);
        brokenItems = List.copyOf(brokenItems);
    }

    /** Assigns deterministic IDs to active-only values supplied by legacy fixtures or migration callers. */
    private static List<LoanRecordEntry> importActiveLoans(List<LoanEntry> loans) {
        ArrayList<LoanRecordEntry> records = new ArrayList<>();
        for (LoanEntry loan : loans) {
            records.add(new LoanRecordEntry((long) records.size() + 1, loan, "ACTIVE", null, null));
        }
        return List.copyOf(records);
    }

    /** An ordered inventory category with its available items. */
    public record InventoryCategory(String category, List<InventoryEntry> items) {
        /** Copies the category's entries so subsequent list changes cannot alter the snapshot. */
        public InventoryCategory {
            items = List.copyOf(items);
        }
    }

    /** Available stock for one named item within its owning category. */
    public record InventoryEntry(String name, int quantity) {
    }

    /** A session with ISO date/time strings and its own total preparation requirements. */
    public record SessionEntry(String name, String date, String location, String startTime, String endTime,
            int headcount, List<PreparationEntry> preparation) {
        /** Copies requirements so the snapshot remains independent of its source list. */
        public SessionEntry {
            preparation = List.copyOf(preparation);
        }
    }

    /** Total required units, which must not be multiplied by headcount during restoration. */
    public record PreparationEntry(String name, String category, int quantity) {
    }

    /** A student request with an ISO deadline and its saved lowercase status. */
    public record RequestEntry(String studentName, String itemName, int quantity, String deadline, String status) {
    }

    /** A contact whose optional email and role may be null; phone numbers remain strings. */
    public record ContactEntry(String name, String phone, String email, String role) {
    }

    /**
     * Broken units with a one-based active contact index, a deleted contact's details, or neither.
     * Active indices retain identity even when two registered contacts have identical details.
     */
    public record BrokenItemEntry(String name, String category, int quantity, String faultDescription,
            String reportedDate, Integer contactIndex, ContactEntry deletedContact) {
    }

    /** Complete immutable loan history entry, with nullable imported recording time and active closure time. */
    public record LoanRecordEntry(long loanId, LoanEntry loan, String status, String recordedAt, String closedAt) {
    }

    /** Captured loan details independent of available inventory and the contact list. */
    public record LoanEntry(String name, String category, int quantity, String borrowerName,
            String borrowerPhone, String dueDate) {
    }
}

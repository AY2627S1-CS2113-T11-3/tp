package seedu.bob;

import java.util.List;

/**
 * Immutable version-1 storage data, independent of managers and their mutable domain objects.
 * List constructors make defensive copies; domain validation takes place during restoration.
 */
public record AppSnapshot(int schemaVersion, List<InventoryCategory> inventory, List<SessionEntry> sessions,
        List<RequestEntry> requests, List<ContactEntry> contacts, List<LoanEntry> loans) {

    public static final int CURRENT_VERSION = 1;

    /** Copies every root collection, rejecting null collections and entries. */
    public AppSnapshot {
        inventory = List.copyOf(inventory);
        sessions = List.copyOf(sessions);
        requests = List.copyOf(requests);
        contacts = List.copyOf(contacts);
        loans = List.copyOf(loans);
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

    /** An active loan independent of available inventory and the contact list. */
    public record LoanEntry(String name, String category, int quantity, String borrowerName,
            String borrowerPhone, String dueDate) {
    }
}

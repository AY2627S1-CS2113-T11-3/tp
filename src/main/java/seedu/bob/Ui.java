package seedu.bob;

import java.util.List;
import java.util.Objects;

/**
 * Displays loan and broken-item results and listings and provides shared constants for console output.
 */
public class Ui {

    /** Separates headings, command output, and list sections in the console. */
    public static final String DIVIDER = "--------------------------------------------------------------------";

    /** Every command and its format, grouped by feature, as displayed by {@code help}. */
    public static final String HELP_MESSAGE = String.join("\n",
            DIVIDER,
            "Available commands",
            DIVIDER,
            "Inventory",
            "  add-i n/NAME c/CATEGORY [q/QUANTITY]",
            "  delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY",
            "  list-i",
            "Lab sessions",
            "  add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT",
            "  delete-s INDEX",
            "  list-s",
            "Lab preparation",
            "  add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]",
            "  delete-p s/SESSION_INDEX i/ITEM_INDEX q/TOTAL_QUANTITY",
            "  list-p s/SESSION_INDEX",
            "Student requests",
            "  add-r n/STUDENT_NAME i/ITEM q/QUANTITY d/DEADLINE",
            "  delete-r INDEX",
            "  list-r",
            "  mark-r INDEX STATUS (prepared or fulfilled)",
            "Contacts",
            "  add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]",
            "  delete-c INDEX",
            "  list-c",
            "Loans",
            "  add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE",
            "  list-l [overdue]",
            "  delete-l INDEX",
            "  return INDEX",
            "Broken items",
            "  add-b n/NAME c/CATEGORY q/QUANTITY f/FAULT d/DATE_REPORTED [m/CONTACT_INDEX]",
            "  list-b",
            "  delete-b INDEX",
            "    Deletion restores all units: use only for full repairs or incorrect reports.",
            "    Date reported: d MMMM yyyy, today or earlier. Optional m/ selects a contact from list-c.",
            "Other",
            "  help",
            "  Bye",
            DIVIDER);

    /**
     * Creates an instance; display constants can also be accessed directly through the class.
     */
    public Ui() {
    }

    /**
     * Prints a successful loan addition and its warning if it was already overdue when recorded.
     *
     * @param result Successful addition with its captured overdue status.
     * @throws NullPointerException If result is null.
     */
    public static void showLoanAdded(LoanAdditionResult result) {
        Objects.requireNonNull(result, "Loan addition result must not be null");
        System.out.println("Successfully added loan: " + result.getLoan());
        if (result.isOverdue()) {
            System.out.println("Warning: this loan is already overdue.");
        }
    }

    /**
     * Prints a correction confirmation after an incorrect loan has been removed and stock restored.
     *
     * @param loan Removed incorrect loan.
     * @throws NullPointerException If loan is null.
     */
    public static void showLoanDeleted(Loan loan) {
        Objects.requireNonNull(loan, "Deleted loan must not be null");
        System.out.println("Successfully deleted incorrect loan: " + loan + ". Inventory restored.");
    }

    /**
     * Prints a return confirmation after all loaned units have been restored to inventory.
     *
     * @param loan Fully returned loan.
     * @throws NullPointerException If loan is null.
     */
    public static void showLoanReturned(Loan loan) {
        Objects.requireNonNull(loan, "Returned loan must not be null");
        System.out.println("Successfully returned loan: " + loan + ". Inventory restored.");
    }

    /**
     * Prints a loan listing using the positions and overdue flags supplied by the manager.
     * Does not filter rows or recalculate overdue status.
     *
     * @param entries Rows already selected by a manager query.
     * @param isOverdueOnly Whether the query selected the overdue-only view.
     * @throws NullPointerException If entries is null or contains a null row.
     */
    public static void showLoans(List<LoanListEntry> entries, boolean isOverdueOnly) {
        Objects.requireNonNull(entries, "Loan listing rows must not be null");
        System.out.println(DIVIDER);
        System.out.println(isOverdueOnly ? "Overdue loans" : "Loans");
        System.out.println(DIVIDER);
        for (LoanListEntry entry : entries) {
            System.out.printf("%d. %s%s%n", entry.getIndex(), entry.getLoan(),
                    entry.isOverdue() ? " [OVERDUE]" : "");
        }
        if (entries.isEmpty()) {
            System.out.println(isOverdueOnly ? "No overdue loans." : "No active loans.");
        }
        System.out.println(DIVIDER);
    }

    //@@author gavinlimsh
    /**
     * Confirms a report after its quantity has been removed from usable inventory.
     *
     * @param item Successfully recorded broken item.
     */
    public static void showBrokenItemAdded(BrokenItem item) {
        Objects.requireNonNull(item, "Added broken item must not be null");
        System.out.println("Successfully recorded broken item: " + item + ". Available inventory reduced.");
    }

    /**
     * Confirms deletion and explicitly states that all recorded units were restored.
     *
     * @param item Report removed after successful stock restoration.
     */
    public static void showBrokenItemDeleted(BrokenItem item) {
        Objects.requireNonNull(item, "Deleted broken item must not be null");
        System.out.println("Successfully deleted broken-item report: " + item + ". All units restored to inventory.");
    }

    /**
     * Displays reports in deletion order and resolves contact availability at display time.
     * Listing never removes reports or changes stock.
     *
     * @param manager Manager supplying current reports and contact membership checks.
     */
    public static void showBrokenItems(BrokenItemManager manager) {
        Objects.requireNonNull(manager, "Broken-item manager must not be null");
        List<BrokenItem> items = manager.getBrokenItems();
        System.out.println(DIVIDER);
        System.out.println("Broken items");
        System.out.println(DIVIDER);
        if (items.isEmpty()) {
            System.out.println("No broken items recorded.");
        }
        for (int i = 0; i < items.size(); i++) {
            BrokenItem item = items.get(i);
            Contact contact = manager.getAvailableContact(item);
            String contactDetails;
            if (contact != null) {
                contactDetails = contact.toString();
            } else if (item.getContact() == null) {
                contactDetails = "Not assigned";
            } else {
                contactDetails = "Unavailable (contact deleted)";
            }
            System.out.printf("%d. %s%n   Contact: %s%n", i + 1, item, contactDetails);
        }
        System.out.println(DIVIDER);
    }
    //@@author
}

package seedu.bob;

//@@author DaynaG3
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
//@@author
import java.util.List;
import java.util.Objects;

/**
 * Displays loan, item-lookup, and broken-item results and provides shared console constants.
 */
public class Ui {

    //@@author Lokwx
    /** Separates headings, command output, and list sections in the console. */
    public static final String DIVIDER = "--------------------------------------------------------------------";

    //@@author
    /** Every command and its format, grouped by feature, as displayed by {@code help}. */
    public static final String HELP_MESSAGE = String.join("\n",
            DIVIDER,
            "Available commands",
            DIVIDER,
            "Inventory",
            "  add-i n/NAME c/CATEGORY [q/QUANTITY]",
            "  delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY",
            "  list-i",
            "  find-i KEYWORD",
            "Lab sessions",
            "  add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT",
            "  delete-s INDEX",
            "  list-s",
            "Lab preparation",
            "  add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]",
            "  delete-p s/SESSION_INDEX i/ITEM_INDEX q/TOTAL_QUANTITY",
            "  list-p s/SESSION_INDEX",
            //@@author Lokwx
            "Student requests",
            "  add-r n/STUDENT_NAME i/ITEM q/QUANTITY d/DEADLINE",
            "  delete-r INDEX",
            "  list-r",
            "  mark-r INDEX STATUS (prepared or fulfilled)",
            //@@author
            "Contacts",
            "  add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]",
            "  delete-c INDEX",
            "  list-c",
            //@@author DaynaG3
            "Loans",
            "  add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE",
            "  list-l [overdue|history|returned]",
            "  delete-l INDEX",
            "  return INDEX",
            //@@author
            "Broken items",
            "  add-b n/NAME c/CATEGORY q/QUANTITY f/FAULT d/DATE_REPORTED [m/CONTACT_INDEX]",
            "  list-b",
            "  delete-b INDEX",
            "    Deletion restores all units: use only for full repairs or incorrect reports.",
            "Other",
            "  help",
            "  Bye",
            DIVIDER);

    //@@author DaynaG3
    /** Displays the recorded local date; full offset timestamps remain available in storage. */
    private static final DateTimeFormatter HISTORY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH);
    //@@author

    /**
     * Creates an instance; display constants can also be accessed directly through the class.
     */
    public Ui() {
    }

    //@@author DaynaG3
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

    /**
     * Displays permanent loan identities, lifecycle statuses, and recorded dates without exposing action indices.
     *
     * @param records Immutable history rows selected by the manager in creation order.
     * @param isReturnedOnly Whether the heading describes the returned-only query.
     * @throws NullPointerException If the list or a record is null.
     */
    public static void showLoanHistory(List<LoanRecord> records, boolean isReturnedOnly) {
        Objects.requireNonNull(records, "Loan history must not be null");
        System.out.println(DIVIDER);
        System.out.println(isReturnedOnly ? "Returned loans" : "Loan history");
        System.out.println(DIVIDER);
        for (LoanRecord record : records) {
            System.out.printf("Loan ID %d [%s] %s%n", record.loanId(), record.status(), record.loan());
            System.out.printf("   Recorded: %s | Closed: %s%n",
                    formatLoanDate(record.recordedAt(), "Unknown"), formatLoanDate(record.closedAt(), "-"));
        }
        if (records.isEmpty()) {
            System.out.println(isReturnedOnly ? "No returned loans." : "No loan records.");
        } else {
            System.out.println("Loan IDs are permanent. Use list-l for active indices.");
        }
        System.out.println(DIVIDER);
    }

    /** Formats the date in its original offset instead of converting it through the viewer's current timezone. */
    private static String formatLoanDate(OffsetDateTime timestamp, String absentText) {
        return timestamp == null ? absentText : timestamp.format(HISTORY_DATE_FORMAT);
    }

    //@@author J-unn-y
    /**
     * Displays quick-lookup results with one-based positions, or a message when nothing matches.
     *
     * @param keyword Keyword the user searched for.
     * @param entries Results already selected by {@link ItemFinder}.
     * @throws NullPointerException If the keyword or entries are null.
     */
    public static void showLookupResults(String keyword, List<ItemLookupEntry> entries) {
        Objects.requireNonNull(keyword, "Search keyword must not be null");
        Objects.requireNonNull(entries, "Search results must not be null");
        System.out.println(DIVIDER);
        System.out.println("Search results for \"" + keyword + "\"");
        System.out.println(DIVIDER);
        if (entries.isEmpty()) {
            System.out.println("No items match \"" + keyword + "\".");
        }
        for (int i = 0; i < entries.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, entries.get(i));
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

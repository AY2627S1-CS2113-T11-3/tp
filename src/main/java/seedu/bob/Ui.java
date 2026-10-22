package seedu.bob;

import java.util.Objects;

/**
 * Displays loan operation results and provides shared constants for console output.
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
            "Contacts",
            "  add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]",
            "  delete-c INDEX",
            "  list-c",
            "Loans",
            "  add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE",
            "  list-l [overdue]",
            "  delete-l INDEX",
            "  return INDEX",
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
}

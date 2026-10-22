package seedu.bob;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.InvalidLoanIndexException;

/**
 * Owns active loans in insertion order and coordinates inventory deductions, corrections, and full returns.
 */
public class LoanManager {

    /** Records loan diagnostics without adding messages to the normal console workflow. */
    private static final Logger LOGGER = Logger.getLogger(LoanManager.class.getName());

    private final Inventory inventory;
    /** Supplies the local date at each operation so overdue state is never cached across days. */
    private final Clock clock;
    /** Each addition is a separate loan, even when the item and borrower match an existing entry. */
    private final ArrayList<Loan> loans = new ArrayList<>();

    /**
     * Creates an empty loan list backed by the application's inventory.
     *
     * @param inventory Inventory from which new loans deduct stock.
     * @throws NullPointerException If inventory is null.
     */
    public LoanManager(Inventory inventory) {
        this(inventory, Clock.systemDefaultZone());
    }

    /**
     * Creates an empty loan list with an explicit clock for deterministic date and rollover testing.
     *
     * @param inventory Inventory from which new loans deduct stock.
     * @param clock Clock whose timezone defines the local calendar date.
     * @throws NullPointerException If inventory or clock is null.
     */
    public LoanManager(Inventory inventory, Clock clock) {
        this.inventory = Objects.requireNonNull(inventory, "Loan inventory must not be null");
        this.clock = Objects.requireNonNull(clock, "Loan clock must not be null");
        assert hasValidState() : "A new loan manager must have valid dependencies and an empty loan list";
    }

    /**
     * Deducts stock and records a validated loan, then prints one loan confirmation.
     * Warns when the accepted loan is already overdue. Rejected stock requests do not change state.
     *
     * @param loan Validated loan to record.
     * @throws CommandException If the item is missing or its available stock is insufficient.
     * @throws NullPointerException If the loan is null.
     */
    public void addLoan(Loan loan) throws CommandException {
        Objects.requireNonNull(loan, "Loan to add must not be null");
        assert hasValidState() && loan.hasValidState() : "Adding a loan requires valid records";
        LocalDate today = LocalDate.now(clock);
        int previousCount = loans.size();
        int previousStock = inventory.getAvailableQuantity(loan.getName(), loan.getCategory());
        inventory.lendItem(loan.getName(), loan.getCategory(), loan.getQuantity());
        assert inventory.getAvailableQuantity(loan.getName(), loan.getCategory())
                == (long) previousStock - loan.getQuantity() : "Lending must deduct the exact loan quantity";
        loans.add(loan);
        assert loans.size() == previousCount + 1 && loans.get(previousCount) == loan
                : "Lending must append exactly one loan";
        assert hasValidState() : "Loan records must remain valid after addition";
        LOGGER.fine(() -> "Recorded loan: " + loan.getName() + " in category " + loan.getCategory()
                + ", quantity=" + loan.getQuantity() + ", due=" + loan.getDueDate()
                + ", overdue=" + loan.isOverdue(today) + ", active=" + loans.size());
        System.out.println("Successfully added loan: " + loan);
        if (loan.isOverdue(today)) {
            System.out.println("Warning: this loan is already overdue.");
        }
    }

    /**
     * Removes an incorrect loan and restores all of its units to available inventory.
     * Restores stock before removing the record so a failed restoration preserves the loan.
     *
     * @param userIndex One-based loan position from the latest loan list.
     * @throws CommandException If the index is invalid or stock restoration would overflow.
     */
    public void deleteLoan(int userIndex) throws CommandException {
        Loan loan = removeLoanAndRestoreStock(userIndex);
        LOGGER.fine(() -> "Deleted incorrect loan: index=" + userIndex + ", item=" + loan.getName()
                + ", category=" + loan.getCategory() + ", quantity=" + loan.getQuantity()
                + ", active=" + loans.size());
        System.out.println("Successfully deleted incorrect loan: " + loan + ". Inventory restored.");
    }

    /**
     * Records a full return by restoring all units and removing the active loan without retaining history.
     *
     * @param userIndex One-based loan position from the latest loan list.
     * @throws CommandException If the index is invalid or stock restoration would overflow.
     */
    public void returnLoan(int userIndex) throws CommandException {
        Loan loan = removeLoanAndRestoreStock(userIndex);
        LOGGER.fine(() -> "Returned loan: index=" + userIndex + ", item=" + loan.getName()
                + ", category=" + loan.getCategory() + ", quantity=" + loan.getQuantity()
                + ", active=" + loans.size());
        System.out.println("Successfully returned loan: " + loan + ". Inventory restored.");
    }

    /**
     * Restores stock before removing a loan so failed returns and corrections preserve both lists.
     *
     * @return Removed loan, for the operation-specific confirmation.
     * @throws CommandException If the index is invalid or stock restoration would overflow.
     */
    private Loan removeLoanAndRestoreStock(int userIndex) throws CommandException {
        assert hasValidState() : "Removing a loan requires valid manager state";
        if (userIndex <= 0 || userIndex > loans.size()) {
            LOGGER.fine(() -> "Rejected loan removal: index=" + userIndex + ", active=" + loans.size());
            throw new InvalidLoanIndexException(userIndex, loans.size());
        }
        int index = userIndex - 1;
        Loan loan = loans.get(index);
        int previousCount = loans.size();
        int previousStock = inventory.getAvailableQuantity(loan.getName(), loan.getCategory());
        inventory.restoreStock(loan.getName(), loan.getCategory(), loan.getQuantity());
        assert inventory.getAvailableQuantity(loan.getName(), loan.getCategory())
                == (long) previousStock + loan.getQuantity() : "Removal must restore the exact loan quantity";
        Loan removedLoan = loans.remove(index);
        assert removedLoan == loan && loans.size() == previousCount - 1
                : "Removal must delete exactly the selected loan";
        assert hasValidState() : "Loan records must remain valid after removal";
        return loan;
    }

    /**
     * Checks active record invariants without imposing uniqueness or future-date restrictions.
     */
    private boolean hasValidState() {
        if (inventory == null || clock == null) {
            return false;
        }
        for (Loan loan : loans) {
            if (loan == null || !loan.hasValidState()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns an immutable snapshot of active loans in their displayed order.
     *
     * @return Active loan records.
     */
    public List<Loan> getLoans() {
        assert hasValidState() : "Loan snapshots require valid manager state";
        return List.copyOf(loans);
    }

    /**
     * Prints active loans with one-based positions and markers for overdue records.
     */
    public void listLoans() {
        printLoans(false);
    }

    /**
     * Prints only overdue loans, preserving their indices from the full active loan list.
     */
    public void listOverdueLoans() {
        printLoans(true);
    }

    /**
     * Displays loans against one current local date without mutating stock or loan records.
     */
    private void printLoans(boolean isOverdueOnly) {
        assert hasValidState() : "Listing requires valid loan records";
        LocalDate today = LocalDate.now(clock);
        LOGGER.fine(() -> "Listing loans: overdueOnly=" + isOverdueOnly + ", active=" + loans.size()
                + ", date=" + today);
        System.out.println(Ui.DIVIDER);
        System.out.println(isOverdueOnly ? "Overdue loans" : "Loans");
        System.out.println(Ui.DIVIDER);
        boolean hasDisplayedLoans = false;
        for (int i = 0; i < loans.size(); i++) {
            Loan loan = loans.get(i);
            boolean isOverdue = loan.isOverdue(today);
            if (isOverdueOnly && !isOverdue) {
                continue;
            }
            System.out.printf("%d. %s%s%n", i + 1, loan, isOverdue ? " [OVERDUE]" : "");
            hasDisplayedLoans = true;
        }
        if (!hasDisplayedLoans) {
            System.out.println(isOverdueOnly ? "No overdue loans." : "No active loans.");
        }
        System.out.println(Ui.DIVIDER);
    }
}

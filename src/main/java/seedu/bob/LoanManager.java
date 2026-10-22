package seedu.bob;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.InvalidLoanIndexException;

/**
 * Owns active loans in insertion order and coordinates inventory deductions, corrections, and full returns.
 */
public class LoanManager {

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
        LocalDate today = LocalDate.now(clock);
        inventory.lendItem(loan.getName(), loan.getCategory(), loan.getQuantity());
        loans.add(loan);
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
        System.out.println("Successfully returned loan: " + loan + ". Inventory restored.");
    }

    /**
     * Restores stock before removing a loan so failed returns and corrections preserve both lists.
     *
     * @return Removed loan, for the operation-specific confirmation.
     * @throws CommandException If the index is invalid or stock restoration would overflow.
     */
    private Loan removeLoanAndRestoreStock(int userIndex) throws CommandException {
        if (userIndex <= 0 || userIndex > loans.size()) {
            throw new InvalidLoanIndexException(userIndex, loans.size());
        }
        int index = userIndex - 1;
        Loan loan = loans.get(index);
        inventory.restoreStock(loan.getName(), loan.getCategory(), loan.getQuantity());
        loans.remove(index);
        return loan;
    }

    /**
     * Returns an immutable snapshot of active loans in their displayed order.
     *
     * @return Active loan records.
     */
    public List<Loan> getLoans() {
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
        LocalDate today = LocalDate.now(clock);
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

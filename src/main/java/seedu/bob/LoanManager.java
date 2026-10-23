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

    /** Shared available stock; each active loan retains an independent item snapshot. */
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
     * Deducts stock and records a validated loan without printing a confirmation.
     * Captures whether the accepted loan is already overdue. Rejected requests do not change state.
     *
     * @param loan Validated loan to record.
     * @return Recorded loan and overdue status at addition.
     * @throws CommandException If the item is missing or its available stock is insufficient.
     * @throws NullPointerException If the loan is null.
     */
    public LoanAdditionResult addLoan(Loan loan) throws CommandException {
        Objects.requireNonNull(loan, "Loan to add must not be null");
        assert hasValidState() && loan.hasValidState() : "Adding a loan requires valid records";
        LocalDate today = LocalDate.now(clock);
        LoanAdditionResult result = new LoanAdditionResult(loan, loan.isOverdue(today));
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
                + ", overdue=" + result.isOverdue() + ", active=" + loans.size());
        return result;
    }

    /**
     * Removes an incorrect loan and restores all of its units to available inventory.
     * Restores stock before removing the record so a failed restoration preserves the loan.
     *
     * @param userIndex One-based loan position from the latest loan list.
     * @return Incorrect loan removed after successful stock restoration.
     * @throws CommandException If the index is invalid or stock restoration would overflow.
     */
    public Loan deleteLoan(int userIndex) throws CommandException {
        Loan loan = removeLoanAndRestoreStock(userIndex);
        LOGGER.fine(() -> "Deleted incorrect loan: index=" + userIndex + ", item=" + loan.getName()
                + ", category=" + loan.getCategory() + ", quantity=" + loan.getQuantity()
                + ", active=" + loans.size());
        return loan;
    }

    /**
     * Records a full return by restoring all units and removing the active loan without retaining history.
     *
     * @param userIndex One-based loan position from the latest loan list.
     * @return Fully returned loan removed after successful stock restoration.
     * @throws CommandException If the index is invalid or stock restoration would overflow.
     */
    public Loan returnLoan(int userIndex) throws CommandException {
        Loan loan = removeLoanAndRestoreStock(userIndex);
        LOGGER.fine(() -> "Returned loan: index=" + userIndex + ", item=" + loan.getName()
                + ", category=" + loan.getCategory() + ", quantity=" + loan.getQuantity()
                + ", active=" + loans.size());
        return loan;
    }

    /**
     * Restores stock before removing a loan so failed returns and corrections preserve both lists.
     *
     * @param userIndex One-based position in the full active-loan list, including non-overdue loans.
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
     * Returns whether dependencies and active records satisfy the manager's invariants.
     * Duplicate records and past due dates remain valid.
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
     * Returns all active loans with their positions and overdue status at query time.
     *
     * @return Immutable rows in active-loan insertion order.
     */
    public List<LoanListEntry> getLoanEntries() {
        return buildLoanEntries(false);
    }

    /**
     * Returns overdue loans with their original positions in the full active-loan list.
     *
     * @return Immutable overdue rows, whose positions may contain gaps.
     */
    public List<LoanListEntry> getOverdueLoanEntries() {
        return buildLoanEntries(true);
    }

    /**
     * Selects loan rows against one current local date without printing or mutating feature state.
     *
     * @param isOverdueOnly Whether to exclude current loans while preserving full-list positions.
     * @return Immutable rows whose overdue flags share the same reference date.
     */
    private List<LoanListEntry> buildLoanEntries(boolean isOverdueOnly) {
        assert hasValidState() : "Listing requires valid loan records";
        LocalDate today = LocalDate.now(clock);
        LOGGER.fine(() -> "Listing loans: overdueOnly=" + isOverdueOnly + ", active=" + loans.size()
                + ", date=" + today);
        List<LoanListEntry> entries = new ArrayList<>();
        for (int i = 0; i < loans.size(); i++) {
            Loan loan = loans.get(i);
            boolean isOverdue = loan.isOverdue(today);
            if (isOverdueOnly && !isOverdue) {
                continue;
            }
            entries.add(new LoanListEntry(i + 1, loan, isOverdue));
        }
        return List.copyOf(entries);
    }
}

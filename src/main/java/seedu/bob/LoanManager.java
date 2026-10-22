package seedu.bob;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import seedu.bob.exception.CommandException;

/**
 * Owns active loans in insertion order and coordinates deductions from available inventory.
 */
public class LoanManager {

    private final Inventory inventory;
    /** Each addition is a separate loan, even when the item and borrower match an existing entry. */
    private final ArrayList<Loan> loans = new ArrayList<>();

    /**
     * Creates an empty loan list backed by the application's inventory.
     *
     * @param inventory Inventory from which new loans deduct stock.
     * @throws NullPointerException If inventory is null.
     */
    public LoanManager(Inventory inventory) {
        this.inventory = Objects.requireNonNull(inventory, "Loan inventory must not be null");
    }

    /**
     * Deducts stock and records a validated loan, then prints one loan confirmation.
     * Rejected stock requests do not change either inventory or loans.
     *
     * @param loan Validated loan to record.
     * @throws CommandException If the item is missing or its available stock is insufficient.
     * @throws NullPointerException If the loan is null.
     */
    public void addLoan(Loan loan) throws CommandException {
        Objects.requireNonNull(loan, "Loan to add must not be null");
        inventory.lendItem(loan.getName(), loan.getCategory(), loan.getQuantity());
        loans.add(loan);
        System.out.println("Successfully added loan: " + loan);
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
     * Prints active loans with one-based positions, including an explicit empty-list message.
     */
    public void listLoans() {
        System.out.println(Ui.DIVIDER);
        System.out.println("Loans");
        System.out.println(Ui.DIVIDER);
        if (loans.isEmpty()) {
            System.out.println("No active loans.");
        }
        for (int i = 0; i < loans.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, loans.get(i));
        }
        System.out.println(Ui.DIVIDER);
    }
}

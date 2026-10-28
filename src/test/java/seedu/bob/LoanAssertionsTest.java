//@@author DaynaG3
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;

/**
 * Demonstrates that assertions catch broken inventory collaborators before changing active loan records.
 */
class LoanAssertionsTest {

    @Test
    void addLoan_inventoryDoesNotDeduct_assertsBeforeAddingRecord() throws CommandException {
        Inventory inventory = new NonDeductingInventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 10));
        LoanManager loans = new LoanManager(inventory);

        assertThrows(AssertionError.class, () -> loans.addLoan(createLoan()));

        assertTrue(loans.getLoans().isEmpty());
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void removeLoan_inventoryDoesNotRestore_assertsBeforeRemovingRecord() throws CommandException {
        for (boolean isReturn : new boolean[] {false, true}) {
            Inventory inventory = new NonRestoringInventory();
            inventory.addItem(new InventoryItem("Wire", "Components", 10));
            LoanManager loans = new LoanManager(inventory);
            Loan loan = createLoan();
            loans.addLoan(loan);

            assertThrows(AssertionError.class, () -> {
                if (isReturn) {
                    loans.returnLoan(1);
                } else {
                    loans.deleteLoan(1);
                }
            });

            assertEquals(List.of(loan), loans.getLoans());
            assertEquals(7, inventory.getAvailableQuantity("Wire", "Components"));
        }
    }

    /**
     * Creates a valid request whose quantity makes missing stock changes observable.
     */
    private Loan createLoan() throws CommandException {
        return new Loan("Wire", "Components", 3,
                new Contact("Alex", "91234567", null, null), "30 October 2099");
    }

    /**
     * Simulates an internal bug that silently skips stock deduction.
     */
    private static class NonDeductingInventory extends Inventory {
        @Override
        void lendItem(String name, String category, int quantity) {
            // Deliberately broken collaborator for the postcondition assertion.
        }
    }

    /**
     * Simulates an internal bug that silently skips stock restoration.
     */
    private static class NonRestoringInventory extends Inventory {
        @Override
        void restoreStock(String name, String category, int quantity) {
            // Deliberately broken collaborator for the postcondition assertion.
        }
    }
}

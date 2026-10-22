package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.CommandFormatException;
import seedu.bob.exception.InvalidQuantityException;
import seedu.bob.exception.LoanException;

/**
 * Verifies that direct loan stock operations reject invalid requests before modifying any inventory.
 */
class InventoryLoanValidationTest {

    private final Inventory inventory = new Inventory();

    @BeforeEach
    void setUp() throws CommandException {
        inventory.addItem(new InventoryItem("Wire", "Components", 10));
        inventory.addItem(new InventoryItem("Battery", "Spares", 5));
    }

    @Test
    void loanStockOperations_invalidIdentifiers_rejectBeforeMutation() {
        for (String invalid : new String[] {null, "", " "}) {
            assertThrows(CommandFormatException.class, () -> inventory.lendItem(invalid, "Components", 1));
            assertThrows(CommandFormatException.class, () -> inventory.lendItem("Wire", invalid, 1));
            assertThrows(CommandFormatException.class, () -> inventory.restoreStock(invalid, "Components", 1));
            assertThrows(CommandFormatException.class, () -> inventory.restoreStock("Wire", invalid, 1));
            // A missing category must not be created before validating its item name.
            assertThrows(CommandFormatException.class, () -> inventory.restoreStock(invalid, "New category", 1));
            assertOriginalStock();
        }
    }

    @Test
    void loanStockOperations_nonPositiveQuantities_preserveStock() {
        for (int quantity : new int[] {Integer.MIN_VALUE, -1, 0}) {
            assertThrows(InvalidQuantityException.class,
                    () -> inventory.lendItem("Wire", "Components", quantity));
            assertThrows(InvalidQuantityException.class,
                    () -> inventory.restoreStock("Wire", "Components", quantity));
            assertOriginalStock();
        }
    }

    @Test
    void restoreStock_exactIntegerLimit_acceptsBoundaryAndRejectsNextUnit() throws CommandException {
        inventory.restoreStock("wire", "COMPONENTS", Integer.MAX_VALUE - 10);
        assertEquals(Integer.MAX_VALUE, inventory.getAvailableQuantity("Wire", "Components"));

        assertThrows(LoanException.class, () -> inventory.restoreStock("Wire", "Components", 1));

        assertEquals(Integer.MAX_VALUE, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(5, inventory.getAvailableQuantity("Battery", "Spares"));
    }

    /**
     * Checks affected and unrelated stock after each rejected operation.
     */
    private void assertOriginalStock() {
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(5, inventory.getAvailableQuantity("Battery", "Spares"));
        assertEquals(0, inventory.getAvailableQuantity("Wire", "New category"));
    }
}

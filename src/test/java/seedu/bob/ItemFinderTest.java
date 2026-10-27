//@@author J-unn-y
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;

/**
 * Verifies keyword matching, stock and loan totals, and result ordering for quick lookup.
 */
class ItemFinderTest {

    private final Inventory inventory = new Inventory();
    private final LoanManager loans = new LoanManager(inventory);

    @Test
    void findItems_partialNameDifferentCase_matchesItem() throws CommandException {
        inventory.addItem(new InventoryItem("Multimeter", "Equipment", 5));
        inventory.addItem(new InventoryItem("Wire", "Components", 20));

        List<ItemLookupEntry> results = ItemFinder.findItems("METER", inventory, loans);

        assertEquals(1, results.size());
        assertEquals("Multimeter [Equipment] (Available: 5, On loan: 0) - Available", results.get(0).toString());
    }

    @Test
    void findItems_partialCategory_matchesEveryItemInCategory() throws CommandException {
        inventory.addItem(new InventoryItem("Wire", "Components", 20));
        inventory.addItem(new InventoryItem("Red LED", "Components", 3));
        inventory.addItem(new InventoryItem("Multimeter", "Equipment", 5));

        List<ItemLookupEntry> results = ItemFinder.findItems("comp", inventory, loans);

        assertEquals(2, results.size());
        assertEquals("Wire", results.get(0).getName());
        assertEquals("Red LED", results.get(1).getName());
    }

    @Test
    void findItems_sameNameInTwoCategories_returnsSeparateEntries() throws CommandException {
        inventory.addItem(new InventoryItem("Wire", "Components", 20));
        inventory.addItem(new InventoryItem("Wire", "Spares", 100));

        List<ItemLookupEntry> results = ItemFinder.findItems("wire", inventory, loans);

        assertEquals(2, results.size());
        assertEquals(20, results.get(0).getAvailableQuantity());
        assertEquals(100, results.get(1).getAvailableQuantity());
    }

    @Test
    void findItems_noMatch_returnsEmptyList() throws CommandException {
        inventory.addItem(new InventoryItem("Wire", "Components", 20));

        assertTrue(ItemFinder.findItems("battery", inventory, loans).isEmpty());
    }

    @Test
    void findItems_partlyLoanedItem_showsStockAndLoanedUnits() throws CommandException {
        inventory.addItem(new InventoryItem("Multimeter", "Equipment", 5));
        loans.addLoan(createLoan("Multimeter", "Equipment", 2));
        loans.addLoan(createLoan("multimeter", "equipment", 1));

        List<ItemLookupEntry> results = ItemFinder.findItems("multimeter", inventory, loans);

        assertEquals(1, results.size());
        assertEquals(2, results.get(0).getAvailableQuantity());
        assertEquals(3, results.get(0).getLoanedQuantity());
        assertEquals("Available", results.get(0).getStatus());
    }

    @Test
    void findItems_everyUnitLoaned_stillFoundAsAllOnLoan() throws CommandException {
        inventory.addItem(new InventoryItem("Oscilloscope", "Equipment", 2));
        inventory.addItem(new InventoryItem("Wire", "Components", 20));
        loans.addLoan(createLoan("Oscilloscope", "Equipment", 2));

        List<ItemLookupEntry> results = ItemFinder.findItems("scope", inventory, loans);

        assertEquals(1, results.size());
        assertEquals("Oscilloscope [Equipment] (Available: 0, On loan: 2) - All on loan",
                results.get(0).toString());
    }

    @Test
    void findItems_stockChangesAfterSearch_resultsUnchanged() throws CommandException {
        inventory.addItem(new InventoryItem("Wire", "Components", 20));
        List<ItemLookupEntry> results = ItemFinder.findItems("wire", inventory, loans);

        loans.addLoan(createLoan("Wire", "Components", 5));

        assertEquals(20, results.get(0).getAvailableQuantity());
        assertEquals(0, results.get(0).getLoanedQuantity());
        assertEquals(15, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void findItems_searching_doesNotChangeStockOrLoans() throws CommandException {
        inventory.addItem(new InventoryItem("Wire", "Components", 20));
        loans.addLoan(createLoan("Wire", "Components", 5));

        ItemFinder.findItems("wire", inventory, loans);

        assertEquals(15, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(1, loans.getLoans().size());
    }

    @Test
    void findItems_blankOrNullArguments_rejected() {
        assertThrows(IllegalArgumentException.class, () -> ItemFinder.findItems("  ", inventory, loans));
        assertThrows(NullPointerException.class, () -> ItemFinder.findItems(null, inventory, loans));
        assertThrows(NullPointerException.class, () -> ItemFinder.findItems("wire", null, loans));
        assertThrows(NullPointerException.class, () -> ItemFinder.findItems("wire", inventory, null));
    }

    @Test
    void getItemSnapshots_changingCopy_doesNotChangeStock() throws CommandException {
        inventory.addItem(new InventoryItem("Wire", "Components", 20));

        inventory.getItemSnapshots().get(0).removeQuantity(5);

        assertEquals(20, inventory.getAvailableQuantity("Wire", "Components"));
    }

    /**
     * Creates a loan due far in the future so overdue rules do not affect these tests.
     */
    private Loan createLoan(String name, String category, int quantity) throws CommandException {
        return new Loan(name, category, quantity, new Contact("Sam Lee", "91234567", null, null), "31 December 2099");
    }
}

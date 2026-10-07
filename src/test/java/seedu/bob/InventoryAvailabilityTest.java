package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;

/**
 * Verifies that preparation stock checks select the correct category and reflect inventory changes.
 */
class InventoryAvailabilityTest {

    @Test
    void getAvailableQuantity_caseVaries_matchesNameAndCategory() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 20));
        inventory.addItem(new InventoryItem("Wire", "Spares", 100));

        assertEquals(20, inventory.getAvailableQuantity("wire", "COMPONENTS"));
        assertEquals(100, inventory.getAvailableQuantity("WIRE", "spares"));
        assertEquals(0, inventory.getAvailableQuantity("Battery", "Components"));
        assertEquals(0, inventory.getAvailableQuantity("Wire", "Unknown"));
    }

    @Test
    void getAvailableQuantity_inventoryChanges_returnsCurrentStock() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 20));
        inventory.removeItem("Components", 1, 5);

        assertEquals(15, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(15, inventory.getAvailableQuantity("Wire", "Components"));
        inventory.removeItem("Components", 1, 15);
        assertEquals(0, inventory.getAvailableQuantity("Wire", "Components"));
    }
}

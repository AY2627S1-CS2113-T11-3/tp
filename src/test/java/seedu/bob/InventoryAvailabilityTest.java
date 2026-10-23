package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;

/**
 * Verifies preparation stock checks, total request availability, and inventory boundary cases.
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

    @Test
    void getTotalAvailableQuantity_caseVaries_sumsAllCategories() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 20));
        inventory.addItem(new InventoryItem("Wire", "Spares", 100));

        assertEquals(120, inventory.getTotalAvailableQuantity("wIrE"));
        assertEquals(0, inventory.getTotalAvailableQuantity("Battery"));
    }

    @Test
    void getTotalAvailableQuantity_inventoryChanges_returnsCurrentTotal() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 20));
        inventory.addItem(new InventoryItem("Wire", "Spares", 100));

        inventory.removeItem("Spares", 1, 40);

        assertEquals(80, inventory.getTotalAvailableQuantity("Wire"));
    }

    @Test
    void getTotalAvailableQuantity_nullName_throwsNullPointerException() {
        Inventory inventory = new Inventory();

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> inventory.getTotalAvailableQuantity(null));

        assertEquals("Item name to look up must not be null", exception.getMessage());
    }

    @Test
    void getTotalAvailableQuantity_sumOverflows_saturatesAtMax() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", Integer.MAX_VALUE));
        inventory.addItem(new InventoryItem("Wire", "Spares", Integer.MAX_VALUE));

        assertEquals(Integer.MAX_VALUE, inventory.getTotalAvailableQuantity("Wire"));
    }
}

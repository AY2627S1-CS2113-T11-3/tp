package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests the creation, validation, and property accessors of {@link InventoryItem}.
 */
class InventoryItemTest {

    //@@author DaynaG3
    @Test
    void addQuantity_validAddition_updatesStockAtIntegerBoundary() {
        InventoryItem item = new InventoryItem("Wire", "Components", Integer.MAX_VALUE - 2);
        item.addQuantity(2);
        assertEquals(Integer.MAX_VALUE, item.getQuantity());
    }

    @Test
    void addQuantity_nonPositiveOrOverflowingAddition_preservesStock() {
        InventoryItem item = new InventoryItem("Wire", "Components", Integer.MAX_VALUE - 2);
        for (int quantity : new int[] {0, -1, 3, Integer.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> item.addQuantity(quantity));
            assertEquals(Integer.MAX_VALUE - 2, item.getQuantity());
        }
    }
    //@@author

    @Test
    void constructor_validArguments_success() {
        InventoryItem item = new InventoryItem("Resistor", "Electronics", 50);

        assertEquals("Resistor", item.getName());
        assertEquals("Electronics", item.getCategory());
        assertEquals(50, item.getQuantity());
    }

    @Test
    void constructor_blankName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new InventoryItem(null, "Electronics", 10));
        assertThrows(IllegalArgumentException.class, () -> new InventoryItem("", "Electronics", 10));
        assertThrows(IllegalArgumentException.class, () -> new InventoryItem("   ", "Electronics", 10));
    }

    @Test
    void constructor_blankCategory_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new InventoryItem("Resistor", null, 10));
        assertThrows(IllegalArgumentException.class, () -> new InventoryItem("Resistor", "", 10));
        assertThrows(IllegalArgumentException.class, () -> new InventoryItem("Resistor", "   ", 10));
    }

    @Test
    void constructor_nonPositiveQuantity_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new InventoryItem("Resistor", "Electronics", 0));
        assertThrows(IllegalArgumentException.class, () -> new InventoryItem("Resistor", "Electronics", -5));
    }

    //@@author DaynaG3
    @Test
    void removeQuantity_validPartialRemoval_updatesQuantity() {
        InventoryItem item = new InventoryItem("Resistor", "Electronics", 50);

        item.removeQuantity(20);

        assertEquals("Resistor", item.getName());
        assertEquals("Electronics", item.getCategory());
        assertEquals(30, item.getQuantity());
    }

    @Test
    void removeQuantity_invalidRemoval_throwsIllegalArgumentException() {
        InventoryItem item = new InventoryItem("Resistor", "Electronics", 50);

        assertThrows(IllegalArgumentException.class, () -> item.removeQuantity(0));
        assertThrows(IllegalArgumentException.class, () -> item.removeQuantity(-1));
        assertThrows(IllegalArgumentException.class, () -> item.removeQuantity(50));
        assertThrows(IllegalArgumentException.class, () -> item.removeQuantity(51));
        assertEquals(50, item.getQuantity());
    }
    //@@author
}

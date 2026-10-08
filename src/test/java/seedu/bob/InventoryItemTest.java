package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests the creation, validation, and property accessors of {@link InventoryItem}.
 */
class InventoryItemTest {

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

    @Test
    void setters_validValues_updatesFields() {
        InventoryItem item = new InventoryItem("Resistor", "Electronics", 50);

        item.setName("Capacitor");
        item.setCategory("Components");
        item.setQuantity(100);

        assertEquals("Capacitor", item.getName());
        assertEquals("Components", item.getCategory());
        assertEquals(100, item.getQuantity());
    }
}

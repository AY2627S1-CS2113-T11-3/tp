package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.bob.exception.CategoryNotFoundException;
import seedu.bob.exception.DuplicateItemException;
import seedu.bob.exception.InsufficientStockException;
import seedu.bob.exception.InvalidItemIndexException;
import seedu.bob.exception.InvalidQuantityException;

/**
 * Verifies addition, duplicate handling, and quantity reductions in {@link Inventory}.
 */
class InventoryTest {

    @Test
    void addItem_validItem_success() throws DuplicateItemException {
        Inventory inventory = new Inventory();
        InventoryItem item = new InventoryItem("Screws", "Fasteners", 100);

        inventory.addItem(item);

        assertEquals(100, inventory.getAvailableQuantity("Screws", "Fasteners"));
    }

    @Test
    void addItem_duplicateItemSameCategory_throwsDuplicateItemException() throws DuplicateItemException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Screws", "Fasteners", 100));

        assertThrows(DuplicateItemException.class, () ->
                inventory.addItem(new InventoryItem("screws", "Fasteners", 50)));
    }

    @Test
    void addItem_sameNameDifferentCategory_success() throws DuplicateItemException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Screws", "Fasteners", 100));
        inventory.addItem(new InventoryItem("Screws", "Spares", 50));

        assertEquals(100, inventory.getAvailableQuantity("Screws", "Fasteners"));
        assertEquals(50, inventory.getAvailableQuantity("Screws", "Spares"));
    }

    @Test
    void removeItem_validPartialQuantity_decrementsStock() throws Exception {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Hammer", "Tools", 10));

        inventory.removeItem("Tools", 1, 4);

        assertEquals(6, inventory.getAvailableQuantity("Hammer", "Tools"));
    }

    @Test
    void removeItem_entireQuantity_removesItemAndCategory() throws Exception {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Hammer", "Tools", 10));

        inventory.removeItem("Tools", 1, 10);

        assertEquals(0, inventory.getAvailableQuantity("Hammer", "Tools"));
    }

    @Test
    void removeItem_nonExistentCategory_throwsCategoryNotFoundException() {
        Inventory inventory = new Inventory();

        assertThrows(CategoryNotFoundException.class, () ->
                inventory.removeItem("UnknownCategory", 1, 5));
    }

    @Test
    void removeItem_invalidIndex_throwsInvalidItemIndexException() throws DuplicateItemException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wrench", "Tools", 5));

        assertThrows(InvalidItemIndexException.class, () -> inventory.removeItem("Tools", 0, 1));
        assertThrows(InvalidItemIndexException.class, () -> inventory.removeItem("Tools", 2, 1));
    }

    @Test
    void removeItem_nonPositiveQuantity_throwsInvalidQuantityException() throws DuplicateItemException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wrench", "Tools", 5));

        assertThrows(InvalidQuantityException.class, () -> inventory.removeItem("Tools", 1, 0));
        assertThrows(InvalidQuantityException.class, () -> inventory.removeItem("Tools", 1, -2));
    }

    @Test
    void removeItem_quantityExceedsStock_throwsInsufficientStockException() throws DuplicateItemException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wrench", "Tools", 5));

        assertThrows(InsufficientStockException.class, () -> inventory.removeItem("Tools", 1, 6));
    }
}

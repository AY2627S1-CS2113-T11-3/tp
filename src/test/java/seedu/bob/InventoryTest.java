//@@author Lokwx
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.bob.exception.CategoryNotFoundException;
import seedu.bob.exception.CommandException;
import seedu.bob.exception.DuplicateItemException;
import seedu.bob.exception.InsufficientStockException;
import seedu.bob.exception.InvalidItemIndexException;
import seedu.bob.exception.InvalidQuantityException;

/**
 * Verifies addition, duplicate handling, and quantity reductions in {@link Inventory}.
 */
class InventoryTest {

    //@@author DaynaG3
    @Test
    void addItem_nullItem_throwsNullPointerExceptionWithoutChangingInventory() {
        Inventory inventory = new Inventory();

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> inventory.addItem(null));

        assertEquals("Item to add must not be null", exception.getMessage());
        assertEquals(0, inventory.getAvailableQuantity("Screws", "Fasteners"));
    }
    //@@author

    //@@author Lokwx
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

    //@@author DaynaG3
    @Test
    void addItem_duplicateWithDifferentCategoryCase_throwsExceptionAndPreservesStock()
            throws DuplicateItemException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Screws", "Fasteners", 100));

        DuplicateItemException exception = assertThrows(DuplicateItemException.class, () ->
                inventory.addItem(new InventoryItem("Screws", "FASTENERS", 50)));

        assertEquals("Item \"Screws\" already exists in category \"Fasteners\".", exception.getMessage());
        assertEquals(100, inventory.getAvailableQuantity("Screws", "Fasteners"));
    }
    //@@author

    //@@author Lokwx
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

    //@@author DaynaG3
    @Test
    void removeItem_insufficientStock_preservesStockAndReportsQuantities() throws DuplicateItemException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wrench", "Tools", 5));

        InsufficientStockException exception = assertThrows(InsufficientStockException.class,
                () -> inventory.removeItem("Tools", 1, 6));

        assertEquals("Cannot remove 6 Wrench. Only 5 are available.", exception.getMessage());
        assertEquals(5, inventory.getAvailableQuantity("Wrench", "Tools"));
    }
    //@@author

    //@@author Lokwx
    @Test
    void deductTotalAvailableQuantity_acrossCategories_deductsInDisplayOrder() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 3));
        inventory.addItem(new InventoryItem("Wire", "Spares", 4));

        inventory.deductTotalAvailableQuantity("wIrE", 5);

        assertEquals(0, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(2, inventory.getAvailableQuantity("Wire", "Spares"));
        assertEquals(2, inventory.getTotalAvailableQuantity("Wire"));
    }

    @Test
    void deductTotalAvailableQuantity_insufficientStock_preservesAllStock() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 3));
        inventory.addItem(new InventoryItem("Wire", "Spares", 4));

        InsufficientStockException exception = assertThrows(InsufficientStockException.class,
                () -> inventory.deductTotalAvailableQuantity("Wire", 8));

        assertEquals("Cannot remove 8 Wire. Only 7 are available.", exception.getMessage());
        assertEquals(3, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(4, inventory.getAvailableQuantity("Wire", "Spares"));
    }

    @Test
    void deductTotalAvailableQuantity_exactStock_removesItemAndCategory() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 5));

        inventory.deductTotalAvailableQuantity("Wire", 5);

        assertEquals(0, inventory.getTotalAvailableQuantity("Wire"));
    }

    @Test
    void deductTotalAvailableQuantity_missingItem_throwsInsufficientStockException() {
        Inventory inventory = new Inventory();

        InsufficientStockException exception = assertThrows(InsufficientStockException.class,
                () -> inventory.deductTotalAvailableQuantity("Absent", 1));

        assertEquals("Cannot remove 1 Absent. Only 0 are available.", exception.getMessage());
    }

    @Test
    void deductTotalAvailableQuantity_nonPositiveQuantity_throwsInvalidQuantityException() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 5));

        assertThrows(InvalidQuantityException.class,
                () -> inventory.deductTotalAvailableQuantity("Wire", 0));
        assertThrows(InvalidQuantityException.class,
                () -> inventory.deductTotalAvailableQuantity("Wire", -1));
    }

    @Test
    void deductTotalAvailableQuantity_nullName_throwsNullPointerException() {
        Inventory inventory = new Inventory();

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> inventory.deductTotalAvailableQuantity(null, 1));

        assertEquals("Item name must not be null", exception.getMessage());
    }
    //@@author
}

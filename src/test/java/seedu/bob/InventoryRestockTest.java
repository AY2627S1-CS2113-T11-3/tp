//@@author Lokwx
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.bob.exception.CategoryNotFoundException;
import seedu.bob.exception.CommandException;
import seedu.bob.exception.CommandFormatException;
import seedu.bob.exception.InventoryItemNotFoundException;
import seedu.bob.exception.InvalidQuantityException;
import seedu.bob.exception.StockOverflowException;

/**
 * Verifies that restocking increases only existing inventory entries and rejects invalid requests
 * without changing stock.
 */
class InventoryRestockTest {

    @Test
    void restockItem_existingItem_addsQuantityToThatEntry() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 10));

        inventory.restockItem("Wire", "Components", 5);

        assertEquals(15, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void restockItem_sameNameDifferentCategories_affectsOnlySelectedCategory() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 3));
        inventory.addItem(new InventoryItem("Wire", "Spares", 4));

        inventory.restockItem("Wire", "Spares", 6);

        assertEquals(3, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Spares"));
    }

    @Test
    void restockItem_caseVaries_matchesStoredNameAndCategory() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 10));

        inventory.restockItem("wIrE", "cOMPONENTS", 5);

        assertEquals(15, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals("Wire", inventory.getItemSnapshots().getFirst().getName());
        assertEquals("Components", inventory.getItemSnapshots().getFirst().getCategory());
    }

    @Test
    void restockItem_missingItem_throwsAndPreservesStock() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 10));

        InventoryItemNotFoundException exception = assertThrows(InventoryItemNotFoundException.class,
                () -> inventory.restockItem("Cable", "Components", 5));

        assertEquals("Item \"Cable\" does not exist in category \"Components\". "
                + "Use add-i to add a new item.", exception.getMessage());
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void restockItem_missingCategory_throwsCategoryNotFound() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 10));

        CategoryNotFoundException exception = assertThrows(CategoryNotFoundException.class,
                () -> inventory.restockItem("Wire", "Tools", 5));

        assertEquals("Category \"Tools\" does not exist.", exception.getMessage());
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void restockItem_nonPositiveQuantity_throwsInvalidQuantity() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 10));

        assertThrows(InvalidQuantityException.class, () -> inventory.restockItem("Wire", "Components", 0));
        assertThrows(InvalidQuantityException.class, () -> inventory.restockItem("Wire", "Components", -5));
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void restockItem_blankOrNullDetails_throwsWithoutChangingInventory() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 10));

        assertThrows(CommandFormatException.class, () -> inventory.restockItem(null, "Components", 5));
        assertThrows(CommandFormatException.class, () -> inventory.restockItem(" ", "Components", 5));
        assertThrows(CommandFormatException.class, () -> inventory.restockItem("Wire", null, 5));
        assertThrows(CommandFormatException.class, () -> inventory.restockItem("Wire", " ", 5));
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void restockItem_quantityWouldOverflow_throwsAndPreservesStock() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 2147483647));

        StockOverflowException exception = assertThrows(StockOverflowException.class,
                () -> inventory.restockItem("Wire", "Components", 1));

        assertEquals("Cannot add 1 Wire. Inventory quantity must not exceed 2147483647.",
                exception.getMessage());
        assertEquals(2147483647, inventory.getAvailableQuantity("Wire", "Components"));
    }
}

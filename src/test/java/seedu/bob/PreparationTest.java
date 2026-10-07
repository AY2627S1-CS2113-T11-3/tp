package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.CommandFormatException;
import seedu.bob.exception.InvalidHeadcountException;
import seedu.bob.exception.InvalidQuantityException;
import seedu.bob.exception.PreparationException;

/**
 * Verifies preparation arithmetic, item identity, and atomic rejection of invalid changes.
 */
class PreparationTest {

    @Test
    void addItem_perPersonQuantity_multipliedByHeadcount() throws CommandException {
        Preparation preparation = new Preparation();

        PreparationItem item = preparation.addItem("Wire", "Components", 2, 10);

        assertEquals(20, item.getQuantity());
        assertEquals("Wire", item.getName());
        assertEquals("Components", item.getCategory());
    }

    @Test
    void addItem_repeatedPair_accumulatesWithoutChangingSpelling() throws CommandException {
        Preparation preparation = new Preparation();
        preparation.addItem("Wire", "Components", 2, 10);

        preparation.addItem(" wire ", " components ", 1, 10);

        assertEquals(1, preparation.getItems().size());
        assertEquals(30, preparation.getItems().get(0).getQuantity());
        assertEquals("Wire [Components]", preparation.getItems().get(0).toString());
    }

    @Test
    void addItem_sameNameInDifferentCategories_keptSeparate() throws CommandException {
        Preparation preparation = new Preparation();
        preparation.addItem("Wire", "Components", 2, 10);
        preparation.addItem("Wire", "Spares", 1, 10);

        assertEquals(2, preparation.getItems().size());
        assertEquals(20, preparation.getItems().get(0).getQuantity());
        assertEquals(10, preparation.getItems().get(1).getQuantity());
    }

    @Test
    void removeItem_partialQuantity_subtractsTotalUnits() throws CommandException {
        Preparation preparation = new Preparation();
        preparation.addItem("Wire", "Components", 2, 10);

        preparation.removeItem(1, 3);

        assertEquals(17, preparation.getItems().get(0).getQuantity());
    }

    @Test
    void removeItem_entireQuantity_renumbersRemainingItems() throws CommandException {
        Preparation preparation = new Preparation();
        preparation.addItem("Wire", "Components", 2, 10);
        preparation.addItem("Battery", "Components", 1, 10);

        preparation.removeItem(1, 20);
        assertEquals("Battery", preparation.getItems().get(0).getName());
        preparation.removeItem(1, 10);

        assertTrue(preparation.getItems().isEmpty());
    }

    @Test
    void removeItem_invalidChange_existingRequirementUnchanged() throws CommandException {
        Preparation preparation = new Preparation();
        preparation.addItem("Wire", "Components", 2, 10);

        assertThrows(PreparationException.class, () -> preparation.removeItem(1, 21));
        assertThrows(PreparationException.class, () -> preparation.removeItem(0, 1));
        assertThrows(PreparationException.class, () -> preparation.removeItem(2, 1));
        assertThrows(InvalidQuantityException.class, () -> preparation.removeItem(1, 0));
        assertThrows(InvalidQuantityException.class, () -> preparation.removeItem(1, -1));
        assertEquals(20, preparation.getItems().get(0).getQuantity());
    }

    @Test
    void addItem_invalidValues_nothingAdded() {
        Preparation preparation = new Preparation();

        assertThrows(CommandFormatException.class, () -> preparation.addItem(" ", "Components", 1, 10));
        assertThrows(CommandFormatException.class, () -> preparation.addItem(null, "Components", 1, 10));
        assertThrows(CommandFormatException.class, () -> preparation.addItem("Wire", " ", 1, 10));
        assertThrows(CommandFormatException.class, () -> preparation.addItem("Wire", null, 1, 10));
        assertThrows(InvalidQuantityException.class, () -> preparation.addItem("Wire", "Components", 0, 10));
        assertThrows(InvalidQuantityException.class, () -> preparation.addItem("Wire", "Components", -1, 10));
        assertThrows(InvalidHeadcountException.class, () -> preparation.addItem("Wire", "Components", 1, 0));
        assertTrue(preparation.getItems().isEmpty());
    }

    @Test
    void addItem_multiplicationOverflow_nothingAdded() {
        Preparation preparation = new Preparation();

        assertThrows(PreparationException.class,
                () -> preparation.addItem("Wire", "Components", Integer.MAX_VALUE, 2));
        assertTrue(preparation.getItems().isEmpty());
    }

    @Test
    void addItem_accumulationOverflow_existingRequirementUnchanged() throws CommandException {
        Preparation preparation = new Preparation();
        preparation.addItem("Wire", "Components", Integer.MAX_VALUE, 1);

        assertThrows(PreparationException.class, () -> preparation.addItem("Wire", "Components", 1, 1));
        assertEquals(Integer.MAX_VALUE, preparation.getItems().get(0).getQuantity());
    }

    @Test
    void getItems_snapshot_cannotModifyPreparation() throws CommandException {
        Preparation preparation = new Preparation();
        preparation.addItem("Wire", "Components", 2, 10);
        List<PreparationItem> snapshot = preparation.getItems();

        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        preparation.removeItem(1, 5);

        assertEquals(20, snapshot.get(0).getQuantity());
        assertEquals(15, preparation.getItems().get(0).getQuantity());
    }
}

//@@author gavinlimsh
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.bob.exception.BrokenItemException;
import seedu.bob.exception.CommandException;

/**
 * Tests stock conservation, failed operations, report indexing, and live contact membership.
 */
class BrokenItemManagerTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-23T04:00:00Z"), ZoneOffset.UTC);
    private final Inventory inventory = new Inventory();
    private final ContactManager contacts = new ContactManager();
    private final BrokenItemManager manager = new BrokenItemManager(inventory, contacts);

    @BeforeEach
    void setUp() throws CommandException {
        inventory.addItem(new InventoryItem("Meter", "Equipment", 10));
    }

    @Test
    void addBrokenItem_validReport_deductsOnlyMatchingCategory() throws CommandException {
        inventory.addItem(new InventoryItem("Meter", "Tools", 5));
        BrokenItem item = new BrokenItem("meter", "EQUIPMENT", 3, "Fault", "23 October 2026", null, CLOCK);
        assertSame(item, manager.addBrokenItem(item));
        assertEquals(7, stock());
        assertEquals(5, inventory.getAvailableQuantity("Meter", "Tools"));
        assertEquals(List.of(item), manager.getBrokenItems());
    }

    @Test
    void addBrokenItem_missingOrInsufficientStock_preservesState() throws CommandException {
        assertThrows(BrokenItemException.class, () -> manager.addBrokenItem(report(11, null)));
        assertThrows(CommandException.class, () -> manager.addBrokenItem(
                new BrokenItem("Meter", "Absent", 1, "Fault", "23 October 2026", null, CLOCK)));
        assertThrows(BrokenItemException.class, () -> manager.addBrokenItem(
                new BrokenItem("Absent", "Equipment", 1, "Fault", "23 October 2026", null, CLOCK)));
        assertEquals(10, stock());
        assertTrue(manager.getBrokenItems().isEmpty());
    }

    @Test
    void deleteBrokenItem_invalidIndex_preservesState() throws CommandException {
        BrokenItem item = manager.addBrokenItem(report(2, null));
        for (int index : new int[]{Integer.MIN_VALUE, -1, 0, 2, Integer.MAX_VALUE}) {
            assertThrows(BrokenItemException.class, () -> manager.deleteBrokenItem(index));
            assertEquals(List.of(item), manager.getBrokenItems());
            assertEquals(8, stock());
        }
    }

    @Test
    void deleteBrokenItem_multipleReports_restoresSelectedQuantityAndRenumbers() throws CommandException {
        BrokenItem first = manager.addBrokenItem(report(2, null));
        BrokenItem second = manager.addBrokenItem(report(3, null));
        assertEquals(5, stock());
        assertSame(first, manager.deleteBrokenItem(1));
        assertEquals(7, stock());
        assertEquals(List.of(second), manager.getBrokenItems());
        assertSame(second, manager.deleteBrokenItem(1));
        assertEquals(10, stock());
        assertThrows(BrokenItemException.class, () -> manager.deleteBrokenItem(1));
        assertEquals(10, stock());
    }

    @Test
    void deleteBrokenItem_depletedCategory_recreatesStock() throws CommandException {
        manager.addBrokenItem(report(10, null));
        assertEquals(0, stock());
        manager.deleteBrokenItem(1);
        assertEquals(10, stock());
        assertTrue(manager.getBrokenItems().isEmpty());
    }

    @Test
    void deleteBrokenItem_missingItemInExistingCategory_preservesOtherItems() throws CommandException {
        inventory.addItem(new InventoryItem("Sensor", "Equipment", 4));
        manager.addBrokenItem(report(10, null));
        manager.deleteBrokenItem(1);
        assertEquals(10, stock());
        assertEquals(4, inventory.getAvailableQuantity("Sensor", "Equipment"));
    }

    @Test
    void deleteBrokenItem_restorationOverflow_retainsReportUntilStockCanBeRestored() throws CommandException {
        BrokenItem item = manager.addBrokenItem(report(10, null));
        inventory.addItem(new InventoryItem("Meter", "Equipment", Integer.MAX_VALUE));
        assertThrows(BrokenItemException.class, () -> manager.deleteBrokenItem(1));
        assertEquals(Integer.MAX_VALUE, stock());
        assertEquals(List.of(item), manager.getBrokenItems());
        inventory.removeItem("Equipment", 1, 10);
        manager.deleteBrokenItem(1);
        assertEquals(Integer.MAX_VALUE, stock());
        assertTrue(manager.getBrokenItems().isEmpty());
    }

    @Test
    void getBrokenItems_snapshot_cannotModifyManagerAndDoesNotTrackLaterChanges() throws CommandException {
        BrokenItem first = manager.addBrokenItem(report(2, null));
        List<BrokenItem> snapshot = manager.getBrokenItems();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        manager.addBrokenItem(report(1, null));
        assertEquals(List.of(first), snapshot);
        assertEquals(2, manager.getBrokenItems().size());
        assertEquals(7, stock());
    }

    @Test
    void getAvailableContact_deletionAndReplacement_doNotRetargetReport() throws CommandException {
        contacts.addContact(new Contact("Other", "11111111", null, null));
        Contact selected = new Contact("Maintenance", "22222222", null, null);
        contacts.addContact(selected);
        BrokenItem item = manager.addBrokenItem(report(2, selected));
        contacts.deleteContact(1);
        assertSame(selected, contacts.getContact(1));
        assertSame(selected, manager.getAvailableContact(item));
        contacts.deleteContact(1);
        contacts.addContact(new Contact("Maintenance", "22222222", null, null));
        assertNull(manager.getAvailableContact(item));
        assertFalse(contacts.containsContact(selected));
        assertFalse(contacts.containsContact(null));
        manager.deleteBrokenItem(1);
        assertEquals(10, stock());
    }

    @Test
    void addBrokenItem_unregisteredContact_rejectsBeforeDeduction() throws CommandException {
        Contact absent = new Contact("Maintenance", "22222222", null, null);
        assertThrows(BrokenItemException.class, () -> manager.addBrokenItem(report(2, absent)));
        assertEquals(10, stock());
        assertTrue(manager.getBrokenItems().isEmpty());
        assertNull(manager.getAvailableContact(manager.addBrokenItem(report(2, null))));
    }

    @Test
    void stockChanges_loansAndBrokenReports_shareAvailableInventory() throws CommandException {
        LoanManager loans = new LoanManager(inventory, CLOCK);
        loans.addLoan(new Loan("Meter", "Equipment", 3,
                new Contact("Student", "12345678", null, null), "30 October 2026"));
        manager.addBrokenItem(report(7, null));
        assertEquals(0, stock());
        assertThrows(CommandException.class, () -> manager.addBrokenItem(report(1, null)));
        loans.returnLoan(1);
        assertEquals(3, stock());
        manager.deleteBrokenItem(1);
        assertEquals(10, stock());
    }

    @Test
    void nullArguments_rejectWithoutMutation() {
        assertThrows(NullPointerException.class, () -> new BrokenItemManager(null, contacts));
        assertThrows(NullPointerException.class, () -> new BrokenItemManager(inventory, null));
        assertThrows(NullPointerException.class, () -> manager.addBrokenItem(null));
        assertThrows(NullPointerException.class, () -> manager.getAvailableContact(null));
        assertEquals(10, stock());
        assertTrue(manager.getBrokenItems().isEmpty());
    }

    /** Builds reports with a fixed date so tests do not depend on the day they run. */
    private BrokenItem report(int quantity, Contact contact) throws BrokenItemException {
        return new BrokenItem("Meter", "Equipment", quantity, "Fault", "23 October 2026", contact, CLOCK);
    }

    private int stock() {
        return inventory.getAvailableQuantity("Meter", "Equipment");
    }
}

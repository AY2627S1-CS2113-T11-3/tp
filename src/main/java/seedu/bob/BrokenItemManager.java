//@@author gavinlimsh
package seedu.bob;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.bob.exception.BrokenItemException;
import seedu.bob.exception.CommandException;
import seedu.bob.exception.InvalidBrokenItemIndexException;

/**
 * Tracks broken-item reports in insertion order using the application's shared inventory and contacts.
 * Each report removes usable stock until its full quantity is restored by deleting the report.
 */
public class BrokenItemManager {

    private static final int INDEX_OFFSET = 1;
    /** Records diagnostics without adding messages to normal console output. */
    private static final Logger LOGGER = Logger.getLogger(BrokenItemManager.class.getName());

    private final Inventory inventory;
    private final ContactManager contactManager;
    /** Reports remain separate even when they describe the same item and fault. */
    private final ArrayList<BrokenItem> brokenItems = new ArrayList<>();

    /**
     * Creates an empty report list backed by the same inventory and contacts as the other features.
     *
     * @param inventory Shared usable stock.
     * @param contactManager Shared supplier and maintenance contacts.
     * @throws NullPointerException If either dependency is null.
     */
    public BrokenItemManager(Inventory inventory, ContactManager contactManager) {
        this.inventory = Objects.requireNonNull(inventory, "Broken-item inventory must not be null");
        this.contactManager = Objects.requireNonNull(contactManager, "Contact manager must not be null");
        assert hasValidState() : "A new broken-item manager must have valid dependencies";
    }

    /**
     * Removes usable stock and appends a validated report without printing a confirmation.
     * An assigned contact must still be registered. Rejected operations leave stock and reports unchanged.
     *
     * @param brokenItem Validated report to add; an unassigned contact is allowed.
     * @return Added report for the caller's confirmation.
     * @throws CommandException If the contact is no longer registered, the item is missing,
     *     or usable stock is insufficient.
     * @throws NullPointerException If the report is null.
     */
    public BrokenItem addBrokenItem(BrokenItem brokenItem) throws CommandException {
        Objects.requireNonNull(brokenItem, "Broken item to add must not be null");
        assert hasValidState() && brokenItem.hasValidState() : "Addition requires valid reports";
        Contact contact = brokenItem.getContact();
        if (contact != null && !contactManager.containsContact(contact)) {
            LOGGER.fine("Rejected broken-item report: selected contact is no longer registered");
            throw new BrokenItemException("Selected contact is no longer available. Use list-c to choose a contact.");
        }

        int previousStock = inventory.getAvailableQuantity(brokenItem.getName(), brokenItem.getCategory());
        inventory.removeBrokenStock(brokenItem.getName(), brokenItem.getCategory(), brokenItem.getQuantity());
        assert inventory.getAvailableQuantity(brokenItem.getName(), brokenItem.getCategory())
                == (long) previousStock - brokenItem.getQuantity() : "Addition must deduct the exact broken quantity";
        brokenItems.add(brokenItem);
        assert hasValidState() : "Reports must remain valid after addition";
        LOGGER.fine(() -> "Recorded broken item: " + brokenItem.getName() + " in category "
                + brokenItem.getCategory() + ", quantity=" + brokenItem.getQuantity()
                + ", active=" + brokenItems.size());
        return brokenItem;
    }

    /**
     * Restores all units and removes a repaired or incorrectly recorded report.
     * This operation is not for disposal or partial repairs. Deleted contacts do not block restoration.
     * Stock restoration must succeed before the report is removed.
     *
     * @param userIndex One-based report position from the latest full broken-item list.
     * @return Removed report for the caller's confirmation.
     * @throws CommandException If the index is invalid or restored stock would exceed the integer limit.
     */
    public BrokenItem deleteBrokenItem(int userIndex) throws CommandException {
        assert hasValidState() : "Deletion requires valid reports";
        if (userIndex <= 0 || userIndex > brokenItems.size()) {
            LOGGER.fine(() -> "Rejected broken-item deletion: index=" + userIndex
                    + ", active=" + brokenItems.size());
            throw new InvalidBrokenItemIndexException(userIndex, brokenItems.size());
        }

        int index = userIndex - INDEX_OFFSET;
        BrokenItem brokenItem = brokenItems.get(index);
        int previousStock = inventory.getAvailableQuantity(brokenItem.getName(), brokenItem.getCategory());
        inventory.restoreBrokenStock(brokenItem.getName(), brokenItem.getCategory(), brokenItem.getQuantity());
        assert inventory.getAvailableQuantity(brokenItem.getName(), brokenItem.getCategory())
                == (long) previousStock + brokenItem.getQuantity() : "Deletion must restore the exact broken quantity";
        brokenItems.remove(index);
        assert hasValidState() : "Reports must remain valid after deletion";
        LOGGER.fine(() -> "Deleted broken-item report and restored stock: index=" + userIndex
                + ", item=" + brokenItem.getName() + ", category=" + brokenItem.getCategory()
                + ", quantity=" + brokenItem.getQuantity() + ", active=" + brokenItems.size());
        return brokenItem;
    }

    /**
     * Returns an immutable snapshot in insertion order, without changing stock.
     * The caller displays positions starting at one, matching deletion indices.
     *
     * @return Active broken-item reports in display order.
     */
    public List<BrokenItem> getBrokenItems() {
        assert hasValidState() : "Listing requires valid reports";
        return List.copyOf(brokenItems);
    }

    /**
     * Resolves the selected contact against the current contact list when displaying a report.
     * Removing or renumbering other contacts cannot redirect this link to another contact.
     *
     * @param brokenItem Report whose selected contact should be checked.
     * @return Registered contact, or {@code null} if unassigned or deleted.
     * @throws NullPointerException If the report is null.
     */
    public Contact getAvailableContact(BrokenItem brokenItem) {
        Objects.requireNonNull(brokenItem, "Broken item must not be null");
        Contact contact = brokenItem.getContact();
        return contactManager.containsContact(contact) ? contact : null;
    }

    /**
     * Checks dependencies and stored reports, without requiring old contacts to remain registered.
     *
     * @return Whether the manager contains only valid reports and nonnull dependencies.
     */
    private boolean hasValidState() {
        if (inventory == null || contactManager == null) {
            return false;
        }
        for (BrokenItem brokenItem : brokenItems) {
            if (brokenItem == null || !brokenItem.hasValidState()) {
                return false;
            }
        }
        return true;
    }
}

package seedu.bob;

import java.util.ArrayList;
import java.util.List;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.CommandFormatException;
import seedu.bob.exception.InvalidHeadcountException;
import seedu.bob.exception.InvalidQuantityException;
import seedu.bob.exception.PreparationException;

/**
 * Stores the total quantities required by one session without reserving inventory.
 */
public class Preparation {

    /** Keeps insertion order so displayed item positions also identify items for deletion. */
    private final ArrayList<PreparationItem> items = new ArrayList<>();

    /**
     * Adds a per-attendee requirement, accumulating repeated name/category pairs without regard to case.
     *
     * @param name Item name, which may be absent from inventory.
     * @param category Inventory category containing the item.
     * @param quantityPerPerson Number of units needed by each attendee.
     * @param headcount Number of attendees in the session.
     * @return Updated item requirement.
     * @throws CommandException If input is invalid or the total would exceed the integer range.
     */
    public PreparationItem addItem(String name, String category, int quantityPerPerson, int headcount)
            throws CommandException {
        if (name == null || name.isBlank()) {
            throw new CommandFormatException("Item name cannot be blank.");
        }
        if (category == null || category.isBlank()) {
            throw new CommandFormatException("Category cannot be blank.");
        }
        if (quantityPerPerson <= 0) {
            throw new InvalidQuantityException();
        }
        if (headcount <= 0) {
            throw new InvalidHeadcountException();
        }

        int index = findItemIndex(name.trim(), category.trim());
        PreparationItem existing = index < 0 ? new PreparationItem(name.trim(), category.trim(), 0) : items.get(index);
        int requiredQuantity;
        try {
            int addedQuantity = Math.multiplyExact(quantityPerPerson, headcount);
            requiredQuantity = Math.addExact(existing.getQuantity(), addedQuantity);
        } catch (ArithmeticException e) {
            throw new PreparationException("Required quantity must not exceed " + Integer.MAX_VALUE + ".");
        }
        PreparationItem updated = new PreparationItem(existing.getName(), existing.getCategory(), requiredQuantity);
        if (index < 0) {
            items.add(updated);
        } else {
            items.set(index, updated);
        }
        return updated;
    }

    /**
     * Finds an existing name/category pair, or returns -1 when the requirement is new.
     */
    private int findItemIndex(String name, String category) {
        for (int i = 0; i < items.size(); i++) {
            PreparationItem item = items.get(i);
            if (item.getName().equalsIgnoreCase(name) && item.getCategory().equalsIgnoreCase(category)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Removes a total quantity, deleting the item when no units remain required.
     *
     * @param userIndex One-based position in the preparation list.
     * @param quantity Total units to remove, not units per attendee.
     * @return Item requirement before the removal.
     * @throws CommandException If the position or quantity is invalid, or removal exceeds the requirement.
     */
    public PreparationItem removeItem(int userIndex, int quantity) throws CommandException {
        if (userIndex <= 0 || userIndex > items.size()) {
            throw new PreparationException(String.format(
                    "Preparation item index %d is invalid. There are %d item(s) in this preparation.",
                    userIndex, items.size()));
        }
        if (quantity <= 0) {
            throw new InvalidQuantityException();
        }

        int index = userIndex - 1;
        PreparationItem item = items.get(index);
        int requiredQuantity = item.getQuantity();
        if (quantity > requiredQuantity) {
            throw new PreparationException(String.format(
                    "Cannot remove %dx %s from preparation; only %d required.", quantity, item, requiredQuantity));
        }

        if (quantity == requiredQuantity) {
            items.remove(index);
        } else {
            items.set(index, new PreparationItem(item.getName(), item.getCategory(), requiredQuantity - quantity));
        }
        return item;
    }

    /**
     * Returns an immutable snapshot of required totals in displayed order.
     *
     * @return Required items and their total quantities.
     */
    public List<PreparationItem> getItems() {
        return List.copyOf(items);
    }
}

package seedu.bob;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Objects;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.CommandFormatException;
import seedu.bob.exception.InvalidQuantityException;
import seedu.bob.exception.LoanException;

/**
 * Represents one immutable loan with its own item quantity, borrower, and explicit due date.
 */
public class Loan {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("d MMMM uuuu", Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.STRICT);

    /** Private item snapshot, never shared with mutable inventory stock or exposed to callers. */
    private final InventoryItem item;
    /** Borrower details belong to this loan and are independent of the supplier contact manager. */
    private final Contact borrower;
    private final LocalDate dueDate;

    /**
     * Creates a validated loan without changing inventory. Past due dates are accepted for backlog entries.
     *
     * @param name Item name.
     * @param category Item category.
     * @param quantity Total units being loaned.
     * @param borrower Borrower details, validated by {@link Contact}.
     * @param dueDateText Due date in {@code d MMMM uuuu} format.
     * @throws CommandException If the item details, quantity, or due date are invalid.
     * @throws NullPointerException If the borrower is null.
     */
    public Loan(String name, String category, int quantity, Contact borrower, String dueDateText)
            throws CommandException {
        if (name == null || name.isBlank()) {
            throw new CommandFormatException("Item name cannot be blank.");
        }
        if (category == null || category.isBlank()) {
            throw new CommandFormatException("Category cannot be blank.");
        }
        if (quantity <= 0) {
            throw new InvalidQuantityException();
        }
        this.borrower = Objects.requireNonNull(borrower, "Loan borrower must not be null");
        if (dueDateText == null) {
            throw new LoanException("Due date must be a valid date in d MMMM yyyy format (e.g., 30 October 2099).");
        }
        try {
            dueDate = LocalDate.parse(dueDateText, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new LoanException("Due date must be a valid date in d MMMM yyyy format (e.g., 30 October 2099).");
        }
        item = new InventoryItem(name, category, quantity);
    }

    public String getName() {
        return item.getName();
    }

    public String getCategory() {
        return item.getCategory();
    }

    public int getQuantity() {
        return item.getQuantity();
    }

    public Contact getBorrower() {
        return borrower;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Returns the item, quantity, borrower details, and due date for a confirmation or list row.
     */
    @Override
    public String toString() {
        return String.format("%dx %s [%s] to %s (%s), due %s", getQuantity(), getName(), getCategory(),
                borrower.getName(), borrower.getPhone(), dueDate.format(DATE_FORMATTER));
    }
}

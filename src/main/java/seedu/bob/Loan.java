//@@author DaynaG3
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

    private static final String INVALID_DUE_DATE_MESSAGE =
            "Due date must be a valid date in d MMMM yyyy format (e.g., 30 October 2099).";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("d MMMM uuuu", Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.STRICT);

    /** Private item snapshot, never shared with mutable inventory stock or exposed to callers. */
    private final InventoryItem item;
    /** Captured values never refer back to a contact that may later change elsewhere. */
    private final String borrowerName;
    private final String borrowerPhone;
    private final LocalDate dueDate;

    /**
     * Creates a validated loan without changing inventory. Past due dates are accepted for backlog entries.
     *
     * @param name Item name.
     * @param category Item category.
     * @param quantity Total units being loaned.
     * @param borrower Contact whose name and phone are captured and validated for this loan.
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
            throw new InvalidQuantityException(InvalidQuantityException.NON_POSITIVE_MESSAGE);
        }
        Objects.requireNonNull(borrower, "Loan borrower must not be null");
        String capturedName = borrower.getName();
        String capturedPhone = borrower.getPhone();
        // Reuse contact validation, including when a mutable subclass supplies the captured values.
        new Contact(capturedName, capturedPhone, null, null);
        borrowerName = capturedName;
        borrowerPhone = capturedPhone;
        if (dueDateText == null) {
            throw new LoanException(INVALID_DUE_DATE_MESSAGE);
        }
        try {
            dueDate = LocalDate.parse(dueDateText, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new LoanException(INVALID_DUE_DATE_MESSAGE, e);
        }
        item = new InventoryItem(name, category, quantity);
        assert hasValidState() : "A constructed loan must contain valid item, borrower, and date details";
    }

    /**
     * Returns whether the immutable record satisfies its item, borrower, and due-date invariants.
     * Past dates and records matching another loan are deliberately valid.
     */
    boolean hasValidState() {
        return item != null && item.getName() != null && !item.getName().isBlank()
                && item.getCategory() != null && !item.getCategory().isBlank() && item.getQuantity() > 0
                && borrowerName != null && !borrowerName.isBlank()
                && borrowerPhone != null && borrowerPhone.matches("\\d+") && dueDate != null;
    }

    /**
     * Returns the recorded item name, preserving the spelling supplied when the loan was created.
     *
     * @return Loaned item name.
     */
    public String getName() {
        return item.getName();
    }

    /**
     * Returns the recorded item category, independently of later inventory changes.
     *
     * @return Loaned item category.
     */
    public String getCategory() {
        return item.getCategory();
    }

    /**
     * Returns the full quantity recorded for this loan; partial returns are not supported.
     *
     * @return Total units in the loan record.
     */
    public int getQuantity() {
        return item.getQuantity();
    }

    /**
     * Returns the borrower name captured when this loan was constructed.
     *
     * @return Immutable borrower name snapshot.
     */
    public String getBorrowerName() {
        return borrowerName;
    }

    /**
     * Returns the borrower phone captured when this loan was constructed, preserving leading zeros.
     *
     * @return Immutable borrower phone snapshot.
     */
    public String getBorrowerPhone() {
        return borrowerPhone;
    }

    /**
     * Returns the last calendar date on which this loan is considered on time.
     *
     * @return Inclusive due date.
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Returns whether this loan's due date is before the supplied local calendar date.
     * A loan due today remains on time until the next day.
     *
     * @param today Local date against which to evaluate the loan.
     * @return Whether the loan is overdue.
     * @throws NullPointerException If today is null.
     */
    public boolean isOverdue(LocalDate today) {
        assert hasValidState() : "Overdue checks require a valid loan record";
        return dueDate.isBefore(Objects.requireNonNull(today, "Today's date must not be null"));
    }

    /**
     * Returns the item, quantity, borrower details, and due date for a confirmation or list row.
     */
    @Override
    public String toString() {
        return String.format("%dx %s [%s] to %s (%s), due %s", getQuantity(), getName(), getCategory(),
                borrowerName, borrowerPhone, dueDate.format(DATE_FORMATTER));
    }
}

//@@author gavinlimsh
package seedu.bob;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Objects;

import seedu.bob.exception.BrokenItemException;

/**
 * Stores one validated report of broken units and an optional maintenance contact.
 * Construction does not change inventory; stock changes belong to the manager.
 */
public final class BrokenItem {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("d MMMM uuuu", Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.STRICT);
    private static final String INVALID_DATE_MESSAGE =
            "Date reported must be a valid date in d MMMM yyyy format (e.g., 23 October 2026).";

    private final String name;
    private final String category;
    private final int quantity;
    private final String faultDescription;
    private final LocalDate reportedDate;

    /** Selected contact object, rather than its changing list index; null means unassigned. */
    private final Contact contact;

    /**
     * Creates a report using the system's local date to reject future report dates.
     *
     * @param name Item name.
     * @param category Item category.
     * @param quantity Positive number of broken units.
     * @param faultDescription Description of the fault.
     * @param reportedDateText Date reported in {@code d MMMM uuuu} format.
     * @param contact Selected maintenance contact, or {@code null} if unassigned.
     * @throws BrokenItemException If text is blank, quantity is not positive, or the date is invalid or future.
     */
    public BrokenItem(String name, String category, int quantity, String faultDescription,
                      String reportedDateText, Contact contact) throws BrokenItemException {
        this(name, category, quantity, faultDescription, reportedDateText, contact, Clock.systemDefaultZone());
    }

    /**
     * Creates a report using an explicit clock for deterministic date validation in tests.
     *
     * @param name Item name.
     * @param category Item category.
     * @param quantity Positive number of broken units.
     * @param faultDescription Description of the fault.
     * @param reportedDateText Date reported in {@code d MMMM uuuu} format.
     * @param contact Selected maintenance contact, or {@code null} if unassigned.
     * @param clock Clock defining today's date.
     * @throws BrokenItemException If text is blank, quantity is not positive, or the date is invalid or future.
     * @throws NullPointerException If the clock is null.
     */
    BrokenItem(String name, String category, int quantity, String faultDescription,
               String reportedDateText, Contact contact, Clock clock) throws BrokenItemException {
        Objects.requireNonNull(clock, "Clock must not be null");
        this.name = requireText(name, "Item name");
        this.category = requireText(category, "Category");
        this.faultDescription = requireText(faultDescription, "Fault description");
        if (quantity <= 0) {
            throw new BrokenItemException("Broken quantity must be greater than zero.");
        }
        this.quantity = quantity;

        String dateText = requireText(reportedDateText, "Date reported");
        try {
            this.reportedDate = LocalDate.parse(dateText, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BrokenItemException(INVALID_DATE_MESSAGE, e);
        }
        if (reportedDate.isAfter(LocalDate.now(clock))) {
            throw new BrokenItemException("Date reported cannot be in the future.");
        }
        this.contact = contact;

        assert hasValidState() : "A constructed broken-item report must contain valid details";
    }

    /**
     * Rejects null or blank text and removes surrounding whitespace.
     *
     * @param value Text to validate.
     * @param fieldName Field name to include in an error message.
     * @return Nonblank text with surrounding whitespace removed.
     * @throws BrokenItemException If the text is null or blank.
     */
    private static String requireText(String value, String fieldName) throws BrokenItemException {
        if (value == null || value.isBlank()) {
            throw new BrokenItemException(fieldName + " cannot be blank.");
        }
        return value.strip();
    }

    /**
     * Checks stored report fields without changing state or consulting the current clock.
     * Contact-list membership is checked separately by the manager.
     *
     * @return Whether the report contains nonblank text, a positive quantity, and a date.
     */
    boolean hasValidState() {
        return name != null && !name.isBlank()
                && category != null && !category.isBlank()
                && faultDescription != null && !faultDescription.isBlank()
                && quantity > 0 && reportedDate != null;
    }

    /**
     * Returns the item name.
     *
     * @return Item name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the item category.
     *
     * @return Item category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Returns the number of broken units represented by this report.
     *
     * @return Positive broken quantity.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the fault description.
     *
     * @return Nonblank description of the fault.
     */
    public String getFaultDescription() {
        return faultDescription;
    }

    /**
     * Returns the date on which the fault was reported.
     *
     * @return Reported date.
     */
    public LocalDate getReportedDate() {
        return reportedDate;
    }

    /**
     * Returns the selected contact independently of later changes to its displayed index.
     * This does not guarantee that the contact is still registered in the contact manager.
     *
     * @return Selected contact, or {@code null} if none was assigned.
     */
    public Contact getContact() {
        return contact;
    }

    /**
     * Describes the report; contact availability is displayed separately by the caller.
     *
     * @return Item, quantity, fault description, and reported date.
     */
    @Override
    public String toString() {
        return String.format("%dx %s [%s], fault: %s, reported %s",
                quantity, name, category, faultDescription, reportedDate.format(DATE_FORMATTER));
    }
}

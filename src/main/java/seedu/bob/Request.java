package seedu.bob;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.CommandFormatException;
import seedu.bob.exception.InvalidQuantityException;
import seedu.bob.exception.InvalidRequestDeadlineException;
import seedu.bob.exception.InvalidRequestStatusException;

/**
 * Represents a student's request for a quantity of an item by a deadline.
 * Implements Comparable so requests can be displayed in deadline order.
 */
public class Request implements Comparable<Request> {

    /** Status values stored in lowercase so they can be printed in confirmations. */
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_PREPARED = "prepared";
    private static final String STATUS_FULFILLED = "fulfilled";

    /** Strict English format used for both parsing and displaying the deadline. */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("d MMMM uuuu", Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.STRICT);

    /** Records request diagnostics at FINE level so normal console output stays unchanged. */
    private static final Logger LOGGER = Logger.getLogger(Request.class.getName());

    private final String studentName;
    private final String itemName;
    private final int quantity;
    private final LocalDate deadline;

    /** Current status, initially pending and changed only through {@link #markAs(String)}. */
    private String status = STATUS_PENDING;

    /**
     * Creates a validated student request.
     *
     * @param studentName Name of the student making the request; surrounding whitespace is ignored.
     * @param itemName Name of the requested item; surrounding whitespace is ignored.
     * @param quantity Number of units requested.
     * @param deadlineText Deadline in {@code d MMMM uuuu} format; surrounding whitespace is ignored.
     * @throws CommandException If a value is blank, the quantity is not positive, or the deadline is invalid.
     */
    public Request(String studentName, String itemName, int quantity, String deadlineText)
            throws CommandException {
        this(studentName, itemName, quantity, deadlineText, Clock.systemDefaultZone());
    }

    /**
     * Creates a validated student request using the supplied clock for the today check.
     *
     * @param studentName Name of the student making the request; surrounding whitespace is ignored.
     * @param itemName Name of the requested item; surrounding whitespace is ignored.
     * @param quantity Number of units requested.
     * @param deadlineText Deadline in {@code d MMMM uuuu} format; surrounding whitespace is ignored.
     * @param clock Clock used to determine today's date.
     * @throws CommandException If a value is blank, the quantity is not positive, or the deadline is invalid.
     */
    Request(String studentName, String itemName, int quantity, String deadlineText, Clock clock)
            throws CommandException {
        //@@author DaynaG3
        this(studentName, itemName, quantity, deadlineText, clock, false);
    }

    /** Reuses validation while retaining historical deadlines and saved display text during restoration. */
    private Request(String studentName, String itemName, int quantity, String deadlineText, Clock clock,
            boolean isRestoring) throws CommandException {
        //@@author
        Objects.requireNonNull(clock, "Clock must not be null");
        String trimmedStudentName = studentName == null ? "" : studentName.trim();
        String trimmedItemName = itemName == null ? "" : itemName.trim();
        if (trimmedStudentName.isEmpty()) {
            throw new CommandFormatException("Student name cannot be blank.");
        }
        if (trimmedItemName.isEmpty()) {
            throw new CommandFormatException("Item name cannot be blank.");
        }
        if (quantity <= 0) {
            LOGGER.fine(() -> "Rejected request quantity: " + quantity);
            throw new InvalidQuantityException(InvalidQuantityException.NON_POSITIVE_MESSAGE);
        }

        String trimmedDeadlineText = deadlineText == null ? "" : deadlineText.trim();
        if (trimmedDeadlineText.isEmpty()) {
            LOGGER.fine(() -> "Rejected request deadline: " + deadlineText);
            throw new InvalidRequestDeadlineException(
                    InvalidRequestDeadlineException.INVALID_FORMAT_MESSAGE);
        }
        LocalDate parsedDeadline;
        try {
            parsedDeadline = LocalDate.parse(trimmedDeadlineText, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            LOGGER.fine(() -> "Rejected request deadline: " + deadlineText);
            throw new InvalidRequestDeadlineException(
                    InvalidRequestDeadlineException.INVALID_FORMAT_MESSAGE);
        }
        // A deadline is a calendar date, so requests due today are still accepted.
        //@@author DaynaG3
        if (!isRestoring && parsedDeadline.isBefore(LocalDate.now(clock))) {
            //@@author
            LOGGER.fine(() -> "Rejected request deadline: " + deadlineText);
            throw new InvalidRequestDeadlineException(
                    InvalidRequestDeadlineException.PAST_DATE_MESSAGE);
        }

        //@@author DaynaG3
        this.studentName = isRestoring ? studentName : trimmedStudentName;
        this.itemName = isRestoring ? itemName : trimmedItemName;
        //@@author
        this.quantity = quantity;
        this.deadline = parsedDeadline;

        assert !this.studentName.isEmpty() : "Validated student name must not be blank";
        assert !this.itemName.isEmpty() : "Validated item name must not be blank";
        assert quantity > 0 : "Validated quantity must be positive";
        assert deadline != null : "Validated deadline must not be null";
        LOGGER.fine(() -> "Created request for " + this.studentName + " for " + this.itemName);
        //@@author DaynaG3
    }

    /** Restores historical requests and exact saved statuses without applying a command status transition. */
    static Request restore(AppSnapshot.RequestEntry entry) throws CommandException {
        if (entry.studentName() == null || entry.studentName().isBlank()
                || entry.itemName() == null || entry.itemName().isBlank()) {
            throw new CommandFormatException("Saved student and item names must not be blank.");
        }
        String savedStatus = entry.status();
        if (!STATUS_PENDING.equals(savedStatus) && !STATUS_PREPARED.equals(savedStatus)
                && !STATUS_FULFILLED.equals(savedStatus)) {
            throw new InvalidRequestStatusException();
        }
        Request request = new Request(entry.studentName(), entry.itemName(), entry.quantity(),
                SnapshotFormat.toCommandDate(entry.deadline()), Clock.systemDefaultZone(), true);
        request.status = savedStatus;
        return request;
    }

    /** Exports the current status with the original deadline, not a derived availability value. */
    AppSnapshot.RequestEntry toSnapshot() {
        return new AppSnapshot.RequestEntry(studentName, itemName, quantity, deadline.toString(), status);
        //@@author
    }

    public String getStudentName() {
        return studentName;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public String getStatus() {
        return status;
    }

    /**
     * Normalizes and validates a status entered by the user.
     *
     * @param newStatus Status text, ignoring case.
     * @return Lowercase status.
     * @throws InvalidRequestStatusException If the status is not prepared or fulfilled.
     */
    public static String normalizeStatus(String newStatus) throws InvalidRequestStatusException {
        // Normalize once so callers may type prepared, Prepared, or PREPARED.
        String normalizedStatus = newStatus == null ? "" : newStatus.trim().toLowerCase(Locale.ENGLISH);
        if (!STATUS_PREPARED.equals(normalizedStatus) && !STATUS_FULFILLED.equals(normalizedStatus)) {
            LOGGER.fine(() -> "Rejected request status: " + newStatus);
            throw new InvalidRequestStatusException();
        }
        return normalizedStatus;
    }

    /**
     * Marks this request as prepared or fulfilled.
     *
     * @param newStatus New status, which must be {@code prepared} or {@code fulfilled}, ignoring case.
     * @throws InvalidRequestStatusException If the status is not one of the two accepted values.
     */
    public void markAs(String newStatus) throws InvalidRequestStatusException {
        this.status = normalizeStatus(newStatus);

        assert STATUS_PREPARED.equals(status) || STATUS_FULFILLED.equals(status)
                : "Validated request status must be prepared or fulfilled";
        LOGGER.fine(() -> "Marked request for " + studentName + " as " + status);
    }

    /**
     * Compares requests by deadline, then by student name and item name for deterministic ordering.
     *
     * @param other Request to compare with.
     * @return A negative integer, zero, or a positive integer as this request is earlier than,
     *         tied with, or later than the other request.
     */
    @Override
    public int compareTo(Request other) {
        int deadlineComparison = deadline.compareTo(other.deadline);
        if (deadlineComparison != 0) {
            return deadlineComparison;
        }

        int studentComparison = studentName.compareToIgnoreCase(other.studentName);
        if (studentComparison != 0) {
            return studentComparison;
        }
        // Names break deadline ties so listing order does not depend on insertion order.
        return itemName.compareToIgnoreCase(other.itemName);
    }

    /**
     * Returns the request in the form {@code STUDENT - QUANTITYx ITEM by DEADLINE}.
     *
     * @return User-facing description of this request.
     */
    @Override
    public String toString() {
        return String.format("%s - %dx %s by %s",
                studentName, quantity, itemName, deadline.format(DATE_FORMATTER));
    }
}

//@@author Lokwx
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.CommandFormatException;
import seedu.bob.exception.InvalidQuantityException;
import seedu.bob.exception.InvalidRequestDeadlineException;
import seedu.bob.exception.InvalidRequestStatusException;

/**
 * Verifies validation, status changes, and ordering of the {@link Request} model.
 */
class RequestTest {

    @Test
    void constructor_validValues_storesFieldsAndStartsPending() throws CommandException {
        LocalDate expectedDeadline = LocalDate.now().plusDays(5);
        String deadline = formatDate(expectedDeadline);
        Request request = new Request("Alice Tan", "Wire", 20, deadline);

        assertEquals("Alice Tan", request.getStudentName());
        assertEquals("Wire", request.getItemName());
        assertEquals(20, request.getQuantity());
        assertEquals(expectedDeadline, request.getDeadline());
        assertEquals("pending", request.getStatus());
        assertEquals("Alice Tan - 20x Wire by " + deadline, request.toString());
    }

    @Test
    void constructor_todayDeadline_accepted() throws CommandException {
        LocalDate today = LocalDate.now();
        Request request = new Request("Alice", "Wire", 1, formatDate(today));

        assertEquals(today, request.getDeadline());
    }

    @Test
    void constructor_blankStudentName_throwsCommandFormatException() {
        CommandFormatException exception = assertThrows(CommandFormatException.class,
                () -> new Request("  ", "Wire", 1, date(2)));
        assertEquals("Student name cannot be blank.", exception.getMessage());
        assertThrows(CommandFormatException.class,
                () -> new Request(null, "Wire", 1, date(2)));
    }

    @Test
    void constructor_blankItemName_throwsCommandFormatException() {
        CommandFormatException exception = assertThrows(CommandFormatException.class,
                () -> new Request("Alice", "  ", 1, date(2)));
        assertEquals("Item name cannot be blank.", exception.getMessage());
        assertThrows(CommandFormatException.class,
                () -> new Request("Alice", null, 1, date(2)));
    }

    @Test
    void constructor_nonPositiveQuantity_throwsInvalidQuantityException() {
        assertThrows(InvalidQuantityException.class,
                () -> new Request("Alice", "Wire", 0, date(2)));
        assertThrows(InvalidQuantityException.class,
                () -> new Request("Alice", "Wire", -1, date(2)));
    }

    @Test
    void constructor_invalidDeadlineFormat_throwsInvalidRequestDeadlineException() {
        String[] invalidDeadlines = {"31 February 2099", "2 october 2050", "2099-10-02", "", "   "};
        for (String deadline : invalidDeadlines) {
            InvalidRequestDeadlineException exception = assertThrows(
                    InvalidRequestDeadlineException.class,
                    () -> new Request("Alice", "Wire", 1, deadline), deadline);
            assertEquals("Deadline must be a valid date in d MMMM yyyy format.",
                    exception.getMessage());
        }
    }

    @Test
    void constructor_nullDeadline_throwsInvalidRequestDeadlineException() {
        InvalidRequestDeadlineException exception = assertThrows(
                InvalidRequestDeadlineException.class,
                () -> new Request("Alice", "Wire", 1, null));

        assertEquals("Deadline must be a valid date in d MMMM yyyy format.", exception.getMessage());
    }

    @Test
    void constructor_pastDeadline_throwsInvalidRequestDeadlineException() {
        InvalidRequestDeadlineException exception = assertThrows(
                InvalidRequestDeadlineException.class,
                () -> new Request("Alice", "Wire", 1, "1 January 2000"));

        assertEquals("Request deadline cannot be before today.", exception.getMessage());
    }

    @Test
    void constructor_fixedClock_usesInjectedTodayForPastDeadlineCheck() throws CommandException {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-10-22T00:00:00Z"), ZoneOffset.UTC);

        Request request = new Request("Alice", "Wire", 1, "22 October 2026", fixedClock);

        assertEquals(LocalDate.of(2026, 10, 22), request.getDeadline());
        assertThrows(InvalidRequestDeadlineException.class,
                () -> new Request("Alice", "Wire", 1, "21 October 2026", fixedClock));
    }

    @Test
    void constructor_surroundingWhitespace_trimsStoredValues() throws CommandException {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-10-22T00:00:00Z"), ZoneOffset.UTC);

        Request request = new Request("  Alice Tan  ", "  Wire  ", 2, "  22 October 2026  ", fixedClock);

        assertEquals("Alice Tan", request.getStudentName());
        assertEquals("Wire", request.getItemName());
        assertEquals(LocalDate.of(2026, 10, 22), request.getDeadline());
    }

    @Test
    void constructor_nullClock_throwsNullPointerException() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> new Request("Alice", "Wire", 1, date(2), null));

        assertEquals("Clock must not be null", exception.getMessage());
    }

    @Test
    void markAs_preparedAndFulfilled_ignoresCase() throws CommandException {
        Request request = new Request("Alice", "Wire", 1, date(2));

        request.markAs("Prepared");
        assertEquals("prepared", request.getStatus());

        request.markAs("FULFILLED");
        assertEquals("fulfilled", request.getStatus());
    }

    @Test
    void markAs_fulfilled_isFulfilledStatus() throws CommandException {
        Request request = new Request("Alice", "Wire", 1, date(2));

        assertFalse(request.isFulfilled());
        assertFalse(Request.isFulfilledStatus("prepared"));
        assertFalse(Request.isFulfilledStatus("pending"));
        assertTrue(Request.isFulfilledStatus("fulfilled"));

        request.markAs("fulfilled");

        assertTrue(request.isFulfilled());
    }

    @Test
    void markAs_invalidStatus_throwsAndPreservesCurrentStatus() throws CommandException {
        Request request = new Request("Alice", "Wire", 1, date(2));

        InvalidRequestStatusException exception = assertThrows(InvalidRequestStatusException.class,
                () -> request.markAs("pending"));
        assertEquals("Status must be \"prepared\" or \"fulfilled\".", exception.getMessage());
        assertEquals("pending", request.getStatus());

        assertThrows(InvalidRequestStatusException.class, () -> request.markAs(null));
        assertThrows(InvalidRequestStatusException.class, () -> request.markAs("   "));
        assertEquals("pending", request.getStatus());

        request.markAs("prepared");
        assertThrows(InvalidRequestStatusException.class, () -> request.markAs("done"));
        assertEquals("prepared", request.getStatus());
    }

    @Test
    void compareTo_differentDeadlines_earlierDeadlineFirst() throws CommandException {
        Request earlier = new Request("Alice", "Wire", 1, date(2));
        Request later = new Request("Bob", "Battery", 1, date(5));

        assertTrue(earlier.compareTo(later) < 0);
        assertTrue(later.compareTo(earlier) > 0);
    }

    @Test
    void compareTo_sameDeadline_sortsByStudentName() throws CommandException {
        String deadline = date(2);
        Request alice = new Request("Alice", "Wire", 1, deadline);
        Request bob = new Request("Bob", "Battery", 1, deadline);

        assertTrue(alice.compareTo(bob) < 0);
        assertTrue(bob.compareTo(alice) > 0);
    }

    @Test
    void compareTo_sameDeadlineAndStudent_sortsByItemName() throws CommandException {
        String deadline = date(2);
        Request battery = new Request("Alice", "Battery", 1, deadline);
        Request wire = new Request("Alice", "Wire", 1, deadline);

        assertTrue(battery.compareTo(wire) < 0);
        assertTrue(wire.compareTo(battery) > 0);
    }

    @Test
    void compareTo_sameDeadlineStudentAndItem_returnsZero() throws CommandException {
        String deadline = date(2);
        Request first = new Request("Alice", "Wire", 1, deadline);
        Request second = new Request("Alice", "Wire", 2, deadline);

        assertEquals(0, first.compareTo(second));
    }

    /**
     * Returns a date the given number of days from today in the documented format.
     */
    private static String date(int daysFromToday) {
        return formatDate(LocalDate.now().plusDays(daysFromToday));
    }

    /**
     * Formats a date using the same pattern as {@link Request}.
     */
    private static String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH));
    }
}

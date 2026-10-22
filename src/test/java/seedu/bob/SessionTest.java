//@@author gavinlimsh
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.CommandFormatException;
import seedu.bob.exception.InvalidHeadcountException;
import seedu.bob.exception.InvalidSessionDateException;
import seedu.bob.exception.InvalidSessionTimeException;

/**
 * Tests session validation, stored values, conflict detection, sorting, and display text.
 */
class SessionTest {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH);

    /** Keeps valid test dates safely in the future without relying on a fixed calendar year. */
    private final LocalDate futureDate = LocalDate.now().plusDays(7);
    private final String futureDateText = futureDate.format(DATE_FORMATTER);

    /**
     * Verifies that all supplied session details are stored correctly.
     */
    @Test
    void constructor_validParameters_storesAllDetails() throws CommandException {
        Session session = new Session(
                "CG2111A", futureDateText, "E4A-04-08", "0900", "1200", 50);

        assertEquals("CG2111A", session.getName());
        assertEquals(futureDate, session.getDate());
        assertEquals("E4A-04-08", session.getLocation());
        assertEquals(LocalTime.of(9, 0), session.getStartTime());
        assertEquals(LocalTime.of(12, 0), session.getEndTime());
        assertEquals(50, session.getHeadcount());
    }

    /**
     * Verifies that null, empty, and whitespace-only names are rejected.
     */
    @Test
    void constructor_invalidNames_throwsCommandFormatException() {
        String[] invalidNames = {null, "", "   "};

        for (String name : invalidNames) {
            CommandFormatException exception = assertThrows(CommandFormatException.class,
                    () -> new Session(name, futureDateText, "E4A-04-08", "0900", "1200", 50));

            assertEquals("Session name cannot be blank.", exception.getMessage());
        }
    }

    /**
     * Verifies that null, empty, and whitespace-only locations are rejected.
     */
    @Test
    void constructor_invalidLocations_throwsCommandFormatException() {
        String[] invalidLocations = {null, "", "   "};

        for (String location : invalidLocations) {
            CommandFormatException exception = assertThrows(CommandFormatException.class,
                    () -> new Session("CG2111A", futureDateText, location, "0900", "1200", 50));

            assertEquals("Session location cannot be blank.", exception.getMessage());
        }
    }

    /**
     * Verifies that zero and negative headcounts are rejected.
     */
    @Test
    void constructor_nonPositiveHeadcounts_throwsInvalidHeadcountException() {
        int[] invalidHeadcounts = {0, -1};

        for (int headcount : invalidHeadcounts) {
            InvalidHeadcountException exception = assertThrows(InvalidHeadcountException.class,
                    () -> new Session("CG2111A", futureDateText, "E4A-04-08",
                            "0900", "1200", headcount));

            assertEquals("Headcount must be a positive integer.", exception.getMessage());
        }
    }

    /**
     * Verifies that a date in the past is rejected.
     */
    @Test
    void constructor_pastDate_throwsInvalidSessionDateException() {
        String pastDateText = LocalDate.now().minusDays(1).format(DATE_FORMATTER);

        InvalidSessionDateException exception = assertThrows(InvalidSessionDateException.class,
                () -> new Session("CG2111A", pastDateText, "E4A-04-08", "0900", "1200", 50));

        assertEquals("Session date cannot be before today.", exception.getMessage());
    }

    /**
     * Verifies that empty, incorrectly formatted, and impossible dates are rejected.
     */
    @Test
    void constructor_invalidDates_throwsInvalidSessionDateException() {
        List<String> invalidDates = List.of(
                "",
                futureDateText.toLowerCase(Locale.ENGLISH),
                futureDate.toString(),
                "31 February " + futureDate.getYear());

        for (String dateText : invalidDates) {
            InvalidSessionDateException exception = assertThrows(InvalidSessionDateException.class,
                    () -> new Session("CG2111A", dateText, "E4A-04-08", "0900", "1200", 50));

            assertEquals(
                    "Date must be in the format 'd MMMM yyyy' (e.g., 22 November 2027). "
                            + "Ensure the month is capitalized.",
                    exception.getMessage());
        }
    }

    /**
     * Verifies that malformed and out-of-range start times are rejected.
     */
    @Test
    void constructor_invalidStartTimes_throwsInvalidSessionTimeException() {
        String[] invalidTimes = {"", "9 AM", "900", "2400", "0960"};

        for (String startTime : invalidTimes) {
            InvalidSessionTimeException exception = assertThrows(InvalidSessionTimeException.class,
                    () -> new Session("CG2111A", futureDateText, "E4A-04-08",
                            startTime, "1200", 50));

            assertEquals(
                    "Start time must be in 'HHmm' format (e.g., 0900 for 9:00 AM).",
                    exception.getMessage());
        }
    }

    /**
     * Verifies that malformed and out-of-range end times are rejected.
     */
    @Test
    void constructor_invalidEndTimes_throwsInvalidSessionTimeException() {
        String[] invalidTimes = {"", "12 PM", "900", "2400", "1260"};

        for (String endTime : invalidTimes) {
            InvalidSessionTimeException exception = assertThrows(InvalidSessionTimeException.class,
                    () -> new Session("CG2111A", futureDateText, "E4A-04-08",
                            "0900", endTime, 50));

            assertEquals(
                    "End time must be in 'HHmm' format (e.g., 1200 for 12:00 PM).",
                    exception.getMessage());
        }
    }

    /**
     * Verifies that the end time must be strictly later than the start time.
     */
    @Test
    void constructor_endAtOrBeforeStart_throwsInvalidSessionTimeException() {
        String[] invalidEndTimes = {"0900", "0800"};

        for (String endTime : invalidEndTimes) {
            InvalidSessionTimeException exception = assertThrows(InvalidSessionTimeException.class,
                    () -> new Session("CG2111A", futureDateText, "E4A-04-08",
                            "0900", endTime, 50));

            assertEquals("Start time must be earlier than end time.", exception.getMessage());
        }
    }

    /**
     * Verifies that valid times at the limits of a day are accepted.
     */
    @Test
    void constructor_dayBoundaryTimes_storesCorrectTimes() throws CommandException {
        Session session = new Session(
                "Full Day Lab", futureDateText, "Room-A", "0000", "2359", 1);

        assertEquals(LocalTime.MIDNIGHT, session.getStartTime());
        assertEquals(LocalTime.of(23, 59), session.getEndTime());
        assertEquals(1, session.getHeadcount());
    }

    /**
     * Verifies that overlapping sessions conflict in either comparison direction.
     */
    @Test
    void conflictsWith_overlappingTimes_returnsTrue() throws CommandException {
        Session first = new Session(
                "First Lab", futureDateText, "Room-A", "0900", "1200", 50);
        Session second = new Session(
                "Second Lab", futureDateText, "Room-A", "1000", "1300", 50);

        assertTrue(first.conflictsWith(second));
        assertTrue(second.conflictsWith(first));
    }

    /**
     * Verifies that location matching ignores capitalization.
     */
    @Test
    void conflictsWith_differentLocationCase_returnsTrue() throws CommandException {
        Session first = new Session(
                "First Lab", futureDateText, "Room-A", "0900", "1200", 50);
        Session second = new Session(
                "Second Lab", futureDateText, "room-a", "0900", "1200", 50);

        assertTrue(first.conflictsWith(second));
        assertTrue(second.conflictsWith(first));
    }

    /**
     * Verifies that a session wholly inside another session's time range conflicts.
     */
    @Test
    void conflictsWith_containedTimeRange_returnsTrue() throws CommandException {
        Session outer = new Session(
                "Long Lab", futureDateText, "Room-A", "0900", "1500", 50);
        Session inner = new Session(
                "Short Lab", futureDateText, "Room-A", "1000", "1100", 50);

        assertTrue(outer.conflictsWith(inner));
        assertTrue(inner.conflictsWith(outer));
    }

    /**
     * Verifies that overlapping sessions at different locations do not conflict.
     */
    @Test
    void conflictsWith_differentLocations_returnsFalse() throws CommandException {
        Session first = new Session(
                "First Lab", futureDateText, "Room-A", "0900", "1200", 50);
        Session second = new Session(
                "Second Lab", futureDateText, "Room-B", "0900", "1200", 50);

        assertFalse(first.conflictsWith(second));
        assertFalse(second.conflictsWith(first));
    }

    /**
     * Verifies that matching locations and times on different dates do not conflict.
     */
    @Test
    void conflictsWith_differentDates_returnsFalse() throws CommandException {
        String laterDateText = futureDate.plusDays(1).format(DATE_FORMATTER);
        Session first = new Session(
                "First Lab", futureDateText, "Room-A", "0900", "1200", 50);
        Session second = new Session(
                "Second Lab", laterDateText, "Room-A", "0900", "1200", 50);

        assertFalse(first.conflictsWith(second));
        assertFalse(second.conflictsWith(first));
    }

    /**
     * Verifies that one session may begin exactly when another ends.
     */
    @Test
    void conflictsWith_adjacentTimes_returnsFalse() throws CommandException {
        Session first = new Session(
                "First Lab", futureDateText, "Room-A", "0900", "1000", 50);
        Session second = new Session(
                "Second Lab", futureDateText, "Room-A", "1000", "1100", 50);

        assertFalse(first.conflictsWith(second));
        assertFalse(second.conflictsWith(first));
    }

    /**
     * Verifies that dates take priority over start times and names when sorting.
     */
    @Test
    void compareTo_differentDates_sortsChronologically() throws CommandException {
        String laterDateText = futureDate.plusDays(1).format(DATE_FORMATTER);
        Session earlier = new Session(
                "Zeta Lab", futureDateText, "Room-A", "1400", "1500", 50);
        Session later = new Session(
                "Alpha Lab", laterDateText, "Room-A", "0900", "1000", 50);

        assertTrue(earlier.compareTo(later) < 0);
        assertTrue(later.compareTo(earlier) > 0);
    }

    /**
     * Verifies that start times take priority over names when dates match.
     */
    @Test
    void compareTo_sameDateDifferentTimes_sortsChronologically() throws CommandException {
        Session earlier = new Session(
                "Zeta Lab", futureDateText, "Room-A", "0900", "1000", 50);
        Session later = new Session(
                "Alpha Lab", futureDateText, "Room-B", "1400", "1500", 50);

        assertTrue(earlier.compareTo(later) < 0);
        assertTrue(later.compareTo(earlier) > 0);
    }

    /**
     * Verifies that names break ties when dates and start times match.
     */
    @Test
    void compareTo_sameDateSameTime_sortsAlphabeticallyByName() throws CommandException {
        Session first = new Session(
                "Alpha Lab", futureDateText, "Room-A", "0900", "1200", 50);
        Session second = new Session(
                "Zeta Lab", futureDateText, "Room-B", "0900", "1200", 50);

        assertTrue(first.compareTo(second) < 0);
        assertTrue(second.compareTo(first) > 0);
    }

    /**
     * Verifies that name capitalization does not affect equivalent sorting keys.
     */
    @Test
    void compareTo_sameSortingKeysIgnoringCase_returnsZero() throws CommandException {
        Session first = new Session(
                "Alpha Lab", futureDateText, "Room-A", "0900", "1200", 50);
        Session second = new Session(
                "alpha lab", futureDateText, "Room-B", "0900", "1300", 20);

        assertEquals(0, first.compareTo(second));
        assertEquals(0, second.compareTo(first));
        assertEquals(0, first.compareTo(first));
    }

    /**
     * Verifies the full user-facing description of a session.
     */
    @Test
    void toString_validSession_returnsFormattedDetails() throws CommandException {
        Session session = new Session(
                "CG2111A", futureDateText, "E4A-04-08", "0900", "1200", 50);

        String expected = "CG2111A on " + futureDateText
                + " at E4A-04-08 from 0900 to 1200 for 50 attendees";

        assertEquals(expected, session.toString());
    }

    /**
     * Verifies that a conflict message identifies the existing session and its time range.
     */
    @Test
    void toConflictMessage_validSession_returnsConflictDetails() throws CommandException {
        Session session = new Session(
                "CG2111A", futureDateText, "E4A-04-08", "0900", "1200", 50);

        String expected = "Session conflicts with \"CG2111A\" at E4A-04-08 on "
                + futureDateText + " from 0900 to 1200.";

        assertEquals(expected, session.toConflictMessage());
    }
}

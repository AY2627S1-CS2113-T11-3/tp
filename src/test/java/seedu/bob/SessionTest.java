package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandFormatException;
import seedu.bob.exception.InvalidHeadcountException;
import seedu.bob.exception.InvalidSessionDateException;
import seedu.bob.exception.InvalidSessionTimeException;

/**
 * Tests the validation logic, conflict detection, and chronological sorting of the Session model.
 */
class SessionTest {

    /**
     * Verifies that the constructor successfully creates a session with valid parameters.
     *
     * @throws Exception If an unexpected validation error occurs.
     */
    @Test
    void constructor_validParameters_success() throws Exception {
        Session session = new Session("CG2111A", "16 September 2050", "E4A-04-08", "0900", "1200", 50);
        assertEquals("CG2111A", session.getName());
        assertEquals("E4A-04-08", session.getLocation());
        assertEquals(50, session.getHeadcount());
    }

    /**
     * Verifies that a blank session name throws a CommandFormatException with the correct message.
     */
    @Test
    void constructor_blankName_throwsCommandFormatException() {
        CommandFormatException exception = assertThrows(CommandFormatException.class, () -> {
            new Session("", "16 September 2050", "E4A-04-08", "0900", "1200", 50);
        });
        assertEquals("Session name cannot be blank.", exception.getMessage());
    }

    /**
     * Verifies that a negative headcount throws an InvalidHeadcountException.
     */
    @Test
    void constructor_invalidHeadcount_throwsInvalidHeadcountException() {
        assertThrows(InvalidHeadcountException.class, () -> {
            new Session("CG2111A", "16 September 2050", "E4A-04-08", "0900", "1200", -5);
        });
    }

    /**
     * Verifies that scheduling a session in the past throws an InvalidSessionDateException.
     */
    @Test
    void constructor_pastDate_throwsInvalidSessionDateException() {
        InvalidSessionDateException exception = assertThrows(InvalidSessionDateException.class, () -> {
            new Session("CG2111A", "16 September 2000", "E4A-04-08", "0900", "1200", 50);
        });
        assertEquals("Session date cannot be before today.", exception.getMessage());
    }

    /**
     * Verifies that an uncapitalized month throws an InvalidSessionDateException with formatting instructions.
     */
    @Test
    void constructor_invalidDateFormat_throwsInvalidSessionDateException() {
        InvalidSessionDateException exception = assertThrows(InvalidSessionDateException.class, () -> {
            new Session("CG2111A", "16 september 2050", "E4A-04-08", "0900", "1200", 50);
        });
        assertEquals("Date must be in the format 'd MMMM yyyy' (e.g., 16 September 2026). "
                + "Ensure the month is capitalized.", exception.getMessage());
    }

    /**
     * Verifies that an incorrectly formatted time string throws an InvalidSessionTimeException.
     */
    @Test
    void constructor_invalidTimeFormat_throwsInvalidSessionTimeException() {
        InvalidSessionTimeException exception = assertThrows(InvalidSessionTimeException.class, () -> {
            new Session("CG2111A", "16 September 2050", "E4A-04-08", "9 AM", "1200", 50);
        });
        assertEquals("Start time must be in 'HHmm' format (e.g., 0900 for 9:00 AM).", exception.getMessage());
    }

    /**
     * Verifies that a start time occurring after an end time throws an InvalidSessionTimeException.
     */
    @Test
    void constructor_startAfterEnd_throwsInvalidSessionTimeException() {
        InvalidSessionTimeException exception = assertThrows(InvalidSessionTimeException.class, () -> {
            new Session("CG2111A", "16 September 2050", "E4A-04-08", "1400", "1200", 50);
        });
        assertEquals("Start time must be earlier than end time.", exception.getMessage());
    }

    /**
     * Verifies that sessions sharing a location and overlapping times trigger a conflict.
     *
     * @throws Exception If session creation fails.
     */
    @Test
    void conflictsWith_overlappingTimeSameLocation_returnsTrue() throws Exception {
        Session session1 = new Session("Lab 1", "16 September 2050", "Lab A", "0900", "1200", 50);
        Session session2 = new Session("Lab 2", "16 September 2050", "Lab A", "1000", "1300", 50);
        assertTrue(session1.conflictsWith(session2));
    }

    /**
     * Verifies that sessions occurring at the same time but in different locations do not conflict.
     *
     * @throws Exception If session creation fails.
     */
    @Test
    void conflictsWith_differentLocation_returnsFalse() throws Exception {
        Session session1 = new Session("Lab 1", "16 September 2050", "Lab A", "0900", "1200", 50);
        Session session2 = new Session("Lab 2", "16 September 2050", "Lab B", "0900", "1200", 50);
        assertFalse(session1.conflictsWith(session2));
    }

    /**
     * Verifies that sessions sharing a location with perfectly adjacent times do not conflict.
     *
     * @throws Exception If session creation fails.
     */
    @Test
    void conflictsWith_adjacentTimes_returnsFalse() throws Exception {
        Session session1 = new Session("Lab 1", "16 September 2050", "Lab A", "0900", "1000", 50);
        Session session2 = new Session("Lab 2", "16 September 2050", "Lab A", "1000", "1100", 50);
        assertFalse(session1.conflictsWith(session2));
    }

    /**
     * Verifies that sorting logic correctly places earlier dates before later dates.
     *
     * @throws Exception If session creation fails.
     */
    @Test
    void compareTo_differentDates_sortsChronologically() throws Exception {
        Session earlier = new Session("Lab A", "16 September 2050", "Room 1", "0900", "1200", 50);
        Session later = new Session("Lab B", "17 September 2050", "Room 1", "0900", "1200", 50);
        assertTrue(earlier.compareTo(later) < 0);
        assertTrue(later.compareTo(earlier) > 0);
    }

    /**
     * Verifies that sorting logic correctly prioritizes start times when dates are identical.
     *
     * @throws Exception If session creation fails.
     */
    @Test
    void compareTo_sameDateDifferentTimes_sortsChronologically() throws Exception {
        Session earlier = new Session("Lab A", "16 September 2050", "Room 1", "0900", "1200", 50);
        Session later = new Session("Lab B", "16 September 2050", "Room 2", "1400", "1700", 50);
        assertTrue(earlier.compareTo(later) < 0);
    }

    /**
     * Verifies that alphabetical sorting is used as a stable tie-breaker for identical dates and times.
     *
     * @throws Exception If session creation fails.
     */
    @Test
    void compareTo_sameDateSameTime_sortsAlphabeticallyByName() throws Exception {
        Session firstAlpha = new Session("Alpha Lab", "16 September 2050", "Room 1", "0900", "1200", 50);
        Session secondAlpha = new Session("Zeta Lab", "16 September 2050", "Room 2", "0900", "1200", 50);
        assertTrue(firstAlpha.compareTo(secondAlpha) < 0);
    }
}

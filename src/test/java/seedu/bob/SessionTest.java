package seedu.bob;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import seedu.bob.exception.CommandFormatException;
import seedu.bob.exception.InvalidHeadcountException;
import seedu.bob.exception.InvalidSessionDateException;
import seedu.bob.exception.InvalidSessionTimeException;

/**
 * Tests the validation logic and conflict detection of the Session model.
 */
public class SessionTest {

    @Test
    public void constructor_validParameters_success() throws Exception {
        Session session = new Session("CG2111A", "16 September 2050", "E4A-04-08", "0900", "1200", 50);
        assertEquals("CG2111A", session.getName());
        assertEquals("E4A-04-08", session.getLocation());
        assertEquals(50, session.getHeadcount());
    }

    @Test
    public void constructor_blankName_throwsCommandFormatException() {
        assertThrows(CommandFormatException.class, () -> {
            new Session("", "16 September 2050", "E4A-04-08", "0900", "1200", 50);
        });
    }

    @Test
    public void constructor_invalidHeadcount_throwsInvalidHeadcountException() {
        assertThrows(InvalidHeadcountException.class, () -> {
            new Session("CG2111A", "16 September 2050", "E4A-04-08", "0900", "1200", -5);
        });
    }

    @Test
    public void constructor_pastDate_throwsInvalidSessionDateException() {
        assertThrows(InvalidSessionDateException.class, () -> {
            new Session("CG2111A", "16 September 2000", "E4A-04-08", "0900", "1200", 50);
        });
    }

    @Test
    public void constructor_invalidTimeFormat_throwsInvalidSessionTimeException() {
        assertThrows(InvalidSessionTimeException.class, () -> {
            new Session("CG2111A", "16 September 2050", "E4A-04-08", "9 AM", "1200", 50);
        });
    }

    @Test
    public void constructor_startAfterEnd_throwsInvalidSessionTimeException() {
        assertThrows(InvalidSessionTimeException.class, () -> {
            new Session("CG2111A", "16 September 2050", "E4A-04-08", "1400", "1200", 50);
        });
    }

    @Test
    public void conflictsWith_overlappingTimeSameLocation_returnsTrue() throws Exception {
        Session session1 = new Session("Lab 1", "16 September 2050", "Lab A", "0900", "1200", 50);
        Session session2 = new Session("Lab 2", "16 September 2050", "Lab A", "1000", "1300", 50);
        assertTrue(session1.conflictsWith(session2));
    }

    @Test
    public void conflictsWith_differentLocation_returnsFalse() throws Exception {
        Session session1 = new Session("Lab 1", "16 September 2050", "Lab A", "0900", "1200", 50);
        Session session2 = new Session("Lab 2", "16 September 2050", "Lab B", "0900", "1200", 50);
        assertFalse(session1.conflictsWith(session2));
    }
}

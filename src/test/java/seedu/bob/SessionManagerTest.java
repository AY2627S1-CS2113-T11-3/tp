package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.InvalidSessionIndexException;

/**
 * Verifies that session lookup follows the positions displayed to users.
 */
class SessionManagerTest {

    @Test
    void getSession_invalidIndex_exceptionThrown() {
        SessionManager manager = new SessionManager();

        assertThrows(InvalidSessionIndexException.class, () -> manager.getSession(0));
        assertThrows(InvalidSessionIndexException.class, () -> manager.getSession(1));
        assertThrows(InvalidSessionIndexException.class, () -> manager.getSession(-1));
    }

    @Test
    void getSession_afterDeletion_remainingSessionReturned() throws CommandException {
        SessionManager manager = new SessionManager();
        Session first = createSession("First Lab", "Room-A", 10);
        Session second = createSession("Second Lab", "Room-B", 20);
        manager.addSession(first);
        manager.addSession(second);
        first.getPreparation().addItem("Wire", "Components", 1, first.getHeadcount());
        second.getPreparation().addItem("Battery", "Components", 2, second.getHeadcount());

        manager.deleteSession(1);

        assertSame(second, manager.getSession(1));
        assertEquals(20, manager.getSession(1).getHeadcount());
        assertEquals("Battery", manager.getSession(1).getPreparation().getItems().get(0).getName());
        assertEquals(40, manager.getSession(1).getPreparation().getItems().get(0).getQuantity());
        assertThrows(InvalidSessionIndexException.class, () -> manager.getSession(2));
    }

    /**
     * Creates a future session so the tests remain valid as the date changes.
     */
    private Session createSession(String name, String location, int headcount) throws CommandException {
        String date = LocalDate.now().plusDays(1)
                .format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH));
        return new Session(name, date, location, "0900", "1000", headcount);
    }
}

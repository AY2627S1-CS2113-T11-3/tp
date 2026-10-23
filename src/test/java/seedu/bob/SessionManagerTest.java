package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.InvalidSessionIndexException;
import seedu.bob.exception.SessionConflictException;

/**
 * Verifies schedule validation, chronological ordering, session lookup, and list output.
 */
class SessionManagerTest {

    /** Defines expected display text independently of the application's formatting constants. */
    private static final String EXPECTED_DIVIDER =
            "--------------------------------------------------------------------\n";
    private static final String EXPECTED_HEADER = EXPECTED_DIVIDER + "Sessions\n" + EXPECTED_DIVIDER;

    /**
     * Verifies that adding a null session throws a NullPointerException and preserves the schedule.
     */
    @Test
    void addSession_nullSession_throwsNullPointerExceptionWithoutChangingSchedule() {
        SessionManager manager = new SessionManager();

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> manager.addSession(null));

        assertEquals("Session to add must not be null", exception.getMessage());
        assertThrows(InvalidSessionIndexException.class, () -> manager.getSession(1));
    }

    /**
     * Verifies that attempting to retrieve an out-of-bounds index throws an InvalidSessionIndexException.
     */
    @Test
    void getSession_invalidIndex_exceptionThrown() {
        SessionManager manager = new SessionManager();

        assertThrows(InvalidSessionIndexException.class, () -> manager.getSession(0));
        assertThrows(InvalidSessionIndexException.class, () -> manager.getSession(1));
        assertThrows(InvalidSessionIndexException.class, () -> manager.getSession(-1));
    }

    /**
     * Verifies that sessions correctly shift their index positions after preceding sessions are deleted.
     *
     * @throws CommandException If test sessions fail to initialize.
     */
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
     * Verifies that adding an overlapping session triggers a SessionConflictException without altering the schedule.
     *
     * @throws CommandException If test sessions fail to initialize.
     */
    @Test
    void addSession_conflictingSession_throwsExceptionAndPreservesSchedule() throws CommandException {
        SessionManager manager = new SessionManager();
        Session existingSession = createSession("First Lab", "Room-A", 10);
        Session conflictingSession = createSession("Second Lab", "room-a", 20);
        manager.addSession(existingSession);

        SessionConflictException exception = assertThrows(SessionConflictException.class,
                () -> manager.addSession(conflictingSession));

        assertEquals(existingSession.toConflictMessage(), exception.getMessage());
        assertSame(existingSession, manager.getSession(1));
        assertThrows(InvalidSessionIndexException.class, () -> manager.getSession(2));
    }

    /**
     * Verifies that attempting to delete an out-of-bounds index throws an exception and protects existing data.
     *
     * @throws CommandException If test sessions fail to initialize.
     */
    @Test
    void deleteSession_invalidIndex_throwsExceptionAndPreservesSchedule() throws CommandException {
        SessionManager manager = new SessionManager();
        Session existingSession = createSession("First Lab", "Room-A", 10);
        manager.addSession(existingSession);

        InvalidSessionIndexException exception = assertThrows(InvalidSessionIndexException.class,
                () -> manager.deleteSession(2));

        assertEquals("Session index 2 is invalid. There are 1 scheduled session(s).", exception.getMessage());
        assertSame(existingSession, manager.getSession(1));
    }

    /**
     * Verifies that sessions added in random order are automatically restructured chronologically by date and time.
     *
     * @throws CommandException If test sessions fail to initialize.
     */
    @Test
    void addSession_outOfOrder_sortsChronologically() throws CommandException {
        SessionManager manager = new SessionManager();
        Session decSession = new Session("December Lab", "16 December 2050", "Room-A", "0900", "1000", 10);
        Session octSession = new Session("October Morning Lab", "16 October 2050", "Room-A", "0900", "1000", 10);
        Session octAfternoon = new Session("October Afternoon Lab", "16 October 2050", "Room-B", "1400", "1500", 10);

        manager.addSession(decSession);
        manager.addSession(octAfternoon);
        manager.addSession(octSession);

        assertSame(octSession, manager.getSession(1));
        assertSame(octAfternoon, manager.getSession(2));
        assertSame(decSession, manager.getSession(3));
    }

    /**
     * Verifies the complete output for an empty schedule.
     */
    @Test
    void listSessions_emptySchedule_printsEmptyMessage() {
        SessionManager manager = new SessionManager();

        assertEquals(EXPECTED_HEADER + "No sessions scheduled.\n" + EXPECTED_DIVIDER,
                captureListing(manager));
    }

    /**
     * Verifies that listing shows full details, chronological order, and one-based positions.
     *
     * @throws CommandException If test sessions fail to initialize or be added.
     */
    @Test
    void listSessions_outOfOrder_printsChronologicalDetails() throws CommandException {
        SessionManager manager = new SessionManager();
        LocalDate firstDate = LocalDate.now().plusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH);
        String firstDateText = firstDate.format(formatter);
        String secondDateText = firstDate.plusDays(1).format(formatter);
        Session morning = new Session("Morning Lab", firstDateText, "Room-A", "0900", "1000", 10);
        Session afternoon = new Session("Afternoon Lab", firstDateText, "Room-B", "1400", "1500", 20);
        Session nextDay = new Session("Next Day Lab", secondDateText, "Room-C", "0800", "0900", 30);
        manager.addSession(nextDay);
        manager.addSession(afternoon);
        manager.addSession(morning);

        String expected = EXPECTED_HEADER
                + "1. Morning Lab on " + firstDateText + " at Room-A from 0900 to 1000 for 10 attendees\n"
                + "2. Afternoon Lab on " + firstDateText + " at Room-B from 1400 to 1500 for 20 attendees\n"
                + "3. Next Day Lab on " + secondDateText + " at Room-C from 0800 to 0900 for 30 attendees\n"
                + EXPECTED_DIVIDER;

        assertEquals(expected, captureListing(manager));
        assertSame(morning, manager.getSession(1));
        assertSame(afternoon, manager.getSession(2));
        assertSame(nextDay, manager.getSession(3));
    }

    /**
     * Verifies that deleting the first session removes its listing and renumbers the remaining session.
     *
     * @throws CommandException If session creation or a schedule operation fails.
     */
    @Test
    void listSessions_afterDeletion_printsRenumberedSession() throws CommandException {
        SessionManager manager = new SessionManager();
        Session first = createSession("First Lab", "Room-A", 10);
        Session second = createSession("Second Lab", "Room-B", 20);
        manager.addSession(first);
        manager.addSession(second);
        manager.deleteSession(1);
        String dateText = second.getDate()
                .format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH));

        String expected = EXPECTED_HEADER
                + "1. Second Lab on " + dateText + " at Room-B from 0900 to 1000 for 20 attendees\n"
                + EXPECTED_DIVIDER;

        assertEquals(expected, captureListing(manager));
        assertSame(second, manager.getSession(1));
    }

    /**
     * Verifies that deleting the last session leaves an empty listing and no accessible session.
     *
     * @throws CommandException If session creation or a schedule operation fails.
     */
    @Test
    void listSessions_afterDeletingLastSession_printsEmptyMessage() throws CommandException {
        SessionManager manager = new SessionManager();
        manager.addSession(createSession("Only Lab", "Room-A", 10));
        manager.deleteSession(1);

        assertEquals(EXPECTED_HEADER + "No sessions scheduled.\n" + EXPECTED_DIVIDER,
                captureListing(manager));
        assertThrows(InvalidSessionIndexException.class, () -> manager.getSession(1));
    }

    /**
     * Captures only list output and restores standard output even if listing fails.
     *
     * @param manager Schedule to display.
     * @return Listing with Windows line endings converted to newline characters.
     */
    private String captureListing(SessionManager manager) {
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            try {
                manager.listSessions();
            } finally {
                System.setOut(originalOutput);
            }
        }
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    /**
     * Creates a future session so the tests remain valid as the date changes over time.
     *
     * @param name Name of the lab session.
     * @param location Venue of the session.
     * @param headcount Expected number of attendees.
     * @return A valid session scheduled for one day in the future.
     * @throws CommandException If session validation fails.
     */
    private Session createSession(String name, String location, int headcount) throws CommandException {
        String date = LocalDate.now().plusDays(1)
                .format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH));
        return new Session(name, date, location, "0900", "1000", headcount);
    }
}

//@@author gavinlimsh
package seedu.bob;

import java.util.ArrayList;
import java.util.Collections;
//@@author DaynaG3
import java.util.List;
//@@author gavinlimsh
import java.util.Objects;
import java.util.logging.Logger;

//@@author DaynaG3
import seedu.bob.exception.CommandException;
//@@author gavinlimsh
import seedu.bob.exception.InvalidSessionIndexException;
import seedu.bob.exception.SessionConflictException;

/**
 * Manages scheduled lab sessions and displays scheduling changes.
 */
public class SessionManager {

    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());
    private static final int INDEX_OFFSET = 1;
    /** Keeps chronological display order, which also defines one-based session positions. */
    private final ArrayList<Session> sessions = new ArrayList<>();

    /**
     * Creates an empty schedule whose sessions are sorted when added.
     */
    public SessionManager() {
    }

    //@@author DaynaG3
    /** Exports sessions with their preparation lists in chronological order. */
    List<AppSnapshot.SessionEntry> toSnapshot() {
        List<AppSnapshot.SessionEntry> entries = new ArrayList<>();
        for (Session session : sessions) {
            entries.add(session.toSnapshot());
        }
        return List.copyOf(entries);
    }

    /** Builds a new conflict-free schedule without printing addition confirmations. */
    static SessionManager restore(List<AppSnapshot.SessionEntry> entries) throws CommandException {
        SessionManager manager = new SessionManager();
        for (AppSnapshot.SessionEntry entry : entries) {
            manager.addSessionSilently(Session.restore(entry));
        }
        return manager;
    }
    //@@author gavinlimsh

    /**
     * Adds a session to the schedule, sorts it chronologically, and prints a confirmation.
     *
     * @param session Session to add.
     * @throws NullPointerException If {@code session} is {@code null}.
     * @throws SessionConflictException If the session overlaps another at the same date and location.
     */
    public void addSession(Session session) throws SessionConflictException {
        Objects.requireNonNull(session, "Session to add must not be null");
        LOGGER.info("Attempting to add new session: " + session.getName());

        //@@author DaynaG3
        addSessionSilently(session);
        //@@author gavinlimsh

        LOGGER.info("Session successfully added to schedule and sorted chronologically.");
        System.out.println("Successfully added: " + session.toString());
        //@@author DaynaG3
    }

    /** Shares conflict validation and ordering between commands and silent snapshot restoration. */
    private void addSessionSilently(Session session) throws SessionConflictException {
        //@@author gavinlimsh

        for (Session existingSession : sessions) {
            if (session.conflictsWith(existingSession)) {
                LOGGER.warning("Session conflict detected with: " + existingSession.getName());
                throw new SessionConflictException(existingSession.toConflictMessage());
            }
        }

        sessions.add(session);
        Collections.sort(sessions);

        assert hasValidState() : "Schedule must remain conflict-free after addition";
    }

    /**
     * Removes a session by its one-based index via a single-line command.
     *
     * @param userIndex One-based session position in the list.
     * @throws InvalidSessionIndexException If the index is outside the schedule.
     */
    public void deleteSession(int userIndex) throws InvalidSessionIndexException {
        LOGGER.info("Attempting to delete session at index " + userIndex);
        Session removedSession = getSession(userIndex);

        assert removedSession != null : "Retrieved session should not be null prior to deletion";

        int index = userIndex - INDEX_OFFSET;
        sessions.remove(index);

        assert hasValidState() : "Schedule must remain valid after deletion";
        LOGGER.info("Successfully removed session: " + removedSession.getName());
        System.out.println("Successfully removed session: " + removedSession.getName());
    }

    //@@author DaynaG3
    /**
     * Returns whether the schedule contains only nonnull, mutually compatible sessions.
     */
    private boolean hasValidState() {
        for (int i = 0; i < sessions.size(); i++) {
            Session session = sessions.get(i);
            if (session == null) {
                return false;
            }

            for (int j = i + 1; j < sessions.size(); j++) {
                Session otherSession = sessions.get(j);
                if (otherSession == null || session.conflictsWith(otherSession)) {
                    return false;
                }
            }
        }
        return true;
    }

    //@@author
    /**
     * Returns the session at the position shown by {@code list-s}.
     *
     * @param userIndex One-based session position.
     * @return Session at the requested position.
     * @throws InvalidSessionIndexException If the index is outside the schedule.
     */
    public Session getSession(int userIndex) throws InvalidSessionIndexException {
        if (userIndex <= 0 || userIndex > sessions.size()) {
            LOGGER.warning("Invalid session index requested: " + userIndex);
            throw new InvalidSessionIndexException(userIndex, sessions.size());
        }
        assert userIndex - INDEX_OFFSET >= 0 : "Calculated array index must always be non-negative";
        return sessions.get(userIndex - INDEX_OFFSET);
    }

    //@@author gavinlimsh
    /**
     * Prints all scheduled sessions with one-based positions.
     */
    public void listSessions() {
        System.out.println(Ui.DIVIDER);
        System.out.println("Sessions");
        System.out.println(Ui.DIVIDER);

        if (sessions.isEmpty()) {
            System.out.println("No sessions scheduled.");
        } else {
            for (int i = 0; i < sessions.size(); i++) {
                System.out.printf("%d. %s\n", i + 1, sessions.get(i).toString());
            }
        }
        System.out.println(Ui.DIVIDER);
    }
    //@@author
}

package seedu.bob;

import java.util.ArrayList;
import java.util.logging.Logger;

import seedu.bob.exception.InvalidSessionIndexException;
import seedu.bob.exception.SessionConflictException;

/**
 * Manages scheduled lab sessions and displays scheduling changes.
 */
public class SessionManager {

    private static final Logger logger = Logger.getLogger(SessionManager.class.getName());
    private static final int INDEX_OFFSET = 1;
    private final ArrayList<Session> sessions = new ArrayList<>();

    /**
     * Adds a session to the schedule and prints a confirmation.
     *
     * @param session Session to add.
     * @throws SessionConflictException If the session overlaps another at the same date and location.
     */
    public void addSession(Session session) throws SessionConflictException {
        assert session != null : "Session object to be added must not be null";
        logger.info("Attempting to add new session: " + session.getName());

        for (Session existingSession : sessions) {
            if (session.conflictsWith(existingSession)) {
                logger.warning("Session conflict detected with: " + existingSession.getName());
                throw new SessionConflictException(existingSession.toConflictMessage());
            }
        }
        sessions.add(session);
        logger.info("Session successfully added to schedule.");
        System.out.println("Successfully added: " + session.toString());
    }

    /**
     * Removes a session by its one-based index via a single-line command.
     *
     * @param userIndex One-based session position in the list.
     * @throws InvalidSessionIndexException If the index is outside the schedule.
     */
    public void deleteSession(int userIndex) throws InvalidSessionIndexException {
        logger.info("Attempting to delete session at index " + userIndex);
        Session removedSession = getSession(userIndex);

        assert removedSession != null : "Retrieved session should not be null prior to deletion";

        int index = userIndex - INDEX_OFFSET;
        sessions.remove(index);

        logger.info("Successfully removed session: " + removedSession.getName());
        System.out.println("Successfully removed session: " + removedSession.getName());
    }

    /**
     * Returns the session at the position shown by {@code list-s}.
     *
     * @param userIndex One-based session position.
     * @return Session at the requested position.
     * @throws InvalidSessionIndexException If the index is outside the schedule.
     */
    public Session getSession(int userIndex) throws InvalidSessionIndexException {
        if (userIndex <= 0 || userIndex > sessions.size()) {
            logger.warning("Invalid session index requested: " + userIndex);
            throw new InvalidSessionIndexException(userIndex, sessions.size());
        }
        assert userIndex - INDEX_OFFSET >= 0 : "Calculated array index must always be non-negative";
        return sessions.get(userIndex - INDEX_OFFSET);
    }

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
}

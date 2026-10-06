package seedu.duke;

import java.util.ArrayList;

/**
 * Manages scheduled lab sessions and displays scheduling changes.
 */
public class SessionManager {
    private static final int INDEX_OFFSET = 1;
    private final ArrayList<Session> sessions = new ArrayList<>();

    /**
     * Adds a session to the schedule and prints a confirmation.
     *
     * @param session Session to add.
     */
    public void addSession(Session session) {
        sessions.add(session);
        System.out.println("Successfully added: " + session.toString());
    }

    /**
     * Removes a session by its one-based index via a single-line command.
     *
     * @param userIndex One-based session position in the list.
     */
    public void deleteSession(int userIndex) {
        try {
            int index = userIndex - INDEX_OFFSET;
            Session removedSession = sessions.remove(index);
            System.out.println("Successfully removed session: " + removedSession.getName());
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Invalid session index!");
        }
    }

    /**
     * Prints all scheduled sessions with one-based positions.
     */
    public void listSessions() {
        System.out.println("\n" + Ui.DIVIDER);
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

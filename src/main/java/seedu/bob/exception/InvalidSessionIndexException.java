package seedu.bob.exception;

/**
 * Indicates that a session index is not a valid one-based position.
 */
public class InvalidSessionIndexException extends CommandException {

    private static final String NON_POSITIVE_MESSAGE =
            "Session index must be a positive integer.";

    /**
     * Creates an exception for an index that cannot be parsed as a positive integer.
     */
    public InvalidSessionIndexException() {
        super(NON_POSITIVE_MESSAGE);
    }

    /**
     * Creates an exception describing an index outside the schedule.
     *
     * @param index Index requested by the user.
     * @param sessionCount Number of sessions currently scheduled.
     */
    public InvalidSessionIndexException(int index, int sessionCount) {
        super(String.format("Session index %d is invalid. There are %d scheduled session(s).",
                index, sessionCount));
    }
}

package seedu.bob.exception;

/**
 * Indicates that a contact index is not a valid one-based position.
 */
public class InvalidContactIndexException extends CommandException {

    private static final String NON_POSITIVE_MESSAGE =
            "Contact index must be a positive integer.";

    /**
     * Creates an exception for an index that cannot be parsed as a positive integer.
     */
    public InvalidContactIndexException() {
        super(NON_POSITIVE_MESSAGE);
    }

    /**
     * Creates an exception describing an index outside the contact list.
     *
     * @param index Index requested by the user.
     * @param contactCount Number of contacts currently stored.
     */
    public InvalidContactIndexException(int index, int contactCount) {
        super(String.format("Contact index %d is invalid. There are %d contact(s).",
                index, contactCount));
    }
}

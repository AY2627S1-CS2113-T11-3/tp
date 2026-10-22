package seedu.bob.exception;

/**
 * Indicates that a student request index is not a valid one-based position.
 *
 * @author Lokwx
 */
public class InvalidRequestIndexException extends CommandException {

    private static final String NON_POSITIVE_MESSAGE =
            "Request index must be a positive integer.";

    /**
     * Creates an exception for an index that cannot be parsed as a positive integer.
     */
    public InvalidRequestIndexException() {
        super(NON_POSITIVE_MESSAGE);
    }

    /**
     * Creates an exception describing an index outside the request list.
     *
     * @param index Index requested by the user.
     * @param requestCount Number of requests currently recorded.
     */
    public InvalidRequestIndexException(int index, int requestCount) {
        super(String.format("Request index %d is invalid. There are %d request(s).",
                index, requestCount));
    }
}

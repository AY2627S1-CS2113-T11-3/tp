package seedu.bob.exception;

/**
 * Indicates that a student request deadline is malformed or in the past.
 */
public class InvalidRequestDeadlineException extends CommandException {

    /** Message for deadlines that cannot be parsed in the documented format. */
    public static final String INVALID_FORMAT_MESSAGE =
            "Deadline must be a valid date in d MMMM yyyy format.";

    /** Message for deadlines that parse successfully but fall before today. */
    public static final String PAST_DATE_MESSAGE = "Request deadline cannot be before today.";

    /**
     * Creates an exception describing the invalid deadline.
     *
     * @param message Explanation shown to the user.
     */
    public InvalidRequestDeadlineException(String message) {
        super(message);
    }
}

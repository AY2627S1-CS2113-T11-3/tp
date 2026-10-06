package seedu.bob.exception;

/**
 * Indicates that a command does not follow its required syntax.
 */
public class CommandFormatException extends CommandException {

    /**
     * Creates an exception describing the invalid command syntax.
     *
     * @param message Explanation shown to the user.
     */
    public CommandFormatException(String message) {
        super(message);
    }
}

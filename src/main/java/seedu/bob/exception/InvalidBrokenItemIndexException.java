//@@author gavinlimsh
package seedu.bob.exception;

/** Indicates an invalid one-based position in the broken-item report list. */
public class InvalidBrokenItemIndexException extends CommandException {

    /** Creates an error for a value that is not a positive integer. */
    public InvalidBrokenItemIndexException() {
        super("Broken-item index must be a positive integer.");
    }

    /**
     * Creates an error while retaining the integer parsing failure.
     *
     * @param cause Original parsing failure.
     */
    public InvalidBrokenItemIndexException(NumberFormatException cause) {
        super("Broken-item index must be a positive integer.", cause);
    }

    /**
     * Creates an error for a position outside the current report list.
     *
     * @param index Requested one-based position.
     * @param reportCount Number of active reports.
     */
    public InvalidBrokenItemIndexException(int index, int reportCount) {
        super(String.format("Broken-item index %d is invalid. There are %d broken-item report(s).",
                index, reportCount));
    }
}

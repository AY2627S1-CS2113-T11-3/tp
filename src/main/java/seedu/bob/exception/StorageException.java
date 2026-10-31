//@@author DaynaG3
package seedu.bob.exception;

/** Represents an expected persistence failure without exposing saved personal data in user messages. */
public class StorageException extends Exception {

    /** Distinguishes recovery decisions without inspecting human-readable exception messages. */
    public enum Kind {
        INVALID_DATA, UNSUPPORTED_VERSION, IO_ERROR
    }

    private final Kind kind;

    /**
     * Creates a storage failure with a safe explanation and the original diagnostic cause.
     *
     * @param kind Failure category used by recovery logic.
     * @param message Safe user-facing explanation.
     * @param cause Underlying error, or null for a directly detected validation failure.
     */
    public StorageException(Kind kind, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }
}

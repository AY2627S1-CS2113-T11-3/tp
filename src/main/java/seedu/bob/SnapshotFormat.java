package seedu.bob;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

import seedu.bob.exception.CommandFormatException;

/** Converts strict storage date/time strings to the formats accepted by domain construction. */
final class SnapshotFormat {

    private static final DateTimeFormatter COMMAND_DATE = DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter STORAGE_TIME = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter COMMAND_TIME = DateTimeFormatter.ofPattern("HHmm");

    private SnapshotFormat() {
    }

    /** Validates an ISO calendar date without imposing a today-or-later rule. */
    static String toCommandDate(String text) throws CommandFormatException {
        if (text == null) {
            throw new CommandFormatException("Saved date must not be null.");
        }
        try {
            return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE).format(COMMAND_DATE);
        } catch (DateTimeParseException e) {
            throw new CommandFormatException("Saved date must be a valid ISO date (yyyy-MM-dd).");
        }
    }

    /** Validates a storage time with minute precision, rejecting seconds and invalid clock values. */
    static String toCommandTime(String text) throws CommandFormatException {
        if (text == null) {
            throw new CommandFormatException("Saved time must not be null.");
        }
        try {
            return LocalTime.parse(text, STORAGE_TIME).format(COMMAND_TIME);
        } catch (DateTimeParseException e) {
            throw new CommandFormatException("Saved time must be a valid time in HH:mm format.");
        }
    }

    /** Formats a domain time with the exact minute precision required by storage. */
    static String formatTime(LocalTime time) {
        return time.format(STORAGE_TIME);
    }
}

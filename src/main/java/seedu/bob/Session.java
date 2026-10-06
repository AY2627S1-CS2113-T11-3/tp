package seedu.bob;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.CommandFormatException;
import seedu.bob.exception.InvalidHeadcountException;
import seedu.bob.exception.InvalidSessionDateException;
import seedu.bob.exception.InvalidSessionTimeException;

/**
 * Represents a scheduled lab session with a date, location, time, and
 * headcount.
 */
public class Session {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("d MMMM uuuu", Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter
            .ofPattern("HHmm")
            .withResolverStyle(ResolverStyle.STRICT);

    private String name;
    private LocalDate date;
    private String location;
    private LocalTime startTime;
    private LocalTime endTime;
    private int headcount;

    /**
     * Creates a validated lab session.
     *
     * @param name Session name.
     * @param dateText Date in {@code d MMMM uuuu} format.
     * @param location Session location.
     * @param startTimeText Start time in {@code HHmm} format.
     * @param endTimeText End time in {@code HHmm} format.
     * @param headcount Number of attendees.
     * @throws CommandException If any session value is invalid.
     */
    public Session(String name, String dateText, String location,
            String startTimeText, String endTimeText, int headcount) throws CommandException {
        if (name == null || name.isBlank()) {
            throw new CommandFormatException("Session name cannot be blank.");
        }
        if (location == null || location.isBlank()) {
            throw new CommandFormatException("Session location cannot be blank.");
        }
        if (headcount <= 0) {
            throw new InvalidHeadcountException();
        }

        this.name = name;
        this.location = location;
        this.headcount = headcount;

        try {
            this.date = LocalDate.parse(dateText, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidSessionDateException(
                    "Date must be a valid date in d MMMM uuuu format.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new InvalidSessionDateException("Session date cannot be before today.");
        }

        try {
            this.startTime = LocalTime.parse(startTimeText, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidSessionTimeException(
                    "Start time must be a valid time in HHmm format.");
        }
        try {
            this.endTime = LocalTime.parse(endTimeText, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidSessionTimeException(
                    "End time must be a valid time in HHmm format.");
        }
        if (!startTime.isBefore(endTime)) {
            throw new InvalidSessionTimeException("Start time must be earlier than end time.");
        }
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Returns whether this session overlaps another at the same date and location.
     *
     * @param other Other session to compare.
     * @return {@code true} if the sessions conflict.
     */
    public boolean conflictsWith(Session other) {
        boolean hasSameDate = date.equals(other.date);
        boolean hasSameLocation = location.equalsIgnoreCase(other.location);
        boolean timesOverlap = startTime.isBefore(other.endTime)
                && other.startTime.isBefore(endTime);
        return hasSameDate && hasSameLocation && timesOverlap;
    }

    /**
     * Describes this session's conflicting date, location, and time range.
     *
     * @return User-facing conflict description.
     */
    public String toConflictMessage() {
        return String.format("Session conflicts with \"%s\" at %s on %s from %s to %s.",
                name, location, date.format(DATE_FORMATTER),
                startTime.format(TIME_FORMATTER), endTime.format(TIME_FORMATTER));
    }

    @Override
    public String toString() {
        return String.format("%s on %s at %s from %s to %s for %d attendees",
                name, date.format(DATE_FORMATTER), location,
                startTime.format(TIME_FORMATTER), endTime.format(TIME_FORMATTER), headcount);
    }
}

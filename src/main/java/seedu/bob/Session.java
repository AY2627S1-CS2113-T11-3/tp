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
 * Represents a scheduled lab session with a date, location, time, and headcount.
 * Implements Comparable to allow chronological sorting.
 */
public class Session implements Comparable<Session> {

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

    /** Belongs to the session itself so schedule renumbering cannot change its preparation. */
    private final Preparation preparation = new Preparation();

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
                    "Date must be in the format 'd MMMM yyyy' (e.g., 22 November 2027). "
                            + "Ensure the month is capitalized.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new InvalidSessionDateException("Session date cannot be before today.");
        }

        try {
            this.startTime = LocalTime.parse(startTimeText, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidSessionTimeException(
                    "Start time must be in 'HHmm' format (e.g., 0900 for 9:00 AM).");
        }
        try {
            this.endTime = LocalTime.parse(endTimeText, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidSessionTimeException(
                    "End time must be in 'HHmm' format (e.g., 1200 for 12:00 PM).");
        }
        if (!startTime.isBefore(endTime)) {
            throw new InvalidSessionTimeException("Start time must be earlier than end time.");
        }
    }

    /**
     * Returns the name of the lab session.
     *
     * @return The session name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the scheduled date of the session.
     *
     * @return The session date as a LocalDate object.
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns the venue or location of the session.
     *
     * @return The session location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the starting time of the session.
     *
     * @return The start time as a LocalTime object.
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Returns the ending time of the session.
     *
     * @return The end time as a LocalTime object.
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Returns the expected number of attendees for the session.
     *
     * @return The session headcount.
     */
    public int getHeadcount() {
        return headcount;
    }

    /**
     * Returns the inventory preparation requirements tied to this specific session.
     *
     * @return The session's Preparation object.
     */
    public Preparation getPreparation() {
        return preparation;
    }

    //@@author DaynaG3
    /**
     * Returns whether this session overlaps another at the same date and location.
     *
     * @param other Other session to compare.
     * @return {@code true} if the sessions conflict.
     */
    public boolean conflictsWith(Session other) {
        boolean hasSameDate = date.equals(other.date);
        boolean hasSameLocation = location.equalsIgnoreCase(other.location);
        boolean hasTimeOverlap = startTime.isBefore(other.endTime)
                && other.startTime.isBefore(endTime);
        return hasSameDate && hasSameLocation && hasTimeOverlap;
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

    //@@author gavinlimsh
    /**
     * Compares this session with another for chronological sorting.
     * Priorities: Date -> Start Time -> Alphabetical Name (Tie-breaker).
     *
     * @param other The other session to be compared.
     * @return A negative integer, zero, or a positive integer as this session
     *         is earlier than, equal to, or later than the specified session.
     */
    @Override
    public int compareTo(Session other) {
        int dateComparison = this.date.compareTo(other.date);
        if (dateComparison != 0) {
            return dateComparison;
        }

        int timeComparison = this.startTime.compareTo(other.startTime);
        if (timeComparison != 0) {
            return timeComparison;
        }

        return this.name.compareToIgnoreCase(other.name);
    }
    //@@author

    /**
     * Returns the string representation of the session for user display.
     *
     * @return Formatted session details.
     */
    @Override
    public String toString() {
        return String.format("%s on %s at %s from %s to %s for %d attendees",
                name, date.format(DATE_FORMATTER), location,
                startTime.format(TIME_FORMATTER), endTime.format(TIME_FORMATTER), headcount);
    }
}

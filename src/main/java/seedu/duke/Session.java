package seedu.duke;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Represents a scheduled lab session with a date, location, time, and headcount.
 */
public class Session {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");

    private String name;
    private LocalDate date;
    private String location;
    private LocalTime startTime;
    private LocalTime endTime;
    private int headcount;

    public Session(String name, String dateStr, String location,
                   String startTimeStr, String endTimeStr, int headcount)
            throws IllegalArgumentException {
        this.name = name;
        this.location = location;
        this.headcount = headcount;

        try {
            this.date = LocalDate.parse(dateStr, DATE_FORMATTER);
            this.startTime = LocalTime.parse(startTimeStr, TIME_FORMATTER);
            this.endTime = LocalTime.parse(endTimeStr, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date or time format. "
                    + "Use 'd MMMM yyyy' for dates and 'HHmm' for times.");
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("%s %s (%s - %s)",
                name,
                date.format(DATE_FORMATTER),
                startTime.format(TIME_FORMATTER),
                endTime.format(TIME_FORMATTER));
    }
}

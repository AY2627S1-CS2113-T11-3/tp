//@@author DaynaG3
package seedu.bob;

/** A completely validated snapshot and its origin, used to report recovery and schedule a repair save. */
public record LoadResult(AppSnapshot snapshot, Source source) {

    /** Identifies a normal load, recovery, or a genuinely new data directory. */
    public enum Source {
        MAIN, BACKUP, NEW
    }
}

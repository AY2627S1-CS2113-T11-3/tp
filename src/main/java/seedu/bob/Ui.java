package seedu.bob;

/**
 * Provides shared display constants for console output.
 *
 * @author Lokwx
 */
public class Ui {

    public static final String DIVIDER = "--------------------------------------------------------------------";

    /** Every command and its format, grouped by feature, as displayed by {@code help}. */
    public static final String HELP_MESSAGE = String.join("\n",
            DIVIDER,
            "Available commands",
            DIVIDER,
            "Inventory",
            "  add-i n/NAME c/CATEGORY [q/QUANTITY]",
            "  delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY",
            "  list-i",
            "Lab sessions",
            "  add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT",
            "  delete-s INDEX",
            "  list-s",
            "Lab preparation",
            "  add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]",
            "  delete-p s/SESSION_INDEX i/ITEM_INDEX q/TOTAL_QUANTITY",
            "  list-p s/SESSION_INDEX",
            "Student requests",
            "  add-r n/STUDENT_NAME i/ITEM q/QUANTITY d/DEADLINE",
            "  delete-r INDEX",
            "  list-r",
            "  mark-r INDEX STATUS",
            "Contacts",
            "  add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]",
            "  delete-c INDEX",
            "  list-c",
            "Other",
            "  help",
            "  Bye",
            DIVIDER);
}

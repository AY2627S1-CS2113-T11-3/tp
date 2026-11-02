//@@author gavinlimsh
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.bob.exception.BrokenItemException;
import seedu.bob.exception.CommandException;

/**
 * Verifies report validation and date boundaries independently of the computer's current date.
 */
class BrokenItemTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-23T04:00:00Z"), ZoneOffset.UTC);

    @Test
    void constructor_validValues_trimsTextAndRetainsContact() throws CommandException {
        Contact contact = new Contact("Maintenance", "00123456", null, null);
        BrokenItem item = new BrokenItem(" Meter ", " Equipment ", 2, " Cracked screen ",
                " 23 October 2026 ", contact, CLOCK);
        assertEquals("Meter", item.getName());
        assertEquals("Equipment", item.getCategory());
        assertEquals(2, item.getQuantity());
        assertEquals("Cracked screen", item.getFaultDescription());
        assertEquals(LocalDate.of(2026, 10, 23), item.getReportedDate());
        assertSame(contact, item.getContact());
        assertTrue(item.hasValidState());
    }

    @Test
    void constructor_blankFields_rejectsEachRequiredField() {
        for (String blank : new String[]{null, "", " \t "}) {
            assertThrows(BrokenItemException.class,
                    () -> new BrokenItem(blank, "Equipment", 1, "Fault", "23 October 2026", null, CLOCK));
            assertThrows(BrokenItemException.class,
                    () -> new BrokenItem("Meter", blank, 1, "Fault", "23 October 2026", null, CLOCK));
            assertThrows(BrokenItemException.class,
                    () -> new BrokenItem("Meter", "Equipment", 1, blank, "23 October 2026", null, CLOCK));
            assertThrows(BrokenItemException.class,
                    () -> new BrokenItem("Meter", "Equipment", 1, "Fault", blank, null, CLOCK));
        }
    }

    @Test
    void constructor_nonPositiveQuantity_rejects() {
        for (int quantity : new int[]{Integer.MIN_VALUE, -1, 0}) {
            assertThrows(BrokenItemException.class, () -> create(quantity, "23 October 2026"));
        }
    }

    @Test
    void constructor_invalidOrFutureDate_rejects() {
        List<String> dates = List.of(
                "31 February 2026", "29 February 2025", "2026-10-23", "24 October 2026", "1 January 9999");
        for (String date : dates) {
            assertThrows(BrokenItemException.class, () -> create(1, date), date);
        }
    }

    @Test
    void constructor_pastLeapDayAndToday_accepts() throws CommandException {
        assertEquals(LocalDate.of(2024, 2, 29), create(1, "29 February 2024").getReportedDate());
        assertEquals(LocalDate.of(2026, 10, 23), create(1, "23 October 2026").getReportedDate());
        assertNull(create(Integer.MAX_VALUE, "23 October 2026").getContact());
    }

    @Test
    void constructor_nullClock_rejects() {
        assertThrows(NullPointerException.class,
                () -> new BrokenItem("Meter", "Equipment", 1, "Fault", "23 October 2026", null, null));
    }

    @Test
    void toString_validReport_formatsDetails() throws CommandException {
        assertEquals("2x Meter [Equipment], fault: Fault, reported 23 October 2026",
                create(2, "23 October 2026").toString());
    }

    /** Creates a report against a fixed reference date. */
    private BrokenItem create(int quantity, String date) throws BrokenItemException {
        return new BrokenItem("Meter", "Equipment", quantity, "Fault", date, null, CLOCK);
    }
}

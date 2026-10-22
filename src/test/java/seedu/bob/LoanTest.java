package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.CommandFormatException;
import seedu.bob.exception.InvalidQuantityException;
import seedu.bob.exception.LoanException;

/**
 * Verifies loan validation, backlog dates, and reuse of the existing item and contact models.
 */
class LoanTest {

    @Test
    void constructor_validDetails_preservesItemBorrowerAndDate() throws CommandException {
        Contact borrower = new Contact("Alex Tan", "00123456", null, null);
        Loan loan = new Loan("Red LED", "Lab Components", 3, borrower, "29 February 2096");

        assertEquals("Red LED", loan.getName());
        assertEquals("Lab Components", loan.getCategory());
        assertEquals(3, loan.getQuantity());
        assertEquals(borrower, loan.getBorrower());
        assertEquals(LocalDate.of(2096, 2, 29), loan.getDueDate());
        assertEquals("3x Red LED [Lab Components] to Alex Tan (00123456), due 29 February 2096",
                loan.toString());
    }

    @Test
    void constructor_pastTodayAndFutureDates_acceptsAll() throws CommandException {
        Contact borrower = new Contact("Alex", "91234567", null, null);
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH);
        for (LocalDate date : new LocalDate[] {today.minusDays(1), today, today.plusDays(1)}) {
            Loan loan = new Loan("Wire", "Components", 1, borrower, date.format(formatter));
            assertEquals(date, loan.getDueDate());
        }
    }

    @Test
    void constructor_invalidDate_rejectsImpossibleAndMalformedDates() throws CommandException {
        Contact borrower = new Contact("Alex", "91234567", null, null);
        String[] dates = {null, "", "31 February 2099", "29 February 2099", "2099-10-30", "30 october 2099"};
        for (String date : dates) {
            LoanException error = assertThrows(LoanException.class,
                    () -> new Loan("Wire", "Components", 1, borrower, date));
            assertEquals("Due date must be a valid date in d MMMM yyyy format (e.g., 30 October 2099).",
                    error.getMessage());
        }
    }

    @Test
    void constructor_invalidItemDetails_rejectsBlankAndNonPositiveValues() throws CommandException {
        Contact borrower = new Contact("Alex", "91234567", null, null);
        assertThrows(CommandFormatException.class,
                () -> new Loan(" ", "Components", 1, borrower, "30 October 2099"));
        assertThrows(CommandFormatException.class,
                () -> new Loan("Wire", null, 1, borrower, "30 October 2099"));
        assertThrows(InvalidQuantityException.class,
                () -> new Loan("Wire", "Components", 0, borrower, "30 October 2099"));
        assertThrows(InvalidQuantityException.class,
                () -> new Loan("Wire", "Components", -1, borrower, "30 October 2099"));
        assertThrows(NullPointerException.class,
                () -> new Loan("Wire", "Components", 1, null, "30 October 2099"));
    }
}

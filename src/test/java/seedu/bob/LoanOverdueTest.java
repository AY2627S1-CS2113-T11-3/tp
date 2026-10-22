package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.LoanException;

/**
 * Verifies overdue boundaries, filtering, and live date rollover without using the computer's clock.
 */
class LoanOverdueTest {

    private final Inventory inventory = new Inventory();
    private final AdjustableClock clock = new AdjustableClock(
            Instant.parse("2026-10-22T04:00:00Z"), ZoneId.of("Asia/Singapore"));
    private final LoanManager loans = new LoanManager(inventory, clock);
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private PrintStream originalOutput;
    private PrintStream capturedOutput;

    @BeforeEach
    void setUp() throws CommandException {
        originalOutput = System.out;
        capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8);
        System.setOut(capturedOutput);
        inventory.addItem(new InventoryItem("Wire", "Components", 20));
        output.reset();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOutput);
        capturedOutput.close();
    }

    @Test
    void isOverdue_beforeOnAndAfterDueDate_usesStrictDayBoundary() throws CommandException {
        Loan loan = createLoan("22 October 2026");
        assertFalse(loan.isOverdue(LocalDate.of(2026, 10, 21)));
        assertFalse(loan.isOverdue(LocalDate.of(2026, 10, 22)));
        assertTrue(loan.isOverdue(LocalDate.of(2026, 10, 23)));
        assertThrows(NullPointerException.class, () -> loan.isOverdue(null));
    }

    @Test
    void isOverdue_yearAndLeapBoundaries_comparesCalendarDates() throws CommandException {
        assertTrue(createLoan("31 December 2026").isOverdue(LocalDate.of(2027, 1, 1)));
        Loan leapDay = createLoan("29 February 2028");
        assertFalse(leapDay.isOverdue(LocalDate.of(2028, 2, 29)));
        assertTrue(leapDay.isOverdue(LocalDate.of(2028, 3, 1)));
        assertTrue(createLoan("28 February 2027").isOverdue(LocalDate.of(2027, 3, 1)));
    }

    @Test
    void addLoan_pastDate_acceptsAndWarnsAfterSuccess() throws CommandException {
        loans.addLoan(createLoan("21 October 2026"));
        assertEquals("Successfully added loan: 1x Wire [Components] to Alex (91234567), due 21 October 2026\n"
                + "Warning: this loan is already overdue.\n", response());
        assertEquals(19, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(1, loans.getLoans().size());
    }

    @Test
    void addLoan_dueTodayOrTomorrow_doesNotWarn() throws CommandException {
        loans.addLoan(createLoan("22 October 2026"));
        loans.addLoan(createLoan("23 October 2026"));
        assertFalse(response().contains("Warning:"));
        output.reset();
        loans.listOverdueLoans();
        assertEquals(display("Overdue loans", "No overdue loans."), response());
    }

    @Test
    void listLoans_mixedDates_preservesFullIndicesAndDoesNotMutateState() throws CommandException {
        loans.addLoan(createLoan("22 October 2026"));
        loans.addLoan(createLoan("21 October 2026"));
        loans.addLoan(createLoan("23 October 2026"));
        loans.addLoan(createLoan("20 October 2026"));
        List<Loan> before = loans.getLoans();
        output.reset();
        loans.listOverdueLoans();
        assertEquals(display("Overdue loans",
                "2. 1x Wire [Components] to Alex (91234567), due 21 October 2026 [OVERDUE]",
                "4. 1x Wire [Components] to Alex (91234567), due 20 October 2026 [OVERDUE]"), response());
        output.reset();
        loans.listLoans();
        assertEquals(display("Loans",
                "1. 1x Wire [Components] to Alex (91234567), due 22 October 2026",
                "2. 1x Wire [Components] to Alex (91234567), due 21 October 2026 [OVERDUE]",
                "3. 1x Wire [Components] to Alex (91234567), due 23 October 2026",
                "4. 1x Wire [Components] to Alex (91234567), due 20 October 2026 [OVERDUE]"), response());
        assertEquals(before, loans.getLoans());
        assertEquals(16, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void listLoans_clockCrossesMidnight_recomputesStatusWithoutRestart() throws CommandException {
        clock.setInstant(Instant.parse("2026-10-22T15:59:59Z"));
        loans.addLoan(createLoan("22 October 2026"));
        output.reset();
        loans.listOverdueLoans();
        assertEquals(display("Overdue loans", "No overdue loans."), response());

        clock.advance(Duration.ofSeconds(1));
        output.reset();
        loans.listOverdueLoans();
        assertEquals(display("Overdue loans",
                "1. 1x Wire [Components] to Alex (91234567), due 22 October 2026 [OVERDUE]"), response());
        output.reset();
        loans.listLoans();
        assertTrue(response().contains("[OVERDUE]"));
        assertEquals(19, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void listOverdueLoans_sameInstantInDifferentZones_usesTheClockLocalDate() throws CommandException {
        clock.setInstant(Instant.parse("2026-10-22T16:00:00Z"));
        LoanManager utcLoans = new LoanManager(inventory, clock.withZone(ZoneOffset.UTC));
        utcLoans.addLoan(createLoan("22 October 2026"));
        assertFalse(response().contains("Warning:"));
        output.reset();
        loans.addLoan(createLoan("22 October 2026"));
        assertTrue(response().contains("Warning: this loan is already overdue."));
        output.reset();
        utcLoans.listOverdueLoans();
        assertEquals(display("Overdue loans", "No overdue loans."), response());
        output.reset();
        loans.listOverdueLoans();
        assertTrue(response().contains("due 22 October 2026 [OVERDUE]"));
    }

    @Test
    void overdueLoans_returnAndCorrection_useFullIndicesAndRemoveRows() throws CommandException {
        Loan current = createLoan("22 October 2026");
        loans.addLoan(current);
        loans.addLoan(createLoan("21 October 2026"));
        loans.addLoan(createLoan("20 October 2026"));
        loans.returnLoan(2);
        output.reset();
        loans.listOverdueLoans();
        assertEquals(display("Overdue loans",
                "2. 1x Wire [Components] to Alex (91234567), due 20 October 2026 [OVERDUE]"), response());
        loans.deleteLoan(2);
        output.reset();
        loans.listOverdueLoans();
        assertEquals(display("Overdue loans", "No overdue loans."), response());
        assertEquals(List.of(current), loans.getLoans());
        assertEquals(19, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void addLoan_rejectedBacklog_doesNotWarnOrChangeStock() throws CommandException {
        Loan tooLarge = new Loan("Wire", "Components", 21,
                new Contact("Alex", "91234567", null, null), "21 October 2026");
        assertThrows(LoanException.class, () -> loans.addLoan(tooLarge));
        assertEquals("", response());
        assertTrue(loans.getLoans().isEmpty());
        assertEquals(20, inventory.getAvailableQuantity("Wire", "Components"));
    }

    /** Creates a one-unit loan with a fixed borrower for date behavior tests. */
    private Loan createLoan(String date) throws CommandException {
        return new Loan("Wire", "Components", 1, new Contact("Alex", "91234567", null, null), date);
    }

    /** Returns captured stdout using the same newline convention as the expected fixtures. */
    private String response() {
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    /** Builds an expected list frame around independently specified rows. */
    private String display(String title, String... rows) {
        return Ui.DIVIDER + "\n" + title + "\n" + Ui.DIVIDER + "\n"
                + String.join("\n", rows) + "\n" + Ui.DIVIDER + "\n";
    }

    /**
     * Provides a controllable instant, shared by zone views, without changing system time.
     */
    private static class AdjustableClock extends Clock {
        private final AtomicReference<Instant> currentInstant;
        private final ZoneId zone;

        /** Creates a clock whose time can be advanced by the test. */
        AdjustableClock(Instant instant, ZoneId zone) {
            this(new AtomicReference<>(instant), zone);
        }

        /** Creates a timezone view over the same controllable instant. */
        private AdjustableClock(AtomicReference<Instant> currentInstant, ZoneId zone) {
            this.currentInstant = currentInstant;
            this.zone = zone;
        }

        void setInstant(Instant instant) {
            currentInstant.set(instant);
        }

        /** Advances every timezone view by the supplied duration. */
        void advance(Duration duration) {
            currentInstant.updateAndGet(instant -> instant.plus(duration));
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new AdjustableClock(currentInstant, zone);
        }

        @Override
        public Instant instant() {
            return currentInstant.get();
        }
    }
}

package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.LoanException;

/**
 * Verifies query snapshots, overdue boundaries, and live date rollover without console rendering.
 */
class LoanOverdueTest {

    private final Inventory inventory = new Inventory();
    private final AdjustableClock clock = new AdjustableClock(
            Instant.parse("2026-10-22T04:00:00Z"), ZoneId.of("Asia/Singapore"));
    private final LoanManager loans = new LoanManager(inventory, clock);

    @BeforeEach
    void setUp() throws CommandException {
        inventory.addItem(new InventoryItem("Wire", "Components", 20));
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
    void addLoan_pastDate_returnsOverdueResult() throws CommandException {
        Loan loan = createLoan("21 October 2026");
        LoanAdditionResult result = loans.addLoan(loan);

        assertTrue(result.isOverdue());
        assertSame(loan, result.getLoan());
        assertEquals(19, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(List.of(loan), loans.getLoans());
    }

    @Test
    void addLoan_dueTodayOrTomorrow_returnsCurrentResults() throws CommandException {
        assertFalse(loans.addLoan(createLoan("22 October 2026")).isOverdue());
        assertFalse(loans.addLoan(createLoan("23 October 2026")).isOverdue());

        assertTrue(loans.getOverdueLoanEntries().isEmpty());
    }

    @Test
    void getLoanEntries_mixedDates_preservesFullIndicesAndDoesNotMutateState() throws CommandException {
        loans.addLoan(createLoan("22 October 2026"));
        loans.addLoan(createLoan("21 October 2026"));
        loans.addLoan(createLoan("23 October 2026"));
        loans.addLoan(createLoan("20 October 2026"));
        List<Loan> before = loans.getLoans();

        List<LoanListEntry> overdue = loans.getOverdueLoanEntries();
        List<LoanListEntry> all = loans.getLoanEntries();

        assertEquals(List.of(2, 4), overdue.stream().map(LoanListEntry::getIndex).toList());
        assertEquals(List.of(before.get(1), before.get(3)),
                overdue.stream().map(LoanListEntry::getLoan).toList());
        assertTrue(overdue.stream().allMatch(LoanListEntry::isOverdue));
        assertEquals(List.of(1, 2, 3, 4), all.stream().map(LoanListEntry::getIndex).toList());
        assertEquals(before, all.stream().map(LoanListEntry::getLoan).toList());
        assertEquals(List.of(false, true, false, true), all.stream().map(LoanListEntry::isOverdue).toList());
        assertThrows(UnsupportedOperationException.class, overdue::clear);
        assertEquals(before, loans.getLoans());
        assertEquals(16, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void getLoanEntries_clockCrossesMidnight_recomputesStatusAndPreservesOldResults() throws CommandException {
        clock.setInstant(Instant.parse("2026-10-22T15:59:59Z"));
        LoanAdditionResult addition = loans.addLoan(createLoan("22 October 2026"));
        List<LoanListEntry> previous = loans.getLoanEntries();
        assertFalse(addition.isOverdue());
        assertFalse(previous.get(0).isOverdue());
        assertTrue(loans.getOverdueLoanEntries().isEmpty());

        clock.advance(Duration.ofSeconds(1));
        List<LoanListEntry> current = loans.getOverdueLoanEntries();

        assertFalse(addition.isOverdue());
        assertFalse(previous.get(0).isOverdue());
        assertEquals(1, current.size());
        assertEquals(1, current.get(0).getIndex());
        assertTrue(current.get(0).isOverdue());
        assertTrue(loans.getLoanEntries().get(0).isOverdue());
        assertEquals(19, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void getLoanEntries_multipleRows_readsClockOncePerQuery() throws CommandException {
        loans.addLoan(createLoan("21 October 2026"));
        loans.addLoan(createLoan("22 October 2026"));
        int before = clock.getReadCount();

        assertEquals(2, loans.getLoanEntries().size());
        assertEquals(before + 1, clock.getReadCount());
        assertEquals(1, loans.getOverdueLoanEntries().size());
        assertEquals(before + 2, clock.getReadCount());
    }

    @Test
    void getOverdueLoanEntries_sameInstantInDifferentZones_usesTheClockLocalDate() throws CommandException {
        clock.setInstant(Instant.parse("2026-10-22T16:00:00Z"));
        LoanManager utcLoans = new LoanManager(inventory, clock.withZone(ZoneOffset.UTC));
        assertFalse(utcLoans.addLoan(createLoan("22 October 2026")).isOverdue());
        assertTrue(loans.addLoan(createLoan("22 October 2026")).isOverdue());

        assertTrue(utcLoans.getOverdueLoanEntries().isEmpty());
        assertEquals(1, loans.getOverdueLoanEntries().size());
        assertTrue(loans.getOverdueLoanEntries().get(0).isOverdue());
        assertEquals(18, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void overdueLoans_returnAndCorrection_useFullIndicesAndRemoveRows() throws CommandException {
        Loan current = createLoan("22 October 2026");
        loans.addLoan(current);
        loans.addLoan(createLoan("21 October 2026"));
        loans.addLoan(createLoan("20 October 2026"));
        loans.returnLoan(2);

        List<LoanListEntry> overdue = loans.getOverdueLoanEntries();
        assertEquals(1, overdue.size());
        assertEquals(2, overdue.get(0).getIndex());
        assertEquals(LocalDate.of(2026, 10, 20), overdue.get(0).getLoan().getDueDate());

        loans.deleteLoan(2);
        assertTrue(loans.getOverdueLoanEntries().isEmpty());
        assertEquals(List.of(current), loans.getLoans());
        assertEquals(19, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void addLoan_rejectedBacklog_preservesStockAndQueryResults() throws CommandException {
        Loan tooLarge = new Loan("Wire", "Components", 21,
                new Contact("Alex", "91234567", null, null), "21 October 2026");

        assertThrows(LoanException.class, () -> loans.addLoan(tooLarge));

        assertTrue(loans.getLoans().isEmpty());
        assertTrue(loans.getLoanEntries().isEmpty());
        assertTrue(loans.getOverdueLoanEntries().isEmpty());
        assertEquals(20, inventory.getAvailableQuantity("Wire", "Components"));
    }

    /**
     * Creates a one-unit loan with a fixed borrower for date behavior tests.
     */
    private Loan createLoan(String date) throws CommandException {
        return new Loan("Wire", "Components", 1, new Contact("Alex", "91234567", null, null), date);
    }

    /**
     * Provides a controllable instant, shared by zone views, without changing system time.
     */
    private static class AdjustableClock extends Clock {
        /** Shared by every zone view so advancing the test clock updates all views together. */
        private final AtomicReference<Instant> currentInstant;
        private final ZoneId zone;
        /** Counts instant reads so a query can be checked for a single reference date. */
        private int readCount;

        /**
         * Creates a clock whose time can be advanced by the test.
         */
        AdjustableClock(Instant instant, ZoneId zone) {
            this(new AtomicReference<>(instant), zone);
        }

        /**
         * Creates a timezone view over the same controllable instant.
         */
        private AdjustableClock(AtomicReference<Instant> currentInstant, ZoneId zone) {
            this.currentInstant = currentInstant;
            this.zone = zone;
        }

        /**
         * Sets the controllable instant for this clock and every zone view sharing it.
         *
         * @param instant Instant used by subsequent clock reads.
         */
        void setInstant(Instant instant) {
            currentInstant.set(instant);
        }

        /**
         * Advances every timezone view by the supplied duration.
         */
        void advance(Duration duration) {
            currentInstant.updateAndGet(instant -> instant.plus(duration));
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        /**
         * Returns a clock for the requested zone that shares this clock's controllable instant.
         *
         * @param zone Zone used to interpret the shared instant.
         * @return Another view of the same controllable time.
         */
        @Override
        public Clock withZone(ZoneId zone) {
            return new AdjustableClock(currentInstant, zone);
        }

        /**
         * Returns the number of instant reads performed through this zone view.
         */
        int getReadCount() {
            return readCount;
        }

        @Override
        public Instant instant() {
            readCount++;
            return currentInstant.get();
        }
    }
}

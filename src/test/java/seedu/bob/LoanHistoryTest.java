//@@author DaynaG3
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;

/** Verifies permanent identities, terminal lifecycle states, and stock-safe history changes. */
class LoanHistoryTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-22T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void close_duplicateLoans_keepsDetailsAndIdsWhileRenumberingActiveIndices() throws CommandException {
        Inventory inventory = stock(5);
        LoanManager manager = new LoanManager(inventory, CLOCK);
        Loan first = loan(2);
        Loan second = loan(1);
        manager.addLoan(first);
        manager.addLoan(second);
        List<LoanRecord> before = manager.getLoanHistory();

        assertSame(first, manager.returnLoan(1));
        assertEquals(List.of(second), manager.getLoans());
        assertEquals(1, manager.getLoanEntries().getFirst().getIndex());
        assertSame(second, manager.deleteLoan(1));

        List<LoanRecord> records = manager.getLoanHistory();
        assertEquals(List.of(1L, 2L), records.stream().map(LoanRecord::loanId).toList());
        assertEquals(List.of(LoanRecord.Status.RETURNED, LoanRecord.Status.VOIDED),
                records.stream().map(LoanRecord::status).toList());
        assertEquals(List.of(records.getFirst()), manager.getReturnedLoans());
        assertEquals(OffsetDateTime.now(CLOCK), records.getFirst().recordedAt());
        assertEquals(OffsetDateTime.now(CLOCK), records.getFirst().closedAt());
        assertEquals(LoanRecord.Status.ACTIVE, before.getFirst().status());
        assertNull(before.getFirst().closedAt());
        assertEquals(5, inventory.getAvailableQuantity("Meter", "Equipment"));
        assertTrue(manager.getLoans().isEmpty());
        assertThrows(CommandException.class, () -> manager.returnLoan(1));
        assertEquals(5, inventory.getAvailableQuantity("Meter", "Equipment"));
    }

    @Test
    void close_stockOverflow_keepsOriginalActiveRecordAndInventory() throws CommandException {
        Inventory inventory = stock(Integer.MAX_VALUE);
        LoanManager manager = new LoanManager(inventory, CLOCK);
        manager.addLoan(loan(2));
        inventory.restoreStock("Meter", "Equipment", 2);
        LoanRecord original = manager.getLoanHistory().getFirst();

        assertThrows(CommandException.class, () -> manager.returnLoan(1));
        assertThrows(CommandException.class, () -> manager.deleteLoan(1));

        assertSame(original, manager.getLoanHistory().getFirst());
        assertNull(original.closedAt());
        assertEquals(Integer.MAX_VALUE, inventory.getAvailableQuantity("Meter", "Equipment"));
    }

    @Test
    void add_rejectedLoan_doesNotConsumeIdAndClosedIdsAreNotReused() throws CommandException {
        Inventory inventory = stock(5);
        LoanManager manager = new LoanManager(inventory, CLOCK);

        assertThrows(CommandException.class, () -> manager.addLoan(loan(6)));
        manager.addLoan(loan(1));
        manager.returnLoan(1);
        manager.addLoan(loan(1));

        assertEquals(List.of(1L, 2L), manager.getLoanHistory().stream().map(LoanRecord::loanId).toList());
        assertThrows(UnsupportedOperationException.class, () -> manager.getLoanHistory().clear());
        assertThrows(UnsupportedOperationException.class, () -> manager.getReturnedLoans().clear());
        assertThrows(UnsupportedOperationException.class, () -> manager.getLoans().clear());
    }

    @Test
    void record_invalidLifecycle_rejectsMissingClosureOrRepeatedClosure() throws CommandException {
        Loan loan = loan(1);
        OffsetDateTime timestamp = OffsetDateTime.now(CLOCK);
        LoanRecord active = new LoanRecord(1, loan, LoanRecord.Status.ACTIVE, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> new LoanRecord(0, loan, LoanRecord.Status.ACTIVE, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new LoanRecord(1, loan, LoanRecord.Status.RETURNED, timestamp, null));
        assertThrows(IllegalArgumentException.class,
                () -> new LoanRecord(1, loan, LoanRecord.Status.ACTIVE, timestamp, timestamp));
        assertThrows(IllegalArgumentException.class, () -> active.close(LoanRecord.Status.ACTIVE, timestamp));
        LoanRecord returned = active.close(LoanRecord.Status.RETURNED, timestamp);
        assertThrows(IllegalStateException.class, () -> returned.close(LoanRecord.Status.VOIDED, timestamp));
        assertNull(returned.recordedAt());
    }

    @Test
    void history_offsetTimestamps_keepsPreciseStorageValuesAndOriginalLocalDates() throws CommandException {
        OffsetDateTime recorded = OffsetDateTime.parse("2026-10-31T00:30:00.123456789+08:00");
        OffsetDateTime closed = OffsetDateTime.parse("2026-11-01T00:10:00+08:00");
        LoanRecord record = new LoanRecord(7, loan(1), LoanRecord.Status.RETURNED, recorded, closed);
        LoanRecord restored = LoanRecord.restore(record.toSnapshot());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream captured = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(captured);
            Ui.showLoanHistory(List.of(restored), false);
        } finally {
            System.setOut(original);
        }

        assertEquals(recorded, restored.recordedAt());
        assertEquals(closed, restored.closedAt());
        assertEquals("2026-10-31T00:30:00.123456789+08:00", record.toSnapshot().recordedAt());
        assertTrue(output.toString(StandardCharsets.UTF_8)
                .contains("Recorded: 31 October 2026 | Closed: 1 November 2026"));
    }

    /** Creates usable inventory without involving another application state. */
    private Inventory stock(int quantity) throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Meter", "Equipment", quantity));
        return inventory;
    }

    /** Creates a validated loan whose captured details are independent of inventory. */
    private Loan loan(int quantity) throws CommandException {
        return new Loan("Meter", "Equipment", quantity,
                new Contact("Jamie", "01234567", null, null), "31 December 2099");
    }
}

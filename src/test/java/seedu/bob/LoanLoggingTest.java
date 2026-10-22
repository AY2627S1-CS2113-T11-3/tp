package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.bob.exception.CategoryNotFoundException;
import seedu.bob.exception.CommandException;
import seedu.bob.exception.InvalidLoanIndexException;
import seedu.bob.exception.LoanException;

/**
 * Verifies useful loan diagnostics, retained failures, and omission of borrower details from logs.
 */
class LoanLoggingTest {

    private static final String BORROWER_NAME = "Private Student";
    private static final String BORROWER_PHONE = "0088888877";
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-22T12:00:00Z"), ZoneOffset.UTC);

    private final Inventory inventory = new Inventory();
    private final LoanManager loans = new LoanManager(inventory, CLOCK);
    private final List<Logger> loggers = List.of(Logger.getLogger(LoanManager.class.getName()),
            Logger.getLogger(Inventory.class.getName()), Logger.getLogger(Parser.class.getName()));
    private final ArrayList<Level> previousLevels = new ArrayList<>();
    private final ArrayList<Boolean> previousParentHandlers = new ArrayList<>();
    private final RecordingHandler handler = new RecordingHandler();
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private PrintStream originalOutput;
    private PrintStream capturedOutput;

    @BeforeEach
    void setUp() throws CommandException {
        for (Logger logger : loggers) {
            previousLevels.add(logger.getLevel());
            previousParentHandlers.add(logger.getUseParentHandlers());
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
            logger.addHandler(handler);
        }
        handler.setLevel(Level.ALL);
        originalOutput = System.out;
        capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8);
        System.setOut(capturedOutput);
        inventory.addItem(new InventoryItem("Wire", "Components", 10));
        output.reset();
        handler.records.clear();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOutput);
        capturedOutput.close();
        for (int i = 0; i < loggers.size(); i++) {
            Logger logger = loggers.get(i);
            logger.removeHandler(handler);
            logger.setLevel(previousLevels.get(i));
            logger.setUseParentHandlers(previousParentHandlers.get(i));
        }
        for (LogRecord record : handler.records) {
            assertNoBorrowerDetails(record.getMessage());
            if (record.getParameters() != null) {
                for (Object parameter : record.getParameters()) {
                    assertNoBorrowerDetails(String.valueOf(parameter));
                }
            }
            for (Throwable cause = record.getThrown(); cause != null; cause = cause.getCause()) {
                assertNoBorrowerDetails(cause.toString());
            }
        }
    }

    @Test
    void loanLifecycle_success_logsDistinctOperationsAndQuantitiesAtFineLevel() throws CommandException {
        loans.addLoan(createLoan("Wire", "Components", 2));
        loans.deleteLoan(1);
        loans.addLoan(createLoan("Wire", "Components", 3));
        loans.returnLoan(1);
        loans.listLoans();
        loans.listOverdueLoans();

        assertEquals(List.of(
                "Recorded loan: Wire in category Components, quantity=2, due=2099-10-30, overdue=false, active=1",
                "Deleted incorrect loan: index=1, item=Wire, category=Components, quantity=2, active=0",
                "Recorded loan: Wire in category Components, quantity=3, due=2099-10-30, overdue=false, active=1",
                "Returned loan: index=1, item=Wire, category=Components, quantity=3, active=0",
                "Listing loans: overdueOnly=false, active=0, date=2026-10-22",
                "Listing loans: overdueOnly=true, active=0, date=2026-10-22"), messagesFrom(LoanManager.class));
        assertTrue(messagesFrom(Inventory.class).contains(
                "Lent inventory stock: Wire in category Components, quantity=3, remaining=7"));
        assertTrue(messagesFrom(Inventory.class).contains(
                "Restored loan stock: Wire in category Components, quantity=3, available=10"));
        assertTrue(handler.records.stream().allMatch(record -> record.getLevel() == Level.FINE));
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void loanLifecycle_backlogDepletesStock_logsOverdueStatusAndRecreation() throws CommandException {
        Loan backlog = new Loan("Wire", "Components", 10,
                new Contact(BORROWER_NAME, BORROWER_PHONE, null, null), "1 January 2000");
        loans.addLoan(backlog);
        loans.returnLoan(1);

        assertTrue(messagesFrom(LoanManager.class).contains(
                "Recorded loan: Wire in category Components, quantity=10, due=2000-01-01, overdue=true, active=1"));
        assertTrue(messagesFrom(Inventory.class).contains(
                "Recreated loan stock: Wire in category Components, quantity=10"));
        assertTrue(handler.records.stream().allMatch(record -> record.getLevel() == Level.FINE));
        assertTrue(loans.getLoans().isEmpty());
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void addLoan_rejectedStock_logsReasonsWithoutSuccessOrMutation() throws CommandException {
        assertThrows(LoanException.class, () -> loans.addLoan(createLoan("Wire", "Components", 11)));
        assertThrows(LoanException.class, () -> loans.addLoan(createLoan("Battery", "Components", 1)));
        assertThrows(CategoryNotFoundException.class, () -> loans.addLoan(createLoan("Wire", "Unknown", 1)));

        assertEquals(List.of(
                "Rejected lending: Wire in category Components, requested=11, available=10",
                "Rejected lending: missing item=Battery in category Components",
                "Rejected lending: missing category=Unknown, item=Wire"), messagesFrom(Inventory.class));
        assertTrue(messagesFrom(LoanManager.class).isEmpty());
        assertTrue(handler.records.stream().allMatch(record -> record.getLevel() == Level.FINE));
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
        assertTrue(loans.getLoans().isEmpty());
    }

    @Test
    void removeLoan_invalidIndex_logsRequestedPositionAndActiveCount() {
        assertThrows(InvalidLoanIndexException.class, () -> loans.deleteLoan(0));
        assertThrows(InvalidLoanIndexException.class, () -> loans.returnLoan(1));

        assertEquals(List.of("Rejected loan removal: index=0, active=0",
                "Rejected loan removal: index=1, active=0"), messagesFrom(LoanManager.class));
        assertTrue(handler.records.stream().allMatch(record -> record.getLevel() == Level.FINE));
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void removeLoan_restorationOverflow_logsRejectionAndKeepsRecord() throws CommandException {
        Loan loan = createLoan("wire", "COMPONENTS", 10);
        loans.addLoan(loan);
        assertTrue(messagesFrom(Inventory.class).contains(
                "Lent inventory stock: Wire in category Components, quantity=10, remaining=0"));
        inventory.addItem(new InventoryItem("Wire", "Components", Integer.MAX_VALUE));
        handler.records.clear();

        assertThrows(LoanException.class, () -> loans.deleteLoan(1));
        assertThrows(LoanException.class, () -> loans.returnLoan(1));

        String rejection = "Rejected loan stock restoration: Wire in category Components, requested=10, "
                + "available=2147483647, maximum=2147483647";
        assertEquals(List.of(rejection, rejection), messagesFrom(Inventory.class));
        assertTrue(messagesFrom(LoanManager.class).isEmpty());
        assertEquals(List.of(loan), loans.getLoans());
        assertEquals(Integer.MAX_VALUE, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void handleCommand_expectedLoanErrors_retainsParsingCausesInDebugLogs() {
        Parser parser = new Parser();
        parser.handleCommand("add-l n/Wire c/Components q/1 b/" + BORROWER_NAME + " t/" + BORROWER_PHONE
                + " d/31 February 2099", inventory, new SessionManager(), new ContactManager(), loans);
        parser.handleCommand("return 999999999999999999999", inventory,
                new SessionManager(), new ContactManager(), loans);

        List<LogRecord> records = recordsFrom(Parser.class);
        assertEquals(2, records.size());
        assertEquals("Rejected loan command: add-l", records.get(0).getMessage());
        LoanException dateError = assertInstanceOf(LoanException.class, records.get(0).getThrown());
        assertInstanceOf(DateTimeParseException.class, dateError.getCause());
        InvalidLoanIndexException indexError = assertInstanceOf(InvalidLoanIndexException.class,
                records.get(1).getThrown());
        assertInstanceOf(NumberFormatException.class, indexError.getCause());
        assertTrue(records.stream().allMatch(record -> record.getLevel() == Level.FINE));
        assertEquals("Due date must be a valid date in d MMMM yyyy format (e.g., 30 October 2099).\n"
                + "Loan index must be a positive integer.\n", normalizedOutput());
        assertTrue(loans.getLoans().isEmpty());
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void handleCommand_unexpectedLendingFailure_logsOnceAtParserBoundary() throws CommandException {
        IllegalStateException failure = new IllegalStateException("Simulated stock deduction failure");
        Inventory failingInventory = new FailingLendingInventory(failure);
        failingInventory.addItem(new InventoryItem("Wire", "Components", 10));
        LoanManager failingLoans = new LoanManager(failingInventory, CLOCK);
        handler.records.clear();
        output.reset();

        new Parser().handleCommand("add-l n/Wire c/Components q/1 b/" + BORROWER_NAME + " t/" + BORROWER_PHONE
                + " d/30 October 2099", failingInventory, new SessionManager(), new ContactManager(), failingLoans);

        assertEquals(1, handler.records.size());
        assertEquals(Parser.class.getName(), handler.records.get(0).getLoggerName());
        assertEquals(Level.SEVERE, handler.records.get(0).getLevel());
        assertSame(failure, handler.records.get(0).getThrown());
        assertEquals("An unexpected error occurred while processing the command. Please try again.\n",
                normalizedOutput());
        assertTrue(failingLoans.getLoans().isEmpty());
        assertEquals(10, failingInventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void loanLifecycle_infoLoggingThreshold_keepsDebugDiagnosticsSilent() throws CommandException {
        for (Logger logger : loggers) {
            logger.setLevel(Level.INFO);
        }
        loans.addLoan(createLoan("Wire", "Components", 3));
        loans.returnLoan(1);

        assertTrue(handler.records.isEmpty());
        assertTrue(normalizedOutput().contains("Successfully added loan:"));
        assertTrue(normalizedOutput().contains("Successfully returned loan:"));
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
    }

    /** Creates a loan with recognizable borrower details to detect accidental logging. */
    private Loan createLoan(String name, String category, int quantity) throws CommandException {
        return new Loan(name, category, quantity,
                new Contact(BORROWER_NAME, BORROWER_PHONE, null, null), "30 October 2099");
    }

    /** Selects diagnostic records by the component that emitted them. */
    private List<LogRecord> recordsFrom(Class<?> source) {
        return handler.records.stream().filter(record -> record.getLoggerName().equals(source.getName())).toList();
    }

    /** Returns only the messages from one component, preserving emission order. */
    private List<String> messagesFrom(Class<?> source) {
        return recordsFrom(source).stream().map(LogRecord::getMessage).toList();
    }

    /** Normalizes platform line separators for exact user-output comparisons. */
    private String normalizedOutput() {
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    /** Checks messages and exception chains without confusing stdout confirmations with diagnostics. */
    private void assertNoBorrowerDetails(String text) {
        assertFalse(text.contains(BORROWER_NAME));
        assertFalse(text.contains(BORROWER_PHONE));
    }

    /** Collects log records without writing diagnostics to the console. */
    private static class RecordingHandler extends Handler {
        private final ArrayList<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            if (isLoggable(record)) {
                records.add(record);
            }
        }

        @Override
        public void flush() {
            // Records are stored immediately in memory.
        }

        @Override
        public void close() {
            // This handler owns no external resources.
        }
    }

    /** Simulates an unexpected runtime failure before stock is modified. */
    private static class FailingLendingInventory extends Inventory {
        private final RuntimeException failure;

        FailingLendingInventory(RuntimeException failure) {
            this.failure = failure;
        }

        @Override
        void lendItem(String name, String category, int quantity) {
            throw failure;
        }
    }
}

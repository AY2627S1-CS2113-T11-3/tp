package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.InvalidQuantityException;
import seedu.bob.exception.InvalidRequestDeadlineException;
import seedu.bob.exception.InvalidRequestStatusException;

/**
 * Verifies FINE-level diagnostics for request creation, validation, and status changes.
 *
 * @author Lokwx
 */
class RequestLoggingTest {

    private final Logger logger = Logger.getLogger(Request.class.getName());
    private final RecordingHandler handler = new RecordingHandler();
    private Level originalLevel;
    private boolean originalUseParentHandlers;

    @BeforeEach
    void setUp() {
        originalLevel = logger.getLevel();
        originalUseParentHandlers = logger.getUseParentHandlers();
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
    }

    @AfterEach
    void tearDown() {
        logger.removeHandler(handler);
        logger.setLevel(originalLevel);
        logger.setUseParentHandlers(originalUseParentHandlers);
    }

    @Test
    void constructorAndMarkAs_validValues_logsDetails() throws CommandException {
        Request request = new Request("Alice", "Wire", 5, date(2));

        request.markAs("prepared");

        assertTrue(handler.contains("Created request for Alice for Wire"));
        assertTrue(handler.contains("Marked request for Alice as prepared"));
    }

    @Test
    void constructor_invalidQuantityAndDeadline_logsRejections() {
        assertThrows(InvalidQuantityException.class,
                () -> new Request("Alice", "Wire", 0, date(2)));
        assertThrows(InvalidRequestDeadlineException.class,
                () -> new Request("Alice", "Wire", 1, "1 January 2000"));

        assertTrue(handler.contains("Rejected request quantity: 0"));
        assertTrue(handler.contains("Rejected request deadline: 1 January 2000"));
    }

    @Test
    void markAs_invalidStatus_logsRejection() throws CommandException {
        Request request = new Request("Alice", "Wire", 5, date(2));

        assertThrows(InvalidRequestStatusException.class, () -> request.markAs("done"));

        assertTrue(handler.contains("Rejected request status: done"));
    }

    /**
     * Returns a date the given number of days from today in the documented format.
     */
    private static String date(int daysFromToday) {
        return LocalDate.now().plusDays(daysFromToday)
                .format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH));
    }

    /**
     * Stores log messages so tests can assert on emitted diagnostics.
     */
    private static class RecordingHandler extends Handler {

        private final List<String> messages = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            messages.add(record.getMessage());
        }

        @Override
        public void flush() {
            // No buffered output to flush.
        }

        @Override
        public void close() {
            // No resources to close.
        }

        boolean contains(String text) {
            return messages.stream().anyMatch(message -> message.contains(text));
        }
    }
}

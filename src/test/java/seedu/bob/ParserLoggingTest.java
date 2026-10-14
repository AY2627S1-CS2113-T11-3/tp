package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

/**
 * Verifies diagnostics and user output for unexpected command-processing failures.
 */
class ParserLoggingTest {

    private static final String EXPECTED_MESSAGE =
            "An unexpected error occurred while processing the command. Please try again.\n";

    @Test
    void handleCommand_unexpectedRuntimeFailure_logsCauseAndReportsSafeMessage() {
        IllegalStateException failure = new IllegalStateException("Simulated internal failure");
        Inventory failingInventory = new FailingInventory(failure);
        Logger logger = Logger.getLogger(Parser.class.getName());
        RecordingHandler handler = new RecordingHandler();
        Level originalLevel = logger.getLevel();
        boolean originallyUsesParentHandlers = logger.getUseParentHandlers();
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
            logger.addHandler(handler);
            System.setOut(capturedOutput);

            new Parser().handleCommand("list-i", failingInventory, new SessionManager());
        } finally {
            System.setOut(originalOutput);
            logger.removeHandler(handler);
            logger.setLevel(originalLevel);
            logger.setUseParentHandlers(originallyUsesParentHandlers);
        }

        assertEquals(EXPECTED_MESSAGE, output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n"));
        assertEquals(Level.SEVERE, handler.getRecord().getLevel());
        assertSame(failure, handler.getRecord().getThrown());
    }

    /**
     * Inventory test double that simulates an unexpected display failure.
     */
    private static class FailingInventory extends Inventory {

        private final RuntimeException failure;

        FailingInventory(RuntimeException failure) {
            this.failure = failure;
        }

        @Override
        public void listItems() {
            throw failure;
        }
    }

    /**
     * Retains the most recent log record for assertions.
     */
    private static class RecordingHandler extends Handler {

        private LogRecord record;

        @Override
        public void publish(LogRecord record) {
            this.record = record;
        }

        @Override
        public void flush() {
            // No buffered output to flush.
        }

        @Override
        public void close() {
            // No resources to close.
        }

        LogRecord getRecord() {
            return record;
        }
    }
}

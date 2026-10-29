//@@author Lokwx
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.DuplicateItemException;
import seedu.bob.exception.InsufficientStockException;

/**
 * Verifies inventory diagnostics while restoring shared logger settings after each test.
 */
class InventoryLoggingTest {

    private final Logger logger = Logger.getLogger(Inventory.class.getName());
    private final RecordingHandler handler = new RecordingHandler();
    private Level originalLevel;
    private boolean wasUsingParentHandlers;

    @BeforeEach
    void captureLogs() {
        originalLevel = logger.getLevel();
        wasUsingParentHandlers = logger.getUseParentHandlers();
        logger.setLevel(Level.FINE);
        logger.setUseParentHandlers(false);
        handler.setLevel(Level.FINE);
        logger.addHandler(handler);
    }

    @AfterEach
    void restoreLogger() {
        logger.removeHandler(handler);
        logger.setLevel(originalLevel);
        logger.setUseParentHandlers(wasUsingParentHandlers);
    }

    @Test
    void inventoryChanges_validOperations_logsQuantities() throws CommandException {
        Inventory inventory = new Inventory();

        inventory.addItem(new InventoryItem("Hammer", "Tools", 10));
        inventory.removeItem("tools", 1, 4);
        inventory.removeItem("Tools", 1, 6);

        assertEquals(List.of(
                "Added inventory item: Hammer in category Tools, quantity=10",
                "Removed inventory stock: Hammer in category Tools, removed=4, remaining=6",
                "Removed inventory stock: Hammer in category Tools, removed=6, remaining=0"), handler.messages);
        assertEquals(0, inventory.getAvailableQuantity("Hammer", "Tools"));
    }

    @Test
    void inventoryChanges_rejectedOperations_logsReasonsAndPreservesStock() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Hammer", "Tools", 10));
        handler.messages.clear();

        assertThrows(DuplicateItemException.class,
                () -> inventory.addItem(new InventoryItem("Hammer", "Tools", 2)));
        assertThrows(InsufficientStockException.class, () -> inventory.removeItem("Tools", 1, 11));

        assertEquals(List.of(
                "Rejected duplicate inventory item: Hammer in category Tools",
                "Rejected stock removal: Hammer in category Tools, requested=11, available=10"), handler.messages);
        assertEquals(10, inventory.getAvailableQuantity("Hammer", "Tools"));
    }

    /**
     * Collects log messages in memory without writing to the application's console.
     */
    private static class RecordingHandler extends Handler {
        private final ArrayList<String> messages = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            if (isLoggable(record)) {
                messages.add(record.getMessage());
            }
        }

        @Override
        public void flush() {
            // Messages are stored immediately in memory.
        }

        @Override
        public void close() {
            // This handler does not own any external resources.
        }
    }
}

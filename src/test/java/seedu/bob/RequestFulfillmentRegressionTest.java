//@@author gavinlimsh
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.InsufficientStockException;
import seedu.bob.exception.RequestException;

/**
 * Verifies terminal request status, stock preservation, and completed-request display.
 */
class RequestFulfillmentRegressionTest {

    @Test
    void markAs_fulfilledRequest_rejectsDirectReversalAndRepeat() throws CommandException {
        Request request = createRequest("Alice", 3);
        request.markAs("fulfilled");

        for (String status : List.of("prepared", "fulfilled", " PREPARED ")) {
            assertThrows(RequestException.class, () -> request.markAs(status));
            assertEquals("fulfilled", request.getStatus());
        }
    }

    @Test
    void markRequest_retainedReference_cannotEnableSecondDeduction() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 10));
        RequestManager manager = new RequestManager();
        manager.addRequest(createRequest("Alice", 3));
        manager.markRequest(1, "fulfilled", inventory);

        assertThrows(RequestException.class, () -> manager.getRequest(1).markAs("prepared"));
        assertThrows(RequestException.class, () -> manager.markRequest(1, "fulfilled", inventory));

        assertEquals(7, inventory.getTotalAvailableQuantity("Wire"));
        assertEquals("fulfilled", manager.getRequest(1).getStatus());
    }

    @Test
    void markRequest_insufficientStock_preservesPreparedStatusAndStock() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 2));
        RequestManager manager = new RequestManager();
        manager.addRequest(createRequest("Alice", 3));
        manager.markRequest(1, "prepared", inventory);

        assertThrows(InsufficientStockException.class, () -> manager.markRequest(1, "fulfilled", inventory));

        assertEquals("prepared", manager.getRequest(1).getStatus());
        assertEquals(2, inventory.getTotalAvailableQuantity("Wire"));
    }

    @Test
    void listRequests_completedAndOutstanding_onlyOutstandingShowsShortage() throws CommandException {
        Inventory inventory = new Inventory();
        inventory.addItem(new InventoryItem("Wire", "Components", 3));
        RequestManager manager = new RequestManager();
        manager.addRequest(createRequest("Alice", 3));
        manager.addRequest(createRequest("Bob", 2));
        manager.markRequest(1, "fulfilled", inventory);

        String listing = captureListing(manager, inventory);
        String completedLine = listing.lines().filter(line -> line.contains("[Fulfilled]")).findFirst().orElseThrow();
        String pendingLine = listing.lines().filter(line -> line.contains("[Pending]")).findFirst().orElseThrow();

        assertTrue(completedLine.contains("(Available: 0)"));
        assertFalse(completedLine.contains("Insufficient stock"));
        assertTrue(pendingLine.contains("!! Insufficient stock (Shortfall: 2)"));
        assertEquals(0, inventory.getTotalAvailableQuantity("Wire"));

        manager.markRequest(2, "prepared", inventory);
        String preparedListing = captureListing(manager, inventory);
        assertTrue(preparedListing.contains("[Prepared]"));
        assertTrue(preparedListing.contains("!! Insufficient stock (Shortfall: 2)"));
        assertEquals(0, inventory.getTotalAvailableQuantity("Wire"));
    }

    /**
     * Creates requests with matching deadlines so alphabetical student order determines their indices.
     */
    private static Request createRequest(String student, int quantity) throws CommandException {
        String deadline = LocalDate.now().plusDays(7)
                .format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH));
        return new Request(student, "Wire", quantity, deadline);
    }

    /**
     * Captures only listing output and always restores the process-wide output stream.
     */
    private static String captureListing(RequestManager manager, Inventory inventory) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            manager.listRequests(inventory);
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}

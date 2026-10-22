package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.CommandFormatException;
import seedu.bob.exception.InvalidRequestIndexException;

/**
 * Verifies deadline ordering, status changes, and availability display in {@link RequestManager}.
 *
 * @author Lokwx
 */
class RequestManagerTest {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private PrintStream originalOutput;
    private PrintStream capturedOutput;

    @BeforeEach
    void setUp() {
        originalOutput = System.out;
        capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8);
        System.setOut(capturedOutput);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOutput);
        capturedOutput.close();
    }

    @Test
    void addRequest_nullRequest_exceptionThrown() {
        RequestManager manager = new RequestManager();

        assertThrows(NullPointerException.class, () -> manager.addRequest(null));
    }

    @Test
    void addRequest_outOfOrder_sortsByDeadlineAndPrintsConfirmation() throws CommandException {
        RequestManager manager = new RequestManager();
        String laterDeadline = date(5);
        String earlierDeadline = date(2);
        Request later = new Request("Bob", "Battery", 2, laterDeadline);
        Request earlier = new Request("Alice", "Wire", 5, earlierDeadline);

        manager.addRequest(later);
        manager.addRequest(earlier);

        assertSame(earlier, manager.getRequest(1));
        assertSame(later, manager.getRequest(2));
        assertEquals("Successfully added request: Bob - 2x Battery by " + laterDeadline + "\n"
                + "Successfully added request: Alice - 5x Wire by " + earlierDeadline + "\n", printed());
    }

    @Test
    void getRequest_invalidIndex_exceptionThrown() {
        RequestManager manager = new RequestManager();

        assertThrows(InvalidRequestIndexException.class, () -> manager.getRequest(0));
        assertThrows(InvalidRequestIndexException.class, () -> manager.getRequest(1));
        assertThrows(InvalidRequestIndexException.class, () -> manager.getRequest(-1));
    }

    @Test
    void deleteRequest_validIndex_removesRequestAndRenumbers() throws CommandException {
        RequestManager manager = new RequestManager();
        String firstDeadline = date(2);
        String secondDeadline = date(3);
        Request first = new Request("Alice", "Wire", 5, firstDeadline);
        Request second = new Request("Bob", "Battery", 2, secondDeadline);
        manager.addRequest(first);
        manager.addRequest(second);
        output.reset();

        manager.deleteRequest(1);

        assertEquals("Successfully removed request: Alice - 5x Wire by " + firstDeadline + "\n", printed());
        assertSame(second, manager.getRequest(1));
        assertThrows(InvalidRequestIndexException.class, () -> manager.getRequest(2));
    }

    @Test
    void deleteRequest_invalidIndex_listUnchanged() throws CommandException {
        RequestManager manager = new RequestManager();
        Request only = new Request("Alice", "Wire", 5, date(2));
        manager.addRequest(only);

        assertThrows(InvalidRequestIndexException.class, () -> manager.deleteRequest(2));
        assertSame(only, manager.getRequest(1));
    }

    @Test
    void markRequest_preparedAndFulfilled_statusUpdated() throws CommandException {
        RequestManager manager = new RequestManager();
        String deadline = date(2);
        Request request = new Request("Alice", "Wire", 5, deadline);
        manager.addRequest(request);
        output.reset();

        manager.markRequest(1, "Prepared");

        assertEquals("prepared", request.getStatus());
        assertEquals("Successfully marked request 1 as prepared: Alice - 5x Wire by "
                + deadline + "\n", printed());

        manager.markRequest(1, "fulfilled");
        assertEquals("fulfilled", request.getStatus());
    }

    @Test
    void markRequest_invalidStatus_statusUnchanged() throws CommandException {
        RequestManager manager = new RequestManager();
        Request request = new Request("Alice", "Wire", 5, date(2));
        manager.addRequest(request);

        assertThrows(CommandFormatException.class, () -> manager.markRequest(1, "pending"));
        assertEquals("pending", request.getStatus());
    }

    @Test
    void listRequests_emptyList_printsPlaceholder() {
        new RequestManager().listRequests(new Inventory());

        assertEquals(Ui.DIVIDER + "\nStudent requests\n" + Ui.DIVIDER + "\n"
                + "No requests recorded.\n" + Ui.DIVIDER + "\n", printed());
    }

    @Test
    void listRequests_populatedList_printsDeadlineOrderStatusAndAvailability() throws CommandException {
        RequestManager manager = new RequestManager();
        Inventory inventory = new Inventory();
        String earlierDeadline = date(2);
        String laterDeadline = date(5);
        inventory.addItem(new InventoryItem("Wire", "Components", 15));
        inventory.addItem(new InventoryItem("Battery", "Components", 1));
        manager.addRequest(new Request("Bob", "Battery", 5, laterDeadline));
        manager.addRequest(new Request("Alice", "Wire", 20, earlierDeadline));
        manager.markRequest(1, "prepared");
        output.reset();

        manager.listRequests(inventory);

        assertEquals(Ui.DIVIDER + "\nStudent requests\n" + Ui.DIVIDER + "\n"
                + "1. [Prepared] Alice - 20x Wire by " + earlierDeadline
                + " (Available: 15) !! Insufficient stock (Shortfall: 5)\n"
                + "2. [Pending] Bob - 5x Battery by " + laterDeadline
                + " (Available: 1) !! Insufficient stock (Shortfall: 4)\n"
                + Ui.DIVIDER + "\n", printed());
    }

    /**
     * Returns a date the given number of days from today in the documented format.
     */
    private static String date(int daysFromToday) {
        return LocalDate.now().plusDays(daysFromToday)
                .format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH));
    }

    /**
     * Returns the console output captured so far, with Windows line endings normalized.
     */
    private String printed() {
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}

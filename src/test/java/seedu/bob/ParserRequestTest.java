package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Exercises student request commands through the same parser used by the console application.
 */
class ParserRequestTest {

    private static final String ADD_USAGE =
            "Invalid format. Use: add-r n/STUDENT_NAME i/ITEM q/QUANTITY d/DEADLINE\n";
    private static final String DELETE_USAGE = "Invalid format. Use: delete-r INDEX\n";
    private static final String LIST_USAGE = "Invalid format. Use: list-r\n";
    private static final String MARK_USAGE = "Invalid format. Use: mark-r INDEX STATUS\n";
    private static final String LIST_UPDATED =
            "Request list updated. Run list-r to see the latest order.\n";

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final Parser parser = new Parser();
    private final Inventory inventory = new Inventory();
    private final SessionManager sessions = new SessionManager();
    private final ContactManager contacts = new ContactManager();
    private final RequestManager requests = new RequestManager();
    private final LoanManager loans = new LoanManager(inventory);
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
    void addRequest_validRequest_confirmationShown() {
        String deadline = date(2);

        assertEquals("Successfully added request: Alice - 20x Wire by " + deadline + "\n"
                + LIST_UPDATED, execute("add-r n/Alice i/Wire q/20 d/" + deadline));
    }

    @Test
    void addRequest_todayDeadline_accepted() {
        String deadline = date(0);

        assertEquals("Successfully added request: Alice - 1x Wire by " + deadline + "\n"
                + LIST_UPDATED, execute("add-r n/Alice i/Wire q/1 d/" + deadline));
    }

    @Test
    void addRequest_multiWordValues_accepted() {
        String deadline = date(2);

        assertEquals("Successfully added request: Alice Tan - 10x Red LED by " + deadline + "\n"
                + LIST_UPDATED, execute("add-r n/Alice Tan i/Red LED q/10 d/" + deadline));
    }

    @Test
    void addRequest_surroundingWhitespace_accepted() {
        String deadline = date(2);

        assertEquals("Successfully added request: Alice - 2x Wire by " + deadline + "\n"
                + LIST_UPDATED, execute("add-r   n/Alice   i/Wire   q/2   d/" + deadline));
    }

    @Test
    void addRequest_malformedArguments_usageShown() {
        String deadline = date(2);
        String[] commands = {
            "add-r",
            "add-r n/Alice i/Wire q/2",
            "add-r n/Alice i/Wire d/" + deadline,
            "add-r n/Alice q/2 d/" + deadline,
            "add-r i/Wire q/2 d/" + deadline,
            "add-r i/Wire n/Alice q/2 d/" + deadline,
            "add-r n/Alice i/Wire q/2 d/" + deadline + " x/extra",
            "add-r n/Alice i/Wire q/2 d/" + deadline + " d/" + deadline,
            "add-r n/Alice n/Bob i/Wire q/2 d/" + deadline,
            "add-r extra n/Alice i/Wire q/2 d/" + deadline
        };
        for (String command : commands) {
            assertEquals(ADD_USAGE, execute(command), command);
        }
    }

    @Test
    void addRequest_invalidValues_errorShown() {
        String deadline = date(2);

        assertEquals("Student name cannot be blank.\n",
                execute("add-r n/ i/Wire q/2 d/" + deadline));
        assertEquals("Item name cannot be blank.\n",
                execute("add-r n/Alice i/ q/2 d/" + deadline));
        assertEquals("Quantity must be greater than zero.\n",
                execute("add-r n/Alice i/Wire q/0 d/" + deadline));
        assertEquals("Quantity must be greater than zero.\n",
                execute("add-r n/Alice i/Wire q/-5 d/" + deadline));
        assertEquals("Quantity must be a positive integer.\n",
                execute("add-r n/Alice i/Wire q/ d/" + deadline));
        assertEquals("Quantity must be a positive integer.\n",
                execute("add-r n/Alice i/Wire q/abc d/" + deadline));
        assertEquals("Quantity must be a positive integer.\n",
                execute("add-r n/Alice i/Wire q/2147483648 d/" + deadline));
        assertEquals("Deadline must be a valid date in d MMMM yyyy format.\n",
                execute("add-r n/Alice i/Wire q/2 d/"));
        assertEquals("Deadline must be a valid date in d MMMM yyyy format.\n",
                execute("add-r n/Alice i/Wire q/2 d/31 February 2099"));
        assertEquals("Deadline must be a valid date in d MMMM yyyy format.\n",
                execute("add-r n/Alice i/Wire q/2 d/2 october 2050"));
        assertEquals("Request deadline cannot be before today.\n",
                execute("add-r n/Alice i/Wire q/2 d/1 January 2000"));
        assertEquals(requestList("No requests recorded.\n"), execute("list-r"));
    }

    @Test
    void listRequests_emptyAndPopulated_sortedByDeadlineWithAvailability() {
        assertEquals(requestList("No requests recorded.\n"), execute("list-r"));

        String laterDeadline = date(5);
        String earlierDeadline = date(2);
        execute("add-r n/Bob i/Battery q/5 d/" + laterDeadline);
        execute("add-r n/Alice i/Wire q/20 d/" + earlierDeadline);
        execute("add-i n/Wire c/Components q/15");

        assertEquals(requestList(
                "1. [Pending] Alice - 20x Wire by " + earlierDeadline
                        + " (Available: 15) !! Insufficient stock (Shortfall: 5)\n"
                        + "2. [Pending] Bob - 5x Battery by " + laterDeadline
                        + " (Available: 0) !! Insufficient stock (Shortfall: 5)\n"),
                execute("list-r"));
    }

    @Test
    void listRequests_equalDeadlines_sortedByStudentName() {
        String deadline = date(2);
        execute("add-r n/Charlie i/Wire q/1 d/" + deadline);
        execute("add-r n/Alice i/Battery q/1 d/" + deadline);
        execute("add-r n/Bob i/Red LED q/1 d/" + deadline);

        String response = execute("list-r");

        assertTrue(response.indexOf("Alice") < response.indexOf("Bob"));
        assertTrue(response.indexOf("Bob") < response.indexOf("Charlie"));
    }

    @Test
    void listRequests_afterStockChange_recalculatesAvailability() {
        String deadline = date(2);
        execute("add-i n/Wire c/Components q/20");
        execute("add-r n/Alice i/Wire q/5 d/" + deadline);

        assertTrue(execute("list-r").contains("(Available: 20)"));

        execute("delete-i c/Components i/1 q/18");

        assertTrue(execute("list-r").contains("(Available: 2) !! Insufficient stock (Shortfall: 3)"));
    }

    @Test
    void listRequests_sameItemAcrossCategories_totalsAvailability() {
        String deadline = date(2);
        execute("add-i n/Wire c/Components q/10");
        execute("add-i n/Wire c/Spares q/5");
        execute("add-r n/Alice i/Wire q/15 d/" + deadline);

        assertTrue(execute("list-r").contains("(Available: 15)"));
    }

    @Test
    void listRequests_itemNameCaseDiffers_stillFindsStock() {
        String deadline = date(2);
        execute("add-i n/Wire c/Components q/20");
        execute("add-r n/Alice i/wire q/5 d/" + deadline);

        assertTrue(execute("list-r").contains("(Available: 20)"));
    }

    @Test
    void listRequests_withArguments_usageShown() {
        assertEquals(LIST_USAGE, execute("list-r extra"));
    }

    @Test
    void deleteRequest_validIndex_removesAndRenumbers() {
        String deadline = date(2);
        String laterDeadline = date(3);
        execute("add-r n/Alice i/Wire q/5 d/" + deadline);
        execute("add-r n/Bob i/Battery q/2 d/" + laterDeadline);

        assertEquals("Successfully removed request: Alice - 5x Wire by " + deadline + "\n"
                + LIST_UPDATED, execute("delete-r 1"));
        assertEquals(requestList("1. [Pending] Bob - 2x Battery by " + laterDeadline
                + " (Available: 0) !! Insufficient stock (Shortfall: 2)\n"),
                execute("list-r"));
    }

    @Test
    void deleteRequest_invalidRequest_noStateChange() {
        execute("add-r n/Alice i/Wire q/5 d/" + date(2));

        assertEquals(DELETE_USAGE, execute("delete-r"));
        assertEquals(DELETE_USAGE, execute("delete-r 1 2"));
        assertEquals("Request index must be a positive integer.\n", execute("delete-r abc"));
        assertEquals("Request index must be a positive integer.\n", execute("delete-r 0"));
        assertEquals("Request index must be a positive integer.\n", execute("delete-r -1"));
        assertEquals("Request index must be a positive integer.\n", execute("delete-r 2147483648"));
        assertEquals("Request index 2 is invalid. There are 1 recorded request(s).\n",
                execute("delete-r 2"));
        assertTrue(execute("list-r").contains("Alice"));
    }

    @Test
    void markRequest_preparedThenFulfilled_statusUpdated() {
        String deadline = date(2);
        execute("add-r n/Alice i/Wire q/5 d/" + deadline);

        assertEquals("Successfully marked request 1 as prepared: Alice - 5x Wire by " + deadline + "\n",
                execute("mark-r 1 prepared"));
        assertTrue(execute("list-r").contains("1. [Prepared] Alice"));

        assertEquals("Successfully marked request 1 as fulfilled: Alice - 5x Wire by " + deadline + "\n",
                execute("mark-r 1 Fulfilled"));
        assertTrue(execute("list-r").contains("1. [Fulfilled] Alice"));
    }

    @Test
    void markRequest_statusUppercase_statusUpdated() {
        String deadline = date(2);
        execute("add-r n/Alice i/Wire q/5 d/" + deadline);

        assertEquals("Successfully marked request 1 as prepared: Alice - 5x Wire by " + deadline + "\n",
                execute("mark-r 1 PREPARED"));
        assertTrue(execute("list-r").contains("1. [Prepared] Alice"));
    }

    @Test
    void markRequest_statusSurroundingWhitespace_accepted() {
        String deadline = date(2);
        execute("add-r n/Alice i/Wire q/5 d/" + deadline);

        assertEquals("Successfully marked request 1 as prepared: Alice - 5x Wire by " + deadline + "\n",
                execute("mark-r   1   prepared   "));
    }

    @Test
    void markRequest_pendingDirectlyToFulfilled_allowed() {
        String deadline = date(2);
        execute("add-r n/Alice i/Wire q/5 d/" + deadline);

        assertEquals("Successfully marked request 1 as fulfilled: Alice - 5x Wire by " + deadline + "\n",
                execute("mark-r 1 fulfilled"));
        assertTrue(execute("list-r").contains("1. [Fulfilled] Alice"));
    }

    @Test
    void markRequest_invalidRequest_noStateChange() {
        execute("add-r n/Alice i/Wire q/5 d/" + date(2));

        assertEquals(MARK_USAGE, execute("mark-r"));
        assertEquals(MARK_USAGE, execute("mark-r 1"));
        assertEquals(MARK_USAGE, execute("mark-r 1   "));
        assertEquals(MARK_USAGE, execute("mark-r 1 prepared extra"));
        assertEquals("Request index must be a positive integer.\n", execute("mark-r abc prepared"));
        assertEquals("Request index must be a positive integer.\n", execute("mark-r 0 prepared"));
        assertEquals("Request index 2 is invalid. There are 1 recorded request(s).\n",
                execute("mark-r 2 prepared"));
        assertEquals("Status must be \"prepared\" or \"fulfilled\".\n",
                execute("mark-r 1 pending"));
        assertEquals("Status must be \"prepared\" or \"fulfilled\".\n",
                execute("mark-r 1 done"));
        assertTrue(execute("list-r").contains("1. [Pending] Alice"));
    }

    @Test
    void requestCommands_nullOrBlankInput_invalidCommand() {
        assertEquals("Invalid command\n", execute(null));
        assertEquals("Invalid command\n", execute("   "));
    }

    @Test
    void requestCommands_wrongCommandWord_treatedAsUnknown() {
        assertEquals("Invalid command\n", execute("ADD-R n/Alice i/Wire q/1 d/" + date(2)));
        assertEquals("Invalid command\n", execute("add-request n/Alice i/Wire q/1 d/" + date(2)));
    }

    /**
     * Returns a date the given number of days from today in the documented format.
     */
    private static String date(int daysFromToday) {
        return LocalDate.now().plusDays(daysFromToday)
                .format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH));
    }

    /**
     * Builds the expected output of the request list display around the given rows.
     */
    private static String requestList(String rows) {
        return Ui.DIVIDER + "\nStudent requests\n" + Ui.DIVIDER + "\n" + rows + Ui.DIVIDER + "\n";
    }

    /**
     * Executes one command and returns only that command's normalized console output.
     */
    private String execute(String command) {
        output.reset();
        parser.handleCommand(command, inventory, sessions, contacts, requests, loans);
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}

//@@author gavinlimsh
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;

/**
 * Checks command syntax, exact feature output, shared-stock displays, and persistent startup wiring.
 */
class ParserBrokenItemTest {

    private static final String DIVIDER = "--------------------------------------------------------------------\n";
    private static final String ADD_COMMAND =
            "add-b n/Meter c/Equipment q/2 f/Cracked screen d/1 January 2020";
    private static final String REPORT = "2x Meter [Equipment], fault: Cracked screen, reported 1 January 2020";
    private static final String ADD_USAGE = "Invalid format. Use: add-b n/NAME c/CATEGORY q/QUANTITY"
            + " f/FAULT d/DATE_REPORTED [m/CONTACT_INDEX]\n";

    private final Inventory inventory = new Inventory();
    private final SessionManager sessions = new SessionManager();
    private final ContactManager contacts = new ContactManager();
    private final RequestManager requests = new RequestManager();
    private final LoanManager loans = new LoanManager(inventory);
    private final BrokenItemManager broken = new BrokenItemManager(inventory, contacts);
    private final Parser parser = new Parser();
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private PrintStream originalOutput;
    private PrintStream capturedOutput;

    @BeforeEach
    void setUp() throws CommandException {
        originalOutput = System.out;
        capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8);
        System.setOut(capturedOutput);
        inventory.addItem(new InventoryItem("Meter", "Equipment", 10));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOutput);
        capturedOutput.close();
    }

    @Test
    void addBrokenItem_validContact_deductsStockAndDisplaysSelectedContact() throws CommandException {
        Contact contact = new Contact("Lab Maintenance", "00123456", null, null);
        contacts.addContact(contact);
        assertEquals("Successfully recorded broken item: " + REPORT + ". Available inventory reduced.\n",
                execute(ADD_COMMAND + " m/1"));
        assertEquals(8, stock());
        assertSame(contact, broken.getBrokenItems().get(0).getContact());
        assertEquals(listing("1. " + REPORT + "\n   Contact: Lab Maintenance (00123456)\n"),
                execute("list-b"));
    }

    @Test
    void listBrokenItems_emptyAndUnassigned_displaysExactMessagesWithoutChangingStock() {
        assertEquals(listing("No broken items recorded.\n"), execute("list-b"));
        execute(ADD_COMMAND);
        String expected = listing("1. " + REPORT + "\n   Contact: Not assigned\n");
        assertEquals(expected, execute("list-b"));
        assertEquals(expected, execute("list-b"));
        assertEquals(8, stock());
        assertEquals(1, broken.getBrokenItems().size());
    }

    @Test
    void listBrokenItems_deletedContact_displaysUnavailableAndAllowsRepair() {
        execute("add-c n/Maintenance t/12345678");
        execute(ADD_COMMAND + " m/1");
        execute("delete-c 1");
        execute("add-c n/Replacement t/87654321");
        assertEquals(listing("1. " + REPORT + "\n   Contact: Unavailable (contact deleted)\n"),
                execute("list-b"));
        assertEquals("Successfully deleted broken-item report: " + REPORT
                + ". All units restored to inventory.\n", execute("delete-b 1"));
        assertEquals(10, stock());
        assertTrue(broken.getBrokenItems().isEmpty());
    }

    @Test
    void addBrokenItem_malformedPrefixes_rejectsWithoutMutation() {
        List<String> commands = List.of(
                "add-b", "add-b stray n/Meter c/Equipment q/2 f/Fault d/1 January 2020",
                ADD_COMMAND.replace("q/2 ", ""), ADD_COMMAND + " q/2", ADD_COMMAND + " x/extra",
                ADD_COMMAND.replace("n/Meter c/Equipment", "c/Equipment n/Meter"),
                ADD_COMMAND.replace("n/Meter", "N/Meter"));
        for (String command : commands) {
            assertEquals(ADD_USAGE, execute(command), command);
            assertUnchanged();
        }
    }

    @Test
    void addBrokenItem_invalidValues_rejectsBeforeMutation() {
        List<String> commands = List.of(
                ADD_COMMAND.replace("n/Meter", "n/"), ADD_COMMAND.replace("c/Equipment", "c/"),
                ADD_COMMAND.replace("f/Cracked screen", "f/"), ADD_COMMAND.replace("d/1 January 2020", "d/"),
                ADD_COMMAND.replace("q/2", "q/0"), ADD_COMMAND.replace("q/2", "q/-2"),
                ADD_COMMAND.replace("q/2", "q/eleven"), ADD_COMMAND.replace("q/2", "q/999999999999"),
                ADD_COMMAND.replace("q/2", "q/11"), ADD_COMMAND.replace("n/Meter", "n/Absent"),
                ADD_COMMAND.replace("c/Equipment", "c/Absent"),
                ADD_COMMAND.replace("1 January 2020", "31 February 2020"),
                ADD_COMMAND.replace("1 January 2020", "1 January 9999"));
        for (String command : commands) {
            assertExpectedRejection(execute(command));
            assertUnchanged();
        }
    }

    @Test
    void addBrokenItem_invalidContact_rejectsWithoutAddingContactsOrDeductingStock() {
        for (String index : new String[]{"", "0", "-1", "1", "no", "999999999999"}) {
            assertExpectedRejection(execute(ADD_COMMAND + " m/" + index));
            assertUnchanged();
            assertTrue(execute("list-c").contains("No contacts saved."));
        }
    }

    @Test
    void deleteBrokenItem_invalidSyntaxOrPosition_preservesExistingReport() {
        execute(ADD_COMMAND);
        BrokenItem item = broken.getBrokenItems().get(0);
        for (String arguments : new String[]{"", "0", "-1", "one", "1 2", "999999999999"}) {
            assertEquals("Invalid format. Use: delete-b INDEX\n", execute("delete-b " + arguments));
            assertEquals(8, stock());
            assertSame(item, broken.getBrokenItems().get(0));
        }
        assertExpectedRejection(execute("delete-b 2"));
        assertEquals(8, stock());
        assertSame(item, broken.getBrokenItems().get(0));
    }

    @Test
    void listBrokenItems_extraArguments_rejectsWithoutMutation() {
        assertEquals("Invalid format. Use: list-b\n", execute("list-b extra"));
        assertUnchanged();
    }

    @Test
    void sharedInventory_requestsAndPreparation_refreshShortagesAfterRepair() throws CommandException {
        inventory.addItem(new InventoryItem("Meter", "Tools", 1));
        requests.addRequest(new Request("Student", "Meter", 10, "30 October 2099"));
        Session session = new Session("Lab", "30 October 2099", "Room", "0900", "1000", 10);
        session.getPreparation().addItem("Meter", "Equipment", 1, 10);
        sessions.addSession(session);
        execute(ADD_COMMAND);
        String requestListing = execute("list-r");
        assertTrue(requestListing.contains("Available: 9"));
        assertTrue(requestListing.contains("Shortfall: 1"));
        String preparationListing = execute("list-p s/1");
        assertTrue(preparationListing.contains("Required: 10, Available: 8"));
        assertTrue(preparationListing.contains("Shortfall: 2"));
        execute("delete-b 1");
        assertFalse(execute("list-r").contains("Shortfall:"));
        assertFalse(execute("list-p s/1").contains("Shortfall:"));
        assertEquals(10, stock());
    }

    @Test
    void main_multipleCommands_keepsReportsUntilDeleted() {
        InputStream originalInput = System.in;
        String input = "Tester\nadd-i n/Meter c/Equipment q/10\n" + ADD_COMMAND
                + "\nlist-b\ndelete-b 1\nlist-i\nBye\n";
        output.reset();
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            Bob.main(new String[0]);
        } finally {
            System.setIn(originalInput);
        }
        String actual = capturedText();
        assertTrue(actual.contains("1. " + REPORT));
        assertTrue(actual.contains("All units restored to inventory."));
        assertTrue(actual.contains("Meter (Qty: 10)"));
        assertFalse(actual.contains("unexpected error"));
    }

    /** Executes one command through the complete seven-argument dispatch path. */
    private String execute(String command) {
        output.reset();
        parser.handleCommand(command, inventory, sessions, contacts, requests, loans, broken);
        return capturedText();
    }

    /** Normalizes line endings only; expected output remains independent of Ui constants. */
    private String capturedText() {
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    private String listing(String rows) {
        return DIVIDER + "Broken items\n" + DIVIDER + rows + DIVIDER;
    }

    private int stock() {
        return inventory.getAvailableQuantity("Meter", "Equipment");
    }

    /** Checks failure atomicity after a rejected addition to an initially empty report list. */
    private void assertUnchanged() {
        assertEquals(10, stock());
        assertTrue(broken.getBrokenItems().isEmpty());
    }

    /** Distinguishes an expected validation response from success or the runtime-error fallback. */
    private void assertExpectedRejection(String response) {
        assertFalse(response.isBlank());
        assertFalse(response.contains("Successfully"));
        assertFalse(response.contains("unexpected error"));
    }
}

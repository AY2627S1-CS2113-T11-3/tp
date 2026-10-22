package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;

/**
 * Exercises loan command validation and verifies rejected commands cannot deduct stock or add contacts.
 */
class ParserLoanTest {

    private static final String ADD_USAGE =
            "Invalid format. Use: add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE\n";
    private final Inventory inventory = new Inventory();
    private final SessionManager sessions = new SessionManager();
    private final ContactManager contacts = new ContactManager();
    private final LoanManager loans = new LoanManager(inventory);
    private final Parser parser = new Parser();
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private PrintStream originalOutput;
    private PrintStream capturedOutput;

    @BeforeEach
    void setUp() throws CommandException {
        originalOutput = System.out;
        capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8);
        System.setOut(capturedOutput);
        inventory.addItem(new InventoryItem("Red LED", "Lab Components", 10));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOutput);
        capturedOutput.close();
    }

    @Test
    void addLoan_validCommand_preservesSpacesAndKeepsBorrowersSeparate() {
        assertEquals("Successfully added loan: 2x Red LED [Lab Components] to Alex Tan (00123456),"
                + " due 30 October 2099\n", execute(
                        "add-l n/Red LED c/Lab Components q/2 b/Alex Tan t/00123456 d/30 October 2099"));
        assertEquals(8, inventory.getAvailableQuantity("Red LED", "Lab Components"));
        assertEquals("00123456", loans.getLoans().get(0).getBorrower().getPhone());
        assertTrue(execute("list-c").contains("No contacts saved."));
    }

    @Test
    void addLoan_malformedPrefixes_rejectsWithoutMutation() {
        String[] commands = {
            "add-l",
            "add-l n/Red LED c/Lab Components b/Alex t/123 d/30 October 2099",
            "add-l c/Lab Components n/Red LED q/1 b/Alex t/123 d/30 October 2099",
            "add-l extra n/Red LED c/Lab Components q/1 b/Alex t/123 d/30 October 2099",
            "add-l n/Red LED c/Lab Components q/1 b/Alex t/123 d/30 October 2099 q/1"
        };
        for (String command : commands) {
            assertEquals(ADD_USAGE, execute(command), command);
        }
        assertTrue(loans.getLoans().isEmpty());
        assertEquals(10, inventory.getAvailableQuantity("Red LED", "Lab Components"));
    }

    @Test
    void addLoan_invalidBorrowerDateOrQuantity_rejectsBeforeDeduction() {
        assertEquals("Phone must contain digits only.\n", execute(
                "add-l n/Red LED c/Lab Components q/2 b/Alex t/+123 d/30 October 2099"));
        assertEquals("Contact name cannot be blank.\n", execute(
                "add-l n/Red LED c/Lab Components q/2 b/ t/123 d/30 October 2099"));
        assertEquals("Due date must be a valid date in d MMMM yyyy format (e.g., 30 October 2099).\n",
                execute("add-l n/Red LED c/Lab Components q/2 b/Alex t/123 d/31 February 2099"));
        assertEquals("Quantity must be a positive integer.\n", execute(
                "add-l n/Red LED c/Lab Components q/2147483648 b/Alex t/123 d/30 October 2099"));
        assertEquals(10, inventory.getAvailableQuantity("Red LED", "Lab Components"));
        assertTrue(loans.getLoans().isEmpty());
    }

    @Test
    void listLoans_argumentsOrUnknownCommand_rejectsWithoutMutation() {
        assertEquals("Invalid format. Use: list-l\n", execute("list-l overdue"));
        assertEquals("Invalid command\n", execute("LIST-L"));
        assertEquals(Ui.DIVIDER + "\nLoans\n" + Ui.DIVIDER + "\nNo active loans.\n" + Ui.DIVIDER + "\n",
                execute("list-l"));
    }

    /**
     * Executes one command and captures only its stdout, with normalized newlines.
     */
    private String execute(String command) {
        output.reset();
        parser.handleCommand(command, inventory, sessions, contacts, loans);
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}

package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

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
    private final LoanManager loans = new LoanManager(inventory,
            Clock.fixed(Instant.parse("2026-10-22T04:00:00Z"), ZoneId.of("Asia/Singapore")));
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
        assertEquals("00123456", loans.getLoans().get(0).getBorrowerPhone());
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
        assertEquals("Invalid format. Use: list-l [overdue]\n", execute("list-l extra"));
        assertEquals("Invalid command\n", execute("LIST-L"));
        assertEquals(Ui.DIVIDER + "\nLoans\n" + Ui.DIVIDER + "\nNo active loans.\n" + Ui.DIVIDER + "\n",
                execute("list-l"));
    }

    @Test
    void deleteLoan_validCommand_restoresStockAndPreservesSupplierContacts() {
        execute("add-c n/Alex t/123 r/Supplier");
        execute("add-l n/Red LED c/Lab Components q/2 b/Alex t/123 d/30 October 2099");

        assertEquals("Successfully deleted incorrect loan: 2x Red LED [Lab Components] to Alex (123),"
                + " due 30 October 2099. Inventory restored.\n", execute("delete-l 1"));
        assertEquals(10, inventory.getAvailableQuantity("Red LED", "Lab Components"));
        assertTrue(loans.getLoans().isEmpty());
        assertTrue(execute("list-c").contains("1. Alex (123) - Supplier"));
    }

    @Test
    void deleteLoan_invalidSyntaxAndIndices_doesNotRestoreStock() {
        execute("add-l n/Red LED c/Lab Components q/2 b/Alex t/123 d/30 October 2099");
        for (String command : new String[] {"delete-l", "delete-l 1 extra", "delete-l 1 q/1"}) {
            assertEquals("Invalid format. Use: delete-l INDEX\n", execute(command));
        }
        for (String index : new String[] {"0", "-1", "abc", "1.5", "2147483648", "i/1"}) {
            assertEquals("Loan index must be a positive integer.\n", execute("delete-l " + index));
        }
        assertEquals("Loan index 2 is invalid. There are 1 active loan(s).\n", execute("delete-l 2"));
        assertEquals(8, inventory.getAvailableQuantity("Red LED", "Lab Components"));
        assertEquals(1, loans.getLoans().size());
    }

    @Test
    void returnLoan_validCommand_restoresFullStockAndKeepsSupplierContacts() {
        execute("add-c n/Alex t/123 r/Supplier");
        execute("add-l n/Red LED c/Lab Components q/2 b/Alex t/123 d/30 October 2099");

        assertEquals("Successfully returned loan: 2x Red LED [Lab Components] to Alex (123),"
                + " due 30 October 2099. Inventory restored.\n", execute("return 1"));
        assertEquals(10, inventory.getAvailableQuantity("Red LED", "Lab Components"));
        assertTrue(loans.getLoans().isEmpty());
        assertTrue(execute("list-c").contains("1. Alex (123) - Supplier"));
        assertEquals("Loan index 1 is invalid. There are 0 active loan(s).\n", execute("return 1"));
        assertEquals(10, inventory.getAvailableQuantity("Red LED", "Lab Components"));
    }

    @Test
    void returnLoan_invalidOrPartialRequest_preservesEntireLoan() {
        execute("add-l n/Red LED c/Lab Components q/2 b/Alex t/123 d/30 October 2099");
        for (String command : new String[] {"return", "return 1 extra", "return 1 q/1", "return 1 2"}) {
            assertEquals("Invalid format. Use: return INDEX\n", execute(command));
        }
        for (String index : new String[] {"0", "-1", "abc", "1.5", "2147483648", "i/1"}) {
            assertEquals("Loan index must be a positive integer.\n", execute("return " + index));
        }
        assertEquals("Loan index 2 is invalid. There are 1 active loan(s).\n", execute("return 2"));
        assertEquals("Invalid command\n", execute("RETURN 1"));
        assertEquals(8, inventory.getAvailableQuantity("Red LED", "Lab Components"));
        assertEquals(2, loans.getLoans().get(0).getQuantity());
    }

    @Test
    void listOverdueLoans_backlogAndCurrentLoans_warnsAndPreservesFullIndices() {
        execute("add-l n/Red LED c/Lab Components q/1 b/Current t/123 d/22 October 2026");
        assertEquals("Successfully added loan: 1x Red LED [Lab Components] to Backlog (123),"
                + " due 21 October 2026\nWarning: this loan is already overdue.\n", execute(
                        "add-l n/Red LED c/Lab Components q/1 b/Backlog t/123 d/21 October 2026"));
        assertEquals(Ui.DIVIDER + "\nOverdue loans\n" + Ui.DIVIDER
                + "\n2. 1x Red LED [Lab Components] to Backlog (123), due 21 October 2026 [OVERDUE]\n"
                + Ui.DIVIDER + "\n", execute("list-l overdue"));
        execute("return 2");
        assertEquals(Ui.DIVIDER + "\nOverdue loans\n" + Ui.DIVIDER + "\nNo overdue loans.\n"
                + Ui.DIVIDER + "\n", execute("list-l overdue"));
        assertEquals("Current", loans.getLoans().get(0).getBorrowerName());
        assertEquals(9, inventory.getAvailableQuantity("Red LED", "Lab Components"));
    }

    @Test
    void listOverdueLoans_unsupportedFilters_rejectsWithoutMutation() {
        for (String command : new String[] {"list-l OVERDUE", "list-l overdue extra", "list-l overdue overdue"}) {
            assertEquals("Invalid format. Use: list-l [overdue]\n", execute(command));
        }
        assertTrue(loans.getLoans().isEmpty());
        assertEquals(10, inventory.getAvailableQuantity("Red LED", "Lab Components"));
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

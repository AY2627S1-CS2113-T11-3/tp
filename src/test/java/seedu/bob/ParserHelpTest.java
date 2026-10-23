package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Exercises the help command through the same parser used by the console application.
 */
class ParserHelpTest {

    /** Defines the expected output independently so changes to the application text are detected. */
    private static final String EXPECTED_HELP = """
            --------------------------------------------------------------------
            Available commands
            --------------------------------------------------------------------
            Inventory
              add-i n/NAME c/CATEGORY [q/QUANTITY]
              delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY
              list-i
            Lab sessions
              add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT
              delete-s INDEX
              list-s
            Lab preparation
              add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]
              delete-p s/SESSION_INDEX i/ITEM_INDEX q/TOTAL_QUANTITY
              list-p s/SESSION_INDEX
            Student requests
              add-r n/STUDENT_NAME i/ITEM q/QUANTITY d/DEADLINE
              delete-r INDEX
              list-r
              mark-r INDEX STATUS (prepared or fulfilled)
            Contacts
              add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]
              delete-c INDEX
              list-c
            Loans
              add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE
              list-l [overdue]
              delete-l INDEX
              return INDEX
            Broken items
              add-b n/NAME c/CATEGORY q/QUANTITY f/FAULT d/DATE_REPORTED [m/CONTACT_INDEX]
              list-b
              delete-b INDEX
                Deletion restores all units: use only for full repairs or incorrect reports.
            Other
              help
              Bye
            --------------------------------------------------------------------
            """;

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final Parser parser = new Parser();
    private final Inventory inventory = new Inventory();
    private final SessionManager sessions = new SessionManager();
    private final ContactManager contacts = new ContactManager();
    private final RequestManager requests = new RequestManager();
    private final LoanManager loans = new LoanManager(inventory);
    private PrintStream originalOutput;
    private PrintStream capturedOutput;

    /**
     * Redirects standard output so each test can inspect command responses.
     */
    @BeforeEach
    void setUp() {
        originalOutput = System.out;
        capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8);
        System.setOut(capturedOutput);
    }

    /**
     * Restores standard output and closes the stream created for the test.
     */
    @AfterEach
    void tearDown() {
        System.setOut(originalOutput);
        capturedOutput.close();
    }

    @Test
    void help_noArguments_listsEveryCommandGroupedByFeature() {
        assertEquals(EXPECTED_HELP, execute("help"));
    }

    @Test
    void help_calledRepeatedly_sameOutputEachTime() {
        assertEquals(EXPECTED_HELP, execute("help"));
        assertEquals(EXPECTED_HELP, execute("help"));
    }

    @Test
    void help_withArguments_usageShownInsteadOfHelp() {
        assertEquals("Invalid format. Use: help\n", execute("help me"));
        assertEquals("Invalid format. Use: help\n", execute("help list-i"));
        assertEquals("Invalid format. Use: help\n", execute("help n/Wire"));
    }

    @Test
    void help_wrongCommandWord_treatedAsUnknown() {
        assertEquals("Invalid command\n", execute("HELP"));
        assertEquals("Invalid command\n", execute("Help"));
        assertEquals("Invalid command\n", execute("helps"));
        assertEquals("Invalid command\n", execute("-help"));
    }

    @Test
    void help_afterOtherCommands_stateUnchanged() {
        execute("add-i n/Wire c/Components q/5");
        String inventoryBefore = execute("list-i");

        execute("help");

        assertEquals(inventoryBefore, execute("list-i"));
    }

    /**
     * Executes one command and returns only that command's normalized console output.
     *
     * @param command Command to execute.
     * @return Command output with Windows line endings converted to newline characters.
     */
    private String execute(String command) {
        output.reset();
        parser.handleCommand(command, inventory, sessions, contacts, requests, loans);
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}

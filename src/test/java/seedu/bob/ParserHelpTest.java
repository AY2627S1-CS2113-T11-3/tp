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

    /** Typed out in full, as written in the user guide, so that a typo in the application is caught. */
    private static final String EXPECTED_HELP =
            "--------------------------------------------------------------------\n"
            + "Available commands\n"
            + "--------------------------------------------------------------------\n"
            + "Inventory\n"
            + "  add-i n/NAME c/CATEGORY [q/QUANTITY]\n"
            + "  delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY\n"
            + "  list-i\n"
            + "Lab sessions\n"
            + "  add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT\n"
            + "  delete-s INDEX\n"
            + "  list-s\n"
            + "Lab preparation\n"
            + "  add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]\n"
            + "  delete-p s/SESSION_INDEX i/ITEM_INDEX q/TOTAL_QUANTITY\n"
            + "  list-p s/SESSION_INDEX\n"
            + "Contacts\n"
            + "  add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]\n"
            + "  delete-c INDEX\n"
            + "  list-c\n"
            + "Loans\n"
            + "  add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE\n"
            + "  list-l [overdue]\n"
            + "  delete-l INDEX\n"
            + "  return INDEX\n"
            + "Other\n"
            + "  help\n"
            + "  Bye\n"
            + "--------------------------------------------------------------------\n";

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final Parser parser = new Parser();
    private final Inventory inventory = new Inventory();
    private final SessionManager sessions = new SessionManager();
    private final ContactManager contacts = new ContactManager();
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
     */
    private String execute(String command) {
        output.reset();
        parser.handleCommand(command, inventory, sessions, contacts, loans);
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}

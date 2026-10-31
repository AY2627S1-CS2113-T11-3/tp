//@@author J-unn-y
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Exercises the find-i command through the same parser used by the console application.
 */
class ParserFindTest {

    private static final String DIVIDER = Ui.DIVIDER + "\n";

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
        execute("add-i n/Multimeter c/Equipment q/5");
        execute("add-i n/Wire c/Components q/20");
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOutput);
        capturedOutput.close();
    }

    @Test
    void findItems_matchingKeyword_listsNumberedResults() {
        execute("add-l n/Multimeter c/Equipment q/2 b/Sam Lee t/91234567 d/31 December 2099");

        assertEquals(DIVIDER
                + "Search results for \"meter\"\n"
                + DIVIDER
                + "1. Multimeter [Equipment] (Available: 3, On loan: 2) - Available\n"
                + DIVIDER, execute("find-i meter"));
    }

    @Test
    void findItems_noMatch_showsEmptyMessage() {
        assertEquals(DIVIDER
                + "Search results for \"battery\"\n"
                + DIVIDER
                + "No items match \"battery\".\n"
                + DIVIDER, execute("find-i battery"));
    }

    @Test
    void findItems_keywordWithSpacesAndPadding_searchesWholeTrimmedText() {
        execute("add-i n/Red LED c/Components q/3");

        String response = execute("find-i    red led   ");

        assertEquals(DIVIDER
                + "Search results for \"red led\"\n"
                + DIVIDER
                + "1. Red LED [Components] (Available: 3, On loan: 0) - Available\n"
                + DIVIDER, response);
    }

    @Test
    void findItems_missingKeyword_showsUsage() {
        assertEquals("Invalid format. Use: find-i KEYWORD\n", execute("find-i"));
        assertEquals("Invalid format. Use: find-i KEYWORD\n", execute("find-i    "));
    }

    @Test
    void findItems_wrongCommandWord_treatedAsUnknown() {
        assertEquals("Invalid command\n", execute("find wire"));
        assertEquals("Invalid command\n", execute("FIND-I wire"));
    }

    @Test
    void findItems_afterSearch_stockUnchanged() {
        execute("find-i wire");

        assertEquals(20, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(5, inventory.getAvailableQuantity("Multimeter", "Equipment"));
    }

    /**
     * Runs one command and returns its console output with Unix line endings.
     */
    private String execute(String command) {
        output.reset();
        parser.handleCommand(command, inventory, sessions, contacts, requests, loans);
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}

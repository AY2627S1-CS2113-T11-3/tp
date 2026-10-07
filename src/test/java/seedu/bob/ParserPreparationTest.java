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

import seedu.bob.exception.CommandException;

/**
 * Exercises preparation commands through the same parser used by the console application.
 */
class ParserPreparationTest {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final Parser parser = new Parser();
    private final Inventory inventory = new Inventory();
    private final SessionManager sessions = new SessionManager();
    private PrintStream originalOutput;
    private PrintStream capturedOutput;

    @BeforeEach
    void setUp() {
        originalOutput = System.out;
        capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8);
        System.setOut(capturedOutput);
        String date = LocalDate.now().plusDays(1)
                .format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH));
        execute("add-s n/First Lab d/" + date + " l/Room-A s/0900 e/1000 p/10");
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOutput);
        capturedOutput.close();
    }

    @Test
    void addPreparation_categorySpecificStock_flagsShortageWithoutConsumingStock() throws CommandException {
        execute("add-i n/Wire c/Components q/3");
        execute("add-i n/Wire c/Spares q/100");

        String response = execute("add-p s/1 i/Wire c/Components");

        assertTrue(response.contains("Successfully added: 10x Wire [Components] to preparation"));
        assertTrue(response.contains("Required: 10, Available: 3) !! Insufficient items (Shortfall: 7)"));
        assertEquals(3, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(100, inventory.getAvailableQuantity("Wire", "Spares"));
        assertEquals(10, sessions.getSession(1).getPreparation().getItems().get(0).getQuantity());
    }

    @Test
    void listPreparation_stockChanges_recalculatesShortage() {
        execute("add-p s/1 i/Red LED c/Components q/2");
        execute("add-i n/Red LED c/Components q/20");

        assertTrue(execute("list-p s/1").contains("1. Red LED [Components] (Required: 20, Available: 20)"));

        execute("delete-i c/Components i/1 q/1");
        assertTrue(execute("list-p s/1").contains("!! Insufficient items (Shortfall: 1)"));
    }

    @Test
    void addPreparation_malformedArguments_noStateChange() throws CommandException {
        String[] commands = {
            "add-p",
            "add-p s/1 i/Wire",
            "add-p i/Wire c/Components s/1",
            "add-p s/1 i/Wire c/Components c/Spares",
            "add-p s/1 i/Wire c/Components x/2",
            "add-p extra s/1 i/Wire c/Components",
            "add-p s/1 i/Wire c/Components q/1 q/2"
        };
        for (String command : commands) {
            assertEquals("Invalid format. Use: add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY"
                    + " [q/QUANTITY_PER_PERSON]\n", execute(command), command);
        }
        assertTrue(sessions.getSession(1).getPreparation().getItems().isEmpty());
    }

    @Test
    void addPreparation_invalidValues_noStateChange() throws CommandException {
        assertEquals("Category cannot be blank.\n", execute("add-p s/1 i/Wire c/"));
        assertEquals("Item name cannot be blank.\n", execute("add-p s/1 i/ c/Components"));
        assertEquals("Quantity must be a positive integer.\n", execute("add-p s/1 i/Wire c/Components q/"));
        assertEquals("Quantity must be a positive integer.\n", execute("add-p s/1 i/Wire c/Components q/abc"));
        assertEquals("Session index must be a positive integer.\n", execute("list-p s/2147483648"));
        assertTrue(sessions.getSession(1).getPreparation().getItems().isEmpty());
    }

    @Test
    void deletePreparation_invalidRequest_noStateChange() throws CommandException {
        execute("add-p s/1 i/Wire c/Components q/2");

        assertEquals("Item index must be a positive integer.\n", execute("delete-p s/1 i/abc q/1"));
        assertEquals("Quantity must be a positive integer.\n", execute("delete-p s/1 i/1 q/0"));
        assertEquals("Quantity must be a positive integer.\n",
                execute("delete-p s/1 i/1 q/1 extra"));
        assertEquals(20, sessions.getSession(1).getPreparation().getItems().get(0).getQuantity());
    }

    @Test
    void deletePreparation_partialAndFullRemoval_preservesInventory() throws CommandException {
        execute("add-i n/Wire c/Components q/20");
        execute("add-p s/1 i/Wire c/Components q/2");

        assertTrue(execute("delete-p s/1 i/1 q/3").contains("Required: 17, Available: 20"));
        assertTrue(execute("delete-p s/1 i/1 q/17").contains("No items in preparation."));
        assertTrue(sessions.getSession(1).getPreparation().getItems().isEmpty());
        assertEquals(20, inventory.getAvailableQuantity("Wire", "Components"));
    }

    /**
     * Executes one command and returns only that command's normalized console output.
     */
    private String execute(String command) {
        output.reset();
        parser.handleCommand(command, inventory, sessions);
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}

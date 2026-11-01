//@@author Lokwx
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Exercises the restock-i command through the same parser and application state used by the console
 * application, including the change report that triggers automatic saving.
 */
class ParserRestockTest {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final Parser parser = new Parser();
    private final AppState state = new AppState();
    private PrintStream originalOutput;
    private PrintStream capturedOutput;

    @BeforeEach
    void setUp() {
        originalOutput = System.out;
        capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8);
        System.setOut(capturedOutput);
        execute("add-i n/Wire c/Components q/10");
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOutput);
        capturedOutput.close();
    }

    @Test
    void restockItem_existingItem_printsQuantityAndReportsPersistedChange() {
        output.reset();

        assertTrue(parser.handleCommand("restock-i n/Wire c/Components q/5", state));
        assertEquals("Item \"Wire\" found in inventory.\n"
                + "Successfully added: 5x Wire to the inventory (Available: 15)\n",
                printedOutput());
        assertEquals(15, state.getInventory().getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void restockItem_caseVaries_keepsStoredSpellingInConfirmation() {
        assertEquals("Item \"Wire\" found in inventory.\n"
                + "Successfully added: 2x Wire to the inventory (Available: 12)\n",
                execute("restock-i n/wire c/COMPONENTS q/2"));
    }

    @Test
    void restockItem_missingItem_reportsAddCommandAndChangesNothing() {
        output.reset();

        assertFalse(parser.handleCommand("restock-i n/Cable c/Components q/5", state));
        assertEquals("Item \"Cable\" does not exist in category \"Components\". "
                + "Use add-i to add a new item.\n", printedOutput());
        assertEquals(10, state.getInventory().getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void restockItem_missingCategory_reportsUnknownCategory() {
        assertEquals("Category \"Tools\" does not exist.\n",
                execute("restock-i n/Wire c/Tools q/5"));
    }

    @Test
    void restockItem_overflowingQuantity_preservesStockAndReportsLimit() {
        execute("add-i n/Battery c/Components q/2147483647");
        output.reset();

        assertFalse(parser.handleCommand("restock-i n/Battery c/Components q/1", state));
        assertEquals("Cannot add 1 Battery. Inventory quantity must not exceed 2147483647.\n",
                printedOutput());
        assertEquals(2147483647, state.getInventory().getAvailableQuantity("Battery", "Components"));
    }

    @Test
    void restockItem_malformedArguments_showsUsage() {
        String usage = "Invalid format. Use: restock-i n/NAME c/CATEGORY q/QUANTITY\n";

        assertEquals(usage, execute("restock-i"));
        assertEquals(usage, execute("restock-i n/Wire c/Components"));
        assertEquals(usage, execute("restock-i c/Components n/Wire q/5"));
        assertEquals(usage, execute("restock-i n/Wire c/Components q/5 x/extra"));
    }

    @Test
    void restockItem_invalidQuantity_showsQuantityMessage() {
        assertEquals("Quantity must be greater than zero.\n",
                execute("restock-i n/Wire c/Components q/0"));
        assertEquals("Quantity must be a positive integer.\n",
                execute("restock-i n/Wire c/Components q/abc"));
    }

    @Test
    void restockItem_blankNameOrCategory_showsBlankFieldMessage() {
        assertEquals("Item name cannot be blank.\n",
                execute("restock-i n/ c/Components q/5"));
        assertEquals("Category cannot be blank.\n",
                execute("restock-i n/Wire c/ q/5"));
    }

    /**
     * Runs one command and returns its console output with Unix line endings.
     */
    private String execute(String command) {
        output.reset();
        parser.handleCommand(command, state);
        return printedOutput();
    }

    /**
     * Returns the output captured since the last reset, with Unix line endings.
     */
    private String printedOutput() {
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}

package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.bob.exception.CategoryNotFoundException;
import seedu.bob.exception.CommandException;
import seedu.bob.exception.LoanException;

/**
 * Tests inventory deductions and the independence of stored loan quantities and list snapshots.
 */
class LoanManagerTest {

    private final Inventory inventory = new Inventory();
    private final LoanManager loans = new LoanManager(inventory);
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private PrintStream originalOutput;
    private PrintStream capturedOutput;

    @BeforeEach
    void setUp() throws CommandException {
        originalOutput = System.out;
        capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8);
        System.setOut(capturedOutput);
        inventory.addItem(new InventoryItem("Wire", "Components", 10));
        output.reset();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOutput);
        capturedOutput.close();
    }

    @Test
    void addLoan_validQuantity_deductsOnceAndPrintsOnlyLoanConfirmation() throws CommandException {
        Loan loan = createLoan("Wire", "Components", 3, "30 October 2099");
        loans.addLoan(loan);

        assertEquals(7, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(List.of(loan), loans.getLoans());
        assertEquals("Successfully added loan: 3x Wire [Components] to Alex (91234567), due 30 October 2099\n",
                output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n"));
        loans.listLoans();
        loans.listLoans();
        assertEquals(7, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void addLoan_repeatedItemAndBorrower_keepsSeparateDueDates() throws CommandException {
        Loan first = createLoan("Wire", "Components", 2, "30 October 2099");
        Loan second = createLoan("Wire", "Components", 3, "31 October 2099");
        loans.addLoan(first);
        loans.addLoan(second);

        assertEquals(List.of(first, second), loans.getLoans());
        assertEquals(5, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void addLoan_stockInsufficientOrMissing_preservesInventoryAndExistingLoans() throws CommandException {
        loans.addLoan(createLoan("Wire", "Components", 2, "30 October 2099"));
        List<Loan> before = loans.getLoans();
        assertThrows(LoanException.class,
                () -> loans.addLoan(createLoan("Wire", "Components", 9, "30 October 2099")));
        assertThrows(LoanException.class,
                () -> loans.addLoan(createLoan("Battery", "Components", 1, "30 October 2099")));
        assertThrows(CategoryNotFoundException.class,
                () -> loans.addLoan(createLoan("Wire", "Unknown", 1, "30 October 2099")));
        assertThrows(NullPointerException.class, () -> loans.addLoan(null));

        assertEquals(8, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(before, loans.getLoans());
    }

    @Test
    void addLoan_caseVariesAndStockDepleted_keepsOtherCategoriesAndLoanSnapshot() throws CommandException {
        inventory.addItem(new InventoryItem("Wire", "Spares", 5));
        Loan loan = createLoan("wire", "COMPONENTS", 10, "30 October 2099");
        loans.addLoan(loan);

        assertEquals(0, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(5, inventory.getAvailableQuantity("Wire", "Spares"));
        inventory.addItem(new InventoryItem("Wire", "Components", 4));
        inventory.removeItem("Components", 1, 1);
        assertEquals(10, loan.getQuantity());
        assertEquals(3, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void getLoans_snapshot_cannotMutateOrObserveLaterAdditions() throws CommandException {
        List<Loan> before = loans.getLoans();
        loans.addLoan(createLoan("Wire", "Components", 1, "30 October 2099"));
        assertTrue(before.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> loans.getLoans().clear());
    }

    @Test
    void addLoan_maximumQuantity_deductsWithoutOverflow() throws CommandException {
        inventory.addItem(new InventoryItem("Resistor", "Components", Integer.MAX_VALUE));
        loans.addLoan(createLoan("Resistor", "Components", Integer.MAX_VALUE, "30 October 2099"));
        assertEquals(0, inventory.getAvailableQuantity("Resistor", "Components"));
        assertEquals(Integer.MAX_VALUE, loans.getLoans().get(0).getQuantity());
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
    }

    /**
     * Creates one validated request without changing the inventory or active loans.
     */
    private Loan createLoan(String name, String category, int quantity, String date) throws CommandException {
        return new Loan(name, category, quantity, new Contact("Alex", "91234567", null, null), date);
    }
}

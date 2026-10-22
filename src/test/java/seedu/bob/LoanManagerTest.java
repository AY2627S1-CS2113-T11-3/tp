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
import seedu.bob.exception.InvalidLoanIndexException;
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

    @Test
    void deleteLoan_middleRecord_restoresOnlyItsQuantityAndRenumbersLoans() throws CommandException {
        Loan first = createLoan("Wire", "Components", 2, "30 October 2099");
        Loan second = createLoan("Wire", "Components", 3, "31 October 2099");
        Loan third = createLoan("Wire", "Components", 1, "1 November 2099");
        loans.addLoan(first);
        loans.addLoan(second);
        loans.addLoan(third);
        output.reset();

        loans.deleteLoan(2);

        assertEquals(7, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(List.of(first, third), loans.getLoans());
        assertEquals("Successfully deleted incorrect loan: " + second + ". Inventory restored.\n",
                output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n"));
        loans.deleteLoan(2);
        loans.deleteLoan(1);
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
        assertTrue(loans.getLoans().isEmpty());
        assertThrows(InvalidLoanIndexException.class, () -> loans.deleteLoan(1));
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void deleteLoan_invalidIndices_preserveStockAndLoans() throws CommandException {
        loans.addLoan(createLoan("Wire", "Components", 2, "30 October 2099"));
        List<Loan> before = loans.getLoans();
        for (int index : new int[] {-1, 0, 2, Integer.MAX_VALUE}) {
            assertThrows(InvalidLoanIndexException.class, () -> loans.deleteLoan(index));
            assertEquals(before, loans.getLoans());
            assertEquals(8, inventory.getAvailableQuantity("Wire", "Components"));
        }
    }

    @Test
    void deleteLoan_depletedCategory_recreatesStockFromLoanWithoutExtraConfirmation() throws CommandException {
        loans.addLoan(createLoan("wire", "COMPONENTS", 10, "30 October 2099"));
        output.reset();
        loans.deleteLoan(1);

        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
        assertTrue(loans.getLoans().isEmpty());
        assertEquals("Successfully deleted incorrect loan: 10x wire [COMPONENTS] to Alex (91234567),"
                + " due 30 October 2099. Inventory restored.\n",
                output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n"));
    }

    @Test
    void deleteLoan_itemReaddedWithDifferentCase_mergesStockWithoutChangingOtherCategory() throws CommandException {
        loans.addLoan(createLoan("wire", "COMPONENTS", 10, "30 October 2099"));
        inventory.addItem(new InventoryItem("WIRE", "Components", 4));
        inventory.addItem(new InventoryItem("Wire", "Spares", 5));
        loans.deleteLoan(1);

        assertEquals(14, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(5, inventory.getAvailableQuantity("Wire", "Spares"));
        output.reset();
        inventory.listItems();
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("1: WIRE (Qty: 14)"));
    }

    @Test
    void deleteLoan_itemDepletedButCategorySurvives_restoresWithoutChangingSurvivingItems() throws CommandException {
        inventory.addItem(new InventoryItem("Battery", "Components", 1));
        loans.addLoan(createLoan("Wire", "Components", 10, "30 October 2099"));
        loans.deleteLoan(1);

        assertEquals(1, inventory.getAvailableQuantity("Battery", "Components"));
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
        output.reset();
        inventory.listItems();
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("2: Wire (Qty: 10)"));
    }

    @Test
    void deleteLoan_restorationOverflows_preservesLoanAndStockUntilRetryIsPossible() throws CommandException {
        Loan loan = createLoan("Wire", "Components", 10, "30 October 2099");
        loans.addLoan(loan);
        inventory.addItem(new InventoryItem("Wire", "Components", Integer.MAX_VALUE));
        output.reset();

        LoanException error = assertThrows(LoanException.class, () -> loans.deleteLoan(1));

        assertEquals("Cannot restore 10 Wire. Inventory quantity must not exceed 2147483647.", error.getMessage());
        assertEquals(Integer.MAX_VALUE, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(List.of(loan), loans.getLoans());
        assertEquals("", output.toString(StandardCharsets.UTF_8));
        inventory.removeItem("Components", 1, 10);
        loans.deleteLoan(1);
        assertEquals(Integer.MAX_VALUE, inventory.getAvailableQuantity("Wire", "Components"));
        assertTrue(loans.getLoans().isEmpty());
    }

    @Test
    void returnLoan_middleRecord_restoresFullQuantityAndPreservesOtherLoans() throws CommandException {
        Loan first = createLoan("Wire", "Components", 2, "30 October 2099");
        Loan second = createLoan("Wire", "Components", 3, "31 October 2099");
        Loan third = createLoan("Wire", "Components", 1, "1 November 2099");
        loans.addLoan(first);
        loans.addLoan(second);
        loans.addLoan(third);
        output.reset();

        loans.returnLoan(2);

        assertEquals(7, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(List.of(first, third), loans.getLoans());
        assertEquals("Successfully returned loan: " + second + ". Inventory restored.\n",
                output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n"));
        loans.deleteLoan(1);
        assertEquals(List.of(third), loans.getLoans());
        loans.returnLoan(1);
        assertTrue(loans.getLoans().isEmpty());
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
        assertThrows(InvalidLoanIndexException.class, () -> loans.returnLoan(1));
        assertThrows(InvalidLoanIndexException.class, () -> loans.deleteLoan(1));
        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
    }

    @Test
    void returnLoan_invalidIndices_preserveStockAndLoans() throws CommandException {
        assertThrows(InvalidLoanIndexException.class, () -> loans.returnLoan(1));
        loans.addLoan(createLoan("Wire", "Components", 2, "30 October 2099"));
        List<Loan> before = loans.getLoans();
        output.reset();
        for (int index : new int[] {-1, 0, 2, Integer.MAX_VALUE}) {
            assertThrows(InvalidLoanIndexException.class, () -> loans.returnLoan(index));
            assertEquals(before, loans.getLoans());
            assertEquals(8, inventory.getAvailableQuantity("Wire", "Components"));
        }
        assertEquals("", output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void returnLoan_depletedItemWithinExistingCategory_recreatesOnlyThatItem() throws CommandException {
        inventory.addItem(new InventoryItem("Battery", "Components", 1));
        loans.addLoan(createLoan("wire", "COMPONENTS", 10, "30 October 2099"));
        loans.returnLoan(1);

        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals(1, inventory.getAvailableQuantity("Battery", "Components"));
        assertTrue(loans.getLoans().isEmpty());
        output.reset();
        inventory.listItems();
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("2: wire (Qty: 10)"));
    }

    @Test
    void returnLoan_depletedCategory_recreatesCategoryWithoutExtraConfirmation() throws CommandException {
        Loan loan = createLoan("Wire", "Components", 10, "30 October 2099");
        loans.addLoan(loan);
        output.reset();
        loans.returnLoan(1);

        assertEquals(10, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals("Successfully returned loan: " + loan + ". Inventory restored.\n",
                output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n"));
        assertTrue(loans.getLoans().isEmpty());
    }

    @Test
    void returnLoan_restorationOverflows_preservesRecordForRetry() throws CommandException {
        Loan loan = createLoan("Wire", "Components", 10, "30 October 2099");
        loans.addLoan(loan);
        inventory.addItem(new InventoryItem("Wire", "Components", Integer.MAX_VALUE));
        output.reset();

        LoanException error = assertThrows(LoanException.class, () -> loans.returnLoan(1));

        assertEquals("Cannot restore 10 Wire. Inventory quantity must not exceed 2147483647.", error.getMessage());
        assertEquals(List.of(loan), loans.getLoans());
        assertEquals(Integer.MAX_VALUE, inventory.getAvailableQuantity("Wire", "Components"));
        assertEquals("", output.toString(StandardCharsets.UTF_8));
        inventory.removeItem("Components", 1, 10);
        loans.returnLoan(1);
        assertTrue(loans.getLoans().isEmpty());
        assertEquals(Integer.MAX_VALUE, inventory.getAvailableQuantity("Wire", "Components"));
    }

    /**
     * Creates one validated request without changing the inventory or active loans.
     */
    private Loan createLoan(String name, String category, int quantity, String date) throws CommandException {
        return new Loan(name, category, quantity, new Contact("Alex", "91234567", null, null), date);
    }
}

//@@author DaynaG3
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;

/**
 * Verifies loan rendering independently of inventory mutations and manager output.
 */
class LoanUiTest {

    private static final String DIVIDER = "--------------------------------------------------------------------\n";
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
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
    void showLoanAdded_currentLoan_printsConfirmationWithoutWarning() throws CommandException {
        Loan loan = createLoan("30 October 2099");

        Ui.showLoanAdded(new LoanAdditionResult(loan, false));

        assertEquals("Successfully added loan: 3x Wire [Components] to Alex (00123456), due 30 October 2099\n",
                response());
    }

    @Test
    void showLoanAdded_overdueLoan_printsWarningAfterConfirmation() throws CommandException {
        Loan loan = createLoan("1 January 2000");

        Ui.showLoanAdded(new LoanAdditionResult(loan, true));

        assertEquals("Successfully added loan: 3x Wire [Components] to Alex (00123456), due 1 January 2000\n"
                + "Warning: this loan is already overdue.\n", response());
    }

    @Test
    void showRemovalResults_sameLoan_printsDistinctCorrectionAndReturnMessages() throws CommandException {
        Loan loan = createLoan("30 October 2099");

        Ui.showLoanDeleted(loan);
        Ui.showLoanReturned(loan);

        assertEquals("Successfully deleted incorrect loan: 3x Wire [Components] to Alex (00123456),"
                + " due 30 October 2099. Inventory restored.\n"
                + "Successfully returned loan: 3x Wire [Components] to Alex (00123456),"
                + " due 30 October 2099. Inventory restored.\n", response());
    }

    @Test
    void showLoans_emptyViews_printsDistinctEmptyMessages() {
        Ui.showLoans(List.of(), false);
        assertEquals(DIVIDER + "Loans\n" + DIVIDER + "No active loans.\n" + DIVIDER, response());

        output.reset();
        Ui.showLoans(List.of(), true);
        assertEquals(DIVIDER + "Overdue loans\n" + DIVIDER + "No overdue loans.\n" + DIVIDER, response());
    }

    @Test
    void showLoans_mixedRows_rendersSuppliedFlagsWithoutConsultingTheClock() throws CommandException {
        // These rows represent a manager query in 2100, independently of the computer's date.
        Loan current = createLoan("1 January 2101");
        Loan overdue = createLoan("30 October 2099");

        Ui.showLoans(List.of(new LoanListEntry(1, current, false), new LoanListEntry(2, overdue, true)), false);

        assertEquals(DIVIDER + "Loans\n" + DIVIDER
                + "1. 3x Wire [Components] to Alex (00123456), due 1 January 2101\n"
                + "2. 3x Wire [Components] to Alex (00123456), due 30 October 2099 [OVERDUE]\n"
                + DIVIDER, response());
    }

    @Test
    void showLoans_overdueRows_preservesGapsInFullListPositions() throws CommandException {
        Loan loan = createLoan("1 January 2000");

        Ui.showLoans(List.of(new LoanListEntry(2, loan, true), new LoanListEntry(4, loan, true)), true);

        assertEquals(DIVIDER + "Overdue loans\n" + DIVIDER
                + "2. 3x Wire [Components] to Alex (00123456), due 1 January 2000 [OVERDUE]\n"
                + "4. 3x Wire [Components] to Alex (00123456), due 1 January 2000 [OVERDUE]\n"
                + DIVIDER, response());
    }

    /**
     * Creates a display fixture with leading zeros that must survive rendering.
     */
    private Loan createLoan(String dueDate) throws CommandException {
        return new Loan("Wire", "Components", 3, new Contact("Alex", "00123456", null, null), dueDate);
    }

    /**
     * Returns captured stdout with normalized line endings.
     */
    private String response() {
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}

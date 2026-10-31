//@@author DaynaG3
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.bob.AppSnapshot.ContactEntry;
import seedu.bob.AppSnapshot.InventoryCategory;
import seedu.bob.AppSnapshot.InventoryEntry;
import seedu.bob.AppSnapshot.LoanEntry;
import seedu.bob.AppSnapshot.PreparationEntry;
import seedu.bob.AppSnapshot.RequestEntry;
import seedu.bob.AppSnapshot.SessionEntry;
import seedu.bob.exception.InvalidRequestDeadlineException;
import seedu.bob.exception.InvalidSessionDateException;
import seedu.bob.exception.InvalidSnapshotException;

/** Exercises complete model restoration, independence, ordering, and domain validation at the snapshot boundary. */
class AppStateTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2030-01-01T00:00:00Z"), ZoneOffset.UTC);

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
    void restore_populatedSnapshot_preservesAllValuesWithoutConfirmations() throws Exception {
        AppSnapshot snapshot = createSnapshot();

        AppState state = AppState.restore(snapshot, CLOCK);

        assertEquals(snapshot, state.toSnapshot());
        assertEquals("", output.toString(StandardCharsets.UTF_8));
        assertEquals(List.of("fulfilled", "pending", "prepared"),
                state.toSnapshot().requests().stream().map(RequestEntry::status).toList());
    }

    @Test
    void restore_emptySnapshot_returnsIndependentEmptyState() throws Exception {
        AppSnapshot snapshot = new AppState(CLOCK).toSnapshot();

        AppState first = AppState.restore(snapshot, CLOCK);
        AppState second = AppState.restore(snapshot, CLOCK);
        first.getInventory().addItem(new InventoryItem("Beaker", "Glassware", 1));

        assertEquals(snapshot, second.toSnapshot());
        assertEquals(0, second.getInventory().getAvailableQuantity("Beaker", "Glassware"));
    }

    @Test
    void restore_liveState_roundTripsActualCommandChanges() throws Exception {
        AppState original = new AppState(CLOCK);
        String date = LocalDate.now().plusYears(1)
                .format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH));
        original.getInventory().addItem(new InventoryItem("Beaker", "Glassware", 10));
        original.getInventory().addItem(new InventoryItem("Gloves", "Safety", 20));
        original.getSessionManager().addSession(new Session("Chemistry", date, "Lab 1", "0900", "1100", 10));
        Preparation preparation = original.getSessionManager().getSession(1).getPreparation();
        preparation.addItem("Beaker", "Glassware", 2, 10);
        preparation.removeItem(1, 7);
        original.getRequestManager().addRequest(new Request("Jamie", "Beaker", 8, date));
        original.getRequestManager().markRequest(1, "prepared", original.getInventory());
        original.getContactManager().addContact(new Contact("Supplier", "01234567", null, "Repairs"));
        original.getLoanManager().addLoan(new Loan("Beaker", "Glassware", 3,
                new Contact("Taylor", "02345678", null, null), date));
        AppSnapshot snapshot = original.toSnapshot();
        output.reset();

        AppState restored = AppState.restore(snapshot, CLOCK);

        assertEquals(snapshot, restored.toSnapshot());
        assertEquals(7, restored.getInventory().getAvailableQuantity("Beaker", "Glassware"));
        assertEquals(13,
                restored.getSessionManager().getSession(1).getPreparation().getItems().getFirst().getQuantity());
        assertEquals("", output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void restore_loans_doesNotDeductAgainAndReturnsToSharedInventory() throws Exception {
        AppState state = AppState.restore(createSnapshot(), CLOCK);

        assertEquals(7, state.getInventory().getAvailableQuantity("beaker", "glassware"));
        assertEquals(3, state.getLoanManager().getLoans().size());
        assertEquals(0, state.getInventory().getAvailableQuantity("Microscope", "Equipment"));
        state.getLoanManager().returnLoan(1);
        assertEquals(10, state.getInventory().getAvailableQuantity("Beaker", "Glassware"));
        state.getLoanManager().returnLoan(1);
        assertEquals(1, state.getInventory().getAvailableQuantity("Microscope", "Equipment"));
        state.getLoanManager().returnLoan(1);
        assertEquals(2, state.getInventory().getAvailableQuantity("Microscope", "Equipment"));
        assertTrue(state.getLoanManager().getLoans().isEmpty());
    }

    @Test
    void toSnapshot_laterModelChanges_doNotAlterEarlierSnapshotOrOtherState() throws Exception {
        AppSnapshot expected = createSnapshot();
        AppState first = AppState.restore(expected, CLOCK);
        AppState second = AppState.restore(expected, CLOCK);
        AppSnapshot beforeChanges = first.toSnapshot();

        first.getInventory().removeItem("Glassware", 1, 1);
        first.getRequestManager().markRequest(2, "prepared", first.getInventory());
        first.getSessionManager().getSession(2).getPreparation().removeItem(1, 1);
        first.getContactManager().deleteContact(1);
        first.getLoanManager().returnLoan(1);
        first.getSessionManager().deleteSession(1);

        assertEquals(expected, beforeChanges);
        assertEquals(expected, second.toSnapshot());
    }

    @Test
    void restore_unsortedSessionsAndRequests_sortsWithoutMovingPreparation() throws Exception {
        AppSnapshot saved = createSnapshot();
        AppSnapshot unsorted = snapshot(saved.inventory(), saved.sessions().reversed(), saved.requests().reversed(),
                saved.contacts(), saved.loans());

        AppState state = AppState.restore(unsorted, CLOCK);

        assertEquals(saved, state.toSnapshot());
        state.getSessionManager().deleteSession(1);
        assertEquals("Introduction to chemistry", state.getSessionManager().getSession(1).getName());
        assertEquals(13, state.getSessionManager().getSession(1).getPreparation().getItems().getFirst().getQuantity());
    }

    @Test
    void restore_historicalDates_acceptsSavedRecordsButCommandsStillRejectPastDates() throws Exception {
        AppState restored = AppState.restore(createSnapshot(), CLOCK);

        assertEquals(LocalDate.of(2020, 1, 15), restored.getSessionManager().getSession(1).getDate());
        assertEquals(LocalDate.of(2020, 1, 15), restored.getRequestManager().getRequest(1).getDeadline());
        assertThrows(InvalidSessionDateException.class,
                () -> new Session("Old lab", "15 January 2020", "Lab 1", "0900", "1100", 10));
        assertThrows(InvalidRequestDeadlineException.class,
                () -> new Request("Alex", "Gloves", 2, "15 January 2020", CLOCK));
    }

    @Test
    void restore_differentCurrentDates_recalculatesOverdueFlags() throws Exception {
        AppSnapshot saved = createSnapshot();
        AppState beforeDeadline = AppState.restore(saved, CLOCK);
        Clock later = Clock.fixed(Instant.parse("2100-01-01T00:00:00Z"), ZoneOffset.UTC);
        AppState afterDeadline = AppState.restore(saved, later);

        assertEquals(1, beforeDeadline.getLoanManager().getOverdueLoanEntries().size());
        assertEquals(3, afterDeadline.getLoanManager().getOverdueLoanEntries().size());
        assertEquals(saved, afterDeadline.toSnapshot());
    }

    @Test
    void snapshot_mutableInputLists_copiesRootAndNestedCollections() {
        List<InventoryEntry> items = new ArrayList<>(List.of(new InventoryEntry("Beaker", 7)));
        InventoryCategory category = new InventoryCategory("Glassware", items);
        List<InventoryCategory> categories = new ArrayList<>(List.of(category));
        List<PreparationEntry> preparation = new ArrayList<>(List.of(new PreparationEntry("Beaker", "Glassware", 13)));
        SessionEntry session = new SessionEntry("Lab", "2020-01-15", "Lab 1", "09:00", "11:00", 10, preparation);
        AppSnapshot saved = snapshot(categories, List.of(session), List.of(), List.of(), List.of());

        items.clear();
        categories.clear();
        preparation.clear();

        assertEquals(7, saved.inventory().getFirst().items().getFirst().quantity());
        assertEquals(13, saved.sessions().getFirst().preparation().getFirst().quantity());
        assertThrows(UnsupportedOperationException.class, () -> saved.inventory().clear());
        assertThrows(UnsupportedOperationException.class, () -> category.items().clear());
        assertThrows(UnsupportedOperationException.class, () -> session.preparation().clear());
        assertThrows(NullPointerException.class,
                () -> new AppSnapshot(1, null, List.of(), List.of(), List.of(), List.of()));
    }

    @Test
    void restore_invalidVersionOrNullSnapshot_rejectsWithDiagnosticCause() {
        AppSnapshot saved = new AppSnapshot(2, List.of(), List.of(), List.of(), List.of(), List.of());

        InvalidSnapshotException error = assertThrows(InvalidSnapshotException.class,
                () -> AppState.restore(saved, CLOCK));

        assertTrue(error.getMessage().contains("Unsupported snapshot version: 2"));
        assertNotNull(error.getCause());
        assertThrows(InvalidSnapshotException.class, () -> AppState.restore(null, CLOCK));
    }

    @Test
    void restore_invalidInventory_rejectsDuplicatesEmptyCategoriesAndBadValues() {
        List<List<InventoryCategory>> invalidInventories = List.of(
                List.of(new InventoryCategory("Glassware", List.of(new InventoryEntry("Beaker", 1))),
                        new InventoryCategory("glassware", List.of(new InventoryEntry("Tube", 1)))),
                List.of(new InventoryCategory("Glassware", List.of(
                        new InventoryEntry("Beaker", 1), new InventoryEntry("BEAKER", 2)))),
                List.of(new InventoryCategory("Glassware", List.of())),
                List.of(new InventoryCategory(" ", List.of(new InventoryEntry("Beaker", 1)))),
                List.of(new InventoryCategory("Glassware", List.of(new InventoryEntry(null, 1)))),
                List.of(new InventoryCategory("Glassware", List.of(new InventoryEntry("Beaker", 0)))));

        for (List<InventoryCategory> inventory : invalidInventories) {
            assertThrows(InvalidSnapshotException.class,
                    () -> AppState.restore(snapshot(inventory, List.of(), List.of(), List.of(), List.of()), CLOCK));
        }
    }

    @Test
    void restore_invalidSessions_rejectsBadDatesTimesHeadcountsAndConflicts() {
        List<SessionEntry> invalidSessions = List.of(
                session("2020-02-30", "09:00", "11:00", 10, List.of()),
                session("15 January 2020", "09:00", "11:00", 10, List.of()),
                session(null, "09:00", "11:00", 10, List.of()),
                session("2020-01-15", "0900", "11:00", 10, List.of()),
                session("2020-01-15", "09:00:00", "11:00", 10, List.of()),
                session("2020-01-15", "09:00", "24:00", 10, List.of()),
                session("2020-01-15", "11:00", "09:00", 10, List.of()),
                session("2020-01-15", "09:00", "11:00", 0, List.of()));
        for (SessionEntry session : invalidSessions) {
            assertThrows(InvalidSnapshotException.class,
                    () -> AppState.restore(
                            snapshot(List.of(), List.of(session), List.of(), List.of(), List.of()), CLOCK));
        }
        SessionEntry first = session("2020-01-15", "09:00", "11:00", 10, List.of());
        SessionEntry conflict = new SessionEntry("Other", "2020-01-15", "lab 1", "10:00", "12:00", 10, List.of());
        assertThrows(InvalidSnapshotException.class, () -> AppState.restore(
                snapshot(List.of(), List.of(first, conflict), List.of(), List.of(), List.of()), CLOCK));
    }

    @Test
    void restore_invalidPreparation_rejectsDuplicatePairsAndNonpositiveTotals() {
        List<List<PreparationEntry>> invalidPreparations = List.of(
                List.of(new PreparationEntry("Beaker", "Glassware", 1),
                        new PreparationEntry("BEAKER", "glassware", 2)),
                List.of(new PreparationEntry("Beaker", "Glassware", 0)),
                List.of(new PreparationEntry("Beaker", null, 1)));

        for (List<PreparationEntry> preparation : invalidPreparations) {
            SessionEntry session = session("2020-01-15", "09:00", "11:00", 10, preparation);
            assertThrows(InvalidSnapshotException.class,
                    () -> AppState.restore(
                            snapshot(List.of(), List.of(session), List.of(), List.of(), List.of()), CLOCK));
        }
    }

    @Test
    void restore_invalidRequests_rejectsStatusesDeadlinesAndQuantities() {
        List<RequestEntry> invalidRequests = List.of(
                new RequestEntry("Alex", "Gloves", 1, "2020-01-15", "Prepared"),
                new RequestEntry("Alex", "Gloves", 1, "2020-01-15", null),
                new RequestEntry("Alex", "Gloves", 1, "2020-02-30", "pending"),
                new RequestEntry("Alex", "Gloves", -1, "2020-01-15", "pending"),
                new RequestEntry("\u2003", "Gloves", 1, "2020-01-15", "pending"));

        for (RequestEntry request : invalidRequests) {
            assertThrows(InvalidSnapshotException.class,
                    () -> AppState.restore(
                            snapshot(List.of(), List.of(), List.of(request), List.of(), List.of()), CLOCK));
        }
    }

    @Test
    void restore_invalidContactsAndLateLoanFailure_doesNotChangeExistingState() throws Exception {
        AppState existing = AppState.restore(createSnapshot(), CLOCK);
        AppSnapshot before = existing.toSnapshot();
        List<ContactEntry> invalidContacts = List.of(
                new ContactEntry("Supplier", "123-456", null, null),
                new ContactEntry("Supplier", "01234567", "invalid", null),
                new ContactEntry("Supplier", "01234567", null, " "));
        for (ContactEntry contact : invalidContacts) {
            assertThrows(InvalidSnapshotException.class, () -> AppState.restore(
                    snapshot(List.of(), List.of(), List.of(), List.of(contact), List.of()), CLOCK));
        }
        List<LoanEntry> loans = new ArrayList<>(before.loans());
        loans.add(new LoanEntry("Beaker", "Glassware", 1, "Borrower", "bad phone", "2020-01-15"));
        assertThrows(InvalidSnapshotException.class, () -> AppState.restore(
                snapshot(before.inventory(), before.sessions(), before.requests(), before.contacts(), loans), CLOCK));
        assertEquals(before, existing.toSnapshot());
    }

    /** Matches the significant values and edge cases in the documented version-1 example. */
    private static AppSnapshot createSnapshot() {
        SessionEntry historical = new SessionEntry("Completed chemistry lab", "2020-01-15", "Lab 1",
                "09:00", "11:00", 10, List.of());
        SessionEntry upcoming = new SessionEntry("Introduction to chemistry", "2099-11-05", "Lab 1",
                "09:00", "11:00", 10, List.of(new PreparationEntry("Beaker", "Glassware", 13),
                        new PreparationEntry("Gloves", "Safety", 20),
                        new PreparationEntry("Test tube", "Glassware", 10)));
        LoanEntry microscope = new LoanEntry("Microscope", "Equipment", 1, "Casey Ng", "03456789", "2099-11-06");
        return snapshot(
                List.of(new InventoryCategory("Glassware", List.of(new InventoryEntry("Beaker", 7))),
                        new InventoryCategory("Safety", List.of(new InventoryEntry("Gloves", 20)))),
                List.of(historical, upcoming),
                List.of(new RequestEntry("Alex Tan", "Gloves", 2, "2020-01-15", "fulfilled"),
                        new RequestEntry("Jamie Lim", "Beaker", 8, "2099-11-04", "pending"),
                        new RequestEntry("Morgan Lee", "Gloves", 4, "2099-11-05", "prepared")),
                List.of(new ContactEntry("Example Lab Supplies", "01234567", "orders@example.com", "Supplier"),
                        new ContactEntry("Example Maintenance", "07654321", null, null)),
                List.of(new LoanEntry("Beaker", "Glassware", 3, "Taylor Wong", "02345678", "2020-01-16"),
                        microscope, microscope));
    }

    /** Creates a version-1 snapshot with selected collections, avoiding irrelevant fixture data in error cases. */
    private static AppSnapshot snapshot(List<InventoryCategory> inventory, List<SessionEntry> sessions,
            List<RequestEntry> requests, List<ContactEntry> contacts, List<LoanEntry> loans) {
        return new AppSnapshot(AppSnapshot.CURRENT_VERSION, inventory, sessions, requests, contacts, loans);
    }

    /** Creates a minimal session for validation cases without preparation or name differences. */
    private static SessionEntry session(String date, String startTime, String endTime, int headcount,
            List<PreparationEntry> preparation) {
        return new SessionEntry("Lab", date, "Lab 1", startTime, endTime, headcount, preparation);
    }
}

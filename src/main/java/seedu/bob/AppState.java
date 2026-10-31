package seedu.bob;

import java.time.Clock;
import java.util.Objects;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.InvalidSnapshotException;

/** Owns the application models and keeps loans connected to the same inventory as other commands. */
public class AppState {

    private final Inventory inventory;
    private final SessionManager sessionManager;
    private final RequestManager requestManager;
    private final ContactManager contactManager;
    private final LoanManager loanManager;

    /** Creates empty application state using the computer's clock for overdue queries. */
    public AppState() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Creates empty state with an explicit clock for deterministic overdue tests.
     *
     * @param clock Clock used by loans to determine the current date.
     * @throws NullPointerException If the clock is null.
     */
    public AppState(Clock clock) {
        this(new Inventory(), new SessionManager(), new RequestManager(), new ContactManager(), clock);
    }

    /** Wires newly restored managers before any of them become visible to the caller. */
    private AppState(Inventory inventory, SessionManager sessionManager, RequestManager requestManager,
            ContactManager contactManager, Clock clock) {
        this.inventory = inventory;
        this.sessionManager = sessionManager;
        this.requestManager = requestManager;
        this.contactManager = contactManager;
        this.loanManager = new LoanManager(inventory, clock);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }

    public ContactManager getContactManager() {
        return contactManager;
    }

    public LoanManager getLoanManager() {
        return loanManager;
    }

    /**
     * Returns an immutable, independent snapshot of every persisted feature.
     *
     * @return Storage values containing no references to mutable domain objects.
     */
    public AppSnapshot toSnapshot() {
        return new AppSnapshot(AppSnapshot.CURRENT_VERSION, inventory.toSnapshot(), sessionManager.toSnapshot(),
                requestManager.toSnapshot(), contactManager.toSnapshot(), loanManager.toSnapshot());
    }

    /**
     * Restores a complete snapshot without changing any existing application state or deducting loan stock.
     *
     * @param snapshot Saved values to validate and restore.
     * @return Independent application state, exposed only after all records pass validation.
     * @throws InvalidSnapshotException If any saved record or version is invalid.
     */
    public static AppState restore(AppSnapshot snapshot) throws InvalidSnapshotException {
        return restore(snapshot, Clock.systemDefaultZone());
    }

    /**
     * Restores all records using an explicit clock for overdue queries after restoration.
     *
     * @param snapshot Saved values to validate and restore.
     * @param clock Clock used for current overdue queries, not validation of historical dates.
     * @return Independent application state after every record passes validation.
     * @throws InvalidSnapshotException If any saved record or version is invalid.
     * @throws NullPointerException If the clock is null.
     */
    public static AppState restore(AppSnapshot snapshot, Clock clock) throws InvalidSnapshotException {
        Objects.requireNonNull(clock, "Clock must not be null");
        try {
            Objects.requireNonNull(snapshot, "Snapshot must not be null");
            if (snapshot.schemaVersion() != AppSnapshot.CURRENT_VERSION) {
                throw new IllegalArgumentException("Unsupported snapshot version: " + snapshot.schemaVersion());
            }
            AppState restored = new AppState(Inventory.restore(snapshot.inventory()),
                    SessionManager.restore(snapshot.sessions()), RequestManager.restore(snapshot.requests()),
                    ContactManager.restore(snapshot.contacts()), clock);
            restored.loanManager.restoreLoans(snapshot.loans());
            return restored;
        } catch (CommandException | IllegalArgumentException | NullPointerException e) {
            throw new InvalidSnapshotException("Invalid saved state: " + e.getMessage(), e);
        }
    }
}

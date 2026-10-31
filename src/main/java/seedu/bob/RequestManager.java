//@@author Lokwx
package seedu.bob;

import java.util.ArrayList;
import java.util.Collections;
//@@author DaynaG3
import java.util.List;
//@@author
import java.util.Objects;
import java.util.logging.Logger;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.InvalidRequestIndexException;
import seedu.bob.exception.RequestException;

/**
 * Stores student requests in deadline order and displays request changes.
 */
public class RequestManager {

    private static final Logger LOGGER = Logger.getLogger(RequestManager.class.getName());
    private static final int INDEX_OFFSET = 1;
    private static final String LIST_UPDATED_MESSAGE =
            "Request list updated. Run list-r to see the latest order.";

    /** Kept sorted by deadline so displayed positions can be used by {@code delete-r} and {@code mark-r}. */
    private final ArrayList<Request> requests = new ArrayList<>();

    //@@author DaynaG3
    /** Exports requests in their current deadline order with their saved statuses. */
    List<AppSnapshot.RequestEntry> toSnapshot() {
        List<AppSnapshot.RequestEntry> entries = new ArrayList<>();
        for (Request request : requests) {
            entries.add(request.toSnapshot());
        }
        return List.copyOf(entries);
    }

    /** Restores historical requests without command confirmations, then applies normal ordering. */
    static RequestManager restore(List<AppSnapshot.RequestEntry> entries) throws CommandException {
        RequestManager manager = new RequestManager();
        for (AppSnapshot.RequestEntry entry : entries) {
            manager.requests.add(Request.restore(entry));
        }
        manager.sortRequests();
        assert manager.hasValidState() : "Restored requests must be valid and sorted";
        return manager;
    }
    //@@author

    /**
     * Adds a request, sorts the list by deadline, and prints a confirmation.
     *
     * @param request Request to add.
     * @throws NullPointerException If {@code request} is {@code null}.
     */
    public void addRequest(Request request) {
        Objects.requireNonNull(request, "Request to add must not be null");
        requests.add(request);
        sortRequests();

        assert hasValidState() : "Request list must remain valid after addition";
        LOGGER.fine(() -> "Added request for " + request.getStudentName());
        System.out.println("Successfully added request: " + request);
        System.out.println(LIST_UPDATED_MESSAGE);
    }

    /**
     * Keeps the displayed order and the stored order aligned by deadline.
     */
    private void sortRequests() {
        Collections.sort(requests);
    }

    /**
     * Removes a request by its one-based position in the deadline-sorted list and prints a confirmation.
     *
     * @param userIndex One-based request position, as shown by {@code list-r}.
     * @throws InvalidRequestIndexException If the index is outside the request list.
     */
    public void deleteRequest(int userIndex) throws InvalidRequestIndexException {
        Request removedRequest = getRequest(userIndex);
        requests.remove(userIndex - INDEX_OFFSET);

        assert hasValidState() : "Request list must remain valid after deletion";
        LOGGER.fine(() -> "Removed request for " + removedRequest.getStudentName());
        System.out.println("Successfully removed request: " + removedRequest);
        System.out.println(LIST_UPDATED_MESSAGE);
    }

    /**
     * Marks a request as prepared or fulfilled and prints a confirmation.
     * Fulfilling deducts the requested quantity from available stock; prepared does not.
     * Fulfillment is one-way, so an already fulfilled request cannot be changed again.
     *
     * @param userIndex One-based request position in the deadline-sorted list.
     * @param status New status, which must be {@code prepared} or {@code fulfilled}.
     * @param inventory Inventory used to deduct stock when the request is fulfilled.
     * @throws CommandException If the index or status is invalid, the request is already fulfilled,
     *     or stock is insufficient.
     */
    public void markRequest(int userIndex, String status, Inventory inventory) throws CommandException {
        Objects.requireNonNull(inventory, "Inventory must not be null");
        Request request = getRequest(userIndex);
        String normalizedStatus = Request.normalizeStatus(status);
        if (request.isFulfilled()) {
            throw new RequestException(String.format(
                    "Request %d is already fulfilled and cannot be changed: %s", userIndex, request));
        }
        if (Request.isFulfilledStatus(normalizedStatus)) {
            inventory.deductTotalAvailableQuantity(request.getItemName(), request.getQuantity());
        }
        request.markAs(normalizedStatus);

        assert hasValidState() : "Request list must remain valid after a status change";
        LOGGER.fine(() -> "Marked request for " + request.getStudentName()
                + " as " + request.getStatus());
        System.out.printf("Successfully marked request %d as %s: %s%n",
                userIndex, request.getStatus(), request);
    }

    /**
     * Returns the request at the position shown by {@code list-r}.
     *
     * @param userIndex One-based request position.
     * @return Request at the requested position.
     * @throws InvalidRequestIndexException If the index is outside the request list.
     */
    public Request getRequest(int userIndex) throws InvalidRequestIndexException {
        if (userIndex <= 0 || userIndex > requests.size()) {
            LOGGER.fine(() -> "Rejected invalid request index: " + userIndex);
            throw new InvalidRequestIndexException(userIndex, requests.size());
        }
        assert userIndex > 0 && userIndex <= requests.size()
                : "Validated request index must be within the list";
        return requests.get(userIndex - INDEX_OFFSET);
    }

    /**
     * Returns whether the request list contains only nonnull requests in deadline order.
     */
    private boolean hasValidState() {
        for (int i = 0; i < requests.size(); i++) {
            Request request = requests.get(i);
            if (request == null || (i > 0 && requests.get(i - 1).compareTo(request) > 0)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Prints all requests in deadline order with status and current inventory availability.
     *
     * @param inventory Inventory used to check whether each requested quantity is available.
     * @throws NullPointerException If {@code inventory} is {@code null}.
     */
    public void listRequests(Inventory inventory) {
        Objects.requireNonNull(inventory, "Inventory to check availability must not be null");
        LOGGER.fine(() -> "Listing " + requests.size() + " request(s)");

        System.out.println(Ui.DIVIDER);
        System.out.println("Student requests");
        System.out.println(Ui.DIVIDER);

        if (requests.isEmpty()) {
            System.out.println("No requests recorded.");
        } else {
            for (int i = 0; i < requests.size(); i++) {
                Request request = requests.get(i);
                // Availability is recalculated on every listing because stock may have changed.
                int available = inventory.getTotalAvailableQuantity(request.getItemName());
                String shortage = request.isFulfilled() ? "" : formatShortage(available, request.getQuantity());
                System.out.printf("%d. [%s] %s (Available: %d)%s%n",
                        i + INDEX_OFFSET, displayStatus(request.getStatus()), request, available, shortage);
            }
        }
        System.out.println(Ui.DIVIDER);
    }

    /**
     * Returns the shortage warning for a request, or an empty string when stock is sufficient.
     */
    private static String formatShortage(int available, int required) {
        if (available >= required) {
            return "";
        }
        return " !! Insufficient stock (Shortfall: " + (required - available) + ")";
    }

    /**
     * Capitalizes a stored lowercase status for display, falling back to {@code Unknown}
     * when a malformed value reaches this rendering layer.
     */
    private static String displayStatus(String status) {
        if (status == null || status.isBlank()) {
            LOGGER.warning(() -> "Request has an invalid stored status: " + status);
            return "Unknown";
        }
        return Character.toUpperCase(status.charAt(0)) + status.substring(1);
    }
}

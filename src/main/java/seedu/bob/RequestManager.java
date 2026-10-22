package seedu.bob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.InvalidRequestIndexException;

/**
 * Stores student requests in deadline order and displays request changes.
 *
 * @author Lokwx
 */
public class RequestManager {

    private static final Logger LOGGER = Logger.getLogger(RequestManager.class.getName());
    private static final int INDEX_OFFSET = 1;

    /** Kept sorted by deadline so displayed positions can be used by {@code delete-r} and {@code mark-r}. */
    private final ArrayList<Request> requests = new ArrayList<>();

    /**
     * Adds a request, sorts the list by deadline, and prints a confirmation.
     *
     * @param request Request to add.
     * @throws NullPointerException If {@code request} is {@code null}.
     */
    public void addRequest(Request request) {
        Objects.requireNonNull(request, "Request to add must not be null");
        requests.add(request);
        // Sorting on every addition keeps the displayed indices in deadline order.
        Collections.sort(requests);

        assert hasValidState() : "Request list must remain valid after addition";
        LOGGER.fine(() -> "Added request for " + request.getStudentName());
        System.out.println("Successfully added request: " + request);
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
    }

    /**
     * Marks a request as prepared or fulfilled and prints a confirmation.
     *
     * @param userIndex One-based request position in the deadline-sorted list.
     * @param status New status, which must be {@code prepared} or {@code fulfilled}.
     * @throws CommandException If the index is invalid or the status is not accepted.
     */
    public void markRequest(int userIndex, String status) throws CommandException {
        Request request = getRequest(userIndex);
        request.markAs(status);

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
     */
    public void listRequests(Inventory inventory) {
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
                boolean isInsufficient = available < request.getQuantity();
                String shortage = isInsufficient
                        ? " !! Insufficient stock (Shortfall: " + (request.getQuantity() - available) + ")"
                        : "";
                System.out.printf("%d. [%s] %s (Available: %d)%s%n",
                        i + INDEX_OFFSET, displayStatus(request.getStatus()), request, available, shortage);
            }
        }
        System.out.println(Ui.DIVIDER);
    }

    /**
     * Capitalizes a stored lowercase status for display.
     */
    private static String displayStatus(String status) {
        return Character.toUpperCase(status.charAt(0)) + status.substring(1);
    }
}

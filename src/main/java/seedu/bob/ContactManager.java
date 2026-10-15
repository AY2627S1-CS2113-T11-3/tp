package seedu.bob;

import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Stores contacts in insertion order and displays contact changes.
 */
public class ContactManager {

    /**
     * Records contact diagnostics at FINE level so normal console output stays unchanged.
     */
    private static final Logger LOGGER = Logger.getLogger(ContactManager.class.getName());

    private final ArrayList<Contact> contacts = new ArrayList<>();

    /**
     * Adds a contact to the list and prints a confirmation.
     *
     * @param contact Contact to add.
     * @throws NullPointerException If {@code contact} is {@code null}.
     */
    public void addContact(Contact contact) {
        Objects.requireNonNull(contact, "Contact to add must not be null");
        contacts.add(contact);
        assert hasValidState() : "Contact list must remain valid after addition";
        LOGGER.fine(() -> "Added contact: " + contact.getName());
        System.out.printf("Successfully added: %s (%s) to contacts%n", contact.getName(), contact.getPhone());
    }

    /**
     * Returns whether the contact list contains only nonnull contacts.
     */
    private boolean hasValidState() {
        for (Contact contact : contacts) {
            if (contact == null) {
                return false;
            }
        }
        return true;
    }
}

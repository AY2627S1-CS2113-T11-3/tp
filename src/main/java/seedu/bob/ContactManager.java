package seedu.bob;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.InvalidContactIndexException;

/**
 * Stores contacts in insertion order and displays contact changes.
 */
public class ContactManager {

    /**
     * Records contact diagnostics at FINE level so normal console output stays unchanged.
     */
    private static final Logger LOGGER = Logger.getLogger(ContactManager.class.getName());
    private static final int INDEX_OFFSET = 1;

    private final ArrayList<Contact> contacts = new ArrayList<>();

    /**
     * Creates an empty supplier and maintenance contact list.
     */
    public ContactManager() {
    }

    /** Copies contacts without losing optional fields or leading zeros in phone numbers. */
    List<AppSnapshot.ContactEntry> toSnapshot() {
        List<AppSnapshot.ContactEntry> entries = new ArrayList<>();
        for (Contact contact : contacts) {
            entries.add(new AppSnapshot.ContactEntry(contact.getName(), contact.getPhone(),
                    contact.getEmail(), contact.getRole()));
        }
        return List.copyOf(entries);
    }

    /** Validates and restores contacts silently, preserving duplicates and insertion order. */
    static ContactManager restore(List<AppSnapshot.ContactEntry> entries) throws CommandException {
        ContactManager manager = new ContactManager();
        for (AppSnapshot.ContactEntry entry : entries) {
            manager.contacts.add(new Contact(entry.name(), entry.phone(), entry.email(), entry.role()));
        }
        return manager;
    }

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
     * Removes a contact by its one-based index and prints a confirmation.
     *
     * @param userIndex One-based contact position, as shown by {@code list-c}.
     * @throws InvalidContactIndexException If the index is outside the contact list.
     */
    public void deleteContact(int userIndex) throws InvalidContactIndexException {
        Contact removedContact = getContact(userIndex);
        contacts.remove(userIndex - INDEX_OFFSET);
        assert hasValidState() : "Contact list must remain valid after deletion";
        LOGGER.fine(() -> "Removed contact: " + removedContact.getName());
        System.out.println("Successfully removed contact: " + removedContact.getName());
    }

    /**
     * Returns the contact at the position shown by {@code list-c}.
     *
     * @param userIndex One-based contact position.
     * @return Contact at the requested position.
     * @throws InvalidContactIndexException If the index is outside the contact list.
     */
    public Contact getContact(int userIndex) throws InvalidContactIndexException {
        if (userIndex <= 0 || userIndex > contacts.size()) {
            LOGGER.fine(() -> "Rejected invalid contact index: " + userIndex);
            throw new InvalidContactIndexException(userIndex, contacts.size());
        }
        return contacts.get(userIndex - INDEX_OFFSET);
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

    /**
     * Prints all contacts with one-based positions.
     */
    public void listContacts() {
        System.out.println(Ui.DIVIDER);
        System.out.println("Contacts");
        System.out.println(Ui.DIVIDER);

        if (contacts.isEmpty()) {
            System.out.println("No contacts saved.");
        } else {
            for (int i = 0; i < contacts.size(); i++) {
                System.out.printf("%d. %s%n", i + INDEX_OFFSET, contacts.get(i));
            }
        }
        System.out.println(Ui.DIVIDER);
    }
}

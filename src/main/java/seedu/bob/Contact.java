package seedu.bob;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.CommandFormatException;

/**
 * Represents a contact, such as a supplier or a maintenance office, with a phone number and an
 * optional email address and role.
 */
public class Contact {

    private final String name;
    private final String phone;
    /** Email address, or {@code null} if none was supplied. */
    private final String email;
    /** Description of what the contact handles, or {@code null} if none was supplied. */
    private final String role;

    /**
     * Creates a validated contact.
     *
     * @param name Contact name.
     * @param phone Phone number, which must consist of digits only.
     * @param email Email address, or {@code null} if not supplied.
     * @param role Role of the contact, or {@code null} if not supplied.
     * @throws CommandException If any supplied value is blank or invalid.
     */
    public Contact(String name, String phone, String email, String role) throws CommandException {
        if (name == null || name.isBlank()) {
            throw new CommandFormatException("Contact name cannot be blank.");
        }
        if (phone == null || phone.isBlank()) {
            throw new CommandFormatException("Phone cannot be blank.");
        }
        if (!phone.matches("\\d+")) {
            throw new CommandFormatException("Phone must contain digits only.");
        }
        if (email != null && email.isBlank()) {
            throw new CommandFormatException("Email cannot be blank.");
        }
        if (email != null && !email.matches("\\S+@\\S+")) {
            throw new CommandFormatException("Email must be a valid address, e.g. name@example.com.");
        }
        if (role != null && role.isBlank()) {
            throw new CommandFormatException("Role cannot be blank.");
        }

        this.name = name;
        this.phone = phone;
        this.email = email;
        this.role = role;
    }

    /**
     * Returns the contact's display name.
     *
     * @return Contact name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the digits-only phone number, preserving leading zeros.
     *
     * @return Phone number as entered.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Returns the email address, or {@code null} if none was supplied.
     *
     * @return Optional email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the contact's role, or {@code null} if none was supplied.
     *
     * @return Optional contact role.
     */
    public String getRole() {
        return role;
    }

    /**
     * Returns the contact in the form {@code NAME (PHONE[, EMAIL])[ - ROLE]}.
     *
     * @return User-facing description of this contact.
     */
    @Override
    public String toString() {
        String details = email == null ? phone : phone + ", " + email;
        String description = name + " (" + details + ")";
        return role == null ? description : description + " - " + role;
    }
}

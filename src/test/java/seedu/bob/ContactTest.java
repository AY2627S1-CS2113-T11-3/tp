package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.CommandFormatException;

/**
 * Verifies contact validation and the text used to display a contact.
 */
class ContactTest {

    @Test
    void toString_allFields_formatsEmailAndRole() throws CommandException {
        Contact contact = new Contact("Component Supplier", "91234567", "supplier@example.com",
                "Resistors, LEDs, wires");

        assertEquals("Component Supplier (91234567, supplier@example.com) - Resistors, LEDs, wires",
                contact.toString());
    }

    @Test
    void toString_requiredFieldsOnly_omitsEmailAndRole() throws CommandException {
        Contact contact = new Contact("NUS Infrastructure", "65161234", null, null);

        assertEquals("NUS Infrastructure (65161234)", contact.toString());
    }

    @Test
    void toString_roleWithoutEmail_showsRoleOnly() throws CommandException {
        Contact contact = new Contact("NUS Infrastructure", "65161234", null, "Aircon and lighting repairs");

        assertEquals("NUS Infrastructure (65161234) - Aircon and lighting repairs", contact.toString());
    }

    @Test
    void constructor_blankValues_exceptionThrown() {
        assertThrows(CommandFormatException.class, () -> new Contact(" ", "91234567", null, null));
        assertThrows(CommandFormatException.class, () -> new Contact(null, "91234567", null, null));
        assertThrows(CommandFormatException.class, () -> new Contact("Bob", " ", null, null));
        assertThrows(CommandFormatException.class, () -> new Contact("Bob", null, null, null));
        assertThrows(CommandFormatException.class, () -> new Contact("Bob", "91234567", "", null));
        assertThrows(CommandFormatException.class, () -> new Contact("Bob", "91234567", null, " "));
    }

    @Test
    void constructor_malformedPhoneOrEmail_exceptionThrown() {
        assertThrows(CommandFormatException.class, () -> new Contact("Bob", "9123abcd", null, null));
        assertThrows(CommandFormatException.class, () -> new Contact("Bob", "+6591234567", null, null));
        assertThrows(CommandFormatException.class, () -> new Contact("Bob", "9123 4567", null, null));
        assertThrows(CommandFormatException.class, () -> new Contact("Bob", "91234567", "not-an-email", null));
        assertThrows(CommandFormatException.class, () -> new Contact("Bob", "91234567", "a b@c.com", null));
    }
}

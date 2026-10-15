package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.bob.exception.CommandException;

/**
 * Verifies contact storage, lookup by displayed position, and the messages printed for changes.
 */
class ContactManagerTest {

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
    void addContact_nullContact_exceptionThrown() {
        ContactManager manager = new ContactManager();

        assertThrows(NullPointerException.class, () -> manager.addContact(null));
    }

    @Test
    void addContact_validContact_printsConfirmation() throws CommandException {
        ContactManager manager = new ContactManager();

        manager.addContact(new Contact("NUS Infrastructure", "65161234", null, null));

        assertEquals("Successfully added: NUS Infrastructure (65161234) to contacts\n", printed());
    }

    /**
     * Returns the console output captured so far, with Windows line endings normalized.
     */
    private String printed() {
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}

package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Exercises contact commands through the same parser used by the console application.
 */
class ParserContactTest {

    private static final String ADD_USAGE = "Invalid format. Use: add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]\n";

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final Parser parser = new Parser();
    private final Inventory inventory = new Inventory();
    private final SessionManager sessions = new SessionManager();
    private final ContactManager contacts = new ContactManager();
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
    void addContact_validCommands_confirmationShown() {
        assertEquals("Successfully added: NUS Infrastructure (65161234) to contacts\n",
                execute("add-c n/NUS Infrastructure t/65161234 r/Aircon and lighting repairs"));
        assertEquals("Successfully added: Component Supplier (91234567) to contacts\n",
                execute("add-c n/Component Supplier t/91234567 e/supplier@example.com r/Resistors, LEDs, wires"));
        assertEquals("Successfully added: Email Only (12345678) to contacts\n",
                execute("add-c n/Email Only t/12345678 e/a@b.com"));
        assertEquals("Successfully added: Bare (87654321) to contacts\n",
                execute("add-c n/Bare t/87654321"));
    }

    @Test
    void addContact_malformedArguments_usageShown() {
        String[] commands = {
            "add-c",
            "add-c n/Bob",
            "add-c t/91234567",
            "add-c t/91234567 n/Bob",
            "add-c n/Bob t/91234567 r/Role e/a@b.com",
            "add-c n/Bob t/91234567 x/1",
            "add-c n/Bob t/91234567 t/91234567",
            "add-c extra n/Bob t/91234567"
        };
        for (String command : commands) {
            assertEquals(ADD_USAGE, execute(command), command);
        }
    }

    @Test
    void addContact_invalidValues_errorShown() {
        assertEquals("Contact name cannot be blank.\n", execute("add-c n/ t/91234567"));
        assertEquals("Phone cannot be blank.\n", execute("add-c n/Bob t/"));
        assertEquals("Phone must contain digits only.\n", execute("add-c n/Bob t/abc"));
        assertEquals("Phone must contain digits only.\n", execute("add-c n/Bob t/9123 4567"));
        assertEquals("Email cannot be blank.\n", execute("add-c n/Bob t/91234567 e/"));
        assertEquals("Email must be a valid address, e.g. name@example.com.\n",
                execute("add-c n/Bob t/91234567 e/nope"));
        assertEquals("Role cannot be blank.\n", execute("add-c n/Bob t/91234567 r/"));
    }

    @Test
    void contactCommands_wrongCommandWord_treatedAsUnknown() {
        assertEquals("Invalid command\n", execute("ADD-C n/Bob t/91234567"));
        assertEquals("Invalid command\n", execute("add-contact n/Bob t/91234567"));
    }

    /**
     * Executes one command and returns only that command's normalized console output.
     */
    private String execute(String command) {
        output.reset();
        parser.handleCommand(command, inventory, sessions, contacts);
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}

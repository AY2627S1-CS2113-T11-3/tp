package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
//@@author DaynaG3
import java.nio.file.Path;
import java.util.Scanner;
//@@author

import org.junit.jupiter.api.Test;
//@@author DaynaG3
import org.junit.jupiter.api.io.TempDir;
//@@author

/**
 * Verifies that the application handles closed standard input without crashing.
 */
class BobTest {

    //@@author DaynaG3
    @TempDir
    private Path directory;
    //@@author

    @Test
    void main_endOfInputBeforeName_exitsCleanly() {
        String output = assertDoesNotThrow(() -> runApplication(""));

        assertTrue(output.contains("What is your name?"));
        assertFalse(output.contains("welcome to Bob!"));
    }

    @Test
    void main_endOfInputAfterName_exitsCleanly() {
        String output = assertDoesNotThrow(() -> runApplication("Alex\n"));

        assertTrue(output.contains("Hello Alex, welcome to Bob!"));
    }

    /**
     * Runs the application with isolated standard input and captures standard output.
     *
     * @param input Complete text supplied through standard input.
     * @return Text printed by the application.
     */
    private String runApplication(String input) {
        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(capturedOutput);
            //@@author DaynaG3
            Bob.run(new Scanner(System.in), new Storage(directory.resolve("bob.json")));
            //@@author
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }

        return output.toString(StandardCharsets.UTF_8);
    }
}

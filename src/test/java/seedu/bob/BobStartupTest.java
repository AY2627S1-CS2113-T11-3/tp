//@@author DaynaG3
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verifies startup loading and safe refusal before accepting commands when saved data is unusable. */
class BobStartupTest {

    @TempDir
    private Path directory;

    @Test
    void run_existingSnapshot_loadsBeforeCommandsWithoutAdditionConfirmations() throws Exception {
        Path main = directory.resolve("bob.json");
        String saved = Files.readString(Path.of("docs/examples/bob-v1.json"));
        Files.writeString(main, saved);

        SessionResult result = run("Alex\nlist-i\nlist-r\nlist-c\nlist-l\nBye\n");

        assertEquals(0, result.exitCode());
        assertTrue(result.output().contains("Beaker (Qty: 7)"));
        assertTrue(result.output().contains("[Fulfilled] Alex Tan"));
        assertTrue(result.output().contains("Example Maintenance (07654321)"));
        assertTrue(result.output().contains("3x Beaker [Glassware] to Taylor Wong"));
        assertFalse(result.output().contains("Successfully added"));
        assertEquals(saved, Files.readString(main));
    }

    @Test
    void run_backupRecovery_reportsRecoveryAndPreservesBackup() throws Exception {
        String backup = Files.readString(Path.of("docs/examples/bob-v1.json"));
        Files.writeString(directory.resolve("bob.json"), "broken main");
        Files.writeString(directory.resolve("bob.json.bak"), backup);

        SessionResult result = run("Alex\nlist-i\nBye\n");

        assertEquals(0, result.exitCode());
        assertTrue(result.output().contains("Recovered data from backup. It may not include the latest changes."));
        assertTrue(result.output().contains("Beaker (Qty: 7)"));
        assertEquals(backup, Files.readString(directory.resolve("bob.json.bak")));
    }

    @Test
    void run_corruptSavedData_refusesCommandsAndPreservesFile() throws Exception {
        Files.writeString(directory.resolve("bob.json"), "broken main");

        SessionResult result = run("Alex\nadd-i n/Beaker c/Glassware q/10\nBye\n");

        assertEquals(1, result.exitCode());
        assertFalse(result.output().contains("What is your name?"));
        assertTrue(result.output().contains("Bob has stopped; saved files were not changed."));
        assertEquals("broken main", Files.readString(directory.resolve("bob.json")));
        assertFalse(Files.exists(directory.resolve("bob.json.bak")));
    }

    @Test
    void run_newerMainAndValidBackup_refusesToDowngrade() throws Exception {
        String newer = "{\"schemaVersion\":2}";
        Files.writeString(directory.resolve("bob.json"), newer);
        Files.writeString(directory.resolve("bob.json.bak"), Files.readString(Path.of("docs/examples/bob-v1.json")));

        SessionResult result = run("Alex\nBye\n");

        assertEquals(1, result.exitCode());
        assertTrue(result.output().contains("Unsupported data version: 2."));
        assertFalse(result.output().contains("Recovered data"));
        assertEquals(newer, Files.readString(directory.resolve("bob.json")));
    }

    @Test
    void run_newReadOnlySession_doesNotCreateSaveFiles() {
        SessionResult result = run("Alex\nlist-i\nBye\n");

        assertEquals(0, result.exitCode());
        assertTrue(result.output().contains("No items in inventory."));
        assertFalse(Files.exists(directory.resolve("bob.json")));
    }

    /** Runs against temporary storage and captures only the user-facing stdout stream. */
    private SessionResult run(String input) {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream captured = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(captured);
            int exitCode = Bob.run(new Scanner(input), new Storage(directory.resolve("bob.json")));
            return new SessionResult(exitCode, output.toString(StandardCharsets.UTF_8));
        } finally {
            System.setOut(original);
        }
    }

    /** Captured process result without terminating the test JVM on startup failure. */
    private record SessionResult(int exitCode, String output) {
    }
}

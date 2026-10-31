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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.bob.exception.StorageException;

/** Verifies automatic saving, complete restart workflows, and explicit handling of unsaved changes. */
class BobPersistenceTest {

    @TempDir
    private Path directory;

    @Test
    void run_everyMutation_savesCompleteStateIncludingAllDeletionTypes() throws Exception {
        RecordingStorage storage = new RecordingStorage(directory.resolve("bob.json"), 0);
        String date = LocalDate.now().plusYears(1)
                .format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH));
        List<String> commands = List.of(
                "add-i n/Beaker c/Glassware q/10",
                "add-s n/Chemistry d/" + date + " l/Lab 1 s/0900 e/1100 p/10",
                "add-p s/1 i/Beaker c/Glassware q/2",
                "delete-p s/1 i/1 q/7",
                "add-r n/Alex i/Beaker q/2 d/" + date,
                "mark-r 1 prepared", "mark-r 1 fulfilled",
                "add-c n/Supplier t/01234567",
                "add-l n/Beaker c/Glassware q/3 b/Taylor t/02345678 d/" + date,
                "return 1",
                "add-l n/Beaker c/Glassware q/2 b/Taylor t/02345678 d/" + date,
                "delete-l 1", "delete-c 1", "delete-r 1", "delete-s 1", "delete-i c/Glassware i/1 q/10");

        SessionResult result = run("Alex\n" + String.join("\n", commands) + "\nBye\n", storage);

        assertEquals(0, result.exitCode());
        assertEquals(commands.size(), storage.attempts);
        assertEquals(new AppState().toSnapshot(), storage.load().snapshot());
        AppSnapshot loanSnapshot = storage.saved.get(8);
        assertEquals(7, loanSnapshot.inventory().getFirst().items().getFirst().quantity());
        assertEquals(3, loanSnapshot.loans().getFirst().quantity());
        assertEquals(13, loanSnapshot.sessions().getFirst().preparation().getFirst().quantity());
        assertEquals("fulfilled", loanSnapshot.requests().getFirst().status());
    }

    @Test
    void run_restart_preservesLoanStockAndRequestStatus() throws Exception {
        RecordingStorage storage = new RecordingStorage(directory.resolve("bob.json"), 0);
        String date = LocalDate.now().plusYears(1)
                .format(DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH));
        SessionResult initial = run("Alex\nadd-i n/Beaker c/Glassware q/10\n"
                + "add-r n/Jamie i/Beaker q/2 d/" + date + "\nmark-r 1 prepared\n"
                + "add-l n/Beaker c/Glassware q/3 b/Taylor t/02345678 d/" + date + "\n", storage);
        SessionResult restarted = run("Alex\nlist-i\nlist-r\nreturn 1\nBye\n", storage);

        assertEquals(0, initial.exitCode());
        assertEquals(0, restarted.exitCode());
        assertTrue(restarted.output().contains("Beaker (Qty: 7)"));
        assertTrue(restarted.output().contains("[Prepared] Jamie"));
        assertEquals(10, storage.load().snapshot().inventory().getFirst().items().getFirst().quantity());
        assertTrue(storage.load().snapshot().loans().isEmpty());
    }

    @Test
    void run_readOnlyRejectedAndUnchangedCommands_neverWrites() throws Exception {
        Path main = directory.resolve("bob.json");
        String original = Files.readString(Path.of("docs/examples/bob-v1.json"));
        Files.writeString(main, original);
        RecordingStorage storage = new RecordingStorage(main, 0);

        SessionResult result = run("Alex\nlist-i\nadd-i n/Beaker c/Glassware q/1\nmark-r 3 prepared\nBye\n", storage);

        assertEquals(0, result.exitCode());
        assertEquals(0, storage.attempts);
        assertEquals(original, Files.readString(main));
        assertFalse(Files.exists(directory.resolve("bob.json.bak")));
    }

    @Test
    void run_saveFailsThenNextChangeSucceeds_savesLatestCompleteState() throws Exception {
        RecordingStorage storage = new RecordingStorage(directory.resolve("bob.json"), 1);

        SessionResult result = run("Alex\nadd-i n/Beaker c/Glassware q/10\nadd-c n/Supplier t/01234567\nBye\n",
                storage);

        assertEquals(0, result.exitCode());
        assertEquals(2, storage.attempts);
        assertTrue(result.output().contains("Warning: changes are only in memory."));
        assertEquals(10, storage.load().snapshot().inventory().getFirst().items().getFirst().quantity());
        assertEquals("Supplier", storage.load().snapshot().contacts().getFirst().name());
    }

    @Test
    void run_exitSaveFailsThenRetrySucceeds_commitsBeforeExiting() throws Exception {
        RecordingStorage storage = new RecordingStorage(directory.resolve("bob.json"), 2);

        SessionResult result = run("Alex\nadd-i n/Beaker c/Glassware q/10\nBye\nretry\n", storage);

        assertEquals(0, result.exitCode());
        assertEquals(3, storage.attempts);
        assertTrue(result.output().contains("Unsaved changes remain. Enter retry, cancel, or exit:"));
        assertEquals(10, storage.load().snapshot().inventory().getFirst().items().getFirst().quantity());
    }

    @Test
    void run_cancelExitThenChange_preservesUnsavedStateUntilSuccessfulSave() throws Exception {
        RecordingStorage storage = new RecordingStorage(directory.resolve("bob.json"), 2);

        SessionResult result = run("Alex\nadd-i n/Beaker c/Glassware q/10\nBye\ncancel\n"
                + "list-i\nadd-c n/Supplier t/01234567\nBye\n", storage);

        assertEquals(0, result.exitCode());
        assertEquals(3, storage.attempts);
        assertTrue(result.output().contains("Exit canceled. Unsaved changes remain in memory."));
        assertTrue(result.output().contains("Beaker (Qty: 10)"));
        assertEquals(1, storage.load().snapshot().contacts().size());
    }

    @Test
    void run_persistentSaveFailureAndExplicitExit_reportsUnsavedExit() {
        RecordingStorage storage = new RecordingStorage(directory.resolve("bob.json"), -1);

        SessionResult result = run("Alex\nadd-i n/Beaker c/Glassware q/10\nBye\ninvalid\nexit\n", storage);

        assertEquals(1, result.exitCode());
        assertEquals(2, storage.attempts);
        assertTrue(result.output().contains("Choose retry, cancel, or exit."));
        assertTrue(result.output().contains("Exiting with unsaved changes."));
        assertFalse(Files.exists(storage.getPath()));
    }

    @Test
    void run_eofDuringFailureOrExitPrompt_reportsFailureWithoutWaiting() {
        for (String ending : List.of("", "Bye\n")) {
            RecordingStorage storage = new RecordingStorage(directory.resolve("bob.json"), -1);

            SessionResult result = run("Alex\nadd-i n/Beaker c/Glassware q/10\n" + ending, storage);

            assertEquals(1, result.exitCode());
            assertEquals(2, storage.attempts);
            assertTrue(result.output().contains("Input ended with unsaved changes. The last saved data was kept."));
            assertFalse(Files.exists(storage.getPath()));
        }
    }

    @Test
    void run_recoveredBackup_savesRepairOnExitWithoutChangingBackup() throws Exception {
        Path main = directory.resolve("bob.json");
        String backup = Files.readString(Path.of("docs/examples/bob-v1.json"));
        Files.writeString(main, "broken main");
        Files.writeString(directory.resolve("bob.json.bak"), backup);
        RecordingStorage storage = new RecordingStorage(main, 0);

        SessionResult result = run("Alex\nBye\n", storage);

        assertEquals(0, result.exitCode());
        assertEquals(1, storage.attempts);
        assertEquals(LoadResult.Source.MAIN, storage.load().source());
        assertEquals(backup, Files.readString(directory.resolve("bob.json.bak")));
    }

    /** Captures user-facing output while keeping every run on explicitly selected temporary storage. */
    private SessionResult run(String input, Storage storage) {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream captured = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(captured);
            int exitCode = Bob.run(new Scanner(input), storage);
            return new SessionResult(exitCode, output.toString(StandardCharsets.UTF_8));
        } finally {
            System.setOut(original);
        }
    }

    /** Result of one application run without calling System.exit in the test JVM. */
    private record SessionResult(int exitCode, String output) {
    }

    /** Records save attempts and injects transient or persistent failures without platform permission assumptions. */
    private static class RecordingStorage extends Storage {
        private int attempts;
        private int remainingFailures;
        private final List<AppSnapshot> saved = new ArrayList<>();

        RecordingStorage(Path path, int remainingFailures) {
            super(path);
            this.remainingFailures = remainingFailures;
        }

        @Override
        public void save(AppSnapshot snapshot) throws StorageException {
            attempts++;
            if (remainingFailures != 0) {
                if (remainingFailures > 0) {
                    remainingFailures--;
                }
                throw new StorageException(StorageException.Kind.IO_ERROR, "The snapshot could not be saved.", null);
            }
            super.save(snapshot);
            saved.add(snapshot);
        }
    }
}

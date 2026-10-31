//@@author DaynaG3
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.StorageException;

/** Exercises strict ledger storage, non-writing migration, and identity preservation through restarts. */
class LoanHistoryStorageTest {

    @TempDir
    private Path directory;

    @Test
    void roundTrip_mixedHistory_preservesClosuresAndRestoresOnlyActiveStock() throws Exception {
        Storage storage = new Storage(directory.resolve("bob.json"));
        AppSnapshot snapshot = SnapshotCodec.decode(example("bob-v2.json"));
        storage.save(snapshot);
        AppState state = AppState.restore(storage.load().snapshot());

        assertEquals(snapshot, state.toSnapshot());
        assertEquals(7, state.getInventory().getAvailableQuantity("Meter", "Equipment"));
        assertEquals(1, state.getLoanManager().getLoans().size());
        state.getLoanManager().returnLoan(1);
        assertEquals(9, state.getInventory().getAvailableQuantity("Meter", "Equipment"));
        assertThrows(CommandException.class, () -> state.getLoanManager().returnLoan(1));
        state.getLoanManager().addLoan(loan());
        storage.save(state.toSnapshot());
        AppState restarted = AppState.restore(storage.load().snapshot());

        assertEquals(List.of(1L, 2L, 3L, 4L),
                restarted.getLoanManager().getLoanHistory().stream().map(LoanRecord::loanId).toList());
        assertEquals(LoanRecord.Status.RETURNED, restarted.getLoanManager().getLoanHistory().get(2).status());
        assertEquals("ACTIVE", snapshot.loanRecords().get(2).status());
        assertEquals(8, restarted.getInventory().getAvailableQuantity("Meter", "Equipment"));
        assertEquals(5, restarted.toSnapshot().nextLoanId());
    }

    @Test
    void load_versionOne_keepsFilesUntouchedAndPreservesLegacyBackupOnFirstChange() throws Exception {
        Path main = directory.resolve("bob.json");
        String original = example("bob-v1.json");
        Files.writeString(main, original);
        Storage storage = new Storage(main);
        AppState state = AppState.restore(storage.load().snapshot());

        assertEquals(2, state.toSnapshot().schemaVersion());
        assertEquals(List.of(1L, 2L, 3L),
                state.getLoanManager().getLoanHistory().stream().map(LoanRecord::loanId).toList());
        assertTrue(state.getLoanManager().getLoanHistory().stream().allMatch(record -> record.recordedAt() == null));
        assertEquals(original, Files.readString(main));
        assertFalse(Files.exists(directory.resolve("bob.json.bak")));
        assertEquals(7, state.getInventory().getAvailableQuantity("Beaker", "Glassware"));
        state.getLoanManager().returnLoan(1);
        storage.save(state.toSnapshot());

        assertEquals(original, Files.readString(directory.resolve("bob.json.bak")));
        JsonObject saved = JsonParser.parseString(Files.readString(main)).getAsJsonObject();
        assertEquals(2, saved.get("schemaVersion").getAsInt());
        LoanRecord returned = AppState.restore(storage.load().snapshot()).getLoanManager().getLoanHistory().getFirst();
        assertEquals(LoanRecord.Status.RETURNED, returned.status());
        assertNull(returned.recordedAt());
        assertEquals(10, state.getInventory().getAvailableQuantity("Beaker", "Glassware"));
    }

    @Test
    void restore_largeId_keepsExactLongValueAndRejectsExhaustionWithoutStockChanges() throws Exception {
        JsonObject document = JsonParser.parseString(example("bob-v2.json")).getAsJsonObject();
        long id = 9_007_199_254_740_993L;
        document.addProperty("nextLoanId", id);
        AppState state = AppState.restore(SnapshotCodec.decode(document.toString()));
        state.getLoanManager().addLoan(loan());
        AppSnapshot restored = SnapshotCodec.decode(SnapshotCodec.encode(state.toSnapshot()));

        assertEquals(id, restored.loanRecords().getLast().loanId());
        assertEquals(id + 1, restored.nextLoanId());
        LoanManager exhausted = new LoanManager(state.getInventory());
        exhausted.restoreRecords(List.of(), Long.MAX_VALUE);
        int stock = state.getInventory().getAvailableQuantity("Meter", "Equipment");
        assertThrows(CommandException.class, () -> exhausted.addLoan(loan()));
        assertEquals(stock, state.getInventory().getAvailableQuantity("Meter", "Equipment"));
        assertTrue(exhausted.getLoanHistory().isEmpty());
    }

    @Test
    void decode_invalidLedger_rejectsInconsistentStatesIdsTypesAndTimestamps() throws Exception {
        List<Consumer<JsonObject>> mutations = List.of(
                root -> first(root).addProperty("loanId", 0),
                root -> first(root).addProperty("loanId", "1"),
                root -> first(root).addProperty("loanId", 1.5),
                root -> first(root).add("loanId", JsonParser.parseString("9223372036854775808")),
                root -> root.getAsJsonArray("loanRecords").add(first(root).deepCopy()),
                root -> root.addProperty("nextLoanId", 3),
                root -> root.addProperty("nextLoanId", -1),
                root -> root.addProperty("nextLoanId", "4"),
                root -> root.remove("nextLoanId"),
                root -> root.remove("brokenItems"),
                root -> root.add("loanRecords", JsonNull.INSTANCE),
                root -> first(root).addProperty("status", "returned"),
                root -> first(root).addProperty("status", "ACTIVE"),
                root -> first(root).add("closedAt", JsonNull.INSTANCE),
                root -> first(root).addProperty("recordedAt", "2020-02-30T09:00:00+08:00"),
                root -> first(root).addProperty("recordedAt", true),
                root -> first(root).remove("recordedAt"),
                root -> first(root).getAsJsonObject("loan").addProperty("quantity", 0),
                root -> root.addProperty("extra", "unexpected"));
        for (Consumer<JsonObject> mutation : mutations) {
            JsonObject document = JsonParser.parseString(example("bob-v2.json")).getAsJsonObject();
            mutation.accept(document);

            StorageException failure = assertThrows(StorageException.class,
                    () -> SnapshotCodec.decode(document.toString()), document.toString());

            assertEquals(StorageException.Kind.INVALID_DATA, failure.getKind());
        }
    }

    @Test
    void load_corruptMainWithLedgerBackup_recoversEntireHistoryWithoutWriting() throws Exception {
        Path main = directory.resolve("bob.json");
        Path backup = directory.resolve("bob.json.bak");
        String original = example("bob-v2.json");
        Files.writeString(main, "broken");
        Files.writeString(backup, original);

        LoadResult loaded = new Storage(main).load();

        assertEquals(LoadResult.Source.BACKUP, loaded.source());
        assertEquals(3, loaded.snapshot().loanRecords().size());
        assertEquals(4, loaded.snapshot().nextLoanId());
        assertEquals("broken", Files.readString(main));
        assertEquals(original, Files.readString(backup));
    }

    /** Selects the first ledger entry for independent corruption cases. */
    private JsonObject first(JsonObject document) {
        return document.getAsJsonArray("loanRecords").get(0).getAsJsonObject();
    }

    /** Reads a tracked storage fixture independently of application runtime files. */
    private String example(String name) throws Exception {
        return Files.readString(Path.of("docs/examples", name));
    }

    /** Creates a small loan to exercise the next persisted identity. */
    private Loan loan() throws CommandException {
        return new Loan("Meter", "Equipment", 1,
                new Contact("Jamie", "01234567", null, null), "31 December 2099");
    }
}

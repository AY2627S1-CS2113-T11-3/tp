//@@author DaynaG3
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.bob.exception.StorageException;

/** Verifies strict JSON validation, backup recovery, and failures that must preserve valid saved data. */
class StorageTest {

    @TempDir
    private Path directory;

    @Test
    void load_missingFiles_returnsEmptyWithoutCreatingDirectory() throws Exception {
        Storage storage = new Storage(directory.resolve("data/bob.json"));

        LoadResult result = storage.load();

        assertEquals(LoadResult.Source.NEW, result.source());
        assertEquals(new AppState().toSnapshot(), result.snapshot());
        assertFalse(Files.exists(directory.resolve("data")));
    }

    @Test
    void save_documentedExamples_roundTripsAllFeaturesAndExplicitNulls() throws Exception {
        Storage storage = new Storage(directory.resolve("data/bob.json"));
        AppSnapshot populated = SnapshotCodec.decode(example("bob-v1.json"));
        AppSnapshot empty = SnapshotCodec.decode(example("bob-v1-empty.json"));

        storage.save(populated);
        assertEquals(populated, storage.load().snapshot());
        assertTrue(Files.readString(storage.getPath()).contains("\"email\": null"));
        storage.save(empty);

        assertEquals(empty, storage.load().snapshot());
        assertEquals(populated, SnapshotCodec.decode(Files.readString(directory.resolve("data/bob.json.bak"))));
    }

    @Test
    void decode_wrongShapeTypesAndMalformedJson_rejectsWithoutCoercion() throws Exception {
        String empty = example("bob-v1-empty.json");
        String populated = example("bob-v1.json");
        List<String> invalid = List.of(
                "", "null", "[]", "{}", empty + "{}", "/* comment */" + empty,
                empty.replace("\"schemaVersion\": 1", "\"schemaVersion\": 1, \"schemaVersion\": 1"),
                empty.replace("\"schemaVersion\": 1", "\"schemaVersion\": \"1\""),
                empty.replace("\"schemaVersion\": 1", "\"schemaVersion\": 1.0"),
                empty.replace("\"schemaVersion\": 1", "\"schemaVersion\": 2147483648"),
                empty.replace("\"inventory\": []", "\"inventory\": null"),
                empty.replace("\"inventory\": []", "\"inventory\": [null]"),
                empty.replace("\"loans\": []", "\"loans\": [], \"extra\": []"),
                populated.replace("\"quantity\": 7", "\"quantity\": \"7\""),
                populated.replace("\"quantity\": 7", "\"quantity\": 7.5"),
                populated.replace("\"quantity\": 7", "\"quantity\": 0"),
                populated.replace("\"phone\": \"01234567\"", "\"phone\": 1234567"),
                populated.replace("\"role\": null", "\"role\": true"),
                populated.replace("\"role\": null", "\"unknown\": null"),
                populated.replace("\"deadline\": \"2020-01-15\"", "\"deadline\": \"2020-02-30\""),
                populated.replace("\"status\": \"prepared\"", "\"status\": \"invalid\""),
                populated.replace("\"headcount\": 10", "\"headcount\": false"),
                populated.replace("\"preparation\": []", "\"preparation\": null"),
                populated.replace("\"name\": \"Beaker\", \"quantity\": 7",
                        "\"name\": \"Beaker\", \"name\": \"Other\", \"quantity\": 7"));
        for (String json : invalid) {
            StorageException failure = assertThrows(StorageException.class, () -> SnapshotCodec.decode(json));
            assertEquals(StorageException.Kind.INVALID_DATA, failure.getKind());
        }
    }

    @Test
    void decode_newVersion_protectsItEvenWithDifferentFields() {
        StorageException failure = assertThrows(StorageException.class,
                () -> SnapshotCodec.decode("{\"schemaVersion\":3,\"newFormat\":99999999999999999999.5}"));

        assertEquals(StorageException.Kind.UNSUPPORTED_VERSION, failure.getKind());
    }

    @Test
    void encode_escapedUnicodeTextAndPhones_preservesExactValues() throws Exception {
        AppSnapshot snapshot = new AppSnapshot(AppSnapshot.CURRENT_VERSION, List.of(), List.of(), List.of(),
                List.of(new AppSnapshot.ContactEntry("Example \"Lab\" \\ supplies \u65b0", "01234567", null,
                        "First line\nSecond line")), List.of());

        assertEquals(snapshot, SnapshotCodec.decode(SnapshotCodec.encode(snapshot)));
    }

    @Test
    void load_corruptOrMissingMain_recoversBackupWithoutWritingEitherFile() throws Exception {
        Path main = directory.resolve("bob.json");
        Path backup = directory.resolve("bob.json.bak");
        String valid = example("bob-v1.json");
        Files.writeString(backup, valid);
        Storage storage = new Storage(main);

        assertEquals(LoadResult.Source.BACKUP, storage.load().source());
        assertFalse(Files.exists(main));
        Files.writeString(main, "broken");
        assertEquals(LoadResult.Source.BACKUP, storage.load().source());
        assertEquals("broken", Files.readString(main));
        assertEquals(valid, Files.readString(backup));

        storage.save(storage.load().snapshot());
        assertEquals(LoadResult.Source.MAIN, storage.load().source());
        assertEquals(valid, Files.readString(backup));
    }

    @Test
    void load_mainValid_ignoresInvalidBackup() throws Exception {
        Files.writeString(directory.resolve("bob.json"), example("bob-v1.json"));
        Files.writeString(directory.resolve("bob.json.bak"), "invalid");

        assertEquals(LoadResult.Source.MAIN, new Storage(directory.resolve("bob.json")).load().source());
    }

    @Test
    void load_unreadableMain_recoversValidBackup() throws Exception {
        Files.createDirectory(directory.resolve("bob.json"));
        Files.writeString(directory.resolve("bob.json.bak"), example("bob-v1-empty.json"));

        assertEquals(LoadResult.Source.BACKUP, new Storage(directory.resolve("bob.json")).load().source());
    }

    @Test
    void load_noUsableSnapshot_failsWithoutOverwritingFiles() throws Exception {
        Path main = directory.resolve("bob.json");
        Path backup = directory.resolve("bob.json.bak");
        Storage storage = new Storage(main);
        Files.writeString(main, "broken main");
        Files.writeString(backup, "broken backup");

        assertThrows(StorageException.class, storage::load);
        assertEquals("broken main", Files.readString(main));
        assertEquals("broken backup", Files.readString(backup));
        Files.delete(main);
        assertThrows(StorageException.class, storage::load);
        assertFalse(Files.exists(main));
    }

    @Test
    void loadAndSave_newerMain_neverFallsBackOrOverwritesIt() throws Exception {
        Path main = directory.resolve("bob.json");
        String newer = "{\"schemaVersion\":3,\"newFormat\":true}";
        Files.writeString(main, newer);
        Files.writeString(directory.resolve("bob.json.bak"), example("bob-v1-empty.json"));
        Storage storage = new Storage(main);

        assertEquals(StorageException.Kind.UNSUPPORTED_VERSION,
                assertThrows(StorageException.class, storage::load).getKind());
        assertEquals(StorageException.Kind.UNSUPPORTED_VERSION,
                assertThrows(StorageException.class, () -> storage.save(new AppState().toSnapshot())).getKind());
        assertEquals(newer, Files.readString(main));
        assertEquals(2, countFiles());
    }

    @Test
    void save_atomicMoveUnavailable_keepsLastMainAndBackupUsable() throws Exception {
        Path main = directory.resolve("bob.json");
        String previous = example("bob-v1.json");
        Files.writeString(main, previous);
        Storage storage = new FailingStorage(main, true);

        assertThrows(StorageException.class, () -> storage.save(new AppState().toSnapshot()));

        assertEquals(previous, Files.readString(main));
        assertEquals(2, countFiles());
        assertEquals(SnapshotCodec.decode(previous), storage.load().snapshot());
    }

    @Test
    void save_backupMoveFails_keepsMainAndPreviousBackupUnchanged() throws Exception {
        Path main = directory.resolve("bob.json");
        String previous = example("bob-v1.json");
        String backup = example("bob-v1-empty.json");
        Files.writeString(main, previous);
        Files.writeString(directory.resolve("bob.json.bak"), backup);

        assertThrows(StorageException.class,
                () -> new FailingStorage(main, false).save(new AppState().toSnapshot()));

        assertEquals(previous, Files.readString(main));
        assertEquals(backup, Files.readString(directory.resolve("bob.json.bak")));
        assertEquals(2, countFiles());
    }

    @Test
    void save_invalidSnapshotOrBlockedDirectory_neverCreatesPartialMain() throws Exception {
        Path main = directory.resolve("data/bob.json");
        Storage storage = new Storage(main);
        AppSnapshot invalid = new AppSnapshot(AppSnapshot.CURRENT_VERSION, List.of(), List.of(), List.of(),
                List.of(new AppSnapshot.ContactEntry("Contact", "bad phone", null, null)), List.of());

        assertThrows(StorageException.class, () -> storage.save(invalid));
        assertFalse(Files.exists(directory.resolve("data")));
        Files.writeString(directory.resolve("data"), "blocks directory creation");
        assertEquals(StorageException.Kind.IO_ERROR,
                assertThrows(StorageException.class, () -> storage.save(new AppState().toSnapshot())).getKind());
        assertEquals("blocks directory creation", Files.readString(directory.resolve("data")));
    }

    @Test
    void load_abandonedTemporaryFile_doesNotPromoteIt() throws Exception {
        Files.writeString(directory.resolve("bob-abandoned.tmp"), example("bob-v1.json"));

        assertEquals(LoadResult.Source.NEW, new Storage(directory.resolve("bob.json")).load().source());
        assertEquals(1, countFiles());
    }

    @Test
    void save_firstAtomicMoveFails_leavesNoPartialSaveOrTemporaryFile() throws Exception {
        Storage storage = new FailingStorage(directory.resolve("bob.json"), true);

        assertThrows(StorageException.class, () -> storage.save(new AppState().toSnapshot()));

        assertEquals(0, countFiles());
        assertEquals(LoadResult.Source.NEW, storage.load().source());
    }

    @Test
    void decode_excessiveNesting_rejectsBeforeRecursiveOverflow() {
        String deeplyNested = "[".repeat(100) + "0" + "]".repeat(100);

        assertEquals(StorageException.Kind.INVALID_DATA,
                assertThrows(StorageException.class, () -> SnapshotCodec.decode(deeplyNested)).getKind());
    }

    /** Reads tracked fictional fixtures; tests write only under their JUnit temporary directory. */
    private static String example(String name) throws IOException {
        return Files.readString(Path.of("docs/examples", name));
    }

    private long countFiles() throws IOException {
        try (var files = Files.list(directory)) {
            return files.count();
        }
    }

    /** Simulates either a failed backup update or an unsupported main-file atomic move. */
    private static class FailingStorage extends Storage {
        private final boolean failsMain;

        FailingStorage(Path path, boolean failsMain) {
            super(path);
            this.failsMain = failsMain;
        }

        @Override
        protected void moveAtomically(Path source, Path target) throws IOException {
            if (target.equals(getPath()) == failsMain) {
                throw new AtomicMoveNotSupportedException(source.toString(), target.toString(), "Simulated failure");
            }
            super.moveAtomically(source, target);
        }
    }
}

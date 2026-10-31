//@@author DaynaG3
package seedu.bob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import seedu.bob.exception.StorageException;

/** Verifies persistence across the upstream broken-item and request-fulfillment integrations. */
class StorageMergeRegressionTest {

    @TempDir
    private Path directory;

    @Test
    void restore_brokenUnitsWithoutAvailableStock_restoresReportAndReturnsUnitsOnlyOnce() throws Exception {
        AppState state = createReportedState(2, null);
        Storage storage = new Storage(directory.resolve("bob.json"));
        storage.save(state.toSnapshot());

        AppState restored = AppState.restore(storage.load().snapshot());

        assertEquals(0, restored.getInventory().getAvailableQuantity("Meter", "Equipment"));
        assertEquals(1, restored.getBrokenItemManager().getBrokenItems().size());
        restored.getBrokenItemManager().deleteBrokenItem(1);
        assertEquals(2, restored.getInventory().getAvailableQuantity("Meter", "Equipment"));
        assertTrue(restored.getBrokenItemManager().getBrokenItems().isEmpty());
    }

    @Test
    void restore_duplicateContactDetails_keepsTheSelectedContactIdentity() throws Exception {
        AppState state = new AppState();
        Contact first = new Contact("Maintenance", "01234567", null, null);
        Contact selected = new Contact("Maintenance", "01234567", null, null);
        state.getContactManager().addContact(first);
        state.getContactManager().addContact(selected);
        addReport(state, 5, selected);

        AppState restored = roundTrip(state);
        BrokenItem report = restored.getBrokenItemManager().getBrokenItems().getFirst();

        assertSame(restored.getContactManager().getContact(2), report.getContact());
        restored.getContactManager().deleteContact(1);
        assertSame(restored.getContactManager().getContact(1),
                restored.getBrokenItemManager().getAvailableContact(report));
        assertEquals(3, restored.getInventory().getAvailableQuantity("Meter", "Equipment"));
    }

    @Test
    void restore_deletedContactWithIdenticalReplacement_doesNotReconnectReport() throws Exception {
        AppState state = new AppState();
        Contact selected = new Contact("Maintenance", "01234567", null, "Repair");
        state.getContactManager().addContact(selected);
        addReport(state, 5, selected);
        state.getContactManager().deleteContact(1);
        state.getContactManager().addContact(new Contact("Maintenance", "01234567", null, "Repair"));

        AppState restored = roundTrip(state);
        BrokenItem report = restored.getBrokenItemManager().getBrokenItems().getFirst();

        assertNotNull(report.getContact());
        assertNull(restored.getBrokenItemManager().getAvailableContact(report));
        assertEquals(state.toSnapshot(), restored.toSnapshot());
        restored.getBrokenItemManager().deleteBrokenItem(1);
        assertEquals(5, restored.getInventory().getAvailableQuantity("Meter", "Equipment"));
    }

    @Test
    void decode_originalVersionOneFiles_defaultsToNoBrokenReports() throws Exception {
        AppSnapshot snapshot = SnapshotCodec.decode(Files.readString(Path.of("docs/examples/bob-v1.json")));

        assertTrue(snapshot.brokenItems().isEmpty());
        assertEquals(snapshot, SnapshotCodec.decode(SnapshotCodec.encode(snapshot)));
    }

    @Test
    void decode_invalidReportTypesAndValues_rejectsEntireSnapshot() throws Exception {
        AppState state = new AppState();
        Contact contact = new Contact("Maintenance", "01234567", null, null);
        state.getContactManager().addContact(contact);
        addReport(state, 5, contact);
        String original = SnapshotCodec.encode(state.toSnapshot());
        for (String field : List.of("quantity", "contactIndex", "reportedDate", "deletedContact")) {
            JsonObject document = JsonParser.parseString(original).getAsJsonObject();
            JsonObject report = document.getAsJsonArray("brokenItems").get(0).getAsJsonObject();
            switch (field) {
                case "quantity" -> report.addProperty(field, 0);
                case "contactIndex" -> report.addProperty(field, "1");
                case "reportedDate" -> report.addProperty(field, "2020-02-30");
                case "deletedContact" -> report.add(field, document.getAsJsonArray("contacts").get(0));
                default -> throw new AssertionError("Unexpected fixture field: " + field);
            }

            StorageException exception = assertThrows(StorageException.class,
                    () -> SnapshotCodec.decode(document.toString()), field);

            assertEquals(StorageException.Kind.INVALID_DATA, exception.getKind(), field);
        }
    }

    @Test
    void restore_outOfRangeReportContact_rejectsSnapshot() throws Exception {
        AppSnapshot original = createReportedState(5, null).toSnapshot();
        AppSnapshot invalid = new AppSnapshot(original.schemaVersion(), original.inventory(), original.sessions(),
                original.requests(), original.contacts(), original.loans(), List.of(new AppSnapshot.BrokenItemEntry(
                        "Meter", "Equipment", 2, "Cracked screen", "2020-01-01", 1, null)));

        assertThrows(StorageException.class, () -> SnapshotCodec.encode(invalid));
    }

    /** Builds a small report fixture whose inventory already excludes the two reported units. */
    private AppState createReportedState(int availableBeforeReport, Contact contact) throws Exception {
        AppState state = new AppState();
        addReport(state, availableBeforeReport, contact);
        return state;
    }

    /** Adds usable stock and submits one report through the upstream stock-changing manager. */
    private void addReport(AppState state, int availableBeforeReport, Contact contact) throws Exception {
        state.getInventory().addItem(new InventoryItem("Meter", "Equipment", availableBeforeReport));
        state.getBrokenItemManager().addBrokenItem(new BrokenItem("Meter", "Equipment", 2,
                "Cracked screen", "1 January 2020", contact));
    }

    /** Exercises both strict JSON validation and domain restoration. */
    private AppState roundTrip(AppState state) throws Exception {
        return AppState.restore(SnapshotCodec.decode(SnapshotCodec.encode(state.toSnapshot())));
    }
}

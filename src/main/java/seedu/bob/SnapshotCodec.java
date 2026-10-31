//@@author DaynaG3
package seedu.bob;

import java.io.IOException;
import java.io.StringReader;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import seedu.bob.exception.InvalidSnapshotException;
import seedu.bob.exception.StorageException;

/** Enforces the version-1 JSON shape before Gson creates typed snapshot records. */
final class SnapshotCodec {

    private static final Gson GSON = new GsonBuilder().serializeNulls().setPrettyPrinting()
            .setStrictness(Strictness.STRICT).create();
    private static final int MAX_DEPTH = 32;

    private SnapshotCodec() {
    }

    /** Parses one strict JSON document and validates both its field types and its domain state. */
    static AppSnapshot decode(String json) throws StorageException {
        try (JsonReader reader = new JsonReader(new StringReader(json))) {
            reader.setStrictness(Strictness.STRICT);
            JsonElement document = readValue(reader, 0);
            if (reader.peek() != JsonToken.END_DOCUMENT || !document.isJsonObject()) {
                throw invalidData();
            }
            JsonObject root = document.getAsJsonObject();
            int version = requireInteger(root.get("schemaVersion"));
            if (version != AppSnapshot.CURRENT_VERSION) {
                throw new StorageException(StorageException.Kind.UNSUPPORTED_VERSION,
                        "Unsupported data version: " + version + ".", null);
            }
            // Earlier version-1 files predate broken-item tracking and have no reports.
            if (!root.has("brokenItems")) {
                root.add("brokenItems", new JsonArray());
            }
            validateRoot(root);
            AppSnapshot snapshot = GSON.fromJson(root, AppSnapshot.class);
            AppState.restore(snapshot);
            return snapshot;
        } catch (IOException | IllegalArgumentException | InvalidSnapshotException e) {
            throw new StorageException(StorageException.Kind.INVALID_DATA, "The saved data is invalid.", e);
        }
    }

    /** Serializes only snapshots that can be restored in full; null contact fields are explicit. */
    static String encode(AppSnapshot snapshot) throws StorageException {
        try {
            AppState.restore(snapshot);
            return GSON.toJson(snapshot) + "\n";
        } catch (InvalidSnapshotException e) {
            throw new StorageException(StorageException.Kind.INVALID_DATA, "The saved data is invalid.", e);
        }
    }

    /** Reads without silently replacing duplicate keys; a depth bound also prevents recursive parser overflow. */
    private static JsonElement readValue(JsonReader reader, int depth) throws IOException, StorageException {
        if (depth > MAX_DEPTH) {
            throw invalidData();
        }
        switch (reader.peek()) {
            case BEGIN_OBJECT -> {
                JsonObject object = new JsonObject();
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (object.has(name)) {
                        throw invalidData();
                    }
                    object.add(name, readValue(reader, depth + 1));
                }
                reader.endObject();
                return object;
            }
            case BEGIN_ARRAY -> {
                JsonArray array = new JsonArray();
                reader.beginArray();
                while (reader.hasNext()) {
                    array.add(readValue(reader, depth + 1));
                }
                reader.endArray();
                return array;
            }
            case STRING -> {
                return new JsonPrimitive(reader.nextString());
            }
            case NUMBER -> {
                // Preserve the token until the version is known; newer formats may use other numeric types.
                return JsonParser.parseString(reader.nextString());
            }
            case BOOLEAN -> {
                return new JsonPrimitive(reader.nextBoolean());
            }
            case NULL -> {
                reader.nextNull();
                return JsonNull.INSTANCE;
            }
            default -> throw invalidData();
        }
    }

    /** Checks exact field sets at every level; nullable values are limited to optional contact details. */
    private static void validateRoot(JsonObject root) throws StorageException {
        requireObject(root, "schemaVersion", "inventory", "sessions", "requests", "contacts", "loans", "brokenItems");
        for (JsonElement value : requireArray(root, "inventory")) {
            JsonObject category = requireObject(value, "category", "items");
            requireStrings(category, "category");
            for (JsonElement item : requireArray(category, "items")) {
                JsonObject entry = requireObject(item, "name", "quantity");
                requireStrings(entry, "name");
                requireInteger(entry.get("quantity"));
            }
        }
        for (JsonElement value : requireArray(root, "sessions")) {
            JsonObject session = requireObject(value, "name", "date", "location", "startTime", "endTime",
                    "headcount", "preparation");
            requireStrings(session, "name", "date", "location", "startTime", "endTime");
            requireInteger(session.get("headcount"));
            for (JsonElement item : requireArray(session, "preparation")) {
                JsonObject entry = requireObject(item, "name", "category", "quantity");
                requireStrings(entry, "name", "category");
                requireInteger(entry.get("quantity"));
            }
        }
        for (JsonElement value : requireArray(root, "requests")) {
            JsonObject request = requireObject(value, "studentName", "itemName", "quantity", "deadline", "status");
            requireStrings(request, "studentName", "itemName", "deadline", "status");
            requireInteger(request.get("quantity"));
        }
        for (JsonElement value : requireArray(root, "contacts")) {
            validateContact(value);
        }
        for (JsonElement value : requireArray(root, "brokenItems")) {
            JsonObject report = requireObject(value, "name", "category", "quantity", "faultDescription",
                    "reportedDate", "contactIndex", "deletedContact");
            requireStrings(report, "name", "category", "faultDescription", "reportedDate");
            requireInteger(report.get("quantity"));
            if (!report.get("contactIndex").isJsonNull()) {
                requireInteger(report.get("contactIndex"));
            }
            if (!report.get("deletedContact").isJsonNull()) {
                validateContact(report.get("deletedContact"));
            }
        }
        for (JsonElement value : requireArray(root, "loans")) {
            JsonObject loan = requireObject(value, "name", "category", "quantity", "borrowerName",
                    "borrowerPhone", "dueDate");
            requireStrings(loan, "name", "category", "borrowerName", "borrowerPhone", "dueDate");
            requireInteger(loan.get("quantity"));
        }
    }

    /** Checks nullable contact details for both registered and deleted-contact snapshots. */
    private static void validateContact(JsonElement value) throws StorageException {
        JsonObject contact = requireObject(value, "name", "phone", "email", "role");
        requireStrings(contact, "name", "phone");
        for (String field : Set.of("email", "role")) {
            if (!contact.get(field).isJsonNull()) {
                requireStrings(contact, field);
            }
        }
    }

    private static JsonObject requireObject(JsonElement value, String... fields) throws StorageException {
        if (value == null || !value.isJsonObject() || !value.getAsJsonObject().keySet().equals(Set.of(fields))) {
            throw invalidData();
        }
        return value.getAsJsonObject();
    }

    private static JsonArray requireArray(JsonObject object, String field) throws StorageException {
        JsonElement value = object.get(field);
        if (value == null || !value.isJsonArray()) {
            throw invalidData();
        }
        return value.getAsJsonArray();
    }

    private static void requireStrings(JsonObject object, String... fields) throws StorageException {
        for (String field : fields) {
            JsonElement value = object.get(field);
            if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                throw invalidData();
            }
        }
    }

    private static int requireInteger(JsonElement value) throws StorageException {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw invalidData();
        }
        String number = value.getAsString();
        if (!number.matches("-?(0|[1-9][0-9]*)")) {
            throw invalidData();
        }
        return Integer.parseInt(number);
    }

    private static StorageException invalidData() {
        return new StorageException(StorageException.Kind.INVALID_DATA, "The saved data is invalid.", null);
    }
}

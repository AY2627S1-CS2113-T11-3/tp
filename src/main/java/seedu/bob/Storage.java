package seedu.bob;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.bob.exception.StorageException;

/** Reads validated snapshots and replaces complete files atomically while retaining a valid backup. */
public class Storage {

    private static final Logger LOGGER = Logger.getLogger(Storage.class.getName());
    private final Path path;
    private final Path backupPath;

    /**
     * Selects a data file; callers can pass a temporary path to keep tests away from real records.
     *
     * @param path Main snapshot path, resolved against the working directory if relative.
     */
    public Storage(Path path) {
        this.path = path.toAbsolutePath().normalize();
        this.backupPath = this.path.resolveSibling(this.path.getFileName() + ".bak");
    }

    public Path getPath() {
        return path;
    }

    /**
     * Loads the main snapshot, a valid backup, or genuinely new empty state without modifying files.
     *
     * @return Validated data and its source.
     * @throws StorageException If neither file is usable, or the main file uses an unsupported version.
     */
    public LoadResult load() throws StorageException {
        StorageException mainFailure = null;
        try {
            String json = readIfPresent(path);
            if (json != null) {
                return new LoadResult(SnapshotCodec.decode(json), LoadResult.Source.MAIN);
            }
        } catch (StorageException e) {
            if (e.getKind() == StorageException.Kind.UNSUPPORTED_VERSION) {
                throw e;
            }
            mainFailure = e;
        }
        String backup = readIfPresent(backupPath);
        if (backup != null) {
            return new LoadResult(SnapshotCodec.decode(backup), LoadResult.Source.BACKUP);
        }
        if (mainFailure != null) {
            throw mainFailure;
        }
        return new LoadResult(new AppState().toSnapshot(), LoadResult.Source.NEW);
    }

    /**
     * Writes a complete snapshot through temporary sibling files, preserving the previous valid main file.
     *
     * @param snapshot Complete state to commit to disk.
     * @throws StorageException If validation, writing, backup creation, or atomic replacement fails.
     */
    public void save(AppSnapshot snapshot) throws StorageException {
        String json = SnapshotCodec.encode(snapshot);
        Path temporary = null;
        Path backupTemporary = null;
        try {
            Files.createDirectories(path.getParent());
            temporary = Files.createTempFile(path.getParent(), "bob-", ".tmp");
            Files.writeString(temporary, json, StandardCharsets.UTF_8);
            String previous = readIfPresent(path);
            if (previous != null && isValidPreviousSnapshot(previous)) {
                backupTemporary = Files.createTempFile(path.getParent(), "bob-backup-", ".tmp");
                Files.writeString(backupTemporary, previous, StandardCharsets.UTF_8);
                moveAtomically(backupTemporary, backupPath);
            }
            moveAtomically(temporary, path);
        } catch (IOException | SecurityException e) {
            throw new StorageException(StorageException.Kind.IO_ERROR, "The snapshot could not be saved.", e);
        } finally {
            deleteTemporary(temporary);
            deleteTemporary(backupTemporary);
        }
    }

    /**
     * Replaces a complete file atomically, with a test seam for deterministic replacement failures.
     *
     * @param source Completed temporary sibling file.
     * @param target Main or backup destination in the same directory.
     * @throws IOException If the filesystem does not support the move or replacement fails.
     */
    protected void moveAtomically(Path source, Path target) throws IOException {
        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    /** Returns null only for a genuinely absent path, not an unreadable file or inaccessible directory. */
    private String readIfPresent(Path file) throws StorageException {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            return null;
        } catch (IOException | SecurityException e) {
            throw new StorageException(StorageException.Kind.IO_ERROR, "The storage file could not be read.", e);
        }
    }

    /** A corrupt main file must never replace a valid recovery backup; newer versions remain protected. */
    private boolean isValidPreviousSnapshot(String json) throws StorageException {
        try {
            SnapshotCodec.decode(json);
            return true;
        } catch (StorageException e) {
            if (e.getKind() == StorageException.Kind.UNSUPPORTED_VERSION) {
                throw e;
            }
            return false;
        }
    }

    private void deleteTemporary(Path temporary) {
        if (temporary == null) {
            return;
        }
        try {
            Files.deleteIfExists(temporary);
        } catch (IOException | SecurityException e) {
            LOGGER.log(Level.FINE, "Could not remove a temporary snapshot file", e);
        }
    }
}

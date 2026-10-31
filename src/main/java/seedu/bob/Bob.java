package seedu.bob;

import java.nio.file.Path;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.bob.exception.InvalidSnapshotException;
import seedu.bob.exception.StorageException;

/**
 * Runs the interactive Bob lab management application.
 */
public class Bob {

    private static final Logger LOGGER = Logger.getLogger(Bob.class.getName());

    /** Distinguishes an exit cancellation from a completed save or an acknowledged unsaved exit. */
    private enum ExitDecision {
        SAVED, CANCELED, UNSAVED
    }

    /**
     * Creates an application instance; interactive startup is performed by {@link #main(String[])}.
     */
    public Bob() {
    }

    /**
     * Greets the user and processes commands until the user enters Bye or standard input ends.
     * Exits without a greeting if input ends before a user name is supplied.
     *
     * @param args Command-line arguments, which are not used.
     */
    public static void main(String[] args) {
        int exitCode = run(new Scanner(System.in), new Storage(Path.of("data/bob.json")));
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    /** Runs an isolated session and returns an exit code, allowing tests to supply temporary storage. */
    static int run(Scanner in, Storage storage) {
        LOGGER.info("Initializing Bob application");

        LoadResult loaded;
        AppState state;
        try {
            loaded = storage.load();
            state = AppState.restore(loaded.snapshot());
        } catch (StorageException | InvalidSnapshotException e) {
            LOGGER.log(Level.FINE, "Could not restore saved application state", e);
            System.out.println("Cannot load data from " + storage.getPath() + ": " + e.getMessage());
            System.out.println("Bob has stopped; saved files were not changed.");
            return 1;
        }

        String banner = "  ____        _     \n"
                + " |  _ \\      | |    \n"
                + " | |_) | ___ | |__  \n"
                + " |  _ < / _ \\| '_ \\ \n"
                + " | |_) | (_) | |_) |\n"
                + " |____/ \\___/|_.__/ \n";
        System.out.println(banner);
        System.out.println("What is your name?");

        if (!in.hasNextLine()) {
            LOGGER.info("Input ended before a user name was provided");
            LOGGER.info("Exiting Bob application cleanly");
            return 0;
        }
        System.out.println("Hello " + in.nextLine() + ", welcome to Bob!");
        System.out.println(Ui.DIVIDER);
        if (loaded.source() == LoadResult.Source.BACKUP) {
            System.out.println("Recovered data from backup. It may not include the latest changes.");
        }

        Parser parser = new Parser();
        boolean hasPendingChanges = loaded.source() == LoadResult.Source.BACKUP;
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (line.trim().equalsIgnoreCase("Bye")) {
                LOGGER.info("User requested application termination");
                if (!hasPendingChanges) {
                    return 0;
                }
                ExitDecision decision = requestExit(in, storage, state);
                if (decision != ExitDecision.CANCELED) {
                    return decision == ExitDecision.SAVED ? 0 : 1;
                }
            } else {
                boolean hasChanged = parser.handleCommand(line, state);
                if (hasChanged) {
                    hasPendingChanges = true;
                    if (save(storage, state)) {
                        hasPendingChanges = false;
                    }
                }
            }
            System.out.println();
        }

        if (hasPendingChanges && !save(storage, state)) {
            System.out.println("Input ended with unsaved changes. The last saved data was kept.");
            return 1;
        }
        LOGGER.info("Exiting Bob application cleanly");
        return 0;
    }

    /** Attempts one complete save; failures leave the caller's pending state and live models intact. */
    private static boolean save(Storage storage, AppState state) {
        try {
            storage.save(state.toSnapshot());
            return true;
        } catch (StorageException e) {
            LOGGER.log(Level.FINE, "Could not save application state", e);
            System.out.println("Warning: changes are only in memory. Could not save to "
                    + storage.getPath() + ": " + e.getMessage());
            return false;
        }
    }

    /** Gives an explicit choice after an exit save fails, without blocking if standard input has ended. */
    private static ExitDecision requestExit(Scanner in, Storage storage, AppState state) {
        while (true) {
            if (save(storage, state)) {
                return ExitDecision.SAVED;
            }
            while (true) {
                System.out.println("Unsaved changes remain. Enter retry, cancel, or exit:");
                System.out.println();
                if (!in.hasNextLine()) {
                    System.out.println("Input ended with unsaved changes. The last saved data was kept.");
                    return ExitDecision.UNSAVED;
                }
                String choice = in.nextLine().trim();
                if (choice.equalsIgnoreCase("retry")) {
                    break;
                }
                if (choice.equalsIgnoreCase("cancel")) {
                    System.out.println("Exit canceled. Unsaved changes remain in memory.");
                    return ExitDecision.CANCELED;
                }
                if (choice.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting with unsaved changes.");
                    return ExitDecision.UNSAVED;
                }
                System.out.println("Choose retry, cancel, or exit.");
            }
        }
    }
}

package seedu.bob;

import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Runs the interactive Bob inventory and scheduling application.
 */
public class Bob {

    private static final Logger LOGGER = Logger.getLogger(Bob.class.getName());

    /**
     * Greets the user and processes commands until the user enters Bye.
     *
     * @param args Command-line arguments, which are not used.
     */
    public static void main(String[] args) {
        LOGGER.info("Initializing Bob application");

        String banner = "  ____        _     \n"
                + " |  _ \\      | |    \n"
                + " | |_) | ___ | |__  \n"
                + " |  _ < / _ \\| '_ \\ \n"
                + " | |_) | (_) | |_) |\n"
                + " |____/ \\___/|_.__/ \n";
        System.out.println(banner);
        System.out.println("What is your name?");

        Scanner in = new Scanner(System.in);
        if (!in.hasNextLine()) {
            LOGGER.info("Input ended before a user name was provided");
            LOGGER.info("Exiting Bob application cleanly");
            return;
        }
        System.out.println("Hello " + in.nextLine() + ", welcome to Bob!");
        System.out.println(Ui.DIVIDER);

        Parser parser = new Parser();
        Inventory inventory = new Inventory();
        SessionManager sessionManager = new SessionManager();
        ContactManager contactManager = new ContactManager();
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (line.trim().equalsIgnoreCase("Bye")) {
                LOGGER.info("User requested application termination");
                break;
            }
            parser.handleCommand(line, inventory, sessionManager, contactManager);
            System.out.println();
        }

        LOGGER.info("Exiting Bob application cleanly");
    }
}

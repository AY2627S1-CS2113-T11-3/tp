package seedu.bob;

import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Runs the interactive Bob lab management application.
 */
public class Bob {

    private static final Logger LOGGER = Logger.getLogger(Bob.class.getName());

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
        //@@author DaynaG3
        if (!in.hasNextLine()) {
            LOGGER.info("Input ended before a user name was provided");
            LOGGER.info("Exiting Bob application cleanly");
            return;
        }
        //@@author
        System.out.println("Hello " + in.nextLine() + ", welcome to Bob!");
        System.out.println(Ui.DIVIDER);

        Parser parser = new Parser();
        //@@author Lokwx
        Inventory inventory = new Inventory();
        //@@author
        SessionManager sessionManager = new SessionManager();
        ContactManager contactManager = new ContactManager();
        //@@author Lokwx
        RequestManager requestManager = new RequestManager();
        //@@author DaynaG3
        LoanManager loanManager = new LoanManager(inventory);
        BrokenItemManager brokenItemManager = new BrokenItemManager(inventory, contactManager);
        while (in.hasNextLine()) {
            //@@author
            String line = in.nextLine();
            if (line.trim().equalsIgnoreCase("Bye")) {
                LOGGER.info("User requested application termination");
                break;
            }
            parser.handleCommand(line, inventory, sessionManager, contactManager, requestManager,
                    loanManager, brokenItemManager);
            System.out.println();
        }

        LOGGER.info("Exiting Bob application cleanly");
    }
}

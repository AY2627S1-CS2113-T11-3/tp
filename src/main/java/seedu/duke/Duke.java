package seedu.duke;

import java.util.Scanner;

/**
 * Runs the interactive Bob inventory and scheduling application.
 */
public class Duke {
    /**
     * Greets the user and processes commands until the user enters Bye.
     *
     * @param args Command-line arguments, which are not used.
     */
    public static void main(String[] args) {
        String banner = "  ____        _     \n"
                + " |  _ \\      | |    \n"
                + " | |_) | ___ | |__  \n"
                + " |  _ < / _ \\| '_ \\ \n"
                + " | |_) | (_) | |_) |\n"
                + " |____/ \\___/|_.__/ \n";
        System.out.println(banner);
        System.out.println("What is your name?");

        Scanner in = new Scanner(System.in);
        System.out.println("Hello " + in.nextLine() + ", welcome to Bob!");
        System.out.println(Ui.DIVIDER);

        Parser parser = new Parser();
        Inventory inventory = new Inventory();
        SessionManager sessionManager = new SessionManager();
        String line;

        do {
            line = in.nextLine();
            if (line.trim().equalsIgnoreCase("Bye")) {
                break;
            }
            parser.handleCommand(line, inventory, sessionManager);
            System.out.println();
        } while (true);
    }
}

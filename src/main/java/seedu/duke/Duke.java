package seedu.duke;

import java.util.Scanner;

/**
 * Runs the interactive inventory application.
 */
public class Duke {
    /**
     * Greets the user and processes inventory commands until the user enters Bye.
     *
     * @param args Command-line arguments, which are not used.
     */
    public static void main(String[] args) {
        String banner = " ____        _        \n"
                + "|  _ \\ _   _| | _____ \n"
                + "| | | | | | | |/ / _ \\\n"
                + "| |_| | |_| |   <  __/\n"
                + "|____/ \\__,_|_|\\_\\___|\n";
        System.out.println(banner);
        System.out.println("What is your name?");

        Scanner in = new Scanner(System.in);
        System.out.println("Hello " + in.nextLine());
        System.out.println(Ui.DIVIDER);

        Parser parser = new Parser();
        Inventory inventory = new Inventory();
        String line;

        do {
            line = in.nextLine();
            parser.handleCommand(line, inventory);
            System.out.println();
        } while (!line.trim().equalsIgnoreCase("Bye"));
    }
}

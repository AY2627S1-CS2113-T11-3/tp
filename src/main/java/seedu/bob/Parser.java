package seedu.bob;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.CommandFormatException;
import seedu.bob.exception.InvalidContactIndexException;
import seedu.bob.exception.InvalidHeadcountException;
import seedu.bob.exception.InvalidItemIndexException;
import seedu.bob.exception.InvalidLoanIndexException;
import seedu.bob.exception.InvalidQuantityException;
import seedu.bob.exception.InvalidSessionIndexException;

/**
 * Parses help, inventory, scheduling, preparation, contact, and loan commands and dispatches them.
 */
public class Parser {

    private static final Logger LOGGER = Logger.getLogger(Parser.class.getName());
    private static final int DEFAULT_ITEM_QUANTITY = 1;
    private static final String UNEXPECTED_ERROR_MESSAGE =
            "An unexpected error occurred while processing the command. Please try again.";
    private static final String ADD_ITEM_USAGE =
            "Invalid format. Use: add-i n/NAME c/CATEGORY [q/QUANTITY]";
    private static final String DELETE_ITEM_USAGE =
            "Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY";
    private static final String LIST_ITEM_USAGE = "Invalid format. Use: list-i";
    private static final String ADD_SESSION_USAGE = "Invalid format. Use: add-s n/NAME d/DATE "
            + "l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT";
    private static final String DELETE_SESSION_USAGE = "Invalid format. Use: delete-s INDEX";
    private static final String LIST_SESSION_USAGE = "Invalid format. Use: list-s";
    private static final String ADD_PREPARATION_USAGE =
            "Invalid format. Use: add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]";
    private static final String DELETE_PREPARATION_USAGE =
            "Invalid format. Use: delete-p s/SESSION_INDEX i/ITEM_INDEX q/TOTAL_QUANTITY";
    private static final String LIST_PREPARATION_USAGE = "Invalid format. Use: list-p s/SESSION_INDEX";
    private static final String ADD_CONTACT_USAGE = "Invalid format. Use: add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]";
    private static final String DELETE_CONTACT_USAGE = "Invalid format. Use: delete-c INDEX";
    private static final String LIST_CONTACT_USAGE = "Invalid format. Use: list-c";
    private static final String ADD_LOAN_USAGE =
            "Invalid format. Use: add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE";
    private static final String LIST_LOAN_USAGE = "Invalid format. Use: list-l [overdue]";
    private static final String DELETE_LOAN_USAGE = "Invalid format. Use: delete-l INDEX";
    private static final String RETURN_LOAN_USAGE = "Invalid format. Use: return INDEX";
    private static final String HELP_USAGE = "Invalid format. Use: help";

    /** Accepted required and optional contact prefixes, in command order. */
    private static final List<List<String>> ADD_CONTACT_PREFIXES = List.of(
            List.of("n", "t"),
            List.of("n", "t", "e"),
            List.of("n", "t", "r"),
            List.of("n", "t", "e", "r"));

    /** Matches a single-letter command prefix occurring at the start of a token. */
    private static final Pattern ARGUMENT_PREFIX_PATTERN = Pattern.compile("(?<!\\S)([A-Za-z])/");

    /**
     * Executes an inventory, scheduling, preparation, contact, or loan command, or prints a message for an
     * unknown command. Expected user errors are displayed without ending the application. Unexpected
     * runtime failures are logged and replaced with a safe user-facing message.
     *
     * @param input Command text containing the required arguments.
     * @param inventory Inventory to query or update.
     * @param sessionManager SessionManager to schedule or modify lab sessions.
     * @param contactManager ContactManager to add, remove, or display contacts.
     * @param loanManager LoanManager to add, correct, return, or display loans.
     */
    public void handleCommand(String input, Inventory inventory, SessionManager sessionManager,
            ContactManager contactManager, LoanManager loanManager) {
        try {
            handleCommandInternal(input, inventory, sessionManager, contactManager, loanManager);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Unexpected failure while processing a command", e);
            System.out.println(UNEXPECTED_ERROR_MESSAGE);
        }
    }

    /**
     * Parses and dispatches one command after the public failure boundary has been established.
     */
    private void handleCommandInternal(String input, Inventory inventory, SessionManager sessionManager,
            ContactManager contactManager, LoanManager loanManager) {
        String[] parts = input.trim().split("\\s+", 2);
        String command = parts[0];

        switch (command) {
            case "add-i" -> {
                try {
                    String arguments = parts.length == 2 ? parts[1] : "";
                    addItem(arguments, inventory);
                } catch (CommandException e) {
                    System.out.println(e.getMessage());
                }
            }
            case "delete-i" -> {
                try {
                    String arguments = parts.length == 2 ? parts[1] : "";
                    deleteItem(arguments, inventory);
                } catch (CommandException e) {
                    System.out.println(e.getMessage());
                }
            }
            case "list-i" -> {
                try {
                    String arguments = parts.length == 2 ? parts[1] : "";
                    listItems(arguments, inventory);
                } catch (CommandException e) {
                    System.out.println(e.getMessage());
                }
            }
            case "add-s" -> {
                try {
                    String arguments = parts.length == 2 ? parts[1] : "";
                    addSession(arguments, sessionManager);
                } catch (CommandException e) {
                    System.out.println(e.getMessage());
                }
            }
            case "delete-s" -> {
                try {
                    String arguments = parts.length == 2 ? parts[1] : "";
                    deleteSession(arguments, sessionManager);
                } catch (CommandException e) {
                    System.out.println(e.getMessage());
                }
            }
            case "list-s" -> {
                try {
                    String arguments = parts.length == 2 ? parts[1] : "";
                    listSessions(arguments, sessionManager);
                } catch (CommandException e) {
                    System.out.println(e.getMessage());
                }
            }
            case "help" -> {
                try {
                    String arguments = parts.length == 2 ? parts[1] : "";
                    showHelp(arguments);
                } catch (CommandException e) {
                    System.out.println(e.getMessage());
                }
            }
            case "add-p", "delete-p", "list-p" -> {
                try {
                    String arguments = parts.length == 2 ? parts[1] : "";
                    handlePreparation(command, arguments, inventory, sessionManager);
                } catch (CommandException e) {
                    System.out.println(e.getMessage());
                }
            }
            case "add-c" -> {
                try {
                    String arguments = parts.length == 2 ? parts[1] : "";
                    addContact(arguments, contactManager);
                } catch (CommandException e) {
                    System.out.println(e.getMessage());
                }
            }
            case "delete-c" -> {
                try {
                    String arguments = parts.length == 2 ? parts[1] : "";
                    deleteContact(arguments, contactManager);
                } catch (CommandException e) {
                    System.out.println(e.getMessage());
                }
            }
            case "list-c" -> {
                try {
                    String arguments = parts.length == 2 ? parts[1] : "";
                    listContacts(arguments, contactManager);
                } catch (CommandException e) {
                    System.out.println(e.getMessage());
                }
            }
            case "add-l", "list-l", "delete-l", "return" -> {
                try {
                    String arguments = parts.length == 2 ? parts[1] : "";
                    handleLoan(command, arguments, loanManager);
                } catch (CommandException e) {
                    System.out.println(e.getMessage());
                }
            }
            default -> {
                System.out.println("Invalid command");
            }
        }
    }

    /**
     * Validates and executes a help command.
     *
     * @param arguments Text following the {@code help} command word.
     * @throws CommandFormatException If any arguments are supplied.
     */
    private void showHelp(String arguments) throws CommandFormatException {
        if (!arguments.isBlank()) {
            throw new CommandFormatException(HELP_USAGE);
        }
        System.out.println(Ui.HELP_MESSAGE);
    }

    /**
     * Parses loan commands, validating input before the manager changes loans or stock.
     *
     * @throws CommandException If arguments, borrower details, date, or requested stock are invalid.
     */
    private void handleLoan(String command, String arguments, LoanManager loanManager) throws CommandException {
        if (command.equals("delete-l") || command.equals("return")) {
            String indexText = arguments.trim();
            if (indexText.isEmpty() || indexText.matches(".*\\s+.*")) {
                String usage = command.equals("return") ? RETURN_LOAN_USAGE : DELETE_LOAN_USAGE;
                throw new CommandFormatException(usage);
            }
            int index = parseLoanIndex(indexText);
            if (command.equals("return")) {
                loanManager.returnLoan(index);
            } else {
                loanManager.deleteLoan(index);
            }
            return;
        }
        if (command.equals("list-l")) {
            if (arguments.isBlank()) {
                loanManager.listLoans();
            } else if (arguments.trim().equals("overdue")) {
                loanManager.listOverdueLoans();
            } else {
                throw new CommandFormatException(LIST_LOAN_USAGE);
            }
            return;
        }
        List<String> values = extractValues(arguments, List.of("n", "c", "q", "b", "t", "d"), ADD_LOAN_USAGE);
        int quantity = parseQuantity(values.get(2));
        Contact borrower = new Contact(values.get(3), values.get(4), null, null);
        Loan loan = new Loan(values.get(0), values.get(1), quantity, borrower, values.get(5));
        loanManager.addLoan(loan);
    }

    /**
     * Parses and executes an add-item command.
     *
     * @param arguments Text following the {@code add-i} command word.
     * @param inventory Inventory to update.
     * @throws CommandException If the arguments are malformed or the item cannot be added.
     */
    private void addItem(String arguments, Inventory inventory) throws CommandException {
        List<String> prefixes = findPrefixes(arguments);
        List<String> expectedPrefixes;
        if (prefixes.equals(List.of("n", "c"))) {
            expectedPrefixes = List.of("n", "c");
        } else if (prefixes.equals(List.of("n", "c", "q"))) {
            expectedPrefixes = List.of("n", "c", "q");
        } else {
            throw new CommandFormatException(ADD_ITEM_USAGE);
        }

        List<String> values = extractValues(arguments, expectedPrefixes, ADD_ITEM_USAGE);
        String name = values.get(0);
        String category = values.get(1);
        if (name.isBlank()) {
            throw new CommandFormatException("Item name cannot be blank.");
        }
        if (category.isBlank()) {
            throw new CommandFormatException("Category cannot be blank.");
        }

        int quantity = expectedPrefixes.size() == 2
                ? DEFAULT_ITEM_QUANTITY
                : parseQuantity(values.get(2));
        inventory.addItem(new InventoryItem(name, category, quantity));
    }

    /**
     * Parses and executes a delete-item command.
     *
     * @param arguments Text following the {@code delete-i} command word.
     * @param inventory Inventory to update.
     * @throws CommandException If the arguments are malformed or the item cannot be removed.
     */
    private void deleteItem(String arguments, Inventory inventory) throws CommandException {
        List<String> expectedPrefixes = List.of("c", "i", "q");
        if (!findPrefixes(arguments).equals(expectedPrefixes)) {
            throw new CommandFormatException(DELETE_ITEM_USAGE);
        }

        List<String> values = extractValues(arguments, expectedPrefixes, DELETE_ITEM_USAGE);
        String category = values.get(0);
        if (category.isBlank()) {
            throw new CommandFormatException("Category cannot be blank.");
        }

        int index = parseItemIndex(values.get(1));
        int quantity = parseQuantity(values.get(2));
        inventory.removeItem(category, index, quantity);
    }

    /**
     * Validates and executes a list-items command.
     *
     * @param arguments Text following the {@code list-i} command word.
     * @param inventory Inventory to display.
     * @throws CommandFormatException If any arguments are supplied.
     */
    private void listItems(String arguments, Inventory inventory) throws CommandFormatException {
        if (!arguments.isBlank()) {
            throw new CommandFormatException(LIST_ITEM_USAGE);
        }
        inventory.listItems();
    }

    /**
     * Parses and executes an add-session command.
     *
     * @param arguments Text following the {@code add-s} command word.
     * @param sessionManager Session schedule to update.
     * @throws CommandException If the arguments or session values are invalid.
     */
    private void addSession(String arguments, SessionManager sessionManager) throws CommandException {
        List<String> expectedPrefixes = List.of("n", "d", "l", "s", "e", "p");
        if (!findPrefixes(arguments).equals(expectedPrefixes)) {
            throw new CommandFormatException(ADD_SESSION_USAGE);
        }

        List<String> values = extractValues(arguments, expectedPrefixes, ADD_SESSION_USAGE);
        String name = values.get(0);
        String date = values.get(1);
        String location = values.get(2);
        String startTime = values.get(3);
        String endTime = values.get(4);
        int headcount = parseHeadcount(values.get(5));

        Session session = new Session(name, date, location, startTime, endTime, headcount);
        sessionManager.addSession(session);
    }

    /**
     * Parses and executes a delete-session command.
     *
     * @param arguments Text following the {@code delete-s} command word.
     * @param sessionManager Session schedule to update.
     * @throws CommandException If the index is malformed or outside the schedule.
     */
    private void deleteSession(String arguments, SessionManager sessionManager) throws CommandException {
        String trimmedArguments = arguments.trim();
        if (trimmedArguments.isEmpty() || trimmedArguments.matches(".*\\s+.*")) {
            throw new CommandFormatException(DELETE_SESSION_USAGE);
        }

        int index = parseSessionIndex(trimmedArguments);
        sessionManager.deleteSession(index);
    }

    /**
     * Validates and executes a list-sessions command.
     *
     * @param arguments Text following the {@code list-s} command word.
     * @param sessionManager Session schedule to display.
     * @throws CommandFormatException If any arguments are supplied.
     */
    private void listSessions(String arguments, SessionManager sessionManager) throws CommandFormatException {
        if (!arguments.isBlank()) {
            throw new CommandFormatException(LIST_SESSION_USAGE);
        }
        sessionManager.listSessions();
    }

    /**
     * Dispatches a preparation command and displays the resulting requirements against current stock.
     *
     * @throws CommandException If the command arguments or requested change are invalid.
     */
    private void handlePreparation(String command, String arguments, Inventory inventory,
            SessionManager sessionManager) throws CommandException {
        Session session;
        switch (command) {
            case "add-p" -> session = addPreparation(arguments, sessionManager);
            case "delete-p" -> session = deletePreparation(arguments, sessionManager);
            case "list-p" -> {
                List<String> values = extractValues(arguments, List.of("s"), LIST_PREPARATION_USAGE);
                session = sessionManager.getSession(parseSessionIndex(values.get(0)));
            }
            default -> throw new CommandFormatException("Invalid preparation command.");
        }
        listPreparation(session, inventory);
    }

    /**
     * Adds an item's per-person quantity to the selected session's preparation.
     *
     * @return Updated session.
     * @throws CommandException If arguments or the calculated requirement are invalid.
     */
    private Session addPreparation(String arguments, SessionManager sessionManager) throws CommandException {
        List<String> prefixes = findPrefixes(arguments);
        List<String> expectedPrefixes = prefixes.equals(List.of("s", "i", "c"))
                ? List.of("s", "i", "c") : List.of("s", "i", "c", "q");
        List<String> values = extractValues(arguments, expectedPrefixes, ADD_PREPARATION_USAGE);
        Session session = sessionManager.getSession(parseSessionIndex(values.get(0)));
        int quantityPerPerson = values.size() == 3 ? DEFAULT_ITEM_QUANTITY : parseQuantity(values.get(3));
        PreparationItem item = session.getPreparation().addItem(
                values.get(1), values.get(2), quantityPerPerson, session.getHeadcount());
        int addedQuantity = quantityPerPerson * session.getHeadcount();
        System.out.printf("Successfully added: %dx %s to preparation%n", addedQuantity, item);
        return session;
    }

    /**
     * Removes a total quantity from an item in the selected session's preparation.
     *
     * @return Updated session.
     * @throws CommandException If the arguments or removal are invalid.
     */
    private Session deletePreparation(String arguments, SessionManager sessionManager) throws CommandException {
        List<String> values = extractValues(arguments, List.of("s", "i", "q"), DELETE_PREPARATION_USAGE);
        Session session = sessionManager.getSession(parseSessionIndex(values.get(0)));
        int index = parseItemIndex(values.get(1));
        int quantity = parseQuantity(values.get(2));
        PreparationItem item = session.getPreparation().removeItem(index, quantity);
        System.out.printf("Successfully removed: %dx %s from preparation%n", quantity, item);
        return session;
    }

    /**
     * Displays required totals and current availability, including a shortfall for insufficient items.
     */
    private void listPreparation(Session session, Inventory inventory) {
        System.out.println(Ui.DIVIDER);
        System.out.println("Lab preparation: " + session);
        System.out.println(Ui.DIVIDER);
        List<PreparationItem> items = session.getPreparation().getItems();
        if (items.isEmpty()) {
            System.out.println("No items in preparation.");
        }

        int index = 1;
        for (PreparationItem item : items) {
            int available = inventory.getAvailableQuantity(item.getName(), item.getCategory());
            int required = item.getQuantity();
            String shortage = available < required
                    ? " !! Insufficient items (Shortfall: " + (required - available) + ")" : "";
            System.out.printf("%d. %s (Required: %d, Available: %d)%s%n",
                    index, item, required, available, shortage);
            index++;
        }
        System.out.println(Ui.DIVIDER);
    }

    /**
     * Parses and executes an add-contact command.
     *
     * @param arguments Text following the {@code add-c} command word.
     * @param contactManager Contact list to update.
     * @throws CommandException If the arguments or contact values are invalid.
     */
    private void addContact(String arguments, ContactManager contactManager) throws CommandException {
        List<String> prefixes = findPrefixes(arguments);
        if (!ADD_CONTACT_PREFIXES.contains(prefixes)) {
            throw new CommandFormatException(ADD_CONTACT_USAGE);
        }

        List<String> values = extractValues(arguments, prefixes, ADD_CONTACT_USAGE);
        String email = null;
        String role = null;
        // Optional values follow the two required ones, in the order given by the prefixes.
        for (int i = 2; i < prefixes.size(); i++) {
            if (prefixes.get(i).equals("e")) {
                email = values.get(i);
            } else {
                role = values.get(i);
            }
        }

        contactManager.addContact(new Contact(values.get(0), values.get(1), email, role));
    }

    /**
     * Parses and executes a delete-contact command.
     *
     * @param arguments Text following the {@code delete-c} command word.
     * @param contactManager Contact list to update.
     * @throws CommandException If the index is malformed or outside the contact list.
     */
    private void deleteContact(String arguments, ContactManager contactManager) throws CommandException {
        String trimmedArguments = arguments.trim();
        if (trimmedArguments.isEmpty() || trimmedArguments.matches(".*\\s+.*")) {
            throw new CommandFormatException(DELETE_CONTACT_USAGE);
        }

        contactManager.deleteContact(parseContactIndex(trimmedArguments));
    }

    /**
     * Validates and executes a list-contacts command.
     *
     * @param arguments Text following the {@code list-c} command word.
     * @param contactManager Contact list to display.
     * @throws CommandFormatException If any arguments are supplied.
     */
    private void listContacts(String arguments, ContactManager contactManager) throws CommandFormatException {
        if (!arguments.isBlank()) {
            throw new CommandFormatException(LIST_CONTACT_USAGE);
        }
        contactManager.listContacts();
    }

    /**
     * Returns all single-letter argument prefixes in their encountered order.
     *
     * @param arguments Command arguments to inspect.
     * @return Prefix names without their trailing slash.
     */
    private List<String> findPrefixes(String arguments) {
        List<String> prefixes = new ArrayList<>();
        Matcher matcher = ARGUMENT_PREFIX_PATTERN.matcher(arguments.trim());
        while (matcher.find()) {
            prefixes.add(matcher.group(1));
        }
        return prefixes;
    }

    /**
     * Extracts values between an already validated sequence of prefixes.
     *
     * @param arguments Command arguments containing the prefixes.
     * @param expectedPrefixes Prefixes expected in the arguments.
     * @param usageMessage Message explaining the command's required syntax.
     * @return Values corresponding to the prefixes.
     * @throws CommandFormatException If the prefix sequence is inconsistent.
     */
    private List<String> extractValues(String arguments, List<String> expectedPrefixes, String usageMessage)
            throws CommandFormatException {
        String trimmedArguments = arguments.trim();
        Matcher matcher = ARGUMENT_PREFIX_PATTERN.matcher(trimmedArguments);
        List<Integer> valueStarts = new ArrayList<>();
        List<Integer> prefixStarts = new ArrayList<>();

        int prefixIndex = 0;
        while (matcher.find()) {
            if (prefixIndex >= expectedPrefixes.size()
                    || !matcher.group(1).equals(expectedPrefixes.get(prefixIndex))) {
                throw new CommandFormatException(usageMessage);
            }
            prefixStarts.add(matcher.start());
            valueStarts.add(matcher.end());
            prefixIndex++;
        }
        if (prefixIndex != expectedPrefixes.size() || prefixStarts.get(0) != 0) {
            throw new CommandFormatException(usageMessage);
        }

        List<String> values = new ArrayList<>();
        for (int i = 0; i < valueStarts.size(); i++) {
            int valueEnd = i + 1 < prefixStarts.size() ? prefixStarts.get(i + 1) : trimmedArguments.length();
            values.add(trimmedArguments.substring(valueStarts.get(i), valueEnd).trim());
        }
        return values;
    }

    /**
     * Parses a positive one-based item index.
     *
     * @param indexText Index text supplied by the user.
     * @return Parsed positive index.
     * @throws InvalidItemIndexException If the index is not a positive integer.
     */
    private int parseItemIndex(String indexText) throws InvalidItemIndexException {
        try {
            int index = Integer.parseInt(indexText);
            if (index <= 0) {
                throw new InvalidItemIndexException();
            }
            return index;
        } catch (NumberFormatException e) {
            throw new InvalidItemIndexException();
        }
    }

    /**
     * Parses a positive item quantity.
     *
     * @param quantityText Quantity text supplied by the user.
     * @return Parsed positive quantity.
     * @throws InvalidQuantityException If the quantity is not a positive integer.
     */
    private int parseQuantity(String quantityText) throws InvalidQuantityException {
        try {
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                throw new InvalidQuantityException();
            }
            return quantity;
        } catch (NumberFormatException e) {
            throw new InvalidQuantityException();
        }
    }

    /**
     * Parses a positive session headcount.
     *
     * @param headcountText Headcount text supplied by the user.
     * @return Parsed positive headcount.
     * @throws InvalidHeadcountException If the headcount is not a positive integer.
     */
    private int parseHeadcount(String headcountText) throws InvalidHeadcountException {
        try {
            int headcount = Integer.parseInt(headcountText);
            if (headcount <= 0) {
                throw new InvalidHeadcountException();
            }
            return headcount;
        } catch (NumberFormatException e) {
            throw new InvalidHeadcountException();
        }
    }

    /**
     * Parses a positive one-based session index.
     *
     * @param indexText Index text supplied by the user.
     * @return Parsed positive index.
     * @throws InvalidSessionIndexException If the index is not a positive integer.
     */
    private int parseSessionIndex(String indexText) throws InvalidSessionIndexException {
        try {
            int index = Integer.parseInt(indexText);
            if (index <= 0) {
                throw new InvalidSessionIndexException();
            }
            return index;
        } catch (NumberFormatException e) {
            throw new InvalidSessionIndexException();
        }
    }

    /**
     * Parses a positive one-based active loan index.
     *
     * @param indexText Loan index text supplied by the user.
     * @return Parsed positive index.
     * @throws InvalidLoanIndexException If the text does not represent a positive integer.
     */
    private int parseLoanIndex(String indexText) throws InvalidLoanIndexException {
        try {
            int index = Integer.parseInt(indexText);
            if (index <= 0) {
                throw new InvalidLoanIndexException();
            }
            return index;
        } catch (NumberFormatException e) {
            throw new InvalidLoanIndexException();
        }
    }

    /**
     * Parses a positive one-based contact index.
     *
     * @param indexText Index text supplied by the user.
     * @return Parsed positive index.
     * @throws InvalidContactIndexException If the index is not a positive integer.
     */
    private int parseContactIndex(String indexText) throws InvalidContactIndexException {
        try {
            int index = Integer.parseInt(indexText);
            if (index <= 0) {
                throw new InvalidContactIndexException();
            }
            return index;
        } catch (NumberFormatException e) {
            throw new InvalidContactIndexException();
        }
    }
}

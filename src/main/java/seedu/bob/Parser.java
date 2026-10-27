package seedu.bob;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.CommandFormatException;
import seedu.bob.exception.InvalidBrokenItemIndexException;
import seedu.bob.exception.InvalidContactIndexException;
import seedu.bob.exception.InvalidHeadcountException;
import seedu.bob.exception.InvalidItemIndexException;
import seedu.bob.exception.InvalidLoanIndexException;
import seedu.bob.exception.InvalidQuantityException;
import seedu.bob.exception.InvalidRequestIndexException;
import seedu.bob.exception.InvalidSessionIndexException;

/**
 * Parses help, inventory, scheduling, preparation, contact, student request, loan, and broken-item commands
 * and dispatches them.
 */
public class Parser {

    private static final Logger LOGGER = Logger.getLogger(Parser.class.getName());
    private static final int DEFAULT_ITEM_QUANTITY = 1;

    // Positions corresponding to the add-l prefixes: n, c, q, b, t, d.
    private static final int LOAN_ITEM_NAME_INDEX = 0;
    private static final int LOAN_CATEGORY_INDEX = 1;
    private static final int LOAN_QUANTITY_INDEX = 2;
    private static final int LOAN_BORROWER_NAME_INDEX = 3;
    private static final int LOAN_BORROWER_PHONE_INDEX = 4;
    private static final int LOAN_DUE_DATE_INDEX = 5;

    private static final String UNEXPECTED_ERROR_MESSAGE =
            "An unexpected error occurred while processing the command. Please try again.";

    private static final String ADD_ITEM_USAGE =
            "Invalid format. Use: add-i n/NAME c/CATEGORY [q/QUANTITY]";
    private static final String DELETE_ITEM_USAGE =
            "Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY";
    private static final String LIST_ITEM_USAGE = "Invalid format. Use: list-i";
    //@@author J-unn-y
    private static final String FIND_ITEM_USAGE = "Invalid format. Use: find-i KEYWORD";
    //@@author

    private static final String ADD_SESSION_USAGE = "Invalid format. Use: add-s n/NAME d/DATE "
            + "l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT";
    private static final String DELETE_SESSION_USAGE = "Invalid format. Use: delete-s INDEX";
    private static final String LIST_SESSION_USAGE = "Invalid format. Use: list-s";

    //@@author J-unn-y
    private static final String ADD_PREPARATION_USAGE =
            "Invalid format. Use: add-p s/SESSION_INDEX i/ITEM_NAME c/CATEGORY [q/QUANTITY_PER_PERSON]";
    private static final String DELETE_PREPARATION_USAGE =
            "Invalid format. Use: delete-p s/SESSION_INDEX i/ITEM_INDEX q/TOTAL_QUANTITY";
    private static final String LIST_PREPARATION_USAGE = "Invalid format. Use: list-p s/SESSION_INDEX";
    //@@author
    private static final String ADD_REQUEST_USAGE =
            "Invalid format. Use: add-r n/STUDENT_NAME i/ITEM q/QUANTITY d/DEADLINE";
    private static final String DELETE_REQUEST_USAGE = "Invalid format. Use: delete-r INDEX";
    private static final String LIST_REQUEST_USAGE = "Invalid format. Use: list-r";
    private static final String MARK_REQUEST_USAGE = "Invalid format. Use: mark-r INDEX STATUS";

    private static final String ADD_CONTACT_USAGE =
            "Invalid format. Use: add-c n/NAME t/PHONE [e/EMAIL] [r/ROLE]";
    private static final String DELETE_CONTACT_USAGE = "Invalid format. Use: delete-c INDEX";
    private static final String LIST_CONTACT_USAGE = "Invalid format. Use: list-c";

    private static final String ADD_LOAN_USAGE =
            "Invalid format. Use: add-l n/NAME c/CATEGORY q/QUANTITY b/BORROWER t/PHONE d/DUE_DATE";
    private static final String LIST_LOAN_USAGE = "Invalid format. Use: list-l [overdue]";
    private static final String DELETE_LOAN_USAGE = "Invalid format. Use: delete-l INDEX";
    private static final String RETURN_LOAN_USAGE = "Invalid format. Use: return INDEX";
    private static final String ADD_BROKEN_USAGE =
            "Invalid format. Use: add-b n/NAME c/CATEGORY q/QUANTITY f/FAULT d/DATE_REPORTED [m/CONTACT_INDEX]";
    private static final String LIST_BROKEN_USAGE = "Invalid format. Use: list-b";
    private static final String DELETE_BROKEN_USAGE = "Invalid format. Use: delete-b INDEX";

    // Positions corresponding to the add-b prefixes: n, c, q, f, d, and optional m.
    private static final int BROKEN_NAME_INDEX = 0;
    private static final int BROKEN_CATEGORY_INDEX = 1;
    private static final int BROKEN_QUANTITY_INDEX = 2;
    private static final int BROKEN_FAULT_INDEX = 3;
    private static final int BROKEN_DATE_INDEX = 4;
    private static final int BROKEN_CONTACT_INDEX = 5;

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
     * Creates a parser that dispatches commands to the managers supplied for each invocation.
     */
    public Parser() {
    }

    /**
     * Executes an inventory, scheduling, preparation, contact, student request, or loan command,
     * or prints a message for an unknown command. Expected user errors are displayed without ending
     * the application. Broken-item commands are explicitly rejected because this overload has no report manager.
     * Unexpected runtime failures are logged and replaced with a safe user-facing message.
     *
     * @param input Command text containing the required arguments.
     * @param inventory Inventory to query or update.
     * @param sessionManager SessionManager to schedule or modify lab sessions.
     * @param contactManager ContactManager to add, remove, or display contacts.
     * @param requestManager RequestManager to add, remove, mark, or display student requests.
     * @param loanManager LoanManager to add, correct, return, or display loans.
     */
    public void handleCommand(String input, Inventory inventory, SessionManager sessionManager,
            ContactManager contactManager, RequestManager requestManager, LoanManager loanManager) {
        handleCommand(input, inventory, sessionManager, contactManager, requestManager, loanManager, null);
    }

    /**
     * Executes commands with one persistent broken-item manager sharing the supplied inventory and contacts.
     * The six-argument overload remains available to callers of the preexisting features.
     *
     * @param input Command text.
     * @param inventory Shared usable inventory.
     * @param sessionManager Lab schedule.
     * @param contactManager Shared maintenance and supplier contacts; broken-item contact selection
     *     is rejected if this dependency is null.
     * @param requestManager Student requests.
     * @param loanManager Active loans backed by the shared inventory.
     * @param brokenItemManager Broken reports backed by the same inventory and contacts;
     *     if null, broken-item commands receive an unavailable-feature response without changing state.
     */
    public void handleCommand(String input, Inventory inventory, SessionManager sessionManager,
            ContactManager contactManager, RequestManager requestManager, LoanManager loanManager,
            BrokenItemManager brokenItemManager) {
        try {
            handleCommandInternal(input, inventory, sessionManager, contactManager, requestManager,
                    loanManager, brokenItemManager);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Unexpected failure while processing a command", e);
            System.out.println(UNEXPECTED_ERROR_MESSAGE);
        }
    }

    /**
     * Parses and dispatches one command after the public failure boundary has been established.
     */
    private void handleCommandInternal(String input, Inventory inventory, SessionManager sessionManager,
                                       ContactManager contactManager, RequestManager requestManager,
                                       LoanManager loanManager,
                                       BrokenItemManager brokenItemManager) {
        String[] parts = (input == null ? "" : input).trim().split("\\s+", 2);
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
            //@@author J-unn-y
            case "find-i" -> {
                try {
                    String arguments = parts.length == 2 ? parts[1] : "";
                    findItems(arguments, inventory, loanManager);
                } catch (CommandException e) {
                    System.out.println(e.getMessage());
                }
            }
            //@@author
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
            case "add-r", "delete-r", "list-r", "mark-r" -> {
                try {
                    String arguments = parts.length == 2 ? parts[1] : "";
                    handleRequest(command, arguments, requestManager, inventory);
                } catch (CommandException e) {
                    System.out.println(e.getMessage());
                }
            }
            case "add-b", "list-b", "delete-b" -> {
                try {
                    String arguments = parts.length == 2 ? parts[1] : "";
                    handleBrokenItem(command, arguments, brokenItemManager, contactManager);
                } catch (CommandException e) {
                    LOGGER.log(Level.FINE, "Rejected broken-item command: " + command, e);
                    System.out.println(e.getMessage());
                }
            }
            case "add-l", "list-l", "delete-l", "return" -> {
                try {
                    String arguments = parts.length == 2 ? parts[1] : "";
                    handleLoan(command, arguments, loanManager);
                } catch (CommandException e) {
                    LOGGER.log(Level.FINE, "Rejected loan command: " + command, e);
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
     * @param command Supported loan command word.
     * @param arguments Text following the command word.
     * @param loanManager Active loans and their shared inventory.
     * @throws CommandException If syntax, borrower details, date, stock, or a loan index is invalid.
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
                Ui.showLoanReturned(loanManager.returnLoan(index));
            } else {
                Ui.showLoanDeleted(loanManager.deleteLoan(index));
            }
            return;
        }
        if (command.equals("list-l")) {
            if (arguments.isBlank()) {
                Ui.showLoans(loanManager.getLoanEntries(), false);
            } else if (arguments.trim().equals("overdue")) {
                Ui.showLoans(loanManager.getOverdueLoanEntries(), true);
            } else {
                throw new CommandFormatException(LIST_LOAN_USAGE);
            }
            return;
        }
        List<String> values = extractValues(arguments, List.of("n", "c", "q", "b", "t", "d"), ADD_LOAN_USAGE);
        int quantity = parseQuantity(values.get(LOAN_QUANTITY_INDEX));
        Contact borrower = new Contact(
                values.get(LOAN_BORROWER_NAME_INDEX),
                values.get(LOAN_BORROWER_PHONE_INDEX),
                null, null);
        Loan loan = new Loan(
                values.get(LOAN_ITEM_NAME_INDEX),
                values.get(LOAN_CATEGORY_INDEX),
                quantity,
                borrower,
                values.get(LOAN_DUE_DATE_INDEX));
        Ui.showLoanAdded(loanManager.addLoan(loan));
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

    //@@author J-unn-y
    /**
     * Searches stock and active loans for items whose name or category contains the keyword.
     * The whole argument text is the keyword, so it may contain spaces.
     *
     * @param arguments Text following the {@code find-i} command word.
     * @param inventory Stock to search.
     * @param loanManager Active loans to search.
     * @throws CommandFormatException If no keyword is supplied.
     */
    private void findItems(String arguments, Inventory inventory, LoanManager loanManager)
            throws CommandFormatException {
        String keyword = arguments.strip();
        if (keyword.isEmpty()) {
            throw new CommandFormatException(FIND_ITEM_USAGE);
        }
        Ui.showLookupResults(keyword, ItemFinder.findItems(keyword, inventory, loanManager));
    }
    //@@author

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

    //@@author J-unn-y
    /**
     * Dispatches a preparation command and displays the resulting requirements against current stock.
     *
     * @param command Supported preparation command word.
     * @param arguments Text following the command word.
     * @param inventory Available stock used to calculate shortages.
     * @param sessionManager Schedule containing the selected session and its preparation.
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
                session = sessionManager.getSession(parseSessionIndex(values.getFirst()));
            }
            default -> throw new CommandFormatException("Invalid preparation command.");
        }
        listPreparation(session, inventory);
    }

    /**
     * Adds an item's per-person quantity to the selected session's preparation.
     *
     * @param arguments Prefixed session, item, category, and optional per-attendee quantity.
     * @param sessionManager Schedule containing the selected session.
     * @return Updated session.
     * @throws CommandException If arguments or the calculated requirement are invalid.
     */
    private Session addPreparation(String arguments, SessionManager sessionManager) throws CommandException {
        List<String> prefixes = findPrefixes(arguments);
        List<String> expectedPrefixes = prefixes.equals(List.of("s", "i", "c"))
                ? List.of("s", "i", "c") : List.of("s", "i", "c", "q");
        List<String> values = extractValues(arguments, expectedPrefixes, ADD_PREPARATION_USAGE);
        Session session = sessionManager.getSession(parseSessionIndex(values.getFirst()));
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
     * @param arguments Prefixed session index, preparation item index, and total quantity to remove.
     * @param sessionManager Schedule containing the selected session.
     * @return Updated session.
     * @throws CommandException If the arguments or removal are invalid.
     */
    private Session deletePreparation(String arguments, SessionManager sessionManager) throws CommandException {
        List<String> values = extractValues(arguments, List.of("s", "i", "q"), DELETE_PREPARATION_USAGE);
        Session session = sessionManager.getSession(parseSessionIndex(values.getFirst()));
        int index = parseItemIndex(values.get(1));
        int quantity = parseQuantity(values.get(2));
        PreparationItem item = session.getPreparation().removeItem(index, quantity);
        System.out.printf("Successfully removed: %dx %s from preparation%n", quantity, item);
        return session;
    }

    /**
     * Displays required totals and current availability, including a shortfall for insufficient items.
     *
     * @param session Session whose preparation requirements are displayed.
     * @param inventory Live inventory used to calculate availability and shortages.
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

    //@@author
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
     * Dispatches a student request command.
     *
     * @throws CommandException If the command arguments or requested change are invalid.
     */
    private void handleRequest(String command, String arguments, RequestManager requestManager,
                               Inventory inventory) throws CommandException {
        LOGGER.fine(() -> "Handling request command: " + command);
        switch (command) {
            case "add-r" -> addRequest(arguments, requestManager);
            case "delete-r" -> deleteRequest(arguments, requestManager);
            case "list-r" -> listRequests(arguments, requestManager, inventory);
            case "mark-r" -> markRequest(arguments, requestManager);
            default -> throw new CommandFormatException("Invalid request command.");
        }
    }

    /**
     * Parses and executes an add-request command.
     *
     * @param arguments Text following the {@code add-r} command word.
     * @param requestManager Request list to update.
     * @throws CommandException If the arguments or request values are invalid.
     */
    private void addRequest(String arguments, RequestManager requestManager) throws CommandException {
        List<String> expectedPrefixes = List.of("n", "i", "q", "d");
        if (!findPrefixes(arguments).equals(expectedPrefixes)) {
            throw new CommandFormatException(ADD_REQUEST_USAGE);
        }

        List<String> values = extractValues(arguments, expectedPrefixes, ADD_REQUEST_USAGE);
        assert values.size() == expectedPrefixes.size() : "Request values must match the prefixes";
        int quantity = parseQuantity(values.get(2));
        requestManager.addRequest(new Request(values.get(0), values.get(1), quantity, values.get(3)));
    }

    /**
     * Parses and executes a delete-request command.
     *
     * @param arguments Text following the {@code delete-r} command word.
     * @param requestManager Request list to update.
     * @throws CommandException If the index is malformed or outside the request list.
     */
    private void deleteRequest(String arguments, RequestManager requestManager) throws CommandException {
        String trimmedArguments = arguments.trim();
        if (trimmedArguments.isEmpty() || trimmedArguments.matches(".*\\s+.*")) {
            throw new CommandFormatException(DELETE_REQUEST_USAGE);
        }

        requestManager.deleteRequest(parseRequestIndex(trimmedArguments));
    }

    /**
     * Validates and executes a list-requests command.
     *
     * @param arguments Text following the {@code list-r} command word.
     * @param requestManager Request list to display.
     * @param inventory Inventory used to check stock availability.
     * @throws CommandFormatException If any arguments are supplied.
     */
    private void listRequests(String arguments, RequestManager requestManager, Inventory inventory)
            throws CommandFormatException {
        if (!arguments.isBlank()) {
            throw new CommandFormatException(LIST_REQUEST_USAGE);
        }
        requestManager.listRequests(inventory);
    }

    /**
     * Parses and executes a mark-request command.
     *
     * @param arguments Text following the {@code mark-r} command word.
     * @param requestManager Request list to update.
     * @throws CommandException If the index or status is invalid.
     */
    private void markRequest(String arguments, RequestManager requestManager) throws CommandException {
        String[] parts = arguments.trim().split("\\s+");
        if (parts.length != 2) {
            throw new CommandFormatException(MARK_REQUEST_USAGE);
        }
        requestManager.markRequest(parseRequestIndex(parts[0]), Request.normalizeStatus(parts[1]));
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
     * Validates the prefix sequence and extracts the values between prefixes.
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
        if (prefixIndex != expectedPrefixes.size() || prefixStarts.getFirst() != 0) {
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
                throw new InvalidQuantityException(InvalidQuantityException.NON_POSITIVE_MESSAGE);
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
            throw new InvalidLoanIndexException(e);
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

    /**
     * Parses a positive one-based request index.
     *
     * @param indexText Index text supplied by the user.
     * @return Parsed positive index.
     * @throws InvalidRequestIndexException If the index is not a positive integer.
     */
    private int parseRequestIndex(String indexText) throws InvalidRequestIndexException {
        try {
            int index = Integer.parseInt(indexText);
            if (index <= 0) {
                throw new InvalidRequestIndexException();
            }
            return index;
        } catch (NumberFormatException e) {
            throw new InvalidRequestIndexException();
        }
    }

    //@@author gavinlimsh
    /**
     * Dispatches broken-item commands after checking that startup supplied a persistent manager.
     *
     * @throws CommandException If command arguments or the requested operation are invalid.
     */
    private void handleBrokenItem(String command, String arguments, BrokenItemManager brokenItemManager,
            ContactManager contactManager) throws CommandException {
        if (brokenItemManager == null) {
            throw new CommandFormatException("Broken-item tracking is unavailable in this command context.");
        }
        switch (command) {
            case "add-b" -> addBrokenItem(arguments, brokenItemManager, contactManager);
            case "list-b" -> {
                if (!arguments.isBlank()) {
                    throw new CommandFormatException(LIST_BROKEN_USAGE);
                }
                Ui.showBrokenItems(brokenItemManager);
            }
            case "delete-b" -> {
                int index = parseBrokenItemIndex(arguments);
                Ui.showBrokenItemDeleted(brokenItemManager.deleteBrokenItem(index));
            }
            default -> throw new CommandFormatException("Invalid broken-item command.");
        }
    }

    /**
     * Resolves the optional contact and validates the entire report before inventory can change.
     * Prefix order follows the same strict rules as the other features.
     *
     * @throws CommandException If a prefix, value, contact index, or stock operation is invalid,
     *     or contact selection is requested without a contact manager.
     */
    private void addBrokenItem(String arguments, BrokenItemManager brokenItemManager,
            ContactManager contactManager) throws CommandException {
        List<String> prefixes = findPrefixes(arguments);
        if (!prefixes.equals(List.of("n", "c", "q", "f", "d"))
                && !prefixes.equals(List.of("n", "c", "q", "f", "d", "m"))) {
            throw new CommandFormatException(ADD_BROKEN_USAGE);
        }
        List<String> values = extractValues(arguments, prefixes, ADD_BROKEN_USAGE);
        int quantity = parseQuantity(values.get(BROKEN_QUANTITY_INDEX));
        Contact contact = null;
        if (values.size() > BROKEN_CONTACT_INDEX) {
            if (contactManager == null) {
                throw new CommandFormatException("Contact selection is unavailable in this command context.");
            }
            contact = contactManager.getContact(parseContactIndex(values.get(BROKEN_CONTACT_INDEX)));
        }
        BrokenItem item = new BrokenItem(values.get(BROKEN_NAME_INDEX), values.get(BROKEN_CATEGORY_INDEX),
                quantity, values.get(BROKEN_FAULT_INDEX), values.get(BROKEN_DATE_INDEX), contact);
        Ui.showBrokenItemAdded(brokenItemManager.addBrokenItem(item));
    }

    /**
     * Parses one report index, separating command syntax errors from invalid index values.
     *
     * @param arguments Text after delete-b.
     * @return Positive one-based report index.
     * @throws CommandFormatException If the index is missing or more than one argument is supplied.
     * @throws InvalidBrokenItemIndexException If the value is nonnumeric, nonpositive, or exceeds the integer limit.
     */
    private int parseBrokenItemIndex(String arguments) throws CommandException {
        String indexText = arguments.trim();
        if (indexText.isEmpty() || indexText.split("\\s+", 2).length != 1) {
            throw new CommandFormatException(DELETE_BROKEN_USAGE);
        }
        try {
            int index = Integer.parseInt(indexText);
            if (index <= 0) {
                throw new InvalidBrokenItemIndexException();
            }
            return index;
        } catch (NumberFormatException e) {
            throw new InvalidBrokenItemIndexException(e);
        }
    }
    //@@author gavinlimsh
}

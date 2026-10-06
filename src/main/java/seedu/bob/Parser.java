package seedu.bob;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.bob.exception.CommandException;
import seedu.bob.exception.CommandFormatException;
import seedu.bob.exception.InvalidHeadcountException;
import seedu.bob.exception.InvalidItemIndexException;
import seedu.bob.exception.InvalidQuantityException;
import seedu.bob.exception.InvalidSessionIndexException;

/**
 * Parses inventory and scheduling commands and dispatches them.
 */
public class Parser {

    private static final int DEFAULT_ITEM_QUANTITY = 1;
    private static final String ADD_ITEM_USAGE =
            "Invalid format. Use: add-i n/NAME c/CATEGORY [q/QUANTITY]";
    private static final String DELETE_ITEM_USAGE =
            "Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY";
    private static final String LIST_ITEM_USAGE = "Invalid format. Use: list-i";
    private static final String ADD_SESSION_USAGE = "Invalid format. Use: add-s n/NAME d/DATE "
            + "l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT";
    private static final String DELETE_SESSION_USAGE = "Invalid format. Use: delete-s INDEX";
    private static final String LIST_SESSION_USAGE = "Invalid format. Use: list-s";

    /** Matches a single-letter command prefix occurring at the start of a token. */
    private static final Pattern ARGUMENT_PREFIX_PATTERN = Pattern.compile("(?<!\\S)([A-Za-z])/");

    /**
     * Executes an inventory or scheduling command, or prints a message for an
     * unknown command. Expected user errors are displayed without ending the application.
     *
     * @param input Command text containing the required arguments.
     * @param inventory Inventory to query or update.
     * @param sessionManager SessionManager to schedule or modify lab sessions.
     * @throws NullPointerException If a required argument is {@code null}.
     */
    public void handleCommand(String input, Inventory inventory, SessionManager sessionManager) {
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
            default -> {
                System.out.println("Invalid command");
            }
        }
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
}

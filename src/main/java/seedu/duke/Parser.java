package seedu.duke;

/**
 * Parses inventory commands and dispatches them to an inventory.
 */
public class Parser {
    private static final int PREFIX_LENGTH = 2;

    /**
     * Executes an add-i, delete-i, or list command, or prints a message for an unknown command.
     * Expects add-i arguments in n/, c/, q/ order and delete-i arguments in c/, i/, q/ order.
     *
     * @param input Command text containing the required arguments.
     * @param inventory Inventory to query or update.
     * @throws IndexOutOfBoundsException If arguments or item positions are missing or invalid.
     * @throws NumberFormatException If a quantity or item position is not an integer.
     * @throws NullPointerException If the input is null or a deletion category does not exist.
     */
    public void handleCommand(String input, Inventory inventory) {
        String[] parts = input.trim().split(" ", 2);
        String command = parts[0];

        switch (command) {
            case "add-i" -> {
                // TODO: Validate add command arguments.
                String args = parts[1];
                int nameIdx = args.indexOf("n/");
                int categoryIdx = args.indexOf("c/");
                int quantityIdx = args.indexOf("q/");

                String name = args.substring(nameIdx + PREFIX_LENGTH, categoryIdx).trim();
                String category = args.substring(categoryIdx + PREFIX_LENGTH, quantityIdx).trim();
                int quantity = Integer.parseInt(args.substring(quantityIdx + PREFIX_LENGTH).trim());

                InventoryItem item = new InventoryItem(name, category, quantity);
                inventory.addItem(item);
            }
            case "delete-i" -> {
                // TODO: Validate delete command arguments.
                String args = parts[1];
                int categoryIdx = args.indexOf("c/");
                int itemIdx = args.indexOf("i/");
                int quantityIdx = args.indexOf("q/");

                String category = args.substring(categoryIdx + PREFIX_LENGTH, itemIdx).trim();
                int index = Integer.parseInt(args.substring(itemIdx + PREFIX_LENGTH, quantityIdx).trim());
                int quantity = Integer.parseInt(args.substring(quantityIdx + PREFIX_LENGTH).trim());

                inventory.removeItem(category, index, quantity);
            }
            case "list" -> {
                inventory.listItems();
            }
            default -> {
                // TODO: Provide more specific feedback for unknown commands.
                System.out.println("Invalid command");
            }
        }
    }
}

package seedu.duke;

public class Parser {
    private static final int PREFIX_LENGTH = 2;

    public void handleCommand(String input, Inventory inventory) {
        String[] parts = input.trim().split(" ", 2);
        String cmd = parts[0];

        switch (cmd) {
            case "add-i" -> {
                //TODO Error handling
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
                //TODO Error handling
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
                //TODO Error handling
                System.out.println("Invalid command");
            }
        }
    }
}

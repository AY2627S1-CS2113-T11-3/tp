package seedu.duke;

/**
 * Parses inventory and scheduling commands and dispatches them.
 */
public class Parser {
    private static final int PREFIX_LENGTH = 2;

    /**
     * Executes an add-i, delete-i, or list command, or prints a message for an unknown command.
     * Expects add-i arguments in n/, c/, q/ order and delete-i arguments in c/, i/, q/ order.
     *
     * @param input Command text containing the required arguments.
     * @param inventory Inventory to query or update.
     * @param sessionManager SessionManager to schedule or modify lab sessions.
     * @throws IndexOutOfBoundsException If arguments or item positions are missing or invalid.
     * @throws NumberFormatException If a quantity or item position is not an integer.
     * @throws NullPointerException If the input is null or a deletion category does not exist.
     */
    public void handleCommand(String input, Inventory inventory, SessionManager sessionManager) {
        String[] parts = input.trim().split(" ", 2);
        String command = parts[0];

        switch (command) {
            case "add-i" -> {
                try {
                    String args = parts[1];
                    int nameIdx = args.indexOf("n/");
                    int categoryIdx = args.indexOf("c/");
                    int quantityIdx = args.indexOf("q/");

                    String name = args.substring(nameIdx + PREFIX_LENGTH, categoryIdx).trim();
                    String category = args.substring(categoryIdx + PREFIX_LENGTH, quantityIdx).trim();
                    int quantity = Integer.parseInt(args.substring(quantityIdx + PREFIX_LENGTH).trim());

                    InventoryItem item = new InventoryItem(name, category, quantity);
                    inventory.addItem(item);
                } catch (Exception e) {
                    System.out.println("Invalid format. Use: add-i n/NAME c/CATEGORY q/QUANTITY");
                }
            }
            case "delete-i" -> {
                try {
                    String args = parts[1];
                    int categoryIdx = args.indexOf("c/");
                    int itemIdx = args.indexOf("i/");
                    int quantityIdx = args.indexOf("q/");

                    String category = args.substring(categoryIdx + PREFIX_LENGTH, itemIdx).trim();
                    int index = Integer.parseInt(args.substring(itemIdx + PREFIX_LENGTH, quantityIdx).trim());
                    int quantity = Integer.parseInt(args.substring(quantityIdx + PREFIX_LENGTH).trim());

                    inventory.removeItem(category, index, quantity);
                } catch (Exception e) {
                    System.out.println("Invalid format. Use: delete-i c/CATEGORY i/INDEX_OF_ITEM q/QUANTITY");
                }
            }
            case "list-i" -> {
                inventory.listItems();
            }
            case "add-s" -> {
                try {
                    String args = parts[1];
                    int nIdx = args.indexOf("n/");
                    int dIdx = args.indexOf("d/");
                    int lIdx = args.indexOf("l/");
                    int sIdx = args.indexOf("s/");
                    int eIdx = args.indexOf("e/");
                    int pIdx = args.indexOf("p/");

                    if (nIdx == -1 || dIdx == -1 || lIdx == -1 || sIdx == -1 || eIdx == -1 || pIdx == -1) {
                        throw new IllegalArgumentException();
                    }

                    String name = args.substring(nIdx + PREFIX_LENGTH, dIdx).trim();
                    String date = args.substring(dIdx + PREFIX_LENGTH, lIdx).trim();
                    String location = args.substring(lIdx + PREFIX_LENGTH, sIdx).trim();
                    String startTime = args.substring(sIdx + PREFIX_LENGTH, eIdx).trim();
                    String endTime = args.substring(eIdx + PREFIX_LENGTH, pIdx).trim();
                    int headcount = Integer.parseInt(args.substring(pIdx + PREFIX_LENGTH).trim());

                    Session session = new Session(name, date, location, startTime, endTime, headcount);
                    sessionManager.addSession(session);
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage() != null ? e.getMessage() : "Invalid format. Use: add-s n/NAME d/DATE l/LOCATION s/STARTTIME e/ENDTIME p/HEADCOUNT");
                } catch (Exception e) {
                    System.out.println("Error parsing add-s command. Please check your format.");
                }
            }
            case "delete-s" -> {
                try {
                    int index = Integer.parseInt(parts[1].trim());
                    sessionManager.deleteSession(index);
                } catch (Exception e) {
                    System.out.println("Invalid format. Use: delete-s INDEX");
                }
            }
            case "list-s" -> {
                sessionManager.listSessions();
            }
            default -> {
                System.out.println("Invalid command");
            }
        }
    }
}
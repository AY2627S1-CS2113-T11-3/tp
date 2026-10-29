//@@author J-unn-y
package seedu.bob;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Searches inventory and active loans for items whose name or category contains a keyword.
 * Searching only reads stock and loans; it never changes them.
 */
public class ItemFinder {

    /** Records diagnostics at FINE level so normal console output stays unchanged. */
    private static final Logger LOGGER = Logger.getLogger(ItemFinder.class.getName());

    /**
     * Prevents instantiation because the finder keeps no state between searches.
     */
    private ItemFinder() {
    }

    /**
     * Returns every item whose name or category contains the keyword, ignoring case.
     * Items in stock are listed first, in inventory display order. Items whose every unit is on loan,
     * and which have therefore left inventory, follow in loan order.
     *
     * @param keyword Nonblank text to look for.
     * @param inventory Current stock.
     * @param loanManager Active loans backed by the same inventory.
     * @return Immutable results, one per name/category pair.
     * @throws NullPointerException If any argument is null.
     * @throws IllegalArgumentException If the keyword is blank.
     */
    public static List<ItemLookupEntry> findItems(String keyword, Inventory inventory, LoanManager loanManager) {
        Objects.requireNonNull(keyword, "Search keyword must not be null");
        Objects.requireNonNull(inventory, "Inventory must not be null");
        Objects.requireNonNull(loanManager, "Loan manager must not be null");
        if (keyword.isBlank()) {
            throw new IllegalArgumentException("Search keyword must not be blank");
        }
        String searchText = keyword.strip();

        // Use the same case-insensitive identity as inventory, including Unicode case variants.
        List<Tally> tallies = new ArrayList<>();
        for (InventoryItem item : inventory.getItemSnapshots()) {
            if (matches(item.getName(), item.getCategory(), searchText)) {
                tallyFor(tallies, item.getName(), item.getCategory()).availableQuantity = item.getQuantity();
            }
        }
        for (Loan loan : loanManager.getLoans()) {
            if (matches(loan.getName(), loan.getCategory(), searchText)) {
                tallyFor(tallies, loan.getName(), loan.getCategory()).addLoanedQuantity(loan.getQuantity());
            }
        }

        List<ItemLookupEntry> results = new ArrayList<>();
        for (Tally tally : tallies) {
            results.add(new ItemLookupEntry(tally.name, tally.category,
                    tally.availableQuantity, tally.loanedQuantity));
        }
        LOGGER.fine(() -> "Found " + results.size() + " item(s) matching \"" + keyword.strip() + "\"");
        return List.copyOf(results);
    }

    /**
     * Returns whether the search text occurs in the item's name or category, ignoring case.
     */
    private static boolean matches(String name, String category, String searchText) {
        return containsIgnoringCase(name, searchText) || containsIgnoringCase(category, searchText);
    }

    /**
     * Matches substrings without locale-dependent conversion or multi-character case expansion.
     */
    private static boolean containsIgnoringCase(String text, String searchText) {
        for (int start = 0; start <= text.length() - searchText.length(); start++) {
            if (text.regionMatches(true, start, searchText, 0, searchText.length())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the running totals for a name/category pair, creating them on first use.
     * The first spelling seen, normally the inventory's, is kept for display.
     */
    private static Tally tallyFor(List<Tally> tallies, String name, String category) {
        for (Tally tally : tallies) {
            if (tally.name.equalsIgnoreCase(name) && tally.category.equalsIgnoreCase(category)) {
                return tally;
            }
        }
        Tally tally = new Tally(name, category);
        tallies.add(tally);
        return tally;
    }

    /**
     * Accumulates the quantities of one name/category pair while a search is in progress.
     */
    private static class Tally {
        private final String name;
        private final String category;
        private int availableQuantity;
        private int loanedQuantity;

        private Tally(String name, String category) {
            this.name = name;
            this.category = category;
        }

        /**
         * Adds loaned units, capping the total at {@link Integer#MAX_VALUE} as inventory totals do.
         */
        private void addLoanedQuantity(int quantity) {
            loanedQuantity = (int) Math.min((long) loanedQuantity + quantity, Integer.MAX_VALUE);
        }
    }
}

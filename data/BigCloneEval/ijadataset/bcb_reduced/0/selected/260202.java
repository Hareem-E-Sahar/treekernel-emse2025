package org.bejug.javacareers.common.ajax;

/**
 * Utility methods for the AJAX Text component (and possibly other components
 * we'll develop next)
 *
 * @author Tor Norbye
 */
public class Utilities {

    /**
     * Creates a new instance of Utilities
     */
    private Utilities() {
    }

    /**
     * Perform a case insensitive binary search for the key in the data string.
     * Not an exact binary search since we return the index of the closest match
     * rather than returning an actual match.
     *
     * @param data       an array of Strings containing the data.
     * @param key        a String containing the key to search.
     * @param ignoreCase whether to ignore the case or not.
     * @return an int representing the closest index.
     */
    private static int findClosestIndex(String[] data, String key, boolean ignoreCase) {
        int low = 0;
        int high = data.length - 1;
        int middle = -1;
        while (high > low) {
            middle = (low + high) / 2;
            int result;
            if (ignoreCase) {
                result = key.compareToIgnoreCase(data[middle]);
            } else {
                result = key.compareTo(data[middle]);
            }
            if (result == 0) {
                return middle;
            } else if (result < 0) {
                high = middle;
            } else if (low != middle) {
                low = middle;
            } else {
                return high;
            }
        }
        return middle;
    }

    /**
     * Return (via the result object) a short completion list from the given data using
     * the given prefix
     *
     * @param sortedData A sorted array of Strings we want to pick completion results
     *                   from
     * @param prefix     A prefix of some of the strings in the sortedData that the
     *                   user has typed so far
     * @param result     A result object that will be populated with completion results
     *                   by this method
     */
    public static void addMatchingItems(String[] sortedData, String prefix, CompletionResult result) {
        int index;
        if (prefix.length() > 0) {
            boolean caseInsensitive = true;
            index = findClosestIndex(sortedData, prefix, caseInsensitive);
        } else {
            index = 0;
        }
        if (index == -1) {
            index = 0;
        }
        for (int i = 0; i < PhaseListener.MAX_RESULTS_RETURNED; i++) {
            if (index >= sortedData.length) {
                break;
            }
            result.addItem(sortedData[index]);
            index++;
        }
    }
}

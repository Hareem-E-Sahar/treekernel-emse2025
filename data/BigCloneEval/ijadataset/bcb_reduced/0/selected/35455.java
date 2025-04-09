package InternetHax;

/**
 *app factory generates items and is responsable for "drops" and proababilty-related app functions
 * @author LoginError
 */
public class AppFactory {

    int[] dropTable;

    public AppFactory() {
    }

    public App dropRandom() {
        return null;
    }

    public int[] generateDropTable(int[] drops) {
        for (int i = 0; i < drops.length; i++) {
            if (i != 0) {
                drops[i] = drops[i] + drops[i - 1];
            }
        }
        return drops;
    }

    public int getDropIndex(int number, int[] dropTable) {
        return binarySearch(dropTable, number, 0, dropTable.length);
    }

    /**
     * Given a probababilty, searches for and returns the index of the item that will be dropped
     * @param table
     * @param target
     * @param lowerBound
     * @param upperBound
     * @return
     */
    private int binarySearch(int[] table, long target, int lowerBound, int upperBound) {
        int currentIndex;
        currentIndex = (lowerBound + upperBound) / 2;
        if (table[currentIndex] == target || ((table[currentIndex] > target && target > (currentIndex != 0 ? table[currentIndex - 1] : 0)))) {
            return currentIndex;
        } else if (lowerBound > upperBound) {
            return -1;
        } else {
            if (table[currentIndex] < target) {
                return binarySearch(table, target, currentIndex + 1, upperBound);
            } else {
                return binarySearch(table, target, lowerBound, currentIndex - 1);
            }
        }
    }

    public void init() throws GameBreakingException {
        try {
            String dropValues = Toolbox.getStringFromFile(this, "/DropValues.txt");
            String[] weights = Toolbox.splitString(dropValues, "\n");
            dropTable = new int[weights.length];
            for (int i = 0; i < weights.length; i++) {
                dropTable[i] = Integer.parseInt(weights[i]);
            }
        } catch (Exception ex) {
            throw new GameBreakingException(ex.toString());
        }
    }
}

public class Test {    public static String getConceptData(boolean isSpecial, String id) {
        long low = 0;
        long conceptIndexFileSize = getConceptIndexFileSize(isSpecial);
        long high = conceptIndexFileSize;
        while (low <= high) {
            long mid = (low + high) / 2;
            if (conceptIndexFileSize - 1 <= mid) {
                return null;
            }
            long conceptDataFP = getConceptDataFp(mid * 10, isSpecial);
            if (conceptDataFP == -1) {
                return null;
            }
            String conceptData = getConceptData(conceptDataFP, isSpecial);
            if (conceptData == null) {
                return null;
            }
            String[] lines = conceptData.split("\t");
            String searchedID = lines[0];
            if (searchedID.compareTo(id) == 0) {
                return conceptData;
            } else if (0 < searchedID.compareTo(id)) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return null;
    }
}
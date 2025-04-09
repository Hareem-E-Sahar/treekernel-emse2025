package ee.meelisolev.filesizesorter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Meelis
 */
public class FileSizeSorterDocument {

    private List<FileSizeSorterItem> items;

    /**
     * 
     */
    public FileSizeSorterDocument() {
        items = new ArrayList<FileSizeSorterItem>();
    }

    /**
     * 
     * @return
     */
    public int size() {
        return items.size();
    }

    /**
     * 
     * @param size
     * @param fileName
     */
    public void add(long size, String fileName) {
        items.add(new FileSizeSorterItem(size, fileName));
    }

    /**
     * 
     * @param index
     * @return
     */
    public FileSizeSorterItem get(int index) {
        return items.get(index);
    }

    /**
     * 
     */
    public void sort() {
        FileSizeSorterItem[] itemsArray = items.toArray(new FileSizeSorterItem[0]);
        Arrays.sort(itemsArray);
        reverse(itemsArray);
        items = Arrays.asList(itemsArray);
    }

    /**
     * 
     * @param size
     * @return
     */
    public int getFirstIndexBySize(long size) {
        int left = 0;
        int right = items.size() - 1;
        while (left <= right) {
            int center = (left + right) / 2;
            if (items.get(center).getSize() == size) {
                while (center > 0 && items.get(center - 1).getSize() == size) {
                    center--;
                }
                return center;
            } else if (items.get(center).getSize() > size) {
                left = center + 1;
            } else {
                right = center - 1;
            }
        }
        return -1;
    }

    /**
     * 
     * @param index
     * @return
     */
    public boolean isMultipleFilesIndex(int index) {
        if (index > 0 && items.get(index).getSize() == items.get(index - 1).getSize()) {
            return true;
        } else if (index < items.size() - 1 && items.get(index).getSize() == items.get(index + 1).getSize()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 
     * @param size
     * @return
     */
    public boolean hasMultipleFiles(long size) {
        int index = getFirstIndexBySize(size);
        return isMultipleFilesIndex(index);
    }

    /**
     * 
     * @param array
     */
    public static void reverse(Object[] array) {
        int left = 0;
        int right = array.length - 1;
        while (left < right) {
            Object temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left++;
            right--;
        }
    }
}

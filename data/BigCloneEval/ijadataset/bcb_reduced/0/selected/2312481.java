package c.io;

import c.io.Field;
import java.util.ArrayList;

/**
 *
 * @author sam
 */
public class SortedFieldList {

    private java.util.ArrayList<Field> sorted;

    public SortedFieldList(Field e) {
        sorted = new ArrayList<Field>();
        sorted.add(e);
    }

    public Field get(int i) {
        return sorted.get(i);
    }

    public int size() {
        return sorted.size();
    }

    public int findSort(String key) {
        int low = 0;
        int high = sorted.size();
        int mid = (low + high) / 2;
        int oldmid = -2;
        int comp;
        while ((mid = (high + low) / 2) != oldmid) {
            comp = sorted.get(mid).compareTo(key);
            if (comp < 0) {
                low = mid;
            } else if (comp > 0) {
                high = mid;
            } else return mid;
            oldmid = mid;
        }
        return -1;
    }

    public Field get(String key) {
        int find = findSort(key);
        if (find == -1) return null;
        return sorted.get(find);
    }

    public boolean addSort(Field element) {
        int low = 0;
        int high = sorted.size();
        int mid = (low + high) / 2;
        int oldmid = mid;
        int comp;
        int count = 0;
        do {
            oldmid = mid;
            count++;
            comp = element.compareTo(sorted.get(mid));
            if (comp > 0) {
                low = mid;
            } else if (comp < 0) {
                high = mid;
            } else return false;
        } while ((mid = (low + high) / 2) != oldmid);
        if (comp > 0) {
            sorted.add(mid + 1, element);
        } else sorted.add(mid, element);
        return true;
    }
}

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Dec 2, 2004
 * Time: 7:54:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class SortTransformation extends Transformation {

    private ColumnStateList mColumnStateList;

    SortTransformation(BaseTransformation chain) {
        super(chain);
    }

    public synchronized void sort() {
        if (mColumnStateList == null || mColumnStateList.size() == 0) return;
        shuttlesort(new ArrayList(m_row_xform), m_row_xform, 0, m_row_xform.size());
    }

    public void setSortList(ColumnStateList columnStateList) {
        mColumnStateList = columnStateList;
    }

    protected void postInitialize() {
        sort();
    }

    private void shuttlesort(List from, List to, int low, int high) {
        if (high - low < 2) {
            return;
        }
        int middle = (low + high) / 2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);
        int p = low;
        int q = middle;
        if (high - low >= 4 && adjustCompare(getInt(from, middle - 1), getInt(from, middle)) <= 0) {
            for (int i = low; i < high; i++) {
                to.set(i, (Integer) from.get(i));
            }
            return;
        }
        for (int i = low; i < high; i++) {
            if (q >= high || (p < middle && adjustCompare(getInt(from, p), getInt(from, q)) <= 0)) {
                to.set(i, from.get(p++));
            } else {
                to.set(i, from.get(q++));
            }
        }
    }

    private int adjustCompare(int r1, int r2) {
        return m_tm.compare(r1, r2, mColumnStateList);
    }

    public synchronized int insert(Object newObj) {
        super.insert(newObj);
        sort();
        return findRow(newObj);
    }
}

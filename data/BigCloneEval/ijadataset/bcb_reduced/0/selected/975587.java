package com.be.table;

import java.util.*;
import com.be.vo.PGDatabaseVO;
import com.util.comparator.*;
import com.util.table.*;

public class PGDatabaseModel implements ITableModel {

    protected PGDatabaseVO fTotal;

    protected PGDatabaseVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public PGDatabaseModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (PGDatabaseVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        PGDatabaseVO vo = fTableData[fSortOrder[pRow]];
        if ("datname".equals(pColumn)) return vo.getDatname();
        if ("datdba".equals(pColumn)) return new Long(vo.getDatdba());
        if ("encoding".equals(pColumn)) return new Long(vo.getEncoding());
        if ("datistemplate".equals(pColumn)) return new Boolean(vo.getDatistemplate());
        if ("datallowconn".equals(pColumn)) return new Boolean(vo.getDatallowconn());
        if ("datlastsysoid".equals(pColumn)) return new Long(vo.getDatlastsysoid());
        if ("datvacuumxid".equals(pColumn)) return vo.getDatvacuumxid();
        if ("datfrozenxid".equals(pColumn)) return vo.getDatfrozenxid();
        if ("dattablespace".equals(pColumn)) return new Long(vo.getDattablespace());
        if ("datconfig".equals(pColumn)) return vo.getDatconfig();
        if ("datacl".equals(pColumn)) return vo.getDatacl();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("datname".equals(pColumn)) return fTotal.getDatname();
            if ("datdba".equals(pColumn)) return new Long(fTotal.getDatdba());
            if ("encoding".equals(pColumn)) return new Long(fTotal.getEncoding());
            if ("datistemplate".equals(pColumn)) return new Boolean(fTotal.getDatistemplate());
            if ("datallowconn".equals(pColumn)) return new Boolean(fTotal.getDatallowconn());
            if ("datlastsysoid".equals(pColumn)) return new Long(fTotal.getDatlastsysoid());
            if ("datvacuumxid".equals(pColumn)) return fTotal.getDatvacuumxid();
            if ("datfrozenxid".equals(pColumn)) return fTotal.getDatfrozenxid();
            if ("dattablespace".equals(pColumn)) return new Long(fTotal.getDattablespace());
            if ("datconfig".equals(pColumn)) return fTotal.getDatconfig();
            if ("datacl".equals(pColumn)) return fTotal.getDatacl();
        }
        return null;
    }

    public void sort(String pColumn, String pSortDirection) {
        boolean up = true;
        if (!"u".equals(pSortDirection)) {
            up = false;
        }
        if ("datname".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDatname();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("datdba".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDatdba();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("encoding".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getEncoding();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("datistemplate".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDatistemplate();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
        }
        if ("datallowconn".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDatallowconn();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
        }
        if ("datlastsysoid".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDatlastsysoid();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("datvacuumxid".equals(pColumn)) {
            fComparator = new ObjectComparator();
            Object[] temp = new Object[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDatvacuumxid();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("datfrozenxid".equals(pColumn)) {
            fComparator = new ObjectComparator();
            Object[] temp = new Object[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDatfrozenxid();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("dattablespace".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDattablespace();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("datconfig".equals(pColumn)) {
            fComparator = new ObjectComparator();
            Object[] temp = new Object[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDatconfig();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("datacl".equals(pColumn)) {
            fComparator = new ObjectComparator();
            Object[] temp = new Object[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDatacl();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        fSortedColumn = pColumn;
    }

    @SuppressWarnings("unchecked")
    private void sort(Object[] a, int lo0, int hi0, boolean up) {
        int lo = lo0;
        int hi = hi0;
        if (lo >= hi) {
            return;
        }
        int mid = (lo + hi) / 2;
        sort(a, lo, mid, up);
        sort(a, mid + 1, hi, up);
        int end_lo = mid;
        int start_hi = mid + 1;
        while ((lo <= end_lo) && (start_hi <= hi)) {
            boolean isChange;
            if (up) {
                isChange = (fComparator.compare(a[fSortOrder[lo]], a[fSortOrder[start_hi]]) <= 0);
            } else {
                isChange = (fComparator.compare(a[fSortOrder[lo]], a[fSortOrder[start_hi]]) >= 0);
            }
            if (isChange) {
                lo++;
            } else {
                int T = fSortOrder[start_hi];
                for (int k = start_hi - 1; k >= lo; k--) {
                    fSortOrder[k + 1] = fSortOrder[k];
                }
                fSortOrder[lo] = T;
                lo++;
                end_lo++;
                start_hi++;
            }
        }
    }

    public String[] getSortedColumns() {
        if (fSortedColumn != null) {
            return new String[] { fSortedColumn };
        } else {
            return null;
        }
    }

    public Object getRowAt(int index) {
        return (fTableData != null ? fTableData[fSortOrder[index]] : null);
    }

    public Object getTotalRow() {
        return fTotal;
    }

    public int getRowCount() {
        return (fTableData != null) ? fTableData.length : 0;
    }
}

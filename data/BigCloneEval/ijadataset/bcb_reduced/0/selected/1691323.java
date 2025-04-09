package com.creditors.table;

import java.util.*;
import com.creditors.vo.ProcessLogVO;
import com.util.comparator.*;
import com.util.table.*;

public class ProcessLogModel implements ITableModel {

    protected ProcessLogVO fTotal;

    protected ProcessLogVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public ProcessLogModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (ProcessLogVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        ProcessLogVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("trxID".equals(pColumn)) return new Long(vo.getTrxID());
        if ("typeID".equals(pColumn)) return new Long(vo.getTypeID());
        if ("typeName".equals(pColumn)) return vo.getTypeName();
        if ("statusID".equals(pColumn)) return new Long(vo.getStatusID());
        if ("statusName".equals(pColumn)) return vo.getStatusName();
        if ("dbUser".equals(pColumn)) return vo.getDbUser();
        if ("userID".equals(pColumn)) return new Long(vo.getUserID());
        if ("modified".equals(pColumn)) return vo.getModified();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("trxID".equals(pColumn)) return new Long(fTotal.getTrxID());
            if ("typeID".equals(pColumn)) return new Long(fTotal.getTypeID());
            if ("typeName".equals(pColumn)) return fTotal.getTypeName();
            if ("statusID".equals(pColumn)) return new Long(fTotal.getStatusID());
            if ("statusName".equals(pColumn)) return fTotal.getStatusName();
            if ("dbUser".equals(pColumn)) return fTotal.getDbUser();
            if ("userID".equals(pColumn)) return new Long(fTotal.getUserID());
            if ("modified".equals(pColumn)) return fTotal.getModified();
        }
        return null;
    }

    public void sort(String pColumn, String pSortDirection) {
        boolean up = true;
        if (!"u".equals(pSortDirection)) {
            up = false;
        }
        if ("id".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getId();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("trxID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTrxID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("typeID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTypeID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("typeName".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTypeName();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("statusID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getStatusID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("statusName".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getStatusName();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("dbUser".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDbUser();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("userID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getUserID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("modified".equals(pColumn)) {
            fComparator = new TimestampComparator();
            java.sql.Timestamp[] temp = new java.sql.Timestamp[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getModified();
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

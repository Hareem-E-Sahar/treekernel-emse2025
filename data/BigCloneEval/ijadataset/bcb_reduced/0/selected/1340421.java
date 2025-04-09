package com.debitors.table;

import java.util.*;
import com.debitors.vo.OrderItemBatchVO;
import com.util.comparator.*;
import com.util.table.*;

public class OrderItemBatchModel implements ITableModel {

    protected OrderItemBatchVO fTotal;

    protected OrderItemBatchVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public OrderItemBatchModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (OrderItemBatchVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        OrderItemBatchVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("orderID".equals(pColumn)) return new Long(vo.getOrderID());
        if ("itemID".equals(pColumn)) return new Long(vo.getItemID());
        if ("itemDetailID".equals(pColumn)) return new Long(vo.getItemDetailID());
        if ("itemBatchNr".equals(pColumn)) return vo.getItemBatchNr();
        if ("batchStart".equals(pColumn)) return new Long(vo.getBatchStart());
        if ("batchEnd".equals(pColumn)) return new Long(vo.getBatchEnd());
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("orderID".equals(pColumn)) return new Long(fTotal.getOrderID());
            if ("itemID".equals(pColumn)) return new Long(fTotal.getItemID());
            if ("itemDetailID".equals(pColumn)) return new Long(fTotal.getItemDetailID());
            if ("itemBatchNr".equals(pColumn)) return fTotal.getItemBatchNr();
            if ("batchStart".equals(pColumn)) return new Long(fTotal.getBatchStart());
            if ("batchEnd".equals(pColumn)) return new Long(fTotal.getBatchEnd());
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
        if ("orderID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getOrderID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("itemID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getItemID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("itemDetailID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getItemDetailID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("itemBatchNr".equals(pColumn)) {
            fComparator = new ObjectComparator();
            Object[] temp = new Object[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getItemBatchNr();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("batchStart".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBatchStart();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("batchEnd".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBatchEnd();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
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

package com.mat.table;

import java.util.*;
import com.mat.vo.StockReceiptVO;
import com.util.comparator.*;
import com.util.table.*;

public class StockReceiptModel implements ITableModel {

    protected StockReceiptVO fTotal;

    protected StockReceiptVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public StockReceiptModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (StockReceiptVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        StockReceiptVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("paymentID".equals(pColumn)) return new Long(vo.getPaymentID());
        if ("paymentType".equals(pColumn)) return new Long(vo.getPaymentType());
        if ("itemID".equals(pColumn)) return new Long(vo.getItemID());
        if ("locationID".equals(pColumn)) return new Long(vo.getLocationID());
        if ("supplierID".equals(pColumn)) return new Long(vo.getSupplierID());
        if ("count".equals(pColumn)) return new Long(vo.getCount());
        if ("price".equals(pColumn)) return vo.getPrice();
        if ("vatID".equals(pColumn)) return new Long(vo.getVatID());
        if ("modified".equals(pColumn)) return vo.getModified();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("paymentID".equals(pColumn)) return new Long(fTotal.getPaymentID());
            if ("paymentType".equals(pColumn)) return new Long(fTotal.getPaymentType());
            if ("itemID".equals(pColumn)) return new Long(fTotal.getItemID());
            if ("locationID".equals(pColumn)) return new Long(fTotal.getLocationID());
            if ("supplierID".equals(pColumn)) return new Long(fTotal.getSupplierID());
            if ("count".equals(pColumn)) return new Long(fTotal.getCount());
            if ("price".equals(pColumn)) return fTotal.getPrice();
            if ("vatID".equals(pColumn)) return new Long(fTotal.getVatID());
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
        if ("paymentID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPaymentID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("paymentType".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPaymentType();
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
        if ("locationID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getLocationID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("supplierID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getSupplierID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("count".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCount();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("price".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPrice();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("vatID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getVatID();
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

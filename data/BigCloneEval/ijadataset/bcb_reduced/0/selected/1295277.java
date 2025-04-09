package com.be.table;

import java.util.*;
import com.be.vo.MktFXRateVO;
import com.util.comparator.*;
import com.util.table.*;

public class MktFXRateModel implements ITableModel {

    protected MktFXRateVO fTotal;

    protected MktFXRateVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public MktFXRateModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (MktFXRateVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        MktFXRateVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("unitCurrencyID".equals(pColumn)) return new Short(vo.getUnitCurrencyID());
        if ("priceCurrencyID".equals(pColumn)) return new Short(vo.getPriceCurrencyID());
        if ("exchangeRate".equals(pColumn)) return new Double(vo.getExchangeRate());
        if ("tradeDate".equals(pColumn)) return vo.getTradeDate();
        if ("modified".equals(pColumn)) return vo.getModified();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("unitCurrencyID".equals(pColumn)) return new Short(fTotal.getUnitCurrencyID());
            if ("priceCurrencyID".equals(pColumn)) return new Short(fTotal.getPriceCurrencyID());
            if ("exchangeRate".equals(pColumn)) return new Double(fTotal.getExchangeRate());
            if ("tradeDate".equals(pColumn)) return fTotal.getTradeDate();
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
        if ("unitCurrencyID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            short[] temp = new short[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getUnitCurrencyID();
            }
            fSortOrder = sorter.sortShort(temp, fSortOrder, up);
        }
        if ("priceCurrencyID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            short[] temp = new short[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPriceCurrencyID();
            }
            fSortOrder = sorter.sortShort(temp, fSortOrder, up);
        }
        if ("exchangeRate".equals(pColumn)) {
            Sorter sorter = new Sorter();
            double[] temp = new double[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getExchangeRate();
            }
            fSortOrder = sorter.sortDouble(temp, fSortOrder, up);
        }
        if ("tradeDate".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTradeDate();
            }
            sort(temp, 0, temp.length - 1, up);
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

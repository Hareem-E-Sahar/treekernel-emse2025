package com.be.table;

import java.util.*;
import com.be.vo.LedgerVO;
import com.util.comparator.*;
import com.util.table.*;

public class LedgerModel implements ITableModel {

    protected LedgerVO fTotal;

    protected LedgerVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public LedgerModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (LedgerVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        LedgerVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("accountID".equals(pColumn)) return vo.getAccountID();
        if ("name".equals(pColumn)) return vo.getName();
        if ("type".equals(pColumn)) return vo.getType();
        if ("allowNeg".equals(pColumn)) return new Boolean(vo.getAllowNeg());
        if ("amount".equals(pColumn)) return vo.getAmount();
        if ("currencyID".equals(pColumn)) return new Short(vo.getCurrencyID());
        if ("exchangeRate".equals(pColumn)) return new Double(vo.getExchangeRate());
        if ("amountReal".equals(pColumn)) return vo.getAmountReal();
        if ("modified".equals(pColumn)) return vo.getModified();
        if ("validFrom".equals(pColumn)) return vo.getValidFrom();
        if ("validTo".equals(pColumn)) return vo.getValidTo();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("accountID".equals(pColumn)) return fTotal.getAccountID();
            if ("name".equals(pColumn)) return fTotal.getName();
            if ("type".equals(pColumn)) return fTotal.getType();
            if ("allowNeg".equals(pColumn)) return new Boolean(fTotal.getAllowNeg());
            if ("amount".equals(pColumn)) return fTotal.getAmount();
            if ("currencyID".equals(pColumn)) return new Short(fTotal.getCurrencyID());
            if ("exchangeRate".equals(pColumn)) return new Double(fTotal.getExchangeRate());
            if ("amountReal".equals(pColumn)) return fTotal.getAmountReal();
            if ("modified".equals(pColumn)) return fTotal.getModified();
            if ("validFrom".equals(pColumn)) return fTotal.getValidFrom();
            if ("validTo".equals(pColumn)) return fTotal.getValidTo();
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
        if ("accountID".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAccountID();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("name".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getName();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("type".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getType();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("allowNeg".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAllowNeg();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
        }
        if ("amount".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAmount();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("currencyID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            short[] temp = new short[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCurrencyID();
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
        if ("amountReal".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAmountReal();
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
        if ("validFrom".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getValidFrom();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("validTo".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getValidTo();
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

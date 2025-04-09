package com.be.table;

import java.util.*;
import com.be.vo.LedgerHistSaldoVO;
import com.util.comparator.*;
import com.util.table.*;

public class LedgerHistSaldoModel implements ITableModel {

    protected LedgerHistSaldoVO fTotal;

    protected LedgerHistSaldoVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public LedgerHistSaldoModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (LedgerHistSaldoVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        LedgerHistSaldoVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("tstamp".equals(pColumn)) return vo.getTstamp();
        if ("dmType".equals(pColumn)) return new Long(vo.getDmType());
        if ("dmSubType".equals(pColumn)) return new Long(vo.getDmSubType());
        if ("evalStart".equals(pColumn)) return vo.getEvalStart();
        if ("evalDate".equals(pColumn)) return vo.getEvalDate();
        if ("ledgerID".equals(pColumn)) return new Long(vo.getLedgerID());
        if ("accountID".equals(pColumn)) return vo.getAccountID();
        if ("ledgerName".equals(pColumn)) return vo.getLedgerName();
        if ("ledgerType".equals(pColumn)) return vo.getLedgerType();
        if ("amountStart".equals(pColumn)) return vo.getAmountStart();
        if ("amountPrevious".equals(pColumn)) return vo.getAmountPrevious();
        if ("amountDebit".equals(pColumn)) return vo.getAmountDebit();
        if ("amountCredit".equals(pColumn)) return vo.getAmountCredit();
        if ("amount".equals(pColumn)) return vo.getAmount();
        if ("currencyID".equals(pColumn)) return new Short(vo.getCurrencyID());
        if ("exchangeRate".equals(pColumn)) return new Double(vo.getExchangeRate());
        if ("amountRef".equals(pColumn)) return vo.getAmountRef();
        if ("validFrom".equals(pColumn)) return vo.getValidFrom();
        if ("validTo".equals(pColumn)) return vo.getValidTo();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("tstamp".equals(pColumn)) return fTotal.getTstamp();
            if ("dmType".equals(pColumn)) return new Long(fTotal.getDmType());
            if ("dmSubType".equals(pColumn)) return new Long(fTotal.getDmSubType());
            if ("evalStart".equals(pColumn)) return fTotal.getEvalStart();
            if ("evalDate".equals(pColumn)) return fTotal.getEvalDate();
            if ("ledgerID".equals(pColumn)) return new Long(fTotal.getLedgerID());
            if ("accountID".equals(pColumn)) return fTotal.getAccountID();
            if ("ledgerName".equals(pColumn)) return fTotal.getLedgerName();
            if ("ledgerType".equals(pColumn)) return fTotal.getLedgerType();
            if ("amountStart".equals(pColumn)) return fTotal.getAmountStart();
            if ("amountPrevious".equals(pColumn)) return fTotal.getAmountPrevious();
            if ("amountDebit".equals(pColumn)) return fTotal.getAmountDebit();
            if ("amountCredit".equals(pColumn)) return fTotal.getAmountCredit();
            if ("amount".equals(pColumn)) return fTotal.getAmount();
            if ("currencyID".equals(pColumn)) return new Short(fTotal.getCurrencyID());
            if ("exchangeRate".equals(pColumn)) return new Double(fTotal.getExchangeRate());
            if ("amountRef".equals(pColumn)) return fTotal.getAmountRef();
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
        if ("tstamp".equals(pColumn)) {
            fComparator = new TimestampComparator();
            java.sql.Timestamp[] temp = new java.sql.Timestamp[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTstamp();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("dmType".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDmType();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("dmSubType".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDmSubType();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("evalStart".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getEvalStart();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("evalDate".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getEvalDate();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("ledgerID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getLedgerID();
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
        if ("ledgerName".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getLedgerName();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("ledgerType".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getLedgerType();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("amountStart".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAmountStart();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("amountPrevious".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAmountPrevious();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("amountDebit".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAmountDebit();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("amountCredit".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAmountCredit();
            }
            sort(temp, 0, temp.length - 1, up);
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
        if ("amountRef".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAmountRef();
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

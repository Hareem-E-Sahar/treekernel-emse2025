package com.be.table;

import java.util.*;
import com.be.vo.LedgerBookingVO;
import com.util.comparator.*;
import com.util.table.*;

public class LedgerBookingModel implements ITableModel {

    protected LedgerBookingVO fTotal;

    protected LedgerBookingVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public LedgerBookingModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (LedgerBookingVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        LedgerBookingVO vo = fTableData[fSortOrder[pRow]];
        if ("orderID".equals(pColumn)) return new Long(vo.getOrderID());
        if ("cancelID".equals(pColumn)) return new Long(vo.getCancelID());
        if ("dbUser".equals(pColumn)) return vo.getDbUser();
        if ("tstamp".equals(pColumn)) return vo.getTstamp();
        if ("valueDate".equals(pColumn)) return vo.getValueDate();
        if ("journalID".equals(pColumn)) return new Long(vo.getJournalID());
        if ("bookingID".equals(pColumn)) return new Long(vo.getBookingID());
        if ("bookDate".equals(pColumn)) return vo.getBookDate();
        if ("bookText".equals(pColumn)) return vo.getBookText();
        if ("ledgerID".equals(pColumn)) return new Long(vo.getLedgerID());
        if ("accountID".equals(pColumn)) return vo.getAccountID();
        if ("ledgerName".equals(pColumn)) return vo.getLedgerName();
        if ("ledgerType".equals(pColumn)) return vo.getLedgerType();
        if ("amountStart".equals(pColumn)) return vo.getAmountStart();
        if ("exchangeRateStart".equals(pColumn)) return new Double(vo.getExchangeRateStart());
        if ("amountStartRef".equals(pColumn)) return vo.getAmountStartRef();
        if ("ledgerDebit".equals(pColumn)) return new Long(vo.getLedgerDebit());
        if ("amountDebit".equals(pColumn)) return vo.getAmountDebit();
        if ("currencyDebitID".equals(pColumn)) return new Short(vo.getCurrencyDebitID());
        if ("ledgerCredit".equals(pColumn)) return new Long(vo.getLedgerCredit());
        if ("amountCredit".equals(pColumn)) return vo.getAmountCredit();
        if ("currencyCreditID".equals(pColumn)) return new Short(vo.getCurrencyCreditID());
        if ("amount".equals(pColumn)) return vo.getAmount();
        if ("currencyID".equals(pColumn)) return new Short(vo.getCurrencyID());
        if ("exchangeRate".equals(pColumn)) return new Double(vo.getExchangeRate());
        if ("amountRef".equals(pColumn)) return vo.getAmountRef();
        if ("userID".equals(pColumn)) return new Long(vo.getUserID());
        if ("validFrom".equals(pColumn)) return vo.getValidFrom();
        if ("validTo".equals(pColumn)) return vo.getValidTo();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("orderID".equals(pColumn)) return new Long(fTotal.getOrderID());
            if ("cancelID".equals(pColumn)) return new Long(fTotal.getCancelID());
            if ("dbUser".equals(pColumn)) return fTotal.getDbUser();
            if ("tstamp".equals(pColumn)) return fTotal.getTstamp();
            if ("valueDate".equals(pColumn)) return fTotal.getValueDate();
            if ("journalID".equals(pColumn)) return new Long(fTotal.getJournalID());
            if ("bookingID".equals(pColumn)) return new Long(fTotal.getBookingID());
            if ("bookDate".equals(pColumn)) return fTotal.getBookDate();
            if ("bookText".equals(pColumn)) return fTotal.getBookText();
            if ("ledgerID".equals(pColumn)) return new Long(fTotal.getLedgerID());
            if ("accountID".equals(pColumn)) return fTotal.getAccountID();
            if ("ledgerName".equals(pColumn)) return fTotal.getLedgerName();
            if ("ledgerType".equals(pColumn)) return fTotal.getLedgerType();
            if ("amountStart".equals(pColumn)) return fTotal.getAmountStart();
            if ("exchangeRateStart".equals(pColumn)) return new Double(fTotal.getExchangeRateStart());
            if ("amountStartRef".equals(pColumn)) return fTotal.getAmountStartRef();
            if ("ledgerDebit".equals(pColumn)) return new Long(fTotal.getLedgerDebit());
            if ("amountDebit".equals(pColumn)) return fTotal.getAmountDebit();
            if ("currencyDebitID".equals(pColumn)) return new Short(fTotal.getCurrencyDebitID());
            if ("ledgerCredit".equals(pColumn)) return new Long(fTotal.getLedgerCredit());
            if ("amountCredit".equals(pColumn)) return fTotal.getAmountCredit();
            if ("currencyCreditID".equals(pColumn)) return new Short(fTotal.getCurrencyCreditID());
            if ("amount".equals(pColumn)) return fTotal.getAmount();
            if ("currencyID".equals(pColumn)) return new Short(fTotal.getCurrencyID());
            if ("exchangeRate".equals(pColumn)) return new Double(fTotal.getExchangeRate());
            if ("amountRef".equals(pColumn)) return fTotal.getAmountRef();
            if ("userID".equals(pColumn)) return new Long(fTotal.getUserID());
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
        if ("orderID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getOrderID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("cancelID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCancelID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("dbUser".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDbUser();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("tstamp".equals(pColumn)) {
            fComparator = new TimestampComparator();
            java.sql.Timestamp[] temp = new java.sql.Timestamp[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTstamp();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("valueDate".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getValueDate();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("journalID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getJournalID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("bookingID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBookingID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("bookDate".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBookDate();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("bookText".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBookText();
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
        if ("exchangeRateStart".equals(pColumn)) {
            Sorter sorter = new Sorter();
            double[] temp = new double[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getExchangeRateStart();
            }
            fSortOrder = sorter.sortDouble(temp, fSortOrder, up);
        }
        if ("amountStartRef".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAmountStartRef();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("ledgerDebit".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getLedgerDebit();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("amountDebit".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAmountDebit();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("currencyDebitID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            short[] temp = new short[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCurrencyDebitID();
            }
            fSortOrder = sorter.sortShort(temp, fSortOrder, up);
        }
        if ("ledgerCredit".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getLedgerCredit();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("amountCredit".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAmountCredit();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("currencyCreditID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            short[] temp = new short[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCurrencyCreditID();
            }
            fSortOrder = sorter.sortShort(temp, fSortOrder, up);
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
        if ("userID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getUserID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
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

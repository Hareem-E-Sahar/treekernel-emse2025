package com.debitors.table;

import java.util.*;
import com.debitors.vo.InpayViewVO;
import com.util.comparator.*;
import com.util.table.*;

public class InpayViewModel implements ITableModel {

    protected InpayViewVO fTotal;

    protected InpayViewVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public InpayViewModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (InpayViewVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        InpayViewVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("billID".equals(pColumn)) return new Long(vo.getBillID());
        if ("type".equals(pColumn)) return vo.getType();
        if ("accountNr".equals(pColumn)) return vo.getAccountNr();
        if ("referenceNr".equals(pColumn)) return vo.getReferenceNr();
        if ("currency".equals(pColumn)) return vo.getCurrency();
        if ("amount".equals(pColumn)) return vo.getAmount();
        if ("currencyCost".equals(pColumn)) return vo.getCurrencyCost();
        if ("cost".equals(pColumn)) return vo.getCost();
        if ("orderNr".equals(pColumn)) return vo.getOrderNr();
        if ("orderDate".equals(pColumn)) return vo.getOrderDate();
        if ("processingDate".equals(pColumn)) return vo.getProcessingDate();
        if ("creditDate".equals(pColumn)) return vo.getCreditDate();
        if ("rejectCode".equals(pColumn)) return vo.getRejectCode();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("billID".equals(pColumn)) return new Long(fTotal.getBillID());
            if ("type".equals(pColumn)) return fTotal.getType();
            if ("accountNr".equals(pColumn)) return fTotal.getAccountNr();
            if ("referenceNr".equals(pColumn)) return fTotal.getReferenceNr();
            if ("currency".equals(pColumn)) return fTotal.getCurrency();
            if ("amount".equals(pColumn)) return fTotal.getAmount();
            if ("currencyCost".equals(pColumn)) return fTotal.getCurrencyCost();
            if ("cost".equals(pColumn)) return fTotal.getCost();
            if ("orderNr".equals(pColumn)) return fTotal.getOrderNr();
            if ("orderDate".equals(pColumn)) return fTotal.getOrderDate();
            if ("processingDate".equals(pColumn)) return fTotal.getProcessingDate();
            if ("creditDate".equals(pColumn)) return fTotal.getCreditDate();
            if ("rejectCode".equals(pColumn)) return fTotal.getRejectCode();
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
        if ("billID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBillID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("type".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getType();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("accountNr".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAccountNr();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("referenceNr".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getReferenceNr();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("currency".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCurrency();
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
        if ("currencyCost".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCurrencyCost();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("cost".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCost();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("orderNr".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getOrderNr();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("orderDate".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getOrderDate();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("processingDate".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getProcessingDate();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("creditDate".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCreditDate();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("rejectCode".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getRejectCode();
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

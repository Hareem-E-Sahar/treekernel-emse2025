package com.debitors.table;

import java.util.*;
import com.debitors.vo.PFTR4VO;
import com.util.comparator.*;
import com.util.table.*;

public class PFTR4Model implements ITableModel {

    protected PFTR4VO fTotal;

    protected PFTR4VO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public PFTR4Model() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (PFTR4VO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        PFTR4VO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("type".equals(pColumn)) return vo.getType();
        if ("ta".equals(pColumn)) return vo.getTa();
        if ("origin".equals(pColumn)) return vo.getOrigin();
        if ("deliveryKind".equals(pColumn)) return vo.getDeliveryKind();
        if ("customerNr".equals(pColumn)) return vo.getCustomerNr();
        if ("sortKey".equals(pColumn)) return vo.getSortKey();
        if ("currencyCode".equals(pColumn)) return vo.getCurrencyCode();
        if ("amount".equals(pColumn)) return vo.getAmount();
        if ("transactionCount".equals(pColumn)) return new Long(vo.getTransactionCount());
        if ("creationDate".equals(pColumn)) return vo.getCreationDate();
        if ("currencyCode2".equals(pColumn)) return vo.getCurrencyCode2();
        if ("cost".equals(pColumn)) return vo.getCost();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("type".equals(pColumn)) return fTotal.getType();
            if ("ta".equals(pColumn)) return fTotal.getTa();
            if ("origin".equals(pColumn)) return fTotal.getOrigin();
            if ("deliveryKind".equals(pColumn)) return fTotal.getDeliveryKind();
            if ("customerNr".equals(pColumn)) return fTotal.getCustomerNr();
            if ("sortKey".equals(pColumn)) return fTotal.getSortKey();
            if ("currencyCode".equals(pColumn)) return fTotal.getCurrencyCode();
            if ("amount".equals(pColumn)) return fTotal.getAmount();
            if ("transactionCount".equals(pColumn)) return new Long(fTotal.getTransactionCount());
            if ("creationDate".equals(pColumn)) return fTotal.getCreationDate();
            if ("currencyCode2".equals(pColumn)) return fTotal.getCurrencyCode2();
            if ("cost".equals(pColumn)) return fTotal.getCost();
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
        if ("type".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getType();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("ta".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTa();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("origin".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getOrigin();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("deliveryKind".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDeliveryKind();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("customerNr".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCustomerNr();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("sortKey".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getSortKey();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("currencyCode".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCurrencyCode();
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
        if ("transactionCount".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTransactionCount();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("creationDate".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCreationDate();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("currencyCode2".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCurrencyCode2();
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

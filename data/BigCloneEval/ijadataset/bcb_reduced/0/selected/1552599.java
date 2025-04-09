package com.debitors.table;

import java.util.*;
import com.debitors.vo.PFTR3VO;
import com.util.comparator.*;
import com.util.table.*;

public class PFTR3Model implements ITableModel {

    protected PFTR3VO fTotal;

    protected PFTR3VO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public PFTR3Model() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (PFTR3VO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        PFTR3VO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("type".equals(pColumn)) return vo.getType();
        if ("esrAccountNr".equals(pColumn)) return vo.getEsrAccountNr();
        if ("sortKey".equals(pColumn)) return vo.getSortKey();
        if ("amount".equals(pColumn)) return vo.getAmount();
        if ("transactionCount".equals(pColumn)) return new Long(vo.getTransactionCount());
        if ("creationDate".equals(pColumn)) return vo.getCreationDate();
        if ("cost".equals(pColumn)) return vo.getCost();
        if ("costPostProcessing".equals(pColumn)) return vo.getCostPostProcessing();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("type".equals(pColumn)) return fTotal.getType();
            if ("esrAccountNr".equals(pColumn)) return fTotal.getEsrAccountNr();
            if ("sortKey".equals(pColumn)) return fTotal.getSortKey();
            if ("amount".equals(pColumn)) return fTotal.getAmount();
            if ("transactionCount".equals(pColumn)) return new Long(fTotal.getTransactionCount());
            if ("creationDate".equals(pColumn)) return fTotal.getCreationDate();
            if ("cost".equals(pColumn)) return fTotal.getCost();
            if ("costPostProcessing".equals(pColumn)) return fTotal.getCostPostProcessing();
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
        if ("esrAccountNr".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getEsrAccountNr();
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
        if ("cost".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCost();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("costPostProcessing".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCostPostProcessing();
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

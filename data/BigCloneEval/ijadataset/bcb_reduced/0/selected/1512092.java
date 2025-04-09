package com.debitors.table;

import java.util.*;
import com.debitors.vo.PFT3VO;
import com.util.comparator.*;
import com.util.table.*;

public class PFT3Model implements ITableModel {

    protected PFT3VO fTotal;

    protected PFT3VO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public PFT3Model() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (PFT3VO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        PFT3VO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("type".equals(pColumn)) return vo.getType();
        if ("esrAccountNr".equals(pColumn)) return vo.getEsrAccountNr();
        if ("referenceNr".equals(pColumn)) return vo.getReferenceNr();
        if ("amount".equals(pColumn)) return vo.getAmount();
        if ("orderReference".equals(pColumn)) return vo.getOrderReference();
        if ("orderDate".equals(pColumn)) return vo.getOrderDate();
        if ("processingDate".equals(pColumn)) return vo.getProcessingDate();
        if ("creditDate".equals(pColumn)) return vo.getCreditDate();
        if ("microFilmNr".equals(pColumn)) return vo.getMicroFilmNr();
        if ("rejectCode".equals(pColumn)) return vo.getRejectCode();
        if ("cost".equals(pColumn)) return vo.getCost();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("type".equals(pColumn)) return fTotal.getType();
            if ("esrAccountNr".equals(pColumn)) return fTotal.getEsrAccountNr();
            if ("referenceNr".equals(pColumn)) return fTotal.getReferenceNr();
            if ("amount".equals(pColumn)) return fTotal.getAmount();
            if ("orderReference".equals(pColumn)) return fTotal.getOrderReference();
            if ("orderDate".equals(pColumn)) return fTotal.getOrderDate();
            if ("processingDate".equals(pColumn)) return fTotal.getProcessingDate();
            if ("creditDate".equals(pColumn)) return fTotal.getCreditDate();
            if ("microFilmNr".equals(pColumn)) return fTotal.getMicroFilmNr();
            if ("rejectCode".equals(pColumn)) return fTotal.getRejectCode();
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
        if ("esrAccountNr".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getEsrAccountNr();
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
        if ("amount".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAmount();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("orderReference".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getOrderReference();
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
        if ("microFilmNr".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getMicroFilmNr();
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

package com.pr.table;

import java.util.*;
import com.pr.vo.WageCategoryVO;
import com.util.comparator.*;
import com.util.table.*;

public class WageCategoryModel implements ITableModel {

    protected WageCategoryVO fTotal;

    protected WageCategoryVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public WageCategoryModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (WageCategoryVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        WageCategoryVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("number".equals(pColumn)) return new Long(vo.getNumber());
        if ("name".equals(pColumn)) return vo.getName();
        if ("type".equals(pColumn)) return new Long(vo.getType());
        if ("ahv".equals(pColumn)) return new Boolean(vo.getAhv());
        if ("alv".equals(pColumn)) return new Boolean(vo.getAlv());
        if ("nbu".equals(pColumn)) return new Boolean(vo.getNbu());
        if ("bvg".equals(pColumn)) return new Boolean(vo.getBvg());
        if ("qst".equals(pColumn)) return new Boolean(vo.getQst());
        if ("factor".equals(pColumn)) return new Long(vo.getFactor());
        if ("lohnausweis".equals(pColumn)) return vo.getLohnausweis();
        if ("modified".equals(pColumn)) return vo.getModified();
        if ("validFrom".equals(pColumn)) return vo.getValidFrom();
        if ("validTo".equals(pColumn)) return vo.getValidTo();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("number".equals(pColumn)) return new Long(fTotal.getNumber());
            if ("name".equals(pColumn)) return fTotal.getName();
            if ("type".equals(pColumn)) return new Long(fTotal.getType());
            if ("ahv".equals(pColumn)) return new Boolean(fTotal.getAhv());
            if ("alv".equals(pColumn)) return new Boolean(fTotal.getAlv());
            if ("nbu".equals(pColumn)) return new Boolean(fTotal.getNbu());
            if ("bvg".equals(pColumn)) return new Boolean(fTotal.getBvg());
            if ("qst".equals(pColumn)) return new Boolean(fTotal.getQst());
            if ("factor".equals(pColumn)) return new Long(fTotal.getFactor());
            if ("lohnausweis".equals(pColumn)) return fTotal.getLohnausweis();
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
        if ("number".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getNumber();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
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
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getType();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("ahv".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAhv();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
        }
        if ("alv".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAlv();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
        }
        if ("nbu".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getNbu();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
        }
        if ("bvg".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBvg();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
        }
        if ("qst".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getQst();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
        }
        if ("factor".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getFactor();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("lohnausweis".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getLohnausweis();
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

package com.be.table;

import java.util.*;
import com.be.vo.CountryVO;
import com.util.comparator.*;
import com.util.table.*;

public class CountryModel implements ITableModel {

    protected CountryVO fTotal;

    protected CountryVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public CountryModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (CountryVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        CountryVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("name".equals(pColumn)) return vo.getName();
        if ("a2".equals(pColumn)) return vo.getA2();
        if ("a3".equals(pColumn)) return vo.getA3();
        if ("num".equals(pColumn)) return new Long(vo.getNum());
        if ("itu".equals(pColumn)) return vo.getItu();
        if ("fips".equals(pColumn)) return vo.getFips();
        if ("ds".equals(pColumn)) return vo.getDs();
        if ("marc".equals(pColumn)) return vo.getMarc();
        if ("independent".equals(pColumn)) return vo.getIndependent();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("name".equals(pColumn)) return fTotal.getName();
            if ("a2".equals(pColumn)) return fTotal.getA2();
            if ("a3".equals(pColumn)) return fTotal.getA3();
            if ("num".equals(pColumn)) return new Long(fTotal.getNum());
            if ("itu".equals(pColumn)) return fTotal.getItu();
            if ("fips".equals(pColumn)) return fTotal.getFips();
            if ("ds".equals(pColumn)) return fTotal.getDs();
            if ("marc".equals(pColumn)) return fTotal.getMarc();
            if ("independent".equals(pColumn)) return fTotal.getIndependent();
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
        if ("name".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getName();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("a2".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getA2();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("a3".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getA3();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("num".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getNum();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("itu".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getItu();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("fips".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getFips();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("ds".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDs();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("marc".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getMarc();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("independent".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getIndependent();
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

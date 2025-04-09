package com.be.table;

import java.util.*;
import com.be.vo.PGUserVO;
import com.util.comparator.*;
import com.util.table.*;

public class PGUserModel implements ITableModel {

    protected PGUserVO fTotal;

    protected PGUserVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public PGUserModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (PGUserVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        PGUserVO vo = fTableData[fSortOrder[pRow]];
        if ("usename".equals(pColumn)) return vo.getUsename();
        if ("usesysid".equals(pColumn)) return new Long(vo.getUsesysid());
        if ("usecreatedb".equals(pColumn)) return new Boolean(vo.getUsecreatedb());
        if ("usesuper".equals(pColumn)) return new Boolean(vo.getUsesuper());
        if ("usecatupd".equals(pColumn)) return new Boolean(vo.getUsecatupd());
        if ("passwd".equals(pColumn)) return vo.getPasswd();
        if ("valuntil".equals(pColumn)) return vo.getValuntil();
        if ("useconfig".equals(pColumn)) return vo.getUseconfig();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("usename".equals(pColumn)) return fTotal.getUsename();
            if ("usesysid".equals(pColumn)) return new Long(fTotal.getUsesysid());
            if ("usecreatedb".equals(pColumn)) return new Boolean(fTotal.getUsecreatedb());
            if ("usesuper".equals(pColumn)) return new Boolean(fTotal.getUsesuper());
            if ("usecatupd".equals(pColumn)) return new Boolean(fTotal.getUsecatupd());
            if ("passwd".equals(pColumn)) return fTotal.getPasswd();
            if ("valuntil".equals(pColumn)) return fTotal.getValuntil();
            if ("useconfig".equals(pColumn)) return fTotal.getUseconfig();
        }
        return null;
    }

    public void sort(String pColumn, String pSortDirection) {
        boolean up = true;
        if (!"u".equals(pSortDirection)) {
            up = false;
        }
        if ("usename".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getUsename();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("usesysid".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getUsesysid();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("usecreatedb".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getUsecreatedb();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
        }
        if ("usesuper".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getUsesuper();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
        }
        if ("usecatupd".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getUsecatupd();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
        }
        if ("passwd".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPasswd();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("valuntil".equals(pColumn)) {
            fComparator = new TimestampComparator();
            java.sql.Timestamp[] temp = new java.sql.Timestamp[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getValuntil();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("useconfig".equals(pColumn)) {
            fComparator = new ObjectComparator();
            Object[] temp = new Object[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getUseconfig();
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

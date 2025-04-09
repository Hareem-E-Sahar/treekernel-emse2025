package com.be.table;

import java.util.*;
import com.be.vo.PGTableVO;
import com.util.comparator.*;
import com.util.table.*;

public class PGTableModel implements ITableModel {

    protected PGTableVO fTotal;

    protected PGTableVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public PGTableModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (PGTableVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        PGTableVO vo = fTableData[fSortOrder[pRow]];
        if ("schemaname".equals(pColumn)) return vo.getSchemaname();
        if ("tablename".equals(pColumn)) return vo.getTablename();
        if ("tableowner".equals(pColumn)) return vo.getTableowner();
        if ("tablespace".equals(pColumn)) return vo.getTablespace();
        if ("hasindexes".equals(pColumn)) return new Boolean(vo.getHasindexes());
        if ("hasrules".equals(pColumn)) return new Boolean(vo.getHasrules());
        if ("hastriggers".equals(pColumn)) return new Boolean(vo.getHastriggers());
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("schemaname".equals(pColumn)) return fTotal.getSchemaname();
            if ("tablename".equals(pColumn)) return fTotal.getTablename();
            if ("tableowner".equals(pColumn)) return fTotal.getTableowner();
            if ("tablespace".equals(pColumn)) return fTotal.getTablespace();
            if ("hasindexes".equals(pColumn)) return new Boolean(fTotal.getHasindexes());
            if ("hasrules".equals(pColumn)) return new Boolean(fTotal.getHasrules());
            if ("hastriggers".equals(pColumn)) return new Boolean(fTotal.getHastriggers());
        }
        return null;
    }

    public void sort(String pColumn, String pSortDirection) {
        boolean up = true;
        if (!"u".equals(pSortDirection)) {
            up = false;
        }
        if ("schemaname".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getSchemaname();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("tablename".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTablename();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("tableowner".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTableowner();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("tablespace".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTablespace();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("hasindexes".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getHasindexes();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
        }
        if ("hasrules".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getHasrules();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
        }
        if ("hastriggers".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getHastriggers();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
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

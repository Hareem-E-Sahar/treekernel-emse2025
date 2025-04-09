package com.be.table;

import java.util.*;
import com.be.vo.PGProcVO;
import com.util.comparator.*;
import com.util.table.*;

public class PGProcModel implements ITableModel {

    protected PGProcVO fTotal;

    protected PGProcVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public PGProcModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (PGProcVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        PGProcVO vo = fTableData[fSortOrder[pRow]];
        if ("proname".equals(pColumn)) return vo.getProname();
        if ("pronamespace".equals(pColumn)) return new Long(vo.getPronamespace());
        if ("proowner".equals(pColumn)) return new Long(vo.getProowner());
        if ("prolang".equals(pColumn)) return new Long(vo.getProlang());
        if ("proisagg".equals(pColumn)) return new Boolean(vo.getProisagg());
        if ("prosecdef".equals(pColumn)) return new Boolean(vo.getProsecdef());
        if ("proisstrict".equals(pColumn)) return new Boolean(vo.getProisstrict());
        if ("proretset".equals(pColumn)) return new Boolean(vo.getProretset());
        if ("provolatile".equals(pColumn)) return vo.getProvolatile();
        if ("pronargs".equals(pColumn)) return new Short(vo.getPronargs());
        if ("prorettype".equals(pColumn)) return new Long(vo.getProrettype());
        if ("proargtypes".equals(pColumn)) return vo.getProargtypes();
        if ("proargnames".equals(pColumn)) return vo.getProargnames();
        if ("prosrc".equals(pColumn)) return vo.getProsrc();
        if ("proacl".equals(pColumn)) return vo.getProacl();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("proname".equals(pColumn)) return fTotal.getProname();
            if ("pronamespace".equals(pColumn)) return new Long(fTotal.getPronamespace());
            if ("proowner".equals(pColumn)) return new Long(fTotal.getProowner());
            if ("prolang".equals(pColumn)) return new Long(fTotal.getProlang());
            if ("proisagg".equals(pColumn)) return new Boolean(fTotal.getProisagg());
            if ("prosecdef".equals(pColumn)) return new Boolean(fTotal.getProsecdef());
            if ("proisstrict".equals(pColumn)) return new Boolean(fTotal.getProisstrict());
            if ("proretset".equals(pColumn)) return new Boolean(fTotal.getProretset());
            if ("provolatile".equals(pColumn)) return fTotal.getProvolatile();
            if ("pronargs".equals(pColumn)) return new Short(fTotal.getPronargs());
            if ("prorettype".equals(pColumn)) return new Long(fTotal.getProrettype());
            if ("proargtypes".equals(pColumn)) return fTotal.getProargtypes();
            if ("proargnames".equals(pColumn)) return fTotal.getProargnames();
            if ("prosrc".equals(pColumn)) return fTotal.getProsrc();
            if ("proacl".equals(pColumn)) return fTotal.getProacl();
        }
        return null;
    }

    public void sort(String pColumn, String pSortDirection) {
        boolean up = true;
        if (!"u".equals(pSortDirection)) {
            up = false;
        }
        if ("proname".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getProname();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("pronamespace".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPronamespace();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("proowner".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getProowner();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("prolang".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getProlang();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("proisagg".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getProisagg();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
        }
        if ("prosecdef".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getProsecdef();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
        }
        if ("proisstrict".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getProisstrict();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
        }
        if ("proretset".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getProretset();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
        }
        if ("provolatile".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getProvolatile();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("pronargs".equals(pColumn)) {
            Sorter sorter = new Sorter();
            short[] temp = new short[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPronargs();
            }
            fSortOrder = sorter.sortShort(temp, fSortOrder, up);
        }
        if ("prorettype".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getProrettype();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("proargtypes".equals(pColumn)) {
            fComparator = new ObjectComparator();
            Object[] temp = new Object[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getProargtypes();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("proargnames".equals(pColumn)) {
            fComparator = new ObjectComparator();
            Object[] temp = new Object[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getProargnames();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("prosrc".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getProsrc();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("proacl".equals(pColumn)) {
            fComparator = new ObjectComparator();
            Object[] temp = new Object[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getProacl();
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

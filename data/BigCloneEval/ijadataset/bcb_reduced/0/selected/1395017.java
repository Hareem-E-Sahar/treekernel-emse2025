package com.be.table;

import java.util.*;
import com.be.vo.PGPrivilegeVO;
import com.util.comparator.*;
import com.util.table.*;

public class PGPrivilegeModel implements ITableModel {

    protected PGPrivilegeVO fTotal;

    protected PGPrivilegeVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public PGPrivilegeModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (PGPrivilegeVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        PGPrivilegeVO vo = fTableData[fSortOrder[pRow]];
        if ("user".equals(pColumn)) return vo.getUser();
        if ("obj".equals(pColumn)) return vo.getObj();
        if ("privilege".equals(pColumn)) return vo.getPrivilege();
        if ("type".equals(pColumn)) return vo.getType();
        if ("hasPrivilege".equals(pColumn)) return new Boolean(vo.isHasPrivilege());
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("user".equals(pColumn)) return fTotal.getUser();
            if ("obj".equals(pColumn)) return fTotal.getObj();
            if ("privilege".equals(pColumn)) return fTotal.getPrivilege();
            if ("type".equals(pColumn)) return fTotal.getType();
            if ("hasPrivilege".equals(pColumn)) return new Boolean(fTotal.isHasPrivilege());
        }
        return null;
    }

    public void sort(String pColumn, String pSortDirection) {
        boolean up = true;
        if (!"u".equals(pSortDirection)) {
            up = false;
        }
        if ("user".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getUser();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("obj".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getObj();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("privilege".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPrivilege();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("type".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getType();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("hasPrivilege".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].isHasPrivilege();
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

package com.be.table;

import java.util.*;
import com.be.vo.RoleRoutineGrantsVO;
import com.util.comparator.*;
import com.util.table.*;

public class RoleRoutineGrantsModel implements ITableModel {

    protected RoleRoutineGrantsVO fTotal;

    protected RoleRoutineGrantsVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public RoleRoutineGrantsModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (RoleRoutineGrantsVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        RoleRoutineGrantsVO vo = fTableData[fSortOrder[pRow]];
        if ("grantor".equals(pColumn)) return vo.getGrantor();
        if ("grantee".equals(pColumn)) return vo.getGrantee();
        if ("specificCatalog".equals(pColumn)) return vo.getSpecificCatalog();
        if ("specificSchema".equals(pColumn)) return vo.getSpecificSchema();
        if ("specificName".equals(pColumn)) return vo.getSpecificName();
        if ("routineCatalog".equals(pColumn)) return vo.getRoutineCatalog();
        if ("routineSchema".equals(pColumn)) return vo.getRoutineSchema();
        if ("routineName".equals(pColumn)) return vo.getRoutineName();
        if ("privilegeType".equals(pColumn)) return vo.getPrivilegeType();
        if ("isGrantable".equals(pColumn)) return vo.getIsGrantable();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("grantor".equals(pColumn)) return fTotal.getGrantor();
            if ("grantee".equals(pColumn)) return fTotal.getGrantee();
            if ("specificCatalog".equals(pColumn)) return fTotal.getSpecificCatalog();
            if ("specificSchema".equals(pColumn)) return fTotal.getSpecificSchema();
            if ("specificName".equals(pColumn)) return fTotal.getSpecificName();
            if ("routineCatalog".equals(pColumn)) return fTotal.getRoutineCatalog();
            if ("routineSchema".equals(pColumn)) return fTotal.getRoutineSchema();
            if ("routineName".equals(pColumn)) return fTotal.getRoutineName();
            if ("privilegeType".equals(pColumn)) return fTotal.getPrivilegeType();
            if ("isGrantable".equals(pColumn)) return fTotal.getIsGrantable();
        }
        return null;
    }

    public void sort(String pColumn, String pSortDirection) {
        boolean up = true;
        if (!"u".equals(pSortDirection)) {
            up = false;
        }
        if ("grantor".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getGrantor();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("grantee".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getGrantee();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("specificCatalog".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getSpecificCatalog();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("specificSchema".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getSpecificSchema();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("specificName".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getSpecificName();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("routineCatalog".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getRoutineCatalog();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("routineSchema".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getRoutineSchema();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("routineName".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getRoutineName();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("privilegeType".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPrivilegeType();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("isGrantable".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getIsGrantable();
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

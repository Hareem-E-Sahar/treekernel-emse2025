package com.be.table;

import java.util.*;
import com.be.vo.RoleTableGrantsVO;
import com.util.comparator.*;
import com.util.table.*;

public class RoleTableGrantsModel implements ITableModel {

    protected RoleTableGrantsVO fTotal;

    protected RoleTableGrantsVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public RoleTableGrantsModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (RoleTableGrantsVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        RoleTableGrantsVO vo = fTableData[fSortOrder[pRow]];
        if ("grantor".equals(pColumn)) return vo.getGrantor();
        if ("grantee".equals(pColumn)) return vo.getGrantee();
        if ("tableCatalog".equals(pColumn)) return vo.getTableCatalog();
        if ("tableSchema".equals(pColumn)) return vo.getTableSchema();
        if ("tableName".equals(pColumn)) return vo.getTableName();
        if ("privilegeType".equals(pColumn)) return vo.getPrivilegeType();
        if ("isGrantable".equals(pColumn)) return vo.getIsGrantable();
        if ("withHierarchy".equals(pColumn)) return vo.getWithHierarchy();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("grantor".equals(pColumn)) return fTotal.getGrantor();
            if ("grantee".equals(pColumn)) return fTotal.getGrantee();
            if ("tableCatalog".equals(pColumn)) return fTotal.getTableCatalog();
            if ("tableSchema".equals(pColumn)) return fTotal.getTableSchema();
            if ("tableName".equals(pColumn)) return fTotal.getTableName();
            if ("privilegeType".equals(pColumn)) return fTotal.getPrivilegeType();
            if ("isGrantable".equals(pColumn)) return fTotal.getIsGrantable();
            if ("withHierarchy".equals(pColumn)) return fTotal.getWithHierarchy();
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
        if ("tableCatalog".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTableCatalog();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("tableSchema".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTableSchema();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("tableName".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTableName();
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
        if ("withHierarchy".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getWithHierarchy();
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

package com.be.table;

import java.util.*;
import com.be.vo.RoleUsageGrantsVO;
import com.util.comparator.*;
import com.util.table.*;

public class RoleUsageGrantsModel implements ITableModel {

    protected RoleUsageGrantsVO fTotal;

    protected RoleUsageGrantsVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public RoleUsageGrantsModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (RoleUsageGrantsVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        RoleUsageGrantsVO vo = fTableData[fSortOrder[pRow]];
        if ("grantor".equals(pColumn)) return vo.getGrantor();
        if ("grantee".equals(pColumn)) return vo.getGrantee();
        if ("objectCatalog".equals(pColumn)) return vo.getObjectCatalog();
        if ("objectSchema".equals(pColumn)) return vo.getObjectSchema();
        if ("objectName".equals(pColumn)) return vo.getObjectName();
        if ("objectType".equals(pColumn)) return vo.getObjectType();
        if ("privilegeType".equals(pColumn)) return vo.getPrivilegeType();
        if ("isGrantable".equals(pColumn)) return vo.getIsGrantable();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("grantor".equals(pColumn)) return fTotal.getGrantor();
            if ("grantee".equals(pColumn)) return fTotal.getGrantee();
            if ("objectCatalog".equals(pColumn)) return fTotal.getObjectCatalog();
            if ("objectSchema".equals(pColumn)) return fTotal.getObjectSchema();
            if ("objectName".equals(pColumn)) return fTotal.getObjectName();
            if ("objectType".equals(pColumn)) return fTotal.getObjectType();
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
        if ("objectCatalog".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getObjectCatalog();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("objectSchema".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getObjectSchema();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("objectName".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getObjectName();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("objectType".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getObjectType();
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

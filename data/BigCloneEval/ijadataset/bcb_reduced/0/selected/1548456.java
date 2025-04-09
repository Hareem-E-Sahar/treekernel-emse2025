package com.be.table;

import java.util.*;
import com.be.vo.QueryParameterVO;
import com.util.comparator.*;
import com.util.table.*;

public class QueryParameterModel implements ITableModel {

    protected QueryParameterVO fTotal;

    protected QueryParameterVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public QueryParameterModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (QueryParameterVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        QueryParameterVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("templateID".equals(pColumn)) return new Long(vo.getTemplateID());
        if ("paramName".equals(pColumn)) return vo.getParamName();
        if ("paramType".equals(pColumn)) return vo.getParamType();
        if ("intParam".equals(pColumn)) return new Long(vo.getIntParam());
        if ("floatParam".equals(pColumn)) return new Double(vo.getFloatParam());
        if ("boolParam".equals(pColumn)) return new Boolean(vo.getBoolParam());
        if ("textParam".equals(pColumn)) return vo.getTextParam();
        if ("amountParam".equals(pColumn)) return vo.getAmountParam();
        if ("dateParam".equals(pColumn)) return vo.getDateParam();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("templateID".equals(pColumn)) return new Long(fTotal.getTemplateID());
            if ("paramName".equals(pColumn)) return fTotal.getParamName();
            if ("paramType".equals(pColumn)) return fTotal.getParamType();
            if ("intParam".equals(pColumn)) return new Long(fTotal.getIntParam());
            if ("floatParam".equals(pColumn)) return new Double(fTotal.getFloatParam());
            if ("boolParam".equals(pColumn)) return new Boolean(fTotal.getBoolParam());
            if ("textParam".equals(pColumn)) return fTotal.getTextParam();
            if ("amountParam".equals(pColumn)) return fTotal.getAmountParam();
            if ("dateParam".equals(pColumn)) return fTotal.getDateParam();
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
        if ("templateID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTemplateID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("paramName".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getParamName();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("paramType".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getParamType();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("intParam".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getIntParam();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("floatParam".equals(pColumn)) {
            Sorter sorter = new Sorter();
            double[] temp = new double[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getFloatParam();
            }
            fSortOrder = sorter.sortDouble(temp, fSortOrder, up);
        }
        if ("boolParam".equals(pColumn)) {
            Sorter sorter = new Sorter();
            boolean[] temp = new boolean[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBoolParam();
            }
            fSortOrder = sorter.sortBoolean(temp, fSortOrder, up);
        }
        if ("textParam".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTextParam();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("amountParam".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAmountParam();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("dateParam".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDateParam();
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

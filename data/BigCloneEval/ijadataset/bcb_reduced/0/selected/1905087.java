package com.be.table;

import java.util.*;
import com.be.vo.MktFXRateReportVO;
import com.util.comparator.*;
import com.util.table.*;

public class MktFXRateReportModel implements ITableModel {

    protected MktFXRateReportVO fTotal;

    protected MktFXRateReportVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public MktFXRateReportModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (MktFXRateReportVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        MktFXRateReportVO vo = fTableData[fSortOrder[pRow]];
        if ("isoCode".equals(pColumn)) return vo.getIsoCode();
        if ("refIsoCode".equals(pColumn)) return vo.getRefIsoCode();
        if ("exchangeRate".equals(pColumn)) return new Double(vo.getExchangeRate());
        if ("tradeDate".equals(pColumn)) return vo.getTradeDate();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("isoCode".equals(pColumn)) return fTotal.getIsoCode();
            if ("refIsoCode".equals(pColumn)) return fTotal.getRefIsoCode();
            if ("exchangeRate".equals(pColumn)) return new Double(fTotal.getExchangeRate());
            if ("tradeDate".equals(pColumn)) return fTotal.getTradeDate();
        }
        return null;
    }

    public void sort(String pColumn, String pSortDirection) {
        boolean up = true;
        if (!"u".equals(pSortDirection)) {
            up = false;
        }
        if ("isoCode".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getIsoCode();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("refIsoCode".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getRefIsoCode();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("exchangeRate".equals(pColumn)) {
            Sorter sorter = new Sorter();
            double[] temp = new double[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getExchangeRate();
            }
            fSortOrder = sorter.sortDouble(temp, fSortOrder, up);
        }
        if ("tradeDate".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTradeDate();
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

package com.pr.table;

import java.math.BigDecimal;
import java.util.*;
import com.debitors.vo.OrderViewVO;
import com.pr.vo.JournalVO;
import com.util.comparator.*;
import com.util.table.*;

public class JournalModel implements ITableModel {

    protected JournalVO fTotal;

    protected JournalVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public JournalModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (JournalVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
        fTotal = computeTotal(fTableData);
    }

    private JournalVO computeTotal(JournalVO[] data) {
        JournalVO total = null;
        if (data != null && data.length > 0) {
            total = new JournalVO();
            BigDecimal amountTotal = new BigDecimal(0);
            for (int i = 0; i < data.length; i++) {
                amountTotal = amountTotal.add(data[i].getAmount());
            }
            total.setAmount(amountTotal);
        }
        return total;
    }

    public Object getValueAt(int pRow, String pColumn) {
        JournalVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("orderID".equals(pColumn)) return new Long(vo.getOrderID());
        if ("bookText".equals(pColumn)) return vo.getBookText();
        if ("bookDate".equals(pColumn)) return vo.getBookDate();
        if ("percentage".equals(pColumn)) return new Double(vo.getPercentage());
        if ("quantity".equals(pColumn)) return new Double(vo.getQuantity());
        if ("rate".equals(pColumn)) return vo.getRate();
        if ("amount".equals(pColumn)) return vo.getAmount();
        if ("currencyID".equals(pColumn)) return new Short(vo.getCurrencyID());
        if ("categoryID".equals(pColumn)) return new Long(vo.getCategoryID());
        if ("employeeID".equals(pColumn)) return new Long(vo.getEmployeeID());
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("orderID".equals(pColumn)) return new Long(fTotal.getOrderID());
            if ("bookText".equals(pColumn)) return fTotal.getBookText();
            if ("bookDate".equals(pColumn)) return fTotal.getBookDate();
            if ("percentage".equals(pColumn)) return new Double(fTotal.getPercentage());
            if ("quantity".equals(pColumn)) return new Double(fTotal.getQuantity());
            if ("rate".equals(pColumn)) return fTotal.getRate();
            if ("amount".equals(pColumn)) return fTotal.getAmount();
            if ("currencyID".equals(pColumn)) return new Short(fTotal.getCurrencyID());
            if ("categoryID".equals(pColumn)) return new Long(fTotal.getCategoryID());
            if ("employeeID".equals(pColumn)) return new Long(fTotal.getEmployeeID());
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
        if ("orderID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getOrderID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("bookText".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBookText();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("bookDate".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBookDate();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("percentage".equals(pColumn)) {
            Sorter sorter = new Sorter();
            double[] temp = new double[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPercentage();
            }
            fSortOrder = sorter.sortDouble(temp, fSortOrder, up);
        }
        if ("quantity".equals(pColumn)) {
            Sorter sorter = new Sorter();
            double[] temp = new double[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getQuantity();
            }
            fSortOrder = sorter.sortDouble(temp, fSortOrder, up);
        }
        if ("rate".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getRate();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("amount".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAmount();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("currencyID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            short[] temp = new short[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCurrencyID();
            }
            fSortOrder = sorter.sortShort(temp, fSortOrder, up);
        }
        if ("categoryID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCategoryID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("employeeID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getEmployeeID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
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

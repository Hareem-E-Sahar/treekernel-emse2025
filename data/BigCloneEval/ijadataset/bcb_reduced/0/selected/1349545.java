package com.debitors.table;

import java.util.*;
import com.debitors.vo.OrderVO;
import com.util.comparator.*;
import com.util.table.*;

public class OrderModel implements ITableModel {

    protected OrderVO fTotal;

    protected OrderVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public OrderModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (OrderVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        OrderVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("cancelID".equals(pColumn)) return new Long(vo.getCancelID());
        if ("guid".equals(pColumn)) return vo.getGuid();
        if ("title".equals(pColumn)) return vo.getTitle();
        if ("dbUser".equals(pColumn)) return vo.getDbUser();
        if ("timeStamp".equals(pColumn)) return vo.getTimeStamp();
        if ("orderDate".equals(pColumn)) return vo.getOrderDate();
        if ("shippingDate".equals(pColumn)) return vo.getShippingDate();
        if ("priceTotal".equals(pColumn)) return vo.getPriceTotal();
        if ("currencyID".equals(pColumn)) return new Short(vo.getCurrencyID());
        if ("discountTotal".equals(pColumn)) return vo.getDiscountTotal();
        if ("discountPercentTotal".equals(pColumn)) return new Double(vo.getDiscountPercentTotal());
        if ("vatTotal".equals(pColumn)) return vo.getVatTotal();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("cancelID".equals(pColumn)) return new Long(fTotal.getCancelID());
            if ("guid".equals(pColumn)) return fTotal.getGuid();
            if ("title".equals(pColumn)) return fTotal.getTitle();
            if ("dbUser".equals(pColumn)) return fTotal.getDbUser();
            if ("timeStamp".equals(pColumn)) return fTotal.getTimeStamp();
            if ("orderDate".equals(pColumn)) return fTotal.getOrderDate();
            if ("shippingDate".equals(pColumn)) return fTotal.getShippingDate();
            if ("priceTotal".equals(pColumn)) return fTotal.getPriceTotal();
            if ("currencyID".equals(pColumn)) return new Short(fTotal.getCurrencyID());
            if ("discountTotal".equals(pColumn)) return fTotal.getDiscountTotal();
            if ("discountPercentTotal".equals(pColumn)) return new Double(fTotal.getDiscountPercentTotal());
            if ("vatTotal".equals(pColumn)) return fTotal.getVatTotal();
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
        if ("cancelID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCancelID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("guid".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getGuid();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("title".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTitle();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("dbUser".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDbUser();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("timeStamp".equals(pColumn)) {
            fComparator = new TimestampComparator();
            java.sql.Timestamp[] temp = new java.sql.Timestamp[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTimeStamp();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("orderDate".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getOrderDate();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("shippingDate".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getShippingDate();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("priceTotal".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPriceTotal();
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
        if ("discountTotal".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDiscountTotal();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("discountPercentTotal".equals(pColumn)) {
            Sorter sorter = new Sorter();
            double[] temp = new double[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDiscountPercentTotal();
            }
            fSortOrder = sorter.sortDouble(temp, fSortOrder, up);
        }
        if ("vatTotal".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getVatTotal();
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

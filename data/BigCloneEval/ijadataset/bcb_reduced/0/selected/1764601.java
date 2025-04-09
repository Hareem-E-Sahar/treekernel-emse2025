package com.debitors.table;

import java.util.*;
import com.debitors.vo.OrderItemVO;
import com.util.comparator.*;
import com.util.table.*;

public class OrderItemModel implements ITableModel {

    protected OrderItemVO fTotal;

    protected OrderItemVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public OrderItemModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (OrderItemVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        OrderItemVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("orderID".equals(pColumn)) return new Long(vo.getOrderID());
        if ("itemID".equals(pColumn)) return new Long(vo.getItemID());
        if ("itemName".equals(pColumn)) return vo.getItemName();
        if ("itemDescription".equals(pColumn)) return vo.getItemDescription();
        if ("itemSerialNr".equals(pColumn)) return vo.getItemSerialNr();
        if ("itemCount".equals(pColumn)) return new Long(vo.getItemCount());
        if ("shippingDate".equals(pColumn)) return vo.getShippingDate();
        if ("price".equals(pColumn)) return vo.getPrice();
        if ("currencyID".equals(pColumn)) return new Short(vo.getCurrencyID());
        if ("discount".equals(pColumn)) return vo.getDiscount();
        if ("discountPercent".equals(pColumn)) return new Double(vo.getDiscountPercent());
        if ("vatID".equals(pColumn)) return new Long(vo.getVatID());
        if ("vat".equals(pColumn)) return vo.getVat();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("orderID".equals(pColumn)) return new Long(fTotal.getOrderID());
            if ("itemID".equals(pColumn)) return new Long(fTotal.getItemID());
            if ("itemName".equals(pColumn)) return fTotal.getItemName();
            if ("itemDescription".equals(pColumn)) return fTotal.getItemDescription();
            if ("itemSerialNr".equals(pColumn)) return fTotal.getItemSerialNr();
            if ("itemCount".equals(pColumn)) return new Long(fTotal.getItemCount());
            if ("shippingDate".equals(pColumn)) return fTotal.getShippingDate();
            if ("price".equals(pColumn)) return fTotal.getPrice();
            if ("currencyID".equals(pColumn)) return new Short(fTotal.getCurrencyID());
            if ("discount".equals(pColumn)) return fTotal.getDiscount();
            if ("discountPercent".equals(pColumn)) return new Double(fTotal.getDiscountPercent());
            if ("vatID".equals(pColumn)) return new Long(fTotal.getVatID());
            if ("vat".equals(pColumn)) return fTotal.getVat();
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
        if ("itemID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getItemID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("itemName".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getItemName();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("itemDescription".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getItemDescription();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("itemSerialNr".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getItemSerialNr();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("itemCount".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getItemCount();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("shippingDate".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getShippingDate();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("price".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPrice();
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
        if ("discount".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDiscount();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("discountPercent".equals(pColumn)) {
            Sorter sorter = new Sorter();
            double[] temp = new double[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDiscountPercent();
            }
            fSortOrder = sorter.sortDouble(temp, fSortOrder, up);
        }
        if ("vatID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getVatID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("vat".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getVat();
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

package com.eshop.table;

import java.util.*;
import com.eshop.vo.ItemViewVO;
import com.util.comparator.*;
import com.util.table.*;

public class ItemViewModel implements ITableModel {

    protected ItemViewVO fTotal;

    protected ItemViewVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public ItemViewModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (ItemViewVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        ItemViewVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("codeAn".equals(pColumn)) return vo.getCodeAn();
        if ("itemNumber".equals(pColumn)) return new Long(vo.getItemNumber());
        if ("supplierID".equals(pColumn)) return new Long(vo.getSupplierID());
        if ("name".equals(pColumn)) return vo.getName();
        if ("typeNum".equals(pColumn)) return new Long(vo.getTypeNum());
        if ("type".equals(pColumn)) return vo.getType();
        if ("guarantee".equals(pColumn)) return new Long(vo.getGuarantee());
        if ("availability".equals(pColumn)) return vo.getAvailability();
        if ("count".equals(pColumn)) return new Long(vo.getCount());
        if ("description".equals(pColumn)) return vo.getDescription();
        if ("remarks".equals(pColumn)) return vo.getRemarks();
        if ("price".equals(pColumn)) return vo.getPrice();
        if ("discount".equals(pColumn)) return vo.getDiscount();
        if ("discountPercent".equals(pColumn)) return new Double(vo.getDiscountPercent());
        if ("vatID".equals(pColumn)) return new Long(vo.getVatID());
        if ("pictureUrl".equals(pColumn)) return vo.getPictureUrl();
        if ("modified".equals(pColumn)) return vo.getModified();
        if ("validFrom".equals(pColumn)) return vo.getValidFrom();
        if ("validTo".equals(pColumn)) return vo.getValidTo();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("codeAn".equals(pColumn)) return fTotal.getCodeAn();
            if ("itemNumber".equals(pColumn)) return new Long(fTotal.getItemNumber());
            if ("supplierID".equals(pColumn)) return new Long(fTotal.getSupplierID());
            if ("name".equals(pColumn)) return fTotal.getName();
            if ("typeNum".equals(pColumn)) return new Long(fTotal.getTypeNum());
            if ("type".equals(pColumn)) return fTotal.getType();
            if ("guarantee".equals(pColumn)) return new Long(fTotal.getGuarantee());
            if ("availability".equals(pColumn)) return fTotal.getAvailability();
            if ("count".equals(pColumn)) return new Long(fTotal.getCount());
            if ("description".equals(pColumn)) return fTotal.getDescription();
            if ("remarks".equals(pColumn)) return fTotal.getRemarks();
            if ("price".equals(pColumn)) return fTotal.getPrice();
            if ("discount".equals(pColumn)) return fTotal.getDiscount();
            if ("discountPercent".equals(pColumn)) return new Double(fTotal.getDiscountPercent());
            if ("vatID".equals(pColumn)) return new Long(fTotal.getVatID());
            if ("pictureUrl".equals(pColumn)) return fTotal.getPictureUrl();
            if ("modified".equals(pColumn)) return fTotal.getModified();
            if ("validFrom".equals(pColumn)) return fTotal.getValidFrom();
            if ("validTo".equals(pColumn)) return fTotal.getValidTo();
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
        if ("codeAn".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCodeAn();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("itemNumber".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getItemNumber();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("supplierID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getSupplierID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("name".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getName();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("typeNum".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTypeNum();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("type".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getType();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("guarantee".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getGuarantee();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("availability".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAvailability();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("count".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCount();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("description".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDescription();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("remarks".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getRemarks();
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
        if ("pictureUrl".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPictureUrl();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("modified".equals(pColumn)) {
            fComparator = new TimestampComparator();
            java.sql.Timestamp[] temp = new java.sql.Timestamp[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getModified();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("validFrom".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getValidFrom();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("validTo".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getValidTo();
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

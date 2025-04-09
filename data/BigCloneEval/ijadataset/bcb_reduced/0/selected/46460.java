package com.mat.table;

import java.math.BigDecimal;
import java.util.*;
import com.mat.vo.ItemVO;
import com.util.comparator.*;
import com.util.table.*;

public class ItemModel implements ITableModel {

    protected ItemVO fTotal;

    protected ItemVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public ItemModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (ItemVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
        fTotal = computeTotal(fTableData);
    }

    private ItemVO computeTotal(ItemVO[] data) {
        ItemVO total = null;
        if (data != null && data.length > 0) {
            total = new ItemVO();
            BigDecimal priceTotal = new BigDecimal(0);
            BigDecimal priceBuyTotal = new BigDecimal(0);
            BigDecimal priceRecommendedTotal = new BigDecimal(0);
            BigDecimal priceRecommendedDiscountTotal = new BigDecimal(0);
            long countTotal = 0;
            for (int i = 0; i < data.length; i++) {
                countTotal = countTotal + data[i].getCount();
                priceTotal = priceTotal.add(data[i].getPrice().multiply(new BigDecimal(data[i].getCount())));
                priceBuyTotal = priceBuyTotal.add(data[i].getPriceBuy().multiply(new BigDecimal(data[i].getCount())));
                priceRecommendedTotal = priceRecommendedTotal.add(data[i].getPriceRecommended().multiply(new BigDecimal(data[i].getCount())));
                priceRecommendedDiscountTotal = priceRecommendedDiscountTotal.add(data[i].getPriceRecommendedDiscount());
            }
            total.setCount(countTotal);
            total.setPrice(priceTotal);
            total.setPriceBuy(priceBuyTotal);
            total.setPriceRecommended(priceRecommendedTotal);
            total.setPriceRecommendedDiscount(priceRecommendedDiscountTotal);
        }
        return total;
    }

    public Object getValueAt(int pRow, String pColumn) {
        ItemVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("codeAn".equals(pColumn)) return vo.getCodeAn();
        if ("itemNumber".equals(pColumn)) return new Long(vo.getItemNumber());
        if ("supplierID".equals(pColumn)) return new Long(vo.getSupplierID());
        if ("name".equals(pColumn)) return vo.getName();
        if ("typeNum".equals(pColumn)) return new Long(vo.getTypeNum());
        if ("type".equals(pColumn)) return vo.getType();
        if ("category".equals(pColumn)) return new Long(vo.getCategory());
        if ("subCategory".equals(pColumn)) return new Long(vo.getSubCategory());
        if ("guarantee".equals(pColumn)) return new Long(vo.getGuarantee());
        if ("availability".equals(pColumn)) return vo.getAvailability();
        if ("count".equals(pColumn)) return new Long(vo.getCount());
        if ("description".equals(pColumn)) return vo.getDescription();
        if ("remarks".equals(pColumn)) return vo.getRemarks();
        if ("price".equals(pColumn)) return vo.getPrice();
        if ("discount".equals(pColumn)) return vo.getDiscount();
        if ("discountPercent".equals(pColumn)) return new Double(vo.getDiscountPercent());
        if ("priceBuy".equals(pColumn)) return vo.getPriceBuy();
        if ("priceRecommended".equals(pColumn)) return vo.getPriceRecommended();
        if ("priceRecommendedDiscount".equals(pColumn)) return vo.getPriceRecommendedDiscount();
        if ("priceType".equals(pColumn)) return new Long(vo.getPriceType());
        if ("vatID".equals(pColumn)) return new Long(vo.getVatID());
        if ("barCode".equals(pColumn)) return vo.getBarCode();
        if ("pictureUrl".equals(pColumn)) return vo.getPictureUrl();
        if ("weightKg".equals(pColumn)) return new Double(vo.getWeightKg());
        if ("lengthM".equals(pColumn)) return new Double(vo.getLengthM());
        if ("widthM".equals(pColumn)) return new Double(vo.getWidthM());
        if ("heightM".equals(pColumn)) return new Double(vo.getHeightM());
        if ("supplierStock".equals(pColumn)) return new Long(vo.getSupplierStock());
        if ("size".equals(pColumn)) return new Double(vo.getSize());
        if ("sizeUnit".equals(pColumn)) return vo.getSizeUnit();
        if ("datasheetLink".equals(pColumn)) return vo.getDatasheetLink();
        if ("manufacturerID".equals(pColumn)) return new Long(vo.getManufacturerID());
        if ("manufacturer".equals(pColumn)) return vo.getManufacturer();
        if ("manufacturerModelNr".equals(pColumn)) return vo.getManufacturerModelNr();
        if ("manufacturerLink".equals(pColumn)) return vo.getManufacturerLink();
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
            if ("category".equals(pColumn)) return new Long(fTotal.getCategory());
            if ("subCategory".equals(pColumn)) return new Long(fTotal.getSubCategory());
            if ("guarantee".equals(pColumn)) return new Long(fTotal.getGuarantee());
            if ("availability".equals(pColumn)) return fTotal.getAvailability();
            if ("count".equals(pColumn)) return new Long(fTotal.getCount());
            if ("description".equals(pColumn)) return fTotal.getDescription();
            if ("remarks".equals(pColumn)) return fTotal.getRemarks();
            if ("price".equals(pColumn)) return fTotal.getPrice();
            if ("discount".equals(pColumn)) return fTotal.getDiscount();
            if ("discountPercent".equals(pColumn)) return new Double(fTotal.getDiscountPercent());
            if ("priceBuy".equals(pColumn)) return fTotal.getPriceBuy();
            if ("priceRecommended".equals(pColumn)) return fTotal.getPriceRecommended();
            if ("priceRecommendedDiscount".equals(pColumn)) return fTotal.getPriceRecommendedDiscount();
            if ("priceType".equals(pColumn)) return new Long(fTotal.getPriceType());
            if ("vatID".equals(pColumn)) return new Long(fTotal.getVatID());
            if ("barCode".equals(pColumn)) return fTotal.getBarCode();
            if ("pictureUrl".equals(pColumn)) return fTotal.getPictureUrl();
            if ("weightKg".equals(pColumn)) return new Double(fTotal.getWeightKg());
            if ("lengthM".equals(pColumn)) return new Double(fTotal.getLengthM());
            if ("widthM".equals(pColumn)) return new Double(fTotal.getWidthM());
            if ("heightM".equals(pColumn)) return new Double(fTotal.getHeightM());
            if ("supplierStock".equals(pColumn)) return new Long(fTotal.getSupplierStock());
            if ("size".equals(pColumn)) return new Double(fTotal.getSize());
            if ("sizeUnit".equals(pColumn)) return fTotal.getSizeUnit();
            if ("datasheetLink".equals(pColumn)) return fTotal.getDatasheetLink();
            if ("manufacturerID".equals(pColumn)) return new Long(fTotal.getManufacturerID());
            if ("manufacturer".equals(pColumn)) return fTotal.getManufacturer();
            if ("manufacturerModelNr".equals(pColumn)) return fTotal.getManufacturerModelNr();
            if ("manufacturerLink".equals(pColumn)) return fTotal.getManufacturerLink();
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
        if ("category".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCategory();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("subCategory".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getSubCategory();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
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
        if ("priceBuy".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPriceBuy();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("priceRecommended".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPriceRecommended();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("priceRecommendedDiscount".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPriceRecommendedDiscount();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("priceType".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPriceType();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("vatID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getVatID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("barCode".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBarCode();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("pictureUrl".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPictureUrl();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("weightKg".equals(pColumn)) {
            Sorter sorter = new Sorter();
            double[] temp = new double[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getWeightKg();
            }
            fSortOrder = sorter.sortDouble(temp, fSortOrder, up);
        }
        if ("lengthM".equals(pColumn)) {
            Sorter sorter = new Sorter();
            double[] temp = new double[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getLengthM();
            }
            fSortOrder = sorter.sortDouble(temp, fSortOrder, up);
        }
        if ("widthM".equals(pColumn)) {
            Sorter sorter = new Sorter();
            double[] temp = new double[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getWidthM();
            }
            fSortOrder = sorter.sortDouble(temp, fSortOrder, up);
        }
        if ("heightM".equals(pColumn)) {
            Sorter sorter = new Sorter();
            double[] temp = new double[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getHeightM();
            }
            fSortOrder = sorter.sortDouble(temp, fSortOrder, up);
        }
        if ("supplierStock".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getSupplierStock();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("size".equals(pColumn)) {
            Sorter sorter = new Sorter();
            double[] temp = new double[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getSize();
            }
            fSortOrder = sorter.sortDouble(temp, fSortOrder, up);
        }
        if ("sizeUnit".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getSizeUnit();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("datasheetLink".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDatasheetLink();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("manufacturerID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getManufacturerID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("manufacturer".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getManufacturer();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("manufacturerModelNr".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getManufacturerModelNr();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("manufacturerLink".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getManufacturerLink();
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

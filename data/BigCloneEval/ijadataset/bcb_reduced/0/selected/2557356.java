package com.be.table;

import java.util.*;
import com.be.vo.BankVO;
import com.util.comparator.*;
import com.util.table.*;

public class BankModel implements ITableModel {

    protected BankVO fTotal;

    protected BankVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public BankModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (BankVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        BankVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("bankGroup".equals(pColumn)) return new Long(vo.getBankGroup());
        if ("bcNr".equals(pColumn)) return new Long(vo.getBcNr());
        if ("branchID".equals(pColumn)) return vo.getBranchID();
        if ("bcNrNew".equals(pColumn)) return new Long(vo.getBcNrNew());
        if ("sicNr".equals(pColumn)) return vo.getSicNr();
        if ("headOffice".equals(pColumn)) return new Long(vo.getHeadOffice());
        if ("bcType".equals(pColumn)) return new Long(vo.getBcType());
        if ("sic".equals(pColumn)) return new Long(vo.getSic());
        if ("eurosic".equals(pColumn)) return new Long(vo.getEurosic());
        if ("lang".equals(pColumn)) return new Long(vo.getLang());
        if ("nameShort".equals(pColumn)) return vo.getNameShort();
        if ("name".equals(pColumn)) return vo.getName();
        if ("poBox".equals(pColumn)) return vo.getPoBox();
        if ("domizil".equals(pColumn)) return vo.getDomizil();
        if ("city".equals(pColumn)) return vo.getCity();
        if ("state".equals(pColumn)) return vo.getState();
        if ("zipCode".equals(pColumn)) return vo.getZipCode();
        if ("telefon".equals(pColumn)) return vo.getTelefon();
        if ("fax".equals(pColumn)) return vo.getFax();
        if ("areaCode".equals(pColumn)) return vo.getAreaCode();
        if ("countryID".equals(pColumn)) return new Long(vo.getCountryID());
        if ("postAccount".equals(pColumn)) return vo.getPostAccount();
        if ("swiftBic".equals(pColumn)) return vo.getSwiftBic();
        if ("validFrom".equals(pColumn)) return vo.getValidFrom();
        if ("validTo".equals(pColumn)) return vo.getValidTo();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("bankGroup".equals(pColumn)) return new Long(fTotal.getBankGroup());
            if ("bcNr".equals(pColumn)) return new Long(fTotal.getBcNr());
            if ("branchID".equals(pColumn)) return fTotal.getBranchID();
            if ("bcNrNew".equals(pColumn)) return new Long(fTotal.getBcNrNew());
            if ("sicNr".equals(pColumn)) return fTotal.getSicNr();
            if ("headOffice".equals(pColumn)) return new Long(fTotal.getHeadOffice());
            if ("bcType".equals(pColumn)) return new Long(fTotal.getBcType());
            if ("sic".equals(pColumn)) return new Long(fTotal.getSic());
            if ("eurosic".equals(pColumn)) return new Long(fTotal.getEurosic());
            if ("lang".equals(pColumn)) return new Long(fTotal.getLang());
            if ("nameShort".equals(pColumn)) return fTotal.getNameShort();
            if ("name".equals(pColumn)) return fTotal.getName();
            if ("poBox".equals(pColumn)) return fTotal.getPoBox();
            if ("domizil".equals(pColumn)) return fTotal.getDomizil();
            if ("city".equals(pColumn)) return fTotal.getCity();
            if ("state".equals(pColumn)) return fTotal.getState();
            if ("zipCode".equals(pColumn)) return fTotal.getZipCode();
            if ("telefon".equals(pColumn)) return fTotal.getTelefon();
            if ("fax".equals(pColumn)) return fTotal.getFax();
            if ("areaCode".equals(pColumn)) return fTotal.getAreaCode();
            if ("countryID".equals(pColumn)) return new Long(fTotal.getCountryID());
            if ("postAccount".equals(pColumn)) return fTotal.getPostAccount();
            if ("swiftBic".equals(pColumn)) return fTotal.getSwiftBic();
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
        if ("bankGroup".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBankGroup();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("bcNr".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBcNr();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("branchID".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBranchID();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("bcNrNew".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBcNrNew();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("sicNr".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getSicNr();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("headOffice".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getHeadOffice();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("bcType".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBcType();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("sic".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getSic();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("eurosic".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getEurosic();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("lang".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getLang();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("nameShort".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getNameShort();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("name".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getName();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("poBox".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPoBox();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("domizil".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDomizil();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("city".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCity();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("state".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getState();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("zipCode".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getZipCode();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("telefon".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getTelefon();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("fax".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getFax();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("areaCode".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAreaCode();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("countryID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCountryID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("postAccount".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPostAccount();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("swiftBic".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getSwiftBic();
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

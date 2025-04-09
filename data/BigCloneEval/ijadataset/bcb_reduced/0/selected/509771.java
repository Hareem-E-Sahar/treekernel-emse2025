package com.pr.table;

import java.util.*;
import com.pr.vo.EmployeeVO;
import com.util.comparator.*;
import com.util.table.*;

public class EmployeeModel implements ITableModel {

    protected EmployeeVO fTotal;

    protected EmployeeVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public EmployeeModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (EmployeeVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        EmployeeVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("codeAn".equals(pColumn)) return vo.getCodeAn();
        if ("contactID".equals(pColumn)) return new Long(vo.getContactID());
        if ("mailAddressID".equals(pColumn)) return new Long(vo.getMailAddressID());
        if ("sex".equals(pColumn)) return vo.getSex();
        if ("nationalityID".equals(pColumn)) return new Long(vo.getNationalityID());
        if ("employeeNumber".equals(pColumn)) return vo.getEmployeeNumber();
        if ("ahvNumber".equals(pColumn)) return vo.getAhvNumber();
        if ("birthday".equals(pColumn)) return vo.getBirthday();
        if ("entry".equals(pColumn)) return vo.getEntry();
        if ("discharge".equals(pColumn)) return vo.getDischarge();
        if ("bankAccountID".equals(pColumn)) return new Long(vo.getBankAccountID());
        if ("ahvCode".equals(pColumn)) return new Long(vo.getAhvCode());
        if ("alvCode".equals(pColumn)) return new Long(vo.getAlvCode());
        if ("nbuCode".equals(pColumn)) return new Long(vo.getNbuCode());
        if ("buvCode".equals(pColumn)) return new Long(vo.getBuvCode());
        if ("bvgAmountEmployee".equals(pColumn)) return vo.getBvgAmountEmployee();
        if ("bvgAmountEmployer".equals(pColumn)) return vo.getBvgAmountEmployer();
        if ("bvgRiskPremium".equals(pColumn)) return vo.getBvgRiskPremium();
        if ("ktvID".equals(pColumn)) return new Long(vo.getKtvID());
        if ("qstCode".equals(pColumn)) return vo.getQstCode();
        if ("qstStateCode".equals(pColumn)) return vo.getQstStateCode();
        if ("qstDepotID".equals(pColumn)) return new Long(vo.getQstDepotID());
        if ("modified".equals(pColumn)) return vo.getModified();
        if ("validFrom".equals(pColumn)) return vo.getValidFrom();
        if ("validTo".equals(pColumn)) return vo.getValidTo();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("codeAn".equals(pColumn)) return fTotal.getCodeAn();
            if ("contactID".equals(pColumn)) return new Long(fTotal.getContactID());
            if ("mailAddressID".equals(pColumn)) return new Long(fTotal.getMailAddressID());
            if ("sex".equals(pColumn)) return fTotal.getSex();
            if ("nationalityID".equals(pColumn)) return new Long(fTotal.getNationalityID());
            if ("employeeNumber".equals(pColumn)) return fTotal.getEmployeeNumber();
            if ("ahvNumber".equals(pColumn)) return fTotal.getAhvNumber();
            if ("birthday".equals(pColumn)) return fTotal.getBirthday();
            if ("entry".equals(pColumn)) return fTotal.getEntry();
            if ("discharge".equals(pColumn)) return fTotal.getDischarge();
            if ("bankAccountID".equals(pColumn)) return new Long(fTotal.getBankAccountID());
            if ("ahvCode".equals(pColumn)) return new Long(fTotal.getAhvCode());
            if ("alvCode".equals(pColumn)) return new Long(fTotal.getAlvCode());
            if ("nbuCode".equals(pColumn)) return new Long(fTotal.getNbuCode());
            if ("buvCode".equals(pColumn)) return new Long(fTotal.getBuvCode());
            if ("bvgAmountEmployee".equals(pColumn)) return fTotal.getBvgAmountEmployee();
            if ("bvgAmountEmployer".equals(pColumn)) return fTotal.getBvgAmountEmployer();
            if ("bvgRiskPremium".equals(pColumn)) return fTotal.getBvgRiskPremium();
            if ("ktvID".equals(pColumn)) return new Long(fTotal.getKtvID());
            if ("qstCode".equals(pColumn)) return fTotal.getQstCode();
            if ("qstStateCode".equals(pColumn)) return fTotal.getQstStateCode();
            if ("qstDepotID".equals(pColumn)) return new Long(fTotal.getQstDepotID());
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
        if ("contactID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getContactID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("mailAddressID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getMailAddressID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("sex".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getSex();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("nationalityID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getNationalityID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("employeeNumber".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getEmployeeNumber();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("ahvNumber".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAhvNumber();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("birthday".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBirthday();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("entry".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getEntry();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("discharge".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDischarge();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("bankAccountID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBankAccountID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("ahvCode".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAhvCode();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("alvCode".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAlvCode();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("nbuCode".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getNbuCode();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("buvCode".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBuvCode();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("bvgAmountEmployee".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBvgAmountEmployee();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("bvgAmountEmployer".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBvgAmountEmployer();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("bvgRiskPremium".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBvgRiskPremium();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("ktvID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getKtvID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("qstCode".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getQstCode();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("qstStateCode".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getQstStateCode();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("qstDepotID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getQstDepotID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
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

package com.creditors.table;

import java.math.BigDecimal;
import java.util.*;
import com.creditors.vo.PFExtViewVO;
import com.mat.vo.ItemVO;
import com.util.comparator.*;
import com.util.table.*;

public class PFExtViewModel implements ITableModel {

    protected PFExtViewVO fTotal;

    protected PFExtViewVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public PFExtViewModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (PFExtViewVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
        fTotal = computeTotal(fTableData);
    }

    private PFExtViewVO computeTotal(PFExtViewVO[] data) {
        PFExtViewVO total = null;
        if (data != null && data.length > 0) {
            total = new PFExtViewVO();
            BigDecimal amountTotal = new BigDecimal(0);
            for (int i = 0; i < data.length; i++) {
                amountTotal = amountTotal.add(data[i].getAmount());
            }
            total.setAmount(amountTotal);
        }
        return total;
    }

    public Object getValueAt(int pRow, String pColumn) {
        PFExtViewVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("type".equals(pColumn)) return new Long(vo.getType());
        if ("principal".equals(pColumn)) return vo.getPrincipal();
        if ("currencyBook".equals(pColumn)) return vo.getCurrencyBook();
        if ("amount".equals(pColumn)) return vo.getAmount();
        if ("currencyPay".equals(pColumn)) return vo.getCurrencyPay();
        if ("country".equals(pColumn)) return vo.getCountry();
        if ("creationDate".equals(pColumn)) return vo.getCreationDate();
        if ("dueDate".equals(pColumn)) return vo.getDueDate();
        if ("postAccount".equals(pColumn)) return vo.getPostAccount();
        if ("bankAccount".equals(pColumn)) return vo.getBankAccount();
        if ("recipient".equals(pColumn)) return vo.getRecipient();
        if ("beneficiary".equals(pColumn)) return vo.getBeneficiary();
        if ("message4x35".equals(pColumn)) return vo.getMessage4x35();
        if ("orderer".equals(pColumn)) return vo.getOrderer();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("type".equals(pColumn)) return new Long(fTotal.getType());
            if ("principal".equals(pColumn)) return fTotal.getPrincipal();
            if ("currencyBook".equals(pColumn)) return fTotal.getCurrencyBook();
            if ("amount".equals(pColumn)) return fTotal.getAmount();
            if ("currencyPay".equals(pColumn)) return fTotal.getCurrencyPay();
            if ("country".equals(pColumn)) return fTotal.getCountry();
            if ("creationDate".equals(pColumn)) return fTotal.getCreationDate();
            if ("dueDate".equals(pColumn)) return fTotal.getDueDate();
            if ("postAccount".equals(pColumn)) return fTotal.getPostAccount();
            if ("bankAccount".equals(pColumn)) return fTotal.getBankAccount();
            if ("recipient".equals(pColumn)) return fTotal.getRecipient();
            if ("beneficiary".equals(pColumn)) return fTotal.getBeneficiary();
            if ("message4x35".equals(pColumn)) return fTotal.getMessage4x35();
            if ("orderer".equals(pColumn)) return fTotal.getOrderer();
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
        if ("type".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getType();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("principal".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPrincipal();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("currencyBook".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCurrencyBook();
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
        if ("currencyPay".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCurrencyPay();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("country".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCountry();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("creationDate".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCreationDate();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("dueDate".equals(pColumn)) {
            fComparator = new DateComparator();
            java.sql.Date[] temp = new java.sql.Date[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDueDate();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("postAccount".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPostAccount();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("bankAccount".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBankAccount();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("recipient".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getRecipient();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("beneficiary".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBeneficiary();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("message4x35".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getMessage4x35();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("orderer".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getOrderer();
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

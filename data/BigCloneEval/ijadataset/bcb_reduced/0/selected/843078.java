package com.creditors.table;

import java.util.*;
import com.creditors.vo.TA827VO;
import com.util.comparator.*;
import com.util.table.*;

public class TA827Model implements ITableModel {

    protected TA827VO fTotal;

    protected TA827VO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public TA827Model() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (TA827VO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        TA827VO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("type".equals(pColumn)) return new Long(vo.getType());
        if ("salaryPayment".equals(pColumn)) return vo.getSalaryPayment();
        if ("referenceNumber".equals(pColumn)) return vo.getReferenceNumber();
        if ("debitAccountID".equals(pColumn)) return new Long(vo.getDebitAccountID());
        if ("principalID".equals(pColumn)) return new Long(vo.getPrincipalID());
        if ("currencyID".equals(pColumn)) return new Short(vo.getCurrencyID());
        if ("amount".equals(pColumn)) return vo.getAmount();
        if ("creationDate".equals(pColumn)) return vo.getCreationDate();
        if ("dueDate".equals(pColumn)) return vo.getDueDate();
        if ("bankAccountID".equals(pColumn)) return new Long(vo.getBankAccountID());
        if ("postAccountID".equals(pColumn)) return new Long(vo.getPostAccountID());
        if ("recipientID".equals(pColumn)) return new Long(vo.getRecipientID());
        if ("beneficiaryID".equals(pColumn)) return new Long(vo.getBeneficiaryID());
        if ("message4x28".equals(pColumn)) return vo.getMessage4x28();
        if ("ordererID".equals(pColumn)) return new Long(vo.getOrdererID());
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("type".equals(pColumn)) return new Long(fTotal.getType());
            if ("salaryPayment".equals(pColumn)) return fTotal.getSalaryPayment();
            if ("referenceNumber".equals(pColumn)) return fTotal.getReferenceNumber();
            if ("debitAccountID".equals(pColumn)) return new Long(fTotal.getDebitAccountID());
            if ("principalID".equals(pColumn)) return new Long(fTotal.getPrincipalID());
            if ("currencyID".equals(pColumn)) return new Short(fTotal.getCurrencyID());
            if ("amount".equals(pColumn)) return fTotal.getAmount();
            if ("creationDate".equals(pColumn)) return fTotal.getCreationDate();
            if ("dueDate".equals(pColumn)) return fTotal.getDueDate();
            if ("bankAccountID".equals(pColumn)) return new Long(fTotal.getBankAccountID());
            if ("postAccountID".equals(pColumn)) return new Long(fTotal.getPostAccountID());
            if ("recipientID".equals(pColumn)) return new Long(fTotal.getRecipientID());
            if ("beneficiaryID".equals(pColumn)) return new Long(fTotal.getBeneficiaryID());
            if ("message4x28".equals(pColumn)) return fTotal.getMessage4x28();
            if ("ordererID".equals(pColumn)) return new Long(fTotal.getOrdererID());
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
        if ("salaryPayment".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getSalaryPayment();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("referenceNumber".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getReferenceNumber();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("debitAccountID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDebitAccountID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("principalID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPrincipalID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("currencyID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            short[] temp = new short[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCurrencyID();
            }
            fSortOrder = sorter.sortShort(temp, fSortOrder, up);
        }
        if ("amount".equals(pColumn)) {
            fComparator = new BigDecimalComparator();
            java.math.BigDecimal[] temp = new java.math.BigDecimal[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getAmount();
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
        if ("bankAccountID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBankAccountID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("postAccountID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPostAccountID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("recipientID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getRecipientID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("beneficiaryID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBeneficiaryID();
            }
            fSortOrder = sorter.sortLong(temp, fSortOrder, up);
        }
        if ("message4x28".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getMessage4x28();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("ordererID".equals(pColumn)) {
            Sorter sorter = new Sorter();
            long[] temp = new long[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getOrdererID();
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

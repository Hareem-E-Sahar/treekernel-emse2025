package com.creditors.table;

import java.util.*;
import com.creditors.vo.TAExtViewVO;
import com.util.comparator.*;
import com.util.table.*;

public class TAExtViewModel implements ITableModel {

    protected TAExtViewVO fTotal;

    protected TAExtViewVO[] fTableData;

    protected String fSortedColumn;

    protected int[] fSortOrder;

    protected Comparator fComparator;

    public TAExtViewModel() {
        super();
    }

    public void setData(Object[] data) {
        fTableData = (TAExtViewVO[]) data;
        fSortOrder = new int[(fTableData != null) ? fTableData.length : 0];
        for (int i = 0; i < fSortOrder.length; i++) {
            fSortOrder[i] = i;
        }
    }

    public Object getValueAt(int pRow, String pColumn) {
        TAExtViewVO vo = fTableData[fSortOrder[pRow]];
        if ("id".equals(pColumn)) return new Long(vo.getId());
        if ("type".equals(pColumn)) return new Long(vo.getType());
        if ("salaryPayment".equals(pColumn)) return vo.getSalaryPayment();
        if ("referenceNumber".equals(pColumn)) return vo.getReferenceNumber();
        if ("debitAccount".equals(pColumn)) return vo.getDebitAccount();
        if ("principal".equals(pColumn)) return vo.getPrincipal();
        if ("currency".equals(pColumn)) return vo.getCurrency();
        if ("amount".equals(pColumn)) return vo.getAmount();
        if ("creationDate".equals(pColumn)) return vo.getCreationDate();
        if ("dueDate".equals(pColumn)) return vo.getDueDate();
        if ("bankAccount".equals(pColumn)) return vo.getBankAccount();
        if ("postAccount".equals(pColumn)) return vo.getPostAccount();
        if ("recipient".equals(pColumn)) return vo.getRecipient();
        if ("beneficiary".equals(pColumn)) return vo.getBeneficiary();
        if ("message".equals(pColumn)) return vo.getMessage();
        if ("orderer".equals(pColumn)) return vo.getOrderer();
        return null;
    }

    public Object getTotalValueAt(String pColumn) {
        if (fTotal != null) {
            if ("id".equals(pColumn)) return new Long(fTotal.getId());
            if ("type".equals(pColumn)) return new Long(fTotal.getType());
            if ("salaryPayment".equals(pColumn)) return fTotal.getSalaryPayment();
            if ("referenceNumber".equals(pColumn)) return fTotal.getReferenceNumber();
            if ("debitAccount".equals(pColumn)) return fTotal.getDebitAccount();
            if ("principal".equals(pColumn)) return fTotal.getPrincipal();
            if ("currency".equals(pColumn)) return fTotal.getCurrency();
            if ("amount".equals(pColumn)) return fTotal.getAmount();
            if ("creationDate".equals(pColumn)) return fTotal.getCreationDate();
            if ("dueDate".equals(pColumn)) return fTotal.getDueDate();
            if ("bankAccount".equals(pColumn)) return fTotal.getBankAccount();
            if ("postAccount".equals(pColumn)) return fTotal.getPostAccount();
            if ("recipient".equals(pColumn)) return fTotal.getRecipient();
            if ("beneficiary".equals(pColumn)) return fTotal.getBeneficiary();
            if ("message".equals(pColumn)) return fTotal.getMessage();
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
        if ("debitAccount".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getDebitAccount();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("principal".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getPrincipal();
            }
            sort(temp, 0, temp.length - 1, up);
        }
        if ("currency".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getCurrency();
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
        if ("bankAccount".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getBankAccount();
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
        if ("message".equals(pColumn)) {
            fComparator = new StringComparator();
            String[] temp = new String[fTableData.length];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = fTableData[i].getMessage();
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

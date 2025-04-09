package net.sf.raptor.ui.swing.table;

import java.text.Collator;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Diese Klasse beinhaltet die Funktionalit�t zur aufsteigenden
 * bzw. absteigenden Sortierung von Tabellenspalten.
 *
 * @author  thomasg
 * @version 1.0, 28. November 2001
 */
public class TableSorter implements Comparator {

    /** Die CVS-Id dieser Klasse */
    public static final String $cvsid = "$Id: TableSorter.java,v 1.1 2004/12/16 15:25:37 thomasgoertz Exp $";

    private Class sortColumnClass;

    private Comparator columnSorter;

    private boolean ascending = true;

    private int columnToBeSorted = 0;

    private int columnNumber;

    private AbstractSortableTableModel tableModel;

    private Collator collator;

    /**
     * Konstruktor. Bekommt das TableModel der Tabelle �bergeben, die sortiert werden soll.
     */
    public TableSorter(AbstractSortableTableModel model) {
        tableModel = model;
        collator = Collator.getInstance(Locale.getDefault());
    }

    /**
     * Vergleicht zwei Objekte. Ist o1&lt;o2 wird -1 zur�ckgegeben, ist
     * o1&gt;o2 wird +1 zur�ckgegeben, bei Gleicheit wird 0 zur�ckgegeben.
     */
    private int compareInternal(Object o1, Object o2) {
        int retValue;
        if (o1 == null && o2 == null) retValue = 0; else if (o1 == null) retValue = -1; else if (o2 == null) retValue = 1; else if (Number.class.isAssignableFrom(sortColumnClass)) {
            Number n1 = (Number) o1;
            double d1 = n1.doubleValue();
            Number n2 = (Number) o2;
            double d2 = n2.doubleValue();
            if (d1 < d2) retValue = -1; else if (d1 > d2) retValue = 1; else retValue = 0;
        } else if (Date.class.isAssignableFrom(sortColumnClass)) {
            long l1 = ((Date) o1).getTime();
            long l2 = ((Date) o2).getTime();
            if (l1 < l2) retValue = -1; else if (l1 > l2) retValue = 1; else retValue = 0;
        } else if (sortColumnClass == Boolean.class) {
            Boolean bool1 = (Boolean) o1;
            boolean b1 = bool1.booleanValue();
            Boolean bool2 = (Boolean) o2;
            boolean b2 = bool2.booleanValue();
            if (b1 == b2) retValue = 0; else if (b1) retValue = 1; else retValue = -1;
        } else {
            String s1 = o1.toString();
            String s2 = o2.toString();
            retValue = collator.compare(s1, s2);
        }
        return retValue;
    }

    public void setAscending(boolean b) {
        this.ascending = b;
    }

    public boolean isAscending() {
        return ascending;
    }

    /**
     * F�hrt einen Vergleich zwischen zwei Objekten durch. Pr�ft, ob
     * der Benutzer einen eigenen Sorter spezifiziert hat. Wenn ja,
     * wird dessen Sortiermethode benutzt.
     * Hat der Benutzer keinen eigenen Sorter spezifiziert,
     * wird die Methode compareInternal zum Vergleichen aufgerufen.
     */
    public int compare(Object o1, Object o2) {
        int retValue;
        if (columnSorter == null) {
            retValue = compareInternal(o1, o2);
        } else {
            retValue = columnSorter.compare(o1, o2);
        }
        return ascending ? retValue : -retValue;
    }

    /**
     * Diese Methode bereitet die Sortierung der �bergebenen
     * Tabellenspalte vor und ruft die Methode auf, die den
     * Sortieralgorithmus implementiert.
     */
    void sort(int column, int[] indices) {
        if (tableModel.getRowCount() < 2) return;
        quicksort(column, indices);
        columnToBeSorted = column;
    }

    /**
     * Implementierung des Quicksort-Algorithmus. Bekommt als Parameter
     * die Spalte und das Index-Array des Table-Models �bergeben.
     */
    private void quicksort(int column, int[] indices) {
        sortColumnClass = tableModel.getColumnClass(column);
        columnSorter = tableModel.getColumnSorter(column);
        columnNumber = column;
        int links = 0;
        int rechts = indices.length - 1;
        quicksortInternal(indices, links, rechts);
    }

    /**
     * Hilfsmethode f�r quicksort(). Beinhaltet den eigentlichen
     * Sortieralgorithmus und wird rekursiv aufgerufen.
     */
    private void quicksortInternal(int[] indices, int begin, int end) {
        if (begin >= end) return;
        int j = end;
        int i = begin;
        int mitte = (i + j) / 2;
        Object mittelElement = tableModel.getValueAt(mitte, columnNumber);
        while (j >= i) {
            while (compare(tableModel.getValueAt(i, columnNumber), mittelElement) < 0) i++;
            while (compare(tableModel.getValueAt(j, columnNumber), mittelElement) > 0) j--;
            if (i <= j) {
                int x = indices[i];
                indices[i] = indices[j];
                indices[j] = x;
                i++;
                j--;
            }
        }
        quicksortInternal(indices, begin, j);
        quicksortInternal(indices, i, end);
    }
}

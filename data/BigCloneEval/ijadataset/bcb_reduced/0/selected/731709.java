package org.freeworld.prilib.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.freeworld.prilib.column.BaseColumnSchemaGroup;
import org.freeworld.prilib.column.ColumnSchema;
import org.freeworld.prilib.column.ColumnSchemaGroup;
import org.freeworld.prilib.impl.sync.NoTableLockManager;
import org.freeworld.prilib.impl.sync.TableLockManager;
import org.freeworld.prilib.impl.sync.TableSyncronizable;
import org.freeworld.prilib.row.DefaultableTableRow;
import org.freeworld.prilib.row.TableRow;
import org.freeworld.prilib.table.Table;

/**
 * <p>
 * This is a collection of helper methods used by any module that does a lot of
 * work on <b>Table</b>s and its derivitives.
 * </p>
 * 
 * @author dchemko
 */
public class TableTools {

    /**
    * <p>Static reference to an empty lock manager so that calls to
    * getTableLockManager who don't have lock managers won't create a new lock
    * manager every time.</p>
    */
    private static final TableLockManager NO_TABLE_LOCK_MANAGER = new NoTableLockManager();

    /**
    * <p> Dumps the Tables schema to stdout </p>
    * 
    * @param table - The table whos schema is to be dumped
    */
    public static void printSchema(Table table) {
        int i = 0;
        System.out.println("Table Schema\n   Type: " + table.getClass().getName());
        for (ColumnSchema schema : table.getColumnSchemaGroup()) {
            System.out.println("   Column " + i++ + ": " + schema.toString());
        }
    }

    /**
    * <p> Dumps the tabular data of a Table to stdout </p>
    * 
    * @param table - The table whos tabular data is to be dumped
    */
    public static void printTabularData(Table table) {
        System.out.println("Table Data");
        for (int i = 0; i < table.getRowCount(); i++) {
            System.out.print("   Row " + i + ": ");
            for (int j = 0; j < table.getColumnCount(); j++) {
                if (j != 0) System.out.print(", ");
                System.out.print(table.getValue(i, j));
            }
            System.out.println();
        }
    }

    /**
    * Fetches the numeric index of the specified String indexed column name
    * 
    * @param table - Table to search
    * 
    * @param columnName - Name of the column to find
    * 
    * @return The numeric index of the column name in the table, or -1 if the
    * column name wasn't found
    */
    public static int getColumnIndex(Table table, String columnName) {
        String[] names = table.getColumnNames();
        for (int i = 0; i < names.length; i++) {
            if (columnName.equals(names[i])) return i;
        }
        return -1;
    }

    /**
    * <p> Deep copies a list of all column schemas into a form that can be input
    * into a table or table row constructor </p>
    * 
    * @param table - The table to extract the column schema information from
    * 
    * @return The deep copied instance of the requested table's schemas
    */
    public static ColumnSchemaGroup<ColumnSchema> copyColumnSchemas(Table table) {
        ColumnSchemaGroup<ColumnSchema> retr = new BaseColumnSchemaGroup<ColumnSchema>();
        retr.addAll(table.getColumnSchemaGroup());
        return retr;
    }

    /**
    * <p> Copies all the data from the source table into the destination table.
    * It does not clear the destination table of any data that might already
    * exist within it. It also doesn't check that the column schemas of the two
    * tables are identical. The the column schemas differ in some way, the
    * destination table may reject the row additions causing this method to
    * fail. </p>
    * 
    * @param sourceTable - Source table
    * 
    * @param destinationTable - Destined table
    */
    public static void copyTableUnchecked(Table sourceTable, Table destinationTable) {
        String[] srcNames = sourceTable.getColumnNames();
        for (int i = 0; i < srcNames.length; i++) {
            if (destinationTable.getColumnSchema(srcNames[i]) == null) destinationTable.addColumn(sourceTable.getColumnSchema(srcNames[i]));
        }
        for (int i = 0; i < sourceTable.getRowCount(); i++) destinationTable.addRow(sourceTable.getRow(i).newCopy(false));
    }

    /**
    * <p> Adds a new index to an array and passes the new array handle back as a
    * result </p>
    * 
    * @param <T> - The type of array defined
    * 
    * @param array - The handle to the original array
    * 
    * @param index - The index of the original array to insert the new array
    * cell
    * 
    * @param newValue - The value to be inserted into the new array cell
    * 
    * @return The newly constructed instance of the original array with the new
    * value
    */
    public static <T> T[] insertIntoArray(T[] array, int index, T newValue) {
        @SuppressWarnings("unchecked") T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
        if (index > 0) System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = newValue;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }

    /**
    * <p> Removes an array from an array and returns the new instance of the
    * array to the caller </p>
    * 
    * @param <T> - The type of the array
    * 
    * @param index - The index within the array to remove
    * 
    * @param array - The original array
    * 
    * @return The new instance of the array that doesn't contain the index
    * specified
    */
    public static <T> T[] removeFromArray(int index, T[] array) {
        @SuppressWarnings("unchecked") T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - 1);
        if (index > 0) System.arraycopy(array, 0, newArray, 0, index);
        System.arraycopy(array, index + 1, newArray, index, array.length - index - 1);
        return newArray;
    }

    /**
    * Clears the values of a specified table row to object null value.
    * 
    * Some fancy table row implementations may support a pre-canned default
    * value using the DefaultableTableRow interface; In which case the default
    * value (or null) will be populated into the cell.
    * 
    * @param row - The row to be cleared
    */
    public static void clearRows(TableRow row) {
        if (row == null) return;
        String[] columns = row.getColumnNames();
        if (row instanceof DefaultableTableRow) {
            for (int i = 0; i < columns.length; i++) row.setValue(columns[i], ((DefaultableTableRow) row).getDefaultValue(columns[i]));
        } else {
            for (int i = 0; i < columns.length; i++) row.setValue(columns[i], null);
        }
    }

    /**
    * Copies the specified array, truncating or padding with nulls (if
    * necessary) so the copy has the specified length. For all indices that are
    * valid in both the original array and the copy, the two arrays will contain
    * identical values. For any indices that are valid in the copy but not the
    * original, the copy will contain <tt>null</tt>. Such indices will exist if
    * and only if the specified length is greater than that of the original
    * array. The resulting array is of exactly the same class as the original
    * array.
    * 
    * @param <T> - The type of the original array
    * 
    * @param original - the array to be copied
    * 
    * @param newLength - the length of the copy to be returned
    * 
    * @return a copy of the original array, truncated or padded with nulls to
    * obtain the specified length
    * 
    * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
    * 
    * @throws NullPointerException if <tt>original</tt> is null
    */
    @SuppressWarnings("unchecked")
    public static <T> T[] copyOf(T[] original, int newLength) {
        return (T[]) copyOf(original, newLength, original.getClass());
    }

    /**
    * Copies the specified array, truncating or padding with nulls (if
    * necessary) so the copy has the specified length. For all indices that are
    * valid in both the original array and the copy, the two arrays will contain
    * identical values. For any indices that are valid in the copy but not the
    * original, the copy will contain <tt>null</tt>. Such indices will exist if
    * and only if the specified length is greater than that of the original
    * array. The resulting array is of the class <tt>newType</tt>.
    * 
    * @param <U> - The original type of the array
    * 
    * @param <T> - The desired type of the return array
    * 
    * @param original - the array to be copied
    * 
    * @param newLength - the length of the copy to be returned
    * 
    * @param newType - the class of the copy to be returned
    * 
    * @return a copy of the original array, truncated or padded with nulls to
    * obtain the specified length
    * 
    * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
    * 
    * @throws NullPointerException if <tt>original</tt> is null
    * 
    * @throws ArrayStoreException if an element copied from <tt>original</tt> is
    * not of a runtime type that can be stored in an array of class
    * <tt>newType</tt>
    */
    @SuppressWarnings("unchecked")
    public static <T, U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
        T[] copy = ((Object) newType == (Object) Object[].class) ? (T[]) new Object[newLength] : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
        return copy;
    }

    /**
    * <p>Searches a table for the value contained within a column.</p>
    * 
    * @param table - Table to search
    * @param columnIndex - Column index to search
    * @param value - The value to search for
    * @return <b>true</b> The value exists within the table<br/><b>false</b> The
    * table doesn't contain the value
    */
    public static boolean hasValue(Table table, int columnIndex, Object value) {
        return hasValue(table, table.getColumnNames()[columnIndex], value);
    }

    /**
    * <p>Searches a table for the value contained within a column and returns
    * true if it is found.</p>
    * 
    * @param table - Table to search
    * @param searchColumnName - Column name to search for
    * @param value - The value to search for
    * @return <b>true</b> The value exists within the table<br/><b>false</b> The
    * table doesn't contain the value
    */
    public static boolean hasValue(Table table, String searchColumnName, Object value) {
        if (table == null || searchColumnName == null || searchColumnName.length() < 1) throw new IllegalArgumentException();
        return getFirstRowIndex(table, searchColumnName, value) > -1;
    }

    /**
    * <p>Searches a table for the value contained within a column and returns
    * the first found row index of it.</p>
    * 
    * @param table - Table to search
    * @param searchColumn - Column name to search for
    * @param value - The value to search for
    * @return The row index of the first found instance of the search value
    */
    public static int getFirstRowIndex(Table table, String searchColumn, Object value) {
        for (int i = 0; i < table.getRowCount(); i++) {
            Object retr = table.getValue(i, searchColumn);
            if (value == null) {
                if (retr == null) return i; else continue;
            } else {
                if (retr == null) continue; else if (value.equals(retr)) return i;
            }
        }
        return -1;
    }

    /**
    * <p>Searches a table for the value contained within a column and returns
    * the last row index of it.</p>
    * 
    * @param table - Table to search
    * @param searchColumn - Column name to search for
    * @param value - The value to search for
    * @return The row index of the last found instance of the search value
    */
    public static int getLastRowIndex(Table table, String searchColumn, Object value) {
        for (int i = table.getRowCount() - 1; i > -1; i--) {
            Object retr = table.getValue(i, searchColumn);
            if (value == null) {
                if (retr == null) return i; else continue;
            } else {
                if (retr == null) continue; else if (value.equals(retr)) return i;
            }
        }
        return -1;
    }

    /**
    * <p>Builds a one time list of values from the column specified. Because the
    * table doesn't have a specific type definition know before inspection, we
    * cannot return a genericised set for consumption.</p>
    * 
    * @param table - The table who's values will be extracted from
    * @param columnName - The column who's keys will be extracted
    * @return The set of values extracted from the table's specified column
    */
    public static Set<Object> createColumnKeySet(Table table, String columnName) {
        Set<Object> retr = new HashSet<Object>();
        getTableLockManager(table).aquireColumnSchemaReader(table);
        getTableLockManager(table).aquireRowReader(table);
        try {
            for (int i = 0; i < table.getRowCount(); i++) retr.add(table.getValue(i, columnName));
        } finally {
            getTableLockManager(table).releaseRowReader(table);
            getTableLockManager(table).releaseColumnSchemaReader(table);
        }
        return retr;
    }

    /**
    * <p>Builds a one time list of values from the columns specified. Because
    * the table doesn't have a specific type definition know before inspection,
    * we cannot return a genericised set for consumption. The return is a set
    * that contains a list of objects as specified by the array of column
    * names.</p>
    * 
    * @param table - The table who's values will be extracted from
    * @param columnName - The column who's keys will be extracted
    * @return The set of values extracted from the table's specified column
    */
    public static Set<List<Object>> createColumnGroupKeySet(Table table, String[] columnNames) {
        Set<List<Object>> retr = new HashSet<List<Object>>();
        getTableLockManager(table).aquireColumnSchemaReader(table);
        getTableLockManager(table).aquireRowReader(table);
        try {
            List<Object> values = new ArrayList<Object>();
            for (int i = 0; i < table.getRowCount(); i++) {
                for (String columnName : columnNames) values.add(table.getValue(i, columnName));
                if (retr.contains(values)) {
                    retr.add(values);
                    values = new ArrayList<Object>();
                } else values.clear();
            }
        } finally {
            getTableLockManager(table).releaseRowReader(table);
            getTableLockManager(table).releaseColumnSchemaReader(table);
        }
        return retr;
    }

    /**
    * <p>Dumps the general information about a table and all of its
    * sub-tables.</p>
    * 
    * @param table - Table to be dumped
    */
    public static void dumpTableAssoc(Table table) {
        dumpTableAssoc(table, 0);
    }

    /**
    * <p>Dumps the general information about a table and all of its
    * sub-tables.</p>
    * 
    * @param table - Table to be dumped
    * @param currentDepth - The indent spacing of the current position within
    * the table graph
    */
    public static void dumpTableAssoc(Table table, int currentDepth) {
        System.out.println(pad(currentDepth) + "Table " + table.getClass().getSimpleName() + " :: " + table.getColumnCount() + "," + table.getRowCount());
        if (table.getSubTables() != null) for (Table tbl : table.getSubTables()) dumpTableAssoc(tbl, currentDepth + 1);
    }

    /**
    * <p>Returns string containing three spaces for every positive whole number
    * specified within padding.</p>
    * 
    * @param padding - Number of three spaces to insert into the string
    * @return The constructed string
    */
    public static String pad(int padding) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < padding; i++) sb.append("   ");
        return sb.toString();
    }

    /**
    * <p>Determines if referenceTable has the specified sub-table as part of its
    * table graph.</p>
    * 
    * @param referenceTable - The table to be scanned
    * @param containsTable - The table to be searches for
    * @return <b>true</b> The containsTable is contained within
    * referenceTable<br/><b>false</b> The containsTable isn't contained within
    * referenceTable
    */
    public static boolean containsTable(Table referenceTable, Table containsTable) {
        if (referenceTable == containsTable) return true;
        List<Table> tables = referenceTable.getSubTables();
        if (tables != null) for (Table table : tables) if (containsTable(table, containsTable)) return true;
        return false;
    }

    /**
    * <p>Fetches the lock manager used by the table specified. If there isn't a
    * lock manager for the table specified, a 'fake' lock manager will be
    * returned.</p>
    * 
    * @param refTable - The table who's lock manager is being returned
    * @return The TableLockManager of the table graph, or a NoTableLockManager
    * if the tables doesnt' have one
    */
    public static TableLockManager getTableLockManager(Table refTable) {
        TableSyncronizable ts = refTable.getBinding(TableSyncronizable.class);
        if (ts != null) return ts.getTableLockManager(); else return NO_TABLE_LOCK_MANAGER;
    }

    /**
    * <p>This method assumes that the two tables have the exact same schema. If
    * it doesn't, the method only copies over values that are compatible with
    * the destination table and who's rows already exist.</p>
    * 
    * <p>This method doesn't perform deep copies for objects that support
    * them.</p>
    * 
    * @param sourceRow - The row who's values will be copied out
    * @param destinationRow - The row who's values will be copied into
    */
    public static void copyRowSafe(TableRow sourceRow, TableRow destinationRow) {
        if (sourceRow == null || destinationRow == null) return;
        for (String sourceColumn : sourceRow.getColumnNames()) {
            Object value = sourceRow.getValue(sourceColumn);
            if (destinationRow.getColumnIndex(sourceColumn) >= 0 && (value == null || destinationRow.getColumnSchema(sourceColumn).getType().isAssignableFrom(value.getClass()))) destinationRow.setValue(sourceColumn, value);
        }
    }

    /**
    * <p>This fast and dangerous method assumes that the two tables have the
    * exact same schema. This is the fastest way to copy between two unspecified
    * table rows who's inter implementation is undefined.</p>
    * 
    * <p>This method doesn't perform deep copies for objects that support
    * them.</p>
    * 
    * @param sourceRow - The row who's values will be copied out
    * @param destinationRow - The row who's values will be copied into
    */
    public static void copyRowUnchecked(TableRow sourceRow, TableRow destinationRow) {
        for (int i = 0; i < sourceRow.getColumnCount(); i++) destinationRow.setValue(i, sourceRow.getValue(i));
    }

    public static int findRowIndexByEqualsRow(Table table, TableRow tableRow) {
        if (table == null || tableRow == null) return -1;
        for (int i = 0; i < table.getRowCount(); i++) if (table.getRow(i).equals(tableRow)) return i;
        return -1;
    }

    public static int findRowIndexByExactRow(Table table, TableRow tableRow) {
        if (table == null || tableRow == null) return -1;
        for (int i = 0; i < table.getRowCount(); i++) if (table.getRow(i) == tableRow) return i;
        return -1;
    }
}

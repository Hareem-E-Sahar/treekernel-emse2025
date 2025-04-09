package org.apache.myfaces.trinidad.model;

import java.beans.IntrospectionException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.faces.model.ArrayDataModel;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.ScalarDataModel;
import org.apache.myfaces.trinidad.logging.TrinidadLogger;

/**
 * Provides convenience methods for converting objects into models, and
 * working with models.
 */
public final class ModelUtils {

    /**
   * Gets an iteration of all the rowKeys in a collection.
   * The collection must not be modified while this iterator is being used.
   * The Iterator is not modifiable.
   */
    public static Iterator<Object> getRowKeyIterator(final CollectionModel model) {
        Iterator<Object> iter = new Iterator<Object>() {

            public boolean hasNext() {
                return _next != null;
            }

            public Object next() {
                if (_next == null) throw new NoSuchElementException();
                Object value = _next;
                _next = _next();
                return value;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            private Object _next() {
                int oldIndex = model.getRowIndex();
                try {
                    model.setRowIndex(_rowIndex++);
                    if (model.isRowAvailable()) return model.getRowKey();
                } finally {
                    model.setRowIndex(oldIndex);
                }
                return null;
            }

            private Object _next = Boolean.TRUE;

            private int _rowIndex = 0;
        };
        iter.next();
        return iter;
    }

    /**
   * finds the last index in the given RowKeyIndex that has data and returns the
   * next index. This is useful when the {@link RowKeyIndex#getRowCount} of the
   * RowKeyIndex is not known.
   * @return a positive number if there is data. Returns zero if there is no data.
   * Note that -1 is never returned.
   */
    public static int getRowCount(RowKeyIndex model) {
        int rowCount = model.getRowCount();
        if (rowCount >= 0) {
            return rowCount;
        }
        int lowerBound = 0;
        int upperBound = 100;
        for (model.setRowIndex(upperBound); model.isRowAvailable(); ) {
            lowerBound = upperBound;
            upperBound <<= 1;
            model.setRowIndex(upperBound);
        }
        return findLastIndex(model, lowerBound, upperBound);
    }

    /**
   * finds the last index in the given RowKeyIndex that has data and returns the
   * next index. This is useful when the {@link RowKeyIndex#getRowCount} of the
   * RowKeyIndex is not known.
   * @param startIndex starts the search from this index. Use zero to start from
   * the beginning.
   * @param endIndex the search will stop just before this index.
   * @return a number >= startIndex. Note that -1 is never returned.
   */
    public static int findLastIndex(RowKeyIndex table, int startIndex, int endIndex) {
        int rowCount = table.getRowCount();
        if (rowCount >= 0) {
            if (rowCount < endIndex) endIndex = rowCount;
        }
        if (table.isRowAvailable(endIndex - 1)) return endIndex;
        final int old = table.getRowIndex();
        try {
            while (startIndex < endIndex) {
                int middle = (startIndex + endIndex) / 2;
                assert (middle != endIndex) : "something is grossly wrong with integer division";
                table.setRowIndex(middle);
                if (table.isRowAvailable()) startIndex = middle + 1; else endIndex = middle;
            }
            return endIndex;
        } finally {
            table.setRowIndex(old);
        }
    }

    /**
   * Converts an instance into a TreeModel
   */
    public static TreeModel toTreeModel(Object value) {
        if (value instanceof TreeModel) return (TreeModel) value;
        return new ChildPropertyTreeModel(value, null);
    }

    /**
   * Converts an instance into a MenuModel
   */
    public static MenuModel toMenuModel(Object value) {
        if (value instanceof MenuModel) return (MenuModel) value; else {
            try {
                return new ViewIdPropertyMenuModel(value, null);
            } catch (IntrospectionException e) {
                IllegalArgumentException re = new IllegalArgumentException(_LOG.getMessage("CANNOT_CONVERT_INTO_MENUMODEL", value));
                re.initCause(e);
                throw re;
            }
        }
    }

    /**
   * Converts an instance into a CollectionModel.
   * @param value This can be a DataModel, List, Array or other CollectionModel.
   */
    public static CollectionModel toCollectionModel(Object value) {
        if (value instanceof CollectionModel) return (CollectionModel) value; else {
            return new SortableModel(value);
        }
    }

    /**
   * Converts an instance into a DataModel.
   * @param value Supported instances include java.util.List and
   * arrays.
   */
    public static DataModel toDataModel(Object value) {
        if (value instanceof DataModel) {
            return (DataModel) value;
        }
        if (value instanceof Object[]) {
            return new ArrayDataModel((Object[]) value);
        }
        if (value == null) {
            return new ListDataModel(Collections.emptyList());
        } else if (value instanceof List) return new ListDataModel((List) value);
        return new ScalarDataModel(value);
    }

    private ModelUtils() {
    }

    private static final TrinidadLogger _LOG = TrinidadLogger.createTrinidadLogger(ModelUtils.class);
}

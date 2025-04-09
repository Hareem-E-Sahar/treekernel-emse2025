package fr.macymed.commons.lang;

import java.lang.reflect.Array;

/** 
 * <p>
 * Common Array manipulation routines.
 * </p>
 * @author <a href="mailto:alexandre.cartapanis@macymed.fr">Cartapanis Alexandre</a>
 * @version 1.3.1
 * @since Commons - Lang API 1.0
 */
public class ArrayUtilities {

    /**
     * <p>
     * Forbids direct instanciation.
     * </p>
     */
    private ArrayUtilities() {
        super();
    }

    /**
     * <p>
     * Reverses an array.
     * </p>
     * @param _array The array to reverse.
     */
    public static final void reverse(Object[] _array) {
        if (_array != null) {
            int i = 0;
            int j = _array.length - 1;
            Object tmp;
            while (j > i) {
                tmp = _array[j];
                _array[j] = _array[i];
                _array[i] = tmp;
                j--;
                i++;
            }
        }
    }

    /**
     * <p>
     * Reverses an array.
     * </p>
     * @param _array The array to reverse.
     */
    public static final void reverse(byte[] _array) {
        if (_array != null) {
            int i = 0;
            int j = _array.length - 1;
            byte tmp;
            while (j > i) {
                tmp = _array[j];
                _array[j] = _array[i];
                _array[i] = tmp;
                j--;
                i++;
            }
        }
    }

    /**
     * <p>
     * Adds an object to the specified array. If the array is null, then a new array taht only contains the item is returned.
     * </p>
     * @param _array The array.
     * @param _item The item.
     * @return <code>Object[]</code> - A new array, that now contain the specified item.
     */
    public static final Object[] addToArray(Object[] _array, Object _item) {
        if (_array == null) {
            Class cla = _item.getClass();
            Object[] array = (Object[]) Array.newInstance(cla, 1);
            array[0] = _item;
            return array;
        }
        Class cla = _array.getClass().getComponentType();
        Object[] array = (Object[]) Array.newInstance(cla, Array.getLength(_array) + 1);
        System.arraycopy(_array, 0, array, 0, _array.length);
        array[_array.length] = _item;
        return array;
    }

    /**
     * <p>
     * Removes an item from a specified array.
     * </p>
     * @param _array The array.
     * @param _item The item.
     * @return <code>Object[]</code> - A new array that contains exactly the same items that the specified array, except the specified item.
     */
    public static final Object[] removeFromArray(Object[] _array, Object _item) {
        if (_item == null || _array == null) {
            return _array;
        }
        for (int i = _array.length; i-- > 0; ) {
            if (_item.equals(_array[i])) {
                Class cla = _array == null ? _item.getClass() : _array.getClass().getComponentType();
                Object[] array = (Object[]) Array.newInstance(cla, Array.getLength(_array) - 1);
                if (i > 0) {
                    System.arraycopy(_array, 0, array, 0, i);
                }
                if (i + 1 < _array.length) {
                    System.arraycopy(_array, i + 1, array, i, _array.length - (i + 1));
                }
                return array;
            }
        }
        return _array;
    }

    /**
     * <p>
     * Returns true if the specified array contains the specified item.
     * </p>
     * <p>
     * This simply compare each array's item with the specified item (using the {@link Object#equals(java.lang.Object)} method.
     * </p>
     * @param _array The array.
     * @param _item The item.
     * @return <code>boolean</code> - True if the specified array contains the specified item, false otherwise.
     */
    public static final boolean contains(Object[] _array, Object _item) {
        for (int i = 0; i < _array.length; i++) {
            if (_array[i].equals(_item)) {
                return true;
            }
        }
        return false;
    }
}

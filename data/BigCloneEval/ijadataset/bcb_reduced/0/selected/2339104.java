package taskblocks.utils;

import java.lang.reflect.Array;
import taskblocks.modelimpl.TaskImpl;

/**
 * Set of utilities used to manipulate arrays.
 * 
 * @author j.neubauer
 * @since 11.1.2007
 */
public class ArrayUtils {

    /**
	 * Creates new array of the same type as given array and adds specified member
	 * to the end of new array.
	 *  
	 * @param array
	 * @param member
	 * @return new array with member appened
	 */
    public static Object[] addToArray(Object[] array, Object member) {
        Object[] newArray = (Object[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = member;
        return newArray;
    }

    public static <T> boolean arrayContains(T[] array, T member) {
        if (array == null) {
            return false;
        }
        for (T m : array) {
            if (m == member) {
                return true;
            }
        }
        return false;
    }

    /**
	 * Finds member in array and if finds it, creates the same array without this member.
	 * 
	 * @param array
	 * @param member
	 * @return new array without member or the old array if member wasn't found
	 */
    public static <T> T[] removeFromArray(T[] array, T member) {
        int i;
        for (i = 0; i < array.length; i++) {
            if (array[i] == member) {
                break;
            }
        }
        if (i < array.length) {
            T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - 1);
            System.arraycopy(array, 0, newArray, 0, i);
            System.arraycopy(array, i + 1, newArray, i, array.length - i - 1);
            return newArray;
        }
        return array;
    }

    public static Object[] removeFromArray(Object[] array, int i) {
        if (i < 0 || i >= array.length) {
            throw new IndexOutOfBoundsException();
        }
        Object[] newArray = (Object[]) Array.newInstance(array.getClass().getComponentType(), array.length - 1);
        System.arraycopy(array, 0, newArray, 0, i);
        System.arraycopy(array, i + 1, newArray, i, array.length - i - 1);
        return newArray;
    }

    public static boolean arrayEqualsExceptNull(Object[] a1, Object[] a2) {
        if ((a1 == null) && (a2 == null)) {
            return true;
        }
        if (a1 == null && a2 != null && a2.length == 0) {
            return true;
        }
        if (a2 == null && a1 != null && a1.length == 0) {
            return true;
        }
        if (((a1 == null) && (a2 != null)) || ((a2 == null) && (a1 != null))) {
            return false;
        }
        if (a1.length != a2.length) {
            return false;
        }
        for (int i = 0; i < a1.length; i++) {
            if ((a1[i] == null) && (a2[i] == null)) {
                continue;
            }
            if ((a1 != null) && a1[i].equals(a2[i])) {
                continue;
            }
            return false;
        }
        return true;
    }

    public static boolean arrayEquals(Object[] a1, Object[] a2) {
        if ((a1 == null) && (a2 == null)) {
            return true;
        }
        if (((a1 == null) && (a2 != null)) || ((a2 == null) && (a1 != null))) {
            return false;
        }
        if (a1.length != a2.length) {
            return false;
        }
        for (int i = 0; i < a1.length; i++) {
            if ((a1[i] == null) && (a2[i] == null)) {
                continue;
            }
            if ((a1 != null) && a1[i].equals(a2[i])) {
                continue;
            }
            return false;
        }
        return true;
    }
}

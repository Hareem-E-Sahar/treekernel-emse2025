package javaEdu.util.array;

import java.lang.reflect.Array;

/**
 * Create the copy of array by <code>Array.newInstance()</code>. The element of
 * array has excepted type. If basic type, the memory is assigned; if object
 * type, the element is <code>null</code>.
 * 
 * @author E2018329
 * 
 */
public class ArrayGrowAndClone {

    /**
	 * Valid only for single direction array, otherwise return <code>null</code>
	 * 
	 * @param array
	 * @return <code>null</code> when array is not array or multi-direction
	 *         array.
	 */
    public static Object arrayGrow(Object array) {
        Class cl = array.getClass();
        if (!cl.isArray()) {
            return null;
        }
        Class componentType = cl.getComponentType();
        if (componentType.isArray()) {
            return null;
        }
        int length = Array.getLength(array);
        int newLength = length * 11 / 10 + 10;
        Object newArray = Array.newInstance(componentType, newLength);
        System.arraycopy(array, 0, newArray, 0, length);
        return newArray;
    }

    /**
	 * The copy of the array has different memory space from the original array.
	 * Valid only for single direction array, otherwise return <code>null</code>
	 * 
	 * @param array
	 * @return <code>null</code> when array is not array or multi-direction
	 *         array.
	 */
    public static Object arrayCopy(Object array) {
        Class cl = array.getClass();
        if (!cl.isArray()) {
            return null;
        }
        Class componentType = cl.getComponentType();
        if (componentType.isArray()) {
            return null;
        }
        int length = Array.getLength(array);
        Object newArray = Array.newInstance(componentType, length);
        System.arraycopy(array, 0, newArray, 0, length);
        return newArray;
    }

    /**
	 * For two-direction array only.
	 * 
	 * @param array
	 * @return
	 */
    public static Object planarArrayRowGrow(Object array) {
        Class arrayClass = array.getClass();
        if (!arrayClass.isArray()) {
            return null;
        }
        Class rowClass = arrayClass.getComponentType();
        if (!rowClass.isArray()) {
            return null;
        }
        Class componentType = rowClass.getComponentType();
        if (componentType.isArray()) {
            return null;
        }
        int rowNum = Array.getLength(array);
        int newRowNum = rowNum * 11 / 10 + 10;
        int columnNum = 0;
        try {
            Object row = Array.get(array, 0);
            if (null != row) {
                columnNum = Array.getLength(row);
            }
        } catch (Exception e) {
        }
        int[] dimensions = { newRowNum, columnNum };
        Object newArray = Array.newInstance(componentType, dimensions);
        Object oldRow, newRow;
        for (int i = 0; i < rowNum; i++) {
            oldRow = Array.get(array, i);
            newRow = Array.get(newArray, i);
            System.arraycopy(oldRow, 0, newRow, 0, Array.getLength(oldRow));
        }
        return newArray;
    }

    /**
	 * For two-direction array only.
	 * 
	 * @param array
	 * @return
	 */
    public static Object planarArrayColumnGrow(Object array) {
        Class arrayClass = array.getClass();
        if (!arrayClass.isArray()) {
            return null;
        }
        Class rowClass = arrayClass.getComponentType();
        if (!rowClass.isArray()) {
            return null;
        }
        Class componentType = rowClass.getComponentType();
        if (componentType.isArray()) {
            return null;
        }
        int rowNum = Array.getLength(array);
        int columnNum = 0;
        try {
            Object row = Array.get(array, 0);
            if (null != row) {
                columnNum = Array.getLength(row);
            }
        } catch (Exception e) {
        }
        int newColumnNum = columnNum * 11 / 10 + 10;
        int[] dimensions = { rowNum, newColumnNum };
        Object newArray = Array.newInstance(componentType, dimensions);
        Object oldRow, newRow;
        for (int i = 0; i < rowNum; i++) {
            oldRow = Array.get(array, i);
            newRow = Array.get(newArray, i);
            System.arraycopy(oldRow, 0, newRow, 0, Array.getLength(oldRow));
        }
        return newArray;
    }

    /**
	 * For two-direction array only.
	 * 
	 * @param array
	 * @return
	 */
    public static Object planarArrayCopy(Object array) {
        Class arrayClass = array.getClass();
        if (!arrayClass.isArray()) {
            return null;
        }
        Class rowClass = arrayClass.getComponentType();
        if (!rowClass.isArray()) {
            return null;
        }
        Class componentType = rowClass.getComponentType();
        if (componentType.isArray()) {
            return null;
        }
        int rowNum = Array.getLength(array);
        int columnNum = 0;
        try {
            Object row = Array.get(array, 0);
            if (null != row) {
                columnNum = Array.getLength(row);
            }
        } catch (Exception e) {
        }
        int[] dimensions = { rowNum, columnNum };
        Object newArray = Array.newInstance(componentType, dimensions);
        Object oldRow, newRow;
        for (int i = 0; i < rowNum; i++) {
            oldRow = Array.get(array, i);
            newRow = Array.get(newArray, i);
            System.arraycopy(oldRow, 0, newRow, 0, Array.getLength(oldRow));
        }
        return newArray;
    }

    public static void main(String[] args) {
        System.out.println("Test 1: ");
        String[] one = { "1ddd", "3wweef", "dd5" };
        p1(one);
        String[] newOne = (String[]) arrayCopy(one);
        p1(newOne);
        System.out.println("change old and new separately");
        one[0].replace('d', 'o');
        one[1] = "sssss9";
        one[2] = "rrrrrr9";
        p1(one);
        p1(newOne);
        System.out.println("Test 2: ");
        Integer[][] two = { { 3, 4, 5, 6 }, { 2, 5, 6 } };
        p2(two);
        Integer[][] newTwo = (Integer[][]) planarArrayCopy(two);
        p2(newTwo);
        System.out.println("change old and new separately");
        two[0][0] = 9;
        two[0][1] = 9;
        two[0][2] = 9;
        two[1][0] = 7;
        two[1][1] = 7;
        two[1][2] = 7;
        two[0][3] = 9;
        p2(two);
        newTwo[1][3] = 78;
        p2(newTwo);
        System.out.println("Test 3: ");
        int[][] three = { { 3, 4, 5, 6 }, { 2, 5, 6 } };
        pb2(three);
        int[][] newThree = (int[][]) planarArrayCopy(three);
        pb2(newThree);
        System.out.println("change old and new separately");
        three[0][0] = 9;
        three[0][1] = 9;
        three[0][2] = 9;
        three[0][3] = 9;
        three[1][0] = 7;
        three[1][1] = 7;
        three[1][2] = 7;
        pb2(three);
        newThree[1][3] = 78;
        pb2(newThree);
    }

    private static void p1(String[] one) {
        System.out.println("*****************");
        for (int i = 0; i < one.length; i++) {
            System.out.print(one[i] + " ");
        }
        System.out.println();
        System.out.println("*****************");
    }

    private static void p2(Object[][] show) {
        System.out.println("*****************");
        for (int i = 0; i < show.length; i++) {
            for (int j = 0; j < show[i].length; j++) {
                System.out.print(show[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("*****************");
    }

    private static void pb2(int[][] show) {
        System.out.println("*****************");
        for (int i = 0; i < show.length; i++) {
            for (int j = 0; j < show[i].length; j++) {
                System.out.print(show[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("*****************");
    }
}

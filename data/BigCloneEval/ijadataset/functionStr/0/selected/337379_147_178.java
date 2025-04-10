public class Test {    public static Object planarArrayCopy(Object array) {
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
}
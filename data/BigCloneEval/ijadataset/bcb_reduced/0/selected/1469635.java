package se.netroid.util;

public class IndexedList<E> {

    private int[] indexList;

    private Object[] objectList;

    private int size;

    private static final int GROW_SIZE = 20;

    public IndexedList() {
        size = 0;
        indexList = new int[20];
        objectList = new Object[20];
    }

    public void add(E object, int index) {
        int listPos = getIndexListPosition(index);
        if (indexList[listPos] == index) {
            objectList[listPos] = object;
            return;
        }
        ensureCapacity(getSize() + 1);
        if (listPos < getSize() && indexList[listPos] < index) {
            listPos++;
        }
        System.arraycopy(indexList, listPos, indexList, listPos + 1, getSize() - listPos);
        System.arraycopy(objectList, listPos, objectList, listPos + 1, getSize() - listPos);
        indexList[listPos] = index;
        objectList[listPos] = object;
        size++;
    }

    @SuppressWarnings("unchecked")
    public E get(int index) {
        int listPos = getIndexListPosition(index);
        if (indexList[listPos] == index) {
            return (E) objectList[listPos];
        }
        return null;
    }

    public int getSize() {
        return size;
    }

    public void removeIndex(int index) {
        int listPos = getIndexListPosition(index);
        if (indexList[listPos] != index) {
            return;
        }
        System.arraycopy(indexList, listPos + 1, indexList, listPos, getSize() - listPos - 1);
        System.arraycopy(objectList, listPos + 1, objectList, listPos, getSize() - listPos - 1);
        objectList[getSize() - 1] = null;
        size--;
    }

    public void removeObject(E object) {
        for (int i = 0; i < getSize(); i++) {
            if (objectList[i].equals(object)) {
                System.arraycopy(indexList, i + 1, indexList, i, getSize() - i - 1);
                System.arraycopy(objectList, i + 1, objectList, i, getSize() - i - 1);
                objectList[getSize() - 1] = null;
                size--;
            }
        }
    }

    public void clear() {
        java.util.Arrays.fill(objectList, null);
        size = 0;
    }

    private int getIndexListPosition(int index) {
        int bottom = 0;
        int top = getSize() - 1;
        int center = (top + bottom) / 2;
        while (indexList[center] != index && bottom < top) {
            if (indexList[center] > index) {
                top = center - 1;
            } else {
                bottom = center + 1;
            }
            center = (top + bottom) / 2;
        }
        return center;
    }

    public int[] getIndexOf(E obj) {
        int[] result = new int[getSize()];
        int index = 0;
        for (int i = 0; i < getSize(); i++) {
            if (objectList[i].equals(obj)) {
                result[index++] = indexList[i];
            }
        }
        int[] resArr = new int[index];
        System.arraycopy(result, 0, resArr, 0, index);
        return resArr;
    }

    private void ensureCapacity(int size) {
        if (indexList.length >= size) {
            return;
        }
        int[] newIndexList = new int[size + GROW_SIZE];
        Object[] newObjectList = new Object[size + GROW_SIZE];
        System.arraycopy(indexList, 0, newIndexList, 0, getSize());
        System.arraycopy(objectList, 0, newObjectList, 0, getSize());
        indexList = newIndexList;
        objectList = newObjectList;
    }
}

public class Test {    public <T> T[] toArray(T[] a) {
        if (a.length < elementCount) a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), elementCount);
        System.arraycopy(elementData, 0, a, 0, elementCount);
        if (a.length > elementCount) a[elementCount] = null;
        return a;
    }
}
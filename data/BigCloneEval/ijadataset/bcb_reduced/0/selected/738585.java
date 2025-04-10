package dr.util;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Permutator implements Iterator {

    private final int size;

    private final Object[] elements;

    private final Object ar;

    private final int[] permutation;

    private boolean next = true;

    public Permutator(Object[] e) {
        size = e.length;
        elements = new Object[size];
        System.arraycopy(e, 0, elements, 0, size);
        ar = Array.newInstance(e.getClass().getComponentType(), size);
        System.arraycopy(e, 0, ar, 0, size);
        permutation = new int[size + 1];
        for (int i = 0; i < size + 1; i++) {
            permutation[i] = i;
        }
    }

    private void formNextPermutation() {
        for (int i = 0; i < size; i++) {
            Array.set(ar, i, elements[permutation[i + 1] - 1]);
        }
    }

    public boolean hasNext() {
        return next;
    }

    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    private void swap(final int i, final int j) {
        final int x = permutation[i];
        permutation[i] = permutation[j];
        permutation[j] = x;
    }

    public Object next() throws NoSuchElementException {
        formNextPermutation();
        int i = size - 1;
        while (permutation[i] > permutation[i + 1]) i--;
        if (i == 0) {
            next = false;
            for (int j = 0; j < size + 1; j++) {
                permutation[j] = j;
            }
            return ar;
        }
        int j = size;
        while (permutation[i] > permutation[j]) j--;
        swap(i, j);
        int r = size;
        int s = i + 1;
        while (r > s) {
            swap(r, s);
            r--;
            s++;
        }
        return ar;
    }

    public String toString() {
        final int n = Array.getLength(ar);
        final StringBuffer sb = new StringBuffer("[");
        for (int j = 0; j < n; j++) {
            sb.append(Array.get(ar, j).toString());
            if (j < n - 1) sb.append(",");
        }
        sb.append("]");
        return new String(sb);
    }

    public static void main(String[] args) {
        for (Iterator i = new Permutator(args); i.hasNext(); ) {
            i.next();
            System.out.println(i);
        }
    }
}

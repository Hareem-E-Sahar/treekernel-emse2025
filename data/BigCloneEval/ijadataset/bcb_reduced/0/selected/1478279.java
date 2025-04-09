package org.kunyit.tst;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

public class Tst implements Serializable {

    static final int DUMMY = 0;

    static final int ROOT = 1;

    static final long serialVersionUID = 1L;

    char[] key_tab;

    int[] left_tab;

    int[] right_tab;

    int[] child_tab;

    boolean[] has_val_tab;

    int[] val_tab;

    int n;

    static final int DEFAULT_SIZE = 1024;

    public Tst() {
        this(Tst.DEFAULT_SIZE);
    }

    public Tst(int size) {
        if (size < 1) {
            size = 1;
        }
        key_tab = new char[size];
        left_tab = new int[size];
        right_tab = new int[size];
        child_tab = new int[size];
        has_val_tab = new boolean[size];
        val_tab = new int[size];
        child_tab[Tst.DUMMY] = Tst.ROOT;
        n = 1;
    }

    char[] copyOf(char[] original, int new_len) {
        char[] new_array = new char[new_len];
        for (int i = 0; i < original.length; i++) {
            new_array[i] = original[i];
        }
        return new_array;
    }

    boolean[] copyOf(boolean[] original, int new_len) {
        boolean[] new_array = new boolean[new_len];
        for (int i = 0; i < original.length; i++) {
            new_array[i] = original[i];
        }
        return new_array;
    }

    int[] copyOf(int[] original, int new_len) {
        int[] new_array = new int[new_len];
        for (int i = 0; i < original.length; i++) {
            new_array[i] = original[i];
        }
        return new_array;
    }

    void resize(int new_size) {
        key_tab = copyOf(key_tab, new_size);
        right_tab = copyOf(right_tab, new_size);
        left_tab = copyOf(left_tab, new_size);
        child_tab = copyOf(child_tab, new_size);
        has_val_tab = copyOf(has_val_tab, new_size);
        val_tab = copyOf(val_tab, new_size);
    }

    void doubleSize() {
        resize(key_tab.length * 2);
    }

    int new_node() {
        if (n >= key_tab.length) {
            doubleSize();
        }
        int old_n = n;
        n++;
        return old_n;
    }

    public void insert(String key, int val) {
        insert(key.toCharArray(), val);
    }

    boolean isRootExist() {
        return (n > 1);
    }

    public void insert(char[] key, int val) {
        if (!isRootExist()) {
            new_node();
        }
        int i = Tst.ROOT;
        for (int j = 0; j < key.length; j++) {
            char k = key[j];
            while (key_tab[i] != k && key_tab[i] != '\0') {
                if (k < key_tab[i]) {
                    if (left_tab[i] == Tst.DUMMY) {
                        left_tab[i] = new_node();
                    }
                    i = left_tab[i];
                } else if (k > key_tab[i]) {
                    if (right_tab[i] == Tst.DUMMY) {
                        right_tab[i] = new_node();
                    }
                    i = right_tab[i];
                }
            }
            if (key_tab[i] == '\0') {
                key_tab[i] = k;
            }
            if (j + 1 < key.length) {
                if (child_tab[i] == Tst.DUMMY) {
                    child_tab[i] = new_node();
                }
                i = child_tab[i];
            }
        }
        val_tab[i] = val;
        has_val_tab[i] = true;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    public int gotoChild(int i) throws TstOutOfBoundException {
        checkRange(i);
        return child_tab[i];
    }

    void checkRange(int i) throws TstOutOfBoundException {
        if (i < 0 || i >= n) {
            throw new TstOutOfBoundException();
        }
    }

    public int findSibling(int i, char key) throws TstOutOfBoundException {
        checkRange(i);
        while (key != key_tab[i]) {
            if (key < key_tab[i]) {
                if (left_tab[i] == Tst.DUMMY) {
                    return Tst.DUMMY;
                }
                i = left_tab[i];
            } else if (key > key_tab[i]) {
                if (right_tab[i] == Tst.DUMMY) {
                    return Tst.DUMMY;
                }
                i = right_tab[i];
            }
        }
        return i;
    }

    public boolean hasValueAt(int i) throws TstOutOfBoundException {
        checkRange(i);
        return has_val_tab[i];
    }

    public int getValue(String key) throws TstNoKeyException {
        try {
            TstIterator i = iterator();
            for (char ch : key.toCharArray()) {
                if (!i.traverse(ch)) {
                    throw new TstNoKeyException();
                }
            }
            return i.getValue();
        } catch (TstNoValueException e) {
            throw new TstNoKeyException();
        } catch (TstOutOfBoundException e) {
            throw new TstNoKeyException();
        }
    }

    public int getValueAt(int i) throws TstOutOfBoundException, TstNoValueException {
        checkRange(i);
        if (!hasValueAt(i)) {
            throw new TstNoValueException();
        }
        return val_tab[i];
    }

    public TstIterator iterator() {
        return new TstIterator(this);
    }

    public void insertSortedData(TstItem[] items) {
        insertSortedData(items, 0, items.length);
    }

    void insertSortedData(TstItem[] items, int l, int r) {
        if (l < r) {
            int m = (l + r) / 2;
            insert(items[m].key, items[m].value);
            insertSortedData(items, l, m);
            insertSortedData(items, m + 1, r);
        }
    }
}

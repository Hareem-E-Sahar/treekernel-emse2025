package org.fao.waicent.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import org.w3c.dom.Element;

public class IntegerVector implements Serializable, Cloneable {

    public static void main(String arg[]) {
        IntegerVector asse = new IntegerVector();
        for (int i = 0; i < 10; i++) {
            asse.add(i);
        }
        for (int i = 0; i < asse.size(); i++) {
            System.out.print(" " + asse.at(i));
        }
        asse.removeAt(1);
        System.out.println("\nremoveAt(1)");
        for (int i = 0; i < asse.size(); i++) {
            System.out.print(" " + asse.at(i));
        }
        while (asse.size() > 0) {
            int j = asse.size() / 2;
            asse.removeAt(j);
            System.out.println("\nremoveAt(" + j + ")");
            for (int i = 0; i < asse.size(); i++) {
                System.out.print(" " + asse.at(i));
            }
        }
    }

    public int[] toArray() {
        int[] ret = new int[size()];
        if (size() > 0) {
            System.arraycopy(data, 0, ret, 0, ret.length);
        }
        return ret;
    }

    transient int size = 0;

    transient int capacity = 0;

    transient int increment = 10;

    transient int data[] = null;

    transient VectorResizeListenerInterface resize_listener = null;

    public void setResizeListener(VectorResizeListenerInterface resize_listener) {
        this.resize_listener = resize_listener;
    }

    public VectorResizeListenerInterface getResizeListener() {
        return this.resize_listener;
    }

    public final int capacity() {
        return capacity;
    }

    public IntegerVector() {
    }

    public IntegerVector(int new_capacity, int increment) {
        this.increment = increment;
        ensureCapacity(new_capacity);
    }

    public IntegerVector(int new_capacity) {
        ensureCapacity(new_capacity);
    }

    public IntegerVector(IntegerVector x) {
        capacity = x.capacity;
        data = new int[capacity];
        size = x.size;
        System.arraycopy(x.data, 0, data, 0, capacity);
    }

    public synchronized Object clone() throws CloneNotSupportedException {
        IntegerVector x = (IntegerVector) super.clone();
        x.capacity = capacity;
        x.data = new int[capacity];
        x.size = size;
        System.arraycopy(data, 0, x.data, 0, capacity);
        return x;
    }

    public boolean compareTo(IntegerVector anotherIntegerVector) {
        boolean equal = (size == anotherIntegerVector.size);
        if (equal) {
            for (int i = 0; i < size; i++) {
                if (data[i] != anotherIntegerVector.data[i]) {
                    equal = false;
                    break;
                }
            }
        }
        return equal;
    }

    private void checkCapacity() {
        if (size > capacity) {
            ensureCapacity(capacity + increment);
        }
    }

    public void ensureCapacity(int new_capacity) {
        if (data == null) {
            data = new int[new_capacity];
            capacity = new_capacity;
        } else {
            if (new_capacity > capacity) {
                if (resize_listener != null) {
                    size--;
                    resize_listener.resizing();
                    size++;
                }
                int new_data[] = new int[new_capacity];
                System.arraycopy(data, 0, new_data, 0, capacity);
                data = new_data;
                capacity = new_capacity;
            }
        }
    }

    public void ensureSize(int size) {
        ensureCapacity(size);
        while (size() < size) {
            add(0);
        }
    }

    /**     *  @return size member variable     */
    public int size() {
        return size;
    }

    /**     *  @param index   Index of integer to return from this vector.     *  @return  Integer at the index in this vector.     */
    public int at(int index) {
        if (index >= size || index < 0) {
            throw new ArrayIndexOutOfBoundsException(getClass().getName() + " " + index + ">=" + size);
        }
        return data[index];
    }

    public void set(int value, int index) {
        if (index >= size || index < 0) {
            throw new ArrayIndexOutOfBoundsException(getClass().getName() + index + ">=" + size);
        }
        data[index] = value;
    }

    public void clear() {
        size = 0;
    }

    public void add(int value) {
        size++;
        checkCapacity();
        data[size() - 1] = value;
    }

    public boolean isInVector(int value) {
        for (int i = 0; i < size; i++) {
            if (at(i) == value) {
                return true;
            }
        }
        return false;
    }

    public void remove(int value) {
        int index = index(value);
        if (index != -1) {
            removeAt(index);
        }
    }

    public void removeAt(int index) {
        if (index >= size || index < 0) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            size--;
            if (index != size) {
                int copy_length = size - index;
                System.arraycopy(data, index + 1, data, index, copy_length);
            }
        }
    }

    public void insertAt(int value, int index) {
        if (index < size()) {
            size++;
            checkCapacity();
            System.arraycopy(data, index, data, index + 1, size() - index - 1);
            data[index] = value;
        } else {
            add(value);
        }
    }

    /**     *  alisaf: get the index of the element with the passed in value.     *     *  @param value The value whose index we are looking for.     */
    public int index(int value) {
        int index = -1;
        for (int i = 0; i < size(); i++) {
            if (value == at(i)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public int index(int value, int start) {
        int index = -1;
        for (int i = start; i < size(); i++) {
            if (value == at(i)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public boolean equals(IntegerVector x) {
        boolean is_equal = (size == x.size);
        if (is_equal) {
            for (int i = 0; i < size; i++) {
                if (x.data[i] != data[i]) {
                    is_equal = false;
                    break;
                }
            }
        }
        return is_equal;
    }

    public void dump() {
        System.out.print("[");
        for (int i = 0; i < size; i++) {
            System.out.print(" " + data[i]);
        }
        System.out.println(" ]");
    }

    private void writeObject(ObjectOutputStream obj_out) throws IOException {
        obj_out.defaultWriteObject();
        obj_out.writeInt(size);
        for (int i = 0; i < size; i++) {
            obj_out.writeInt(data[i]);
        }
    }

    private void readObject(ObjectInputStream obj_in) throws IOException {
        try {
            obj_in.defaultReadObject();
            size = obj_in.readInt();
            capacity = size;
            data = new int[size];
            for (int i = 0; i < size; i++) {
                data[i] = obj_in.readInt();
            }
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }
    }

    public void save(DataOutputStream out) throws IOException {
        out.writeInt(size);
        for (int i = 0; i < size; i++) {
            out.writeInt(data[i]);
        }
    }

    /**     *  alisaf: added constructor to support building Matrix from DOM.     */
    public IntegerVector(Element element) {
        int iSize = Integer.parseInt(element.getAttribute("size"));
        ensureCapacity(iSize);
        size = capacity;
        for (int i = 0; i < size; i++) {
            data[i] = Integer.parseInt(element.getAttribute("D_" + i));
        }
    }

    /**     *  alisaf: additional constructor to test if stream value equals xml values     */
    public IntegerVector(Element element, DataInputStream in) throws Exception {
        int iSize = Integer.parseInt(element.getAttribute("size"));
        int iJunk = in.readInt();
        System.out.println("         IntegerVector.size: stream=" + iSize + " xml=" + element.getAttribute("size"));
        ensureCapacity(iSize);
        size = capacity;
        for (int i = 0; i < size; i++) {
            data[i] = Integer.parseInt(element.getAttribute("D_" + i));
            iJunk = in.readInt();
            System.out.println("         IntegerVector.data[" + i + "]: stream=" + iJunk + " xml=" + data[i]);
        }
    }

    /**     *  alisaf: XML version: Read in the DataInputStream     *  and generate an XML DOM from it     */
    public IntegerVector(DataInputStream in, Element element) throws IOException {
        int iSize = in.readInt();
        element.setAttribute("size", Integer.toString(iSize));
        ensureCapacity(iSize);
        size = capacity;
        for (int i = 0; i < size; i++) {
            data[i] = in.readInt();
            element.setAttribute("D_" + i, Integer.toString(data[i]));
        }
    }

    public IntegerVector(DataInputStream in) throws IOException {
        this(in.readInt());
        size = capacity;
        for (int i = 0; i < size; i++) {
            data[i] = in.readInt();
        }
    }

    /**     *  alisaf: Constructor to build a key from an integer array.     */
    public IntegerVector(int[] iaKey) {
        this(iaKey.length);
        size = iaKey.length;
        for (int i = 0; i < size; i++) {
            data[i] = iaKey[i];
        }
    }

    public void dump(PrintStream out) {
        if (out == null) {
            out = System.out;
        }
        out.println("(");
        for (int i = 0; i < size(); i++) {
            out.print(at(i) + " ");
        }
        out.println(")");
    }

    private void QuickSort(int data[], int l, int r) throws Exception {
        int M = 4;
        int i;
        int j;
        int v;
        if ((r - l) > M) {
            i = (r + l) / 2;
            if (data[l] > data[i]) {
                swap(data, l, i);
            }
            if (data[l] > data[r]) {
                swap(data, l, r);
            }
            if (data[i] > data[r]) {
                swap(data, i, r);
            }
            j = r - 1;
            swap(data, i, j);
            i = l;
            v = data[j];
            for (; ; ) {
                while (data[++i] < v) {
                    ;
                }
                while (data[--j] > v) {
                    ;
                }
                if (j < i) {
                    break;
                }
                swap(data, i, j);
            }
            swap(data, i, r - 1);
            QuickSort(data, l, j);
            QuickSort(data, i + 1, r);
        }
    }

    private void swap(int data[], int i, int j) {
        int T;
        T = data[i];
        data[i] = data[j];
        data[j] = T;
    }

    private void InsertionSort(int data[], int lo0, int hi0) throws Exception {
        int i;
        int j;
        int v;
        for (i = lo0 + 1; i <= hi0; i++) {
            v = data[i];
            j = i;
            while ((j > lo0) && (data[j - 1] > v)) {
                data[j] = data[j - 1];
                j--;
            }
            data[j] = v;
        }
    }

    public void sort() {
        try {
            QuickSort(data, 0, size() - 1);
            InsertionSort(data, 0, size() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        String str = "";
        if (data != null && data.length > 0) {
            for (int i = 0; i < data.length; i++) {
                str += "[" + data[i] + "] ";
            }
        }
        return str;
    }

    public boolean contains(int value) {
        for (int i = 0; i < size(); i++) {
            if (data[i] == value) {
                return true;
            }
        }
        return false;
    }

    public void toXML(Element element) {
        element.setAttribute("size", String.valueOf(this.size()));
        for (int i = 0; i < size(); i++) {
            element.setAttribute("D_" + i, String.valueOf(this.at(i)));
        }
    }
}

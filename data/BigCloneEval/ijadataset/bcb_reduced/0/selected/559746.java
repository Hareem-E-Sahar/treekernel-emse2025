package org.fao.waicent.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DoubleVector implements Serializable {

    transient int size = 0;

    transient int capacity = 0;

    transient int increment = 10;

    transient double data[] = null;

    public DoubleVector() {
    }

    public DoubleVector(int new_capacity, int increment) {
        this.increment = increment;
        ensureCapacity(new_capacity);
    }

    public DoubleVector(int new_capacity) {
        ensureCapacity(new_capacity);
    }

    public DoubleVector(DoubleVector x) {
        capacity = x.capacity;
        data = new double[capacity];
        size = x.size;
        System.arraycopy(x.data, 0, data, 0, capacity);
    }

    public synchronized Object clone() throws CloneNotSupportedException {
        DoubleVector x = (DoubleVector) super.clone();
        x.capacity = capacity;
        x.data = new double[capacity];
        x.size = size;
        System.arraycopy(data, 0, x.data, 0, capacity);
        return x;
    }

    public boolean compareTo(DoubleVector anotherDoubleVector) {
        boolean equal = (size == anotherDoubleVector.size);
        if (equal) {
            for (int i = 0; i < size; i++) {
                if (data[i] != anotherDoubleVector.data[i]) {
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
            data = new double[new_capacity];
            capacity = new_capacity;
        } else {
            if (new_capacity > capacity) {
                double new_data[] = new double[new_capacity];
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

    public int size() {
        return size;
    }

    public double at(int index) {
        return data[index];
    }

    public void set(double value, int index) {
        data[index] = value;
    }

    public void clear() {
        size = 0;
    }

    public void add(double value) {
        size++;
        checkCapacity();
        data[size() - 1] = value;
    }

    public void insertAt(double value, int index) {
        if (index < size()) {
            size++;
            checkCapacity();
            System.arraycopy(data, index, data, index + 1, size() - index - 1);
            data[index] = value;
        } else {
            add(value);
        }
    }

    public int index(double value) {
        int index = -1;
        for (int i = 0; i < size(); i++) {
            if (value == at(i)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public boolean equals(DoubleVector x) {
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
            obj_out.writeDouble(data[i]);
        }
    }

    private void readObject(ObjectInputStream obj_in) throws IOException {
        try {
            obj_in.defaultReadObject();
            size = obj_in.readInt();
            capacity = size;
            data = new double[size];
            for (int i = 0; i < size; i++) {
                data[i] = obj_in.readDouble();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void save(DataOutputStream out) throws IOException {
        out.writeInt(size);
        for (int i = 0; i < size; i++) {
            out.writeDouble(data[i]);
        }
    }

    public DoubleVector(Element element) throws IOException {
        this(Integer.parseInt(element.getAttribute("size")));
        size = capacity;
        for (int i = 0; i < size; i++) {
            data[i] = Double.parseDouble(element.getAttribute("D_" + i));
        }
    }

    public DoubleVector(DataInputStream in, Document doc, Element parent_element, int iSize) throws IOException {
        this(iSize = in.readInt());
        size = capacity;
        Element element = doc.createElement("DoubleVector");
        element.setAttribute("size", Integer.toString(iSize));
        for (int i = 0; i < size; i++) {
            data[i] = in.readDouble();
            element.setAttribute("D_" + i, Double.toString(data[i]));
        }
        parent_element.appendChild(element);
    }

    public DoubleVector(DataInputStream in) throws IOException {
        this(in.readInt());
        size = capacity;
        for (int i = 0; i < size; i++) {
            data[i] = in.readDouble();
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

    private void QuickSort(double data[], int l, int r) throws Exception {
        int M = 4;
        int i;
        int j;
        double v;
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

    private void swap(double data[], int i, int j) {
        double T;
        T = data[i];
        data[i] = data[j];
        data[j] = T;
    }

    private void InsertionSort(double data[], int lo0, int hi0) throws Exception {
        int i;
        int j;
        double v;
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

    public void sort() throws Exception {
        QuickSort(data, 0, size() - 1);
        InsertionSort(data, 0, size() - 1);
    }

    public DoubleVector(ResultSet r) throws SQLException {
        while (r.next()) {
            add(r.getDouble(1));
        }
    }

    public static DoubleVector loadDoubleVector(ResultSet r) throws SQLException {
        return new DoubleVector(r);
    }

    public static DoubleVector loadDoubleVector(Connection con, String SQL) throws SQLException {
        Statement statement = null;
        ResultSet result_set = null;
        try {
            statement = con.createStatement();
            result_set = statement.executeQuery(SQL);
            return loadDoubleVector(result_set);
        } catch (SQLException e) {
            System.err.println(e + " SQL:=" + SQL);
            throw e;
        } finally {
            if (result_set != null) {
                try {
                    result_set.close();
                } catch (SQLException e) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
    }
}

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

public class FloatVector implements Serializable, Cloneable {

    transient int size = 0;

    transient int capacity = 0;

    transient int increment = 10;

    transient float data[] = null;

    Float _average = null;

    public FloatVector() {
    }

    public FloatVector(int new_capacity, int increment) {
        this.increment = increment;
        ensureCapacity(new_capacity);
    }

    public FloatVector(int new_capacity) {
        ensureCapacity(new_capacity);
    }

    public FloatVector(FloatVector x) {
        capacity = x.capacity;
        data = new float[capacity];
        size = x.size;
        System.arraycopy(x.data, 0, data, 0, capacity);
    }

    public FloatVector(float[] x) {
        capacity = x.length;
        data = x;
        size = x.length;
        System.arraycopy(x, 0, data, 0, capacity);
    }

    public synchronized Object clone() throws CloneNotSupportedException {
        FloatVector x = (FloatVector) super.clone();
        x.capacity = capacity;
        x.data = new float[capacity];
        x.size = size;
        if (data == null) {
            x.data = null;
        } else {
            System.arraycopy(data, 0, x.data, 0, capacity);
        }
        return x;
    }

    public boolean compareTo(FloatVector anotherFloatVector) {
        boolean equal = (size == anotherFloatVector.size);
        if (equal) {
            for (int i = 0; i < size; i++) {
                if (data[i] != anotherFloatVector.data[i]) {
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
            data = new float[new_capacity];
            capacity = new_capacity;
        } else {
            if (new_capacity > capacity) {
                float new_data[] = new float[new_capacity];
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

    public float at(int index) {
        return data[index];
    }

    public void set(float value, int index) {
        data[index] = value;
    }

    public void clear() {
        size = 0;
    }

    public void add(float value) {
        size++;
        checkCapacity();
        data[size() - 1] = value;
    }

    public void insertAt(float value, int index) {
        if (index < size()) {
            size++;
            checkCapacity();
            System.arraycopy(data, index, data, index + 1, size() - index - 1);
            data[index] = value;
        } else {
            add(value);
        }
    }

    public int index(float value) {
        int index = -1;
        for (int i = 0; i < size(); i++) {
            if (value == at(i)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public boolean equals(FloatVector x) {
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
            obj_out.writeFloat(data[i]);
        }
    }

    private void readObject(ObjectInputStream obj_in) throws IOException {
        try {
            obj_in.defaultReadObject();
            size = obj_in.readInt();
            capacity = size;
            data = new float[size];
            for (int i = 0; i < size; i++) {
                data[i] = obj_in.readFloat();
            }
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }
    }

    public void save(DataOutputStream out) throws IOException {
        out.writeInt(size);
        for (int i = 0; i < size; i++) {
            out.writeFloat(data[i]);
        }
    }

    public FloatVector(Element element) throws IOException {
        this(Integer.parseInt(element.getAttribute("size")));
        size = capacity;
        for (int i = 0; i < size; i++) {
            data[i] = Float.parseFloat(element.getAttribute("D_" + i));
        }
    }

    public FloatVector(DataInputStream in, Document doc, Element parent_element, int iSize) throws IOException {
        this(iSize = in.readInt());
        size = capacity;
        Element element = doc.createElement("FloatVector");
        element.setAttribute("size", Integer.toString(iSize));
        for (int i = 0; i < size; i++) {
            data[i] = in.readFloat();
            element.setAttribute("D_" + i, Float.toString(data[i]));
        }
        parent_element.appendChild(element);
    }

    public FloatVector(DataInputStream in) throws IOException {
        this(in.readInt());
        size = capacity;
        for (int i = 0; i < size; i++) {
            data[i] = in.readFloat();
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

    public FloatVector distinct() {
        FloatVector float_vector = new FloatVector();
        for (int i = 0; i < size(); i++) {
            int count = 0;
            for (int j = 0; j < float_vector.size(); j++) {
                if (float_vector.at(j) == at(i)) {
                    count++;
                }
            }
            if (count < size()) {
                float_vector.add(at(i));
            }
        }
        return float_vector;
    }

    public void reverse() {
        int n = size();
        int head = 0;
        int tail = n - 1;
        while (head < tail) {
            float temp = data[head];
            data[head] = data[tail];
            data[tail] = temp;
            head++;
            tail--;
        }
    }

    public FloatVector(ResultSet r) throws SQLException {
        while (r.next()) {
            add(r.getFloat(1));
        }
    }

    public static FloatVector loadFloatVector(ResultSet r) throws SQLException {
        return new FloatVector(r);
    }

    public static FloatVector loadFloatVector(Connection con, String SQL) throws SQLException {
        Statement statement = null;
        ResultSet result_set = null;
        try {
            statement = con.createStatement();
            result_set = statement.executeQuery(SQL);
            return loadFloatVector(result_set);
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

    public void toXML(Document doc, Element parent_element) {
        Element element = doc.createElement("FloatVector");
        element.setAttribute("size", Integer.toString(this.size()));
        for (int i = 0; i < size(); i++) {
            element.setAttribute("D_" + i, Float.toString(this.at(i)));
        }
        parent_element.appendChild(element);
    }

    public Float average() {
        float sum = 0;
        for (int i = 0; i < size(); i++) {
            sum += this.at(i);
        }
        _average = sum / this.size();
        return _average;
    }

    public Float standardDeviation() {
        if (_average == null) {
            average();
        }
        float avg = _average.floatValue();
        float sum = 0;
        for (int i = 0; i < size(); i++) {
            sum += Math.pow(this.at(i) - avg, 2);
        }
        return new Float(Math.sqrt((double) (sum / size())));
    }

    public FloatVector distinctValues() {
        FloatVector float_vector = new FloatVector();
        return float_vector;
    }

    private void QuickSort(float data[], int l, int r) throws Exception {
        int M = 4;
        int i;
        int j;
        float v;
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

    private void swap(float data[], int i, int j) {
        float T;
        T = data[i];
        data[i] = data[j];
        data[j] = T;
    }

    private void InsertionSort(float data[], int lo0, int hi0) throws Exception {
        int i;
        int j;
        float v;
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
}

package com.rbnb.fie;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
  * A Java interface to MATLAB's .MAT format.
  * @author WHF
  * @version 2007/08/22
  */
public class JMatStruct {

    /**
	  * Creates an anonymous MATLAB structure.
	  */
    public JMatStruct() {
        this(null);
    }

    /**
	  * Creates a MATLAB structure given the specified name.
	  */
    public JMatStruct(String name) {
        setStructName(name);
    }

    /**
	  * Adds a field to this structure.  Returns its index.  Any spaces in the
	  *   field name will be replaced with underscores.
	  * @NullPointerException if fieldName or data are null.
	  */
    public int addField(String fieldName, int rows, int cols, Serializable data) {
        field_t f = new field_t();
        f.fieldName = fieldName.replace(' ', '_');
        f.rows = rows;
        f.cols = cols;
        f.data = data;
        sFields.add(f);
        return sFields.size() - 1;
    }

    /**
	  * Utility method which wraps an array in a Serializable and adds it.
	  * @param data  A primative one-D array.
	  * @return  the field index.
	  * @throws IllegalArgumentException  if the data is not an array or if the
	  *   type is not supported.
	  */
    public int addField(String fieldName, int rows, int cols, Object data) {
        Serializable ser;
        if (data instanceof double[]) ser = new DoubleSerial((double[]) data); else if (data instanceof float[]) ser = new FloatSerial((float[]) data); else if (data instanceof long[]) ser = new LongSerial((long[]) data); else if (data instanceof int[]) ser = new IntSerial((int[]) data); else if (data instanceof short[]) ser = new ShortSerial((short[]) data); else if (data instanceof char[]) ser = new CharSerial((char[]) data); else if (data instanceof byte[]) ser = new ByteSerial((byte[]) data); else throw new IllegalArgumentException("Data type " + data.getClass() + " not supported.");
        return addField(fieldName, rows, cols, ser);
    }

    /**
	  * Adds a child structure to this structure.  Note that arrays of 
	  *  structures are not supported.
	  */
    public int addChild(String fieldName, JMatStruct child) {
        field_t f = new field_t();
        f.fieldName = fieldName;
        f.fieldName = fieldName.replace(' ', '_');
        child.setStructName("");
        f.rows = 1;
        f.cols = 1;
        f.child = child;
        sFields.add(f);
        return sFields.size() - 1;
    }

    private static final String prefix = ("MATLAB 5.0 MAT-file, Platform: PCWIN, Created on: ");

    private static final int filemagic = 0x4d490100;

    private static final int magic[] = { 0x0e, 0x00, 0x06, 0x08, 0x02, 0x00, 0x05, 0x08, 0x01, 0x01 };

    private static final int magic2[] = { 0x00040005, 0x20, 0x01 };

    private static class FieldHeader {

        public int unknown1, unknown2, elementType, elementSize, unknown3, unknown4, rows, cols, fieldCharSize, fieldNameLength, compression, blockBytes;

        public FieldHeader() {
            unknown1 = 0x06;
            unknown2 = 0x08;
            elementType = 0x00;
            elementSize = 0x08;
            unknown3 = 0x05;
            unknown4 = 0x08;
            rows = 0x0;
            cols = 0x0;
            fieldCharSize = 0x01;
            fieldNameLength = 0x00;
            compression = 0x09;
            blockBytes = 0x0;
        }

        public void write(ByteBuffer bb) {
            bb.putInt(unknown1).putInt(unknown2).putInt(elementType).putInt(elementSize).putInt(unknown3).putInt(unknown4).putInt(rows).putInt(cols).putInt(fieldCharSize).putInt(fieldNameLength).putInt(compression).putInt(blockBytes);
        }
    }

    ;

    private void put(ByteBuffer bb, int[] array) {
        for (int ii = 0; ii < array.length; ++ii) bb.putInt(array[ii]);
    }

    private void writeField(RandomAccessFile file, ByteBuffer bb, field_t field) throws java.io.IOException {
        FileChannel fc = file.getChannel();
        fh.rows = field.rows;
        fh.cols = field.cols;
        fh.elementType = field.data.getElementDescriptor().getCode();
        fh.elementSize = field.data.getElementDescriptor().getSize();
        fh.compression = field.data.getElementDescriptor().getCode() == ElementDescriptor.CHAR_DESCRIPTOR.getCode() ? 0x4 : 0x9;
        long currp = fc.position();
        bb.clear();
        bb.putInt(0xe);
        bb.flip();
        fc.write(bb);
        long headerp = fc.position();
        bb.clear();
        bb.putInt(0);
        fh.write(bb);
        bb.flip();
        fc.write(bb);
        long headerSize = fc.position() - 4 - headerp;
        currp = fc.position();
        if (fh.cols == -1) throw new UnsupportedOperationException("Unknown number of columns not supported.");
        fh.blockBytes = fh.rows * fh.cols * fh.elementSize;
        ByteBuffer fieldBuffer = ByteBuffer.allocateDirect(fh.blockBytes);
        fieldBuffer.order(ByteOrder.LITTLE_ENDIAN);
        field.data.writeBinary(fieldBuffer);
        if (fieldBuffer.position() != fieldBuffer.capacity()) throw new IllegalStateException("Buffer not filled.  " + fieldBuffer.position() + " != " + fieldBuffer.capacity());
        fieldBuffer.flip();
        fc.write(fieldBuffer);
        int bytesWritten = fh.blockBytes;
        while (bytesWritten % 8 != 0) {
            file.write(0);
            ++bytesWritten;
        }
        currp = fc.position();
        fc.position(headerp);
        bb.clear();
        bb.putInt((int) (currp - headerp - 4));
        fh.write(bb);
        bb.flip();
        fc.write(bb);
        fc.position(currp);
    }

    private void write(RandomAccessFile file) throws java.io.IOException {
        FileChannel fc = file.getChannel();
        ByteBuffer bb = ByteBuffer.allocate(1024);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        long structStart = fc.position() + 8;
        put(bb, magic);
        if (structName.length() <= 4) {
            bb.putShort((short) 0x1);
            bb.putShort((short) structName.length());
            bb.put(structName.getBytes());
            bb.position(bb.position() + 4 - structName.length());
        } else {
            bb.putInt(0x1);
            bb.putInt(structName.length());
            bb.put(structName.getBytes());
            int rem = bb.position() % 8;
            bb.position(bb.position() + 8 - rem);
        }
        put(bb, magic2);
        bb.putInt(0x20 * sFields.size());
        bb.flip();
        fc.write(bb);
        long fieldstart = fc.position();
        for (int ii = 0; ii < sFields.size(); ++ii) {
            field_t field = (field_t) sFields.get(ii);
            file.writeBytes(field.fieldName);
            fc.position(fieldstart += 0x20);
        }
        for (int ii = 0; ii < sFields.size(); ++ii) {
            field_t field = (field_t) sFields.get(ii);
            if (field.child != null) field.child.write(file); else writeField(file, bb, field);
        }
        long end = fc.position();
        int temp = (int) (fc.position() - structStart);
        fc.position(structStart - 4);
        bb.clear();
        bb.putInt(temp);
        bb.flip();
        fc.write(bb);
        fc.position(end);
    }

    /**
	  * Write the structure to a file with the specified name.  If null,
	  *  the filename will be constructed from the structure name.
	  */
    public void writeStruct(String fname) throws java.io.IOException {
        if (fname == null) fname = structName + ".mat";
        RandomAccessFile file = new RandomAccessFile(fname, "rw");
        file.setLength(0);
        file.writeBytes(prefix);
        file.writeBytes(new java.util.Date().toString());
        for (int fill = (int) (file.getFilePointer()); fill < 0x7c; fill++) file.write(' ');
        file.write(0x00);
        file.write(0x01);
        file.write(0x49);
        file.write(0x4D);
        write(file);
        file.close();
    }

    /**
	  * Erases all child fields and the structure's name.
	  */
    public void clear() {
        sFields.clear();
        structName = null;
    }

    /**
	  * Returns the name of this structure.
	  */
    public String getStructName() {
        return structName;
    }

    /**
	  * Sets the name of this structure.  Any spaces in the name will be 
	  *   replaced with underscores.  Final because called from constructor.
	  */
    public final void setStructName(String name) {
        if (name != null) structName = name.replace(' ', '_'); else structName = null;
    }

    /**
	  * Returns the number of fields in this structure.
	  */
    public int getNumFields() {
        return sFields.size();
    }

    /**
	  * Returns the name of the specified field.
	  */
    public String getFieldName(int field) {
        return ((field_t) sFields.get(field)).fieldName;
    }

    /**
	  * Returns the number of rows in a field of this structure.
	  */
    public int getFieldRows(int field) {
        return ((field_t) sFields.get(field)).rows;
    }

    /**
	  * Returns the number of columns in a field of this structure.
	  */
    public int getFieldCols(int field) {
        return ((field_t) sFields.get(field)).cols;
    }

    /**
	  * Returns the data object of a field.
	  */
    public Serializable getFieldData(int field) {
        return ((field_t) sFields.get(field)).data;
    }

    /**
	  * Finds the index of a field given its name, or -1.
	  */
    public int findField(String fieldName) {
        for (int ii = 0; ii < sFields.size(); ++ii) if (((field_t) sFields.get(ii)).fieldName.equals(fieldName)) return ii;
        return -1;
    }

    private String structName;

    private static class field_t {

        public String fieldName;

        public int rows, cols;

        public Serializable data;

        public JMatStruct child;
    }

    private final ArrayList sFields = new ArrayList();

    private final FieldHeader fh = new FieldHeader();

    /**
	  * Testing only.
	  */
    public static void main(String[] args) throws Exception {
        String[] names = { "foo", "foobar", "foobarfoobarfoobarfoobarfoobarfoo" };
        for (int iii = 0; iii < 3; ++iii) {
            JMatStruct jms = new JMatStruct(names[iii]);
            int len = 57;
            double[] dataD = new double[len];
            float[] dataF = new float[len];
            long[] dataL = new long[len];
            int[] dataI = new int[len];
            short[] dataS = new short[len];
            char[] dataC = new char[len];
            byte[] dataB = new byte[len];
            for (int ii = 0; ii < len; ++ii) {
                double x = ii * Math.PI;
                dataD[ii] = x;
                dataF[ii] = (float) x;
                dataL[ii] = (long) x;
                dataI[ii] = (int) x;
                dataS[ii] = (short) x;
                dataC[ii] = (char) ('A' + ii);
                dataB[ii] = (byte) x;
            }
            jms.addField("double", 1, len, dataD);
            jms.addField("float", 1, len, dataF);
            jms.addField("long", 1, len, dataL);
            jms.addField("int", 1, len, dataI);
            jms.addField("short", 1, len, dataS);
            jms.addField("char", 1, len, dataC);
            jms.addField("byte", 1, len, dataB);
            JMatStruct jms2 = new JMatStruct();
            jms2.addField("child_child_child_child_child_X", 1, len, dataD);
            jms.addChild("child", jms2);
            jms.writeStruct("test" + iii + ".mat");
        }
    }
}

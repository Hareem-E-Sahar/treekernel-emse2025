package com.ibm.wala.shrikeBT.shrikeCT.tools;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import com.ibm.wala.shrikeBT.Util;
import com.ibm.wala.shrikeCT.ClassReader;
import com.ibm.wala.shrikeCT.ClassWriter;
import com.ibm.wala.shrikeCT.ConstantValueWriter;
import com.ibm.wala.shrikeCT.InvalidClassFileException;

public class AddSerialVersion {

    private AddSerialVersion() {
    }

    /**
   * This method computes the serialVersionUID for class r (if there isn't one already) and adds the field to the classwriter w.
   * 
   * When run as a program, just takes a list of class files as command line arguments and computes their serialVersionUIDs.
   * 
   * @throws IllegalArgumentException if r is null
   */
    public static void addSerialVersionUID(ClassReader r, ClassWriter w) throws InvalidClassFileException {
        if (r == null) {
            throw new IllegalArgumentException("r is null");
        }
        int numFields = r.getFieldCount();
        for (int i = 0; i < numFields; i++) {
            if (r.getFieldName(i).equals("serialVersionUID")) {
                return;
            }
        }
        long UID = computeSerialVersionUID(r);
        w.addField(ClassReader.ACC_PUBLIC | ClassReader.ACC_STATIC | ClassReader.ACC_FINAL, "serialVersionUID", "J", new ClassWriter.Element[] { new ConstantValueWriter(w, UID) });
    }

    /**
   * This class implements a stream that just discards everything written to it.
   */
    public static final class SinkOutputStream extends OutputStream {

        @Override
        public void write(int b) {
        }

        @Override
        public void write(byte[] b) {
        }

        @Override
        public void write(byte[] b, int off, int len) {
        }
    }

    /**
   * This method computes the serialVersionUID for class r. See the specification at
   * http://java.sun.com/j2se/1.4.2/docs/guide/serialization/spec/class.html
   * 
   * @throws IllegalArgumentException if r is null
   */
    public static long computeSerialVersionUID(final ClassReader r) throws InvalidClassFileException {
        if (r == null) {
            throw new IllegalArgumentException("r is null");
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new Error("SHA algorithm not supported: " + e.getMessage());
        }
        SinkOutputStream sink = new SinkOutputStream();
        DataOutputStream out = new DataOutputStream(new DigestOutputStream(sink, digest));
        try {
            out.writeUTF(r.getName());
            out.writeInt(r.getAccessFlags());
            String[] interfaces = r.getInterfaceNames();
            Arrays.sort(interfaces);
            for (int i = 0; i < interfaces.length; i++) {
                out.writeUTF(interfaces[i]);
            }
            Integer[] fields = new Integer[r.getFieldCount()];
            final String[] fieldNames = new String[fields.length];
            int fieldCount = 0;
            for (int f = 0; f < fields.length; f++) {
                int flags = r.getFieldAccessFlags(f);
                if ((flags & ClassReader.ACC_PRIVATE) == 0 || (flags & (ClassReader.ACC_STATIC | ClassReader.ACC_TRANSIENT)) == 0) {
                    fields[fieldCount] = new Integer(f);
                    fieldNames[f] = r.getFieldName(f);
                    fieldCount++;
                }
            }
            Arrays.sort(fields, 0, fieldCount, new Comparator<Integer>() {

                public int compare(Integer o1, Integer o2) {
                    String name1 = fieldNames[o1.intValue()];
                    String name2 = fieldNames[o2.intValue()];
                    return name1.compareTo(name2);
                }
            });
            for (int i = 0; i < fieldCount; i++) {
                int f = fields[i].intValue();
                out.writeUTF(fieldNames[f]);
                out.writeInt(r.getFieldAccessFlags(f));
                out.writeUTF(r.getFieldType(f));
            }
            Integer[] methods = new Integer[r.getMethodCount()];
            final int[] methodKinds = new int[methods.length];
            final String[] methodSigs = new String[methods.length];
            int methodCount = 0;
            for (int m = 0; m < methodSigs.length; m++) {
                String name = r.getMethodName(m);
                int flags = r.getMethodAccessFlags(m);
                if (name.equals("<clinit>") || (flags & ClassReader.ACC_PRIVATE) == 0) {
                    methods[methodCount] = new Integer(m);
                    methodSigs[m] = name + r.getMethodType(m);
                    if (name.equals("<clinit>")) {
                        methodKinds[m] = 0;
                    } else if (name.equals("<init>")) {
                        methodKinds[m] = 1;
                    } else {
                        methodKinds[m] = 2;
                    }
                    methodCount++;
                }
            }
            Arrays.sort(methods, 0, methodCount, new Comparator<Integer>() {

                public int compare(Integer o1, Integer o2) {
                    int m1 = o1.intValue();
                    int m2 = o2.intValue();
                    if (methodKinds[m1] != methodKinds[m2]) {
                        return methodKinds[m1] - methodKinds[m2];
                    }
                    String name1 = methodSigs[m1];
                    String name2 = methodSigs[m2];
                    return name1.compareTo(name2);
                }
            });
            for (int i = 0; i < methodCount; i++) {
                int m = methods[i].intValue();
                out.writeUTF(r.getMethodName(m));
                out.writeInt(r.getMethodAccessFlags(m));
                out.writeUTF(r.getMethodType(m));
            }
        } catch (IOException e1) {
            throw new Error("Unexpected IOException: " + e1.getMessage());
        } finally {
            try {
                out.close();
            } catch (IOException e2) {
            }
        }
        byte[] hash = digest.digest();
        return (hash[0] & 0xFF) | (hash[1] & 0xFF) << 8 | (hash[2] & 0xFF) << 16 | hash[3] << 24 | (hash[4] & 0xFF) << 32 | (hash[5] & 0xFF) << 40 | (hash[6] & 0xFF) << 48 | (hash[7] & 0xFF) << 56;
    }

    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                throw new IllegalArgumentException("args[" + i + "] is null");
            }
            try {
                byte[] data = Util.readFully(new FileInputStream(args[i]));
                ClassReader r = new ClassReader(data);
                System.out.println(Util.makeClass(r.getName()) + ": serialVersionUID = " + computeSerialVersionUID(r));
            } catch (FileNotFoundException e) {
                System.err.println("File not found: " + args[i]);
            } catch (IOException e) {
                System.err.println("Error reading file: " + args[i]);
            } catch (InvalidClassFileException e) {
                System.err.println("Invalid class file: " + args[i]);
            }
        }
    }
}

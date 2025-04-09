package blueprint4j.utils;

import java.util.*;
import java.util.zip.*;
import java.io.*;

public class VectorProperties implements java.io.Serializable {

    private Vector store = new Vector();

    public VectorProperties() {
    }

    public VectorProperties(ZipInputStream zipinput) throws IOException {
        for (ZipEntry entry = zipinput.getNextEntry(); entry != null; entry = zipinput.getNextEntry()) {
            ByteArrayOutputStream tmpstream = new ByteArrayOutputStream();
            for (int r = zipinput.read(); r != -1; r = zipinput.read()) {
                tmpstream.write(r);
            }
            Properties properties = new Properties();
            properties.load(new java.io.ByteArrayInputStream(tmpstream.toByteArray()));
            add(properties);
        }
    }

    public Properties get(int pos) {
        return (Properties) store.get(pos);
    }

    public void add(Properties item) {
        store.add(item);
    }

    public boolean remove(Properties item) {
        return store.remove(item);
    }

    public Properties remove(int pos) {
        return (Properties) store.remove(pos);
    }

    public int size() {
        return store.size();
    }

    public String toString(String delimiter) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < size(); i++) {
            sb.append(get(i).toString() + delimiter);
        }
        return sb.toString();
    }

    public String toString() {
        return toString("\n");
    }

    public byte[] toZip() throws IOException {
        ByteArrayOutputStream bstream = new ByteArrayOutputStream();
        ZipOutputStream zipstream = new ZipOutputStream(bstream);
        for (int i = 0; i < size(); i++) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            get(i).store(stream, "");
            zipstream.putNextEntry(new ZipEntry("" + i));
            zipstream.write(stream.toByteArray());
            zipstream.closeEntry();
        }
        return bstream.toByteArray();
    }

    public Properties[] toArray() {
        Properties properties[] = new Properties[size()];
        for (int i = 0; i < size(); i++) {
            properties[i] = get(i);
        }
        return properties;
    }
}

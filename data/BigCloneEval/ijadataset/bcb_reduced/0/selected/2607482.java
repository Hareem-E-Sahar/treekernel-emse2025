package com.vangent.hieos.xutil.iosupport;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.InputStream;
import java.security.MessageDigest;
import javax.activation.FileDataSource;

public class Sha1Bean {

    public static final String PROP_SAMPLE_PROPERTY = "sampleProperty";

    private PropertyChangeSupport propertySupport;

    /**
     * Holds value of property byteStream.
     */
    private byte[] byteStream;

    public Sha1Bean() {
        propertySupport = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    /**
     * Setter for property byteStream.
     * @param byteStream New value of property byteStream.
     */
    public void setByteStream(byte[] byteStream) {
        this.byteStream = byteStream;
    }

    /**
     * Getter for property sha1.
     * @return Value of property sha1.
     */
    public byte[] getSha1() throws Exception {
        if (byteStream == null) throw new Exception("Sha1Bean:getSha1: byteStream is null");
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (Exception e) {
        }
        byte[] result = md.digest(byteStream);
        return result;
    }

    /**
     * Getter for property sha1String.
     * @return Value of property sha1String.
     */
    public String getSha1String() throws Exception {
        byte[] sha1 = getSha1();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < sha1.length; i++) {
            String h = Integer.toHexString(sha1[i] & 0xff);
            if (h.length() == 1) h = "0" + h;
            buf.append(h);
        }
        return new String(buf);
    }

    public String getSha1File(File input) throws Exception {
        javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(new FileDataSource(input));
        InputStream is = dataHandler.getInputStream();
        int file_length = (int) input.length();
        byte[] inb = new byte[file_length];
        is.read(inb);
        setByteStream(inb);
        return getSha1String();
    }

    public static void main(String[] argv) throws Exception {
        String fin = Io.stringFromFile(new File("/Users/bill/ihe/testing/sha1/utf8ccds/gallowYounger.xml"));
        byte[] inb = fin.getBytes();
        Sha1Bean sb = new Sha1Bean();
        sb.setByteStream(inb);
        System.out.println(sb.getSha1String());
    }
}

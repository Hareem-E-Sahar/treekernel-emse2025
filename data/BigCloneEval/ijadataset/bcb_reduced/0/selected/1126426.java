package de.plugmail.data;

import java.util.zip.CRC32;

/**
 * <p>Default Attachment Bean</p>
 * @author Aug
 * @version 1.0
 * @since 23.03.2006
 */
public class Attachment {

    private long aid;

    private String filename;

    private long size = 0l;

    private byte[] file;

    private String crc32;

    private String type;

    public long getAid() {
        return aid;
    }

    public void setAid(long id) {
        aid = id;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String file) {
        this.filename = file;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long s) {
        size = s;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] f) {
        file = f;
    }

    public String getCrc32() {
        return crc32;
    }

    public String setCrc32() {
        CRC32 crc = new CRC32();
        crc.update(file);
        crc32 = "" + crc.getValue();
        return crc32;
    }

    public String getType() {
        return type;
    }

    public void setType(String typ) {
        type = typ;
    }

    public String toString() {
        String s = "";
        s += "file=" + filename + ";";
        s += "size=" + size + ";";
        s += "type=" + type + ";";
        s += "CRC32=" + crc32 + ";";
        return s;
    }
}

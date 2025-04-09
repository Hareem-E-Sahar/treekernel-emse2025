package org.gnu.amSpacks.model;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class UpdateInfo {

    static final String VHEADER = "versionInformation.aupService.xml";

    public TargetVersion from;

    public TargetVersion to;

    public TreeSet<String> deleteFiles = new TreeSet<String>();

    public UpdateInfo() {
    }

    ;

    public UpdateInfo(TargetVersion _from, TargetVersion _to) {
        from = _from;
        to = _to;
    }

    public void writeTo(ZipOutputStream stream) throws IOException {
        ZipEntry versions = new ZipEntry(VHEADER);
        stream.putNextEntry(versions);
        XMLEncoder encoder = new XMLEncoder(stream);
        encoder.writeObject(this);
        encoder.close();
    }

    public static UpdateInfo readFrom(File zip) {
        ZipEntry z;
        UpdateInfo v = null;
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(zip);
            ZipInputStream stream = new ZipInputStream(new BufferedInputStream(fi));
            while (true) {
                z = stream.getNextEntry();
                if (z == null) {
                    break;
                }
                if (z.getName().equals(VHEADER)) {
                    XMLDecoder dec = new XMLDecoder(stream);
                    v = (UpdateInfo) dec.readObject();
                    break;
                }
            }
        } catch (Exception e) {
        } finally {
            if (fi != null) {
                try {
                    fi.close();
                } catch (IOException e) {
                }
            }
        }
        return v;
    }

    public TargetVersion getFrom() {
        return from;
    }

    public TargetVersion getTo() {
        return to;
    }

    public void setFrom(TargetVersion from) {
        this.from = from;
    }

    public void setTo(TargetVersion to) {
        this.to = to;
    }

    public TreeSet<String> getDelete() {
        return deleteFiles;
    }

    public void setDelete(TreeSet<String> delete) {
        this.deleteFiles = delete;
    }
}

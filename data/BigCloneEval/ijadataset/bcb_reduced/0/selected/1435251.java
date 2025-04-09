package org.ztest.classinfo.ser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.ztest.classinfo.ZIClassInfo;

public class ZClassInfoSerWriter {

    private Logger log = Logger.getLogger(ZClassInfoSerWriter.class);

    private final OutputStream outputStream;

    public ZClassInfoSerWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public ZClassInfoSerWriter(File file) throws FileNotFoundException {
        this(new BufferedOutputStream(new FileOutputStream(file), 16 * 1024));
    }

    public void write(ZIClassInfo classInfo) throws Exception {
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        ZipEntry ze = new ZipEntry("classinfo.ser");
        zip.putNextEntry(ze);
        ObjectOutputStream os = new ObjectOutputStream(zip);
        os.writeObject(classInfo);
        os.flush();
        zip.closeEntry();
        zip.close();
    }
}

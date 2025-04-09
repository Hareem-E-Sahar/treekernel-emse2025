package com.meschbach.cise.jam;

import com.meschbach.cise.util.StreamCopier;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author "Mark Eschbach" meschbach@gmail.com
 */
public class AddEntryStreamProcessor implements StreamProcessor {

    String name;

    InputStream source;

    public AddEntryStreamProcessor(String name, InputStream source) {
        this.name = name;
        this.source = source;
    }

    public void affectStream(ZipOutputStream output) throws IOException {
        ZipEntry ze = new ZipEntry(name);
        ze.setTime(System.currentTimeMillis());
        output.putNextEntry(ze);
        StreamCopier sc = new StreamCopier(source, output);
        sc.doCopy();
        output.closeEntry();
    }
}

package com.meschbach.cise.jam;

import com.meschbach.cise.util.StreamCopier;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author "Mark Eschbach" meschbach@gmail.com;
 */
public class ReplaceEntryVisitor implements EntryVisitor {

    InputStream alternateData;

    public ReplaceEntryVisitor(byte alternateDataBuffer[]) {
        alternateData = new ByteArrayInputStream(alternateDataBuffer);
    }

    public ReplaceEntryVisitor(InputStream alternateData) {
        this.alternateData = alternateData;
    }

    public void visitEntry(String name, ZipEntry e, ZipInputStream input, ZipOutputStream output) throws IOException {
        ZipEntry ze = new ZipEntry(e.getName());
        ze.setComment(e.getComment());
        ze.setTime(System.currentTimeMillis());
        output.putNextEntry(ze);
        StreamCopier sc = new StreamCopier(alternateData, output);
        sc.doCopy();
        output.closeEntry();
    }
}

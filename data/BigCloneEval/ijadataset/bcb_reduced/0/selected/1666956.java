package com.meschbach.cise.jam;

import com.meschbach.cise.util.StreamCopier;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author "Mark Eschbach" meschbach@gmail.com
 */
public class CopyVisitor implements EntryVisitor {

    private static final CopyVisitor SHARED_INSTANCE = new CopyVisitor();

    public static CopyVisitor getSharedInstance() {
        return SHARED_INSTANCE;
    }

    public void visitEntry(String name, ZipEntry sourceEntry, ZipInputStream input, ZipOutputStream output) throws IOException {
        ZipEntry resultingEntry = new ZipEntry(sourceEntry.getName());
        resultingEntry.setComment(sourceEntry.getComment());
        resultingEntry.setTime(sourceEntry.getTime());
        output.putNextEntry(resultingEntry);
        StreamCopier sc = new StreamCopier(input, output);
        sc.doCopy();
        output.closeEntry();
    }
}

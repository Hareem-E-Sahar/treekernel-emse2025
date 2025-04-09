package de.unkrig.commons.file.zipentrytransformation;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import de.unkrig.commons.file.contentstransformation.ContentsTransformer;

/**
 * A {@link ZipEntryTransformer} that passes the contents of the ZIP entries to a {@link ContentsTransformer} delegate.
 */
public class ZipEntryContentsTransformer implements ZipEntryTransformer {

    private final ContentsTransformer delegate;

    public ZipEntryContentsTransformer(ContentsTransformer delegate) {
        this.delegate = delegate;
    }

    public void transform(ZipEntry entry, String name, InputStream is, ZipOutputStream zos) throws IOException {
        ZipEntry entry2 = new ZipEntry(entry.getName());
        entry2.setComment(entry.getComment());
        entry2.setExtra(entry.getExtra());
        entry2.setTime(entry.getTime());
        zos.putNextEntry(entry2);
        if (entry.isDirectory()) return;
        this.delegate.transform(name, is, zos);
    }
}

package de.unkrig.commons.util.zipentrytransformation;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import de.unkrig.commons.util.ExceptionHandler;
import de.unkrig.commons.util.contentstransformation.ContentsTransformer;

/**
 * A {@link ZipEntryTransformer} that passes the contents of the ZIP entries to a {@link ContentsTransformer} delegate.
 */
public class ZipEntryContentsTransformer implements ZipEntryTransformer {

    private final ContentsTransformer delegate;

    private final ExceptionHandler<IOException> exceptionHandler;

    public ZipEntryContentsTransformer(ContentsTransformer delegate) {
        this.delegate = delegate;
        this.exceptionHandler = ExceptionHandler.defaultHandler();
    }

    public ZipEntryContentsTransformer(ContentsTransformer delegate, ExceptionHandler<IOException> exceptionHandler) {
        this.delegate = delegate;
        this.exceptionHandler = exceptionHandler;
    }

    public void transform(ZipEntry entry, String name, InputStream is, ZipOutputStream zos) throws IOException {
        ZipEntry entry2 = new ZipEntry(entry.getName());
        entry2.setComment(entry.getComment());
        entry2.setExtra(entry.getExtra());
        entry2.setTime(entry.getTime());
        zos.putNextEntry(entry2);
        if (entry.isDirectory()) return;
        try {
            this.delegate.transform(name, is, zos);
        } catch (IOException ioe) {
            IOException ioe2 = new IOException(name + ": " + ioe.getLocalizedMessage());
            ioe2.initCause(ioe);
            this.exceptionHandler.handle(ioe2);
        } catch (RuntimeException re) {
            this.exceptionHandler.handle(new RuntimeException(name + ": " + re.getLocalizedMessage(), re));
        }
    }
}

package de.unkrig.commons.util.contentstransformation;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import de.unkrig.commons.util.pattern.Glob;

public class ZipContentsTransformer implements ContentsTransformer {

    private static final int LOCHDR = 30;

    final Glob selector;

    private final ContentsTransformer delegate1;

    private final ContentsTransformer delegate2;

    /**
     * Processes all ZIP entries recursively. Those who match the {@code selector} are passed to {@code delegate1},
     * all other to {@code delegate2}.
     */
    public ZipContentsTransformer(Glob selector, ContentsTransformer delegate1, ContentsTransformer delegate2) {
        this.selector = selector;
        this.delegate1 = delegate1;
        this.delegate2 = delegate2;
    }

    /**
     * If the contents of {@code is} is not in ZIP format, then it is transformed through {code delegate2}. Otherwise,
     * it is opened as a {@link ZipInputStream}, a {@link ZipOutputStream} is created on {@code os}, and for each
     * entry on the {@link ZipInputStream}, an entry with the same name is created on the {@link ZipOutputStream},
     * and then this method is called recursively on the entry contents, and the transformed output is stored
     * in the {@link ZipOutputStream}.
     */
    public void transform(InputStream is, OutputStream os) throws IOException {
        if (!is.markSupported()) {
            is = new BufferedInputStream(is, LOCHDR);
        }
        is.mark(LOCHDR);
        ZipInputStream zis = new ZipInputStream(is);
        ZipEntry e = zis.getNextEntry();
        if (e == null) {
            is.reset();
            this.delegate2.transform(is, os);
            return;
        }
        ZipOutputStream zos = new ZipOutputStream(os);
        try {
            do {
                {
                    ZipEntry e2 = new ZipEntry(e.getName());
                    e2.setComment(e.getComment());
                    e2.setExtra(e.getExtra());
                    e2.setTime(e.getTime());
                    zos.putNextEntry(e2);
                }
                Glob subselector;
                {
                    final String prefix = e.getName() + '!';
                    subselector = new Glob() {

                        @Override
                        public boolean matches(String subject) {
                            return ZipContentsTransformer.this.selector.matches(prefix + subject);
                        }
                    };
                }
                new ZipContentsTransformer(subselector, this.delegate1, this.delegate1 == this.delegate2 || this.selector.matches(e.getName()) ? this.delegate1 : this.delegate2).transform(zis, zos);
                e = zis.getNextEntry();
            } while (e != null);
        } catch (IOException ioe) {
            IOException ioe2 = new IOException("Transforming ZIP entry '" + e.getName() + "': " + ioe.getMessage());
            ioe2.initCause(ioe);
            throw ioe2;
        } catch (RuntimeException re) {
            throw new RuntimeException("Transforming ZIP entry '" + e.getName() + "': " + re.getMessage(), re);
        }
        zos.finish();
    }
}

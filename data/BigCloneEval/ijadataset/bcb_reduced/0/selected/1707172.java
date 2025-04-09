package mipt.io.jdom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import mipt.io.IO;
import org.jdom.Element;

/**
 * The implementation if item 1 described in {@link IO} for JDOM.
 * @author Alexey Evdokimov
 */
public class ZipDOMIO extends DOMIO {

    protected ZipOutputStream out;

    protected ZipInputStream in;

    public ZipDOMIO() {
    }

    public ZipDOMIO(OutputStream out, InputStream in) throws IOException {
        super(out, in);
    }

    public void setStreams(OutputStream out, InputStream in) {
        this.out = new ZipOutputStream(out);
        this.in = new ZipInputStream(in) {

            public void close() throws IOException {
            }
        };
    }

    protected final InputStream getIn() {
        return in;
    }

    protected final OutputStream getOut() {
        return out;
    }

    public Element read() throws IOException {
        while (in.getNextEntry() == null) Thread.yield();
        return super.read();
    }

    public void write(Element element) throws IOException {
        out.putNextEntry(new ZipEntry("e" + counter++));
        super.write(element);
        out.closeEntry();
        out.flush();
    }

    private long counter = 0;
}

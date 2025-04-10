package fanc.xml;

import java.io.*;
import java.util.zip.*;

/**
 * XWriter is a specialized Writer that provides
 * support for generating an XML output stream.
 *
 * @author    Brian Frank
 * @creation  29 Sept 00
 * @version   $Revision: 8$ $Date: 19-Jul-04 4:19:10 PM$
 */
public class XWriter extends Writer {

    /**
   * Construct writer for specified file.
   */
    public XWriter(File file) throws IOException {
        this(new BufferedOutputStream(new FileOutputStream(file)));
    }

    /**
   * Construct writer for specified output stream.
   */
    public XWriter(OutputStream out) throws IOException {
        this.sink = out;
    }

    /**
   * Write the specified Object and return this.
   */
    public XWriter w(Object x) {
        write(String.valueOf(x));
        return this;
    }

    /**
   * Write the specified boolean and return this.
   */
    public final XWriter w(boolean x) {
        write(String.valueOf(x));
        return this;
    }

    /**
   * Write the specified char and return this.
   */
    public final XWriter w(char x) {
        write(x);
        return this;
    }

    /**
   * Write the specified int and return this.
   */
    public final XWriter w(int x) {
        write(String.valueOf(x));
        return this;
    }

    /**
   * Write the specified long and return this.
   */
    public final XWriter w(long x) {
        write(String.valueOf(x));
        return this;
    }

    /**
   * Write the specified float and return this.
   */
    public final XWriter w(float x) {
        write(String.valueOf(x));
        return this;
    }

    /**
   * Write the specified double and return this.
   */
    public final XWriter w(double x) {
        write(String.valueOf(x));
        return this;
    }

    /**
   * Write a newline character and return this.
   */
    public final XWriter nl() {
        write('\n');
        return this;
    }

    /**
   * Write the specified number of spaces.
   */
    public final XWriter indent(int indent) {
        write(getSpaces(indent));
        return this;
    }

    /**
   * Write an attribute pair <code>name="value"</code>
   * where the value is written using safe().
   */
    public final XWriter attr(String name, String value) {
        write(name);
        write('=');
        write('"');
        safe(value);
        write('"');
        return this;
    }

    /**
   * This write the standard prolog
   * <code>&lt;?xml version="1.0" encoding="UTF-8"?&gt;</code>
   */
    public XWriter prolog() {
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        return this;
    }

    /**
   * Convenience for <code>XWriter.safe(this, s, escapeWhitespace)</code>.
   */
    public final XWriter safe(String s, boolean escapeWhitespace) {
        try {
            XWriter.safe(xout, s, escapeWhitespace);
            return this;
        } catch (IOException e) {
            throw error(e);
        }
    }

    /**
   * Convenience for <code>XWriter.safe(this, s, true)</code>.
   */
    public final XWriter safe(String s) {
        try {
            XWriter.safe(xout, s, true);
            return this;
        } catch (IOException e) {
            throw error(e);
        }
    }

    /**
   * Convenience for <code>XWriter.safe(this, c, escapeWhitespace)</code>.
   */
    public final XWriter safe(int c, boolean escapeWhitespace) {
        try {
            XWriter.safe(this, c, escapeWhitespace);
            return this;
        } catch (IOException e) {
            throw error(e);
        }
    }

    /**
   * This writes each character in the string to the output stream
   * using the <code>safe(Writer, int, boolean)</code> method.
   */
    public static void safe(Writer out, String s, boolean escapeWhitespace) throws IOException {
        int len = s.length();
        for (int i = 0; i < len; ++i) safe(out, s.charAt(i), escapeWhitespace);
    }

    /**
   * Write a "safe" character.  This method will escape unsafe
   * characters common in XML and HTML markup.
   * <ul>
   * <li>'&lt;' -> &amp;lt;</li>
   * <li>'&gt;' -> &amp;gt;</li>
   * <li>'&amp;' -> &amp;amp;</li>
   * <li>'&apos;' -> &amp;#x27;</li>
   * <li>'&quot;' -> &amp;#x22;</li>
   * <li>Below 0x20 -> &amp;#x{hex};</li>
   * <li>Above 0x7E -> &amp;#x{hex};</li>
   * </ul>
   */
    public static void safe(Writer out, int c, boolean escapeWhitespace) throws IOException {
        if (c < 0x20 || c > 0x7e || c == '\'' || c == '"') {
            if (!escapeWhitespace) {
                if (c == '\n') {
                    out.write('\n');
                    return;
                }
                if (c == '\r') {
                    out.write('\r');
                    return;
                }
                if (c == '\t') {
                    out.write('\t');
                    return;
                }
            }
            out.write("&#x");
            out.write(Integer.toHexString(c));
            out.write(';');
        } else if (c == '<') out.write("&lt;"); else if (c == '>') out.write("&gt;"); else if (c == '&') out.write("&amp;"); else out.write((char) c);
    }

    /**
   * Return if this XWriter is being used to generate a PKZIP file
   * containing the XML document. See <code>setZipped()</code>
   */
    public boolean isZipped() {
        return zipped;
    }

    /**
   * If set to true, then XWriter generates a compressed PKZIP
   * file with one entry called "file.xml".  This method cannot be
   * called once bytes have been written.  Zipped XWriters should
   * only be used with stand alone files, it should not be used in
   * streams mixed with other data.  This feature is used in conjunction
   * with XParser, which automatically detects plain text XML versuse
   * PKZIP documents.
   */
    public void setZipped(boolean zipped) throws IOException {
        if (numWritten != 0) throw new IllegalStateException("Cannot setZipped after data has been written");
        this.zipped = zipped;
    }

    public void write(int c) {
        try {
            if (xout == null) initOut();
            numWritten++;
            xout.write(c);
        } catch (IOException e) {
            throw error(e);
        }
    }

    public void write(char[] buf) {
        try {
            if (xout == null) initOut();
            numWritten += buf.length;
            xout.write(buf);
        } catch (IOException e) {
            throw error(e);
        }
    }

    public void write(char[] buf, int off, int len) {
        try {
            if (xout == null) initOut();
            numWritten += len;
            xout.write(buf, off, len);
        } catch (IOException e) {
            throw error(e);
        }
    }

    public void write(String str) {
        try {
            if (xout == null) initOut();
            numWritten += str.length();
            xout.write(str);
        } catch (IOException e) {
            throw error(e);
        }
    }

    public void write(String str, int off, int len) {
        try {
            if (xout == null) initOut();
            numWritten += len;
            xout.write(str, off, len);
        } catch (IOException e) {
            throw error(e);
        }
    }

    public void flush() {
        try {
            if (xout == null) initOut();
            xout.flush();
        } catch (IOException e) {
            throw error(e);
        }
    }

    public void close() {
        try {
            if (xout == null) initOut();
            xout.close();
        } catch (IOException e) {
            throw error(e);
        }
    }

    void initOut() throws IOException {
        if (zipped) {
            zout = new ZipOutputStream(sink);
            zout.putNextEntry(new ZipEntry("file.xml"));
            this.xout = new OutputStreamWriter(zout, "UTF8");
        } else {
            this.xout = new OutputStreamWriter(sink, "UTF8");
        }
    }

    XException error(IOException e) {
        throw new XException(e.toString(), e);
    }

    static String getSpaces(int num) {
        try {
            return SPACES[num];
        } catch (ArrayIndexOutOfBoundsException e) {
            if (num < 0) return "";
            int len = SPACES.length;
            StringBuffer buf;
            buf = new StringBuffer(num);
            int rem = num;
            while (true) {
                if (rem < len) {
                    buf.append(SPACES[rem]);
                    break;
                } else {
                    buf.append(SPACES[len - 1]);
                    rem -= len - 1;
                }
            }
            return buf.toString();
        }
    }

    private static String[] SPACES;

    static {
        SPACES = new String[50];
        SPACES[0] = "";
        for (int i = 1; i < 50; ++i) SPACES[i] = SPACES[i - 1] + " ";
    }

    private OutputStream sink;

    private Writer xout;

    private ZipOutputStream zout;

    private boolean zipped;

    private int numWritten;
}

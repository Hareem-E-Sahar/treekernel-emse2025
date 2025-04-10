package org.japano.jasper.compiler;

import java.io.CharArrayWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.jar.JarFile;
import java.net.URL;
import java.net.MalformedURLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.japano.jasper.JasperException;
import org.japano.jasper.JspCompilationContext;

/**
 * JspReader is an input buffer for the JSP parser. It should allow
 * unlimited lookahead and pushback. It also has a bunch of parsing
 * utility methods for understanding htmlesque thingies.
 *
 * @author Anil K. Vijendran
 * @author Anselm Baird-Smith
 * @author Harish Prabandham
 * @author Rajiv Mordani
 * @author Mandar Raje
 * @author Danno Ferrin
 * @author Kin-man Chung
 * @author Shawn Bayern
 * @author Mark Roth
 */
class JspReader {

    private static Log log = LogFactory.getLog(JspReader.class);

    private Mark current;

    private String master;

    private Vector sourceFiles;

    private int currFileId;

    private int size;

    private JspCompilationContext context;

    private ErrorDispatcher err;

    private boolean singleFile;

    public JspReader(JspCompilationContext ctxt, String fname, String encoding, JarFile jarFile, ErrorDispatcher err) throws JasperException, FileNotFoundException, IOException {
        this(ctxt, fname, encoding, JspUtil.getReader(fname, encoding, jarFile, ctxt, err), err);
    }

    public JspReader(JspCompilationContext ctxt, String fname, String encoding, InputStreamReader reader, ErrorDispatcher err) throws JasperException, FileNotFoundException {
        this.context = ctxt;
        this.err = err;
        sourceFiles = new Vector();
        currFileId = 0;
        size = 0;
        singleFile = false;
        pushFile(fname, encoding, reader);
    }

    String getFile(int fileid) {
        return (String) sourceFiles.elementAt(fileid);
    }

    boolean hasMoreInput() throws JasperException {
        if (current.cursor >= current.stream.length) {
            if (singleFile) return false;
            while (popFile()) {
                if (current.cursor < current.stream.length) return true;
            }
            return false;
        }
        return true;
    }

    int nextChar() throws JasperException {
        if (!hasMoreInput()) return -1;
        int ch = current.stream[current.cursor];
        current.cursor++;
        if (ch == '\n') {
            current.line++;
            current.col = 0;
        } else {
            current.col++;
        }
        return ch;
    }

    /**
     * Back up the current cursor by one char, assumes current.cursor > 0,
     * and that the char to be pushed back is not '\n'.
     */
    void pushChar() {
        current.cursor--;
        current.col--;
    }

    String getText(Mark start, Mark stop) throws JasperException {
        Mark oldstart = mark();
        reset(start);
        CharArrayWriter caw = new CharArrayWriter();
        while (!stop.equals(mark())) caw.write(nextChar());
        caw.close();
        reset(oldstart);
        return caw.toString();
    }

    int peekChar() {
        return current.stream[current.cursor];
    }

    Mark mark() {
        return new Mark(current);
    }

    void reset(Mark mark) {
        current = new Mark(mark);
    }

    boolean matchesIgnoreCase(String string) throws JasperException {
        Mark mark = mark();
        int ch = 0;
        int i = 0;
        do {
            ch = nextChar();
            if (Character.toLowerCase((char) ch) != string.charAt(i++)) {
                reset(mark);
                return false;
            }
        } while (i < string.length());
        reset(mark);
        return true;
    }

    /**
     * search the stream for a match to a string
     * @param string The string to match
     * @return <strong>true</strong> is one is found, the current position
     *         in stream is positioned after the search string, <strong>
     *	       false</strong> otherwise, position in stream unchanged.
     */
    boolean matches(String string) throws JasperException {
        Mark mark = mark();
        int ch = 0;
        int i = 0;
        do {
            ch = nextChar();
            if (((char) ch) != string.charAt(i++)) {
                reset(mark);
                return false;
            }
        } while (i < string.length());
        return true;
    }

    boolean matchesETag(String tagName) throws JasperException {
        Mark mark = mark();
        if (!matches("</" + tagName)) return false;
        skipSpaces();
        if (nextChar() == '>') return true;
        reset(mark);
        return false;
    }

    boolean matchesETagWithoutLessThan(String tagName) throws JasperException {
        Mark mark = mark();
        if (!matches("/" + tagName)) return false;
        skipSpaces();
        if (nextChar() == '>') return true;
        reset(mark);
        return false;
    }

    /**
     * Looks ahead to see if there are optional spaces followed by
     * the given String.  If so, true is returned and those spaces and
     * characters are skipped.  If not, false is returned and the
     * position is restored to where we were before.
     */
    boolean matchesOptionalSpacesFollowedBy(String s) throws JasperException {
        Mark mark = mark();
        skipSpaces();
        boolean result = matches(s);
        if (!result) {
            reset(mark);
        }
        return result;
    }

    int skipSpaces() throws JasperException {
        int i = 0;
        while (hasMoreInput() && isSpace()) {
            i++;
            nextChar();
        }
        return i;
    }

    /**
     * Skip until the given string is matched in the stream.
     * When returned, the context is positioned past the end of the match.
     *
     * @param s The String to match.
     * @return A non-null <code>Mark</code> instance (positioned immediately
     *         before the search string) if found, <strong>null</strong>
     *         otherwise.
     */
    Mark skipUntil(String limit) throws JasperException {
        Mark ret = null;
        int limlen = limit.length();
        int ch;
        skip: for (ret = mark(), ch = nextChar(); ch != -1; ret = mark(), ch = nextChar()) {
            if (ch == limit.charAt(0)) {
                Mark restart = mark();
                for (int i = 1; i < limlen; i++) {
                    if (peekChar() == limit.charAt(i)) nextChar(); else {
                        reset(restart);
                        continue skip;
                    }
                }
                return ret;
            }
        }
        return null;
    }

    /**
     * Skip until the given string is matched in the stream, but ignoring
     * chars initially escaped by a '\'.
     * When returned, the context is positioned past the end of the match.
     *
     * @param s The String to match.
     * @return A non-null <code>Mark</code> instance (positioned immediately
     *         before the search string) if found, <strong>null</strong>
     *         otherwise.
     */
    Mark skipUntilIgnoreEsc(String limit) throws JasperException {
        Mark ret = null;
        int limlen = limit.length();
        int ch;
        int prev = 'x';
        skip: for (ret = mark(), ch = nextChar(); ch != -1; ret = mark(), prev = ch, ch = nextChar()) {
            if (ch == '\\' && prev == '\\') {
                ch = 0;
            } else if (ch == limit.charAt(0) && prev != '\\') {
                for (int i = 1; i < limlen; i++) {
                    if (peekChar() == limit.charAt(i)) nextChar(); else continue skip;
                }
                return ret;
            }
        }
        return null;
    }

    /**
     * Skip until the given end tag is matched in the stream.
     * When returned, the context is positioned past the end of the tag.
     *
     * @param tag The name of the tag whose ETag (</tag>) to match.
     * @return A non-null <code>Mark</code> instance (positioned immediately
     *	       before the ETag) if found, <strong>null</strong> otherwise.
     */
    Mark skipUntilETag(String tag) throws JasperException {
        Mark ret = skipUntil("</" + tag);
        if (ret != null) {
            skipSpaces();
            if (nextChar() != '>') ret = null;
        }
        return ret;
    }

    final boolean isSpace() {
        return peekChar() <= ' ';
    }

    /**
     * Parse a space delimited token.
     * If quoted the token will consume all characters up to a matching quote,
     * otherwise, it consumes up to the first delimiter character.
     *
     * @param quoted If <strong>true</strong> accept quoted strings.
     */
    String parseToken(boolean quoted) throws JasperException {
        StringBuffer stringBuffer = new StringBuffer();
        skipSpaces();
        stringBuffer.setLength(0);
        int ch = peekChar();
        if (quoted) {
            if (ch == '"' || ch == '\'') {
                char endQuote = ch == '"' ? '"' : '\'';
                ch = nextChar();
                for (ch = nextChar(); ch != -1 && ch != endQuote; ch = nextChar()) {
                    if (ch == '\\') ch = nextChar();
                    stringBuffer.append((char) ch);
                }
                if (ch == -1) {
                    err.jspError(mark(), "jsp.error.quotes.unterminated");
                }
            } else {
                err.jspError(mark(), "jsp.error.attr.quoted");
            }
        } else {
            if (!isDelimiter()) {
                do {
                    ch = nextChar();
                    if (ch == '\\') {
                        if (peekChar() == '"' || peekChar() == '\'' || peekChar() == '>' || peekChar() == '%') ch = nextChar();
                    }
                    stringBuffer.append((char) ch);
                } while (!isDelimiter());
            }
        }
        return stringBuffer.toString();
    }

    void setSingleFile(boolean val) {
        singleFile = val;
    }

    /**
     * Gets the URL for the given path name.
     *
     * @param path Path name
     *
     * @return URL for the given path name.
     *
     * @exception MalformedURLException if the path name is not given in 
     * the correct form
     */
    URL getResource(String path) throws MalformedURLException {
        return context.getResource(path);
    }

    /**
     * Parse utils - Is current character a token delimiter ?
     * Delimiters are currently defined to be =, &gt;, &lt;, ", and ' or any
     * any space character as defined by <code>isSpace</code>.
     *
     * @return A boolean.
     */
    private boolean isDelimiter() throws JasperException {
        if (!isSpace()) {
            int ch = peekChar();
            if (ch == '=' || ch == '>' || ch == '"' || ch == '\'' || ch == '/') {
                return true;
            }
            if (ch == '-') {
                Mark mark = mark();
                if (((ch = nextChar()) == '>') || ((ch == '-') && (nextChar() == '>'))) {
                    reset(mark);
                    return true;
                } else {
                    reset(mark);
                    return false;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Register a new source file.
     * This method is used to implement file inclusion. Each included file
     * gets a unique identifier (which is the index in the array of source
     * files).
     *
     * @return The index of the now registered file.
     */
    private int registerSourceFile(String file) {
        if (sourceFiles.contains(file)) return -1;
        sourceFiles.addElement(file);
        this.size++;
        return sourceFiles.size() - 1;
    }

    /**
     * Unregister the source file.
     * This method is used to implement file inclusion. Each included file
     * gets a uniq identifier (which is the index in the array of source
     * files).
     *
     * @return The index of the now registered file.
     */
    private int unregisterSourceFile(String file) {
        if (!sourceFiles.contains(file)) return -1;
        sourceFiles.removeElement(file);
        this.size--;
        return sourceFiles.size() - 1;
    }

    /**
     * Push a file (and its associated Stream) on the file stack.  THe
     * current position in the current file is remembered.
     */
    private void pushFile(String file, String encoding, InputStreamReader reader) throws JasperException, FileNotFoundException {
        String longName = file;
        int fileid = registerSourceFile(longName);
        if (fileid == -1) {
            err.jspError("jsp.error.file.already.registered", file);
        }
        currFileId = fileid;
        try {
            CharArrayWriter caw = new CharArrayWriter();
            char buf[] = new char[1024];
            for (int i = 0; (i = reader.read(buf)) != -1; ) caw.write(buf, 0, i);
            caw.close();
            if (current == null) {
                current = new Mark(this, caw.toCharArray(), fileid, getFile(fileid), master, encoding);
            } else {
                current.pushStream(caw.toCharArray(), fileid, getFile(fileid), longName, encoding);
            }
        } catch (Throwable ex) {
            log.error("Exception parsing file ", ex);
            popFile();
            err.jspError("jsp.error.file.cannot.read", file);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception any) {
                }
            }
        }
    }

    /**
     * Pop a file from the file stack.  The field "current" is retored
     * to the value to point to the previous files, if any, and is set
     * to null otherwise.
     * @return true is there is a previous file on the stck.
     *         false otherwise.
     */
    private boolean popFile() throws JasperException {
        if (current == null || currFileId < 0) {
            return false;
        }
        String fName = getFile(currFileId);
        currFileId = unregisterSourceFile(fName);
        if (currFileId < -1) {
            err.jspError("jsp.error.file.not.registered", fName);
        }
        Mark previous = current.popStream();
        if (previous != null) {
            master = current.baseDir;
            current = previous;
            return true;
        }
        return false;
    }
}

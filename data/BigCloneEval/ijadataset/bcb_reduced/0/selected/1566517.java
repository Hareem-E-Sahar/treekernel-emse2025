package org.makumba.analyser.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.makumba.ProgrammerError;

/**
 * The collection of syntax points in a source file gathered from a source analysis.
 * 
 * @author Cristian Bogdan
 * @version $Id: SourceSyntaxPoints.java 2251 2008-04-16 16:37:59Z manuelbernhardt $
 */
public class SourceSyntaxPoints {

    static interface PreprocessorClient {

        public void treatInclude(int position, String includeDirective, SyntaxPoint start, SyntaxPoint end, SourceSyntaxPoints host);

        public Pattern[] getCommentPatterns();

        public String[] getCommentPatternNames();

        public Pattern[] getLiteralPatterns();

        public String[] getLiteralPatternNames();

        public Pattern getIncludePattern();

        public String getIncludePatternName();
    }

    /** The path of the analyzed file */
    File file;

    public String toString() {
        return file.toString() + " " + offset;
    }

    PreprocessorClient client;

    /** The timestamp of the analyzed file. If it is found newer on disk, the cached object is discarded. */
    long lastChanged;

    /** The syntax points, sorted */
    TreeSet<SyntaxPoint> syntaxPoints = new TreeSet<SyntaxPoint>();

    /** The line beginnings, added in occuring order */
    ArrayList<SyntaxPoint> lineBeginnings = new ArrayList<SyntaxPoint>();

    /** The file beginnings, added in occuring order. When file F includes file I, I begins, then F begins again */
    ArrayList<Integer> fileBeginningIndexes = new ArrayList<Integer>();

    ArrayList<SourceSyntaxPoints> fileBeginnings = new ArrayList<SourceSyntaxPoints>();

    /** The original text */
    String originalText;

    /** The content, where comments are replaced by whitespace and include directives are replaced by included text */
    String content;

    /** offset in the including file */
    int offset;

    /** the parent, in which we are included */
    SourceSyntaxPoints parent;

    /**
     * The constructor inserts syntax points (begin and end) for every line in a text, and does preprocessing
     * (uncomments text, includes other text). Most syntax colourers need to do specific operations at every line.
     * 
     * @param f
     *            the parsed file
     * @param cl
     *            the preprocessor
     */
    public SourceSyntaxPoints(File f, PreprocessorClient cl) {
        this(f, cl, null, null, 0);
    }

    /**
     * The constructor inserts syntax points (begin and end) for every line in a text, and does preprocessing
     * (uncomments text, includes other text). Most syntax colourers need to do specific operations at every line.
     * 
     * @param f
     *            the parsed file
     * @param cl
     *            the preprocessor
     * @param parent
     *            the parent in which we are included
     * @param includeDirective
     *            the include directive
     * @param offset
     *            the offset at which the inclusion takes place
     */
    public SourceSyntaxPoints(File f, PreprocessorClient cl, SourceSyntaxPoints parent, String includeDirective, int offset) {
        this.offset = offset;
        this.parent = parent;
        file = f;
        client = cl;
        lastChanged = file.lastModified();
        content = originalText = readFile(includeDirective);
        fileBeginningIndexes.add(new Integer(0));
        fileBeginnings.add(this);
        findLineBreaks();
        if (client.getLiteralPatterns() != null) {
            for (int i = 0; i < client.getLiteralPatterns().length; i++) {
                treatLiterals(i);
            }
        }
        if (client.getCommentPatterns() != null) {
            for (int i = 0; i < client.getCommentPatterns().length; i++) {
                unComment(i);
            }
        }
        if (client.getIncludePattern() != null) include();
    }

    /**
     * Finds the line breaks in the string
     */
    void findLineBreaks() {
        int start = 0;
        int line = 1;
        int max = originalText.length();
        for (int i = 0; i < max; i++) {
            if (originalText.charAt(i) == '\r') {
                if (i + 1 < max && originalText.charAt(i + 1) == '\n') i++;
            } else if (originalText.charAt(i) != '\n') continue;
            addSyntaxPointsLine(start, i, "TextLine", new Integer(line));
            start = i + 1;
            line++;
        }
        if (start < max) addSyntaxPointsLine(start, max, "TextLine", new Integer(line));
    }

    /**
     * Gets the text of the line n.
     * This will not work after discardSyntaxPoints because both the text of the file
     * and the syntax points are gone, to spare memory.
     * 
     * @param n
     *            the line number
     * @return A String containing the text at the indicated line
     */
    public String getLineText(int n) {
        SyntaxPoint line = (SyntaxPoint) lineBeginnings.get(n - 1);
        if (n == lineBeginnings.size()) return originalText.substring(line.getOriginalPosition());
        SyntaxPoint nextline = (SyntaxPoint) lineBeginnings.get(n);
        return originalText.substring(line.getOriginalPosition(), nextline.getOriginalPosition() - 1);
    }

    /**
     * Includes a file into the current content
     */
    void include() {
        while (true) {
            Matcher m = client.getIncludePattern().matcher(content);
            if (!m.find()) return;
            SyntaxPoint end = addSyntaxPoints(m.start() + offset, m.end() + offset, "JSPIncludeDirective", content.substring(m.start(), m.end()));
            SyntaxPoint start = (SyntaxPoint) end.getOtherInfo();
            client.treatInclude(m.start(), content.substring(m.start(), m.end()), start, end, this);
        }
    }

    /**
     * Includes the given file, at the given position, included by the given directive
     * 
     * @param f
     *            the file to be included
     * @param position
     *            the position of the included file
     * @param includeDirective
     *            the directive calling for the inclusion
     */
    public void include(File f, int position, String includeDirective) {
        SourceSyntaxPoints sf = new SourceSyntaxPoints(f, client, this, includeDirective, position + offset);
        int delta = sf.getContent().length() - includeDirective.length();
        StringBuffer sb = new StringBuffer();
        sb.append(content.substring(0, position)).append(sf.getContent()).append(content.substring(position + includeDirective.length()));
        content = sb.toString();
        for (Iterator<SyntaxPoint> i = syntaxPoints.iterator(); i.hasNext(); ) {
            SyntaxPoint sp = i.next();
            if (sp.position > position + offset && !sp.getType().equals("JSPIncludeDirective")) sp.moveByInclude(delta);
        }
        int n = fileBeginningIndexes.size() - 1;
        if (((Integer) fileBeginningIndexes.get(n)).intValue() == position) fileBeginnings.set(n, sf); else {
            fileBeginningIndexes.add(position);
            fileBeginnings.add(sf);
        }
        fileBeginningIndexes.add(new Integer(position + delta));
        fileBeginnings.add(this);
    }

    /**
     * Treats comments, to be specific creates a syntax point for them and then replaces their content.
     * 
     * @param patternIndex
     *            the index at which the comment is stored
     */
    void unComment(int patternIndex) {
        unComment(client.getCommentPatterns()[patternIndex], client.getCommentPatternNames()[patternIndex]);
    }

    /**
     * Replaces comments or literals from a text by blanks, and stores syntax points. The comment or literal is defined
     * by the given pattern and pattern name. As a result, the text with comments is replaced by blanks, of equal length
     * as the input.
     * 
     * @param pattern
     *            the pattern to match the literal or comment
     * @param patternName
     *            the name of the pattern.
     */
    private void unComment(Pattern pattern, String patternName) {
        Matcher m = pattern.matcher(content);
        int endOfLast = 0;
        StringBuffer uncommentedContent = new StringBuffer();
        while (m.find()) {
            uncommentedContent.append(content.substring(endOfLast, m.start()));
            for (int i = m.start(); i < m.end(); i++) uncommentedContent.append(' ');
            endOfLast = m.end();
            java.util.logging.Logger.getLogger("org.makumba." + "syntaxpoint.comment").fine("UNCOMMENT " + patternName + " : " + m.group());
            addSyntaxPoints(m.start() + offset, m.end() + offset, patternName, null);
        }
        uncommentedContent.append(content.substring(endOfLast));
        content = uncommentedContent.toString();
    }

    /**
     * Treat literals, to be specific creates a syntax point for them and then replaces their content.
     * 
     * @param patternIndex
     *            the index at which the literal is stored
     */
    void treatLiterals(int patternIndex) {
        unComment(client.getLiteralPatterns()[patternIndex], client.getLiteralPatternNames()[patternIndex]);
    }

    /**
     * Creates a beginning and end syntaxPoint for a syntax entity, and adds these to the collection of points.
     * 
     * @param start
     *            the starting position
     * @param end
     *            the end position
     * @param type
     *            String stating the type of syntax point
     * @param extra
     *            any extra info (for example the object created at the syntax point
     * @see #addSyntaxPointsCommon(int start, int end, String type, Object extra)
     */
    public SyntaxPoint.End addSyntaxPoints(int start, int end, String type, Object extra) {
        SourceSyntaxPoints ssp = findSourceFile(start);
        if (ssp == this) return addSyntaxPoints1(start, end, type, extra); else return ssp.addSyntaxPoints(start, end, type, extra);
    }

    SyntaxPoint.End addSyntaxPoints1(int start, int end, String type, Object extra) {
        SyntaxPoint.End e = addSyntaxPointsCommon(start, end, type, extra);
        setLineAndColumn(e);
        setLineAndColumn((SyntaxPoint) e.getOtherInfo());
        return e;
    }

    /**
     * Fills in the Line and Column for the given SyntaxPoint, based on the collection of lineBeginnings syntaxPoints.
     * 
     * @param point
     *            the syntax point to be filled in
     */
    void setLineAndColumn(SyntaxPoint point) {
        SyntaxPoint lineBegin = (SyntaxPoint) lineBeginnings.get((-1) * Collections.binarySearch(lineBeginnings, point) - 2);
        point.line = lineBegin.line;
        point.column = point.position - lineBegin.position + 1;
        point.sourceFile = this;
    }

    /**
     * Finds the source file that contains the given syntax point
     * 
     * @param position
     *            position of the syntax point
     */
    SourceSyntaxPoints findSourceFile(int position) {
        int index = Collections.binarySearch(fileBeginningIndexes, new Integer(position - offset));
        if (index < 0) index = -index - 2;
        return (SourceSyntaxPoints) fileBeginnings.get(index);
    }

    /**
     * Creates begin- and end- syntaxpoints (but without setting the line and column fields) at given location and with
     * given info, and adds them to the collection.
     * 
     * @param start
     *            the starting position
     * @param end
     *            the end position
     * @param type
     *            String stating the type of syntax point
     * @param extra
     *            any extra info (for example the object created at the syntax point
     * 
     * @return the created <tt>SyntaxPoint.End</tt>
     * @see #addSyntaxPoints(int, int, String, Object)
     */
    SyntaxPoint.End addSyntaxPointsCommon(int start, int end, String type, Object extra) {
        final String type1 = type;
        final Object extra1 = extra;
        SyntaxPoint point = new SyntaxPoint(start) {

            public String getType() {
                return type1;
            }

            public Object getOtherInfo() {
                return extra1;
            }
        };
        syntaxPoints.add(point);
        SyntaxPoint.End theEnd = (SyntaxPoint.End) SyntaxPoint.makeEnd(point, end);
        syntaxPoints.add(theEnd);
        return theEnd;
    }

    /**
     * Creates begin- and end- syntaxpoints for a full line in text.
     * 
     * @param start
     *            the starting position
     * @param end
     *            the end position
     * @param type
     *            String stating the type of syntax point
     * @param extra
     *            any extra info (for example the object created at the syntax point
     */
    void addSyntaxPointsLine(int start, int end, String type, Object extra) {
        SyntaxPoint.End e = addSyntaxPointsCommon(start, end, type, extra);
        e.moveByInclude(offset);
        SyntaxPoint lineBegin = (SyntaxPoint) e.getOtherInfo();
        lineBegin.moveByInclude(offset);
        lineBegin.line = e.line = ((Integer) lineBegin.getOtherInfo()).intValue();
        lineBegin.column = 1;
        e.column = end - start + 1;
        e.sourceFile = lineBegin.sourceFile = this;
        lineBeginnings.add(lineBegin);
    }

    /**
     * Checks if the file changed on the disk since it was last analysed.
     * 
     * @return <code>false</code> if unchanged, <code>true</code> otherwise
     */
    boolean unchanged() {
        if (file.lastModified() != lastChanged) return false;
        for (Iterator<SourceSyntaxPoints> i = fileBeginnings.iterator(); i.hasNext(); ) {
            SourceSyntaxPoints ss = i.next();
            if (ss != this && !ss.unchanged()) return false;
        }
        return true;
    }

    /**
     * Reads the content of the JSP file into a string.
     * 
     * @param includeDirective
     *            the directive by which this file has been included
     * @return A String containing a JSP file
     */
    String readFile(String includeDirective) {
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader rd = new BufferedReader(new FileReader(file));
            char[] buffer = new char[2048];
            int n;
            while ((n = rd.read(buffer)) != -1) sb.append(buffer, 0, n);
        } catch (FileNotFoundException e) {
            String msg = "File '" + file.getName() + "' not found.\n\t(" + e.getMessage() + ")";
            if (includeDirective != null) {
                msg = "Error in include directive:\n\n" + includeDirective + "\n\n" + msg;
            } else {
                msg = "Error in reading a file: " + msg;
            }
            throw new ProgrammerError(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    String getContent() {
        return content;
    }

    /**
     * Returns the syntaxPoints.
     * 
     * @return An array of SyntaxPoints
     */
    public SyntaxPoint[] getSyntaxPoints() {
        ArrayList<SyntaxPoint> list = new ArrayList<SyntaxPoint>(syntaxPoints);
        Collections.sort(list);
        SyntaxPoint[] result = (SyntaxPoint[]) list.toArray(new SyntaxPoint[syntaxPoints.size()]);
        for (int i = 0; i + 1 < result.length; i++) {
            if (result[i].getType().equals("TextLine") && result[i + 1].getType().equals("TextLine") && result[i].getLine() == result[i + 1].getLine() && result[i].getColumn() == result[i + 1].getColumn() && !result[i].isBegin() && result[i + 1].isBegin()) {
                SyntaxPoint temp = result[i];
                result[i] = result[i + 1];
                result[i + 1] = temp;
            }
        }
        return result;
    }

    public void discardPoints() {
        for (Iterator<SourceSyntaxPoints> i = fileBeginnings.iterator(); i.hasNext(); ) {
            SourceSyntaxPoints s = i.next();
            if (s != this) s.discardPoints();
        }
        content = originalText = null;
        fileBeginningIndexes = null;
        lineBeginnings = null;
        syntaxPoints = null;
    }

    public File getFile() {
        return file;
    }
}

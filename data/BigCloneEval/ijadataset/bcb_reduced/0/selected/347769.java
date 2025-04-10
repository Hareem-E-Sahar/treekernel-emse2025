package org.makumba.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
 * The collection of syntax points in a source file gathered from a source analysis.
 */
public class SourceSyntaxPoints {

    static interface PreprocessorClient {

        public void treatInclude(int position, String includeDirective, SourceSyntaxPoints host);

        public Pattern[] getCommentPatterns();

        public String[] getCommentPatternNames();

        public Pattern getIncludePattern();

        public String getIncludePatternName();
    }

    /** The path of the analyzed file */
    File file;

    public String toString() {
        return file.toString() + " " + offset;
    }

    PreprocessorClient client;

    /** The timestamp of the analyzed file. If the it is found newer on disk, the cached object is discarded. */
    long lastChanged;

    /** The syntax points, sorted */
    TreeSet syntaxPoints = new TreeSet();

    /** The line beginnings, added in occuring order */
    ArrayList lineBeginnings = new ArrayList();

    /** The file beginnings, added in occuring order. When file F includes file I, I begins, then F begins again */
    ArrayList fileBeginningIndexes = new ArrayList();

    ArrayList fileBeginnings = new ArrayList();

    /** The original text */
    String originalText;

    /** The content, where comments are replaced by whitespace and include directives are replaced by included text */
    String content;

    /** offset in the including file */
    int offset;

    /** the parent, in which we are included */
    SourceSyntaxPoints parent;

    /** 
   * The constructor inserts syntax points (begin and end) for every line in a text, and does preprocessing (uncomments text, includes other text). 
   * Most syntax colourers need to do specific operations at every line 
   */
    public SourceSyntaxPoints(File f, PreprocessorClient cl) {
        this(f, cl, null, 0);
    }

    public SourceSyntaxPoints(File f, PreprocessorClient cl, SourceSyntaxPoints parent, int offset) {
        this.offset = offset;
        this.parent = parent;
        file = f;
        client = cl;
        lastChanged = file.lastModified();
        content = originalText = readFile();
        fileBeginningIndexes.add(new Integer(0));
        fileBeginnings.add(this);
        findLineBreaks();
        for (int i = 0; i < client.getCommentPatterns().length; i++) unComment(i);
        if (client.getIncludePattern() != null) include();
    }

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

    /** get the text of the line n */
    public String getLineText(int n) {
        SyntaxPoint line = (SyntaxPoint) lineBeginnings.get(n - 1);
        if (n == lineBeginnings.size()) return originalText.substring(line.getOriginalPosition());
        SyntaxPoint nextline = (SyntaxPoint) lineBeginnings.get(n);
        return originalText.substring(line.getOriginalPosition(), nextline.getOriginalPosition() - 1);
    }

    void include() {
        while (true) {
            Matcher m = client.getIncludePattern().matcher(content);
            if (!m.find()) return;
            client.treatInclude(m.start(), content.substring(m.start(), m.end()), this);
        }
    }

    /** include the given file, at the given position, included by the given directive */
    public void include(File f, int position, String includeDirective) {
        SourceSyntaxPoints sf = new SourceSyntaxPoints(f, client, this, position);
        int delta = sf.getContent().length() - includeDirective.length();
        StringBuffer sb = new StringBuffer();
        sb.append(content.substring(0, position)).append(sf.getContent()).append(content.substring(position + includeDirective.length()));
        content = sb.toString();
        for (Iterator i = syntaxPoints.iterator(); i.hasNext(); ) {
            SyntaxPoint sp = (SyntaxPoint) i.next();
            if (sp.position > position) sp.moveByInclude(delta);
        }
        int n = fileBeginningIndexes.size() - 1;
        if (((Integer) fileBeginningIndexes.get(n)).intValue() == position) fileBeginnings.set(n, sf); else {
            fileBeginningIndexes.add(new Integer(position));
            fileBeginnings.add(sf);
        }
        fileBeginningIndexes.add(new Integer(position + delta));
        fileBeginnings.add(this);
    }

    /** Replaces comments from a text by blanks, and stores syntax points. Comment is defined by a Pattern. 
   * @return The text with comments replaced by blanks, of equal length as the input.
   */
    void unComment(int patternIndex) {
        Matcher m = client.getCommentPatterns()[patternIndex].matcher(content);
        int endOfLast = 0;
        StringBuffer uncommentedContent = new StringBuffer();
        while (m.find()) {
            uncommentedContent.append(content.substring(endOfLast, m.start()));
            for (int i = m.start(); i < m.end(); i++) uncommentedContent.append(' ');
            endOfLast = m.end();
            org.makumba.MakumbaSystem.getMakumbaLogger("syntaxpoint.comment").fine("UNCOMMENT " + client.getCommentPatternNames()[patternIndex] + " : " + m.group());
            addSyntaxPoints(m.start() + offset, m.end() + offset, client.getCommentPatternNames()[patternIndex], null);
        }
        uncommentedContent.append(content.substring(endOfLast));
        content = uncommentedContent.toString();
    }

    /** Creates a beginning and end syntaxPoint for a syntax entity, and adds these to the collection of points.
   * @param start  the starting position
   * @param end    the end position
   * @param type   String stating the type of syntax point
   * @param extra  any extra info (e.g. the object created at the syntax point 
   * @see   #addSyntaxPointsCommon(int start, int end, String type, Object extra)
   */
    public SyntaxPoint.End addSyntaxPoints(int start, int end, String type, Object extra) {
        return findSourceFile(start).addSyntaxPoints1(start, end, type, extra);
    }

    SyntaxPoint.End addSyntaxPoints1(int start, int end, String type, Object extra) {
        SyntaxPoint.End e = addSyntaxPointsCommon(start, end, type, extra);
        setLineAndColumn(e);
        setLineAndColumn((SyntaxPoint) e.getOtherInfo());
        return e;
    }

    /** Fills in the Line and Column for the given SyntaxPoint, based on the collection of lineBeginnings syntaxPoints. */
    void setLineAndColumn(SyntaxPoint point) {
        SyntaxPoint lineBegin = (SyntaxPoint) lineBeginnings.get((-1) * Collections.binarySearch(lineBeginnings, point) - 2);
        point.line = lineBegin.line;
        point.column = point.position - lineBegin.position + 1;
        point.sourceFile = this;
    }

    /** Find the source file that contains the given syntax point */
    SourceSyntaxPoints findSourceFile(int position) {
        int index = Collections.binarySearch(fileBeginningIndexes, new Integer(position));
        if (index < 0) index = -index - 2;
        return (SourceSyntaxPoints) fileBeginnings.get(index);
    }

    /** 
   * Creates begin- and end- syntaxpoints (but without setting the line and column fields) 
   * at given location and with given info, and adds them to the collection.
   * @return   the created <tt>SyntaxPoint.End</tt>
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
   * @return   the created <tt>SyntaxPoint.End</tt>
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

    /** Is file changed on disk since it was last analysed. */
    boolean unchanged() {
        if (file.lastModified() != lastChanged) return false;
        for (Iterator i = fileBeginnings.iterator(); i.hasNext(); ) {
            SourceSyntaxPoints ss = (SourceSyntaxPoints) i.next();
            if (ss != this && !ss.unchanged()) return false;
        }
        return true;
    }

    /** Return the content of the JSP file in a string. */
    String readFile() {
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader rd = new BufferedReader(new FileReader(file));
            char[] buffer = new char[2048];
            int n;
            while ((n = rd.read(buffer)) != -1) sb.append(buffer, 0, n);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    String getContent() {
        return content;
    }

    /**
     * @return Returns the syntaxPoints.
     */
    public SyntaxPoint[] getSyntaxPoints() {
        ArrayList list = new ArrayList(syntaxPoints);
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
}

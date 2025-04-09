package vqwiki.lex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import vqwiki.Environment;

/**
 * Experimental class. This class may be used in two ways:
 *
 * The static addTableOfContents(String) method may be called to automatically
 * adds a table of contents on the right side of an article.  This method
 * works with all lexers, because it parses the HTML for headers. However it
 * doesn't care where it is. So if you have a header on the TopArea / LeftMenu /
 * BottomArea, it will also add a TOC there...
 *
 * The static addTableOfContents(MakeTableOfContents, StringBuffer) method
 * may be called to insert a pre-built MakeTableOfContents object into an
 * article.  This method requires that the parser has added all table of
 * contents headings to the object and included a TOC_INSERT_TAG at the point
 * where the table of contents should be inserted.  It is a bit more flexible
 * but requires more preperatory work.
 *
 * @author studer
 */
public class MakeTableOfContents {

    private static final Logger logger = Logger.getLogger(MakeTableOfContents.class);

    public static final int STATUS_TOC_UNINITIALIZED = 0;

    public static final int STATUS_TOC_INITIALIZED = 1;

    public static final int STATUS_NO_TOC = 2;

    public static final String TOC_INSERT_TAG = "__INSERT_TOC__";

    private int minLevel = 4;

    private List entries = new ArrayList();

    private Set headeranchors = new HashSet();

    private int status = STATUS_TOC_UNINITIALIZED;

    /**
     * Adds TOC at the beginning as a table on the right side of the page if the
     * page has any HTML-headers.
     *
     * @param text
     * @return
     */
    public static String addTableOfContents(String text, String contentTitle) {
        logger.debug("Start TOC generating...");
        Pattern p = Pattern.compile("<[Hh]([1-6])[^>]*>(.*?)</[Hh][1-6][^>]*>");
        Matcher m = p.matcher(text);
        StringBuffer result = new StringBuffer();
        MakeTableOfContents maketoc = new MakeTableOfContents();
        int position = 0;
        while (m.find()) {
            int level = Integer.parseInt(m.group(1));
            String header = m.group(2);
            String anchor = maketoc.makeAnchor(header);
            result.append(text.substring(position, m.start(2)));
            position = m.start(2);
            result.append("<a class=\"tocheader\" name=\"" + anchor + "\" id=\"" + anchor + "\"></a>");
            result.append(text.substring(position, m.end(2)));
            position = m.end(2);
            maketoc.addEntry(anchor, header, level);
            logger.debug("Adding content: " + header);
        }
        result.append(text.substring(position));
        if ((maketoc.size() >= Environment.getInstance().getTocMinimumHeaders()) && Environment.getInstance().isTocInsert()) {
            return maketoc.toHTML(contentTitle) + result.toString();
        }
        return result.toString();
    }

    /**
     * Insert an existing MakeTableOfContents object into formatted HTML
     * output.
     *
     * @param toc A pre-built MakeTableOfContents object.
     * @param contents The Wiki syntax, which should contain TOC_INSERT_TAG at
     *  the point where the table of contents object is to be inserted.
     * @return The formatted content containing the table of contents.
     */
    public static StringBuffer addTableOfContents(MakeTableOfContents toc, StringBuffer contents, String contentTitle) {
        int pos = contents.indexOf(MakeTableOfContents.TOC_INSERT_TAG);
        if (pos >= 0) {
            if (toc == null || toc.size() <= Environment.getInstance().getTocMinimumHeaders() || toc.getStatus() == MakeTableOfContents.STATUS_NO_TOC || !Environment.getInstance().isTocInsert()) {
                contents.delete(pos, pos + MakeTableOfContents.TOC_INSERT_TAG.length());
            } else {
                contents.replace(pos, pos + MakeTableOfContents.TOC_INSERT_TAG.length(), toc.toHTML(contentTitle));
            }
        }
        return contents;
    }

    /**
     * Add a new table of contents entry.
     *
     * @param anchor The name of the entry, to be used in the anchor tag name.
     * @param text The text to display for the table of contents entry.
     * @param level The level of the entry.  If an entry is a sub-heading of
     *  another entry the value should be 2.  If there is a sub-heading of that
     *  entry then its value would be 3, and so forth.
     */
    public void addEntry(String anchor, String text, int level) {
        if (this.status != STATUS_NO_TOC) this.status = STATUS_TOC_INITIALIZED;
        TableOfContentsEntry entry = new TableOfContentsEntry(anchor, text, level);
        entries.add(entry);
        if (level < minLevel) minLevel = level;
    }

    /**
     * Return the current table of contents status, such as "no table of contents
     * allowed" or "uninitialized".
     */
    public int getStatus() {
        return this.status;
    }

    /**
     * Set the current table of contents status, such as "no table of contents
     * allowed" or "uninitialized".
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Return the number of entries in this TOC object.
     */
    public int size() {
        return this.entries.size();
    }

    /**
     * Return an HTML representation of this table of contents object.
     */
    public String toHTML(String contenttitle) {
        int activelevel = 1;
        Iterator i = entries.iterator();
        StringBuffer text = new StringBuffer();
        text.append("<table class=\"toctable\">");
        text.append("<tr><th>");
        text.append(contenttitle);
        text.append("</th></tr><tr><td><ul>");
        TableOfContentsEntry entry = null;
        while (i.hasNext()) {
            entry = (TableOfContentsEntry) i.next();
            activelevel = activelevel - entry.level;
            if (activelevel > 0) {
                for (; activelevel != 0; activelevel--) {
                    text.append("</ul>");
                }
            } else if (activelevel < 0) {
                for (; activelevel != 0; activelevel++) {
                    text.append("<ul>");
                }
            }
            activelevel = entry.level;
            text.append("<li class=\"toclevel-" + entry.level + "\">");
            text.append("<a href=\"#").append(entry.anchor).append("\">");
            text.append(entry.text);
            text.append("</a>");
            text.append("</li>");
        }
        for (; activelevel != 0; activelevel--) {
            text.append("</ul>");
        }
        text.append("</td></tr></table>");
        return text.toString();
    }

    /**
     * creates a anchor text based on some other text; is unique in the case of this TOC
     * @param headertext
     * @return
     */
    public String makeAnchor(String headertext) {
        headertext = headertext.trim().replaceAll("(<[^>]*>)|[ &<>]", "_");
        while (headeranchors.contains(headertext)) {
            headertext = "_" + headertext;
        }
        headeranchors.add(headertext);
        return headertext;
    }

    /**
     * Inner class holds TOC entries until they can be processed for display.
     */
    class TableOfContentsEntry {

        int level;

        String anchor;

        String text;

        /**
         * Create a "table of contents" entry
         * @param anchor  name of the anchor
         * @param text    descriptive text for TOC entry
         * @param level   level of TOC
         */
        TableOfContentsEntry(String anchor, String text, int level) {
            this.anchor = anchor;
            this.text = text;
            this.level = level;
        }
    }
}

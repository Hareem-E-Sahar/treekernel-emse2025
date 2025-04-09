package org.ignition.blojsom.plugin.textile;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Textile
 * An implementation of the Textism's Textile. See http://www.textism.com/tools/textile
 *
 * Please also see JTextile http://pipthepixie.tripod.com/code/jtextile.html
 *
 * @author Mark Lussier
 * @since blojsom 1.9
 * @version $Id: Textile.java,v 1.6 2003-05-29 04:10:29 czarneckid Exp $
 */
public class Textile implements TextileConstants {

    /**
     * Public Constructor
     */
    public Textile() {
    }

    /**
     * Process a textile formatted string
     *
     * @param content Textile formatted content
     * @return Content converted to HTML
     */
    public String process(String content) {
        content = content.replaceAll(EXP_AMPERSAND, EXP_AMPERSAND_REPLACE);
        content = replace(content, "&gt;", ">");
        content = replace(content, "&lt;", "<");
        content = replace(content, "&amp;", "&");
        content = replace(content, "\r\n", "\n");
        content = replace(content, "\t", "");
        StringBuffer splitBuffer = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(content, "\n", true);
        while (tokenizer.hasMoreTokens()) {
            splitBuffer.append(tokenizer.nextToken().trim());
            splitBuffer.append("\n");
        }
        content = splitBuffer.toString();
        content = content.replaceAll(EXP_DOUBLEQUOTE_MATCH, EXP_DOUBLEQUOTE_REPLACE);
        content = content.replaceAll(EXP_IMAGE_QTAG_MATCH, EXP_IMAGE_QTAG_REPLACE);
        content = content.replaceAll(EXP_HREF_QTAG_MATCH, EXP_HREF_QTAG_REPLACE);
        for (int x = 0; x < EXP_PHRASE_MODIFIER_SOURCETAGS.length; x++) {
            content.replaceAll("(^|\\s|>)" + EXP_PHRASE_MODIFIER_SOURCETAGS[x] + "\\b(.+?)\\b([^\\w\\s]*?)" + EXP_PHRASE_MODIFIER_SOURCETAGS[x] + "([^\\w\\s]{0,2})(\\s|$)?", "$1<" + EXP_PHRASE_MODIFIER_REPLACETAGS[x] + ">$2$3</" + EXP_PHRASE_MODIFIER_REPLACETAGS[x] + ">$4");
        }
        content = content.replaceAll(EXP_EMPHASIS_MATCH, EXP_EMPHASIS_REPLACE);
        content = content.replaceAll(EXP_ITALICS_MATCH, EXP_ITALICS_REPLACE);
        content = content.replaceAll(EXP_SUPERSCRIPT_MATCH, EXP_SUPERSCRIPT_REPLACE);
        content = content.replaceAll(EXP_EOL_DBL_QUOTES, " ");
        String[] glyphMatches = { EXP_SINGLE_CLOSING, EXP_SINGLE_OPENING, EXP_DOUBLE_CLOSING, EXP_DOUBLE_OPENING, EXP_ELLIPSES, EXP_3UPPER_ACCRONYM, EXP_3UPPERCASE_CAPS, EXP_EM_DASH, EXP_EN_DASH, EXP_EN_DECIMAL_DASH, EXP_DIMENSION_SIGN, EXP_TRADEMARK, EXP_REGISTERED, EXP_COPYRIGHT };
        String[] glyphReplacement = { REPLACE_SINGLE_CLOSING, REPLACE_SINGLE_OPENING, REPLACE_DOUBLE_CLOSING, REPLACE_DOUBLE_OPENING, REPLACE_ELLIPSES, REPLACE_3UPPER_ACCRONYM, REPLACE_3UPPERCASE_CAPS, REPLACE_EM_DASH, REPLACE_EN_DASH, REPLACE_EN_DECIMAL_DASH, REPLACE_DIMENSION_SIGN, REPLACE_TRADEMARK, REPLACE_REGISTERED, REPLACE_COPYRIGHT };
        boolean ishtml = Pattern.compile(EXP_ISHTML).matcher(content).find();
        boolean inpreservation = false;
        if (!ishtml) {
            content = arrayReplaceAll(content, glyphMatches, glyphReplacement);
        } else {
            String[] segments = splitContent(EXP_ISHTML, content);
            StringBuffer segmentBuffer = new StringBuffer();
            for (int x = 0; x < segments.length; x++) {
                if (segments[x].toLowerCase().matches(EXP_STARTPRESERVE)) {
                    inpreservation = true;
                } else if (segments[x].toLowerCase().matches(EXP_ENDPRESERVE)) {
                    inpreservation = false;
                }
                if (!Pattern.compile(EXP_ISHTML).matcher(segments[x]).find() && !inpreservation) {
                    segments[x] = arrayReplaceAll(segments[x], glyphMatches, glyphReplacement);
                }
                if (inpreservation) {
                    segments[x] = htmlSpecialChars(segments[x], MODE_ENT_NOQUOTES);
                    segments[x] = replace(segments[x], "&lt;pre&gt;", "<pre>");
                    segments[x] = replace(segments[x], "&lt;code&gt;", "<code>");
                    segments[x] = replace(segments[x], "&lt;notextile&gt;", "<notextile>");
                }
                segmentBuffer.append(segments[x]);
            }
            content = segmentBuffer.toString();
        }
        content = content.replaceAll(EXP_FORCESLINEBREAKS, REPLACE_FORCESLINEBREAK);
        content = replace(content, "l><br />", "l>\n");
        String[] blockMatches = { EXP_BULLETED_LIST, EXP_NUMERIC_LIST, EXP_BLOCKQUOTE, EXP_HEADER_WITHCLASS, EXP_HEADER, EXP_PARA_WITHCLASS, EXP_PARA, EXP_REMAINING_PARA };
        String[] blockReplace = { REPLACE_BULLETED_LIST, REPLACE_NUMERIC_LIST, REPLACE_BLOCKQUOTE, REPLACE_HEADER_WITHCLASS, REPLACE_HEADER, REPLACE_PARA_WITHCLASS, REPLACE_PARA, REPLACE_REMAINING_PARA };
        StringBuffer blockBuffer = new StringBuffer();
        String list = "";
        content += " \n";
        boolean inpre = false;
        StringTokenizer blockTokenizer = new StringTokenizer(content, "\n", false);
        while (blockTokenizer.hasMoreTokens()) {
            String line = blockTokenizer.nextToken();
            if (!line.matches("^$")) {
                if (line.toLowerCase().indexOf("<pre>") > -1) {
                    inpre = true;
                }
                if (!inpre) {
                    line = arrayReplaceAll(line, blockMatches, blockReplace);
                }
                if (inpre) {
                    line = replace(line, "<br />", "\n");
                    line = replace(line, "<br/>", "\n");
                }
                if (line.toLowerCase().indexOf("</pre>") > -1) {
                    inpre = false;
                }
                boolean islist = Pattern.compile(EXP_LISTSTART).matcher(line).find();
                boolean islistline = Pattern.compile(EXP_LISTSTART + list).matcher(line).find();
                if (list.length() == 0 && islist) {
                    line = line.replaceAll(EXP_MATCHLIST, REPLACE_MATCHLIST);
                    list = line.substring(2, 3);
                } else if (list.length() > 0 && !islistline) {
                    line = line.replaceAll(EXP_ENDMATCHLIST, "</" + list + REPLACE_ENDMATCHLIST);
                    list = "";
                }
            }
            blockBuffer.append(line);
            blockBuffer.append("\n");
        }
        content = blockBuffer.toString();
        content = content.replaceAll("<\\/?notextile>", "");
        content = content.replaceAll("<(\\/?)li(u|o)>", "<$1li>");
        content = replace(content, "x%x%", "&#38;");
        content = replace(content, "<br />", "<br />\n");
        return content;
    }

    /**
     * An implementation of the PHP htmlspecialchars()
     *
     * @param content Source string
     * @param mode Mode to select replacement string for quotes
     *
     * @return String with replace occurrences
     */
    private String htmlSpecialChars(String content, int mode) {
        content = replace(content, "&", "&amp;");
        if (mode != MODE_ENT_NOQUOTES) {
            content = replace(content, "\"", "&quot;");
        }
        if (mode == MODE_ENT_QUOTES) {
            content = replace(content, "'", "&#039;");
        }
        content = replace(content, "<", "&lt;");
        content = replace(content, ">", "&gt;");
        return content;
    }

    /**
     * Splits a string into a string array based on a matching regex
     *
     * @param matchexp Expression to match
     * @param content Content to split
     * @return String array of split content
     */
    private String[] splitContent(String matchexp, String content) {
        int startAt = 0;
        List tempList = new ArrayList();
        Pattern pattern = Pattern.compile(matchexp);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            tempList.add(content.substring(startAt, matcher.start()));
            tempList.add(matcher.group());
            startAt = matcher.end();
        }
        tempList.add(content.substring(startAt));
        String[] result = new String[tempList.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (String) tempList.get(i);
        }
        return result;
    }

    /**
     * Replace an array of match patterns in a string
     *
     * @param content Source string
     * @param matches Match patterns
     * @param replaces Replacement patterns
     * @return String with replaced occurrences
     */
    private String arrayReplaceAll(String content, String[] matches, String[] replaces) {
        String result = content;
        for (int x = 0; x < matches.length; x++) {
            result = result.replaceAll(matches[x], replaces[x]);
        }
        return result;
    }

    /**
     * Replace any occurances of a string pattern within a string with a different string.
     *
     * @param str The source string.  This is the string that will be searched and have the replacements
     * @param pattern The pattern to look for in str
     * @param replace The string to insert in the place of <i>pattern</i>
     * @return String with replaced occurences
     */
    private static String replace(String str, String pattern, String replace) {
        if (str == null || "".equals(str)) {
            return str;
        }
        if (replace == null) {
            return str;
        }
        if ("".equals(pattern)) {
            return str;
        }
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();
        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e + pattern.length();
        }
        result.append(str.substring(s));
        return result.toString();
    }
}

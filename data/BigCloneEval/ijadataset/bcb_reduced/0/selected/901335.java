package org.fife.ui.rtextarea;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;

/**
 * A singleton class that can perform advanced find/replace operations
 * in an <code>RTextArea</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class SearchEngine {

    /**
	 * Private constructor to prevent instantiation.
	 */
    private SearchEngine() {
    }

    /**
	 * Finds the next instance of the string/regular expression specified
	 * from the caret position.  If a match is found, it is selected in this
	 * text area.
	 *
	 * @param textArea The text area in which to search.
	 * @param text The string literal or regular expression to search for.
	 * @param forward Whether to search forward from the caret position or
	 *        backward from it.
	 * @param matchCase Whether the search should be case-sensitive.
	 * @param wholeWord Whether there should be spaces or tabs on either side
	 *        of the match.
	 * @param regex Whether <code>text</code> is a Java regular expression to
	 *        search for.
	 * @return Whether a match was found (and thus selected).
	 * @throws PatternSyntaxException If <code>regex</code> is
	 *         <code>true</code> but <code>text</code> is not a valid regular
	 *         expression.
	 * @see #replace
	 * @see #regexReplace
	 */
    public static boolean find(JTextArea textArea, String text, boolean forward, boolean matchCase, boolean wholeWord, boolean regex) throws PatternSyntaxException {
        Caret c = textArea.getCaret();
        int start = forward ? Math.max(c.getDot(), c.getMark()) : Math.min(c.getDot(), c.getMark());
        String findIn = getFindInText(textArea, start, forward);
        if (findIn == null || findIn.length() == 0) return false;
        if (regex == false) {
            int pos = getNextMatchPos(text, findIn, forward, matchCase, wholeWord);
            findIn = null;
            if (pos != -1) {
                c.setSelectionVisible(true);
                pos = forward ? start + pos : pos;
                c.setDot(pos);
                c.moveDot(pos + text.length());
                return true;
            }
        } else {
            Point regExPos = getNextMatchPosRegEx(text, findIn, forward, matchCase, wholeWord);
            findIn = null;
            if (regExPos != null) {
                c.setSelectionVisible(true);
                if (forward) {
                    regExPos.translate(start, start);
                }
                c.setDot(regExPos.x);
                c.moveDot(regExPos.y);
                return true;
            }
        }
        return false;
    }

    /**
	 * Returns the text in which to search, as a string.  This is used
	 * internally to grab the smallest buffer possible in which to search.
	 */
    protected static String getFindInText(JTextArea textArea, int start, boolean forward) {
        String findIn = null;
        if (forward) {
            try {
                findIn = textArea.getText(start, textArea.getDocument().getLength() - start);
            } catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        } else {
            try {
                findIn = textArea.getText(0, start + 1);
            } catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
        return findIn;
    }

    /**
	 * This method is called internally by
	 * <code>getNextMatchPosRegExImpl</code> and is used to get the locations
	 * of all regular-expression matches, and possibly their replacement
	 * strings.<p>
	 *
	 * Returns either:
	 * <ul>
	 *   <li>A list of points representing the starting and ending positions
	 *       of all matches returned by the specified matcher, or
	 *   <li>A list of <code>RegExReplaceInfo</code>s describing the matches
	 *       found by the matcher and the replacement strings for each.
	 * </ul>
	 *
	 * If <code>replacement</code> is <code>null</code>, this method call is
	 * assumed to be part of a "find" operation and points are returned.  If
	 * if is non-<code>null</code>, it is assumed to be part of a "replace"
	 * operation and the <code>RegExReplaceInfo</code>s are returned.<p>
	 *
	 * @param m The matcher.
	 * @param replaceStr The string to replace matches with.  This is a
	 *        "template" string and can contain captured group references in
	 *        the form "<code>${digit}</code>".
	 * @return A list of result objects.
	 * @throws IndexOutOfBoundsException If <code>replaceStr</code> references
	 *         an invalid group (less than zero or greater than the number of
	 *         groups matched).
	 */
    protected static List getMatches(Matcher m, String replaceStr) {
        ArrayList matches = new ArrayList();
        while (m.find()) {
            Point loc = new Point(m.start(), m.end());
            if (replaceStr == null) {
                matches.add(loc);
            } else {
                matches.add(new RegExReplaceInfo(m.group(0), loc.x, loc.y, getReplacementText(m, replaceStr)));
            }
        }
        return matches;
    }

    /**
	 * Searches <code>searchIn</code> for an occurance of
	 * <code>searchFor</code> either forwards or backwards, matching
	 * case or not.
	 *
	 * @param searchFor The string to look for.
	 * @param searchIn The string to search in.
	 * @param forward Whether to search forward or backward in
	 *        <code>searchIn</code>.
	 * @param matchCase If <code>true</code>, do a case-sensitive search for
	 *        <code>searchFor</code>.
	 * @param wholeWord If <code>true</code>, <code>searchFor</code>
	 *        occurances embedded in longer words in <code>searchIn</code>
	 *        don't count as matches.
	 * @return The starting position of a match, or <code>-1</code> if no
	 *         match was found.
	 * @see #getNextMatchPosImpl
	 * @see #getNextMatchPosRegEx
	 */
    public static final int getNextMatchPos(String searchFor, String searchIn, boolean forward, boolean matchCase, boolean wholeWord) {
        if (!matchCase) {
            return getNextMatchPosImpl(searchFor.toLowerCase(), searchIn.toLowerCase(), forward, matchCase, wholeWord);
        }
        return getNextMatchPosImpl(searchFor, searchIn, forward, matchCase, wholeWord);
    }

    /**
	 * Actually does the work of matching; assumes searchFor and searchIn
	 * are already upper/lower-cased appropriately.<br>
	 * The reason this method is here is to attempt to speed up
	 * <code>FindInFilesDialog</code>; since it repeatedly calls
	 * this method instead of <code>getNextMatchPos</code>, it gets better
	 * performance as it no longer has to allocate a lower-cased string for
	 * every call.
	 *
	 * @param searchFor The string to search for.
	 * @param searchIn The string to search in.
	 * @param goForward Whether the search is forward or backward.
	 * @param matchCase Whether the search is case-sensitive.
	 * @param wholeWord Whether only whole words should be matched.
	 * @return The location of the next match, or <code>-1</code> if no
	 *         match was found.
	 */
    protected static final int getNextMatchPosImpl(String searchFor, String searchIn, boolean goForward, boolean matchCase, boolean wholeWord) {
        if (wholeWord) {
            int len = searchFor.length();
            int temp = goForward ? 0 : searchIn.length();
            int tempChange = goForward ? 1 : -1;
            while (true) {
                if (goForward) temp = searchIn.indexOf(searchFor, temp); else temp = searchIn.lastIndexOf(searchFor, temp);
                if (temp != -1) {
                    if (isWholeWord(searchIn, temp, len)) {
                        return temp;
                    } else {
                        temp += tempChange;
                        continue;
                    }
                }
                return temp;
            }
        } else {
            return goForward ? searchIn.indexOf(searchFor) : searchIn.lastIndexOf(searchFor);
        }
    }

    /**
	 * Searches <code>searchIn</code> for an occurance of <code>regEx</code>
	 * either forwards or backwards, matching case or not.
	 *
	 * @param regEx The regular expression to look for.
	 * @param searchIn The string to search in.
	 * @param goForward Whether to search forward.  If <code>false</code>,
	 *        search backward.
	 * @param matchCase Whether or not to do a case-sensitive search for
	 *        <code>regEx</code>.
	 * @param wholeWord If <code>true</code>, <code>regEx</code>
	 *        occurances embedded in longer words in <code>searchIn</code>
	 *        don't count as matches.
	 * @return A <code>Point</code> representing the starting and ending
	 *         position of the match, or <code>null</code> if no match was
	 *         found.
	 * @throws PatternSyntaxException If <code>regEx</code> is an invalid
	 *         regular expression.
	 * @see #getNextMatchPos
	 */
    public static Point getNextMatchPosRegEx(String regEx, CharSequence searchIn, boolean goForward, boolean matchCase, boolean wholeWord) {
        return (Point) getNextMatchPosRegExImpl(regEx, searchIn, goForward, matchCase, wholeWord, null);
    }

    /**
	 * Searches <code>searchIn</code> for an occurance of <code>regEx</code>
	 * either forwards or backwards, matching case or not.
	 *
	 * @param regEx The regular expression to look for.
	 * @param searchIn The string to search in.
	 * @param goForward Whether to search forward.  If <code>false</code>,
	 *        search backward.
	 * @param matchCase Whether or not to do a case-sensitive search for
	 *        <code>regEx</code>.
	 * @param wholeWord If <code>true</code>, <code>regEx</code>
	 *        occurances embedded in longer words in <code>searchIn</code>
	 *        don't count as matches.
	 * @param replaceStr The string that will replace the match found (if
	 *        a match is found).  The object returned will contain the
	 *        replacement string with matched groups substituted.  If this
	 *        value is <code>null</code>, it is assumed this call is part of a
	 *        "find" instead of a "replace" operation.
	 * @return If <code>replaceStr</code> is <code>null</code>, a
	 *         <code>Point</code> representing the starting and ending points
	 *         of the match.  If it is non-<code>null</code>, an object with
	 *         information about the match and the morphed string to replace
	 *         it with.  If no match is found, <code>null</code> is returned.
	 * @throws PatternSyntaxException If <code>regEx</code> is an invalid
	 *         regular expression.
	 * @throws IndexOutOfBoundsException If <code>replaceStr</code> references
	 *         an invalid group (less than zero or greater than the number of
	 *         groups matched).
	 * @see #getNextMatchPos
	 */
    protected static Object getNextMatchPosRegExImpl(String regEx, CharSequence searchIn, boolean goForward, boolean matchCase, boolean wholeWord, String replaceStr) {
        int flags = matchCase ? 0 : (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Pattern pattern = Pattern.compile(regEx, flags);
        Matcher m = pattern.matcher(searchIn);
        if (goForward) {
            if (!wholeWord) {
                if (m.find()) {
                    if (replaceStr == null) {
                        return new Point(m.start(), m.end());
                    } else {
                        return new RegExReplaceInfo(m.group(0), m.start(), m.end(), getReplacementText(m, replaceStr));
                    }
                }
            } else {
                while (m.find()) {
                    Point loc = new Point(m.start(), m.end());
                    if (isWholeWord(searchIn, loc.x, loc.y - loc.x)) {
                        if (replaceStr == null) {
                            return loc;
                        } else {
                            return new RegExReplaceInfo(m.group(0), loc.x, loc.y, getReplacementText(m, replaceStr));
                        }
                    }
                }
            }
        } else {
            List matches = getMatches(m, replaceStr);
            if (matches.isEmpty()) return null;
            int pos = matches.size() - 1;
            if (wholeWord == false) {
                if (replaceStr == null) {
                    return matches.get(pos);
                } else {
                    return matches.get(pos);
                }
            }
            while (pos >= 0) {
                Object matchObj = matches.get(pos);
                if (replaceStr == null) {
                    Point loc = (Point) matchObj;
                    if (isWholeWord(searchIn, loc.x, loc.y - loc.x)) {
                        return matchObj;
                    }
                } else {
                    RegExReplaceInfo info = (RegExReplaceInfo) matchObj;
                    int x = info.getStartIndex();
                    int y = info.getEndIndex();
                    if (isWholeWord(searchIn, x, y - x)) {
                        return matchObj;
                    }
                }
                pos--;
            }
        }
        return null;
    }

    /**
	 * Returns information on how to implement a regular expression "replace"
	 * action in the specified text with the specified replacement string.
	 *
	 * @param regEx The regular expression to look for.
	 * @param searchIn The string to search in.
	 * @param goForward Whether to search forward.  If <code>false</code>,
	 *        search backward.
	 * @param matchCase Whether or not to do a case-sensitive search for
	 *        <code>regEx</code>.
	 * @param wholeWord If <code>true</code>, <code>regEx</code> occurrances
	 *        embedded in longer words in <code>searchIn</code> don't count as
	 *        matches.
	 * @param replacement A template for the replacement string (e.g., this
	 *        can contain <code>\t</code> and <code>\n</code> to mean tabs
	 *        and newlines, respectively, as well as group references
	 *        <code>$n</code>).
	 * @return A <code>RegExReplaceInfo</code> object describing how to
	 *         implement the replace.
	 * @throws PatternSyntaxException If <code>regEx</code> is an invalid
	 *         regular expression.
	 * @throws IndexOutOfBoundsException If <code>replacement</code> references
	 *         an invalid group (less than zero or greater than the number of
	 *         groups matched).
	 * @see #getNextMatchPos
	 */
    protected static RegExReplaceInfo getRegExReplaceInfo(String regEx, String searchIn, boolean goForward, boolean matchCase, boolean wholeWord, String replacement) {
        if (replacement == null) {
            replacement = "";
        }
        return (RegExReplaceInfo) getNextMatchPosRegExImpl(regEx, searchIn, goForward, matchCase, wholeWord, replacement);
    }

    /**
	 * Called internally by <code>getMatches()</code>.  This method assumes
	 * that the specified matcher has just found a match, and that you want
	 * to get the string with which to replace that match.
	 *
	 * @param m The matcher.
	 * @param template The template for the replacement string.  For example,
	 *        "<code>foo</code>" would yield the replacement string
	 *        "<code>foo</code>", while "<code>$1 is the greatest</code>"
	 *        would yield different values depending on the value of the first
	 *        captured group in the match.
	 * @return The string to replace the match with.
	 * @throws IndexOutOfBoundsException If <code>template</code> references
	 *         an invalid group (less than zero or greater than the number of
	 *         groups matched).
	 */
    public static String getReplacementText(Matcher m, CharSequence template) {
        int cursor = 0;
        StringBuffer result = new StringBuffer();
        while (cursor < template.length()) {
            char nextChar = template.charAt(cursor);
            if (nextChar == '\\') {
                nextChar = template.charAt(++cursor);
                switch(nextChar) {
                    case 'n':
                        nextChar = '\n';
                        break;
                    case 't':
                        nextChar = '\t';
                        break;
                }
                result.append(nextChar);
                cursor++;
            } else if (nextChar == '$') {
                cursor++;
                int refNum = template.charAt(cursor) - '0';
                if ((refNum < 0) || (refNum > 9)) {
                    throw new IndexOutOfBoundsException("No group " + template.charAt(cursor));
                }
                cursor++;
                boolean done = false;
                while (!done) {
                    if (cursor >= template.length()) {
                        break;
                    }
                    int nextDigit = template.charAt(cursor) - '0';
                    if ((nextDigit < 0) || (nextDigit > 9)) {
                        break;
                    }
                    int newRefNum = (refNum * 10) + nextDigit;
                    if (m.groupCount() < newRefNum) {
                        done = true;
                    } else {
                        refNum = newRefNum;
                        cursor++;
                    }
                }
                if (m.group(refNum) != null) result.append(m.group(refNum));
            } else {
                result.append(nextChar);
                cursor++;
            }
        }
        return result.toString();
    }

    /**
	 * Returns whether the characters on either side of
	 * <code>substr(searchIn,startPos,startPos+searchStringLength)</code>
	 * are whitespace.  While this isn't the best definition of "whole word",
	 * it's the one we're going to use for now.
	 */
    private static final boolean isWholeWord(CharSequence searchIn, int offset, int len) {
        boolean wsBefore, wsAfter;
        try {
            wsBefore = Character.isWhitespace(searchIn.charAt(offset - 1));
        } catch (IndexOutOfBoundsException e) {
            wsBefore = true;
        }
        try {
            wsAfter = Character.isWhitespace(searchIn.charAt(offset + len));
        } catch (IndexOutOfBoundsException e) {
            wsAfter = true;
        }
        return wsBefore && wsAfter;
    }

    /**
	 * Makes the caret's dot and mark the same location so that, for the
	 * next search in the specified direction, a match will be found even
	 * if it was within the original dot and mark's selection.
	 *
	 * @param textArea The text area.
	 * @param forward Whether the search will be forward through the
	 *        document (<code>false</code> means backward).
	 * @return The new dot and mark position.
	 */
    protected static int makeMarkAndDotEqual(JTextArea textArea, boolean forward) {
        Caret c = textArea.getCaret();
        int val = forward ? Math.min(c.getDot(), c.getMark()) : Math.max(c.getDot(), c.getMark());
        c.setDot(val);
        return val;
    }

    /**
	 * Finds the next instance of the regular expression specified from
	 * the caret position.  If a match is found, it is replaced with
	 * the specified replacement string.
	 *
	 * @param textArea The text area in which to search.
	 * @param toFind The regular expression to search for.
	 * @param replaceWith The string to replace the found regex with.
	 * @param forward Whether to search forward from the caret position
	 *        or backward from it.
	 * @param matchCase Whether the search should be case-sensitive.
	 * @param wholeWord Whether there should be spaces or tabs on either
	 *        side of the match.
	 * @return Whether a match was found (and thus replaced).
	 * @throws PatternSyntaxException If <code>toFind</code> is not a
	 *         valid regular expression.
	 * @throws IndexOutOfBoundsException If <code>replaceWith</code> references
	 *         an invalid group (less than zero or greater than the number of
	 *         groups matched).
	 * @see #replace
	 * @see #find
	 */
    protected static boolean regexReplace(JTextArea textArea, String toFind, String replaceWith, boolean forward, boolean matchCase, boolean wholeWord) throws PatternSyntaxException {
        Caret c = textArea.getCaret();
        int start = makeMarkAndDotEqual(textArea, forward);
        String findIn = getFindInText(textArea, start, forward);
        if (findIn == null) return false;
        RegExReplaceInfo info = getRegExReplaceInfo(toFind, findIn, forward, matchCase, wholeWord, replaceWith);
        findIn = null;
        if (info != null) {
            c.setSelectionVisible(true);
            int matchStart = info.getStartIndex();
            int matchEnd = info.getEndIndex();
            if (forward) {
                matchStart += start;
                matchEnd += start;
            }
            c.setDot(matchStart);
            c.moveDot(matchEnd);
            textArea.replaceSelection(info.getReplacement());
            return true;
        }
        return false;
    }

    /**
	 * Finds the next instance of the text/regular expression specified from
	 * the caret position.  If a match is found, it is replaced with the
	 * specified replacement string.
	 *
	 * @param textArea The text area in which to search.
	 * @param toFind The text/regular expression  to search for.
	 * @param replaceWith The string to replace the found text with.
	 * @param forward Whether to search forward from the caret position or
	 *        backward from it.
	 * @param matchCase Whether the search should be case-sensitive.
	 * @param wholeWord Whether there should be spaces or tabs on either
	 *        side of the match.
	 * @param regex Whether or not this is a regular expression search.
	 * @return Whether a match was found (and thus replaced).
	 * @throws PatternSyntaxException If <code>regex</code> is
	 *         <code>true</code> but <code>toFind</code> is not a valid
	 *         regular expression.
	 * @throws IndexOutOfBoundsException If <code>regex</code> is
	 *         <code>true</code> and <code>replaceWith</code> references
	 *         an invalid group (less than zero or greater than the number
	 *         of groups matched).
	 * @see #regexReplace
	 * @see #find
	 */
    public static boolean replace(JTextArea textArea, String toFind, String replaceWith, boolean forward, boolean matchCase, boolean wholeWord, boolean regex) throws PatternSyntaxException {
        if (regex) {
            return regexReplace(textArea, toFind, replaceWith, forward, matchCase, wholeWord);
        }
        makeMarkAndDotEqual(textArea, forward);
        if (find(textArea, toFind, forward, matchCase, wholeWord, false)) {
            textArea.replaceSelection(replaceWith);
            return true;
        }
        return false;
    }

    /**
	 * Replaces all instances of the text/regular expression specified in
	 * the specified document with the specified replacement.
	 *
	 * @param textArea The text area in which to search.
	 * @param toFind The text/regular expression  to search for.
	 * @param replaceWith The string to replace the found text with.
	 * @param matchCase Whether the search should be case-sensitive.
	 * @param wholeWord Whether there should be spaces or tabs on either
	 *        side of the match.
	 * @param regex Whether or not this is a regular expression search.
	 * @return The number of replacements done.
	 * @throws PatternSyntaxException If <code>regex</code> is
	 *         <code>true</code> and <code>toFind</code> is an invalid
	 *         regular expression.
	 * @throws IndexOutOfBoundsException If <code>replaceWith</code> references
	 *         an invalid group (less than zero or greater than the number of
	 *         groups matched).
	 * @see #replace
	 * @see #regexReplace
	 * @see #find
	 */
    public static int replaceAll(JTextArea textArea, String toFind, String replaceWith, boolean matchCase, boolean wholeWord, boolean regex) throws PatternSyntaxException {
        int count = 0;
        if (regex) {
            StringBuffer sb = new StringBuffer();
            String findIn = textArea.getText();
            int lastEnd = 0;
            Pattern p = Pattern.compile(toFind);
            Matcher m = p.matcher(findIn);
            try {
                while (m.find()) {
                    sb.append(findIn.substring(lastEnd, m.start()));
                    sb.append(getReplacementText(m, replaceWith));
                    lastEnd = m.end();
                    count++;
                }
                sb.append(findIn.substring(lastEnd));
                textArea.setText(sb.toString());
            } finally {
                findIn = null;
            }
        } else {
            textArea.setCaretPosition(0);
            while (SearchEngine.find(textArea, toFind, true, matchCase, wholeWord, false)) {
                textArea.replaceSelection(replaceWith);
                count++;
            }
        }
        return count;
    }
}

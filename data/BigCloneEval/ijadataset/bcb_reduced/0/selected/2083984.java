package solowiki.internal;

import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of useful methods for String manipulation. It maps some of
 * Php functions used by the parser.
 * @author Giuseppe Profiti
 */
public class StringUtils {

    /**
     * Fill a Vector with all the matching groups from a regex match.
     * @param e1matcher the regex matcher
     * @param m the Vector to populate
     */
    public static void populateMatches(Matcher e1matcher, Vector<String> m) {
        for (int i = 0; i <= e1matcher.groupCount(); i++) {
            m.add(i, e1matcher.group(i));
        }
    }

    /**
     * Generate a String containing <i>i</i> concatenated substring.
     * @param c the String to repeat
     * @param i the number of occurrences
     * @return the generated String
     */
    public static String repeat(String c, int i) {
        String res = "";
        for (int z = 0; z < i; z++) {
            res += c;
        }
        return res;
    }

    /**
     * Custom implementation of php strspn function.
     * @param s the String to evaluate
     * @param f the CharSewuence to search
     * @return the max lenght of a string containing only f chars
     */
    public static int strspn(String s, CharSequence f) {
        String regex = "";
        int res = 0;
        for (int i = 0; i < f.length(); i++) {
            regex += "|" + f.charAt(i);
        }
        regex = regex.substring(1);
        regex = "(" + regex + ")+";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            if (res < (end - start)) {
                res = (end - start);
            }
        }
        return res;
    }

    public static int strspn(String s, CharSequence f, int start) {
        return strspn(s.substring(start), f);
    }

    /**
     * Like split but keeps the delimiters.
     * lots of code: if the string starts with 
     * delimiter, php preg_split creates the first chunk
     * of data as empty string
     * @param text
     * @param patt
     * @return
     */
    static ArrayList<String> splitWithDelims(String text, Pattern patt) {
        ArrayList<String> arr = new ArrayList();
        Matcher m = patt.matcher(text);
        int begin = 0;
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            if (start != begin) {
                arr.add(text.substring(begin, start));
            } else if (start == 0) {
                arr.add("");
            }
            begin = end;
            arr.add(text.substring(start, end));
        }
        arr.add(text.substring(begin, text.length()));
        return arr;
    }

    /**
     * Replace occurrences of a substring. Can replace the first or all
     * occurrences, can use regex or plain text.
     * @param input the whole String
     * @param find the substring to change
     * @param replacement the new text
     * @param firstonly true in order to change only the first
     * @param isregex true is <i>find</i> is a regex
     * @return the modified string
     */
    public static String myReplace(String input, String find, String replacement, boolean firstonly, boolean isregex) {
        String res = "";
        if (isregex) {
            if (firstonly) {
                res = input.replaceFirst(find, replacement);
            } else {
                res = input.replaceAll(find, replacement);
            }
        } else {
            if (firstonly) {
                int pos = input.indexOf(find);
                res = input.substring(0, pos);
                res += replacement;
                res += input.substring(pos + find.length());
            } else {
                res = input.replace(find, replacement);
            }
        }
        return res;
    }

    /**
     * Finds how many newlines are in the specified text, from the start
     * to the specified position.
     * @param pos the last position
     * @param text the text
     * @return the number of newlines
     */
    public static int newLinesToPos(int pos, String text) {
        int howmany = 0;
        int i = 0;
        while (((i = text.indexOf(System.getProperty("line.separator"), i)) > 0) && (i < pos)) {
            howmany++;
            i++;
        }
        return howmany;
    }

    /**
     * Converts a position in a text to a caret position in a view.
     * @param pos the position in the text
     * @param text the text
     * @return the caret position
     */
    public static int modelToView(int pos, String text) {
        return pos - newLinesToPos(pos, text);
    }

    /**
     * Converts a caret position to a position in the text.
     * @param pos the caret position
     * @param text the text
     * @return the text position
     */
    public static int viewToModel(int pos, String text) {
        return pos + newLinesToPos(pos, text);
    }
}

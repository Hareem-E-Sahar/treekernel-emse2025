package superabbrevs.stdlib;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.TextUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.textarea.JEditTextArea;

/**
 *
 * @author Sune Simonsen
 */
public class Std {

    private String indentString;

    /** Creates a new instance of Std */
    public Std(String indentString) {
        this.indentString = indentString;
    }

    public String firstUp(String s) {
        StringBuffer res = new StringBuffer(s);
        if (0 < res.length()) {
            char first = res.charAt(0);
            res.setCharAt(0, Character.toUpperCase(first));
        }
        return res.toString();
    }

    public String firstDown(String s) {
        StringBuffer res = new StringBuffer(s);
        if (0 < res.length()) {
            char first = res.charAt(0);
            res.setCharAt(0, Character.toLowerCase(first));
        }
        return res.toString();
    }

    public String choose(String s, String regexp, String match, String noMatch) {
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(s);
        return m.matches() ? match : noMatch;
    }

    public String replace(String s, String regexp, String replacement, String noMatch) {
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(s);
        if (m.find()) {
            return m.replaceAll(replacement);
        } else {
            return noMatch;
        }
    }

    public String foreach(String s, String regexp, String replacement) {
        Pattern rp = Pattern.compile("\\$(\\d)");
        Matcher rm = rp.matcher(replacement);
        StringBuffer res = new StringBuffer();
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(s);
        int groupCount = m.groupCount();
        int end = 0;
        while (m.find()) {
            while (rm.find(end)) {
                res.append(replacement.substring(end, rm.start()));
                int g = Integer.parseInt(rm.group(1));
                if (0 <= g && g <= groupCount) {
                    res.append(m.group(g));
                }
                end = rm.end();
            }
            res.append(replacement.substring(end));
            end = 0;
        }
        return res.toString();
    }

    public ArrayList<String> retrieve(String s, String regexp) {
        ArrayList<String> matches = new ArrayList<String>();
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(s);
        while (m.find()) {
            matches.add(s.substring(m.start(), m.end()));
        }
        return matches;
    }

    public String ifTrue(boolean condition, String output) {
        return condition ? output : "";
    }

    public String ifEmpty(String s, String emptyValue) {
        return s.trim().equals("") ? emptyValue : s;
    }

    public String ifNotEmpty(String s, String notEmptyValue) {
        return s.trim().equals("") ? s : notEmptyValue;
    }

    public String ifEquals(String s, String pattern, String match, String noMatch) {
        return s.equals(pattern) ? match : noMatch;
    }

    public String repeat(String s, int times) {
        StringBuffer res = new StringBuffer();
        for (int i = 0; i < times; i++) {
            res.append(s);
        }
        return res.toString();
    }

    public String substring(String s, int from, int length) {
        if (0 <= from && 0 <= length && from + length <= s.length()) {
            return s.substring(from, length);
        } else {
            return "";
        }
    }

    public String substring(String s, int from) {
        if (from < 0) {
            return "";
        } else if (s.length() <= from) {
            return s;
        } else {
            return s.substring(from);
        }
    }

    /**
     * Match the string s against a comma seperated list of words, if s is a unique 
     * prefix of one of the words, the words except the prefix will be returned.
     * 
     * ex. complete("Arr","ArrayList,Hashtable,LinkedList") will return "ayList"
     *
     *@param s the string to check against the words 
     *@param words a comma seperated list of words to complete against
     */
    public String complete(String s, String words) {
        return complete(s, words.split(","));
    }

    /**
     * Match the string s against a comma seperated list of words, if s is a unique 
     * prefix of one of the words, the words except the prefix will be returned.
     *
     * ex.
     * String[] words = {"ArrayList","Hashtable","LinkedList"};
     * complete("Arr",words) will return "ayList"
     *
     *@param s the string to check against the words 
     *@param words a list of words to complete against
     */
    public String complete(String s, String[] words) {
        int longestPrefix = 0;
        int longestPrefixIndex = -1;
        for (int i = 0; i < words.length; i++) {
            if (words[i].length() < s.length()) {
                continue;
            }
            int j = 0;
            while (j < s.length() && s.charAt(j) == words[i].charAt(j)) {
                j++;
            }
            if (j == s.length() && longestPrefix < j) {
                longestPrefix = j;
                longestPrefixIndex = i;
            }
            if (longestPrefix == s.length()) {
                break;
            }
        }
        if (0 < longestPrefix) {
            return words[longestPrefixIndex].substring(longestPrefix);
        } else {
            return "";
        }
    }

    public String indent(String whitespace, String s) {
        return s.replaceAll("\n", "\n" + indentString + whitespace);
    }
}

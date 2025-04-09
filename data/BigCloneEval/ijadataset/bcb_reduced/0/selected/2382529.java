package self.lang;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.zip.*;
import self.text.ReplacementUtils;

public final class StringUtils {

    public static final String EMPTY = "";

    public static final String NEW_LINE;

    public static final int NEW_LINE_SZ;

    private StringUtils() {
    }

    public static String replaceFirst(String line, String oldStr, String newStr) {
        String encOldStr = ReplacementUtils.encodeForRegex(oldStr);
        return line.replaceFirst(encOldStr, newStr);
    }

    public static String replaceLast(String line, String oldStr, String newStr) {
        String ret = line;
        int pos = line.lastIndexOf(oldStr);
        if (pos > -1) {
            StringBuffer assemble = new StringBuffer();
            assemble.append(line.substring(0, pos));
            assemble.append(newStr);
            assemble.append(line.substring(pos + oldStr.length()));
            ret = assemble.toString();
        }
        return ret;
    }

    public static String replace(String line, String oldStr, String newStr) {
        return replace(line, oldStr, newStr, false);
    }

    public static String replace(String line, String oldStr, String newStr, boolean wholeWordsOnly) {
        if (oldStr.equals(newStr)) return line;
        final int OLD_STR_LEN = oldStr.length();
        final int NEW_STR_LEN = newStr.length();
        StringBuffer buildBuff = new StringBuffer(line.length());
        int pos = 0;
        while ((pos = line.indexOf(oldStr, pos)) >= 0) {
            if (wholeWordsOnly) {
                boolean ignore = false;
                if (pos > 0) ignore = Character.isLetterOrDigit(line.charAt(pos - 1));
                if (!ignore) {
                    int lineLen = line.length();
                    if (pos + OLD_STR_LEN < lineLen) ignore = Character.isLetterOrDigit(line.charAt(pos + OLD_STR_LEN));
                }
                if (ignore) {
                    pos++;
                    continue;
                }
            }
            buildBuff.append(line.substring(0, pos)).append(newStr).append(line.substring(pos + OLD_STR_LEN));
            line = buildBuff.toString();
            buildBuff.setLength(0);
            pos += NEW_STR_LEN;
        }
        return line;
    }

    public static String toEmptyIfNull(String s) {
        if (s == null) return EMPTY;
        return s;
    }

    public static boolean isNullOrEmpty(String s) {
        if (s == null) return true; else return (s.equals(EMPTY));
    }

    public static String toNullOrNonEmptyValue(String s) {
        return isNullOrEmpty(s) ? null : s;
    }

    public static boolean isSameWithNullOrEmpty(String s1, String s2) {
        if (s1 == s2) return true;
        String s1Chgd = toEmptyIfNull(s1);
        String s2Chgd = toEmptyIfNull(s2);
        return s1Chgd.equals(s2Chgd);
    }

    public static int skipWhiteSpaceCharacters(String srch, int startAt) {
        int len = srch.length();
        while (startAt < len) {
            char toChk = srch.charAt(startAt++);
            if (!Character.isWhitespace(toChk)) return startAt - 1;
        }
        return len;
    }

    public static int compareToIgnoreCase(String p1, String p2) {
        String i1 = p1.toLowerCase();
        String i2 = p2.toLowerCase();
        return i1.compareTo(i2);
    }

    public static class IgnoreCaseComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            return compareToIgnoreCase((String) o1, (String) o2);
        }

        public boolean equals(Object obj) {
            return false;
        }
    }

    public static int getLastNonReturnCharacterPosition(String src) {
        int len = src.length();
        int lastRealChar = len - 1;
        while (true) {
            if (lastRealChar == -1) break;
            char curr = src.charAt(lastRealChar);
            if ((curr == '\r') || (curr == '\n')) {
                lastRealChar--;
                continue;
            }
            break;
        }
        return lastRealChar;
    }

    public static int getFirstNonReturnCharacterPosition(String src) {
        int len = src.length();
        int firstRealChar = 0;
        while (true) {
            if (firstRealChar == len) return -1;
            char curr = src.charAt(firstRealChar);
            if ((curr == '\r') || (curr == '\n')) {
                firstRealChar++;
                continue;
            }
            break;
        }
        return firstRealChar;
    }

    /**
   * 
   * @param src - non-null source string
   * @param enc - encoding to use when identifying the size (null value will use "UTF-8") 
   * @return the byte length of the string using the encoding
   */
    public static long getByteLength(String src, String enc) {
        if (src == null) return 0;
        try {
            if (enc == null) enc = "UTF-8";
            return src.getBytes(enc).length;
        } catch (UnsupportedEncodingException err) {
            throw new IllegalArgumentException(err.getMessage());
        }
    }

    public static long textToInt(String txt) {
        CRC32 crc = new CRC32();
        crc.update(txt.getBytes());
        return crc.getValue();
    }

    static {
        NEW_LINE = System.getProperty("line.separator");
        NEW_LINE_SZ = NEW_LINE.length();
    }
}

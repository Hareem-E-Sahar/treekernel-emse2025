package cc.sprite;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Random;

/**
 * Contains usefull static functions. 
 * 
 * @author Joe Mayer
 */
public class WUtil {

    /** The sequence counter used by the nextSeq method. */
    private static int sequenceCounter = 0x1fffffff & (int) System.nanoTime();

    /** The random number generator used by then nextRandomSeq method. */
    private static Random randomGenerator = new Random();

    /** Generates a random number in the range 1..2^29-1. 
   * @return The next random 29-bit non-zero integer. */
    public static int nextRandomSeq() {
        return 1 + randomGenerator.nextInt(0x1fffffff);
    }

    /**
   * Parse a string value into a boolean value.  Ignores
   * leading and trailing spaces.
   * @param v The value to parse.
   * @return false if v is null, empty, "no", "false", "0", true otherwise.
   */
    public static boolean parseBoolean(String v) {
        if (v == null) {
            return false;
        }
        v = v.trim();
        if (v.length() == 0 || v.equals("no") || v.equals("false") || v.equals("0")) {
            return false;
        }
        return true;
    }

    /**
   * Set a field on an object.  The field must be an int, double, or string.
   * @param target The target object.
   * @param prop   The name of the field to set.
   * @param value  The value to set.
   * @return true if the value was set.
   */
    public static boolean set(Object target, String prop, String value) {
        try {
            Class c = target.getClass();
            Field f = c.getField(prop);
            Class t = f.getType();
            if (t == String.class) {
                f.set(target, value);
            } else if (t == Integer.TYPE) {
                int i = Integer.parseInt(value);
                f.setInt(target, i);
            } else if (t == Double.TYPE) {
                double d = Double.parseDouble(value);
                f.setDouble(target, d);
            } else {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
   * Allocate a new array of the same component type but a
   * different length.  Copy values from the original array
   * into the new one.
   * @param source  The source array.
   * @param newLength The length of the new array.
   * @return The new array.
   */
    public static Object resizeArray(Object source, int newLength) {
        Class compType = source.getClass().getComponentType();
        int sourceLength = Array.getLength(source);
        Object target = Array.newInstance(compType, newLength);
        if (newLength < sourceLength) {
            sourceLength = newLength;
        }
        System.arraycopy(source, 0, target, 0, sourceLength);
        return target;
    }

    /**
   * Remove all occurances of an item from an array.  Shifts array
   * elements down to fill the gaps, and pads the end of the array
   * with the same number of nulls as there was items removed.
   * 
   * @param source The array to scan for items to remove.
   * @param count The number of elements in the array to scan.
   * @param item The item value to remove.
   * @return The new count of the number of items in the array.
   */
    public static int removeItem(Object source, int count, Object item) {
        int j = 0;
        for (int i = 0; i < count; i++) {
            Object e = Array.get(source, i);
            if (e != item) {
                if (j == i) {
                    j++;
                } else {
                    Array.set(source, j++, e);
                }
            }
        }
        for (int i = j + 1; i < count; i++) {
            Array.set(source, i, null);
        }
        return j;
    }

    /** Used by encodeId() */
    private static final char[] encodeChars = new char[64];

    /** Used by decodeId() */
    private static final short[] decodeChars = new short[128];

    static {
        for (short i = 0; i < 64; i++) {
            if (i < 26) {
                encodeChars[i] = (char) (65 + i);
            } else if (i < 52) {
                encodeChars[i] = (char) (71 + i);
            } else if (i < 62) {
                encodeChars[i] = (char) (i - 4);
            } else if (i < 63) {
                encodeChars[i] = '_';
            } else {
                encodeChars[i] = '-';
            }
            decodeChars[encodeChars[i]] = i;
        }
    }

    /**
   * Encode a 29-bit int into a 5 char legal HTML identifer.
   * Legal HTML identifiers start with [A-Za-z] followed by
   * zero or more [A-Za-z0-9] plus hyphen ('-') and underscore ('_').
   * Period ('.') and colon (':') are also allowed in HTML but are 
   * not used here.
   *  
   * @param id The id to turn into an identifier.
   * @return A legal identifier corrisponding to the id.
   */
    public static String encodeId(int id) {
        char[] buf = new char[5];
        for (int i = 4; i >= 0; i--) {
            buf[i] = encodeChars[id & 63];
            id = id >>> 6;
        }
        return new String(buf);
    }

    /**
   * Decode a 29-bit int from an identifier produced by encode.
   * @param s The identifer to decode.
   * @return The decoded integer.
   */
    public static int decodeId(String s) {
        int id = 0;
        for (int i = 0; i < 5; i++) {
            id = (id << 6) | decodeChars[s.charAt(i)];
        }
        return id;
    }

    /**
   * Generates a randomly started sequence of non-zero 29 bit integers.
   * This method is used to generate id's for elements that are then
   * encoded into 5 character identifiers.
   * @return The next integer in the sequence from 1..2^29-1
   */
    public static synchronized int nextSeq() {
        if (sequenceCounter == 0x1fffffff) {
            sequenceCounter = 0;
        }
        return ++sequenceCounter;
    }

    /**
   * Finds the first ocurence of any char in charList in s.
   * @param s  The string to search.
   * @param charList The characters to search for.
   * @param i  The index to start searching at.
   * @return The index of the found character, or -1.
   */
    public static int indexOfAny(String s, String charList, int i) {
        int n = (s == null) ? 0 : s.length();
        for (; i < n; i++) {
            if (charList.indexOf(s.charAt(i)) >= 0) {
                return i;
            }
        }
        return -1;
    }

    /**
   * Encodes newlines and backslashes as backslash-n and backslash-backslash.
   * @param source
   * @return The encoded string.
   */
    public static String encodeNl(String source) {
        int j = indexOfAny(source, "\n\\", 0);
        if (j < 0) {
            return source;
        }
        StringBuilder b = new StringBuilder(source.length() + 16);
        int i = 0;
        for (; ; ) {
            b.append(source.substring(i, j));
            if (source.charAt(j) == '\n') {
                b.append("\\n");
            } else {
                b.append("\\\\");
            }
            i = j + 1;
            j = indexOfAny(source, "\n\\", i);
            if (j < 0) {
                break;
            }
        }
        b.append(source.substring(i));
        return b.toString();
    }

    /**
   * Scans a string for backslash n and double backslash escapes
   * and replaces them with new lines and single backslashes.
   * @param source The string to decode.
   * @return The decode string, or the original if there were no escapses.
   */
    public static String decodeNl(String source) {
        if (source == null) {
            return null;
        }
        int i = 0;
        int j = source.indexOf('\\');
        if (j < 0) {
            return source;
        }
        int n = source.length();
        StringBuilder b = new StringBuilder(n);
        do {
            b.append(source.substring(i, j));
            j++;
            char ch = (j == n) ? 0 : source.charAt(j);
            if (ch == '\\') {
                b.append('\\');
                i = j + 1;
            } else if (ch == 'n') {
                b.append('\n');
                i = j + 1;
            } else {
                b.append('\\');
                i = j;
            }
            j = source.indexOf('\\', i);
        } while (j > 0);
        if (i < n) {
            b.append(source.substring(i));
        }
        return b.toString();
    }

    /**
   * Replace the characters quote, ambersand, 
   * less than and greater than with their html named entities.
   * @param source
   * @return  The encoded string.
   */
    public static String encodeHtml(String source) {
        int j = indexOfAny(source, "\"&<>", 0);
        if (j < 0) {
            return source;
        }
        StringBuilder b = new StringBuilder(source.length() + 16);
        int i = 0;
        for (; ; ) {
            b.append(source.substring(i, j));
            switch(source.charAt(j)) {
                case '\"':
                    b.append("&quot;");
                    break;
                case '&':
                    b.append("&amp;");
                    break;
                case '<':
                    b.append("&lt;");
                    break;
                case '>':
                    b.append("&gt;");
                    break;
            }
            i = j + 1;
            j = indexOfAny(source, "\"&<>", i);
            if (j < 0) {
                break;
            }
        }
        b.append(source.substring(i));
        return b.toString();
    }

    /**
   * Escapes strings that are about to be embedded in javascript single
   * quotes.  Does not append the single quotes.  Returns the original
   * string if no escaping is necessary.
   * 
   * Changes puts backslashes in front of ' and " and changes newlines to 
   * backslash n. 
   * 
   * @param s  The string to be escaped.
   * @return The escaped string.
   */
    public static String escapeSQuote(String s) {
        int i = 0;
        int j = indexOfAny(s, "\'\\\n", i);
        if (j < 0) {
            return s;
        }
        StringBuilder b = new StringBuilder(s.length() + 8);
        for (; ; ) {
            b.append(s, i, j);
            b.append('\\');
            char ch = s.charAt(j);
            b.append(ch == '\n' ? 'n' : ch);
            i = j + 1;
            j = indexOfAny(s, "\'\\\n", i);
            if (j < 0) {
                break;
            }
        }
        b.append(s, i, s.length());
        return b.toString();
    }
}

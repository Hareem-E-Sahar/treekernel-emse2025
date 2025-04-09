package com.sun.j2me.pim.formats;

import com.sun.j2me.pim.UnsupportedPIMFormatException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import com.sun.j2me.jsr75.StringUtil;

/**
 * Supporting methods for interpreting vCard and vCalendar encodings.
 *
 */
public class FormatSupport {

    /** Code name of the Quoted-Printable binary encoding. */
    public static final String QUOTED_PRINTABLE = "QUOTED_PRINTABLE";

    /** Code name of the Base64 binary encoding. */
    public static final String BASE64 = "BASE64";

    /** Code name of plain text binary encoding. */
    public static final String PLAIN_TEXT = "PLAIN_TEXT";

    /** Name of default character encoding. */
    public static final String UTF8 = "UTF-8";

    /** Repeat rule daily frequency char representation. */
    public static final char DAILY = 'D';

    /** Repeat rule weekly frequency char representation. */
    public static final char WEEKLY = 'W';

    /** Repeat rule monthly frequency char representation. */
    public static final char MONTHLY = 'M';

    /** Repeat rule yearly frequency char representation. */
    public static final char YEARLY = 'Y';

    /** Repeat rule day-in-month char representation. */
    public static final char DAY_IN_MONTH = 'D';

    /** Repeat rule week-in-month char representation. */
    public static final char WEEK_IN_MONTH = 'P';

    /** Repeat rule day-in-year char representation. */
    public static final char DAY_IN_YEAR = 'D';

    /** Repeat rule month-in-year char representation. */
    public static final char MONTH_IN_YEAR = 'M';

    /**
     * Gets the character set specified by the given property attributes.
     * The default is UTF-8, unless the attributes contain a CHARSET= entry.
     * @param attributes an array of vCard or vCalendar property attributes
     * @return the encoding specified by the attributes
     */
    public static String getCharSet(String[] attributes) {
        String charset = getAttributeValue(attributes, "CHARSET=", UTF8);
        try {
            "".getBytes(charset);
            return charset;
        } catch (UnsupportedEncodingException e) {
            return UTF8;
        }
    }

    /**
     * Gets an attribute of the form (key)(value), if one exists in the supplied
     * attributes list.
     * @param attributes an array of attributes
     * @param key the attribute key (e.g. "CHARSET=")
     * @param defaultValue a default value to be returned if no matching
     * attribute is found.
     * @return the value of the requested attribute, or defaultValue if the
     * attribute is not present.
     */
    public static String getAttributeValue(String[] attributes, String key, String defaultValue) {
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].startsWith(key)) {
                return attributes[i].substring(key.length());
            }
        }
        return defaultValue;
    }

    /**
     * Gets the encoding used for a value with the given attributes.
     *
     * @param attributes an array of attributes
     * @return either VCardSupport.QUOTED_PRINTABLE, VCardSupport.BASE64
     * or VCardSupport.PLAIN_TEXT
     */
    public static String getEncoding(String[] attributes) {
        for (int i = 0; i < attributes.length; i++) {
            String s = attributes[i].toUpperCase();
            if (s.equals("ENCODING=QUOTED-PRINTABLE") || s.equals("QUOTED-PRINTABLE")) {
                return QUOTED_PRINTABLE;
            }
            if (s.equals("ENCODING=BASE64") || s.equals("BASE64") || s.equals("ENCODING=B")) {
                return BASE64;
            }
        }
        return PLAIN_TEXT;
    }

    /**
     * Converts a string from the given UTF-8 plain text encoding to the
     * specified encoding.
     * @param data input data to be converted
     * @param encoding input data encoding
     * @param charset output encoding
     * @return encoded string
     */
    public static String convertString(String data, String encoding, String charset) {
        if (encoding.equals(QUOTED_PRINTABLE)) {
            byte[] b = QuotedPrintableEncoding.fromQuotedPrintable(data);
            try {
                return new String(b, charset);
            } catch (UnsupportedEncodingException e) {
                return new String(b);
            }
        } else if (encoding.equals(BASE64)) {
            byte[] b = Base64Encoding.fromBase64(data);
            try {
                return new String(b, charset);
            } catch (UnsupportedEncodingException e) {
                return new String(b);
            }
        } else if (charset.equals(UTF8)) {
            return data;
        } else {
            try {
                return new String(data.getBytes(UTF8), charset);
            } catch (UnsupportedEncodingException e) {
                throw new Error(UTF8 + " encoding not available");
            }
        }
    }

    /**
     * Sorts an array of integers.
     * @param a the list of integers
     */
    public static void sort(int[] a) {
        for (int j = 1; j < a.length; j++) {
            int v = a[j];
            int i = j - 1;
            while (i >= 0 && a[i] > v) {
                a[i + 1] = a[i];
                i--;
            }
            a[i + 1] = v;
        }
    }

    /**
     * Checks to see if a sorted array of integers contains a given integer.
     * @param a input array to be checked
     * @param value to be checked int the array
     * @return <code>true</code> if the value is found int the array
     */
    public static boolean contains(int[] a, int value) {
        int lowerBound = 0;
        int upperBound = a.length - 1;
        while (upperBound - lowerBound >= 0) {
            int i = lowerBound + (upperBound - lowerBound) / 2;
            int v = a[i];
            if (v > value) {
                upperBound = i - 1;
            } else if (v < value) {
                lowerBound = i + 1;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles data element parsing for V-object inputs.
     */
    public static class DataElement {

        /** Name of the property. */
        String propertyName;

        /** Attributes of the element. */
        String[] attributes;

        /** Data to be processed. */
        String data;
    }

    /**
     * Extracts data from a vCard or vCalendar line.
     *
     * @param line the input line, in the form
     * (propertyname)[;(attributes)]:(data)
     * @return the property data
     * @throws UnsupportedPIMFormatException if the line is not in the expected
     * format
     */
    public static DataElement parseObjectLine(String line) throws UnsupportedPIMFormatException {
        int i = line.indexOf(':');
        if (i == -1 || i == 0) {
            throw new UnsupportedPIMFormatException("Invalid line: '" + line + "'");
        }
        DataElement element = new DataElement();
        element.data = line.substring(i + 1).trim();
        String prefix = line.substring(0, i).trim();
        i = prefix.indexOf(';');
        if (i == -1) {
            element.propertyName = prefix.toUpperCase();
            element.attributes = new String[0];
        } else {
            element.propertyName = prefix.substring(0, i).toUpperCase();
            element.attributes = StringUtil.split(prefix, ';', i + 1);
            for (int j = 0; j < element.attributes.length; j++) {
                element.attributes[j] = element.attributes[j].toUpperCase();
            }
        }
        i = element.propertyName.lastIndexOf('.');
        if (i != -1) {
            element.propertyName = element.propertyName.substring(i + 1);
        }
        return element;
    }

    /**
     * Interpret a vCard or vCalendar data element as a string, taking
     * into account any encoding parameters specified in the attribute array.
     * @param attributes An array of attributes obtained from a class to
     * parseObjectLine.
     * @param data The string data of a vCard or vCalendar object line,
     * obtained from a call to parseObjectLine.
     * @return the decoded string data
     */
    public static String parseString(String[] attributes, String data) {
        String charset = getCharSet(attributes);
        String encoding = getEncoding(attributes);
        return convertString(data, encoding, charset);
    }

    /**
     * Interpret a vCard or vCalendar data element as a string array, taking
     * into account any encoding parameters specified in the attribute array.
     * @param attributes An array of attributes obtained from a call to
     * parseObjectLine.
     * @param data The string data of a vCard or vCalendar object line,
     * obtained from a call to parseObjectLine.
     * @return the decoded string array data
     */
    public static String[] parseStringArray(String[] attributes, String data) {
        String charset = getCharSet(attributes);
        String encoding = getEncoding(attributes);
        String[] elements = StringUtil.split(data, ';', 0, false);
        for (int i = 0; i < elements.length; i++) {
            elements[i] = convertString(elements[i], encoding, charset);
            if ("".equals(elements[i])) {
                elements[i] = null;
            }
        }
        return elements;
    }

    /**
     * Interpret a vCard or vCalendar data element as a byte array, taking
     * into account any encoding parameters specified in the attribute array.
     * @param attributes An array of attributes obtained from a class to
     * parseObjectLine.
     * @param data The string data of a vCard or vCalendar object line,
     * obtained from a call to parseObjectLine.
     * @return the decoded binary data
     */
    public static byte[] parseBinary(String[] attributes, String data) {
        String encoding = getEncoding(attributes);
        if (encoding.equals(QUOTED_PRINTABLE)) {
            return QuotedPrintableEncoding.fromQuotedPrintable(data);
        } else if (encoding.equals(BASE64)) {
            return Base64Encoding.fromBase64(data);
        } else {
            return data.getBytes();
        }
    }
}

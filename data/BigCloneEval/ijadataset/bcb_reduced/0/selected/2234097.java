package com.atolsystems.atolutilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class AStringUtilities {

    /**
     * Replace every occurrence of \n with System.getProperty("line.separator").
     *
     * @param in the string to process
     * 
     * @return a string derived from this string by replacing every occurrence of \n with System.getProperty("line.separator").
     */
    public static String adaptNewLineToPlatform(String in) {
        String out = in.replace("\n", System.getProperty("line.separator"));
        return out;
    }

    public static String systemNewLine;

    static {
        systemNewLine = System.getProperty("line.separator");
    }

    public static int count(String in, String toCount) {
        int count = 0;
        int searchIndex = 0;
        while ((searchIndex = in.indexOf(toCount, searchIndex)) != -1) {
            count++;
        }
        return count;
    }

    public static void writeToFile(String in, String fileName) throws FileNotFoundException {
        writeToFile(in, new File(fileName), EOL_SYSTEM);
    }

    public static void writeToFile(String in, String fileName, int eolPolicy) throws FileNotFoundException {
        writeToFile(in, new File(fileName), eolPolicy);
    }

    static final int EOL_SYSTEM = -1;

    static final int EOL_KEEP = 0;

    static final int EOL_UNIX = 1;

    static final int EOL_LF = 1;

    static final int EOL_WINDOWS = 2;

    static final int EOL_CRLF = 2;

    static final int EOL_MAC = 3;

    static final int EOL_CR = 3;

    public static void writeToFile(String in, File file, int eolPolicy) throws FileNotFoundException {
        PrintStream ps = new PrintStream(file);
        in = replaceEol(in, eolPolicy);
        ps.append(in);
        ps.close();
    }

    public static String replaceEol(String in, int eolPolicy) {
        String eol = null;
        if (EOL_KEEP == eolPolicy) return in;
        String out = in.replace("\r\n", "\n");
        out = out.replace("\r", "\n");
        switch(eolPolicy) {
            case EOL_SYSTEM:
                eol = systemNewLine;
            case EOL_LF:
                return out;
            case EOL_CR:
                eol = "\r";
                break;
            case EOL_CRLF:
                eol = "\r\n";
                break;
        }
        out = out.replace("\n", eol);
        return out;
    }

    public static String replaceEol(String in, String eol) {
        String out = in.replace("\r\n", "\n");
        out = out.replace("\r", "\n");
        out = out.replace("\n", eol);
        return out;
    }

    public static String readFromFile(String fileName) throws FileNotFoundException, IOException {
        return readFromFile(new File(fileName));
    }

    public static String readFromFile(File file) throws FileNotFoundException, IOException {
        BufferedReader isr = new BufferedReader(new FileReader(file));
        CharSequence out = AStreamUtilities.stream2CharSequence(isr);
        isr.close();
        return (String) out;
    }

    /**
     * Replace every occurrence of XML predifined entities by their respective
     * escape sequence:<p>
     * <ul>
     * <li>& is replaced by &amp;amp;
     * <li>&lt; is replaced by &amp;lt;
     * <li>> is replaced by &amp;gt;
     * <li>' is replaced by &amp;apos;
     * <li>" is replaced by &amp;quot;</ul>
     *
     * @param in the string to process
     * 
     * @return a string derived from this string by replacing XML entities.
     */
    public static String textToXml(String in) {
        String out = in.replace("&", "&amp;");
        out = out.replace("<", "&lt;");
        out = out.replace(">", "&gt;");
        out = out.replace("'", "&apos;");
        out = out.replace("\"", "&quot;");
        return out;
    }

    /**
     * Replace every occurrence of XML predifined escape sequence by their
     * respective entity:<p>
     * <ul>
     * <li>&amp;amp; is replaced by &
     * <li>&amp;lt; is replaced by &lt;
     * <li>&amp;gt; is replaced by >
     * <li>&amp;apos; is replaced by '
     * <li>&amp;quot; is replaced by "</ul>
     *
     * @param in the string to process
     * 
     * @return a string derived from this string by replacing XML escape sequences.
     */
    public static String xmlToText(String in) {
        String out = in.replace("&quot;", "\"");
        out = out.replace("&lt;", "<");
        out = out.replace("&gt;", ">");
        out = out.replace("&apos;", "'");
        out = out.replace("&amp;", "&");
        return out;
    }

    /**
     * Encode a text into a Java/C++ source code string<p>
     *
     * " is replaced by \"
     * \ is replaced by \\
     * 
 is replaced by \n
     *  is replaced by \r
     *
     * @param in the string to process
     *
     * @return a string derived from this string by replacing special characters
     * by their respective escape sequence.
     */
    public static String textToSourceCodeString(String in) {
        String out = in.replace("\\", "\\");
        out = out.replace("\"", "\\\"");
        out = out.replace("\n", "\\\n");
        out = out.replace("\r", "\\\r");
        return out;
    }

    /**
     * Remove Java/C++ single line comments from a string<p>
     *
     * @param in the string to process
     *
     * @return a string derived from this string without single line comments.
     */
    public static String removeSingleLineComments(String in) {
        StringBuilder str = new StringBuilder(in.length());
        int searchIndex = 0;
        int copyIndex = 0;
        do {
            int stringIndex = in.indexOf('"', searchIndex);
            int commentIndex = in.indexOf("//", searchIndex);
            if ((stringIndex != -1) && (stringIndex < commentIndex)) {
                searchIndex = in.indexOf('"', stringIndex);
                if (searchIndex == -1) {
                    str.append(in.substring(copyIndex));
                    break;
                }
                searchIndex++;
            } else {
                if (commentIndex != -1) {
                    str.append(in.substring(copyIndex, commentIndex));
                    copyIndex = in.indexOf('\n', searchIndex);
                    searchIndex = copyIndex;
                } else {
                    str.append(in.substring(copyIndex));
                    break;
                }
            }
        } while (copyIndex != -1);
        return str.toString();
    }

    public static String removeLeadingBlanks(String in) {
        String out = in;
        int match[] = findFirstRegExp2(in, "\\A\\s+");
        if (match[0] == 0) {
            out = in.substring(match[1]);
        }
        return out;
    }

    public static String removeTrailingBlanks(String in) {
        String out = in;
        int match[] = findLastRegExp2(in, "\\s+?\\z");
        if (match[0] != -1) {
            out = in.substring(0, match[0]);
        }
        return out;
    }

    public static int findFirstRegExp(String input, String regExp) {
        return findFirstRegExp2(input, regExp)[0];
    }

    public static int[] findFirstRegExp2(String input, String regExp) {
        int[] out = new int[2];
        out[0] = -1;
        Pattern lbPattern = Pattern.compile(regExp);
        Matcher matcher = lbPattern.matcher(input);
        if (matcher.find()) {
            out[0] = matcher.start();
            out[1] = matcher.end();
        }
        return out;
    }

    public static int findLastRegExp(String input, String regExp) throws java.util.regex.PatternSyntaxException {
        return findLastRegExp2(input, regExp)[0];
    }

    public static int[] findLastRegExp2(String input, String regExp) throws java.util.regex.PatternSyntaxException {
        int[] out = new int[2];
        out[0] = -1;
        Pattern lbPattern = Pattern.compile(regExp);
        Matcher matcher = lbPattern.matcher(input);
        while (matcher.find()) {
            out[0] = matcher.start();
            out[1] = matcher.end();
        }
        return out;
    }

    /**
     * Find the first occurrence of any character contained in <code>chars</code> in <code>in</code>.
     *
     * @param chars a string containing the characters to look for
     * @param in the string to process
     *
     * @return index of the first match, or -1 if no match exists
     */
    public static int findFirstOf(String chars, String in) {
        return findFirstOf(chars, in, 0);
    }

    /**
     * Find the first occurrence of any character contained in <code>chars</code> in <code>in</code>.
     * The search starts from the index specified by <code>offset</code>.
     *
     * @param chars a string containing the characters to look for
     * @param in the string to process
     * @param offset the index from which the search should start
     *
     * @return index of the first match, or -1 if no match exists
     */
    public static int findFirstOf(String chars, String in, int offset) {
        int out = in.length();
        for (int i = 0; i < chars.length(); i++) {
            int temp = in.indexOf(chars.charAt(i), offset);
            if (temp != -1) {
                out = Math.min(out, temp);
            }
        }
        if (out == in.length()) {
            out = -1;
        }
        return out;
    }

    /**
     * Find the last occurrence of any character contained in <code>chars</code> in <code>in</code>.
     *
     * @param chars a string containing the characters to look for
     * @param in the string to process
     *
     * @return index of the last match, or -1 if no match exists
     */
    public static int findLastOf(String chars, String in) {
        return findLastOf(chars, in, in.length());
    }

    /**
     * Find the last occurrence of any character contained in <code>chars</code> in <code>in</code>.
     * The search starts from the index specified by <code>offset</code>.
     *
     * @param chars a string containing the characters to look for
     * @param in the string to process
     * @param offset the index from which the search should start
     *
     * @return index of the last match, or -1 if no match exists.
     */
    public static int findLastOf(String chars, String in, int offset) {
        int out = -1;
        for (int i = 0; i < chars.length(); i++) {
            out = Math.max(out, in.lastIndexOf(chars.charAt(i), offset));
        }
        return out;
    }

    public static String replace(String in, Map<String, String> replaceMap) {
        String out = in;
        for (String s : replaceMap.keySet()) {
            out = out.replace(s, replaceMap.get(s));
        }
        return out;
    }

    public static byte hexToByte(String str) throws IllegalArgumentException {
        return hexToByte(str, 0);
    }

    public static byte hexToByte(String str, int offset) throws IllegalArgumentException {
        byte out;
        if (str == null) {
            throw new IllegalArgumentException();
        }
        if (str.length() < offset + 2) {
            throw new IllegalArgumentException("input range must be at least 2 characters long");
        }
        String in = str.substring(offset, offset + 2);
        out = (byte) Integer.parseInt(in, 16);
        return out;
    }

    public static String byteToHex(int data) {
        StringBuilder str = new StringBuilder(2);
        if ((data & 0xF0) == 0) str.append("0");
        str.append(java.lang.Integer.toHexString(data & 0xFF).toUpperCase());
        return str.toString();
    }

    public static short hexToShort(String str) throws IllegalArgumentException {
        short out;
        if (str == null) {
            throw new IllegalArgumentException();
        } else if (str.length() != 4) {
            throw new IllegalArgumentException("input string must be exactly 4 characters long");
        } else {
            out = (short) Integer.parseInt(str, 16);
        }
        return out;
    }

    public static int hexToInt(String str) throws IllegalArgumentException {
        int out;
        if (str == null) {
            throw new IllegalArgumentException();
        } else if (str.length() != 8) {
            throw new IllegalArgumentException("input string must be exactly 8 characters long");
        } else {
            out = (int) Long.parseLong(str, 16);
        }
        return out;
    }

    public static String shortToHex(int data) {
        StringBuilder str = new StringBuilder(4);
        if ((data & 0xF000) == 0) {
            str.append("0");
            if ((data & 0xFF00) == 0) {
                str.append("0");
                if ((data & 0xFFF0) == 0) str.append("0");
            }
        }
        str.append(java.lang.Integer.toHexString(data & 0xFFFF).toUpperCase());
        return str.toString();
    }

    public static String longToHex(long data) {
        StringBuilder str = new StringBuilder(16);
        str.append(intToHex((int) (data >> 32)));
        str.append(intToHex((int) (data & 0xFFFFFFFF)));
        return str.toString();
    }

    public static String longToHex(long data, int widthInBits) {
        int hexDigitWidth = (widthInBits + 3) / 4;
        return longToHex(data).substring(16 - hexDigitWidth);
    }

    public static String intToHex(int data) {
        StringBuilder str = new StringBuilder(8);
        str.append(shortToHex(data >> 16));
        str.append(shortToHex(data));
        return str.toString();
    }

    public static String intToHex(int data, int width) {
        int hexDigitWidth = (width + 3) / 4;
        return intToHex(data).substring(8 - hexDigitWidth);
    }

    public static boolean[] intsToBools(int[] data, int nBitsPerInt) {
        boolean[] out = new boolean[data.length * nBitsPerInt];
        for (int i = 0; i < out.length; i++) {
            int val = data[i / nBitsPerInt];
            int oBit = i % nBitsPerInt;
            int bit = (val >> oBit) & 0x01;
            if (bit == 0x01) out[i] = true;
        }
        return out;
    }

    public static String intsToHex(int[] data, int nBits) {
        boolean[] bits = intsToBools(data, nBits);
        return boolsToHexLittleEndianNibbles(bits);
    }

    /**
     * Convert an integer into its binary representation
     * @param data
     * @param bitOffset
     * @param bitLength
     * @return a string with 0/1 values, LSB is on the left (NOT the usual representation)
     */
    public static String toBinStr(int data, int bitOffset, int bitLength) {
        boolean[] bools = AArrayUtilities.int2Bools(data);
        String s = AStringUtilities.toBinStr(bools, bitOffset, bitLength);
        return s;
    }

    public static String toRightToLeftBinStr(int data, int bitOffset, int bitLength) {
        return reverse(toBinStr(data, bitOffset, bitLength));
    }

    public static String toBinStr(boolean[] bools, int bitOffset, int bitLength) {
        char[] out = new char[bitLength];
        for (int i = 0; i < bitLength; i++) {
            if (bools[i + bitOffset]) out[i] = '1'; else out[i] = '0';
        }
        return new String(out);
    }

    public static String toRightToLeftBinStr(boolean[] bools, int bitOffset, int bitLength) {
        return reverse(toBinStr(bools, bitOffset, bitLength));
    }

    public static String reverse(String in) {
        int length = in.length();
        char[] out = new char[length];
        for (int i = 0; i < in.length(); i++) {
            out[length - i - 1] = in.charAt(i);
        }
        return new String(out);
    }

    public static byte hexToByte(StringReader reader) throws IOException {
        char[] buf = new char[2];
        reader.read(buf);
        return AStringUtilities.hexToByte(new String(buf));
    }

    public static short hexToShort(StringReader reader) throws IOException {
        char[] buf = new char[4];
        reader.read(buf);
        return AStringUtilities.hexToShort(new String(buf));
    }

    public static int hexToInt(StringReader reader) throws IOException {
        char[] buf = new char[8];
        reader.read(buf);
        return AStringUtilities.hexToInt(new String(buf));
    }

    public static int intBitMask(int nBits) {
        return (int) ((1L << nBits) - 1L);
    }

    public static int hexToInt(StringReader reader, int nChars, int nBits) throws IOException {
        char[] buf = new char[nChars];
        reader.read(buf);
        char[] paddedBuf = new char[8];
        int i = 0;
        for (i = 0; i < 8 - nChars; i++) {
            paddedBuf[i] = '0';
        }
        System.arraycopy(buf, 0, paddedBuf, i, nChars);
        int mask = intBitMask(nBits);
        return mask & AStringUtilities.hexToInt(new String(paddedBuf));
    }

    public static byte[] hexToBytes(String str) throws IllegalArgumentException {
        if (str == null) {
            throw new NullPointerException();
        } else if ((str.length() % 2) > 0) {
            throw new IllegalArgumentException("cannot convert a string with odd number of characters to hex bytes");
        } else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
            }
            return buffer;
        }
    }

    public static void hexToBools(String src, int srcPos, boolean[] dest, int destPos, int length) throws IllegalArgumentException {
        String hexStr;
        if (0 != (src.length() % 2)) hexStr = src + "0"; else hexStr = src;
        byte[] bytes = hexToBytes(hexStr);
        boolean[] bools = AArrayUtilities.bytes2Bools(bytes);
        System.arraycopy(bools, srcPos, dest, destPos, length);
    }

    public static byte[] hexToBytesRightToLeft(String str, String charactersToIgnore) throws IllegalArgumentException {
        byte[] lr = hexToBytes(str, charactersToIgnore);
        return AArrayUtilities.reverse(lr);
    }

    public static byte[] hexToBytes(String str, String charactersToIgnore) throws IllegalArgumentException {
        return hexToBytes(str, charactersToIgnore, new String[0]);
    }

    public static byte[] hexToBytes(String str, String charactersToIgnore, String[] validPrefixes) throws IllegalArgumentException {
        if (str == null) {
            throw new NullPointerException();
        } else {
            ByteBuffer bytes = MappedByteBuffer.allocate(str.length() / 2);
            int index = 0;
            try {
                while ((index + 1) < str.length()) {
                    boolean prefixFound = false;
                    for (String p : validPrefixes) {
                        if (p.equals(str.substring(index, index + p.length()))) {
                            index += p.length();
                            prefixFound = true;
                            break;
                        }
                    }
                    if (!prefixFound) {
                        if (-1 != charactersToIgnore.indexOf(str.charAt(index))) index++; else {
                            if (-1 != charactersToIgnore.indexOf(str.charAt(index + 1))) throw new IllegalArgumentException("Illegal character found in second nibble of hex byte");
                            bytes.put((byte) Integer.parseInt(str.substring(index, index + 2), 16));
                            index += 2;
                        }
                    }
                }
                if ((index < str.length()) && (-1 == charactersToIgnore.indexOf(str.charAt(index)))) throw new IllegalArgumentException("Last character not consumed");
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("cannot convert string to hex bytes", e);
            }
            byte out[] = new byte[bytes.position()];
            bytes.rewind();
            bytes.get(out);
            return out;
        }
    }

    public static String boolsToHexLittleEndianNibbles(boolean[] data) {
        if (data == null) return null;
        return boolsToHexLittleEndianNibbles(data, 0, data.length);
    }

    /**
     * index  0123 4567 89AB CDEF
     * bools: 1000_0010 1100_1110
     * hex:  "1    4    3    7" 
     * @param data
     * @param offset
     * @param length
     * @return Hexadecimal representation of <code>data</code>
     */
    public static String boolsToHexLittleEndianNibbles(boolean[] data, int offset, int length) {
        if (data == null) return null;
        StringBuilder str = new StringBuilder(data.length * 2);
        for (int i = offset; i < offset + length; i += 4) {
            int digit = 0;
            if ((data.length > i + 3) && data[i + 3]) digit += 8;
            if ((data.length > i + 2) && data[i + 2]) digit += 4;
            if ((data.length > i + 1) && data[i + 1]) digit += 2;
            if ((data.length > i + 0) && data[i + 0]) digit += 1;
            str.append(java.lang.Integer.toHexString(digit).toUpperCase());
        }
        return str.toString();
    }

    @Deprecated
    public static String boolsToHex(boolean[] data) {
        if (data == null) return null;
        return boolsToHex(data, 0, data.length);
    }

    /**
     * index  0123 4567 89AB CDEF
     * bools: 1000_0010 1100_1110
     * hex:  "8    2    C    E" 
     * @param data
     * @param offset
     * @param length
     * @return Hexadecimal representation of <code>data</code>
     */
    @Deprecated
    public static String boolsToHex(boolean[] data, int offset, int length) {
        if (data == null) return null;
        StringBuilder str = new StringBuilder(data.length * 2);
        for (int i = offset; i < offset + length; i += 4) {
            int digit = 0;
            if ((data.length > i + 3) && data[i + 3]) digit += 1;
            if ((data.length > i + 2) && data[i + 2]) digit += 2;
            if ((data.length > i + 1) && data[i + 1]) digit += 4;
            if ((data.length > i + 0) && data[i + 0]) digit += 8;
            str.append(java.lang.Integer.toHexString(digit).toUpperCase());
        }
        return str.toString();
    }

    @Deprecated
    public static String bytesToHex(byte[] data) {
        if (data == null) return null;
        return bytesToHex(data, 0, data.length);
    }

    @Deprecated
    public static String bytesToHex(byte[] data, int offset, int length) {
        if (data == null) return null;
        StringBuilder str = new StringBuilder(data.length * 2);
        for (int i = offset; i < offset + length; i++) {
            if ((data[i] & 0xF0) == 0) str.append("0");
            str.append(java.lang.Integer.toHexString(data[i] & 0xFF).toUpperCase());
        }
        return str.toString();
    }

    public static byte[] StringToBytes(String str) throws IllegalArgumentException {
        if (str == null) {
            throw new IllegalArgumentException();
        }
        byte[] buffer = new byte[str.length()];
        for (int i = 0; i < str.length(); i++) {
            buffer[i] = (byte) str.charAt(i);
        }
        return buffer;
    }

    public static byte[] StringToBytesWithFinalNull(String str) throws IllegalArgumentException {
        if (str == null) {
            throw new IllegalArgumentException();
        }
        byte[] buffer = new byte[str.length() + 1];
        for (int i = 0; i < str.length(); i++) {
            buffer[i] = (byte) str.charAt(i);
        }
        buffer[str.length()] = (byte) 0;
        return buffer;
    }

    @Deprecated
    public static short[] hexToShorts(String str) throws IllegalArgumentException {
        if (str == null) {
            return null;
        } else if ((str.length() % 4) > 0) {
            throw new IllegalArgumentException("cannot convert a string with number of characters not multiple of 4 to hex shorts");
        } else {
            int len = str.length() / 4;
            short[] buffer = new short[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = (short) Integer.parseInt(str.substring(i * 4, i * 4 + 4), 16);
            }
            return buffer;
        }
    }

    @Deprecated
    public static String shortsToHex(short[] data) {
        if (data == null) {
            return null;
        } else {
            StringBuilder str = new StringBuilder(data.length * 4);
            for (int i = 0; i < data.length; i++) {
                if ((data[i] & 0xF000) == 0) {
                    str.append("0");
                    if ((data[i] & 0xFF00) == 0) {
                        str.append("0");
                        if ((data[i] & 0xFFF0) == 0) str.append("0");
                    }
                }
                str.append(java.lang.Integer.toHexString(data[i] & 0xFFFF).toUpperCase());
            }
            return str.toString();
        }
    }

    public static String boolsToString(boolean[] in) {
        return boolsToString(in, ", ", "", 0, ' ');
    }

    public static String boolsToString(boolean[] in, String separator) {
        return boolsToString(in, separator, "", 0, ' ');
    }

    public static String boolsToString(boolean[] in, String separator, String prefix) {
        return boolsToString(in, separator, prefix, 0, ' ');
    }

    public static String boolsToString(boolean[] in, String separator, String prefix, int padSize) {
        return boolsToString(in, separator, prefix, padSize, ' ');
    }

    public static String boolsToString(boolean[] in, String separator, String prefix, int padSize, char padChar) {
        int actualPadSize = padSize - prefix.length() - separator.length();
        if (0 > actualPadSize) actualPadSize = 0;
        StringBuilder out = new StringBuilder();
        char[] padding = new char[actualPadSize];
        for (int i = 0; i < actualPadSize; i++) padding[i] = padChar;
        String pad = new String(padding);
        for (int i = 0; i < in.length; i++) {
            String t = in[i] ? "1" : "0";
            if (t.length() < actualPadSize) out.append(pad.substring(0, actualPadSize - t.length()));
            out.append(prefix);
            out.append(t);
            if (i != in.length - 1) out.append(separator);
        }
        return out.toString();
    }

    public static String intsToString(int[] in) {
        return intsToString(in, 10, ", ", "", 0, ' ');
    }

    public static String intsToString(int[] in, int radix) {
        return intsToString(in, radix, ", ", "", 0, ' ');
    }

    public static String intsToString(int[] in, int radix, String separator) {
        return intsToString(in, radix, separator, "", 0, ' ');
    }

    public static String intsToString(int[] in, int radix, String separator, String prefix) {
        return intsToString(in, radix, separator, prefix, 0, ' ');
    }

    public static String intsToString(int[] in, int radix, String separator, String prefix, int padSize) {
        return intsToString(in, radix, separator, prefix, padSize, ' ');
    }

    public static String intsToString(int[] in, int radix, String separator, String prefix, int padSize, char padChar) {
        int actualPadSize = padSize - prefix.length() - separator.length();
        if (0 > actualPadSize) actualPadSize = 0;
        StringBuilder out = new StringBuilder();
        char[] padding = new char[actualPadSize];
        for (int i = 0; i < actualPadSize; i++) padding[i] = padChar;
        String pad = new String(padding);
        for (int i = 0; i < in.length; i++) {
            String t = Integer.toString(in[i], radix);
            if (t.length() < actualPadSize) out.append(pad.substring(0, actualPadSize - t.length()));
            out.append(prefix);
            out.append(t);
            if (i != in.length - 1) out.append(separator);
        }
        return out.toString();
    }

    public static String intToString(int in, int radix, int padSize) {
        return intToString(in, radix, padSize, ' ');
    }

    public static String intToString(int in, int radix, int padSize, char padChar) {
        StringBuilder out = new StringBuilder();
        String t = Integer.toString(in, radix);
        int toPad = padSize - t.length();
        for (int i = 0; i < toPad; i++) out.append(padChar);
        out.append(t);
        return out.toString();
    }

    public static String shortsToString(short[] data, MutableInteger offset) {
        StringBuilder str = new StringBuilder(data.length);
        while ((offset.value < data.length) && (0x0000 != data[offset.value])) {
            str.append((char) (data[offset.value]));
            offset.value++;
        }
        offset.value++;
        return str.toString();
    }

    public static String shortsToString(short[] data) {
        return shortsToString(data, new MutableInteger(0));
    }

    public static String[] shortsToStrings(short[] data, MutableInteger offset) {
        List<String> strings = new ArrayList<String>();
        while ((offset.value < data.length) && (0x00 != data[offset.value])) {
            strings.add(shortsToString(data, offset));
        }
        return (String[]) strings.toArray(new String[strings.size()]);
    }

    public static String[] shortsToStrings(short[] data) {
        return shortsToStrings(data, new MutableInteger(0));
    }

    public static String bytesToString(byte[] data, MutableInteger offset) {
        StringBuilder str = new StringBuilder(data.length);
        while ((offset.value < data.length) && (0x00 != data[offset.value])) {
            str.append((char) (data[offset.value]));
            offset.value++;
        }
        offset.value++;
        return str.toString();
    }

    public static String bytesToString(byte[] data) {
        return bytesToString(data, new MutableInteger(0));
    }

    public static String[] bytesToStrings(byte[] data, MutableInteger offset) {
        List<String> strings = new ArrayList<String>();
        while (data[offset.value] != 0x0000) {
            strings.add(bytesToString(data, offset));
        }
        return (String[]) strings.toArray(new String[strings.size()]);
    }

    public static String[] bytesToStrings(byte[] data) {
        return bytesToStrings(data, new MutableInteger(0));
    }

    public static String limitSize(String data, int sizeLimit) {
        return limitSize(data, sizeLimit, "");
    }

    public static String limitSize(String data, int sizeLimit, String toAppend) {
        if (0 <= sizeLimit) {
            if (data.length() > sizeLimit) return data.substring(0, sizeLimit) + toAppend;
        }
        return data;
    }
}

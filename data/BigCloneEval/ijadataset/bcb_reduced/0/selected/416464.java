package org.apache.commons.compress.archivers.zip;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This ZipEncoding implementation implements a simple 8bit character
 * set, which mets the following restrictions:
 * 
 * <ul>
 * <li>Characters 0x0000 to 0x007f are encoded as the corresponding
 *        byte values 0x00 to 0x7f.</li>
 * <li>All byte codes from 0x80 to 0xff are mapped to a unique unicode
 *       character in the range 0x0080 to 0x7fff. (No support for
 *       UTF-16 surrogates)
 * </ul>
 * 
 * <p>These restrictions most notably apply to the most prominent
 * omissions of java-1.4's {@link java.nio.charset.Charset Charset}
 * implementation, Cp437 and Cp850.</p>
 * 
 * <p>The methods of this class are reentrant.</p>
 * @Immutable
 */
class Simple8BitZipEncoding implements ZipEncoding {

    /**
     * A character entity, which is put to the reverse mapping table
     * of a simple encoding.
     */
    private static final class Simple8BitChar implements Comparable {

        public final char unicode;

        public final byte code;

        Simple8BitChar(byte code, char unicode) {
            this.code = code;
            this.unicode = unicode;
        }

        public int compareTo(Object o) {
            Simple8BitChar a = (Simple8BitChar) o;
            return this.unicode - a.unicode;
        }

        public String toString() {
            return "0x" + Integer.toHexString(0xffff & unicode) + "->0x" + Integer.toHexString(0xff & code);
        }
    }

    /**
     * The characters for byte values of 128 to 255 stored as an array of
     * 128 chars.
     */
    private final char[] highChars;

    /**
     * A list of {@link Simple8BitChar} objects sorted by the unicode
     * field.  This list is used to binary search reverse mapping of
     * unicode characters with a character code greater than 127.
     */
    private final List reverseMapping;

    /**
     * @param highChars The characters for byte values of 128 to 255
     * stored as an array of 128 chars.
     */
    public Simple8BitZipEncoding(char[] highChars) {
        this.highChars = (char[]) highChars.clone();
        List temp = new ArrayList(this.highChars.length);
        byte code = 127;
        for (int i = 0; i < this.highChars.length; ++i) {
            temp.add(new Simple8BitChar(++code, this.highChars[i]));
        }
        Collections.sort(temp);
        this.reverseMapping = Collections.unmodifiableList(temp);
    }

    /**
     * Return the character code for a given encoded byte.
     * 
     * @param b The byte to decode.
     * @return The associated character value.
     */
    public char decodeByte(byte b) {
        if (b >= 0) {
            return (char) b;
        }
        return this.highChars[128 + b];
    }

    /**
     * @param c The character to encode.
     * @return Whether the given unicode character is covered by this encoding.
     */
    public boolean canEncodeChar(char c) {
        if (c >= 0 && c < 128) {
            return true;
        }
        Simple8BitChar r = this.encodeHighChar(c);
        return r != null;
    }

    /**
     * Pushes the encoded form of the given character to the given byte buffer.
     * 
     * @param bb The byte buffer to write to.
     * @param c The character to encode.
     * @return Whether the given unicode character is covered by this encoding.
     *         If <code>false</code> is returned, nothing is pushed to the
     *         byte buffer. 
     */
    public boolean pushEncodedChar(ByteBuffer bb, char c) {
        if (c >= 0 && c < 128) {
            bb.put((byte) c);
            return true;
        }
        Simple8BitChar r = this.encodeHighChar(c);
        if (r == null) {
            return false;
        }
        bb.put(r.code);
        return true;
    }

    /**
     * @param c A unicode character in the range from 0x0080 to 0x7f00
     * @return A Simple8BitChar, if this character is covered by this encoding.
     *         A <code>null</code> value is returned, if this character is not
     *         covered by this encoding.
     */
    private Simple8BitChar encodeHighChar(char c) {
        int i0 = 0;
        int i1 = this.reverseMapping.size();
        while (i1 > i0) {
            int i = i0 + (i1 - i0) / 2;
            Simple8BitChar m = (Simple8BitChar) this.reverseMapping.get(i);
            if (m.unicode == c) {
                return m;
            }
            if (m.unicode < c) {
                i0 = i + 1;
            } else {
                i1 = i;
            }
        }
        if (i0 >= this.reverseMapping.size()) {
            return null;
        }
        Simple8BitChar r = (Simple8BitChar) this.reverseMapping.get(i0);
        if (r.unicode != c) {
            return null;
        }
        return r;
    }

    /**
     * @see
     * org.apache.commons.compress.archivers.zip.ZipEncoding#canEncode(java.lang.String)
     */
    public boolean canEncode(String name) {
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (!this.canEncodeChar(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @see
     * org.apache.commons.compress.archivers.zip.ZipEncoding#encode(java.lang.String)
     */
    public ByteBuffer encode(String name) {
        ByteBuffer out = ByteBuffer.allocate(name.length() + 6 + (name.length() + 1) / 2);
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (out.remaining() < 6) {
                out = ZipEncodingHelper.growBuffer(out, out.position() + 6);
            }
            if (!this.pushEncodedChar(out, c)) {
                ZipEncodingHelper.appendSurrogate(out, c);
            }
        }
        out.limit(out.position());
        out.rewind();
        return out;
    }

    /**
     * @see
     * org.apache.commons.compress.archivers.zip.ZipEncoding#decode(byte[])
     */
    public String decode(byte[] data) throws IOException {
        char[] ret = new char[data.length];
        for (int i = 0; i < data.length; ++i) {
            ret[i] = this.decodeByte(data[i]);
        }
        return new String(ret);
    }
}

package net.jxta.endpoint;

import net.jxta.document.MimeMediaType;
import java.util.logging.Level;
import net.jxta.logging.Logging;
import java.util.logging.Logger;
import java.io.*;
import java.lang.ref.SoftReference;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * A Message Element using byte arrays for the element data.
 *
 * <p/>This implementation does not copy the byte array provided and assumes
 * that the contents of the byte array will not change through out the lifetime
 * of the MessageElement.
 *
 * <p/>some synchronization is due to optimization in {@link #getBytes(boolean)}
 * which replaces value of internal member {@link #b}.
 */
public class ByteArrayMessageElement extends MessageElement {

    /**
     * Logger
     */
    private static final transient Logger LOG = Logger.getLogger(ByteArrayMessageElement.class.getName());

    /**
     * The bytes of this element.
     */
    protected byte[] b;

    /**
     * This is the offset of our data within the array
     */
    protected int offset;

    /**
     * length of the element data. sometimes the same as b.length, but may be
     * lesser.
     */
    protected int len;

    /**
     * Create a new Message Element. The contents of the provided byte array
     * are <b>not</b> copied during construction.
     *
     * @param name Name of the MessageElement. May be the empty string ("") if
     *             the MessageElement is not named.
     * @param type Type of the MessageElement. null is the same as specifying
     *             the type "Application/Octet-stream".
     * @param b    A byte array containing the contents of this element.
     * @param sig  optional message digest/digital signature element or null if
     *             no signature is desired.
     */
    public ByteArrayMessageElement(String name, MimeMediaType type, byte[] b, MessageElement sig) {
        this(name, type, b, 0, b.length, sig);
    }

    /**
     * Create a new MessageElement, The contents of the provided byte array are
     * <b>not</b> copied during construction.
     *
     * @param name   Name of the MessageElement. May be the empty string ("") if
     *               the MessageElement is not named.
     * @param type   Type of the MessageElement. null is the same as specifying
     *               the type "Application/Octet-stream".
     * @param b      A byte array containing the contents of this element.
     * @param offset all bytes before this location in <code>b</code>
     *               will be ignored.
     * @param sig    optional message digest/digital signature elemnent or null if
     *               no signature is desired.
     */
    public ByteArrayMessageElement(String name, MimeMediaType type, byte[] b, int offset, MessageElement sig) {
        this(name, type, b, offset, b.length - offset, sig);
    }

    /**
     * Create a new Element, but dont add it to the message.  The contents of
     * the byte array are <b>not</b> copied during construction.
     *
     * @param name   Name of the MessageElement. May be the empty string ("") if
     *               the MessageElement is not named.
     * @param type   Type of the MessageElement. null is the same as specifying
     *               the type "Application/Octet-stream".
     * @param b      A byte array containing the contents of this Element.
     * @param offset all bytes before this location will be ignored.
     * @param len    number of bytes to include
     * @param sig    optional message digest/digital signature element or null if
     *               no signature is desired.
     */
    public ByteArrayMessageElement(String name, MimeMediaType type, byte[] b, int offset, int len, MessageElement sig) {
        super(name, type, sig);
        if (null == b) {
            throw new IllegalArgumentException("byte array must not be null");
        }
        if (len < 0) {
            throw new IllegalArgumentException("len must be >= 0 : " + len);
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset must within byte array : " + offset);
        }
        if ((0 != len) && (offset >= b.length)) {
            throw new IllegalArgumentException("offset must be positioned within byte array : " + offset + "," + len);
        }
        if (((offset + len) > b.length) || ((offset + len) < 0)) {
            throw new IllegalArgumentException("offset + len must be positioned within byte array");
        }
        if ((0 == len) && (0 != b.length)) {
            b = new byte[len];
            offset = 0;
        }
        this.b = b;
        this.offset = offset;
        this.len = len;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object target) {
        if (this == target) {
            return true;
        }
        if (target instanceof MessageElement) {
            if (!super.equals(target)) {
                return false;
            }
            if (target instanceof ByteArrayMessageElement) {
                ByteArrayMessageElement likeMe = (ByteArrayMessageElement) target;
                synchronized (this) {
                    synchronized (likeMe) {
                        if (likeMe.len != len) {
                            return false;
                        }
                        for (int eachByte = len - 1; eachByte >= 0; eachByte--) {
                            if (likeMe.b[likeMe.offset + eachByte] != b[offset + eachByte]) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            } else {
                try {
                    MessageElement likeMe = (MessageElement) target;
                    InputStream myStream = getStream();
                    InputStream itsStream = likeMe.getStream();
                    int mine;
                    int its;
                    do {
                        mine = myStream.read();
                        its = itsStream.read();
                        if (mine != its) {
                            return false;
                        }
                    } while ((-1 != mine) && (-1 != its));
                    return ((-1 == mine) && (-1 == its));
                } catch (IOException fatal) {
                    if (Logging.SHOW_SEVERE && LOG.isLoggable(Level.SEVERE)) {
                        LOG.log(Level.SEVERE, "MessageElements could not be compared.", fatal);
                    }
                    throw new IllegalStateException("MessageElements could not be compared." + fatal);
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int hashCode() {
        Checksum crc = new CRC32();
        crc.update(b, offset, len);
        int dataHash = (int) crc.getValue();
        int result = super.hashCode() * 6037 + dataHash;
        return (0 != result) ? result : 1;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Returns the string representation of this element. The 'charset'
     * parameter of the mimetype, if any, is used to determine encoding. If
     * the charset specified is unsupported then the default encoding will be
     * used.
     *
     * @return String string representation of this message element.
     */
    @Override
    public synchronized String toString() {
        String result;
        if (null != cachedToString) {
            result = cachedToString.get();
            if (null != result) {
                return result;
            }
        }
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("creating toString of " + getClass().getName() + '@' + Integer.toHexString(hashCode()));
        }
        String charset = type.getParameter("charset");
        try {
            if (null == charset) {
                result = new String(b, offset, len);
            } else {
                result = new String(b, offset, len, charset);
            }
        } catch (UnsupportedEncodingException caught) {
            result = new String(b, offset, len);
        }
        cachedToString = new SoftReference<String>(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getByteLength() {
        return len;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p/>synchronized so that we can replace our internal buffer with
     * the buffer we are returning if we were using a shared buffer.
     */
    @Override
    public synchronized byte[] getBytes(boolean copy) {
        if ((!copy) && (0 == offset) && (b.length == len)) {
            return b;
        }
        byte[] result = new byte[len];
        System.arraycopy(b, offset, result, 0, len);
        if (!copy) {
            b = result;
            offset = 0;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized InputStream getStream() {
        return new ByteArrayInputStream(b, offset, len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendToStream(OutputStream sendTo) throws IOException {
        byte[] sending;
        int sendingOffset;
        synchronized (this) {
            sending = b;
            sendingOffset = offset;
        }
        sendTo.write(sending, sendingOffset, len);
    }

    /**
     * Returns the contents of this element as a byte array. If this elements
     * was originally constructed from a intact byte array, the array returned
     * is a "shared" copy of the byte array used by this element. If this
     * element was constructed with an offset of other than zero and a length
     * different than the length of the source array then this function <b>WILL
     * RETURN A COPY OF THE BYTE ARRAY</b>.
     *
     * @return a byte array containing the contents of this element.
     */
    public byte[] getBytes() {
        return getBytes(false);
    }
}

package edu.mit.aero.foamcut;

import java.lang.reflect.Constructor;
import java.nio.*;
import java.util.Date;

/**
  @author mschafer
*/
public abstract class Message {

    public static ByteOrder s_byteOrder = ByteOrder.nativeOrder();

    public static final byte SYNC = 0x7E;

    public static final int MAX_SIZE = 128;

    public static final int LENGTH = 3;

    public static final int CRC_OFFSET = 0;

    public static final int CMD_OFFSET = 1;

    public static final int LENGTH_OFFSET = 2;

    private static Class s_dict[] = new Class[256];

    public byte m_cmd;

    public int m_length;

    public Date m_date;

    /** Creates a new instance of Message */
    public Message() {
        m_cmd = 0;
        m_length = 0;
    }

    /** Create a new instance of a Message from a ByteBuffer. */
    public Message(ByteBuffer bb) {
        bb.order(s_byteOrder);
        m_cmd = bb.get(CMD_OFFSET);
        m_length = (int) (0xFF & bb.get(LENGTH_OFFSET));
        m_date = new Date();
    }

    /** Serialize the message into a ByteBuffer for transmission. */
    public ByteBuffer serialize() {
        ByteBuffer bb = ByteBuffer.allocate(m_length);
        bb.order(s_byteOrder);
        bb.rewind();
        bb.put((byte) 0);
        bb.put(m_cmd);
        bb.put((byte) (0xFF & m_length));
        return bb;
    }

    /** Create a new message from a ByteBuffer. */
    public static Message create(ByteBuffer bb) {
        Message msg = null;
        try {
            byte[] hdr = new byte[LENGTH];
            bb.rewind();
            bb.get(hdr);
            Class msgClass = s_dict[hdr[CMD_OFFSET]];
            if (msgClass == null) {
                Logging.logger.warning("Unrecognized message " + hdr[CMD_OFFSET]);
            } else {
                Class[] cstrArgs = new Class[1];
                cstrArgs[0] = Class.forName("java.nio.ByteBuffer");
                Constructor cstr = msgClass.getConstructor(cstrArgs);
                msg = (Message) cstr.newInstance(bb);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        return msg;
    }

    /** 
     * Calculates the checksum and stores it in the proper place. 
     * @param bb A ByteBuffer with all the field values and header bytes
     * already filled in.
     */
    public static void putChecksum(ByteBuffer bb) {
        bb.position(1);
        int n = (int) (0xFF & bb.get(LENGTH_OFFSET)) - 1;
        byte ba[] = new byte[n];
        bb.get(ba);
        byte crc = Crc8.calc(ba, n);
        bb.put(0, crc);
    }

    /** Calculates the checksum of the message in the ByteBuffer and compares
     * it to the checksum value in the header.
     * @param bb A ByteBuffer containing a message.
     * @return true if the checksum is correct and false otherwise.
     */
    public static boolean verifyChecksum(ByteBuffer bb) {
        bb.position(1);
        int n = bb.limit() - 1;
        byte ba[] = new byte[n];
        bb.get(ba);
        byte crc = Crc8.calc(ba, n);
        byte v = bb.get(0);
        return (v == crc);
    }

    /**
     * Register the class in the Message dictionary.
     * @param cls
     * @param cmd
     */
    protected static void registerMsgType(Class cls, char cmd) {
        assert (s_dict[cmd] == null);
        s_dict[cmd] = cls;
    }
}

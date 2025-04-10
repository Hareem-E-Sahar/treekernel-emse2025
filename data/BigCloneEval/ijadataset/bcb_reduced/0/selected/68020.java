package org.isodl.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import net.sourceforge.scuba.smartcards.APDUWrapper;
import net.sourceforge.scuba.smartcards.ISO7816;
import net.sourceforge.scuba.tlv.BERTLVObject;
import net.sourceforge.scuba.util.Hex;

/**
 * Secure messaging wrapper for apdus.
 * Based on Section E.3 of ICAO-TR-PKI.
 * This is the same protocol as BAP configuration 1 described in ISO18013-3. 
 * 
 * TODO: make this more general (and related routines) to support BAP other configurations
 *
 * @author Cees-Bart Breunesse (ceesb@cs.ru.nl)
 * @author Martijn Oostdijk (martijno@cs.ru.nl)
 * @author Wojciech Mostowski <woj@cs.ru.nl>
 *
 */
public class SecureMessagingWrapper implements APDUWrapper {

    private static final IvParameterSpec ZERO_IV_PARAM_SPEC = new IvParameterSpec(new byte[8]);

    private SecretKey ksEnc, ksMac;

    private Cipher cipher;

    private Mac mac;

    private long ssc;

    /**
    * Constructs a secure messaging wrapper based on the secure messaging
    * session keys. The initial value of the send sequence counter is set
    * to <code>0L</code>.
    *
    * @param ksEnc the session key for encryption
    * @param ksMac the session key for macs
    *
    * @throws GeneralSecurityException when the available JCE providers
    *         cannot provide the necessary cryptographic primitives
    *         ("DESede/CBC/Nopadding" Cipher, "ISO9797Alg3Mac" Mac).
    */
    public SecureMessagingWrapper(SecretKey ksEnc, SecretKey ksMac) throws GeneralSecurityException {
        this(ksEnc, ksMac, 0L);
    }

    /**
    * Constructs a secure messaging wrapper based on the secure messaging
    * session keys and the initial value of the send sequence counter.
    *
    * @param ksEnc the session key for encryption
    * @param ksMac the session key for macs
    * @param ssc the initial value of the send sequence counter
    *
    * @throws GeneralSecurityException when the available JCE providers
    *         cannot provide the necessary cryptographic primitives
    *         ("DESede/CBC/Nopadding" Cipher, "ISO9797Alg3Mac" Mac).
    */
    public SecureMessagingWrapper(SecretKey ksEnc, SecretKey ksMac, long ssc) throws GeneralSecurityException {
        this.ksEnc = ksEnc;
        this.ksMac = ksMac;
        this.ssc = ssc;
        cipher = Cipher.getInstance("DESede/CBC/NoPadding");
        mac = Mac.getInstance("ISO9797Alg3Mac");
    }

    /**
    * Gets the current value of the send sequence counter.
    *
    * @return the current value of the send sequence counter.
    */
    public long getSendSequenceCounter() {
        return ssc;
    }

    /**
    * Wraps the apdu buffer <code>capdu</code> of a command apdu.
    * As a side effect, this method increments the internal send
    * sequence counter maintained by this wrapper.
    *
    * @param commandAPDU buffer containing the command apdu.
    *
    * @return length of the command apdu after wrapping.
    */
    public CommandAPDU wrap(CommandAPDU commandAPDU) {
        try {
            byte[] capdu = commandAPDU.getBytes();
            byte[] wrappedApdu = wrapCommandAPDU(capdu, capdu.length);
            return new CommandAPDU(wrappedApdu);
        } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
            throw new IllegalStateException(gse.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new IllegalStateException(ioe.toString());
        }
    }

    /**
    * Unwraps the apdu buffer <code>rapdu</code> of a response apdu.
    *
    * @param responseAPDU buffer containing the response apdu.
    * @param len length of the actual response apdu.
    *
    * @return a new byte array containing the unwrapped buffer.
    */
    public ResponseAPDU unwrap(ResponseAPDU responseAPDU, int len) {
        try {
            byte[] rapdu = responseAPDU.getBytes();
            return new ResponseAPDU(unwrapResponseAPDU(rapdu, len));
        } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
            throw new IllegalStateException(gse.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new IllegalStateException(ioe.toString());
        }
    }

    /**
    * Does the actual encoding of a command apdu.
    * Based on Section E.3 of ICAO-TR-PKI, especially the examples.
    * Similar examples can be found in Annex B of ISO18013-3.
    *
    * @param capdu buffer containing the apdu data. It must be large enough
    *             to receive the wrapped apdu.
    * @param len length of the apdu data.
    *
    * @return a byte array containing the wrapped apdu buffer.
    */
    private byte[] wrapCommandAPDU(byte[] capdu, int len) throws GeneralSecurityException, IOException {
        if (capdu == null || capdu.length < 4 || len < 4) {
            throw new IllegalArgumentException("Invalid type");
        }
        int lc = 0;
        int le = capdu[len - 1] & 0x000000FF;
        if (len == 4) {
            lc = 0;
            le = 0;
        } else if (len == 5) {
            lc = 0;
        } else if (len > 5) {
            lc = capdu[ISO7816.OFFSET_LC] & 0x000000FF;
        }
        if (4 + lc >= len) {
            le = 0;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] maskedHeader = new byte[4];
        System.arraycopy(capdu, 0, maskedHeader, 0, 4);
        maskedHeader[ISO7816.OFFSET_CLA] = (byte) (capdu[ISO7816.OFFSET_CLA] | 0x0C);
        byte[] paddedHeader = Util.pad(maskedHeader);
        byte[] do87 = new byte[0];
        byte[] do8E = new byte[0];
        byte[] do97 = new byte[0];
        if (le > 0) {
            out.reset();
            out.write((byte) 0x97);
            out.write((byte) 0x01);
            out.write((byte) le);
            do97 = out.toByteArray();
        }
        if (lc > 0) {
            byte[] data = Util.pad(capdu, ISO7816.OFFSET_CDATA, lc);
            cipher.init(Cipher.ENCRYPT_MODE, ksEnc, ZERO_IV_PARAM_SPEC);
            byte[] ciphertext = cipher.doFinal(data);
            out.reset();
            out.write((byte) 0x87);
            out.write(BERTLVObject.getLengthAsBytes(ciphertext.length + 1));
            out.write(0x01);
            out.write(ciphertext, 0, ciphertext.length);
            do87 = out.toByteArray();
        }
        out.reset();
        out.write(paddedHeader, 0, paddedHeader.length);
        out.write(do87, 0, do87.length);
        out.write(do97, 0, do97.length);
        byte[] m = out.toByteArray();
        out.reset();
        DataOutputStream dataOut = new DataOutputStream(out);
        ssc++;
        dataOut.writeLong(ssc);
        dataOut.write(m, 0, m.length);
        dataOut.flush();
        byte[] n = Util.pad(out.toByteArray());
        mac.init(ksMac);
        byte[] cc = mac.doFinal(n);
        out.reset();
        out.write((byte) 0x8E);
        out.write(cc.length);
        out.write(cc, 0, cc.length);
        do8E = out.toByteArray();
        out.reset();
        out.write(maskedHeader, 0, 4);
        out.write((byte) (do87.length + do97.length + do8E.length));
        out.write(do87, 0, do87.length);
        out.write(do97, 0, do97.length);
        out.write(do8E, 0, do8E.length);
        out.write(0x00);
        return out.toByteArray();
    }

    /**
    * Does the actual decoding of a response apdu.
    * Based on Section E.3 of TR-PKI, especially the examples.
    * Similar examples can be found in Annex B of ISO18013-3.
    *
    * @param rapdu buffer containing the apdu data.
    * @param len length of the apdu data.
    *
    * @return a byte array containing the unwrapped apdu buffer.
    */
    private byte[] unwrapResponseAPDU(byte[] rapdu, int len) throws GeneralSecurityException, IOException {
        long oldssc = ssc;
        try {
            if (rapdu == null || rapdu.length < 2 || len < 2) {
                throw new IllegalArgumentException("Invalid response APDU");
            }
            cipher.init(Cipher.DECRYPT_MODE, ksEnc, ZERO_IV_PARAM_SPEC);
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(rapdu));
            byte[] data = new byte[0];
            short sw = 0;
            boolean finished = false;
            byte[] cc = null;
            while (!finished) {
                int tag = in.readByte();
                switch(tag) {
                    case (byte) 0x87:
                        data = readDO87(in);
                        break;
                    case (byte) 0x99:
                        sw = readDO99(in);
                        break;
                    case (byte) 0x8E:
                        cc = readDO8E(in);
                        finished = true;
                        break;
                }
            }
            if (!checkMac(rapdu, cc)) {
                throw new IllegalStateException("Invalid MAC");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(data, 0, data.length);
            out.write((sw & 0x0000FF00) >> 8);
            out.write(sw & 0x000000FF);
            return out.toByteArray();
        } finally {
            if (ssc == oldssc) {
                ssc++;
            }
        }
    }

    /**
    * The <code>0x87</code> tag has already been read.
    *
    * @param in inputstream to read from.
    */
    private byte[] readDO87(DataInputStream in) throws IOException, GeneralSecurityException {
        int length = 0;
        int buf = in.readUnsignedByte();
        if ((buf & 0x00000080) != 0x00000080) {
            length = buf;
            buf = in.readUnsignedByte();
            if (buf != 0x01) {
                throw new IllegalStateException("DO'87 expected 0x01 marker, found " + Hex.byteToHexString((byte) buf));
            }
        } else {
            int lengthBytesCount = buf & 0x0000007F;
            for (int i = 0; i < lengthBytesCount; i++) {
                length = (length << 8) | in.readUnsignedByte();
            }
            buf = in.readUnsignedByte();
            if (buf != 0x01) {
                throw new IllegalStateException("DO'87 expected 0x01 marker");
            }
        }
        length--;
        byte[] ciphertext = new byte[length];
        in.read(ciphertext, 0, length);
        byte[] paddedData = cipher.doFinal(ciphertext);
        byte[] data = Util.unpad(paddedData);
        return data;
    }

    /**
    * The <code>0x99</code> tag has already been read.
    *
    * @param in inputstream to read from.
    */
    private short readDO99(DataInputStream in) throws IOException {
        int length = in.readUnsignedByte();
        if (length != 2) {
            throw new IllegalStateException("DO'99 wrong length");
        }
        byte sw1 = in.readByte();
        byte sw2 = in.readByte();
        return (short) (((sw1 & 0x000000FF) << 8) | (sw2 & 0x000000FF));
    }

    /**
    * The <code>0x8E</code> tag has already been read.
    *
    * @param in inputstream to read from.
    */
    private byte[] readDO8E(DataInputStream in) throws IOException, GeneralSecurityException {
        int length = in.readUnsignedByte();
        if (length != 8) {
            throw new IllegalStateException("DO'8E wrong length");
        }
        byte[] cc1 = new byte[8];
        in.readFully(cc1);
        return cc1;
    }

    private boolean checkMac(byte[] rapdu, byte[] cc1) throws GeneralSecurityException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(out);
            ssc++;
            dataOut.writeLong(ssc);
            byte[] paddedData = Util.pad(rapdu, 0, rapdu.length - 2 - 8 - 2);
            dataOut.write(paddedData, 0, paddedData.length);
            dataOut.flush();
            mac.init(ksMac);
            byte[] cc2 = mac.doFinal(out.toByteArray());
            dataOut.close();
            return Arrays.equals(cc1, cc2);
        } catch (IOException ioe) {
            return false;
        }
    }
}

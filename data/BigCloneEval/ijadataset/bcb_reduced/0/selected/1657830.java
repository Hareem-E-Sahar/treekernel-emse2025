package ch.ethz.ssh2.crypto.digest;

import java.math.BigInteger;

/**
 * HashForSSH2Types.
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id: HashForSSH2Types.java,v 1.3 2005/08/12 23:37:18 cplattne Exp $
 */
public class HashForSSH2Types {

    Digest md;

    public HashForSSH2Types(Digest md) {
        this.md = md;
    }

    public HashForSSH2Types(String type) {
        if (type.equals("SHA1")) {
            md = new SHA1();
        } else if (type.equals("MD5")) {
            md = new MD5();
        } else throw new IllegalArgumentException("Unknown algorithm " + type);
    }

    public void updateByte(byte b) {
        byte[] tmp = new byte[1];
        tmp[0] = b;
        md.update(tmp);
    }

    public void updateBytes(byte[] b) {
        md.update(b);
    }

    public void updateUINT32(int v) {
        md.update((byte) (v >> 24));
        md.update((byte) (v >> 16));
        md.update((byte) (v >> 8));
        md.update((byte) (v));
    }

    public void updateByteString(byte[] b) {
        updateUINT32(b.length);
        updateBytes(b);
    }

    public void updateBigInt(BigInteger b) {
        updateByteString(b.toByteArray());
    }

    public void reset() {
        md.reset();
    }

    public int getDigestLength() {
        return md.getDigestLength();
    }

    public byte[] getDigest() {
        byte[] tmp = new byte[md.getDigestLength()];
        getDigest(tmp);
        return tmp;
    }

    public void getDigest(byte[] out) {
        getDigest(out, 0);
    }

    public void getDigest(byte[] out, int off) {
        md.digest(out, off);
    }
}

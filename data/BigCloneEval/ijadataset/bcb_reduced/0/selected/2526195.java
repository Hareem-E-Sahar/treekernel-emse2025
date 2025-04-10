package freenet.crypt;

import java.io.*;
import freenet.Presentation;
import freenet.support.FileBucket;
import freenet.support.io.*;

/**
 * A progressive hash stream is a stream of data where each part is preceded
 * by a hash of that part AND the hash of the next part. This means
 * that only the hash of the first part (and second hash) needs to be known,
 * but one can be sure that each part is valid after reading it.
 *
 * This class provides a VerifyingInputStream that verifies each part of the
 * hash on reading.
 *
 * The design of the progressive hash as used in Freenet is taken from:
 * Gennaro, R and Rohatgi, P; "How to Sign Digital Streams",
 * Advances in Cryptology - CRYPTO '97, 1997.
 *
 * @author oskar
 **/
public class ProgressiveHashInputStream extends VerifyingInputStream {

    public static void main(String[] args) throws Exception {
        File data = new File(args[0]);
        FileInputStream fin = new FileInputStream(data);
        long partSize = Long.parseLong(args[1]);
        ProgressiveHashOutputStream hout = new ProgressiveHashOutputStream(partSize, new SHA1Factory(), new FileBucket());
        long left = data.length();
        byte[] buf = new byte[4096];
        while (left > 0) {
            int n = fin.read(buf, 0, (int) Math.min(left, buf.length));
            if (n == -1) throw new EOFException("EOF while reading input file");
            hout.write(buf, 0, n);
            left -= n;
        }
        hout.close();
        byte[] init = hout.getInitialDigest();
        for (int i = 0; i < init.length; ++i) System.err.print((init[i] & 0xff) + " ");
        long totalLength = hout.getLength();
        System.err.println();
        System.err.println("TotalLength = " + totalLength);
        InputStream in = hout.getInputStream();
        VerifyingInputStream vin = new ProgressiveHashInputStream(in, partSize, totalLength, SHA1.getInstance(), init);
        vin.stripControls(args.length < 3 || Integer.parseInt(args[2]) == 0 ? true : false);
        int i = vin.read(buf);
        while (i > 0) {
            System.out.write(buf, 0, i);
            i = vin.read(buf);
        }
    }

    private long partSize;

    private long pos = 0;

    private Digest ctx;

    private int ds;

    private byte[] expectedHash;

    private byte[] controlBuf;

    private DataNotValidIOException dnv;

    /**
     * Create a new InputStream that verifies a stream of Serially hashed data
     * @param in             The inputstream to read.
     * @param partSize       The amount of data preceding each digest value and
     *                       control character.
     * @param dataLength     The total length of the data.
     * @param ctx            A digest of the type needed.
     * @param initialDigest  The Digest value to expect for the first part (and
     *                       second digest value). The length of the digest bytes
     *                       will be copied starting from the first.
     * @exception DataNotValidIOException is thrown if the partSize combined
     *            with the datalength produces an impossible EOF.
     **/
    public ProgressiveHashInputStream(InputStream in, long partSize, long dataLength, Digest ctx, byte[] initialDigest) throws DataNotValidIOException {
        super(in, dataLength);
        ds = ctx.digestSize() >> 3;
        int parts = (int) (dataLength / (partSize + ds + 1));
        long lastPart = dataLength - parts * (partSize + ds + 1);
        if (dataLength < 2 || lastPart < 2 || partSize <= 0 || partSize > dataLength - 1 || lastPart > partSize + 1) throw new DataNotValidIOException(Presentation.CB_BAD_KEY);
        this.partSize = partSize;
        this.ctx = ctx;
        this.expectedHash = new byte[ds];
        this.controlBuf = new byte[ds + 1];
        System.arraycopy(initialDigest, 0, expectedHash, 0, ds);
    }

    public int read() throws DataNotValidIOException, IOException {
        if (dnv != null) throw dnv;
        if (pos < 0) return (int) controlBuf[controlBuf.length + (int) pos] & 0xff;
        int b = super.read();
        if (b == -1) return -1;
        ctx.update((byte) b);
        ++pos;
        if (pos == partSize || allRead) readControlBytes();
        return b;
    }

    public int read(byte[] buf, int off, int len) throws DataNotValidIOException, IOException {
        if (dnv != null) throw dnv;
        if (len <= 0) return 0;
        if (pos < 0) {
            int n = (int) Math.min(len, 0 - (int) pos);
            System.arraycopy(controlBuf, controlBuf.length + (int) pos, buf, off, n);
            pos += n;
            return n;
        }
        len = (int) Math.min(len, partSize - pos);
        int n = super.read(buf, off, len);
        if (n == -1) return -1;
        ctx.update(buf, off, n);
        pos += n;
        if (pos == partSize || allRead) readControlBytes();
        return n;
    }

    private void readControlBytes() throws DataNotValidIOException, IOException {
        int togo = (allRead ? 1 : ds + 1);
        pos = (stripControls ? 0 : 0 - togo);
        int b = 0;
        while (togo > 0) {
            b = super.read();
            if (b == -1) throw new EOFException("EOF while reading control bytes");
            controlBuf[controlBuf.length - togo] = (byte) b;
            if (togo != 1) ctx.update((byte) b);
            --togo;
        }
        if (b != Presentation.CB_OK || !Util.byteArrayEqual(ctx.digest(), expectedHash)) {
            dnv = new DataNotValidIOException(b == Presentation.CB_OK ? Presentation.CB_BAD_DATA : b);
            throw dnv;
        }
        System.arraycopy(controlBuf, 0, expectedHash, 0, ds);
    }

    public void finish() throws DataNotValidIOException, IOException {
        finished = true;
    }
}

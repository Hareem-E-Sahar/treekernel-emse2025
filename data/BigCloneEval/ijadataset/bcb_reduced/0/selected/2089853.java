package gnu.javax.crypto.cipher;

import gnu.java.security.Registry;
import gnu.java.security.util.Util;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Square is a 128-bit key, 128-bit block cipher algorithm developed by Joan
 * Daemen, Lars Knudsen and Vincent Rijmen.
 * <p>
 * References:
 * <ol>
 * <li><a href="http://www.esat.kuleuven.ac.be/~rijmen/square/">The block
 * cipher Square</a>.<br>
 * <a href="mailto:daemen.j@protonworld.com">Joan Daemen</a>, <a
 * href="mailto:lars.knudsen@esat.kuleuven.ac.be">Lars Knudsen</a> and <a
 * href="mailto:vincent.rijmen@esat.kuleuven.ac.be">Vincent Rijmen</a>.</li>
 * </ol>
 */
public final class Square extends BaseCipher {

    private static final int DEFAULT_BLOCK_SIZE = 16;

    private static final int DEFAULT_KEY_SIZE = 16;

    private static final int ROUNDS = 8;

    private static final int ROOT = 0x1F5;

    private static final int[] OFFSET = new int[ROUNDS];

    private static final String Sdata = "뇎쎕媭䵄ﮑಇꅐ" + "쭧哝䚏ﳫ龍ᩮ" + "廵첍᱖䏾ݡ姿̢" + "諑Ꮾ蠀ิᖀ铣匣" + "䭇ឧ逵ꯘ룟佗骒?" + "㳈餄軠흽薻䀬㩅" + "攠䄘爥鍰㘅ꍹ" + "✱㊶粰ੳ孻랁툍樦" + "鹘鲃璳갰穩眏긡?" + "⺗Ⴄ風푨ⵢ⥭ᙉ盇" + "阷挒슦ᒼ패" + "꼯勆ꀉ붌콝ᅟǅ" + "鼽ꊛ줻빑᤟㽜닯䫍" + "뾺潤?㺴ꫜ픆쁾" + "沄焸뤝羝䢋⫚ꔳ船" + "홸蛺ꤞ襠毪啌";

    /** Substitution boxes for encryption and decryption. */
    private static final byte[] Se = new byte[256];

    private static final byte[] Sd = new byte[256];

    /** Transposition boxes for encryption and decryption. */
    private static final int[] Te = new int[256];

    private static final int[] Td = new int[256];

    /**
   * KAT vector (from ecb_vk): I=87 KEY=00000000000000000000020000000000
   * CT=A9DF031B4E25E89F527EFFF89CB0BEBA
   */
    private static final byte[] KAT_KEY = Util.toBytesFromString("00000000000000000000020000000000");

    private static final byte[] KAT_CT = Util.toBytesFromString("A9DF031B4E25E89F527EFFF89CB0BEBA");

    /** caches the result of the correctness test, once executed. */
    private static Boolean valid;

    static {
        int i, j;
        int limit = Sdata.length();
        char c1;
        for (i = 0, j = 0; i < limit; i++) {
            c1 = Sdata.charAt(i);
            Se[j++] = (byte) (c1 >>> 8);
            Se[j++] = (byte) c1;
        }
        for (i = 0; i < 256; i++) Sd[Se[i] & 0xFF] = (byte) i;
        OFFSET[0] = 1;
        for (i = 1; i < ROUNDS; i++) {
            OFFSET[i] = mul(OFFSET[i - 1], 2);
            OFFSET[i - 1] <<= 24;
        }
        OFFSET[ROUNDS - 1] <<= 24;
        for (i = 0; i < 256; i++) {
            j = Se[i] & 0xFF;
            Te[i] = (Se[i & 3] == 0) ? 0 : mul(j, 2) << 24 | j << 16 | j << 8 | mul(j, 3);
            j = Sd[i] & 0xFF;
            Td[i] = (Sd[i & 3] == 0) ? 0 : mul(j, 14) << 24 | mul(j, 9) << 16 | mul(j, 13) << 8 | mul(j, 11);
        }
    }

    /** Trivial 0-arguments constructor. */
    public Square() {
        super(Registry.SQUARE_CIPHER, DEFAULT_BLOCK_SIZE, DEFAULT_KEY_SIZE);
    }

    private static void square(byte[] in, int i, byte[] out, int j, int[][] K, int[] T, byte[] S) {
        int a = ((in[i++]) << 24 | (in[i++] & 0xFF) << 16 | (in[i++] & 0xFF) << 8 | (in[i++] & 0xFF)) ^ K[0][0];
        int b = ((in[i++]) << 24 | (in[i++] & 0xFF) << 16 | (in[i++] & 0xFF) << 8 | (in[i++] & 0xFF)) ^ K[0][1];
        int c = ((in[i++]) << 24 | (in[i++] & 0xFF) << 16 | (in[i++] & 0xFF) << 8 | (in[i++] & 0xFF)) ^ K[0][2];
        int d = ((in[i++]) << 24 | (in[i++] & 0xFF) << 16 | (in[i++] & 0xFF) << 8 | (in[i] & 0xFF)) ^ K[0][3];
        int r, aa, bb, cc, dd;
        for (r = 1; r < ROUNDS; r++) {
            aa = T[(a >>> 24)] ^ rot32R(T[(b >>> 24)], 8) ^ rot32R(T[(c >>> 24)], 16) ^ rot32R(T[(d >>> 24)], 24) ^ K[r][0];
            bb = T[(a >>> 16) & 0xFF] ^ rot32R(T[(b >>> 16) & 0xFF], 8) ^ rot32R(T[(c >>> 16) & 0xFF], 16) ^ rot32R(T[(d >>> 16) & 0xFF], 24) ^ K[r][1];
            cc = T[(a >>> 8) & 0xFF] ^ rot32R(T[(b >>> 8) & 0xFF], 8) ^ rot32R(T[(c >>> 8) & 0xFF], 16) ^ rot32R(T[(d >>> 8) & 0xFF], 24) ^ K[r][2];
            dd = T[a & 0xFF] ^ rot32R(T[b & 0xFF], 8) ^ rot32R(T[c & 0xFF], 16) ^ rot32R(T[d & 0xFF], 24) ^ K[r][3];
            a = aa;
            b = bb;
            c = cc;
            d = dd;
        }
        aa = ((S[(a >>> 24)]) << 24 | (S[(b >>> 24)] & 0xFF) << 16 | (S[(c >>> 24)] & 0xFF) << 8 | (S[(d >>> 24)] & 0xFF)) ^ K[r][0];
        bb = ((S[(a >>> 16) & 0xFF]) << 24 | (S[(b >>> 16) & 0xFF] & 0xFF) << 16 | (S[(c >>> 16) & 0xFF] & 0xFF) << 8 | (S[(d >>> 16) & 0xFF] & 0xFF)) ^ K[r][1];
        cc = ((S[(a >>> 8) & 0xFF]) << 24 | (S[(b >>> 8) & 0xFF] & 0xFF) << 16 | (S[(c >>> 8) & 0xFF] & 0xFF) << 8 | (S[(d >>> 8) & 0xFF] & 0xFF)) ^ K[r][2];
        dd = ((S[a & 0xFF]) << 24 | (S[b & 0xFF] & 0xFF) << 16 | (S[c & 0xFF] & 0xFF) << 8 | (S[d & 0xFF] & 0xFF)) ^ K[r][3];
        out[j++] = (byte) (aa >>> 24);
        out[j++] = (byte) (aa >>> 16);
        out[j++] = (byte) (aa >>> 8);
        out[j++] = (byte) aa;
        out[j++] = (byte) (bb >>> 24);
        out[j++] = (byte) (bb >>> 16);
        out[j++] = (byte) (bb >>> 8);
        out[j++] = (byte) bb;
        out[j++] = (byte) (cc >>> 24);
        out[j++] = (byte) (cc >>> 16);
        out[j++] = (byte) (cc >>> 8);
        out[j++] = (byte) cc;
        out[j++] = (byte) (dd >>> 24);
        out[j++] = (byte) (dd >>> 16);
        out[j++] = (byte) (dd >>> 8);
        out[j] = (byte) dd;
    }

    /**
   * Applies the Theta function to an input <i>in</i> in order to produce in
   * <i>out</i> an internal session sub-key.
   * <p>
   * Both <i>in</i> and <i>out</i> are arrays of four ints.
   * <p>
   * Pseudo-code is:
   * <pre>
   * for (i = 0; i &lt; 4; i++)
   *   {
   *     out[i] = 0;
   *     for (j = 0, n = 24; j &lt; 4; j++, n -= 8)
   *       {
   *         k = mul(in[i] &gt;&gt;&gt; 24, G[0][j]) &circ; mul(in[i] &gt;&gt;&gt; 16, G[1][j])
   *             &circ; mul(in[i] &gt;&gt;&gt; 8, G[2][j]) &circ; mul(in[i], G[3][j]);
   *         out[i] &circ;= k &lt;&lt; n;
   *       }
   *   }
   * </pre>
   */
    private static void transform(int[] in, int[] out) {
        int l3, l2, l1, l0, m;
        for (int i = 0; i < 4; i++) {
            l3 = in[i];
            l2 = l3 >>> 8;
            l1 = l3 >>> 16;
            l0 = l3 >>> 24;
            m = ((mul(l0, 2) ^ mul(l1, 3) ^ l2 ^ l3) & 0xFF) << 24;
            m ^= ((l0 ^ mul(l1, 2) ^ mul(l2, 3) ^ l3) & 0xFF) << 16;
            m ^= ((l0 ^ l1 ^ mul(l2, 2) ^ mul(l3, 3)) & 0xFF) << 8;
            m ^= ((mul(l0, 3) ^ l1 ^ l2 ^ mul(l3, 2)) & 0xFF);
            out[i] = m;
        }
    }

    /**
   * Left rotate a 32-bit chunk.
   * 
   * @param x the 32-bit data to rotate
   * @param s number of places to left-rotate by
   * @return the newly permutated value.
   */
    private static int rot32L(int x, int s) {
        return x << s | x >>> (32 - s);
    }

    /**
   * Right rotate a 32-bit chunk.
   * 
   * @param x the 32-bit data to rotate
   * @param s number of places to right-rotate by
   * @return the newly permutated value.
   */
    private static int rot32R(int x, int s) {
        return x >>> s | x << (32 - s);
    }

    /**
   * Returns the product of two binary numbers a and b, using the generator ROOT
   * as the modulus: p = (a * b) mod ROOT. ROOT Generates a suitable Galois
   * Field in GF(2**8).
   * <p>
   * For best performance call it with abs(b) &lt; abs(a).
   * 
   * @param a operand for multiply.
   * @param b operand for multiply.
   * @return the result of (a * b) % ROOT.
   */
    private static final int mul(int a, int b) {
        if (a == 0) return 0;
        a &= 0xFF;
        b &= 0xFF;
        int result = 0;
        while (b != 0) {
            if ((b & 0x01) != 0) result ^= a;
            b >>>= 1;
            a <<= 1;
            if (a > 0xFF) a ^= ROOT;
        }
        return result & 0xFF;
    }

    public Object clone() {
        Square result = new Square();
        result.currentBlockSize = this.currentBlockSize;
        return result;
    }

    public Iterator blockSizes() {
        ArrayList al = new ArrayList();
        al.add(Integer.valueOf(DEFAULT_BLOCK_SIZE));
        return Collections.unmodifiableList(al).iterator();
    }

    public Iterator keySizes() {
        ArrayList al = new ArrayList();
        al.add(Integer.valueOf(DEFAULT_KEY_SIZE));
        return Collections.unmodifiableList(al).iterator();
    }

    public Object makeKey(byte[] uk, int bs) throws InvalidKeyException {
        if (bs != DEFAULT_BLOCK_SIZE) throw new IllegalArgumentException();
        if (uk == null) throw new InvalidKeyException("Empty key");
        if (uk.length != DEFAULT_KEY_SIZE) throw new InvalidKeyException("Key is not 128-bit.");
        int[][] Ke = new int[ROUNDS + 1][4];
        int[][] Kd = new int[ROUNDS + 1][4];
        int[][] tK = new int[ROUNDS + 1][4];
        int i = 0;
        Ke[0][0] = (uk[i++] & 0xFF) << 24 | (uk[i++] & 0xFF) << 16 | (uk[i++] & 0xFF) << 8 | (uk[i++] & 0xFF);
        tK[0][0] = Ke[0][0];
        Ke[0][1] = (uk[i++] & 0xFF) << 24 | (uk[i++] & 0xFF) << 16 | (uk[i++] & 0xFF) << 8 | (uk[i++] & 0xFF);
        tK[0][1] = Ke[0][1];
        Ke[0][2] = (uk[i++] & 0xFF) << 24 | (uk[i++] & 0xFF) << 16 | (uk[i++] & 0xFF) << 8 | (uk[i++] & 0xFF);
        tK[0][2] = Ke[0][2];
        Ke[0][3] = (uk[i++] & 0xFF) << 24 | (uk[i++] & 0xFF) << 16 | (uk[i++] & 0xFF) << 8 | (uk[i] & 0xFF);
        tK[0][3] = Ke[0][3];
        int j;
        for (i = 1, j = 0; i < ROUNDS + 1; i++, j++) {
            tK[i][0] = tK[j][0] ^ rot32L(tK[j][3], 8) ^ OFFSET[j];
            tK[i][1] = tK[j][1] ^ tK[i][0];
            tK[i][2] = tK[j][2] ^ tK[i][1];
            tK[i][3] = tK[j][3] ^ tK[i][2];
            System.arraycopy(tK[i], 0, Ke[i], 0, 4);
            transform(Ke[j], Ke[j]);
        }
        for (i = 0; i < ROUNDS; i++) System.arraycopy(tK[ROUNDS - i], 0, Kd[i], 0, 4);
        transform(tK[0], Kd[ROUNDS]);
        return new Object[] { Ke, Kd };
    }

    public void encrypt(byte[] in, int i, byte[] out, int j, Object k, int bs) {
        if (bs != DEFAULT_BLOCK_SIZE) throw new IllegalArgumentException();
        int[][] K = (int[][]) ((Object[]) k)[0];
        square(in, i, out, j, K, Te, Se);
    }

    public void decrypt(byte[] in, int i, byte[] out, int j, Object k, int bs) {
        if (bs != DEFAULT_BLOCK_SIZE) throw new IllegalArgumentException();
        int[][] K = (int[][]) ((Object[]) k)[1];
        square(in, i, out, j, K, Td, Sd);
    }

    public boolean selfTest() {
        if (valid == null) {
            boolean result = super.selfTest();
            if (result) result = testKat(KAT_KEY, KAT_CT);
            valid = Boolean.valueOf(result);
        }
        return valid.booleanValue();
    }
}

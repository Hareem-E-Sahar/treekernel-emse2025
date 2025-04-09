package wtanaka.praya.gale;

import java.math.BigInteger;
import java.util.Random;

/**
 * Generator for keys.  Based on openssl 0.9.6b written by the OpenSSL
 * Project.
 *
 * <p>
 * Return to <A href="http://sourceforge.net/projects/praya/">
 * <IMG src="http://sourceforge.net/sflogo.php?group_id=2302&type=1"
 *   alt="Sourceforge" width="88" height="31" border="0"></A>
 * or the <a href="http://praya.sourceforge.net/">Praya Homepage</a>
 *
 * @concurrency SERIAL this class is not thread safe.  Callers must
 * ensure that nobody else is calling a method on this class at the
 * same time.
 *
 * @author $Author: wtanaka $
 * @version $Name:  $ $Date: 2002/01/27 20:22:17 $
 **/
public class GaleKeyGen {

    public static final int MIN_RSA_MODULUS_BITS = 508;

    public static final int MAX_RSA_MODULUS_BITS = 1024;

    public static final int MAX_RSA_MODULUS_LEN = ((MAX_RSA_MODULUS_BITS + 7) / 8);

    public static final int MAX_RSA_PRIME_BITS = ((MAX_RSA_MODULUS_BITS + 1) / 2);

    private static final long RSA_F4 = 0x10001L;

    private GalePublicKey m_publicKey;

    private GalePrivateKey m_privateKey;

    private int pad;

    private int version;

    private BigInteger n;

    private BigInteger e;

    private BigInteger d;

    private BigInteger p;

    private BigInteger q;

    private BigInteger dmp1;

    private BigInteger dmq1;

    private BigInteger iqmp;

    int references;

    int flags;

    /**
    * Convenient method to generate a new key with certainty of
    * primality equal to 1 - 1/2**1024.
    * @param id the name to embed in the key.
    * @param bits must be between MIN_RSA_MODULUS_BITS and
    * MAX_RSA_MODULUS_BITS, inclusive.  If this parameter is set to 0,
    * this method will assume the default value of 768.
    * @param owner the owner string for this key
    * @param random the source of random numbers.
    **/
    public void generate(String id, String owner, int bits, Random random) throws IllegalArgumentException {
        generate(id, owner, bits, 1024, random);
    }

    /**
    * Generate a new key.  Port of gale_crypto_generate from gale.
    * @param id the name to embed in the key in OWO order.  This name
    * should have the most specific part first.  e.g.
    * mail.jtr@ofb.net or *.pub@ofb.net.
    * @param bits must be between MIN_RSA_MODULUS_BITS and
    * MAX_RSA_MODULUS_BITS, inclusive.  If this parameter is set to 0,
    * this method will assume the default value of 768.
    * @param owner the owner string for this key
    * @param certainty a measure of the uncertainty that the caller is
    * willing to tolerate: the probability that the number is prime
    * will exceed 1 - 1/2**certainty. The execution time is
    * proportional to the value of the certainty parameter. 
    * @param random the source of random numbers.
    **/
    public void generate(String id, String owner, int bits, int certainty, Random random) throws IllegalArgumentException {
        if (bits <= 0) bits = 768;
        if (bits > MAX_RSA_MODULUS_BITS) {
            throw new IllegalArgumentException("specified bits " + bits + " is larger than maximum: " + MAX_RSA_MODULUS_BITS);
        } else if (bits < MIN_RSA_MODULUS_BITS) {
            throw new IllegalArgumentException("specified bits " + bits + " is smaller than minimum: " + MIN_RSA_MODULUS_BITS);
        }
        RSA_generate_key(bits, RSA_F4, null, null, certainty, random);
        m_privateKey = new GalePrivateKey(id, bits, n, e, d, p, q, dmp1, dmq1, iqmp);
        m_publicKey = new GalePublicKey(owner, id, bits, n, e);
    }

    /**
    * Ported from openssl 0.9.6b crypto/rsa/rsa_gen.c.
    * http://www.openssl.org/source/cvs/crypto/rsa/rsa_gen.c
    **/
    private void RSA_generate_key(int bits, long e_value, Callback callback, Object cb_arg, int certainty, Random random) {
        BigInteger r0 = null, r1 = null, r2 = null, r3 = null;
        int bitsp, bitsq, ok = -1, i;
        int localN = 0;
        bitsp = (bits + 1) / 2;
        bitsq = bits - bitsp;
        e = BigInteger.valueOf(e_value);
        for (; ; ) {
            p = new BigInteger(bitsp, certainty, random);
            r2 = p.subtract(BigInteger.valueOf(1));
            r1 = r2.gcd(e);
            if (r1.equals(BigInteger.valueOf(1))) break;
            if (callback != null) callback.callback(2, localN++, cb_arg);
            p = null;
        }
        if (callback != null) callback.callback(3, 0, cb_arg);
        for (; ; ) {
            q = new BigInteger(bitsq, certainty, random);
            r2 = q.subtract(BigInteger.valueOf(1));
            r1 = r2.gcd(e);
            if (r1.equals(BigInteger.valueOf(1)) && p.compareTo(q) != 0) break;
            if (callback != null) callback.callback(2, localN++, cb_arg);
            q = null;
        }
        if (callback != null) callback.callback(3, 1, cb_arg);
        if (p.compareTo(q) < 0) {
            BigInteger tmp = p;
            p = q;
            q = tmp;
        }
        n = p.multiply(q);
        r1 = p.subtract(BigInteger.valueOf(1));
        r2 = q.subtract(BigInteger.valueOf(1));
        r0 = r1.multiply(r2);
        d = e.modInverse(r0);
        dmp1 = d.mod(r1);
        dmq1 = d.mod(r2);
        iqmp = q.modInverse(p);
    }

    public static class Callback {

        public void callback(int a, int b, Object o) {
            System.err.println(a + ", " + b + ": " + o);
        }
    }

    public GalePublicKey getPublicKey() {
        return m_publicKey;
    }

    public GalePrivateKey getPrivateKey() {
        return m_privateKey;
    }
}

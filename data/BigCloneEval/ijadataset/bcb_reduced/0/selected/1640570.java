package gnu.java.security.key.rsa;

import gnu.java.security.Configuration;
import gnu.java.security.Registry;
import gnu.java.security.key.IKeyPairGenerator;
import gnu.java.security.util.PRNG;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A key-pair generator for asymetric keys to use in conjunction with the RSA
 * scheme.
 * <p>
 * Reference:
 * <ol>
 * <li><a
 * href="http://www.cosic.esat.kuleuven.ac.be/nessie/workshop/submissions/rsa-pss.zip">
 * RSA-PSS Signature Scheme with Appendix</a>, part B. Primitive specification
 * and supporting documentation. Jakob Jonsson and Burt Kaliski. </li>
 * <li><a href="http://www.cacr.math.uwaterloo.ca/hac/">Handbook of Applied
 * Cryptography</a>, Alfred J. Menezes, Paul C. van Oorschot and Scott A.
 * Vanstone. Section 11.3 RSA and related signature schemes.</li>
 * </ol>
 */
public class RSAKeyPairGenerator implements IKeyPairGenerator {

    private static final Logger log = Logger.getLogger(RSAKeyPairGenerator.class.getName());

    /** The BigInteger constant 1. */
    private static final BigInteger ONE = BigInteger.ONE;

    /** The BigInteger constant 2. */
    private static final BigInteger TWO = BigInteger.valueOf(2L);

    /** Property name of the length (Integer) of the modulus of an RSA key. */
    public static final String MODULUS_LENGTH = "gnu.crypto.rsa.L";

    /**
   * Property name of an optional {@link SecureRandom} instance to use. The
   * default is to use a classloader singleton from {@link PRNG}.
   */
    public static final String SOURCE_OF_RANDOMNESS = "gnu.crypto.rsa.prng";

    /**
   * Property name of an optional {@link RSAKeyGenParameterSpec} instance to use
   * for this generator's <code>n</code>, and <code>e</code> values. The
   * default is to generate <code>n</code> and use a fixed value for
   * <code>e</.code> (Fermat's F4 number).
   */
    public static final String RSA_PARAMETERS = "gnu.crypto.rsa.params";

    /**
   * Property name of the preferred encoding format to use when externalizing
   * generated instance of key-pairs from this generator. The property is taken
   * to be an {@link Integer} that encapsulates an encoding format identifier.
   */
    public static final String PREFERRED_ENCODING_FORMAT = "gnu.crypto.rsa.encoding";

    /** Default value for the modulus length. */
    private static final int DEFAULT_MODULUS_LENGTH = 1024;

    /** Default encoding format to use when none was specified. */
    private static final int DEFAULT_ENCODING_FORMAT = Registry.RAW_ENCODING_ID;

    /** The desired bit length of the modulus. */
    private int L;

    /**
   * This implementation uses, by default, Fermat's F4 number as the public
   * exponent.
   */
    private BigInteger e = BigInteger.valueOf(65537L);

    /** The optional {@link SecureRandom} instance to use. */
    private SecureRandom rnd = null;

    /** Our default source of randomness. */
    private PRNG prng = null;

    /** Preferred encoding format of generated keys. */
    private int preferredFormat;

    public String name() {
        return Registry.RSA_KPG;
    }

    /**
   * Configures this instance.
   * 
   * @param attributes the map of name/value pairs to use.
   * @exception IllegalArgumentException if the designated MODULUS_LENGTH value
   *              is less than 1024.
   */
    public void setup(Map attributes) {
        if (Configuration.DEBUG) log.entering(this.getClass().getName(), "setup", attributes);
        rnd = (SecureRandom) attributes.get(SOURCE_OF_RANDOMNESS);
        RSAKeyGenParameterSpec params = (RSAKeyGenParameterSpec) attributes.get(RSA_PARAMETERS);
        if (params != null) {
            L = params.getKeysize();
            e = params.getPublicExponent();
        } else {
            Integer l = (Integer) attributes.get(MODULUS_LENGTH);
            L = (l == null ? DEFAULT_MODULUS_LENGTH : l.intValue());
        }
        if (L < 1024) throw new IllegalArgumentException(MODULUS_LENGTH);
        Integer formatID = (Integer) attributes.get(PREFERRED_ENCODING_FORMAT);
        preferredFormat = formatID == null ? DEFAULT_ENCODING_FORMAT : formatID.intValue();
        if (Configuration.DEBUG) log.exiting(this.getClass().getName(), "setup");
    }

    /**
   * <p>
   * The algorithm used here is described in <i>nessie-pss-B.pdf</i> document
   * which is part of the RSA-PSS submission to NESSIE.
   * </p>
   * 
   * @return an RSA keypair.
   */
    public KeyPair generate() {
        if (Configuration.DEBUG) log.entering(this.getClass().getName(), "generate");
        BigInteger p, q, n, d;
        int M = (L + 1) / 2;
        BigInteger lower = TWO.pow(M - 1);
        BigInteger upper = TWO.pow(M).subtract(ONE);
        byte[] kb = new byte[(M + 7) / 8];
        step1: while (true) {
            nextRandomBytes(kb);
            p = new BigInteger(1, kb).setBit(0);
            if (p.compareTo(lower) >= 0 && p.compareTo(upper) <= 0 && p.isProbablePrime(80) && p.gcd(e).equals(ONE)) break step1;
        }
        step2: while (true) {
            nextRandomBytes(kb);
            q = new BigInteger(1, kb).setBit(0);
            n = p.multiply(q);
            if (n.bitLength() == L && q.isProbablePrime(80) && q.gcd(e).equals(ONE)) break step2;
        }
        BigInteger phi = p.subtract(ONE).multiply(q.subtract(ONE));
        d = e.modInverse(phi);
        PublicKey pubK = new GnuRSAPublicKey(preferredFormat, n, e);
        PrivateKey secK = new GnuRSAPrivateKey(preferredFormat, p, q, e, d);
        KeyPair result = new KeyPair(pubK, secK);
        if (Configuration.DEBUG) log.exiting(this.getClass().getName(), "generate", result);
        return result;
    }

    /**
   * Fills the designated byte array with random data.
   * 
   * @param buffer the byte array to fill with random data.
   */
    private void nextRandomBytes(byte[] buffer) {
        if (rnd != null) rnd.nextBytes(buffer); else getDefaultPRNG().nextBytes(buffer);
    }

    private PRNG getDefaultPRNG() {
        if (prng == null) prng = PRNG.getInstance();
        return prng;
    }
}

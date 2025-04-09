package cdc.standard.rsa;

import java.math.BigInteger;
import java.security.*;

/**
* This class extends the KeyPairGenerator class. The key generation
* follows the PKCS#1 standard. The bit-length of n = p*q is the value of the variable strength.
* The key pair consists of a RSAPubKey and a RSAPrivCrtKey.
*
* The default bit-length of n is 1024 bits.
* <p>
* @author
* <a href="mailto:twahren@cdc.informatik.tu-darmstadt.de">Thomas Wahrenbruch</a>
* @version 0.61
* @see cdc.standard.rsa.RSAPubKey
* @see cdc.standard.rsa.RSAPrivCrtKey
*/
public class RSAKeyPairGenerator extends KeyPairGenerator {

    /**
	* The certainty, that the generated numbers are prime.
	*/
    private final int CERTAINTY = 80;

    /**
	* Default encryption exponent. For RSA signatures this
	* is the exponent used in the verification process. 
	*/
    private final int DEFAULT_EXPONENT = 65537;

    /**
	* The source of randomness.
	*/
    private SecureRandom secureRandom_ = new SecureRandom();

    /**
	* The bit-length of n = p*q.
	*/
    private int strength_ = 1024;

    /**
	* Encryption/verification exponent
	*/
    private BigInteger e_ = null;

    /**
	* The default constructor simply calls the superclass with the name of the algorithm.
	*/
    public RSAKeyPairGenerator() {
        super("RSA");
    }

    /**
	* Generates a RSA keypair. The value e will be 3 + 2*x, until e
	* satifies the conditions.
	* <p>
	*
	* @return The keypair, containing a RSAPrivCrtKey and a RSAPubKey.
	* @see cdc.standard.rsa.RSAPrivCrtKey
	* @see cdc.standard.rsa.RSAPubKey
	*/
    public KeyPair generateKeyPair() {
        int halfStrength = (strength_ + 1) / 2;
        BigInteger p = new BigInteger(halfStrength, CERTAINTY, secureRandom_);
        BigInteger q;
        do {
            q = new BigInteger(halfStrength, CERTAINTY, secureRandom_);
        } while (p.equals(q));
        BigInteger n = p.multiply(q);
        BigInteger one = BigInteger.ONE;
        BigInteger two = BigInteger.valueOf(2);
        BigInteger pm = p.subtract(one);
        BigInteger qm = q.subtract(one);
        BigInteger phi = pm.multiply(qm);
        if (e_ == null) {
            e_ = BigInteger.valueOf(DEFAULT_EXPONENT);
            while (!(phi.gcd(e_)).equals(one)) {
                e_ = e_.add(two);
            }
        }
        BigInteger d = e_.modInverse(phi);
        BigInteger exponentOne = d.mod(pm);
        BigInteger exponentTwo = d.mod(qm);
        BigInteger crt = q.modInverse(p);
        RSAPubKey pub = new RSAPubKey(n, e_);
        RSAPrivCrtKey priv = new RSAPrivCrtKey(n, d, e_, p, q, exponentOne, exponentTwo, crt);
        return (new KeyPair(pub, priv));
    }

    /**
	* Initalizes the KPG and generates a new secure random object for
	* primenumber generation.
	* <p>
	* @param strength the bit-length of the prime p.
	*/
    public void initialize(int strength) {
        secureRandom_ = new SecureRandom();
        if (strength > 1) strength_ = strength;
    }

    /**
	* Initalizes the KPG.
	* <p>
	* @param strength     the bit-length of the prime p.
	* @param secureRandom the SecureRandom for generating the numbers.
	*/
    public void initialize(int strength, SecureRandom random) {
        secureRandom_ = (random != null) ? random : new SecureRandom();
        if (strength > 1) strength_ = strength;
    }

    /**
	* Initalizes the KPG.
	* <p>
	* @param strength     the bit-length of the prime p.
	* @param secureRandom the SecureRandom for generating the numbers.
	* @param e            the encryption/verification exponent.
	*/
    public void initialize(int strength, BigInteger e, SecureRandom random) {
        secureRandom_ = (random != null) ? random : new SecureRandom();
        if (strength > 1) strength_ = strength;
        e_ = e;
    }
}

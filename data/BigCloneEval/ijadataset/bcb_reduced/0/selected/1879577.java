package gnu.javax.crypto.key.srp6;

import gnu.java.security.Registry;
import gnu.java.security.hash.IMessageDigest;
import gnu.java.security.util.Util;
import gnu.javax.crypto.key.KeyAgreementException;
import gnu.javax.crypto.key.IncomingMessage;
import gnu.javax.crypto.key.OutgoingMessage;
import gnu.javax.crypto.sasl.srp.SRP;
import gnu.javax.crypto.sasl.srp.SRPAuthInfoProvider;
import gnu.javax.crypto.sasl.srp.SRPRegistry;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * The implementation of the Host in the SRP-6 key agreement protocol.
 * <p>
 * Reference:
 * <ol>
 * <li><a href="http://srp.stanford.edu/design.html">SRP Protocol Design</a><br>
 * Thomas J. Wu.</li>
 * </ol>
 */
public class SRP6Host extends SRP6KeyAgreement {

    /** The user's ephemeral key pair. */
    private KeyPair hostKeyPair;

    /** The SRP password database. */
    private SRPAuthInfoProvider passwordDB;

    protected void engineInit(final Map attributes) throws KeyAgreementException {
        rnd = (SecureRandom) attributes.get(SOURCE_OF_RANDOMNESS);
        N = (BigInteger) attributes.get(SHARED_MODULUS);
        if (N == null) throw new KeyAgreementException("missing shared modulus");
        g = (BigInteger) attributes.get(GENERATOR);
        if (g == null) throw new KeyAgreementException("missing generator");
        final String md = (String) attributes.get(HASH_FUNCTION);
        if (md == null || md.trim().length() == 0) throw new KeyAgreementException("missing hash function");
        srp = SRP.instance(md);
        passwordDB = (SRPAuthInfoProvider) attributes.get(HOST_PASSWORD_DB);
        if (passwordDB == null) throw new KeyAgreementException("missing SRP password database");
    }

    protected OutgoingMessage engineProcessMessage(final IncomingMessage in) throws KeyAgreementException {
        switch(step) {
            case 0:
                return computeSharedSecret(in);
            default:
                throw new IllegalStateException("unexpected state");
        }
    }

    protected void engineReset() {
        hostKeyPair = null;
        super.engineReset();
    }

    private OutgoingMessage computeSharedSecret(final IncomingMessage in) throws KeyAgreementException {
        final String I = in.readString();
        final BigInteger A = in.readMPI();
        final Map credentials;
        try {
            final Map userID = new HashMap();
            userID.put(Registry.SASL_USERNAME, I);
            userID.put(SRPRegistry.MD_NAME_FIELD, srp.getAlgorithm());
            credentials = passwordDB.lookup(userID);
        } catch (IOException x) {
            throw new KeyAgreementException("computeSharedSecret()", x);
        }
        final BigInteger s = new BigInteger(1, Util.fromBase64((String) credentials.get(SRPRegistry.SALT_FIELD)));
        final BigInteger v = new BigInteger(1, Util.fromBase64((String) credentials.get(SRPRegistry.USER_VERIFIER_FIELD)));
        final SRPKeyPairGenerator kpg = new SRPKeyPairGenerator();
        final Map attributes = new HashMap();
        if (rnd != null) attributes.put(SRPKeyPairGenerator.SOURCE_OF_RANDOMNESS, rnd);
        attributes.put(SRPKeyPairGenerator.SHARED_MODULUS, N);
        attributes.put(SRPKeyPairGenerator.GENERATOR, g);
        attributes.put(SRPKeyPairGenerator.USER_VERIFIER, v);
        kpg.setup(attributes);
        hostKeyPair = kpg.generate();
        final BigInteger B = ((SRPPublicKey) hostKeyPair.getPublic()).getY();
        final BigInteger u = uValue(A, B);
        final BigInteger b = ((SRPPrivateKey) hostKeyPair.getPrivate()).getX();
        final BigInteger S = A.multiply(v.modPow(u, N)).modPow(b, N);
        final byte[] sBytes = Util.trim(S);
        final IMessageDigest hash = srp.newDigest();
        hash.update(sBytes, 0, sBytes.length);
        K = new BigInteger(1, hash.digest());
        final OutgoingMessage result = new OutgoingMessage();
        result.writeMPI(s);
        result.writeMPI(B);
        complete = true;
        return result;
    }
}

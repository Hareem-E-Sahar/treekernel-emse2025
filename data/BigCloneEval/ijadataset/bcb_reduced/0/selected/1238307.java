package sevs.network;

import sevs.crypto.Util;
import java.io.*;
import java.security.*;
import java.security.GeneralSecurityException;
import javax.crypto.*;

/**
 * NetworkClient implementation that encrypts the message on the wire.
 * 
 * @author  Kohsuke Kawaguchi
 */
class SecureClientImpl extends AbstractClientImpl {

    /** DES key generator used to generate session keys. */
    private static final KeyGenerator gen;

    static {
        try {
            gen = KeyGenerator.getInstance("DES", "CryptixCrypto");
            gen.init(new SecureRandom());
            gen.generateKey();
        } catch (Exception e) {
            System.err.println("Cryptix is not installed properly");
            e.printStackTrace();
            throw new InternalError();
        }
    }

    /**
     * @param _serverKey
     *      The public key of the server.
     */
    SecureClientImpl(String _server, int _port) throws IOException {
        super(_server, _port);
        this.serverKey = readServerKey();
    }

    /**
     * Maybe we should have the factory load the key.
     */
    private static PublicKey readServerKey() throws IOException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("key.pub"));
            PublicKey key = (PublicKey) ois.readObject();
            ois.close();
            return key;
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
    }

    /** The public key of the server. */
    private final PublicKey serverKey;

    public Serializable onMessage(Serializable msg) throws IOException {
        try {
            BidirectionalChannel channel = connect();
            SecretKey sessionKey = gen.generateKey();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS#1", "CryptixCrypto");
            cipher.init(Cipher.ENCRYPT_MODE, serverKey);
            byte[] encoded = sessionKey.getEncoded();
            byte[] header = cipher.doFinal(encoded);
            channel.getOutputStream().write(header);
            Util.encrypt(sessionKey, new TimeStampedEnvelope(msg), channel.getOutputStream());
            Serializable ret = Util.decrypt(sessionKey, channel.getInputStream());
            channel.close();
            return ret;
        } catch (KeyException e) {
            e.printStackTrace();
            throw new IOException("Cryptix library is not installed");
        } catch (GeneralSecurityException e) {
            e.printStackTrace(System.err);
            throw new InternalError();
        }
    }
}

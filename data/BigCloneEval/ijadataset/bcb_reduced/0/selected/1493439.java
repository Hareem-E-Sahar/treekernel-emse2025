package smartcard.OCF.util;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Enumeration;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import opencard.core.service.DefaultCHVDialog;
import sun.misc.BASE64Decoder;
import cryptix.jce.provider.key.RawSecretKey;
import cryptix.jce.provider.util.Util;

public class CryptoUtil {

    public static boolean verifyCardCryptogram(byte[] _session_key, byte[] _host_challenge, byte[] _card_challenge, byte[] _card_cryptogram) throws Exception {
        byte[] message = new byte[_host_challenge.length + _card_challenge.length];
        System.arraycopy(_host_challenge, 0, message, 0, _host_challenge.length);
        System.arraycopy(_card_challenge, 0, message, _host_challenge.length, _card_challenge.length);
        byte[] expected = calculateMAC(_session_key, message, iv);
        System.err.println("Original:   " + toString(_card_cryptogram));
        System.err.println("Calculated: " + toString(expected));
        return areEqual(expected, _card_cryptogram);
    }

    public static byte[] calculateHostCryptogram(byte[] _session_key, byte[] _card_challenge, byte[] _host_challenge) throws Exception {
        byte[] message = new byte[_card_challenge.length + _host_challenge.length];
        System.arraycopy(_card_challenge, 0, message, 0, _card_challenge.length);
        System.arraycopy(_host_challenge, 0, message, _card_challenge.length, _host_challenge.length);
        return calculateMAC(_session_key, message, iv);
    }

    public static byte[] calculateMAC(byte[] _session_key, byte[] _message, byte[] new_iv) throws Exception {
        System.out.println(toString(_message));
        IvParameterSpec new_ivSpec = new IvParameterSpec(new_iv);
        SecretKey session_key = new RawSecretKey("TripleDES", expandKey(_session_key));
        int block_count = _message.length / TDES_BLOCK_SIZE;
        int last_block_length = _message.length % TDES_BLOCK_SIZE;
        Cipher cipher = Cipher.getInstance("TripleDES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, session_key, new_ivSpec);
        byte[] block = new byte[TDES_BLOCK_SIZE];
        for (int i = 0; i < block_count; i++) {
            System.arraycopy(_message, i * TDES_BLOCK_SIZE, block, 0, TDES_BLOCK_SIZE);
            cipher.update(block);
        }
        int pos = block_count * TDES_BLOCK_SIZE;
        System.arraycopy(_message, pos, block, 0, _message.length - pos);
        System.arraycopy(PADDING, 0, block, _message.length - pos, TDES_BLOCK_SIZE - last_block_length);
        return cipher.doFinal(block);
    }

    private static byte[] getDerivationData(byte[] _host_chlg, byte[] _card_chlg) {
        byte[] derivation_data = new byte[TDES_KEY_LENGTH];
        System.arraycopy(_card_chlg, 4, derivation_data, 0, 4);
        System.arraycopy(_host_chlg, 0, derivation_data, 4, 4);
        System.arraycopy(_card_chlg, 0, derivation_data, 8, 4);
        System.arraycopy(_host_chlg, 4, derivation_data, 12, 4);
        return derivation_data;
    }

    public static byte[] getSessionKey(byte[] _auth_key, byte[] _host_chlg, byte[] _card_chlg) throws Exception {
        Cipher cipher = Cipher.getInstance("TripleDES/ECB/NoPadding");
        SecretKey key = new RawSecretKey("TripleDES", expandKey(_auth_key));
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(getDerivationData(_host_chlg, _card_chlg));
    }

    private static byte[] expandKey(byte[] _tdes_key) {
        byte[] tmp = new byte[TDES_KEY_LENGTH];
        System.arraycopy(_tdes_key, 0, tmp, 0, 16);
        System.arraycopy(_tdes_key, 0, tmp, 16, 8);
        return tmp;
    }

    public static byte[] getSHA1_PKCS1Hash(byte[] data) throws Exception {
        int MODULUS_BYTE_LEN = 0x80;
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.reset();
        byte[] SHA1_ASN_DATA = { 0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2B, 0x0E, 0x03, 0x02, 0x1A, 0x05, 0x00, 0x04, 0x14 };
        int mdl = md.digest().length;
        int length = MODULUS_BYTE_LEN;
        int aidl = SHA1_ASN_DATA.length;
        int padLen = length - 3 - aidl - mdl;
        if (padLen < 0) throw new InvalidKeyException("Signer's key modulus too short.");
        md.update(data);
        return Util.toFixedLenByteArray(makePKCS1(md, MODULUS_BYTE_LEN, SHA1_ASN_DATA), MODULUS_BYTE_LEN);
    }

    /**
     * Returns a byte array consisting of a padded message digest value,
     * previously computed. This packet will be RSA-encrypted with the
     * private key of this object to act as an authentication for whatever
     * was digested.
     * <p>
     * As described in the <i>engineSign()</i> method above, the return
     * array will consist of:
     * <pre>
     *    MSB                                                            LSB
     *    00  01   FF-1 ... FF-n  00   AID-1 ... AID-n   04 LL MD-1 ... MD-n
     *      | BT |----- PS -----|    |-- AlgorithmId --|------ digest ------|
     * </pre>
     * <p>
     * The <i>AID<i> bytes form the <i>AlgorithmIdentifier</i> token.
     * The OCTET STRING tag is <i>04</i> and <i>LL</i> is the length byte
     * (the number of bytes in the message digest proper, i.e. <i>n</i>).
     * <p>
     * Bytes <i>MD-1</i> to <i>MD-n</i> are the message digest value
     * of the material updated so far, thus completing the <i>digest</i>
     * token in the SEQUENCE described in <i>engineSign()</i> above.
     *
     * @return the result of the updating process framed in a PKCS#1
     *         type 01 block structure as a BigInteger.
     * @exception SignatureException If the length of the minimal PKCS#1 frame
     *      generated by this method will be longer than the public key modulus.
     */
    private static BigInteger makePKCS1(MessageDigest md, int modulus_byte_length, byte[] aid) throws SignatureException {
        byte[] theMD = md.digest();
        int mdl = theMD.length;
        byte[] r = new byte[modulus_byte_length];
        r[1] = 0x01;
        int aidl = aid.length;
        int padLen = modulus_byte_length - 3 - aidl - mdl;
        if (padLen < 0) throw new SignatureException("Signer's public key modulus too short.");
        for (int i = 0; i < padLen; ) r[2 + i++] = (byte) 0xFF;
        System.arraycopy(aid, 0, r, padLen + 3, aidl);
        System.arraycopy(theMD, 0, r, modulus_byte_length - mdl, mdl);
        return new BigInteger(r);
    }

    public static boolean verifySignedData(RSAPublicKey key, byte[] data, byte[] signature) throws Exception {
        Signature sign = Signature.getInstance("SHA1withRSA");
        sign.initVerify(key);
        sign.update(data);
        return sign.verify(signature);
    }

    public static byte[] getRSAPublicDecrypt(byte[] cipher, RSAPublicKey key) throws Exception {
        BigInteger modulus = key.getModulus();
        BigInteger exponent = key.getPublicExponent();
        BigInteger plaintext = new BigInteger(1, cipher).modPow(exponent, modulus);
        return getBigIntByteArray(plaintext);
    }

    private static byte[] getBigIntByteArray(BigInteger big) {
        byte[] bigBytes = big.toByteArray();
        if ((big.bitLength() % 8) != 0) return bigBytes; else {
            byte[] smallBytes = new byte[big.bitLength() / 8];
            System.arraycopy(bigBytes, 1, smallBytes, 0, smallBytes.length);
            return smallBytes;
        }
    }

    public static boolean verifyFromKeyStoreWithX509TrustManager(X509Certificate cert) throws Exception {
        KeyStore keyStore = loadKeyStore();
        try {
            Security.addProvider((Provider) Class.forName("com.sun.net.ssl.internal.ssl.Provider").newInstance());
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return false;
    }

    public static boolean verifyFromKeyStore(X509Certificate cert) throws Exception {
        boolean verified = false;
        KeyStore keyStore = loadKeyStore();
        Enumeration e = keyStore.aliases();
        int i = 0;
        while (e.hasMoreElements()) {
            String alias = (String) e.nextElement();
            Certificate caCert = keyStore.getCertificate(alias);
            if (caCert instanceof X509Certificate) {
                System.out.println("Cert No. " + ++i + " from keystore");
                System.out.println((X509Certificate) caCert);
            }
            PublicKey caPublicKey = caCert.getPublicKey();
            try {
                cert.verify(caPublicKey);
                if (caCert instanceof X509Certificate) {
                    System.out.println("Cert No. " + i + " from keystore with alias " + alias + " verifes");
                    System.out.println((X509Certificate) caCert);
                }
                verified = true;
            } catch (Exception ee) {
                System.out.println(ee.getMessage());
                continue;
            }
        }
        return verified;
    }

    public static boolean verifyFromStaticCert(X509Certificate cert) throws Exception {
        Certificate caCert = loadVerisignIntermediateCA();
        PublicKey caPublicKey = caCert.getPublicKey();
        try {
            cert.verify(caPublicKey);
            return true;
        } catch (Exception ee) {
            return false;
        }
    }

    public static KeyStore loadKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS", "SUN");
        Frame frame = new Frame("");
        FileDialog fd = new FileDialog(frame);
        frame.setVisible(false);
        Dimension dim = frame.getToolkit().getScreenSize();
        fd.setSize(dim.width / 3, dim.height / 3);
        fd.setLocation(dim.width / 2 - dim.width / 6, dim.height / 2 - dim.height / 6);
        fd.setVisible(true);
        frame.dispose();
        File f = new File(fd.getDirectory() + File.separator + fd.getFile());
        FileInputStream fin = new FileInputStream(f);
        String keyStorePass = new DefaultCHVDialog().getCHV(1);
        keyStore.load(fin, keyStorePass.toCharArray());
        return keyStore;
    }

    public static byte[] loadVerisignIntermediateCABytes() throws Exception {
        BufferedReader in = new BufferedReader(new CharArrayReader(verisignclass1caindividualsubscriberpersonanotvalidated.toCharArray()));
        String begin = in.readLine();
        if (!(begin.equals("-----BEGIN CERTIFICATE-----"))) throw new IOException("Could not find beginning of certificate");
        String base64 = new String();
        boolean trucking = true;
        while (trucking) {
            String line = in.readLine();
            if (line.startsWith("-----")) trucking = false; else base64 += line;
        }
        return new BASE64Decoder().decodeBuffer(base64);
    }

    public static X509Certificate loadVerisignIntermediateCA() throws Exception {
        return bytes2X509Certificate(loadVerisignIntermediateCABytes());
    }

    public static X509Certificate bytes2X509Certificate(byte[] cert_bytes) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(cert_bytes);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(bis);
    }

    public static final String verisignclass1caindividualsubscriberpersonanotvalidated = "-----BEGIN CERTIFICATE-----\r\nMIIDYjCCAsugAwIBAgIQC9oLF8E/iY6rCXR6tM4uMzANBgkqhkiG9w0BAQIFADBf\r\nMQswCQYDVQQGEwJVUzEXMBUGA1UEChMOVmVyaVNpZ24sIEluYy4xNzA1BgNVBAsT\r\nLkNsYXNzIDEgUHVibGljIFByaW1hcnkgQ2VydGlmaWNhdGlvbiBBdXRob3JpdHkw\r\nHhcNOTgwNTEyMDAwMDAwWhcNMDgwNTEyMjM1OTU5WjCBzDEXMBUGA1UEChMOVmVy\r\naVNpZ24sIEluYy4xHzAdBgNVBAsTFlZlcmlTaWduIFRydXN0IE5ldHdvcmsxRjBE\r\nBgNVBAsTPXd3dy52ZXJpc2lnbi5jb20vcmVwb3NpdG9yeS9SUEEgSW5jb3JwLiBC\r\neSBSZWYuLExJQUIuTFREKGMpOTgxSDBGBgNVBAMTP1ZlcmlTaWduIENsYXNzIDEg\r\nQ0EgSW5kaXZpZHVhbCBTdWJzY3JpYmVyLVBlcnNvbmEgTm90IFZhbGlkYXRlZDCB\r\nnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAu1pEigQWu1X9A3qKLZRPFXg2uA1K\r\nsm+cVL+86HcqnbnwaLuV2TFBcHqBS7lIE1YtxwjhhEKrwKKSq0RcqkLwgg4C6S/7\r\nwju7vsknCl22sDZCM7VuVIhPh0q/Gdr5FegPh7Yc48zGmo5/aiSS4/zgZbqnsX7v\r\nyds3ashKyAkG5JkCAwEAAaOBsDCBrTAPBgNVHRMECDAGAQH/AgEAMEcGA1UdIARA\r\nMD4wPAYLYIZIAYb4RQEHAQEwLTArBggrBgEFBQcCARYfd3d3LnZlcmlzaWduLmNv\r\nbS9yZXBvc2l0b3J5L1JQQTAxBgNVHR8EKjAoMCagJKAihiBodHRwOi8vY3JsLnZl\r\ncmlzaWduLmNvbS9wY2ExLmNybDALBgNVHQ8EBAMCAQYwEQYJYIZIAYb4QgEBBAQD\r\nAgEGMA0GCSqGSIb3DQEBAgUAA4GBAAJ9nm9FSziguN7pU2QhvORMK48e/pJArNgK\r\nOWqhMiEsB5urWf7SYhp9VTiwN3Pc9AdmY2K94VNwUofnqNhS6VstquHez6wxVNSL\r\nGcjYI6jvBCsyfSwYHMh8iagud/JE0WUKTXS17tMbknN0Lok7NRNy50AxmtOyxKvn\r\nVr6L4/sV\r\n-----END CERTIFICATE-----";

    public static byte[] bytesFromHexString(String hex) {
        int len = hex.length();
        byte[] buf = new byte[((len + 1) / 2)];
        int i = 0, j = 0;
        if ((len % 2) == 1) buf[j++] = (byte) fromDigit(hex.charAt(i++));
        while (i < len) {
            buf[j++] = (byte) ((fromDigit(hex.charAt(i++)) << 4) | fromDigit(hex.charAt(i++)));
        }
        return buf;
    }

    public static int fromDigit(char ch) {
        if (ch >= '0' && ch <= '9') return ch - '0';
        if (ch >= 'A' && ch <= 'F') return ch - 'A' + 10;
        if (ch >= 'a' && ch <= 'f') return ch - 'a' + 10;
        throw new IllegalArgumentException("invalid hex digit '" + ch + "'");
    }

    public static boolean areEqual(byte[] a, byte[] b) {
        int aLength = a.length;
        if (aLength != b.length) return false;
        for (int i = 0; i < aLength; i++) if (a[i] != b[i]) return false;
        return true;
    }

    public static String toString(byte[] ba) {
        int length = ba.length;
        char[] buf = new char[length * 3];
        for (int i = 0, j = 0, k; i < length; ) {
            k = ba[i++];
            buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
            buf[j++] = HEX_DIGITS[k & 0x0F];
            buf[j++] = ' ';
        }
        return new String(buf);
    }

    public static byte[] xor(byte[] a, byte[] b) {
        byte[] tmp = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            tmp[i] = (byte) (a[i] ^ b[i]);
        }
        return tmp;
    }

    public static byte[] or(byte[] a, byte[] b) {
        byte[] tmp = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            tmp[i] = (byte) (a[i] | b[i]);
        }
        return tmp;
    }

    public static byte[] and(byte[] a, byte[] b) {
        byte[] tmp = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            tmp[i] = (byte) (a[i] & b[i]);
        }
        return tmp;
    }

    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    static {
        try {
            java.security.Security.addProvider((Provider) Class.forName("cryptix.jce.provider.CryptixCrypto").newInstance());
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    static final byte[] iv = { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

    static final byte[] PADDING = { (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

    static IvParameterSpec ivSpec = new IvParameterSpec(iv);

    static final int TDES_KEY_LENGTH = 0x18;

    static final int TDES_BLOCK_SIZE = 0x08;
}

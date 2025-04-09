package de.uni_bremen.informatik.p2p.peeranha42.core.network.wrapper.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.ArrayList;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;
import org.apache.log4j.Logger;
import de.uni_bremen.informatik.p2p.peeranha42.core.network.wrapper.MessageWrapper;
import de.uni_bremen.informatik.p2p.peeranha42.core.plugin.Client;

/**
 * @author lippold
 * 
 * Derived from BouncyCastle rsa_enc/Encrypt-Decrypt-HMAC.java example
 *
 */
public class RSAWrapper implements MessageWrapper {

    static final int FILE_HEADER = 0x7e01;

    static final int DATA_BLOCK = 1;

    static final int FINAL_DATA_BLOCK = 2;

    static final int HMAC_BLOCK = 3;

    static final int CERT_BLOCK = 19;

    static final int KEY_BLOCK = 16;

    static final int IV_BLOCK = 17;

    static final int HMAC_KEY_BLOCK = 18;

    private Certificate[] certs;

    private PrivateKey priv_key;

    private SecretKey aes_key = null;

    private SecretKey mac_key = null;

    private byte[] aes_iv = null;

    private byte[] mac_code = null;

    private SecureRandom sec_rand;

    private static final Logger log = Logger.getLogger(RSAWrapper.class);

    public RSAWrapper(Certificate[] certs) {
        this.certs = certs;
        initRandom();
    }

    public RSAWrapper(PrivateKey pKey) {
        priv_key = pKey;
        initRandom();
    }

    void initRandom() {
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            sec_rand = SecureRandom.getInstance("SHA1PRNG", "SUN");
            if (new File("/dev/urandom").exists()) {
                log.debug("Salting SecureRandom (SHA1PRNG) from /dev/urandom");
                byte[] salt = new byte[8192];
                new FileInputStream("/dev/urandom").read(salt);
                sec_rand.setSeed(salt);
            }
        } catch (Exception e) {
            log.fatal(e.toString());
        }
    }

    public Message[] wrap(Message[] msg) {
        Message[] msgarr = new Message[msg.length];
        for (int i = 0; i < msg.length; i++) {
            Message newMsg = new Message();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] message = null;
            try {
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(msg[i]);
                oos.close();
                message = baos.toByteArray();
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            byte[] encrypted = encrypt(message);
            ByteArrayMessageElement bams = new ByteArrayMessageElement("RSA-HMAC encrypted Message", MimeMediaType.AOS, encrypted, null);
            newMsg.addMessageElement("RSAWrapper", bams);
            msgarr[i] = newMsg;
        }
        return msgarr;
    }

    public Message[] unwrap(Message[] msg) {
        ArrayList unwrapArr = new ArrayList();
        for (int i = 0; i < msg.length; i++) {
            boolean encrypted = false;
            Message.ElementIterator mit = msg[i].getMessageElementsOfNamespace("RSAWrapper");
            while (mit.hasNext()) {
                encrypted = true;
                Object o = mit.next();
                if (o instanceof ByteArrayMessageElement) {
                    ByteArrayMessageElement el = (ByteArrayMessageElement) o;
                    byte[] crypted = el.getBytes();
                    Message retmsg = null;
                    ByteArrayInputStream bin = new ByteArrayInputStream(decrypt(crypted));
                    try {
                        ObjectInputStream oin = new ObjectInputStream(bin);
                        retmsg = (Message) oin.readObject();
                        unwrapArr.add(retmsg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else continue;
            }
            if (!encrypted) unwrapArr.add(msg[i]);
        }
        unwrapArr.trimToSize();
        Message[] ret = (Message[]) unwrapArr.toArray(new Message[0]);
        return ret;
    }

    byte[] encrypt(byte[] b) {
        try {
            KeyGenerator key_gen = KeyGenerator.getInstance("AES", "BC");
            key_gen.init(128, sec_rand);
            Key aes_key = key_gen.generateKey();
            log.debug("Set up cipher AES/CBC/PKCS7Padding");
            Cipher output_cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
            output_cipher.init(Cipher.ENCRYPT_MODE, aes_key, sec_rand);
            byte[] aes_iv = output_cipher.getIV();
            byte[] aes_key_enc = aes_key.getEncoded();
            Mac mac = Mac.getInstance("HMACSHA1", "BC");
            log.debug("Generate key and then set up HMAC (HMACSHA1).");
            byte[] mac_key_bytes = new byte[20];
            sec_rand.nextBytes(mac_key_bytes);
            Key mac_key = new SecretKeySpec(mac_key_bytes, "HMACSHA1");
            mac.init(mac_key);
            log.debug("Set up RSA with OAEPPadding (see PKCS1 V2)");
            Cipher rsa_eng = Cipher.getInstance("RSA/None/OAEPPadding", "BC");
            ByteArrayOutputStream msg_str = new ByteArrayOutputStream();
            MacOutputStream mac_str = new MacOutputStream(msg_str, mac);
            DataOutputStream data_str = new DataOutputStream(mac_str);
            log.debug("writing header");
            data_str.writeShort(FILE_HEADER);
            log.debug("Writing HashCodes of used Certificates");
            data_str.writeShort(CERT_BLOCK);
            data_str.writeInt(certs.length);
            PublicKey pub_key[] = new PublicKey[certs.length];
            for (int i = 0; i < certs.length; i++) {
                pub_key[i] = certs[i].getPublicKey();
                data_str.writeInt(certs[i].hashCode());
            }
            log.debug("Aes key enc with RSA");
            data_str.writeShort(KEY_BLOCK);
            for (int i = 0; i < pub_key.length; i++) {
                rsa_eng.init(Cipher.ENCRYPT_MODE, pub_key[i], sec_rand);
                byte[] tmp = rsa_eng.doFinal(aes_key_enc);
                data_str.writeInt(tmp.length);
                data_str.write(tmp);
                blank(tmp);
            }
            log.debug("Aes IV enc with RSA (See note in source code)");
            data_str.writeShort(IV_BLOCK);
            for (int i = 0; i < pub_key.length; i++) {
                rsa_eng.init(Cipher.ENCRYPT_MODE, pub_key[i], sec_rand);
                byte[] tmp = rsa_eng.doFinal(aes_iv);
                data_str.writeInt(tmp.length);
                data_str.write(tmp);
                blank(tmp);
            }
            log.debug("HMACSHA1 key enc with RSA");
            data_str.writeShort(HMAC_KEY_BLOCK);
            byte[] tmp = output_cipher.doFinal(mac_key.getEncoded());
            data_str.writeInt(tmp.length);
            data_str.write(tmp);
            blank(tmp);
            int l = 0;
            byte[] buf = new byte[8192];
            byte[] out = null;
            ByteArrayInputStream in_str = new ByteArrayInputStream(b);
            while ((l = in_str.read(buf)) > -1) {
                out = output_cipher.update(buf, 0, l);
                if (out != null) {
                    data_str.writeShort(DATA_BLOCK);
                    data_str.writeInt(out.length);
                    data_str.write(out);
                    log.debug("Encrypted " + out.length + " bytes output");
                }
            }
            out = output_cipher.doFinal();
            data_str.writeShort(FINAL_DATA_BLOCK);
            data_str.writeInt(out.length);
            data_str.write(out);
            log.debug("Final Encrypted " + out.length + " bytes output");
            blank(buf);
            buf = null;
            log.debug("Write out HMAC code block.");
            data_str.writeShort(HMAC_BLOCK);
            data_str.flush();
            tmp = mac.doFinal();
            data_str.writeInt(tmp.length);
            data_str.write(tmp);
            blank(tmp);
            data_str.flush();
            data_str.close();
            log.debug("Dispose of key material as best we can in java..");
            blank(aes_key_enc);
            aes_key_enc = null;
            aes_key = null;
            tmp = null;
            log.debug("The End..");
            return msg_str.toByteArray();
        } catch (Exception e) {
            log.fatal(e.toString());
            return null;
        }
    }

    private static void blank(byte[] bytes) {
        for (int t = 0; t < bytes.length; t++) {
            bytes[t] = 0;
        }
    }

    byte[] decrypt(byte[] b) {
        try {
            parseHeader(b);
            if (!validateHMAC(b)) {
                throw new Exception("HMAC check failed");
            }
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, aes_key, new IvParameterSpec(aes_iv));
            ByteArrayOutputStream file_str = new ByteArrayOutputStream();
            DataInputStream data_str = new DataInputStream(new ByteArrayInputStream(b));
            int cmd = 0;
            int l = 0;
            int noRcpts = -1;
            byte[] buf = new byte[8192];
            byte[] _out = null;
            for (; ; ) {
                cmd = data_str.readShort();
                if (cmd == DATA_BLOCK) {
                    l = data_str.readInt();
                    data_str.readFully(buf, 0, l);
                    _out = cipher.update(buf, 0, l);
                    if (_out != null) file_str.write(_out);
                    log.debug(".");
                    continue;
                }
                if (cmd == FINAL_DATA_BLOCK) {
                    l = data_str.readInt();
                    data_str.readFully(buf, 0, l);
                    _out = cipher.doFinal(buf, 0, l);
                    if (_out != null) file_str.write(_out);
                    log.debug("!");
                    break;
                }
                if (cmd == HMAC_BLOCK) {
                    l = data_str.readInt();
                    data_str.skip(l);
                    continue;
                }
                if (cmd == KEY_BLOCK) {
                    for (int i = 0; i < noRcpts; i++) {
                        l = data_str.readInt();
                        data_str.read(buf, 0, l);
                    }
                    continue;
                }
                if (cmd == IV_BLOCK) {
                    for (int i = 0; i < noRcpts; i++) {
                        l = data_str.readInt();
                        data_str.read(buf, 0, l);
                    }
                    continue;
                }
                if (cmd == HMAC_KEY_BLOCK) {
                    for (int i = 0; i < noRcpts; i++) {
                        l = data_str.readInt();
                        data_str.read(buf, 0, l);
                    }
                    continue;
                }
                if (cmd == CERT_BLOCK) {
                    noRcpts = data_str.readInt();
                    for (int i = 0; i < noRcpts; i++) {
                        data_str.readInt();
                    }
                    continue;
                }
            }
            blank(buf);
            buf = null;
            file_str.flush();
            byte[] ret = file_str.toByteArray();
            file_str.close();
            data_str.close();
            return ret;
        } catch (Exception e) {
            log.fatal(e.toString());
            return null;
        }
    }

    /**
	 * Validate the encrypted file by using the MAC code.
	 * Like the decrypt method this method assumes that the
	 * parseHeader() method has already been called and
	 * the 'mac_key' variable had been set.
	 * If not it will break with a NullPointerException.
	 * <P>
	 * To validate (Note: not authenticate) the file a
	 * the mac is first initialized with the mac key and
	 * then the whole file is processed through the MacInputStream
	 * when the HMAC_BLOCK is found the processing stops and
	 * the mac code is computed. This computed mac code is compared
	 * to the mac code stored in the HMAC_BLOCK.
	 * If the two codes match then the file is valid and intact.
	 */
    boolean validateHMAC(byte[] b) throws Exception {
        Mac mac = Mac.getInstance("HMACSHA1", "BC");
        mac.init(mac_key);
        MacInputStream mac_str = new MacInputStream(new ByteArrayInputStream(b), mac);
        DataInputStream data_str = new DataInputStream(mac_str);
        int cmd = 0;
        int l = 0;
        int noRcpts = -1;
        byte[] buf = new byte[8192];
        do {
            cmd = data_str.readShort();
            if (cmd == DATA_BLOCK) {
                l = data_str.readInt();
                data_str.read(buf, 0, l);
                continue;
            }
            if (cmd == FINAL_DATA_BLOCK) {
                l = data_str.readInt();
                data_str.read(buf, 0, l);
                continue;
            }
            if (cmd == CERT_BLOCK) {
                noRcpts = data_str.readInt();
                for (int i = 0; i < noRcpts; i++) {
                    data_str.readInt();
                }
                continue;
            }
            if (cmd == KEY_BLOCK) {
                for (int i = 0; i < noRcpts; i++) {
                    l = data_str.readInt();
                    data_str.read(buf, 0, l);
                }
                continue;
            }
            if (cmd == IV_BLOCK) {
                for (int i = 0; i < noRcpts; i++) {
                    l = data_str.readInt();
                    data_str.read(buf, 0, l);
                }
                continue;
            }
            if (cmd == HMAC_KEY_BLOCK) {
                for (int i = 0; i < noRcpts; i++) {
                    l = data_str.readInt();
                    data_str.read(buf, 0, l);
                }
                continue;
            }
        } while (cmd != HMAC_BLOCK);
        buf = mac.doFinal();
        data_str.close();
        return MessageDigest.isEqual(buf, mac_code);
    }

    /**
	 * The parseHeader() method, parses the header and attempts to decrypt any information
	 * that needs to be decrypted.
	 * To do this the parse header method requires the private key of the recipient.
	 *
	 * In these examples RSA is used with OAEPPadding. OAEPPadding is very very useful.
	 * The encoding is such that if the decryption fails the decoding of the OAEPPadding
	 * will fail also. (See RSAES-OAEP over at RSA labs.)
	 */
    void parseHeader(byte[] b) throws Exception {
        DataInputStream data_in = new DataInputStream(new ByteArrayInputStream(b));
        int l = 0;
        int myCert = -1;
        int noRcpts = -1;
        boolean ena = false;
        boolean stop = false;
        Cipher rsa_eng = Cipher.getInstance("RSA/None/OAEPPadding", "BC");
        rsa_eng.init(Cipher.DECRYPT_MODE, priv_key, sec_rand);
        while (!stop) {
            try {
                int cmd = data_in.readShort();
                if (cmd == FILE_HEADER) {
                    ena = true;
                    log.debug("Header Parse: File Header");
                    continue;
                }
                if (cmd == DATA_BLOCK) {
                    if (!ena) {
                        throw new Exception("Broken header");
                    }
                    log.debug("Header Parse: Data Block, size = " + l);
                    l = data_in.readInt();
                    data_in.skip(l);
                    continue;
                }
                if (cmd == FINAL_DATA_BLOCK) {
                    if (!ena) {
                        throw new Exception("Broken header");
                    }
                    l = data_in.readInt();
                    log.debug("Header Parse: Final Data Block, size = " + l);
                    data_in.skip(l);
                    continue;
                }
                if (cmd == HMAC_BLOCK) {
                    if (!ena) {
                        throw new Exception("Broken header");
                    }
                    l = data_in.readInt();
                    log.debug("Parse: HMAC block size = " + l);
                    mac_code = new byte[l];
                    data_in.readFully(mac_code);
                    continue;
                }
                if (cmd == CERT_BLOCK) {
                    if (!ena) {
                        throw new Exception("Broken header");
                    }
                    noRcpts = data_in.readInt();
                    for (int i = 0; i < noRcpts; i++) {
                        int hashCode = data_in.readInt();
                        if (hashCode == Client.getCertificate().hashCode()) {
                            log.debug("Encrypted to me!");
                            myCert = i;
                        }
                    }
                }
                if (cmd == KEY_BLOCK) {
                    if (!ena) {
                        throw new Exception("Broken header");
                    }
                    if (myCert < 0) {
                        throw new Exception("this is not for me");
                    }
                    for (int i = 0; i < noRcpts; i++) {
                        l = data_in.readInt();
                        log.debug("Parse: Key encoded block size = " + l);
                        byte[] d = new byte[l];
                        data_in.readFully(d);
                        if (i == myCert) {
                            log.debug("Initialized AES key");
                            aes_key = (SecretKey) new SecretKeySpec(rsa_eng.doFinal(d), "AES");
                        }
                    }
                    continue;
                }
                if (cmd == IV_BLOCK) {
                    if (!ena) {
                        throw new Exception("Broken header");
                    }
                    for (int i = 0; i < noRcpts; i++) {
                        l = data_in.readInt();
                        aes_iv = new byte[l];
                        data_in.readFully(aes_iv);
                        if (i == myCert) {
                            log.debug("Parse: IV block, size = " + l);
                            aes_iv = rsa_eng.doFinal(aes_iv);
                        }
                    }
                    continue;
                }
                if (cmd == HMAC_KEY_BLOCK) {
                    if (!ena) {
                        throw new Exception("Broken header");
                    }
                    l = data_in.readInt();
                    byte[] d = new byte[l];
                    data_in.readFully(d);
                    log.debug("Parse: HMAC Key block, size = " + l);
                    Cipher hmac_dec = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
                    hmac_dec.init(Cipher.DECRYPT_MODE, aes_key, new IvParameterSpec(aes_iv));
                    mac_key = (SecretKey) new SecretKeySpec(hmac_dec.doFinal(d), "HMACSHA1");
                    continue;
                }
            } catch (EOFException eof) {
                stop = true;
            }
        }
        return;
    }
}

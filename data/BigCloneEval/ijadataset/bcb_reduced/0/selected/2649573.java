package org.briareo.common.pki;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.briareo.common.config.GlobalWorkDirConfiguration;
import org.briareo.common.config.MainPropertiesConfigurator;
import org.briareo.exception.EncryptException;
import org.briareo.model.ApplicationAuditMessage;
import org.briareo.model.AuditMessage;
import com.thoughtworks.xstream.XStream;

public class EncryptedMessage {

    /**
   * Loggger de clase
   */
    private static Logger logger = Logger.getLogger(EncryptedMessage.class);

    /**
   * Key para el byte[] con los random data de la generaci�n del cifrado
   */
    private String ENCRYPTED_DATA = "ENCRYPTED_DATA";

    /**
   * Key para el byte[] con los random data de la generaci�n del cifrado
   */
    private String RANDOM_DATA = "RANDOM_DATA";

    /**
   * Key para la llave simetrica cifrada por la asimetrica utilizada.
   */
    private String SIMETRIC_KEY_CIPHERED = "SIMETRIC_KEY_CIPHERED";

    /**
   * Coleccion de datos del bean
   */
    private Map data = new HashMap();

    private PKIContext pkiContext = null;

    /**
   * Creates an instance of the EncryptedMessage.
   * 
   * @throws EncryptException
   */
    public EncryptedMessage(PKIContext context) {
        this.pkiContext = context;
    }

    /**
   * Encrypts the data and stores the result into a field of the message.
   * 
   * @param is
   * @param wrappingKey
   * @throws EncryptException
   */
    public void encrypt(InputStream is, Key wrappingKey) throws EncryptException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.encrypt(is, baos, wrappingKey);
        try {
            baos.flush();
            baos.close();
        } catch (IOException e) {
            logger.warn("Some problem has occurred closing the Output Stream");
        }
        this.setEncyptedData(baos.toByteArray());
    }

    /**
   * Encrypts the data and stores the result into a field of the message.
   * 
   * @param is
   * @param wrappingKey
   * @throws EncryptException
   */
    public byte[] decrypt(InputStream is) throws EncryptException {
        if (is == null) {
            throw new EncryptException("The InputStream with the encryptedMessage can not be null.");
        }
        this.initDataFromXML(is);
        logger.debug("EncryptedMessage recovered");
        ByteArrayInputStream bais = new ByteArrayInputStream(this.getEncryptedData());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.decrypt(bais, baos);
        try {
            bais.close();
            baos.flush();
            baos.close();
        } catch (IOException e) {
            throw new EncryptException(e);
        }
        return baos.toByteArray();
    }

    /**
   * Performs the cipher operation and stores the encrypted data into the
   * OutputStream.
   * 
   * @param is
   * @param os
   * @param wrappingKey
   * @return
   * @throws EncryptException
   */
    private void encrypt(InputStream is, OutputStream os, Key wrappingKey) throws EncryptException {
        try {
            SecretKey simmetricKey = this.pkiContext.generateSimmetricKey();
            logger.debug("Using ecryption algorithm: " + this.pkiContext.getSymmetricAlgorithm());
            Cipher cipher = Cipher.getInstance(this.pkiContext.getSymmetricAlgorithm(), "BC");
            cipher.init(Cipher.ENCRYPT_MODE, simmetricKey);
            byte[] buffer = new byte[1024];
            int numBytes = 0;
            byte[] abEncryptedData = null;
            while ((numBytes = is.read(buffer)) > -1) {
                byte[] abTmp = new byte[numBytes];
                System.arraycopy(buffer, 0, abTmp, 0, numBytes);
                abEncryptedData = cipher.update(abTmp);
                if (abEncryptedData != null) {
                    os.write(abEncryptedData);
                }
            }
            abEncryptedData = cipher.doFinal();
            if (abEncryptedData != null) {
                os.write(abEncryptedData);
            }
            logger.debug("Data encryption done.");
            this.setRandomData(cipher.getIV());
            this.setSimetricKeyCiphered(this.wrapKey(simmetricKey, wrappingKey));
        } catch (Exception e) {
            EncryptException ex = new EncryptException(e);
            throw ex;
        }
    }

    /**
   * Performs the decrypt operation.
   * 
   * @param is,
   *          ciphered content
   * @param os,
   *          stream for decrypted content
   * @return
   * @throws EncryptException
   */
    private void decrypt(InputStream is, OutputStream os) throws EncryptException {
        try {
            Cipher cipher = this.initCipherDecryptionMode();
            if (cipher == null) {
                throw new EncryptException("CIPHER IS NOT INITIALIZED.");
            }
            byte[] buffer = new byte[1024];
            int numBytes = 0;
            byte[] abDecryptedData = null;
            while ((numBytes = is.read(buffer)) > -1) {
                byte[] abTmp = new byte[numBytes];
                System.arraycopy(buffer, 0, abTmp, 0, numBytes);
                abDecryptedData = cipher.update(abTmp);
                if (abDecryptedData != null) {
                    os.write(abDecryptedData);
                }
            }
            abDecryptedData = cipher.doFinal();
            if (abDecryptedData != null) {
                os.write(abDecryptedData);
            }
            logger.debug("Deciphered data.");
        } catch (Exception e) {
            EncryptException ex = new EncryptException(e);
            throw ex;
        }
    }

    /**
   * Initializes the Cipher instance for decrypt mode
   * 
   * @return
   * @throws EncryptException
   */
    private Cipher initCipherDecryptionMode() throws EncryptException {
        Key sk = this.unwrapKey(this.getSimetricKeyCiphered());
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(this.pkiContext.getSymmetricAlgorithm(), "BC");
            logger.debug("Using decryption algorithm: " + this.pkiContext.getSymmetricAlgorithm());
            if (this.getRandomData() != null) {
                logger.debug("Initializing cipher with random data.");
                cipher.init(Cipher.DECRYPT_MODE, sk, new IvParameterSpec(this.getRandomData()));
            } else {
                logger.debug("Initializing cipher with random data.");
                cipher.init(Cipher.DECRYPT_MODE, sk);
            }
        } catch (Throwable t) {
            throw new EncryptException(t);
        }
        logger.debug("[OUT]:: " + cipher);
        return cipher;
    }

    /**
   * Wraps the simmetric key from an EncryptedMessage.
   * 
   * @param key
   * @return
   * @throws EncryptException
   */
    private byte[] wrapKey(Key key, Key wrappingKey) throws EncryptException {
        if (key == null) {
            throw new EncryptException("The Key to wrap can not be null.");
        }
        if (this.pkiContext == null) {
            throw new EncryptException("The PKIContext is not initialized.");
        }
        byte[] wrappedKey = null;
        try {
            logger.debug("Using wrapping key algorithm: " + this.pkiContext.getWrappingAlgorithm());
            Cipher wrapper = Cipher.getInstance(this.pkiContext.getWrappingAlgorithm(), "BC");
            wrapper.init(Cipher.WRAP_MODE, wrappingKey);
            wrappedKey = wrapper.wrap(key);
        } catch (Throwable t) {
            throw new EncryptException(t);
        }
        logger.debug("Simetric key wrapped: " + wrappedKey);
        return wrappedKey;
    }

    /**
   * Unwraps the simmetric key from an EncryptedMessage.
   * 
   * @param wrappedKey
   * @return
   * @throws EncryptException
   */
    private Key unwrapKey(byte[] wrappedKey) throws EncryptException {
        if (wrappedKey == null) {
            throw new EncryptException("The wrapped Key can not be null.");
        }
        if (this.pkiContext == null) {
            throw new EncryptException("The PKIContext is not initialized.");
        }
        Key unwrappedKey = null;
        try {
            Cipher unwrapper = Cipher.getInstance(this.pkiContext.getWrappingAlgorithm(), "BC");
            unwrapper.init(Cipher.UNWRAP_MODE, this.pkiContext.getPrivateKey());
            unwrappedKey = unwrapper.unwrap(wrappedKey, this.pkiContext.getSymmetricKeyType(), Cipher.SECRET_KEY);
        } catch (Throwable t) {
            throw new EncryptException(t);
        }
        logger.debug("Simetric key unwrapped: " + unwrappedKey);
        return unwrappedKey;
    }

    /**
   * Obtiene la instancaia del map de datos del objeto.
   * 
   * @return
   */
    private Map getData() {
        if (this.data == null) {
            this.data = new HashMap();
        }
        return this.data;
    }

    private byte[] getEncryptedData() {
        byte[] encryptedData = (byte[]) this.getData().get(this.ENCRYPTED_DATA);
        logger.debug("[getEncryptedData]:: encryptedData: " + new String(encryptedData));
        return encryptedData;
    }

    private void setEncyptedData(byte[] ab) {
        logger.debug("[setEncyptedData.entrada]:: " + ab);
        this.getData().put(this.ENCRYPTED_DATA, ab);
    }

    private byte[] getRandomData() {
        byte[] iv = (byte[]) this.getData().get(this.RANDOM_DATA);
        logger.debug("[getRandomData]:: iv: " + iv);
        return iv;
    }

    private void setRandomData(byte[] ab) {
        logger.debug("[setRandomData.entrada]:: " + ab);
        this.getData().put(this.RANDOM_DATA, ab);
    }

    private byte[] getSimetricKeyCiphered() {
        return (byte[]) this.getData().get(this.SIMETRIC_KEY_CIPHERED);
    }

    private void setSimetricKeyCiphered(byte[] ab) {
        logger.debug("[setSimetricKeyCiphered.entrada]:: " + ab);
        this.getData().put(this.SIMETRIC_KEY_CIPHERED, ab);
    }

    /**
   * Vuelca el contenido de los datos del Bean en un XML.
   * 
   * @return
   */
    public String toXML() {
        XStream xstream = new XStream();
        return xstream.toXML(this.getData());
    }

    /**
   * Lee el contenido de los datos del Bean desde un XML.
   * 
   * @return
   */
    private void initDataFromXML(InputStream is) {
        XStream xstream = new XStream();
        this.data = (Map) xstream.fromXML(is);
    }

    /**
   * Vuelca un stream de entrada en uno de salida.
   * 
   * @param is
   * @param os
   * @throws IOException
   */
    public void writeToStream(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[8 * 1024];
        while (true) {
            int len = is.read(buffer);
            if (len < 0) {
                break;
            }
            os.write(buffer, 0, len);
        }
        os.flush();
        is.close();
        is = null;
    }

    public static void main(String[] args) {
        try {
            PropertyConfigurator.configure("etc/log4j.properties");
            logger.debug("Logger ON");
            File fConf = new File(GlobalWorkDirConfiguration.getDefaultRootPath(), "text_PKIContext.properties");
            logger.debug("fConf: " + fConf.getAbsolutePath());
            MainPropertiesConfigurator mpc = new MainPropertiesConfigurator(fConf);
            logger.debug("mpc: " + mpc);
            PKIContext context = new PKIContext(mpc);
            logger.debug("PKI Context Initialized.");
            AuditMessage am = new ApplicationAuditMessage(1, "source", "body");
            logger.debug("am: " + am);
            EncryptedMessage msg = new EncryptedMessage(context);
            InputStream is = new ByteArrayInputStream(am.getBody().getBytes());
            msg.encrypt(is, context.getPublicKey());
            is.close();
            logger.debug("Message encrypted");
            String encryptedMsg = msg.toXML();
            logger.debug("encryptedMsg: " + encryptedMsg);
            ByteArrayInputStream bais = new ByteArrayInputStream(encryptedMsg.getBytes());
            EncryptedMessage em = new EncryptedMessage(context);
            byte[] abDec = em.decrypt(bais);
            bais.close();
            logger.debug("Decrypted: " + new String(abDec));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

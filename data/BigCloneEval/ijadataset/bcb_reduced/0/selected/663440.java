package net.sourceforge.statelessfilter.backend.aescookie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.SignatureException;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sourceforge.statelessfilter.backend.ISessionData;
import net.sourceforge.statelessfilter.backend.support.CookieBackendSupport;
import net.sourceforge.statelessfilter.backend.support.CookieDataSupport;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Backend based on an AES-encrypted cookie. Cookie can be compressed to save
 * bandwidth.
 * 
 * <p>
 * Parameters :
 * </p>
 * <ul>
 * <li>cookiename : name of the cookie</li>
 * <li>compress : enable cookie compression (gzip) when value is "true"</li>
 * <li>key : key used to encrypt data</li>
 * <li>iv : iv used to encrypt data</li>
 * </ul>
 * 
 * @author Nicolas Richeton - Capgemini
 * 
 */
public class AESCookieBackend extends CookieBackendSupport {

    private static final String DESERIALIZE_ERROR = "Cannot deserialize session. A new one will be created";

    private static final String ENCRYPTION = "AES";

    private static final String ENCRYPTION_WITH_PARAM = "AES/CBC/PKCS5Padding";

    private static final String ID = "aescookie";

    private static final String PARAM_COMPRESS = "compress";

    private static final String PARAM_IV = "iv";

    private static final String PARAM_KEY = "key";

    private static final String SEPARATOR = "B";

    private boolean compress = true;

    private IvParameterSpec iv = null;

    Logger logger = LoggerFactory.getLogger(AESCookieBackend.class);

    private SecretKeySpec secretKey = null;

    public AESCookieBackend() {
        setCookieName("es");
    }

    /**
	 * @see com.capgemini.stateless.backend.plaincookie.ISessionBackend#destroy()
	 */
    @Override
    public void destroy() {
    }

    private byte[] getEncryptionBytes(String data, int length) {
        byte[] keyRaw = new byte[length];
        for (int i = 0; i < length; i++) {
            keyRaw[i] = 0;
        }
        byte[] dataRaw = Base64.decodeBase64(data);
        System.arraycopy(dataRaw, 0, keyRaw, 0, dataRaw.length > length ? length : dataRaw.length);
        return keyRaw;
    }

    /**
	 * @see com.capgemini.stateless.backend.plaincookie.ISessionBackend#getId()
	 */
    @Override
    public String getId() {
        return ID;
    }

    /**
	 * Loads key and iv for encryption and performs normal init.
	 * 
	 * @throws Exception
	 * @see com.capgemini.stateless.backend.plaincookie.ISessionBackend#init(java.util.Map)
	 */
    @Override
    public void init(Map<String, String> config) throws Exception {
        super.init(config);
        String compress = config.get(PARAM_COMPRESS);
        if (!StringUtils.isEmpty(compress)) {
            this.compress = Boolean.parseBoolean(compress);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Cookie name: '" + cookieName + "', compression: '" + this.compress + "'");
        }
        String key = config.get(PARAM_KEY);
        String iv = config.get(PARAM_IV);
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(iv)) {
            throw new Exception(ID + "." + PARAM_KEY + " or " + ID + "." + PARAM_IV + " parameter missing in /stateless.properties.");
        }
        secretKey = new SecretKeySpec(getEncryptionBytes(key, 16), ENCRYPTION);
        this.iv = new IvParameterSpec(getEncryptionBytes(iv, 16));
    }

    /**
	 * @see com.capgemini.stateless.backend.plaincookie.ISessionBackend#restore(javax.servlet.http.HttpServletRequest)
	 */
    @Override
    public ISessionData restore(HttpServletRequest request) {
        try {
            byte[] data = getCookieData(request, null);
            if (data != null) {
                int index = ArrayUtils.indexOf(data, SEPARATOR.getBytes()[0]);
                int size = Integer.parseInt(new String(ArrayUtils.subarray(data, 0, index)));
                data = ArrayUtils.subarray(data, index + 1, data.length);
                Cipher decryptCipher = Cipher.getInstance(ENCRYPTION_WITH_PARAM);
                decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
                data = decryptCipher.doFinal(data);
                data = ArrayUtils.subarray(data, 0, size + 1);
                InputStream inputStream = new ByteArrayInputStream(data);
                if (compress) {
                    inputStream = new GZIPInputStream(inputStream);
                }
                ObjectInputStream ois = new ObjectInputStream(inputStream);
                CookieDataSupport s = (CookieDataSupport) ois.readObject();
                if (s.isValid() && s.getRemoteAddress().equals(getFullRemoteAddr(request))) {
                    return s;
                }
            }
        } catch (Exception e) {
            logger.info(DESERIALIZE_ERROR, e);
        }
        return null;
    }

    /**
	 * @see net.sourceforge.statelessfilter.backend.support.CookieBackendSupport#save(net.sourceforge.statelessfilter.backend.ISessionData,
	 *      java.util.List, javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
    @Override
    public void save(ISessionData session, List<String> dirtyAttributes, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if (session != null) {
                CookieDataSupport cookieData = new CookieDataSupport(session);
                cookieData.setRemoteAddress(getFullRemoteAddr(request));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                OutputStream outputStream = baos;
                if (compress) {
                    outputStream = new GZIPOutputStream(outputStream);
                }
                ObjectOutputStream oos = new ObjectOutputStream(outputStream);
                oos.writeObject(cookieData);
                oos.close();
                outputStream.close();
                baos.close();
                byte[] data;
                try {
                    Cipher encryptCipher = Cipher.getInstance(ENCRYPTION_WITH_PARAM);
                    encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
                    data = encryptCipher.doFinal(baos.toByteArray());
                } catch (Exception e) {
                    throw new IOException(e.getMessage());
                }
                byte[] size = (data.length + SEPARATOR).getBytes();
                setCookieData(request, response, ArrayUtils.addAll(size, data));
                if (logger.isDebugEnabled()) {
                    logger.debug("Cookie size : " + ArrayUtils.addAll(size, data).length);
                }
            } else {
                setCookieData(request, response, null);
            }
        } catch (SignatureException e) {
            throw new IOException(e);
        }
    }
}

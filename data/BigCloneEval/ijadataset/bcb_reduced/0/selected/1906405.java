package net.sourceforge.statelessfilter.backend.jsonaescookie;

import java.io.IOException;
import java.security.SignatureException;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.codehaus.jackson.map.ObjectMapper;
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
public class JSONAESCookieBackend extends CookieBackendSupport {

    private static final String DESERIALIZE_ERROR = "Cannot deserialize session. A new one will be created";

    private static final String ENCRYPTION = "AES";

    private static final String ENCRYPTION_WITH_PARAM = "AES/CBC/PKCS5Padding";

    private static final String ID = "jsonaescookie";

    private static final String PARAM_IV = "iv";

    private static final String PARAM_KEY = "key";

    private static final String SEPARATOR = "B";

    private IvParameterSpec iv = null;

    Logger logger = LoggerFactory.getLogger(JSONAESCookieBackend.class);

    ObjectMapper mapper = null;

    private SecretKeySpec secretKey = null;

    public JSONAESCookieBackend() {
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
        mapper = new ObjectMapper();
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
                CookieDataSupport s = mapper.readValue(new String(data), CookieDataSupport.class);
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
                ensureStrings(session.getContent());
                String dataString = mapper.writeValueAsString(cookieData);
                byte[] data;
                try {
                    Cipher encryptCipher = Cipher.getInstance(ENCRYPTION_WITH_PARAM);
                    encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
                    data = encryptCipher.doFinal(dataString.getBytes());
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

    /**
	 * Throw IllegalArgumentException if the map does not contains only String
	 * objects.
	 * 
	 * @param map
	 */
    private void ensureStrings(Map<String, Object> map) {
        if (map == null || map.size() == 0) return;
        Set<String> keys = map.keySet();
        for (String key : keys) {
            if (!(map.get(key) instanceof String)) {
                throw new IllegalArgumentException(key + " is not a String. JSON stateless session only support string data.");
            }
        }
    }
}

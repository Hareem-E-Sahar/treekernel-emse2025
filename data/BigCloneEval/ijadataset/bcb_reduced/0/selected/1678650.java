package com.aimluck.eip.util;

import java.util.StringTokenizer;
import java.util.zip.CRC32;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.cayenne.DataRow;
import org.apache.commons.codec.binary.Base64;
import org.apache.jetspeed.capability.CapabilityMap;
import org.apache.jetspeed.capability.CapabilityMapFactory;
import org.apache.jetspeed.om.profile.Entry;
import org.apache.jetspeed.om.profile.Portlets;
import org.apache.jetspeed.om.registry.MediaTypeEntry;
import org.apache.jetspeed.services.Registry;
import org.apache.jetspeed.services.logging.JetspeedLogFactoryService;
import org.apache.jetspeed.services.logging.JetspeedLogger;
import org.apache.jetspeed.services.resources.JetspeedResources;
import org.apache.jetspeed.services.rundata.JetspeedRunData;
import org.apache.jetspeed.util.MimeType;
import org.apache.jetspeed.util.template.JetspeedLink;
import org.apache.jetspeed.util.template.JetspeedLinkFactory;
import org.apache.turbine.util.DynamicURI;
import org.apache.turbine.util.RunData;
import com.aimluck.eip.cayenne.om.account.EipMCompany;
import com.aimluck.eip.common.ALEipConstants;
import com.aimluck.eip.common.ALEipUser;
import com.aimluck.eip.services.config.ALConfigHandler.Property;
import com.aimluck.eip.services.config.ALConfigService;

/**
 * Aimluck EIP のユーティリティクラスです。
 * 
 */
public class ALCellularUtils {

    /** logger */
    private static final JetspeedLogger logger = JetspeedLogFactoryService.getLogger(ALCellularUtils.class.getName());

    /**
   * 携帯電話の固有 ID によるログイン認証時のアクセス URL に付加する値を取得します。
   * 
   * @param username
   * @return
   */
    public static String getCheckValueForCellLogin(String username, String userid) {
        if (username == null || username.length() == 0 || userid == null) {
            return "";
        }
        String marge = username + userid;
        CRC32 crc32 = new CRC32();
        crc32.update(marge.getBytes());
        long value = crc32.getValue();
        String base64value = null;
        try {
            base64value = new String(Base64.encodeBase64(String.valueOf(value).getBytes()));
        } catch (Exception e) {
        }
        return (base64value == null) ? "" : base64value.toLowerCase();
    }

    /**
   * 携帯電話の固有 ID を取得します。
   * 
   * @param rundata
   * @return
   */
    public static String getCellularUid(RunData rundata) {
        String password = "";
        JetspeedRunData data = (JetspeedRunData) rundata;
        String useragent = data.getRequest().getHeader("User-Agent");
        if (useragent != null && useragent.length() > 0) {
            useragent = useragent.trim();
            CapabilityMap cm = CapabilityMapFactory.getCapabilityMap(useragent);
            MimeType mime = cm.getPreferredType();
            if (mime != null) {
                MediaTypeEntry media = (MediaTypeEntry) Registry.getEntry(Registry.MEDIA_TYPE, cm.getPreferredMediaType());
                if ("docomo_imode".equals(media.getName())) {
                    int lastindex = useragent.lastIndexOf("ser");
                    if (lastindex >= 0) {
                        password = useragent.substring(lastindex, useragent.length());
                    }
                } else if ("docomo_foma".equals(media.getName())) {
                    StringTokenizer st = new StringTokenizer(useragent, ";");
                    String token = null;
                    while (st.hasMoreTokens()) {
                        if ((token = st.nextToken()).startsWith("ser")) {
                            password = token.trim();
                            break;
                        }
                    }
                } else if ("au".equals(media.getName())) {
                    String header = data.getRequest().getHeader("x-up-subno");
                    if (header != null && header.length() > 0) {
                        int index = header.indexOf("_");
                        if (index >= 0) {
                            password = header.substring(0, index);
                        }
                    }
                } else if ("vodafone".equals(media.getName())) {
                    int index = useragent.indexOf("SN");
                    if (index >= 0) {
                        int delta = -1;
                        if (useragent.startsWith("J-PHONE/4")) {
                            delta = 10;
                        } else if (useragent.startsWith("J-PHONE/5")) {
                            delta = 15;
                        } else if (useragent.startsWith("Vodafone")) {
                            delta = 15;
                        } else if (useragent.startsWith("SoftBank")) {
                            delta = 15;
                        }
                        if (index >= 0 && delta > 0) {
                            password = useragent.substring(index, index + 2 + delta);
                        }
                    } else {
                        String jphoneUid = data.getRequest().getHeader("x-jphone-uid");
                        if (jphoneUid != null) {
                            password = jphoneUid;
                        }
                    }
                }
            }
        }
        return password;
    }

    /**
   * アクセス元の端末が携帯電話であるかを判定します。
   * 
   * @param data
   * @return
   */
    public static boolean isCellularPhone(RunData data) {
        boolean isCellularPhone = false;
        CapabilityMap cm = CapabilityMapFactory.getCapabilityMap(data.getRequest().getHeader("User-Agent"));
        MimeType mime = cm.getPreferredType();
        if (mime != null) {
            MediaTypeEntry media = (MediaTypeEntry) Registry.getEntry(Registry.MEDIA_TYPE, cm.getPreferredMediaType());
            String mediatype = media.getName();
            if ("docomo_imode".equals(mediatype) || "docomo_foma".equals(mediatype) || "au".equals(mediatype) || "vodafone".equals(mediatype)) {
                isCellularPhone = true;
            }
        }
        return isCellularPhone;
    }

    /**
   * データベースの検索結果から、指定したキーに対応する値を取得します。
   * 
   * @param dataRow
   * @param key
   * @return
   */
    public static Object getObjFromDataRow(DataRow dataRow, String key) {
        String lowerKey = key.toLowerCase();
        if (dataRow.containsKey(lowerKey)) {
            return dataRow.get(lowerKey);
        } else {
            return dataRow.get(key.toUpperCase());
        }
    }

    /**
   * 携帯電話からのアクセス用 URL を取得します。
   * 
   * @param useraddr
   * @return
   */
    public static String getCellularUrl(RunData rundata, ALEipUser eipUser) {
        String url;
        String loginUrl = ALConfigService.get(Property.EXTERNAL_LOGIN_URL);
        if (loginUrl != null && loginUrl.length() > 0) {
            return loginUrl;
        }
        String key = eipUser.getName().getValue() + "_" + getCheckValueForCellLogin(eipUser.getName().getValue(), eipUser.getUserId().toString());
        EipMCompany record = ALEipUtils.getEipMCompany("1");
        String domain = ALServletUtils.getAccessUrl(record.getIpaddress(), record.getPort().intValue(), true);
        if (domain != null && domain.length() > 0) {
            url = domain + "?key=" + key;
        } else {
            url = "";
        }
        return url;
    }

    /**
   * 携帯電話からのアクセス用 key を取得します。
   * 
   * @param useraddr
   * @return
   */
    public static String getCellularKey(ALEipUser eipUser) {
        String key = eipUser.getName().getValue() + "_" + getCheckValueForCellLogin(eipUser.getName().getValue(), eipUser.getUserId().toString());
        return key;
    }

    /**
   * Triple DES で文字列を暗号化します。
   * 
   * @param plain
   *          暗号化対象文字列
   * @return 暗号化文字列
   * @throws Exception
   *           暗号化時の例外
   */
    public static String crypt3Des(String key, String plain) throws Exception {
        String KEY_CRPTY_ALGORITHM = "DESede";
        byte[] tripleDesKeyData = new byte[24];
        byte[] kyebyte = key.getBytes();
        int len = kyebyte.length;
        for (int i = 0; i < len; i++) {
            tripleDesKeyData[i] = kyebyte[i];
        }
        SecretKey secretKey = new SecretKeySpec(tripleDesKeyData, KEY_CRPTY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(KEY_CRPTY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedMessage = cipher.doFinal(plain.getBytes());
        return new String(Base64.encodeBase64(encryptedMessage));
    }

    /**
   * Triple DES で文字列を復号します。
   * 
   * @param plain
   *          復号対象文字列
   * @return 復号文字列
   * @throws Exception
   *           復号時の例外
   */
    @SuppressWarnings("unused")
    private static String decrypt3Des(String key, String plain) throws Exception {
        String KEY_CRPTY_ALGORITHM = "DESede";
        byte[] tripleDesKeyData = new byte[24];
        byte[] kyebyte = key.getBytes();
        int len = kyebyte.length;
        for (int i = 0; i < len; i++) {
            tripleDesKeyData[i] = kyebyte[i];
        }
        SecretKey secretKey = new SecretKeySpec(tripleDesKeyData, KEY_CRPTY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(KEY_CRPTY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        Base64 decoder = new Base64();
        byte[] decParam = decoder.decode(plain.trim().getBytes());
        return String.valueOf(cipher.doFinal(decParam));
    }

    /**
   * 指定したエントリー名のポートレットへの URI を取得します。
   * 
   * @param rundata
   * @param portletEntryName
   *          PSML ファイルに記述されているタグ entry の要素 parent
   * @return
   */
    public static String getPortletURIForCell(RunData rundata, String portletEntryId) {
        try {
            Portlets portlets = ((JetspeedRunData) rundata).getProfile().getDocument().getPortlets();
            if (portlets == null) {
                return null;
            }
            Entry[] entries = portlets.getEntriesArray();
            if (entries == null || entries.length <= 0) {
                return null;
            }
            int ent_length = entries.length;
            for (int j = 0; j < ent_length; j++) {
                if (entries[j].getId().equals(portletEntryId)) {
                    JetspeedLink jsLink = JetspeedLinkFactory.getInstance(rundata);
                    DynamicURI duri = jsLink.getLink(JetspeedLink.CURRENT, null, null, JetspeedLink.CURRENT, null);
                    duri = duri.addPathInfo(JetspeedResources.PATH_PORTLETID_KEY, entries[j].getId()).addQueryData(JetspeedResources.PATH_ACTION_KEY, "controls.Maximize").addQueryData(ALEipConstants.MODE, ALEipConstants.MODE_LIST);
                    return duri.toString();
                }
            }
        } catch (Exception ex) {
            logger.error("Exception", ex);
            return null;
        }
        return null;
    }
}

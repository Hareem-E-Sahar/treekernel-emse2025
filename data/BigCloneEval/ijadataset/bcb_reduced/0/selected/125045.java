package org.fressia.actions.rest;

import org.apache.commons.httpclient.URIException;
import org.fressia.core.ActionBase;
import org.fressia.core.security.XTrustProvider;
import org.fressia.util.RegExpUtils;
import org.fressia.util.RegExpUtils.Match;
import static org.testng.AssertJUnit.assertFalse;
import org.testng.Reporter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Base class for HTTP actions.
 * <p>
 * One of the key base functionalities is the 'resource references
 * resolution'. This features allows to specify parameters values as
 * resources references. A resource reference is a value enclosed by
 * '{}' which indicates the path to an external resource file -
 * the path can be absolute or relative; if the resource is not found
 * directly in the indicated path it will be searched in the classpath.
 * <p>
 * @author Alvaro Egana
 *
 */
public class HttpTestBase extends ActionBase {

    public HttpTestBase() {
    }

    /**
     * Just installs the provider which trusts all
     * server certificates.
     *
     */
    protected void initServerAuthentication() {
        XTrustProvider.install();
    }

    /**
     * Sets the client certificate info.
     *
     * @param certPath Path to the client certificate.
     * @param certPass Client certificate password.
     */
    protected void initClientAuthentication(String certPath, String certPass) {
        String keyStore = "javax.net.ssl.keyStore";
        String keyStoreType = "javax.net.ssl.keyStoreType";
        String keyStorePass = "javax.net.ssl.keyStorePassword";
        System.setProperty(keyStore, certPath);
        System.setProperty(keyStoreType, "PKCS12");
        System.setProperty(keyStorePass, certPass);
        Reporter.log("Keystore: " + System.getProperty(keyStore));
        Reporter.log("Keystore type: " + System.getProperty(keyStoreType));
        Reporter.log("Keystore password: " + System.getProperty(keyStorePass));
    }

    /**
     * Builds an URL resolving (optional)resource references.
     *
     * @param sourceUrl Source URL with (optional) resource references.
     * @return Well-formed URL.
     */
    protected String buildUrl(String sourceUrl) {
        String url = sourceUrl;
        try {
            url = parseUrl(url);
        } catch (URIException e) {
            Reporter.log("Fatal error.");
            assertFalse(e.getMessage(), true);
        }
        return url;
    }

    private String parseUrl(String url) throws URIException {
        String base = url;
        Map<String, String[]> params = null;
        int qi = url.indexOf("?");
        if (qi != -1) {
            base = url.substring(0, qi);
            params = getMap(url.substring(qi + 1));
        }
        String urlTail = "";
        if (params != null) {
            Iterator<String> it = params.keySet().iterator();
            String key;
            while (it.hasNext()) {
                key = it.next();
                for (String value : params.get(key)) {
                    if ("".equals(urlTail)) {
                        urlTail = "" + key + "=" + value;
                    } else {
                        urlTail = urlTail + "&" + key + "=" + value;
                    }
                }
            }
        }
        return base + "?" + urlTail;
    }

    private String applyEncoding(String input) throws UnsupportedEncodingException {
        return URLEncoder.encode(input, "US-ASCII");
    }

    private Map<String, String[]> getMap(String urlStringParams) {
        HashMap<String, String[]> parameters = new HashMap<String, String[]>();
        if (urlStringParams.length() > 0) {
            String[] urlParams = safeSplit(urlStringParams);
            HashMap<String, CopyOnWriteArraySet<String>> elements = new HashMap<String, CopyOnWriteArraySet<String>>();
            String[] keyAndValue;
            for (String param : urlParams) {
                keyAndValue = param.split("=");
                if (keyAndValue.length > 2) {
                    StringBuffer sb = new StringBuffer();
                    int i = 1;
                    for (i = 1; i < keyAndValue.length; i++) {
                        sb.append(keyAndValue[i]);
                        if (i < (keyAndValue.length - 1)) {
                            sb.append("=");
                        }
                    }
                    keyAndValue[1] = sb.toString();
                    keyAndValue = (String[]) resizeArray(keyAndValue, 2);
                }
                for (int i = 0; i < keyAndValue.length; i++) {
                    try {
                        keyAndValue[i] = applyEncoding(keyAndValue[i]);
                    } catch (UnsupportedEncodingException e) {
                    }
                }
                if (keyAndValue.length == 2) {
                    if (!elements.containsKey(keyAndValue[0])) {
                        elements.put(keyAndValue[0], new CopyOnWriteArraySet<String>());
                    }
                    elements.get(keyAndValue[0]).add(keyAndValue[1]);
                } else {
                    if (!elements.containsKey(keyAndValue[0])) {
                        elements.put(keyAndValue[0], new CopyOnWriteArraySet<String>());
                    }
                    elements.get(keyAndValue[0]).add("");
                }
            }
            for (String key : elements.keySet()) {
                CopyOnWriteArraySet<String> values = elements.get(key);
                String[] aux = new String[values.size()];
                parameters.put(key, elements.get(key).toArray(aux));
            }
        }
        return parameters;
    }

    /**
	 *  Splits a url tail considering that it may 
	 *  contains xml escape characters.
	 */
    private String[] safeSplit(String urlStringParams) {
        List<Match> matches = RegExpUtils.extractAll(urlStringParams, "&\\w+;", '\\');
        String[] params;
        int size = matches.size();
        if (size > 0) {
            HashMap<String, Match> escs = new HashMap<String, Match>();
            int i;
            String esc;
            for (i = 0; i < size; i++) {
                esc = getFressiaEscape(i);
                escs.put(esc, matches.get(i));
                urlStringParams = urlStringParams.replaceFirst(matches.get(i).getText(), esc);
            }
            params = urlStringParams.split("&");
            int j;
            for (i = 0; i < size; i++) {
                esc = getFressiaEscape(i);
                for (j = 0; j < params.length; j++) {
                    params[j] = params[j].replaceFirst(esc, escs.get(esc).getText());
                }
            }
        } else {
            params = urlStringParams.split("&");
        }
        return params;
    }

    private String getFressiaEscape(int i) {
        return "fressia-escape" + i + "";
    }

    @SuppressWarnings("unchecked")
    private Object resizeArray(Object oldArray, int newSize) {
        int oldSize = java.lang.reflect.Array.getLength(oldArray);
        Class elementType = oldArray.getClass().getComponentType();
        Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
        int preserveLength = Math.min(oldSize, newSize);
        if (preserveLength > 0) {
            System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
        }
        return newArray;
    }
}

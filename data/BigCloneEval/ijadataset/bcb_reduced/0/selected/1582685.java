package org.apache.xml.security.utils.resolver.implementations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.Base64;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.w3c.dom.Attr;

/**
 * A simple ResourceResolver for HTTP requests. This class handles only 'pure'
 * HTTP URIs which means without a fragment. The Fragment handling is done by the
 * {@link ResolverFragment} class.
 * <BR>
 * If the user has a corporate HTTP proxy which is to be used, the usage can be
 * switched on by setting properties for the resolver:
 * <PRE>
 * resourceResolver.setProperty("http.proxy.host", "proxy.company.com");
 * resourceResolver.setProperty("http.proxy.port", "8080");
 *
 * // if we need a password for the proxy
 * resourceResolver.setProperty("http.proxy.username", "proxyuser3");
 * resourceResolver.setProperty("http.proxy.password", "secretca");
 * </PRE>
 *
 * @author $Author: coheigea $
 * @see <A HREF="http://www.javaworld.com/javaworld/javatips/jw-javatip42_p.html">Java Tip 42: Write Java apps that work with proxy-based firewalls</A>
 * @see <A HREF="http://java.sun.com/j2se/1.4/docs/guide/net/properties.html">SUN J2SE docs for network properties</A>
 * @see <A HREF="http://metalab.unc.edu/javafaq/javafaq.html#proxy">The JAVA FAQ Question 9.5: How do I make Java work with a proxy server?</A>
 */
public class ResolverDirectHTTP extends ResourceResolverSpi {

    /** {@link org.apache.commons.logging} logging facility */
    private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(ResolverDirectHTTP.class);

    /** Field properties[] */
    private static final String properties[] = { "http.proxy.host", "http.proxy.port", "http.proxy.username", "http.proxy.password", "http.basic.username", "http.basic.password" };

    /** Field HttpProxyHost */
    private static final int HttpProxyHost = 0;

    /** Field HttpProxyPort */
    private static final int HttpProxyPort = 1;

    /** Field HttpProxyUser */
    private static final int HttpProxyUser = 2;

    /** Field HttpProxyPass */
    private static final int HttpProxyPass = 3;

    /** Field HttpProxyUser */
    private static final int HttpBasicUser = 4;

    /** Field HttpProxyPass */
    private static final int HttpBasicPass = 5;

    @Override
    public boolean engineIsThreadSafe() {
        return true;
    }

    /**
     * Method resolve
     *
     * @param uri
     * @param baseURI
     *
     * @throws ResourceResolverException
     * @return 
     * $todo$ calculate the correct URI from the attribute and the baseURI
     */
    public XMLSignatureInput engineResolve(Attr uri, String baseURI) throws ResourceResolverException {
        try {
            boolean useProxy = false;
            String proxyHost = engineGetProperty(ResolverDirectHTTP.properties[ResolverDirectHTTP.HttpProxyHost]);
            String proxyPort = engineGetProperty(ResolverDirectHTTP.properties[ResolverDirectHTTP.HttpProxyPort]);
            if ((proxyHost != null) && (proxyPort != null)) {
                useProxy = true;
            }
            String oldProxySet = null;
            String oldProxyHost = null;
            String oldProxyPort = null;
            if (useProxy) {
                if (log.isDebugEnabled()) {
                    log.debug("Use of HTTP proxy enabled: " + proxyHost + ":" + proxyPort);
                }
                oldProxySet = System.getProperty("http.proxySet");
                oldProxyHost = System.getProperty("http.proxyHost");
                oldProxyPort = System.getProperty("http.proxyPort");
                System.setProperty("http.proxySet", "true");
                System.setProperty("http.proxyHost", proxyHost);
                System.setProperty("http.proxyPort", proxyPort);
            }
            boolean switchBackProxy = ((oldProxySet != null) && (oldProxyHost != null) && (oldProxyPort != null));
            URI uriNew = null;
            try {
                uriNew = getNewURI(uri.getNodeValue(), baseURI);
            } catch (URISyntaxException ex) {
                throw new ResourceResolverException("generic.EmptyMessage", ex, uri, baseURI);
            }
            URL url = uriNew.toURL();
            URLConnection urlConnection = url.openConnection();
            {
                String proxyUser = engineGetProperty(ResolverDirectHTTP.properties[ResolverDirectHTTP.HttpProxyUser]);
                String proxyPass = engineGetProperty(ResolverDirectHTTP.properties[ResolverDirectHTTP.HttpProxyPass]);
                if ((proxyUser != null) && (proxyPass != null)) {
                    String password = proxyUser + ":" + proxyPass;
                    String encodedPassword = Base64.encode(password.getBytes("ISO-8859-1"));
                    urlConnection.setRequestProperty("Proxy-Authorization", encodedPassword);
                }
            }
            {
                String auth = urlConnection.getHeaderField("WWW-Authenticate");
                if (auth != null && auth.startsWith("Basic")) {
                    String user = engineGetProperty(ResolverDirectHTTP.properties[ResolverDirectHTTP.HttpBasicUser]);
                    String pass = engineGetProperty(ResolverDirectHTTP.properties[ResolverDirectHTTP.HttpBasicPass]);
                    if ((user != null) && (pass != null)) {
                        urlConnection = url.openConnection();
                        String password = user + ":" + pass;
                        String encodedPassword = Base64.encode(password.getBytes("ISO-8859-1"));
                        urlConnection.setRequestProperty("Authorization", "Basic " + encodedPassword);
                    }
                }
            }
            String mimeType = urlConnection.getHeaderField("Content-Type");
            InputStream inputStream = urlConnection.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte buf[] = new byte[4096];
            int read = 0;
            int summarized = 0;
            while ((read = inputStream.read(buf)) >= 0) {
                baos.write(buf, 0, read);
                summarized += read;
            }
            if (log.isDebugEnabled()) {
                log.debug("Fetched " + summarized + " bytes from URI " + uriNew.toString());
            }
            XMLSignatureInput result = new XMLSignatureInput(baos.toByteArray());
            result.setSourceURI(uriNew.toString());
            result.setMIMEType(mimeType);
            if (useProxy && switchBackProxy) {
                System.setProperty("http.proxySet", oldProxySet);
                System.setProperty("http.proxyHost", oldProxyHost);
                System.setProperty("http.proxyPort", oldProxyPort);
            }
            return result;
        } catch (MalformedURLException ex) {
            throw new ResourceResolverException("generic.EmptyMessage", ex, uri, baseURI);
        } catch (IOException ex) {
            throw new ResourceResolverException("generic.EmptyMessage", ex, uri, baseURI);
        }
    }

    /**
     * We resolve http URIs <I>without</I> fragment...
     *
     * @param uri
     * @param baseURI
     *  @return true if can be resolved
     */
    public boolean engineCanResolve(Attr uri, String baseURI) {
        if (uri == null) {
            if (log.isDebugEnabled()) {
                log.debug("quick fail, uri == null");
            }
            return false;
        }
        String uriNodeValue = uri.getNodeValue();
        if (uriNodeValue.equals("") || (uriNodeValue.charAt(0) == '#')) {
            if (log.isDebugEnabled()) {
                log.debug("quick fail for empty URIs and local ones");
            }
            return false;
        }
        if (log.isDebugEnabled()) {
            log.debug("I was asked whether I can resolve " + uriNodeValue);
        }
        if (uriNodeValue.startsWith("http:") || (baseURI != null && baseURI.startsWith("http:"))) {
            if (log.isDebugEnabled()) {
                log.debug("I state that I can resolve " + uriNodeValue);
            }
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug("I state that I can't resolve " + uriNodeValue);
        }
        return false;
    }

    /**
     * @inheritDoc 
     */
    public String[] engineGetPropertyKeys() {
        return (String[]) ResolverDirectHTTP.properties.clone();
    }

    private static URI getNewURI(String uri, String baseURI) throws URISyntaxException {
        URI newUri = null;
        if (baseURI == null || "".equals(baseURI)) {
            newUri = new URI(uri);
        } else {
            newUri = new URI(baseURI).resolve(uri);
        }
        if (newUri.getFragment() != null) {
            URI uriNewNoFrag = new URI(newUri.getScheme(), newUri.getSchemeSpecificPart(), null);
            return uriNewNoFrag;
        }
        return newUri;
    }
}

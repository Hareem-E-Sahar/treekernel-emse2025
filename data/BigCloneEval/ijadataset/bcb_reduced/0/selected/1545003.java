package com.sun.org.apache.xml.internal.security.utils.resolver.implementations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import com.sun.org.apache.xml.internal.utils.URI;
import com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolverException;
import com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolverSpi;
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
 *
 * @author $Author: mullan $
 * @see <A HREF="http://www.javaworld.com/javaworld/javatips/jw-javatip42_p.html">Java Tip 42: Write Java apps that work with proxy-based firewalls</A>
 * @see <A HREF="http://java.sun.com/j2se/1.4/docs/guide/net/properties.html">SUN J2SE docs for network properties</A>
 * @see <A HREF="http://metalab.unc.edu/javafaq/javafaq.html#proxy">The JAVA FAQ Question 9.5: How do I make Java work with a proxy server?</A>
 * $todo$ the proxy behaviour seems not to work; if a on-existing proxy is set, it works ?!?
 */
public class ResolverDirectHTTP extends ResourceResolverSpi {

    /** {@link java.util.logging} logging facility */
    static java.util.logging.Logger log = java.util.logging.Logger.getLogger(ResolverDirectHTTP.class.getName());

    /** Field properties[] */
    static final String properties[] = { "http.proxy.host", "http.proxy.port", "http.proxy.username", "http.proxy.password", "http.basic.username", "http.basic.password" };

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

    /**
    * Method resolve
    *
    * @param uri
    * @param BaseURI
    *
    * @throws ResourceResolverException
    * @return 
    * $todo$ calculate the correct URI from the attribute and the BaseURI
    */
    public XMLSignatureInput engineResolve(Attr uri, String BaseURI) throws ResourceResolverException {
        try {
            boolean useProxy = false;
            String proxyHost = engineGetProperty(ResolverDirectHTTP.properties[ResolverDirectHTTP.HttpProxyHost]);
            String proxyPort = engineGetProperty(ResolverDirectHTTP.properties[ResolverDirectHTTP.HttpProxyPort]);
            if ((proxyHost != null) && (proxyPort != null)) {
                useProxy = true;
            }
            String oldProxySet = (String) System.getProperties().get("http.proxySet");
            String oldProxyHost = (String) System.getProperties().get("http.proxyHost");
            String oldProxyPort = (String) System.getProperties().get("http.proxyPort");
            boolean switchBackProxy = ((oldProxySet != null) && (oldProxyHost != null) && (oldProxyPort != null));
            if (useProxy) {
                if (true) if (log.isLoggable(java.util.logging.Level.FINE)) log.log(java.util.logging.Level.FINE, "Use of HTTP proxy enabled: " + proxyHost + ":" + proxyPort);
                System.getProperties().put("http.proxySet", "true");
                System.getProperties().put("http.proxyHost", proxyHost);
                System.getProperties().put("http.proxyPort", proxyPort);
            }
            URI uriNew = getNewURI(uri.getNodeValue(), BaseURI);
            URI uriNewNoFrag = new URI(uriNew);
            uriNewNoFrag.setFragment(null);
            URL url = new URL(uriNewNoFrag.toString());
            URLConnection urlConnection = url.openConnection();
            {
                String proxyUser = engineGetProperty(ResolverDirectHTTP.properties[ResolverDirectHTTP.HttpProxyUser]);
                String proxyPass = engineGetProperty(ResolverDirectHTTP.properties[ResolverDirectHTTP.HttpProxyPass]);
                if ((proxyUser != null) && (proxyPass != null)) {
                    String password = proxyUser + ":" + proxyPass;
                    String encodedPassword = Base64.encode(password.getBytes());
                    urlConnection.setRequestProperty("Proxy-Authorization", encodedPassword);
                }
            }
            {
                String auth = urlConnection.getHeaderField("WWW-Authenticate");
                if (auth != null) {
                    if (auth.startsWith("Basic")) {
                        String user = engineGetProperty(ResolverDirectHTTP.properties[ResolverDirectHTTP.HttpBasicUser]);
                        String pass = engineGetProperty(ResolverDirectHTTP.properties[ResolverDirectHTTP.HttpBasicPass]);
                        if ((user != null) && (pass != null)) {
                            urlConnection = url.openConnection();
                            String password = user + ":" + pass;
                            String encodedPassword = Base64.encode(password.getBytes());
                            urlConnection.setRequestProperty("Authorization", "Basic " + encodedPassword);
                        }
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
            if (log.isLoggable(java.util.logging.Level.FINE)) log.log(java.util.logging.Level.FINE, "Fetched " + summarized + " bytes from URI " + uriNew.toString());
            XMLSignatureInput result = new XMLSignatureInput(baos.toByteArray());
            result.setSourceURI(uriNew.toString());
            result.setMIMEType(mimeType);
            if (switchBackProxy) {
                System.getProperties().put("http.proxySet", oldProxySet);
                System.getProperties().put("http.proxyHost", oldProxyHost);
                System.getProperties().put("http.proxyPort", oldProxyPort);
            }
            return result;
        } catch (MalformedURLException ex) {
            throw new ResourceResolverException("generic.EmptyMessage", ex, uri, BaseURI);
        } catch (IOException ex) {
            throw new ResourceResolverException("generic.EmptyMessage", ex, uri, BaseURI);
        }
    }

    /**
    * We resolve http URIs <I>without</I> fragment...
    *
    * @param uri
    * @param BaseURI
    *  @return true if can be resolved
    */
    public boolean engineCanResolve(Attr uri, String BaseURI) {
        if (uri == null) {
            if (log.isLoggable(java.util.logging.Level.FINE)) log.log(java.util.logging.Level.FINE, "quick fail, uri == null");
            return false;
        }
        String uriNodeValue = uri.getNodeValue();
        if (uriNodeValue.equals("") || (uriNodeValue.charAt(0) == '#')) {
            if (log.isLoggable(java.util.logging.Level.FINE)) log.log(java.util.logging.Level.FINE, "quick fail for empty URIs and local ones");
            return false;
        }
        if (true) if (log.isLoggable(java.util.logging.Level.FINE)) log.log(java.util.logging.Level.FINE, "I was asked whether I can resolve " + uriNodeValue);
        if (uriNodeValue.startsWith("http:") || BaseURI.startsWith("http:")) {
            if (true) if (log.isLoggable(java.util.logging.Level.FINE)) log.log(java.util.logging.Level.FINE, "I state that I can resolve " + uriNodeValue);
            return true;
        }
        if (true) if (log.isLoggable(java.util.logging.Level.FINE)) log.log(java.util.logging.Level.FINE, "I state that I can't resolve " + uriNodeValue);
        return false;
    }

    /**
    * @inheritDoc 
    */
    public String[] engineGetPropertyKeys() {
        return (String[]) ResolverDirectHTTP.properties.clone();
    }

    private URI getNewURI(String uri, String BaseURI) throws URI.MalformedURIException {
        if ((BaseURI == null) || "".equals(BaseURI)) {
            return new URI(uri);
        }
        return new URI(new URI(BaseURI), uri);
    }
}

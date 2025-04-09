package com.ca.commons.jndi;

import java.io.*;
import java.net.*;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.security.GeneralSecurityException;
import javax.net.ssl.*;
import javax.naming.NamingException;

/**
 * <p>Socket factory for SSL jndi links that returns an SSL socket.
 * It incorporates a keystore, which must contain the certs used
 * to authenticate the client.</p>
 *
 * <p>This code is based on sample code made freely available by author
 * Spencer W. Thomas on his web site http://hubris.engin.umich.edu/java/
 * On Wed 24 May, 2000.</p>
 *
 * <p><b>Warning</b></p>
 *
 * <p>This class relies heavily on an internal, single, static SSLSocketFactory.
 * multiple objects of this type in fact will use the same internal SSLSocketFactory.
 * (This is why a single static init() method sets up everything for the entire
 * class.)  The reason for this structure is that JndiSocketFactory is dynmaically
 * invoked by the jndi connection, and we have no other chance to initialise the
 * object.</p>
 */
public class JndiSocketFactory extends SSLSocketFactory {

    /**
     * This is the (static) factory internally shared between
     * all JndiSocketFactory objects.
     */
    private static SSLSocketFactory factory = null;

    /**
     * A single default object of this class.  It is
     * initialised when first called for, and then reused
     * whenever called again.
     */
    private static JndiSocketFactory default_factory = null;

    private static KeyStore clientKeystore;

    /**
     * The sun 'JKS' keystore is the simplest and most commonly
     * available keystore type.
     */
    private static final String DEFAULT_KEYSTORE_TYPE = "JKS";

    /**
     * The 'KSE' keystore is used by the Glen Iris group to
     * wrap openssl calls. It has some peculiarities requiring
     * special handling.
     */
    private static final String PKI_INTERNAL_TYPE = "KSE";

    /**
     *  the class loader to use for loading security providers
     *  'n stuff.  Defaults to the system loader.
     */
    private static ClassLoader myClassLoader = null;

    /**
     *    Register a custom class loader to be used by
     *    the class when getting security providers.
     */
    public static void setClassLoader(ClassLoader newLoader) {
        myClassLoader = newLoader;
    }

    /**
     *  checks that myClassLoader is initialised, uses the System
     *  default loader if it isn't, and returns the guaranteed
     *  initialised loader.
     */
    private static ClassLoader getClassLoader() {
        if (myClassLoader == null) myClassLoader = ClassLoader.getSystemClassLoader();
        return myClassLoader;
    }

    /**
     *  Enable debugging...
     */
    public static void setDebugOn() {
        System.setProperty("javax.net.debug", "ssl handshake verbose");
    }

    /**
     *    <p>Initialize the socket factory with a particular key store(s) and
     *    security provider.  The minimum requirement is for a keystore
     *    containing trusted directory servers (the 'castore', or trusted
     *    certificate authority store, since the servers are usually signed
     *    by a common CA, whose cert would be held in this file).</p>
     *
     *    <p>Further options include a private key store (the 'clientstore')
     *    that allows for client-authenticated ssl and SASL).</p>
     *
     *    <p>Finally, it is possible to configure a non-standard keystore
     *    type and security provider.  The keystore type defaults to Sun's
     *    JKS (at time of writting, the only keystore type that the default
     *    Sun security provider will handle).</p>
     *
     *    <p>Nb. - it is possible to set a custom class loader (using
     *    'registerClassLoader()' ) in which case this loader can be used
     *    to load the security provider.</p>
     *
     *    @param caKeystoreFile      A keystore file name of public certificates (trusted CA signs)
     *    @param clientKeystoreFile  A keystore file name of the client's certificates, containing private keys.
     *                               (may be null if only simple, 'server authenticated' ssl is being used).
     *    @param caPassphrase        A password for the caKeystoreFile certificate.
     *                               (may be null if only simple, 'server authenticated' ssl is being used, and keystore type is 'JKS').
     *                               <b>Calling Program must manually clear passphrase after init() call.</b>
     *    @param clientPassphrase    A password for the clientKeystoreFile certificate.
     *                               (may be null if only simple, 'server authenticated' ssl is being used).
     *                               <b>Calling Program must manually clear passphrase after init() call.</b>
     *    @param caKeystoreType      The type of cakeystore file. (null => 'JKS')
     *    @param clientKeystoreType  The type of clientkeystore file. (null => 'JKS')
     */
    public static void init(String caKeystoreFile, String clientKeystoreFile, char[] caPassphrase, char[] clientPassphrase, String caKeystoreType, String clientKeystoreType) throws NamingException {
        if (default_factory != null) return;
        try {
            checkFileSanity(caKeystoreFile, clientKeystoreFile, clientPassphrase);
            if (caKeystoreFile == null) caKeystoreFile = clientKeystoreFile;
            SSLContext sslctx;
            String protocol = System.getProperty("sslversion", "TLS");
            if (!"TLS".equals(protocol)) System.out.println("SECURITY: Using non-standard ssl version: '" + protocol + "'");
            sslctx = SSLContext.getInstance(protocol);
            KeyManagerFactory clientKeyManagerFactory = null;
            TrustManagerFactory caTrustManagerFactory;
            KeyStore caKeystore;
            if ((clientPassphrase != null) && (clientPassphrase.length > 0)) {
                if (PKI_INTERNAL_TYPE.equals(clientKeystoreType)) {
                    try {
                        Class c = getClassLoader().loadClass("com.ca.commons.security.openssl.ParsePkcs12");
                        if (c == null) {
                            System.out.println("PKI internal error");
                            return;
                        }
                        Constructor constructor = c.getConstructor(new Class[] { String.class, byte[].class });
                        int size = clientPassphrase.length;
                        byte[] password = new byte[size];
                        for (int i = 0; i < size; i++) password[i] = (byte) clientPassphrase[i];
                        Object pkcs12Parser = constructor.newInstance(new Object[] { clientKeystoreFile, password });
                        Method getSunKeyStore = c.getMethod("getSunKeyStore", new Class[] { String.class });
                        clientKeystore = (KeyStore) getSunKeyStore.invoke(pkcs12Parser, new Object[] { "MyFriend" });
                        for (int i = 0; i < size; i++) password[i] = 0;
                    } catch (Exception e) {
                        System.err.println("unable to load pkcs12 parser (not in class path?)");
                        e.printStackTrace();
                        return;
                    }
                } else {
                    if (clientKeystoreType == null) clientKeystoreType = DEFAULT_KEYSTORE_TYPE;
                    clientKeystore = KeyStore.getInstance(clientKeystoreType);
                    if (clientKeystoreFile != null) clientKeystore.load(new FileInputStream(clientKeystoreFile), clientPassphrase);
                }
                clientKeyManagerFactory = KeyManagerFactory.getInstance("SunX509");
                clientKeyManagerFactory.init(clientKeystore, clientPassphrase);
            }
            KeyManager[] keyManagers = null;
            if (clientKeyManagerFactory != null) keyManagers = clientKeyManagerFactory.getKeyManagers();
            if (caKeystoreType == null) caKeystoreType = DEFAULT_KEYSTORE_TYPE;
            caKeystore = KeyStore.getInstance(caKeystoreType);
            if (caKeystoreFile != null) {
                caKeystore.load(new FileInputStream(caKeystoreFile), caPassphrase);
            }
            caTrustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            caTrustManagerFactory.init(caKeystore);
            TrustManager[] trustManagers = caTrustManagerFactory.getTrustManagers();
            sslctx.init(keyManagers, trustManagers, null);
            synchronized (JndiSocketFactory.class) {
                factory = sslctx.getSocketFactory();
                default_factory = new JndiSocketFactory();
            }
        } catch (GeneralSecurityException e) {
            NamingException ne = new NamingException("security error: unable to initialise JndiSocketFactory");
            ne.initCause(e);
            throw ne;
        } catch (IOException e) {
            NamingException ne = new NamingException("file access error: unable to initialise JndiSocketFactory");
            ne.initCause(e);
            throw ne;
        }
    }

    /**
     *    Checks that the files containing the keystores really exist.
     *    Throws an exception (that can be bubbled through to the gui)
     *    if they don't.  This is much clearer than relying on the
     *    Sun ssl stuff to meaningfully report back the error :-).
     */
    private static void checkFileSanity(String caKeystoreFile, String clientKeystoreFile, char[] clientPassphrase) throws NamingException {
        if (clientKeystoreFile == null && caKeystoreFile == null) throw new NamingException("SSL Initialisation error: No valid keystore files available.");
        if (caKeystoreFile != null) if (new File(caKeystoreFile).exists() == false) throw new NamingException("SSL Initialisation error: file '" + caKeystoreFile + "' does not exist.");
        if (clientKeystoreFile != null && clientPassphrase != null) if (new File(clientKeystoreFile).exists() == false) throw new NamingException("SSL Initialisation error: file '" + clientKeystoreFile + "' does not exist.");
    }

    /**
     * Constructor
     */
    public JndiSocketFactory() {
    }

    /**
     * Return an instance of this class.
     *
     * @return		An instance of JndiSocketFactory.
     */
    public static SocketFactory getDefault() {
        synchronized (JndiSocketFactory.class) {
            if (default_factory == null) default_factory = new JndiSocketFactory();
        }
        return (SocketFactory) default_factory;
    }

    public static KeyStore getClientKeyStore() {
        return clientKeystore;
    }

    /**
     * Return an SSLSocket (upcast to Socket) given host and port.
     *
     * @param host	Name of the host to which the socket will be opened.
     * @param port	Port to connect to.
     * @return		An SSLSocket instance (as a Socket).
     * @throws	IOException	If the connection can't be established.
     * @throws	UnknownHostException	If the host is not known.
     */
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return factory.createSocket(host, port);
    }

    /**
     * Return an SSLSocket (upcast to Socket) given host and port.
     *
     * @param host	Address of the server host.
     * @param port	Port to connect to.
     * @return		An SSLSocket instance (as a Socket).
     * @throws	IOException	If the connection can't be established.
     * @throws	UnknownHostException	If the host is not known.
     */
    public Socket createSocket(InetAddress host, int port) throws IOException, UnknownHostException {
        return factory.createSocket(host, port);
    }

    /**
     * Return an SSLSocket (upcast to Socket) given host and port.
     * The client is bound to the specified network address and port.
     *
     * @param host	Address of the server host.
     * @param port	Port to connect to.
     * @param client_host	Address of this (client) host.
     * @return		An SSLSocket instance (as a Socket).
     * @throws	IOException	If the connection can't be established.
     * @throws	UnknownHostException	If the host is not known.
     */
    public Socket createSocket(InetAddress host, int port, InetAddress client_host, int client_port) throws IOException, UnknownHostException {
        return factory.createSocket(host, port, client_host, client_port);
    }

    /**
     * Return an SSLSocket (upcast to Socket) given host and port.
     * The client is bound to the specified network address and port.
     *
     * @param host	Address of the server host.
     * @param port	Port to connect to.
     * @param client_host	Address of this (client) host.
     * @return		An SSLSocket instance (as a Socket).
     * @throws	IOException	If the connection can't be established.
     * @throws	UnknownHostException	If the host is not known.
     */
    public Socket createSocket(String host, int port, InetAddress client_host, int client_port) throws IOException, UnknownHostException {
        return factory.createSocket(host, port, client_host, client_port);
    }

    /**
     * Return an SSLSocket layered on top of the given Socket.
     */
    public Socket createSocket(Socket socket, String host, int port, boolean autoclose) throws IOException, UnknownHostException {
        return factory.createSocket(socket, host, port, autoclose);
    }

    /**
     * Return default cipher suites.
     */
    public String[] getDefaultCipherSuites() {
        return factory.getDefaultCipherSuites();
    }

    /**
     * Return supported cipher suites.
     */
    public String[] getSupportedCipherSuites() {
        return factory.getSupportedCipherSuites();
    }
}

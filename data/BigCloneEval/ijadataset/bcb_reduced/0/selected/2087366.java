package test.unit.be.fedict.eid.applet.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.RSAKeyGenParameterSpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.testing.ServletTester;
import be.fedict.eid.applet.service.Address;
import be.fedict.eid.applet.service.AppletServiceServlet;
import be.fedict.eid.applet.service.Identity;
import be.fedict.eid.applet.shared.AppletProtocolMessageCatalog;
import be.fedict.eid.applet.shared.HelloMessage;
import be.fedict.eid.applet.shared.IdentificationRequestMessage;
import be.fedict.eid.applet.shared.IdentityDataMessage;
import be.fedict.eid.applet.shared.protocol.Transport;
import be.fedict.eid.applet.shared.protocol.Unmarshaller;

public class AppletServiceServletTest {

    private static final Log LOG = LogFactory.getLog(AppletServiceServletTest.class);

    private ServletTester servletTester;

    private String location;

    private String sslLocation;

    private KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = new SecureRandom();
        keyPairGenerator.initialize(new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4), random);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    private SubjectKeyIdentifier createSubjectKeyId(PublicKey publicKey) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(publicKey.getEncoded());
        SubjectPublicKeyInfo info = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(bais).readObject());
        return new SubjectKeyIdentifier(info);
    }

    private AuthorityKeyIdentifier createAuthorityKeyId(PublicKey publicKey) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(publicKey.getEncoded());
        SubjectPublicKeyInfo info = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(bais).readObject());
        return new AuthorityKeyIdentifier(info);
    }

    private void persistKey(File pkcs12keyStore, PrivateKey privateKey, X509Certificate certificate, char[] keyStorePassword, char[] keyEntryPassword) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore keyStore = KeyStore.getInstance("pkcs12");
        keyStore.load(null, keyStorePassword);
        keyStore.setKeyEntry("default", privateKey, keyEntryPassword, new Certificate[] { certificate });
        FileOutputStream keyStoreOut = new FileOutputStream(pkcs12keyStore);
        keyStore.store(keyStoreOut, keyStorePassword);
        keyStoreOut.close();
    }

    private X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String subjectDn, DateTime notBefore, DateTime notAfter) throws IOException, InvalidKeyException, IllegalStateException, NoSuchAlgorithmException, SignatureException, CertificateException {
        PublicKey subjectPublicKey = keyPair.getPublic();
        PrivateKey issuerPrivateKey = keyPair.getPrivate();
        String signatureAlgorithm = "SHA1WithRSAEncryption";
        X509V3CertificateGenerator certificateGenerator = new X509V3CertificateGenerator();
        certificateGenerator.reset();
        certificateGenerator.setPublicKey(subjectPublicKey);
        certificateGenerator.setSignatureAlgorithm(signatureAlgorithm);
        certificateGenerator.setNotBefore(notBefore.toDate());
        certificateGenerator.setNotAfter(notAfter.toDate());
        X509Principal issuerDN = new X509Principal(subjectDn);
        certificateGenerator.setIssuerDN(issuerDN);
        certificateGenerator.setSubjectDN(new X509Principal(subjectDn));
        certificateGenerator.setSerialNumber(new BigInteger(128, new SecureRandom()));
        certificateGenerator.addExtension(X509Extensions.SubjectKeyIdentifier, false, createSubjectKeyId(subjectPublicKey));
        PublicKey issuerPublicKey;
        issuerPublicKey = subjectPublicKey;
        certificateGenerator.addExtension(X509Extensions.AuthorityKeyIdentifier, false, createAuthorityKeyId(issuerPublicKey));
        certificateGenerator.addExtension(X509Extensions.BasicConstraints, false, new BasicConstraints(true));
        X509Certificate certificate;
        certificate = certificateGenerator.generate(issuerPrivateKey);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certificate.getEncoded()));
        return certificate;
    }

    private static int getFreePort() throws Exception {
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        serverSocket.close();
        return port;
    }

    @Before
    public void setUp() throws Exception {
        this.servletTester = new ServletTester();
        this.servletTester.addServlet(AppletServiceServlet.class, "/");
        Security.addProvider(new BouncyCastleProvider());
        KeyPair keyPair = generateKeyPair();
        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusMonths(1);
        X509Certificate certificate = generateSelfSignedCertificate(keyPair, "CN=localhost", notBefore, notAfter);
        File tmpP12File = File.createTempFile("ssl-", ".p12");
        LOG.debug("p12 file: " + tmpP12File.getAbsolutePath());
        persistKey(tmpP12File, keyPair.getPrivate(), certificate, "secret".toCharArray(), "secret".toCharArray());
        SslSocketConnector sslSocketConnector = new SslSocketConnector();
        sslSocketConnector.setKeystore(tmpP12File.getAbsolutePath());
        sslSocketConnector.setTruststore(tmpP12File.getAbsolutePath());
        sslSocketConnector.setTruststoreType("pkcs12");
        sslSocketConnector.setKeystoreType("pkcs12");
        sslSocketConnector.setPassword("secret");
        sslSocketConnector.setKeyPassword("secret");
        sslSocketConnector.setTrustPassword("secret");
        sslSocketConnector.setMaxIdleTime(30000);
        int sslPort = getFreePort();
        sslSocketConnector.setPort(sslPort);
        this.servletTester.getContext().getServer().addConnector(sslSocketConnector);
        this.sslLocation = "https://localhost:" + sslPort + "/";
        this.servletTester.start();
        this.location = this.servletTester.createSocketConnector(true);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManager trustManager = new TestTrustManager(certificate);
        sslContext.init(null, new TrustManager[] { trustManager }, null);
        SSLContext.setDefault(sslContext);
    }

    private static class TestTrustManager implements X509TrustManager {

        private final X509Certificate serverCertificate;

        public TestTrustManager(X509Certificate serverCertificate) {
            this.serverCertificate = serverCertificate;
        }

        public void checkClientTrusted(X509Certificate[] chain, String authnType) throws CertificateException {
            throw new CertificateException("not implemented");
        }

        public void checkServerTrusted(X509Certificate[] chain, String authnType) throws CertificateException {
            if (false == this.serverCertificate.equals(chain[0])) {
                throw new CertificateException("server certificate not trusted");
            }
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    @After
    public void tearDown() throws Exception {
        this.servletTester.stop();
    }

    @Test
    public void get() throws Exception {
        LOG.debug("URL: " + this.location);
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(this.location);
        int result = httpClient.executeMethod(getMethod);
        assertEquals(HttpServletResponse.SC_OK, result);
        String responseBody = getMethod.getResponseBodyAsString();
        LOG.debug("Response body: " + responseBody);
        assertTrue(responseBody.indexOf("applet service") != 1);
    }

    @Test
    public void doPostRequiresSSL() throws Exception {
        LOG.debug("URL: " + this.location);
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(this.location);
        int result = httpClient.executeMethod(postMethod);
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, result);
    }

    @Test
    public void sslPostIdentityMessage() throws Exception {
        byte[] idFile = IOUtils.toByteArray(AppletServiceServletTest.class.getResourceAsStream("/id-alice.tlv"));
        LOG.debug("SSL URL: " + this.sslLocation);
        HttpClient httpClient = new HttpClient();
        HelloMessage helloMessage = new HelloMessage();
        PostMethod postMethod = new PostMethod(this.sslLocation);
        PostMethodHttpTransmitter httpTransmitter = new PostMethodHttpTransmitter(postMethod);
        Transport.transfer(helloMessage, httpTransmitter);
        int result = httpClient.executeMethod(postMethod);
        assertEquals(HttpServletResponse.SC_OK, result);
        Header setCookieHeader = postMethod.getResponseHeader("Set-Cookie");
        String setCookieValue = setCookieHeader.getValue();
        int sessionIdIdx = setCookieValue.indexOf("JSESSIONID=") + "JESSSIONID=".length();
        String sessionId = setCookieValue.substring(sessionIdIdx, setCookieValue.indexOf(";", sessionIdIdx));
        LOG.debug("session id: " + sessionId);
        postMethod = new PostMethod(this.sslLocation);
        postMethod.addRequestHeader("X-AppletProtocol-Version", "1");
        postMethod.addRequestHeader("X-AppletProtocol-Type", "IdentityDataMessage");
        postMethod.addRequestHeader("X-AppletProtocol-IdentityFileSize", Integer.toString(idFile.length));
        RequestEntity requestEntity = new ByteArrayRequestEntity(idFile);
        postMethod.setRequestEntity(requestEntity);
        result = httpClient.executeMethod(postMethod);
        assertEquals(HttpServletResponse.SC_OK, result);
        HttpSession httpSession = this.servletTester.getContext().getSessionHandler().getSessionManager().getHttpSession(sessionId);
        Identity identity = (Identity) httpSession.getAttribute("eid.identity");
        assertNotNull(identity);
        assertEquals("Alice Geldigekaart2266", identity.firstName);
        Address address = (Address) httpSession.getAttribute("eid.address");
        assertNull(address);
    }

    @Test
    public void sslPostIdentityMessageViaTransport() throws Exception {
        byte[] idFile = IOUtils.toByteArray(AppletServiceServletTest.class.getResourceAsStream("/id-alice.tlv"));
        LOG.debug("SSL URL: " + this.sslLocation);
        HttpClient httpClient = new HttpClient();
        HelloMessage helloMessage = new HelloMessage();
        PostMethod postMethod = new PostMethod(this.sslLocation);
        PostMethodHttpTransmitter httpTransmitter = new PostMethodHttpTransmitter(postMethod);
        Transport.transfer(helloMessage, httpTransmitter);
        int result = httpClient.executeMethod(postMethod);
        assertEquals(HttpServletResponse.SC_OK, result);
        Header setCookieHeader = postMethod.getResponseHeader("Set-Cookie");
        String setCookieValue = setCookieHeader.getValue();
        int sessionIdIdx = setCookieValue.indexOf("JSESSIONID=") + "JSESSIONID=".length();
        String sessionId = setCookieValue.substring(sessionIdIdx, setCookieValue.indexOf(";", sessionIdIdx));
        LOG.debug("session id: " + sessionId);
        postMethod = new PostMethod(this.sslLocation);
        httpTransmitter = new PostMethodHttpTransmitter(postMethod);
        IdentityDataMessage identityDataMessage = new IdentityDataMessage();
        identityDataMessage.identityFileSize = idFile.length;
        identityDataMessage.body = idFile;
        Transport.transfer(identityDataMessage, httpTransmitter);
        result = httpClient.executeMethod(postMethod);
        assertEquals(HttpServletResponse.SC_OK, result);
        HttpSession httpSession = this.servletTester.getContext().getSessionHandler().getSessionManager().getHttpSession(sessionId);
        Identity identity = (Identity) httpSession.getAttribute("eid.identity");
        assertNotNull(identity);
        assertEquals("Alice Geldigekaart2266", identity.firstName);
        Address address = (Address) httpSession.getAttribute("eid.address");
        assertNull(address);
    }

    @Test
    public void helloMessage() throws Exception {
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(this.sslLocation);
        HelloMessage helloMessage = new HelloMessage();
        PostMethodHttpTransmitter httpTransmitter = new PostMethodHttpTransmitter(postMethod);
        Transport.transfer(helloMessage, httpTransmitter);
        int result = httpClient.executeMethod(postMethod);
        assertEquals(HttpServletResponse.SC_OK, result);
        Unmarshaller unmarshaller = new Unmarshaller(new AppletProtocolMessageCatalog());
        PostMethodHttpReceiver httpReceiver = new PostMethodHttpReceiver(postMethod);
        Object resultMessageObject = unmarshaller.receive(httpReceiver);
        assertTrue(resultMessageObject instanceof IdentificationRequestMessage);
    }
}

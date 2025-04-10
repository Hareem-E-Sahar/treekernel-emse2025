package test.unit.be.fedict.eid.applet.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.security.KeyPair;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import be.fedict.eid.applet.service.AppletServiceServlet;
import be.fedict.eid.applet.service.EIdData;
import be.fedict.eid.applet.service.Identity;
import be.fedict.eid.applet.service.impl.RequestContext;
import be.fedict.eid.applet.service.impl.handler.IdentityDataMessageHandler;
import be.fedict.eid.applet.service.spi.IdentityIntegrityService;
import be.fedict.eid.applet.shared.IdentityDataMessage;

public class IdentityDataMessageHandlerTest {

    private static final Log LOG = LogFactory.getLog(IdentityDataMessageHandlerTest.class);

    private IdentityDataMessageHandler testedInstance;

    @Before
    public void setUp() throws Exception {
        this.testedInstance = new IdentityDataMessageHandler();
        IdentityIntegrityTestService.reset();
        AuditTestService.reset();
    }

    @Test
    public void testHandleMessage() throws Exception {
        ServletConfig mockServletConfig = EasyMock.createMock(ServletConfig.class);
        Map<String, String> httpHeaders = new HashMap<String, String>();
        HttpSession mockHttpSession = EasyMock.createMock(HttpSession.class);
        HttpServletRequest mockServletRequest = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(mockServletConfig.getInitParameter("IdentityIntegrityService")).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter("IdentityIntegrityServiceClass")).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter("AuditService")).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter("AuditServiceClass")).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter("SkipNationalNumberCheck")).andStubReturn(null);
        mockHttpSession.setAttribute(EasyMock.eq("eid.identity"), EasyMock.isA(Identity.class));
        EasyMock.expect(mockHttpSession.getAttribute("eid")).andStubReturn(null);
        mockHttpSession.setAttribute(EasyMock.eq("eid"), EasyMock.isA(EIdData.class));
        EasyMock.expect(mockHttpSession.getAttribute(RequestContext.INCLUDE_ADDRESS_SESSION_ATTRIBUTE)).andStubReturn(false);
        EasyMock.expect(mockHttpSession.getAttribute(RequestContext.INCLUDE_CERTIFICATES_SESSION_ATTRIBUTE)).andStubReturn(false);
        EasyMock.expect(mockHttpSession.getAttribute(RequestContext.INCLUDE_PHOTO_SESSION_ATTRIBUTE)).andStubReturn(false);
        EasyMock.expect(mockServletConfig.getInitParameter(IdentityDataMessageHandler.INCLUDE_DATA_FILES)).andReturn(null);
        byte[] idFile = "foobar-id-file".getBytes();
        IdentityDataMessage message = new IdentityDataMessage();
        message.idFile = idFile;
        EasyMock.replay(mockServletConfig, mockHttpSession, mockServletRequest);
        AppletServiceServlet.injectInitParams(mockServletConfig, this.testedInstance);
        this.testedInstance.init(mockServletConfig);
        this.testedInstance.handleMessage(message, httpHeaders, mockServletRequest, mockHttpSession);
        EasyMock.verify(mockServletConfig, mockHttpSession, mockServletRequest);
    }

    @Test
    public void testHandleMessageWithIntegrityCheck() throws Exception {
        KeyPair rootKeyPair = MiscTestUtils.generateKeyPair();
        KeyPair rrnKeyPair = MiscTestUtils.generateKeyPair();
        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusYears(1);
        X509Certificate rootCertificate = MiscTestUtils.generateCertificate(rootKeyPair.getPublic(), "CN=TestRootCA", notBefore, notAfter, null, rootKeyPair.getPrivate(), true, 0, null, null);
        X509Certificate rrnCertificate = MiscTestUtils.generateCertificate(rrnKeyPair.getPublic(), "CN=TestNationalRegistration", notBefore, notAfter, null, rootKeyPair.getPrivate(), false, 0, null, null);
        ServletConfig mockServletConfig = EasyMock.createMock(ServletConfig.class);
        Map<String, String> httpHeaders = new HashMap<String, String>();
        HttpSession mockHttpSession = EasyMock.createMock(HttpSession.class);
        HttpServletRequest mockServletRequest = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(mockServletConfig.getInitParameter("IdentityIntegrityService")).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter("IdentityIntegrityServiceClass")).andStubReturn(IdentityIntegrityTestService.class.getName());
        EasyMock.expect(mockServletConfig.getInitParameter("AuditService")).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter("AuditServiceClass")).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter("SkipNationalNumberCheck")).andStubReturn(null);
        EasyMock.expect(mockHttpSession.getAttribute("eid.identifier")).andStubReturn(null);
        mockHttpSession.setAttribute(EasyMock.eq("eid.identity"), EasyMock.isA(Identity.class));
        EasyMock.expect(mockHttpSession.getAttribute("eid")).andStubReturn(null);
        mockHttpSession.setAttribute(EasyMock.eq("eid"), EasyMock.isA(EIdData.class));
        EasyMock.expect(mockHttpSession.getAttribute(RequestContext.INCLUDE_ADDRESS_SESSION_ATTRIBUTE)).andStubReturn(false);
        EasyMock.expect(mockHttpSession.getAttribute(RequestContext.INCLUDE_CERTIFICATES_SESSION_ATTRIBUTE)).andStubReturn(false);
        EasyMock.expect(mockHttpSession.getAttribute(RequestContext.INCLUDE_PHOTO_SESSION_ATTRIBUTE)).andStubReturn(false);
        EasyMock.expect(mockServletConfig.getInitParameter(IdentityDataMessageHandler.INCLUDE_DATA_FILES)).andReturn(null);
        byte[] idFile = "foobar-id-file".getBytes();
        IdentityDataMessage message = new IdentityDataMessage();
        message.idFile = idFile;
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(rrnKeyPair.getPrivate());
        signature.update(idFile);
        byte[] idFileSignature = signature.sign();
        message.identitySignatureFile = idFileSignature;
        message.rrnCertFile = rrnCertificate.getEncoded();
        message.rootCertFile = rootCertificate.getEncoded();
        EasyMock.replay(mockServletConfig, mockHttpSession, mockServletRequest);
        AppletServiceServlet.injectInitParams(mockServletConfig, this.testedInstance);
        this.testedInstance.init(mockServletConfig);
        this.testedInstance.handleMessage(message, httpHeaders, mockServletRequest, mockHttpSession);
        EasyMock.verify(mockServletConfig, mockHttpSession, mockServletRequest);
        assertEquals(rrnCertificate, IdentityIntegrityTestService.getCertificate());
    }

    @Test
    public void testHandleMessageInvalidIntegritySignature() throws Exception {
        KeyPair keyPair = MiscTestUtils.generateKeyPair();
        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusYears(1);
        X509Certificate certificate = MiscTestUtils.generateCertificate(keyPair.getPublic(), "CN=TestNationalRegistration", notBefore, notAfter, null, keyPair.getPrivate(), true, 0, null, null);
        ServletConfig mockServletConfig = EasyMock.createMock(ServletConfig.class);
        Map<String, String> httpHeaders = new HashMap<String, String>();
        HttpSession mockHttpSession = EasyMock.createMock(HttpSession.class);
        HttpServletRequest mockServletRequest = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(mockServletConfig.getInitParameter("IdentityIntegrityService")).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter("IdentityIntegrityServiceClass")).andStubReturn(IdentityIntegrityTestService.class.getName());
        EasyMock.expect(mockServletConfig.getInitParameter("AuditService")).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter("AuditServiceClass")).andStubReturn(AuditTestService.class.getName());
        EasyMock.expect(mockServletConfig.getInitParameter("SkipNationalNumberCheck")).andStubReturn(null);
        EasyMock.expect(mockServletRequest.getRemoteAddr()).andStubReturn("remote-address");
        EasyMock.expect(mockHttpSession.getAttribute(RequestContext.INCLUDE_ADDRESS_SESSION_ATTRIBUTE)).andStubReturn(false);
        EasyMock.expect(mockHttpSession.getAttribute(RequestContext.INCLUDE_CERTIFICATES_SESSION_ATTRIBUTE)).andStubReturn(false);
        EasyMock.expect(mockHttpSession.getAttribute(RequestContext.INCLUDE_PHOTO_SESSION_ATTRIBUTE)).andStubReturn(false);
        EasyMock.expect(mockServletConfig.getInitParameter(IdentityDataMessageHandler.INCLUDE_DATA_FILES)).andReturn(null);
        byte[] idFile = "foobar-id-file".getBytes();
        IdentityDataMessage message = new IdentityDataMessage();
        message.idFile = idFile;
        KeyPair intruderKeyPair = MiscTestUtils.generateKeyPair();
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(intruderKeyPair.getPrivate());
        signature.update(idFile);
        byte[] idFileSignature = signature.sign();
        message.identitySignatureFile = idFileSignature;
        message.rrnCertFile = certificate.getEncoded();
        EasyMock.replay(mockServletConfig, mockHttpSession, mockServletRequest);
        AppletServiceServlet.injectInitParams(mockServletConfig, this.testedInstance);
        this.testedInstance.init(mockServletConfig);
        try {
            this.testedInstance.handleMessage(message, httpHeaders, mockServletRequest, mockHttpSession);
            fail();
        } catch (ServletException e) {
            LOG.debug("expected exception: " + e.getMessage());
            EasyMock.verify(mockServletConfig, mockHttpSession, mockServletRequest);
            assertNull(IdentityIntegrityTestService.getCertificate());
            assertEquals("remote-address", AuditTestService.getAuditIntegrityRemoteAddress());
        }
    }

    @Test
    public void testHandleMessageCorruptIntegritySignature() throws Exception {
        KeyPair keyPair = MiscTestUtils.generateKeyPair();
        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusYears(1);
        X509Certificate certificate = MiscTestUtils.generateCertificate(keyPair.getPublic(), "CN=TestNationalRegistration", notBefore, notAfter, null, keyPair.getPrivate(), true, 0, null, null);
        ServletConfig mockServletConfig = EasyMock.createMock(ServletConfig.class);
        Map<String, String> httpHeaders = new HashMap<String, String>();
        HttpSession mockHttpSession = EasyMock.createMock(HttpSession.class);
        HttpServletRequest mockServletRequest = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(mockServletConfig.getInitParameter("IdentityIntegrityService")).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter("IdentityIntegrityServiceClass")).andStubReturn(IdentityIntegrityTestService.class.getName());
        EasyMock.expect(mockServletConfig.getInitParameter("AuditService")).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter("AuditServiceClass")).andStubReturn(AuditTestService.class.getName());
        EasyMock.expect(mockServletConfig.getInitParameter("SkipNationalNumberCheck")).andStubReturn(null);
        EasyMock.expect(mockHttpSession.getAttribute(RequestContext.INCLUDE_ADDRESS_SESSION_ATTRIBUTE)).andStubReturn(false);
        EasyMock.expect(mockHttpSession.getAttribute(RequestContext.INCLUDE_CERTIFICATES_SESSION_ATTRIBUTE)).andStubReturn(false);
        EasyMock.expect(mockHttpSession.getAttribute(RequestContext.INCLUDE_PHOTO_SESSION_ATTRIBUTE)).andStubReturn(false);
        EasyMock.expect(mockServletConfig.getInitParameter(IdentityDataMessageHandler.INCLUDE_DATA_FILES)).andReturn(null);
        EasyMock.expect(mockServletRequest.getRemoteAddr()).andStubReturn("remote-address");
        byte[] idFile = "foobar-id-file".getBytes();
        IdentityDataMessage message = new IdentityDataMessage();
        message.idFile = idFile;
        message.identitySignatureFile = "foobar-signature".getBytes();
        message.rrnCertFile = certificate.getEncoded();
        EasyMock.replay(mockServletConfig, mockHttpSession, mockServletRequest);
        AppletServiceServlet.injectInitParams(mockServletConfig, this.testedInstance);
        this.testedInstance.init(mockServletConfig);
        try {
            this.testedInstance.handleMessage(message, httpHeaders, mockServletRequest, mockHttpSession);
            fail();
        } catch (ServletException e) {
            LOG.debug("expected exception: " + e.getMessage());
            EasyMock.verify(mockServletConfig, mockHttpSession, mockServletRequest);
            assertNull(IdentityIntegrityTestService.getCertificate());
            assertEquals("remote-address", AuditTestService.getAuditIntegrityRemoteAddress());
        }
    }

    public static class IdentityIntegrityTestService implements IdentityIntegrityService {

        private static X509Certificate certificate;

        public static void reset() {
            IdentityIntegrityTestService.certificate = null;
        }

        public static X509Certificate getCertificate() {
            return IdentityIntegrityTestService.certificate;
        }

        public void checkNationalRegistrationCertificate(List<X509Certificate> certificateChain) throws SecurityException {
            IdentityIntegrityTestService.certificate = certificateChain.get(0);
        }
    }
}

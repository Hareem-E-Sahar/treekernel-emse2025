public class Test {    @Test
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
}
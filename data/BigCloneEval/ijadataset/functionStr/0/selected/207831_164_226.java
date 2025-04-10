public class Test {    @Test
    public void testHandleMessageExpiredChallenge() throws Exception {
        KeyPair keyPair = MiscTestUtils.generateKeyPair();
        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusYears(1);
        String userId = "1234";
        X509Certificate certificate = MiscTestUtils.generateCertificate(keyPair.getPublic(), "CN=Test, SERIALNUMBER=" + userId, notBefore, notAfter, null, keyPair.getPrivate(), true, 0, null, null);
        byte[] salt = "salt".getBytes();
        byte[] sessionId = "session-id".getBytes();
        AuthenticationDataMessage message = new AuthenticationDataMessage();
        message.authnCert = certificate;
        message.saltValue = salt;
        message.sessionId = sessionId;
        Map<String, String> httpHeaders = new HashMap<String, String>();
        HttpSession testHttpSession = new HttpTestSession();
        HttpServletRequest mockServletRequest = EasyMock.createMock(HttpServletRequest.class);
        ServletConfig mockServletConfig = EasyMock.createMock(ServletConfig.class);
        byte[] challenge = AuthenticationChallenge.generateChallenge(testHttpSession);
        Thread.sleep(1000);
        AuthenticationContract authenticationContract = new AuthenticationContract(salt, null, null, sessionId, null, challenge);
        byte[] toBeSigned = authenticationContract.calculateToBeSigned();
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(toBeSigned);
        byte[] signatureValue = signature.sign();
        message.signatureValue = signatureValue;
        EasyMock.expect(mockServletConfig.getInitParameter(AuthenticationDataMessageHandler.CHALLENGE_MAX_MATURITY_INIT_PARAM_NAME)).andReturn("1");
        EasyMock.expect(mockServletConfig.getInitParameter(AuthenticationDataMessageHandler.AUTHN_SERVICE_INIT_PARAM_NAME)).andReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter(AuthenticationDataMessageHandler.AUTHN_SERVICE_INIT_PARAM_NAME + "Class")).andReturn(AuthenticationTestService.class.getName());
        EasyMock.expect(mockServletConfig.getInitParameter(HelloMessageHandler.HOSTNAME_INIT_PARAM_NAME)).andReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter(HelloMessageHandler.INET_ADDRESS_INIT_PARAM_NAME)).andReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter(HelloMessageHandler.CHANNEL_BINDING_SERVER_CERTIFICATE)).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter(HelloMessageHandler.SESSION_ID_CHANNEL_BINDING_INIT_PARAM_NAME)).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter(HelloMessageHandler.INCLUDE_IDENTITY_INIT_PARAM_NAME)).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter(HelloMessageHandler.INCLUDE_CERTS_INIT_PARAM_NAME)).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter(HelloMessageHandler.INCLUDE_ADDRESS_INIT_PARAM_NAME)).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter(HelloMessageHandler.INCLUDE_PHOTO_INIT_PARAM_NAME)).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter(HelloMessageHandler.IDENTITY_INTEGRITY_SERVICE_INIT_PARAM_NAME)).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter(HelloMessageHandler.IDENTITY_INTEGRITY_SERVICE_INIT_PARAM_NAME + "Class")).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter(AuthenticationDataMessageHandler.AUDIT_SERVICE_INIT_PARAM_NAME)).andReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter(AuthenticationDataMessageHandler.AUDIT_SERVICE_INIT_PARAM_NAME + "Class")).andReturn(AuditTestService.class.getName());
        EasyMock.expect(mockServletRequest.getRemoteAddr()).andStubReturn("remote-address");
        EasyMock.expect(mockServletRequest.getAttribute("javax.servlet.request.ssl_session")).andStubReturn(new String(Hex.encodeHex(sessionId)));
        EasyMock.expect(mockServletConfig.getInitParameter(AuthenticationDataMessageHandler.NRCID_SECRET_INIT_PARAM_NAME)).andStubReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter(HelloMessageHandler.CHANNEL_BINDING_SERVICE)).andReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter(HelloMessageHandler.CHANNEL_BINDING_SERVICE + "Class")).andReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter(AuthenticationDataMessageHandler.NRCID_ORG_ID_INIT_PARAM_NAME)).andReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter(AuthenticationDataMessageHandler.NRCID_APP_ID_INIT_PARAM_NAME)).andReturn(null);
        EasyMock.expect(mockServletConfig.getInitParameter(IdentityDataMessageHandler.INCLUDE_DATA_FILES)).andReturn(null);
        EasyMock.replay(mockServletRequest, mockServletConfig);
        AppletServiceServlet.injectInitParams(mockServletConfig, this.testedInstance);
        this.testedInstance.init(mockServletConfig);
        try {
            this.testedInstance.handleMessage(message, httpHeaders, mockServletRequest, testHttpSession);
            fail();
        } catch (ServletException e) {
            EasyMock.verify(mockServletRequest, mockServletConfig);
            assertNull(AuditTestService.getAuditUserId());
            assertNull(testHttpSession.getAttribute("eid.identifier"));
            assertEquals(certificate, AuditTestService.getAuditClientCertificate());
            assertEquals("remote-address", AuditTestService.getAuditRemoteAddress());
        }
    }
}
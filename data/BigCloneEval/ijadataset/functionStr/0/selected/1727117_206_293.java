public class Test {    @Test
    public void testSignEnvelopingDocumentOffice2010() throws Exception {
        EnvelopedSignatureFacet envelopedSignatureFacet = new EnvelopedSignatureFacet();
        KeyInfoSignatureFacet keyInfoSignatureFacet = new KeyInfoSignatureFacet(true, false, false);
        SignaturePolicyService signaturePolicyService = new ExplicitSignaturePolicyService("urn:test", "hello world".getBytes(), "description", "http://here.com");
        XAdESSignatureFacet xadesSignatureFacet = new XAdESSignatureFacet(signaturePolicyService);
        TimeStampService mockTimeStampService = EasyMock.createMock(TimeStampService.class);
        RevocationDataService mockRevocationDataService = EasyMock.createMock(RevocationDataService.class);
        XAdESXLSignatureFacet xadesXLSignatureFacet = new XAdESXLSignatureFacet(mockTimeStampService, mockRevocationDataService);
        XmlSignatureTestService testedInstance = new XmlSignatureTestService(envelopedSignatureFacet, keyInfoSignatureFacet, xadesSignatureFacet, new Office2010SignatureFacet(), xadesXLSignatureFacet);
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusYears(1);
        X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair.getPublic(), "CN=Test", notBefore, notAfter, null, keyPair.getPrivate(), true, 0, null, null, new KeyUsage(KeyUsage.nonRepudiation));
        List<X509Certificate> certificateChain = new LinkedList<X509Certificate>();
        certificateChain.add(certificate);
        certificateChain.add(certificate);
        RevocationData revocationData = new RevocationData();
        final X509CRL crl = PkiTestUtils.generateCrl(certificate, keyPair.getPrivate());
        revocationData.addCRL(crl);
        OCSPResp ocspResp = PkiTestUtils.createOcspResp(certificate, false, certificate, certificate, keyPair.getPrivate(), "SHA1withRSA");
        revocationData.addOCSP(ocspResp.getEncoded());
        EasyMock.expect(mockTimeStampService.timeStamp(EasyMock.anyObject(byte[].class), EasyMock.anyObject(RevocationData.class))).andStubAnswer(new IAnswer<byte[]>() {

            public byte[] answer() throws Throwable {
                Object[] arguments = EasyMock.getCurrentArguments();
                RevocationData revocationData = (RevocationData) arguments[1];
                revocationData.addCRL(crl);
                return "time-stamp-token".getBytes();
            }
        });
        EasyMock.expect(mockRevocationDataService.getRevocationData(EasyMock.eq(certificateChain))).andStubReturn(revocationData);
        EasyMock.replay(mockTimeStampService, mockRevocationDataService);
        DigestInfo digestInfo = testedInstance.preSign(null, certificateChain);
        assertNotNull(digestInfo);
        assertEquals("SHA-1", digestInfo.digestAlgo);
        assertNotNull(digestInfo.digestValue);
        TemporaryTestDataStorage temporaryDataStorage = (TemporaryTestDataStorage) testedInstance.getTemporaryDataStorage();
        assertNotNull(temporaryDataStorage);
        InputStream tempInputStream = temporaryDataStorage.getTempInputStream();
        assertNotNull(tempInputStream);
        Document tmpDocument = PkiTestUtils.loadDocument(tempInputStream);
        LOG.debug("tmp document: " + PkiTestUtils.toString(tmpDocument));
        Element nsElement = tmpDocument.createElement("ns");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", Constants.SignatureSpecNS);
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:xades", "http://uri.etsi.org/01903/v1.3.2#");
        Node digestValueNode = XPathAPI.selectSingleNode(tmpDocument, "//ds:DigestValue", nsElement);
        assertNotNull(digestValueNode);
        String digestValueTextContent = digestValueNode.getTextContent();
        LOG.debug("digest value text content: " + digestValueTextContent);
        assertFalse(digestValueTextContent.isEmpty());
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
        byte[] digestInfoValue = ArrayUtils.addAll(PkiTestUtils.SHA1_DIGEST_INFO_PREFIX, digestInfo.digestValue);
        byte[] signatureValue = cipher.doFinal(digestInfoValue);
        testedInstance.postSign(signatureValue, certificateChain);
        EasyMock.verify(mockTimeStampService, mockRevocationDataService);
        byte[] signedDocumentData = testedInstance.getSignedDocumentData();
        assertNotNull(signedDocumentData);
        Document signedDocument = PkiTestUtils.loadDocument(new ByteArrayInputStream(signedDocumentData));
        LOG.debug("signed document: " + PkiTestUtils.toString(signedDocument));
        NodeList signatureNodeList = signedDocument.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        assertEquals(1, signatureNodeList.getLength());
        Node signatureNode = signatureNodeList.item(0);
        DOMValidateContext domValidateContext = new DOMValidateContext(KeySelector.singletonKeySelector(keyPair.getPublic()), signatureNode);
        XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance();
        XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);
        boolean validity = xmlSignature.validate(domValidateContext);
        assertTrue(validity);
        File tmpFile = File.createTempFile("xades-bes-", ".xml");
        FileUtils.writeStringToFile(tmpFile, PkiTestUtils.toString(signedDocument));
        LOG.debug("tmp file: " + tmpFile.getAbsolutePath());
        Node resultNode = XPathAPI.selectSingleNode(signedDocument, "ds:Signature/ds:Object/xades:QualifyingProperties/xades:SignedProperties/xades:SignedSignatureProperties/xades:SigningCertificate/xades:Cert/xades:CertDigest/ds:DigestValue", nsElement);
        assertNotNull(resultNode);
        Node qualifyingPropertiesNode = XPathAPI.selectSingleNode(signedDocument, "ds:Signature/ds:Object/xades:QualifyingProperties", nsElement);
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        LSResourceResolver xadesResourceResolver = new XAdESLSResourceResolver();
        factory.setResourceResolver(xadesResourceResolver);
        InputStream schemaInputStream = XAdESSignatureFacetTest.class.getResourceAsStream("/XAdESv141.xsd");
        Source schemaSource = new StreamSource(schemaInputStream);
        Schema schema = factory.newSchema(schemaSource);
        Validator validator = schema.newValidator();
        validator.validate(new DOMSource(qualifyingPropertiesNode));
        StreamSource streamSource = new StreamSource(tmpFile.toURI().toString());
        ByteArrayOutputStream resultOutputStream = new ByteArrayOutputStream();
        StreamResult streamResult = new StreamResult(resultOutputStream);
        LOG.debug("result: " + resultOutputStream);
    }
}
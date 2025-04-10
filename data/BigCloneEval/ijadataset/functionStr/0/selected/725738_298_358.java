public class Test {    @Test
    public void testSignEnvelopingDocumentWithExternalDigestInfo() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element rootElement = document.createElementNS("urn:test", "tns:root");
        rootElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:tns", "urn:test");
        document.appendChild(rootElement);
        XmlSignatureTestService testedInstance = new XmlSignatureTestService();
        testedInstance.setEnvelopingDocument(document);
        testedInstance.setSignatureDescription("test-signature-description");
        byte[] refData = "hello world".getBytes();
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        messageDigest.update(refData);
        byte[] digestValue = messageDigest.digest();
        DigestInfo refDigestInfo = new DigestInfo(digestValue, "SHA-1", "urn:test:ref");
        DigestInfo digestInfo = testedInstance.preSign(Collections.singletonList(refDigestInfo), null);
        assertNotNull(digestInfo);
        LOG.debug("digest info description: " + digestInfo.description);
        assertEquals("test-signature-description", digestInfo.description);
        assertNotNull(digestInfo.digestValue);
        LOG.debug("digest algo: " + digestInfo.digestAlgo);
        assertEquals("SHA-1", digestInfo.digestAlgo);
        TemporaryTestDataStorage temporaryDataStorage = (TemporaryTestDataStorage) testedInstance.getTemporaryDataStorage();
        assertNotNull(temporaryDataStorage);
        InputStream tempInputStream = temporaryDataStorage.getTempInputStream();
        assertNotNull(tempInputStream);
        Document tmpDocument = PkiTestUtils.loadDocument(tempInputStream);
        LOG.debug("tmp document: " + PkiTestUtils.toString(tmpDocument));
        Element nsElement = tmpDocument.createElement("ns");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", Constants.SignatureSpecNS);
        Node digestValueNode = XPathAPI.selectSingleNode(tmpDocument, "//ds:DigestValue", nsElement);
        assertNotNull(digestValueNode);
        String digestValueTextContent = digestValueNode.getTextContent();
        LOG.debug("digest value text content: " + digestValueTextContent);
        assertFalse(digestValueTextContent.isEmpty());
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
        byte[] digestInfoValue = ArrayUtils.addAll(PkiTestUtils.SHA1_DIGEST_INFO_PREFIX, digestInfo.digestValue);
        byte[] signatureValue = cipher.doFinal(digestInfoValue);
        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusYears(1);
        X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair.getPublic(), "CN=Test", notBefore, notAfter, null, keyPair.getPrivate(), true, 0, null, null, new KeyUsage(KeyUsage.nonRepudiation));
        testedInstance.postSign(signatureValue, Collections.singletonList(certificate));
        byte[] signedDocumentData = testedInstance.getSignedDocumentData();
        assertNotNull(signedDocumentData);
        Document signedDocument = PkiTestUtils.loadDocument(new ByteArrayInputStream(signedDocumentData));
        LOG.debug("signed document: " + PkiTestUtils.toString(signedDocument));
        NodeList signatureNodeList = signedDocument.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        assertEquals(1, signatureNodeList.getLength());
        Node signatureNode = signatureNodeList.item(0);
        DOMValidateContext domValidateContext = new DOMValidateContext(KeySelector.singletonKeySelector(keyPair.getPublic()), signatureNode);
        URIDereferencer dereferencer = new URITest2Dereferencer();
        domValidateContext.setURIDereferencer(dereferencer);
        XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance();
        XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);
        boolean validity = xmlSignature.validate(domValidateContext);
        assertTrue(validity);
    }
}
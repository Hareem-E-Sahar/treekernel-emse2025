public class Test {    @Test
    public void testSign() throws Exception {
        URL ooxmlUrl = AbstractOOXMLSignatureServiceTest.class.getResource("/ms-office-2010.docx");
        OOXMLTestSignatureService signatureService = new OOXMLTestSignatureService(ooxmlUrl);
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusYears(1);
        X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair.getPublic(), "CN=Test", notBefore, notAfter, null, keyPair.getPrivate(), true, 0, null, null, new KeyUsage(KeyUsage.digitalSignature));
        List<X509Certificate> certChain = Collections.singletonList(certificate);
        DigestInfo digestInfo = signatureService.preSign(null, certChain);
        assertNotNull(digestInfo);
        LOG.debug("digest algo: " + digestInfo.digestAlgo);
        LOG.debug("digest description: " + digestInfo.description);
        assertEquals("Office OpenXML Document", digestInfo.description);
        assertNotNull(digestInfo.digestAlgo);
        assertNotNull(digestInfo.digestValue);
        TemporaryDataStorage temporaryDataStorage = signatureService.getTemporaryDataStorage();
        String preSignResult = IOUtils.toString(temporaryDataStorage.getTempInputStream());
        LOG.debug("pre-sign result: " + preSignResult);
        File tmpFile = File.createTempFile("ooxml-pre-sign-", ".xml");
        FileUtils.writeStringToFile(tmpFile, preSignResult);
        LOG.debug("tmp pre-sign file: " + tmpFile.getAbsolutePath());
        LOG.debug("digest algo: " + digestInfo.digestAlgo);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
        byte[] digestInfoValue = ArrayUtils.addAll(PkiTestUtils.SHA1_DIGEST_INFO_PREFIX, digestInfo.digestValue);
        byte[] signatureValue = cipher.doFinal(digestInfoValue);
        signatureService.postSign(signatureValue, certChain);
        byte[] signedOOXMLData = signatureService.getSignedOfficeOpenXMLDocumentData();
        assertNotNull(signedOOXMLData);
        LOG.debug("signed OOXML size: " + signedOOXMLData.length);
        String extension = FilenameUtils.getExtension(ooxmlUrl.getFile());
        tmpFile = File.createTempFile("ooxml-signed-", "." + extension);
        FileUtils.writeByteArrayToFile(tmpFile, signedOOXMLData);
        LOG.debug("signed OOXML file: " + tmpFile.getAbsolutePath());
        OOXMLSignatureVerifier verifier = new OOXMLSignatureVerifier();
        List<X509Certificate> signers = verifier.getSigners(tmpFile.toURI().toURL());
        assertEquals(1, signers.size());
        LOG.debug("signed OOXML file: " + tmpFile.getAbsolutePath());
    }
}
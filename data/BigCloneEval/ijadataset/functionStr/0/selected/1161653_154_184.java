public class Test {    private void sign(String resourceName, int signatureCount) throws Exception {
        LOG.debug("test sign: " + resourceName);
        URL odfUrl = AbstractODFSignatureServiceTest.class.getResource(resourceName);
        assertNotNull(odfUrl);
        ODFTestSignatureService odfSignatureService = new ODFTestSignatureService();
        odfSignatureService.setOdfUrl(odfUrl);
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusYears(1);
        X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair.getPublic(), "CN=Test", notBefore, notAfter, null, keyPair.getPrivate(), true, 0, null, null, new KeyUsage(KeyUsage.nonRepudiation));
        DigestInfo digestInfo = odfSignatureService.preSign(null, Collections.singletonList(certificate));
        assertNotNull(digestInfo);
        LOG.debug("signature description: " + digestInfo.description);
        LOG.debug("signature hash algo: " + digestInfo.digestAlgo);
        assertEquals("ODF Document", digestInfo.description);
        assertEquals("SHA-1", digestInfo.digestAlgo);
        assertNotNull(digestInfo.digestValue);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
        byte[] digestInfoValue = ArrayUtils.addAll(PkiTestUtils.SHA1_DIGEST_INFO_PREFIX, digestInfo.digestValue);
        byte[] signatureValue = cipher.doFinal(digestInfoValue);
        odfSignatureService.postSign(signatureValue, Collections.singletonList(certificate));
        byte[] signedODFData = odfSignatureService.getSignedODFData();
        assertNotNull(signedODFData);
        LOG.debug("signed ODF size: " + signedODFData.length);
        File tmpFile = File.createTempFile("signed-", ".odt");
        FileUtils.writeByteArrayToFile(tmpFile, signedODFData);
        LOG.debug("signed ODF file: " + tmpFile.getAbsolutePath());
        assertTrue(hasOdfSignature(tmpFile.toURI().toURL(), signatureCount));
        LOG.debug("signed ODF file: " + tmpFile.getAbsolutePath());
    }
}
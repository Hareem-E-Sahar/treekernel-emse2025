package test.unit.be.fedict.eid.applet.service.signer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import javax.crypto.Cipher;
import be.fedict.eid.applet.service.signer.DigestAlgo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;
import be.fedict.eid.applet.service.signer.TemporaryDataStorage;
import be.fedict.eid.applet.service.signer.ooxml.AbstractOOXMLSignatureService;
import be.fedict.eid.applet.service.signer.ooxml.OOXMLProvider;
import be.fedict.eid.applet.service.signer.ooxml.OOXMLSignatureVerifier;
import be.fedict.eid.applet.service.spi.DigestInfo;

public class AbstractOOXMLSignatureServiceTest {

    private static final Log LOG = LogFactory.getLog(AbstractOOXMLSignatureServiceTest.class);

    @BeforeClass
    public static void setUp() {
        OOXMLProvider.install();
    }

    private static class OOXMLTestSignatureService extends AbstractOOXMLSignatureService {

        private final URL ooxmlUrl;

        private final TemporaryTestDataStorage temporaryDataStorage;

        private final ByteArrayOutputStream signedOOXMLOutputStream;

        public OOXMLTestSignatureService(URL ooxmlUrl) {
            super(DigestAlgo.SHA1);
            this.temporaryDataStorage = new TemporaryTestDataStorage();
            this.signedOOXMLOutputStream = new ByteArrayOutputStream();
            this.ooxmlUrl = ooxmlUrl;
        }

        @Override
        protected URL getOfficeOpenXMLDocumentURL() {
            return this.ooxmlUrl;
        }

        @Override
        protected OutputStream getSignedOfficeOpenXMLDocumentOutputStream() {
            return this.signedOOXMLOutputStream;
        }

        public byte[] getSignedOfficeOpenXMLDocumentData() {
            return this.signedOOXMLOutputStream.toByteArray();
        }

        @Override
        protected TemporaryDataStorage getTemporaryDataStorage() {
            return this.temporaryDataStorage;
        }
    }

    @Test
    public void testPreSign() throws Exception {
        URL ooxmlUrl = AbstractOOXMLSignatureServiceTest.class.getResource("/hello-world-unsigned.docx");
        assertNotNull(ooxmlUrl);
        OOXMLTestSignatureService signatureService = new OOXMLTestSignatureService(ooxmlUrl);
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusYears(1);
        X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair.getPublic(), "CN=Test", notBefore, notAfter, null, keyPair.getPrivate(), true, 0, null, null, new KeyUsage(KeyUsage.nonRepudiation));
        DigestInfo digestInfo = signatureService.preSign(null, Collections.singletonList(certificate));
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
    }

    @Test
    public void testPostSign() throws Exception {
        sign("/hello-world-unsigned.docx");
    }

    @Test
    public void testSignOffice2010TechnicalPreview() throws Exception {
        sign("/hello-world-office-2010-technical-preview-unsigned.docx");
    }

    @Test
    public void testSignOffice2010() throws Exception {
        sign("/ms-office-2010.docx");
    }

    @Test
    public void testSignTwice() throws Exception {
        sign("/hello-world-signed.docx", 2);
    }

    @Test
    public void testSignTwiceHere() throws Exception {
        File tmpFile = sign("/hello-world-unsigned.docx", 1);
        sign(tmpFile.toURI().toURL(), "CN=Test2", 2);
    }

    @Test
    public void testSignPowerpoint() throws Exception {
        sign("/hello-world-unsigned.pptx");
    }

    @Test
    public void testSignSpreadsheet() throws Exception {
        sign("/hello-world-unsigned.xlsx");
    }

    private void sign(String documentResourceName) throws Exception {
        sign(documentResourceName, 1);
    }

    private File sign(String documentResourceName, int signerCount) throws Exception {
        URL ooxmlUrl = AbstractOOXMLSignatureServiceTest.class.getResource(documentResourceName);
        return sign(ooxmlUrl, signerCount);
    }

    private File sign(URL ooxmlUrl, int signerCount) throws Exception {
        return sign(ooxmlUrl, "CN=Test", signerCount);
    }

    private File sign(URL ooxmlUrl, String signerDn, int signerCount) throws Exception {
        assertNotNull(ooxmlUrl);
        OOXMLTestSignatureService signatureService = new OOXMLTestSignatureService(ooxmlUrl);
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusYears(1);
        X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair.getPublic(), signerDn, notBefore, notAfter, null, keyPair.getPrivate(), true, 0, null, null, new KeyUsage(KeyUsage.digitalSignature));
        DigestInfo digestInfo = signatureService.preSign(null, Collections.singletonList(certificate));
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
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
        byte[] digestInfoValue = ArrayUtils.addAll(PkiTestUtils.SHA1_DIGEST_INFO_PREFIX, digestInfo.digestValue);
        byte[] signatureValue = cipher.doFinal(digestInfoValue);
        signatureService.postSign(signatureValue, Collections.singletonList(certificate));
        byte[] signedOOXMLData = signatureService.getSignedOfficeOpenXMLDocumentData();
        assertNotNull(signedOOXMLData);
        LOG.debug("signed OOXML size: " + signedOOXMLData.length);
        String extension = FilenameUtils.getExtension(ooxmlUrl.getFile());
        tmpFile = File.createTempFile("ooxml-signed-", "." + extension);
        FileUtils.writeByteArrayToFile(tmpFile, signedOOXMLData);
        LOG.debug("signed OOXML file: " + tmpFile.getAbsolutePath());
        OOXMLSignatureVerifier verifier = new OOXMLSignatureVerifier();
        List<X509Certificate> signers = verifier.getSigners(tmpFile.toURI().toURL());
        assertEquals(signerCount, signers.size());
        LOG.debug("signed OOXML file: " + tmpFile.getAbsolutePath());
        return tmpFile;
    }
}

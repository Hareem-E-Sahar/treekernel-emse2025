package test.be.fedict.eid.applet;

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
import java.util.Locale;
import javax.crypto.Cipher;
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
import test.unit.be.fedict.eid.applet.service.signer.AbstractOOXMLSignatureServiceTest;
import be.fedict.eid.applet.Messages;
import be.fedict.eid.applet.sc.PcscEid;
import be.fedict.eid.applet.service.signer.DigestAlgo;
import be.fedict.eid.applet.service.signer.TemporaryDataStorage;
import be.fedict.eid.applet.service.signer.ooxml.AbstractOOXMLSignatureService;
import be.fedict.eid.applet.service.signer.ooxml.OOXMLProvider;
import be.fedict.eid.applet.service.signer.ooxml.OOXMLSignatureVerifier;
import be.fedict.eid.applet.service.spi.DigestInfo;

public class OOXMLTest {

    private static final Log LOG = LogFactory.getLog(OOXMLTest.class);

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

    private File sign(URL ooxmlUrl, int signerCount) throws Exception {
        assertNotNull(ooxmlUrl);
        OOXMLTestSignatureService signatureService = new OOXMLTestSignatureService(ooxmlUrl);
        Messages messages = new Messages(Locale.ENGLISH);
        PcscEid pcscEid = new PcscEid(new TestView(), messages);
        if (false == pcscEid.isEidPresent()) {
            LOG.debug("insert eID card");
            pcscEid.waitForEidPresent();
        }
        List<X509Certificate> certChain = pcscEid.getSignCertificateChain();
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
        byte[] signatureValue = pcscEid.sign(digestInfo.digestValue, digestInfo.digestAlgo);
        pcscEid.close();
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
        assertEquals(signerCount, signers.size());
        LOG.debug("signed OOXML file: " + tmpFile.getAbsolutePath());
        return tmpFile;
    }

    @Test
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

    @Test
    public void testSignOffice2010ProductioneID() throws Exception {
        sign("/ms-office-2010.docx");
    }

    private void sign(String documentResourceName) throws Exception {
        sign(documentResourceName, 1);
    }

    private File sign(String documentResourceName, int signerCount) throws Exception {
        URL ooxmlUrl = AbstractOOXMLSignatureServiceTest.class.getResource(documentResourceName);
        return sign(ooxmlUrl, signerCount);
    }
}

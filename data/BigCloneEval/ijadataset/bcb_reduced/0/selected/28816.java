package be.fedict.eid.dss.document.ooxml;

import be.fedict.eid.applet.service.signer.DigestAlgo;
import be.fedict.eid.applet.service.signer.HttpSessionTemporaryDataStorage;
import be.fedict.eid.applet.service.signer.SignatureFacet;
import be.fedict.eid.applet.service.signer.TemporaryDataStorage;
import be.fedict.eid.applet.service.signer.facets.IdentitySignatureFacet;
import be.fedict.eid.applet.service.signer.facets.RevocationDataService;
import be.fedict.eid.applet.service.signer.facets.XAdESSignatureFacet;
import be.fedict.eid.applet.service.signer.facets.XAdESXLSignatureFacet;
import be.fedict.eid.applet.service.signer.ooxml.AbstractOOXMLSignatureService;
import be.fedict.eid.applet.service.signer.time.TimeStampService;
import be.fedict.eid.applet.service.spi.AddressDTO;
import be.fedict.eid.applet.service.spi.DigestInfo;
import be.fedict.eid.applet.service.spi.IdentityDTO;
import be.fedict.eid.applet.service.spi.SignatureServiceEx;
import be.fedict.eid.dss.spi.utils.CloseActionOutputStream;
import org.apache.commons.io.IOUtils;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;

public class OOXMLSignatureService extends AbstractOOXMLSignatureService implements SignatureServiceEx {

    private final TemporaryDataStorage temporaryDataStorage;

    private final OutputStream documentOutputStream;

    private final File tmpFile;

    public OOXMLSignatureService(InputStream documentInputStream, OutputStream documentOutputStream, SignatureFacet signatureFacet, String role, IdentityDTO identity, byte[] photo, RevocationDataService revocationDataService, TimeStampService timeStampService, DigestAlgo signatureDigestAlgo) throws IOException {
        super(signatureDigestAlgo);
        this.temporaryDataStorage = new HttpSessionTemporaryDataStorage();
        this.documentOutputStream = documentOutputStream;
        this.tmpFile = File.createTempFile("eid-dss-", ".ooxml");
        FileOutputStream fileOutputStream;
        fileOutputStream = new FileOutputStream(this.tmpFile);
        IOUtils.copy(documentInputStream, fileOutputStream);
        addSignatureFacet(signatureFacet);
        addSignatureFacet(new XAdESXLSignatureFacet(timeStampService, revocationDataService, getSignatureDigestAlgorithm()));
        XAdESSignatureFacet xadesSignatureFacet = super.getXAdESSignatureFacet();
        xadesSignatureFacet.setRole(role);
        if (null != identity) {
            IdentitySignatureFacet identitySignatureFacet = new IdentitySignatureFacet(identity, photo, getSignatureDigestAlgorithm());
            addSignatureFacet(identitySignatureFacet);
        }
    }

    @Override
    protected URL getOfficeOpenXMLDocumentURL() {
        try {
            return this.tmpFile.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL error: " + e.getMessage(), e);
        }
    }

    @Override
    protected OutputStream getSignedOfficeOpenXMLDocumentOutputStream() {
        return new CloseActionOutputStream(this.documentOutputStream, new CloseAction());
    }

    private class CloseAction implements Runnable {

        public void run() {
            OOXMLSignatureService.this.tmpFile.delete();
        }
    }

    @Override
    protected TemporaryDataStorage getTemporaryDataStorage() {
        return this.temporaryDataStorage;
    }

    public DigestInfo preSign(List<DigestInfo> digestInfos, List<X509Certificate> signingCertificateChain, IdentityDTO identity, AddressDTO address, byte[] photo) throws NoSuchAlgorithmException {
        return super.preSign(digestInfos, signingCertificateChain);
    }
}

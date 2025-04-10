package be.fedict.eid.applet.service.signer.odf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import be.fedict.eid.applet.service.signer.KeyInfoKeySelector;

/**
 * ODF Signature Verifier.
 * 
 * @author fcorneli
 */
public class ODFSignatureVerifier {

    private static final Log LOG = LogFactory.getLog(ODFSignatureVerifier.class);

    private ODFSignatureVerifier() {
        super();
    }

    /**
     * Checks whether the ODF document available via the given URL has been
     * signed.
     *
     * @param odfUrl
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws org.apache.xml.security.signature.XMLSignatureException
     * @throws XMLSecurityException
     * @throws MarshalException
     * @throws XMLSignatureException
     */
    public static boolean hasOdfSignature(URL odfUrl) throws IOException, ParserConfigurationException, SAXException, org.apache.xml.security.signature.XMLSignatureException, XMLSecurityException, MarshalException, XMLSignatureException {
        List<X509Certificate> signers = getSigners(odfUrl);
        return false == signers.isEmpty();
    }

    /**
     * return list of signers for the document available via the given
     * URL.
     *
     * @param odfUrl
     * @return list of X509 certificates
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws MarshalException
     * @throws XMLSignatureException
     */
    public static List<X509Certificate> getSigners(URL odfUrl) throws IOException, ParserConfigurationException, SAXException, MarshalException, XMLSignatureException {
        List<X509Certificate> signers = new LinkedList<X509Certificate>();
        if (null == odfUrl) {
            throw new IllegalArgumentException("odfUrl is null");
        }
        ZipInputStream odfZipInputStream = new ZipInputStream(odfUrl.openStream());
        ZipEntry zipEntry;
        while (null != (zipEntry = odfZipInputStream.getNextEntry())) {
            if (ODFUtil.isSignatureFile(zipEntry)) {
                Document documentSignatures = ODFUtil.loadDocument(odfZipInputStream);
                NodeList signatureNodeList = documentSignatures.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
                for (int idx = 0; idx < signatureNodeList.getLength(); idx++) {
                    Node signatureNode = signatureNodeList.item(idx);
                    X509Certificate signer = getVerifiedSignatureSigner(odfUrl, signatureNode);
                    if (null == signer) {
                        LOG.debug("JSR105 says invalid signature");
                    } else {
                        signers.add(signer);
                    }
                }
                return signers;
            }
        }
        LOG.debug("no signature file present");
        return signers;
    }

    private static X509Certificate getVerifiedSignatureSigner(URL odfUrl, Node signatureNode) throws MarshalException, XMLSignatureException {
        if (null == odfUrl) {
            throw new IllegalArgumentException("odfUrl is null");
        }
        KeyInfoKeySelector keySelector = new KeyInfoKeySelector();
        DOMValidateContext domValidateContext = new DOMValidateContext(keySelector, signatureNode);
        ODFURIDereferencer dereferencer = new ODFURIDereferencer(odfUrl);
        domValidateContext.setURIDereferencer(dereferencer);
        XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance();
        LOG.debug("java version: " + System.getProperty("java.version"));
        XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);
        boolean validity = xmlSignature.validate(domValidateContext);
        if (false == validity) {
            LOG.debug("invalid signature");
            return null;
        }
        X509Certificate signer = keySelector.getCertificate();
        if (null == signer) {
            throw new IllegalStateException("signer X509 certificate is null");
        }
        LOG.debug("signer: " + signer.getSubjectX500Principal());
        return signer;
    }

    /**
     * Checks whether the document available on the given URL is an ODF document
     * or not.
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static boolean isODF(URL url) throws IOException {
        InputStream resStream = ODFUtil.findDataInputStream(url.openStream(), ODFUtil.MIMETYPE_FILE);
        if (null == resStream) {
            LOG.debug("mimetype stream not found in ODF package");
            return false;
        }
        String mimetypeContent = IOUtils.toString(resStream);
        return mimetypeContent.startsWith(ODFUtil.MIMETYPE_START);
    }
}

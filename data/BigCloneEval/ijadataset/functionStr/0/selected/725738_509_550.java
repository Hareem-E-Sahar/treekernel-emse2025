public class Test {    @Test
    public void testJsr105SignatureExternalXMLWithDTD() throws Exception {
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element rootElement = document.createElementNS("urn:test", "tns:root");
        rootElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:tns", "urn:test");
        document.appendChild(rootElement);
        Element dataElement = document.createElementNS("urn:test", "tns:data");
        dataElement.setAttributeNS(null, "Id", "id-1234");
        dataElement.setTextContent("data to be signed");
        rootElement.appendChild(dataElement);
        XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM", new org.jcp.xml.dsig.internal.dom.XMLDSigRI());
        XMLSignContext signContext = new DOMSignContext(keyPair.getPrivate(), document.getDocumentElement());
        signContext.setURIDereferencer(new MyURIDereferencer());
        signContext.putNamespacePrefix(javax.xml.crypto.dsig.XMLSignature.XMLNS, "ds");
        DigestMethod digestMethod = signatureFactory.newDigestMethod(DigestMethod.SHA1, null);
        List<Transform> transforms = new LinkedList<Transform>();
        Transform transform = signatureFactory.newTransform(CanonicalizationMethod.INCLUSIVE, (TransformParameterSpec) null);
        LOG.debug("transform type: " + transform.getClass().getName());
        transforms.add(transform);
        Reference reference = signatureFactory.newReference("/bookstore.xml", digestMethod, transforms, null, null);
        DOMReference domReference = (DOMReference) reference;
        assertNull(domReference.getCalculatedDigestValue());
        assertNull(domReference.getDigestValue());
        SignatureMethod signatureMethod = signatureFactory.newSignatureMethod(SignatureMethod.RSA_SHA1, null);
        CanonicalizationMethod canonicalizationMethod = signatureFactory.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS, (C14NMethodParameterSpec) null);
        SignedInfo signedInfo = signatureFactory.newSignedInfo(canonicalizationMethod, signatureMethod, Collections.singletonList(reference));
        javax.xml.crypto.dsig.XMLSignature xmlSignature = signatureFactory.newXMLSignature(signedInfo, null);
        DOMXMLSignature domXmlSignature = (DOMXMLSignature) xmlSignature;
        domXmlSignature.marshal(document.getDocumentElement(), "ds", (DOMCryptoContext) signContext);
        domReference.digest(signContext);
        Element nsElement = document.createElement("ns");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", Constants.SignatureSpecNS);
        Node digestValueNode = XPathAPI.selectSingleNode(document, "//ds:DigestValue", nsElement);
        assertNotNull(digestValueNode);
        String digestValueTextContent = digestValueNode.getTextContent();
        LOG.debug("digest value text content: " + digestValueTextContent);
        assertFalse(digestValueTextContent.isEmpty());
    }
}
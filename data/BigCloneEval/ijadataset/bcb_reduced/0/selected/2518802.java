package net.sf.istcontract.wsimport.message.source;

import net.sf.istcontract.wsimport.message.RootElementSniffer;
import net.sf.istcontract.wsimport.streaming.SourceReaderFactory;
import net.sf.istcontract.wsimport.util.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Vivek Pandey
 */
final class SourceUtils {

    int srcType;

    private final int domSource = 1;

    private final int streamSource = 2;

    private final int saxSource = 4;

    public SourceUtils(Source src) {
        if (src instanceof StreamSource) {
            srcType = streamSource;
        } else if (src instanceof DOMSource) {
            srcType = domSource;
        } else if (src instanceof SAXSource) {
            srcType = saxSource;
        }
    }

    public boolean isDOMSource() {
        return (srcType & domSource) == domSource;
    }

    public boolean isStreamSource() {
        return (srcType & streamSource) == streamSource;
    }

    public boolean isSaxSource() {
        return (srcType & saxSource) == saxSource;
    }

    /**
     * This would peek into the Source (DOMSource and SAXSource) for the localName and NamespaceURI
     * of the top-level element.
     * @param src
     * @return QName of the payload
     */
    public QName sniff(Source src) {
        return sniff(src, new RootElementSniffer());
    }

    public QName sniff(Source src, RootElementSniffer sniffer) {
        String localName = null;
        String namespaceUri = null;
        if (isDOMSource()) {
            DOMSource domSource = (DOMSource) src;
            Node n = domSource.getNode();
            if (n.getNodeType() == Node.DOCUMENT_NODE) {
                n = ((Document) n).getDocumentElement();
            }
            localName = n.getLocalName();
            namespaceUri = n.getNamespaceURI();
        } else if (isSaxSource()) {
            SAXSource saxSrc = (SAXSource) src;
            SAXResult saxResult = new SAXResult(sniffer);
            try {
                Transformer tr = XmlUtil.newTransformer();
                tr.transform(saxSrc, saxResult);
            } catch (TransformerConfigurationException e) {
                throw new WebServiceException(e);
            } catch (TransformerException e) {
                localName = sniffer.getLocalName();
                namespaceUri = sniffer.getNsUri();
            }
        }
        return new QName(namespaceUri, localName);
    }

    public static void serializeSource(Source src, XMLStreamWriter writer) throws XMLStreamException {
        XMLStreamReader reader = SourceReaderFactory.createSourceReader(src, true);
        int state;
        do {
            state = reader.next();
            switch(state) {
                case XMLStreamConstants.START_ELEMENT:
                    String uri = reader.getNamespaceURI();
                    String prefix = reader.getPrefix();
                    String localName = reader.getLocalName();
                    if (prefix == null) {
                        if (uri == null) {
                            writer.writeStartElement(localName);
                        } else {
                            writer.writeStartElement(uri, localName);
                        }
                    } else {
                        assert uri != null;
                        if (prefix.length() > 0) {
                            String writerURI = null;
                            if (writer.getNamespaceContext() != null) writerURI = writer.getNamespaceContext().getNamespaceURI(prefix);
                            String writerPrefix = writer.getPrefix(uri);
                            if (declarePrefix(prefix, uri, writerPrefix, writerURI)) {
                                writer.writeStartElement(prefix, localName, uri);
                                writer.setPrefix(prefix, uri != null ? uri : "");
                                writer.writeNamespace(prefix, uri);
                            } else {
                                writer.writeStartElement(prefix, localName, uri);
                            }
                        } else {
                            writer.writeStartElement(prefix, localName, uri);
                        }
                    }
                    int n = reader.getNamespaceCount();
                    for (int i = 0; i < n; i++) {
                        String nsPrefix = reader.getNamespacePrefix(i);
                        if (nsPrefix == null) nsPrefix = "";
                        String writerURI = null;
                        if (writer.getNamespaceContext() != null) writerURI = writer.getNamespaceContext().getNamespaceURI(nsPrefix);
                        String readerURI = reader.getNamespaceURI(i);
                        if (writerURI == null || ((nsPrefix.length() == 0) || (prefix.length() == 0)) || (!nsPrefix.equals(prefix) && !writerURI.equals(readerURI))) {
                            writer.setPrefix(nsPrefix, readerURI != null ? readerURI : "");
                            writer.writeNamespace(nsPrefix, readerURI != null ? readerURI : "");
                        }
                    }
                    n = reader.getAttributeCount();
                    for (int i = 0; i < n; i++) {
                        String attrPrefix = reader.getAttributePrefix(i);
                        String attrURI = reader.getAttributeNamespace(i);
                        writer.writeAttribute(attrPrefix != null ? attrPrefix : "", attrURI != null ? attrURI : "", reader.getAttributeLocalName(i), reader.getAttributeValue(i));
                        setUndeclaredPrefix(attrPrefix, attrURI, writer);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    writer.writeEndElement();
                    break;
                case XMLStreamConstants.CHARACTERS:
                    writer.writeCharacters(reader.getText());
            }
        } while (state != XMLStreamConstants.END_DOCUMENT);
        reader.close();
    }

    /**
     * sets undeclared prefixes on the writer
     * @param prefix
     * @param writer
     * @throws XMLStreamException
     */
    private static void setUndeclaredPrefix(String prefix, String readerURI, XMLStreamWriter writer) throws XMLStreamException {
        String writerURI = null;
        if (writer.getNamespaceContext() != null) writerURI = writer.getNamespaceContext().getNamespaceURI(prefix);
        if (writerURI == null) {
            writer.setPrefix(prefix, readerURI != null ? readerURI : "");
            writer.writeNamespace(prefix, readerURI != null ? readerURI : "");
        }
    }

    /**
     * check if we need to declare
     * @param rPrefix
     * @param rUri
     * @param wPrefix
     * @param wUri
     */
    private static boolean declarePrefix(String rPrefix, String rUri, String wPrefix, String wUri) {
        if (wUri == null || ((wPrefix != null) && !rPrefix.equals(wPrefix)) || (rUri != null && !wUri.equals(rUri))) return true;
        return false;
    }
}

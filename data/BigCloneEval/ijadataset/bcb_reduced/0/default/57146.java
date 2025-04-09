import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class XMPPDOMParser {

    public static String getRootTagName(String XMLexcerpt) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document d = factory.newDocumentBuilder().parse(new InputSource(new StringReader(XMLexcerpt)));
            Node n = d.getFirstChild();
            return n.getNodeName();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getMessageBodyFromMessageStanza(String stanza) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document d = factory.newDocumentBuilder().parse(new InputSource(new StringReader(stanza)));
            Node n = d.getElementsByTagName("body").item(0);
            return n.getTextContent();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getToAttributeFromMessageStanza(String stanza) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document d = factory.newDocumentBuilder().parse(new InputSource(new StringReader(stanza)));
            return d.getAttributes().getNamedItem("to").toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getFromAttributeFromMessageStanza(String stanza) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document d = factory.newDocumentBuilder().parse(new InputSource(new StringReader(stanza)));
            return d.getAttributes().getNamedItem("from").toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getUrlFromFileTransferStanza(String stanza) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document d = factory.newDocumentBuilder().parse(new InputSource(new StringReader(stanza)));
            return d.getElementsByTagName("url").item(0).getTextContent();
        } catch (Exception e) {
            return null;
        }
    }
}

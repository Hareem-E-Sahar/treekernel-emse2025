import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.Text;
import javax.xml.soap.Name;
import javax.xml.namespace.QName;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;

public class DOMExample {

    static Document document;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Argument required: " + "-Dxml-file=<filename>");
            System.exit(1);
        }
        DOMExample de = new DOMExample();
        document = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new File(args[0]));
        } catch (SAXParseException spe) {
            System.out.println("\n** Parsing error" + ", line " + spe.getLineNumber() + ", uri " + spe.getSystemId());
            System.out.println("   " + spe.getMessage());
            Exception x = spe;
            if (spe.getException() != null) {
                x = spe.getException();
            }
            x.printStackTrace();
        } catch (SAXException sxe) {
            Exception x = sxe;
            if (sxe.getException() != null) {
                x = sxe.getException();
            }
            x.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        try {
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage message = messageFactory.createMessage();
            SOAPHeader header = message.getSOAPHeader();
            header.detachNode();
            SOAPBody body = message.getSOAPBody();
            SOAPBodyElement docElement = body.addDocument(document);
            message.saveChanges();
            Iterator iter1 = body.getChildElements();
            de.getContents(iter1, "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getContents(Iterator iterator, String indent) {
        while (iterator.hasNext()) {
            Node node = (Node) iterator.next();
            SOAPElement element = null;
            Text text = null;
            if (node instanceof SOAPElement) {
                element = (SOAPElement) node;
                QName name = element.getElementQName();
                System.out.println(indent + "Name is " + name.toString());
                Iterator attrs = element.getAllAttributesAsQNames();
                while (attrs.hasNext()) {
                    QName attrName = (QName) attrs.next();
                    System.out.println(indent + " Attribute name is " + attrName.toString());
                    System.out.println(indent + " Attribute value is " + element.getAttributeValue(attrName));
                }
                Iterator iter2 = element.getChildElements();
                getContents(iter2, indent + " ");
            } else {
                text = (Text) node;
                String content = text.getValue();
                System.out.println(indent + "Content is: " + content);
            }
        }
    }
}

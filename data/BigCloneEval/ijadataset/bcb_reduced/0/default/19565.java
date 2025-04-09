import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.jaxen.JaxenException;
import org.jaxen.dom.DocumentNavigator;
import org.jaxen.dom.DocumentUpdater;
import org.jaxen.dom.XPath;
import org.jaxen.xupdate.XUpdate;
import org.saxpath.SAXPathException;
import org.saxpath.XPathSyntaxException;
import java.util.List;
import java.util.Iterator;

public class DOMUpdateDemo {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("usage: DOMUpdaterDemo <document url> " + "<update document url>");
            System.exit(1);
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            OutputFormat o = new OutputFormat("xml", "ISO-8859-1", true);
            XMLSerializer serial;
            Document doc = builder.parse(args[0]);
            serial = new XMLSerializer(System.out, o);
            System.out.println("Document: ");
            serial.serialize(doc);
            System.out.println();
            Document updateDoc = builder.parse(args[1]);
            serial = new XMLSerializer(System.out, o);
            System.out.println("Update doc: ");
            serial.serialize(updateDoc);
            System.out.println();
            XUpdate updater = new XUpdate(new DocumentUpdater(), DocumentNavigator.getInstance());
            updater.executeModifications(doc, updateDoc.getDocumentElement());
            System.out.println("Document   :: " + args[0]);
            System.out.println("Update doc :: " + args[1]);
            System.out.println();
            System.out.println("Updated document");
            System.out.println("----------------------------------");
            serial = new XMLSerializer(System.out, o);
            serial.serialize(doc);
            System.out.println();
        } catch (SAXPathException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

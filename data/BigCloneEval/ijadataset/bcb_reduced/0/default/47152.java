import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
   * Show how to transform a DOM tree into another DOM tree.  
   * This uses the javax.xml.parsers to parse both an XSL file 
   * and the XML file into a DOM, and create an output DOM.
   */
public class DOM2DOM {

    public static void main(String[] args) throws TransformerException, TransformerConfigurationException, FileNotFoundException, ParserConfigurationException, SAXException, IOException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        if (tFactory.getFeature(DOMSource.FEATURE) && tFactory.getFeature(DOMResult.FEATURE)) {
            DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
            dFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dFactory.newDocumentBuilder();
            Document xslDoc = dBuilder.parse("birds.xsl");
            DOMSource xslDomSource = new DOMSource(xslDoc);
            xslDomSource.setSystemId("birds.xsl");
            Transformer transformer = tFactory.newTransformer(xslDomSource);
            Document xmlDoc = dBuilder.parse("birds.xml");
            DOMSource xmlDomSource = new DOMSource(xmlDoc);
            xmlDomSource.setSystemId("birds.xml");
            DOMResult domResult = new DOMResult();
            transformer.transform(xmlDomSource, domResult);
            java.util.Properties xmlProps = OutputPropertiesFactory.getDefaultMethodProperties("xml");
            xmlProps.setProperty("indent", "yes");
            xmlProps.setProperty("standalone", "no");
            Serializer serializer = SerializerFactory.getSerializer(xmlProps);
            serializer.setOutputStream(System.out);
            serializer.asDOMSerializer().serialize(domResult.getNode());
        } else {
            throw new org.xml.sax.SAXNotSupportedException("DOM node processing not supported!");
        }
    }
}

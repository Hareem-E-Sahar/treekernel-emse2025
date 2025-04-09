import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author issam
 */
public class CorrectIDs {

    public static void correct(int startID, String xmlFile) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder buidler = factory.newDocumentBuilder();
        Document doc = buidler.parse(new File(xmlFile));
        NodeList childs = doc.getDocumentElement().getElementsByTagName("definition");
        for (int i = 0; i < childs.getLength(); i++) {
            Element element = (Element) childs.item(i);
            element.setAttribute("id", (startID + i) + "");
        }
        TransformerFactory tFact = TransformerFactory.newInstance();
        Transformer transform = tFact.newTransformer();
        transform.transform(new DOMSource(doc), new StreamResult(new File(xmlFile)));
    }

    public static void main(String[] args) {
        try {
            correct(Integer.parseInt(args[0]), args[1]);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(CorrectIDs.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(CorrectIDs.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

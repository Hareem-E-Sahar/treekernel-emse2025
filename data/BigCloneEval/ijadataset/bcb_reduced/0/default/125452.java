import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Oleg Orlov
 */
public class InterceptorTest {

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger(InterceptorTest.class.getName());
        try {
            Document doc1 = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element element = doc1.createElement("preved");
            Element imported = (Element) doc.importNode(element, true);
            Element clone = (Element) element.cloneNode(true);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
}

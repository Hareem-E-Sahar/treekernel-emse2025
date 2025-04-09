import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.*;

class CJobProposition {

    protected Document doc;

    public CJobProposition() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse("PropositionEx.xml");
            Element element = doc.getDocumentElement();
            NodeList ndlst = element.getChildNodes();
            ndlst = (NodeList) xpath.evaluate("/proposition/*", element, XPathConstants.NODESET);
            for (int i = 0; i < ndlst.getLength(); i++) {
                Node nd = ndlst.item(i);
                System.out.println(i);
                System.out.println(nd.getNodeName());
                System.out.println(nd.getTextContent());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void update() {
    }
}

/**
 * @author Slawomir Caluch
 *
 */
interface IField {

    int getMinSize();

    int getMaxSize();

    String getType();

    String getLabel();

    String getLabel(String lang);

    String getDescription();

    String getDescription(String lang);

    boolean getMandatory();
}

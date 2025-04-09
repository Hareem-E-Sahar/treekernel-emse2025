import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlDom {

    public static void main(String[] args) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse("config.xml");
            Node config = document.getDocumentElement();
            NodeList sectionList = config.getChildNodes();
            for (int i = 0; i < sectionList.getLength(); i++) {
                Node section = sectionList.item(i);
                if (!"section".equals(section.getNodeName())) continue;
                System.out.println("============ " + findAttribute(section, "name") + " ================");
                NodeList paramList = section.getChildNodes();
                for (int j = 0; j < paramList.getLength(); j++) {
                    Node parameter = paramList.item(j);
                    if (!"parameter".equals(parameter.getNodeName())) continue;
                    String name = findAttribute(parameter, "name");
                    String value = findAttribute(parameter, "value");
                    System.out.println(name + "=" + value);
                    Node attr = document.createAttribute("value");
                    attr.setNodeValue("test");
                    parameter.getAttributes().setNamedItem(attr);
                }
                Node par = document.createElement("neu");
                Node attr = document.createAttribute("value");
                attr.setNodeValue("test-neu");
                par.getAttributes().setNamedItem(attr);
                attr = document.createAttribute("name");
                attr.setNodeValue("neu");
                par.getAttributes().setNamedItem(attr);
                section.appendChild(par);
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(document), new StreamResult(System.out));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String findAttribute(Node node, String name) {
        NamedNodeMap map = node.getAttributes();
        if (map == null) return null;
        Node att = map.getNamedItem(name);
        if (att == null) return null; else return att.getNodeValue();
    }
}

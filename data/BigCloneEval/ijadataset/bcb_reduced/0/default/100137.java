import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import modules.base.res.Partner;
import org.mga.common.SQLObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author mga
 *
 */
public class ViewParser {

    /**
	 *
	 */
    public ViewParser() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder bilder = factory.newDocumentBuilder();
        Document root = bilder.parse(new File("partner_view.xml"));
        System.out.println(root.getFirstChild().getNodeName());
        root.getElementsByTagName("record");
        this.processRecords(root.getElementsByTagName("record"));
    }

    private StringBuffer __evailXML__(Node node, StringBuffer xml) {
        if (node.getNodeType() == node.ELEMENT_NODE) {
            if (node.hasChildNodes()) {
                xml.append("<" + node.getNodeName());
                if (node.hasAttributes()) {
                    NamedNodeMap attr = node.getAttributes();
                    for (int i = 0; i < attr.getLength(); i++) {
                        xml.append(" " + attr.item(i).getNodeName() + "=\"" + attr.item(i).getNodeValue() + "\"");
                    }
                }
                xml.append(">");
                xml.append("\n");
                NodeList child = node.getChildNodes();
                for (int i = 0; i < child.getLength(); i++) {
                    this.__evailXML__(child.item(i), xml);
                }
            } else {
                xml.append("<" + node.getNodeName());
                if (node.hasAttributes()) {
                    NamedNodeMap attr = node.getAttributes();
                    for (int i = 0; i < attr.getLength(); i++) {
                        xml.append(" " + attr.item(i).getNodeName() + "=\"" + attr.item(i).getNodeValue() + "\"");
                    }
                }
                xml.append("/>");
                xml.append("\n");
            }
        }
        return xml;
    }

    public void processRecords(NodeList list) {
        for (int index = 0; index < list.getLength(); index++) {
            Node node = list.item(index);
            if (node.getNodeType() == node.ELEMENT_NODE && node.getNodeName() == "record") {
                if (node.hasAttributes()) {
                    NamedNodeMap attr = node.getAttributes();
                    String model = attr.getNamedItem("model").getNodeValue();
                    String id = attr.getNamedItem("id").getNodeValue();
                    System.out.println(node.getNodeName() + " : " + model + " : " + id);
                }
                if (node.hasChildNodes()) {
                    NodeList child = node.getChildNodes();
                    for (int i = 0; i < child.getLength(); i++) {
                        if (child.item(i).getNodeType() == child.item(i).ELEMENT_NODE && child.item(i).getNodeName() == "field") {
                            NamedNodeMap attr = child.item(i).getAttributes();
                            String name = attr.getNamedItem("name").getNodeValue();
                            System.out.println(child.item(i).getNodeName() + " : " + name + " : " + child.item(i).getTextContent());
                            String ref = null;
                            if (attr.getNamedItem("ref") != null) {
                                ref = attr.getNamedItem("ref").getNodeValue();
                                System.out.println("ref" + " : " + ref);
                            }
                            String type = null;
                            if (attr.getNamedItem("type") != null) {
                                if (child.item(i).hasChildNodes()) {
                                    NodeList views = child.item(i).getChildNodes();
                                    for (int vi = 0; vi < views.getLength(); vi++) {
                                        StringBuffer sb = new StringBuffer();
                                        this.__evailXML__(views.item(vi), sb);
                                        if (views.item(vi).getNodeName() != "#text") {
                                            sb.append("</" + views.item(vi).getNodeName() + ">");
                                        }
                                        System.out.println(sb.toString().trim());
                                    }
                                } else {
                                    StringBuffer sb = new StringBuffer();
                                    this.__evailXML__(child.item(i), sb);
                                    System.out.println(sb.toString().trim());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
	 * @param args
	 * @throws ParserConfigurationException
	 */
    public static void main(String[] args) {
        try {
            new ViewParser();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

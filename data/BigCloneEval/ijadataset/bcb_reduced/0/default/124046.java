import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GetPortNum {

    public void parseXML(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("Error: Could not open xml file.");
                System.exit(1);
            }
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            @SuppressWarnings("unused") String appName = new String();
            String portNum = new String();
            doc.getDocumentElement().normalize();
            NodeList nodeLst = doc.getElementsByTagName("application");
            for (int i = 0; i < nodeLst.getLength(); i++) {
                Node node = nodeLst.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    NamedNodeMap nodeMap = nodeLst.item(i).getAttributes();
                    for (int j = 0; j < nodeMap.getLength(); j++) {
                        if (nodeMap.item(j).getNodeName() == "name") appName = nodeMap.item(j).getNodeValue();
                    }
                    if (nodeLst.item(i).hasChildNodes()) {
                        NodeList childNodes = nodeLst.item(i).getChildNodes();
                        for (int k = 0; k < childNodes.getLength(); k++) {
                            if (childNodes.item(k).getNodeType() == Node.ELEMENT_NODE) {
                                if (childNodes.item(k).getNodeName() == "servicePortNum") {
                                    portNum = childNodes.item(k).getTextContent();
                                    System.out.println(portNum);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int argCount = args.length;
        if (argCount != 1) {
            System.err.println("Error: Incorrect number of arguments passed.");
            System.exit(1);
        }
        final String appConfigFile = args[0];
        GetPortNum getPortNum = new GetPortNum();
        getPortNum.parseXML(appConfigFile);
    }
}

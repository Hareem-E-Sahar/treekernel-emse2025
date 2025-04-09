import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.io.File;
import java.io.IOException;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.*;

public class XMLParser {

    static boolean DEBUG = false;

    static String type[];

    static String label[];

    static String info[];

    static String id[];

    static boolean bSkip[];

    static String marker;

    static int size;

    static int paramIndex = -1;

    static int nMarker = 0;

    static final String[] typeName = { "none", "Element", "Attr", "Text", "CDATA", "EntityRef", "Entity", "ProcInstr", "Comment", "Document", "DocType", "DocFragment", "Notation", "parameter id" };

    public XMLParser(org.w3c.dom.Node nodeI) {
        int arraySize = 2048;
        type = new String[arraySize];
        label = new String[arraySize];
        info = new String[arraySize];
        id = new String[arraySize];
        bSkip = new boolean[arraySize];
        for (int i = 0; i < arraySize; ++i) {
            bSkip[i] = false;
        }
        printNode(nodeI, "", false, "", false);
    }

    public XMLParser(String file) {
        marker = "";
        nMarker = 0;
        paramIndex = -1;
        size = 0;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(file));
            new XMLParser(document);
        } catch (SAXParseException spe) {
            System.out.println("\n** Parsing error" + ", line " + spe.getLineNumber() + ", uri " + spe.getSystemId());
            System.out.println("   " + spe.getMessage());
            Exception x = spe;
            if (spe.getException() != null) x = spe.getException();
            x.printStackTrace();
        } catch (SAXException sxe) {
            Exception x = sxe;
            if (sxe.getException() != null) x = sxe.getException();
            x.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String argv[]) {
        XMLParser c = new XMLParser(argv[0]);
        for (int i = 0; i < c.getNParam(); ++i) {
            System.out.println(type[i] + "\t" + label[i] + "\t" + info[i] + "\t" + bSkip[i]);
        }
        System.out.println("Channels: " + c.getNParam());
        System.out.println("Marker: " + marker);
        System.out.println("Size: " + size);
        System.out.println("Label.length()" + label.length);
    }

    private void printNode(org.w3c.dom.Node nodeI, String prefix, boolean b, String str, boolean bIsMarker) {
        String temp = nodeI.getNodeName();
        if (temp.equals("parameter")) {
            paramIndex++;
            org.w3c.dom.NamedNodeMap na = nodeI.getAttributes();
            org.w3c.dom.Node nid = na.getNamedItem("id");
            if (nid == null) {
                nid = na.getNamedItem("xml:id");
            }
            id[paramIndex] = nid.getNodeValue();
            org.w3c.dom.NodeList nodeList = nodeI.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node subnode = nodeList.item(i);
                boolean b_is_marker = false;
                if ((id[paramIndex] != null) && (id[paramIndex].equals("Marker"))) {
                    b_is_marker = true;
                }
                printNode(subnode, prefix + "...", true, "", b_is_marker);
            }
        }
        if (b) {
            if (nodeI.getNodeName().equals("#text")) {
                if (str.equals("type")) {
                    type[paramIndex] = nodeI.getNodeValue().trim();
                    if (nodeI.getNodeValue().trim().equals("double")) size += 8;
                    if (nodeI.getNodeValue().trim().equals("float")) size += 4;
                }
                if (str.equals("label")) {
                    label[paramIndex] = nodeI.getNodeValue().trim();
                }
                if (str.equals("info")) {
                    info[paramIndex] = nodeI.getNodeValue().trim();
                }
                if (str.equals("skip")) {
                    String skipStr = nodeI.getNodeValue().trim();
                    if (skipStr.equals("1")) {
                        bSkip[paramIndex] = true;
                    } else {
                        bSkip[paramIndex] = false;
                    }
                }
                if ((str.equals("format")) && (bIsMarker)) {
                    marker = marker + nodeI.getNodeValue().trim();
                    size += nodeI.getNodeValue().trim().length();
                    nMarker++;
                }
            }
            if (nodeI.getNodeType() == Node.ELEMENT_NODE) {
                for (int i = 0; i < nodeI.getAttributes().getLength(); ++i) {
                    printNode(nodeI.getAttributes().item(i), prefix + "---", false, nodeI.getNodeName(), false);
                }
            }
        }
        org.w3c.dom.NodeList nodeList = nodeI.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            org.w3c.dom.Node subnode = nodeList.item(i);
            printNode(subnode, prefix + "...", b, nodeI.getNodeName(), bIsMarker);
        }
    }

    public String[] getType() {
        return type;
    }

    public String[] getInfo() {
        return info;
    }

    public boolean[] getSkip() {
        return bSkip;
    }

    public String[] getLabel() {
        return label;
    }

    public int getSize() {
        return size;
    }

    public String getMarker() {
        return marker;
    }

    public String[] getID() {
        return id;
    }

    public int getNParam() {
        return paramIndex + 1;
    }
}

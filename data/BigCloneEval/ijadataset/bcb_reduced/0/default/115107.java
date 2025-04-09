import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MailSorterConfig {

    private static final String configFileLocation = "mailconfig.xml";

    static void writeConfig(Vector<GroupItem> groups) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(configFileLocation));
            out.write("<?xml version=\"1.0\" ");
            out.write("encoding=\"ISO-8859-1\"?>");
            out.newLine();
            out.write("<grouplist>");
            out.newLine();
            for (int index = 0; index < groups.size(); ++index) {
                out.write(groups.elementAt(index).toXMLString());
            }
            out.write("</grouplist>");
            out.newLine();
            out.flush();
            out.close();
        } catch (UnsupportedEncodingException e) {
            System.out.println("This VM does not support the Latin-1 character set.");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    static Vector<GroupItem> loadConfig() {
        Vector<GroupItem> groups = new Vector<GroupItem>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder domBuilder;
        try {
            File cFile = new File(configFileLocation);
            if (!cFile.exists()) {
                cFile.createNewFile();
                writeConfig(groups);
                return groups;
            }
            domBuilder = domFactory.newDocumentBuilder();
            Document configFile = domBuilder.parse(new File(configFileLocation));
            NodeList nodes = configFile.getElementsByTagName("group");
            ;
            for (int index = 0; index < nodes.getLength(); ++index) {
                Node currentGroup = nodes.item(index);
                String groupName = getGroupName((Element) currentGroup);
                String fileName = getFileName((Element) currentGroup);
                groups.add(new GroupItem(groupName, fileName));
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return groups;
    }

    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "?";
    }

    private static String getGroupName(Element groupElement) {
        String name = null;
        NodeList nameNodes = groupElement.getElementsByTagName("name");
        if (nameNodes.getLength() == 1) {
            Element nameElement = (Element) nameNodes.item(0);
            name = getCharacterDataFromElement(nameElement);
        }
        return name;
    }

    private static String getFileName(Element groupElement) {
        String name = null;
        NodeList nameNodes = groupElement.getElementsByTagName("file");
        if (nameNodes.getLength() == 1) {
            Element nameElement = (Element) nameNodes.item(0);
            name = getCharacterDataFromElement(nameElement);
        }
        return name;
    }
}

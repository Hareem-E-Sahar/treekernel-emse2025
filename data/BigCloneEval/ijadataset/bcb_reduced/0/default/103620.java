import java.io.*;
import java.net.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import org.w3c.dom.traversal.*;

@Deprecated
public class XMLConfigReader {

    /**
	 * Main Method - Runs Application Program shit.
	 * @param args
	 */
    public static void main(String[] args) {
        XMLConfigReader xmlConfigReader = new XMLConfigReader();
    }

    public Document xmlQuery;

    public Document xmlConfig;

    public String xmlResponse = new String();

    /**
	 * Constructor
	 */
    public XMLConfigReader() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            String xmlQueryString = new String("<CommandList>\n<GetRegisteredUser />\n</CommandList>");
            InputStream in = new ByteArrayInputStream(xmlQueryString.getBytes("UTF-8"));
            this.xmlQuery = builder.parse(in);
            this.xmlConfig = builder.parse(new File("trunk/engine/files/xml.conf"));
            this.xmlResponse += "\n<CommandList>\n";
            this.parse(this.xmlQuery.getElementsByTagName("CommandList"));
            this.xmlResponse += "</CommandList>";
            System.out.println(this.xmlResponse);
        } catch (Exception e) {
            System.out.println("Error...");
            e.printStackTrace();
        }
    }

    /**
	 * Recursive Function..?
	 * @param nodeList
	 */
    private void parse(NodeList nl) {
        if (nl.getLength() == 0) {
            return;
        } else {
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).hasChildNodes()) {
                    if (!nl.item(i).hasAttributes()) {
                        if (nl.item(i).getAttributes().getNamedItem("parent") != null) {
                            if (nl.item(i).getAttributes().getNamedItem("parent").equals("true")) {
                            }
                        } else {
                            this.parse(nl.item(i).getChildNodes());
                        }
                    }
                } else {
                    if (nl.item(i).getNodeName().equals("#text")) {
                        continue;
                    } else {
                        System.out.println(nl.item(i).getNodeName());
                        try {
                        } catch (Exception e) {
                            this.xmlResponse += e.toString();
                            e.printStackTrace();
                            return;
                        }
                    }
                }
            }
        }
    }

    public String getResponse() {
        return this.xmlResponse;
    }
}

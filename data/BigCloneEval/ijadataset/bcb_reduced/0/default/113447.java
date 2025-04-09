import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.util.ArrayList;
import java.io.*;

public class QueryXML {

    private Document dom = null;

    private ArrayList<Integer> idList = null;

    private HashMap<String, String> itemHash = null;

    QueryXML(File f) {
        idList = new ArrayList<Integer>();
        itemHash = new HashMap<String, String>();
        parseXmlFile(f);
        parseDocument();
    }

    private void parseXmlFile(File f) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(f);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void parseDocument() {
        Element docEle = dom.getDocumentElement();
        NodeList nl = docEle.getElementsByTagName("Id");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                String val = el.getFirstChild().getTextContent();
                if (val != null) {
                    idList.add(Integer.valueOf(val));
                }
            }
        }
        nl = docEle.getElementsByTagName("Item");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                String name = el.getAttribute("Name");
                String val = el.getFirstChild().getTextContent();
                if (name != null && val != null) {
                    itemHash.put(name, val);
                }
            }
        }
    }

    /**
	 * @return the idList
	 */
    public ArrayList<Integer> getIdList() {
        return idList;
    }

    /**
	 * @return the idList
	 */
    public Integer getIdCount() {
        return idList.size();
    }
}

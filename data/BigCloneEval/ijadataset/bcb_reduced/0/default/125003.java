import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XFDLDocument {

    private static final String FILE_HEADER_BLOCK = "application/vnd.xfdl;content-encoding=\"base64-gzip\"";

    protected static final String XFDL_ELEMENT_PAGE = "page";

    protected String XFDL_ATTRIBUTE_SID = "sid";

    protected String XFDL_ATTRIBUTE_FORMNUMBER = "number";

    protected static String XFDL_QUERY_NEXT = "./next/text()";

    protected static String XFDL_QUERY_PREVIOUS = "./previous/text()";

    protected static final String QUERY_FORM_NUMBER = "/XFDL/globalpage/global/xmlmodel/instances/instance" + "/form_metadata/title/documentnbr/@number";

    protected Document doc;

    private String fileLocation;

    public XFDLDocument(String inputFile) throws IOException, ParserConfigurationException, SAXException {
        fileLocation = inputFile;
        try {
            File f = new File(inputFile);
            if (!f.exists()) {
                throw new IOException("Specified File could not be found!");
            }
            FileInputStream fis = new FileInputStream(inputFile);
            fis.skip(FILE_HEADER_BLOCK.length());
            Base64.InputStream bis = new Base64.InputStream(fis, Base64.DECODE);
            GZIPInputStream gis = new GZIPInputStream(bis);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(gis);
            gis.close();
            bis.close();
            fis.close();
        } catch (ParserConfigurationException pce) {
            throw new ParserConfigurationException("Error parsing XFDL from file.");
        } catch (SAXException saxe) {
            throw new SAXException("Error parsing XFDL into XML Document.");
        }
    }

    /**
	 * Saves the current document to the current file location.  
	 * Current location is the original location unless it has been modified.
	 * @throws IOException File cannot be created at specified location 
	 */
    public void saveFile() throws IOException, TransformerConfigurationException, TransformerException {
        saveFile(fileLocation, false);
    }

    /**
	 * Saves the current document to the specified location
	 * @param destination Desired destination for the file.
	 * @param asXML True if output needs should be as un-encoded XML not Base64/GZIP
	 * @throws IOException File cannot be created at specified location
	 * @throws TransformerConfigurationExample
	 * @throws TransformerException 
	 */
    public void saveFile(String destination, boolean asXML) throws IOException, TransformerConfigurationException, TransformerException {
        BufferedWriter bf = new BufferedWriter(new FileWriter(destination));
        bf.write(FILE_HEADER_BLOCK);
        bf.newLine();
        bf.flush();
        bf.close();
        OutputStream outStream;
        if (!asXML) {
            outStream = new GZIPOutputStream(new Base64.OutputStream(new FileOutputStream(destination, true)));
        } else {
            outStream = new FileOutputStream(destination, true);
        }
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.transform(new DOMSource(doc), new StreamResult(outStream));
        outStream.flush();
        outStream.close();
    }

    /**
	 * Gets the number of pages in the current XFDL Document
	 * @return Number of pages in document. 
	 */
    public int getPageCount() {
        Element e = doc.getDocumentElement();
        NodeList nl = e.getElementsByTagName(XFDL_ELEMENT_PAGE);
        return nl.getLength();
    }

    /**
	 * Modifies specified node with new value
	 * @param sid SID of Node to be modified
	 * @param value New value for specified field
	 * @throws XPathExpressionException Throw if there is an XPath error 
	 * 		while searching for the field. 
	 * @throws InvalidFieldException Thrown if the requested field 
	 * 		cannot be found.
	 */
    public void setFieldValue(String sid, String value) throws XPathExpressionException, InvalidFieldException {
        Node resultNode;
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            resultNode = (Node) xPath.evaluate(buildFieldValueQuery(sid), doc, XPathConstants.NODE);
        } catch (XPathExpressionException xpee) {
            throw new XPathExpressionException("Error while retriving node: " + sid);
        }
        if (resultNode != null) {
            resultNode.setTextContent(value);
        } else {
            throw new InvalidFieldException("Unable to locate requested field for modification");
        }
    }

    /**
	 * Builds an XPath Query that allows us to find the value
	 * option of a given field.  
	 * @param sid SID of the desired field.
	 * @return String representation of an XPath Query for value of given 
	 * field 
	 */
    protected String buildFieldValueQuery(String sid) {
        return "//field[@" + XFDL_ATTRIBUTE_SID + "='" + sid + "']/value";
    }

    /***
	 * Locates the Document Number information in the file and returns the form number.
	 * @return File's self-declared number.
	 * @throws InvalidFormException Thrown when XPath cannot find the "documentnbr" element in the file.
	 */
    public String getFormNumber() throws InvalidFormException {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Node result = (Node) xPath.evaluate(QUERY_FORM_NUMBER, doc, XPathConstants.NODE);
            if (result != null) {
                return result.getNodeValue();
            } else {
                throw new InvalidFormException("Unable to identify form.");
            }
        } catch (XPathExpressionException err) {
            throw new InvalidFormException("Unable to find form number in file.");
        }
    }
}

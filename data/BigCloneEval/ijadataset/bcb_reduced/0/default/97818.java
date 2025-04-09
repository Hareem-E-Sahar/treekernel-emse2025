import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xpath.domapi.XPathEvaluatorImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathEvaluator;
import org.w3c.dom.xpath.XPathNSResolver;
import org.w3c.dom.xpath.XPathResult;
import org.xml.sax.InputSource;

/**
 *  Very basic utility for applying the DOM L3 XPath API (currently in Last Call)
 *  to an xml file and printing information about the execution of the XPath object 
 *  and the nodes it finds.
 *  Takes 2 arguments:
 *     (1) an xml filename
 *     (2) an XPath expression to apply to the file
 *  Examples:
 *     java ApplyXPathDOM foo.xml /
 *     java ApplyXPathDOM foo.xml /doc/name[1]/@last
 *
 *<p>See also the <a href='http://www.w3.org/TR/2004/NOTE-DOM-Level-3-XPath-20040226'>Document Object Model (DOM) Level 3 XPath Specification</a>.</p>
 * @see XPathEvaluator
 * 
 */
public class ApplyXPathDOM {

    protected String filename = null;

    protected String xpath = null;

    /** Process input args and execute the XPath.  */
    public void doMain(String[] args) throws Exception {
        filename = args[0];
        xpath = args[1];
        if ((filename != null) && (filename.length() > 0) && (xpath != null) && (xpath.length() > 0)) {
            System.out.println("Loading classes, parsing " + filename + ", and setting up serializer");
            InputSource in = new InputSource(new FileInputStream(filename));
            DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
            dfactory.setNamespaceAware(true);
            Document doc = dfactory.newDocumentBuilder().parse(in);
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            System.out.println("Querying DOM using " + xpath);
            XPathEvaluator evaluator = new XPathEvaluatorImpl(doc);
            XPathNSResolver resolver = evaluator.createNSResolver(doc);
            XPathResult result = (XPathResult) evaluator.evaluate(xpath, doc, resolver, XPathResult.UNORDERED_NODE_ITERATOR_TYPE, null);
            System.out.println("<output>");
            Node n;
            while ((n = result.iterateNext()) != null) {
                if (isTextNode(n)) {
                    StringBuffer sb = new StringBuffer(n.getNodeValue());
                    for (Node nn = n.getNextSibling(); isTextNode(nn); nn = nn.getNextSibling()) {
                        sb.append(nn.getNodeValue());
                    }
                    System.out.print(sb);
                } else {
                    serializer.transform(new DOMSource(n), new StreamResult(new OutputStreamWriter(System.out)));
                }
                System.out.println();
            }
            System.out.println("</output>");
        } else {
            System.out.println("Bad input args: " + filename + ", " + xpath);
        }
    }

    /** Decide if the node is text, and so must be handled specially */
    static boolean isTextNode(Node n) {
        if (n == null) return false;
        short nodeType = n.getNodeType();
        return nodeType == Node.CDATA_SECTION_NODE || nodeType == Node.TEXT_NODE;
    }

    /** Main method to run from the command line.    */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("java ApplyXPathDOM filename.xml xpath\n" + "Reads filename.xml and applies the xpath; prints the nodelist found.");
            return;
        }
        ApplyXPathDOM app = new ApplyXPathDOM();
        app.doMain(args);
    }
}

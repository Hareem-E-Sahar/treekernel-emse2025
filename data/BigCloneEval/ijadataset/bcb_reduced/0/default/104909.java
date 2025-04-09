import java.io.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

/**
 * DomDocumentPrinter uses the Document Object Model (DOM) to read an XML file and print each node out to the output console.
 * Default attribute values are not hidden.
 *
 * Currently hard-coded to use Java API for XML Parsing (JAXP) version&nbsp;1.1
 *
 *@version $Id: DomDocumentPrinter.java 7724 2010-08-07 21:12:38Z brutzman $
 *@author <a href="mailto:brutzman@nps.edu">Don Brutzman</a> (<a href="http://web.nps.navy.mil/~brutzman">http://web.nps.navy.mil/~brutzman</a>)
 *
 *@created 21 October 2000
 *@revised 06 July    2007
 *
 *<dt><b>References:</b>
 *<dd>		JAXP 1.1: <a href="http://java.sun.com/xml/download.html">http://java.sun.com/xml/download.html</a>
 *<dd>		JAXP demo program: <a href="C:\Program Files\JavaSoft\jaxp-1.1ea\examples\dom\main.java">C:\Program Files\JavaSoft\jaxp-1.1ea\examples\dom\main.java</a>
 *<dd>		Brett McLaughlin, <i>Java and XML</i>, O'Reilly &amp; Associates, 2000, chapter&nbsp;7: <a href="C:\java\javaxml\ch07\DOMParserDemo.java">C:\java\javaxml\ch07\DOMParserDemo.java</A>
 *		available via <a href="http://www.newInstance.com">"http://www.newInstance.com</a>
 *
 *@see DomDocumentOutputStreamWriter
 *@see ContentCatalogBuilder
 */
public class DomDocumentPrinter {

    protected String indentLevel = "  ";

    protected int lineBreakWidth = 100;

    private int lineLength = 0;

    public int getIndentLevel() {
        return indentLevel.length();
    }

    public void setIndentLevel(int numberOfSpaces) {
        if (numberOfSpaces < 1) {
            numberOfSpaces = 1;
            System.out.println("*** error, numberOfSpaces=" + numberOfSpaces + ", reset to 1");
        }
        indentLevel = "";
        for (int i = 0; i < numberOfSpaces; i++) {
            indentLevel += " ";
        }
    }

    public String getIndent() {
        return indentLevel;
    }

    public void setIndent(String newIndent) {
        indentLevel = newIndent;
    }

    /**
     * Convenience method for printNode with default zero indent.
     * @param node 
     */
    public void printNode(Node node) {
        printNode(node, "");
        System.out.println();
        System.out.println();
    }

    /**
     * Recurse through all of the XML nodes and output them.
     * @param node
     * @param indent 
     */
    public void printNode(Node node, String indent) {
        boolean nodeChildFound = false;
        switch(node.getNodeType()) {
            case Node.DOCUMENT_NODE:
                System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                NodeList nodes = node.getChildNodes();
                if (nodes != null) {
                    for (int i = 0; i < nodes.getLength(); i++) {
                        printNode(nodes.item(i), "");
                    }
                }
                break;
            case Node.DOCUMENT_TYPE_NODE:
                DocumentType documentType = (DocumentType) node;
                System.out.print("<!DOCTYPE " + documentType.getName());
                if (documentType.getName().compareTo("X3D") == 0) {
                    System.out.print("\n");
                    System.out.print(" PUBLIC \"http://www.web3d.org/specifications/x3d-3.1.dtd\"" + "\n");
                    System.out.print("        \"file:///www.web3d.org/TaskGroups/x3d/translation/x3d-3.1.dtd\"" + "\n");
                } else if ((documentType.getPublicId() != null) && (documentType.getSystemId() != null)) {
                    System.out.print("\n");
                    System.out.print(" PUBLIC \"" + documentType.getPublicId() + "\"" + "\n");
                    System.out.print("        \"" + documentType.getSystemId());
                } else if ((documentType.getPublicId() == null) && (documentType.getSystemId() != null)) {
                    System.out.print(" SYSTEM \"" + documentType.getSystemId());
                } else {
                    System.out.print("");
                }
                nodes = node.getChildNodes();
                if (nodes != null) {
                    for (int i = 0; i < nodes.getLength(); i++) {
                        printNode(nodes.item(i), "");
                    }
                }
                if ((documentType.getPublicId() != null) && (documentType.getSystemId() != null)) {
                    System.out.print("\">" + "\n");
                } else if ((documentType.getPublicId() == null) && (documentType.getSystemId() != null)) {
                    System.out.print("\">" + "\n");
                } else {
                    System.out.print(">" + "\n");
                }
                break;
            case Node.ELEMENT_NODE:
                String name = node.getNodeName();
                System.out.print(indent + "<" + name);
                lineLength = name.length() + 2;
                NamedNodeMap attributes = node.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node current = attributes.item(i);
                    lineLength = lineLength + current.getNodeName().length() + current.getNodeValue().length() + 4;
                    if (lineLength > lineBreakWidth) {
                        System.out.println();
                        System.out.print(indent + "  ");
                        lineLength = current.getNodeName().length() + indent.length() + current.getNodeValue().length() + 2;
                    }
                    System.out.print(" " + current.getNodeName() + "=\'" + current.getNodeValue() + "\'");
                }
                NodeList children = node.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    if (children.item(i).getNodeType() != Node.TEXT_NODE) {
                        nodeChildFound = true;
                        break;
                    }
                }
                if (nodeChildFound == true) {
                    System.out.println(">");
                    for (int i = 0; i < children.getLength(); i++) {
                        printNode(children.item(i), indent + indentLevel);
                    }
                    System.out.println(indent + "</" + name + ">");
                } else {
                    System.out.println("/>");
                }
                break;
            case Node.TEXT_NODE:
                System.out.print(node.getNodeValue().trim());
                break;
            case Node.CDATA_SECTION_NODE:
                System.out.println(indent + "<![CDATA[" + node.getNodeValue() + "]]>");
                break;
            case Node.COMMENT_NODE:
                System.out.println(indent + "<!--" + node.getNodeValue() + "-->");
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                System.out.println(indent + "<?" + node.getNodeName() + " " + node.getNodeValue() + "?>");
                break;
            case Node.ENTITY_REFERENCE_NODE:
                System.out.print("&" + node.getNodeName() + ";");
                break;
        }
    }

    /**
     * Read the filename from the command line as a Uniform Resource Identifier (URI), meaning a URL address.
     * Example local filename:
     * <a href="file:///C:/www.web3D.org/TaskGroups/x3d/content/examples/Vrml2.0Sourcebook/Figure02.1Hut.xml">file:///C:/www.web3D.org/TaskGroups/x3d/content/examples/Vrml2.0Sourcebook/Figure02.1Hut.xml</a>
     * @param argv
     * @throws IOException
     * @throws DOMException
     * @throws ParserConfigurationException 
     */
    public static void main(String argv[]) throws IOException, DOMException, ParserConfigurationException {
        if (argv.length < 1) {
            System.out.println("Usage: java DomDocumentPrinter filename.xml [otherFiles.xml]");
            System.exit(0);
        }
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setValidating(false);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        X3dCustomHandler handler = new X3dCustomHandler();
        documentBuilder.setEntityResolver(handler);
        documentBuilder.setErrorHandler(handler);
        Document document = documentBuilder.newDocument();
        DomDocumentPrinter domDocumentPrinter = new DomDocumentPrinter();
        for (int i = 0; i < argv.length; i++) {
            String fileName = argv[i];
            try {
                if (argv.length > 1) {
                    System.out.println("\nParsing " + fileName + "\n");
                }
                handler.setInputSource(new InputSource(fileName));
                document = documentBuilder.parse(fileName);
            } catch (Exception badParse) {
                System.out.println("Error in parsing: " + badParse.getMessage());
                badParse.printStackTrace();
            }
            domDocumentPrinter.printNode(document);
        }
    }
}

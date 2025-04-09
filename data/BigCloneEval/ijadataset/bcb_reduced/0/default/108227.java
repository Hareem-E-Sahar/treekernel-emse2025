import java.io.File;
import java.io.*;
import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
  FileParsing.java

  This class is to parse an XML document stored in a file (argv[0])
  By retrieving and storing the whole file content into doc
  Do the convertion.
  Finally, store the changed doc as a whole into the orignal file by transformer.

    @author          Phoebe Wong
    @company         DCIVision Limited
    @creation date   21/07/2003
    @version         $Revision: 1.1 $
*/
public class FileParsing {

    private Document doc;

    public FileParsing() {
    }

    public void setDoc(Document doc) {
        this.doc = doc;
    }

    /**
   * changeOrAddAttr
   *
   * To add a new attribute 
   * Or to change the value of the an orignally existed attribute
   * It depends on the attrName pass in
   *
   * @param attrName      The orginal or new attribute name
   * @param attrValue     New value of the attribute
   *  
   */
    public void changeOrAddAttr(String attrName, String attrValue) {
        doc.getDocumentElement().setAttribute(attrName, attrValue);
    }

    /**
   * replaceNode
   *
   * To replace the orginal node by a new node
   * Or just to change the content an originally existed node
   * If just to change the content, oldNodeName=newNodeName
   *
   * @param oldNodeName      The orginal node name
   * @param newNodeName      The new node name
   * @param textNode1        The content of the node
   *  
   */
    public void replaceNode(String oldNodeName, String newNodeName, String textNode1) {
        NodeList ls = doc.getElementsByTagName(oldNodeName);
        Element oldChild = (Element) ls.item(0);
        Element newChild = doc.createElement(newNodeName);
        newChild.appendChild(doc.createTextNode(textNode1));
        oldChild.getParentNode().replaceChild(newChild, oldChild);
    }

    /**
   * duplicateNode
   *
   * To duplicate a node and insert it immediately after the cloned element
   *
   * @param DupNodeName      The name of the node to be duplicated
   *  
   */
    public void duplicateNode(String DupNodeName) {
        NodeList ls = doc.getElementsByTagName(DupNodeName);
        Element element = (Element) ls.item(0);
        Element dup = (Element) element.cloneNode(true);
        element.getParentNode().insertBefore(dup, element.getNextSibling());
    }

    /**
   * insertNewNode
   *
   * To insert a sibling node
   *
   * @param siblingNodeName  The sibling node name
   * @param newNodeName      The new node name
   * @param textNode         The content of the new node
   *  
   */
    public void insertNewNode(String siblingNodeName, String newNodeName, String textNode) {
        NodeList ls = doc.getElementsByTagName(siblingNodeName);
        Element siblingNode = (Element) ls.item(0);
        Element insertNode = doc.createElement(newNodeName);
        insertNode.appendChild(doc.createTextNode(textNode));
        siblingNode.getParentNode().insertBefore(insertNode, siblingNode.getNextSibling());
    }

    /**
   * addCommentBefore
   *
   * Adding a comment before a DOM element
   *
   * @param nodeName         The sibling node name
   * @param newComment       The comment to be added
   *  
   */
    public void addCommentBefore(String nodeName, String newComment) {
        NodeList ls = doc.getElementsByTagName(nodeName);
        for (int i = 0; i < ls.getLength(); i++) {
            Element element = (Element) ls.item(i);
            Comment comment = doc.createComment(newComment);
            element.getParentNode().insertBefore(comment, element);
        }
    }

    /**
   * addCommentBehind
   *
   * Adding a comment after a DOM element
   *
   * @param nodeName         The sibling node name
   * @param newComment       The comment to be added
   *  
   */
    public void addCommentBehind(String nodeName, String newComment) {
        NodeList ls = doc.getElementsByTagName(nodeName);
        for (int i = 0; i < ls.getLength(); i++) {
            Element element = (Element) ls.item(i);
            Comment comment = doc.createComment(i + " " + newComment);
            element.getParentNode().insertBefore(comment, element.getNextSibling());
        }
    }

    /**
   * setOrAddDOMAttr
   *
   * To add a new attribute in a DOM Element 
   * Or to change the value of the an attribute in a DOM Element
   * It depends on the attrName pass in
   *
   * @param nodeName      The name of the node
   * @param attrName      The orginal or new attribute name
   * @param attrValue     New value of the attribute
   *  
   */
    public void setOrAddDOMAttr(String nodeName, String attrName, String attrValue) {
        NodeList ls = doc.getElementsByTagName(nodeName);
        for (int i = 0; i < ls.getLength(); i++) {
            Element element = (Element) ls.item(i);
            element.setAttribute(attrName, attrValue);
        }
    }

    /**
   * listDOMAttr
   *
   * To list all the attributes of a DOM Element
   *
   * @param nodeName      The name of the node wants to displace
   *  
   */
    public void listDOMAttr(String nodeName) {
        NodeList ls = doc.getElementsByTagName(nodeName);
        for (int i = 0; i < ls.getLength(); i++) {
            Element element = (Element) ls.item(i);
            NamedNodeMap attrs = element.getAttributes();
            for (int j = 0; j < attrs.getLength(); j++) {
                Attr attri = (Attr) attrs.item(j);
                String attrName = attri.getNodeName();
                String attrValue = attri.getNodeValue();
                System.out.print(attrName + " ");
                System.out.print(attrValue);
                System.out.println();
            }
        }
    }

    /**
   * splitTextNode
   *
   * Splitting a Text Node in a DOM by inserting a new element
   *
   * @param nodeName      The name of the node wants to be splitted
   * @param position      The text position to be splitted
   *  
   */
    public void splitTextNode(String nodeName, String position) {
        NodeList ls = doc.getElementsByTagName(nodeName);
        Element element = (Element) ls.item(0);
        Text text1 = (Text) element.getFirstChild();
        String string = text1.getData();
        Text text2 = text1.splitText(string.indexOf(position));
        Text text3 = text2.splitText(position.length());
        Element newElement = doc.createElement("o");
        newElement.appendChild(text2);
        element.insertBefore(newElement, text3);
    }

    /**
   * mergeTextNode
   *
   * Merging Text Node in a DOM by removing an element
   *
   * @param nodeName      The name of the node to be removed
   *  
   */
    public void mergeTextNode(String nodeName) {
        NodeList ls = doc.getElementsByTagName(nodeName);
        Element element = (Element) ls.item(0);
        Node parent = element.getParentNode();
        while (element.hasChildNodes()) {
            parent.insertBefore(element.getFirstChild(), element);
        }
        parent.removeChild(element);
        parent.normalize();
    }

    /**
   * removeOneNode   
   * Remove a specified node only
   * @param nodeName      The name of the node to be removed
   */
    public void removeOneNode(String nodeName) {
        NodeList ls = doc.getElementsByTagName(nodeName);
        Element element = (Element) ls.item(0);
        element.getParentNode().removeChild(element);
    }

    /**
   * removeALL  
   * Remove a specified node including all of its child and comment
   * @param node          The document
   * @param nodeType      The type of the node  
   * @param nodeName      The name of the node to be removed   
   */
    public static void removeAll(Node node, short nodeType, String nodeName) {
        if (node.getNodeType() == nodeType && (nodeName == null || node.getNodeName().equals(nodeName))) {
            node.getParentNode().removeChild(node);
        } else {
            NodeList ls = node.getChildNodes();
            for (int i = 0; i < ls.getLength(); i++) {
                removeAll(ls.item(i), nodeType, nodeName);
            }
        }
    }

    /**
   * addPI
   * Add a PI at the beginning of the document
   * @param nodeName      The name of the mother node
   * @param ipName        The name of the IP node
   * @param instruct      The instruction to be added
   */
    public void addPI(String nodeName, String ipName, String instruct) {
        NodeList ls = doc.getElementsByTagName(nodeName);
        Element element = (Element) ls.item(0);
        ProcessingInstruction pi = doc.createProcessingInstruction(ipName, instruct);
        element.getParentNode().insertBefore(pi, element);
    }

    /**
   * addCDATA 
   * Add a CDATA section to the root element
   * @param nodeName      The name of the mother node
   * @param cData         The value of the CDATA to be added
   */
    public void addCDATA(String nodeName, String cData) {
        NodeList ls = doc.getElementsByTagName(nodeName);
        Element element = (Element) ls.item(0);
        CDATASection cdata = doc.createCDATASection(cData);
        element.appendChild(cdata);
    }

    /************************ main **************************************/
    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Usage: cmd filename");
            System.exit(1);
        }
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(argv[0]));
            doc.getDocumentElement().normalize();
            String sTmp = doc.getDocumentElement().toString();
            String attr = (String) doc.getDocumentElement().getNodeName();
            System.out.println("Root element of the doc is " + attr);
            FileParsing f = new FileParsing();
            f.setDoc(doc);
            f.changeOrAddAttr("author", "Phoebe");
            f.replaceNode("title", "title", "Kinbe");
            f.insertNewNode("title", "Phoebe", "Yeah!!");
            f.setOrAddDOMAttr("slide", "type", "Good!!");
            f.setOrAddDOMAttr("slide", "Kinbe", "Wahaha!!");
            f.listDOMAttr("slide");
            f.addCommentBefore("title", "I O U");
            f.addCommentBehind("slide", "MeiMei");
            f.removeAll(doc, Node.ELEMENT_NODE, "item");
            f.removeOneNode("Lee");
            f.splitTextNode("Jingle", "orange");
            f.mergeTextNode("drink");
            f.addPI("slide", "Kenbe", "Good");
            f.addCDATA("Angus", "CD");
            File outputFile = new File(argv[0]);
            FileWriter fo = new FileWriter(outputFile);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(doc.getDocumentElement());
            StreamResult result = new StreamResult(fo);
            transformer.transform(source, result);
        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println("   " + err.getMessage());
        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.exit(0);
    }
}

package org.exist.xquery.functions.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.log4j.Logger;
import org.exist.EXistException;
import org.exist.dom.DocumentImpl;
import org.exist.dom.QName;
import org.exist.dom.StoredNode;
import org.exist.stax.EmbeddedXMLStreamReader;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.xquery.Cardinality;
import org.exist.xquery.Function;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Delivers the fragment between two nodes (normally milestones) of a document.
 * It leads to more performance for most XML documents because it
 * determines the fragment directly by the EmbeddedXmlReader and not by 
 * XQL operators.
 * @author Josef Willenborg, Max Planck Institute for the history of science,
 * http://www.mpiwg-berlin.mpg.de, jwillenborg@mpiwg-berlin.mpg.de 
 */
public class GetFragmentBetween extends Function {

    protected static final Logger logger = Logger.getLogger(GetFragmentBetween.class);

    public static final FunctionSignature signature = new FunctionSignature(new QName("get-fragment-between", UtilModule.NAMESPACE_URI, UtilModule.PREFIX), "Returns an xml fragment or a sequence of nodes between two elements (normally milestone elements). " + "This function works only on documents which are stored in eXist DB." + "The $beginning-node represents the first node/milestone element, $ending-node, the second one. " + "The third argument, $make-fragment, is " + "a boolean value for the path completion. If it is set to true() the " + "result sequence is wrapped into a parent element node. " + "The fourth argument, display-root-namespace, is " + "a boolean value for displaying the root node namespace. If it is set to true() the " + "attribute \"xmlns\" in the root node of the result sequence is determined explicitely from the $beginning-node. " + "Example call of the function for getting the fragment between two TEI page break element nodes: " + "  let $fragment := util:get-fragment-between(//pb[1], //pb[2], true(), true())", new SequenceType[] { new FunctionParameterSequenceType("beginning-node", Type.NODE, Cardinality.ZERO_OR_ONE, "The first node/milestone element"), new FunctionParameterSequenceType("ending-node", Type.NODE, Cardinality.ZERO_OR_ONE, "The second node/milestone element"), new FunctionParameterSequenceType("make-fragment", Type.BOOLEAN, Cardinality.ZERO_OR_ONE, "The flag make a fragment."), new FunctionParameterSequenceType("display-root-namespace", Type.BOOLEAN, Cardinality.ZERO_OR_ONE, "Display the namespace of the root node of the fragment.") }, new FunctionReturnSequenceType(Type.STRING, Cardinality.ONE, "the string containing the fragment between the two node/milestone elements."), true);

    public GetFragmentBetween(XQueryContext context) {
        super(context, signature);
    }

    /**
   * Get the fragment between two elements (normally milestone elements) of a document 
   * @param args 1. first node (e.g. pb[10])  2. second node (e.g.: pb[11]) 3. pathCompletion:
   * open and closing tags before and after the fragment are appended (Default: true) 
   * 4. Display the namespace of the root node of the fragment (Default: false)
   * @return the fragment between the two nodes
   * @throws XPathException
   */
    public Sequence eval(Sequence contextSequence, Item contextItem) throws XPathException {
        int argumentCount = getArgumentCount();
        if (argumentCount < 2) {
            logger.error("requires at least 2 arguments");
            throw new XPathException(this, "requires at least 2 arguments");
        }
        Sequence ms1 = getArgument(0).eval(contextSequence, contextItem);
        Sequence ms2 = getArgument(1).eval(contextSequence, contextItem);
        if (ms1.isEmpty()) {
            throw new XPathException(this, "your first argument delivers an empty node (no valid node position in document)");
        }
        Node ms1Node = null;
        if (!(ms1.itemAt(0) == null)) ms1Node = ((NodeValue) ms1.itemAt(0)).getNode();
        Node ms2Node = null;
        if (!(ms2.itemAt(0) == null)) ms2Node = ((NodeValue) ms2.itemAt(0)).getNode();
        boolean pathCompletion = true;
        if (argumentCount > 2) {
            Sequence seqPathCompletion = getArgument(2).eval(contextSequence, contextItem);
            pathCompletion = seqPathCompletion.effectiveBooleanValue();
        }
        boolean displayRootNamespace = false;
        if (argumentCount > 3) {
            Sequence seqDisplayRootNamespace = getArgument(3).eval(contextSequence, contextItem);
            displayRootNamespace = seqDisplayRootNamespace.effectiveBooleanValue();
        }
        StringBuilder fragment = getFragmentBetween(ms1Node, ms2Node);
        if (pathCompletion) {
            String msFromPathName = getNodeXPath(ms1Node.getParentNode(), displayRootNamespace);
            String openElementsOfMsFrom = pathName2XmlTags(msFromPathName, "open");
            String closingElementsOfMsTo = "";
            if (!(ms2Node == null)) {
                String msToPathName = getNodeXPath(ms2Node.getParentNode(), displayRootNamespace);
                closingElementsOfMsTo = pathName2XmlTags(msToPathName, "close");
            }
            fragment.insert(0, openElementsOfMsFrom);
            fragment.append(closingElementsOfMsTo);
        }
        StringValue strValFragment = new StringValue(fragment.toString());
        ValueSequence resultFragment = new ValueSequence();
        resultFragment.add(strValFragment);
        return resultFragment;
    }

    /**
   * Fetch the fragment between two nodes (normally milestones) in an XML document
   * @param node1 first node from which down to the node node2 the XML fragment is delivered as a string
   * @param node2 the node to which down the XML fragment is delivered as a string
   * @return fragment between the two nodes
   * @throws XPathException
   */
    private StringBuilder getFragmentBetween(Node node1, Node node2) throws XPathException {
        StoredNode storedNode1 = (StoredNode) node1;
        StoredNode storedNode2 = (StoredNode) node2;
        String node1NodeId = storedNode1.getNodeId().toString();
        String node2NodeId = "-1";
        if (!(node2 == null)) node2NodeId = storedNode2.getNodeId().toString();
        DocumentImpl docImpl = (DocumentImpl) node1.getOwnerDocument();
        BrokerPool brokerPool = null;
        DBBroker dbBroker = null;
        StringBuilder resultFragment = new StringBuilder("");
        String actualNodeId = "-2";
        boolean getFragmentMode = false;
        try {
            brokerPool = docImpl.getBrokerPool();
            dbBroker = brokerPool.get(null);
            EmbeddedXMLStreamReader reader = null;
            NodeList children = docImpl.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                StoredNode docChildStoredNode = (StoredNode) children.item(i);
                int docChildStoredNodeType = docChildStoredNode.getNodeType();
                reader = dbBroker.getXMLStreamReader(docChildStoredNode, false);
                while (reader.hasNext() && !node2NodeId.equals(actualNodeId) && docChildStoredNodeType != Node.PROCESSING_INSTRUCTION_NODE && docChildStoredNodeType != Node.COMMENT_NODE) {
                    int status = reader.next();
                    switch(status) {
                        case XMLStreamReader.START_DOCUMENT:
                        case XMLStreamReader.END_DOCUMENT:
                            break;
                        case XMLStreamReader.START_ELEMENT:
                            actualNodeId = reader.getNode().getNodeId().toString();
                            if (actualNodeId.equals(node1NodeId)) getFragmentMode = true;
                            if (actualNodeId.equals(node2NodeId)) getFragmentMode = false;
                            if (getFragmentMode) {
                                String startElementTag = getStartElementTag(reader);
                                resultFragment.append(startElementTag);
                            }
                            break;
                        case XMLStreamReader.END_ELEMENT:
                            if (getFragmentMode) {
                                String endElementTag = getEndElementTag(reader);
                                resultFragment.append(endElementTag);
                            }
                            break;
                        case XMLStreamReader.CHARACTERS:
                            if (getFragmentMode) {
                                String characters = getCharacters(reader);
                                resultFragment.append(characters);
                            }
                            break;
                        case XMLStreamReader.CDATA:
                            if (getFragmentMode) {
                                String cdata = getCDataTag(reader);
                                resultFragment.append(cdata);
                            }
                            break;
                        case XMLStreamReader.COMMENT:
                            if (getFragmentMode) {
                                String comment = getCommentTag(reader);
                                resultFragment.append(comment);
                            }
                            break;
                        case XMLStreamReader.PROCESSING_INSTRUCTION:
                            if (getFragmentMode) {
                                String piTag = getPITag(reader);
                                resultFragment.append(piTag);
                            }
                            break;
                    }
                }
            }
        } catch (EXistException e) {
            throw new XPathException(this, "An error occurred while getFragmentBetween: " + e.getMessage(), e);
        } catch (XMLStreamException e) {
            throw new XPathException(this, "An error occurred while getFragmentBetween: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new XPathException(this, "An error occurred while getFragmentBetween: " + e.getMessage(), e);
        } finally {
            if (brokerPool != null) brokerPool.release(dbBroker);
        }
        return resultFragment;
    }

    private String getStartElementTag(EmbeddedXMLStreamReader reader) {
        String elemName = reader.getLocalName();
        String elemAttrString = "";
        String elemNsString = "";
        int nsCount = reader.getNamespaceCount();
        for (int ni = 0; ni < nsCount; ni++) {
            String nsPrefix = reader.getNamespacePrefix(ni);
            String nsUri = reader.getNamespaceURI(ni);
            String nsString = "xmlns:" + nsPrefix + "=\"" + nsUri + "\"";
            if (nsPrefix != null && nsPrefix.equals("")) nsString = "xmlns" + "=\"" + nsUri + "\"";
            elemNsString = elemNsString + " " + nsString;
        }
        int attrCount = reader.getAttributeCount();
        for (int j = 0; j < attrCount; j++) {
            String attrNamePrefix = reader.getAttributePrefix(j);
            String attrName = reader.getAttributeLocalName(j);
            String attrValue = reader.getAttributeValue(j);
            attrValue = escape(attrValue);
            String attrString = "";
            if (!(attrNamePrefix == null || attrNamePrefix.length() == 0)) attrString = attrNamePrefix + ":";
            attrString = attrString + attrName + "=\"" + attrValue + "\"";
            elemAttrString = elemAttrString + " " + attrString;
        }
        String elemPrefix = reader.getPrefix();
        String elemPart = "";
        if (!(elemPrefix == null || elemPrefix.length() == 0)) elemPart = elemPrefix + ":";
        elemPart = elemPart + elemName;
        String elementString = "<" + elemPart + elemNsString + elemAttrString + ">";
        return elementString;
    }

    private String getEndElementTag(EmbeddedXMLStreamReader reader) {
        String elemName = reader.getLocalName();
        String elemPrefix = reader.getPrefix();
        String elemPart = "";
        if (!(elemPrefix == null || elemPrefix.length() == 0)) elemPart = elemPrefix + ":";
        elemPart = elemPart + elemName;
        return "</" + elemPart + ">";
    }

    private String getCharacters(EmbeddedXMLStreamReader reader) {
        String xmlChars = reader.getText();
        xmlChars = escape(xmlChars);
        return xmlChars;
    }

    private String getCDataTag(EmbeddedXMLStreamReader reader) {
        char[] chars = reader.getTextCharacters();
        return "<![CDATA[\n" + new String(chars) + "\n]]>";
    }

    private String getCommentTag(EmbeddedXMLStreamReader reader) {
        char[] chars = reader.getTextCharacters();
        return "<!--" + new String(chars) + "-->";
    }

    private String getPITag(EmbeddedXMLStreamReader reader) {
        String piTarget = reader.getPITarget();
        String piData = reader.getPIData();
        if (!(piData == null || piData.length() == 0)) piData = " " + piData; else piData = "";
        return "<?" + piTarget + piData + "?>";
    }

    private String escape(String inputStr) {
        StringBuilder resultStrBuf = new StringBuilder();
        for (int i = 0; i < inputStr.length(); i++) {
            char ch = inputStr.charAt(i);
            switch(ch) {
                case '<':
                    resultStrBuf.append("&lt;");
                    break;
                case '>':
                    resultStrBuf.append("&gt;");
                    break;
                case '&':
                    resultStrBuf.append("&amp;");
                    break;
                case '\"':
                    resultStrBuf.append("&quot;");
                    break;
                case '\'':
                    resultStrBuf.append("&#039;");
                    break;
                default:
                    resultStrBuf.append(ch);
                    break;
            }
        }
        return resultStrBuf.toString();
    }

    /**
   * A path name delivered by function xnode-path (with special strings such as 
   * "@", "[", "]", " eq ") is converted to an XML String with xml tags, 
   * opened or closed such as the mode says
   * @param pathName delivered by function xnode-path: Example: /archimedes[@xmlns:xlink eq "http://www.w3.org/1999/xlink"]/text/body/chap/p[@type eq "main"]/s/foreign[@lang eq "en"]
   * @param mode open or close
   * @return xml tags opened or closed
   */
    private String pathName2XmlTags(String pathName, String mode) {
        String result = "";
        ArrayList<String> elements = pathName2ElementsWithAttributes(pathName);
        if (mode.equals("open")) {
            for (int i = 0; i < elements.size(); i++) {
                String element = elements.get(i);
                element = element.replaceAll("\\[", " ");
                element = element.replaceAll(" eq ", "=");
                element = element.replaceAll("@", "");
                element = element.replaceAll("\\]", "");
                if (!(element.length() == 0)) result += "<" + element + ">\n";
            }
        } else if (mode.equals("close")) {
            for (int i = elements.size() - 1; i >= 0; i--) {
                String element = elements.get(i);
                element = element.replaceAll("\\[[^\\]]*\\]", "");
                if (!(element.length() == 0)) result += "</" + element + ">\n";
            }
        }
        return result;
    }

    private ArrayList<String> pathName2ElementsWithAttributes(String pathName) {
        ArrayList<String> result = new ArrayList<String>();
        if (pathName.charAt(0) == '/') pathName = pathName.substring(1, pathName.length());
        String regExpr = "[a-zA-Z0-9:]+?\\[.+?\\]/" + "|" + "[a-zA-Z0-9:]+?/" + "|" + "[a-zA-Z0-9:]+?\\[.+\\]$" + "|" + "[a-zA-Z0-9:]+?$";
        Pattern p = Pattern.compile(regExpr, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher m = p.matcher(pathName);
        while (m.find()) {
            int msBeginPos = m.start();
            int msEndPos = m.end();
            String elementName = pathName.substring(msBeginPos, msEndPos);
            int elemNameSize = elementName.length();
            if (elemNameSize > 0 && elementName.charAt(elemNameSize - 1) == '/') elementName = elementName.substring(0, elemNameSize - 1);
            result.add(elementName);
        }
        return result;
    }

    private String getNodeXPath(Node n, boolean setRootNamespace) {
        if (n.getNodeType() == Node.DOCUMENT_NODE) return "/";
        StringBuilder buf = new StringBuilder(nodeToXPath(n, setRootNamespace));
        while ((n = n.getParentNode()) != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                buf.insert(0, nodeToXPath(n, setRootNamespace));
            }
        }
        return buf.toString();
    }

    /**
   * Creates an XPath for a Node
   * The nodes attribute's become predicates
   * 
   * @param n The Node to generate an XPath for
   * @return StringBuilder containing the XPath
   */
    private StringBuilder nodeToXPath(Node n, boolean setRootNamespace) {
        StringBuilder xpath = new StringBuilder("/" + getFullNodeName(n));
        if (setRootNamespace) {
            Node parentNode = n.getParentNode();
            short parentNodeType = parentNode.getNodeType();
            if (parentNodeType == Node.DOCUMENT_NODE) {
                String nsUri = n.getNamespaceURI();
                if (nsUri != null) {
                    xpath.append("[@" + "xmlns" + " eq \"" + nsUri + "\"]");
                }
            }
        }
        NamedNodeMap attrs = n.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            String fullNodeName = getFullNodeName(attr);
            String attrNodeValue = attr.getNodeValue();
            if (!fullNodeName.equals("") && (!(fullNodeName == null))) xpath.append("[@" + fullNodeName + " eq \"" + attrNodeValue + "\"]");
        }
        return xpath;
    }

    /**
   * Returns the full node name including the prefix if present
   * 
   * @param n The node to get the name for
   * @return The full name of the node
   */
    private String getFullNodeName(Node n) {
        String prefix = n.getPrefix();
        String localName = n.getLocalName();
        if (prefix == null || prefix.equals("")) {
            if (localName == null || localName.equals("")) return ""; else return localName;
        } else {
            if (localName == null || localName.equals("")) return ""; else return prefix + ":" + localName;
        }
    }
}

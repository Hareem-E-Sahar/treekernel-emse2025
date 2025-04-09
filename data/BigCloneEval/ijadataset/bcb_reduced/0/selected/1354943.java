package org.newsml.toolkit.dom;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.newsml.toolkit.AssignedFormalName;
import org.newsml.toolkit.AssignedOriginText;
import org.newsml.toolkit.AssignmentNode;
import org.newsml.toolkit.BaseNode;
import org.newsml.toolkit.BasisForChoice;
import org.newsml.toolkit.Catalog;
import org.newsml.toolkit.CatalogNode;
import org.newsml.toolkit.Comment;
import org.newsml.toolkit.CommentNode;
import org.newsml.toolkit.FormalName;
import org.newsml.toolkit.HrefNode;
import org.newsml.toolkit.IdNode;
import org.newsml.toolkit.LanguageNode;
import org.newsml.toolkit.NewsComponent;
import org.newsml.toolkit.NewsMLException;
import org.newsml.toolkit.NewsMLSession;
import org.newsml.toolkit.OriginText;
import org.newsml.toolkit.Party;
import org.newsml.toolkit.PartyList;
import org.newsml.toolkit.Property;
import org.newsml.toolkit.PropertyNode;
import org.newsml.toolkit.RevisionHistory;
import org.newsml.toolkit.Text;
import org.newsml.toolkit.Topic;
import org.newsml.toolkit.TopicNode;
import org.newsml.toolkit.TopicOccurrence;

/**
 * Base class for all NewsML objects.
 *
 * <p>This base class provides access to the underlying DOM node,
 * including a series of convenience methods based on the {@link
 * DOMUtils} class.  The base class also contains an {@link #equals}
 * method that compares the underlying DOM nodes rather than the
 * wrapper classes.</p>
 *
 * <p>Additionally, since Java does not allow multiple inheritance,
 * this class provides default implementations for all of the methods
 * declared in the common *Node interfaces (except for OriginNode).
 * The interfaces will hide unused methods from the user.</p>
 *
 * <p>Finally, this class provides convenience methods for accessing
 * the underlying DOM tree.</p>
 *
 * @author Reuters PLC
 * @version 2.0
 */
class DOMBaseNode implements AssignmentNode, BaseNode, CatalogNode, CommentNode, HrefNode, IdNode, LanguageNode, PropertyNode, RevisionHistory, TopicNode, TopicOccurrence {

    /**
     * Constructor.
     *
     * @param session The NewsML session for the package.
     */
    protected DOMBaseNode(Node node, DOMSessionCore session) {
        if (node == null) throw new NullPointerException("null DOM Element node");
        this.node = node;
        this.session = session;
    }

    /**
     * @see AssignmentNode#getAssignedBy()
     */
    public Text getAssignedBy() {
        return getAttr("@AssignedBy");
    }

    /**
     * @see AssignmentNode#getImportance()
     */
    public FormalName getImportance() {
        return new DOMFormalName(node, session, "Importance");
    }

    /**
     * @see AssignmentNode#getConfidence()
     */
    public FormalName getConfidence() {
        return new DOMFormalName(node, session, "Confidence");
    }

    /**
     * @see AssignmentNode#getHowPresent()
     */
    public FormalName getHowPresent() {
        return new DOMFormalName(node, session, "HowPresent");
    }

    /**
     * @see AssignmentNode#getAssignmentDateAndTime()
     */
    public Text getAssignmentDateAndTime() {
        return getAttr("@DateAndTime");
    }

    /**
     * @see BaseNode#getXMLName
     */
    public String getXMLName() {
        switch(node.getNodeType()) {
            case Node.ELEMENT_NODE:
                return node.getNodeName();
            case Node.ATTRIBUTE_NODE:
                return "@" + node.getNodeName();
            default:
                throw new RuntimeException("Illegal DOM node type");
        }
    }

    /**
     * @see BaseNode#getXPath
     */
    public String getXPath() {
        String name = getXMLName();
        int index = -1;
        StringBuffer buf = new StringBuffer();
        BaseNode parent = getParent();
        if (parent != null) {
            buf.append(parent.getXPath());
            if (!name.startsWith("@")) {
                int nChildren = parent.getChildCount(name);
                for (int i = 0; i < nChildren; i++) {
                    if (equals(parent.getChild(name, i))) {
                        index = i;
                        break;
                    }
                }
            }
        }
        buf.append('/');
        buf.append(getXMLName());
        if (index != -1) {
            buf.append('[');
            buf.append(index);
            buf.append(']');
        }
        return buf.toString();
    }

    /**
     * @see BaseNode#getSession
     */
    public NewsMLSession getSession() {
        return new DOMNewsMLSession(this, session);
    }

    /**
     * @see BaseNode#getParent()
     */
    public BaseNode getParent() {
        Node parent;
        switch(node.getNodeType()) {
            case Node.ELEMENT_NODE:
                return session.makeNewsMLNode(node.getParentNode());
            case Node.ATTRIBUTE_NODE:
                return session.makeNewsMLNode(((Attr) node).getOwnerElement());
            default:
                throw new RuntimeException("Illegal DOM node type");
        }
    }

    /**
     * @see BaseNode#getChildCount()
     */
    public int getChildCount() {
        int count = 0;
        while (getChild(count) != null) count++;
        return count;
    }

    /**
     * @see BaseNode#getChildCount(String)
     */
    public int getChildCount(String xmlName) {
        int nChildren = getChildCount();
        int matches = 0;
        for (int i = 0; i < nChildren; i++) {
            BaseNode child = getChild(i);
            if (child != null && child.getXMLName().equals(xmlName)) matches++;
        }
        return matches;
    }

    /**
     * @see BaseNode#getChild(int)
     */
    public BaseNode getChild(int index) {
        if (index == -1) index = getChildCount() - 1;
        NodeList domChildren = node.getChildNodes();
        int nDOMChildren = domChildren.getLength();
        for (int i = 0; i < nDOMChildren; i++) {
            Node node = domChildren.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                BaseNode result = session.makeNewsMLNode(node);
                if (result != null) {
                    if (index <= 0) return result; else index--;
                }
            }
        }
        return null;
    }

    /**
     * @see BaseNode#getChild(String, int)
     */
    public BaseNode getChild(String xmlName, int index) {
        int pos = findChildIndex(xmlName, index);
        if (pos == -1) return null; else return getChild(pos);
    }

    /**
     * @see BaseNode#getChild()
     */
    public BaseNode[] getChild() {
        int count = getChildCount();
        BaseNode ret[] = new BaseNode[count];
        for (int i = 0; i < count; i++) ret[i] = getChild(i);
        return ret;
    }

    /**
     * @see BaseNode#getChild()
     */
    public BaseNode[] getChild(String xmlName) {
        int count = getChildCount(xmlName);
        BaseNode ret[] = new BaseNode[count];
        for (int i = 0; i < count; i++) ret[i] = getChild(xmlName, i);
        return ret;
    }

    /**
     * @see BaseNode#insertChild(int,BaseNode)
     */
    public BaseNode insertChild(int index, BaseNode child) {
        DOMBaseNode dChild = (DOMBaseNode) child;
        adoptBaseNode(dChild);
        DOMUtils.insertChild(index, node, dChild.getDOMNode());
        return child;
    }

    /**
     * @see BaseNode#insertChild(int,BaseNode[])
     */
    public BaseNode[] insertChild(int index, BaseNode children[]) {
        if (index == -1) {
            for (int i = 0; i < children.length; i++) insertChild(-1, children[i]);
        } else {
            for (int i = children.length - 1; i >= 0; i--) insertChild(index, children[i]);
        }
        return children;
    }

    /**
     * @see BaseNode#insertFirst(BaseNode)
     */
    public BaseNode insertFirst(BaseNode child) {
        return insertChild(0, child);
    }

    /**
     * @see BaseNode#insertFirst(BaseNode[])
     */
    public BaseNode[] insertFirst(BaseNode children[]) {
        return insertChild(0, children);
    }

    /**
     * @see BaseNode#insertLast(BaseNode)
     */
    public BaseNode insertLast(BaseNode child) {
        return insertChild(-1, child);
    }

    /**
     * @see BaseNode#insertLast(BaseNode[])
     */
    public BaseNode[] insertLast(BaseNode children[]) {
        return insertChild(-1, children);
    }

    /**
     * @see BaseNode#insertBefore(String,int,BaseNode)
     */
    public BaseNode insertBefore(String xmlName, int index, BaseNode child) {
        int pos = findChildIndex(xmlName, index);
        if (pos == -1) return null; else return insertChild(pos, child);
    }

    /**
     * @see BaseNode#insertBefore(String,BaseNode)
     */
    public BaseNode insertBefore(String xmlName, BaseNode child) {
        return insertBefore(xmlName, 0, child);
    }

    /**
     * @see BaseNode#insertBefore(String,int,BaseNode[])
     */
    public BaseNode[] insertBefore(String xmlName, int index, BaseNode children[]) {
        int pos = findChildIndex(xmlName, index);
        if (pos == -1) return null; else return insertChild(pos, children);
    }

    /**
     * @see BaseNode#insertBefore(String,BaseNode[])
     */
    public BaseNode[] insertBefore(String xmlName, BaseNode children[]) {
        return insertBefore(xmlName, 0, children);
    }

    /**
     * @see BaseNode#insertBeforeDuid(String,BaseNode)
     */
    public BaseNode insertBeforeDuid(String duid, BaseNode child) {
        int pos = findChildIndexByDuid(duid);
        if (pos == -1) return null; else if (pos == getChildCount()) return insertChild(-1, child); else return insertChild(pos, child);
    }

    /**
     * @see BaseNode#insertBeforeDuid(String,BaseNode)
     */
    public BaseNode[] insertBeforeDuid(String duid, BaseNode children[]) {
        int pos = findChildIndexByDuid(duid);
        if (pos == -1) return null; else if (pos == getChildCount()) return insertChild(-1, children); else return insertChild(pos, children);
    }

    /**
     * @see BaseNode#insertAfter(String,int,BaseNode)
     */
    public BaseNode insertAfter(String xmlName, int index, BaseNode child) {
        int pos = findChildIndex(xmlName, index);
        if (pos == -1) return null; else if (pos == getChildCount()) return insertChild(-1, child); else return insertChild(pos + 1, child);
    }

    /**
     * @see BaseNode#insertAfter(String,BaseNode)
     */
    public BaseNode insertAfter(String xmlName, BaseNode child) {
        return insertAfter(xmlName, -1, child);
    }

    /**
     * @see BaseNode#insertAfter(String,int,BaseNode[])
     */
    public BaseNode[] insertAfter(String xmlName, int index, BaseNode children[]) {
        int pos = findChildIndex(xmlName, index);
        if (pos == -1) return null; else return insertChild(pos + 1, children);
    }

    /**
     * @see BaseNode#insertAfter(String,BaseNode[])
     */
    public BaseNode[] insertAfter(String xmlName, BaseNode children[]) {
        return insertAfter(xmlName, 0, children);
    }

    /**
     * @see BaseNode#insertAfterDuid(String,BaseNode)
     */
    public BaseNode insertAfterDuid(String duid, BaseNode child) {
        int pos = findChildIndexByDuid(duid);
        if (pos == -1) return null; else if (pos == getChildCount()) return insertChild(-1, child); else return insertChild(pos + 1, child);
    }

    /**
     * @see BaseNode#insertAfterDuid(String,BaseNode[])
     */
    public BaseNode[] insertAfterDuid(String duid, BaseNode children[]) {
        int pos = findChildIndexByDuid(duid);
        if (pos == -1) return null; else if (pos == getChildCount()) return insertChild(-1, children); else return insertChild(pos + 1, children);
    }

    /**
     * @see BaseNode#replaceChild
     */
    public BaseNode replaceChild(int index, BaseNode child) {
        BaseNode oldChild = removeChild(index);
        insertChild(index, child);
        return oldChild;
    }

    /**
     * @see BaseNode#replaceChild
     */
    public BaseNode replaceChild(int index, BaseNode children[]) {
        BaseNode oldChild = removeChild(index);
        insertChild(index, children);
        return oldChild;
    }

    /**
     * @see BaseNode#replaceChild(String,int,BaseNode)
     */
    public BaseNode replaceChild(String xmlName, int index, BaseNode child) {
        int pos = findChildIndex(xmlName, index);
        if (pos == -1) return null; else return replaceChild(pos, child);
    }

    /**
     * @see BaseNode#replaceChild(String,BaseNode)
     */
    public BaseNode replaceChild(String xmlName, BaseNode child) {
        return replaceChild(xmlName, 0, child);
    }

    /**
     * @see BaseNode#replaceChild(String,int,BaseNode[])
     */
    public BaseNode replaceChild(String xmlName, int index, BaseNode children[]) {
        int pos = findChildIndex(xmlName, index);
        if (pos == -1) return null; else return replaceChild(pos, children);
    }

    /**
     * @see BaseNode#replaceChild(String,BaseNode[])
     */
    public BaseNode replaceChild(String xmlName, BaseNode children[]) {
        return replaceChild(xmlName, 0, children);
    }

    /**
     * @see BaseNode#removeChild
     */
    public BaseNode removeChild(int index) {
        BaseNode oldChild = getChild(index);
        if (index == -1) index = getChildCount() - 1;
        try {
            DOMUtils.removeChild(index, node);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        return oldChild;
    }

    /**
     * @see BaseNode#removeChild(String,int,BaseNode)
     */
    public BaseNode removeChild(String xmlName, int index) {
        int pos = findChildIndex(xmlName, index);
        if (pos == -1) return null; else return removeChild(pos);
    }

    /**
     * @see BaseNode#removeSelf
     */
    public void removeSelf() throws NewsMLException {
        if (getParent() == null) throw new NewsMLException("Cannot remove root node " + getXMLName());
        switch(node.getNodeType()) {
            case Node.ELEMENT_NODE:
                try {
                    DOMUtils.removeNode(node);
                } catch (DOMException e) {
                    throw new NewsMLException(e);
                }
                return;
            case Node.ATTRIBUTE_NODE:
                throw new NewsMLException("Removing attributes not supported");
            default:
                throw new RuntimeException("Bad DOM node type.");
        }
    }

    /**
     * @see BaseNode#getAttr(String)
     */
    public Text getAttr(String xmlName) {
        if (xmlName.startsWith("@")) return ((Text) session.makeNewsMLNode(getDOMAttribute(xmlName.substring(1)))); else return null;
    }

    /**
     * @see BaseNode#setAttr(Text)
     */
    public Text setAttr(Text attr) {
        DOMBaseNode dAttr = (DOMBaseNode) attr;
        adoptBaseNode(dAttr);
        Node anode = dAttr.getDOMNode();
        if (anode.getNodeType() == Node.ATTRIBUTE_NODE) {
            Text oldAttr = getAttr(attr.getXMLName());
            setDOMAttribute((Attr) anode);
            return oldAttr;
        } else {
            return null;
        }
    }

    /**
     * @see BaseNode#setAttr(String,String)
     */
    public Text setAttr(String name, String value) {
        Text attr = getAttr(name);
        if (attr == null) {
            try {
                attr = (Text) getSession().getFactory().createNewNodeAttr(name);
            } catch (IOException e) {
                return null;
            }
        }
        if (attr == null) {
            return null;
        } else {
            attr.setString(value);
            setAttr(attr);
            return attr;
        }
    }

    /**
     * @see BaseNode#unsetAttr(Text)
     */
    public Text unsetAttr(Text attr) {
        try {
            removeDOMAttribute(attr.getXMLName().substring(1));
        } catch (DOMException e) {
            return null;
        }
        return attr;
    }

    /**
     * @see BaseNode#unsetAttr(String)
     */
    public Text unsetAttr(String name) {
        Text attr = getAttr(name);
        if (attr != null) return unsetAttr(attr); else return null;
    }

    /**
     * @see BaseNode#getNodePath
     */
    public BaseNode[] getPath() {
        int count = 0;
        BaseNode node = this;
        while (node != null) {
            count++;
            node = node.getParent();
        }
        BaseNode ret[] = new BaseNode[count];
        node = this;
        for (int i = count - 1; i >= 0; i--) {
            ret[i] = node;
            node = node.getParent();
        }
        return ret;
    }

    /**
     * @see BaseNode#writeXML(Writer,boolean)
     */
    public void writeXML(Writer output, boolean isDocument) throws IOException {
        DOMUtils.writeNode(node, output, isDocument, null, null);
    }

    /**
     * @see BaseNode#writeXML(Writer,String,String)
     */
    public void writeXML(Writer output, String encoding, String internalSubset) throws IOException {
        DOMUtils.writeNode(node, output, true, encoding, internalSubset);
    }

    /**
     * @see BaseNode#toXML(boolean)
     */
    public String toXML(boolean isDocument) {
        StringWriter writer = new StringWriter();
        try {
            writeXML(writer, isDocument);
        } catch (IOException e) {
        }
        return writer.toString();
    }

    /**
     * @see BaseNode#toXML(String,String)
     */
    public String toXML(String encoding, String internalSubset) {
        StringWriter writer = new StringWriter();
        try {
            writeXML(writer, encoding, internalSubset);
        } catch (IOException e) {
        }
        return writer.toString();
    }

    /**
     * @see CatalogNode#getCatalog()
     */
    public Catalog getCatalog() {
        return (Catalog) getChild("Catalog", 0);
    }

    /**
     * @see CommentNode#getCommentCount
     */
    public int getCommentCount() {
        return getChildCount("Comment");
    }

    /**
     * @see CommentNode#getComment(int)
     */
    public Comment getComment(int index) {
        return (Comment) getChild("Comment", index);
    }

    /**
     * @see CommentNode#getComment()
     */
    public Comment[] getComment() {
        int len = getCommentCount();
        Comment ret[] = new Comment[len];
        for (int i = 0; i < len; i++) ret[i] = getComment(i);
        return ret;
    }

    /**
     * @see EquivalentNode#getBasisForChoiceNodes.
     */
    public BaseNode[] getBasisForChoiceNodes() throws NewsMLException {
        BasisForChoice bases[] = ((NewsComponent) getParent()).getRankedBasisForChoice();
        BaseNode result[] = new BaseNode[bases.length];
        for (int i = 0; i < bases.length; i++) {
            BaseNode matches[] = session.getNodesByXPath(this, bases[i].toString());
            if (matches.length > 0) result[i] = matches[0];
        }
        return result;
    }

    /**
     * @see HrefNode#getHref()
     */
    public Text getHref() {
        return getAttr("@Href");
    }

    /**
     * @see IdNode#getDuid()
     */
    public Text getDuid() {
        return getAttr("@Duid");
    }

    /**
     * @see IdNode#getEuid()
     */
    public Text getEuid() {
        return getAttr("@Euid");
    }

    /**
     * @see LanguageNode#getLang()
     */
    public Text getLang() {
        return getAttr("@xml:lang");
    }

    /**
     * @see PropertyNode#getPropertyCount()
     */
    public int getPropertyCount() {
        return getChildCount("Property");
    }

    /**
     * @see PropertyNode#getProperty(int)
     */
    public Property getProperty(int index) {
        return (Property) getChild("Property", index);
    }

    /**
     * @see PropertyNode#getProperty()
     */
    public Property[] getProperty() {
        int len = getPropertyCount();
        Property ret[] = new Property[len];
        for (int i = 0; i < len; i++) ret[i] = getProperty(i);
        return ret;
    }

    /**
     * @see TopicNode#getTopicRef
     */
    public Text getTopicRef() {
        return getAttr("@Topic");
    }

    /**
     * @see TopicNode#getReferencedTopic
     */
    public Topic getReferencedTopic(boolean useExternal) throws IOException {
        BaseNode node = null;
        Text ref = getTopicRef();
        if (ref != null) node = session.findReference(this, ref.toString(), useExternal);
        if (node != null) {
            if (!"Topic".equals(node.getXMLName())) throw new IOException("Topic reference points to " + node.getXMLName()); else return (Topic) node;
        }
        return null;
    }

    /**
     * Set the core session object.
     *
     * <p>The {@link #getSession()} method creates a wrapper
     * around this whenever it is invoked.</p>
     *
     * @param session The core session.
     */
    void setSession(DOMSessionCore session) {
        this.session = session;
    }

    /**
     * Get the underlying DOM node for this NewsML object.
     *
     * The underlying node allows users to query information not
     * available directly through this library.
     *
     * @return The underlying DOM node (not null).
     */
    protected Node getDOMNode() {
        return node;
    }

    /**
     * Set the underlying DOM node for this NewsML object.
     *
     * @param node The new DOM node.
     */
    protected void setDOMNode(Node node) {
        this.node = node;
    }

    /**
     * Get the text content of the current element.
     *
     * Return all of the text in the current element and its descendants.
     *
     * @return The text as a string.
     * @see DOMUtils#getText(org.w3c.dom.Node)
     */
    protected String getDOMContent() {
        switch(node.getNodeType()) {
            case Node.ELEMENT_NODE:
                return DOMUtils.getText(node);
            case Node.ATTRIBUTE_NODE:
                return node.getNodeValue();
            default:
                throw new RuntimeException("Bad DOM node type.");
        }
    }

    /**
     * Set the text of the current element.
     *
     * Any current child nodes will be lost.
     *
     * @see DOMUtils#setText(org.w3c.dom.Node)
     */
    protected void setDOMContent(String content) {
        switch(node.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                node.setNodeValue(content);
                return;
            case Node.ELEMENT_NODE:
                DOMUtils.clearChildren(node);
                Node child = node.getOwnerDocument().createTextNode(content);
                node.appendChild(child);
                return;
            default:
                throw new RuntimeException("Internal error: wrong node type");
        }
    }

    /**
     * Get the DOM node for an attribute.
     *
     * @return The DOM node or null.
     */
    protected Node getDOMAttribute(String name) {
        NamedNodeMap atts = node.getAttributes();
        return (atts == null ? null : atts.getNamedItem(name));
    }

    /**
     * Add a new DOM node for an attribute.
     *
     * @param node The node for the attribute.
     */
    protected void setDOMAttribute(Attr attribute) {
        if (node.getNodeType() != Node.ELEMENT_NODE) return;
        Node parent = attribute.getOwnerElement();
        if (parent != null) parent.getAttributes().removeNamedItem(attribute.getNodeName());
        node.getAttributes().setNamedItem(attribute);
    }

    /**
     * Remove the DOM node for an attribute.
     *
     * @param name The name of the attribute to remove.
     */
    protected void removeDOMAttribute(String name) {
        NamedNodeMap atts = node.getAttributes();
        if (atts != null) atts.removeNamedItem(name);
    }

    /**
     * Test whether two NewsML objects are equal.
     *
     * Two NewsML objects are equal if they share the same subclass
     * and point to the same underlying DOM node.
     *
     * @param o The object to compare.
     * @return true if the objects are equal, false otherwise.
     */
    public boolean equals(Object o) {
        boolean retval;
        if (o == null) {
            retval = false;
        } else if (o == this) {
            retval = true;
        } else if (o instanceof DOMBaseNode) {
            retval = (getClass().equals(o.getClass()) && getDOMNode().equals(((DOMBaseNode) o).getDOMNode()));
        } else {
            retval = false;
        }
        return retval;
    }

    public String toString() {
        return getXMLName();
    }

    public int hashCode() {
        return node.hashCode();
    }

    /**
     * Ensure that a node belongs to this implementation.
     */
    private void adoptBaseNode(DOMBaseNode target) {
        Node domNode = target.getDOMNode();
        Document document = node.getOwnerDocument();
        if (!document.equals(domNode.getOwnerDocument())) {
            Node copy = document.importNode(domNode, true);
            target.setDOMNode(copy);
            target.setSession(session);
        }
    }

    /**
     * Find the index of a named child.
     *
     * @param name The name of the child.
     * @param index The index of the child among others with the same
     * name.
     * @return The absolute index, or -1 if not found.
     */
    private int findChildIndex(String xmlName, int index) {
        int nChildren = getChildCount();
        int matches = 0;
        if (index == -1) index = getChildCount(xmlName) - 1;
        for (int i = 0; i < nChildren; i++) {
            BaseNode child = getChild(i);
            if (child != null && xmlName.equals(child.getXMLName())) {
                if (index == matches) return i; else matches++;
            }
        }
        return -1;
    }

    private int findChildIndexByDuid(String duid) {
        int nChildren = getChildCount();
        for (int i = 0; i < nChildren; i++) {
            BaseNode child = getChild(i);
            Text attr = child.getAttr("@Duid");
            if (attr != null && duid.equals(attr.toString())) return i;
        }
        return -1;
    }

    private Node node;

    protected DOMSessionCore session;
}

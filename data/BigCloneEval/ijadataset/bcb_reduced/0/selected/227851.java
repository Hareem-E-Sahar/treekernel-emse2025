package com.jkn.db;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

/**
 * $Id: ClobXMLPanel.java,v 1.1.1.1 2005/04/29 17:05:28 jknelson Exp $
 * ClobXMLPanel  - panel to show xml text
 * 
 * @author jnelson
 *         Copyright (c) 2004
 *
 *
 *         <p/>
 *         THE ACCOMPANYING PROGRAM IS PROVIDED UNDER THE TERMS OF THIS COMMON PUBLIC LICENSE
 *         ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THE PROGRAM CONSTITUTES RECIPIENT'S
 *         ACCEPTANCE OF THIS AGREEMENT.
 *         <p/>
 *         THE PROGRAM IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *         EITHER EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR CONDITIONS OF TITLE,
 *         NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
 *
 *         See the Common Public License for more details.
 */
public class ClobXMLPanel extends JPanel {

    static Document document;

    boolean compress = false;

    static final int windowHeight = 200;

    static final int leftWidth = 200;

    static final int rightWidth = 200;

    static final int windowWidth = leftWidth + rightWidth;

    private static String[] connectOptionName = { "Close" };

    ;

    private static String connectTitle = "Clob Text";

    /**
    * panel to show xml text
    */
    public ClobXMLPanel(String sXML) {
        buildDom(sXML);
        init(sXML);
    }

    /**
      * panel to show xml text
     */
    public ClobXMLPanel() {
        init("");
    }

    /**
	 * initialize panel with xml string
	 * @param sXML xml text
	 */
    private void init(String sXML) {
        EmptyBorder eb = new EmptyBorder(5, 5, 5, 5);
        BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
        CompoundBorder cb = new CompoundBorder(eb, bb);
        this.setBorder(new CompoundBorder(cb, eb));
        JTree tree = new JTree(new DomToTreeModelAdapter());
        JScrollPane treeView = new JScrollPane(tree);
        treeView.setPreferredSize(new Dimension(leftWidth, windowHeight));
        final JEditorPane htmlPane = new JEditorPane("text", "");
        htmlPane.setEditable(false);
        JScrollPane htmlView = new JScrollPane(htmlPane);
        htmlView.setPreferredSize(new Dimension(rightWidth, windowHeight));
        tree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
            }
        });
        htmlPane.setText(sXML);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeView, htmlView);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerLocation(leftWidth);
        splitPane.setPreferredSize(new Dimension(windowWidth + 10, windowHeight + 10));
        this.setLayout(new BorderLayout());
        this.add("Center", splitPane);
    }

    /**
	 * get option name
	 * @return String[] the option name - CLOSE
	 */
    public String[] getOptionNames() {
        return connectOptionName;
    }

    /**
	 * get connection title
	 * @return String the connection title - TEXT
	 */
    public String getConnectionTitle() {
        return connectTitle;
    }

    /**
     * used for testing
     * @param argv
     */
    public static void main(String argv[]) {
        if (argv.length != 1) {
            buildDom();
            makeFrame();
            return;
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new File(argv[0]));
            makeFrame();
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

    public static void makeFrame() {
        JFrame frame = new JFrame("DOM Echo");
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        final ClobXMLPanel echoPanel = new ClobXMLPanel();
        frame.getContentPane().add("Center", echoPanel);
        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = windowWidth + 10;
        int h = windowHeight + 10;
        frame.setLocation(screenSize.width / 3 - w / 2, screenSize.height / 2 - h / 2);
        frame.setSize(w, h);
        frame.setVisible(true);
    }

    /**
     * used for testing
     * @param sXML
     */
    public void buildDom(String sXML) {
        Date currDate = new Date();
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("hh_MMM_d_yyyy");
        String dateStr = formatter.format(currDate);
        String fileName = new String("temp" + dateStr + ".xml");
        File newFile = null;
        try {
            newFile = new File(fileName);
            java.io.FileWriter fileOutput = new java.io.FileWriter(newFile);
            fileOutput.write(sXML);
            fileOutput.close();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                document = builder.parse(fileName);
            } catch (SAXException sxe) {
                Exception x = sxe;
                if (sxe.getException() != null) {
                    x = sxe.getException();
                    x.printStackTrace();
                }
            } catch (ParserConfigurationException pce) {
                pce.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                if (newFile.exists()) {
                    newFile.delete();
                }
            }
        } catch (Exception e2) {
            System.out.println("ERROR -- saving to temp file " + fileName + ". " + e2.getMessage() + ".");
        }
    }

    /**
	 * build dom
	 */
    public static void buildDom() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();
            Element root = (Element) document.createElement("rootElement");
            document.appendChild(root);
            root.appendChild(document.createTextNode("Some"));
            root.appendChild(document.createTextNode(" "));
            root.appendChild(document.createTextNode("text"));
            document.getDocumentElement().normalize();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
    }

    static final String[] typeName = { "none", "Element", "Attr", "Text", "CDATA", "EntityRef", "Entity", "ProcInstr", "Comment", "Document", "DocType", "DocFragment", "Notation" };

    static final int ELEMENT_TYPE = 1;

    static final int ATTR_TYPE = 2;

    static final int TEXT_TYPE = 3;

    static final int CDATA_TYPE = 4;

    static final int ENTITYREF_TYPE = 5;

    static final int ENTITY_TYPE = 6;

    static final int PROCINSTR_TYPE = 7;

    static final int COMMENT_TYPE = 8;

    static final int DOCUMENT_TYPE = 9;

    static final int DOCTYPE_TYPE = 10;

    static final int DOCFRAG_TYPE = 11;

    static final int NOTATION_TYPE = 12;

    static String[] treeElementNames = { "slideshow", "slide", "title", "slide-title", "item" };

    boolean treeElement(String elementName) {
        for (int i = 0; i < treeElementNames.length; i++) {
            if (elementName.equals(treeElementNames[i])) return true;
        }
        return false;
    }

    public class AdapterNode {

        org.w3c.dom.Node domNode;

        public AdapterNode(org.w3c.dom.Node node) {
            domNode = node;
        }

        public String toString() {
            String s = typeName[domNode.getNodeType()];
            String nodeName = domNode.getNodeName();
            if (!nodeName.startsWith("#")) {
                s += ": " + nodeName;
            }
            if (compress) {
                String t = content().trim();
                int x = t.indexOf("\n");
                if (x >= 0) t = t.substring(0, x);
                s += " " + t;
                return s;
            }
            if (domNode.getNodeValue() != null) {
                if (s.startsWith("ProcInstr")) s += ", "; else s += ": ";
                String t = domNode.getNodeValue().trim();
                int x = t.indexOf("\n");
                if (x >= 0) t = t.substring(0, x);
                s += t;
            }
            return s;
        }

        public String content() {
            String s = "";
            org.w3c.dom.NodeList nodeList = domNode.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node node = nodeList.item(i);
                int type = node.getNodeType();
                AdapterNode adpNode = new AdapterNode(node);
                if (type == ELEMENT_TYPE) {
                    if (treeElement(node.getNodeName())) continue;
                    s += "<" + node.getNodeName() + ">";
                    s += adpNode.content();
                    s += "</" + node.getNodeName() + ">";
                } else if (type == TEXT_TYPE) {
                    s += node.getNodeValue();
                } else if (type == ENTITYREF_TYPE) {
                    s += adpNode.content();
                } else if (type == CDATA_TYPE) {
                    StringBuffer sb = new StringBuffer(node.getNodeValue());
                    for (int j = 0; j < sb.length(); j++) {
                        if (sb.charAt(j) == '<') {
                            sb.setCharAt(j, '&');
                            sb.insert(j + 1, "lt;");
                            j += 3;
                        } else if (sb.charAt(j) == '&') {
                            sb.setCharAt(j, '&');
                            sb.insert(j + 1, "amp;");
                            j += 4;
                        }
                    }
                    s += "<pre>" + sb + "\n</pre>";
                }
            }
            return s;
        }

        public int index(AdapterNode child) {
            int count = childCount();
            for (int i = 0; i < count; i++) {
                AdapterNode n = this.child(i);
                if (child.domNode == n.domNode) return i;
            }
            return -1;
        }

        public AdapterNode child(int searchIndex) {
            org.w3c.dom.Node node = domNode.getChildNodes().item(searchIndex);
            if (compress) {
                int elementNodeIndex = 0;
                for (int i = 0; i < domNode.getChildNodes().getLength(); i++) {
                    node = domNode.getChildNodes().item(i);
                    if (node.getNodeType() == ELEMENT_TYPE && treeElement(node.getNodeName()) && elementNodeIndex++ == searchIndex) {
                        break;
                    }
                }
            }
            return new AdapterNode(node);
        }

        public int childCount() {
            if (!compress) {
                return domNode.getChildNodes().getLength();
            }
            int count = 0;
            for (int i = 0; i < domNode.getChildNodes().getLength(); i++) {
                org.w3c.dom.Node node = domNode.getChildNodes().item(i);
                if (node.getNodeType() == ELEMENT_TYPE && treeElement(node.getNodeName())) {
                    ++count;
                }
            }
            return count;
        }
    }

    public class DomToTreeModelAdapter implements javax.swing.tree.TreeModel {

        public Object getRoot() {
            return new AdapterNode(document);
        }

        public boolean isLeaf(Object aNode) {
            AdapterNode node = (AdapterNode) aNode;
            if (node.childCount() > 0) return false;
            return true;
        }

        public int getChildCount(Object parent) {
            AdapterNode node = (AdapterNode) parent;
            return node.childCount();
        }

        public Object getChild(Object parent, int index) {
            AdapterNode node = (AdapterNode) parent;
            return node.child(index);
        }

        public int getIndexOfChild(Object parent, Object child) {
            AdapterNode node = (AdapterNode) parent;
            return node.index((AdapterNode) child);
        }

        public void valueForPathChanged(TreePath path, Object newValue) {
        }

        private Vector listenerList = new Vector();

        public void addTreeModelListener(TreeModelListener listener) {
            if (listener != null && !listenerList.contains(listener)) {
                listenerList.addElement(listener);
            }
        }

        public void removeTreeModelListener(TreeModelListener listener) {
            if (listener != null) {
                listenerList.removeElement(listener);
            }
        }

        public void fireTreeNodesChanged(TreeModelEvent e) {
            Enumeration listeners = listenerList.elements();
            while (listeners.hasMoreElements()) {
                TreeModelListener listener = (TreeModelListener) listeners.nextElement();
                listener.treeNodesChanged(e);
            }
        }

        public void fireTreeNodesInserted(TreeModelEvent e) {
            Enumeration listeners = listenerList.elements();
            while (listeners.hasMoreElements()) {
                TreeModelListener listener = (TreeModelListener) listeners.nextElement();
                listener.treeNodesInserted(e);
            }
        }

        public void fireTreeNodesRemoved(TreeModelEvent e) {
            Enumeration listeners = listenerList.elements();
            while (listeners.hasMoreElements()) {
                TreeModelListener listener = (TreeModelListener) listeners.nextElement();
                listener.treeNodesRemoved(e);
            }
        }

        public void fireTreeStructureChanged(TreeModelEvent e) {
            Enumeration listeners = listenerList.elements();
            while (listeners.hasMoreElements()) {
                TreeModelListener listener = (TreeModelListener) listeners.nextElement();
                listener.treeStructureChanged(e);
            }
        }
    }
}

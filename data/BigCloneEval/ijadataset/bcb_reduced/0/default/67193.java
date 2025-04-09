import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import visumtree.*;
import java.io.*;
import org.apache.xerces.parsers.DOMParser;

public class XMLVisumTree implements TreeListener {

    boolean dirty = false;

    TreePanel treePanel;

    Tree tree;

    String term = "@@node.:terminal=true";

    String agr = "agr.num=sg|agr.gen=com";

    File document = null;

    final JFileChooser fc = new JFileChooser();

    JFrame parent;

    String base_title;

    SchemaWrapper schema;

    public XMLVisumTree(JFrame parent, SchemaWrapper schema, String title) {
        this.base_title = title;
        this.schema = schema;
        this.parent = parent;
        fc.setCurrentDirectory(new File("./Documents/"));
        treePanel = new TreePanel();
        tree = treePanel.tree();
        treePanel.setBorder(null);
        tree.addTreeListener(this);
        treePanel.getHorizontalScrollBar().setUnitIncrement(20);
        treePanel.getHorizontalScrollBar().setBlockIncrement(20);
        treePanel.getVerticalScrollBar().setUnitIncrement(20);
        treePanel.getVerticalScrollBar().setBlockIncrement(20);
        setupTree();
        setDirty(false, "init");
        addTreeWindowAdapter();
    }

    public TreePanel getPanel() {
        return treePanel;
    }

    class TreeWindowAdapter extends WindowAdapter {

        public void windowClosing(WindowEvent event) {
            System.exit(0);
        }
    }

    void addTreeWindowAdapter() {
        parent.addWindowListener(new TreeWindowAdapter());
    }

    public void treeActionPerformed(TreeEvent e) {
        switch(e.type()) {
            case TreeEvent.SELECT:
                Object o = tree.getAV(tree.selectedPath());
                if (o instanceof String) {
                    System.out.println("User selected " + tree.selectedPath() + ":" + (String) o);
                }
                break;
            case TreeEvent.DBLCLICK:
                if (newNode(true)) {
                    setDirty(true, "added new node");
                }
                break;
            case TreeEvent.NEWDAUGHTER:
            case TreeEvent.NEWMOTHER:
            case TreeEvent.MOVE:
            case TreeEvent.REORDER:
            case TreeEvent.REMOVE:
            case TreeEvent.CHANGE:
                setDirty(true, "" + e.type());
                break;
            case TreeEvent.LAYOUT:
                break;
            default:
                System.out.println("Received unknown tree event: " + e.type());
        }
    }

    public void OpenFile() {
        if (SaveIfDirty("Opening File")) {
            int rc = fc.showOpenDialog(parent);
            if (rc == JFileChooser.APPROVE_OPTION) {
                document = fc.getSelectedFile();
                System.out.println("I should be opening " + document.getPath());
                setupTree(document.getPath());
                setDirty(false, "opened new file");
            }
        }
    }

    public boolean SaveAndValidate() {
        if (SaveFile()) {
            if (SchemaValidator.validate(document.getPath(), schema.getFileName())) {
                JOptionPane.showMessageDialog(parent, document.getName() + " is valid.", "Validation Succeeded", JOptionPane.WARNING_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(parent, SchemaValidator.getErrorMsg(), "Validation Failed", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean SaveAs() {
        int rc = fc.showDialog(parent, "Save As");
        if (rc == JFileChooser.APPROVE_OPTION) {
            document = fc.getSelectedFile();
            if (!document.exists()) {
                return SaveFile();
            } else {
                int resp = JOptionPane.showConfirmDialog(parent, "File exists; overwrite it?", "Overwrite File?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (resp == JOptionPane.YES_OPTION) {
                    return SaveFile();
                }
                document = null;
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean SaveFile() {
        if (document == null) {
            return SaveAs();
        } else {
            try {
                FileWriter out = new FileWriter(document);
                StringBuffer xml = toXML();
                if (xml == null) {
                    return false;
                }
                out.write(xml.toString());
                out.close();
            } catch (IOException e) {
                System.out.println("Save to " + document + " failed.");
                e.printStackTrace();
                return false;
            }
            setDirty(false, "saved file");
            return true;
        }
    }

    public void New() {
        if (SaveIfDirty("Creating New File")) {
            fc.setSelectedFile(null);
            document = null;
            setupTree();
            setDirty(false, "created new file");
        }
    }

    public void Quit() {
        if (SaveIfDirty("Quitting")) {
            System.exit(0);
        }
    }

    public boolean SaveIfDirty(String action) {
        if (!dirty) {
            return true;
        } else {
            int rc = JOptionPane.showConfirmDialog(parent, "Save Changes?", action, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            switch(rc) {
                case JOptionPane.YES_OPTION:
                    return SaveFile();
                case JOptionPane.NO_OPTION:
                    return true;
                case JOptionPane.CANCEL_OPTION:
                    return false;
                default:
                    System.out.println("Unknown choice: " + rc);
            }
            return false;
        }
    }

    void setupTree() {
        String basicdoc = "Documents/skel.xml";
        setupTree(basicdoc);
    }

    void setupTree(String xml) {
        System.out.println("Regenerating tree...");
        parent.setTitle(base_title);
        tree.resetNodes();
        tree.resetInfo();
        tree.setOption("tree.layoutStyle", new Integer(1));
        tree.setOption("avm.tooltip", "${} is ${${}}");
        tree.setOption("macro.selectAV.attrBG", Color.yellow);
        tree.setOption("macro.selectAV.valBG", Color.yellow);
        tree.setOption("macro.selectNode.nodeBG", Color.cyan);
        tree.setOption("node.abbrMacro", "${cat}");
        tree.setOption("node.PERMremove", Boolean.TRUE);
        tree.setOption("node.PERMndaughter", Boolean.TRUE);
        tree.setOption("node.PERMterminal", Boolean.TRUE);
        tree.setOption("macro.terminal.abbrMacro", "${lex}");
        tree.setOption("macro.terminal.PERMchange", Boolean.TRUE);
        tree.setOption("macro.terminal.PERMremove", Boolean.TRUE);
        tree.setOption("macro.terminal.PERMreceive", Boolean.FALSE);
        tree.setOption("macro.terminal.PERMreorder", Boolean.TRUE);
        tree.setOption("macro.terminal.PERMterminal", Boolean.TRUE);
        tree.setOption("macro.terminal.PERMabbreviate", Boolean.FALSE);
        tree.setOption("macro.terminal.abbreviate", Boolean.TRUE);
        if (!XMLToTree(tree.root(), xml)) {
            tree.root().newDaughter(new Node("cat=s"));
        }
        tree.setOption("node.PERMabbreviate", Boolean.FALSE);
        tree.setOption("node.abbreviate", Boolean.TRUE);
    }

    public boolean XMLToTree(Node root, String xml) {
        org.w3c.dom.Document doc;
        try {
            DOMParser parser = new DOMParser();
            parser.parse(xml);
            doc = parser.getDocument();
        } catch (Exception e) {
            System.out.println("Exception parsing XML string.");
            e.printStackTrace();
            return false;
        }
        org.w3c.dom.Element e = doc.getDocumentElement();
        if (e != null) {
            addDaughters(tree.root(), e);
        }
        return true;
    }

    public void addDaughters(Node n, org.w3c.dom.Node xn) {
        Node next;
        n.newDaughter(next = new Node("cat=" + xn.getNodeName()));
        org.w3c.dom.NodeList nl = xn.getChildNodes();
        int daughters = 0;
        if (nl.getLength() != 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i) instanceof org.w3c.dom.Element) {
                    daughters++;
                    addDaughters(next, nl.item(i));
                }
            }
        }
        if (daughters == 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                org.w3c.dom.Node curr = nl.item(i);
                if (curr instanceof org.w3c.dom.CharacterData) {
                    try {
                        String lex = ((org.w3c.dom.CharacterData) curr).getData();
                        next.newDaughter(new Node("lex=" + lex + "|" + term));
                    } catch (org.w3c.dom.DOMException e) {
                        System.out.println("Element " + xn.getNodeName() + " has no children or text");
                    }
                }
            }
        }
    }

    public boolean editAV() {
        if (tree.selectedPath() == null) {
            JOptionPane.showMessageDialog(parent, "Please click a node first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        Object o = tree.getAV(tree.selectedPath());
        if (o instanceof String || o == null) {
            String curr = (String) o;
            String choice = null;
            if (tree.selectedPath().equals("lex")) {
                choice = (String) JOptionPane.showInputDialog(parent, "Word", "Edit Word", JOptionPane.QUESTION_MESSAGE, null, null, curr);
            } else if (tree.selectedPath().equals("cat")) {
                String mother_type = (String) tree.selected()[0].mother().getAV("cat");
                Object[] valid_subnodes = schema.getValidSubNodes(mother_type);
                if (valid_subnodes == null || valid_subnodes.length == 1) {
                    JOptionPane.showMessageDialog(parent, "You have no other choices for " + "this node.", "Notice", JOptionPane.WARNING_MESSAGE);
                    return false;
                } else {
                    choice = (String) JOptionPane.showInputDialog(parent, "Please choose", "Change Marker Type", JOptionPane.QUESTION_MESSAGE, null, schema.getValidSubNodes(mother_type), curr);
                }
            }
            System.out.println("In editAV(), user supplied " + choice);
            if (choice == null) {
                return false;
            } else {
                tree.setAV(tree.selectedPath(), choice);
                setDirty(true, "edited node");
                return true;
            }
        } else {
            System.out.println(o + " is not a string, can't edit!");
            return false;
        }
    }

    public boolean newNode() {
        return newNode(false);
    }

    public boolean newNode(boolean allowBounceToEdit) {
        Node root = tree.root();
        if (root.next() == null) {
            System.out.println("No valid root element...");
            String nodetype = schema.getRootElement();
            if (nodetype != null) {
                Node newnode;
                root.newDaughter(newnode = new Node("cat=" + nodetype));
                tree.select(newnode, "cat");
                setDirty(true, "planted tree");
                return true;
            }
        }
        if (tree.selectedPath() == null) {
            JOptionPane.showMessageDialog(parent, "Please click a node before adding " + "a marker.", "Notice", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String mother_type = (String) tree.getAV(tree.selectedPath());
        Object[] node_types = schema.getValidSubNodes(mother_type);
        if (node_types == null || node_types.length == 0) {
            if (schema.isTypedTerminal(mother_type)) {
                return newWord();
            } else {
                if (allowBounceToEdit) {
                    System.out.println("Can't add a node, editing this one instead.");
                    return editAV();
                } else {
                    JOptionPane.showMessageDialog(parent, "You cannot add to this node.", "Notice", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            }
        }
        String choice;
        if (node_types.length == 1) {
            choice = (String) node_types[0];
        } else {
            choice = (String) JOptionPane.showInputDialog(parent, "Please choose", "New Marker", JOptionPane.QUESTION_MESSAGE, null, node_types, null);
        }
        if (choice == null) {
            System.out.println("User canceled.");
            return false;
        } else {
            Node newnode;
            tree.newDaughter(newnode = new Node("cat=" + choice));
            tree.select(newnode, "cat");
            setDirty(true, "added new node");
            return true;
        }
    }

    public boolean deleteNode() {
        if (tree.selectedPath() == null) {
            JOptionPane.showMessageDialog(parent, "Please click a node first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return false;
        } else {
            if (tree.selected()[0].size() > 0) {
                int resp = JOptionPane.showConfirmDialog(parent, "This node has children. Really delete it and " + "all its children?", "Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (resp == JOptionPane.YES_OPTION) {
                    removeBranch(tree.selected()[0]);
                    setDirty(true, "removed node and daughters");
                    tree.deselectAll();
                    return true;
                }
                return false;
            } else {
                if (tree.remove()) {
                    setDirty(true, "removed node");
                    return true;
                } else {
                    System.out.println("Couldn't delete selected node!");
                    return false;
                }
            }
        }
    }

    public boolean newWord() {
        String choice = (String) JOptionPane.showInputDialog(parent, "Word", "New Word", JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (choice != null) {
            Node newnode;
            tree.newDaughter(newnode = new Node("lex=" + choice + "|" + term));
            tree.select(newnode, "lex");
            return true;
        } else {
            System.out.println("User canceled.");
            return false;
        }
    }

    public void setDirty(boolean dirty, String reason) {
        String fn;
        if (document == null) {
            fn = "New Document";
        } else {
            fn = document.getName();
        }
        if (dirty) {
            System.out.println("Tree has changed: " + reason);
            parent.setTitle(base_title + ": " + fn + " (modified) ");
            this.dirty = true;
        } else {
            parent.setTitle(base_title + ": " + fn);
            this.dirty = false;
        }
    }

    /**
   * Generate an XML document from the tree.
   */
    public StringBuffer toXML() {
        Node root = tree.root();
        Node doc = root.next();
        Object o = doc.getAV("cat");
        if (o == null) {
            System.out.println("Root node is not the right type");
            return null;
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("<?xml version=\"1.0\"?>\n");
            sb.append("<" + (String) o + " ");
            sb.append("xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>\n");
            Node[] children = doc.daughters();
            for (int i = 0; i < children.length; i++) {
                digXML(children[i], sb, 0);
            }
            sb.append("</" + (String) o + ">\n");
            return sb;
        }
    }

    private void digXML(Node n, StringBuffer sb, int depth) {
        depth++;
        Node[] children = n.daughters();
        Object o = n.getAV("cat");
        if (o == null) {
            System.out.println("Encountered a null 'cat':" + n);
        } else {
            String e = (String) o;
            if (children.length == 0) {
                indentSB(sb, depth);
                sb.append("<" + e + "/>\n");
            } else {
                indentSB(sb, depth);
                sb.append("<" + e + ">\n");
                if (schema.isTypedTerminal(e)) {
                    if (children.length > 1) {
                        System.out.println("Found multiple children for " + e);
                    }
                    indentSB(sb, depth + 1);
                    sb.append(children[0].getAV("lex") + "\n");
                } else {
                    for (int i = 0; i < children.length; i++) {
                        digXML(children[i], sb, depth);
                    }
                }
                indentSB(sb, depth);
                sb.append("</" + e + ">\n");
            }
        }
        depth--;
    }

    private void indentSB(StringBuffer sb, int depth) {
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
    }

    public void Print() {
        PrintJob pjob = parent.getToolkit().getPrintJob(parent, "Printing Test", null);
        if (pjob != null) {
            Graphics pg = pjob.getGraphics();
            if (pg != null) {
                double marginx = 0.1;
                double marginy = 0.1;
                double fillx = 0.5;
                double filly = 0;
                Dimension size = pjob.getPageDimension();
                int fontsize = 10;
                Font oldfont = (Font) tree.getOption("tree.font");
                tree.setOption("tree.font", new Font("Helvetica", Font.PLAIN, fontsize));
                tree.printlayout(pg, 0, 0);
                Rectangle bbox = tree.getBBox();
                double scalew = ((double) 1 - 2 * marginx) * size.width / bbox.width;
                double scaleh = ((double) 1 - 2 * marginy) * size.height / bbox.height;
                double scale = min(1, min(scalew, scaleh));
                fontsize = (int) (scale * fontsize);
                if (fontsize < 1) fontsize = 1;
                tree.setOption("tree.font", new Font("Helvetica", Font.PLAIN, fontsize));
                tree.printlayout(pg, 0, 0);
                bbox = tree.getBBox();
                int xref = (int) (marginx * size.width + fillx * ((1 - 2 * marginx) * size.width - bbox.width));
                int yref = (int) (marginy * size.height + filly * ((1 - 2 * marginy) * size.height - bbox.height));
                tree.print(pg, xref, yref);
                pg.dispose();
                tree.setOption("tree.font", oldfont);
                tree.requestRepaintAll();
            }
            pjob.end();
        }
    }

    double min(double x, double y) {
        return (x < y) ? x : y;
    }

    void removeBranch(Node curr) {
        Node[] children = curr.daughters();
        for (int i = 0; i < children.length; i++) {
            removeBranch(children[i]);
        }
        tree.select(curr, "*");
        tree.remove();
    }
}

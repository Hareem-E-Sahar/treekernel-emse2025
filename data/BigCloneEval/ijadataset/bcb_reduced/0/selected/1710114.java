package org.form4j.form.field.nodedata.tree;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import org.apache.log4j.Logger;
import org.form4j.form.field.nodedata.Tree;
import org.form4j.form.main.Form;
import org.form4j.form.util.remove.Remove;
import org.form4j.form.util.remove.RemoveConfirmable;
import org.form4j.form.util.xml.XMLPrintHelper;
import org.form4j.form.util.xml.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handle remove/insert operations on trees
 *
 * <br clear="left"/>
 * FEATURE: CUT
 *
 * @author $Author: cjuon $
 * @version 0.2 $Revision: 1.26 $ $Date: 2010/04/26 03:01:00 $
 **/
public class InsertRemoveHandler implements MouseListener, KeyListener, ActionListener, RemoveConfirmable {

    public InsertRemoveHandler(Form form, JTree jTree, Element fieldDescriptor) {
        this.form = form;
        this.jTree = jTree;
        this.descriptor = (Element) fieldDescriptor.getElementsByTagName("insertRemoveRestrictions").item(0);
        this.removeRestrictions = this.descriptor.getElementsByTagName("removable");
        this.insertRestrictions = this.descriptor.getElementsByTagName("insertable");
        this.copyRestrictions = this.descriptor.getElementsByTagName("copyable");
        this.pasteRestrictions = this.descriptor.getElementsByTagName("pasteable");
        try {
            this.nodeNameGenerator = (NodeNameGenerator) Class.forName(NAME_GENERATOR).getConstructor(new Class[] { Element.class }).newInstance(new Object[] { fieldDescriptor });
        } catch (Exception e) {
            LOG.error(e);
        }
        Remove.initRemoveConfirmable(this, (Element) (removeRestrictions.getLength() > 0 ? removeRestrictions.item(0) : descriptor));
        jTree.addKeyListener(this);
        jTree.addMouseListener(this);
    }

    public void actionPerformed(ActionEvent evt) {
        isActive = true;
        Vector victims = obtainVictims();
        LOG.debug("VICTIMS " + victims.size());
        String command = evt.getActionCommand();
        if (command.equals("remove") && mayRemove(victims)) remove(victims);
        if (command.equals("new") && mayInsert(victim)) insert(victim);
        if (command.equals("copy") && mayCopy(victims)) copy(victims);
        if (command.equals("paste") && mayPaste(victim)) paste(victim);
        isActive = false;
    }

    private Vector obtainVictims() {
        Vector victims = getVictims();
        if (victim == null && victims.size() > 0) victim = (TreePath) victims.elementAt(0);
        return victims;
    }

    public void mouseClicked(MouseEvent evt) {
    }

    public void mouseEntered(MouseEvent evt) {
    }

    public void mouseExited(MouseEvent evt) {
    }

    public void mousePressed(MouseEvent evt) {
        isActive = true;
        if (!System.getProperty("os.name").startsWith("Win") && evt.isPopupTrigger()) {
            selectMousePosition(evt);
            showPopupMenu(evt);
        }
    }

    public void mouseReleased(MouseEvent evt) {
        isActive = true;
        if (System.getProperty("os.name").startsWith("Win") && evt.isPopupTrigger()) {
            selectMousePosition(evt);
            showPopupMenu(evt);
        }
    }

    private void selectMousePosition(MouseEvent evt) {
    }

    public void keyPressed(KeyEvent evt) {
    }

    public void keyTyped(KeyEvent evt) {
    }

    public void keyReleased(KeyEvent evt) {
        isActive = true;
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            victim = jTree.getSelectionPath();
            Vector victims = getVictims();
            if (mayRemove(victims)) remove(victims);
        }
        isActive = false;
    }

    public Object getRemoveEditor() {
        Vector victims = getVictims();
        Vector v = new Vector();
        for (int i = 0; i < victims.size(); i++) v.addElement((Node) ((TreePath) victims.elementAt(i)).getLastPathComponent());
        return new JLabel(createRemoveMessage(v));
    }

    public String getRemoveOption() {
        return removeOption;
    }

    public String getRemoveTitle() {
        return removeTitle;
    }

    public String getRemove1Message() {
        return remove1Message;
    }

    public String getRemoveNMessage() {
        return removeNMessage;
    }

    public String getCloseOption() {
        return closeOption;
    }

    public JComponent getParentComponent() {
        return jTree;
    }

    public void setRemoveOption(String string) {
        removeOption = string;
    }

    public void setRemoveTitle(String string) {
        removeTitle = string;
    }

    public void setCloseOption(String string) {
        closeOption = string;
    }

    public void setRemove1Message(String string) {
        remove1Message = string;
    }

    public void setRemoveNMessage(String string) {
        removeNMessage = string;
    }

    private String createRemoveMessage(Vector victims) {
        String base = (victims.size() == 1 ? getRemove1Message() : getRemoveNMessage());
        if (base.indexOf("{") == -1) return base; else return "<html>" + new MessageFormat(base).format(new Object[] { createRemoveInfo(victims) }) + "</html>";
    }

    private String createRemoveInfo(Vector victims) {
        StringBuffer info = new StringBuffer("<ul>");
        try {
            String xpath = XPathAPI.selectSingleNode(descriptor, "removable/@confirmInfoXPath").getNodeValue();
            for (int i = 0; i < victims.size(); i++) {
                info.append("<li>");
                info.append(XPathAPI.eval((Node) victims.elementAt(i), xpath).toString());
                info.append("</li>");
            }
        } catch (Exception e) {
            LOG.warn(e);
        }
        info.append("</ul>");
        return info.toString();
    }

    public boolean isActive() {
        return isActive;
    }

    public Vector getVictims() {
        Vector victims = new Vector();
        synchronized (jTree.getModel()) {
            TreePath selectionPaths[] = jTree.getSelectionPaths();
            if (selectionPaths != null) for (int i = 0; i < selectionPaths.length; i++) victims.addElement(selectionPaths[i]);
        }
        return victims;
    }

    public boolean mayRemove() {
        Vector victims = obtainVictims();
        return mayRemove(victims);
    }

    private boolean mayRemove(Vector victims) {
        if (victims == null || victims.size() == 0) return false;
        boolean ok = true;
        for (int v = 0; v < victims.size(); v++) {
            Node victimNode = (Node) ((TreePath) victims.elementAt(v)).getLastPathComponent();
            try {
                for (int i = 0; i < removeRestrictions.getLength() && ok; i++) {
                    Element restriction = (Element) removeRestrictions.item(i);
                    ok &= XPathAPI.evalBool(victimNode, restriction.getAttribute("srcExpr"));
                }
            } catch (Exception e) {
                LOG.error(e);
            }
        }
        return ok;
    }

    private boolean removalConfirmed(Vector victims) {
        return Remove.getDataFromRemoveDialog(this) != null;
    }

    private void remove(Vector victims) {
        if (removalConfirmed(victims)) {
            final Vector parents = new Vector();
            for (int i = 0; i < victims.size(); i++) {
                TreePath currentVictimPath = (TreePath) victims.elementAt(i);
                parents.add(currentVictimPath.getParentPath());
                ((DefaultTreeModel) jTree.getModel()).remove(currentVictimPath.getLastPathComponent());
            }
            form.fireDirty(jTree, true);
            DefaultTreeModel model = (DefaultTreeModel) jTree.getModel();
            Tree treeField = (Tree) form.getFieldByComponent((JComponent) jTree.getParent().getParent());
            treeField.setData(treeField.getData());
            jTree.repaint();
            Thread selector = new Thread() {

                public void run() {
                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {
                    }
                    for (int p = 0; p < parents.size(); p++) {
                        jTree.expandPath((TreePath) parents.elementAt(p));
                    }
                    jTree.repaint();
                }
            };
            selector.start();
        }
    }

    private boolean mayCopy(Vector victims) {
        if (victims == null || victims.size() == 0) return false;
        boolean ok = copyRestrictions.getLength() > 0;
        for (int v = 0; v < victims.size(); v++) {
            Node victimNode = (Node) ((TreePath) victims.elementAt(v)).getLastPathComponent();
            try {
                for (int i = 0; i < copyRestrictions.getLength() && ok; i++) {
                    Element restriction = (Element) copyRestrictions.item(i);
                    ok &= XPathAPI.evalBool(victimNode, restriction.getAttribute("srcExpr"));
                }
            } catch (Exception e) {
                LOG.error(e);
            }
        }
        return ok;
    }

    private void copy(Vector victims) {
        try {
            copied = victims;
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private boolean mayPaste(TreePath target) {
        if (target == null) return false;
        Node targetNode = (Node) target.getLastPathComponent();
        if (copied == null || copied.size() == 0) return false;
        pasteable = new Vector();
        for (int v = 0; v < copied.size(); v++) {
            Node copyNode = (Node) ((TreePath) copied.elementAt(v)).getLastPathComponent();
            LOG.debug("TARGET " + targetNode.getNodeName() + " copyNode " + copyNode);
            try {
                for (int i = 0; i < pasteRestrictions.getLength(); i++) {
                    Element restriction = (Element) pasteRestrictions.item(i);
                    if (XPathAPI.eval(copyNode, restriction.getAttribute("srcExpr")).toString().equals("true") && XPathAPI.eval(targetNode, restriction.getAttribute("dstExpr")).toString().equals("true")) {
                        pasteable.addElement(new Pasteable(restriction, copyNode));
                    }
                }
            } catch (Exception e) {
                LOG.error(e);
            }
        }
        return pasteable.size() > 0;
    }

    private void paste(final TreePath victimPath) {
        if (pasteable == null || pasteable.size() == 0) return;
        for (int v = 0; v < pasteable.size(); v++) {
            Node copyNode = ((Pasteable) pasteable.elementAt(v)).copyNode;
            try {
                ((DefaultTreeModel) jTree.getModel()).copyNode(copyNode.cloneNode(true), (Node) victimPath.getLastPathComponent(), ((Pasteable) pasteable.elementAt(v)).asChild(), false);
                form.fireDirty(jTree, true);
            } catch (Exception e) {
                LOG.error(e);
            }
        }
        jTree.updateUI();
    }

    public boolean mayInsert() {
        victim = null;
        obtainVictims();
        return mayInsert(victim);
    }

    private boolean mayInsert(TreePath victimPath) {
        if (victimPath == null) return false;
        Node victimNode = (Node) victimPath.getLastPathComponent();
        boolean ok = false;
        try {
            insertRestriction = null;
            for (int i = 0; i < insertRestrictions.getLength() && !ok; i++) {
                Element restriction = (Element) insertRestrictions.item(i);
                if (LOG.isDebugEnabled()) LOG.debug("RESTRICTION " + restriction.getAttribute("srcExpr") + "\nVICTIM NODE " + XMLPrintHelper.xmlDocumentToString(victimNode));
                if (XPathAPI.eval(victimNode, restriction.getAttribute("srcExpr")).toString().equals("true")) {
                    LOG.debug("RESTRICTION HOLDS");
                    ok = true;
                    insertRestriction = restriction;
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }
        return ok;
    }

    private void insert(final TreePath victimPath) {
        try {
            Element template = (Element) form.getModel().getData(insertRestriction.getAttribute("template"));
            if (LOG.isDebugEnabled()) LOG.debug("TEMPLATE " + XMLPrintHelper.xmlDocumentToString(template));
            final Node inserted = ((DefaultTreeModel) jTree.getModel()).copyNode(template.cloneNode(true), (Node) victimPath.getLastPathComponent(), true, false);
            form.fireDirty(jTree, true);
            DefaultTreeModel model = (DefaultTreeModel) jTree.getModel();
            Tree treeField = (Tree) form.getFieldByComponent((JComponent) jTree.getParent().getParent());
            treeField.setData(treeField.getData());
            jTree.updateUI();
            jTree.expandPath(victimPath.pathByAddingChild(inserted));
            Thread selector = new Thread() {

                public void run() {
                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {
                    }
                    jTree.setSelectionPath(victimPath.pathByAddingChild(inserted));
                    jTree.repaint();
                    jTree.expandPath(victimPath.pathByAddingChild(inserted));
                }
            };
            selector.start();
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private void showPopupMenu(MouseEvent evt) {
        isActive = true;
        Point location = evt.getPoint();
        victim = jTree.getPathForLocation(location.x, location.y);
        Vector victims = getVictims();
        JPopupMenu popup = null;
        boolean pasteDone = false;
        if (mayRemove(victims)) {
            popup = new JPopupMenu();
            JMenuItem removeItem = new JMenuItem(localizeMenuItem("remove"));
            removeItem.setActionCommand("remove");
            removeItem.addActionListener(this);
            popup.add(removeItem);
        }
        if (mayInsert(victim)) {
            if (popup == null) popup = new JPopupMenu();
            JMenuItem insertItem = new JMenuItem(localizeMenuItem("insert"));
            insertItem.setActionCommand("new");
            insertItem.addActionListener(this);
            popup.add(insertItem);
        }
        if (mayCopy(victims)) {
            if (popup == null) popup = new JPopupMenu();
            JMenuItem copyItem = new JMenuItem(localizeMenuItem("copy"));
            copyItem.setActionCommand("copy");
            copyItem.addActionListener(this);
            popup.add(copyItem);
            JMenuItem pasteItem = new JMenuItem(localizeMenuItem("paste"));
            pasteItem.setActionCommand("paste");
            pasteItem.addActionListener(this);
            pasteItem.setEnabled(copied != null ? true : false);
            popup.add(pasteItem);
            pasteDone = true;
        }
        if (!pasteDone && mayPaste(victim)) {
            if (popup == null) popup = new JPopupMenu();
            JMenuItem pasteItem = new JMenuItem(localizeMenuItem("paste"));
            pasteItem.setActionCommand("paste");
            pasteItem.addActionListener(this);
            pasteItem.setEnabled(copied != null ? true : false);
            popup.add(pasteItem);
            pasteDone = true;
        }
        if (popup != null) popup.show(evt.getComponent(), evt.getX(), evt.getY());
    }

    private String localizeMenuItem(String key) {
        String localized = descriptor.getAttribute(key + "Label");
        if (localized == null || localized.trim().length() == 0) localized = key;
        return localized;
    }

    private static class Pasteable {

        public Pasteable(Node restriction, Node copyNode) {
            this.restriction = (Element) restriction;
            this.copyNode = copyNode;
        }

        public boolean asChild() {
            return restriction.getAttribute("asChild").equals("true");
        }

        public Element restriction;

        public Node copyNode;
    }

    private TreePath victim = null;

    private Element descriptor = null;

    private NodeList removeRestrictions = null;

    private NodeList insertRestrictions = null;

    private Element insertRestriction = null;

    private NodeList pasteRestrictions = null;

    private Vector pasteable = null;

    private NodeList copyRestrictions = null;

    private Vector copied = null;

    private String removeOption = null;

    private String removeTitle = null;

    private String closeOption = null;

    private String remove1Message = null;

    private String removeNMessage = null;

    private NodeNameGenerator nodeNameGenerator = null;

    private static final String NAME_GENERATOR = "org.form4j.form.field.nodedata.tree.NodeNameGeneratorImpl";

    private Form form = null;

    private JTree jTree = null;

    private boolean isActive = false;

    private static final Logger LOG = Logger.getLogger(InsertRemoveHandler.class.getName());
}

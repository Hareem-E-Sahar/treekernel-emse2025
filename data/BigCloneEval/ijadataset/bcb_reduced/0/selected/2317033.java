package net.etherstorm.jOpenRPG;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import net.etherstorm.jOpenRPG.utils.*;
import net.etherstorm.jOpenRPG.nodehandlers.*;
import net.etherstorm.jOpenRPG.actions.DefaultAction;
import net.etherstorm.jOpenRPG.actions.SendNodeToPlayerAction;
import net.etherstorm.jOpenRPG.actions.GametreeAction;
import java.util.*;
import java.io.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.*;
import net.etherstorm.jOpenRPG.event.*;
import net.etherstorm.jOpenRPG.commlib.TreeMessage;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

/**
 * 
 * @author Ted Berg
 * @author $Author: tedberg $
 * @version $Revision: 352 $
 * $Date: 2002-02-01 02:32:11 -0500 (Fri, 01 Feb 2002) $
 */
public class JGameTreePanel extends JPanel implements GameTreeListener, FileHistoryListener {

    JGameTree gametree;

    Document doc;

    GameTreeModel treemodel;

    Hashtable images;

    File gametreeFile;

    JPopupMenu popupMenu;

    boolean treeIsDirty = false;

    SAXBuilder sax;

    ReferenceManager referenceManager = ReferenceManager.getInstance();

    String lastUsedDir = "";

    public final OpenTreeAction openTreeAction = new OpenTreeAction();

    public final SaveTreeAction saveTreeAction = new SaveTreeAction();

    public final SaveTreeAsAction saveTreeAsAction = new SaveTreeAsAction();

    public final ImportXMLFileAction importXMLFileAction = new ImportXMLFileAction();

    public final ExportXMLFileAction exportXMLFileAction = new ExportXMLFileAction();

    public final SendNodeToPlayerAction sendNodeToPlayerAction = new SendNodeToPlayerAction(null);

    public final JFileHistoryMenu gametreeHistory = new JFileHistoryMenu("Reopen Gametree");

    public final JFileHistoryMenu nodeHistory = new JFileHistoryMenu("Reimport Node");

    /**
	 * 
	 */
    public JGameTreePanel() {
        super(new BorderLayout());
        sax = new SAXBuilder();
        try {
            referenceManager.getPythonInterpreter().exec("import nodeloader");
            referenceManager.getPythonInterpreter().exec("loader = nodeloader.NodeLoader()");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            java.net.URL u = JGameTreePanel.class.getResource("/tree.xml");
            doc = sax.build(u);
            treemodel = new GameTreeModel(doc);
            treeIsDirty = false;
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
        gametree = new JGameTree(treemodel);
        gametree.setCellRenderer(new GameTreeCellRenderer());
        add(new JScrollPane(gametree));
        ToolTipManager.sharedInstance().registerComponent(gametree);
        images = new Hashtable();
        gametree.setEditable(true);
        popupMenu = new JPopupMenu("Gametree");
        JToolBar tb = new JToolBar();
        tb.add(new MoveNodeUpAction(null));
        tb.add(new MoveNodeDownAction(null));
        tb.add(new MoveNodeLeftAction(null));
        tb.add(new MoveNodeRightAction(null));
        tb.addSeparator();
        tb.add(new CloneNodeAction(null));
        tb.addSeparator();
        tb.add(new DeleteNodeAction(null));
        add(tb, BorderLayout.NORTH);
        gametree.addMouseListener(new MouseAdapter() {

            /**
					 * Method declaration
					 * 
					 * 
					 * @param evt
					 * 
					 * 
					 */
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    TreePath path = gametree.getClosestPathForLocation(evt.getX(), evt.getY());
                    BaseNodehandler bnh = (BaseNodehandler) path.getPath()[path.getPathCount() - 1];
                    bnh.openHandler();
                } else {
                    tryPopup(evt);
                }
            }

            /**
					 * Method declaration
					 * 
					 * 
					 * @param evt
					 * 
					 * 
					 */
            public void mousePressed(MouseEvent evt) {
                tryPopup(evt);
            }

            /**
					 * Method declaration
					 * 
					 * 
					 * @param evt
					 * 
					 * 
					 */
            public void mouseReleased(MouseEvent evt) {
                tryPopup(evt);
            }
        });
        referenceManager.getCore().addGameTreeListener(this);
        gametreeHistory.addFileHistoryListener(this);
        nodeHistory.addFileHistoryListener(this);
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * 
	 */
    public void doLoadInitialTree() {
        try {
            java.net.URL u = JGameTreePanel.class.getResource("/tree.xml");
            doc = sax.build(u);
            treemodel = new GameTreeModel(doc);
            gametree.setModel(treemodel);
            treeIsDirty = false;
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param evt
	 * 
	 * 
	 */
    void tryPopup(MouseEvent evt) {
        if (popupMenu.isPopupTrigger(evt)) {
            TreePath p = gametree.getClosestPathForLocation(evt.getX(), evt.getY());
            popupMenu.removeAll();
            ((BaseNodehandler) p.getLastPathComponent()).populatePopupMenu(popupMenu);
            popupMenu.show(gametree, evt.getX(), evt.getY());
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param l
	 * 
	 * 
	 */
    public void addTreeSelectionListener(TreeSelectionListener l) {
        gametree.addTreeSelectionListener(l);
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param l
	 * 
	 * 
	 */
    public void removeTreeSelectionListener(TreeSelectionListener l) {
        gametree.removeTreeSelectionListener(l);
    }

    public void reopenFile(FileHistoryEvent fhe) {
        if (fhe.getSource() == gametreeHistory) {
            loadGameTree(new File(fhe.getFilename()));
        } else if (fhe.getSource() == nodeHistory) {
            importXMLFile(new File(fhe.getFilename()));
        }
    }

    /**
	 * 
	 * 
	 * @param f
	 */
    public void loadGameTree(File f) {
        try {
            treemodel = null;
            doc = sax.build(f);
            treemodel = new GameTreeModel(doc);
            gametree.setModel(treemodel);
            gametreeFile = f;
            treeIsDirty = false;
            gametreeHistory.addFile(f);
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param e
	 * 
	 * 
	 */
    public void doImportXML(Element e) {
        try {
            org.jdom.output.XMLOutputter xout = new org.jdom.output.XMLOutputter();
            RootNodehandler root = (RootNodehandler) treemodel.getRoot();
            root.importXML(e);
            TreeModelEvent tme = new TreeModelEvent(treemodel, treemodel.getTreePath(root), new int[] { treemodel.getChildCount(root) - 1 }, null);
            treemodel.fireTreeNodesInsertedEvent(tme);
            treeIsDirty = true;
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * 
	 */
    public void doImportXML() {
        try {
            JFileChooser fc = referenceManager.getDefaultFileChooser();
            fc.setSelectedFile(new File(lastUsedDir));
            int result = fc.showOpenDialog(referenceManager.getMainFrame());
            if (result == fc.APPROVE_OPTION) {
                importXMLFile(fc.getSelectedFile());
            }
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    public void importXMLFile(File file) {
        try {
            lastUsedDir = file.getCanonicalPath();
            Document d = sax.build(file);
            doImportXML(new Element("foo").addContent((Element) d.getRootElement().clone()));
            nodeHistory.addFile(file);
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param e
	 * @param f
	 * 
	 * 
	 */
    public void exportXML(Element e, File f) {
        try {
            Document d = new Document((Element) e.clone());
            referenceManager.getCore().writeDomToFile(d);
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * 
	 */
    public void doExportXML() {
        try {
            BaseNodehandler bnh = (BaseNodehandler) gametree.getLastSelectedPathComponent();
            File sf = new File(bnh.getNodeName() + ".xml");
            JFileChooser fc = referenceManager.getDefaultFileChooser();
            fc.setSelectedFile(sf);
            int result = fc.showSaveDialog(referenceManager.getMainFrame());
            if (result == fc.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                Document d = new Document((Element) bnh.getElement().clone());
                referenceManager.getCore().writeDomToFile(d, f);
            }
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param e
	 * @param id
	 * 
	 * 
	 */
    public void doSendNodeToPlayer(Element e, String id) {
        referenceManager.getCore().sendTreeMessage(id, e);
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param target
	 * 
	 * 
	 */
    public void doSendNodeToPlayers(BaseNodehandler target) {
        try {
            BaseNodehandler bnh = (target == null) ? (BaseNodehandler) gametree.getLastSelectedPathComponent() : target;
            if (bnh == null) {
                return;
            }
            TreeMessage tm = new TreeMessage();
            tm.setNodehandler(bnh.getElement());
            tm.setIncoming(false);
            tm.send();
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * 
	 */
    public void doOpenTree() {
        try {
            JFileChooser fc = referenceManager.getDefaultFileChooser();
            int result = fc.showOpenDialog(referenceManager.getMainFrame());
            if (result == fc.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                loadGameTree(f);
                treemodel.fireTreeStructureChangedEvent();
                referenceManager.getMainFrame().setActiveDocument(f.toString());
            }
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * 
	 */
    public void doSaveTree() {
        try {
            if (gametreeFile == null) {
                doSaveTreeAs();
                return;
            }
            referenceManager.getCore().writeDomToFile(doc, gametreeFile);
            treeIsDirty = false;
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * 
	 */
    public void doSaveTreeAs() {
        try {
            JFileChooser fc = referenceManager.getDefaultFileChooser();
            fc.setSelectedFile(gametreeFile);
            int result = fc.showSaveDialog(referenceManager.getMainFrame());
            if (result == fc.APPROVE_OPTION) {
                gametreeFile = fc.getSelectedFile();
                doSaveTree();
            }
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    /**
	 * 
	 */
    public void enumNodehandlers() {
        try {
            Document d = sax.build(new File("/home/tedberg/teds_gaming_stuff.xml"));
            doImportXML(d.getRootElement().detach());
            treemodel.fireTreeStructureChangedEvent();
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    /**
	 * 
	 * 
	 * @param value
	 */
    Icon getNodeIcon(BaseNodehandler value) {
        String imageName = value.getNodeImageName();
        Icon i = null;
        if ((imageName != null) && (!imageName.equals(""))) {
            i = (Icon) images.get(imageName);
            if (i == null) {
                try {
                    i = ImageLib.loadImage("icons/" + imageName + ".gif");
                } catch (Exception ex) {
                }
                images.put(imageName, i);
            }
        }
        return i;
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param evt
	 * 
	 * 
	 */
    public void receivedTreeNode(GameTreeEvent evt) {
        Element e = new Element("foo");
        e.addContent((Element) evt.getMsg().getNodehandler().clone());
        doImportXML(e);
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param target
	 * 
	 * 
	 */
    public void doMoveNodeUp(BaseNodehandler target) {
        treemodel.doMoveNodeUp(target);
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param target
	 * 
	 * 
	 */
    public void doMoveNodeDown(BaseNodehandler target) {
        treemodel.doMoveNodeDown(target);
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param target
	 * 
	 * 
	 */
    public void doMoveNodeLeft(BaseNodehandler target) {
        treemodel.doMoveNodeLeft(target);
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param target
	 * 
	 * 
	 */
    public void doMoveNodeRight(BaseNodehandler target) {
        treemodel.doMoveNodeRight(target);
    }

    public void doCloneNode(BaseNodehandler target) {
        treemodel.doCloneNode(target);
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param target
	 * 
	 * 
	 */
    public void doDeleteNode(BaseNodehandler target) {
        treemodel.doDeleteNode(target);
    }

    /**
	 * 
	 * 
	 */
    class GameTreeCellRenderer extends DefaultTreeCellRenderer {

        /**
		 * 
		 */
        public GameTreeCellRenderer() {
            super();
        }

        /**
		 * 
		 * 
		 * @param tree
		 * @param value
		 * @param selected
		 * @param expanded
		 * @param leaf
		 * @param row
		 * @param hasFocus
		 */
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            try {
                setToolTipText("Node class :" + ((BaseNodehandler) value).getNodeType() + "[" + value.getClass() + "]");
                Icon i = getNodeIcon((BaseNodehandler) value);
                if (i != null) {
                    setIcon(i);
                }
            } catch (Exception ex) {
            }
            setBackgroundNonSelectionColor(getBackground());
            return this;
        }
    }

    /**
	 * 
	 * 
	 */
    public class OpenTreeAction extends DefaultAction {

        /**
		 * Constructor declaration
		 * 
		 * 
		 */
        public OpenTreeAction() {
            super();
            initProperties("OpenTreeAction");
        }

        /**
		 * Method declaration
		 * 
		 * 
		 * @param evt
		 * 
		 * 
		 */
        public void actionPerformed(ActionEvent evt) {
            super.actionPerformed(evt);
            doOpenTree();
        }
    }

    /**
	 * 
	 * 
	 */
    public class SaveTreeAction extends DefaultAction {

        /**
		 * Constructor declaration
		 * 
		 * 
		 */
        public SaveTreeAction() {
            super();
            initProperties("SaveTreeAction");
        }

        /**
		 * Method declaration
		 * 
		 * 
		 * @param evt
		 * 
		 * 
		 */
        public void actionPerformed(ActionEvent evt) {
            super.actionPerformed(evt);
            doSaveTree();
        }
    }

    /**
	 * 
	 * 
	 */
    public class SaveTreeAsAction extends DefaultAction {

        /**
		 * Constructor declaration
		 * 
		 * 
		 */
        public SaveTreeAsAction() {
            super();
            initProperties("SaveTreeAsAction");
        }

        /**
		 * Method declaration
		 * 
		 * 
		 * @param evt
		 * 
		 * 
		 */
        public void actionPerformed(ActionEvent evt) {
            super.actionPerformed(evt);
            doSaveTreeAs();
        }
    }

    /**
	 * 
	 * 
	 */
    public class ImportXMLFileAction extends DefaultAction {

        /**
		 * Constructor declaration
		 * 
		 * 
		 */
        public ImportXMLFileAction() {
            super();
            initProperties("ImportXMLFileAction");
        }

        /**
		 * Method declaration
		 * 
		 * 
		 * @param evt
		 * 
		 * 
		 */
        public void actionPerformed(ActionEvent evt) {
            super.actionPerformed(evt);
            doImportXML();
        }
    }

    /**
	 * 
	 * 
	 */
    public class ExportXMLFileAction extends DefaultAction {

        /**
		 * Constructor declaration
		 * 
		 * 
		 */
        public ExportXMLFileAction() {
            super();
            initProperties("ExportXMLFileAction");
        }

        /**
		 * Method declaration
		 * 
		 * 
		 * @param evt
		 * 
		 * 
		 */
        public void actionPerformed(ActionEvent evt) {
            super.actionPerformed(evt);
            doExportXML();
        }
    }

    /**
	 * 
	 * 
	 */
    public class MoveNodeUpAction extends GametreeAction {

        /**
		 * Constructor declaration
		 * 
		 * 
		 * @param bnh
		 * 
		 */
        public MoveNodeUpAction(BaseNodehandler bnh) {
            super(bnh);
            initProperties("MoveNodeUpAction");
        }

        /**
		 * Method declaration
		 * 
		 * 
		 * @param evt
		 * 
		 * 
		 */
        public void actionPerformed(ActionEvent evt) {
            super.actionPerformed(evt);
            doMoveNodeUp((BaseNodehandler) gametree.getLastSelectedPathComponent());
        }
    }

    /**
	 * 
	 * 
	 */
    public class MoveNodeDownAction extends GametreeAction {

        /**
		 * Constructor declaration
		 * 
		 * 
		 * @param bnh
		 * 
		 */
        public MoveNodeDownAction(BaseNodehandler bnh) {
            super(bnh);
            initProperties("MoveNodeDownAction");
        }

        /**
		 * Method declaration
		 * 
		 * 
		 * @param evt
		 * 
		 * 
		 */
        public void actionPerformed(ActionEvent evt) {
            super.actionPerformed(evt);
            doMoveNodeDown((BaseNodehandler) gametree.getLastSelectedPathComponent());
        }
    }

    /**
	 * 
	 * 
	 */
    public class MoveNodeLeftAction extends GametreeAction {

        /**
		 * Constructor declaration
		 * 
		 * 
		 * @param bnh
		 * 
		 */
        public MoveNodeLeftAction(BaseNodehandler bnh) {
            super(bnh);
            initProperties("MoveNodeLeftAction");
        }

        /**
		 * Method declaration
		 * 
		 * 
		 * @param evt
		 * 
		 * 
		 */
        public void actionPerformed(ActionEvent evt) {
            super.actionPerformed(evt);
            doMoveNodeLeft((BaseNodehandler) gametree.getLastSelectedPathComponent());
        }
    }

    /**
	 * 
	 * 
	 */
    public class MoveNodeRightAction extends GametreeAction {

        /**
		 * Constructor declaration
		 * 
		 * 
		 * @param bnh
		 * 
		 */
        public MoveNodeRightAction(BaseNodehandler bnh) {
            super(bnh);
            initProperties("MoveNodeRightAction");
        }

        /**
		 * Method declaration
		 * 
		 * 
		 * @param evt
		 * 
		 * 
		 */
        public void actionPerformed(ActionEvent evt) {
            super.actionPerformed(evt);
            doMoveNodeRight((BaseNodehandler) gametree.getLastSelectedPathComponent());
        }
    }

    public class CloneNodeAction extends GametreeAction {

        public CloneNodeAction(BaseNodehandler bnh) {
            super(bnh);
            initProperties("CloneNodeAction");
        }

        public void actionPerformed(ActionEvent evt) {
            super.actionPerformed(evt);
            doCloneNode((BaseNodehandler) gametree.getLastSelectedPathComponent());
        }
    }

    /**
	 * 
	 * 
	 */
    public class DeleteNodeAction extends GametreeAction {

        /**
		 * Constructor declaration
		 * 
		 * 
		 * @param bnh
		 * 
		 */
        public DeleteNodeAction(BaseNodehandler bnh) {
            super(bnh);
            initProperties("DeleteNodeAction");
        }

        /**
		 * Method declaration
		 * 
		 * 
		 * @param evt
		 * 
		 * 
		 */
        public void actionPerformed(ActionEvent evt) {
            super.actionPerformed(evt);
            doDeleteNode((BaseNodehandler) gametree.getLastSelectedPathComponent());
        }
    }
}

/**
 * Class declaration
 * 
 * 
 * @author $Author: tedberg $
 * @version $Revision: 352 $
 */
class JGameTree extends JTree implements DropTargetListener, DragSourceListener, DragGestureListener {

    GameTreeModel model;

    DragSource dragSource;

    DropTarget dropTarget;

    int margin = 12;

    /**
	 * Constructor declaration
	 * 
	 * 
	 * @param tmodel
	 * 
	 */
    public JGameTree(GameTreeModel tmodel) {
        super(tmodel);
        model = tmodel;
        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
        dropTarget = new DropTarget(this, this);
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param dtde
	 * 
	 * 
	 * 
	 */
    protected BaseNodehandler getHandlerForEvent(DropTargetDragEvent dtde) {
        return getHandlerForLocation(dtde.getLocation());
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param p
	 * 
	 * 
	 * 
	 */
    protected BaseNodehandler getHandlerForLocation(Point p) {
        BaseNodehandler target = null;
        target = (BaseNodehandler) getClosestPathForLocation(p.x, p.y).getLastPathComponent();
        return target;
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param dtde
	 * 
	 * 
	 * 
	 */
    protected BaseNodehandler getHandlerForEvent(DropTargetDropEvent dtde) {
        return getHandlerForLocation(dtde.getLocation());
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param location
	 * 
	 * 
	 */
    public void autoscroll(Point p) {
        int realrow = getRowForLocation(p.x, p.y);
        Rectangle outer = getBounds();
        realrow = ((p.y + outer.y <= margin) ? ((realrow < 1) ? 0 : realrow - 1) : ((realrow < getRowCount() - 1) ? realrow + 1 : realrow));
        scrollRowToVisible(realrow);
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * 
	 * 
	 */
    public Insets getAutoscrollInsets() {
        Rectangle outer = getBounds();
        Rectangle inner = getParent().getBounds();
        return new Insets(inner.y - outer.y + margin, inner.x - outer.x + margin, outer.height - inner.height - inner.y + outer.y + margin, outer.width - inner.width - inner.x + outer.x + margin);
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param dtde
	 * 
	 * 
	 */
    public void dragEnter(DropTargetDragEvent dtde) {
        BaseNodehandler target = getHandlerForEvent(dtde);
        if (target == null || target.isAcceptingChildren() == false) {
            dtde.rejectDrag();
        } else {
            dtde.acceptDrag(DnDConstants.ACTION_MOVE);
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param dte
	 * 
	 * 
	 */
    public void dragExit(DropTargetEvent dte) {
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param dtde
	 * 
	 * 
	 */
    public void dragOver(DropTargetDragEvent dtde) {
        BaseNodehandler target = getHandlerForEvent(dtde);
        if (target == null || target.isAcceptingChildren() == false) {
            dtde.rejectDrag();
        } else {
            dtde.acceptDrag(DnDConstants.ACTION_MOVE);
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param dtde
	 * 
	 * 
	 */
    public void drop(DropTargetDropEvent dtde) {
        BaseNodehandler target = getHandlerForEvent(dtde);
        if (target == null || target.isAcceptingChildren() == false) {
            dtde.rejectDrop();
            return;
        }
        for (int loop = 0; loop < dtde.getCurrentDataFlavors().length; loop++) {
            if (dtde.getCurrentDataFlavors()[loop].getHumanPresentableName().equals(BaseNodehandler.HUMAN_PRESENTABLE_NAME)) {
                try {
                    BaseNodehandler bnh = (BaseNodehandler) dtde.getTransferable().getTransferData(dtde.getCurrentDataFlavors()[loop]);
                    if (target.isAcceptingChildren() && !target.isChildOf(bnh) && (target != bnh)) {
                        System.out.println("removing node");
                        model.removeNodehandler(bnh);
                        System.out.println("inserting node");
                        model.insertNodehandler(target, bnh, 0);
                        System.out.println("accepting drop");
                        dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                        System.out.println("completing drop");
                        dtde.dropComplete(true);
                        System.out.println("------------------------------------");
                        return;
                    }
                    dtde.rejectDrop();
                    return;
                } catch (UnsupportedFlavorException ufe) {
                    dtde.rejectDrop();
                    ExceptionHandler.handleException(ufe);
                    return;
                } catch (IOException ioe) {
                    dtde.rejectDrop();
                    ExceptionHandler.handleException(ioe);
                    return;
                }
            }
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param dtde
	 * 
	 * 
	 */
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param dsde
	 * 
	 * 
	 */
    public void dragDropEnd(DragSourceDropEvent dsde) {
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param dsde
	 * 
	 * 
	 */
    public void dragEnter(DragSourceDragEvent dsde) {
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param dse
	 * 
	 * 
	 */
    public void dragExit(DragSourceEvent dse) {
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param dsde
	 * 
	 * 
	 */
    public void dragOver(DragSourceDragEvent dsde) {
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param dsde
	 * 
	 * 
	 */
    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param dge
	 * 
	 * 
	 */
    public void dragGestureRecognized(DragGestureEvent dge) {
        int row = getRowForLocation(dge.getDragOrigin().x, dge.getDragOrigin().y);
        if (row > -1) {
            TreePath path = getPathForRow(row);
            BaseNodehandler bnh = (BaseNodehandler) path.getLastPathComponent();
            if (bnh.isDraggable()) {
                dragSource.startDrag(dge, DragSource.DefaultMoveDrop, bnh, this);
            }
        }
    }
}

/**
 * 
 * 
 */
class GameTreeModel implements TreeModel {

    RootNodehandler root;

    Vector listeners;

    Document doc;

    /**
	 * 
	 */
    public GameTreeModel(Document doc) {
        root = new RootNodehandler(doc.getRootElement());
        listeners = new Vector();
        this.doc = doc;
    }

    /**
	 * 
	 */
    public void reload() {
        root.clear();
        root.importXML(root, doc.getRootElement());
        fireTreeStructureChangedEvent();
    }

    /**
	 * 
	 * 
	 * @param l
	 */
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    /**
	 * 
	 * 
	 * @param l
	 */
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    /**
	 * 
	 * 
	 * @param parent
	 * @param index
	 */
    public Object getChild(Object parent, int index) {
        return ((BaseNodehandler) parent).getChildNode(index);
    }

    /**
	 * 
	 * 
	 * @param parent
	 */
    public int getChildCount(Object parent) {
        return ((BaseNodehandler) parent).getChildNodeCount();
    }

    /**
	 * 
	 * 
	 * @param parent
	 * @param child
	 */
    public int getIndexOfChild(Object parent, Object child) {
        return ((BaseNodehandler) parent).indexOfChild((BaseNodehandler) child);
    }

    /**
	 * 
	 * 
	 */
    public Object getRoot() {
        return root;
    }

    /**
	 * 
	 * 
	 * @param node
	 */
    public boolean isLeaf(Object node) {
        return ((BaseNodehandler) node).getChildNodeCount() == 0;
    }

    /**
	 * 
	 * 
	 * @param path
	 * @param newValue
	 */
    public void valueForPathChanged(TreePath path, Object newValue) {
        BaseNodehandler bnh = (BaseNodehandler) path.getLastPathComponent();
        bnh.setNodeName((String) newValue);
        TreeModelEvent tme = new TreeModelEvent(this, path, null, null);
        fireTreeNodesChangedEvent(tme);
    }

    /**
	 * 
	 * 
	 * @param evt
	 */
    public void fireTreeStructureChangedEvent() {
        TreeModelEvent evt = new TreeModelEvent(this, new TreePath(root));
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            ((TreeModelListener) iter.next()).treeStructureChanged(evt);
        }
    }

    /**
	 * 
	 * 
	 * @param evt
	 */
    public void fireTreeNodesChangedEvent(TreeModelEvent evt) {
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            ((TreeModelListener) iter.next()).treeNodesChanged(evt);
        }
    }

    /**
	 * 
	 * 
	 * @param evt
	 */
    public void fireTreeNodesInsertedEvent(TreeModelEvent evt) {
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            ((TreeModelListener) iter.next()).treeNodesInserted(evt);
        }
    }

    /**
	 * 
	 * 
	 * @param evt
	 */
    public void fireTreeNodesRemovedEvent(TreeModelEvent evt) {
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            ((TreeModelListener) iter.next()).treeNodesRemoved(evt);
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param target
	 * 
	 * 
	 * 
	 */
    TreePath getTreePath(BaseNodehandler target) {
        Vector v = new Vector();
        do {
            v.add(0, target);
            target = target.getParentNodehandler();
        } while (target != null);
        TreePath p = new TreePath(v.toArray());
        return p;
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param bnh
	 * 
	 * 
	 */
    public void removeNodehandler(BaseNodehandler bnh) {
        System.out.println("Starting node removal");
        BaseNodehandler parent = bnh.getParentNodehandler();
        int index = parent.indexOfChild(bnh);
        parent.removeChildNode(bnh);
        bnh.getElement().detach();
        parent.getElement().removeContent(bnh.getElement());
        TreeModelEvent tme = new TreeModelEvent(this, getTreePath(parent), new int[] { index }, null);
        fireTreeNodesRemovedEvent(tme);
        System.out.println("Ending node removal");
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param parent
	 * @param child
	 * @param index
	 * 
	 * 
	 */
    public void insertNodehandler(BaseNodehandler parent, BaseNodehandler child, int index) {
        System.out.println("Starting node insertion");
        parent.addChildNode(index, child);
        parent.getElement().getChildren("nodehandler").add(index, child.getElement());
        TreeModelEvent tme = new TreeModelEvent(this, getTreePath(parent), new int[] { index }, null);
        fireTreeNodesInsertedEvent(tme);
        System.out.println("Ending node insertion");
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param target
	 * 
	 * 
	 */
    public void doMoveNodeUp(BaseNodehandler target) {
        BaseNodehandler child = target;
        if (child == null) {
            return;
        }
        BaseNodehandler parent = child.getParentNodehandler();
        int index = parent.indexOfChild(child);
        if (index > 0) {
            removeNodehandler(child);
            insertNodehandler(parent, child, index - 1);
        } else {
            if (parent != root) {
                index = parent.getParentNodehandler().indexOfChild(parent);
                BaseNodehandler prev = parent.getParentNodehandler();
                removeNodehandler(child);
                insertNodehandler(prev, child, index);
            }
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param target
	 * 
	 * 
	 */
    public void doMoveNodeDown(BaseNodehandler target) {
        BaseNodehandler child = target;
        if (child == null) {
            return;
        }
        BaseNodehandler parent = child.getParentNodehandler();
        int index = parent.indexOfChild(child);
        if (index < parent.getChildNodeCount() - 1) {
            removeNodehandler(child);
            insertNodehandler(parent, child, index + 1);
        } else {
            if (parent != root) {
                index = parent.getParentNodehandler().indexOfChild(parent);
                BaseNodehandler prev = parent.getParentNodehandler();
                removeNodehandler(child);
                insertNodehandler(prev, child, index + 1);
            }
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param target
	 * 
	 * 
	 */
    public void doMoveNodeLeft(BaseNodehandler target) {
        BaseNodehandler child = target;
        if (child == null) {
            return;
        }
        BaseNodehandler parent = child.getParentNodehandler();
        int index = parent.indexOfChild(child);
        if (parent != root) {
            index = parent.getParentNodehandler().indexOfChild(parent);
            removeNodehandler(child);
            insertNodehandler(parent.getParentNodehandler(), child, index + 1);
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param target
	 * 
	 * 
	 */
    public void doMoveNodeRight(BaseNodehandler target) {
        BaseNodehandler child = target;
        if (child == null) {
            return;
        }
        BaseNodehandler parent = child.getParentNodehandler();
        int index = parent.indexOfChild(child);
        if (index < parent.getChildNodeCount() - 1) {
            BaseNodehandler dest = parent.getChildNode(index + 1);
            if (dest.isAcceptingChildren()) {
                removeNodehandler(child);
                insertNodehandler(dest, child, 0);
            } else {
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    /**
	 *
	 */
    public void doCloneNode(BaseNodehandler target) {
        System.out.println("In doCloneNode");
        BaseNodehandler child = target;
        if (child == null) return;
        if (child != root) {
            System.out.println("Everything looks okay, cloning");
            BaseNodehandler parent = child.getParentNodehandler();
            int index = getIndexOfChild(parent, child);
            Element e = (Element) child.getElement().clone();
            root.importXML(parent, new Element("foo").addContent(e));
            fireTreeStructureChangedEvent();
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param target
	 * 
	 * 
	 */
    public void doDeleteNode(BaseNodehandler target) {
        BaseNodehandler child = target;
        if (child == null) {
            return;
        }
        BaseNodehandler parent = child.getParentNodehandler();
        removeNodehandler(child);
    }
}

/**
 * 
 * 
 */
class RootNodehandler extends BaseNodehandler {

    /**
	 * 
	 * 
	 * @param e
	 */
    public RootNodehandler(Element e) {
        super(e);
        importXML(this, myElement);
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * 
	 * 
	 */
    public boolean isAcceptingChildren() {
        return true;
    }

    /**
	 * Method declaration
	 *
	 *
	 *
	 *
	 */
    public boolean isDraggable() {
        return false;
    }

    /**
	 * 
	 * 
	 */
    public String getNodeName() {
        return "Gametree";
    }

    /**
	 * 
	 * 
	 * @param name
	 */
    public void setName(String name) {
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param name
	 * @param e
	 * 
	 * 
	 * 
	 */
    BaseNodehandler loadJavaNodehandler(String name, Element e) {
        try {
            String classname = "net.etherstorm.jOpenRPG.nodehandlers." + name;
            Class c = getClass().getClassLoader().loadClass(classname);
            Class[] arg_types = { Element.class };
            java.lang.reflect.Constructor ctor = c.getConstructor(arg_types);
            Object[] args = { e };
            BaseNodehandler bnh = (BaseNodehandler) ctor.newInstance(args);
            return bnh;
        } catch (ClassNotFoundException ex) {
            return null;
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
            return null;
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param name
	 * @param e
	 * 
	 * 
	 * 
	 */
    BaseNodehandler loadInternalPyNodehandler(String name, Element e) {
        try {
            this.referenceManager.getPythonInterpreter().exec("import java\nimport sys\njava.lang.System.out.println( sys.path )");
            System.out.println("using internal python nodehandler");
            this.referenceManager.getPythonInterpreter().exec("import scripts.pyhandlers");
            System.out.println("import done");
            this.referenceManager.getPythonInterpreter().set("__element", e);
            System.out.println("element defined");
            this.referenceManager.getPythonInterpreter().exec("__foo = scripts.pyhandlers." + name + "." + name + "( __element )");
            System.out.println("nodehandler created");
            BaseNodehandler bnh = (BaseNodehandler) this.referenceManager.getPythonInterpreter().get("__foo", BaseNodehandler.class);
            System.out.println("nodehandler retrieved");
            System.out.println(bnh);
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return bnh;
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
            ex.printStackTrace();
            return null;
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param name
	 * @param e
	 * 
	 * 
	 * 
	 */
    BaseNodehandler loadExternalPyNodehandler(String name, Element e) {
        try {
            System.out.println("loading external handler");
            File f = new File(System.getProperty("user.home") + File.separator + "jopenrpg" + File.separator + "jazz" + File.separator + "pyhandlers" + File.separator + name + ".py");
            System.out.println("using file " + f);
            if (f.exists()) {
                this.referenceManager.getPythonInterpreter().exec("print sys.path");
                System.out.println("file \"" + f + "\" exists");
                this.referenceManager.getPythonInterpreter().exec("import jazz");
                System.out.println("imported nodehandlers");
                this.referenceManager.getPythonInterpreter().set("__element", e);
                System.out.println("set element");
                this.referenceManager.getPythonInterpreter().exec("__foo = jazz.pyhandlers." + name + "." + name + "( __element )");
                System.out.println("created nodehandler");
                this.referenceManager.getPythonInterpreter().exec("print __foo");
                this.referenceManager.getPythonInterpreter().exec("print type( __foo )");
                this.referenceManager.getPythonInterpreter().exec("__bar = __foo.bork()");
                org.python.core.PyObject obj = referenceManager.getPythonInterpreter().get("__bar");
                System.out.println(obj);
                BaseNodehandler bnh = (BaseNodehandler) obj.__tojava__(BaseNodehandler.class);
                System.out.println("retrieved nodehandler");
                return bnh;
            } else {
                return null;
            }
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
            return null;
        }
    }

    /**
	 * 
	 * 
	 * @param name
	 * @param e
	 */
    BaseNodehandler loadHandler(String name, Element e) {
        BaseNodehandler bnh = loadJavaNodehandler(name, e);
        if (bnh == null) {
            bnh = loadInternalPyNodehandler(name, e);
            if (bnh == null) {
                bnh = loadExternalPyNodehandler(name, e);
                if (bnh == null) {
                    bnh = new UnknownNodehandler(e);
                }
            }
        }
        return bnh;
    }

    /**
	 * 
	 * 
	 * @param parent
	 * @param e
	 */
    public void importXML(BaseNodehandler parent, Element e) {
        java.util.List nl = e.getChildren("nodehandler");
        Iterator iter = nl.iterator();
        while (iter.hasNext()) {
            Element elem = (Element) iter.next();
            elem.detach();
            BaseNodehandler handler = loadHandler(elem.getAttributeValue("class"), elem);
            parent.getElement().addContent(elem);
            parent.addChildNode(handler);
            importXML(handler, elem);
        }
    }

    /**
	 * Method declaration
	 * 
	 * 
	 * @param e
	 * 
	 * 
	 */
    public void importXML(Element e) {
        importXML(this, e);
    }
}

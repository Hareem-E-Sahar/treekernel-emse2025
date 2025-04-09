package org.mitre.rt.client.ui.groups;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.TreeExpansionEvent;
import org.mitre.rt.client.exceptions.RTClientException;
import org.mitre.rt.client.ui.recommendations.AddEditRecommendationsPanel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.math.BigInteger;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import org.apache.log4j.Logger;
import org.mitre.rt.client.core.MetaManager;
import org.mitre.rt.client.xml.ApplicationHelper;
import org.mitre.rt.client.xml.GroupHelper;
import org.mitre.rt.client.xml.RecommendationHelper;
import org.mitre.rt.client.xml.VersionedItemTypeHelper;
import org.mitre.rt.client.exceptions.DataManagerException;
import org.mitre.rt.rtclient.ApplicationType;
import org.mitre.rt.rtclient.GroupType;
import org.mitre.rt.rtclient.RecommendationReferenceType;
import org.mitre.rt.rtclient.RecommendationType;
import org.mitre.rt.client.ui.html.ViewHtmlDialog;
import org.mitre.rt.client.ui.recommendations.AddEditRuleMainDialog;
import org.mitre.rt.client.ui.transfer.GroupTreeTransferHandler;
import org.mitre.rt.client.util.GlobalUITools;

/**
 * <code>ManageGroups</code> provides the user interface and GUI control code 
 * needed to add, edit, and delete groups.  Support for modifying group 
 * metadata is provided by {@link GroupMetadataPanel} and {@link AddEditRecommendationsPanel}.  
 * <p>
 * A tree-view on the left hand side of the panel provides navigation through 
 * groups.  It allows the user to reorder, delete, and add groups.  
 * Recommendations appear in the tree view as leaf-nodes descended from 
 * a group.  Groups with children are treated as branches; all others are leaves.  
 * @author  Reid Gilman (rgilman@mitre.org)
 */
public class ManageGroups extends javax.swing.JPanel {

    private static final Logger logger = Logger.getLogger(ManageGroups.class.getPackage().getName());

    private final ApplicationType currentApplication;

    private AddEditRecommendationsPanel myRecPanel;

    private GroupMetadataPanel myGroupPanel;

    private DefaultTreeModel treeModel;

    private final JPopupMenu groupContextMenu;

    private final JPopupMenu treeContextMenu;

    private final JPopupMenu recommendationContextMenu;

    private final ManageGroups selfReference = this;

    private Vector<Integer> expandedRows;

    private MutableTreeNode rootNode;

    /**
     * Creates a new <code>ManageGroups</code> panel displaying all of <code>
     * curApp<code>'s groups.  
     * @param curApp The application whose groups are to be edited
     */
    public ManageGroups(ApplicationType curApp) {
        currentApplication = curApp;
        rootNode = getRootNode();
        treeModel = new DefaultTreeModel(rootNode);
        initComponents();
        this.groupTree.addTreeExpansionListener(new TreeExpansionListener() {

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                logger.debug(event.getPath() + " EXPANDED");
                TreePath tp = event.getPath();
                Rectangle rect = groupTree.getPathBounds(tp);
                groupTree.scrollRectToVisible(rect);
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                logger.debug(event.getPath() + " COLLAPSED");
                TreePath tp = event.getPath();
                Rectangle rect = groupTree.getPathBounds(tp);
                groupTree.scrollRectToVisible(rect);
            }
        });
        groupContextMenu = createGroupContextMenu();
        treeContextMenu = createTreeContextMenu();
        recommendationContextMenu = createRecommendationContextMenu();
        myRecPanel = new AddEditRecommendationsPanel(this);
        myGroupPanel = new GroupMetadataPanel(this);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
        rightPanel.add(myGroupPanel);
        groupTree.setModel(treeModel);
        groupTree.setDragEnabled(true);
        groupTree.setTransferHandler(new GroupTreeTransferHandler(groupTree, this));
        groupTree.setRootVisible(false);
        groupTree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                Object node = groupTree.getLastSelectedPathComponent();
                GroupType selectedGroup = null;
                if (node instanceof GroupMutableTreeNode) {
                    selectedGroup = ((GroupMutableTreeNode) node).getGroup();
                } else if (node instanceof RecommendationMutableTreeNode) {
                    selectedGroup = (((RecommendationMutableTreeNode) node).getParent()).getGroup();
                } else {
                    return;
                }
                displayGroupMetadataPanel(selectedGroup);
            }
        });
        groupTree.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    TreePath objectClickedPath = groupTree.getPathForLocation(e.getX(), e.getY());
                    groupTree.setSelectionPath(objectClickedPath);
                    if (objectClickedPath == null) {
                        treeContextMenu.show(e.getComponent(), e.getX(), e.getY());
                    } else {
                        Object objectClicked = objectClickedPath.getLastPathComponent();
                        if (objectClicked instanceof GroupMutableTreeNode) {
                            groupContextMenu.show(e.getComponent(), e.getX(), e.getY());
                        } else if (objectClicked instanceof RecommendationMutableTreeNode) {
                            recommendationContextMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            }

            public void mousePressed(MouseEvent e) {
                return;
            }

            public void mouseReleased(MouseEvent e) {
                return;
            }

            public void mouseEntered(MouseEvent e) {
                return;
            }

            public void mouseExited(MouseEvent e) {
                return;
            }
        });
    }

    private void storeExpansionState() {
        expandedRows = new Vector<Integer>();
        int rowCount = groupTree.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            if (groupTree.isExpanded(i)) {
                expandedRows.add(i);
            }
        }
    }

    private void restoreExpansionState() {
        for (Integer row : expandedRows) {
            MutableTreeNode node = (MutableTreeNode) groupTree.getPathForRow(row).getLastPathComponent();
            if (node.getChildCount() > 0) {
                groupTree.expandRow(row);
            }
        }
    }

    protected void refreshTree() {
        GlobalUITools.refreshComponent(groupTree);
    }

    /**
     * 
     * @return the currently selected node
     */
    private MutableTreeNode getSelectedNode() {
        TreePath selectedPath = groupTree.getSelectionPath();
        if (selectedPath == null) {
            return null;
        }
        return ((MutableTreeNode) selectedPath.getLastPathComponent());
    }

    private TreePath getSelectedPath() {
        return groupTree.getSelectionPath();
    }

    /**
     * 
     * @return the popup menu for <code>GroupMutableTreeNode</code>s
     */
    private JPopupMenu createGroupContextMenu() {
        final JPopupMenu menu = new JPopupMenu();
        JMenuItem moveDown = getMoveDownItem();
        JMenuItem moveUp = getMoveUpItem();
        groupTree.addTreeSelectionListener(getMenuActivationListener(moveUp, moveDown));
        JMenuItem addChild = new JMenuItem("Add Sub-group");
        addChild.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                GroupType groupClicked;
                MutableTreeNode selectedNode = getSelectedNode();
                if (!(selectedNode instanceof GroupMutableTreeNode)) {
                    return;
                }
                groupClicked = ((GroupMutableTreeNode) selectedNode).getGroup();
                GroupType newGroup = ApplicationHelper.getNewGroup(currentApplication);
                newGroup.setTitle("New Group");
                newGroup.setDescription("");
                GroupHelper.addSubGroupReference(groupClicked, newGroup, BigInteger.ONE);
                TreePath pathClicked = getSelectedPath();
                MutableTreeNode parentNode = (MutableTreeNode) pathClicked.getLastPathComponent();
                GroupMutableTreeNode newNode = new GroupMutableTreeNode(newGroup);
                treeModel.insertNodeInto(newNode, parentNode, 0);
                newNode.setParent(parentNode);
                storeExpansionState();
                GlobalUITools.refreshComponent(groupTree);
                restoreExpansionState();
            }
        });
        JMenuItem delete = new JMenuItem("Delete");
        delete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                GroupType groupClicked = null;
                try {
                    MutableTreeNode selectedNode = getSelectedNode();
                    MutableTreeNode parentNode = (MutableTreeNode) groupTree.getSelectionPath().getLastPathComponent();
                    groupClicked = ((GroupMutableTreeNode) selectedNode).getGroup();
                    int[] childIndices = { parentNode.getIndex(selectedNode) };
                    Object[] childrenRemoved = { selectedNode };
                    if (parentNode instanceof GroupMutableTreeNode) {
                        GroupType parentGroup = ((GroupMutableTreeNode) parentNode).getGroup();
                        GroupHelper.removeSubGroupReference(parentGroup, groupClicked);
                        GroupType selectedGroup = ((GroupMutableTreeNode) selectedNode).getGroup();
                        GroupHelper.markAsDeleted(currentApplication, selectedGroup);
                        treeModel.removeNodeFromParent(selectedNode);
                        treeModel.nodesWereRemoved(parentNode, childIndices, childrenRemoved);
                    } else if (parentNode instanceof RootGroupNode) {
                        logger.debug("Attempted to deleted a root group. Not currently implemented.");
                    }
                } catch (RTClientException ex) {
                    if (groupClicked != null) {
                        logger.error("Unable to delete group " + groupClicked.getTitle(), ex);
                    } else {
                        logger.error("Unable to delete group.");
                    }
                } catch (ClassCastException ex) {
                    logger.error("menu client properties were not storing the right kind of objects!", ex);
                } finally {
                    GlobalUITools.refreshComponent(groupTree);
                }
            }
        });
        menu.add(moveUp);
        menu.add(moveDown);
        menu.addSeparator();
        menu.add(addChild);
        menu.add(delete);
        return menu;
    }

    private TreeSelectionListener getMenuActivationListener(final JMenuItem moveUp, final JMenuItem moveDown) {
        return new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                Object selected = groupTree.getLastSelectedPathComponent();
                if (selected instanceof MutableTreeNode) {
                    MutableTreeNode node = (MutableTreeNode) selected;
                    MutableTreeNode parent = (MutableTreeNode) node.getParent();
                    if (parent == null) {
                        RootGroupNode root = (ManageGroups.RootGroupNode) treeModel.getRoot();
                        parent = root;
                    }
                    int numChildren = parent.getChildCount();
                    int nodeIndex = parent.getIndex(node);
                    if (nodeIndex < 0) {
                        return;
                    }
                    if (nodeIndex == 0) {
                        moveUp.setEnabled(false);
                    } else if (node instanceof RecommendationMutableTreeNode && parent.getChildAt(nodeIndex - 1) instanceof GroupMutableTreeNode) {
                        moveUp.setEnabled(false);
                    } else {
                        moveUp.setEnabled(true);
                    }
                    if (nodeIndex + 1 == numChildren) {
                        moveDown.setEnabled(false);
                    } else if (node instanceof GroupMutableTreeNode && parent.getChildAt(nodeIndex + 1) instanceof RecommendationMutableTreeNode) {
                        moveDown.setEnabled(false);
                    } else {
                        moveDown.setEnabled(true);
                    }
                }
            }
        };
    }

    private JMenuItem getMoveDownItem() {
        JMenuItem moveDown = new JMenuItem("Move Down");
        moveDown.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MutableTreeNode selectedNode = getSelectedNode();
                MutableTreeNode parentNode = (MutableTreeNode) selectedNode.getParent();
                if (parentNode == null) {
                    logger.debug("moving root groups....");
                    int firstNodeIndex = rootNode.getIndex(selectedNode);
                } else {
                    int selectedNodeIndex = parentNode.getIndex(selectedNode);
                    int youngerNodeIndex = selectedNodeIndex + 1;
                    MutableTreeNode youngerSibling = (MutableTreeNode) parentNode.getChildAt(youngerNodeIndex);
                    if (parentNode instanceof RootGroupNode) {
                        ((RootGroupNode) parentNode).swap(selectedNode, youngerSibling);
                    } else if (parentNode instanceof GroupMutableTreeNode) {
                        ((GroupMutableTreeNode) parentNode).swap(youngerSibling, selectedNode);
                    }
                }
                treeModel.nodeStructureChanged(parentNode);
                GlobalUITools.refreshComponent(groupTree);
            }
        });
        return moveDown;
    }

    private JMenuItem getMoveUpItem() {
        JMenuItem moveUp = new JMenuItem("Move Up");
        moveUp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MutableTreeNode selectedNode = getSelectedNode();
                MutableTreeNode parentNode = (MutableTreeNode) selectedNode.getParent();
                if (parentNode == null) {
                    logger.debug("moving root groups....");
                    int firstNodeIndex = rootNode.getIndex(selectedNode);
                } else {
                    int selectedNodeIndex = parentNode.getIndex(selectedNode);
                    int elderSiblingIndex = selectedNodeIndex - 1;
                    MutableTreeNode elderSibling = (MutableTreeNode) parentNode.getChildAt(elderSiblingIndex);
                    if (parentNode instanceof GroupMutableTreeNode) {
                        ((GroupMutableTreeNode) parentNode).swap(elderSibling, selectedNode);
                    } else if (parentNode instanceof RootGroupNode) {
                        ((RootGroupNode) parentNode).swap(elderSibling, selectedNode);
                    }
                }
                storeExpansionState();
                treeModel.nodeStructureChanged(parentNode);
                GlobalUITools.refreshComponent(groupTree);
                restoreExpansionState();
            }
        });
        return moveUp;
    }

    /**
     * Creates a new JMenuItem for viewing a recommendation
     * 
     * @return
     */
    private JMenuItem createViewRuleMenuItem() {
        JMenuItem item = new JMenuItem("View Rule...");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                doViewRule();
            }
        });
        return item;
    }

    /**
     * Creates a new JMenuItem for editing a recommendation
     * 
     * @return
     */
    private JMenuItem createEditRuleMenuItem() {
        JMenuItem item = new JMenuItem("Edit Rule...");
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    ApplicationType app = MetaManager.getMainWindow().getApplicationBar().getApplication(), tmpApp = ApplicationType.Factory.newInstance();
                    tmpApp.set(app);
                    MutableTreeNode selectedNode = getSelectedNode();
                    if (selectedNode instanceof RecommendationMutableTreeNode) {
                        RecommendationReferenceType recRef = ((RecommendationMutableTreeNode) selectedNode).getRecommendation();
                        RecommendationType tmpRec = RecommendationHelper.getRecommendationTypeForRecommendation(tmpApp, recRef.getStringValue());
                        AddEditRuleMainDialog editRecWin = new AddEditRuleMainDialog(MetaManager.getMainWindow(), true, app, tmpApp, true, tmpRec);
                        editRecWin.setVisible(true);
                        storeExpansionState();
                        GlobalUITools.refreshComponent(groupTree);
                        GlobalUITools.refreshComponent(myRecPanel);
                        restoreExpansionState();
                    }
                } catch (Exception ex) {
                    logger.debug("Error while responding to edit right click", ex);
                }
            }
        });
        return item;
    }

    public void doViewRule() {
        try {
            ApplicationType app = MetaManager.getMainWindow().getApplicationBar().getApplication();
            MutableTreeNode selectedNode = getSelectedNode();
            if (selectedNode instanceof RecommendationMutableTreeNode) {
                RecommendationReferenceType recRef = ((RecommendationMutableTreeNode) selectedNode).getRecommendation();
                RecommendationType rec = RecommendationHelper.getRecommendationTypeForRecommendation(app, recRef.getStringValue());
                File outputHtml = RecommendationHelper.applyViewRecommendationXsl(currentApplication, rec);
                if (Desktop.isDesktopSupported()) {
                    logger.debug("Displaying via browser");
                    Desktop desktop = Desktop.getDesktop();
                    String path = "file:///" + outputHtml.getCanonicalPath();
                    URL uri = new URL(path);
                    desktop.browse(uri.toURI());
                } else {
                    logger.debug("Displaying via dialog");
                    String title = "View Rule: " + RecommendationHelper.getUserFriendlyId(app, rec);
                    ViewHtmlDialog recDialog = new ViewHtmlDialog(MetaManager.getMainWindow(), true, title, outputHtml);
                    recDialog.setVisible(true);
                }
            }
        } catch (Exception ex) {
            logger.fatal("Unhandled exception while viewing a Rule.", ex);
        }
    }

    /**
     * 
     * @return the popup menu to display when the user right clicks on the tree
     */
    private JPopupMenu createTreeContextMenu() {
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem addTopLevelGroup = new JMenuItem("New Top-Level Group");
        addTopLevelGroup.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                GroupType newGroup = ApplicationHelper.getNewGroup(currentApplication);
                newGroup.setTitle("New Group");
                newGroup.setDescription("");
                newGroup.setRootOrderNum(BigInteger.ONE);
                int newChildIndex = ((RootGroupNode) treeModel.getRoot()).getChildCount();
                newGroup.setRootOrderNum(new BigInteger(Integer.toString(newChildIndex + 1)));
                logger.debug("gave new group order num " + new BigInteger(Integer.toString(newChildIndex + 1)));
                MutableTreeNode newChild = new GroupMutableTreeNode(newGroup);
                treeModel.insertNodeInto(newChild, (RootGroupNode) treeModel.getRoot(), newChildIndex);
            }
        });
        popupMenu.add(addTopLevelGroup);
        return popupMenu;
    }

    /**
     * 
     * @return the popup menu for <code>RecommendationMutableTreeNode</code>s
     */
    private JPopupMenu createRecommendationContextMenu() {
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem moveUp = getMoveUpItem();
        JMenuItem moveDown = getMoveDownItem();
        popupMenu.add(moveUp);
        popupMenu.add(moveDown);
        popupMenu.addSeparator();
        popupMenu.add(this.createEditRuleMenuItem());
        popupMenu.add(this.createViewRuleMenuItem());
        groupTree.addTreeSelectionListener(getMenuActivationListener(moveUp, moveDown));
        JMenuItem delete = new JMenuItem("Remove");
        delete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    GroupType parent;
                    RecommendationType toDelete;
                    TreePath pathClicked = getSelectedPath();
                    RecommendationMutableTreeNode nodeClicked = ((RecommendationMutableTreeNode) pathClicked.getLastPathComponent());
                    GroupMutableTreeNode parentNode = (GroupMutableTreeNode) pathClicked.getParentPath().getLastPathComponent();
                    parent = parentNode.getGroup();
                    RecommendationReferenceType recRef = nodeClicked.getRecommendation();
                    toDelete = RecommendationHelper.getRecommendationTypeForRecommendation(currentApplication, recRef.getStringValue());
                    GroupHelper.removeRecommendationReferenceFromGroup(parent, recRef.getStringValue());
                    treeModel.removeNodeFromParent(nodeClicked);
                    myRecPanel.putRecommendationInList(toDelete);
                } catch (DataManagerException ex) {
                    logger.error("Unable to delete node", ex);
                } catch (RTClientException ex) {
                    logger.error("Unable to delete node", ex);
                } finally {
                    storeExpansionState();
                    GlobalUITools.refreshComponent(groupTree);
                    GlobalUITools.refreshComponent(myRecPanel);
                    restoreExpansionState();
                }
            }
        });
        popupMenu.add(delete);
        return popupMenu;
    }

    /**
     * Brings the {@link org.mitre.rt.ui.recommendations.AddEditRecommendationsPanel} 
     * to the front hiding any {@link org.mitre.rt.ui.groups.GroupMetadatPanel}s 
     * that may exist.
     */
    public void displayRecPanel() {
        rightPanel.remove(myGroupPanel);
        rightPanel.add(myRecPanel, BorderLayout.CENTER);
        rightPanel.validate();
        rightPanel.repaint();
    }

    /**
     * Creates the root node of the tree containing all of the top-level groups 
     * in this application.
     * @return
     */
    private MutableTreeNode getRootNode() {
        GroupType[] rootGroups = ApplicationHelper.getRootGroups(currentApplication);
        RootGroupNode rootNode = new RootGroupNode(rootGroups);
        logger.debug(rootNode.toString());
        return rootNode;
    }

    /**
     * Brings the {@link org.mitre.rt.ui.groups.GroupMetadataPanel} for 
     * <code>groupToDisplay</code> to the front, hiding any {@link org.mitre.rt.ui.recommendations.AddEditRecommendationsPanel}s 
     * that may exist.
     * @param groupToDisplay
     */
    public void displayGroupMetadataPanel(GroupType groupToDisplay) {
        if (groupToDisplay == null) {
            MutableTreeNode selectedNode = getSelectedNode();
            if (selectedNode instanceof GroupMutableTreeNode) {
                myGroupPanel.displayGroup(((GroupMutableTreeNode) selectedNode).getGroup());
            }
        } else if (!groupToDisplay.equals(myGroupPanel.getGroup())) {
            myGroupPanel.displayGroup(groupToDisplay);
        }
        rightPanel.remove(myRecPanel);
        rightPanel.add(myGroupPanel);
        rightPanel.validate();
        rightPanel.repaint();
    }

    /**
     * Groups do not come prearranged in a hierarchy.  Each group knows which other 
     * groups are its children, but they exist in a flat structure.  This class is 
     * the fake "root group" that contains all of the top-level groups for a given 
     * aplication.
     */
    public class RootGroupNode extends DefaultMutableTreeNode {

        @SuppressWarnings("unchecked")
        public RootGroupNode(GroupType[] rootGroups) {
            children = new Vector<GroupMutableTreeNode>();
            for (GroupType child : rootGroups) {
                GroupMutableTreeNode childNode = new GroupMutableTreeNode(child);
                children.add(childNode);
            }
            Collections.sort(children, new Comparator<GroupMutableTreeNode>() {

                @Override
                public int compare(GroupMutableTreeNode o1, GroupMutableTreeNode o2) {
                    GroupType firstGroup = o1.getGroup();
                    GroupType secondGroup = o2.getGroup();
                    BigInteger g1Num = firstGroup.getRootOrderNum();
                    BigInteger g2Num = secondGroup.getRootOrderNum();
                    if (g1Num == null) {
                        return -1;
                    }
                    if (g2Num == null) {
                        return 1;
                    }
                    int orderNumCompare = g1Num.compareTo(g2Num);
                    if (orderNumCompare == 0) {
                        return firstGroup.getModified().compareTo(secondGroup.getModified());
                    } else {
                        return orderNumCompare;
                    }
                }
            });
        }

        /**
         * Swaps the position of two nodes in the tree and updates their 
         * order numbers.
         * @param firstNode
         * @param secondNode
         */
        @SuppressWarnings("unchecked")
        public void swap(MutableTreeNode firstNode, MutableTreeNode secondNode) {
            int firstNodeIndex = getIndex(firstNode);
            int secondNodeIndex = getIndex(secondNode);
            if (firstNodeIndex == -1 || secondNodeIndex == -1) {
                throw new IllegalArgumentException("Can only swap children of this node.");
            }
            GroupType firstGroup = ((GroupMutableTreeNode) firstNode).getGroup();
            GroupType secondGroup = ((GroupMutableTreeNode) secondNode).getGroup();
            VersionedItemTypeHelper.markModified(firstGroup);
            VersionedItemTypeHelper.markModified(secondGroup);
            logger.debug("first group: " + firstGroup.getTitle() + " has order num " + firstGroup.getRootOrderNum());
            logger.debug("second group: " + secondGroup.getTitle() + " has order num " + secondGroup.getRootOrderNum());
            BigInteger firstGroupOrderNum = firstGroup.getRootOrderNum();
            BigInteger secondGroupOrderNum = secondGroup.getRootOrderNum();
            logger.debug(firstGroupOrderNum + "/" + secondGroupOrderNum);
            firstGroup.setRootOrderNum(secondGroupOrderNum);
            secondGroup.setRootOrderNum(firstGroupOrderNum);
            children.set(firstNodeIndex, (GroupMutableTreeNode) secondNode);
            children.set(secondNodeIndex, (GroupMutableTreeNode) firstNode);
        }

        @Override
        public void setUserObject(Object object) {
            throw new UnsupportedOperationException("Root node does not really represent anything");
        }

        @Override
        public void removeFromParent() {
            throw new UnsupportedOperationException("Root node has no parent.");
        }

        @Override
        public void setParent(MutableTreeNode newParent) {
            throw new UnsupportedOperationException("Root node has no parent");
        }

        @Override
        public boolean isLeaf() {
            return false;
        }
    }

    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        rightPanel = new javax.swing.JPanel();
        groupTree = new javax.swing.JTree();
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 100, Short.MAX_VALUE));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 100, Short.MAX_VALUE));
        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setMaximumSize(new java.awt.Dimension(500, 32767));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(100, 23));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 322));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new java.awt.Dimension(800, 600));
        setLayout(new java.awt.BorderLayout());
        rightPanel.setPreferredSize(new java.awt.Dimension(600, 500));
        rightPanel.setLayout(new java.awt.BorderLayout());
        add(rightPanel, java.awt.BorderLayout.CENTER);
        groupTree.setScrollsOnExpand(true);
        groupTree.setDragEnabled(true);
        groupTree.setMaximumSize(new java.awt.Dimension(500, 64));
        groupTree.setMinimumSize(new java.awt.Dimension(150, 0));
        groupTree.setPreferredSize(new java.awt.Dimension(195, 64));
        groupTree.setShowsRootHandles(true);
        add(groupTree, java.awt.BorderLayout.WEST);
    }

    private javax.swing.JTree groupTree;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JPanel rightPanel;
}

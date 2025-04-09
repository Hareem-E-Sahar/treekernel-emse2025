package org.formaria.editor.visualizer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.JOptionPane;
import org.formaria.editor.project.EditorProject;
import org.formaria.editor.project.EditorProjectManager;
import org.formaria.editor.project.pages.IEditorUtility;
import org.formaria.aria.ProjectManager;
import org.formaria.aria.data.BaseModel;
import org.formaria.aria.data.DataModel;
import org.formaria.editor.ui.AriaToolbar;
import java.beans.PropertyChangeEvent;
import java.awt.event.ActionEvent;

/**
 * The main panel of the data model visualization.
 * <p> Copyright (c) Formaria Ltd., 2001-2006, This software is licensed under
 * the GNU Public License (GPL), please see license.txt for more details. If
 * you make commercial use of this software you must purchase a commercial
 * license from Formaria.</p>
 * <p> $Revision: 1.18 $</p>
 */
public class ModelVisualiserPanel extends JPanel implements TreeSelectionListener, MouseListener {

    private NewNodeDialog newNodeDialog;

    private JTabbedPane tabbedPane;

    private JPanel leftPane;

    private JPanel rightPane;

    private JPanel visualizerPanel;

    private ModelVisualiserTree visualizerTreel;

    private JSplitPane splitPane = null;

    private AriaToolbar visualizerToolbar;

    private AriaToolbar treeToolbar;

    private DataModel rootModel;

    private StructureTable structureTable;

    private VisualiserDebuggerModel debuggerModel;

    private VisualiserDebuggerEngine debuggerEngine;

    private boolean debugView;

    private String projectPath;

    private EditorProject currentProject;

    private JButton editAttributeBtn, copyAttrToClipboardBtn;

    private JButton addAttributeBtn, deleteAttributeBtn;

    private JButton refreshBtn, copyNodeToClipboardBtn;

    private JButton deleteNodeBtn, renameNodeBtn, addNodeBtn;

    private ActionListener leftPaneListener, rightPaneListener;

    public ModelVisualiserPanel(int preferredWidth, VisualiserDebuggerEngine dEngine) {
        debuggerEngine = dEngine;
        currentProject = (EditorProject) EditorProjectManager.getCurrentProject();
        debugView = (debuggerEngine != null);
        leftPaneListener = new LeftPaneActionListener();
        rightPaneListener = new RightPaneActionListener();
        leftPane = new JPanel();
        leftPane.setLayout(new BorderLayout());
        leftPane.setBorder(BorderFactory.createEmptyBorder());
        visualizerTreel = new ModelVisualiserTree(this);
        visualizerTreel.addTreeSelectionListener(this);
        visualizerTreel.expandRoot();
        visualizerTreel.getTreeComponent().addMouseListener(this);
        visualizerTreel.setPreferredSize(new Dimension(preferredWidth, 200));
        visualizerTreel.setMinimumSize(new Dimension(preferredWidth / 2, 100));
        treeToolbar = new AriaToolbar();
        refreshBtn = treeToolbar.addTool(leftPaneListener, "refresh.gif", "Refresh", "Refresh the model");
        treeToolbar.addSeparator();
        addNodeBtn = treeToolbar.addTool(leftPaneListener, "newModelNode.gif", "Add...", "Add a new node to the model");
        deleteNodeBtn = treeToolbar.addTool(leftPaneListener, "deleteModelNode.gif", "Delete", "Delete the selected node from the model");
        deleteNodeBtn.setEnabled(false);
        treeToolbar.addSeparator();
        renameNodeBtn = treeToolbar.addTool(leftPaneListener, "renameModelNode.gif", "Rename...", "Rename the model node");
        renameNodeBtn.setEnabled(false);
        treeToolbar.addSeparator();
        copyNodeToClipboardBtn = treeToolbar.addTool(leftPaneListener, "modelPathToClipboard.gif", "Copy path to clipboard", "Copy the model path to the clipboard");
        treeToolbar.addSeparator();
        leftPane.add(treeToolbar, BorderLayout.NORTH);
        leftPane.add(visualizerTreel, BorderLayout.CENTER);
        rightPane = new JPanel();
        rightPane.setLayout(new BorderLayout());
        rightPane.setBorder(BorderFactory.createEmptyBorder());
        tabbedPane = new JTabbedPane();
        visualizerPanel = new JPanel();
        visualizerPanel.setLayout(new BorderLayout());
        structureTable = new StructureTable();
        visualizerToolbar = new AriaToolbar();
        editAttributeBtn = visualizerToolbar.addTool(rightPaneListener, "editModelNode.gif", "Edit...", "Edit the attribute");
        editAttributeBtn.setEnabled(false);
        if (!debugView) {
            visualizerToolbar.addSeparator();
            addAttributeBtn = visualizerToolbar.addTool(rightPaneListener, "addAttribute.gif", "Add Attribute...", "Add a new attribute to the model node");
            addAttributeBtn.setEnabled(false);
            deleteAttributeBtn = visualizerToolbar.addTool(rightPaneListener, "deleteAttribute.gif", "Delete Attribute", "Delete the selected attribute from the model node");
            deleteAttributeBtn.setEnabled(false);
        }
        visualizerToolbar.addSeparator();
        copyAttrToClipboardBtn = visualizerToolbar.addTool(rightPaneListener, "modelPathToClipboard.gif", "Copy path to clipboard", "Copy the model path to the clipboard");
        copyAttrToClipboardBtn.setEnabled(false);
        rightPane.add(visualizerToolbar, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(structureTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        rightPane.add(scrollPane, BorderLayout.CENTER);
        tabbedPane.add("structure", rightPane);
        tabbedPane.add("visualizer", visualizerPanel);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, tabbedPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(6);
        splitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI());
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
        splitPane.setDividerLocation(0.3);
        if (debugView) debuggerEngine.setupDebuggerInterface(this);
        newNodeDialog = new NewNodeDialog(null);
    }

    public ModelVisualiserPanel(int preferredWidth) {
        this(preferredWidth, null);
    }

    public boolean debugView() {
        return debugView;
    }

    /**
   * Set the root mode node displayed by this visualizer
   * @param root the root of the node hierarchy
   */
    public void setRootModel(DataModel root) {
        rootModel = root;
        structureTable.setModelNode(root);
        refresh(null);
    }

    /**
   * Set a message in an empty tree
   * @param message the new message text
   */
    public void addMessageNode(String message) {
        visualizerTreel.addMessageNode(message);
    }

    /**
   * Set listener for the tree
   * @param the listener
   */
    public void setModelTreeListener(ModelTreeListener mtl) {
        visualizerTreel.setModelTreeListner(mtl);
    }

    /**
   * Set the selected path in the tree
   * @param the selection path
   */
    public void setSelectedPath(String path) {
        visualizerTreel.setSelectedPath(path);
    }

    /**
   * Get the splitpane
   * @return the split pane
   */
    protected JSplitPane getSplitPane() {
        return splitPane;
    }

    /**
   * Get the model tree
   * @return the tree
   */
    public ModelVisualiserTree getModelVisualiserTree() {
        return visualizerTreel;
    }

    /**
   * Update the table following a tree selection change
   */
    public void valueChanged(TreeSelectionEvent evt) {
        if (rootModel == null) return;
        String path = getTreePath();
        structureTable.setModelNode(null);
        boolean dbTableNodeSelected = isDbTableNodeSelected();
        boolean buttState = (path.length() > 0) && (!dbTableNodeSelected);
        if (addNodeBtn != null) addNodeBtn.setEnabled(!dbTableNodeSelected);
        if (deleteNodeBtn != null) deleteNodeBtn.setEnabled(path.length() > 0);
        if (renameNodeBtn != null) renameNodeBtn.setEnabled(buttState);
        if (addAttributeBtn != null) addAttributeBtn.setEnabled(buttState);
        DataModel modelNode = null;
        if (path.length() > 0) {
            try {
                modelNode = (DataModel) rootModel.get(path);
                if (modelNode == null) return;
            } catch (ClassCastException ex) {
                ex.printStackTrace();
            }
            visualizerPanel.removeAll();
            Component comp = ModelVisualizerFactory.getVisualizer(modelNode, this);
            visualizerPanel.add(comp, BorderLayout.CENTER);
            visualizerPanel.repaint();
            structureTable.setModelNode(modelNode);
        }
    }

    /**
   * Get the path of the selected treenode by looping the selected path array
   * and placing a '/' between each item
   * @return the path to the selected DataModel
   */
    protected String getTreePath() {
        String modelName = null;
        TreePath selectedPath = visualizerTreel.getSelectionPath();
        if (selectedPath != null) {
            Object path[] = selectedPath.getPath();
            modelName = "";
            for (int i = 1; i < path.length; i++) {
                String pathEle = path[i].toString();
                pathEle = pathEle.substring(0, pathEle.indexOf(": "));
                String value = path[i].toString();
                value = value.substring(value.indexOf(": ") + 2, value.length());
                modelName += "/" + pathEle;
            }
        }
        if (modelName == null) return ""; else if (modelName.trim().compareTo("") != 0) return modelName.substring(1); else return "";
    }

    /**
   * Centre the panel on screen.
   */
    public void centerScreen(Rectangle parentRect) {
        double x = parentRect.getWidth() - getSize().getWidth();
        double y = parentRect.getHeight() - getSize().getHeight();
        setLocation((int) x, (int) y);
    }

    /**
   * left tree actions listener
   */
    private class LeftPaneActionListener implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
            String path = visualizerTreel.getStrippedPath();
            String cmd = ae.getActionCommand();
            if (cmd.equals("Refresh")) {
                visualizerTreel.refresh();
            } else if (cmd.equals("Add...")) {
                if (newNodeDialog.showDialog(path)) {
                    if (newNodeDialog.getType() == NewNodeDialog.TABLEMODEL) {
                        int ncols = newNodeDialog.getNumCols();
                        int nrows = newNodeDialog.getNumRows();
                        String name = newNodeDialog.getTableName();
                        addTable(path, name, nrows, ncols);
                    } else if (newNodeDialog.getType() == NewNodeDialog.BASEMODEL) {
                        String name = newNodeDialog.getSingleNodeName();
                        addNode(path, name);
                        path += ("/" + name);
                    }
                }
            } else if (cmd.equals("Delete")) {
                int res = JOptionPane.showConfirmDialog(leftPane, "Are you sure you want to delete: " + path, "Delete node", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (res == JOptionPane.OK_OPTION) deleteNode(path);
            } else if (cmd.equals("Rename...")) {
                String name = JOptionPane.showInputDialog(leftPane, "Rename: " + path, "Enter the new name for the node", JOptionPane.QUESTION_MESSAGE);
                if (name == null) return;
                renameNode(path, name);
            } else if (cmd.equals("Copy path to clipboard")) {
                copyNodePathToClipboard();
                return;
            } else return;
            refresh(path);
        }
    }

    /**
   * right table actions listener
   */
    private class RightPaneActionListener implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
            String cmd = ae.getActionCommand();
            String path = visualizerTreel.getStrippedPath();
            if (cmd.equals("Edit...")) {
                int idx = structureTable.getSelectedRow();
                if (idx >= 0) {
                    String attribName = (String) structureTable.getValueAt(idx, 0);
                    String value = JOptionPane.showInputDialog(tabbedPane, "Value for: " + path + "@" + attribName, "Enter the new node value", JOptionPane.QUESTION_MESSAGE);
                    if (value == null) return;
                    setNodeValue(path, attribName, value);
                } else JOptionPane.showMessageDialog(tabbedPane, "Please select the attribute you wish to edit.", "Cannot edit the node value", JOptionPane.WARNING_MESSAGE);
            } else if (cmd.equals("Add Attribute...")) {
                String name = JOptionPane.showInputDialog(tabbedPane, "Add attribute: " + path, "Enter the new attribute name", JOptionPane.OK_CANCEL_OPTION);
                if (name == null) return;
                addAttribute(path, name);
            } else if (cmd.equals("Delete Attribute")) {
                int idx = structureTable.getSelectedRow();
                String attribName = (String) structureTable.getValueAt(idx, 0);
                int res = JOptionPane.showConfirmDialog(tabbedPane, "Are you sure you want to delete the attribute: " + attribName, "Delete attribute", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (res == JOptionPane.OK_OPTION) deleteAttribute(path, attribName); else return;
            } else if (cmd.equals("Copy path to clipboard")) {
                copyAttrPathToClipboard();
                return;
            } else return;
            refresh(path);
        }
    }

    /**
   * The mouse was clicked on this component
   * @param me the mouse event
   */
    public void mouseClicked(MouseEvent me) {
    }

    private void popupTriggered(MouseEvent me) {
        if (me.isPopupTrigger()) {
            JPopupMenu popupMenu = new JPopupMenu("Refresh");
            if (visualizerTreel.getSelectionPath() != null) {
                JMenuItem mi = new JMenuItem("Refresh");
                mi.addActionListener(leftPaneListener);
                popupMenu.add(mi);
                popupMenu.addSeparator();
                mi = new JMenuItem("Add...");
                mi.addActionListener(leftPaneListener);
                popupMenu.add(mi);
                if (visualizerTreel.getSelectionPath().getPathCount() > 0) {
                    mi = new JMenuItem("Delete");
                    mi.addActionListener(leftPaneListener);
                    popupMenu.add(mi);
                    popupMenu.addSeparator();
                    mi = new JMenuItem("Rename...");
                    mi.addActionListener(leftPaneListener);
                    popupMenu.add(mi);
                    popupMenu.addSeparator();
                    mi = new JMenuItem("Copy path to clipboard");
                    mi.addActionListener(leftPaneListener);
                    popupMenu.add(mi);
                }
                Point pt = me.getPoint();
                Point vp = visualizerTreel.getLocation();
                popupMenu.show(this, pt.x + vp.x, pt.y + vp.y);
            }
        }
    }

    /**
   * The mouse button was pressed while over this component
   * @param me the mouse event
   */
    public void mousePressed(MouseEvent me) {
        popupTriggered(me);
    }

    /**
   * Responds to a mouse button release by poping up a context menu for the component
   * @param me
   */
    public void mouseReleased(MouseEvent me) {
        popupTriggered(me);
    }

    /**
   * The mouse entered this component's area
   * @param me the mouse event
   */
    public void mouseEntered(MouseEvent me) {
    }

    /**
   * The mouse exited this component's area
   * @param me the mouse event
   */
    public void mouseExited(MouseEvent me) {
    }

    /**
   * Refresh the model representation
   * @param selectedPath the selected path in the tree or null for no sleection
   */
    public synchronized void refresh(String selectedPath) {
        if (debugView && (debuggerModel == null)) debuggerEngine.setupDebuggerInterface(this);
        if (debugView) visualizerTreel.setRootModel(debuggerModel); else visualizerTreel.setRootModel(ProjectManager.getCurrentProject().getModel());
        visualizerTreel.removeTreeSelectionListener(this);
        visualizerTreel.removeMouseListener(this);
        visualizerTreel.createTreeComp(null);
        visualizerTreel.addTreeSelectionListener(this);
        visualizerTreel.addMouseListener(this);
        if (selectedPath != null) {
            visualizerTreel.setSelectedPath(selectedPath);
        }
    }

    /**
   * Copies selected node path to the clipboard
   */
    public void copyNodePathToClipboard() {
        String selection = visualizerTreel.getStrippedPath();
        if (selection != null) {
            IEditorUtility editorUtility = currentProject.getEditorUtility();
            editorUtility.copyToClipboard(selection);
        }
    }

    /**
   * Copies selected attribute path to the clipboard
   */
    public void copyAttrPathToClipboard() {
        String selection = visualizerTreel.getStrippedPath();
        int row = structureTable.getSelectedRow();
        if (row != -1) {
            String attribute = (String) structureTable.getValueAt(row, 0);
            if (attribute != null) {
                selection += "/" + attribute;
                IEditorUtility editorUtility = currentProject.getEditorUtility();
                editorUtility.copyToClipboard(selection);
            }
        }
    }

    /**
   * Determines whether selected node is a database table node
   */
    protected boolean isDbTableNodeSelected() {
        String selection = visualizerTreel.getStrippedPath();
        DataModel model = (DataModel) rootModel.get(selection);
        return ModelVisualizerFactory.isDbTableModel(model);
    }

    /**
   * Add an attribute to the model
   * @param path the selected path
   * @param attribName the new attribute name
   */
    public void addAttribute(String path, String attribName) {
        if (path.length() > 0) {
            DataModel model = (DataModel) rootModel.get(path);
            model.set("@" + attribName, "");
            currentProject.setModified(true);
        }
    }

    /**
   * Delete the selected attribute from the model
   */
    public void deleteAttribute(String path, String attribName) {
        if (path.length() > 0) {
            if (attribName.equals("value") || attribName.equals("id")) {
                JOptionPane.showMessageDialog(tabbedPane, attribName + " is a required attribute ", "Cannot delete the attribute", JOptionPane.WARNING_MESSAGE);
                return;
            }
            DataModel node = (DataModel) rootModel.get(path);
            if (node instanceof BaseModel) {
                if (attribName != null) {
                    int idx = node.getAttribute(attribName);
                    ((BaseModel) node).setAttribValue(idx, null, null);
                    currentProject.setModified(true);
                }
            }
        }
    }

    /**
   * Add a new node to the model
   * @param path the path of the parent node
   * @param name the name/ID of the new node
   */
    public void addNode(String path, String name) {
        DataModel model = (DataModel) (path.length() > 0 ? rootModel.get(path) : rootModel);
        DataModel newNode = (DataModel) model.append(name);
        newNode.setTagName("data");
        currentProject.setModified(true);
    }

    /**
   * Add a new table model to the model
   * @param path the path of the parent node
   * @param name the name/ID of the new node
   */
    public void addTable(String path, String name, int nrows, int ncols) {
        DataModel model = (DataModel) (path.length() > 0 ? rootModel.get(path) : rootModel);
        DataModel tableNode = (DataModel) model.append(name);
        tableNode.setTagName("table");
        DataModel headerNode = (DataModel) tableNode.append(name + " header");
        headerNode.setTagName("th");
        for (int i = 1; i <= ncols; i++) {
            DataModel columnNode = (DataModel) headerNode.append(String.valueOf(i));
            columnNode.set("col " + i);
            columnNode.setTagName("data");
        }
        for (int i = 1; i <= nrows; i++) {
            DataModel rowNode = (DataModel) tableNode.append(String.valueOf(i));
            rowNode.setTagName("tr");
            rowNode.set("");
            for (int j = 1; j <= ncols; j++) {
                DataModel ceilNode = (DataModel) rowNode.append(String.valueOf(j));
                ceilNode.setTagName("td");
                ceilNode.set("");
            }
        }
        currentProject.setModified(true);
    }

    /**
   * Deletes selected node from the model
   * @param path the path of the model node
   */
    public void deleteNode(String path) {
        if (path.length() > 0) {
            int pos = path.lastIndexOf('/');
            if (pos > 0) {
                DataModel model = (DataModel) rootModel.get(path.substring(0, pos));
                if (model instanceof BaseModel) ((BaseModel) model).removeChild(path.substring(pos + 1)); else if (model instanceof VisualiserDebuggerModel) ((VisualiserDebuggerModel) model).removeChild(path.substring(pos + 1));
            } else {
                if (rootModel instanceof BaseModel) {
                    ((BaseModel) rootModel).removeChild(path);
                } else if (rootModel instanceof VisualiserDebuggerModel) {
                    ((VisualiserDebuggerModel) rootModel).removeChild(path);
                }
            }
            currentProject.setModified(true);
        }
    }

    /**
   * Reset the node's ID
   * @param path the path of the parent node
   * @param name the name/ID of the new node
   */
    public void renameNode(String path, String name) {
        if (path == null) return;
        DataModel model = (DataModel) rootModel.get(path);
        model.setAttribValue(BaseModel.ID_ATTRIBUTE, name);
        currentProject.setModified(true);
    }

    /**
   * Reset the node's value
   * @param path the path of the parent node
   * @param name the name/ID of the new node
   */
    public void setNodeValue(String path, String attribName, String value) {
        if (path.length() > 0) {
            DataModel model = (DataModel) rootModel.get(path);
            int attribIdx = model.getAttribute(attribName);
            model.setAttribValue(attribIdx, value);
            if (model.getAttribValue(attribIdx) == value) currentProject.setModified(true);
        }
    }

    public VisualiserDebuggerModel getDebuggerModel() {
        return debuggerModel;
    }

    public void setDebuggerModel(VisualiserDebuggerModel dModel) {
        debuggerModel = dModel;
    }

    public DataModel getRootModel() {
        return rootModel;
    }

    public void setProjectPath(String ppath) {
        projectPath = ppath;
    }

    public String getProjectPath() {
        return projectPath;
    }

    /**
   *  This method gets called when a bound property is changed
   */
    public void propertyChange(PropertyChangeEvent evt) {
    }

    class NewNodeDialog extends JDialog {

        static final int BASEMODEL = 1;

        static final int TABLEMODEL = 2;

        private int type;

        private JPanel topPanel, centerPanel, bottomPanel;

        private JPanel baseModelPanel, tableModelPanel;

        private JTextField baseModelName;

        private JSpinner spinnCols, spinnRows;

        private JLabel tableNameLbl, singleNodeLbl;

        private JTextField tableModelName;

        private JComboBox typeCombo;

        private JButton buttOk, buttCancel;

        private String[] types = { "single node", "table model" };

        private CardLayout cardLayout;

        private boolean okPressed;

        private String path;

        NewNodeDialog(JFrame parent) {
            super(parent);
            path = "";
            okPressed = false;
            type = BASEMODEL;
            setModal(true);
            setLayout(new BorderLayout());
            createBaseModelPanel();
            createTableModelPanel();
            typeCombo = new JComboBox(types);
            typeCombo.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    String type = typeCombo.getSelectedItem().toString();
                    if (type.equals("single node")) showBaseModel(); else if (type.equals("table model")) showTableModel();
                }
            });
            topPanel = new JPanel();
            topPanel.setLayout(new BorderLayout());
            TitledBorder tb1 = BorderFactory.createTitledBorder("choose model type");
            typeCombo.setBorder(tb1);
            topPanel.add(typeCombo, BorderLayout.CENTER);
            add(topPanel, BorderLayout.NORTH);
            cardLayout = new CardLayout();
            centerPanel = new JPanel();
            centerPanel.setLayout(cardLayout);
            centerPanel.add(baseModelPanel, "baseModel");
            centerPanel.add(tableModelPanel, "tableModel");
            add(centerPanel, BorderLayout.CENTER);
            Dimension buttSize = new Dimension(100, 20);
            buttCancel = new JButton("cancel");
            buttCancel.setSize(buttSize);
            buttCancel.setPreferredSize(buttSize);
            buttCancel.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    okPressed = false;
                    setVisible(false);
                }
            });
            buttOk = new JButton("ok");
            buttOk.setSize(buttSize);
            buttOk.setPreferredSize(buttSize);
            buttOk.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (modelIdSpecified()) {
                        okPressed = true;
                        setVisible(false);
                    } else {
                        JOptionPane.showMessageDialog(NewNodeDialog.this, "Please enter the model ID");
                    }
                }
            });
            bottomPanel = new JPanel();
            bottomPanel.setLayout(new FlowLayout());
            bottomPanel.add(buttCancel);
            bottomPanel.add(buttOk);
            add(bottomPanel, BorderLayout.SOUTH);
            pack();
            setLocation();
        }

        private boolean modelIdSpecified() {
            return ((type == BASEMODEL) && !"".equals(baseModelName.getText()) || (type == TABLEMODEL) && !"".equals(tableModelName));
        }

        String getTableName() {
            return tableModelName.getText();
        }

        int getNumCols() {
            return (new Integer(spinnCols.getValue().toString())).intValue();
        }

        int getNumRows() {
            return (new Integer(spinnRows.getValue().toString())).intValue();
        }

        public String getSingleNodeName() {
            return baseModelName.getText();
        }

        public boolean showDialog(String p) {
            path = p;
            okPressed = false;
            repaint();
            pack();
            setVisible(true);
            return okPressed;
        }

        int getType() {
            return type;
        }

        private void showBaseModel() {
            type = BASEMODEL;
            cardLayout.show(centerPanel, "baseModel");
        }

        private void showTableModel() {
            type = TABLEMODEL;
            cardLayout.show(centerPanel, "tableModel");
        }

        private void createBaseModelPanel() {
            Dimension d = new Dimension(100, 20);
            baseModelName = new JTextField();
            baseModelName.setSize(d);
            baseModelName.setPreferredSize(d);
            baseModelName.setMaximumSize(d);
            baseModelPanel = new JPanel();
            baseModelPanel.setLayout(new FlowLayout());
            TitledBorder tb = BorderFactory.createTitledBorder("add node");
            baseModelPanel.setBorder(tb);
            singleNodeLbl = new JLabel("model id: ");
            baseModelPanel.add(singleNodeLbl);
            baseModelPanel.add(baseModelName);
            pack();
            repaint();
        }

        private void createTableModelPanel() {
            Dimension nameSize = new Dimension(100, 20);
            Dimension spinnSize = new Dimension(30, 20);
            tableModelPanel = new JPanel();
            tableModelPanel.setLayout(new FlowLayout());
            TitledBorder tb = BorderFactory.createTitledBorder("add table");
            tableModelPanel.setBorder(tb);
            spinnCols = new JSpinner();
            spinnCols.setSize(spinnSize);
            spinnCols.setPreferredSize(spinnSize);
            spinnRows = new JSpinner();
            spinnRows.setSize(spinnSize);
            spinnRows.setPreferredSize(spinnSize);
            tableModelName = new JTextField();
            tableModelName.setSize(nameSize);
            tableModelName.setPreferredSize(nameSize);
            tableModelName.setMinimumSize(nameSize);
            tableNameLbl = new JLabel("model id: ");
            tableModelPanel.add(tableNameLbl);
            tableModelPanel.add(tableModelName);
            tableModelPanel.add(new JLabel("columns"));
            tableModelPanel.add(spinnCols);
            tableModelPanel.add(new JLabel("rows"));
            tableModelPanel.add(spinnRows);
            pack();
            repaint();
        }

        private void setLocation() {
            int px = ModelVisualiserPanel.this.getLocation().x;
            int py = ModelVisualiserPanel.this.getLocation().y;
            int pw = ModelVisualiserPanel.this.getSize().width;
            int ph = ModelVisualiserPanel.this.getSize().height;
            int w = getSize().width;
            int h = getSize().height;
            int x = px + (pw - w) / 2;
            int y = py + (ph - h) / 2;
            setLocation(500, 720);
        }
    }
}

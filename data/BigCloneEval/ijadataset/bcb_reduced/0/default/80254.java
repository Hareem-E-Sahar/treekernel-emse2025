import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.text.*;
import java.io.*;

interface Constant {

    public static final String license = "Copyright (c) 2008, solomonson.com\n" + "All rights reserved.\n" + "\n" + " Redistribution and use in source and binary forms, with or without\n" + " modification, are permitted provided that the following conditions are met:\n" + "\n" + "     * Redistributions of source code must retain the above copyright\n" + "       notice, this list of conditions and the following disclaimer.\n" + "     * Redistributions in binary form must reproduce the above copyright\n" + "       notice, this list of conditions and the following disclaimer in the\n" + "       documentation and/or other materials provided with the distribution.\n" + "     * Neither the name of the solomonson.com nor the\n" + "       names of its contributors may be used to endorse or promote products\n" + "       derived from this software without specific prior written permission.\n" + "\n" + " THIS SOFTWARE IS PROVIDED BY solomonson.com ''AS IS'' AND ANY\n" + " EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED\n" + " WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE\n" + " DISCLAIMED. IN NO EVENT SHALL solomonson.com BE LIABLE FOR ANY\n" + " DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES\n" + " (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;\n" + " LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND\n" + " ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n" + " (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS\n" + " SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n";
}

class DiligentFrame extends JFrame {

    protected JTree tree;

    protected DiligentDetails details;

    protected boolean nodeSelectionMode = false;

    protected JFrame frame = this;

    protected Properties prop;

    protected DiligentTreeModel treeModel;

    protected DiligentNode root;

    protected DiligentTableModel tableModel;

    protected JTable table;

    public void storeProperties() {
        try {
            prop.store(new FileOutputStream("diligent.prop"), "diligent");
        } catch (IOException e) {
            System.out.println("Couldn't write .diligent.");
        }
        dispose();
    }

    public DiligentFrame() {
        super("Diligent");
        setIconImage(ResourceManager.createImageIcon("icons/closed.png").getImage());
        prop = new Properties();
        try {
            prop.load(new FileInputStream("diligent.prop"));
        } catch (IOException e) {
            System.out.println("Couldn't open .diligent.");
        }
        root = new DiligentNode("root");
        treeModel = new DiligentTreeModel(root);
        if (prop.getProperty("last", "").equals("")) {
            setFilename("");
        } else {
            open(prop.getProperty("last"));
        }
        tree = new DiligentTree(treeModel);
        tree.addTreeExpansionListener(new TreeExpansionListener() {

            public void treeCollapsed(TreeExpansionEvent e) {
                DiligentNode node = (DiligentNode) e.getPath().getLastPathComponent();
                node.collapse();
            }

            public void treeExpanded(TreeExpansionEvent e) {
                DiligentNode node = (DiligentNode) e.getPath().getLastPathComponent();
                node.expand();
            }
        });
        treeModel.addTreeModelListener(new TreeModelListener() {

            public void treeNodesChanged(TreeModelEvent e) {
                DiligentNode selected = (DiligentNode) tree.getLastSelectedPathComponent();
                if (selected != null) selected.setName((String) selected.getUserObject());
                details.requery();
                tree.requestFocusInWindow();
            }

            public void treeNodesInserted(TreeModelEvent e) {
                DiligentNode selected = (DiligentNode) tree.getLastSelectedPathComponent();
                if (selected != null) selected.dirty(true);
            }

            public void treeNodesRemoved(TreeModelEvent e) {
                DiligentNode selected = (DiligentNode) tree.getLastSelectedPathComponent();
                if (selected != null) selected.dirty(true);
            }

            public void treeStructureChanged(TreeModelEvent e) {
                DiligentNode selected = (DiligentNode) tree.getLastSelectedPathComponent();
                if (selected != null) selected.dirty(true);
            }
        });
        final JPopupMenu popup = new JPopupMenu();
        JMenu submenu = new JMenu("Status");
        JMenuItem menuItem = new JMenuItem("open", ResourceManager.createImageIcon("icons/open.png"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (tree.getSelectionPath() != null) {
                    DiligentNode last = (DiligentNode) tree.getLastSelectedPathComponent();
                    last.setStatus(DiligentNode.STATUS_OPEN);
                }
            }
        });
        submenu.add(menuItem);
        menuItem = new JMenuItem("accept", ResourceManager.createImageIcon("icons/accepted.png"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (tree.getSelectionPath() != null) {
                    DiligentNode last = (DiligentNode) tree.getLastSelectedPathComponent();
                    last.setStatus(DiligentNode.STATUS_ACCEPTED);
                }
            }
        });
        submenu.add(menuItem);
        menuItem = new JMenuItem("reject", ResourceManager.createImageIcon("icons/rejected.png"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (tree.getSelectionPath() != null) {
                    DiligentNode last = (DiligentNode) tree.getLastSelectedPathComponent();
                    last.setStatus(DiligentNode.STATUS_REJECTED);
                }
            }
        });
        submenu.add(menuItem);
        menuItem = new JMenuItem("close", ResourceManager.createImageIcon("icons/closed.png"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (tree.getSelectionPath() != null) {
                    DiligentNode last = (DiligentNode) tree.getLastSelectedPathComponent();
                    last.setStatus(DiligentNode.STATUS_CLOSED);
                    details.requery();
                }
            }
        });
        submenu.add(menuItem);
        menuItem = new JMenuItem("category");
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (tree.getSelectionPath() != null) {
                    DiligentNode last = (DiligentNode) tree.getLastSelectedPathComponent();
                    last.setStatus(DiligentNode.STATUS_CATEGORY);
                }
            }
        });
        submenu.add(menuItem);
        popup.add(submenu);
        menuItem = new JMenuItem("Add...");
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (tree.getSelectionPath() != null) {
                    DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                    DiligentNode last = (DiligentNode) tree.getLastSelectedPathComponent();
                    treeModel.insertNodeInto(new DiligentNode("new"), last, 0);
                }
            }
        });
        popup.add(menuItem);
        menuItem = new JMenuItem("Remove");
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (tree.getSelectionPath() != null) {
                    DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                    DiligentNode last = (DiligentNode) tree.getLastSelectedPathComponent();
                    DiligentNode parent = (DiligentNode) last.getParent();
                    if (parent != null) treeModel.removeNodeFromParent(last);
                }
            }
        });
        popup.add(menuItem);
        tree.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    tree.setSelectionRow(tree.getRowForLocation(e.getX(), e.getY()));
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        details = new DiligentDetails(null);
        JSplitPane treeSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), new JScrollPane(details, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        treeSplit.addAncestorListener(new AncestorListener() {

            public void ancestorAdded(AncestorEvent ae) {
            }

            public void ancestorMoved(AncestorEvent ae) {
            }

            public void ancestorRemoved(AncestorEvent ae) {
            }
        });
        tableModel = new DiligentTableModel(treeModel);
        table = new DiligentTable(tableModel) {

            public void valueChanged(ListSelectionEvent e) {
                if (((DefaultListSelectionModel) e.getSource()).isSelectionEmpty()) return;
                final DiligentNode node = (DiligentNode) table.getValueAt(table.getSelectedRow(), 0);
                details.query(node);
                super.valueChanged(e);
            }
        };
        tree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                final DiligentNode node = (DiligentNode) tree.getLastSelectedPathComponent();
                details.query(node);
            }
        });
        final JList filterList = new JList(DiligentFilter.filters);
        filterList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    root.setFilter(filterList.getSelectedIndex());
                    tableModel.fireTableDataChanged();
                    treeModel.nodeStructureChanged(root);
                    Enumeration nodes = ((DiligentNode) treeModel.getRoot()).breadthFirstEnumeration();
                    DiligentNode node;
                    while (nodes.hasMoreElements()) {
                        node = (DiligentNode) nodes.nextElement();
                        if (node.isExpanded()) {
                            DiligentNode parent = (DiligentNode) node.getParent();
                            boolean expand = true;
                            while (parent != null && !parent.isRoot() && expand) {
                                if (!parent.isExpanded()) expand = false;
                                parent = (DiligentNode) parent.getParent();
                            }
                            if (expand) tree.expandPath(new TreePath(node.getPath()));
                        }
                    }
                }
            }
        });
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("tree", new JScrollPane(tree));
        tabbedPane.addTab("table", new JScrollPane(table));
        tabbedPane.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                tableModel.fireTableChanged(new TableModelEvent(tableModel));
            }
        });
        JSplitPane filterSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, filterList, tabbedPane);
        JSplitPane explorerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filterSplit, details);
        getContentPane().add(explorerSplit);
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        menuItem = new JMenuItem("New");
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (checkDirty()) treeModel.setRoot(new DiligentNode("root"));
                setFilename("");
            }
        });
        fileMenu.add(menuItem);
        menuItem = new JMenuItem("Open...");
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (checkDirty()) open();
            }
        });
        fileMenu.add(menuItem);
        menuItem = new JMenuItem("Save");
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (prop.containsKey("last")) save(prop.getProperty("last"));
            }
        });
        fileMenu.add(menuItem);
        menuItem = new JMenuItem("Save As...");
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (checkDirty()) save();
            }
        });
        fileMenu.add(menuItem);
        JMenu menu = new JMenu("Help");
        menuBar.add(menu);
        menuItem = new JMenuItem("About...");
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "$Revision: 4 $\n\n" + Constant.license);
            }
        });
        menu.add(menuItem);
    }

    public void setFilename(String filename) {
        prop.setProperty("last", filename);
        ((DiligentNode) treeModel.getRoot()).clean();
        if (filename.equals("")) filename = "(untitled)";
        this.setTitle("Diligent - " + filename);
    }

    public boolean checkDirty() {
        if (root.isTreeDirty()) {
            int option = JOptionPane.showConfirmDialog(this, "The project has changed.  Save?", "Save?", JOptionPane.YES_NO_CANCEL_OPTION);
            switch(option) {
                case JOptionPane.YES_OPTION:
                    save(prop.getProperty("last", ""));
                case JOptionPane.NO_OPTION:
                    return true;
                default:
                case JOptionPane.CANCEL_OPTION:
                    return false;
            }
        }
        return true;
    }

    public void open(String filename) {
        if (filename == null || filename.equals("")) open();
        try {
            FileInputStream in = new FileInputStream(filename);
            ObjectInputStream s = new ObjectInputStream(in);
            treeModel.setRoot((DiligentNode) s.readObject());
            setFilename(filename);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void open() {
        JFileChooser fc = new JFileChooser();
        int fcReturn = fc.showOpenDialog(frame);
        if (fcReturn == JFileChooser.APPROVE_OPTION) {
            open(fc.getSelectedFile().getAbsolutePath());
        }
    }

    public void save(String filename) {
        try {
            FileOutputStream out = new FileOutputStream(filename);
            ObjectOutputStream s = new ObjectOutputStream(out);
            s.writeObject((DiligentNode) treeModel.getRoot());
            s.flush();
            setFilename(filename);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void save() {
        JFileChooser fc = new JFileChooser();
        int fcReturn = fc.showSaveDialog(frame);
        if (fcReturn == JFileChooser.APPROVE_OPTION) {
            save(fc.getSelectedFile().getAbsolutePath());
        }
    }
}

class DiligentTreeModel extends DefaultTreeModel {

    public DiligentTreeModel(DiligentNode root) {
        super(root);
    }

    public void nodeStructureChanged(TreeNode node) {
        super.nodeStructureChanged(node);
    }

    public void removeNodeFromParent(MutableTreeNode node) {
        removeNodeFromParent(node, true);
    }

    public void removeNodeFromParent(MutableTreeNode node, boolean verify) {
        int option = JOptionPane.OK_OPTION;
        if (verify) {
            option = JOptionPane.showConfirmDialog(null, "Really Delete '".concat(node.toString()).concat("'?"), "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        }
        if (option == JOptionPane.OK_OPTION) super.removeNodeFromParent(node);
    }
}

class DiligentTree extends JTree {

    protected DiligentNode export;

    public DiligentTree(TreeModel treeModel) {
        super(treeModel);
        setEditable(true);
        setShowsRootHandles(true);
        setCellRenderer(new DiligentNodeRenderer());
        addKeyListener(new KeyAdapter() {

            public void keyTyped(KeyEvent e) {
                if (getSelectionPath() != null) {
                    if (!isEditing()) {
                        DiligentNode selected = (DiligentNode) getLastSelectedPathComponent();
                        if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                            DiligentNode newNode = new DiligentNode("");
                            if (e.isControlDown()) {
                                ((DiligentTreeModel) getModel()).insertNodeInto(newNode, selected, 0);
                                startEditingAtPath(new TreePath(newNode.getPath()));
                            } else {
                                if (!selected.isRoot()) {
                                    ((DiligentTreeModel) getModel()).insertNodeInto(newNode, (DiligentNode) selected.getParent(), 0);
                                    startEditingAtPath(new TreePath(newNode.getPath()));
                                }
                            }
                        } else if (e.getKeyChar() == KeyEvent.VK_DELETE) {
                            int selectionRow[] = getSelectionRows();
                            ((DiligentTreeModel) getModel()).removeNodeFromParent(selected);
                            setSelectionRow(selectionRow[0]);
                        } else System.out.println(e.getKeyChar());
                    }
                }
            }
        });
        setDragEnabled(true);
        setTransferHandler(new TransferHandler() {

            DiligentNode export;

            public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
                return true;
            }

            public boolean importData(JComponent comp, Transferable t) {
                DiligentNode node = null;
                try {
                    node = (DiligentNode) t.getTransferData(new DataFlavor(export.getClass(), null));
                } catch (Exception e) {
                    System.err.println("Couldn't.");
                }
                ;
                JTree ctree = (JTree) comp;
                DiligentTreeModel ctreeModel = (DiligentTreeModel) ctree.getModel();
                DiligentNode last = (DiligentNode) ctree.getLastSelectedPathComponent();
                if (node != null && !export.isNodeDescendant(last)) {
                    ctreeModel.removeNodeFromParent(export, false);
                    ctreeModel.insertNodeInto(node, last, 0);
                    return true;
                } else return false;
            }

            public void exportAsDrag(JComponent c, InputEvent e, int action) {
                super.exportAsDrag(c, e, action);
                JTree t = (JTree) c;
                export = (DiligentNode) t.getLastSelectedPathComponent();
            }

            public int getSourceActions(JComponent c) {
                return TransferHandler.COPY_OR_MOVE;
            }

            protected Transferable createTransferable(JComponent c) {
                Transferable t = new Transferable() {

                    public Object getTransferData(DataFlavor flavor) {
                        if (flavor.equals(new DataFlavor("".getClass(), null))) {
                            return export.getName();
                        } else if (flavor.equals(new DataFlavor(export.getClass(), null))) {
                            return export;
                        } else return null;
                    }

                    public DataFlavor[] getTransferDataFlavors() {
                        DataFlavor flavors[] = { new DataFlavor("".getClass(), null), new DataFlavor(export.getClass(), null) };
                        return flavors;
                    }

                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return true;
                    }
                };
                return t;
            }

            protected void exportDone(JComponent source, Transferable data, int action) {
                super.exportDone(source, data, action);
            }
        });
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }
}

class DiligentDetails extends JPanel {

    Frame frame;

    JComboBox statusField;

    protected DiligentNode node;

    public DiligentDetails(DiligentNode node) {
        super();
        this.node = node;
        query(node);
    }

    public void query(DiligentNode node) {
        this.node = node;
        requery();
    }

    public void requery() {
        if (node != null) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setPreferredSize(new Dimension(0, 0));
            removeAll();
            repaint();
            final JLabel parentsLabel = new JLabel("Parents: ".concat(node.getParentsString()));
            add(parentsLabel);
            final JTextField nameField = new JTextField(node.getName());
            nameField.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
            JLabel nameLabel = new JLabel("Name:");
            nameLabel.setLabelFor(nameField);
            nameField.getDocument().addDocumentListener(new DocumentListener() {

                public void update() {
                    node.setName(nameField.getText());
                }

                public void insertUpdate(DocumentEvent e) {
                    update();
                }

                public void removeUpdate(DocumentEvent e) {
                    update();
                }

                public void changedUpdate(DocumentEvent e) {
                }
            });
            add(nameLabel);
            add(nameField);
            final JTextArea descriptionField = new JTextArea(node.getDescription());
            descriptionField.setLineWrap(true);
            descriptionField.setWrapStyleWord(true);
            JLabel descriptionLabel = new JLabel("Description:");
            descriptionLabel.setLabelFor(descriptionField);
            descriptionField.getDocument().addDocumentListener(new DocumentListener() {

                public void update() {
                    node.setDescription(descriptionField.getText());
                }

                public void insertUpdate(DocumentEvent e) {
                    update();
                }

                public void removeUpdate(DocumentEvent e) {
                    update();
                }

                public void changedUpdate(DocumentEvent e) {
                }
            });
            add(descriptionLabel);
            add(new JScrollPane(descriptionField));
            JTextField conceivedField = new JTextField(node.getConceived().toString());
            conceivedField.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
            JLabel conceivedLabel = new JLabel("Conceived:");
            conceivedLabel.setLabelFor(conceivedField);
            add(conceivedLabel);
            add(conceivedField);
            final JTextField startLineField = new JTextField();
            if (node.getStartLine() != null) startLineField.setText(node.getStartLine().toString());
            startLineField.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    if (ae.getActionCommand().equals("")) {
                        node.setStartLine(null);
                        startLineField.setText("null");
                    }
                    try {
                        Date d = DateParse.parse(ae.getActionCommand());
                        node.setStartLine(d);
                        startLineField.setText(node.getStartLine().toString());
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            });
            startLineField.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
            JLabel startLineLabel = new JLabel("Startline:");
            startLineLabel.setLabelFor(startLineField);
            add(startLineLabel);
            add(startLineField);
            final JTextField deadlineField = new JTextField();
            if (node.getDeadline() != null) deadlineField.setText(node.getDeadline().toString());
            deadlineField.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    if (ae.getActionCommand().equals("")) {
                        node.setDeadline(null);
                        deadlineField.setText("null");
                    }
                    try {
                        Date d = DateParse.parse(ae.getActionCommand());
                        node.setDeadline(d);
                        deadlineField.setText(node.getDeadline().toString());
                    } catch (Exception e) {
                    }
                }
            });
            deadlineField.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
            JLabel deadlineLabel = new JLabel("Deadline:");
            deadlineLabel.setLabelFor(deadlineField);
            add(deadlineLabel);
            add(deadlineField);
            JTextField completedField = new JTextField();
            if (node.getCompleted() != null) completedField.setText(node.getCompleted().toString());
            completedField.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
            JLabel completedLabel = new JLabel("Completed:");
            completedLabel.setLabelFor(completedField);
            add(completedLabel);
            add(completedField);
            final JTextField periodField = new JTextField();
            periodField.setText(String.valueOf(node.getPeriodText()));
            periodField.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
            periodField.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    if (ae.getActionCommand().equals("")) {
                        node.setPeriod(0);
                    }
                    try {
                        node.setPeriod(ae.getActionCommand());
                        periodField.setText(node.getPeriodText());
                    } catch (Exception e) {
                    }
                }
            });
            JLabel periodLabel = new JLabel("Period:");
            periodLabel.setLabelFor(periodField);
            add(periodLabel);
            add(periodField);
            final JTextField prerequisiteField = new JTextField();
            if (node.getPrerequisite() == null) prerequisiteField.setText("null"); else {
                String s = node.getPrerequisite().toString();
                prerequisiteField.setText(s);
            }
            prerequisiteField.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
            Component c = this;
            while (c.getParent() != null) c = c.getParent();
            frame = (Frame) c;
            final MouseAdapter prereqMouseListener = new MouseAdapter() {

                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        prerequisiteField.setText("Select a node.");
                        DiligentNodeSelector selector = new DiligentNodeSelector(frame, (DiligentNode) node.getRoot());
                        DiligentNode selected = selector.getNode();
                        if (selected != null) node.setPrerequisite(new DiligentEvent(selected));
                        if (node.getPrerequisite() == null) prerequisiteField.setText("null"); else prerequisiteField.setText(node.getPrerequisite().toString());
                        statusField.setSelectedIndex(node.getStatus());
                    }
                }
            };
            prerequisiteField.addMouseListener(prereqMouseListener);
            JLabel prerequisiteLabel = new JLabel("Prerequisite");
            prerequisiteLabel.setLabelFor(prerequisiteField);
            add(prerequisiteLabel);
            add(prerequisiteField);
            statusField = new JComboBox(DiligentNode.statusStrings);
            statusField.setSelectedIndex(node.getStatus());
            statusField.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox) e.getSource();
                    node.setStatus(cb.getSelectedIndex());
                    requery();
                }
            });
            JLabel statusLabel = new JLabel("Status:");
            statusField.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
            statusLabel.setLabelFor(statusField);
            add(statusLabel);
            add(statusField);
            JLabel inheritedStatusLabel = new JLabel("Inherited Status: ".concat(node.getInheritedStatusString()));
            add(inheritedStatusLabel);
            validate();
        }
    }
}

class DiligentNodeSelector extends JDialog {

    DiligentNode root;

    DiligentNode selected;

    JTree tree;

    public DiligentNodeSelector(Frame owner, DiligentNode root) {
        super(owner, "Select a Node.", true);
        this.root = root;
        selected = null;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        drawFrame();
        setVisible(false);
    }

    public void drawFrame() {
        tree = new JTree(root);
        getContentPane().add(tree);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(new DiligentNodeRenderer());
        tree.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    dispose();
                }
            }
        });
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                selected = null;
                dispose();
            }
        });
        pack();
    }

    public DiligentNode getNode() {
        show();
        return (DiligentNode) tree.getLastSelectedPathComponent();
    }
}

class DiligentTable extends JTable {

    protected DiligentNode transfer;

    protected TransferHandler tableTransferHandler;

    public DiligentTable(final AbstractTableModel tableModel) {
        super(tableModel);
        getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JComboBox(DiligentNode.statusStrings)) {

            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                ((JComboBox) getComponent()).setSelectedIndex(((Integer) value).intValue());
                return getComponent();
            }
        });
        getColumnModel().getColumn(1).setCellRenderer(new TableCellRenderer() {

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = new JLabel(DiligentNode.statusStrings[((Integer) value).intValue()]);
                return label;
            }
        });
        tableTransferHandler = new TransferHandler() {

            public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
                super.exportToClipboard(comp, clip, action);
            }

            public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
                return true;
            }

            public boolean importData(JComponent comp, Transferable t) {
                DiligentNode target = (DiligentNode) getModel().getValueAt(getSelectedRow(), 0);
                transfer.moveAcceptID(target);
                return true;
            }

            public void exportAsDrag(JComponent c, InputEvent e, int action) {
                super.exportAsDrag(c, e, action);
                if (getSelectedRow() != -1) transfer = (DiligentNode) getModel().getValueAt(getSelectedRow(), 0);
                System.out.println(transfer.getChildCount());
            }

            public int getSourceActions(JComponent c) {
                return TransferHandler.COPY_OR_MOVE;
            }

            protected Transferable createTransferable(JComponent c) {
                Transferable t = new Transferable() {

                    public Object getTransferData(DataFlavor flavor) {
                        if (flavor.equals(new DataFlavor("".getClass(), null))) {
                            return transfer.getName();
                        } else if (flavor.equals(new DataFlavor(DiligentNode.class, null))) {
                            return transfer;
                        } else return null;
                    }

                    public DataFlavor[] getTransferDataFlavors() {
                        DataFlavor flavors[] = { new DataFlavor(DiligentNode.class, null), new DataFlavor(String.class, null) };
                        return flavors;
                    }

                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return true;
                    }
                };
                return t;
            }

            protected void exportDone(JComponent source, Transferable data, int action) {
                super.exportDone(source, data, action);
                ((AbstractTableModel) getModel()).fireTableChanged(new TableModelEvent(getModel()));
            }
        };
        final JTable table = this;
        addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                if (e.getX() < getColumnModel().getColumn(0).getWidth() && e.getButton() == MouseEvent.BUTTON1) {
                    table.addRowSelectionInterval(table.rowAtPoint(new Point(e.getX(), e.getY())), table.rowAtPoint(new Point(e.getX(), e.getY())));
                    tableTransferHandler.exportAsDrag((JComponent) e.getSource(), e, TransferHandler.MOVE);
                }
            }
        });
        setTransferHandler(tableTransferHandler);
    }
}

class DiligentTableModel extends AbstractTableModel implements TableModelListener {

    protected String columnNames[] = { "Name", "Status" };

    protected int sort = 0;

    protected Vector data;

    protected TreeModel treeModel;

    public DiligentTableModel(TreeModel treeModel) {
        super();
        addTableModelListener((TableModelListener) this);
        this.treeModel = treeModel;
    }

    protected void populate() {
        data = new Vector();
        Enumeration e = ((DiligentNode) treeModel.getRoot()).preorderEnumeration();
        DiligentNode node;
        while (e.hasMoreElements()) {
            node = (DiligentNode) e.nextElement();
            if (!node.isFiltered(false)) data.addElement(node);
        }
        Collections.sort(data, new Comparator() {

            public int compare(Object o1, Object o2) {
                return ((DiligentNode) o2).getAcceptID() - ((DiligentNode) o1).getAcceptID();
            }
        });
    }

    public void tableChanged(TableModelEvent e) {
        populate();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public int getRowCount() {
        int i;
        if (data == null) populate();
        Enumeration e = data.elements();
        for (i = 0; e.hasMoreElements(); i++) e.nextElement();
        return i;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int row, int col) {
        if (row < 0 || col < 0) return null;
        DiligentNode node = (DiligentNode) data.get(row);
        switch(col) {
            case 0:
                return node;
            case 1:
                return new Integer(node.getStatus());
            case 2:
                return new Integer(node.getAcceptID());
            default:
                return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        return true;
    }

    public void setValueAt(Object value, int row, int col) {
        if (col == 0) ((DiligentNode) data.get(row)).setName((String) value);
        if (col == 1) {
            String status[] = DiligentNode.statusStrings;
            int i;
            for (i = 0; !status[i].equals((String) value); i++) ;
            ((DiligentNode) data.get(row)).setStatus(i);
        }
        fireTableChanged(new TableModelEvent(this));
    }
}

;

class DiligentEvent implements Serializable {

    static final long serialVersionUID = -5792262236273238953L;

    static final int TYPE_NODE = 0;

    static final int TYPE_DATE = 1;

    protected int type;

    protected Date date;

    protected DiligentNode node;

    protected int nodeStatus;

    public DiligentEvent(DiligentNode node) {
        this.type = TYPE_NODE;
        this.node = node;
        nodeStatus = node.getStatus();
    }

    public DiligentEvent(Date date) {
        this.type = TYPE_DATE;
        this.date = date;
    }

    public int getType() {
        return type;
    }

    public boolean finished() {
        if (type == TYPE_NODE) return (node.getStatus() != nodeStatus); else if (type == TYPE_DATE) {
            Date now = new Date();
            return (now.after(date));
        } else return true;
    }

    public String toString() {
        String s = "";
        if (type == TYPE_NODE) {
            s = "Node: ".concat(node.getName()).concat("!=").concat(DiligentNode.getStatusString(nodeStatus)).concat(":").concat(finished() ? "true" : "false");
        } else if (type == TYPE_DATE) s = "Now>".concat(date.toString());
        return s;
    }
}

class DiligentNodeRenderer extends DefaultTreeCellRenderer {

    protected ImageIcon openIcon;

    protected ImageIcon acceptedIcon;

    protected ImageIcon rejectedIcon;

    protected ImageIcon closedIcon;

    public DiligentNodeRenderer() {
        super();
        openIcon = ResourceManager.createImageIcon("icons/open.png");
        acceptedIcon = ResourceManager.createImageIcon("icons/accepted.png");
        rejectedIcon = ResourceManager.createImageIcon("icons/rejected.png");
        closedIcon = ResourceManager.createImageIcon("icons/closed.png");
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        switch(((DiligentNode) value).getStatus()) {
            case DiligentNode.STATUS_OPEN:
                setIcon(openIcon);
                break;
            case DiligentNode.STATUS_REJECTED:
                setIcon(rejectedIcon);
                break;
            case DiligentNode.STATUS_ACCEPTED:
                setIcon(acceptedIcon);
                break;
            case DiligentNode.STATUS_CLOSED:
                setIcon(closedIcon);
                break;
            default:
                break;
        }
        return this;
    }
}

class ResourceManager {

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = Diligent.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}

class DateParse {

    static Date parse(String date) {
        Date d;
        if (date.equals("")) return null;
        try {
            DateFormat df = new SimpleDateFormat("dd-MMM-yy");
            df.setLenient(true);
            d = df.parse(date);
        } catch (Exception e) {
            try {
                d = new Date((new Date()).getTime() + Period.parse(date));
            } catch (Exception e2) {
                System.out.println(e2);
                d = null;
            }
        }
        return d;
    }
}

class Period {

    static long parse(String period) {
        StringTokenizer st = new StringTokenizer(period, "wdhms", true);
        long length = 0;
        long total = 0;
        String id;
        while (st.hasMoreTokens()) {
            length = new Long(st.nextToken()).longValue();
            id = st.nextToken();
            if (id.equals("w")) length *= (long) 1000 * 60 * 60 * 24 * 7;
            if (id.equals("d")) length *= (long) 1000 * 60 * 60 * 24;
            if (id.equals("h")) length *= (long) 1000 * 60 * 60;
            if (id.equals("m")) length *= (long) 1000 * 60;
            if (id.equals("s")) length *= (long) 1000;
            total += length;
        }
        return total;
    }

    static String generate(long period) {
        String total = "";
        long remainder;
        remainder = period / ((long) 1000 * 60 * 60 * 24 * 7);
        period -= remainder * ((long) 1000 * 60 * 60 * 24 * 7);
        total = total.concat(new Long(remainder).toString()).concat("w");
        remainder = period / ((long) 1000 * 60 * 60 * 24);
        period -= remainder * ((long) 1000 * 60 * 60 * 24);
        if (remainder != 0) total = total.concat(new Long(remainder).toString()).concat("d");
        remainder = period / ((long) 1000 * 60 * 60);
        period -= remainder * ((long) 1000 * 60 * 60);
        if (remainder != 0) total = total.concat(new Long(remainder).toString()).concat("h");
        remainder = period / ((long) 1000 * 60);
        period -= remainder * ((long) 1000 * 60);
        if (remainder != 0) total = total.concat(new Long(remainder).toString()).concat("m");
        remainder = period / ((long) 1000);
        period -= remainder * ((long) 1000);
        if (remainder != 0) total = total.concat(new Long(remainder).toString()).concat("s");
        return total;
    }
}

class DiligentFilter {

    public static final int FILTER_NO_FILTER = 0;

    public static final int FILTER_ASSIGN = 1;

    public static final int FILTER_WORK = 2;

    public static final int FILTER_EVENTS = 3;

    public static final int FILTER_REVIEW = 4;

    public static final int FILTER_RECYCLE = 5;

    public static String[] filters = { "No Filter", "Assign", "Work", "Events", "Review", "Recycle" };

    protected int filter = 0;

    public DiligentFilter(int filter) {
        setFilter(filter);
    }

    public void setFilter(int filter) {
        if (filter >= 0 && filter <= 5) this.filter = filter;
    }

    public int getFilter() {
        return filter;
    }

    public boolean isFiltered(DiligentNode node, boolean tree) {
        if (filter == FILTER_NO_FILTER) return false;
        if (tree) {
            Enumeration children = node.breadthFirstEnumeration();
            boolean filtered = true;
            children.nextElement();
            DiligentNode child;
            while (children.hasMoreElements()) {
                child = (DiligentNode) children.nextElement();
                if (!isFiltered(child, false)) filtered = false;
            }
            return (filtered && isFiltered(node, false));
        } else {
            switch(filter) {
                case FILTER_NO_FILTER:
                default:
                    return false;
                case FILTER_ASSIGN:
                    return !(node.getInheritedStatus() != DiligentNode.ISTATUS_INACTIVE && node.getStatus() == DiligentNode.STATUS_OPEN);
                case FILTER_WORK:
                    if (node.getInheritedStatus() != DiligentNode.ISTATUS_ACTIVE || node.getStatus() != DiligentNode.STATUS_ACCEPTED) return true;
                    Enumeration children = node.breadthFirstEnumeration();
                    boolean filtered = false;
                    DiligentNode child;
                    children.nextElement();
                    while (children.hasMoreElements() && !filtered) {
                        child = (DiligentNode) children.nextElement();
                        if (child.getInheritedStatus() == DiligentNode.ISTATUS_ACTIVE && child.getStatus() == DiligentNode.STATUS_ACCEPTED) filtered = true;
                    }
                    return filtered;
                case FILTER_EVENTS:
                    return !(node.getInheritedStatus() != DiligentNode.ISTATUS_INACTIVE && (node.getStatus() == DiligentNode.STATUS_OPEN_PENDING || node.getStatus() == DiligentNode.STATUS_ACCEPTED_PENDING));
                case FILTER_REVIEW:
                    return !(node.getStatus() == DiligentNode.STATUS_CLOSED);
                case FILTER_RECYCLE:
                    return !(node.getStatus() == DiligentNode.STATUS_REJECTED);
            }
        }
    }

    public boolean isFiltered(DiligentNode node) {
        return isFiltered(node, false);
    }
}

class DiligentNode extends DefaultMutableTreeNode implements Serializable {

    static final long serialVersionUID = -6310363575619505217L;

    static final int STATUS_OPEN = 0;

    static final int STATUS_REJECTED = 1;

    static final int STATUS_ACCEPTED = 2;

    static final int STATUS_CLOSED = 3;

    static final int STATUS_CATEGORY = 4;

    static final int STATUS_OPEN_PENDING = 5;

    static final int STATUS_ACCEPTED_PENDING = 6;

    static final String statusStrings[] = { "Open", "Rejected", "Accepted", "Closed", "Category", "Open Pending", "Accepted Pending" };

    static final int ISTATUS_ACTIVE = 0;

    static final int ISTATUS_NEW = 1;

    static final int ISTATUS_WAITING = 2;

    static final int ISTATUS_INACTIVE = 3;

    static final String iStatusStrings[] = { "Active", "New", "Waiting", "Inactive" };

    protected String description;

    protected int status;

    protected String completion;

    protected Date conceived;

    protected Date deadline;

    protected Date completed;

    protected DiligentEvent prerequisite;

    public int acceptID;

    protected transient boolean dirty;

    protected static transient boolean treeDirty;

    protected long period;

    protected Date startLine;

    protected static transient DiligentFilter filter;

    protected boolean expanded;

    public DiligentNode(String name) {
        super(name);
        this.description = "";
        this.status = 0;
        conceived = new Date();
        status = STATUS_OPEN;
        prerequisite = null;
        acceptID = 0;
        dirty(false);
        period = 0;
        filter = new DiligentFilter(0);
        expanded = false;
    }

    public void expand() {
        expanded = true;
        dirty(true);
    }

    public void collapse() {
        expanded = false;
        dirty(true);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public String getParentsString() {
        if (isRoot()) return "";
        String parents = "root";
        DiligentNode parent = (DiligentNode) getParent();
        while (!parent.isRoot()) {
            parents = parents.concat(">>").concat(parent.toString());
            parent = (DiligentNode) parent.getParent();
        }
        return parents;
    }

    public Enumeration children() {
        Enumeration allChildren = super.children();
        Vector filtered = new Vector();
        DiligentNode node;
        while (allChildren.hasMoreElements()) {
            node = (DiligentNode) allChildren.nextElement();
            if (!node.isFiltered(true)) filtered.addElement(node);
        }
        return filtered.elements();
    }

    public int getChildCount() {
        int i;
        Enumeration nodes = children();
        for (i = 0; nodes.hasMoreElements(); i++) nodes.nextElement();
        return i;
    }

    public TreeNode getChildAt(int index) {
        Enumeration nodes = children();
        Object node = null;
        for (int i = 0; nodes.hasMoreElements() && i <= index; i++) node = nodes.nextElement();
        return (TreeNode) node;
    }

    public boolean isFiltered(DiligentNode node, boolean tree) {
        return filter.isFiltered(node, tree);
    }

    public boolean isFiltered(boolean tree) {
        return isFiltered(this, tree);
    }

    public void setFilter(int filter) {
        this.filter.setFilter(filter);
    }

    public Date getStartLine() {
        return startLine;
    }

    public void setStartLine(Date startLine) {
        this.startLine = startLine;
        dirty(true);
    }

    public long getPeriod() {
        return period;
    }

    public String getPeriodText() {
        if (period == 0) return "(none)";
        return Period.generate(period);
    }

    public void setPeriod(long period) {
        this.period = period;
        dirty(true);
    }

    public void setPeriod(String period) {
        setPeriod(Period.parse(period));
    }

    public void setPrerequisite(DiligentEvent prerequisite) {
        this.prerequisite = prerequisite;
        if (status == STATUS_ACCEPTED) status = STATUS_ACCEPTED_PENDING; else status = STATUS_OPEN_PENDING;
        dirty(true);
    }

    public DiligentEvent getPrerequisite() {
        return prerequisite;
    }

    public boolean isPending() {
        if (getStatus() != STATUS_OPEN_PENDING && getStatus() != STATUS_ACCEPTED_PENDING) return false; else return !(prerequisite.finished());
    }

    public int getStatus() {
        boolean expired = true;
        if (startLine != null && startLine.after(new Date())) expired = false;
        if (status == STATUS_OPEN_PENDING && expired && (prerequisite == null || prerequisite.finished())) {
            status = STATUS_OPEN;
            dirty(true);
        }
        if (status == STATUS_ACCEPTED_PENDING && expired && (prerequisite == null || prerequisite.finished())) {
            status = STATUS_ACCEPTED;
            dirty(true);
        }
        if (status == STATUS_OPEN && prerequisite != null && !(prerequisite.finished())) {
            status = STATUS_OPEN_PENDING;
            dirty(true);
        }
        if (status == STATUS_ACCEPTED && prerequisite != null && !(prerequisite.finished())) {
            status = STATUS_ACCEPTED_PENDING;
            dirty(true);
        }
        return status;
    }

    public int getInheritedStatus() {
        if (isRoot()) return ISTATUS_ACTIVE;
        int state = ISTATUS_ACTIVE;
        DiligentNode node = (DiligentNode) getParent();
        while (!node.isRoot() && state != ISTATUS_INACTIVE) {
            switch(node.getStatus()) {
                case STATUS_OPEN:
                    if (state != ISTATUS_WAITING) state = ISTATUS_NEW;
                    break;
                case STATUS_OPEN_PENDING:
                case STATUS_ACCEPTED_PENDING:
                    state = ISTATUS_WAITING;
                    break;
                case STATUS_CLOSED:
                case STATUS_REJECTED:
                    state = ISTATUS_INACTIVE;
                    break;
            }
            node = (DiligentNode) node.getParent();
        }
        return state;
    }

    public String getStatusString() {
        return getStatusString(getStatus());
    }

    public static String getStatusString(int status) {
        return statusStrings[status];
    }

    public String getInheritedStatusString() {
        return iStatusStrings[getInheritedStatus()];
    }

    public static String getInheritedStatusString(int status) {
        return iStatusStrings[status];
    }

    public int getAcceptID() {
        return acceptID;
    }

    public int getNewAcceptID() {
        if (isRoot()) return acceptID++; else {
            DiligentNode r = (DiligentNode) getRoot();
            return r.getNewAcceptID();
        }
    }

    public void setStatus(int status) {
        if (status == STATUS_OPEN_PENDING || status == STATUS_ACCEPTED_PENDING) {
            if (prerequisite == null && period == 0) return;
        } else prerequisite = null;
        this.status = status;
        if (status == STATUS_ACCEPTED) {
            acceptID = getNewAcceptID();
            if (startLine == null) startLine = new Date();
        }
        if (status == STATUS_CLOSED) {
            completed = new Date();
            if (period != 0) {
                setStatus(STATUS_ACCEPTED_PENDING);
                startLine = new Date((new Date()).getTime() + period);
            }
        }
        dirty(true);
    }

    public void __decrementAcceptID() {
        acceptID--;
        dirty(true);
    }

    public void __incrementAcceptID() {
        acceptID++;
        dirty(true);
    }

    public void moveAcceptID(DiligentNode node) {
        int previousAcceptID = acceptID;
        if (status == STATUS_ACCEPTED) {
            acceptID = node.getAcceptID();
            Enumeration e = ((DiligentNode) getRoot()).postorderEnumeration();
            if (previousAcceptID < acceptID) {
                DiligentNode currentNode;
                while (e.hasMoreElements()) {
                    currentNode = (DiligentNode) e.nextElement();
                    if (!equals(currentNode) && currentNode.getAcceptID() > previousAcceptID && currentNode.getAcceptID() <= acceptID) currentNode.__decrementAcceptID();
                }
            } else if (previousAcceptID > acceptID) {
                DiligentNode currentNode;
                while (e.hasMoreElements()) {
                    currentNode = (DiligentNode) e.nextElement();
                    if (!equals(currentNode) && currentNode.getAcceptID() < previousAcceptID && currentNode.getAcceptID() >= acceptID) currentNode.__incrementAcceptID();
                }
            }
        }
        dirty(true);
    }

    public Vector statesAvailable() {
        Vector states = new Vector();
        switch(status) {
            case STATUS_OPEN:
                states.addElement(new Integer(STATUS_REJECTED));
                states.addElement(new Integer(STATUS_ACCEPTED));
                break;
            case STATUS_ACCEPTED:
                states.addElement(new Integer(STATUS_CLOSED));
            case STATUS_REJECTED:
                states.addElement(new Integer(STATUS_OPEN));
                break;
            case STATUS_CLOSED:
                states.addElement(new Integer(STATUS_ACCEPTED));
                break;
            default:
                break;
        }
        return states;
    }

    public Date getConceived() {
        return conceived;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
        dirty(true);
    }

    public Date getCompleted() {
        return completed;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        dirty(true);
    }

    public String getCompletion() {
        return completion;
    }

    public void setCompletion(String completion) {
        this.completion = completion;
        dirty(true);
    }

    public String getName() {
        return getUserObject().toString();
    }

    public void setName(String name) {
        setUserObject(name);
        dirty(true);
    }

    public void dirty(boolean dirty) {
        this.dirty = dirty;
        if (dirty) treeDirty = true;
    }

    public void clean() {
        treeDirty = false;
    }

    public boolean isTreeDirty() {
        return treeDirty;
    }
}

class Diligent {

    public static void main(String[] args) {
        final DiligentFrame frame = new DiligentFrame();
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                if (frame.checkDirty()) {
                    frame.storeProperties();
                    System.exit(0);
                }
            }
        });
        frame.pack();
        frame.setVisible(true);
    }
}

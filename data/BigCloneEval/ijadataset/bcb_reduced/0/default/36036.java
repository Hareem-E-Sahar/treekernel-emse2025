import java.awt.AWTException;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.EventQueue;
import java.awt.Point;
import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.JComboBox;
import javax.swing.event.ListDataEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.Position;
import javax.swing.tree.MutableTreeNode;
import org.jdesktop.application.AbstractBean;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import org.jdesktop.application.Task;

/**
 *
 * @author  Cory Flanagin
 */
public class JFrameMain extends javax.swing.JFrame {

    Timer myTimer;

    TimerTask foo;

    /** Creates new form JFrameMain */
    public JFrameMain() {
        initComponents();
    }

    public static String bob = "boo";

    DefaultTreeModel treeModel;

    volatile DefaultMutableTreeNode parent_node;

    DefaultMutableTreeNode root;

    TreePath pathSelectedNode;

    DefaultMutableTreeNode removed_node;

    File directory;

    boolean delete_pressed;

    public String previous_node;

    String changeAttributeNameDialog = "";

    public GlobalVars globalVars = new GlobalVars();

    boolean toggleTimer = false;

    public static JPanel theDeck;

    public static JScrollPane CS;

    static ConnectScene classCS;

    static final zoomListener zl = new zoomListener();

    private void initComponents() {
        buttonGroup1 = new javax.swing.ButtonGroup();
        genderGroup = new javax.swing.ButtonGroup();
        jToolBar1 = new javax.swing.JToolBar();
        open_book = new javax.swing.JButton();
        new_book = new javax.swing.JButton();
        button_save = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        add_category = new javax.swing.JButton();
        Add_Custom_Note_ = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        add_chapter = new javax.swing.JButton();
        Add_Global_Note_ = new javax.swing.JButton();
        Add_Character_ = new javax.swing.JButton();
        Add_Place_ = new javax.swing.JButton();
        remove_node = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        cardDeck = new javax.swing.JPanel();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        category_pane = new javax.swing.JEditorPane();
        jPanel1 = new javax.swing.JPanel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jMenuBar1 = new javax.swing.JMenuBar();
        FileMenu = new javax.swing.JMenu();
        NewBook = new javax.swing.JMenuItem();
        LoadBook = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        PrintBook = new javax.swing.JMenuItem();
        menuConvert = new javax.swing.JMenu();
        ConvertPDF = new javax.swing.JMenuItem();
        ConvertDoc = new javax.swing.JMenuItem();
        ConvertHtml = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        menuExit = new javax.swing.JMenuItem();
        AddMenu = new javax.swing.JMenu();
        menuAddCategory = new javax.swing.JMenuItem();
        HelpMenu = new javax.swing.JMenu();
        menu_Tutorial_ = new javax.swing.JMenuItem();
        menu_HelpTopics_ = new javax.swing.JMenuItem();
        menu_AboutWub_ = new javax.swing.JMenuItem();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(740, 480));
        setName("Form");
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });
        jToolBar1.setRollover(true);
        jToolBar1.setName("ToolBar");
        jToolBar1.setPreferredSize(new java.awt.Dimension(585, 25));
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(JFrameMain.class);
        open_book.setText(resourceMap.getString("open_book.text"));
        open_book.setFocusable(false);
        open_book.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        open_book.setName("open_book");
        open_book.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(open_book);
        new_book.setText(resourceMap.getString("new_book.text"));
        new_book.setFocusable(false);
        new_book.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        new_book.setName("new_book");
        new_book.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(new_book);
        button_save.setText(resourceMap.getString("button_save.text"));
        button_save.setName("button_save");
        button_save.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                button_saveMouseClicked(evt);
            }
        });
        jToolBar1.add(button_save);
        jSeparator3.setName("jSeparator3");
        jToolBar1.add(jSeparator3);
        add_category.setText(resourceMap.getString("add_category.text"));
        add_category.setName("add_category");
        add_category.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                add_categoryMouseClicked(evt);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                add_categoryMouseExited(evt);
            }
        });
        jToolBar1.add(add_category);
        Add_Custom_Note_.setText(resourceMap.getString("Add_Custom_Note_.text"));
        Add_Custom_Note_.setFocusable(false);
        Add_Custom_Note_.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        Add_Custom_Note_.setName("Add_Custom_Note_");
        Add_Custom_Note_.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        Add_Custom_Note_.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Add_Custom_Note_ActionPerformed(evt);
            }
        });
        jToolBar1.add(Add_Custom_Note_);
        jSeparator4.setName("jSeparator4");
        jToolBar1.add(jSeparator4);
        add_chapter.setText(resourceMap.getString("add_chapter.text"));
        add_chapter.setFocusable(false);
        add_chapter.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        add_chapter.setName("add_chapter");
        add_chapter.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        add_chapter.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                add_chapterMouseClicked(evt);
            }
        });
        jToolBar1.add(add_chapter);
        Add_Global_Note_.setText(resourceMap.getString("Add_Global_Note_.text"));
        Add_Global_Note_.setFocusable(false);
        Add_Global_Note_.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        Add_Global_Note_.setName("Add_Global_Note_");
        Add_Global_Note_.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        Add_Global_Note_.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                Add_Global_Note_MousePressed(evt);
            }
        });
        jToolBar1.add(Add_Global_Note_);
        Add_Character_.setText(resourceMap.getString("Add_Character_.text"));
        Add_Character_.setFocusable(false);
        Add_Character_.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        Add_Character_.setName("Add_Character_");
        Add_Character_.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        Add_Character_.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Add_Character_ActionPerformed(evt);
            }
        });
        jToolBar1.add(Add_Character_);
        Add_Place_.setText(resourceMap.getString("Add_Place_.text"));
        Add_Place_.setFocusable(false);
        Add_Place_.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        Add_Place_.setName("Add_Place_");
        Add_Place_.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(Add_Place_);
        remove_node.setText(resourceMap.getString("remove_node.text"));
        remove_node.setFocusable(false);
        remove_node.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        remove_node.setName("remove_node");
        remove_node.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        remove_node.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                remove_nodeMouseClicked(evt);
            }
        });
        jToolBar1.add(remove_node);
        jSeparator5.setBackground(resourceMap.getColor("jSeparator5.background"));
        jSeparator5.setBorder(javax.swing.BorderFactory.createEtchedBorder(resourceMap.getColor("jSeparator5.border.highlightColor"), null));
        jSeparator5.setName("jSeparator5");
        jToolBar1.add(jSeparator5);
        jButton1.setIcon(resourceMap.getIcon("jButton1.icon"));
        jButton1.setText(resourceMap.getString("jButton1.text"));
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setMaximumSize(new java.awt.Dimension(37, 30));
        jButton1.setMinimumSize(new java.awt.Dimension(37, 30));
        jButton1.setName("jButton1");
        jButton1.setPreferredSize(new java.awt.Dimension(37, 30));
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });
        jToolBar1.add(jButton1);
        jButton2.setText(resourceMap.getString("jButton2.text"));
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setName("jButton2");
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });
        jToolBar1.add(jButton2);
        jSplitPane1.setDividerLocation(195);
        jSplitPane1.setDividerSize(10);
        jSplitPane1.setAutoscrolls(true);
        jSplitPane1.setContinuousLayout(true);
        jSplitPane1.setMaximumSize(new java.awt.Dimension(2147, 2147));
        jSplitPane1.setMinimumSize(new java.awt.Dimension(136, 26));
        jSplitPane1.setName("jSplitPane1");
        jSplitPane1.setOneTouchExpandable(true);
        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setMaximumSize(new java.awt.Dimension(3276, 3276));
        jScrollPane1.setName("jScrollPane1");
        jTree1.setAutoscrolls(true);
        jTree1.setEditable(true);
        jTree1.setMaximumSize(new java.awt.Dimension(1000, 2000));
        jTree1.setMinimumSize(new java.awt.Dimension(50, 100));
        jTree1.setName("jTree1");
        jTree1.setPreferredSize(null);
        jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {

            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTree1ValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jTree1);
        jSplitPane1.setLeftComponent(jScrollPane1);
        cardDeck.setBackground(resourceMap.getColor("cardDeck.background"));
        cardDeck.setForeground(resourceMap.getColor("cardDeck.foreground"));
        cardDeck.setName("cardDeck");
        cardDeck.addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentResized(java.awt.event.ComponentEvent evt) {
                cardDeckComponentResized(evt);
            }
        });
        cardDeck.setLayout(new java.awt.CardLayout());
        jLayeredPane1.setName("jLayeredPane1");
        jLayeredPane1.addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentResized(java.awt.event.ComponentEvent evt) {
                jLayeredPane1ComponentResized(evt);
            }
        });
        category_pane.setDragEnabled(true);
        category_pane.setName("category_pane");
        category_pane.setPreferredSize(new java.awt.Dimension(106, 200));
        category_pane.setBounds(0, 0, 690, 470);
        jLayeredPane1.add(category_pane, javax.swing.JLayeredPane.DEFAULT_LAYER);
        cardDeck.add(jLayeredPane1, "card2");
        jSplitPane1.setRightComponent(cardDeck);
        jPanel1.setName("jPanel1");
        jPanel1.setPreferredSize(new java.awt.Dimension(5, 15));
        jProgressBar1.setAlignmentX(0.0F);
        jProgressBar1.setAlignmentY(0.0F);
        jProgressBar1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jProgressBar1.setMaximumSize(new java.awt.Dimension(32, 32));
        jProgressBar1.setName("jProgressBar1");
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup().addContainerGap(649, Short.MAX_VALUE).addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap()));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jProgressBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE));
        jMenuBar1.setName("jMenuBar1");
        FileMenu.setText(resourceMap.getString("FileMenu.text"));
        FileMenu.setName("FileMenu");
        NewBook.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        NewBook.setText(resourceMap.getString("NewBook.text"));
        NewBook.setName("NewBook");
        FileMenu.add(NewBook);
        LoadBook.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        LoadBook.setText(resourceMap.getString("LoadBook.text"));
        LoadBook.setName("LoadBook");
        FileMenu.add(LoadBook);
        jSeparator1.setName("jSeparator1");
        FileMenu.add(jSeparator1);
        PrintBook.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        PrintBook.setText(resourceMap.getString("PrintBook.text"));
        PrintBook.setName("PrintBook");
        FileMenu.add(PrintBook);
        menuConvert.setText(resourceMap.getString("menuConvert.text"));
        menuConvert.setName("menuConvert");
        ConvertPDF.setText(resourceMap.getString("ConvertPDF.text"));
        ConvertPDF.setName("ConvertPDF");
        menuConvert.add(ConvertPDF);
        ConvertDoc.setText(resourceMap.getString("ConvertDoc.text"));
        ConvertDoc.setName("ConvertDoc");
        menuConvert.add(ConvertDoc);
        ConvertHtml.setText(resourceMap.getString("ConvertHtml.text"));
        ConvertHtml.setName("ConvertHtml");
        menuConvert.add(ConvertHtml);
        FileMenu.add(menuConvert);
        jSeparator2.setName("jSeparator2");
        FileMenu.add(jSeparator2);
        menuExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        menuExit.setText(resourceMap.getString("menuExit.text"));
        menuExit.setName("menuExit");
        menuExit.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                menuExitMousePressed(evt);
            }
        });
        FileMenu.add(menuExit);
        jMenuBar1.add(FileMenu);
        AddMenu.setText(resourceMap.getString("AddMenu.text"));
        AddMenu.setName("AddMenu");
        menuAddCategory.setText(resourceMap.getString("menuAddCategory.text"));
        menuAddCategory.setName("menuAddCategory");
        menuAddCategory.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                menuAddCategoryMousePressed(evt);
            }
        });
        AddMenu.add(menuAddCategory);
        jMenuBar1.add(AddMenu);
        HelpMenu.setText(resourceMap.getString("HelpMenu.text"));
        HelpMenu.setName("HelpMenu");
        menu_Tutorial_.setText(resourceMap.getString("menu_Tutorial_.text"));
        menu_Tutorial_.setName("menu_Tutorial_");
        HelpMenu.add(menu_Tutorial_);
        menu_HelpTopics_.setText(resourceMap.getString("menu_HelpTopics_.text"));
        menu_HelpTopics_.setName("menu_HelpTopics_");
        HelpMenu.add(menu_HelpTopics_);
        menu_AboutWub_.setText(resourceMap.getString("menu_AboutWub_.text"));
        menu_AboutWub_.setName("menu_AboutWub_");
        menu_AboutWub_.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                menu_AboutWub_MousePressed(evt);
            }
        });
        HelpMenu.add(menu_AboutWub_);
        jMenuBar1.add(HelpMenu);
        setJMenuBar(jMenuBar1);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 819, Short.MAX_VALUE).addContainerGap()).addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 829, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 809, Short.MAX_VALUE).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)));
        pack();
    }

    private class showNode {

        private void initializeTree() {
            getTreeNodes();
        }

        private void getTreeNodes() {
            File dir = new File(GlobalVars.treePath);
            File[] files = dir.listFiles();
            String shortPath = null;
            String[] splitShortPath = null;
            String innerShortPath;
            for (int i = 0; i <= files.length - 1; i++) {
                splitShortPath = files[i].toString().split("\\\\");
                shortPath = splitShortPath[3];
                if (!shortPath.equalsIgnoreCase("chapterNotes")) {
                    populateTree(shortPath, "root");
                    File inner_dir = new File(GlobalVars.treePath + shortPath);
                    File[] inner_files = inner_dir.listFiles();
                    String strDir_length = inner_dir.getAbsolutePath();
                    for (int j = 0; j < inner_files.length; j++) {
                        innerShortPath = inner_files[j].toString().replace(".txt", "");
                        populateTree(innerShortPath, shortPath);
                    }
                }
            }
        }

        DefaultMutableTreeNode populateTree(String childNode, String parentNode) {
            DefaultMutableTreeNode outterNode;
            if (parentNode.equals("root")) {
                outterNode = new DefaultMutableTreeNode(childNode);
                GlobalVars.tempNode = outterNode;
                treeModel.insertNodeInto(outterNode, root, 0);
            } else {
                DefaultMutableTreeNode innerNode = new DefaultMutableTreeNode(childNode);
                treeModel.insertNodeInto(innerNode, GlobalVars.tempNode, 0);
            }
            treeModel.addTreeModelListener(new MyTreeModelListener());
            jTree_expand_width();
            jTree1.setModel(treeModel);
            jTree1.updateUI();
            return (root);
        }

        public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child, boolean shouldBeVisible) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
            treeModel.insertNodeInto(childNode, parent, parent.getChildCount());
            if (shouldBeVisible) {
                jTree1.scrollPathToVisible(new TreePath(childNode.getPath()));
            }
            return childNode;
        }

        private DefaultMutableTreeNode makeshow(String nodeName, DefaultMutableTreeNode parent) {
            DefaultMutableTreeNode show = null;
            Hashtable hash = new Hashtable();
            return show;
        }
    }

    private void displayHtml(String pageName) {
        pageName = "A-1.html";
        java.net.URL url = JFrameMain.class.getResource(pageName);
        try {
            category_pane.setPage(url);
            category_pane.setVisible(true);
            category_pane.validate();
        } catch (IOException ex) {
            Logger.getLogger(JFrameMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void formWindowOpened(java.awt.event.WindowEvent evt) {
        showNode shownode = this.new showNode();
        classCS = new ConnectScene();
        JPanel card1 = new foo();
        JPanel sbp = new SBP();
        CS = classCS.showScene();
        CS.addKeyListener(zl);
        JPanel card3 = new CharacterPane();
        JPanel card4 = new ChapterPane();
        JPanel card5 = new GeneralNotesPane();
        JPanel place = new PlacePane();
        cardDeck.add("cpc", card1);
        cardDeck.add("sbp", sbp);
        cardDeck.add("cpc3", card3);
        cardDeck.add("cpc4", card4);
        cardDeck.add("cpc5", card5);
        cardDeck.add("CS", CS);
        cardDeck.add("place", place);
        jSplitPane1.setRightComponent(cardDeck);
        root = new DefaultMutableTreeNode("main");
        treeModel = new DefaultTreeModel(root);
        jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTree1.setEditable(true);
        shownode.initializeTree();
    }

    protected static ImageIcon createImageIcon(String path) {
        try {
            java.net.URL imgURL = JFrameMain.class.getResource(path);
            return new ImageIcon(imgURL);
        } catch (NullPointerException exc) {
            System.out.println("No image found");
            return null;
        }
    }

    @SuppressWarnings("empty-statement")
    private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt) {
        try {
            System.out.println("  " + jTree1.getLastSelectedPathComponent().toString());
            String node_used = null;
            node_used = evt.getPath().getLastPathComponent().toString();
            GlobalVars.setstrParent_node(evt.getPath().getPathComponent(1).toString());
            GlobalVars.setSelected_node(jTree1.getLastSelectedPathComponent().toString());
            if (GlobalVars.getSelected_node() != null) {
                if (delete_pressed == false) {
                    save_story(previous_node);
                }
            }
            removed_node = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
            if (GlobalVars.getSelected_node().equalsIgnoreCase(GlobalVars.getstrParent_node())) {
                System.out.println("html page: " + GlobalVars.catsPath + GlobalVars.getSelected_node() + ".html");
                category_pane.setPage("file:" + GlobalVars.catsPath + GlobalVars.getSelected_node() + ".html");
            }
            if (evt.getPath().getPathCount() == 3) {
                System.out.print("line 716");
                visiblePane(GlobalVars.getstrParent_node());
                readFromFile(node_used);
            }
            previous_node = GlobalVars.getSelected_node();
            System.out.println("previous of parent: " + previous_node);
            if (GlobalVars.getstrParent_node().equalsIgnoreCase("characters")) {
                System.out.println("           this is a test....");
                CharacterPane.newCharacterSelected();
            }
            if (GlobalVars.getstrParent_node().equalsIgnoreCase("chapters")) {
                ChapterPane.newChapterSelected();
                ChapterPane.chapterText.requestFocus();
            }
        } catch (NullPointerException exc) {
        } catch (IOException ex) {
            System.out.println("File not found!!" + ex.getMessage());
        }
    }

    public void readFromFile(String node_used) {
        System.out.print("line 740");
        try {
            int k;
            FileInputStream f = null;
            if (node_used.compareToIgnoreCase("default") != 0) {
                f = new FileInputStream(GlobalVars.treePath + GlobalVars.getstrParent_node().toString() + "\\" + node_used + ".txt");
                System.out.print("line 746: " + GlobalVars.treePath + GlobalVars.getstrParent_node().toString() + "\\" + node_used + ".txt");
            }
            int len = 0;
            len = f.available();
            String s = " ";
            for (k = 1; k <= len; k++) {
                s = s + (char) f.read();
            }
            f.close();
        } catch (NullPointerException exc) {
        } catch (IOException ex) {
            ex.getMessage();
            System.out.println("File not found!");
        }
    }

    private void add_categoryMouseClicked(java.awt.event.MouseEvent evt) {
        String new_cat = "default";
        DefaultMutableTreeNode cat_node = new DefaultMutableTreeNode(new_cat);
        if (cat_node.toString().compareToIgnoreCase("") != 0) {
            try {
                File dir = new File(GlobalVars.treePath);
                File[] files = dir.listFiles();
                treeModel.insertNodeInto(cat_node, root, files.length);
                pathSelectedNode = new TreePath(cat_node.getPath());
                jTree1.startEditingAtPath(pathSelectedNode);
                File create_file = new File("\\wub\\cats\\" + cat_node + ".html");
                create_file.createNewFile();
                File create_dir = new File(GlobalVars.treePath + cat_node + "\\");
                create_dir.mkdir();
                File create_default_dir = new File("\\wub\\cats\\default.html");
                create_default_dir.createNewFile();
            } catch (IOException ex) {
                System.out.println("add cat error");
                Logger.getLogger(JFrameMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void add_a_note(String note_category) {
        GlobalVars.setSelected_node(note_category);
        try {
            String new_note = "default";
            DefaultMutableTreeNode note_node = new DefaultMutableTreeNode(new_note);
            int node_index = -1;
            int root_children = root.getChildCount() - 1;
            TreePath newTreePath = null;
            TreeNode root_child = null;
            while (root_children >= 0) {
                if ((root.getChildAt(root_children).toString()).equalsIgnoreCase(note_category) == true) {
                    String t1 = root.getChildAt(root_children).toString();
                    System.out.println("root:: " + root.getChildAt(root_children).toString().contains(note_category));
                    if (t1.compareToIgnoreCase(note_category) == 0) {
                        System.out.println("doup file");
                    }
                    root_child = root.getChildAt(root_children);
                    node_index = root_children;
                }
                root_children--;
            }
            if (node_index != -1) {
                DefaultMutableTreeNode StoryWideNotes = (DefaultMutableTreeNode) (root.getChildAt(node_index));
                int child_count_cat = root.getChildAt(node_index).getChildCount();
                if (note_node.toString().compareToIgnoreCase("") != 0) {
                    File dir = new File(GlobalVars.treePath + note_category + "\\");
                    File[] files = dir.listFiles();
                    try {
                        File create_file = new File(GlobalVars.treePath + note_category + "\\default.txt");
                        System.out.println("create file: " + create_file.toString());
                        if (create_file.createNewFile() == true) {
                            System.out.println("default created ");
                        } else {
                            System.out.println("default NOT!!! created ");
                        }
                    } catch (IOException ex) {
                        System.out.println("add a note IO ex");
                        Logger.getLogger(JFrameMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                treeModel.insertNodeInto(note_node, StoryWideNotes, child_count_cat);
                newTreePath = new TreePath(((DefaultMutableTreeNode) root_child.getChildAt(child_count_cat)).getPath());
                parent_node = (DefaultMutableTreeNode) root.getChildAt(node_index);
                jTree1.startEditingAtPath(newTreePath);
                jTree_expand_width();
            } else {
                System.out.println("There was an erroer in add_note");
            }
        } catch (NullPointerException exc) {
        }
    }

    private void add_chapterMouseClicked(java.awt.event.MouseEvent evt) {
        add_a_note("Chapters");
    }

    private void menuExitMousePressed(java.awt.event.MouseEvent evt) {
        System.out.println("should exit");
        System.exit(0);
    }

    private void menuAddCategoryMousePressed(java.awt.event.MouseEvent evt) {
        add_categoryMouseClicked(evt);
    }

    private void add_categoryMouseExited(java.awt.event.MouseEvent evt) {
        try {
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_ENTER);
        } catch (AWTException ex) {
            Logger.getLogger(JFrameMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void remove_nodeMouseClicked(java.awt.event.MouseEvent evt) {
        int choice = AreYouSure_yesno("Are you sure you want to remove this node: " + GlobalVars.getSelected_node());
        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (removed_node.getParent().toString().compareToIgnoreCase("main") != 0) {
                    System.out.println("parent node: " + parent_node + " :: " + "selected node: " + GlobalVars.getSelected_node());
                    File delete_directory = new File(GlobalVars.treePath + parent_node + "\\" + GlobalVars.getSelected_node() + ".txt");
                    deleteFiles(delete_directory);
                }
                if (removed_node.getParent().toString().compareToIgnoreCase("main") == 0) {
                    File delete_directory = new File(GlobalVars.treePath + GlobalVars.getSelected_node());
                    deleteFiles(delete_directory);
                }
            } catch (NullPointerException exc) {
            }
        }
        if (choice == JOptionPane.NO_OPTION) {
            System.out.println("no");
        }
        if (choice == JOptionPane.CANCEL_OPTION) {
        }
    }

    private void button_saveMouseClicked(java.awt.event.MouseEvent evt) {
        save_story(GlobalVars.getSelected_node());
    }

    public void save_story(String node_to_save) {
    }

    private void menu_AboutWub_MousePressed(java.awt.event.MouseEvent evt) {
        final int x = 200 + getX();
        final int y = 200 + getY();
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                About_WUB dialog = new About_WUB(new javax.swing.JFrame(), true);
                dialog.setBounds(x, y, 500, 200);
                dialog.setVisible(true);
            }
        });
    }

    private void Add_Character_ActionPerformed(java.awt.event.ActionEvent evt) {
        add_a_note("characters");
    }

    private void Add_Custom_Note_ActionPerformed(java.awt.event.ActionEvent evt) {
        String last = jTree1.getLastSelectedPathComponent().toString();
        add_a_note(last);
    }

    private void Add_Global_Note_MousePressed(java.awt.event.MouseEvent evt) {
        add_a_note("Story Wide Notes");
    }

    private void jLayeredPane1ComponentResized(java.awt.event.ComponentEvent evt) {
    }

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {
        classCS.saveTimeLine();
    }

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {
        classCS.loadTimeLine();
    }

    private void cardDeckComponentResized(java.awt.event.ComponentEvent evt) {
    }

    private class GlobalVars2 {

        String deleteThis = " gone";

        String oldAttributeData = ".";

        String fName = GlobalVars.treePath + GlobalVars.getstrParent_node().toString() + "\\" + GlobalVars.getSelected_node() + ".txt";
    }

    public static int AreYouSure_yesno(String display_text) {
        int int_yes_no_cancel = JOptionPane.showConfirmDialog((Component) null, display_text, "Remove Item", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        return int_yes_no_cancel;
    }

    private void deleteFiles(File directory) {
        delete_pressed = true;
        File files = (directory);
        File[] dir = (directory.listFiles());
        if (dir != null) {
            for (File f : dir) {
                if (f.isDirectory()) {
                    deleteFiles(f);
                } else {
                    if (f.isDirectory() == false) {
                        f.delete();
                    }
                }
            }
            File html_file = new File("\\wub\\cats\\" + GlobalVars.getSelected_node() + ".html");
            System.out.println("this is html_file:" + html_file);
            html_file.delete();
            directory.delete();
        } else {
            files.delete();
        }
        System.out.println(" is directory?: " + files.isDirectory());
        System.out.println("    files removed node: " + removed_node);
        System.out.println("    directory: " + directory);
        System.out.println("    files : " + files);
        System.out.println("    parent: " + removed_node.getParent());
        System.out.println("    previous sib: " + removed_node.getPreviousSibling());
        System.out.println("    previous node: " + removed_node.getPreviousNode());
        try {
            treeModel.removeNodeFromParent(removed_node);
        } catch (NullPointerException exc) {
        }
    }

    public void jTree_expand_width() {
        int intJT_width = 0;
        int intDisplayed_rows = jTree1.getRowCount() - 1;
        int intBiggestNode = 0;
        while (intDisplayed_rows > 0) {
            String[] split = jTree1.getPathForRow(intDisplayed_rows).toString().split(", ");
            if (jTree1.getPathForRow(intDisplayed_rows).getPathCount() == 3) {
                if (split[2].length() > intBiggestNode) {
                    intBiggestNode = split[2].length();
                }
                intJT_width = (int) (intBiggestNode * 8);
                jSplitPane1.setDividerLocation(intJT_width);
            } else if (jTree1.getPathForRow(intDisplayed_rows).getPathCount() == 2) {
                if (split[1].length() > intBiggestNode) {
                    intBiggestNode = split[1].length();
                }
                intJT_width = (int) (intBiggestNode * 9);
            }
            intDisplayed_rows--;
        }
        jSplitPane1.setDividerLocation(intJT_width);
    }

    public void visiblePane(String paneName) {
        CardLayout cl2 = (CardLayout) (cardDeck.getLayout());
        if (paneName.equalsIgnoreCase("chapters")) {
            ChapterPane.chapterTabs.setTitleAt(0, "Chapter title: " + GlobalVars.getSelected_node().toString());
            ChapterPane.chapterTabs.setTitleAt(1, "Chapter Notes");
            cl2.show(cardDeck, "cpc4");
        }
        if (paneName.equalsIgnoreCase("cartoons")) {
            JPanel x1;
            x1 = new JPanel();
            x1.add(new JButton("ahhhhhh"));
            jLayeredPane1.add(x1);
            jLayeredPane1.moveToFront(x1);
            jLayeredPane1.revalidate();
        }
        if (paneName.equalsIgnoreCase("characters")) {
            cl2.show(cardDeck, "cpc3");
            CharacterPane.characterName.setText(GlobalVars.getSelected_node());
        }
        if (paneName.equalsIgnoreCase("insperationPane")) {
        }
        if (paneName.equalsIgnoreCase("Timeline")) {
            int a = 0;
            cl2.show(cardDeck, "CS");
            CS.requestFocus();
            if (GlobalVars.getstrParent_node().contentEquals("Timeline")) {
                System.out.println("Timeline is the parent ...............");
            }
        }
        if (paneName.equalsIgnoreCase("Story Wide Notes")) {
            cl2.show(cardDeck, "cpc5");
        }
    }

    static class zoomListener implements KeyListener {

        double zoomIncrement = 1.0;

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            System.out.println("..................Something pressed...............");
            if ((e.isControlDown() == true) && (e.getKeyCode() == KeyEvent.VK_EQUALS)) {
                zoomIncrement = zoomIncrement + .10;
                GlobalVars.setZoomFactor(zoomIncrement);
                ConnectScene.zoomIn.invokeLayoutImmediately();
                System.out.println(GlobalVars.getZoomFactor());
            }
            if ((e.isControlDown() == true) && ((e.getKeyCode() == KeyEvent.VK_PLUS) || (e.getKeyCode() == KeyEvent.VK_ADD))) {
                zoomIncrement = zoomIncrement + .10;
                GlobalVars.setZoomFactor(zoomIncrement);
                ConnectScene.zoomIn.invokeLayoutImmediately();
                System.out.println(GlobalVars.getZoomFactor());
            }
            if ((e.isControlDown() == true) && ((e.getKeyCode() == KeyEvent.VK_MINUS) || (e.getKeyCode() == KeyEvent.VK_SUBTRACT))) {
                if (zoomIncrement >= 0) {
                    zoomIncrement = zoomIncrement - .10;
                    GlobalVars.setZoomFactor(zoomIncrement);
                    ConnectScene.zoomIn.invokeLayoutImmediately();
                    System.out.println(GlobalVars.getZoomFactor());
                }
            }
        }

        public void keyReleased(KeyEvent e) {
        }
    }

    class MyTreeModelListener implements TreeModelListener {

        DefaultMutableTreeNode node;

        public void treeNodesChanged(TreeModelEvent e) {
            node = (DefaultMutableTreeNode) (e.getTreePath().getLastPathComponent());
            try {
                int index = e.getChildIndices()[0];
                node = (DefaultMutableTreeNode) (node.getChildAt(index));
            } catch (NullPointerException exc) {
            }
            System.out.println("The user has finished editing the node.");
            delete_pressed = false;
            System.out.println("Old value: " + GlobalVars.getSelected_node().toString());
            Object new_value = node.getUserObject();
            jTree_expand_width();
            if (e.getTreePath().toString().equalsIgnoreCase("[main]")) {
                File cat = new File("\\wub\\cats\\" + GlobalVars.getSelected_node() + ".html");
                cat.renameTo(new File("\\wub\\cats\\" + new_value.toString() + ".html"));
                File dir = new File(GlobalVars.treePath + GlobalVars.getSelected_node());
                dir.renameTo(new File(GlobalVars.treePath + node.getUserObject()));
                GlobalVars.setSelected_node(node.getUserObject().toString());
            } else {
                System.out.println("selected node before: " + GlobalVars.getSelected_node());
                File child = new File(GlobalVars.treePath + parent_node + "\\" + GlobalVars.getSelected_node() + ".txt");
                if (child.renameTo(new File(GlobalVars.treePath + parent_node + "\\" + node.getUserObject().toString() + ".txt"))) {
                    System.out.println("worked");
                } else {
                    System.out.println("failed");
                }
                GlobalVars.setSelected_node(node.getUserObject().toString());
                System.out.println("selected node after: " + GlobalVars.getSelected_node());
            }
        }

        public void treeNodesInserted(TreeModelEvent e) {
        }

        public void treeNodesRemoved(TreeModelEvent e) {
        }

        public void treeStructureChanged(TreeModelEvent e) {
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new JFrameMain().setVisible(true);
            }
        });
    }

    private javax.swing.JMenu AddMenu;

    private javax.swing.JButton Add_Character_;

    private javax.swing.JButton Add_Custom_Note_;

    private javax.swing.JButton Add_Global_Note_;

    private javax.swing.JButton Add_Place_;

    private javax.swing.JMenuItem ConvertDoc;

    private javax.swing.JMenuItem ConvertHtml;

    private javax.swing.JMenuItem ConvertPDF;

    private javax.swing.JMenu FileMenu;

    private javax.swing.JMenu HelpMenu;

    private javax.swing.JMenuItem LoadBook;

    private javax.swing.JMenuItem NewBook;

    private javax.swing.JMenuItem PrintBook;

    private javax.swing.JButton add_category;

    private javax.swing.JButton add_chapter;

    private javax.swing.ButtonGroup buttonGroup1;

    private javax.swing.JButton button_save;

    protected static javax.swing.JPanel cardDeck;

    private javax.swing.JEditorPane category_pane;

    private javax.swing.ButtonGroup genderGroup;

    private javax.swing.JButton jButton1;

    private javax.swing.JButton jButton2;

    protected static javax.swing.JLayeredPane jLayeredPane1;

    private javax.swing.JMenuBar jMenuBar1;

    private javax.swing.JPanel jPanel1;

    protected static javax.swing.JProgressBar jProgressBar1;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JSeparator jSeparator1;

    private javax.swing.JSeparator jSeparator2;

    private javax.swing.JToolBar.Separator jSeparator3;

    private javax.swing.JToolBar.Separator jSeparator4;

    private javax.swing.JToolBar.Separator jSeparator5;

    protected static javax.swing.JSplitPane jSplitPane1;

    private javax.swing.JToolBar jToolBar1;

    protected javax.swing.JTree jTree1;

    private javax.swing.JMenuItem menuAddCategory;

    private javax.swing.JMenu menuConvert;

    private javax.swing.JMenuItem menuExit;

    private javax.swing.JMenuItem menu_AboutWub_;

    private javax.swing.JMenuItem menu_HelpTopics_;

    private javax.swing.JMenuItem menu_Tutorial_;

    private javax.swing.JButton new_book;

    private javax.swing.JButton open_book;

    private javax.swing.JButton remove_node;
}

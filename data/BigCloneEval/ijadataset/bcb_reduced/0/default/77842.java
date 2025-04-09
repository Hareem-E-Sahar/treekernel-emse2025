import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.text.rtf.*;
import javax.swing.undo.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.tree.*;
import java.awt.print.*;
import javax.print.attribute.*;
import javax.swing.text.rtf.*;
import javax.swing.undo.*;

public class GhinWebit extends JFrame implements ActionListener {

    protected JFileChooser filechooser;

    public String sfile;

    public JEditorPane my_monitor;

    public JTextComponent textComp;

    public StyleContext my_context;

    public Document my_doc;

    public RTFEditorKit my_kit;

    protected JFileChooser my_chooser;

    public JTextPane textPaneView;

    public HTMLEditorKit htdoc;

    public StyledEditorKit textPane_kit;

    public File my_FChoosen;

    public String my_OpenFile = "";

    public static final ImageIcon ICON_COMPUTER = new ImageIcon(ClassLoader.getSystemResource("folder.png"));

    public static final ImageIcon ICON_FILES = new ImageIcon(ClassLoader.getSystemResource("kword.png"));

    protected DefaultTreeModel myTreeModel;

    private JSplitPane splitMe;

    private DefaultMutableTreeNode top;

    private File sourceDir, openRunwith;

    private JTree tree, myTree;

    public String treeSelect;

    private int indexHome = 0;

    public final JLabel statusBar;

    private JComboBox fontNamejb;

    private JComboBox fontSizejb;

    private JComboBox fontColorjb;

    private JComboBox fontBahasajb;

    private GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

    private String[] fontNames = ge.getAvailableFontFamilyNames();

    private Canvas fontcanvas = new Canvas();

    private Canvas fontbgcanvas = new Canvas();

    private String font_file = "fontsetting.conf";

    private String set_fontName = "";

    private String set_bahasa = "";

    private int set_fontSize = 0;

    private Color cf = Color.black;

    private Color cb = Color.white;

    private JScrollPane ps;

    private String myhome;

    private ReadFontConfig ReadfnConf;

    private JScrollPane treeSCPan;

    private MyToolbar toolbar;

    private TreePath selPath;

    public JTextField outdirfile = new JTextField(20);

    public JTextField openLinkWith = new JTextField(20);

    private JPanel myLeftPanel;

    private String setoutdir;

    private boolean isfilesource;

    private int Clikme = 0;

    public GhinWebit() {
        super("GhinWebit ");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image image = kit.getImage(ClassLoader.getSystemResource("logo.gif"));
        setIconImage(image);
        UIManager.put("Tree.expandedIcon", new ImageIcon(ClassLoader.getSystemResource("open.png")));
        UIManager.put("Tree.collapsedIcon", new ImageIcon(ClassLoader.getSystemResource("closed.png")));
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception evt) {
        }
        Runtime r = Runtime.getRuntime();
        Properties p = System.getProperties();
        myhome = (String) p.get("user.dir");
        ReadfnConf = new ReadFontConfig(myhome + File.separatorChar + font_file);
        openRunwith = new File(ReadfnConf.getOpenLinkWith());
        JPanel nortPanel = new JPanel();
        nortPanel.setLayout(new GridLayout(1, 2, 3, 3));
        toolbar = new MyToolbar();
        for (int i = 0; i < toolbar.imageName.length; i++) {
            toolbar.button[i].addActionListener(this);
        }
        nortPanel.add(toolbar);
        getContentPane().add(nortPanel, BorderLayout.NORTH);
        statusBar = new JLabel(" ");
        getContentPane().add(statusBar, BorderLayout.SOUTH);
        textPaneView = new JTextPane();
        textPane_kit = new StyledEditorKit();
        textPaneView.setEditorKit(textPane_kit);
        my_monitor = new JEditorPane();
        my_monitor.setEditable(false);
        htdoc = new HTMLEditorKit();
        my_monitor.setEditorKit(htdoc);
        my_monitor.addHyperlinkListener(new SimpleLinkListener(textPaneView, "", statusBar));
        my_context = new StyleContext();
        my_doc = new DefaultStyledDocument(my_context);
        my_doc = htdoc.createDefaultDocument();
        my_monitor.setDocument(my_doc);
        textPaneView.requestFocus();
        textPaneView.grabFocus();
        textPaneView.setSelectionStart(1);
        setFontAttributte();
        ps = new JScrollPane(my_monitor);
        top = new DefaultMutableTreeNode(new IconData(ICON_COMPUTER, null, "My Project  "));
        myTreeModel = new DefaultTreeModel(top);
        sourceDir = new File(ReadfnConf.getWorkingDir());
        if (sourceDir == null) sourceDir = new File(myhome); else sourceDir = new File("" + ReadfnConf.getWorkingDir());
        setCurrentFile(sourceDir);
        File[] roots = sourceDir.listFiles();
        for (int k = 0; k < roots.length; k++) {
            myTreeModel.insertNodeInto(new DefaultMutableTreeNode(new IconData(ICON_FILES, null, roots[k].getName())), top, k);
        }
        myTree = new JTree(myTreeModel);
        myTree.setSelectionRow(0);
        myTree.putClientProperty("JTree.lineStyle", "Angled");
        myTree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent te) {
                try {
                    selPath = te.getNewLeadSelectionPath();
                    if (selPath != null) {
                        String treeSelect = selPath.getLastPathComponent().toString();
                        if (treeSelect.startsWith("C:")) my_OpenFile = treeSelect; else my_OpenFile = sourceDir.getAbsolutePath() + File.separator + treeSelect;
                        statusBar.setText("" + my_OpenFile);
                    }
                } catch (Exception ex) {
                    warnme("Error select file directory " + ex);
                }
            }
        });
        MouseListener mouseListener = new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                int selRow = myTree.getRowForLocation(e.getX(), e.getY());
                selPath = myTree.getPathForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    if (e.getClickCount() == 1) {
                    }
                    if (e.getClickCount() == 2) {
                        File dir = new File(my_OpenFile);
                        toolbar.button[9].setVisible(true);
                        toolbar.button[7].setVisible(true);
                        if (dir.isDirectory()) {
                            my_monitor.setText(dirHTML(dir.toString()));
                            textPaneView.repaint();
                        } else if (dir.isFile()) {
                            if (my_OpenFile.endsWith("gif") || my_OpenFile.endsWith("png") || my_OpenFile.endsWith("jpg")) {
                                my_monitor.setText(insertMyImage(my_OpenFile));
                            } else if (my_OpenFile.endsWith("html") || my_OpenFile.endsWith("htm")) {
                                try {
                                    goEditHTML(my_OpenFile);
                                } catch (Throwable err) {
                                }
                            }
                        }
                    }
                }
            }
        };
        myTree.addMouseListener(mouseListener);
        TreeCellRenderer renderer = new IconCellRenderer();
        myTree.setCellRenderer(renderer);
        myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        myTree.setShowsRootHandles(true);
        myTree.setEditable(false);
        treeSCPan = new JScrollPane();
        treeSCPan.getViewport().add(myTree);
        splitMe = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeSCPan, ps);
        splitMe.setMinimumSize(new Dimension(150, 170));
        splitMe.setOneTouchExpandable(true);
        splitMe.setResizeWeight(0.2);
        splitMe.setDividerSize(17);
        getContentPane().add(splitMe, BorderLayout.CENTER);
        JMenuBar menuBar = createMenuBar();
        setJMenuBar(menuBar);
        my_chooser = new JFileChooser();
        my_chooser.setCurrentDirectory(new File("."));
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setSize(750, 500);
        setLocation((screenSize.width - 750) / 2, (screenSize.height - 500) / 2);
        setVisible(true);
    }

    protected JMenuBar createMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        JMenu mFile = new JMenu("File");
        mFile.setMnemonic('f');
        JMenuItem item = new JMenuItem("New");
        item.setMnemonic('n');
        ActionListener lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                my_OpenFile = sourceDir.getAbsolutePath() + File.separator + "Default.html";
                goEditHTML(my_OpenFile);
                isfilesource = false;
            }
        };
        item.addActionListener(lst);
        mFile.add(item);
        item = new JMenuItem("Open...");
        item.setMnemonic('o');
        lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Thread runner = new Thread() {

                    public void run() {
                        if (my_chooser.showOpenDialog(GhinWebit.this) != JFileChooser.APPROVE_OPTION) return;
                        GhinWebit.this.repaint();
                        my_FChoosen = my_chooser.getSelectedFile();
                        my_OpenFile = my_chooser.getSelectedFile().toString();
                        sourceHTML(my_OpenFile);
                        toolbar.button[9].setVisible(false);
                        toolbar.button[7].setVisible(true);
                        int newIndex1 = top.getChildCount();
                        if (my_FChoosen.getAbsolutePath() != sourceDir.getAbsolutePath()) myTreeModel.insertNodeInto(new DefaultMutableTreeNode(new IconData(ICON_FILES, null, my_FChoosen.toString())), top, newIndex1); else myTreeModel.insertNodeInto(new DefaultMutableTreeNode(new IconData(ICON_FILES, null, my_FChoosen.getName())), top, newIndex1);
                        myTree.expandRow(newIndex1);
                        myTree.setSelectionRow(newIndex1 + 1);
                        myTree.repaint();
                    }
                };
                runner.start();
            }
        };
        item.addActionListener(lst);
        mFile.add(item);
        item = new JMenuItem("Save As...");
        item.setMnemonic('A');
        lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                GhinWebit.this.repaint();
                Thread runner = new Thread() {

                    public void run() {
                        saveFile();
                    }
                };
                runner.start();
            }
        };
        item.addActionListener(lst);
        mFile.add(item);
        mFile.addSeparator();
        item = new JMenuItem("View HTML...");
        item.setMnemonic('H');
        lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Thread runner = new Thread() {

                    public void run() {
                        try {
                            goViewHTML(my_OpenFile);
                        } catch (Exception ex) {
                            warnme("check the HTML file ");
                        }
                    }
                };
                runner.start();
            }
        };
        item.addActionListener(lst);
        mFile.add(item);
        item = new JMenuItem("Edit HTML...");
        item.setMnemonic('H');
        lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Thread runner = new Thread() {

                    public void run() {
                        try {
                            goEditHTML(my_OpenFile);
                        } catch (Exception ex) {
                            warnme("check the HTML file ");
                        }
                    }
                };
                runner.start();
            }
        };
        item.addActionListener(lst);
        mFile.add(item);
        mFile.addSeparator();
        item = new JMenuItem("View Source...");
        item.setMnemonic('r');
        lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Thread runner = new Thread() {

                    public void run() {
                        sourceHTML(my_OpenFile.toString());
                    }
                };
                runner.start();
            }
        };
        item.addActionListener(lst);
        mFile.add(item);
        item = new JMenuItem("About Ghinwebit 1.0");
        item.setMnemonic('A');
        lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("About Ghinwebit 1.0");
                ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource("logo.gif"));
                JLabel label1 = new JLabel(icon);
                frame.add("North", label1);
                JLabel label2 = new JLabel("<html><li>Ghinwebit 1.0� " + "</li><li><p>Ver# 1.0 </li>" + "<li><p>Develop by: Goen-Ghin</li><li><p>JavaGeo Technology System</li><li>" + "<p>Copyright<font size=\"2\">�</font> June 2008 @Pekanbaru Riau Indonesia</li></html>");
                label2.setFont(new Font("Tahoma", Font.PLAIN, 11));
                frame.add(label2);
                Toolkit kit = Toolkit.getDefaultToolkit();
                Image image = kit.getImage(ClassLoader.getSystemResource("logo.gif"));
                frame.setIconImage(image);
                java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                frame.setSize(new java.awt.Dimension(240, 150));
                frame.setLocation((screenSize.width - 240) / 2, (screenSize.height - 240) / 2);
                frame.setVisible(true);
            }
        };
        item.addActionListener(lst);
        mFile.add(item);
        item = new JMenuItem("Exit");
        item.setMnemonic('x');
        lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };
        item.addActionListener(lst);
        mFile.add(item);
        menuBar.add(mFile);
        return menuBar;
    }

    void sourceHTML(String filename) {
        try {
            isfilesource = true;
            File myFile = new File(filename);
            InputStream in = new FileInputStream(myFile);
            my_doc = htdoc.createDefaultDocument();
            textPane_kit.read(in, my_doc, 0);
            my_monitor.setDocument(my_doc);
            textPaneView.setDocument(my_doc);
            my_monitor.setEditable(true);
            textPaneView.setEditable(true);
            textPaneView.grabFocus();
            setFontAttributte();
            in.close();
        } catch (Exception ex) {
            warnme("check the HTML file ");
        }
    }

    void saveFile() {
        if (my_chooser.showSaveDialog(GhinWebit.this) != JFileChooser.APPROVE_OPTION) return;
        GhinWebit.this.repaint();
        File fChoosen = my_chooser.getSelectedFile();
        if (isfilesource == true) {
            saveAsCODE(fChoosen.getAbsolutePath());
        } else if (isfilesource == false) {
            saveAsHTML(fChoosen.getAbsolutePath());
        }
        int newIndex1 = top.getChildCount();
        myTreeModel.insertNodeInto(new DefaultMutableTreeNode(new IconData(ICON_FILES, null, fChoosen.getAbsolutePath())), top, newIndex1);
        myTree.expandRow(newIndex1);
        myTree.setSelectionRow(newIndex1 + 1);
        myTree.repaint();
        my_chooser.rescanCurrentDirectory();
    }

    void goEditHTML(String filename) {
        try {
            isfilesource = false;
            my_monitor.setPage("file:/" + filename);
            my_monitor.setEditable(true);
            textPaneView.setEditable(false);
            setFontAttributte();
        } catch (Exception ex) {
        }
    }

    void goViewHTML(String filename) {
        try {
            isfilesource = false;
            my_monitor.setPage("file:/" + filename);
            my_monitor.setEditable(false);
            textPaneView.setEditable(false);
            setFontAttributte();
        } catch (Exception ex) {
        }
    }

    void runHTML(String address) {
        String dest = address;
        try {
            if (dest.length() > 0) {
                String checkForProtocol = dest.substring(0, 7).toLowerCase();
                if (!checkForProtocol.equals("http://") && !checkForProtocol.startsWith("file:/")) {
                    if (checkForProtocol.indexOf(':') == 1) {
                        dest = "file:/" + dest;
                    } else {
                        dest = "http://" + dest;
                    }
                }
            }
            runFile(openRunwith.toString(), dest);
        } catch (Exception ex) {
        }
    }

    void runFile(String runcomfile, String file) {
        try {
            String s = null;
            Runtime r = Runtime.getRuntime();
            Properties p = System.getProperties();
            Process ps = null;
            ps = r.exec(runcomfile + " " + file);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            BufferedReader stdOutput1 = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
                if ((s = stdInput.readLine()) == null) {
                    System.out.println("not running...");
                }
            }
            while ((s = stdOutput1.readLine()) != null) {
                System.out.println(s);
            }
            stdInput.close();
            stdOutput1.close();
        } catch (IOException e) {
            warnme("Error open file " + e);
        }
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == toolbar.button[0]) {
            Thread runner0 = new Thread() {

                public void run() {
                    my_OpenFile = sourceDir.getAbsolutePath() + File.separator + "Default.html";
                    goEditHTML(my_OpenFile);
                }
            };
            runner0.start();
        }
        if (ae.getSource() == toolbar.button[1]) {
            Thread runner1 = new Thread() {

                public void run() {
                    myTree.setSelectionRow(0);
                }
            };
            runner1.start();
        }
        if (ae.getSource() == toolbar.button[2]) {
            Thread runner2 = new Thread() {

                public void run() {
                    Clikme = myTree.getLeadSelectionRow();
                    Clikme++;
                    if (Clikme >= 0) myTree.setSelectionRow(Clikme);
                    if (Clikme >= myTree.getRowCount()) {
                        Clikme = 0;
                        myTree.setSelectionRow(0);
                    }
                }
            };
            runner2.start();
        }
        if (ae.getSource() == toolbar.button[3]) {
            Thread runner3 = new Thread() {

                public void run() {
                    Clikme = myTree.getLeadSelectionRow();
                    Clikme--;
                    if (Clikme <= myTree.getRowCount()) myTree.setSelectionRow(Clikme);
                    if (Clikme <= 0) {
                        Clikme = 0;
                        myTree.setSelectionRow(0);
                    }
                }
            };
            runner3.start();
        }
        if (ae.getSource() == toolbar.button[4]) {
            Thread printloh = new Thread() {

                public void run() {
                    try {
                        if (isfilesource == true) {
                            try {
                                PrinterJob jobprint = PrinterJob.getPrinterJob();
                                PageFormat pf = jobprint.pageDialog(jobprint.defaultPage());
                                PrintFile pfile = new PrintFile(my_OpenFile);
                                jobprint.setPrintable(pfile, pf);
                                jobprint.setCopies(1);
                                if (jobprint.printDialog()) {
                                    try {
                                        jobprint.print();
                                    } catch (Exception e) {
                                        warnme("Error due to " + e.getClass() + e.getMessage());
                                    }
                                }
                            } catch (Exception e) {
                                warnme("Error due to " + e.getClass() + e.getMessage());
                            }
                        } else if (isfilesource == false) printHTMLview();
                    } catch (Exception e) {
                        warnme("Error due to " + e.getClass() + e.getMessage());
                    }
                }
            };
            printloh.start();
        }
        if (ae.getSource() == toolbar.button[5]) {
            Thread optionrun = new Thread() {

                public void run() {
                    try {
                        JFrame frame = new JFrame("Set Configuration");
                        frame.setContentPane(JPanelFontOpen(frame));
                        Toolkit kit = Toolkit.getDefaultToolkit();
                        Image image = kit.getImage(ClassLoader.getSystemResource("logo.gif"));
                        frame.setIconImage(image);
                        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                        frame.setSize(new java.awt.Dimension(420, 310));
                        frame.setLocation((screenSize.width - 420) / 2, (screenSize.height - 310) / 2);
                        frame.setVisible(true);
                    } catch (Exception e) {
                        warnme("Error due to " + e.getClass() + e.getMessage());
                    }
                }
            };
            optionrun.start();
        }
        if (ae.getSource() == toolbar.button[6]) {
            Thread saverun = new Thread() {

                public void run() {
                    try {
                        saveFile();
                    } catch (Exception e) {
                        warnme("Error due to " + e.getClass() + e.getMessage());
                    }
                }
            };
            saverun.start();
        }
        if (ae.getSource() == toolbar.button[7]) {
            Thread webedrun = new Thread() {

                public void run() {
                    try {
                        toolbar.button[9].setVisible(true);
                        toolbar.button[7].setVisible(false);
                        saveCODE(my_OpenFile);
                        goEditHTML(my_OpenFile);
                    } catch (Exception e) {
                        warnme("Error due to " + e.getClass() + e.getMessage());
                    }
                }
            };
            webedrun.start();
        }
        if (ae.getSource() == toolbar.button[8]) {
            Thread webrun = new Thread() {

                public void run() {
                    try {
                        if (my_OpenFile == "") return;
                        goViewHTML(my_OpenFile);
                    } catch (Exception e) {
                        warnme("Error due to " + e.getClass() + e.getMessage());
                    }
                }
            };
            webrun.start();
        }
        if (ae.getSource() == toolbar.button[9]) {
            Thread coderun = new Thread() {

                public void run() {
                    try {
                        if (my_OpenFile == "") return;
                        toolbar.button[9].setVisible(false);
                        toolbar.button[7].setVisible(true);
                        saveHTML(my_OpenFile);
                        sourceHTML(my_OpenFile);
                        setFontAttributte();
                    } catch (Exception e) {
                        warnme("Error due to " + e.getClass() + e.getMessage());
                    }
                }
            };
            coderun.start();
        }
        if (ae.getSource() == toolbar.button[10]) {
            System.out.println("Terima kasih, (thanks)");
            System.exit(0);
        }
    }

    public JPanel JPanelFontOpen(final JFrame j) {
        final JPanel myPanel = new JPanel();
        myPanel.setLayout(new BorderLayout());
        myPanel.setBorder(new TitledBorder(new EtchedBorder(), "Option"));
        JFileChooser filechooser;
        JPanel myLabelPanel = new JPanel();
        JLabel fontNameLabel = new JLabel(" Font Name: ");
        JLabel fontSizeLabel = new JLabel(" Font Size: ");
        JLabel fontColorLabel = new JLabel(" Font Color: ");
        JLabel fontBgColorLabel = new JLabel(" Background Color: ");
        JLabel outdirLabel = new JLabel(" Project Directory : ");
        JLabel openlinkLabel = new JLabel(" Open Link With : ");
        JPanel myTextFieldPanel = new JPanel();
        JTextField fontName = new JTextField();
        JTextField fontColor = new JTextField();
        JTextField fontSize = new JTextField();
        JPanel myButtondPanel = new JPanel();
        JButton fontColorButton;
        JButton fontBackgroundButton;
        JButton brosCurvebutton;
        JButton applyButton;
        JButton closeButton;
        JButton brosoutdirbutton;
        JButton openLinkButton;
        ReadfnConf = new ReadFontConfig(myhome + File.separatorChar + font_file);
        myLabelPanel.setLayout(new GridLayout(7, 1, 5, 15));
        myLabelPanel.add(fontColorLabel);
        myLabelPanel.add(fontBgColorLabel);
        myLabelPanel.add(fontNameLabel);
        myLabelPanel.add(fontSizeLabel);
        myLabelPanel.add(outdirLabel);
        myLabelPanel.add(openlinkLabel);
        myPanel.add("West", myLabelPanel);
        myTextFieldPanel.setLayout(new GridLayout(7, 1, 5, 5));
        fontNamejb = new JComboBox();
        for (int a = 0; a < fontNames.length; a++) {
            fontNamejb.addItem(fontNames[a]);
        }
        fontBahasajb = new JComboBox();
        fontSizejb = new JComboBox(new String[] { "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72" });
        fontcanvas = new Canvas();
        fontbgcanvas = new Canvas();
        myTextFieldPanel.add(fontcanvas);
        myTextFieldPanel.add(fontbgcanvas);
        myTextFieldPanel.add(fontNamejb);
        myTextFieldPanel.add(fontSizejb);
        outdirfile.setText("" + ReadfnConf.getWorkingDir());
        myTextFieldPanel.add(outdirfile);
        openLinkWith.setText("" + ReadfnConf.getOpenLinkWith());
        myTextFieldPanel.add(openLinkWith);
        myPanel.add("Center", myTextFieldPanel);
        myButtondPanel.setLayout(new GridLayout(7, 1, 5, 5));
        fontColorButton = new JButton("..");
        fontColorButton.addActionListener(this);
        fontBackgroundButton = new JButton("..");
        fontBackgroundButton.addActionListener(this);
        myButtondPanel.add(fontColorButton);
        myButtondPanel.add(fontBackgroundButton);
        JLabel jl1 = new JLabel("");
        JLabel jl2 = new JLabel("");
        myButtondPanel.add(jl1);
        myButtondPanel.add(jl2);
        brosoutdirbutton = new JButton("..");
        myButtondPanel.add(brosoutdirbutton);
        brosoutdirbutton.addActionListener(this);
        openLinkButton = new JButton("..");
        myButtondPanel.add(openLinkButton);
        openLinkButton.addActionListener(this);
        myPanel.add("East", myButtondPanel);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4, 5, 5));
        applyButton = new JButton("Apply");
        applyButton.addActionListener(this);
        JButton defaultButton = new JButton("Default");
        defaultButton.addActionListener(this);
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        closeButton = new JButton("Close");
        closeButton.addActionListener(this);
        buttonPanel.add(applyButton);
        buttonPanel.add(defaultButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(closeButton);
        myPanel.add("South", buttonPanel);
        applyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setoutdir = outdirfile.getText();
                Font fn = new Font(ReadfnConf.getFontName(), Font.PLAIN, ReadfnConf.getFontSize());
                set_fontName = fontNamejb.getSelectedItem().toString();
                int fontSize = Integer.parseInt(fontSizejb.getSelectedItem().toString());
                set_fontSize = fontSize;
                MutableAttributeSet attr = new SimpleAttributeSet();
                StyleConstants.setFontFamily(attr, set_fontName);
                StyleConstants.setFontSize(attr, set_fontSize);
                StyleConstants.setForeground(attr, cf);
                StyleConstants.setBackground(attr, cb);
                getDocument().setCharacterAttributes(0, my_doc.getLength(), attr, false);
                textPaneView.grabFocus();
                textPaneView.repaint();
                try {
                    FileOutputStream outfo = new FileOutputStream(font_file);
                    DataOutputStream outst = new DataOutputStream(outfo);
                    outst.writeUTF(" " + "\n" + "FontName:" + set_fontName + "\n" + "FontSize:" + set_fontSize + "\n" + "FontColor:" + cf.getRGB() + "\n" + "FontBgColor:" + cb.getRGB() + "\n" + "WorkingDir:" + setoutdir + "\n" + "OpenLinkWith:" + openLinkWith.getText() + "\n");
                    ;
                    outst.close();
                    outfo.close();
                    top.removeAllChildren();
                    top.removeFromParent();
                    myTreeModel.reload();
                    sourceDir = new File("" + setoutdir);
                    if (!sourceDir.exists()) warnme("Directory Not Found"); else if (sourceDir != null || sourceDir.exists()) {
                        File[] roots = sourceDir.listFiles();
                        for (int k = 0; k < roots.length; k++) {
                            myTreeModel.insertNodeInto(new DefaultMutableTreeNode(new IconData(ICON_FILES, null, roots[k].getName())), top, k);
                        }
                        myTree.expandRow(0);
                        myTree.repaint();
                    }
                    setCurrentFile(sourceDir);
                } catch (IOException ie) {
                }
                j.show();
            }
        });
        defaultButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fontNamejb.setSelectedItem("Arial");
                fontSizejb.setSelectedItem(12);
                fontcanvas.setBackground(Color.black);
                fontbgcanvas.setBackground(Color.white);
                repaint();
            }
        });
        resetButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ReadfnConf = new ReadFontConfig(font_file);
                fontNamejb.setSelectedItem(ReadfnConf.getFontName());
                fontSizejb.setSelectedItem(ReadfnConf.getFontSize());
                fontcanvas.setBackground(new Color(ReadfnConf.getFontColor()));
                fontbgcanvas.setBackground(new Color(ReadfnConf.getFontBgColor()));
            }
        });
        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                j.dispose();
            }
        });
        brosoutdirbutton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(".");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                File file = new File(".");
                if (chooser.showDialog(myPanel, "Select") == JFileChooser.APPROVE_OPTION) {
                    file = chooser.getSelectedFile();
                    outdirfile.setText(file.getPath());
                    sourceDir = file;
                }
            }
        });
        openLinkButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(".");
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                File file = new File(".");
                if (chooser.showDialog(myPanel, "Select") == JFileChooser.APPROVE_OPTION) {
                    file = chooser.getSelectedFile();
                    openLinkWith.setText(file.getAbsolutePath());
                    openRunwith = file;
                }
            }
        });
        fontColorButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cf = JColorChooser.showDialog(((Component) null), "set Font Color", Color.blue);
                fontcanvas.setBackground(cf);
                if (cf == null) fontbgcanvas.setBackground(Color.white);
            }
        });
        fontBackgroundButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cb = JColorChooser.showDialog(((Component) null), "set Font Background Color", Color.blue);
                fontbgcanvas.setBackground(cb);
                if (cb == null) fontbgcanvas.setBackground(Color.blue);
            }
        });
        fontSetOpen(myhome + File.separatorChar + font_file);
        return myPanel;
    }

    public static String insertMyImage(String filename) {
        StringWriter sout = new StringWriter();
        PrintWriter out = new PrintWriter(sout);
        String html_file = "";
        out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
        out.print("<CENTER>");
        out.println("<H2>  Ghinwebit Is A Simple HTML Editor </H2>");
        out.println("<H2>  Javageo.com - Pekanbaru Riau Indonesia </H2>");
        out.print("<TABLE BORDER=2 WIDTH=440 bgcolor=GRAY><TR>");
        out.print("<TH bgcolor=BLUE>Name: " + filename + "</TH>");
        out.println("</TR>");
        File f = new File(filename);
        out.println("<TR><TD>" + "<p align=\"center\">" + "<img src=\"" + "file:\\" + filename + "\"" + " align=" + "\"" + "middle\"" + " HEIGHT=" + "\"100\"" + " WIDTH=" + "\"144\"" + ">" + "</p>" + "</TD></TR>");
        out.println("</TABLE>");
        out.print("</CENTER>");
        out.close();
        return sout.toString();
    }

    protected void saveAsHTML(String file) {
        try {
            File myfile = new File(file);
            if (myfile.exists()) {
                if (JOptionPane.showConfirmDialog(this, "Save Edit ?", "Save", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) return;
            }
            HTMLDocument my_doc = (HTMLDocument) my_monitor.getDocument();
            HTMLEditorKit mykit = new HTMLEditorKit();
            my_monitor.setEditorKit(mykit);
            OutputStream out = new FileOutputStream(myfile);
            mykit.write(out, my_doc, 0, my_doc.getLength());
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void saveHTML(String file) {
        try {
            File myfile = new File(file);
            HTMLDocument my_doc = (HTMLDocument) my_monitor.getDocument();
            HTMLEditorKit mykit = new HTMLEditorKit();
            my_monitor.setEditorKit(mykit);
            OutputStream out = new FileOutputStream(myfile);
            mykit.write(out, my_doc, 0, my_doc.getLength());
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void saveAsCODE(String file) {
        try {
            File myfile = new File(file);
            if (JOptionPane.showConfirmDialog(this, "Save Edit?", "Save", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) return;
            OutputStream out = new FileOutputStream(myfile);
            textPane_kit.write(out, my_doc, 0, my_doc.getLength());
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void saveCODE(String file) {
        try {
            File myfile = new File(file);
            OutputStream out = new FileOutputStream(myfile);
            textPane_kit.write(out, my_doc, 0, my_doc.getLength());
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void setCurrentFile(File file) {
        if (file != null) {
            setTitle("GhinWebit 1.0 [ Working Project :" + file.getPath() + "]");
        }
    }

    public DefaultStyledDocument getDocument() {
        return (DefaultStyledDocument) my_doc;
    }

    public void setFontAttributte() {
        ReadfnConf = new ReadFontConfig(myhome + File.separatorChar + font_file);
        MutableAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attr, ReadfnConf.getFontName());
        StyleConstants.setFontSize(attr, ReadfnConf.getFontSize());
        StyleConstants.setForeground(attr, new Color(ReadfnConf.getFontColor()));
        StyleConstants.setBackground(attr, new Color(ReadfnConf.getFontBgColor()));
        getDocument().setCharacterAttributes(0, my_doc.getLength(), attr, false);
    }

    public void fontSetOpen(String configFile) {
        ReadfnConf = new ReadFontConfig(myhome + File.separatorChar + font_file);
        fontNamejb.setSelectedItem(ReadfnConf.getFontName());
        fontSizejb.setSelectedItem(ReadfnConf.getFontSize());
        fontcanvas.setBackground(new Color(ReadfnConf.getFontColor()));
        fontbgcanvas.setBackground(new Color(ReadfnConf.getFontBgColor()));
    }

    public void printHTMLview() {
        RTFRenderer RTFRenderer1 = new RTFRenderer();
        RTFRenderer1.setpane(my_monitor);
        try {
            PrinterJob prnJob = PrinterJob.getPrinterJob();
            PageFormat format = prnJob.pageDialog(prnJob.defaultPage());
            prnJob.setPrintable(RTFRenderer1, format);
            if (prnJob.printDialog() == false) {
                return;
            }
            prnJob.print();
        } catch (PrinterException e) {
            warnme("A printing error has occurred");
        }
    }

    public void warnme(String message) {
        JOptionPane.showMessageDialog(new JFrame(), message, "Warning", JOptionPane.INFORMATION_MESSAGE);
    }

    public static String defaultHTML() {
        StringWriter sout = new StringWriter();
        PrintWriter out = new PrintWriter(sout);
        String html_file = "";
        out.print("<CENTER>");
        out.println("<H2>  Welcome Ghinwebit Is A Simple HTML Editor </H2>");
        out.println("<H1>" + "Hi" + "</H1>");
        out.print("<TABLE BORDER=2 WIDTH=640 bgcolor=GRAY><TR>");
        out.print("<TH bgcolor=BLUE>Name</TH><TH bgcolor=BLUE>Size</TH><TH bgcolor=BLUE>Modified</TH>");
        out.println("<TH bgcolor=BLUE>Read</TH><TH bgcolor=BLUE>Write</TH></TR>");
        out.println("<TR><TD>" + "test" + "<a href=\"" + "test" + "\">" + "test" + "</a>" + "</TD><TD align=center HEIGHT=10 WIDTH=10>" + "test" + "</TD><TD align=center HEIGHT=10 WIDTH=190>" + "test" + "</TD><TD align=center HEIGHT=10 WIDTH=10>" + "test" + "</TD><TD align=center HEIGHT=10 WIDTH=10>" + "test" + "</TD></TR>");
        out.println("</TABLE>");
        out.print("</CENTER>");
        out.close();
        return sout.toString();
    }

    public static String dirHTML(String dirname) {
        File dir = new File(dirname);
        String[] entries = dir.list();
        StringWriter sout = new StringWriter();
        PrintWriter out = new PrintWriter(sout);
        String html_file = "";
        out.print("<CENTER>");
        out.println("<H2>  Ghinwebit Is A Simple HTML Editor </H2>");
        out.println("<H1>" + dirname + "</H1>");
        out.print("<TABLE BORDER=2 WIDTH=640 bgcolor=GRAY><TR>");
        out.print("<TH bgcolor=BLUE>Name</TH><TH bgcolor=BLUE>Size</TH><TH bgcolor=BLUE>Modified</TH>");
        out.println("<TH bgcolor=BLUE>Read</TH><TH bgcolor=BLUE>Write</TH></TR>");
        for (int i = 0; i < entries.length; i++) {
            File f = new File(dir, entries[i]);
            if (f.toString().endsWith(".htm") || f.toString().endsWith(".html")) html_file = entries[i];
            out.println("<TR><TD>" + (f.isDirectory() ? "<a href=\"" + f + "\">" + entries[i] + "</a>" : entries[i]) + "</TD><TD align=center HEIGHT=10 WIDTH=10>" + f.length() + "</TD><TD align=center HEIGHT=10 WIDTH=190>" + new Date(f.lastModified()) + "</TD><TD align=center HEIGHT=10 WIDTH=10>" + (f.canRead() ? "x" : " ") + "</TD><TD align=center HEIGHT=10 WIDTH=10>" + (f.canWrite() ? "x" : " ") + "</TD></TR>");
        }
        out.println("</TABLE>");
        out.print("</CENTER>");
        out.close();
        return sout.toString();
    }

    public static void main(String argv[]) {
        final GhinWebit gw = new GhinWebit();
    }

    class SimpleLinkListener implements HyperlinkListener {

        private JTextPane pane;

        private String urlField;

        private JLabel statusBar;

        public SimpleLinkListener(JTextPane jep, String jtf, JLabel jl) {
            pane = jep;
            urlField = jtf;
            statusBar = jl;
        }

        public SimpleLinkListener(JTextPane jep) {
            this(jep, null, null);
            jep.setContentType("text/htm");
        }

        public void hyperlinkUpdate(HyperlinkEvent he) {
            if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                final String dest = he.getURL().toString();
                HyperlinkEvent.EventType type = he.getEventType();
                if (type == HyperlinkEvent.EventType.ENTERED) {
                    if (statusBar != null) {
                        statusBar.setText(he.getURL().toString());
                    }
                } else if (type == HyperlinkEvent.EventType.EXITED) {
                    if (statusBar != null) {
                        statusBar.setText(" ");
                    }
                } else if (type == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        runHTML(he.getURL().toString());
                        statusBar.setText(he.getURL().toString());
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    class MyToolbar extends JToolBar {

        public JButton[] button;

        public String[] imageName = { "hide.gif", "home.gif", "forward.gif", "back.gif", "print.gif", "option.gif", "save.gif", "webedit.gif", "web.gif", "code.gif", "Exit.gif" };

        public String[] tipText = { "New", "Home", "Forward ", "Backward", "Print", "Option", "Save File", "HTML Edit", "HTML Viewer", "View Code", "Exit" };

        public MyToolbar() {
            button = new JButton[11];
            for (int i = 0; i < imageName.length; i++) {
                add(button[i] = new JButton(new ImageIcon(ClassLoader.getSystemResource(imageName[i]))));
                button[i].setToolTipText(tipText[i]);
            }
        }
    }

    class IconCellRenderer extends JLabel implements TreeCellRenderer {

        protected Color m_textSelectionColor;

        protected Color m_textNonSelectionColor;

        protected Color m_bkSelectionColor;

        protected Color m_bkNonSelectionColor;

        protected Color m_borderSelectionColor;

        protected boolean m_selected;

        public IconCellRenderer() {
            super();
            m_textSelectionColor = UIManager.getColor("Tree.selectionForeground");
            m_textNonSelectionColor = UIManager.getColor("Tree.textForeground");
            m_bkSelectionColor = UIManager.getColor("Tree.selectionBackground");
            m_bkNonSelectionColor = UIManager.getColor("Tree.textBackground");
            m_borderSelectionColor = UIManager.getColor("Tree.selectionBorderColor");
            setOpaque(false);
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object obj = node.getUserObject();
            setText(obj.toString());
            if (obj instanceof Boolean) setText("Waiting ...");
            if (obj instanceof IconData) {
                IconData idata = (IconData) obj;
                if (expanded) setIcon(idata.getExpandedIcon()); else setIcon(idata.getIcon());
            } else setIcon(null);
            setFont(tree.getFont());
            setForeground(sel ? m_textSelectionColor : m_textNonSelectionColor);
            setBackground(sel ? m_bkSelectionColor : m_bkNonSelectionColor);
            m_selected = sel;
            return this;
        }

        public void paintComponent(Graphics g) {
            Color bColor = getBackground();
            Icon icon = getIcon();
            g.setColor(bColor);
            int offset = 0;
            if (icon != null && getText() != null) offset = (icon.getIconWidth() + getIconTextGap());
            g.fillRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);
            if (m_selected) {
                g.setColor(m_borderSelectionColor);
                g.drawRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);
            }
            super.paintComponent(g);
        }
    }

    class IconData {

        protected Icon m_icon;

        protected Icon m_expandedIcon;

        protected Object m_data;

        protected ImageIcon TEXT_ICON = new ImageIcon(ClassLoader.getSystemResource("kviewshell.png"));

        protected ImageIcon WEB_ICON = new ImageIcon(ClassLoader.getSystemResource("mozilla.png"));

        protected ImageIcon IMAGE_ICON = new ImageIcon(ClassLoader.getSystemResource("kpaint.png"));

        protected ImageIcon APLI_ICON = new ImageIcon(ClassLoader.getSystemResource("launcher.png"));

        public IconData(Icon icon, Object data) {
            m_icon = icon;
            m_expandedIcon = null;
            m_data = data;
        }

        public IconData(Icon icon, Icon expandedIcon, Object data) {
            m_icon = icon;
            m_expandedIcon = expandedIcon;
            m_data = data;
        }

        public Icon getIcon() {
            String name = (String) m_data;
            if (name.endsWith(".html") || name.endsWith(".htm")) return WEB_ICON; else if (name.endsWith(".txt") || name.endsWith(".htm")) return TEXT_ICON; else if (name.endsWith(".php") || name.endsWith(".jsp") || name.endsWith(".asp")) return APLI_ICON; else if (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".bmp") || name.endsWith(".tif")) return IMAGE_ICON; else return m_icon;
        }

        public Icon getExpandedIcon() {
            return m_expandedIcon != null ? m_expandedIcon : m_icon;
        }

        public Object getObject() {
            return m_data;
        }

        public String toString() {
            return m_data.toString();
        }
    }

    class PrintFile implements Printable {

        private RandomAccessFile raf;

        private String fileName;

        private Font fnt = new Font("Helvetica", Font.PLAIN, 10);

        private int rememberedPageIndex = -1;

        private long rememberedFilePointer = -1;

        private boolean rememberedEOF = false;

        public PrintFile(String file) {
            fileName = file;
            try {
                raf = new RandomAccessFile(file, "r");
            } catch (Exception e) {
                rememberedEOF = true;
            }
        }

        public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
            try {
                if (pageIndex != rememberedPageIndex) {
                    rememberedPageIndex = pageIndex;
                    if (rememberedEOF) return Printable.NO_SUCH_PAGE;
                    rememberedFilePointer = raf.getFilePointer();
                } else raf.seek(rememberedFilePointer);
                g.setColor(Color.black);
                g.setFont(fnt);
                int x = (int) pf.getImageableX() + 10;
                int y = (int) pf.getImageableY() + 12;
                g.drawString("File Name: " + fileName + ", Page Number: " + (pageIndex + 1), x, y);
                y += 36;
                while (y + 12 < pf.getImageableY() + pf.getImageableHeight()) {
                    String line = raf.readLine();
                    if (line == null) {
                        rememberedEOF = true;
                        break;
                    }
                    g.drawString(line, x, y);
                    y += 12;
                }
                return Printable.PAGE_EXISTS;
            } catch (Exception e) {
                return Printable.NO_SUCH_PAGE;
            }
        }
    }

    class ReadFontConfig {

        String s[] = new String[8];

        public ReadFontConfig(String sfile) {
            try {
                FileReader fr = new FileReader(sfile);
                BufferedReader br = new BufferedReader(fr);
                for (int i = 0; i < 7; i++) {
                    s[i] = br.readLine();
                }
                fr.close();
            } catch (IOException e) {
                System.out.println("Error while reading file, due to " + e.getMessage());
                System.exit(1);
            }
        }

        public String getFontGoen() {
            return s[0].substring(9);
        }

        public String getFontName() {
            return s[1].substring(9);
        }

        public int getFontSize() {
            return Integer.valueOf(s[2].substring(9));
        }

        public int getFontColor() {
            return Integer.valueOf(s[3].substring(10));
        }

        public int getFontBgColor() {
            return Integer.valueOf(s[4].substring(12));
        }

        public String getWorkingDir() {
            return s[5].substring(11);
        }

        public String getOpenLinkWith() {
            return s[6].substring(13);
        }
    }

    class RTFRenderer implements Printable {

        int currentPage = -1;

        JEditorPane jtextPane = new JEditorPane();

        double pageEndY = 0;

        double pageStartY = 0;

        boolean scaleWidthToFit = true;

        int currentIndex = 0;

        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
            double scale = 1.0;
            Graphics2D graphics2D;
            View rootView;
            graphics2D = (Graphics2D) graphics;
            jtextPane.setSize((int) pageFormat.getImageableWidth(), Integer.MAX_VALUE);
            jtextPane.validate();
            rootView = jtextPane.getUI().getRootView(jtextPane);
            if ((scaleWidthToFit) && (jtextPane.getMinimumSize().getWidth() > pageFormat.getImageableWidth())) {
                scale = pageFormat.getImageableWidth() / jtextPane.getMinimumSize().getWidth();
                graphics2D.scale(scale, scale);
            }
            graphics2D.setClip((int) (pageFormat.getImageableX() / scale), (int) (pageFormat.getImageableY() / scale), (int) (pageFormat.getImageableWidth() / scale), (int) (pageFormat.getImageableHeight() / scale));
            if (pageIndex > currentPage) {
                currentPage = pageIndex;
                pageStartY += pageEndY;
                pageEndY = graphics2D.getClipBounds().getHeight();
            }
            graphics2D.translate(graphics2D.getClipBounds().getX(), graphics2D.getClipBounds().getY());
            Rectangle allocation = new Rectangle(0, (int) -pageStartY, (int) (jtextPane.getMinimumSize().getWidth()), (int) (jtextPane.getPreferredSize().getHeight()));
            if (printView(graphics2D, allocation, rootView)) {
                return PAGE_EXISTS;
            } else {
                pageStartY = 0;
                pageEndY = 0;
                currentPage = -1;
                currentIndex = 0;
                return NO_SUCH_PAGE;
            }
        }

        protected boolean printView(Graphics2D graphics2D, Shape allocation, View view) {
            boolean pageExists = false;
            Rectangle clipRectangle = graphics2D.getClipBounds();
            Shape childAllocation;
            View childView;
            if (view.getViewCount() > 0) {
                for (int i = 0; i < view.getViewCount(); i++) {
                    childAllocation = view.getChildAllocation(i, allocation);
                    if (childAllocation != null) {
                        childView = view.getView(i);
                        if (printView(graphics2D, childAllocation, childView)) {
                            pageExists = true;
                        }
                    }
                }
            } else {
                if (allocation.getBounds().getMaxY() >= clipRectangle.getY()) {
                    pageExists = true;
                    if ((allocation.getBounds().getHeight() > clipRectangle.getHeight()) && (allocation.intersects(clipRectangle))) {
                        view.paint(graphics2D, allocation);
                    } else {
                        if (allocation.getBounds().getY() >= clipRectangle.getY()) {
                            if (allocation.getBounds().getMaxY() <= clipRectangle.getMaxY() - 15) {
                                view.paint(graphics2D, allocation);
                            } else {
                                if (allocation.getBounds().getY() < pageEndY) {
                                    pageEndY = allocation.getBounds().getY();
                                }
                            }
                        }
                    }
                }
            }
            return pageExists;
        }

        public void setpane(JEditorPane TextPane1) {
            jtextPane.setContentType("text/html");
            jtextPane = TextPane1;
        }
    }
}

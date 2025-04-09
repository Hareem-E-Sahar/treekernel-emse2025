package com.robrohan.treebeard;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.regexp.RE;
import com.robrohan.editorkit.Find;
import com.robrohan.editorkit.XMLEditorPane;
import com.robrohan.editorkit.XMLEditorPaneDnD;
import com.robrohan.fangorn.DocumentChooser;
import com.robrohan.fangorn.Enting;
import com.robrohan.fangorn.LoadFile;
import com.robrohan.fangorn.Settings;
import com.robrohan.fangorn.XMLSetup;
import com.robrohan.fangorn.transform.TreeTransform;
import com.robrohan.fangorn.treeview.TreeViewBuilder;
import com.robrohan.tools.AshpoolDB;
import com.robrohan.tools.FrameTools;
import com.robrohan.tools.Globals;
import com.robrohan.tools.ImageManager;

/**
 * This is a transforming window
 * @author  rob
 */
public class Ent extends Enting {

    private String redirectFileName = "";

    /** what the ent does with the transformed document */
    private byte entOutput = Globals.PLAIN_HTML;

    /** find dialog */
    private Find f;

    /** xml setup dialog */
    private XMLSetup xs;

    /** output setup dialog */
    private OutputSetup os;

    /** the template chooser */
    private DocumentChooser dc;

    /** the transforming engine */
    private TreeTransform tt;

    /** the xml area */
    private XMLEditorPane txtXML;

    /** the xslt area */
    private XMLEditorPane txtXSLT;

    private XMLEditorPane txtOutput;

    private TreeViewBuilder treeview;

    private JScrollPane TreeScrollPane;

    private Settings settings;

    /** used in treeview static */
    public JScrollPane sp = new JScrollPane();

    private String rFile1 = "", rFile2 = "";

    /** Creates a new Ent
	 * @param title the title of this ent (in the ents titlebar)
	 * @param resizable is this ent resizable?
	 * @param closeable is this ent closeable?
	 * @param maximizable is this ent maximizable?
	 * @param iconifiable is this ent iconifiable?
	 */
    public Ent(String title) {
        super(title);
        initComponents();
        ImageManager.loadResourceImage(getClass(), "tree.20.merge", "/com/robrohan/treebeard/images/merge_sm.gif");
        ImageManager.loadResourceImage(getClass(), "tree.20.tidy", "/com/robrohan/treebeard/images/tidy_sm.gif");
        ImageManager.loadResourceImage(getClass(), "tree.20.expandall", "/com/robrohan/treebeard/images/expandall.gif");
        ImageManager.loadResourceImage(getClass(), "tree.20.collapseall", "/com/robrohan/treebeard/images/collapseall.gif");
        type = Enting.TYPE_WINDOW;
        this.setTitle(title);
    }

    public void restoreState(String title, int height, int width, int x, int y, float div1, float div2, String file1, String file2) {
        restoreState(title, height, width, x, y);
        rFile1 = file1;
        rFile2 = file2;
    }

    public void restoreStatePost() {
        try {
            if (rFile1.length() > 0) {
                txtXML.loadFile(new File(rFile1));
            }
            if (rFile2.length() > 0) {
                txtXSLT.loadFile(new File(rFile2));
            }
        } catch (Exception e) {
            System.err.println("Setting Window Items Error: " + e.toString());
            e.printStackTrace(System.err);
        }
    }

    /**
	 * make this ent save itsself to the database
	 */
    public void saveState(int projectid) {
        String file1 = "";
        String file2 = "";
        try {
            file1 = txtXML.getCurrentFile().getAbsolutePath();
            file2 = txtXSLT.getCurrentFile().getAbsolutePath();
        } catch (Exception e) {
            ;
        }
        try {
            String qry = "insert into desktop( projectid, type, name, h, w, x, y, div1, div2, iconify, file1, file2" + " )values( " + " " + projectid + ", " + " " + type + ", " + " '" + this.getTitle() + "', " + " " + getHeight() + ", " + " " + getWidth() + ", " + " " + getX() + ", " + " " + getY() + ", " + " " + jSplitPane1.getLastDividerLocation() + ", " + " " + jSplitPane2.getLastDividerLocation() + ", " + " " + false + ", " + " '" + file1 + "', " + " '" + file2 + "' " + ");";
            AshpoolDB.executeQuery(qry);
        } catch (Exception e) {
            System.err.println("I can't save myself! " + e.toString());
            e.printStackTrace(System.err);
        }
    }

    /**
	 * setup the xml kits
	 */
    public void buildXMLTools() {
        settings = new Settings();
        try {
            ResultSet psettings = AshpoolDB.executeQuery("select saxstring, domstring, xsltstring from globals;");
            psettings.next();
            settings.DOMFactory = psettings.getString("domstring");
            settings.SAXFactory = psettings.getString("saxstring");
            settings.XSLTFactory = psettings.getString("xsltstring");
        } catch (Exception e) {
            System.err.println("Could not load parser settings " + e.toString());
        }
        treeview = new TreeViewBuilder();
        try {
            txtXML = new XMLEditorPaneDnD(this, "text/xml");
            txtOutput = new XMLEditorPaneDnD(this, "text/svg");
            txtXSLT = new XMLEditorPaneDnD(this, "text/xsl");
        } catch (Exception e) {
            e.printStackTrace(System.err);
            txtXML = new XMLEditorPane(this, "text/xml");
            txtOutput = new XMLEditorPane(this, "text/svg");
            txtXSLT = new XMLEditorPane(this, "text/xsl");
        }
        txtXSLT.getInputHandler().addKeyBinding("F5", getTransformAction());
        txtXSLT.getInputHandler().addKeyBinding("F9", getTransformAction());
        txtXML.getInputHandler().addKeyBinding("F5", getTransformAction());
        txtXML.getInputHandler().addKeyBinding("F9", getTransformAction());
        jSplitPane2.setLeftComponent(txtXML);
        jSplitPane2.setRightComponent(txtXSLT);
        this.panoutput.add(txtOutput);
        jSplitPane1.setLeftComponent(jSplitPane2);
        jSplitPane1.setRightComponent(jTabbedPane1);
        JMenu mnuDocSub = new JMenu(Globals.getMenuLabel("xslt", Globals.WINDOW_BUNDLE));
        JMenuItem jmi = new JMenuItem(new Ent.makeNewXSLT());
        jmi.setIcon(null);
        mnuDocSub.add(jmi);
        jmi = new JMenuItem(this.txtXSLT.getLoadAction());
        jmi.setText(Globals.getMenuLabel("load", Globals.WINDOW_BUNDLE));
        jmi.setIcon(null);
        mnuDocSub.add(jmi);
        jmi = new JMenuItem(this.txtXSLT.getSaveAction());
        jmi.setText(Globals.getMenuLabel("save", Globals.WINDOW_BUNDLE));
        jmi.setIcon(null);
        mnuDocSub.add(jmi);
        jmi = new JMenuItem(this.txtXSLT.getSaveAsAction());
        jmi.setText(Globals.getMenuLabel("saveas", Globals.WINDOW_BUNDLE));
        jmi.setIcon(null);
        mnuDocSub.add(jmi);
        mnuFile.add(mnuDocSub);
        mnuDocSub = new JMenu(Globals.getMenuLabel("xml", Globals.WINDOW_BUNDLE));
        jmi = new JMenuItem(new Ent.makeNewXML());
        jmi.setIcon(null);
        mnuDocSub.add(jmi);
        jmi = new JMenuItem(this.txtXML.getLoadAction());
        jmi.setText(Globals.getMenuLabel("load", Globals.WINDOW_BUNDLE));
        jmi.setIcon(null);
        mnuDocSub.add(jmi);
        jmi = new JMenuItem(this.txtXML.getSaveAction());
        jmi.setText(Globals.getMenuLabel("save", Globals.WINDOW_BUNDLE));
        jmi.setIcon(null);
        mnuDocSub.add(jmi);
        jmi = new JMenuItem(this.txtXML.getSaveAsAction());
        jmi.setText(Globals.getMenuLabel("saveas", Globals.WINDOW_BUNDLE));
        jmi.setIcon(null);
        mnuDocSub.add(jmi);
        mnuFile.add(mnuDocSub);
        this.mnuFile.add(new JSeparator());
        this.mnuFile.add(new Ent.DoTransform()).setIcon(null);
        jmi = new JMenuItem(this.txtOutput.getSaveAsAction());
        jmi.setText(Globals.getMenuLabel("saveoutput", Globals.WINDOW_BUNDLE));
        jmi.setIcon(null);
        this.mnuFile.add(jmi);
        this.mnuFile.add(new JSeparator());
        this.mnuFile.add(new AbstractAction(Globals.getMenuLabel("close", Globals.WINDOW_BUNDLE)) {

            public void actionPerformed(ActionEvent evt) {
                closeWindow();
            }
        });
        AbstractAction tmp = new Ent.DoFind();
        this.mnuEdit.add(tmp).setIcon(null);
        txtXSLT.getInputHandler().addKeyBinding("C+.", tmp);
        txtXML.getInputHandler().addKeyBinding("C+.", tmp);
        this.mnuEdit.add(new Ent.DoReplace()).setIcon(null);
        this.mnuEdit.add(new JSeparator());
        this.mnuEdit.add(txtXSLT.getUndoAction()).setIcon(null);
        this.mnuEdit.add(txtXSLT.getRedoAction()).setIcon(null);
        mnuEntTools.add(new nameEnt());
        mnuEntTools.add(new setOutputParams());
        mnuEntTools.add(new ShowParserSettings());
        this.mnuTools.add(new Ent.parserSetup());
        this.mnuTools.add(new Ent.SaveSettings());
        jToolXSLT.setFloatable(false);
        jToolXSLT.add(txtXSLT.getSaveAction()).setText("");
        jToolXSLT.add(txtXSLT.getLoadAction()).setText("");
        jToolXSLT.add(txtXSLT.getUndoAction()).setText("");
        jToolXSLT.add(txtXSLT.getRedoAction()).setText("");
        jToolMerge.add(new Ent.DoTransform()).setText("");
        jToolMerge.setFloatable(false);
        this.JToolXML.add(new tidyXML()).setText("");
        JToolXML.setFloatable(false);
        f = new Find(this, false);
        f.setSize(325, 265);
        f.xmlPane = this.txtXML;
        f.xsltPane = this.txtXSLT;
        f.outputPane = this.txtOutput;
        FrameTools.center(f);
        xs = new XMLSetup(this, true);
        xs.setSettings(settings);
        xs.setSize(415, 175);
        FrameTools.center(xs);
        os = new OutputSetup(this, true);
        FrameTools.center(os);
        dc = new DocumentChooser();
        dc.setSize(317, 260);
        FrameTools.center(dc);
        txtXML.setFileChooser(fileChooser);
        txtXML.setFileLoader(fileLoader);
        txtXSLT.setFileChooser(fileChooser);
        txtXSLT.setFileLoader(fileLoader);
        txtOutput.setFileChooser(fileChooser);
        tt = new TreeTransform();
        this.requestFocus();
    }

    /**
	 * the tidy button attempts to format an xml document
	 * by applying a simple style sheet 
	 */
    class tidyXML extends AbstractAction {

        public tidyXML() {
            super("Tidy XML", ImageManager.getImage("tree.20.tidy"));
            putValue(Action.SHORT_DESCRIPTION, Globals.getTooltipLabel("tidy", Globals.WINDOW_BUNDLE));
        }

        public void actionPerformed(ActionEvent e) {
            Thread wk = new Thread() {

                public void run() {
                    Globals.setThinking(true);
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        ;
                    }
                    doTidy();
                    Globals.setThinking(false);
                }
            };
            wk.start();
        }
    }

    /**
	 * Sets the name of an ent. An ents default name is "W-x" where
	 * x is a number
	 */
    class nameEnt extends AbstractAction {

        public nameEnt() {
            super(Globals.getMenuLabel("namewin", Globals.WINDOW_BUNDLE));
        }

        public void actionPerformed(ActionEvent e) {
            setTitle(JOptionPane.showInputDialog(null, Globals.getDialogLabel("namewin", Globals.WINDOW_BUNDLE)));
        }
    }

    /**
	 * shows the dialog to change where the transform results go
	 */
    class setOutputParams extends AbstractAction {

        public setOutputParams() {
            super(Globals.getMenuLabel("sendres", Globals.WINDOW_BUNDLE));
        }

        public void actionPerformed(ActionEvent e) {
            sendResults();
        }
    }

    /**
	 * set to currently selected result redirection
	 */
    public void sendResults() {
        os.setSelectedOption(entOutput);
        os.setFileName(redirectFileName);
        os.show();
    }

    /** 
	 * shows the XSLT template chooser dialog
	 */
    class makeNewXSLT extends AbstractAction {

        public makeNewXSLT() {
            super(Globals.getMenuLabel("new", Globals.WINDOW_BUNDLE));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            if (txtXSLT.getText().toString().length() > 0 && JOptionPane.showConfirmDialog(null, Globals.getDialogLabel("createnewxsl", Globals.WINDOW_BUNDLE)) == JOptionPane.YES_OPTION) {
                createNewXSLT();
            } else if (txtXSLT.getText().toString().length() == 0) {
                createNewXSLT();
            }
        }
    }

    /**
	 * creates a new blank xml document
	 */
    class makeNewXML extends AbstractAction {

        public makeNewXML() {
            super(Globals.getMenuLabel("new", Globals.WINDOW_BUNDLE));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            if (txtXML.getText().toString().length() > 0 && JOptionPane.showConfirmDialog(null, Globals.getDialogLabel("createnewxml", Globals.WINDOW_BUNDLE)) == JOptionPane.YES_OPTION) {
                createNewXML();
            } else if (txtXML.getText().toString().length() == 0) {
                createNewXML();
            }
        }
    }

    /**
	 * shows the set parser dialog (to change xml parser and xslt transformer)
	 */
    class parserSetup extends AbstractAction {

        public parserSetup() {
            super(Globals.getMenuLabel("setup", Globals.WINDOW_BUNDLE));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            parserSettings();
        }
    }

    /** 
	 * does the transformation
	 */
    class DoTransform extends AbstractAction {

        public DoTransform() {
            super(Globals.getMenuLabel("merge", Globals.WINDOW_BUNDLE), ImageManager.getImage("tree.20.merge"));
            putValue(javax.swing.Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke("F5"));
            putValue(Action.SHORT_DESCRIPTION, Globals.getTooltipLabel("merge", Globals.WINDOW_BUNDLE));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            Thread trans = new Thread() {

                public void run() {
                    Globals.setThinking(true);
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        System.err.println("DoTransform: " + e);
                        e.printStackTrace(System.err);
                    }
                    txtOutput.setText("");
                    txtOutputHTML.setText("");
                    transform();
                    Globals.setThinking(false);
                }
            };
            trans.start();
        }
    }

    /**
	 * displays what transformer is currently active
	 */
    class ShowParserSettings extends AbstractAction {

        public ShowParserSettings() {
            super(Globals.getMenuLabel("checktrans", Globals.WINDOW_BUNDLE));
        }

        public void actionPerformed(ActionEvent e) {
            Enting rdwin = Globals.getWindow("ReadMe");
            ((com.robrohan.tools.ReadMe) rdwin).showString(applyLibStyleSheet("transInfo.xsl", "<root/>"));
            rdwin.show();
        }
    }

    /**
	 * saves the selected xml parser and transformer settings to the database
	 * so every new ent will have these settings
	 */
    class SaveSettings extends AbstractAction {

        public SaveSettings() {
            super(Globals.getMenuLabel("default", Globals.WINDOW_BUNDLE));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            try {
                com.robrohan.tools.AshpoolDB.executeQuery("update globals set saxstring = '" + settings.SAXFactory + "', domstring = '" + settings.DOMFactory + "', xsltstring = '" + settings.XSLTFactory + "';");
                JOptionPane.showMessageDialog(null, Globals.getDialogLabel("setdefault", Globals.WINDOW_BUNDLE));
            } catch (Exception e) {
                System.err.println(e);
                e.printStackTrace(System.err);
            }
        }
    }

    /**
	 * show the find dialog
	 */
    class DoFind extends AbstractAction {

        public DoFind() {
            super(Globals.getMenuLabel("find", Globals.WINDOW_BUNDLE));
            putValue(Action.SHORT_DESCRIPTION, "Open the find window");
        }

        public void actionPerformed(ActionEvent actionEvent) {
            f.setToFind();
            f.show();
        }
    }

    /**
	 * show the find dialog
	 */
    class DoReplace extends AbstractAction {

        public DoReplace() {
            super(Globals.getMenuLabel("replace", Globals.WINDOW_BUNDLE));
            putValue(Action.SHORT_DESCRIPTION, "Open the find window");
        }

        public void actionPerformed(ActionEvent actionEvent) {
            f.setToReplace();
            f.show();
        }
    }

    /**
	 * gets the transform action
	 * @return
	 */
    public AbstractAction getTransformAction() {
        return new Ent.DoTransform();
    }

    /** Sets the xml pane's text to the passed string
	 * @param newText the text to set the xml pane to (should be xml)
	 */
    public void setXMLText(String newText) {
        this.txtXML.setText(newText);
    }

    /** Sets the xslt pane's text to the passed string
	 * @param newText the text to set the xslt pane to (should be an xslt)
	 */
    public void setXSLTText(String newText) {
        this.txtXML.setText(newText);
    }

    public void setFileLoader(LoadFile lf) {
        fileLoader = lf;
    }

    public void setFileChooser(com.robrohan.fangorn.EntFileChooser efc) {
        fileChooser = efc;
    }

    /** This method is called from within the constructor to
	 * initialize the form.
	 */
    private void initComponents() {
        panCenter = new JPanel();
        jSplitPane1 = new JSplitPane();
        panDocuments = new JPanel();
        jSplitPane2 = new JSplitPane();
        jTabbedPane1 = new JTabbedPane();
        panoutput = new JPanel();
        jScrollPane1 = new JScrollPane();
        txtOutputHTML = new JEditorPane();
        panTop = new JPanel();
        JToolXML = new JToolBar();
        cmdTreeView = new JButton();
        cmdTextView = new JButton();
        cmdCopyPath = new JButton();
        cmdCopyLeaf = new JButton();
        jToolMerge = new JToolBar();
        jToolXSLT = new JToolBar();
        panBottom = new JPanel();
        panRight = new JPanel();
        panLeft = new JPanel();
        jMenuBar = new JMenuBar();
        mnuFile = new JMenu();
        mnuEdit = new JMenu();
        mnuEntTools = new JMenu();
        mnuTools = new JMenu();
        panCenter.setLayout(new BorderLayout());
        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setOneTouchExpandable(true);
        jSplitPane1.addComponentListener(new ComponentAdapter() {

            public void componentResized(ComponentEvent evt) {
                jSplitPane1ComponentResized(evt);
            }
        });
        panDocuments.setLayout(new BorderLayout());
        jSplitPane2.setDividerLocation(323);
        jSplitPane2.setResizeWeight(1.0);
        jSplitPane2.setOneTouchExpandable(true);
        panDocuments.add(jSplitPane2, BorderLayout.CENTER);
        jSplitPane1.setLeftComponent(panDocuments);
        jTabbedPane1.setTabPlacement(JTabbedPane.BOTTOM);
        jTabbedPane1.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });
        panoutput.setLayout(new BorderLayout());
        panoutput.setBorder(new TitledBorder("Output"));
        jTabbedPane1.addTab(Globals.getLabelLabel("plain", Globals.WINDOW_BUNDLE), panoutput);
        txtOutputHTML.setEditable(false);
        txtOutputHTML.setContentType("text/html");
        txtOutputHTML.addComponentListener(new ComponentAdapter() {

            public void componentShown(ComponentEvent evt) {
                txtOutputHTMLComponentShown(evt);
            }
        });
        jScrollPane1.setViewportView(txtOutputHTML);
        jTabbedPane1.addTab(Globals.getLabelLabel("html", Globals.WINDOW_BUNDLE), jScrollPane1);
        jSplitPane1.setRightComponent(jTabbedPane1);
        panCenter.add(jSplitPane1, java.awt.BorderLayout.CENTER);
        getContentPane().add(panCenter, java.awt.BorderLayout.CENTER);
        panTop.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        ImageManager.loadResourceImage(getClass(), "tree.20.tree", "/com/robrohan/treebeard/images/tree_sm.gif");
        cmdTreeView.setIcon(ImageManager.getImage("tree.20.tree"));
        cmdTreeView.setToolTipText(Globals.getTooltipLabel("treeview", Globals.WINDOW_BUNDLE));
        cmdTreeView.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdTreeViewActionPerformed(evt);
            }
        });
        JToolXML.add(cmdTreeView);
        cmdTextView.setIcon(ImageManager.getImage("fan.20.mime.xml"));
        cmdTextView.setToolTipText(Globals.getTooltipLabel("plainview", Globals.WINDOW_BUNDLE));
        cmdTextView.setEnabled(false);
        cmdTextView.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdTextViewActionPerformed(evt);
            }
        });
        JToolXML.add(cmdTextView);
        ImageManager.loadResourceImage(getClass(), "tree.20.addpath", "/com/robrohan/treebeard/images/addpath_sm.gif");
        cmdCopyPath.setIcon(ImageManager.getImage("tree.20.addpath"));
        cmdCopyPath.setToolTipText(Globals.getTooltipLabel("addpath", Globals.WINDOW_BUNDLE));
        cmdCopyPath.setEnabled(false);
        cmdCopyPath.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdCopyPathActionPerformed(evt);
            }
        });
        JToolXML.add(cmdCopyPath);
        ImageManager.loadResourceImage(getClass(), "tree.20.addnode", "/com/robrohan/treebeard/images/addnode_sm.gif");
        cmdCopyLeaf.setIcon(ImageManager.getImage("tree.20.addnode"));
        cmdCopyLeaf.setToolTipText(Globals.getTooltipLabel("addnode", Globals.WINDOW_BUNDLE));
        cmdCopyLeaf.setEnabled(false);
        cmdCopyLeaf.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdCopyLeafActionPerformed(evt);
            }
        });
        JToolXML.add(cmdCopyLeaf);
        panTop.add(JToolXML);
        panTop.add(jToolMerge);
        panTop.add(jToolXSLT);
        getContentPane().add(panTop, java.awt.BorderLayout.NORTH);
        getContentPane().add(panBottom, java.awt.BorderLayout.SOUTH);
        getContentPane().add(panRight, java.awt.BorderLayout.EAST);
        getContentPane().add(panLeft, java.awt.BorderLayout.WEST);
        mnuFile.setText(Globals.getMenuLabel("file", Globals.WINDOW_BUNDLE));
        jMenuBar.add(mnuFile);
        mnuEdit.setText(Globals.getMenuLabel("edit", Globals.WINDOW_BUNDLE));
        jMenuBar.add(mnuEdit);
        mnuEntTools.setText(Globals.getMenuLabel("tools", Globals.WINDOW_BUNDLE));
        jMenuBar.add(mnuEntTools);
        mnuTools.setText(Globals.getMenuLabel("parser", Globals.WINDOW_BUNDLE));
        jMenuBar.add(mnuTools);
        setJMenuBar(jMenuBar);
        pack();
    }

    public void setHTMLPaneWithPlainPaneData() {
        if (txtOutputHTML != null && txtOutput != null) {
            if (txtOutput.getText().length() > 0) {
                txtOutputHTML.setText(txtOutput.getText());
            }
        }
    }

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {
        setHTMLPaneWithPlainPaneData();
    }

    private void txtOutputHTMLComponentShown(java.awt.event.ComponentEvent evt) {
    }

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
        ;
    }

    private void jSplitPane1ComponentResized(java.awt.event.ComponentEvent evt) {
        if (txtXSLT != null) {
            txtXSLT.recalculateVisibleLines();
            txtXSLT.repaint();
        }
    }

    private void cmdCopyLeafActionPerformed(java.awt.event.ActionEvent evt) {
        copyNode();
    }

    private void cmdCopyPathActionPerformed(java.awt.event.ActionEvent evt) {
        copyPath();
    }

    private void cmdTextViewActionPerformed(java.awt.event.ActionEvent evt) {
        setToTextView();
        setTreeView(false);
    }

    private void cmdTreeViewActionPerformed(java.awt.event.ActionEvent evt) {
        if (setToTreeView()) {
            setTreeView(true);
        }
    }

    private void setTreeView(boolean on) {
        if (on) {
            this.cmdTreeView.setEnabled(false);
        } else {
            this.cmdTreeView.setEnabled(true);
        }
        this.cmdTextView.setEnabled(on);
        this.cmdCopyPath.setEnabled(on);
        this.cmdCopyLeaf.setEnabled(on);
        if (jSplitPane2.getDividerLocation() <= 0) {
            jSplitPane2.setDividerLocation(100);
        } else {
            jSplitPane2.setDividerLocation(this.jSplitPane2.getDividerLocation());
        }
    }

    /** action to expand all the tree view */
    class treeExpandAll extends AbstractAction {

        public treeExpandAll() {
            super("", ImageManager.getImage("tree.20.expandall"));
            this.putValue(Action.SHORT_DESCRIPTION, Globals.getTooltipLabel("expandall", Globals.WINDOW_BUNDLE));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            treeview.expandAll();
        }
    }

    /** action to collapse all the tree view */
    class treeCollapseAll extends AbstractAction {

        public treeCollapseAll() {
            super("", ImageManager.getImage("tree.20.collapseall"));
            this.putValue(Action.SHORT_DESCRIPTION, Globals.getTooltipLabel("collapseall", Globals.WINDOW_BUNDLE));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            treeview.collapseAll();
        }
    }

    /** Sets the xml pane to the treeview instead of the plain text view
	 * @return returns false on error
	 */
    public boolean setToTreeView() {
        javax.swing.JPanel p = new javax.swing.JPanel();
        p.setLayout(new java.awt.BorderLayout());
        javax.swing.JToolBar jtb = new javax.swing.JToolBar();
        jtb.setOrientation(javax.swing.JToolBar.VERTICAL);
        jtb.setFloatable(false);
        jtb.add(new treeExpandAll());
        jtb.add(new treeCollapseAll());
        p.add(jtb, java.awt.BorderLayout.WEST);
        try {
            if (txtXML.isReferenced()) {
                sp.setViewportView(treeview.getTreePane(new FileInputStream(txtXML.getCurrentFile())));
            } else {
                sp.setViewportView(treeview.getTreePane(new ByteArrayInputStream(txtXML.getText().toString().getBytes())));
            }
        } catch (Exception e) {
            System.err.println("Can't go into tree view because: " + e);
            return false;
        }
        p.add(sp, java.awt.BorderLayout.CENTER);
        jSplitPane2.setLeftComponent(p);
        return true;
    }

    /** Sets the xml pane to the plain text instead of the treeview view */
    public void setToTextView() {
        jSplitPane2.setLeftComponent(txtXML);
    }

    /** Moves the selected tree path in the xml document to the xslt document.
	 * (when one clicks the "branch" button, used to build xpaths)
	 */
    public void copyPath() {
        if (treeview != null) {
            try {
                txtXSLT.getDocument().insertString(this.txtXSLT.getCaretPosition(), treeview.getCurrentPath(), null);
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    /** Moves the selected tree leaf in the xml document to the xslt document.
	 * (when one clicks the "leaf" button, used to build xpaths)
	 */
    public void copyNode() {
        if (treeview != null) {
            try {
                txtXSLT.getDocument().insertString(txtXSLT.getCaretPosition(), treeview.getCurrentNode(), null);
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    private void doTidy() {
        setXMLText(applyLibStyleSheet("tidy.xsl", txtXML.getText().toString()));
    }

    /**
	 * 
	 * @param xslFileName
	 * @param XML
	 * @return
	 */
    private String applyLibStyleSheet(String xslFileName, String XML) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            File cmdpath = new File(Globals.getProgramPath() + "lib" + System.getProperty("file.separator") + xslFileName);
            long nada = tt.transform(new ByteArrayInputStream(XML.getBytes()), new FileInputStream(cmdpath), baos, cmdpath, null);
            return baos.toString();
        } catch (Exception e) {
            System.err.println("Ent->applyLibStyleSheet: " + e.toString());
            e.printStackTrace(System.err);
        }
        return "";
    }

    private static java.io.ByteArrayOutputStream baos = null;

    private static java.io.ByteArrayInputStream bais = null;

    private void transform() {
        if (xs.getSettings() != null) {
            settings = xs.getSettings();
        }
        entOutput = os.getSelectedOption();
        redirectFileName = os.getFileName();
        if (entOutput != Globals.PLAIN_HTML) {
            jSplitPane1.setDividerLocation(1.0);
        } else if (jSplitPane1.getDividerLocation() > jSplitPane1.getMaximumDividerLocation()) {
            jSplitPane1.setDividerLocation(.6);
        }
        try {
            if (baos == null) baos = new java.io.ByteArrayOutputStream();
            long time = 0;
            switch(entOutput) {
                case Globals.RUN_EXTERNAL:
                case Globals.PLAIN_SAVE:
                case Globals.PLAIN_HTML:
                case Globals.TO_WINDOW:
                    if (txtXML.isReferenced()) {
                        java.io.FileInputStream fXML = new java.io.FileInputStream(txtXML.getCurrentFile());
                        java.io.ByteArrayInputStream bXSL = new java.io.ByteArrayInputStream(this.txtXSLT.getText().toString().getBytes());
                        time = tt.transform(fXML, bXSL, baos, txtXSLT.getCurrentFile(), txtXML.getCurrentFile());
                        fXML.close();
                        bXSL.close();
                        fXML = null;
                        bXSL = null;
                    } else {
                        java.io.ByteArrayInputStream bXML = new java.io.ByteArrayInputStream(this.txtXML.getText().toString().getBytes());
                        java.io.ByteArrayInputStream bXSL = new java.io.ByteArrayInputStream(this.txtXSLT.getText().toString().getBytes());
                        time = tt.transform(bXML, bXSL, baos, txtXSLT.getCurrentFile(), txtXML.getCurrentFile());
                        bXML.close();
                        bXSL.close();
                        bXML = bXSL = null;
                    }
                    break;
                case Globals.FOP_PREVIEW:
                case Globals.PDF_SAVE:
                    if (txtXML.isReferenced()) {
                        java.io.FileInputStream fXML = new java.io.FileInputStream(txtXML.getCurrentFile());
                        java.io.ByteArrayInputStream bXSL = new java.io.ByteArrayInputStream(this.txtXSLT.getText().toString().getBytes());
                        time = tt.FOPtransform(fXML, bXSL, baos, entOutput, txtXSLT.getCurrentFile(), txtXML.getCurrentFile());
                        fXML.close();
                        bXSL.close();
                        fXML = null;
                        bXSL = null;
                    } else {
                        java.io.ByteArrayInputStream bXML = new java.io.ByteArrayInputStream(this.txtXML.getText().toString().getBytes());
                        java.io.ByteArrayInputStream bXSL = new java.io.ByteArrayInputStream(this.txtXSLT.getText().toString().getBytes());
                        time = tt.FOPtransform(bXML, bXSL, baos, entOutput, txtXSLT.getCurrentFile(), txtXML.getCurrentFile());
                        bXML.close();
                        bXSL.close();
                        bXML = bXSL = null;
                    }
                    break;
            }
            java.io.FileOutputStream newFileStream = null;
            switch(entOutput) {
                case Globals.PLAIN_HTML:
                    bais = new ByteArrayInputStream(baos.toByteArray());
                    txtOutput.setText(bais);
                    setHTMLPaneWithPlainPaneData();
                    bais = null;
                    break;
                case Globals.PLAIN_SAVE:
                case Globals.PDF_SAVE:
                    newFileStream = new FileOutputStream(this.redirectFileName);
                    newFileStream.write(baos.toByteArray());
                    newFileStream.flush();
                    newFileStream.close();
                    newFileStream = null;
                    break;
                case Globals.TO_WINDOW:
                    Ent nEnt = new Ent(this.getTitle() + "- Results");
                    nEnt.buildXMLTools();
                    nEnt.getXMLPane().setText(new ByteArrayInputStream(baos.toByteArray()));
                    nEnt.show();
                    break;
                case Globals.RUN_EXTERNAL:
                    File tempOutFile = new File("tempOUTPUT");
                    newFileStream = new FileOutputStream(tempOutFile);
                    newFileStream.write(baos.toByteArray());
                    newFileStream.flush();
                    newFileStream.close();
                    String tempCommand = "";
                    if (this.redirectFileName.indexOf("$1") > 0) {
                        RE r = new RE("\\$1");
                        tempCommand = r.subst(redirectFileName, tempOutFile.getAbsolutePath());
                    }
                    Process p = Runtime.getRuntime().exec(tempCommand);
                    break;
                case Globals.FOP_PREVIEW:
                    baos.reset();
                    break;
            }
            ((javax.swing.border.TitledBorder) ((javax.swing.JPanel) txtOutput.getParent()).getBorder()).setTitle(Globals.getLabelLabel("took", Globals.WINDOW_BUNDLE) + " " + time + " ms " + "(" + settings.XSLTFactory + ")");
            ((javax.swing.JPanel) txtOutput.getParent()).repaint();
            baos.flush();
            baos.close();
            baos = null;
        } catch (Exception e) {
            System.err.println("Window::transform: " + e);
            e.printStackTrace(System.err);
        }
    }

    private void createNewXML() {
        try {
            ResultSet newxml = AshpoolDB.executeQuery("select template_start, template_end from templates where type = 1");
            newxml.next();
            String start = newxml.getString("template_start").replaceAll("&#9;", "\t");
            String end = newxml.getString("template_end").replaceAll("&#9;", "\t");
            txtXML.createNewDocument("text/xml", start.replaceAll("&#10;", "\n") + end.replaceAll("&#10;", "\n"));
        } catch (Exception e) {
            System.err.println("Error creating new xml " + e.toString());
        }
    }

    private void createNewXSLT() {
        dc.setPane(txtXSLT);
        dc.createDocList();
        dc.show();
    }

    public com.robrohan.editorkit.XMLEditorPane getXMLPane() {
        return this.txtXML;
    }

    public com.robrohan.editorkit.XMLEditorPane getXSLTPane() {
        return this.txtXSLT;
    }

    /** if this is an ent (used by the manager)
	 * @return if this is an ent will return true (should always return true in this class)
	 */
    public boolean isEnt() {
        return true;
    }

    public void destroy() {
        f.setVisible(false);
        os.setVisible(false);
        xs.setVisible(false);
        dc.setVisible(false);
        setVisible(false);
        txtXSLT.killAnalyzer();
        txtXML.killAnalyzer();
        txtOutput.killAnalyzer();
        f.dispose();
        os.dispose();
        xs.dispose();
        dc.dispose();
    }

    /** envokes the xml / xslt parser settings dialog for this ent */
    public void parserSettings() {
        xs.show();
    }

    private JScrollPane jScrollPane1;

    private JMenu mnuTools;

    private JPanel panLeft;

    private JEditorPane txtOutputHTML;

    private JPanel panoutput;

    private JSplitPane jSplitPane2;

    private JToolBar JToolXML;

    private JSplitPane jSplitPane1;

    private JPanel panRight;

    private JPanel panTop;

    private JButton cmdTreeView;

    private com.robrohan.fangorn.EntFileChooser fileChooser;

    private LoadFile fileLoader;

    private JPanel panBottom;

    private JButton cmdCopyPath;

    private JPanel panDocuments;

    private JButton cmdTextView;

    private JMenu mnuEdit;

    private JMenu mnuEntTools;

    private JPanel panCenter;

    private JMenuBar jMenuBar;

    private JToolBar jToolXSLT;

    private JToolBar jToolMerge;

    private JButton cmdCopyLeaf;

    private JTabbedPane jTabbedPane1;

    private JMenu mnuFile;
}

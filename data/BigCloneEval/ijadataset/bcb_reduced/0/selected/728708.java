package com.qspin.qtaste.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.TreePath;
import org.apache.log4j.Logger;
import com.qspin.qtaste.config.StaticConfiguration;
import com.qspin.qtaste.config.TestBedConfiguration;
import com.qspin.qtaste.config.TestEngineConfiguration;
import com.qspin.qtaste.debug.Breakpoint;
import com.qspin.qtaste.event.DumpPythonResultEvent;
import com.qspin.qtaste.event.DumpPythonResultEventHandler;
import com.qspin.qtaste.event.DumpPythonResultListener;
import com.qspin.qtaste.event.TestScriptBreakpointEvent;
import com.qspin.qtaste.event.TestScriptBreakpointHandler;
import com.qspin.qtaste.event.TestScriptBreakpointListener;
import com.qspin.qtaste.kernel.engine.TestEngine;
import com.qspin.qtaste.testsuite.TestSuite;
import com.qspin.qtaste.testsuite.impl.DirectoryTestSuite;
import com.qspin.qtaste.ui.csveditor.TestDataEditor;
import com.qspin.qtaste.ui.debug.DebugVariable;
import com.qspin.qtaste.ui.debug.DebugVariablePanel;
import com.qspin.qtaste.ui.jedit.DebuggerShortcuts;
import com.qspin.qtaste.ui.jedit.GenericShortcuts;
import com.qspin.qtaste.ui.jedit.NonWrappingTextPane;
import com.qspin.qtaste.ui.log4j.Log4jPanel;
import com.qspin.qtaste.ui.log4j.TextAreaAppender;
import com.qspin.qtaste.ui.testcampaign.TestCampaignMainPanel;
import com.qspin.qtaste.ui.tools.FileNode;
import com.qspin.qtaste.ui.tools.HTMLDocumentLoader;
import com.qspin.qtaste.ui.tools.PythonTestScript;
import com.qspin.qtaste.ui.tools.SpringUtilities;
import com.qspin.qtaste.util.Log4jLoggerFactory;
import com.qspin.qtaste.util.ScriptCheckSyntaxValidator;
import com.qspin.qtaste.util.ThreadManager;
import com.qspin.qtaste.ui.tools.ResourceManager;

@SuppressWarnings("serial")
public class TestCasePane extends JPanel implements TestScriptBreakpointListener, DumpPythonResultListener {

    private static Logger logger = Log4jLoggerFactory.getLogger(TestCasePane.class);

    protected JTextPane tcDocsPane = new JTextPane();

    protected Log4jPanel tcLogsPane = new Log4jPanel();

    private NonWrappingTextPane tcSourceTextPane = null;

    protected JButton executeButton = new JButton();

    protected JButton startExecutionButton = new JButton();

    protected JButton stepOverExecutionButton = new JButton();

    protected JButton stepIntoExecutionButton = new JButton();

    protected JButton stopExecutionButton = new JButton();

    protected JButton saveButton = new JButton();

    protected JButton resultsButton = new JButton("View Test Report");

    protected JButton debugButton = new JButton();

    protected JTabbedPane editorTabbedPane;

    protected JPanel tcSourcePanel = new JPanel(new BorderLayout());

    protected DebugVariablePanel debugPanel;

    public TestCaseTree tcTree;

    protected TestCaseResultsPane resultsPane;

    protected JTabbedPane tabbedPane;

    protected MainPanel parent;

    protected JSplitPane sourcePanel;

    protected boolean stopExecution = false;

    private static final String DOC = "Documentation";

    private static final String SOURCE = "Test Case Source";

    private static final String RESULTS = "Results";

    private static final String LOGS = "Logs";

    public static final int DOC_INDEX = 0;

    public static final int SOURCE_INDEX = 1;

    public static final int RESULTS_INDEX = 2;

    public static final int LOGS_INDEX = 3;

    public static final int SRC_TAB_SIZE = 2;

    private TestExecutionThread testExecutionHandler = null;

    private TestCampaignMainPanel.CampaignExecutionThread testCampaignExecutionHandler = null;

    private TestScriptBreakpointHandler breakPointEventHandler;

    public boolean isExecuting;

    private String currentSelectedTestsuite = "TestSuite";

    private FileNode currentSelectedFileNode = null;

    protected DumpPythonResultEventHandler pythonResultEventHandler = DumpPythonResultEventHandler.getInstance();

    private String mTestCaseSuitesRootDir = StaticConfiguration.DEFAULT_TESTSUITES_DIR;

    protected static final HTMLDocument EMPTY_DOC = new HTMLDocument();

    public TestCasePane(MainPanel parent) {
        super(new BorderLayout());
        this.parent = parent;
        resultsPane = new TestCaseResultsPane(this);
        breakPointEventHandler = TestScriptBreakpointHandler.getInstance();
        breakPointEventHandler.addTestScriptBreakpointListener(this);
        pythonResultEventHandler.addPythonResultListener(this);
        genUI();
    }

    @Override
    protected void finalize() {
        breakPointEventHandler.removeTestScriptBreakpointListener(this);
    }

    public void setTestCaseTree(TestCaseTree tcTree) {
        this.tcTree = tcTree;
        resultsPane.setTestCaseTree(tcTree);
    }

    public TestCaseTree getTestCaseTree() {
        return tcTree;
    }

    public boolean isDocTabSelected() {
        return tabbedPane.getSelectedIndex() == DOC_INDEX;
    }

    public void importTestSuites() {
        JFileChooser chooser = new JFileChooser(mTestCaseSuitesRootDir);
        chooser.setDialogTitle("Load TestSuites directory ...");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                String selectedTestSuites = chooser.getSelectedFile().getAbsoluteFile().getCanonicalPath();
                setTestSuiteDirectory(selectedTestSuites);
                tcTree.generateScriptsTree(selectedTestSuites);
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }

    public void setExecuteButtonsEnabled(boolean enabled) {
        executeButton.setEnabled(enabled);
        debugButton.setEnabled(enabled);
    }

    protected void genUI() {
        executeButton.setMnemonic(KeyEvent.VK_R);
        saveButton.setMnemonic(KeyEvent.VK_C);
        startExecutionButton.setMnemonic(KeyEvent.VK_N);
        tcDocsPane.setEditable(false);
        tcDocsPane.setContentType("text/html");
        TextAreaAppender.addTextArea(tcLogsPane);
        tcDocsPane.setEditorKit(new HTMLEditorKit());
        tcDocsPane.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
            }
        });
        ExecuteButtonAction buttonListener = new ExecuteButtonAction(this);
        executeButton.addActionListener(buttonListener);
        executeButton.setIcon(ResourceManager.getInstance().getImageIcon("icons/running_32"));
        executeButton.setToolTipText("Run Test(s)");
        startExecutionButton.setIcon(ResourceManager.getInstance().getImageIcon("icons/start"));
        startExecutionButton.setToolTipText("Continue test case execution (F8)");
        startExecutionButton.setVisible(false);
        startExecutionButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                continueDebug();
            }
        });
        stepOverExecutionButton.setIcon(ResourceManager.getInstance().getImageIcon("icons/stepover"));
        stepOverExecutionButton.setToolTipText("Step over the script execution (F6)");
        stepOverExecutionButton.setVisible(false);
        stepOverExecutionButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                continueStep();
            }
        });
        stepIntoExecutionButton.setIcon(ResourceManager.getInstance().getImageIcon("icons/stepinto"));
        stepIntoExecutionButton.setToolTipText("Step into the script execution (F5)");
        stepIntoExecutionButton.setVisible(false);
        stepIntoExecutionButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                continueStepInto();
            }
        });
        stopExecutionButton.setIcon(ResourceManager.getInstance().getImageIcon("icons/stop"));
        stopExecutionButton.setToolTipText("Stop execution");
        stopExecutionButton.setVisible(false);
        stopExecutionButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                stopExecution();
            }
        });
        debugButton.setIcon(ResourceManager.getInstance().getImageIcon("icons/debug"));
        debugButton.setToolTipText("Debug Test(s)");
        debugButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                {
                    runTestSuite(true, 1, false);
                }
            }
        });
        saveButton.setIcon(ResourceManager.getInstance().getImageIcon("icons/save_32"));
        saveButton.setToolTipText("Save and check script(s) syntax");
        saveButton.setName("save button");
        saveButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                checkScriptsSyntax();
            }
        });
        resultsButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                boolean showTestSuiteReport = resultsPane.getCurrentRunName().equals("Run1");
                String resDir = TestEngineConfiguration.getInstance().getString("reporting.generated_report_path");
                String baseDir = System.getProperty("user.dir");
                String filename = baseDir + File.separator + resDir + File.separator + (showTestSuiteReport ? "index.html" : "campaign.html");
                File resultsFile = new File(filename);
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(resultsFile);
                    } else {
                        logger.error("Feature not supported by this platform");
                    }
                } catch (IOException ex) {
                    logger.error("Could not open " + filename);
                }
            }
        });
        resultsButton.setToolTipText("View the HTML Test Run Summary Results");
        resultsButton.setName("test run results button");
        JPanel northP = new JPanel(new SpringLayout());
        northP.add(executeButton);
        northP.add(saveButton);
        northP.add(debugButton);
        northP.add(resultsButton);
        northP.add(new CommonShortcutsPanel());
        northP.add(startExecutionButton);
        northP.add(stepOverExecutionButton);
        northP.add(stepIntoExecutionButton);
        northP.add(stopExecutionButton);
        SpringUtilities.makeCompactGrid(northP, 1, 9, 6, 6, 6, 6);
        add(northP, BorderLayout.NORTH);
        tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
        editorTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        editorTabbedPane.addMouseListener(new TabMouseListener());
        editorTabbedPane.setFocusable(false);
        sourcePanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        getTcSourcePane().add(editorTabbedPane);
        sourcePanel.setTopComponent(getTcSourcePane());
        sourcePanel.setFocusable(false);
        sourcePanel.setDividerSize(4);
        debugPanel = new DebugVariablePanel();
        debugPanel.setPreferredSize(new Dimension(100, 150));
        sourcePanel.setResizeWeight(0.9);
        sourcePanel.setBottomComponent(debugPanel);
        debugPanel.setVisible(false);
        tabbedPane.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (tabbedPane.getSelectedIndex() == SOURCE_INDEX) {
                    Component tab = editorTabbedPane.getSelectedComponent();
                    if (tab != null && tab instanceof JScrollPane) {
                        JScrollPane scrollPane = (JScrollPane) tab;
                        tab = scrollPane.getViewport().getView();
                        if (tab != null) {
                            tab.requestFocusInWindow();
                        }
                    }
                }
                if (!isDocTabSelected()) {
                    return;
                }
                NonWrappingTextPane tsPane = getTcSourceTextPane();
                if (tsPane != null) {
                    File tsFile = new File(tsPane.getFileName());
                    PythonTestScript pScript = new PythonTestScript(tsFile, getTestSuiteDirectory());
                    if (pScript.isDocSynchronized()) {
                        return;
                    }
                    parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    pScript.generateDoc();
                    HTMLDocumentLoader loader = new HTMLDocumentLoader();
                    HTMLDocument doc;
                    try {
                        File tcDoc = pScript.getTestcaseDoc();
                        if (tcDoc != null) {
                            doc = loader.loadDocument(tcDoc.toURI().toURL());
                            setTestCaseInfo(doc);
                        } else {
                            setTestCaseInfo(null);
                        }
                    } catch (MalformedURLException e1) {
                        logger.error(e1);
                        setTestCaseInfo(null);
                    } catch (IOException e1) {
                        logger.error(e1);
                        setTestCaseInfo(null);
                    }
                    parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
        tabbedPane.add(DOC, new JScrollPane(tcDocsPane));
        tabbedPane.add(SOURCE, sourcePanel);
        tabbedPane.add(RESULTS, resultsPane);
        tabbedPane.add(LOGS, tcLogsPane);
        add(tabbedPane, BorderLayout.CENTER);
    }

    public void stopExecution() {
        stopDebug();
        stopExecution = true;
        if (testExecutionHandler != null) {
            testExecutionHandler.stop();
        } else if (testCampaignExecutionHandler != null) {
            testCampaignExecutionHandler.stop();
        }
        TestEngine.cancelStartStopSUT();
    }

    public boolean checkScriptsSyntax() {
        int i = editorTabbedPane.getSelectedIndex();
        NonWrappingTextPane tabTextPane = getTextPane(i);
        if (tabTextPane != null) {
            if (tabTextPane.isModified()) {
                tabTextPane.save();
            }
            if (tabTextPane.getFileName().endsWith(".py")) {
                ScriptCheckSyntaxValidator scriptCheckSyntaxValidator = new ScriptCheckSyntaxValidator(tabTextPane.getFileName(), tabTextPane.getText());
                if (!scriptCheckSyntaxValidator.check()) {
                    return false;
                }
            }
        }
        TestDataEditor tabDataPane = this.getTestDataPane(i);
        if (tabDataPane != null && tabDataPane.isModified()) {
            tabDataPane.save();
        }
        return true;
    }

    public void stopDebug() {
        logger.trace("Test case pane: stop debug");
        breakPointEventHandler.stop();
    }

    public void continueDebug() {
        logger.trace("Test case pane: continue");
        breakPointEventHandler.continue_();
    }

    public void continueStep() {
        logger.trace("Test case pane: continue step");
        breakPointEventHandler.step();
    }

    public void continueStepInto() {
        logger.trace("Test case pane: continue step into");
        breakPointEventHandler.stepInto();
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public Log4jPanel getResultsLog4jPanel() {
        return resultsPane.getLog4jPanel();
    }

    public Document getCurrentTestCaseDoc() {
        return tcDocsPane.getDocument();
    }

    public JTextPane getDocPane() {
        return tcDocsPane;
    }

    public void setTestCaseInfo(Document htmlDoc) {
        if (htmlDoc == null) {
            tcDocsPane.setDocument(EMPTY_DOC);
        } else {
            tcDocsPane.setDocument(htmlDoc);
        }
        tcDocsPane.setCaretPosition(0);
        tcDocsPane.scrollRectToVisible(new Rectangle(0, 0, 0, 0));
        tcDocsPane.setEditable(false);
    }

    /**
     * loadTestCaseSource method loads the file contents if necessary. If the
     *   file is already loaded, this method activates the correct tab based
     *   on the filename
     * @param f : file to load in the JTextPane
     * @param activateSourceTab : specifies if the tab "TestCase Source" must be activated
     * @param isTestScript : specifies if the file to load is the main TestScript
     * @return the JTextPane(NonWrappingTextPane) containing the file content
     */
    public NonWrappingTextPane loadTestCaseSource(final File f, final boolean activateSourceTab, final boolean isTestScript) {
        return this.loadTestCaseSource(f, activateSourceTab, isTestScript, false);
    }

    /**
     * loadTestCaseSource method loads the file contents if necessary. If the
     *   file is already loaded, this method activates the correct tab based
     *   on the filename
     * @param f : file to load in the JTextPane
     * @param activateSourceTab : specifies if the tab "TestCase Source" must be activated
     * @param isTestScript : specifies if the file to load is the main TestScript
     * @param force : specifies if the file must be read from source
     * @return the JTextPane(NonWrappingTextPane) containing the file content
     */
    public NonWrappingTextPane loadTestCaseSource(final File f, final boolean activateSourceTab, final boolean isTestScript, final boolean force) {
        String absolutePath;
        try {
            absolutePath = f.getAbsoluteFile().getCanonicalPath();
        } catch (IOException e) {
            logger.fatal("Cannot load testcase source", e);
            return null;
        }
        NonWrappingTextPane textPane = activateSourceIfAlreadyLoaded(absolutePath, isTestScript);
        if (!force & textPane != null) {
            return textPane;
        }
        if (f.exists() && f.canRead()) {
            StringBuffer contents = new StringBuffer();
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(f));
                while (in.ready()) {
                    contents.append(in.readLine()).append("\n");
                }
                boolean newTab = textPane == null;
                if (textPane == null) {
                    textPane = new NonWrappingTextPane(isTestScript);
                }
                if (newTab) {
                    JScrollPane sp = new JScrollPane(textPane);
                    if (absolutePath.endsWith(".py")) {
                        textPane.init("text/python");
                    } else if (absolutePath.endsWith(".xml")) {
                        textPane.init("text/xml");
                    } else if (absolutePath.endsWith(".sh")) {
                        textPane.init("text/bash");
                    } else if (absolutePath.endsWith(".bat") || absolutePath.endsWith(".cmd")) {
                        textPane.init("text/dosbatch");
                    } else {
                        textPane.init("text/plain");
                    }
                    textPane.setFileName(absolutePath);
                    if (isTestScript) {
                        tcSourceTextPane = textPane;
                    }
                    textPane.setTestCasePane(this);
                    editorTabbedPane.addTab(f.getName(), null, sp, absolutePath);
                    editorTabbedPane.setSelectedIndex(editorTabbedPane.getTabCount() - 1);
                    textPane.addDocumentListener();
                    textPane.addPropertyChangeListener("isModified", new PropertyChangeListener() {

                        public void propertyChange(PropertyChangeEvent evt) {
                            if (evt.getNewValue().equals(true)) {
                                String currentTitle = editorTabbedPane.getTitleAt(editorTabbedPane.getSelectedIndex());
                                if (!currentTitle.contains("*")) {
                                    currentTitle += " *";
                                    editorTabbedPane.setTitleAt(editorTabbedPane.getSelectedIndex(), currentTitle);
                                }
                            } else {
                                String currentTitle = editorTabbedPane.getTitleAt(editorTabbedPane.getSelectedIndex());
                                if (currentTitle.contains(" *")) {
                                    currentTitle = currentTitle.replace(" *", "");
                                    editorTabbedPane.setTitleAt(editorTabbedPane.getSelectedIndex(), currentTitle);
                                }
                            }
                        }
                    });
                }
                setTestCaseSource(textPane, absolutePath, contents.toString(), isTestScript);
                textPane.setModified(false);
                textPane.clearUndos();
                if (absolutePath.endsWith(".py")) {
                    new DebuggerShortcuts(this);
                }
                new GenericShortcuts(this);
                if (activateSourceTab) {
                    tabbedPane.setSelectedIndex(TestCasePane.SOURCE_INDEX);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ioe) {
                }
            }
        }
        return textPane;
    }

    /** 
     * This method gets the current activated textPane
     * @return null if there is no tab or the current tab is not a TextPane
     */
    public NonWrappingTextPane getActiveTextPane() {
        int tabIndex = editorTabbedPane.getSelectedIndex();
        if (tabIndex == -1) {
            return null;
        }
        return this.getTextPane(tabIndex);
    }

    /**
     * This method return the textPane in which the main TestScript is loaded
     * @return NonWrappingTextPane object containing the TestScript file
     */
    public NonWrappingTextPane getTcSourceTextPane() {
        return tcSourceTextPane;
    }

    /**
     * This method activates the python script based on the file name if already loaded
     * @param fileName of the script file
     * @param isTestScript specifies if the python script is the main TestScript file
     * @return TextPane is already loaded otherwise null
     */
    public NonWrappingTextPane activateSourceIfAlreadyLoaded(String fileName, boolean isTestScript) {
        boolean tcTextFound = false;
        int testScriptTabIndex = getTestScriptTabIndex();
        if (testScriptTabIndex != -1) {
            tcTextFound = true;
        }
        int textPaneTabIndex = getTextPane(fileName);
        if (textPaneTabIndex != -1) {
            editorTabbedPane.setSelectedIndex(textPaneTabIndex);
            tabbedPane.setSelectedIndex(TestCasePane.SOURCE_INDEX);
            return getTextPane(textPaneTabIndex);
        }
        if (tcTextFound && isTestScript) {
            closeAllTabs();
        }
        return null;
    }

    /**
     * Close all tabs and ask user confirmation if a file is modified
     */
    public void closeAllTabs() {
        while (editorTabbedPane.getTabCount() > 0) {
            NonWrappingTextPane currentPane = getTextPane(0);
            if (currentPane != null) {
                if (currentPane.isModified()) {
                    if (JOptionPane.showConfirmDialog(null, "Do you want to save your current modification in '" + currentPane.getFileName() + "?'", "Save confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        currentPane.save();
                    }
                }
                currentPane.removeAllBreakpoints();
                currentPane = null;
            }
            TestDataEditor currentDataPane = getTestDataPane(0);
            if (currentDataPane != null) {
                if (currentDataPane.isModified()) {
                    if (JOptionPane.showConfirmDialog(null, "Do you want to save your current modification in '" + currentDataPane.getCurrentCSVFile() + "?'", "Save confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        currentDataPane.save();
                    }
                }
                currentDataPane = null;
            }
            editorTabbedPane.removeTabAt(0);
        }
    }

    /**
     * This method saves the current document only if the current pane is a textpane containing a python script
     */
    public void saveActiveDocument() {
        NonWrappingTextPane activeTextPane = getActiveTextPane();
        if (activeTextPane != null) {
            activeTextPane.save();
        }
    }

    public void setTestCaseSource(NonWrappingTextPane textPane, String fileName, String contents, boolean isTestScript) {
        textPane.setText(contents);
        textPane.setCaretPosition(0);
    }

    public NonWrappingTextPane getTestScripPane() {
        int testScriptTabIndex = this.getTestScriptTabIndex();
        if (testScriptTabIndex != -1) {
            return this.getTextPane(testScriptTabIndex);
        }
        return null;
    }

    public int getTestScriptTabIndex() {
        for (int i = 0; i < editorTabbedPane.getTabCount(); i++) {
            NonWrappingTextPane tabTextPane = getTextPane(i);
            if (tabTextPane != null) {
                if (tabTextPane.isTestScript) {
                    return i;
                }
            }
        }
        return -1;
    }

    public NonWrappingTextPane[] getVisibleTextPanes() {
        ArrayList<NonWrappingTextPane> textPanes = new ArrayList<NonWrappingTextPane>();
        for (int i = 0; i < editorTabbedPane.getTabCount(); i++) {
            NonWrappingTextPane tabTextPane = getTextPane(i);
            if (tabTextPane == null) {
                continue;
            }
            textPanes.add(tabTextPane);
        }
        NonWrappingTextPane[] array = new NonWrappingTextPane[textPanes.size()];
        textPanes.toArray(array);
        return array;
    }

    public int getTextPane(String fileName) {
        for (int i = 0; i < editorTabbedPane.getTabCount(); i++) {
            NonWrappingTextPane tabTextPane = getTextPane(i);
            if (tabTextPane == null) {
                continue;
            }
            if (tabTextPane.getFileName().equals(fileName)) {
                return i;
            }
        }
        return -1;
    }

    public NonWrappingTextPane getTextPane(int tabIndex) {
        Component comp = editorTabbedPane.getComponentAt(tabIndex);
        if (comp instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) comp;
            if (sp.getViewport() != null) {
                for (int j = 0; j < sp.getViewport().getComponentCount(); j++) {
                    comp = sp.getViewport().getComponent(j);
                    if (comp instanceof NonWrappingTextPane) {
                        return (NonWrappingTextPane) comp;
                    }
                }
            }
        }
        return null;
    }

    public TestDataEditor getTestDataPane(int tabIndex) {
        Component comp = editorTabbedPane.getComponentAt(tabIndex);
        if (comp instanceof TestDataEditor) {
            return (TestDataEditor) comp;
        }
        return null;
    }

    public TestDataEditor getTestDataPane() {
        for (int i = 0; i < editorTabbedPane.getTabCount(); i++) {
            TestDataEditor testDataEditor = getTestDataPane(i);
            if (testDataEditor != null) {
                return testDataEditor;
            }
        }
        return null;
    }

    public int getTestDataTabIndex() {
        for (int i = 0; i < editorTabbedPane.getTabCount(); i++) {
            TestDataEditor tabTestDataPane = getTestDataPane(i);
            if (tabTestDataPane != null) {
                return i;
            }
        }
        return -1;
    }

    public void activateTestcaseSourceTab() {
        int testScriptTabIndex = getTestScriptTabIndex();
        if (testScriptTabIndex != -1) {
            editorTabbedPane.setSelectedIndex(testScriptTabIndex);
        }
    }

    public void showTestcaseResultsTab() {
        tabbedPane.setSelectedIndex(RESULTS_INDEX);
    }

    public void loadCSVFile(String fileName) {
        int testDataTabIndex = getTestDataTabIndex();
        if (testDataTabIndex != -1) {
            TestDataEditor currentTestDataEditor = this.getTestDataPane(testDataTabIndex);
            if (currentTestDataEditor != null) {
                if (currentTestDataEditor.getCurrentCSVFile().equals(fileName)) {
                    return;
                }
            }
        }
        TestDataEditor dataEditor = new TestDataEditor();
        dataEditor.loadCSVFile(fileName);
        editorTabbedPane.addTab("TestData", null, dataEditor, fileName);
        dataEditor.addPropertyChangeListener("isModified", new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue().equals(true)) {
                    String currentTitle = editorTabbedPane.getTitleAt(editorTabbedPane.getSelectedIndex());
                    if (!currentTitle.contains("*")) {
                        currentTitle += " *";
                        editorTabbedPane.setTitleAt(editorTabbedPane.getSelectedIndex(), currentTitle);
                    }
                } else {
                    String currentTitle = editorTabbedPane.getTitleAt(editorTabbedPane.getSelectedIndex());
                    if (currentTitle.contains(" *")) {
                        currentTitle = currentTitle.replace(" *", "");
                        editorTabbedPane.setTitleAt(editorTabbedPane.getSelectedIndex(), currentTitle);
                    }
                }
            }
        });
    }

    public JPanel getTcSourcePane() {
        return tcSourcePanel;
    }

    public String getCurrentSelectedTestsuite() {
        return currentSelectedTestsuite;
    }

    public void setCurrentSelectedTestsuite(String currentSelectedTestsuite) {
        this.currentSelectedTestsuite = currentSelectedTestsuite;
    }

    public FileNode getCurrentSelectedFileNode() {
        return currentSelectedFileNode;
    }

    public void setCurrentSelectedFileNode(FileNode currentSelectedFileNode) {
        this.currentSelectedFileNode = currentSelectedFileNode;
    }

    protected class ExecuteButtonAction implements ActionListener {

        TestCasePane tcPane;

        protected ExecuteButtonAction(TestCasePane tcPane) {
            this.tcPane = tcPane;
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            runTestSuite(false);
        }
    }

    public void runTestSuite(boolean debug) {
        runTestSuite(debug, 1, false);
    }

    public void runTestSuite(final boolean debug, final int numberLoops, final boolean loopsInHours) {
        final TreePath[] selectedTcs = tcTree.getSelectionPaths();
        if (selectedTcs == null) {
            logger.warn("No testsuite selected");
            return;
        }
        Object[] obj = selectedTcs[0].getPath();
        String testSuiteDir = mTestCaseSuitesRootDir;
        for (Object o : obj) {
            if (o instanceof TCTreeNode) {
                TCTreeNode tcTreeNode = (TCTreeNode) o;
                if (tcTreeNode.getParent() != null) {
                    testSuiteDir += File.separator + o.toString();
                }
            }
        }
        DirectoryTestSuite testSuite = new DirectoryTestSuite(testSuiteDir);
        testSuite.setExecutionLoops(numberLoops, loopsInHours);
        runTestSuite(testSuite, debug);
    }

    public void setTestSuiteDirectory(String dir) {
        mTestCaseSuitesRootDir = dir;
    }

    public String getTestSuiteDirectory() {
        return mTestCaseSuitesRootDir;
    }

    public void runTestSuite(final TestSuite testSuite, final boolean debug) {
        if (!this.checkScriptsSyntax()) {
            return;
        }
        resultsPane.setRunTab("Run1");
        resultsPane.resetTables();
        tabbedPane.setSelectedIndex(RESULTS_INDEX);
        resultsPane.setVisible(true);
        stopExecution = false;
        resultsPane.proceedExecution();
        parent.setTestSuite(testSuite.getName());
        parent.refreshParams();
        TestBedConfiguration.setSUTVersion(parent.getSUTVersion());
        isExecuting = true;
        stopExecution = false;
        stopExecutionButton.setVisible(true);
        parent.getHeaderPanel().setControlTestbedButtonsEnabled();
        testExecutionHandler = new TestExecutionThread(testSuite, debug);
        Thread t = new Thread(testExecutionHandler);
        t.start();
    }

    public void doAction(TestScriptBreakpointEvent event) {
        switch(event.getAction()) {
            case CONTINUE:
            case STEP:
            case STEPINTO:
            case STOP:
                startExecutionButton.setVisible(false);
                stepOverExecutionButton.setVisible(false);
                stepIntoExecutionButton.setVisible(false);
                tabbedPane.setSelectedIndex(RESULTS_INDEX);
                debugPanel.setVisible(false);
                NonWrappingTextPane[] panes = this.getVisibleTextPanes();
                for (int i = 0; i < panes.length; i++) {
                    NonWrappingTextPane textPane = panes[i];
                    textPane.getDefaultLineNumberPanel().update(event);
                }
                break;
            case BREAK:
                startExecutionButton.setVisible(true);
                stepOverExecutionButton.setVisible(true);
                stepIntoExecutionButton.setVisible(true);
                debugPanel.setVisible(true);
                sourcePanel.setDividerLocation(sourcePanel.getDividerLocation() - 150);
                tabbedPane.setSelectedIndex(SOURCE_INDEX);
                getTcSourcePane().setVisible(true);
                Object extraData = event.getExtraData();
                if (extraData instanceof Breakpoint) {
                    Breakpoint breakpoint = (Breakpoint) extraData;
                    File f = new File(breakpoint.getFileName());
                    NonWrappingTextPane textPane = null;
                    if (f.exists() && f.canRead()) {
                        textPane = loadTestCaseSource(f, true, f.getName().equals(StaticConfiguration.TEST_SCRIPT_FILENAME));
                        tabbedPane.setSelectedIndex(TestCasePane.SOURCE_INDEX);
                        textPane.selectLine(breakpoint.getLineIndex());
                        textPane.getLineNumberPanel().update(event);
                    }
                }
                break;
        }
    }

    public void setExecutingTestCampaign(boolean executing, TestCampaignMainPanel.CampaignExecutionThread testCampaignExecutionHandler) {
        isExecuting = executing;
        this.testCampaignExecutionHandler = testCampaignExecutionHandler;
    }

    public void updateButtons() {
        SwingUtilities.invokeLater(new UpdateButtons());
    }

    public void setSelectedTab(int index) {
        tabbedPane.setSelectedIndex(index);
    }

    @SuppressWarnings("unchecked")
    public void pythonResult(DumpPythonResultEvent event) {
        if (event.getSource() instanceof ArrayList) {
            ArrayList<DebugVariable> debugVariables = (ArrayList<DebugVariable>) event.getSource();
            debugPanel.setDebugVariables(debugVariables);
        }
    }

    public class UpdateButtons implements Runnable {

        public void run() {
            executeButton.setEnabled(!isExecuting);
            stopExecutionButton.setVisible(isExecuting);
            debugButton.setEnabled(!isExecuting);
            parent.getHeaderPanel().setControlTestbedButtonsEnabled();
            if (!isExecuting) {
                tabbedPane.setSelectedIndex(RESULTS_INDEX);
            }
        }
    }

    public class TestExecutionThread implements Runnable {

        private TestSuite testSuite;

        private boolean debug;

        public TestExecutionThread(final TestSuite testSuite, final boolean debug) {
            this.testSuite = testSuite;
            this.debug = debug;
        }

        public void stop() {
            ThreadGroup root = Thread.currentThread().getThreadGroup();
            ThreadManager.stopThread(root, 0);
        }

        public void run() {
            SwingUtilities.invokeLater(new UpdateButtons());
            isExecuting = true;
            try {
                TestEngine.execute(testSuite, debug);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isExecuting = false;
                SwingUtilities.invokeLater(new UpdateButtons());
                testExecutionHandler = null;
                debugPanel.setVisible(false);
            }
        }
    }

    public void closeCurrentEditorFile() {
        NonWrappingTextPane textPane = getActiveTextPane();
        if (textPane != null) {
            if (!textPane.isTestScript) {
                int tabIndex = editorTabbedPane.getSelectedIndex();
                if (tabIndex != -1) {
                    if (textPane.isModified()) {
                        if (JOptionPane.showConfirmDialog(null, "Do you want to save your current modification in '" + textPane.getFileName() + "?'", "Save confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            textPane.save();
                        }
                    }
                    editorTabbedPane.remove(tabIndex);
                }
            }
        }
    }

    public class TabMouseListener extends MouseAdapter {

        private void evaluatePopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                JPopupMenu menu = new JPopupMenu();
                menu.add(new FileCloseAction());
                Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), TestCasePane.this);
                menu.show(TestCasePane.this, pt.x, pt.y);
                e.consume();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            evaluatePopup(e);
            if (e.isConsumed()) {
                return;
            }
            if (e.getButton() == MouseEvent.BUTTON2) {
                closeCurrentEditorFile();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            evaluatePopup(e);
        }
    }

    class FileCloseAction extends AbstractAction {

        public FileCloseAction() {
            super("Close");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            closeCurrentEditorFile();
        }

        @Override
        public boolean isEnabled() {
            NonWrappingTextPane textPane = getActiveTextPane();
            if (textPane != null) {
                if (!textPane.isTestScript) {
                    return true;
                }
            }
            return false;
        }
    }
}

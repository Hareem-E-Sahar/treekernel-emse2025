import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.filechooser.*;
import java.beans.*;

/** 
The MainPane class is the top-level window for this application, holding all other windows in its contentPane
(which is a JDesktopPane).  There should be only one instance of MainPane.  The static fields <code>activity</code> and
<code>openBrowsers</code> hold, respectively, the system-wide Error Log and a list of all open LibBrowser windows
(for good housekeeping purposes).
<p>
This class creates the MenuBar and is the ActionListener which implements menu choices.  Menu choices which lead to 
exceptions trigger the instantiation of the Error Log and posting to it.  The Error Log is intended to record all
problems encountered and recommended solutions.  Therefore, all methods that post to it must provide enough information
about the problem for the User to know what specific action to take much later when the log is reviewed.

@author		Gary Morris, Northern Virginia Community College		garymorris2245@verizon.net
 */
public class MainPane extends JFrame implements ActionListener {

    public static KSJInternalFrame activity;

    static ArrayList<Object> openBrowsers = new ArrayList<Object>(), openPersonEditors = new ArrayList<Object>(), openFamilyEditors = new ArrayList<Object>();

    public static ContextEditor curr_CUC_Editor;

    static JMenu menuView;

    public JMenu menuFileAdd2Library;

    public JMenuItem miFileOpenSILK, miFileDelete, miFileDeleteContext, miFileSaveContext;

    public static JDesktopPane desktop;

    public static MainPane topPane;

    public static int testSerialNmbr;

    public static int buildNmbr = 11;

    public static int NUMBER_OF_EGOS = 4;

    public static boolean fill_In_Flag = false;

    private JMenuBar menuBar = new JMenuBar();

    JMenu menuAdmin;

    KSJInternalFrame currentFrame;

    Runtime topDog = Runtime.getRuntime();

    JFileChooser fc = null;

    File currDomTheoryDir = null, currGEDCOMDir = null;

    int numWindows = 0;

    public static TreeMap treeMapDeepCopy(TreeMap model) {
        TreeMap newTM = new TreeMap();
        Iterator topIter = model.entrySet().iterator();
        while (topIter.hasNext()) {
            Map.Entry entry = (Map.Entry) topIter.next();
            Object key = entry.getKey(), value = entry.getValue(), newValue;
            if (value instanceof TreeMap) {
                newValue = treeMapDeepCopy((TreeMap) value);
            } else if (value instanceof ArrayList) {
                newValue = new ArrayList<Object>((ArrayList<Object>) value);
            } else {
                newValue = value;
            }
            newTM.put(key, newValue);
        }
        return newTM;
    }

    public MainPane(String title) {
        super(title);
        createMenu();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        desktop = new JDesktopPane();
        topPane = this;
        setContentPane(desktop);
        Toolkit tKit = getToolkit();
        createActivityLog(desktop, menuView);
        Dimension wndSize = tKit.getScreenSize();
        int maxHi = 768, maxWide = 1024;
        setBounds(40, 0, Math.min(maxWide, (int) (wndSize.width / 1.1)), Math.min(maxHi, (int) (wndSize.height / 1.1)));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        topDog.addShutdownHook(new CleanUpThread());
        setVisible(false);
    }

    public static void displayError(String msg, String title, int typ) {
        if (activity == null) {
            createActivityLog(desktop, menuView);
        }
        JFrame topWindow = topPane;
        if (topWindow == null || !topWindow.isVisible()) {
            topWindow = SIL_Edit.editWindow;
        }
        JOptionPane.showMessageDialog(topWindow, msg, title, typ);
        MainPane.activity.log.append(msg + "\n\n");
    }

    /** Add a window to the desktop, and make it the current focus. 

    @param wind  A window to be added to the system's desktop & displayed. */
    public void addWindow(KSJInternalFrame wind) {
        currentFrame = wind;
        numWindows++;
        wind.desktop = desktop;
        wind.miViewMe = menuView.add(wind.windowNum);
        wind.miViewMe.addActionListener(wind);
        wind.menuView = menuView;
        desktop.add(wind);
        wind.show();
    }

    public void createMenu() {
        setJMenuBar(menuBar);
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic(KeyEvent.VK_F);
        JMenu menuEdit = new JMenu("Edit");
        menuEdit.setMnemonic(KeyEvent.VK_E);
        menuView = new JMenu("View");
        menuView.setMnemonic(KeyEvent.VK_V);
        menuAdmin = new JMenu("Admin");
        JMenu menuHelp = new HelpFrame.HMenu();
        menuHelp.setText("Help");
        menuHelp.setMnemonic(KeyEvent.VK_H);
        HelpFrame.help = new HelpFrame();
        JMenuItem miFileNewBrowser = menuFile.add("New Library Browser");
        miFileNewBrowser.setActionCommand("new browser");
        miFileNewBrowser.addActionListener(this);
        JMenuItem miFileClose = menuFile.add("Close Window");
        miFileClose.setActionCommand("close window");
        miFileClose.addActionListener(this);
        miFileOpenSILK = menuFile.add("Open SILK File");
        miFileOpenSILK.setActionCommand("open silk");
        miFileOpenSILK.addActionListener(this);
        JMenuItem miFileOpenContext = menuFile.add("Open Context");
        miFileOpenContext.setActionCommand("open context");
        miFileOpenContext.addActionListener(this);
        menuFileAdd2Library = new JMenu("Add to Library...");
        JMenuItem miFileAdd2LibraryDT = menuFileAdd2Library.add("Domain Theory");
        miFileAdd2LibraryDT.setActionCommand("add domain theory");
        miFileAdd2LibraryDT.addActionListener(this);
        JMenuItem miFileAdd2LibraryBatch = menuFileAdd2Library.add("Batch Add Dom Thys");
        miFileAdd2LibraryBatch.setActionCommand("add batch of domain theories");
        miFileAdd2LibraryBatch.addActionListener(this);
        JMenuItem miFileAdd2LibraryCen = menuFileAdd2Library.add("People Data (.cen)");
        miFileAdd2LibraryCen.setEnabled(false);
        JMenuItem miFileAdd2LibraryGEDCOM = menuFileAdd2Library.add("GEDCOM File (.ged)");
        miFileAdd2LibraryGEDCOM.setEnabled(false);
        menuFile.add(menuFileAdd2Library);
        miFileDelete = menuFile.add("Delete From Library...");
        miFileDelete.setActionCommand("delete from library");
        miFileDelete.addActionListener(this);
        miFileDeleteContext = menuFile.add("Delete Context");
        miFileDeleteContext.setActionCommand("delete context");
        miFileDeleteContext.addActionListener(this);
        menuFile.addSeparator();
        miFileSaveContext = menuFile.add("Save Context");
        miFileSaveContext.setActionCommand("save context");
        miFileSaveContext.addActionListener(this);
        JMenuItem miFileSaveContextAs = menuFile.add("Save Context As...");
        miFileSaveContextAs.setActionCommand("save context as");
        miFileSaveContextAs.addActionListener(this);
        JMenu menuFileExport = new JMenu("Export File ...");
        JMenuItem miFileExportDT = menuFileExport.add("Domain Theory (.thy)");
        miFileExportDT.setActionCommand("export domain theory");
        miFileExportDT.addActionListener(this);
        JMenuItem miFileExportCen = menuFileExport.add("People Data (.cen)");
        miFileExportCen.setEnabled(false);
        JMenuItem miFileExportGEDCOM = menuFileExport.add("GEDCOM File (.ged)");
        miFileExportGEDCOM.setActionCommand("export GEDCOM");
        miFileExportGEDCOM.addActionListener(this);
        menuFile.add(menuFileExport);
        menuFile.addSeparator();
        JMenuItem miFileExit = menuFile.add("Exit SILKin");
        miFileExit.setActionCommand("exit");
        miFileExit.addActionListener(this);
        JMenuItem miEditUnDo = menuEdit.add("Un-Do");
        miEditUnDo.setEnabled(false);
        JMenuItem miEditReDo = menuEdit.add("Re-Do");
        miEditReDo.setEnabled(false);
        menuEdit.addSeparator();
        JMenuItem miEditCut = new JMenuItem(new DefaultEditorKit.CutAction());
        miEditCut.setText("Cut");
        miEditCut.setMnemonic(KeyEvent.VK_T);
        menuEdit.add(miEditCut);
        JMenuItem miEditCopy = new JMenuItem(new DefaultEditorKit.CopyAction());
        miEditCopy.setText("Copy");
        miEditCopy.setMnemonic(KeyEvent.VK_C);
        menuEdit.add(miEditCopy);
        JMenuItem miEditPaste = new JMenuItem(new DefaultEditorKit.PasteAction());
        miEditPaste.setText("Paste");
        miEditPaste.setMnemonic(KeyEvent.VK_P);
        menuEdit.add(miEditPaste);
        menuEdit.addSeparator();
        JMenuItem miEditPrefs = menuEdit.add("Edit Prefs");
        miEditPrefs.setActionCommand("edit prefs");
        miEditPrefs.addActionListener(this);
        JMenuItem miEditUserContext = menuEdit.add("Edit User Context");
        miEditUserContext.setActionCommand("edit user context");
        miEditUserContext.addActionListener(this);
        JMenuItem miTest1 = menuAdmin.add("Compute Similarity Matrix");
        miTest1.setActionCommand("Compute Similarity Matrix");
        miTest1.addActionListener(this);
        JMenuItem miTest2 = menuAdmin.add("Make Curr Lang dyadsUndefined");
        miTest2.setActionCommand("Make Curr Lang dyadsUndefined");
        miTest2.addActionListener(this);
        JMenuItem miTest3 = menuAdmin.add("Match dyadsUndefined");
        miTest3.setActionCommand("Match dyadsUndefined");
        miTest3.addActionListener(this);
        JMenuItem miTest4 = menuAdmin.add("Cluster the Feature Vectors");
        miTest4.setActionCommand("Cluster the Feature Vectors");
        miTest4.addActionListener(this);
        JMenuItem miTest5 = menuAdmin.add("Gen CUC Population");
        miTest5.setActionCommand("Gen CUC Population");
        miTest5.addActionListener(this);
        JMenuItem miTest6 = menuAdmin.add("Gen Indexes");
        miTest6.setActionCommand("Gen Indexes");
        miTest6.addActionListener(this);
        JMenuItem miTest7 = menuAdmin.add("Merge Indexes");
        miTest7.setActionCommand("Merge Indexes");
        miTest7.addActionListener(this);
        JMenuItem miTest10 = menuAdmin.add("Simulate User Data");
        miTest10.setActionCommand("Simulate User Data");
        miTest10.addActionListener(this);
        JMenuItem miTest11 = menuAdmin.add("Leave One Out");
        miTest11.setActionCommand("Leave One Out");
        miTest11.addActionListener(this);
        JMenuItem miTest12 = menuAdmin.add("Leave 1 Instant Replay");
        miTest12.setActionCommand("Leave 1 Instant Replay");
        miTest12.addActionListener(this);
        menuBar.add(menuFile);
        menuBar.add(menuEdit);
        menuBar.add(menuView);
        menuBar.add(menuAdmin);
        menuBar.add(menuHelp);
        if (Library.currentActivity != Library.ADMIN) {
            menuAdmin.setEnabled(false);
        }
        menuBar.validate();
    }

    public void enableAdvancedMenuItems(boolean bool) {
        menuFileAdd2Library.setEnabled(bool);
        miFileDelete.setEnabled(bool);
        miFileDeleteContext.setEnabled(bool);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("new browser")) {
            browseLibrary();
        } else if (e.getActionCommand().equals("new context")) {
            try {
                Library.createUserContext(currentFrame);
            } catch (Exception ex) {
                activity.log.append("While creating a new context, " + ex + "\n\n");
            }
            changeActivity(Library.DATA_GATHERING);
        } else if (e.getActionCommand().equals("Match dyadsUndefined")) {
            try {
                DomainTheory.current.matchDyads();
            } catch (Exception ex) {
                activity.log.append("While matching dyadsUndefined, " + ex + "\n\n");
            }
        } else if (e.getActionCommand().equals("open context")) {
            try {
                Library.chooseUserContext(currentFrame);
            } catch (Exception ex) {
                activity.log.append("While opening a new context, " + ex + "\n\n");
            }
            changeActivity(Library.DATA_GATHERING);
        } else if (e.getActionCommand().equals("open silk")) {
            if (fc == null) {
                fc = new JFileChooser();
            }
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fc.setDialogTitle("File from KinshipEditor (.silk)");
            int returnVal = fc.showOpenDialog(desktop);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                changeActivity(Library.DATA_GATHERING);
                File file = fc.getSelectedFile();
                try {
                    Library.loadSILKFile(file);
                } catch (Exception ex) {
                    activity.log.append("While opening a KinshipEditor (.silk) file:\n" + ex + "\n\n");
                }
            }
        } else if (e.getActionCommand().equals("close window")) {
            KSJInternalFrame frame = (KSJInternalFrame) desktop.getSelectedFrame();
            frame.doDefaultCloseAction();
        } else if (e.getActionCommand().equals("exit")) {
            if (SIL_Edit.editWindow != null && SIL_Edit.editWindow.chart.dirty) {
                SIL_Edit.editWindow.chart.doWantToSave();
            }
            System.exit(0);
        } else if (e.getActionCommand().equals("delete from library")) {
            String[] langs = Library.genCtxtMenu("All");
            String victim = (String) JOptionPane.showInputDialog(desktop, "1)  For what language would you like to make a deletion?", "What To Delete", JOptionPane.PLAIN_MESSAGE, null, langs, langs[0]);
            if (victim == null) {
                return;
            }
            if (victim.equals("All")) {
                String vict;
                Library.ContextStub cs1;
                File aboutToDi;
                try {
                    for (int k = 1; k < langs.length; k++) {
                        vict = langs[k];
                        cs1 = Library.retrieveOrCreateStub(vict);
                        Library.removeContextStub(cs1);
                        aboutToDi = new File(Library.libraryCtxtDirectory + vict + ".ctxt");
                        aboutToDi.delete();
                    }
                    Library.cbIndex = null;
                    Library.writeStubFile();
                } catch (Exception exc) {
                    activity.log.append("While deleting Library files:\n" + exc + "\n\n");
                }
            } else {
                Library.ContextStub cs = Library.retrieveOrCreateStub(victim);
                Context actxt = (Context) Library.activeContexts.get(victim);
                String[] choices;
                if (cs.adrThyExists && cs.refThyExists) {
                    choices = new String[4];
                    choices[3] = "Both domain theories";
                    choices[1] = victim + " domain theory";
                    choices[2] = victim + " (Adr) domain theory";
                    choices[0] = "The entire " + victim + " context & its 2 domain theories";
                } else if (cs.refThyExists) {
                    choices = new String[2];
                    choices[1] = victim + " domain theory";
                    choices[0] = "The entire " + victim + " context and its domain theory";
                } else if (cs.adrThyExists) {
                    choices = new String[2];
                    choices[1] = victim + " (Adr) domain theory";
                    choices[0] = "The entire " + victim + " context and its domain theory";
                } else {
                    choices = new String[1];
                    choices[0] = "The entire " + victim + " context.  (It has no domain theory.)";
                }
                String choice = (String) JOptionPane.showInputDialog(desktop, "2)  Which elements of the context for that language \nwould you like to delete?", "What To Delete", JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
                if (choice == null) {
                    return;
                }
                String[] onlyAlso = { "Card File ONLY", "Disk ALSO" };
                String eraser = (String) JOptionPane.showInputDialog(desktop, "3)  Delete from the Library's 'card file' only?\n Or delete the file from your computer also?", "What To Delete", JOptionPane.PLAIN_MESSAGE, null, onlyAlso, "Disk ALSO");
                if (eraser == null) {
                    return;
                }
                String filePath = Library.libraryCtxtDirectory + victim;
                File aboutToDie;
                try {
                    if (choice.substring(0, 10).equals("The entire")) {
                        Library.removeContextStub(cs);
                        Library.writeStubFile();
                        if (eraser.substring(0, 4).equals("Disk")) {
                            aboutToDie = new File(filePath + ".ctxt");
                            aboutToDie.delete();
                            filePath = Library.thyDirectory + victim;
                            aboutToDie = new File(filePath + ".thy");
                            if (aboutToDie.exists()) {
                                aboutToDie.delete();
                            }
                            aboutToDie = new File(filePath + "(Adr).thy");
                            if (aboutToDie.exists()) {
                                aboutToDie.delete();
                            }
                        }
                    } else if (choice.substring(0, 4).equals("Both")) {
                        cs.refThyExists = false;
                        cs.adrThyExists = false;
                        if (actxt == null) {
                            actxt = Library.readContextFromDisk(filePath + ".ctxt");
                        }
                        actxt.domTheoryAdrNullify();
                        actxt.domTheoryRefNullify();
                        if (eraser.substring(0, 4).equals("Disk")) {
                            Library.saveContextToDisk(actxt);
                            filePath = Library.thyDirectory + victim;
                            aboutToDie = new File(filePath + ".thy");
                            if (aboutToDie.exists()) {
                                aboutToDie.delete();
                            }
                            aboutToDie = new File(filePath + "(Adr).thy");
                            if (aboutToDie.exists()) {
                                aboutToDie.delete();
                            }
                        }
                    } else if (choice.indexOf("(Adr)") >= 0) {
                        cs.adrThyExists = false;
                        if (actxt == null) {
                            actxt = Library.readContextFromDisk(filePath + ".ctxt");
                        }
                        actxt.domTheoryAdrNullify();
                        if (eraser.substring(0, 4).equals("Disk")) {
                            Library.saveContextToDisk(actxt);
                            filePath = Library.thyDirectory + victim;
                            aboutToDie = new File(filePath + "(Adr).thy");
                            if (aboutToDie.exists()) {
                                aboutToDie.delete();
                            }
                        }
                    } else {
                        cs.refThyExists = false;
                        if (actxt == null) {
                            actxt = Library.readContextFromDisk(filePath + ".ctxt");
                        }
                        actxt.domTheoryRefNullify();
                        if (eraser.substring(0, 4).equals("Disk")) {
                            Library.saveContextToDisk(actxt);
                            filePath = Library.thyDirectory + victim;
                            aboutToDie = new File(filePath + ".thy");
                            if (aboutToDie.exists()) {
                                aboutToDie.delete();
                            }
                        }
                    }
                } catch (Exception exc) {
                    activity.log.append("While deleting Library files:\n" + exc + "\n\n");
                }
            }
            for (int i = 0; i < MainPane.openBrowsers.size(); i++) {
                LibBrowser lb = (LibBrowser) MainPane.openBrowsers.get(i);
                lb.picker.refreshLangMenu();
            }
            JOptionPane.showMessageDialog(desktop, "Requested deletions are completed.", "Task Completed", JOptionPane.PLAIN_MESSAGE);
        } else if (e.getActionCommand().equals("add domain theory")) {
            if (fc == null) {
                fc = new JFileChooser();
            }
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fc.setDialogTitle("File holding Domain Theory (Horn Clauses)");
            if (currDomTheoryDir != null) {
                fc.setCurrentDirectory(currDomTheoryDir);
            }
            int returnVal = fc.showOpenDialog(desktop);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                try {
                    Library.loadNewDomTh(file);
                    DomainTheory dt = DomainTheory.current;
                    Context.simulation = false;
                    dt.ctxt.simDataGen = false;
                    String lang = dt.languageName, filePath;
                    for (int i = 0; i < MainPane.openBrowsers.size(); i++) {
                        LibBrowser lb = (LibBrowser) MainPane.openBrowsers.get(i);
                        lb.picker.refreshLangMenu();
                    }
                    ArrayList<Object> egoList = new ArrayList<Object>();
                    if (FeatureVectorObj.el_1_distn == null) {
                        Library.readDistributionsFromFile();
                    }
                    FeatureVectorObj fv1 = dt.computeFeatureVector(egoList);
                    Library.postDistributions(fv1);
                    filePath = Library.libraryDirectory + "Feature Vectors/" + fv1.languageName + ".fvect";
                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
                    fv1.toDisk(out);
                    dt.findOverlappingTerms(egoList);
                    dt.findHiddenNeuterEgos();
                    currDomTheoryDir = fc.getCurrentDirectory();
                    filePath = Library.libraryDirectory + "Domain Theory Files/" + lang + ".thy";
                    out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
                    dt.toThyFile(out);
                    Library.saveContextToDisk(dt.ctxt);
                    JOptionPane.showMessageDialog(desktop, lang + " Successfully Added to Library.", "Task Completed", JOptionPane.PLAIN_MESSAGE);
                } catch (KSConstraintInconsistency exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = "PROBLEM: " + exc.toString().substring(where + 1) + "\nRECOMMENDATION: Correct or remove this clause from domain theory.\n                 " + "Then delete this theory from the Library & re-add it.";
                    displayError(msg, "Horn Clause Error - Constraints", JOptionPane.ERROR_MESSAGE);
                } catch (KSBadHornClauseException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = "PROBLEM: " + exc.toString().substring(where + 1) + "\nRECOMMENDATION: Correct or remove this clause from domain theory.\n                 " + "Then delete this theory from the Library & re-add it.";
                    displayError(msg, "Horn Clause Error", JOptionPane.ERROR_MESSAGE);
                } catch (KSParsingErrorException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = "PROBLEM: " + exc.toString().substring(where + 1) + "\nRECOMMENDATION: Correct this line number & try again.";
                    JOptionPane.showMessageDialog(desktop, msg, "Parsing Error", JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (ClassNotFoundException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = "PROBLEM: " + exc.toString().substring(where + 1) + "\nRECOMMENDATION: Submit Bug Report.";
                    JOptionPane.showMessageDialog(desktop, msg, "Missing Class encountered", JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (KSInternalErrorException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = "PROBLEM: " + exc.toString().substring(where + 1) + "\nRECOMMENDATION: To replace an existing theory, delete the old" + " version, then add the new version.";
                    JOptionPane.showMessageDialog(desktop, msg, "Duplicate Domain Theory", JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (NotSerializableException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = "PROBLEM: Internal Error: " + exc + "\nRECOMMENDATION: Submit Bug Report.";
                    JOptionPane.showMessageDialog(desktop, msg, "Internal Error", JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (IOException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = "PROBLEM: Writing out to Library, " + exc.toString().substring(where + 1) + "\nRECOMMENDATION: Check for disk, directory, or permissions problems.";
                    JOptionPane.showMessageDialog(desktop, msg, "File System Error", JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (JavaSystemException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = "PROBLEM: Writing out to Library, " + exc.toString().substring(where + 1) + "\nRECOMMENDATION: Check for disk, directory, or permissions problems.";
                    JOptionPane.showMessageDialog(desktop, msg, "File System Error", JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                }
            }
        } else if (e.getActionCommand().equals("add batch of domain theories")) {
            if (fc == null) {
                fc = new JFileChooser();
            }
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setDialogTitle("Directory holding Domain Theories");
            if (currDomTheoryDir != null) {
                fc.setCurrentDirectory(currDomTheoryDir.getParentFile());
            }
            String lang = "";
            DomainTheory dt = null;
            int returnVal = fc.showOpenDialog(desktop);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    File dir = fc.getSelectedFile();
                    if (!dir.isDirectory()) {
                        throw new JavaSystemException("Selected file is not a directory.");
                    }
                    int doFVs = JOptionPane.showConfirmDialog(this, "Compute Feature Vectors?");
                    int keep = JOptionPane.showConfirmDialog(this, "Keep Contexts Active?");
                    int overWrite = JOptionPane.showConfirmDialog(this, "Allow Over-Write of Existing DTs?");
                    Library.allowOverWrites = (overWrite == JOptionPane.YES_OPTION);
                    currDomTheoryDir = dir;
                    if (doFVs == JOptionPane.YES_OPTION && FeatureVectorObj.el_1_distn == null) {
                        Library.resetDistributions();
                    }
                    File[] batch = dir.listFiles();
                    for (int i = 0; i < batch.length; i++) {
                        File file = batch[i];
                        String fName = file.getName(), filePath, mod;
                        if (fName.indexOf(".") != 0) {
                            Library.loadNewDomTh(file);
                            dt = DomainTheory.current;
                            Context.current = dt.ctxt;
                            Context.simulation = false;
                            dt.ctxt.simDataGen = false;
                            if (keep == JOptionPane.NO_OPTION) {
                                lang = dt.languageName;
                                int end = lang.indexOf("(");
                                if (end > -1) {
                                    lang = lang.substring(0, end);
                                }
                                Library.activeContexts.remove(lang);
                            }
                            PrintWriter out;
                            ArrayList<Object> egoList = new ArrayList<Object>();
                            if (doFVs == JOptionPane.YES_OPTION) {
                                FeatureVectorObj fv1 = dt.computeFeatureVector(egoList);
                                filePath = Library.libraryDirectory + "Feature Vectors/" + dt.languageName + ".fvect";
                                out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
                                fv1.toDisk(out);
                            } else {
                                ClauseBody.priorPred = "";
                                ClauseBody.seqTotal = 0;
                                ClauseBody.dupTotal = 0;
                                Iterator ktdIter = dt.theory.values().iterator();
                                egoList = dt.maleAndFemaleCreatedHeThem();
                                while (ktdIter.hasNext()) {
                                    KinTermDef ktd = (KinTermDef) ktdIter.next();
                                    if (dt.printableTerm(ktd)) {
                                        ktd.assureExamplesGenerated(egoList);
                                        if (ktd.eqcSigExact == null) {
                                            ktd.makeSigStrings();
                                        }
                                    }
                                }
                                String pad = "";
                                if (++ClauseBody.seq < 10) {
                                    pad = "  ";
                                } else if (ClauseBody.seq < 100) {
                                    pad = " ";
                                }
                                System.out.println(ClauseBody.priorPred + ": " + pad + ClauseBody.seq + " - " + ClauseBody.dups + " = " + (ClauseBody.seq - ClauseBody.dups));
                                ClauseBody.seqTotal += ClauseBody.seq;
                                System.out.println("\nTotal clauses for " + dt.languageName + " is " + ClauseBody.seqTotal + " - " + ClauseBody.dupTotal + " = " + (ClauseBody.seqTotal - ClauseBody.dupTotal) + "\n\n");
                            }
                            dt.findOverlappingTerms(egoList);
                            dt.findHiddenNeuterEgos();
                            filePath = Library.libraryDirectory + "Domain Theory Files/" + dt.languageName + ".thy";
                            out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
                            dt.toThyFile(out);
                            System.out.println("Wrote: " + filePath);
                            Library.saveContextToDisk(Context.current);
                        }
                    }
                    for (int i = 0; i < MainPane.openBrowsers.size(); i++) {
                        LibBrowser lb = (LibBrowser) MainPane.openBrowsers.get(i);
                        lb.picker.refreshLangMenu();
                    }
                    int num = batch.length - 1;
                    JOptionPane.showMessageDialog(desktop, num + " DomTheories Added to Library.", "Task Completed", JOptionPane.PLAIN_MESSAGE);
                } catch (KSConstraintInconsistency exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = "PROBLEM with " + dt.languageName + ": " + exc.toString().substring(where + 1) + "\nRECOMMENDATION: Correct or remove this clause from domain theory.\n                 " + "Then delete this theory from the Library & re-add it.";
                    JOptionPane.showMessageDialog(desktop, msg, "Horn Clause Error - Constraints", JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (KSBadHornClauseException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = "PROBLEM with " + dt.languageName + ": " + exc.toString().substring(where + 1) + "\nRECOMMENDATION: Correct or remove this clause from domain theory.\n                 " + "Then delete this theory from the Library & re-add it.";
                    JOptionPane.showMessageDialog(desktop, msg, "Horn Clause Error", JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (KSParsingErrorException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = "PROBLEM in Parsing: " + exc.toString().substring(where + 1) + "\nRECOMMENDATION: Correct this line number & try again.";
                    JOptionPane.showMessageDialog(desktop, msg, "Parsing Error", JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (ClassNotFoundException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = "PROBLEM with " + dt.languageName + ": " + exc.toString().substring(where + 1) + "\nRECOMMENDATION: Submit Bug Report.";
                    JOptionPane.showMessageDialog(desktop, msg, "Missing Class encountered", JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (KSInternalErrorException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = "PROBLEM with " + DomainTheory.current.languageName + ": " + exc.toString().substring(where + 1) + "\nRECOMMENDATION: To replace an existing theory, delete the old" + " version, then add the new version.";
                    JOptionPane.showMessageDialog(desktop, msg, "Duplicate Domain Theory", JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (NotSerializableException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = "PROBLEM: Internal Error: " + exc.toString().substring(where + 1) + "\nRECOMMENDATION: Submit Bug Report.";
                    JOptionPane.showMessageDialog(desktop, msg, "Internal Error", JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (IOException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = "PROBLEM: Writing out to Library, " + exc.toString().substring(where + 1) + "\nRECOMMENDATION: Check for disk, directory, or permissions problems.";
                    JOptionPane.showMessageDialog(desktop, msg, "File System Error", JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (JavaSystemException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = "PROBLEM: Writing out to Library, " + exc.toString().substring(where + 1) + "\nRECOMMENDATION: Check for disk, directory, or permissions problems.";
                    JOptionPane.showMessageDialog(desktop, msg, "File System Error", JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                }
            }
        } else if (e.getActionCommand().equals("delete context")) {
            try {
                Library.deleteContext(currentFrame);
            } catch (Exception ex) {
                activity.log.append("While deleting a context, " + ex + "\n\n");
            }
            changeActivity(Library.DATA_GATHERING);
        } else if (e.getActionCommand().equals("save context")) {
            if (SIL_Edit.editWindow != null) {
                SIL_Edit.editWindow.chart.saveSILKinFile();
            } else try {
                Library.saveUserContext(currentFrame, false);
            } catch (Exception ex) {
                activity.log.append("While saving a context, " + ex + "\n\n");
            }
            changeActivity(Library.DATA_GATHERING);
        } else if (e.getActionCommand().equals("save context as")) {
            if (SIL_Edit.editWindow != null) {
                SIL_Edit.editWindow.chart.saveAsFile();
            } else try {
                Library.saveUserContext(currentFrame, true);
            } catch (Exception ex) {
                activity.log.append("While saving a context, " + ex + "\n\n");
            }
            changeActivity(Library.DATA_GATHERING);
        } else if (e.getActionCommand().equals("export GEDCOM")) {
            int population = Context.current.indSerNumGen + Context.current.famSerNumGen;
            if (population < 2) {
                JOptionPane.showMessageDialog(desktop, "You are requesting export of a total of " + population + " people and families.\n" + "That makes no sense.  Make sure you have chosen the correct\n" + "context and generated a population before exporting.", "Nothing To Export", JOptionPane.ERROR_MESSAGE);
            } else {
                if (fc == null) {
                    fc = new JFileChooser();
                }
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fc.setDialogTitle("Location for GEDCOM file -- .ged extension recommended");
                if (currGEDCOMDir != null) {
                    fc.setCurrentDirectory(currGEDCOMDir);
                }
                int returnVal = fc.showSaveDialog(desktop);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    currGEDCOMDir = fc.getCurrentDirectory();
                    String fName = file.getName(), fPath = file.getPath();
                    Object[] options2 = { "Include", "Don't Include" };
                    int choice1 = 1;
                    choice1 = JOptionPane.showOptionDialog(desktop, "Some names have 'flags' like <aux> embedded in them.  Include those?", "Include Names With Tags?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options2, options2[1]);
                    try {
                        PrintWriter outFile = new PrintWriter(new BufferedWriter(new FileWriter(fPath)));
                        Context.current.exportGEDCOM(outFile, fName, (String) options2[choice1]);
                        outFile.flush();
                        outFile.close();
                    } catch (IOException exc) {
                        String msg = "PROBLEM: While exporting a GEDCOM file, " + prettify(exc.toString()) + "\nRECOMMENDATION: Check for disk, directory, or permissions problems.";
                        JOptionPane.showMessageDialog(desktop, msg, "File System Error", JOptionPane.ERROR_MESSAGE);
                        activity.log.append(msg + "\n\n");
                    } catch (Exception exc) {
                        String msg = "PROBLEM: While exporting a GEDCOM file, " + prettify(exc.toString()) + "\nRECOMMENDATION: Submit a Bug Report!";
                        JOptionPane.showMessageDialog(desktop, msg, "General Error", JOptionPane.ERROR_MESSAGE);
                        activity.log.append(msg + "\n\n");
                    }
                }
            }
        } else if (e.getActionCommand().equals("export domain theory")) {
        } else if (e.getActionCommand().equals("edit prefs")) {
            if (SIL_Edit.editWindow != null) {
                SIL_Edit.editWindow.editPrefsItemActionPerformed(null);
            } else {
                String msg = "You may only edit Prefs for a Context Under Construction." + "\nThere currently is none.";
                JOptionPane.showMessageDialog(desktop, msg, "Invalid Command", JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }
        } else if (e.getActionCommand().equals("edit user context")) {
            editCUC();
        } else if (e.getActionCommand().equals("Compute Similarity Matrix")) {
            try {
                Library.generateSimMatrix();
            } catch (Exception exc) {
                String msg = "PROBLEM: During FV generation, " + prettify(exc.toString()) + "\nRECOMMENDATION: DeBug!";
                JOptionPane.showMessageDialog(desktop, msg, "Testing Error", JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }
        } else if (e.getActionCommand().equals("Cluster the Feature Vectors")) {
            int minK, maxK;
            float penalty;
            try {
                String minKStr = JOptionPane.showInputDialog(this, "Enter Smallest Number of Clusters"), maxKStr = JOptionPane.showInputDialog(this, "Enter Largest Number of Clusters"), penaltyStr = JOptionPane.showInputDialog(this, "Enter Penalty Percentage (a whole number)");
                minK = Integer.parseInt(minKStr);
                maxK = Integer.parseInt(maxKStr);
                penalty = Float.parseFloat(penaltyStr) / 100f;
                Library.clusterFVs(minK, maxK, penalty);
            } catch (Exception exc) {
                String msg = "PROBLEM: During FV clustering, " + prettify(exc.toString()) + "\nRECOMMENDATION: DeBug!";
                JOptionPane.showMessageDialog(desktop, msg, "Testing Error", JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }
        } else if (e.getActionCommand().equals("Make Curr Lang dyadsUndefined")) {
            try {
                if (Context.current != null) {
                    System.out.println("Generating dyadsUndefined for " + Context.current.languageName);
                    DomainTheory.current.dyadsUndefined = new DyadTMap();
                    Context.current.addDyads((Individual) Context.current.individualCensus.get(0));
                }
            } catch (Exception exc) {
                String msg = "PROBLEM: During Dyad generation, " + prettify(exc.toString()) + "\nRECOMMENDATION: DeBug!";
                JOptionPane.showMessageDialog(desktop, msg, "Testing Error", JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }
        } else if (e.getActionCommand().equals("Gen Indexes")) {
            try {
                String startStr = JOptionPane.showInputDialog(this, "Start with what Language number?"), endStr = JOptionPane.showInputDialog(this, "End with what Language number (inclusive)?"), tag = JOptionPane.showInputDialog(this, "Suffix for this segment");
                int start = Integer.parseInt(startStr), end = Integer.parseInt(endStr);
                if (Library.predEncodings == null) {
                    Library.predEncodings = new TreeMap();
                }
                if (Library.predDecodings == null) {
                    Library.predDecodings = new TreeMap();
                }
                Library.parseClauseCounterOn = false;
                Library.generateAllIndexes(start, end, tag);
            } catch (RuntimeException rte) {
                throw new RuntimeException(rte);
            } catch (Exception exc) {
                String msg = "PROBLEM: During Index generation, " + prettify(exc.toString()) + "\nRECOMMENDATION: DeBug!";
                JOptionPane.showMessageDialog(desktop, msg, "Testing Error", JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }
        } else if (e.getActionCommand().equals("Merge Indexes")) {
            String[] suffixes = new String[10];
            int index = 0;
            String suffix = JOptionPane.showInputDialog(this, "What was the 1st Suffix used?  (Hit 'return' to cancel.)");
            if (suffix.equals("")) {
                return;
            }
            suffixes[index++] = suffix;
            while (!suffix.equals("")) {
                suffix = JOptionPane.showInputDialog(this, "What was the next Suffix used?  (Hit 'return' to end.)");
                suffixes[index++] = suffix;
            }
            index--;
            ArrayList<Object> cbIfiles = new ArrayList<Object>(), bcbIfiles = new ArrayList<Object>(), ktFiles = new ArrayList<Object>(), cbCountFiles = new ArrayList<Object>(), predEncodeFiles = new ArrayList<Object>(), predDecodeFiles = new ArrayList<Object>();
            String cbIbase = Library.libraryDirectory + "Index Segments/ClauseIndex", bcbIbase = Library.libraryDirectory + "Index Segments/BaseCBIndex", ktBase = Library.libraryDirectory + "Index Segments/KinTermSigTree", cbCountBase = Library.libraryDirectory + "Index Segments/ClauseCounts", predEncodeBase = Library.libraryDirectory + "Index Segments/PredEncodings", predDecodeBase = Library.libraryDirectory + "Index Segments/PredDecodings";
            for (int i = 0; i < index; i++) {
                cbIfiles.add(cbIbase + suffixes[i]);
                bcbIfiles.add(bcbIbase + suffixes[i]);
                ktFiles.add(ktBase + suffixes[i]);
                cbCountFiles.add(cbCountBase + suffixes[i]);
                predEncodeFiles.add(predEncodeBase + suffixes[i]);
                predDecodeFiles.add(predDecodeBase + suffixes[i]);
            }
            try {
                if (Library.predEncodings == null) {
                    Library.readPredEncodings();
                }
                if (Library.predDecodings == null) {
                    Library.readPredDecodings();
                }
                Library.mergeIndexes(cbIfiles, bcbIfiles, ktFiles, cbCountFiles, predEncodeFiles, predDecodeFiles);
            } catch (RuntimeException rte) {
                throw new RuntimeException(rte);
            } catch (Exception exc) {
                String msg = "PROBLEM: During merger of Index files,\n" + prettify(exc.toString()) + "\nRECOMMENDATION: DeBug!";
                JOptionPane.showMessageDialog(desktop, msg, "Testing Error", JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }
            return;
        } else if (e.getActionCommand().equals("Leave 1 Instant Replay")) {
            File file = null;
            try {
                if (fc == null) {
                    fc = new JFileChooser();
                }
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fc.setDialogTitle("Choose the test (.case file) to replay");
                int returnVal = fc.showOpenDialog(desktop);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile();
                }
                if (file == null) {
                    return;
                }
                int nmbrOfRounds, maxNoise, ignorable, minDyadsPerPCStr, roundNmbr, start, where;
                BufferedReader inFile = new BufferedReader(new FileReader(file));
                String line, dtFileName = inFile.readLine();
                ObjectInputStream str = new ObjectInputStream(new FileInputStream(dtFileName));
                Learned_DT learner = (Learned_DT) str.readObject();
                DomainTheory sourceDT = (learner.languageName.contains("(Adr)") ? learner.ctxt.domTheoryAdr() : learner.ctxt.domTheoryRef());
                Context.current = learner.ctxt;
                nmbrOfRounds = Integer.parseInt(inFile.readLine());
                Library.baseCB_Wait = Integer.parseInt(inFile.readLine());
                Library.inductionWait = Integer.parseInt(inFile.readLine());
                int[] distanceLimits = new int[nmbrOfRounds + 1];
                line = inFile.readLine();
                start = 1;
                for (int i = 1; i <= nmbrOfRounds; i++) {
                    where = line.indexOf(",", start);
                    if (where == -1) {
                        where = line.length() - 1;
                    }
                    distanceLimits[i] = Integer.parseInt(line.substring(start, where));
                    start = where + 2;
                }
                int[] minHitPercents = new int[nmbrOfRounds + 1];
                line = inFile.readLine();
                start = 1;
                for (int i = 1; i <= nmbrOfRounds; i++) {
                    where = line.indexOf(",", start);
                    if (where == -1) {
                        where = line.length() - 1;
                    }
                    minHitPercents[i] = Integer.parseInt(line.substring(start, where));
                    start = where + 2;
                }
                maxNoise = Integer.parseInt(inFile.readLine());
                ignorable = Integer.parseInt(inFile.readLine());
                Context.spellingNoise = Float.parseFloat(inFile.readLine());
                Context.classNoise = Float.parseFloat(inFile.readLine());
                Oracle.spellingNoise = Float.parseFloat(inFile.readLine());
                Oracle.classNoise = Float.parseFloat(inFile.readLine());
                Library.ClusterState.wtVectChoice = Integer.parseInt(inFile.readLine());
                minDyadsPerPCStr = Integer.parseInt(inFile.readLine());
                roundNmbr = Integer.parseInt(inFile.readLine());
                float priorMinHitPercent = Library.minHitPerCent;
                Context.simulation = true;
                if (FeatureVectorObj.el_1_distn == null) {
                    Library.readDistributionsFromFile();
                }
                String fileName = Library.libraryDirectory + "Test Cases/-Logs & Charts/Test-" + ++testSerialNmbr + ".log", tabFileName = Library.libraryDirectory + "Test Cases/-Logs & Charts/Test-" + testSerialNmbr + ".txt";
                PrintWriter logFile = new PrintWriter(new BufferedWriter(new FileWriter(fileName))), tabFile = new PrintWriter(new BufferedWriter(new FileWriter(tabFileName)));
                Library.parseClauseCounterOn = false;
                leaveOneOut(learner, sourceDT, logFile, tabFile, nmbrOfRounds, distanceLimits, minHitPercents, maxNoise, ignorable, minDyadsPerPCStr, true, roundNmbr);
                logFile.flush();
                logFile.close();
                tabFile.flush();
                tabFile.close();
                Library.parseClauseCounterOn = true;
                Library.minHitPerCent = priorMinHitPercent;
                Library.contextUnderConstruction = null;
                return;
            } catch (RuntimeException rte) {
                throw rte;
            } catch (Exception exc) {
                String msg = "PROBLEM: During Leave-1-Instant-Replay,\n" + prettify(exc.toString()) + "\nRECOMMENDATION: DeBug!";
                JOptionPane.showMessageDialog(desktop, msg, "Testing Error", JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }
            return;
        } else if (e.getActionCommand().equals("Leave One Out")) {
            File file = null;
            try {
                if (fc == null) {
                    fc = new JFileChooser();
                }
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fc.setDialogTitle("Choose the theory file to parse.");
                int returnVal = fc.showOpenDialog(desktop);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile();
                }
                if (file == null) {
                    return;
                }
                String fileName = file.getAbsolutePath();
                Library.readThyFile(fileName);
            } catch (Exception exc) {
            }
        } else if (e.getActionCommand().equals("Simulate User Data")) {
            try {
                String[] langs = Library.genCtxtMenu();
                String msg = "What context to use.", langChoice = null;
                boolean goodChoice = false;
                while (!goodChoice) {
                    langChoice = JOptionPane.showInputDialog(msg);
                    if (langChoice == null || langChoice.equals("")) {
                        return;
                    }
                    for (int i = 0; i < langs.length; i++) {
                        if (langChoice.equals(langs[i])) {
                            goodChoice = true;
                        }
                    }
                    if (!goodChoice) {
                        msg = "Sorry -- " + langChoice + " does not match any context name in the Library.";
                    }
                }
                Context ctxt = (Context) Library.activeContexts.get(langChoice);
                if (ctxt == null) {
                    String fileName = Library.libraryCtxtDirectory + langChoice + ".ctxt";
                    ctxt = Library.readContextFromDisk(fileName);
                }
                if (ctxt.individualCensus.size() > 0) {
                    msg = "There are " + ctxt.individualCensus.size() + " people already present.  Delete & start fresh?";
                    int killer = JOptionPane.showConfirmDialog(null, msg);
                    if (killer == JOptionPane.YES_OPTION) {
                        ctxt.resetTo(0, 0);
                        if (ctxt.domTheoryRefExists()) {
                            ctxt.domTheoryRef().dyadsUndefined = new DyadTMap();
                        }
                        if (ctxt.domTheoryAdrExists()) {
                            ctxt.domTheoryAdr().dyadsUndefined = new DyadTMap();
                        }
                        ctxt.ktm = new KinTermMatrix();
                    }
                } else {
                    ctxt.ktm = new KinTermMatrix();
                    if (ctxt.domTheoryRefExists()) {
                        ctxt.domTheoryRef().dyadsUndefined = new DyadTMap();
                    }
                    if (ctxt.domTheoryAdrExists()) {
                        ctxt.domTheoryAdr().dyadsUndefined = new DyadTMap();
                    }
                }
                String nmbrOf = JOptionPane.showInputDialog("Number of Egos to use? (enter a number)"), badSp = JOptionPane.showInputDialog("Percent Spelling Noise? (% entered as whole number)"), badCl = JOptionPane.showInputDialog("Percent Class Name Noise? (% entered as whole number)");
                NUMBER_OF_EGOS = Integer.parseInt(nmbrOf);
                Context.spellingNoise = Integer.parseInt(badSp) / 100f;
                Context.classNoise = Integer.parseInt(badCl) / 100f;
                Context.simulation = true;
                ArrayList<Object> egoBag = null;
                if (ctxt.domTheoryRefExists()) {
                    DomainTheory dt = ctxt.domTheoryRef();
                    Learned_DT learner = new Learned_DT(dt);
                    egoBag = dt.maleAndFemaleCreatedHeThem();
                    generateSimulatedData(ctxt, dt, 9, egoBag, learner);
                }
                if (ctxt.domTheoryAdrExists()) {
                    DomainTheory dt = ctxt.domTheoryAdr();
                    Learned_DT learner = new Learned_DT(dt);
                    if (egoBag == null) {
                        egoBag = dt.maleAndFemaleCreatedHeThem();
                    }
                    generateSimulatedData(ctxt, dt, 9, egoBag, learner);
                }
                if (!ctxt.domTheoryRefExists() && !ctxt.domTheoryAdrExists()) {
                    JOptionPane.showMessageDialog(null, "Could not simulate data -- there are no domain theories in this context.", "Serious Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    Library.contextUnderConstruction = ctxt;
                }
            } catch (RuntimeException rte) {
                throw new RuntimeException(rte);
            } catch (Exception exc) {
                String msg = "PROBLEM: During User Simulation,\n" + prettify(exc.toString()) + "\nRECOMMENDATION: DeBug!";
                JOptionPane.showMessageDialog(desktop, msg, "Testing Error", JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }
            return;
        } else if (e.getActionCommand().equals("Gen CUC Population")) {
            try {
                Context cuc = Library.contextUnderConstruction;
                Context.current = cuc;
                DomainTheory.current = cuc.domTheoryRef();
                Individual indie;
                String gender;
                for (int j = 0; j < 20; j++) {
                    if (j % 2 == 0) {
                        gender = "F";
                    } else {
                        gender = "M";
                    }
                    indie = new Individual("Person#" + j, gender);
                }
            } catch (Exception exc) {
                String msg = "PROBLEM: During generation of a fake population, " + prettify(exc.toString()) + "\nRECOMMENDATION: DeBug!";
                JOptionPane.showMessageDialog(desktop, msg, "Testing Error", JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }
        }
    }

    public void browseCurrentContext() {
        try {
            Library.saveUserContext();
        } catch (Exception exc) {
            String msg = "PROBLEM: File error during attempt to save your current context:\n" + prettify(exc.toString()) + "\nSend the error log to your support person.";
            JOptionPane.showMessageDialog(desktop, msg, "File Error", JOptionPane.ERROR_MESSAGE);
            activity.log.append(msg + "\n\n");
            return;
        }
        String lang = Context.current.languageName;
        Library.ContextStub newStub = Library.retrieveOrCreateStub(lang);
        newStub.refThyExists = Context.current.domTheoryRefExists();
        newStub.adrThyExists = Context.current.domTheoryAdrExists();
        Library.currentLanguage = lang;
        browseLibrary();
    }

    public void browseLibrary() {
        currentFrame = new LibBrowser();
        currentFrame.desktop = desktop;
        currentFrame.setLocation(25 * numWindows, 25 * numWindows++);
        desktop.add(currentFrame);
        currentFrame.show();
        currentFrame.moveToFront();
        try {
            currentFrame.setSelected(true);
        } catch (PropertyVetoException pv) {
        }
        currentFrame.miViewMe = menuView.add(currentFrame.windowNum);
        currentFrame.miViewMe.addActionListener(currentFrame);
        currentFrame.menuView = menuView;
    }

    public void editCUC() {
        if (Library.contextUnderConstruction == null) {
            try {
                Library.chooseUserContext(currentFrame);
            } catch (Exception ex) {
                String msg = "While editing a context, " + ex;
                activity.log.append(msg + "\n\n");
            }
        }
        if (curr_CUC_Editor != null) {
            currentFrame = curr_CUC_Editor;
            currentFrame.moveToFront();
            try {
                currentFrame.setSelected(true);
            } catch (PropertyVetoException pv) {
            }
        } else {
            try {
                currentFrame = Library.contextUnderConstruction.editFields();
                curr_CUC_Editor = (ContextEditor) currentFrame;
                currentFrame.desktop = desktop;
                currentFrame.setLocation(25 * numWindows, 25 * numWindows++);
                desktop.add(currentFrame);
                currentFrame.show();
                currentFrame.moveToFront();
                try {
                    currentFrame.setSelected(true);
                } catch (PropertyVetoException pv) {
                }
                currentFrame.miViewMe = menuView.add(currentFrame.windowNum);
                currentFrame.miViewMe.addActionListener(currentFrame);
                currentFrame.menuView = menuView;
            } catch (Exception ex) {
                activity.log.append("While editing a context, " + ex + "\n\n");
            }
        }
        changeActivity(Library.DATA_GATHERING);
    }

    public void changeActivity(int activityCode) {
        Library.currentActivity = activityCode;
    }

    public static void paws() {
        JOptionPane.showMessageDialog(desktop, "Hit 'OK' to resume processing.", "Programmed Pause", JOptionPane.PLAIN_MESSAGE);
    }

    public void findEmptyExpansions(DomainTheory dt) throws KSInternalErrorException {
        Iterator ktdIter = dt.theory.values().iterator();
        while (ktdIter.hasNext()) {
            KinTermDef ktd = (KinTermDef) ktdIter.next();
            int priorBaseCB_Nmbr = -1, thisBaseCB_Nmbr = 0;
            TreeMap seqNmbs = new TreeMap();
            for (int i = 0; i < ktd.expandedDefs.size(); i++) {
                ClauseBody cb = (ClauseBody) ktd.expandedDefs.get(i);
                String firstExpansion = (String) cb.expansionPath.get(0);
                int colon = firstExpansion.indexOf(":");
                try {
                    thisBaseCB_Nmbr = Integer.parseInt(firstExpansion.substring(colon + 1).trim());
                } catch (RuntimeException rte) {
                    System.out.println("Hit Bad Expansion Number: " + rte);
                }
                if (thisBaseCB_Nmbr != priorBaseCB_Nmbr) {
                    if (seqNmbs.get(thisBaseCB_Nmbr) == null) {
                        seqNmbs.put(thisBaseCB_Nmbr, firstExpansion);
                    }
                    priorBaseCB_Nmbr = thisBaseCB_Nmbr;
                }
            }
            priorBaseCB_Nmbr = -1;
            thisBaseCB_Nmbr = 0;
            Iterator seqIter = seqNmbs.keySet().iterator();
            while (seqIter.hasNext()) {
                thisBaseCB_Nmbr = ((Integer) seqIter.next()).intValue();
                if ((thisBaseCB_Nmbr - priorBaseCB_Nmbr) > 1) {
                    for (int i = priorBaseCB_Nmbr + 1; i < thisBaseCB_Nmbr; i++) {
                        System.out.println("\tNo expansions of " + ktd.kinTerm + ":" + i);
                    }
                }
                priorBaseCB_Nmbr = thisBaseCB_Nmbr;
            }
        }
    }

    String prettify(String ugly) {
        int start = 0, stop = 100, length = ugly.length(), period, comma, leftSquare, rightSquare, rightSquareComma, max;
        String lessUgly = null;
        while (stop < length) {
            period = ugly.substring(start, stop - 10).lastIndexOf(".") + 1 + start;
            comma = ugly.substring(start, stop - 10).lastIndexOf(",") + 1 + start;
            rightSquare = ugly.substring(start, stop).lastIndexOf("]") + 1 + start;
            rightSquareComma = ugly.substring(start, stop).lastIndexOf("],") + 2 + start;
            leftSquare = ugly.substring(start, stop).lastIndexOf("[") + start;
            max = Math.max(comma, period);
            max = Math.max(max, leftSquare);
            max = Math.max(max, rightSquare);
            max = Math.max(max, rightSquareComma);
            if (max > (start + 60)) {
                stop = max;
            }
            lessUgly += ugly.substring(start, stop) + "\n";
            start = stop;
            stop += 100;
        }
        lessUgly += ugly.substring(start);
        return lessUgly;
    }

    public void doLv1ActiveLearning(Learned_DT learner, int maxNoise, int ignorable, int maxConf, int minDyadsPerPCStr, DomainTheory sourceDT) throws KSInternalErrorException, KSParsingErrorException, KSBadHornClauseException, KSConstraintInconsistency, ClassNotFoundException, JavaSystemException, FileNotFoundException, IOException {
        if (learner == null) {
            return;
        }
        if (Library.predEncodings == null) {
            Library.readPredEncodings();
        }
        if (Library.predDecodings == null) {
            Library.readPredDecodings();
        }
        learner.ctxt.simDataGen = true;
        Context.current = learner.ctxt;
        DomainTheory.current = learner;
        if (Library.synUmbDetectOn) {
            learner.resolveSynonymsInDyads();
        }
        boolean nonTermsFound = learner.dyadsUndefined.containsKey("no__term");
        Iterator dyadIter = learner.theory.keySet().iterator();
        while (dyadIter.hasNext()) {
            String kterm = (String) dyadIter.next();
            if (nonTermsFound || learner.dyadsUndefined.containsKey(kterm)) {
                if (Library.errorCorrectionOn) {
                    ArrayList<Object> validationIssues = learner.validateNewDyads(kterm, maxNoise);
                    learner.postAnomaliesForUser(kterm, validationIssues);
                }
            }
        }
        dyadIter = learner.dyadsUndefined.keySet().iterator();
        while (dyadIter.hasNext()) {
            String kterm = (String) dyadIter.next();
            if (learner.dyadsUndefined.avgDyadsPerPCStr(kterm) >= minDyadsPerPCStr) {
                if (DomainTheory.LGG_ONLY) {
                    learner.learnKinTermLGG(kterm, maxNoise, ignorable);
                } else {
                    learner.learnKinTerm(kterm, maxNoise, ignorable, maxConf, sourceDT);
                }
            }
        }
        Library.CB_Ptr.clearCache();
        Library.KTD_Ptr.clearCacheDTs();
        System.gc();
        if (learner.dyadsDefined != null) {
            dyadIter = learner.dyadsDefined.entrySet().iterator();
            while (dyadIter.hasNext()) {
                Map.Entry entry = (Map.Entry) dyadIter.next();
                String kterm = (String) entry.getKey();
                TreeMap pos = (TreeMap) entry.getValue(), neg = learner.makeNEG(kterm, pos);
                if (Library.synUmbDetectOn) {
                    learner.detectSynonymsAndUmbrellas(kterm, pos, neg, maxNoise);
                }
            }
        }
        if (learner.potUmbrellas != null) {
            learner.proposeUmbrellas();
        }
        if (Library.synUmbDetectOn) {
            learner.analyzeSynonymsAndUmbrellas();
            learner.validateSynonymsAndUmbrellas();
        }
        learner.ctxt.simDataGen = false;
    }

    public void leaveOneOut(File sourceFile, PrintWriter logFile, PrintWriter tabFile, int nmbrOfRounds, int[] distanceLimits, int[] minHitPercents, int maxNoise, int ignorable, int minDyadsPerPCStr) throws KSParsingErrorException, KSConstraintInconsistency, KSBadHornClauseException, FileNotFoundException, IOException, KSInternalErrorException, KinshipSystemException, ClassNotFoundException, JavaSystemException {
        Parser parzer = new Parser(new Tokenizer(Library.getDFA(), new Linus(sourceFile)));
        DomainTheory sourceDT = parzer.parseDomainTheory();
        Context sourceCtxt = new Context(sourceDT);
        Iterator ktdIter = sourceDT.theory.values().iterator();
        while (ktdIter.hasNext()) {
            KinTermDef ktd = (KinTermDef) ktdIter.next();
            for (int j = 0; j < ktd.expandedDefs.size(); j++) {
                ((ClauseBody) ktd.expandedDefs.get(j)).unifyVariables();
            }
        }
        Learned_DT learner = new Learned_DT(sourceDT);
        Linus macroLineServer = new Linus(Library.libraryDirectory + "Standard_Macros");
        Tokenizer tok = new Tokenizer(Library.getDFA(), macroLineServer);
        parzer = new Parser(tok, tok);
        parzer.parseStandardMacros(learner);
        leaveOneOut(learner, sourceDT, logFile, tabFile, nmbrOfRounds, distanceLimits, minHitPercents, maxNoise, ignorable, minDyadsPerPCStr, false, 1);
    }

    public void leaveOneOut(Learned_DT learner, DomainTheory sourceDT, PrintWriter logFile, PrintWriter tabFile, int nmbrOfRounds, int[] distanceLimits, int[] minHitPercents, int maxNoise, int ignorable, int minDyadsPerPCStr, boolean replay, int roundNmbr) throws KSParsingErrorException, KSConstraintInconsistency, KSBadHornClauseException, FileNotFoundException, IOException, KSInternalErrorException, KinshipSystemException, ClassNotFoundException, JavaSystemException {
        Library.contextUnderConstruction = learner.ctxt;
        if (FeatureVectorObj.el_1_distn == null) {
            Library.readDistributionsFromFile();
        }
        learner.ctxt.featuresHaveChanged = true;
        int testSerial = testSerialNmbr, noiseCntr = 0;
        System.out.println("*************Beginning Leave-1-Out for " + learner.languageName + "-" + testSerial);
        boolean done = false;
        while (roundNmbr <= nmbrOfRounds && !done) {
            if (learner.ctxt.featuresHaveChanged) {
                FeatureVectorObj fv = learner.computeFeatureVector(new ArrayList<Object>());
                String filePath = Library.libraryDirectory + "Feature Vectors/" + fv.languageName + ".fvect";
                fv.toDisk(new PrintWriter(new BufferedWriter(new FileWriter(filePath))));
                learner.ctxt.featuresHaveChanged = false;
            }
            System.out.println("*** Round " + roundNmbr + ".0");
            Library.minHitPerCent = minHitPercents[roundNmbr] / 100f;
            if (!replay) {
                ArrayList<Object> egoBag = sourceDT.maleAndFemaleCreatedHeThem();
                generateSimulatedData(sourceDT.ctxt, sourceDT, distanceLimits[roundNmbr], egoBag, learner);
                learner.dyadsUndefined.assimlate(sourceDT.dyadsUndefined);
            } else {
                replay = false;
            }
            System.out.println(learner.dyadsUndefined.summaryString());
            noiseCntr = learner.postNoiseCounts(roundNmbr, noiseCntr);
            if (roundNmbr > Library.baseCB_Wait) {
                Learned_DT.doBaseCBs = true;
            }
            if (roundNmbr > Library.inductionWait) {
                Learned_DT.doInduction = true;
            }
            int maxConf = ignorable / 2;
            doLv1ActiveLearning(learner, maxNoise, ignorable, maxConf, minDyadsPerPCStr, sourceDT);
            learner.printSuggestions("" + roundNmbr + ".0");
            learner.processSuggestions(roundNmbr, 0, sourceDT, maxNoise, ignorable, minDyadsPerPCStr);
            done = learner.allDone(minDyadsPerPCStr, sourceDT);
            learner.dumpRemNoise();
            ArrayList<Object> learnRec = new ArrayList<Object>();
            learnRec.add("Total Dyads thru Round ");
            learnRec.add(roundNmbr);
            learnRec.add(" = ");
            Integer dyTotal = new Integer(learner.dyadsDefined.total() + learner.dyadsUndefined.total());
            learnRec.add(dyTotal);
            learner.postLrnHist("" + roundNmbr, learnRec);
            roundNmbr++;
        }
        learner.writeTestResults(logFile, tabFile, sourceDT);
        System.out.println("\n\n\t\tDefinitions Learned");
        Iterator iter = learner.theory.keySet().iterator();
        while (iter.hasNext()) {
            String term = (String) iter.next();
            if (!learner.nonTerms.contains(term) && term.indexOf("aux>") == -1) {
                System.out.println(term);
            }
        }
    }

    public void cleanRAM(Learned_DT learner) throws KSParsingErrorException, JavaSystemException, KSBadHornClauseException, KSInternalErrorException, KSConstraintInconsistency {
        Library.CB_Ptr.clearCache();
        Library.KTD_Ptr.clearCacheDTs();
        Iterator iter = Library.activeContexts.values().iterator();
        while (iter.hasNext()) {
            Context ctxt = (Context) iter.next();
            if (ctxt != learner.ctxt) {
                if (ctxt.domTheoryRefLoaded()) {
                    ctxt.domTheoryRef().theory.clear();
                }
                if (ctxt.domTheoryAdrLoaded()) {
                    ctxt.domTheoryAdr().theory.clear();
                }
            }
        }
        System.gc();
    }

    public void makeTestCaseFile(Learned_DT learner, int nmbrOfRounds, int[] distanceLimits, int[] minHitPercents, int maxNoise, int ignorable, int minDyadsPerPCStr, int testSerial, int roundNmbr) throws NotSerializableException, IOException, FileNotFoundException {
        int ender = learner.languageName.lastIndexOf("test");
        String label = learner.languageName.substring(0, ender) + testSerial + "-" + roundNmbr, dtFileName = Library.libraryDirectory + "Test Cases/" + label + ".ldt";
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dtFileName));
        out.writeObject(learner);
        out.flush();
        out.close();
        System.out.println("Wrote " + dtFileName);
        String caseFileName = Library.libraryDirectory + "Test Cases/" + label + ".case";
        PrintWriter outFile = new PrintWriter(new BufferedWriter(new FileWriter(caseFileName)));
        outFile.println(dtFileName);
        outFile.println(nmbrOfRounds);
        outFile.println(Library.baseCB_Wait);
        outFile.println(Library.inductionWait);
        outFile.print("[");
        for (int i = 1; i <= nmbrOfRounds; i++) {
            outFile.print(distanceLimits[i]);
            if (i < nmbrOfRounds) {
                outFile.print(", ");
            }
        }
        outFile.println("]");
        outFile.print("[");
        for (int i = 1; i <= nmbrOfRounds; i++) {
            outFile.print(minHitPercents[i]);
            if (i < nmbrOfRounds) {
                outFile.print(", ");
            }
        }
        outFile.println("]");
        outFile.println(maxNoise);
        outFile.println(ignorable);
        outFile.println(Context.spellingNoise);
        outFile.println(Context.classNoise);
        outFile.println(Oracle.spellingNoise);
        outFile.println(Oracle.classNoise);
        outFile.println(Library.ClusterState.wtVectChoice);
        outFile.println(minDyadsPerPCStr);
        outFile.println(roundNmbr);
        outFile.flush();
        outFile.close();
        System.out.println("Wrote " + caseFileName);
    }

    public void generateSimulatedData(Context ctxt, DomainTheory dt, int distLimit, ArrayList<Object> originalEgoBag, Learned_DT learner) throws KSBadHornClauseException, KSInternalErrorException, KinshipSystemException, ClassNotFoundException {
        Oracle orca = learner.wiseGuy;
        Context.current = ctxt;
        ctxt.simDataGen = true;
        boolean oldSimVal = Context.simulation;
        Context.simulation = true;
        ClauseBody.distanceLimit = distLimit;
        DomainTheory.addrTerms = dt.addressTerms;
        DomainTheory.current = dt;
        ArrayList<Object> egoBag = new ArrayList<Object>(originalEgoBag);
        Individual ego = (Individual) egoBag.remove(0), potUsedEgo;
        dt.dyadsUndefined = new DyadTMap();
        KinTermDef ktd;
        for (int i = 0; i < NUMBER_OF_EGOS; i++) {
            ctxt.changeEgoTo(ego);
            egoBag.add(0, ego);
            ArrayList<Object> round2 = new ArrayList<Object>();
            Iterator iter = dt.theory.values().iterator();
            while (iter.hasNext()) {
                ktd = (KinTermDef) iter.next();
                if (ktd.hasDelayFlags()) {
                    round2.add(ktd);
                } else if (dt.printableBaseTerm(ktd)) {
                    ktd.generateExamples(ctxt, egoBag, orca, (KinTermDef) learner.theory.get(ktd.kinTerm));
                }
            }
            fill_In_Flag = true;
            for (int j = 0; j < round2.size(); j++) {
                ktd = (KinTermDef) round2.get(j);
                if (dt.printableBaseTerm(ktd)) {
                    ktd.generateExamples(ctxt, egoBag, orca, (KinTermDef) learner.theory.get(ktd.kinTerm));
                }
            }
            fill_In_Flag = false;
            ego = randomEgo(ctxt, egoBag, ego.gender);
            Iterator egoIter = egoBag.iterator();
            while (egoIter.hasNext()) {
                potUsedEgo = (Individual) egoIter.next();
                if (potUsedEgo.birthFamily != null || potUsedEgo.marriages.size() > 0) {
                    ctxt.currentEgo = potUsedEgo;
                    ctxt.addDyads(potUsedEgo);
                    egoIter.remove();
                }
            }
        }
        ctxt.changeEgoTo(null);
        ctxt.simDataGen = false;
        Context.simulation = oldSimVal;
    }

    public Individual randomEgo(Context ctxt, ArrayList<Object> priorEgos, String priorSex) throws KinshipSystemException {
        if (priorSex.equals("?")) {
            priorSex = "M";
        }
        int which = (int) Math.floor((Math.random() * ctxt.individualCensus.size())), reps = 0;
        Individual newEgo = (Individual) ctxt.individualCensus.get(which);
        while (priorEgos.contains(newEgo) || !newEgo.gender.equals(priorSex)) {
            which = (int) Math.floor((Math.random() * ctxt.individualCensus.size()));
            newEgo = (Individual) ctxt.individualCensus.get(which);
            if (reps++ > 100) {
                throw new KinshipSystemException("No new Egos available in Simulation");
            }
        }
        priorEgos.add(0, newEgo);
        reps = 0;
        which = (int) Math.floor((Math.random() * ctxt.individualCensus.size()));
        newEgo = (Individual) ctxt.individualCensus.get(which);
        while (priorEgos.contains(newEgo) || newEgo.gender.equals(priorSex)) {
            which = (int) Math.floor((Math.random() * ctxt.individualCensus.size()));
            newEgo = (Individual) ctxt.individualCensus.get(which);
            if (reps++ > 100) {
                throw new KinshipSystemException("No new Egos available in Simulation");
            }
        }
        return newEgo;
    }

    public TreeMap makeTempSizes(DomainTheory tempdt) {
        TreeMap result = new TreeMap();
        Iterator iter = tempdt.theory.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String kterm = (String) entry.getKey();
            KinTermDef ktd = (KinTermDef) entry.getValue();
            Integer siz = new Integer(ktd.expandedDefs.size());
            result.put(kterm, siz);
        }
        return result;
    }

    public void scanForNeuters(KinTermDef ktd) {
        for (int i = 0; i < ktd.expandedDefs.size(); i++) {
            ClauseBody cb = (ClauseBody) ktd.expandedDefs.get(i);
            ClauseBody.StringObj details = new ClauseBody.StringObj("");
            if (ktd.domTh.printableTerm(ktd) && neutersIn(cb, details)) {
                System.out.println("\t" + ktd.kinTerm + ":" + cb.seqNmbr + " -- " + details);
            }
        }
    }

    public boolean neutersIn(ClauseBody cb, ClauseBody.StringObj details) {
        TreeMap genderConstraints = new TreeMap(), genderVars = new TreeMap();
        ArrayList<Object> opposites = new ArrayList<Object>();
        postConstraints(cb.body, genderConstraints, genderVars, opposites);
        boolean changesMade = true;
        Iterator iter;
        while (changesMade) {
            boolean allOK = true;
            iter = genderConstraints.entrySet().iterator();
            while (allOK && iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                ClauseBody.StringObj gender = (ClauseBody.StringObj) entry.getValue();
                String person = (String) entry.getKey();
                if (gender.value.equals("") && !person.equals("Ego")) {
                    allOK = false;
                }
            }
            if (allOK) {
                return false;
            }
            changesMade = false;
            iter = genderConstraints.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                ClauseBody.StringObj gender = (ClauseBody.StringObj) entry.getValue();
                String person = (String) entry.getKey();
                if (gender.value.equals("") && !person.equals("Ego")) {
                    ArrayList<Object> oppPers = getOppo(person, opposites);
                    if (!oppPers.isEmpty()) {
                        for (int i = 0; i < oppPers.size(); i++) {
                            String oppPer = (String) oppPers.get(i);
                            String oppSexGndr = ((ClauseBody.StringObj) genderConstraints.get(oppPer)).value;
                            if (oppSexGndr.equals("M")) {
                                gender.value = "F";
                            } else if (oppSexGndr.equals("F")) {
                                gender.value = "M";
                            } else if (oppSexGndr.equals("") && oppPer.equals("Ego")) {
                                gender.value = "oppEgo";
                            } else if (oppSexGndr.equals("oppEgo")) {
                                gender.value = "samEgo";
                            } else if (oppSexGndr.equals("samEgo")) {
                                gender.value = "oppEgo";
                            }
                            if (!gender.value.equals("")) {
                                changesMade = true;
                            }
                        }
                    }
                    if (gender.value.equals("") && genderVars.get(person) != null) {
                        String genVar = (String) genderVars.get(person);
                        ArrayList<Object> grp = (ArrayList<Object>) genderVars.get(genVar);
                        if (grp != null) {
                            for (int i = 0; i < grp.size(); i++) {
                                String coSex = (String) grp.get(i), coSexGndr = ((ClauseBody.StringObj) genderConstraints.get(coSex)).value;
                                if (!coSexGndr.equals("")) {
                                    gender.value = coSexGndr;
                                    changesMade = true;
                                    i = grp.size();
                                } else if (coSex.equals("Ego")) {
                                    gender.value = "samEgo";
                                    changesMade = true;
                                    i = grp.size();
                                }
                            }
                        }
                        ArrayList<Object> oppoVars = getOppo(genVar, opposites);
                        if (gender.value.equals("") && !oppoVars.isEmpty()) {
                            for (int i = 0; i < oppoVars.size(); i++) {
                                String oppoVar = (String) oppoVars.get(i);
                                grp = (ArrayList<Object>) genderVars.get(oppoVar);
                                if (grp != null) {
                                    for (int j = 0; j < grp.size(); j++) {
                                        String oppSex = (String) grp.get(j), oppSexGndr = ((ClauseBody.StringObj) genderConstraints.get(oppSex)).value;
                                        if (oppSexGndr.equals("M")) {
                                            gender.value = "F";
                                        } else if (oppSexGndr.equals("F")) {
                                            gender.value = "M";
                                        } else if (oppSexGndr.equals("") && oppSex.equals("Ego")) {
                                            gender.value = "oppEgo";
                                        } else if (oppSexGndr.equals("oppEgo")) {
                                            gender.value = "samEgo";
                                        } else if (oppSexGndr.equals("samEgo")) {
                                            gender.value = "oppEgo";
                                        }
                                        if (!gender.value.equals("")) {
                                            changesMade = true;
                                            j = grp.size();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        iter = genderConstraints.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String gender = ((ClauseBody.StringObj) entry.getValue()).value, person = (String) entry.getKey();
            if (gender.equals("") && !person.equals("Ego")) {
                details.value += person + ", ";
            }
        }
        details.value = details.value.substring(0, details.value.lastIndexOf(", "));
        return true;
    }

    public ArrayList<Object> getOppo(String key, ArrayList<Object> pairs) {
        ArrayList<Object> result = new ArrayList<Object>();
        for (int i = 0; i < pairs.size(); i++) {
            ArrayList<Object> pair = (ArrayList<Object>) pairs.get(i);
            if (key.equals((String) pair.get(0))) {
                result.add(pair.get(1));
            }
            if (key.equals((String) pair.get(1))) {
                result.add(pair.get(0));
            }
        }
        return result;
    }

    public void postConstraints(ArrayList<Object> body, TreeMap genderConstraints, TreeMap genderVars, ArrayList<Object> opposites) {
        ArrayList<Object> unequals = new ArrayList<Object>(), genXers = new ArrayList<Object>();
        TreeMap parentsOf = new TreeMap();
        for (int i = 0; i < body.size(); i++) {
            Literal lit = (Literal) body.get(i);
            String predName = lit.predicate.name;
            ClauseBody.StringObj arg0gender = null, arg1gender = null;
            if (predName.equals("not")) {
                Literal subLit = (Literal) lit.args.get(0);
                if (subLit.predicate.name.equals("equal") && lit.args.size() == 1) {
                    ArrayList<Object> pair = new ArrayList<Object>();
                    pair.add(((Argument) subLit.args.get(0)).argName);
                    pair.add(((Argument) subLit.args.get(1)).argName);
                    unequals.add(pair);
                }
            } else if (predName.equals("lessThan") || predName.equals("greaterThan") || predName.equals("lessOrEql") || predName.equals("greaterOrEql") || predName.equals("contains") || predName.equals("allowCreation") || predName.substring(0, 1).equals("*")) {
            } else {
                Argument arg0 = (Argument) lit.args.get(0), arg1 = null;
                if (genderConstraints.get(arg0.argName) == null) {
                    arg0gender = new ClauseBody.StringObj("");
                    genderConstraints.put(arg0.argName, arg0gender);
                } else {
                    arg0gender = (ClauseBody.StringObj) genderConstraints.get(arg0.argName);
                }
                if (lit.args.size() > 1) {
                    arg1 = (Argument) lit.args.get(1);
                    if (genderConstraints.get(arg1.argName) == null) {
                        arg1gender = new ClauseBody.StringObj("");
                        genderConstraints.put(arg1.argName, arg1gender);
                    } else {
                        arg1gender = (ClauseBody.StringObj) genderConstraints.get(arg1.argName);
                    }
                }
                if (predName.equals("father") || predName.equals("son") || predName.equals("male")) {
                    arg0gender.value = "M";
                } else if (predName.equals("mother") || predName.equals("daughter") || predName.equals("female")) {
                    arg0gender.value = "F";
                } else if (predName.equals("husband")) {
                    arg0gender.value = "M";
                    arg1gender.value = "F";
                } else if (predName.equals("wife")) {
                    arg0gender.value = "F";
                    arg1gender.value = "M";
                } else if (predName.equals("spouse")) {
                    ArrayList<Object> pair = new ArrayList<Object>();
                    pair.add(arg0.argName);
                    pair.add(arg1.argName);
                    opposites.add(pair);
                } else if (predName.equals("gender")) {
                    genderVars.put(arg1.argName, arg0.argName);
                    genderConstraints.remove(arg0.argName);
                    genXers.add(arg0.argName);
                    if (genderVars.get(arg0.argName) == null) {
                        genderVars.put(arg0.argName, new ArrayList<Object>());
                    }
                    ArrayList<Object> people = (ArrayList<Object>) genderVars.get(arg0.argName);
                    people.add(arg1.argName);
                }
                if (predName.equals("father") || predName.equals("son") || predName.equals("mother") || predName.equals("daughter") || predName.equals("parent") || predName.equals("child")) {
                    boolean parental = (predName.equals("father") || predName.equals("parent") || predName.equals("mother"));
                    String parArgName = (parental ? arg0.argName : arg1.argName), kidArgName = (!parental ? arg0.argName : arg1.argName), otherParent = (String) parentsOf.get(kidArgName);
                    if (otherParent == null) {
                        parentsOf.put(kidArgName, parArgName);
                    } else if (!otherParent.equals(parArgName)) {
                        ArrayList<Object> pair = new ArrayList<Object>();
                        pair.add(parArgName);
                        pair.add(otherParent);
                        opposites.add(pair);
                    }
                }
            }
        }
        for (int j = 0; j < unequals.size(); j++) {
            ArrayList<Object> pair = (ArrayList<Object>) unequals.get(j);
            if (genXers.containsAll(pair)) {
                opposites.add(pair);
            }
        }
    }

    public static void createActivityLog(JDesktopPane desktop, JMenu menuView) {
        activity = new KSJInternalFrame("Error Log");
        activity.desktop = desktop;
        activity.windowNum = "Error Log";
        activity.miViewMe = menuView.add(activity.windowNum);
        activity.miViewMe.addActionListener(activity);
        activity.menuView = menuView;
        JTextArea log = new JTextArea(45, 300);
        activity.log = log;
        String logDate = UDate.today();
        log.append("########### Log of activity, errors, and warnings encountered on " + logDate + ".\n");
        log.append("########### Version 1.0, build # " + buildNmbr + ".\n");
        JScrollPane logroll = new JScrollPane(log);
        logroll.setMinimumSize(new Dimension(250, 45));
        logroll.setMaximumSize(new Dimension(850, 500));
        logroll.setPreferredSize(new Dimension(250, 45));
        activity.getContentPane().add(logroll);
        activity.setDefaultCloseOperation(HIDE_ON_CLOSE);
        desktop.add(activity);
        activity.setSize(900, 235);
        activity.setVisible(true);
        activity.moveToBack();
    }

    public void writeActivityLog() {
        if (activity == null) {
            return;
        }
        String fileName = Library.libraryDirectory + "ActivityLog";
        try {
            PrintWriter outFile = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
            outFile.println(activity.log.getText());
            outFile.flush();
            outFile.close();
            System.out.println("Wrote: " + fileName);
        } catch (IOException e) {
            System.out.println("Activity Log write to disk failed:\n" + e + "\n\n");
        }
    }

    public static class ContextFilter extends javax.swing.filechooser.FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String extension = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');
            if (i > 0 && i < s.length() - 1) {
                extension = s.substring(i + 1).toLowerCase();
            }
            if ((extension != null) && (extension.equals("ctxt"))) {
                return true;
            } else {
                return false;
            }
        }

        public String getDescription() {
            return "Only Context files";
        }
    }

    /** 
    The inner class CleanUpThread is used to tidy up and save state before the Kinship System exits.
    It first checks all active contexts to see if any need to be saved to disk, & saves them.  It then
    writes out the Library's StubFile which serves as a directory to all files available in the Library.
     */
    public class CleanUpThread extends Thread {

        public void run() {
            if (SIL_Edit.editWindow.chart.saveFile != null) {
                try {
                    SIL_Edit.editWindow.chart.saveSILKinFile();
                    System.out.println("Wrote: " + SIL_Edit.editWindow.chart.saveFile);
                } catch (Exception ex) {
                    System.out.println("Hit File-Save exception in clean-up thread.");
                }
            }
            try {
                Library.writeStubFile();
                writeActivityLog();
            } catch (Exception goof) {
                System.out.println("Error writing Library.stubs file or Error Log: " + goof + "\n\n");
            }
        }
    }
}

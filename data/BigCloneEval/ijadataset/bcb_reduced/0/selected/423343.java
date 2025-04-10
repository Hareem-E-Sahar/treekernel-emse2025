package plugin.notes.gui;

import gmgen.GMGenSystem;
import gmgen.GMGenSystemView;
import gmgen.gui.ExtendedHTMLDocument;
import gmgen.gui.ExtendedHTMLEditorKit;
import gmgen.gui.ImageFileChooser;
import gmgen.io.SimpleFileFilter;
import gmgen.util.LogReceiver;
import gmgen.util.LogUtilities;
import gmgen.util.MiscUtilities;
import pcgen.core.SettingsHandler;
import pcgen.gui.panes.FlippingSplitPane;
import pcgen.util.Logging;
import plugin.notes.NotesPlugin;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *  This class is the main view for the Notes Plugin. Mostof the work is done
 *  here and in the NotesTreeNode Class.
 *
 * @author     soulcatcher
 * @since    August 27, 2003
 */
public class NotesView extends JPanel {

    /**  Drop Target for the Edit Area */
    protected DropTarget editAreaDT;

    /**  Drop Target for the File Bar */
    protected DropTarget filesBarDT;

    /**  Drop Target for the Tree */
    protected DropTarget treeDT;

    /**  Insert OL Action for JTextPane */
    protected ExtendedHTMLEditorKit.InsertListAction actionListOrdered = new ExtendedHTMLEditorKit.InsertListAction("InsertOLItem", HTML.Tag.OL);

    /**  Insert UL Action for JTextPane */
    protected ExtendedHTMLEditorKit.InsertListAction actionListUnordered = new ExtendedHTMLEditorKit.InsertListAction("InsertULItem", HTML.Tag.UL);

    protected NotesPlugin plugin;

    /**  Root node of tree */
    protected NotesTreeNode root;

    /**  Redo Action for JTextPane */
    protected RedoAction redoAction = new RedoAction();

    /**  Data Directory */
    protected String dataDir;

    /**  Undo Action for JTextPane */
    protected UndoAction undoAction = new UndoAction();

    /**  Undo Manager */
    protected UndoManager undo = new UndoManager();

    protected final String[] extsIMG = { "gif", "jpg", "jpeg", "png" };

    private JButton boldButton;

    private JButton bulletButton;

    private JButton centerJustifyButton;

    private JButton colorButton;

    private JButton copyButton;

    private JButton cutButton;

    private JButton deleteButton;

    private JButton enumButton;

    private JButton exportButton;

    private JButton fileLeft;

    private JButton fileRight;

    private JButton imageButton;

    private JButton italicButton;

    private JButton leftJustifyButton;

    private JButton newButton;

    private JButton pasteButton;

    private JButton revertButton;

    private JButton rightJustifyButton;

    private JButton saveButton;

    private JButton underlineButton;

    private JComboBox sizeCB;

    private JPanel filePane;

    private JPanel jPanel1;

    private JPanel jPanel2;

    private JScrollPane jScrollPane1;

    private JScrollPane jScrollPane2;

    private FlippingSplitPane jSplitPane1;

    private JTextPane editor;

    private JToolBar alignmentBar;

    private JToolBar clipboardBar;

    private JToolBar fileBar;

    private JToolBar filesBar;

    private JToolBar formatBar;

    private JTree notesTree;

    /**
	 *  Creates new form NotesView
	 *
	 *@param  dataDir  Data directory where notes will be stored.
	 * @param plugin
	 */
    public NotesView(String dataDir, NotesPlugin plugin) {
        this.plugin = plugin;
        this.dataDir = dataDir;
        initComponents();
        initEditingComponents();
        initDnDComponents();
        initTree();
        initFileBar(new ArrayList());
        initLogging();
    }

    /**
	 *  Searches a text component for a particular action.
	 *
	 *@param  textComponent  Text component to search for the action in
	 *@param  name           name of the action to get
	 *@return                the action
	 */
    public Action getActionByName(JTextComponent textComponent, String name) {
        Action[] actionsArray = textComponent.getActions();
        for (int i = 0; i < actionsArray.length; i++) {
            Action a = actionsArray[i];
            if (a.getValue(Action.NAME).equals(name)) {
                return a;
            }
        }
        return null;
    }

    /**
	 *  handle File->Open. Will open any .gmn files, and import them into your
	 *  notes structure
	 */
    public void handleOpen() {
        String sFile = SettingsHandler.getGMGenOption(NotesPlugin.LOG_NAME + ".LastFile", System.getProperty("user.dir"));
        File defaultFile = new File(sFile);
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(defaultFile);
        FileFilter[] ff = plugin.getFileTypes();
        for (int i = 0; i < ff.length; i++) {
            chooser.addChoosableFileFilter(ff[i]);
            chooser.setFileFilter(ff[i]);
        }
        chooser.setMultiSelectionEnabled(true);
        java.awt.Cursor saveCursor = MiscUtilities.setBusyCursor(GMGenSystem.inst);
        int option = chooser.showOpenDialog(GMGenSystem.inst);
        if (option == JFileChooser.APPROVE_OPTION) {
            File[] noteFiles = chooser.getSelectedFiles();
            for (int i = 0; i < noteFiles.length; i++) {
                SettingsHandler.setGMGenOption(NotesPlugin.LOG_NAME + ".LastFile", noteFiles[i].toString());
                if (noteFiles[i].toString().endsWith(".gmn")) {
                    openGMN(noteFiles[i]);
                }
            }
        }
        MiscUtilities.setCursor(GMGenSystem.inst, saveCursor);
        refreshTree();
    }

    /**
	 *  fills the 'edit' menu of the main menu
	 *
	 *@param  editMenu  The Edit Menu
	 */
    public void initEditMenu(JMenu editMenu) {
        JMenuItem paste = new JMenuItem("Paste");
        paste.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pasteButtonActionPerformed(evt);
            }
        });
        editMenu.insert(paste, 0);
        JMenuItem copy = new JMenuItem("Copy");
        paste.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyButtonActionPerformed(evt);
            }
        });
        editMenu.insert(copy, 0);
        JMenuItem cut = new JMenuItem("Cut");
        paste.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cutButtonActionPerformed(evt);
            }
        });
        editMenu.insert(cut, 0);
        editMenu.insertSeparator(0);
        editMenu.insert(redoAction, 0);
        editMenu.insert(undoAction, 0);
    }

    /**
	 *  Opens a .gmn file
	 *
	 *@param  notesFile  .gmn file to open
	 */
    public void openGMN(File notesFile) {
        try {
            Object obj = notesTree.getLastSelectedPathComponent();
            if (obj instanceof NotesTreeNode) {
                NotesTreeNode node = (NotesTreeNode) obj;
                if (node != root) {
                    int choice = JOptionPane.showConfirmDialog(this, "Importing note " + notesFile.getName() + " into a node other then root, Continue?", "Importing to a node other then root", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (choice == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
                InputStream in = new BufferedInputStream(new FileInputStream(notesFile));
                ZipInputStream zin = new ZipInputStream(in);
                ZipEntry e;
                ProgressMonitor pm = new ProgressMonitor(GMGenSystem.inst, "Reading Notes Export", "Reading", 1, 1000);
                int progress = 1;
                while ((e = zin.getNextEntry()) != null) {
                    unzip(zin, e.getName(), node.getDir());
                    progress++;
                    if (progress > 99) {
                        progress = 99;
                    }
                    pm.setProgress(progress);
                }
                zin.close();
                pm.close();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error Reading File" + notesFile.getName());
            Logging.errorPrint("Error Reading File" + notesFile.getName());
            Logging.errorPrint(e.getMessage(), e);
        }
    }

    /**  refreshs the tree, and updates it's UI */
    public void refreshTree() {
        root.refresh();
        notesTree.updateUI();
    }

    /**  called when window is closed, saves everything in the tree */
    public void windowClosed() {
        if (root.isTreeDirty()) {
            GMGenSystemView.getTabPane().setSelectedComponent(this);
        }
        root.checkSave();
    }

    /**
	 *  Exports a node out to a gmn file.
	 *
	 *@param  node  node to export to file
	 */
    protected void exportFile(NotesTreeNode node) {
        JFileChooser fLoad = new JFileChooser();
        String sFile = SettingsHandler.getGMGenOption(NotesPlugin.LOG_NAME + ".LastFile", "");
        new File(sFile);
        String[] fileExt = new String[] { "gmn" };
        SimpleFileFilter ff = new SimpleFileFilter(fileExt, "GMGen Notes Export");
        fLoad.addChoosableFileFilter(ff);
        fLoad.setFileFilter(ff);
        int returnVal = fLoad.showSaveDialog(this);
        try {
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String fileName = fLoad.getSelectedFile().getName();
                String dirName = fLoad.getSelectedFile().getParent();
                String ext = "";
                if (fileName.indexOf(".gmn") < 0) {
                    ext = ".gmn";
                }
                File expFile = new File(dirName + File.separator + fileName + ext);
                if (expFile.exists()) {
                    int choice = JOptionPane.showConfirmDialog(this, "File Exists, Overwrite?", "File Exists", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (choice == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
                SettingsHandler.setGMGenOption(NotesPlugin.LOG_NAME + ".LastFile", expFile.toString());
                writeNotesFile(expFile, node);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Writing File");
            Logging.errorPrint("Error Writing to file: " + e.getMessage(), e);
        }
    }

    /**
	 *  gets the number of files in a directory so that you can have a progress
	 *  meter as we zip them into a gmn file. This function is recursive
	 *
	 *@param  count  File to count the children of
	 *@return        count of all files in this dir
	 */
    protected int fileCount(File count) {
        int num = 0;
        File[] entries = count.listFiles();
        for (int i = 0; i < entries.length; i++) {
            File f = entries[i];
            if (f.isDirectory()) {
                num = num + fileCount(f);
            } else {
                num++;
            }
        }
        return num;
    }

    /**
	 *  Sets a border of an editing button to indicate that the function of the
	 *  button is active according to the text location of the cursor
	 *
	 *@param  button  Button to highlight
	 */
    protected void highlightButton(JButton button) {
        button.setBorder(new BevelBorder(BevelBorder.LOWERED));
    }

    /**
	 *  Performs an action of a particular name on the man editor.
	 *
	 *@param  name  name of the action to perform.
	 *@param  evt   ActionEvent that sparked the calling of this function.
	 */
    protected void performTextPaneAction(String name, java.awt.event.ActionEvent evt) {
        Action action = getActionByName(editor, name);
        action.actionPerformed(evt);
        editor.grabFocus();
        int cp = editor.getCaretPosition();
        updateButtons(editor, cp);
    }

    /**
	 *  Sets a border of an editing button to indicate that the function of the
	 *  button is not active according to the text location of the cursor
	 *
	 *@param  button  button to set in standard mode
	 */
    protected void stdButton(JButton button) {
        button.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    }

    /**
	 *  Unzips one file from a zipinputstream
	 *
	 *@param  zin              Zip input stream
	 *@param  homeDir          Directory to unzip the file to
	 *@param  entry            Description of the Parameter
	 *@exception  IOException  read or write error
	 */
    protected void unzip(ZipInputStream zin, String entry, File homeDir) throws IOException {
        File outFile = new File(homeDir.getPath() + File.separator + entry);
        File parentDir = outFile.getParentFile();
        parentDir.mkdirs();
        outFile.createNewFile();
        FileOutputStream out = new FileOutputStream(outFile);
        byte[] b = new byte[512];
        int len = 0;
        while ((len = zin.read(b)) != -1) {
            out.write(b, 0, len);
        }
        out.close();
    }

    /**
	 *  Updates Editing buttons based on the location of the cursor
	 *
	 *@param  textPane  text pane to update buttons base on
	 *@param  pos       current text position
	 */
    protected void updateButtons(JTextPane textPane, int pos) {
        StyledDocument doc = textPane.getStyledDocument();
        AttributeSet set = doc.getCharacterElement(pos - 1).getAttributes();
        AttributeSet set1 = doc.getCharacterElement(pos).getAttributes();
        if (StyleConstants.isBold(set) && StyleConstants.isBold(set1)) {
            highlightButton(boldButton);
        } else {
            stdButton(boldButton);
        }
        if (StyleConstants.isItalic(set) && StyleConstants.isItalic(set1)) {
            highlightButton(italicButton);
        } else {
            stdButton(italicButton);
        }
        if (StyleConstants.isUnderline(set) && StyleConstants.isUnderline(set1)) {
            highlightButton(underlineButton);
        } else {
            stdButton(underlineButton);
        }
        int align = StyleConstants.getAlignment(set);
        stdButton(leftJustifyButton);
        stdButton(rightJustifyButton);
        stdButton(centerJustifyButton);
        if (align == StyleConstants.ALIGN_LEFT) {
            highlightButton(leftJustifyButton);
        } else if (align == StyleConstants.ALIGN_RIGHT) {
            highlightButton(rightJustifyButton);
        } else if (align == StyleConstants.ALIGN_CENTER) {
            highlightButton(centerJustifyButton);
        }
        int fontSize = StyleConstants.getFontSize(set);
        for (int i = 0; i < sizeCB.getItemCount(); i++) {
            String value = (String) sizeCB.getItemAt(i);
            if (value.equals(fontSize + "")) {
                sizeCB.setSelectedItem(value);
                break;
            }
        }
    }

    /**
	 *  Writes out a directory to a zipoutputstream
	 *
	 *@param  out              Zip output stream to write to
	 *@param  parentDir        parent dir of whole structure to be written out
	 *@param  currentDir       dir to be zipped up
	 *@param  pm               progress meter that will display the progress
	 *@param  progress         progress up to this dir
	 *@return                  current progress
	 *@exception  IOException  write or read failed for some reason
	 */
    protected int writeNotesDir(ZipOutputStream out, File parentDir, File currentDir, ProgressMonitor pm, int progress) throws IOException {
        File[] entries = currentDir.listFiles();
        byte[] buffer = new byte[4096];
        int bytes_read;
        for (int i = 0; i < entries.length; i++) {
            if (pm.isCanceled()) {
                return 0;
            }
            File f = entries[i];
            if (f.isDirectory()) {
                progress = writeNotesDir(out, parentDir, f, pm, progress);
            } else {
                FileInputStream in = new FileInputStream(f);
                try {
                    String parentPath = parentDir.getParentFile().getAbsolutePath();
                    ZipEntry entry = new ZipEntry(f.getAbsolutePath().substring(parentPath.length() + 1));
                    out.putNextEntry(entry);
                    while ((bytes_read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytes_read);
                    }
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                        }
                    }
                }
                progress++;
            }
        }
        pm.setProgress(progress);
        return progress;
    }

    /**
	 *  Writes out a GMN file
	 *
	 *@param  exportFile       file to export to
	 *@param  node             node to export
	 *@exception  IOException  file write failed for some reason
	 */
    protected void writeNotesFile(File exportFile, NotesTreeNode node) throws IOException {
        File dir = node.getDir();
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(exportFile));
        int max = fileCount(dir);
        ProgressMonitor pm = new ProgressMonitor(GMGenSystem.inst, "Writing out Notes Export", "Writing", 0, max);
        try {
            writeNotesDir(out, dir, dir, pm, 0);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        pm.close();
    }

    private File getCurrentDir() {
        Object obj = notesTree.getLastSelectedPathComponent();
        if (obj instanceof NotesTreeNode) {
            NotesTreeNode node = (NotesTreeNode) obj;
            return node.getDir();
        }
        return null;
    }

    /**
	 *  obtains an Image for input using a custom JFileChooser dialog
	 *
	 *@param  startDir  Directory to open JFielChooser to
	 *@param  exts      Extensions to search for
	 *@param  desc      Description for files
	 *@return           File pointing to the selected image
	 */
    private File getImageFromChooser(String startDir, String[] exts, String desc) {
        ImageFileChooser jImageDialog = new ImageFileChooser(startDir);
        jImageDialog.setDialogType(JFileChooser.CUSTOM_DIALOG);
        jImageDialog.setFileFilter(new SimpleFileFilter(exts, desc));
        jImageDialog.setDialogTitle("Select an Image to Insert");
        int optionSelected = JFileChooser.CANCEL_OPTION;
        optionSelected = jImageDialog.showDialog(this, "Insert");
        if (optionSelected == JFileChooser.APPROVE_OPTION) {
            return jImageDialog.getSelectedFile();
        }
        return null;
    }

    private void notesTreeNodesChanged() {
        Object obj = notesTree.getLastSelectedPathComponent();
        if (obj instanceof NotesTreeNode) {
            NotesTreeNode node = (NotesTreeNode) obj;
            try {
                node.rename((String) node.getUserObject());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void boldButtonActionPerformed(java.awt.event.ActionEvent evt) {
        performTextPaneAction("font-bold", evt);
    }

    private void centerJustifyButtonActionPerformed(java.awt.event.ActionEvent evt) {
        Action action = new StyledEditorKit.AlignmentAction("Align Centre", StyleConstants.ALIGN_CENTER);
        action.actionPerformed(evt);
        editor.grabFocus();
        int cp = editor.getCaretPosition();
        updateButtons(editor, cp);
    }

    private void colorButtonActionPerformed() {
        AttributeSet as = editor.getCharacterAttributes();
        SimpleAttributeSet sas = new SimpleAttributeSet(as);
        Color newColor = JColorChooser.showDialog(GMGenSystem.inst, "Choose Text Color", editor.getStyledDocument().getForeground(as));
        if (newColor != null) {
            StyleConstants.setForeground(sas, newColor);
            editor.setCharacterAttributes(sas, true);
        }
        editor.repaint();
    }

    private void copyButtonActionPerformed(java.awt.event.ActionEvent evt) {
        performTextPaneAction(DefaultEditorKit.copyAction, evt);
    }

    private void cutButtonActionPerformed(java.awt.event.ActionEvent evt) {
        performTextPaneAction(DefaultEditorKit.cutAction, evt);
    }

    private void deleteButtonActionPerformed() {
        Object obj = notesTree.getLastSelectedPathComponent();
        if (obj instanceof NotesTreeNode) {
            NotesTreeNode node = (NotesTreeNode) obj;
            node.delete();
        }
        notesTree.updateUI();
    }

    private void editorCaretUpdate(CaretEvent evt) {
        int dot = evt.getDot();
        updateButtons(editor, dot);
        Object obj = notesTree.getLastSelectedPathComponent();
        if (obj instanceof NotesTreeNode) {
            NotesTreeNode node = (NotesTreeNode) obj;
            if (node.isDirty()) {
                revertButton.setEnabled(true);
            } else {
                revertButton.setEnabled(false);
            }
        }
    }

    private void editorKeyTyped(KeyEvent evt) {
        editor.getCaretPosition();
        editor.getStyledDocument();
        if (evt.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
            handleBackspace();
        } else if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
            handleEnter();
        }
    }

    private void editorUndoableEditHappened(UndoableEditEvent e) {
        undo.addEdit(e.getEdit());
        undoAction.updateUndoState();
        redoAction.updateRedoState();
    }

    private void exportButtonActionPerformed() {
        Object obj = notesTree.getLastSelectedPathComponent();
        if (obj instanceof NotesTreeNode) {
            NotesTreeNode node = (NotesTreeNode) obj;
            exportFile(node);
        }
    }

    private void fileLeftActionPerformed() {
        if (filesBar.getComponentCount() > 1) {
            Component c = filesBar.getComponent(filesBar.getComponentCount() - 1);
            filesBar.remove(c);
            filesBar.add(c, 0);
        }
        filesBar.updateUI();
    }

    private void fileRightActionPerformed() {
        if (filesBar.getComponentCount() > 1) {
            Component c = filesBar.getComponent(0);
            filesBar.remove(c);
            filesBar.add(c);
        }
        filesBar.updateUI();
    }

    private void handleBackspace() {
        Element elem;
        int pos = editor.getCaretPosition();
        ExtendedHTMLDocument htmlDoc = (ExtendedHTMLDocument) editor.getStyledDocument();
        try {
            if (pos > 0) {
                if ((editor.getSelectedText()) != null) {
                    ExtendedHTMLEditorKit.delete(editor);
                    return;
                }
                int sOffset = htmlDoc.getParagraphElement(pos).getStartOffset();
                if (sOffset == editor.getSelectionStart()) {
                    boolean content = true;
                    if (ExtendedHTMLEditorKit.checkParentsTag(htmlDoc.getParagraphElement(editor.getCaretPosition()), HTML.Tag.LI)) {
                        elem = ExtendedHTMLEditorKit.getListItemParent(htmlDoc.getCharacterElement(editor.getCaretPosition()));
                        content = false;
                        int so = elem.getStartOffset();
                        int eo = elem.getEndOffset();
                        if ((so + 1) < eo) {
                            char[] temp = editor.getText(so, eo - so).toCharArray();
                            for (int i = 0; i < temp.length; i++) {
                                if (!Character.isWhitespace(temp[i])) {
                                    content = true;
                                }
                            }
                        }
                        if (!content) {
                            elem.getParentElement();
                            ExtendedHTMLEditorKit.removeTag(editor, elem, true);
                            editor.setCaretPosition(sOffset - 1);
                            return;
                        }
                        editor.setCaretPosition(editor.getCaretPosition() - 1);
                        editor.moveCaretPosition(editor.getCaretPosition() - 2);
                        editor.replaceSelection("");
                        return;
                    }
                }
                editor.replaceSelection("");
                return;
            }
        } catch (BadLocationException ble) {
            Logging.errorPrint(ble.getMessage(), ble);
        }
    }

    private void handleEnter() {
        Element elem;
        int pos = editor.getCaretPosition();
        int repos = -1;
        ExtendedHTMLDocument htmlDoc = (ExtendedHTMLDocument) editor.getStyledDocument();
        try {
            if ((ExtendedHTMLEditorKit.checkParentsTag(htmlDoc.getParagraphElement(editor.getCaretPosition()), HTML.Tag.UL) == true) | (ExtendedHTMLEditorKit.checkParentsTag(htmlDoc.getParagraphElement(editor.getCaretPosition()), HTML.Tag.OL) == true)) {
                elem = ExtendedHTMLEditorKit.getListItemParent(htmlDoc.getCharacterElement(editor.getCaretPosition()));
                int so = elem.getStartOffset();
                int eo = elem.getEndOffset();
                char[] temp = editor.getText(so, eo - so).toCharArray();
                boolean content = false;
                for (int i = 0; i < temp.length; i++) {
                    if (!Character.isWhitespace(temp[i])) {
                        content = true;
                    }
                }
                if (content) {
                    int end = -1;
                    int j = temp.length;
                    do {
                        j--;
                        if (Character.isLetterOrDigit(temp[j])) {
                            end = j;
                        }
                    } while ((end == -1) && (j >= 0));
                    j = end;
                    do {
                        j++;
                        if (!Character.isSpaceChar(temp[j])) {
                            repos = j - end - 1;
                        }
                    } while ((repos == -1) && (j < temp.length));
                    if (repos == -1) {
                        repos = 0;
                    }
                }
                if ((elem.getStartOffset() == elem.getEndOffset()) || !content) {
                    manageListElement(htmlDoc);
                } else {
                    if ((editor.getCaretPosition() + 1) == elem.getEndOffset()) {
                        ExtendedHTMLEditorKit.insertListElement(editor, "");
                        editor.setCaretPosition(pos - repos);
                    } else {
                        int caret = editor.getCaretPosition();
                        String tempString = editor.getText(caret, eo - caret);
                        editor.select(caret, eo - 1);
                        editor.replaceSelection("");
                        ExtendedHTMLEditorKit.insertListElement(editor, tempString);
                        Element newLi = ExtendedHTMLEditorKit.getListItemParent(htmlDoc.getCharacterElement(editor.getCaretPosition()));
                        editor.setCaretPosition(newLi.getEndOffset());
                    }
                }
            }
        } catch (BadLocationException ble) {
            Logging.errorPrint(ble.getMessage(), ble);
        }
    }

    private void imageButtonActionPerformed() {
        try {
            insertLocalImage(null);
        } catch (Exception e) {
            Logging.errorPrint(e.getMessage(), e);
        }
    }

    /**
	 *  This method is called from within the constructor to initialize the form.
	 *  WARNING: Do NOT modify this code. The content of this method is always
	 *  regenerated by the Form Editor.
	 */
    private void initComponents() {
        jSplitPane1 = new FlippingSplitPane();
        jScrollPane1 = new JScrollPane();
        notesTree = new JTree();
        jPanel1 = new JPanel();
        jScrollPane2 = new JScrollPane();
        editor = new JTextPane();
        jPanel2 = new JPanel();
        fileBar = new JToolBar();
        newButton = new JButton();
        saveButton = new JButton();
        exportButton = new JButton();
        revertButton = new JButton();
        deleteButton = new JButton();
        clipboardBar = new JToolBar();
        cutButton = new JButton();
        copyButton = new JButton();
        pasteButton = new JButton();
        formatBar = new JToolBar();
        sizeCB = new JComboBox();
        boldButton = new JButton();
        italicButton = new JButton();
        underlineButton = new JButton();
        colorButton = new JButton();
        bulletButton = new JButton();
        enumButton = new JButton();
        imageButton = new JButton();
        alignmentBar = new JToolBar();
        leftJustifyButton = new JButton();
        centerJustifyButton = new JButton();
        rightJustifyButton = new JButton();
        filePane = new JPanel();
        fileLeft = new JButton();
        fileRight = new JButton();
        filesBar = new JToolBar();
        setLayout(new java.awt.BorderLayout());
        jSplitPane1.setDividerLocation(175);
        jSplitPane1.setDividerSize(5);
        jScrollPane1.setViewportView(notesTree);
        jSplitPane1.setLeftComponent(jScrollPane1);
        jPanel1.setLayout(new java.awt.BorderLayout());
        editor.addCaretListener(new CaretListener() {

            public void caretUpdate(CaretEvent evt) {
                editorCaretUpdate(evt);
            }
        });
        jScrollPane2.setViewportView(editor);
        jPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        newButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_new-16.png")));
        newButton.setToolTipText("New Node");
        newButton.setBorder(new EtchedBorder());
        newButton.setEnabled(false);
        newButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed();
            }
        });
        fileBar.add(newButton);
        saveButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_save-16.png")));
        saveButton.setToolTipText("Save Node");
        saveButton.setBorder(new EtchedBorder());
        saveButton.setEnabled(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed();
            }
        });
        fileBar.add(saveButton);
        exportButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_export-16.png")));
        exportButton.setToolTipText("Export");
        exportButton.setBorder(new EtchedBorder());
        exportButton.setEnabled(false);
        exportButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed();
            }
        });
        fileBar.add(exportButton);
        revertButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_revert-16.png")));
        revertButton.setToolTipText("Revert to Saved");
        revertButton.setBorder(new EtchedBorder());
        revertButton.setEnabled(false);
        revertButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                revertButtonActionPerformed();
            }
        });
        fileBar.add(revertButton);
        deleteButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_broken_image-16.png")));
        deleteButton.setToolTipText("Delete Node");
        deleteButton.setBorder(new EtchedBorder());
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed();
            }
        });
        fileBar.add(deleteButton);
        jPanel2.add(fileBar);
        cutButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_cut-16.png")));
        cutButton.setToolTipText("Cut");
        cutButton.setBorder(new EtchedBorder());
        cutButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cutButtonActionPerformed(evt);
            }
        });
        clipboardBar.add(cutButton);
        copyButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_copy-16.png")));
        copyButton.setToolTipText("Copy");
        copyButton.setBorder(new EtchedBorder());
        copyButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyButtonActionPerformed(evt);
            }
        });
        clipboardBar.add(copyButton);
        pasteButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_paste-16.png")));
        pasteButton.setToolTipText("Paste");
        pasteButton.setBorder(new EtchedBorder());
        pasteButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pasteButtonActionPerformed(evt);
            }
        });
        clipboardBar.add(pasteButton);
        jPanel2.add(clipboardBar);
        sizeCB.setToolTipText("Size");
        sizeCB.setBorder(new EtchedBorder());
        sizeCB.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sizeCBActionPerformed(evt);
            }
        });
        formatBar.add(sizeCB);
        boldButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_text_bold-16.png")));
        boldButton.setToolTipText("Bold");
        boldButton.setBorder(new EtchedBorder());
        boldButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boldButtonActionPerformed(evt);
            }
        });
        formatBar.add(boldButton);
        italicButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_text_italic-16.png")));
        italicButton.setToolTipText("Italic");
        italicButton.setBorder(new EtchedBorder());
        italicButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                italicButtonActionPerformed(evt);
            }
        });
        formatBar.add(italicButton);
        underlineButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_text_underline-16.png")));
        underlineButton.setToolTipText("Underline");
        underlineButton.setBorder(new EtchedBorder());
        underlineButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                underlineButtonActionPerformed(evt);
            }
        });
        formatBar.add(underlineButton);
        colorButton.setForeground(new java.awt.Color(0, 0, 0));
        colorButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/menu-mode-RGB-alt.png")));
        colorButton.setToolTipText("Color");
        colorButton.setBorder(new EtchedBorder());
        colorButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorButtonActionPerformed();
            }
        });
        formatBar.add(colorButton);
        bulletButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_list_bulet-16.png")));
        bulletButton.setToolTipText("Bulleted List");
        bulletButton.setAction(actionListUnordered);
        bulletButton.setBorder(new EtchedBorder());
        formatBar.add(bulletButton);
        enumButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_list_enum-16.png")));
        enumButton.setToolTipText("Numbered List");
        enumButton.setAction(actionListOrdered);
        enumButton.setBorder(new EtchedBorder());
        formatBar.add(enumButton);
        imageButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_insert_graphic-16.png")));
        imageButton.setBorder(new EtchedBorder());
        imageButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imageButtonActionPerformed();
            }
        });
        formatBar.add(imageButton);
        jPanel2.add(formatBar);
        leftJustifyButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_text_align_left-16.png")));
        leftJustifyButton.setToolTipText("Left Justify");
        leftJustifyButton.setBorder(new EtchedBorder());
        leftJustifyButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leftJustifyButtonActionPerformed(evt);
            }
        });
        alignmentBar.add(leftJustifyButton);
        centerJustifyButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_text_align_center-16.png")));
        centerJustifyButton.setToolTipText("Center");
        centerJustifyButton.setBorder(new EtchedBorder());
        centerJustifyButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                centerJustifyButtonActionPerformed(evt);
            }
        });
        alignmentBar.add(centerJustifyButton);
        rightJustifyButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_text_align_right-16.png")));
        rightJustifyButton.setToolTipText("Right Justify");
        rightJustifyButton.setBorder(new EtchedBorder());
        rightJustifyButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rightJustifyButtonActionPerformed(evt);
            }
        });
        alignmentBar.add(rightJustifyButton);
        jPanel2.add(alignmentBar);
        jPanel1.add(jPanel2, java.awt.BorderLayout.NORTH);
        filePane.setLayout(new BoxLayout(filePane, BoxLayout.X_AXIS));
        fileLeft.setText("<");
        fileLeft.setBorder(new EtchedBorder());
        fileLeft.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileLeftActionPerformed();
            }
        });
        filePane.add(fileLeft);
        fileRight.setText(">");
        fileRight.setBorder(new EtchedBorder());
        fileRight.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileRightActionPerformed();
            }
        });
        filePane.add(fileRight);
        filePane.add(filesBar);
        jPanel1.add(filePane, java.awt.BorderLayout.SOUTH);
        jSplitPane1.setRightComponent(jPanel1);
        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }

    private void initDnDComponents() {
        filesBarDT = new DropTarget(filesBar, new DropBarListener());
        treeDT = new DropTarget(notesTree, new DropTreeListener());
    }

    private void initEditingComponents() {
        bulletButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_list_bulet-16.png")));
        bulletButton.setToolTipText("Bulleted List");
        enumButton.setIcon(new ImageIcon(getClass().getResource("/pcgen/gui/resource/stock_list_enum-16.png")));
        enumButton.setToolTipText("Numbered List");
        enumButton.setText("");
        bulletButton.setText("");
        Vector fontVector = new Vector();
        fontVector.add("8");
        fontVector.add("10");
        fontVector.add("12");
        fontVector.add("14");
        fontVector.add("16");
        fontVector.add("18");
        fontVector.add("24");
        fontVector.add("36");
        fontVector.add("48");
        DefaultComboBoxModel cbModel = new DefaultComboBoxModel(fontVector);
        sizeCB.setModel(cbModel);
        sizeCB.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        stdButton(boldButton);
        stdButton(italicButton);
        stdButton(underlineButton);
        stdButton(colorButton);
        stdButton(leftJustifyButton);
        stdButton(centerJustifyButton);
        stdButton(rightJustifyButton);
        stdButton(newButton);
        stdButton(saveButton);
        stdButton(deleteButton);
        stdButton(cutButton);
        stdButton(copyButton);
        stdButton(pasteButton);
    }

    private void initFileBar(List files) {
        filePane.removeAll();
        filesBar.removeAll();
        if (files.size() > 0) {
            filePane.add(fileLeft);
            filePane.add(fileRight);
            filePane.add(filesBar);
            for (int i = 0; i < files.size(); i++) {
                filesBar.add(new JIcon((File) files.get(i), plugin));
            }
        }
        filePane.updateUI();
    }

    private void initLogging() {
        LogUtilities.inst().addReceiver(new NotesLogReciever());
    }

    private void initTree() {
        File dir = new File(dataDir);
        dir.listFiles();
        root = new NotesTreeNode(dir.getName(), dir, notesTree);
        TreeModel model = new DefaultTreeModel(root);
        notesTree.setModel(model);
        notesTree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent evt) {
                notesTreeActionPerformed();
            }
        });
        notesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        notesTree.setEditable(true);
        model.addTreeModelListener(new TreeModelListener() {

            public void treeNodesChanged(TreeModelEvent e) {
                notesTreeNodesChanged();
            }

            public void treeNodesInserted(TreeModelEvent e) {
            }

            public void treeNodesRemoved(TreeModelEvent e) {
            }

            public void treeStructureChanged(TreeModelEvent e) {
            }
        });
    }

    /**
	 *  Method for inserting an image from a file
	 *
	 *@param  whatImage                 pointer to file
	 *@exception  IOException           if the file can't be read
	 *@exception  BadLocationException  if the file does not exist
	 *@exception  RuntimeException      cause
	 */
    private void insertLocalImage(File whatImage) throws IOException, BadLocationException, RuntimeException {
        if (whatImage == null) {
            File dir = getCurrentDir();
            File newImage = getImageFromChooser(dir.getPath(), extsIMG, "Image File");
            if (newImage != null && newImage.exists()) {
                whatImage = new File(dir.getAbsolutePath() + File.separator + newImage.getName());
                if (!whatImage.exists()) {
                    MiscUtilities.copy(newImage, whatImage);
                }
            }
        }
        if (whatImage != null) {
            int caretPos = editor.getCaretPosition();
            ExtendedHTMLEditorKit htmlKit = (ExtendedHTMLEditorKit) editor.getEditorKit();
            ExtendedHTMLDocument htmlDoc = (ExtendedHTMLDocument) editor.getStyledDocument();
            htmlKit.insertHTML(htmlDoc, caretPos, "<IMG SRC=\"" + whatImage + "\">", 0, 0, HTML.Tag.IMG);
            editor.setCaretPosition(caretPos + 1);
        }
    }

    private void italicButtonActionPerformed(java.awt.event.ActionEvent evt) {
        performTextPaneAction("font-italic", evt);
    }

    private void leftJustifyButtonActionPerformed(java.awt.event.ActionEvent evt) {
        Action action = new StyledEditorKit.AlignmentAction("Left Justify", StyleConstants.ALIGN_LEFT);
        action.actionPerformed(evt);
        editor.grabFocus();
        int cp = editor.getCaretPosition();
        updateButtons(editor, cp);
    }

    private void manageListElement(ExtendedHTMLDocument htmlDoc) {
        Element h = ExtendedHTMLEditorKit.getListItemParent(htmlDoc.getCharacterElement(editor.getCaretPosition()));
        h.getParentElement();
        if (h != null) {
            ExtendedHTMLEditorKit.removeTag(editor, h, true);
        }
    }

    private void newButtonActionPerformed() {
        Object obj = notesTree.getLastSelectedPathComponent();
        if (obj instanceof NotesTreeNode) {
            NotesTreeNode node = (NotesTreeNode) obj;
            node.createChild();
        }
        refreshTree();
    }

    private void notesTreeActionPerformed() {
        refreshTreeNodes();
    }

    private void pasteButtonActionPerformed(java.awt.event.ActionEvent evt) {
        performTextPaneAction(DefaultEditorKit.pasteAction, evt);
    }

    private void refreshTreeNodes() {
        Object obj = notesTree.getLastSelectedPathComponent();
        if (obj instanceof NotesTreeNode) {
            NotesTreeNode node = (NotesTreeNode) obj;
            editor = node.getTextPane();
            root.checkCache();
            JViewport vp = new JViewport();
            vp.setView(editor);
            jScrollPane2.setViewport(vp);
            editAreaDT = new DropTarget(editor, new DropEditorListener());
            editor.addCaretListener(new CaretListener() {

                public void caretUpdate(CaretEvent evt) {
                    editorCaretUpdate(evt);
                }
            });
            editor.addKeyListener(new java.awt.event.KeyListener() {

                public void keyTyped(KeyEvent e) {
                    editorKeyTyped(e);
                }

                public void keyPressed(KeyEvent e) {
                }

                public void keyReleased(KeyEvent e) {
                }
            });
            editor.getStyledDocument().addUndoableEditListener(new UndoableEditListener() {

                public void undoableEditHappened(UndoableEditEvent evt) {
                    editorUndoableEditHappened(evt);
                }
            });
            if (node.isLeaf()) {
                deleteButton.setEnabled(true);
            } else {
                deleteButton.setEnabled(false);
            }
            if (node == root) {
                exportButton.setEnabled(false);
            } else {
                exportButton.setEnabled(true);
            }
            if (node.isDirty()) {
                revertButton.setEnabled(true);
            } else {
                revertButton.setEnabled(false);
            }
            initFileBar(node.getFiles());
            saveButton.setEnabled(true);
            newButton.setEnabled(true);
        } else if (obj == null) {
            deleteButton.setEnabled(false);
            saveButton.setEnabled(false);
            revertButton.setEnabled(false);
            newButton.setEnabled(false);
        }
    }

    private void revertButtonActionPerformed() {
        Object obj = notesTree.getLastSelectedPathComponent();
        if (obj instanceof NotesTreeNode) {
            NotesTreeNode node = (NotesTreeNode) obj;
            node.revert();
            refreshTreeNodes();
            notesTree.updateUI();
        }
    }

    private void rightJustifyButtonActionPerformed(java.awt.event.ActionEvent evt) {
        Action action = new StyledEditorKit.AlignmentAction("Right Justify", StyleConstants.ALIGN_RIGHT);
        action.actionPerformed(evt);
        editor.grabFocus();
        int cp = editor.getCaretPosition();
        updateButtons(editor, cp);
    }

    private void saveButtonActionPerformed() {
        Object obj = notesTree.getLastSelectedPathComponent();
        if (obj instanceof NotesTreeNode) {
            NotesTreeNode node = (NotesTreeNode) obj;
            node.save();
        }
        revertButton.setEnabled(false);
        notesTree.updateUI();
    }

    private void sizeCBActionPerformed(final ActionEvent evt) {
        if (sizeCB.hasFocus()) {
            String fontS = (String) sizeCB.getSelectedItem();
            performTextPaneAction("font-size-" + fontS, evt);
        }
    }

    private void underlineButtonActionPerformed(java.awt.event.ActionEvent evt) {
        performTextPaneAction("font-underline", evt);
    }

    /**
	 *  This is an abstract drop listener. Extend this to listen for drop events
	 *  for a particular Component
	 */
    public abstract class DropListener implements DropTargetListener {

        /**
		 *  Checks to see if dragEnter is supported for the actions on this event
		 *  Accepts only javaFileListFlavor data flavors
		 *
		 *@param  dtde  DropTargetDragEvent
		 */
        public void dragEnter(DropTargetDragEvent dtde) {
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrag(dtde.getDropAction());
            } else {
                dtde.rejectDrag();
            }
        }

        /**
		 *  Called when drag exit happens. override this id you need anythign to
		 *  happen
		 *
		 *@param  dte  DropTargetEvent
		 */
        public void dragExit(DropTargetEvent dte) {
        }

        /**
		 *  Accpets a drag over if the data flavor is javaFileListFlavor, otherwise
		 *  rejects it.
		 *
		 *@param  dtde  DropTargetDragEvent
		 */
        public void dragOver(DropTargetDragEvent dtde) {
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrag(dtde.getDropAction());
            } else {
                dtde.rejectDrag();
            }
        }

        /**
		 *  implements a drop. you need to implements this in your class.
		 *
		 *@param  dtde  DropTargetDropEvent
		 */
        public abstract void drop(DropTargetDropEvent dtde);

        /**
		 *  Action has changed - we don't do anything, override if you need it to.
		 *
		 *@param  dtde  DropTargetDragEvent
		 */
        public void dropActionChanged(DropTargetDragEvent dtde) {
        }
    }

    /**
	 *  Drop listener for the File bar on the bottom of the Notes screen
	 */
    public class DropBarListener extends DropListener {

        /**
		 *  implements drop.if we accept it, pass the event to the currently selected
		 *  node
		 *
		 *@param  dtde  DropTargetDropEvent
		 */
        public void drop(DropTargetDropEvent dtde) {
            Object obj = notesTree.getLastSelectedPathComponent();
            if (obj instanceof NotesTreeNode) {
                NotesTreeNode node = (NotesTreeNode) obj;
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.dropComplete(node.handleDropJavaFileList(dtde));
                    refreshTreeNodes();
                } else {
                    dtde.rejectDrop();
                }
            } else {
                dtde.rejectDrop();
            }
        }
    }

    /**
	 *  Drop listener for the Editor pane on the notes screen
	 */
    public class DropEditorListener extends DropListener {

        /**
		 *  Determines if a file passed in is an image or not (based on extension
		 *
		 *@param  image  File to check
		 *@return        true if image, false if not
		 */
        public boolean isImageFile(File image) {
            for (int i = 0; i < extsIMG.length; i++) {
                if (image.getName().endsWith(extsIMG[i])) {
                    return true;
                }
            }
            return false;
        }

        /**
		 *  implements drop. if we accept it, pass the eventthe handler
		 *
		 *@param  dtde  Description of the Parameter
		 */
        public void drop(DropTargetDropEvent dtde) {
            Object obj = notesTree.getLastSelectedPathComponent();
            if (obj instanceof NotesTreeNode) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.dropComplete(handleDropJavaFileListAsImage(dtde));
                    refreshTreeNodes();
                } else {
                    dtde.rejectDrop();
                }
            } else {
                dtde.rejectDrop();
            }
        }

        /**
		 *  handles a drop. if the drop is an image, it will insert the image to the
		 *  proper place in the editor window.
		 *
		 *@param  dtde  DropTargetDropEvent
		 *@return       drop successful or not
		 */
        public boolean handleDropJavaFileListAsImage(DropTargetDropEvent dtde) {
            dtde.acceptDrop(dtde.getDropAction());
            Transferable t = dtde.getTransferable();
            try {
                List fileList = ((List) t.getTransferData(DataFlavor.javaFileListFlavor));
                File dir = getCurrentDir();
                for (int i = 0; i < fileList.size(); i++) {
                    File newFile = (File) fileList.get(i);
                    if (newFile.exists()) {
                        File destFile = new File(dir.getAbsolutePath() + File.separator + newFile.getName());
                        if (!isImageFile(destFile) || !destFile.exists()) {
                            MiscUtilities.copy(newFile, destFile);
                        }
                        editor.setCaretPosition(editor.viewToModel(dtde.getLocation()));
                        handleImageDropInsertion(destFile);
                    }
                }
            } catch (Exception e) {
                Logging.errorPrint(e.getMessage(), e);
                return false;
            }
            return true;
        }

        /**
		 *  Inserts a dropped image into the editor pane
		 *
		 *@param  image  File to insert
		 */
        public void handleImageDropInsertion(File image) {
            for (int i = 0; i < extsIMG.length; i++) {
                if (image.getName().endsWith(extsIMG[i])) {
                    try {
                        insertLocalImage(image);
                    } catch (Exception e) {
                        Logging.errorPrint(e.getMessage(), e);
                    }
                    break;
                }
            }
        }
    }

    /**
	 *  Drop listener for the Tree
	 */
    public class DropTreeListener extends DropListener {

        /**
		 *  implements drop.if we accept it, pass the event to the currently selected
		 *  node
		 *
		 *@param  dtde  Description of the Parameter
		 */
        public void drop(DropTargetDropEvent dtde) {
            Point p = dtde.getLocation();
            TreePath path = notesTree.getPathForLocation(p.x, p.y);
            if (path == null) {
                dtde.rejectDrop();
                return;
            }
            Object obj = path.getLastPathComponent();
            if (obj instanceof NotesTreeNode) {
                NotesTreeNode node = (NotesTreeNode) obj;
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.dropComplete(node.handleDropJavaFileList(dtde));
                    refreshTreeNodes();
                } else {
                    dtde.rejectDrop();
                }
            } else {
                dtde.rejectDrop();
            }
        }
    }

    public class NotesLogReciever implements LogReceiver {

        NotesTreeNode log;

        public NotesLogReciever() {
        }

        /**
		 * Logs a message associated with a specific owner.
		 *
		 * @param owner the owner of the message being logged.
		 * @param message the message to log.
		 *@since        GMGen 3.3
		 */
        public void logMessage(String owner, String message) {
            if (log == null) {
                log = getChildNode("Logs", root);
            }
            NotesTreeNode node = getChildNode(owner, log);
            SimpleDateFormat dateFmt = new SimpleDateFormat("MM-dd-yyyy hh.mm.ss a z");
            node.appendText("<br>\n<b>" + dateFmt.format(Calendar.getInstance().getTime()) + "</b> " + message);
        }

        /**
		 * Logs a message not associated with a specific owner.
		 *
		 * @param message the message to log.
		 *@since        GMGen 3.3
		 */
        public void logMessage(String message) {
            logMessage("Misc", message);
        }

        private NotesTreeNode getChildNode(String name, NotesTreeNode parentNode) {
            Enumeration newNodes = parentNode.children();
            for (; newNodes.hasMoreElements(); ) {
                NotesTreeNode node = (NotesTreeNode) newNodes.nextElement();
                if (node.getUserObject().equals(NotesTreeNode.checkName(name))) {
                    return node;
                }
            }
            return parentNode.createChild(name);
        }
    }

    /**
	 *  Action implementing Redo for editor
	 */
    protected class RedoAction extends AbstractAction {

        /**  Constructor for the RedoAction object */
        public RedoAction() {
            super("Redo");
            setEnabled(false);
        }

        /**
		 *  Redo Action is preformed, run undo on the undo manager
		 *
		 *@param  e  Action Event
		 */
        public void actionPerformed(ActionEvent e) {
            try {
                undo.redo();
            } catch (CannotRedoException ex) {
                Logging.errorPrint("Unable to redo: " + ex);
            }
            updateRedoState();
            undoAction.updateUndoState();
        }

        /**  Update the current state of the redo labe */
        protected void updateRedoState() {
            if (undo.canRedo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getRedoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
    }

    /**
	 *  Action implementing Undo for editor
	 */
    protected class UndoAction extends AbstractAction {

        /**  Constructor for the UndoAction object */
        public UndoAction() {
            super("Undo");
            setEnabled(false);
        }

        /**
		 *  Undo Action is preformed, run undo on the undo manager.
		 *
		 *@param  e  Action Event
		 */
        public void actionPerformed(ActionEvent e) {
            try {
                undo.undo();
            } catch (CannotUndoException ex) {
                Logging.errorPrint("Unable to undo: " + ex.getMessage(), ex);
            }
            updateUndoState();
            redoAction.updateRedoState();
        }

        /**  Update the current state of the undo label */
        protected void updateUndoState() {
            if (undo.canUndo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getUndoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }
    }
}

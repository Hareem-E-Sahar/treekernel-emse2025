import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JToolBar;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.undo.*;

/**
 *
 * @author cfviper
 */
public class EditorBar extends javax.swing.JPanel implements ActionListener {

    private HTMLDocument document;

    private JTextPane textPane = new JTextPane();

    private boolean debug = false;

    private File currentFile;

    static final JButton theBold = new JButton("B");

    /** Listener for the edits on the current document. */
    protected UndoableEditListener undoHandler = new UndoHandler();

    /** UndoManager that we add edits to. */
    protected UndoManager undo = new UndoManager();

    private UndoAction undoAction = new UndoAction();

    private RedoAction redoAction = new RedoAction();

    private Action cutAction = new DefaultEditorKit.CutAction();

    private Action copyAction = new DefaultEditorKit.CopyAction();

    private Action pasteAction = new DefaultEditorKit.PasteAction();

    protected Action boldAction = new StyledEditorKit.BoldAction();

    private Action underlineAction = new StyledEditorKit.UnderlineAction();

    private Action italicAction = new StyledEditorKit.ItalicAction();

    private Action insertBreakAction = new DefaultEditorKit.InsertBreakAction();

    private HTMLEditorKit.InsertHTMLTextAction unorderedListAction = new HTMLEditorKit.InsertHTMLTextAction("Bullets", "<ul><li> </li></ul>", HTML.Tag.P, HTML.Tag.UL);

    private HTMLEditorKit.InsertHTMLTextAction bulletAction = new HTMLEditorKit.InsertHTMLTextAction("Bullets", "<li> </li>", HTML.Tag.UL, HTML.Tag.LI);

    protected JPanel thePane;

    public EditorBar(JPanel thePane) {
        HTMLEditorKit editorKit = new HTMLEditorKit();
        document = (HTMLDocument) editorKit.createDefaultDocument();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exc) {
            System.err.println("Error loading L&F: " + exc);
        }
        init();
    }

    public static void main(String[] args) {
        EditorBar editor = new EditorBar();
    }

    private EditorBar() {
    }

    public void init() {
        JMenuBar menuBar = new JMenuBar();
        thePane.add(menuBar, BorderLayout.NORTH);
        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");
        JMenu colorMenu = new JMenu("Color");
        JMenu fontMenu = new JMenu("Font");
        JMenu styleMenu = new JMenu("Style");
        JMenu alignMenu = new JMenu("Align");
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(colorMenu);
        menuBar.add(fontMenu);
        menuBar.add(styleMenu);
        menuBar.add(alignMenu);
        menuBar.add(theBold);
        JMenuItem newItem = new JMenuItem("New", new ImageIcon("whatsnew-bang.gif"));
        JMenuItem openItem = new JMenuItem("Open", new ImageIcon("open.gif"));
        JMenuItem saveItem = new JMenuItem("Save", new ImageIcon("save.gif"));
        JMenuItem saveAsItem = new JMenuItem("Save As");
        JMenuItem exitItem = new JMenuItem("Exit", new ImageIcon("exit.gif"));
        newItem.addActionListener(this);
        openItem.addActionListener(this);
        saveItem.addActionListener(this);
        saveAsItem.addActionListener(this);
        exitItem.addActionListener(this);
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(exitItem);
        JMenuItem undoItem = new JMenuItem(undoAction);
        JMenuItem redoItem = new JMenuItem(redoAction);
        JMenuItem cutItem = new JMenuItem(cutAction);
        JMenuItem copyItem = new JMenuItem(copyAction);
        JMenuItem pasteItem = new JMenuItem(pasteAction);
        JMenuItem clearItem = new JMenuItem("Clear");
        JMenuItem selectAllItem = new JMenuItem("Select All");
        JMenuItem insertBreaKItem = new JMenuItem(insertBreakAction);
        JMenuItem unorderedListItem = new JMenuItem(unorderedListAction);
        JMenuItem bulletItem = new JMenuItem(bulletAction);
        cutItem.setText("Cut");
        copyItem.setText("Copy");
        pasteItem.setText("Paste");
        insertBreaKItem.setText("Break");
        cutItem.setIcon(new ImageIcon("cut.gif"));
        copyItem.setIcon(new ImageIcon("copy.gif"));
        pasteItem.setIcon(new ImageIcon("paste.gif"));
        insertBreaKItem.setIcon(new ImageIcon("break.gif"));
        unorderedListItem.setIcon(new ImageIcon("bullets.gif"));
        clearItem.addActionListener(this);
        selectAllItem.addActionListener(this);
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.add(clearItem);
        editMenu.add(selectAllItem);
        editMenu.add(insertBreaKItem);
        editMenu.add(unorderedListItem);
        editMenu.add(bulletItem);
        JMenuItem redTextItem = new JMenuItem(new StyledEditorKit.ForegroundAction("Red", Color.red));
        JMenuItem orangeTextItem = new JMenuItem(new StyledEditorKit.ForegroundAction("Orange", Color.orange));
        JMenuItem yellowTextItem = new JMenuItem(new StyledEditorKit.ForegroundAction("Yellow", Color.yellow));
        JMenuItem greenTextItem = new JMenuItem(new StyledEditorKit.ForegroundAction("Green", Color.green));
        JMenuItem blueTextItem = new JMenuItem(new StyledEditorKit.ForegroundAction("Blue", Color.blue));
        JMenuItem cyanTextItem = new JMenuItem(new StyledEditorKit.ForegroundAction("Cyan", Color.cyan));
        JMenuItem magentaTextItem = new JMenuItem(new StyledEditorKit.ForegroundAction("Magenta", Color.magenta));
        JMenuItem blackTextItem = new JMenuItem(new StyledEditorKit.ForegroundAction("Black", Color.black));
        redTextItem.setIcon(new ImageIcon("red.gif"));
        orangeTextItem.setIcon(new ImageIcon("orange.gif"));
        yellowTextItem.setIcon(new ImageIcon("yellow.gif"));
        greenTextItem.setIcon(new ImageIcon("green.gif"));
        blueTextItem.setIcon(new ImageIcon("blue.gif"));
        cyanTextItem.setIcon(new ImageIcon("cyan.gif"));
        magentaTextItem.setIcon(new ImageIcon("magenta.gif"));
        blackTextItem.setIcon(new ImageIcon("black.gif"));
        colorMenu.add(redTextItem);
        colorMenu.add(orangeTextItem);
        colorMenu.add(yellowTextItem);
        colorMenu.add(greenTextItem);
        colorMenu.add(blueTextItem);
        colorMenu.add(cyanTextItem);
        colorMenu.add(magentaTextItem);
        colorMenu.add(blackTextItem);
        JMenu fontTypeMenu = new JMenu("Font Type");
        fontMenu.add(fontTypeMenu);
        String[] fontTypes = { "SansSerif", "Serif", "Monospaced", "Dialog", "DialogInput" };
        for (int i = 0; i < fontTypes.length; i++) {
            if (debug) System.out.println(fontTypes[i]);
            JMenuItem nextTypeItem = new JMenuItem(fontTypes[i]);
            nextTypeItem.setAction(new StyledEditorKit.FontFamilyAction(fontTypes[i], fontTypes[i]));
            fontTypeMenu.add(nextTypeItem);
        }
        JMenu fontSizeMenu = new JMenu("Font Size");
        fontMenu.add(fontSizeMenu);
        int[] fontSizes = { 6, 8, 10, 12, 14, 16, 20, 24, 32, 36, 48, 72 };
        for (int i = 0; i < fontSizes.length; i++) {
            if (debug) System.out.println(fontSizes[i]);
            JMenuItem nextSizeItem = new JMenuItem(String.valueOf(fontSizes[i]));
            nextSizeItem.setAction(new StyledEditorKit.FontSizeAction(String.valueOf(fontSizes[i]), fontSizes[i]));
            fontSizeMenu.add(nextSizeItem);
        }
        JMenuItem boldMenuItem = new JMenuItem(boldAction);
        JMenuItem underlineMenuItem = new JMenuItem(underlineAction);
        JMenuItem italicMenuItem = new JMenuItem(italicAction);
        boldMenuItem.setText("Bold---");
        underlineMenuItem.setText("Underline");
        italicMenuItem.setText("Italic");
        boldMenuItem.setIcon(new ImageIcon("Sal.jpg"));
        underlineMenuItem.setIcon(new ImageIcon("underline.gif"));
        italicMenuItem.setIcon(new ImageIcon("italic.gif"));
        theBold.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                theBold.setAction(boldAction);
                theBold.setText("B");
            }
        });
        styleMenu.add(boldMenuItem);
        styleMenu.add(underlineMenuItem);
        styleMenu.add(italicMenuItem);
        JMenuItem subscriptMenuItem = new JMenuItem(new SubscriptAction());
        JMenuItem superscriptMenuItem = new JMenuItem(new SuperscriptAction());
        JMenuItem strikeThroughMenuItem = new JMenuItem(new StrikeThroughAction());
        subscriptMenuItem.setText("Subscript");
        superscriptMenuItem.setText("Superscript");
        strikeThroughMenuItem.setText("StrikeThrough");
        subscriptMenuItem.setIcon(new ImageIcon("subscript.gif"));
        superscriptMenuItem.setIcon(new ImageIcon("superscript.gif"));
        strikeThroughMenuItem.setIcon(new ImageIcon("strikethough.gif"));
        styleMenu.add(subscriptMenuItem);
        styleMenu.add(superscriptMenuItem);
        styleMenu.add(strikeThroughMenuItem);
        JMenuItem leftAlignMenuItem = new JMenuItem(new StyledEditorKit.AlignmentAction("Left Align", StyleConstants.ALIGN_LEFT));
        JMenuItem centerMenuItem = new JMenuItem(new StyledEditorKit.AlignmentAction("Center", StyleConstants.ALIGN_CENTER));
        JMenuItem rightAlignMenuItem = new JMenuItem(new StyledEditorKit.AlignmentAction("Right Align", StyleConstants.ALIGN_RIGHT));
        leftAlignMenuItem.setText("Left Align");
        centerMenuItem.setText("Center");
        rightAlignMenuItem.setText("Right Align");
        leftAlignMenuItem.setIcon(new ImageIcon("left.gif"));
        centerMenuItem.setIcon(new ImageIcon("center.gif"));
        rightAlignMenuItem.setIcon(new ImageIcon("right.gif"));
        alignMenu.add(leftAlignMenuItem);
        alignMenu.add(centerMenuItem);
        alignMenu.add(rightAlignMenuItem);
        JPanel editorControlPanel = new JPanel();
        editorControlPanel.setLayout(new FlowLayout());
        JButton cutButton = new JButton(cutAction);
        JButton copyButton = new JButton(copyAction);
        JButton pasteButton = new JButton(pasteAction);
        JButton boldButton = new JButton(boldAction);
        JButton underlineButton = new JButton(underlineAction);
        JButton italicButton = new JButton(italicAction);
        cutButton.setText("Cut");
        copyButton.setText("Copy");
        pasteButton.setText("Paste");
        boldButton.setText("Bold!!");
        underlineButton.setText("Underline");
        italicButton.setText("Italic");
        cutButton.setIcon(new ImageIcon("cut.gif"));
        copyButton.setIcon(new ImageIcon("copy.gif"));
        pasteButton.setIcon(new ImageIcon("paste.gif"));
        boldButton.setIcon(new ImageIcon("bold.gif"));
        underlineButton.setIcon(new ImageIcon("underline.gif"));
        italicButton.setIcon(new ImageIcon("italic.gif"));
        editorControlPanel.add(cutButton);
        editorControlPanel.add(copyButton);
        editorControlPanel.add(pasteButton);
        editorControlPanel.add(boldButton);
        editorControlPanel.add(underlineButton);
        editorControlPanel.add(italicButton);
        JButton subscriptButton = new JButton(new SubscriptAction());
        JButton superscriptButton = new JButton(new SuperscriptAction());
        JButton strikeThroughButton = new JButton(new StrikeThroughAction());
        subscriptButton.setIcon(new ImageIcon("subscript.gif"));
        superscriptButton.setIcon(new ImageIcon("superscript.gif"));
        strikeThroughButton.setIcon(new ImageIcon("strikethough.gif"));
        JPanel specialPanel = new JPanel();
        specialPanel.setLayout(new FlowLayout());
        specialPanel.add(subscriptButton);
        specialPanel.add(superscriptButton);
        specialPanel.add(strikeThroughButton);
        JButton leftAlignButton = new JButton(new StyledEditorKit.AlignmentAction("Left Align", StyleConstants.ALIGN_LEFT));
        JButton centerButton = new JButton(new StyledEditorKit.AlignmentAction("Center", StyleConstants.ALIGN_CENTER));
        JButton rightAlignButton = new JButton(new StyledEditorKit.AlignmentAction("Right Align", StyleConstants.ALIGN_RIGHT));
        JButton colorButton = new JButton(new StyledEditorKit.AlignmentAction("Right Align", StyleConstants.ALIGN_RIGHT));
        leftAlignButton.setIcon(new ImageIcon("left.gif"));
        centerButton.setIcon(new ImageIcon("center.gif"));
        rightAlignButton.setIcon(new ImageIcon("right.gif"));
        colorButton.setIcon(new ImageIcon("color.gif"));
        leftAlignButton.setText("Left Align");
        centerButton.setText("Center");
        rightAlignButton.setText("Right Align");
        JPanel alignPanel = new JPanel();
        alignPanel.setLayout(new FlowLayout());
        alignPanel.add(leftAlignButton);
        alignPanel.add(centerButton);
        alignPanel.add(rightAlignButton);
        document.addUndoableEditListener(undoHandler);
        resetUndoManager();
        textPane = new JTextPane(document);
        textPane.setContentType("text/html");
        JScrollPane scrollPane = new JScrollPane(textPane);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension scrollPaneSize = new Dimension(5 * screenSize.width / 8, 5 * screenSize.height / 8);
        scrollPane.setPreferredSize(scrollPaneSize);
        JPanel toolPanel = new JPanel();
        toolPanel.setLayout(new BorderLayout());
        toolPanel.add(editorControlPanel, BorderLayout.NORTH);
        toolPanel.add(specialPanel, BorderLayout.CENTER);
        toolPanel.add(alignPanel, BorderLayout.SOUTH);
        thePane.add(menuBar, BorderLayout.NORTH);
        thePane.add(scrollPane, BorderLayout.SOUTH);
        startNewDocument();
        show();
    }

    public void actionPerformed(ActionEvent ae) {
        String actionCommand = ae.getActionCommand();
        if (debug) {
            int modifier = ae.getModifiers();
            long when = ae.getWhen();
            String parameter = ae.paramString();
            System.out.println("actionCommand: " + actionCommand);
            System.out.println("modifier: " + modifier);
            System.out.println("when: " + when);
            System.out.println("parameter: " + parameter);
        }
        if (actionCommand.compareTo("New") == 0) {
            startNewDocument();
        } else if (actionCommand.compareTo("Open") == 0) {
            openDocument();
        } else if (actionCommand.compareTo("Save") == 0) {
            saveDocument();
        } else if (actionCommand.compareTo("Save As") == 0) {
            saveDocumentAs();
        } else if (actionCommand.compareTo("Exit") == 0) {
            exit();
        } else if (actionCommand.compareTo("Clear") == 0) {
            clear();
        } else if (actionCommand.compareTo("Select All") == 0) {
            selectAll();
        } else if (actionCommand.compareTo("Keyboard Shortcuts") == 0) {
            showShortcuts();
        }
    }

    protected void resetUndoManager() {
        undo.discardAllEdits();
        undoAction.update();
        redoAction.update();
    }

    public void startNewDocument() {
        Document oldDoc = textPane.getDocument();
        if (oldDoc != null) oldDoc.removeUndoableEditListener(undoHandler);
        HTMLEditorKit editorKit = new HTMLEditorKit();
        document = (HTMLDocument) editorKit.createDefaultDocument();
        textPane.setDocument(document);
        currentFile = null;
        textPane.getDocument().addUndoableEditListener(undoHandler);
        resetUndoManager();
    }

    public void openDocument() {
        try {
            File current = new File(".");
            JFileChooser chooser = new JFileChooser(current);
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setFileFilter(new HTMLFileFilter());
            int approval = chooser.showSaveDialog(this);
            if (approval == JFileChooser.APPROVE_OPTION) {
                currentFile = chooser.getSelectedFile();
                FileReader fr = new FileReader(currentFile);
                Document oldDoc = textPane.getDocument();
                if (oldDoc != null) oldDoc.removeUndoableEditListener(undoHandler);
                HTMLEditorKit editorKit = new HTMLEditorKit();
                document = (HTMLDocument) editorKit.createDefaultDocument();
                editorKit.read(fr, document, 0);
                document.addUndoableEditListener(undoHandler);
                textPane.setDocument(document);
                resetUndoManager();
            }
        } catch (BadLocationException ble) {
            System.err.println("BadLocationException: " + ble.getMessage());
        } catch (FileNotFoundException fnfe) {
            System.err.println("FileNotFoundException: " + fnfe.getMessage());
        } catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

    public void saveDocument() {
        if (currentFile != null) {
            try {
                FileWriter fw = new FileWriter(currentFile);
                fw.write(textPane.getText());
                fw.close();
            } catch (FileNotFoundException fnfe) {
                System.err.println("FileNotFoundException: " + fnfe.getMessage());
            } catch (IOException ioe) {
                System.err.println("IOException: " + ioe.getMessage());
            }
        } else {
            saveDocumentAs();
        }
    }

    public void saveDocumentAs() {
        try {
            File current = new File(".");
            JFileChooser chooser = new JFileChooser(current);
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setFileFilter(new HTMLFileFilter());
            int approval = chooser.showSaveDialog(this);
            if (approval == JFileChooser.APPROVE_OPTION) {
                File newFile = chooser.getSelectedFile();
                if (newFile.exists()) {
                    String message = newFile.getAbsolutePath() + " already exists. \n" + "Do you want to replace it?";
                    if (JOptionPane.showConfirmDialog(this, message) == JOptionPane.YES_OPTION) {
                        currentFile = newFile;
                        FileWriter fw = new FileWriter(currentFile);
                        fw.write(textPane.getText());
                        fw.close();
                        if (debug) System.out.println("Saved " + currentFile.getAbsolutePath());
                    }
                } else {
                    currentFile = new File(newFile.getAbsolutePath());
                    FileWriter fw = new FileWriter(currentFile);
                    fw.write(textPane.getText());
                    fw.close();
                    if (debug) System.out.println("Saved " + currentFile.getAbsolutePath());
                }
            }
        } catch (FileNotFoundException fnfe) {
            System.err.println("FileNotFoundException: " + fnfe.getMessage());
        } catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

    public void exit() {
        String exitMessage = "Are you sure you want to exit?";
        if (JOptionPane.showConfirmDialog(this, exitMessage) == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public void clear() {
        startNewDocument();
    }

    public void selectAll() {
        textPane.selectAll();
    }

    public void showShortcuts() {
        String shortcuts = "Navigate in    |  Tab\n" + "Navigate out   |  Ctrl+Tab\n" + "Navigate out backwards    |  Shift+Ctrl+Tab\n" + "Move up/down a line    |  Up/Down Arrown\n" + "Move left/right a component or char    |  Left/Right Arrow\n" + "Move up/down one vertical block    |  PgUp/PgDn\n" + "Move to start/end of line    |  Home/End\n" + "Move to previous/next word    |  Ctrl+Left/Right Arrow\n" + "Move to start/end of data    |  Ctrl+Home/End\n" + "Move left/right one block    |  Ctrl+PgUp/PgDn\n" + "Select All    |  Ctrl+A\n" + "Extend selection up one line    |  Shift+Up Arrow\n" + "Extend selection down one line    |  Shift+Down Arrow\n" + "Extend selection to beginning of line    |  Shift+Home\n" + "Extend selection to end of line    |  Shift+End\n" + "Extend selection to beginning of data    |  Ctrl+Shift+Home\n" + "Extend selection to end of data    |  Ctrl+Shift+End\n" + "Extend selection left    |  Shift+Right Arrow\n" + "Extend selection right    |  Shift+Right Arrow\n" + "Extend selection up one vertical block    |  Shift+PgUp\n" + "Extend selection down one vertical block    |  Shift+PgDn\n" + "Extend selection left one block    |  Ctrl+Shift+PgUp\n" + "Extend selection right one block    |  Ctrl+Shift+PgDn\n" + "Extend selection left one word    |  Ctrl+Shift+Left Arrow\n" + "Extend selection right one word    |  Ctrl+Shift+Right Arrow\n";
        JOptionPane.showMessageDialog(this, shortcuts);
    }

    class FrameListener extends WindowAdapter {

        public void windowClosing(WindowEvent we) {
            exit();
        }
    }

    class SubscriptAction extends StyledEditorKit.StyledTextAction {

        public SubscriptAction() {
            super(StyleConstants.Subscript.toString());
        }

        public void actionPerformed(ActionEvent ae) {
            JEditorPane editor = getEditor(ae);
            if (editor != null) {
                StyledEditorKit kit = getStyledEditorKit(editor);
                MutableAttributeSet attr = kit.getInputAttributes();
                boolean subscript = (StyleConstants.isSubscript(attr)) ? false : true;
                SimpleAttributeSet sas = new SimpleAttributeSet();
                StyleConstants.setSubscript(sas, subscript);
                setCharacterAttributes(editor, sas, false);
            }
        }
    }

    class SuperscriptAction extends StyledEditorKit.StyledTextAction {

        public SuperscriptAction() {
            super(StyleConstants.Superscript.toString());
        }

        public void actionPerformed(ActionEvent ae) {
            JEditorPane editor = getEditor(ae);
            if (editor != null) {
                StyledEditorKit kit = getStyledEditorKit(editor);
                MutableAttributeSet attr = kit.getInputAttributes();
                boolean superscript = (StyleConstants.isSuperscript(attr)) ? false : true;
                SimpleAttributeSet sas = new SimpleAttributeSet();
                StyleConstants.setSuperscript(sas, superscript);
                setCharacterAttributes(editor, sas, false);
            }
        }
    }

    class StrikeThroughAction extends StyledEditorKit.StyledTextAction {

        public StrikeThroughAction() {
            super(StyleConstants.StrikeThrough.toString());
        }

        public void actionPerformed(ActionEvent ae) {
            JEditorPane editor = getEditor(ae);
            if (editor != null) {
                StyledEditorKit kit = getStyledEditorKit(editor);
                MutableAttributeSet attr = kit.getInputAttributes();
                boolean strikeThrough = (StyleConstants.isStrikeThrough(attr)) ? false : true;
                SimpleAttributeSet sas = new SimpleAttributeSet();
                StyleConstants.setStrikeThrough(sas, strikeThrough);
                setCharacterAttributes(editor, sas, false);
            }
        }
    }

    class HTMLFileFilter extends javax.swing.filechooser.FileFilter {

        public boolean accept(File f) {
            return ((f.isDirectory()) || (f.getName().toLowerCase().indexOf(".htm") > 0));
        }

        public String getDescription() {
            return "html";
        }
    }

    class UndoHandler implements UndoableEditListener {

        /**
		 * Messaged when the Document has created an edit, the edit is
		 * added to <code>undo</code>, an instance of UndoManager.
		 */
        public void undoableEditHappened(UndoableEditEvent e) {
            undo.addEdit(e.getEdit());
            undoAction.update();
            redoAction.update();
        }
    }

    class UndoAction extends AbstractAction {

        public UndoAction() {
            super("Undo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.undo();
            } catch (CannotUndoException ex) {
                System.out.println("Unable to undo: " + ex);
                ex.printStackTrace();
            }
            update();
            redoAction.update();
        }

        protected void update() {
            if (undo.canUndo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getUndoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }
    }

    class RedoAction extends AbstractAction {

        public RedoAction() {
            super("Redo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.redo();
            } catch (CannotRedoException ex) {
                System.err.println("Unable to redo: " + ex);
                ex.printStackTrace();
            }
            update();
            undoAction.update();
        }

        protected void update() {
            if (undo.canRedo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getRedoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
    }

    public EditorBar(JToolBar menu) {
        initComponents();
        JButton b = new JButton("x");
        menu.add(b);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        editorToolBar = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        setName("Form");
        editorToolBar.setRollover(true);
        editorToolBar.setName("editorToolBar");
        jButton1.setText(org.openide.util.NbBundle.getMessage(EditorBar.class, "EditorBar.jButton1.text"));
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setName("jButton1");
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editorToolBar.add(jButton1);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(115, 115, 115).addComponent(editorToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(185, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(42, 42, 42).addComponent(editorToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(233, Short.MAX_VALUE)));
    }

    private javax.swing.JToolBar editorToolBar;

    private javax.swing.JButton jButton1;
}

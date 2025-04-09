package org.dance.editor;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.Utilities;
import org.dance.ScoreWriter;
import org.dance.editor.i18n.Messages;
import org.dance.parser.ModernRockParser;
import org.dance.parser.ParseException;
import org.dance.score.Score;

/**
 * A <a href="http://ostermiller.org/syntax/editor.html">demonstration text
 * editor</a> that uses syntax highlighting.
 */
public class ScoreEditor extends JFrame {

    /**
	 * 
	 */
    private static final long serialVersionUID = -868841813224952531L;

    private static final Logger logger = Logger.getLogger(ScoreEditor.class.getName());

    /** The document holding the text being edited. */
    private HighlightedDocument document = new HighlightedDocument();

    /** The text pane displaying the document. */
    private JTextPane textPane = new JTextPane(document);

    private JTextPane outputPane = new JTextPane();

    private JEditorPane htmlPane = new JEditorPane();

    private File currentFile = null;

    private File htmlTempFile = null, exportHtmlFile = null;

    private JFileChooser fc = new JFileChooser();

    private CompoundUndoManager undoManager = null;

    private JLabel labelStatus = new JLabel();

    private JTabbedPane tabbedPane;

    /**
	 * Create a new Demo
	 */
    public ScoreEditor() {
        super(Messages.getString("DanceScoreEditor"));
        try {
            String strlookAndFeel = UIManager.getSystemLookAndFeelClassName();
            logger.log(Level.INFO, "LookAndFeel java.swing :" + strlookAndFeel);
            UIManager.setLookAndFeel(strlookAndFeel);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Cannot set Look And Feel to System default.", ex);
        }
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        tabbedPane = new JTabbedPane();
        JComponent panel1 = makeEditPanel();
        tabbedPane.addTab(Messages.getString("Tab_DanceScore"), null, panel1, Messages.getString("Tab_DanceScoreHint"));
        JComponent panel2 = makeHTMLPanel();
        tabbedPane.addTab(Messages.getString("Tab_HTMLScore"), null, panel2, Messages.getString("Tab_HTMLScoreHint"));
        add(tabbedPane);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        JMenuBar mb = new JMenuBar();
        mb.add(createFileMenu());
        mb.add(createEditMenu());
        mb.add(createHelpMenu());
        setJMenuBar(mb);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

            public void windowActivated(WindowEvent e) {
                textPane.requestFocus();
            }
        });
        tabbedPane.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent arg0) {
                if (tabbedPane.indexOfTab(Messages.getString("Tab_HTMLScore")) == tabbedPane.getSelectedIndex()) {
                    checkAndComputeHTMLView();
                }
            }
        });
        initDocument();
        pack();
        setVisible(true);
        this.setLocationRelativeTo(null);
    }

    public JComponent makeEditPanel() {
        JPanel centerPanel = new JPanel();
        SpringLayout layout = new SpringLayout();
        centerPanel.setLayout(layout);
        JPanel panel = new JPanel();
        BorderLayout l = new BorderLayout();
        panel.setLayout(l);
        JScrollPane textScrollPane = new JScrollPane(textPane);
        textPane.setEditable(true);
        textPane.addCaretListener(new CaretListener() {

            public void caretUpdate(CaretEvent e) {
                JTextPane editArea = (JTextPane) e.getSource();
                if (editArea.getText().length() != 0) {
                    try {
                        int caretPos = editArea.getCaretPosition();
                        int rowNum = (caretPos == 0) ? 1 : 0;
                        for (int offset = caretPos; offset > 0; ) {
                            offset = Utilities.getRowStart(editArea, offset) - 1;
                            rowNum++;
                        }
                        int offset = Utilities.getRowStart(editArea, caretPos);
                        int colNum = caretPos - offset + 1;
                        updateStatus(rowNum, colNum);
                    } catch (BadLocationException ex) {
                        logger.log(Level.SEVERE, "error while computing row and column number.", ex);
                        updateStatus(1, 1);
                    }
                } else {
                    updateStatus(1, 1);
                }
            }
        });
        textPane.addMouseListener(new DoubleClickListener(textPane));
        TextLineNumber tln = new TextLineNumber(textPane);
        textScrollPane.setRowHeaderView(tln);
        textPane.setCaretPosition(0);
        updateStatus(1, 1);
        undoManager = new CompoundUndoManager(textPane);
        centerPanel.add(textScrollPane);
        JLabel labelParsing = new JLabel(Messages.getString("Label_ParsingMessage"));
        centerPanel.add(labelParsing);
        outputPane.setEditable(false);
        Style style = outputPane.addStyle("Red", null);
        StyleConstants.setForeground(style, Color.red);
        StyleConstants.setBold(style, true);
        style = outputPane.addStyle("Green", null);
        StyleConstants.setForeground(style, Color.green.darker());
        StyleConstants.setBold(style, true);
        JScrollPane outputScrollPane = new JScrollPane(outputPane);
        ;
        centerPanel.add(outputScrollPane);
        SpringLayout.Constraints cons;
        cons = layout.getConstraints(textScrollPane);
        cons.setX(Spring.constant(0));
        cons.setY(Spring.constant(0));
        cons.setWidth(layout.getConstraint(SpringLayout.EAST, centerPanel));
        cons.setHeight(Spring.scale(layout.getConstraint(SpringLayout.SOUTH, centerPanel), .7f));
        cons = layout.getConstraints(labelParsing);
        cons.setX(Spring.constant(0));
        cons.setY(layout.getConstraint(SpringLayout.SOUTH, textScrollPane));
        cons.setWidth(layout.getConstraint(SpringLayout.EAST, centerPanel));
        cons.setHeight(Spring.scale(layout.getConstraint(SpringLayout.SOUTH, centerPanel), .1f));
        cons = layout.getConstraints(outputScrollPane);
        cons.setX(Spring.constant(0));
        cons.setY(layout.getConstraint(SpringLayout.SOUTH, labelParsing));
        cons.setWidth(layout.getConstraint(SpringLayout.EAST, centerPanel));
        cons.setHeight(Spring.scale(layout.getConstraint(SpringLayout.SOUTH, centerPanel), .2f));
        setSize(800, 600);
        setPreferredSize(getSize());
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(labelStatus, BorderLayout.PAGE_END);
        return panel;
    }

    public JComponent makeHTMLPanel() {
        htmlPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(htmlPane);
        return scrollPane;
    }

    private void updateStatus(int linenumber, int columnnumber) {
        labelStatus.setText("Line: " + linenumber + " Column: " + columnnumber);
    }

    public void openURI(URI uri) {
        if (!java.awt.Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(this, Messages.getString("Msg_DesktopIsNotSupported"));
            return;
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            JOptionPane.showMessageDialog(this, Messages.getString("Msg_BrowseActionNotSupported"));
            return;
        }
        try {
            desktop.browse(uri);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, Messages.getString("Msg_AnErrorOccuredWhileBrowsing") + " " + e.getMessage());
        }
    }

    private void copy() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        TransferHandler transferHandler = textPane.getTransferHandler();
        transferHandler.exportToClipboard(textPane, clipboard, TransferHandler.COPY);
    }

    private void paste() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        TransferHandler transferHandler = textPane.getTransferHandler();
        transferHandler.importData(textPane, clipboard.getContents(null));
    }

    private void cut() {
        textPane.cut();
    }

    private static String readFileAsString(String filePath) throws java.io.IOException {
        BufferedReader reader = null;
        try {
            StringBuffer fileData = new StringBuffer(1000);
            reader = new BufferedReader(new FileReader(filePath));
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
            return fileData.toString();
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error while closing file", ex);
            }
        }
    }

    public int askUserToSaveDocument() {
        int retval = JOptionPane.showConfirmDialog(this, "Do you want to save the change ?");
        switch(retval) {
            case JOptionPane.CANCEL_OPTION:
                break;
            case JOptionPane.NO_OPTION:
                break;
            case JOptionPane.YES_OPTION:
                if (currentFile == null) {
                    saveAsDocument();
                } else {
                    saveDocument();
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown retval value !");
        }
        return retval;
    }

    public void loadDocument() {
        int returnVal = JOptionPane.YES_OPTION;
        if (document.isModified()) {
            returnVal = askUserToSaveDocument();
        }
        if (returnVal != JOptionPane.CANCEL_OPTION) {
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                currentFile = fc.getSelectedFile();
                logger.info("Opening: " + currentFile.getAbsolutePath());
                try {
                    textPane.setText(readFileAsString(currentFile.getAbsolutePath()));
                    document.setModified(false);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error while loading the file '" + currentFile.getPath() + "'.");
                    logger.log(Level.SEVERE, "Error while reading file", ex);
                }
            } else {
                logger.info("Open command cancelled by user.");
            }
        } else {
            logger.info("Open command cancelled by user.");
        }
    }

    public void saveAsDocument() {
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        if (currentFile == null) {
            fc.setSelectedFile(new File("new.code"));
        } else {
            fc.setSelectedFile(currentFile);
        }
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            currentFile = fc.getSelectedFile();
            int result = JOptionPane.YES_OPTION;
            if (currentFile.exists()) {
                result = JOptionPane.showConfirmDialog(this, String.format(Messages.getString("Msg_OverwriteExistingFile"), new Object[] { currentFile.getAbsolutePath() }));
            }
            if (result == JOptionPane.YES_OPTION) {
                saveDocument();
            }
        } else {
            logger.info("Save command cancelled by user.");
        }
    }

    public void saveHTMLView() {
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        if (exportHtmlFile != null) {
            fc.setSelectedFile(exportHtmlFile);
        }
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            exportHtmlFile = fc.getSelectedFile();
            if (!exportHtmlFile.getAbsolutePath().endsWith(".html")) {
                exportHtmlFile = new File(exportHtmlFile.getAbsolutePath() + ".html");
            }
            logger.info("Saving to: " + exportHtmlFile.getAbsolutePath());
            int result = JOptionPane.YES_OPTION;
            if (exportHtmlFile.exists()) {
                result = JOptionPane.showConfirmDialog(this, String.format(Messages.getString("Msg_OverwriteExistingFile"), new Object[] { exportHtmlFile.getAbsolutePath() }));
            }
            if (result == JOptionPane.YES_OPTION) {
                logger.log(Level.INFO, "Exporting HTML view to " + exportHtmlFile.getAbsolutePath());
                FileWriter fw = null;
                try {
                    String htmlContent = readFileAsString(htmlTempFile.getAbsolutePath());
                    fw = new FileWriter(exportHtmlFile, false);
                    fw.write(htmlContent);
                    fw.flush();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "error while exporting HTML view.", ex);
                    JOptionPane.showMessageDialog(this, Messages.getString("Msg_ErrorWhileExportingHTML") + "\n" + ex.getMessage());
                } finally {
                    try {
                        fw.close();
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "error while closing file.", e);
                    }
                }
            }
        } else {
            logger.info("Save command cancelled by user.");
        }
    }

    public void saveDocument() {
        if (currentFile == null) {
            saveAsDocument();
        } else {
            logger.info("Saving to " + currentFile.getAbsolutePath());
            FileWriter fw = null;
            try {
                fw = new FileWriter(currentFile, false);
                fw.write(textPane.getText());
                fw.flush();
                fw.close();
            } catch (IOException ex) {
                try {
                    fw.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error while closing the current file : '" + currentFile.getPath() + "'.", e);
                }
                JOptionPane.showMessageDialog(this, "Error while saving the file '" + currentFile.getPath() + "'.");
                logger.log(Level.SEVERE, "Error while saving the current file : '" + currentFile.getPath() + "'.", ex);
            }
        }
    }

    public void createHTMLView(Map<String, Score> allScores) {
        FileWriter out = null;
        try {
            if (htmlTempFile == null) {
                htmlTempFile = File.createTempFile("htmlView", ".html");
                htmlTempFile.deleteOnExit();
            }
            out = new FileWriter(htmlTempFile, false);
            out.write(ScoreWriter.renderHTML(allScores, false));
            out.flush();
            out.close();
            Document doc = htmlPane.getDocument();
            doc.putProperty(Document.StreamDescriptionProperty, null);
            htmlPane.setPage(htmlTempFile.toURI().toURL());
        } catch (IOException ex) {
            try {
                out.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while closing the current file : '" + htmlTempFile.getPath() + "'.", e);
            }
            JOptionPane.showMessageDialog(this, "Error while creating HTML view.");
            logger.log(Level.SEVERE, "Error while creating HTML View", ex);
        }
    }

    private JMenu createFileMenu() {
        JMenu menu = new JMenu(Messages.getString("Menu_File"));
        JMenuItem item = new JMenuItem(Messages.getString("MenuItem_Load"));
        ActionListener saveAction, saveAsAction, exitAction;
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                loadDocument();
            }
        });
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK, true));
        menu.add(item);
        item = new JMenuItem(Messages.getString("MenuItem_Save"));
        item.addActionListener(saveAction = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (currentFile == null) {
                    saveAsDocument();
                } else {
                    saveDocument();
                }
            }
        });
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK, true));
        menu.add(item);
        item = new JMenuItem(Messages.getString("MenuItem_SaveAs"));
        item.addActionListener(saveAsAction = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                saveAsDocument();
            }
        });
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem(Messages.getString("MenuItem_Check"));
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                checkAndComputeHTMLView();
            }
        });
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.CTRL_MASK, true));
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem(Messages.getString("MenuItem_HtmlView"));
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (htmlTempFile == null && !checkAndComputeHTMLView()) {
                    JOptionPane.showMessageDialog(ScoreEditor.this, Messages.getString("Msg_ParsingErrorSeeCodeTab"));
                }
                openURI(htmlTempFile.toURI());
            }
        });
        menu.add(item);
        item = new JMenuItem(Messages.getString("MenuItem_ExportHtml"));
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (htmlTempFile == null && !checkAndComputeHTMLView()) {
                    JOptionPane.showMessageDialog(ScoreEditor.this, Messages.getString("Msg_ParsingErrorSeeCodeTab"));
                } else {
                    saveHTMLView();
                }
            }
        });
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem(Messages.getString("MenuItem_Exit"));
        item.addActionListener(exitAction = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.exit(0);
            }
        });
        menu.add(item);
        return menu;
    }

    public void parsingError(String message) {
        StyledDocument doc = outputPane.getStyledDocument();
        outputPane.setText(message);
        doc.setCharacterAttributes(0, outputPane.getText().length(), outputPane.getStyle("Red"), true);
        htmlPane.setText(Messages.getString("Msg_ParsingErrorSeeCodeTab"));
        textPane.requestFocus();
    }

    public void parsingMessage(String message) {
        StyledDocument doc = outputPane.getStyledDocument();
        outputPane.setText(message);
        doc.setCharacterAttributes(0, outputPane.getText().length(), outputPane.getStyle("Green"), true);
    }

    private void goTo(int toLine, int toColumn) {
        try {
            int offset = 0;
            for (int line = 1; line < toLine; line++) {
                offset = Utilities.getRowEnd(textPane, offset) + 1;
            }
            offset += toColumn - 1;
            textPane.setCaretPosition(offset);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private boolean checkAndComputeHTMLView() {
        try {
            ModernRockParser parser = new ModernRockParser(new ByteArrayInputStream(textPane.getText().getBytes()));
            Map<String, Score> allScores = parser.parse();
            parsingMessage(Messages.getString("Msg_ParsingSuccessfull"));
            createHTMLView(allScores);
            return true;
        } catch (ParseException ex) {
            goTo(ex.currentToken.beginLine, ex.currentToken.beginColumn);
            parsingError(ex.toString());
        } catch (IllegalStateException ex) {
            parsingError(ex.toString());
        } catch (org.dance.parser.TokenMgrError error) {
            goTo(error.errorLine, error.errorColumn);
            parsingError(error.getMessage());
        }
        return false;
    }

    private JMenu createEditMenu() {
        JMenu menu = new JMenu(Messages.getString("Menu_Edit"));
        JMenuItem item = new JMenuItem(Messages.getString("MenuItem_Undo"));
        ActionListener cutAction, copyAction, pasteAction, undoAction, redoAction;
        item.addActionListener(undoAction = undoManager.getUndoAction());
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK, true));
        menu.add(item);
        item = new JMenuItem(Messages.getString("MenuItem_Redo"));
        item.addActionListener(redoAction = undoManager.getRedoAction());
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK, true));
        menu.add(item);
        item = new JMenuItem(Messages.getString("MenuItem_Cut"));
        item.addActionListener(cutAction = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                cut();
            }
        });
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK, true));
        menu.add(item);
        item = new JMenuItem(Messages.getString("MenuItem_Copy"));
        item.addActionListener(copyAction = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                copy();
            }
        });
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK, true));
        menu.add(item);
        item = new JMenuItem(Messages.getString("MenuItem_Paste"));
        item.addActionListener(pasteAction = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                paste();
            }
        });
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK, true));
        menu.add(item);
        ActionListener nullAction = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };
        textPane.registerKeyboardAction(nullAction, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
        textPane.registerKeyboardAction(nullAction, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
        textPane.registerKeyboardAction(nullAction, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
        return menu;
    }

    private JMenu createHelpMenu() {
        JMenu helpMenu = new JMenu(Messages.getString("Menu_Help"));
        helpMenu.setMnemonic('H');
        JMenuItem helpItem = helpMenu.add(Messages.getString("MenuItem_BrowseOnlineDoc"));
        helpItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    openURI(new URI("http://guidedudanseur.free.fr/pmwiki.php/Partitions.HomePage"));
                } catch (URISyntaxException ex) {
                    logger.log(Level.SEVERE, "error while opening help.", ex);
                }
            }
        });
        JMenuItem aboutItem = helpMenu.add(Messages.getString("MenuItem_About"));
        aboutItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent e) {
                AboutDialog dialog = new AboutDialog(ScoreEditor.this);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        });
        return helpMenu;
    }

    /**
	 * Initialize the document with some default text and set they initial type
	 * of syntax highlighting.
	 */
    private void initDocument() {
        String initString = "	T:[Flammes]\n" + "			S:{o* -s/ 'N'-f} !> !4!\n" + "				:1 {f0,25'Wc/ 'N'-f}\n" + " 				:2 {=>Cl / => 'N'-f(f0,5'@)}\n" + "				:3 { Cl / 'N'-f(f0,5'@)} F{ 3-4 2@'}\n" + "				:4 {Cl/ => 'C}\n" + "				!> { Cl / 'C} !!\n";
        textPane.setText(initString);
        document.setModified(false);
    }

    /**
	 * Run the demo.
	 * 
	 * @param args
	 *            ignored
	 */
    public static void main(String[] args) {
        ScoreEditor frame = new ScoreEditor();
    }
}

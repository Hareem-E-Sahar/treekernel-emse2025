import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.io.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;
import java.io.File;

/**
 * @ Main class for the Application.
 */
public class xes extends JFrame implements ActionListener {

    private Container con = getContentPane();

    public nodePackageDec baseNode;

    public String fileName = "";

    public int row = -1;

    private JMenuBar menuBar;

    private JMenu file, view, options, tabs;

    private mjbMenuItem sourceFileMenu, SymbolTableMenu, saveXesMenu, clearAllMenu, exitMenu, generatexesFileMenu, parserTreeMenu, xesFileMenu, transformMenu, batchMenu, aboutApplnMenu, versionMenu, aboutMenu;

    private JCheckBoxMenuItem statusbar, messages;

    /**  display title for whole xes program */
    private JLabel topLabel;

    /** right hand tabbed pane */
    public JTabbedPane jtp;

    /** left hand tabbed pane */
    private JTabbedPane left;

    /**
   * this tab displays a tree structure containg all the nodes in the program
   */
    public EncoderTree encTree;

    /**
   * this tab displays the symbol table which holds a token (integer value)
   * for each string in the program
   */
    public encoderTokeniser encToken;

    /**
   * this tab displays any error messages generated
   */
    public EncoderProperties EerrorMsg;

    /**
   * this tab displays the results of any XSLT transforms
   */
    public EncoderXes EResults;

    /**
   * this tab displays the properties for the node selected
   */
    public EncoderProperties propertiesEncoder;

    /**
   * this tab allows a source code file to be parsed and loaded
   */
    public EncoderLoad ELoad;

    /**
   * this tab allows many files to be transformed in a batch
   */
    public EncoderBatch EBatch;

    /**
   * this tab displays XSLT script and allows it to be displayed,read,
   * written and run
   */
    public encoderTransform Etransf;

    /**
   * tab for future expansion
   */
    public FileList fileList;

    /**
   * these literals define a name for tabs so if any new
   * tabs are inserted only these entries need to be changed
   */
    public static final int TAB_LEFT_SYMBOL = 0;

    public static final int TAB_LEFT_PARSER = 1;

    public static final int TAB_LEFT_TRANSFORM = 2;

    public static final int TAB_LEFT_FILE = 3;

    public static final int TAB_RIGHT_READ = 0;

    public static final int TAB_RIGHT_BATCH = 1;

    public static final int TAB_RIGHT_PROPERTIES = 2;

    public static final int TAB_RIGHT_RESULTS = 3;

    public static final int TAB_RIGHT_ERRORS = 4;

    public static String copyright = "\nTitle: xes (XML Encoded Source) " + "\nCopyright (c) 2004-2005 Martin John Baker" + "\n\nThis program is free software; you can redistribute it and/or" + "\nmodify it under the terms of the GNU General Public License" + "\nas published by the Free Software Foundation; either version 2" + "\nof the License, or (at your option) any later version." + "\n\nThis program is distributed in the hope that it will be useful," + "\nbut WITHOUT ANY WARRANTY; without even the implied warranty of" + "\nMERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the" + "\nGNU General Public License for more details." + "\n\nFor information about the GNU General Public License see http://www.gnu.org/" + "\n\nTo discuss this program http://sourceforge.net/forum/forum.php?forum_id=389154" + "\nalso see website http://www.euclideanspace.com/software/language/xes/";

    /**
 * Constructor sets up program
 */
    public xes() {
        try {
            addMenubar();
            addComponents();
            addListeners();
            addWindowProperties();
            nodeBase.mainClass = this;
        } catch (Exception e) {
            EerrorMsg.text.setText("xes.const " + e);
            jtp.setSelectedIndex(TAB_RIGHT_ERRORS);
            System.out.println("xes.init " + e);
        }
    }

    /**
 * Adds up a menubar to the application.
 */
    private void addMenubar() {
        menuBar = new JMenuBar();
        menuBar.add(addFileMenu());
        menuBar.add(addViewMenu());
        menuBar.add(addOptionsMenu());
        setJMenuBar(menuBar);
    }

    /**
 * @return a JMenu object having File menu.
 */
    private JMenu addFileMenu() {
        file = new JMenu("File");
        file.setMnemonic('F');
        sourceFileMenu = new mjbMenuItem("Select source file", 'B', this);
        clearAllMenu = new mjbMenuItem("Clear All", 'C', this);
        exitMenu = new mjbMenuItem("Exit", 'X', this);
        file.add(sourceFileMenu);
        file.addSeparator();
        file.add(clearAllMenu);
        file.addSeparator();
        file.add(exitMenu);
        return file;
    }

    /**
 * @return a JMenu to add the view menu to the Menubar.
 */
    private JMenu addViewMenu() {
        view = new JMenu("View");
        file.setMnemonic('V');
        statusbar = new JCheckBoxMenuItem("Status Bar", true);
        messages = new JCheckBoxMenuItem("Messages", true);
        view.add(statusbar);
        view.add(messages);
        view.add(addTabMenu());
        return view;
    }

    /**
 * @return a JMenu having a Tab sub menu.
 */
    private JMenu addTabMenu() {
        SymbolTableMenu = new mjbMenuItem("Symbol Table", 'R', this);
        parserTreeMenu = new mjbMenuItem("Parser tree", 'L', this);
        xesFileMenu = new mjbMenuItem("xes file", 'F', this);
        transformMenu = new mjbMenuItem("Transform", 'T', this);
        batchMenu = new mjbMenuItem("Batch", 'B', this);
        tabs = new JMenu("Tabs");
        file.setMnemonic('T');
        tabs.add(parserTreeMenu);
        tabs.add(batchMenu);
        tabs.add(xesFileMenu);
        tabs.add(SymbolTableMenu);
        tabs.add(transformMenu);
        return tabs;
    }

    /**
 * @return a JMenu to add the Options menu.
 */
    private JMenu addOptionsMenu() {
        options = new JMenu("Options");
        file.setMnemonic('O');
        generatexesFileMenu = new mjbMenuItem("generate xes", 'S', this);
        options.add(generatexesFileMenu);
        saveXesMenu = new mjbMenuItem("save xes", 'S', this);
        options.add(saveXesMenu);
        aboutMenu = new mjbMenuItem("about", 'A', this);
        options.add(aboutMenu);
        return options;
    }

    /**
 * Adds components.
 */
    private void addComponents() {
        GridLayout gl = new GridLayout();
        gl.setColumns(2);
        con.setLayout(gl);
        jtp = new JTabbedPane();
        topLabel = new JLabel("xes - XML encoded source", JLabel.CENTER);
        topLabel.setForeground(Color.gray);
        EBatch = new EncoderBatch(this, "Batch");
        ELoad = new EncoderLoad(this, "Read");
        jtp.addTab("Read", ELoad);
        jtp.validate();
        ELoad.setVisible(true);
        jtp.addTab("Batch", EBatch);
        jtp.validate();
        jtp.addTab("Properties", propertiesEncoder = new EncoderProperties(this, "Properties"));
        jtp.addTab("results", EResults = new EncoderXes(this, "results"));
        jtp.addTab("error messages", EerrorMsg = new EncoderProperties(this, "error messages"));
        left = new JTabbedPane();
        left.addTab("Tokeniser Symbol Table", encToken = new encoderTokeniser(this, "Tokeniser Symbol Table"));
        left.addTab("Parser tree", encTree = new EncoderTree(this, "Parser tree"));
        left.addTab("transform", Etransf = new encoderTransform(this, "transform"));
        left.addTab("File list", fileList = new FileList(this, "File list"));
        JPanel down = new JPanel();
        down.setLayout(new BorderLayout());
        con.add(left, null);
        con.add(jtp, null);
    }

    /** 
 * @ Adds Listeners to all the components.
 */
    private void addListeners() {
        addWindowListener(new MyWindowListener());
    }

    class MyWindowListener extends WindowAdapter {

        public void windowClosing(WindowEvent we) {
            System.exit(0);
        }
    }

    /**
* Adds Window Properties.
*/
    private void addWindowProperties() {
        setTitle("XML Encoded Source");
        setLocation(100, 50);
        setSize(800, 500);
        setVisible(true);
    }

    /**
 * Program execution starts from here.
 * @param args no arguments required
 */
    public static void main(String[] args) {
        try {
            xes jtxmlencoder = new xes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** this method required for ActionListener
 * @param ae ActionEvent object to get the events.
 */
    public void actionPerformed(ActionEvent ae) {
        try {
            nodeBase.mainClass = this;
            Object object = ae.getSource();
            if (object == exitMenu) {
                System.exit(0);
            } else if (object == parserTreeMenu) {
                jtp.setSelectedIndex(TAB_LEFT_SYMBOL);
            } else if (object == sourceFileMenu) {
                loadJavaSource();
            } else if (object == batchMenu) {
                jtp.setSelectedIndex(TAB_LEFT_PARSER);
            } else if (object == xesFileMenu) {
                jtp.setSelectedIndex(TAB_LEFT_FILE);
            } else if (object == transformMenu) {
                jtp.setSelectedIndex(TAB_LEFT_TRANSFORM);
            } else if (object == clearAllMenu) {
                EResults.text.setText("");
            } else if (object == aboutApplnMenu) {
                JOptionPane.showMessageDialog(aboutApplnMenu, copyright, "XML Encoder....", JOptionPane.INFORMATION_MESSAGE);
            } else if (object == versionMenu) {
                JOptionPane.showMessageDialog(null, copyright, "XML Encoded Source", JOptionPane.INFORMATION_MESSAGE);
            } else if (object == generatexesFileMenu) {
                generateAndSaveXES();
            } else if (object == saveXesMenu) {
                generateAndSaveXES();
            } else if (object == aboutMenu) {
                about();
            }
        } catch (Exception e) {
            EerrorMsg.text.setText("xes.actionPerformed " + e);
            jtp.setSelectedIndex(TAB_RIGHT_ERRORS);
        }
    }

    /** called when data has changed, for instance if a file has been loaded */
    public void dataChanged() {
        if (encTree != null) encTree.refresh();
        TableModelEvent tme = new TableModelEvent((TableModel) new symbolTable());
        ((encoderTokeniser) encToken).table.tableChanged(tme);
    }

    /**
 * when a row is selected we want to display its value in the properties window
 * @param r identifies row
 */
    public void rowSelected(int r) {
        row = r;
        jtp.setSelectedIndex(TAB_RIGHT_PROPERTIES);
    }

    public void textChanged(String s) {
        if (symbolTable.table == null) return;
        if (row >= 0) symbolTable.table[row] = s;
    }

    public void buttonSourceDir() {
        File file = new File(EBatch.textSourceDir.getText());
        fileNameFilter fileFilter = new fileNameFilter(EBatch.cmbSourceFileFilter.getSelectedItem().toString());
        String[] files = file.list(fileFilter);
        String text = new String("");
        for (int i = 0; i < files.length; i++) text = text + EBatch.textSourceDir.getText() + "/" + files[i] + '\n';
        fileList.text.setText("");
        fileList.text.setText(text);
        left.setSelectedIndex(TAB_RIGHT_ERRORS);
    }

    /**
 * called when button pressed in load tab or menu to load
 * a Java source code file.
 * 
 * for information about parsing see here:
 * http://www.euclideanspace.com/software/language/xes/programmersGuide/parsing/
 */
    public void loadJavaSource() {
        try {
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            if (EncoderLoad.directory == null) EncoderLoad.directory = new File(System.getProperty("user.dir"));
            fileChooser.setCurrentDirectory(EncoderLoad.directory);
            fileChooser.setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
            int returnvalue = fileChooser.showDialog(null, "Load");
            if (returnvalue == javax.swing.JFileChooser.APPROVE_OPTION) {
                ((EncoderLoad) (ELoad)).setBaseDir(fileChooser.getCurrentDirectory());
                ((EncoderLoad) (ELoad)).setBase(fileChooser.getSelectedFile());
                File f = fileChooser.getSelectedFile();
                JavaParser parser = new JavaParser(f.getAbsolutePath());
                try {
                    SimpleNode.vsymbolTable = new Vector();
                    for (int i = 0; i < JavaParserConstants.tokenImage.length; i++) {
                        String im = JavaParserConstants.tokenImage[i];
                        if (im.charAt(0) == '\"') im = im.substring(1, im.length() - 1);
                        SimpleNode.vsymbolTable.addElement(im);
                    }
                    baseNode = parser.CompilationUnit();
                    symbolTable.table = new String[SimpleNode.vsymbolTable.size()];
                    SimpleNode.vsymbolTable.toArray(symbolTable.table);
                    if (encTree != null) encTree.setModel(baseNode);
                } catch (ParseException e) {
                    EerrorMsg.text.setText("xes.loadJavaSource errors during parse  " + e);
                    jtp.setSelectedIndex(TAB_RIGHT_ERRORS);
                }
            }
            dataChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
 * called when button pressed in load tab or menu to load
 * a C# source code file.
 * 
 * for information about parsing see here:
 * http://www.euclideanspace.com/software/language/xes/programmersGuide/parsing/
 */
    public void loadCsSource() {
        try {
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            if (EncoderLoad.directory == null) EncoderLoad.directory = new File(System.getProperty("user.dir"));
            fileChooser.setCurrentDirectory(EncoderLoad.directory);
            fileChooser.setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
            int returnvalue = fileChooser.showDialog(null, "Load");
            if (returnvalue == javax.swing.JFileChooser.APPROVE_OPTION) {
                ((EncoderLoad) (ELoad)).setBaseDir(fileChooser.getCurrentDirectory());
                ((EncoderLoad) (ELoad)).setBase(fileChooser.getSelectedFile());
                File f = fileChooser.getSelectedFile();
                csParser parser = new csParser(f.getAbsolutePath());
                try {
                    SimpleNode.vsymbolTable = new Vector();
                    for (int i = 0; i < JavaParserConstants.tokenImage.length; i++) {
                        String im = JavaParserConstants.tokenImage[i];
                        if (im.charAt(0) == '\"') im = im.substring(1, im.length() - 1);
                        SimpleNode.vsymbolTable.addElement(im);
                    }
                    baseNode = parser.CompilationUnit();
                    symbolTable.table = new String[SimpleNode.vsymbolTable.size()];
                    SimpleNode.vsymbolTable.toArray(symbolTable.table);
                    if (encTree != null) encTree.setModel(baseNode);
                } catch (ParseException e) {
                    EerrorMsg.text.setText("xes.loadCsSource errors during parse  " + e);
                    jtp.setSelectedIndex(TAB_RIGHT_ERRORS);
                }
            }
            dataChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
 * display information about program
 */
    public void about() {
        try {
            EerrorMsg.text.setText(copyright);
            jtp.setSelectedIndex(TAB_RIGHT_ERRORS);
        } catch (Exception e) {
            System.out.println("xes.about " + e);
        }
    }

    /**
 * load from xes file
 *
 */
    public void loadXesSource() {
        try {
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            if (EncoderLoad.directory == null) EncoderLoad.directory = new File(System.getProperty("user.dir"));
            fileChooser.setCurrentDirectory(EncoderLoad.directory);
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            int returnvalue = fileChooser.showDialog(null, "Load");
            if (returnvalue == javax.swing.JFileChooser.APPROVE_OPTION) {
                ((EncoderLoad) (ELoad)).setBaseDir(fileChooser.getCurrentDirectory());
                ELoad.setBase(fileChooser.getSelectedFile());
                Vector vsymbolTable = new Vector();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(fileChooser.getSelectedFile());
                org.w3c.dom.Node root = doc.getFirstChild();
                if (baseNode == null) {
                    baseNode = new nodePackageDec(JavaParserTreeConstants.JJTNODEPACKAGEDEC);
                }
                SimpleNode.vsymbolTable = new Vector();
                for (int i = 0; i < JavaParserConstants.tokenImage.length; i++) {
                    String im = JavaParserConstants.tokenImage[i];
                    if (im.charAt(0) == '\"') im = im.substring(1, im.length() - 1);
                    SimpleNode.vsymbolTable.addElement(im);
                }
                baseNode.loadfromDom(root, SimpleNode.vsymbolTable);
                symbolTable.table = new String[SimpleNode.vsymbolTable.size()];
                SimpleNode.vsymbolTable.toArray(symbolTable.table);
            }
            if (encTree != null) encTree.setModel(baseNode);
            dataChanged();
        } catch (Exception e) {
            System.out.println("xes.loadXesSource " + e);
        }
    }

    /**
 * called by encoderTransform.transform()
 * 
 * This generates a DOM tree from the internal representation of nodes
 * this is not very efficient because it duplicated the whole tree
 *  the DOM tree is used as input to XSLT transform
 * 
 * @return dom document
 */
    public org.w3c.dom.Document generateDomTree() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = db.newDocument();
            if (baseNode == null) {
                System.out.println("xes.generateDomTree baseNode == null");
                return null;
            }
            org.w3c.dom.Element root = baseNode.generateDomTree(doc);
            doc.appendChild(root);
            return doc;
        } catch (Exception e) {
            EerrorMsg.text.setText("xes.generateDomTree " + e);
            jtp.setSelectedIndex(TAB_RIGHT_ERRORS);
            return null;
        }
    }

    /**
 * outputs to XES by using canned XSLT script built into this method
 * 
 * this method uses the javax.xml.transform class library.
 * The main stages are, first create a Transformer from the xsl file:
 *       Transformer xform = tfactory.newTransformer(new StreamSource(is));
 * Then create a dom tree from the nodes stored in the program:
 *       DOMSource ds=new DOMSource(root);
 * Then run the transform:
 *       xform.transform(ds,new StreamResult(outFile));
 * It is very inefficient because it needs to create a dom tree which
 * duplicates all the information in the nodes, if you can think of a
 * way to avoid creating this dom tree I would be very interested.
 * 
 * For mor information about running XSLT script see here:
 * http://www.euclideanspace.com/software/language/xes/programmersGuide/translating/
 */
    public void generateAndSaveXES() {
        if (baseNode == null) {
            EerrorMsg.text.setText("xes.source not loaded ");
            jtp.setSelectedIndex(TAB_RIGHT_ERRORS);
            return;
        }
        try {
            org.w3c.dom.Document doc = generateDomTree();
            if (doc == null) {
                System.out.println("no program loaded ");
                return;
            }
            String xsltStr = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">" + "<xsl:output method=\"xml\" indent=\"yes\"/>" + "<xsl:template match=\"node() | @*\">" + "<xsl:copy>" + "<xsl:apply-templates select=\"@* | node()\"/>" + "</xsl:copy>" + "</xsl:template>" + "</xsl:stylesheet>";
            byte buf[] = xsltStr.getBytes();
            ByteArrayInputStream is = new ByteArrayInputStream(buf);
            TransformerFactory tfactory = TransformerFactory.newInstance();
            Transformer xform = tfactory.newTransformer(new StreamSource(is));
            JFileChooser jfc = new JFileChooser();
            jfc.setCurrentDirectory(EncoderLoad.directory);
            int retValue = jfc.showSaveDialog(this);
            File outFile;
            if (retValue == JFileChooser.APPROVE_OPTION) {
                ((EncoderLoad) (ELoad)).setBaseDir(jfc.getCurrentDirectory());
                outFile = jfc.getSelectedFile();
                org.w3c.dom.Node root = doc.getFirstChild();
                DOMSource ds = new DOMSource(root);
                xform.transform(ds, new StreamResult(outFile));
            }
        } catch (Exception e) {
            System.out.println("\nxes.encodingMethod " + e);
        }
    }
}

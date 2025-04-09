package aurora.hwc.config;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.filechooser.FileFilter;
import aurora.AuroraConstants;
import aurora.hwc.common.WindowAbout;
import aurora.util.JFileFilter;
import aurora.util.UtilGUI;

/**
 * GISImporter:
 * - Open shp file
 * - Filter by type
 * - Remove reduntant edges
 * - Export to aurora 
 * Convert GIS shape file to Aurora configuration XML files.
 * @author Jaimyoung Kwon
 * @version $Id: GISImporter.java 38 2010-02-08 22:59:00Z akurzhan $
 */
public class GISImporter extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final String separator = "=======================================\n";

    private File oldFile = null;

    private Container content;

    private JLabel statusBar = new JLabel(" ");

    private JTextArea logText = null;

    private GISObject gisObject;

    private File currentDir = new File(AuroraConstants.DEFAULT_HOME);

    private static String myTitle = "Aurora RNM GIS Importer";

    /**
	 * Constructor which takes care of most steps
	 */
    public GISImporter(String[] args) throws Exception {
        super(myTitle);
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        content = getContentPane();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(createMenuBar());
        createButtons();
        JPanel logPanel = new JPanel();
        logPanel.setBorder(BorderFactory.createTitledBorder("LOG"));
        logText = new JTextArea();
        logText.setFont(new Font("Helvetica", Font.PLAIN, 11));
        JScrollPane scroller = new JScrollPane(logText);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        myLog("Log of Aurora RNM GIS Importer\n");
        logPanel.add(scroller, BorderLayout.CENTER);
        scroller.setPreferredSize(new Dimension(780, 460));
        content.add(logPanel, BorderLayout.CENTER);
        content.add(statusBar, BorderLayout.SOUTH);
        setSize(800, 600);
        ImageIcon icon = UtilGUI.createImageIcon("/icons/gislogo2.jpg");
        if (icon != null) setIconImage(icon.getImage());
        setVisible(true);
        gisObject = new GISObject(this);
        myLog("Starting...");
        timestamp();
        if (false) {
            oldFile = new File("C:\\WORK\\devel\\support\\SanPablo_Aurora\\cacontst_west.shp");
            openFile(oldFile.toURI().toURL());
        }
    }

    public static void main(String[] args) throws Exception {
        new GISImporter(args);
    }

    /**
	 * read in the input shape file into native GIS feature collection
	 */
    private void openFile(URL shape) {
        setTitle(myTitle);
        myLog(separator + "Opening file...");
        JFileFilter filter = new JFileFilter();
        filter.addType("shp");
        filter.setDescription("Shape files (*.shp)");
        JFileChooser fc = new JFileChooser("Open File");
        fc.setFileFilter(filter);
        fc.setCurrentDirectory(currentDir);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                if (shape == null) {
                    oldFile = fc.getSelectedFile();
                    shape = oldFile.toURI().toURL();
                }
                if (shape == null) return;
                gisObject.openShapefile(shape);
            } catch (Exception e) {
                currentDir = fc.getCurrentDirectory();
                e.printStackTrace();
                return;
            }
            setTitle(myTitle + " - " + oldFile);
            gisObject.debug();
            buttonOpen.setEnabled(true);
            buttonFilterType.setEnabled(true);
            buttonFilterName.setEnabled(true);
            buttonFilterSimplify.setEnabled(true);
            buttonSaveConfig.setEnabled(true);
            timestamp();
        }
        currentDir = fc.getCurrentDirectory();
        return;
    }

    /**
	 * step 2: Type filter
	 */
    private void typeFilter() {
        myLog(separator + "Filtering by road types...");
        if (gisObject.getFeatureCollection() == null) {
            myLog("featureCollection is empty!");
            return;
        }
        GISImporterTypeWindow vw = new GISImporterTypeWindow(this, true, gisObject.getFeatureCollection());
        if (!vw.getAnswer()) {
            myLog("Operation cancelled.");
            return;
        }
        String attributeName = vw.getAttributeName();
        Object[] avs = vw.getAttributeValues();
        if (vw.getValueText() != null && vw.getValueText().length() > 0) {
            avs = vw.getValueText().split(",");
        }
        if (avs.length < 1) return;
        gisObject.typeFilter(attributeName, avs);
        timestamp();
    }

    /**
	 * Name filter
		// TO-DO: make the following info available to users: 
		// The file should contain capitalized street name (without suffixes St. Ave. Rd. etc)
		// cf. san_pablo_ints.dat
	 */
    private void nameFilter() {
        myLog(separator + "Filtering by road names...");
        if (gisObject.getFeatureCollection() == null) {
            myLog("featureCollection is empty!");
            return;
        }
        File file = null;
        try {
            file = fileDialog(null);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }
        if (file == null) {
            myLog("Operation cancelled.");
            return;
        }
        String filename = file.getAbsolutePath();
        myLog("Filtering by NAME values in: " + filename);
        ArrayList<String> avs = new ArrayList<String>();
        String thisLine;
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            while ((thisLine = br.readLine()) != null) {
                avs.add(thisLine);
                myLog("> " + thisLine);
            }
        } catch (IOException e) {
            System.err.println("Error: " + e);
            return;
        }
        gisObject.typeFilter("NAME", avs.toArray());
        timestamp();
    }

    /**
	 * step 3: simplifying edges
	 */
    private void simplifyEdges() {
        myLog(separator + "Simplifying edges...");
        gisObject.simplifyEdges();
        buttonFilterSimplify.setEnabled(false);
        buttonFilterName.setEnabled(false);
        buttonFilterType.setEnabled(false);
        myLog("For another edge-simplification, re-open the GIS file");
        timestamp();
    }

    /**
	 * step 4: exporting to aurora XML (can borrow Alex' codes?) 
	 */
    private void exportToXML() {
        myLog(separator + "Exporting to XML...");
        if (gisObject.getFeatureCollection() == null) {
            myLog("featureCollection is empty!");
            return;
        }
        File newFile = getNewXMLFile(oldFile);
        if (newFile == null) {
            myLog("Exporting operation cancelled!");
            return;
        }
        gisObject.exportToXML(newFile);
        timestamp();
    }

    /**
	 * Export to shape file 
	 * cf. http://osdir.com/ml/gis.geotools2.user/2006-07/msg00060.html
	 */
    private void exportToGIS() {
        myLog(separator + "Exporting to GIS...");
        if (gisObject.getFeatureCollection() == null) {
            myLog("featureCollection is empty!");
            return;
        }
        File newFile = getNewShapeFile(oldFile, 2);
        if (newFile == null) {
            myLog("GIS Export Canceled");
            return;
        }
        gisObject.exportToGIS(newFile);
        timestamp();
    }

    /**
	 * This method prompts the user for an XML file
	 * 
	 * @param file2
	 * @return
	 */
    private File getNewXMLFile(File file2) {
        String path = file2.getAbsolutePath();
        String newPath = path.substring(0, path.length() - 4) + ".xml";
        JFileChooser chooser = new JFileChooser("Save XML file");
        chooser.setCurrentDirectory(currentDir);
        chooser.setSelectedFile(new File(newPath));
        chooser.setFileFilter(new FileFilter() {

            public boolean accept(File f) {
                return f.isDirectory() || f.getPath().endsWith("xml") || f.getPath().endsWith("XML");
            }

            public String getDescription() {
                return "XML file";
            }
        });
        int returnVal = chooser.showSaveDialog(null);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File newFile = chooser.getSelectedFile();
        currentDir = chooser.getCurrentDirectory();
        return newFile;
    }

    /**
	 * This method will prompt the user for a shape file.
	 * 
	 * @return url to selected shape file.
	 * @throws MalformedURLException
	 */
    private File fileDialog(FileNameExtensionFilter filter) throws MalformedURLException {
        File file = null;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(currentDir);
        if (filter != null) {
            fileChooser.setFileFilter(filter);
        }
        fileChooser.setSelectedFile(new File("."));
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        }
        currentDir = fileChooser.getCurrentDirectory();
        return file;
    }

    /**
	 * This method prompts the user for a new GIS shape file name
	 * 
	 * @param file
	 * @param level
	 * @return
	 */
    private File getNewShapeFile(File file, Integer level) {
        String path = null;
        String newPath = null;
        if (file != null) {
            path = file.getAbsolutePath();
            newPath = path.substring(0, path.length() - 4) + level + ".shp";
        }
        JFileChooser chooser = new JFileChooser("Save shape file");
        chooser.setCurrentDirectory(currentDir);
        chooser.setSelectedFile(new File(newPath));
        chooser.setFileFilter(new FileFilter() {

            public boolean accept(File f) {
                return f.isDirectory() || f.getPath().endsWith("shp") || f.getPath().endsWith("SHP");
            }

            public String getDescription() {
                return "Shape files";
            }
        });
        int returnVal = chooser.showSaveDialog(null);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File newFile = chooser.getSelectedFile();
        if (newFile.equals(file)) {
            System.out.println("Cannot replace " + file);
            return null;
        }
        setTitle(myTitle + " - " + newFile);
        currentDir = chooser.getCurrentDirectory();
        return newFile;
    }

    /**
	 * put the current timestamp in the log window
	 */
    private void timestamp() {
        String datestr = (new Date()).toString();
        logText.append("(" + datestr + ")" + "\n");
        logText.setCaretPosition(logText.getText().length());
    }

    /**
	 * send log message to the log screen
	 */
    private void myLog(String logmessage) {
        logText.append(logmessage + "\n");
        logText.setCaretPosition(logText.getText().length());
    }

    private HashMap<String, JMenuItem> cmd2item = new HashMap<String, JMenuItem>();

    private static final String cmdFileNewSimulation = "FileNewSimulation";

    private static final String cmdFileOpenFile = "FileOpenFile";

    private static final String cmdFileSaveConfiguration = "FileSaveConfiguration";

    private static final String cmdFileSaveAs = "FileSaveAs";

    private static final String cmdFileExit = "FileExit";

    private static final String cmdViewZoomIn = "ViewZoomIn";

    private static final String cmdViewZoomOut = "ViewZoomOut";

    private static final String cmdFilterType = "FilterType";

    private static final String cmdFilterName = "FilterName";

    private static final String cmdFilterSimplify = "FilterSimplify";

    private static final String cmdHelpAbout = "HelpAbout";

    private static final String cmdHelpContactTOPL = "HelpContactTOPL";

    private JButton buttonOpen;

    private JButton buttonFilterType;

    private JButton buttonFilterName;

    private JButton buttonFilterSimplify;

    private JButton buttonSaveConfig;

    /**
	 * create buttons
	 */
    private void createButtons() {
        Font font = new Font("SansSerif", Font.BOLD, 16);
        JPanel controls = new JPanel();
        buttonOpen = new JButton("Open GIS File");
        buttonOpen.setActionCommand(cmdFileOpenFile);
        buttonOpen.addActionListener(this);
        controls.add(buttonOpen);
        buttonOpen.setFont(font);
        buttonFilterType = new JButton("Type Filter...");
        buttonFilterType.setActionCommand(cmdFilterType);
        buttonFilterType.addActionListener(this);
        controls.add(buttonFilterType);
        buttonFilterType.setFont(font);
        buttonFilterName = new JButton("Name Filter...");
        buttonFilterName.setActionCommand(cmdFilterName);
        buttonFilterName.addActionListener(this);
        controls.add(buttonFilterName);
        buttonFilterName.setFont(font);
        buttonFilterSimplify = new JButton("Simplify Edges...");
        buttonFilterSimplify.setActionCommand(cmdFilterSimplify);
        buttonFilterSimplify.addActionListener(this);
        controls.add(buttonFilterSimplify);
        buttonFilterSimplify.setFont(font);
        buttonSaveConfig = new JButton("Save as XML...");
        buttonSaveConfig.setActionCommand(cmdFileSaveConfiguration);
        buttonSaveConfig.addActionListener(this);
        controls.add(buttonSaveConfig);
        buttonSaveConfig.setFont(font);
        JButton buttonSaveAs = new JButton("Save as GIS...");
        buttonSaveAs.setActionCommand(cmdFileSaveAs);
        buttonSaveAs.addActionListener(this);
        buttonSaveAs.setFont(font);
        GridLayout experimentLayout = new GridLayout(1, 5);
        controls.setLayout(experimentLayout);
        controls.setPreferredSize(new Dimension(800, 40));
        buttonFilterType.setEnabled(false);
        buttonFilterName.setEnabled(false);
        buttonFilterSimplify.setEnabled(false);
        buttonSaveConfig.setEnabled(false);
        controls.setBackground(Color.DARK_GRAY);
        content.add(controls, BorderLayout.NORTH);
    }

    /**
	 * Creates menu bar.
	 * @return menu bar.
	 */
    private JMenuBar createMenuBar() {
        JMenuItem item;
        JMenu menu;
        JMenuBar mainMenu = new JMenuBar();
        menu = new JMenu("File");
        item = new JMenuItem("New Configuration");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
        item.addActionListener(this);
        item.setEnabled(true);
        item.setActionCommand(cmdFileNewSimulation);
        cmd2item.put(cmdFileNewSimulation, item);
        item = new JMenuItem("Open GIS...");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        item.addActionListener(this);
        item.setActionCommand(cmdFileOpenFile);
        cmd2item.put(cmdFileOpenFile, item);
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem("Save As XML...");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        item.addActionListener(this);
        item.setEnabled(true);
        item.setActionCommand(cmdFileSaveConfiguration);
        cmd2item.put(cmdFileSaveConfiguration, item);
        menu.add(item);
        item = new JMenuItem("Save As GIS...");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        item.addActionListener(this);
        item.setEnabled(true);
        item.setActionCommand(cmdFileSaveAs);
        cmd2item.put(cmdFileSaveAs, item);
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem("Exit");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
        item.addActionListener(this);
        item.setActionCommand(cmdFileExit);
        cmd2item.put(cmdFileExit, item);
        menu.add(item);
        mainMenu.add(menu);
        menu = new JMenu("View");
        item = new JMenuItem("Zoom In");
        item.setAccelerator(KeyStroke.getKeyStroke('+'));
        item.addActionListener(this);
        item.setActionCommand(cmdViewZoomIn);
        cmd2item.put(cmdViewZoomIn, item);
        menu.add(item);
        item = new JMenuItem("Zoom Out");
        item.setAccelerator(KeyStroke.getKeyStroke('-'));
        item.addActionListener(this);
        item.setActionCommand(cmdViewZoomOut);
        cmd2item.put(cmdViewZoomOut, item);
        menu.add(item);
        menu = new JMenu("Filter");
        item = new JMenuItem("Type Filter...");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
        item.addActionListener(this);
        item.setActionCommand(cmdFilterType);
        cmd2item.put(cmdFilterType, item);
        menu.add(item);
        item = new JMenuItem("Name Filter...");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.ALT_MASK));
        item.addActionListener(this);
        item.setActionCommand(cmdFilterName);
        cmd2item.put(cmdFilterName, item);
        menu.add(item);
        item = new JMenuItem("Simplify Edges...");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.ALT_MASK));
        item.addActionListener(this);
        item.setActionCommand(cmdFilterSimplify);
        cmd2item.put(cmdFilterSimplify, item);
        menu.add(item);
        mainMenu.add(menu);
        menu = new JMenu("Help");
        item = new JMenuItem("About");
        item.addActionListener(this);
        item.setActionCommand(cmdHelpAbout);
        cmd2item.put(cmdHelpAbout, item);
        menu.add(item);
        item = new JMenuItem("Contact TOPL");
        item.addActionListener(this);
        item.setActionCommand(cmdHelpContactTOPL);
        cmd2item.put(cmdHelpContactTOPL, item);
        menu.add(item);
        mainMenu.add(menu);
        return mainMenu;
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmdFileOpenFile.equals(cmd)) {
            openFile(null);
            return;
        }
        if (cmdFilterType.equals(cmd)) {
            typeFilter();
            return;
        }
        if (cmdFilterName.equals(cmd)) {
            nameFilter();
            return;
        }
        if (cmdFilterSimplify.equals(cmd)) {
            simplifyEdges();
            return;
        }
        if (cmdFileSaveAs.equals(cmd)) {
            exportToGIS();
            return;
        }
        if (cmdFileSaveConfiguration.equals(cmd)) {
            exportToXML();
            return;
        }
        if (cmdHelpAbout.equals(cmd)) {
            new WindowAbout("GISImporter", this);
            return;
        }
        if (cmdHelpContactTOPL.equals(cmd)) {
            Desktop desktop = null;
            String email = AuroraConstants.CONTACT_EMAIL;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.MAIL)) try {
                    desktop.mail(new URI("mailto", email, null));
                    return;
                } catch (Exception exp) {
                }
            }
            JOptionPane.showMessageDialog(this, "Cannot launch email client...\n Please, email your questions to\n" + email, "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (cmdFileExit.equals(cmd)) {
            statusBar.setText("Exiting...");
            setVisible(false);
            dispose();
            System.exit(0);
        }
    }

    public JTextArea getLogText() {
        return logText;
    }
}

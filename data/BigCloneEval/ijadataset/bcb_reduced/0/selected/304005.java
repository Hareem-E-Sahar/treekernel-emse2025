package org.gdbi.pgv.importer;

import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import org.gdbi.api.*;
import org.gdbi.db.pgvcgi.*;
import org.gdbi.util.parse.RawdbDatabase;
import org.gdbi.util.list.UListControl;
import org.gdbi.util.list.UListStarter;
import org.gdbi.pgv.gedcomSax.gedcomSaxParser;
import javax.swing.JButton;

/**
 * @author jfinlay
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ImporterGUI extends JFrame {

    public static final String VERSION = "1.0 alpha";

    private File gedcomfile;

    private Vector<OBJE> objects;

    private String relativePath;

    private CgiAccess remoteConn;

    private ImporterGUI ME;

    private String Url;

    private String username;

    private String password;

    private GdbiDatabase gdbigedcom;

    private int mediaLevels;

    private String mediaDirectory;

    private javax.swing.JPanel jContentPane = null;

    private javax.swing.JMenuBar jJMenuBar = null;

    private javax.swing.JMenu fileMenu = null;

    private javax.swing.JMenu helpMenu = null;

    private javax.swing.JMenuItem exitMenuItem = null;

    private javax.swing.JMenuItem aboutMenuItem = null;

    private JMenuItem openMenuItem = null;

    private JLabel gedcomLabel = null;

    private JScrollPane jScrollPane = null;

    private JList objeList = null;

    private JScrollPane detailsScrollPane = null;

    private JPanel jPanel = null;

    private JLabel imageLabel = null;

    private JTextArea detailsLabel = null;

    private JScrollPane jScrollPane1 = null;

    private JMenuItem zipMenuItem = null;

    private JMenuItem openRemoteMenuItem = null;

    private JScrollPane jScrollPane2 = null;

    private JTextArea logTextArea = null;

    private JPanel jPanel1 = null;

    private JButton uploadButton = null;

    private JButton selectAllButton = null;

    /**
	 * This is the default constructor
	 */
    public ImporterGUI() {
        super();
        objects = new Vector<OBJE>();
        relativePath = "";
        initialize();
        ME = this;
    }

    /**
	 * Constructor for GDBI Main Window
	 */
    public ImporterGUI(GdbiDatabase ged) {
        super();
        objects = new Vector<OBJE>();
        relativePath = "";
        initialize();
        ME = this;
        gdbigedcom = ged;
        String dbtype = ged.getDBType();
        if (dbtype != null) {
            if (dbtype.equals("phpGedView")) {
                remoteConn = ((PgvcgiDatabase) (ged.getGdbiIntrDatabase())).getCgiAccess();
                Url = remoteConn.getBaseUrl();
                String levelsStr = remoteConn.actionGetvar("MEDIA_DIRECTORY_LEVELS");
                mediaDirectory = remoteConn.actionGetvar("MEDIA_DIRECTORY");
                mediaLevels = Integer.parseInt(levelsStr);
                gedcomLabel.setText("Remote Gedcom: " + remoteConn.actionGetvar("GEDCOM"));
                zipMenuItem.setEnabled(false);
                uploadButton.setEnabled(true);
                relativePath = "";
                objects.removeAllElements();
                log("Searching for media items...");
                remoteThread worker = new remoteThread(ME);
                worker.start();
            }
            if (dbtype.equals("Text")) {
                gedcomfile = ((RawdbDatabase) (ged.getGdbiIntrDatabase())).getFile();
                openGedcomFile(gedcomfile);
            }
        }
    }

    /**
	 * This method initializes this
	 * 
	 * @return void
	 */
    private void initialize() {
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/org/gdbi/pgv/importer/clippings.gif")));
        this.setJMenuBar(getJJMenuBar());
        this.setSize(640, 457);
        this.setContentPane(getJContentPane());
        this.setTitle("PGV Importer " + VERSION);
    }

    /**
	 * This method initializes jMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
    private JMenuItem getOpenMenuItem() {
        if (openMenuItem == null) {
            openMenuItem = new JMenuItem();
            openMenuItem.setText("Open Gedcom File...");
            openMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.showOpenDialog(null);
                    File chosenFile = fileChooser.getSelectedFile();
                    openGedcomFile(chosenFile);
                }
            });
        }
        return openMenuItem;
    }

    /**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setPreferredSize(new java.awt.Dimension(250, 250));
            jScrollPane.setViewportView(getObjeList());
        }
        return jScrollPane;
    }

    /**
	 * This method initializes jList
	 * 
	 * @return javax.swing.JList
	 */
    private JList getObjeList() {
        if (objeList == null) {
            objeList = new JList();
            objeList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {

                public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                    OBJE obj = (OBJE) objeList.getSelectedValue();
                    if (obj != null) {
                        imageLabel.setIcon(new ImageIcon(obj.getFilepath()));
                        String fpath = obj.getFilepath();
                        String origPath = fpath;
                        int hasDrive = fpath.indexOf(":\\");
                        if (hasDrive == -1) hasDrive = fpath.indexOf(":/");
                        String firstChar = fpath.substring(0, 1);
                        if ((hasDrive == -1) && (!firstChar.equals("/"))) fpath = relativePath + fpath;
                        File test = new File(fpath);
                        int imgHeight = 0;
                        int imgWidth = 0;
                        boolean showImage = true;
                        if ((fpath.indexOf("http") != 0) && (!test.exists())) {
                            if (remoteConn != null) {
                                fpath = Url.replaceAll("client.php", "") + mediaDirectory + uploadWorker.extract_filename(fpath, mediaLevels);
                                showImage = true;
                            } else {
                                imageLabel.setText("File not found in location.");
                                showImage = false;
                            }
                        }
                        if (showImage) {
                            imageLabel.setText("");
                            ImageIcon large = null;
                            try {
                                large = new ImageIcon(new URL(fpath.replaceAll(" ", "%20")));
                            } catch (MalformedURLException me) {
                            }
                            if (large == null) large = new ImageIcon(fpath);
                            ImageIcon resized = large;
                            if (large != null) {
                                final int maxHeight = imageLabel.getHeight();
                                final int maxWidth = imageLabel.getWidth();
                                imgHeight = large.getIconHeight();
                                imgWidth = large.getIconWidth();
                                if (imgHeight > maxHeight) {
                                    resized = new ImageIcon(large.getImage().getScaledInstance(-1, maxHeight, 0));
                                }
                            }
                            imageLabel.setIcon(resized);
                            if (resized == null) imageLabel.setText("Unsupported media format.");
                        }
                        String details = fpath;
                        if (!fpath.equals(origPath)) {
                            details = details + "\r\nOriginal path: " + origPath;
                        }
                        String title = obj.getTitle();
                        if (!title.equals("")) details = details + "\r\n" + title;
                        if (imgHeight > 0) details = details + "\r\nSize: " + imgWidth + "x" + imgHeight;
                        detailsLabel.setText(details);
                    }
                }
            });
        }
        return objeList;
    }

    /**
	 * This method initializes jScrollPane1
	 * 
	 * @return javax.swing.JScrollPane
	 */
    private JScrollPane getDetailsScrollPane() {
        if (detailsScrollPane == null) {
            detailsScrollPane = new JScrollPane();
            detailsScrollPane.setPreferredSize(new java.awt.Dimension(200, 150));
            detailsScrollPane.setViewportView(getJPanel());
        }
        return detailsScrollPane;
    }

    /**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(new FlowLayout());
            jPanel.setPreferredSize(new java.awt.Dimension(370, 210));
            imageLabel = new JLabel();
            imageLabel.setPreferredSize(new java.awt.Dimension(370, 220));
            imageLabel.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
            imageLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            imageLabel.setText("");
            jPanel.add(imageLabel, null);
            jPanel.add(getJScrollPane1(), null);
        }
        return jPanel;
    }

    /**
	 * This method initializes jLabel
	 * 
	 * @return javax.swing.JLabel
	 */
    private JTextArea getDetailsLabel() {
        if (detailsLabel == null) {
            detailsLabel = new JTextArea();
            detailsLabel.setText("Select a item from the list to the left.");
            detailsLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            detailsLabel.setLineWrap(false);
            detailsLabel.setWrapStyleWord(true);
            detailsLabel.setEditable(false);
        }
        return detailsLabel;
    }

    /**
	 * This method initializes jScrollPane1
	 * 
	 * @return javax.swing.JScrollPane
	 */
    private JScrollPane getJScrollPane1() {
        if (jScrollPane1 == null) {
            jScrollPane1 = new JScrollPane();
            jScrollPane1.setViewportView(getDetailsLabel());
            jScrollPane1.setPreferredSize(new java.awt.Dimension(370, 64));
        }
        return jScrollPane1;
    }

    /**
	 * This method initializes jMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
    private JMenuItem getZipMenuItem() {
        if (zipMenuItem == null) {
            zipMenuItem = new JMenuItem();
            zipMenuItem.setText("Create PGV Zip file...");
            zipMenuItem.setEnabled(false);
            zipMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser(relativePath);
                    fileChooser.showSaveDialog(null);
                    File chosenFile = fileChooser.getSelectedFile();
                    try {
                        byte b[] = new byte[512];
                        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(chosenFile));
                        HashSet<String> written = new HashSet<String>();
                        for (int i = 0; i < objects.size(); i++) {
                            OBJE obje = objects.elementAt(i);
                            String fpath = obje.getFilepath();
                            int hasDrive = fpath.indexOf(":\\");
                            if (hasDrive == -1) hasDrive = fpath.indexOf(":/");
                            String firstChar = fpath.substring(0, 1);
                            if ((hasDrive == -1) && (!firstChar.equals("/"))) fpath = relativePath + fpath;
                            if (!written.contains(fpath)) {
                                written.add(fpath);
                                System.out.println("Added file to zip " + fpath);
                                InputStream in = new FileInputStream(fpath);
                                ZipEntry ze = new ZipEntry("media/" + obje.getFilename());
                                zout.putNextEntry(ze);
                                int len = 0;
                                while ((len = in.read(b)) != -1) {
                                    zout.write(b, 0, len);
                                }
                                zout.closeEntry();
                                in.close();
                            }
                        }
                        zout.close();
                    } catch (IOException ie) {
                        System.out.println(ie.getMessage());
                    }
                }
            });
        }
        return zipMenuItem;
    }

    /**
	 * This method initializes jMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
    private JMenuItem getOpenRemoteMenuItem() {
        if (openRemoteMenuItem == null) {
            openRemoteMenuItem = new JMenuItem();
            openRemoteMenuItem.setText("Open Remote PGV Gedcom...");
            openRemoteMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    openPGVDialog remoteSite = new openPGVDialog(Url, username, password);
                    remoteSite.setVisible(true);
                    CgiAccess rc = remoteSite.getRemoteConn();
                    if (rc != null) {
                        remoteConn = rc;
                        username = remoteSite.getUsername();
                        password = remoteSite.getPassword();
                        Url = remoteSite.getURL();
                        gedcomLabel.setText("Remote Gedcom: " + remoteConn.actionGetvar("GEDCOM"));
                        zipMenuItem.setEnabled(false);
                        uploadButton.setEnabled(true);
                        relativePath = "";
                        objects.removeAllElements();
                        log("Searching for media items...");
                        remoteThread worker = new remoteThread(ME);
                        worker.start();
                    }
                }
            });
        }
        return openRemoteMenuItem;
    }

    /**
	 * This method initializes jScrollPane2	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
    private JScrollPane getJScrollPane2() {
        if (jScrollPane2 == null) {
            jScrollPane2 = new JScrollPane();
            jScrollPane2.setViewportView(getLogTextArea());
            jScrollPane2.setPreferredSize(new java.awt.Dimension(600, 75));
        }
        return jScrollPane2;
    }

    /**
	 * This method initializes jTextArea	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
    private JTextArea getLogTextArea() {
        if (logTextArea == null) {
            logTextArea = new JTextArea();
        }
        return logTextArea;
    }

    /**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
    private JPanel getJPanel1() {
        if (jPanel1 == null) {
            FlowLayout flowLayout1 = new FlowLayout();
            jPanel1 = new JPanel();
            jPanel1.setLayout(flowLayout1);
            jPanel1.setPreferredSize(new java.awt.Dimension(250, 180));
            flowLayout1.setHgap(1);
            flowLayout1.setVgap(1);
            jPanel1.add(getJScrollPane(), null);
            jPanel1.add(getSelectAllButton(), null);
            jPanel1.add(getUploadButton(), null);
        }
        return jPanel1;
    }

    /**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
    private JButton getUploadButton() {
        if (uploadButton == null) {
            uploadButton = new JButton();
            uploadButton.setText("Upload Selected Media");
            uploadButton.setPreferredSize(new java.awt.Dimension(250, 26));
            uploadButton.setEnabled(false);
            uploadButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    openPGVDialog remoteSite = new openPGVDialog(Url, username, password);
                    remoteSite.setVisible(true);
                    CgiAccess rc = remoteSite.getRemoteConn();
                    if (rc != null) {
                        remoteConn = rc;
                        username = remoteSite.getUsername();
                        password = remoteSite.getPassword();
                        Url = remoteSite.getURL();
                        uploadWorker worker = new uploadWorker(ME);
                        worker.start();
                    }
                }
            });
        }
        return uploadButton;
    }

    private JButton getSelectAllButton() {
        if (selectAllButton == null) {
            selectAllButton = new JButton();
            selectAllButton.setText("Select All");
            selectAllButton.setPreferredSize(new java.awt.Dimension(250, 26));
            selectAllButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    ListModel options = objeList.getModel();
                    int[] indexes = new int[options.getSize()];
                    for (int i = 0; i < options.getSize(); i++) {
                        indexes[i] = i;
                    }
                    objeList.setSelectedIndices(indexes);
                }
            });
        }
        return selectAllButton;
    }

    public static void main(String[] args) {
        ImporterGUI application = new ImporterGUI();
        application.setVisible(true);
    }

    public static void registerStarter() {
        UListControl.registerStarter(new MergeStarter());
    }

    public static class MergeStarter implements UListStarter {

        public String getName() {
            return "PGV Importer";
        }

        public void startProgram(JFrame frame, GdbiDatabase gedcom) {
            final ImporterGUI application = new ImporterGUI(gedcom);
            application.setVisible(true);
        }
    }

    /**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
    private javax.swing.JPanel getJContentPane() {
        if (jContentPane == null) {
            gedcomLabel = new JLabel();
            jContentPane = new javax.swing.JPanel();
            jContentPane.setLayout(new java.awt.BorderLayout());
            gedcomLabel.setText("First open a gedcom file from the file menu");
            gedcomLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
            jContentPane.setPreferredSize(new java.awt.Dimension(640, 500));
            jContentPane.add(gedcomLabel, java.awt.BorderLayout.NORTH);
            jContentPane.add(getJPanel1(), java.awt.BorderLayout.WEST);
            jContentPane.add(getJScrollPane2(), java.awt.BorderLayout.SOUTH);
            jContentPane.add(getDetailsScrollPane(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
	 * This method initializes jJMenuBar
	 * 
	 * @return javax.swing.JMenuBar
	 */
    private javax.swing.JMenuBar getJJMenuBar() {
        if (jJMenuBar == null) {
            jJMenuBar = new javax.swing.JMenuBar();
            jJMenuBar.add(getFileMenu());
            jJMenuBar.add(getHelpMenu());
        }
        return jJMenuBar;
    }

    /**
	 * This method initializes jMenu
	 * 
	 * @return javax.swing.JMenu
	 */
    private javax.swing.JMenu getFileMenu() {
        if (fileMenu == null) {
            fileMenu = new javax.swing.JMenu();
            fileMenu.setText("File");
            fileMenu.add(getOpenMenuItem());
            fileMenu.add(getOpenRemoteMenuItem());
            fileMenu.add(getZipMenuItem());
            fileMenu.add(getExitMenuItem());
        }
        return fileMenu;
    }

    /**
	 * This method initializes jMenu
	 * 
	 * @return javax.swing.JMenu
	 */
    private javax.swing.JMenu getHelpMenu() {
        if (helpMenu == null) {
            helpMenu = new javax.swing.JMenu();
            helpMenu.setText("Help");
            helpMenu.add(getAboutMenuItem());
        }
        return helpMenu;
    }

    /**
	 * This method initializes jMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
    private javax.swing.JMenuItem getExitMenuItem() {
        if (exitMenuItem == null) {
            exitMenuItem = new javax.swing.JMenuItem();
            exitMenuItem.setText("Close");
            exitMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    ME.setVisible(false);
                }
            });
        }
        return exitMenuItem;
    }

    /**
	 * This method initializes jMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
    private javax.swing.JMenuItem getAboutMenuItem() {
        if (aboutMenuItem == null) {
            aboutMenuItem = new javax.swing.JMenuItem();
            aboutMenuItem.setText("About");
            aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    new javax.swing.JDialog(ImporterGUI.this, "About", true).setVisible(true);
                }
            });
        }
        return aboutMenuItem;
    }

    public void openGedcomFile(File gedfile) {
        if (gedfile == null) return;
        gedcomfile = gedfile;
        gedcomLabel.setText("Gedcom: " + gedcomfile.getName());
        zipMenuItem.setEnabled(true);
        uploadButton.setEnabled(true);
        relativePath = gedcomfile.getAbsolutePath();
        int pos1 = relativePath.lastIndexOf("/");
        if (pos1 == -1) pos1 = relativePath.lastIndexOf("\\");
        if (pos1 != -1) relativePath = relativePath.substring(0, pos1 + 1);
        objects.removeAllElements();
        try {
            FileInputStream is = new FileInputStream(gedcomfile);
            byte buf[] = new byte[4096];
            int ret = -1;
            gedcomSaxParser parser = new gedcomSaxParser();
            objeHandler handler = new objeHandler("* OBJE", this);
            parser.registerHandler(handler);
            log("Reading file...");
            long fileSize = gedfile.length();
            long total_processed = 1;
            int progress = 0;
            char c = (char) is.read();
            while (c != '0') {
                c = (char) is.read();
                total_processed++;
                if ((total_processed > 512) || (c == -1)) {
                    System.out.println("not a valid gedcom file.");
                    gedcomLabel.setText("Not a valid gedocm file.");
                }
            }
            buf[0] = (byte) c;
            while ((ret = is.read(buf)) != -1) {
                total_processed += ret;
                String raw = new String(buf);
                parser.parse(raw);
                progress = (int) (total_processed / fileSize) * 100;
            }
            log("Finished reading file.");
            objeList.setListData(objects);
            objeList.updateUI();
        } catch (FileNotFoundException e) {
            log("Error reading gedcom file " + e.getMessage());
        } catch (IOException e) {
            log("Error reading gedcom file " + e.getMessage());
        }
    }

    public void addObje(OBJE obje) {
        log("Found " + obje);
        objects.add(obje);
    }

    public void log(String l) {
        logTextArea.setText(logTextArea.getText() + "\r\n" + l);
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String getUsername() {
        return username;
    }

    public String password() {
        return password;
    }

    public String getURL() {
        return Url;
    }

    public CgiAccess getRemoteConn() {
        return remoteConn;
    }

    public void updateProgress(int progress) {
        objeList.setListData(objects);
        objeList.updateUI();
    }

    public Object[] getSelectedItems() {
        return objeList.getSelectedValues();
    }
}

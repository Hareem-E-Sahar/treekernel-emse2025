package DE.FhG.IGD.semoa.sad;

import DE.FhG.IGD.semoa.sad.uihelper.*;
import DE.FhG.IGD.ui.*;
import DE.FhG.IGD.util.*;
import DE.FhG.IGD.semoa.server.Environment;
import DE.FhG.IGD.semoa.server.AgentLauncher;
import DE.FhG.IGD.semoa.security.AgentStructure;
import DE.FhG.IGD.logging.*;
import java.util.zip.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

/**
 * SemoaAgentDirectory is a graphical interface to the SeMoA environment.
 * The main components are the environment tree and the desktop pane.
 * <p>
 * To start it either type on the jshell:
 * <tt>java DE.FhG.IGD.semoa.envision.Envision</tt><br>
 * or add an alias to your conf file:
 * <tt>alias envision java DE.FhG.IGD.semoa.envision.Envision</tt>
 *
 * @author Daniel Bachmann
 * @author Daniel Bachman <daniel.bachmann@igd.fhg.de>
 * 
 * @version $Id: 1.0
 */
public class AgentDirectory extends JFrame implements ActionListener {

    /**
     * Logger
     */
    private static Logger logger_ = LoggerFactory.getLogger("gui/sad");

    /**
     * action commands
     */
    private static final String AC_REFRESH = "refresh";

    private static final String AC_INSTALL = "install";

    /**
     * The descriptor of possible command line options
     */
    private static final String DESCR_ = "localbase:F,webbase:U,config:s,help:!";

    private static final String DEFAULT_CONFIG_FILE = "sad.conf";

    protected static final String REPOSITORY_FILE = "/directory.info";

    protected static File localBase_ = null;

    protected static URL webBase_ = null;

    /**
     * Image for MessageBox
     */
    private ImageIcon messageBoxIcon_;

    /**
     * SeMoA-Strip Image
     */
    private ImageIcon stripIcon_;

    /**
     * Image for agent Directory
     */
    private ImageIcon agentDirectoryIcon_;

    /**
     * Image for jar file
     */
    private ImageIcon jarArchiveIcon_;

    /**
     * Image for standard leaf
     */
    private ImageIcon leafIcon_;

    /**
     * Image for standard node (not leaf, not root)
     */
    private ImageIcon closedIcon_;

    /**
     * Image for standard node (not leaf, not root) in expanded mode
     */
    private ImageIcon openIcon_;

    /**
     * Represents a dragable tree view of the local jar files.
     */
    private FileDragTree localTree_;

    /**
     * Represents the renderer of the FileTree
     */
    private FileTreeCellRenderer treeRenderer_;

    /**
     * Represents general informations about the jar archive
     * version, dependencies, properties
     */
    private JTextArea jarInfo_;

    /**
     * Represents the download Frame
     */
    private InstallFrame installFrame_;

    /**
     * The name of the configuration file.
     */
    private String configFile_ = System.getProperty("semoa.etc") + File.separator + DEFAULT_CONFIG_FILE;

    /**
     * Main Constructor
     */
    public AgentDirectory() {
        this(null, null);
    }

    /**
     * Constructor with parameters configFile
     */
    public AgentDirectory(String configFile) {
        super();
        if (configFile != null) {
            configFile_ = configFile;
        }
        loadConfig();
        init();
    }

    /**
     * Constructor with parameters localBase and webBase
     */
    public AgentDirectory(File localBase, URL webBase) {
        super();
        setLocalBase(localBase);
        setWebBase(webBase);
        init();
    }

    protected void init() {
        Container contentPane;
        logger_.entering(new Object[] {});
        loadImages();
        contentPane = this.getContentPane();
        contentPane.add(createMainPanel());
        this.setTitle("SeMoA Agent Directory");
        this.setSize(615, 590);
        this.center();
        this.pack();
        ToolTipManager.sharedInstance().setEnabled(true);
        this.addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentResized(ComponentEvent e) {
                if (installFrame_ != null) {
                    installFrame_.setLocation((int) getLocation().getX() + getWidth(), (int) getLocation().getY());
                    installFrame_.setSize(installFrame_.getWidth(), getHeight());
                    installFrame_.validate();
                }
            }

            public void componentMoved(ComponentEvent e) {
                if (installFrame_ != null) {
                    installFrame_.setLocation((int) getLocation().getX() + getWidth(), (int) getLocation().getY());
                    installFrame_.setSize(installFrame_.getWidth(), getHeight());
                    installFrame_.validate();
                }
            }
        });
        this.show();
        logger_.exiting();
    }

    /**
     * Moves this frame to the center of the screen.
     */
    private void center() {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        Rectangle bounds = gc.getBounds();
        Dimension dim = getSize();
        logger_.entering(new Object[] {});
        setLocation(bounds.x + ((bounds.width - dim.width) / 2), bounds.y + ((bounds.height - dim.height) / 2));
        logger_.exiting();
    }

    /**
     * Load all needed images.
     */
    private void loadImages() {
        ClassLoader cl;
        String path;
        java.net.URL url;
        Images img;
        logger_.entering(new Object[] {});
        path = this.getClass().getPackage().getName().replace('.', '/');
        path += "/images/";
        cl = this.getClass().getClassLoader();
        try {
            url = cl.getResource(path + "icon.gif");
            messageBoxIcon_ = new ImageIcon(url);
            url = cl.getResource(path + "semoa_strip.jpg");
            stripIcon_ = new ImageIcon(url);
            url = cl.getResource(path + "jar_archive.gif");
            agentDirectoryIcon_ = new ImageIcon(url);
            url = cl.getResource(path + "jar_archive.gif");
            jarArchiveIcon_ = new ImageIcon(url);
            url = cl.getResource(path + "folder_icon.gif");
            closedIcon_ = new ImageIcon(url);
            url = cl.getResource(path + "open_folder_icon.gif");
            openIcon_ = new ImageIcon(url);
            url = cl.getResource(path + "leaf_icon.gif");
            leafIcon_ = new ImageIcon(url);
        } catch (Exception e) {
            JOptionPane.showInternalMessageDialog(this.getContentPane(), "Failure to load images: \n" + e.getMessage(), "Attention", JOptionPane.ERROR_MESSAGE);
            logger_.error("Load Images");
            logger_.caught(e);
        }
        logger_.exiting();
    }

    /**
     * Create the main panel that contains all other GUI elements.
     */
    private JPanel createMainPanel() {
        JPanel mainPanel;
        logger_.entering(new Object[] {});
        mainPanel = new JPanel(new GridBagLayout());
        addConstrained(mainPanel, new JLabel(stripIcon_), 0, 0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH, 1, 1, new Insets(0, 0, 0, 0));
        addConstrained(mainPanel, createDirectoryPanel(), 0, 1, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER, 1, 20, new Insets(5, 0, 5, 0));
        addConstrained(mainPanel, createButtonPanel(), 0, 2, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH, 1, 1, new Insets(5, 5, 5, 5));
        logger_.exiting();
        return mainPanel;
    }

    /**
     * Create the button panel with <i>refresh</i> button
     * and the <i>install from SeMoA - Repository</i> button.
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel;
        JButton refresh;
        JButton install;
        logger_.entering(new Object[] {});
        buttonPanel = new JPanel(new GridBagLayout());
        refresh = new JButton("Refresh");
        refresh.setToolTipText("Synchronizes the tree with the localbase directory");
        refresh.addActionListener(this);
        refresh.setActionCommand(AC_REFRESH);
        install = new JButton("Install from SeMoA - Repository");
        install.setToolTipText("Tries to connect to the webbase URL");
        install.addActionListener(this);
        install.setActionCommand(AC_INSTALL);
        addConstrained(buttonPanel, refresh, 0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 1, 1, new Insets(0, 5, 0, 5));
        addConstrained(buttonPanel, install, 4, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST, 1, 1, new Insets(0, 5, 0, 5));
        logger_.exiting();
        return buttonPanel;
    }

    /**
     * Create the directory Panel with the Tree
     * and the agent property information
     */
    private JPanel createDirectoryPanel() {
        logger_.entering(new Object[] {});
        JPanel directoryPanel;
        JSplitPane splitPanel;
        directoryPanel = new JPanel(new GridBagLayout());
        splitPanel = new JSplitPane();
        splitPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jarInfo_ = new JTextArea();
        jarInfo_.setEditable(false);
        jarInfo_.setVisible(true);
        localTree_ = new FileDragTree();
        localTree_.addMouseListener(new MouseListener() {

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath treePath;
                    File file;
                    Resource res;
                    Properties props;
                    AgentLauncher al;
                    String key;
                    String value;
                    treePath = localTree_.getPathForLocation(e.getX(), e.getY());
                    if (treePath != null) {
                        if (((File) treePath.getLastPathComponent()).toString().endsWith(".jar")) {
                            try {
                                file = (File) treePath.getLastPathComponent();
                                res = new MemoryResource();
                                props = new Properties();
                                Resources.unzip(new FileInputStream(file), res);
                                props.load(res.getInputStream(AgentStructure.PROPERTIES));
                                key = WhatIs.stringValue("AGENT_LAUNCHER");
                                if (key != null) {
                                    al = (AgentLauncher) Environment.getEnvironment().lookup(key);
                                    if (al != null) {
                                        String input;
                                        for (Enumeration en = props.propertyNames(); en.hasMoreElements(); ) {
                                            key = (String) en.nextElement();
                                            value = props.getProperty(key);
                                            if (value.startsWith("<") && value.endsWith(">")) {
                                                input = JOptionPane.showInputDialog("Please enter a value for " + key, value);
                                                if (input == null) props.remove(key); else props.setProperty(key, input);
                                            }
                                        }
                                        al.launchAgent(res, props);
                                    }
                                }
                            } catch (Exception ex) {
                                logger_.error("Agent Launch");
                                logger_.caught(ex);
                            }
                        }
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                refreshText(jarInfo_, localTree_.getPathForLocation(e.getX(), e.getY()));
            }
        });
        localTree_.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                refreshText(jarInfo_, localTree_.getSelectionPath());
            }
        });
        refresh();
        treeRenderer_ = new FileTreeCellRenderer(jarArchiveIcon_, closedIcon_, openIcon_);
        localTree_.setCellRenderer(treeRenderer_);
        loadIcons();
        splitPanel.setTopComponent(new JScrollPane(localTree_));
        splitPanel.setBottomComponent(new JScrollPane(jarInfo_));
        splitPanel.setOneTouchExpandable(true);
        splitPanel.setDividerLocation(200);
        addConstrained(directoryPanel, splitPanel, 0, 0, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER, 1, 1, new Insets(0, 0, 0, 0));
        directoryPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        logger_.exiting();
        return directoryPanel;
    }

    /**
     * updates the textArea
     */
    private void refreshText(JTextArea jarInfo, TreePath treePath) {
        logger_.entering(new Object[] { "jarInfo= " + jarInfo, "treePath= " + treePath });
        File file;
        Resource res;
        Properties props;
        try {
            if (treePath != null) if (((File) treePath.getLastPathComponent()).toString().endsWith(".jar")) {
                jarInfo.setText("");
                file = (File) treePath.getLastPathComponent();
                res = new MemoryResource();
                props = new Properties();
                Resources.unzip(new FileInputStream(file), res);
                props.load(res.getInputStream(AgentStructure.PROPERTIES));
                jarInfo.append("Resource List :\n");
                for (int i = 0; i < res.list().size(); i++) {
                    jarInfo.append(res.list().get(i).toString() + "\n");
                }
                jarInfo.append("\nMain Agent Properties :\n");
                jarInfo.append("Agent Name   : " + props.getProperty(AgentStructure.PROP_AGENT_ALIAS) + "\n");
                jarInfo.append("Agent Class    : " + props.getProperty(AgentStructure.PROP_AGENT_CLASS) + "\n");
                jarInfo.append("Agent System : " + props.getProperty(AgentStructure.PROP_AGENT_SYSTEM) + "\n");
                jarInfo.append("Agent Type      : " + props.getProperty(AgentStructure.PROP_AGENT_TYPE) + "\n");
                jarInfo.append("\nInitial Properties :\n");
                String key;
                String value;
                for (Enumeration e = props.propertyNames(); e.hasMoreElements(); ) {
                    key = (String) e.nextElement();
                    value = props.getProperty(key);
                    if (value.startsWith("<") && value.endsWith(">")) {
                        jarInfo.append(key);
                        jarInfo.append(" = ");
                        jarInfo.append(value + "\n");
                    }
                }
                InputStream manifestInputStream;
                java.util.jar.Manifest man;
                String agentVersion, platformVersion;
                manifestInputStream = res.getInputStream(AgentStructure.MANIFEST);
                if (manifestInputStream != null) {
                    jarInfo.append("\nManifest Properties :\n\n");
                    man = new java.util.jar.Manifest(manifestInputStream);
                    agentVersion = (String) man.getMainAttributes().get("Agent-Version");
                    platformVersion = (String) man.getMainAttributes().get("Platform-Version");
                    if (agentVersion != null) jarInfo.append("Agent Version     : " + agentVersion + "\n");
                    if (platformVersion != null) jarInfo.append("Platform Version : " + platformVersion + "\n");
                }
            }
        } catch (Exception e) {
            jarInfo.setText("Failure in jar-Archive");
            logger_.error("Incorrect jar-Archive");
            logger_.caught(e);
        }
        logger_.exiting();
    }

    /**
     * Loads agent icons defined in static/properties "agent.icon"
     */
    private void loadIcons() {
        logger_.entering(new Object[] {});
        Vector files;
        Resource res;
        Properties props;
        try {
            files = getFiles((File) localTree_.getModel().getRoot());
            for (int i = 0; i < files.size(); i++) {
                res = new MemoryResource();
                props = new Properties();
                Resources.unzip(new FileInputStream((File) files.get(i)), res);
                props.load(res.getInputStream(AgentStructure.PROPERTIES));
                if (props.getProperty(AgentStructure.PROP_AGENT_ICON) != null) {
                    ImageIcon icon;
                    InputStream in;
                    ByteArrayOutputStream out;
                    byte[] buf;
                    int n;
                    in = res.getInputStream(props.getProperty("agent.icon"));
                    out = new ByteArrayOutputStream();
                    if (in != null) {
                        buf = new byte[1024];
                        n = 0;
                        while ((n = in.read(buf)) > -1) {
                            out.write(buf);
                        }
                        in.close();
                        out.close();
                        icon = new ImageIcon(out.toByteArray());
                        treeRenderer_.addNewFileIcon((File) files.get(i), icon);
                        localTree_.revalidate();
                        localTree_.repaint();
                    }
                }
            }
        } catch (Exception e) {
            logger_.warning("Loading Agent Icons");
            logger_.caught(e);
        }
        logger_.exiting();
    }

    /**
     * returns a vector of the files in the local base
     * @param node the file node.
     * @return a vector of files containing in local base.
     */
    private Vector getFiles(File node) {
        logger_.entering(new Object[] { "node= " + node });
        Vector files;
        File file;
        files = new Vector();
        for (int i = 0; i < localTree_.getModel().getChildCount(node); i++) {
            file = (File) localTree_.getModel().getChild(node, i);
            if (file.toString().endsWith(".jar")) files.add(file);
            files.addAll(getFiles(file));
        }
        logger_.exiting(files);
        return files;
    }

    /**
     * Loads the config File "sad.conf"
     */
    private void loadConfig() {
        Properties config;
        String path;
        logger_.entering(new Object[] {});
        try {
            localBase_ = null;
            webBase_ = null;
            config = VariableSubstitution.parseConfigFile(configFile_, VariableSubstitution.SYSTEM_PROPERTIES | VariableSubstitution.SHELL_VARIABLES);
            localBase_ = new File(config.getProperty("SEMOA_JAR_LOCAL"));
            webBase_ = new URL(config.getProperty("SEMOA_JAR_REPOSITORY"));
        } catch (Exception e) {
            JOptionPane.showInternalMessageDialog(this.getContentPane(), "Cannot load config file '" + configFile_ + "'\n" + e.getMessage(), "Attention", JOptionPane.ERROR_MESSAGE);
            logger_.warning("Could not load config file ('" + configFile_ + "'");
            logger_.caught(e);
        }
        logger_.exiting();
    }

    /**
     * Updates tree view of local jar directory
     */
    private void refresh() {
        FileTreeModel model;
        File file;
        logger_.entering(new Object[] {});
        loadConfig();
        try {
            model = new FileTreeModel(localBase_);
            localTree_.setModel(model);
            localTree_.setRootVisible(true);
            localTree_.revalidate();
            localTree_.repaint();
        } catch (Exception e) {
            JOptionPane.showInternalMessageDialog(this.getContentPane(), "Failure to refresh Tree: \n" + e.getMessage(), "Attention", JOptionPane.ERROR_MESSAGE);
            logger_.error("Refresh Tree");
            logger_.caught(e);
        }
        logger_.exiting();
    }

    /**
     * Connects to the SeMoA jar repository and opens a jar browse tree
     */
    private void install() {
        logger_.entering(new Object[] {});
        if (installFrame_ != null) installFrame_.dispose();
        try {
            installFrame_ = new InstallFrame(localTree_);
            installFrame_.setLocation((int) getLocation().getX() + getWidth(), (int) getLocation().getY());
            installFrame_.setSize(300, getHeight());
            installFrame_.validate();
            installFrame_.show();
        } catch (Exception e) {
            JOptionPane.showInternalMessageDialog(this.getContentPane(), "Cannot install from SeMoA Repository, maybe no connection: \n" + e.getMessage(), "Attention", JOptionPane.ERROR_MESSAGE);
            logger_.error("Install from SeMoA Repository");
            logger_.caught(e);
        }
        logger_.exiting();
    }

    /**
     * This is a convenience method for adding a <code>Component</code>
     * into a <code>Container</code>.
     */
    protected static void addConstrained(Container container, Component component, int gridx, int gridy, int gridwidth, int gridheight, int fill, int anchor, double weightx, double weighty, Insets insets) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheight;
        gbc.fill = fill;
        gbc.anchor = anchor;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.insets = insets;
        container.add(component, gbc);
    }

    public void actionPerformed(ActionEvent e) {
        String command;
        command = e.getActionCommand();
        if (command.equals(AC_REFRESH)) {
            refresh();
        }
        if (command.equals(AC_INSTALL)) {
            install();
        }
    }

    public static File getLocalBase() {
        return localBase_;
    }

    public static URL getWebBase() {
        return webBase_;
    }

    public void setLocalBase(File localBase) {
        localBase_ = localBase;
    }

    public void setWebBase(URL webBase) {
        webBase_ = webBase;
    }

    /**
     * Just for the construction and intialisation of this class.
     */
    public static void main(String[] args) {
        AgentDirectory gui;
        ArgsParser ap;
        File localBase;
        URL webBase;
        logger_.entering(new Object[] { "args= " + args });
        try {
            ap = new ArgsParser(DESCR_);
            ap.parse(args);
            if (ap.isDefined("help")) {
                System.out.println(new String("\nWelcome to the SeMoA agent directory." + "\n\nUSAGE: java " + AgentDirectory.class.getName() + " [<Options>]" + "\n\nOptions are:" + "\n -config    <configuration_file>" + "\n -localbase <local_base_directory>" + "\n Sets the flag localbase to <local_base_directory>." + "\n     e.g. -localbase c:/semoa/agents (for Windows)" + "\n     or   -localbase c\\semoa\\agents" + "\n Default value is /semoa/agents." + "\n\n -webbase <semoa_agent_repository_weburl>" + "\n Sets the flag webbase to <semoa_agent_repository_weburl>." + "\n     e.g. -webbase http://www.semoa.org/agent-repository/" + "directory.info <default>"));
            } else {
                if (ap.isDefined("config")) {
                    gui = new AgentDirectory(ap.stringValue("config"));
                } else if (ap.isDefined("localbase") && ap.isDefined("webbase")) {
                    localBase = (File) ap.value("localbase");
                    webBase = (URL) ap.value("webbase");
                    gui = new AgentDirectory(localBase, webBase);
                } else {
                    gui = new AgentDirectory(null);
                }
            }
        } catch (Exception e) {
            System.out.println("Internal error. Terminating...");
            logger_.throwing(e);
        }
        logger_.exiting();
    }
}

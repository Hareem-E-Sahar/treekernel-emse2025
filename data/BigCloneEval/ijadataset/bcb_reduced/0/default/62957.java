import com.jidesoft.action.*;
import com.jidesoft.action.event.CommandMenuBar;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideMenu;
import com.jidesoft.swing.JideToggleButton;
import com.jidesoft.utils.Lm;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * A demo that shows all of the Swing components.
 *
 * @author Jeff Dinkins
 * @version 1.42 12/19/03
 */
public class SwingSet2 extends ContentContainer implements DockableBarHolder {

    String[] demos = { "ButtonDemo", "ColorChooserDemo", "ComboBoxDemo", "FileChooserDemo", "HtmlDemo", "ListDemo", "OptionPaneDemo", "ProgressBarDemo", "ScrollPaneDemo", "SliderDemo", "SplitPaneDemo", "TabbedPaneDemo", "TableDemo", "ToolTipDemo", "TreeDemo" };

    void loadDemos() {
        for (int i = 0; i < demos.length; ) {
            if (isApplet() && demos[i].equals("FileChooserDemo")) {
            } else {
                loadDemo(demos[i]);
            }
            i++;
        }
        Lm.setParent((JFrame) this.getTopLevelAncestor());
        Lm.showPopupMessageBox("<HTML><font size=3>Please note: </font>" + "<BR>This is a modified version of SwingSet2 demo coming along with any JDK release. " + "<BR>It is meant to show how to migrate from existing applicatoin to use JIDE Action Framework. " + "<BR>For more details, please refer to JIDE Action Framework Developer Guide - Migrating from Existing Application.<HTML>");
    }

    private static final String mac = "com.sun.java.swing.plaf.mac.MacLookAndFeel";

    private static final String metal = "javax.swing.plaf.metal.MetalLookAndFeel";

    private static final String motif = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";

    private static final String windows = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";

    private static final String gtk = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";

    private static String currentLookAndFeel = metal;

    private Vector demosVector = new Vector();

    private static final int PREFERRED_WIDTH = 720;

    private static final int PREFERRED_HEIGHT = 640;

    private Dimension HGAP = new Dimension(1, 5);

    private Dimension VGAP = new Dimension(5, 1);

    private ResourceBundle bundle = null;

    private DemoModule currentDemo = null;

    private JPanel demoPanel = null;

    private JDialog aboutBox = null;

    private JTextField statusField = null;

    private ToggleButtonToolBar toolbar = null;

    private ButtonGroup toolbarGroup = new ButtonGroup();

    private CommandBar menuBar = null;

    private JideMenu lafMenu = null;

    private JideMenu themesMenu = null;

    private JideMenu audioMenu = null;

    private JideMenu toolTipMenu = null;

    private ButtonGroup lafMenuGroup = new ButtonGroup();

    private ButtonGroup themesMenuGroup = new ButtonGroup();

    private ButtonGroup audioMenuGroup = new ButtonGroup();

    private ButtonGroup toolTipMenuGroup = new ButtonGroup();

    private JPopupMenu popupMenu = null;

    private ButtonGroup popupMenuGroup = new ButtonGroup();

    private JFrame frame = null;

    private JWindow splashScreen = null;

    private SwingSet2Applet applet = null;

    private boolean DEBUG = true;

    private int debugCounter = 0;

    private JTabbedPane tabbedPane = null;

    private JEditorPane demoSrcPane = null;

    private JLabel splashLabel = null;

    Container contentPane = null;

    private static int numSSs = 0;

    private static Vector swingSets = new Vector();

    public SwingSet2(SwingSet2Applet applet) {
        this(applet, null);
    }

    /**
     * SwingSet2 Constructor
     */
    public SwingSet2(SwingSet2Applet applet, GraphicsConfiguration gc) {
        try {
            UIManager.setLookAndFeel(WindowsLookAndFeel.class.getName());
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (UnsupportedLookAndFeelException e) {
        }
        LookAndFeelFactory.installJideExtension(LookAndFeelFactory.OFFICE2003_STYLE);
        this.applet = applet;
        if (!isApplet()) {
            frame = createFrame(gc);
        }
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        createSplashScreen();
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                showSplashScreen();
            }
        });
        initializeDemo();
        preloadFirstDemo();
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                showSwingSet2();
                hideSplash();
            }
        });
        DemoLoadThread demoLoader = new DemoLoadThread(this);
        demoLoader.start();
    }

    /**
     * SwingSet2 Main. Called only if we're an application, not an applet.
     */
    public static void main(String[] args) {
        SwingSet2 swingset = new SwingSet2(null, GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
    }

    public void initializeDemo() {
        menuBar = createMenus();
        popupMenu = createPopupMenu();
        toolbar = new ToggleButtonToolBar();
        _dockableBarManager = new DefaultDockableBarManager(frame, this);
        _dockableBarManager.setProfileKey("swingset2");
        _dockableBarManager.addDockableBar(menuBar);
        _dockableBarManager.addDockableBar(toolbar);
        _dockableBarManager.getMainContainer().setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();
        _dockableBarManager.getMainContainer().add(tabbedPane, BorderLayout.CENTER);
        tabbedPane.getModel().addChangeListener(new TabListener());
        statusField = new JTextField("");
        statusField.setEditable(false);
        demoPanel = new JPanel();
        demoPanel.setLayout(new BorderLayout());
        demoPanel.setBorder(new EtchedBorder());
        tabbedPane.addTab("Hi There!", demoPanel);
        demoSrcPane = new JEditorPane("text/html", getString("SourceCode.loading"));
        demoSrcPane.setEditable(false);
        JScrollPane scroller = new JScrollPane();
        scroller.getViewport().add(demoSrcPane);
        tabbedPane.addTab(getString("TabbedPane.src_label"), null, scroller, getString("TabbedPane.src_tooltip"));
        getDockableBarManager().loadLayoutData();
    }

    DemoModule currentTabDemo = null;

    class TabListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            SingleSelectionModel model = (SingleSelectionModel) e.getSource();
            boolean srcSelected = model.getSelectedIndex() == 1;
            if (currentTabDemo != currentDemo && demoSrcPane != null && srcSelected) {
                demoSrcPane.setText(getString("SourceCode.loading"));
                repaint();
            }
            if (currentTabDemo != currentDemo && srcSelected) {
                currentTabDemo = currentDemo;
                setSourceCode(currentDemo);
            }
        }
    }

    /**
     * Create menus
     */
    public CommandBar createMenus() {
        JMenuItem mi;
        CommandBar menuBar = new CommandMenuBar("Menu Bar");
        menuBar.setStretch(true);
        menuBar.getAccessibleContext().setAccessibleName(getString("MenuBar.accessible_description"));
        JideMenu fileMenu = (JideMenu) menuBar.add(new JideMenu(getString("FileMenu.file_label")));
        fileMenu.setMnemonic(getMnemonic("FileMenu.file_mnemonic"));
        fileMenu.getAccessibleContext().setAccessibleDescription(getString("FileMenu.accessible_description"));
        createMenuItem(fileMenu, "FileMenu.about_label", "FileMenu.about_mnemonic", "FileMenu.about_accessible_description", new AboutAction(this));
        fileMenu.addSeparator();
        createMenuItem(fileMenu, "FileMenu.open_label", "FileMenu.open_mnemonic", "FileMenu.open_accessible_description", null);
        createMenuItem(fileMenu, "FileMenu.save_label", "FileMenu.save_mnemonic", "FileMenu.save_accessible_description", null);
        createMenuItem(fileMenu, "FileMenu.save_as_label", "FileMenu.save_as_mnemonic", "FileMenu.save_as_accessible_description", null);
        if (!isApplet()) {
            fileMenu.addSeparator();
            createMenuItem(fileMenu, "FileMenu.exit_label", "FileMenu.exit_mnemonic", "FileMenu.exit_accessible_description", new ExitAction(this));
        }
        if (numSSs == 0) {
            lafMenu = (JideMenu) menuBar.add(new JideMenu(getString("LafMenu.laf_label")));
            lafMenu.setMnemonic(getMnemonic("LafMenu.laf_mnemonic"));
            lafMenu.getAccessibleContext().setAccessibleDescription(getString("LafMenu.laf_accessible_description"));
            createLafMenuItem(lafMenu, "LafMenu.java_label", "LafMenu.java_mnemonic", "LafMenu.java_accessible_description", metal);
            createLafMenuItem(lafMenu, "LafMenu.mac_label", "LafMenu.mac_mnemonic", "LafMenu.mac_accessible_description", mac);
            createLafMenuItem(lafMenu, "LafMenu.motif_label", "LafMenu.motif_mnemonic", "LafMenu.motif_accessible_description", motif);
            UIManager.LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();
            for (int counter = 0; counter < lafInfo.length; counter++) {
                if (lafInfo[counter].getName().toLowerCase().indexOf("windows") != -1) {
                    mi = createLafMenuItem(lafMenu, "LafMenu.windows_label", "LafMenu.windows_mnemonic", "LafMenu.windows_accessible_description", windows);
                    mi.setSelected(true);
                }
            }
            for (int counter = 0; counter < lafInfo.length; counter++) {
                if (lafInfo[counter].getName().toLowerCase().indexOf("gtk") != -1) {
                    createLafMenuItem(lafMenu, "LafMenu.gtk_label", "LafMenu.gtk_mnemonic", "LafMenu.gtk_accessible_description", gtk);
                    break;
                }
            }
            themesMenu = (JideMenu) menuBar.add(new JideMenu(getString("ThemesMenu.themes_label")));
            themesMenu.setMnemonic(getMnemonic("ThemesMenu.themes_mnemonic"));
            themesMenu.getAccessibleContext().setAccessibleDescription(getString("ThemesMenu.themes_accessible_description"));
            audioMenu = (JideMenu) themesMenu.add(new JideMenu(getString("AudioMenu.audio_label")));
            audioMenu.setMnemonic(getMnemonic("AudioMenu.audio_mnemonic"));
            audioMenu.getAccessibleContext().setAccessibleDescription(getString("AudioMenu.audio_accessible_description"));
            createAudioMenuItem(audioMenu, "AudioMenu.on_label", "AudioMenu.on_mnemonic", "AudioMenu.on_accessible_description", new OnAudioAction(this));
            mi = createAudioMenuItem(audioMenu, "AudioMenu.default_label", "AudioMenu.default_mnemonic", "AudioMenu.default_accessible_description", new DefaultAudioAction(this));
            mi.setSelected(true);
            createAudioMenuItem(audioMenu, "AudioMenu.off_label", "AudioMenu.off_mnemonic", "AudioMenu.off_accessible_description", new OffAudioAction(this));
            createThemesMenuItem(themesMenu, "ThemesMenu.aqua_label", "ThemesMenu.aqua_mnemonic", "ThemesMenu.aqua_accessible_description", new AquaTheme());
            createThemesMenuItem(themesMenu, "ThemesMenu.charcoal_label", "ThemesMenu.charcoal_mnemonic", "ThemesMenu.charcoal_accessible_description", new CharcoalTheme());
            createThemesMenuItem(themesMenu, "ThemesMenu.contrast_label", "ThemesMenu.contrast_mnemonic", "ThemesMenu.contrast_accessible_description", new ContrastTheme());
            createThemesMenuItem(themesMenu, "ThemesMenu.emerald_label", "ThemesMenu.emerald_mnemonic", "ThemesMenu.emerald_accessible_description", new EmeraldTheme());
            createThemesMenuItem(themesMenu, "ThemesMenu.ruby_label", "ThemesMenu.ruby_mnemonic", "ThemesMenu.ruby_accessible_description", new RubyTheme());
            toolTipMenu = (JideMenu) menuBar.add(new JideMenu(getString("ToolTipMenu.tooltip_label")));
            toolTipMenu.setMnemonic(getMnemonic("ToolTipMenu.tooltip_mnemonic"));
            toolTipMenu.getAccessibleContext().setAccessibleDescription(getString("ToolTipMenu.tooltip_accessible_description"));
            mi = createToolTipMenuItem(toolTipMenu, "ToolTipMenu.on_label", "ToolTipMenu.on_mnemonic", "ToolTipMenu.on_accessible_description", new ToolTipAction(this, true));
            mi.setSelected(true);
            createToolTipMenuItem(toolTipMenu, "ToolTipMenu.off_label", "ToolTipMenu.off_mnemonic", "ToolTipMenu.off_accessible_description", new ToolTipAction(this, false));
        }
        if (!isApplet()) {
            GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            if (screens.length > 1) {
                JideMenu multiScreenMenu = (JideMenu) menuBar.add(new JideMenu(getString("MultiMenu.multi_label")));
                multiScreenMenu.setMnemonic(getMnemonic("MultiMenu.multi_mnemonic"));
                multiScreenMenu.getAccessibleContext().setAccessibleDescription(getString("MultiMenu.multi_accessible_description"));
                createMultiscreenMenuItem(multiScreenMenu, MultiScreenAction.ALL_SCREENS);
                for (int i = 0; i < screens.length; i++) {
                    createMultiscreenMenuItem(multiScreenMenu, i);
                }
            }
        }
        return menuBar;
    }

    /**
     * Create the tool tip submenu
     */
    public JMenuItem createToolTipMenuItem(JideMenu menu, String label, String mnemonic, String accessibleDescription, Action action) {
        JRadioButtonMenuItem mi = (JRadioButtonMenuItem) menu.add(new JRadioButtonMenuItem(getString(label)));
        toolTipMenuGroup.add(mi);
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext().setAccessibleDescription(getString(accessibleDescription));
        mi.addActionListener(action);
        return mi;
    }

    /**
     * Create the theme's audio submenu
     */
    public JMenuItem createAudioMenuItem(JideMenu menu, String label, String mnemonic, String accessibleDescription, Action action) {
        JRadioButtonMenuItem mi = (JRadioButtonMenuItem) menu.add(new JRadioButtonMenuItem(getString(label)));
        audioMenuGroup.add(mi);
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext().setAccessibleDescription(getString(accessibleDescription));
        mi.addActionListener(action);
        return mi;
    }

    /**
     * Creates a generic menu item
     */
    public JMenuItem createMenuItem(JideMenu menu, String label, String mnemonic, String accessibleDescription, Action action) {
        JMenuItem mi = (JMenuItem) menu.add(new JMenuItem(getString(label)));
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext().setAccessibleDescription(getString(accessibleDescription));
        mi.addActionListener(action);
        if (action == null) {
            mi.setEnabled(false);
        }
        return mi;
    }

    /**
     * Creates a JRadioButtonMenuItem for the Themes menu
     */
    public JMenuItem createThemesMenuItem(JideMenu menu, String label, String mnemonic, String accessibleDescription, MetalTheme theme) {
        JRadioButtonMenuItem mi = (JRadioButtonMenuItem) menu.add(new JRadioButtonMenuItem(getString(label)));
        themesMenuGroup.add(mi);
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext().setAccessibleDescription(getString(accessibleDescription));
        mi.addActionListener(new ChangeThemeAction(this, theme));
        return mi;
    }

    /**
     * Creates a JRadioButtonMenuItem for the Look and Feel menu
     */
    public JMenuItem createLafMenuItem(JideMenu menu, String label, String mnemonic, String accessibleDescription, String laf) {
        JMenuItem mi = (JRadioButtonMenuItem) menu.add(new JRadioButtonMenuItem(getString(label)));
        lafMenuGroup.add(mi);
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext().setAccessibleDescription(getString(accessibleDescription));
        mi.addActionListener(new ChangeLookAndFeelAction(this, laf));
        mi.setEnabled(isAvailableLookAndFeel(laf));
        return mi;
    }

    /**
     * Creates a multi-screen menu item
     */
    public JMenuItem createMultiscreenMenuItem(JideMenu menu, int screen) {
        JMenuItem mi = null;
        if (screen == MultiScreenAction.ALL_SCREENS) {
            mi = (JMenuItem) menu.add(new JMenuItem(getString("MultiMenu.all_label")));
            mi.setMnemonic(getMnemonic("MultiMenu.all_mnemonic"));
            mi.getAccessibleContext().setAccessibleDescription(getString("MultiMenu.all_accessible_description"));
        } else {
            mi = (JMenuItem) menu.add(new JMenuItem(getString("MultiMenu.single_label") + " " + screen));
            mi.setMnemonic(KeyEvent.VK_0 + screen);
            mi.getAccessibleContext().setAccessibleDescription(getString("MultiMenu.single_accessible_description") + " " + screen);
        }
        mi.addActionListener(new MultiScreenAction(this, screen));
        return mi;
    }

    public JPopupMenu createPopupMenu() {
        JPopupMenu popup = new JPopupMenu("JPopupMenu demo");
        createPopupMenuItem(popup, "LafMenu.java_label", "LafMenu.java_mnemonic", "LafMenu.java_accessible_description", metal);
        createPopupMenuItem(popup, "LafMenu.mac_label", "LafMenu.mac_mnemonic", "LafMenu.mac_accessible_description", mac);
        createPopupMenuItem(popup, "LafMenu.motif_label", "LafMenu.motif_mnemonic", "LafMenu.motif_accessible_description", motif);
        createPopupMenuItem(popup, "LafMenu.windows_label", "LafMenu.windows_mnemonic", "LafMenu.windows_accessible_description", windows);
        createPopupMenuItem(popup, "LafMenu.gtk_label", "LafMenu.gtk_mnemonic", "LafMenu.gtk_accessible_description", gtk);
        InputMap map = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.SHIFT_MASK), "postMenuAction");
        getActionMap().put("postMenuAction", new ActivatePopupMenuAction(this, popup));
        return popup;
    }

    /**
     * Creates a JMenuItem for the Look and Feel popup menu
     */
    public JMenuItem createPopupMenuItem(JPopupMenu menu, String label, String mnemonic, String accessibleDescription, String laf) {
        JMenuItem mi = menu.add(new JMenuItem(getString(label)));
        popupMenuGroup.add(mi);
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext().setAccessibleDescription(getString(accessibleDescription));
        mi.addActionListener(new ChangeLookAndFeelAction(this, laf));
        mi.setEnabled(isAvailableLookAndFeel(laf));
        return mi;
    }

    /**
     * Load the first demo. This is done separately from the remaining demos
     * so that we can get SwingSet2 up and available to the user quickly.
     */
    public void preloadFirstDemo() {
        DemoModule demo = addDemo(new InternalFrameDemo(this));
        setDemo(demo);
    }

    /**
     * Add a demo to the toolbar
     */
    public DemoModule addDemo(DemoModule demo) {
        demosVector.addElement(demo);
        SwingUtilities.invokeLater(new SwingSetRunnable(this, demo) {

            public void run() {
                SwitchToDemoAction action = new SwitchToDemoAction(swingset, (DemoModule) obj);
                JideToggleButton tb = swingset.getToolBar().addToggleButton(action);
                swingset.getToolBarGroup().add(tb);
                if (swingset.getToolBarGroup().getSelection() == null) {
                    tb.setSelected(true);
                }
                tb.setText(null);
                tb.setToolTipText(((DemoModule) obj).getToolTip());
                if (demos[demos.length - 1].equals(obj.getClass().getName())) {
                    setStatus(getString("Status.popupMenuAccessible"));
                }
            }
        });
        return demo;
    }

    /**
     * Sets the current demo
     */
    public void setDemo(DemoModule demo) {
        currentDemo = demo;
        JComponent currentDemoPanel = demo.getDemoPanel();
        SwingUtilities.updateComponentTreeUI(currentDemoPanel);
        demoPanel.removeAll();
        demoPanel.add(currentDemoPanel, BorderLayout.CENTER);
        tabbedPane.setSelectedIndex(0);
        tabbedPane.setTitleAt(0, demo.getName());
        tabbedPane.setToolTipTextAt(0, demo.getToolTip());
    }

    /**
     * Bring up the SwingSet2 demo by showing the frame (only
     * applicable if coming up as an application, not an applet);
     */
    public void showSwingSet2() {
        if (!isApplet() && getFrame() != null) {
            JFrame f = getFrame();
            f.setTitle(getString("Frame.title"));
            f.getContentPane().add(this, BorderLayout.CENTER);
            f.pack();
            Rectangle screenRect = f.getGraphicsConfiguration().getBounds();
            Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(f.getGraphicsConfiguration());
            int centerWidth = screenRect.width < f.getSize().width ? screenRect.x : screenRect.x + screenRect.width / 2 - f.getSize().width / 2;
            int centerHeight = screenRect.height < f.getSize().height ? screenRect.y : screenRect.y + screenRect.height / 2 - f.getSize().height / 2;
            centerHeight = centerHeight < screenInsets.top ? screenInsets.top : centerHeight;
            f.setLocation(centerWidth, centerHeight);
            f.show();
            numSSs++;
            swingSets.add(this);
        }
    }

    /**
     * Show the spash screen while the rest of the demo loads
     */
    public void createSplashScreen() {
        splashLabel = new JLabel(createImageIcon("Splash.jpg", "Splash.accessible_description"));
        if (!isApplet()) {
            splashScreen = new JWindow(getFrame());
            splashScreen.getContentPane().add(splashLabel);
            splashScreen.pack();
            Rectangle screenRect = getFrame().getGraphicsConfiguration().getBounds();
            splashScreen.setLocation(screenRect.x + screenRect.width / 2 - splashScreen.getSize().width / 2, screenRect.y + screenRect.height / 2 - splashScreen.getSize().height / 2);
        }
    }

    public void showSplashScreen() {
        if (!isApplet()) {
            splashScreen.show();
        } else {
            add(splashLabel, BorderLayout.CENTER);
            validate();
            repaint();
        }
    }

    /**
     * pop down the spash screen
     */
    public void hideSplash() {
        if (!isApplet()) {
            splashScreen.setVisible(false);
            splashScreen = null;
            splashLabel = null;
        }
    }

    /**
     * Loads a demo from a classname
     */
    void loadDemo(String classname) {
        setStatus(getString("Status.loading") + getString(classname + ".name"));
        DemoModule demo = null;
        try {
            Class demoClass = Class.forName(classname);
            Constructor demoConstructor = demoClass.getConstructor(new Class[] { SwingSet2.class });
            demo = (DemoModule) demoConstructor.newInstance(new Object[] { this });
            addDemo(demo);
        } catch (Exception e) {
            System.out.println("Error occurred loading demo: " + classname);
        }
    }

    /**
     * A utility function that layers on top of the LookAndFeel's
     * isSupportedLookAndFeel() method. Returns true if the LookAndFeel
     * is supported. Returns false if the LookAndFeel is not supported
     * and/or if there is any kind of error checking if the LookAndFeel
     * is supported.
     * <p/>
     * The L&F menu will use this method to detemine whether the various
     * L&F options should be active or inactive.
     */
    protected boolean isAvailableLookAndFeel(String laf) {
        try {
            Class lnfClass = Class.forName(laf);
            LookAndFeel newLAF = (LookAndFeel) (lnfClass.newInstance());
            return newLAF.isSupportedLookAndFeel();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Determines if this is an applet or application
     */
    public boolean isApplet() {
        return (applet != null);
    }

    /**
     * Returns the applet instance
     */
    public SwingSet2Applet getApplet() {
        return applet;
    }

    /**
     * Returns the frame instance
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * Returns the menubar
     */
    public CommandBar getMenuBar() {
        return menuBar;
    }

    /**
     * Returns the toolbar
     */
    public ToggleButtonToolBar getToolBar() {
        return toolbar;
    }

    /**
     * Returns the toolbar button group
     */
    public ButtonGroup getToolBarGroup() {
        return toolbarGroup;
    }

    /**
     * Returns the content pane wether we're in an applet
     * or application
     */
    public Container getContentPane() {
        if (contentPane == null) {
            if (getFrame() != null) {
                contentPane = getFrame().getContentPane();
            } else if (getApplet() != null) {
                contentPane = getApplet().getContentPane();
            }
        }
        return contentPane;
    }

    /**
     * Create a frame for SwingSet2 to reside in if brought up
     * as an application.
     */
    public static JFrame createFrame(GraphicsConfiguration gc) {
        JFrame frame = new JFrame(gc);
        if (numSSs == 0) {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            WindowListener l = new WindowAdapter() {

                public void windowClosing(WindowEvent e) {
                    numSSs--;
                    swingSets.remove(this);
                }
            };
            frame.addWindowListener(l);
        }
        return frame;
    }

    /**
     * Set the status
     */
    public void setStatus(String s) {
        SwingUtilities.invokeLater(new SwingSetRunnable(this, s) {

            public void run() {
                swingset.statusField.setText((String) obj);
            }
        });
    }

    /**
     * This method returns a string from the demo's resource bundle.
     */
    public String getString(String key) {
        String value = null;
        try {
            value = getResourceBundle().getString(key);
        } catch (MissingResourceException e) {
            System.out.println("java.util.MissingResourceException: Couldn't find value for: " + key);
        }
        if (value == null) {
            value = "Could not find resource: " + key + "  ";
        }
        return value;
    }

    /**
     * Returns the resource bundle associated with this demo. Used
     * to get accessable and internationalized strings.
     */
    public ResourceBundle getResourceBundle() {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle("resources.swingset");
        }
        return bundle;
    }

    /**
     * Returns a mnemonic from the resource bundle. Typically used as
     * keyboard shortcuts in menu items.
     */
    public char getMnemonic(String key) {
        return (getString(key)).charAt(0);
    }

    /**
     * Creates an icon from an image contained in the "images" directory.
     */
    public ImageIcon createImageIcon(String filename, String description) {
        String path = "/resources/images/" + filename;
        return new ImageIcon(getClass().getResource(path));
    }

    /**
     * If DEBUG is defined, prints debug information out to std ouput.
     */
    public void debug(String s) {
        if (DEBUG) {
            System.out.println((debugCounter++) + ": " + s);
        }
    }

    /**
     * Stores the current L&F, and calls updateLookAndFeel, below
     */
    public void setLookAndFeel(String laf) {
        if (currentLookAndFeel != laf) {
            currentLookAndFeel = laf;
            String lafName = null;
            if (laf == mac) lafName = getString("LafMenu.mac_label");
            if (laf == metal) lafName = getString("LafMenu.java_label");
            if (laf == motif) lafName = getString("LafMenu.motif_label");
            if (laf == windows) lafName = getString("LafMenu.windows_label");
            if (laf == gtk) lafName = getString("LafMenu.gtk_label");
            themesMenu.setEnabled(laf == metal);
            updateLookAndFeel();
            for (int i = 0; i < lafMenu.getItemCount(); i++) {
                JMenuItem item = lafMenu.getItem(i);
                if (item.getText() == lafName) {
                    item.setSelected(true);
                } else {
                    item.setSelected(false);
                }
            }
        }
    }

    /**
     * Sets the current L&F on each demo module
     */
    public void updateLookAndFeel() {
        try {
            UIManager.setLookAndFeel(currentLookAndFeel);
            LookAndFeelFactory.installJideExtension(LookAndFeelFactory.OFFICE2003_STYLE);
            for (Iterator itr = swingSets.iterator(); itr.hasNext(); ) {
                SwingSet2 ss = (SwingSet2) itr.next();
                SwingUtilities.updateComponentTreeUI(ss);
            }
            if (!isApplet()) {
                SwingUtilities.updateComponentTreeUI(getFrame());
            } else {
                SwingUtilities.updateComponentTreeUI(getApplet());
            }
            SwingUtilities.updateComponentTreeUI(popupMenu);
        } catch (Exception ex) {
            System.out.println("Failed loading L&F: " + currentLookAndFeel);
            System.out.println(ex);
        }
        for (int i = 0; i < demosVector.size(); i++) {
            DemoModule demo = (DemoModule) demosVector.elementAt(i);
            if (currentDemo != demo) {
                SwingUtilities.invokeLater(new SwingSetRunnable(this, demo) {

                    public void run() {
                        SwingUtilities.updateComponentTreeUI(((DemoModule) obj).getDemoPanel());
                    }
                });
            }
        }
    }

    /**
     * Loads and puts the source code text into JEditorPane in the "Source Code" tab
     */
    public void setSourceCode(DemoModule demo) {
        SwingUtilities.invokeLater(new SwingSetRunnable(this, demo) {

            public void run() {
                swingset.demoSrcPane.setText(((DemoModule) obj).getSourceCode());
                swingset.demoSrcPane.setCaretPosition(0);
            }
        });
    }

    static Insets zeroInsets = new Insets(1, 1, 1, 1);

    protected class ToggleButtonToolBar extends CommandBar {

        public ToggleButtonToolBar() {
            super("ToolBar");
            getContext().setInitIndex(1);
        }

        JideToggleButton addToggleButton(Action a) {
            JideToggleButton tb = new JideToggleButton((String) a.getValue(Action.NAME), (Icon) a.getValue(Action.SMALL_ICON));
            tb.setMargin(zeroInsets);
            tb.setText(null);
            tb.setEnabled(a.isEnabled());
            tb.setToolTipText((String) a.getValue(Action.SHORT_DESCRIPTION));
            tb.setAction(a);
            add(tb);
            return tb;
        }
    }

    class ToolBarPanel extends JPanel implements ContainerListener {

        public boolean contains(int x, int y) {
            Component c = getParent();
            if (c != null) {
                Rectangle r = c.getBounds();
                return (x >= 0) && (x < r.width) && (y >= 0) && (y < r.height);
            } else {
                return super.contains(x, y);
            }
        }

        public void componentAdded(ContainerEvent e) {
            Container c = e.getContainer().getParent();
            if (c != null) {
                c.getParent().validate();
                c.getParent().repaint();
            }
        }

        public void componentRemoved(ContainerEvent e) {
            Container c = e.getContainer().getParent();
            if (c != null) {
                c.getParent().validate();
                c.getParent().repaint();
            }
        }
    }

    /**
     * Generic SwingSet2 runnable. This is intended to run on the
     * AWT gui event thread so as not to muck things up by doing
     * gui work off the gui thread. Accepts a SwingSet2 and an Object
     * as arguments, which gives subtypes of this class the two
     * "must haves" needed in most runnables for this demo.
     */
    class SwingSetRunnable implements Runnable {

        protected SwingSet2 swingset;

        protected Object obj;

        public SwingSetRunnable(SwingSet2 swingset, Object obj) {
            this.swingset = swingset;
            this.obj = obj;
        }

        public void run() {
        }
    }

    public class SwitchToDemoAction extends AbstractAction {

        SwingSet2 swingset;

        DemoModule demo;

        public SwitchToDemoAction(SwingSet2 swingset, DemoModule demo) {
            super(demo.getName(), demo.getIcon());
            this.swingset = swingset;
            this.demo = demo;
        }

        public void actionPerformed(ActionEvent e) {
            swingset.setDemo(demo);
        }
    }

    class OkAction extends AbstractAction {

        JDialog aboutBox;

        protected OkAction(JDialog aboutBox) {
            super("OkAction");
            this.aboutBox = aboutBox;
        }

        public void actionPerformed(ActionEvent e) {
            aboutBox.setVisible(false);
        }
    }

    class ChangeLookAndFeelAction extends AbstractAction {

        SwingSet2 swingset;

        String laf;

        protected ChangeLookAndFeelAction(SwingSet2 swingset, String laf) {
            super("ChangeTheme");
            this.swingset = swingset;
            this.laf = laf;
        }

        public void actionPerformed(ActionEvent e) {
            swingset.setLookAndFeel(laf);
        }
    }

    class ActivatePopupMenuAction extends AbstractAction {

        SwingSet2 swingset;

        JPopupMenu popup;

        protected ActivatePopupMenuAction(SwingSet2 swingset, JPopupMenu popup) {
            super("ActivatePopupMenu");
            this.swingset = swingset;
            this.popup = popup;
        }

        public void actionPerformed(ActionEvent e) {
            Dimension invokerSize = getSize();
            Dimension popupSize = popup.getPreferredSize();
            popup.show(swingset, (invokerSize.width - popupSize.width) / 2, (invokerSize.height - popupSize.height) / 2);
        }
    }

    class OnAudioAction extends AbstractAction {

        SwingSet2 swingset;

        protected OnAudioAction(SwingSet2 swingset) {
            super("Audio On");
            this.swingset = swingset;
        }

        public void actionPerformed(ActionEvent e) {
            UIManager.put("AuditoryCues.playList", UIManager.get("AuditoryCues.allAuditoryCues"));
            swingset.updateLookAndFeel();
        }
    }

    class DefaultAudioAction extends AbstractAction {

        SwingSet2 swingset;

        protected DefaultAudioAction(SwingSet2 swingset) {
            super("Audio Default");
            this.swingset = swingset;
        }

        public void actionPerformed(ActionEvent e) {
            UIManager.put("AuditoryCues.playList", UIManager.get("AuditoryCues.defaultCueList"));
            swingset.updateLookAndFeel();
        }
    }

    class OffAudioAction extends AbstractAction {

        SwingSet2 swingset;

        protected OffAudioAction(SwingSet2 swingset) {
            super("Audio Off");
            this.swingset = swingset;
        }

        public void actionPerformed(ActionEvent e) {
            UIManager.put("AuditoryCues.playList", UIManager.get("AuditoryCues.noAuditoryCues"));
            swingset.updateLookAndFeel();
        }
    }

    class ToolTipAction extends AbstractAction {

        SwingSet2 swingset;

        boolean status;

        protected ToolTipAction(SwingSet2 swingset, boolean status) {
            super("ToolTip Control");
            this.swingset = swingset;
            this.status = status;
        }

        public void actionPerformed(ActionEvent e) {
            ToolTipManager.sharedInstance().setEnabled(status);
        }
    }

    class ChangeThemeAction extends AbstractAction {

        SwingSet2 swingset;

        MetalTheme theme;

        protected ChangeThemeAction(SwingSet2 swingset, MetalTheme theme) {
            super("ChangeTheme");
            this.swingset = swingset;
            this.theme = theme;
        }

        public void actionPerformed(ActionEvent e) {
            MetalLookAndFeel.setCurrentTheme(theme);
            swingset.updateLookAndFeel();
        }
    }

    class ExitAction extends AbstractAction {

        SwingSet2 swingset;

        protected ExitAction(SwingSet2 swingset) {
            super("ExitAction");
            this.swingset = swingset;
        }

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    class AboutAction extends AbstractAction {

        SwingSet2 swingset;

        protected AboutAction(SwingSet2 swingset) {
            super("AboutAction");
            this.swingset = swingset;
        }

        public void actionPerformed(ActionEvent e) {
            if (aboutBox == null) {
                JPanel panel = new AboutPanel(swingset);
                panel.setLayout(new BorderLayout());
                aboutBox = new JDialog(swingset.getFrame(), getString("AboutBox.title"), false);
                aboutBox.getContentPane().add(panel, BorderLayout.CENTER);
                JPanel buttonpanel = new JPanel();
                buttonpanel.setOpaque(false);
                JButton button = (JButton) buttonpanel.add(new JButton(getString("AboutBox.ok_button_text")));
                panel.add(buttonpanel, BorderLayout.SOUTH);
                button.addActionListener(new OkAction(aboutBox));
            }
            aboutBox.pack();
            Point p = swingset.getLocationOnScreen();
            aboutBox.setLocation(p.x + 10, p.y + 10);
            aboutBox.show();
        }
    }

    class MultiScreenAction extends AbstractAction {

        static final int ALL_SCREENS = -1;

        int screen;

        protected MultiScreenAction(SwingSet2 swingset, int screen) {
            super("MultiScreenAction");
            this.screen = screen;
        }

        public void actionPerformed(ActionEvent e) {
            GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            if (screen == ALL_SCREENS) {
                for (int i = 0; i < gds.length; i++) {
                    SwingSet2 swingset = new SwingSet2(null, gds[i].getDefaultConfiguration());
                }
            } else {
                SwingSet2 swingset = new SwingSet2(null, gds[screen].getDefaultConfiguration());
            }
        }
    }

    class DemoLoadThread extends Thread {

        SwingSet2 swingset;

        public DemoLoadThread(SwingSet2 swingset) {
            this.swingset = swingset;
        }

        public void run() {
            swingset.loadDemos();
        }
    }

    class AboutPanel extends JPanel {

        ImageIcon aboutimage = null;

        SwingSet2 swingset = null;

        public AboutPanel(SwingSet2 swingset) {
            this.swingset = swingset;
            aboutimage = swingset.createImageIcon("About.jpg", "AboutBox.accessible_description");
            setOpaque(false);
        }

        public void paint(Graphics g) {
            aboutimage.paintIcon(this, g, 0, 0);
            super.paint(g);
        }

        public Dimension getPreferredSize() {
            return new Dimension(aboutimage.getIconWidth(), aboutimage.getIconHeight());
        }
    }

    DockableBarManager _dockableBarManager;

    public DockableBarManager getDockableBarManager() {
        return _dockableBarManager;
    }
}

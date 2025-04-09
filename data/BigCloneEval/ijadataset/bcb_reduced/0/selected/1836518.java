package megamek.client.ui.swing;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import keypoint.PngEncoder;
import megamek.client.Client;
import megamek.client.bot.TestBot;
import megamek.client.event.BoardViewEvent;
import megamek.client.event.BoardViewListener;
import megamek.client.ui.GBC;
import megamek.client.ui.IBoardView;
import megamek.client.ui.Messages;
import megamek.client.ui.swing.util.PlayerColors;
import megamek.common.Coords;
import megamek.common.Entity;
import megamek.common.EntityListFile;
import megamek.common.IGame;
import megamek.common.MechSummaryCache;
import megamek.common.Player;
import megamek.common.event.GameEndEvent;
import megamek.common.event.GameListener;
import megamek.common.event.GameListenerAdapter;
import megamek.common.event.GameMapQueryEvent;
import megamek.common.event.GamePhaseChangeEvent;
import megamek.common.event.GamePlayerChatEvent;
import megamek.common.event.GamePlayerConnectedEvent;
import megamek.common.event.GamePlayerDisconnectedEvent;
import megamek.common.event.GameReportEvent;
import megamek.common.event.GameSettingsChangeEvent;
import megamek.common.preference.PreferenceManager;
import megamek.common.util.Distractable;
import megamek.common.util.StringUtil;

public class ClientGUI extends JPanel implements WindowListener, BoardViewListener, ActionListener {

    private static final long serialVersionUID = 3913466735610109147L;

    public static final String VIEW_MEK_DISPLAY = "viewMekDisplay";

    public static final String VIEW_MINI_MAP = "viewMiniMap";

    public static final String VIEW_LOS_SETTING = "viewLOSSetting";

    public static final String VIEW_UNIT_OVERVIEW = "viewUnitOverview";

    public static final String VIEW_ZOOM_IN = "viewZoomIn";

    public static final String VIEW_ZOOM_OUT = "viewZoomOut";

    public static final String VIEW_TOGGLE_ISOMETRIC = "viewToggleIsometric";

    public JFrame frame;

    protected CommonMenuBar menuBar;

    private CommonAboutDialog about;

    private CommonHelpDialog help;

    private CommonSettingsDialog setdlg;

    private String helpFileName = "readme.txt";

    ChatterBox cb;

    ChatterBox2 cb2;

    public IBoardView bv;

    private Component bvc;

    public JDialog mechW;

    public MechDisplay mechD;

    public JDialog minimapW;

    public MiniMap minimap;

    private MapMenu popup;

    private UnitOverview uo;

    private Ruler ruler;

    protected JComponent curPanel;

    public ChatLounge chatlounge;

    GameOptionsDialog gameOptionsDialog;

    private MechSelectorDialog mechSelectorDialog;

    private CustomFighterSquadronDialog customFSDialog;

    private StartingPositionDialog startingPositionDialog;

    private PlayerListDialog playerListDialog;

    private RandomArmyDialog randomArmyDialog;

    private RandomSkillDialog randomSkillDialog;

    private RandomNameDialog randomNameDialog;

    private PlanetaryConditionsDialog conditionsDialog;

    /**
     * Save and Open dialogs for MegaMek Unit List (mul) files.
     */
    private JFileChooser dlgLoadList;

    private JFileChooser dlgSaveList;

    private Client client;

    private File curfileBoardImage;

    private File curfileBoard;

    /**
     * Cache for the "bing" soundclip.
     */
    private AudioClip bingClip;

    /**
     * Map each phase to the name of the card for the main display area.
     */
    private HashMap<String, String> mainNames = new HashMap<String, String>();

    /**
     * The <code>JPanel</code> containing the main display area.
     */
    private JPanel panMain = new JPanel();

    /**
     * The <code>CardLayout</code> of the main display area.
     */
    private CardLayout cardsMain = new CardLayout();

    /**
     * Map each phase to the name of the card for the secondary area.
     */
    private HashMap<String, String> secondaryNames = new HashMap<String, String>();

    /**
     * The <code>JPanel</code> containing the secondary display area.
     */
    private JPanel panSecondary = new JPanel();

    /**
     * The <code>CardLayout</code> of the secondary display area.
     */
    private CardLayout cardsSecondary = new CardLayout();

    /**
     * Map phase component names to phase component objects.
     */
    HashMap<String, JComponent> phaseComponents = new HashMap<String, JComponent>();

    /**
     * Current Selected entity
     */
    private int selectedEntityNum = Entity.NONE;

    /**
     * Construct a client which will display itself in a new frame. It will not
     * try to connect to a server yet. When the frame closes, this client will
     * clean up after itself as much as possible, but will not call
     * System.exit().
     */
    public ClientGUI(Client client) {
        super(new BorderLayout());
        this.client = client;
        loadSoundClip();
        panMain.setLayout(cardsMain);
        panSecondary.setLayout(cardsSecondary);
        JPanel panDisplay = new JPanel(new BorderLayout());
        panDisplay.add(panMain, BorderLayout.CENTER);
        panDisplay.add(panSecondary, BorderLayout.SOUTH);
        add(panDisplay, BorderLayout.CENTER);
    }

    public IBoardView getBoardView() {
        return bv;
    }

    /**
     * Try to load the "bing" sound clip.
     */
    private void loadSoundClip() {
        if (GUIPreferences.getInstance().getSoundBingFilename() == null) {
            return;
        }
        try {
            File file = new File(GUIPreferences.getInstance().getSoundBingFilename());
            if (!file.exists()) {
                System.err.println("Failed to load audio file: " + GUIPreferences.getInstance().getSoundBingFilename());
                return;
            }
            bingClip = Applet.newAudioClip(file.toURI().toURL());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Display a system message in the chat box.
     *
     * @param message
     *            the <code>String</code> message to be shown.
     */
    public void systemMessage(String message) {
        cb.systemMessage(message);
        cb2.addChatMessage("Megamek: " + message);
    }

    /**
     * Initializes a number of things about this frame.
     */
    private void initializeFrame() {
        frame = new JFrame(Messages.getString("ClientGUI.title"));
        menuBar.setGame(client.game);
        frame.setJMenuBar(menuBar);
        Rectangle virtualBounds = new Rectangle();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        for (GraphicsDevice gd : gs) {
            GraphicsConfiguration[] gc = gd.getConfigurations();
            for (GraphicsConfiguration element : gc) {
                virtualBounds = virtualBounds.union(element.getBounds());
            }
        }
        if (GUIPreferences.getInstance().getWindowSizeHeight() != 0) {
            int x = GUIPreferences.getInstance().getWindowPosX();
            int y = GUIPreferences.getInstance().getWindowPosY();
            int w = GUIPreferences.getInstance().getWindowSizeWidth();
            int h = GUIPreferences.getInstance().getWindowSizeHeight();
            if ((x < virtualBounds.getMinX()) || ((x + w) > virtualBounds.getMaxX())) {
                x = 0;
            }
            if ((y < virtualBounds.getMinY()) || ((y + h) > virtualBounds.getMaxY())) {
                y = 0;
            }
            if (w > virtualBounds.getWidth()) {
                w = (int) virtualBounds.getWidth();
            }
            if (h > virtualBounds.getHeight()) {
                h = (int) virtualBounds.getHeight();
            }
            frame.setLocation(x, y);
            frame.setSize(w, h);
        } else {
            frame.setSize(800, 600);
        }
        frame.setBackground(SystemColor.menu);
        frame.setForeground(SystemColor.menuText);
        List<Image> iconList = new ArrayList<Image>();
        iconList.add(frame.getToolkit().getImage("data/images/misc/megamek-icon-16x16.png"));
        iconList.add(frame.getToolkit().getImage("data/images/misc/megamek-icon-32x32.png"));
        iconList.add(frame.getToolkit().getImage("data/images/misc/megamek-icon-48x48.png"));
        iconList.add(frame.getToolkit().getImage("data/images/misc/megamek-icon-256x256.png"));
        frame.setIconImages(iconList);
    }

    /**
     * Lays out the frame by setting this Client object to take up the full
     * frame display area.
     */
    private void layoutFrame() {
        frame.setTitle(client.getName() + Messages.getString("ClientGUI.clientTitleSuffix"));
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(this, BorderLayout.CENTER);
        frame.validate();
    }

    /**
     * Have the client register itself as a listener wherever it's needed.
     * <p/>
     * According to
     * http://www-106.ibm.com/developerworks/java/library/j-jtp0618.html it is a
     * major bad no-no to perform these registrations before the constructor
     * finishes, so this function has to be called after the <code>Client</code>
     * is created.
     */
    public void initialize() {
        menuBar = new CommonMenuBar(getClient());
        initializeFrame();
        try {
            client.game.addGameListener(gameListener);
            Class<?> c = getClass().getClassLoader().loadClass(System.getProperty("megamek.client.ui.AWT.boardView", "megamek.client.ui.swing.BoardView1"));
            bv = (IBoardView) c.getConstructor(IGame.class).newInstance(client.game);
            bvc = bv.getComponent();
            bvc.setName("BoardView");
            bv.addBoardViewListener(this);
        } catch (Exception e) {
            e.printStackTrace();
            doAlertDialog(Messages.getString("ClientGUI.FatalError.title"), Messages.getString("ClientGUI.FatalError.message") + e);
            die();
        }
        layoutFrame();
        frame.setVisible(true);
        menuBar.addActionListener(this);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                frame.setVisible(false);
                saveSettings();
                die();
            }
        });
        UnitLoadingDialog unitLoadingDialog = new UnitLoadingDialog(frame);
        if (!MechSummaryCache.getInstance().isInitialized()) {
            unitLoadingDialog.setVisible(true);
        }
        cb2 = new ChatterBox2(this, bv);
        bv.addDisplayable(cb2);
        bv.addKeyListener(cb2);
        uo = new UnitOverview(this);
        bv.addDisplayable(uo);
        Dimension screenSize = frame.getToolkit().getScreenSize();
        int x;
        int y;
        int h;
        int w;
        mechW = new JDialog(frame, Messages.getString("ClientGUI.MechDisplay"), false);
        x = GUIPreferences.getInstance().getDisplayPosX();
        y = GUIPreferences.getInstance().getDisplayPosY();
        h = GUIPreferences.getInstance().getDisplaySizeHeight();
        w = GUIPreferences.getInstance().getDisplaySizeWidth();
        if ((x + w) > screenSize.width) {
            x = 0;
            w = Math.min(w, screenSize.width);
        }
        if ((y + h) > screenSize.height) {
            y = 0;
            h = Math.min(h, screenSize.height);
        }
        mechW.setLocation(x, y);
        mechW.setSize(w, h);
        mechW.setResizable(true);
        mechW.addWindowListener(this);
        mechD = new MechDisplay(this);
        mechD.addMechDisplayListener(bv);
        mechW.add(mechD);
        Ruler.color1 = GUIPreferences.getInstance().getRulerColor1();
        Ruler.color2 = GUIPreferences.getInstance().getRulerColor2();
        ruler = new Ruler(frame, client, bv);
        x = GUIPreferences.getInstance().getRulerPosX();
        y = GUIPreferences.getInstance().getRulerPosY();
        h = GUIPreferences.getInstance().getRulerSizeHeight();
        w = GUIPreferences.getInstance().getRulerSizeWidth();
        if ((x + w) > screenSize.width) {
            x = 0;
            w = Math.min(w, screenSize.width);
        }
        if ((y + h) > screenSize.height) {
            y = 0;
            h = Math.min(h, screenSize.height);
        }
        ruler.setLocation(x, y);
        ruler.setSize(w, h);
        minimapW = new JDialog(frame, Messages.getString("ClientGUI.MiniMap"), false);
        x = GUIPreferences.getInstance().getMinimapPosX();
        y = GUIPreferences.getInstance().getMinimapPosY();
        try {
            minimap = new MiniMap(minimapW, this, bv);
        } catch (IOException e) {
            e.printStackTrace();
            doAlertDialog(Messages.getString("ClientGUI.FatalError.title"), Messages.getString("ClientGUI.FatalError.message1") + e);
            die();
        }
        h = minimap.getSize().height;
        w = minimap.getSize().width;
        if (((x + 10) >= screenSize.width) || ((x + w) < 10)) {
            x = screenSize.width - w;
        }
        if (((y + 10) > screenSize.height) || ((y + h) < 10)) {
            y = screenSize.height - h;
        }
        minimapW.setLocation(x, y);
        minimapW.addWindowListener(this);
        minimapW.add(minimap);
        cb = new ChatterBox(this);
        cb.setChatterBox2(cb2);
        cb2.setChatterBox(cb);
        client.changePhase(IGame.Phase.PHASE_UNKNOWN);
        mechSelectorDialog = new MechSelectorDialog(this, unitLoadingDialog);
        customFSDialog = new CustomFighterSquadronDialog(this, unitLoadingDialog);
        randomArmyDialog = new RandomArmyDialog(this);
        randomSkillDialog = new RandomSkillDialog(this);
        randomNameDialog = new RandomNameDialog(this);
        new Thread(mechSelectorDialog, "Mech Selector Dialog").start();
    }

    /**
     * Get the menu bar for this client.
     *
     * @return the <code>CommonMenuBar</code> of this client.
     */
    public CommonMenuBar getMenuBar() {
        return menuBar;
    }

    /**
     * Called when the user selects the "Help->About" menu item.
     */
    private void showAbout() {
        if (about == null) {
            about = new CommonAboutDialog(frame);
        }
        about.setVisible(true);
    }

    /**
     * Called when the user selects the "Help->Contents" menu item.
     * <p/>
     * This method can be called by subclasses.
     */
    private void showHelp() {
        if (help == null) {
            help = new CommonHelpDialog(frame, new File(helpFileName));
        }
        help.setVisible(true);
    }

    /**
     * Called when the user selects the "View->Client Settings" menu item.
     */
    private void showSettings() {
        if (setdlg == null) {
            setdlg = new CommonSettingsDialog(frame);
        }
        setdlg.setVisible(true);
    }

    /**
     * Called when the user selects the "View->Game Options" menu item.
     */
    private void showOptions() {
        if (client.game.getPhase() == IGame.Phase.PHASE_LOUNGE) {
            getGameOptionsDialog().setEditable(true);
        } else {
            getGameOptionsDialog().setEditable(false);
        }
        getGameOptionsDialog().update(client.game.getOptions());
        getGameOptionsDialog().setVisible(true);
    }

    /**
     * Called when the user selects the "View->Player List" menu item.
     */
    private void showPlayerList() {
        if (playerListDialog == null) {
            playerListDialog = new PlayerListDialog(frame, client);
        }
        playerListDialog.setVisible(true);
    }

    /**
     * Called when the user selects the "View->Round Report" menu item.
     */
    private void showRoundReport() {
        new MiniReportDisplay(frame, client.roundReport).setVisible(true);
    }

    /**
     * Implement the <code>ActionListener</code> interface.
     */
    public void actionPerformed(ActionEvent event) {
        if ("fileGameSave".equalsIgnoreCase(event.getActionCommand())) {
            JFileChooser fc = new JFileChooser(".");
            fc.setLocation(frame.getLocation().x + 150, frame.getLocation().y + 100);
            fc.setDialogTitle(Messages.getString("ClientGUI.FileSaveDialog.title"));
            int returnVal = fc.showSaveDialog(frame);
            if ((returnVal != JFileChooser.APPROVE_OPTION) || (fc.getSelectedFile() == null)) {
                return;
            }
            if (fc.getSelectedFile() != null) {
                String file = fc.getSelectedFile().getAbsolutePath();
                file = file.replace(" ", "|");
                client.sendChat("/save " + file);
            }
        }
        if ("helpAbout".equalsIgnoreCase(event.getActionCommand())) {
            showAbout();
        }
        if ("helpContents".equalsIgnoreCase(event.getActionCommand())) {
            showHelp();
        }
        if ("fileUnitsSave".equalsIgnoreCase(event.getActionCommand())) {
            doSaveUnit();
        }
        if ("viewClientSettings".equalsIgnoreCase(event.getActionCommand())) {
            showSettings();
        }
        if ("viewGameOptions".equalsIgnoreCase(event.getActionCommand())) {
            showOptions();
        }
        if ("viewPlayerList".equalsIgnoreCase(event.getActionCommand())) {
            showPlayerList();
        }
        if ("viewRoundReport".equalsIgnoreCase(event.getActionCommand())) {
            showRoundReport();
        }
        if ("fileBoardSave".equalsIgnoreCase(event.getActionCommand())) {
            boardSave();
        } else if ("fileBoardSaveAs".equalsIgnoreCase(event.getActionCommand())) {
            boardSaveAs();
        } else if ("fileBoardSaveAsImage".equalsIgnoreCase(event.getActionCommand())) {
            boardSaveAsImage();
        }
        if (event.getActionCommand().equals(VIEW_MEK_DISPLAY)) {
            toggleDisplay();
        } else if (event.getActionCommand().equals(VIEW_MINI_MAP)) {
            toggleMap();
        } else if (event.getActionCommand().equals(VIEW_UNIT_OVERVIEW)) {
            toggleUnitOverview();
        } else if (event.getActionCommand().equals(VIEW_LOS_SETTING)) {
            showLOSSettingDialog();
        } else if (event.getActionCommand().equals(VIEW_ZOOM_IN)) {
            bv.zoomIn();
        } else if (event.getActionCommand().equals(VIEW_ZOOM_OUT)) {
            bv.zoomOut();
        } else if (event.getActionCommand().equals(VIEW_TOGGLE_ISOMETRIC)) {
            GUIPreferences.getInstance().setIsometricEnabled(bv.toggleIsometric());
        }
    }

    /**
     * Save all the current in use Entities each grouped by
     * player name
     *
     * and a file for salvage
     */
    public void doSaveUnit() {
        for (Enumeration<Player> iter = getClient().game.getPlayers(); iter.hasMoreElements(); ) {
            Player p = iter.nextElement();
            ArrayList<Entity> l = getClient().game.getPlayerEntities(p, false);
            for (Enumeration<Entity> iter2 = getClient().game.getRetreatedEntities(); iter2.hasMoreElements(); ) {
                Entity e = iter2.nextElement();
                if (e.getOwnerId() == p.getId()) {
                    l.add(e);
                }
            }
            saveListFile(l, p.getName());
        }
        ArrayList<Entity> destroyed = new ArrayList<Entity>();
        Enumeration<Entity> graveyard = getClient().game.getGraveyardEntities();
        while (graveyard.hasMoreElements()) {
            Entity entity = graveyard.nextElement();
            if (entity.isSalvage()) {
                destroyed.add(entity);
            }
        }
        if (destroyed.size() > 0) {
            String sLogDir = PreferenceManager.getClientPreferences().getLogDirectory();
            File logDir = new File(sLogDir);
            if (!logDir.exists()) {
                logDir.mkdir();
            }
            String fileName = "salvage.mul";
            if (PreferenceManager.getClientPreferences().stampFilenames()) {
                fileName = StringUtil.addDateTimeStamp(fileName);
            }
            File unitFile = new File(sLogDir + File.separator + fileName);
            try {
                EntityListFile.saveTo(unitFile, destroyed);
            } catch (IOException excep) {
                excep.printStackTrace(System.err);
                doAlertDialog(Messages.getString("ClientGUI.errorSavingFile"), excep.getMessage());
            }
        }
    }

    /**
     * Saves the current settings to the cfg file.
     */
    void saveSettings() {
        GUIPreferences.getInstance().setWindowPosX(frame.getLocation().x);
        GUIPreferences.getInstance().setWindowPosY(frame.getLocation().y);
        GUIPreferences.getInstance().setWindowSizeWidth(frame.getSize().width);
        GUIPreferences.getInstance().setWindowSizeHeight(frame.getSize().height);
        if ((minimapW != null) && ((minimapW.getSize().width * minimapW.getSize().height) > 0)) {
            GUIPreferences.getInstance().setMinimapPosX(minimapW.getLocation().x);
            GUIPreferences.getInstance().setMinimapPosY(minimapW.getLocation().y);
            GUIPreferences.getInstance().setMinimapZoom(minimap.getZoom());
        }
        if ((mechW != null) && ((mechW.getSize().width * mechW.getSize().height) > 0)) {
            GUIPreferences.getInstance().setDisplayPosX(mechW.getLocation().x);
            GUIPreferences.getInstance().setDisplayPosY(mechW.getLocation().y);
            GUIPreferences.getInstance().setDisplaySizeWidth(mechW.getSize().width);
            GUIPreferences.getInstance().setDisplaySizeHeight(mechW.getSize().height);
        }
        if ((ruler != null) && (ruler.getSize().width != 0) && (ruler.getSize().height != 0)) {
            GUIPreferences.getInstance().setRulerPosX(ruler.getLocation().x);
            GUIPreferences.getInstance().setRulerPosY(ruler.getLocation().y);
            GUIPreferences.getInstance().setRulerSizeWidth(ruler.getSize().width);
            GUIPreferences.getInstance().setRulerSizeHeight(ruler.getSize().height);
        }
    }

    /**
     * Shuts down threads and sockets
     */
    void die() {
        boolean reportHandled = false;
        Iterator<String> names = phaseComponents.keySet().iterator();
        while (names.hasNext()) {
            JComponent component = phaseComponents.get(names.next());
            if (component instanceof ReportDisplay) {
                if (reportHandled) {
                    continue;
                }
                reportHandled = true;
            }
            if (component instanceof Distractable) {
                ((Distractable) component).removeAllListeners();
            }
        }
        frame.removeAll();
        frame.setVisible(false);
        try {
            frame.dispose();
        } catch (Throwable error) {
            error.printStackTrace();
        }
        client.die();
        if (chatlounge != null) {
            chatlounge.die();
        }
    }

    public GameOptionsDialog getGameOptionsDialog() {
        if (gameOptionsDialog == null) {
            gameOptionsDialog = new GameOptionsDialog(this);
        }
        return gameOptionsDialog;
    }

    public MechSelectorDialog getMechSelectorDialog() {
        return mechSelectorDialog;
    }

    public CustomFighterSquadronDialog getCustomFSDialog() {
        return customFSDialog;
    }

    public StartingPositionDialog getStartingPositionDialog() {
        if (startingPositionDialog == null) {
            startingPositionDialog = new StartingPositionDialog(this);
        }
        return startingPositionDialog;
    }

    public PlanetaryConditionsDialog getPlanetaryConditionsDialog() {
        if (conditionsDialog == null) {
            conditionsDialog = new PlanetaryConditionsDialog(this);
        }
        return conditionsDialog;
    }

    void switchPanel(IGame.Phase phase) {
        if (curPanel instanceof BoardViewListener) {
            bv.removeBoardViewListener((BoardViewListener) curPanel);
        }
        if (curPanel instanceof ActionListener) {
            menuBar.removeActionListener((ActionListener) curPanel);
        }
        if (curPanel instanceof Distractable) {
            ((Distractable) curPanel).setIgnoringEvents(true);
        }
        String name = String.valueOf(phase);
        curPanel = phaseComponents.get(name);
        if (curPanel == null) {
            curPanel = initializePanel(phase);
        }
        switch(phase) {
            case PHASE_LOUNGE:
                ReportDisplay rD = (ReportDisplay) phaseComponents.get(String.valueOf(IGame.Phase.PHASE_INITIATIVE_REPORT));
                if (rD != null) {
                    rD.resetTabs();
                }
                ChatLounge cl = (ChatLounge) phaseComponents.get(String.valueOf(IGame.Phase.PHASE_LOUNGE));
                cb.setDoneButton(cl.butDone);
                cl.add(cb.getComponent(), BorderLayout.SOUTH);
                getBoardView().getTilesetManager().reset();
                break;
            case PHASE_DEPLOY_MINEFIELDS:
            case PHASE_DEPLOYMENT:
            case PHASE_TARGETING:
            case PHASE_MOVEMENT:
            case PHASE_OFFBOARD:
            case PHASE_FIRING:
            case PHASE_PHYSICAL:
                if (GUIPreferences.getInstance().getMinimapEnabled() && !minimapW.isVisible()) {
                    setMapVisible(true);
                }
                break;
            case PHASE_INITIATIVE_REPORT:
            case PHASE_TARGETING_REPORT:
            case PHASE_MOVEMENT_REPORT:
            case PHASE_OFFBOARD_REPORT:
            case PHASE_FIRING_REPORT:
            case PHASE_PHYSICAL_REPORT:
            case PHASE_END_REPORT:
            case PHASE_VICTORY:
                rD = (ReportDisplay) phaseComponents.get(String.valueOf(IGame.Phase.PHASE_INITIATIVE_REPORT));
                cb.setDoneButton(rD.butDone);
                rD.add(cb.getComponent(), GBC.eol().fill(GridBagConstraints.HORIZONTAL));
                setMapVisible(false);
                mechW.setVisible(false);
                break;
            default:
                break;
        }
        cardsMain.show(panMain, mainNames.get(name));
        String secondaryToShow = secondaryNames.get(name);
        if (secondaryToShow != null) {
            panSecondary.setVisible(true);
            cardsSecondary.show(panSecondary, secondaryNames.get(name));
        } else {
            panSecondary.setVisible(false);
        }
        if (curPanel instanceof BoardViewListener) {
            bv.addBoardViewListener((BoardViewListener) curPanel);
        }
        if (curPanel instanceof ActionListener) {
            menuBar.addActionListener((ActionListener) curPanel);
        }
        if (curPanel instanceof Distractable) {
            ((Distractable) curPanel).setIgnoringEvents(false);
        }
        if (GUIPreferences.getInstance().getFocus() && !(client instanceof TestBot)) {
            curPanel.requestFocus();
        }
    }

    private JComponent initializePanel(IGame.Phase phase) {
        String name = String.valueOf(phase);
        JComponent component;
        String secondary = null;
        String main;
        switch(phase) {
            case PHASE_LOUNGE:
                component = new ChatLounge(this);
                chatlounge = (ChatLounge) component;
                main = "ChatLounge";
                component.setName(main);
                panMain.add(component, main);
                break;
            case PHASE_STARTING_SCENARIO:
                component = new JLabel(Messages.getString("ClientGUI.StartingScenario"));
                main = "JLabel-StartingScenario";
                component.setName(main);
                panMain.add(component, main);
                break;
            case PHASE_EXCHANGE:
                component = new JLabel(Messages.getString("ClientGUI.TransmittingData"));
                main = "JLabel-Exchange";
                component.setName(main);
                panMain.add(component, main);
                break;
            case PHASE_SET_ARTYAUTOHITHEXES:
                component = new SelectArtyAutoHitHexDisplay(this);
                main = "BoardView";
                secondary = "SelectArtyAutoHitHexDisplay";
                component.setName(secondary);
                if (!mainNames.containsValue(main)) {
                    panMain.add(bvc, main);
                }
                panSecondary.add(component, secondary);
                break;
            case PHASE_DEPLOY_MINEFIELDS:
                component = new DeployMinefieldDisplay(this);
                main = "BoardView";
                secondary = "DeployMinefieldDisplay";
                component.setName(secondary);
                if (!mainNames.containsValue(main)) {
                    panMain.add(bvc, main);
                }
                panSecondary.add(component, secondary);
                break;
            case PHASE_DEPLOYMENT:
                component = new DeploymentDisplay(this);
                main = "BoardView";
                secondary = "DeploymentDisplay";
                component.setName(secondary);
                if (!mainNames.containsValue(main)) {
                    panMain.add(bvc, main);
                }
                panSecondary.add(component, secondary);
                break;
            case PHASE_TARGETING:
                component = new TargetingPhaseDisplay(this, false);
                ((TargetingPhaseDisplay) component).initializeListeners();
                main = "BoardView";
                secondary = "TargetingPhaseDisplay";
                component.setName(secondary);
                if (!mainNames.containsValue(main)) {
                    panMain.add(bvc, main);
                }
                panSecondary.add(component, secondary);
                break;
            case PHASE_MOVEMENT:
                component = new MovementDisplay(this);
                main = "BoardView";
                secondary = "MovementDisplay";
                component.setName(secondary);
                if (!mainNames.containsValue(main)) {
                    panMain.add(bvc, main);
                }
                panSecondary.add(component, secondary);
                break;
            case PHASE_OFFBOARD:
                component = new TargetingPhaseDisplay(this, true);
                ((TargetingPhaseDisplay) component).initializeListeners();
                main = "BoardView";
                secondary = "OffboardDisplay";
                component.setName(secondary);
                if (!mainNames.containsValue(main)) {
                    panMain.add(bvc, main);
                }
                panSecondary.add(component, secondary);
                break;
            case PHASE_FIRING:
                component = new FiringDisplay(this);
                main = "BoardView";
                secondary = "FiringDisplay";
                component.setName(secondary);
                if (!mainNames.containsValue(main)) {
                    panMain.add(bvc, main);
                }
                panSecondary.add(component, secondary);
                break;
            case PHASE_PHYSICAL:
                component = new PhysicalDisplay(this);
                main = "BoardView";
                secondary = "PhysicalDisplay";
                component.setName(secondary);
                if (!mainNames.containsValue(main)) {
                    panMain.add(bvc, main);
                }
                panSecondary.add(component, secondary);
                break;
            case PHASE_INITIATIVE_REPORT:
                component = new ReportDisplay(this);
                main = "ReportDisplay";
                component.setName(main);
                panMain.add(main, component);
                break;
            case PHASE_TARGETING_REPORT:
            case PHASE_MOVEMENT_REPORT:
            case PHASE_OFFBOARD_REPORT:
            case PHASE_FIRING_REPORT:
            case PHASE_PHYSICAL_REPORT:
            case PHASE_END_REPORT:
            case PHASE_VICTORY:
                component = phaseComponents.get(String.valueOf(IGame.Phase.PHASE_INITIATIVE_REPORT));
                if (component == null) {
                    component = initializePanel(IGame.Phase.PHASE_INITIATIVE_REPORT);
                }
                main = "ReportDisplay";
                break;
            default:
                component = new JLabel(Messages.getString("ClientGUI.waitingOnTheServer"));
                main = "JLabel-Default";
                secondary = main;
                component.setName(main);
                panMain.add(main, component);
        }
        phaseComponents.put(name, component);
        mainNames.put(name, main);
        if (secondary != null) {
            secondaryNames.put(name, secondary);
        }
        return component;
    }

    protected void showBoardPopup(Coords c) {
        if (fillPopup(c)) {
            bv.showPopup(popup, c);
        }
    }

    /**
     * Toggles the entity display window
     */
    private void toggleDisplay() {
        mechW.setVisible(!mechW.isVisible());
        if (mechW.isVisible()) {
            frame.requestFocus();
        }
    }

    /**
     * Sets the visibility of the entity display window
     */
    public void setDisplayVisible(boolean visible) {
        mechW.setVisible(visible);
        if (visible) {
            frame.requestFocus();
        }
    }

    private void toggleUnitOverview() {
        uo.setVisible(!uo.isVisible());
        bv.refreshDisplayables();
    }

    /**
     * Toggles the minimap window Also, toggles the minimap enabled setting
     */
    private void toggleMap() {
        if (minimapW.isVisible()) {
            GUIPreferences.getInstance().setMinimapEnabled(false);
        } else {
            GUIPreferences.getInstance().setMinimapEnabled(true);
        }
        minimapW.setVisible(!minimapW.isVisible());
        if (minimapW.isVisible()) {
            frame.requestFocus();
        }
    }

    /**
     * Sets the visibility of the minimap window
     */
    void setMapVisible(boolean visible) {
        minimapW.setVisible(visible);
        if (visible) {
            frame.requestFocus();
        }
    }

    private boolean fillPopup(Coords coords) {
        popup = new MapMenu(coords, client, curPanel, this);
        return popup.getHasMenu();
    }

    /**
     * Pops up a dialog box giving the player a series of choices that are not
     * mutually exclusive.
     *
     * @param title
     *            the <code>String</code> title of the dialog box.
     * @param question
     *            the <code>String</code> question that has a "Yes" or "No"
     *            answer. The question will be split across multiple line on the
     *            '\n' characters.
     * @param choices
     *            the array of <code>String</code> choices that the player can
     *            select from.
     * @return The array of the <code>int</code> indexes of the from the input
     *         array that match the selected choices. If no choices were
     *         available, if the player did not select a choice, or if the
     *         player canceled the choice, a <code>null</code> value is
     *         returned.
     */
    public int[] doChoiceDialog(String title, String question, String[] choices) {
        ChoiceDialog choice = new ChoiceDialog(frame, title, question, choices);
        choice.setVisible(true);
        return choice.getChoices();
    }

    /**
     * Pops up a dialog box showing an alert
     */
    public void doAlertDialog(String title, String message) {
        JTextPane textArea = new JTextPane();
        ReportDisplay.setupStylesheet(textArea);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        textArea.setText("<pre>" + message + "</pre>");
        JOptionPane.showMessageDialog(frame, scrollPane, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Pops up a dialog box asking a yes/no question
     *
     * @param title
     *            the <code>String</code> title of the dialog box.
     * @param question
     *            the <code>String</code> question that has a "Yes" or "No"
     *            answer. The question will be split across multiple line on the
     *            '\n' characters.
     * @return <code>true</code> if yes
     */
    public boolean doYesNoDialog(String title, String question) {
        ConfirmDialog confirm = new ConfirmDialog(frame, title, question);
        confirm.setVisible(true);
        return confirm.getAnswer();
    }

    /**
     * Pops up a dialog box asking a yes/no question
     * <p/>
     * The player will be given a chance to not show the dialog again.
     *
     * @param title
     *            the <code>String</code> title of the dialog box.
     * @param question
     *            the <code>String</code> question that has a "Yes" or "No"
     *            answer. The question will be split across multiple line on the
     *            '\n' characters.
     * @return the <code>ConfirmDialog</code> containing the player's responses.
     *         The dialog will already have been shown to the player, and is
     *         only being returned so the calling function can see the answer to
     *         the question and the state of the "Show again?" question.
     */
    public ConfirmDialog doYesNoBotherDialog(String title, String question) {
        ConfirmDialog confirm = new ConfirmDialog(frame, title, question, true);
        confirm.setVisible(true);
        return confirm;
    }

    /**
     * Allow the player to select a MegaMek Unit List file to load. The
     * <code>Entity</code>s in the file will replace any that the player has
     * already selected. As such, this method should only be called in the chat
     * lounge. The file can record damage sustained, non- standard munitions
     * selected, and ammunition expended in a prior engagement.
     */
    protected void loadListFile() {
        if (dlgLoadList == null) {
            dlgLoadList = new JFileChooser(".");
            dlgLoadList.setLocation(frame.getLocation().x + 150, frame.getLocation().y + 100);
            dlgLoadList.setDialogTitle(Messages.getString("ClientGUI.openUnitListFileDialog.title"));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Mul Files", "mul");
            dlgLoadList.setFileFilter(filter);
        }
        dlgLoadList.setSelectedFile(new File(client.getLocalPlayer().getName() + ".mul"));
        int returnVal = dlgLoadList.showOpenDialog(frame);
        if ((returnVal != JFileChooser.APPROVE_OPTION) || (dlgLoadList.getSelectedFile() == null)) {
            return;
        }
        File unitFile = dlgLoadList.getSelectedFile();
        if (unitFile != null) {
            try {
                Vector<Entity> loadedUnits = EntityListFile.loadFrom(unitFile);
                for (Entity entity : loadedUnits) {
                    entity.setOwner(client.getLocalPlayer());
                    client.sendAddEntity(entity);
                }
            } catch (IOException excep) {
                excep.printStackTrace(System.err);
                doAlertDialog(Messages.getString("ClientGUI.errorLoadingFile"), excep.getMessage());
            }
        }
    }

    /**
     * Allow the player to save a list of entities to a MegaMek Unit List file.
     * A "Save As" dialog will be displayed that allows the user to select the
     * file's name and directory. The player can later load this file to quickly
     * select the units for a new game. The file will record damage sustained,
     * non-standard munitions selected, and ammunition expended during the
     * course of the current engagement.
     *
     * @param unitList
     *            - the <code>Vector</code> of <code>Entity</code>s to be saved
     *            to a file. If this value is <code>null</code> or empty, the
     *            "Save As" dialog will not be displayed.
     */
    protected void saveListFile(ArrayList<Entity> unitList) {
        saveListFile(unitList, client.getLocalPlayer().getName());
    }

    protected void saveListFile(ArrayList<Entity> unitList, String filename) {
        if ((unitList == null) || unitList.isEmpty()) {
            return;
        }
        if (dlgSaveList == null) {
            dlgSaveList = new JFileChooser(".");
            dlgSaveList.setLocation(frame.getLocation().x + 150, frame.getLocation().y + 100);
            dlgSaveList.setDialogTitle(Messages.getString("ClientGUI.saveUnitListFileDialog.title"));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Mul Files", "mul");
            dlgSaveList.setFileFilter(filter);
        }
        dlgSaveList.setSelectedFile(new File(filename + ".mul"));
        int returnVal = dlgSaveList.showSaveDialog(frame);
        if ((returnVal != JFileChooser.APPROVE_OPTION) || (dlgSaveList.getSelectedFile() == null)) {
            return;
        }
        File unitFile = dlgSaveList.getSelectedFile();
        if (unitFile != null) {
            if (!(unitFile.getName().toLowerCase().endsWith(".mul") || unitFile.getName().toLowerCase().endsWith(".xml"))) {
                try {
                    unitFile = new File(unitFile.getCanonicalPath() + ".mul");
                } catch (IOException ie) {
                    return;
                }
            }
            try {
                EntityListFile.saveTo(unitFile, unitList);
            } catch (IOException excep) {
                excep.printStackTrace(System.err);
                doAlertDialog(Messages.getString("ClientGUI.errorSavingFile"), excep.getMessage());
            }
        }
    }

    public void windowActivated(WindowEvent windowEvent) {
    }

    public void windowClosed(WindowEvent windowEvent) {
    }

    public void windowClosing(WindowEvent windowEvent) {
        if (windowEvent.getWindow().equals(minimapW)) {
            setMapVisible(false);
        } else if (windowEvent.getWindow().equals(mechW)) {
            setDisplayVisible(false);
        }
    }

    public void windowDeactivated(WindowEvent windowEvent) {
    }

    public void windowDeiconified(WindowEvent windowEvent) {
    }

    public void windowIconified(WindowEvent windowEvent) {
    }

    public void windowOpened(WindowEvent windowEvent) {
    }

    /**
     * @return the frame this client is displayed in
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     *  Shows a dialog where the player can select the entity types
     *  used in the LOS tool.
     */
    private void showLOSSettingDialog() {
        GUIPreferences gp = GUIPreferences.getInstance();
        LOSDialog ld = new LOSDialog(frame, gp.getMechInFirst(), gp.getMechInSecond());
        ld.setVisible(true);
        gp.setMechInFirst(ld.getMechInFirst());
        gp.setMechInSecond(ld.getMechInSecond());
    }

    /**
     *  Loads a preview image of the unit into the BufferedPanel.
     * @param bp
     * @param entity
     */
    public void loadPreviewImage(JLabel bp, Entity entity) {
        Player player = client.game.getPlayer(entity.getOwnerId());
        loadPreviewImage(bp, entity, player);
    }

    public void loadPreviewImage(JLabel bp, Entity entity, Player player) {
        Image camo = bv.getTilesetManager().getPlayerCamo(player);
        int tint = PlayerColors.getColorRGB(player.getColorIndex());
        bp.setIcon(new ImageIcon(bv.getTilesetManager().loadPreviewImage(entity, camo, tint, bp)));
    }

    /**
     * Make a "bing" sound.
     */
    void bing() {
        if (!GUIPreferences.getInstance().getSoundMute() && (bingClip != null)) {
            bingClip.play();
        }
    }

    private GameListener gameListener = new GameListenerAdapter() {

        @Override
        public void gamePlayerDisconnected(GamePlayerDisconnectedEvent e) {
            JOptionPane.showMessageDialog(frame, Messages.getString("ClientGUI.Disconnected.message"), Messages.getString("ClientGUI.Disconnected.title"), JOptionPane.ERROR_MESSAGE);
            frame.setVisible(false);
            die();
        }

        @Override
        public void gamePlayerChat(GamePlayerChatEvent e) {
            bing();
        }

        @Override
        public void gamePhaseChange(GamePhaseChangeEvent e) {
            if (bv.getLocalPlayer() == null) {
                bv.setLocalPlayer(getClient().getLocalPlayer());
            }
            switchPanel(getClient().game.getPhase());
            menuBar.setPhase(getClient().game.getPhase());
            validate();
            cb.moveToEnd();
        }

        @Override
        public void gamePlayerConnected(GamePlayerConnectedEvent e) {
            System.err.println("gamePlayerConnected");
            System.err.flush();
            if (curPanel instanceof ReportDisplay) {
                ((ReportDisplay) curPanel).resetReadyButton();
                System.err.println("resetReadyButton");
                System.err.flush();
            }
        }

        @Override
        public void gameReport(GameReportEvent e) {
            if ((e.getReport() == null) && (curPanel instanceof ReportDisplay)) {
                ((ReportDisplay) curPanel).appendReportTab(getClient().phaseReport);
                ((ReportDisplay) curPanel).resetReadyButton();
                if (getClient().game.hasTacticalGenius(getClient().getLocalPlayer())) {
                    if (!((ReportDisplay) curPanel).hasRerolled()) {
                        ((ReportDisplay) curPanel).resetRerollButton();
                    }
                }
            } else {
                if (!(getClient() instanceof TestBot)) {
                    doAlertDialog("Movement Report", e.getReport());
                }
            }
        }

        @Override
        public void gameEnd(GameEndEvent e) {
            bv.clearMovementData();
            for (Client client2 : getBots().values()) {
                client2.die();
            }
            getBots().clear();
            ArrayList<Entity> living = getClient().game.getPlayerEntities(getClient().getLocalPlayer(), false);
            for (Enumeration<Entity> iter = getClient().game.getRetreatedEntities(); iter.hasMoreElements(); ) {
                living.add(iter.nextElement());
            }
            if (!living.isEmpty() && doYesNoDialog(Messages.getString("ClientGUI.SaveUnitsDialog.title"), Messages.getString("ClientGUI.SaveUnitsDialog.message"))) {
                saveListFile(living);
            }
            ArrayList<Entity> destroyed = new ArrayList<Entity>();
            Enumeration<Entity> graveyard = getClient().game.getGraveyardEntities();
            while (graveyard.hasMoreElements()) {
                Entity entity = graveyard.nextElement();
                if (entity.isSalvage()) {
                    destroyed.add(entity);
                }
            }
            if (destroyed.size() > 0) {
                String sLogDir = PreferenceManager.getClientPreferences().getLogDirectory();
                File logDir = new File(sLogDir);
                if (!logDir.exists()) {
                    logDir.mkdir();
                }
                String fileName = "salvage.mul";
                if (PreferenceManager.getClientPreferences().stampFilenames()) {
                    fileName = StringUtil.addDateTimeStamp(fileName);
                }
                File unitFile = new File(sLogDir + File.separator + fileName);
                try {
                    EntityListFile.saveTo(unitFile, destroyed);
                } catch (IOException excep) {
                    excep.printStackTrace(System.err);
                    doAlertDialog(Messages.getString("ClientGUI.errorSavingFile"), excep.getMessage());
                }
            }
        }

        @Override
        public void gameSettingsChange(GameSettingsChangeEvent e) {
            if ((gameOptionsDialog != null) && gameOptionsDialog.isVisible()) {
                gameOptionsDialog.update(getClient().game.getOptions());
            }
            if (curPanel instanceof ChatLounge) {
                ChatLounge cl = (ChatLounge) curPanel;
                cl.updateMapSettings(getClient().getMapSettings());
            }
        }

        @Override
        public void gameMapQuery(GameMapQueryEvent e) {
        }
    };

    public Client getClient() {
        return client;
    }

    public Map<String, Client> getBots() {
        return client.bots;
    }

    /**
     * @return Returns the selectedEntityNum.
     */
    public int getSelectedEntityNum() {
        return selectedEntityNum;
    }

    /**
     * @param selectedEntityNum
     *            The selectedEntityNum to set.
     */
    public void setSelectedEntityNum(int selectedEntityNum) {
        this.selectedEntityNum = selectedEntityNum;
    }

    public RandomArmyDialog getRandomArmyDialog() {
        return randomArmyDialog;
    }

    public RandomSkillDialog getRandomSkillDialog() {
        return randomSkillDialog;
    }

    public RandomNameDialog getRandomNameDialog() {
        return randomNameDialog;
    }

    /**
     * Checks to see if there is already a path and name stored; if not, calls
     * "save as"; otherwise, saves the board to the specified file.
     */
    private void boardSave() {
        if (curfileBoard == null) {
            boardSaveAs();
            return;
        }
        try {
            OutputStream os = new FileOutputStream(curfileBoard);
            client.game.getBoard().save(os);
            os.close();
        } catch (IOException ex) {
            System.err.println("error opening file to save!");
            System.err.println(ex);
        }
    }

    /**
     * Saves the board in PNG image format.
     */
    private void boardSaveImage() {
        if (curfileBoardImage == null) {
            boardSaveAsImage();
            return;
        }
        JDialog waitD = new JDialog(frame, Messages.getString("BoardEditor.waitDialog.title"));
        waitD.add(new JLabel(Messages.getString("BoardEditor.waitDialog.message")));
        waitD.setSize(250, 130);
        waitD.setLocation((frame.getSize().width / 2) - (waitD.getSize().width / 2), (frame.getSize().height / 2) - (waitD.getSize().height / 2));
        waitD.setVisible(true);
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        waitD.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        int filter = 0;
        int compressionLevel = 9;
        PngEncoder png = new PngEncoder(bv.getEntireBoardImage(), PngEncoder.NO_ALPHA, filter, compressionLevel);
        try {
            FileOutputStream outfile = new FileOutputStream(curfileBoardImage);
            byte[] pngbytes;
            pngbytes = png.pngEncode();
            if (pngbytes == null) {
                System.out.println("Failed to save board as image:Null image");
            } else {
                outfile.write(pngbytes);
            }
            outfile.flush();
            outfile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        waitD.setVisible(false);
        frame.setCursor(Cursor.getDefaultCursor());
    }

    /**
     * Opens a file dialog box to select a file to save as; saves the board to
     * the file.
     */
    private void boardSaveAs() {
        JFileChooser fc = new JFileChooser("data" + File.separator + "boards");
        fc.setLocation(frame.getLocation().x + 150, frame.getLocation().y + 100);
        fc.setDialogTitle(Messages.getString("BoardEditor.saveBoardAs"));
        fc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File dir) {
                return (null != dir.getName()) && dir.getName().endsWith(".board");
            }

            @Override
            public String getDescription() {
                return ".board";
            }
        });
        int returnVal = fc.showSaveDialog(frame);
        if ((returnVal != JFileChooser.APPROVE_OPTION) || (fc.getSelectedFile() == null)) {
            return;
        }
        curfileBoard = fc.getSelectedFile();
        if (!curfileBoard.getName().toLowerCase().endsWith(".board")) {
            try {
                curfileBoard = new File(curfileBoard.getCanonicalPath() + ".board");
            } catch (IOException ie) {
                return;
            }
        }
        boardSave();
    }

    /**
     * Opens a file dialog box to select a file to save as; saves the board to
     * the file as an image. Useful for printing boards.
     */
    private void boardSaveAsImage() {
        JFileChooser fc = new JFileChooser(".");
        fc.setLocation(frame.getLocation().x + 150, frame.getLocation().y + 100);
        fc.setDialogTitle(Messages.getString("BoardEditor.saveAsImage"));
        fc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File dir) {
                return (null != dir.getName()) && dir.getName().endsWith(".png");
            }

            @Override
            public String getDescription() {
                return ".png";
            }
        });
        int returnVal = fc.showSaveDialog(frame);
        if ((returnVal != JFileChooser.APPROVE_OPTION) || (fc.getSelectedFile() == null)) {
            return;
        }
        curfileBoardImage = fc.getSelectedFile();
        if (!curfileBoardImage.getName().toLowerCase().endsWith(".png")) {
            try {
                curfileBoardImage = new File(curfileBoardImage.getCanonicalPath() + ".png");
            } catch (IOException ie) {
                return;
            }
        }
        boardSaveImage();
    }

    public void hexMoused(BoardViewEvent b) {
        if (b.getType() == BoardViewEvent.BOARD_HEX_POPUP) {
            showBoardPopup(b.getCoords());
        }
    }

    public void hexCursor(BoardViewEvent b) {
    }

    public void boardHexHighlighted(BoardViewEvent b) {
    }

    public void hexSelected(BoardViewEvent b) {
    }

    public void firstLOSHex(BoardViewEvent b) {
    }

    public void secondLOSHex(BoardViewEvent b, Coords c) {
    }

    public void finishedMovingUnits(BoardViewEvent b) {
    }

    public void unitSelected(BoardViewEvent b) {
    }
}

package applet;

import static sidplay.ConsolePlayer.playerExit;
import static sidplay.ConsolePlayer.playerFast;
import static sidplay.ConsolePlayer.playerRestart;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.JApplet;
import javax.swing.JOptionPane;
import libsidplay.Player;
import libsidplay.components.c1541.C1541;
import libsidplay.components.c1541.DiskImage;
import libsidplay.components.c1541.ExtendImagePolicy;
import libsidplay.components.c1541.IExtendImageListener;
import libsidutils.PRG2TAP;
import libsidutils.zip.ZipEntryFileProxy;
import org.swixml.SwingEngine;
import sidplay.ConsolePlayer;
import sidplay.ini.IniConfig;
import sidplay.ini.IniSidplay2Section;
import applet.events.IGotoURL;
import applet.events.IInsertMedia;
import applet.events.IPlayTune;
import applet.events.IReplayTune;
import applet.events.IStopTune;
import applet.events.ITuneStateChanged;
import applet.events.Reset;
import applet.events.UIEvent;
import applet.events.UIEventFactory;
import applet.events.UIEventListener;

/**
 * @author Ken H�ndel
 * @author Joakim Eriksson
 * 
 *         SID Player main class
 */
public class JSIDPlay2 extends JApplet implements UIEventListener {

    /**
	 * Console player
	 */
    protected final ConsolePlayer cp;

    /**
	 * Console player thread.
	 */
    protected Thread fPlayerThread;

    /**
	 * Event management of UI events.
	 */
    protected UIEventFactory uiEvents = UIEventFactory.getInstance();

    /**
	 * Main window user interface.
	 */
    protected JSIDPlay2UI ui;

    /**
	 * Applet constructor.
	 */
    public JSIDPlay2() {
        this(new String[0]);
    }

    /**
	 * Application constructor.
	 */
    public JSIDPlay2(final String[] args) {
        uiEvents.addListener(this);
        cp = new ConsolePlayer();
        if (args.length != 0) {
            cp.args(args);
        }
    }

    /**
	 * Player runnable to play music in the background.
	 */
    private final transient Runnable playerRunnable = new Runnable() {

        public void run() {
            while (true) {
                try {
                    if (!cp.open()) {
                        return;
                    }
                    ui.setTune(getPlayer().getTune());
                    while (true) {
                        if (cp.getState() == ConsolePlayer.playerPaused) {
                            Thread.sleep(250);
                        }
                        if (!cp.play()) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                } finally {
                    cp.close();
                }
                getConfig().write();
                if ((cp.getState() & ~playerFast) == playerRestart) {
                    continue;
                }
                break;
            }
            uiEvents.fireEvent(ITuneStateChanged.class, new ITuneStateChanged() {

                public File getTune() {
                    return getPlayer().getTune().getInfo().file;
                }

                public boolean naturalFinished() {
                    return cp.getState() == playerExit;
                }
            });
        }
    };

    /**
	 * The user interface is set up, here.
	 * 
	 * @see java.applet.Applet#init()
	 */
    @Override
    public void init() {
        initializeTmpDir();
        createUI();
    }

    /**
	 * Start the emulation.
	 * 
	 * @see java.applet.Applet#start()
	 */
    @Override
    public void start() {
        startC64();
    }

    /**
	 * Stop emulation.
	 * 
	 * @see java.applet.Applet#stop()
	 */
    @Override
    public void stop() {
        stopC64();
    }

    /**
	 * Free resources.
	 * 
	 * @see java.applet.Applet#destroy()
	 */
    @Override
    public void destroy() {
        for (final C1541 floppy : getPlayer().getFloppies()) {
            try {
                floppy.getDiskController().ejectDisk();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            getPlayer().getDatasette().ejectTape();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
	 * Create temp directory, if not exists (default is user home dir).
	 * 
	 * Note: system property jsidplay2.tmpdir is set accordingly.
	 */
    private void initializeTmpDir() {
        String tmpDirPath = getConfig().sidplay2().getTmpDir();
        File tmpDir = new File(tmpDirPath);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        System.setProperty("jsidplay2.tmpdir", tmpDirPath);
    }

    /**
	 * Create the user interface.
	 */
    private void createUI() {
        ui = new JSIDPlay2UI(this, cp);
    }

    /**
	 * Start emulation (start player thread).
	 */
    private void startC64() {
        fPlayerThread = new Thread(playerRunnable);
        fPlayerThread.setPriority(Thread.MAX_PRIORITY);
        fPlayerThread.start();
    }

    /**
	 * Stop emulation (stop player thread).
	 */
    private void stopC64() {
        try {
            while (fPlayerThread.isAlive()) {
                cp.quit();
                fPlayerThread.join(1000);
                fPlayerThread.interrupt();
            }
        } catch (InterruptedException e) {
        }
    }

    /**
	 * Play tune.
	 * 
	 * @param file
	 *            file to play the tune (null means just reset C64)
	 */
    protected void playTune(final File file) {
        stopC64();
        cp.loadTune(file);
        startC64();
    }

    /**
	 * Ask the user to insert a tape.
	 */
    private void insertTape(final File selectedTape, final File autostartFile, final Component component) throws IOException {
        if (!selectedTape.getName().toLowerCase().endsWith(".tap")) {
            final File convertedTape = new File(System.getProperty("jsidplay2.tmpdir"), selectedTape.getName() + ".tap");
            convertedTape.deleteOnExit();
            String[] args = new String[] { selectedTape.getAbsolutePath(), convertedTape.getAbsolutePath() };
            PRG2TAP.main(args);
            getPlayer().getDatasette().insertTape(convertedTape);
        } else {
            getPlayer().getDatasette().insertTape(selectedTape);
        }
        if (autostartFile != null) {
            uiEvents.fireEvent(IPlayTune.class, new IPlayTune() {

                @Override
                public boolean switchToVideoTab() {
                    return true;
                }

                @Override
                public File getFile() {
                    return autostartFile;
                }

                @Override
                public Component getComponent() {
                    return component;
                }
            });
        }
    }

    /**
	 * Ask the user to insert a disk.
	 */
    private void insertDisk(final File selectedDisk, final File autostartFile, final Component component) throws IOException {
        getPlayer().enableFloppyDiskDrives(true);
        getConfig().c1541().setDriveOn(true);
        DiskImage disk = getPlayer().getFloppies()[0].getDiskController().insertDisk(selectedDisk);
        disk.setExtendImagePolicy(new IExtendImageListener() {

            public boolean isAllowed() {
                if (getConfig().c1541().getExtendImagePolicy() == ExtendImagePolicy.EXTEND_ASK) {
                    return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(JSIDPlay2.this, ui.getSwix().getLocalizer().getString("EXTEND_DISK_IMAGE_TO_40_TRACKS"), ui.getSwix().getLocalizer().getString("EXTEND_DISK_IMAGE"), JOptionPane.YES_NO_OPTION);
                } else if (getConfig().c1541().getExtendImagePolicy() == ExtendImagePolicy.EXTEND_ACCESS) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        if (autostartFile != null) {
            uiEvents.fireEvent(IPlayTune.class, new IPlayTune() {

                @Override
                public boolean switchToVideoTab() {
                    return true;
                }

                @Override
                public File getFile() {
                    return autostartFile;
                }

                @Override
                public Component getComponent() {
                    return component;
                }
            });
        }
    }

    /**
	 * Ask the user to insert a cartridge.
	 * 
	 * @throws IOException
	 *             cannot read cartridge file
	 */
    private void insertCartridge(final File selectedFile) throws IOException {
        getPlayer().getC64().insertCartridge(selectedFile);
        uiEvents.fireEvent(Reset.class, new Reset() {

            @Override
            public boolean switchToVideoTab() {
                return false;
            }

            @Override
            public String getCommand() {
                return null;
            }

            @Override
            public Component getComponent() {
                return JSIDPlay2.this;
            }
        });
    }

    /**
	 * Main method. Create an application frame and start emulation.
	 * 
	 * @param args
	 *            command line arguments
	 */
    public static void main(final String[] args) {
        final JSIDPlay2 sidplayApplet = new JSIDPlay2(args);
        sidplayApplet.init();
        try {
            SwingEngine swix = new SwingEngine(sidplayApplet);
            final Window window = (Window) swix.render(JSIDPlay2.class.getResource("JSIDPlay2.xml"));
            window.add(sidplayApplet);
            final IniSidplay2Section section = sidplayApplet.getConfig().sidplay2();
            if (section.getFrameX() != -1 && section.getFrameY() != -1) {
                window.setLocation(section.getFrameX(), section.getFrameY());
                window.setSize(section.getFrameWidth(), section.getFrameHeight());
            } else {
                window.setSize(1024, 768);
            }
            window.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(final WindowEvent e) {
                    sidplayApplet.stop();
                    sidplayApplet.destroy();
                }
            });
            window.addComponentListener(new ComponentAdapter() {

                @Override
                public void componentResized(final ComponentEvent e) {
                    final Dimension size = window.getSize();
                    sidplayApplet.getConfig().sidplay2().setFrameWidth(size.width);
                    sidplayApplet.getConfig().sidplay2().setFrameHeight(size.height);
                }

                @Override
                public void componentMoved(final ComponentEvent e) {
                    final Point loc = window.getLocation();
                    sidplayApplet.getConfig().sidplay2().setFrameX(loc.x);
                    sidplayApplet.getConfig().sidplay2().setFrameY(loc.y);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        sidplayApplet.start();
    }

    /**
	 * Capture events to do certain tasks. Play tune, open browser, etc.
	 * 
	 * @param evt
	 *            property change event
	 */
    public void notify(final UIEvent evt) {
        if (evt.isOfType(IReplayTune.class)) {
            if (getPlayer().getTune() != null) {
                playTune(getPlayer().getTune().getInfo().file);
            } else {
                playTune(null);
            }
        } else if (evt.isOfType(IPlayTune.class)) {
            IPlayTune ifObj = (IPlayTune) evt.getUIEventImpl();
            if (evt.isOfType(Reset.class)) {
                getPlayer().setCommand(((Reset) evt.getUIEventImpl()).getCommand());
            }
            playTune(ifObj.getFile());
        } else if (evt.isOfType(IGotoURL.class)) {
            IGotoURL ifObj = (IGotoURL) evt.getUIEventImpl();
            if (isActive()) {
                getAppletContext().showDocument(ifObj.getCollectionURL(), "_blank");
            } else {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        try {
                            desktop.browse(ifObj.getCollectionURL().toURI());
                        } catch (final IOException ioe) {
                            ioe.printStackTrace();
                        } catch (final URISyntaxException urie) {
                            urie.printStackTrace();
                        }
                    }
                }
            }
        } else if (evt.isOfType(IStopTune.class)) {
            stopC64();
        } else if (evt.isOfType(IInsertMedia.class)) {
            IInsertMedia ifObj = (IInsertMedia) evt.getUIEventImpl();
            File mediaFile = ifObj.getSelectedMedia();
            try {
                if (mediaFile instanceof ZipEntryFileProxy) {
                    mediaFile = ZipEntryFileProxy.extractFromZip((ZipEntryFileProxy) mediaFile);
                }
                if (mediaFile.getName().endsWith(".gz")) {
                    mediaFile = ZipEntryFileProxy.extractFromGZ(mediaFile);
                }
                switch(ifObj.getMediaType()) {
                    case TAPE:
                        insertTape(mediaFile, ifObj.getAutostartFile(), ifObj.getComponent());
                        break;
                    case DISK:
                        insertDisk(mediaFile, ifObj.getAutostartFile(), ifObj.getComponent());
                        break;
                    case CART:
                        insertCartridge(mediaFile);
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                System.err.println(String.format("Cannot attach file '%s'.", mediaFile.getAbsolutePath()));
                return;
            }
        }
    }

    /**
	 * Get saved INI file configuration.
	 * 
	 * @return INI file configuration
	 */
    public IniConfig getConfig() {
        return cp.getConfig();
    }

    /**
	 * Get player (C64 and peripherals).
	 * 
	 * @return the player
	 */
    public Player getPlayer() {
        return cp.getPlayer();
    }
}

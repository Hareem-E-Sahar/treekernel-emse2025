package net.sourceforge.jepesi.controller;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.tree.MutableTreeNode;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import net.sourceforge.jepesi.addon.AddonInterface;
import net.sourceforge.jepesi.gui.AboutWindow;
import net.sourceforge.jepesi.gui.EditAddHostWindow;
import net.sourceforge.jepesi.gui.MainWindow;
import net.sourceforge.jepesi.gui.SplashScreen;
import net.sourceforge.jepesi.gui.StatusPanel;
import net.sourceforge.jepesi.gui.StatusPanelInterface;
import net.sourceforge.jepesi.gui.hosttree.HostItemPopupMenuItem;
import net.sourceforge.jepesi.gui.tab.LogPanel;
import net.sourceforge.jepesi.gui.tab.LogPanelInterface;
import net.sourceforge.jepesi.jsch.JschSession;
import net.sourceforge.jepesi.jsch.SshSession;
import net.sourceforge.jepesi.model.Cluster;
import net.sourceforge.jepesi.model.Clusters;
import net.sourceforge.jepesi.model.Config;
import net.sourceforge.jepesi.model.ConfigInterface;
import net.sourceforge.jepesi.model.Host;
import net.sourceforge.jepesi.model.Hosts;
import net.sourceforge.jepesi.model.Lang;
import net.sourceforge.jepesi.model.Language;
import net.sourceforge.jepesi.model.Lock;
import net.sourceforge.jepesi.model.Os;
import net.sourceforge.jepesi.model.Tools;
import net.sourceforge.jepesi.plugin.OsPluginInterface;
import net.sourceforge.jepesi.plugin.TimeOutCaller;
import net.sourceforge.jepesi.plugin.ToolPluginInterface;

class JepesiControl implements JepesiListener, JepesiInterface, JepesiStarter {

    private static final int LUGIN_MAX_DEPENDENCY_RESOLVES = 5;

    private static final int PLUGIN_START_TIMEOUT = 5000;

    private static final int PLUGIN_PER_DEPENDENCY_RESOLVE_TIMEOUT = 3000;

    @SuppressWarnings("unused")
    private SplashScreen splashScreen;

    private ConnectionControl connectionControl;

    private CopyTrackerControl copyTrackerControl;

    private PluginControl pluginControl;

    private MainWindow mainWindow;

    private AboutWindow aboutWindow;

    private Hosts hosts;

    private Clusters clusters;

    private LogPanel logger = null;

    private Map<Integer, AddonInterface> nodeToStartedAddon = new HashMap<Integer, AddonInterface>();

    private Map<Integer, AddonInterface> nodeToLoadedAddon = new HashMap<Integer, AddonInterface>();

    private boolean copyTrackerLoaded = false;

    private boolean testRun;

    private ConfigInterface config;

    private StatusPanelInterface status = new StatusPanel();

    private boolean doRestart = false;

    public JepesiControl() {
        this.setTestRun(false);
    }

    public void restart() {
        for (Host host : hosts) {
            if (host.getStatus() == JschSession.CONNECTED) {
                this.connectionControl.disconnect(host);
            }
        }
        this.mainWindow.dispose();
        Lang.reload();
        doRestart = true;
        synchronized (this) {
            this.notifyAll();
        }
    }

    public boolean doRestart() {
        return doRestart;
    }

    public void startup() {
        String resPrePath = "";
        if (System.getProperty("os.name").startsWith("Mac") && !testRun) {
            resPrePath = "Jepesi.app/";
        }
        this.splashScreen = new SplashScreen(this);
        try {
            this.config = new Config(resPrePath + "config.xml");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.hosts = new Hosts(resPrePath + "hosts.xml");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.hosts.save();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            this.clusters = new Clusters(this.hosts, resPrePath + "clusters.xml");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.clusters.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.connectionControl = new ConnectionControl(this, this.hosts, this.clusters);
        this.copyTrackerControl = new CopyTrackerControl(this);
        this.logger = new LogPanel();
        this.mainWindow = new MainWindow(this);
        updateHosts();
        this.pluginControl = new PluginControl(this, resPrePath);
        pluginControl.loadOsPlugins();
        pluginControl.loadToolPlugins();
        this.setStatus("");
        if (hosts.size() == 0) {
            this.showInfo(Lang.get("NOHOSTSMSG"), Lang.get("NOHOSTSTITLE"));
            new EditAddHostWindow(this);
        }
    }

    public LogPanelInterface getLogger() {
        return logger;
    }

    public void log(String text) {
        if (logger == null) {
            System.out.println(text);
        } else {
            logger.log(text);
            setStatus(text);
        }
    }

    /**
	 * quit jepesi
	 */
    private void quit() {
        log("Shutting down...");
        System.exit(0);
    }

    private void setLocale(Locale newlocale) {
        int reply = JOptionPane.showConfirmDialog(null, Lang.get("REALYWANTTOCHANGELANANDRESTARTMSG"), Lang.get("REALYWANTTOCHANGELANANDRESTARTTITLE"), JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
            Locale.setDefault(newlocale);
            this.restart();
        }
    }

    public synchronized void processModelChange() {
        try {
            this.hosts.save();
            this.clusters.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateHosts();
    }

    public void onHostConected(Host host) {
        log("connected to " + host.getName() + " (" + host.getHost() + ")");
        Date now = new Date();
        Long rescanInterval = new Long(2592000);
        if (config.hasVar("rescanInterval")) {
            rescanInterval = (Long) config.getVar("rescanInterval");
        }
        if (host.getLastCheck() < now.getTime() - rescanInterval) {
            log("checking feature scope on " + host.getName());
            runOsPlugins(host);
            runToolPlugins(host);
            host.setLastCheck(now.getTime());
            processModelChange();
        }
        pluginControl.loadAddons(host);
        log(host.getName() + " is ready!");
    }

    private void runOsPlugins(Host host) {
        List<OsPluginInterface> osPlugins = pluginControl.getOsPlugins();
        SshSession session = this.connectionControl.getSessionByHost(host);
        Lock lock = new Lock();
        Os os = host.getOs();
        for (OsPluginInterface osPlugin : osPlugins) {
            osPlugin.check(lock, os, session);
        }
        new Thread(new TimeOutCaller(lock, 5000)).start();
        try {
            lock.waitUntilReleaseOrTimeout();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (OsPluginInterface osPlugin : osPlugins) {
            osPlugin.removeListeners();
        }
    }

    private void runToolPlugins(Host host) {
        List<ToolPluginInterface> toolPlugins = pluginControl.getToolPlugins();
        SshSession session = this.connectionControl.getSessionByHost(host);
        Lock lock = new Lock();
        Os os = host.getOs();
        Tools tools = host.getTools();
        tools.clear();
        List<ToolPluginInterface> startedTools = new ArrayList<ToolPluginInterface>();
        int run = 0;
        boolean changed = true;
        TimeOutCaller timeOutCaller = new TimeOutCaller(lock, PLUGIN_START_TIMEOUT);
        new Thread(timeOutCaller).start();
        while (run <= LUGIN_MAX_DEPENDENCY_RESOLVES && toolPlugins.size() > startedTools.size() && changed) {
            System.out.println("Plugins run " + run);
            Set<String> enabledTools = tools.getEnabled();
            changed = false;
            for (ToolPluginInterface toolPlugin : toolPlugins) {
                if (!startedTools.contains(toolPlugin)) {
                    if (toolPlugin.isEnabled(os)) {
                        if (toolPlugin.dependsOn() == null || toolPlugin.dependsOn().size() == 0 || enabledTools.containsAll(toolPlugin.dependsOn())) {
                            timeOutCaller.addTimeout(PLUGIN_PER_DEPENDENCY_RESOLVE_TIMEOUT);
                            toolPlugin.check(lock, tools, session);
                            startedTools.add(toolPlugin);
                            changed = true;
                            System.out.println("Starting " + toolPlugin.getClass().getName());
                        }
                    }
                }
            }
            try {
                lock.waitUntilReleaseOrTimeout();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            run++;
        }
        for (ToolPluginInterface toolPlugin : startedTools) {
            toolPlugin.removeListeners();
        }
    }

    public void updateHosts() {
        this.mainWindow.update(this.hosts);
    }

    public void leftClickOnHostTree(Host host) {
        if (host.getStatus() == JschSession.NOTABLETOCONNECT || host.getStatus() == JschSession.NOTTRYEDTOCONNECT) {
            log("connecting to host " + host.getName());
            this.connectionControl.connect(host);
        }
    }

    private void showAddon(int nodeId) {
        AddonInterface addon = nodeToStartedAddon.get(nodeId);
        showTap(addon.getPanel());
        addon.focus();
    }

    private void showTap(JPanel panel) {
        this.mainWindow.getTap().setSelectedComponent(panel);
    }

    public void openCopyTracker() {
        if (!copyTrackerLoaded) {
            this.mainWindow.getTap().addTab(copyTrackerControl.getTitle(), copyTrackerControl.getPanel());
            copyTrackerLoaded = true;
        } else {
            this.mainWindow.getTap().setSelectedComponent(copyTrackerControl.getPanel());
        }
    }

    public void openAddon(Host host, MutableTreeNode node) {
        log("open addon " + node.toString() + " on " + host.getName());
        int nodeId = System.identityHashCode(node);
        if (nodeToStartedAddon.containsKey(nodeId)) {
            showAddon(nodeId);
        } else {
            startAddon(host, node);
        }
    }

    public void addLoadedAddon(MutableTreeNode node, AddonInterface addon) {
        this.nodeToLoadedAddon.put(System.identityHashCode(node), addon);
    }

    public void addHost(Host host) {
        this.hosts.addHost(host);
    }

    public void removeHost(Host host) {
        this.closeAllTabs(host);
        if (host.getStatus() == JschSession.CONNECTED) {
            this.connectionControl.disconnect(host);
        }
        this.hosts.remove(host);
        this.processModelChange();
        this.updateHosts();
    }

    public void startAddon(Host host, MutableTreeNode node) {
        List<AddonInterface> addons = this.getPluginControl().getAddons(host);
        for (AddonInterface addon : addons) {
            if (addon.getNode().equals(node)) {
                addon.start(host);
                this.mainWindow.getTap().addTab(addon.getTitle(), addon.getPanel());
                nodeToStartedAddon.put(System.identityHashCode(node), addon);
            }
        }
    }

    public void showError(String text, String title) {
        JOptionPane.showMessageDialog(this.mainWindow, text, title, JOptionPane.ERROR_MESSAGE);
    }

    public void showWarning(String text, String title) {
        JOptionPane.showMessageDialog(this.mainWindow, text, title, JOptionPane.WARNING_MESSAGE);
    }

    public void showInfo(String text, String title) {
        JOptionPane.showMessageDialog(this.mainWindow, text, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public void closeAllTabs(Host host) {
        List<AddonInterface> addons = this.pluginControl.getAddons(host);
        for (AddonInterface addon : addons) {
            int nodeId = System.identityHashCode(addon.getNode());
            if (nodeToStartedAddon.containsKey(nodeId)) {
                this.mainWindow.getTap().remove(addon.getPanel());
                addon.unload();
                nodeToStartedAddon.remove(nodeId);
            }
        }
    }

    public void browseTo(String url) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeTab(AddonInterface addon) {
        int nodeId = System.identityHashCode(addon.getNode());
        if (nodeToStartedAddon.containsKey(nodeId)) {
            this.mainWindow.getTap().remove(addon.getPanel());
            addon.unload();
            nodeToStartedAddon.remove(nodeId);
        }
        showTap(this.logger);
    }

    public ImageIcon getIcon(MutableTreeNode node) {
        int nodeId = System.identityHashCode(node);
        if (nodeToLoadedAddon.containsKey(nodeId)) {
            AddonInterface addon = nodeToLoadedAddon.get(nodeId);
            return pluginControl.getIcon(addon.getClass().getName());
        }
        return null;
    }

    public BufferedImage getAddonImage(String name) {
        return pluginControl.getImage(name);
    }

    public void leftClickOnHostTree(Cluster cluster) {
        log("click on cluster " + cluster.getName());
    }

    public void windowActivated(WindowEvent arg0) {
    }

    public void windowClosed(WindowEvent arg0) {
    }

    public void windowClosing(WindowEvent arg0) {
        quit();
    }

    public void windowDeactivated(WindowEvent arg0) {
    }

    public void windowDeiconified(WindowEvent arg0) {
    }

    public void windowIconified(WindowEvent arg0) {
    }

    public void windowOpened(WindowEvent arg0) {
    }

    public boolean isTestRun() {
        return this.testRun;
    }

    public void actionPerformed(ActionEvent ev) {
        String cmd = ev.getActionCommand();
        if (cmd.equals("MainMenuCloseClick")) {
            quit();
        } else if (cmd.equals("MainMenuAboutClick")) {
            if (aboutWindow == null) {
                aboutWindow = new AboutWindow();
            } else {
                aboutWindow.setVisible(true);
            }
        } else if (cmd.equals("MainMenuLangClick")) {
            this.setLocale(new Locale("en", "US"));
        } else if (cmd.startsWith("MainMenuHostConnectClick/")) {
            int id = Integer.parseInt(cmd.substring(25));
            connectionControl.connect(hosts.get(id));
            updateHosts();
        } else if (cmd.startsWith("MainMenuHostDisconnectClick/")) {
            int id = Integer.parseInt(cmd.substring(28));
            connectionControl.disconnect(hosts.get(id));
            updateHosts();
        } else if (cmd.startsWith("MainMenuLangSwitch_")) {
            String code = cmd.substring(19);
            String[] scode = code.split("_");
            this.setLocale(new Locale(scode[0], scode[1]));
        } else if (cmd.equals("MainMenuWebsiteClick")) {
            this.browseTo("http://sourceforge.net/projects/jepesi2/");
        } else if (cmd.equals("MainMenuSupportClick")) {
            this.browseTo("http://sourceforge.net/tracker/?func=add&group_id=384406&atid=1598211");
        } else if (cmd.equals("MainMenuAddHostClick") || cmd.equals("HostTreePopupAddClick")) {
            new EditAddHostWindow(this);
        } else if (cmd.equals("HostTreePopupDisconnectClick")) {
            HostItemPopupMenuItem disconnectItem = (HostItemPopupMenuItem) ev.getSource();
            connectionControl.disconnect(disconnectItem.getHost());
            updateHosts();
        } else if (cmd.equals("HostTreePopupEditClick")) {
            HostItemPopupMenuItem popupItem = (HostItemPopupMenuItem) ev.getSource();
            new EditAddHostWindow(this, popupItem.getHost());
        } else if (cmd.equals("HostTreePopupRemoveClick")) {
            HostItemPopupMenuItem popupItem = (HostItemPopupMenuItem) ev.getSource();
            this.removeHost(popupItem.getHost());
        } else if (cmd.equals("HostTreePopupConnectClick")) {
            HostItemPopupMenuItem connectItem = (HostItemPopupMenuItem) ev.getSource();
            connectionControl.connect(connectItem.getHost());
            updateHosts();
        }
    }

    public CopyTrackerControl getCopyTrackerControl() {
        return copyTrackerControl;
    }

    public void setTestRun(boolean testRun) {
        this.testRun = testRun;
        if (testRun) {
            this.setStatus("Running in TestMode! Plugins will be loaded from project");
            System.out.println("Running in TestMode: ");
            System.out.println("\tPlugins will be loaded from project");
            System.out.println("\tAddons are only loading the [AddonName]_en_US.properties lang");
            System.out.println("\tIf you want to test your Plugin JARs disable TestMode");
        }
    }

    public Connection getConnection() {
        return connectionControl;
    }

    /**
	 * @return the pluginControl
	 */
    public PluginControl getPluginControl() {
        return pluginControl;
    }

    /**
	 * @return the config
	 */
    public ConfigInterface getConfig() {
        return config;
    }

    public List<Language> getLanguages() {
        return getConfig().getLanguages();
    }

    public StatusPanelInterface getStatusPanel() {
        return status;
    }

    public void setStatus(String text) {
        this.status.display(text);
    }

    public List<AddonInterface> getAddons(Host host) {
        return this.pluginControl.getAddons(host);
    }

    public void addCopyTrackerListener(SshSession jschSession) {
        jschSession.addCopyListener(this.getCopyTrackerControl());
    }
}

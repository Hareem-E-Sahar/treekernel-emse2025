package ch.kwa.ee;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ProgressMonitor;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.InputSource;
import ch.kwa.ee.browser.EditionSourceTreeModel;
import ch.kwa.ee.browser.FaksimieDescriptionListRenderer;
import ch.kwa.ee.browser.StructureTreeCellRenderer;
import ch.kwa.ee.facsimile.FaksimileViewer;
import ch.kwa.ee.facsimile.event.PointerEvent;
import ch.kwa.ee.facsimile.event.PointerListener;
import ch.kwa.ee.model.Faksimile;
import ch.kwa.ee.model.Transkription;
import ch.kwa.ee.model.browser.FaksimileDescription;
import ch.kwa.ee.model.browser.StructureFolder;
import ch.kwa.ee.model.transkription.TranskriptionModelElement;
import ch.kwa.ee.search.Match;
import ch.kwa.ee.search.SearchWindow;
import ch.kwa.ee.source.IEditionSource;
import ch.kwa.ee.view.SplashScreen;
import ch.kwa.ee.view.config.RenderConfig;
import ch.kwa.ee.view.config.RenderConfigParser;
import ch.unibas.germa.util.IProgressWatcher;

/**
 * EEViewer is the main gui class of the EEViewer. It shows the main window of
 * the application.
 */
public class EEViewer extends JFrame implements TreeSelectionListener, MouseListener, WindowListener, ActionListener, ItemListener, PointerListener {

    private static final long serialVersionUID = 1L;

    JTree structureTree;

    JList folderList;

    JEditorPane infoPanel;

    IEditionSource source;

    JSplitPane verticalSplit;

    JSplitPane horizontalSplit;

    SplashScreen splashScreen;

    private static Logger logger = Logger.getLogger(EEViewer.class);

    String helpUrl;

    HelpSet helpSet = null;

    HelpBroker helpBroker = null;

    public EEViewer() throws HeadlessException {
        super();
        structureTree = new JTree();
        folderList = new JList();
        folderList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        folderList.addMouseListener(this);
        folderList.setVisibleRowCount(0);
        infoPanel = new JEditorPane();
        infoPanel.setEditable(false);
        infoPanel.setContentType("text/html");
        infoPanel.addHyperlinkListener(new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == EventType.ACTIVATED && Desktop.isDesktopSupported()) {
                    try {
                        logger.info("Url: " + e.getDescription());
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                }
            }
        });
        structureTree.addTreeSelectionListener(this);
        verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(folderList), new JScrollPane(infoPanel));
        horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(structureTree), verticalSplit);
        this.getContentPane().add(horizontalSplit);
        JToolBar toolbar = new JToolBar();
        toolbar.add(makeNavigationButton("search.gif", "SEARCH", "Suchfenster öffnen", "Suchen"));
        toolbar.addSeparator();
        toolbar.add(makeNavigationButton("help.gif", "HELP", "Hilfefenster öffnen", "Hilfe"));
        this.getContentPane().add(toolbar, BorderLayout.PAGE_START);
        Dimension screen = getToolkit().getScreenSize();
        this.setBounds(100, 100, screen.width - 200, screen.height - 200);
        horizontalSplit.setDividerLocation(0.8);
        verticalSplit.setDividerLocation(400);
        JMenuBar menubar = createMenu();
        this.setJMenuBar(menubar);
        this.addWindowListener(this);
    }

    private JMenuBar createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu help = createHelpMenu();
        menuBar.add(help);
        return menuBar;
    }

    private JMenu createHelpMenu() {
        JMenu help = new JMenu("Hilfe");
        JMenuItem item = new JMenuItem("Hilfe zu diesem Fenster");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                openHelpWindow("KWA.Fenster.Hauptfenster");
            }
        });
        help.add(item);
        help.addSeparator();
        item = new JMenuItem("Inhalt");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                openHelpWindow("KWA");
            }
        });
        help.add(item);
        item = new JMenuItem("Index");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                openHelpWindow(null);
            }
        });
        help.add(item);
        help.addSeparator();
        item = new JMenuItem("<html><body>&uuml;ber die KWA<sup>e</sup></body></html>");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                openHelpWindow("KWA.Info");
            }
        });
        help.add(item);
        return help;
    }

    public IEditionSource getSource() {
        return source;
    }

    public String getHelpUrl() {
        return helpUrl;
    }

    public void setHelpUrl(String helpUrl) {
        this.helpUrl = helpUrl;
    }

    public HelpBroker getHelpBroker() {
        if (helpBroker == null) {
            HelpSet hs = getHelpSet();
            if (hs != null) helpBroker = helpSet.createHelpBroker();
        }
        return helpBroker;
    }

    private HelpSet getHelpSet() {
        if (helpSet == null) {
            try {
                logger.info(getHelpUrl());
                URL url = HelpSet.findHelpSet(this.getClass().getClassLoader(), getHelpUrl());
                helpSet = new HelpSet(this.getClass().getClassLoader(), url);
            } catch (Exception e) {
                logger.error("Failed to initialize help system!");
                logger.error(e);
                return null;
            }
        }
        return helpSet;
    }

    public void setSource(IEditionSource source) {
        this.source = source;
        structureTree.setModel(new EditionSourceTreeModel(source));
        structureTree.setCellRenderer(new StructureTreeCellRenderer());
        this.setTitle(source.getEditionName());
        setRootInfo();
    }

    private void setRootInfo() {
        try {
            URI uri = source.resolveUri(null, source.getEdition().getRootInfo());
            infoPanel.setPage(uri.toURL());
            infoPanel.setCaretPosition(0);
        } catch (Exception e) {
            logger.error("Invalid root info uri! " + source.getEdition().getRootInfo());
            logger.error(e);
            infoPanel.setText("");
        }
    }

    protected JButton makeNavigationButton(String imageName, String actionCommand, String toolTipText, String altText) {
        String imgLocation = "/icons/" + imageName;
        URL imageURL = FaksimileViewer.class.getResource(imgLocation);
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTipText);
        button.addActionListener(this);
        if (imageURL != null) {
            button.setIcon(new ImageIcon(imageURL, altText));
        } else {
            button.setText(altText);
            System.err.println("Resource not found: " + imgLocation);
        }
        return button;
    }

    /**
	 * @param args
	 * @throws Exception
	 */
    public static void main(String[] args) throws Exception {
        URL imageURL = FaksimileViewer.class.getResource("/icons/Splash.jpg");
        SplashScreen screen = null;
        logger.info(imageURL);
        if (imageURL != null) {
            screen = new SplashScreen();
            logger.info("loading splash image");
            Image splash = Toolkit.getDefaultToolkit().createImage(imageURL);
            MediaTracker mt = new MediaTracker(screen);
            mt.addImage(splash, 1);
            mt.waitForAll();
            screen.setImage(splash);
            screen.setAlwaysOnTop(true);
            screen.setCopyright("Kritische Robert Walser-Ausgabe, 2012");
            screen.setVisible(true);
            screen.toFront();
            logger.info("showing splash");
        }
        ClassPathResource res = new ClassPathResource("beans.xml");
        XmlBeanFactory factory = new XmlBeanFactory(res);
        try {
            logger.info("fetching viewer");
            EEViewer viewer = (EEViewer) factory.getBean("EEViewer");
            logger.info("Setting up viewer");
            if (screen != null) viewer.setSplashScreen(screen);
            viewer.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            viewer.setVisible(true);
        } catch (Exception e) {
            logger.error("An exception occured on startup!", e);
        }
    }

    public void valueChanged(TreeSelectionEvent evt) {
        if (structureTree.getSelectionPath() != null) {
            StructureFolder folder = (StructureFolder) structureTree.getSelectionPath().getLastPathComponent();
            structureTree.expandPath(structureTree.getSelectionPath());
            DefaultListModel listmodel = new DefaultListModel();
            Iterator<FaksimileDescription> it = folder.getContent().iterator();
            while (it.hasNext()) {
                listmodel.addElement(it.next());
            }
            folderList.setCellRenderer(new FaksimieDescriptionListRenderer(getSource()));
            folderList.setModel(listmodel);
            folderList.invalidate();
            if (folder == structureTree.getModel().getRoot()) setRootInfo(); else loadFolderDescription(folder);
            folderList.repaint();
        }
    }

    private void loadFolderDescription(StructureFolder folder) {
        Stack<String> stack = new Stack<String>();
        stack.push(folder.getName());
        while (folder.getParent() != null) {
            folder = folder.getParent();
            stack.push(folder.getName());
        }
        if (folder.getDescriptionUri() != null && folder.getDescriptionUri().length() > 0) {
            logger.info("Loading " + folder.getDescriptionUri());
            try {
                URI uri = source.resolveUri(null, folder.getDescriptionUri());
                infoPanel.setPage(uri.toURL());
                infoPanel.setCaretPosition(0);
            } catch (Exception e) {
                logger.error(e);
                infoPanel.setText("");
            }
        } else {
            infoPanel.setText("");
        }
    }

    /**
	 * Maps the primary open viewer for a pointer
	 */
    Hashtable<String, FaksimileViewer> openViewers = new Hashtable<String, FaksimileViewer>();

    ArrayList<FaksimileViewer> allViewers = new ArrayList<FaksimileViewer>();

    ArrayList<SearchWindow> openSearchWindows = new ArrayList<SearchWindow>();

    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() instanceof SearchWindow) {
            Match match = (Match) e.getItem();
            String faksimileURI = getSource().getFaksimileFromTEI(null, match.getPointer());
            FaksimileViewer viewer;
            if (openViewers.containsKey(faksimileURI)) {
                viewer = openViewers.get(faksimileURI);
                TranskriptionModelElement from = source.getTranskriptionModelElement(match.getPointer());
                viewer.selectFromTo(from, match.getFrom(), match.getTo());
                if (viewer.getExtendedState() == JFrame.ICONIFIED) viewer.setExtendedState(JFrame.NORMAL);
                viewer.setVisible(true);
                viewer.toFront();
            } else {
                final ProgressMonitor monitor = new ProgressMonitor(this, "Lade Textträger-Faksimile", "", 0, 500);
                FaksimileLoaderThread loadrunner = new FaksimileLoaderThread(getSource(), monitor, faksimileURI, this);
                loadrunner.setMatch(match);
                Thread loader = new Thread(loadrunner);
                loader.start();
            }
        }
    }

    public void mouseClicked(MouseEvent evt) {
        final FaksimileDescription desc = (FaksimileDescription) folderList.getSelectedValue();
        if (desc != null) {
            if (evt.getClickCount() > 1) {
                FaksimileViewer viewer;
                if (openViewers.containsKey(desc.getTrdUri())) {
                    viewer = openViewers.get(desc.getTrdUri());
                    viewer.setVisible(true);
                    viewer.toFront();
                } else {
                    final ProgressMonitor monitor = new ProgressMonitor(this, "Lade Textträger-Faksimile", "", 0, 500);
                    FaksimileLoaderThread loadrunner = new FaksimileLoaderThread(getSource(), monitor, desc.getTrdUri(), this);
                    Thread loader = new Thread(loadrunner);
                    loader.start();
                }
            } else {
                if (desc.getDescriptionUri() != null && desc.getDescriptionUri().length() > 0) {
                    logger.info("Loading " + desc.getDescriptionUri());
                    logger.info("Loading " + desc.getDescriptionUri());
                    try {
                        URI uri = source.resolveUri(null, desc.getDescriptionUri());
                        infoPanel.setPage(uri.toURL());
                        infoPanel.setCaretPosition(0);
                    } catch (Exception e) {
                        logger.error(e);
                        infoPanel.setText("");
                    }
                } else {
                    infoPanel.setText("");
                }
            }
        }
    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mousePressed(MouseEvent arg0) {
    }

    public void mouseReleased(MouseEvent arg0) {
    }

    public void windowActivated(WindowEvent arg0) {
    }

    public void windowClosed(WindowEvent arg0) {
    }

    public void windowClosing(WindowEvent arg0) {
        int res = JOptionPane.showConfirmDialog(this, "Soll '" + getSource().getEditionName() + "' beendet werden?", "Programm verlassen?", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            this.setVisible(false);
            Iterator<FaksimileViewer> it = allViewers.iterator();
            while (it.hasNext()) it.next().dispose();
            Iterator<SearchWindow> it2 = openSearchWindows.iterator();
            while (it2.hasNext()) it2.next().dispose();
            if (helpBroker != null) {
                helpBroker.setDisplayed(false);
            }
            this.dispose();
            System.exit(0);
        }
    }

    public void windowDeactivated(WindowEvent arg0) {
    }

    public void windowDeiconified(WindowEvent arg0) {
    }

    public void windowIconified(WindowEvent arg0) {
    }

    public void windowOpened(WindowEvent arg0) {
        if (splashScreen != null) {
            Thread delayedCloser = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    splashScreen.setVisible(false);
                    splashScreen.dispose();
                }
            });
            delayedCloser.start();
        }
        verticalSplit.setDividerLocation(0.4);
        horizontalSplit.setDividerLocation(0.2);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("SEARCH")) {
            openSearchWindow(null);
            return;
        }
        if (e.getActionCommand().equals("HELP")) {
            openHelpWindow("KWA.Fenster.Hauptfenster");
            return;
        }
    }

    public void openHelpWindow(String string) {
        final HelpBroker hb = getHelpBroker();
        if (hb != null) {
            if (string == null) hb.setCurrentView("Index"); else {
                hb.setCurrentView("TOC");
                hb.setCurrentID(string);
            }
            hb.setDisplayed(true);
        }
    }

    public SearchWindow openSearchWindow(String group) {
        SearchWindow searchForm = new SearchWindow();
        openSearchWindows.add(searchForm);
        searchForm.setSource(source);
        if (group != null) searchForm.setSelectedGroup(group);
        searchForm.addItemListener(this);
        searchForm.setMaster(this);
        searchForm.setVisible(true);
        searchForm.toFront();
        return searchForm;
    }

    public void pointerActivated(PointerEvent e) {
        String[] pointerParts = e.getPointer().split("@");
        String faksimileUri = source.getFaksimileFromTEI(null, pointerParts[0]);
        if (faksimileUri == null) logger.warn("Invalid pointer: " + e.getPointer());
        if (openViewers.containsKey(faksimileUri)) {
            FaksimileViewer v = openViewers.get(faksimileUri);
            if (pointerParts.length > 1) {
                TranskriptionModelElement page = ((Transkription) v.getFaksimile().getTranskription()).getPage(pointerParts[1]);
                if (page == null) JOptionPane.showMessageDialog(v, "Seite " + pointerParts[1] + " konnte nicht gefunden werden!"); else {
                    if (page.getId() != null) {
                        v.scrollToId(page.getId());
                    }
                }
            }
            v.setVisible(true);
            v.toFront();
        } else {
            if (pointerParts.length > 1) faksimileUri = faksimileUri + '@' + pointerParts[1];
            final ProgressMonitor monitor = new ProgressMonitor(this, "Lade Textträger-Faksimile", "", 0, 500);
            FaksimileLoaderThread loadrunner = new FaksimileLoaderThread(getSource(), monitor, faksimileUri, this);
            Thread loader = new Thread(loadrunner);
            loader.start();
        }
    }

    public SplashScreen getSplashScreen() {
        return splashScreen;
    }

    public void setSplashScreen(SplashScreen splashScreen) {
        this.splashScreen = splashScreen;
    }

    public void addControlledViewer(FaksimileViewer viewer) {
        allViewers.add(viewer);
        viewer.setParent(this);
        viewer.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public void tileWindowsHorizontal() {
        int num = 0;
        Iterator<FaksimileViewer> vit = allViewers.iterator();
        while (vit.hasNext()) {
            final FaksimileViewer viewer = vit.next();
            if (viewer.getState() == Frame.NORMAL && viewer.isVisible() || viewer.getState() == Frame.ICONIFIED) num++;
        }
        vit = allViewers.iterator();
        int count = 0;
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        while (vit.hasNext()) {
            final FaksimileViewer viewer = vit.next();
            if (viewer.getState() == Frame.NORMAL && viewer.isVisible() || viewer.getState() == Frame.ICONIFIED) {
                Rectangle bounds = new Rectangle(0, (count * screenDim.height) / num, screenDim.width, screenDim.height / num);
                count++;
                viewer.setState(Frame.NORMAL);
                viewer.setBounds(bounds);
            }
        }
    }

    public void tileWindowsVertical() {
        int num = 0;
        Iterator<FaksimileViewer> vit = allViewers.iterator();
        while (vit.hasNext()) {
            final FaksimileViewer viewer = vit.next();
            if (viewer.getState() == Frame.NORMAL && viewer.isVisible() || viewer.getState() == Frame.ICONIFIED) num++;
        }
        int count = 0;
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        vit = allViewers.iterator();
        while (vit.hasNext()) {
            final FaksimileViewer viewer = vit.next();
            if (viewer.getState() == Frame.NORMAL && viewer.isVisible() || viewer.getState() == Frame.ICONIFIED) {
                Rectangle bounds = new Rectangle((count * screenDim.width) / num, 0, screenDim.width / num, screenDim.height);
                count++;
                viewer.setState(Frame.NORMAL);
                viewer.setBounds(bounds);
            }
        }
    }

    public void cascadeWindows() {
        Iterator<FaksimileViewer> vit = allViewers.iterator();
        int count = 0;
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        while (vit.hasNext()) {
            final FaksimileViewer viewer = vit.next();
            if (viewer.getState() == Frame.NORMAL && viewer.isVisible() || viewer.getState() == Frame.ICONIFIED) {
                Rectangle bounds = new Rectangle(20 * count, 20 * count, screenDim.width - 20 * count, screenDim.height - 20 * count);
                count++;
                viewer.setState(Frame.NORMAL);
                viewer.requestFocus();
                viewer.setBounds(bounds);
            }
        }
    }

    class FaksimileLoaderThread implements Runnable {

        Faksimile faksimile;

        IEditionSource source;

        ProgressMonitor monitor;

        String pointer;

        FaksimileViewer viewer;

        String id = null;

        String terms = null;

        EEViewer master = null;

        Match match;

        public FaksimileLoaderThread(IEditionSource source, ProgressMonitor monitor, String pointer, EEViewer master) {
            super();
            this.source = source;
            this.monitor = monitor;
            this.pointer = pointer;
            this.master = master;
        }

        public Match getMatch() {
            return match;
        }

        public void setMatch(Match match) {
            this.match = match;
        }

        public void setTerms(String terms) {
            this.terms = terms;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void run() {
            monitor.setNote("... lade Beschreibung...");
            String[] pointerParts = pointer.split("@");
            faksimile = source.getFaksimile(pointerParts[0], new IProgressWatcher() {

                public void notifyProgress(int progress) {
                    monitor.setProgress(progress);
                }
            });
            monitor.setNote("... konfiguriere...");
            monitor.setProgress(300);
            RenderConfig config = new RenderConfig();
            RenderConfigParser parser = new RenderConfigParser(config);
            try {
                parser.parse(new InputSource(source.getRessourceFromUri(pointerParts[0], faksimile.getConfigUri())));
            } catch (Exception e) {
                e.printStackTrace();
            }
            monitor.setNote("... starte Textträgeransicht...");
            monitor.setProgress(400);
            viewer = new FaksimileViewer(faksimile, config, getSource());
            viewer.setParent(master);
            viewer.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            viewer.addPointerListener(master);
            openViewers.put(pointerParts[0], viewer);
            allViewers.add(viewer);
            if (match != null) {
                TranskriptionModelElement from = source.getTranskriptionModelElement(match.getPointer());
                viewer.selectFromTo(from, match.getFrom(), match.getTo());
            }
            monitor.setNote("...fertig.");
            monitor.setProgress(500);
            if (pointerParts.length > 1) {
                logger.info(((Transkription) faksimile.getTranskription()).getPage(pointerParts[1]));
                viewer.scrollToId(((Transkription) faksimile.getTranskription()).getPage(pointerParts[1]).getId());
            }
            viewer.setVisible(true);
            viewer.toFront();
        }
    }
}

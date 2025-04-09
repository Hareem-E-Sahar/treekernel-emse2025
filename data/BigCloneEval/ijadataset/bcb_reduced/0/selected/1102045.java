package org.thesandbox.itask;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.BreakIterator;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main iTask application frame.
 *
 * Created by IntelliJ IDEA.
 *
 * Date: 30-Nov-2009
 * @author jrxrs
 */
public class ITaskView extends FrameView {

    private static Logger logger = Logger.getLogger(ITaskView.class.getCanonicalName());

    private ResourceMap resourceMap;

    private ActionMap actionMap;

    private JFrame mainFrame;

    private JPanel mainPanel;

    private JProgressBar progressBar;

    private JLabel statusAnimationLabel, statusMessageLabel;

    private JMenuItem rescanMenuItem;

    private JButton rescanToolBarButton;

    private ITaskTriggers triggersDialog;

    private Status status = Status.NO_PATH;

    private int dlCount;

    private JDialog aboutBox;

    private Timer rescanTimer;

    public static enum Status {

        NO_PATH("status.NoRepPath.text"), NO_DOWNLOADS("status.NoDownloads.text"), SCANNING("status.Scanning.text"), DOWNLOADING("status.Downloading.text"), COMPLETE("status.DownloadingComplete.text"), STALE("status.StaleDownloads.text");

        private String resource;

        private Status(String resource) {
            this.resource = resource;
        }

        public String getResource() {
            return resource;
        }
    }

    public ITaskView(SingleFrameApplication app) {
        super(app);
        resourceMap = getResourceMap();
        actionMap = app.getContext().getActionMap(ITaskView.class, this);
        mainFrame = getFrame();
        triggersDialog = ITaskTriggers.getInstance(mainFrame);
        dlCount = 0;
        processProperties();
        initComponents();
        if (status == Status.SCANNING) {
            bgScan();
        }
        startTimer();
    }

    @Action
    public void rescan() {
        bgScan();
    }

    @Action
    public void configTrig() {
        triggersDialog.setLocationRelativeTo(mainFrame);
        ITaskApp.getApplication().show(triggersDialog);
    }

    @Action
    public void iPlayer() {
        desktopBrowse(resourceMap.getString("iPlayer.url"));
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            aboutBox = new ITaskAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        ITaskApp.getApplication().show(aboutBox);
    }

    /**
     * Create a new Setting dialog to allow the user to view and/or alter their
     * settings.
     */
    @Action
    public void showSettings() {
        ITaskSettings settingsDialog = new ITaskSettings(mainFrame);
        settingsDialog.setLocationRelativeTo(mainFrame);
        ITaskApp.getApplication().show(settingsDialog);
        if (settingsDialog.getDisposeAction() == ITaskSettings.SAVE) {
            bgScan();
            reconfigureTimer();
        }
    }

    private void processProperties() {
        if (ITaskProperties.NOT_SET.equals(ITaskProperties.getInstance().get(ITaskProperties.REP_PATH, ITaskProperties.NOT_SET))) {
            status = Status.NO_PATH;
        } else {
            status = Status.SCANNING;
        }
    }

    public void setStatus(Status newStatus) {
        status = newStatus;
        statusMessageLabel.setText(resourceMap.getString(status.getResource()));
        switch(status) {
            case COMPLETE:
                progressBar.setValue(0);
                break;
            case NO_DOWNLOADS:
                progressBar.setIndeterminate(false);
                break;
            case DOWNLOADING:
                progressBar.setIndeterminate(true);
                break;
            case STALE:
                progressBar.setValue(50);
                break;
        }
        triggersDialog.newStatusSet(newStatus);
    }

    /**
     * Enable or disable the rescan actions.
     *
     * @param enabled   true to enable the rescan action
     *                  false to disable the rescan action
     */
    private void rescanEnabled(boolean enabled) {
        rescanMenuItem.setEnabled(enabled);
        rescanToolBarButton.setEnabled(enabled);
    }

    private void bgScan() {
        setStatus(Status.SCANNING);
        progressBar.setIndeterminate(true);
        rescanEnabled(false);
        BGRepScan task = new BGRepScan();
        task.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    progressBar.setIndeterminate(false);
                    progressBar.setValue((Integer) evt.getNewValue());
                }
            }
        });
        task.execute();
    }

    private void startTimer() {
        rescanTimer = new Timer();
        try {
            int duration = Integer.parseInt(ITaskProperties.getInstance().get(ITaskProperties.RESCAN_PERIOD));
            if (duration > 29) {
                duration *= 1000;
                rescanTimer.scheduleAtFixedRate(new TimerScan(), duration, duration);
            } else {
                logger.log(Level.INFO, "Rescan duration was less than 30 seconds" + ", auto rescan disabled.");
            }
        } catch (NumberFormatException nfe) {
            logger.log(Level.WARNING, ITaskProperties.RESCAN_PERIOD + " property was not a number.", nfe);
        }
    }

    private void reconfigureTimer() {
        rescanTimer.cancel();
        rescanTimer.purge();
        startTimer();
    }

    private class TimerScan extends TimerTask {

        @Override
        public void run() {
            bgScan();
        }
    }

    private void desktopBrowse(String uri) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(uri));
            } catch (URISyntaxException urise) {
                logger.log(Level.WARNING, "Incorrect URI", urise);
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "General IO Error", ioe);
            }
        }
    }

    private void initComponents() {
        getFrame().setIconImage((resourceMap.getImageIcon("window.icon")).getImage());
        getFrame().setPreferredSize(new Dimension(750, 500));
        JPanel mainPanelHolder = new JPanel(new BorderLayout());
        mainPanel = new JPanel();
        JPanel statusPanel = new JPanel();
        JSeparator statusPanelSeparator = new JSeparator();
        statusMessageLabel = new JLabel();
        statusAnimationLabel = new JLabel();
        progressBar = new JProgressBar();
        mainPanel.setName("mainPanel");
        mainPanel.setBorder(BorderFactory.createTitledBorder(null, resourceMap.getString("mainPanel.border.text"), TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, UIManager.getFont("TitledBorder.font").deriveFont(16.0f)));
        mainPanel.setLayout(new GridLayout(0, 1));
        mainPanel.add(new JLabel(resourceMap.getIcon("downloading.gif")));
        statusPanel.setName("statusPanel");
        statusPanelSeparator.setName("statusPanelSeparator");
        statusMessageLabel.setName("statusMessageLabel");
        statusMessageLabel.setText(resourceMap.getString(status.getResource()));
        statusAnimationLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel");
        statusAnimationLabel.setText(String.valueOf(dlCount));
        progressBar.setName("progressBar");
        GroupLayout statusPanelLayout = new GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(statusPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(statusPanelSeparator, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE).addGroup(statusPanelLayout.createSequentialGroup().addContainerGap().addComponent(statusMessageLabel).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 226, Short.MAX_VALUE).addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(statusAnimationLabel).addContainerGap()));
        statusPanelLayout.setVerticalGroup(statusPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(statusPanelLayout.createSequentialGroup().addComponent(statusPanelSeparator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(statusPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(statusMessageLabel).addComponent(statusAnimationLabel).addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(3, 3, 3)));
        JScrollPane jsp = new JScrollPane(mainPanel);
        JScrollBar jsb = jsp.getVerticalScrollBar();
        jsb.setUnitIncrement(75);
        mainPanelHolder.add(createToolBar(), BorderLayout.NORTH);
        mainPanelHolder.add(jsp, BorderLayout.CENTER);
        setComponent(mainPanelHolder);
        setMenuBar(getITaskViewMenuBar());
        setStatusBar(statusPanel);
    }

    private JComponent createToolBar() {
        String[] toolbarActionNames = { "rescan", "configTrig", "iPlayer" };
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        Border border = new EmptyBorder(2, 9, 2, 9);
        for (String actionName : toolbarActionNames) {
            JButton button = new JButton();
            button.setBorder(border);
            button.setVerticalTextPosition(JButton.BOTTOM);
            button.setHorizontalTextPosition(JButton.CENTER);
            button.setAction(actionMap.get(actionName));
            button.setIcon(resourceMap.getImageIcon(actionName + ".icon"));
            button.setFocusable(false);
            toolBar.add(button);
            if ("rescan".equals(actionName)) rescanToolBarButton = button;
        }
        return toolBar;
    }

    private JMenuBar getITaskViewMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu();
        JMenuItem exitMenuItem = new JMenuItem();
        rescanMenuItem = new JMenuItem();
        JMenuItem configTrigMenuItem = new JMenuItem();
        JMenuItem iPlayerWebsiteMenuItem = new JMenuItem();
        JMenu optionsMenu = new JMenu();
        JMenuItem settingsMenuItem = new JMenuItem();
        JMenu helpMenu = new JMenu();
        JMenuItem aboutMenuItem = new JMenuItem();
        menuBar.setName("menuBar");
        fileMenu.setText(resourceMap.getString("fileMenu.text"));
        fileMenu.setName("fileMenu");
        rescanMenuItem.setAction(actionMap.get("rescan"));
        rescanMenuItem.setName("rescanMenuItem");
        fileMenu.add(rescanMenuItem);
        configTrigMenuItem.setAction(actionMap.get("configTrig"));
        configTrigMenuItem.setName("configTrigMenuItem");
        fileMenu.add(configTrigMenuItem);
        iPlayerWebsiteMenuItem.setAction(actionMap.get("iPlayer"));
        iPlayerWebsiteMenuItem.setName("iPlayerWebsiteMenuItem");
        fileMenu.add(iPlayerWebsiteMenuItem);
        fileMenu.addSeparator();
        exitMenuItem.setAction(actionMap.get("quit"));
        exitMenuItem.setName("exitMenuItem");
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        optionsMenu.setText(resourceMap.getString("optionsMenu.text"));
        optionsMenu.setName("optionsMenu");
        settingsMenuItem.setAction(actionMap.get("showSettings"));
        settingsMenuItem.setName("settingsMenuItem");
        optionsMenu.add(settingsMenuItem);
        menuBar.add(optionsMenu);
        helpMenu.setText(resourceMap.getString("helpMenu.text"));
        helpMenu.setName("helpMenu");
        aboutMenuItem.setAction(actionMap.get("showAboutBox"));
        aboutMenuItem.setName("aboutMenuItem");
        helpMenu.add(aboutMenuItem);
        menuBar.add(helpMenu);
        return menuBar;
    }

    /**
     * Inner class to handle scanning the iPlayer Repository for files.
     */
    class BGRepScan extends SwingWorker<HashMap<String, IPlayerDownload>, IPlayerDownload> {

        private final HashMap<String, IPlayerDownload> downloads;

        public BGRepScan() {
            this.downloads = new HashMap<String, IPlayerDownload>();
        }

        @Override
        public HashMap<String, IPlayerDownload> doInBackground() {
            File repDir = new File(ITaskProperties.getInstance().get(ITaskProperties.REP_PATH));
            if (repDir.isDirectory()) {
                File[] folders = repDir.listFiles();
                ArrayList<File> temp = new ArrayList<File>(folders.length);
                for (File f : folders) {
                    if (!f.getName().equalsIgnoreCase("cache")) temp.add(f);
                }
                for (File f : temp) {
                    if (isCancelled()) break;
                    IPlayerDownload ipdl = IPlayerDownload.parse(f);
                    downloads.put(ipdl.getTitle(), ipdl);
                    publish(ipdl);
                    setProgress((downloads.size() / temp.size()) * 100);
                }
            }
            return downloads;
        }

        @Override
        protected void process(List<IPlayerDownload> chunks) {
            for (IPlayerDownload ipdl : chunks) {
                statusAnimationLabel.setText(String.valueOf(dlCount++));
            }
        }

        @Override
        public void done() {
            FormLayout layout = new FormLayout("pref, 10dlu, pref:grow, 5dlu, pref", "");
            DefaultFormBuilder builder = new DefaultFormBuilder(layout);
            Status localStatus = Status.COMPLETE;
            Object[] titles = downloads.keySet().toArray();
            Arrays.sort(titles);
            for (Object title : titles) {
                IPlayerDownload ipdl = downloads.get((String) title);
                try {
                    builder.appendSeparator(ipdl.getTitle());
                    JLabel pic = new JLabel(new ImageIcon(ipdl.getIcon().getImage().getScaledInstance(320, -1, Image.SCALE_SMOOTH)));
                    final String url = ipdl.getUrl();
                    pic.addMouseListener(new MouseAdapter() {

                        @Override
                        public void mouseClicked(MouseEvent me) {
                            desktopBrowse(url);
                        }
                    });
                    pic.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    pic.setToolTipText(resourceMap.getString("openInBrowser.text"));
                    builder.append(pic);
                    JWrappableLabel sum = new JWrappableLabel(mainFrame, ipdl.getSummary(), 425);
                    builder.append(sum);
                    JLabel icon = new JLabel();
                    if (ipdl.isComplete()) {
                        icon.setIcon(resourceMap.getIcon("complete.icon"));
                    } else {
                        if (ipdl.isStale()) {
                            localStatus = Status.STALE;
                            icon.setIcon(resourceMap.getIcon("stale.icon"));
                        } else {
                            localStatus = localStatus == Status.STALE ? Status.STALE : Status.DOWNLOADING;
                            icon.setIcon(resourceMap.getIcon("downloading.gif"));
                        }
                    }
                    builder.append(icon);
                    builder.nextLine();
                } catch (NullPointerException npe) {
                }
            }
            mainPanel.removeAll();
            mainPanel.add(builder.getPanel());
            statusAnimationLabel.setText(String.valueOf(downloads.size()));
            dlCount = 0;
            if (downloads.size() == 0) {
                localStatus = Status.NO_DOWNLOADS;
            }
            setStatus(localStatus);
            rescanEnabled(true);
        }
    }

    private class JWrappableLabel extends JLabel {

        private Component parent;

        private String text;

        private int rel;

        public JWrappableLabel(Component parent, String text, int rel) {
            super();
            this.parent = parent;
            this.text = text;
            this.rel = rel;
            addComponentListener(new WrappableLabelListener());
            wrapLabelText();
        }

        public void setWrappedText(String text) {
            this.text = text;
            wrapLabelText();
        }

        public void wrapLabelText() {
            FontMetrics fm = getFontMetrics(getFont());
            BreakIterator boundary = BreakIterator.getWordInstance();
            boundary.setText(text);
            StringBuffer trial = new StringBuffer();
            StringBuffer real = new StringBuffer("<html>");
            int width = parent.getSize().width - rel;
            int start = boundary.first();
            for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
                String word = text.substring(start, end);
                trial.append(word);
                int trialWidth = SwingUtilities.computeStringWidth(fm, trial.toString());
                if (trialWidth > width) {
                    trial = new StringBuffer(word);
                    real.append("<br>");
                }
                real.append(word);
            }
            real.append("</html>");
            super.setText(real.toString());
        }

        private class WrappableLabelListener extends ComponentAdapter {

            @Override
            public void componentResized(ComponentEvent ce) {
                wrapLabelText();
            }
        }
    }
}

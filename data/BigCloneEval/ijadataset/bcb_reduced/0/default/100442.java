import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class Gui_Settings extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel pathPanel = new JPanel();

    private JPanel languagePanel = new JPanel();

    private JPanel actionPanel = new JPanel();

    private JPanel buttonPanel = new JPanel();

    private JPanel commonPanel = new JPanel();

    private JTextField ripperPathField = new JTextField("", 30);

    private JTextField shoutcastPlayer = new JTextField("", 30);

    private JTextField generellPathField = new JTextField("", 30);

    private JTextField fileBrowserField = new JTextField("", 30);

    private JTextField webBrowserField = new JTextField("", 30);

    private ImageIcon findIcon = new ImageIcon((URL) getClass().getResource("Icons/open_small.png"));

    private ImageIcon langIcon = new ImageIcon((URL) getClass().getResource("Icons/lang_small.png"));

    private ImageIcon saveAndExitIcon = new ImageIcon((URL) getClass().getResource("Icons/ok_small.png"));

    private ImageIcon saveIcon = new ImageIcon((URL) getClass().getResource("Icons/save_small.png"));

    private ImageIcon abortIcon = new ImageIcon((URL) getClass().getResource("Icons/abort_small.png"));

    private JLabel ripLabel = new JLabel("Path to streamripper: ");

    private JLabel mediaPlayer = new JLabel("Path to mp3 player: ");

    private JLabel generellPathLabel = new JLabel("Generell Save : ");

    private JLabel fileBrowserLabel = new JLabel("Path to filemanager");

    private JLabel webBrowserLabel = new JLabel("Path to webbrowser");

    private JLabel reqRestart = new JLabel("Chances require programmrestart");

    private JLabel explainActionLabel = new JLabel("What to to, when doubleclicking on a Field: ");

    private JLabel statusLabel = new JLabel("Status :");

    private JLabel nameLabel = new JLabel("Name :");

    private JLabel currentTrackLabel = new JLabel("Current Track: ");

    private JLabel windowClosing = new JLabel("Action when clicking on closing window");

    private JButton abortButton = new JButton("Abort", abortIcon);

    private JButton saveAndExitButton = new JButton("OK", saveAndExitIcon);

    private JButton saveButton = new JButton("Save", saveIcon);

    private JButton browseRipper = new JButton(findIcon);

    private JButton browseMP3Player = new JButton(findIcon);

    private JButton browseGenerellPath = new JButton(findIcon);

    private JButton browseFileBrowserPath = new JButton(findIcon);

    private JButton browseWebBrowserPath = new JButton(findIcon);

    private JFileChooser dirChooser;

    private String[] languages = { "English", "German" };

    private String[] actions = { "none", "Open Browser", "edit Stream", "start/stop", "streamoptions", "play Stream", "Open scheduler" };

    private String[] windowActions = { "do nothing", "Exit Stripper", "Send in Systemtray" };

    private JComboBox langMenu = new JComboBox(languages);

    private JComboBox statusBox = new JComboBox(actions);

    private JComboBox nameBox = new JComboBox(actions);

    private JComboBox currentTrackBox = new JComboBox(actions);

    private JComboBox windowActionBox = new JComboBox(windowActions);

    private JCheckBox activeTrayIcon = new JCheckBox("Show Systemtray (requires restart)");

    private JCheckBox showTextCheckBox = new JCheckBox("Show Text under Icons");

    private ResourceBundle trans;

    private Properties stripperSettings = new Properties();

    private String fileSettingsPath = "NOT_FOUND";

    private JTabbedPane settingsPane = new JTabbedPane();

    public Gui_Settings(ResourceBundle trans) {
        super("Preferences");
        this.trans = trans;
        this.fileSettingsPath = (new Control_TestStartFirst()).getStripperPath();
        setLayout(new BorderLayout());
        settingsPane.addTab("General", commonPanel);
        settingsPane.addTab("Path", pathPanel);
        settingsPane.addTab("Action", actionPanel);
        settingsPane.addTab("Language", langIcon, languagePanel);
        add(settingsPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pathPanel.setLayout(new GridBagLayout());
        languagePanel.setLayout(new GridBagLayout());
        actionPanel.setLayout(new GridBagLayout());
        buttonPanel.setLayout(new GridBagLayout());
        commonPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 5, 2, 5);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridy = 0;
        c.gridx = 0;
        pathPanel.add(ripLabel, c);
        c.gridx = 1;
        c.weightx = 1.0;
        pathPanel.add(ripperPathField, c);
        c.gridx = 2;
        c.weightx = 0.0;
        pathPanel.add(browseRipper, c);
        c.gridy = 1;
        c.gridx = 0;
        pathPanel.add(mediaPlayer, c);
        c.gridx = 1;
        c.weightx = 1.0;
        pathPanel.add(shoutcastPlayer, c);
        c.gridx = 2;
        c.weightx = 0.0;
        pathPanel.add(browseMP3Player, c);
        c.gridy = 2;
        c.gridx = 0;
        pathPanel.add(generellPathLabel, c);
        c.gridx = 1;
        c.weightx = 1.0;
        pathPanel.add(generellPathField, c);
        c.gridx = 2;
        c.weightx = 0.0;
        pathPanel.add(browseGenerellPath, c);
        c.gridy = 3;
        c.gridx = 0;
        pathPanel.add(webBrowserLabel, c);
        c.gridx = 1;
        c.weightx = 1.0;
        pathPanel.add(webBrowserField, c);
        c.gridx = 2;
        c.weightx = 0.0;
        pathPanel.add(browseWebBrowserPath, c);
        c.gridy = 4;
        c.gridx = 0;
        pathPanel.add(fileBrowserLabel, c);
        c.gridx = 1;
        c.weightx = 1.0;
        pathPanel.add(fileBrowserField, c);
        c.gridx = 2;
        c.weightx = 0.0;
        pathPanel.add(browseFileBrowserPath, c);
        c.weightx = 0.0;
        c.gridy = 0;
        c.gridx = 0;
        languagePanel.add(langMenu, c);
        c.weightx = 1.0;
        c.gridx = 1;
        languagePanel.add(new JLabel(""), c);
        c.weightx = 0.0;
        c.gridy = 1;
        c.gridx = 0;
        c.gridwidth = 2;
        languagePanel.add(reqRestart, c);
        c.insets = new Insets(5, 5, 10, 5);
        c.weightx = 0.0;
        c.gridy = 0;
        c.gridx = 0;
        c.gridwidth = 7;
        actionPanel.add(explainActionLabel, c);
        c.gridx = 0;
        c.weightx = 1;
        actionPanel.add(new JLabel(""), c);
        c.insets = new Insets(2, 30, 2, 5);
        c.weightx = 0;
        c.gridwidth = 1;
        c.gridy = 1;
        c.gridx = 0;
        actionPanel.add(statusLabel, c);
        c.gridx = 1;
        actionPanel.add(statusBox, c);
        c.gridy = 2;
        c.gridx = 0;
        actionPanel.add(nameLabel, c);
        c.gridx = 1;
        actionPanel.add(nameBox, c);
        c.gridy = 3;
        c.gridx = 0;
        actionPanel.add(currentTrackLabel, c);
        c.gridx = 1;
        actionPanel.add(currentTrackBox, c);
        c.insets = new Insets(2, 5, 2, 5);
        c.gridy = 0;
        c.gridx = 0;
        c.gridwidth = 2;
        commonPanel.add(activeTrayIcon, c);
        c.gridwidth = 1;
        c.gridy = 1;
        commonPanel.add(windowClosing, c);
        c.gridx = 1;
        commonPanel.add(windowActionBox, c);
        c.gridx = 2;
        c.weightx = 1;
        commonPanel.add(new JLabel(""), c);
        c.gridy = 2;
        c.gridx = 0;
        commonPanel.add(showTextCheckBox, c);
        c.insets = new Insets(5, 5, 5, 5);
        c.weightx = 0;
        c.gridy = 0;
        c.gridx = 0;
        c.gridwidth = 1;
        buttonPanel.add(saveAndExitButton, c);
        c.gridx = 1;
        buttonPanel.add(saveButton, c);
        c.weightx = 1.0;
        c.gridx = 2;
        buttonPanel.add(new JLabel(""), c);
        c.weightx = 0.0;
        c.gridx = 3;
        buttonPanel.add(abortButton, c);
        statusBox.setSelectedIndex(3);
        nameBox.setSelectedIndex(5);
        currentTrackBox.setSelectedIndex(4);
        dirChooser = new JFileChooser();
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        abortButton.addActionListener(new ExitListener());
        saveButton.addActionListener(new SaveListener());
        saveAndExitButton.addActionListener(new SaveAndExitListener());
        browseMP3Player.addActionListener(new MP3Listener());
        browseRipper.addActionListener(new RipperPathListener());
        browseGenerellPath.addActionListener(new BrowseListener());
        browseWebBrowserPath.addActionListener(new WebBrowserListener());
        browseFileBrowserPath.addActionListener(new FileBrowserListener());
        activeTrayIcon.addActionListener(new ChangeTrayFields());
        setLanguage();
        getPathFromOptionFile();
        repaintCommon();
        pack();
        Dimension frameDim = getSize();
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenDim.width - frameDim.width) / 2;
        int y = (screenDim.height - frameDim.height) / 2;
        setLocation(x, y);
        setVisible(true);
        KeyStroke escStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
        getRootPane().registerKeyboardAction(new ExitListener(), escStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public void setLanguage() {
        try {
            setTitle(trans.getString("pref"));
            settingsPane.setTitleAt(0, trans.getString("tab.general"));
            settingsPane.setTitleAt(1, trans.getString("tab.path"));
            settingsPane.setTitleAt(2, trans.getString("tab.action"));
            settingsPane.setTitleAt(3, trans.getString("tab.language"));
            activeTrayIcon.setText(trans.getString("showSysTray"));
            windowClosing.setText(trans.getString("actionX"));
            windowActionBox.removeAllItems();
            windowActionBox.addItem(trans.getString("X.doNothing"));
            windowActionBox.addItem(trans.getString("X.Exit"));
            windowActionBox.addItem(trans.getString("X.inTray"));
            explainActionLabel.setText(trans.getString("whenClickAction"));
            actions[0] = trans.getString("X.doNothing");
            actions[1] = trans.getString("action.OpenBrowser");
            actions[2] = trans.getString("action.editStream");
            actions[3] = trans.getString("action.startStop");
            actions[4] = trans.getString("action.streamOptions");
            actions[5] = trans.getString("action.playStream");
            actions[6] = trans.getString("action.Openscheduler");
            statusBox.removeAllItems();
            statusBox.addItem(actions[0]);
            statusBox.addItem(actions[1]);
            statusBox.addItem(actions[2]);
            statusBox.addItem(actions[3]);
            statusBox.addItem(actions[4]);
            statusBox.addItem(actions[5]);
            statusBox.addItem(actions[6]);
            nameBox.removeAllItems();
            nameBox.addItem(actions[0]);
            nameBox.addItem(actions[1]);
            nameBox.addItem(actions[2]);
            nameBox.addItem(actions[3]);
            nameBox.addItem(actions[4]);
            nameBox.addItem(actions[5]);
            nameBox.addItem(actions[6]);
            currentTrackBox.removeAllItems();
            currentTrackBox.addItem(actions[0]);
            currentTrackBox.addItem(actions[1]);
            currentTrackBox.addItem(actions[2]);
            currentTrackBox.addItem(actions[3]);
            currentTrackBox.addItem(actions[4]);
            currentTrackBox.addItem(actions[5]);
            currentTrackBox.addItem(actions[6]);
            ripLabel.setText(trans.getString("pathStreamripper"));
            mediaPlayer.setText(trans.getString("pathToMp3Player"));
            generellPathLabel.setText(trans.getString("genSavePath"));
            fileBrowserLabel.setText(trans.getString("filebrowserPath"));
            webBrowserLabel.setText(trans.getString("webBrowserPath"));
            reqRestart.setText(trans.getString("reqRestart"));
            abortButton.setText(trans.getString("abortButton"));
            saveAndExitButton.setText(trans.getString("okButton"));
            saveButton.setText(trans.getString("save"));
        } catch (MissingResourceException e) {
            System.err.println(e);
        }
    }

    private Gui_Settings gimme() {
        return this;
    }

    public class WebBrowserListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            dirChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int i = dirChooser.showOpenDialog(gimme());
            if (i == JFileChooser.APPROVE_OPTION) {
                webBrowserField.setText(dirChooser.getSelectedFile().toString());
            }
        }
    }

    public class FileBrowserListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            dirChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int i = dirChooser.showOpenDialog(gimme());
            if (i == JFileChooser.APPROVE_OPTION) {
                fileBrowserField.setText(dirChooser.getSelectedFile().toString());
            }
        }
    }

    public class RipperPathListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            dirChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int i = dirChooser.showOpenDialog(gimme());
            if (i == JFileChooser.APPROVE_OPTION) {
                ripperPathField.setText(dirChooser.getSelectedFile().toString());
            }
        }
    }

    public class MP3Listener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            dirChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int i = dirChooser.showOpenDialog(gimme());
            if (i == JFileChooser.APPROVE_OPTION) {
                shoutcastPlayer.setText(dirChooser.getSelectedFile().toString());
            }
        }
    }

    public class BrowseListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int i = dirChooser.showOpenDialog(gimme());
            if (i == JFileChooser.APPROVE_OPTION) {
                generellPathField.setText(dirChooser.getSelectedFile().toString());
            }
        }
    }

    public class SaveAndExitListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            save();
            dispose();
        }
    }

    public class SaveListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            save();
        }
    }

    public void save() {
        try {
            Properties stripperSettings = new Properties();
            stripperSettings.setProperty("StreamripperPath", ripperPathField.getText());
            stripperSettings.setProperty("MP3Player", shoutcastPlayer.getText());
            stripperSettings.setProperty("GenerellPath", generellPathField.getText());
            stripperSettings.setProperty("WebBrowserPath", webBrowserField.getText());
            stripperSettings.setProperty("FileBrowserPath", fileBrowserField.getText());
            stripperSettings.setProperty("lang", getLangFromGui()[0]);
            stripperSettings.setProperty("reg", getLangFromGui()[1]);
            stripperSettings.setProperty("done", "true");
            stripperSettings.setProperty("statusAction", String.valueOf(statusBox.getSelectedIndex()));
            stripperSettings.setProperty("nameAction", String.valueOf(nameBox.getSelectedIndex()));
            stripperSettings.setProperty("currentTrackAction", String.valueOf(currentTrackBox.getSelectedIndex()));
            stripperSettings.setProperty("showIcon", String.valueOf(activeTrayIcon.isSelected()));
            if (activeTrayIcon.isSelected()) stripperSettings.setProperty("windowAction", String.valueOf(windowActionBox.getSelectedIndex())); else stripperSettings.setProperty("windowAction", "1");
            stripperSettings.setProperty("showTextUnderIcons", String.valueOf(showTextCheckBox.isSelected()));
            Writer settingsWriter = new FileWriter(fileSettingsPath + "Settings");
            stripperSettings.store(settingsWriter, "Settings from Stripper");
            settingsWriter.close();
        } catch (FileNotFoundException e) {
            System.err.println("Can't find " + fileSettingsPath + "Settings");
        } catch (IOException e) {
            System.err.println("I/O failed.");
        }
    }

    public String[] getLangFromGui() {
        int i = 0;
        String[] lang = { "en", "" };
        i = langMenu.getSelectedIndex();
        if (i == 0) {
            lang[0] = "en";
            lang[1] = "";
        }
        if (i == 1) {
            lang[0] = "de";
            lang[1] = "DE";
        }
        return lang;
    }

    public void fillWithFoundPrograms() {
        String[][] programms = new Control_TestStartFirst().searchPrograms();
        ripperPathField.setText(programms[3][0]);
        shoutcastPlayer.setText(programms[2][0]);
        webBrowserField.setText(programms[1][0]);
        fileBrowserField.setText(programms[0][0]);
    }

    public boolean getPathFromOptionFile() {
        try {
            Reader settingsReader = new FileReader(fileSettingsPath + "Settings");
            stripperSettings.load(settingsReader);
            String configTest = stripperSettings.getProperty("done", "");
            if (!configTest.equals("true")) fillWithFoundPrograms(); else {
                ripperPathField.setText(stripperSettings.getProperty("StreamripperPath"));
                shoutcastPlayer.setText(stripperSettings.getProperty("MP3Player"));
                webBrowserField.setText(stripperSettings.getProperty("WebBrowserPath"));
                fileBrowserField.setText(stripperSettings.getProperty("FileBrowserPath"));
                generellPathField.setText(stripperSettings.getProperty("GenerellPath"));
                statusBox.setSelectedIndex(Integer.parseInt(stripperSettings.getProperty("statusAction")));
                nameBox.setSelectedIndex(Integer.parseInt(stripperSettings.getProperty("nameAction")));
                currentTrackBox.setSelectedIndex(Integer.parseInt(stripperSettings.getProperty("currentTrackAction")));
                windowActionBox.setSelectedIndex(Integer.parseInt(stripperSettings.getProperty("windowAction")));
                if (stripperSettings.getProperty("showIcon").equals("true")) activeTrayIcon.setSelected(true); else activeTrayIcon.setSelected(false);
                if (stripperSettings.getProperty("lang", "en").equals("en")) langMenu.setSelectedIndex(0);
                if (stripperSettings.getProperty("lang", "").equals("de")) langMenu.setSelectedIndex(1);
                if (stripperSettings.getProperty("showTextUnderIcons") != null) {
                    if (stripperSettings.getProperty("showTextUnderIcons").equals("true")) showTextCheckBox.setSelected(true); else showTextCheckBox.setSelected(false);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Can't find " + fileSettingsPath + "Settings");
        } catch (IOException e) {
            System.err.println("I/O failed.");
        }
        return true;
    }

    public void repaintCommon() {
        if (activeTrayIcon.isSelected()) {
            windowActionBox.setEnabled(true);
            windowClosing.setEnabled(true);
        } else {
            windowActionBox.setEnabled(false);
            windowClosing.setEnabled(false);
        }
    }

    public class ExitListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }

    public class ChangeTrayFields implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            repaintCommon();
        }
    }
}

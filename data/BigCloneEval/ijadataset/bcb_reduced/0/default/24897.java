import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;
import javax.swing.filechooser.*;
import java.net.*;

public class scribeGui extends JFrame implements ActionListener, MouseListener {

    /************
* Variables *
************/
    JFrame frame;

    scribeBinder lincoln = new scribeBinder();

    JList topicList;

    JTextArea textArea;

    JToolBar toolTime;

    String temp = null;

    String[] settings = { "1", "0", "Example user", "Example password", "example.server.com", "/example/directory/" };

    JScrollPane[] scrollPane = { new JScrollPane(), new JScrollPane() };

    JSplitPane splitPane;

    int currentTopic = 0;

    String[] filename = { null, null };

    double version = 0.4;

    JLabel status = new JLabel("Currently: No crutch. | Topics: 0");

    JPanel[] panel = { new JPanel(), new JPanel() };

    JMenuBar menuBar = new JMenuBar();

    JMenu[] menu = { new JMenu("Crutch"), new JMenu("Topics"), new JMenu("Settings"), new JMenu("Help me!"), new JMenu("Export to") };

    JMenuItem[] menuItem = { new JMenuItem("Create"), new JMenuItem("Restore"), new JMenuItem("Text"), new JMenuItem("Html"), new JMenuItem("Wave"), new JMenuItem("Mp3"), new JMenuItem("Save"), new JMenuItem("Upload"), new JMenuItem("Name"), new JMenuItem("Quit Program"), new JMenuItem("FTP Account Details"), new JMenuItem("Check For Updates!"), new JMenuItem("How Do I Use This?"), new JMenuItem("About"), new JMenuItem("Sort Alphabetically"), new JMenuItem("Clear Topics"), new JMenuItem("Export List"), new JMenuItem("Import List"), new JMenuItem("Rename Selected"), new JMenuItem("Delete Selected"), new JMenuItem("Add Topic(s)") };

    JRadioButtonMenuItem[] specialMenuItem = { new JRadioButtonMenuItem("Toggle Divider Arrows"), new JRadioButtonMenuItem("Remind Me About Updates") };

    /*******************************
                * Method for updating the list *
                *******************************/
    public void updateList() {
        topicList.setListData(lincoln.getList());
    }

    /****************
		* Update things *
 		****************/
    public void boogie() {
        updateList();
        status.setText("Currently: " + lincoln.getTitle() + " | Topics: " + lincoln.getSize() + " | Current Topic: " + lincoln.readNoteTitle(currentTopic));
        setTitle("Scribe, a free crutch editor. - " + lincoln.getTitle());
        if (lincoln.getSize() > 0) {
            textArea.setEditable(true);
        } else if (lincoln.getSize() == 0) {
            textArea.setEditable(false);
        }
        if (filename[0] != null) {
            tempSaveTopic(currentTopic);
        }
    }

    /*******************************
                * Method for adding new topics *
                *******************************/
    public void addNewTopic() {
        temp = JOptionPane.showInputDialog(null, "Enter as many topics as you wish, separated by colons.", "Enter topic(s).", JOptionPane.QUESTION_MESSAGE);
        if (temp.contains(":")) {
            String[] tempTopicItems = temp.split(":");
            for (int k = 0; tempTopicItems[k] != null; k++) {
                lincoln.newNote(tempTopicItems[k], "".toCharArray());
            }
        } else if (temp.contains(":") == false) {
            lincoln.newNote(temp, "".toCharArray());
        }
        boogie();
    }

    /***************************************
                * Method for temporarily saving topics *
                ***************************************/
    public void tempSaveTopic(int topicToSave) {
        lincoln.setNoteBody(topicToSave, textArea.getText().toCharArray());
    }

    /************************
		* The About popup thing *
		************************/
    public void showAbout() {
        JOptionPane.showMessageDialog(null, "This is Scribe, a free crutch editor.\n" + "You are using version " + version + ".\n\n" + "Created by Tom Arnold.", "About Scribe...", JOptionPane.INFORMATION_MESSAGE);
    }

    /********************
 		* Check for updates *
	        ********************/
    public int checkUpdate(String server) {
        double tempVersion = -5;
        int tempReturn = 0;
        URL masterServer = null;
        try {
            masterServer = new URL(server + "update.txt");
        } catch (MalformedURLException error) {
            tempReturn = 3;
        }
        try {
            BufferedReader read = new BufferedReader(new InputStreamReader(masterServer.openStream()));
            tempVersion = Double.parseDouble(read.readLine());
            read.close();
        } catch (IOException error) {
            tempReturn = 3;
        }
        if (tempVersion != -5 && tempVersion > version) {
            tempReturn = 1;
        } else if (tempVersion != -5 && tempVersion <= version) {
            tempReturn = 0;
        }
        return tempReturn;
    }

    /**********************
		* Check update-dialog *
	        **********************/
    public void checkUpdateDialog(String server, int manual) {
        int update = checkUpdate(server);
        if (update == 1) {
            Object[] choices = { "Alright", "No thanks" };
            int choice = JOptionPane.showOptionDialog(null, "You are running version " + version + ", there is an update available.\n" + "Would you like to go to the Scribe website?\n" + "Note: You can disable this message from the settings menu.", "Update Available!", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, choices, choices[0]);
            if (choice == 0) {
                wizard.openBrowser(server + "osiris/doku.php/java/projects/scribe_project");
            }
        }
        if (manual == 1) {
            if (update == 0) {
                JOptionPane.showMessageDialog(null, "There are no updates available.", "No update available.", JOptionPane.INFORMATION_MESSAGE);
            } else if (update == 3) {
                JOptionPane.showMessageDialog(null, "There was a problem connecting to the server.\n" + "It may be down, or you may not currently have a working connection.\n" + "Please try again later.", "Problem connecting to server.", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /*****************************
                * Method for removing topics *
                *****************************/
    public void removeTopic() {
        lincoln.delNote(currentTopic);
        currentTopic = lincoln.getSize() - 1;
        textArea.setText(temp = new String(lincoln.readNoteBody(currentTopic)));
        boogie();
    }

    /*****************************
                * Method for renaming topics *
                *****************************/
    public void renameTopic() {
        temp = JOptionPane.showInputDialog(null, "Please enter a new title for \"" + lincoln.readNoteTitle(currentTopic) + "\".", "Enter new title.", JOptionPane.QUESTION_MESSAGE);
        if (temp == null) {
            temp = lincoln.readNoteTitle(currentTopic);
        }
        lincoln.setNoteTitle(currentTopic, temp);
        boogie();
    }

    /*******************
                * Set up listeners *
                *******************/
    public void setListeners() {
        for (int k = 0; k < menuItem.length; k++) {
            if (k < 2) {
                specialMenuItem[k].addActionListener(this);
            }
            menuItem[k].addActionListener(this);
        }
        topicList.addMouseListener(this);
        addMouseListener(this);
    }

    /*********************
                * Set up the toolbar *
                *********************/
    public void setToolbar() {
        toolTime = new JToolBar();
        toolTime.setFloatable(false);
        toolTime.add(status);
    }

    /******************
                * Set up the menu *
                ******************/
    public void setMenus() {
        for (int k = 0; k < 4; k++) {
            menuBar.add(menu[k]);
        }
        for (int k = 0; k < menuItem.length; k++) {
            if (k == 2) {
                menu[0].add(menu[4]);
                menu[4].add(menuItem[k]);
                menu[4].add(menuItem[k + 1]);
                if (wizard.getOperatingSystem().startsWith("Linux") && wizard.checkCommand("espeak -help") == 1) {
                    menu[4].add(menuItem[k + 2]);
                    if (wizard.checkCommand("lame") == 1) {
                        menu[4].add(menuItem[k + 3]);
                    }
                }
            }
            if (k < 2 | k > 5 && k < 10) {
                menu[0].add(menuItem[k]);
            }
            if (k > 13 && k < 21 && k != 15) {
                menu[1].add(menuItem[k]);
            }
            if (k == 9) {
                menu[2].add(specialMenuItem[0]);
                menu[2].add(specialMenuItem[1]);
                menu[2].add(menuItem[k + 1]);
            }
            if (k > 10 && k < 14) {
                menu[3].add(menuItem[k]);
            }
        }
    }

    /******************
                * Set up the list *
                ******************/
    public void setList() {
        topicList = new JList();
        topicList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        topicList.setLayoutOrientation(JList.VERTICAL);
        topicList.setVisibleRowCount(-1);
        topicList.setSelectedIndex(0);
        scrollPane[0] = new JScrollPane(topicList);
        scrollPane[0].setPreferredSize(new Dimension(20, 20));
    }

    /***********************
                * Set up the text area *
                ***********************/
    public void setTextArea() {
        textArea = new JTextArea(5, 30);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        scrollPane[1] = new JScrollPane(textArea);
    }

    /*************************
		* When the program quits *
		*************************/
    public void stop() {
    }

    /************************
                * Set up the split pane *
                ************************/
    public void setSplitPane() {
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane[0], scrollPane[1]);
        if (settings[1].equals("0")) {
            splitPane.setOneTouchExpandable(false);
        } else {
            splitPane.setOneTouchExpandable(true);
        }
        splitPane.setDividerLocation(125);
    }

    /*********************
		* New crutch dialog! *
		*********************/
    public void newCrutch() {
        JFileChooser oracle = new JFileChooser();
        String[] vars = { ".crutch", ".CRUTCH", "Crutch" };
        oracle.addChoosableFileFilter(new scribeFilter(vars));
        oracle.setAcceptAllFileFilterUsed(false);
        if (oracle.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            filename[0] = oracle.getSelectedFile().getPath();
            filename[1] = oracle.getSelectedFile().getName();
            String tempName = JOptionPane.showInputDialog(null, "Enter a title for your crutch:", "Enter crutch title", JOptionPane.QUESTION_MESSAGE);
            if (tempName != null) {
                lincoln.setTitle(tempName);
            }
            if (filename[0] != null) {
                saveCrutch(filename[0]);
                lincoln.clean();
                textArea.setText("");
            }
        }
        boogie();
    }

    /**************************************************
                * What happens when you choose the Restore option *
                * in the menu *************************************
                **************/
    public void restoreDialog() {
        JFileChooser oracle = new JFileChooser();
        String[] vars = { ".crutch", ".CRUTCH", "Crutch" };
        oracle.addChoosableFileFilter(new scribeFilter(vars));
        oracle.setAcceptAllFileFilterUsed(false);
        if (oracle.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            filename[0] = oracle.getSelectedFile().getPath();
            filename[1] = oracle.getSelectedFile().getName();
            textArea.setEditable(true);
            lincoln.restoreBinder(filename[0]);
            textArea.setText(temp = new String(lincoln.readNoteBody(0)));
            topicList.setSelectedIndex(0);
            currentTopic = 0;
            boogie();
        }
    }

    /******************************************************
                * What happens when you choose the import list option *
                * in the menu *****************************************
                **************/
    public void importListDialog() {
        JFileChooser oracle = new JFileChooser();
        String[] vars = { ".list", ".LIST", "List" };
        oracle.addChoosableFileFilter(new scribeFilter(vars));
        oracle.setAcceptAllFileFilterUsed(false);
        if (oracle.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            temp = oracle.getSelectedFile().getPath();
            topicList.setListData(lincoln.importList(temp));
            topicList.setSelectedIndex(0);
            textArea.setText(temp = new String(lincoln.readNoteBody(0)));
        }
        boogie();
    }

    /******************************************************
                * What happens when you choose the export list option *
                * in the menu *****************************************
                **************/
    public void exportListDialog() {
        JFileChooser oracle = new JFileChooser();
        String[] vars = { ".list", ".LIST", "List" };
        oracle.addChoosableFileFilter(new scribeFilter(vars));
        oracle.setAcceptAllFileFilterUsed(false);
        if (oracle.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            temp = oracle.getSelectedFile().getPath();
            lincoln.exportList(temp);
        }
    }

    /*************************************************
                * What happens when you choose the Export option *
                * in the menu ************************************
                **************/
    public void exportDialog(String command) {
        JFileChooser oracle = new JFileChooser();
        String[] vars;
        if (command.equals("Html")) {
            vars = new String[] { ".html", ".HTML", "Html" };
        } else if (command.equals("Text")) {
            vars = new String[] { ".txt", ".TXT", "Text" };
        } else if (command.equals("Wave")) {
            vars = new String[] { ".wav", ".WAV", "Wave" };
        } else {
            vars = new String[] { ".mp3", ".MP3", "Mp3" };
        }
        oracle.addChoosableFileFilter(new scribeFilter(vars));
        oracle.setAcceptAllFileFilterUsed(false);
        if (oracle.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            exportCrutch(command, oracle.getSelectedFile().getPath());
        }
        boogie();
    }

    /*********************
                * Set up the layouts *
                *********************/
    public void setLayouts() {
        panel[0].setLayout(new GridLayout(1, 2));
        panel[1].setLayout(new GridLayout(1, 6));
    }

    /********************
                * Set up the panels *
                ********************/
    public void setPanels() {
        panel[0].add(splitPane);
        panel[1].add(toolTime);
    }

    /***********************
                * A normal save method *
                ***********************/
    public void saveCrutch(String filename) {
        tempSaveTopic(currentTopic);
        if (lincoln.getSize() > 0) {
            lincoln.saveBinder(filename);
        }
    }

    /****************************
                * Export to whatever method *
                ****************************/
    public void exportCrutch(String command, String filename) {
        tempSaveTopic(currentTopic);
        if (command.equals("Html")) {
            lincoln.writeToHtml(filename);
        } else if (command.equals("Text")) {
            lincoln.writeToTxt(filename);
        } else if (command.equals("Wave")) {
            lincoln.writeToWav(filename);
        } else {
            lincoln.writeToMp3(filename);
        }
    }

    /***********************
		* Save settings method *
		***********************/
    public void saveSettings() {
        File file = new File("settings.dat");
        try {
            FileOutputStream fileOutput = new FileOutputStream(file);
            DataOutputStream dataOut = new DataOutputStream(fileOutput);
            for (int k = 0; k < 6; k++) {
                dataOut.writeUTF(settings[k]);
            }
            fileOutput.close();
        } catch (IOException error) {
            System.out.println("There was an error: " + error);
        }
    }

    /**************************
		* Restore settings method *
		**************************/
    public void restoreSettings() {
        File file = new File("settings.dat");
        try {
            FileInputStream fileInput = new FileInputStream(file);
            DataInputStream dataIn = new DataInputStream(fileInput);
            for (int k = 0; k < 6; k++) {
                settings[k] = dataIn.readUTF();
            }
        } catch (IOException error) {
            System.out.println("There was an error: " + error);
        }
        if (settings[0].equals("1")) {
            specialMenuItem[1].setSelected(true);
        }
        if (settings[1].equals("1")) {
            specialMenuItem[0].setSelected(true);
        }
    }

    /********************************
		* Change the name of the crutch *
		********************************/
    public void changeName() {
        lincoln.setTitle(JOptionPane.showInputDialog(null, "Please enter a title for your crutch.", "Enter crutch name", JOptionPane.QUESTION_MESSAGE));
        boogie();
    }

    /**************************
		* Would you like to quit? *
		**************************/
    public void youSure() {
        Object[] choices = { "Save and quit", "Just quit", "Nevermind" };
        int choice = JOptionPane.showOptionDialog(null, "Are you sure you want to quit?", "Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
        if (choice == 0) {
            if (filename[0] != null) {
                saveCrutch(filename[0]);
            }
            saveSettings();
            System.exit(1);
        } else if (choice == 1) {
            saveSettings();
            System.exit(1);
        }
    }

    /***********************
		 * FTP config dialog(s) *
		 ***********************/
    public void ftpSettings() {
        settings[2] = (String) JOptionPane.showInputDialog(null, "Please enter your username.", "FTP Username", JOptionPane.QUESTION_MESSAGE, null, null, settings[2]);
        settings[3] = (String) JOptionPane.showInputDialog(null, "Please enter your password.", "FTP Password", JOptionPane.QUESTION_MESSAGE, null, null, settings[3]);
        settings[4] = (String) JOptionPane.showInputDialog(null, "Please enter the address of the server.", "FTP Address", JOptionPane.QUESTION_MESSAGE, null, null, settings[4]);
        settings[5] = (String) JOptionPane.showInputDialog(null, "Please enter the directory you would like to use.", "FTP Directory", JOptionPane.QUESTION_MESSAGE, null, null, settings[5]);
        if (settings[2] == null) {
            settings[2] = "Example user";
        }
        if (settings[3] == null) {
            settings[3] = "Example password";
        }
        if (settings[4] == null) {
            settings[4] = "example.server.com";
        }
        if (settings[5] == null) {
            settings[5] = "/example/directory/";
        }
    }

    /*******************************
* Construct the program object *
*******************************/
    public scribeGui() {
        super("Scribe, a free crutch editor. - No crutch.");
        setSize(600, 400);
        setResizable(true);
        wizard.setTheme();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Container pane = getContentPane();
        BorderLayout layout = new BorderLayout();
        pane.setLayout(layout);
        restoreSettings();
        setToolbar();
        setList();
        setTextArea();
        setSplitPane();
        setMenus();
        setListeners();
        setLayouts();
        setPanels();
        pane.add(panel[0]);
        pane.add(panel[1], BorderLayout.SOUTH);
        setContentPane(pane);
        setJMenuBar(menuBar);
        setVisible(true);
        if (settings[0].equals("1")) {
            checkUpdateDialog("http://tom9729.arnolda.com/", 0);
        }
    }

    /******************
* Action listener *
******************/
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        Object source = event.getSource();
        if (filename[0] != null) {
            if (command.equals("Delete Selected")) {
                removeTopic();
            } else if (command.equals("Rename Selected")) {
                renameTopic();
            } else if (command.equals("Add Topic(s)")) {
                addNewTopic();
            } else if (command.equals("Name")) {
                changeName();
            } else if (command.equals("Import List")) {
                importListDialog();
            } else if (command.equals("Export List")) {
                exportListDialog();
            } else if (command.equals("Save")) {
                saveCrutch(filename[0]);
            } else if (command.equals("Sort Alphabetically")) {
                lincoln.sortBinder();
                currentTopic = 0;
                textArea.setText(temp = new String(lincoln.readNoteBody(currentTopic)));
                boogie();
            } else if (command.equals("Text") | command.equals("Html") | command.equals("Wave") | command.equals("Mp3")) {
                exportDialog(command);
            } else if (command.equals("Upload")) {
                lincoln.writeToFtp(settings[2], settings[3], settings[4], settings[5], filename[1]);
            }
        }
        if (filename[0] != null | filename[0] == null) {
            if (command.equals("Restore")) {
                restoreDialog();
            } else if (command.equals("Create")) {
                newCrutch();
            } else if (command.equals("Check For Updates!")) {
                checkUpdateDialog("http://tom9729.arnolda.com/", 1);
            } else if (command.equals("About")) {
                showAbout();
            } else if (command.equals("FTP Account Details")) {
                ftpSettings();
            } else if (command.equals("How Do I Use This?")) {
                scribeHelp cookie = new scribeHelp();
            } else if (command.equals("Remind Me About Updates")) {
                if (settings[0].equals("1")) {
                    settings[0] = "0";
                } else {
                    settings[0] = "1";
                }
                ;
            } else if (command.equals("Toggle Divider Arrows")) {
                if (settings[1].equals("0")) {
                    settings[1] = "1";
                    splitPane.setOneTouchExpandable(true);
                } else if (settings[1].equals("1")) {
                    settings[1] = "0";
                    splitPane.setOneTouchExpandable(false);
                }
            } else if (command.equals("Quit Program")) {
                if (filename[0] != null) {
                    youSure();
                } else {
                    saveSettings();
                    System.exit(1);
                }
            }
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        tempSaveTopic(currentTopic);
        textArea.setText(temp = new String(lincoln.readNoteBody(topicList.locationToIndex(e.getPoint()))));
        currentTopic = topicList.locationToIndex(e.getPoint());
        boogie();
    }

    public static void main(String[] arguments) {
        scribeGui monster = new scribeGui();
    }
}

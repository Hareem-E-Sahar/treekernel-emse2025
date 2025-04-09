package paraphrase;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import javax.swing.*;
import com.sun.java.swing.plaf.windows.WindowsBorders.ToolBarBorder;

/**
 * @author andres
 * 
 */
@SuppressWarnings("unused")
public class Main extends JFrame implements ActionListener {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    /**
	 * Program Version Number. Program version number in the following format:
	 * <li>x.0.0 where x is a major version number. Only changed where a major
	 * change in the program has occured.</li> <li>0.x.0 where x is a moderate
	 * change like, a new feature, major bug fixes</li> <li>0.0.x where x is a
	 * very minor change like something that was left before build.</li>
	 */
    static final String VersionNum = "0.2.0";

    private DictionarySearch Dic;

    JMenu File = null;

    JMenuItem FileOpen = null;

    JMenuItem FileSave = null;

    JMenuItem FileExit = null;

    JMenu Edit = null;

    JMenuItem EditCut = null;

    JMenuItem EditCopy = null;

    JMenuItem EditPaste = null;

    JMenuItem EditSelectAll = null;

    JMenuItem EditSettings = null;

    JMenu Paraphrase = null;

    JMenuItem ParaphraseParaphrase = null;

    JMenuItem ParaphraseGrammar = null;

    JMenuItem ParaphraseDocStats = null;

    JMenuItem ParaphraseDictionary = null;

    JMenu Help = null;

    JMenuItem HelpHelp = null;

    JMenuItem HelpReport = null;

    JMenuItem HelpAbout = null;

    JTextArea Text = null;

    JScrollPane Scroll = null;

    StatusBar statusbar;

    /**
	 * Get the Screen Dimensions to place the pop-ups in the center of the screen.
	 */
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

    /**
	 * Get the Operating System Currently in use
	 */
    String OS_in_use = System.getProperty("os.name");

    String OS_Version = System.getProperty("os.version");

    /**Used to calculate the program run start.
	 * Used to calculate the program run time. Records Start time at initialization.
	 */
    final long RunStart = System.currentTimeMillis();

    /**
	 * Used to calculate the program run time. This is a place holder for end time.
	 */
    long RunEnd = 0;

    public Main(String title) {
        super(title);
        final Container window = this.getContentPane();
        final Point location = new Point(100, 100);
        window.setLayout(new BorderLayout());
        Text = new JTextArea();
        Scroll = new JScrollPane(Text);
        window.add(Scroll, BorderLayout.CENTER);
        makeMenu();
        JToolBar toolBar = new JToolBar("Tolbar");
        toolBar.setFloatable(false);
        add(toolBar, BorderLayout.PAGE_START);
        JButton Open = makeToolbarButton("Open_Folder", "Open", "Open a File", "Open");
        toolBar.add(Open);
        JButton Save = makeToolbarButton("save-256x256", "Save", "Save the File.", "Save");
        toolBar.add(Save);
        JButton Para = makeToolbarButton("Para", "Paraphrase", "Open the paraphrase wizard.", "Paraphrase");
        toolBar.add(Para);
        JButton Search = makeToolbarButton("Search", "Dictionary Search", "Search for a word in our dictionary", "Dictionary Search");
        toolBar.add(Search, -1);
        statusbar = new StatusBar();
        getContentPane().add(statusbar, java.awt.BorderLayout.SOUTH);
        this.setSize(800, 500);
        this.setLocation(location);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final boolean status = false;
        final String command = e.getActionCommand();
        if (command.equals("Open")) {
            if (!status) JOptionPane.showMessageDialog(null, "Error opening file!", "File Open Error", JOptionPane.ERROR_MESSAGE);
        } else if (command.equals("Save")) {
            if (!status) JOptionPane.showMessageDialog(null, "IO error in saving file!!", "File Save Error", JOptionPane.ERROR_MESSAGE);
        } else if (command.equals("Exit")) dispose(); else if (command.equals("Copy")) Text.copy(); else if (command.equals("Paste")) Text.paste(); else if (command.equals("Cut")) Text.cut(); else if (command.equals("Select All")) Text.selectAll(); else if (command.equals("Paraphrasing")) showUnder(); else if (command.equals("Grammar")) showUnder(); else if (command.equals("Settings")) showUnder(); else if (command.equals("Dictionary Search")) {
            try {
                dicSearch();
            } catch (Exception a) {
                a.getStackTrace();
            }
        } else if (command.equals("Help")) showHelp(); else if (command.equals("Credits")) showCredits(); else if (command.equals("Document Stats")) showStats(); else if (command.equals("About")) showAbout(); else if (command.equalsIgnoreCase("Paraphrase")) paraphrase(); else if (command.equalsIgnoreCase("Report Bugs")) showBugs();
    }

    private void paraphrase() {
        String text = Text.getText();
        Paraphrase a = new Paraphrase(text);
        Thread t = new Thread(a);
        t.start();
        try {
            while (t.isAlive()) {
                statusbar.setMessage(a.getStatus());
                statusbar.updateUI();
                this.update(getGraphics());
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
        this.Text.setText(a.getText());
        statusbar.setMessage("Ready");
        String msg = "This Paraphrasing took " + a.getTime() + " seconds.\nThats " + a.getTime() / a.wordCount + " seconds per word.";
        JOptionPane.showMessageDialog(null, msg, "Finished!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showStats() {
        FleshKincaid a = new FleshKincaid(Text.getText() + "\n");
        System.out.println(a.getFleschKincaidGradelLevel());
        System.out.println(a.getFleschReadingEase());
        double Grade = a.getFleschKincaidGradelLevel();
        double Read = a.getFleschReadingEase();
        String result;
        String g = "Flesch-Kincaid Grade Level: " + Grade;
        int G = g.indexOf(".");
        g = g + "  ";
        g = g.substring(0, G + 3);
        String r = "\nFlesch-Kincaid Reading Ease: " + Read;
        int R = g.indexOf(".");
        r = r + "  ";
        r = r.substring(0, R + 6);
        result = g + r;
        JOptionPane.showMessageDialog(null, result, "Stats", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        String msg = "Automatic Paraphraser v." + VersionNum + " Alpha Release\nUse at your own risk...\nCreated By:  Andres Ruiz\n                       Gustavo Bravo\n                       Julio Julia";
        JOptionPane.showMessageDialog(null, msg, "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showCredits() {
        String Credits = "Credits:\n  Coders:\n     Andres Ruiz\n     Gustavo Bravo\n     Julio Julia\n   Code Borrowed:\n     Jack Frink(Flesh-Kincaid Algorithm)";
        JOptionPane.showMessageDialog(null, Credits, "Credits", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showHelp() {
        String Help = "";
        String UserName = System.getProperty("user.name");
        Help += "Hello, " + UserName + ", and welcome to Paraphrase!\n";
        Help += "Paraphrase was created in such way that it's very easy to use.\nHeres a list of features for Paraphrase:\n";
        Help += "Paraphrase!\n";
        Help += "The single most important feature of the whole program.\n It allows you, the user, to paraphrase any\ntext you desire.\n";
        Help += "This feature can be accessed from the toolbar or\n from the Paraphrase -> Paraphrase Menu\n";
        Help += "Documents Stats\n";
        Help += "Calculates the Flesch-Kincaid Reading Ease and Grade Level.\nUsed as an informational tool,\n but used mostly internally, just there for extra help.\n";
        JOptionPane.showMessageDialog(null, Help, "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showUnder() {
        String msg = "This section is still under construction.\nPlease try again when a new version is released.";
        JOptionPane.showMessageDialog(null, msg, "Warning", JOptionPane.ERROR_MESSAGE);
    }

    private void showBugs() {
        String msg = "If you find any bugs while usng this program, please help us\n by submitting any bugs by pressing the report button bellow";
        int Dialog = JOptionPane.showConfirmDialog(null, "Help us by submiting bug reports.\nPress yes to open a mail client to send us your bugs", "Bug Reports", JOptionPane.YES_NO_OPTION);
        if (Dialog == 0 && Desktop.isDesktopSupported()) {
            Desktop a = Desktop.getDesktop();
            try {
                URI email = new URI("mailto", "bug.lucerna@gmail.com", null);
                a.mail(email);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Is Desktop Supported: " + Desktop.isDesktopSupported());
                JOptionPane.showMessageDialog(null, "Error, please mail to bug.lucerna@gmail.com", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (Dialog == 0 && !Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(null, "Platform not supported", "Error", JOptionPane.ERROR_MESSAGE);
        } else System.out.println("Canceled");
    }

    @Override
    public void dispose() {
        final int Dialog = JOptionPane.showConfirmDialog(null, "You will lose any unsaved work.\nQuit?", "Quit?", JOptionPane.YES_NO_OPTION);
        RunEnd = System.currentTimeMillis();
        long RunTime = (RunEnd - RunStart) / 100;
        System.out.println("\nRunning in: " + OS_in_use + " " + OS_Version + "\nRan for " + RunTime + " seconds");
        if (Dialog == 0) super.dispose();
    }

    /**
	 * Create the Menu to appear in the window
	 */
    private void makeMenu() {
        File = new JMenu("File");
        File.add(FileOpen = makeMenuItem("Open"));
        File.add(FileSave = makeMenuItem("Save"));
        File.add(FileExit = makeMenuItem("Exit"));
        Edit = new JMenu("Edit");
        Edit.add(EditCut = makeMenuItem("Cut"));
        Edit.add(EditCopy = makeMenuItem("Copy"));
        Edit.add(EditPaste = makeMenuItem("Paste"));
        Edit.add(EditSelectAll = makeMenuItem("Select All"));
        Edit.add(EditSettings = makeMenuItem("Settings"));
        Paraphrase = new JMenu("Paraphrase");
        Paraphrase.add(ParaphraseParaphrase = makeMenuItem("Paraphrase"));
        Paraphrase.add(ParaphraseGrammar = makeMenuItem("Grammar"));
        Paraphrase.add(ParaphraseDocStats = makeMenuItem("Document Stats"));
        Paraphrase.add(ParaphraseDictionary = makeMenuItem("Dictionary Search"));
        Help = new JMenu("Help");
        Help.add(HelpHelp = makeMenuItem("Help"));
        Help.add(HelpReport = makeMenuItem("Report Bugs"));
        Help.add(HelpAbout = makeMenuItem("About"));
        final JMenuBar MenuBar = new JMenuBar();
        MenuBar.add(File);
        MenuBar.add(Edit);
        MenuBar.add(Paraphrase);
        MenuBar.add(Help);
        this.setJMenuBar(MenuBar);
    }

    /**
	 * Makes a JMenuItem and then registers this object as a listener to it.
	 **/
    private JMenuItem makeMenuItem(final String name) {
        final JMenuItem m = new JMenuItem(name);
        m.addActionListener(this);
        return m;
    }

    /**
	 * Makes a JButton with the selected parameters
	 */
    private JButton makeToolbarButton(final String imageName, final String actionCommand, final String toolTipText, final String altText) {
        final String imgLocation = "/images/" + imageName + ".png";
        final URL imageURL = ToolBarBorder.class.getResource(imgLocation);
        final JButton button = new JButton();
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

    @SuppressWarnings("deprecation")
    public void dicSearch() {
        String dic = JOptionPane.showInputDialog(null, "Please type the word you wish the search");
        Word a = new Word();
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        HTMLget s = new HTMLget();
        s.setWord(dic);
        Thread t = new Thread(s);
        t.run();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        s.printHTML();
        String html = s.get_HTML();
        t.stop();
        filterHTML HTML = new filterHTML(html, dic);
        HTML.toWord();
        a = HTML.word;
        JOptionPane.showMessageDialog(null, a, "Results", JOptionPane.INFORMATION_MESSAGE);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
	 * @param args
	 */
    public static void main(final String[] args) {
        final String title = "Paraphrase!" + " v." + VersionNum;
        final Main f = new Main(title);
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        f.setVisible(true);
    }
}

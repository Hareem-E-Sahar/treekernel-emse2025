package coffeeviewer.gui.main;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Panel;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.Label;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Dimension;
import javax.swing.JOptionPane;
import javax.swing.JEditorPane;
import javax.swing.ImageIcon;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.JTable;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.border.TitledBorder;
import javax.swing.border.BevelBorder;
import java.awt.ComponentOrientation;
import java.awt.event.KeyEvent;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.TextField;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JMenuBar;
import coffeeviewer.gui.about.AboutWindow;
import coffeeviewer.gui.preference.PreferenceWindow;

public class MainGui extends JFrame implements MouseListener {

    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;

    private Panel panel = null;

    private JPanel jLoginInformation = null;

    private JButton bottonLogin = null;

    private JLabel labelFirstName = null;

    private JTextField textFirstName = null;

    private JLabel labelSecondName = null;

    private JTextField textSecondName = null;

    private Panel secondPanel = null;

    private JPanel imagenPanel = null;

    private JPanel noticiasPanel = null;

    private JLabel labelImagen = null;

    private JPanel gridstatusPanel = null;

    private JLabel gridstatusLabel = null;

    private JLabel currentimeLabel = null;

    private JLabel statusLabel = null;

    private JLabel onlinenowLabel = null;

    private JLabel loggedlast60daysLabel = null;

    private JLabel timeLabel = null;

    private JLabel logged60 = null;

    private JLabel numberonlineLabel = null;

    private Panel clientinformationPanel = null;

    private Label passwordLabel = null;

    private TextField textPassword = null;

    private Label clientinformationLabel = null;

    private Label channelLabel = null;

    private Label versionLabel = null;

    private Label channelviewerLabel = null;

    private Label empty = null;

    private Label versionviewerLabel = null;

    private JPanel coffeeviewernoticesPanel = null;

    private JLabel coffeeviewernewsLabel = null;

    private JLabel coffeeNews1Label = null;

    private JMenuBar mainJMenuBar = null;

    private JMenu preferencesMenu = null;

    private JMenuItem preferenceMenuItem = null;

    private JMenu helpMenu = null;

    private JMenuItem aboutCoffeeMenuItem = null;

    private static void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(uri);
            } catch (IOException e) {
            }
        } else {
        }
    }

    /**
	 * This is the default constructor
	 */
    public MainGui() {
        super();
        initialize();
    }

    /**
	 * This method initializes this
	 * 
	 * @return void
	 */
    private void initialize() {
        this.setSize(640, 480);
        this.setJMenuBar(getMainJMenuBar());
        this.setPreferredSize(new Dimension(640, 480));
        this.setMinimumSize(new Dimension(640, 480));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(getJContentPane());
        this.setTitle("CoffeeViewer");
        this.setVisible(true);
    }

    /**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getPanel(), BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
	 * This method initializes panel	
	 * 	
	 * @return java.awt.Panel	
	 */
    private Panel getPanel() {
        if (panel == null) {
            panel = new Panel();
            panel.setLayout(new BorderLayout());
            panel.add(getJLoginInformation(), BorderLayout.SOUTH);
            panel.add(getSecondPanel(), BorderLayout.CENTER);
        }
        return panel;
    }

    /**
	 * This method initializes jLoginInformation	
	 * 	
	 * @return javax.swing.JPanel	
	 */
    private JPanel getJLoginInformation() {
        if (jLoginInformation == null) {
            passwordLabel = new Label();
            passwordLabel.setText("Password:");
            passwordLabel.setFont(new Font("Dialog", Font.BOLD, 12));
            GridLayout gridLayout2 = new GridLayout();
            gridLayout2.setRows(1);
            gridLayout2.setColumns(7);
            labelSecondName = new JLabel();
            labelSecondName.setText("Last Name:");
            labelFirstName = new JLabel();
            labelFirstName.setText("First Name:");
            jLoginInformation = new JPanel();
            jLoginInformation.setLayout(gridLayout2);
            jLoginInformation.add(labelFirstName, null);
            jLoginInformation.add(getTextFirstName(), null);
            jLoginInformation.add(labelSecondName, null);
            jLoginInformation.add(getTextSecondName(), null);
            jLoginInformation.add(passwordLabel, null);
            jLoginInformation.add(getTextPassword(), null);
            jLoginInformation.add(getBottonLogin(), null);
        }
        return jLoginInformation;
    }

    /**
	 * This method initializes bottonLogin	
	 * 	
	 * @return javax.swing.JButton	
	 */
    private JButton getBottonLogin() {
        if (bottonLogin == null) {
            bottonLogin = new JButton();
            bottonLogin.setText("Login");
        }
        return bottonLogin;
    }

    /**
	 * This method initializes textFirstName	
	 * 	
	 * @return javax.swing.JTextField	
	 */
    private JTextField getTextFirstName() {
        if (textFirstName == null) {
            textFirstName = new JTextField();
        }
        return textFirstName;
    }

    /**
	 * This method initializes textSecondName	
	 * 	
	 * @return javax.swing.JTextField	
	 */
    private JTextField getTextSecondName() {
        if (textSecondName == null) {
            textSecondName = new JTextField();
        }
        return textSecondName;
    }

    /**
	 * This method initializes secondPanel	
	 * 	
	 * @return java.awt.Panel	
	 */
    private Panel getSecondPanel() {
        if (secondPanel == null) {
            secondPanel = new Panel();
            secondPanel.setLayout(new BorderLayout());
            secondPanel.add(getImagenPanel(), BorderLayout.WEST);
            secondPanel.add(getNoticiasPanel(), BorderLayout.CENTER);
        }
        return secondPanel;
    }

    /**
	 * This method initializes imagenPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
    private JPanel getImagenPanel() {
        if (imagenPanel == null) {
            labelImagen = new JLabel();
            labelImagen.setText("");
            labelImagen.setBackground(Color.white);
            labelImagen.setIcon(new ImageIcon(getClass().getResource("/beso2.jpg")));
            imagenPanel = new JPanel();
            imagenPanel.setLayout(new GridBagLayout());
            imagenPanel.setBackground(Color.white);
            imagenPanel.add(labelImagen, new GridBagConstraints());
        }
        return imagenPanel;
    }

    /**
	 * This method initializes noticiasPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
    private JPanel getNoticiasPanel() {
        if (noticiasPanel == null) {
            GridLayout gridLayout = new GridLayout();
            gridLayout.setRows(5);
            gridstatusLabel = new JLabel();
            gridstatusLabel.setText("Grid Status");
            noticiasPanel = new JPanel();
            noticiasPanel.setLayout(gridLayout);
            noticiasPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            noticiasPanel.setEnabled(false);
            noticiasPanel.add(getGridstatusPanel(), null);
            noticiasPanel.add(getClientinformationPanel(), null);
            noticiasPanel.add(getCoffeeviewernoticesPanel(), null);
        }
        return noticiasPanel;
    }

    /**
	 * This method initializes gridstatusPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
    private JPanel getGridstatusPanel() {
        if (gridstatusPanel == null) {
            numberonlineLabel = new JLabel();
            numberonlineLabel.setText("0");
            numberonlineLabel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            logged60 = new JLabel();
            logged60.setText("0");
            logged60.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            timeLabel = new JLabel();
            timeLabel.setText("0");
            timeLabel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            loggedlast60daysLabel = new JLabel();
            loggedlast60daysLabel.setText("Logged In Last 60 days:");
            onlinenowLabel = new JLabel();
            onlinenowLabel.setText("Online Now:");
            statusLabel = new JLabel();
            statusLabel.setText("offline");
            statusLabel.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
            statusLabel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            currentimeLabel = new JLabel();
            currentimeLabel.setText("Current Time :");
            GridLayout gridLayout1 = new GridLayout();
            gridLayout1.setRows(4);
            gridLayout1.setColumns(2);
            gridstatusPanel = new JPanel();
            gridstatusPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            gridstatusPanel.setLayout(gridLayout1);
            gridstatusPanel.add(gridstatusLabel, null);
            gridstatusPanel.add(statusLabel, null);
            gridstatusPanel.add(currentimeLabel, null);
            gridstatusPanel.add(timeLabel, null);
            gridstatusPanel.add(loggedlast60daysLabel, null);
            gridstatusPanel.add(logged60, null);
            gridstatusPanel.add(onlinenowLabel, null);
            gridstatusPanel.add(numberonlineLabel, null);
        }
        return gridstatusPanel;
    }

    /**
	 * This method initializes clientinformationPanel	
	 * 	
	 * @return java.awt.Panel	
	 */
    private Panel getClientinformationPanel() {
        if (clientinformationPanel == null) {
            versionviewerLabel = new Label();
            versionviewerLabel.setText("0.1");
            empty = new Label();
            empty.setText("");
            channelviewerLabel = new Label();
            channelviewerLabel.setText("CoffeAlpha");
            versionLabel = new Label();
            versionLabel.setText("Version:");
            channelLabel = new Label();
            channelLabel.setText("Channel:");
            clientinformationLabel = new Label();
            clientinformationLabel.setText("Client Information");
            clientinformationLabel.setFont(new Font("Dialog", Font.BOLD, 12));
            GridLayout gridLayout3 = new GridLayout();
            gridLayout3.setRows(3);
            gridLayout3.setColumns(2);
            clientinformationPanel = new Panel();
            clientinformationPanel.setLayout(gridLayout3);
            clientinformationPanel.add(clientinformationLabel, null);
            clientinformationPanel.add(empty, null);
            clientinformationPanel.add(channelLabel, null);
            clientinformationPanel.add(channelviewerLabel, null);
            clientinformationPanel.add(versionLabel, null);
            clientinformationPanel.add(versionviewerLabel, null);
        }
        return clientinformationPanel;
    }

    /**
	 * This method initializes textPassword	
	 * 	
	 * @return java.awt.TextField	
	 */
    private TextField getTextPassword() {
        if (textPassword == null) {
            textPassword = new TextField();
        }
        return textPassword;
    }

    /**
	 * This method initializes coffeeviewernoticesPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
    private JPanel getCoffeeviewernoticesPanel() {
        if (coffeeviewernoticesPanel == null) {
            coffeeNews1Label = new JLabel();
            coffeeNews1Label.setText("<html><font color=\"#0000CF\"><u>http://darkchar.heliohost.org/</u></font></html>");
            coffeeNews1Label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            coffeeNews1Label.addMouseListener(this);
            coffeeviewernewsLabel = new JLabel();
            coffeeviewernewsLabel.setText("Coffee Viewer News:");
            GridLayout gridLayout4 = new GridLayout();
            gridLayout4.setRows(2);
            coffeeviewernoticesPanel = new JPanel();
            coffeeviewernoticesPanel.setLayout(gridLayout4);
            coffeeviewernoticesPanel.add(coffeeviewernewsLabel, null);
            coffeeviewernoticesPanel.add(coffeeNews1Label, null);
        }
        return coffeeviewernoticesPanel;
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
        URI a;
        try {
            a = new URI("http://darkchar.heliohost.org/");
            open(a);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }

    /**
	 * This method initializes mainJMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
    private JMenuBar getMainJMenuBar() {
        if (mainJMenuBar == null) {
            mainJMenuBar = new JMenuBar();
            mainJMenuBar.add(getPreferencesMenu());
            mainJMenuBar.add(getHelpMenu());
        }
        return mainJMenuBar;
    }

    /**
	 * This method initializes preferencesMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
    private JMenu getPreferencesMenu() {
        if (preferencesMenu == null) {
            preferencesMenu = new JMenu();
            preferencesMenu.setText("Preferences");
            preferencesMenu.add(getPreferenceMenuItem());
        }
        return preferencesMenu;
    }

    /**
	 * This method initializes preferenceMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
    private JMenuItem getPreferenceMenuItem() {
        if (preferenceMenuItem == null) {
            preferenceMenuItem = new JMenuItem();
            preferenceMenuItem.setText("preference");
            preferenceMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    PreferenceWindow pref;
                    pref = new PreferenceWindow();
                    pref.setVisible(true);
                }
            });
        }
        return preferenceMenuItem;
    }

    /**
	 * This method initializes helpMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
    private JMenu getHelpMenu() {
        if (helpMenu == null) {
            helpMenu = new JMenu();
            helpMenu.setText("Help");
            helpMenu.add(getAboutCoffeeMenuItem());
        }
        return helpMenu;
    }

    /**
	 * This method initializes aboutCoffeeMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
    private JMenuItem getAboutCoffeeMenuItem() {
        if (aboutCoffeeMenuItem == null) {
            aboutCoffeeMenuItem = new JMenuItem();
            aboutCoffeeMenuItem.setText("about coffee viewer");
            aboutCoffeeMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    AboutWindow about;
                    about = new AboutWindow();
                    about.setVisible(true);
                }
            });
        }
        return aboutCoffeeMenuItem;
    }
}

package org.projectopen.timesheet;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.TrayIcon.MessageType;
import java.net.URL;
import javax.swing.table.TableColumn;
import org.projectopen.browser.ObjectBrowserFrame;
import org.projectopen.rest.RESTClient;
import org.projectopen.treetable.JTreeTable;
import org.projectopen.util.Logger;

@SuppressWarnings("deprecation")
public class TrayIconStarter implements Logger {

    static TrayIcon trayIcon = null;

    public static JFrame treeFrame;

    public static JFrame configFrame;

    public static JFrame debugFrame;

    public static JFrame browserFrame;

    public static DebugPanel debugPanel = null;

    static int mouseX, mouseY;

    private static boolean useSystemLookAndFeel = true;

    /**
     * Provide the rest of the application with a way
     * to communicate error messages to the user.
     */
    public static void trayIconError(TrayIcon.MessageType errorType, String title, String message) {
        TrayIcon trayIcon = TrayIconStarter.getTrayIcon();
        trayIcon.displayMessage(title, message, errorType);
    }

    private static TrayIcon getTrayIcon() {
        return trayIcon;
    }

    /**
     * Launch a panel with the main time sheet logging functionality
     */
    private void launchHoursPanel() {
        System.out.println("SystemTrayTest.launchHoursPanel");
        if (treeFrame == null) {
            treeFrame = new JFrame("TreeTable");
            System.out.println("SystemTrayTest.launchHoursPanel: before new model");
            ProjectTreeTableModel model = new ProjectTreeTableModel();
            JTreeTable treeTable = new JTreeTable(model);
            treeTable.setDefaultRenderer(Float.class, new NumberRenderer());
            treeTable.setDefaultRenderer(Double.class, new NumberRenderer());
            treeTable.setDefaultRenderer(Number.class, new NumberRenderer());
            int colWidth[] = { 300, 50, 200 };
            TableColumn column = null;
            for (int i = 0; i < 3; i++) {
                column = treeTable.getColumnModel().getColumn(i);
                column.setPreferredWidth(colWidth[i]);
            }
            column = treeTable.getColumnModel().getColumn(1);
            treeFrame.getContentPane().add(new JScrollPane(treeTable));
            treeFrame.pack();
            Dimension dim = treeFrame.getSize();
            treeFrame.setLocation(mouseX - dim.width, mouseY - dim.height);
            treeFrame.show();
        }
        treeFrame.setVisible(true);
    }

    ;

    /**
     * Launch a panel allowing the user to change 
     * server/email/password parameters.
     */
    private void launchConfigPanel(boolean visible) {
        if (configFrame == null) {
            configFrame = new JFrame("Configuration");
            configFrame.add(new ConfigPanel(this));
            configFrame.pack();
            Dimension dim = configFrame.getSize();
            configFrame.setLocation(mouseX - dim.width, mouseY - dim.height);
        }
        configFrame.setVisible(visible);
    }

    ;

    /**
     * Launch a panel allowing the user to browse 
     * objects of a particular type
     */
    private void launchBrowserPanel(boolean visible) {
        if (browserFrame == null) {
            browserFrame = new JFrame("Browser Frame");
            browserFrame.setContentPane(new ObjectBrowserFrame());
            browserFrame.pack();
            Dimension dim = browserFrame.getSize();
            browserFrame.setLocation(mouseX - dim.width, mouseY - dim.height);
            browserFrame.show();
        }
        browserFrame.setVisible(true);
    }

    ;

    /**
     * Launch a panel that shows debugging information.
     */
    private void launchDebugPanel(boolean visible) {
        if (debugFrame == null) {
            debugFrame = new JFrame("Debug");
            debugFrame.add(debugPanel);
            debugFrame.pack();
            Dimension dim = debugFrame.getSize();
            debugFrame.setLocation(mouseX - dim.width, mouseY - dim.height);
        }
        debugFrame.setVisible(visible);
    }

    ;

    public TrayIconStarter() {
        if (SystemTray.isSupported()) {
            new RESTClient(this);
            debugPanel = new DebugPanel();
            this.logMessage(Logger.INFO, "Application", "Started", "");
            MouseListener mouseListener = new MouseListener() {

                public void mouseClicked(MouseEvent e) {
                    mouseX = e.getX();
                    mouseY = e.getY();
                    switch(e.getButton()) {
                        case 1:
                            {
                                launchHoursPanel();
                            }
                    }
                }

                public void mouseEntered(MouseEvent e) {
                }

                public void mouseExited(MouseEvent e) {
                }

                public void mousePressed(MouseEvent e) {
                }

                public void mouseReleased(MouseEvent e) {
                }
            };
            ActionListener exitListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    System.out.println("Exiting...");
                    System.exit(0);
                }
            };
            ActionListener configListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    launchConfigPanel(true);
                }
            };
            ActionListener browserListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    launchBrowserPanel(true);
                }
            };
            ActionListener debugListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    launchDebugPanel(true);
                }
            };
            ActionListener hoursListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    launchHoursPanel();
                }
            };
            ActionListener loginListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    RESTClient.defaultInstance().restBrowserLogin();
                }
            };
            PopupMenu popup = new PopupMenu();
            MenuItem hoursMenuItem = new MenuItem("Log Hours");
            hoursMenuItem.addActionListener(hoursListener);
            popup.add(hoursMenuItem);
            if (java.awt.Desktop.isDesktopSupported()) {
                MenuItem loginItem = new MenuItem("Login");
                loginItem.addActionListener(loginListener);
                popup.add(loginItem);
            }
            MenuItem browserItem = new MenuItem("Object Browser");
            browserItem.addActionListener(browserListener);
            popup.add(browserItem);
            MenuItem configItem = new MenuItem("Configuration");
            configItem.addActionListener(configListener);
            popup.add(configItem);
            MenuItem debugItem = new MenuItem("Debug");
            debugItem.addActionListener(debugListener);
            popup.add(debugItem);
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(exitListener);
            popup.add(exitItem);
            ActionListener actionListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    trayIcon.displayMessage("Action Event", "An Action Event Has Been Peformed!", TrayIcon.MessageType.INFO);
                    Image updatedImage = Toolkit.getDefaultToolkit().getImage("images/tray.gif");
                    trayIcon.setImage(updatedImage);
                    trayIcon.setImageAutoSize(true);
                }
            };
            String imagePath = "/org/projectopen/timesheet/po-icon.gif";
            URL fileLocation = getClass().getResource(imagePath);
            Image image = Toolkit.getDefaultToolkit().getImage(fileLocation);
            String imageStr = "null";
            if (image != null) {
                imageStr = image.getSource().toString();
            }
            this.logMessage(Logger.INFO, "Application", "Loading image from " + imagePath, imageStr);
            trayIcon = new TrayIcon(image, "Tray Demo", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(actionListener);
            trayIcon.addMouseListener(mouseListener);
            SystemTray tray = SystemTray.getSystemTray();
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }
        } else {
            System.err.println("System tray is currently not supported.");
        }
    }

    /**
     * Start the time sheet logging application.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (useSystemLookAndFeel) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("Couldn't use system look and feel.");
            }
        }
        new TrayIconStarter();
    }

    /**
	 * Accepts log messages from the RESTClient and
	 * displays them to the user as a TrayIcon message
	 * if they are of level ERROR or higher.
	 */
    public void logMessage(int level, String domain, String message, String details) {
        if (null != debugPanel) {
            debugPanel.logMessage(level, domain, message, details);
        }
        if (level >= Logger.ERROR) {
            MessageType errorType = TrayIcon.MessageType.ERROR;
            switch(level) {
                case Logger.DEBUG:
                    errorType = TrayIcon.MessageType.INFO;
                case Logger.INFO:
                    errorType = TrayIcon.MessageType.INFO;
                case Logger.WARNING:
                    errorType = TrayIcon.MessageType.WARNING;
                case Logger.ERROR:
                    errorType = TrayIcon.MessageType.ERROR;
                case Logger.FATAL:
                    errorType = TrayIcon.MessageType.ERROR;
            }
            TrayIcon trayIcon = TrayIconStarter.getTrayIcon();
            if (null != trayIcon) {
                trayIcon.displayMessage(domain, message, errorType);
            }
        }
    }
}

package org.xito.appmanager;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.lang.reflect.*;
import javax.swing.*;
import org.jdesktop.jdic.tray.*;
import org.xito.appmanager.store.ApplicationStore;
import org.xito.boot.*;
import org.xito.reflect.*;

/**
 * Main class of the Xito Application Manager
 *
 * @author Deane Richan
 */
public class Main {

    private static MainFrame mainFrame;

    private static JPopupMenu trayMenu;

    public static void initService(ServiceDesc service) {
    }

    public static void main(String args[]) {
        if (Boot.getCurrentOS() == Boot.WINDOWS_OS) {
            try {
                javax.swing.UIManager.setLookAndFeel("net.java.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e) {
            }
        }
        if (Boot.getCurrentOS() == Boot.WINDOWS_OS) {
            buildTrayIcon();
            mainFrame = new MainFrame(false);
        } else {
            mainFrame = new MainFrame(true);
        }
        mainFrame.show();
        mainFrame.loadApplications(true);
        if (Boot.getCurrentOS() == Boot.MAC_OS) {
            installAppleQuitHandler();
        }
    }

    /**
   * Hide the Desktop
   */
    private static void hideDesktop() {
        try {
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            Robot robot = new Robot();
            final BufferedImage screenImage = robot.createScreenCapture(new Rectangle(0, 0, size.width, size.height));
            final Color overlay = new Color(0.5f, 0.5f, 0.5f, 0.9f);
            Window w = new Window(new Frame()) {

                public void paint(Graphics g) {
                    g.drawImage(screenImage, 0, 0, null);
                    g.setColor(overlay);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            w.setSize(size);
            w.setVisible(true);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    /**
   * Build a Tray Icon for Windows
   */
    private static void buildTrayIcon() {
        TrayIcon trayIcon = new TrayIcon(new ImageIcon(Main.class.getResource("images/xito_16_grey.png")), Boot.getAppDisplayName());
        trayIcon.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                mainFrame.setVisible(true);
                if (mainFrame.getState() == Frame.ICONIFIED) {
                    mainFrame.setState(Frame.NORMAL);
                }
                mainFrame.toFront();
            }
        });
        trayMenu = new TrayPopupMenu();
        trayIcon.setPopupMenu(trayMenu);
        SystemTray systray = SystemTray.getDefaultSystemTray();
        systray.addTrayIcon(trayIcon);
    }

    /**
   * Install Quit Handler for Apple
   */
    private static void installAppleQuitHandler() {
        try {
            Reflection rkit = Reflection.getToolKit();
            Class AppClass = rkit.findClass("com.apple.eawt.Application");
            Object appleApp = rkit.callStatic(AppClass, "getApplication");
            Class AppListenerClass = rkit.findClass("com.apple.eawt.ApplicationListener");
            Object appleAppHandler = Proxy.newProxyInstance(Main.class.getClassLoader(), new Class[] { AppListenerClass }, new AppInvocationHandler());
            rkit.call(appleApp, "addApplicationListener", appleAppHandler, AppListenerClass);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    /**
   * Get the Popup Menu used with the System Tray
   */
    public static JPopupMenu getTrayMenu() {
        return trayMenu;
    }

    private static MainFrame getMainFrame() {
        return mainFrame;
    }

    /**
    * Class Used to Handle Apple Application Events
    */
    public static class AppInvocationHandler implements InvocationHandler {

        public Object invoke(Object proxy, Method method, Object[] args) {
            if (method.getName().equals("handleQuit")) {
                handleQuit(args[0]);
            }
            return null;
        }

        public void handleQuit(Object event) {
            mainFrame.promptForExit();
            try {
                Reflection rkit = Reflection.getToolKit();
                rkit.call(event, "setHandled", false);
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        }
    }

    /**
   * Menu for Tray Popup Menu
   */
    private static class TrayPopupMenu extends JPopupMenu implements ActionListener {

        private JMenuItem exitMI;

        private JMenuItem aboutMI;

        private JMenuItem showAppManagerMI;

        private JMenuItem showDesktopMI;

        public TrayPopupMenu() {
            super();
            init();
        }

        private void init() {
            showAppManagerMI = new JMenuItem("Show AppManager");
            showAppManagerMI.addActionListener(this);
            add(showAppManagerMI);
            aboutMI = new JMenuItem("About");
            aboutMI.addActionListener(this);
            add(aboutMI);
            add(new JSeparator());
            exitMI = new JMenuItem("Exit");
            exitMI.addActionListener(this);
            add(exitMI);
        }

        public void actionPerformed(ActionEvent evt) {
            if (evt.getSource() == exitMI) {
                mainFrame.promptForExit();
            }
            if (evt.getSource() == aboutMI) {
                org.xito.about.AboutService.showAboutWindow(mainFrame);
            }
            if (evt.getSource() == showAppManagerMI) {
                mainFrame.setVisible(true);
                mainFrame.toFront();
            }
        }
    }
}

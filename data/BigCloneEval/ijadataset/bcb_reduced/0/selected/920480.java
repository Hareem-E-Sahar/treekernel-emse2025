package xplanetconfigurator.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

public class XPlanetSystemTray {

    private MainFrame caller;

    private Logger logger;

    private SystemTray tray;

    private Desktop desktop;

    TrayIcon trayIcon;

    private CheckboxMenuItem checkboxMenuItemRun;

    public XPlanetSystemTray(MainFrame caller) {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.caller = caller;
        if (!SystemTray.isSupported()) {
            this.logger.config("System Tray is not supported");
            return;
        }
        PopupMenu popup = new PopupMenu();
        this.trayIcon = new TrayIcon(createImage("/xplanetconfigurator/gui/resources/img/mgs.jpg", "tray icon"));
        this.trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("XPlanet");
        this.tray = SystemTray.getSystemTray();
        MenuItem menuItemShow = new MenuItem("Show Window...");
        this.checkboxMenuItemRun = new CheckboxMenuItem("Run XPlanet");
        MenuItem menuItemExit = new MenuItem("Exit");
        MenuItem menuItemWebsite = null;
        MenuItem menuItemFeedback = null;
        popup.add(menuItemShow);
        popup.add(this.checkboxMenuItemRun);
        popup.addSeparator();
        if (!Desktop.isDesktopSupported()) {
            this.logger.config("Desktop is not supported");
            return;
        } else {
            this.desktop = Desktop.getDesktop();
            menuItemWebsite = new MenuItem("Website...");
            menuItemFeedback = new MenuItem("Feedback...");
            popup.add(menuItemWebsite);
            popup.add(menuItemFeedback);
            popup.addSeparator();
            menuItemWebsite.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    openWebsite();
                }
            });
            menuItemFeedback.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    openFeedback();
                }
            });
        }
        popup.add(menuItemExit);
        trayIcon.setPopupMenu(popup);
        try {
            this.tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return;
        }
        trayIcon.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                showGUI();
            }
        });
        menuItemShow.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                showGUI();
            }
        });
        this.checkboxMenuItemRun.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                int cb1Id = e.getStateChange();
                if (cb1Id == ItemEvent.SELECTED) {
                    setXPlanetRunning(true);
                } else {
                    setXPlanetRunning(false);
                }
            }
        });
        menuItemExit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                cleanUpCaller();
            }
        });
    }

    public void cleanUp() {
        if (this.tray != null) {
            tray.remove(trayIcon);
        }
    }

    private void cleanUpCaller() {
        this.caller.cleanUp();
        System.exit(0);
    }

    private void showGUI() {
        this.caller.setVisible(true);
        this.caller.setState(Frame.NORMAL);
    }

    private void setXPlanetRunning(boolean run) {
        if (run) {
            this.caller.startXPlanet();
            this.trayIcon.setImage(createImage("/xplanetconfigurator/gui/resources/img/mgs.jpg", "tray icon"));
            trayIcon.setToolTip("XPlanet running");
        } else {
            this.caller.stopXPlanet();
            this.trayIcon.setImage(createImage("/xplanetconfigurator/gui/resources/img/mgs_notrunning.jpg", "tray icon"));
            trayIcon.setToolTip("XPlanet stopped");
        }
    }

    public void setCheckBoxRun(boolean isSelected) {
        if (this.trayIcon != null) {
            this.checkboxMenuItemRun.setState(isSelected);
        }
    }

    private void openWebsite() {
        URI uri = null;
        try {
            uri = new URI("http://xplanetconfig.sourceforge.net/");
            this.desktop.browse(uri);
        } catch (IOException ioe) {
            this.logger.warning("Failed to open default browser to open the website http://xplanetconfig.sourceforge.net/" + ioe.toString());
        } catch (URISyntaxException use) {
            this.logger.warning("Wrong syntax for website http://xplanetconfig.sourceforge.net/" + use.toString());
        }
    }

    private void openFeedback() {
        URI uriMailTo = null;
        try {
            uriMailTo = new URI("mailto", "tomwie@users.sourceforge.net?SUBJECT=User feedback xplanet configurator", null);
            this.desktop.mail(uriMailTo);
        } catch (IOException ioe) {
            this.logger.warning("Failed to open default browser to mail with 'mailto:tomwie@users.sourceforge.net?SUBJECT=User feedback xplanet configurator'." + ioe.toString());
        } catch (URISyntaxException use) {
            this.logger.warning("Wrong syntay while open mail client with 'mailto:tomwie@users.sourceforge.net?SUBJECT=User feedback xplanet configurator'." + use.toString());
        }
    }

    protected static Image createImage(String path, String description) {
        URL imageURL = XPlanetSystemTray.class.getResource(path);
        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
}

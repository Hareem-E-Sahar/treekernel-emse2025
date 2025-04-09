package owlviewer.controller;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import javax.swing.JOptionPane;
import owlviewer.controller.net.Client;
import owlviewer.controller.owl.OWLParser;
import owlviewer.model.Model;
import owlviewer.view.IconManager;
import owlviewer.view.View;
import owlviewer.view.constants.E_Messages;
import owlviewer.view.constants.E_Windows;
import prefuse.visual.NodeItem;

/**
 *
 * @author Juanjo Vega
 */
public class Controller {

    public static Model model;

    public static View view;

    private static MenuItem exitItem;

    private static MenuItem hideItem;

    private static MenuItem showItem;

    private static PopupMenu popup;

    public void loadFile(final URI uri, boolean synchronously) {
        setSelectedNode(null);
        Thread thread = new Thread(new Runnable() {

            public void run() {
                View.setBusy(true);
                View.setProgress(0);
                View.setIndeterminate(true);
                Model.ontology = OWLParser.loadOWLFile(uri);
                View.setIndeterminate(false);
                View.setProgress(30);
                View.setFileName(uri.toString());
                View.setProgress(50);
                View.panelOntology.displayTree();
                View.setProgress(70);
                if (Model.orphans.size() > 0) {
                    View.setWindow(E_Windows.ORPHANS);
                }
                View.setProgress(100);
                View.setBusy(false);
            }
        });
        thread.start();
        if (synchronously) {
            try {
                while (thread.isAlive()) {
                    Thread.sleep(100);
                }
            } catch (Exception e) {
            }
        }
    }

    public static void setSelectedNode(NodeItem item) {
        Model.selectedNode = item;
        if (Model.selectedNode != null) {
            View.panelOntology.setEnabledNodeButtons(true);
        } else {
            View.panelOntology.setEnabledNodeButtons(false);
        }
    }

    public static void sendSelectedNode() {
        char msg[] = Controller.toCharArray(Model.getSelectedNodeID(), Model.getSelectedNodeLabel());
        try {
            Client.send(msg);
            JOptionPane.showMessageDialog(View.windowMain, E_Messages.TEXT_NODE_SENT(Model.getSelectedNodeLabel(), Model.getSelectedNodeID()), E_Messages.TITLE_NODE_SENT, JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public static boolean sendMail(String mailTo, String subject, String message) {
        boolean success = false;
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.MAIL)) {
                String mail = "mailto:" + mailTo + "?SUBJECT=" + subject + "&BODY=" + message;
                URI uriMailTo = null;
                try {
                    if (mailTo.length() > 0) {
                        uriMailTo = new URI(mail.replaceAll(" ", "%20"));
                        desktop.mail(uriMailTo);
                    } else {
                        desktop.mail();
                    }
                    success = true;
                } catch (Exception e) {
                    handleException(e);
                }
            }
        }
        return success;
    }

    public static void browse(URI uri) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(uri);
                } catch (Exception e) {
                    handleException(e);
                }
            }
        }
    }

    public static void handleException(Exception e) {
        Model.exception = e;
        e.printStackTrace();
        View.setWindow(E_Windows.EXCEPTION);
    }

    public static String getExceptionReport() {
        String stacktrace = "[null]";
        if (Model.exception != null) {
            StringWriter sw = new StringWriter();
            Model.exception.printStackTrace(new PrintWriter(sw));
            stacktrace = Model.exception.getMessage() + ":\n" + sw.toString();
        }
        return stacktrace;
    }

    public static char[] toCharArray(String id, String label) {
        char msg[] = new char[id.length() + label.length() + 1];
        char arrayID[] = id.toCharArray();
        char arrayLabel[] = label.toCharArray();
        System.arraycopy(arrayID, 0, msg, 0, arrayID.length);
        msg[arrayID.length] = '\0';
        System.arraycopy(arrayLabel, 0, msg, arrayID.length + 1, arrayLabel.length);
        return msg;
    }

    public static void installTrayIcon() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            MouseListener mouseListener = new MouseListener() {

                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() > 1) {
                        if (Model.hidden) {
                            show();
                        } else {
                            hide();
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
                    exit();
                }
            };
            ActionListener hideListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    hide();
                }
            };
            ActionListener showListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    show();
                }
            };
            popup = new PopupMenu();
            exitItem = new MenuItem(E_Messages.OPTION_EXIT);
            hideItem = new MenuItem(E_Messages.OPTION_HIDE);
            showItem = new MenuItem(E_Messages.OPTION_SHOW);
            exitItem.addActionListener(exitListener);
            hideItem.addActionListener(hideListener);
            showItem.addActionListener(showListener);
            popup.add(exitItem);
            popup.add(hideItem);
            popup.add(showItem);
            showItem.setEnabled(false);
            IconManager.trayIcon = new TrayIcon(IconManager.ICON.getImage(), E_Messages.APPLICATION_NAME, popup);
            IconManager.trayIcon.setImageAutoSize(true);
            IconManager.trayIcon.addMouseListener(mouseListener);
            try {
                tray.add(IconManager.trayIcon);
            } catch (AWTException e) {
                handleException(e);
            }
        } else {
        }
    }

    private static void exit() {
        System.exit(0);
    }

    private static void hide() {
        if (hideItem.isEnabled()) {
            IconManager.trayIcon.displayMessage(E_Messages.APPLICATION_NAME, E_Messages.TEXT_HIDDEN, TrayIcon.MessageType.INFO);
            hideItem.setEnabled(false);
            showItem.setEnabled(true);
            View.hide();
            Model.hidden = true;
        }
    }

    private static void show() {
        if (showItem.isEnabled()) {
            showItem.setEnabled(false);
            hideItem.setEnabled(true);
            View.show();
            Model.hidden = false;
        }
    }
}

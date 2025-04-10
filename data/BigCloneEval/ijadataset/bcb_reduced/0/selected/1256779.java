package net.sf.jaer.util;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import net.sf.jaer.graphics.AEViewer;
import net.sf.jaer.graphics.AEViewerAboutDialog;

/** A frame with text area to show logging results in. Has buttons to copy to clipboard and to mail text to developers. */
public class LoggingWindow extends JFrame {

    private final JTextArea textArea = new JTextArea();

    /** Developer email addresses */
    public static final String DEVELOPER_EMAIL = "tobidelbruck@users.sourceforge.net,bernerr@users.sourceforge.net";

    public LoggingWindow(String title, final int width, final int height) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                addVersionInfo();
                setSize(width, height);
                JScrollPane pane = new JScrollPane(textArea);
                textArea.setEditable(false);
                getContentPane().add(pane, BorderLayout.CENTER);
                JButton copyBut = new JButton("Copy to clipboard");
                copyBut.setToolTipText("Copies text to clipboard");
                copyBut.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            clipboard.setContents(new StringSelection(textArea.getText()), null);
                        } catch (Exception ex) {
                            System.err.println("couldn't copy exception pane: " + ex.toString());
                        }
                    }
                });
                JButton mailBut = new JButton("Mail to developers");
                mailBut.setToolTipText("Opens your email client to mail this exception to the jAER core developers. Restricted to 2048 characters.");
                mailBut.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            mailToDevelopers();
                        } catch (Exception ex) {
                            System.err.println("couldn't copy exception pane: " + ex.toString());
                        }
                    }
                });
                JButton helpForumButton = new JButton("Open jAER help forum");
                helpForumButton.setToolTipText("Opens your browser to the jAER help forum on sourceforge.");
                helpForumButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            showInBrowser(AEViewer.HELP_URL_HELP_FORUM);
                        } catch (Exception ex) {
                            System.err.println("couldn't copy exception pane: " + ex.toString());
                        }
                    }
                });
                JPanel butPan = new JPanel();
                butPan.setLayout(new BoxLayout(butPan, BoxLayout.X_AXIS));
                butPan.add(new Box(BoxLayout.X_AXIS));
                butPan.add(copyBut);
                butPan.add(mailBut);
                butPan.add(helpForumButton);
                getContentPane().add(butPan, BorderLayout.SOUTH);
                setVisible(true);
            }
        });
    }

    private void showInBrowser(String url) {
        if (!Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(this, "No Desktop support, can't show help from " + url);
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Couldn't show " + url + "; caught " + ex);
        }
    }

    public void addLogInfo(final String data) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                textArea.append(data);
            }
        });
    }

    public void addVersionInfo() {
        Properties props = new Properties();
        ClassLoader cl = this.getClass().getClassLoader();
        addLogInfo("\nLoading version info from resource " + AEViewerAboutDialog.VERSION_FILE);
        URL versionURL = cl.getResource(AEViewerAboutDialog.VERSION_FILE);
        addLogInfo("\nVersion URL=" + versionURL + "\n");
        if (versionURL != null) {
            try {
                Object urlContents = versionURL.getContent();
                BufferedReader in = null;
                if (urlContents instanceof InputStream) {
                    props.load((InputStream) urlContents);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
            PrintWriter ps = new PrintWriter(baos);
            props.list(ps);
            ps.flush();
            try {
                addLogInfo("\n" + baos.toString("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                System.err.println("cannot encode version information in LoggingWindow.addVersionInfo: " + ex.toString());
            }
        } else {
            props.setProperty("version", "missing file " + AEViewerAboutDialog.VERSION_FILE + " in jAER.jar");
        }
    }

    void mailToDevelopers() {
        Desktop desktop = null;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.MAIL)) {
                String mailTo = DEVELOPER_EMAIL;
                URI uriMailTo = null;
                try {
                    if (mailTo.length() > 0) {
                        uriMailTo = new URI("mailto", mailTo + "?subject=jAER uncaught exception&body=" + textArea.getText(), null);
                        desktop.mail(uriMailTo);
                    } else {
                        desktop.mail();
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (URISyntaxException use) {
                    use.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Thread.UncaughtExceptionHandler handler = new LoggingThreadGroup("jAER UncaughtExceptionHandler");
        Thread.setDefaultUncaughtExceptionHandler(handler);
        throw new RuntimeException("test exception");
    }
}

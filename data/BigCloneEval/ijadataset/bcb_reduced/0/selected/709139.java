package net.sf.jaer.graphics;

import edu.stanford.ejalbert.BrowserLauncher;
import java.awt.Cursor;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * The About dialog.  It displays About information and latest SVN commit and build dates.
 * The version information file is updated by the project build.xml.
 *
 * @author tobi
 */
public class AEViewerAboutDialog extends javax.swing.JDialog {

    public static final String VERSION_FILE = "BUILDVERSION.txt";

    static Logger log = Logger.getLogger("About");

    /**
     * Creates new form AEViewerAboutDialog
     */
    public AEViewerAboutDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        Properties props = new Properties();
        String dateModified = null;
        ClassLoader cl = this.getClass().getClassLoader();
        log.info("Loading version info from resource " + VERSION_FILE);
        URL versionURL = cl.getResource(VERSION_FILE);
        log.info("Version URL=" + versionURL);
        if (versionURL != null) {
            try {
                Object urlContents = versionURL.getContent();
                BufferedReader in = null;
                if (urlContents instanceof InputStream) {
                    props.load((InputStream) urlContents);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(parent, e);
            }
        } else {
            props.setProperty("version", "missing file " + VERSION_FILE + " in jAER.jar");
        }
        Enumeration e = props.propertyNames();
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            if (o instanceof String) {
                String key = (String) o;
                String value = props.getProperty(key);
                versionLabel.setText(versionLabel.getText() + "<center>" + key + " = " + value + "</center>");
            }
        }
        versionLabel.setText(versionLabel.getText());
        pack();
    }

    private void initComponents() {
        aboutLabel = new javax.swing.JLabel();
        versionLabel = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        jaerProjectLinkLabel = new javax.swing.JLabel();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        aboutLabel.setFont(new java.awt.Font("Tahoma", 0, 24));
        aboutLabel.setText("<html> <center> <h1> jAER - Java tools for AER </h1> </center></html>");
        versionLabel.setText("<html>");
        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        jaerProjectLinkLabel.setFont(new java.awt.Font("Tahoma", 0, 18));
        jaerProjectLinkLabel.setText("<html> <em><a href=\"http://jaer.sourceforge.net\">jaer.sourceforge.net</a> </em></html>");
        jaerProjectLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jaerProjectLinkLabelMouseClicked(evt);
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jaerProjectLinkLabelMouseEntered(evt);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                jaerProjectLinkLabelMouseExited(evt);
            }
        });
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup().add(77, 77, 77).add(jaerProjectLinkLabel).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 225, Short.MAX_VALUE).add(okButton).addContainerGap()).add(layout.createSequentialGroup().addContainerGap().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(versionLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE).addContainerGap()).add(layout.createSequentialGroup().add(aboutLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE).add(167, 167, 167)))));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(aboutLabel).addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED).add(versionLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 107, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 49, Short.MAX_VALUE).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(okButton).add(jaerProjectLinkLabel)).addContainerGap()));
        pack();
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {
        setVisible(false);
        dispose();
    }

    private void jaerProjectLinkLabelMouseClicked(java.awt.event.MouseEvent evt) {
        try {
            showInBrowser(AEViewer.HELP_URL_USER_GUIDE);
            setCursor(Cursor.getDefaultCursor());
        } catch (Exception e) {
            log.warning(e.toString());
        }
    }

    private void showInBrowser(String url) {
        if (!Desktop.isDesktopSupported()) {
            log.warning("No Desktop support, can't show help from " + url);
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            log.warning("Couldn't show " + url + "; caught " + ex);
        }
    }

    private void jaerProjectLinkLabelMouseEntered(java.awt.event.MouseEvent evt) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void jaerProjectLinkLabelMouseExited(java.awt.event.MouseEvent evt) {
        setCursor(Cursor.getDefaultCursor());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new AEViewerAboutDialog(new javax.swing.JFrame(), true).setVisible(true);
    }

    private javax.swing.JLabel aboutLabel;

    private javax.swing.JLabel jaerProjectLinkLabel;

    private javax.swing.JButton okButton;

    private javax.swing.JLabel versionLabel;
}

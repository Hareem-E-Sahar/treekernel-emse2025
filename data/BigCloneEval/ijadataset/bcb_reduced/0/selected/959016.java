package solowiki;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author  Giuseppe Profiti
 */
public class AboutDialog extends javax.swing.JDialog {

    /** Creates new form iAboutDialog */
    public AboutDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    private void initComponents() {
        javax.swing.JLabel appTitleLabel = new javax.swing.JLabel();
        javax.swing.JLabel appDescLabel = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();
        javax.swing.JLabel versionLabel = new javax.swing.JLabel();
        javax.swing.JLabel vendorLabel = new javax.swing.JLabel();
        javax.swing.JLabel homepageLabel = new javax.swing.JLabel();
        javax.swing.JLabel licenseLabel = new javax.swing.JLabel();
        javax.swing.JLabel appLicenseLabel = new javax.swing.JLabel();
        javax.swing.JLabel appVersionLabel = new javax.swing.JLabel();
        javax.swing.JLabel appVendorLabel = new javax.swing.JLabel();
        javax.swing.JLabel appHomepageLabel = new javax.swing.JLabel();
        parserLabel = new javax.swing.JLabel();
        parserLabelText = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        parserlicenseLabel = new javax.swing.JLabel();
        javax.swing.JLabel parserLicenceText = new javax.swing.JLabel();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("solowiki/resources/SoloWikiAboutBox");
        setTitle(bundle.getString("title"));
        setLocationByPlatform(true);
        setResizable(false);
        appTitleLabel.setFont(appTitleLabel.getFont().deriveFont(appTitleLabel.getFont().getStyle() | java.awt.Font.BOLD, appTitleLabel.getFont().getSize() + 4));
        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("solowiki/resources/SoloWikiApp");
        appTitleLabel.setText(bundle1.getString("Application.name"));
        appDescLabel.setText(bundle.getString("appDescLabel.text"));
        closeButton.setText(bundle.getString("closeAboutBox.Action.text"));
        closeButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButton(evt);
            }
        });
        versionLabel.setFont(versionLabel.getFont().deriveFont(versionLabel.getFont().getStyle() | java.awt.Font.BOLD));
        versionLabel.setText(bundle.getString("versionLabel.text"));
        vendorLabel.setFont(vendorLabel.getFont().deriveFont(vendorLabel.getFont().getStyle() | java.awt.Font.BOLD));
        vendorLabel.setText(bundle.getString("vendorLabel.text"));
        homepageLabel.setFont(homepageLabel.getFont().deriveFont(homepageLabel.getFont().getStyle() | java.awt.Font.BOLD));
        homepageLabel.setText(bundle.getString("homepageLabel.text"));
        licenseLabel.setFont(licenseLabel.getFont().deriveFont(licenseLabel.getFont().getStyle() | java.awt.Font.BOLD));
        licenseLabel.setText(bundle.getString("licenseLabel.text"));
        appLicenseLabel.setText(bundle.getString("appLicenseLabel.text"));
        appLicenseLabel.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                openWebsite(evt);
            }
        });
        appVersionLabel.setText(bundle1.getString("Application.version"));
        appVendorLabel.setText(bundle1.getString("Application.vendor"));
        appHomepageLabel.setText(bundle1.getString("Application.homepage"));
        appHomepageLabel.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                openWebsite(evt);
            }
        });
        parserLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        parserLabel.setText(bundle.getString("parserlabel"));
        parserLabelText.setText(bundle.getString("parserlabel.text"));
        parserLabelText.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                openWebsite(evt);
            }
        });
        parserlicenseLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        parserlicenseLabel.setText(bundle.getString("licenseLabel.text"));
        parserLicenceText.setText(bundle.getString("parserLicenseLabel.text"));
        parserLicenceText.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                openWebsite(evt);
            }
        });
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(appTitleLabel).addComponent(appDescLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(versionLabel).addComponent(vendorLabel).addComponent(homepageLabel).addComponent(licenseLabel)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(appVersionLabel).addComponent(appVendorLabel).addComponent(appLicenseLabel).addComponent(appHomepageLabel)).addGap(16, 16, 16)))).addGroup(layout.createSequentialGroup().addGap(120, 120, 120).addComponent(closeButton)).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(parserLabel).addComponent(parserlicenseLabel)).addGap(23, 23, 23).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(parserLicenceText).addComponent(parserLabelText)))).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(appTitleLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(appDescLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(versionLabel).addComponent(appVersionLabel)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(vendorLabel).addComponent(appVendorLabel)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(homepageLabel).addComponent(appHomepageLabel)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(licenseLabel).addComponent(appLicenseLabel)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(parserLabel).addComponent(parserLabelText)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(parserlicenseLabel).addComponent(parserLicenceText)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(closeButton).addContainerGap()));
        pack();
    }

    private void closeButton(java.awt.event.ActionEvent evt) {
        setVisible(false);
    }

    private void openWebsite(java.awt.event.MouseEvent evt) {
        javax.swing.JLabel source = (javax.swing.JLabel) evt.getSource();
        String text = source.getText();
        int start = text.indexOf("href=\"") + 6;
        int end = text.indexOf('\"', start);
        openWebsite(text.substring(start, end));
    }

    /**
     * Starts the system default browser
     * @param url
     */
    private void openWebsite(String url) {
        try {
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(new URI(url));
        } catch (IOException ex) {
            Logger.getLogger(AboutDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(AboutDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                AboutDialog dialog = new AboutDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    private javax.swing.JButton closeButton;

    private javax.swing.JSeparator jSeparator1;

    private javax.swing.JLabel parserLabel;

    private javax.swing.JLabel parserLabelText;

    private javax.swing.JLabel parserlicenseLabel;
}

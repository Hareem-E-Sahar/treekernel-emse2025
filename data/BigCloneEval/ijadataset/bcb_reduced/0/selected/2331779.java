package swingextras.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.action.OpenBrowserAction;
import org.jdesktop.swingx.hyperlink.AbstractHyperlinkAction;
import org.jdesktop.swingx.painter.MattePainter;
import swingextras.action.ActionX;
import swingextras.action.ActionXData;
import swingextras.Common;
import swingextras.GuiUtils;
import swingextras.license.License;
import swingextras.icons.IconManager;
import swingextras.Library;

/**
 * A dialog that displays information about a program
 * @author  Joao Leal
 */
@SuppressWarnings("serial")
public class DialogAbout extends javax.swing.JDialog {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("swingextras/i18n/internationalization");

    /**
     * Holds value of property projectName.
     */
    private String projectName;

    /**
     * Holds value of property projectName.
     */
    private String projectVersion;

    /**
     * Holds value of property packageDescripton.
     */
    private String packageDescripton;

    /**
     * Holds value of property packageDescripton.
     */
    private String packageLicSmallDescripton;

    /**
     * Holds value of property libraries.
     */
    private Library[] libraries;

    /**
     * Holds value of property icon.
     */
    private ImageIcon imageAbout;

    /**
     * An URL to the Projects homepage
     */
    private URL projectHomepage;

    /** Creates new form DialogAbout */
    public DialogAbout(java.awt.Window parent) {
        super(parent, ModalityType.MODELESS);
        setIconImages(parent.getIconImages());
        initComponents();
        Color gradientStart = headerAbout.getMatteColor();
        Color gradientEnd = GuiUtils.interpolateColor(gradientStart, jXPanelAbout.getBackground(), 0.9);
        GradientPaint paint = new GradientPaint(0, 0, gradientEnd, 0, 1, gradientStart);
        MattePainter painter = new MattePainter(paint, true);
        jXPanelAbout.setBackgroundPainter(painter);
        setTitle(bundle.getString("About"));
        @SuppressWarnings("serial") ActionX actionClose = new ActionX(new ActionXData("close", Common.ACTIONBUNDLE)) {

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        GuiUtils.installEscapeActionDialog(this, actionClose);
        jButtonClose.setAction(actionClose);
        getRootPane().setDefaultButton(jButtonClose);
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        jLabelLibHead = new javax.swing.JLabel();
        jLabelNoLibs = new javax.swing.JLabel();
        jPanel = new javax.swing.JPanel();
        jTabbedPane = new javax.swing.JTabbedPane();
        jXPanelAbout = new org.jdesktop.swingx.JXPanel();
        prettyIcon = new swingextras.gui.PrettyIcon();
        jLabelDescription = new javax.swing.JLabel();
        jLabelSmallLicDescription = new javax.swing.JLabel();
        jPanelLicense = new javax.swing.JPanel();
        jScrollPaneProject = new javax.swing.JScrollPane();
        jTextAreaLicense = new javax.swing.JTextArea();
        jPanelLibs = new javax.swing.JPanel();
        jScrollPaneLibs = new javax.swing.JScrollPane();
        jPanelLibsList = new javax.swing.JPanel();
        jButtonClose = new javax.swing.JButton();
        headerAbout = new swingextras.Header();
        jLabelLibHead.setText(bundle.getString("The_following_libraries_and_their_dependencies_are_used_in_this_package"));
        jLabelNoLibs.setText(bundle.getString("No_libraries_defined"));
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());
        jPanel.setMinimumSize(new java.awt.Dimension(450, 350));
        jPanel.setPreferredSize(new java.awt.Dimension(550, 400));
        jPanel.setLayout(new java.awt.GridBagLayout());
        jXPanelAbout.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jXPanelAbout.add(prettyIcon, gridBagConstraints);
        jLabelDescription.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelDescription.setIconTextGap(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jXPanelAbout.add(jLabelDescription, gridBagConstraints);
        jLabelSmallLicDescription.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        jXPanelAbout.add(jLabelSmallLicDescription, gridBagConstraints);
        jTabbedPane.addTab(bundle.getString("About"), jXPanelAbout);
        jPanelLicense.setLayout(new java.awt.GridBagLayout());
        jTextAreaLicense.setEditable(false);
        jTextAreaLicense.setLineWrap(true);
        jTextAreaLicense.setRows(5);
        jTextAreaLicense.setWrapStyleWord(true);
        jScrollPaneProject.setViewportView(jTextAreaLicense);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelLicense.add(jScrollPaneProject, gridBagConstraints);
        jTabbedPane.addTab("License", jPanelLicense);
        jPanelLibs.setLayout(new java.awt.GridBagLayout());
        jPanelLibsList.setLayout(new java.awt.GridBagLayout());
        jScrollPaneLibs.setViewportView(jPanelLibsList);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelLibs.add(jScrollPaneLibs, gridBagConstraints);
        jTabbedPane.addTab(bundle.getString("Libraries"), jPanelLibs);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel.add(jTabbedPane, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel.add(jButtonClose, gridBagConstraints);
        headerAbout.setAnchor(java.awt.GridBagConstraints.CENTER);
        headerAbout.setSubTitle("version");
        headerAbout.setTitle(bundle.getString("About"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel.add(headerAbout, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel, gridBagConstraints);
        pack();
    }

    private swingextras.Header headerAbout;

    private javax.swing.JButton jButtonClose;

    private javax.swing.JLabel jLabelDescription;

    private javax.swing.JLabel jLabelLibHead;

    private javax.swing.JLabel jLabelNoLibs;

    private javax.swing.JLabel jLabelSmallLicDescription;

    private javax.swing.JPanel jPanel;

    private javax.swing.JPanel jPanelLibs;

    private javax.swing.JPanel jPanelLibsList;

    private javax.swing.JPanel jPanelLicense;

    private javax.swing.JScrollPane jScrollPaneLibs;

    private javax.swing.JScrollPane jScrollPaneProject;

    private javax.swing.JTabbedPane jTabbedPane;

    private javax.swing.JTextArea jTextAreaLicense;

    private org.jdesktop.swingx.JXPanel jXPanelAbout;

    private swingextras.gui.PrettyIcon prettyIcon;

    private org.jdesktop.swingx.JXHyperlink jXHyperlinkProject;

    /**
     * Indexed getter for property libraries.
     * @param index Index of the property.
     * @return Value of the property at <CODE>index</CODE>.
     */
    public Library getLibraries(int index) {
        if (libraries != null) {
            return this.libraries[index];
        } else {
            return null;
        }
    }

    /**
     * Getter for property libraries.
     * @return Value of property libraries.
     */
    public Library[] getLibraries() {
        return this.libraries;
    }

    /**
     * Indexed setter for property libraries.
     * @param index Index of the property.
     * @param library New value of the property at <CODE>index</CODE>.
     */
    public void setLibraries(int index, Library library) {
        this.libraries[index] = library;
        firePropertyChange("libraries", null, null);
    }

    /**
     * Setter for property libraries.
     * @param libraries New value of property libraries.
     */
    public void setLibraries(Library[] libraries) {
        Library[] oldLibraries = this.libraries;
        this.libraries = libraries;
        firePropertyChange("libraries", oldLibraries, libraries);
        recreateLibraries();
    }

    /**
     * Getter for property packageDescripton.
     * @return Value of property packageDescripton.
     */
    public String getPackageDescripton() {
        return this.packageDescripton;
    }

    /**
     * Setter for property packageDescripton.
     * @param packageDescripton New value of property packageDescripton.
     */
    public void setPackageDescripton(String packageDescripton) {
        String oldPackageDescripton = this.packageDescripton;
        this.packageDescripton = packageDescripton;
        firePropertyChange("packageDescripton", oldPackageDescripton, packageDescripton);
        if (packageDescripton != null) {
            jLabelDescription.setText("<html><p align='justify'>" + packageDescripton + "</p></html>");
        } else {
            jLabelDescription.setText(null);
        }
    }

    /**
     * Returns the text used for a small description about the package license.
     * @return the text used for a small description about the package license.
     */
    public String getPackageLicenseSmallDescripton() {
        return this.packageLicSmallDescripton;
    }

    /**
     * Sets the text used for a small description about the package license
     * @param packageLicSmallDescripton a small description about the package license
     */
    public void setPackageLicenseSmallDescripton(String packageLicSmallDescripton) {
        String oldPckgLicSmllDscrpt = this.packageLicSmallDescripton;
        this.packageLicSmallDescripton = packageLicSmallDescripton;
        firePropertyChange("packageLicSmallDescripton", oldPckgLicSmllDscrpt, packageLicSmallDescripton);
        if (packageLicSmallDescripton != null) {
            jLabelSmallLicDescription.setText("<html><p align='center' style='font-size: 12pt'>" + packageLicSmallDescripton + "</p></html>");
        } else {
            jLabelSmallLicDescription.setText(null);
        }
    }

    /**
     * Getter for property projectName.
     * @return Value of property projectName.
     */
    public String getProjectName() {
        return this.projectName;
    }

    /**
     * Setter for property projectName.
     * @param projectName New value of property projectName.
     */
    public void setProjectName(String projectName) {
        String oldProjectName = this.projectName;
        this.projectName = projectName;
        firePropertyChange("projectName", oldProjectName, projectName);
        headerAbout.setTitle(getProjectName());
        String title = MessageFormat.format(bundle.getString("About_{0}"), getProjectName());
        setTitle(title);
    }

    /**
     * Getter for property projectVersion.
     * @return Value of property projectName.
     */
    public String getProjectVersion() {
        return this.projectVersion;
    }

    /**
     * Sets the project version.
     * @param projectVersion the project version.
     */
    public void setProjectVersion(String projectVersion) {
        String oldProjectVersion = this.projectVersion;
        this.projectVersion = projectVersion;
        firePropertyChange("projectVersion", oldProjectVersion, projectVersion);
        if (projectVersion != null && !projectVersion.isEmpty()) {
            String subtitle = MessageFormat.format(bundle.getString("Product_version_{0}"), projectVersion);
            headerAbout.setSubTitle(subtitle);
        } else {
            headerAbout.setSubTitle(null);
        }
    }

    /**
     * Getter for property icon.
     * @return Value of property icon.
     */
    public Icon getImageAbout() {
        return this.imageAbout;
    }

    /**
     * Sets the image in the dialog's header.
     * @param image the image for the header.
     */
    public void setImageAbout(ImageIcon image) {
        Icon oldImageAbout = this.imageAbout;
        this.imageAbout = image;
        firePropertyChange("ImageAbout", oldImageAbout, imageAbout);
        prettyIcon.setImage(image);
    }

    /**
     * Getter for property projectHomepage.
     * @return Value of property projectHomepage.
     */
    public URL getProjectHomepage() {
        return this.projectHomepage;
    }

    /**
     * Setter for property projectHomepage.
     * @param projectHomepage New value of property projectHomepage.
     */
    public void setProjectHomepage(URL projectHomepage) {
        URL oldProjectHomepage = this.projectHomepage;
        this.projectHomepage = projectHomepage;
        firePropertyChange("projectHomepage", oldProjectHomepage, projectHomepage);
        if (oldProjectHomepage != null) {
            jXPanelAbout.remove(jXHyperlinkProject);
        }
        if (projectHomepage != null) {
            try {
                jXHyperlinkProject = new JXHyperlink(new OpenBrowserAction(projectHomepage));
                jXHyperlinkProject.setText(projectHomepage.toString());
                prettyIcon.setTarget(projectHomepage);
                GridBagConstraints gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 2;
                gridBagConstraints.insets = new Insets(5, 5, 5, 5);
                gridBagConstraints.gridwidth = 2;
                jXPanelAbout.add(jXHyperlinkProject, gridBagConstraints);
            } catch (URISyntaxException ex) {
                Logger.getLogger(DialogAbout.class.getName()).log(Level.SEVERE, "Unable to convert project homepage URL to URI", ex);
            }
        }
    }

    public void setPackageLicense(License packageLicense) {
        jTextAreaLicense.setText(packageLicense.text);
        jTextAreaLicense.setCaretPosition(0);
    }

    public void addLicense(License... lic) {
        for (License l : lic) {
            jTabbedPane.addTab(l.shortName, DialogLicense.createAdditionalLicPanel(l));
        }
    }

    private void recreateLibraries() {
        jPanelLibsList.removeAll();
        if (libraries != null && libraries.length > 0) {
            GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new Insets(10, 10, 10, 10);
            jPanelLibsList.add(jLabelLibHead, gridBagConstraints);
            ImageIcon licIcon = IconManager.getIcon("16x16/license.png");
            for (int i = 0; i < libraries.length; i++) {
                JXHyperlink link = new JXHyperlink(new LinkActionX<URI>(libraries[i].hyperlink));
                link.setText(libraries[i].libName);
                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
                gridBagConstraints.insets = new Insets(10, 20, 10, 10);
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = i + 1;
                jPanelLibsList.add(link, gridBagConstraints);
                JScrollPane jScrollPane = new JScrollPane();
                jScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
                jScrollPane.setOpaque(false);
                JTextArea jTextArea = new JTextArea();
                jTextArea.setColumns(20);
                jTextArea.setEditable(false);
                jTextArea.setLineWrap(true);
                jTextArea.setWrapStyleWord(true);
                jTextArea.setText(libraries[i].description);
                jTextArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                jTextArea.setOpaque(false);
                jScrollPane.setViewportView(jTextArea);
                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                gridBagConstraints.weightx = 1;
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = i + 1;
                gridBagConstraints.insets = new Insets(10, 10, 10, 10);
                jPanelLibsList.add(jScrollPane, gridBagConstraints);
                JButton button;
                if (licIcon == null) {
                    button = new JButton("Lic");
                } else {
                    button = new JButton(licIcon);
                }
                button.setMargin(new Insets(2, 2, 2, 2));
                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
                gridBagConstraints.insets = new Insets(10, 10, 10, 20);
                gridBagConstraints.gridx = 2;
                gridBagConstraints.gridy = i + 1;
                jPanelLibsList.add(button, gridBagConstraints);
                if (libraries[i].mainPackageLicense != null) {
                    button.addActionListener(new ButtonActionListener(libraries[i], this));
                } else {
                    button.setEnabled(false);
                }
            }
        } else {
            GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new Insets(10, 10, 10, 10);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.gridwidth = 3;
            jPanelLibsList.add(jLabelNoLibs, gridBagConstraints);
        }
    }

    class ButtonActionListener implements ActionListener {

        private Library lib;

        private Window parent;

        ButtonActionListener(Library lib, Window parent) {
            this.lib = lib;
            this.parent = parent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DialogLicense dialog = new DialogLicense(parent, lib.libName);
            dialog.setPackageLicense(lib.mainPackageLicense);
            if (lib.additionalLicenses != null) {
                for (int i = 0; i < lib.additionalLicenses.length; i++) {
                    dialog.addAdditionalLicense(lib.additionalLicenses[i]);
                }
            }
            GuiUtils.centerWindowOnParent(dialog);
            dialog.setVisible(true);
        }
    }

    ;
}

class LinkActionX<URI> extends AbstractHyperlinkAction<URI> {

    private static final long serialVersionUID = 1L;

    public LinkActionX(URI target) {
        setTarget(target);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (java.awt.Desktop.isDesktopSupported()) {
            try {
                java.awt.Desktop.getDesktop().browse((java.net.URI) this.getTarget());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

;

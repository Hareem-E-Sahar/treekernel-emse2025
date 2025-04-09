package net.sf.traser.configtool.panels;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStoreException;
import java.security.KeyStore.PrivateKeyEntry;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;
import javax.xml.namespace.QName;
import net.sf.traser.common.ConfigurationException;
import net.sf.traser.common.KeystoreFault;
import net.sf.traser.common.KeystoreManager;
import net.sf.traser.common.LabelManager;
import net.sf.traser.common.TraserConstants;
import net.sf.traser.configtool.KeygenFrame;
import net.sf.traser.configtool.PasswordFrame;
import net.sf.traser.configtool.TraserConfigFileManager;
import net.sf.traser.configtool.TraserConfigInterface;
import net.sf.traser.utils.HexUtils;
import net.sf.traser.utils.XmlUtils;
import org.apache.axiom.om.OMElement;
import org.apache.ws.security.WSPasswordCallback;

/**
 * @author karnokd, 2008.01.30.
 * @version $Revision 1.0$
 */
public class LocalUserPanel extends AbstractPanel {

    /** The security alias. */
    private static final String SECURITY_ALIAS = "Security";

    /** Local user name. */
    private JTextField localUserName;

    /** The local url. */
    private JTextField localURL;

    /** Local user private file. */
    private JTextField localUserPrivateFile;

    /** Local user cert file. */
    private JTextField localUserCertFile;

    /** Local user private. */
    private JLabel localUserPrivate;

    /** Local user cert. */
    private JLabel localUserCert;

    /** Local generate key. */
    private JButton localGenkey;

    /** Local import key. */
    private JButton localImportKey;

    /** Local export key. */
    private JButton localExportKey;

    /** Local import cert. */
    private JButton localImportCert;

    /** Local export cert. */
    private JButton localExportCert;

    /** Sender user element name. */
    private static final QName SENDER_USER = new QName("SenderUser");

    /** Dark green color. */
    private static final Color DARK_GREEN = new Color(0x00, 0x80, 0x00);

    /** The keystore manager. */
    private KeystoreManager keymanager;

    /** The key generator frame. */
    private KeygenFrame keygenFrame;

    /** The current certificate directory. */
    private File currentCertDir = new File(".");

    /** The password frame. */
    private PasswordFrame passwordFrame;

    /** The Keystore element. */
    private static final QName KEYSTORE = new QName("Keystore");

    /** The Password callback element. */
    private static final QName PASSWORD_CALLBACK = new QName("PasswordCallback");

    /** Password callback function. */
    private CallbackHandler passwordCallback;

    /** Export local url and public key. */
    private JButton exportPartnerData;

    /** The keystore file name. */
    private String keystoreFilename;

    /** The password edit dialog. */
    private PasswordEditDialog passwordDialog;

    /**
	 * {@inheritDoc}
	 */
    @Override
    public void apply() {
        String keypath = localUserPrivateFile.getText();
        String certpath = localUserCertFile.getText();
        if (!isNullOrEmpty(keypath)) {
            if (isNullOrEmpty(certpath)) {
                showError(labels.get("configtool_keyimport_certneeded"));
                return;
            }
            passwordFrame.setLocationRelativeTo(null);
            if (passwordFrame.showModal()) {
                try {
                    InputStream keyIn = new FileInputStream(keypath);
                    try {
                        InputStream certIn = new FileInputStream(certpath);
                        try {
                            keymanager.importPrivateKey(localUserName.getText(), passwordFrame.getPassword(), keyIn, certIn);
                            HexUtils.addEntryTo(new File(TraserConstants.DEFAULT_PASSWORD_FILE), localUserName.getText(), passwordFrame.getPassword());
                        } finally {
                            certIn.close();
                        }
                    } finally {
                        keyIn.close();
                    }
                } catch (IOException ex) {
                    showError(labels.format("configtool_keyimport_error", keypath, ex.toString()));
                    return;
                }
            }
        } else if (!isNullOrEmpty(certpath) && isNullOrEmpty(keypath)) {
            try {
                InputStream in = new FileInputStream(certpath);
                try {
                    keymanager.installReply(localUserName.getText(), passwordFrame.getPassword(), in, false);
                } finally {
                    in.close();
                }
            } catch (IOException ex) {
                showError(labels.format("configtool_certimport_error", certpath, ex.toString()));
                return;
            }
        }
        TraserConfigInterface tci = config.getInterfaceMap().get(SECURITY_ALIAS);
        OMElement parent = tci.getElement();
        OMElement senderUserElement = parent.getFirstChildWithName(SENDER_USER);
        senderUserElement.setText(localUserName.getText());
        saveKeymanager(parent);
        OMElement url = parent.getFirstChildWithName(URL_QNAME);
        if (url == null) {
            if (localURLLabel.isSelected()) {
                XmlUtils.addElement(factory, parent, URL_QNAME, localURL.getText());
            }
        } else {
            if (localURLLabel.isSelected()) {
                url.setText(localURL.getText());
            } else {
                url.detach();
            }
        }
        setLocalKeypairState(localUserName.getText());
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public void done() {
        if (passwordFrame != null) {
            passwordFrame.dispose();
        }
        if (keygenFrame != null) {
            keygenFrame.dispose();
        }
        if (passwordDialog != null) {
            passwordDialog.done();
        }
    }

    /** Initialize the panel. */
    @Override
    protected void init() {
        panel = new JPanel();
        JLabel localUserNameLabel = new JLabel(labels.get("configtool_local_name"));
        localUserName = new JTextField();
        localUserName.getDocument().addDocumentListener(documentChanged);
        localURLLabel = new JCheckBox(labels.get("traserconfigeditor_local_url"));
        localURLLabel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                localURL.setEnabled(localURLLabel.isSelected());
                panelChanged();
            }
        });
        localURL = new JTextField();
        localURL.getDocument().addDocumentListener(documentChanged);
        exportPartnerData = new JButton(labels.get("traserconfigeditor_export_data"));
        exportPartnerData.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doExportData();
            }
        });
        JLabel localUserPrivateLabel = new JLabel(labels.get("configtool_local_privatekey"));
        JLabel localUserCertLabel = new JLabel(labels.get("configtool_local_cert"));
        localUserPrivate = new JLabel(labels.get("configtool_local_absent"));
        localUserPrivate.setOpaque(true);
        localUserPrivate.setForeground(Color.RED);
        localUserCert = new JLabel(labels.get("configtool_local_absent"));
        localUserCert.setOpaque(true);
        localUserCert.setForeground(Color.RED);
        localUserPrivateFile = new JTextField();
        localUserPrivateFile.setEditable(false);
        localUserCertFile = new JTextField();
        localUserCertFile.setEditable(false);
        JButton localSetpw = new JButton(labels.get("configtool_local_setpw"));
        localSetpw.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doSetPassword();
            }
        });
        localGenkey = new JButton(labels.get("configtool_local_genkey"));
        localGenkey.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doGenerateKeypair();
            }
        });
        localImportKey = new JButton(labels.get("configtool_local_importkey"));
        localImportKey.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doSelectFileFor(localUserPrivateFile);
            }
        });
        localExportKey = new JButton(labels.get("configtool_local_exportkey"));
        localExportKey.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doExportLocalPrivateKey();
            }
        });
        localImportCert = new JButton(labels.get("configtool_local_importcert"));
        localImportCert.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doSelectFileFor(localUserCertFile);
            }
        });
        localExportCert = new JButton(labels.get("configtool_local_exportcert"));
        localExportCert.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doExportLocalCertificate(localUserName.getText());
            }
        });
        GroupLayout gl = new GroupLayout(panel);
        panel.setLayout(gl);
        gl.setAutoCreateContainerGaps(true);
        gl.setAutoCreateGaps(true);
        gl.setHorizontalGroup(gl.createParallelGroup(Alignment.TRAILING).addGroup(gl.createSequentialGroup().addGroup(gl.createParallelGroup(Alignment.LEADING).addComponent(localUserNameLabel).addComponent(localURLLabel).addComponent(localUserPrivateLabel).addComponent(localUserCertLabel)).addGroup(gl.createParallelGroup(Alignment.LEADING).addComponent(localUserName).addComponent(localURL).addComponent(localUserPrivate).addComponent(localUserPrivateFile).addComponent(localUserCert).addComponent(localUserCertFile))).addGroup(gl.createSequentialGroup().addComponent(localSetpw).addComponent(localGenkey).addComponent(exportPartnerData)).addGroup(gl.createSequentialGroup().addComponent(localImportKey).addComponent(localExportKey)).addGroup(gl.createSequentialGroup().addComponent(localImportCert).addComponent(localExportCert)));
        gl.setVerticalGroup(gl.createSequentialGroup().addGroup(gl.createParallelGroup(Alignment.BASELINE).addComponent(localUserNameLabel).addComponent(localUserName)).addGroup(gl.createParallelGroup(Alignment.BASELINE).addComponent(localURLLabel).addComponent(localURL)).addGroup(gl.createParallelGroup(Alignment.BASELINE).addComponent(localSetpw).addComponent(localGenkey).addComponent(exportPartnerData)).addGroup(gl.createParallelGroup(Alignment.BASELINE).addComponent(localUserPrivateLabel).addComponent(localUserPrivate)).addGroup(gl.createParallelGroup(Alignment.BASELINE).addComponent(localUserPrivateFile)).addGroup(gl.createParallelGroup(Alignment.BASELINE).addComponent(localImportKey).addComponent(localExportKey)).addGroup(gl.createParallelGroup(Alignment.BASELINE).addComponent(localUserCertLabel).addComponent(localUserCert)).addGroup(gl.createParallelGroup(Alignment.BASELINE).addComponent(localUserCertFile)).addGroup(gl.createParallelGroup(Alignment.BASELINE).addComponent(localImportCert).addComponent(localExportCert)));
        gl.linkSize(SwingConstants.HORIZONTAL, localImportKey, localExportKey);
        gl.linkSize(SwingConstants.HORIZONTAL, localImportCert, localExportCert);
    }

    /**
	 * Set the keypair field state according to the keystore.
	 * @param alias the alias string
	 */
    private void setLocalKeypairState(String alias) {
        try {
            if (keymanager.getKeyStore().entryInstanceOf(alias, PrivateKeyEntry.class)) {
                localUserPrivate.setText(labels.get("configtool_local_present"));
                localUserPrivate.setForeground(DARK_GREEN);
            } else {
                localUserPrivate.setText(labels.get("configtool_local_absent"));
                localUserPrivate.setForeground(Color.RED);
            }
            if (keymanager.getCertificate(alias) != null) {
                localUserCert.setText(labels.get("configtool_local_present"));
                localUserCert.setForeground(DARK_GREEN);
            } else {
                localUserCert.setText(labels.get("configtool_local_absent"));
                localUserCert.setForeground(Color.RED);
            }
        } catch (KeystoreFault ex) {
            showError(labels.format("configtool_keystore_error", ex.toString()));
        } catch (KeyStoreException ex) {
            showError(labels.format("configtool_keystore_error", ex.toString()));
        }
    }

    /**
	 * Generate new keypair.
	 */
    private void doGenerateKeypair() {
        if (keygenFrame == null) {
            keygenFrame = new KeygenFrame(keymanager, labels);
            keygenFrame.setLocationRelativeTo(null);
        }
        if (keygenFrame.showModal(localUserName.getText())) {
            HexUtils.addEntryTo(TraserConstants.DEFAULT_PASSWORD_FILE, localUserName.getText(), keygenFrame.getPassword());
            JOptionPane.showMessageDialog(null, labels.get("configtool_genkey_success"), getTitle(), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
	 * Export the private key.
	 */
    private void doExportLocalPrivateKey() {
        passwordFrame.setLocationRelativeTo(null);
        if (passwordFrame.showModal()) {
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(currentCertDir);
            fc.setSelectedFile(new File(localUserName.getText() + ".pem"));
            if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                currentCertDir = f.getParentFile();
                try {
                    OutputStream out = new FileOutputStream(f);
                    try {
                        keymanager.exportPrivateKey(localUserName.getText(), passwordFrame.getPassword(), out, false);
                    } finally {
                        out.close();
                    }
                } catch (IOException ex) {
                    showError(labels.format("configtool_genkey_exporterror", ex.toString()));
                } catch (KeystoreFault ex) {
                    showError(labels.format("configtool_genkey_exporterror", ex.toString()));
                }
            }
        }
    }

    /**
	 * Export the certificate.
	 * @param alias the alias of the certificate
	 */
    private void doExportLocalCertificate(String alias) {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(currentCertDir);
        fc.setSelectedFile(new File(alias + ".cer"));
        if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            currentCertDir = f.getParentFile();
            try {
                OutputStream out = new FileOutputStream(f);
                try {
                    keymanager.exportCertificate(alias, out, false);
                } finally {
                    out.close();
                }
            } catch (IOException ex) {
                showError(labels.format("configtool_genkey_exporterror", ex.toString()));
            } catch (KeystoreFault ex) {
                showError(labels.format("configtool_genkey_exporterror", ex.toString()));
            }
        }
    }

    /**
	 * Show an open file dialog and fill the selected filename into the field.
	 * @param field the field to fill in
	 */
    private void doSelectFileFor(JTextField field) {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(currentCertDir);
        if (fc.showOpenDialog(field.getParent()) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            field.setText(f.getAbsolutePath());
            currentCertDir = f.getParentFile();
        }
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public String getTitle() {
        return labels.get("traserconfigeditor_localuser");
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public void revert() {
        TraserConfigInterface security = config.getInterfaceMap().get(SECURITY_ALIAS);
        if (security != null) {
            processSecurity(security.getElement());
            setLocalKeypairState(localUserName.getText());
        }
    }

    /**
	 * Process the security interface configuration.
	 * @param e the configuration node.
	 */
    private void processSecurity(OMElement e) {
        OMElement ks = e.getFirstChildWithName(KEYSTORE);
        OMElement senderUserElement = e.getFirstChildWithName(SENDER_USER);
        localUserName.setText(senderUserElement.getText());
        String s = XmlUtils.getValue(e, URL_QNAME);
        localURLLabel.setSelected(s != null);
        localURL.setEnabled(s != null);
        localURL.setText(s);
        keystoreFilename = ks.getText();
        keymanager.load(keystoreFilename, getKeystorePassword(e));
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public void setConfig(TraserConfigFileManager config) {
        super.setConfig(config);
        keymanager = new KeystoreManager();
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public void setLabels(LabelManager labels) {
        super.setLabels(labels);
        passwordFrame = new PasswordFrame(labels);
        passwordDialog = new PasswordEditDialog(labels);
    }

    /**
	 * Tests whether the string is null or empty.
	 * @param s the string
	 * @return true if s is null or is empty
	 */
    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /** Export full data. */
    private void doExportData() {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(currentCertDir);
        fc.setSelectedFile(new File(localUserName.getText() + ".xml"));
        if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            currentCertDir = f.getParentFile();
            try {
                OutputStream out = new FileOutputStream(f);
                out.write("<?xml version='1.0' encoding='UTF-8'?>\n".getBytes("UTF-8"));
                out.write("<".getBytes("UTF-8"));
                out.write(TRASER_PARTNER_DATA.getBytes("UTF-8"));
                out.write(">\n".getBytes("UTF-8"));
                try {
                    out.write("  <".getBytes("UTF-8"));
                    out.write(PARTNER.getBytes("UTF-8"));
                    out.write(">\n".getBytes("UTF-8"));
                    out.write("    <".getBytes("UTF-8"));
                    out.write(URL_TAG.getBytes("UTF-8"));
                    out.write(">".getBytes("UTF-8"));
                    out.write(localURL.getText().getBytes("UTF-8"));
                    out.write("</".getBytes("UTF-8"));
                    out.write(URL_TAG.getBytes("UTF-8"));
                    out.write(">\n".getBytes("UTF-8"));
                    out.write("    <".getBytes("UTF-8"));
                    out.write(CERTIFICATE.getBytes("UTF-8"));
                    out.write(">\n".getBytes("UTF-8"));
                    keymanager.exportCertificate(localUserName.getText(), out, false);
                    out.write("    </".getBytes("UTF-8"));
                    out.write(CERTIFICATE.getBytes("UTF-8"));
                    out.write(">\n".getBytes("UTF-8"));
                    out.write("  </".getBytes("UTF-8"));
                    out.write(PARTNER.getBytes("UTF-8"));
                    out.write(">\n".getBytes("UTF-8"));
                } finally {
                    out.write("</".getBytes("UTF-8"));
                    out.write(TRASER_PARTNER_DATA.getBytes("UTF-8"));
                    out.write(">\n".getBytes("UTF-8"));
                    out.close();
                }
            } catch (IOException ex) {
                showError(labels.format("configtool_genkey_exporterror", ex.toString()));
            } catch (KeystoreFault ex) {
                showError(labels.format("configtool_genkey_exporterror", ex.toString()));
            }
        }
    }

    /**
	 * Save keymanager state.
	 * @param element the security element
	 */
    private void saveKeymanager(OMElement element) {
        try {
            keymanager.save(keystoreFilename, getKeystorePassword(element));
        } catch (KeystoreFault ex) {
            showError(ex.toString());
        }
    }

    /**
	 * Ket keystore password.
	 * @param element the security element
	 * @return the password
	 */
    private char[] getKeystorePassword(OMElement element) {
        OMElement pc = element.getFirstChildWithName(PASSWORD_CALLBACK);
        String passwordCallbackName = pc.getText();
        try {
            passwordCallback = (CallbackHandler) Class.forName(passwordCallbackName).newInstance();
            WSPasswordCallback c = new WSPasswordCallback(TraserConstants.KEYSTORE_PASSWORD_REQUEST, WSPasswordCallback.UNKNOWN);
            passwordCallback.handle(new Callback[] { c });
            String pass = c.getPassword();
            return pass != null ? pass.toCharArray() : null;
        } catch (UnsupportedCallbackException ex) {
            throw new ConfigurationException("Password callback class " + passwordCallbackName + " support error", ex);
        } catch (IOException ex) {
            throw new ConfigurationException("Password callback class " + passwordCallbackName + " IO error", ex);
        } catch (ClassNotFoundException ex) {
            throw new ConfigurationException("Password callback class " + passwordCallbackName + " not found", ex);
        } catch (InstantiationException ex) {
            throw new ConfigurationException("Password callback class " + passwordCallbackName + " instanciation error", ex);
        } catch (IllegalAccessException ex) {
            throw new ConfigurationException("Password callback class " + passwordCallbackName + " illegal access error", ex);
        } catch (ClassCastException ex) {
            throw new ConfigurationException("Password callback class " + passwordCallbackName + " is not a javax.security.auth.callback.PasswordCallback", ex);
        }
    }

    /**
	 * Set password.
	 */
    private void doSetPassword() {
        if (passwordDialog.showModal()) {
            HexUtils.addEntryTo(TraserConstants.DEFAULT_PASSWORD_FILE, localUserName.getText(), passwordDialog.getPassword());
        }
    }

    /** Traser partner data element. */
    private static final String TRASER_PARTNER_DATA = "TraserPartnerData";

    /** The url element. */
    private static final String URL_TAG = "URL";

    /** The url element name. */
    private static final QName URL_QNAME = new QName(URL_TAG);

    /** The certificate element. */
    private static final String CERTIFICATE = "Certificate";

    /** The partner element. */
    private static final String PARTNER = "Partner";

    /** The local url checkbox. */
    private JCheckBox localURLLabel;
}

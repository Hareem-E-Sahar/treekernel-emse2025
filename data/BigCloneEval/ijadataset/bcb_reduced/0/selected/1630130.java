package com.hanhuy.scurp;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import com.hanhuy.common.ui.DataBindingManager;
import com.hanhuy.common.ui.ResourceBundleForm;
import com.hanhuy.common.ui.Util;
import com.hanhuy.scurp.data.DatabaseFile;
import com.hanhuy.scurp.data.DatabaseFile.StaleException;
import com.hanhuy.scurp.server.CertificateGenerator;
import com.hanhuy.scurp.server.PasswordServer;

/**
 *
 * @author pfnguyen
 */
public class ServerOptionsDialog extends ResourceBundleForm {

    private JDialog dialog;

    private DatabaseFile database;

    private DataBindingManager manager = new DataBindingManager();

    private boolean caLabelsAdded;

    private boolean serverLabelsAdded;

    private boolean caKeyChanged;

    private boolean serverKeyChanged;

    private X509Certificate caCert, serverCert;

    private PrivateKey caKey, serverKey;

    private int newPort = -1;

    private String newHost;

    private boolean runServer = Main.getPreferences().getBoolean(Main.SCURP_RUNSERVER_KEY, false);

    private JLabel caDNField, caIssuedField, caExpiresField, caSHA1FingerprintField, caMD5FingerprintField;

    private JLabel serverDNField, serverIssuedField, serverExpiresField, serverSHA1FingerprintField, serverMD5FingerprintField, serverSerialField;

    public ServerOptionsDialog(Frame parent, DatabaseFile data) {
        database = data;
        dialog = new JDialog(parent, getString("title"), true);
        Map.Entry<X509Certificate, PrivateKey> caPair = data.getCAKeyPair();
        if (caPair != null) {
            caCert = caPair.getKey();
            caKey = caPair.getValue();
        }
        Map.Entry<X509Certificate, PrivateKey> serverPair = data.getServerKeyPair();
        if (serverPair != null) {
            serverCert = serverPair.getKey();
            serverKey = serverPair.getValue();
        }
        layoutDialog();
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.pack();
        Util.centerWindow(parent, dialog);
        dialog.setVisible(true);
    }

    private void layoutDialog() {
        dialog.setLayout(createLayoutManager());
        if (CertificateGenerator.isEnabled()) layoutEnabledDialog(); else layoutDisabledDialog();
    }

    private void layoutDisabledDialog() {
        dialog.add(new JLabel(), "disabledLabel");
    }

    private void layoutEnabledDialog() {
        final JCheckBox runBox = new JCheckBox();
        dialog.add(runBox, "runCheckbox");
        dialog.add(new JLabel(), "listenPortLabel");
        JTextField portField = new JTextField();
        dialog.add(portField, "listenPortField");
        dialog.add(new JLabel(), "listenAddressLabel");
        JTextField addressField = new JTextField();
        dialog.add(addressField, "listenAddressField");
        final JButton clearServerButton = new JButton();
        final JButton clearCAButton = new JButton();
        dialog.add(clearCAButton, "clearCAButton");
        clearCAButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int r = JOptionPane.showConfirmDialog(dialog, getString("confirmClearCAText"), getString("confirmClearCATitle"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (r != JOptionPane.OK_OPTION) return;
                clearCAButton.setVisible(false);
                clearServerButton.setVisible(false);
                runBox.setSelected(false);
                setCAComponentsVisible(false);
                setServerComponentsVisible(false);
                caCert = null;
                serverCert = null;
                dialog.pack();
                database.removeCAKeyPair();
                try {
                    database.save();
                } catch (IOException ex) {
                    error(ex.getMessage(), getString("errorCannotSave"));
                } catch (StaleException ex) {
                    error(ex.getMessage(), getString("errorStaleDatabase"));
                }
            }
        });
        clearCAButton.setVisible(caCert != null);
        JButton generateCAButton = new JButton();
        dialog.add(generateCAButton, "generateCAButton");
        generateCAButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (caCert != null) {
                    int r = JOptionPane.showConfirmDialog(dialog, getString("confirmRegenerateCAText"), getString("confirmRegenerateCATitle"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (r == JOptionPane.CANCEL_OPTION) return;
                }
                generateCACert();
                clearCAButton.setVisible(true);
            }
        });
        clearCAButton.setVisible(caCert != null);
        JButton generateServerButton = new JButton();
        dialog.add(generateServerButton, "generateServerButton");
        generateServerButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (serverCert != null) {
                    int r = JOptionPane.showConfirmDialog(dialog, getString("confirmRegenerateServerText"), getString("confirmRegenerateServerTitle"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (r == JOptionPane.CANCEL_OPTION) return;
                }
                generateServerCert();
                clearServerButton.setVisible(true);
                clearCAButton.setVisible(true);
            }
        });
        dialog.add(clearServerButton, "clearServerButton");
        clearServerButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int r = JOptionPane.showConfirmDialog(dialog, getString("confirmClearServerText"), getString("confirmClearServerTitle"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (r != JOptionPane.OK_OPTION) return;
                clearServerButton.setVisible(false);
                runBox.setSelected(false);
                setServerComponentsVisible(false);
                dialog.pack();
                database.removeServerKeyPair();
                serverCert = null;
                try {
                    database.save();
                } catch (IOException ex) {
                    error(ex.getMessage(), getString("errorCannotSave"));
                } catch (StaleException ex) {
                    error(ex.getMessage(), getString("errorStaleDatabase"));
                }
            }
        });
        clearServerButton.setVisible(serverCert != null);
        if (caCert != null) bindCACertificate(caCert);
        if (serverCert != null) bindServerCertificate(serverCert);
        manager.bind(portField, "text", this, "port");
        manager.bind(addressField, "text", this, "host");
        manager.bind(runBox, "selected", this, "runServer");
        JButton okButton = new JButton();
        ActionListener saveAction = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                saveContents();
            }
        };
        okButton.addActionListener(saveAction);
        portField.addActionListener(saveAction);
        addressField.addActionListener(saveAction);
        JButton cancelButton = new JButton();
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        dialog.add(okButton, "okButton");
        dialog.add(cancelButton, "cancelButton");
    }

    private void setCAComponentsVisible(boolean b) {
        setComponentsVisible(new String[] { "caDNLabel", "caCreatedLabel", "caExpiresLabel", "caSHA1FingerPrintLabel", "caMD5FingerPrintLabel", "caDNField", "caCreatedField", "caExpiresField", "caSHA1FingerPrintField", "caMD5FingerPrintField" }, b);
    }

    private void setServerComponentsVisible(boolean b) {
        setComponentsVisible(new String[] { "serverDNLabel", "serverCreatedLabel", "serverExpiresLabel", "serverSHA1FingerPrintLabel", "serverMD5FingerPrintLabel", "serverSerialLabel", "serverDNField", "serverCreatedField", "serverExpiresField", "serverSHA1FingerPrintField", "serverMD5FingerPrintField", "serverSerialField" }, b);
    }

    private void setComponentsVisible(String[] names, boolean b) {
        HashMap<String, Component> cmap = new HashMap<String, Component>();
        Component[] comps = dialog.getContentPane().getComponents();
        for (Component c : comps) cmap.put(c.getName(), c);
        for (String name : names) {
            Component c = cmap.get(name);
            if (c != null) c.setVisible(b);
        }
        dialog.pack();
    }

    private void bindCACertificate(X509Certificate cert) {
        if (!caLabelsAdded) {
            dialog.add(new JLabel(), "caDNLabel");
            dialog.add(new JLabel(), "caCreatedLabel");
            dialog.add(new JLabel(), "caExpiresLabel");
            dialog.add(new JLabel(), "caSHA1FingerPrintLabel");
            dialog.add(new JLabel(), "caMD5FingerPrintLabel");
            caDNField = new JLabel();
            caIssuedField = new JLabel();
            caExpiresField = new JLabel();
            caSHA1FingerprintField = new JLabel();
            caMD5FingerprintField = new JLabel();
            dialog.add(caDNField, "caDNField");
            dialog.add(caIssuedField, "caCreatedField");
            dialog.add(caExpiresField, "caExpiresField");
            dialog.add(caSHA1FingerprintField, "caSHA1FingerPrintField");
            dialog.add(caMD5FingerprintField, "caMD5FingerPrintField");
        }
        caLabelsAdded = true;
        String x500principal = cert.getSubjectX500Principal().toString();
        caDNField.setText(x500principal);
        caIssuedField.setText(format(cert.getNotBefore()));
        caExpiresField.setText(format(cert.getNotAfter()));
        try {
            caSHA1FingerprintField.setText(Digest.sha1(cert.getEncoded()));
            caMD5FingerprintField.setText(Digest.md5(cert.getEncoded()));
        } catch (CertificateEncodingException e) {
            throw new IllegalStateException(e);
        }
        dialog.pack();
    }

    private void bindServerCertificate(X509Certificate cert) {
        if (!serverLabelsAdded) {
            dialog.add(new JLabel(), "serverDNLabel");
            dialog.add(new JLabel(), "serverCreatedLabel");
            dialog.add(new JLabel(), "serverExpiresLabel");
            dialog.add(new JLabel(), "serverSHA1FingerPrintLabel");
            dialog.add(new JLabel(), "serverMD5FingerPrintLabel");
            dialog.add(new JLabel(), "serverSerialLabel");
            serverDNField = new JLabel();
            serverIssuedField = new JLabel();
            serverExpiresField = new JLabel();
            serverSHA1FingerprintField = new JLabel();
            serverMD5FingerprintField = new JLabel();
            serverSerialField = new JLabel();
            dialog.add(serverDNField, "serverDNField");
            dialog.add(serverSerialField, "serverSerialField");
            dialog.add(serverIssuedField, "serverCreatedField");
            dialog.add(serverExpiresField, "serverExpiresField");
            dialog.add(serverSHA1FingerprintField, "serverSHA1FingerPrintField");
            dialog.add(serverMD5FingerprintField, "serverMD5FingerPrintField");
        }
        serverLabelsAdded = true;
        String x500principal = cert.getSubjectX500Principal().toString();
        serverDNField.setText(x500principal);
        serverIssuedField.setText(format(cert.getNotBefore()));
        serverExpiresField.setText(format(cert.getNotAfter()));
        ByteBuffer b = ByteBuffer.allocate(8);
        b.putLong(cert.getSerialNumber().longValue());
        serverSerialField.setText(cert.getSerialNumber().toString() + " (" + Digest.toHexString(b.array()) + ")");
        try {
            serverSHA1FingerprintField.setText(Digest.sha1(cert.getEncoded()));
            serverMD5FingerprintField.setText(Digest.md5(cert.getEncoded()));
        } catch (CertificateEncodingException e) {
            throw new IllegalStateException(e);
        }
        dialog.pack();
    }

    private String format(Date date) {
        return format("dateFormat", date);
    }

    public int getPort() {
        return Main.getPreferences().getInt(Main.SCURP_PORT_KEY, Main.DEFAULT_LISTEN_PORT);
    }

    public void setPort(int port) {
        newPort = port;
    }

    public String getHost() {
        return Main.getPreferences().get(Main.SCURP_HOST_KEY, "localhost");
    }

    public void setHost(String host) {
        newHost = host;
    }

    private void generateCACert() {
        KeyPair kp = CertificateGenerator.generateKeyPair();
        X509Certificate c;
        if (serverCert != null) generateServerCert();
        try {
            String name = database.getFile().getName();
            name = name.replaceAll(",", "\\,");
            name = name.replaceAll("\\\\", "/");
            c = CertificateGenerator.generateCACertificate(kp, name);
            bindCACertificate(c);
            caCert = c;
            caKey = kp.getPrivate();
            caKeyChanged = true;
        } catch (GeneralSecurityException ex) {
            error(ex.getMessage(), getString("errorCannotGenerateCACert"));
        }
        setCAComponentsVisible(true);
    }

    private void generateServerCert() {
        KeyPair kp = CertificateGenerator.generateKeyPair();
        X509Certificate c;
        try {
            manager.saveToBean();
        } catch (NumberFormatException e) {
            error(getString("errorPortNumberNotNumeric"), getString("errorInvalidInput"));
            return;
        }
        if (caCert == null) {
            serverCert = null;
            generateCACert();
        }
        try {
            c = CertificateGenerator.generateCertificate(getHost(), kp.getPublic(), caKey, caCert);
            bindServerCertificate(c);
            serverCert = c;
            serverKey = kp.getPrivate();
            serverKeyChanged = true;
        } catch (GeneralSecurityException ex) {
            error(ex.getMessage(), getString("errorCannotGenerateServerCert"));
        }
        setServerComponentsVisible(true);
    }

    private void error(String message, String title) {
        JOptionPane.showMessageDialog(dialog, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void saveContents() {
        try {
            manager.saveToBean();
        } catch (NumberFormatException e) {
            error(getString("errorPortNumberNotNumeric"), getString("errorInvalidInput"));
            return;
        }
        if (newPort > 0) Main.getPreferences().putInt(Main.SCURP_PORT_KEY, newPort);
        if (newHost != null) Main.getPreferences().put(Main.SCURP_HOST_KEY, newHost);
        Main.getPreferences().putBoolean(Main.SCURP_RUNSERVER_KEY, runServer);
        if (runServer) {
            if (caCert == null) {
                generateCACert();
                generateServerCert();
            }
            if (serverCert == null) generateServerCert();
            if (PasswordServer.isRunning() && serverKeyChanged) {
                if (database == PasswordServer.getCurrentDatabase()) {
                    database.getPasswordServer().stop();
                    database.setPasswordServer(null);
                }
            }
            if (!PasswordServer.isRunning()) Main.launchServer(dialog, database);
        }
        if (!runServer && PasswordServer.isRunning()) {
            if (PasswordServer.getCurrentDatabase() == database) {
                database.getPasswordServer().stop();
                database.setPasswordServer(null);
            }
        }
        if (caKeyChanged && caCert != null) {
            database.setCAKeyPair(caCert, caKey);
        }
        if (serverKeyChanged && serverCert != null) {
            database.setServerKeyPair(serverCert, serverKey);
        }
        try {
            if ((caKeyChanged && caCert != null) || (serverKeyChanged && serverCert != null)) {
                database.save();
            }
            dialog.setVisible(false);
            dialog.dispose();
        } catch (IOException e) {
            error(e.getMessage(), getString("errorCannotSave"));
        } catch (StaleException ex) {
            error(ex.getMessage(), getString("errorStaleDatabase"));
        }
    }

    public boolean getRunServer() {
        return Main.getPreferences().getBoolean(Main.SCURP_RUNSERVER_KEY, runServer);
    }

    public void setRunServer(boolean runServer) {
        this.runServer = runServer;
    }
}

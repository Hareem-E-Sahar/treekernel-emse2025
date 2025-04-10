package com.sshtools.common.keygen;

import com.sshtools.common.ui.IconWrapperPanel;
import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.j2ssh.configuration.ConfigurationException;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.transport.publickey.OpenSSHPublicKeyFormat;
import com.sshtools.j2ssh.transport.publickey.SECSHPublicKeyFormat;
import com.sshtools.j2ssh.transport.publickey.SshKeyGenerator;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.15 $
 */
public class Main extends JFrame implements ActionListener {

    static final String ICON = "/com/sshtools/common/authentication/largepassphrase.png";

    JButton close;

    JButton generate;

    KeygenPanel keygen;

    /**
* Creates a new Main object.
*/
    public Main() {
        super("ssh-keygen");
        try {
            ConfigurationLoader.initialize(false);
        } catch (ConfigurationException ex) {
        }
        setIconImage(new ResourceIcon(ICON).getImage());
        keygen = new KeygenPanel();
        IconWrapperPanel centerPanel = new IconWrapperPanel(new ResourceIcon(ICON), keygen);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(6, 6, 0, 0);
        gbc.weighty = 1.0;
        generate = new JButton("Generate");
        generate.addActionListener(this);
        generate.setMnemonic('g');
        this.getRootPane().setDefaultButton(generate);
        UIUtil.jGridBagAdd(buttonPanel, generate, gbc, GridBagConstraints.RELATIVE);
        close = new JButton("Close");
        close.addActionListener(this);
        close.setMnemonic('c');
        UIUtil.jGridBagAdd(buttonPanel, close, gbc, GridBagConstraints.REMAINDER);
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        southPanel.add(buttonPanel);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        getContentPane().setLayout(new GridLayout(1, 1));
        getContentPane().add(mainPanel);
    }

    /**
*
*
* @param evt
*/
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == close) {
            dispose();
            return;
        }
        final String newPassphrase = new String(keygen.getNewPassphrase()).trim();
        final String oldPassphrase = new String(keygen.getOldPassphrase()).trim();
        if ((keygen.getAction() == KeygenPanel.GENERATE_KEY_PAIR) || (keygen.getAction() == KeygenPanel.CHANGE_PASSPHRASE)) {
            if (newPassphrase.length() == 0) {
                if (JOptionPane.showConfirmDialog(this, "Passphrase is empty. Are you sure?", "Empty Passphrase", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                    return;
                }
            }
        }
        final File inputFile = new File(keygen.getInputFilename());
        final File outputFile = new File(keygen.getOutputFilename());
        final File publicFile = new File(keygen.getOutputFilename() + ".pub");
        if ((keygen.getAction() == KeygenPanel.CONVERT_IETF_SECSH_TO_OPENSSH) || (keygen.getAction() == KeygenPanel.CONVERT_OPENSSH_TO_IETF_SECSH) || (keygen.getAction() == KeygenPanel.GENERATE_KEY_PAIR)) {
            if (keygen.getOutputFilename().length() == 0) {
                JOptionPane.showMessageDialog(this, "No Output file supplied.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (outputFile.exists()) {
                if (JOptionPane.showConfirmDialog(this, "Output file " + outputFile.getName() + " exists. Are you sure?", "File exists", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            if (outputFile.exists() && !outputFile.canWrite()) {
                JOptionPane.showMessageDialog(this, "Output file " + outputFile.getName() + " can not be written.", "Unwriteable file", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if ((keygen.getAction() == KeygenPanel.CONVERT_IETF_SECSH_TO_OPENSSH) || (keygen.getAction() == KeygenPanel.CONVERT_OPENSSH_TO_IETF_SECSH)) {
            if (keygen.getInputFilename().length() == 0) {
                JOptionPane.showMessageDialog(this, "No Input file supplied.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else if (keygen.getAction() == KeygenPanel.GENERATE_KEY_PAIR) {
            if (publicFile.exists() && !publicFile.canWrite()) {
                JOptionPane.showMessageDialog(this, "Public key file " + publicFile.getName() + " can not be written.", "Unwriteable file", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        final ProgressMonitor monitor = new ProgressMonitor(this, "Generating keys", "Generating", 0, 100);
        monitor.setMillisToDecideToPopup(0);
        monitor.setMillisToPopup(0);
        Runnable r = new Runnable() {

            public void run() {
                try {
                    if (keygen.getAction() == KeygenPanel.CHANGE_PASSPHRASE) {
                        monitor.setNote("Changing passphrase");
                        SshKeyGenerator.changePassphrase(inputFile, oldPassphrase, newPassphrase);
                        monitor.setNote("Complete");
                        JOptionPane.showMessageDialog(Main.this, "Passphrase changed", "Passphrase changed", JOptionPane.INFORMATION_MESSAGE);
                    } else if (keygen.getAction() == KeygenPanel.CONVERT_IETF_SECSH_TO_OPENSSH) {
                        monitor.setNote("Converting key file");
                        writeString(outputFile, SshKeyGenerator.convertPublicKeyFile(inputFile, new OpenSSHPublicKeyFormat()));
                        monitor.setNote("Complete");
                        JOptionPane.showMessageDialog(Main.this, "Key converted", "Key converted", JOptionPane.INFORMATION_MESSAGE);
                    } else if (keygen.getAction() == KeygenPanel.CONVERT_OPENSSH_TO_IETF_SECSH) {
                        monitor.setNote("Converting key file");
                        writeString(outputFile, SshKeyGenerator.convertPublicKeyFile(inputFile, new SECSHPublicKeyFormat()));
                        monitor.setNote("Complete");
                        JOptionPane.showMessageDialog(Main.this, "Key converted", "Key converted", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        monitor.setNote("Creating generator");
                        SshKeyGenerator generator = new SshKeyGenerator();
                        monitor.setNote("Generating");
                        String username = System.getProperty("user.name");
                        generator.generateKeyPair(keygen.getType(), keygen.getBits(), outputFile.getAbsolutePath(), username, newPassphrase);
                        monitor.setNote("Complete");
                        JOptionPane.showMessageDialog(Main.this, "Key generated to " + outputFile.getName(), "Complete", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(Main.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    monitor.close();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    /**
*
*
* @param args
*/
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        Main main = new Main();
        main.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });
        main.pack();
        UIUtil.positionComponent(SwingConstants.CENTER, main);
        main.setVisible(true);
    }

    private void writeString(File file, String string) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            PrintWriter w = new PrintWriter(out, true);
            w.println(string);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}

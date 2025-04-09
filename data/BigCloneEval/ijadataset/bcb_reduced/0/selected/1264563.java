package mailredirect;

import com.runstate.mailpage.Attachment;
import com.runstate.mailpage.Renderable;
import com.runstate.mailpage.RenderableMessage;
import com.runstate.mailpage.RenderablePlainText;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 *
 * @author  Navneet
 */
public class MailRedirect extends javax.swing.JFrame {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    transient JFileChooser fileChooser = new JFileChooser();

    private static boolean TEST_MODE = false;

    private Store store = null;

    private Thread messageThread = null;

    Message currentMessage;

    private String lastProcessedFolder;

    private Connection connection;

    /** Creates new form MainFrame */
    public MailRedirect() throws ClassNotFoundException {
        Class.forName("org.hsqldb.jdbcDriver");
        initComponents();
    }

    private void initComponents() {
        javax.swing.JLabel jLabel1;
        javax.swing.JLabel jLabel10;
        javax.swing.JLabel jLabel2;
        javax.swing.JLabel jLabel3;
        javax.swing.JLabel jLabel4;
        javax.swing.JLabel jLabel5;
        javax.swing.JLabel jLabel6;
        javax.swing.JLabel jLabel7;
        javax.swing.JLabel jLabel8;
        javax.swing.JLabel jLabel9;
        javax.swing.JPanel jPanel1;
        javax.swing.JPanel jPanel2;
        javax.swing.JPanel jPanel3;
        javax.swing.JScrollPane jScrollPane1;
        javax.swing.JScrollPane jScrollPane2;
        jLabel1 = new javax.swing.JLabel();
        mboxField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        folderMsgCountLabel = new javax.swing.JLabel();
        folderNameLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        skipAlwaysButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        smtpHost = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        smtpUser = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        smtpPassword = new javax.swing.JPasswordField();
        needAuth = new javax.swing.JCheckBox();
        jLabel10 = new javax.swing.JLabel();
        targetEmaillAddress = new javax.swing.JTextField();
        compressAttachCheckBox = new javax.swing.JCheckBox();
        jLabel15 = new javax.swing.JLabel();
        maxSizeTextField = new javax.swing.JTextField();
        locationSelectButton = new javax.swing.JButton();
        testButton = new javax.swing.JButton();
        detailsTabbedPane = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        folderList = new javax.swing.JList();
        folderList.setModel(new DefaultListModel());
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        msgNumber = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        msgFolder = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        fromTextField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        toTextField = new javax.swing.JTextField();
        subjectTextField = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        sizeTextField = new javax.swing.JTextField();
        statusLabel = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        msgDate = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        otherInfoTextArea = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        msgArea = new javax.swing.JTextArea();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        jLabel1.setText("Mailbox Location:");
        cancelButton.setText("Cancel");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        saveButton.setText("Start");
        saveButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        jLabel5.setText("Folder:");
        folderMsgCountLabel.setText("N/A");
        folderNameLabel.setText("N/A");
        jLabel7.setText(" msg count:");
        skipAlwaysButton.setText("Skip Always");
        skipAlwaysButton.setEnabled(false);
        skipAlwaysButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skipAlwaysButtonActionPerformed(evt);
            }
        });
        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup().addContainerGap().add(jLabel7).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(folderMsgCountLabel).add(110, 110, 110).add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 34, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(12, 12, 12).add(folderNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(saveButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(cancelButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(skipAlwaysButton).addContainerGap()));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup().addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(skipAlwaysButton).add(cancelButton).add(saveButton).add(jLabel7).add(folderMsgCountLabel).add(jLabel5).add(folderNameLabel)).addContainerGap()));
        jLabel6.setText("SMTP Server:");
        jLabel8.setText("User :");
        smtpUser.setEnabled(false);
        jLabel9.setText("Password:");
        smtpPassword.setText("jPasswordField1");
        smtpPassword.setEnabled(false);
        needAuth.setText("Auth ?");
        needAuth.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        needAuth.setMargin(new java.awt.Insets(0, 0, 0, 0));
        needAuth.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                needAuthActionPerformed(evt);
                needAuthActionPerformed1(evt);
            }
        });
        jLabel10.setText("Target Email address:");
        compressAttachCheckBox.setText("Compress message attachments");
        compressAttachCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        compressAttachCheckBox.setEnabled(false);
        compressAttachCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jLabel15.setText("Skip messages exceeping size(bytes):");
        maxSizeTextField.setText("0");
        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel3Layout.createSequentialGroup().add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel3Layout.createSequentialGroup().add(jLabel6).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(smtpHost, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(needAuth).add(15, 15, 15).add(jLabel8).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(smtpUser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 83, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel9).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(smtpPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 91, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(jPanel3Layout.createSequentialGroup().add(jLabel10).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(targetEmaillAddress, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)).add(jPanel3Layout.createSequentialGroup().add(compressAttachCheckBox).add(53, 53, 53).add(jLabel15).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(maxSizeTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE))).addContainerGap()));
        jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel3Layout.createSequentialGroup().addContainerGap().add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel6).add(smtpHost, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(smtpUser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel9).add(needAuth).add(jLabel8).add(smtpPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel10).add(targetEmaillAddress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(compressAttachCheckBox).add(jLabel15).add(maxSizeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))));
        locationSelectButton.setText("...");
        locationSelectButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationSelectButtonActionPerformed(evt);
            }
        });
        testButton.setText("Fetch folder list");
        testButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testButtonActionPerformed(evt);
            }
        });
        detailsTabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        folderList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {

            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                folderListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(folderList);
        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE));
        jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE));
        detailsTabbedPane.addTab("Mailbox Selection Tab", jPanel4);
        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel2.setText("Msg:");
        msgNumber.setText("0");
        jLabel3.setText("Message Folder:");
        msgFolder.setEditable(false);
        jLabel4.setText("Subject:");
        jLabel11.setText("From:");
        fromTextField.setEditable(false);
        jLabel12.setText("To:");
        toTextField.setEditable(false);
        subjectTextField.setEditable(false);
        jLabel13.setText("Size:");
        sizeTextField.setEditable(false);
        statusLabel.setText("    ");
        jLabel14.setText("Msg Date:");
        msgDate.setEditable(false);
        jLabel16.setText("Other:");
        otherInfoTextArea.setColumns(20);
        otherInfoTextArea.setRows(5);
        jScrollPane3.setViewportView(otherInfoTextArea);
        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(statusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE).add(jPanel2Layout.createSequentialGroup().add(jLabel2).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(msgNumber, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 83, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel13).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(sizeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 141, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(msgFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)).add(jPanel2Layout.createSequentialGroup().addContainerGap().add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jLabel14).add(jLabel4).add(jLabel12).add(jLabel11).add(jLabel16)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE).add(fromTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE).add(toTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE).add(subjectTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE).add(msgDate, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE))));
        jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel2Layout.createSequentialGroup().add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel2).add(jLabel13).add(jLabel3).add(sizeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(msgFolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(msgNumber)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel11).add(fromTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(toTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel12)).add(9, 9, 9).add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(subjectTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel4)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel14).add(msgDate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jLabel16).add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(statusLabel)));
        detailsTabbedPane.addTab("Current Message Information", jPanel2);
        msgArea.setColumns(20);
        msgArea.setRows(5);
        jScrollPane2.setViewportView(msgArea);
        detailsTabbedPane.addTab("Message Area", jScrollPane2);
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, detailsTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 687, Short.MAX_VALUE).add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(layout.createSequentialGroup().add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(mboxField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(locationSelectButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(testButton)).add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel1).add(testButton).add(locationSelectButton).add(mboxField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(7, 7, 7).add(detailsTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
        pack();
    }

    private void skipAlwaysButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (currentMessage != null && currentMessage instanceof MimeMessage) {
            MimeMessage m = (MimeMessage) currentMessage;
            try {
                insertMessageID(m.getMessageID());
                msgArea.append("\nWill skip this message in next attempt");
            } catch (MessagingException me) {
                me.printStackTrace();
                msgArea.append(me.getMessage());
            } catch (SQLException sqle) {
                sqle.printStackTrace();
                msgArea.append("\n" + sqle.getMessage());
            }
        }
    }

    private void needAuthActionPerformed1(java.awt.event.ActionEvent evt) {
        smtpPassword.setEnabled(needAuth.isSelected());
    }

    private void needAuthActionPerformed(java.awt.event.ActionEvent evt) {
        smtpUser.setEnabled(needAuth.isSelected());
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (messageThread != null) {
            msgArea.append("\nAttempting to stop the current thread");
            messageThread.stop();
        }
    }

    private void folderListValueChanged(javax.swing.event.ListSelectionEvent evt) {
        Object o = folderList.getSelectedValue();
        if (o != null) {
            Folder f = (Folder) o;
            folderNameLabel.setText(f.getName());
            try {
                folderMsgCountLabel.setText(Integer.toString(f.getMessageCount()));
            } catch (MessagingException me) {
                folderMsgCountLabel.setText("NA");
            }
        }
    }

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {
        messageSendButtonUpdate(true);
        messageThread = new Thread() {

            public void run() {
                try {
                    processSelectedFolders();
                } catch (AddressException ex) {
                    JOptionPane.showMessageDialog(null, "Incorrect address specified.");
                } catch (NoSuchProviderException ex) {
                    ex.printStackTrace();
                } catch (MessagingException me) {
                    me.printStackTrace();
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                } finally {
                    messageSendButtonUpdate(false);
                    messageThread = null;
                }
            }
        };
        messageThread.start();
    }

    private void messageSendButtonUpdate(boolean start) {
        saveButton.setEnabled(!start);
        cancelButton.setEnabled(start);
        skipAlwaysButton.setEnabled(start || currentMessage != null);
        folderList.setEnabled(!start);
        detailsTabbedPane.setSelectedIndex((start) ? 1 : 0);
    }

    private void testButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (!isStoreConnected()) {
            connectStore();
            if (!isStoreConnected()) {
                return;
            }
        }
        Folder[] folders;
        try {
            folders = getFolderList();
            DefaultListModel model = (DefaultListModel) folderList.getModel();
            model.clear();
            for (Folder f : folders) {
                try {
                    f.open(Folder.READ_ONLY);
                    model.addElement(f);
                    f.close(false);
                } catch (MessagingException ex) {
                }
            }
        } catch (MessagingException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while getting folder list");
        }
        disconnectStore();
    }

    private void locationSelectButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String currentPath = getMboxField().getText();
        File definedFile = new File(currentPath);
        fileChooser.setSelectedFile(definedFile);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        int retVal = fileChooser.showOpenDialog(this);
        if (retVal == fileChooser.APPROVE_OPTION) {
            try {
                getMboxField().setText(fileChooser.getSelectedFile().getCanonicalPath());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws ClassNotFoundException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        }
        new MailRedirect().setVisible(true);
    }

    private javax.swing.JButton cancelButton;

    private javax.swing.JCheckBox compressAttachCheckBox;

    private javax.swing.JTabbedPane detailsTabbedPane;

    private javax.swing.JList folderList;

    private javax.swing.JLabel folderMsgCountLabel;

    private javax.swing.JLabel folderNameLabel;

    private javax.swing.JTextField fromTextField;

    private javax.swing.JLabel jLabel11;

    private javax.swing.JLabel jLabel12;

    private javax.swing.JLabel jLabel13;

    private javax.swing.JLabel jLabel14;

    private javax.swing.JLabel jLabel15;

    private javax.swing.JLabel jLabel16;

    private javax.swing.JPanel jPanel4;

    private javax.swing.JScrollPane jScrollPane3;

    private javax.swing.JButton locationSelectButton;

    private javax.swing.JTextField maxSizeTextField;

    private javax.swing.JTextField mboxField;

    private javax.swing.JTextArea msgArea;

    private javax.swing.JTextField msgDate;

    private javax.swing.JTextField msgFolder;

    private javax.swing.JLabel msgNumber;

    private javax.swing.JCheckBox needAuth;

    private javax.swing.JTextArea otherInfoTextArea;

    private javax.swing.JButton saveButton;

    private javax.swing.JTextField sizeTextField;

    private javax.swing.JButton skipAlwaysButton;

    private javax.swing.JTextField smtpHost;

    private javax.swing.JPasswordField smtpPassword;

    private javax.swing.JTextField smtpUser;

    private javax.swing.JLabel statusLabel;

    private javax.swing.JTextField subjectTextField;

    private javax.swing.JTextField targetEmaillAddress;

    private javax.swing.JButton testButton;

    private javax.swing.JTextField toTextField;

    private Message[] getMessageList(String folderName) throws MessagingException {
        if (isStoreConnected()) {
            Folder folder = getStore().getDefaultFolder().getFolder(folderName);
            folder.open(Folder.READ_ONLY);
            return folder.getMessages();
        }
        return null;
    }

    public Folder[] getFolderList() throws MessagingException {
        Folder defaultFolder = getStore().getDefaultFolder();
        Folder inbox = defaultFolder.getFolder("Inbox");
        Folder[] folders = defaultFolder.list();
        return folders;
    }

    public boolean isStoreConnected() {
        return ((getStore() != null) && (getStore().isConnected()));
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public javax.swing.JTextField getMboxField() {
        return mboxField;
    }

    public void setMboxField(javax.swing.JTextField mboxField) {
        this.mboxField = mboxField;
    }

    public String getMboxPath() {
        return mboxField.getText();
    }

    public String getSMTPHost() {
        return smtpHost.getText();
    }

    public String getSMTPUser() {
        return smtpUser.getText();
    }

    public char[] getSMTPPassword() {
        return smtpPassword.getPassword();
    }

    public boolean needAuth() {
        return needAuth.isSelected();
    }

    public String getTargetEmailAddress() {
        return targetEmaillAddress.getText();
    }

    public void connectStore() {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", getSMTPHost());
        Session session = Session.getDefaultInstance(props);
        Store tmpStore;
        try {
            String mboxPath = getMboxPath();
            mboxPath = mboxPath.replace('\\', '/').trim();
            if (mboxPath.length() == 0) {
                return;
            }
            tmpStore = session.getStore(new URLName("mstor:" + mboxPath));
            tmpStore.connect();
            setStore(tmpStore);
        } catch (NoSuchProviderException ex) {
            ex.printStackTrace();
        } catch (MessagingException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error while connecting");
        }
    }

    public void disconnectStore() {
        Store tmpStore = getStore();
        setStore(null);
        try {
            tmpStore.close();
        } catch (MessagingException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error while disconnecting");
        }
    }

    public void processSelectedFolders() throws AddressException, NoSuchProviderException, MessagingException, SQLException {
        final Address toAddress = new InternetAddress(getTargetEmailAddress());
        final Address[] toAddressList = new Address[] { toAddress };
        Properties props = System.getProperties();
        props.put("mail.smtp.host", getSMTPHost());
        int maxSize = Integer.parseInt(maxSizeTextField.getText());
        boolean compressAttachements = compressAttachCheckBox.isEnabled();
        Session session = null;
        Transport tr = null;
        if (!TEST_MODE) {
            session = Session.getDefaultInstance(props);
            tr = session.getTransport("smtp");
        }
        try {
            if (tr != null) {
                if (needAuth()) {
                    tr.connect(getSMTPHost(), getSMTPUser(), new String(getSMTPPassword()));
                } else {
                    tr.connect();
                }
            }
        } catch (MessagingException me) {
            me.printStackTrace();
            return;
        }
        for (Object o : folderList.getSelectedValues()) {
            Folder f = (Folder) o;
            try {
                f.open(Folder.READ_ONLY);
                msgArea.setText("");
                msgArea.append("Reading mailbox size");
                folderNameLabel.setText(f.getName());
                folderMsgCountLabel.setText("" + f.getMessageCount());
                if (lastProcessedFolder == null || !lastProcessedFolder.equals(f.getFullName())) {
                    lastProcessedFolder = f.getFullName();
                }
                int msgCount = f.getMessageCount();
                for (int i = 1; i <= msgCount; i++) {
                    Message m = null;
                    try {
                        m = f.getMessage(i);
                    } catch (MessagingException me) {
                        me.printStackTrace();
                        System.err.println("Ignoring message, trying next one.\n");
                        continue;
                    }
                    this.currentMessage = m;
                    if (compressAttachements) {
                    }
                    StringBuilder contentStr = new StringBuilder();
                    String id = null;
                    if (m instanceof MimeMessage) {
                        MimeMessage mm = (MimeMessage) m;
                        id = mm.getMessageID();
                        if (m.getSize() > maxSize && !compressAttachements) {
                        }
                    }
                    if (m.getSize() > maxSize) {
                        msgArea.append("Skipping message(" + m.getMessageNumber() + ") since over size limit\n");
                        continue;
                    }
                    if (alreadyProcessedId(id)) {
                        msgArea.append("Skipping message(" + m.getMessageNumber() + ")\n");
                        continue;
                    }
                    msgFolder.setText(m.getFolder().getFullName());
                    msgArea.append("Message sent on:" + m.getSentDate() + ". Initiating send.\n");
                    statusLabel.setText("Initiating send for message number\n");
                    Address[] fromList;
                    Address[] toList;
                    try {
                        fromList = m.getFrom();
                        toList = m.getAllRecipients();
                    } catch (AddressException ae) {
                        ae.printStackTrace();
                        msgArea.append("Skipping message since illegal from address\n");
                        statusLabel.setText("Skipping message since illegal from address\n");
                        continue;
                    }
                    if (toList == null || toList.length == 0 || fromList == null) {
                        msgArea.append("Skipping message since illegal from or to address list\n");
                        statusLabel.setText("Skipping message since illegal from or to address list\n");
                        continue;
                    }
                    List toAsList = Arrays.asList(toList);
                    fromTextField.setText(fromList[0].toString());
                    toTextField.setText(toAsList.toString());
                    subjectTextField.setText(m.getSubject());
                    msgNumber.setText("" + i + " / " + msgCount);
                    sizeTextField.setText(m.getSize() + " bytes");
                    Date sentDate = m.getSentDate();
                    if (sentDate != null) {
                        msgDate.setText(sentDate.toLocaleString());
                    } else {
                        msgDate.setText("No date available");
                    }
                    statusLabel.setText("Starting message: " + i + "\n");
                    if (tr != null) {
                        tr.sendMessage(m, toAddressList);
                        if (id != null) {
                            insertMessageID(id);
                        }
                    }
                    statusLabel.setText("Done\n");
                    try {
                        if (TEST_MODE) {
                            Thread.sleep(5 * 1000);
                        } else {
                            Thread.sleep(3 * 1000);
                        }
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            } finally {
                if (this.connection != null) {
                    try {
                        this.connection.close();
                    } catch (SQLException sqle) {
                        sqle.printStackTrace();
                    }
                }
                try {
                    f.close(false);
                } catch (MessagingException me) {
                    me.printStackTrace();
                }
            }
        }
    }

    public void insertMessageID(String messageId) throws SQLException {
        Connection c = getDBConnection();
        Statement stmt = c.createStatement();
        stmt.executeUpdate("INSERT INTO messages(msgid) values ('" + messageId + "')");
        stmt.close();
    }

    private synchronized Connection getDBConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            String userHome = System.getProperty("user.home");
            String fileName = userHome + "/.mailRedirect/userdb";
            File f = new File(fileName);
            f.getParentFile().mkdirs();
            connection = DriverManager.getConnection("jdbc:hsqldb:file:" + fileName, "sa", "");
            checkDBStructure(connection);
        }
        return this.connection;
    }

    private void checkDBStructure(Connection connection) throws SQLException {
        try {
            Statement stmt = connection.createStatement();
            stmt.execute("CREATE TABLE messages (msgid VARCHAR(255) PRIMARY KEY," + " insertDate TIMESTAMP default 'now');");
        } catch (SQLException sqle) {
            if (sqle.getErrorCode() != -21) {
                throw sqle;
            }
        }
    }

    private boolean alreadyProcessedId(String messageId) throws SQLException {
        Connection c = getDBConnection();
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * from messages where msgid = '" + messageId + "'");
        boolean retVal = rs.next();
        stmt.close();
        return retVal;
    }

    private void compressMessageAttachments(MimeMessage message) throws MessagingException, IOException {
        Renderable rm = null;
        try {
            if (message.getContentType().startsWith("text/plain")) {
                rm = new RenderablePlainText(message);
            } else {
                rm = new RenderableMessage(message);
            }
        } catch (MessagingException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        int attachmentCount = rm.getAttachmentCount();
        if (attachmentCount > 0) {
            File tempOutFile = File.createTempFile("Msg-Consolidated-Attachments", "zip");
            ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(tempOutFile));
            for (int attachment = 0; attachment < attachmentCount; attachment++) {
                Attachment a = rm.getAttachment(attachment);
                ZipEntry entry = new ZipEntry(a.getFilename());
                zipFile.putNextEntry(entry);
                zipFile.write(a.getContent());
                zipFile.closeEntry();
            }
            zipFile.close();
            MimeMultipart mp = new MimeMultipart();
            MimeBodyPart bp2 = new MimeBodyPart();
            bp2.setFileName("Msg-Consolidated-Attachments.zip");
            bp2.attachFile(tempOutFile);
            mp.addBodyPart(bp2);
            message.setContent(mp);
        }
    }
}

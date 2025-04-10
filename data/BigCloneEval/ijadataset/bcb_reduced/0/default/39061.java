import javax.swing.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.awt.*;
import java.io.*;

/**
 * Create Hashing for text n files
 * Use AES encryption for hash encryption
 *
 */
public class integrity extends javax.swing.JFrame {

    KeyGenerator keygen;

    SecretKey myKey;

    Cipher cipher;

    MessageDigest myDigest;

    byte[] cipherbytes, digestbytes;

    String cipheralgorithm = "AES", mdalgorithm = "MD5";

    private void saveHashFiles() {
        try {
            FileWriter outhash = new FileWriter(new File("hash.txt"));
            FileWriter outenchash = new FileWriter(new File("enchash.txt"));
            for (int i = 0; i < digestbytes.length; i++) {
                outhash.write(digestbytes[i]);
            }
            outhash.close();
            for (int i = 0; i < cipherbytes.length; i++) {
                outenchash.write(cipherbytes[i]);
            }
            outenchash.close();
            lblStatus.setText("Status: save file completed successfully");
        } catch (Exception e) {
            lblStatus.setText("Status: Error: " + e.getMessage());
        }
    }

    private void loadTextFile() {
        BufferedReader infile;
        FileDialog ofd = new FileDialog(this, "Open");
        ofd.setVisible(true);
        String filename = ofd.getDirectory() + ofd.getFile();
        if (filename != null) {
            try {
                infile = new BufferedReader(new FileReader(filename));
                String line = "";
                txtCleartext.setText("");
                while (line != null) {
                    line = infile.readLine();
                    if (line != null) {
                        txtCleartext.append(line + "\n");
                    }
                }
                lblStatus.setText("Status: Loaded file: " + ofd.getFile());
            } catch (Exception e) {
                lblStatus.setText("Status: Error: " + e.getMessage());
            }
        }
    }

    private void doHash() {
        try {
            keygen = KeyGenerator.getInstance(cipheralgorithm);
            myKey = keygen.generateKey();
            myDigest = MessageDigest.getInstance(mdalgorithm);
            digestbytes = myDigest.digest(txtCleartext.getText().getBytes());
            txtHash.setText(new String(digestbytes));
            cipher = Cipher.getInstance(cipheralgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, myKey);
            cipherbytes = myDigest.digest();
            txtEncryptedHash.setText(new String(cipherbytes));
            lblStatus.setText("Status: Hash computed and encrypted");
        } catch (Exception e) {
            lblStatus.setText("Status: Error - Exception: " + e.getMessage());
        }
    }

    /** Creates new form intergrity */
    public integrity() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtCleartext = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        btnHash = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtHash = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtEncryptedHash = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ITK3:SÄK integrity");
        jButton1.setText("Load");
        jButton1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);
        jButton2.setText("Save");
        jButton2.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);
        getContentPane().add(jToolBar1, java.awt.BorderLayout.NORTH);
        jPanel3.setLayout(new java.awt.GridLayout(4, 1));
        jPanel3.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(3, 3, 3, 3)));
        jPanel1.setLayout(new java.awt.BorderLayout());
        jScrollPane1.setViewportView(txtCleartext);
        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jPanel3.add(jPanel1);
        btnHash.setText("Create Hash");
        btnHash.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHashActionPerformed(evt);
            }
        });
        jPanel4.add(btnHash);
        jPanel3.add(jPanel4);
        jPanel2.setLayout(new java.awt.BorderLayout());
        jScrollPane2.setViewportView(txtHash);
        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);
        jLabel2.setText("Hash (vanilla):");
        jPanel2.add(jLabel2, java.awt.BorderLayout.NORTH);
        jPanel3.add(jPanel2);
        jPanel5.setLayout(new java.awt.BorderLayout());
        jScrollPane3.setViewportView(txtEncryptedHash);
        jPanel5.add(jScrollPane3, java.awt.BorderLayout.CENTER);
        jLabel3.setText("Hash (encrypted):");
        jPanel5.add(jLabel3, java.awt.BorderLayout.NORTH);
        jPanel3.add(jPanel5);
        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);
        lblStatus.setText("Status:");
        getContentPane().add(lblStatus, java.awt.BorderLayout.SOUTH);
        jMenu1.setText("File");
        jMenuItem1.setText("Exit");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);
        jMenuBar1.add(jMenu1);
        setJMenuBar(jMenuBar1);
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 400) / 2, (screenSize.height - 300) / 2, 400, 300);
    }

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {
        System.exit(0);
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        saveHashFiles();
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        loadTextFile();
    }

    private void btnHashActionPerformed(java.awt.event.ActionEvent evt) {
        doHash();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new integrity().setVisible(true);
            }
        });
    }

    private javax.swing.JButton btnHash;

    private javax.swing.JButton jButton1;

    private javax.swing.JButton jButton2;

    private javax.swing.JLabel jLabel2;

    private javax.swing.JLabel jLabel3;

    private javax.swing.JMenu jMenu1;

    private javax.swing.JMenuBar jMenuBar1;

    private javax.swing.JMenuItem jMenuItem1;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JPanel jPanel2;

    private javax.swing.JPanel jPanel3;

    private javax.swing.JPanel jPanel4;

    private javax.swing.JPanel jPanel5;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JScrollPane jScrollPane2;

    private javax.swing.JScrollPane jScrollPane3;

    private javax.swing.JToolBar jToolBar1;

    private javax.swing.JLabel lblStatus;

    private javax.swing.JTextArea txtCleartext;

    private javax.swing.JTextArea txtEncryptedHash;

    private javax.swing.JTextArea txtHash;
}

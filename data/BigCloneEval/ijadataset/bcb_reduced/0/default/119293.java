import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.security.*;
import java.security.spec.*;
import com.sun.crypto.provider.SunJCE;
import javax.swing.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class EncipherDecipher extends JFrame {

    private static final byte[] salt = { (byte) 0xf5, (byte) 0x33, (byte) 0x01, (byte) 0x2a, (byte) 0xb2, (byte) 0xcc, (byte) 0xe4, (byte) 0x7f };

    private int iterationCount = 100;

    private JTextField passwordTextField;

    private JTextField fileNameTextField;

    private JEditorPane fileContentsEditorPane;

    public EncipherDecipher() {
        Security.addProvider(new SunJCE());
        setSize(new Dimension(400, 400));
        setTitle("Encryption and Decryption Example");
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        topPanel.setLayout(new BorderLayout());
        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new GridLayout(2, 1));
        JLabel passwordLabel = new JLabel(" Password: ");
        JLabel fileNameLabel = new JLabel(" File Name: ");
        labelsPanel.add(fileNameLabel);
        labelsPanel.add(passwordLabel);
        topPanel.add(labelsPanel, BorderLayout.WEST);
        JPanel textFieldsPanel = new JPanel();
        textFieldsPanel.setLayout(new GridLayout(2, 1));
        passwordTextField = new JPasswordField();
        fileNameTextField = new JTextField();
        textFieldsPanel.add(fileNameTextField);
        textFieldsPanel.add(passwordTextField);
        topPanel.add(textFieldsPanel, BorderLayout.CENTER);
        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BorderLayout());
        JLabel fileContentsLabel = new JLabel();
        fileContentsLabel.setText(" File Contents");
        middlePanel.add(fileContentsLabel, BorderLayout.NORTH);
        fileContentsEditorPane = new JEditorPane();
        middlePanel.add(new JScrollPane(fileContentsEditorPane), BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel();
        JButton encryptButton = new JButton("Encrypt and Write to File");
        encryptButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                encryptAndWriteToFile();
            }
        });
        bottomPanel.add(encryptButton);
        JButton decryptButton = new JButton("Read from File and Decrypt");
        decryptButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                readFromFileAndDecrypt();
            }
        });
        bottomPanel.add(decryptButton);
        JPanel contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(middlePanel, BorderLayout.CENTER);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void encryptAndWriteToFile() {
        String originalText = fileContentsEditorPane.getText();
        String password = passwordTextField.getText();
        String fileName = fileNameTextField.getText();
        Cipher cipher = null;
        try {
            PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey secretKey = keyFactory.generateSecret(keySpec);
            PBEParameterSpec parameterSpec = new PBEParameterSpec(salt, iterationCount);
            cipher = Cipher.getInstance("PBEWithMD5AndDES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        } catch (NoSuchAlgorithmException exception) {
            exception.printStackTrace();
            System.exit(1);
        } catch (InvalidKeySpecException exception) {
            exception.printStackTrace();
            System.exit(1);
        } catch (InvalidKeyException exception) {
            exception.printStackTrace();
            System.exit(1);
        } catch (NoSuchPaddingException exception) {
            exception.printStackTrace();
            System.exit(1);
        } catch (InvalidAlgorithmParameterException exception) {
            exception.printStackTrace();
            System.exit(1);
        }
        byte[] outputArray = null;
        try {
            outputArray = originalText.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException exception) {
            exception.printStackTrace();
            System.exit(1);
        }
        File file = new File(fileName);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (IOException exception) {
            exception.printStackTrace();
            System.exit(1);
        }
        CipherOutputStream out = new CipherOutputStream(fileOutputStream, cipher);
        try {
            out.write(outputArray);
            out.flush();
            out.close();
        } catch (IOException exception) {
            exception.printStackTrace();
            System.exit(1);
        }
        Vector fileBytes = new Vector();
        try {
            FileInputStream in = new FileInputStream(file);
            byte contents;
            while (in.available() > 0) {
                contents = (byte) in.read();
                fileBytes.add(new Byte(contents));
            }
            in.close();
        } catch (IOException exception) {
            exception.printStackTrace();
            System.exit(1);
        }
        byte[] encryptedText = new byte[fileBytes.size()];
        for (int i = 0; i < fileBytes.size(); i++) {
            encryptedText[i] = ((Byte) fileBytes.elementAt(i)).byteValue();
        }
        fileContentsEditorPane.setText(new String(encryptedText));
    }

    private void readFromFileAndDecrypt() {
        Vector fileBytes = new Vector();
        String password = passwordTextField.getText();
        String fileName = fileNameTextField.getText();
        Cipher cipher = null;
        try {
            PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey secretKey = keyFactory.generateSecret(keySpec);
            PBEParameterSpec parameterSpec = new PBEParameterSpec(salt, iterationCount);
            cipher = Cipher.getInstance("PBEWithMD5AndDES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        } catch (NoSuchAlgorithmException exception) {
            exception.printStackTrace();
            System.exit(1);
        } catch (InvalidKeySpecException exception) {
            exception.printStackTrace();
            System.exit(1);
        } catch (InvalidKeyException exception) {
            exception.printStackTrace();
            System.exit(1);
        } catch (NoSuchPaddingException exception) {
            exception.printStackTrace();
            System.exit(1);
        } catch (InvalidAlgorithmParameterException exception) {
            exception.printStackTrace();
            System.exit(1);
        }
        try {
            File file = new File(fileName);
            FileInputStream fileInputStream = new FileInputStream(file);
            CipherInputStream in = new CipherInputStream(fileInputStream, cipher);
            byte contents = (byte) in.read();
            while (contents != -1) {
                fileBytes.add(new Byte(contents));
                contents = (byte) in.read();
            }
            in.close();
        } catch (IOException exception) {
            exception.printStackTrace();
            System.exit(1);
        }
        byte[] decryptedText = new byte[fileBytes.size()];
        for (int i = 0; i < fileBytes.size(); i++) {
            decryptedText[i] = ((Byte) fileBytes.elementAt(i)).byteValue();
        }
        fileContentsEditorPane.setText(new String(decryptedText));
    }

    public static void main(String[] args) {
        EncipherDecipher crypto = new EncipherDecipher();
        crypto.validate();
        crypto.setVisible(true);
    }
}

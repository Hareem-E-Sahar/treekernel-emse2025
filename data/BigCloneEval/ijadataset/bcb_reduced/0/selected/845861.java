package com.hs.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * @author <a href="mailto:johnny@meta4-group.com">Johnny</a>
 *
 */
public class Writer extends JFrame implements ActionListener {

    private static final long serialVersionUID = 4151804539925328508L;

    private JLabel enterpriseNameLabel;

    private JTextField enterpriseNameTextField;

    private JButton submitButton;

    private JTextField filePathTextField;

    private JLabel filePathLable;

    private JTextField expireTimeTextField;

    private JLabel expireTimeLable;

    public Writer() {
        super();
        this.setSize(400, 300);
        this.getContentPane().setLayout(null);
        this.add(new JLabel("sdafsdf"), null);
        this.setTitle("SN creater");
        this.add(getEnterpriseNameLabel(), null);
        this.add(getEnterpriseNameTextField(), null);
        this.add(getSubmitButton(), null);
        this.add(getExpiereTime(), null);
        this.add(getFilePath());
        this.add(getFilePathLable());
        this.add(getExpireTimeLable());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private JLabel getEnterpriseNameLabel() {
        if (enterpriseNameLabel == null) {
            enterpriseNameLabel = new JLabel();
            enterpriseNameLabel.setBounds(20, 20, 120, 20);
            enterpriseNameLabel.setText("Enterprise Name:");
        }
        return enterpriseNameLabel;
    }

    private JTextField getEnterpriseNameTextField() {
        if (enterpriseNameTextField == null) {
            enterpriseNameTextField = new JTextField();
            enterpriseNameTextField.setBounds(120, 20, 220, 20);
        }
        return enterpriseNameTextField;
    }

    private JTextField getFilePath() {
        if (filePathTextField == null) {
            filePathTextField = new JTextField();
            filePathTextField.setBounds(120, 50, 220, 20);
        }
        return filePathTextField;
    }

    private JTextField getExpiereTime() {
        if (expireTimeTextField == null) {
            expireTimeTextField = new JTextField();
            expireTimeTextField.setBounds(120, 80, 220, 20);
        }
        return expireTimeTextField;
    }

    private JLabel getFilePathLable() {
        if (filePathLable == null) {
            filePathLable = new JLabel();
            filePathLable.setBounds(20, 50, 120, 20);
            filePathLable.setText("File path:");
        }
        return filePathLable;
    }

    private JLabel getExpireTimeLable() {
        if (expireTimeLable == null) {
            expireTimeLable = new JLabel();
            expireTimeLable.setBounds(20, 80, 120, 20);
            expireTimeLable.setText("Expire time:");
        }
        return expireTimeLable;
    }

    private JButton getSubmitButton() {
        if (submitButton == null) {
            submitButton = new JButton();
            submitButton.setBounds(90, 140, 71, 27);
            submitButton.setText("OK");
            submitButton.addActionListener(this);
        }
        return submitButton;
    }

    public void actionPerformed(ActionEvent e) {
        Date expireDate = null;
        File file = null;
        String enterpriseName = enterpriseNameTextField.getText();
        String expireTime = expireTimeTextField.getText();
        String filePath = filePathTextField.getText();
        long expireMills = 0L;
        if (!"-1".equals(expireTime)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                expireDate = sdf.parse(expireTime);
                expireMills = expireDate.getTime();
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        } else {
            expireMills = -1L;
        }
        if (expireMills != 0L && filePath != null && !filePath.trim().equals("") && enterpriseName != null && !enterpriseName.trim().equals("")) {
            EnterpriseImpl s = new EnterpriseImpl(expireMills, enterpriseName);
            file = new File(filePath);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                ZipOutputStream zos = new ZipOutputStream(fos);
                ZipEntry ze = new ZipEntry(expireMills + "");
                zos.putNextEntry(ze);
                ObjectOutputStream oos = new ObjectOutputStream(zos);
                oos.writeObject(s);
                oos.close();
                zos.close();
                fos.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        this.dispose();
    }

    public static void main(String[] args) throws IOException {
        Writer w = new Writer();
        w.setVisible(true);
    }
}

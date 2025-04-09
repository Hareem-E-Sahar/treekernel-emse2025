package es.caib.signatura.client.swing;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JComboBox;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import es.caib.signatura.api.Signature;
import es.caib.signatura.api.SignatureCertNotFoundException;
import es.caib.signatura.api.SignaturePrivKeyException;

public class SignaturaFramePDF extends JFrame {

    private static final long serialVersionUID = 1L;

    private String contentType;

    private es.caib.signatura.api.Signer signer = null;

    private SignaturaClientProperties signaturaProperties;

    private ImageIcon imageOpen = null;

    private JPanel jContentPane = null;

    private JToolBar jToolBar = null;

    private JPanel jPanel = null;

    private JButton jButton_help = null;

    private JLabel jLabel_inputPath = null;

    private JButton jButton_openInputFile = null;

    private JTextField jTextField_URL = null;

    private JTextField jTextField_inputPath = null;

    private JLabel jLabel_URL = null;

    private JScrollPane jScrollPane_certList = null;

    private JList jList_certList = null;

    private JLabel jLabel_certPasswd = null;

    private JPasswordField jPasswordField_certPasswd = null;

    private JLabel jLabel_certList = null;

    private JComboBox jComboBox_position = null;

    private JLabel jLabel_position = null;

    private JButton jButton_signPDF = null;

    private JLabel jLabel_outputPath = null;

    private JTextField jTextField_outputPath = null;

    private JButton jButton_openOutputFile = null;

    private JFileChooser chooser;

    public SignaturaFramePDF(String contentType) throws HeadlessException {
        super();
        this.contentType = contentType;
        try {
            signaturaProperties = new SignaturaClientProperties();
        } catch (Exception e) {
            e.printStackTrace();
        }
        initialize();
        initSignature();
    }

    /**
	 * This method initializes this
	 * 
	 * @return void
	 */
    private void initialize() {
        this.setSize(675, 421);
        this.setContentPane(getJContentPane());
        this.setTitle(signaturaProperties.getProperty("signpdf"));
    }

    /**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJToolBar(), BorderLayout.NORTH);
            jContentPane.add(getJPanel(), BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
	 * This method initializes jToolBar	
	 * 	
	 * @return javax.swing.JToolBar	
	 */
    private JToolBar getJToolBar() {
        if (jToolBar == null) {
            jToolBar = new JToolBar();
            jToolBar.add(getJButton_help());
        }
        return jToolBar;
    }

    /**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jLabel_outputPath = new JLabel();
            jLabel_outputPath.setBounds(new Rectangle(14, 195, 572, 16));
            jLabel_outputPath.setText(signaturaProperties.getProperty("jLabel_outputPath"));
            jLabel_position = new JLabel();
            jLabel_position.setBounds(new Rectangle(15, 286, 375, 15));
            jLabel_position.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
            jLabel_position.setText(signaturaProperties.getProperty("jLabel_position"));
            jLabel_certList = new JLabel();
            jLabel_certList.setBounds(new Rectangle(15, 12, 434, 18));
            jLabel_certList.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
            jLabel_certList.setText(signaturaProperties.getProperty("jLabel_certList"));
            jLabel_certPasswd = new JLabel();
            jLabel_certPasswd.setBounds(new Rectangle(16, 104, 217, 17));
            jLabel_certPasswd.setText(signaturaProperties.getProperty("jLabel_certPasswd"));
            jLabel_URL = new JLabel();
            jLabel_URL.setBounds(new Rectangle(13, 240, 575, 16));
            jLabel_URL.setText(signaturaProperties.getProperty("jLabel_URL"));
            jLabel_inputPath = new JLabel();
            jLabel_inputPath.setBounds(new Rectangle(14, 150, 573, 16));
            jLabel_inputPath.setText(signaturaProperties.getProperty("jLabel_inputPath"));
            jPanel = new JPanel();
            jPanel.setLayout(null);
            jPanel.add(jLabel_inputPath, null);
            jPanel.add(getJButton_openInputFile(), null);
            jPanel.add(getJTextField_URL(), null);
            jPanel.add(getJTextField_inputPath(), null);
            jPanel.add(jLabel_URL, null);
            jPanel.add(getJScrollPane_certList(), null);
            jPanel.add(jLabel_certPasswd, null);
            jPanel.add(getjPasswordField_certPasswd(), null);
            jPanel.add(jLabel_certList, null);
            jPanel.add(getJComboBox_position(), null);
            jPanel.add(jLabel_position, null);
            jPanel.add(getJButton_signPDF(), null);
            jPanel.add(jLabel_outputPath, null);
            jPanel.add(getJTextField_outputPath(), null);
            jPanel.add(getJButton_openOutputFile(), null);
        }
        return jPanel;
    }

    /**
	 * This method initializes jButton_help	
	 * 	
	 * @return javax.swing.JButton	
	 */
    private JButton getJButton_help() {
        if (jButton_help == null) {
            jButton_help = new JButton();
            jButton_help.setToolTipText("About");
            jButton_help.setText("Sobre...");
            jButton_help.addMouseListener(new java.awt.event.MouseAdapter() {

                public void mouseClicked(MouseEvent e) {
                    jButton_help_mouseClicked(e);
                }
            });
        }
        return jButton_help;
    }

    /**
	 * This method initializes jButton_openInputFile	
	 * 	
	 * @return javax.swing.JButton	
	 */
    private JButton getJButton_openInputFile() {
        if (jButton_openInputFile == null) {
            jButton_openInputFile = new JButton();
            imageOpen = new ImageIcon(SignaturaFrameSign.class.getResource("openfile.gif"));
            jButton_openInputFile.setIcon(imageOpen);
            jButton_openInputFile.setBounds(new Rectangle(597, 165, 47, 21));
            jButton_openInputFile.addMouseListener(new java.awt.event.MouseAdapter() {

                public void mouseClicked(MouseEvent e) {
                    jButton_openInputFile_mouseClicked(e);
                }
            });
        }
        return jButton_openInputFile;
    }

    /**
	 * This method initializes jTextField_URL	
	 * 	
	 * @return javax.swing.JTextField	
	 */
    private JTextField getJTextField_URL() {
        if (jTextField_URL == null) {
            jTextField_URL = new JTextField();
            jTextField_URL.setBounds(new Rectangle(13, 255, 576, 20));
        }
        return jTextField_URL;
    }

    /**
	 * This method initializes jTextField_inputPath	
	 * 	
	 * @return javax.swing.JTextField	
	 */
    private JTextField getJTextField_inputPath() {
        if (jTextField_inputPath == null) {
            jTextField_inputPath = new JTextField();
            jTextField_inputPath.setBounds(new Rectangle(14, 166, 574, 20));
        }
        return jTextField_inputPath;
    }

    /**
	 * This method initializes jScrollPane_certList	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
    private JScrollPane getJScrollPane_certList() {
        if (jScrollPane_certList == null) {
            jScrollPane_certList = new JScrollPane();
            jScrollPane_certList.setBounds(new Rectangle(15, 30, 436, 61));
            jScrollPane_certList.setViewportView(getJList_certList());
        }
        return jScrollPane_certList;
    }

    /**
	 * This method initializes jList_certList	
	 * 	
	 * @return javax.swing.JList	
	 */
    private JList getJList_certList() {
        if (jList_certList == null) {
            jList_certList = new JList();
        }
        return jList_certList;
    }

    /**
	 * This method initializes jPasswordField_certPasswd	
	 * 	
	 * @return javax.swing.JTextField	
	 */
    private JTextField getjPasswordField_certPasswd() {
        if (jPasswordField_certPasswd == null) {
            jPasswordField_certPasswd = new JPasswordField();
            jPasswordField_certPasswd.setBounds(new Rectangle(15, 120, 219, 20));
        }
        return jPasswordField_certPasswd;
    }

    /**
	 * This method initializes jComboBox_position	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
    private JComboBox getJComboBox_position() {
        if (jComboBox_position == null) {
            String[] positionsList = { signaturaProperties.getProperty("position.Top"), signaturaProperties.getProperty("position.Bottom"), signaturaProperties.getProperty("position.Right"), signaturaProperties.getProperty("position.Left") };
            jComboBox_position = new JComboBox(positionsList);
            jComboBox_position.setBounds(new Rectangle(391, 285, 197, 17));
        }
        return jComboBox_position;
    }

    /**
	 * This method initializes jButton_signPDF	
	 * 	
	 * @return javax.swing.JButton	
	 */
    private JButton getJButton_signPDF() {
        if (jButton_signPDF == null) {
            jButton_signPDF = new JButton();
            jButton_signPDF.setBounds(new Rectangle(240, 312, 153, 24));
            jButton_signPDF.setText("Firmar y estampar");
            jButton_signPDF.addMouseListener(new MouseAdapter() {

                public void mouseClicked(MouseEvent e) {
                    jButton_signPDF_mouseClicked(e);
                }
            });
        }
        return jButton_signPDF;
    }

    private void jButton_help_mouseClicked(MouseEvent e) {
        showMessageBox("about");
    }

    private void showMessageBox(String message) {
        JOptionPane.showMessageDialog(null, signaturaProperties.getProperty(message), "Govern Balear: Signatura Digital", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showMessageError(String message) {
        JOptionPane.showMessageDialog(null, signaturaProperties.getProperty(message), "Govern Balear: Signatura Digital", JOptionPane.ERROR_MESSAGE);
    }

    private void showMessageError(String userDefinedMessage, String message) {
        JOptionPane.showMessageDialog(null, signaturaProperties.getProperty(message) + "\n" + userDefinedMessage, "Govern Balear: Signatura Digital", JOptionPane.ERROR_MESSAGE);
    }

    private void initSignature() {
        signer = ClientSignerFactory.getSigner();
        if (signer == null) {
            showMessageError("NoProvider");
            return;
        }
        String[] certList = { signaturaProperties.getProperty("certListNotFound") };
        jList_certList.setListData(certList);
        try {
            certList = signer.getCertList(contentType);
        } catch (SignaturePrivKeyException ex) {
            showMessageError("certListNotFound");
            ex.printStackTrace();
            return;
        } catch (SignatureCertNotFoundException cex) {
            showMessageError("certListNotFound");
            cex.printStackTrace();
            return;
        }
        if (certList != null) {
            jList_certList.setListData(certList);
        }
        jList_certList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void jButton_signPDF_mouseClicked(MouseEvent e) {
        jButton_signPDF();
    }

    private void jButton_signPDF() {
        String certName = (String) jList_certList.getSelectedValue();
        if (certName == null) {
            showMessageBox("selectCert");
            return;
        }
        if (jTextField_inputPath.getText().equalsIgnoreCase("") || jTextField_outputPath.getText().equalsIgnoreCase("")) {
            showMessageBox("selectDocumentFile");
            return;
        }
        InputStream inStream = null;
        try {
            inStream = new BufferedInputStream(new FileInputStream(jTextField_inputPath.getText()));
        } catch (FileNotFoundException e) {
            showMessageError(jTextField_inputPath.getText(), "documentFileNotFound");
            e.printStackTrace();
            return;
        }
        OutputStream outStream = null;
        OutputStream pdfOutStream = null;
        try {
            outStream = new FileOutputStream(jTextField_outputPath.getText());
        } catch (FileNotFoundException e) {
            showMessageError("documentCannotCreate");
            e.printStackTrace();
            return;
        }
        try {
            pdfOutStream = signer.signPDF(inStream, certName, jPasswordField_certPasswd.getText(), contentType, jTextField_URL.getText(), jComboBox_position.getSelectedIndex() + 1);
        } catch (FileNotFoundException fe) {
            showMessageError("documentFileNotFound");
        } catch (IOException io) {
            showMessageError("networkError");
        } catch (Exception se) {
            se.printStackTrace();
            Throwable root = se;
            while (root.getCause() != null && root.getCause() != root) root = root.getCause();
            JOptionPane.showMessageDialog(null, signaturaProperties.getProperty("signatureError") + se.toString(), "Govern Balear: Signatura Digital", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                inStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            outStream.write(((ByteArrayOutputStream) pdfOutStream).toByteArray());
        } catch (IOException e1) {
            e1.printStackTrace();
            showMessageError("documentCannotCreate");
        }
        try {
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
	 * This method initializes jTextField_outputPath	
	 * 	
	 * @return javax.swing.JTextField	
	 */
    private JTextField getJTextField_outputPath() {
        if (jTextField_outputPath == null) {
            jTextField_outputPath = new JTextField();
            jTextField_outputPath.setBounds(new Rectangle(14, 211, 574, 20));
        }
        return jTextField_outputPath;
    }

    /**
	 * This method initializes jButton_openOutputFile	
	 * 	
	 * @return javax.swing.JButton	
	 */
    private JButton getJButton_openOutputFile() {
        if (jButton_openOutputFile == null) {
            jButton_openOutputFile = new JButton();
            imageOpen = new ImageIcon(SignaturaFrameSign.class.getResource("openfile.gif"));
            jButton_openOutputFile.setIcon(imageOpen);
            jButton_openOutputFile.setBounds(new Rectangle(599, 210, 48, 20));
            jButton_openOutputFile.addMouseListener(new java.awt.event.MouseAdapter() {

                public void mouseClicked(MouseEvent e) {
                    jButton_openOutputFile_mouseClicked(e);
                }
            });
        }
        return jButton_openOutputFile;
    }

    private JFileChooser getChooser() {
        if (chooser == null) {
            FileFilter ff = new FileFilter() {

                public boolean accept(File f) {
                    if (f.getName().toLowerCase().endsWith("pdf")) return true; else return false;
                }

                public String getDescription() {
                    return "Adobe acrobat document (*.pdf)";
                }
            };
            chooser = new JFileChooser();
            chooser.setFileFilter(ff);
        }
        return chooser;
    }

    private void jButton_openOutputFile_mouseClicked(MouseEvent e) {
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = getChooser().getSelectedFile();
            jTextField_outputPath.setText(file.getAbsolutePath());
        }
    }

    private void jButton_openInputFile_mouseClicked(MouseEvent e) {
        int returnVal = getChooser().showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            jTextField_inputPath.setText(file.getAbsolutePath());
        }
    }
}

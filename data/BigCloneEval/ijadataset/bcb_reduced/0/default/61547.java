import org.chir.swing.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

/**
 *
 * @author  yo
 */
public class JarWizard extends javax.swing.JFrame {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new JarWizard().show();
    }

    class MyCellRenderer implements ListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = (Component) value;
            if (isSelected) {
                c.setBackground(list.getSelectionBackground());
                c.setForeground(list.getSelectionForeground());
            } else {
                c.setBackground(list.getBackground());
                c.setForeground(list.getForeground());
            }
            c.setEnabled(list.isEnabled());
            c.setFont(list.getFont());
            return c;
        }
    }

    /** Creates a new form JarWizard */
    public JarWizard() {
        super("JAR Creation Wizard v2.00");
        setIconImage(loadImage("gui/icon.gif"));
        setResizable(false);
        initComponents();
        JLabel[] listData = { new JLabel("Application ", new ImageIcon(getClass().getResource("gui/application.gif")), SwingConstants.LEFT), new JLabel("Bean", new ImageIcon(getClass().getResource("gui/bean.gif")), SwingConstants.LEFT) };
        listData[0].setOpaque(true);
        listData[1].setOpaque(true);
        jList1.setListData(listData);
        jList1.setCellRenderer(new MyCellRenderer());
        m_wPanel0 = new WizardPanel0();
        m_wPanel1 = new WizardPanel1();
        m_wPanel2 = new WizardPanel2();
        m_wPanel3 = new WizardPanel3();
        wizard1.addWizardPanel(m_wPanel0);
        wizard1.addWizardPanel(m_wPanel1);
        wizard1.addWizardPanel(m_wPanel2);
        wizard1.addWizardPanel(m_wPanel3);
        FileDirectoryFilter dfilter = new FileDirectoryFilter();
        m_fc1 = new JFileChooser(new File("."));
        m_fc1.setFileFilter(dfilter);
        m_fc1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        m_fc2 = new JFileChooser(new File("."));
        m_fc2.setFileFilter(new FileExtensionFilter(".class", "Java Class (*.class)"));
        m_fc3 = new JFileChooser(new File("."));
        m_fc3.setDialogType(JFileChooser.SAVE_DIALOG);
        m_fc3.setFileFilter(new FileExtensionFilter(".jar", "Java JAR (*.jar)"));
        wizard1.setNextText(ResourceBundle.getBundle("jarwizard_res").getString("IDS_NEXT"));
        wizard1.setBackText(ResourceBundle.getBundle("jarwizard_res").getString("IDS_BACK"));
        wizard1.setFinishText(ResourceBundle.getBundle("jarwizard_res").getString("IDS_FINISH"));
        dirLabel.setText(ResourceBundle.getBundle("jarwizard_res").getString("IDS_BASE_DIR"));
        classLabel.setText(ResourceBundle.getBundle("jarwizard_res").getString("IDS_MAIN_CLASS"));
        jarLabel.setText(ResourceBundle.getBundle("jarwizard_res").getString("IDS_JAR_FILE"));
        dirText.setToolTipText(ResourceBundle.getBundle("jarwizard_res").getString("IDS_BASE_DIR_TOOLTIP"));
        classText.setToolTipText(ResourceBundle.getBundle("jarwizard_res").getString("IDS_MAIN_CLASS_TOOLTIP"));
        jarText.setToolTipText(ResourceBundle.getBundle("jarwizard_res").getString("IDS_JAR_FILE_TOOLTIP"));
        jLabel11.setText(ResourceBundle.getBundle("jarwizard_res").getString("IDS_FILL_IN_1"));
        jLabel112.setText(ResourceBundle.getBundle("jarwizard_res").getString("IDS_FILL_IN_2"));
        m_fc1.setDialogTitle(ResourceBundle.getBundle("jarwizard_res").getString("IDS_BASE_DIR"));
        m_fc2.setDialogTitle(ResourceBundle.getBundle("jarwizard_res").getString("IDS_MAIN_CLASS"));
        m_fc3.setDialogTitle(ResourceBundle.getBundle("jarwizard_res").getString("IDS_JAR_FILE"));
        dfilter.setDescription(ResourceBundle.getBundle("jarwizard_res").getString("IDS_DIRECTORIES"));
        dirButton.setToolTipText(ResourceBundle.getBundle("jarwizard_res").getString("IDS_BROWSE"));
        classButton.setToolTipText(ResourceBundle.getBundle("jarwizard_res").getString("IDS_BROWSE"));
        classButton1.setToolTipText(ResourceBundle.getBundle("jarwizard_res").getString("IDS_BROWSE"));
        jLabel3.setText(ResourceBundle.getBundle("jarwizard_res").getString("IDS_WAIT"));
        m_listHelpText = new String[2];
        m_listHelpText[0] = ResourceBundle.getBundle("jarwizard_res").getString("IDS_WELCOME_APPLICATION");
        m_listHelpText[1] = ResourceBundle.getBundle("jarwizard_res").getString("IDS_WELCOME_BEAN");
        jLabel2.setText(ResourceBundle.getBundle("jarwizard_res").getString("IDS_WELCOME"));
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        int x0 = (screenDim.width - getSize().width) / 2, y0 = (screenDim.height - getSize().height) / 2;
        int x = x0, y = y0;
        try {
            m_properties.load(new FileInputStream("JarWizard.ini"));
            x = Integer.parseInt(m_properties.getProperty("WINDOW_X"));
            y = Integer.parseInt(m_properties.getProperty("WINDOW_Y"));
            jList1.setSelectedIndex(Integer.parseInt(m_properties.getProperty("JAR_TYPE")));
            setDirText(m_properties.getProperty("DIRECTORY"));
            setClassText(m_properties.getProperty("MAIN_CLASS"));
            setJarText(m_properties.getProperty("JAR_FILE"));
        } catch (IOException e) {
        } catch (NumberFormatException e) {
        }
        setLocation(x, y);
        pack();
    }

    /** First page */
    class WizardPanel0 extends WizardPanel {

        public WizardPanel0() {
            super(jPanel6);
            jList1.addListSelectionListener(wizard1);
        }

        public boolean canMoveNext() {
            return (!jList1.isSelectionEmpty());
        }

        public void showPanel() {
            jList1.requestFocus();
        }
    }

    /** Directory & mainclass page */
    class WizardPanel1 extends WizardPanel {

        public WizardPanel1() {
            super(jPanel1);
            classText.addKeyListener(wizard1);
            dirText.addKeyListener(wizard1);
            classText.getDocument().addDocumentListener(wizard1);
            dirText.getDocument().addDocumentListener(wizard1);
        }

        public boolean canMoveNext() {
            return (!dirText.getText().trim().equals("") && classText.getText().trim().endsWith(".class"));
        }

        public void showPanel() {
            dirText.requestFocus();
        }
    }

    /** Jarfile page */
    class WizardPanel2 extends WizardPanel {

        public WizardPanel2() {
            super(jPanel2);
            jarText.addKeyListener(wizard1);
            jarText.getDocument().addDocumentListener(wizard1);
        }

        public boolean canMoveNext() {
            return (jarText.getText().trim().toLowerCase().endsWith(".jar"));
        }

        public boolean moveNext() {
            classText.setText(classText.getText().trim());
            dirText.setText(dirText.getText().trim());
            if (classText.getText().equals("") || dirText.getText().equals("")) {
                JOptionPane.showMessageDialog(this, ResourceBundle.getBundle("jarwizard_res").getString("IDS_BOTH_ERROR"), "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (!classText.getText().startsWith(dirText.getText())) {
                JOptionPane.showMessageDialog(this, ResourceBundle.getBundle("jarwizard_res").getString("IDS_SUBDIR_ERROR"), "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new JarringThread(jarringDialog, dirText.getText(), m_fc3.getSelectedFile().getAbsolutePath(), classText.getText(), (jList1.getSelectedIndex() == 1)).start();
            jarringDialog.pack();
            Dimension screenDim = getSize();
            int dx = (screenDim.width - jarringDialog.getSize().width) / 2, dy = (screenDim.height - jarringDialog.getSize().height) / 2;
            jarringDialog.setLocation(getLocationOnScreen().x + dx, getLocationOnScreen().y + dy);
            jarringDialog.show();
            boolean b;
            if (m_errorDescription == null) {
                jLabel111.setText(ResourceBundle.getBundle("jarwizard_res").getString("IDS_CONGRATS_1") + m_fc3.getSelectedFile().getAbsolutePath() + ResourceBundle.getBundle("jarwizard_res").getString("IDS_CONGRATS_2"));
                Toolkit.getDefaultToolkit().beep();
                b = true;
            } else {
                JOptionPane.showMessageDialog(this, "Jar Creation error\n\n" + m_errorDescription + "\n\n", "Error", JOptionPane.ERROR_MESSAGE);
                b = false;
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return b;
        }

        public void showPanel() {
            jarText.requestFocus();
        }
    }

    /** Finish page */
    class WizardPanel3 extends WizardPanel {

        public WizardPanel3() {
            super(jPanel3);
        }

        public boolean canMoveBack() {
            return false;
        }

        public boolean moveNext() {
            exitForm();
            return true;
        }
    }

    class FileExtensionFilter extends javax.swing.filechooser.FileFilter {

        public FileExtensionFilter(String ext, String lab) {
            m_ext = ext;
            m_lab = lab;
        }

        public boolean accept(File f) {
            if (!f.isDirectory()) {
                return f.getName().endsWith(m_ext);
            } else {
                return true;
            }
        }

        public String getDescription() {
            return m_lab;
        }

        String m_ext, m_lab;
    }

    class FileDirectoryFilter extends javax.swing.filechooser.FileFilter {

        public FileDirectoryFilter() {
            setDescription("_none_");
        }

        public boolean accept(File f) {
            return (f.isDirectory());
        }

        public String getDescription() {
            return m_lab;
        }

        public void setDescription(String lab) {
            m_lab = lab;
        }

        String m_lab;
    }

    class JarringThread extends Thread {

        JarringThread(JDialog dialog, String dirName, String jarName, String className, boolean isBean) {
            m_dirName = dirName;
            m_jarName = jarName;
            m_className = className;
            m_dialog = dialog;
            m_isBean = isBean;
        }

        public void run() {
            while (!m_dialog.isShowing()) {
                Thread.currentThread().yield();
            }
            m_errorDescription = createJar(m_dirName, m_jarName, m_className, m_isBean);
            m_dialog.hide();
        }

        String m_dirName, m_jarName, m_className;

        JDialog m_dialog;

        boolean m_isBean;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        jPanel1 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        classText = new javax.swing.JTextField();
        classButton = new javax.swing.JButton();
        dirText = new javax.swing.JTextField();
        dirButton = new javax.swing.JButton();
        classLabel = new javax.swing.JLabel();
        dirLabel = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel51 = new javax.swing.JPanel();
        jarText = new javax.swing.JTextField();
        classButton1 = new javax.swing.JButton();
        jarLabel = new javax.swing.JLabel();
        jLabel112 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel111 = new javax.swing.JLabel();
        jarringDialog = new javax.swing.JDialog();
        jLabel3 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jList1 = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        wizard1 = new org.chir.swing.Wizard();
        topLabel = new javax.swing.JLabel();
        jPanel1.setLayout(new java.awt.GridBagLayout());
        jPanel1.setAlignmentX(0.0F);
        jPanel1.setAlignmentY(0.0F);
        jPanel1.setMinimumSize(new java.awt.Dimension(500, 250));
        jPanel1.setPreferredSize(new java.awt.Dimension(500, 250));
        jPanel5.setLayout(new java.awt.GridBagLayout());
        jPanel5.setMinimumSize(new java.awt.Dimension(500, 200));
        jPanel5.setPreferredSize(new java.awt.Dimension(500, 200));
        classText.setFont(new java.awt.Font("Arial", 0, 12));
        classText.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        classText.setToolTipText("Type the program's main class here");
        classText.setMinimumSize(new java.awt.Dimension(240, 21));
        classText.setPreferredSize(new java.awt.Dimension(240, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 2, 4, 0);
        jPanel5.add(classText, gridBagConstraints);
        classButton.setFont(new java.awt.Font("Arial", 0, 10));
        classButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/browse.gif")));
        classButton.setToolTipText("Browse...");
        classButton.setMaximumSize(new java.awt.Dimension(30, 23));
        classButton.setMinimumSize(new java.awt.Dimension(30, 23));
        classButton.setPreferredSize(new java.awt.Dimension(30, 23));
        classButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                classButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanel5.add(classButton, gridBagConstraints);
        dirText.setFont(new java.awt.Font("Arial", 0, 12));
        dirText.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        dirText.setToolTipText("Type the program's directory here");
        dirText.setMinimumSize(new java.awt.Dimension(240, 21));
        dirText.setPreferredSize(new java.awt.Dimension(240, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 2, 4, 0);
        jPanel5.add(dirText, gridBagConstraints);
        dirButton.setFont(new java.awt.Font("Arial", 0, 10));
        dirButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/browse.gif")));
        dirButton.setToolTipText("Browse...");
        dirButton.setMaximumSize(new java.awt.Dimension(30, 23));
        dirButton.setMinimumSize(new java.awt.Dimension(30, 23));
        dirButton.setPreferredSize(new java.awt.Dimension(30, 23));
        dirButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dirButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanel5.add(dirButton, gridBagConstraints);
        classLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        classLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/main.gif")));
        classLabel.setText("___Main class___");
        classLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 2);
        jPanel5.add(classLabel, gridBagConstraints);
        dirLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        dirLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/open.gif")));
        dirLabel.setText("__Directory__");
        dirLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 2);
        jPanel5.add(dirLabel, gridBagConstraints);
        jLabel11.setFont(new java.awt.Font("Times New Roman", 0, 14));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel11.setText("<html>____Please, fill-in the following information____ <br> </html>");
        jLabel11.setIconTextGap(10);
        jLabel11.setMaximumSize(new java.awt.Dimension(500, 500));
        jLabel11.setMinimumSize(new java.awt.Dimension(500, 142));
        jLabel11.setPreferredSize(new java.awt.Dimension(500, 500));
        jLabel11.setRequestFocusEnabled(false);
        jLabel11.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel5.add(jLabel11, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jPanel5, gridBagConstraints);
        jPanel2.setLayout(new java.awt.GridBagLayout());
        jPanel2.setAlignmentX(0.0F);
        jPanel2.setAlignmentY(0.0F);
        jPanel2.setMinimumSize(new java.awt.Dimension(500, 250));
        jPanel2.setPreferredSize(new java.awt.Dimension(500, 250));
        jPanel51.setLayout(new java.awt.GridBagLayout());
        jPanel51.setMinimumSize(new java.awt.Dimension(500, 200));
        jPanel51.setPreferredSize(new java.awt.Dimension(500, 200));
        jarText.setFont(new java.awt.Font("Arial", 0, 12));
        jarText.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jarText.setToolTipText("Type the program's main class here");
        jarText.setMinimumSize(new java.awt.Dimension(240, 21));
        jarText.setPreferredSize(new java.awt.Dimension(240, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 2, 4, 0);
        jPanel51.add(jarText, gridBagConstraints);
        classButton1.setFont(new java.awt.Font("Arial", 0, 10));
        classButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/browse.gif")));
        classButton1.setToolTipText("Browse...");
        classButton1.setMaximumSize(new java.awt.Dimension(30, 23));
        classButton1.setMinimumSize(new java.awt.Dimension(30, 23));
        classButton1.setPreferredSize(new java.awt.Dimension(30, 23));
        classButton1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                classButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanel51.add(classButton1, gridBagConstraints);
        jarLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jarLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/jar.gif")));
        jarLabel.setText("___JAR file___");
        jarLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 2);
        jPanel51.add(jarLabel, gridBagConstraints);
        jLabel112.setFont(new java.awt.Font("Times New Roman", 0, 14));
        jLabel112.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel112.setText("<html>____Please, fill-in the following information____ <br> </html>");
        jLabel112.setIconTextGap(10);
        jLabel112.setMinimumSize(new java.awt.Dimension(500, 142));
        jLabel112.setPreferredSize(new java.awt.Dimension(500, 500));
        jLabel112.setRequestFocusEnabled(false);
        jLabel112.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel51.add(jLabel112, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jPanel51, gridBagConstraints);
        jPanel3.setLayout(new java.awt.GridBagLayout());
        jPanel3.setAlignmentX(0.0F);
        jPanel3.setAlignmentY(0.0F);
        jPanel3.setMinimumSize(new java.awt.Dimension(500, 250));
        jPanel3.setPreferredSize(new java.awt.Dimension(500, 250));
        jLabel111.setFont(new java.awt.Font("Times New Roman", 0, 14));
        jLabel111.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel111.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/finish.gif")));
        jLabel111.setText("<html><br><u>Congratulations !</u><br><br>Your file<br><br>XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX<br><br> was successfully created. <br><br>Press Finish to exit.<br><br> </html>");
        jLabel111.setIconTextGap(10);
        jLabel111.setMinimumSize(new java.awt.Dimension(500, 200));
        jLabel111.setPreferredSize(new java.awt.Dimension(500, 200));
        jLabel111.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel3.add(jLabel111, gridBagConstraints);
        jarringDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        jarringDialog.setTitle("Working...");
        jarringDialog.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        jarringDialog.setModal(true);
        jarringDialog.setResizable(false);
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/jarring.gif")));
        jLabel3.setText("<html><body><br>Creating JAR file...<br><br>Please wait.<br></body></html>");
        jLabel3.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel3.setMinimumSize(new java.awt.Dimension(300, 100));
        jLabel3.setPreferredSize(new java.awt.Dimension(300, 100));
        jarringDialog.getContentPane().add(jLabel3, java.awt.BorderLayout.CENTER);
        jPanel6.setLayout(new java.awt.BorderLayout());
        jList1.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {

            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jPanel6.add(jList1, java.awt.BorderLayout.CENTER);
        jLabel2.setFont(new java.awt.Font("Times New Roman", 0, 14));
        jLabel2.setText("<html><br><br>Select the kind of project you want to pack :<br></html>");
        jPanel6.add(jLabel2, java.awt.BorderLayout.NORTH);
        jLabel4.setFont(new java.awt.Font("Times New Roman", 0, 14));
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel4.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 10, 0, 10)));
        jLabel4.setMinimumSize(new java.awt.Dimension(300, 150));
        jLabel4.setPreferredSize(new java.awt.Dimension(300, 150));
        jPanel6.add(jLabel4, java.awt.BorderLayout.EAST);
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        wizard1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));
        getContentPane().add(wizard1, java.awt.BorderLayout.CENTER);
        topLabel.setFont(new java.awt.Font("Times New Roman", 0, 12));
        topLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        topLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/top.gif")));
        topLabel.setAlignmentY(0.0F);
        topLabel.setRequestFocusEnabled(false);
        topLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        getContentPane().add(topLabel, java.awt.BorderLayout.NORTH);
        pack();
    }

    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {
        jLabel4.setText(m_listHelpText[jList1.getSelectedIndex()]);
    }

    private void classButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (m_fc3.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            setJarText(m_fc3.getSelectedFile().getPath());
        }
        jarText.requestFocus();
    }

    private void classButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (m_fc2.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            setClassText(m_fc2.getSelectedFile().getPath());
        }
        classText.requestFocus();
    }

    private void dirButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (m_fc1.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            setDirText(m_fc1.getSelectedFile().getAbsolutePath());
        }
        dirText.requestFocus();
    }

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {
        exitForm();
    }

    /** Exit the Application */
    private void exitForm() {
        try {
            Point o = getLocation();
            m_properties.put("WINDOW_X", "" + o.x);
            m_properties.put("WINDOW_Y", "" + o.y);
            if (!jList1.isSelectionEmpty()) {
                m_properties.put("JAR_TYPE", "" + jList1.getSelectedIndex());
            }
            if (dirText.getText().trim() != "") {
                m_properties.put("DIRECTORY", dirText.getText().trim());
            }
            if (classText.getText().trim() != "") {
                m_properties.put("MAIN_CLASS", classText.getText().trim());
            }
            if (jarText.getText().trim() != "") {
                m_properties.put("JAR_FILE", jarText.getText().trim());
            }
            FileOutputStream out = new FileOutputStream("JarWizard.ini");
            m_properties.store(out, "JarWizard preferences");
            out.close();
        } catch (Exception e) {
        }
        System.exit(0);
    }

    Image loadImage(String s) {
        Image im = null;
        URL u;
        String s2 = "/" + s;
        u = getClass().getResource(s2);
        if (u == null) {
            u = getClass().getResource(s);
        }
        if (u != null) {
            im = Toolkit.getDefaultToolkit().getImage(u);
        } else {
            im = Toolkit.getDefaultToolkit().getImage(s);
            if (im == null) {
                im = Toolkit.getDefaultToolkit().getImage(s2);
            }
        }
        return im;
    }

    String createJar(String dirText, String jarText, String classText, boolean isBean) {
        try {
            JarCreator jc = new JarCreator();
            jc.addExtension("class");
            jc.addExtension("gif");
            jc.addExtension("jpeg");
            jc.addExtension("jpg");
            jc.addExtension("au");
            jc.addExtension("properties");
            jc.createJar(dirText, jarText, classText, isBean);
        } catch (JarCreator.JarCreatorException e) {
            return e.getMessage();
        }
        return null;
    }

    void setDirText(String s) {
        if (s != null) {
            s = s.trim();
            if (!s.equals("")) {
                dirText.setText(s);
                dirText.setSelectionStart(0);
                dirText.setSelectionEnd(9999);
                m_fc1.setCurrentDirectory(new File(s));
                m_fc2.setCurrentDirectory(m_fc1.getCurrentDirectory());
                m_fc3.setCurrentDirectory(m_fc1.getCurrentDirectory());
            }
        }
    }

    void setClassText(String s) {
        if (s != null) {
            s = s.trim();
            if (!s.equals("")) {
                classText.setText(s);
                classText.setSelectionStart(0);
                classText.setSelectionEnd(9999);
                m_fc2.setSelectedFile(new File(s));
            }
        }
    }

    void setJarText(String s) {
        if (s != null) {
            s = s.trim();
            if (!s.equals("")) {
                jarText.setText(s);
                jarText.setSelectionStart(0);
                jarText.setSelectionEnd(9999);
                m_fc3.setSelectedFile(new File(s));
            }
        }
    }

    private javax.swing.JPanel jPanel6;

    private javax.swing.JPanel jPanel5;

    private javax.swing.JPanel jPanel3;

    private javax.swing.JPanel jPanel2;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JDialog jarringDialog;

    private javax.swing.JLabel jLabel112;

    private javax.swing.JTextField jarText;

    private javax.swing.JLabel jLabel111;

    private javax.swing.JButton classButton1;

    private javax.swing.JLabel dirLabel;

    private org.chir.swing.Wizard wizard1;

    private javax.swing.JLabel topLabel;

    private javax.swing.JButton dirButton;

    private javax.swing.JLabel classLabel;

    private javax.swing.JTextField dirText;

    private javax.swing.JLabel jarLabel;

    private javax.swing.JPanel jPanel51;

    private javax.swing.JLabel jLabel4;

    private javax.swing.JLabel jLabel11;

    private javax.swing.JButton classButton;

    private javax.swing.JList jList1;

    private javax.swing.JLabel jLabel3;

    private javax.swing.JLabel jLabel2;

    private javax.swing.JTextField classText;

    WizardPanel m_wPanel0, m_wPanel1, m_wPanel2, m_wPanel3;

    JFileChooser m_fc1, m_fc2, m_fc3;

    Properties m_properties = new Properties();

    String m_errorDescription;

    String[] m_listHelpText = { "IDS_WELCOME_APPLICATION", "IDS_WELCOME_BEAN" };
}

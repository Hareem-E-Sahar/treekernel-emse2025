package windu.sms.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.UIManager;
import windu.fungsi.IconFrame;
import windu.sms.entity.Gammurc;
import windu.sms.entity.Smsdrc;
import windu.sms.file.GammurcWriter;
import windu.sms.file.SmsdrcWriter;

/**
 *
 * @author Windu Purnomo
 */
public class GammuConfig extends javax.swing.JFrame {

    private String gammuLocation = System.getProperty("user.dir") + "\\Gammu";

    private Gammurc gammurc;

    private Smsdrc smsdrc;

    /** Creates new form GammuConfig */
    public GammuConfig() {
        this.setIconImage(new IconFrame().setIconFrame());
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        testBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtTestConn = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtDatabase = new javax.swing.JTextField();
        txtPC = new javax.swing.JTextField();
        txtPass = new javax.swing.JTextField();
        txtUser = new javax.swing.JTextField();
        txtService = new javax.swing.JTextField();
        btnSave = new javax.swing.JButton();
        port = new javax.swing.JComboBox();
        conn = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Gammu Configuration");
        jPanel1.setBackground(new java.awt.Color(204, 204, 204));
        jPanel2.setBackground(new java.awt.Color(204, 204, 204));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Configuration Attribute", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        testBtn.setText("test");
        testBtn.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testBtnActionPerformed(evt);
            }
        });
        txtTestConn.setBackground(new java.awt.Color(0, 0, 0));
        txtTestConn.setColumns(20);
        txtTestConn.setEditable(false);
        txtTestConn.setFont(new java.awt.Font("Courier", 0, 10));
        txtTestConn.setForeground(new java.awt.Color(255, 204, 0));
        txtTestConn.setRows(5);
        jScrollPane1.setViewportView(txtTestConn);
        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel2Layout.createSequentialGroup().addContainerGap().add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE).add(testBtn)).addContainerGap()));
        jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel2Layout.createSequentialGroup().add(testBtn).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE).addContainerGap()));
        jPanel4.setBackground(new java.awt.Color(204, 204, 204));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Configuration Attribute", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Service");
        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("User");
        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Password");
        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("PC");
        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Database");
        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        port.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "COM10", "COM11", "COM12", "COM13", "COM14", "COM15", "COM16" }));
        port.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portActionPerformed(evt);
            }
        });
        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Connection");
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Port");
        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel4Layout.createSequentialGroup().add(29, 29, 29).add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(btnSave).add(jPanel4Layout.createSequentialGroup().add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jLabel7).add(jLabel8).add(jLabel6).add(jLabel5).add(jLabel3).add(jLabel2).add(jLabel1)).add(45, 45, 45).add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false).add(txtUser).add(txtPass).add(txtPC).add(txtDatabase).add(conn).add(txtService, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE).add(port, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))).addContainerGap(81, Short.MAX_VALUE)));
        jPanel4Layout.linkSize(new java.awt.Component[] { txtDatabase, txtPC, txtPass, txtService, txtUser }, org.jdesktop.layout.GroupLayout.HORIZONTAL);
        jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel4Layout.createSequentialGroup().add(17, 17, 17).add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(port, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel1)).add(6, 6, 6).add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(conn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel2)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(txtService, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel3)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(txtUser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel5)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(txtPass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel6)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(txtPC, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel7)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(txtDatabase, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel8)).addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED).add(btnSave).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel1Layout.createSequentialGroup().addContainerGap().add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false).add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(org.jdesktop.layout.GroupLayout.LEADING, jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap()));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel1Layout.createSequentialGroup().addContainerGap().add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap(40, Short.MAX_VALUE)));
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 356) / 2, (screenSize.height - 513) / 2, 356, 513);
    }

    private void testBtnActionPerformed(java.awt.event.ActionEvent evt) {
        Process p = null;
        Runtime r = Runtime.getRuntime();
        try {
            p = r.exec("cmd.exe /c gammu identify");
            BufferedReader rd = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String temp = "";
            String s = "";
            while ((temp = rd.readLine()) != null) {
                s += temp + "\n";
            }
            txtTestConn.setText(s);
        } catch (IOException ex) {
            ex.getMessage();
        }
    }

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {
        smsdrc = new Smsdrc();
        gammurc = new Gammurc();
        smsdrc.setService(txtService.getText());
        smsdrc.setUser(txtUser.getText());
        smsdrc.setPassword(txtPass.getText());
        smsdrc.setDatabase(txtDatabase.getText());
        smsdrc.setConnection(gammurc.getConnection());
        smsdrc.setPort(port.getSelectedItem().toString());
        smsdrc.setConnection(conn.getText());
        gammurc.setPort(smsdrc.getPort());
        gammurc.setConnection(smsdrc.getConnection());
        new SmsdrcWriter(smsdrc);
        new GammurcWriter(gammurc);
    }

    private void portActionPerformed(java.awt.event.ActionEvent evt) {
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("error");
        }
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new GammuConfig().setVisible(true);
            }
        });
    }

    private javax.swing.JButton btnSave;

    private javax.swing.JTextField conn;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel2;

    private javax.swing.JLabel jLabel3;

    private javax.swing.JLabel jLabel5;

    private javax.swing.JLabel jLabel6;

    private javax.swing.JLabel jLabel7;

    private javax.swing.JLabel jLabel8;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JPanel jPanel2;

    private javax.swing.JPanel jPanel4;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JComboBox port;

    private javax.swing.JButton testBtn;

    private javax.swing.JTextField txtDatabase;

    private javax.swing.JTextField txtPC;

    private javax.swing.JTextField txtPass;

    private javax.swing.JTextField txtService;

    private javax.swing.JTextArea txtTestConn;

    private javax.swing.JTextField txtUser;
}

package net.sourceforge.buildmonitor.dialogs;

import java.awt.Color;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author  sbrunot
 */
public class BambooPropertiesDialog extends javax.swing.JDialog {

    private static final Color COLOR_TEXT_IN_ERROR = Color.RED;

    private static final Color COLOR_TEXT_DEFAULT = Color.BLACK;

    private static final Color COLOR_BACKGROUND_MANDATORY_FIELD_EMPY = Color.RED;

    private static final Color COLOR_BACKGROUND_FIELD_NORMAL = Color.WHITE;

    public static final int BUTTON_CLOSE = 1;

    public static final int BUTTON_OK = 2;

    public static final int BUTTON_CANCEL = 3;

    private int lastClickedButton;

    /**
     * Get the last clicked button
     */
    public int getLastClickedButton() {
        return this.lastClickedButton;
    }

    /** Creates new form BambooPropertiesDialog */
    public BambooPropertiesDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    /**
     * Enable or disable the ok button regarding the values of the different
     * fields
     */
    private void setButtonsState() {
        if (isBaseUrlOk() && isUsernameOk() && isPasswordOk() && isUpdatePeriodOk()) {
            this.okButton.setEnabled(true);
        } else {
            this.okButton.setEnabled(false);
        }
        if (isBaseUrlOk()) {
            this.openBaseURLButton.setEnabled(true);
        } else {
            this.openBaseURLButton.setEnabled(false);
        }
    }

    private boolean isBaseUrlOk() {
        return (!isBaseUrlEmptyWhenTrimed() && isBaseUrlValid());
    }

    /**
     * Is the base url field value a valid URL ?
     */
    private boolean isBaseUrlValid() {
        boolean returnedValue = true;
        try {
            URL baseUrl = new URL(this.baseURLField.getText());
            if (!"http".equals(baseUrl.getProtocol()) && !"https".equals(baseUrl.getProtocol())) {
                returnedValue = false;
            }
        } catch (MalformedURLException e) {
            returnedValue = false;
        }
        return returnedValue;
    }

    /**
     * Is the base url field value empty when trimed ?
     */
    private boolean isBaseUrlEmptyWhenTrimed() {
        return ("".equals(this.baseURLField.getText().trim()));
    }

    /**
     * Is the username field value ok ?
     */
    private boolean isUsernameOk() {
        return (!"".equals(this.usernameField.getText().trim()));
    }

    /**
     * Is the password field value ok ?
     */
    private boolean isPasswordOk() {
        return (!"".equals(new String(this.passwordField.getPassword()).trim()));
    }

    /**
     * Is the value in the update period field ok ?
     */
    private boolean isUpdatePeriodOk() {
        return true;
    }

    private void initComponents() {
        javax.swing.JLabel jLabel1;
        javax.swing.JLabel jLabel10;
        javax.swing.JLabel jLabel11;
        javax.swing.JLabel jLabel12;
        javax.swing.JLabel jLabel13;
        javax.swing.JLabel jLabel2;
        javax.swing.JLabel jLabel3;
        javax.swing.JLabel jLabel4;
        javax.swing.JLabel jLabel5;
        javax.swing.JLabel jLabel6;
        javax.swing.JLabel jLabel7;
        javax.swing.JLabel jLabel8;
        javax.swing.JLabel jLabel9;
        javax.swing.JPanel jPanel1;
        javax.swing.JSeparator jSeparator1;
        javax.swing.JSeparator jSeparator2;
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        baseURLField = new javax.swing.JTextField();
        this.baseURLField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent evt) {
                updateBaseURLFieldStatus();
                setButtonsState();
            }

            public void removeUpdate(DocumentEvent evt) {
                updateBaseURLFieldStatus();
                setButtonsState();
            }

            public void changedUpdate(DocumentEvent evt) {
            }
        });
        passwordField = new javax.swing.JPasswordField();
        this.passwordField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent evt) {
                updatePasswordFieldStatus();
                setButtonsState();
            }

            public void removeUpdate(DocumentEvent evt) {
                updatePasswordFieldStatus();
                setButtonsState();
            }

            public void changedUpdate(DocumentEvent evt) {
            }
        });
        usernameField = new javax.swing.JTextField();
        this.usernameField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent evt) {
                updateUsernameFieldStatus();
                setButtonsState();
            }

            public void removeUpdate(DocumentEvent evt) {
                updateUsernameFieldStatus();
                setButtonsState();
            }

            public void changedUpdate(DocumentEvent evt) {
            }
        });
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        updatePeriodField = new javax.swing.JFormattedTextField();
        this.updatePeriodField.setValue(new Integer(5));
        jLabel13 = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        openBaseURLButton = new javax.swing.JButton();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }

            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });
        jPanel1.setBackground(java.awt.SystemColor.info);
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 13));
        jLabel1.setText("Bamboo build monitoring parameters");
        jLabel2.setText("Here you must define the parameters that Build Monitor will use to monitor your bamboo builds. ");
        jLabel3.setText("The Bamboo server parameters are mandatory for Build Monitor to be able to connect to your Bamboo server.");
        jLabel4.setText("The update period defines the delay, in minutes, between two queries to the Bamboo server in order to retrieve");
        jLabel5.setText("the states of the lasts builds.");
        jLabel6.setText("After you've clicked the Ok button, all values are saved in the bamboo-monitor.properties file in your user directory.");
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel1).addComponent(jLabel2).addComponent(jLabel3).addComponent(jLabel4).addComponent(jLabel5).addComponent(jLabel6)).addContainerGap(22, Short.MAX_VALUE)));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addComponent(jLabel1).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel2).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel3).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel4).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel5).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel6).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jLabel7.setText("Bamboo server parameters");
        jLabel9.setText("Username:");
        jLabel9.setToolTipText("A valid Bamboo user defined for the server to monitor.");
        jLabel8.setText("Base URL:");
        jLabel8.setToolTipText("The base URL to connect to the Bamboo server instance to monitor.");
        jLabel10.setText("Password:");
        jLabel10.setToolTipText("The password of the Bamboo user.");
        baseURLField.setText("http://server:port");
        baseURLField.addFocusListener(new java.awt.event.FocusAdapter() {

            public void focusLost(java.awt.event.FocusEvent evt) {
                baseURLFieldFocusLost(evt);
            }
        });
        passwordField.addFocusListener(new java.awt.event.FocusAdapter() {

            public void focusLost(java.awt.event.FocusEvent evt) {
                passwordFieldFocusLost(evt);
            }
        });
        usernameField.addFocusListener(new java.awt.event.FocusAdapter() {

            public void focusLost(java.awt.event.FocusEvent evt) {
                usernameFieldFocusLost(evt);
            }
        });
        jLabel11.setText("Monitoring parameters");
        jLabel12.setText("Update period:");
        jLabel12.setToolTipText("The delay between two queries of the Bamboo server to retrieve status of the last builds.");
        updatePeriodField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        updatePeriodField.addFocusListener(new java.awt.event.FocusAdapter() {

            public void focusLost(java.awt.event.FocusEvent evt) {
                updatePeriodFieldFocusLost(evt);
            }
        });
        jLabel13.setText("minutes.");
        okButton.setText("Ok");
        okButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        openBaseURLButton.setText("Open...");
        openBaseURLButton.setFocusable(false);
        openBaseURLButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openBaseURLButtonActionPerformed(evt);
            }
        });
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(17, 17, 17).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jLabel8).addComponent(jLabel9).addComponent(jLabel10)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false).addComponent(passwordField, javax.swing.GroupLayout.Alignment.LEADING).addComponent(usernameField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(baseURLField, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(openBaseURLButton)))).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(okButton).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(cancelButton)).addGroup(layout.createSequentialGroup().addGap(10, 10, 10).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 684, Short.MAX_VALUE).addComponent(jLabel7))).addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))).addGroup(layout.createSequentialGroup().addGap(22, 22, 22).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 684, Short.MAX_VALUE).addComponent(jLabel11).addGroup(layout.createSequentialGroup().addGap(10, 10, 10).addComponent(jLabel12).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(updatePeriodField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel13))))).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel7).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel8).addComponent(openBaseURLButton).addComponent(baseURLField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel9)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel10).addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(21, 21, 21).addComponent(jLabel11).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel12).addComponent(updatePeriodField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel13)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(cancelButton).addComponent(okButton)).addContainerGap()));
        pack();
    }

    private void openBaseURLButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (Desktop.isDesktopSupported()) {
            try {
                URI baseURI = new URI(this.baseURLField.getText());
                Desktop.getDesktop().browse(baseURI);
            } catch (Exception err) {
            }
        }
    }

    private void formWindowOpened(java.awt.event.WindowEvent evt) {
        setButtonsState();
    }

    private void formWindowClosed(java.awt.event.WindowEvent evt) {
        this.lastClickedButton = BUTTON_CLOSE;
    }

    private void updatePeriodFieldFocusLost(java.awt.event.FocusEvent evt) {
        try {
            this.updatePeriodField.commitEdit();
        } catch (ParseException e) {
        }
        if (((Integer) this.updatePeriodField.getValue()).intValue() < 1) {
            this.updatePeriodField.setValue(new Integer(1));
        }
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        this.lastClickedButton = BUTTON_CANCEL;
        setVisible(false);
    }

    private void passwordFieldFocusLost(java.awt.event.FocusEvent evt) {
        updatePasswordFieldStatus();
        setButtonsState();
    }

    private void usernameFieldFocusLost(java.awt.event.FocusEvent evt) {
        updateUsernameFieldStatus();
        setButtonsState();
    }

    private void baseURLFieldFocusLost(java.awt.event.FocusEvent evt) {
        updateBaseURLFieldStatus();
        setButtonsState();
    }

    public void updateBaseURLFieldStatus() {
        if (isBaseUrlEmptyWhenTrimed()) {
            this.baseURLField.setBackground(COLOR_BACKGROUND_MANDATORY_FIELD_EMPY);
            this.baseURLField.setToolTipText("base URL is mandatory to connect to the bamboo server !");
            if (COLOR_TEXT_IN_ERROR.equals(this.baseURLField.getForeground())) {
                this.baseURLField.setForeground(COLOR_TEXT_DEFAULT);
            }
        } else {
            this.baseURLField.setBackground(COLOR_BACKGROUND_FIELD_NORMAL);
            if (isBaseUrlValid()) {
                this.baseURLField.setForeground(COLOR_TEXT_DEFAULT);
                this.baseURLField.setToolTipText(null);
            } else {
                this.baseURLField.setForeground(COLOR_TEXT_IN_ERROR);
                this.baseURLField.setToolTipText(this.baseURLField.getText() + " is not a valid http URL !");
            }
        }
    }

    public void updateUsernameFieldStatus() {
        if (isUsernameOk()) {
            this.usernameField.setBackground(COLOR_BACKGROUND_FIELD_NORMAL);
            this.usernameField.setToolTipText(null);
        } else {
            this.usernameField.setBackground(COLOR_BACKGROUND_MANDATORY_FIELD_EMPY);
            this.usernameField.setToolTipText("username is mandatory to connect to the bamboo server !");
        }
    }

    public void updatePasswordFieldStatus() {
        if (isPasswordOk()) {
            this.passwordField.setBackground(COLOR_BACKGROUND_FIELD_NORMAL);
            this.passwordField.setToolTipText(null);
        } else {
            this.passwordField.setBackground(COLOR_BACKGROUND_MANDATORY_FIELD_EMPY);
            this.passwordField.setToolTipText("password is mandatory to connect to the bamboo server !");
        }
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        this.lastClickedButton = BUTTON_OK;
        setVisible(false);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new BambooPropertiesDialog(new javax.swing.JFrame(), true).setVisible(true);
            }
        });
    }

    public javax.swing.JTextField baseURLField;

    private javax.swing.JButton cancelButton;

    private javax.swing.JButton okButton;

    private javax.swing.JButton openBaseURLButton;

    public javax.swing.JPasswordField passwordField;

    public javax.swing.JFormattedTextField updatePeriodField;

    public javax.swing.JTextField usernameField;
}

package newgen.presentation.administration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import newgen.presentation.StaticValues;
import newgen.presentation.component.NGLResourceBundle;
import newgen.presentation.component.ServletConnector;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.json.JSONObject;

/**
 *
 * @author yogesh
 */
public class CirculationPrivilegeMatrix extends javax.swing.JDialog {

    private newgen.presentation.component.NewGenXMLGenerator newGenXMLGenerator = null;

    private newgen.presentation.component.Utility utility = null;

    private newgen.presentation.component.ServletConnector servletConnector = null;

    SpinnerModel sm = new SpinnerNumberModel(0, 0, 1000, 1);

    private DefaultTableModel modelAdvance = null;

    private DefaultTableModel jtab2 = null;

    boolean valit;

    private int mode = -1;

    private int CREATION_MODE = 1;

    private int MODIFICATION_MODE = 2;

    private String currentData;

    private int inserted = 3;

    private int updated = 4;

    private int error = 5;

    private int stats = -1;

    CirculationPrivilegeMatrixList cpml = CirculationPrivilegeMatrixList.getInstance();

    private boolean commonValuesNotSet = true;

    /** Creates new form CirculationPrivilegeMatrix */
    public CirculationPrivilegeMatrix(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.loadLocales();
        newGenXMLGenerator = newgen.presentation.component.NewGenXMLGenerator.getInstance();
        utility = newgen.presentation.component.Utility.getInstance();
        servletConnector = newgen.presentation.component.ServletConnector.getInstance();
        String[] column = { NGLResourceBundle.getInstance().getString("CPM.Day"), NGLResourceBundle.getInstance().getString("CPM.Month") };
        String[] colun = { NGLResourceBundle.getInstance().getString("From"), NGLResourceBundle.getInstance().getString("To"), NGLResourceBundle.getInstance().getString("Overdue") };
        modelAdvance = new DefaultTableModel(column, 0) {

            @Override
            public boolean isCellEditable(int r, int c) {
                return true;
            }
        };
        jTable2.setModel(modelAdvance);
        jtab2 = new DefaultTableModel(colun, 0) {

            @Override
            public boolean isCellEditable(int r, int c) {
                return true;
            }
        };
        jTable1.setModel(jtab2);
        utility.getLibraryDetails(cbLibrary);
        utility.getMaterialTypes(cbPhyform);
        cbLibrary.setSelectedItem(newgen.presentation.NewGenMain.getAppletInstance().getLibraryName(newgen.presentation.NewGenMain.getAppletInstance().getLibraryID()));
        cbPhyform.setSelectedIndex(6);
    }

    public void loadLocales() {
        lbLibrary.setText(NGLResourceBundle.getInstance().getString("Library"));
        lbPatronCategory.setText(NGLResourceBundle.getInstance().getString("PatronCategory"));
        lbphysicalpresentform.setText(NGLResourceBundle.getInstance().getString("PhysicalPresentationForm"));
        lbWithEffectFrom.setText(NGLResourceBundle.getInstance().getString("WEF"));
        lbGlobalLoanLimit.setText(NGLResourceBundle.getInstance().getString("OverallLoanLimit"));
        lbzero.setText(NGLResourceBundle.getInstance().getString("ZeroMeansOverallLoanLimitIsNotDefined"));
        cbRenewal.setText(NGLResourceBundle.getInstance().getString("RenewThrOpac"));
        jLabel1.setText(NGLResourceBundle.getInstance().getString("Loanperiod(days)"));
        lbLoanLt.setText(NGLResourceBundle.getInstance().getString("Loanlimit(number)"));
        jLabel3.setText(NGLResourceBundle.getInstance().getString("Renewallimit(times)"));
        jLabel4.setText(NGLResourceBundle.getInstance().getString("CeilingOfOverdue"));
        jLabel2.setText(NGLResourceBundle.getInstance().getString("Loanperiod"));
        rbIndays.setText(NGLResourceBundle.getInstance().getString("CPM.InDays"));
        rbInhours.setText(NGLResourceBundle.getInstance().getString("CPM.InHours"));
        rbNextOccuring.setText(NGLResourceBundle.getInstance().getString("CPM.NextOccurrence"));
        jRadioButton1.setText(NGLResourceBundle.getInstance().getString("IncludeHolidays"));
        jRadioButton2.setText(NGLResourceBundle.getInstance().getString("ExcludeHolidays"));
        jLabel6.setText(NGLResourceBundle.getInstance().getString("LoanPeriodHours"));
        jLabel5.setText(NGLResourceBundle.getInstance().getString("ZeroMeansCeilingOfOverdueIsNotDefined"));
        jRadioButton3.setText(NGLResourceBundle.getInstance().getString("CPM.Days"));
        jRadioButton4.setText(NGLResourceBundle.getInstance().getString("CPM.Hours"));
        jLabel7.setText(NGLResourceBundle.getInstance().getString("DuringOverdueCalculation"));
        jRadioButton6.setText(NGLResourceBundle.getInstance().getString("IncludeHolidays"));
        jRadioButton5.setText(NGLResourceBundle.getInstance().getString("ExcludeHolidays"));
        jButton1.setText(NGLResourceBundle.getInstance().getString("Save"));
        jButton2.setText(NGLResourceBundle.getInstance().getString("Cancel"));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        lbLibrary = new javax.swing.JLabel();
        lbPatronCategory = new javax.swing.JLabel();
        lbphysicalpresentform = new javax.swing.JLabel();
        lbWithEffectFrom = new javax.swing.JLabel();
        cbLibrary = new javax.swing.JComboBox();
        cbPatronCategory = new javax.swing.JComboBox();
        cbPhyform = new javax.swing.JComboBox();
        wef = new newgen.presentation.component.DateField();
        cbRenewal = new javax.swing.JCheckBox();
        jPanel22 = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        rbIndays = new javax.swing.JRadioButton();
        rbInhours = new javax.swing.JRadioButton();
        rbNextOccuring = new javax.swing.JRadioButton();
        jPanel8 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jFormattedTextField1 = new JFormattedTextField(new Integer(0));
        jPanel10 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jFormattedTextField2 = new JFormattedTextField(new Integer(0));
        jPanel11 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jPanel15 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel23 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jPanel13 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lbLoanLt = new javax.swing.JLabel();
        jFormattedTextField3 = new JFormattedTextField(new Integer(0));
        jFormattedTextField4 = new JFormattedTextField(new Integer(0));
        jFormattedTextField5 = new JFormattedTextField(new Float(0.00));
        jPanel4 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jPanel17 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel19 = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jPanel20 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jRadioButton6 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        lbGlobalLoanLimit = new javax.swing.JLabel();
        jsGlobalLoanlt = new JSpinner(sm);
        lbzero = new javax.swing.JLabel();
        cbAllowMultipleCopies = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("");
        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {

            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });
        jPanel1.setName(NGLResourceBundle.getInstance().getString("General"));
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setLayout(new java.awt.GridBagLayout());
        lbLibrary.setText("Library");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel2.add(lbLibrary, gridBagConstraints);
        lbPatronCategory.setText("Patron category");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel2.add(lbPatronCategory, gridBagConstraints);
        lbphysicalpresentform.setText("Physical / Presentation form");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel2.add(lbphysicalpresentform, gridBagConstraints);
        lbWithEffectFrom.setText("With effect from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel2.add(lbWithEffectFrom, gridBagConstraints);
        cbLibrary.setMinimumSize(new java.awt.Dimension(110, 22));
        cbLibrary.setPreferredSize(new java.awt.Dimension(110, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(cbLibrary, gridBagConstraints);
        cbPatronCategory.setMinimumSize(new java.awt.Dimension(110, 22));
        cbPatronCategory.setPreferredSize(new java.awt.Dimension(110, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(cbPatronCategory, gridBagConstraints);
        cbPhyform.setMinimumSize(new java.awt.Dimension(110, 22));
        cbPhyform.setPreferredSize(new java.awt.Dimension(110, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(cbPhyform, gridBagConstraints);
        wef.setMinimumSize(new java.awt.Dimension(125, 19));
        wef.setPreferredSize(new java.awt.Dimension(125, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(wef, gridBagConstraints);
        cbRenewal.setSelected(true);
        cbRenewal.setText("Allow renewal through OPAC");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel2.add(cbRenewal, gridBagConstraints);
        jPanel1.add(jPanel2);
        jPanel22.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), NGLResourceBundle.getInstance().getString("Loanperiod")));
        jPanel22.setLayout(new java.awt.BorderLayout());
        jPanel21.setLayout(new java.awt.BorderLayout());
        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), NGLResourceBundle.getInstance().getString("Duration")));
        jPanel7.setPreferredSize(new java.awt.Dimension(250, 49));
        jPanel7.setLayout(new java.awt.GridBagLayout());
        buttonGroup1.add(rbIndays);
        rbIndays.setSelected(true);
        rbIndays.setText("In days");
        rbIndays.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbIndaysActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel7.add(rbIndays, gridBagConstraints);
        buttonGroup1.add(rbInhours);
        rbInhours.setText("In hours");
        rbInhours.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbInhoursActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel7.add(rbInhours, gridBagConstraints);
        buttonGroup1.add(rbNextOccuring);
        rbNextOccuring.setText("Next occuring");
        rbNextOccuring.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbNextOccuringActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel7.add(rbNextOccuring, gridBagConstraints);
        jPanel21.add(jPanel7, java.awt.BorderLayout.WEST);
        jPanel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel8.setPreferredSize(new java.awt.Dimension(452, 200));
        jPanel8.setLayout(new java.awt.CardLayout());
        jLabel1.setForeground(new java.awt.Color(170, 0, 0));
        jLabel1.setText("Loan period(days)");
        jPanel9.add(jLabel1);
        jFormattedTextField1.setColumns(15);
        jPanel9.add(jFormattedTextField1);
        jPanel8.add(jPanel9, "card2");
        jLabel6.setForeground(new java.awt.Color(170, 0, 0));
        jLabel6.setText("Loan period(hours)");
        jPanel10.add(jLabel6);
        jFormattedTextField2.setColumns(15);
        jPanel10.add(jFormattedTextField2);
        jPanel8.add(jPanel10, "card3");
        jPanel11.setLayout(new java.awt.BorderLayout());
        jPanel14.setLayout(new java.awt.BorderLayout());
        jScrollPane3.setPreferredSize(new java.awt.Dimension(452, 175));
        jTable2.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null, null, null }, { null, null, null, null }, { null, null, null, null }, { null, null, null, null } }, new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
        jScrollPane3.setViewportView(jTable2);
        jPanel14.add(jScrollPane3, java.awt.BorderLayout.CENTER);
        jPanel11.add(jPanel14, java.awt.BorderLayout.CENTER);
        jPanel15.setLayout(new java.awt.GridBagLayout());
        jButton3.setText("+");
        jButton3.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel15.add(jButton3, gridBagConstraints);
        jButton4.setText("-");
        jButton4.setMaximumSize(new java.awt.Dimension(21, 26));
        jButton4.setMinimumSize(new java.awt.Dimension(21, 26));
        jButton4.setPreferredSize(new java.awt.Dimension(21, 26));
        jButton4.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel15.add(jButton4, gridBagConstraints);
        jPanel11.add(jPanel15, java.awt.BorderLayout.EAST);
        jPanel8.add(jPanel11, "card4");
        jPanel21.add(jPanel8, java.awt.BorderLayout.CENTER);
        jPanel22.add(jPanel21, java.awt.BorderLayout.CENTER);
        jLabel2.setText("Loan period");
        jPanel23.add(jLabel2);
        buttonGroup2.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("Includes holidays");
        jPanel23.add(jRadioButton1);
        buttonGroup2.add(jRadioButton2);
        jRadioButton2.setText("Excludes holidays");
        jPanel23.add(jRadioButton2);
        jPanel22.add(jPanel23, java.awt.BorderLayout.SOUTH);
        jPanel1.add(jPanel22);
        jPanel13.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel13.setPreferredSize(new java.awt.Dimension(587, 175));
        jPanel13.setLayout(new java.awt.GridBagLayout());
        jLabel3.setText("Renewal limit(times)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel13.add(jLabel3, gridBagConstraints);
        jLabel4.setText("Ceiling of overdue");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        jPanel13.add(jLabel4, gridBagConstraints);
        jLabel5.setFont(new java.awt.Font("DejaVu Sans", 0, 8));
        jLabel5.setText("Zero means ceiling of overdue is not defined");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        jPanel13.add(jLabel5, gridBagConstraints);
        lbLoanLt.setForeground(new java.awt.Color(170, 0, 0));
        lbLoanLt.setText("Loan limit(number)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel13.add(lbLoanLt, gridBagConstraints);
        jFormattedTextField3.setColumns(15);
        jPanel13.add(jFormattedTextField3, new java.awt.GridBagConstraints());
        jFormattedTextField4.setColumns(15);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        jPanel13.add(jFormattedTextField4, gridBagConstraints);
        jFormattedTextField5.setColumns(15);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        jPanel13.add(jFormattedTextField5, gridBagConstraints);
        jPanel1.add(jPanel13);
        jTabbedPane1.addTab(NGLResourceBundle.getInstance().getString("General"), jPanel1);
        jPanel4.setBorder(null);
        jPanel4.setLayout(new java.awt.BorderLayout());
        buttonGroup3.add(jRadioButton3);
        jRadioButton3.setSelected(true);
        jRadioButton3.setText("Days");
        jPanel16.add(jRadioButton3);
        buttonGroup3.add(jRadioButton4);
        jRadioButton4.setText("Hours");
        jPanel16.add(jRadioButton4);
        jPanel4.add(jPanel16, java.awt.BorderLayout.NORTH);
        jPanel17.setLayout(new java.awt.BorderLayout());
        jPanel18.setLayout(new java.awt.BorderLayout());
        jTable1.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null, null, null }, { null, null, null, null }, { null, null, null, null }, { null, null, null, null } }, new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
        jScrollPane2.setViewportView(jTable1);
        jPanel18.add(jScrollPane2, java.awt.BorderLayout.CENTER);
        jPanel17.add(jPanel18, java.awt.BorderLayout.CENTER);
        jPanel19.setLayout(new java.awt.GridBagLayout());
        jButton5.setText("+");
        jButton5.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel19.add(jButton5, gridBagConstraints);
        jButton6.setText("-");
        jButton6.setMaximumSize(new java.awt.Dimension(21, 26));
        jButton6.setMinimumSize(new java.awt.Dimension(21, 26));
        jButton6.setPreferredSize(new java.awt.Dimension(21, 26));
        jButton6.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel19.add(jButton6, gridBagConstraints);
        jPanel17.add(jPanel19, java.awt.BorderLayout.EAST);
        jPanel4.add(jPanel17, java.awt.BorderLayout.CENTER);
        jLabel7.setText("<html><head></head><body>During over due calculation</body></html>");
        jPanel20.add(jLabel7);
        buttonGroup4.add(jRadioButton6);
        jRadioButton6.setSelected(true);
        jRadioButton6.setText("Include holidays");
        jPanel20.add(jRadioButton6);
        buttonGroup4.add(jRadioButton5);
        jRadioButton5.setText("Exclude holidays");
        jPanel20.add(jRadioButton5);
        jPanel4.add(jPanel20, java.awt.BorderLayout.SOUTH);
        jTabbedPane1.addTab(NGLResourceBundle.getInstance().getString("Overduescharges"), jPanel4);
        jPanel3.setLayout(new java.awt.GridBagLayout());
        lbGlobalLoanLimit.setText("Global loan limit");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(lbGlobalLoanLimit, gridBagConstraints);
        jsGlobalLoanlt.setMinimumSize(new java.awt.Dimension(110, 22));
        jsGlobalLoanlt.setPreferredSize(new java.awt.Dimension(110, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel3.add(jsGlobalLoanlt, gridBagConstraints);
        lbzero.setFont(new java.awt.Font("DejaVu Sans", 0, 8));
        lbzero.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lbzero.setText("Zero means global loan limit is not defined");
        lbzero.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel3.add(lbzero, gridBagConstraints);
        cbAllowMultipleCopies.setText(NGLResourceBundle.getInstance().getString("AllowThisPatronCategoryToCheckoutMultipleCopiesOfATitle"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel3.add(cbAllowMultipleCopies, gridBagConstraints);
        jTabbedPane1.addTab(NGLResourceBundle.getInstance().getString("CommonToThisCategory"), jPanel3);
        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);
        jPanel5.setBorder(null);
        jButton1.setText("Save");
        jButton1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton1);
        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton2);
        getContentPane().add(jPanel5, java.awt.BorderLayout.SOUTH);
        pack();
    }

    private void rbIndaysActionPerformed(java.awt.event.ActionEvent evt) {
        if (mode == CREATION_MODE) {
            ((java.awt.CardLayout) this.jPanel8.getLayout()).show(jPanel8, "card2");
            jFormattedTextField2.setText("");
            for (int i = 0; i < modelAdvance.getRowCount(); i++) {
                modelAdvance.removeRow(i);
            }
            jFormattedTextField1.grabFocus();
        } else if (mode == MODIFICATION_MODE) {
            ((java.awt.CardLayout) this.jPanel8.getLayout()).show(jPanel8, "card2");
        }
    }

    private void rbInhoursActionPerformed(java.awt.event.ActionEvent evt) {
        if (mode == CREATION_MODE) {
            ((java.awt.CardLayout) this.jPanel8.getLayout()).show(jPanel8, "card3");
            jFormattedTextField1.setText("");
            for (int i = 0; i < modelAdvance.getRowCount(); i++) {
                modelAdvance.removeRow(i);
            }
            jFormattedTextField2.grabFocus();
        } else if (mode == MODIFICATION_MODE) {
            ((java.awt.CardLayout) this.jPanel8.getLayout()).show(jPanel8, "card3");
        }
    }

    private void rbNextOccuringActionPerformed(java.awt.event.ActionEvent evt) {
        if (mode == CREATION_MODE) {
            ((java.awt.CardLayout) this.jPanel8.getLayout()).show(jPanel8, "card4");
            jFormattedTextField1.setText("");
            jFormattedTextField2.setText("");
            jButton3.grabFocus();
        } else if (mode == MODIFICATION_MODE) {
            ((java.awt.CardLayout) this.jPanel8.getLayout()).show(jPanel8, "card4");
        }
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        defaulttablevalidation();
        System.out.println("validation************" + validateMethod());
        if (validateMethod() == true) {
            System.out.println("mode********************" + mode);
            if (mode == CREATION_MODE) {
                if (rbNextOccuring.isSelected()) {
                    if (modelAdvance.getRowCount() == 0) {
                        JsonStrins("CREATION");
                    } else if (valit == true) {
                        JsonStrins("CREATION");
                    }
                } else {
                    JsonStrins("CREATION");
                }
            } else if (mode == MODIFICATION_MODE) {
                if (rbNextOccuring.isSelected()) {
                    defaulttablevalidation();
                    if (modelAdvance.getRowCount() == 0) {
                        JsonStrins("MODIFICATION");
                    } else if (valit == true) {
                        JsonStrins("MODIFICATION");
                    }
                } else {
                    JsonStrins("MODIFICATION");
                }
            }
            if (stats == inserted) {
                JOptionPane.showMessageDialog(this, NGLResourceBundle.getInstance().getString("TaskSuccessful"));
                this.dispose();
            } else if (stats == updated) {
                JOptionPane.showMessageDialog(this, NGLResourceBundle.getInstance().getString("TaskSuccessful"));
                this.dispose();
            } else if (stats == error) {
                JOptionPane.showMessageDialog(this, NGLResourceBundle.getInstance().getString("ERROR"));
            }
        }
        cpml.refresh();
    }

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
        Object[][] row = { { new Integer(0), new Integer(0) } };
        modelAdvance.addRow(row);
        if (modelAdvance.getRowCount() == 1) {
            modelAdvance.setValueAt(new Integer(0), modelAdvance.getRowCount() - 1, 0);
            modelAdvance.setValueAt(new Integer(0), modelAdvance.getRowCount() - 1, 1);
        } else {
            modelAdvance.setValueAt(new Integer(0), modelAdvance.getRowCount() - 1, 0);
            modelAdvance.setValueAt(new Integer(0), modelAdvance.getRowCount() - 1, 1);
        }
    }

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {
        int[] row = jTable2.getSelectedRows();
        if (row.length > 0) {
            for (int i = row.length; i > 0; i--) {
                modelAdvance.removeRow(row[i - 1]);
            }
            jButton3.grabFocus();
        } else {
            if (jTable2.getRowCount() > 0) {
                newgen.presentation.NewGenMain.getAppletInstance().showInsufficientDataDialog(newgen.presentation.NewGenMain.getAppletInstance().getMyResource().getString("Selectarecordtobedeleted"));
                jTable2.grabFocus();
            }
        }
    }

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {
        Object[][] row = new Object[1][1];
        jtab2.addRow(row);
        if (jtab2.getRowCount() == 1) {
            jtab2.setValueAt(new Integer(0), jtab2.getRowCount() - 1, 0);
            jtab2.setValueAt(new Integer(0), jtab2.getRowCount() - 1, 1);
            jtab2.setValueAt(new Double(0.0), jtab2.getRowCount() - 1, 2);
        } else {
            jtab2.setValueAt(new Integer(0), jtab2.getRowCount() - 1, 0);
            jtab2.setValueAt(new Integer(0), jtab2.getRowCount() - 1, 1);
            jtab2.setValueAt(new Double(0.0), jtab2.getRowCount() - 1, 2);
        }
    }

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {
        int[] row = jTable1.getSelectedRows();
        if (row.length > 0) {
            for (int i = row.length; i > 0; i--) {
                jtab2.removeRow(row[i - 1]);
            }
            jButton5.grabFocus();
        } else {
            if (jTable1.getRowCount() > 0) {
                newgen.presentation.NewGenMain.getAppletInstance().showInsufficientDataDialog(newgen.presentation.NewGenMain.getAppletInstance().getMyResource().getString("Selectarecordtobedeleted"));
                jTable1.grabFocus();
            }
        }
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        this.dispose();
    }

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {
        if (jTabbedPane1.getSelectedIndex() == 2) {
            try {
                commonValuesNotSet = false;
                JSONObject job = new JSONObject();
                String req = job.put("PatronCategory", cbPatronCategory.getSelectedItem().toString()).put("Libraryid", StaticValues.getInstance().getLibraryID()).put("OperationId", "54").toString();
                String res = ServletConnector.getInstance().sendJSONRequest("JSONServlet", req);
                JSONObject jret = new JSONObject(res);
                jsGlobalLoanlt.setValue(Integer.parseInt(jret.getString("OverAllLoanLimit")));
                if (jret.getString("AllowMultipleCopies").equals("") || jret.getString("AllowMultipleCopies").equals("N")) cbAllowMultipleCopies.setSelected(false); else cbAllowMultipleCopies.setSelected(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public void getPatronCategoriesForTheLibrary(String library) {
        String xmlStr = "";
        cbPatronCategory.removeAllItems();
        org.jdom.Element root = new org.jdom.Element("OperationId");
        root.setAttribute("no", "1");
        org.jdom.Element libraryId = new org.jdom.Element("LibraryId");
        libraryId.setText(newgen.presentation.NewGenMain.getAppletInstance().getLibraryId(library));
        root.addContent(libraryId);
        org.jdom.Document doc = new org.jdom.Document(root);
        xmlStr = (new org.jdom.output.XMLOutputter()).outputString(doc);
        xmlStr = servletConnector.sendRequest("CirculationPrivilegeMatrixServlet", xmlStr);
        org.jdom.Element root1 = newGenXMLGenerator.getRootElement(xmlStr);
        Object[] patronCategoryName = new Object[0];
        patronCategoryName = root1.getChildren("PatronCategoryName").toArray();
        java.util.ArrayList arrPatronCategory = new java.util.ArrayList();
        for (int i = 0; i < patronCategoryName.length; i++) {
            arrPatronCategory.add(((org.jdom.Element) patronCategoryName[i]).getText());
            java.util.Collections.sort(arrPatronCategory);
        }
        Object[] object = arrPatronCategory.toArray();
        for (int l = 0; l < object.length; l++) {
            cbPatronCategory.addItem(object[l]);
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                CirculationPrivilegeMatrix dialog = new CirculationPrivilegeMatrix(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    private javax.swing.ButtonGroup buttonGroup1;

    private javax.swing.ButtonGroup buttonGroup2;

    private javax.swing.ButtonGroup buttonGroup3;

    private javax.swing.ButtonGroup buttonGroup4;

    private javax.swing.JCheckBox cbAllowMultipleCopies;

    public javax.swing.JComboBox cbLibrary;

    private javax.swing.JComboBox cbPatronCategory;

    private javax.swing.JComboBox cbPhyform;

    private javax.swing.JCheckBox cbRenewal;

    private javax.swing.JButton jButton1;

    private javax.swing.JButton jButton2;

    private javax.swing.JButton jButton3;

    private javax.swing.JButton jButton4;

    private javax.swing.JButton jButton5;

    private javax.swing.JButton jButton6;

    private javax.swing.JFormattedTextField jFormattedTextField1;

    private javax.swing.JFormattedTextField jFormattedTextField2;

    private javax.swing.JFormattedTextField jFormattedTextField3;

    private javax.swing.JFormattedTextField jFormattedTextField4;

    private javax.swing.JFormattedTextField jFormattedTextField5;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel2;

    private javax.swing.JLabel jLabel3;

    private javax.swing.JLabel jLabel4;

    private javax.swing.JLabel jLabel5;

    private javax.swing.JLabel jLabel6;

    private javax.swing.JLabel jLabel7;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JPanel jPanel10;

    private javax.swing.JPanel jPanel11;

    private javax.swing.JPanel jPanel13;

    private javax.swing.JPanel jPanel14;

    private javax.swing.JPanel jPanel15;

    private javax.swing.JPanel jPanel16;

    private javax.swing.JPanel jPanel17;

    private javax.swing.JPanel jPanel18;

    private javax.swing.JPanel jPanel19;

    private javax.swing.JPanel jPanel2;

    private javax.swing.JPanel jPanel20;

    private javax.swing.JPanel jPanel21;

    private javax.swing.JPanel jPanel22;

    private javax.swing.JPanel jPanel23;

    private javax.swing.JPanel jPanel3;

    private javax.swing.JPanel jPanel4;

    private javax.swing.JPanel jPanel5;

    private javax.swing.JPanel jPanel7;

    private javax.swing.JPanel jPanel8;

    private javax.swing.JPanel jPanel9;

    private javax.swing.JRadioButton jRadioButton1;

    private javax.swing.JRadioButton jRadioButton2;

    private javax.swing.JRadioButton jRadioButton3;

    private javax.swing.JRadioButton jRadioButton4;

    private javax.swing.JRadioButton jRadioButton5;

    private javax.swing.JRadioButton jRadioButton6;

    private javax.swing.JScrollPane jScrollPane2;

    private javax.swing.JScrollPane jScrollPane3;

    private javax.swing.JTabbedPane jTabbedPane1;

    private javax.swing.JTable jTable1;

    private javax.swing.JTable jTable2;

    private javax.swing.JSpinner jsGlobalLoanlt;

    private javax.swing.JLabel lbGlobalLoanLimit;

    private javax.swing.JLabel lbLibrary;

    private javax.swing.JLabel lbLoanLt;

    private javax.swing.JLabel lbPatronCategory;

    private javax.swing.JLabel lbWithEffectFrom;

    private javax.swing.JLabel lbphysicalpresentform;

    private javax.swing.JLabel lbzero;

    private javax.swing.JRadioButton rbIndays;

    private javax.swing.JRadioButton rbInhours;

    private javax.swing.JRadioButton rbNextOccuring;

    private newgen.presentation.component.DateField wef;

    public boolean validateMethod() {
        boolean retn = false;
        int jspin = -1;
        int inday = -1;
        int inhors = -1;
        int lonlt = -1;
        int retld = -1;
        int loanperiod = -1;
        double celing = -1;
        try {
            jspin = (Integer) jsGlobalLoanlt.getValue();
        } catch (Exception ex) {
        }
        try {
            if (rbIndays.isSelected()) {
                loanperiod = Integer.parseInt(jFormattedTextField1.getValue().toString());
            } else if (rbInhours.isSelected()) {
                loanperiod = Integer.parseInt(jFormattedTextField2.getValue().toString());
            } else if (rbNextOccuring.isSelected()) {
                loanperiod = 1;
                defaulttablevalidation();
            }
        } catch (Exception ex) {
        }
        try {
            lonlt = Integer.parseInt(jFormattedTextField3.getValue().toString());
        } catch (Exception ex) {
        }
        try {
            retld = Integer.parseInt(jFormattedTextField4.getValue().toString());
        } catch (Exception ex) {
        }
        try {
            celing = Double.parseDouble(jFormattedTextField5.getValue().toString());
        } catch (Exception ex) {
        }
        if (jspin >= 0) {
            if (loanperiod > 0) {
                if (lonlt > 0) {
                    if (retld >= 0) {
                        if (celing >= 0.0) {
                            retn = true;
                        } else {
                            JOptionPane.showMessageDialog(this, NGLResourceBundle.getInstance().getString("MaximumCeilingOnOverdueCannotBeANegativeNumberLeaveTheMaximumCeilingOnOverdueAs0ToIgnoreIt"), NGLResourceBundle.getInstance().getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, NGLResourceBundle.getInstance().getString("EnterAValidRenewalLimit"), NGLResourceBundle.getInstance().getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, NGLResourceBundle.getInstance().getString("LoanLimitMustBeGreaterThanZero"), NGLResourceBundle.getInstance().getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, NGLResourceBundle.getInstance().getString("EnterAValidLoanPeriod"), NGLResourceBundle.getInstance().getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, NGLResourceBundle.getInstance().getString("OverallLoanLimitCannotBeANegativeNumberLeaveTheOverallLoanLimitAs0ToIgnoreIt"), NGLResourceBundle.getInstance().getString("ERROR"), JOptionPane.ERROR_MESSAGE);
        }
        return retn;
    }

    public void defaulttablevalidation() {
        int n = 0;
        int m = 0;
        if (modelAdvance.getRowCount() >= 1) {
            int ro = modelAdvance.getRowCount();
            for (int i = 0; i < ro; i++) {
                n = Integer.parseInt(modelAdvance.getValueAt(i, 0).toString());
                m = Integer.parseInt(modelAdvance.getValueAt(i, 1).toString());
                if (n >= 1 && n <= 31) {
                    valit = true;
                } else {
                    valit = false;
                }
                if (m >= 1 && m <= 12) {
                    valit = true;
                } else {
                    valit = false;
                }
            }
        }
    }

    public String xmlCreation() {
        String overduelt = "";
        String durationtype = "";
        String duration = "";
        String occday = "";
        String ocmnth = "";
        String holidays = "";
        String durtontype = "";
        String from = "";
        String to = "";
        String amount = "";
        String holidas1 = "";
        overduelt = jsGlobalLoanlt.getValue().toString();
        System.out.println("************" + jsGlobalLoanlt.getValue().toString());
        if (rbIndays.isSelected()) {
            durationtype = "Day";
            duration = jFormattedTextField1.getValue().toString();
        } else if (rbInhours.isSelected()) {
            durationtype = "Hour";
            duration = jFormattedTextField2.getValue().toString();
        } else if (rbNextOccuring.isSelected()) {
            durationtype = "NextOccurring";
        }
        if (jRadioButton1.isSelected()) {
            holidays = "INCLUDE";
        } else if (jRadioButton2.isSelected()) {
            holidays = "EXCLUDE";
        }
        if (jRadioButton3.isSelected()) {
            durtontype = "days";
        } else if (jRadioButton4.isSelected()) {
            durtontype = "hours";
        }
        if (jRadioButton5.isSelected()) {
            holidas1 = "EXCLUDE";
        } else if (jRadioButton6.isSelected()) {
            holidas1 = "INCLUDE";
        }
        org.jdom.Document doc = null;
        org.jdom.Element ele = new org.jdom.Element("Root");
        Element ele1 = new Element("OverallLoanLimit");
        ele1.setText(overduelt);
        ele.addContent(ele1);
        Element ele2 = new Element("LoanPeriod");
        Element ele3 = new Element("DurationType");
        ele3.setText(durationtype);
        ele2.addContent(ele3);
        if (!rbNextOccuring.isSelected()) {
            Element ele6 = new Element("Duration");
            ele6.setText(duration);
            ele2.addContent(ele6);
        }
        if (rbNextOccuring.isSelected()) {
            if (modelAdvance.getRowCount() > 0) {
                Element ele4 = new Element("Occurrances");
                for (int i = 0; i < modelAdvance.getRowCount(); i++) {
                    Element ele41 = new Element("Occurrance");
                    Element ele7 = new Element("day");
                    ele7.setText(modelAdvance.getValueAt(i, 0).toString());
                    ele41.addContent(ele7);
                    ele7 = new Element("month");
                    ele7.setText(modelAdvance.getValueAt(i, 1).toString());
                    ele41.addContent(ele7);
                    ele4.addContent(ele41);
                }
                ele2.addContent(ele4);
            }
        }
        Element ele5 = new Element("Holidays");
        ele5.setText(holidays);
        ele2.addContent(ele5);
        Element ele10 = new Element("OverDue");
        Element ele9 = new Element("DurationType");
        ele9.setText(durtontype);
        ele10.addContent(ele9);
        if (jtab2.getRowCount() > 0) {
            Element ele11 = new Element("Charges");
            for (int i = 0; i < jtab2.getRowCount(); i++) {
                Element ele12 = new Element("Charge");
                Element ele13 = new Element("From");
                ele13.setText(jtab2.getValueAt(i, 0).toString());
                ele12.addContent(ele13);
                Element ele14 = new Element("To");
                ele14.setText(jtab2.getValueAt(i, 1).toString());
                ele12.addContent(ele14);
                Element ele15 = new Element("Amount");
                ele15.setText(jtab2.getValueAt(i, 2).toString());
                ele12.addContent(ele15);
                ele11.addContent(ele12);
            }
            ele10.addContent(ele11);
        }
        Element ele16 = new Element("Holidays");
        ele16.setText(holidas1);
        ele10.addContent(ele16);
        ele.addContent(ele2);
        ele.addContent(ele10);
        doc = new Document(ele);
        String xmlStr = (new org.jdom.output.XMLOutputter("\t", true)).outputString(doc);
        System.out.println("***********" + xmlStr);
        return xmlStr;
    }

    public void JsonStrins(String type) {
        try {
            boolean renwel = true;
            String xml = "";
            String patroncatgory = "";
            int loanlt = 0;
            int renlt = 0;
            double celovrdue = 0;
            long wefm;
            int libId = 0;
            int patronId = 0;
            int metrlType = 0;
            String str = getCurrentData();
            String entryId = "";
            String entryLibId = "";
            libId = Integer.parseInt(newgen.presentation.NewGenMain.getAppletInstance().getLibraryID());
            metrlType = Integer.parseInt(newgen.presentation.NewGenMain.getAppletInstance().getMaterialId(cbPhyform.getSelectedItem().toString()));
            patroncatgory = cbPatronCategory.getSelectedItem().toString();
            wefm = Long.parseLong(wef.getDate().toString());
            loanlt = Integer.parseInt(jFormattedTextField3.getValue().toString());
            renlt = Integer.parseInt(jFormattedTextField4.getValue().toString());
            celovrdue = Double.parseDouble(jFormattedTextField5.getValue().toString());
            entryId = StaticValues.getInstance().getEntryID();
            entryLibId = StaticValues.getInstance().getLibraryID();
            xml = xmlCreation();
            if (!cbRenewal.isSelected()) {
                renwel = false;
            }
            JSONObject jo = new JSONObject();
            jo.put("OperationId", "23");
            jo.put("LibraryId", libId);
            jo.put("PatronCategory", patroncatgory);
            jo.put("MeterialTypeId", metrlType);
            jo.put("OverallLoanLimit", jsGlobalLoanlt.getValue().toString());
            jo.put("AllowMultipleCopies", cbAllowMultipleCopies.isSelected());
            jo.put("WEF", wefm);
            jo.put("AllowRenewalThroughOPAC", renwel);
            jo.put("xml", xml);
            jo.put("LoanLimit", loanlt);
            jo.put("RenewalLimit", renlt);
            jo.put("CeilingOfOverdue", celovrdue);
            jo.put("EntryId", entryId);
            jo.put("EntryLibraryId", entryLibId);
            jo.put("Type", type);
            jo.put("OldData", str);
            jo.put("CommonValuesNotSet", commonValuesNotSet);
            String req = jo.toString();
            System.out.println(req);
            String res = ServletConnector.getInstance().sendJSONRequest("JSONServlet", req);
            JSONObject jon = new JSONObject(res);
            String status = jon.getString("STATUS");
            if (status.equals("0")) {
                stats = error;
            } else if (status.equals("1")) {
                stats = inserted;
            } else if (status.equals("2")) {
                stats = updated;
            }
            System.out.println("*************************************" + jo.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setOldData(String strn, String paCat, String phypreform, String libname, String wef1) {
        refreshData();
        cbPatronCategory.setSelectedItem(paCat);
        cbLibrary.setSelectedItem(libname);
        cbPhyform.setSelectedItem(phypreform);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dt = sdf.parse(wef1);
            SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
            String st = format.format(dt);
            wef.setDate(st);
            JSONObject jo = new JSONObject(strn);
            jo.put("OperationId", "25");
            String req = jo.toString();
            System.out.println(req);
            String res = ServletConnector.getInstance().sendJSONRequest("JSONServlet", req);
            System.out.println(res);
            JSONObject jon1 = new JSONObject(res);
            String opac = jon1.getString("renewal_through_opac");
            String lonlt = jon1.getString("loan_limit");
            String renlt = jon1.getString("renewal_limit");
            String max_cel = jon1.getString("max_ceil_on_fine");
            String otrdtl = jon1.getString("other_details");
            int overLoanLimit = Integer.parseInt(jon1.getString("OverallLoanLimit"));
            String mulCop = jon1.getString("AllowMultipleCopies");
            String selectData = "";
            if (opac.equals("Y")) {
                cbRenewal.setSelected(true);
            } else if (opac.equals("N")) {
                cbRenewal.setSelected(false);
            }
            if (mulCop.equals("Y")) {
                cbAllowMultipleCopies.setSelected(true);
            } else if (mulCop.equals("N")) {
                cbAllowMultipleCopies.setSelected(false);
            }
            int on = 0;
            if (lonlt != null && !lonlt.equals("")) {
                on = Integer.parseInt(lonlt);
            }
            int rn = 0;
            if (renlt != null && !renlt.equals("")) {
                rn = Integer.parseInt(renlt);
            }
            double ce = 0.0;
            if (max_cel != null && !max_cel.equals("")) {
                Double.parseDouble(max_cel);
            }
            jFormattedTextField3.setValue(on);
            jFormattedTextField4.setValue(rn);
            jFormattedTextField5.setValue(ce);
            try {
                SAXBuilder sb = new SAXBuilder();
                Document doc = sb.build(new java.io.StringReader(otrdtl));
                String ovrlonlt = " ";
                String lonpty = " ";
                String durton = " ";
                String ocren = " ";
                String day = " ";
                String mont = " ";
                String lpinclude = " ";
                String odinclude = "";
                String oduron = "";
                if (doc != null) {
                    ovrlonlt = doc.getRootElement().getChildText("OverallLoanLimit");
                    int ol = 0;
                    try {
                        ol = Integer.parseInt(ovrlonlt);
                    } catch (Exception ex) {
                    }
                    jsGlobalLoanlt.setValue(overLoanLimit);
                    int dutn = 0;
                    try {
                        durton = doc.getRootElement().getChild("LoanPeriod").getChildText("Duration");
                        dutn = Integer.parseInt(durton);
                    } catch (Exception ex) {
                    }
                    lonpty = doc.getRootElement().getChild("LoanPeriod").getChildText("DurationType");
                    if (lonpty.trim().toUpperCase().equals("DAY")) {
                        rbIndays.setSelected(true);
                        ((java.awt.CardLayout) this.jPanel8.getLayout()).show(jPanel8, "card2");
                        jFormattedTextField1.setValue(dutn);
                    } else if (lonpty.trim().toUpperCase().equals("HOUR")) {
                        rbInhours.setSelected(true);
                        ((java.awt.CardLayout) this.jPanel8.getLayout()).show(jPanel8, "card3");
                        jFormattedTextField2.setValue(dutn);
                    } else if (lonpty.trim().toUpperCase().equals("NEXTOCCURRING")) {
                        rbNextOccuring.setSelected(true);
                        ((java.awt.CardLayout) this.jPanel8.getLayout()).show(jPanel8, "card4");
                    }
                }
                if (rbNextOccuring.isSelected()) {
                    List ls = doc.getRootElement().getChild("LoanPeriod").getChild("Occurrances").getChildren("Occurrance");
                    if (ls.size() > 0) {
                        for (int i = 0; i < ls.size(); i++) {
                            Object[][] row = new Object[1][1];
                            day = ((org.jdom.Element) ls.get(i)).getChildText("day");
                            mont = ((org.jdom.Element) ls.get(i)).getChildText("month");
                            int day1 = Integer.parseInt(day);
                            int mont1 = Integer.parseInt(mont);
                            modelAdvance.addRow(row);
                            if (modelAdvance.getRowCount() == 1) {
                                modelAdvance.setValueAt(day1, modelAdvance.getRowCount() - 1, 0);
                                modelAdvance.setValueAt(mont1, modelAdvance.getRowCount() - 1, 1);
                            } else {
                                modelAdvance.setValueAt(day1, modelAdvance.getRowCount() - 1, 0);
                                modelAdvance.setValueAt(mont1, modelAdvance.getRowCount() - 1, 1);
                            }
                        }
                    }
                }
                oduron = doc.getRootElement().getChild("OverDue").getChildText("DurationType");
                lpinclude = doc.getRootElement().getChild("LoanPeriod").getChildText("Holidays");
                odinclude = doc.getRootElement().getChild("OverDue").getChildText("Holidays");
                if (lpinclude.toUpperCase().equals("INCLUDE")) {
                    jRadioButton1.setSelected(true);
                } else if (lpinclude.toUpperCase().equals("EXCLUDE")) {
                    jRadioButton2.setSelected(true);
                }
                if (oduron.trim().toUpperCase().equals("DAYS")) {
                    jRadioButton3.setSelected(true);
                } else if (oduron.trim().toUpperCase().equals("HOURS")) {
                    jRadioButton4.setSelected(true);
                }
                if (odinclude.toUpperCase().equals("INCLUDE")) {
                    jRadioButton6.setSelected(true);
                } else if (odinclude.toUpperCase().equals("EXCLUDE")) {
                    jRadioButton5.setSelected(true);
                }
                try {
                    List lst = doc.getRootElement().getChild("OverDue").getChild("Charges").getChildren("Charge");
                    if (lst.size() > 0) {
                        for (int i = 0; i < lst.size(); i++) {
                            String frm = ((org.jdom.Element) lst.get(i)).getChildText("From");
                            String to = ((org.jdom.Element) lst.get(i)).getChildText("To");
                            String amont = ((org.jdom.Element) lst.get(i)).getChildText("Amount");
                            Object[][] row = new Object[1][1];
                            jtab2.addRow(row);
                            if (jtab2.getRowCount() == 1) {
                                jtab2.setValueAt(frm, jtab2.getRowCount() - 1, 0);
                                jtab2.setValueAt(to, jtab2.getRowCount() - 1, 1);
                                jtab2.setValueAt(amont, jtab2.getRowCount() - 1, 2);
                            } else {
                                jtab2.setValueAt(frm, jtab2.getRowCount() - 1, 0);
                                jtab2.setValueAt(to, jtab2.getRowCount() - 1, 1);
                                jtab2.setValueAt(amont, jtab2.getRowCount() - 1, 2);
                            }
                        }
                    }
                } catch (Exception ex) {
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void refreshData() {
        cbLibrary.setSelectedIndex(0);
        cbPatronCategory.setSelectedIndex(0);
        cbPhyform.setSelectedIndex(0);
        jsGlobalLoanlt.setValue(0);
        cbRenewal.setSelected(false);
        rbIndays.setSelected(true);
        jFormattedTextField1.setValue(0);
        jFormattedTextField2.setValue(0);
        jFormattedTextField3.setValue(0);
        jFormattedTextField4.setValue(0);
        jFormattedTextField5.setValue(0);
        jRadioButton1.setSelected(true);
        jRadioButton3.setSelected(true);
        jRadioButton6.setSelected(true);
        for (int i = 0; i < modelAdvance.getRowCount(); i++) {
            modelAdvance.removeRow(i);
        }
        for (int i = 0; i < jtab2.getRowCount(); i++) {
            jtab2.removeRow(i);
        }
    }

    /**
     * @return the mode
     */
    public int getMode() {
        return mode;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * @return the currentData
     */
    public String getCurrentData() {
        return currentData;
    }

    /**
     * @param currentData the currentData to set
     */
    public void setCurrentData(String currentData) {
        this.currentData = currentData;
    }
}

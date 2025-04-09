package paymentsimulatorgui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.LineNumberReader;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

/**
 * The application's main frame.
 */
public class View extends FrameView {

    public static final String KEY_PAYMENTS = "payments_file";

    public static final String KEY_FUNDS = "funds_file";

    public static final String KEY_OVERDRAFTS = "overdrafts_file";

    public static final String KEY_BILATERAL_LIMITS = "bilaterallimits_file";

    public static final String KEY_OUTPUT = "output_file";

    public static final String KEY_STATS = "bank_stats_file";

    public static final String KEY_DATEFORMAT = "date_format";

    public static final String KEY_TIMEFORMAT = "time_format";

    public static final String KEY_OPENING = "opening_time";

    public static final String KEY_CLOSING = "closing_time";

    public static final String KEY_SEPARATOR = "field_separator";

    public static final String KEY_DECIMAL = "decimal_separator";

    public static final String KEY_SETTLEMENT = "settlement_method";

    public static final String KEY_QOPTDELAY = "queue_optimization_delay";

    public static final String KEY_ALLOWNEGATIVE = "allow_negative_balance";

    public static final String KEY_SPLITTING = "splitting_method";

    public static final String KEY_THRESHOLD = "splitting_threshold";

    public static final String KEY_MINLIQUIDITY = "minimum_liquidity";

    public boolean running = false;

    public View(SingleFrameApplication app) {
        super(app);
        initComponents();
    }

    private String checkFields() {
        String err = "";
        err += checkEmpty(paymentsfield, "Payments file");
        err += checkEmpty(dateformatfield, "Date format");
        err += checkEmpty(timeformatfield, "Time format");
        err += checkEmpty(fieldseparatorfield, "Field separator");
        err += checkEmpty(decimalseparatorfield, "Decimal separator");
        err += checkEmpty(openingtimefield, "Opening time");
        err += checkEmpty(closingtimefield, "Closing time");
        if (doqoptcb.isSelected()) {
            err += checkInt(qoptdelayfield, "Queue optimization delay");
        }
        if (splitthresholdcb.isSelected()) {
            err += checkInt(thresholdfield, "Splitting threshold");
        }
        if (splitliquiditycb.isSelected()) {
            err += checkInt(minliquidityfield, "Minimum liquidity for splitting");
        }
        String d = dateformatfield.getText();
        try {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat(d);
            sdf.format(date);
        } catch (Exception e) {
            err += "Syntax error in date format\n";
        }
        if (d.contains("m")) JOptionPane.showMessageDialog(null, "Warning: m in date format means minutes,\n" + "use M for months");
        if (d.contains("D")) JOptionPane.showMessageDialog(null, "Warning: D in date format means day in year,\n" + "use d for day in month");
        d = timeformatfield.getText();
        try {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat(d);
            sdf.format(date);
        } catch (Exception e) {
            err += "Syntax error in time format\n";
        }
        if (d.contains("h")) JOptionPane.showMessageDialog(null, "Warning: h in time format means hours in am/pm,\n" + "use H for hours in 0-23 range");
        try {
            String time = openingtimefield.getText();
            SimpleDateFormat sdf = new SimpleDateFormat(timeformatfield.getText());
            sdf.parse(time);
        } catch (Exception e) {
            err += "Parsing error in opening time\n";
        }
        try {
            String time = closingtimefield.getText();
            SimpleDateFormat sdf = new SimpleDateFormat(timeformatfield.getText());
            sdf.parse(time);
        } catch (Exception e) {
            err += "Parsing error in closing time\n";
        }
        return err;
    }

    private String checkEmpty(JTextField f, String name) {
        String s = f.getText().trim();
        if (s.equals("")) return name + " field is empty\n";
        return "";
    }

    private String checkInt(JTextField f, String name) {
        String s = f.getText();
        int val = 0;
        try {
            val = Integer.parseInt(s);
        } catch (Exception e) {
            return name + " must contain an integer number\n";
        }
        if (val < 0) return name + " must not be negative\n";
        return "";
    }

    private void start() {
        Runner r = new Runner(this);
        r.start();
    }

    public void done() {
        running = false;
        gobutton.setText("START");
    }

    private class Runner extends Thread {

        View view;

        public Runner(View v) {
            view = v;
        }

        @Override
        public void run() {
            Simulator s = new Simulator(view);
            s.run();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        mainPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        paymentsfield = new javax.swing.JTextField();
        loadpaymentsbutton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        fundsfield = new javax.swing.JTextField();
        loadfundsbutton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        dateformatfield = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        fieldseparatorfield = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        decimalseparatorfield = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        openingtimefield = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        closingtimefield = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        qoptdelayfield = new javax.swing.JTextField();
        loaddefaultsbutton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        gobutton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        logarea = new javax.swing.JTextArea();
        clearlogbutton = new javax.swing.JButton();
        saveparametersbutton = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        timeformatfield = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        doqoptcb = new javax.swing.JCheckBox();
        allownegativecb = new javax.swing.JCheckBox();
        settlementlist = new javax.swing.JComboBox();
        jLabel17 = new javax.swing.JLabel();
        thresholdfield = new javax.swing.JTextField();
        splitnonecb = new javax.swing.JRadioButton();
        splitthresholdcb = new javax.swing.JRadioButton();
        splitliquiditycb = new javax.swing.JRadioButton();
        minliquidityfield = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        overdraftsfield = new javax.swing.JTextField();
        loadoverdraftsbutton = new javax.swing.JButton();
        bilaterallimitsfield = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        bilaterallimitsbutton = new javax.swing.JButton();
        splittinggroup = new javax.swing.ButtonGroup();
        mainPanel.setName("mainPanel");
        mainPanel.setPreferredSize(new java.awt.Dimension(500, 800));
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(paymentsimulatorgui.App.class).getContext().getResourceMap(View.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text"));
        jLabel1.setName("jLabel1");
        paymentsfield.setText(resourceMap.getString("paymentsfield.text"));
        paymentsfield.setName("paymentsfield");
        loadpaymentsbutton.setText(resourceMap.getString("loadpaymentsbutton.text"));
        loadpaymentsbutton.setName("loadpaymentsbutton");
        loadpaymentsbutton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadPaymentsPressed(evt);
            }
        });
        jLabel3.setText(resourceMap.getString("jLabel3.text"));
        jLabel3.setName("jLabel3");
        fundsfield.setText(resourceMap.getString("fundsfield.text"));
        fundsfield.setName("fundsfield");
        loadfundsbutton.setText(resourceMap.getString("loadfundsbutton.text"));
        loadfundsbutton.setName("loadfundsbutton");
        loadfundsbutton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadFundsPressed(evt);
            }
        });
        jLabel4.setText(resourceMap.getString("jLabel4.text"));
        jLabel4.setName("jLabel4");
        dateformatfield.setText(resourceMap.getString("dateformatfield.text"));
        dateformatfield.setName("dateformatfield");
        jLabel5.setText(resourceMap.getString("jLabel5.text"));
        jLabel5.setName("jLabel5");
        fieldseparatorfield.setText(resourceMap.getString("fieldseparatorfield.text"));
        fieldseparatorfield.setName("fieldseparatorfield");
        jLabel6.setText(resourceMap.getString("jLabel6.text"));
        jLabel6.setName("jLabel6");
        decimalseparatorfield.setText(resourceMap.getString("decimalseparatorfield.text"));
        decimalseparatorfield.setName("decimalseparatorfield");
        jLabel9.setText(resourceMap.getString("jLabel9.text"));
        jLabel9.setName("jLabel9");
        openingtimefield.setColumns(20);
        openingtimefield.setName("openingtimefield");
        jLabel10.setText(resourceMap.getString("jLabel10.text"));
        jLabel10.setName("jLabel10");
        closingtimefield.setColumns(5);
        closingtimefield.setName("closingtimefield");
        jLabel12.setText(resourceMap.getString("jLabel12.text"));
        jLabel12.setName("jLabel12");
        jLabel13.setText(resourceMap.getString("jLabel13.text"));
        jLabel13.setName("jLabel13");
        qoptdelayfield.setText(resourceMap.getString("qoptdelayfield.text"));
        qoptdelayfield.setName("qoptdelayfield");
        loaddefaultsbutton.setText(resourceMap.getString("loaddefaultsbutton.text"));
        loaddefaultsbutton.setName("loaddefaultsbutton");
        loaddefaultsbutton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadDefaultsPressed(evt);
            }
        });
        jSeparator1.setName("jSeparator1");
        gobutton.setFont(resourceMap.getFont("gobutton.font"));
        gobutton.setText(resourceMap.getString("gobutton.text"));
        gobutton.setName("gobutton");
        gobutton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startPressed(evt);
            }
        });
        jScrollPane1.setName("jScrollPane1");
        logarea.setColumns(20);
        logarea.setFont(resourceMap.getFont("logarea.font"));
        logarea.setRows(5);
        logarea.setName("logarea");
        jScrollPane1.setViewportView(logarea);
        clearlogbutton.setText(resourceMap.getString("clearlogbutton.text"));
        clearlogbutton.setName("clearlogbutton");
        clearlogbutton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearLogPressed(evt);
            }
        });
        saveparametersbutton.setText(resourceMap.getString("saveparametersbutton.text"));
        saveparametersbutton.setName("saveparametersbutton");
        saveparametersbutton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveParametersPressed(evt);
            }
        });
        jLabel14.setText(resourceMap.getString("jLabel14.text"));
        jLabel14.setName("jLabel14");
        jLabel15.setText(resourceMap.getString("jLabel15.text"));
        jLabel15.setName("jLabel15");
        timeformatfield.setText(resourceMap.getString("timeformatfield.text"));
        timeformatfield.setName("timeformatfield");
        jLabel16.setText(resourceMap.getString("jLabel16.text"));
        jLabel16.setName("jLabel16");
        doqoptcb.setSelected(true);
        doqoptcb.setText(resourceMap.getString("doqoptcb.text"));
        doqoptcb.setName("doqoptcb");
        doqoptcb.addItemListener(new java.awt.event.ItemListener() {

            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                doqoptchanged(evt);
            }
        });
        allownegativecb.setText(resourceMap.getString("allownegativecb.text"));
        allownegativecb.setName("allownegativecb");
        settlementlist.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Real-time gross settlement", "Receipt reactive gross settlement" }));
        settlementlist.setName("settlementlist");
        jLabel17.setText(resourceMap.getString("jLabel17.text"));
        jLabel17.setName("jLabel17");
        thresholdfield.setText(resourceMap.getString("thresholdfield.text"));
        thresholdfield.setEnabled(false);
        thresholdfield.setName("thresholdfield");
        splittinggroup.add(splitnonecb);
        splitnonecb.setSelected(true);
        splitnonecb.setText(resourceMap.getString("splitnonecb.text"));
        splitnonecb.setName("splitnonecb");
        splitnonecb.addItemListener(new java.awt.event.ItemListener() {

            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                splitChanged(evt);
            }
        });
        splittinggroup.add(splitthresholdcb);
        splitthresholdcb.setText(resourceMap.getString("splitthresholdcb.text"));
        splitthresholdcb.setName("splitthresholdcb");
        splitthresholdcb.addItemListener(new java.awt.event.ItemListener() {

            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                splitChanged(evt);
            }
        });
        splittinggroup.add(splitliquiditycb);
        splitliquiditycb.setText(resourceMap.getString("splitliquiditycb.text"));
        splitliquiditycb.setName("splitliquiditycb");
        splitliquiditycb.addItemListener(new java.awt.event.ItemListener() {

            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                splitChanged(evt);
            }
        });
        minliquidityfield.setEnabled(false);
        minliquidityfield.setName("minliquidityfield");
        jLabel2.setText(resourceMap.getString("jLabel2.text"));
        jLabel2.setName("jLabel2");
        jLabel7.setText(resourceMap.getString("jLabel7.text"));
        jLabel7.setName("jLabel7");
        overdraftsfield.setName("overdraftsfield");
        loadoverdraftsbutton.setText(resourceMap.getString("loadoverdraftsbutton.text"));
        loadoverdraftsbutton.setName("loadoverdraftsbutton");
        loadoverdraftsbutton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadOverdraftsPressed(evt);
            }
        });
        bilaterallimitsfield.setName("bilaterallimitsfield");
        jLabel8.setText(resourceMap.getString("jLabel8.text"));
        jLabel8.setName("jLabel8");
        bilaterallimitsbutton.setText(resourceMap.getString("bilaterallimitsbutton.text"));
        bilaterallimitsbutton.setName("bilaterallimitsbutton");
        bilaterallimitsbutton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadBilateralLimitsPressed(evt);
            }
        });
        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, mainPanelLayout.createSequentialGroup().add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false).add(org.jdesktop.layout.GroupLayout.LEADING, mainPanelLayout.createSequentialGroup().addContainerGap().add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 494, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(org.jdesktop.layout.GroupLayout.LEADING, mainPanelLayout.createSequentialGroup().add(147, 147, 147).add(jLabel13).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(qoptdelayfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 47, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(org.jdesktop.layout.GroupLayout.LEADING, mainPanelLayout.createSequentialGroup().addContainerGap().add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(clearlogbutton).add(gobutton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(mainPanelLayout.createSequentialGroup().add(loaddefaultsbutton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(saveparametersbutton)).add(mainPanelLayout.createSequentialGroup().add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jLabel5).add(jLabel6)).add(47, 47, 47).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(decimalseparatorfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(fieldseparatorfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))).add(org.jdesktop.layout.GroupLayout.LEADING, mainPanelLayout.createSequentialGroup().addContainerGap().add(jLabel17).add(56, 56, 56).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(splitnonecb).add(mainPanelLayout.createSequentialGroup().add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(splitthresholdcb).add(splitliquiditycb)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(thresholdfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 88, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(mainPanelLayout.createSequentialGroup().add(minliquidityfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 88, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel2)))).add(settlementlist, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))).add(org.jdesktop.layout.GroupLayout.LEADING, mainPanelLayout.createSequentialGroup().addContainerGap().add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jLabel7).add(jLabel8)).add(6, 6, 6).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(mainPanelLayout.createSequentialGroup().add(dateformatfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 115, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel14)).add(mainPanelLayout.createSequentialGroup().add(bilaterallimitsfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 262, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(bilaterallimitsbutton)).add(mainPanelLayout.createSequentialGroup().add(overdraftsfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 262, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(loadoverdraftsbutton)).add(mainPanelLayout.createSequentialGroup().add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(fundsfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 262, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(paymentsfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 262, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(loadpaymentsbutton).add(loadfundsbutton))))).add(org.jdesktop.layout.GroupLayout.LEADING, mainPanelLayout.createSequentialGroup().addContainerGap().add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 494, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(org.jdesktop.layout.GroupLayout.LEADING, mainPanelLayout.createSequentialGroup().addContainerGap().add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(mainPanelLayout.createSequentialGroup().add(jLabel9).add(38, 38, 38).add(openingtimefield, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)).add(mainPanelLayout.createSequentialGroup().add(jLabel15).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 46, Short.MAX_VALUE).add(timeformatfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 115, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(jLabel1).add(jLabel3).add(jLabel4).add(mainPanelLayout.createSequentialGroup().add(jLabel10).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 43, Short.MAX_VALUE).add(closingtimefield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(doqoptcb).add(allownegativecb).add(jLabel12)).add(10, 10, 10).add(jLabel16).add(14, 14, 14))).addContainerGap()));
        mainPanelLayout.linkSize(new java.awt.Component[] { closingtimefield, dateformatfield, openingtimefield, qoptdelayfield, timeformatfield }, org.jdesktop.layout.GroupLayout.HORIZONTAL);
        mainPanelLayout.linkSize(new java.awt.Component[] { fundsfield, paymentsfield }, org.jdesktop.layout.GroupLayout.HORIZONTAL);
        mainPanelLayout.linkSize(new java.awt.Component[] { decimalseparatorfield, fieldseparatorfield }, org.jdesktop.layout.GroupLayout.HORIZONTAL);
        mainPanelLayout.linkSize(new java.awt.Component[] { minliquidityfield, thresholdfield }, org.jdesktop.layout.GroupLayout.HORIZONTAL);
        mainPanelLayout.setVerticalGroup(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(mainPanelLayout.createSequentialGroup().addContainerGap().add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel1).add(paymentsfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(loadpaymentsbutton)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel3).add(loadfundsbutton).add(fundsfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel7).add(overdraftsfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(loadoverdraftsbutton)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel8).add(bilaterallimitsfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(bilaterallimitsbutton)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel4).add(dateformatfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel14)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel15).add(jLabel16).add(timeformatfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel9).add(openingtimefield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel10).add(closingtimefield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(doqoptcb).add(jLabel13).add(qoptdelayfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(allownegativecb).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel12).add(settlementlist, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jLabel17).add(mainPanelLayout.createSequentialGroup().add(splitnonecb).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(splitthresholdcb).add(thresholdfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(splitliquiditycb).add(minliquidityfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel2)))).add(9, 9, 9).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(fieldseparatorfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel5)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(decimalseparatorfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel6)).add(13, 13, 13).add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(loaddefaultsbutton).add(saveparametersbutton)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(gobutton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(clearlogbutton).add(17, 17, 17)));
        setComponent(mainPanel);
    }

    private void loadDefaultsPressed(java.awt.event.ActionEvent evt) {
        File f = Utils.loadFile("Load parameters file", "Text files", "txt");
        if (f == null) return;
        Properties props = new Properties();
        try {
            readProps(props, f);
            paymentsfield.setText(props.getProperty(KEY_PAYMENTS, ""));
            fundsfield.setText(props.getProperty(KEY_FUNDS, ""));
            overdraftsfield.setText(props.getProperty(KEY_OVERDRAFTS, ""));
            bilaterallimitsfield.setText(props.getProperty(KEY_BILATERAL_LIMITS, ""));
            dateformatfield.setText(props.getProperty(KEY_DATEFORMAT, "yyyyMMdd"));
            timeformatfield.setText(props.getProperty(KEY_TIMEFORMAT, "HHmmss"));
            fieldseparatorfield.setText(props.getProperty(KEY_SEPARATOR, ","));
            decimalseparatorfield.setText(props.getProperty(KEY_DECIMAL, "."));
            openingtimefield.setText(props.getProperty(KEY_OPENING, ""));
            closingtimefield.setText(props.getProperty(KEY_CLOSING, ""));
            String s = props.getProperty(KEY_SETTLEMENT);
            if (s.equals("1")) settlementlist.setSelectedIndex(0); else settlementlist.setSelectedIndex(1);
            int qoptdelay = Integer.parseInt(props.getProperty(KEY_QOPTDELAY, ""));
            boolean on = qoptdelay > 0;
            doqoptcb.setSelected(on);
            if (on) qoptdelayfield.setText("" + qoptdelay);
            enableQOptDelay(on);
            int splitmethod = Integer.parseInt(props.getProperty(KEY_SPLITTING, "0"));
            if (splitmethod == 0) splitnonecb.setSelected(true); else if (splitmethod == 1) splitthresholdcb.setSelected(true); else splitliquiditycb.setSelected(true);
            enableThresholds(splitmethod);
            thresholdfield.setText(props.getProperty(KEY_THRESHOLD, ""));
            minliquidityfield.setText(props.getProperty(KEY_MINLIQUIDITY, ""));
            s = props.getProperty(KEY_ALLOWNEGATIVE, "0");
            allownegativecb.setSelected(s.equals("1"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error in processing parameter file: " + e.getMessage());
        }
    }

    private void readProps(Properties props, File f) throws Exception {
        LineNumberReader lnr = new LineNumberReader(new FileReader(f));
        String line = null;
        while ((line = lnr.readLine()) != null) {
            int pos = line.indexOf("=");
            if (pos == -1) continue;
            String key = line.substring(0, pos).trim();
            String val = "";
            if (pos < line.length() - 1) val = line.substring(pos + 1).trim();
            props.setProperty(key, val);
        }
        lnr.close();
    }

    private void writeProps(Properties props, File f) throws Exception {
        FileOutputStream out = new FileOutputStream(f);
        Enumeration e = props.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String val = (String) props.getProperty(key);
            String line = key + " = " + val + "\n";
            out.write(line.getBytes());
        }
        out.close();
    }

    private void clearLogPressed(java.awt.event.ActionEvent evt) {
        logarea.setText("");
    }

    public void log(String s) {
        logarea.append(s + "\n");
        logarea.setCaretPosition(logarea.getText().length() - 1);
    }

    private void startPressed(java.awt.event.ActionEvent evt) {
        if (running) {
            gobutton.setText("START");
            running = false;
        } else {
            String error = checkFields();
            if (error.equals("")) {
                gobutton.setText("STOP");
                running = true;
                start();
            } else {
                JOptionPane.showMessageDialog(null, "Error:\n" + error);
            }
        }
    }

    private void loadPaymentsPressed(java.awt.event.ActionEvent evt) {
        File f = Utils.loadFile("Load payments file", "Payment files", "txt");
        if (f != null) paymentsfield.setText(f.getAbsolutePath());
    }

    private void loadFundsPressed(java.awt.event.ActionEvent evt) {
        File f = Utils.loadFile("Load funds file", "Funds files", "txt");
        if (f != null) fundsfield.setText(f.getAbsolutePath());
    }

    private void saveParametersPressed(java.awt.event.ActionEvent evt) {
        File f = Utils.saveFile("Load parameters file", "parameters.txt", "Text files", "txt", true);
        if (f == null) return;
        Properties props = new Properties();
        try {
            props.setProperty(KEY_PAYMENTS, paymentsfield.getText());
            props.setProperty(KEY_FUNDS, fundsfield.getText());
            props.setProperty(KEY_OVERDRAFTS, overdraftsfield.getText());
            props.setProperty(KEY_BILATERAL_LIMITS, bilaterallimitsfield.getText());
            props.setProperty(KEY_DATEFORMAT, dateformatfield.getText());
            props.setProperty(KEY_TIMEFORMAT, timeformatfield.getText());
            props.setProperty(KEY_SEPARATOR, fieldseparatorfield.getText());
            props.setProperty(KEY_DECIMAL, decimalseparatorfield.getText());
            props.setProperty(KEY_OPENING, openingtimefield.getText());
            props.setProperty(KEY_CLOSING, closingtimefield.getText());
            props.setProperty(KEY_ALLOWNEGATIVE, allownegativecb.isSelected() ? "1" : "0");
            String r = "0";
            if (doqoptcb.isSelected()) r = qoptdelayfield.getText();
            props.setProperty(KEY_QOPTDELAY, r);
            int split = 0;
            if (splitthresholdcb.isSelected()) split = 1;
            if (splitliquiditycb.isSelected()) split = 2;
            props.setProperty(KEY_SPLITTING, "" + split);
            props.setProperty(KEY_THRESHOLD, thresholdfield.getText());
            props.setProperty(KEY_MINLIQUIDITY, minliquidityfield.getText());
            int i = settlementlist.getSelectedIndex();
            props.setProperty(KEY_SETTLEMENT, i == 0 ? Simulator.METHOD_RTGS + "" : Simulator.METHOD_RRGS + "");
            writeProps(props, f);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error in writing parameter file: " + e.getMessage());
        }
    }

    private void doqoptchanged(java.awt.event.ItemEvent evt) {
        boolean on = doqoptcb.isSelected();
        enableQOptDelay(on);
    }

    private void splitChanged(java.awt.event.ItemEvent evt) {
        int split = 0;
        if (splitthresholdcb.isSelected()) split = 1;
        if (splitliquiditycb.isSelected()) split = 2;
        enableThresholds(split);
    }

    private void loadOverdraftsPressed(java.awt.event.ActionEvent evt) {
        File f = Utils.loadFile("Load overdrafts file", "Overdrafts files", "txt");
        if (f != null) overdraftsfield.setText(f.getAbsolutePath());
    }

    private void loadBilateralLimitsPressed(java.awt.event.ActionEvent evt) {
        File f = Utils.loadFile("Load bilateral limits file", "Bilateral Limits files", "txt");
        if (f != null) bilaterallimitsfield.setText(f.getAbsolutePath());
    }

    private void enableQOptDelay(boolean on) {
        qoptdelayfield.setEnabled(on);
    }

    private void enableThresholds(int split) {
        thresholdfield.setEnabled(split == 1);
        minliquidityfield.setEnabled(split == 2);
    }

    protected javax.swing.JCheckBox allownegativecb;

    protected javax.swing.JButton bilaterallimitsbutton;

    protected javax.swing.JTextField bilaterallimitsfield;

    protected javax.swing.JButton clearlogbutton;

    protected javax.swing.JTextField closingtimefield;

    protected javax.swing.JTextField dateformatfield;

    protected javax.swing.JTextField decimalseparatorfield;

    protected javax.swing.JCheckBox doqoptcb;

    protected javax.swing.JTextField fieldseparatorfield;

    protected javax.swing.JTextField fundsfield;

    protected javax.swing.JButton gobutton;

    protected javax.swing.JLabel jLabel1;

    protected javax.swing.JLabel jLabel10;

    protected javax.swing.JLabel jLabel12;

    protected javax.swing.JLabel jLabel13;

    protected javax.swing.JLabel jLabel14;

    protected javax.swing.JLabel jLabel15;

    protected javax.swing.JLabel jLabel16;

    protected javax.swing.JLabel jLabel17;

    protected javax.swing.JLabel jLabel2;

    protected javax.swing.JLabel jLabel3;

    protected javax.swing.JLabel jLabel4;

    protected javax.swing.JLabel jLabel5;

    protected javax.swing.JLabel jLabel6;

    protected javax.swing.JLabel jLabel7;

    protected javax.swing.JLabel jLabel8;

    protected javax.swing.JLabel jLabel9;

    protected javax.swing.JScrollPane jScrollPane1;

    protected javax.swing.JSeparator jSeparator1;

    protected javax.swing.JButton loaddefaultsbutton;

    protected javax.swing.JButton loadfundsbutton;

    protected javax.swing.JButton loadoverdraftsbutton;

    protected javax.swing.JButton loadpaymentsbutton;

    protected javax.swing.JTextArea logarea;

    protected javax.swing.JPanel mainPanel;

    protected javax.swing.JTextField minliquidityfield;

    protected javax.swing.JTextField openingtimefield;

    protected javax.swing.JTextField overdraftsfield;

    protected javax.swing.JTextField paymentsfield;

    protected javax.swing.JTextField qoptdelayfield;

    protected javax.swing.JButton saveparametersbutton;

    protected javax.swing.JComboBox settlementlist;

    protected javax.swing.JRadioButton splitliquiditycb;

    protected javax.swing.JRadioButton splitnonecb;

    protected javax.swing.JRadioButton splitthresholdcb;

    protected javax.swing.ButtonGroup splittinggroup;

    protected javax.swing.JTextField thresholdfield;

    protected javax.swing.JTextField timeformatfield;
}

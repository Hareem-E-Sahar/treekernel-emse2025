package ms2package;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * The Chart Program for MS2 output files.
 * Released under LGPL. For complete license, see copying.txt. Alternatively visit: http://www.gnu.org/licenses/lgpl.html
 * <P>
 * NOTE:<BR>
 * 1. The panel which holds the chart has to be <B>BorderLayout</B>. In <I>NetBeans 5.0</I>, right-click on the panel and select <B>BorderLayout</B>.<BR>
 * 2. It is important to note that RUN files are treated first than RAV file. This is to simplify the algorithm.<BR>
 * </P>
 * <P>
 * BUGS:<BR>
 * 1. Medium. View Data is broken and doesn't show all the data used in the Chart.<BR>
 * 2. Minor. Instead of deprecated StringTokenizer class, use String.split().<BR>
 * 3. Minor. If error is not present, still a comma (",") is shown.
 * 4. Minor. German localisation is not complete.
 * </P>
 * @author Anupam Srivastava, Fri, 24 Nov 2006 12:36:19 +0100
 * @version 2.3beta
 */
public class Ms2chart extends javax.swing.JFrame {

    /**
     * Returns the index of the largest integer in the given array of type <CODE>Integer</CODE>.
     * <P>For example,</P>
     * <P><BLOCKQUOTE><CODE>
     * int[] a1 = { 0, 1, 2, 100, 4, 50, 22 };<BR>
     * int i = findIndexOfLargestInt(a1); // i = 2<BR>
     * System.out.println(a1[i]); // ai[i] = 100<BR>
     * </CODE></BLOCKQUOTE></P>
     * @param arrayOfInts Array of the set of integers.
     * @return The index of the largest integer in the input array <B>arrayOfInts</B>.
     */
    public int findIndexOfLargestInt(int[] arrayOfInts) {
        int[] tempArray = new int[arrayOfInts.length];
        System.arraycopy(arrayOfInts, 0, tempArray, 0, arrayOfInts.length);
        Arrays.sort(tempArray);
        return binarySearch(arrayOfInts, tempArray[tempArray.length - 1]);
    }

    /**
     * Performs the standard binary search using two comparisons per level.
     * @param arrayOfInts 
     * @param intToBeFound 
     * @return The index where the item is found, or -1.
     */
    public static int binarySearch(int[] arrayOfInts, int intToBeFound) {
        int low = 0;
        int high = arrayOfInts.length - 1;
        int mid;
        while (low <= high) {
            mid = (low + high) / 2;
            if (arrayOfInts[mid] > intToBeFound) {
                low = mid + 1;
            } else if (arrayOfInts[mid] < intToBeFound) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return -1;
    }

    /**
     * Does initialisation.
     */
    public Ms2chart() {
        String nativeLF = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(nativeLF);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initComponents();
        selectedDirectory = startingDirectory;
        comboCaseList.setModel(new javax.swing.DefaultComboBoxModel(getDirList()));
        if (isHavingCases) {
            prepareData();
        } else {
            cbRUN.setEnabled(false);
            cbRAV.setEnabled(false);
            cbNVTE.setEnabled(false);
            cbEQUI.setEnabled(false);
            cbPROD.setEnabled(false);
            tfXMax.setEnabled(false);
            tfXMin.setEnabled(false);
            tfYMax.setEnabled(false);
            tfYMin.setEnabled(false);
            tfError.setEnabled(false);
            buttonLOG.setEnabled(false);
            buttonPAR.setEnabled(false);
            buttonRES.setEnabled(false);
            buttonDrawChart.setEnabled(false);
            buttonViewData.setEnabled(false);
            buttonPNGsave.setEnabled(false);
            buttonXAxisChange.setEnabled(false);
            buttonYAxisChange.setEnabled(false);
            buttonClear.setEnabled(false);
            comboXAxis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));
            listYAxises.setModel(new javax.swing.AbstractListModel() {

                String[] strings = new String[] { "" };

                public int getSize() {
                    return strings.length;
                }

                public Object getElementAt(int i) {
                    return strings[i];
                }
            });
            setLabelStatusMessage(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Browse_for_a_directory..."));
            panelChartContainer.removeAll();
            panelChartContainer.setVisible(false);
            panelChartContainer.setVisible(true);
        }
    }

    private void initComponents() {
        buttonGroupFileType = new javax.swing.ButtonGroup();
        panelControl = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        buttonBrowse = new javax.swing.JButton();
        comboCaseList = new javax.swing.JComboBox();
        cbRUN = new javax.swing.JCheckBox();
        cbRAV = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        cbNVTE = new javax.swing.JCheckBox();
        cbEQUI = new javax.swing.JCheckBox();
        cbPROD = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        comboXAxis = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listYAxises = new javax.swing.JList();
        jLabel6 = new javax.swing.JLabel();
        tfXMax = new javax.swing.JTextField();
        tfXMin = new javax.swing.JTextField();
        buttonXAxisChange = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        tfYMax = new javax.swing.JTextField();
        tfYMin = new javax.swing.JTextField();
        buttonYAxisChange = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        tfError = new javax.swing.JTextField();
        buttonDrawChart = new javax.swing.JButton();
        buttonPNGsave = new javax.swing.JButton();
        cbAutoDraw = new javax.swing.JCheckBox();
        cbAskDirectory = new javax.swing.JCheckBox();
        jLabel9 = new javax.swing.JLabel();
        buttonLOG = new javax.swing.JButton();
        buttonPAR = new javax.swing.JButton();
        buttonRES = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        buttonQuit = new javax.swing.JButton();
        buttonRestart = new javax.swing.JButton();
        buttonClear = new javax.swing.JButton();
        rbRUN_RAV = new javax.swing.JRadioButton();
        rbRTR = new javax.swing.JRadioButton();
        panelStatusBar = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        labelStatus = new javax.swing.JLabel();
        buttonViewData = new javax.swing.JButton();
        panelChartContainer = new javax.swing.JPanel();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Ms2 Chart");
        setName("ms2chart_frame");
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("ms2package/lang");
        jLabel3.setText(bundle.getString("Cases:"));
        buttonBrowse.setText(bundle.getString("Browse"));
        buttonBrowse.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonBrowseActionPerformed(evt);
            }
        });
        comboCaseList.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboCaseListActionPerformed(evt);
            }
        });
        cbRUN.setText(".run");
        cbRUN.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbRUN.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbRUN.addItemListener(new java.awt.event.ItemListener() {

            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbRUNItemStateChanged(evt);
            }
        });
        cbRAV.setSelected(true);
        cbRAV.setText(".rav");
        cbRAV.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbRAV.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbRAV.addItemListener(new java.awt.event.ItemListener() {

            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbRAVItemStateChanged(evt);
            }
        });
        cbNVTE.setText(bundle.getString("NVT_Equilibration"));
        cbNVTE.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbNVTE.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbNVTE.addItemListener(new java.awt.event.ItemListener() {

            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbNVTEItemStateChanged(evt);
            }
        });
        cbEQUI.setText(bundle.getString("Equilibration"));
        cbEQUI.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbEQUI.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbEQUI.addItemListener(new java.awt.event.ItemListener() {

            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbEQUIItemStateChanged(evt);
            }
        });
        cbPROD.setText(bundle.getString("Production"));
        cbPROD.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbPROD.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbPROD.addItemListener(new java.awt.event.ItemListener() {

            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbPRODItemStateChanged(evt);
            }
        });
        jLabel4.setText(bundle.getString("X_Axis:"));
        comboXAxis.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboXAxisActionPerformed(evt);
            }
        });
        jLabel5.setText(bundle.getString("Y_Axis:"));
        listYAxises.addListSelectionListener(new javax.swing.event.ListSelectionListener() {

            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listYAxisesValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(listYAxises);
        jLabel6.setText("X:");
        tfXMax.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfXMaxActionPerformed(evt);
            }
        });
        tfXMin.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfXMinActionPerformed(evt);
            }
        });
        buttonXAxisChange.setText("OK");
        buttonXAxisChange.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonXAxisChangeActionPerformed(evt);
            }
        });
        jLabel7.setText("Y:");
        tfYMax.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfYMaxActionPerformed(evt);
            }
        });
        tfYMin.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfYMinActionPerformed(evt);
            }
        });
        buttonYAxisChange.setText("OK");
        buttonYAxisChange.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonYAxisChangeActionPerformed(evt);
            }
        });
        jLabel8.setText(bundle.getString("Error:"));
        tfError.setEditable(false);
        buttonDrawChart.setText(bundle.getString("Draw_Chart"));
        buttonDrawChart.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDrawChartActionPerformed(evt);
            }
        });
        buttonPNGsave.setText(bundle.getString("Save_Image"));
        buttonPNGsave.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPNGsaveActionPerformed(evt);
            }
        });
        cbAutoDraw.setSelected(true);
        cbAutoDraw.setText(bundle.getString("Auto_draw"));
        cbAutoDraw.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbAutoDraw.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbAutoDraw.addItemListener(new java.awt.event.ItemListener() {

            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbAutoDrawItemStateChanged(evt);
            }
        });
        cbAskDirectory.setText(bundle.getString("Ask_directory"));
        cbAskDirectory.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbAskDirectory.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbAskDirectory.addItemListener(new java.awt.event.ItemListener() {

            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbAskDirectoryItemStateChanged(evt);
            }
        });
        jLabel9.setText(bundle.getString("View:"));
        buttonLOG.setText("log");
        buttonLOG.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLOGActionPerformed(evt);
            }
        });
        buttonPAR.setText("par");
        buttonPAR.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPARActionPerformed(evt);
            }
        });
        buttonRES.setText("res");
        buttonRES.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRESActionPerformed(evt);
            }
        });
        buttonQuit.setText(bundle.getString("Quit"));
        buttonQuit.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonQuitActionPerformed(evt);
            }
        });
        buttonRestart.setText(bundle.getString("Restart"));
        buttonRestart.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRestartActionPerformed(evt);
            }
        });
        buttonClear.setText(bundle.getString("Clear"));
        buttonClear.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonClearActionPerformed(evt);
            }
        });
        buttonGroupFileType.add(rbRUN_RAV);
        rbRUN_RAV.setText("run/rav");
        rbRUN_RAV.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbRUN_RAV.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbRUN_RAV.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbRUN_RAVActionPerformed(evt);
            }
        });
        buttonGroupFileType.add(rbRTR);
        rbRTR.setSelected(true);
        rbRTR.setText("rtr");
        rbRTR.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbRTR.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbRTR.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbRTRActionPerformed(evt);
            }
        });
        org.jdesktop.layout.GroupLayout panelControlLayout = new org.jdesktop.layout.GroupLayout(panelControl);
        panelControl.setLayout(panelControlLayout);
        panelControlLayout.setHorizontalGroup(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(panelControlLayout.createSequentialGroup().add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(panelControlLayout.createSequentialGroup().add(12, 12, 12).add(jLabel3).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 102, Short.MAX_VALUE).add(buttonBrowse)).add(panelControlLayout.createSequentialGroup().add(12, 12, 12).add(cbPROD)).add(panelControlLayout.createSequentialGroup().add(12, 12, 12).add(cbEQUI)).add(panelControlLayout.createSequentialGroup().add(12, 12, 12).add(cbNVTE)).add(panelControlLayout.createSequentialGroup().addContainerGap().add(buttonPNGsave).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(cbAskDirectory)).add(panelControlLayout.createSequentialGroup().addContainerGap().add(jLabel9).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 16, Short.MAX_VALUE).add(buttonLOG).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(buttonPAR).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(buttonRES)).add(panelControlLayout.createSequentialGroup().addContainerGap().add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)).add(panelControlLayout.createSequentialGroup().addContainerGap().add(buttonQuit).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE).add(buttonClear).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(buttonRestart)).add(panelControlLayout.createSequentialGroup().addContainerGap().add(comboCaseList, 0, 209, Short.MAX_VALUE)).add(org.jdesktop.layout.GroupLayout.TRAILING, panelControlLayout.createSequentialGroup().add(12, 12, 12).add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(cbRUN).add(cbRAV)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 111, Short.MAX_VALUE).add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(rbRTR).add(rbRUN_RAV))).add(panelControlLayout.createSequentialGroup().add(12, 12, 12).add(jLabel4).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(comboXAxis, 0, 160, Short.MAX_VALUE)).add(panelControlLayout.createSequentialGroup().addContainerGap().add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)).add(panelControlLayout.createSequentialGroup().addContainerGap().add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(panelControlLayout.createSequentialGroup().add(jLabel8).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(tfError, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)).add(panelControlLayout.createSequentialGroup().add(buttonDrawChart).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(cbAutoDraw).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 37, Short.MAX_VALUE)))).add(org.jdesktop.layout.GroupLayout.TRAILING, panelControlLayout.createSequentialGroup().addContainerGap().add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jLabel6).add(jLabel7)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(tfYMax, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(tfXMax, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(tfXMin, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE).add(tfYMin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(buttonXAxisChange).add(buttonYAxisChange))).add(panelControlLayout.createSequentialGroup().add(12, 12, 12).add(jLabel5).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))).addContainerGap()));
        panelControlLayout.linkSize(new java.awt.Component[] { buttonDrawChart, buttonPNGsave }, org.jdesktop.layout.GroupLayout.HORIZONTAL);
        panelControlLayout.linkSize(new java.awt.Component[] { buttonXAxisChange, buttonYAxisChange }, org.jdesktop.layout.GroupLayout.HORIZONTAL);
        panelControlLayout.linkSize(new java.awt.Component[] { tfXMax, tfXMin, tfYMax, tfYMin }, org.jdesktop.layout.GroupLayout.HORIZONTAL);
        panelControlLayout.setVerticalGroup(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(panelControlLayout.createSequentialGroup().addContainerGap().add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel3).add(buttonBrowse)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(comboCaseList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(cbRUN).add(rbRUN_RAV)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(cbRAV).add(rbRTR)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(cbNVTE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(cbEQUI).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(cbPROD).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel4).add(comboXAxis, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jLabel5).add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(buttonXAxisChange).add(tfXMin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(tfXMax, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel6)).add(12, 12, 12).add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(buttonYAxisChange).add(tfYMin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(tfYMax, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel7)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel8).add(tfError, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(buttonDrawChart).add(cbAutoDraw)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(buttonPNGsave).add(cbAskDirectory)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel9).add(buttonRES).add(buttonPAR).add(buttonLOG)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(panelControlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(buttonQuit).add(buttonRestart).add(buttonClear)).addContainerGap(27, Short.MAX_VALUE)));
        panelControlLayout.linkSize(new java.awt.Component[] { buttonXAxisChange, buttonYAxisChange }, org.jdesktop.layout.GroupLayout.VERTICAL);
        panelControlLayout.linkSize(new java.awt.Component[] { tfXMax, tfXMin, tfYMax, tfYMin }, org.jdesktop.layout.GroupLayout.VERTICAL);
        panelControlLayout.linkSize(new java.awt.Component[] { buttonDrawChart, buttonPNGsave }, org.jdesktop.layout.GroupLayout.VERTICAL);
        jLabel1.setText(bundle.getString("Status:"));
        buttonViewData.setText(bundle.getString("View_data"));
        buttonViewData.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonViewDataActionPerformed(evt);
            }
        });
        org.jdesktop.layout.GroupLayout panelStatusBarLayout = new org.jdesktop.layout.GroupLayout(panelStatusBar);
        panelStatusBar.setLayout(panelStatusBarLayout);
        panelStatusBarLayout.setHorizontalGroup(panelStatusBarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(panelStatusBarLayout.createSequentialGroup().addContainerGap().add(jLabel1).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(labelStatus).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 635, Short.MAX_VALUE).add(buttonViewData).addContainerGap()));
        panelStatusBarLayout.setVerticalGroup(panelStatusBarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(panelStatusBarLayout.createSequentialGroup().addContainerGap().add(panelStatusBarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel1).add(labelStatus).add(buttonViewData)).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        panelChartContainer.setLayout(new java.awt.BorderLayout());
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(panelControl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(panelChartContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE)).add(panelStatusBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup().addContainerGap().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(panelControl, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(panelChartContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 571, Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(panelStatusBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap()));
        pack();
    }

    private void rbRTRActionPerformed(java.awt.event.ActionEvent evt) {
        panelChartContainer.removeAll();
        setLabelStatusMessage(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Listing_.rtr_files"));
        comboCaseList.setModel(new javax.swing.DefaultComboBoxModel(getDirList()));
        if (isHavingCases) {
            prepareData();
        } else {
            cbRUN.setEnabled(false);
            cbRAV.setEnabled(false);
            cbNVTE.setEnabled(false);
            cbEQUI.setEnabled(false);
            cbPROD.setEnabled(false);
            tfXMax.setEnabled(false);
            tfXMin.setEnabled(false);
            tfYMax.setEnabled(false);
            tfYMin.setEnabled(false);
            tfError.setEnabled(false);
            buttonLOG.setEnabled(false);
            buttonPAR.setEnabled(false);
            buttonRES.setEnabled(false);
            buttonDrawChart.setEnabled(false);
            buttonViewData.setEnabled(false);
            buttonPNGsave.setEnabled(false);
            buttonXAxisChange.setEnabled(false);
            buttonYAxisChange.setEnabled(false);
            buttonClear.setEnabled(false);
            comboXAxis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));
            listYAxises.setModel(new javax.swing.AbstractListModel() {

                String[] strings = new String[] { "" };

                public int getSize() {
                    return strings.length;
                }

                public Object getElementAt(int i) {
                    return strings[i];
                }
            });
            setLabelStatusMessage(java.util.ResourceBundle.getBundle("ms2package/lang").getString("No_file_found._Browse_again..."));
            panelChartContainer.removeAll();
            panelChartContainer.setVisible(false);
            panelChartContainer.setVisible(true);
        }
    }

    private void rbRUN_RAVActionPerformed(java.awt.event.ActionEvent evt) {
        panelChartContainer.removeAll();
        setLabelStatusMessage(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Listing_.run_and_.rav_files"));
        comboCaseList.setModel(new javax.swing.DefaultComboBoxModel(getDirList()));
        if (isHavingCases) {
            prepareData();
        } else {
            cbRUN.setEnabled(false);
            cbRAV.setEnabled(false);
            cbNVTE.setEnabled(false);
            cbEQUI.setEnabled(false);
            cbPROD.setEnabled(false);
            tfXMax.setEnabled(false);
            tfXMin.setEnabled(false);
            tfYMax.setEnabled(false);
            tfYMin.setEnabled(false);
            tfError.setEnabled(false);
            buttonLOG.setEnabled(false);
            buttonPAR.setEnabled(false);
            buttonRES.setEnabled(false);
            buttonDrawChart.setEnabled(false);
            buttonViewData.setEnabled(false);
            buttonPNGsave.setEnabled(false);
            buttonXAxisChange.setEnabled(false);
            buttonYAxisChange.setEnabled(false);
            buttonClear.setEnabled(false);
            comboXAxis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));
            listYAxises.setModel(new javax.swing.AbstractListModel() {

                String[] strings = new String[] { "" };

                public int getSize() {
                    return strings.length;
                }

                public Object getElementAt(int i) {
                    return strings[i];
                }
            });
            setLabelStatusMessage(java.util.ResourceBundle.getBundle("ms2package/lang").getString("No_file_found._Browse_again..."));
            panelChartContainer.removeAll();
            panelChartContainer.setVisible(false);
            panelChartContainer.setVisible(true);
        }
    }

    private void tfYMinActionPerformed(java.awt.event.ActionEvent evt) {
        buttonYAxisChangeActionPerformed(evt);
    }

    private void tfYMaxActionPerformed(java.awt.event.ActionEvent evt) {
        buttonYAxisChangeActionPerformed(evt);
    }

    private void tfXMinActionPerformed(java.awt.event.ActionEvent evt) {
        buttonXAxisChangeActionPerformed(evt);
    }

    private void tfXMaxActionPerformed(java.awt.event.ActionEvent evt) {
        buttonXAxisChangeActionPerformed(evt);
    }

    private void cbPRODItemStateChanged(java.awt.event.ItemEvent evt) {
        if ((columnInRunFileForXAxis != -1 || columnInRavFileForXAxis != -1) && (columnsInRunFileForYAxis.length != 0 || columnsInRavFileForYAxis.length != 0) && (cbRUN.isEnabled() && cbRUN.isSelected() || cbRAV.isEnabled() && cbRAV.isSelected()) && (cbNVTE.isEnabled() && cbNVTE.isSelected() || cbEQUI.isEnabled() && cbEQUI.isSelected() || cbPROD.isEnabled() && cbPROD.isSelected())) {
            buttonDrawChart.setEnabled(true);
            if (cbAutoDraw.isSelected()) {
                plot();
            }
        } else {
            buttonDrawChart.setEnabled(false);
        }
    }

    private void cbEQUIItemStateChanged(java.awt.event.ItemEvent evt) {
        if ((columnInRunFileForXAxis != -1 || columnInRavFileForXAxis != -1) && (columnsInRunFileForYAxis.length != 0 || columnsInRavFileForYAxis.length != 0) && (cbRUN.isEnabled() && cbRUN.isSelected() || cbRAV.isEnabled() && cbRAV.isSelected()) && (cbNVTE.isEnabled() && cbNVTE.isSelected() || cbEQUI.isEnabled() && cbEQUI.isSelected() || cbPROD.isEnabled() && cbPROD.isSelected())) {
            buttonDrawChart.setEnabled(true);
            if (cbAutoDraw.isSelected()) {
                plot();
            }
        } else {
            buttonDrawChart.setEnabled(false);
        }
    }

    private void cbNVTEItemStateChanged(java.awt.event.ItemEvent evt) {
        if ((columnInRunFileForXAxis != -1 || columnInRavFileForXAxis != -1) && (columnsInRunFileForYAxis.length != 0 || columnsInRavFileForYAxis.length != 0) && (cbRUN.isEnabled() && cbRUN.isSelected() || cbRAV.isEnabled() && cbRAV.isSelected()) && (cbNVTE.isEnabled() && cbNVTE.isSelected() || cbEQUI.isEnabled() && cbEQUI.isSelected() || cbPROD.isEnabled() && cbPROD.isSelected())) {
            buttonDrawChart.setEnabled(true);
            if (cbAutoDraw.isSelected()) {
                plot();
            }
        } else {
            buttonDrawChart.setEnabled(false);
        }
    }

    private void comboCaseListActionPerformed(java.awt.event.ActionEvent evt) {
        if (isHavingCases) {
            prepareData();
        }
    }

    private void comboXAxisActionPerformed(java.awt.event.ActionEvent evt) {
        String selectedValue = (String) comboXAxis.getSelectedItem();
        columnInRtrFileForXAxis = -1;
        columnInRunFileForXAxis = -1;
        columnInRavFileForXAxis = -1;
        String title = selectedValue;
        if (rbRTR.isSelected()) {
            for (int j = 0; j < arrayOfRtrFile[0].length; ++j) {
                if (title.equals(arrayOfRtrFile[0][j])) {
                    columnInRtrFileForXAxis = j;
                }
            }
        }
        if (cbRUN.isEnabled()) {
            for (int j = 0; j < arrayOfRunFile[0].length; ++j) {
                if (title.equals(arrayOfRunFile[0][j])) {
                    columnInRunFileForXAxis = j;
                }
            }
        }
        if (cbRAV.isEnabled()) {
            for (int j = 0; j < arrayOfRavFile[0].length; ++j) {
                if (title.equals(arrayOfRavFile[0][j])) {
                    columnInRavFileForXAxis = j;
                }
            }
        }
        if (columnInRunFileForXAxis == -1) {
            cbRUN.setEnabled(false);
        }
        if (columnInRavFileForXAxis == -1) {
            cbRAV.setEnabled(false);
        }
        if (rbRTR.isSelected()) {
            if (columnInRtrFileForXAxis != -1 && columnsInRtrFileForYAxis.length != 0) {
                buttonDrawChart.setEnabled(true);
                if (cbAutoDraw.isSelected()) {
                    plot();
                }
            } else {
                buttonDrawChart.setEnabled(false);
            }
        } else {
            if ((columnInRunFileForXAxis != -1 || columnInRavFileForXAxis != -1) && (columnsInRunFileForYAxis.length != 0 || columnsInRavFileForYAxis.length != 0) && ((cbRUN.isEnabled() && cbRUN.isSelected()) || (cbRAV.isEnabled() && cbRAV.isSelected())) && ((cbNVTE.isEnabled() && cbNVTE.isSelected()) || (cbEQUI.isEnabled() && cbEQUI.isSelected()) || (cbPROD.isEnabled() && cbPROD.isSelected()))) {
                buttonDrawChart.setEnabled(true);
                if (cbAutoDraw.isSelected()) {
                    plot();
                }
            } else {
                buttonDrawChart.setEnabled(false);
            }
        }
    }

    private void listYAxisesValueChanged(javax.swing.event.ListSelectionEvent evt) {
        if (evt.getValueIsAdjusting() == false) {
            Object[] selectedValues = listYAxises.getSelectedValues();
            columnsInRtrFileForYAxis = new int[0];
            columnsInRunFileForYAxis = new int[0];
            columnsInRavFileForYAxis = new int[0];
            for (int i = 0; i < selectedValues.length; ++i) {
                String titleSelected = (String) selectedValues[i];
                if (rbRTR.isSelected()) {
                    for (int j = 0; j < arrayOfRtrFile[0].length; ++j) {
                        if (titleSelected.equals(arrayOfRtrFile[0][j])) {
                            int[] tempArray = new int[columnsInRtrFileForYAxis.length + 1];
                            System.arraycopy(columnsInRtrFileForYAxis, 0, tempArray, 0, columnsInRtrFileForYAxis.length);
                            tempArray[columnsInRtrFileForYAxis.length] = j;
                            columnsInRtrFileForYAxis = tempArray;
                        }
                    }
                }
                if (cbRUN.isEnabled()) {
                    for (int j = 0; j < arrayOfRunFile[0].length; ++j) {
                        if (titleSelected.equals(arrayOfRunFile[0][j])) {
                            int[] tempArray = new int[columnsInRunFileForYAxis.length + 1];
                            System.arraycopy(columnsInRunFileForYAxis, 0, tempArray, 0, columnsInRunFileForYAxis.length);
                            tempArray[columnsInRunFileForYAxis.length] = j;
                            columnsInRunFileForYAxis = tempArray;
                        }
                    }
                }
                if (cbRAV.isEnabled()) {
                    for (int j = 0; j < arrayOfRavFile[0].length; ++j) {
                        if (titleSelected.equals(arrayOfRavFile[0][j])) {
                            int[] tempArray = new int[columnsInRavFileForYAxis.length + 1];
                            System.arraycopy(columnsInRavFileForYAxis, 0, tempArray, 0, columnsInRavFileForYAxis.length);
                            tempArray[columnsInRavFileForYAxis.length] = j;
                            columnsInRavFileForYAxis = tempArray;
                        }
                    }
                }
            }
        }
        if (columnInRunFileForXAxis == -1) {
            cbRUN.setEnabled(false);
        }
        if (columnInRavFileForXAxis == -1) {
            cbRAV.setEnabled(false);
        }
        if (rbRTR.isSelected()) {
            if (columnInRtrFileForXAxis != -1 && columnsInRtrFileForYAxis.length != 0) {
                buttonDrawChart.setEnabled(true);
                if (cbAutoDraw.isSelected()) {
                    plot();
                }
            } else {
                buttonDrawChart.setEnabled(false);
            }
        } else {
            if ((columnInRunFileForXAxis != -1 || columnInRavFileForXAxis != -1) && (columnsInRunFileForYAxis.length != 0 || columnsInRavFileForYAxis.length != 0) && ((cbRUN.isEnabled() && cbRUN.isSelected()) || (cbRAV.isEnabled() && cbRAV.isSelected())) && ((cbNVTE.isEnabled() && cbNVTE.isSelected()) || (cbEQUI.isEnabled() && cbEQUI.isSelected()) || (cbPROD.isEnabled() && cbPROD.isSelected()))) {
                buttonDrawChart.setEnabled(true);
                if (cbAutoDraw.isSelected()) {
                    plot();
                }
            } else {
                buttonDrawChart.setEnabled(false);
            }
        }
    }

    private void buttonViewDataActionPerformed(java.awt.event.ActionEvent evt) {
        XYDataset xydataset = jPanelOfChart.getChart().getXYPlot().getDataset();
        int[] arrayOfItemCounts = new int[xydataset.getSeriesCount()];
        for (int i = 0; i < xydataset.getSeriesCount(); ++i) {
            arrayOfItemCounts[i] = xydataset.getItemCount(i);
        }
        String[][] data = new String[arrayOfItemCounts[findIndexOfLargestInt(arrayOfItemCounts)]][xydataset.getSeriesCount() + 1];
        for (int series = 0; series < data[data.length - 1].length; ++series) {
            for (int item = 0; item < data.length; ++item) {
                if (series == 0) {
                    data[item][series] = xydataset.getXValue(findIndexOfLargestInt(arrayOfItemCounts), item) + "";
                } else {
                    if (xydataset.getItemCount(series - 1) > item) {
                        data[item][series] = xydataset.getYValue(series - 1, item) + "";
                    } else {
                        data[item][series] = null;
                    }
                }
            }
        }
        String[] yAxisTitle = jPanelOfChart.getChart().getXYPlot().getRangeAxis().getLabel().toString().split(", ");
        String[] title = new String[yAxisTitle.length + 1];
        System.arraycopy(yAxisTitle, 0, title, 1, yAxisTitle.length);
        title[0] = "NR";
        ShowTableFrame frame;
        frame = new ShowTableFrame(data, title);
        frame.setVisible(true);
    }

    private void cbAutoDrawItemStateChanged(java.awt.event.ItemEvent evt) {
        setLabelStatusMessage(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Toggles_between_use_of_Draw_Button"));
    }

    private void cbAskDirectoryItemStateChanged(java.awt.event.ItemEvent evt) {
        setLabelStatusMessage(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Press_Save_Image_to_save_in_PNG_format"));
    }

    private void buttonQuitActionPerformed(java.awt.event.ActionEvent evt) {
        setVisible(false);
        dispose();
        System.exit(0);
    }

    private void buttonClearActionPerformed(java.awt.event.ActionEvent evt) {
        buttonViewData.setEnabled(false);
        buttonPNGsave.setEnabled(false);
        buttonXAxisChange.setEnabled(false);
        buttonYAxisChange.setEnabled(false);
        buttonClear.setEnabled(false);
        panelChartContainer.removeAll();
        panelChartContainer.setVisible(false);
        panelChartContainer.setVisible(true);
    }

    private void buttonRestartActionPerformed(java.awt.event.ActionEvent evt) {
        setVisible(false);
        dispose();
        columnInRunFileForXAxis = -1;
        columnInRavFileForXAxis = -1;
        columnsInRunFileForYAxis = new int[0];
        columnsInRavFileForYAxis = new int[0];
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new Ms2chart().setVisible(true);
            }
        });
    }

    private void buttonLOGActionPerformed(java.awt.event.ActionEvent evt) {
        ShowFileFrame frame;
        if (isHavingCases) {
            frame = new ShowFileFrame(selectedDirectory.getAbsolutePath() + java.io.File.separator + caseTitle + ".log");
        } else {
            frame = new ShowFileFrame();
        }
        frame.setVisible(true);
    }

    private void buttonPARActionPerformed(java.awt.event.ActionEvent evt) {
        ShowFileFrame frame;
        if (isHavingCases) {
            frame = new ShowFileFrame(selectedDirectory.getAbsolutePath() + java.io.File.separator + caseTitle + ".par");
        } else {
            frame = new ShowFileFrame();
        }
        frame.setVisible(true);
    }

    private void buttonRESActionPerformed(java.awt.event.ActionEvent evt) {
        ShowFileFrame frame;
        if (isHavingCases) {
            if (rbRTR.isSelected()) {
                frame = new ShowFileFrame(selectedDirectory.getAbsolutePath() + java.io.File.separator + caseTitle + ".res");
            } else if (rbRUN_RAV.isSelected()) {
                frame = new ShowFileFrame(selectedDirectory.getAbsolutePath() + java.io.File.separator + caseTitle + "_1.res");
            } else {
                frame = new ShowFileFrame();
            }
        } else {
            frame = new ShowFileFrame();
        }
        frame.setVisible(true);
    }

    private void buttonPNGsaveActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            JFreeChart chart = jPanelOfChart.getChart();
            Object[] yTitles = listYAxises.getSelectedValues();
            String yName = "";
            for (int i = 0; i < yTitles.length; ++i) {
                yName += ((String) yTitles[i]).replaceAll("\\s", " ") + "_";
            }
            String xName = ((String) comboXAxis.getSelectedItem()).replaceAll("\\s", " ");
            File file = new File(selectedDirectory.getAbsolutePath() + java.io.File.separator + caseTitle + "_" + yName + "_" + xName + ".png");
            if (cbAskDirectory.isSelected()) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    file = new File(fileChooser.getSelectedFile().getAbsolutePath() + java.io.File.separator + caseTitle + "_" + yName + "_" + xName + ".png");
                }
            }
            try {
                if (file.createNewFile()) {
                    ImageIO.write(chart.createBufferedImage(panelChartContainer.getWidth(), panelChartContainer.getHeight()), "png", file);
                } else {
                    setLabelStatusMessage(java.util.ResourceBundle.getBundle("ms2package/lang").getString("File_already_exists"));
                }
            } catch (Exception e) {
                setLabelStatusMessage(java.util.ResourceBundle.getBundle("ms2package/lang").getString("ERROR!"));
            }
        } catch (Exception e) {
            setLabelStatusMessage(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Error_reading_chart!"));
        }
    }

    private void buttonDrawChartActionPerformed(java.awt.event.ActionEvent evt) {
        plot();
    }

    private void buttonYAxisChangeActionPerformed(java.awt.event.ActionEvent evt) {
        rangeAxis.setUpperBound(Double.parseDouble(tfYMax.getText()));
        rangeAxis.setLowerBound(Double.parseDouble(tfYMin.getText()));
        tfYMax.setText(Math.round(rangeAxis.getRange().getUpperBound() * 100) / 100.0 + "");
        tfYMin.setText(Math.round(rangeAxis.getRange().getLowerBound() * 100) / 100.0 + "");
    }

    private void buttonXAxisChangeActionPerformed(java.awt.event.ActionEvent evt) {
        domainAxis.setUpperBound(Double.parseDouble(tfXMax.getText()));
        domainAxis.setLowerBound(Double.parseDouble(tfXMin.getText()));
        tfXMax.setText(Math.round(domainAxis.getRange().getUpperBound() * 100) / 100.0 + "");
        tfXMin.setText(Math.round(domainAxis.getRange().getLowerBound() * 100) / 100.0 + "");
    }

    private void buttonBrowseActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(selectedDirectory);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedDirectory = fileChooser.getSelectedFile();
            setLabelStatusMessage(java.util.ResourceBundle.getBundle("ms2package/lang").getString("In_") + selectedDirectory.getAbsolutePath());
            comboCaseList.setModel(new javax.swing.DefaultComboBoxModel(getDirList()));
            if (isHavingCases) {
                prepareData();
            } else {
                cbRUN.setEnabled(false);
                cbRAV.setEnabled(false);
                cbNVTE.setEnabled(false);
                cbEQUI.setEnabled(false);
                cbPROD.setEnabled(false);
                tfXMax.setEnabled(false);
                tfXMin.setEnabled(false);
                tfYMax.setEnabled(false);
                tfYMin.setEnabled(false);
                tfError.setEnabled(false);
                buttonLOG.setEnabled(false);
                buttonPAR.setEnabled(false);
                buttonRES.setEnabled(false);
                buttonDrawChart.setEnabled(false);
                buttonViewData.setEnabled(false);
                buttonPNGsave.setEnabled(false);
                buttonXAxisChange.setEnabled(false);
                buttonYAxisChange.setEnabled(false);
                buttonClear.setEnabled(false);
                comboXAxis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));
                listYAxises.setModel(new javax.swing.AbstractListModel() {

                    String[] strings = new String[] { "" };

                    public int getSize() {
                        return strings.length;
                    }

                    public Object getElementAt(int i) {
                        return strings[i];
                    }
                });
                setLabelStatusMessage(java.util.ResourceBundle.getBundle("ms2package/lang").getString("No_.rav_or_.run_file_found._Browse_again..."));
                panelChartContainer.removeAll();
                panelChartContainer.setVisible(false);
                panelChartContainer.setVisible(true);
            }
        }
    }

    private void cbRAVItemStateChanged(java.awt.event.ItemEvent evt) {
        if ((columnInRunFileForXAxis != -1 || columnInRavFileForXAxis != -1) && (columnsInRunFileForYAxis.length != 0 || columnsInRavFileForYAxis.length != 0) && ((cbRUN.isEnabled() && cbRUN.isSelected()) || (cbRAV.isEnabled() && cbRAV.isSelected())) && ((cbNVTE.isEnabled() && cbNVTE.isSelected()) || (cbEQUI.isEnabled() && cbEQUI.isSelected()) || (cbPROD.isEnabled() && cbPROD.isSelected()))) {
            buttonDrawChart.setEnabled(true);
            if (cbAutoDraw.isSelected()) {
                plot();
            }
        } else {
            buttonDrawChart.setEnabled(false);
        }
    }

    private void cbRUNItemStateChanged(java.awt.event.ItemEvent evt) {
        if ((columnInRunFileForXAxis != -1 || columnInRavFileForXAxis != -1) && (columnsInRunFileForYAxis.length != 0 || columnsInRavFileForYAxis.length != 0) && ((cbRUN.isEnabled() && cbRUN.isSelected()) || (cbRAV.isEnabled() && cbRAV.isSelected())) && ((cbNVTE.isEnabled() && cbNVTE.isSelected()) || (cbEQUI.isEnabled() && cbEQUI.isSelected()) || (cbPROD.isEnabled() && cbPROD.isSelected()))) {
            buttonDrawChart.setEnabled(true);
            if (cbAutoDraw.isSelected()) {
                plot();
            }
        } else {
            buttonDrawChart.setEnabled(false);
        }
    }

    /**
     * main function for Ms2chart. Starts the program.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        startingDirectory = new File(new File("").getAbsolutePath());
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new Ms2chart().setVisible(true);
            }
        });
    }

    private String[] getDirList() {
        if (rbRTR.isSelected()) {
            String[] dirListForRtrFile;
            FilenameFilter rtrFilter = new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.endsWith(".rtr");
                }
            };
            dirListForRtrFile = selectedDirectory.list(rtrFilter);
            if (dirListForRtrFile.length > 0) {
                for (int i = 0; i < dirListForRtrFile.length; ++i) {
                    dirListForRtrFile[i] = dirListForRtrFile[i].substring(0, dirListForRtrFile[i].length() - 4);
                }
            }
            Arrays.sort(dirListForRtrFile);
            if (dirListForRtrFile.length > 0) {
                isHavingCases = true;
                return dirListForRtrFile;
            } else {
                isHavingCases = false;
                return new String[] { "" };
            }
        } else {
            String[] dirListForRunFile, dirListForRavFile, tempDirList;
            FilenameFilter runFilter = new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.endsWith(".run");
                }
            };
            FilenameFilter ravFilter = new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.endsWith(".rav");
                }
            };
            dirListForRunFile = selectedDirectory.list(ravFilter);
            dirListForRavFile = selectedDirectory.list(runFilter);
            if (dirListForRunFile.length > 0) {
                for (int i = 0; i < dirListForRunFile.length; ++i) {
                    dirListForRunFile[i] = dirListForRunFile[i].substring(0, dirListForRunFile[i].length() - 6);
                }
            }
            if (dirListForRavFile.length > 0) {
                for (int i = 0; i < dirListForRavFile.length; ++i) {
                    dirListForRavFile[i] = dirListForRavFile[i].substring(0, dirListForRavFile[i].length() - 6);
                }
            }
            tempDirList = new String[dirListForRunFile.length + dirListForRavFile.length];
            System.arraycopy(dirListForRunFile, 0, tempDirList, 0, dirListForRunFile.length);
            System.arraycopy(dirListForRavFile, 0, tempDirList, dirListForRunFile.length, dirListForRavFile.length);
            Arrays.sort(tempDirList);
            if (tempDirList.length > 0) {
                isHavingCases = true;
                String[] dirList = new String[1];
                dirList[0] = tempDirList[0];
                for (int i = 1; i < tempDirList.length; ++i) {
                    if (!tempDirList[i].equals(tempDirList[i - 1])) {
                        String[] tempArray = new String[dirList.length + 1];
                        System.arraycopy(dirList, 0, tempArray, 0, dirList.length);
                        tempArray[dirList.length] = tempDirList[i];
                        dirList = tempArray;
                    }
                }
                return dirList;
            } else {
                isHavingCases = false;
                return new String[] { "" };
            }
        }
    }

    private void prepareData() {
        FileInputStream fstream;
        caseTitle = (String) comboCaseList.getSelectedItem();
        if (rbRTR.isSelected()) {
            try {
                fstream = new FileInputStream(selectedDirectory.getAbsolutePath() + java.io.File.separator + caseTitle + ".rtr");
                fstream.close();
                arrayOfRtrFile = readFileIn2DStringArray(caseTitle + ".rtr");
                arrayOfRtrFileStartingIndices = getArrayOfStartingIndices(arrayOfRtrFile);
                arrayOfRtrFileDatasetLengths = getArrayOfDatasetLengths(arrayOfRtrFile, arrayOfRtrFileStartingIndices);
                cbRUN.setEnabled(false);
                cbRAV.setEnabled(false);
                cbNVTE.setEnabled(false);
                cbEQUI.setEnabled(false);
                cbPROD.setEnabled(false);
            } catch (Exception e) {
                setLabelStatusMessage(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Error_selecting_RTR_files"));
            }
        } else {
            cbPROD.setEnabled(true);
            try {
                fstream = new FileInputStream(selectedDirectory.getAbsolutePath() + java.io.File.separator + caseTitle + "_1.run");
                fstream.close();
                arrayOfRunFile = readFileIn2DStringArray(caseTitle + "_1.run");
                arrayOfRunFileStartingIndices = getArrayOfStartingIndices(arrayOfRunFile);
                arrayOfRunFileDatasetLengths = getArrayOfDatasetLengths(arrayOfRunFile, arrayOfRunFileStartingIndices);
                if (arrayOfRunFileDatasetLengths.length > 2) {
                    cbEQUI.setEnabled(true);
                }
                if (arrayOfRunFileDatasetLengths.length > 1) {
                    cbNVTE.setEnabled(true);
                }
                cbRUN.setEnabled(true);
            } catch (Exception e) {
                cbRUN.setEnabled(false);
            }
            try {
                fstream = new FileInputStream(selectedDirectory.getAbsolutePath() + java.io.File.separator + caseTitle + "_1.rav");
                fstream.close();
                arrayOfRavFile = readFileIn2DStringArray(caseTitle + "_1.rav");
                arrayOfRavFileStartingIndices = getArrayOfStartingIndices(arrayOfRavFile);
                arrayOfRavFileDatasetLengths = getArrayOfDatasetLengths(arrayOfRavFile, arrayOfRavFileStartingIndices);
                if (arrayOfRavFileDatasetLengths.length > 2) {
                    cbEQUI.setEnabled(true);
                }
                if (arrayOfRavFileDatasetLengths.length > 1) {
                    cbNVTE.setEnabled(true);
                }
                cbRAV.setEnabled(true);
            } catch (Exception e) {
                cbRAV.setEnabled(false);
            }
        }
        try {
            fstream = new FileInputStream(selectedDirectory.getAbsolutePath() + java.io.File.separator + caseTitle + ".log");
            fstream.close();
            buttonLOG.setEnabled(true);
        } catch (Exception e) {
            buttonLOG.setEnabled(false);
        }
        try {
            fstream = new FileInputStream(selectedDirectory.getAbsolutePath() + java.io.File.separator + caseTitle + ".par");
            fstream.close();
            buttonPAR.setEnabled(true);
        } catch (Exception e) {
            buttonPAR.setEnabled(false);
        }
        try {
            if (rbRTR.isSelected()) {
                fstream = new FileInputStream(selectedDirectory.getAbsolutePath() + java.io.File.separator + caseTitle + ".res");
                fstream.close();
                buttonRES.setEnabled(true);
            } else if (rbRUN_RAV.isSelected()) {
                fstream = new FileInputStream(selectedDirectory.getAbsolutePath() + java.io.File.separator + caseTitle + "_1.res");
                fstream.close();
                buttonRES.setEnabled(true);
            } else {
                buttonRES.setEnabled(false);
            }
        } catch (Exception e) {
            buttonRES.setEnabled(false);
        }
        comboXAxis.setModel(new javax.swing.DefaultComboBoxModel(getListOfXAxises()));
        comboXAxis.setSelectedIndex(0);
        int[] temp = listYAxises.getSelectedIndices();
        listYAxises.setModel(new javax.swing.AbstractListModel() {

            String[] strings = getListOfYAxises();

            public int getSize() {
                return strings.length;
            }

            public Object getElementAt(int i) {
                return strings[i];
            }
        });
        listYAxises.setSelectedIndices(temp);
    }

    private String[][] readFileIn2DStringArray(String fileName) {
        int numberOfColumns = 0, numberOfRows = 0;
        BufferedReader inputStream;
        String line;
        StringBuffer fileData = new StringBuffer();
        File inputFile = new File(selectedDirectory.getAbsolutePath() + java.io.File.separator + fileName);
        try {
            inputStream = new BufferedReader(new FileReader(inputFile));
            if (rbRTR.isSelected()) {
                while ((line = inputStream.readLine()) != null) {
                    if (line.length() >= 6) {
                        ++numberOfRows;
                        int index = 0;
                        String tempString, oldTempString = "";
                        if (numberOfRows == 1) {
                            numberOfColumns = line.length() / 10 + 1;
                        }
                        for (int i = 0; i < numberOfColumns; ++i) {
                            int index2;
                            if (index + 10 > line.length()) {
                                index2 = line.length();
                            } else {
                                index2 = index + 10;
                            }
                            tempString = line.substring(index, index2);
                            if (tempString.endsWith("TIME[ps")) {
                                tempString = line.substring(index, index + 11);
                                index += 11;
                            } else if (oldTempString.endsWith("TIME[ps]")) {
                                index += 9;
                            } else {
                                index += 10;
                            }
                            fileData.append(tempString + " ");
                            oldTempString = tempString;
                        }
                    }
                }
            } else {
                while ((line = inputStream.readLine()) != null) {
                    if (line.length() >= 6) {
                        ++numberOfRows;
                        int iterationsLeft, index = 0;
                        if (numberOfRows == 1) {
                            numberOfColumns = line.length() / 10 + 1;
                        }
                        fileData.append(line.substring(index, index + 7) + " ");
                        index += 7;
                        if (line.length() % 5 == 0) {
                            fileData.append(line.substring(index, index + 8) + " ");
                            index += 8;
                            iterationsLeft = numberOfColumns - 2;
                        } else {
                            iterationsLeft = numberOfColumns - 1;
                        }
                        for (int i = 0; i < iterationsLeft; ++i) {
                            fileData.append(line.substring(index, index + 10) + " ");
                            index += 10;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setLabelStatusMessage("R: " + numberOfRows + " C: " + numberOfColumns + " " + fileName);
        StringTokenizer st = new StringTokenizer(fileData.toString());
        String[][] data = new String[numberOfRows][numberOfColumns];
        for (int i = 0; i < numberOfRows; ++i) {
            for (int j = 0; j < numberOfColumns; ++j) {
                if (st.hasMoreTokens()) {
                    data[i][j] = st.nextToken();
                } else {
                    break;
                }
                if (data[i][j].equals("MUE") || data[i][j].equals("FRACT") || data[i][j].equals("VW") || data[i][j].equals("Int") || data[i][j].equals("D") || data[i][j].endsWith("_i")) {
                    data[i][j] = data[i][j] + " " + st.nextToken();
                    if (data[i][j].equals("Int D")) {
                        data[i][j] = data[i][j] + " " + st.nextToken();
                    }
                }
            }
        }
        return data;
    }

    private int[] getArrayOfStartingIndices(String[][] arrayOfFile) {
        int[] arrayOfFileStartingIndices = new int[0];
        for (int i = 0; i < arrayOfFile.length; ++i) {
            if (arrayOfFile[i][0].equals(arrayOfFile[0][0])) {
                int[] tempArray = new int[arrayOfFileStartingIndices.length + 1];
                System.arraycopy(arrayOfFileStartingIndices, 0, tempArray, 0, arrayOfFileStartingIndices.length);
                tempArray[arrayOfFileStartingIndices.length] = i;
                arrayOfFileStartingIndices = tempArray;
            }
        }
        return arrayOfFileStartingIndices;
    }

    private int[] getArrayOfDatasetLengths(String[][] arrayOfFile, int[] arrayOfFileStartingIndices) {
        int[] arrayOfFileDatasetLengths = new int[arrayOfFileStartingIndices.length];
        for (int i = 0; i < arrayOfFileDatasetLengths.length - 1; ++i) {
            arrayOfFileDatasetLengths[i] = arrayOfFileStartingIndices[i + 1] - arrayOfFileStartingIndices[i] - 1;
        }
        arrayOfFileDatasetLengths[arrayOfFileDatasetLengths.length - 1] = arrayOfFile.length - arrayOfFileStartingIndices[arrayOfFileStartingIndices.length - 1] - 1;
        return arrayOfFileDatasetLengths;
    }

    private String[] getListOfXAxises() {
        String[] arrayOfRunFileXAxises = null;
        String[] arrayOfRavFileXAxises = null;
        String[] arrayOfRtrFileXAxises = null;
        String[] listOfXAxises, tempXlist;
        if (cbRUN.isEnabled()) {
            arrayOfRunFileXAxises = new String[arrayOfRunFile[0].length];
            for (int i = 0; i < arrayOfRunFileXAxises.length; ++i) {
                arrayOfRunFileXAxises[i] = arrayOfRunFile[0][i];
            }
        }
        if (cbRAV.isEnabled()) {
            arrayOfRavFileXAxises = new String[arrayOfRavFile[0].length];
            for (int i = 0; i < arrayOfRavFileXAxises.length; ++i) {
                arrayOfRavFileXAxises[i] = arrayOfRavFile[0][i];
            }
        }
        if (cbRUN.isEnabled() && cbRAV.isEnabled()) {
            boolean isPresent;
            for (int i = 0; i < arrayOfRavFileXAxises.length; ++i) {
                isPresent = false;
                for (int j = 0; j < arrayOfRunFileXAxises.length; ++j) {
                    if (arrayOfRavFileXAxises[i].equals(arrayOfRunFileXAxises[j])) {
                        isPresent = true;
                        break;
                    }
                }
                if (isPresent == false) {
                    String[] tempArray = new String[arrayOfRunFileXAxises.length + 1];
                    System.arraycopy(arrayOfRunFileXAxises, 0, tempArray, 0, arrayOfRunFileXAxises.length);
                    tempArray[arrayOfRunFileXAxises.length] = arrayOfRavFileXAxises[i];
                    arrayOfRunFileXAxises = tempArray;
                }
            }
            listOfXAxises = arrayOfRunFileXAxises;
        } else if (cbRUN.isEnabled()) {
            listOfXAxises = arrayOfRunFileXAxises;
        } else if (cbRAV.isEnabled()) {
            listOfXAxises = arrayOfRavFileXAxises;
        } else if (rbRTR.isSelected()) {
            arrayOfRtrFileXAxises = new String[arrayOfRtrFile[0].length];
            for (int i = 0; i < arrayOfRtrFileXAxises.length; ++i) {
                arrayOfRtrFileXAxises[i] = arrayOfRtrFile[0][i];
            }
            listOfXAxises = arrayOfRtrFileXAxises;
        } else {
            listOfXAxises = new String[] { "" };
        }
        return listOfXAxises;
    }

    private String[] getListOfYAxises() {
        String[] arrayOfRunFileYAxises = null;
        String[] arrayOfRavFileYAxises = null;
        String[] arrayOfRtrFileYAxises = null;
        String[] listOfYAxises, tempYList;
        if (cbRUN.isEnabled()) {
            arrayOfRunFileYAxises = new String[arrayOfRunFile[0].length];
            for (int i = 0; i < arrayOfRunFileYAxises.length; ++i) {
                arrayOfRunFileYAxises[i] = arrayOfRunFile[0][i];
            }
        }
        if (cbRAV.isEnabled()) {
            arrayOfRavFileYAxises = new String[arrayOfRavFile[0].length];
            for (int i = 0; i < arrayOfRavFileYAxises.length; ++i) {
                arrayOfRavFileYAxises[i] = arrayOfRavFile[0][i];
            }
        }
        if (cbRUN.isEnabled() && cbRAV.isEnabled()) {
            boolean isPresent;
            for (int i = 0; i < arrayOfRavFileYAxises.length; ++i) {
                isPresent = false;
                for (int j = 0; j < arrayOfRunFileYAxises.length; ++j) {
                    if (arrayOfRavFileYAxises[i].equals(arrayOfRunFileYAxises[j])) {
                        isPresent = true;
                        break;
                    }
                }
                if (isPresent == false) {
                    String[] tempArray = new String[arrayOfRunFileYAxises.length + 1];
                    System.arraycopy(arrayOfRunFileYAxises, 0, tempArray, 0, arrayOfRunFileYAxises.length);
                    tempArray[arrayOfRunFileYAxises.length] = arrayOfRavFileYAxises[i];
                    arrayOfRunFileYAxises = tempArray;
                }
            }
            listOfYAxises = arrayOfRunFileYAxises;
        } else if (cbRUN.isEnabled()) {
            listOfYAxises = arrayOfRunFileYAxises;
        } else if (cbRAV.isEnabled()) {
            listOfYAxises = arrayOfRavFileYAxises;
        } else if (rbRTR.isSelected()) {
            arrayOfRtrFileYAxises = new String[arrayOfRtrFile[0].length];
            for (int i = 1; i < arrayOfRtrFileYAxises.length; ++i) {
                arrayOfRtrFileYAxises[i] = arrayOfRtrFile[0][i];
            }
            listOfYAxises = arrayOfRtrFileYAxises;
        } else {
            listOfYAxises = new String[] { "" };
        }
        return listOfYAxises;
    }

    private void plot() {
        Object[] selectedValues = listYAxises.getSelectedValues();
        String titleForYAxis = "", error = "", acf = "";
        String[] arrayOfErrorsPLUS = new String[selectedValues.length];
        String[] arrayOfErrorsMINUS = new String[selectedValues.length];
        boolean acfIsNeeded = false;
        if (selectedValues.length > 1) {
            for (int i = 0; i < selectedValues.length - 1; ++i) {
                String titleSelected = (String) selectedValues[i];
                titleForYAxis += titleSelected + ", ";
                if (buttonRES.isEnabled()) {
                    arrayOfErrorsPLUS[i] = "+" + getError(titleSelected);
                    arrayOfErrorsMINUS[i] = "-" + getError(titleSelected);
                    error += "+" + arrayOfErrorsMINUS[i] + ", ";
                }
            }
        }
        titleForYAxis += (String) selectedValues[selectedValues.length - 1];
        if (rbRTR.isSelected() && buttonRES.isEnabled()) {
            if (!titleForYAxis.startsWith("Int") && (titleForYAxis.indexOf("D_") != -1 || titleForYAxis.indexOf("VS") != -1 || titleForYAxis.indexOf("VB") != -1)) {
                acf = getAverageRTR();
                acfIsNeeded = true;
            }
        }
        titleForYAxis = titleForYAxis.replaceAll("NR", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Number_of_runs"));
        titleForYAxis = titleForYAxis.replaceAll("DRUCK", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Reduced_pressure"));
        titleForYAxis = titleForYAxis.replaceAll("DICHTE", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Reduced_density"));
        titleForYAxis = titleForYAxis.replaceAll("TEMP", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Reduced_temperature"));
        titleForYAxis = titleForYAxis.replaceAll("EPOT", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Reduced_potential_energy"));
        titleForYAxis = titleForYAxis.replaceAll("ENTLP", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Reduced_enthalpy"));
        titleForYAxis = titleForYAxis.replaceAll("MUE", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Chemical_potential"));
        titleForYAxis = titleForYAxis.replaceAll("FRACT", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Mole_fraction"));
        titleForYAxis = titleForYAxis.replaceAll("DISP", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Displacement"));
        titleForYAxis = titleForYAxis.replaceAll("VW", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Partial_Molar_Volume"));
        titleForYAxis = titleForYAxis.replaceAll("D_i", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Self-diff._coeff."));
        titleForYAxis = titleForYAxis.replaceAll("VS", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Shear-Viscosity"));
        titleForYAxis = titleForYAxis.replaceAll("VB", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Bulk-Viscosity"));
        titleForYAxis = titleForYAxis.replaceAll("CO", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Thermal_conductivity"));
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        if (buttonRES.isEnabled()) {
            arrayOfErrorsPLUS[selectedValues.length - 1] = "+" + getError((String) selectedValues[selectedValues.length - 1]);
            arrayOfErrorsMINUS[selectedValues.length - 1] = "-" + getError((String) selectedValues[selectedValues.length - 1]);
            error += "+" + arrayOfErrorsMINUS[selectedValues.length - 1];
            tfError.setEnabled(true);
            tfError.setText(error);
        }
        if (rbRTR.isSelected()) {
            oneByESeries = new XYSeries("1/e");
            averageSeries = new XYSeries("ACF");
            xyseriesForRtr = new XYSeries[columnsInRtrFileForYAxis.length][arrayOfRtrFileDatasetLengths.length];
            if (acfIsNeeded) {
                for (int j = 0; j < arrayOfRtrFileDatasetLengths[arrayOfRtrFileDatasetLengths.length - 1]; ++j) {
                    try {
                        oneByESeries.add(Double.parseDouble(arrayOfRtrFile[arrayOfRtrFileStartingIndices[arrayOfRtrFileStartingIndices.length - 1] + 1 + j][0]), 0.367879441);
                        averageSeries.add(Double.parseDouble(acf), -0.5);
                        averageSeries.add(Double.parseDouble(acf), 1);
                    } catch (Exception e) {
                        System.out.println(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Incorrect_data_point_at:_") + j + " 1/e");
                        oneByESeries.add(Double.parseDouble(arrayOfRtrFile[arrayOfRtrFileStartingIndices[arrayOfRtrFileStartingIndices.length - 1] + 1 + j][0]), null);
                        averageSeries.add(null, -0.5);
                        averageSeries.add(null, 1);
                    }
                }
            }
            if (acfIsNeeded) {
                xyseriescollection.addSeries(oneByESeries);
                xyseriescollection.addSeries(averageSeries);
            }
            for (int i = 0; i < columnsInRtrFileForYAxis.length; ++i) {
                xyseriesForRtr[i][xyseriesForRtr[i].length - 1] = new XYSeries(java.util.ResourceBundle.getBundle("ms2package/lang").getString("RTR_file"));
                for (int j = 0; j < arrayOfRtrFileDatasetLengths[arrayOfRtrFileDatasetLengths.length - 1]; ++j) {
                    try {
                        xyseriesForRtr[i][xyseriesForRtr[i].length - 1].add(Double.parseDouble(arrayOfRtrFile[arrayOfRtrFileStartingIndices[arrayOfRtrFileStartingIndices.length - 1] + 1 + j][columnInRtrFileForXAxis]), Double.parseDouble(arrayOfRtrFile[arrayOfRtrFileStartingIndices[arrayOfRtrFileStartingIndices.length - 1] + 1 + j][columnsInRtrFileForYAxis[i]]));
                    } catch (Exception e) {
                        System.out.println(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Incorrect_data_point_at:_") + j + java.util.ResourceBundle.getBundle("ms2package/lang").getString("_for_column_") + i);
                        xyseriesForRtr[i][xyseriesForRtr[i].length - 1].add(Double.parseDouble(arrayOfRtrFile[arrayOfRtrFileStartingIndices[arrayOfRtrFileStartingIndices.length - 1] + 1 + j][columnInRtrFileForXAxis]), null);
                    }
                }
                xyseriescollection.addSeries(xyseriesForRtr[i][xyseriesForRtr[i].length - 1]);
            }
        } else {
            if (cbRUN.isEnabled() && cbRUN.isSelected()) {
                xyseriesForRun = new XYSeries[columnsInRunFileForYAxis.length][arrayOfRunFileDatasetLengths.length];
                int offset;
                for (int i = 0; i < columnsInRunFileForYAxis.length; ++i) {
                    if (cbPROD.isEnabled() && cbPROD.isSelected()) {
                        offset = 1;
                        xyseriesForRun[i][xyseriesForRun[i].length - offset] = new XYSeries(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Production_(run)"));
                        for (int j = 0; j < arrayOfRunFileDatasetLengths[arrayOfRunFileDatasetLengths.length - offset]; ++j) {
                            try {
                                xyseriesForRun[i][xyseriesForRun[i].length - offset].add(Double.parseDouble(arrayOfRunFile[arrayOfRunFileStartingIndices[arrayOfRunFileStartingIndices.length - offset] + 1 + j][columnInRunFileForXAxis]), Double.parseDouble(arrayOfRunFile[arrayOfRunFileStartingIndices[arrayOfRunFileStartingIndices.length - offset] + 1 + j][columnsInRunFileForYAxis[i]]));
                            } catch (Exception e) {
                                System.out.println(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Incorrect_data_point_at:_") + j + java.util.ResourceBundle.getBundle("ms2package/lang").getString("_for_column_") + i);
                                xyseriesForRun[i][xyseriesForRun[i].length - offset].add(Double.parseDouble(arrayOfRunFile[arrayOfRunFileStartingIndices[arrayOfRunFileStartingIndices.length - offset] + 1 + j][columnInRunFileForXAxis]), null);
                            }
                        }
                        xyseriescollection.addSeries(xyseriesForRun[i][xyseriesForRun[i].length - offset]);
                    }
                    if (cbEQUI.isEnabled() && cbEQUI.isSelected()) {
                        offset = 2;
                        xyseriesForRun[i][xyseriesForRun[i].length - offset] = new XYSeries(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Equilibration_(run)"));
                        for (int j = 0; j < arrayOfRunFileDatasetLengths[arrayOfRunFileDatasetLengths.length - offset]; ++j) {
                            try {
                                xyseriesForRun[i][xyseriesForRun[i].length - offset].add(Double.parseDouble(arrayOfRunFile[arrayOfRunFileStartingIndices[arrayOfRunFileStartingIndices.length - offset] + 1 + j][columnInRunFileForXAxis]), Double.parseDouble(arrayOfRunFile[arrayOfRunFileStartingIndices[arrayOfRunFileStartingIndices.length - offset] + 1 + j][columnsInRunFileForYAxis[i]]));
                            } catch (Exception e) {
                                System.out.println(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Incorrect_data_point_at:_") + j + java.util.ResourceBundle.getBundle("ms2package/lang").getString("_for_column_") + i);
                                xyseriesForRun[i][xyseriesForRun[i].length - offset].add(Double.parseDouble(arrayOfRunFile[arrayOfRunFileStartingIndices[arrayOfRunFileStartingIndices.length - offset] + 1 + j][columnInRunFileForXAxis]), null);
                            }
                        }
                        xyseriescollection.addSeries(xyseriesForRun[i][xyseriesForRun[i].length - offset]);
                    }
                    if (cbNVTE.isEnabled() && cbNVTE.isSelected()) {
                        if (cbEQUI.isEnabled()) {
                            offset = 3;
                        } else {
                            offset = 2;
                        }
                        xyseriesForRun[i][xyseriesForRun[i].length - offset] = new XYSeries(java.util.ResourceBundle.getBundle("ms2package/lang").getString("NVT_Equilibration_(run)"));
                        for (int j = 0; j < arrayOfRunFileDatasetLengths[arrayOfRunFileDatasetLengths.length - offset]; ++j) {
                            try {
                                xyseriesForRun[i][xyseriesForRun[i].length - offset].add(Double.parseDouble(arrayOfRunFile[arrayOfRunFileStartingIndices[arrayOfRunFileStartingIndices.length - offset] + 1 + j][columnInRunFileForXAxis]), Double.parseDouble(arrayOfRunFile[arrayOfRunFileStartingIndices[arrayOfRunFileStartingIndices.length - offset] + 1 + j][columnsInRunFileForYAxis[i]]));
                            } catch (Exception e) {
                                System.out.println(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Incorrect_data_point_at:_") + j + java.util.ResourceBundle.getBundle("ms2package/lang").getString("_for_column_") + i);
                                xyseriesForRun[i][xyseriesForRun[i].length - offset].add(Double.parseDouble(arrayOfRunFile[arrayOfRunFileStartingIndices[arrayOfRunFileStartingIndices.length - offset] + 1 + j][columnInRunFileForXAxis]), null);
                            }
                        }
                        xyseriescollection.addSeries(xyseriesForRun[i][xyseriesForRun[i].length - offset]);
                    }
                }
            }
            if (cbRAV.isEnabled() && cbRAV.isSelected()) {
                xyseriesForRav = new XYSeries[columnsInRavFileForYAxis.length][arrayOfRavFileDatasetLengths.length];
                errorseriesForRav = new XYSeries[columnsInRavFileForYAxis.length][2];
                int offset;
                for (int i = 0; i < columnsInRavFileForYAxis.length; ++i) {
                    if (cbPROD.isEnabled() && cbPROD.isSelected()) {
                        offset = 1;
                        xyseriesForRav[i][xyseriesForRav[i].length - offset] = new XYSeries(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Production_(rav)"));
                        errorseriesForRav[i][0] = new XYSeries(java.util.ResourceBundle.getBundle("ms2package/lang").getString("(+)_Error"));
                        errorseriesForRav[i][1] = new XYSeries(java.util.ResourceBundle.getBundle("ms2package/lang").getString("(-)_Error"));
                        for (int j = 0; j < arrayOfRavFileDatasetLengths[arrayOfRavFileDatasetLengths.length - offset]; ++j) {
                            try {
                                xyseriesForRav[i][xyseriesForRav[i].length - offset].add(Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + 1 + j][columnInRavFileForXAxis]), Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + 1 + j][columnsInRavFileForYAxis[i]]));
                            } catch (Exception e) {
                                System.out.println(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Incorrect_data_point_at:_") + j + java.util.ResourceBundle.getBundle("ms2package/lang").getString("_for_column_") + i);
                                xyseriesForRav[i][xyseriesForRav[i].length - offset].add(Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + 1 + j][columnInRavFileForXAxis]), null);
                            }
                        }
                        if (buttonRES.isEnabled() && !arrayOfErrorsPLUS[i].startsWith("+ ")) {
                            for (int j = 0; j < arrayOfRavFileDatasetLengths[arrayOfRavFileDatasetLengths.length - offset]; ++j) {
                                try {
                                    errorseriesForRav[i][0].add(Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + 1 + j][0]), Double.parseDouble(arrayOfRavFile[arrayOfRavFile.length - 1][columnsInRavFileForYAxis[i]]) + Double.parseDouble(arrayOfErrorsPLUS[i]));
                                    errorseriesForRav[i][1].add(Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + 1 + j][0]), Double.parseDouble(arrayOfRavFile[arrayOfRavFile.length - 1][columnsInRavFileForYAxis[i]]) + Double.parseDouble(arrayOfErrorsMINUS[i]));
                                } catch (Exception e) {
                                    System.out.println(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Incorrect_data_point_(for_error)_at:_") + j + java.util.ResourceBundle.getBundle("ms2package/lang").getString("_for_column_") + i);
                                    errorseriesForRav[i][0].add(Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + 1 + j][0]), null);
                                    errorseriesForRav[i][1].add(Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + 1 + j][0]), null);
                                }
                            }
                        }
                        xyseriescollection.addSeries(xyseriesForRav[i][xyseriesForRav[i].length - offset]);
                        if (buttonRES.isEnabled() && !arrayOfErrorsPLUS[i].startsWith("+ ")) {
                            xyseriescollection.addSeries(errorseriesForRav[i][0]);
                            xyseriescollection.addSeries(errorseriesForRav[i][1]);
                        }
                    }
                    if (cbEQUI.isEnabled() && cbEQUI.isSelected()) {
                        offset = 2;
                        xyseriesForRav[i][xyseriesForRav[i].length - offset] = new XYSeries(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Equilibration_(rav)"));
                        for (int j = 0; j < arrayOfRavFileDatasetLengths[arrayOfRavFileDatasetLengths.length - offset]; ++j) {
                            try {
                                xyseriesForRav[i][xyseriesForRav[i].length - offset].add(Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + 1 + j][columnInRavFileForXAxis]), Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + 1 + j][columnsInRavFileForYAxis[i]]));
                            } catch (Exception e) {
                                System.out.println(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Incorrect_data_point_at:_") + j + java.util.ResourceBundle.getBundle("ms2package/lang").getString("_for_column_") + i);
                                xyseriesForRav[i][xyseriesForRav[i].length - offset].add(Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + 1 + j][columnInRavFileForXAxis]), null);
                            }
                        }
                        xyseriescollection.addSeries(xyseriesForRav[i][xyseriesForRav[i].length - offset]);
                    }
                    if (cbNVTE.isEnabled() && cbNVTE.isSelected()) {
                        if (cbEQUI.isEnabled()) {
                            offset = 3;
                        } else {
                            offset = 2;
                        }
                        xyseriesForRav[i][xyseriesForRav[i].length - offset] = new XYSeries(java.util.ResourceBundle.getBundle("ms2package/lang").getString("NVT_Equilibration_(rav)"));
                        for (int j = 0; j < arrayOfRavFileDatasetLengths[arrayOfRavFileDatasetLengths.length - offset]; ++j) {
                            try {
                                xyseriesForRav[i][xyseriesForRav[i].length - offset].add(Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + 1 + j][columnInRavFileForXAxis]), Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + 1 + j][columnsInRavFileForYAxis[i]]));
                            } catch (Exception e) {
                                System.out.println(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Incorrect_data_point_at:_") + j + java.util.ResourceBundle.getBundle("ms2package/lang").getString("_for_column_") + i);
                                xyseriesForRav[i][xyseriesForRav[i].length - offset].add(Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + 1 + j][columnInRavFileForXAxis]), null);
                            }
                        }
                        xyseriescollection.addSeries(xyseriesForRav[i][xyseriesForRav[i].length - offset]);
                    }
                }
            }
        }
        JFreeChart chart = ChartFactory.createXYLineChart(null, (String) comboXAxis.getSelectedItem(), titleForYAxis, xyseriescollection, org.jfree.chart.plot.PlotOrientation.VERTICAL, true, false, false);
        jPanelOfChart = new ChartPanel(chart, true);
        panelChartContainer.removeAll();
        panelChartContainer.add(jPanelOfChart);
        panelChartContainer.setVisible(false);
        panelChartContainer.setVisible(true);
        XYPlot xyplot = chart.getXYPlot();
        rangeAxis = (NumberAxis) xyplot.getRangeAxis();
        domainAxis = (NumberAxis) xyplot.getDomainAxis();
        rangeAxis.setAutoRangeIncludesZero(false);
        XYTextAnnotation textAnnotation;
        XYLineAndShapeRenderer rr = (XYLineAndShapeRenderer) xyplot.getRenderer();
        Color green = new Color(0, 255, 0);
        Color blue = new Color(0, 0, 255);
        Color red = new Color(255, 0, 0);
        Color black = new Color(0, 0, 0);
        BasicStroke stroke1 = new BasicStroke(1.0F, 1, 1, 1.0F, new float[] { 2.0F, 6F }, 0.0F);
        BasicStroke stroke2 = new BasicStroke(2.0F);
        int k = 0;
        try {
            if (rbRTR.isSelected()) {
                if (acfIsNeeded) {
                    int j = arrayOfRtrFileDatasetLengths[arrayOfRtrFileDatasetLengths.length - 1];
                    textAnnotation = new XYTextAnnotation("1/e", Double.parseDouble(arrayOfRtrFile[arrayOfRtrFileStartingIndices[arrayOfRtrFileStartingIndices.length - 1] + j][0]), 0.367879441);
                    xyplot.addAnnotation(textAnnotation);
                    rr.setSeriesPaint(k, black, true);
                    ++k;
                    textAnnotation = new XYTextAnnotation("ACF", Double.parseDouble(acf), (domainAxis.getLowerBound() + domainAxis.getUpperBound()) / 2);
                    xyplot.addAnnotation(textAnnotation);
                    rr.setSeriesPaint(k, black, true);
                    ++k;
                }
                for (int i = 0; i < columnsInRtrFileForYAxis.length; ++i) {
                    int j = arrayOfRtrFileDatasetLengths[arrayOfRtrFileDatasetLengths.length - 1];
                    textAnnotation = new XYTextAnnotation((String) selectedValues[i], Double.parseDouble(arrayOfRtrFile[arrayOfRtrFileStartingIndices[arrayOfRtrFileStartingIndices.length - 1] + j][columnInRtrFileForXAxis]), Double.parseDouble(arrayOfRtrFile[arrayOfRtrFileStartingIndices[arrayOfRtrFileStartingIndices.length - 1] + j][columnsInRtrFileForYAxis[i]]));
                    xyplot.addAnnotation(textAnnotation);
                    rr.setSeriesStroke(k, stroke2, true);
                    rr.setSeriesPaint(k, red, true);
                    ++k;
                }
            } else {
                if (cbRUN.isEnabled() && cbRUN.isSelected()) {
                    int offset;
                    for (int i = 0; i < columnsInRunFileForYAxis.length; ++i) {
                        if (cbPROD.isEnabled() && cbPROD.isSelected()) {
                            offset = 1;
                            int j = arrayOfRunFileDatasetLengths[arrayOfRunFileDatasetLengths.length - offset];
                            textAnnotation = new XYTextAnnotation((String) selectedValues[i], Double.parseDouble(arrayOfRunFile[arrayOfRunFileStartingIndices[arrayOfRunFileStartingIndices.length - offset] + j][columnInRunFileForXAxis]), Double.parseDouble(arrayOfRunFile[arrayOfRunFileStartingIndices[arrayOfRunFileStartingIndices.length - offset] + j][columnsInRunFileForYAxis[i]]));
                            xyplot.addAnnotation(textAnnotation);
                            rr.setSeriesStroke(k, stroke1, true);
                            rr.setSeriesPaint(k, red, true);
                            ++k;
                        }
                        if (cbEQUI.isEnabled() && cbEQUI.isSelected()) {
                            offset = 2;
                            int j = arrayOfRunFileDatasetLengths[arrayOfRunFileDatasetLengths.length - offset];
                            textAnnotation = new XYTextAnnotation((String) selectedValues[i], Double.parseDouble(arrayOfRunFile[arrayOfRunFileStartingIndices[arrayOfRunFileStartingIndices.length - offset] + j][columnInRunFileForXAxis]), Double.parseDouble(arrayOfRunFile[arrayOfRunFileStartingIndices[arrayOfRunFileStartingIndices.length - offset] + j][columnsInRunFileForYAxis[i]]));
                            xyplot.addAnnotation(textAnnotation);
                            rr.setSeriesStroke(k, stroke1, true);
                            rr.setSeriesPaint(k, blue, true);
                            ++k;
                        }
                        if (cbNVTE.isEnabled() && cbNVTE.isSelected()) {
                            if (cbEQUI.isEnabled()) {
                                offset = 3;
                            } else {
                                offset = 2;
                            }
                            int j = arrayOfRunFileDatasetLengths[arrayOfRunFileDatasetLengths.length - offset];
                            textAnnotation = new XYTextAnnotation((String) selectedValues[i], Double.parseDouble(arrayOfRunFile[arrayOfRunFileStartingIndices[arrayOfRunFileStartingIndices.length - offset] + j][columnInRunFileForXAxis]), Double.parseDouble(arrayOfRunFile[arrayOfRunFileStartingIndices[arrayOfRunFileStartingIndices.length - offset] + j][columnsInRunFileForYAxis[i]]));
                            xyplot.addAnnotation(textAnnotation);
                            rr.setSeriesStroke(k, stroke1, true);
                            rr.setSeriesPaint(k, green, true);
                            ++k;
                        }
                    }
                }
                if (cbRAV.isEnabled() && cbRAV.isSelected()) {
                    int offset;
                    for (int i = 0; i < columnsInRavFileForYAxis.length; ++i) {
                        if (cbPROD.isEnabled() && cbPROD.isSelected()) {
                            offset = 1;
                            int j = arrayOfRavFileDatasetLengths[arrayOfRavFileDatasetLengths.length - offset];
                            textAnnotation = new XYTextAnnotation((String) selectedValues[i], Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + j][columnInRavFileForXAxis]), Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + j][columnsInRavFileForYAxis[i]]));
                            xyplot.addAnnotation(textAnnotation);
                            rr.setSeriesStroke(k, stroke2, true);
                            rr.setSeriesPaint(k, red, true);
                            ++k;
                            if (buttonRES.isEnabled() && !arrayOfErrorsPLUS[i].startsWith("+ ")) {
                                textAnnotation = new XYTextAnnotation((String) selectedValues[i] + java.util.ResourceBundle.getBundle("ms2package/lang").getString("_(+_error)"), Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + j][0]), Double.parseDouble(arrayOfRavFile[arrayOfRavFile.length - 1][columnsInRavFileForYAxis[i]]) + Double.parseDouble(arrayOfErrorsPLUS[i]));
                                xyplot.addAnnotation(textAnnotation);
                                rr.setSeriesPaint(k, black, true);
                                ++k;
                                textAnnotation = new XYTextAnnotation((String) selectedValues[i] + java.util.ResourceBundle.getBundle("ms2package/lang").getString("_(-_error)"), Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + j][0]), Double.parseDouble(arrayOfRavFile[arrayOfRavFile.length - 1][columnsInRavFileForYAxis[i]]) + Double.parseDouble(arrayOfErrorsMINUS[i]));
                                xyplot.addAnnotation(textAnnotation);
                                rr.setSeriesPaint(k, black, true);
                                ++k;
                            }
                        }
                        if (cbEQUI.isEnabled() && cbEQUI.isSelected()) {
                            offset = 2;
                            int j = arrayOfRavFileDatasetLengths[arrayOfRavFileDatasetLengths.length - offset];
                            textAnnotation = new XYTextAnnotation((String) selectedValues[i], Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + j][columnInRavFileForXAxis]), Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + j][columnsInRavFileForYAxis[i]]));
                            xyplot.addAnnotation(textAnnotation);
                            rr.setSeriesStroke(k, stroke2, true);
                            rr.setSeriesPaint(k, blue, true);
                            ++k;
                        }
                        if (cbNVTE.isEnabled() && cbNVTE.isSelected()) {
                            if (cbEQUI.isEnabled()) {
                                offset = 3;
                            } else {
                                offset = 2;
                            }
                            int j = arrayOfRavFileDatasetLengths[arrayOfRavFileDatasetLengths.length - offset];
                            textAnnotation = new XYTextAnnotation((String) selectedValues[i], Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + j][columnInRavFileForXAxis]), Double.parseDouble(arrayOfRavFile[arrayOfRavFileStartingIndices[arrayOfRavFileStartingIndices.length - offset] + j][columnsInRavFileForYAxis[i]]));
                            xyplot.addAnnotation(textAnnotation);
                            rr.setSeriesStroke(k, stroke2, true);
                            rr.setSeriesPaint(k, green, true);
                            ++k;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tfXMax.setText(Math.round(domainAxis.getRange().getUpperBound() * 100) / 100.0 + "");
        tfXMin.setText(Math.round(domainAxis.getRange().getLowerBound() * 100) / 100.0 + "");
        tfYMax.setText(Math.round(rangeAxis.getRange().getUpperBound() * 100) / 100.0 + "");
        tfYMin.setText(Math.round(rangeAxis.getRange().getLowerBound() * 100) / 100.0 + "");
        tfXMax.setEnabled(true);
        tfXMin.setEnabled(true);
        tfYMax.setEnabled(true);
        tfYMin.setEnabled(true);
        buttonViewData.setEnabled(true);
        buttonPNGsave.setEnabled(true);
        buttonXAxisChange.setEnabled(true);
        buttonYAxisChange.setEnabled(true);
        buttonClear.setEnabled(true);
    }

    private String getError(String titleSelected) {
        String trailingTitle = null;
        if (titleSelected.startsWith("MUE") || titleSelected.startsWith("FRACT") || titleSelected.startsWith("VW") || titleSelected.startsWith("D_i")) {
            StringTokenizer st = new StringTokenizer(titleSelected);
            titleSelected = st.nextToken();
            trailingTitle = st.nextToken();
        }
        String reducedError = "";
        File inputFile;
        if (rbRTR.isSelected()) {
            inputFile = new File(selectedDirectory.getAbsolutePath() + java.io.File.separator + caseTitle + ".res");
        } else {
            inputFile = new File(selectedDirectory.getAbsolutePath() + java.io.File.separator + caseTitle + "_1.res");
        }
        try {
            BufferedReader inputStream = new BufferedReader(new FileReader(inputFile));
            inputStream.readLine();
            String fileInString;
            do {
                fileInString = inputStream.readLine();
            } while (!fileInString.endsWith("===="));
            if (rbRTR.isSelected()) {
                do {
                    fileInString = inputStream.readLine();
                } while (!fileInString.endsWith("===="));
            }
            String line;
            while (!(line = inputStream.readLine()).endsWith("====")) {
                fileInString += line + " ";
            }
            StringTokenizer st = new StringTokenizer(fileInString);
            String[] error2 = new String[0];
            String title = titleSelected;
            title = title.replaceAll("DRUCK", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Pressure"));
            title = title.replaceAll("DICHTE", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Density"));
            title = title.replaceAll("TEMP", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Temperature"));
            title = title.replaceAll("EPOT", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Potential"));
            title = title.replaceAll("ENTLP", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Enthalpy"));
            title = title.replaceAll("MUE", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Chemical"));
            title = title.replaceAll("FRACT", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Molar"));
            title = title.replaceAll("VW", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Partial"));
            title = title.replaceAll("D_i", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Self-diff."));
            title = title.replaceAll("VS", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Shear-Viscosity"));
            title = title.replaceAll("VB", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Bulk-Viscosity"));
            title = title.replaceAll("CO", java.util.ResourceBundle.getBundle("ms2package/lang").getString("Thermal"));
            if (title.equals(titleSelected)) {
                setLabelStatusMessage(java.util.ResourceBundle.getBundle("ms2package/lang").getString("No_column_for_") + titleSelected + java.util.ResourceBundle.getBundle("ms2package/lang").getString("_in_") + caseTitle + ".res");
                return " ";
            }
            while (st.hasMoreTokens()) {
                line = st.nextToken();
                String delimiter;
                if (line.equals(title)) {
                    if (title.equals(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Pressure")) || title.equals(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Density")) || title.equals(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Temperature")) || title.equals(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Potential")) || title.equals(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Enthalpy")) || title.equals(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Shear-Viscosity")) || title.equals(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Bulk-Viscosity")) || title.equals(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Thermal"))) {
                        do {
                            delimiter = st.nextToken();
                        } while (!delimiter.endsWith(":"));
                        if (rbRTR.isSelected()) {
                            do {
                                delimiter = st.nextToken();
                            } while (!delimiter.endsWith(":"));
                        }
                        st.nextToken();
                        reducedError = st.nextToken();
                        break;
                    } else if (title.equals(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Chemical")) || title.equals(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Molar")) || title.equals(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Partial")) || title.equals(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Self-diff."))) {
                        String[] tempArray = new String[error2.length + 1];
                        System.arraycopy(error2, 0, tempArray, 0, error2.length);
                        do {
                            delimiter = st.nextToken();
                        } while (!(delimiter.endsWith(":")));
                        if (rbRTR.isSelected()) {
                            do {
                                delimiter = st.nextToken();
                            } while (!delimiter.endsWith(":"));
                        }
                        st.nextToken();
                        tempArray[error2.length] = st.nextToken();
                        error2 = tempArray;
                    }
                }
            }
            if (title.equals(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Chemical")) || title.equals(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Molar")) || title.equals(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Partial")) || title.equals(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Self-diff."))) {
                reducedError = error2[Integer.parseInt(trailingTitle) - 1];
            }
        } catch (Exception e) {
            e.printStackTrace();
            return " ";
        }
        return reducedError;
    }

    private String getAverageRTR() {
        String acf = " ";
        File inputFile = new File(selectedDirectory.getAbsolutePath() + java.io.File.separator + caseTitle + ".res");
        try {
            BufferedReader inputStream = new BufferedReader(new FileReader(inputFile));
            inputStream.readLine();
            String fileInString;
            do {
                fileInString = inputStream.readLine();
            } while (!fileInString.endsWith("===="));
            do {
                fileInString = inputStream.readLine();
            } while (!fileInString.endsWith("===="));
            String line;
            while (!(line = inputStream.readLine()).endsWith("====")) {
                fileInString += line + " ";
            }
            StringTokenizer st = new StringTokenizer(fileInString);
            while (st.hasMoreTokens()) {
                line = st.nextToken();
                if (line.equals(java.util.ResourceBundle.getBundle("ms2package/lang").getString("Time"))) {
                    line = st.nextToken();
                    if (line.equals(java.util.ResourceBundle.getBundle("ms2package/lang").getString("span"))) {
                        String delimiter;
                        do {
                            delimiter = st.nextToken();
                        } while (!delimiter.endsWith(":"));
                        do {
                            delimiter = st.nextToken();
                        } while (!delimiter.endsWith(":"));
                        acf = st.nextToken();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            acf = " ";
        }
        return acf;
    }

    private void setLabelStatusMessage(String statusMessage) {
        String newStatusMessage = statusMessage + " | " + labelStatus.getText();
        if (newStatusMessage.length() > 100) {
            newStatusMessage = newStatusMessage.substring(0, 100) + "...";
        }
        labelStatus.setText(newStatusMessage);
    }

    private javax.swing.JButton buttonBrowse;

    private javax.swing.JButton buttonClear;

    private javax.swing.JButton buttonDrawChart;

    private javax.swing.ButtonGroup buttonGroupFileType;

    private javax.swing.JButton buttonLOG;

    private javax.swing.JButton buttonPAR;

    private javax.swing.JButton buttonPNGsave;

    private javax.swing.JButton buttonQuit;

    private javax.swing.JButton buttonRES;

    private javax.swing.JButton buttonRestart;

    private javax.swing.JButton buttonViewData;

    private javax.swing.JButton buttonXAxisChange;

    private javax.swing.JButton buttonYAxisChange;

    private javax.swing.JCheckBox cbAskDirectory;

    private javax.swing.JCheckBox cbAutoDraw;

    private javax.swing.JCheckBox cbEQUI;

    private javax.swing.JCheckBox cbNVTE;

    private javax.swing.JCheckBox cbPROD;

    private javax.swing.JCheckBox cbRAV;

    private javax.swing.JCheckBox cbRUN;

    private javax.swing.JComboBox comboCaseList;

    private javax.swing.JComboBox comboXAxis;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel3;

    private javax.swing.JLabel jLabel4;

    private javax.swing.JLabel jLabel5;

    private javax.swing.JLabel jLabel6;

    private javax.swing.JLabel jLabel7;

    private javax.swing.JLabel jLabel8;

    private javax.swing.JLabel jLabel9;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JSeparator jSeparator1;

    private javax.swing.JSeparator jSeparator2;

    private javax.swing.JLabel labelStatus;

    private javax.swing.JList listYAxises;

    private javax.swing.JPanel panelChartContainer;

    private javax.swing.JPanel panelControl;

    private javax.swing.JPanel panelStatusBar;

    private javax.swing.JRadioButton rbRTR;

    private javax.swing.JRadioButton rbRUN_RAV;

    private javax.swing.JTextField tfError;

    private javax.swing.JTextField tfXMax;

    private javax.swing.JTextField tfXMin;

    private javax.swing.JTextField tfYMax;

    private javax.swing.JTextField tfYMin;

    private boolean isErrorSeriesDrawable;

    private boolean isHavingCases;

    private ChartPanel jPanelOfChart;

    private File selectedDirectory;

    private static File startingDirectory;

    private int[] arrayOfRtrFileDatasetLengths;

    private int[] arrayOfRavFileDatasetLengths;

    private int[] arrayOfRunFileDatasetLengths;

    private int[] arrayOfRtrFileStartingIndices;

    private int[] arrayOfRavFileStartingIndices;

    private int[] arrayOfRunFileStartingIndices;

    private int columnInRtrFileForXAxis = -1;

    private int columnInRavFileForXAxis = -1;

    private int columnInRunFileForXAxis = -1;

    private int[] columnsInRtrFileForYAxis = new int[0];

    private int[] columnsInRavFileForYAxis = new int[0];

    private int[] columnsInRunFileForYAxis = new int[0];

    private NumberAxis domainAxis;

    private NumberAxis rangeAxis;

    private String[][] arrayOfRtrFile;

    private String[][] arrayOfRavFile;

    private String[][] arrayOfRunFile;

    private String caseTitle;

    private XYSeries[][] errorseriesForRav;

    private XYSeries[][] xyseriesForRtr;

    private XYSeries[][] xyseriesForRav;

    private XYSeries[][] xyseriesForRun;

    private XYSeries averageSeries;

    private XYSeries oneByESeries;
}

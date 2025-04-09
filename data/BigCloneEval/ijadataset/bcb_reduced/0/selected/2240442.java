package magictool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import magictool.filefilters.GifFilter;
import magictool.filefilters.NoEditFileChooser;
import slider.MThumbSlider;
import slider.MetalMThumbSliderUI;

/**
 * TableFrame is a frame which displays colored table containing the data from an expression file.
 * The genes shown may be limited to a group file within an expression file. The color scale can be
 * altered by the user.
 */
public class TableFrame extends JInternalFrame {

    private JPanel contentPane = new JPanel();

    private JScrollPane jScrollPane1;

    private DefaultTableCellRenderer tablecellrenderer = new DefaultTableCellRenderer();

    private BorderLayout borderLayout1 = new BorderLayout();

    private JPanel paramsPanel = new JPanel();

    private JPanel pixPanel = new JPanel();

    private JLabel pixLabel = new JLabel();

    private JTextField pixTextField = new JTextField();

    private VerticalLayout verticalLayout1 = new VerticalLayout();

    private JButton heightButton = new JButton();

    private JPanel sliderPanel = new JPanel();

    private VerticalLayout verticalLayout2 = new VerticalLayout();

    private JLabel black, centerLabel, white;

    private JPanel labelsPanel;

    private JMenuBar jMenuBar1 = new JMenuBar();

    private JMenu filemenu = new JMenu();

    private JMenuItem print = new JMenuItem();

    private JMenuItem saveMenu = new JMenuItem();

    private JMenuItem close = new JMenuItem();

    private JMenu colormenu = new JMenu();

    private JCheckBoxMenuItem graymenu = new JCheckBoxMenuItem();

    private JCheckBoxMenuItem rgmenu = new JCheckBoxMenuItem();

    private JMenu editMenu = new JMenu();

    private JMenuItem decimalMenu = new JMenuItem();

    private DecimalFormat labelFormat = new DecimalFormat("0.##");

    private Project project;

    private ColorLabel colorLabel;

    /**table displayed containing the data from the expression file*/
    protected PrintableTable jTable;

    /**expression file displayed in the table*/
    protected ExpFile expMain;

    private float minvalue;

    private float maxvalue;

    private float radius;

    private float center;

    private float actualmax, actualmin;

    /**number of pixels per line in the table*/
    protected int pixPerLine;

    private MThumbSlider mSlider;

    /**
   * Constructs a frame containing a table of all genes in the specified expression file
   * @param expMain expression file to display
   * @param project project associated with the table
   */
    public TableFrame(ExpFile expMain, Project project) {
        this(expMain, new GrpFile(), project);
    }

    /**
   * Constructs a frame containing a table of the genes in the group file from the specified expression file
   * @param expMain expression file to display
   * @param grp group file containing group of genes to be displayed
   * @param project project associated with the table
   */
    public TableFrame(ExpFile expMain, GrpFile grp, Project project) {
        this.expMain = expMain;
        Vector v = new Vector();
        this.project = project;
        Object o[] = grp.getGroup();
        for (int i = 0; i < o.length; i++) {
            v.addElement(o[i]);
        }
        if (v.size() == 0) jTable = new PrintableTable(expMain, PrintableTable.GRAYSCALE); else jTable = new PrintableTable(expMain, v, PrintableTable.GRAYSCALE);
        jScrollPane1 = new JScrollPane(jTable);
        minvalue = actualmin = expMain.getMinExpValue();
        maxvalue = actualmax = expMain.getMaxExpValue();
        center = minvalue + (maxvalue - minvalue) / 2;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        contentPane.setLayout(borderLayout1);
        this.setClosable(true);
        this.setIconifiable(true);
        this.setJMenuBar(jMenuBar1);
        this.setMaximizable(true);
        this.setResizable(true);
        int n = 3;
        int min = (int) minvalue;
        int max = (int) maxvalue;
        mSlider = new MThumbSlider(n, 0, 1000);
        mSlider.setUI(new MetalMThumbSliderUI());
        colorLabel = new ColorLabel((double) minvalue, (double) maxvalue, (double) minvalue, (double) maxvalue, (double) ((minvalue + maxvalue) / 2), Color.white, new Color(153, 153, 153), Color.black);
        white = new JLabel("White", JLabel.LEFT);
        white.setForeground(Color.white);
        black = new JLabel("Black", JLabel.CENTER);
        black.setForeground(Color.black);
        black.setHorizontalAlignment(SwingConstants.RIGHT);
        centerLabel = new JLabel("Center", JLabel.RIGHT);
        centerLabel.setForeground(new Color(255 / 2, 255 / 2, 255 / 2));
        centerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        pixLabel.setText("Pixels Per Line:");
        paramsPanel.setPreferredSize(new Dimension(582, 150));
        paramsPanel.setLayout(verticalLayout1);
        pixTextField.setPreferredSize(new Dimension(45, 21));
        heightButton.setText("Update Line Height");
        heightButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                heightButton_actionPerformed(e);
            }
        });
        rootPane.setDefaultButton(heightButton);
        sliderPanel.setBorder(BorderFactory.createEtchedBorder());
        pixPanel.setBorder(BorderFactory.createEtchedBorder());
        print.setText("Print...");
        print.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK, false));
        print.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                print_actionPerformed(e);
            }
        });
        saveMenu.setText("Save Image...");
        saveMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK, false));
        saveMenu.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                saveMenu_actionPerformed(e);
            }
        });
        close.setText("Close");
        close.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK, false));
        close.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                close_actionPerformed(e);
            }
        });
        filemenu.setText("File");
        colormenu.setText("Color");
        graymenu.setText("Grayscale");
        graymenu.setState(true);
        graymenu.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                graymenu_actionPerformed(e);
            }
        });
        rgmenu.setText("Red/Green");
        rgmenu.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                rgmenu_actionPerformed(e);
            }
        });
        colorLabel.setText("Color Label");
        editMenu.setText("Edit");
        decimalMenu.setText("Decimal Places");
        decimalMenu.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                decimalMenu_actionPerformed(e);
            }
        });
        this.getContentPane().add(contentPane, BorderLayout.CENTER);
        this.setSize(new Dimension(500, 580));
        this.setLocation(100, 100);
        contentPane.add(jScrollPane1, BorderLayout.CENTER);
        this.getContentPane().add(paramsPanel, BorderLayout.SOUTH);
        paramsPanel.add(sliderPanel, null);
        pixPanel.add(pixLabel, null);
        pixPanel.add(pixTextField, null);
        pixPanel.add(heightButton, null);
        paramsPanel.add(pixPanel, null);
        jMenuBar1.add(filemenu);
        jMenuBar1.add(editMenu);
        jMenuBar1.add(colormenu);
        filemenu.add(saveMenu);
        filemenu.add(print);
        filemenu.addSeparator();
        filemenu.add(close);
        colormenu.add(rgmenu);
        colormenu.add(graymenu);
        editMenu.add(decimalMenu);
        jTable.paintTable(minvalue, center, maxvalue, jTable.getRowHeight());
        setParams();
    }

    private void setParams() {
        pixTextField.setText(Integer.toString(jTable.getRowHeight()));
        mSlider = new MThumbSlider(3, 0, 1000);
        mSlider.setUI(new MetalMThumbSliderUI());
        mSlider.setValueAt(0, 0);
        mSlider.setValueAt(1000, 1);
        mSlider.setValueAt(500, 2);
        mSlider.setFillColorAt(Color.white, 0);
        mSlider.setFillColorAt(new Color(255 / 2, 255 / 2, 255 / 2), 1);
        mSlider.setTrackFillColor(Color.black);
        Hashtable imageDictionary = new Hashtable();
        imageDictionary.put(new Integer(0), new JLabel(labelFormat.format(convertSlider(0))));
        imageDictionary.put(new Integer(250), new JLabel(labelFormat.format(convertSlider(250))));
        imageDictionary.put(new Integer(500), new JLabel(labelFormat.format(convertSlider(500))));
        imageDictionary.put(new Integer(750), new JLabel(labelFormat.format(convertSlider(750))));
        imageDictionary.put(new Integer(1000), new JLabel(labelFormat.format(convertSlider(1000))));
        mSlider.setMinorTickSpacing(1000 / 8);
        mSlider.setMajorTickSpacing(1000 / 4);
        mSlider.setPaintTicks(true);
        mSlider.setPaintLabels(true);
        mSlider.setLabelTable(imageDictionary);
        labelsPanel = new JPanel(new GridLayout(1, 3));
        colorLabel.setBeginEndValues((double) minvalue, (double) maxvalue);
        colorLabel.setMinMax((double) minvalue, (double) maxvalue);
        colorLabel.setCenter((double) center);
        colorLabel.setColors(Color.white, new Color(153, 153, 153), Color.black);
        colorLabel.showLabels();
        sliderPanel.setLayout(verticalLayout2);
        sliderPanel.add(labelsPanel);
        sliderPanel.add(mSlider);
        sliderPanel.add(colorLabel, null);
        labelsPanel.add(white);
        labelsPanel.add(centerLabel);
        labelsPanel.add(black);
        mSlider.setMiddleRange();
        mSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                mSlider_stateChanged(e);
            }
        });
        centerLabel.setText("Center: " + labelFormat.format(convertSlider(mSlider.getValueAt(2))));
        white.setText("White: " + labelFormat.format(convertSlider(mSlider.getValueAt(0))));
        black.setText("Black: " + labelFormat.format(convertSlider(mSlider.getValueAt(1))));
    }

    private double convertSlider(int val) {
        return actualmin + (val / 1000.0) * (actualmax - actualmin);
    }

    private void mSlider_stateChanged(ChangeEvent e) {
        mSlider.setMiddleRange();
        centerLabel.setText("Center: " + labelFormat.format(convertSlider(mSlider.getValueAt(2))));
        white.setText((jTable.getType() == jTable.GRAYSCALE ? "White: " : "Green: ") + labelFormat.format(convertSlider(mSlider.getValueAt(0))));
        black.setText((jTable.getType() == jTable.GRAYSCALE ? "Black: " : "Red: ") + labelFormat.format(convertSlider(mSlider.getValueAt(1))));
        minvalue = (float) convertSlider(mSlider.getValueAt(0));
        center = (float) convertSlider(mSlider.getValueAt(2));
        maxvalue = (float) convertSlider(mSlider.getValueAt(1));
        jTable.paintTable(minvalue, center, maxvalue, jTable.getRowHeight());
        colorLabel.setBeginEndValues((double) minvalue, (double) maxvalue);
        colorLabel.setCenter((double) center);
    }

    private void heightButton_actionPerformed(ActionEvent e) {
        try {
            pixPerLine = Integer.parseInt(pixTextField.getText().trim());
            minvalue = (float) convertSlider(mSlider.getValueAt(0));
            center = (float) convertSlider(mSlider.getValueAt(2));
            maxvalue = (float) convertSlider(mSlider.getValueAt(1));
            colorLabel.setBeginEndValues((double) minvalue, (double) maxvalue);
            colorLabel.setCenter((double) center);
            jTable.paintTable(minvalue, center, maxvalue, pixPerLine);
        } catch (Exception e1) {
            pixTextField.setText("" + jTable.getRowHeight());
        }
    }

    private void print_actionPerformed(ActionEvent e) {
        Thread thread = new Thread() {

            public void run() {
                PrinterJob pj = PrinterJob.getPrinterJob();
                PageFormat pf = pj.pageDialog(pj.defaultPage());
                jTable.setDoubleBuffered(false);
                pj.setPrintable(jTable, pf);
                colorLabel.showLabels();
                jTable.header = colorLabel;
                if (pj.printDialog()) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    try {
                        pj.print();
                    } catch (Exception PrintException) {
                    }
                    setCursor(Cursor.getDefaultCursor());
                }
                colorLabel.showLabels();
                jTable.setDoubleBuffered(true);
            }
        };
        thread.start();
    }

    private void saveMenu_actionPerformed(ActionEvent e) {
        Thread thread = new Thread() {

            public void run() {
                try {
                    NoEditFileChooser jfc = new NoEditFileChooser(MainFrame.fileLoader.getFileSystemView());
                    jfc.setFileFilter(new GifFilter());
                    jfc.setDialogTitle("Create New Gif File...");
                    jfc.setApproveButtonText("Select");
                    File direct = new File(project.getPath() + "images" + File.separator);
                    if (!direct.exists()) direct.mkdirs();
                    jfc.setCurrentDirectory(direct);
                    int result = jfc.showSaveDialog(null);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File fileobj = jfc.getSelectedFile();
                        String name = fileobj.getPath();
                        if (!name.endsWith(".gif")) name += ".gif";
                        jTable.setHeader(colorLabel);
                        saveImage(name);
                    }
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(null, "Failed To Create Image");
                    e1.printStackTrace();
                }
            }
        };
        thread.start();
    }

    private void saveImage(String name) {
        try {
            int number = 1;
            jTable.saveImage(name, number = (int) Math.ceil(jTable.getMegaPixels() / project.getImageSize()));
            if (number > 1) {
                String tn = name.substring(name.lastIndexOf(File.separator), name.lastIndexOf("."));
                String tempname = name.substring(0, name.lastIndexOf(File.separator)) + tn + ".html";
                BufferedWriter bw = new BufferedWriter(new FileWriter(tempname));
                bw.write("<html><header><title>" + name + "</title></header>");
                bw.write("<body>");
                bw.write("<table cellpadding=0 cellspacing=0 border=0");
                for (int i = 0; i < number; i++) {
                    bw.write("<tr><td><img src=" + tn.substring(1) + "_images" + tn + i + ".gif border=0></td></tr>");
                }
                bw.write("</table></body></html>");
                bw.close();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed To Create Image");
        }
    }

    private void close_actionPerformed(ActionEvent e) {
        dispose();
    }

    private void rgmenu_actionPerformed(ActionEvent e) {
        graymenu.setState(false);
        rgmenu.setState(true);
        jTable.setType(PrintableTable.REDGREEN);
        jTable.paintTable(minvalue, center, maxvalue, jTable.getRowHeight());
        centerLabel.setText("Center: " + labelFormat.format(convertSlider(mSlider.getValueAt(2))));
        white.setText("Green: " + labelFormat.format(convertSlider(mSlider.getValueAt(0))));
        white.setForeground(new Color(51, 153, 51));
        black.setText("Red: " + labelFormat.format(convertSlider(mSlider.getValueAt(1))));
        black.setForeground(new Color(255, 0, 0));
        centerLabel.setForeground(new Color(0, 0, 0));
        colorLabel.setColors(Color.green, Color.black, Color.red);
        mSlider.setFillColorAt(Color.green, 0);
        mSlider.setFillColorAt(Color.black, 1);
        mSlider.setTrackFillColor(Color.red);
    }

    private void graymenu_actionPerformed(ActionEvent e) {
        graymenu.setState(true);
        rgmenu.setState(false);
        jTable.setType(PrintableTable.GRAYSCALE);
        jTable.paintTable(minvalue, center, maxvalue, jTable.getRowHeight());
        centerLabel.setText("Center: " + labelFormat.format(convertSlider(mSlider.getValueAt(2))));
        white.setText("White: " + labelFormat.format(convertSlider(mSlider.getValueAt(0))));
        black.setText("Black: " + labelFormat.format(convertSlider(mSlider.getValueAt(1))));
        black.setForeground(Color.black);
        white.setForeground(Color.white);
        centerLabel.setForeground(new Color(255 / 2, 255 / 2, 255 / 2));
        colorLabel.setColors(Color.white, new Color(255 / 2, 255 / 2, 255 / 2), Color.black);
        mSlider.setFillColorAt(Color.white, 0);
        mSlider.setFillColorAt(new Color(255 / 2, 255 / 2, 255 / 2), 1);
        mSlider.setTrackFillColor(Color.black);
    }

    private void decimalMenu_actionPerformed(ActionEvent e) {
        try {
            String number = "";
            number = JOptionPane.showInputDialog(this, "Please Enter Decimal Places To Show");
            if (number != null) {
                int n = Integer.parseInt(number);
                if (n >= 1) {
                    String form = "####.#";
                    for (int i = 1; i < n; i++) {
                        form += "#";
                    }
                    DecimalFormat df = new DecimalFormat(form);
                    jTable.setDecimalFormat(df);
                } else JOptionPane.showMessageDialog(this, "Error! You Must Enter An Integer Value Greater Than Or Equal To 1.", "Error!", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e2) {
            JOptionPane.showMessageDialog(this, "Error! You Must Enter An Integer Value Greater Than Or Equal To 1.", "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }
}

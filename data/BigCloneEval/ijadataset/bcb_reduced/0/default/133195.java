import gnu.io.CommPortIdentifier;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.*;
import java.util.Enumeration;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.*;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

public class MainFrame extends javax.swing.JFrame implements ActionListener, FocusListener, ListSelectionListener {

    /** Creates new form MainFrame */
    protected JTable table;

    private Vector enteredCode = new Vector(224, 5);

    protected EnteredCodeTable tabelle = new EnteredCodeTable(enteredCode);

    private Vector gCode = new Vector(30, 5);

    private Vector mCode = new Vector(15, 5);

    private ProgramCode tmpProgram = new ProgramCode();

    private JFileChooser fc = new JFileChooser();

    private int position;

    private int previousPosition;

    private Config c;

    private Thread rxtx;

    private String debug;

    private String homeDir;

    private String configFile;

    private String programVersion = "EmcoGUI V0.7";

    private int threadId = 0;

    private ReceiveNetwork rn = null;

    private ReceiveSerial rs = null;

    private SendSerial ss = null;

    private SendNetwork sn = null;

    private File currentFile;

    private String[] comPorts;

    private String[] parityText = { "None", "Odd", "Even", "Mark", "Space" };

    private String[] stopBitText = { "1", "2", "1,5" };

    private String[] dataBitValues = { "5", "6", "7", "8" };

    private String[] flowControlText = { "None", "RTSCTS_IN", "RTSCTS_OUT", "XONOFF_IN", "XONOFF_OUT" };

    private String aboutText = programVersion + "\n" + java.util.ResourceBundle.getBundle("MainFrameBundle").getString("THIS SOFTWARE HAS BEEN CREATED DURING OUR DEGREE DISSERTATION IN 2010.") + "\n" + java.util.ResourceBundle.getBundle("MainFrameBundle").getString("THE SOFTWARE WAS DEVELOPED BY HOLGER HERBST AND MARKUS SCHEFFOLD ") + "\n" + java.util.ResourceBundle.getBundle("MainFrameBundle").getString("AT ELEKTRONIKSCHULE TETTNANG, GERMANY, AND VEDC/PPPPTK IN MALANG, JAVA, INDONESIA. ") + "\n" + java.util.ResourceBundle.getBundle("MainFrameBundle").getString("THIS SOFTWARE IS GOING TO BE LICENCED UNDER THE GPL GENERAL PUBLIC LICENCE SOON. ") + "\n" + java.util.ResourceBundle.getBundle("MainFrameBundle").getString("AT THE TIME OF CREATION IT WAS MADE TO CONTROL AN EMCO COMPACT 5 CNC AND CONTROL T.U. CNC-2A.");

    private String fileNotFoundError = java.util.ResourceBundle.getBundle("MainFrameBundle").getString("FILE NOT FOUND!");

    private String ioError = java.util.ResourceBundle.getBundle("MainFrameBundle").getString("INPUT/OUTPUT ERROR!");

    private String unexpectedError = java.util.ResourceBundle.getBundle("MainFrameBundle").getString("UNEXPECTED ERROR");

    private String classNotFoundError = java.util.ResourceBundle.getBundle("MainFrameBundle").getString("CLASS NOT FOUND!");

    private String numberFormatError = java.util.ResourceBundle.getBundle("MainFrameBundle").getString("WRONG NUMBER FORMAT!");

    public MainFrame() {
        super("EmcoGUI V0.7");
        debug = "Application startet\nProcessing MainFrame()" + "\nTitle set to: " + "" + super.getTitle() + "\nConstructor of Superclass processed";
        homeDir = System.getProperty("user.home");
        configFile = homeDir + "/EMCOGui.ser";
        debug += "\nSearching user home directory";
        debug += "\nUser Home Path: " + homeDir;
        this.getComPorts();
        String errMsg = java.util.ResourceBundle.getBundle("MainFrameBundle").getString("ERROR WHILE LOADING THE CONFIG FILE. DEFAULT CONFIGURATION WILL BE LOADED.");
        try {
            debug += "\nTry to load config file: " + configFile;
            FileInputStream fis = new FileInputStream(configFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            c = (Config) ois.readObject();
            ois.close();
            debug += "\nConfig file found and loaded, configuration set";
        } catch (FileNotFoundException ex) {
            c = new Config();
            debug += "\nConfig file not found, file missing or wrong file name";
            debug += "\nDefault config will be used";
            JOptionPane.showMessageDialog(this, errMsg, fileNotFoundError, JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            c = new Config();
            debug += "\nConfig file not loaded: IOException";
            debug += "\nDefault config will be used";
            JOptionPane.showMessageDialog(this, errMsg, ioError, JOptionPane.WARNING_MESSAGE);
        } catch (ClassNotFoundException ex) {
            c = new Config();
            debug += "\nConfig file not loaded: ClassNotFoundException";
            debug += "\nDefault config will be used";
            JOptionPane.showMessageDialog(this, errMsg, classNotFoundError, JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            c = new Config();
            debug += "\nConfig file not loaded: Unknown Exception";
            debug += "\nDefault config will be used";
            JOptionPane.showMessageDialog(this, errMsg, unexpectedError, JOptionPane.WARNING_MESSAGE);
        }
        debug += c;
        getDefaultMachineCode();
        debug += "\nCNC-Command set loaded";
        initComponents();
        this.MISave.setEnabled(false);
        this.TADebug.setText(debug);
        this.TADebug.append("\ninitComponents() processed");
        if (!c.getUseDebugOutput()) {
            this.TPMain.remove(this.JPDebug);
        }
        UpdateTransmissionTab(true);
        this.TADebug.append("\nUpdateTransmissionTab(true) processed");
        UpdateConfigTab();
        this.TADebug.append("\nUpdateConfigTab() processed");
        this.setPreferredSize(new java.awt.Dimension(800, 600));
        this.setMaximumSize(new java.awt.Dimension(1024, 768));
        this.setMinimumSize(new java.awt.Dimension(800, 600));
        fc.addChoosableFileFilter(new MyFileFilter());
        TableColumn column = null;
        for (int i = 0; i < 11; i++) {
            column = table.getColumnModel().getColumn(i);
            if (i == 2 || i == 4 || i == 6 || i == 8) {
                column.setPreferredWidth(15);
            } else {
                column.setPreferredWidth(150);
            }
        }
        this.TADebug.append("\nWindowsize and table column width set");
        this.TADebug.append("\nDone with processing MainFrame()");
        this.TADebug.append("\n=======================================");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("MainFrameBundle");
        BGMetric = new javax.swing.ButtonGroup();
        BGTrans = new javax.swing.ButtonGroup();
        TPMain = new javax.swing.JTabbedPane();
        JPMain = new javax.swing.JPanel();
        table = new JTable(tabelle);
        SPProgram = new javax.swing.JScrollPane(table);
        JPMainNorth = new javax.swing.JPanel();
        JPMainNorthLeft = new javax.swing.JPanel();
        JPMainNorthLeft1 = new javax.swing.JPanel();
        JLProgName = new javax.swing.JLabel();
        TFProgName = new javax.swing.JTextField("pawn code", 20);
        JPMainNorthLeft2 = new javax.swing.JPanel();
        JLMeasurement = new javax.swing.JLabel();
        JRBInch = new javax.swing.JRadioButton();
        JRBMetric = new javax.swing.JRadioButton();
        JPMainNorthLeft3 = new javax.swing.JPanel();
        JLDescription = new javax.swing.JLabel();
        JPMainNorthLeft4 = new javax.swing.JPanel();
        JLLine = new javax.swing.JLabel();
        TFLine = new javax.swing.JTextField(4);
        JLCode = new javax.swing.JLabel();
        TFCode = new javax.swing.JTextField(4);
        JPMainNorthLeft5 = new javax.swing.JPanel();
        JLComment = new javax.swing.JLabel();
        TFComment = new javax.swing.JTextField(20);
        JPMainNorthCenter = new javax.swing.JPanel();
        JPMainNorthCenter1 = new javax.swing.JPanel();
        CBGCode = new javax.swing.JComboBox();
        JPMainNorthCenter2 = new javax.swing.JPanel();
        CBMCode = new javax.swing.JComboBox();
        JPMainNorthCenter3 = new javax.swing.JPanel();
        JPMainNorthCenter31 = new javax.swing.JPanel();
        JPMainNorthCenter311 = new javax.swing.JPanel();
        JLX = new javax.swing.JLabel();
        JPMainNorthCenter312 = new javax.swing.JPanel();
        TFX = new javax.swing.JTextField(4);
        JPMainNorthCenter32 = new javax.swing.JPanel();
        JPMainNorthCenter321 = new javax.swing.JPanel();
        JLZ = new javax.swing.JLabel();
        JPMainNorthCenter322 = new javax.swing.JPanel();
        TFZ = new javax.swing.JTextField(4);
        JPMainNorthCenter4 = new javax.swing.JPanel();
        JPMainNorthCenter41 = new javax.swing.JPanel();
        JPMainNorthCenter411 = new javax.swing.JPanel();
        JLF = new javax.swing.JLabel();
        JPMainNorthCenter412 = new javax.swing.JPanel();
        TFF = new javax.swing.JTextField(4);
        JPMainNorthCenter42 = new javax.swing.JPanel();
        JPMainNorthCenter421 = new javax.swing.JPanel();
        JLH = new javax.swing.JLabel();
        JPMainNorthCenter422 = new javax.swing.JPanel();
        TFH = new javax.swing.JTextField(4);
        JPMainNorthCenter5 = new javax.swing.JPanel();
        JPMainNorthCenter51 = new javax.swing.JPanel();
        JBAdd = new javax.swing.JButton();
        JPMainNorthCenter52 = new javax.swing.JPanel();
        JBDelete = new javax.swing.JButton();
        JPMainNorthCenter6 = new javax.swing.JPanel();
        JPMainNorthCenter61 = new javax.swing.JPanel();
        JBInsert = new javax.swing.JButton();
        JPMainNorthCenter62 = new javax.swing.JPanel();
        JBUpdate = new javax.swing.JButton();
        JPTrans = new javax.swing.JPanel();
        JPTransCenter = new javax.swing.JPanel();
        SPRxTx = new javax.swing.JScrollPane();
        TARxTx = new javax.swing.JTextArea();
        JPTransRight = new javax.swing.JPanel();
        JPTransRight1 = new javax.swing.JPanel();
        JPTransRight2 = new javax.swing.JPanel();
        JPTransRight3 = new javax.swing.JPanel();
        JPTransRight3a = new javax.swing.JPanel();
        JRBEthernet = new javax.swing.JRadioButton();
        JPTransRight3b = new javax.swing.JPanel();
        JRBSerial = new javax.swing.JRadioButton();
        JPTransRight4 = new javax.swing.JPanel();
        CBXport = new javax.swing.JCheckBox();
        JPTransRight5 = new javax.swing.JPanel();
        JPTransRight6 = new javax.swing.JPanel();
        JPTransRight7 = new javax.swing.JPanel();
        JBTransmit = new javax.swing.JButton();
        JPTransRight8 = new javax.swing.JPanel();
        JBReceive = new javax.swing.JButton();
        JPTransRight9 = new javax.swing.JPanel();
        JBTransAbort = new javax.swing.JButton();
        JPTransRight10 = new javax.swing.JPanel();
        JPTransRight11 = new javax.swing.JPanel();
        JPTransRight12 = new javax.swing.JPanel();
        JPTransRight13 = new javax.swing.JPanel();
        JPTransRight14 = new javax.swing.JPanel();
        JPTransRight15 = new javax.swing.JPanel();
        JPTransRight16 = new javax.swing.JPanel();
        JPTransRight17 = new javax.swing.JPanel();
        JPTransRight18 = new javax.swing.JPanel();
        JPTransRight19 = new javax.swing.JPanel();
        JPTransRight20 = new javax.swing.JPanel();
        JPConf = new javax.swing.JPanel();
        JPConf1 = new javax.swing.JPanel();
        JPConf11 = new javax.swing.JPanel();
        JPConf111 = new javax.swing.JPanel();
        JLSerialInterface = new javax.swing.JLabel();
        JPConf112 = new javax.swing.JPanel();
        JCBSerialInterface = new javax.swing.JComboBox();
        JPConf113 = new javax.swing.JPanel();
        JLBaudRate = new javax.swing.JLabel();
        JPConf114 = new javax.swing.JPanel();
        JCBBaudRate = new javax.swing.JComboBox();
        JPConf115 = new javax.swing.JPanel();
        JLParity = new javax.swing.JLabel();
        JPConf116 = new javax.swing.JPanel();
        JCBParity = new javax.swing.JComboBox();
        JPConf12 = new javax.swing.JPanel();
        JPConf121 = new javax.swing.JPanel();
        JLStopBits = new javax.swing.JLabel();
        JPConf122 = new javax.swing.JPanel();
        JCBStopBit = new javax.swing.JComboBox();
        JPConf123 = new javax.swing.JPanel();
        JLDataBits = new javax.swing.JLabel();
        JPConf124 = new javax.swing.JPanel();
        JCBDataBits = new javax.swing.JComboBox(this.dataBitValues);
        JPConf125 = new javax.swing.JPanel();
        JLFlowControl = new javax.swing.JLabel();
        JPConf126 = new javax.swing.JPanel();
        JCBFlowControl = new javax.swing.JComboBox();
        JPConf2 = new javax.swing.JPanel();
        JPConf21 = new javax.swing.JPanel();
        JLIPAddress = new javax.swing.JLabel();
        TFIPAddress = new javax.swing.JTextField("", 20);
        JLIPPort = new javax.swing.JLabel();
        TFIPPort = new javax.swing.JTextField("", 20);
        JPConf22 = new javax.swing.JPanel();
        CBLanguage = new javax.swing.JCheckBox();
        JCBLanguage = new javax.swing.JComboBox();
        JPConf3 = new javax.swing.JPanel();
        JPConf31 = new javax.swing.JPanel();
        JPConf32 = new javax.swing.JPanel();
        JPConf33 = new javax.swing.JPanel();
        JPConf331 = new javax.swing.JPanel();
        CBDebug = new javax.swing.JCheckBox();
        JPConf332 = new javax.swing.JPanel();
        JBDefaultSettings = new javax.swing.JButton();
        JBConfSave = new javax.swing.JButton();
        JBConfAbort = new javax.swing.JButton();
        JPDebug = new javax.swing.JPanel();
        SPDebug = new javax.swing.JScrollPane();
        TADebug = new javax.swing.JTextArea();
        MBMain = new javax.swing.JMenuBar();
        JMFile = new javax.swing.JMenu();
        MINew = new javax.swing.JMenuItem();
        MIOpen = new javax.swing.JMenuItem();
        MISave = new javax.swing.JMenuItem();
        MISaveAs = new javax.swing.JMenuItem();
        MIExit = new javax.swing.JMenuItem();
        JMAbout = new javax.swing.JMenu();
        JMIAbout = new javax.swing.JMenuItem();
        BGMetric.add(JRBMetric);
        BGMetric.add(JRBInch);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(800, 600));
        TPMain.setMaximumSize(new java.awt.Dimension(1920, 1200));
        TPMain.setMinimumSize(new java.awt.Dimension(780, 540));
        TPMain.setPreferredSize(new java.awt.Dimension(780, 540));
        JPMain.setLayout(new java.awt.BorderLayout());
        table.getSelectionModel().addListSelectionListener(this);
        SPProgram.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        JPMain.add(SPProgram, java.awt.BorderLayout.CENTER);
        JPMainNorth.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        JPMainNorth.setPreferredSize(new java.awt.Dimension(676, 230));
        JPMainNorth.setLayout(new java.awt.GridLayout(1, 3));
        JPMainNorthLeft.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("PROGRAM INFORMATION")));
        JPMainNorthLeft.setPreferredSize(new java.awt.Dimension(338, 100));
        JPMainNorthLeft.setLayout(new java.awt.GridLayout(5, 1));
        JPMainNorthLeft1.setMinimumSize(new java.awt.Dimension(230, 33));
        JPMainNorthLeft1.setPreferredSize(new java.awt.Dimension(230, 33));
        JPMainNorthLeft1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JLProgName.setText(bundle.getString("TOPIC FOR THE PROGRAM"));
        JPMainNorthLeft1.add(JLProgName);
        TFProgName.setToolTipText(bundle.getString("SET THE HEADLINE FOR THE CNC PROGRAM"));
        JPMainNorthLeft1.add(TFProgName);
        JPMainNorthLeft.add(JPMainNorthLeft1);
        JPMainNorthLeft2.setMinimumSize(new java.awt.Dimension(230, 33));
        JPMainNorthLeft2.setPreferredSize(new java.awt.Dimension(230, 33));
        JPMainNorthLeft2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JLMeasurement.setText(bundle.getString("MEASUREMENT SYSTEM"));
        JLMeasurement.setToolTipText(bundle.getString("THIS CHOICE IS ONLY AVAILABLE BEFORE ANY PROGRAM LINES WERE INSERTED"));
        JPMainNorthLeft2.add(JLMeasurement);
        JRBInch.setText(bundle.getString("IMPERIAL"));
        JRBInch.setToolTipText(bundle.getString("SET THE MEASUREMENT SYSTEM TO IMPERIAL"));
        JRBInch.addActionListener(this);
        JRBInch.setActionCommand("imperial");
        JPMainNorthLeft2.add(JRBInch);
        JRBMetric.setText(bundle.getString("METRIC"));
        JRBMetric.setToolTipText(bundle.getString("SET THE MEASUREMENT SYSTEM TO METRIC"));
        JRBMetric.addActionListener(this);
        JRBMetric.setActionCommand("metric");
        JPMainNorthLeft2.add(JRBMetric);
        JPMainNorthLeft.add(JPMainNorthLeft2);
        JPMainNorthLeft3.setPreferredSize(new java.awt.Dimension(338, 33));
        JPMainNorthLeft3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JLDescription.setText(bundle.getString("DESCRIPTION"));
        JPMainNorthLeft3.add(JLDescription);
        JPMainNorthLeft.add(JPMainNorthLeft3);
        JPMainNorthLeft4.setPreferredSize(new java.awt.Dimension(338, 33));
        JPMainNorthLeft4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JLLine.setText(bundle.getString("LINE"));
        JPMainNorthLeft4.add(JLLine);
        TFLine.setText("");
        TFLine.setToolTipText(bundle.getString("NUMBER OF THE CURRENT PROGRAM LINE"));
        TFLine.setEditable(false);
        JPMainNorthLeft4.add(TFLine);
        JLCode.setText(bundle.getString("CODE"));
        JPMainNorthLeft4.add(JLCode);
        TFCode.setEditable(false);
        TFCode.setText("");
        TFCode.setToolTipText(bundle.getString("CURRENTLY CHOSEN MACHINE PROGRAM COMMAND"));
        JPMainNorthLeft4.add(TFCode);
        JPMainNorthLeft.add(JPMainNorthLeft4);
        JPMainNorthLeft5.setPreferredSize(new java.awt.Dimension(338, 33));
        JPMainNorthLeft5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JLComment.setText(bundle.getString("COMMENT"));
        JPMainNorthLeft5.add(JLComment);
        TFComment.setText("");
        TFComment.setToolTipText(bundle.getString("ADD A LINE COMMENT"));
        TFComment.setMaximumSize(new java.awt.Dimension(20, 5));
        JPMainNorthLeft5.add(TFComment);
        JPMainNorthLeft.add(JPMainNorthLeft5);
        JPMainNorth.add(JPMainNorthLeft);
        JPMainNorthCenter.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("PROGRAM LINE ENTRY")));
        JPMainNorthCenter.setMinimumSize(new java.awt.Dimension(140, 140));
        JPMainNorthCenter.setPreferredSize(new java.awt.Dimension(338, 100));
        JPMainNorthCenter.setLayout(new java.awt.GridLayout(6, 1));
        JPMainNorthCenter1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        CBGCode.setModel(new javax.swing.DefaultComboBoxModel(gCode));
        CBGCode.setToolTipText(bundle.getString("LIST OF AVAILABLE MCOMMANDS"));
        CBGCode.setPreferredSize(new java.awt.Dimension(365, 20));
        CBGCode.setMaximumRowCount(30);
        CBGCode.addActionListener(this);
        CBGCode.setActionCommand("gcclick");
        JPMainNorthCenter1.add(CBGCode);
        JPMainNorthCenter.add(JPMainNorthCenter1);
        JPMainNorthCenter2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        CBMCode.setModel(new javax.swing.DefaultComboBoxModel(mCode));
        CBMCode.setToolTipText(bundle.getString("LIST OF AVAILABLE GCOMMANDS"));
        CBMCode.setPreferredSize(new java.awt.Dimension(365, 20));
        CBMCode.setMaximumRowCount(30);
        CBMCode.addActionListener(this);
        CBMCode.setActionCommand("mcclick");
        JPMainNorthCenter2.add(CBMCode);
        JPMainNorthCenter.add(JPMainNorthCenter2);
        JPMainNorthCenter3.setMinimumSize(new java.awt.Dimension(140, 65));
        JPMainNorthCenter3.setPreferredSize(new java.awt.Dimension(140, 65));
        JPMainNorthCenter3.setLayout(new java.awt.GridLayout(1, 2));
        JPMainNorthCenter31.setLayout(new java.awt.GridLayout(1, 2));
        JPMainNorthCenter311.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        JLX.setText("X");
        JPMainNorthCenter311.add(JLX);
        JPMainNorthCenter31.add(JPMainNorthCenter311);
        JPMainNorthCenter312.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        TFX.setText("");
        TFX.addFocusListener(this);
        JPMainNorthCenter312.add(TFX);
        TFX.getAccessibleContext().setAccessibleName("TFX");
        JPMainNorthCenter31.add(JPMainNorthCenter312);
        JPMainNorthCenter3.add(JPMainNorthCenter31);
        JPMainNorthCenter32.setLayout(new java.awt.GridLayout(1, 2));
        JPMainNorthCenter321.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        JLZ.setText("Z");
        JPMainNorthCenter321.add(JLZ);
        JPMainNorthCenter32.add(JPMainNorthCenter321);
        JPMainNorthCenter322.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        TFZ.setText("");
        TFZ.addFocusListener(this);
        JPMainNorthCenter322.add(TFZ);
        TFZ.getAccessibleContext().setAccessibleName("TFZ");
        JPMainNorthCenter32.add(JPMainNorthCenter322);
        JPMainNorthCenter3.add(JPMainNorthCenter32);
        JPMainNorthCenter.add(JPMainNorthCenter3);
        JPMainNorthCenter4.setLayout(new java.awt.GridLayout(1, 2));
        JPMainNorthCenter41.setLayout(new java.awt.GridLayout(1, 2));
        JPMainNorthCenter411.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        JLF.setText("F");
        JPMainNorthCenter411.add(JLF);
        JPMainNorthCenter41.add(JPMainNorthCenter411);
        JPMainNorthCenter412.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        TFF.setText("");
        TFF.addFocusListener(this);
        JPMainNorthCenter412.add(TFF);
        TFF.getAccessibleContext().setAccessibleName("TFF");
        JPMainNorthCenter41.add(JPMainNorthCenter412);
        JPMainNorthCenter4.add(JPMainNorthCenter41);
        JPMainNorthCenter42.setLayout(new java.awt.GridLayout(1, 2));
        JPMainNorthCenter421.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        JLH.setText("H");
        JPMainNorthCenter421.add(JLH);
        JPMainNorthCenter42.add(JPMainNorthCenter421);
        JPMainNorthCenter422.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        TFH.setText("");
        TFH.addFocusListener(this);
        JPMainNorthCenter422.add(TFH);
        TFH.getAccessibleContext().setAccessibleName("TFH");
        JPMainNorthCenter42.add(JPMainNorthCenter422);
        JPMainNorthCenter4.add(JPMainNorthCenter42);
        JPMainNorthCenter.add(JPMainNorthCenter4);
        JPMainNorthCenter5.setLayout(new java.awt.GridLayout(1, 2));
        JBAdd.setText(bundle.getString("APPEND"));
        JBAdd.setToolTipText(bundle.getString("ADD THIS LINE AFTER THE LAST LINE IN THE PROGRAM"));
        JBAdd.setMaximumSize(new java.awt.Dimension(130, 30));
        JBAdd.setMinimumSize(new java.awt.Dimension(50, 20));
        JBAdd.setPreferredSize(new java.awt.Dimension(130, 20));
        JBAdd.addActionListener(this);
        JBAdd.setActionCommand("add");
        JBAdd.setEnabled(false);
        JPMainNorthCenter51.add(JBAdd);
        JPMainNorthCenter5.add(JPMainNorthCenter51);
        JBDelete.setText(bundle.getString("DELETE"));
        JBDelete.setToolTipText(bundle.getString("DELETE THE CURRENTLY SELECTED LINE"));
        JBDelete.setMaximumSize(new java.awt.Dimension(130, 30));
        JBDelete.setMinimumSize(new java.awt.Dimension(50, 20));
        JBDelete.setPreferredSize(new java.awt.Dimension(130, 20));
        JBDelete.addActionListener(this);
        JBDelete.setActionCommand("delete");
        JBDelete.setEnabled(false);
        JPMainNorthCenter52.add(JBDelete);
        JPMainNorthCenter5.add(JPMainNorthCenter52);
        JPMainNorthCenter.add(JPMainNorthCenter5);
        JPMainNorthCenter6.setPreferredSize(new java.awt.Dimension(140, 45));
        JPMainNorthCenter6.setLayout(new java.awt.GridLayout(1, 2));
        JBInsert.setText(bundle.getString("INSERT"));
        JBInsert.setToolTipText(bundle.getString("INSERT THE LINE AT THE CURRENT POSITION"));
        JBInsert.setMaximumSize(new java.awt.Dimension(130, 30));
        JBInsert.setMinimumSize(new java.awt.Dimension(50, 20));
        JBInsert.setPreferredSize(new java.awt.Dimension(130, 20));
        JBInsert.addActionListener(this);
        JBInsert.setActionCommand("insert");
        JBInsert.setEnabled(false);
        JPMainNorthCenter61.add(JBInsert);
        JPMainNorthCenter6.add(JPMainNorthCenter61);
        JBUpdate.setText(bundle.getString("UPDATE"));
        JBUpdate.setToolTipText(bundle.getString("CHANGE THE CURRENT LINE"));
        JBUpdate.setActionCommand("update");
        JBUpdate.setMaximumSize(new java.awt.Dimension(130, 30));
        JBUpdate.setMinimumSize(new java.awt.Dimension(50, 20));
        JBUpdate.setPreferredSize(new java.awt.Dimension(130, 20));
        JBUpdate.addActionListener(this);
        JBUpdate.setEnabled(false);
        JPMainNorthCenter62.add(JBUpdate);
        JBUpdate.getAccessibleContext().setAccessibleName("");
        JPMainNorthCenter6.add(JPMainNorthCenter62);
        JPMainNorthCenter.add(JPMainNorthCenter6);
        JPMainNorth.add(JPMainNorthCenter);
        JPMain.add(JPMainNorth, java.awt.BorderLayout.PAGE_START);
        TPMain.addTab(bundle.getString("MAIN WINDOW"), JPMain);
        JPTrans.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        JPTrans.setLayout(new java.awt.BorderLayout());
        JPTransCenter.setLayout(new java.awt.BorderLayout());
        TARxTx.setColumns(20);
        TARxTx.setRows(5);
        TARxTx.setEditable(false);
        SPRxTx.setViewportView(TARxTx);
        JPTransCenter.add(SPRxTx, java.awt.BorderLayout.CENTER);
        JPTrans.add(JPTransCenter, java.awt.BorderLayout.CENTER);
        JPTransRight.setLayout(new java.awt.GridLayout(10, 2));
        JPTransRight.add(JPTransRight1);
        JPTransRight.add(JPTransRight2);
        JPTransRight3.setPreferredSize(new java.awt.Dimension(182, 50));
        JPTransRight3.setLayout(new java.awt.GridLayout(2, 1));
        JRBEthernet.setText(bundle.getString("ETHERNET"));
        JRBEthernet.setToolTipText(bundle.getString("TRANSMIT RECEIVE OVER ETHERNET"));
        BGTrans.add(JRBEthernet);
        JRBEthernet.setActionCommand("useeth");
        JRBEthernet.setSelected(true);
        javax.swing.GroupLayout JPTransRight3aLayout = new javax.swing.GroupLayout(JPTransRight3a);
        JPTransRight3a.setLayout(JPTransRight3aLayout);
        JPTransRight3aLayout.setHorizontalGroup(JPTransRight3aLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 201, Short.MAX_VALUE).addGroup(JPTransRight3aLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(JPTransRight3aLayout.createSequentialGroup().addGap(0, 50, Short.MAX_VALUE).addComponent(JRBEthernet, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(0, 51, Short.MAX_VALUE))));
        JPTransRight3aLayout.setVerticalGroup(JPTransRight3aLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 56, Short.MAX_VALUE).addGroup(JPTransRight3aLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(JPTransRight3aLayout.createSequentialGroup().addGap(0, 16, Short.MAX_VALUE).addComponent(JRBEthernet).addGap(0, 17, Short.MAX_VALUE))));
        JPTransRight3.add(JPTransRight3a);
        JRBSerial.setText(bundle.getString("SERIAL"));
        JRBSerial.setToolTipText(bundle.getString("TRANSMIT RECEIVE OVER SERIAL CONNECTION"));
        BGTrans.add(JRBSerial);
        JRBSerial.setActionCommand("useserial");
        javax.swing.GroupLayout JPTransRight3bLayout = new javax.swing.GroupLayout(JPTransRight3b);
        JPTransRight3b.setLayout(JPTransRight3bLayout);
        JPTransRight3bLayout.setHorizontalGroup(JPTransRight3bLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 201, Short.MAX_VALUE).addGroup(JPTransRight3bLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(JPTransRight3bLayout.createSequentialGroup().addGap(0, 50, Short.MAX_VALUE).addComponent(JRBSerial, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(0, 51, Short.MAX_VALUE))));
        JPTransRight3bLayout.setVerticalGroup(JPTransRight3bLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 56, Short.MAX_VALUE).addGroup(JPTransRight3bLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(JPTransRight3bLayout.createSequentialGroup().addGap(0, 16, Short.MAX_VALUE).addComponent(JRBSerial).addGap(0, 17, Short.MAX_VALUE))));
        JPTransRight3.add(JPTransRight3b);
        JPTransRight.add(JPTransRight3);
        JPTransRight4.setPreferredSize(new java.awt.Dimension(182, 50));
        JPTransRight4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        CBXport.setText(bundle.getString("XPORT LINUX OS IN USE"));
        CBXport.setToolTipText(bundle.getString("WHEN YOU ARE GOING TO USE A XPORT"));
        CBXport.setActionCommand("setxport");
        CBXport.addActionListener(this);
        CBXport.setSelected(c.getUseXPort());
        JPTransRight4.add(CBXport);
        JPTransRight.add(JPTransRight4);
        JPTransRight.add(JPTransRight5);
        JPTransRight.add(JPTransRight6);
        JBTransmit.setText(bundle.getString("TRANSMIT"));
        JBTransmit.setActionCommand("transmit");
        JBTransmit.addActionListener(this);
        JPTransRight7.add(JBTransmit);
        JPTransRight.add(JPTransRight7);
        JPTransRight8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JBReceive.setText(bundle.getString("RECEIVE"));
        JBReceive.setActionCommand("receive");
        JBReceive.addActionListener(this);
        JPTransRight8.add(JBReceive);
        JPTransRight.add(JPTransRight8);
        JBTransAbort.setText(bundle.getString("ABORT"));
        JBTransAbort.setToolTipText(bundle.getString("ABORTS RECEIVING TRANSMITTING"));
        JBTransAbort.setActionCommand("rxtxabort");
        JBTransAbort.addActionListener(this);
        JPTransRight9.add(JBTransAbort);
        JPTransRight.add(JPTransRight9);
        JPTransRight.add(JPTransRight10);
        javax.swing.GroupLayout JPTransRight11Layout = new javax.swing.GroupLayout(JPTransRight11);
        JPTransRight11.setLayout(JPTransRight11Layout);
        JPTransRight11Layout.setHorizontalGroup(JPTransRight11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 201, Short.MAX_VALUE));
        JPTransRight11Layout.setVerticalGroup(JPTransRight11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 112, Short.MAX_VALUE));
        JPTransRight.add(JPTransRight11);
        javax.swing.GroupLayout JPTransRight12Layout = new javax.swing.GroupLayout(JPTransRight12);
        JPTransRight12.setLayout(JPTransRight12Layout);
        JPTransRight12Layout.setHorizontalGroup(JPTransRight12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 201, Short.MAX_VALUE));
        JPTransRight12Layout.setVerticalGroup(JPTransRight12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 112, Short.MAX_VALUE));
        JPTransRight.add(JPTransRight12);
        javax.swing.GroupLayout JPTransRight13Layout = new javax.swing.GroupLayout(JPTransRight13);
        JPTransRight13.setLayout(JPTransRight13Layout);
        JPTransRight13Layout.setHorizontalGroup(JPTransRight13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 201, Short.MAX_VALUE));
        JPTransRight13Layout.setVerticalGroup(JPTransRight13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 112, Short.MAX_VALUE));
        JPTransRight.add(JPTransRight13);
        javax.swing.GroupLayout JPTransRight14Layout = new javax.swing.GroupLayout(JPTransRight14);
        JPTransRight14.setLayout(JPTransRight14Layout);
        JPTransRight14Layout.setHorizontalGroup(JPTransRight14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 201, Short.MAX_VALUE));
        JPTransRight14Layout.setVerticalGroup(JPTransRight14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 112, Short.MAX_VALUE));
        JPTransRight.add(JPTransRight14);
        javax.swing.GroupLayout JPTransRight15Layout = new javax.swing.GroupLayout(JPTransRight15);
        JPTransRight15.setLayout(JPTransRight15Layout);
        JPTransRight15Layout.setHorizontalGroup(JPTransRight15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 201, Short.MAX_VALUE));
        JPTransRight15Layout.setVerticalGroup(JPTransRight15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 112, Short.MAX_VALUE));
        JPTransRight.add(JPTransRight15);
        javax.swing.GroupLayout JPTransRight16Layout = new javax.swing.GroupLayout(JPTransRight16);
        JPTransRight16.setLayout(JPTransRight16Layout);
        JPTransRight16Layout.setHorizontalGroup(JPTransRight16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 201, Short.MAX_VALUE));
        JPTransRight16Layout.setVerticalGroup(JPTransRight16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 112, Short.MAX_VALUE));
        JPTransRight.add(JPTransRight16);
        javax.swing.GroupLayout JPTransRight17Layout = new javax.swing.GroupLayout(JPTransRight17);
        JPTransRight17.setLayout(JPTransRight17Layout);
        JPTransRight17Layout.setHorizontalGroup(JPTransRight17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 201, Short.MAX_VALUE));
        JPTransRight17Layout.setVerticalGroup(JPTransRight17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 112, Short.MAX_VALUE));
        JPTransRight.add(JPTransRight17);
        javax.swing.GroupLayout JPTransRight18Layout = new javax.swing.GroupLayout(JPTransRight18);
        JPTransRight18.setLayout(JPTransRight18Layout);
        JPTransRight18Layout.setHorizontalGroup(JPTransRight18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 201, Short.MAX_VALUE));
        JPTransRight18Layout.setVerticalGroup(JPTransRight18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 112, Short.MAX_VALUE));
        JPTransRight.add(JPTransRight18);
        javax.swing.GroupLayout JPTransRight19Layout = new javax.swing.GroupLayout(JPTransRight19);
        JPTransRight19.setLayout(JPTransRight19Layout);
        JPTransRight19Layout.setHorizontalGroup(JPTransRight19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 201, Short.MAX_VALUE));
        JPTransRight19Layout.setVerticalGroup(JPTransRight19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 112, Short.MAX_VALUE));
        JPTransRight.add(JPTransRight19);
        javax.swing.GroupLayout JPTransRight20Layout = new javax.swing.GroupLayout(JPTransRight20);
        JPTransRight20.setLayout(JPTransRight20Layout);
        JPTransRight20Layout.setHorizontalGroup(JPTransRight20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 201, Short.MAX_VALUE));
        JPTransRight20Layout.setVerticalGroup(JPTransRight20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 112, Short.MAX_VALUE));
        JPTransRight.add(JPTransRight20);
        JPTrans.add(JPTransRight, java.awt.BorderLayout.LINE_END);
        TPMain.addTab(bundle.getString("TRANSMIT RECEIVE"), JPTrans);
        JPConf.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        JPConf.setLayout(new java.awt.GridLayout(3, 1));
        JPConf1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("SERIAL PORT SETTINGS")));
        JPConf1.setLayout(new java.awt.GridLayout(2, 0));
        JPConf11.setLayout(new java.awt.GridLayout(1, 6));
        JPConf111.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JLSerialInterface.setText(bundle.getString("SERIAL PORT"));
        JPConf111.add(JLSerialInterface);
        JPConf11.add(JPConf111);
        JPConf112.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JCBSerialInterface.setModel(new javax.swing.DefaultComboBoxModel(this.comPorts));
        JPConf112.add(JCBSerialInterface);
        JPConf11.add(JPConf112);
        JPConf113.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JLBaudRate.setText(bundle.getString("BAUD RATE"));
        JPConf113.add(JLBaudRate);
        JPConf11.add(JPConf113);
        JPConf114.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JCBBaudRate.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "110", "300", "1200", "2400", "4800", "9600", "19200", "38400", "57600", "115200", "230400", "460800", "921600" }));
        JPConf114.add(JCBBaudRate);
        JPConf11.add(JPConf114);
        JPConf115.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JLParity.setText(bundle.getString("PARITY"));
        JPConf115.add(JLParity);
        JPConf11.add(JPConf115);
        JPConf116.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JCBParity.setModel(new javax.swing.DefaultComboBoxModel(parityText));
        JPConf116.add(JCBParity);
        JPConf11.add(JPConf116);
        JPConf1.add(JPConf11);
        JPConf12.setLayout(new java.awt.GridLayout(1, 6));
        JPConf121.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JLStopBits.setText(bundle.getString("STOP BITS"));
        JPConf121.add(JLStopBits);
        JPConf12.add(JPConf121);
        JPConf122.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JCBStopBit.setModel(new javax.swing.DefaultComboBoxModel(this.stopBitText));
        JPConf122.add(JCBStopBit);
        JPConf12.add(JPConf122);
        JPConf123.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JLDataBits.setText(bundle.getString("DATA BITS"));
        JPConf123.add(JLDataBits);
        JPConf12.add(JPConf123);
        JPConf124.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JPConf124.add(JCBDataBits);
        JPConf12.add(JPConf124);
        JPConf125.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JLFlowControl.setText(bundle.getString("FLOW CONTROL"));
        JPConf125.add(JLFlowControl);
        JPConf12.add(JPConf125);
        JPConf126.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JCBFlowControl.setModel(new javax.swing.DefaultComboBoxModel(this.flowControlText));
        JPConf126.add(JCBFlowControl);
        JPConf12.add(JPConf126);
        JPConf1.add(JPConf12);
        JPConf.add(JPConf1);
        JPConf2.setLayout(new java.awt.GridLayout(2, 0));
        JPConf21.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("NETWORK SETTINGS")));
        JPConf21.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JLIPAddress.setText(bundle.getString("REMOTE HOST IP ADDRESS"));
        JPConf21.add(JLIPAddress);
        TFIPAddress.setText(c.getIp());
        TFIPAddress.setToolTipText(bundle.getString("ENTER THE IP ADDRESS OF THE REMOTE HOST"));
        JPConf21.add(TFIPAddress);
        JLIPPort.setText(bundle.getString("REMOTE LOCAL PORT"));
        JPConf21.add(JLIPPort);
        TFIPPort.setText("" + c.getPort());
        TFIPPort.setToolTipText(bundle.getString("ENTER THE TCP-PORT USED (AT THIS HOST AND AT THE REMOTE HOST)"));
        JPConf21.add(TFIPPort);
        JPConf2.add(JPConf21);
        JPConf22.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("LANGUAGE SETTINGS")));
        JPConf22.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        CBLanguage.setText(bundle.getString("USE SYSTEM DEFAULT"));
        CBLanguage.setToolTipText(bundle.getString("TRY TO LET THE SOFTWARE CHOOSE THE LANGUAGE FOR YOU BASED ON YOUR SYSTEM SETTINGS"));
        CBLanguage.setActionCommand("defaultlanguage");
        CBLanguage.addActionListener(this);
        JPConf22.add(CBLanguage);
        JCBLanguage.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Deutsch", "English", "Indonesia" }));
        JCBLanguage.setToolTipText(bundle.getString("CHOOSE YOUR PREFERED LANGUAGE MANUALLY"));
        JPConf22.add(JCBLanguage);
        JPConf2.add(JPConf22);
        JPConf.add(JPConf2);
        JPConf3.setLayout(new java.awt.GridLayout(3, 0));
        javax.swing.GroupLayout JPConf31Layout = new javax.swing.GroupLayout(JPConf31);
        JPConf31.setLayout(JPConf31Layout);
        JPConf31Layout.setHorizontalGroup(JPConf31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 1249, Short.MAX_VALUE));
        JPConf31Layout.setVerticalGroup(JPConf31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 124, Short.MAX_VALUE));
        JPConf3.add(JPConf31);
        javax.swing.GroupLayout JPConf32Layout = new javax.swing.GroupLayout(JPConf32);
        JPConf32.setLayout(JPConf32Layout);
        JPConf32Layout.setHorizontalGroup(JPConf32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 1249, Short.MAX_VALUE));
        JPConf32Layout.setVerticalGroup(JPConf32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 124, Short.MAX_VALUE));
        JPConf3.add(JPConf32);
        JPConf33.setLayout(new java.awt.BorderLayout());
        JPConf331.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        CBDebug.setText(bundle.getString("SHOW THE DEBUG OUTPUT TAB"));
        CBDebug.setToolTipText(bundle.getString("SHOWS AN ADDITONAL TAB WITH DEBUGGING INFORMATION"));
        CBDebug.setActionCommand("usedebug");
        CBDebug.addActionListener(this);
        JPConf331.add(CBDebug);
        JPConf33.add(JPConf331, java.awt.BorderLayout.LINE_START);
        JPConf332.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        JBDefaultSettings.setText(bundle.getString("RESTORE DEFAULT SETTINGS"));
        JBDefaultSettings.setToolTipText(bundle.getString("SET THE CONFIGURATION BACK TO FACTURY DEFAULTS"));
        JBDefaultSettings.setActionCommand("defaultsettings");
        JBDefaultSettings.addActionListener(this);
        JPConf332.add(JBDefaultSettings);
        JBConfSave.setText(bundle.getString("APPLY SETTINGS"));
        JBConfSave.setToolTipText(bundle.getString("APPLY AND SAVE THE CONFIGURATION"));
        JBConfSave.setActionCommand("confsave");
        JBConfSave.addActionListener(this);
        JPConf332.add(JBConfSave);
        JBConfAbort.setText(bundle.getString("ABORT"));
        JBConfAbort.setToolTipText(bundle.getString("RESET THE CONFIGURATION TO THE LAST SAVED SETTINGS"));
        JBConfAbort.setActionCommand("configabort");
        JBConfAbort.addActionListener(this);
        JPConf332.add(JBConfAbort);
        JPConf33.add(JPConf332, java.awt.BorderLayout.LINE_END);
        JPConf3.add(JPConf33);
        JPConf.add(JPConf3);
        TPMain.addTab(bundle.getString("CONFIGURATION"), JPConf);
        JPDebug.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        JPDebug.setLayout(new java.awt.BorderLayout());
        TADebug.setColumns(20);
        TADebug.setRows(5);
        SPDebug.setViewportView(TADebug);
        JPDebug.add(SPDebug, java.awt.BorderLayout.CENTER);
        TPMain.addTab(bundle.getString("DEBUGGING OUTPUT"), JPDebug);
        getContentPane().add(TPMain, java.awt.BorderLayout.CENTER);
        JMFile.setText(bundle.getString("FILE"));
        JMFile.setActionCommand("file");
        JMFile.addActionListener(this);
        MINew.setText(bundle.getString("NEW FILE"));
        MINew.setToolTipText(bundle.getString("CREATE A NEW FILE"));
        MINew.setActionCommand("new");
        MINew.addActionListener(this);
        JMFile.add(MINew);
        MIOpen.setText(bundle.getString("OPEN FILE"));
        MIOpen.setToolTipText(bundle.getString("OPEN AN EXISTING FILE"));
        MIOpen.setActionCommand("open");
        MIOpen.addActionListener(this);
        JMFile.add(MIOpen);
        MISave.setText(bundle.getString("SAVE"));
        MISave.setToolTipText(bundle.getString("SAVE CURRENT FILE"));
        MISave.setActionCommand("save");
        MISave.addActionListener(this);
        JMFile.add(MISave);
        MISaveAs.setText(bundle.getString("SAVE AS ..."));
        MISaveAs.setToolTipText(bundle.getString("SAVE AS A NEW FILE"));
        MISaveAs.setActionCommand("saveas");
        MISaveAs.addActionListener(this);
        JMFile.add(MISaveAs);
        MIExit.setText(bundle.getString("EXIT"));
        MIExit.setToolTipText(bundle.getString("EXIT THIS PROGRAM"));
        MIExit.setActionCommand("exit");
        MIExit.addActionListener(this);
        JMFile.add(MIExit);
        MBMain.add(JMFile);
        JMAbout.setText(bundle.getString("ABOUT"));
        JMAbout.setToolTipText(bundle.getString("INFORMATION ABOUT THIS SOFTWARE"));
        JMAbout.setActionCommand("aboutmenu");
        JMIAbout.setText(bundle.getString("ABOUT THIS SOFTWARE"));
        JMIAbout.addActionListener(this);
        JMIAbout.setActionCommand("about");
        JMAbout.add(JMIAbout);
        MBMain.add(JMAbout);
        setJMenuBar(MBMain);
        pack();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                System.out.println(Locale.getDefault());
                System.out.println(Locale.getDefault());
                new MainFrame().setVisible(true);
            }
        });
    }

    public void getComPorts() {
        debug += "\n=======================================" + "\ngetComPorts()\n";
        String temp[] = new String[99];
        Enumeration portList;
        CommPortIdentifier portId;
        portList = CommPortIdentifier.getPortIdentifiers();
        int foundPorts = 0;
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                temp[foundPorts] = portId.getName();
                foundPorts++;
            }
        }
        if (foundPorts > 0) {
            debug += "\nSerial ports found: ";
            comPorts = new String[foundPorts];
            for (int i = 0; i < foundPorts; i++) {
                comPorts[i] = temp[i];
                debug += "\n" + comPorts[i];
            }
        } else {
            comPorts = new String[1];
            comPorts[0] = "COM1";
            debug += "\nNo serial port found!";
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("NO SERIAL INTERFACE FOUND!"), java.util.ResourceBundle.getBundle("MainFrameBundle").getString("SEND PROGRAMS OVER SERIAL CONNECTION WILL NOT BE ACCESSABLE!"), JOptionPane.WARNING_MESSAGE);
        }
        temp = null;
        debug += "\n=======================================";
    }

    public void getDefaultMachineCode() {
        debug += "\n=======================================" + "\ngetDefaultMachineCode()\n";
        gCode.add(new ProgramCode("00", -1999, 1999, -12900, 12900, 0, 0, 0, 0, -5999, 5999, -32760, 32760, 0, 0, 0, 0, " ", " ", " ", " ", "G00", "X", "Z", " ", " ", true, true, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("RAPID TRAVERSE")));
        gCode.add(new ProgramCode("01", -1999, 1999, -12900, 12900, 2, 199, 0, 0, -5999, 5999, -32760, 32760, 2, 499, 0, 0, " ", " ", " ", " ", "G01", "X", "Z", "F", " ", true, true, true, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("LINEAR INTERPOLATION")));
        gCode.add(new ProgramCode("02", -1999, 1999, -12900, 12900, 2, 199, 0, 0, -5999, 5999, -32760, 32760, 2, 499, 0, 0, " ", " ", " ", " ", "G02", "X", "Z", "F", " ", true, true, true, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("CW CIRCULAR INTERPOLATION (2-D)")));
        gCode.add(new ProgramCode("03", -1999, 1999, -12900, 12900, 2, 199, 0, 0, -5999, 5999, -32760, 32760, 2, 499, 0, 0, " ", " ", " ", " ", "G03", "X", "Z", "F", " ", true, true, true, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("CCW CIRCULAR INTERPOLATION (2-D)")));
        gCode.add(new ProgramCode("04", 0, 5999, 0, 0, 0, 0, 0, 0, " ", " ", " ", " ", "G04", "Time", " ", " ", " ", true, false, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("DWELL")));
        gCode.add(new ProgramCode("21", 0, 0, 0, 0, 0, 0, 0, 0, " ", " ", " ", " ", "G21", " ", " ", " ", " ", false, false, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("EMPTY LINE")));
        gCode.add(new ProgramCode("24", 0, 0, 0, 0, 0, 0, 0, 0, " ", " ", " ", " ", "G24", " ", " ", " ", " ", false, false, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("RADIUS PROGRAMMING")));
        gCode.add(new ProgramCode("25", 0, 0, 0, 0, 0, 221, 0, 0, " ", " ", "L", " ", "G25", " ", " ", "Line", " ", false, false, true, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("SUB-ROUTINE CALL-UP")));
        gCode.add(new ProgramCode("27", 0, 0, 0, 0, 0, 221, 0, 0, " ", " ", "L", " ", "G27", " ", " ", "Line", " ", false, false, true, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("JUMP INSTRUCTION")));
        gCode.add(new ProgramCode("33", 0, 0, -12900, 12900, 2, 199, 0, 0, 0, 0, -32760, 32760, 2, 499, 0, 0, " ", " ", "K", " ", "G33", " ", "Z", "K", " ", false, true, true, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("THREADING WITH CONSTANT PITCH")));
        gCode.add(new ProgramCode("64", 0, 0, 0, 0, 0, 0, 0, 0, " ", " ", " ", " ", "G64", " ", " ", " ", " ", false, false, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("FEED MOTORS CURRENTLESS")));
        gCode.add(new ProgramCode("73", 0, 0, -12900, 12900, 2, 199, 0, 0, 0, 0, -32760, 32760, 2, 499, 0, 0, " ", " ", " ", " ", "G73", " ", "Z", "F", " ", false, true, true, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("CHIP BREAKAGE CYCLE")));
        gCode.add(new ProgramCode("78", -1999, 1999, -12900, 12900, 2, 199, 0, 999, -5999, 5999, -32760, 32760, 2, 499, 0, 999, " ", " ", "K", " ", "G78", "X", "Z", "K", "H", true, true, true, true, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("THREADING CYCLE")));
        gCode.add(new ProgramCode("81", 0, 0, -12900, 12900, 2, 199, 0, 0, 0, 0, -32760, 32760, 2, 499, 0, 0, " ", " ", " ", " ", "G81", " ", "Z", "F", " ", false, true, true, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("DRILLING CYCLE")));
        gCode.add(new ProgramCode("82", 0, 0, -12900, 12900, 2, 199, 0, 0, 0, 0, -32760, 32760, 2, 499, 0, 0, " ", " ", " ", " ", "G82", " ", "Z", "F", " ", false, true, true, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("DRILLING CYCLE WITH DWELL")));
        gCode.add(new ProgramCode("83", 0, 0, -12900, 12900, 2, 199, 0, 0, 0, 0, -32760, 32760, 2, 499, 0, 0, " ", " ", " ", " ", "G83", " ", "Z", "F", " ", false, true, true, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("DRILLING CYCLE, DEEP HOLE WITH WITHDRAWAL")));
        gCode.add(new ProgramCode("84", -1999, 1999, -12900, 12900, 2, 199, 0, 999, -5999, 5999, -32760, 32760, 2, 499, 0, 999, " ", " ", " ", " ", "G84", "X", "Z", "F", "H", true, true, true, true, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("LONGITUDINAL TURNING")));
        gCode.add(new ProgramCode("85", 0, 0, -12900, 12900, 2, 199, 0, 0, 0, 0, -32760, 32760, 2, 499, 0, 0, " ", " ", " ", " ", "G85", " ", "Z", "F", " ", false, true, true, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("REAMING CYCLE")));
        gCode.add(new ProgramCode("86", -1999, 1999, -12900, 12900, 2, 199, 10, 999, -5999, 5999, -32760, 32760, 2, 499, 10, 999, " ", " ", " ", " ", "G86", "X", "Z", "F", "H", true, true, true, true, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("GROOVING WITH DIVISION OF CUT (PARAMETER H)")));
        gCode.add(new ProgramCode("88", -1999, 1999, -12900, 12900, 2, 199, 0, 999, -5999, 5999, -32760, 32760, 2, 499, 0, 999, " ", " ", " ", " ", "G88", "X", "Z", "F", "H", true, true, true, true, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("FACING WITH DIVISION OF CUT")));
        gCode.add(new ProgramCode("89", 0, 0, -12900, 12900, 2, 199, 0, 0, 0, 0, -32760, 32760, 2, 499, 0, 0, " ", " ", " ", " ", "G89", " ", "Z", "F", " ", false, true, true, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("REAMING AND DRILLING WITH DWELL")));
        gCode.add(new ProgramCode("90", 0, 0, 0, 0, 0, 0, 0, 0, " ", " ", " ", " ", "G90", " ", " ", " ", " ", false, false, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("ABSOLUTE MODE CANCELED ONLY BY G91")));
        gCode.add(new ProgramCode("91", 0, 0, 0, 0, 0, 0, 0, 0, " ", " ", " ", " ", "G91", " ", " ", " ", " ", false, false, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("INCREMENTAL MODE CANCELED ONLY BY G90 OR G92")));
        gCode.add(new ProgramCode("92", -1999, 1999, -12900, 12900, 0, 0, 0, 0, -5999, 5999, -32760, 32760, 0, 0, 0, 0, " ", " ", " ", " ", "G92", "X", "Z", " ", " ", true, true, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("SET REGISTER (ZERO POINT OFFSET) ABSOLUTE MODE")));
        gCode.add(new ProgramCode("94", 0, 0, 0, 0, 0, 0, 0, 0, " ", " ", " ", " ", "G94", " ", " ", " ", " ", false, false, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("FEED IN MM/MIN (OR IN/MIN)")));
        gCode.add(new ProgramCode("95", 0, 0, 0, 0, 0, 0, 0, 0, " ", " ", " ", " ", "G95", " ", " ", " ", " ", false, false, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("FEED IN MM/REV (OR IN/REV) ")));
        mCode.add(new ProgramCode("M00", 0, 0, 0, 0, 0, 0, 0, 0, " ", " ", " ", " ", "M00", " ", " ", " ", " ", false, false, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("PROGRAMMED STOP (PAUSE)")));
        mCode.add(new ProgramCode("M03", 0, 0, 0, 0, 0, 0, 0, 0, " ", " ", " ", " ", "M03", " ", " ", " ", " ", false, false, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("SPINDLE ON, CW")));
        mCode.add(new ProgramCode("M05", 0, 0, 0, 0, 0, 0, 0, 0, " ", " ", " ", " ", "M05", " ", " ", " ", " ", false, false, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("SPINDLE OFF")));
        mCode.add(new ProgramCode("M06", 0, 100, 0, 100, 0, 499, 0, 0, 0, 100, 0, 100, 0, 499, 0, 0, " ", " ", "T", " ", "M06", "X", "Z", "Tool", " ", true, true, true, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("TOOL LENGTH COMPENSATION")));
        mCode.add(new ProgramCode("M08", 0, 0, 0, 0, 0, 0, 0, 0, " ", " ", " ", " ", "M08", " ", " ", " ", " ", false, false, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("SWITCH EXIT X62 PIN 15 HIGH")));
        mCode.add(new ProgramCode("M09", 0, 0, 0, 0, 0, 0, 0, 0, " ", " ", " ", " ", "M09", " ", " ", " ", " ", false, false, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("SWITCH EXIT X62 PIN 15 LOW")));
        mCode.add(new ProgramCode("M17", 0, 0, 0, 0, 0, 0, 0, 0, " ", " ", " ", " ", "M17", " ", " ", " ", " ", false, false, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("RETURN COMMAND TO THE MAIN PROGRAM")));
        mCode.add(new ProgramCode("M22", 0, 0, 0, 0, 0, 0, 0, 0, " ", " ", " ", " ", "M22", " ", " ", " ", " ", false, false, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("SWITCH EXIT X62 PIN 18 HIGH")));
        mCode.add(new ProgramCode("M23", 0, 0, 0, 0, 0, 0, 0, 0, " ", " ", " ", " ", "M23", " ", " ", " ", " ", false, false, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("SWITCH EXIT X62 PIN 18 LOW")));
        mCode.add(new ProgramCode("M26", 0, 0, 0, 0, 0, 0, 0, 0, " ", " ", " ", " ", "M26", " ", " ", " ", " ", false, false, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("SWITCH EXIT X62 PIN 20")));
        mCode.add(new ProgramCode("M30", 0, 0, 0, 0, 0, 0, 0, 0, " ", " ", " ", " ", "M30", " ", " ", " ", " ", false, false, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("END OF PROGRAM (MUST BE IN PROGRAM!!!)")));
        mCode.add(new ProgramCode("M98", 0, 100, 0, 100, 0, 0, 0, 0, 0, 100, 0, 100, 0, 100, 0, 0, " ", " ", " ", " ", "M98", "X", "Z", " ", " ", true, true, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("AUTOMATIC COMPENSATION OF PLAY")));
        mCode.add(new ProgramCode("M99", 0, 1999, 0, 12900, 0, 0, 0, 0, 0, 5999, 0, 5999, 0, 32760, 0, 0, "I", "K", " ", " ", "M99", "I", "K", " ", " ", true, true, false, false, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("CIRCLE PARAMETER")));
    }

    public void UpdatePanel(Object o, boolean fromList) {
        this.TADebug.append("\n=======================================" + "\nUpdatePanel()");
        clearPanel();
        if (fromList) {
            this.TFX.setText(((EnteredCode) o).getX());
            this.TFZ.setText(((EnteredCode) o).getZ());
            this.TFF.setText(((EnteredCode) o).getF());
            this.TFH.setText(((EnteredCode) o).getH());
            this.TFComment.setText(((EnteredCode) o).getUserComment());
            this.TFLine.setText("" + this.position);
            JBDelete.setEnabled(true);
            JBInsert.setEnabled(true);
            JBUpdate.setEnabled(true);
        } else {
            TFLine.setText("");
        }
        this.tmpProgram = ((ProgramCode) o);
        JLDescription.setText(tmpProgram.getLabelText() + ": " + tmpProgram.getComment());
        TFCode.setText(tmpProgram.getLabelText());
        JLX.setVisible(tmpProgram.getShowX());
        TFX.setVisible(tmpProgram.getShowX());
        JLZ.setVisible(tmpProgram.getShowZ());
        TFZ.setVisible(tmpProgram.getShowZ());
        JLF.setVisible(tmpProgram.getShowF());
        TFF.setVisible(tmpProgram.getShowF());
        JLH.setVisible(tmpProgram.getShowH());
        TFH.setVisible(tmpProgram.getShowH());
        int xMin, xMax, zMin, zMax, fMin, fMax, hMin, hMax;
        if (c.getUseMetric()) {
            JRBMetric.setSelected(true);
            xMin = tmpProgram.getMinXmetric();
            xMax = tmpProgram.getMaxXmetric();
            zMin = tmpProgram.getMinZmetric();
            zMax = tmpProgram.getMaxZmetric();
            fMin = tmpProgram.getMinFmetric();
            fMax = tmpProgram.getMaxFmetric();
            hMin = tmpProgram.getMinHmetric();
            hMax = tmpProgram.getMaxHmetric();
        } else {
            JRBInch.setSelected(true);
            xMin = tmpProgram.getMinX();
            xMax = tmpProgram.getMaxX();
            zMin = tmpProgram.getMinZ();
            zMax = tmpProgram.getMaxZ();
            fMin = tmpProgram.getMinF();
            fMax = tmpProgram.getMaxF();
            hMin = tmpProgram.getMinH();
            hMax = tmpProgram.getMaxH();
        }
        if (tmpProgram.getShowX()) {
            JLX.setText(tmpProgram.getXlText());
            TFX.setToolTipText(java.util.ResourceBundle.getBundle("MainFrameBundle").getString("VALUE FOR ") + JLX.getText() + ": " + xMin + " - " + xMax);
        }
        if (tmpProgram.getShowZ()) {
            JLZ.setText(tmpProgram.getZlText());
            TFZ.setToolTipText(java.util.ResourceBundle.getBundle("MainFrameBundle").getString("VALUE FOR ") + JLZ.getText() + ": " + zMin + " - " + zMax);
        }
        if (tmpProgram.getShowF()) {
            JLF.setText(tmpProgram.getFlText());
            TFF.setToolTipText(java.util.ResourceBundle.getBundle("MainFrameBundle").getString("VALUE FOR ") + JLF.getText() + ": " + fMin + " - " + fMax);
        }
        if (tmpProgram.getShowH()) {
            JLH.setText(tmpProgram.getHlText());
            TFH.setToolTipText(java.util.ResourceBundle.getBundle("MainFrameBundle").getString("VALUE FOR ") + JLH.getText() + ": " + hMin + " - " + hMax);
        }
        if (tabelle.enteredCode.size() > 0) {
            JRBMetric.setEnabled(false);
            JRBInch.setEnabled(false);
        } else {
            JRBMetric.setEnabled(true);
            JRBInch.setEnabled(true);
        }
    }

    public void UpdateConfigTab() {
        this.TADebug.append("\n=======================================" + "\nUpdateConfigTab()");
        this.CBLanguage.setSelected(c.getUseDefaultLanguage());
        this.JCBLanguage.setEnabled(!this.CBLanguage.isSelected());
        try {
            this.JCBSerialInterface.setSelectedIndex(c.getCBComPortPosition());
        } catch (Exception ex) {
            this.JCBSerialInterface.setSelectedIndex(0);
        }
        this.JCBBaudRate.setSelectedIndex(c.getCBBaudRatePosition());
        this.JCBParity.setSelectedIndex(c.getParity());
        this.JCBStopBit.setSelectedIndex(c.getStopBitPosition());
        this.JCBDataBits.setSelectedIndex(c.getDataBitPosition());
        this.JCBFlowControl.setSelectedIndex(c.getFlowControlPosition());
        this.TFIPAddress.setText(c.getIp());
        this.TFIPPort.setText("" + c.getPort());
        this.CBDebug.setSelected(c.getUseDebugOutput());
    }

    public void UpdateTransmissionTab(boolean activate) {
        this.TADebug.append("\n=======================================" + "\nUpdateTransmissionTab()");
        if (activate) {
            this.TADebug.append("\nActivated");
            this.JRBEthernet.setEnabled(true);
            this.JRBSerial.setEnabled(true);
            this.JBReceive.setEnabled(true);
            this.JBTransmit.setEnabled(true);
            this.CBXport.setEnabled(true);
            this.JBTransAbort.setEnabled(false);
        } else {
            this.TADebug.append("\nDeactivated");
            this.JRBEthernet.setEnabled(false);
            this.JRBSerial.setEnabled(false);
            this.JBReceive.setEnabled(false);
            this.JBTransmit.setEnabled(false);
            this.CBXport.setEnabled(false);
            this.JBTransAbort.setEnabled(true);
        }
    }

    public void clearPanel() {
        this.TADebug.append("\n=======================================" + "\nclearPanel()");
        TFX.setText("");
        TFZ.setText("");
        TFF.setText("");
        TFH.setText("");
        JBAdd.setEnabled(true);
        TFLine.setText("" + enteredCode.size());
        TFComment.setText("");
    }

    public void importProgramFile(File file) {
        this.TADebug.append("\n=======================================" + "\nimportProgramFile(File file)");
        tabelle.enteredCode.clear();
        boolean read = false;
        String line;
        BufferedReader inputStream;
        this.TADebug.append("\nStarted importing the program");
        String errMsg = java.util.ResourceBundle.getBundle("MainFrameBundle").getString("ERROR WHILE IMPORTING THE PROGRAM FILE!");
        try {
            inputStream = new BufferedReader(new FileReader(file));
            while ((line = inputStream.readLine()) != null) {
                read = importProgram(line, read);
            }
            inputStream.close();
            this.TADebug.append("\nSuccessfully finished importing the program" + "\n=======================================");
        } catch (FileNotFoundException e) {
            System.out.println(e);
            this.TADebug.append("FileNotFoundException occured!" + "\n=======================================");
            JOptionPane.showMessageDialog(this, errMsg, fileNotFoundError, JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            this.TADebug.append("IOException occured!" + "\n=======================================");
            JOptionPane.showMessageDialog(this, errMsg, ioError, JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean importProgram(String line, boolean read) {
        this.TADebug.append("\n=======================================" + "\nimportProgram(String line, boolean read)");
        if (!read) {
            if (line.equalsIgnoreCase("    N` G`   X `    Z `  F`  H ")) {
                read = true;
                this.TADebug.append("\nReceived  '    N` G`   X `    Z `  F`  H '" + "\nStart with reading the program");
            } else {
                if (!(line.equalsIgnoreCase("%"))) {
                    this.TFProgName.setText(line);
                    this.TADebug.append("\nReceived headline: \n" + line + "\nLength: " + line.length());
                }
            }
        } else {
            ProgramCode tmpPC = new ProgramCode();
            if (line.equalsIgnoreCase("   M") || line.equalsIgnoreCase("   \"")) {
                this.TADebug.append("Received last line: \n" + line + "\nLength: " + line.length());
                if (line.equalsIgnoreCase("   \"")) {
                    c.setUseMetric(false);
                    this.TADebug.append("\nImperial");
                } else {
                    c.setUseMetric(true);
                    this.TADebug.append("\nMetric");
                }
            } else {
                this.TADebug.append("\nReceived program line: \n" + line + "\nLength: " + line.length());
                String code;
                String xPref = "" + line.charAt(10);
                String x = "" + line.charAt(11) + line.charAt(12) + line.charAt(13) + line.charAt(14);
                if (xPref.equalsIgnoreCase("-")) {
                    xPref = " ";
                    x = "-" + x;
                }
                x = parseTextField(x);
                String zPref = "" + line.charAt(16);
                String z = "" + line.charAt(17) + line.charAt(18) + line.charAt(19) + line.charAt(20) + line.charAt(21);
                if (zPref.equalsIgnoreCase("-")) {
                    zPref = " ";
                    z = "-" + z;
                }
                z = parseTextField(z);
                String fPref = "" + line.charAt(22);
                String f = "" + line.charAt(23) + line.charAt(24) + line.charAt(25);
                if (fPref.equalsIgnoreCase("-")) {
                    fPref = " ";
                    f = "-" + f;
                }
                f = parseTextField(f);
                String hPref = "" + line.charAt(26);
                String h = "" + line.charAt(27) + line.charAt(28) + line.charAt(29);
                if (hPref.equalsIgnoreCase("-")) {
                    hPref = " ";
                    h = "-" + h;
                }
                h = parseTextField(h);
                String userComment = "";
                if (line.length() > 30) {
                    for (int i = 30; i < line.length(); i++) {
                        userComment += line.charAt(i);
                    }
                }
                String labelText;
                if (line.charAt(6) != ' ') {
                    code = "" + line.charAt(6) + line.charAt(7) + line.charAt(8);
                    labelText = code;
                    for (int i = 0; i < mCode.size(); i++) {
                        tmpPC = ((ProgramCode) mCode.get(i));
                        if (tmpPC.getCode().equalsIgnoreCase(code)) {
                            break;
                        }
                    }
                } else {
                    code = "" + line.charAt(7) + line.charAt(8);
                    labelText = code;
                    for (int i = 0; i < gCode.size(); i++) {
                        tmpPC = ((ProgramCode) gCode.get(i));
                        if (tmpPC.getCode().equalsIgnoreCase(code)) {
                            break;
                        }
                    }
                }
                this.TADebug.append("\nFinished reading the line, program line will be added to table");
                tabelle.newEnteredCode(new EnteredCode(code, tmpPC.getComment(), xPref, zPref, fPref, hPref, labelText, tmpPC.getXlText(), tmpPC.getZlText(), tmpPC.getFlText(), tmpPC.getHlText(), tmpPC.getShowX(), tmpPC.getShowZ(), tmpPC.getShowF(), tmpPC.getShowH(), x, z, f, h, tmpPC.getMinX(), tmpPC.getMinZ(), tmpPC.getMinF(), tmpPC.getMinH(), tmpPC.getMaxX(), tmpPC.getMaxZ(), tmpPC.getMaxF(), tmpPC.getMaxH(), tmpPC.getMinXmetric(), tmpPC.getMinZmetric(), tmpPC.getMinFmetric(), tmpPC.getMinHmetric(), tmpPC.getMaxXmetric(), tmpPC.getMaxZmetric(), tmpPC.getMaxFmetric(), tmpPC.getMaxHmetric(), userComment));
            }
        }
        return read;
    }

    public String export(boolean withComments) {
        this.TADebug.append("\n=======================================" + "\nexport(boolean withComments)");
        this.TADebug.append("\nTry to export");
        String line;
        line = this.TFProgName.getText() + "\r\n";
        line += "%" + "\r\n";
        line += "    N` G`   X `    Z `  F`  H " + "\r\n";
        for (int i = 0; i < tabelle.enteredCode.size(); i++) {
            if (i < 10) {
                line += "    0" + i;
            } else if (i >= 10 && i < 100) {
                line += "    " + i;
            } else if (i >= 100 && i < 1000) {
                line += "   " + i;
            } else if (i >= 1000 && i < 10000) {
                line += "  " + i;
            } else {
                line += " " + i;
            }
            if (((EnteredCode) tabelle.enteredCode.elementAt(i)).getCode().length() < 3) {
                line += " ";
            }
            line += ((EnteredCode) tabelle.enteredCode.elementAt(i)).getCode();
            line += " ";
            String errMsg = java.util.ResourceBundle.getBundle("MainFrameBundle").getString("ERROR WHILE EXPORTING THE PROGRAM AT LINE ");
            if (((EnteredCode) tabelle.enteredCode.elementAt(i)).getShowX()) {
                try {
                    line += prepareForExport(((EnteredCode) tabelle.enteredCode.elementAt(i)).getX(), 4, ((EnteredCode) tabelle.enteredCode.elementAt(i)).getXPrefix());
                } catch (java.lang.NumberFormatException e) {
                    line += "     ";
                    JOptionPane.showMessageDialog(this, errMsg + i + java.util.ResourceBundle.getBundle("MainFrameBundle").getString(", IN FIELD X"), numberFormatError, JOptionPane.WARNING_MESSAGE);
                }
            } else {
                line += "     ";
            }
            line += " ";
            if (((EnteredCode) tabelle.enteredCode.elementAt(i)).getShowZ()) {
                try {
                    line += prepareForExport(((EnteredCode) tabelle.enteredCode.elementAt(i)).getZ(), 5, ((EnteredCode) tabelle.enteredCode.elementAt(i)).getZPrefix());
                } catch (java.lang.NumberFormatException e) {
                    line += "      ";
                    JOptionPane.showMessageDialog(this, errMsg + i + java.util.ResourceBundle.getBundle("MainFrameBundle").getString(", IN FIELD Z"), numberFormatError, JOptionPane.WARNING_MESSAGE);
                }
            } else {
                line += "      ";
            }
            if (((EnteredCode) tabelle.enteredCode.elementAt(i)).getShowF()) {
                try {
                    line += prepareForExport(((EnteredCode) tabelle.enteredCode.elementAt(i)).getF(), 3, ((EnteredCode) tabelle.enteredCode.elementAt(i)).getFPrefix());
                } catch (java.lang.NumberFormatException e) {
                    line += "    ";
                    JOptionPane.showMessageDialog(this, errMsg + i + java.util.ResourceBundle.getBundle("MainFrameBundle").getString(", IN FIELD F"), numberFormatError, JOptionPane.WARNING_MESSAGE);
                }
            } else {
                line += "    ";
            }
            if (((EnteredCode) tabelle.enteredCode.elementAt(i)).getShowH()) {
                try {
                    line += prepareForExport(((EnteredCode) tabelle.enteredCode.elementAt(i)).getH(), 3, ((EnteredCode) tabelle.enteredCode.elementAt(i)).getHPrefix());
                } catch (java.lang.NumberFormatException e) {
                    line += "    ";
                    JOptionPane.showMessageDialog(this, errMsg + i + java.util.ResourceBundle.getBundle("MainFrameBundle").getString(", IN FIELD H"), numberFormatError, JOptionPane.WARNING_MESSAGE);
                }
            } else {
                line += "    ";
            }
            if (withComments) {
                line += ((EnteredCode) tabelle.enteredCode.elementAt(i)).getUserComment();
            }
            line += "\r\n";
        }
        if (c.getUseMetric()) {
            line += "   M";
        } else {
            line += "   \"";
        }
        this.TADebug.append("\nSuccessfully exportet the program");
        this.TADebug.append("\n" + line);
        this.TADebug.append("\n=======================================");
        return line;
    }

    public void exportProgramAsFile(String line, File file) throws IOException {
        this.TADebug.append("\n=======================================" + "\nexportProgramAsFile(String line, File file)");
        FileWriter outputStream = null;
        try {
            this.TADebug.append("\nWriting file " + file);
            outputStream = new FileWriter(file);
            outputStream.write(line);
        } catch (IOException ex) {
            this.TADebug.append("\nIOException occured");
            JOptionPane.showMessageDialog(this, ioError, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("ERROR WHILE SAVING THE FILE"), JOptionPane.ERROR_MESSAGE);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    public String prepareForExport(String text, int max, String prefix) {
        this.TADebug.append("\n=======================================" + "\nprepareForExport(String text, int max, String prefix)");
        String line = "";
        String tempString = "";
        int tempInt = Integer.parseInt(text);
        if (tempInt < 0) {
            line += "-";
            tempInt *= -1;
        } else {
            line += prefix;
        }
        if (tempInt < 10) {
            tempString += "0";
        }
        tempString += tempInt;
        if (tempString.length() < max) {
            for (int j = tempString.length(); j < max; j++) {
                line += " ";
            }
        }
        line += tempString;
        this.TADebug.append("\nLine:\n" + line);
        return line;
    }

    public String parseTextField(String t) {
        this.TADebug.append("\n=======================================" + "\nparseTextField(String t)");
        String temp = "";
        for (int i = 0; i < t.length(); i++) {
            if (Character.isWhitespace(t.charAt(i)) == false) {
                if (Character.isDigit(t.charAt(i)) || t.charAt(i) == '-') {
                    temp += t.charAt(i);
                }
            }
        }
        return temp;
    }

    public boolean verifyInput(javax.swing.JTextField t, int min, int max) {
        this.TADebug.append("\n=======================================" + "\nverifyInput(javax.swing.JTextField t, int min, int max)");
        boolean valid = false;
        int temp = max + 1;
        try {
            temp = Integer.parseInt(t.getText());
        } catch (InputMismatchException e) {
            valid = false;
        } catch (NullPointerException e) {
            valid = false;
        } catch (NumberFormatException e) {
            valid = false;
        }
        if ((temp >= min) && (temp <= max)) {
            valid = true;
        } else {
            valid = false;
        }
        if (valid) {
            t.setBackground(Color.white);
        } else {
            t.setBackground(Color.red);
        }
        return valid;
    }

    public void resetColor() {
        this.TADebug.append("\n=======================================" + "\nresetColor()");
        this.TFX.setBackground(Color.WHITE);
        this.TFZ.setBackground(Color.WHITE);
        this.TFF.setBackground(Color.WHITE);
        this.TFH.setBackground(Color.WHITE);
    }

    public boolean checkValues() {
        this.TADebug.append("\n=======================================" + "\ncheckValues()");
        boolean xValid = true, zValid = true, fValid = true, hValid = true;
        if (c.getUseMetric() == true) {
            if (tmpProgram.getShowX()) {
                xValid = verifyInput(TFX, tmpProgram.getMinXmetric(), tmpProgram.getMaxXmetric());
            }
            if (tmpProgram.getShowZ()) {
                zValid = verifyInput(TFZ, tmpProgram.getMinZmetric(), tmpProgram.getMaxZmetric());
            }
            if (tmpProgram.getShowF()) {
                fValid = verifyInput(TFF, tmpProgram.getMinFmetric(), tmpProgram.getMaxFmetric());
            }
            if (tmpProgram.getShowH()) {
                hValid = verifyInput(TFH, tmpProgram.getMinHmetric(), tmpProgram.getMaxHmetric());
            }
        } else {
            if (tmpProgram.getShowX()) {
                xValid = verifyInput(TFX, tmpProgram.getMinX(), tmpProgram.getMaxX());
            }
            if (tmpProgram.getShowZ()) {
                zValid = verifyInput(TFZ, tmpProgram.getMinZ(), tmpProgram.getMaxZ());
            }
            if (tmpProgram.getShowF()) {
                fValid = verifyInput(TFF, tmpProgram.getMinF(), tmpProgram.getMaxF());
            }
            if (tmpProgram.getShowH()) {
                hValid = verifyInput(TFH, tmpProgram.getMinH(), tmpProgram.getMaxH());
            }
        }
        boolean valid;
        if (xValid == false || zValid == false || fValid == false || hValid == false) {
            valid = false;
        } else {
            valid = true;
        }
        return valid;
    }

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        this.TADebug.append("\n=======================================" + "\nfocusLost(FocusEvent e)");
        if (e.getComponent().getAccessibleContext().getAccessibleName().equalsIgnoreCase("TFX")) {
            if (c.getUseMetric()) {
                verifyInput(this.TFX, tmpProgram.getMinXmetric(), tmpProgram.getMaxXmetric());
            } else {
                verifyInput(this.TFX, tmpProgram.getMinX(), tmpProgram.getMaxX());
            }
        }
        if (e.getComponent().getAccessibleContext().getAccessibleName().equalsIgnoreCase("TFZ")) {
            if (c.getUseMetric()) {
                verifyInput(this.TFZ, tmpProgram.getMinZmetric(), tmpProgram.getMaxZmetric());
            } else {
                verifyInput(this.TFZ, tmpProgram.getMinZ(), tmpProgram.getMaxZ());
            }
        }
        if (e.getComponent().getAccessibleContext().getAccessibleName().equalsIgnoreCase("TFF")) {
            if (c.getUseMetric()) {
                verifyInput(this.TFF, tmpProgram.getMinFmetric(), tmpProgram.getMaxFmetric());
            } else {
                verifyInput(this.TFF, tmpProgram.getMinF(), tmpProgram.getMaxF());
            }
        }
        if (e.getComponent().getAccessibleContext().getAccessibleName().equalsIgnoreCase("TFH")) {
            if (c.getUseMetric()) {
                verifyInput(this.TFH, tmpProgram.getMinHmetric(), tmpProgram.getMaxHmetric());
            } else {
                verifyInput(this.TFH, tmpProgram.getMinH(), tmpProgram.getMaxH());
            }
        }
    }

    public void rxtxAbort() {
        this.TADebug.append("\n=======================================" + "\nrxtxAbort()");
        if ((this.threadId < 5) && (this.threadId > 0)) {
            rxtx.stop();
        }
        switch(this.threadId) {
            case 1:
                rn.closeNetworklConnection();
                break;
            case 2:
                rs.closeSerialConnection();
                break;
            case 3:
                sn.closeNetworkConnection();
                break;
            case 4:
                ss.closeSerialConnection();
                break;
            default:
                this.TARxTx.append("\n" + java.util.ResourceBundle.getBundle("MainFrameBundle").getString("NO OPEN CONNECTION") + "\n");
        }
        this.threadId = 0;
    }

    public void actionPerformed(ActionEvent e) {
        this.TADebug.append("\n=======================================" + "\nactionPerformed(ActionEvent e)\n" + "Action event: '" + e.getActionCommand() + "'");
        if (e.getActionCommand().equalsIgnoreCase("gcclick")) {
            UpdatePanel(CBGCode.getSelectedItem(), false);
            JBAdd.setEnabled(true);
            resetColor();
        } else if (e.getActionCommand().equalsIgnoreCase("mcclick")) {
            UpdatePanel(CBMCode.getSelectedItem(), false);
            JBAdd.setEnabled(true);
            resetColor();
        } else if (e.getActionCommand().equalsIgnoreCase("exit")) {
            if (JOptionPane.showConfirmDialog(this, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("ARE YOU SURE YOU WANT TO QUIT?"), java.util.ResourceBundle.getBundle("MainFrameBundle").getString("QUIT"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        } else if (e.getActionCommand().equalsIgnoreCase("add")) {
            if (checkValues()) {
                tabelle.newEnteredCode(new EnteredCode(tmpProgram, TFX.getText(), TFZ.getText(), TFF.getText(), TFH.getText(), TFComment.getText()));
                tabelle.fireTableDataChanged();
                table.changeSelection(this.previousPosition + 1, 0, false, false);
                clearPanel();
            }
        } else if (e.getActionCommand().equalsIgnoreCase("insert")) {
            this.previousPosition = this.position;
            if (checkValues()) {
                try {
                    tabelle.insertEnteredCode(new EnteredCode(tmpProgram, TFX.getText(), TFZ.getText(), TFF.getText(), TFH.getText(), TFComment.getText()), this.position);
                    tabelle.fireTableRowsInserted(position, position);
                    table.changeSelection(1, 0, false, false);
                    clearPanel();
                } catch (ArrayIndexOutOfBoundsException ex) {
                    JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("NO LINE IN PROGRAM OR NOT SELECTED!"));
                    this.TADebug.append("\nTried to insert at a non-existing line");
                }
            }
        } else if (e.getActionCommand().equalsIgnoreCase("update")) {
            if (checkValues()) {
                try {
                    tabelle.updateEnteredCode(new EnteredCode(tmpProgram, TFX.getText(), TFZ.getText(), TFF.getText(), TFH.getText(), TFComment.getText()), this.position);
                    tabelle.fireTableRowsUpdated(position, position);
                    clearPanel();
                } catch (ArrayIndexOutOfBoundsException ex) {
                    JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("NO LINE IN PROGRAM OR NOT SELECTED!"));
                    this.TADebug.append("\nTried to update a non-existing line");
                }
            }
        } else if (e.getActionCommand().equalsIgnoreCase("delete")) {
            try {
                tabelle.deleteEnteredCode(position);
                this.TADebug.append("\nLine deleted");
            } catch (ArrayIndexOutOfBoundsException ex) {
                JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("NO LINE IN PROGRAM OR NOT SELECTED!"));
                this.TADebug.append("\nTried to delete a non-existing line");
            }
            tabelle.fireTableDataChanged();
            if (this.previousPosition > 0) {
                table.changeSelection(this.previousPosition - 1, 0, false, false);
            }
            clearPanel();
        } else if (e.getActionCommand().equalsIgnoreCase("open")) {
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                currentFile = fc.getSelectedFile();
                importProgramFile(currentFile);
                this.TADebug.append("\nLoading file " + currentFile);
                tabelle.fireTableDataChanged();
                this.setTitle(this.programVersion + " - " + currentFile);
                this.TADebug.append("\nProgram title updated to " + this.getTitle());
                this.MISave.setEnabled(true);
                this.JBDelete.setEnabled(true);
            } else {
                this.TADebug.append("\nAborted");
            }
        } else if (e.getActionCommand().equalsIgnoreCase("saveas")) {
            boolean goOn = true;
            do {
                int returnVal = fc.showSaveDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    currentFile = fc.getSelectedFile();
                    if (!currentFile.getName().toLowerCase().endsWith(".cnc")) {
                        currentFile = new File(currentFile.getAbsolutePath() + ".cnc");
                    }
                    this.TADebug.append("\nTry to save program as file " + currentFile);
                    if (currentFile.exists()) {
                        this.TADebug.append("\n" + currentFile + " already exists!");
                        if (JOptionPane.showConfirmDialog(this, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("OVERWRITE EXISTING FILE ") + currentFile + "?", java.util.ResourceBundle.getBundle("MainFrameBundle").getString("SELECTED FILE ALREADY EXISTS!"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            goOn = false;
                        } else {
                            goOn = true;
                        }
                    } else {
                        goOn = false;
                    }
                    if (!goOn) {
                        try {
                            exportProgramAsFile(export(true), currentFile);
                            this.TADebug.append("\nSuccessfully saved " + currentFile);
                            this.setTitle(this.programVersion + " - " + currentFile);
                            this.MISave.setEnabled(true);
                        } catch (IOException ex) {
                            this.TADebug.append("\nIOException while writing " + currentFile);
                            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("ERROR WHILE WRITING TO ") + currentFile, ioError, JOptionPane.ERROR_MESSAGE);
                        }
                        goOn = false;
                    }
                } else {
                    this.TADebug.append("\nAborted by user");
                    goOn = false;
                }
            } while (goOn);
        } else if (e.getActionCommand().equalsIgnoreCase("new")) {
            tabelle.enteredCode.clear();
            tabelle.fireTableDataChanged();
            clearPanel();
            this.TFProgName.setText("title of program");
            this.MISave.setEnabled(false);
            this.setTitle(this.programVersion);
        } else if (e.getActionCommand().equalsIgnoreCase("metric")) {
            c.setUseMetric(true);
            UpdatePanel(tmpProgram, false);
        } else if (e.getActionCommand().equalsIgnoreCase("imperial")) {
            c.setUseMetric(false);
            UpdatePanel(tmpProgram, false);
        } else if (e.getActionCommand().equalsIgnoreCase("usedebug")) {
            c.setUseDebugOutput(this.CBDebug.isSelected());
            if (c.getUseDebugOutput()) {
                TPMain.addTab(java.util.ResourceBundle.getBundle("MainFrameBundle").getString("DEBUGGING OUTPUT"), JPDebug);
                this.pack();
            } else {
                TPMain.remove(JPDebug);
                this.pack();
            }
        } else if (e.getActionCommand().equalsIgnoreCase("defaultsettings")) {
            c = new Config();
            this.JCBBaudRate.setSelectedIndex(1);
            this.JCBSerialInterface.setSelectedIndex(0);
            this.UpdateConfigTab();
        } else if (e.getActionCommand().equalsIgnoreCase("defaultlanguage")) {
            c.setUseDefaultLanguage(this.CBLanguage.isSelected());
            this.JCBLanguage.setEnabled(!this.CBLanguage.isSelected());
        } else if (e.getActionCommand().equalsIgnoreCase("confsave")) {
            c.setCBComPortPosition(this.JCBSerialInterface.getSelectedIndex());
            c.setComPort(this.JCBSerialInterface.getItemAt(c.getCBComPortPosition()).toString());
            c.setBaudRate(Integer.parseInt(this.JCBBaudRate.getSelectedItem().toString()));
            c.setCBBaudRatePosition(this.JCBBaudRate.getSelectedIndex());
            c.setIp(this.TFIPAddress.getText());
            c.setUseDebugOutput(this.CBDebug.isSelected());
            c.setParity(this.JCBParity.getSelectedIndex());
            c.setStopBit(this.JCBStopBit.getSelectedIndex());
            c.setDataBit(this.JCBDataBits.getSelectedIndex());
            c.setFlowControl(this.JCBFlowControl.getSelectedIndex());
            try {
                c.setPort(Integer.parseInt(this.TFIPPort.getText()));
            } catch (InputMismatchException ex) {
                c.setPort(10001);
                this.TADebug.append("\nNo valid tcp port entered, set to 10001");
            }
            UpdateConfigTab();
            String errMsg = java.util.ResourceBundle.getBundle("MainFrameBundle").getString("ERROR WHILE WRITING THE CONFIG FILE: ") + configFile;
            try {
                FileOutputStream fos = new FileOutputStream(configFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(c);
                oos.close();
                this.TADebug.append("\nSuccessfully saved the configuration in:" + "\n" + configFile);
                this.TADebug.append("\n" + this.c);
                JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("SUCCESSFULLY SAVED THE CONFIG FILE TO ") + "\n" + configFile);
            } catch (FileNotFoundException ex) {
                this.TADebug.append("\nFileNotFoundException while saving" + " the config file " + configFile);
                JOptionPane.showMessageDialog(this, errMsg, fileNotFoundError, JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                this.TADebug.append("\nIOException while saving" + " the config file " + configFile);
                JOptionPane.showMessageDialog(this, errMsg, ioError, JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                this.TADebug.append("\nUnexpected error while saving" + " the config file " + configFile);
                JOptionPane.showMessageDialog(this, errMsg, unexpectedError, JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getActionCommand().equalsIgnoreCase("configabort")) {
            this.JCBSerialInterface.setSelectedIndex(c.getCBComPortPosition());
            this.JCBBaudRate.setSelectedIndex(c.getCBBaudRatePosition());
            this.JCBParity.setSelectedIndex(c.getParity());
            this.JCBStopBit.setSelectedIndex(c.getStopBitPosition());
            this.JCBDataBits.setSelectedIndex(c.getDataBitPosition());
            this.JCBFlowControl.setSelectedIndex(c.getFlowControlPosition());
            this.TFIPAddress.setText(c.getIp());
            this.TFIPPort.setText("" + c.getPort());
            this.CBDebug.setSelected(c.getUseDebugOutput());
            UpdateConfigTab();
        } else if (e.getActionCommand().equalsIgnoreCase("about")) {
            JOptionPane.showMessageDialog(this, aboutText, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("ABOUT"), JOptionPane.INFORMATION_MESSAGE);
        } else if (e.getActionCommand().equalsIgnoreCase("receive")) {
            if (this.BGTrans.getSelection().getActionCommand().equalsIgnoreCase("useeth")) {
                rn = new ReceiveNetwork(this, this.c);
                rxtx = new Thread(rn);
                rxtx.start();
                this.threadId = 1;
            } else if (this.BGTrans.getSelection().getActionCommand().equalsIgnoreCase("useserial")) {
                rs = new ReceiveSerial(this, this.c);
                rxtx = new Thread(rs);
                rxtx.start();
                this.threadId = 2;
            } else {
                JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("PLEASE CHOOSE WETHER TO USE SERIAL OR ETHERNET!"));
            }
        } else if (e.getActionCommand().equalsIgnoreCase("transmit")) {
            if (this.BGTrans.getSelection().getActionCommand().equalsIgnoreCase("useeth")) {
                sn = new SendNetwork(export(false), this.c, this);
                rxtx = new Thread(sn);
                rxtx.start();
                this.threadId = 3;
            } else if (this.BGTrans.getSelection().getActionCommand().equalsIgnoreCase("useserial")) {
                ss = new SendSerial(export(true), this.c, this);
                rxtx = new Thread(ss);
                rxtx.start();
                this.threadId = 4;
            } else {
                JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("MainFrameBundle").getString("PLEASE CHOOSE WETHER TO USE SERIAL OR ETHERNET!"));
            }
        } else if (e.getActionCommand().equalsIgnoreCase("setxport")) {
            c.setUseXPort(this.CBXport.isSelected());
        } else if (e.getActionCommand().equalsIgnoreCase("rxtxabort")) {
            if ((this.threadId < 5) && (this.threadId > 0)) {
                rxtxAbort();
            }
        } else {
            this.TADebug.append("\nUnknown ActionCommand!");
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        this.position = table.getSelectedRow();
        if (this.position != -1 && position < tabelle.enteredCode.size()) {
            UpdatePanel(tabelle.enteredCode.elementAt(position), true);
        }
    }

    private javax.swing.ButtonGroup BGMetric;

    private javax.swing.ButtonGroup BGTrans;

    private javax.swing.JCheckBox CBDebug;

    private javax.swing.JComboBox CBGCode;

    private javax.swing.JCheckBox CBLanguage;

    private javax.swing.JComboBox CBMCode;

    private javax.swing.JCheckBox CBXport;

    private javax.swing.JButton JBAdd;

    private javax.swing.JButton JBConfAbort;

    private javax.swing.JButton JBConfSave;

    private javax.swing.JButton JBDefaultSettings;

    private javax.swing.JButton JBDelete;

    private javax.swing.JButton JBInsert;

    private javax.swing.JButton JBReceive;

    private javax.swing.JButton JBTransAbort;

    private javax.swing.JButton JBTransmit;

    private javax.swing.JButton JBUpdate;

    private javax.swing.JComboBox JCBBaudRate;

    private javax.swing.JComboBox JCBDataBits;

    private javax.swing.JComboBox JCBFlowControl;

    private javax.swing.JComboBox JCBLanguage;

    private javax.swing.JComboBox JCBParity;

    private javax.swing.JComboBox JCBSerialInterface;

    private javax.swing.JComboBox JCBStopBit;

    private javax.swing.JLabel JLBaudRate;

    private javax.swing.JLabel JLCode;

    private javax.swing.JLabel JLComment;

    private javax.swing.JLabel JLDataBits;

    private javax.swing.JLabel JLDescription;

    private javax.swing.JLabel JLF;

    private javax.swing.JLabel JLFlowControl;

    private javax.swing.JLabel JLH;

    private javax.swing.JLabel JLIPAddress;

    private javax.swing.JLabel JLIPPort;

    private javax.swing.JLabel JLLine;

    private javax.swing.JLabel JLMeasurement;

    private javax.swing.JLabel JLParity;

    private javax.swing.JLabel JLProgName;

    private javax.swing.JLabel JLSerialInterface;

    private javax.swing.JLabel JLStopBits;

    private javax.swing.JLabel JLX;

    private javax.swing.JLabel JLZ;

    private javax.swing.JMenu JMAbout;

    private javax.swing.JMenu JMFile;

    private javax.swing.JMenuItem JMIAbout;

    private javax.swing.JPanel JPConf;

    private javax.swing.JPanel JPConf1;

    private javax.swing.JPanel JPConf11;

    private javax.swing.JPanel JPConf111;

    private javax.swing.JPanel JPConf112;

    private javax.swing.JPanel JPConf113;

    private javax.swing.JPanel JPConf114;

    private javax.swing.JPanel JPConf115;

    private javax.swing.JPanel JPConf116;

    private javax.swing.JPanel JPConf12;

    private javax.swing.JPanel JPConf121;

    private javax.swing.JPanel JPConf122;

    private javax.swing.JPanel JPConf123;

    private javax.swing.JPanel JPConf124;

    private javax.swing.JPanel JPConf125;

    private javax.swing.JPanel JPConf126;

    private javax.swing.JPanel JPConf2;

    private javax.swing.JPanel JPConf21;

    private javax.swing.JPanel JPConf22;

    private javax.swing.JPanel JPConf3;

    private javax.swing.JPanel JPConf31;

    private javax.swing.JPanel JPConf32;

    private javax.swing.JPanel JPConf33;

    private javax.swing.JPanel JPConf331;

    private javax.swing.JPanel JPConf332;

    private javax.swing.JPanel JPDebug;

    private javax.swing.JPanel JPMain;

    private javax.swing.JPanel JPMainNorth;

    private javax.swing.JPanel JPMainNorthCenter;

    private javax.swing.JPanel JPMainNorthCenter1;

    private javax.swing.JPanel JPMainNorthCenter2;

    private javax.swing.JPanel JPMainNorthCenter3;

    private javax.swing.JPanel JPMainNorthCenter31;

    private javax.swing.JPanel JPMainNorthCenter311;

    private javax.swing.JPanel JPMainNorthCenter312;

    private javax.swing.JPanel JPMainNorthCenter32;

    private javax.swing.JPanel JPMainNorthCenter321;

    private javax.swing.JPanel JPMainNorthCenter322;

    private javax.swing.JPanel JPMainNorthCenter4;

    private javax.swing.JPanel JPMainNorthCenter41;

    private javax.swing.JPanel JPMainNorthCenter411;

    private javax.swing.JPanel JPMainNorthCenter412;

    private javax.swing.JPanel JPMainNorthCenter42;

    private javax.swing.JPanel JPMainNorthCenter421;

    private javax.swing.JPanel JPMainNorthCenter422;

    private javax.swing.JPanel JPMainNorthCenter5;

    private javax.swing.JPanel JPMainNorthCenter51;

    private javax.swing.JPanel JPMainNorthCenter52;

    private javax.swing.JPanel JPMainNorthCenter6;

    private javax.swing.JPanel JPMainNorthCenter61;

    private javax.swing.JPanel JPMainNorthCenter62;

    private javax.swing.JPanel JPMainNorthLeft;

    private javax.swing.JPanel JPMainNorthLeft1;

    private javax.swing.JPanel JPMainNorthLeft2;

    private javax.swing.JPanel JPMainNorthLeft3;

    private javax.swing.JPanel JPMainNorthLeft4;

    private javax.swing.JPanel JPMainNorthLeft5;

    private javax.swing.JPanel JPTrans;

    private javax.swing.JPanel JPTransCenter;

    private javax.swing.JPanel JPTransRight;

    private javax.swing.JPanel JPTransRight1;

    private javax.swing.JPanel JPTransRight10;

    private javax.swing.JPanel JPTransRight11;

    private javax.swing.JPanel JPTransRight12;

    private javax.swing.JPanel JPTransRight13;

    private javax.swing.JPanel JPTransRight14;

    private javax.swing.JPanel JPTransRight15;

    private javax.swing.JPanel JPTransRight16;

    private javax.swing.JPanel JPTransRight17;

    private javax.swing.JPanel JPTransRight18;

    private javax.swing.JPanel JPTransRight19;

    private javax.swing.JPanel JPTransRight2;

    private javax.swing.JPanel JPTransRight20;

    private javax.swing.JPanel JPTransRight3;

    private javax.swing.JPanel JPTransRight3a;

    private javax.swing.JPanel JPTransRight3b;

    private javax.swing.JPanel JPTransRight4;

    private javax.swing.JPanel JPTransRight5;

    private javax.swing.JPanel JPTransRight6;

    private javax.swing.JPanel JPTransRight7;

    private javax.swing.JPanel JPTransRight8;

    private javax.swing.JPanel JPTransRight9;

    private javax.swing.JRadioButton JRBEthernet;

    private javax.swing.JRadioButton JRBInch;

    private javax.swing.JRadioButton JRBMetric;

    private javax.swing.JRadioButton JRBSerial;

    private javax.swing.JMenuBar MBMain;

    private javax.swing.JMenuItem MIExit;

    private javax.swing.JMenuItem MINew;

    private javax.swing.JMenuItem MIOpen;

    private javax.swing.JMenuItem MISave;

    private javax.swing.JMenuItem MISaveAs;

    private javax.swing.JScrollPane SPDebug;

    private javax.swing.JScrollPane SPProgram;

    private javax.swing.JScrollPane SPRxTx;

    protected javax.swing.JTextArea TADebug;

    public javax.swing.JTextArea TARxTx;

    private javax.swing.JTextField TFCode;

    private javax.swing.JTextField TFComment;

    private javax.swing.JTextField TFF;

    private javax.swing.JTextField TFH;

    private javax.swing.JTextField TFIPAddress;

    private javax.swing.JTextField TFIPPort;

    private javax.swing.JTextField TFLine;

    private javax.swing.JTextField TFProgName;

    private javax.swing.JTextField TFX;

    private javax.swing.JTextField TFZ;

    private javax.swing.JTabbedPane TPMain;
}

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;

class xecl extends JFrame {

    static optionConstant Const;

    static Constrain constrain;

    static final String CMP_CC = "cc";

    static final String CMP_CL = "cl";

    static final String CMP_ECL = "ecl";

    static String CC;

    static boolean flg_newCompilation;

    static boolean flg_errCompilation;

    static boolean flg_launchEsInEcl;

    static boolean flg_eclCmp;

    static boolean flg_esCmp;

    static boolean flg_eclTxtArgChanged;

    static boolean flg_esTxtArgChanged;

    static boolean flg_EsterelOpt;

    static boolean flg_EsterelAloneOpt;

    static boolean flg_VccOpt;

    static boolean flg_VccImport;

    static boolean flg_TestOpt;

    static boolean flg_OutputPrompt;

    static boolean flg_simulPresent;

    static int esterelCmpVersion;

    static String str_chgDir;

    static String str_CPreCmd;

    static String str_eclCmd;

    static String str_esCmd;

    static String str_clObjCmd;

    static String str_clLinkCmd;

    static String str_vccImportCmd;

    static String str_exeSimulCmd;

    static String str_compareCmd;

    static String[] str_copyCmd;

    static String[] tabCmd;

    static int nbCmd;

    static String EclCmpVersion = "";

    static String EsCmpVersion = "";

    static String XEclVersion = "3.1.0";

    static String EclHelp = "";

    static String EsHelp = "";

    static boolean tabOptionEcl[];

    static boolean tabOptionEs[];

    static boolean tabOptionEsAlone[];

    static String tabEclOpt2Str[];

    static String tabEsOpt2Str[];

    static String dirsep;

    static String wlcDir;

    static String wlcvDir;

    static String Cmp;

    static JMenuBar menubar;

    static JMenu mnuFile;

    static JMenuItem mnuFile_quit;

    static JMenu mnuOption;

    static JMenuItem mnuOption_reset;

    static JMenu mnuOption_EsCompiler;

    static JRadioButtonMenuItem mnuOption_EsCompiler_esterel;

    static JRadioButtonMenuItem mnuOption_EsCompiler_esterelv6;

    static ButtonGroup mnuOption_EsCompiler_Gr;

    static JMenu mnuOption_CCompiler;

    static JRadioButtonMenuItem mnuOption_CCompiler_cl;

    static JRadioButtonMenuItem mnuOption_CCompiler_cc;

    static JRadioButtonMenuItem mnuOption_CCompiler_gcc;

    static ButtonGroup mnuOption_CCompiler_Gr;

    static JCheckBoxMenuItem mnuOption_outputPrompt;

    static JMenu mnuHelp;

    static JMenu mnuHelp_ecl;

    static JMenuItem mnuHelp_ecl_help;

    static JMenuItem mnuHelp_ecl_version;

    static boolean mnuHelp_flgEclHelp;

    static boolean mnuHelp_flgEclVersion;

    static JMenu mnuHelp_es;

    static JMenuItem mnuHelp_es_help;

    static JMenuItem mnuHelp_es_version;

    static boolean mnuHelp_flgEsHelp;

    static boolean mnuHelp_flgEsVersion;

    static JMenuItem mnuHelp_about;

    static JPanel pnlEclCmd;

    static JLabel pnlEclCmd_lbl;

    static JTextArea pnlEclCmd_txtArg;

    static JScrollPane pnlEclCmd_scrPane;

    static JButton pnlEclCmd_btnRun;

    static JButton pnlEclCmd_btnRebuild;

    static JButton pnlEclCmd_btnClear;

    static JButton pnlEclCmd_btnSave;

    static JButton pnlEclCmd_btnLoad;

    static String Es_strFilename;

    static String Ecl_strFilename;

    static String Ecl_strFilePath;

    static String Ecl_strOutDir;

    static String Ecl_strOldOutDir;

    static String base;

    static JPanel pnlEsEcl;

    static JLabel pnlEsECl_lbl;

    static ButtonGroup pnlEsEcl_chkGr;

    static JRadioButton pnlEsEcl_chkEs;

    static JRadioButton pnlEsEcl_chkEcl;

    static JLabel pnlEsEcl_lblFilename;

    static JTextField pnlEsEcl_txtFilename;

    static JButton pnlEsEcl_btnFilename;

    static JLabel pnlEsEcl_lblOutDir;

    static JTextField pnlEsEcl_txtOutDir;

    static JButton pnlEsEcl_btnOutDir;

    static int pnlEsEcl_nbRow;

    static JPanel pnlEclOption;

    static JPanel pnlEclGen;

    static JLabel pnlEclGen_lbl;

    static JCheckBox pnlEclGen_chkCheck;

    static JCheckBox pnlEclGen_chkES;

    static JCheckBox pnlEclGen_chkG;

    static int pnlEclGen_nbRow;

    static JPanel pnlEclCmpMode;

    static JLabel pnlEclCmpMode_lbl;

    static JCheckBox pnlEclCmpMode_chkEsterel;

    static JCheckBox pnlEclCmpMode_chkVcc;

    static JCheckBox pnlEclCmpMode_chkClean;

    static JCheckBox pnlEclCmpMode_chkTest;

    static int pnlEclCmpMode_nbRow;

    static String Vcc_strCellname;

    static String Vcc_strLibname;

    static String Vcc_strWorkspace;

    static String Vcc_strMainmodule;

    static String Vcc_strDfttypelib;

    static String Vcc_strTypelibfile;

    static Vector Vcc_vctTypelib_lib;

    static Vector Vcc_vctTypelib_type;

    static boolean Vcc_Noimport;

    static boolean Vcc_Importtypes;

    static String Es_strD;

    static String Es_strStrlic;

    static String Es_strIclink;

    static String Es_strLcsc;

    static String Es_strScssc;

    static String Es_strSccausal;

    static String Es_strScoc;

    static String EsA_strD;

    static String EsA_strStrlic;

    static String EsA_strIclink;

    static String EsA_strLcsc;

    static String EsA_strScssc;

    static String EsA_strSccausal;

    static String EsA_strScoc;

    static Vector EclAdv_vctI;

    static Vector EclAdv_vctDMacro;

    static Vector EclAdv_vctDValue;

    static JPanel pnlOut;

    static JLabel pnlOut_lbl;

    static JTextArea pnlOut_txtArea;

    JScrollPane pnlOut_scrPane;

    static JButton pnlEclMoreOpt_btnDbgCpre;

    static JButton pnlEsMoreOpt_btnAdv;

    static JButton pnlVccMoreOpt_btnAdv;

    static String FileFormats[], FileDescriptions[];

    static ecl eclInst;

    int mainGrid_nbRow;

    static xeclActionEvents guiActionEvents;

    static xeclItemEvents guiItemEvents;

    static xeclKeyEvents guiKeyEvents;

    static JFileChooser chooser;

    static xeclEclMoreOption DlgEclMoreOpt;

    static xeclVccOptions DlgEclVcc;

    static xeclEsOptions DlgEs;

    JPanel pnlRoot;

    static common_ecl_xecl argXEcl;

    static String[] argCommandLine;

    static Font font;

    static javax.swing.filechooser.FileFilter strlFilter;

    static javax.swing.filechooser.FileFilter eclFilter;

    static javax.swing.filechooser.FileFilter hFilter;

    static javax.swing.filechooser.FileFilter xeclFilter;

    public xecl(String s) throws java.io.IOException, java.lang.Exception {
        super(s);
        Const = new optionConstant();
        constrain = new Constrain();
        tabOptionEcl = new boolean[Const.ECL_NBOPTION];
        tabOptionEs = new boolean[Const.ES_NBOPTION];
        tabOptionEsAlone = new boolean[Const.ES_NBOPTION];
        tabEclOpt2Str = new String[Const.ECL_NBOPTION];
        tabEsOpt2Str = new String[Const.ES_NBOPTION];
        guiActionEvents = new xeclActionEvents();
        guiItemEvents = new xeclItemEvents();
        guiKeyEvents = new xeclKeyEvents();
        Dimension dimButton;
        if (argXEcl.nt) dimButton = new Dimension(80, 30); else dimButton = new Dimension(100, 30);
        Dimension dimTxtArea = new Dimension(561, 120);
        pnlEsEcl_nbRow = 0;
        pnlEclGen_nbRow = 0;
        pnlEclCmpMode_nbRow = 0;
        mainGrid_nbRow = 0;
        this.setBackground(Color.gray);
        GridBagLayout gridbag = new GridBagLayout();
        chooser = new JFileChooser();
        FileFormats = new String[] { ".ecl", ".strl", ".h", ".xecl" };
        FileDescriptions = new String[] { "ecl Source Files (*.ecl)", "strl Source Files (*.strl)", "header file (*.h)", "Xecl option files (*.xecl)" };
        javax.swing.filechooser.FileFilter defaultFilter = new SimpleFilter(FileFormats[0], FileDescriptions[0]);
        xeclFilter = new SimpleFilter(FileFormats[3], FileDescriptions[3]);
        hFilter = new SimpleFilter(FileFormats[2], FileDescriptions[2]);
        strlFilter = new SimpleFilter(FileFormats[1], FileDescriptions[1]);
        eclFilter = new SimpleFilter(FileFormats[0], FileDescriptions[0]);
        pnlEsEcl = new JPanel();
        pnlEsEcl.setLayout(gridbag);
        pnlEclOption = new JPanel();
        pnlEclOption.setLayout(gridbag);
        pnlEclGen = new JPanel();
        pnlEclGen.setLayout(gridbag);
        pnlEclCmpMode = new JPanel();
        pnlEclCmpMode.setLayout(gridbag);
        pnlOut = new JPanel();
        pnlOut.setLayout(gridbag);
        pnlEclCmd = new JPanel();
        pnlEclCmd.setLayout(gridbag);
        menubar = new JMenuBar();
        this.setJMenuBar(menubar);
        mnuFile = new JMenu("File");
        mnuFile.setMnemonic('F');
        mnuFile_quit = new JMenuItem("Quit", 'Q');
        mnuFile.add(mnuFile_quit);
        mnuOption = new JMenu("Option");
        mnuOption.setMnemonic('i');
        mnuOption_reset = new JMenuItem("Clear all options");
        mnuOption_EsCompiler = new JMenu("Es Compiler...");
        mnuOption_EsCompiler_esterel = new JRadioButtonMenuItem("Esterel");
        mnuOption_EsCompiler_esterelv6 = new JRadioButtonMenuItem("EsterelV6", true);
        mnuOption_CCompiler = new JMenu("C Compiler...");
        mnuOption_CCompiler_cl = new JRadioButtonMenuItem("cl compiler", true);
        mnuOption_CCompiler_cc = new JRadioButtonMenuItem("cc compiler");
        mnuOption_CCompiler_gcc = new JRadioButtonMenuItem("gcc compiler");
        mnuOption_outputPrompt = new JCheckBoxMenuItem("Output prompt");
        mnuOption.add(mnuOption_reset);
        mnuOption.add(mnuOption_EsCompiler);
        mnuOption_EsCompiler.add(mnuOption_EsCompiler_esterel);
        mnuOption_EsCompiler.add(mnuOption_EsCompiler_esterelv6);
        mnuOption.add(mnuOption_CCompiler);
        mnuOption_CCompiler.add(mnuOption_CCompiler_cl);
        mnuOption_CCompiler.add(mnuOption_CCompiler_cc);
        mnuOption_CCompiler.add(mnuOption_CCompiler_gcc);
        mnuOption.add(mnuOption_outputPrompt);
        mnuOption_EsCompiler_Gr = new ButtonGroup();
        mnuOption_EsCompiler_Gr.add(mnuOption_EsCompiler_esterel);
        mnuOption_EsCompiler_Gr.add(mnuOption_EsCompiler_esterelv6);
        mnuOption_CCompiler_Gr = new ButtonGroup();
        mnuOption_CCompiler_Gr.add(mnuOption_CCompiler_cl);
        mnuOption_CCompiler_Gr.add(mnuOption_CCompiler_cc);
        mnuOption_CCompiler_Gr.add(mnuOption_CCompiler_gcc);
        mnuHelp = new JMenu("Help");
        mnuHelp.setMnemonic('H');
        mnuHelp_ecl = new JMenu("ECL");
        mnuHelp_ecl_help = new JMenuItem("Help");
        mnuHelp_ecl_version = new JMenuItem("Version");
        mnuHelp_es = new JMenu("Esterel");
        mnuHelp_es_help = new JMenuItem("Help");
        mnuHelp_es_version = new JMenuItem("Version");
        mnuHelp_about = new JMenuItem("About", 'A');
        mnuHelp.add(mnuHelp_ecl);
        mnuHelp_ecl.add(mnuHelp_ecl_help);
        mnuHelp_ecl.add(mnuHelp_ecl_version);
        mnuHelp.add(mnuHelp_es);
        mnuHelp_es.add(mnuHelp_es_help);
        mnuHelp_es.add(mnuHelp_es_version);
        mnuHelp.addSeparator();
        mnuHelp.add(mnuHelp_about);
        menubar.add(mnuFile);
        menubar.add(mnuOption);
        menubar.add(mnuHelp);
        JLabel pnlEsEcl_lbl = new JLabel("Choose an input type:");
        pnlEsEcl_chkGr = new ButtonGroup();
        pnlEsEcl_chkEs = new JRadioButton("Esterel", false);
        pnlEsEcl_chkEcl = new JRadioButton("ECL", true);
        pnlEsEcl_chkGr.add(pnlEsEcl_chkEs);
        pnlEsEcl_chkGr.add(pnlEsEcl_chkEcl);
        pnlEsEcl_lblFilename = new JLabel("Input filename:");
        pnlEsEcl_txtFilename = new JTextField(30);
        pnlEsEcl_btnFilename = new JButton("...");
        pnlEsEcl_lblOutDir = new JLabel("Output directory: ");
        pnlEsEcl_txtOutDir = new JTextField(30);
        pnlEsEcl_btnOutDir = new JButton("...");
        constrain.set(pnlEsEcl, pnlEsEcl_lbl, 0, 0, 1, 1);
        constrain.set(pnlEsEcl, pnlEsEcl_chkEcl, 0, 1, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 40, 0, 0);
        constrain.set(pnlEsEcl, pnlEsEcl_chkEs, 1, 1, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 0, 20);
        constrain.set(pnlEsEcl, pnlEsEcl_lblFilename, 2, 0, 1, 1);
        constrain.set(pnlEsEcl, pnlEsEcl_txtFilename, 3, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 3, 0, 0, 5);
        constrain.set(pnlEsEcl, pnlEsEcl_btnFilename, 4, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 0, 20);
        constrain.set(pnlEsEcl, pnlEsEcl_lblOutDir, 2, 1, 1, 1);
        constrain.set(pnlEsEcl, pnlEsEcl_txtOutDir, 3, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 3, 0, 0, 5);
        constrain.set(pnlEsEcl, pnlEsEcl_btnOutDir, 4, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 0, 20);
        pnlEclCmpMode_lbl = new JLabel("Compiler Mode:");
        pnlEclCmpMode_chkEsterel = new JCheckBox("ESTEREL simulation");
        pnlEclCmpMode_chkVcc = new JCheckBox("VCC import");
        pnlEclCmpMode_chkClean = new JCheckBox("CLEAN");
        pnlEclCmpMode_chkTest = new JCheckBox("TEST");
        pnlEclCmpMode_chkEsterel.setMnemonic('t');
        pnlEclCmpMode_chkVcc.setMnemonic('v');
        pnlEclCmpMode_chkClean.setMnemonic('l');
        pnlEclCmpMode_chkTest.setMnemonic('s');
        constrain.set(pnlEclCmpMode, pnlEclCmpMode_lbl, 0, pnlEclCmpMode_nbRow++, 1, 1);
        constrain.set(pnlEclCmpMode, pnlEclCmpMode_chkEsterel, 0, pnlEclCmpMode_nbRow, 1, 1);
        constrain.set(pnlEclCmpMode, pnlEclCmpMode_chkClean, 1, pnlEclCmpMode_nbRow++, 1, 1);
        constrain.set(pnlEclCmpMode, pnlEclCmpMode_chkVcc, 0, pnlEclCmpMode_nbRow, 1, 1);
        constrain.set(pnlEclCmpMode, pnlEclCmpMode_chkTest, 1, pnlEclCmpMode_nbRow++, 1, 1);
        constrain.set(pnlEclOption, pnlEclCmpMode, 0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 0, 0);
        constrain.set(pnlEclOption, pnlEclGen, 0, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 0, 0);
        pnlEclOption.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Options:  "), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        pnlEclGen_lbl = new JLabel("Options:");
        pnlEclGen_chkCheck = new JCheckBox("-CHECK");
        pnlEclGen_chkES = new JCheckBox("-ES");
        pnlEclGen_chkG = new JCheckBox("-Debug");
        pnlEclGen_chkCheck.setMnemonic('h');
        pnlEclGen_chkES.setMnemonic('e');
        pnlEclGen_chkG.setMnemonic('g');
        pnlEclMoreOpt_btnDbgCpre = new JButton("Advanced ECL...");
        pnlEsMoreOpt_btnAdv = new JButton("Adv. Esterel...");
        pnlVccMoreOpt_btnAdv = new JButton("Advanced VCC...");
        pnlEclMoreOpt_btnDbgCpre.setMnemonic('n');
        pnlEsMoreOpt_btnAdv.setMnemonic('d');
        pnlVccMoreOpt_btnAdv.setMnemonic('c');
        constrain.set(pnlEclGen, pnlEclGen_lbl, 0, pnlEclGen_nbRow++, 1, 1);
        constrain.set(pnlEclGen, pnlEclGen_chkCheck, 0, pnlEclGen_nbRow, 1, 1);
        constrain.set(pnlEclGen, pnlEclGen_chkG, 1, pnlEclGen_nbRow, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 0, 10);
        constrain.set(pnlEclGen, pnlEclMoreOpt_btnDbgCpre, 2, pnlEclGen_nbRow, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 20, 0, 10);
        constrain.set(pnlEclGen, pnlVccMoreOpt_btnAdv, 3, pnlEclGen_nbRow++, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 20, 0, 10);
        constrain.set(pnlEclGen, pnlEsMoreOpt_btnAdv, 2, pnlEclGen_nbRow++, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 20, 0, 10);
        JPanel tmpPnlEclCmd = new JPanel();
        tmpPnlEclCmd.setLayout(gridbag);
        pnlEclCmd_txtArg = new JTextArea();
        pnlEclCmd_txtArg.setLineWrap(true);
        pnlEclCmd_txtArg.setWrapStyleWord(false);
        pnlEclCmd_scrPane = new JScrollPane(pnlEclCmd_txtArg);
        pnlEclCmd_scrPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tmpPnlEclCmd.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Commands:  "), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        constrain.set(tmpPnlEclCmd, pnlEclCmd_scrPane, 0, 0, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER, 1.0, 1.0, 0, 5, 5, 5);
        pnlEclCmd_btnRun = new JButton("Run");
        pnlEclCmd_btnRebuild = new JButton("Rebuild");
        pnlEclCmd_btnClear = new JButton("Clear");
        pnlEclCmd_btnSave = new JButton("Save...");
        pnlEclCmd_btnLoad = new JButton("Load...");
        pnlEclCmd_btnRun.setMnemonic('u');
        pnlEclCmd_btnRebuild.setMnemonic('b');
        pnlEclCmd_btnClear.setMnemonic('r');
        pnlEclCmd_btnSave.setMnemonic('a');
        pnlEclCmd_btnLoad.setMnemonic('o');
        pnlEclCmd_btnRun.setPreferredSize(dimButton);
        pnlEclCmd_btnRebuild.setPreferredSize(dimButton);
        pnlEclCmd_btnClear.setPreferredSize(dimButton);
        pnlEclCmd_btnSave.setPreferredSize(dimButton);
        pnlEclCmd_btnLoad.setPreferredSize(dimButton);
        constrain.set(pnlEclCmd, tmpPnlEclCmd, 0, 0, 1, 7, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 2, 10, 20);
        constrain.set(pnlEclCmd, pnlEclCmd_btnRun, 1, 1, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, 0.0, 0.0, 20, 0, 2, 10);
        constrain.set(pnlEclCmd, pnlEclCmd_btnRebuild, 1, 2, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, 0.0, 0.0, 0, 0, 2, 10);
        constrain.set(pnlEclCmd, pnlEclCmd_btnClear, 1, 3, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, 0.0, 0.0, 0, 0, 2, 10);
        constrain.set(pnlEclCmd, pnlEclCmd_btnSave, 2, 1, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, 0.0, 0.0, 20, 0, 2, 10);
        constrain.set(pnlEclCmd, pnlEclCmd_btnLoad, 2, 2, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, 0.0, 0.0, 0, 0, 2, 10);
        pnlEclCmd_scrPane.setMinimumSize(dimTxtArea);
        pnlEclCmd_scrPane.setMaximumSize(dimTxtArea);
        pnlEclCmd_scrPane.setPreferredSize(dimTxtArea);
        pnlOut_txtArea = new JTextArea(6, 55);
        pnlOut_txtArea.setEditable(false);
        pnlOut_scrPane = new JScrollPane(pnlOut_txtArea);
        pnlOut_scrPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        pnlOut_scrPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pnlOut.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Output:  "), BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        constrain.set(pnlOut, pnlOut_scrPane, 0, 0, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER, 1.0, 1.0, 0, 5, 5, 5);
        pnlRoot = new JPanel(new BorderLayout());
        pnlRoot.setLayout(gridbag);
        getContentPane().add(BorderLayout.CENTER, pnlRoot);
        JPanel pnlOpt = new JPanel();
        pnlOpt.setLayout(gridbag);
        constrain.set(pnlOpt, pnlEclOption, 0, 0, 2, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 1.0, 2, 0, 0, 2);
        constrain.set(pnlRoot, pnlEsEcl, 0, mainGrid_nbRow++, 2, 1, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 0.0, 0.0, 2, 10, 0, 2);
        constrain.set(pnlRoot, pnlOpt, 0, mainGrid_nbRow++, 2, 1, GridBagConstraints.BOTH, GridBagConstraints.WEST, 0.0, 0.0, 2, 10, 0, 2);
        constrain.set(pnlRoot, pnlEclCmd, 0, mainGrid_nbRow++, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.WEST, 1.0, 0.0, 2, 10, 0, 2);
        constrain.set(pnlRoot, pnlOut, 0, mainGrid_nbRow++, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.WEST, 1.0, 1.0, 2, 10, 0, 2);
        init();
        if (argCommandLine.length > 1 && !argXEcl.nt || argCommandLine.length > 0 && argXEcl.nt) {
            argXEcl.parserCommandLine(argCommandLine, true);
            displayGUI();
            buildCmd();
        }
        mnuFile_quit.addActionListener(guiActionEvents);
        mnuOption_reset.addActionListener(guiActionEvents);
        mnuOption_EsCompiler_esterel.addActionListener(guiActionEvents);
        mnuOption_EsCompiler_esterelv6.addActionListener(guiActionEvents);
        mnuOption_CCompiler_cl.addActionListener(guiActionEvents);
        mnuOption_CCompiler_cc.addActionListener(guiActionEvents);
        mnuOption_CCompiler_gcc.addActionListener(guiActionEvents);
        mnuOption_outputPrompt.addActionListener(guiActionEvents);
        mnuHelp_ecl_help.addActionListener(guiActionEvents);
        mnuHelp_ecl_version.addActionListener(guiActionEvents);
        mnuHelp_es_help.addActionListener(guiActionEvents);
        mnuHelp_es_version.addActionListener(guiActionEvents);
        mnuHelp_about.addActionListener(guiActionEvents);
        pnlEclCmd_btnRebuild.addActionListener(guiActionEvents);
        pnlEclCmd_btnRun.addActionListener(guiActionEvents);
        pnlEclCmd_btnClear.addActionListener(guiActionEvents);
        pnlEclCmd_btnSave.addActionListener(guiActionEvents);
        pnlEclCmd_btnLoad.addActionListener(guiActionEvents);
        pnlEclCmd_txtArg.addKeyListener(guiKeyEvents);
        pnlEsEcl_btnFilename.addActionListener(guiActionEvents);
        pnlEsEcl_txtFilename.addKeyListener(guiKeyEvents);
        pnlEsEcl_btnOutDir.addActionListener(guiActionEvents);
        pnlEsEcl_txtOutDir.addKeyListener(guiKeyEvents);
        pnlEsEcl_chkEs.addItemListener(guiItemEvents);
        pnlEsEcl_chkEcl.addItemListener(guiItemEvents);
        pnlEclCmpMode_chkEsterel.addItemListener(guiItemEvents);
        pnlEclCmpMode_chkVcc.addItemListener(guiItemEvents);
        pnlEclCmpMode_chkClean.addItemListener(guiItemEvents);
        pnlEclCmpMode_chkTest.addItemListener(guiItemEvents);
        pnlEclGen_chkCheck.addItemListener(guiItemEvents);
        pnlEclGen_chkES.addItemListener(guiItemEvents);
        pnlEclGen_chkG.addItemListener(guiItemEvents);
        pnlEclMoreOpt_btnDbgCpre.addActionListener(guiActionEvents);
        pnlEsMoreOpt_btnAdv.addActionListener(guiActionEvents);
        pnlVccMoreOpt_btnAdv.addActionListener(guiActionEvents);
        mnuFile_quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK, false));
        mnuOption_reset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.ALT_MASK, false));
        mnuHelp_about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK, false));
        xeclToolTips toolTips = new xeclToolTips();
        this.setResizable(true);
        this.pack();
    }

    public static void init() {
        initTab();
        str_chgDir = "";
        str_CPreCmd = "";
        str_eclCmd = "";
        str_esCmd = "";
        str_clObjCmd = "";
        str_clLinkCmd = "";
        str_exeSimulCmd = "";
        str_compareCmd = "";
        str_vccImportCmd = "";
        str_copyCmd = new String[6];
        str_copyCmd[0] = "";
        tabCmd = new String[20];
        nbCmd = 0;
        EclAdv_vctI = new Vector(0);
        EclAdv_vctDMacro = new Vector(0);
        EclAdv_vctDValue = new Vector(0);
        Vcc_vctTypelib_lib = new Vector(0);
        Vcc_vctTypelib_type = new Vector(0);
        Cmp = CMP_ECL;
        if (argXEcl.nt) CC = Const.CL_CMP; else CC = Const.CC_CMP;
        Ecl_strFilename = "";
        Ecl_strFilePath = "";
        Ecl_strOutDir = "";
        Ecl_strOldOutDir = "";
        Es_strFilename = "";
        base = null;
        if (argXEcl.nt) esterelCmpVersion = Const.ESTEREL_CMP_V6; else esterelCmpVersion = Const.ESTEREL_CMP_V5;
        if (esterelCmpVersion == Const.ESTEREL_CMP_V5) mnuOption_EsCompiler_esterel.setSelected(true);
        if (esterelCmpVersion == Const.ESTEREL_CMP_V6) mnuOption_EsCompiler_esterelv6.setSelected(true);
        mnuOption_outputPrompt.setSelected(true);
        wlcDir = "";
        mnuHelp_flgEclVersion = false;
        mnuHelp_flgEclHelp = false;
        mnuHelp_flgEsVersion = false;
        mnuHelp_flgEsHelp = false;
        flg_OutputPrompt = true;
        flg_simulPresent = false;
        flg_launchEsInEcl = false;
        flg_eclCmp = true;
        flg_esCmp = false;
        flg_newCompilation = true;
        flg_errCompilation = false;
        flg_esTxtArgChanged = false;
        flg_eclTxtArgChanged = false;
        flg_EsterelOpt = false;
        flg_EsterelAloneOpt = false;
        flg_VccOpt = false;
        flg_VccImport = false;
        flg_TestOpt = false;
        wlcDir = "";
        wlcvDir = "";
        resetAllOptions();
        resetOptionVariables();
        if (System.getProperties().getProperty("ESTEREL") == null) mnuOption_EsCompiler_esterel.setEnabled(false);
        if (System.getProperties().getProperty("ESTERELV6") == null) mnuOption_EsCompiler_esterelv6.setEnabled(false);
    }

    protected static void EclCmpModeEnabled(boolean state) {
        pnlEclCmpMode_chkClean.setEnabled(state);
        pnlEclCmpMode_chkEsterel.setEnabled(state);
        pnlEclCmpMode_chkVcc.setEnabled(state);
        pnlEclCmpMode_chkTest.setEnabled(state);
    }

    protected static void buildCmd() {
        pnlEclCmd_txtArg.setText("");
        str_chgDir = "";
        str_CPreCmd = "";
        str_eclCmd = "";
        str_esCmd = "";
        str_clObjCmd = "";
        str_clLinkCmd = "";
        str_exeSimulCmd = "";
        str_compareCmd = "";
        str_vccImportCmd = "";
        str_copyCmd[0] = "";
        nbCmd = 0;
        if (!pnlEsEcl_txtOutDir.getText().equals(".") && !pnlEsEcl_txtOutDir.toString().toUpperCase().equals(Ecl_strFilePath)) Ecl_strOutDir = pnlEsEcl_txtOutDir.getText(); else Ecl_strOutDir = "";
        if (flg_eclCmp) {
            if (!Ecl_strOutDir.equals("") && !Ecl_strOutDir.equals(Ecl_strFilePath) && !Ecl_strFilePath.equals("")) {
                if (argXEcl.nt) str_chgDir = "copy "; else str_chgDir = "cp ";
                if (!Ecl_strFilePath.equals("")) str_chgDir += Ecl_strFilePath + dirsep;
                str_chgDir += base + ".ecl " + Ecl_strOutDir + dirsep;
            }
            if (flg_TestOpt) {
                str_CPreCmd = buildCCmd(Const.CPRE);
                str_eclCmd = buildEclCmd();
                str_esCmd = buildEsCmd();
                str_clObjCmd = buildCCmd(Const.C2OBJ);
                str_clLinkCmd = buildCCmd(Const.LINK);
                if (!Ecl_strOutDir.equals("")) str_exeSimulCmd += Ecl_strOutDir + dirsep;
                str_exeSimulCmd += base + ".exe" + " -novarcheck ";
                if (!Ecl_strOutDir.equals("")) str_exeSimulCmd += Ecl_strOutDir + dirsep;
                str_exeSimulCmd += base + ".in > ";
                if (!Ecl_strOutDir.equals("")) str_exeSimulCmd += Ecl_strOutDir + dirsep;
                str_exeSimulCmd += base + ".test";
                if (argXEcl.nt) str_compareCmd = "fc "; else str_compareCmd = "cmp ";
                if (!Ecl_strOutDir.equals("")) str_compareCmd += Ecl_strOutDir + dirsep;
                str_compareCmd += base + ".gd ";
                if (!Ecl_strOutDir.equals("")) str_compareCmd += Ecl_strOutDir + dirsep;
                str_compareCmd += base + ".test";
            }
            if (flg_EsterelOpt) {
                str_CPreCmd = buildCCmd(Const.CPRE);
                str_eclCmd = buildEclCmd();
                str_esCmd = buildEsCmd();
                str_clObjCmd = buildCCmd(Const.C2OBJ);
                str_clLinkCmd = buildCCmd(Const.LINK);
            }
            if (flg_VccOpt) {
                wlcDir = Vcc_strWorkspace + dirsep + Vcc_strLibname + dirsep + Vcc_strCellname;
                wlcvDir = Vcc_strWorkspace + dirsep + Vcc_strLibname + dirsep + Vcc_strCellname + dirsep + Const.VCC_ECL_DIR;
                Ecl_strOutDir = wlcvDir;
                pnlEsEcl_txtOutDir.setText(Ecl_strOutDir);
                str_CPreCmd = buildCCmd(Const.CPRE);
                str_eclCmd = buildEclCmd();
                str_esCmd = buildEsCmd();
                if (!tabOptionEcl[Const.NOIMPORT]) {
                    flg_VccImport = false;
                    str_vccImportCmd = Const.VCC_IMPORT_NT + " -LCV " + Vcc_strLibname + "." + Vcc_strCellname;
                    str_vccImportCmd += ":" + Const.VCC_WHTBOX_DIR + Const.VCC_IMPORT_OPT;
                    str_vccImportCmd += wlcvDir + dirsep + Const.VCC_SYMBOL_OPT;
                } else {
                    int ind = 0;
                    String cmdCopy;
                    if (argXEcl.nt) cmdCopy = "copy "; else cmdCopy = "cp ";
                    if (pnlEclGen_chkG.isSelected()) if (argXEcl.nt) str_clObjCmd = CC + Const.COPY_VCC_DEBUG_NT + wlcvDir + dirsep + base + ".c"; else str_clObjCmd = CC + Const.COPY_VCC_DEBUG_UX + wlcvDir + dirsep + base + ".c";
                    str_copyCmd[ind++] = cmdCopy + wlcDir + dirsep + Const.VCC_ECL_DIR + dirsep + base + ".ecl " + wlcDir + dirsep + Const.VCC_WHTBOX_DIR + dirsep;
                    if (pnlEclGen_chkG.isSelected()) {
                        str_copyCmd[ind] = cmdCopy;
                        if (!Ecl_strOutDir.equals("")) str_copyCmd[ind] += Ecl_strOutDir + dirsep;
                        str_copyCmd[ind++] += base + ".obj " + wlcDir + dirsep + Const.VCC_WHTBOX_DIR + dirsep;
                    }
                    str_copyCmd[ind++] = cmdCopy + wlcDir + dirsep + Const.VCC_ECL_DIR + dirsep + base + ".c " + wlcDir + dirsep + Const.VCC_WHTBOX_DIR + dirsep;
                    str_copyCmd[ind++] = cmdCopy + wlcDir + dirsep + Const.VCC_ECL_DIR + dirsep + base + ".h " + wlcDir + dirsep + Const.VCC_WHTBOX_DIR + dirsep;
                    str_copyCmd[ind++] = cmdCopy + wlcDir + dirsep + Const.VCC_ECL_DIR + dirsep + "white.c " + wlcDir + dirsep + Const.VCC_WHTBOX_DIR + dirsep;
                    str_copyCmd[ind++] = cmdCopy + wlcDir + dirsep + Const.VCC_ECL_DIR + dirsep + Const.VCC_LIBRARY_FILE + " " + wlcDir + dirsep + Const.VCC_WHTBOX_DIR + dirsep;
                }
            }
            if (!pnlEclCmpMode_chkEsterel.isSelected() && !pnlEclCmpMode_chkVcc.isSelected() && !pnlEclCmpMode_chkTest.isSelected()) {
                str_eclCmd = buildEclCmd();
            }
        } else if (flg_esCmp) {
            str_esCmd = buildEsCmd();
            if (str_esCmd.indexOf("-simul ") != -1 && !tabOptionEs[Const.ES_SIMUL] || str_esCmd.indexOf("-simul ") == -1 && tabOptionEs[Const.ES_SIMUL]) {
                str_clObjCmd = buildCCmd(Const.C2OBJ);
            } else str_clObjCmd = "";
        }
        if (!str_chgDir.equals("")) {
            pnlEclCmd_txtArg.append(str_chgDir + "\n");
            tabCmd[nbCmd++] = str_chgDir;
        }
        if (!str_CPreCmd.equals("")) {
            pnlEclCmd_txtArg.append(str_CPreCmd + "\n");
            tabCmd[nbCmd++] = str_CPreCmd;
        }
        if (!str_eclCmd.equals("")) {
            pnlEclCmd_txtArg.append(str_eclCmd + "\n");
            tabCmd[nbCmd++] = str_eclCmd;
        }
        if (!str_esCmd.equals("")) {
            pnlEclCmd_txtArg.append(str_esCmd + "\n");
            tabCmd[nbCmd++] = str_esCmd;
        }
        if (!str_clObjCmd.equals("")) {
            pnlEclCmd_txtArg.append(str_clObjCmd + "\n");
            tabCmd[nbCmd++] = str_clObjCmd;
        }
        if (!str_vccImportCmd.equals("")) {
            pnlEclCmd_txtArg.append(str_vccImportCmd + "\n");
            tabCmd[nbCmd++] = str_vccImportCmd;
        }
        if (!str_clLinkCmd.equals("")) {
            pnlEclCmd_txtArg.append(str_clLinkCmd + "\n");
            tabCmd[nbCmd++] = str_clLinkCmd;
        }
        if (!str_exeSimulCmd.equals("")) {
            pnlEclCmd_txtArg.append(str_exeSimulCmd + "\n");
            tabCmd[nbCmd++] = str_exeSimulCmd;
        }
        if (!str_compareCmd.equals("")) {
            pnlEclCmd_txtArg.append(str_compareCmd + "\n");
            tabCmd[nbCmd++] = str_compareCmd;
        }
        if (!str_copyCmd[0].equals("")) {
            for (int i = 0; i < 5; i++) {
                pnlEclCmd_txtArg.append(str_copyCmd[i] + "\n");
                tabCmd[nbCmd++] = str_copyCmd[i];
            }
            if (pnlEclGen_chkG.isSelected()) {
                pnlEclCmd_txtArg.append(str_copyCmd[5] + "\n");
                tabCmd[nbCmd++] = str_copyCmd[5];
            }
        }
        pnlEclCmd_txtArg.setForeground(Color.black);
    }

    protected static String buildCCmd(int type) {
        String arg;
        if (argXEcl.nt && CC == Const.CL_CMP) arg = Const.CL_NOLOGO_OPT; else arg = "";
        if (type == Const.CPRE) {
            if (argXEcl.nt && CC != Const.GCC_CMP) arg += Const.CL_MORE_OPT_NT; else if (argXEcl.linux || CC == Const.GCC_CMP) arg += Const.CPRE_OPT_LINUX; else arg += Const.CPRE_OPT_UX;
            if (flg_EsterelOpt || flg_TestOpt) {
                if (argXEcl.nt) arg += " /D"; else arg += " -D";
                arg += Const.MACRO_SIMUL_OPT;
            }
            if (tabOptionEcl[Const.I]) {
                for (int i = 0; i < EclAdv_vctI.size(); i++) {
                    if (argXEcl.nt) arg += " /I"; else arg += " -I";
                    arg += (String) EclAdv_vctI.elementAt(i);
                }
            }
            if (tabOptionEcl[Const.D]) {
                for (int i = 0; i < EclAdv_vctDMacro.size(); i++) {
                    if (argXEcl.nt) arg += " /D"; else arg += " -D";
                    arg += (String) EclAdv_vctDMacro.elementAt(i) + "=" + (String) EclAdv_vctDValue.elementAt(i) + " ";
                }
            }
            if (argXEcl.linux || CC == Const.GCC_CMP) arg += " -x c ";
            if (flg_EsterelOpt || flg_TestOpt) {
                if (!Ecl_strOutDir.equals("")) arg += Ecl_strOutDir + dirsep;
                arg += base + ".ecl > ";
                if (!Ecl_strOutDir.equals("")) arg += Ecl_strOutDir + dirsep;
                arg += base + ".i";
            }
            if (flg_VccOpt) {
                if (!wlcvDir.equals("")) arg += wlcvDir + dirsep + base + ".ecl > " + wlcvDir + dirsep + base + ".i";
            }
        } else if (type == Const.C2OBJ) {
            if (flg_TestOpt || flg_EsterelOpt || flg_esCmp) {
                if (argXEcl.nt) {
                    arg += " /D" + Const.MACRO_SIMUL_OPT + " /c ";
                    if (!Ecl_strOutDir.equals("")) arg += Ecl_strOutDir + dirsep;
                    arg += base + ".c";
                    if (!Ecl_strOutDir.equals("")) arg += " /Fo" + Ecl_strOutDir + dirsep + base + ".obj";
                } else {
                    arg += Const.CCOMPILER_OPT_OBJ_UX + Ecl_strOutDir + dirsep + base + ".c";
                    if (!Ecl_strOutDir.equals("")) arg += " -o" + Ecl_strOutDir + dirsep + base + ".o";
                }
            }
        } else if (type == Const.LINK) {
            if (flg_TestOpt || flg_EsterelOpt) {
                if (argXEcl.nt) {
                    arg += " /D" + Const.MACRO_SIMUL_OPT;
                    if (!Ecl_strOutDir.equals("")) arg += Ecl_strOutDir + dirsep;
                    arg += base + ".obj";
                    if (!Ecl_strOutDir.equals("")) arg += " /Fe" + Ecl_strOutDir + dirsep + base + ".exe ";
                    arg += Const.CCOMPILER_OPT_LINK1_NT;
                } else arg += Const.CCOMPILER_OPT_LINK_UX + base + ".exe " + base + ".o ";
                if (esterelCmpVersion == Const.ESTEREL_CMP_V5) arg += System.getProperties().getProperty("ESTEREL");
                if (esterelCmpVersion == Const.ESTEREL_CMP_V6) arg += System.getProperties().getProperty("ESTERELV6");
                if (argXEcl.nt) arg += Const.CCOMPILER_OPT_LINK2_NT; else arg += Const.CCOMPILER_OPT_LIB;
            }
        }
        return (CC + arg);
    }

    protected static String buildEsCmd() {
        String compiler = "";
        String arg = "";
        boolean tabOption[] = new boolean[Const.ES_NBOPTION];
        for (int i = 0; i < Const.ES_NBOPTION; i++) {
            if (flg_esCmp) tabOption[i] = tabOptionEsAlone[i]; else tabOption[i] = tabOptionEs[i];
        }
        if (tabOption[Const.ES_SIMUL]) arg += " -simul";
        if (tabOption[Const.ES_STRLIC]) arg += " -strlic:" + (flg_esCmp ? EsA_strStrlic : Es_strStrlic);
        if (!flg_esCmp && (flg_EsterelOpt || flg_TestOpt || flg_VccOpt)) {
            if (flg_VccOpt) arg += " -D " + wlcvDir + dirsep;
        }
        if (tabOption[Const.ES_L]) arg += " -L";
        if (tabOption[Const.ES_A]) arg += " -A";
        if (tabOption[Const.ES_I]) arg += " -F";
        if (tabOption[Const.ES_CAUSAL]) arg += " -causal";
        if (tabOption[Const.ES_SINGLE]) arg += " -single";
        if (tabOption[Const.ES_CYCLES]) arg += " -cycles";
        if (tabOption[Const.ES_SUB]) arg += " -sub";
        if (tabOption[Const.ES_MOD]) arg += " -mod";
        if (tabOption[Const.ES_MODULE]) arg += " -module";
        if (tabOption[Const.ES_W]) arg += " -w";
        if (tabOption[Const.ES_WW]) arg += " -W";
        if (tabOption[Const.ES_STAT]) arg += " -stat";
        if (tabOption[Const.ES_SIZE]) arg += " -size";
        if (tabOption[Const.ES_S]) arg += " -s";
        if (tabOption[Const.ES_IC]) arg += " -ic";
        if (tabOption[Const.ES_LC]) arg += " -lc";
        if (tabOption[Const.ES_SC]) arg += " -sc";
        if (tabOption[Const.ES_SSC]) arg += " -ssc";
        if (tabOption[Const.ES_OC]) arg += " -oc";
        if (tabOption[Const.ES_K]) arg += " -K";
        if (tabOption[Const.ES_D]) arg += " -D:" + (flg_esCmp ? EsA_strD : Es_strD);
        if (tabOption[Const.ES_ICLINK]) arg += " -iclink:" + (flg_esCmp ? EsA_strIclink : Es_strIclink);
        if (tabOption[Const.ES_LCSC]) arg += " -lcsc:" + (flg_esCmp ? EsA_strLcsc : Es_strLcsc);
        if (tabOption[Const.ES_SCSSC]) arg += " -scssc:" + (flg_esCmp ? EsA_strScssc : Es_strScssc);
        if (tabOption[Const.ES_SCCAUSAL]) arg += " -sccausal:" + (flg_esCmp ? EsA_strSccausal : Es_strSccausal);
        if (tabOption[Const.ES_SCOC]) arg += " -scoc:" + (flg_esCmp ? EsA_strScoc : Es_strScoc);
        if (flg_esCmp) {
            if (tabOption[Const.ES_FILENAME]) if (Ecl_strOutDir.equals("")) arg += " " + Es_strFilename; else arg += "-D " + Ecl_strOutDir + dirsep + " " + Ecl_strOutDir + dirsep + base + ".strl"; else if (!arg.equals("")) arg += " <filename.strl>";
        } else {
            if (flg_EsterelOpt || flg_TestOpt) {
                if (base != null) if (Ecl_strOutDir.equals("")) arg += " " + base + ".strl"; else arg += " -D " + Ecl_strOutDir + dirsep + " " + Ecl_strOutDir + dirsep + base + ".strl"; else arg += " <filename.strl>";
            } else if (flg_VccOpt) {
                arg += " " + wlcvDir + dirsep + base + ".strl";
            }
        }
        flg_esTxtArgChanged = false;
        if (!arg.equals("")) {
            if (esterelCmpVersion == Const.ESTEREL_CMP_V6) compiler = Const.CMP_ESTERELV6; else compiler = Const.CMP_ESTEREL;
            return (compiler + arg);
        } else return "";
    }

    protected static String buildEclCmd() {
        Cmp = CMP_ECL;
        String arg = "";
        if (tabOptionEcl[Const.CLEAN]) arg += " -CLEAN"; else {
            if (flg_EsterelOpt || flg_TestOpt || flg_VccOpt) {
                arg += " -NOCPP";
                tabOptionEcl[Const.NOCPP] = true;
            } else tabOptionEcl[Const.NOCPP] = false;
            if (tabOptionEcl[Const.ES]) arg += " -ES";
            if (tabOptionEcl[Const.G]) arg += " -G";
            if (tabOptionEcl[Const.F]) arg += " -F";
            if (tabOptionEcl[Const.E]) arg += " -E";
            if (tabOptionEcl[Const.CHECK]) arg += " -CHECK";
            if (!flg_EsterelOpt && !flg_TestOpt && !flg_VccOpt) {
                if (tabOptionEcl[Const.NOCPP]) arg += " -NOCPP";
                if (tabOptionEcl[Const.I]) for (int i = 0; i < EclAdv_vctI.size(); i++) arg += " -I " + (String) EclAdv_vctI.elementAt(i);
                if (tabOptionEcl[Const.D]) for (int i = 0; i < EclAdv_vctDMacro.size(); i++) arg += " -D " + (String) EclAdv_vctDMacro.elementAt(i) + " " + (String) EclAdv_vctDValue.elementAt(i);
            }
            if (tabOptionEcl[Const.PD]) arg += " -PD";
            if (tabOptionEcl[Const.CD]) arg += " -CD";
            if (tabOptionEcl[Const.NG]) arg += " -NG";
        }
        if (flg_VccOpt) {
            arg += " -VCC_XECL " + Vcc_strLibname + "." + Vcc_strCellname;
            if (!Vcc_strWorkspace.equals("")) arg += " -WORKSPACE " + Vcc_strWorkspace;
            if (!Vcc_strMainmodule.equals("")) arg += " -MAINMODULE " + Vcc_strMainmodule;
            if (!Vcc_strDfttypelib.equals("")) arg += " -DEFAULTTYPELIB " + Vcc_strDfttypelib;
            if (!Vcc_strTypelibfile.equals("")) arg += " -TYPELIBFILE " + Vcc_strTypelibfile;
            for (int i = 0; i < Vcc_vctTypelib_lib.size(); i++) arg += " -TYPELIB " + Vcc_vctTypelib_lib.elementAt(i) + " " + Vcc_vctTypelib_type.elementAt(i);
            if (Vcc_Noimport) arg += " -NOIMPORT";
            if (Vcc_Importtypes) arg += " -IMPORTTYPES";
        }
        pnlEclCmd_txtArg.setForeground(Color.black);
        flg_eclTxtArgChanged = false;
        if (tabOptionEcl[Const.FILENAME]) if (Ecl_strOutDir.equals("") || flg_VccOpt) arg += " " + Ecl_strFilename; else arg += " " + Ecl_strOutDir + dirsep + base + ".ecl"; else if (!arg.equals("")) arg += " <filename>";
        if (!arg.equals("")) return ("ecl" + arg); else return ("");
    }

    static void compilerManager() {
        boolean flg_launchCompil = true;
        if (!flg_eclTxtArgChanged) {
            if (pnlEsEcl_txtFilename.getText().equals("")) {
                endCompilationMsg(Const.CMP_NOFILE);
                flg_launchCompil = false;
            }
        } else {
            if (isCmdEmpty()) {
                endCompilationMsg(Const.CMP_NOCMD);
                flg_launchCompil = false;
            }
        }
        if (flg_launchCompil) {
            flg_newCompilation = true;
            for (int i = 0; i < nbCmd; i++) {
                if (!flg_errCompilation) {
                    try {
                        runCmd(tabCmd[i]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (flg_VccImport) {
                try {
                    PrintWriter tmp = new PrintWriter(new FileOutputStream(wlcDir + dirsep + "wht_ecl" + dirsep + "master.tag"));
                    tmp.write("white.c\n");
                    tmp.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                flg_VccImport = false;
            }
            if (flg_errCompilation) {
                endCompilationMsg(Const.CMP_ERR);
                flg_errCompilation = false;
            } else {
                endCompilationMsg(Const.CMP_OK);
            }
        }
    }

    static void runCmd(String cmdLine) throws java.lang.Exception, java.io.IOException {
        String[] cmd = new String[2];
        if (argXEcl.nt) {
            cmd[0] = "cmd";
            cmd[1] = "/c";
        } else {
            cmd[0] = "/bin/sh";
            cmd[1] = "-c";
        }
        String[] command = { cmd[0], cmd[1], cmdLine };
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(command);
        PrintWriter outf;
        outf = new PrintWriter(System.out);
        InputStream procStdout = p.getInputStream();
        InputStream procStderr = p.getErrorStream();
        int exit = 0, n1, n2;
        boolean processEnded = false;
        if (flg_newCompilation) {
            pnlOut_txtArea.setText("");
            flg_newCompilation = false;
        }
        if (flg_OutputPrompt) System.out.println(cmdLine);
        if (!mnuHelp_flgEclHelp && !mnuHelp_flgEclVersion && !mnuHelp_flgEsHelp && !mnuHelp_flgEsVersion) pnlOut_txtArea.append(cmdLine + "\n");
        while (!processEnded) {
            xecl.f.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            pnlEclCmd_txtArg.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            pnlOut_txtArea.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            try {
                exit = p.exitValue();
                processEnded = true;
                xecl.f.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                pnlEclCmd_txtArg.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                pnlOut_txtArea.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            } catch (IllegalThreadStateException e) {
            }
            n1 = procStdout.available();
            if (n1 > 0) {
                for (int i = 0; i < n1; i++) {
                    int b = procStdout.read();
                    if (b < 0) break;
                    outf.write(b);
                    if (mnuHelp_flgEclVersion) {
                        if (b == 10 || b == 13) EclCmpVersion += "\n"; else EclCmpVersion += (char) b;
                    } else if (mnuHelp_flgEsVersion) {
                        EsCmpVersion += (char) b;
                    }
                    if (!mnuHelp_flgEclVersion && !mnuHelp_flgEsHelp && !mnuHelp_flgEsVersion && !mnuHelp_flgEclHelp) {
                        pnlOut_txtArea.setText(pnlOut_txtArea.getText() + "" + (char) b);
                        if (flg_OutputPrompt) {
                            System.err.print((char) b);
                            System.err.flush();
                        }
                    }
                }
            }
            n2 = procStderr.available();
            if (n2 > 0) {
                byte[] pbytes = new byte[n2];
                procStderr.read(pbytes);
                if (mnuHelp_flgEclHelp) EclHelp += new String(pbytes); else if (mnuHelp_flgEsVersion) EsCmpVersion += new String(pbytes); else if (mnuHelp_flgEsHelp) EsHelp += new String(pbytes);
                if (!mnuHelp_flgEclVersion && !mnuHelp_flgEsHelp && !mnuHelp_flgEsVersion && !mnuHelp_flgEclHelp) {
                    pnlOut_txtArea.append(new String(pbytes));
                    if (flg_OutputPrompt) {
                        System.err.print(new String(pbytes));
                        System.err.flush();
                    }
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
        do {
            n1 = procStdout.available();
            if (n1 > 0) {
                for (int i = 0; i < n1; i++) {
                    int b = procStdout.read();
                    if (b < 0) break;
                    outf.write(b);
                    pnlOut_txtArea.append("" + (char) b);
                    if (flg_OutputPrompt) {
                        System.err.print((char) b);
                        System.err.flush();
                    }
                }
            }
            n2 = procStderr.available();
            if (n2 > 0) {
                byte[] pbytes = new byte[n2];
                procStderr.read(pbytes);
                pnlOut_txtArea.append(new String(pbytes));
                if (flg_OutputPrompt) {
                    System.err.print(new String(pbytes));
                    System.err.flush();
                }
            }
        } while (n1 > 0 || n2 > 0);
        if (exit != 0) {
            if (!mnuHelp_flgEclVersion) {
                flg_errCompilation = true;
                pnlOut_txtArea.append("Error " + exit + " during subprocess execution ");
                if (flg_OutputPrompt) System.err.println("Error " + exit + " during subprocess execution");
            }
        }
    }

    static JFrame f;

    static WindowMonitor xeclWindowEvents;

    public static void main(String[] args) throws java.io.IOException, java.lang.Exception {
        eclInst = new ecl();
        argXEcl = new common_ecl_xecl();
        argXEcl.setOS();
        argCommandLine = args;
        if (argXEcl.nt) dirsep = "\\"; else dirsep = "/";
        f = new xecl("xecl");
        xeclWindowEvents = new WindowMonitor();
        f.addWindowListener(xeclWindowEvents);
        try {
            f.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        Dimension screenSize = f.getToolkit().getScreenSize();
        int top = 50;
        int left = screenSize.width / 2 - f.getWidth() / 2;
        f.setLocation(left, top);
    }

    public static void resetVccOptions() {
        tabOptionEcl[Const.VCCLIB] = false;
        tabOptionEcl[Const.MAINMODULE] = false;
        tabOptionEcl[Const.DEFAULTTYPELIB] = false;
        tabOptionEcl[Const.TYPELIB] = false;
        tabOptionEcl[Const.TYPELIBFILE] = false;
        tabOptionEcl[Const.WORKSPACE] = false;
        tabOptionEcl[Const.IMPORTTYPES] = false;
        tabOptionEcl[Const.NOIMPORT] = false;
        tabOptionEcl[Const.VCCCELL] = false;
        resetOptionVariables();
    }

    public static void resetEclCmpModeOptions() {
        tabOptionEcl[Const.VCC] = false;
        tabOptionEcl[Const.ESTEREL] = false;
        tabOptionEcl[Const.CLEAN] = false;
        tabOptionEcl[Const.G] = false;
        pnlEclCmpMode_chkEsterel.setSelected(false);
        pnlEclCmpMode_chkClean.setSelected(false);
        pnlEclCmpMode_chkVcc.setSelected(false);
        pnlEclGen_chkG.setSelected(false);
        pnlEsMoreOpt_btnAdv.setEnabled(false);
        pnlVccMoreOpt_btnAdv.setEnabled(false);
    }

    public static void resetGenOptions() {
        tabOptionEcl[Const.P] = false;
        tabOptionEcl[Const.F] = false;
        tabOptionEcl[Const.C] = false;
        tabOptionEcl[Const.E] = false;
        tabOptionEcl[Const.CHECK] = false;
        tabOptionEcl[Const.TEST] = false;
    }

    public static void resetEclDbgOptions() {
        tabOptionEcl[Const.NOCPP] = false;
        tabOptionEcl[Const.PD] = false;
        tabOptionEcl[Const.CD] = false;
        tabOptionEcl[Const.NG] = false;
    }

    public static void resetEclCpreOptions() {
        tabOptionEcl[Const.D] = false;
        tabOptionEcl[Const.I] = false;
    }

    private static void resetOptionVariables() {
        Vcc_strCellname = "";
        Vcc_strLibname = "";
        Vcc_strWorkspace = "";
        Vcc_strTypelibfile = "";
        Vcc_strMainmodule = "";
        Vcc_strDfttypelib = "";
        Vcc_Noimport = false;
        Vcc_Importtypes = false;
        Es_strD = "";
        Es_strIclink = "";
        Es_strLcsc = "";
        Es_strScssc = "";
        Es_strSccausal = "";
        Es_strScoc = "";
        EsA_strD = "";
        EsA_strStrlic = "";
        EsA_strIclink = "";
        EsA_strLcsc = "";
        EsA_strScssc = "";
        EsA_strSccausal = "";
        EsA_strScoc = "";
    }

    public static void resetAllOptions() {
        pnlEsEcl_chkEcl.setSelected(true);
        resetVccOptions();
        resetGenOptions();
        resetEclDbgOptions();
        resetEclCmpModeOptions();
        resetEclCpreOptions();
        tabOptionEcl[Const.FILENAME] = false;
        pnlEsEcl_txtFilename.setText("");
        pnlEsEcl_txtOutDir.setText(".");
        pnlOut_txtArea.setText("");
        pnlEclCmd_txtArg.setText("");
        pnlEclMoreOpt_btnDbgCpre.setEnabled(true);
    }

    private static void endCompilationMsg(int msgType) {
        String msg = "";
        if (msgType == Const.CMP_ERR) {
            msg = "Error during compilation\nSee the Output window";
            flg_errCompilation = false;
        }
        if (msgType == Const.CMP_NOFILE) {
            msg = "No input file";
        }
        if (msgType == Const.CMP_OK) {
            msg = "Compilation successful";
        }
        if (msgType == Const.CMP_NOCMD) {
            msg = "Nothing to compile";
        }
        InfoDialog DlgEndCmp = new InfoDialog(xecl.f, "End Compilation", msg);
        DlgEndCmp.setModal(true);
        DlgEndCmp.setVisible(true);
        flg_newCompilation = true;
    }

    public static void setModifiedCmd() {
        char txtCmd[];
        char tmpString[] = new char[256];
        int j = 0;
        int lg;
        nbCmd = 0;
        lg = pnlEclCmd_txtArg.getText().length();
        txtCmd = new char[lg + 1];
        txtCmd = pnlEclCmd_txtArg.getText().toCharArray();
        if (lg != 0) if (txtCmd[lg - 1] != '\n') {
            pnlEclCmd_txtArg.append("\n");
            txtCmd = pnlEclCmd_txtArg.getText().toCharArray();
            lg++;
        }
        for (int i = 0; i < lg; i++) {
            if ((int) txtCmd[i] == '\n') {
                tmpString[j] = '\0';
                char tmp[] = new char[j];
                for (int k = 0; k < j; k++) tmp[k] = tmpString[k];
                tabCmd[nbCmd++] = new String(tmp);
                j = 0;
            } else tmpString[j++] = txtCmd[i];
        }
    }

    private static boolean isCmdEmpty() {
        boolean flg_isEmpty = true;
        int lg = pnlEclCmd_txtArg.getText().length();
        char str[] = new char[lg];
        str = pnlEclCmd_txtArg.getText().toCharArray();
        for (int i = 0; i < lg; i++) if (isChar(str[i])) flg_isEmpty = false;
        return flg_isEmpty;
    }

    private static boolean isChar(char c) {
        boolean flg_isChar = false;
        if (c >= '0' && c <= '9') flg_isChar = true;
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) flg_isChar = true;
        return flg_isChar;
    }

    private static void displayGUI() {
        if (tabOptionEcl[Const.D]) {
            EclAdv_vctDMacro = new Vector(argXEcl.macros);
            EclAdv_vctDValue = new Vector(argXEcl.values);
        }
        if (tabOptionEcl[Const.I]) EclAdv_vctI = new Vector(argXEcl.includes);
        if (tabOptionEcl[Const.CHECK]) pnlEclGen_chkCheck.setSelected(true);
        if (tabOptionEcl[Const.G]) pnlEclGen_chkG.setSelected(true);
        if (tabOptionEcl[Const.ESTEREL]) {
            pnlEclCmpMode_chkEsterel.setSelected(true);
            flg_EsterelOpt = true;
            tabOptionEs[Const.ES_SIMUL] = true;
            tabOptionEs[Const.ES_STRLIC] = true;
            Es_strStrlic = "-shared_var ";
        }
        if (tabOptionEcl[Const.TEST]) {
            pnlEclCmpMode_chkTest.setSelected(true);
            flg_TestOpt = true;
        }
        if (tabOptionEcl[Const.VCC]) {
            if (argXEcl.cellib != null) {
                StringTokenizer celltok = new StringTokenizer(argXEcl.cellib, ".");
                String lib = celltok.nextToken();
                Vcc_strLibname = new String(lib);
                Vcc_strCellname = celltok.nextToken();
            }
            if (argXEcl.workspace == null) {
                pnlOut_txtArea.setText("Warning: assuming c:\\MyWorkspace as workspace");
                Vcc_strWorkspace = new String("c:\\MyWorkspace");
            } else Vcc_strWorkspace = new String(argXEcl.workspace);
            pnlEclCmpMode_chkVcc.setSelected(true);
            xecl.pnlVccMoreOpt_btnAdv.setEnabled(true);
            flg_VccOpt = true;
        }
        if (tabOptionEcl[Const.DEFAULTTYPELIB]) Vcc_strDfttypelib = argXEcl.typelibTable.get("*").toString();
        if (tabOptionEcl[Const.TYPELIBFILE]) Vcc_strTypelibfile = argXEcl.typelibFile;
        if (tabOptionEcl[Const.MAINMODULE]) Vcc_strMainmodule = argXEcl.mainmodule;
        if (tabOptionEcl[Const.CLEAN]) pnlEclCmpMode_chkClean.setSelected(true);
        if (tabOptionEcl[Const.ES]) pnlEclGen_chkES.setSelected(true);
        if (tabOptionEcl[Const.FILENAME]) {
            if (argXEcl.nt) Ecl_strFilename = new String(argXEcl.name); else Ecl_strFilename = argXEcl.name.substring(0, argXEcl.name.length() - 1);
            pnlEsEcl_txtFilename.setText(Ecl_strFilename);
            StringTokenizer tokenizer = new StringTokenizer(xecl.pnlEsEcl_txtFilename.getText(), ".");
            xecl.base = tokenizer.nextToken();
        }
        if (pnlEclCmpMode_chkClean.isSelected()) {
            pnlEclCmpMode_chkClean.setEnabled(true);
            pnlEsMoreOpt_btnAdv.setEnabled(false);
            xecl.EclCmpModeEnabled(false);
            pnlEclCmpMode_chkTest.setEnabled(false);
            pnlEclCmpMode_chkClean.setEnabled(true);
        } else if (pnlEclGen_chkES.isSelected() || pnlEclCmpMode_chkEsterel.isSelected() || pnlEclCmpMode_chkTest.isSelected() || pnlEclCmpMode_chkVcc.isSelected()) {
            pnlEclGen_chkES.setEnabled(true);
            pnlEclCmpMode_chkTest.setEnabled(true);
            pnlEclCmpMode_chkEsterel.setEnabled(true);
            pnlEclCmpMode_chkVcc.setEnabled(true);
            pnlEclCmpMode_chkClean.setEnabled(true);
            if (pnlEclCmpMode_chkVcc.isSelected()) {
                pnlEclGen_chkG.setEnabled(true);
            }
            xecl.pnlEsMoreOpt_btnAdv.setEnabled(true);
            pnlEclGen_chkG.setEnabled(false);
        }
    }

    public static void saveFile(String filename) {
        try {
            PrintWriter fout = new PrintWriter(new FileWriter(filename));
            fout.println("# This file was automatically generated by XECL");
            fout.println("# Modify at your own risk");
            int i;
            String str = "";
            if (xecl.flg_EsterelOpt) fout.println("Xecl.Esterel_simul ");
            if (xecl.flg_VccOpt) fout.println("Xecl.VCC ");
            if (xecl.flg_TestOpt) fout.println("Xecl.Test ");
            for (i = 1; i < Const.ECL_NBOPTION; i++) {
                if (xecl.tabOptionEcl[i]) {
                    str = "Xecl." + tabEclOpt2Str[i];
                    if (xecl.tabEclOpt2Str[i].equals("VccLib")) str += " " + Vcc_strLibname;
                    if (xecl.tabEclOpt2Str[i].equals("Mainmodule")) str += " " + Vcc_strMainmodule;
                    if (xecl.tabEclOpt2Str[i].equals("DefaultTypeLib")) str += " " + Vcc_strDfttypelib;
                    if (xecl.tabEclOpt2Str[i].equals("TypeLib")) for (int j = 0; j < Vcc_vctTypelib_lib.size(); j++) str += " -TYPELIB " + xecl.Vcc_vctTypelib_lib.elementAt(j) + " " + xecl.Vcc_vctTypelib_type.elementAt(j);
                    if (xecl.tabEclOpt2Str[i].equals("TypeLibFile")) str += " " + Vcc_strTypelibfile;
                    if (xecl.tabEclOpt2Str[i].equals("Workspace")) str += " " + Vcc_strWorkspace;
                    if (xecl.tabEclOpt2Str[i].equals("VccCell")) str += " " + Vcc_strCellname;
                    if (xecl.tabEclOpt2Str[i].equals("Filename")) str += " " + Ecl_strFilename;
                    fout.println(str + " ");
                }
            }
            if (!Ecl_strFilePath.equals("")) fout.println("Xecl.FilePath " + Ecl_strFilePath);
            if (!Ecl_strOutDir.equals("")) fout.println("Xecl.OutDir " + Ecl_strOutDir);
            for (i = 0; i < Const.ES_NBOPTION - 1; i++) {
                if (tabOptionEs[i]) {
                    str = "Esterel." + tabEsOpt2Str[i];
                    if (tabEsOpt2Str[i].equals("D")) str += " " + Es_strD;
                    if (tabEsOpt2Str[i].equals("strlic")) str += " " + Es_strStrlic;
                    if (tabEsOpt2Str[i].equals("iclink")) str += " " + Es_strIclink;
                    if (tabEsOpt2Str[i].equals("lcsc")) str += " " + Es_strLcsc;
                    if (tabEsOpt2Str[i].equals("scssc")) str += " " + Es_strScssc;
                    if (tabEsOpt2Str[i].equals("sccausal")) str += " " + Es_strSccausal;
                    if (tabEsOpt2Str[i].equals("scoc")) str += " " + Es_strScoc;
                    fout.println(str + " ");
                }
            }
            fout.close();
            InfoDialog DlgEndCmp = new InfoDialog(xecl.f, "Store options", "Save successful");
            DlgEndCmp.setModal(true);
            DlgEndCmp.setVisible(true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void loadFile(String filename) {
        try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                if (!currentLine.startsWith("#")) {
                    if (currentLine.startsWith("Xecl.Esterel_simul ")) {
                        xecl.flg_EsterelOpt = true;
                        pnlEclCmpMode_chkEsterel.setSelected(true);
                        tabOptionEs[Const.ES_SIMUL] = false;
                        tabOptionEs[Const.ES_STRLIC] = false;
                        Es_strStrlic = "";
                    }
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.VCC] + " ")) {
                        xecl.flg_VccOpt = true;
                        pnlEclCmpMode_chkVcc.setSelected(true);
                    }
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.VCCLIB] + " ")) {
                        tabOptionEcl[Const.VCCLIB] = true;
                        Vcc_strCellname = currentLine.substring(currentLine.indexOf(" ") + 1, currentLine.length());
                    }
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.MAINMODULE] + " ")) {
                        tabOptionEcl[Const.MAINMODULE] = true;
                        Vcc_strMainmodule = currentLine.substring(currentLine.indexOf(" ") + 1, currentLine.length());
                    }
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.DEFAULTTYPELIB] + " ")) {
                        tabOptionEcl[Const.DEFAULTTYPELIB] = true;
                        Vcc_strDfttypelib = currentLine.substring(currentLine.indexOf(" ") + 1, currentLine.length());
                    }
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.TYPELIBFILE] + " ")) {
                        tabOptionEcl[Const.TYPELIBFILE] = true;
                        Vcc_strCellname = currentLine.substring(currentLine.indexOf(" ") + 1, currentLine.length());
                    }
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.WORKSPACE] + " ")) {
                        tabOptionEcl[Const.WORKSPACE] = true;
                        Vcc_strCellname = currentLine.substring(currentLine.indexOf(" ") + 1, currentLine.length());
                    }
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.IMPORTTYPES] + " ")) tabOptionEcl[Const.IMPORTTYPES] = true;
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.NOIMPORT] + " ")) tabOptionEcl[Const.NOIMPORT] = true;
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.VCCCELL] + " ")) {
                        tabOptionEcl[Const.VCCCELL] = true;
                        Vcc_strCellname = currentLine.substring(currentLine.indexOf(" ") + 1, currentLine.length());
                    }
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.G] + " ")) {
                        tabOptionEcl[Const.G] = true;
                        pnlEclGen_chkG.setSelected(true);
                    }
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.P] + " ")) tabOptionEcl[Const.P] = true;
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.F] + " ")) tabOptionEcl[Const.F] = true;
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.C] + " ")) tabOptionEcl[Const.C] = true;
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.E] + " ")) tabOptionEcl[Const.E] = true;
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.CHECK] + " ")) {
                        tabOptionEcl[Const.CHECK] = true;
                        pnlEclGen_chkCheck.setSelected(true);
                    }
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.NOCPP] + " ")) tabOptionEcl[Const.NOCPP] = true;
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.TEST] + " ")) {
                        xecl.flg_TestOpt = true;
                        pnlEclCmpMode_chkTest.setSelected(true);
                    }
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.PD] + " ")) tabOptionEcl[Const.PD] = true;
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.CD] + " ")) tabOptionEcl[Const.CD] = true;
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.NG] + " ")) tabOptionEcl[Const.NG] = true;
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.CLEAN] + " ")) {
                        tabOptionEcl[Const.CLEAN] = true;
                        pnlEclCmpMode_chkClean.setSelected(true);
                    }
                    if (currentLine.startsWith("Xecl." + tabEclOpt2Str[Const.ES] + " ")) {
                        tabOptionEcl[Const.ES] = true;
                        pnlEclGen_chkES.setSelected(true);
                    }
                    if (currentLine.startsWith("Xecl.FilePath ")) Ecl_strFilePath = currentLine.substring(currentLine.indexOf(" ") + 1, currentLine.length());
                    if (currentLine.startsWith("Xecl.OutDir ")) Ecl_strOutDir = currentLine.substring(currentLine.indexOf(" ") + 1, currentLine.length());
                    if (currentLine.startsWith("Xecl.Filename ")) {
                        tabOptionEcl[Const.FILENAME] = true;
                        Ecl_strFilename = currentLine.substring(currentLine.indexOf(" ") + 1, currentLine.length());
                        StringTokenizer tokenizer = new StringTokenizer(Ecl_strFilename, ".");
                        base = tokenizer.nextToken();
                    }
                    if (!Ecl_strFilePath.equals("")) pnlEsEcl_txtFilename.setText(Ecl_strFilePath + dirsep + Ecl_strFilename); else pnlEsEcl_txtFilename.setText(Ecl_strFilename);
                    if (Ecl_strOutDir.equals("")) pnlEsEcl_txtOutDir.setText("."); else pnlEsEcl_txtOutDir.setText(Ecl_strOutDir);
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_L] + " ")) tabOptionEs[Const.ES_L] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_A] + " ")) tabOptionEs[Const.ES_A] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_I] + " ")) tabOptionEs[Const.ES_I] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_SIMUL] + " ")) tabOptionEs[Const.ES_SIMUL] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_CAUSAL] + " ")) tabOptionEs[Const.ES_CAUSAL] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_SINGLE] + " ")) tabOptionEs[Const.ES_SINGLE] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_CYCLES] + " ")) tabOptionEs[Const.ES_CYCLES] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_SUB] + " ")) tabOptionEs[Const.ES_SUB] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_MOD] + " ")) tabOptionEs[Const.ES_MOD] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_MODULE] + " ")) tabOptionEs[Const.ES_MODULE] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_D] + " ")) tabOptionEs[Const.ES_D] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_W] + " ")) tabOptionEs[Const.ES_W] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_WW] + " ")) tabOptionEs[Const.ES_WW] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_STAT] + " ")) tabOptionEs[Const.ES_STAT] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_SIZE] + " ")) tabOptionEs[Const.ES_SIZE] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_S] + " ")) tabOptionEs[Const.ES_S] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_IC] + " ")) tabOptionEs[Const.ES_IC] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_LC] + " ")) tabOptionEs[Const.ES_LC] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_SC] + " ")) tabOptionEs[Const.ES_SC] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_SSC] + " ")) tabOptionEs[Const.ES_SSC] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_OC] + " ")) tabOptionEs[Const.ES_OC] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_K] + " ")) tabOptionEs[Const.ES_K] = true;
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_STRLIC] + " ")) {
                        tabOptionEs[Const.ES_STRLIC] = true;
                        Es_strStrlic = currentLine.substring(currentLine.indexOf(" ") + 1, currentLine.length());
                    }
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_ICLINK])) {
                        tabOptionEs[Const.ES_ICLINK] = true;
                        Es_strIclink = currentLine.substring(currentLine.indexOf(" ") + 1, currentLine.length());
                    }
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_LCSC])) {
                        tabOptionEs[Const.ES_LCSC] = true;
                        Es_strLcsc = currentLine.substring(currentLine.indexOf(" ") + 1, currentLine.length());
                    }
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_SCSSC])) {
                        tabOptionEs[Const.ES_SCSSC] = true;
                        Es_strScssc = currentLine.substring(currentLine.indexOf(" ") + 1, currentLine.length());
                    }
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_SCCAUSAL])) {
                        tabOptionEs[Const.ES_SCCAUSAL] = true;
                        Es_strSccausal = currentLine.substring(currentLine.indexOf(" ") + 1, currentLine.length());
                    }
                    if (currentLine.startsWith("Esterel." + tabEsOpt2Str[Const.ES_SCOC])) {
                        tabOptionEs[Const.ES_SCOC] = true;
                        Es_strScoc = currentLine.substring(currentLine.indexOf(" ") + 1, currentLine.length());
                    }
                }
            }
            buildCmd();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void initTab() {
        for (int i = 0; i < Const.ECL_NBOPTION; i++) tabOptionEcl[i] = false;
        for (int i = 0; i < Const.ES_NBOPTION; i++) {
            tabOptionEs[i] = false;
            tabOptionEsAlone[i] = false;
        }
        tabEclOpt2Str[Const.ESTEREL] = "Esterel_simul";
        tabEclOpt2Str[Const.VCC] = "VCC";
        tabEclOpt2Str[Const.VCCLIB] = "VccLib";
        tabEclOpt2Str[Const.MAINMODULE] = "Mainmodule";
        tabEclOpt2Str[Const.DEFAULTTYPELIB] = "DefaultTypeLib";
        tabEclOpt2Str[Const.TYPELIB] = "TypeLib";
        tabEclOpt2Str[Const.TYPELIBFILE] = "TypeLibFile";
        tabEclOpt2Str[Const.WORKSPACE] = "Workspace";
        tabEclOpt2Str[Const.IMPORTTYPES] = "ImportType";
        tabEclOpt2Str[Const.NOIMPORT] = "Noimport";
        tabEclOpt2Str[Const.VCCCELL] = "VccCell";
        tabEclOpt2Str[Const.G] = "G";
        tabEclOpt2Str[Const.P] = "P";
        tabEclOpt2Str[Const.F] = "F";
        tabEclOpt2Str[Const.C] = "C";
        tabEclOpt2Str[Const.E] = "E";
        tabEclOpt2Str[Const.CHECK] = "Check";
        tabEclOpt2Str[Const.NOCPP] = "Nocpp";
        tabEclOpt2Str[Const.TEST] = "Test";
        tabEclOpt2Str[Const.PD] = "PD";
        tabEclOpt2Str[Const.CD] = "CD";
        tabEclOpt2Str[Const.NG] = "NG";
        tabEclOpt2Str[Const.D] = "D";
        tabEclOpt2Str[Const.I] = "I";
        tabEclOpt2Str[Const.CLEAN] = "Clean";
        tabEclOpt2Str[Const.H] = "H";
        tabEclOpt2Str[Const.ES] = "ES";
        tabEclOpt2Str[Const.FILENAME] = "Filename";
        tabEsOpt2Str[Const.ES_L] = "L";
        tabEsOpt2Str[Const.ES_A] = "A";
        tabEsOpt2Str[Const.ES_I] = "I";
        tabEsOpt2Str[Const.ES_SIMUL] = "simul";
        tabEsOpt2Str[Const.ES_CAUSAL] = "causal";
        tabEsOpt2Str[Const.ES_SINGLE] = "single";
        tabEsOpt2Str[Const.ES_CYCLES] = "cycles";
        tabEsOpt2Str[Const.ES_SUB] = "sub";
        tabEsOpt2Str[Const.ES_MOD] = "mod";
        tabEsOpt2Str[Const.ES_MODULE] = "module";
        tabEsOpt2Str[Const.ES_D] = "D";
        tabEsOpt2Str[Const.ES_W] = "w";
        tabEsOpt2Str[Const.ES_WW] = "W";
        tabEsOpt2Str[Const.ES_STAT] = "stat";
        tabEsOpt2Str[Const.ES_SIZE] = "size";
        tabEsOpt2Str[Const.ES_S] = "s";
        tabEsOpt2Str[Const.ES_IC] = "ic";
        tabEsOpt2Str[Const.ES_LC] = "lc";
        tabEsOpt2Str[Const.ES_SC] = "sc";
        tabEsOpt2Str[Const.ES_SSC] = "ssc";
        tabEsOpt2Str[Const.ES_OC] = "oc";
        tabEsOpt2Str[Const.ES_K] = "K";
        tabEsOpt2Str[Const.ES_STRLIC] = "strlic";
        tabEsOpt2Str[Const.ES_ICLINK] = "iclink";
        tabEsOpt2Str[Const.ES_LCSC] = "lcsc";
        tabEsOpt2Str[Const.ES_SCSSC] = "scssc";
        tabEsOpt2Str[Const.ES_SCCAUSAL] = "sccausal";
        tabEsOpt2Str[Const.ES_SCOC] = "scoc";
        tabEsOpt2Str[Const.ES_FILENAME] = "filename";
    }
}

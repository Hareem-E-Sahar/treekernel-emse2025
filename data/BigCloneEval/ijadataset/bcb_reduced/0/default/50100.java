import java.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class xeclVccOptions extends JDialog implements ActionListener, KeyListener, ItemListener {

    static JPanel pnl1;

    static String pnl1_title;

    static JLabel pnl1_lblCellname;

    static JTextField pnl1_txtCellname;

    static JButton pnl1_btnCellname;

    static File pnl1_Cell;

    static JLabel pnl1_lblLibname;

    static JTextField pnl1_txtLibname;

    static JButton pnl1_btnLibname;

    static File pnl1_Lib;

    static JLabel pnl1_lblWorkspace;

    static JTextField pnl1_txtWorkspace;

    static JButton pnl1_btnWorkspace;

    static File pnl1_Workspace;

    static JLabel pnl1_lblDfttypelib;

    static JTextField pnl1_txtDfttypelib;

    static JButton pnl1_btnDfttypelib;

    static JLabel pnl1_lblTypelib;

    static JButton pnl1_btnTypelib;

    static JLabel pnl1_lblTypelibfile;

    static JTextField pnl1_txtTypelibfile;

    static JButton pnl1_btnTypelibfile;

    static JCheckBox pnl1_chkImporttypes;

    static JPanel pnl2;

    static ButtonGroup pnl2_chkGr;

    static JRadioButton pnl2_chkImport;

    static JRadioButton pnl2_chkUpdate;

    static JPanel pnl3;

    static JLabel pnl3_lblMainmodule;

    static JTextField pnl3_txtMainmodule;

    static JButton pnl3_btnMainmodule;

    static JPanel pnl4;

    static JButton pnl4_btnOK;

    static JButton pnl4_btnCANCEL;

    JPanel pnlRoot;

    optionConstant Const;

    Constrain constrain;

    public xeclVccOptions(JFrame parent) {
        super(parent, "VCC options");
        Const = new optionConstant();
        constrain = new Constrain();
        GridBagLayout gridbag = new GridBagLayout();
        pnlRoot = new JPanel(new BorderLayout());
        pnlRoot.setLayout(gridbag);
        getContentPane().add(BorderLayout.CENTER, pnlRoot);
        pnl1 = new JPanel();
        pnl1.setLayout(gridbag);
        pnl2 = new JPanel();
        pnl2.setLayout(gridbag);
        pnl3 = new JPanel();
        pnl3.setLayout(gridbag);
        pnl4 = new JPanel();
        pnl4.setLayout(gridbag);
        this.setConstrainVcc();
        getCurrentOptions();
        pnl1_btnCellname.addActionListener(this);
        pnl1_btnLibname.addActionListener(this);
        pnl1_btnWorkspace.addActionListener(this);
        pnl1_btnTypelibfile.addActionListener(this);
        pnl2_chkImport.addItemListener(this);
        pnl2_chkUpdate.addItemListener(this);
        pnl3_btnMainmodule.addActionListener(this);
        pnl1_btnDfttypelib.addActionListener(this);
        pnl1_btnTypelib.addActionListener(this);
        pnl4_btnOK.addActionListener(this);
        pnl4_btnCANCEL.addActionListener(this);
        pnl1_txtLibname.addKeyListener(this);
        pnl1_txtCellname.addKeyListener(this);
        pnl1_txtWorkspace.addKeyListener(this);
        pnl1_txtTypelibfile.addKeyListener(this);
        pnl1_txtDfttypelib.addKeyListener(this);
        pnl3_txtMainmodule.addKeyListener(this);
        this.addKeyListener(this);
        setVccTooltips();
        xecl.flg_VccOpt = false;
        this.pack();
        this.setModal(true);
    }

    private void setConstrainVcc() {
        int nbRow = 0;
        pnl1_lblCellname = new JLabel("CellName: ");
        pnl1_txtCellname = new JTextField(15);
        pnl1_btnCellname = new JButton("...");
        pnl1_lblLibname = new JLabel("LibName: ");
        pnl1_txtLibname = new JTextField(15);
        pnl1_btnLibname = new JButton("...");
        pnl1_lblWorkspace = new JLabel("WORKSPACE");
        pnl1_txtWorkspace = new JTextField(15);
        pnl1_btnWorkspace = new JButton("...");
        pnl1_lblDfttypelib = new JLabel("DEFAULTTYPELIB");
        pnl1_txtDfttypelib = new JTextField(15);
        pnl1_btnDfttypelib = new JButton("...");
        pnl1_lblTypelib = new JLabel("TYPELIB");
        pnl1_btnTypelib = new JButton("...");
        pnl1_lblTypelibfile = new JLabel("TYPELIBFILE");
        pnl1_txtTypelibfile = new JTextField(15);
        pnl1_btnTypelibfile = new JButton("...");
        pnl1_chkImporttypes = new JCheckBox("-IMPORTTYPES");
        pnl1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Libraries and types:  "), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        pnl2_chkGr = new ButtonGroup();
        pnl2_chkImport = new JRadioButton("Import model", true);
        pnl2_chkUpdate = new JRadioButton("Update model");
        pnl2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("VCC target:  "), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        pnl2_chkGr.add(pnl2_chkImport);
        pnl2_chkGr.add(pnl2_chkUpdate);
        pnl3.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("More options:  "), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        pnl3_lblMainmodule = new JLabel("-MAINMODULE");
        pnl3_txtMainmodule = new JTextField(15);
        pnl3_btnMainmodule = new JButton("...");
        pnl4_btnOK = new JButton("OK");
        pnl4_btnCANCEL = new JButton("Cancel");
        constrain.set(pnl1, pnl1_lblCellname, 0, nbRow, 1, 1);
        constrain.set(pnl1, pnl1_txtCellname, 1, nbRow, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 2, 0, 0, 5);
        constrain.set(pnl1, pnl1_btnCellname, 2, nbRow++, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 0, 10);
        constrain.set(pnl1, pnl1_lblLibname, 0, nbRow, 1, 1);
        constrain.set(pnl1, pnl1_txtLibname, 1, nbRow, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 2, 0, 0, 5);
        constrain.set(pnl1, pnl1_btnLibname, 2, nbRow++, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 0, 10);
        constrain.set(pnl1, pnl1_lblWorkspace, 0, nbRow, 1, 1);
        constrain.set(pnl1, pnl1_txtWorkspace, 1, nbRow, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 2, 0, 0, 5);
        constrain.set(pnl1, pnl1_btnWorkspace, 2, nbRow++, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 0, 10);
        constrain.set(pnl1, pnl1_lblDfttypelib, 0, nbRow, 1, 1);
        constrain.set(pnl1, pnl1_txtDfttypelib, 1, nbRow, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 2, 0, 0, 5);
        constrain.set(pnl1, pnl1_btnDfttypelib, 2, nbRow++, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 0, 10);
        constrain.set(pnl1, pnl1_lblTypelib, 0, nbRow, 1, 1);
        constrain.set(pnl1, pnl1_btnTypelib, 2, nbRow++, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 0, 10);
        constrain.set(pnl1, pnl1_lblTypelibfile, 0, nbRow, 1, 1);
        constrain.set(pnl1, pnl1_txtTypelibfile, 1, nbRow, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 2, 0, 0, 5);
        constrain.set(pnl1, pnl1_btnTypelibfile, 2, nbRow++, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 0, 10);
        constrain.set(pnl1, pnl1_chkImporttypes, 0, nbRow++, 1, 1);
        constrain.set(pnl2, pnl2_chkImport, 0, nbRow++, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 0, 10);
        constrain.set(pnl2, pnl2_chkUpdate, 0, nbRow++, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 0, 10);
        constrain.set(pnl3, pnl3_lblMainmodule, 0, nbRow, 1, 1);
        constrain.set(pnl3, pnl3_txtMainmodule, 1, nbRow, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 2, 0, 0, 5);
        constrain.set(pnl3, pnl3_btnMainmodule, 2, nbRow++, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 0, 10);
        constrain.set(pnl4, pnl4_btnOK, 0, nbRow, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 1.0, 0.0, 5, 40, 5, 20);
        constrain.set(pnl4, pnl4_btnCANCEL, 1, nbRow, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 1.0, 0.0, 5, 20, 5, 20);
        constrain.set(pnlRoot, pnl1, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, 1.0, 0.0, 5, 5, 5, 5);
        constrain.set(pnlRoot, pnl2, 0, 1, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, 1.0, 0.0, 5, 5, 5, 5);
        constrain.set(pnlRoot, pnl3, 0, 2, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, 1.0, 0.0, 5, 5, 5, 5);
        constrain.set(pnlRoot, pnl4, 0, 3, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, 1.0, 0.0, 5, 5, 5, 5);
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == pnl1_btnCellname) {
            pnl1_txtCellname.setForeground(Color.black);
            xecl.chooser.setDialogTitle("Select the Cell's name");
            String path = "";
            if (!pnl1_txtWorkspace.getText().equals("")) {
                path += pnl1_txtWorkspace.getText();
                if (!pnl1_txtLibname.getText().equals("")) path += xecl.dirsep + pnl1_txtLibname.getText();
            }
            if (path.equals("")) xecl.chooser.setCurrentDirectory(new File("C:\\MyWorkspace")); else xecl.chooser.setCurrentDirectory(new File(path));
            xecl.chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int ret = xecl.chooser.showOpenDialog(xecl.f);
            if (xecl.chooser.getSelectedFile() != null && ret == 0) {
                File Cell, Lib, Workspace;
                Cell = xecl.chooser.getSelectedFile();
                Lib = xecl.chooser.getCurrentDirectory();
                Workspace = xecl.chooser.getCurrentDirectory();
                pnl1_txtCellname.setText(Cell.getName());
                pnl1_txtLibname.setText(Lib.getName());
                pnl1_txtWorkspace.setText(Workspace.getParent());
                xecl.tabOptionEcl[Const.VCCCELL] = true;
                xecl.tabOptionEcl[Const.VCCLIB] = true;
                xecl.tabOptionEcl[Const.WORKSPACE] = true;
            }
        }
        if (event.getSource() == pnl1_btnLibname) {
            pnl1_txtLibname.setForeground(Color.black);
            String path = "";
            if (!pnl1_txtWorkspace.getText().equals("")) path += pnl1_txtWorkspace.getText();
            if (path.equals("")) xecl.chooser.setCurrentDirectory(new File("C:\\MyWorkspace")); else xecl.chooser.setCurrentDirectory(new File(path));
            xecl.chooser.setDialogTitle("Select the Lib's name");
            xecl.chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int ret = xecl.chooser.showOpenDialog(xecl.f);
            if (xecl.chooser.getSelectedFile() != null && ret == 0) {
                File Lib, Workspace;
                Lib = xecl.chooser.getSelectedFile();
                Workspace = xecl.chooser.getCurrentDirectory();
                pnl1_txtLibname.setText(Lib.getName());
                pnl1_txtWorkspace.setText(Workspace.getPath());
                xecl.tabOptionEcl[Const.VCCLIB] = true;
                xecl.tabOptionEcl[Const.WORKSPACE] = true;
            }
        }
        if (event.getSource() == pnl1_btnWorkspace) {
            pnl1_txtWorkspace.setForeground(Color.black);
            if (xecl.Vcc_strWorkspace == null) xecl.chooser.setCurrentDirectory(new File("C:\\MyWorkspace")); else xecl.chooser.setCurrentDirectory(new File(xecl.Vcc_strWorkspace));
            xecl.chooser.setDialogTitle("Select the workspace's name");
            xecl.chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int ret = xecl.chooser.showOpenDialog(xecl.f);
            if (xecl.chooser.getSelectedFile() != null && ret == 0) {
                File Workspace;
                Workspace = xecl.chooser.getSelectedFile();
                pnl1_txtWorkspace.setText(Workspace.toString());
                xecl.tabOptionEcl[Const.WORKSPACE] = true;
            }
        }
        if (event.getSource() == pnl3_btnMainmodule) {
            pnl3_txtMainmodule.setForeground(Color.black);
            String path = "";
            if (!pnl1_txtWorkspace.getText().equals("")) {
                path += pnl1_txtWorkspace.getText();
                if (!pnl1_txtLibname.getText().equals("")) {
                    path += xecl.dirsep + pnl1_txtLibname.getText();
                    if (!pnl1_txtCellname.getText().equals("")) path += xecl.dirsep + pnl1_txtCellname.getText();
                }
            }
            if (path == null) xecl.chooser.setCurrentDirectory(new File("C:\\MyWorkspace")); else xecl.chooser.setCurrentDirectory(new File(path));
            xecl.chooser.setDialogTitle("Select the Mainmodule's name");
            xecl.chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int ret = xecl.chooser.showOpenDialog(xecl.f);
            if (xecl.chooser.getSelectedFile() != null && ret == 0) {
                pnl3_txtMainmodule.setText(xecl.chooser.getSelectedFile().getName());
                xecl.Vcc_strMainmodule = pnl3_txtMainmodule.getText();
                xecl.tabOptionEcl[Const.MAINMODULE] = true;
            }
        }
        if (event.getSource() == pnl1_btnDfttypelib) {
            pnl1_txtDfttypelib.setForeground(Color.black);
            String path = "";
            if (!pnl1_txtWorkspace.getText().equals("")) {
                path += pnl1_txtWorkspace.getText();
                if (!pnl1_txtLibname.getText().equals("")) {
                    path += xecl.dirsep + pnl1_txtLibname.getText();
                    if (!pnl1_txtCellname.getText().equals("")) path += xecl.dirsep + pnl1_txtCellname.getText();
                }
            }
            if (path == null) xecl.chooser.setCurrentDirectory(new File("C:\\MyWorkspace")); else xecl.chooser.setCurrentDirectory(new File(path));
            xecl.chooser.setDialogTitle("Select the user-defineds for all types");
            xecl.chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int ret = xecl.chooser.showOpenDialog(xecl.f);
            if (xecl.chooser.getSelectedFile() != null && ret == 0) {
                pnl1_txtDfttypelib.setText(xecl.chooser.getSelectedFile().getName());
                xecl.Vcc_strDfttypelib = pnl1_txtDfttypelib.getText();
                xecl.tabOptionEcl[Const.DEFAULTTYPELIB] = true;
            }
        }
        if (event.getSource() == pnl1_btnTypelib) {
            this.setModal(false);
            xecl_I_D_OptDlg dlgTypelib = new xecl_I_D_OptDlg(xecl.f, Const.TYPELIB);
            dlgTypelib.setModal(true);
            dlgTypelib.setVisible(true);
            this.setModal(true);
        }
        if (event.getSource() == pnl1_btnTypelibfile) {
            pnl1_txtTypelibfile.setForeground(Color.black);
            pnl1_txtTypelibfile.setForeground(Color.black);
            String path = "";
            if (!pnl1_txtWorkspace.getText().equals("")) {
                path += pnl1_txtWorkspace.getText();
                if (!pnl1_txtLibname.getText().equals("")) {
                    path += xecl.dirsep + pnl1_txtLibname.getText();
                    if (!pnl1_txtCellname.getText().equals("")) path += xecl.dirsep + pnl1_txtCellname.getText();
                }
            }
            if (path == null) xecl.chooser.setCurrentDirectory(new File("C:\\MyWorkspace")); else xecl.chooser.setCurrentDirectory(new File(path));
            xecl.chooser.setDialogTitle("Select the user-defined type");
            xecl.chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int ret = xecl.chooser.showOpenDialog(xecl.f);
            if (xecl.chooser.getSelectedFile() != null && ret == 0) {
                pnl1_txtTypelibfile.setText(xecl.chooser.getSelectedFile().getName());
                xecl.tabOptionEcl[Const.TYPELIBFILE] = true;
                xecl.Vcc_strTypelibfile = pnl1_txtTypelibfile.getText();
            }
        }
        if (event.getSource() == pnl4_btnCANCEL) {
            ExitCancel();
        }
        if (event.getSource() == pnl4_btnOK) {
            ExitOk();
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            ExitOk();
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            ExitCancel();
        }
        if (e.getSource() == pnl1_txtCellname) {
            pnl1_txtCellname.setForeground(Color.red);
        }
        if (e.getSource() == pnl1_txtLibname) {
            pnl1_txtLibname.setForeground(Color.red);
        }
        if (e.getSource() == pnl1_txtWorkspace) {
            pnl1_txtWorkspace.setForeground(Color.red);
        }
        if (e.getSource() == pnl3_txtMainmodule) {
            if (pnl3_txtMainmodule.getForeground() != Color.red) pnl3_txtMainmodule.setForeground(Color.red);
            xecl.Vcc_strMainmodule = pnl3_txtMainmodule.getText();
        }
        if (e.getSource() == xeclVccOptions.pnl1_txtDfttypelib) {
            if (pnl1_txtDfttypelib.getForeground() != Color.red) pnl1_txtDfttypelib.setForeground(Color.red);
            xecl.Vcc_strDfttypelib = pnl1_txtDfttypelib.getText();
        }
        if (e.getSource() == pnl1_txtTypelibfile) {
            pnl1_txtTypelibfile.setForeground(Color.red);
        }
    }

    private void ExitOk() {
        this.setNewOptions();
        for (int i = 0; i < Const.FILENAME; i++) {
            if (xecl.tabOptionEcl[i]) xecl.flg_VccOpt = true;
        }
        if (!xecl.flg_VccOpt) {
            xecl.pnlEclCmpMode_chkVcc.setSelected(false);
        }
        xecl.buildCmd();
        this.dispose();
    }

    private void ExitCancel() {
        if (!xecl.flg_VccOpt) xecl.pnlEclCmpMode_chkVcc.setSelected(false);
        this.dispose();
    }

    protected void getCurrentOptions() {
        pnl1_txtCellname.setText(xecl.Vcc_strCellname);
        pnl1_txtLibname.setText(xecl.Vcc_strLibname);
        pnl1_txtWorkspace.setText(xecl.Vcc_strWorkspace);
        pnl1_txtTypelibfile.setText(xecl.Vcc_strTypelibfile);
        pnl1_txtDfttypelib.setText(xecl.Vcc_strDfttypelib);
        pnl3_txtMainmodule.setText(xecl.Vcc_strMainmodule);
        if (xecl.tabOptionEcl[Const.IMPORTTYPES]) pnl1_chkImporttypes.setSelected(true);
    }

    protected void setNewOptions() {
        xecl.Vcc_strCellname = pnl1_txtCellname.getText();
        xecl.Vcc_strLibname = pnl1_txtLibname.getText();
        xecl.Vcc_strWorkspace = pnl1_txtWorkspace.getText();
        xecl.Vcc_strTypelibfile = pnl1_txtTypelibfile.getText();
        xecl.Vcc_strMainmodule = pnl3_txtMainmodule.getText();
        xecl.Vcc_strDfttypelib = pnl1_txtDfttypelib.getText();
        if (!pnl1_txtCellname.getText().equals("")) xecl.tabOptionEcl[Const.VCCCELL] = true; else xecl.tabOptionEcl[Const.VCCCELL] = false;
        if (!pnl1_txtLibname.getText().equals("")) xecl.tabOptionEcl[Const.VCCLIB] = true; else xecl.tabOptionEcl[Const.VCCLIB] = false;
        if (!pnl1_txtWorkspace.getText().equals("")) xecl.tabOptionEcl[Const.WORKSPACE] = true; else xecl.tabOptionEcl[Const.WORKSPACE] = false;
        if (!pnl1_txtTypelibfile.getText().equals("")) xecl.tabOptionEcl[Const.TYPELIBFILE] = true; else xecl.tabOptionEcl[Const.TYPELIBFILE] = false;
        if (!pnl3_txtMainmodule.getText().equals("")) xecl.tabOptionEcl[Const.MAINMODULE] = true; else xecl.tabOptionEcl[Const.MAINMODULE] = false;
        if (!pnl1_txtDfttypelib.getText().equals("")) xecl.tabOptionEcl[Const.DEFAULTTYPELIB] = true; else xecl.tabOptionEcl[Const.DEFAULTTYPELIB] = false;
        if (pnl1_chkImporttypes.isSelected()) xecl.tabOptionEcl[Const.IMPORTTYPES] = true; else xecl.tabOptionEcl[Const.IMPORTTYPES] = false;
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getItemSelectable() == pnl2_chkImport) {
            if (pnl2_chkImport.isSelected()) {
                xecl.pnlEclGen_chkG.setSelected(false);
            }
            xecl.buildCmd();
        }
        if (e.getItemSelectable() == pnl2_chkUpdate) {
        }
    }

    private void setVccTooltips() {
        pnl1_lblCellname.setToolTipText("Cell name");
        pnl1_txtCellname.setToolTipText("Cell name");
        pnl1_btnCellname.setToolTipText("Pushes to select the Cell name");
        pnl1_lblLibname.setToolTipText("Librairy name");
        pnl1_txtLibname.setToolTipText("Librairy name");
        pnl1_btnLibname.setToolTipText("Push to select the Library name");
        pnl1_lblWorkspace.setToolTipText("Specifies the VCC workspace pathname");
        pnl1_txtWorkspace.setToolTipText("Specifies the VCC workspace pathname");
        pnl1_btnWorkspace.setToolTipText("Pushes to select the workspace pathname");
        pnl1_lblDfttypelib.setToolTipText("Default VCC library to read or import user-defined types");
        pnl1_txtDfttypelib.setToolTipText("Default VCC library to read or import user-defined types");
        pnl1_btnDfttypelib.setToolTipText("Pushes to select a librairy");
        pnl1_lblTypelib.setToolTipText("Read or import a type from a VCC library");
        pnl1_btnTypelib.setToolTipText("Read or import a type from a VCC library");
        pnl1_lblTypelibfile.setToolTipText("Specify a file to find TYPELIB type/library pairs");
        pnl1_txtTypelibfile.setToolTipText("Specify a file to find TYPELIB type/library pairs");
        pnl1_btnTypelibfile.setToolTipText("Pushes to select the typelib file");
        pnl1_chkImporttypes.setToolTipText("Import into VCC any types not found in designated libraries");
        pnl2_chkImport.setToolTipText("Import the model in VCC");
        pnl2_chkUpdate.setToolTipText("Update the existing model in VCC");
        pnl3_lblMainmodule.setToolTipText("Specifies the name of the main module used in VCC import");
        pnl3_txtMainmodule.setToolTipText("Specifies the name of the main module used in VCC import");
        pnl3_btnMainmodule.setToolTipText("Pushes to select the mainmodule name");
    }
}

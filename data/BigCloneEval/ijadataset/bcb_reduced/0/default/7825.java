import java.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class xecl_I_D_OptDlg extends JDialog implements ActionListener, KeyListener, ListSelectionListener {

    JPanel pnlRoot;

    JPanel pnlList;

    JList pnlList_lst;

    JScrollPane pnlList_scrPane;

    JPanel pnlBtn;

    JButton pnlBtn_btnAdd;

    JButton pnlBtn_btnRemove;

    JButton pnlBtn_btnRemoveAll;

    JPanel pnlExit;

    JButton pnlExit_btnOk;

    JButton pnlExit_btnCancel;

    JPanel pnlOpt;

    JLabel pnlOpt_lbl1;

    JTextField pnlOpt_txt1;

    JButton pnlOpt_btnI;

    JLabel pnlOpt_lblValue;

    JTextField pnlOpt_txtValue;

    Vector include;

    Vector macro;

    Vector value;

    Vector lib;

    Vector type;

    int nbIncl;

    Vector oldInclude;

    Vector oldMacro;

    Vector oldValue;

    Vector oldLib;

    Vector oldType;

    Dimension listDimension;

    Dimension buttonDimension;

    optionConstant Const;

    Constrain constrain;

    int mode;

    boolean flg_replaceElt;

    int indReplacedElt;

    public xecl_I_D_OptDlg(JFrame parent, int option) {
        super(parent);
        String title = "";
        mode = option;
        if (mode == Const.INCLUDE) title = "Include librairy";
        if (mode == Const.MACRO) title = "Define Macro";
        if (mode == Const.TYPELIB) title = "Define Typelib";
        this.setTitle(title);
        Const = new optionConstant();
        constrain = new Constrain();
        if (mode == Const.INCLUDE) listDimension = new Dimension(250, 150); else listDimension = new Dimension(250, 200);
        if (xecl.argXEcl.nt) buttonDimension = new Dimension(100, 20); else buttonDimension = new Dimension(120, 20);
        flg_replaceElt = false;
        GridBagLayout gridbag = new GridBagLayout();
        pnlRoot = new JPanel();
        pnlRoot.setLayout(gridbag);
        getContentPane().add(BorderLayout.CENTER, pnlRoot);
        pnlOpt = new JPanel();
        pnlOpt.setLayout(gridbag);
        if (mode == Const.INCLUDE) pnlOpt_lbl1 = new JLabel("Include:");
        if (mode == Const.INCLUDE || mode == Const.TYPELIB) pnlOpt_btnI = new JButton("...");
        if (mode == Const.MACRO) {
            pnlOpt_lbl1 = new JLabel("Macro:");
            pnlOpt_lblValue = new JLabel("Value:");
            pnlOpt_txtValue = new JTextField(15);
        }
        if (mode == Const.TYPELIB) {
            pnlOpt_lbl1 = new JLabel("Lib:");
            pnlOpt_lblValue = new JLabel("Type:");
            pnlOpt_txtValue = new JTextField(15);
        }
        pnlOpt_txt1 = new JTextField(15);
        constrain.set(pnlOpt, pnlOpt_lbl1, 0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 5, 5, 5);
        constrain.set(pnlOpt, pnlOpt_txt1, 0, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 5, 5, 5);
        if (mode == Const.INCLUDE || mode == Const.TYPELIB) constrain.set(pnlOpt, pnlOpt_btnI, 1, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 5, 5, 5);
        if (mode == Const.MACRO || mode == Const.TYPELIB) {
            constrain.set(pnlOpt, pnlOpt_lblValue, 0, 2, 1, 1, GridBagConstraints.VERTICAL, GridBagConstraints.NORTHWEST, 1.0, 1.0, 0, 5, 5, 5);
            constrain.set(pnlOpt, pnlOpt_txtValue, 0, 3, 1, 1, GridBagConstraints.VERTICAL, GridBagConstraints.NORTHWEST, 1.0, 1.0, 0, 5, 5, 5);
        }
        pnlBtn = new JPanel();
        pnlBtn.setLayout(gridbag);
        pnlBtn_btnAdd = new JButton("Add");
        pnlBtn_btnRemove = new JButton("Remove");
        pnlBtn_btnRemoveAll = new JButton("Remove All");
        pnlBtn_btnAdd.setPreferredSize(buttonDimension);
        pnlBtn_btnRemove.setPreferredSize(buttonDimension);
        pnlBtn_btnRemoveAll.setPreferredSize(buttonDimension);
        constrain.set(pnlBtn, pnlBtn_btnAdd, 0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST, 1.0, 0.0, 0, 5, 5, 5);
        constrain.set(pnlBtn, pnlBtn_btnRemove, 0, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST, 1.0, 0.0, 0, 5, 5, 5);
        constrain.set(pnlBtn, pnlBtn_btnRemoveAll, 0, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST, 1.0, 0.0, 0, 5, 5, 5);
        pnlExit = new JPanel();
        pnlExit.setLayout(gridbag);
        pnlExit_btnOk = new JButton("OK");
        pnlExit_btnCancel = new JButton("Cancel");
        constrain.set(pnlExit, pnlExit_btnOk, 0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 1.0, 1.0, 0, 5, 5, 20);
        constrain.set(pnlExit, pnlExit_btnCancel, 1, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 1.0, 1.0, 0, 20, 5, 5);
        pnlList = new JPanel();
        pnlList.setLayout(gridbag);
        pnlList_lst = new JList();
        pnlList_lst.setVisibleRowCount(2);
        pnlList_lst.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        pnlList.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("List:  "), BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        pnlList_scrPane = new JScrollPane(pnlList_lst);
        pnlList_scrPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        constrain.set(pnlList, pnlList_scrPane, 0, 0, 1, 1);
        pnlList_scrPane.setPreferredSize(listDimension);
        pnlList_scrPane.setMinimumSize(listDimension);
        pnlList_scrPane.setMaximumSize(listDimension);
        constrain.set(pnlRoot, pnlOpt, 0, 0, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 5, 5, 5);
        constrain.set(pnlRoot, pnlBtn, 0, 1, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.NORTHEAST, 1.0, 0.0, 0, 5, 5, 5);
        constrain.set(pnlRoot, pnlList, 1, 0, 1, 2, GridBagConstraints.BOTH, GridBagConstraints.CENTER, 1.0, 1.0, 0, 5, 5, 5);
        constrain.set(pnlRoot, pnlExit, 0, 2, 2, 1, GridBagConstraints.VERTICAL, GridBagConstraints.CENTER, 0.0, 1.0, 0, 5, 5, 5);
        getCurrentOptions();
        pnlBtn_btnRemove.setEnabled(false);
        if (nbIncl == 0) {
            pnlBtn_btnRemoveAll.setEnabled(false);
            pnlBtn_btnAdd.setEnabled(false);
        }
        this.setResizable(false);
        this.pack();
        pnlExit_btnOk.addActionListener(this);
        pnlExit_btnCancel.addActionListener(this);
        pnlBtn_btnAdd.addActionListener(this);
        pnlBtn_btnRemove.addActionListener(this);
        pnlBtn_btnRemoveAll.addActionListener(this);
        if (mode == Const.INCLUDE || mode == Const.TYPELIB) pnlOpt_btnI.addActionListener(this);
        pnlOpt_txt1.addKeyListener(this);
        if (mode == Const.MACRO || mode == Const.TYPELIB) pnlOpt_txtValue.addKeyListener(this);
        pnlList_lst.addListSelectionListener(this);
        pnlList_lst.addKeyListener(this);
        Dimension screenSize = xecl.f.getToolkit().getScreenSize();
        int top = 50;
        int left = screenSize.width / 2 - this.getWidth() / 2;
        this.setLocation(left, top);
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == pnlExit_btnOk) {
            ExitOk();
        }
        if (event.getSource() == pnlExit_btnCancel) {
            ExitCancel();
        }
        if (event.getSource() == pnlBtn_btnAdd) {
            if (mode == Const.INCLUDE) {
                addInclude();
            }
            if (mode == Const.MACRO || mode == Const.TYPELIB) {
                addElement();
            }
        }
        if (event.getSource() == pnlBtn_btnRemove) {
            int nb = 0;
            for (int i = 0; i < nbIncl; i++) if (pnlList_lst.isSelectedIndex(i)) nb++;
            if (nb > 0) {
                removeSelected(pnlList_lst.getSelectedIndices(), nb);
                pnlBtn_btnRemove.setEnabled(false);
                pnlOpt_txt1.requestFocus();
            }
        }
        if (event.getSource() == pnlBtn_btnRemoveAll) {
            if (mode == Const.INCLUDE) {
                include.removeAllElements();
                pnlList_lst.setListData(include);
                nbIncl = 0;
                pnlBtn_btnRemove.setEnabled(false);
            }
            if (mode == Const.MACRO || mode == Const.TYPELIB) {
                if (mode == Const.MACRO) {
                    macro.removeAllElements();
                    value.removeAllElements();
                }
                if (mode == Const.TYPELIB) {
                    lib.removeAllElements();
                    type.removeAllElements();
                }
                nbIncl = 0;
                pnlList_lst.setListData(macro);
                pnlBtn_btnRemove.setEnabled(false);
            }
            pnlBtn_btnRemoveAll.setEnabled(false);
            pnlOpt_txt1.requestFocus();
        }
        if (event.getSource() == pnlOpt_btnI) {
            xecl.chooser.setDialogTitle("Select the directory to include");
            xecl.chooser.setCurrentDirectory(new File("."));
            xecl.chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            xecl.chooser.setFileFilter(xecl.hFilter);
            int ret = xecl.chooser.showOpenDialog(xecl.f);
            if (xecl.chooser.getSelectedFile() != null && ret == 0) {
                pnlOpt_txt1.setText(xecl.chooser.getSelectedFile().toString());
                if (mode == Const.INCLUDE) {
                    addInclude();
                }
                if (mode == Const.TYPELIB) {
                    pnlOpt_txtValue.requestFocus();
                }
            }
        }
    }

    public void valueChanged(ListSelectionEvent event) {
        pnlBtn_btnRemove.setEnabled(true);
        flg_replaceElt = true;
        if (mode == Const.INCLUDE) {
            if (pnlList_lst.getSelectedIndex() >= 0 && pnlList_lst.getSelectedIndex() != include.size()) pnlOpt_txt1.setText((include.elementAt(pnlList_lst.getSelectedIndex())).toString());
        }
        if (mode == Const.MACRO) {
            if (pnlList_lst.getSelectedIndex() >= 0 && pnlList_lst.getSelectedIndex() != macro.size()) {
                pnlOpt_txt1.setText((macro.elementAt(pnlList_lst.getSelectedIndex())).toString());
                pnlOpt_txtValue.setText((value.elementAt(pnlList_lst.getSelectedIndex())).toString());
            }
        }
        if (mode == Const.TYPELIB) {
            if (pnlList_lst.getSelectedIndex() >= 0 && pnlList_lst.getSelectedIndex() != lib.size()) {
                pnlOpt_txt1.setText((lib.elementAt(pnlList_lst.getSelectedIndex())).toString());
                pnlOpt_txtValue.setText((type.elementAt(pnlList_lst.getSelectedIndex())).toString());
            }
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        if (mode == Const.INCLUDE) {
            if (pnlOpt_txt1.getText().equals("")) pnlBtn_btnAdd.setEnabled(false); else pnlBtn_btnAdd.setEnabled(true);
        } else {
            if (pnlOpt_txt1.getText().equals("") || pnlOpt_txtValue.getText().equals("")) pnlBtn_btnAdd.setEnabled(false); else pnlBtn_btnAdd.setEnabled(true);
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (pnlOpt_txt1.getText().equals("") && pnlOpt_txtValue.getText().equals("") && pnlOpt_txt1.hasFocus()) ExitOk(); else {
                if (mode == Const.INCLUDE) {
                    if (!pnlOpt_txt1.getText().equals("")) addInclude();
                    pnlOpt_txt1.requestFocus();
                }
                if (mode == Const.MACRO || mode == Const.TYPELIB) {
                    if (!pnlOpt_txt1.getText().equals("") && !pnlOpt_txtValue.getText().equals("")) addElement(); else if (pnlOpt_txt1.hasFocus() && !pnlOpt_txt1.getText().equals("") && pnlOpt_txtValue.getText().equals("")) pnlOpt_txtValue.requestFocus(); else if (pnlOpt_txtValue.hasFocus() && pnlOpt_txt1.getText().equals("") && !pnlOpt_txtValue.getText().equals("")) pnlOpt_txt1.requestFocus(); else if (pnlOpt_txt1.getText().equals("") || pnlOpt_txtValue.getText().equals("")) {
                        InfoDialog msgErr = new InfoDialog(xecl.f, "Error message", "Both Field have to be filled in before added");
                        msgErr.setModal(true);
                        msgErr.setVisible(true);
                    }
                }
            }
            flg_replaceElt = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) ExitCancel();
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            int nb = 0;
            for (int i = 0; i < nbIncl; i++) if (pnlList_lst.isSelectedIndex(i)) nb++;
            if (nb > 0) {
                removeSelected(pnlList_lst.getSelectedIndices(), nb);
                pnlBtn_btnRemove.setEnabled(false);
            }
            flg_replaceElt = false;
        }
    }

    public void addInclude() {
        if (flg_replaceElt) {
            include.set(pnlList_lst.getSelectedIndex(), pnlOpt_txt1.getText());
        } else include.add(nbIncl++, pnlOpt_txt1.getText());
        pnlOpt_txt1.setText("");
        pnlList_lst.setListData(include);
        pnlBtn_btnRemoveAll.setEnabled(true);
        pnlBtn_btnAdd.setEnabled(false);
        pnlOpt_txt1.requestFocus();
    }

    public void addElement() {
        if (mode == Const.MACRO) {
            if (!flg_replaceElt) {
                macro.add(nbIncl, pnlOpt_txt1.getText());
                value.add(nbIncl++, pnlOpt_txtValue.getText());
            } else {
                macro.set(pnlList_lst.getSelectedIndex(), pnlOpt_txt1.getText());
                value.set(pnlList_lst.getSelectedIndex(), pnlOpt_txtValue.getText());
            }
        }
        if (mode == Const.TYPELIB) {
            if (!flg_replaceElt) {
                lib.add(nbIncl, pnlOpt_txt1.getText());
                type.add(nbIncl++, pnlOpt_txtValue.getText());
            } else {
                lib.set(pnlList_lst.getSelectedIndex(), pnlOpt_txt1.getText());
                type.set(pnlList_lst.getSelectedIndex(), pnlOpt_txtValue.getText());
            }
        }
        setList();
        pnlOpt_txt1.setText("");
        pnlOpt_txtValue.setText("");
        pnlBtn_btnRemoveAll.setEnabled(true);
        pnlBtn_btnAdd.setEnabled(false);
        pnlOpt_txt1.requestFocus();
    }

    public void removeSelected(int[] ind, int nb) {
        for (int i = 0; i < nb; i++) {
            if (mode == Const.INCLUDE) {
                include.removeElementAt(ind[nb - 1 - i]);
            }
            if (mode == Const.MACRO) {
                macro.removeElementAt(ind[nb - 1 - i]);
                value.removeElementAt(ind[nb - 1 - i]);
            }
            nbIncl--;
        }
        if (mode == Const.INCLUDE) pnlList_lst.setListData(include);
        if (mode == Const.MACRO || mode == Const.TYPELIB) setList();
        pnlOpt_txt1.setText("");
        if (mode == Const.MACRO || mode == Const.TYPELIB) pnlOpt_txtValue.setText("");
        if (mode == Const.INCLUDE) if (nb != include.size()) pnlList_lst.setSelectedIndex(ind[0]);
    }

    public void ExitOk() {
        setNewOptions();
        this.dispose();
    }

    public void ExitCancel() {
        if (mode == Const.INCLUDE) {
            if (oldInclude.size() == 0) {
                xecl.EclAdv_vctI = new Vector(0);
                xecl.DlgEclMoreOpt.pnlEclCpre_chkI.setSelected(false);
            } else xecl.EclAdv_vctI = oldInclude;
        }
        if (mode == Const.MACRO) {
            if (oldMacro.size() == 0) {
                xecl.EclAdv_vctDMacro = new Vector(0);
                xecl.EclAdv_vctDValue = new Vector(0);
                xecl.DlgEclMoreOpt.pnlEclCpre_chkD.setSelected(false);
            } else {
                xecl.EclAdv_vctDMacro = oldMacro;
                xecl.EclAdv_vctDValue = oldValue;
            }
        }
        if (mode == Const.TYPELIB) {
            if (oldLib.size() == 0) {
                xecl.Vcc_vctTypelib_lib = new Vector(0);
                xecl.Vcc_vctTypelib_type = new Vector(0);
            } else {
                xecl.Vcc_vctTypelib_lib = oldLib;
                xecl.Vcc_vctTypelib_type = oldType;
            }
        }
        dispose();
    }

    void setNewOptions() {
        if (mode == Const.INCLUDE) {
            if (nbIncl != 0) {
                xecl.EclAdv_vctI = include;
                xecl.tabOptionEcl[Const.I] = true;
            } else {
                xecl.tabOptionEcl[Const.I] = false;
                xecl.DlgEclMoreOpt.pnlEclCpre_chkI.setSelected(false);
            }
        }
        if (mode == Const.MACRO) {
            if (nbIncl != 0) {
                xecl.EclAdv_vctDMacro = macro;
                xecl.EclAdv_vctDValue = value;
                xecl.tabOptionEcl[Const.D] = true;
            } else {
                xecl.tabOptionEcl[Const.D] = false;
                xecl.DlgEclMoreOpt.pnlEclCpre_chkD.setSelected(false);
            }
        }
        if (mode == Const.TYPELIB) {
            if (nbIncl != 0) {
                xecl.tabOptionEcl[Const.TYPELIB] = true;
                xecl.Vcc_vctTypelib_lib = lib;
                xecl.Vcc_vctTypelib_type = type;
            } else {
                xecl.tabOptionEcl[Const.TYPELIB] = false;
            }
        }
    }

    public void getCurrentOptions() {
        if (mode == Const.INCLUDE) {
            if (xecl.EclAdv_vctI.size() == 0) {
                include = new Vector(0);
                nbIncl = 0;
            } else {
                include = new Vector(xecl.EclAdv_vctI);
                nbIncl = include.size();
                pnlList_lst.setListData(macro);
            }
            oldInclude = new Vector(include);
        }
        if (mode == Const.MACRO) {
            if (xecl.EclAdv_vctDMacro.size() == 0) {
                macro = new Vector(0);
                value = new Vector(0);
                nbIncl = 0;
            } else {
                macro = new Vector(xecl.EclAdv_vctDMacro);
                value = new Vector(xecl.EclAdv_vctDValue);
                nbIncl = macro.size();
                setList();
            }
            oldMacro = new Vector(macro);
            oldValue = new Vector(value);
        }
        if (mode == Const.TYPELIB) {
            if (xecl.Vcc_vctTypelib_lib.size() == 0) {
                lib = new Vector(0);
                type = new Vector(0);
                nbIncl = 0;
            } else {
                lib = new Vector(xecl.Vcc_vctTypelib_lib);
                type = new Vector(xecl.Vcc_vctTypelib_type);
                nbIncl = lib.size();
                setList();
            }
            oldLib = new Vector(lib);
            oldType = new Vector(type);
        }
    }

    public void setList() {
        String[] tmp = new String[nbIncl];
        for (int i = 0; i < nbIncl; i++) {
            if (mode == Const.MACRO) tmp[i] = (String) macro.elementAt(i) + " " + (String) value.elementAt(i);
            if (mode == Const.TYPELIB) tmp[i] = (String) lib.elementAt(i) + " " + (String) type.elementAt(i);
        }
        pnlList_lst.setListData(tmp);
        flg_replaceElt = false;
    }
}

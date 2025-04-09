package form;

import engine.Array2DComparator;
import engine.Array2DComparatorCost;
import java.awt.Component;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import xml.XMLController;

/**
 *
 * @author bi
 */
public class Management extends JFrame {

    XMLController docFaction, docPlayer, docSquad, docWeapon, docEquipment, docSpecial, docDeployment;

    JFrame parent;

    /** Creates new form Management */
    public Management(JFrame parent, int tab) {
        initComponents();
        this.setLocation(300, 50);
        this.parent = parent;
        pnlTabMain.setSelectedIndex(tab);
        docFaction = new XMLController("factions");
        docPlayer = new XMLController("players");
        docSquad = new XMLController("squads");
        docWeapon = new XMLController("weapons");
        docEquipment = new XMLController("equipments");
        docSpecial = new XMLController("specials");
        docDeployment = new XMLController("deployments");
        refreshTable();
    }

    private void refreshTable() {
        autoSetTable(tblFaction, docFaction);
        autoSetTable(tblPlayer, docPlayer);
        autoSetTable(tblSquad, docSquad);
        autoSetTable(tblWeapon, docWeapon);
        autoSetTable(tblEquipment, docEquipment);
        autoSetTable(tblSpecial, docSpecial);
        autoSetTable(tblDeployment, docDeployment);
    }

    private void autoSetTable(JTable table, final XMLController document) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setModel(new AbstractTableModel() {

            public int getRowCount() {
                return document.table.length;
            }

            public int getColumnCount() {
                return document.columnName.length;
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                return document.table[rowIndex][columnIndex];
            }

            @Override
            public String getColumnName(int columnIndex) {
                return document.columnName[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                document.table[rowIndex][columnIndex] = aValue.toString();
            }
        });
        int margin = 5;
        for (int i = 0; i < table.getColumnCount(); i++) {
            int vColIndex = i;
            DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
            TableColumn col = colModel.getColumn(vColIndex);
            int width = 0;
            TableCellRenderer renderer = col.getHeaderRenderer();
            if (renderer == null) {
                renderer = table.getTableHeader().getDefaultRenderer();
            }
            Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);
            width = comp.getPreferredSize().width;
            for (int r = 0; r < table.getRowCount(); r++) {
                renderer = table.getCellRenderer(r, vColIndex);
                comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, vColIndex), false, false, r, vColIndex);
                width = Math.max(width, comp.getPreferredSize().width);
            }
            width += 2 * margin;
            col.setPreferredWidth(width);
        }
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        table.getTableHeader().setReorderingAllowed(false);
    }

    private void addRecord(JTable table, XMLController document) {
        int addRow = table.getSelectedRow();
        int oldRowLength = document.table.length;
        int oldColumnLength = document.table[0].length;
        String[][] newTable = Arrays.copyOf(document.table, oldRowLength + 1);
        newTable[oldRowLength] = new String[oldColumnLength];
        if (addRow != -1) {
            System.arraycopy(document.table[addRow], 0, newTable[oldRowLength], 0, oldColumnLength);
        } else {
            Arrays.fill(newTable[oldRowLength], "0");
        }
        document.table = newTable;
        refreshTable();
        document.writeXML();
    }

    private void removeRecord(JTable table, XMLController document) {
        int[] deleteRow = table.getSelectedRows();
        for (int i = 0; i < deleteRow.length; i++) {
            int realDeleteRow = deleteRow[i] - i;
            int oldRowLength = document.table.length;
            String[][] newTable = Arrays.copyOf(document.table, oldRowLength - 1);
            if (realDeleteRow != table.getRowCount() - 1) {
                System.arraycopy(document.table, realDeleteRow + 1, newTable, realDeleteRow, oldRowLength - realDeleteRow - 1);
            }
            document.table = newTable;
            refreshTable();
        }
    }

    private void moveUp(JTable table, XMLController document) {
        int rowIdx = table.getSelectedRow();
        if (rowIdx > 0) {
            String[] data = document.table[rowIdx];
            document.table[rowIdx] = document.table[rowIdx - 1];
            document.table[rowIdx - 1] = data;
            document.writeXML();
            refreshTable();
        }
    }

    private void moveDown(JTable table, XMLController document) {
        int rowIdx = table.getSelectedRow();
        if (rowIdx < document.table.length - 1) {
            String[] data = document.table[rowIdx];
            document.table[rowIdx] = document.table[rowIdx + 1];
            document.table[rowIdx + 1] = data;
            document.writeXML();
            refreshTable();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        pnlTitle = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        pnlTabMain = new javax.swing.JTabbedPane();
        pnlFaction = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblFaction = new javax.swing.JTable();
        pnlPlayer = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblPlayer = new javax.swing.JTable();
        pnlSquad = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblSquad = new javax.swing.JTable();
        pnlSquadDmg = new javax.swing.JPanel();
        txtAvgDmg = new javax.swing.JTextField();
        btnAvgDmg = new javax.swing.JButton();
        pnlWeapon = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblWeapon = new javax.swing.JTable();
        pnlWeaponCost = new javax.swing.JPanel();
        txtWeaponCost = new javax.swing.JTextField();
        btnWeaponCost = new javax.swing.JButton();
        pnlEquipment = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblEquipment = new javax.swing.JTable();
        pnlSpecial = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tblSpecial = new javax.swing.JTable();
        pnlDeployment = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tblDeployment = new javax.swing.JTable();
        pnlButton = new javax.swing.JPanel();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnUp = new javax.swing.JButton();
        btnDown = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Warhammer 40.000: Text Game - Management");
        setMinimumSize(new java.awt.Dimension(800, 600));
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));
        pnlTitle.setMaximumSize(new java.awt.Dimension(32767, 22));
        pnlTitle.setLayout(new java.awt.GridLayout(1, 0));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Warhammer 40.000: Text Game");
        pnlTitle.add(jLabel1);
        getContentPane().add(pnlTitle);
        pnlFaction.setLayout(new javax.swing.BoxLayout(pnlFaction, javax.swing.BoxLayout.Y_AXIS));
        tblFaction.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null, null, null }, { null, null, null, null }, { null, null, null, null }, { null, null, null, null } }, new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
        tblFaction.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane1.setViewportView(tblFaction);
        pnlFaction.add(jScrollPane1);
        pnlTabMain.addTab("Faction", pnlFaction);
        pnlPlayer.setLayout(new javax.swing.BoxLayout(pnlPlayer, javax.swing.BoxLayout.Y_AXIS));
        tblPlayer.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null, null, null }, { null, null, null, null }, { null, null, null, null }, { null, null, null, null } }, new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
        jScrollPane2.setViewportView(tblPlayer);
        pnlPlayer.add(jScrollPane2);
        pnlTabMain.addTab("Player", pnlPlayer);
        pnlSquad.setLayout(new javax.swing.BoxLayout(pnlSquad, javax.swing.BoxLayout.Y_AXIS));
        tblSquad.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null, null, null }, { null, null, null, null }, { null, null, null, null }, { null, null, null, null } }, new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
        jScrollPane3.setViewportView(tblSquad);
        pnlSquad.add(jScrollPane3);
        pnlSquadDmg.setMaximumSize(new java.awt.Dimension(2147483647, 83));
        pnlSquadDmg.setLayout(new javax.swing.BoxLayout(pnlSquadDmg, javax.swing.BoxLayout.LINE_AXIS));
        txtAvgDmg.setEditable(false);
        pnlSquadDmg.add(txtAvgDmg);
        btnAvgDmg.setText("Calculate");
        btnAvgDmg.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAvgDmgActionPerformed(evt);
            }
        });
        pnlSquadDmg.add(btnAvgDmg);
        pnlSquad.add(pnlSquadDmg);
        pnlTabMain.addTab("Squad", pnlSquad);
        pnlWeapon.setLayout(new javax.swing.BoxLayout(pnlWeapon, javax.swing.BoxLayout.Y_AXIS));
        tblWeapon.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null, null, null }, { null, null, null, null }, { null, null, null, null }, { null, null, null, null } }, new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
        jScrollPane4.setViewportView(tblWeapon);
        pnlWeapon.add(jScrollPane4);
        pnlWeaponCost.setMaximumSize(new java.awt.Dimension(32767, 83));
        pnlWeaponCost.setLayout(new javax.swing.BoxLayout(pnlWeaponCost, javax.swing.BoxLayout.LINE_AXIS));
        txtWeaponCost.setEditable(false);
        pnlWeaponCost.add(txtWeaponCost);
        btnWeaponCost.setText("Calculate");
        btnWeaponCost.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWeaponCostActionPerformed(evt);
            }
        });
        pnlWeaponCost.add(btnWeaponCost);
        pnlWeapon.add(pnlWeaponCost);
        pnlTabMain.addTab("Weapon", pnlWeapon);
        pnlEquipment.setLayout(new javax.swing.BoxLayout(pnlEquipment, javax.swing.BoxLayout.LINE_AXIS));
        tblEquipment.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null, null, null }, { null, null, null, null }, { null, null, null, null }, { null, null, null, null } }, new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
        jScrollPane5.setViewportView(tblEquipment);
        pnlEquipment.add(jScrollPane5);
        pnlTabMain.addTab("Equipment", pnlEquipment);
        pnlSpecial.setLayout(new javax.swing.BoxLayout(pnlSpecial, javax.swing.BoxLayout.LINE_AXIS));
        tblSpecial.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null, null, null }, { null, null, null, null }, { null, null, null, null }, { null, null, null, null } }, new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
        jScrollPane6.setViewportView(tblSpecial);
        pnlSpecial.add(jScrollPane6);
        pnlTabMain.addTab("Special", pnlSpecial);
        pnlDeployment.setLayout(new javax.swing.BoxLayout(pnlDeployment, javax.swing.BoxLayout.LINE_AXIS));
        tblDeployment.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null, null, null }, { null, null, null, null }, { null, null, null, null }, { null, null, null, null } }, new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
        jScrollPane7.setViewportView(tblDeployment);
        pnlDeployment.add(jScrollPane7);
        pnlTabMain.addTab("Deployment", pnlDeployment);
        getContentPane().add(pnlTabMain);
        pnlButton.setMaximumSize(new java.awt.Dimension(32767, 23));
        pnlButton.setLayout(new java.awt.GridLayout(1, 0));
        btnAdd.setText("Add");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });
        pnlButton.add(btnAdd);
        btnRemove.setText("Remove");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });
        pnlButton.add(btnRemove);
        btnUp.setText("Move Up");
        btnUp.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpActionPerformed(evt);
            }
        });
        pnlButton.add(btnUp);
        btnDown.setText("Move Down");
        btnDown.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownActionPerformed(evt);
            }
        });
        pnlButton.add(btnDown);
        btnUpdate.setText("Update");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });
        pnlButton.add(btnUpdate);
        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });
        pnlButton.add(btnClose);
        getContentPane().add(pnlButton);
        pack();
    }

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {
        switch(pnlTabMain.getSelectedIndex()) {
            case 0:
                addRecord(tblFaction, docFaction);
                break;
            case 1:
                addRecord(tblPlayer, docPlayer);
                break;
            case 2:
                addRecord(tblSquad, docSquad);
                break;
            case 3:
                addRecord(tblWeapon, docWeapon);
                break;
            case 4:
                addRecord(tblEquipment, docEquipment);
                break;
            case 5:
                addRecord(tblSpecial, docSpecial);
                break;
            case 6:
                addRecord(tblDeployment, docDeployment);
                break;
            default:
                break;
        }
    }

    private void btnFactionClose1ActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {
        Arrays.sort(docFaction.table, new Array2DComparator(0));
        Arrays.sort(docFaction.table, new Array2DComparator(1));
        Arrays.sort(docSquad.table, new Array2DComparatorCost(2));
        Arrays.sort(docSquad.table, new Array2DComparator(1));
        Arrays.sort(docWeapon.table, new Array2DComparator(0));
        Arrays.sort(docWeapon.table, new Array2DComparatorCost(7));
        Arrays.sort(docEquipment.table, new Array2DComparator(2));
        Arrays.sort(docEquipment.table, new Array2DComparator(0));
        Arrays.sort(docDeployment.table, new Array2DComparatorCost(0));
        refreshTable();
        docFaction.writeXML();
        docPlayer.writeXML();
        docSquad.writeXML();
        docWeapon.writeXML();
        docEquipment.writeXML();
        docSpecial.writeXML();
        docDeployment.writeXML();
    }

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {
        int answer = JOptionPane.showConfirmDialog(this, "Would you like to update info?", "Update", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            docFaction.writeXML();
            docPlayer.writeXML();
            docSquad.writeXML();
            docWeapon.writeXML();
            docEquipment.writeXML();
            docSpecial.writeXML();
            docDeployment.writeXML();
            this.dispose();
            parent.setVisible(true);
        } else if (answer == JOptionPane.NO_OPTION) {
            this.dispose();
            parent.setVisible(true);
        }
    }

    private void btnUpActionPerformed(java.awt.event.ActionEvent evt) {
        switch(pnlTabMain.getSelectedIndex()) {
            case 0:
                moveUp(tblFaction, docFaction);
                break;
            case 1:
                moveUp(tblPlayer, docPlayer);
                break;
            case 2:
                moveUp(tblSquad, docSquad);
                break;
            case 3:
                moveUp(tblWeapon, docWeapon);
                break;
            case 4:
                moveUp(tblEquipment, docEquipment);
                break;
            case 5:
                moveUp(tblSpecial, docSpecial);
                break;
            case 6:
                moveUp(tblDeployment, docDeployment);
                break;
            default:
                break;
        }
    }

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {
        switch(pnlTabMain.getSelectedIndex()) {
            case 0:
                removeRecord(tblFaction, docFaction);
                break;
            case 1:
                removeRecord(tblPlayer, docPlayer);
                break;
            case 2:
                removeRecord(tblSquad, docSquad);
                break;
            case 3:
                removeRecord(tblWeapon, docWeapon);
                break;
            case 4:
                removeRecord(tblEquipment, docEquipment);
                break;
            case 5:
                removeRecord(tblSpecial, docSpecial);
                break;
            case 6:
                removeRecord(tblDeployment, docDeployment);
                break;
            default:
                break;
        }
    }

    private void btnDownActionPerformed(java.awt.event.ActionEvent evt) {
        switch(pnlTabMain.getSelectedIndex()) {
            case 0:
                moveDown(tblFaction, docFaction);
                break;
            case 1:
                moveDown(tblPlayer, docPlayer);
                break;
            case 2:
                moveDown(tblSquad, docSquad);
                break;
            case 3:
                moveDown(tblWeapon, docWeapon);
                break;
            case 4:
                moveDown(tblEquipment, docEquipment);
                break;
            case 5:
                moveDown(tblSpecial, docSpecial);
                break;
            case 6:
                moveDown(tblDeployment, docDeployment);
                break;
            default:
                break;
        }
    }

    private void btnAvgDmgActionPerformed(java.awt.event.ActionEvent evt) {
        int checkRow = tblSquad.getSelectedRow();
        if (checkRow != -1) {
            double attackCost = 0;
            double priCost = 0;
            double health = Double.parseDouble(docSquad.table[checkRow][3]);
            double armor = Double.parseDouble(docSquad.table[checkRow][4]);
            double size = Double.parseDouble(docSquad.table[checkRow][5]);
            double remain = size;
            for (int i = 0; i < docEquipment.table.length; i++) {
                if (docEquipment.table[i][0].equals(docSquad.table[checkRow][0])) {
                    for (int j = 0; j < docWeapon.table.length; j++) {
                        if (docEquipment.table[i][1].equals(docWeapon.table[j][0])) {
                            int weaponAmount = Integer.valueOf(docEquipment.table[i][2]);
                            if (weaponAmount == 0) {
                                priCost = Integer.valueOf(docWeapon.table[j][7]);
                            } else {
                                attackCost += Integer.valueOf(docWeapon.table[j][7]) * weaponAmount;
                                remain -= weaponAmount;
                            }
                        }
                    }
                }
            }
            if (remain > 0) {
                attackCost += priCost * remain;
                remain = 0;
            }
            double defChance = armor / 100;
            double breakChance = 1 - defChance;
            double fullHealth = size * health;
            double defRate = 10;
            double breakRate = 10 * defChance;
            double defense = (fullHealth * (defChance * defRate + breakChance * breakRate) / 100);
            double attack = attackCost / 20;
            double total = defense + attack;
            int cost = (int) Math.round((attack + defense) * 2.3) * 10;
            txtAvgDmg.setText("Defense:" + String.valueOf(defense) + "   Suggest Cost:" + String.valueOf(cost) + "   Is this Zero:" + String.valueOf(remain));
        }
    }

    private void btnWeaponCostActionPerformed(java.awt.event.ActionEvent evt) {
        int checkRow = tblWeapon.getSelectedRow();
        if (checkRow != -1) {
            double dmg = Double.parseDouble(docWeapon.table[checkRow][1]);
            double pen = Double.parseDouble(docWeapon.table[checkRow][2]);
            double acc = Double.parseDouble(docWeapon.table[checkRow][3]);
            double round = Double.parseDouble(docWeapon.table[checkRow][4]);
            double blast = Double.parseDouble(docWeapon.table[checkRow][5]);
            double hitChance = acc;
            double missChance = 1 - hitChance;
            double penChance = pen / 100;
            double bounceChance = 1 - penChance;
            double blastSize = (blast + 1) / 2;
            double hitDmg = dmg * blastSize;
            double bounceDmg = dmg * blastSize * penChance;
            double missDmg = dmg * (blastSize - 1) * penChance;
            double attack = ((hitChance * (penChance * hitDmg + bounceChance * bounceDmg) + missChance * missDmg) * round);
            int cost = (int) Math.round(attack * 20);
            txtWeaponCost.setText("Attack:" + String.valueOf(attack) + "   Suggest Cost:" + String.valueOf(cost));
        }
    }

    private javax.swing.JButton btnAdd;

    private javax.swing.JButton btnAvgDmg;

    private javax.swing.JButton btnClose;

    private javax.swing.JButton btnDown;

    private javax.swing.JButton btnRemove;

    private javax.swing.JButton btnUp;

    private javax.swing.JButton btnUpdate;

    private javax.swing.JButton btnWeaponCost;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JScrollPane jScrollPane2;

    private javax.swing.JScrollPane jScrollPane3;

    private javax.swing.JScrollPane jScrollPane4;

    private javax.swing.JScrollPane jScrollPane5;

    private javax.swing.JScrollPane jScrollPane6;

    private javax.swing.JScrollPane jScrollPane7;

    private javax.swing.JPanel pnlButton;

    private javax.swing.JPanel pnlDeployment;

    private javax.swing.JPanel pnlEquipment;

    private javax.swing.JPanel pnlFaction;

    private javax.swing.JPanel pnlPlayer;

    private javax.swing.JPanel pnlSpecial;

    private javax.swing.JPanel pnlSquad;

    private javax.swing.JPanel pnlSquadDmg;

    private javax.swing.JTabbedPane pnlTabMain;

    private javax.swing.JPanel pnlTitle;

    private javax.swing.JPanel pnlWeapon;

    private javax.swing.JPanel pnlWeaponCost;

    private javax.swing.JTable tblDeployment;

    private javax.swing.JTable tblEquipment;

    private javax.swing.JTable tblFaction;

    private javax.swing.JTable tblPlayer;

    private javax.swing.JTable tblSpecial;

    private javax.swing.JTable tblSquad;

    private javax.swing.JTable tblWeapon;

    private javax.swing.JTextField txtAvgDmg;

    private javax.swing.JTextField txtWeaponCost;
}

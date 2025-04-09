package realcix20.classes.plugins;

import java.awt.Window;
import java.io.File;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Random;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import realcix20.cixfiles.CIXFile;
import realcix20.cixfiles.CIXFileData;
import realcix20.classes.PartnerClass;
import realcix20.classes.basic.BaseClass;
import realcix20.classes.basic.Cell;
import realcix20.classes.basic.Row;
import realcix20.guis.utils.DialogManager;
import realcix20.guis.utils.TxtManager;
import realcix20.guis.views.MainView;
import realcix20.guis.views.ObjectInfo;
import realcix20.utils.DAO;
import realcix20.utils.Resources;

/**
 *
 * @author JerryChen
 */
public class ObjectPlugin implements Serializable {

    private Window window;

    private BaseClass object;

    private Row row;

    private CIXFile cixFile;

    private File file;

    public ObjectPlugin(Window window, BaseClass object, Row row, String action) {
        this.window = window;
        this.object = object;
        this.row = row;
        if (object instanceof PartnerClass) {
            dealPartnerThings(action);
        } else {
        }
    }

    public ObjectPlugin(Window window, BaseClass object, Row row, File file, CIXFile cixFile, String action) {
        this.file = file;
        this.window = window;
        this.object = object;
        this.row = row;
        this.cixFile = cixFile;
        if (object instanceof PartnerClass) {
            dealPartnerThings(action);
        } else {
        }
    }

    public ObjectPlugin(Window window, BaseClass object, Row row, CIXFile cixFile, String action) {
        this.window = window;
        this.object = object;
        this.row = row;
        this.cixFile = cixFile;
        if (object instanceof PartnerClass) {
            dealPartnerThings(action);
        } else {
        }
    }

    public ObjectPlugin(Window window, File file) {
        this.window = window;
        this.file = file;
        CIXFile cixFile = new CIXFile();
        try {
            cixFile.importFile(file);
            this.cixFile = cixFile;
            analyType();
        } catch (Exception e) {
            DialogManager.showMessageDialog(window, TxtManager.getTxt("PARTNER.IMPORT.NOTVAILDFILE"));
            e.printStackTrace();
        }
    }

    private void analyType() {
        MainView container = (MainView) window;
        String type = cixFile.getData().getType();
        Row row = cixFile.getData().getRow(cixFile.getPublicKey());
        if (type.equals("Confirm Add Account")) {
            JTree tree = container.getTree();
            for (int i = 1; i <= tree.getRowCount(); i++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getPathForRow(i).getLastPathComponent();
                if (node != null) {
                    ObjectInfo oj = (ObjectInfo) node.getUserObject();
                    if ((oj.flag.equals("object")) && (oj.clsId == 250)) {
                        tree.setSelectionPath(tree.getPathForRow(i));
                        container.getListener().initialObjectAdd(this, "Confirm Add Account Request");
                        break;
                    }
                }
            }
        } else if (type.equals("Re Confirm Add Account")) {
            dealPartnerThings("Re Confirm Add Account");
        } else if (type.equals("Used")) {
            DialogManager.showMessageDialog(window, TxtManager.getTxt("PARTNER.IMPORT.USEDFILE"));
        }
    }

    private void dealPartnerThings(String action) {
        if (action.equals("ADD")) {
            Cell cell = findCell(getRow(), "P", "NS");
            if ((cell != null) && (!cell.getColumnValue().equals("ME"))) {
                try {
                    String type = "Confirm Add Account";
                    Iterator rowIter = getRow().getRowSet().getRows().iterator();
                    Cell pCell = findCell(getRow(), "P", "DEFPA");
                    while (rowIter.hasNext()) {
                        Row childRow = (Row) rowIter.next();
                        Cell cCell = findCell(childRow, "PA", "PA");
                        if ((childRow != getRow()) && (cCell.getColumnValue().equals(pCell.getColumnValue()))) {
                            CIXFileData data = new CIXFileData(type, getPrivateKey(), childRow);
                            CIXFile cixFile = new CIXFile(getPublicKey(), data);
                            Random random = new Random();
                            String fileName = "confirm_add_account_" + random.nextInt(25000);
                            cixFile.exportFile(fileName);
                            String str = TxtManager.getTxt("PARTNER.IMPORT.ADDPARTNER");
                            str = str.replaceAll("%", fileName);
                            DialogManager.showMessageDialog(getWindow(), str);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ((cell != null) && (cell.getColumnValue().equals("ME"))) {
                try {
                    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                    keyGen.initialize(1024);
                    KeyPair key = keyGen.generateKeyPair();
                    Iterator rowIter = getRow().getRowSet().getRows().iterator();
                    while (rowIter.hasNext()) {
                        Row childRow = (Row) rowIter.next();
                        if (childRow != getRow()) {
                            Cell publicKeyCell = findCell(childRow, "PA", "PUBKEY");
                            Cell privateKeyCell = findCell(childRow, "PA", "PRIKEY");
                            publicKeyCell.setColumnValue(key.getPublic());
                            privateKeyCell.setColumnValue(key.getPrivate());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (action.equals("Confirm Add Account Request")) {
            Cell cell = findCell(getRow(), "P", "NS");
            if ((cell != null) && (!cell.getColumnValue().equals("ME"))) {
                try {
                    Iterator rowIter = getRow().getRowSet().getRows().iterator();
                    while (rowIter.hasNext()) {
                        Row childRow = (Row) rowIter.next();
                        if (childRow != getRow()) {
                            Cell publicKeyCell = findCell(childRow, "PA", "PUBKEY");
                            publicKeyCell.setColumnValue(getCixFile().getPublicKey());
                        }
                    }
                    String type = "Re Confirm Add Account";
                    rowIter = getRow().getRowSet().getRows().iterator();
                    Cell pCell = findCell(getRow(), "P", "DEFPA");
                    while (rowIter.hasNext()) {
                        Row childRow = (Row) rowIter.next();
                        Cell cCell = findCell(childRow, "PA", "PA");
                        if ((childRow != getRow()) && (cCell.getColumnValue().equals(pCell.getColumnValue()))) {
                            CIXFileData data = new CIXFileData(type, getPrivateKey(), childRow);
                            CIXFile cixFile = new CIXFile(getPublicKey(), data);
                            Random random = new Random();
                            String fileName = "re_confirm_add_account_" + random.nextInt(25000);
                            cixFile.exportFile(fileName);
                            String str = TxtManager.getTxt("PARTNER.IMPORT.CONFIRMADDPARTNER");
                            str = str.replaceAll("%", fileName);
                            DialogManager.showMessageDialog(getWindow(), str);
                            this.cixFile.getData().setType("Used");
                            this.cixFile.exportFile("used_" + getFile().getName().substring(0, getFile().getName().length() - 2));
                            file.delete();
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (action.equals("Re Confirm Add Account")) {
            MainView container = (MainView) window;
            JTree tree = container.getTree();
            Row tRow = cixFile.getData().getRow(cixFile.getPublicKey());
            Cell cell1 = findCell(tRow, "P", "NS");
            Cell cell2 = findCell(tRow, "P", "P");
            Cell cell3 = findCell(tRow, "P", "URNS");
            Cell cell4 = findCell(tRow, "P", "URP");
            for (int i = 1; i <= tree.getRowCount(); i++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getPathForRow(i).getLastPathComponent();
                if (node != null) {
                    ObjectInfo oj = (ObjectInfo) node.getUserObject();
                    if ((oj.flag.equals("object")) && (oj.clsId == 250)) {
                        tree.setSelectionPath(tree.getPathForRow(i));
                        Iterator rowIter = container.getCurrentObject().getRows().iterator();
                        boolean findRow = false;
                        while (rowIter.hasNext()) {
                            Row cRow = (Row) rowIter.next();
                            Cell ccell1 = findCell(cRow, "P", "NS");
                            Cell ccell2 = findCell(cRow, "P", "P");
                            if ((ccell1.getColumnValue().equals(cell3.getColumnValue())) && (ccell2.getColumnValue().equals(cell4.getColumnValue()))) {
                                findRow = true;
                                Iterator childRowIter = cRow.getRowSet().getRows().iterator();
                                while (childRowIter.hasNext()) {
                                    Row childRow = (Row) childRowIter.next();
                                    childRow.setModify(true);
                                    Cell ccell3 = findCell(childRow, "P", "URNS");
                                    Cell ccell4 = findCell(childRow, "P", "URP");
                                    Cell publicKeyCell = findCell(childRow, "PA", "PUBKEY");
                                    ccell3.setColumnValue(cell1.getColumnValue());
                                    ccell4.setColumnValue(cell2.getColumnValue());
                                    publicKeyCell.setColumnValue(cixFile.getPublicKey());
                                }
                                container.getCurrentObject().classUpdate_WholeObject(cRow, false);
                                break;
                            }
                        }
                        if (findRow) {
                            tree.setSelectionPath(tree.getPathForRow(i));
                            String str = TxtManager.getTxt("PARTNER.IMPORT.RECONFIRMADDPARTNER");
                            str = str.replaceAll("%", cell3.getColumnValue().toString());
                            str = str.replaceAll("#", cell4.getColumnValue().toString());
                            DialogManager.showMessageDialog(container, str);
                        } else {
                            String str = TxtManager.getTxt("PARTNER.IMPORT.RECONFIRMADDPARTNER.FAIL");
                            DialogManager.showMessageDialog(container, str);
                        }
                        this.cixFile.getData().setType("Used");
                        this.cixFile.exportFile("used_" + getFile().getName().substring(0, getFile().getName().length() - 2));
                        file.delete();
                        break;
                    }
                }
            }
        }
    }

    private PublicKey getPublicKey() {
        PublicKey publicKey = null;
        DAO dao = DAO.getInstance();
        dao.query(Resources.SELECT_KEYPAIR_SQL);
        ResultSet rs = dao.executeQuery();
        try {
            if (rs.next()) publicKey = (PublicKey) rs.getObject("PUBKEY");
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    private PrivateKey getPrivateKey() {
        PrivateKey privateKey = null;
        DAO dao = DAO.getInstance();
        dao.query(Resources.SELECT_KEYPAIR_SQL);
        ResultSet rs = dao.executeQuery();
        try {
            if (rs.next()) privateKey = (PrivateKey) rs.getObject("PRIKEY");
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    private Cell findCell(Row row, String tableName, String columnName) {
        Cell tCell = null;
        Iterator cellIter = row.getNewCells().iterator();
        while (cellIter.hasNext()) {
            Cell cell = (Cell) cellIter.next();
            if ((cell.getTableName().equals(tableName)) && (cell.getColumnName().equals(columnName))) {
                tCell = cell;
                break;
            }
        }
        return tCell;
    }

    public CIXFile getCixFile() {
        return cixFile;
    }

    public Window getWindow() {
        return window;
    }

    public BaseClass getObject() {
        return object;
    }

    public Row getRow() {
        return row;
    }

    public File getFile() {
        return file;
    }
}

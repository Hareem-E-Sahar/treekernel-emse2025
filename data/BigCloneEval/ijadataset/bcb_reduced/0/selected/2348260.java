package org.digitall.common.cashflow.classes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import org.digitall.lib.sql.LibSQL;

public class CCExpenditureAccountsGroup extends Vector {

    private CostsCentre costsCentre;

    private int indexSelected;

    public CCExpenditureAccountsGroup() {
    }

    public CCExpenditureAccountsGroup(CostsCentre _costsCentre) {
        costsCentre = _costsCentre;
    }

    public void sort() {
        Object ccTmp;
        for (int i = 0; i < size() - 1; i++) {
            for (int j = i + 1; j < size(); j++) {
                int idExpTypei = ((ExpenditureAccount.CCItem) elementAt(i)).getIDExpenditureAccount();
                int idExpTypej = ((ExpenditureAccount.CCItem) elementAt(i)).getIDExpenditureAccount();
                if (idExpTypei > idExpTypej) {
                    ccTmp = elementAt(i);
                    setElementAt(elementAt(j), i);
                    setElementAt(ccTmp, j);
                }
            }
        }
    }

    public ExpenditureAccount.CCItem getElement(int _idExpenditureAccount) {
        int initIndex = 0;
        int endIndex = size() - 1;
        int midIndex = 0;
        while (initIndex <= endIndex) {
            midIndex = (initIndex + endIndex) / 2;
            int idExpAcc = ((ExpenditureAccount.CCItem) elementAt(midIndex)).getIDExpenditureAccount();
            if (_idExpenditureAccount == idExpAcc) {
                break;
            } else if (_idExpenditureAccount > idExpAcc) {
                initIndex = midIndex + 1;
            } else if (_idExpenditureAccount < idExpAcc) {
                endIndex = midIndex - 1;
            }
        }
        if (initIndex > endIndex) {
            return null;
        } else {
            indexSelected = midIndex;
            return (ExpenditureAccount.CCItem) elementAt(midIndex);
        }
    }

    public int getIndexSelected() {
        return indexSelected;
    }

    public JTree createTree() throws Exception {
        DefaultMutableTreeNode tree = new DefaultMutableTreeNode(costsCentre);
        String params = costsCentre.getIdCostCentre() + ",0";
        ResultSet treeRS = LibSQL.exFunction("cashflow.getAllExpenditureAccountsByCostCentre", params);
        getSubTree(tree, treeRS, 0);
        sort();
        return new JTree(tree);
    }

    private void getSubTree(DefaultMutableTreeNode _parentTree, ResultSet _rs, int _parent) throws SQLException {
        _rs.beforeFirst();
        while (_rs.next()) {
            if (_rs.getInt("idparent") == _parent) {
                ExpenditureAccount.CCItem objItem = new ExpenditureAccount.CCItem(costsCentre);
                objItem.setIDExpenditureAccount(_rs.getInt("idaccount"));
                objItem.setName(_rs.getString("name"));
                objItem.setIdParent(_rs.getInt("idparent"));
                objItem.setInitalAmount(_rs.getDouble("initialamount"));
                objItem.setInitalAmountP(_rs.getDouble("initialamountp"));
                objItem.setSpentAmount(_rs.getDouble("spentamount"));
                objItem.setSpentAmountP(_rs.getDouble("spentamountp"));
                objItem.setAvailableCCAmount(_rs.getDouble("availableamount"));
                objItem.setModifiedCCAmount(_rs.getDouble("modifiedamount"));
                this.add(objItem);
                DefaultMutableTreeNode childrenNodes = new DefaultMutableTreeNode(objItem);
                _parentTree.add(childrenNodes);
                int previous = _rs.getRow();
                getSubTree(childrenNodes, _rs, _rs.getInt("idaccount"));
                _rs.absolute(previous);
            }
        }
    }
}

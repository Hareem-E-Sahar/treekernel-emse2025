package org.digitall.common.cashflow.classes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import org.digitall.lib.data.Format;
import org.digitall.common.cashflow.classes.Budget;
import org.digitall.common.cashflow.classes.CostsCentre;
import org.digitall.lib.components.Advisor;
import org.digitall.lib.sql.LibSQL;

public class ExpenditureAccountsGroup extends Vector {

    private int indexSelected;

    private Budget budget;

    private CostsCentre costsCentre;

    public ExpenditureAccountsGroup(Budget _budget) {
        budget = _budget;
    }

    public ExpenditureAccountsGroup(CostsCentre _costsCentre) {
        costsCentre = _costsCentre;
    }

    public ExpenditureAccountsGroup() {
    }

    public void sort() {
        Object ccTmp;
        for (int i = 0; i < size() - 1; i++) {
            for (int j = i + 1; j < size(); j++) {
                int idExpTypei = ((ExpenditureAccount) elementAt(i)).getIDExpenditureAccount();
                int idExpTypej = ((ExpenditureAccount) elementAt(j)).getIDExpenditureAccount();
                if (idExpTypei > idExpTypej) {
                    ccTmp = elementAt(i);
                    setElementAt(elementAt(j), i);
                    setElementAt(ccTmp, j);
                }
            }
        }
    }

    public ExpenditureAccount getElement(int _idExpenditureAccount) {
        int initIndex = 0;
        int endIndex = size() - 1;
        int midIndex = 0;
        while (initIndex <= endIndex) {
            midIndex = (initIndex + endIndex) / 2;
            int idExpType = ((ExpenditureAccount) elementAt(midIndex)).getIDExpenditureAccount();
            if (_idExpenditureAccount == idExpType) {
                break;
            } else if (_idExpenditureAccount > idExpType) {
                initIndex = midIndex + 1;
            } else if (_idExpenditureAccount < idExpType) {
                endIndex = midIndex - 1;
            }
        }
        if (initIndex > endIndex) {
            return null;
        } else {
            indexSelected = midIndex;
            return (ExpenditureAccount) elementAt(midIndex);
        }
    }

    public int getIndexSelected() {
        return indexSelected;
    }

    public JTree createTree(boolean _fixed) throws Exception {
        DefaultMutableTreeNode tree;
        ResultSet treeRS;
        if (budget != null || costsCentre != null) {
            if (budget != null) {
                tree = new DefaultMutableTreeNode(budget);
                String params = budget.getIdBudget() + ",0,'" + _fixed + "'";
                treeRS = LibSQL.exFunction("cashflow.getallBudgetsByExpenditureAccount", params);
            } else {
                tree = new DefaultMutableTreeNode(costsCentre);
                String params = costsCentre.getIdCostCentre() + ",0";
                treeRS = LibSQL.exFunction("cashflow.getallExpenditureAccountsByCostsCentre", params);
            }
        } else {
            tree = new DefaultMutableTreeNode("Tipos de Gastos");
            treeRS = LibSQL.exFunction("accounting.getAllExpenditureAccounts", "0");
        }
        createSubTree(tree, treeRS, _fixed);
        sort();
        return new JTree(tree);
    }

    public void createSubTree(DefaultMutableTreeNode _parentTree, ResultSet _rs, boolean _fixed) throws SQLException {
        while (_rs.next()) {
            ExpenditureAccount objExpenditureAccount = new ExpenditureAccount();
            String params;
            ResultSet subTreeRS;
            if (budget != null || costsCentre != null) {
                if (budget != null) {
                    objExpenditureAccount.setIdBudgetExpenditureAccount(_rs.getInt("idBudgetExpenditureAccount"));
                    objExpenditureAccount.setIdBudget(_rs.getInt("idBudget"));
                    objExpenditureAccount.setAssignedAmountToET(_rs.getDouble("assignedamounttoet"));
                    objExpenditureAccount.setAssignedAmountToETp(_rs.getDouble("assignedamounttoetp"));
                    objExpenditureAccount.setAssignedAmountToCC(_rs.getDouble("assignedamounttocc"));
                    objExpenditureAccount.setAssignedAmountToCCp(_rs.getDouble("assignedamounttoccp"));
                    objExpenditureAccount.setAssignedAmountToETColor(Format.hex2Color(_rs.getString("assignedamounttoetcolor")));
                    objExpenditureAccount.setAssignedAmountToCCColor(Format.hex2Color(_rs.getString("assignedamounttocccolor")));
                    objExpenditureAccount.setValueBlock(_rs.getInt("valueblock"));
                    params = budget.getIdBudget() + "," + _rs.getString("idaccount") + ",'" + _fixed + "'";
                    subTreeRS = LibSQL.exFunction("cashflow.getallBudgetsByExpenditureAccount", params);
                } else {
                    params = costsCentre.getIdCostCentre() + "," + _rs.getString("idaccount");
                    subTreeRS = LibSQL.exFunction("cashflow.getallExpenditureAccountsByCostsCentre", params);
                }
                objExpenditureAccount.setInitialAmount(_rs.getDouble("initialamount"));
                objExpenditureAccount.setInitialAmountP(_rs.getDouble("initialamountp"));
                objExpenditureAccount.setModifiedAmount(_rs.getDouble("modifiedamount"));
            } else {
                objExpenditureAccount.setCode(_rs.getString("code"));
                objExpenditureAccount.setDescription(_rs.getString("description"));
                params = _rs.getString("idaccount");
                subTreeRS = LibSQL.exFunction("accounting.getAllExpenditureAccounts", params);
            }
            objExpenditureAccount.setIdParent(_rs.getInt("idparent"));
            objExpenditureAccount.setIDExpenditureAccount(_rs.getInt("idaccount"));
            objExpenditureAccount.setName(_rs.getString("name"));
            objExpenditureAccount.setInitialAmountColor(Format.hex2Color(_rs.getString("initialamountcolor")));
            this.add(objExpenditureAccount);
            DefaultMutableTreeNode childrenNodes = new DefaultMutableTreeNode(objExpenditureAccount);
            _parentTree.add(childrenNodes);
            createSubTree(childrenNodes, subTreeRS, _fixed);
        }
    }

    public JTree getTree(boolean _fixed) throws Exception {
        DefaultMutableTreeNode tree;
        ResultSet treeRS;
        if (budget != null || costsCentre != null) {
            if (budget != null) {
                tree = new DefaultMutableTreeNode(budget);
                String params = budget.getIdBudget() + ",-1,'" + _fixed + "'";
                treeRS = LibSQL.exFunction("cashflow.getallBudgetsByExpenditureAccount", params);
                getSubTree(tree, treeRS, _fixed, 0);
            } else {
                tree = new DefaultMutableTreeNode(costsCentre);
                String params = costsCentre.getIdCostCentre() + ",0";
                treeRS = LibSQL.exFunction("cashflow.getallExpenditureAccountsByCostsCentre", params);
            }
        } else {
            tree = new DefaultMutableTreeNode("Tipos de Gastos");
            treeRS = LibSQL.exFunction("accounting.getAllExpenditureAccounts", "-1");
            getSubTree(tree, treeRS, _fixed, 0);
        }
        sort();
        return new JTree(tree);
    }

    public void getSubTree(DefaultMutableTreeNode _parentTree, ResultSet _rs, boolean _fixed, int _parent) throws SQLException {
        _rs.beforeFirst();
        while (_rs.next()) {
            if (_rs.getInt("idparent") == _parent) {
                ExpenditureAccount objExpenditureAccount = new ExpenditureAccount();
                if (budget != null || costsCentre != null) {
                    if (budget != null) {
                        objExpenditureAccount.setIdBudgetExpenditureAccount(_rs.getInt("idBudgetExpenditureAccount"));
                        objExpenditureAccount.setIdBudget(_rs.getInt("idBudget"));
                        objExpenditureAccount.setAssignedAmountToET(_rs.getDouble("assignedamounttoet"));
                        objExpenditureAccount.setAssignedAmountToETp(_rs.getDouble("assignedamounttoetp"));
                        objExpenditureAccount.setAssignedAmountToCC(_rs.getDouble("assignedamounttocc"));
                        objExpenditureAccount.setAssignedAmountToCCp(_rs.getDouble("assignedamounttoccp"));
                        objExpenditureAccount.setAssignedAmountToETColor(Format.hex2Color(_rs.getString("assignedamounttoetcolor")));
                        objExpenditureAccount.setAssignedAmountToCCColor(Format.hex2Color(_rs.getString("assignedamounttocccolor")));
                        objExpenditureAccount.setValueBlock(_rs.getInt("valueblock"));
                    } else {
                        Advisor.messageBox("Arbol no disponible", "Aviso");
                    }
                    objExpenditureAccount.setInitialAmount(_rs.getDouble("initialamount"));
                    objExpenditureAccount.setInitialAmountP(_rs.getDouble("initialamountp"));
                    objExpenditureAccount.setModifiedAmount(_rs.getDouble("modifiedamount"));
                } else {
                    objExpenditureAccount.setCode(_rs.getString("code"));
                    objExpenditureAccount.setDescription(_rs.getString("description"));
                }
                objExpenditureAccount.setIdParent(_rs.getInt("idparent"));
                objExpenditureAccount.setIDExpenditureAccount(_rs.getInt("idaccount"));
                objExpenditureAccount.setName(_rs.getString("name"));
                objExpenditureAccount.setInitialAmountColor(Format.hex2Color(_rs.getString("initialamountcolor")));
                this.add(objExpenditureAccount);
                DefaultMutableTreeNode childrenNodes = new DefaultMutableTreeNode(objExpenditureAccount);
                _parentTree.add(childrenNodes);
                int previous = _rs.getRow();
                getSubTree(childrenNodes, _rs, _fixed, _rs.getInt("idaccount"));
                _rs.absolute(previous);
            }
        }
    }
}

package org.adempiere.webui.apps.form;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import org.adempiere.webui.EnvWeb;
import org.adempiere.webui.component.ConfirmPanel;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.editor.WEditor;
import org.adempiere.webui.editor.WSearchEditor;
import org.adempiere.webui.editor.WTableDirEditor;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.window.FDialog;
import org.compiere.common.constants.DisplayTypeConstants;
import org.compiere.framework.Lookup;
import org.compiere.model.MBPartner;
import org.compiere.model.MInvoice;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MPayment;
import org.compiere.model.X_M_Cost;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Trx;
import org.zkoss.zk.au.out.AuEcho;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.DesktopUnavailableException;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkex.zul.Borderlayout;
import org.zkoss.zkex.zul.Center;
import org.zkoss.zkex.zul.South;

/**
 *	Merge Dialog.
 * 	Restriction - fails for Accounting
 *
 *	@author Jorg Janke
 *	@version $Id: VMerge.java,v 1.2 2006/07/30 00:51:28 jjanke Exp $
 */
public class WMerge extends ADForm implements EventListener {

    private int m_totalCount = 0;

    /** Error Log			*/
    private StringBuffer m_errorLog = new StringBuffer();

    private Trx m_trx = null;

    /**	Logger			*/
    private static CLogger log = CLogger.getCLogger(WMerge.class);

    private static String AD_ORG_ID = "AD_Org_ID";

    private static String C_BPARTNER_ID = "C_BPartner_ID";

    private static String AD_USER_ID = "AD_User_ID";

    private static String M_PRODUCT_ID = "M_Product_ID";

    /** Tables to delete (not update) for AD_Org	*/
    private static String[] s_delete_Org = new String[] { "AD_OrgInfo" };

    /** Tables to delete (not update) for AD_User	*/
    private static String[] s_delete_User = new String[] { "AD_User_Roles" };

    /** Tables to delete (not update) for C_BPartner	*/
    private static String[] s_delete_BPartner = new String[] { "C_BP_Employee_Acct", "C_BP_Vendor_Acct", "C_BP_Customer_Acct", "T_Aging" };

    /** Tables to delete (not update) for M_Product		*/
    private static String[] s_delete_Product = new String[] { "M_Product_PO", "M_Replenish", "T_Replenish", "M_ProductPrice", "M_Product_Costing", "M_Cost", "M_Product_Trl", "M_Product_Acct" };

    private String[] m_columnName = null;

    private Label[] m_label = null;

    private WEditor[] m_from = null;

    private WEditor[] m_to = null;

    private String[] m_deleteTables = null;

    private Borderlayout mainLayout = new Borderlayout();

    private Panel CenterPanel = new Panel();

    private Grid centerLayout = GridFactory.newGridLayout();

    private Label mergeFromLabel = new Label();

    private Label mergeToLabel = new Label();

    private ConfirmPanel confirmPanel = new ConfirmPanel(true);

    private String m_msg;

    private boolean m_success;

    /**
	 *	Initialize Panel
	 */
    protected void initForm() {
        log.info("VMerge.init - WinNo=" + m_WindowNo);
        try {
            preInit();
            jbInit();
        } catch (Exception ex) {
            log.log(Level.SEVERE, "", ex);
        }
    }

    /**
	 * 	Pre Init
	 */
    private void preInit() {
        int count = 4;
        m_columnName = new String[count];
        m_label = new Label[count];
        m_from = new WEditor[count];
        m_to = new WEditor[count];
        preInit(0, 2163, DisplayTypeConstants.TableDir, AD_ORG_ID);
        preInit(1, 2762, DisplayTypeConstants.Search, C_BPARTNER_ID);
        preInit(2, 971, DisplayTypeConstants.Search, AD_USER_ID);
        preInit(3, 2221, DisplayTypeConstants.Search, M_PRODUCT_ID);
    }

    /**
	 * 	Pre Init Line
	 *	@param index index
	 *	@param AD_Column_ID id
	 *	@param displayType display type
	 *	@param ColumnName column name
	 */
    private void preInit(int index, int AD_Column_ID, int displayType, String ColumnName) {
        m_columnName[index] = ColumnName;
        String what = Msg.translate(EnvWeb.getCtx(), ColumnName);
        m_label[index] = new Label(what);
        Lookup lookup = MLookupFactory.get(EnvWeb.getCtx(), m_WindowNo, AD_Column_ID, displayType);
        if (displayType == DisplayTypeConstants.Search) {
            m_from[index] = new WSearchEditor(ColumnName, false, false, true, lookup);
            m_to[index] = new WSearchEditor(ColumnName, false, false, true, lookup);
        } else {
            m_from[index] = new WTableDirEditor(ColumnName, false, false, true, lookup);
            m_to[index] = new WTableDirEditor(ColumnName, false, false, true, lookup);
        }
    }

    /**
	 * 	Static init
	 * 	@throws java.lang.Exception
	 */
    void jbInit() throws Exception {
        this.appendChild(mainLayout);
        mainLayout.setHeight("100%");
        mainLayout.setWidth("100%");
        South south = new South();
        mainLayout.appendChild(south);
        south.appendChild(confirmPanel);
        confirmPanel.addActionListener(this);
        Rows rows = centerLayout.newRows();
        CenterPanel.appendChild(centerLayout);
        Center center = new Center();
        mainLayout.appendChild(center);
        center.appendChild(CenterPanel);
        Row row = rows.newRow();
        row.appendChild(new Label());
        row.appendChild(mergeFromLabel);
        row.appendChild(mergeToLabel);
        mergeFromLabel.setText(Msg.getMsg(EnvWeb.getCtx(), "MergeFrom"));
        mergeFromLabel.setStyle("font-weight: bold");
        mergeToLabel.setText(Msg.getMsg(EnvWeb.getCtx(), "MergeTo"));
        mergeToLabel.setStyle("font-weight: bold");
        for (int i = 0; i < m_label.length; i++) {
            row = rows.newRow();
            row.appendChild(m_label[i]);
            row.appendChild(m_from[i].getComponent());
            row.appendChild(m_to[i].getComponent());
        }
    }

    /**
	 * 	Dispose
	 */
    public void dispose() {
        SessionManager.getAppDesktop().closeActiveWindow();
    }

    /**
	 *  Action Listener
	 *  @param e event
	 */
    public void onEvent(Event e) {
        if (e.getTarget().getId().equals(ConfirmPanel.A_CANCEL)) {
            dispose();
            return;
        }
        String columnName = null;
        String from_Info = null;
        String to_Info = null;
        int from_ID = 0;
        int to_ID = 0;
        for (int i = 0; (i < m_columnName.length && from_ID == 0 && to_ID == 0); i++) {
            Object value = m_from[i].getValue();
            if (value != null) {
                if (value instanceof Integer) from_ID = ((Integer) value).intValue(); else continue;
                value = m_to[i].getValue();
                if (value != null && value instanceof Integer) to_ID = ((Integer) value).intValue(); else from_ID = 0;
                if (from_ID != 0) {
                    columnName = m_columnName[i];
                    from_Info = m_from[i].getDisplay();
                    to_Info = m_to[i].getDisplay();
                }
            }
        }
        if (from_ID == 0 || from_ID == to_ID) return;
        m_msg = Msg.getMsg(EnvWeb.getCtx(), "MergeFrom") + " = " + from_Info + "\n" + Msg.getMsg(EnvWeb.getCtx(), "MergeTo") + " = " + to_Info;
        if (!FDialog.ask(m_WindowNo, this, "MergeQuestion", m_msg)) return;
        if (columnName.equals(AD_ORG_ID)) m_deleteTables = s_delete_Org; else if (columnName.equals(AD_USER_ID)) m_deleteTables = s_delete_User; else if (columnName.equals(C_BPARTNER_ID)) m_deleteTables = s_delete_BPartner; else if (columnName.equals(M_PRODUCT_ID)) m_deleteTables = s_delete_Product;
        Clients.showBusy("Processing...", true);
        if (!getDesktop().isServerPushEnabled()) getDesktop().enableServerPush(true);
        MergeRunnable runnable = new MergeRunnable(columnName, from_ID, to_ID, this.getDesktop());
        new Thread(runnable).start();
    }

    class MergeRunnable implements Runnable {

        private int to_ID;

        private int from_ID;

        private String columnName;

        private Desktop desktop;

        private MergeRunnable(String columnName, int from_ID, int to_ID, Desktop desktop) {
            this.columnName = columnName;
            this.from_ID = from_ID;
            this.to_ID = to_ID;
            this.desktop = desktop;
        }

        public void run() {
            try {
                Executions.activate(desktop);
                try {
                    m_success = merge(columnName, from_ID, to_ID);
                    postMerge(columnName, to_ID);
                } finally {
                    Clients.showBusy(null, false);
                    Clients.response(new AuEcho(WMerge.this, "onAfterProcess", null));
                    Executions.deactivate(desktop);
                }
            } catch (DesktopUnavailableException e) {
                log.log(Level.SEVERE, e.getLocalizedMessage(), e);
            } catch (InterruptedException e) {
                log.log(Level.WARNING, e.getLocalizedMessage(), e);
            }
        }
    }

    public void onAfterProcess() {
        if (m_success) {
            FDialog.info(m_WindowNo, this, "MergeSuccess", m_msg + " #" + m_totalCount);
        } else {
            FDialog.error(m_WindowNo, this, "MergeError", m_errorLog.toString());
            return;
        }
        dispose();
    }

    /**
	 * 	Merge.
	 *	@param ColumnName column
	 *	@param from_ID from
	 *	@param to_ID to
	 *	@return true if merged
	 */
    private boolean merge(String ColumnName, int from_ID, int to_ID) {
        String TableName = ColumnName.substring(0, ColumnName.length() - 3);
        log.config(ColumnName + " - From=" + from_ID + ",To=" + to_ID);
        boolean success = true;
        m_totalCount = 0;
        m_errorLog = new StringBuffer();
        String sql = "SELECT t.TableName, c.ColumnName " + "FROM AD_Table t" + " INNER JOIN AD_Column c ON (t.AD_Table_ID=c.AD_Table_ID) " + "WHERE t.IsView='N'" + " AND t.TableName NOT IN ('C_TaxDeclarationAcct')" + " AND (" + "(c.ColumnName=? AND c.IsKey='N')" + " OR " + "c.AD_Reference_Value_ID IN " + "(SELECT rt.AD_Reference_ID FROM AD_Ref_Table rt" + " INNER JOIN AD_Column cc ON (rt.AD_Table_ID=cc.AD_Table_ID AND rt.AD_Key=cc.AD_Column_ID) " + "WHERE cc.IsKey='Y' AND cc.ColumnName=?)" + ") " + "ORDER BY t.LoadSeq DESC";
        PreparedStatement pstmt = null;
        try {
            m_trx = org.compierezk.util.Trx.get("merge");
            pstmt = org.compierezk.util.DB.prepareStatement(sql, m_trx);
            pstmt.setString(1, ColumnName);
            pstmt.setString(2, ColumnName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String tName = rs.getString(1);
                String cName = rs.getString(2);
                if (!TableName.equals(tName)) {
                    int count = mergeTable(tName, cName, from_ID, to_ID);
                    if (count < 0) success = false; else m_totalCount += count;
                }
            }
            rs.close();
            pstmt.close();
            pstmt = null;
            log.config("Success=" + success + " - " + ColumnName + " - From=" + from_ID + ",To=" + to_ID);
            if (success) {
                sql = "DELETE " + TableName + " WHERE " + ColumnName + "=" + from_ID;
                if (org.compierezk.util.DB.executeUpdate(sql, m_trx) < 0) {
                    m_errorLog.append(Env.NL).append("DELETE ").append(TableName).append(" - ");
                    success = false;
                    log.config(m_errorLog.toString());
                    m_trx.rollback();
                    return false;
                }
            }
            if (success) m_trx.commit(); else m_trx.rollback();
            m_trx.close();
        } catch (Exception ex) {
            log.log(Level.SEVERE, ColumnName, ex);
        }
        try {
            if (pstmt != null) pstmt.close();
        } catch (Exception ex) {
        }
        pstmt = null;
        return success;
    }

    /**
	 * 	Merge Table
	 * 	@param TableName table
	 * 	@param ColumnName column
	 * 	@param from_ID from
	 * 	@param to_ID to
	 * 	@return -1 for error or number of changes
	 */
    private int mergeTable(String TableName, String ColumnName, int from_ID, int to_ID) {
        log.fine(TableName + "." + ColumnName + " - From=" + from_ID + ",To=" + to_ID);
        String sql = "UPDATE " + TableName + " SET " + ColumnName + "=" + to_ID + " WHERE " + ColumnName + "=" + from_ID;
        boolean delete = false;
        for (int i = 0; i < m_deleteTables.length; i++) {
            if (m_deleteTables[i].equals(TableName)) {
                delete = true;
                sql = "DELETE " + TableName + " WHERE " + ColumnName + "=" + from_ID;
            }
        }
        if (delete && X_M_Cost.Table_Name.equals(TableName) && M_PRODUCT_ID.equals(ColumnName)) {
            sql += " AND CurrentCostPrice =0" + " AND CurrentQty =0" + " AND CumulatedAmt=0" + " AND CumulatedQty=0";
        }
        int count = org.compierezk.util.DB.executeUpdate(sql, m_trx);
        if (count < 0) {
            count = -1;
            m_errorLog.append(Env.NL).append(delete ? "DELETE " : "UPDATE ").append(TableName).append(" - ").append(" - ").append(sql);
            log.config(m_errorLog.toString());
            m_trx.rollback();
        }
        log.fine(count + (delete ? " -Delete- " : " -Update- ") + TableName);
        return count;
    }

    /**
	 * 	Post Merge
	 *	@param ColumnName column name
	 *	@param to_ID ID
	 */
    private void postMerge(String ColumnName, int to_ID) {
        if (ColumnName.equals(AD_ORG_ID)) {
        } else if (ColumnName.equals(AD_USER_ID)) {
        } else if (ColumnName.equals(C_BPARTNER_ID)) {
            MBPartner bp = new MBPartner(EnvWeb.getCtx(), to_ID, null);
            if (bp.get_ID() != 0) {
                MPayment[] payments = MPayment.getOfBPartner(EnvWeb.getCtx(), bp.getC_BPartner_ID(), null);
                for (int i = 0; i < payments.length; i++) {
                    MPayment payment = payments[i];
                    if (payment.testAllocation()) payment.save();
                }
                MInvoice[] invoices = MInvoice.getOfBPartner(EnvWeb.getCtx(), bp.getC_BPartner_ID(), null);
                for (int i = 0; i < invoices.length; i++) {
                    MInvoice invoice = invoices[i];
                    if (invoice.testAllocation()) invoice.save();
                }
                bp.setTotalOpenBalance();
                bp.setActualLifeTimeValue();
                bp.save();
            }
        } else if (ColumnName.equals(M_PRODUCT_ID)) {
        }
    }
}

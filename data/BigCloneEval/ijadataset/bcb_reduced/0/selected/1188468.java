package org.compiere.model;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;
import org.compiere.process.*;
import org.compiere.util.*;

/**
 * Initial Setup Model
 *
 * @author Jorg Janke
 * @version $Id: MSetup.java,v 1.3 2006/07/30 00:51:02 jjanke Exp $
 * 
 * @author Teo Sarca, SC ARHIPAC SERVICE SRL
 * 			<li>FR [ 1795384 ] Setup: create default accounts records is too rigid
 */
public final class MSetup {

    /**
	 *  Constructor
	 *  @param ctx context
	 *  @param WindowNo window
	 */
    public MSetup(Properties ctx, int WindowNo) {
        m_ctx = new Properties(ctx);
        m_lang = Env.getAD_Language(m_ctx);
        m_WindowNo = WindowNo;
    }

    /**	Logger			*/
    protected CLogger log = CLogger.getCLogger(getClass());

    private Trx m_trx = Trx.get(Trx.createTrxName("Setup"), true);

    private Properties m_ctx;

    private String m_lang;

    private int m_WindowNo;

    private StringBuffer m_info;

    private String m_clientName;

    private String m_stdColumns = "AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy";

    private String m_stdValues;

    private String m_stdValuesOrg;

    private NaturalAccountMap<String, MElementValue> m_nap = null;

    private MClient m_client;

    private MOrg m_org;

    private MAcctSchema m_as;

    private int AD_User_ID;

    private String AD_User_Name;

    private int AD_User_U_ID;

    private String AD_User_U_Name;

    private MCalendar m_calendar;

    private int m_AD_Tree_Account_ID;

    private int C_Cycle_ID;

    private boolean m_hasProject = false;

    private boolean m_hasMCampaign = false;

    private boolean m_hasSRegion = false;

    /**
	 *  Create Client Info.
	 *  - Client, Trees, Org, Role, User, User_Role
	 *  @param clientName client name
	 *  @param orgName org name
	 *  @param userClient user id client
	 *  @param userOrg user id org
	 *  @return true if created
	 */
    public boolean createClient(String clientName, String orgName, String userClient, String userOrg) {
        log.info(clientName);
        m_trx.start();
        m_info = new StringBuffer();
        String name = null;
        String sql = null;
        int no = 0;
        name = clientName;
        if (name == null || name.length() == 0) name = "newClient";
        m_clientName = name;
        m_client = new MClient(m_ctx, 0, true, m_trx.getTrxName());
        m_client.setValue(m_clientName);
        m_client.setName(m_clientName);
        if (!m_client.save()) {
            String err = "Client NOT created";
            log.log(Level.SEVERE, err);
            m_info.append(err);
            m_trx.rollback();
            m_trx.close();
            return false;
        }
        int AD_Client_ID = m_client.getAD_Client_ID();
        Env.setContext(m_ctx, m_WindowNo, "AD_Client_ID", AD_Client_ID);
        Env.setContext(m_ctx, "#AD_Client_ID", AD_Client_ID);
        m_stdValues = String.valueOf(AD_Client_ID) + ",0,'Y',SysDate,0,SysDate,0";
        m_info.append(Msg.translate(m_lang, "AD_Client_ID")).append("=").append(name).append("\n");
        if (!MSequence.checkClientSequences(m_ctx, AD_Client_ID, m_trx.getTrxName())) {
            String err = "Sequences NOT created";
            log.log(Level.SEVERE, err);
            m_info.append(err);
            m_trx.rollback();
            m_trx.close();
            return false;
        }
        if (!m_client.setupClientInfo(m_lang)) {
            String err = "Client Info NOT created";
            log.log(Level.SEVERE, err);
            m_info.append(err);
            m_trx.rollback();
            m_trx.close();
            return false;
        }
        m_AD_Tree_Account_ID = m_client.getSetup_AD_Tree_Account_ID();
        name = orgName;
        if (name == null || name.length() == 0) name = "newOrg";
        m_org = new MOrg(m_client, name);
        if (!m_org.save()) {
            String err = "Organization NOT created";
            log.log(Level.SEVERE, err);
            m_info.append(err);
            m_trx.rollback();
            m_trx.close();
            return false;
        }
        Env.setContext(m_ctx, m_WindowNo, "AD_Org_ID", getAD_Org_ID());
        Env.setContext(m_ctx, "#AD_Org_ID", getAD_Org_ID());
        m_stdValuesOrg = AD_Client_ID + "," + getAD_Org_ID() + ",'Y',SysDate,0,SysDate,0";
        m_info.append(Msg.translate(m_lang, "AD_Org_ID")).append("=").append(name).append("\n");
        name = m_clientName + " Admin";
        MRole admin = new MRole(m_ctx, 0, m_trx.getTrxName());
        admin.setClientOrg(m_client);
        admin.setName(name);
        admin.setUserLevel(MRole.USERLEVEL_ClientPlusOrganization);
        admin.setPreferenceType(MRole.PREFERENCETYPE_Client);
        admin.setIsShowAcct(true);
        if (!admin.save()) {
            String err = "Admin Role A NOT inserted";
            log.log(Level.SEVERE, err);
            m_info.append(err);
            m_trx.rollback();
            m_trx.close();
            return false;
        }
        MRoleOrgAccess adminClientAccess = new MRoleOrgAccess(admin, 0);
        if (!adminClientAccess.save()) log.log(Level.SEVERE, "Admin Role_OrgAccess 0 NOT created");
        MRoleOrgAccess adminOrgAccess = new MRoleOrgAccess(admin, m_org.getAD_Org_ID());
        if (!adminOrgAccess.save()) log.log(Level.SEVERE, "Admin Role_OrgAccess NOT created");
        m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");
        name = m_clientName + " User";
        MRole user = new MRole(m_ctx, 0, m_trx.getTrxName());
        user.setClientOrg(m_client);
        user.setName(name);
        if (!user.save()) {
            String err = "User Role A NOT inserted";
            log.log(Level.SEVERE, err);
            m_info.append(err);
            m_trx.rollback();
            m_trx.close();
            return false;
        }
        MRoleOrgAccess userOrgAccess = new MRoleOrgAccess(user, m_org.getAD_Org_ID());
        if (!userOrgAccess.save()) log.log(Level.SEVERE, "User Role_OrgAccess NOT created");
        m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");
        name = userClient;
        if (name == null || name.length() == 0) name = m_clientName + "Client";
        AD_User_ID = getNextID(AD_Client_ID, "AD_User");
        AD_User_Name = name;
        name = DB.TO_STRING(name);
        sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID," + "Name,Description,Password)" + " VALUES (" + m_stdValues + "," + AD_User_ID + "," + name + "," + name + "," + name + ")";
        no = DB.executeUpdate(sql, m_trx.getTrxName());
        if (no != 1) {
            String err = "Admin User NOT inserted - " + AD_User_Name;
            log.log(Level.SEVERE, err);
            m_info.append(err);
            m_trx.rollback();
            m_trx.close();
            return false;
        }
        m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_Name).append("/").append(AD_User_Name).append("\n");
        name = userOrg;
        if (name == null || name.length() == 0) name = m_clientName + "Org";
        AD_User_U_ID = getNextID(AD_Client_ID, "AD_User");
        AD_User_U_Name = name;
        name = DB.TO_STRING(name);
        sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID," + "Name,Description,Password)" + " VALUES (" + m_stdValues + "," + AD_User_U_ID + "," + name + "," + name + "," + name + ")";
        no = DB.executeUpdate(sql, m_trx.getTrxName());
        if (no != 1) {
            String err = "Org User NOT inserted - " + AD_User_U_Name;
            log.log(Level.SEVERE, err);
            m_info.append(err);
            m_trx.rollback();
            m_trx.close();
            return false;
        }
        m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_U_Name).append("/").append(AD_User_U_Name).append("\n");
        sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)" + " VALUES (" + m_stdValues + "," + AD_User_ID + "," + admin.getAD_Role_ID() + ")";
        no = DB.executeUpdate(sql, m_trx.getTrxName());
        if (no != 1) log.log(Level.SEVERE, "UserRole ClientUser+Admin NOT inserted");
        sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)" + " VALUES (" + m_stdValues + "," + AD_User_ID + "," + user.getAD_Role_ID() + ")";
        no = DB.executeUpdate(sql, m_trx.getTrxName());
        if (no != 1) log.log(Level.SEVERE, "UserRole ClientUser+User NOT inserted");
        sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)" + " VALUES (" + m_stdValues + "," + AD_User_U_ID + "," + user.getAD_Role_ID() + ")";
        no = DB.executeUpdate(sql, m_trx.getTrxName());
        if (no != 1) log.log(Level.SEVERE, "UserRole OrgUser+Org NOT inserted");
        MAcctProcessor ap = new MAcctProcessor(m_client, AD_User_ID);
        ap.save();
        MRequestProcessor rp = new MRequestProcessor(m_client, AD_User_ID);
        rp.save();
        log.info("fini");
        return true;
    }

    /**************************************************************************
	 *  Create Accounting elements.
	 *  - Calendar
	 *  - Account Trees
	 *  - Account Values
	 *  - Accounting Schema
	 *  - Default Accounts
	 *
	 *  @param currency currency
	 *  @param hasProduct has product segment
	 *  @param hasBPartner has bp segment
	 *  @param hasProject has project segment
	 *  @param hasMCampaign has campaign segment
	 *  @param hasSRegion has sales region segment
	 *  @param AccountingFile file name of accounting file
	 *  @return true if created
	 */
    public boolean createAccounting(KeyNamePair currency, boolean hasProduct, boolean hasBPartner, boolean hasProject, boolean hasMCampaign, boolean hasSRegion, File AccountingFile) {
        log.info(m_client.toString());
        m_hasProject = hasProject;
        m_hasMCampaign = hasMCampaign;
        m_hasSRegion = hasSRegion;
        m_info = new StringBuffer();
        String name = null;
        StringBuffer sqlCmd = null;
        int no = 0;
        m_calendar = new MCalendar(m_client);
        if (!m_calendar.save()) {
            String err = "Calendar NOT inserted";
            log.log(Level.SEVERE, err);
            m_info.append(err);
            m_trx.rollback();
            m_trx.close();
            return false;
        }
        m_info.append(Msg.translate(m_lang, "C_Calendar_ID")).append("=").append(m_calendar.getName()).append("\n");
        if (m_calendar.createYear(m_client.getLocale()) == null) log.log(Level.SEVERE, "Year NOT inserted");
        name = m_clientName + " " + Msg.translate(m_lang, "Account_ID");
        MElement element = new MElement(m_client, name, MElement.ELEMENTTYPE_Account, m_AD_Tree_Account_ID);
        if (!element.save()) {
            String err = "Acct Element NOT inserted";
            log.log(Level.SEVERE, err);
            m_info.append(err);
            m_trx.rollback();
            m_trx.close();
            return false;
        }
        int C_Element_ID = element.getC_Element_ID();
        m_info.append(Msg.translate(m_lang, "C_Element_ID")).append("=").append(name).append("\n");
        m_nap = new NaturalAccountMap<String, MElementValue>(m_ctx, m_trx.getTrxName());
        String errMsg = m_nap.parseFile(AccountingFile);
        if (errMsg.length() != 0) {
            log.log(Level.SEVERE, errMsg);
            m_info.append(errMsg);
            m_trx.rollback();
            m_trx.close();
            return false;
        }
        if (m_nap.saveAccounts(getAD_Client_ID(), getAD_Org_ID(), C_Element_ID)) m_info.append(Msg.translate(m_lang, "C_ElementValue_ID")).append(" # ").append(m_nap.size()).append("\n"); else {
            String err = "Acct Element Values NOT inserted";
            log.log(Level.SEVERE, err);
            m_info.append(err);
            m_trx.rollback();
            m_trx.close();
            return false;
        }
        int C_ElementValue_ID = m_nap.getC_ElementValue_ID("DEFAULT_ACCT");
        log.fine("C_ElementValue_ID=" + C_ElementValue_ID);
        m_as = new MAcctSchema(m_client, currency);
        if (!m_as.save()) {
            String err = "AcctSchema NOT inserted";
            log.log(Level.SEVERE, err);
            m_info.append(err);
            m_trx.rollback();
            m_trx.close();
            return false;
        }
        m_info.append(Msg.translate(m_lang, "C_AcctSchema_ID")).append("=").append(m_as.getName()).append("\n");
        String sql2 = null;
        if (Env.isBaseLanguage(m_lang, "AD_Reference")) sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=181"; else sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t " + "WHERE l.AD_Reference_ID=181 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID" + " AND t.AD_Language=" + DB.TO_STRING(m_lang);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            int AD_Client_ID = m_client.getAD_Client_ID();
            stmt = DB.prepareStatement(sql2, m_trx.getTrxName());
            rs = stmt.executeQuery();
            while (rs.next()) {
                String ElementType = rs.getString(1);
                name = rs.getString(2);
                String IsMandatory = null;
                String IsBalanced = "N";
                int SeqNo = 0;
                int C_AcctSchema_Element_ID = 0;
                if (ElementType.equals("OO")) {
                    C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
                    IsMandatory = "Y";
                    IsBalanced = "Y";
                    SeqNo = 10;
                } else if (ElementType.equals("AC")) {
                    C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
                    IsMandatory = "Y";
                    SeqNo = 20;
                } else if (ElementType.equals("PR") && hasProduct) {
                    C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
                    IsMandatory = "N";
                    SeqNo = 30;
                } else if (ElementType.equals("BP") && hasBPartner) {
                    C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
                    IsMandatory = "N";
                    SeqNo = 40;
                } else if (ElementType.equals("PJ") && hasProject) {
                    C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
                    IsMandatory = "N";
                    SeqNo = 50;
                } else if (ElementType.equals("MC") && hasMCampaign) {
                    C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
                    IsMandatory = "N";
                    SeqNo = 60;
                } else if (ElementType.equals("SR") && hasSRegion) {
                    C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
                    IsMandatory = "N";
                    SeqNo = 70;
                }
                if (IsMandatory != null) {
                    sqlCmd = new StringBuffer("INSERT INTO C_AcctSchema_Element(");
                    sqlCmd.append(m_stdColumns).append(",C_AcctSchema_Element_ID,C_AcctSchema_ID,").append("ElementType,Name,SeqNo,IsMandatory,IsBalanced) VALUES (");
                    sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_Element_ID).append(",").append(m_as.getC_AcctSchema_ID()).append(",").append("'").append(ElementType).append("','").append(name).append("',").append(SeqNo).append(",'").append(IsMandatory).append("','").append(IsBalanced).append("')");
                    no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
                    if (no == 1) m_info.append(Msg.translate(m_lang, "C_AcctSchema_Element_ID")).append("=").append(name).append("\n");
                    if (ElementType.equals("OO")) {
                        sqlCmd = new StringBuffer("UPDATE C_AcctSchema_Element SET Org_ID=");
                        sqlCmd.append(getAD_Org_ID()).append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
                        no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
                        if (no != 1) log.log(Level.SEVERE, "Default Org in AcctSchamaElement NOT updated");
                    }
                    if (ElementType.equals("AC")) {
                        sqlCmd = new StringBuffer("UPDATE C_AcctSchema_Element SET C_ElementValue_ID=");
                        sqlCmd.append(C_ElementValue_ID).append(", C_Element_ID=").append(C_Element_ID);
                        sqlCmd.append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
                        no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
                        if (no != 1) log.log(Level.SEVERE, "Default Account in AcctSchamaElement NOT updated");
                    }
                }
            }
        } catch (SQLException e1) {
            log.log(Level.SEVERE, "Elements", e1);
            m_info.append(e1.getMessage());
            m_trx.rollback();
            m_trx.close();
            return false;
        } finally {
            DB.close(rs, stmt);
            rs = null;
            stmt = null;
        }
        try {
            createAccountingRecord(X_C_AcctSchema_GL.Table_Name);
            createAccountingRecord(X_C_AcctSchema_Default.Table_Name);
        } catch (Exception e) {
            String err = e.getLocalizedMessage();
            log.log(Level.SEVERE, err);
            m_info.append(err);
            m_trx.rollback();
            m_trx.close();
            return false;
        }
        createGLCategory("Standard", MGLCategory.CATEGORYTYPE_Manual, true);
        int GL_None = createGLCategory("None", MGLCategory.CATEGORYTYPE_Document, false);
        int GL_GL = createGLCategory("Manual", MGLCategory.CATEGORYTYPE_Manual, false);
        int GL_ARI = createGLCategory("AR Invoice", MGLCategory.CATEGORYTYPE_Document, false);
        int GL_ARR = createGLCategory("AR Receipt", MGLCategory.CATEGORYTYPE_Document, false);
        int GL_MM = createGLCategory("Material Management", MGLCategory.CATEGORYTYPE_Document, false);
        int GL_API = createGLCategory("AP Invoice", MGLCategory.CATEGORYTYPE_Document, false);
        int GL_APP = createGLCategory("AP Payment", MGLCategory.CATEGORYTYPE_Document, false);
        int GL_CASH = createGLCategory("Cash/Payments", MGLCategory.CATEGORYTYPE_Document, false);
        int ii = createDocType("GL Journal", Msg.getElement(m_ctx, "GL_Journal_ID"), MDocType.DOCBASETYPE_GLJournal, null, 0, 0, 1000, GL_GL);
        if (ii == 0) {
            String err = "Document Type not created";
            m_info.append(err);
            m_trx.rollback();
            m_trx.close();
            return false;
        }
        createDocType("GL Journal Batch", Msg.getElement(m_ctx, "GL_JournalBatch_ID"), MDocType.DOCBASETYPE_GLJournal, null, 0, 0, 100, GL_GL);
        int DT_I = createDocType("AR Invoice", Msg.getElement(m_ctx, "C_Invoice_ID", true), MDocType.DOCBASETYPE_ARInvoice, null, 0, 0, 100000, GL_ARI);
        int DT_II = createDocType("AR Invoice Indirect", Msg.getElement(m_ctx, "C_Invoice_ID", true), MDocType.DOCBASETYPE_ARInvoice, null, 0, 0, 150000, GL_ARI);
        int DT_IC = createDocType("AR Credit Memo", Msg.getMsg(m_ctx, "CreditMemo"), MDocType.DOCBASETYPE_ARCreditMemo, null, 0, 0, 170000, GL_ARI);
        createDocType("AP Invoice", Msg.getElement(m_ctx, "C_Invoice_ID", false), MDocType.DOCBASETYPE_APInvoice, null, 0, 0, 0, GL_API);
        int DT_IPC = createDocType("AP CreditMemo", Msg.getMsg(m_ctx, "CreditMemo"), MDocType.DOCBASETYPE_APCreditMemo, null, 0, 0, 0, GL_API);
        createDocType("Match Invoice", Msg.getElement(m_ctx, "M_MatchInv_ID", false), MDocType.DOCBASETYPE_MatchInvoice, null, 0, 0, 390000, GL_API);
        createDocType("AR Receipt", Msg.getElement(m_ctx, "C_Payment_ID", true), MDocType.DOCBASETYPE_ARReceipt, null, 0, 0, 0, GL_ARR);
        createDocType("AP Payment", Msg.getElement(m_ctx, "C_Payment_ID", false), MDocType.DOCBASETYPE_APPayment, null, 0, 0, 0, GL_APP);
        createDocType("Allocation", "Allocation", MDocType.DOCBASETYPE_PaymentAllocation, null, 0, 0, 490000, GL_CASH);
        int DT_S = createDocType("MM Shipment", "Delivery Note", MDocType.DOCBASETYPE_MaterialDelivery, null, 0, 0, 500000, GL_MM);
        int DT_SI = createDocType("MM Shipment Indirect", "Delivery Note", MDocType.DOCBASETYPE_MaterialDelivery, null, 0, 0, 550000, GL_MM);
        int DT_VRM = createDocType("MM Vendor Return", "Vendor Returns", MDocType.DOCBASETYPE_MaterialDelivery, null, 0, 0, 590000, GL_MM);
        createDocType("MM Receipt", "Vendor Delivery", MDocType.DOCBASETYPE_MaterialReceipt, null, 0, 0, 0, GL_MM);
        int DT_RM = createDocType("MM Returns", "Customer Returns", MDocType.DOCBASETYPE_MaterialReceipt, null, 0, 0, 570000, GL_MM);
        createDocType("Purchase Order", Msg.getElement(m_ctx, "C_Order_ID", false), MDocType.DOCBASETYPE_PurchaseOrder, null, 0, 0, 800000, GL_None);
        createDocType("Match PO", Msg.getElement(m_ctx, "M_MatchPO_ID", false), MDocType.DOCBASETYPE_MatchPO, null, 0, 0, 890000, GL_None);
        createDocType("Purchase Requisition", Msg.getElement(m_ctx, "M_Requisition_ID", false), MDocType.DOCBASETYPE_PurchaseRequisition, null, 0, 0, 900000, GL_None);
        createDocType("Vendor Return Material", "Vendor Return Material Authorization", MDocType.DOCBASETYPE_PurchaseOrder, MDocType.DOCSUBTYPESO_ReturnMaterial, DT_VRM, DT_IPC, 990000, GL_MM);
        createDocType("Bank Statement", Msg.getElement(m_ctx, "C_BankStatemet_ID", true), MDocType.DOCBASETYPE_BankStatement, null, 0, 0, 700000, GL_CASH);
        createDocType("Cash Journal", Msg.getElement(m_ctx, "C_Cash_ID", true), MDocType.DOCBASETYPE_CashJournal, null, 0, 0, 750000, GL_CASH);
        createDocType("Material Movement", Msg.getElement(m_ctx, "M_Movement_ID", false), MDocType.DOCBASETYPE_MaterialMovement, null, 0, 0, 610000, GL_MM);
        createDocType("Physical Inventory", Msg.getElement(m_ctx, "M_Inventory_ID", false), MDocType.DOCBASETYPE_MaterialPhysicalInventory, null, 0, 0, 620000, GL_MM);
        createDocType("Material Production", Msg.getElement(m_ctx, "M_Production_ID", false), MDocType.DOCBASETYPE_MaterialProduction, null, 0, 0, 630000, GL_MM);
        createDocType("Project Issue", Msg.getElement(m_ctx, "C_ProjectIssue_ID", false), MDocType.DOCBASETYPE_ProjectIssue, null, 0, 0, 640000, GL_MM);
        createDocType("Binding offer", "Quotation", MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_Quotation, 0, 0, 10000, GL_None);
        createDocType("Non binding offer", "Proposal", MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_Proposal, 0, 0, 20000, GL_None);
        createDocType("Prepay Order", "Prepay Order", MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_PrepayOrder, DT_S, DT_I, 30000, GL_None);
        createDocType("Return Material", "Return Material Authorization", MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_ReturnMaterial, DT_RM, DT_IC, 30000, GL_None);
        createDocType("Standard Order", "Order Confirmation", MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_StandardOrder, DT_S, DT_I, 50000, GL_None);
        createDocType("Credit Order", "Order Confirmation", MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_OnCreditOrder, DT_SI, DT_I, 60000, GL_None);
        createDocType("Warehouse Order", "Order Confirmation", MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_WarehouseOrder, DT_S, DT_I, 70000, GL_None);
        int DT = createDocType("POS Order", "Order Confirmation", MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_POSOrder, DT_SI, DT_II, 80000, GL_None);
        createPreference("C_DocTypeTarget_ID", String.valueOf(DT), 143);
        sqlCmd = new StringBuffer("UPDATE AD_ClientInfo SET ");
        sqlCmd.append("C_AcctSchema1_ID=").append(m_as.getC_AcctSchema_ID()).append(", C_Calendar_ID=").append(m_calendar.getC_Calendar_ID()).append(" WHERE AD_Client_ID=").append(m_client.getAD_Client_ID());
        no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
        if (no != 1) {
            String err = "ClientInfo not updated";
            log.log(Level.SEVERE, err);
            m_info.append(err);
            m_trx.rollback();
            m_trx.close();
            return false;
        }
        DocumentTypeVerify.createDocumentTypes(m_ctx, getAD_Client_ID(), null, m_trx.getTrxName());
        DocumentTypeVerify.createPeriodControls(m_ctx, getAD_Client_ID(), null, m_trx.getTrxName());
        log.info("fini");
        return true;
    }

    private void createAccountingRecord(String tableName) throws Exception {
        MTable table = MTable.get(m_ctx, tableName);
        PO acct = table.getPO(0, m_trx.getTrxName());
        MColumn[] cols = table.getColumns(false);
        for (MColumn c : cols) {
            String columnName = c.getColumnName();
            if (c.isStandardColumn()) {
            } else if (DisplayType.Account == c.getAD_Reference_ID()) {
                acct.set_Value(columnName, getAcct(columnName));
                log.info("Account: " + columnName);
            } else if (DisplayType.YesNo == c.getAD_Reference_ID()) {
                acct.set_Value(columnName, Boolean.TRUE);
                log.info("YesNo: " + c.getColumnName());
            }
        }
        acct.setAD_Client_ID(m_client.getAD_Client_ID());
        acct.set_Value(I_C_AcctSchema.COLUMNNAME_C_AcctSchema_ID, m_as.getC_AcctSchema_ID());
        if (!acct.save()) {
            throw new AdempiereUserError(CLogger.retrieveErrorString(table.getName() + " not created"));
        }
    }

    /**
	 * Get Account ID for key
	 * @param key key
	 * @return C_ValidCombination_ID
	 * @throws AdempiereUserError 
	 */
    private Integer getAcct(String key) throws AdempiereUserError {
        log.fine(key);
        int C_ElementValue_ID = m_nap.getC_ElementValue_ID(key.toUpperCase());
        if (C_ElementValue_ID == 0) {
            throw new AdempiereUserError("Account not defined: " + key);
        }
        MAccount vc = MAccount.getDefault(m_as, true);
        vc.setAD_Org_ID(0);
        vc.setAccount_ID(C_ElementValue_ID);
        if (!vc.save()) {
            throw new AdempiereUserError("Not Saved - Key=" + key + ", C_ElementValue_ID=" + C_ElementValue_ID);
        }
        int C_ValidCombination_ID = vc.getC_ValidCombination_ID();
        if (C_ValidCombination_ID == 0) {
            throw new AdempiereUserError("No account - Key=" + key + ", C_ElementValue_ID=" + C_ElementValue_ID);
        }
        return C_ValidCombination_ID;
    }

    /**
	 *  Create GL Category
	 *  @param Name name
	 *  @param CategoryType category type MGLCategory.CATEGORYTYPE_*
	 *  @param isDefault is default value
	 *  @return GL_Category_ID
	 */
    private int createGLCategory(String Name, String CategoryType, boolean isDefault) {
        MGLCategory cat = new MGLCategory(m_ctx, 0, m_trx.getTrxName());
        cat.setName(Name);
        cat.setCategoryType(CategoryType);
        cat.setIsDefault(isDefault);
        if (!cat.save()) {
            log.log(Level.SEVERE, "GL Category NOT created - " + Name);
            return 0;
        }
        return cat.getGL_Category_ID();
    }

    /**
	 *  Create Document Types with Sequence
	 *  @param Name name
	 *  @param PrintName print name
	 *  @param DocBaseType document base type
	 *  @param DocSubTypeSO sales order sub type
	 *  @param C_DocTypeShipment_ID shipment doc
	 *  @param C_DocTypeInvoice_ID invoice doc
	 *  @param StartNo start doc no
	 *  @param GL_Category_ID gl category
	 *  @return C_DocType_ID doc type or 0 for error
	 */
    private int createDocType(String Name, String PrintName, String DocBaseType, String DocSubTypeSO, int C_DocTypeShipment_ID, int C_DocTypeInvoice_ID, int StartNo, int GL_Category_ID) {
        MSequence sequence = null;
        if (StartNo != 0) {
            sequence = new MSequence(m_ctx, getAD_Client_ID(), Name, StartNo, m_trx.getTrxName());
            if (!sequence.save()) {
                log.log(Level.SEVERE, "Sequence NOT created - " + Name);
                return 0;
            }
        }
        MDocType dt = new MDocType(m_ctx, DocBaseType, Name, m_trx.getTrxName());
        if (PrintName != null && PrintName.length() > 0) dt.setPrintName(PrintName);
        if (DocSubTypeSO != null) dt.setDocSubTypeSO(DocSubTypeSO);
        if (C_DocTypeShipment_ID != 0) dt.setC_DocTypeShipment_ID(C_DocTypeShipment_ID);
        if (C_DocTypeInvoice_ID != 0) dt.setC_DocTypeInvoice_ID(C_DocTypeInvoice_ID);
        if (GL_Category_ID != 0) dt.setGL_Category_ID(GL_Category_ID);
        if (sequence == null) dt.setIsDocNoControlled(false); else {
            dt.setIsDocNoControlled(true);
            dt.setDocNoSequence_ID(sequence.getAD_Sequence_ID());
        }
        dt.setIsSOTrx();
        if (!dt.save()) {
            log.log(Level.SEVERE, "DocType NOT created - " + Name);
            return 0;
        }
        return dt.getC_DocType_ID();
    }

    /**************************************************************************
	 *  Create Default main entities.
	 *  - Dimensions & BPGroup, Prod Category)
	 *  - Location, Locator, Warehouse
	 *  - PriceList
	 *  - Cashbook, PaymentTerm
	 *  @param C_Country_ID country
	 *  @param City city
	 *  @param C_Region_ID region
	 *  @param C_Currency_ID currency
	 *  @return true if created
	 */
    public boolean createEntities(int C_Country_ID, String City, int C_Region_ID, int C_Currency_ID) {
        if (m_as == null) {
            log.severe("No AcctountingSChema");
            m_trx.rollback();
            m_trx.close();
            return false;
        }
        log.info("C_Country_ID=" + C_Country_ID + ", City=" + City + ", C_Region_ID=" + C_Region_ID);
        m_info.append("\n----\n");
        String defaultName = Msg.translate(m_lang, "Standard");
        String defaultEntry = "'" + defaultName + "',";
        StringBuffer sqlCmd = null;
        int no = 0;
        int C_Channel_ID = getNextID(getAD_Client_ID(), "C_Channel");
        sqlCmd = new StringBuffer("INSERT INTO C_Channel ");
        sqlCmd.append("(C_Channel_ID,Name,");
        sqlCmd.append(m_stdColumns).append(") VALUES (");
        sqlCmd.append(C_Channel_ID).append(",").append(defaultEntry);
        sqlCmd.append(m_stdValues).append(")");
        no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
        if (no != 1) log.log(Level.SEVERE, "Channel NOT inserted");
        int C_Campaign_ID = getNextID(getAD_Client_ID(), "C_Campaign");
        sqlCmd = new StringBuffer("INSERT INTO C_Campaign ");
        sqlCmd.append("(C_Campaign_ID,C_Channel_ID,").append(m_stdColumns).append(",");
        sqlCmd.append(" Value,Name,Costs) VALUES (");
        sqlCmd.append(C_Campaign_ID).append(",").append(C_Channel_ID).append(",").append(m_stdValues).append(",");
        sqlCmd.append(defaultEntry).append(defaultEntry).append("0)");
        no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
        if (no == 1) m_info.append(Msg.translate(m_lang, "C_Campaign_ID")).append("=").append(defaultName).append("\n"); else log.log(Level.SEVERE, "Campaign NOT inserted");
        if (m_hasMCampaign) {
            sqlCmd = new StringBuffer("UPDATE C_AcctSchema_Element SET ");
            sqlCmd.append("C_Campaign_ID=").append(C_Campaign_ID);
            sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
            sqlCmd.append(" AND ElementType='MC'");
            no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
            if (no != 1) log.log(Level.SEVERE, "AcctSchema ELement Campaign NOT updated");
        }
        int C_SalesRegion_ID = getNextID(getAD_Client_ID(), "C_SalesRegion");
        sqlCmd = new StringBuffer("INSERT INTO C_SalesRegion ");
        sqlCmd.append("(C_SalesRegion_ID,").append(m_stdColumns).append(",");
        sqlCmd.append(" Value,Name,IsSummary) VALUES (");
        sqlCmd.append(C_SalesRegion_ID).append(",").append(m_stdValues).append(", ");
        sqlCmd.append(defaultEntry).append(defaultEntry).append("'N')");
        no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
        if (no == 1) m_info.append(Msg.translate(m_lang, "C_SalesRegion_ID")).append("=").append(defaultName).append("\n"); else log.log(Level.SEVERE, "SalesRegion NOT inserted");
        if (m_hasSRegion) {
            sqlCmd = new StringBuffer("UPDATE C_AcctSchema_Element SET ");
            sqlCmd.append("C_SalesRegion_ID=").append(C_SalesRegion_ID);
            sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
            sqlCmd.append(" AND ElementType='SR'");
            no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
            if (no != 1) log.log(Level.SEVERE, "AcctSchema ELement SalesRegion NOT updated");
        }
        MBPGroup bpg = new MBPGroup(m_ctx, 0, m_trx.getTrxName());
        bpg.setValue(defaultName);
        bpg.setName(defaultName);
        bpg.setIsDefault(true);
        if (bpg.save()) m_info.append(Msg.translate(m_lang, "C_BP_Group_ID")).append("=").append(defaultName).append("\n"); else log.log(Level.SEVERE, "BP Group NOT inserted");
        MBPartner bp = new MBPartner(m_ctx, 0, m_trx.getTrxName());
        bp.setValue(defaultName);
        bp.setName(defaultName);
        bp.setBPGroup(bpg);
        if (bp.save()) m_info.append(Msg.translate(m_lang, "C_BPartner_ID")).append("=").append(defaultName).append("\n"); else log.log(Level.SEVERE, "BPartner NOT inserted");
        MLocation bpLoc = new MLocation(m_ctx, C_Country_ID, C_Region_ID, City, m_trx.getTrxName());
        bpLoc.save();
        MBPartnerLocation bpl = new MBPartnerLocation(bp);
        bpl.setC_Location_ID(bpLoc.getC_Location_ID());
        if (!bpl.save()) log.log(Level.SEVERE, "BP_Location (Standard) NOT inserted");
        sqlCmd = new StringBuffer("UPDATE C_AcctSchema_Element SET ");
        sqlCmd.append("C_BPartner_ID=").append(bp.getC_BPartner_ID());
        sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
        sqlCmd.append(" AND ElementType='BP'");
        no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
        if (no != 1) log.log(Level.SEVERE, "AcctSchema Element BPartner NOT updated");
        createPreference("C_BPartner_ID", String.valueOf(bp.getC_BPartner_ID()), 143);
        MProductCategory pc = new MProductCategory(m_ctx, 0, m_trx.getTrxName());
        pc.setValue(defaultName);
        pc.setName(defaultName);
        pc.setIsDefault(true);
        if (pc.save()) m_info.append(Msg.translate(m_lang, "M_Product_Category_ID")).append("=").append(defaultName).append("\n"); else log.log(Level.SEVERE, "Product Category NOT inserted");
        int C_UOM_ID = 100;
        int C_TaxCategory_ID = getNextID(getAD_Client_ID(), "C_TaxCategory");
        sqlCmd = new StringBuffer("INSERT INTO C_TaxCategory ");
        sqlCmd.append("(C_TaxCategory_ID,").append(m_stdColumns).append(",");
        sqlCmd.append(" Name,IsDefault) VALUES (");
        sqlCmd.append(C_TaxCategory_ID).append(",").append(m_stdValues).append(", ");
        if (C_Country_ID == 100) sqlCmd.append("'Sales Tax','Y')"); else sqlCmd.append(defaultEntry).append("'Y')");
        no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
        if (no != 1) log.log(Level.SEVERE, "TaxCategory NOT inserted");
        MTax tax = new MTax(m_ctx, "Standard", Env.ZERO, C_TaxCategory_ID, m_trx.getTrxName());
        tax.setIsDefault(true);
        if (tax.save()) m_info.append(Msg.translate(m_lang, "C_Tax_ID")).append("=").append(tax.getName()).append("\n"); else log.log(Level.SEVERE, "Tax NOT inserted");
        MProduct product = new MProduct(m_ctx, 0, m_trx.getTrxName());
        product.setValue(defaultName);
        product.setName(defaultName);
        product.setC_UOM_ID(C_UOM_ID);
        product.setM_Product_Category_ID(pc.getM_Product_Category_ID());
        product.setC_TaxCategory_ID(C_TaxCategory_ID);
        if (product.save()) m_info.append(Msg.translate(m_lang, "M_Product_ID")).append("=").append(defaultName).append("\n"); else log.log(Level.SEVERE, "Product NOT inserted");
        sqlCmd = new StringBuffer("UPDATE C_AcctSchema_Element SET ");
        sqlCmd.append("M_Product_ID=").append(product.getM_Product_ID());
        sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
        sqlCmd.append(" AND ElementType='PR'");
        no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
        if (no != 1) log.log(Level.SEVERE, "AcctSchema Element Product NOT updated");
        MLocation loc = new MLocation(m_ctx, C_Country_ID, C_Region_ID, City, m_trx.getTrxName());
        loc.save();
        sqlCmd = new StringBuffer("UPDATE AD_OrgInfo SET C_Location_ID=");
        sqlCmd.append(loc.getC_Location_ID()).append(" WHERE AD_Org_ID=").append(getAD_Org_ID());
        no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
        if (no != 1) log.log(Level.SEVERE, "Location NOT inserted");
        createPreference("C_Country_ID", String.valueOf(C_Country_ID), 0);
        MWarehouse wh = new MWarehouse(m_ctx, 0, m_trx.getTrxName());
        wh.setValue(defaultName);
        wh.setName(defaultName);
        wh.setC_Location_ID(loc.getC_Location_ID());
        if (!wh.save()) log.log(Level.SEVERE, "Warehouse NOT inserted");
        MLocator locator = new MLocator(wh, defaultName);
        locator.setIsDefault(true);
        if (!locator.save()) log.log(Level.SEVERE, "Locator NOT inserted");
        sqlCmd = new StringBuffer("UPDATE AD_ClientInfo SET ");
        sqlCmd.append("C_BPartnerCashTrx_ID=").append(bp.getC_BPartner_ID());
        sqlCmd.append(",M_ProductFreight_ID=").append(product.getM_Product_ID());
        sqlCmd.append(" WHERE AD_Client_ID=").append(getAD_Client_ID());
        no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
        if (no != 1) {
            String err = "ClientInfo not updated";
            log.log(Level.SEVERE, err);
            m_info.append(err);
            return false;
        }
        MPriceList pl = new MPriceList(m_ctx, 0, m_trx.getTrxName());
        pl.setName(defaultName);
        pl.setC_Currency_ID(C_Currency_ID);
        pl.setIsDefault(true);
        if (!pl.save()) log.log(Level.SEVERE, "PriceList NOT inserted");
        MDiscountSchema ds = new MDiscountSchema(m_ctx, 0, m_trx.getTrxName());
        ds.setName(defaultName);
        ds.setDiscountType(MDiscountSchema.DISCOUNTTYPE_Pricelist);
        if (!ds.save()) log.log(Level.SEVERE, "DiscountSchema NOT inserted");
        MPriceListVersion plv = new MPriceListVersion(pl);
        plv.setName();
        plv.setM_DiscountSchema_ID(ds.getM_DiscountSchema_ID());
        if (!plv.save()) log.log(Level.SEVERE, "PriceList_Version NOT inserted");
        MProductPrice pp = new MProductPrice(plv, product.getM_Product_ID(), Env.ONE, Env.ONE, Env.ONE);
        if (!pp.save()) log.log(Level.SEVERE, "ProductPrice NOT inserted");
        MBPartner bpCU = new MBPartner(m_ctx, 0, m_trx.getTrxName());
        bpCU.setValue(AD_User_U_Name);
        bpCU.setName(AD_User_U_Name);
        bpCU.setBPGroup(bpg);
        bpCU.setIsEmployee(true);
        bpCU.setIsSalesRep(true);
        if (bpCU.save()) m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_U_Name).append("\n"); else log.log(Level.SEVERE, "SalesRep (User) NOT inserted");
        MLocation bpLocCU = new MLocation(m_ctx, C_Country_ID, C_Region_ID, City, m_trx.getTrxName());
        bpLocCU.save();
        MBPartnerLocation bplCU = new MBPartnerLocation(bpCU);
        bplCU.setC_Location_ID(bpLocCU.getC_Location_ID());
        if (!bplCU.save()) log.log(Level.SEVERE, "BP_Location (User) NOT inserted");
        sqlCmd = new StringBuffer("UPDATE AD_User SET C_BPartner_ID=");
        sqlCmd.append(bpCU.getC_BPartner_ID()).append(" WHERE AD_User_ID=").append(AD_User_U_ID);
        no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
        if (no != 1) log.log(Level.SEVERE, "User of SalesRep (User) NOT updated");
        MBPartner bpCA = new MBPartner(m_ctx, 0, m_trx.getTrxName());
        bpCA.setValue(AD_User_Name);
        bpCA.setName(AD_User_Name);
        bpCA.setBPGroup(bpg);
        bpCA.setIsEmployee(true);
        bpCA.setIsSalesRep(true);
        if (bpCA.save()) m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_Name).append("\n"); else log.log(Level.SEVERE, "SalesRep (Admin) NOT inserted");
        MLocation bpLocCA = new MLocation(m_ctx, C_Country_ID, C_Region_ID, City, m_trx.getTrxName());
        bpLocCA.save();
        MBPartnerLocation bplCA = new MBPartnerLocation(bpCA);
        bplCA.setC_Location_ID(bpLocCA.getC_Location_ID());
        if (!bplCA.save()) log.log(Level.SEVERE, "BP_Location (Admin) NOT inserted");
        sqlCmd = new StringBuffer("UPDATE AD_User SET C_BPartner_ID=");
        sqlCmd.append(bpCA.getC_BPartner_ID()).append(" WHERE AD_User_ID=").append(AD_User_ID);
        no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
        if (no != 1) log.log(Level.SEVERE, "User of SalesRep (Admin) NOT updated");
        int C_PaymentTerm_ID = getNextID(getAD_Client_ID(), "C_PaymentTerm");
        sqlCmd = new StringBuffer("INSERT INTO C_PaymentTerm ");
        sqlCmd.append("(C_PaymentTerm_ID,").append(m_stdColumns).append(",");
        sqlCmd.append("Value,Name,NetDays,GraceDays,DiscountDays,Discount,DiscountDays2,Discount2,IsDefault) VALUES (");
        sqlCmd.append(C_PaymentTerm_ID).append(",").append(m_stdValues).append(",");
        sqlCmd.append("'Immediate','Immediate',0,0,0,0,0,0,'Y')");
        no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
        if (no != 1) log.log(Level.SEVERE, "PaymentTerm NOT inserted");
        C_Cycle_ID = getNextID(getAD_Client_ID(), "C_Cycle");
        sqlCmd = new StringBuffer("INSERT INTO C_Cycle ");
        sqlCmd.append("(C_Cycle_ID,").append(m_stdColumns).append(",");
        sqlCmd.append(" Name,C_Currency_ID) VALUES (");
        sqlCmd.append(C_Cycle_ID).append(",").append(m_stdValues).append(", ");
        sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
        no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
        if (no != 1) log.log(Level.SEVERE, "Cycle NOT inserted");
        int C_Project_ID = getNextID(getAD_Client_ID(), "C_Project");
        sqlCmd = new StringBuffer("INSERT INTO C_Project ");
        sqlCmd.append("(C_Project_ID,").append(m_stdColumns).append(",");
        sqlCmd.append(" Value,Name,C_Currency_ID,IsSummary) VALUES (");
        sqlCmd.append(C_Project_ID).append(",").append(m_stdValuesOrg).append(", ");
        sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Currency_ID).append(",'N')");
        no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
        if (no == 1) m_info.append(Msg.translate(m_lang, "C_Project_ID")).append("=").append(defaultName).append("\n"); else log.log(Level.SEVERE, "Project NOT inserted");
        if (m_hasProject) {
            sqlCmd = new StringBuffer("UPDATE C_AcctSchema_Element SET ");
            sqlCmd.append("C_Project_ID=").append(C_Project_ID);
            sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
            sqlCmd.append(" AND ElementType='PJ'");
            no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
            if (no != 1) log.log(Level.SEVERE, "AcctSchema ELement Project NOT updated");
        }
        MCashBook cb = new MCashBook(m_ctx, 0, m_trx.getTrxName());
        cb.setName(defaultName);
        cb.setC_Currency_ID(C_Currency_ID);
        if (cb.save()) m_info.append(Msg.translate(m_lang, "C_CashBook_ID")).append("=").append(defaultName).append("\n"); else log.log(Level.SEVERE, "CashBook NOT inserted");
        boolean success = m_trx.commit();
        m_trx.close();
        log.info("finish");
        return success;
    }

    /**
	 *  Create Preference
	 *  @param Attribute attribute
	 *  @param Value value
	 *  @param AD_Window_ID window
	 */
    private void createPreference(String Attribute, String Value, int AD_Window_ID) {
        int AD_Preference_ID = getNextID(getAD_Client_ID(), "AD_Preference");
        StringBuffer sqlCmd = new StringBuffer("INSERT INTO AD_Preference ");
        sqlCmd.append("(AD_Preference_ID,").append(m_stdColumns).append(",");
        sqlCmd.append("Attribute,Value,AD_Window_ID) VALUES (");
        sqlCmd.append(AD_Preference_ID).append(",").append(m_stdValues).append(",");
        sqlCmd.append("'").append(Attribute).append("','").append(Value).append("',");
        if (AD_Window_ID == 0) sqlCmd.append("NULL)"); else sqlCmd.append(AD_Window_ID).append(")");
        int no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
        if (no != 1) log.log(Level.SEVERE, "Preference NOT inserted - " + Attribute);
    }

    /**************************************************************************
	 * 	Get Next ID
	 * 	@param AD_Client_ID client
	 * 	@param TableName table name
	 * 	@return id
	 */
    private int getNextID(int AD_Client_ID, String TableName) {
        return DB.getNextID(AD_Client_ID, TableName, m_trx.getTrxName());
    }

    /**
	 *  Get Client
	 *  @return AD_Client_ID
	 */
    public int getAD_Client_ID() {
        return m_client.getAD_Client_ID();
    }

    /**
	 * 	Get AD_Org_ID
	 *	@return AD_Org_ID
	 */
    public int getAD_Org_ID() {
        return m_org.getAD_Org_ID();
    }

    /**
	 * 	Get AD_User_ID
	 *	@return AD_User_ID
	 */
    public int getAD_User_ID() {
        return AD_User_ID;
    }

    /**
	 * 	Get Info
	 *	@return Info
	 */
    public String getInfo() {
        return m_info.toString();
    }
}

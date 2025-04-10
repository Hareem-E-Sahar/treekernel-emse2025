public class Test {    public boolean createEntities(int C_Country_ID, String City, int C_Region_ID, int C_Currency_ID) {
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
}
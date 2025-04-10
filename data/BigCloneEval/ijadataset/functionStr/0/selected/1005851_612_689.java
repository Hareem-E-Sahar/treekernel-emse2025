public class Test {    public boolean createAllDocTypes(int GL_None, int GL_GL, int GL_ARI, int GL_ARR, int GL_MM, int GL_API, int GL_APP, int GL_CASH, int GL_M, int GL_SignoPositivo, int GL_SignoNegativo) {
        createDocType("Pedido de Mantenimiento", Msg.getElement(m_ctx, "MPC_Order_ID", false), MDocType.DOCBASETYPE_MaintenanceOrder, null, 0, 0, 910000, GL_M, GL_SignoPositivo, MDocType.DOCTYPE_MaintenanceOrder);
        createDocType("Asunto de Pedido de Manufactura", Msg.getElement(m_ctx, "MPC_Order_ID", false), MDocType.DOCBASETYPE_ManufacturingOrderIssue, null, 0, 0, 920000, GL_M, GL_SignoPositivo, MDocType.DOCTYPE_ManufacturingOrderIssue);
        createDocType("Variacion de Metodo de Pedido de Manufactura", Msg.getElement(m_ctx, "MPC_Order_ID", false), MDocType.DOCBASETYPE_ManufacturingOrderMethodVariation, null, 0, 0, 930000, GL_M, GL_SignoPositivo, MDocType.DOCTYPE_ManufacturingOrderMethodVariation);
        createDocType("Pedido de Manufactura", Msg.getElement(m_ctx, "MPC_Order_ID", false), MDocType.DOCBASETYPE_ManufacturingOrder, null, 0, 0, 940000, GL_M, GL_SignoPositivo, MDocType.DOCTYPE_ManufacturingOrder);
        createDocType("Planificacion de pedido de Manufactura", Msg.getElement(m_ctx, "MPC_Order_ID", false), MDocType.DOCBASETYPE_ManufacturingOrder, null, 0, 0, 950000, GL_M, GL_SignoPositivo, MDocType.DOCTYPE_ManufacturingOrderPlanning);
        createDocType("Albaran de Pedido de Manufactura", Msg.getElement(m_ctx, "MPC_Order_ID", false), MDocType.DOCBASETYPE_ManufacturingOrderReceipt, null, 0, 0, 960000, GL_M, GL_SignoPositivo, MDocType.DOCTYPE_ManufacturingOrderReceipt);
        createDocType("Variacion de uso de Pedido de Manufactura", Msg.getElement(m_ctx, "MPC_Order_ID", false), MDocType.DOCBASETYPE_ManufacturingOrderUseVariation, null, 0, 0, 970000, GL_M, GL_SignoPositivo, MDocType.DOCTYPE_ManufacturingOrderUseVariation);
        createDocType("Variacion de Tasa de Pedido de Manufactura", Msg.getElement(m_ctx, "MPC_Order_ID", false), MDocType.DOCBASETYPE_ManufacturingOrderRateVariation, null, 0, 0, 980000, GL_M, GL_SignoPositivo, MDocType.DOCTYPE_ManufacturingOrderRateVariation);
        createDocType("Planificacion de Aviso de Pedido de Material", Msg.getElement(m_ctx, "M_Requisition_ID", false), MDocType.DOCBASETYPE_PurchaseRequisition, null, 0, 0, 910000, GL_None, GL_SignoPositivo, MDocType.DOCTYPE_PurchaseRequisitionPlanning);
        int ii = createDocType("Diario del Mayor", Msg.getElement(m_ctx, "GL_Journal_ID"), MDocType.DOCBASETYPE_GLJournal, null, 0, 0, 1000, GL_GL, GL_SignoPositivo, MDocType.DOCTYPE_GLJournal);
        if (ii == 0) {
            String err = "Document Type not created";
            m_info.append(err);
            m_trx.rollback();
            m_trx.close();
            return false;
        }
        createDocType("Lote de Asientos", Msg.getElement(m_ctx, "GL_JournalBatch_ID"), MDocType.DOCBASETYPE_GLJournal, null, 0, 0, 100, GL_GL, GL_SignoPositivo, MDocType.DOCTYPE_GLJournalBatch);
        int DT_I = createDocType("Factura de Cliente", Msg.getElement(m_ctx, "C_Invoice_ID", true), MDocType.DOCBASETYPE_ARInvoice, null, 0, 0, 100000, GL_ARI, GL_SignoPositivo, MDocType.DOCTYPE_CustomerInvoice);
        int DT_II = createDocType("Factura de Cliente Indirecta", Msg.getElement(m_ctx, "C_Invoice_ID", true), MDocType.DOCBASETYPE_ARInvoice, null, 0, 0, 150000, GL_ARI, GL_SignoPositivo, MDocType.DOCTYPE_CustomerIndirectInvoice);
        int DT_IC = createDocType("Abono de Cliente", Msg.getMsg(m_ctx, "CreditMemo"), MDocType.DOCBASETYPE_ARCreditMemo, null, 0, 0, 170000, GL_ARI, GL_SignoNegativo, MDocType.DOCTYPE_CustomerCreditMemo);
        createDocType("Factura de Proveedor", Msg.getElement(m_ctx, "C_Invoice_ID", false), MDocType.DOCBASETYPE_APInvoice, null, 0, 0, 0, GL_API, GL_SignoNegativo, MDocType.DOCTYPE_VendorInvoice, true);
        createDocType("Abono de Proveedor", Msg.getMsg(m_ctx, "CreditMemo"), MDocType.DOCBASETYPE_APCreditMemo, null, 0, 0, 0, GL_API, GL_SignoPositivo, MDocType.DOCTYPE_VendorCreditMemo, true);
        createDocType("Corresponder Factura", Msg.getElement(m_ctx, "M_MatchInv_ID", false), MDocType.DOCBASETYPE_MatchInvoice, null, 0, 0, 390000, GL_API, GL_SignoPositivo, MDocType.DOCTYPE_MatchInvoice);
        createDocType("Cobro a Cliente", Msg.getElement(m_ctx, "C_Payment_ID", true), MDocType.DOCBASETYPE_ARReceipt, null, 0, 0, 0, GL_ARR, GL_SignoNegativo, MDocType.DOCTYPE_CustomerReceipt);
        createDocType("Pago a Proveedor", Msg.getElement(m_ctx, "C_Payment_ID", false), MDocType.DOCBASETYPE_APPayment, null, 0, 0, 0, GL_APP, GL_SignoPositivo, MDocType.DOCTYPE_VendorPayment);
        createDocType("Asignacion", "Asignacion", MDocType.DOCBASETYPE_PaymentAllocation, null, 0, 0, 490000, GL_CASH, GL_SignoPositivo, MDocType.DOCTYPE_PaymentAllocation);
        int outTrf_id = createDocType("Transferencia Saliente", Msg.getElement(m_ctx, "C_BankTransfer_ID", false), MDocType.DOCBASETYPE_APPayment, null, 0, 0, 0, GL_APP, GL_SignoPositivo, MDocType.DOCTYPE_OutgoingBankTransfer);
        int inTrf_id = createDocType("Transferencia Entrante", Msg.getElement(m_ctx, "C_BankTransfer_ID", true), MDocType.DOCBASETYPE_ARReceipt, null, 0, 0, 0, GL_ARR, GL_SignoNegativo, MDocType.DOCTYPE_IncomingBankTransfer);
        setClientTransferDocTypes(inTrf_id, outTrf_id);
        createDocType("Comprobante de Retencion (Proveedor)", Msg.getElement(m_ctx, "CreditMemo", false), MDocType.DOCBASETYPE_APCreditMemo, null, 0, 0, 0, GL_API, GL_SignoPositivo, MDocType.DOCTYPE_Retencion_Receipt);
        createDocType("Factura de Retencion (Proveedor)", Msg.getElement(m_ctx, "C_Invoice_ID", false), MDocType.DOCBASETYPE_APInvoice, null, 0, 0, 0, GL_API, GL_SignoNegativo, MDocType.DOCTYPE_Retencion_Invoice);
        createDocType("Comprobante de Retencion (Cliente)", Msg.getElement(m_ctx, "CreditMemo", false), MDocType.DOCBASETYPE_ARCreditMemo, null, 0, 0, 0, GL_ARI, GL_SignoNegativo, MDocType.DOCTYPE_Retencion_ReceiptCustomer);
        createDocType("Factura de Retencion (Cliente)", Msg.getElement(m_ctx, "C_Invoice_ID", false), MDocType.DOCBASETYPE_ARInvoice, null, 0, 0, 0, GL_ARI, GL_SignoPositivo, MDocType.DOCTYPE_Retencion_InvoiceCustomer);
        int DT_S = createDocType("Albaran de Salida", "Albaran de Salida", MDocType.DOCBASETYPE_MaterialDelivery, null, 0, 0, 500000, GL_MM, GL_SignoPositivo, MDocType.DOCTYPE_MaterialDelivery);
        int DT_SI = createDocType("Albaran de Salida Indirecto", "Albaran de Salida", MDocType.DOCBASETYPE_MaterialDelivery, null, 0, 0, 550000, GL_MM, GL_SignoPositivo, MDocType.DOCTYPE_MaterialIndirectDelivery);
        int DT_RM = createDocType("Devolucion de Cliente", "Devolucion de Cliente", MDocType.DOCBASETYPE_MaterialDelivery, null, 0, 0, 570000, GL_MM, GL_SignoPositivo, MDocType.DOCTYPE_CustomerReturn);
        createDocType("Albaran de Entrada", "Albaran de Entrada", MDocType.DOCBASETYPE_MaterialReceipt, null, 0, 0, 0, GL_MM, GL_SignoPositivo, MDocType.DOCTYPE_MaterialReceipt);
        createDocType("Devolucion de Proveedor", "Devolucion de Proveedor", MDocType.DOCBASETYPE_MaterialReceipt, null, 0, 0, 870000, GL_MM, GL_SignoNegativo, MDocType.DOCTYPE_VendorReturn);
        createDocType("Pedido a Proveedor", Msg.getElement(m_ctx, "C_Order_ID", false), MDocType.DOCBASETYPE_PurchaseOrder, null, 0, 0, 800000, GL_None, GL_SignoPositivo, MDocType.DOCTYPE_PurchaseOrder);
        createDocType("Corresponder PP", Msg.getElement(m_ctx, "M_MatchPO_ID", false), MDocType.DOCBASETYPE_MatchPO, null, 0, 0, 890000, GL_None, GL_SignoPositivo, MDocType.DOCTYPE_MatchPO);
        createDocType("Aviso de Pedido de Material", Msg.getElement(m_ctx, "M_Requisition_ID", false), MDocType.DOCBASETYPE_PurchaseRequisition, null, 0, 0, 900000, GL_None, GL_SignoPositivo, MDocType.DOCTYPE_PurchaseRequisition);
        createDocType("Extracto Bancario", Msg.getElement(m_ctx, "C_BankStatemet_ID", true), MDocType.DOCBASETYPE_BankStatement, null, 0, 0, 700000, GL_CASH, GL_SignoPositivo, MDocType.DOCTYPE_BankStatement);
        createDocType("Diario de Caja", Msg.getElement(m_ctx, "C_Cash_ID", true), MDocType.DOCBASETYPE_CashJournal, null, 0, 0, 750000, GL_CASH, GL_SignoPositivo, MDocType.DOCTYPE_CashJournal);
        createDocType("Movimiento de Material", Msg.getElement(m_ctx, "M_Movement_ID", false), MDocType.DOCBASETYPE_MaterialMovement, null, 0, 0, 610000, GL_MM, GL_SignoPositivo, MDocType.DOCTYPE_MaterialMovement);
        createDocType("Inventario Fisico", Msg.getElement(m_ctx, "M_Inventory_ID", false), MDocType.DOCBASETYPE_MaterialPhysicalInventory, null, 0, 0, 620000, GL_MM, GL_SignoPositivo, MDocType.DOCTYPE_MaterialPhysicalInventory);
        createDocType("Produccion", Msg.getElement(m_ctx, "M_Production_ID", false), MDocType.DOCBASETYPE_MaterialProduction, null, 0, 0, 630000, GL_MM, GL_SignoPositivo, MDocType.DOCTYPE_MaterialProduction);
        createDocType("Asunto de Proyecto", Msg.getElement(m_ctx, "C_ProjectIssue_ID", false), MDocType.DOCBASETYPE_ProjectIssue, null, 0, 0, 640000, GL_MM, GL_SignoPositivo, MDocType.DOCTYPE_ProjectIssue);
        createDocType("Ingreso/Egreso Simple", "Ingreso/Egreso Simple", MDocType.DOCBASETYPE_MaterialPhysicalInventory, null, 0, 0, 650000, GL_MM, GL_SignoPositivo, MDocType.DOCTYPE_SimpleMaterialInOut);
        createDocType("Parte de Movimientos", "Parte de Movimientos", MDocType.DOCBASETYPE_MaterialDelivery, null, 0, 0, 700000, GL_MM, GL_SignoPositivo, MDocType.DOCTYPE_ParteDeMovimientos);
        createDocType("Parte de Movimientos Valorizados", "Parte de Movimientos Valorizados", MDocType.DOCBASETYPE_APInvoice, null, 0, 0, 0, GL_API, GL_SignoNegativo, MDocType.DOCTYPE_ParteDeMovimientosValorizados);
        createDocType("Presupuesto en Firme", "Presupuesto en Firme", MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_Quotation, 0, 0, 10000, GL_None, GL_SignoPositivo, MDocType.DOCTYPE_Quotation);
        createDocType("Presupuesto", "Presupuesto", MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_Proposal, 0, 0, 20000, GL_None, GL_SignoPositivo, MDocType.DOCTYPE_Proposal);
        createDocType("Pedido Prepago", "Pedido Prepago", MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_PrepayOrder, DT_S, DT_I, 30000, GL_None, GL_SignoPositivo, MDocType.DOCTYPE_PrepayOrder);
        createDocType("RMA", "RMA", MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_ReturnMaterial, DT_RM, DT_IC, 30000, GL_None, GL_SignoPositivo, MDocType.DOCTYPE_ReturnMaterial);
        createDocType("Pedido", "Pedido", MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_StandardOrder, DT_S, DT_I, 50000, GL_None, GL_SignoPositivo, MDocType.DOCTYPE_StandarOrder);
        createDocType("Pedido a Credito", "Pedido a Credito", MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_OnCreditOrder, DT_SI, DT_I, 60000, GL_None, GL_SignoPositivo, MDocType.DOCTYPE_OnCreditOrder);
        createDocType("Pedido de Almacen", "Pedido de Almacen", MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_WarehouseOrder, DT_S, DT_I, 70000, GL_None, GL_SignoPositivo, MDocType.DOCTYPE_WarehouseOrder);
        int DT = createDocType("Ticket TPV", "Ticket TPV", MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_POSOrder, DT_SI, DT_II, 80000, GL_None, GL_SignoPositivo, MDocType.DOCTYPE_POSOrder);
        createDocType("Boleta de Deposito", "Boleta de Deposito", MDocType.DOCBASETYPE_ARReceipt, null, 0, 0, 0, GL_ARR, GL_SignoNegativo, MDocType.DOCTYPE_DepositReceipt);
        createPreference("C_DocTypeTarget_ID", String.valueOf(DT), 143);
        StringBuffer sqlCmd = new StringBuffer("UPDATE AD_ClientInfo SET ");
        sqlCmd.append("C_AcctSchema1_ID=").append(m_as.getC_AcctSchema_ID()).append(", C_Calendar_ID=").append(m_calendar.getC_Calendar_ID()).append(" WHERE AD_Client_ID=").append(m_client.getAD_Client_ID());
        int no = DB.executeUpdate(sqlCmd.toString(), m_trx.getTrxName());
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
}
public class Test {    private org.omg.CORBA.portable.OutputStream _OB_op_connect_structured_push_supplier(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            org.omg.CosNotifyComm.StructuredPushSupplier _ob_a0 = org.omg.CosNotifyComm.StructuredPushSupplierHelper.read(in);
            connect_structured_push_supplier(_ob_a0);
            out = handler.createReply();
        } catch (org.omg.CosEventChannelAdmin.AlreadyConnected _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosEventChannelAdmin.AlreadyConnectedHelper.write(out, _ob_ex);
        }
        return out;
    }
}
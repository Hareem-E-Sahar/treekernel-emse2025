public class Test {    private org.omg.CORBA.portable.OutputStream _OB_op_connect_push_consumer(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            org.omg.CosEventComm.PushConsumer _ob_a0 = org.omg.CosEventComm.PushConsumerHelper.read(in);
            connect_push_consumer(_ob_a0);
            out = handler.createReply();
        } catch (AlreadyConnected _ob_ex) {
            out = handler.createExceptionReply();
            AlreadyConnectedHelper.write(out, _ob_ex);
        } catch (TypeError _ob_ex) {
            out = handler.createExceptionReply();
            TypeErrorHelper.write(out, _ob_ex);
        }
        return out;
    }
}
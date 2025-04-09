package org.omg.CosEventChannelAdmin;

public abstract class ConsumerAdminPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, ConsumerAdminOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosEventChannelAdmin/ConsumerAdmin:1.0" };

    public ConsumerAdmin _this() {
        return ConsumerAdminHelper.narrow(super._this_object());
    }

    public ConsumerAdmin _this(org.omg.CORBA.ORB orb) {
        return ConsumerAdminHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "obtain_pull_supplier", "obtain_push_supplier" };
        int _ob_left = 0;
        int _ob_right = _ob_names.length;
        int _ob_index = -1;
        while (_ob_left < _ob_right) {
            int _ob_m = (_ob_left + _ob_right) / 2;
            int _ob_res = _ob_names[_ob_m].compareTo(opName);
            if (_ob_res == 0) {
                _ob_index = _ob_m;
                break;
            } else if (_ob_res > 0) _ob_right = _ob_m; else _ob_left = _ob_m + 1;
        }
        switch(_ob_index) {
            case 0:
                return _OB_op_obtain_pull_supplier(in, handler);
            case 1:
                return _OB_op_obtain_push_supplier(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_obtain_pull_supplier(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        ProxyPullSupplier _ob_r = obtain_pull_supplier();
        out = handler.createReply();
        ProxyPullSupplierHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_obtain_push_supplier(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        ProxyPushSupplier _ob_r = obtain_push_supplier();
        out = handler.createReply();
        ProxyPushSupplierHelper.write(out, _ob_r);
        return out;
    }
}

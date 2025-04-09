package org.omg.CosEventComm;

public abstract class PullSupplierPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, PullSupplierOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosEventComm/PullSupplier:1.0" };

    public PullSupplier _this() {
        return PullSupplierHelper.narrow(super._this_object());
    }

    public PullSupplier _this(org.omg.CORBA.ORB orb) {
        return PullSupplierHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "disconnect_pull_supplier", "pull", "try_pull" };
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
                return _OB_op_disconnect_pull_supplier(in, handler);
            case 1:
                return _OB_op_pull(in, handler);
            case 2:
                return _OB_op_try_pull(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_disconnect_pull_supplier(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        disconnect_pull_supplier();
        out = handler.createReply();
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_pull(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            org.omg.CORBA.Any _ob_r = pull();
            out = handler.createReply();
            out.write_any(_ob_r);
        } catch (Disconnected _ob_ex) {
            out = handler.createExceptionReply();
            DisconnectedHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_try_pull(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            org.omg.CORBA.BooleanHolder _ob_ah0 = new org.omg.CORBA.BooleanHolder();
            org.omg.CORBA.Any _ob_r = try_pull(_ob_ah0);
            out = handler.createReply();
            out.write_any(_ob_r);
            out.write_boolean(_ob_ah0.value);
        } catch (Disconnected _ob_ex) {
            out = handler.createExceptionReply();
            DisconnectedHelper.write(out, _ob_ex);
        }
        return out;
    }
}

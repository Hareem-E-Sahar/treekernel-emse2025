package org.omg.CosEventChannelAdmin;

public abstract class EventChannelPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, EventChannelOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosEventChannelAdmin/EventChannel:1.0" };

    public EventChannel _this() {
        return EventChannelHelper.narrow(super._this_object());
    }

    public EventChannel _this(org.omg.CORBA.ORB orb) {
        return EventChannelHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "destroy", "for_consumers", "for_suppliers" };
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
                return _OB_op_destroy(in, handler);
            case 1:
                return _OB_op_for_consumers(in, handler);
            case 2:
                return _OB_op_for_suppliers(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_destroy(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        destroy();
        out = handler.createReply();
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_for_consumers(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        ConsumerAdmin _ob_r = for_consumers();
        out = handler.createReply();
        ConsumerAdminHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_for_suppliers(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        SupplierAdmin _ob_r = for_suppliers();
        out = handler.createReply();
        SupplierAdminHelper.write(out, _ob_r);
        return out;
    }
}

package org.omg.CosPropertyService;

public abstract class PropertiesIteratorPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, PropertiesIteratorOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosPropertyService/PropertiesIterator:1.0" };

    public PropertiesIterator _this() {
        return PropertiesIteratorHelper.narrow(super._this_object());
    }

    public PropertiesIterator _this(org.omg.CORBA.ORB orb) {
        return PropertiesIteratorHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "destroy", "next_n", "next_one", "reset" };
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
                return _OB_op_next_n(in, handler);
            case 2:
                return _OB_op_next_one(in, handler);
            case 3:
                return _OB_op_reset(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_destroy(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        destroy();
        out = handler.createReply();
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_next_n(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int _ob_a0 = in.read_ulong();
        PropertiesHolder _ob_ah1 = new PropertiesHolder();
        boolean _ob_r = next_n(_ob_a0, _ob_ah1);
        out = handler.createReply();
        out.write_boolean(_ob_r);
        PropertiesHelper.write(out, _ob_ah1.value);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_next_one(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        PropertyHolder _ob_ah0 = new PropertyHolder();
        boolean _ob_r = next_one(_ob_ah0);
        out = handler.createReply();
        out.write_boolean(_ob_r);
        PropertyHelper.write(out, _ob_ah0.value);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_reset(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        reset();
        out = handler.createReply();
        return out;
    }
}

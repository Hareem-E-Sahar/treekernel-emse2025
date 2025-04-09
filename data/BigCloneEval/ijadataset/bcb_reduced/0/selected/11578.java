package gov.lanl.adirondemo.Demo;

public abstract class RelayPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, RelayOperations {

    static final String[] _ob_ids_ = { "IDL:adirondemo.lanl.gov/Demo/Relay:1.0" };

    public Relay _this() {
        return RelayHelper.narrow(super._this_object());
    }

    public Relay _this(org.omg.CORBA.ORB orb) {
        return RelayHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "relay_me" };
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
                return _OB_op_relay_me(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_relay_me(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        String _ob_a0 = in.read_string();
        Hello _ob_a1 = HelloHelper.read(in);
        String _ob_r = relay_me(_ob_a0, _ob_a1);
        out = handler.createReply();
        out.write_string(_ob_r);
        return out;
    }
}

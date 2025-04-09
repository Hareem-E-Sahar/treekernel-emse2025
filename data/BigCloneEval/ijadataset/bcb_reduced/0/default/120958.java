public abstract class pcitor48POA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, pcitor48Operations {

    static final String[] _ob_ids_ = { "IDL:pcitor48:1.0" };

    public pcitor48 _this() {
        return pcitor48Helper.narrow(super._this_object());
    }

    public pcitor48 _this(org.omg.CORBA.ORB orb) {
        return pcitor48Helper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "Read", "SetLed" };
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
                return _OB_op_Read(in, handler);
            case 1:
                return _OB_op_SetLed(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_Read(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        short _ob_a0 = in.read_short();
        boolean _ob_r = Read(_ob_a0);
        out = handler.createReply();
        out.write_boolean(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_SetLed(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        short _ob_a0 = in.read_short();
        SetLed(_ob_a0);
        out = handler.createReply();
        return out;
    }
}

package gov.lanl.adirondemo.Demo;

public abstract class AccessDecisionPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, AccessDecisionOperations {

    static final String[] _ob_ids_ = { "IDL:adirondemo.lanl.gov/Demo/AccessDecision:1.0" };

    public AccessDecision _this() {
        return AccessDecisionHelper.narrow(super._this_object());
    }

    public AccessDecision _this(org.omg.CORBA.ORB orb) {
        return AccessDecisionHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "access_allowed" };
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
                return _OB_op_access_allowed(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_access_allowed(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        com.adiron.SecurityLevel3.Principal _ob_a0 = com.adiron.SecurityLevel3.PrincipalHelper.read(in);
        boolean _ob_r = access_allowed(_ob_a0);
        out = handler.createReply();
        out.write_boolean(_ob_r);
        return out;
    }
}

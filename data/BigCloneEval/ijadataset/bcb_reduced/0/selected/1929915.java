package gov.lanl.Authenticate;

public abstract class AuthenticatorPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, AuthenticatorOperations {

    static final String[] _ob_ids_ = { "IDL:lanl.gov/Authenticate/Authenticator:1.0" };

    public Authenticator _this() {
        return AuthenticatorHelper.narrow(super._this_object());
    }

    public Authenticator _this(org.omg.CORBA.ORB orb) {
        return AuthenticatorHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "areUsersOk", "isUserOk", "isUserOkFromCredentials", "logoffUser", "logoffUserFromCredentials" };
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
                return _OB_op_areUsersOk(in, handler);
            case 1:
                return _OB_op_isUserOk(in, handler);
            case 2:
                return _OB_op_isUserOkFromCredentials(in, handler);
            case 3:
                return _OB_op_logoffUser(in, handler);
            case 4:
                return _OB_op_logoffUserFromCredentials(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_areUsersOk(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.GSSUP.InitialContextToken[] _ob_a0 = TokenSeqHelper.read(in);
        boolean[] _ob_r = areUsersOk(_ob_a0);
        out = handler.createReply();
        BooleanSeqHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_isUserOk(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.GSSUP.InitialContextToken _ob_a0 = org.omg.GSSUP.InitialContextTokenHelper.read(in);
        boolean _ob_r = isUserOk(_ob_a0);
        out = handler.createReply();
        out.write_boolean(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_isUserOkFromCredentials(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        boolean _ob_r = isUserOkFromCredentials();
        out = handler.createReply();
        out.write_boolean(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_logoffUser(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.GSSUP.InitialContextToken _ob_a0 = org.omg.GSSUP.InitialContextTokenHelper.read(in);
        logoffUser(_ob_a0);
        out = handler.createReply();
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_logoffUserFromCredentials(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        logoffUserFromCredentials();
        out = handler.createReply();
        return out;
    }
}

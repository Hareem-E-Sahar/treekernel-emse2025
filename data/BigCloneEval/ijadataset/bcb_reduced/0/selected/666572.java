package org.omg.CosTime;

public abstract class TimeServicePOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, TimeServiceOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosTime/TimeService:1.0" };

    public TimeService _this() {
        return TimeServiceHelper.narrow(super._this_object());
    }

    public TimeService _this(org.omg.CORBA.ORB orb) {
        return TimeServiceHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "new_interval", "new_universal_time", "secure_universal_time", "universal_time", "uto_from_utc" };
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
                return _OB_op_new_interval(in, handler);
            case 1:
                return _OB_op_new_universal_time(in, handler);
            case 2:
                return _OB_op_secure_universal_time(in, handler);
            case 3:
                return _OB_op_universal_time(in, handler);
            case 4:
                return _OB_op_uto_from_utc(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_new_interval(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.TimeBase.ulonglong _ob_a0 = org.omg.TimeBase.TimeTHelper.read(in);
        org.omg.TimeBase.ulonglong _ob_a1 = org.omg.TimeBase.TimeTHelper.read(in);
        TIO _ob_r = new_interval(_ob_a0, _ob_a1);
        out = handler.createReply();
        TIOHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_new_universal_time(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.TimeBase.ulonglong _ob_a0 = org.omg.TimeBase.TimeTHelper.read(in);
        org.omg.TimeBase.ulonglong _ob_a1 = org.omg.TimeBase.InaccuracyTHelper.read(in);
        short _ob_a2 = org.omg.TimeBase.TdfTHelper.read(in);
        UTO _ob_r = new_universal_time(_ob_a0, _ob_a1, _ob_a2);
        out = handler.createReply();
        UTOHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_secure_universal_time(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            UTO _ob_r = secure_universal_time();
            out = handler.createReply();
            UTOHelper.write(out, _ob_r);
        } catch (TimeUnavailable _ob_ex) {
            out = handler.createExceptionReply();
            TimeUnavailableHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_universal_time(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            UTO _ob_r = universal_time();
            out = handler.createReply();
            UTOHelper.write(out, _ob_r);
        } catch (TimeUnavailable _ob_ex) {
            out = handler.createExceptionReply();
            TimeUnavailableHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_uto_from_utc(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.TimeBase.UtcT _ob_a0 = org.omg.TimeBase.UtcTHelper.read(in);
        UTO _ob_r = uto_from_utc(_ob_a0);
        out = handler.createReply();
        UTOHelper.write(out, _ob_r);
        return out;
    }
}

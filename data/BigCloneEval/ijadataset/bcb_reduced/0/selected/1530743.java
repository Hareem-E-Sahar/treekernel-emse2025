package org.omg.CosTimerEvent;

public abstract class TimerEventHandlerPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, TimerEventHandlerOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosTimerEvent/TimerEventHandler:1.0" };

    public TimerEventHandler _this() {
        return TimerEventHandlerHelper.narrow(super._this_object());
    }

    public TimerEventHandler _this(org.omg.CORBA.ORB orb) {
        return TimerEventHandlerHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "SetTimer", "_get_status", "cancel_timer", "set_data", "time_set" };
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
                return _OB_op_SetTimer(in, handler);
            case 1:
                return _OB_att_get_status(in, handler);
            case 2:
                return _OB_op_cancel_timer(in, handler);
            case 3:
                return _OB_op_set_data(in, handler);
            case 4:
                return _OB_op_time_set(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_SetTimer(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        TimeType _ob_a0 = TimeTypeHelper.read(in);
        org.omg.CosTime.UTO _ob_a1 = org.omg.CosTime.UTOHelper.read(in);
        SetTimer(_ob_a0, _ob_a1);
        out = handler.createReply();
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_status(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        EventStatus _ob_r = status();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        EventStatusHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_cancel_timer(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        boolean _ob_r = cancel_timer();
        out = handler.createReply();
        out.write_boolean(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_data(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.CORBA.Any _ob_a0 = in.read_any();
        set_data(_ob_a0);
        out = handler.createReply();
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_time_set(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.CosTime.UTOHolder _ob_ah0 = new org.omg.CosTime.UTOHolder();
        boolean _ob_r = time_set(_ob_ah0);
        out = handler.createReply();
        out.write_boolean(_ob_r);
        org.omg.CosTime.UTOHelper.write(out, _ob_ah0.value);
        return out;
    }
}

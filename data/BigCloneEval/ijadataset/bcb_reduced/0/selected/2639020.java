package org.omg.CosTimerEvent;

public abstract class TimerEventServicePOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, TimerEventServiceOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosTimerEvent/TimerEventService:1.0" };

    public TimerEventService _this() {
        return TimerEventServiceHelper.narrow(super._this_object());
    }

    public TimerEventService _this(org.omg.CORBA.ORB orb) {
        return TimerEventServiceHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "event_time", "register", "unregister" };
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
                return _OB_op_event_time(in, handler);
            case 1:
                return _OB_op_register(in, handler);
            case 2:
                return _OB_op_unregister(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_event_time(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        TimerEventT _ob_a0 = TimerEventTHelper.read(in);
        org.omg.CosTime.UTO _ob_r = event_time(_ob_a0);
        out = handler.createReply();
        org.omg.CosTime.UTOHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_register(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.CosEventComm.PushConsumer _ob_a0 = org.omg.CosEventComm.PushConsumerHelper.read(in);
        org.omg.CORBA.Any _ob_a1 = in.read_any();
        TimerEventHandler _ob_r = register(_ob_a0, _ob_a1);
        out = handler.createReply();
        TimerEventHandlerHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_unregister(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        TimerEventHandler _ob_a0 = TimerEventHandlerHelper.read(in);
        unregister(_ob_a0);
        out = handler.createReply();
        return out;
    }
}

package org.omg.CosTime;

public abstract class TIOPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, TIOOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosTime/TIO:1.0" };

    public TIO _this() {
        return TIOHelper.narrow(super._this_object());
    }

    public TIO _this(org.omg.CORBA.ORB orb) {
        return TIOHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "_get_time_interval", "overlaps", "spans", "time" };
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
                return _OB_att_get_time_interval(in, handler);
            case 1:
                return _OB_op_overlaps(in, handler);
            case 2:
                return _OB_op_spans(in, handler);
            case 3:
                return _OB_op_time(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_time_interval(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.TimeBase.IntervalT _ob_r = time_interval();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        org.omg.TimeBase.IntervalTHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_overlaps(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        TIO _ob_a0 = TIOHelper.read(in);
        TIOHolder _ob_ah1 = new TIOHolder();
        boolean _ob_r = overlaps(_ob_a0, _ob_ah1);
        out = handler.createReply();
        out.write_boolean(_ob_r);
        TIOHelper.write(out, _ob_ah1.value);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_spans(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        UTO _ob_a0 = UTOHelper.read(in);
        TIOHolder _ob_ah1 = new TIOHolder();
        boolean _ob_r = spans(_ob_a0, _ob_ah1);
        out = handler.createReply();
        out.write_boolean(_ob_r);
        TIOHelper.write(out, _ob_ah1.value);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_time(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        UTO _ob_r = time();
        out = handler.createReply();
        UTOHelper.write(out, _ob_r);
        return out;
    }
}

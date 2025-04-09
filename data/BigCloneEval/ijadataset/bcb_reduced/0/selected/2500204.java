package org.omg.CosTradingDynamic;

public abstract class DynamicPropEvalPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, DynamicPropEvalOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosTradingDynamic/DynamicPropEval:1.0" };

    public DynamicPropEval _this() {
        return DynamicPropEvalHelper.narrow(super._this_object());
    }

    public DynamicPropEval _this(org.omg.CORBA.ORB orb) {
        return DynamicPropEvalHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "evalDP" };
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
                return _OB_op_evalDP(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_evalDP(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = org.omg.CosTrading.PropertyNameHelper.read(in);
            org.omg.CORBA.TypeCode _ob_a1 = in.read_TypeCode();
            org.omg.CORBA.Any _ob_a2 = in.read_any();
            org.omg.CORBA.Any _ob_r = evalDP(_ob_a0, _ob_a1, _ob_a2);
            out = handler.createReply();
            out.write_any(_ob_r);
        } catch (DPEvalFailure _ob_ex) {
            out = handler.createExceptionReply();
            DPEvalFailureHelper.write(out, _ob_ex);
        }
        return out;
    }
}

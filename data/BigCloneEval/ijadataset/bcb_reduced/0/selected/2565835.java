package org.omg.CosTrading;

public abstract class OfferIdIteratorPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, OfferIdIteratorOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosTrading/OfferIdIterator:1.0" };

    public OfferIdIterator _this() {
        return OfferIdIteratorHelper.narrow(super._this_object());
    }

    public OfferIdIterator _this(org.omg.CORBA.ORB orb) {
        return OfferIdIteratorHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "destroy", "max_left", "next_n" };
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
                return _OB_op_max_left(in, handler);
            case 2:
                return _OB_op_next_n(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_destroy(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        destroy();
        out = handler.createReply();
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_max_left(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            int _ob_r = max_left();
            out = handler.createReply();
            out.write_ulong(_ob_r);
        } catch (UnknownMaxLeft _ob_ex) {
            out = handler.createExceptionReply();
            UnknownMaxLeftHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_next_n(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int _ob_a0 = in.read_ulong();
        OfferIdSeqHolder _ob_ah1 = new OfferIdSeqHolder();
        boolean _ob_r = next_n(_ob_a0, _ob_ah1);
        out = handler.createReply();
        out.write_boolean(_ob_r);
        OfferIdSeqHelper.write(out, _ob_ah1.value);
        return out;
    }
}

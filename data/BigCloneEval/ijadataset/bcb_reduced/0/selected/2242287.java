package org.omg.CosPropertyService;

public abstract class PropertySetFactoryPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, PropertySetFactoryOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosPropertyService/PropertySetFactory:1.0" };

    public PropertySetFactory _this() {
        return PropertySetFactoryHelper.narrow(super._this_object());
    }

    public PropertySetFactory _this(org.omg.CORBA.ORB orb) {
        return PropertySetFactoryHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "create_constrained_propertyset", "create_initial_propertyset", "create_propertyset" };
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
                return _OB_op_create_constrained_propertyset(in, handler);
            case 1:
                return _OB_op_create_initial_propertyset(in, handler);
            case 2:
                return _OB_op_create_propertyset(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_create_constrained_propertyset(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            org.omg.CORBA.TypeCode[] _ob_a0 = PropertyTypesHelper.read(in);
            Property[] _ob_a1 = PropertiesHelper.read(in);
            PropertySet _ob_r = create_constrained_propertyset(_ob_a0, _ob_a1);
            out = handler.createReply();
            PropertySetHelper.write(out, _ob_r);
        } catch (ConstraintNotSupported _ob_ex) {
            out = handler.createExceptionReply();
            ConstraintNotSupportedHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_create_initial_propertyset(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            Property[] _ob_a0 = PropertiesHelper.read(in);
            PropertySet _ob_r = create_initial_propertyset(_ob_a0);
            out = handler.createReply();
            PropertySetHelper.write(out, _ob_r);
        } catch (MultipleExceptions _ob_ex) {
            out = handler.createExceptionReply();
            MultipleExceptionsHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_create_propertyset(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        PropertySet _ob_r = create_propertyset();
        out = handler.createReply();
        PropertySetHelper.write(out, _ob_r);
        return out;
    }
}

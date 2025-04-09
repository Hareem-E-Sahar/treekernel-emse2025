package org.omg.CosPropertyService;

public abstract class PropertySetDefFactoryPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, PropertySetDefFactoryOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosPropertyService/PropertySetDefFactory:1.0" };

    public PropertySetDefFactory _this() {
        return PropertySetDefFactoryHelper.narrow(super._this_object());
    }

    public PropertySetDefFactory _this(org.omg.CORBA.ORB orb) {
        return PropertySetDefFactoryHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "create_constrained_propertysetdef", "create_initial_propertysetdef", "create_propertysetdef" };
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
                return _OB_op_create_constrained_propertysetdef(in, handler);
            case 1:
                return _OB_op_create_initial_propertysetdef(in, handler);
            case 2:
                return _OB_op_create_propertysetdef(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_create_constrained_propertysetdef(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            org.omg.CORBA.TypeCode[] _ob_a0 = PropertyTypesHelper.read(in);
            PropertyDef[] _ob_a1 = PropertyDefsHelper.read(in);
            PropertySetDef _ob_r = create_constrained_propertysetdef(_ob_a0, _ob_a1);
            out = handler.createReply();
            PropertySetDefHelper.write(out, _ob_r);
        } catch (ConstraintNotSupported _ob_ex) {
            out = handler.createExceptionReply();
            ConstraintNotSupportedHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_create_initial_propertysetdef(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            PropertyDef[] _ob_a0 = PropertyDefsHelper.read(in);
            PropertySetDef _ob_r = create_initial_propertysetdef(_ob_a0);
            out = handler.createReply();
            PropertySetDefHelper.write(out, _ob_r);
        } catch (MultipleExceptions _ob_ex) {
            out = handler.createExceptionReply();
            MultipleExceptionsHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_create_propertysetdef(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        PropertySetDef _ob_r = create_propertysetdef();
        out = handler.createReply();
        PropertySetDefHelper.write(out, _ob_r);
        return out;
    }
}

package org.omg.CosPropertyService;

public abstract class PropertySetPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, PropertySetOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosPropertyService/PropertySet:1.0" };

    public PropertySet _this() {
        return PropertySetHelper.narrow(super._this_object());
    }

    public PropertySet _this(org.omg.CORBA.ORB orb) {
        return PropertySetHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "define_properties", "define_property", "delete_all_properties", "delete_properties", "delete_property", "get_all_properties", "get_all_property_names", "get_number_of_properties", "get_properties", "get_property_value", "is_property_defined" };
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
                return _OB_op_define_properties(in, handler);
            case 1:
                return _OB_op_define_property(in, handler);
            case 2:
                return _OB_op_delete_all_properties(in, handler);
            case 3:
                return _OB_op_delete_properties(in, handler);
            case 4:
                return _OB_op_delete_property(in, handler);
            case 5:
                return _OB_op_get_all_properties(in, handler);
            case 6:
                return _OB_op_get_all_property_names(in, handler);
            case 7:
                return _OB_op_get_number_of_properties(in, handler);
            case 8:
                return _OB_op_get_properties(in, handler);
            case 9:
                return _OB_op_get_property_value(in, handler);
            case 10:
                return _OB_op_is_property_defined(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_define_properties(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            Property[] _ob_a0 = PropertiesHelper.read(in);
            define_properties(_ob_a0);
            out = handler.createReply();
        } catch (MultipleExceptions _ob_ex) {
            out = handler.createExceptionReply();
            MultipleExceptionsHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_define_property(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = PropertyNameHelper.read(in);
            org.omg.CORBA.Any _ob_a1 = in.read_any();
            define_property(_ob_a0, _ob_a1);
            out = handler.createReply();
        } catch (InvalidPropertyName _ob_ex) {
            out = handler.createExceptionReply();
            InvalidPropertyNameHelper.write(out, _ob_ex);
        } catch (ConflictingProperty _ob_ex) {
            out = handler.createExceptionReply();
            ConflictingPropertyHelper.write(out, _ob_ex);
        } catch (UnsupportedTypeCode _ob_ex) {
            out = handler.createExceptionReply();
            UnsupportedTypeCodeHelper.write(out, _ob_ex);
        } catch (UnsupportedProperty _ob_ex) {
            out = handler.createExceptionReply();
            UnsupportedPropertyHelper.write(out, _ob_ex);
        } catch (ReadOnlyProperty _ob_ex) {
            out = handler.createExceptionReply();
            ReadOnlyPropertyHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_delete_all_properties(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        boolean _ob_r = delete_all_properties();
        out = handler.createReply();
        out.write_boolean(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_delete_properties(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String[] _ob_a0 = PropertyNamesHelper.read(in);
            delete_properties(_ob_a0);
            out = handler.createReply();
        } catch (MultipleExceptions _ob_ex) {
            out = handler.createExceptionReply();
            MultipleExceptionsHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_delete_property(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = PropertyNameHelper.read(in);
            delete_property(_ob_a0);
            out = handler.createReply();
        } catch (PropertyNotFound _ob_ex) {
            out = handler.createExceptionReply();
            PropertyNotFoundHelper.write(out, _ob_ex);
        } catch (InvalidPropertyName _ob_ex) {
            out = handler.createExceptionReply();
            InvalidPropertyNameHelper.write(out, _ob_ex);
        } catch (FixedProperty _ob_ex) {
            out = handler.createExceptionReply();
            FixedPropertyHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_all_properties(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int _ob_a0 = in.read_ulong();
        PropertiesHolder _ob_ah1 = new PropertiesHolder();
        PropertiesIteratorHolder _ob_ah2 = new PropertiesIteratorHolder();
        get_all_properties(_ob_a0, _ob_ah1, _ob_ah2);
        out = handler.createReply();
        PropertiesHelper.write(out, _ob_ah1.value);
        PropertiesIteratorHelper.write(out, _ob_ah2.value);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_all_property_names(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int _ob_a0 = in.read_ulong();
        PropertyNamesHolder _ob_ah1 = new PropertyNamesHolder();
        PropertyNamesIteratorHolder _ob_ah2 = new PropertyNamesIteratorHolder();
        get_all_property_names(_ob_a0, _ob_ah1, _ob_ah2);
        out = handler.createReply();
        PropertyNamesHelper.write(out, _ob_ah1.value);
        PropertyNamesIteratorHelper.write(out, _ob_ah2.value);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_number_of_properties(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int _ob_r = get_number_of_properties();
        out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_properties(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        String[] _ob_a0 = PropertyNamesHelper.read(in);
        PropertiesHolder _ob_ah1 = new PropertiesHolder();
        boolean _ob_r = get_properties(_ob_a0, _ob_ah1);
        out = handler.createReply();
        out.write_boolean(_ob_r);
        PropertiesHelper.write(out, _ob_ah1.value);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_property_value(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = PropertyNameHelper.read(in);
            org.omg.CORBA.Any _ob_r = get_property_value(_ob_a0);
            out = handler.createReply();
            out.write_any(_ob_r);
        } catch (PropertyNotFound _ob_ex) {
            out = handler.createExceptionReply();
            PropertyNotFoundHelper.write(out, _ob_ex);
        } catch (InvalidPropertyName _ob_ex) {
            out = handler.createExceptionReply();
            InvalidPropertyNameHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_is_property_defined(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = PropertyNameHelper.read(in);
            boolean _ob_r = is_property_defined(_ob_a0);
            out = handler.createReply();
            out.write_boolean(_ob_r);
        } catch (InvalidPropertyName _ob_ex) {
            out = handler.createExceptionReply();
            InvalidPropertyNameHelper.write(out, _ob_ex);
        }
        return out;
    }
}

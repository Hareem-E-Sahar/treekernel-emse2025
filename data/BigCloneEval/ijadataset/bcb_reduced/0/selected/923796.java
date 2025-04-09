package org.omg.CosNotifyChannelAdmin;

public abstract class EventChannelPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, EventChannelOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosNotifyChannelAdmin/EventChannel:1.0", "IDL:omg.org/CosNotification/QoSAdmin:1.0", "IDL:omg.org/CosNotification/AdminPropertiesAdmin:1.0", "IDL:omg.org/CosEventChannelAdmin/EventChannel:1.0" };

    public EventChannel _this() {
        return EventChannelHelper.narrow(super._this_object());
    }

    public EventChannel _this(org.omg.CORBA.ORB orb) {
        return EventChannelHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "_get_MyFactory", "_get_default_consumer_admin", "_get_default_filter_factory", "_get_default_supplier_admin", "destroy", "for_consumers", "for_suppliers", "get_admin", "get_all_consumeradmins", "get_all_supplieradmins", "get_consumeradmin", "get_qos", "get_supplieradmin", "new_for_consumers", "new_for_suppliers", "set_admin", "set_qos", "validate_qos" };
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
                return _OB_att_get_MyFactory(in, handler);
            case 1:
                return _OB_att_get_default_consumer_admin(in, handler);
            case 2:
                return _OB_att_get_default_filter_factory(in, handler);
            case 3:
                return _OB_att_get_default_supplier_admin(in, handler);
            case 4:
                return _OB_op_destroy(in, handler);
            case 5:
                return _OB_op_for_consumers(in, handler);
            case 6:
                return _OB_op_for_suppliers(in, handler);
            case 7:
                return _OB_op_get_admin(in, handler);
            case 8:
                return _OB_op_get_all_consumeradmins(in, handler);
            case 9:
                return _OB_op_get_all_supplieradmins(in, handler);
            case 10:
                return _OB_op_get_consumeradmin(in, handler);
            case 11:
                return _OB_op_get_qos(in, handler);
            case 12:
                return _OB_op_get_supplieradmin(in, handler);
            case 13:
                return _OB_op_new_for_consumers(in, handler);
            case 14:
                return _OB_op_new_for_suppliers(in, handler);
            case 15:
                return _OB_op_set_admin(in, handler);
            case 16:
                return _OB_op_set_qos(in, handler);
            case 17:
                return _OB_op_validate_qos(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_MyFactory(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        EventChannelFactory _ob_r = MyFactory();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        EventChannelFactoryHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_default_consumer_admin(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        ConsumerAdmin _ob_r = default_consumer_admin();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        ConsumerAdminHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_default_filter_factory(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CosNotifyFilter.FilterFactory _ob_r = default_filter_factory();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        org.omg.CosNotifyFilter.FilterFactoryHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_default_supplier_admin(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        SupplierAdmin _ob_r = default_supplier_admin();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        SupplierAdminHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_destroy(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        destroy();
        out = handler.createReply();
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_for_consumers(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.CosEventChannelAdmin.ConsumerAdmin _ob_r = for_consumers();
        out = handler.createReply();
        org.omg.CosEventChannelAdmin.ConsumerAdminHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_for_suppliers(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.CosEventChannelAdmin.SupplierAdmin _ob_r = for_suppliers();
        out = handler.createReply();
        org.omg.CosEventChannelAdmin.SupplierAdminHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_admin(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.CosNotification.Property[] _ob_r = get_admin();
        out = handler.createReply();
        org.omg.CosNotification.AdminPropertiesHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_all_consumeradmins(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int[] _ob_r = get_all_consumeradmins();
        out = handler.createReply();
        AdminIDSeqHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_all_supplieradmins(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int[] _ob_r = get_all_supplieradmins();
        out = handler.createReply();
        AdminIDSeqHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_consumeradmin(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            int _ob_a0 = AdminIDHelper.read(in);
            ConsumerAdmin _ob_r = get_consumeradmin(_ob_a0);
            out = handler.createReply();
            ConsumerAdminHelper.write(out, _ob_r);
        } catch (AdminNotFound _ob_ex) {
            out = handler.createExceptionReply();
            AdminNotFoundHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_qos(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.CosNotification.Property[] _ob_r = get_qos();
        out = handler.createReply();
        org.omg.CosNotification.QoSPropertiesHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_supplieradmin(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            int _ob_a0 = AdminIDHelper.read(in);
            SupplierAdmin _ob_r = get_supplieradmin(_ob_a0);
            out = handler.createReply();
            SupplierAdminHelper.write(out, _ob_r);
        } catch (AdminNotFound _ob_ex) {
            out = handler.createExceptionReply();
            AdminNotFoundHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_new_for_consumers(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        InterFilterGroupOperator _ob_a0 = InterFilterGroupOperatorHelper.read(in);
        org.omg.CORBA.IntHolder _ob_ah1 = new org.omg.CORBA.IntHolder();
        ConsumerAdmin _ob_r = new_for_consumers(_ob_a0, _ob_ah1);
        out = handler.createReply();
        ConsumerAdminHelper.write(out, _ob_r);
        AdminIDHelper.write(out, _ob_ah1.value);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_new_for_suppliers(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        InterFilterGroupOperator _ob_a0 = InterFilterGroupOperatorHelper.read(in);
        org.omg.CORBA.IntHolder _ob_ah1 = new org.omg.CORBA.IntHolder();
        SupplierAdmin _ob_r = new_for_suppliers(_ob_a0, _ob_ah1);
        out = handler.createReply();
        SupplierAdminHelper.write(out, _ob_r);
        AdminIDHelper.write(out, _ob_ah1.value);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_admin(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            org.omg.CosNotification.Property[] _ob_a0 = org.omg.CosNotification.AdminPropertiesHelper.read(in);
            set_admin(_ob_a0);
            out = handler.createReply();
        } catch (org.omg.CosNotification.UnsupportedAdmin _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosNotification.UnsupportedAdminHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_qos(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            org.omg.CosNotification.Property[] _ob_a0 = org.omg.CosNotification.QoSPropertiesHelper.read(in);
            set_qos(_ob_a0);
            out = handler.createReply();
        } catch (org.omg.CosNotification.UnsupportedQoS _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosNotification.UnsupportedQoSHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_validate_qos(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            org.omg.CosNotification.Property[] _ob_a0 = org.omg.CosNotification.QoSPropertiesHelper.read(in);
            org.omg.CosNotification.NamedPropertyRangeSeqHolder _ob_ah1 = new org.omg.CosNotification.NamedPropertyRangeSeqHolder();
            validate_qos(_ob_a0, _ob_ah1);
            out = handler.createReply();
            org.omg.CosNotification.NamedPropertyRangeSeqHelper.write(out, _ob_ah1.value);
        } catch (org.omg.CosNotification.UnsupportedQoS _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosNotification.UnsupportedQoSHelper.write(out, _ob_ex);
        }
        return out;
    }
}

package org.omg.CosNotifyChannelAdmin;

public abstract class ConsumerAdminPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, ConsumerAdminOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosNotifyChannelAdmin/ConsumerAdmin:1.0", "IDL:omg.org/CosNotification/QoSAdmin:1.0", "IDL:omg.org/CosNotifyComm/NotifySubscribe:1.0", "IDL:omg.org/CosNotifyFilter/FilterAdmin:1.0", "IDL:omg.org/CosEventChannelAdmin/ConsumerAdmin:1.0" };

    public ConsumerAdmin _this() {
        return ConsumerAdminHelper.narrow(super._this_object());
    }

    public ConsumerAdmin _this(org.omg.CORBA.ORB orb) {
        return ConsumerAdminHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "_get_MyChannel", "_get_MyID", "_get_MyOperator", "_get_lifetime_filter", "_get_priority_filter", "_get_pull_suppliers", "_get_push_suppliers", "_set_lifetime_filter", "_set_priority_filter", "add_filter", "destroy", "get_all_filters", "get_filter", "get_proxy_supplier", "get_qos", "obtain_notification_pull_supplier", "obtain_notification_push_supplier", "obtain_pull_supplier", "obtain_push_supplier", "remove_all_filters", "remove_filter", "set_qos", "subscription_change", "validate_qos" };
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
                return _OB_att_get_MyChannel(in, handler);
            case 1:
                return _OB_att_get_MyID(in, handler);
            case 2:
                return _OB_att_get_MyOperator(in, handler);
            case 3:
                return _OB_att_get_lifetime_filter(in, handler);
            case 4:
                return _OB_att_get_priority_filter(in, handler);
            case 5:
                return _OB_att_get_pull_suppliers(in, handler);
            case 6:
                return _OB_att_get_push_suppliers(in, handler);
            case 7:
                return _OB_att_set_lifetime_filter(in, handler);
            case 8:
                return _OB_att_set_priority_filter(in, handler);
            case 9:
                return _OB_op_add_filter(in, handler);
            case 10:
                return _OB_op_destroy(in, handler);
            case 11:
                return _OB_op_get_all_filters(in, handler);
            case 12:
                return _OB_op_get_filter(in, handler);
            case 13:
                return _OB_op_get_proxy_supplier(in, handler);
            case 14:
                return _OB_op_get_qos(in, handler);
            case 15:
                return _OB_op_obtain_notification_pull_supplier(in, handler);
            case 16:
                return _OB_op_obtain_notification_push_supplier(in, handler);
            case 17:
                return _OB_op_obtain_pull_supplier(in, handler);
            case 18:
                return _OB_op_obtain_push_supplier(in, handler);
            case 19:
                return _OB_op_remove_all_filters(in, handler);
            case 20:
                return _OB_op_remove_filter(in, handler);
            case 21:
                return _OB_op_set_qos(in, handler);
            case 22:
                return _OB_op_subscription_change(in, handler);
            case 23:
                return _OB_op_validate_qos(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_MyChannel(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        EventChannel _ob_r = MyChannel();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        EventChannelHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_MyID(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        int _ob_r = MyID();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        AdminIDHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_MyOperator(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        InterFilterGroupOperator _ob_r = MyOperator();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        InterFilterGroupOperatorHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_lifetime_filter(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CosNotifyFilter.MappingFilter _ob_r = lifetime_filter();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        org.omg.CosNotifyFilter.MappingFilterHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_priority_filter(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CosNotifyFilter.MappingFilter _ob_r = priority_filter();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        org.omg.CosNotifyFilter.MappingFilterHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_pull_suppliers(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        int[] _ob_r = pull_suppliers();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        ProxyIDSeqHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_push_suppliers(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        int[] _ob_r = push_suppliers();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        ProxyIDSeqHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_set_lifetime_filter(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CosNotifyFilter.MappingFilter _ob_a = org.omg.CosNotifyFilter.MappingFilterHelper.read(in);
        lifetime_filter(_ob_a);
        return handler.createReply();
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_set_priority_filter(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CosNotifyFilter.MappingFilter _ob_a = org.omg.CosNotifyFilter.MappingFilterHelper.read(in);
        priority_filter(_ob_a);
        return handler.createReply();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_add_filter(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.CosNotifyFilter.Filter _ob_a0 = org.omg.CosNotifyFilter.FilterHelper.read(in);
        int _ob_r = add_filter(_ob_a0);
        out = handler.createReply();
        org.omg.CosNotifyFilter.FilterIDHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_destroy(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        destroy();
        out = handler.createReply();
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_all_filters(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int[] _ob_r = get_all_filters();
        out = handler.createReply();
        org.omg.CosNotifyFilter.FilterIDSeqHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_filter(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            int _ob_a0 = org.omg.CosNotifyFilter.FilterIDHelper.read(in);
            org.omg.CosNotifyFilter.Filter _ob_r = get_filter(_ob_a0);
            out = handler.createReply();
            org.omg.CosNotifyFilter.FilterHelper.write(out, _ob_r);
        } catch (org.omg.CosNotifyFilter.FilterNotFound _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosNotifyFilter.FilterNotFoundHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_proxy_supplier(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            int _ob_a0 = ProxyIDHelper.read(in);
            ProxySupplier _ob_r = get_proxy_supplier(_ob_a0);
            out = handler.createReply();
            ProxySupplierHelper.write(out, _ob_r);
        } catch (ProxyNotFound _ob_ex) {
            out = handler.createExceptionReply();
            ProxyNotFoundHelper.write(out, _ob_ex);
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

    private org.omg.CORBA.portable.OutputStream _OB_op_obtain_notification_pull_supplier(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            ClientType _ob_a0 = ClientTypeHelper.read(in);
            org.omg.CORBA.IntHolder _ob_ah1 = new org.omg.CORBA.IntHolder();
            ProxySupplier _ob_r = obtain_notification_pull_supplier(_ob_a0, _ob_ah1);
            out = handler.createReply();
            ProxySupplierHelper.write(out, _ob_r);
            ProxyIDHelper.write(out, _ob_ah1.value);
        } catch (AdminLimitExceeded _ob_ex) {
            out = handler.createExceptionReply();
            AdminLimitExceededHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_obtain_notification_push_supplier(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            ClientType _ob_a0 = ClientTypeHelper.read(in);
            org.omg.CORBA.IntHolder _ob_ah1 = new org.omg.CORBA.IntHolder();
            ProxySupplier _ob_r = obtain_notification_push_supplier(_ob_a0, _ob_ah1);
            out = handler.createReply();
            ProxySupplierHelper.write(out, _ob_r);
            ProxyIDHelper.write(out, _ob_ah1.value);
        } catch (AdminLimitExceeded _ob_ex) {
            out = handler.createExceptionReply();
            AdminLimitExceededHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_obtain_pull_supplier(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.CosEventChannelAdmin.ProxyPullSupplier _ob_r = obtain_pull_supplier();
        out = handler.createReply();
        org.omg.CosEventChannelAdmin.ProxyPullSupplierHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_obtain_push_supplier(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.CosEventChannelAdmin.ProxyPushSupplier _ob_r = obtain_push_supplier();
        out = handler.createReply();
        org.omg.CosEventChannelAdmin.ProxyPushSupplierHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_remove_all_filters(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        remove_all_filters();
        out = handler.createReply();
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_remove_filter(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            int _ob_a0 = org.omg.CosNotifyFilter.FilterIDHelper.read(in);
            remove_filter(_ob_a0);
            out = handler.createReply();
        } catch (org.omg.CosNotifyFilter.FilterNotFound _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosNotifyFilter.FilterNotFoundHelper.write(out, _ob_ex);
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

    private org.omg.CORBA.portable.OutputStream _OB_op_subscription_change(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            org.omg.CosNotification.EventType[] _ob_a0 = org.omg.CosNotification.EventTypeSeqHelper.read(in);
            org.omg.CosNotification.EventType[] _ob_a1 = org.omg.CosNotification.EventTypeSeqHelper.read(in);
            subscription_change(_ob_a0, _ob_a1);
            out = handler.createReply();
        } catch (org.omg.CosNotifyComm.InvalidEventType _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosNotifyComm.InvalidEventTypeHelper.write(out, _ob_ex);
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

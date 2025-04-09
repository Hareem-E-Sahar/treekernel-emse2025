package org.omg.CosTypedNotifyChannelAdmin;

public abstract class TypedSupplierAdminPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, TypedSupplierAdminOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosTypedNotifyChannelAdmin/TypedSupplierAdmin:1.0", "IDL:omg.org/CosNotifyChannelAdmin/SupplierAdmin:1.0", "IDL:omg.org/CosNotification/QoSAdmin:1.0", "IDL:omg.org/CosNotifyComm/NotifyPublish:1.0", "IDL:omg.org/CosNotifyFilter/FilterAdmin:1.0", "IDL:omg.org/CosEventChannelAdmin/SupplierAdmin:1.0", "IDL:omg.org/CosTypedEventChannelAdmin/TypedSupplierAdmin:1.0" };

    public TypedSupplierAdmin _this() {
        return TypedSupplierAdminHelper.narrow(super._this_object());
    }

    public TypedSupplierAdmin _this(org.omg.CORBA.ORB orb) {
        return TypedSupplierAdminHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "_get_MyChannel", "_get_MyID", "_get_MyOperator", "_get_pull_consumers", "_get_push_consumers", "add_filter", "destroy", "get_all_filters", "get_filter", "get_proxy_consumer", "get_qos", "obtain_notification_pull_consumer", "obtain_notification_push_consumer", "obtain_pull_consumer", "obtain_push_consumer", "obtain_typed_notification_pull_consumer", "obtain_typed_notification_push_consumer", "obtain_typed_pull_consumer", "obtain_typed_push_consumer", "offer_change", "remove_all_filters", "remove_filter", "set_qos", "validate_qos" };
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
                return _OB_att_get_pull_consumers(in, handler);
            case 4:
                return _OB_att_get_push_consumers(in, handler);
            case 5:
                return _OB_op_add_filter(in, handler);
            case 6:
                return _OB_op_destroy(in, handler);
            case 7:
                return _OB_op_get_all_filters(in, handler);
            case 8:
                return _OB_op_get_filter(in, handler);
            case 9:
                return _OB_op_get_proxy_consumer(in, handler);
            case 10:
                return _OB_op_get_qos(in, handler);
            case 11:
                return _OB_op_obtain_notification_pull_consumer(in, handler);
            case 12:
                return _OB_op_obtain_notification_push_consumer(in, handler);
            case 13:
                return _OB_op_obtain_pull_consumer(in, handler);
            case 14:
                return _OB_op_obtain_push_consumer(in, handler);
            case 15:
                return _OB_op_obtain_typed_notification_pull_consumer(in, handler);
            case 16:
                return _OB_op_obtain_typed_notification_push_consumer(in, handler);
            case 17:
                return _OB_op_obtain_typed_pull_consumer(in, handler);
            case 18:
                return _OB_op_obtain_typed_push_consumer(in, handler);
            case 19:
                return _OB_op_offer_change(in, handler);
            case 20:
                return _OB_op_remove_all_filters(in, handler);
            case 21:
                return _OB_op_remove_filter(in, handler);
            case 22:
                return _OB_op_set_qos(in, handler);
            case 23:
                return _OB_op_validate_qos(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_MyChannel(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CosNotifyChannelAdmin.EventChannel _ob_r = MyChannel();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        org.omg.CosNotifyChannelAdmin.EventChannelHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_MyID(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        int _ob_r = MyID();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        org.omg.CosNotifyChannelAdmin.AdminIDHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_MyOperator(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator _ob_r = MyOperator();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        org.omg.CosNotifyChannelAdmin.InterFilterGroupOperatorHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_pull_consumers(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        int[] _ob_r = pull_consumers();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        org.omg.CosNotifyChannelAdmin.ProxyIDSeqHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_push_consumers(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        int[] _ob_r = push_consumers();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        org.omg.CosNotifyChannelAdmin.ProxyIDSeqHelper.write(out, _ob_r);
        return out;
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

    private org.omg.CORBA.portable.OutputStream _OB_op_get_proxy_consumer(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            int _ob_a0 = org.omg.CosNotifyChannelAdmin.ProxyIDHelper.read(in);
            org.omg.CosNotifyChannelAdmin.ProxyConsumer _ob_r = get_proxy_consumer(_ob_a0);
            out = handler.createReply();
            org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper.write(out, _ob_r);
        } catch (org.omg.CosNotifyChannelAdmin.ProxyNotFound _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosNotifyChannelAdmin.ProxyNotFoundHelper.write(out, _ob_ex);
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

    private org.omg.CORBA.portable.OutputStream _OB_op_obtain_notification_pull_consumer(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            org.omg.CosNotifyChannelAdmin.ClientType _ob_a0 = org.omg.CosNotifyChannelAdmin.ClientTypeHelper.read(in);
            org.omg.CORBA.IntHolder _ob_ah1 = new org.omg.CORBA.IntHolder();
            org.omg.CosNotifyChannelAdmin.ProxyConsumer _ob_r = obtain_notification_pull_consumer(_ob_a0, _ob_ah1);
            out = handler.createReply();
            org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper.write(out, _ob_r);
            org.omg.CosNotifyChannelAdmin.ProxyIDHelper.write(out, _ob_ah1.value);
        } catch (org.omg.CosNotifyChannelAdmin.AdminLimitExceeded _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosNotifyChannelAdmin.AdminLimitExceededHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_obtain_notification_push_consumer(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            org.omg.CosNotifyChannelAdmin.ClientType _ob_a0 = org.omg.CosNotifyChannelAdmin.ClientTypeHelper.read(in);
            org.omg.CORBA.IntHolder _ob_ah1 = new org.omg.CORBA.IntHolder();
            org.omg.CosNotifyChannelAdmin.ProxyConsumer _ob_r = obtain_notification_push_consumer(_ob_a0, _ob_ah1);
            out = handler.createReply();
            org.omg.CosNotifyChannelAdmin.ProxyConsumerHelper.write(out, _ob_r);
            org.omg.CosNotifyChannelAdmin.ProxyIDHelper.write(out, _ob_ah1.value);
        } catch (org.omg.CosNotifyChannelAdmin.AdminLimitExceeded _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosNotifyChannelAdmin.AdminLimitExceededHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_obtain_pull_consumer(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.CosEventChannelAdmin.ProxyPullConsumer _ob_r = obtain_pull_consumer();
        out = handler.createReply();
        org.omg.CosEventChannelAdmin.ProxyPullConsumerHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_obtain_push_consumer(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.CosEventChannelAdmin.ProxyPushConsumer _ob_r = obtain_push_consumer();
        out = handler.createReply();
        org.omg.CosEventChannelAdmin.ProxyPushConsumerHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_obtain_typed_notification_pull_consumer(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = KeyHelper.read(in);
            org.omg.CORBA.IntHolder _ob_ah1 = new org.omg.CORBA.IntHolder();
            TypedProxyPullConsumer _ob_r = obtain_typed_notification_pull_consumer(_ob_a0, _ob_ah1);
            out = handler.createReply();
            TypedProxyPullConsumerHelper.write(out, _ob_r);
            org.omg.CosNotifyChannelAdmin.ProxyIDHelper.write(out, _ob_ah1.value);
        } catch (org.omg.CosTypedEventChannelAdmin.NoSuchImplementation _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTypedEventChannelAdmin.NoSuchImplementationHelper.write(out, _ob_ex);
        } catch (org.omg.CosNotifyChannelAdmin.AdminLimitExceeded _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosNotifyChannelAdmin.AdminLimitExceededHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_obtain_typed_notification_push_consumer(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = KeyHelper.read(in);
            org.omg.CORBA.IntHolder _ob_ah1 = new org.omg.CORBA.IntHolder();
            TypedProxyPushConsumer _ob_r = obtain_typed_notification_push_consumer(_ob_a0, _ob_ah1);
            out = handler.createReply();
            TypedProxyPushConsumerHelper.write(out, _ob_r);
            org.omg.CosNotifyChannelAdmin.ProxyIDHelper.write(out, _ob_ah1.value);
        } catch (org.omg.CosTypedEventChannelAdmin.InterfaceNotSupported _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTypedEventChannelAdmin.InterfaceNotSupportedHelper.write(out, _ob_ex);
        } catch (org.omg.CosNotifyChannelAdmin.AdminLimitExceeded _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosNotifyChannelAdmin.AdminLimitExceededHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_obtain_typed_pull_consumer(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = org.omg.CosTypedEventChannelAdmin.KeyHelper.read(in);
            org.omg.CosEventChannelAdmin.ProxyPullConsumer _ob_r = obtain_typed_pull_consumer(_ob_a0);
            out = handler.createReply();
            org.omg.CosEventChannelAdmin.ProxyPullConsumerHelper.write(out, _ob_r);
        } catch (org.omg.CosTypedEventChannelAdmin.NoSuchImplementation _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTypedEventChannelAdmin.NoSuchImplementationHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_obtain_typed_push_consumer(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = org.omg.CosTypedEventChannelAdmin.KeyHelper.read(in);
            org.omg.CosTypedEventChannelAdmin.TypedProxyPushConsumer _ob_r = obtain_typed_push_consumer(_ob_a0);
            out = handler.createReply();
            org.omg.CosTypedEventChannelAdmin.TypedProxyPushConsumerHelper.write(out, _ob_r);
        } catch (org.omg.CosTypedEventChannelAdmin.InterfaceNotSupported _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTypedEventChannelAdmin.InterfaceNotSupportedHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_offer_change(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            org.omg.CosNotification.EventType[] _ob_a0 = org.omg.CosNotification.EventTypeSeqHelper.read(in);
            org.omg.CosNotification.EventType[] _ob_a1 = org.omg.CosNotification.EventTypeSeqHelper.read(in);
            offer_change(_ob_a0, _ob_a1);
            out = handler.createReply();
        } catch (org.omg.CosNotifyComm.InvalidEventType _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosNotifyComm.InvalidEventTypeHelper.write(out, _ob_ex);
        }
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

package org.omg.CosTypedNotifyChannelAdmin;

public abstract class TypedEventChannelFactoryPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, TypedEventChannelFactoryOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosTypedNotifyChannelAdmin/TypedEventChannelFactory:1.0" };

    public TypedEventChannelFactory _this() {
        return TypedEventChannelFactoryHelper.narrow(super._this_object());
    }

    public TypedEventChannelFactory _this(org.omg.CORBA.ORB orb) {
        return TypedEventChannelFactoryHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "create_typed_channel", "get_all_typed_channels", "get_typed_event_channel" };
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
                return _OB_op_create_typed_channel(in, handler);
            case 1:
                return _OB_op_get_all_typed_channels(in, handler);
            case 2:
                return _OB_op_get_typed_event_channel(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_create_typed_channel(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            org.omg.CosNotification.Property[] _ob_a0 = org.omg.CosNotification.QoSPropertiesHelper.read(in);
            org.omg.CosNotification.Property[] _ob_a1 = org.omg.CosNotification.AdminPropertiesHelper.read(in);
            org.omg.CORBA.IntHolder _ob_ah2 = new org.omg.CORBA.IntHolder();
            TypedEventChannel _ob_r = create_typed_channel(_ob_a0, _ob_a1, _ob_ah2);
            out = handler.createReply();
            TypedEventChannelHelper.write(out, _ob_r);
            org.omg.CosNotifyChannelAdmin.ChannelIDHelper.write(out, _ob_ah2.value);
        } catch (org.omg.CosNotification.UnsupportedQoS _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosNotification.UnsupportedQoSHelper.write(out, _ob_ex);
        } catch (org.omg.CosNotification.UnsupportedAdmin _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosNotification.UnsupportedAdminHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_all_typed_channels(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int[] _ob_r = get_all_typed_channels();
        out = handler.createReply();
        org.omg.CosNotifyChannelAdmin.ChannelIDSeqHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_typed_event_channel(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            int _ob_a0 = org.omg.CosNotifyChannelAdmin.ChannelIDHelper.read(in);
            TypedEventChannel _ob_r = get_typed_event_channel(_ob_a0);
            out = handler.createReply();
            TypedEventChannelHelper.write(out, _ob_r);
        } catch (org.omg.CosNotifyChannelAdmin.ChannelNotFound _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosNotifyChannelAdmin.ChannelNotFoundHelper.write(out, _ob_ex);
        }
        return out;
    }
}

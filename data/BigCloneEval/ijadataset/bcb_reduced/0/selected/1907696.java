package org.omg.CORBA.FT;

/**
 *	Generated from IDL definition of interface "ReplicationManagerEx"
 *	@author JacORB IDL compiler 
 */
public abstract class ReplicationManagerExPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, org.omg.CORBA.FT.ReplicationManagerExOperations {

    private static final java.util.Hashtable m_opsHash = new java.util.Hashtable();

    static {
        m_opsHash.put("get_properties", new java.lang.Integer(0));
        m_opsHash.put("get_object_group_ref", new java.lang.Integer(1));
        m_opsHash.put("push_structured_event", new java.lang.Integer(2));
        m_opsHash.put("set_primary_member", new java.lang.Integer(3));
        m_opsHash.put("get_fault_notifier", new java.lang.Integer(4));
        m_opsHash.put("create_member", new java.lang.Integer(5));
        m_opsHash.put("disconnect_structured_push_consumer", new java.lang.Integer(6));
        m_opsHash.put("create_object", new java.lang.Integer(7));
        m_opsHash.put("remove_default_properties", new java.lang.Integer(8));
        m_opsHash.put("get_default_properties", new java.lang.Integer(9));
        m_opsHash.put("set_default_properties", new java.lang.Integer(10));
        m_opsHash.put("set_properties_dynamically", new java.lang.Integer(11));
        m_opsHash.put("remove_type_properties", new java.lang.Integer(12));
        m_opsHash.put("register_fault_notifier", new java.lang.Integer(13));
        m_opsHash.put("get_object_group_id", new java.lang.Integer(14));
        m_opsHash.put("get_type_properties", new java.lang.Integer(15));
        m_opsHash.put("delete_object", new java.lang.Integer(16));
        m_opsHash.put("offer_change", new java.lang.Integer(17));
        m_opsHash.put("list_object_groups", new java.lang.Integer(18));
        m_opsHash.put("set_type_properties", new java.lang.Integer(19));
        m_opsHash.put("get_member_ref", new java.lang.Integer(20));
        m_opsHash.put("remove_member", new java.lang.Integer(21));
        m_opsHash.put("locations_of_members", new java.lang.Integer(22));
        m_opsHash.put("add_member", new java.lang.Integer(23));
    }

    private String[] ids = { "IDL:omg.org/CORBA/FT/ReplicationManagerEx:1.0", "IDL:omg.org/CORBA/FT/PropertyManager:1.0", "IDL:omg.org/CORBA/FT/ObjectGroupManager:1.0", "IDL:omg.org/CosNotifyComm/StructuredPushConsumer:1.0", "IDL:omg.org/CORBA/FT/GenericFactory:1.0", "IDL:omg.org/CosNotifyComm/NotifyPublish:1.0", "IDL:omg.org/CORBA/Object:1.0", "IDL:omg.org/CORBA/FT/ReplicationManager:1.0" };

    public org.omg.CORBA.FT.ReplicationManagerEx _this() {
        return org.omg.CORBA.FT.ReplicationManagerExHelper.narrow(_this_object());
    }

    public org.omg.CORBA.FT.ReplicationManagerEx _this(org.omg.CORBA.ORB orb) {
        return org.omg.CORBA.FT.ReplicationManagerExHelper.narrow(_this_object(orb));
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String method, org.omg.CORBA.portable.InputStream _input, org.omg.CORBA.portable.ResponseHandler handler) throws org.omg.CORBA.SystemException {
        org.omg.CORBA.portable.OutputStream _out = null;
        java.lang.Integer opsIndex = (java.lang.Integer) m_opsHash.get(method);
        if (null == opsIndex) throw new org.omg.CORBA.BAD_OPERATION(method + " not found");
        switch(opsIndex.intValue()) {
            case 0:
                {
                    try {
                        org.omg.CORBA.Object _arg0 = _input.read_Object();
                        _out = handler.createReply();
                        org.omg.CORBA.FT.PropertiesHelper.write(_out, get_properties(_arg0));
                    } catch (org.omg.CORBA.FT.ObjectGroupNotFound _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.ObjectGroupNotFoundHelper.write(_out, _ex0);
                    }
                    break;
                }
            case 1:
                {
                    try {
                        org.omg.CORBA.Object _arg0 = _input.read_Object();
                        _out = handler.createReply();
                        _out.write_Object(get_object_group_ref(_arg0));
                    } catch (org.omg.CORBA.FT.ObjectGroupNotFound _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.ObjectGroupNotFoundHelper.write(_out, _ex0);
                    }
                    break;
                }
            case 2:
                {
                    try {
                        org.omg.CosNotification.StructuredEvent _arg0 = org.omg.CosNotification.StructuredEventHelper.read(_input);
                        _out = handler.createReply();
                        push_structured_event(_arg0);
                    } catch (org.omg.CosEventComm.Disconnected _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CosEventComm.DisconnectedHelper.write(_out, _ex0);
                    }
                    break;
                }
            case 3:
                {
                    try {
                        org.omg.CORBA.Object _arg0 = _input.read_Object();
                        org.omg.CosNaming.NameComponent[] _arg1 = org.omg.CORBA.FT.LocationHelper.read(_input);
                        _out = handler.createReply();
                        _out.write_Object(set_primary_member(_arg0, _arg1));
                    } catch (org.omg.CORBA.FT.PrimaryNotSet _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.PrimaryNotSetHelper.write(_out, _ex0);
                    } catch (org.omg.CORBA.FT.MemberNotFound _ex1) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.MemberNotFoundHelper.write(_out, _ex1);
                    } catch (org.omg.CORBA.FT.BadReplicationStyle _ex2) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.BadReplicationStyleHelper.write(_out, _ex2);
                    } catch (org.omg.CORBA.FT.ObjectGroupNotFound _ex3) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.ObjectGroupNotFoundHelper.write(_out, _ex3);
                    }
                    break;
                }
            case 4:
                {
                    try {
                        _out = handler.createReply();
                        org.omg.CORBA.FT.FaultNotifierHelper.write(_out, get_fault_notifier());
                    } catch (org.omg.CORBA.FT.InterfaceNotFound _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.InterfaceNotFoundHelper.write(_out, _ex0);
                    }
                    break;
                }
            case 5:
                {
                    try {
                        org.omg.CORBA.Object _arg0 = _input.read_Object();
                        org.omg.CosNaming.NameComponent[] _arg1 = org.omg.CORBA.FT.LocationHelper.read(_input);
                        java.lang.String _arg2 = org.omg.CORBA.FT.TypeIdHelper.read(_input);
                        org.omg.CORBA.FT.Property[] _arg3 = org.omg.CORBA.FT.CriteriaHelper.read(_input);
                        _out = handler.createReply();
                        _out.write_Object(create_member(_arg0, _arg1, _arg2, _arg3));
                    } catch (org.omg.CORBA.FT.ObjectNotCreated _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.ObjectNotCreatedHelper.write(_out, _ex0);
                    } catch (org.omg.CORBA.FT.MemberAlreadyPresent _ex1) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.MemberAlreadyPresentHelper.write(_out, _ex1);
                    } catch (org.omg.CORBA.FT.CannotMeetCriteria _ex2) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.CannotMeetCriteriaHelper.write(_out, _ex2);
                    } catch (org.omg.CORBA.FT.ObjectGroupNotFound _ex3) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.ObjectGroupNotFoundHelper.write(_out, _ex3);
                    } catch (org.omg.CORBA.FT.InvalidCriteria _ex4) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.InvalidCriteriaHelper.write(_out, _ex4);
                    } catch (org.omg.CORBA.FT.NoFactory _ex5) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.NoFactoryHelper.write(_out, _ex5);
                    }
                    break;
                }
            case 6:
                {
                    _out = handler.createReply();
                    disconnect_structured_push_consumer();
                    break;
                }
            case 7:
                {
                    try {
                        java.lang.String _arg0 = org.omg.CORBA.FT.TypeIdHelper.read(_input);
                        org.omg.CORBA.FT.Property[] _arg1 = org.omg.CORBA.FT.CriteriaHelper.read(_input);
                        org.omg.CORBA.AnyHolder _arg2 = new org.omg.CORBA.AnyHolder();
                        _out = handler.createReply();
                        _out.write_Object(create_object(_arg0, _arg1, _arg2));
                        org.omg.CORBA.FT.GenericFactoryPackage.FactoryCreationIdHelper.write(_out, _arg2.value);
                    } catch (org.omg.CORBA.FT.ObjectNotCreated _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.ObjectNotCreatedHelper.write(_out, _ex0);
                    } catch (org.omg.CORBA.FT.CannotMeetCriteria _ex1) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.CannotMeetCriteriaHelper.write(_out, _ex1);
                    } catch (org.omg.CORBA.FT.InvalidProperty _ex2) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.InvalidPropertyHelper.write(_out, _ex2);
                    } catch (org.omg.CORBA.FT.InvalidCriteria _ex3) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.InvalidCriteriaHelper.write(_out, _ex3);
                    } catch (org.omg.CORBA.FT.NoFactory _ex4) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.NoFactoryHelper.write(_out, _ex4);
                    }
                    break;
                }
            case 8:
                {
                    try {
                        org.omg.CORBA.FT.Property[] _arg0 = org.omg.CORBA.FT.PropertiesHelper.read(_input);
                        _out = handler.createReply();
                        remove_default_properties(_arg0);
                    } catch (org.omg.CORBA.FT.InvalidProperty _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.InvalidPropertyHelper.write(_out, _ex0);
                    } catch (org.omg.CORBA.FT.UnsupportedProperty _ex1) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.UnsupportedPropertyHelper.write(_out, _ex1);
                    }
                    break;
                }
            case 9:
                {
                    _out = handler.createReply();
                    org.omg.CORBA.FT.PropertiesHelper.write(_out, get_default_properties());
                    break;
                }
            case 10:
                {
                    try {
                        org.omg.CORBA.FT.Property[] _arg0 = org.omg.CORBA.FT.PropertiesHelper.read(_input);
                        _out = handler.createReply();
                        set_default_properties(_arg0);
                    } catch (org.omg.CORBA.FT.InvalidProperty _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.InvalidPropertyHelper.write(_out, _ex0);
                    } catch (org.omg.CORBA.FT.UnsupportedProperty _ex1) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.UnsupportedPropertyHelper.write(_out, _ex1);
                    }
                    break;
                }
            case 11:
                {
                    try {
                        org.omg.CORBA.Object _arg0 = _input.read_Object();
                        org.omg.CORBA.FT.Property[] _arg1 = org.omg.CORBA.FT.PropertiesHelper.read(_input);
                        _out = handler.createReply();
                        set_properties_dynamically(_arg0, _arg1);
                    } catch (org.omg.CORBA.FT.InvalidProperty _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.InvalidPropertyHelper.write(_out, _ex0);
                    } catch (org.omg.CORBA.FT.ObjectGroupNotFound _ex1) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.ObjectGroupNotFoundHelper.write(_out, _ex1);
                    } catch (org.omg.CORBA.FT.UnsupportedProperty _ex2) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.UnsupportedPropertyHelper.write(_out, _ex2);
                    }
                    break;
                }
            case 12:
                {
                    try {
                        java.lang.String _arg0 = org.omg.CORBA.FT.TypeIdHelper.read(_input);
                        org.omg.CORBA.FT.Property[] _arg1 = org.omg.CORBA.FT.PropertiesHelper.read(_input);
                        _out = handler.createReply();
                        remove_type_properties(_arg0, _arg1);
                    } catch (org.omg.CORBA.FT.InvalidProperty _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.InvalidPropertyHelper.write(_out, _ex0);
                    } catch (org.omg.CORBA.FT.UnsupportedProperty _ex1) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.UnsupportedPropertyHelper.write(_out, _ex1);
                    }
                    break;
                }
            case 13:
                {
                    org.omg.CORBA.FT.FaultNotifier _arg0 = org.omg.CORBA.FT.FaultNotifierHelper.read(_input);
                    _out = handler.createReply();
                    register_fault_notifier(_arg0);
                    break;
                }
            case 14:
                {
                    try {
                        org.omg.CORBA.Object _arg0 = _input.read_Object();
                        _out = handler.createReply();
                        _out.write_ulonglong(get_object_group_id(_arg0));
                    } catch (org.omg.CORBA.FT.ObjectGroupNotFound _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.ObjectGroupNotFoundHelper.write(_out, _ex0);
                    }
                    break;
                }
            case 15:
                {
                    java.lang.String _arg0 = org.omg.CORBA.FT.TypeIdHelper.read(_input);
                    _out = handler.createReply();
                    org.omg.CORBA.FT.PropertiesHelper.write(_out, get_type_properties(_arg0));
                    break;
                }
            case 16:
                {
                    try {
                        org.omg.CORBA.Any _arg0 = org.omg.CORBA.FT.GenericFactoryPackage.FactoryCreationIdHelper.read(_input);
                        _out = handler.createReply();
                        delete_object(_arg0);
                    } catch (org.omg.CORBA.FT.ObjectNotFound _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.ObjectNotFoundHelper.write(_out, _ex0);
                    }
                    break;
                }
            case 17:
                {
                    try {
                        org.omg.CosNotification.EventType[] _arg0 = org.omg.CosNotification.EventTypeSeqHelper.read(_input);
                        org.omg.CosNotification.EventType[] _arg1 = org.omg.CosNotification.EventTypeSeqHelper.read(_input);
                        _out = handler.createReply();
                        offer_change(_arg0, _arg1);
                    } catch (org.omg.CosNotifyComm.InvalidEventType _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CosNotifyComm.InvalidEventTypeHelper.write(_out, _ex0);
                    }
                    break;
                }
            case 18:
                {
                    _out = handler.createReply();
                    org.omg.CORBA.FT.ObjectSeqHelper.write(_out, list_object_groups());
                    break;
                }
            case 19:
                {
                    try {
                        java.lang.String _arg0 = org.omg.CORBA.FT.TypeIdHelper.read(_input);
                        org.omg.CORBA.FT.Property[] _arg1 = org.omg.CORBA.FT.PropertiesHelper.read(_input);
                        _out = handler.createReply();
                        set_type_properties(_arg0, _arg1);
                    } catch (org.omg.CORBA.FT.InvalidProperty _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.InvalidPropertyHelper.write(_out, _ex0);
                    } catch (org.omg.CORBA.FT.UnsupportedProperty _ex1) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.UnsupportedPropertyHelper.write(_out, _ex1);
                    }
                    break;
                }
            case 20:
                {
                    try {
                        org.omg.CORBA.Object _arg0 = _input.read_Object();
                        org.omg.CosNaming.NameComponent[] _arg1 = org.omg.CORBA.FT.LocationHelper.read(_input);
                        _out = handler.createReply();
                        _out.write_Object(get_member_ref(_arg0, _arg1));
                    } catch (org.omg.CORBA.FT.MemberNotFound _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.MemberNotFoundHelper.write(_out, _ex0);
                    } catch (org.omg.CORBA.FT.ObjectGroupNotFound _ex1) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.ObjectGroupNotFoundHelper.write(_out, _ex1);
                    }
                    break;
                }
            case 21:
                {
                    try {
                        org.omg.CORBA.Object _arg0 = _input.read_Object();
                        org.omg.CosNaming.NameComponent[] _arg1 = org.omg.CORBA.FT.LocationHelper.read(_input);
                        _out = handler.createReply();
                        _out.write_Object(remove_member(_arg0, _arg1));
                    } catch (org.omg.CORBA.FT.MemberNotFound _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.MemberNotFoundHelper.write(_out, _ex0);
                    } catch (org.omg.CORBA.FT.ObjectGroupNotFound _ex1) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.ObjectGroupNotFoundHelper.write(_out, _ex1);
                    }
                    break;
                }
            case 22:
                {
                    try {
                        org.omg.CORBA.Object _arg0 = _input.read_Object();
                        _out = handler.createReply();
                        org.omg.CORBA.FT.LocationsHelper.write(_out, locations_of_members(_arg0));
                    } catch (org.omg.CORBA.FT.ObjectGroupNotFound _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.ObjectGroupNotFoundHelper.write(_out, _ex0);
                    }
                    break;
                }
            case 23:
                {
                    try {
                        org.omg.CORBA.Object _arg0 = _input.read_Object();
                        org.omg.CosNaming.NameComponent[] _arg1 = org.omg.CORBA.FT.LocationHelper.read(_input);
                        org.omg.CORBA.Object _arg2 = _input.read_Object();
                        _out = handler.createReply();
                        _out.write_Object(add_member(_arg0, _arg1, _arg2));
                    } catch (org.omg.CORBA.FT.ObjectNotAdded _ex0) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.ObjectNotAddedHelper.write(_out, _ex0);
                    } catch (org.omg.CORBA.INV_OBJREF _ex1) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.INV_OBJREFHelper.write(_out, _ex1);
                    } catch (org.omg.CORBA.FT.MemberAlreadyPresent _ex2) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.MemberAlreadyPresentHelper.write(_out, _ex2);
                    } catch (org.omg.CORBA.FT.ObjectGroupNotFound _ex3) {
                        _out = handler.createExceptionReply();
                        org.omg.CORBA.FT.ObjectGroupNotFoundHelper.write(_out, _ex3);
                    }
                    break;
                }
        }
        return _out;
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] obj_id) {
        return ids;
    }
}

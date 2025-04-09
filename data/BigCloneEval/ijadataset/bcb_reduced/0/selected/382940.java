package gov.lanl.ObservationManager;

public abstract class ObservationComponentPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, ObservationComponentOperations {

    static final String[] _ob_ids_ = { "IDL:lanl.gov/ObservationManager/ObservationComponent:1.0", "IDL:omg.org/DsObservationAccess/AccessComponent:1.0" };

    public ObservationComponent _this() {
        return ObservationComponentHelper.narrow(super._this_object());
    }

    public ObservationComponent _this(org.omg.CORBA.ORB orb) {
        return ObservationComponentHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "_get_coas_version", "_get_naming_service", "_get_pid_service", "_get_terminology_service", "_get_trader_service", "are_iterators_supported", "get_components", "get_current_time", "get_default_policies", "get_observation_mgr", "get_supported_codes", "get_supported_policies", "get_supported_qualifiers", "get_type_code_for_observation_type" };
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
                return _OB_att_get_coas_version(in, handler);
            case 1:
                return _OB_att_get_naming_service(in, handler);
            case 2:
                return _OB_att_get_pid_service(in, handler);
            case 3:
                return _OB_att_get_terminology_service(in, handler);
            case 4:
                return _OB_att_get_trader_service(in, handler);
            case 5:
                return _OB_op_are_iterators_supported(in, handler);
            case 6:
                return _OB_op_get_components(in, handler);
            case 7:
                return _OB_op_get_current_time(in, handler);
            case 8:
                return _OB_op_get_default_policies(in, handler);
            case 9:
                return _OB_op_get_observation_mgr(in, handler);
            case 10:
                return _OB_op_get_supported_codes(in, handler);
            case 11:
                return _OB_op_get_supported_policies(in, handler);
            case 12:
                return _OB_op_get_supported_qualifiers(in, handler);
            case 13:
                return _OB_op_get_type_code_for_observation_type(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_coas_version(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        String _ob_r = coas_version();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        out.write_string(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_naming_service(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CosNaming.NamingContext _ob_r = naming_service();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        org.omg.DsObservationAccess.NamingContextHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_pid_service(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.PersonIdService.IdentificationComponent _ob_r = pid_service();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        org.omg.DsObservationAccess.IdentificationComponentHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_terminology_service(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.TerminologyServices.TerminologyService _ob_r = terminology_service();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        org.omg.DsObservationAccess.TerminologyServiceHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_trader_service(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CosTrading.TraderComponents _ob_r = trader_service();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        org.omg.DsObservationAccess.TraderComponentsHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_are_iterators_supported(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        boolean _ob_r = are_iterators_supported();
        out = handler.createReply();
        out.write_boolean(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_components(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.DsObservationAccess.AccessComponentData _ob_r = get_components();
        out = handler.createReply();
        org.omg.DsObservationAccess.AccessComponentDataHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_current_time(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        String _ob_r = get_current_time();
        out = handler.createReply();
        org.omg.DsObservationAccess.TimeStampHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_default_policies(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.DsObservationAccess.NameValuePair[] _ob_r = get_default_policies();
        out = handler.createReply();
        org.omg.DsObservationAccess.QueryPolicySeqHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_observation_mgr(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        ObservationMgr _ob_r = get_observation_mgr();
        out = handler.createReply();
        ObservationMgrHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_supported_codes(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int _ob_a0 = in.read_ulong();
        org.omg.DsObservationAccess.QualifiedCodeIteratorHolder _ob_ah1 = new org.omg.DsObservationAccess.QualifiedCodeIteratorHolder();
        String[] _ob_r = get_supported_codes(_ob_a0, _ob_ah1);
        out = handler.createReply();
        org.omg.DsObservationAccess.QualifiedCodeStrSeqHelper.write(out, _ob_r);
        org.omg.DsObservationAccess.QualifiedCodeIteratorHelper.write(out, _ob_ah1.value);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_supported_policies(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        String[] _ob_r = get_supported_policies();
        out = handler.createReply();
        org.omg.DsObservationAccess.QualifiedCodeStrSeqHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_supported_qualifiers(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = org.omg.DsObservationAccess.QualifiedCodeStrHelper.read(in);
            String[] _ob_r = get_supported_qualifiers(_ob_a0);
            out = handler.createReply();
            org.omg.DsObservationAccess.QualifiedCodeStrSeqHelper.write(out, _ob_r);
        } catch (org.omg.DsObservationAccess.InvalidCodes _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.DsObservationAccess.InvalidCodesHelper.write(out, _ob_ex);
        } catch (org.omg.DsObservationAccess.NotImplemented _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.DsObservationAccess.NotImplementedHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_type_code_for_observation_type(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = org.omg.DsObservationAccess.QualifiedCodeStrHelper.read(in);
            org.omg.CORBA.TypeCode _ob_r = get_type_code_for_observation_type(_ob_a0);
            out = handler.createReply();
            org.omg.DsObservationAccess.TypeCodeHelper.write(out, _ob_r);
        } catch (org.omg.DsObservationAccess.InvalidCodes _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.DsObservationAccess.InvalidCodesHelper.write(out, _ob_ex);
        } catch (org.omg.DsObservationAccess.NotImplemented _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.DsObservationAccess.NotImplementedHelper.write(out, _ob_ex);
        }
        return out;
    }
}

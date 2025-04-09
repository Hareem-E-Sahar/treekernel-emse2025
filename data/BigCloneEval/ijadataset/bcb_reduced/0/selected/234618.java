package org.omg.CosTrading;

public abstract class LookupPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, LookupOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosTrading/Lookup:1.0", "IDL:omg.org/CosTrading/TraderComponents:1.0", "IDL:omg.org/CosTrading/SupportAttributes:1.0", "IDL:omg.org/CosTrading/ImportAttributes:1.0" };

    public Lookup _this() {
        return LookupHelper.narrow(super._this_object());
    }

    public Lookup _this(org.omg.CORBA.ORB orb) {
        return LookupHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "_get_admin_if", "_get_def_follow_policy", "_get_def_hop_count", "_get_def_match_card", "_get_def_return_card", "_get_def_search_card", "_get_link_if", "_get_lookup_if", "_get_max_follow_policy", "_get_max_hop_count", "_get_max_list", "_get_max_match_card", "_get_max_return_card", "_get_max_search_card", "_get_proxy_if", "_get_register_if", "_get_supports_dynamic_properties", "_get_supports_modifiable_properties", "_get_supports_proxy_offers", "_get_type_repos", "query" };
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
                return _OB_att_get_admin_if(in, handler);
            case 1:
                return _OB_att_get_def_follow_policy(in, handler);
            case 2:
                return _OB_att_get_def_hop_count(in, handler);
            case 3:
                return _OB_att_get_def_match_card(in, handler);
            case 4:
                return _OB_att_get_def_return_card(in, handler);
            case 5:
                return _OB_att_get_def_search_card(in, handler);
            case 6:
                return _OB_att_get_link_if(in, handler);
            case 7:
                return _OB_att_get_lookup_if(in, handler);
            case 8:
                return _OB_att_get_max_follow_policy(in, handler);
            case 9:
                return _OB_att_get_max_hop_count(in, handler);
            case 10:
                return _OB_att_get_max_list(in, handler);
            case 11:
                return _OB_att_get_max_match_card(in, handler);
            case 12:
                return _OB_att_get_max_return_card(in, handler);
            case 13:
                return _OB_att_get_max_search_card(in, handler);
            case 14:
                return _OB_att_get_proxy_if(in, handler);
            case 15:
                return _OB_att_get_register_if(in, handler);
            case 16:
                return _OB_att_get_supports_dynamic_properties(in, handler);
            case 17:
                return _OB_att_get_supports_modifiable_properties(in, handler);
            case 18:
                return _OB_att_get_supports_proxy_offers(in, handler);
            case 19:
                return _OB_att_get_type_repos(in, handler);
            case 20:
                return _OB_op_query(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_admin_if(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        Admin _ob_r = admin_if();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        AdminHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_def_follow_policy(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        FollowOption _ob_r = def_follow_policy();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        FollowOptionHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_def_hop_count(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        int _ob_r = def_hop_count();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_def_match_card(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        int _ob_r = def_match_card();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_def_return_card(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        int _ob_r = def_return_card();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_def_search_card(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        int _ob_r = def_search_card();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_link_if(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        Link _ob_r = link_if();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        LinkHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_lookup_if(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        Lookup _ob_r = lookup_if();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        LookupHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_max_follow_policy(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        FollowOption _ob_r = max_follow_policy();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        FollowOptionHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_max_hop_count(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        int _ob_r = max_hop_count();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_max_list(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        int _ob_r = max_list();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_max_match_card(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        int _ob_r = max_match_card();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_max_return_card(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        int _ob_r = max_return_card();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_max_search_card(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        int _ob_r = max_search_card();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_proxy_if(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        Proxy _ob_r = proxy_if();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        ProxyHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_register_if(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        Register _ob_r = register_if();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        RegisterHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_supports_dynamic_properties(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        boolean _ob_r = supports_dynamic_properties();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        out.write_boolean(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_supports_modifiable_properties(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        boolean _ob_r = supports_modifiable_properties();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        out.write_boolean(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_supports_proxy_offers(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        boolean _ob_r = supports_proxy_offers();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        out.write_boolean(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_type_repos(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.Object _ob_r = type_repos();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        TypeRepositoryHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_query(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = ServiceTypeNameHelper.read(in);
            String _ob_a1 = ConstraintHelper.read(in);
            String _ob_a2 = org.omg.CosTrading.LookupPackage.PreferenceHelper.read(in);
            Policy[] _ob_a3 = PolicySeqHelper.read(in);
            org.omg.CosTrading.LookupPackage.SpecifiedProps _ob_a4 = org.omg.CosTrading.LookupPackage.SpecifiedPropsHelper.read(in);
            int _ob_a5 = in.read_ulong();
            OfferSeqHolder _ob_ah6 = new OfferSeqHolder();
            OfferIteratorHolder _ob_ah7 = new OfferIteratorHolder();
            PolicyNameSeqHolder _ob_ah8 = new PolicyNameSeqHolder();
            query(_ob_a0, _ob_a1, _ob_a2, _ob_a3, _ob_a4, _ob_a5, _ob_ah6, _ob_ah7, _ob_ah8);
            out = handler.createReply();
            OfferSeqHelper.write(out, _ob_ah6.value);
            OfferIteratorHelper.write(out, _ob_ah7.value);
            PolicyNameSeqHelper.write(out, _ob_ah8.value);
        } catch (IllegalServiceType _ob_ex) {
            out = handler.createExceptionReply();
            IllegalServiceTypeHelper.write(out, _ob_ex);
        } catch (UnknownServiceType _ob_ex) {
            out = handler.createExceptionReply();
            UnknownServiceTypeHelper.write(out, _ob_ex);
        } catch (IllegalConstraint _ob_ex) {
            out = handler.createExceptionReply();
            IllegalConstraintHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.LookupPackage.IllegalPreference _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.LookupPackage.IllegalPreferenceHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.LookupPackage.IllegalPolicyName _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.LookupPackage.IllegalPolicyNameHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.LookupPackage.PolicyTypeMismatch _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.LookupPackage.PolicyTypeMismatchHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.LookupPackage.InvalidPolicyValue _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.LookupPackage.InvalidPolicyValueHelper.write(out, _ob_ex);
        } catch (IllegalPropertyName _ob_ex) {
            out = handler.createExceptionReply();
            IllegalPropertyNameHelper.write(out, _ob_ex);
        } catch (DuplicatePropertyName _ob_ex) {
            out = handler.createExceptionReply();
            DuplicatePropertyNameHelper.write(out, _ob_ex);
        } catch (DuplicatePolicyName _ob_ex) {
            out = handler.createExceptionReply();
            DuplicatePolicyNameHelper.write(out, _ob_ex);
        }
        return out;
    }
}

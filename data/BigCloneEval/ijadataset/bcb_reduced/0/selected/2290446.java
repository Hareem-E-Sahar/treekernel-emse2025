package org.omg.CosTrading;

public abstract class AdminPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, AdminOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosTrading/Admin:1.0", "IDL:omg.org/CosTrading/TraderComponents:1.0", "IDL:omg.org/CosTrading/SupportAttributes:1.0", "IDL:omg.org/CosTrading/ImportAttributes:1.0", "IDL:omg.org/CosTrading/LinkAttributes:1.0" };

    public Admin _this() {
        return AdminHelper.narrow(super._this_object());
    }

    public Admin _this(org.omg.CORBA.ORB orb) {
        return AdminHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "_get_admin_if", "_get_def_follow_policy", "_get_def_hop_count", "_get_def_match_card", "_get_def_return_card", "_get_def_search_card", "_get_link_if", "_get_lookup_if", "_get_max_follow_policy", "_get_max_hop_count", "_get_max_link_follow_policy", "_get_max_list", "_get_max_match_card", "_get_max_return_card", "_get_max_search_card", "_get_proxy_if", "_get_register_if", "_get_request_id_stem", "_get_supports_dynamic_properties", "_get_supports_modifiable_properties", "_get_supports_proxy_offers", "_get_type_repos", "list_offers", "list_proxies", "set_def_follow_policy", "set_def_hop_count", "set_def_match_card", "set_def_return_card", "set_def_search_card", "set_max_follow_policy", "set_max_hop_count", "set_max_link_follow_policy", "set_max_list", "set_max_match_card", "set_max_return_card", "set_max_search_card", "set_request_id_stem", "set_supports_dynamic_properties", "set_supports_modifiable_properties", "set_supports_proxy_offers", "set_type_repos" };
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
                return _OB_att_get_max_link_follow_policy(in, handler);
            case 11:
                return _OB_att_get_max_list(in, handler);
            case 12:
                return _OB_att_get_max_match_card(in, handler);
            case 13:
                return _OB_att_get_max_return_card(in, handler);
            case 14:
                return _OB_att_get_max_search_card(in, handler);
            case 15:
                return _OB_att_get_proxy_if(in, handler);
            case 16:
                return _OB_att_get_register_if(in, handler);
            case 17:
                return _OB_att_get_request_id_stem(in, handler);
            case 18:
                return _OB_att_get_supports_dynamic_properties(in, handler);
            case 19:
                return _OB_att_get_supports_modifiable_properties(in, handler);
            case 20:
                return _OB_att_get_supports_proxy_offers(in, handler);
            case 21:
                return _OB_att_get_type_repos(in, handler);
            case 22:
                return _OB_op_list_offers(in, handler);
            case 23:
                return _OB_op_list_proxies(in, handler);
            case 24:
                return _OB_op_set_def_follow_policy(in, handler);
            case 25:
                return _OB_op_set_def_hop_count(in, handler);
            case 26:
                return _OB_op_set_def_match_card(in, handler);
            case 27:
                return _OB_op_set_def_return_card(in, handler);
            case 28:
                return _OB_op_set_def_search_card(in, handler);
            case 29:
                return _OB_op_set_max_follow_policy(in, handler);
            case 30:
                return _OB_op_set_max_hop_count(in, handler);
            case 31:
                return _OB_op_set_max_link_follow_policy(in, handler);
            case 32:
                return _OB_op_set_max_list(in, handler);
            case 33:
                return _OB_op_set_max_match_card(in, handler);
            case 34:
                return _OB_op_set_max_return_card(in, handler);
            case 35:
                return _OB_op_set_max_search_card(in, handler);
            case 36:
                return _OB_op_set_request_id_stem(in, handler);
            case 37:
                return _OB_op_set_supports_dynamic_properties(in, handler);
            case 38:
                return _OB_op_set_supports_modifiable_properties(in, handler);
            case 39:
                return _OB_op_set_supports_proxy_offers(in, handler);
            case 40:
                return _OB_op_set_type_repos(in, handler);
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

    private org.omg.CORBA.portable.OutputStream _OB_att_get_max_link_follow_policy(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        FollowOption _ob_r = max_link_follow_policy();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        FollowOptionHelper.write(out, _ob_r);
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

    private org.omg.CORBA.portable.OutputStream _OB_att_get_request_id_stem(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        byte[] _ob_r = request_id_stem();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        org.omg.CosTrading.AdminPackage.OctetSeqHelper.write(out, _ob_r);
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

    private org.omg.CORBA.portable.OutputStream _OB_op_list_offers(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            int _ob_a0 = in.read_ulong();
            OfferIdSeqHolder _ob_ah1 = new OfferIdSeqHolder();
            OfferIdIteratorHolder _ob_ah2 = new OfferIdIteratorHolder();
            list_offers(_ob_a0, _ob_ah1, _ob_ah2);
            out = handler.createReply();
            OfferIdSeqHelper.write(out, _ob_ah1.value);
            OfferIdIteratorHelper.write(out, _ob_ah2.value);
        } catch (NotImplemented _ob_ex) {
            out = handler.createExceptionReply();
            NotImplementedHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_list_proxies(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            int _ob_a0 = in.read_ulong();
            OfferIdSeqHolder _ob_ah1 = new OfferIdSeqHolder();
            OfferIdIteratorHolder _ob_ah2 = new OfferIdIteratorHolder();
            list_proxies(_ob_a0, _ob_ah1, _ob_ah2);
            out = handler.createReply();
            OfferIdSeqHelper.write(out, _ob_ah1.value);
            OfferIdIteratorHelper.write(out, _ob_ah2.value);
        } catch (NotImplemented _ob_ex) {
            out = handler.createExceptionReply();
            NotImplementedHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_def_follow_policy(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        FollowOption _ob_a0 = FollowOptionHelper.read(in);
        FollowOption _ob_r = set_def_follow_policy(_ob_a0);
        out = handler.createReply();
        FollowOptionHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_def_hop_count(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int _ob_a0 = in.read_ulong();
        int _ob_r = set_def_hop_count(_ob_a0);
        out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_def_match_card(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int _ob_a0 = in.read_ulong();
        int _ob_r = set_def_match_card(_ob_a0);
        out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_def_return_card(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int _ob_a0 = in.read_ulong();
        int _ob_r = set_def_return_card(_ob_a0);
        out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_def_search_card(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int _ob_a0 = in.read_ulong();
        int _ob_r = set_def_search_card(_ob_a0);
        out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_max_follow_policy(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        FollowOption _ob_a0 = FollowOptionHelper.read(in);
        FollowOption _ob_r = set_max_follow_policy(_ob_a0);
        out = handler.createReply();
        FollowOptionHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_max_hop_count(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int _ob_a0 = in.read_ulong();
        int _ob_r = set_max_hop_count(_ob_a0);
        out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_max_link_follow_policy(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        FollowOption _ob_a0 = FollowOptionHelper.read(in);
        FollowOption _ob_r = set_max_link_follow_policy(_ob_a0);
        out = handler.createReply();
        FollowOptionHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_max_list(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int _ob_a0 = in.read_ulong();
        int _ob_r = set_max_list(_ob_a0);
        out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_max_match_card(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int _ob_a0 = in.read_ulong();
        int _ob_r = set_max_match_card(_ob_a0);
        out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_max_return_card(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int _ob_a0 = in.read_ulong();
        int _ob_r = set_max_return_card(_ob_a0);
        out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_max_search_card(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        int _ob_a0 = in.read_ulong();
        int _ob_r = set_max_search_card(_ob_a0);
        out = handler.createReply();
        out.write_ulong(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_request_id_stem(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        byte[] _ob_a0 = org.omg.CosTrading.AdminPackage.OctetSeqHelper.read(in);
        byte[] _ob_r = set_request_id_stem(_ob_a0);
        out = handler.createReply();
        org.omg.CosTrading.AdminPackage.OctetSeqHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_supports_dynamic_properties(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        boolean _ob_a0 = in.read_boolean();
        boolean _ob_r = set_supports_dynamic_properties(_ob_a0);
        out = handler.createReply();
        out.write_boolean(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_supports_modifiable_properties(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        boolean _ob_a0 = in.read_boolean();
        boolean _ob_r = set_supports_modifiable_properties(_ob_a0);
        out = handler.createReply();
        out.write_boolean(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_supports_proxy_offers(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        boolean _ob_a0 = in.read_boolean();
        boolean _ob_r = set_supports_proxy_offers(_ob_a0);
        out = handler.createReply();
        out.write_boolean(_ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_set_type_repos(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.CORBA.Object _ob_a0 = TypeRepositoryHelper.read(in);
        org.omg.CORBA.Object _ob_r = set_type_repos(_ob_a0);
        out = handler.createReply();
        TypeRepositoryHelper.write(out, _ob_r);
        return out;
    }
}

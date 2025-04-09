package org.omg.CosTrading;

public abstract class LinkPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, LinkOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosTrading/Link:1.0", "IDL:omg.org/CosTrading/TraderComponents:1.0", "IDL:omg.org/CosTrading/SupportAttributes:1.0", "IDL:omg.org/CosTrading/LinkAttributes:1.0" };

    public Link _this() {
        return LinkHelper.narrow(super._this_object());
    }

    public Link _this(org.omg.CORBA.ORB orb) {
        return LinkHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "_get_admin_if", "_get_link_if", "_get_lookup_if", "_get_max_link_follow_policy", "_get_proxy_if", "_get_register_if", "_get_supports_dynamic_properties", "_get_supports_modifiable_properties", "_get_supports_proxy_offers", "_get_type_repos", "add_link", "describe_link", "list_links", "modify_link", "remove_link" };
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
                return _OB_att_get_link_if(in, handler);
            case 2:
                return _OB_att_get_lookup_if(in, handler);
            case 3:
                return _OB_att_get_max_link_follow_policy(in, handler);
            case 4:
                return _OB_att_get_proxy_if(in, handler);
            case 5:
                return _OB_att_get_register_if(in, handler);
            case 6:
                return _OB_att_get_supports_dynamic_properties(in, handler);
            case 7:
                return _OB_att_get_supports_modifiable_properties(in, handler);
            case 8:
                return _OB_att_get_supports_proxy_offers(in, handler);
            case 9:
                return _OB_att_get_type_repos(in, handler);
            case 10:
                return _OB_op_add_link(in, handler);
            case 11:
                return _OB_op_describe_link(in, handler);
            case 12:
                return _OB_op_list_links(in, handler);
            case 13:
                return _OB_op_modify_link(in, handler);
            case 14:
                return _OB_op_remove_link(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_att_get_admin_if(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        Admin _ob_r = admin_if();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        AdminHelper.write(out, _ob_r);
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

    private org.omg.CORBA.portable.OutputStream _OB_att_get_max_link_follow_policy(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        FollowOption _ob_r = max_link_follow_policy();
        org.omg.CORBA.portable.OutputStream out = handler.createReply();
        FollowOptionHelper.write(out, _ob_r);
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

    private org.omg.CORBA.portable.OutputStream _OB_op_add_link(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = LinkNameHelper.read(in);
            Lookup _ob_a1 = LookupHelper.read(in);
            FollowOption _ob_a2 = FollowOptionHelper.read(in);
            FollowOption _ob_a3 = FollowOptionHelper.read(in);
            add_link(_ob_a0, _ob_a1, _ob_a2, _ob_a3);
            out = handler.createReply();
        } catch (org.omg.CosTrading.LinkPackage.IllegalLinkName _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.LinkPackage.IllegalLinkNameHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.LinkPackage.DuplicateLinkName _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.LinkPackage.DuplicateLinkNameHelper.write(out, _ob_ex);
        } catch (InvalidLookupRef _ob_ex) {
            out = handler.createExceptionReply();
            InvalidLookupRefHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.LinkPackage.DefaultFollowTooPermissive _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.LinkPackage.DefaultFollowTooPermissiveHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.LinkPackage.LimitingFollowTooPermissive _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.LinkPackage.LimitingFollowTooPermissiveHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_describe_link(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = LinkNameHelper.read(in);
            org.omg.CosTrading.LinkPackage.LinkInfo _ob_r = describe_link(_ob_a0);
            out = handler.createReply();
            org.omg.CosTrading.LinkPackage.LinkInfoHelper.write(out, _ob_r);
        } catch (org.omg.CosTrading.LinkPackage.IllegalLinkName _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.LinkPackage.IllegalLinkNameHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.LinkPackage.UnknownLinkName _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.LinkPackage.UnknownLinkNameHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_list_links(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        String[] _ob_r = list_links();
        out = handler.createReply();
        LinkNameSeqHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_modify_link(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = LinkNameHelper.read(in);
            FollowOption _ob_a1 = FollowOptionHelper.read(in);
            FollowOption _ob_a2 = FollowOptionHelper.read(in);
            modify_link(_ob_a0, _ob_a1, _ob_a2);
            out = handler.createReply();
        } catch (org.omg.CosTrading.LinkPackage.IllegalLinkName _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.LinkPackage.IllegalLinkNameHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.LinkPackage.UnknownLinkName _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.LinkPackage.UnknownLinkNameHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.LinkPackage.DefaultFollowTooPermissive _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.LinkPackage.DefaultFollowTooPermissiveHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.LinkPackage.LimitingFollowTooPermissive _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.LinkPackage.LimitingFollowTooPermissiveHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_remove_link(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = LinkNameHelper.read(in);
            remove_link(_ob_a0);
            out = handler.createReply();
        } catch (org.omg.CosTrading.LinkPackage.IllegalLinkName _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.LinkPackage.IllegalLinkNameHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.LinkPackage.UnknownLinkName _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.LinkPackage.UnknownLinkNameHelper.write(out, _ob_ex);
        }
        return out;
    }
}

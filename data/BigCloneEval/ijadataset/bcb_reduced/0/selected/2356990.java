package org.omg.CosTrading;

public abstract class ImportAttributesPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, ImportAttributesOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosTrading/ImportAttributes:1.0" };

    public ImportAttributes _this() {
        return ImportAttributesHelper.narrow(super._this_object());
    }

    public ImportAttributes _this(org.omg.CORBA.ORB orb) {
        return ImportAttributesHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "_get_def_follow_policy", "_get_def_hop_count", "_get_def_match_card", "_get_def_return_card", "_get_def_search_card", "_get_max_follow_policy", "_get_max_hop_count", "_get_max_list", "_get_max_match_card", "_get_max_return_card", "_get_max_search_card" };
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
                return _OB_att_get_def_follow_policy(in, handler);
            case 1:
                return _OB_att_get_def_hop_count(in, handler);
            case 2:
                return _OB_att_get_def_match_card(in, handler);
            case 3:
                return _OB_att_get_def_return_card(in, handler);
            case 4:
                return _OB_att_get_def_search_card(in, handler);
            case 5:
                return _OB_att_get_max_follow_policy(in, handler);
            case 6:
                return _OB_att_get_max_hop_count(in, handler);
            case 7:
                return _OB_att_get_max_list(in, handler);
            case 8:
                return _OB_att_get_max_match_card(in, handler);
            case 9:
                return _OB_att_get_max_return_card(in, handler);
            case 10:
                return _OB_att_get_max_search_card(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
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
}

public class Test {    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "_get_supports_dynamic_properties", "_get_supports_modifiable_properties", "_get_supports_proxy_offers", "_get_type_repos" };
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
                return _OB_att_get_supports_dynamic_properties(in, handler);
            case 1:
                return _OB_att_get_supports_modifiable_properties(in, handler);
            case 2:
                return _OB_att_get_supports_proxy_offers(in, handler);
            case 3:
                return _OB_att_get_type_repos(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }
}
public class Test {    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "create_constrained_propertysetdef", "create_initial_propertysetdef", "create_propertysetdef" };
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
                return _OB_op_create_constrained_propertysetdef(in, handler);
            case 1:
                return _OB_op_create_initial_propertysetdef(in, handler);
            case 2:
                return _OB_op_create_propertysetdef(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }
}
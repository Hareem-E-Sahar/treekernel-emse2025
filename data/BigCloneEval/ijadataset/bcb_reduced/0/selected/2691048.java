package SSLexample.hello;

/**
 * Class declaration
 *
 *
 * @author
 * @version $Revision: 1424 $ $Date: 2002-05-07 16:44:41 -0400 (Tue, 07 May 2002) $
 */
public abstract class HelloPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, HelloOperations {

    static final String[] _ob_ids_ = { "IDL:Hello:1.0" };

    /**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
    public Hello _this() {
        return HelloHelper.narrow(super._this_object());
    }

    /**
	 * Method declaration
	 *
	 *
	 * @param orb
	 *
	 * @return
	 *
	 * @see
	 */
    public Hello _this(org.omg.CORBA.ORB orb) {
        return HelloHelper.narrow(super._this_object(orb));
    }

    /**
	 * Method declaration
	 *
	 *
	 * @param poa
	 * @param objectId
	 *
	 * @return
	 *
	 * @see
	 */
    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    /**
	 * Method declaration
	 *
	 *
	 * @param opName
	 * @param in
	 * @param handler
	 *
	 * @return
	 *
	 * @see
	 */
    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "say_hello" };
        int _ob_left = 0;
        int _ob_right = _ob_names.length;
        int _ob_index = -1;
        while (_ob_left < _ob_right) {
            int _ob_m = (_ob_left + _ob_right) / 2;
            int _ob_res = _ob_names[_ob_m].compareTo(opName);
            if (_ob_res == 0) {
                _ob_index = _ob_m;
                break;
            } else if (_ob_res > 0) {
                _ob_right = _ob_m;
            } else {
                _ob_left = _ob_m + 1;
            }
        }
        switch(_ob_index) {
            case 0:
                return _OB_op_say_hello(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    /**
	 * Method declaration
	 *
	 *
	 * @param in
	 * @param handler
	 *
	 * @return
	 *
	 * @see
	 */
    private org.omg.CORBA.portable.OutputStream _OB_op_say_hello(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        say_hello();
        out = handler.createReply();
        return out;
    }
}

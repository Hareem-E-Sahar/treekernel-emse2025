package gov.lanl.ObservationManager;

public abstract class ObservationMgrPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, ObservationMgrOperations {

    static final String[] _ob_ids_ = { "IDL:lanl.gov/ObservationManager/ObservationMgr:1.0" };

    public ObservationMgr _this() {
        return ObservationMgrHelper.narrow(super._this_object());
    }

    public ObservationMgr _this(org.omg.CORBA.ORB orb) {
        return ObservationMgrHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "add_observation", "cancel_editable_observation", "get_editable_observation", "update_observation" };
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
                return _OB_op_add_observation(in, handler);
            case 1:
                return _OB_op_cancel_editable_observation(in, handler);
            case 2:
                return _OB_op_get_editable_observation(in, handler);
            case 3:
                return _OB_op_update_observation(in, handler);
        }
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_add_observation(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        org.omg.CORBA.Any _ob_a0 = org.omg.DsObservationAccess.ObservationDataHelper.read(in);
        org.omg.DsObservationAccess.ObservationId _ob_r = add_observation(_ob_a0);
        out = handler.createReply();
        org.omg.DsObservationAccess.ObservationIdHelper.write(out, _ob_r);
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_cancel_editable_observation(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            org.omg.DsObservationAccess.ObservationId _ob_a0 = org.omg.DsObservationAccess.ObservationIdHelper.read(in);
            cancel_editable_observation(_ob_a0);
            out = handler.createReply();
        } catch (ObservationNotLockedException _ob_ex) {
            out = handler.createExceptionReply();
            ObservationNotLockedExceptionHelper.write(out, _ob_ex);
        } catch (ObservationNotFoundException _ob_ex) {
            out = handler.createExceptionReply();
            ObservationNotFoundExceptionHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_get_editable_observation(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            org.omg.DsObservationAccess.ObservationId _ob_a0 = org.omg.DsObservationAccess.ObservationIdHelper.read(in);
            org.omg.CORBA.Any _ob_r = get_editable_observation(_ob_a0);
            out = handler.createReply();
            org.omg.DsObservationAccess.ObservationDataHelper.write(out, _ob_r);
        } catch (ObservationNotFoundException _ob_ex) {
            out = handler.createExceptionReply();
            ObservationNotFoundExceptionHelper.write(out, _ob_ex);
        } catch (ObservationSignedException _ob_ex) {
            out = handler.createExceptionReply();
            ObservationSignedExceptionHelper.write(out, _ob_ex);
        } catch (ObservationLockedException _ob_ex) {
            out = handler.createExceptionReply();
            ObservationLockedExceptionHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_update_observation(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            org.omg.CORBA.Any _ob_a0 = org.omg.DsObservationAccess.ObservationDataHelper.read(in);
            org.omg.CORBA.Any _ob_r = update_observation(_ob_a0);
            out = handler.createReply();
            org.omg.DsObservationAccess.ObservationDataHelper.write(out, _ob_r);
        } catch (ObservationSignedException _ob_ex) {
            out = handler.createExceptionReply();
            ObservationSignedExceptionHelper.write(out, _ob_ex);
        } catch (ObservationNotLockedException _ob_ex) {
            out = handler.createExceptionReply();
            ObservationNotLockedExceptionHelper.write(out, _ob_ex);
        } catch (ObservationNotFoundException _ob_ex) {
            out = handler.createExceptionReply();
            ObservationNotFoundExceptionHelper.write(out, _ob_ex);
        }
        return out;
    }
}

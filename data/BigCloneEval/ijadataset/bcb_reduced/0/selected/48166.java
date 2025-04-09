package org.omg.CosTrading;

public abstract class RegisterPOA extends org.omg.PortableServer.Servant implements org.omg.CORBA.portable.InvokeHandler, RegisterOperations {

    static final String[] _ob_ids_ = { "IDL:omg.org/CosTrading/Register:1.0", "IDL:omg.org/CosTrading/TraderComponents:1.0", "IDL:omg.org/CosTrading/SupportAttributes:1.0" };

    public Register _this() {
        return RegisterHelper.narrow(super._this_object());
    }

    public Register _this(org.omg.CORBA.ORB orb) {
        return RegisterHelper.narrow(super._this_object(orb));
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return _ob_ids_;
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        final String[] _ob_names = { "_get_admin_if", "_get_link_if", "_get_lookup_if", "_get_proxy_if", "_get_register_if", "_get_supports_dynamic_properties", "_get_supports_modifiable_properties", "_get_supports_proxy_offers", "_get_type_repos", "describe", "export", "modify", "resolve", "withdraw", "withdraw_using_constraint" };
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
                return _OB_att_get_proxy_if(in, handler);
            case 4:
                return _OB_att_get_register_if(in, handler);
            case 5:
                return _OB_att_get_supports_dynamic_properties(in, handler);
            case 6:
                return _OB_att_get_supports_modifiable_properties(in, handler);
            case 7:
                return _OB_att_get_supports_proxy_offers(in, handler);
            case 8:
                return _OB_att_get_type_repos(in, handler);
            case 9:
                return _OB_op_describe(in, handler);
            case 10:
                return _OB_op_export(in, handler);
            case 11:
                return _OB_op_modify(in, handler);
            case 12:
                return _OB_op_resolve(in, handler);
            case 13:
                return _OB_op_withdraw(in, handler);
            case 14:
                return _OB_op_withdraw_using_constraint(in, handler);
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

    private org.omg.CORBA.portable.OutputStream _OB_op_describe(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = OfferIdHelper.read(in);
            org.omg.CosTrading.RegisterPackage.OfferInfo _ob_r = describe(_ob_a0);
            out = handler.createReply();
            org.omg.CosTrading.RegisterPackage.OfferInfoHelper.write(out, _ob_r);
        } catch (IllegalOfferId _ob_ex) {
            out = handler.createExceptionReply();
            IllegalOfferIdHelper.write(out, _ob_ex);
        } catch (UnknownOfferId _ob_ex) {
            out = handler.createExceptionReply();
            UnknownOfferIdHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.RegisterPackage.ProxyOfferId _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.RegisterPackage.ProxyOfferIdHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_export(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            org.omg.CORBA.Object _ob_a0 = in.read_Object();
            String _ob_a1 = ServiceTypeNameHelper.read(in);
            Property[] _ob_a2 = PropertySeqHelper.read(in);
            String _ob_r = export(_ob_a0, _ob_a1, _ob_a2);
            out = handler.createReply();
            OfferIdHelper.write(out, _ob_r);
        } catch (org.omg.CosTrading.RegisterPackage.InvalidObjectRef _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.RegisterPackage.InvalidObjectRefHelper.write(out, _ob_ex);
        } catch (IllegalServiceType _ob_ex) {
            out = handler.createExceptionReply();
            IllegalServiceTypeHelper.write(out, _ob_ex);
        } catch (UnknownServiceType _ob_ex) {
            out = handler.createExceptionReply();
            UnknownServiceTypeHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.RegisterPackage.InterfaceTypeMismatch _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.RegisterPackage.InterfaceTypeMismatchHelper.write(out, _ob_ex);
        } catch (IllegalPropertyName _ob_ex) {
            out = handler.createExceptionReply();
            IllegalPropertyNameHelper.write(out, _ob_ex);
        } catch (PropertyTypeMismatch _ob_ex) {
            out = handler.createExceptionReply();
            PropertyTypeMismatchHelper.write(out, _ob_ex);
        } catch (ReadonlyDynamicProperty _ob_ex) {
            out = handler.createExceptionReply();
            ReadonlyDynamicPropertyHelper.write(out, _ob_ex);
        } catch (MissingMandatoryProperty _ob_ex) {
            out = handler.createExceptionReply();
            MissingMandatoryPropertyHelper.write(out, _ob_ex);
        } catch (DuplicatePropertyName _ob_ex) {
            out = handler.createExceptionReply();
            DuplicatePropertyNameHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_modify(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = OfferIdHelper.read(in);
            String[] _ob_a1 = PropertyNameSeqHelper.read(in);
            Property[] _ob_a2 = PropertySeqHelper.read(in);
            modify(_ob_a0, _ob_a1, _ob_a2);
            out = handler.createReply();
        } catch (NotImplemented _ob_ex) {
            out = handler.createExceptionReply();
            NotImplementedHelper.write(out, _ob_ex);
        } catch (IllegalOfferId _ob_ex) {
            out = handler.createExceptionReply();
            IllegalOfferIdHelper.write(out, _ob_ex);
        } catch (UnknownOfferId _ob_ex) {
            out = handler.createExceptionReply();
            UnknownOfferIdHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.RegisterPackage.ProxyOfferId _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.RegisterPackage.ProxyOfferIdHelper.write(out, _ob_ex);
        } catch (IllegalPropertyName _ob_ex) {
            out = handler.createExceptionReply();
            IllegalPropertyNameHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.RegisterPackage.UnknownPropertyName _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.RegisterPackage.UnknownPropertyNameHelper.write(out, _ob_ex);
        } catch (PropertyTypeMismatch _ob_ex) {
            out = handler.createExceptionReply();
            PropertyTypeMismatchHelper.write(out, _ob_ex);
        } catch (ReadonlyDynamicProperty _ob_ex) {
            out = handler.createExceptionReply();
            ReadonlyDynamicPropertyHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.RegisterPackage.MandatoryProperty _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.RegisterPackage.MandatoryPropertyHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.RegisterPackage.ReadonlyProperty _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.RegisterPackage.ReadonlyPropertyHelper.write(out, _ob_ex);
        } catch (DuplicatePropertyName _ob_ex) {
            out = handler.createExceptionReply();
            DuplicatePropertyNameHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_resolve(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String[] _ob_a0 = TraderNameHelper.read(in);
            Register _ob_r = resolve(_ob_a0);
            out = handler.createReply();
            RegisterHelper.write(out, _ob_r);
        } catch (org.omg.CosTrading.RegisterPackage.IllegalTraderName _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.RegisterPackage.IllegalTraderNameHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.RegisterPackage.UnknownTraderName _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.RegisterPackage.UnknownTraderNameHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.RegisterPackage.RegisterNotSupported _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.RegisterPackage.RegisterNotSupportedHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_withdraw(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = OfferIdHelper.read(in);
            withdraw(_ob_a0);
            out = handler.createReply();
        } catch (IllegalOfferId _ob_ex) {
            out = handler.createExceptionReply();
            IllegalOfferIdHelper.write(out, _ob_ex);
        } catch (UnknownOfferId _ob_ex) {
            out = handler.createExceptionReply();
            UnknownOfferIdHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.RegisterPackage.ProxyOfferId _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.RegisterPackage.ProxyOfferIdHelper.write(out, _ob_ex);
        }
        return out;
    }

    private org.omg.CORBA.portable.OutputStream _OB_op_withdraw_using_constraint(org.omg.CORBA.portable.InputStream in, org.omg.CORBA.portable.ResponseHandler handler) {
        org.omg.CORBA.portable.OutputStream out = null;
        try {
            String _ob_a0 = ServiceTypeNameHelper.read(in);
            String _ob_a1 = ConstraintHelper.read(in);
            withdraw_using_constraint(_ob_a0, _ob_a1);
            out = handler.createReply();
        } catch (IllegalServiceType _ob_ex) {
            out = handler.createExceptionReply();
            IllegalServiceTypeHelper.write(out, _ob_ex);
        } catch (UnknownServiceType _ob_ex) {
            out = handler.createExceptionReply();
            UnknownServiceTypeHelper.write(out, _ob_ex);
        } catch (IllegalConstraint _ob_ex) {
            out = handler.createExceptionReply();
            IllegalConstraintHelper.write(out, _ob_ex);
        } catch (org.omg.CosTrading.RegisterPackage.NoMatchingOffers _ob_ex) {
            out = handler.createExceptionReply();
            org.omg.CosTrading.RegisterPackage.NoMatchingOffersHelper.write(out, _ob_ex);
        }
        return out;
    }
}

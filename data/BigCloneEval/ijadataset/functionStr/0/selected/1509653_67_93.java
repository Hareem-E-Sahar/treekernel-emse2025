public class Test {    public XplDeclarator(String n_name, String n_internalname, String n_externalname, boolean n_donotrender, int n_storage, boolean n_atomicwrite, boolean n_atomicread) {
        p_name = "";
        p_internalname = "";
        p_externalname = "";
        p_donotrender = false;
        p_storage = XplVarstorage_enum.AUTO;
        p_doc = "";
        p_helpURL = "";
        p_ldsrc = "";
        p_iny = false;
        p_inydata = "";
        p_inyby = "";
        p_lddata = "";
        p_address = "";
        p_atomicwrite = false;
        p_atomicread = false;
        set_name(n_name);
        set_internalname(n_internalname);
        set_externalname(n_externalname);
        set_donotrender(n_donotrender);
        set_storage(n_storage);
        set_atomicwrite(n_atomicwrite);
        set_atomicread(n_atomicread);
        p_type = null;
        p_aliasref = null;
        p_i = null;
    }
}
public class Test {    public void copyKeyFieldsUpdate(OID oid) {
        throw BindingSupportImpl.getInstance().invalidOperation("Not allowed to read/write to a instance marked for deletion");
    }
}
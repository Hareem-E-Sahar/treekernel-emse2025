public class Test {    public void setInternalBooleanFieldAbs(int field, boolean newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation("Not allowed to read/write to a instance marked for deletion");
    }
}
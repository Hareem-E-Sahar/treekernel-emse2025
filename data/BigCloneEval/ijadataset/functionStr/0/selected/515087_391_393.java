public class Test {    public void setInternalObjectField(int field, Object newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation("Not allowed to read/write to a instance marked for deletion");
    }
}
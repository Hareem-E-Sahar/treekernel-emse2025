public class Test {    public void setInternalLongFieldAbs(int field, long newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation("Not allowed to read/write to a instance marked for deletion");
    }
}
public class Test {    public void setInternalStringFieldAbs(int field, String newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation("Not allowed to read/write to a instance marked for deletion");
    }
}
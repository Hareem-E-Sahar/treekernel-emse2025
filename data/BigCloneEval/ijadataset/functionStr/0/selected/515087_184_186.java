public class Test {    public void setInternalFloatFieldAbs(int field, float newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation("Not allowed to read/write to a instance marked for deletion");
    }
}
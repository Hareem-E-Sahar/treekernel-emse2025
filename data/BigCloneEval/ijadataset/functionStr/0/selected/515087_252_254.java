public class Test {    public void setFloatFieldAbs(int absFieldNo, float newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation("Not allowed to read/write to a instance marked for deletion");
    }
}
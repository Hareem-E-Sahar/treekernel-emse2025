public class Test {    public void setByteField(int stateFieldNo, byte newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation("Not allowed to read/write to a instance marked for deletion");
    }
}
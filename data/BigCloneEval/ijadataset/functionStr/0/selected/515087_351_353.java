public class Test {    public void setObjectField(int stateFieldNo, Object newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation("Not allowed to read/write to a instance marked for deletion");
    }
}
public class Test {    public void setCharField(int stateFieldNo, char newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation("Not allowed to read/write to a instance marked for deletion");
    }
}
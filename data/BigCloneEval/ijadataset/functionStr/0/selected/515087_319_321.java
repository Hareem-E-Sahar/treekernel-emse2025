public class Test {    public State getCopy() {
        throw BindingSupportImpl.getInstance().invalidOperation("Not allowed to read/write to a instance marked for deletion");
    }
}
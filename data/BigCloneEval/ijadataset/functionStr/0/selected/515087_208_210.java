public class Test {    public void updateAutoSetFieldsModified(Date now, State oldState) {
        throw BindingSupportImpl.getInstance().invalidOperation("Not allowed to read/write to a instance marked for deletion");
    }
}
public class Test {    public void copyOptimisticLockingField(State state) {
        throw BindingSupportImpl.getInstance().invalidOperation("Not allowed to read/write to a instance marked for deletion");
    }
}
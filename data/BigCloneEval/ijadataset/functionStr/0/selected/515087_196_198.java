public class Test {    public String getStringFieldAbs(int absFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation("Not allowed to read/write to a instance marked for deletion");
    }
}
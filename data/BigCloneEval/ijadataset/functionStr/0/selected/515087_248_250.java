public class Test {    public byte getByteFieldAbs(int absFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation("Not allowed to read/write to a instance marked for deletion");
    }
}
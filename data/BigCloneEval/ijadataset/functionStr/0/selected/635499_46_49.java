public class Test {    @Override
    public byte[] process(byte[] entireData) {
        return messageDigest.digest(entireData);
    }
}
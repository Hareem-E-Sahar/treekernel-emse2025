public class Test {    @Override
    public byte[] encrypt(byte[] data) {
        return md.digest(data);
    }
}
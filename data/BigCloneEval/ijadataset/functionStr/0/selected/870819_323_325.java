public class Test {    public byte[] generateHash(MessageDigest md) throws Exception {
        return md.digest();
    }
}
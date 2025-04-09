public class Test {    public static byte[] md5(byte[] data) {
        return getDigest().digest(data);
    }
}
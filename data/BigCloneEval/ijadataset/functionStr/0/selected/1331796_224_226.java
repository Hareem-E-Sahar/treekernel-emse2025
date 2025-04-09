public class Test {    public static byte[] generateKeyID(byte[] key) {
        return CCNDigestHelper.digest(key);
    }
}
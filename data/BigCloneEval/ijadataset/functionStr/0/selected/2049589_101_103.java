public class Test {    public static byte[] sha(byte[] data) {
        return getShaDigest().digest(data);
    }
}
public class Test {    public static byte[] getMD5(String str) {
        return md5.digest(str.getBytes());
    }
}
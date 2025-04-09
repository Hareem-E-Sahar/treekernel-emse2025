public class Test {    public static final byte[] digest(byte[] data) {
        md5.reset();
        return md5.digest(data);
    }
}
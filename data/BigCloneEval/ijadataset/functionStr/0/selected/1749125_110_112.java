public class Test {    public static byte[] getMd2(final byte[] data) {
        return getMd2Digest().digest(data);
    }
}
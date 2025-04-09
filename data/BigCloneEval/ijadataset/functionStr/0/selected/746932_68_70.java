public class Test {    public static String getHashString(MessageDigest md) {
        return asHex(md.digest());
    }
}
public class Test {    public static byte[] getKeyIdentifier(Key key) {
        return CCNDigestHelper.digest(key.getEncoded());
    }
}
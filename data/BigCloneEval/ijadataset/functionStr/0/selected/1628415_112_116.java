public class Test {    protected static String getHash(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.reset();
        return new String(digest.digest(password.getBytes("UTF-8")));
    }
}
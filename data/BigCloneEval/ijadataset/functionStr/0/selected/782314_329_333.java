public class Test {    private byte[] signSkDigest() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(ConsEnv.NAME_DIGEST);
        String s = new StringBuffer(name).append('$').append(pwds).append('#').toString();
        return md.digest((s + getSalt(s)).getBytes());
    }
}
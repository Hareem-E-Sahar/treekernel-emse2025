public class Test {    public static String encodePassword(String password, String algorithm) {
        if (password == null) return null;
        byte unencodedPassword[] = password.getBytes();
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (Exception e) {
            log.error("Exception: " + e);
            return password;
        }
        md.reset();
        md.update(unencodedPassword);
        byte encodedPassword[] = md.digest();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < encodedPassword.length; i++) {
            if ((encodedPassword[i] & 0xff) < 16) buf.append("0");
            buf.append(Long.toString(encodedPassword[i] & 0xff, 16));
        }
        return buf.toString();
    }
}
public class Test {    public static String doSHA1(String s) throws Throwable {
        String s1;
        MessageDigest messagedigest = MessageDigest.getInstance("SHA");
        byte abyte0[] = s.getBytes();
        messagedigest.update(abyte0, 0, s.length());
        byte abyte1[] = messagedigest.digest();
        BASE64Encoder base64encoder = new BASE64Encoder();
        s1 = base64encoder.encode(abyte1);
        return s1;
    }
}
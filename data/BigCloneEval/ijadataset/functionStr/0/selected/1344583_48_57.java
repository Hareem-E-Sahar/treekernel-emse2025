public class Test {    public static String md5(String source) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(source.getBytes());
            return getHexString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
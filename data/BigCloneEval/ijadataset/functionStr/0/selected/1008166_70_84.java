public class Test {    public static final String digestPassword(String password) {
        MessageDigest md;
        ByteArrayOutputStream bos;
        if (password == null || password.equals("")) return "";
        try {
            md = MessageDigest.getInstance("SHA");
            byte[] digest = md.digest(password.getBytes("iso-8859-1"));
            bos = new ByteArrayOutputStream();
            OutputStream encodedStream = MimeUtility.encode(bos, "base64");
            encodedStream.write(digest);
            return bos.toString("iso-8859-1");
        } catch (Exception _) {
            return "";
        }
    }
}
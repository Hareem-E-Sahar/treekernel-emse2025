public class Test {    private String md5(String senha, String hashStr) {
        String sen = "";
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        BigInteger hash = new BigInteger(1, md.digest((hashStr + senha).getBytes()));
        sen = hash.toString(16);
        return sen;
    }
}